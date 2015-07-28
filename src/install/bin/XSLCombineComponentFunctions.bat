@ECHO OFF
@CLS

call SetSAFSEnvironment.bat

msxsl %SAFSXML%\XSLComponentFunctions.xml %SAFSXSL%\XSLCombineComponentFunctions.XSL -o %SAFSDATA%\XLSComponentFunctions.csv
