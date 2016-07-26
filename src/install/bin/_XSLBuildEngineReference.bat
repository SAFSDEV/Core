@ECHO OFF
@CLS

REM This batch is supposed to call with a parameter 'engine', which could be one of TIDComponent IOS Android RobotJ Selenium TestComplete Autoit

call SetSAFSEnvironment.bat

setlocal
set ENGINE_NAME=%1

echo Generating html documents for engine %ENGINE_NAME% ...

msxsl %SAFSXML%\XSLDriverCommands.xml     %SAFSXSL%\XSLCoreReferenceLists.XSL engine=%ENGINE_NAME% -o %SAFSDOC%\%ENGINE_NAME%DriverCommandsList.htm
msxsl %SAFSXML%\XSLComponentFunctions.xml %SAFSXSL%\XSLCoreReferenceLists.XSL engine=%ENGINE_NAME% -o %SAFSDOC%\%ENGINE_NAME%ComponentFunctionsList.htm
msxsl %SAFSXML%\XSLEngineCommands.xml     %SAFSXSL%\XSLCoreReferenceLists.XSL engine=%ENGINE_NAME% -o %SAFSDOC%\%ENGINE_NAME%EngineCommandsList.htm
msxsl %SAFSXML%\XSLDriverCommands.xml     %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=%ENGINE_NAME% -o %SAFSBIN%\%ENGINE_NAME%DriverCommandsPrep.bat
msxsl %SAFSXML%\XSLComponentFunctions.xml %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=%ENGINE_NAME% -o %SAFSBIN%\%ENGINE_NAME%ComponentFunctionsPrep.bat
msxsl %SAFSXML%\XSLEngineCommands.xml     %SAFSXSL%\XSLCoreReferencePrep.XSL  engine=%ENGINE_NAME% -o %SAFSBIN%\%ENGINE_NAME%EngineCommandsPrep.bat

call %ENGINE_NAME%DriverCommandsPrep.bat
call %ENGINE_NAME%ComponentFunctionsPrep.bat
call %ENGINE_NAME%EngineCommandsPrep.bat

endlocal