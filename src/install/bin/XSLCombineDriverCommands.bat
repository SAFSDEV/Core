@ECHO OFF
@CLS

call SetSAFSEnvironment.bat

msxsl %SAFSXML%\XSLDriverCommands.xml %SAFSXSL%\XSLCombineDriverCommands.XSL -o %SAFSDATA%\XLSDriverCommands.csv
