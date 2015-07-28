
Set oShell = CreateObject("WScript.Shell")
Set env   = oShell.Environment("SYSTEM")

sLink = env("SELENIUM_PLUS")
'sLink = oShell.ExpandEnvironmentStrings("%SELENIUM_PLUS%")
sPath = sLink &"\eclipse"

Set oShortCut = oShell.CreateShortcut( sLink & "\SeleniumPlus.lnk")

oShortCut.TargetPath = sPath & "\eclipse.exe"
'oShortCut.Arguments = "value,value"
'oShortCut.IconLocation = sWinSysDir & "\Shell32.dll,47"
oShortCut.WindowStyle = 1
oShortCut.Description = "SeleniumPlus"
oShortCut.WorkingDirectory = sPath
oShortCut.Save
