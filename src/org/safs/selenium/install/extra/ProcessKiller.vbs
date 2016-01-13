
Option Explicit

'******************************************************************************
'* ProcessKiller.vbs
'* 
'* Optional script parameters:
'*            
'*     -process <process name>             Ex: java.exe  (default)
'*
'*            The process name (image name) of the process to seek in running processes.
'*
'*     -command <command-line substring>   Ex: com.ibm.staf.service.STAFServiceHelper
'*     
'*            Unique substring of command-line used to launch the process.
'*            (We don't want to kill the wrong process, right?)
'*
'*     -commandignorecase
'*     
'*            Ignore the command's case during the comparison with process's CommandLine
'*    
'*     -noprompt
'*
'*            No prompt.  Silent execution.
'*     
'* Author: Carl Nagle
'* Original Release: SEP 24, 2013
'*           Update: SEP 23, 2014 CANAGL Fix -noprompt when used as last argument
'*           Update: JAN 13, 2016 SBJLWA Add option -commandignorecase
'*
'* Copyright (C) SAS Institute
'* General Public License: http://www.opensource.org/licenses/gpl-license.php
'******************************************************************************

Dim WshShell, objWMIService, objProcess, colProcess, objLoc
Dim process, command, title
Dim prompt, arg, args, lcarg, cr, returncode, commandignorecase

title = "SAFS Process Killer"
process = "java.exe"
command = "com.ibm.staf.service.STAFServiceHelper"
cr = chr(13)  'carriage return
prompt = True
commandignorecase = False

Dim i
Set WshShell = WScript.CreateObject("WScript.Shell")
Set args  = WScript.Arguments
arg = ""

' loop thru all args
'======================
For i = 0 to args.Count -1
    arg = args(i)        
    lcarg = lcase(arg)
    
    if (lcarg = "-process")      then
        if ( i < args.Count -1) then 
            process = args(i+1)
        end if
    elseif (lcarg = "-command") then
        if ( i < args.Count -1) then
            command = args(i+1)
        end if
    elseif (lcarg = "-commandignorecase") then
        commandignorecase = True
    elseif (lcarg = "-noprompt") then
        prompt = False
    end if    
Next

Dim msg

If (prompt = True) Then 
   msg = "Do you wish to kill process '"& process &"'"& cr
   msg = msg &"matching Command Line: '"& command &"'?"& cr & cr
   msg = msg &"You can invoke this script with options:"& cr
   msg = msg &"---------------------------------------------------------------------"& cr 
   msg = msg &"-noprompt"& cr
   msg = msg &"Bypass this dialog."& cr & cr
   msg = msg &"-process <processName>"& cr
   msg = msg &"Default: java.exe"& cr & cr
   msg = msg &"-command <cmdLine substring>"& cr
   msg = msg &"Default: com.ibm.staf.service.STAFServiceHelper"& cr
   msg = msg &"---------------------------------------------------------------------"& cr & cr
   msg = msg &"Click YES to proceed;  NO to Cancel"
   returncode = WshShell.Popup(msg, 0, title, 36) 
   If ((returncode = 2) Or (returncode = 7)) Then
	WScript.Quit
   End If
end if

Set objLoc = CreateObject("wbemscripting.swbemlocator")
objLoc.Security_.privileges.addasstring "sedebugprivilege", true

Set objWMIService = GetObject("winmgmts:\\.\root\cimv2")
Set colProcess = objWMIService.ExecQuery _
    ("Select * from Win32_Process WHERE Name = '"& process &"'")
For Each objProcess in colProcess
	if commandignorecase then
		i = InStr(LCase(objProcess.CommandLine), LCase(command))
	else
		i = InStr(objProcess.CommandLine, command)
	end if
    if i > 0 then
        if (prompt = True) then
            msg = "Found process: "& objProcess.Name & cr & cr
            msg = msg &"with command-line:"& cr & cr
            msg = msg &"   "& objProcess.CommandLine & cr 
            msg = msg &"---------------------------------------------------------------------"& cr & cr
            msg = msg &"Click YES to proceed;  NO to Cancel"
            returncode = WshShell.Popup(msg, 0, title, 36) 
            If ((returncode = 2) Or (returncode = 7)) Then
                WScript.Quit
            End If
        end if
        ObjProcess.Terminate()
        WScript.Quit
    end if	    
Next	

WScript.Quit
