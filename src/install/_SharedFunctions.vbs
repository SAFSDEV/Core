
'*****************************************************************************
'* This script provides shared functions for other WSH files and scripts.
'*****************************************************************************


'*****************************************************************************
'* checkWSHVersion 
'* Assumes a global WScript.Shell object named 'shell'
'*
'* return -1 if WSH is too low a version
'* return  0 if WSH is the exact version sought
'* return >0 if WSH is a higher version than sought
'*****************************************************************************
Private Function checkWSHVersion (min_version)
    Dim wsh_version, response, message
    wsh_version = WScript.Version
    response = StrComp(wsh_version, min_version, 1)
    if response = -1 then
        message = "Script Host must be V"& min_version &" or greater."& cr 
        message = message & "Current installed version is: " & wsh_version & cr & cr
        message = message & "Upgrade WSH from:"& cr & cr
        message = message & "http://msdn.microsoft.com/downloads/list/webdev.asp"& cr
        shell.Popup message, 5,"Script Host V"& min_version &" Required", 16    
    end if
    checkWSHVersion = response
End Function


'*****************************************************************************
'* getJavaVersion 
'* Assumes a global WScript.Shell object named 'shell'
'* Assumes a global CR char variable named 'cr'
'* Assumes a global QUOT char variable named 'q'
'*
'* return  a string that represent the java version, like "1.5.0_12"
'*****************************************************************************
Private Function getJavaVersion ()
    Dim exec, java_out, response, message, java_version
    Dim java_stdout_array, len_array, counter, version_array
    Dim pos_java, pos_version

    getJavaVersion = ""

    On Error Resume Next
    Set exec = shell.Exec("java -version")

    'shared until changed
    message = "occurred while trying to launch Java."& cr
    message = message & "If needed, Java V"& major_version &"."& minor_version 
    message = message & " or later can be installed from:"& cr & cr
    message = message & "http://java.sun.com/j2se/"& cr & cr
    message = message & "Run SetupSAFS again when ready."

    If Err.Number <> 0 then
        message = "Error <"& CStr(Err.Number) &":"& Err.Description &"> "& message
        shell.Popup message,0, "SAFS Installation Aborted",16
        Exit Function
    End if

    On Error Goto 0

    'extract Java stdout and stderr

    java_out = ""
    Do While Exec.Status = 0
        java_out = java_out & readall(exec)
        WScript.Sleep(300)
    Loop

    ' locate java version line in stdout
    ' ----------------------------------
    java_stdout_array = Split(java_out, " ", -1, 1)
    len_array = UBound(java_stdout_array, 1)
    counter = 0

    do while counter <= len_array
        response = strcomp("java", java_stdout_array(counter), 1)
        if response = 0 then pos_java = counter
        response = strcomp("version", java_stdout_array(counter), 1)
        if response = 0 then pos_version = counter
       counter = counter +1
       if ((pos_java <> 0) AND (pos_version <> 0)) then Exit Do
    loop

    ' extract the version info from the line
    ' --------------------------------------
    if ((pos_java + 1) = pos_version) then
        version_array = Split(java_stdout_array(pos_version +1), q, -1, 1)
        getJavaVersion = version_array(1)
    end if

End Function

'*****************************************************************************
'* checkJavaVersion 
'* Assumes a global WScript.Shell object named 'shell'
'* Assumes a global CR char variable named 'cr'
'* Assumes a global QUOT char variable named 'q'
'*
'* return -2 if checking Java generates an error
'* return -1 if Java is too low a version
'* return  0 if Java is the exact version sought
'* return >0 if Java is a higher version than sought
'*****************************************************************************
Private Function checkJavaVersion (major_version, minor_version)
    Dim exec, java_out, response, message, java_version
    Dim java_stdout_array, len_array, counter, version_array
    Dim pos_java, pos_version

    checkJavaVersion = -2
	java_version = getJavaVersion()

    ' extract the version info from the line
    ' --------------------------------------
    if (java_version <> "") then
        'split into major and minor versions
        version_array = Split(java_version, ".", -1, 1)

        response = strcomp(version_array(0), major_version, 1)

        if response = 0 then
            response = strcomp(version_array(1), minor_version, 1)
        end if    

				checkJavaVersion = response
	
        if response < 0 then
            message = "Java version "& java_version &" rejection "& message
            shell.Popup message,0, "Java Upgrade Required",16
        end if
    else
        message = "Java version extraction error "& message
        shell.Popup message,0, "SAFS Installation Aborted",16
        'exits with -2
    end if

End Function


'*****************************************************************************
'* appendEnvironment
'* Assumes a global WScript.Shell object named 'shell'
'* Assumes a global shell.Environment object named 'env'
'* Check if the given Environment variable contains the provided substring.  
'* Append the substring to the current value of the variable if it does not. 
'*****************************************************************************
Private Sub appendEnvironment(varname, newvalue)

  Dim varvalue
  Dim lcvarvalue
  Dim lcnewvalue
  
  varvalue   = env(varname)
  lcvarvalue = lcase(varvalue)
  lcnewvalue = lcase(newvalue)

  'shell.Popup "Append "& varname &" evaluating: "& Chr(13) & varvalue & Chr(13) & Chr(13) & newvalue, 3, varname, 64
  
  if InStr(1, lcvarvalue, lcnewvalue, 1) then exit sub
  
  if (Right(varvalue,1)<>";") then varvalue = varvalue & ";"
  varvalue = varvalue & newvalue
  'WScript.Echo "Appending Environment: "& varname &"=%"& varname &"%;"& newvalue
  'shell.Popup "Appending Environment: "& varname &"=%"& varname &"%;"& newvalue, 3, "System Environment", 64
  env(varname) = varvalue
  
End Sub


'*********************************************************************************
'* CleanEnvironment
'* Assumes a global WScript.Shell object named 'shell'
'* Assumes a global shell.Environment object named 'env'
'* 
'* Traverse through the varname environment variable and remove all instances of 
'* the strings stored in varArray.
'*
'* Author: Bob Lawler
'* Original Release: 12.16.2005
'* 
'*********************************************************************************
Private Sub CleanEnvironment(varname, varArray)

    Dim EnvString, AnEnvString
    Dim EnvArray, AnEnvArray()
    Dim aresponse
    
    EnvString = ""
    AnEnvString = ""
    EnvString = env(varname)

    'validate input parameters
    If EnvString = "" Then 
	'MsgBox "Environment variable '" & varname & "' not found," & _
	'       " so it could not be cleaned.", 48, "Warning!"
	Exit Sub
    End If
    If Not IsArray(varArray) Then
	'MsgBox "No input strings provided.  Environment variable '" & _ 
	'        varname & "' was not cleaned.", 48, "Warning!"
	Exit Sub
    End If

    'initialize
    Dim i, j, a
    Dim Match
    
    'MsgBox EnvString & chr(13)
    
    EnvArray = Split(EnvString, ";", -1, 1)
    
    'MsgBox "EnvArray UBound "& UBound(EnvArray) & Chr(13) & Join(EnvArray,";") & Chr(13)
    
    ReDim AnEnvArray(UBound(EnvArray))
    For a = 0 to UBound(EnvArray)
        AnEnvArray(a) = EnvArray(a)
    Next

    'MsgBox "Original "& Chr(13) & Join(AnEnvArray, ";") & Chr(13)

    'search for items in varArray to be removed from varname
    i = 0
    Do While i <= UBound(AnEnvArray)
        
        if anEnvArray(i) = "." then  'skip everything            
            'do nothing
            i = i + 1            
        Else
	    'loop through all items in varArray to compare
	    Match = 0
	    For j = 0 to UBound(varArray)
                'MsgBox "Comparing "& Chr(13) & AnEnvArray(i) & Chr(13) &" with "& Chr(13) & varArray(j) & chr(13)
                if Len(varArray(j)) > 0 then
		    If InStr(1, AnEnvArray(i), varArray(j), 1) Then
                        'MsgBox "Matched on "& Chr(13) & AnEnvArray(i) & Chr(13) & varArray(j) & chr(13)
		        Match = 1
		        Exit For
		    end if
		End If
	    Next

	    If Match Then
		'match found, so remove entry from array
		AnEnvArray(i) = ""
		For j = i to UBound(AnEnvArray) - 1
		    AnEnvArray(j) = AnEnvArray(j + 1)
		Next
		
		'reduce size of array by 1
		ReDim Preserve AnEnvArray(UBound(AnEnvArray) - 1)

		'MsgBox "Edited "& Chr(13) & Join(AnEnvArray, ";") & chr(13)
	    	
	    	'don't increment i so we can check for another match of same substring
	    Else	    
		i = i + 1		
	    End If	    
	End If	
    Loop

    AnEnvString = Join(AnEnvArray, ";")
    'MsgBox EnvString & chr(13) & UBound(EnvArray) & chr(13) & chr(13) & _ 
    '       AnEnvString & chr(13) & UBound(AnEnvArray)
    env(varname) = AnEnvString

End Sub

'*****************************************************************************
'* Control a Windows Service by Name  (action = start, stop, etc)
'*****************************************************************************
Function controlService(action, service)
    
    Dim cmdline, scexe, stdout
    
    cmdline = "sc.exe "& action &" "& q & service & q
    Set scexe = shell.Exec(cmdline)

    stdout = ""
    Do While scexe.Status = 0
        stdout = stdout & readall(scexe)
        WScript.Sleep(1000)
    Loop
    
    controlService = stdout

End function


'*****************************************************************************
'* ReadAll  Streams
'* Assumes a global new line mark named 'newline', newline = chr(13) & chr(10)
'*****************************************************************************
Function readall(exec)

    readall = ""
    if not exec.StdOut.AtEndOfStream then
        readall = "STDOUT:" & newline & exec.StdOut.ReadAll        
    end if

    if not exec.StdErr.AtEndOfStream then
        readall = readall &"STDERR:" & newline & exec.StdErr.ReadAll        
    end if

End function


'*****************************************************************************
'* Delete all subfolders and files within the provided folder
'*****************************************************************************
Function deleteFolderContents(folder)
    
    Dim fc
    if Len(folder.Name) < 4 then exit function
    Set fc = folder.SubFolders
    For Each f1 in fc
      f1.Delete 
    Next
    Set fc = folder.Files
    For Each f1 in fc
      f1.Delete 
    Next

End function


'*****************************************************************************
'* Delete a fullpath file.  Return 0 on success. -1 on failure.
'*****************************************************************************
Function deleteFile(fullpath)
    
    if (fso.FileExists(fullpath)) then 
        On Error Resume Next
    	fso.DeleteFile fullpath, True
	If Err.Number = 0 then    	
	    deleteFile = 0
	else
	    deleteFile = -1
	end if
    else
        deleteFile = -1
    end if

End Function


'******************************************************************************
'* Copy all files aand subfolders from the folder to the destination
'******************************************************************************
Function copyFolderContents(folder, destination)
    
    Dim fc
    if Len(folder.Name) < 4 then exit function
    if Len(destination.Name) < 4 then exit function
    folder.Copy (destination)

End function


'*****************************************************************************
'* ScreenSaverEnabled Function
'* TRUE (1) If Screen Saver is enabled
'*****************************************************************************
Function ScreenSaverEnabled()
        Dim CurrentValue
	Reg_Screen = "HKEY_CURRENT_USER\Control Panel\Desktop\ScreenSaveActive"
	ScreenSaverEnabled = FALSE
	if RegReadValue (Reg_Screen, CurrentValue) = TRUE then
	   If CurrentValue = "1" then
	       ScreenSaverEnabled = TRUE
	   end if
	end if
end Function


'*****************************************************************************
'* DefaultScreenSaver Sub
'* Sets the current screen saver to 'Default Screen Saver'.
'*****************************************************************************
Sub DefaultScreenSaver()
	Reg_Screen = "HKEY_CURRENT_USER\Control Panel\Desktop"
        RegWriteValue Reg_Screen & "\SCRNSAVE.EXE", "%systemroot%\System32\scrnsave.scr","REG_EXPAND_SZ"
	RegWriteValue Reg_Screen & "\ScreenSaveActive", "1","REG_SZ"
end Sub


'*****************************************************************************
'* DisableScreenSaver Sub
'* Disables screen saver settings
'*****************************************************************************
Sub DisableScreenSaver()
	Reg_Screen = "HKEY_CURRENT_USER\Control Panel\Desktop"
        RegWriteValue Reg_Screen & "\SCRNSAVE.EXE", "","REG_SZ"
	RegWriteValue Reg_Screen & "\ScreenSaveActive", "0","REG_SZ"
end Sub


'*****************************************************************************
'* RegReadValue Function
'* Returns FALSE if read fails, TRUE if read succeeds.
'* If success, Result will be set to the value read.
'* If failed, Result will not be changed.
'* To read the "default" value of a key, append \ to end of key name;
'* if a default value is not defined, function will return false.
'*****************************************************************************
Function RegReadValue(RKey, ByRef Result)
	'Returns FALSE if read fails, TRUE if read succeeds.
	'If success, Result will be set to the value read.
	'If failed, Result will not be changed.
	'To read the "default" value of a key, append \ to end of key name;
	'if a default value is not defined, function will return false.
	Dim WShellObj, X
	RegReadValue = False
	Set WShellObj = Wscript.CreateObject("Wscript.Shell")
	On Error Resume Next
	X = WShellObj.RegRead(RKey)
	if Err.Number = 0 then
		RegReadValue = True
		Result = X
	end if
	On Error Goto 0
	Set WShellObj = Nothing
End Function


'*****************************************************************************
'* RegExistValue Function
'* Same as RegReadValue, but doesn't return the value it finds; just true or false.
'*****************************************************************************
Function RegExistValue(Key)
	'Same as RegReadValue, but doesn't return the value it finds; just true or false.
	Dim scratch
	RegExistValue = RegReadValue(Key, scratch)
End Function


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


'*****************************************************************************
'* RegDeleteValue Sub
'* Delete a registry key or key value
'* If parameter ends with a backslash character (\) this sub deletes the key 
'* instead of the value.
'*****************************************************************************
Sub RegDeleteValue(S)
     'If S ends with a backslash character (\), this sub deletes the key instead of the value.
     if not RegExistValue(S) then Exit Sub
     Dim WShellObj
     Set WShellObj = Wscript.CreateObject("Wscript.Shell")
     WShellObj.RegDelete S
     Set WShellObj = Nothing
End Sub


'*****************************************************************************
'* Return an array of partial JAR paths and names to be used in cleaning or 
'* building CLASSPATH entries for SAFS tools.
'*****************************************************************************
Function getSAFSClasspathArray()

    Dim SAFSClassPathArray(24)
    SAFSClassPathArray(0)="\lib\jaccess.jar"
    SAFSClassPathArray(1)="\lib\jakarta-regexp-1.3.jar"
    SAFSClassPathArray(2)="\lib\JRex.jar"
    SAFSClassPathArray(3)="\lib\jna.zip"
    SAFSClassPathArray(4)="\lib\win32-x86.zip"
    SAFSClassPathArray(5)="\lib\jai_core.jar"
    SAFSClassPathArray(6)="\lib\jai_codec.jar"
    SAFSClassPathArray(7)="\lib\safsjvmagent.jar"
    SAFSClassPathArray(8)="\lib\safscust.jar"
    SAFSClassPathArray(9)="\lib\safsjrex.jar"
    SAFSClassPathArray(10)="\lib\safs.jar"
    SAFSClassPathArray(11)="\lib\safsdebug.jar"
    SAFSClassPathArray(12)="\lib"
    SAFSClassPathArray(13)="\lib\jai_imageio.jar"
    SAFSClassPathArray(14)="\lib\clibwrapper_jiio.jar" 
	SAFSClassPathArray(15)="\lib\safsselenium.jar"
    SAFSClassPathArray(16)="\lib\selenium-server-standalone-2.37.0.jar"
    SAFSClassPathArray(17)="\lib\dom4j-2.0.0-ALPHA-2.jar"
    SAFSClassPathArray(18)="\lib\jaxen-1.1.1.jar"
    SAFSClassPathArray(19)="\lib\nekohtml.jar"  

    SAFSClassPathArray(20)="\lib\robotium-remotecontrol.jar"  
    SAFSClassPathArray(21)="\lib\safsdroid.jar"
	SAFSClassPathArray(22)="\lib\ddmlib.jar"
	SAFSClassPathArray(23)="\lib\juniversalchardet-1.0.3.jar"
    
    getSAFSClasspathArray = SAFSClassPathArray
End Function


'*****************************************************************************
'* Return an array of filenames matching provided root name (no wildcards).
'* A match means the filename starts with the root name.
'* Returns the array. UBOUND of array holds the number of items in the array.
'* Array items start at index 0 to UBOUND -1 (typical of VBS).
'* (CANAGL) Added 12.17.2008
'*****************************************************************************
Function getVersionedFileNameArray( folder, rootname )

    Dim cfiles, ofolder, f1, n, i
    Dim fileArray
    if (fso.FolderExists(folder)) then
    	Set ofolder = fso.GetFolder(folder)
    	Set cfiles  = ofolder.Files
        'shell.Popup "Searching "& cfiles.Count &" files in folder "& ofolder.name &" for file "& rootname, 0, "Debug: Matching Files", 64
        ReDim fileArray(0)
    	For Each f1 in cfiles
    	    'compare filenames with rootname
    	    n = f1.name   	    	        	    
    	    'expect the filename to start with the rootname
    	    if InStr(1, n, rootname, 1) > 0 then
	       'shell.Popup "FileArray Matched File "& n, 0, "Debug: Matched File", 64
    	       i = UBound(fileArray)
    	       ReDim Preserve fileArray(i+1)
	       'shell.Popup "FileArray UBound "& i, 0, "Debug: Matched File", 64
    	       fileArray(i) = n
    	    end if    	    
    	Next
    end if   
    getVersionedFileNameArray = fileArray

End Function


'*****************************************************************************
'* Backup an environment variable if no previous backup exits.
'*****************************************************************************
Sub backupEnv(source, target)
    Dim original, backup, stafdir

    source = UCase(source)
    target = UCase(target)

    original = ""
    backup   = ""
    On Error Resume Next
    original = env(source)
    backup   = env(target)
    if Len(backup) = 0 then env(target) = original
    On Error Goto 0
End Sub


'*****************************************************************************
'* Return the directory that corresponds to the 32-bit "System32" directory.
'* On 32-bit systems this is simply %SYSTEMROOT%\System32, but on 64-bit 
'* systems this is %SYSTEMROOT%\SysWOW64.
'* Assumes a global Scripting.FileSystemObject named 'fso'
'* Returns the appropriate path with no trailing backslash.
'*****************************************************************************
Function getSystem32Dir()
    
    'check if 64-bit Win OS
    '======================
    'windir6432 = "\SysWOW64\"
    'windiros32 = "\System32\"

    Dim winfolder

    Set winfolder = fso.GetSpecialFolder(0)

    if fso.FolderExists(winfolder.Path &"\SysWOW64") then
        getSystem32Dir = winfolder.Path & "\SysWOW64"    
    else
        getSystem32Dir = winfolder.Path & "\System32"
    end if

End Function


'*****************************************************************************
'* Copy a source file to the 32-bit System location.
'* Uses getSystem32Dir() to determine \System32 or \SysWOW64 directories.
'* Assumes a global Scripting.FileSystemObject named 'fso'
'* Returns 0 on success; -1 on failure
'*****************************************************************************
Function installSystemFile( source )
    Dim systempath
    installSystemFile = -1
    if (fso.FileExists(source)) then
        systempath = getSystem32Dir()
        fso.CopyFile source, systempath &"\"
        installSystemFile = 0
    end if
End Function


'*****************************************************************************
'* register a 32-bit automation server file with 32-bit regsvr
'* Uses getSystem32Dir() to determine \System32 or \SysWOW64 directories.
'* Assumes a global WScript.Shell object named 'shell'
'* Assumes a global Scripting.FileSystemObject named 'fso'
'* Assumes a global quote mark named 'q'
'* Returns 0 on success; -1 on timeout failure\timeout
'*****************************************************************************
Function registerServerFile( source )
    Dim exec, timeout, regsvr
    registerServerFile = -1
    regsvr = getSystem32Dir() &"\regsvr32.exe"
    if (fso.FileExists(source)) then
	Set exec = shell.Exec(regsvr &" /s "& q & source & q)

	timeout = 0
	Do while ((exec.Status = 0)AND(timeout < 100))
	    WScript.Sleep 100    
	    timeout = timeout + 1
	Loop

	if (timeout < 100) then
            registerServerFile = 0
        end if
    end if
End Function

'*****************************************************************************
'* unregister an automation server file with regsvr
'* Assumes a global WScript.Shell object named 'shell'
'* Assumes a global Scripting.FileSystemObject named 'fso'
'* Assumes a global quote mark named 'q'
'* Returns 0 on success; -1 on timeout failure\timeout
'*****************************************************************************
Function unregisterServerFile( source )
    Dim exec, timeout, regsvr
    unregisterServerFile = -1
    regsvr = getSystem32Dir() &"\regsvr32.exe"
    if (fso.FileExists(source)) then
	Set exec = shell.Exec(regsvr &" /s /u "& q & source & q)

	timeout = 0
	Do while ((exec.Status = 0)AND(timeout < 100))
	    WScript.Sleep 100    
	    timeout = timeout + 1
	Loop

	if (timeout < 100) then
            unregisterServerFile = 0
        end if
    end if
End Function


'*****************************************************************************
'* Get the current installed STAF's version
'* Assumes a global WScript.Shell object named 'shell'
'* Assumes a global new line mark named 'newline', newline = chr(13) & chr(10)
'*****************************************************************************
Function getSTAFVersion(stafprocexe,stafexe)
    Dim stafProcExec, requestExec, waittime
    Dim getVersionRequest2, getVersionRequest3, stopStafRequest
    Dim requestout, requestout_array, counter, position_rc

    getVersionRequest2 = stafexe & " LOCAL VAR GLOBAL GET STAF/Version"
    getVersionRequest3 = stafexe & " LOCAL VAR GET SYSTEM VAR STAF/Version"
    stopStafRequest = stafexe & " LOCAL SHUTDOWN SHUTDOWN"
    
    getSTAFVersion = "0.0.0"

	'Try to launch the STAFProc
	On Error Resume Next
	Set stafProcExec = shell.Exec(stafprocexe)
	If Err.Number <> 0 then
        message = "Error <"& CStr(Err.Number) &":"& Err.Description &"> "& message
        shell.Popup message,0, "Can not start STAFProc, Just consider STAF is not installed.",0
        Exit Function
    End if
    On Error Goto 0
    
    'wait for stafproc being launched
    'how much time should we wait????
    waittime=0
	Do While waittime<10
		WScript.Sleep 200
		waittime = waittime+1
	Loop
		
	'Try to execute request to get staf's version
	On Error Resume Next
	Set requestExec = shell.Exec(getVersionRequest2)
	If Err.Number <> 0 then
        message = "Error <"& CStr(Err.Number) &":"& Err.Description &"> "& message
        shell.Popup message,0, "Can not run staf request, Just consider STAF is not installed.",0
        Exit Function
    End if
    On Error Goto 0
		
	'extract staf var request stdout and stderr
	'if "RC:" exists in stdout, which means request fail
	requestout = ""
    Do While requestExec.Status = 0
        requestout = requestout & readall(requestExec)
        WScript.Sleep(300)
    Loop
	'WScript.Echo requestout
		
	position_rc = -1
    requestout_array = Split(requestout, " ", -1, 1)
    len_array = UBound(requestout_array, 1)
    counter = 0
    
    Do While counter <= len_array
		response = strcomp("RC:", requestout_array(counter), 1)
     	If response = 0 Then
     		position_rc = counter
     		Exit Do
     	End If
     	counter = counter +1
    Loop
		
	'If request of form in STAF 2 fails, try request of staf version 3.
	If (position_rc<>-1) Then
		On Error Resume Next
		Set requestExec = shell.Exec(getVersionRequest3)
		If Err.Number <> 0 then
       	 	message = "Error <"& CStr(Err.Number) &":"& Err.Description &"> "& message
        	shell.Popup message,0, "Can not run staf request, Just consider STAF is not installed.",0
        	Exit Function
    	End if
    	On Error Goto 0
		
		'extract staf var request stdout and stderr
		'if "RC:" exists in stdout, which means request fail
		requestout = ""
    	Do While requestExec.Status = 0
       		requestout = requestout & readall(requestExec)
        	WScript.Sleep(300)
    	Loop
		'WScript.Echo requestout
		
		position_rc = -1
    	requestout_array = Split(requestout, " ", -1, 1)
    	len_array = UBound(requestout_array, 1)
    	counter = 0

    	Do While counter <= len_array
			response = strcomp("RC:", requestout_array(counter), 1)
     		If response = 0 Then
     			position_rc = counter
     			Exit Do
     		End If
     		counter = counter +1
    	Loop
	End If
		
	'if there is no "RC:" in stdout, which means request success
	'try to get the version string
	If (position_rc=-1) Then
    	requestout_array = Split(requestout, newline , -1, 1)
    	len_array = UBound(requestout_array, 1)
    	counter = 0

    	Do While counter <= len_array
    		'WScript.Echo "line:" & requestout_array(counter)
    		'Response
    		'--------
    		'2.6.11
			response = strcomp("Response", requestout_array(counter), 1)
     		If response = 0 Then
     			getSTAFVersion = requestout_array(counter+2)
     			Exit Do
     		End If
     		counter = counter +1
    	Loop
	End If
		
	'Finally stop STAFProc by sentint request "staf local shutdown shutdown"
	On Error Resume Next
	Set requestExec = shell.Exec(stopStafRequest)
	If Err.Number <> 0 then
        message = "Error <"& CStr(Err.Number) &":"& Err.Description &"> "& message
        shell.Popup message,0, "STAF has been started, you need to stop it mannually.",0
    End if
    On Error Goto 0
		
    Do While requestexec.Status = 0
        WScript.Sleep(300)
    Loop
    	
End Function

'*****************************************************************************
'* get the major version
'*****************************************************************************
Private Function getMajorVersion(currentversion)
    Dim version_array

    version_array = Split(currentversion, ".", -1, 1)
	getMajorVersion = version_array(0)
End Function

'*****************************************************************************
'* get the minor version
'*****************************************************************************
Private Function getMinorVersion(currentversion)
    Dim version_array, len_array

    version_array = Split(currentversion, ".", -1, 1)
    len_array = UBound(version_array, 1)
    If(len_array>1) Then
		getMinorVersion = version_array(1)
	Else
		getMinorVersion = version_array(0)
	End If
End Function

'*****************************************************************************
'* Show debug message in a pompt dialog
'*****************************************************************************
Function debug(message)
	If(toDebug) Then
		WScript.Echo message
	End If
End Function

'*****************************************************************************
'* Return SAFS's directory
'* Assumes a global shell.Environment object named 'env'
'* Assumes a global Scripting.FileSystemObject object named 'fso'
'* Assumes a global safsenv that is the Environment Variable for SAFS home directory
'* Returns a blank string on failure(No SAFS); otherwise returns SAFS directory on success
'*****************************************************************************
Private Function getSAFSDir( )
	  Dim safsfolder
	  safsfolder = env(safsenv)
		If (Len(safsfolder)> 0) Then 
  			'check for a safs core file: <SAFSDIR>\lib\safs.jar
  			'we think SAFS exists if the core file found
  			If fso.FileExists(safsfolder & "\lib\safs.jar") Then 
   					getSAFSDir = safsfolder
  			Else
   					getSAFSDir = ""
  			End If
  	Else
  			getSAFSDir = ""  
  	End If   
End Function

'************************************************************************************************
'* SAFS setup Wizard for installation, return 1 to install SAFS; 0, not install safs 
'* argsafsDir, target safs directory user inputs
'*             argsafsDir is passed in as argument in command line option '-safs' 
'* 
'************************************************************************************************
Function safsSetupWizard( argsafsDir )
    Dim msg, existsafs 
    Dim returncode
  
    existsafs = getSAFSDir() 

    If (Len(existsafs)>0) Then 
   	 	 
    	 	 'Unload the existing SAFS if 
    	 	 'existing safs dir, is different from the safs dir passed in as argument  "-safs"
    	 	 If ((Len(argsafsDir) > 0) And (StrComp(LCase(existsafs), LCase(argsafsDir)) <> 0)) Then
    	   		 msg =       "The target SAFS directory  : " & argsafsDir & "." & cr
    	   		 msg = msg & "The existing SAFS directory: " & existsafs & cr & cr
    	   		 msg = msg & "You have to uninstall the existing SAFS before install new one to " & argsafsDir & "?" & cr & cr
    	   		 msg = msg & "YES to proceed; No to cancel uninstall and skip SAFS install."
    	   		 returncode = shell.Popup(msg, 0, "Target directory different from existing SAFS", 36)
    	   		 If ((returncode = 2) Or (returncode = 7)) Then
                 safsSetupWizard = 0
                 Exit Function 
    	 	 		 End If
    	 	 		 'uninstall the existing SAFS
    	 	 		 returncode = UnInstallSAFS()
    	 	 		 If ((returncode = 0) Or (returncode = -1)) Then
		 	 	    	   msg = "Uninstall for the existing SAFS is NOT executed."& cr & cr
    	   		     msg = msg & "The new version of SAFS Framework will NOT be installed." & cr
    	           shell.Popup msg, 0, "SAFS Setup", 64 
    	 	 		     safsSetupWizard = 0
    	 	 		     Exit Function 
    	 	 		 End If	
    	 	 		 safsSetupWizard = 1  'install the newer one in argsafsDir
    	 	 		 Exit Function 
    	 	 Else
 	 	    	   msg = "SAFS already exists in "& existsafs & "." & cr
    	   		 msg = msg & "The existing files will be overwriten if continue to install." & cr
    	   		 msg = msg & "Continue to install SAFS for update?"& cr & cr
    	   	   msg = msg &"Click YES to proceed; NO to skip SAFS install."
    	       returncode = shell.Popup(msg, 0, "SAFS Setup", 32+4) 
    	       If ((returncode = 2) Or (returncode = 7)) Then
                 safsSetupWizard = 0
                 Exit Function     	 	     
    	 	     End If

					   safsSetupWizard = 1  'install safs to overwrite the existing safs dir
					   Exit Function  	 	 		   
    	 	 End If 
  	Else
  			 msg = "No SAFS found. SAFS will be installed in "& argsafsDir & "." & cr & cr
    	   msg = msg & "Continue to install SAFS?"& cr & cr
    	   msg = msg &"Click YES to proceed; NO to skip SAFS Install."
    	   returncode = shell.Popup(msg, 0, "SAFS Setup", 36) 
    	   If ((returncode = 2) Or (returncode = 7)) Then
    	 	     safsSetupWizard = 0
             Exit Function  
    	 	 End If
  			 safsSetupWizard = 1  'No SAFS exists, return 1 to install safs
  	End If   	 

End Function

'********************************************************************************************
'* create a shortcut for a Windows program group at programDir.
'* Called by createSAFSProgramGroup.
'* appendEnvironment
'* Assumes a global WScript.Shell object named 'shell'
'*
'* programDir: the directory of target program group that already exists
'* targetfile: the target file that is linked to the shortcut intended to be created  
'* title:      the title for the shortcut on Windows program menu
'*********************************************************************************************
Private Sub createShortcut( programDir, targetfile, title )
		Dim pos, workingdir
		Dim lnkFile
		lnkFile = programDir & "\" & title & ".lnk"
		
		'get the working dir of targetfile
		pos = InStrRev(targetfile,"\")
		If (pos > 0) Then
			  workingdir = Left(targetfile, pos-1)
		End If
			  
		On Error Resume Next
    Set oShellLink = shell.CreateShortcut(lnkFile)
    oShellLink.TargetPath = targetfile
    oShellLink.WindowStyle = 1
    oShellLink.WorkingDirectory = workingdir
    oShellLink.Save
    On Error GoTo 0
End Sub

'*******************************************************************************
'* Create SAFS program group and corresponding shortcuts for SAFS installation
'* appendEnvironment
'* Assumes a global WScript.Shell object named 'shell'
'* Assumes a global Scripting.FileSystemObject object named 'fso'
'* 
'* Six shortcuts shall be created and linked to SAFS programs (Scripts, vbs, bat, html)
'*     "Start test log"          ------> <SAFSDIR>\bin\SAFSTESTLOGStartup.bat 
'*     "Shutdown test log"       ------> <SAFSDIR>\bin\SAFSTESTLOGShutdown.bat
'*     "STAF ProcessContainer"   ------> <SAFSDIR>\Project\runSTAFProcessContainer.bat   put it here since user needs to change STAFProcessContainer.ini 
'*     "SAFSVersion Hot Swap"    ------> <SAFSDIR>\bin\SAFSVersionHotswap.vbs
'*     "Uninstall SAFS"          ------> <SAFSDIR>\UninstallSAFS.wsf
'*     "SAFS Keywords"           ------> <SAFSDIR>\doc\RRAFSReference.htm
'* safsHome, existing safs directory 
'*******************************************************************************
Const safsGroupTitle = "SAFS 2012.08"     'SAFS title of SAFS group on Windows program menu 
Const docGroupTitle  = "Documentation"    'sub-group for SAFS documentation
Sub createSAFSProgramGroup( safsHome )
    Dim file
    Dim mainProgramsDir, safsProgramDir, safsDocProgramDir 
    mainProgramsDir   = shell.SpecialFolders("Programs")
    safsProgramDir    = mainProgramsDir & "\" & safsGroupTitle	
	safsDocProgramDir = safsProgramDir & "\" & docGroupTitle	
	  
	'create SAFS program group and sub groups under it
    On Error Resume Next  
	fso.CreateFolder(safsProgramDir)
    fso.CreateFolder(safsDocProgramDir) 
    	
    'create shortcuts in SAFS program group     
    file = safsHome & "\bin\SAFSTESTLOGStartup.bat"
    createShortcut safsProgramDir, file, "Start test log"
    
    file = safsHome & "\bin\SAFSTESTLOGShutdown.bat"
    createShortcut safsProgramDir, file, "Shutdown test log"
    
    'STAF ProcessContainer 
    file = safsHome & "\Project\runSTAFProcessContainer.bat"
    createShortcut safsProgramDir, file, "STAF ProcessContainer"
    
    'SAFSVersionHotswap 
    file = safsHome & "\bin\SAFSVersionHotswap.vbs"
    createShortcut safsProgramDir, file, "SAFSVersion Hot Swap"
    
    file = safsHome & "\UninstallSAFS.wsf"
    createShortcut safsProgramDir, file, "Uninstall SAFS"
    
    'create shortcuts in SAFS Documentation group 
    file = safsHome & "\doc\RRAFSReference.htm"
    createShortcut safsDocProgramDir, file, "SAFS Keywords"
    
    On Error Goto 0
End Sub 

'*************************************************************************************
' Uninstall SAFS
'* Assumes a global WScript.Shell object named 'shell'
'* Assumes a global shell.Environment object named 'env'
'* Assumes a global Scripting.FileSystemObject object named 'fso'
'* Assumes a global safsenv that is the Environment Variable for SAFS home directory
'* Returns 0: Uninstall not executed 
'*         -1: No SAFS detected
'*          1: Uninstall finished
'*************************************************************************************
Function UnInstallSAFS()
    Dim msg, existsafs 
    Dim returncode
    Dim DDVariableDLL
    
    existsafs = getSAFSDir() 
    If (Len(existsafs)>0) Then 
           msg = "Are you sure to uninstall SAFS in "& existsafs & "?" & cr
    	   msg = msg & "Close any SAFS program before click Yes."& cr 
    	   msg = msg & "**Dangerous** All DATA in "& existsafs &" will be deleted!!!"& cr & cr
    	   msg = msg &"Click YES to proceed; NO to cancel."
    	   returncode = shell.Popup(msg, 0, "Uninstall SAFS", 36) 
    	   If ((returncode = 2) Or (returncode = 7)) Then
    	   	   UnInstallSAFS = 0
    	 	     Exit Function
    	 	 End If
    Else
         shell.Popup "SAFS Not Detected !", 0, "Uninstall SAFS", 64
         UnInstallSAFS = -1
    	 	 Exit Function
	  End If	 
    
    'start to uninstall 
    mainProgramsDir   = shell.SpecialFolders("Programs")
    safsProgramDir    = mainProgramsDir & "\" & safsGroupTitle	
	safsDocProgramDir = safsProgramDir & "\" & docGroupTitle	

    On Error Resume Next  
    '1) remove SAFS from CLASSPATH/PATH and remove enviorment variable SAFSDIR
		'Old SAFS Jars
		'-------------
		Dim SAFSClassPathArray
		SAFSClassPathArray = getSAFSClasspathArray()
		'first clean the CLASSPATH of all old SAFS jars
		CleanEnvironment "CLASSPATH", SAFSClassPathArray
		
		Dim SAFSPathArray(0)
		SAFSPathArray(0) = existsafs&"\bin"
		CleanEnvironment "PATH", SAFSPathArray
		
		'remove SAFSDIR from system enviroment varibles 
		env.Remove(safsenv)
		
		'remove TESSDATA_PREFIX and GOCRDATA_DIR from system enviroment varibles
		env.Remove(tess_ocr_prefix)
		env.Remove(g_ocr_data_dir)

		'remove TCAFS variables from system enviroment
		env.Remove(tcafs_home)
		
		env.Remove(droidenv)
		
	'=============================================
    'unregister DDVariableStore.dll before delete it
    DDVariableDLL = existsafs & "\bin\DDVariableStore.dll"
    returncode = unregisterServerFile(DDVariableDLL)
    if returncode = -1 then
        msg = "Fail to unregister "& DDVariableDLL & "!" & cr
        msg = "Files in SAFS Floder may not be deleted automatically!" & cr & cr
        shell.Popup msg , 0, "Uninstall SAFS", 64
    end if

    '2) delete SAFS program group that appears on Window menu
    fso.DeleteFolder(safsDocProgramDir)
    fso.DeleteFolder(safsProgramDir)

    '3) delete SAFS files and folder
    Set safsFolder = fso.GetFolder(existsafs)
    deleteFolderContents(safsFolder)
    safsFolder.Delete
    Set safsFolder = nothing
 	shell.Popup "SAFS in "& existsafs & " has been uninstalled successfully!", 0, "Information", 64 
		
	UnInstallSAFS = 1
    On Error Goto 0	 
End Function

'*************************************************************************************
'* WriteFileContent, write the content of a text file. 
'*
'* Assumes a global Scripting.FileSystemObject object named 'fso'
'* Arguments
'*        file: a text file with its full path.
'* fileContent: the text including linefeeds to write.
'*
'*   Returns 0: Success
'*          -1: Output file does not exist after write
'*************************************************************************************
Function WriteFileContent(file, fileContent)
    On Error Resume Next
    Set OutStream = fso.OpenTextFile(file, 2, True)
    OutStream.Write fileContent
    On Error Goto 0
    If not fso.FileExists(file) then
        WriteFileContent = -1
    else
        WriteFileContent = 0
    end if
End Function


'*************************************************************************************
' ReplaceFileContent, replace any findStr in the content of a text file with replacewith. 
'
'* Assumes a global Scripting.FileSystemObject object named 'fso'
'* Arguments
'*        file: a text file with its full path
'*     findStr: the string to find out
'* replacewith: the string to replace findStr with
'*
'*   Returns 0: success to preform the replacement
'*          -1: file not exist
'*************************************************************************************
Function ReplaceFileContent(file, findStr, replacewith)
	If not fso.FileExists(file) Then 
        Return -1
    End If

    On Error Resume Next   
    FileContents = fso.OpenTextFile(file).ReadAll
	'perform a textual comparison, replace without caring about upper/lower case
    FileContents = Replace(FileContents, findStr, replacewith, 1, -1, 1)  
    
	'write back the updated content to the source file
    Set OutStream = fso.OpenTextFile(file, 2, True)
    OutStream.Write FileContents
    On Error Goto 0

    ReplaceFileContent = 0
End Function 

'*********************************************************************************************
' ReplaceFilesInFolder, do ReplaceFileContent operation for every matched file in a folder.
' The suffix of any matched file should be found in filesuffix. 
'
'* Assumes a global Scripting.FileSystemObject object named 'fso'
'* Arguments
'*  folderPath: a folder's full path
'*  filesuffix: the suffix string of the files for replacement. For example: ".ini" or ".ini|*.bat"
'*     findStr: the string to find out
'* replacewith: the string to replace findStr with
'*
'* See ReplaceFileContent
'**********************************************************************************************
Sub ReplaceFilesInFolder(folderPath, filesuffix, findStr, replacewith)
	Set   mainFolder   =   fso.GetFolder(folderPath)   
	Set   fcollect  = mainFolder.Files
	Dim pos, afile
	For   Each   afile   in   fcollect
		'find the match in filesuffix for the suffix of a file name with 
		pos = InStrRev(filesuffix, Right(afile.Name,4), -1, 1)
		if (pos > 0) then
	        debug(folderPath&"\"&afile.Name)
		    ReplaceFileContent folderPath&"\"&afile.Name, findStr, replacewith
        end if
	Next   
End Sub

'*********************************************************************************************
' ReplaceFilesInSubFolder, do ReplaceFilesInFolder operation for each subfolder in a target folder.
' The suffix of any matched file should be found in filesuffix. 
'
'* Assumes a global Scripting.FileSystemObject object named 'fso'
'* Arguments
'*  parentPath: a target folder's full path, where every subfolder will be preformed ReplaceFilesInFolder with.
'*  filesuffix: the suffix string of the files for replacement. For example: ".ini" or ".ini|*.bat"
'*     findStr: the string to find out
'* replacewith: the string to replace findStr with
'*
'* See ReplaceFilesInFolder 
'**********************************************************************************************
Sub ReplaceFilesInSubFolder(parentPath, filesuffix, findStr, replacewith)
	Set mainFolder = fso.GetFolder(parentPath)
	Set subfolders = mainFolder.Subfolders
	For Each objsubfolder in subfolders
		ReplaceFilesInFolder parentPath & "\" & objsubfolder.Name, filesuffix, findStr, replacewith
	Next
End Sub