@ECHO OFF
@CLS

call SetSAFSEnvironment.bat

msxsl %SAFSXML%\XSLQuickReference.xml %SAFSXSL%\XSLQuickReferenceHTML.XSL engine=RobotJ -o %SAFSDOC%\RJQuickReference.htm
