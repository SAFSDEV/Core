@ECHO OFF
@CLS

call SetSAFSEnvironment.bat

msxsl %SAFSXML%\XSLComponentFunctions.xml %SAFSXSL%\XSLComponentActionsMap.XSL -o %SAFSDATA%\XSLComponentActions.MAP
