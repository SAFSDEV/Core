@ECHO OFF
@CLS

call SetSAFSEnvironment.bat

msxsl %SAFSXML%\XSLEngineCommands.xml %SAFSXSL%\XSLTCEngineCommandsInfo.XSL -o %SAFSDATA%\XLSTCEngineCommandsInfo.csv
