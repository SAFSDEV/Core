@ECHO OFF
@CLS

call SetSAFSEnvironment.bat

msxsl %SAFSXML%\XSLDriverCommands.xml     %SAFSXSL%\XSLCoreReferenceLists.XSL engine=Android -o %SAFSDOC%\AndroidDriverCommandsList.htm
msxsl %SAFSXML%\XSLComponentFunctions.xml %SAFSXSL%\XSLCoreReferenceLists.XSL engine=Android -o %SAFSDOC%\AndroidComponentFunctionsList.htm
msxsl %SAFSXML%\XSLEngineCommands.xml     %SAFSXSL%\XSLCoreReferenceLists.XSL engine=Android -o %SAFSDOC%\AndroidEngineCommandsList.htm
msxsl %SAFSXML%\XSLDriverCommands.xml     %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=Android -o %SAFSBIN%\DRDDriverCommandsPrep.bat
msxsl %SAFSXML%\XSLComponentFunctions.xml %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=Android -o %SAFSBIN%\DRDComponentFunctionsPrep.bat
msxsl %SAFSXML%\XSLEngineCommands.xml     %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=Android -o %SAFSBIN%\DRDEngineCommandsPrep.bat

call DRDDriverCommandsPrep.bat
call DRDComponentFunctionsPrep.bat
call DRDEngineCommandsPrep.bat
