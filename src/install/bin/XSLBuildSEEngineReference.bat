@ECHO OFF
@CLS

call SetSAFSEnvironment.bat

msxsl %SAFSXML%\XSLDriverCommands.xml     %SAFSXSL%\XSLCoreReferenceLists.XSL engine=Selenium -o %SAFSDOC%\SeleniumDriverCommandsList.htm
msxsl %SAFSXML%\XSLComponentFunctions.xml %SAFSXSL%\XSLCoreReferenceLists.XSL engine=Selenium -o %SAFSDOC%\SeleniumComponentFunctionsList.htm
msxsl %SAFSXML%\XSLEngineCommands.xml     %SAFSXSL%\XSLCoreReferenceLists.XSL engine=Selenium -o %SAFSDOC%\SeleniumEngineCommandsList.htm
msxsl %SAFSXML%\XSLDriverCommands.xml     %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=Selenium -o %SAFSBIN%\SeleniumDriverCommandsPrep.bat
msxsl %SAFSXML%\XSLComponentFunctions.xml %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=Selenium -o %SAFSBIN%\SeleniumComponentFunctionsPrep.bat
msxsl %SAFSXML%\XSLEngineCommands.xml     %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=Selenium -o %SAFSBIN%\SeleniumEngineCommandsPrep.bat

call SeleniumDriverCommandsPrep.bat
call SeleniumComponentFunctionsPrep.bat
call SeleniumEngineCommandsPrep.bat
