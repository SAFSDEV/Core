
Option Explicit

'******************************************************************************
'* This script monitors and logs the memory used by a user-definable process--
'* iexplore.exe by default.  It will monitor the process until another 
'* user-definable process has closed--STAFProc.exe by default.
'* The user-definable output file location is c:\\safs\\data\\iememoryusage.txt 
'* by default.
'* 
'* Optional script parameters:
'*     
'*     -noprompt
'*           
'*           Bypass the initial prompt dialog and continue.
'*
'*     -delay <N seconds>
'*           
'*            Specify N seconds before monitoring will begin.
'*            By default this is 0
'*            
'*     -monitor <processname.exe>
'*     
'*            The process name to monitor and log memory usage on.
'*            By default this is 'iexplore.exe'
'*     
'*     -watch <processname.exe> 
'*     
'*            The running process name to watch.  This script will log memory 
'*            usage on the monitored process for as long as this watched process 
'*            is running.
'*            By default this is 'STAFProc.exe'
'*     
'*     -interval <N seconds>
'*           
'*            Specify N seconds between memory snapshots.
'*            By default this is 60 (1 minute)
'*            
'*     -output <alternate path>
'*           
'*            Allows the user to specify an alternate output file path.
'*            By default this is 'C:\\safs\\data\\iememoryusage.txt'
'*           
'*     -noviewer
'*           
'*           Do not view the saved output file upon completion.
'*
'*     -nocomplete
'*           
'*           Do not display the final dialog upon completion.
'*           
'******************************************************************************

Dim WshShell, counter, counterMax, objFSO, objFile, objWMIService, objProcess, colProcess, strOut
Dim arg, args, monitorproc, watchproc, outfile, sleepTime, boolIEStillRunning, boolJavaStillRunning
Dim lcarg, delay, qtdoutfile, showlog, showdone, showintro, cr, returncode

monitorproc = "iexplore.exe"
watchproc   = "STAFProc.exe"
outfile     = "c:\\safs\\data\\iememoryusage.txt"
qtdoutfile  = outfile
delay       = 0

counter = 0
sleepTime = 60000 '60000 milliseconds = 1 minute
boolIEStillRunning = True
boolJavaStillRunning = True
showlog  = True
showdone = True
showintro = True
cr = chr(13)  'carriage return

Dim i
Set WshShell = WScript.CreateObject("WScript.Shell")
Set args  = WScript.Arguments
arg = ""

' loop thru all args
'======================
For i = 0 to args.Count -1
    arg = args(i)        
    lcarg = lcase(arg)
    
    if (lcarg = "-output")      then
        if ( i < args.Count -1) then 
            outfile = args(i+1)
            qtdoutfile = outfile
        end if
    elseif (lcarg = "-monitor") then
        if ( i < args.Count -1) then
            monitorproc = args(i+1)
        end if
    elseif (lcarg = "-watch")   then
        if ( i < args.Count -1) then
            watchproc = args(i+1)
        end if
    elseif (lcarg = "-delay")   then
        if ( i < args.Count -1) then
            delay = args(i+1)
        end if        
    elseif (lcarg = "-interval")   then
        if ( i < args.Count -1) then
            sleeptime = args(i+1) * 1000
        end if        
    elseif (lcarg = "-noviewer")   then
            showlog = False
    elseif (lcarg = "-nocomplete")   then
            showdone = False
    elseif (lcarg = "-noprompt")   then
            showintro = False
    end if    
Next

Dim msg

If (showintro = True) Then 
   msg = "Do you wish to monitor '"& monitorproc &"'"& cr
   msg = msg &"until '"& watchproc &"' terminates?"& cr & cr
   msg = msg &"You can invoke this script with options:"& cr
   msg = msg &"---------------------------------------------------------------------"& cr 
   msg = msg &"-noprompt"& cr
   msg = msg &"Bypass this dialog."& cr & cr
   msg = msg &"-delay <N seconds>"& cr
   msg = msg &"Default: 0"& cr & cr
   msg = msg &"-monitor <processname to record>"& cr
   msg = msg &"Default: iexplore.exe"& cr & cr
   msg = msg &"-watch <processname to terminate on>"& cr
   msg = msg &"Default: STAFProc.exe"& cr & cr
   msg = msg &"-output <alternate output path>"& cr
   msg = msg &"Default: C:\SAFS\data\iememoryusage.txt"& cr & cr
   msg = msg &"-interval <N seconds>"& cr
   msg = msg &"Seconds between memory snapshots"& cr
   msg = msg &"Default: 60"& cr & cr
   msg = msg &"-noviewer"& cr
   msg = msg &"Bypass text viewer upon completion."& cr & cr
   msg = msg &"-nocomplete"& cr
   msg = msg &"Bypass last dialog upon completion."& cr & cr
   msg = msg &"---------------------------------------------------------------------"& cr & cr
   msg = msg &"Click YES to proceed;  NO to Cancel"
   returncode = WshShell.Popup(msg, 0, "SAFS Memory Monitor", 36) 
   If ((returncode = 2) Or (returncode = 7)) Then
	WScript.Quit
   End If
end if




'if needed,
'wrap outfile name in quotes if embedded space exists
'====================================================
'if InStr(1, outfile, " ", 1) > 0 then
'    qtdoutfile = """"& outfile &""""
'end if

Set objFSO = CreateObject("Scripting.FileSystemObject")
Set objFile = objFSO.CreateTextFile(outfile)

Set objWMIService = GetObject("winmgmts:\\.\root\cimv2")

'=============================================================
'
' Sleep if you need time for startup
'
'=============================================================
if delay > 0 then WScript.Sleep(delay)

Do While boolJavaStillRunning = True
	counter = counter + 1
	boolIEStillRunning = False
	boolJavaStillRunning = False
	Set colProcess = objWMIService.ExecQuery("Select * from Win32_Process")


	For Each objProcess in colProcess
		if objProcess.Name=monitorproc then
			strOut = counter &"   "& objProcess.Name  & _
			"   WorkingSetSize:"& objProcess.WorkingSetSize & _
			"   PrivatePageCount:"& objProcess.PrivatePageCount
			boolIEStillRunning = True
		end if
		
		if objProcess.Name=watchproc then
			boolJavaStillRunning = True
		end if
	Next
	
	if boolIEStillRunning = False then
		strOut = counter &"   "& monitorproc &" IS NOT RUNNING"
	end if

	objFile.WriteLine(strOut)
	
	if boolJavaStillRunning = True then WScript.Sleep(sleepTime)		
Loop

objFile.WriteLine(" ")
objFile.WriteLine("Monitor Complete.  "& watchproc &" is not running.")

objFile.Close

if showlog = True then 
    WshShell.Run "Notepad.exe "& outfile
end if
if showdone = True then WScript.Echo "SAFS Memory Monitor has shutdown."
WScript.Quit
