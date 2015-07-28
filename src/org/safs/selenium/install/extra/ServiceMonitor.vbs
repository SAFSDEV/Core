
Option Explicit

'******************************************************************************
'* ServiceMonitor.vbs
'* 
'* Optional script parameters:
'*            
'*     -noprompt
'*
'*            No prompt.  Silent execution.
'*     
'* Author: Carl Nagle
'* Original Release: DEC 04, 2013
'*
'* Copyright (C) SAS Institute
'* General Public License: http://www.opensource.org/licenses/gpl-license.php
'******************************************************************************

Dim WshShell, objWMIService, objProcess, colProcess, objLoc
Dim serviceprocess, command, title, cmdprocess, stafprocess, stafquery, servicequery, cmdquery
Dim prompt, arg, args, lcarg, cr, returncode, stafprocrunning

title = "Server Monitor"
serviceprocess = "java.exe"
cmdprocess = "cmd.exe"
command = "com.ibm.staf.service.STAFServiceHelper"
stafprocess = "STAFProc.exe"
cr = chr(13)  'carriage return
prompt = True

Dim i
Set WshShell = WScript.CreateObject("WScript.Shell")
Set args  = WScript.Arguments
arg = ""

' loop thru all args
'======================
For i = 0 to args.Count -1
    arg = args(i)        
    lcarg = lcase(arg)
    
    if (lcarg = "-noprompt") then
        prompt = False
    end if    
Next

Dim msg

If (prompt = True) Then 
   msg = "This program will monitor and terminate STAF Service processes as needed."& cr & cr
   msg = msg &"You can invoke this script with options:"& cr
   msg = msg &"---------------------------------------------------------------------"& cr 
   msg = msg &"-noprompt"& cr
   msg = msg &"Silent execution -- no dialogs."& cr & cr
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

stafprocrunning = false
stafquery = "Select * from Win32_Process WHERE Name = '"& stafprocess &"'"
servicequery = "Select * from Win32_Process WHERE Name = '"& serviceprocess &"'"
cmdquery = "Select * from Win32_Process WHERE Name = '"& cmdprocess &"'"
Do 
    'loop watching for STAFProc first
    Set colProcess = objWMIService.ExecQuery(stafquery)
    if colProcess.Count > 0 then
        Set colProcess = Nothing
        if stafprocrunning = false then
           if prompt = true then WshShell.Popup stafprocess &" detected.", 2, title, 0
        end if
        stafprocrunning = true   
    else
        'if it WAS running, time to attempt other shutdowns
        if stafprocrunning then
            stafprocrunning = false
            if prompt = true then WshShell.Popup stafprocess &" shutdown detected.", 2, title, 0
            Set colProcess = objWMIService.ExecQuery(servicequery)
	    For Each objProcess in colProcess
		    i = InStr(objProcess.CommandLine, command) 
		    if i > 0 then
			if prompt = true then
			    msg = "Found service process: "& objProcess.Name & cr & cr
			    msg = msg &"with command-line:"& cr & cr
			    msg = msg &"   "& objProcess.CommandLine & cr 
			    msg = msg &"---------------------------------------------------------------------"& cr & cr
			    msg = msg &"Click YES to proceed;  NO to Cancel"
			    returncode = WshShell.Popup(msg, 0, title, 36) 
			    If ((returncode = 2) Or (returncode = 7)) Then
				WScript.Quit
			    End If
			end if
			objProcess.Terminate()
		    end if	    
	    Next
	    Set colProcess = Nothing
	    WScript.Sleep(2000)
            Set colProcess = objWMIService.ExecQuery(cmdquery)
	    For Each objProcess in colProcess
		    i = InStr(objProcess.CommandLine, command) 
		    if i > 0 then
			if prompt = true then
			    msg = "Found service process: "& objProcess.Name & cr & cr
			    msg = msg &"with command-line:"& cr & cr
			    msg = msg &"   "& objProcess.CommandLine & cr 
			    msg = msg &"---------------------------------------------------------------------"& cr & cr
			    msg = msg &"Click YES to proceed;  NO to Cancel"
			    returncode = WshShell.Popup(msg, 0, title, 36) 
			    If ((returncode = 2) Or (returncode = 7)) Then
				WScript.Quit
			    End If
			end if
			objProcess.Terminate()
		    end if	    
	    Next
	end if
    end if
    Set colProcess = Nothing
    WScript.Sleep(3000)
    
'loop indefinitely
Loop while true

WScript.Quit
