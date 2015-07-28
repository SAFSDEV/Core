/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

import java.awt.AWTError;
import java.awt.HeadlessException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.safs.model.commands.DriverCommands;
import org.safs.natives.NativeWrapper;
import org.safs.robot.Robot;
import org.safs.text.FAILStrings;
import org.safs.text.FileUtilities;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.consoles.ProcessCapture;
import org.safs.tools.drivers.DriverInterface;
import org.safs.tools.stringutils.StringUtilities;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFResult;

/**
 * <br><em>Purpose:</em> DCDriverMiscCommands, process a miscellaneous driver commands, like pause
 * <br><em>Lifetime:</em> instantiated by DCDriverCommand
 * <p>
 * @author  Doug Bauman
 * @since   Sep 23, 2003
 *
 *   <br>   Sep 23, 2003    (DBauman) Original Release
 *   <br>   Oct 02, 2003    (DBauman) moved to package org.safs because no dependency on rational
 *   <br>   Nov 14, 2003    (DBauman) adding breakpoint commands:<br><pre>
 *    BP  -- Breakpoint -- Set a line as a debugging breakpoint.  Debugger stops
 *                         before processing of the next line if Breakpoints have
 *                         been Enabled.
 *    Breakpoints   ON  -- Enable/Disable stopping at BP records.
 *    CommandDebug  ON  -- Enable/Disable stopping at Driver Command (C) records.
 *    TestDebug     ON  -- Enable/Disable stopping at Test (T) records.
 *    RecordsDebug  ON  -- Enable/Disable stopping at every record.
 *   <br>   Nov 17, 2003    (DBauman) adding commands:<br>
 *    CloseApplication, LaunchApplication,
 *    CopyVariableValueEx, SetVariableValueEx,
 *    GetSystemDate, GetSystemDateTime, GetSystemTime,
 *    SetBenchDirectory, SetDifDirectory, SetProjectDirectory, SetTestDirectory,
 *    SetRootVerifyDirectory
 *   <br>   Nov 18, 2003    (DBauman) adding commands:<br>
 *    SetVariableValues, StartWebBrowser, ClearClipboard, SaveClipboardToFile
 *   <br>   Nov 19, 2003    (DBauman) adding commands:<br>
 *    SetClipboard
 *   <br>   Dec 9, 2003    (DBauman) adding commands:<br>
 *    VerifyClipboardToFile
 *   <br>   MAY 02, 2005    (Carl Nagle) adding command AssignClipboardVariable
 *   <br>   JUL 27, 2005    (Carl Nagle) adding command CallRemote contributed by Steve Sampson
 *   <br>   AUG 04, 2006    (Carl Nagle) adding commands AppMapChaining, AppMapResolve, GetAppMapValue
 *   <br>   AUG 22, 2006    (Bob Lawler) updated application() to call Runtime.exec() with null env params for LaunchApplication
 *   <br>   APR 15, 2008    (JunwuMa)added GetCompScreenResolution
 *   <br>   MAY 27, 2008    (JunwuMa)added ClearAllVariables 
 *   <br>   AUG 26, 2008    (Carl Nagle)Fixed formatted output of GetSystemDate, GetSystemTime, GetSystemDateTime 
 *   <br>	NOV 12, 2008	(LeiWang)	Modified method application(): if user want to start a batch file app.bat, but he just
 *   									give the file name "app" without suffix .bat as parameter, Runtime.getRuntime().exce("app")
 *   									will not work and throw an exception, so I catch this exception and use 
 *   									Runtime.getRuntime().exce("cmd /c app") to try again.
 *   <br>	MAY 19, 2010	(LeiWang) Modify method application(): Use ProcessConsole to deal the stream of stderr and stdout, and
 *                                                                 exitValue for Process. Add a parameter timeout, use this timeout
 *                                                                 to wait for the end of Process.
 *   <br>	JUN 04, 2010	(Carl Nagle) In application(), check Process exitValue to check for success or failure in launching app.
 *   									Reverting to correct use of reverted ProcessConsole.
 *   <br>   NOV 15, 2011    (Lei Wang) Modify method getSystemDateTime(): Add an optional parameter to make it convert to the military time.
 *                                   By default, the method will convert date to AM-PM time as before.
 *   <br>   JAN 27, 2014    (Lei Wang) Modify method clearArrayVariables(): don't clear SAFS-reserved variables.
 *   <br>   APR 29, 2014    (Lei Wang) Modify method getAppMapValue(): use default map if mapid is not provided;
 *                                                                   set map item value to testrecord's statusinfo so jsafs user can get it easily.
 *   <br>   NOV 26, 2014    (Lei Wang) Modify method saveClipboard()/verifyClipboard(): Call deduceXXXFile() to get test/bench file.
 *   <br>   JAN 04, 2015    (Lei Wang) Add method scrollWheel().
 **/
public class DCDriverMiscCommands extends DriverCommand {

  /** "C:\\Program Files\\Internet Explorer\\iexplore.exe" */
  public static final String BROWSER = "C:\\Program Files\\Internet Explorer\\iexplore.exe";
  /** "SAFSWebBrowserPath" */
  public static final String WEB_BROWSER_PATH_VAR	   	   = "SAFSWebBrowserPath";
  
  public static final String APPMAPRESOLVE                 = "AppMapResolve";
  public static final String APPMAPCHAINING                = "AppMapChaining";
  public static final String GETAPPMAPVALUE                = "GetAppMapValue";
  public static final String PAUSE                         = "pause";
  public static final String DELAY                         = "delay";
  public static final String GETVERSION                    = "getVersion";
  public static final String BP                            = "BP";
  public static final String BREAKPOINTS                   = "Breakpoints";
  public static final String CALLREMOTE                    = "CallRemote";
  public static final String COMMANDDEBUG                  = "CommandDebug";
  public static final String CAPTUREMOUSEPOSITION          = "CaptureMousePositionOnScreen";
  public static final String TESTDEBUG                     = "TestDebug";
  public static final String RECORDSDEBUG                  = "RecordsDebug";
  public static final String CLOSEAPPLICATION              = "CloseApplication";
  public static final String LAUNCHAPPLICATION             = "LaunchApplication";
  public static final String STARTWEBBROWSER               = "StartWebBrowser";
  public static final String COPYVARIABLEVALUEEX           = "CopyVariableValueEx";
  public static final String SETVARIABLEVALUEEX            = "SetVariableValueEx";
  public static final String SETVARIABLEVALUES             = "SetVariableValues";
  public static final String GETSYSTEMDATE                 = "GetSystemDate";
  public static final String GETSYSTEMDATETIME             = "GetSystemDateTime";
  public static final String GETSYSTEMTIME                 = "GetSystemTime";
  public static final String SETBENCHDIRECTORY             = "SetBenchDirectory";
  public static final String SETDIFDIRECTORY               = "SetDifDirectory";
  public static final String SETPROJECTDIRECTORY           = "SetProjectDirectory";
  public static final String SETTESTDIRECTORY              = "SetTestDirectory";
  public static final String SETROOTVERIFYDIRECTORY        = "SetRootVerifyDirectory";
  public static final String ASSIGNCLIPBOARDVARIABLE       = "AssignClipboardVariable";
  public static final String CLEARCLIPBOARD                = "ClearClipboard";
  public static final String SAVECLIPBOARDTOFILE           = "SaveClipboardToFile";
  public static final String SETCLIPBOARD                  = "SetClipboard";
  public static final String VERIFYCLIPBOARDTOFILE         = "VerifyClipboardToFile";
  public static final String CLEARALLVARIABLES             = "ClearAllVariables";     
  public static final String CLEARARRAYVARIABLES           = "ClearArrayVariables";     
  public static final String GETCOMPSCREENRESOLUTION       = "GetCompScreenResolution";
  public static final String GETREGISTRYKEYVALUE           = "GetRegistryKeyValue";
  public static final String WAITFORREGISTRYKEYEXISTS      = "WaitForRegistryKeyExists";
  public static final String WAITFORREGISTRYKEYVALUE       = "WaitForRegistryKeyValue";
  public static final String NOTIFYANDWAIT                 = "NotifyAndWait";
  
  String _command = null; // _command

  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public DCDriverMiscCommands () {
    super();
  }

  /**
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing; processedCommand is set to false
   * if we do not recognize this command
   * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
   * <br><em>Assumptions:</em>  none
   * <br>   JUL 27, 2005    (Carl Nagle) adding command CallRemote contributed by Steve Sampson
   * <br>   AUG 03, 2006    (Carl Nagle) Addition of AppMapResolve and AppMapChaining commands
   **/
  public void process() {
    try {
      _command = testRecordData.getCommand();
      if (_command.equalsIgnoreCase(PAUSE) || // pause expects seconds
          _command.equalsIgnoreCase(DELAY)) { // delay expects millisecs
        pause();
      } else if (_command.equalsIgnoreCase(GETVERSION)) {
        if (params.size() <= 0) {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          getLogUtilities().logMessage(testRecordData.getFac(), ", "+
                                       _command +
                                       ": wrong params, should include: variableName  ",
                                       FAILED_MESSAGE);
          return;
        }
        String version = SAFSVersion.VERSION;
        Iterator iterator = params.iterator();
        String var = (String) iterator.next();

        if (!setVariable(var, version)) {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          getLogUtilities().logMessage(testRecordData.getFac(),
                                       _command +
                                       ": setVariable failure: "+var,
                                       FAILED_MESSAGE);
          return;
        }
        getLogUtilities().logMessage(testRecordData.getFac(),
                                     genericText.convert(TXT_SUCCESS_2,
                                                         _command,
                                                         _command, version),
                                     Processor.GENERIC_MESSAGE);
        testRecordData.setStatusCode(StatusCodes.OK);
      } else if (_command.equalsIgnoreCase(BP)) {
        bp();
      } else if (_command.equalsIgnoreCase(BREAKPOINTS) ||
                 _command.equalsIgnoreCase(COMMANDDEBUG) ||
                 _command.equalsIgnoreCase(TESTDEBUG) ||
                 _command.equalsIgnoreCase(RECORDSDEBUG)) {
        turnBreakpointCommand();
      } else if (_command.equalsIgnoreCase(CLOSEAPPLICATION)) {
        application(false, false);
      } else if (_command.equalsIgnoreCase(LAUNCHAPPLICATION)) {
        application(true, false);
      } else if (_command.equalsIgnoreCase(STARTWEBBROWSER)) {
        application(true, true);
      } else if (_command.equalsIgnoreCase(COPYVARIABLEVALUEEX)) {
        setCopyVariableValueEx(false);
      } else if (_command.equalsIgnoreCase(SETVARIABLEVALUEEX)) {
        setCopyVariableValueEx(true);
      } else if (_command.equalsIgnoreCase(SETVARIABLEVALUES)) {
        setVariableValues();
      } else if (_command.equalsIgnoreCase(CAPTUREMOUSEPOSITION)) {
        captureMousePositionOnScreen();
      } else if (_command.equalsIgnoreCase(GETSYSTEMDATE)) {
        getSystemDateTime(1); //date
      } else if (_command.equalsIgnoreCase(GETSYSTEMDATETIME)) {
        getSystemDateTime(3); // both
      } else if (_command.equalsIgnoreCase(GETSYSTEMTIME)) {
        getSystemDateTime(2); //time
      } else if (_command.equalsIgnoreCase(SETBENCHDIRECTORY)) {
        setDirectory(STAFHelper.SAFS_VAR_BENCHDIRECTORY, "bench");
      } else if (_command.equalsIgnoreCase(SETDIFDIRECTORY)) {
        setDirectory(STAFHelper.SAFS_VAR_DIFDIRECTORY, "dif");
      } else if (_command.equalsIgnoreCase(SETTESTDIRECTORY)) {
        setDirectory(STAFHelper.SAFS_VAR_TESTDIRECTORY, "test");
      } else if (_command.equalsIgnoreCase(SETROOTVERIFYDIRECTORY)) {
        setDirectory("safsrootverifydirectory", "rootverify"); //??
      } else if (_command.equalsIgnoreCase(SETPROJECTDIRECTORY)) {
        setDirectory(STAFHelper.SAFS_VAR_PROJECTDIRECTORY, "project");
      } else if (_command.equalsIgnoreCase(ASSIGNCLIPBOARDVARIABLE)) {
        clipboard2Variable();
      } else if (_command.equalsIgnoreCase(CLEARCLIPBOARD)) {
        clearClipboard();
      } else if (_command.equalsIgnoreCase(CLEARALLVARIABLES)) {
        clearAllVariables();
      } else if (_command.equalsIgnoreCase(CLEARARRAYVARIABLES)) {
          clearArrayVariables();
      } 
      else if (_command.equalsIgnoreCase(SAVECLIPBOARDTOFILE)) {
        saveClipboard();
      } else if (_command.equalsIgnoreCase(SETCLIPBOARD)) {
        setClipboard();
      } else if (_command.equalsIgnoreCase(VERIFYCLIPBOARDTOFILE)) {
        verifyClipboard();
      } else if (_command.equalsIgnoreCase(CALLREMOTE)) {
        callRemote();
      } else if (_command.equalsIgnoreCase(APPMAPRESOLVE)) {
		   appMapResolve();
   	  } else if (_command.equalsIgnoreCase(APPMAPCHAINING)) {
	 	   appMapChaining();
	  } else if (_command.equalsIgnoreCase(GETAPPMAPVALUE)) {
		   getAppMapValue();
      } else if (_command.equalsIgnoreCase(GETCOMPSCREENRESOLUTION)) {
          commandGetCompScreenResolution();
      } else if (_command.equalsIgnoreCase(WAITFORREGISTRYKEYEXISTS)) {
          waitRegistryKeyExists();
      } else if (_command.equalsIgnoreCase(WAITFORREGISTRYKEYVALUE)) {
          waitRegistryKeyValue();
      } else if (_command.equalsIgnoreCase(GETREGISTRYKEYVALUE)) {
          getRegistryKeyValue();
      } else if (_command.equalsIgnoreCase(NOTIFYANDWAIT)) {
          notifyPrompt();
      } else if (DriverCommands.SCROLLWHEEL_KEYWORD.equalsIgnoreCase(_command)) {
          scrollWheel();
      } else {
        setRecordProcessed(false);
      }
      
    } catch (SAFSException ex) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "SAFSException: "+ex.getMessage(),
                     FAILED_MESSAGE);
    }
  }

  private void captureMousePositionOnScreen () throws SAFSException {
      if (params.size() < 2) {
    	  this.issueParameterCountFailure("X_VariableName, Y_VariableName");
          return;
      }      
      Iterator iterator = params.iterator();
      String xVar = (String) iterator.next();
      String yVar = (String) iterator.next();
      String command = testRecordData.getCommand().toLowerCase();
      Log.info("............................"+command+": xVar:"+xVar+", yVar:"+yVar);

      //Use Rational libraries to return the current position of the mouse on the screen.
      //Note: Sun's Java 1.5 contains a MouseInfo class which could be used in the same way by
      //a general driver, like the TID, when SAFS eventually moves to a newer version of Java.
      //Java 1.5 usage: Point mousePt = new Point(MouseInfo.getPointerInfo().getLocation());
      Point mousePt = null;
   	  try{ mousePt = new Point(MouseInfo.getPointerInfo().getLocation());}
   	  catch(Exception x){
       	  Log.debug(command +" handling "+ x.getClass().getSimpleName() +" while receiving coordinates from Java: "+ x.getMessage());
   		  this.issueActionFailure(x.getClass().getSimpleName()+":"+ x.getMessage());
   		  return;
   	  }

      //turns the double values returned by getX() and getY() into strings and
      //then attempts to save the string to the variable names given as parameters
      if (!setVariable(xVar, String.valueOf(mousePt.getX())) || 
          !setVariable(yVar, String.valueOf(mousePt.getY()))) {
    	  this.issueErrorPerformingAction(FAILStrings.text(FAILStrings.COULD_NOT_SET_VARS,
    			  "Could not set one or more variable values."));
      }
      else {
    	  this.issueGenericSuccess(GENStrings.convert(GENStrings.MOUSE_POSITION_SAVED, 
    			  "Mouse position "+ mousePt.getX()+","+ mousePt.getY()+" saved.", 
    			  String.valueOf(mousePt.getX()), String.valueOf(mousePt.getY())));
      }
      return;
    }
  
  /** <br><em>Purpose:</em> pause/delay
   **/
  private void pause () {
    if (params.size() <= 0) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(), getClass().getName()+", "+_command +
                     ": there are no params, the first param is supposed to be number of seconds",
                     FAILED_MESSAGE);
      return;
    }
    Iterator iterator = params.iterator();
    // get the number of seconds name
    String param = (String) iterator.next();

    String command = _command.toLowerCase();
    Log.info("............................."+command+": "+param);
    // now call the script
    try {
      Integer seci = new Integer(param);
      int secii = seci.intValue();
      if (command.equalsIgnoreCase(PAUSE)) { // pause expects seconds
        secii = 1000*secii;
      }
      if (delay(secii)) {
        log.logMessage(testRecordData.getFac(),
                       genericText.convert(TXT_SUCCESS_2, command, command, param),
                       GENERIC_MESSAGE);
        testRecordData.setStatusCode(StatusCodes.OK);
      } else {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      }
    } catch (NumberFormatException nfe) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(), getClass().getName()+", "+_command +
                     ": not an integer: "+param+ ", msg: "+nfe.getMessage(),
                     FAILED_MESSAGE);
    }
  }
  /** <br><em>Purpose:</em> turnBreakpointCommand: param is either ON or OFF
   **/
  private void turnBreakpointCommand () {
    if (params.size() <= 0) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(), getClass().getName()+", "+_command +
                     ": there are no params, the first param is supposed to be ON|OFF",
                     FAILED_MESSAGE);
      return;
    }
    Iterator iterator = params.iterator();
    // get the number of seconds name
    String param = (String) iterator.next();

    param = param.toUpperCase();
    String command = _command;
    Log.info("............................."+command+": "+param);
    boolean flag = true;
    if (param.equals("ON")) {
      flag = true;
    } else if (param.equals("OFF")) {
      flag = false;
    } else {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(), getClass().getName()+", "+command+":"+param +
                     ": not ON or OFF.", FAILED_MESSAGE);
      return;
    }
    if (command.equalsIgnoreCase(BREAKPOINTS)) {
      Processor.setBreakpointsOn(flag);
    } else if (command.equalsIgnoreCase(COMMANDDEBUG)) {
      DriverCommandProcessor.setDCBreakpointsOn(flag);
    } else if (command.equalsIgnoreCase(TESTDEBUG)) {
      TestStepProcessor.setCFBreakpointsOn(flag);
    } else if (command.equalsIgnoreCase(RECORDSDEBUG)) {
      DriverCommandProcessor.setDCBreakpointsOn(flag);
      TestStepProcessor.setCFBreakpointsOn(flag);
    }
    log.logMessage(testRecordData.getFac(),
                   genericText.convert(TXT_SUCCESS_2, command, command, param),
                   GENERIC_MESSAGE);
    testRecordData.setStatusCode(StatusCodes.OK);
  }

  /** <br><em>Purpose:</em> bp: do the breakpoint
   **/
  private void bp () {
    String text = getClass().getName() +" "+genericText.translate("Breakpoint");
    log.logMessage(testRecordData.getFac(),
                   genericText.convert(TXT_SUCCESS_1, text, text),
                   GENERIC_MESSAGE);
    testRecordData.setStatusCode(StatusCodes.OK);
    checkBreakpoints(text);
  }

  /** used to keep instances of Process instances, so that later we can kill one **/
  private HashMap pMap = new HashMap();

  /** application: launchApplication, closeApplication, and startWebBrowser
   * @param                     launch, boolean, if true launch, else close app
   * <br> AUG 22, 2006    (Bob Lawler) updated LaunchApplication to call Runtime.exec() with null env params
   **/
  private void application (boolean launch, boolean webBrowser) throws SAFSException {
    if (!webBrowser && params.size() <= (launch?1:0)) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(), getClass().getName()+", "+_command +
                     ": wrong params, the first param is supposed to be the application id name"+
                     (launch?", and then the application executable command line":""),
                     FAILED_MESSAGE);
      return;
    }
    Iterator iterator = params.iterator();
    String url = (webBrowser?(iterator.hasNext()?(String)iterator.next():""):"");
    String idname = (webBrowser?(iterator.hasNext()?(String)iterator.next():""):(String)iterator.next());
    int timeout = 5000;//millisecond
    
    String appname = null;
    if(webBrowser){
    	// attempt to locate a DDVariable (or ApplicationConstant) specifying a 
    	// non-default browser path
    	try{ appname = getVariable(WEB_BROWSER_PATH_VAR);}
    	catch(Exception x){
    		Log.info(_command +" IGNORING missing or invalid "+ WEB_BROWSER_PATH_VAR +" setting. (Optional)");
    	}
    	// if no valid browser path found then attempt to launch the default web browser
    	if (appname == null || appname.length()==0){
    		// interesting...WIN32 ShellExecute when given an empty url ("") opens Windows Explorer.
    		// WIN32 "about:blank" works for both Firefox and IE in the case of an empty URL.
    		// WIN32 "about:home" works for IE to go to whatever the home page is, BUT
    		//       "about:home" does not work for Firefox.  It is considered "invalid".
    		// Changing an empty URL to "about:blank" may be an issue where a few users might have  
    		// launched IE with no URL and IE would go to the preset Home page.  
    		// However, Firefox does NOT go to the default Home page if you simply launch it 
    		// with an empty URL.
    		if (url==null||url.length()==0) url="about:blank";
    		Object result = NativeWrapper.LaunchURLInDefaultWebBrowser(url);
    		// non-null means we likely succeeded
    		// if null, then we will fall back to the original default mechanism and try again
    		if(result instanceof Integer){//WIN32 returns an Integer 42, usually, on success
        		Log.info(_command +" using NativeWrapper for URL '"+ url +"' returned "+ String.valueOf(((Integer)result).intValue()));
		        log.logMessage(testRecordData.getFac(),
		                genericText.convert(TXT_SUCCESS_3, 
                		_command+":"+idname +" '"+ url +"' successful.",
                        _command, idname, url), GENERIC_MESSAGE);
    	        testRecordData.setStatusCode(StatusCodes.OK);
    	        return;    			
    		}
    	}
    	// resort to original default hardcoded Internet Explorer implementation
    	if (appname!=null && appname.length() > 0) appname += " "+url;
    	else appname = BROWSER+" "+url;
    }else if(launch){
    	appname = (String) iterator.next();
    }
    String workdir = null;
    String param = null;
    String appmap = null;
    File  thedir  = null;
    String ucappname = null;
    boolean isWinBatch = false;
    
    if ( appname != null ) {
        if (launch) {
        	String appnamelookup = getAppMapItem( null, null, appname);
        	if((appnamelookup!=null)&&(appnamelookup.length()>0)){
        		appname = appnamelookup;
        	}
        }
    	ucappname = appname.toUpperCase();
    }

    // problem reusing existing CMD console if running
    // start windows-specific code
    if ( ucappname != null ) {
    	if ( (ucappname.equals("CMD"))||
	        (ucappname.equals("CMD.EXE"))||
	        (ucappname.endsWith("\\CMD"))||
	        (ucappname.endsWith("\\CMD.EXE"))||
	        (ucappname.equals("COMMAND"))||
	        (ucappname.equals("COMMAND.COM"))||
	        (ucappname.endsWith("\\COMMAND"))||
	        (ucappname.endsWith("\\COMMAND.COM")) ) {
	    	Log.info("DCDC.application detecting request for Win CMD or COMMAND execution using ProcessConsole.");
	    	try{
	    		if (idname.length()>0){
	    			appname = "CMD /K start \""+ idname +"\" "+ appname;
	    		}
	    		else{
	    			appname = "CMD /K start "+ appname;
	    		}
	    	}
	    	catch(Exception x){
	    		appname = "CMD /K start "+ appname;
	    	}
    	}
    	// Carl Nagle 2009.02.13
    	// might need to use a console on cscript.exe or wscript.exe
    	else if ((ucappname.trim().endsWith(".BAT"))||
    			 (ucappname.trim().indexOf(".BAT ") > 0)){
	    	Log.info("DCDC.application detected request for Win .BAT execution....");
	    	isWinBatch = true;
    	}
    }// end windows-specific code
	Log.info("DCDC.application using APPNAME: "+ appname);
    try {
      workdir = (String) iterator.next();
      if (workdir != null){
      	if (workdir.length()>0){
      		thedir = new CaseInsensitiveFile(workdir).toFile();
      		if(!(thedir.isDirectory())){
      			thedir = null;
      			workdir= null;
      		}
      	}else{
      	    thedir = null;
      	    workdir= null;
      	}
      }
      Log.info("DCDC.application using WORKDIR: "+ workdir);
      Log.info("DCDC.application using WORKDIR resolves to: "+ thedir);

      param = (String) iterator.next();
      Log.info("DCDC.application using PARAM: "+ param);
      if (param != null) appname = appname + " " + param;
      appmap = (String) iterator.next();
      Log.info("DCDC.application using APPMAP: "+ appmap);
      if(iterator.hasNext()){
    	  timeout = Integer.parseInt(iterator.next().toString());
      }
    } catch (Exception ee) {
    	Log.debug("DCDC.application IGNORING Exception: "+ ee.getClass().getSimpleName());
    } // ignore
    Log.info(".............................url: "+url);
    Log.info(".............................idname: "+idname);
    Log.info(".............................appname: "+appname);
    try {
      if (launch) {
    	Log.info("DCDC.LaunchApplication attempting launch of:"+ appname);    	
        Runtime rt = Runtime.getRuntime();
        Process p = null;
        
        if (isWinBatch){
    		try{
    			STAFHelper staf = testRecordData.getSTAFHelper();
    			Log.info("DCDC.Launch BAT file thru STAF with workdir: " + workdir);
    			staf.localStartProcess(appname, workdir);
     		}catch(Exception io){
            	Log.debug("IOException occurred and unrecoverable: "+ io.getClass().getSimpleName()+" "+ io.getMessage());
            	throw new IOException(io.getMessage());//caught below
    		}
        }else{
	        try{
	        	if (workdir==null) {
	        		try{
	        			p = rt.exec(appname);
	        		}
	        		catch(SecurityException sx){
	        	    	Log.debug("DCDC Runtime.exec(appname) SecurityException: "+ sx.getClass().getSimpleName()+":"+ sx.getMessage());
	        		}
	        	} else {
	        		try{
	        			p = rt.exec(appname, null, thedir);
	        		}
	        		catch(SecurityException sx){
	        	    	Log.debug("DCDC Runtime.exec(appname, null, thedir) SecurityException: "+ sx.getClass().getSimpleName()+":"+ sx.getMessage());
	        		}
	        	}
	        }catch(IOException io){
	        	//Try to use "cmd /c commandString" to run
	        	//This fixes problems like missing ".bat" extension, but does not fix other 
	        	//issues that can generate an IOException.  We will need to put code here to 
	        	//better deduce WHAT kind of problem we really have that generated the IOException.
	        	if( ! appname.toUpperCase().startsWith("CMD /C")){
	            	Log.debug("IOException occured. We will try 'cmd /c commandString' to execute.");
	        		appname = "cmd /c " + appname;
		        	if (workdir==null) {
		        		try{
		        			p = rt.exec(appname);
		        		}
		        		catch(SecurityException sx){
		        	    	Log.debug("DCDC Runtime.exec(cmd /c appname) SecurityException: "+ sx.getClass().getSimpleName()+":"+ sx.getMessage());
		        		}
		        	} else {
		        		try{
		        			p = rt.exec(appname, null, thedir);
		        		}
		        		catch(SecurityException sx){
		        	    	Log.debug("DCDC Runtime.exec(cmd /c appname, null, thedir) SecurityException: "+ sx.getClass().getSimpleName()+":"+ sx.getMessage());
		        		}
		        	}
	        	}
	        	else{
	            	Log.debug("IOException occurred and unrecoverable: "+ io.getClass().getSimpleName()+" "+ io.getMessage());
	            	throw io;//caught below
	        	}
	        }
        }
    	try{ 
    		Log.info("DCDC.LaunchApplication process '"+ idname +"' created: "+p);
    	}
    	catch(NullPointerException np){
    		Log.debug("DCDC.LaunchApplication process '"+ idname +"' is NULL!");
    	}
    	
    	if(p!=null){
			Log.info("DCDC: "+ _command +" checking success for "+ appname);
			ProcessCapture console = new ProcessCapture(p);
			Thread thread = new Thread(console);
			thread.start();
			int  loop = 0;
			long loopsleep = 750; //milliseconds
			int  loopmax = 3;
			int exitValue = 99;
			while(loop++ < loopmax){
		    	try {
					exitValue = p.exitValue();//throws IllegalStateException if still running
	    		    p.destroy();
					Log.info("DCDC: "+ _command +" Process exited with code '"+exitValue+"' for "+ appname);
					if(exitValue!=0){
						console.shutdown();
						Vector data = console.getData();
						String lf = "\n";
						StringBuffer message = new StringBuffer(lf);
						for(int line=0;line < data.size();line++) 
							message.append(data.get(line) +lf);
		    			Log.debug("DCDC: "+ _command +" Process may not have terminated successfully: "+ message.toString());
		    		    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		    		    log.logMessage(testRecordData.getFac(), 
		    		    		genericText.convert(TXT_FAILURE_1, "Unable to perform "+_command+" : "+appname,
		    		    				_command+" : "+appname), FAILED_MESSAGE);
		    		    return;
					}else{
		    			Log.info("DCDC: "+ _command +" Process seems to have exited normally for "+ appname);
		    			break;
					}
				} catch (IllegalThreadStateException e) {
					//if we got here, then the process is still running
					//we will check it up to loopmax to be sure all is well
				} catch(Exception e2){
	    			Log.debug("DCDC: IGNORING "+ _command +" Process or Thread "+ 
	    					  e2.getClass().getSimpleName()+"; "+e2.getMessage());
	    			break;
				}
				try{Thread.sleep(loopsleep);}catch(InterruptedException x){}
			}
			console.shutdown();
    	}
        pMap.put(idname, p);
        
        if(thedir==null){
	        log.logMessage(testRecordData.getFac(),
	                genericText.convert(TXT_SUCCESS_3, 
	                		_command+":"+idname +" '"+appname +"' successful.",
	                        _command, idname, appname), GENERIC_MESSAGE);
        }else{
	        log.logMessage(testRecordData.getFac(),
	                genericText.convert(TXT_SUCCESS_3a, 
	                		_command+":"+idname +" '"+appname +"' successful using '"+ thedir.getAbsolutePath() +"'",
	                        _command, idname, appname, thedir.getAbsolutePath()), GENERIC_MESSAGE);
        }
        testRecordData.setStatusCode(StatusCodes.OK);
        return;
      } else {
        Process p = (Process) pMap.get(idname);
        if (p != null) {
          p.destroy();
          pMap.remove(idname);
        } else {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          log.logMessage(testRecordData.getFac(), _command +
                         ": no app started by Driver with idname: "+idname,
                         FAILED_MESSAGE);
          return;
        }
      }
      log.logMessage(testRecordData.getFac(),
                     genericText.convert(TXT_SUCCESS_2, _command,
                                         _command, idname),
                     GENERIC_MESSAGE);
      testRecordData.setStatusCode(StatusCodes.OK);
    } catch (IOException io) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(), getClass().getName()+", "+_command +
                     ": "+io.getMessage()+(workdir!=null?", workdir: "+workdir: ""),
                     FAILED_MESSAGE);
    }
  }

  /** setCopyVariableValueEx: set/copy the value of a dynamic DDVariable to value/another var.
   ** @param set, if true, then first param is dst var and second param is simply the value,
   ** otherwise the first param is the source variable, and the second param is the dest param.
   **/
  private void setCopyVariableValueEx (boolean set) throws SAFSException {
    if (params.size() <= 1) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(), getClass().getName()+", "+_command +
                     ": wrong params, should be two.",
                     FAILED_MESSAGE);
      return;
    }
    Iterator iterator = params.iterator();
    String src = (String) iterator.next(); //src for COPY
    String dst = (String) iterator.next(); //dst for COPY
    String val = "";
    if (set) {
      val = dst; // swap for SET
      dst = src; // swap for SET
      src = "";
    } else {
      val = getVariable(src);
    }
    if (!setVariable(dst, val)) {
      String error = failedText.convert("could_not_set",
                     "Could not set '"+ dst +"' to '"+ val +"'",
                     dst, val);
      issueActionFailure(error);
    }else{
	  String message = genericText.convert("something_set",
                       "'"+ dst +"' set to '"+ val +"'",
        	           dst, val);
	  issueGenericSuccess(message);
    }
  }

  /**
   * setVariableValueEx: set the value of a dynamic DDVariable(s) to value(s)/other var(s).
   * this is already done by the calling drivers.  SetVariableValues is just a convenient
   * keyword placeholder for something that is automatically already done.
   * If additional code exists here to do anything else then all standard driver
   * implementations become broken.
   * (Carl Nagle) 2004.10.25
   **/
  private void setVariableValues () throws SAFSException {
    testRecordData.setStatusCode(StatusCodes.OK);
  	log.logMessage(testRecordData.getFac(),
  	               genericText.convert("success1", testRecordData.getInputRecord() + " successful.",
  	                                   testRecordData.getInputRecord()),
  	               GENERIC_MESSAGE);
  }

  /** getSystemDateTime: get system date/time to variable
   ** @param type, int, bit 1: date, bit 2: time, so values:
   ** 1: date, 2, time, 3: both
   **/
  private void getSystemDateTime (int type) throws SAFSException {
	String debugmsg = getClass().getName()+".getSystemDateTime() ";
	String dst = "";
	boolean getMilitaryTime = false;
	String currentTime = "";
	java.util.Date ud = null;
	
    if (params.size() <= 0) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(), getClass().getName()+", "+_command +
                     ": wrong params, the first param is supposed to be the dest var.",
                     FAILED_MESSAGE);
      return;
    }
    Iterator iterator = params.iterator();
    //Get the parameter 'variable name' where the time will be stored
    dst = (String) iterator.next();
    
    //Get the optional parameter 'getMilitaryTime', if it is true, the converted
    //time will be in 24-hours format (0-23); otherwise, the time will be 12-hours
    //format (1-12)
    if(iterator.hasNext()){
    	getMilitaryTime = StringUtilities.convertBool((String)iterator.next());
    }

    if(getMilitaryTime){
    	Log.debug(debugmsg+" Time will be in military format.");
    }else{
    	Log.debug(debugmsg+" Time will be in AM-PM format.");    	
    }
    
    ud = new java.util.Date();
    if (type == 1) { //date only
    	currentTime = StringUtilities.getDateString(ud);
    } else if (type == 2) { //time only
    	currentTime = StringUtilities.getTimeString(ud, getMilitaryTime);
    } else { //both date and time
    	currentTime = StringUtilities.getDateTimeString(ud, getMilitaryTime);
    }
    if (!setVariable(dst, currentTime)) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     " setVariable failure, dst: "+dst,
                     FAILED_MESSAGE);
      return;
    }
    // set status to ok
    log.logMessage(testRecordData.getFac(),
                   genericText.convert(TXT_SUCCESS_3, _command,
                                       _command, dst, currentTime),
                   GENERIC_MESSAGE);
    testRecordData.setStatusCode(StatusCodes.OK);
  }

  /** setDirectory: set/change the dir in which the framework will find/create 'dir' things.
   ** @param vardirname, String
   **/
  private void setDirectory (String vardirname, String altname) throws SAFSException {
    if (params.size() <= 0) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(), getClass().getName()+", "+_command +
                     ": wrong params, should be one.",
                     FAILED_MESSAGE);
      return;
    }
    Iterator iterator = params.iterator();
    String path = (String) iterator.next();

    File dir = new CaseInsensitiveFile(path).toFile();
    if (!dir.isAbsolute()) {
      String pdir = getVariable(STAFHelper.SAFS_VAR_PROJECTDIRECTORY);
      if (pdir == null) return;
      dir = new CaseInsensitiveFile(pdir, path).toFile();
    }
    if (!dir.isDirectory()) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     " setVariable failure, no such dir: "+dir.getAbsolutePath(),
                     FAILED_MESSAGE);
      return;
    }
    path = dir.getAbsolutePath();

    if (!setVariable(vardirname, path)) {
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     " setVariable failure, vardirname: "+vardirname+", path: "+path,
                     FAILED_MESSAGE);
      return;
    }
    // set status to ok
    log.logMessage(testRecordData.getFac(),
                   genericText.convert(TXT_SUCCESS_3, _command,
                                       _command, vardirname, path),
                   GENERIC_MESSAGE);
    testRecordData.setStatusCode(StatusCodes.OK);
  }

  private void clearClipboard() throws SAFSException{
      Toolkit tk = Toolkit.getDefaultToolkit();
      Clipboard cl = tk.getSystemClipboard();
      StringSelection ss = new StringSelection("");
      cl.setContents(ss, ss);
      // set status to ok
      log.logMessage(testRecordData.getFac(),
                     genericText.convert(TXT_SUCCESS_1, _command +" successful.",
                                         _command),
                     GENERIC_MESSAGE);
      testRecordData.setStatusCode(StatusCodes.OK);
  }
  
  /**
   * return a list of variable names, these variables are critical to SAFS and must exist.
   * @return List, a list of variable names, SAFS reserved variables' name.
   */
  private List<String> getReservedCriticalVariableNames(){
	  List<String> variables = new ArrayList<String>();
	  
	  variables.add(STAFHelper.SAFS_VAR_PROJECTDIRECTORY);
	  variables.add(STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY);
	  variables.add(STAFHelper.SAFS_VAR_BENCHDIRECTORY);
	  variables.add(STAFHelper.SAFS_VAR_TESTDIRECTORY);
	  variables.add(STAFHelper.SAFS_VAR_DIFDIRECTORY);
	  variables.add(STAFHelper.SAFS_VAR_LOGSDIRECTORY);
	  variables.add(STAFHelper.SAFS_VAR_SYSTEMUSERID);
	  variables.add(DriverInterface.DRIVER_CONTROL_VAR);
	  variables.add(DriverInterface.DRIVER_CONTROL_POF_VAR);
	  
	  return variables;
  }
  
  /**
   * return a list of variable names, these variables are reserved to SAFS but can be void.
   * @return List, a list of variable names, SAFS reserved variables' name.
   */  
  private List<String> getReservedNonCriticalVariableNames(){
	  List<String> variables = new ArrayList<String>();
	  
	  variables.add(STAFHelper.SAFS_VAR_SECSWAITFORWINDOW);
	  variables.add(STAFHelper.SAFS_VAR_SECSWAITFORCOMPONENT);
	  variables.add(STAFHelper.SAFS_VAR_COMMANDLINEBREAKPOINT);
	  variables.add(STAFHelper.SAFS_VAR_SAFSACTIVECYCLE);
	  variables.add(STAFHelper.SAFS_VAR_SAFSACTIVESUITE);
	  variables.add(STAFHelper.SAFS_VAR_SAFSACTIVESTEP);
	  
	  return variables;
  }
  
  /**
   * @return List, a list of variable names, SAFS reserved variables' name.
   */ 
  private List<String> getReservedVariableNames(){
	  List<String> variables = new ArrayList<String>();
	  
	  variables.addAll(getReservedCriticalVariableNames());
	  variables.addAll(getReservedNonCriticalVariableNames());
	  
	  return variables;
  }
  
  private boolean isReservedVariable(String variable){
	  for(String reservedVar: getReservedVariableNames()){
		  if(reservedVar.equals(variable)) return true;
	  }
	  return false;
  }
  
  /**
   * preform ClearAllVariables by resetting SAFSVARS service, after that reset initial preset variables.
   * @throws SAFSException
   */
  private void clearAllVariables() throws SAFSException {
      String debugMsg = getClass().getName()+".clearAllVariables() ";
      
      Log.info("................clearAllVariables:");
      // 1. backup initial preset variables 
      Log.info("................... save the preset variables"); 
      Hashtable<String, String> reservedVariables = new Hashtable<String, String>();
      for(String var: getReservedVariableNames()){
    	  reservedVariables.put(var, getVariable(var));
      }
      
	  // 2. clear all variables by RESET 
      Log.info("................... execute SAFSVARS RESET"); 
	  STAFHelper helper = testRecordData.getSTAFHelper();
	  String machine = helper.getMachine();

	  STAFResult result = helper.submit2ForFormatUnchangedService(machine, STAFHelper.SAFS_VARIABLE_SERVICE, "RESET");	      
	  // test the result
	  if (result.rc != 0) {
	     issueActionFailure("RC:"+ result.rc +":RESULT:"+result.result);
	  	 return;
	  }   
	  // 3. restore initial preset variables
      Log.info("................... restore the preset variables"); 
	  
      for(String var: getReservedCriticalVariableNames()){
    	  if(!setVariable(var, reservedVariables.get(var))){
              testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
              log.logMessage(testRecordData.getFac(),
                         " fail to call setVariable for setting preset Variables ",
                         FAILED_MESSAGE);
          	  return;
    	  }
      }

      String value = null;
      for(String var:getReservedNonCriticalVariableNames()){
    	  value = reservedVariables.get(var);
    	  if(!value.isEmpty()) setVariable(var, value);
      }

      Log.info(debugMsg+" success clear all variables and restore the preset variables");
	  log.logMessage(testRecordData.getFac(),
				 genericText.convert("success1", _command +" successful.",
									 _command ),
				 GENERIC_MESSAGE);
	  testRecordData.setStatusCode(StatusCodes.OK);      
  }

  /**
   * perform ClearArrayVariables by DELETING STAF/SAFSVARS of a particular prefix.
   * @throws SAFSException
   */
  private void clearArrayVariables() throws SAFSException{
      if (params.size() <= 0) {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          this.issueParameterCountFailure();
          return;
      }
      Iterator iterator = params.iterator();
      String str = (String) iterator.next();
      if (str.length() < 0) {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          this.issueParameterValueFailure("ARRAYNAME");
          return;
      }      

	  STAFHelper helper = testRecordData.getSTAFHelper();
	  String machine = helper.getMachine();

	  STAFResult result = helper.submit2ForFormatUnchangedService(machine, STAFHelper.SAFS_VARIABLE_SERVICE, "LIST V2");
	  List<String> vars = new ArrayList<String>();
	  String item;
      if ((result.rc == STAFResult.Ok) && (result.result.length() > 0)){
    	  String lcstr = str.toLowerCase();
    	  String eq = "=";
	      StringTokenizer counter = new StringTokenizer(result.result, "\n\r");
	      for(; counter.hasMoreTokens();){
			  try{
				  item = counter.nextToken();
				  int eqindex = item.indexOf(eq);
				  if (eqindex > 0){
					  item = item.substring(0, eqindex);
					  if(item.toLowerCase().startsWith(lcstr) && !isReservedVariable(item))
						  vars.add(item);
				  }
			  }catch(Exception ex){
			      Log.debug("Error parsing SAFSVARS V2 LIST command!");
			      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			      this.issueActionFailure(failedText.convert("failed.extract", "Failed to extract 'SAFSVARS LIST V2'.",
                          "SAFSVARS LIST V2"));
			      return;
			  }
		  }
      }
      Log.info("Preparing to DELETE "+ vars.size()+" SAFSVARS variables with Name prefix: "+ str);
      if(!vars.isEmpty()){
    	  Iterator<String> it = vars.iterator();    	  
    	  while(it.hasNext()){
    		  item = (String)it.next();
    		  helper.submit2ForFormatUnchangedService(machine, STAFHelper.SAFS_VARIABLE_SERVICE, "DELETE "+ item);
    	  }
      }
	  
	  // test the result
      // set status to ok
      testRecordData.setStatusCode(StatusCodes.OK);
      log.logMessage(testRecordData.getFac(),
                     genericText.convert(TXT_SUCCESS_2a, _command +" using '"+ str +"' successful.",
                                         _command, str),
                     GENERIC_MESSAGE);
  }
  
  private void setClipboard() throws SAFSException{
      Toolkit tk = Toolkit.getDefaultToolkit();
      Clipboard cl = tk.getSystemClipboard();
      if (params.size() <= 0) {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          this.issueParameterCountFailure();
          return;
      }
      Iterator iterator = params.iterator();
      String str = (String) iterator.next();

      StringSelection ss = new StringSelection(str);
      cl.setContents(ss, ss);
      // set status to ok
      log.logMessage(testRecordData.getFac(),
                     genericText.convert(TXT_SUCCESS_2a, _command +" using '"+ str +"' successful.",
                                         _command, str),
                     GENERIC_MESSAGE);
      testRecordData.setStatusCode(StatusCodes.OK);
  }
    
  private String getClipboardText() throws SAFSException{
	  String debugmsg = StringUtils.debugmsg(DCDriverMiscCommands.class, "getClipboardText");
	  String val = null;
	  Reader reader = null;
	  Toolkit tk = Toolkit.getDefaultToolkit();
	  Clipboard cl = tk.getSystemClipboard();

	  try {
		  tk = Toolkit.getDefaultToolkit();
		  cl = tk.getSystemClipboard();
	  }catch (AWTError ae) {
		  // awt Toolkit not available so we are not able to handle this action
		  Log.error(debugmsg+"AWT Toolkit unavailable\n", ae);
		  throw new SAFSException(ae);
	  }catch (HeadlessException ae) {
		  // guiless mode for JVM
		  Log.error(debugmsg+"AWT Toolkit unavailable\n", ae);
		  throw new SAFSException(ae);
	  }
	  
	  try{
		  Object requestor = null;// not used currently
		  Transferable tf = cl.getContents(requestor);
		  
		  DataFlavor[] dfs = tf.getTransferDataFlavors();
		  DataFlavor dataFlavorReader = null;
		  DataFlavor dataFlavorPlainTextReader = DataFlavor.getTextPlainUnicodeFlavor();
		  
		  for(DataFlavor d:dfs){
			  if(d.equals(DataFlavor.stringFlavor)){
				  Log.debug(debugmsg+" using DataFlavor: "+ d.getHumanPresentableName()+" ("+ d.getMimeType()+")");
				  val = (String) tf.getTransferData(d);
				  break;
			  }else if(d.equals(dataFlavorPlainTextReader)){
				  dataFlavorReader = d;
			  }
		  }
		  
		  if(val==null){
			  char[] buffer = new char[1024*10];
			  int nchars = 0;
			  if(dataFlavorReader==null) dataFlavorReader = DataFlavor.selectBestTextFlavor(dfs);
			  Log.debug(debugmsg+" using DataFlavor: "+ dataFlavorReader.getHumanPresentableName()+" ("+ dataFlavorReader.getMimeType()+")");
			  reader = dataFlavorReader.getReaderForText(tf);
			  while(nchars > -1){
				  nchars = reader.read(buffer);
				  if(nchars > 0) val += new String(buffer, 0, nchars);
			  }
			  reader.close();
		  }

	  }catch(Exception x){
		  Log.debug(debugmsg+" ignoring Exception:", x);
	  }finally{
		  try{ if(reader != null) reader.close();}catch(Exception x2){;}
	  }
	  
	  if(val==null){
		  Log.debug(debugmsg+" doesn't get any value from clipboard, set it to \"\"");
		  val = "";
	  }
	  Log.debug(debugmsg+" returning: '"+ val +"'");
	  return val;	 
  }
  
  private void saveClipboard() throws SAFSException{
      if (params.size() <= 0) {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          this.issueParameterCountFailure();
          return;
      }
      Iterator iterator = params.iterator();
      Log.info("params: "+params);

      File name = deduceTestFile((String) iterator.next());
      String path = name.getAbsolutePath();
      Log.info("path: "+path);
      
      String encoding = "UTF-8";
      if(iterator.hasNext()){
    	  encoding = (String) iterator.next();
      }
      Log.info("encoding: "+encoding);
      
      try {
    	String val = getClipboardText();
        FileUtilities.writeStringToFile(path, encoding, val);
      } catch (IOException ioe) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(), _command + ": io failure for: "+params+
                       ", msg: "+ioe.getMessage(),
                       FAILED_MESSAGE);
        return;
      }  
      
      // set status to ok
      log.logMessage(testRecordData.getFac(),
                     genericText.convert(TXT_SUCCESS_2a, _command +" successful using '"+ path,
                                         _command, path),
                     GENERIC_MESSAGE);
      testRecordData.setStatusCode(StatusCodes.OK);
  }
  
  /** 
   * verify clipboard to file
   **/
  private void verifyClipboard () throws SAFSException {
    try {
        if (params.size() <= 0) {
            testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
            this.issueParameterCountFailure();
          return;
        }
        Iterator iterator = params.iterator();
        Log.info("params: "+params);
        
        File name = deduceBenchFile((String) iterator.next());
        String path = name.getAbsolutePath();
        Log.info("path: "+path);
        
        String encoding = "UTF-8";
        if(iterator.hasNext()){
      	  encoding = (String) iterator.next();
        }
        Log.info("encoding: "+encoding);

        //save an "actual" in the Datapool\Test directory
        File testpath = deduceTestFile(name.getName());
        String val = getClipboardText();
        try {
          FileUtilities.writeStringToFile(testpath.getAbsolutePath(), encoding, val);
        } catch (IOException ioe) {
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          log.logMessage(testRecordData.getFac(), _command + ": io failure for: "+params+
                         ", msg: "+ioe.getMessage(),
                         FAILED_MESSAGE);
          return;
        }            
        
        String benchString = FileUtilities.readStringFromEncodingFile(path, encoding);
        Log.info("realValue: "+val);
        Log.info("compValue: "+benchString);
        if (!val.equals(benchString)) {
          this.issueActionFailure(genericText.convert("contents_do_not_match", 
        		  "Contents of 'Clipboard' do not match contents of '"+ path,
                                           "Clipbaord", path));
          return;
        }
        
        // set status to ok
        log.logMessage(testRecordData.getFac(),
                     genericText.convert(TXT_SUCCESS_2a, _command +" successful using '"+ path +"'.",
                                         _command, path),
                     GENERIC_MESSAGE);
        testRecordData.setStatusCode(StatusCodes.OK);
      
    } catch (IllegalStateException ise) {
      ise.printStackTrace();
      throw new SAFSException("ise: "+ise.getMessage());
    } catch (AWTError ae) {
      ae.printStackTrace();
      throw new SAFSException("ae: "+ae.getMessage());
    } catch (FileNotFoundException fnfe) {
      throw new SAFSException(": "+fnfe.getMessage());
    } catch (Exception he) {
      he.printStackTrace();
      throw new SAFSException("he: "+he.getMessage());
    }
  }


    /**
     * AssignClipboardVariable
     * Assign clipboard contents to variable
     **/
    private void clipboard2Variable () throws SAFSException {
    	// insufficient parameters provided?
	 	if (params.size() <= 0) {
    		issueParameterCountFailure();
      		return;
    	}

    	Iterator iterator = params.iterator();
    	String varname = (String) iterator.next();
    	try{
			if (varname.substring(0,1).equals("^")) {
				varname = varname.substring(1);
			}
    	}
		catch(Exception ex){;}

  		String val = getClipboardText();

    	// write to DDVariable
	    if (!setVariable(varname, val)) {
	      String error = failedText.convert("could_not_set",
	                     "Could not set '"+ varname +"' to '"+ val +"'",
	                     varname, val);
	      issueActionFailure(error);
	    }else{
		  String message = genericText.convert("something_set",
	                       "'"+ varname +"' set to '"+ val +"'",
	        	           varname, val);
		  issueGenericSuccess(message);
	    }
  	}

  /** <br><em>: Purpose:</em> CallRemote
   */
  private void callRemote () throws SAFSException {
  	if (params.size() <= 3) {
  		issueParameterCountFailure();
  		return;
  	}
	Iterator iterator = params.iterator();
	String prt = (String) iterator.next(); // Protocol
	String sys = (String) iterator.next(); // Machine name
	String srv = (String) iterator.next(); // Service
	String cmd = (String) iterator.next(); // Command
	String var = null;
	String par = null;
	String s = " ";
	try {
		var = (String) iterator.next(); // Variable root name
		if (var.length() == 0) var = "result"; //not passed default it
	    if (var.charAt(0)=='^'){ var = var.substring(1);} //strip the carat if present
	} catch (Exception ee) {
		var = "result";  // field may not exist in record at all
	}

	try { par = (String) iterator.next();}
	catch (Exception ee) {
		par = "";// field may not exist in record at all
	}

	if (par.length() > 0)
	    cmd = new StringBuffer(cmd).append(s).append(par).toString();

	String fullcmd = new StringBuffer(prt).append(s).append(sys).append(s).append(srv).append(s).append(cmd).toString();
	if (prt.equalsIgnoreCase("STAF")) { //STAF protocol
		try {
		    String command = _command.toLowerCase();
		    Log.info("............................."+command+" "+ fullcmd);
			STAFResult result = testRecordData.getSTAFHelper().submit2WithVar(sys, srv, cmd, var);
		  	// test the result
		  	if (result.rc != 0) {
			    Log.info(".............................STAFResult: "+result.rc+":\""+result.result+"\"");
		  		issueActionFailure("RC:"+ result.rc +":RESULT:"+result.result);
		  		return;
		  	}
		    Log.info(".............................STAFResult: "+result.rc+":\""+result.result+"\"");
		  	issueGenericSuccess(":"+ fullcmd);

		} catch (STAFException se){
	  		issueActionFailure(se.getClass().getSimpleName()+":"+fullcmd);
		}
	} else {
	  		issueActionFailure(failedText.convert("support_not_found",
	  		    "Support for '"+ prt +"' not found!", prt));
	}
  }

  private void appMapResolve () throws SAFSException {
	  if (params.size() <= 0) {
		  issueParameterCountFailure();
		  return;
	  }
	  Iterator iterator = params.iterator();
	  // get ON or OFF
	  String param = ((String) iterator.next()).trim();
  	  STAFResult result = null;
	  STAFHelper helper = testRecordData.getSTAFHelper();
	  String machine = helper.getMachine();

	  if (param.equalsIgnoreCase("ON")){
		result = helper.submit2ForFormatUnchangedService(machine, "SAFSMAPS", "ENABLERESOLVE");
		// test the result
		if (result.rc != 0) {
			issueActionFailure("STAFRC:"+ result.rc +":STAFRESULT:"+result.result);
			return;
		}
	  }else if (param.equalsIgnoreCase("OFF")){
		result = helper.submit2ForFormatUnchangedService(machine, "SAFSMAPS", "DISABLERESOLVE");
		// test the result
		if (result.rc != 0) {
			issueActionFailure("STAFRC:"+ result.rc +":STAFRESULT:"+result.result);
			return;
		}
	  }else{
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		log.logMessage(testRecordData.getFac(), _command +":"+param +
					   ": not ON or OFF.", FAILED_MESSAGE);
		return;
	  }
	  testRecordData.setStatusCode(StatusCodes.OK);
	  log.logMessage(testRecordData.getFac(),
				   genericText.convert("success1", _command +" "+ param +" successful.",
	                                   _command +" "+ param),
				   GENERIC_MESSAGE);
  }

  private void appMapChaining () throws SAFSException {
	  if (params.size() <= 0) {
		  issueParameterCountFailure();
		  return;
	  }
	  Iterator iterator = params.iterator();
	  // get ON or OFF
	  String param = ((String) iterator.next()).trim();
	  STAFResult result = null;
	  STAFHelper helper = testRecordData.getSTAFHelper();
	  String machine = helper.getMachine();

	  if (param.equalsIgnoreCase("ON")){
		result = helper.submit2ForFormatUnchangedService(machine, "SAFSMAPS", "ENABLECHAIN");
		// test the result
		if (result.rc != 0) {
			issueActionFailure("STAFRC:"+ result.rc +":STAFRESULT:"+result.result);
			return;
		}
	  }else if (param.equalsIgnoreCase("OFF")){
		result = helper.submit2ForFormatUnchangedService(machine, "SAFSMAPS", "DISABLECHAIN");
		// test the result
		if (result.rc != 0) {
			issueActionFailure("STAFRC:"+ result.rc +":STAFRESULT:"+result.result);
			return;
		}
	  }else{
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		log.logMessage(testRecordData.getFac(), _command +":"+param +
					   ": not ON or OFF.", FAILED_MESSAGE);
		return;
	  }
	  testRecordData.setStatusCode(StatusCodes.OK);
	  log.logMessage(testRecordData.getFac(),
				 genericText.convert("success1", _command +" "+ param +" successful.",
									 _command +" "+ param),
				 GENERIC_MESSAGE);
  }

  private void getAppMapValue () throws SAFSException {
	  if ( params.size() < 4 ) {
		  issueParameterCountFailure();
		  return;
	  }
	  Iterator iterator = params.iterator();

	  String mapid = ((String) iterator.next()).trim();
	  String section = ((String) iterator.next()).trim();
	  String item = ((String) iterator.next()).trim();
	  String varName = ((String) iterator.next()).trim();

	  if(mapid.isEmpty()){
		  mapid = testRecordData.getAppMapName();
		  IndependantLog.warn("User does not provide map id, use default map '"+mapid+"'");
	  }

	  if (item.length()==0){
	  	issueParameterValueFailure("ITEM");
	  	return;
	  }
      if (varName.length()==0){
	    issueParameterValueFailure("VARNAME");
	    return;
	  }

	  String varValue = getAppMapItem(mapid, section, item);
      setVariable(varName, varValue);
      //Set the value to StatusInfo for easy use for JSAFS user
      testRecordData.setStatusInfo(varValue);

	  testRecordData.setStatusCode(StatusCodes.OK);
	  String message = genericText.convert("app_map_value",
	                   "Application Map '"+ mapid +"' "+ section +":"+
	                   item +" = '"+ varValue +"'.",
	                   mapid, section, item, varValue);
      String message2 = genericText.convert("something_set",
	 				   "'"+ varName +"' set to '"+ varValue +"'",
					   varName, varValue);

	  log.logMessage(testRecordData.getFac(), message, message2,  GENERIC_MESSAGE);
  }
  
  
  private void waitRegistryKeyExists () throws SAFSException {
	  final String TIMEOUT_DEFAULT = "15";
	  if ( params.size() < 1 ) {
		  issueParameterCountFailure();
		  return;
	  }
	  Iterator iterator = params.iterator();

	  String keyname = ((String) iterator.next()).trim();
	  if (keyname.length()==0){
		  	issueParameterValueFailure("KEY");
		  	return;
		  }
	  String valuename = null; // is optional
	  String strtimeout = TIMEOUT_DEFAULT;
	  int tseconds = 15;
	  if(iterator.hasNext()) {
		  valuename = ((String) iterator.next()).trim();
		  if(valuename.length()==0) valuename = null;
	  }
	  if (valuename == null){
		Log.info("WaitForRegistryKeyExists seeking a Key.  No KeyValue specified...");
	  }
      String msgval = new String(keyname);
      if(valuename != null) msgval+=": "+valuename;
      
	  if(iterator.hasNext()) strtimeout = ((String) iterator.next()).trim();
      try{ 
		tseconds = Integer.parseInt(strtimeout);
		if(tseconds < 0) {
			tseconds = 0;
			strtimeout = "0";
		}
	  }
	  catch(NumberFormatException nf){
		Log.info("WaitForRegistryKeyExists IGNORING invalid TIMEOUT value. Using Default "+ TIMEOUT_DEFAULT);
	  }
	  boolean exists = false;
	  for(int i=0;!exists && i<=tseconds;i++){ 
		exists = NativeWrapper.DoesRegistryKeyExist(keyname, valuename);
	  	if(!exists && i<tseconds) try{Thread.sleep(1000);}catch(Exception x){;}	  		
	  }
	  if(exists){
  		String message = genericText.convert("found_timeout", 
				msgval +" was found within timeout "+ strtimeout, 
				msgval, strtimeout);
  		issueGenericSuccess(message);
	  }else{
  		String warning = genericText.convert("not_found_timeout", 
				msgval +" was not found within timeout "+ strtimeout, 
				msgval, strtimeout);
  		issueActionWarning(warning);
      }		  
  }
  
  private void waitRegistryKeyValue () throws SAFSException {
	  final String TIMEOUT_DEFAULT = "15";
	  if ( params.size() < 3 ) {
		  issueParameterCountFailure();
		  return;
	  }
	  Iterator iterator = params.iterator();

	  String keyname = ((String) iterator.next()).trim();
	  if (keyname.length()==0){
	  	issueParameterValueFailure("KEY");
	  	return;
	  }
	  String valuename = ((String) iterator.next()).trim();
	  if (valuename.length()==0){
	  	issueParameterValueFailure("KEYVALUE");
	  	return;
	  }
	  String expected = ((String) iterator.next()).trim();
	  if (expected.length()==0){
	  	issueParameterValueFailure("EXPECTEDVALUE");
	  	return;
	  }
	  String msgval = valuename+"="+expected;
	  String strtimeout = TIMEOUT_DEFAULT;
	  int tseconds = 15;
	  boolean ignore_case = false;
	  if(iterator.hasNext()) strtimeout = ((String) iterator.next()).trim();
      try{ 
		tseconds = Integer.parseInt(strtimeout);
		if(tseconds < 0) {
			tseconds = 0;
			strtimeout = "0";
		}
	  }
	  catch(NumberFormatException nf){
		Log.info("WaitForRegistryKeyValue IGNORING invalid TIMEOUT value. Using Default "+ TIMEOUT_DEFAULT);
	  }
	  String strignore_case = "";
	  boolean ignoreCase = false;
	  if(iterator.hasNext()) strignore_case = ((String) iterator.next()).trim();
      if ((strignore_case.equalsIgnoreCase(CASEINSENSITIVE_FLAG))||
    	         (strignore_case.equalsIgnoreCase(CASE_INSENSITIVE_FLAG))||
    	         (strignore_case.equalsIgnoreCase(String.valueOf(false)))){
   		  Log.info("WaitForRegistryKeyValue IGNORING case-sensitivity...");
    	  ignoreCase = true;	      	  
      }
	  
	  String actual = null;
	  boolean matched = false;
	  boolean exists = false;
	  for(int i=0;!matched && i<=tseconds;i++){
		try{ actual = NativeWrapper.GetRegistryKeyValue(keyname, valuename).toString();}
		catch(NullPointerException x){/* NULL can be returned */}
		exists = (actual != null);
		if(exists){
			if(ignoreCase){
				matched = actual.equalsIgnoreCase(expected);
			}else{
				matched = actual.equals(expected);
			}
		}
	  	if(!matched && i<tseconds) try{Thread.sleep(1000);}catch(Exception x){;}	  		
	  }
	  if(matched){
  		String message = genericText.convert("found_timeout", 
				msgval +" was found within timeout "+ strtimeout, 
				msgval, strtimeout);
  		issueGenericSuccess(message);
	  }else{
  		String warning = genericText.convert("not_found_timeout", 
				msgval +" was not found within timeout "+ strtimeout, 
				msgval, strtimeout);
  		issueActionWarning(warning);
      }		  
  }

  
  private void getRegistryKeyValue () throws SAFSException {
	  if ( params.size() < 3 ) {
		  issueParameterCountFailure();
		  return;
	  }
	  Iterator iterator = params.iterator();

	  String keyname = ((String) iterator.next()).trim();
	  if (keyname.length()==0){
	  	issueParameterValueFailure("KEY");
	  	return;
	  }
	  String valuename = ((String) iterator.next()).trim();
	  if (valuename.length()==0){
	  	issueParameterValueFailure("KEYVALUE");
	  	return;
	  }
	  String varname = ((String) iterator.next()).trim();
	  if (valuename.length()==0){
	  	issueParameterValueFailure("VARNAME");
	  	return;
	  }
	  setVariable(varname, "");
	  String actual = null;
	  boolean exists = false;
	  try{ actual = NativeWrapper.GetRegistryKeyValue(keyname, valuename).toString();}
	  catch(NullPointerException x){/* NULL can be returned */}
	  exists = (actual != null);
	  if(exists){
    	// write to DDVariable
	    if (!setVariable(varname, actual)) {
	      String error = failedText.convert("could_not_set",
	                     "Could not set '"+ varname +"' to '"+ actual +"'",
	                     varname, actual);
	      issueActionWarning(error);
	    }else{
		  String message = genericText.convert("something_set",
	                       "'"+ varname +"' set to '"+ actual +"'",
	        	           varname, actual);
		  issueGenericSuccess(message);
	    }
	  }else{
		String msgval = keyname +":"+ valuename;
  		String warning = genericText.convert(FAILStrings.COULD_NOT_GET,
  				"Could not get "+ msgval +".",msgval);
  		issueActionWarning(warning);
      }		  
  }

  
  /**
   *  perform getCompScreenResolution. Actually it get the screen size and write the width and height to two variables.
   * @throws SAFSException
   */
  private void commandGetCompScreenResolution() throws SAFSException {
      String debugMsg = getClass().getName() + ".commandGetCompScreenResolution(): ";
      Log.info("...start getting screen resolution");
      if (params.size() < 2) {
        testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(), 
                		failedText.convert(FAILStrings.PARAMSIZE_1, ": wrong param number!",_command),
                       FAILED_MESSAGE);
        return;
      }
      Iterator iterator = params.iterator();
      String xres = (String) iterator.next();
      String yres = (String) iterator.next();
      
      Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
      java.awt.Dimension screensize = toolkit.getScreenSize (); 

      if (!setVariable(xres, String.valueOf(screensize.width)) || 
          !setVariable(yres, String.valueOf(screensize.height)) ) {
        
          Log.debug(debugMsg+" failed to set variable "+xres+" or "+yres);
          log.logMessage(testRecordData.getFac(),
                  		"failed to set variable "+xres+" or "+yres,
                  		FAILED_MESSAGE);
          testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
          return;
      }
      // set status to ok
      Log.info(debugMsg+" success setting screen-resolution variables");
      log.logMessage(testRecordData.getFac(),
                     genericText.convert(TXT_SUCCESS_2, _command,
                                         _command, " xres="+screensize.width+"; yres="+screensize.height),
                     PASSED_MESSAGE);
      testRecordData.setStatusCode(StatusCodes.OK);
    }
  
    private void notifyPrompt() throws SAFSException {
        String debugMsg = getClass().getName() + ".notifyPrompt: ";
        if (params.size() < 1) {
        	Log.info(debugMsg +" insufficient parameters.");
        	issueParameterCountFailure();
            return;
        }
        Iterator<String> iterator = params.iterator();
        String text = iterator.next();
        String title = iterator.hasNext() ? iterator.next() : _command;
        try{ 
        	Log.info(debugMsg +"'"+title+"' notification displaying.");
        	JOptionPane.showMessageDialog(null, text, title, JOptionPane.PLAIN_MESSAGE);
        	Log.info(debugMsg +"'"+title+"' notification dismissed.");
        }
        catch(Throwable ignore){
        	Log.info(debugMsg +"'"+title+"' ignoring notification "+ ignore.getClass().getName()+": "+ ignore.getMessage());
        }
        issueGenericSuccess(title);
    }
    
    private void scrollWheel() throws SAFSException {
    	String debugMsg = StringUtils.debugmsg(false);
    	if (params.size() < 1) {
    		Log.info(debugMsg +" insufficient parameters.");
    		issueParameterCountFailure();
    		return;
    	}
    	Iterator<?> iterator = params.iterator();
    	String amount = null;
    	int wheelAmt = 0;
    	try{
    		amount = (String) iterator.next();
    		wheelAmt = Integer.parseInt(amount);
    	}catch(Exception e){
    		IndependantLog.error(debugMsg+StringUtils.debugmsg(e));
    		issueParameterValueFailure("Wheel Amount");
    		return;
    	}
    	
    	boolean success = false;
    	try{
    		success = Robot.mouseWheel(wheelAmt);
    		
    	}catch(Throwable th){
    		Log.info(debugMsg +"Met"+StringUtils.debugmsg(th));
    	}
    	
    	if(success){
    		issueGenericSuccessUsing(amount, null);
    	}else{
    		issueErrorPerformingActionUsing(amount,"");
    	}
    }
}
