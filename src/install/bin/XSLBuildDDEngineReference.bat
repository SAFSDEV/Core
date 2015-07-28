@ECHO OFF
@CLS

call SetSAFSEnvironment.bat

%SAFSBIN%\msxsl %SAFSXML%\XSLDriverCommands.xml     %SAFSXSL%\XSLDDEngineReference.XSL  -o %SAFSDOC%\DDEngineReference.htm
%SAFSBIN%\msxsl %SAFSXML%\XSLDriverCommands.xml     %SAFSXSL%\XSLCoreReferenceLists.XSL -o %SAFSDOC%\DriverCommandsList.htm
%SAFSBIN%\msxsl %SAFSXML%\XSLComponentFunctions.xml %SAFSXSL%\XSLCoreReferenceLists.XSL -o %SAFSDOC%\ComponentFunctionsList.htm
%SAFSBIN%\msxsl %SAFSXML%\XSLEngineCommands.xml     %SAFSXSL%\XSLCoreReferenceLists.XSL -o %SAFSDOC%\EngineCommandsList.htm
%SAFSBIN%\msxsl %SAFSXML%\XSLDriverCommands.xml     %SAFSXSL%\XSLCoreReferencePrep.XSL  -o %SAFSBIN%\DriverCommandsPrep.bat
%SAFSBIN%\msxsl %SAFSXML%\XSLComponentFunctions.xml %SAFSXSL%\XSLCoreReferencePrep.XSL  -o %SAFSBIN%\ComponentFunctionsPrep.bat
%SAFSBIN%\msxsl %SAFSXML%\XSLEngineCommands.xml     %SAFSXSL%\XSLCoreReferencePrep.XSL  -o %SAFSBIN%\EngineCommandsPrep.bat

call DriverCommandsPrep.bat
call ComponentFunctionsPrep.bat
call EngineCommandsPrep.bat
