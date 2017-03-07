
REM ***************************************************************************
REM This script is used to for installs on 32-bit and 64-bit Windows Systems.
REM It will attempt to detect the 32-bit versions of WSH and invoke that
REM instead of the default 64-bit version.
REM 
REM ***************************************************************************

REM %CD% will be wrongly interpreted as "C:\Windows\System32" on some environment such as Windows 10, it is not stable.
REM SET CURRENT_DIRECTORY=%CD%\
SET CURRENT_DIRECTORY=%~dp0

IF EXIST %WINDIR%\SysWOW64\cscript.exe GOTO FORCE32
%WINDIR%\System32\cscript.exe "%CURRENT_DIRECTORY%install\Setup.WSF" "%CURRENT_DIRECTORY%"
GOTO END

:FORCE32
%WINDIR%\SysWOW64\cscript.exe "%CURRENT_DIRECTORY%install\Setup.WSF" "%CURRENT_DIRECTORY%"
:END