
Sub createShortcut( oshell, programDir, targetfile, title, arguments )
    Dim pos, workingdir, oShellLink
    Dim lnkFile, lnkExt
    lnkExt = ".lnk"
    if Right(targetfile, 4) = ".htm" OR _
       Right(targetfile, 4) = ".php" OR _
       Right(targetfile, 5) = ".html" then 
       lnkExt = ".url"
    end if
    lnkFile = programDir & "\" & title & lnkExt

    'get the working dir of targetfile
    pos = InStrRev(targetfile,"\")
    If (pos > 0) Then
        workingdir = Left(targetfile, pos-1)
    End If

    On Error Resume Next
    Set oShellLink = oshell.CreateShortcut(lnkFile)
    oShellLink.TargetPath = targetfile
    oShellLink.WindowStyle = 1
    oShellLink.WorkingDirectory = workingdir
    if (Len(arguments) > 0) then   
        oShellLink.Arguments = arguments
    end if
    oShellLink.Save
    On Error GoTo 0
    Set oShellLink = Nothing
End Sub
'*******************************************************************************
'* Assumes a global WScript.Shell object named 'shell'
'* Assumes a global Scripting.FileSystemObject object named 'fso'
'* 
'* Shortcuts created and linked to programs (Scripts, vbs, bat, html)
'*     "Start Debug Log"          ------> <ROOTDIR>\bin\DebugStartup.bat 
'*     "Shutdown Debug Log"       ------> <ROOTDIR>\bin\DebugShutdown.bat
'*     "Uninstall"                ------> <ROOTDIR>\uninstall\Uninstall.bat
'*     "Keywords Reference"       ------> <ROOTDIR>\doc\SAFSReference.htm
'*******************************************************************************
Const groupTitle = "SAFS 2016"     'title of group on Windows program menu 
Const docGroupTitle  = "Documentation"    'sub-group for documentation
Dim file, shell, fso, env, installdir, stafdir
Dim mainProgramsDir, safsProgramDir, safsDocProgramDir 

Set shell = WScript.CreateObject("WScript.Shell")
Set env   = shell.Environment("SYSTEM")
Set fso   = WScript.CreateObject("Scripting.FileSystemObject")

mainProgramsDir   = shell.SpecialFolders("AllUsersPrograms")
safsProgramDir    = mainProgramsDir & "\" & groupTitle	
safsDocProgramDir = safsProgramDir & "\" & docGroupTitle	
installdir = env("SAFSDIR")
stafdir = env("STAFDIR")

On Error Resume Next  

'nothing to do -- we don't seem to be installed successfully
if (Len(installdir) = 0) then 
    WScript.Quit
end if

'create program group and sub groups under it
fso.CreateFolder(safsProgramDir)
fso.CreateFolder(safsDocProgramDir) 

'create launcher shortcuts in program group     
'create Debug Log shortcuts in program group     
file = installdir & "\bin\SAFSTESTLOGStartup.bat"
createShortcut shell, safsProgramDir, file, "Start Debug Log", ""

file = installdir & "\bin\SAFSTESTLOGShutdown.bat"
createShortcut shell, safsProgramDir, file, "Shutdown Debug Log", ""

file = stafdir & "\startSTAFProc.bat"
createShortcut shell, safsProgramDir, file, "Start STAF", ""

file = stafdir & "\bin\STAF.exe"
createShortcut shell, safsProgramDir, file, "Shutdown STAF",  "local SHUTDOWN SHUTDOWN"

file = installdir & "\uninstall\Uninstall.bat"
createShortcut shell, safsProgramDir, file, "Uninstall", ""

'create shortcuts in Documentation group 
file = "http://safsdev.sourceforge.net/sqabasic2000/SAFSReference.php"
createShortcut shell, safsDocProgramDir, file, "Command Keyword Reference", ""

file = installdir & "\WhatsNewInSAFS.htm"
createShortcut shell, safsDocProgramDir, file, "Whats New In SAFS", ""

file = "http://safsdev.sourceforge.net/sqabasic2000/SAFSWINReleaseNotes2016.01.08.htm"
createShortcut shell, safsDocProgramDir, file, "Release Notes", ""

Set shell = nothing
Set env   = nothing
Set fso   = nothing

