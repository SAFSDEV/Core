@ECHO OFF
@CLS

call SetSAFSEnvironment.bat

msxsl %SAFSXML%\XSLQuickReference.xml %SAFSXSL%\XSLQuickReferenceHTML.XSL engine=IOS -o %SAFSDOC%\IOSQuickReference.htm
