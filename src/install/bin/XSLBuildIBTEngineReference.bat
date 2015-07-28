@ECHO OFF
@CLS

call SetSAFSEnvironment.bat

msxsl %SAFSXML%\XSLDriverCommands.xml     %SAFSXSL%\XSLCoreReferenceLists.XSL engine=TIDComponent -o %SAFSDOC%\TIDComponentDriverCommandsList.htm
msxsl %SAFSXML%\XSLComponentFunctions.xml %SAFSXSL%\XSLCoreReferenceLists.XSL engine=TIDComponent -o %SAFSDOC%\TIDComponentComponentFunctionsList.htm
msxsl %SAFSXML%\XSLEngineCommands.xml     %SAFSXSL%\XSLCoreReferenceLists.XSL engine=TIDComponent -o %SAFSDOC%\TIDComponentEngineCommandsList.htm
msxsl %SAFSXML%\XSLDriverCommands.xml     %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=TIDComponent -o %SAFSBIN%\TIDDriverCommandsPrep.bat
msxsl %SAFSXML%\XSLComponentFunctions.xml %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=TIDComponent -o %SAFSBIN%\TIDComponentFunctionsPrep.bat
msxsl %SAFSXML%\XSLEngineCommands.xml     %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=TIDComponent -o %SAFSBIN%\TIDEngineCommandsPrep.bat

call TIDDriverCommandsPrep.bat
call TIDComponentFunctionsPrep.bat
call TIDEngineCommandsPrep.bat
