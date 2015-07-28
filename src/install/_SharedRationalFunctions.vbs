'*****************************************************************************
'* This script provides shared functions for other WSH files and scripts.
'*****************************************************************************


'*****************************************************************************
'* Return an array of partial JAR paths and names to be used in cleaning or 
'* building CLASSPATH entries for Rational tools.
'*****************************************************************************
Function getRationalClasspathArray()

    Dim RatClassPathArray(12)
    'Item position in array MUST NOT CHANGE
    RatClassPathArray(0)  = "\rational_ft.jar"	        'Append to rationalftdir for XDETester & FT
    RatClassPathArray(1)  = "\datapool_api.jar"	        'Append to rationalftdir for FT 6.x
    RatClassPathArray(2)  = "\rational_ft_core.jar"	'Append to rationalftdir for FT 6.x
    RatClassPathArray(3)  = "\xerces.jar"		'Append to rationalftdir for RobotJ & XDETester
    RatClassPathArray(4)  = "\rttssjava.jar"		'Append to rationalinstalldir for Robot
    RatClassPathArray(5)  = "\xmlParserAPIs.jar"	'Append to eclipsedir for RobotJ & XDETester
    RatClassPathArray(6)  = "\xercesImpl.jar"	        'Append to eclipsedir for RobotJ & XDETester
    RatClassPathArray(7)  = "\lib\safsrational_ft.jar"  'Append to SAFSDIR for FT
    RatClassPathArray(8)  = "\lib\safsrational_xde.jar" 'Append to SAFSDIR for XDETester
    RatClassPathArray(9)  = "\lib\safsrational_ft_enabler.jar"   'Append to SAFSDIR for XDETester & FT
    RatClassPathArray(10) = "\com.rational.test.ft.core_"   'Append to ibmpluginsdir for RFT 7 -> 8.1.0
    RatClassPathArray(11) = "\com.ibm.rational.test.ft.corecomponents_"   'For RFT 8.1.1

    getRationalClasspathArray = RatClassPathArray
End Function


'*****************************************************************************
'* Remove any old references to TestScript and TestScriptHelper from an 
'* existing RFT Project Datastore.
'*****************************************************************************
Sub cleanRFTProjectDatastore(projectdir)
    
    deleteFile projectdir &"\TestScript.java"
    deleteFile projectdir &"\TestScript.class"
    deleteFile projectdir &"\resources\TestScriptHelper.java"
    deleteFile projectdir &"\resources\TestScriptHelper.class"
    
End Sub
