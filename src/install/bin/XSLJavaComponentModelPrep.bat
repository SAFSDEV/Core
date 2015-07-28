@ECHO OFF
@CLS

call SetSAFSEnvironment.bat

msxsl %SAFSXML%\XSLComponentFunctions.xml %SAFSXSL%\XSLJavaComponentModelPrep.XSL -o %SAFSBIN%\_JavaComponentModelBuild.bat

call _JavaComponentModelBuild.bat
