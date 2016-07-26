@ECHO OFF
@CLS

REM This batch is supposed to call with a parameter 'engine', which could be one of TIDComponent IOS Android RobotJ Selenium TestComplete Autoit

call SetSAFSEnvironment.bat

echo Generating quick reference for engine %ENGINE_NAME% ...

setlocal
set ENGINE_NAME=%1
msxsl %SAFSXML%\XSLQuickReference.xml %SAFSXSL%\XSLQuickReferenceHTML.XSL engine=%ENGINE_NAME% -o %SAFSDOC%\%ENGINE_NAME%QuickReference.htm
endlocal