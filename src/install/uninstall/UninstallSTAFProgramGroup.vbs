
'*************************************************************************************
' Uninstall STAF Program Group
'*************************************************************************************
Const groupTitle = "STAF 3.4.26"     'title of group on Windows program menu 
Dim shell, fso
Dim mainProgramsDir, safsProgramDir

Set shell = WScript.CreateObject("WScript.Shell")
Set fso   = WScript.CreateObject("Scripting.FileSystemObject")

'uninstall program groups
mainProgramsDir   = shell.SpecialFolders("Programs")
safsProgramDir    = mainProgramsDir & "\" & groupTitle	

On Error Resume Next  
fso.DeleteFolder(safsProgramDir)

Set shell = nothing
Set fso   = nothing
