@ECHO OFF
@CLS

call SetSAFSEnvironment.bat

msxsl %SAFSXML%\XSLQuickReference.xml %SAFSXSL%\XSLQuickReferenceHTML.XSL engine=Android -o %SAFSDOC%\DRDQuickReference.htm
