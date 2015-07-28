'************************************
'* Original From http://www.tek-tips.com/faqs.cfm?fid=5661
'* UserIsAdmin
'* This function checks to see if the current user is an administrator
'* Note: Requires WMI
'* Not tested
'************************************
Function UserIsAdmin
  'The local machine
  strComputer = "."

  'Connect  to the registry using WMI
  Set fncObjReg = GetObject("winmgmts:{impersonationLevel=impersonate}!\\" & strComputer & "\root\default:StdRegProv")

  'The key path to check access to
  fncStrKeyPath = "SYSTEM\CurrentControlSet"

  'Check the users access to delete this component of the registry, and return the result
  fncObjReg.CheckAccess HKEY_LOCAL_MACHINE, fncStrKeyPath, DELETE, UserIsAdmin
End Function

'************************************
'* Original From http://www.tek-tips.com/faqs.cfm?fid=5661
'* UserIsAdministrator
'* This function checks to see if the current user is an administrator
'* Assumes a global WScript.Shell object named 'shell'
'************************************
Function UserIsAdministrator
  On Error Resume Next

  'Try to write to the root of the registry "Local Machine" (only Administrator's can do this)
  shell.RegWrite "HKLM\SAFS_Test_Admin_Access_Right", "Access Granted"

  If (Err.Number <> 0) Then
    UserIsAdministrator = false
  Else
    shell.RegDelete "HKLM\SAFS_Test_Admin_Access_Right"
    UserIsAdministrator = true
  End If

  Err.Clear

  On Error Goto 0
End Function
