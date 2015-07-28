
Dim shell
Dim env
Dim fso

Dim Robot_dsfolder
Dim message
Dim response
Dim numerrors
Dim robotj_title

Dim safsdir, safsenv
Dim curr_datastorej
Dim safs_datastorej
Dim arg

Dim cr
Dim q

cr        = chr(13)  'carriage return
q         = chr(34)  'dobule quote
numerrors = 0

robotj_title = "IBM Rational Datastore Setup"


safsdir = "c:\SAFS" 'Default install directory
safsenv = "SAFSDIR" 'Environment Variable for SAFS root directory.


'WScript.Interactive = false

Set shell = WScript.CreateObject("WScript.Shell")
Set env   = shell.Environment("SYSTEM")
Set fso   = WScript.CreateObject("Scripting.FileSystemObject")

'User choice to continue RobotJ Engine setup or not
'======================================
message = "This script copies a RobotJ Datastore template into the current working directory"& cr & cr
message = message &"Do you wish to proceed?"& cr & cr
message = message &"Click YES to proceed; NO to cancel the install."

'36=32+4  32=Question Icon; 4=Yes/No buttons
response = shell.Popup (message,0, robotj_title,36)

'possible responses:
'Yes   = 6
'No    = 7
'Cancel= 2

if response = 7 then WScript.Quit



'Get CURRENT CLASSIC ROBOT DATASTORE FOLDER
'==========================================
Robot_dsfolder = shell.CurrentDirectory
'shell.Popup "Setup RobotJ Datastore in current folder:"& cr & cr & Robot_dsfolder, 5,robotj_title, 64

'Set Local Folders FOR COPY
'==========================
curr_datastorej     = Robot_dsfolder    


'Get SAFS location
'=================
arg = env(safsenv)
if Len(arg) > 0 then safsdir = arg
'shell.Popup "SAFS install location:"& cr & cr & safsdir, 5, robotj_title, 64


'Set SAFS datastorej folder
'==========================
safs_datastorej     = safsdir &"\datastorej"
'shell.Popup "SAFS RobotJ template folder:"& cr & cr & safs_datastorej, 5, robotj_title, 64
'shell.Popup "Copying necessary folders to:"& cr & cr & curr_datastorej, 5, robotj_title, 64


'Copy datastorej folder as needed (do not overwrite existing copy)
'=================================================================
if (fso.FolderExists(safs_datastorej)) then
    fso.CopyFolder safs_datastorej, curr_datastorej, true
    'shell.Popup "Completed copy of SAFS RobotJ Datastore folders!", 5, robotj_title, 64
else
    shell.Popup "Error finding DatastoreJ template!", 5, robotj_title, 48
    numerrors = numerrors + 1
end if


'Finished
'========
if (numerrors > 0) then

    shell.Popup "RobotJ Datastore Setup completed with errors!", 5, robotj_title, 48
else    
    shell.Popup "RobotJ Datastore Setup completed successfully!", 5, robotj_title, 64
end if


Set shell = nothing
Set fso   = nothing
Set exec  = nothing
