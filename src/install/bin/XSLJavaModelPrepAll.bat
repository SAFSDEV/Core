@ECHO OFF

SET KEYWORD_TYPES=Command Component
FOR %%i IN (%KEYWORD_TYPES%) DO (
    @ECHO Generating Java %%i Model files ...
    START "Generating Java %%i Model files" /REALTIME /WAIT cmd.exe /C XSLJava%%iModelPrep.bat
)