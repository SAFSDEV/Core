
REM ***************************************************************************
REM This script is used to for installs on 32-bit and 64-bit Windows Systems.
REM It will attempt to detect the 32-bit versions of WSH and invoke that
REM instead of the default 64-bit version.
REM 
REM ***************************************************************************

IF EXIST %WINDIR%\SysWOW64\cscript.exe GOTO FORCE32
%WINDIR%\System32\cscript.exe "%CD%\install\Setup.WSF" "%CD%"
GOTO END

:FORCE32
%WINDIR%\SysWOW64\cscript.exe "%CD%\install\Setup.WSF" "%CD%"
:END