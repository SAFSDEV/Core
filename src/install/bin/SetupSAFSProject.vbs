
Dim shell
Dim env
Dim fso

Dim message
Dim response
Dim numerrors
Dim DDE_RUNTIME, Default_INI_file
Dim installdir, installkey, title

Dim safsdir, safsenv, stafdir, stafenv
Dim curr_datastorej
Dim safs_datastorej
Dim arg, content

Dim cr, nl
Dim q

Dim Project_fldr, Project_fldr_name
Dim Datapool_fldr, Datapool_fldr_name
Dim Bench_fldr, Bench_fldr_name
Dim Dif_fldr, Dif_fldr_name
Dim Logs_fldr, Logs_fldr_name
Dim Test_fldr, Test_fldr_name
Dim safstid_ini, copy_safstid_ini, processcontainer_ini

cr        = chr(13)  'carriage return
nl        = chr(10)  'newline
q         = chr(34)  'dobule quote
numerrors = 0

title = "SAFS Project Setup"

safsdir = "c:\SAFS" 'Default install directory
safsenv = "SAFSDIR" 'Environment Variable for SAFS root directory.
stafdir = "c:\STAF" 'Default STAF install directory
stafenv = "STAFDIR" 'Environment Variable for STAF root directory.


'WScript.Interactive = false

Set shell = WScript.CreateObject("WScript.Shell")
Set env   = shell.Environment("SYSTEM")
Set fso   = WScript.CreateObject("Scripting.FileSystemObject")

On Error Resume Next

'handle 64-bit systems
installkey = shell.RegRead("HKLM\Software\Wow6432Node\")

On Error Goto 0

'User choice to continue setup or not
'============================================================
message = "Creating SAFS project in the current working directory..."& cr & cr
message = message &"Do you wish to proceed?"& cr & cr
message = message &"Click YES to proceed; NO to cancel the install."

'36=32+4  32=Question Icon; 4=Yes/No buttons
response = shell.Popup (message,0, title,36)

'possible responses:
'Yes   = 6
'No    = 7
'Cancel= 2

if response = 7 then WScript.Quit


'Get CURRENT FOLDER
'==========================================
Project_fldr = shell.CurrentDirectory

'shell.LogEvent 4, "Creating SAFS Project structure in current folder: "& Project_fldr
'shell.Popup "Creating SAFS Project structure in current folder:"& cr & cr & Project_fldr, 5, title, 64

arg = env(safsenv)
if Len(arg) > 0 then safsdir = arg

arg = env(stafenv)
if Len(arg) > 0 then stafdir = arg

safstid_ini = safsdir &"\project\safstid.ini"
copy_safstid_ini = Project_fldr &"\safstid.ini"

'Set local folder and file names
'===============================
Datapool_fldr_name = Project_fldr &"\Datapool\"
Bench_fldr_name = Project_fldr &"\Datapool\Bench\"
Dif_fldr_name = Project_fldr &"\Datapool\Dif\"
Logs_fldr_name = Project_fldr &"\Datapool\Logs\"
Test_fldr_name = Project_fldr &"\Datapool\Test\"


shell.LogEvent 4, "Creating SAFS Project structure in: "& Project_fldr
shell.Popup "Creating SAFS Project structure in:"& cr & cr & Project_fldr, 5, title, 64


'Create folders as needed (do not overwrite existing copy)
'=================================================================
 if (NOT (fso.FolderExists(Datapool_fldr_name))) then
    	Set Datapool_fldr = fso.CreateFolder(Datapool_fldr_name)
	Set Bench_fldr = fso.CreateFolder(Bench_fldr_name)
	Set DIf_fldr = fso.CreateFolder(Dif_fldr_name)
	Set Logs_fldr = fso.CreateFolder(Logs_fldr_name)
	Set Test_fldr = fso.CreateFolder(Test_fldr_name)

        shell.LogEvent 4, "Completed creation of SAFS Project structure..."
        shell.Popup "Completed creation of SAFS Project structure!", 5, title, 64
    
 else
       shell.LogEvent 1, "Datapool folder already exists! "
       shell.Popup "Datapool folder already exists!", 5, title, 48    
      

       'Create non-existent sub-folders in existing Datapool folder
       '=========================================================== 

       if(NOT (fso.FolderExists(Bench_fldr_name))) then
		Set Bench_fldr = fso.CreateFolder(Bench_fldr_name)
                shell.LogEvent 4, "Created Bench folder..."
                shell.Popup "Created Bench folder!", 5, title, 64
       else
           shell.LogEvent 1, "Bench folder already exists!"   
           shell.Popup "Bench folder already exists!", 5, title, 48
       end if 

       if(NOT (fso.FolderExists(Dif_fldr_name))) then
		Set DIf_fldr = fso.CreateFolder(Dif_fldr_name)
                shell.LogEvent 4, "Created Dif folder..."
                shell.Popup "Created Dif folder !", 5, title, 64
       else
           shell.LogEvent 1, "Dif folder already exists!"   
           shell.Popup "Dif folder already exists!", 5, title, 48
       end if 

       if(NOT (fso.FolderExists(Logs_fldr_name))) then
		Set Logs_fldr = fso.CreateFolder(Logs_fldr_name)
                shell.LogEvent 4, "Created Logs folder..."
                shell.Popup "Created Logs folder !", 5, title, 64
       else
           shell.LogEvent 1, "Logs folder already exists!"   
           shell.Popup "Logs folder already exists!", 5, title, 48
       end if 

       if(NOT (fso.FolderExists(Test_fldr_name))) then
   		Set Test_fldr = fso.CreateFolder(Test_fldr_name)
                shell.LogEvent 4, "Created Test folder..."
                shell.Popup "Created Test folder !", 5, title, 64
       else
           shell.LogEvent 1, "Test folder already exists!"   
           shell.Popup "Test folder already exists!", 5, title, 48
       end if  
  end if 

'Create ancillary files in Project folder
'========================================
if (NOT (fso.FileExists(copy_safstid_ini))) then
	content = cr &"[SAFS_DRIVER]"& cr & nl &"DriverRoot="& q & safsdir & q & cr & nl & cr & nl
	shell.LogEvent 4, "Creating default SAFSTID.INI file..."
	WriteFileContent copy_safstid_ini, content
end if 

processcontainer_ini = Project_fldr &"\processcontainer.ini"
if (NOT (fso.FileExists(processcontainer_ini))) then
	content = cr &"[SAFS_PROJECT]"& cr & nl &"ProjectRoot="& q & Project_fldr & q & cr & nl
	content = content & cr & nl &"[SAFS_ENGINES]"& cr & nl &"First="& cr & nl
	shell.LogEvent 4, "Default ProcessContainer.ini created"
	WriteFileContent processcontainer_ini, content
end if 

arg = Project_fldr &"\runSTAFProcessContainer.bat"
if(NOT (fso.FileExists(arg))) then
        content = cr & "SET CLASSPATH=%STAFDIR%\bin\JSTAF.jar;%SAFSDIR%\lib\SAFS.jar" & cr & nl & nl
	content = content & q &"%SAFSDIR%\jre\bin\java.exe"& q &" -cp "& q &"%CLASSPATH%"& q &" -Dsafs.processcontainer.ini=processcontainer.ini org.safs.tools.drivers.STAFProcessContainer" & cr & nl	
	shell.LogEvent 4, "Creating runSTAFProcessContainer.bat..."
	WriteFileContent arg, content
end if

arg = Project_fldr &"\runImageManager.bat"
if(NOT (fso.FileExists(arg))) then
        content = cr & "SET CLASSPATH=%STAFDIR%\bin\JSTAF.jar;%SAFSDIR%\lib\SAFS.jar" & cr & nl & nl
	content = content & q &"%SAFSDIR%\jre\bin\java.exe"& q &" -cp "& q &"%CLASSPATH%"& q &" org.safs.image.ImageManager" & cr & nl	
	shell.LogEvent 4, "Creating runImageManager.bat..."
	WriteFileContent arg, content
end if

arg = Project_fldr &"\STAFStart.bat"
if(NOT (fso.FileExists(arg))) then
	content = cr & "start /D" & stafdir &"\bin /separate "& stafdir &"\bin\stafproc.exe" & cr & nl
        content = content &"PING 1.1.1.1 -n 1 -w 3000 > NUL" & cr & nl
	
	shell.LogEvent 4, "Creating STAFStart.bat..."
	WriteFileContent arg, content
end if

arg = Project_fldr &"\STAFStop.bat"
if(NOT (fso.FileExists(arg))) then
	content = cr & "Call SAFSDebugShutdown.bat" & cr & nl
	content = content & "staf local shutdown shutdown" & cr & nl
	
	shell.LogEvent 4, "Creating STAFStop.bat..."
	WriteFileContent arg, content
end if

arg = Project_fldr &"\SAFSDebugStartup.bat"
if(NOT (fso.FileExists(arg))) then
        content = cr & "SET CLASSPATH=%STAFDIR%\bin\JSTAF.jar;%SAFSDIR%\lib\SAFS.jar" & cr & nl & nl
	content = content &"Call STAFStart.bat" & cr & nl
        content = content & q &"%SAFSDIR%\jre\bin\java.exe"& q &" -cp "& q &"%CLASSPATH%"& q &" org.safs.Log debug -file:"& Project_fldr &"\debuglog.txt"& cr &nl
	
	shell.LogEvent 4, "Creating SAFSDebugStartup.bat..."
	WriteFileContent arg, content
end if

arg = Project_fldr &"\SAFSDebugShutdown.bat"
if(NOT (fso.FileExists(arg))) then
	content = cr & "staf local queue queue name SAFS/TESTLOG message SHUTDOWN"& cr & nl
	
	shell.LogEvent 4, "Creating SAFSDebugShutdown.bat..."
	WriteFileContent arg, content
end if

arg = Project_fldr &"\SAFSProjectStartup.bat"
if(NOT (fso.FileExists(arg))) then
        content = cr & "SET CLASSPATH=%STAFDIR%\bin\JSTAF.jar;%SAFSDIR%\lib\SAFS.jar" & cr & nl & nl
	content = content &"Call STAFStart.bat" & cr & nl
	content = content &"staf local service add service safsinput library jstaf execute "& q & safsdir &"\lib\safsinput.jar"& q &" OPTION "& q &"J2=-cp %CLASSPATH%"& q &" OPTION "& q &"JVM="& safsdir &"\jre\bin\java.exe"& q &" PARMS dir "& q & Project_fldr &"\datapool" & q & cr & nl
	content = content &"staf local service add service safsmaps  library jstaf execute "& q & safsdir &"\lib\safsmaps.jar"& q &" PARMS dir "& q & Project_fldr &"\datapool" & q & cr & nl
	content = content &"staf local service add service safsvars  library jstaf execute "& q & safsdir &"\lib\safsvars.jar"& q & cr & nl
	content = content &"staf local service add service safslogs library jstaf execute "& q & safsdir &"\lib\safslogs.jar"& q &" PARMS dir "& q & Project_fldr &"\datapool\logs" & q & cr & nl
	
	shell.LogEvent 4, "Creating SAFSProjectStartup.bat..."
	WriteFileContent arg, content
end if

'Finished
'========
if (numerrors > 0) then
   
    shell.Popup "SAFS Project Setup completed with errors!", 5, title, 48

else
    
    shell.Popup "SAFS Project Setup completed successfully!", 5, title, 64

end if


Set shell = nothing
Set fso   = nothing
Set exec  = nothing

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


