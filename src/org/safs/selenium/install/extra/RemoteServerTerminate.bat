
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

cscript "%SELENIUM_PLUS%\extra\ProcessKiller.vbs" -process java.exe -command selenium-server-standalone -noprompt

cscript "%SELENIUM_PLUS%\extra\ProcessKiller.vbs" -process cmd.exe -command RemoteServer.bat -noprompt

cscript "%SELENIUM_PLUS%\extra\ProcessKiller.vbs" -process wscript.exe -command RemoteServerInstall.vbs -noprompt

REM kill any chromedriver.exe left behind after automation test
cscript "%SELENIUM_PLUS%\extra\ProcessKiller.vbs" -process "chromedriver.exe" -command "%SELENIUM_PLUS%\extra\chromedriver.exe"  -commandIgnoreCase -killall -noprompt

REM kill any IEDriverServer.exe left behind after automation test
cscript "%SELENIUM_PLUS%\extra\ProcessKiller.vbs" -process "IEDriverServer.exe" -command "%SELENIUM_PLUS%\extra\IEDriverServer.exe"  -commandIgnoreCase -killall -noprompt
