
'*************************************************************************************
' Delete SeleniumPlus Program Group
'* Returns 0:   when success 
'*         76:  folder does not exist 
'*         100: not found SeleniumPlus Program Group 
'*************************************************************************************
Const groupTitle = "SeleniumPlus 2014"     'title of group on Windows program menu 
Dim shell, fso
Dim mainProgramsDir, safsProgramDir
Dim year, firstSupportYear, lastSupportYear

Set shell = WScript.CreateObject("WScript.Shell")
Set fso   = WScript.CreateObject("Scripting.FileSystemObject")
firstSupportYear = 2010
lastSupportYear = 2050

'uninstall program groups
mainProgramsDir   = shell.SpecialFolders("AllUsersPrograms")
safsProgramDir    = mainProgramsDir & "\" & groupTitle

'If not exist, then try to detect it
If Not (fso.FolderExists(safsProgramDir)) Then
    For year = firstSupportYear to lastSupportYear
        safsProgramDir = mainProgramsDir & "\SeleniumPlus " & year
        'Once found, exit the loop
        If (fso.FolderExists(safsProgramDir)) Then Exit For
    Next
    
    'WScript.Echo year
    'If (year = lastSupportYear) And (Not fso.FolderExists(safsProgramDir)) Then
    If (year = lastSupportYear+1) Then
        WScript.Echo "The Selenium Group Folder can NOT be detected, the lastSupportYear '" &lastSupportYear& "' might need be increased."
        WScript.Quit 100 'code for "not found SeleniumPlus Program Group"
    End If
End If

On Error Resume Next
Err.Clear
If Not (fso.FolderExists(safsProgramDir)) Then
    WScript.Echo "The Selenium Group Folder '" & safsProgramDir & "' does NOT exist."
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