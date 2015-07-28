@ECHO OFF
@CLS

call SetSAFSEnvironment.bat

msxsl %SAFSXML%\XSLDriverCommands.xml %SAFSXSL%\XSLTCDriverCommandsInfo.XSL -o %SAFSDATA%\XLSTCDriverCommandsInfo.csv
