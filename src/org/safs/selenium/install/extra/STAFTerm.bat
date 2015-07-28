
@ECHO OFF

REM **************************************************************************
REM PROBLEM: If you kill STAFProc.exe while or before the SAFS services have 
REM          been able to unregister then you are virtually guaranteed to have 
REM          stray java.exe and cmd.exe processes left behind.
REM 
REM  SOLVED: Use ProcessKiller Script to iterate over the necessary processes 
REM          to terminate those we know should no longer be running and might 
REM          be deadlocked.
REM 
REM Author: Carl Nagle
REM Original Release: SEP 24, 2013
REM 
REM Copyright (C) SAS Institute
REM General Public License: http://www.opensource.org/licenses/gpl-license.php
REM **************************************************************************

cscript "%SELENIUM_PLUS%\extra\ProcessKiller.vbs" -process java.exe -command com.ibm.staf.service.STAFServiceHelper -noprompt

cscript "%SELENIUM_PLUS%\extra\ProcessKiller.vbs" -process cmd.exe -command com.ibm.staf.service.STAFServiceHelper -noprompt

cscript "%SELENIUM_PLUS%\extra\ProcessKiller.vbs" -process STAFProc.exe -command STAFProc.exe -noprompt

