
Dim errH 
Dim parser
Dim xmlFile

if WScript.Arguments.Count = 0 then 
    WScript.Echo "You must specify the XML file to process!"& chr(13), _
    	         "This command must be run from a command line", _
                 "in order to provide the name of the file."
    WScript.Quit
end if

xmlFile = WScript.Arguments(0)
WScript.Echo "Processing "& xmlFile

Set parser = WScript.CreateObject("Msxml2.DOMDocument")
    
    parser.async = false
    parser.validateOnParse = true
    status = parser.load(xmlfile)
    
    if (NOT status) then 
    	Set errH = parser.parseError
    	WScript.Echo errH.reason &" at line "& errH.line &" "& errH.srcText
    else
    	WScript.Echo "No Errors Detected."
    end if

Set errH = Nothing
Set parser = Nothing
