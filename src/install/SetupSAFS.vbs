'******************************************************************************
'* 
'* SetupSAFS.vbs  --  DEPRECATED
'*
'* This script now simply launches the new SetupSAFS.wsf script.
'* 
'* Author: Carl Nagle
'* Original Release: OCT 23, 2003
'*         (CANAGL): DEC 19, 2003  Updated for Release2003.12.19
'*         (CANAGL): AUG 03, 2004  Updated for Release2004.08.13
'*         (CANAGL): AUG 11, 2004  Added the ability to bypass the user prompt
'*                                 and the addition of safsrational.jar
'*         (CANAGL): OCT 03, 2004  Fixed some bad prompts for SAFS-only installs.
'*         (CANAGL): DEC 13, 2004  Eliminated most informational popup dialogs.
'*         (DPERRY): DEC 13, 2004  Abort\Report Downrev Java version.
'*         (CANAGL): AUG 15, 2005  Added call to SetupRationalClasspath.VBS
'*         (CANAGL): JAN 05, 2006  Deprecated for SetupSAFS.wsf
'*
'* Copyright (C) SAS Institute
'* General Public License: http://www.opensource.org/licenses/gpl-license.php
'******************************************************************************

Dim shell, exec, args

WScript.Interactive = True

Set shell = WScript.CreateObject("WScript.Shell")
Set args  = WScript.Arguments

Dim arg
Dim cmdline

'Check Java availability and Version
'=====================================
On Error Resume Next

cmdline = "cscript SetupSAFS.wsf "

arg=""

' loop thru all args to pass along
'=================================
For i = 0 to args.Count -1
    arg = args(i)
        
    'wrap cmdline args in quotes if embedded space exists
    '====================================================
    if InStr(1, arg, " ", 1) > 0 then
        cmdline = cmdline &""""& arg &""" "
    else
        cmdline = cmdline & arg & " "
    end if
    
Next

' invoke SetupVBS.wsf with command-line args
'===========================================
On Error Resume Next
Set exec = shell.Exec(cmdline)

If Err.Number <> 0 then
    WScript.Interactive = True
    shell.Popup "Error <"& CStr(Err.Number) &":"& Err.Description &"> occurred while trying to launch Java.",0, "Installation Aborted.",16
    Wscript.Quit    
End if

On Error Goto 0

arg = ""
Do While Exec.Status = 0
    arg = arg & readall(exec)
    WScript.Sleep(3000)
Loop

Set shell = nothing
Set exec  = nothing

'*****************************************************************************
'* ReadAll  Streams
'*****************************************************************************
Function readall(exec)
    readall = ""
    if not exec.StdOut.AtEndOfStream then
        readall = "STDOUT:"& exec.StdOut.ReadAll        
    end if
    if not exec.StdErr.AtEndOfStream then
        readall = readall &"STDERR:"& exec.StdErr.ReadAll        
    end if
End function
