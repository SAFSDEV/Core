
@ECHO OFF

REM **************************************************************************
REM  Use ProcessKiller Script to iterate over the necessary processes 
REM  to terminate those we know should no longer be running and might 
REM  be deadlocked.
REM 
REM Author: Carl Nagle
REM Original Release: JAN 20, 2014
REM 
REM Copyright (C) SAS Institute
REM General Public License: http://www.opensource.org/licenses/gpl-license.php
REM **************************************************************************

cscript "%SAFSDIR%\bin\ProcessKiller.vbs" -process java.exe -command selenium-server-standalone -noprompt

cscript "%SAFSDIR%\bin\ProcessKiller.vbs" -process cmd.exe -command RemoteServer.bat -noprompt
