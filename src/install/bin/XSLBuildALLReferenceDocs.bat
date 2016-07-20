@ECHO OFF
@CLS
@TITLE Building SAFS Reference Documents

@ECHO Building Default Reference
START "Build Default Reference" /REALTIME /WAIT cmd.exe /C XSLBuildDDEngineReference.bat
@ECHO Building Default Quick Reference
START "Build Default Quick Reference" /REALTIME /WAIT cmd.exe /C XSLQuickReference.bat

SETLOCAL
REM ENGINES is case sensitive
SET ENGINES=TIDComponent IOS Android RobotJ Selenium TestComplete Autoit
FOR %%i IN (%ENGINES%) DO (
  @ECHO Building %%i Reference
  START "Build %%i Reference" /REALTIME /WAIT cmd.exe /C _XSLBuildEngineReference.bat %%i
  
  @ECHO Building %%i Quick Reference
  START "Build %%i Quick Reference" /REALTIME /WAIT cmd.exe /C _XSLQuickReference.bat %%i
)
ENDLOCAL

@ECHO Finished Building Reference Docs
PAUSE