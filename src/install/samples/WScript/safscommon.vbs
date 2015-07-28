
'************************************************************************************************************
'*
'* Library MUST be used only in a 32-bit version of Windows Scripting Host due to use of
'* 32-bit COM objects which cannot run in 64-bit process space.
'*
'* ON 64-bit systems the 32-bit WScript is located at %SYSTEMROOT%\SysWOW64\cscript.exe
'* 
'************************************************************************************************************

Option Explicit

Dim ostaf
Dim otrd
Dim oresult
Dim omonitor
Dim safsstatus
Dim safshandle
Dim safsprocess
Dim safsvalid
Dim staftried
Dim stafdir, safsdir

CONST SAFS_DEBUG_LOG = "SAFS/TESTLOG"
CONST SAFS_DEBUG_PREFIX = "0|[DEBUG             : "

CONST ENV_STAFDIR = "STAFDIR"
CONST ENV_SAFSDIR = "SAFSDIR"

CONST PATH_STAFPROC = "\bin\STAFProc.exe"

CONST STAF_NOT_RUNNING = 21
CONST STAF_REGISTRATION_ERROR = 26
CONST STAF_ITEM_EXISTS = 49


Sub safs_debug ( message )
	IF( safsvalid ) THEN
		ostaf.sendQueueMessage SAFS_DEBUG_LOG, SAFS_DEBUG_PREFIX & safsprocess & " " & message
	END IF
End Sub

Function lengthTagSTAF (avalue)
	lengthTagSTAF = ":0:"
	IF Len(avalue) > 0 THEN
		lengthTagSTAF = ":"& Trim(CStr(Len(avalue)))&":"& avalue
	END IF
End Function

Sub waitprogram (command)
	Dim shell, prog
	Set shell = WScript.CreateObject("WScript.Shell")
    Set prog = shell.Exec(command)
    Do While (prog.Status = 0)
        WScript.Sleep(1000)
    Loop
    Set shell = Nothing
End Sub


Sub autoLaunchSTAF ()
	Dim shell, env
	Set shell = WScript.CreateObject("WScript.Shell")
	Set env   = shell.Environment("SYSTEM")
	
	stafdir = env(ENV_STAFDIR)
	IF (stafdir = "") then
		WScript.Echo "Could not deduce installation location of STAF for AutoLaunch."
	ELSE
		On Error Resume Next
		shell.Exec(stafdir & PATH_STAFPROC)
		If Err.Number <> 0 then
			message = "Error <"& CStr(Err.Number) &":"& Err.Description &"> "& message
			Wscript.Echo "Unable to AutoLaunch STAF: "& message
			EXIT SUB
		End if
		On Error Goto 0
		Dim waittime
		waittime = 0
		Do While waittime < 10
			WScript.Sleep 500
			waittime = waittime+1
		Loop			
		if (isObject(omonitor)) then omonitor.addSTAFMonitor
	END IF
	Set shell = Nothing
	Set env = Nothing
End Sub


Sub initSAFSClient (id)
    IF (NOT safsvalid) THEN
 
		if(not isObject(ostaf)) then Set ostaf = WScript.CreateObject("DDVariableStore.STAFUtilities")
		if(not isObject(otrd)) then Set otrd  = WScript.CreateObject("DDVariableStore.TestRecordData")
		if(not isObject(oresult)) then Set oresult = WScript.CreateObject("DDVariableStore.STAFResult")
		if(not isObject(omonitor)) then Set omonitor = WScript.CreateObject("DDVariableStore.SAFSMonitor")
		If (Not isObject(ostaf)) then
			status = STAF_REGISTRATION_ERROR
			WScript.Echo "COM interface to STAF could not be initialized."
			EXIT SUB
		End If
		otrd.setSTAFHelper ostaf
		safsstatus = ostaf.registerNewProcess(id)
		if ((safsstatus = STAF_NOT_RUNNING )AND(staftried <> 1)) then
			staftried = 1
			autoLaunchSTAF
			initSAFSClient id
			EXIT SUB
		End If
		If (safsstatus <> 0)  then
			safsvalid = false
			WScript.Echo "COM STAF registration failure statuscode: "& safsstatus
			destroySAFSObjects
			EXIT SUB
		End If
		
		safsvalid = true
		safsprocess = id
		safshandle = ostaf.getHandleID()
		omonitor.useProcessInfo CStr(safsprocess), CLng(safshandle)
		safs_debug "Initialized SAFS Client '"& safsprocess &"' handle: "& safshandle
	ELSE
		safsstatus = STAF_ITEM_EXISTS
		safs_debug "SAFS Client '"& safsprocess &"' handle: "& safshandle &", already initialized."
	END IF
End Sub


Sub initSAFSServices (projectdir)
	Dim stafprog, modprojectdir, modsafsdir
	Dim shell, env
	Set shell = WScript.CreateObject("WScript.Shell")
	Set env   = shell.Environment("SYSTEM")	
	safsdir = env(ENV_SAFSDIR)
	modsafsdir = Replace(safsdir, "\","/")	
	modprojectdir = Replace(projectdir, "\", "/")	
	'Start SAFSMAPS
	stafprog = "staf local service add service safsmaps library jstaf execute "& modsafsdir &"/lib/safsmaps.jar PARMS dir "& modprojectdir &"/Datapool"
	waitprogram stafprog
	'Start SAFSVARS 
	stafprog = "staf local service add service safsvars library jstaf execute "& modsafsdir &"/lib/safsvars.jar"
	waitprogram stafprog
	'Start SAFSLOGS 
	stafprog = "staf local service add service safslogs library jstaf execute "& modsafsdir &"/lib/safslogs.jar PARMS dir "& modprojectdir &"/Datapool/Logs"
	waitprogram stafprog
	'Start SAFSINPUT
	stafprog = "staf local service add service safsinput library jstaf execute "& modsafsdir &"/lib/safsinput.jar PARMS dir "& modprojectdir &"/Datapool"
	waitprogram stafprog	
	ostaf.submitSTAFVariantRequest "local", "safsmaps", "ENABLECHAIN", oresult
	ostaf.submitSTAFVariantRequest "local", "safsmaps", "ENABLERESOLVE", oresult
	Set env = Nothing
	Set shell = Nothing
End Sub


Sub initSAFSAppMap (appmap)
	Dim message
	message = "OPEN "& appmap &" FILE "& appmap
	safsstatus = ostaf.submitSTAFVariantRequest("local", "safsmaps", message, oresult)
End Sub


Function getSAFSMapItem (section, item)
	IF (section = null) OR (section = Empty) THEN section = "DEFAULTMAPSECTION"
	Dim message
	message = "GETITEM SECTION "& section &" ITEM "& item
	safsstatus = ostaf.submitSTAFVariantRequest("local", "safsmaps", message, oresult)
	getSAFSMapItem = oresult.result
End Function


Function getSAFSVariable (varname)
	getSAFSVariable = ""
	IF (varname = null) OR (varname = Empty) THEN 
		oresult.rc = 47
		oresult.result = ""
		EXIT FUNCTION
	END IF
	Dim message
	message = "GET "& varname
	safsstatus = ostaf.submitSTAFVariantRequest("local", "safsvars", message, oresult)
	getSAFSVariable = oresult.result
End Function


Sub setSAFSVariable (varname, varvalue)
	IF (varname = null) OR (varname = Empty) THEN 
		oresult.rc = 47
		oresult.result = ""
		EXIT SUB
	END IF
	Dim message
	message = "SET "& varname & " VALUE "& lengthTagSTAF(varvalue)
	safsstatus = ostaf.submitSTAFVariantRequest("local", "safsvars", message, oresult)
End Sub


Sub destroySAFSObjects ()
	Set ostaf  = Nothing
	Set oresult = Nothing
	Set otrd = Nothing
	Set omonitor = Nothing
	safsvalid = false
	safsprocess = ""
	safshandle = 0
End Sub


Sub shutdownSAFSClient ()
	IF ( safsvalid ) THEN
		safs_debug "Removing SAFS Client '"& safsprocess &"' handle: "& safshandle
		IF (staftried <> 1) then
			safsstatus = ostaf.unRegisterProcess()
		END IF
		destroySAFSObjects
	ELSE
		WScript.Echo "COM interface to STAF was not initialized for shutdown."
		safsstatus = STAF_REGISTRATION_ERROR
	END IF
End Sub