
'******************************************************************************
'* SAFSVersionHotswap.VBS
'*
'* This script will swap different SAFS versions into the SAFS\lib directory.
'* The script assumes that each subfolder of SAFS\lib is a different version.
'* The name of the subfolder is considered the name of the version.
'* The user is expected to create and populate these directories as desired.
'* 
'* All files in the specified version will be copied into and overwrite files
'* of the same name in the C:\SAFS\lib directory.
'* 
'* Sample Directory Structure:
'* 
'*    C:\SAFS\lib
'*    C:\SAFS\lib\production  (* highly recommended default)
'*    C:\SAFS\lib\development
'* 
'* The script will assume there are 2 "versions" of SAFS available for swapping:
'* 
'*    production
'*    development
'* 
'* If no command-line argument is provided then the script will provide the list 
'* of available SAFS versions and prompt the user to enter the version to use.
'* 
'* If a command-line argument is provided then the script assumes it is being 
'* batch-driven and will attempt to execute without any prompting.
'* However, if an invalid version is passed the script will display an error 
'* message for a brief period and then exit.
'* 
'* Sample invocations:
'* 
'*    1. Double-Click the VBS file, or
'*    2. Call\Launch SAFSVersionHotswap from command prompt or batch file:
'*       
'*       a) SAFSVersionHotswap
'*       b) SAFSVersionHotswap production
'*       c) SAFSVersionHotswap development
'*       d) SAFSVersionHotswap bogus
'* 
'* a) result: Interactive Mode prompting for user input
'* b) result: silent copying all files from C:\SAFS\lib\production into C:\SAFS\lib
'* c) result: silent copying all files from C:\SAFS\lib\development into C:\SAFS\lib
'* d) result: a brief error dialog because C:\SAFS\lib\bogus does not exist
'* 
'* Author: Carl Nagle
'* Original Release: DEC 17, 2008
'*
'* Copyright (C) SAS Institute
'* General Public License: http://www.opensource.org/licenses/gpl-license.php
'******************************************************************************

Dim shell
Dim env
Dim fso

Dim message
Dim response
Dim numerrors
Dim title

Dim safsdir, safsenv, libdir, libfolder, libfolders
Dim args, arg, prompt
Dim input, versiondir, versionfolder, versionfiles, f1

Dim cr
Dim q

cr        = chr(13)  'carriage return
q         = chr(34)  'dobule quote
numerrors = 0

title = "SAFS Version Hotswap"
input = ""
safsdir = "c:\SAFS" 'Default install directory
safsenv = "SAFSDIR" 'Environment Variable for SAFS root directory.


'WScript.Interactive = false

Set shell = WScript.CreateObject("WScript.Shell")
Set env   = shell.Environment("SYSTEM")
Set fso   = WScript.CreateObject("Scripting.FileSystemObject")
Set args = WScript.Arguments
if args.Count > 0 then
    input = args(0)
    prompt = False
else
    prompt = True
end if


'Do not prompt if properly command-line driven
'=============================================
if Len(input) = 0 then

    'User choice to continue version hotswap or not
    '============================================================
    message = "Swapping SAFS Version..."& cr & cr
    message = message &"Do you wish to proceed?"& cr & cr
    message = message &"Click YES to proceed; NO to cancel the install."

    '36=32+4  32=Question Icon; 4=Yes/No buttons
    response = shell.Popup (message,0, title,36)
    
    'possible responses:
    'Yes   = 6
    'No    = 7
    'Cancel= 2
    
    if response = 7 then WScript.Quit
end if

'================================
arg = env(safsenv)
if Len(arg) > 0 then safsdir = arg

libdir = safsdir &"\lib"

if (fso.FolderExists(libdir)) then
    Set libfolder  = fso.GetFolder(libdir)
    Set libfolders = libfolder.SubFolders
    if libfolders.Count > 0 then
	if Len(input) = 0 then
	    message = "The following versions are available:"&cr&cr
	    For Each f1 in libfolders
	        message = message &"    "& f1.Name &cr
	    Next
	    message = message & cr & "Enter the version to use:"
	    input = InputBox(message, "Enter SAFS Version", "production")
	end if
	
	if Len(input) > 0 then
	    versiondir = libdir &"\"& input
	    if (fso.FolderExists(versiondir)) then
	    	Set versionfolder = fso.GetFolder(versiondir)
	    	Set versionfiles = versionfolder.Files
	    	if versionfiles.Count > 0 then
	    	    For Each f1 in versionfiles
                        'shell.Popup "Copying "& f1.Name &" to "& libfolder.Path, 5, title, 48    
	    	        f1.Copy libfolder.Path &"\"& f1.Name, True
	    	    Next
	    	else
                    numerrors = numerrors + 1
                    shell.Popup "No SAFS Version Files to Copy! Nothing Swapped!", 5, title, 48    
	    	end if
	    else
        	numerrors = numerrors + 1
        	shell.Popup "Error SAFS version '"& input &"' does not exist! Nothing Swapped!", 5, title, 48    
	    end if
	else
            numerrors = numerrors + 1
            shell.Popup "No SAFS Version Specified! Nothing Swapped!", 5, title, 48
            prompt = False
	end if
    else
        numerrors = numerrors + 1
        shell.Popup "Error SAFS\lib contains no SubFolders! Nothing Swapped!", 5, title, 48    
    end if    
else
    shell.LogEvent 1, "Error folder SAFS\lib does not exist! Nothing Swapped!"
    shell.Popup "Error folder SAFS\lib does not exist! Nothing Swapped!", 5, title, 48
    numerrors = numerrors + 1
end if 

'Finished
'========
if (numerrors > 0) then
    if prompt then shell.Popup "SAFS Version Swap completed with errors!", 5, title, 48
else
    if prompt then shell.Popup "SAFS Version Swap completed successfully!", 5, title, 64
end if

Set shell = nothing
Set fso   = nothing
Set exec  = nothing
