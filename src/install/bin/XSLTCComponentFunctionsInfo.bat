@ECHO OFF
@CLS

call SetSAFSEnvironment.bat

msxsl %SAFSXML%\XSLComponentFunctions.xml %SAFSXSL%\XSLTCComponentFunctionsInfo.XSL -o %SAFSDATA%\XLSTCComponentFunctionsInfo.csv
