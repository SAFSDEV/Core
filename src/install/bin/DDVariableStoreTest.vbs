'************************************************************************************************************
'*
'* Script MUST be run from 32-bit version of Windows Scripting Host.
'* ON 64-bit systems this is located at %SYSTEMROOT%\SysWOW64\cscript.exe
'* 
'************************************************************************************************************

Dim ostaf
Dim oresult
Dim status
Dim handleid
Dim processid
Dim gvars
Dim gvarvalue
Dim version

Set ostaf   = WScript.CreateObject("DDVariableStore.STAFUtilities")

status = ostaf.RegisterNewProcess("STAFUtilitiesTest")
WScript.Echo "STAF Registration status:"& status

handleid = ostaf.getHandleID()
WScript.Echo "STAF HandleID:"& handleid

processid = ostaf.getProcessID()
WScript.Echo "STAF ProcessID:"& processid

version = ostaf.getStafVersion()
WScript.Echo "STAF version: "& version

Set oresult = WScript.CreateObject("DDVariableStore.STAFResult")

if oresult is Nothing then
    WScript.Echo "Could not CreateObject on STAFResult!!!"    
else
    oresult.reset
    WScript.Echo "STAFResult initialized RC:"& oresult.rc
    processid = ostaf.getProcessID()
    WScript.Echo "STAF ProcessID:"& processid
    if version < 3 then 
        status = ostaf.submitSTAFVariantRequest("local", "handle", "query all", oresult)
    else
        status = ostaf.submitSTAFVariantRequest("local", "handle", "list", oresult)
    end if
    WScript.Echo "STAF Submit status:"& status
    WScript.Echo "STAF RC:"& oresult.rc
    WScript.Echo "STAF RESULT:"& oresult.result
    
    oresult.reset
    WScript.Echo "OK to Wait MAX 5 seconds for BogusEvent"
    status = ostaf.WaitEvent("BogusEvent", 5)
    WScript.Echo "STAF Submit status:"& status
    
    oresult.reset
    status = ostaf.submitSTAFVariantRequest("local", "safsmaps", "enablechain", oresult)
    WScript.Echo "STAF RC:"& oresult.rc
    WScript.Echo "STAF RESULT:"& oresult.result
    
    oresult.reset
    status = ostaf.submitSTAFVariantRequest("local", "safsmaps", "enableresolve", oresult)
    WScript.Echo "STAF RC:"& oresult.rc
    WScript.Echo "STAF RESULT:"& oresult.result

    oresult.reset
    status = ostaf.submitSTAFVariantRequest("local", "safsmaps", "open nlsmap file c:\safs\project\datapool\nlsbridgetest_ja.map", oresult)
    WScript.Echo "STAF RC:"& oresult.rc
    WScript.Echo "STAF RESULT:"& oresult.result
    oresult.reset
    status = ostaf.submitSTAFVariantRequest("local", "safsmaps", "open nlsmain file c:\safs\project\datapool\nlsbridgetest.map", oresult)
    WScript.Echo "STAF RC:"& oresult.rc
    WScript.Echo "STAF RESULT:"& oresult.result
    oresult.reset
    status = ostaf.submitSTAFVariantRequest("local", "safsmaps", "getitem nlsmain section DEFAULTMAPSECTION item howDoIText", oresult)
    WScript.Echo "STAF RC:"& oresult.rc
    WScript.Echo "STAF RESULT:"& oresult.result    
    oresult.reset
    status = ostaf.submitSTAFVariantRequest("local", "safsvars", "get howDoIText", oresult)
    WScript.Echo "STAF RC:"& oresult.rc
    WScript.Echo "STAF RESULT:"& oresult.result

    status = ostaf.submitSTAFVariantRequest("local", "safsvars", "set text value "& oresult.result, oresult)
    WScript.Echo "STAF RC:"& oresult.rc
    WScript.Echo "STAF RESULT:"& oresult.result
end if

status = ostaf.unRegisterProcess()
WScript.Echo "STAF UnRegister status:"& status

Set ostaf  = Nothing
Set oresult = Nothing