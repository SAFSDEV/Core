
REM ***************************************************************************
REM This script is used to launch SAFS installs on 64-bit Windows Systems.
REM It will attempt to detect the 32-bit versions of WSH and invoke that
REM instead of the default 64-bit version.
REM 
REM This script must exist in the same directory as the SAFS WSH\VBS scripts
REM it will try to execute.
REM ***************************************************************************

IF EXIST %WINDIR%\SysWOW64\cscript.exe GOTO FORCE32
%WINDIR%\System32\cscript.exe .\install\Setup.WSF "C:\SAFS" "C:\STAF"
GOTO END

:FORCE32
%WINDIR%\SysWOW64\cscript.exe .\install\Setup.WSF "C:\SAFS" "C:\STAF"
:END