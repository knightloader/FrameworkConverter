# FrameworkConverter
project converting LoadRunner correlation rules into NeoLoad framework rule


#  How to use the Framework Converter 
FrameworkConvert.jar has 2 parameters :
- -I : input file ( NeoLoad correlation file)
- -O : output output NeoLoad xml framework file

Example of usage :
java -jar FrameworkConvert.jar -I "C:\knightloader\test1.cor" -O "C:\knightloader\test1.xml"
