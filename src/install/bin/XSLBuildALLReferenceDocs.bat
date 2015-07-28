
@ECHO OFF
@CLS
@TITLE Building All Docs

@ECHO Building Default Reference
start "Build ALL Reference" /REALTIME /WAIT cmd.exe /C XSLBuildDDEngineReference.bat
@ECHO Building IBT Reference
start "Build IBT Reference" /REALTIME /WAIT cmd.exe /C XSLBuildIBTEngineReference.bat
@ECHO Building IOS Reference
start "Build IOS  Reference" /REALTIME /WAIT cmd.exe /C XSLBuildIOSEngineReference.bat
@ECHO Building Android Reference
start "Build Android  Reference" /REALTIME /WAIT cmd.exe /C XSLBuildDRDEngineReference.bat
@ECHO Building RFT Reference
start "Build RFT Reference" /REALTIME /WAIT cmd.exe /C XSLBuildRJEngineReference.bat
@ECHO Building Selenium Reference
start "Build Selenium Reference" /REALTIME /WAIT cmd.exe /C XSLBuildSEEngineReference.bat
@ECHO Building TestComplete Reference
start "Build TestComplete Reference" /REALTIME /WAIT cmd.exe /C XSLBuildTCEngineReference.bat

@ECHO Building Default Quick Reference
start "Build Default Quick Reference" /REALTIME /WAIT cmd.exe /C XSLQuickReference.bat
@ECHO Building IBT Quick Reference
start "Build IBT Quick Reference" /REALTIME /WAIT cmd.exe /C XSLQuickReference_IBT.bat
@ECHO Building IOS Quick Reference
start "Build IOS Quick Reference" /REALTIME /WAIT cmd.exe /C XSLQuickReference_IOS.bat
@ECHO Building Android Quick Reference
start "Build Android Quick Reference" /REALTIME /WAIT cmd.exe /C XSLQuickReference_DRD.bat
@ECHO Building RFT Quick Reference
start "Build RFT Quick Reference" /REALTIME /WAIT cmd.exe /C XSLQuickReference_RJ.bat
@ECHO Building Selenium Quick Reference
start "Build Selenium Quick Reference" /REALTIME /WAIT cmd.exe /C XSLQuickReference_SE.bat
@ECHO Building TestComplete Quick Reference
start "Build TestComplete Quick Reference" /REALTIME /WAIT cmd.exe /C XSLQuickReference_TC.bat

@ECHO
@ECHO Finished Building Reference Docs
@ECHO
pause