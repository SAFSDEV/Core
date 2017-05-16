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

'************************************
'* Set the windows .lnk file's property 'Run As Administrator' on.
'* So that the link will be run with Administrator priviledge.
'* Parameters:
'*   oshell         WScript.Shell object
'*   programDir     The directory holding the shortcut
'*   targetfile     The shortcut's target file
'*   title          The shortcut's name
'*   arguments      The shortcut's arguments
'************************************
Function createShortcut(oshell, programDir, targetfile, title, arguments )
    Dim pos, workingdir, oShellLink
    Dim lnkFile
    lnkFile = programDir & "\" & title & ".lnk"
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
    
    SetLinkFileRunASAdmnistrator(lnkFile)
        
    On Error GoTo 0
    Set oShellLink = Nothing
End Function

'************************************
'* Set the windows .lnk file's property 'Run As Administrator' on.
'* So that the link will be run with Administrator priviledge.
'* Parameters:
'*   lnkFile The link file to be modified to "Run As Administrator"
'************************************
Function SetLinkFileRunASAdmnistrator(lnkFile)
  Dim debug
  
  debug = false
  
  if(Not debug) then
    On Error Resume Next
  end if
  
  Dim propertyByteArray

  'set the 6th bit (0x20) of 21th (0x15) byte ON, which represents the "Run As Administrator" property
  'change the properties to "run as administrator"
  Set srcStream = CreateObject( "ADODB.Stream" )
  srcStream.Mode = 3 'read-write
  srcStream.Type = 1 'adTypeBinary
  srcStream.Open
  srcStream.LoadFromFile lnkFile
  srcStream.Position = 21 '0x15
  propertyByteArray = srcStream.Read(1)
  'propertyByteArrayis Byte(), which is really just byte strings, it is very hard to handle!!!
  if(debug) then
    WScript.Echo "propertyByteArray TypeName=" & TypeName(propertyByteArray) & " VarType="&VarType(propertyByteArray)
    WScript.Echo "Err.Number " & Err.Number
  end if
  
  'Cannot modify propertyByteArray(0) ???
  'propertyByteArray(0) = propertyByteArray(0) or 32 '0x20
  'WScript.Echo "Err.Number " & Err.Number
  
  Dim xmldoc, node
  Set xmldoc = CreateObject("Msxml2.DOMDocument")
  Set node = xmldoc.CreateElement("binary")
  node.DataType = "bin.hex"
  if(debug) then
    node.NodeTypedValue = propertyByteArray
    WScript.Echo "the 21th byte, its original value is " & node.Text
  end if
  'Set the text to 0x20, it is 0010 0000, will set the 6 bit on
  'so that node.NodeTypedValue will contain the Variant of type Byte() 
  node.Text = "20" '0x20

  srcStream.Position = 21
  'srcStream.Write propertyByteArray
  srcStream.Write node.NodeTypedValue
  'WScript.Echo "Err.Number " & Err.Number
  
  srcStream.SaveToFile lnkFile, 2 'adSaveCreateOverWrite
  if(debug) then
    WScript.Echo "srcStream.Position: " & srcStream.Position
  end if
  
  'Clean up resources
  srcStream.Close
  Set srcStream = Nothing
  Err.Clear

  On Error Goto 0
End Function