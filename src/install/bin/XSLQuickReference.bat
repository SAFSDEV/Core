@ECHO OFF
@CLS

call SetSAFSEnvironment.bat

echo Generating SAFS quick reference document...

msxsl %SAFSXML%\XSLQuickReference.xml %SAFSXSL%\XSLQuickReferenceHTML.XSL -o %SAFSDOC%\SAFSQuickReference.htm
