'******************************************************************************
'* SAFSToolsStartup.vbs
'*
'* Starts the key SAFS services with Project-dependent location information.
'*
'* NOTE: STAF must already be running in order for these services to be started.
'*
'*
'* ARGUMENTS:
'* ==========
'*
'*    ARG(0): Input Filess Directory (Ex: "C:\Project\Datapool")
'*            Full path to the location where Test Tables and App Maps will be 
'*            found for the locally running project.
'*
'*    ARG(1): Logs Directory (Ex: "C:\Project\Datapool\Logs")
'*            Full path to the location where logs will be written for the 
'*            locally running project.
'*
'*    Both of these arguments are required for SAFS to operate correctly.
'*
'*
'* Example Invocations:
'* ====================
'*
'*  Command Line CScript.EXE:
'*  -------------------------
'*  cscript "C:\safs\bin\SAFSStartup.vbs" "C:\Project\Datapool" "C:\Project\Datapool\Logs"
'*
'*
'*  Windows Script WScript.EXE:
'*  ---------------------------
'*  wscript "C:\safs\bin\SAFSStartup.vbs" "C:\Project\Datapool" "C:\Project\Datapool\Logs"
'*
'*
'* Author: Carl Nagle
'* Original Release: OCT 27, 2003
'*          Updated: AUG 27, 2004
'*
'* Copyright (C) SAS Institute
'* General Public License: http://www.opensource.org/licenses/gpl-license.php
'******************************************************************************

Dim args, arg
Dim shell, env
Dim safsroot, jsafsroot
Dim stafprog
Dim cr

Set shell = WScript.CreateObject("WScript.Shell")
Set env   = shell.Environment("SYSTEM")
Set args  = WScript.Arguments
n = chr(10)


'JSTAF requires directory separators to be in UNIX format.
'==============================================================================
safsroot = env("SAFSDIR")
jsafsroot = Replace(safsroot, "\", "/")


'Exit with Help Info if insufficient arguments provided
'==============================================================================
if args.Count < 2 then    
    arg =       "Missing arguments!"& n 
    arg = arg & n 
    arg = arg & "Arg #0: Project Input Directory (...\Datapool)"& n
    arg = arg & n
    arg = arg & "Arg #1: Project Logs Directory (...\Datapool\Logs)"
    WScript.Echo arg
    WScript.Quit(-1)
end if


'Start SAFSMAPS with ARGS(0) 
'==============================================================================
stafprog = "staf local service add service safsmaps library jstaf execute "& jsafsroot &"/lib/safsmaps.jar PARMS dir "& args(0)
runprog stafprog


'Start SAFSVARS 
'==============================================================================
stafprog = "staf local service add service safsvars library jstaf execute "& jsafsroot &"/lib/safsvars.jar"
runprog stafprog


'Start SAFSLOGS with ARGS(1) 
'==============================================================================
stafprog = "staf local service add service safslogs library jstaf execute "& jsafsroot &"/lib/safslogs.jar PARMS dir "& args(1)
runprog stafprog


'Start SAFSINPUT with ARGS(0)
'==============================================================================
stafprog = "staf local service add service safsinput library jstaf execute "& jsafsroot &"/lib/safsinput.jar PARMS dir "& args(0)
runprog stafprog


'Finished
'==============================================================================
WScript.Quit(1)


'Subroutine to launch service and wait for initialization return
'==============================================================================
Sub runprog (command)
    Dim prog 
    Set prog = shell.Exec(command)
    Do While (prog.Status = 0)
        WScript.Sleep(1000)
    Loop
End Sub