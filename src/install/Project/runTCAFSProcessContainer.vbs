Option Explicit

Dim env, message, details
Dim status
Dim projectname
Dim suitename
Dim scriptname
Dim command
Dim shell, exec
Dim args
Dim tcHome, tcExe, safsdir, temp

projectname = "ProcessContainer"
suitename = "\TCAFS\TCAFS.pjs"
scriptname = "ProcessContainer"
tcExe = "TestComplete.exe"

Set shell = WScript.CreateObject("WScript.Shell")
Set args  = WScript.Arguments
Set env   = shell.Environment("SYSTEM")

tcHome = env("TESTCOMPLETE_HOME")
if Len(tcHome) = 0 then
    WScript.Interactive = True
    shell.Popup "Path to TestComplete install directory was not deduced.",0, "Process Container Aborted.",16
    Wscript.Quit    
end if

temp = env("SAFSDIR")
suitename = temp & safsdir

temp = env("TESTCOMPLETE_EXE")
if Len(temp) > 0 then tcExe = temp
command = tcHome &"\bin\"& tcExe &" "& suitename & " /r /p:" & projectname & " /u:" & scriptname & " /rt:Main /e /SilentMode /ns"

message = "Command"
details = command
On Error Resume Next
Set exec = shell.Exec(command)      
