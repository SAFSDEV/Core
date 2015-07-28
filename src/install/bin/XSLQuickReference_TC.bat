@ECHO OFF
@CLS

call SetSAFSEnvironment.bat

msxsl %SAFSXML%\XSLQuickReference.xml %SAFSXSL%\XSLQuickReferenceHTML.XSL engine=TestComplete -o %SAFSDOC%\TCQuickReference.htm
