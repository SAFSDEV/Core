
'*************************************************************************************
' Uninstall SAFS
'* Assumes a global safsenv that is the Environment Variable for SAFS home directory
'* Returns 0: Uninstall not executed 
'*         -1: No SAFS detected
'*          1: Uninstall finished
'*************************************************************************************
Const groupTitle = "SAFS 2013.11"     'title of group on Windows program menu 
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
