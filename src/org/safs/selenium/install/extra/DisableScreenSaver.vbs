
Reg_Screen = "HKEY_CURRENT_USER\Control Panel\Desktop"
RegWriteValue Reg_Screen & "\SCRNSAVE.EXE", "","REG_SZ"
RegWriteValue Reg_Screen & "\ScreenSaveActive", "0","REG_SZ"

'*****************************************************************************
'* RegWriteValue Sub
'* key, value, type
'* Type is normally "REG_SZ" but others are described in WSH docs.
'* To set the default value of a key, append \ to end of key name.
'*****************************************************************************
Sub RegWriteValue(K, V, T)
     'key, value, type
     'Type is normally "REG_SZ" but others are described in WSH docs.
     'To set the default value of a key, append \ to end of key name.
     Dim WShellObj
     Set WShellObj = Wscript.CreateObject("Wscript.Shell")
     WShellObj.RegWrite K, V, T
     Set WShellObj = Nothing
End Sub
