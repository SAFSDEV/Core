@ECHO OFF
@CLS

call SetSAFSEnvironment.bat

msxsl %SAFSXML%\XSLDriverCommands.xml     %SAFSXSL%\XSLCoreReferenceLists.XSL engine=RobotJ -o %SAFSDOC%\RobotJDriverCommandsList.htm
msxsl %SAFSXML%\XSLComponentFunctions.xml %SAFSXSL%\XSLCoreReferenceLists.XSL engine=RobotJ -o %SAFSDOC%\RobotJComponentFunctionsList.htm
msxsl %SAFSXML%\XSLEngineCommands.xml     %SAFSXSL%\XSLCoreReferenceLists.XSL engine=RobotJ -o %SAFSDOC%\RobotJEngineCommandsList.htm
msxsl %SAFSXML%\XSLDriverCommands.xml     %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=RobotJ -o %SAFSBIN%\RJDriverCommandsPrep.bat
msxsl %SAFSXML%\XSLComponentFunctions.xml %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=RobotJ -o %SAFSBIN%\RJComponentFunctionsPrep.bat
msxsl %SAFSXML%\XSLEngineCommands.xml     %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=RobotJ -o %SAFSBIN%\RJEngineCommandsPrep.bat

call RJDriverCommandsPrep.bat
call RJComponentFunctionsPrep.bat
call RJEngineCommandsPrep.bat
