
'*************************************************************************************
' Uninstall STAF Program Group
'*************************************************************************************
Const groupTitle = "STAF 3.4.11"     'title of group on Windows program menu 
Dim shell, fso
Dim mainProgramsDir, safsProgramDir
Dim i, everSupportedVersion(2) '(2) 2 means the UBound, not the length

everSupportedVersion(0) = "2.6.11"
everSupportedVersion(1) = "3.4.11"
everSupportedVersion(2) = "3.4.13"

WScript.Echo UBound(everSupportedVersion)

Set shell = WScript.CreateObject("WScript.Shell")
Set fso   = WScript.CreateObject("Scripting.FileSystemObject")

'uninstall program groups
mainProgramsDir   = shell.SpecialFolders("Programs")
safsProgramDir    = mainProgramsDir & "\" & groupTitle  

'If not exist, then try to detect it
If Not fso.FolderExists(safsProgramDir) Then
    For i = LBound(everSupportedVersion) to UBound(everSupportedVersion)
        safsProgramDir = mainProgramsDir & "\STAF " & everSupportedVersion(i)
        If fso.FolderExists(safsProgramDir) Then Exit For
    Next
End If

On Error Resume Next
Err.Clear
If Not (fso.FolderExists(safsProgramDir)) Then
    WScript.Echo "The Folder '" & safsProgramDir & "' does NOT exist."
    WScript.Quit 76 'code for "folder does not exist"
Else
    WScript.Echo "Tried to delete folder '" & safsProgramDir & "'"
    fso.DeleteFolder(safsProgramDir)
    If Err.Number = 0 Then
        WScript.Echo "Deleted folder '" & safsProgramDir & "'"
    Else
        WScript.Echo "Failed to delete folder '" & safsProgramDir & "', Err.Number=" & Err.Number
    End If

    WScript.Quit Err.Number 
End If

Set shell = nothing
Set fso   = nothing
