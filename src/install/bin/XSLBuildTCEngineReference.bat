@ECHO OFF
@CLS

call SetSAFSEnvironment.bat

msxsl %SAFSXML%\XSLDriverCommands.xml     %SAFSXSL%\XSLCoreReferenceLists.XSL engine=TestComplete -o %SAFSDOC%\TestCompleteDriverCommandsList.htm
msxsl %SAFSXML%\XSLComponentFunctions.xml %SAFSXSL%\XSLCoreReferenceLists.XSL engine=TestComplete -o %SAFSDOC%\TestCompleteComponentFunctionsList.htm
msxsl %SAFSXML%\XSLEngineCommands.xml     %SAFSXSL%\XSLCoreReferenceLists.XSL engine=TestComplete -o %SAFSDOC%\TestCompleteEngineCommandsList.htm
msxsl %SAFSXML%\XSLDriverCommands.xml     %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=TestComplete -o %SAFSBIN%\TCDriverCommandsPrep.bat
msxsl %SAFSXML%\XSLComponentFunctions.xml %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=TestComplete -o %SAFSBIN%\TCComponentFunctionsPrep.bat
msxsl %SAFSXML%\XSLEngineCommands.xml     %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=TestComplete -o %SAFSBIN%\TCEngineCommandsPrep.bat

call TCDriverCommandsPrep.bat
call TCComponentFunctionsPrep.bat
call TCEngineCommandsPrep.bat
