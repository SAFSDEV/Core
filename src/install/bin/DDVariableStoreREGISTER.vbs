'************************************************************************************************************
'*
'* Script MUST be run from 32-bit version of Windows Scripting Host.
'* ON 64-bit systems this is located at %SYSTEMROOT%\SysWOW64\cscript.exe
'* 
'************************************************************************************************************

Dim shell
Dim env
Dim fso
Dim exec
Dim cmdline
Dim response
Dim msg_title
Dim errormsg

Dim dde_runtime
Dim system_path
Dim message
Dim timeout
Dim numerrors
Dim regsrv, winfolder

Dim arg
Dim dde_ddvariablestore
Dim cr
Dim q

cr        = chr(13)  'carriage return
q         = chr(34)  'dobule quote
numerrors = 0
msg_title = "Register DDVariableStore.DLL"

Set shell = WScript.CreateObject("WScript.Shell")
Set env   = shell.Environment("SYSTEM")
Set fso   = WScript.CreateObject("Scripting.FileSystemObject")


'Get DDE_RUNTIME FOLDER
'======================
dde_runtime = shell.CurrentDirectory
shell.LogEvent 4, "Registering DDVariableStore.DLL from: "& dde_runtime
response = shell.Popup ("Registering DDVariableStore.DLL from: "& cr & cr & dde_runtime, 10, msg_title, 65)

if response = 2 then WScript.Quit

'Set Local Filenames
'===================
dde_ddvariablestore = dde_runtime &"\DDVariableStore.dll"

Set winfolder = fso.GetSpecialFolder(0)

if fso.FolderExists(winfolder.Path &"\SysWOW64") then
    regsrv = winfolder.Path & "\SysWOW64\regsvr32"    
else
    regsrv = winfolder.Path & "\System32\regsvr32"
end if

'Register DDVARIABLESTORE
'========================
if (fso.FileExists(dde_ddvariablestore)) then
    shell.LogEvent 4, "Registering DDVariableStore.DLL..."
    shell.Popup "Registering DDVariableStore.DLL...", 3, msg_title, 64
    Set exec = shell.Exec(regsrv &" /s "& q & dde_ddvariablestore & q)

    timeout = 0
    Do while ((exec.Status = 0)AND(timeout < 100))
        WScript.Sleep 100    
        timeout = timeout + 1
    Loop

    if (timeout = 100) then
        shell.LogEvent 1, "Error registering DDVariableStore.DLL!"
        shell.Popup "Error registering DDVariableStore.DLL!", 5, msg_title, 48
        numerrors = numerrors + 1
        errormsg = "Error registering DDVariableStore.DLL"
    else
        shell.LogEvent 4, "Registered DDVariableStore."
    end if
else
    shell.LogEvent 1, "Error finding DDVariableStore.DLL!"
    shell.Popup "Error finding DDVariableStore.DLL!", 5, msg_title, 48
    numerrors = numerrors + 1
    errormsg = "Could not locate DDVariableStore.DLL"
end if


'Finished
'========
if (numerrors > 0) then
    message = "Errors occurred registering DDVariableStore.DLL!"& cr & cr & errormsg
else
    message = "DDVariableStore.DLL should now be registered."
end if

shell.LogEvent 4, message
shell.Popup message, 0, msg_title, 64


Set shell = nothing
Set fso   = nothing
Set exec  = nothing
