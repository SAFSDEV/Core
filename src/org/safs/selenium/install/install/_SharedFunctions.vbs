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
  'Cannot modify propertyByteArray(0) ???
  'propertyByteArray(0) = propertyByteArray(0) or 32 '0x20
  'WScript.Echo "Err.Number " & Err.Number
  if(debug) then
    WScript.Echo "propertyByteArray TypeName=" & TypeName(propertyByteArray) & " VarType="&VarType(propertyByteArray)
  end if

  Dim xmldoc, node
  Set xmldoc = CreateObject("Msxml2.DOMDocument")
  Set node = xmldoc.CreateElement("binary")
  node.DataType = "bin.hex"
  'node.Text will hold the byte array in string format (each byte is represented by an hex-decimal string)
  node.NodeTypedValue = propertyByteArray
  if(debug) then
    WScript.Echo "the 21th byte, its original value is 0X" & node.Text
  end if
  
  Dim hexPrefix, originalHex, sixBitOnHex
  hexPrefix = "&H"
  originalHex = hexPrefix & node.Text
  sixBitOnHex = hexPrefix & "20" 'it is 0010 0000, will set the 6 bit on
  'make a bit-OR between the original value and sixBitOnHex, and convert the result to an Hex string format
  'and set it to node.Text so that node.NodeTypedValue will contain the Variant of type Byte() 
  node.Text = CStr(Hex(CByte(originalHex) or CByte(sixBitOnHex)))
  propertyByteArray = node.NodeTypedValue
  if(debug) then
    WScript.Echo "the 21th byte, its new value is 0X" & node.Text
  end if

  srcStream.Position = 21
  srcStream.Write propertyByteArray
  srcStream.SaveToFile lnkFile, 2 'adSaveCreateOverWrite

  if(debug) then
    'After calling srcStream.SaveToFile, the srcStream.Position should be reset to 0
    WScript.Echo "srcStream.Position: " & srcStream.Position
    WScript.Echo "Err.Number " & Err.Number
  end if
  
  'Clean up resources
  srcStream.Close
  Set srcStream = Nothing
  Err.Clear

  On Error Goto 0
End Function

Function Test
    Dim hex1, hex2, hexPrefix
    Dim byte1, byte2
    
    hexPrefix = "&H"
    hex1 = "20"
    hex2 = "40"
    
    byte1 = CByte(hexPrefix & hex1)
    byte2 = CByte(hexPrefix & hex2)
    
    WScript.Echo "byte1=" & byte1 & " byte2=" & byte2 
    WScript.Echo "byte1 or byte2 " & (byte1 or byte2) & " Hex(byte1 or byte2)=" & CStr(Hex(byte1 or byte2))
    WScript.Echo "byte1 and byte2 " & (byte1 and byte2)
    WScript.Echo "byte1 xor byte2 " & (byte1 xor byte2)
    
End Function