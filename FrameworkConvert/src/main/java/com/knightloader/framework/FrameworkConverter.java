package com.knightloader.framework;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;

import java.io.*;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.SAXException;



public class FrameworkConverter
{
    private static Options options = new Options();

//    final static String strTemplatePathfile="template.xml";
    final static String strTemplatePathfile="template.xml";

    final static String SEARCH_CASE_SENSITIVE="9";
    final static String SEARCH_CASE_NONSENSITIVE="8";
    final static String SEARCH_DIGIT_REPLACEMENT="521";
    final static String SEARCH_REGEX="33289";
    final static String SEARCH_BODY="8";
    final static String SEARCH_HEADER="128";


    public static void main(String[] args)
    {
        CommandLineParser parser = new DefaultParser();
        String LR_CORR_FILE=null;
        String NL_FRAMEWORK_FILE=null;
        File lr_CORR_File=null;
        File nl_FRAMEWORKFile=null;
        File nl_templatefile;
        String leftboudary;
        String rightboudary;
        String Rulename;
        String FrameworkName;
        Element Frameworkrule;
        String flags;
        String type;

        options.addOption("O","NL_FRAMEWORK_FILE", true, "Path to the NL Framework Xml file");
        options.addOption("I","LR_CORR_FILE", true, "Path to the loadrunner correlation file");
        options.addOption("h", "help", false, "show help.");
        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption('h')) {
                help();
            }
            if (line.hasOption("LR_CORR_FILE")) {
                LR_CORR_FILE = line.getOptionValue("LR_CORR_FILE");
                if (LR_CORR_FILE == null) {
                    System.out.println("LR_CORR_FILE needs to be not null ");
                    help();
                }
                else {
                    lr_CORR_File= new File(LR_CORR_FILE);
                    if(!lr_CORR_File.exists())
                    {
                        System.out.println( LR_CORR_FILE+" doesn't exists "  );
                        help();
                    }
                }

            }
            if (line.hasOption("NL_FRAMEWORK_FILE"))
            {

                NL_FRAMEWORK_FILE = line.getOptionValue("NL_FRAMEWORK_FILE");
                if (NL_FRAMEWORK_FILE == null) {
                    System.out.println("NL_FRAMEWORK_FILE needs to be not null ");
                    help();
                }




            }
            if(CreateFrameworkFile(NL_FRAMEWORK_FILE)) {
                nl_FRAMEWORKFile = new File(NL_FRAMEWORK_FILE);
                if (lr_CORR_File != null) {

                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(lr_CORR_File);
                    doc.getDocumentElement().normalize();
                    NodeList GroupTag = doc.getElementsByTagName("Group");
                    //----get the name of the framework-----------------
                    Element gr = (Element) GroupTag.item(0);
                    FrameworkName = gr.getAttribute("Name");
                    //----------------------------------------------


                    //-----Initiate the framework File--------------
                    DocumentBuilderFactory NLFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder NLuilder = dbFactory.newDocumentBuilder();
                    Document NLdoc = NLuilder.parse(nl_FRAMEWORKFile);
                    NLdoc.getDocumentElement().normalize();

                    NodeList frameworklist = NLdoc.getElementsByTagName("framework");
                    Element fr = (Element) frameworklist.item(0);
                    fr.setAttribute("name", FrameworkName);

                    NodeList dynamicparameters = NLdoc.getElementsByTagName("dynamic-parameters");
                    Node rootelement = dynamicparameters.item(0);

                    NodeList dynamicparameter = NLdoc.getElementsByTagName("dynamic-parameter");
                    Element dynamicParam = (Element) dynamicparameter.item(0);
                    NodeList template = dynamicParam.getElementsByTagName("dp-request-injector");
                    Node tempelement = template.item(0);
                    //---------------------------------------------
                    NodeList nList = doc.getElementsByTagName("Rule");
                    for (int temp = 0; temp < nList.getLength(); temp++) {

                        Element nNode = (Element) nList.item(temp);
                        leftboudary = nNode.getAttribute("LeftBoundText");
                        rightboudary = nNode.getAttribute("RightBoundText");
                        flags = nNode.getAttribute("Flags");
                        type = nNode.getAttribute("Type");
                        Rulename = nNode.getAttribute("Name");

                        if (temp == 0) {
                            dynamicParam.setAttribute("name", Rulename);
                            dynamicParam.setAttribute("regexp", getNLRegexp(flags, leftboudary, rightboudary));
                            dynamicParam.setAttribute("extractionSource", getNLType(type));

                        } else {

                            Frameworkrule = NLdoc.createElement("dynamic-parameter");
                            Frameworkrule.setAttribute("name", Rulename);
                            Frameworkrule.setAttribute("regexp", getNLRegexp(flags, leftboudary, rightboudary));
                            Frameworkrule.setAttribute("extractionMethod", "REGEXP_ONLY");
                            Frameworkrule.setAttribute("extractionSource", getNLType(type));
                            Frameworkrule.setAttribute("template", "$1$");
                        //    Frameworkrule.appendChild(tempelement);
                            Element Injector =NLdoc.createElement("dp-request-injector");
                            Injector.setAttribute("injectionMethod","WHOLE_VALUE");
                            Injector.setAttribute("type","PARAMETER_VALUE");
                            Frameworkrule.appendChild(Injector);
                            rootelement.appendChild(Frameworkrule);
                        }
                    }
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(NLdoc);
                    StreamResult result = new StreamResult(nl_FRAMEWORKFile);
                    transformer.transform(source, result);
                    // Output to console for testing
                   // StreamResult consoleResult = new StreamResult(System.out);
                    //transformer.transform(source, consoleResult);

                }
            }
        }
        catch(ParseException e) {
            e.printStackTrace();
            System.exit(0);
        }
        catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

    }
    private static InputStream Gettemplatepath()
    {
             return FrameworkConverter.class.getResourceAsStream(strTemplatePathfile);
    }
    private static String EscapeRegexpCharacter( String boudary)
    {
        Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");
        boudary=SPECIAL_REGEX_CHARS.matcher(boudary).replaceAll("\\\\$0");

        return boudary;

    }
    private static String getNLRegexp(String LRFlag,String LRleft,String LRright)
    {
           String reg;
           String NLregexp = null;
            switch (LRFlag) {
                case SEARCH_CASE_SENSITIVE:
                    NLregexp = EscapeRegexpCharacter(LRleft) + GenerateRegexp(LRright) + EscapeRegexpCharacter(LRright);
                    break;
                case SEARCH_CASE_NONSENSITIVE:
                    NLregexp = EscapeRegexpCharacter(LRleft) + GenerateRegexp(LRright) + EscapeRegexpCharacter(LRright);
                    break;
                case SEARCH_DIGIT_REPLACEMENT:
                    NLregexp = LRleft.replaceAll("#", "\\\\d{1}" )+ GenerateRegexp(LRright) + LRright.replaceAll("#", "\\\\d{1}");
                    break;
                case SEARCH_REGEX:
                    NLregexp = LRleft;
                    break;
            }

        return NLregexp;
    }
    private static String GenerateRegexp(String rightBoundary)
    {
        String firstcharacter=rightBoundary.substring(0,1);

        return "([^"+EscapeRegexpCharacter(firstcharacter)+"]+)";

    }
    private static boolean CreateFrameworkFile(String NL_FRAMEWORK_FILE)
    {
        Boolean createframeworkresult=false;
         FileOutputStream outstream = null;
        InputStream input;
        try{

            input=Gettemplatepath();
            File outfile =new File(NL_FRAMEWORK_FILE);

             outstream = new FileOutputStream(outfile);

            byte[] buffer = new byte[1024];

            int length;
    	    /*copying the contents from input stream to
    	     * output stream using read and write methods
    	     */
            while ((length = input.read(buffer)) != -1){
                outstream.write(buffer, 0, length);
            }

            //Closing the input/output file streams
            input.close();
            outstream.close();

            createframeworkresult=true;

        }catch(IOException ioe){
            ioe.printStackTrace();
        }


        return createframeworkresult;
    }
    public static String getNLType(String LRtype)
    {
            String NLType=null;
            switch (LRtype) {
                case SEARCH_BODY:
                    NLType= "BODY";
                break;
                case SEARCH_HEADER:
                    NLType= "HEADERS";
                break;

            }
            return NLType;
    }


    private static void help() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "FrameworkConverter", options );
        System.exit(0);
    }
}
