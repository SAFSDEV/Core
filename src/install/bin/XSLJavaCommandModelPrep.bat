@ECHO OFF
@CLS

call SetSAFSEnvironment.bat

msxsl %SAFSXML%\XSLQuickReference.xml %SAFSXSL%\XSLJavaCommandModelPrep.XSL -o %SAFSBIN%\_JavaCommandModelBuild.bat

call _JavaCommandModelBuild.bat
