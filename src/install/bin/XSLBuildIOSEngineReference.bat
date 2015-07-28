@ECHO OFF
@CLS

call SetSAFSEnvironment.bat

msxsl %SAFSXML%\XSLDriverCommands.xml     %SAFSXSL%\XSLCoreReferenceLists.XSL engine=IOS -o %SAFSDOC%\IOSDriverCommandsList.htm
msxsl %SAFSXML%\XSLComponentFunctions.xml %SAFSXSL%\XSLCoreReferenceLists.XSL engine=IOS -o %SAFSDOC%\IOSComponentFunctionsList.htm
msxsl %SAFSXML%\XSLEngineCommands.xml     %SAFSXSL%\XSLCoreReferenceLists.XSL engine=IOS -o %SAFSDOC%\IOSEngineCommandsList.htm
msxsl %SAFSXML%\XSLDriverCommands.xml     %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=IOS -o %SAFSBIN%\IOSDriverCommandsPrep.bat
msxsl %SAFSXML%\XSLComponentFunctions.xml %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=IOS -o %SAFSBIN%\IOSComponentFunctionsPrep.bat
msxsl %SAFSXML%\XSLEngineCommands.xml     %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=IOS -o %SAFSBIN%\IOSEngineCommandsPrep.bat

call IOSDriverCommandsPrep.bat
call IOSComponentFunctionsPrep.bat
call IOSEngineCommandsPrep.bat
