/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
package org.safs.ios;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.regex.PatternSyntaxException;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSProcessorInitializationException;
import org.safs.StatusCodes;
import org.safs.TestRecordData;
//import org.safs.TestRecordHelper;
import org.safs.image.ImageUtils;
import org.safs.robot.Robot;
import org.safs.tools.*;
import org.safs.tools.consoles.ProcessCapture;
import org.safs.tools.consoles.ProcessConsole;
import org.safs.tools.drivers.DriverConstant;

/**
 * Utilities for the IOS testing environment including the preparation, launch, and monitoring of UIAutomation using 
 * the Instruments SDK tool.
 * 
 * @author Carl Nagle
 */
public class Utilities {

	/** SAFSDIR variable export from STAFEnv.sh script **/
	public static String rootRunDir = System.getenv("SAFSDIR");

	/** "/Library/SAFS/IOS/ascript/" */
	public static String ROOT_ASCRIPTS_DIR = rootRunDir + "/IOS/ascript/";

	/** "/Library/SAFS/IOS/jscript/" */
	public static String ROOT_JSCRIPTS_DIR = rootRunDir + "/IOS/jscript/";

	/** "Instruments/selectRecentScript.scpt" 
	 * Default applescript relative to overall SAFS applescript root directory. 
	 * This is used to set IOS Instruments execution script.  Primarily for IOS 5 and later.
	 */
	public static String PATH_SELECT_RECENT_SCRIPT_ASCRIPT = "Instruments/selectRecentScript.scpt";
	
	/** "IS_ABSOLUTE_PATH"
	 * SubDir spec saying the item file path provided is NOT relative. It is absolute. */
	public static final String JSCRIPTS_ABSOLUTE_PATH = "IS_ABSOLUTE_PATH";
	
	/** "hook.js" */
	public static String JSCRIPTS_HOOK = "hook.js";
	
	/** "trd.js" */
	public static String JSCRIPTS_TRD = "trd.js";
	
	/** "iospcdata.js" */
	public static String JSCRIPTS_IOSPCDATA = "iospcdata.js";
	
	/** "Utilities/startup.js" */
	public static String DEFAULT_JSSTARTUP_IMPORT = "Utilities/startup.js";
	
	/** "SAFSRuntime.js" */
	public static String DEFAULT_SAFSRUNTIME_SCRIPT = "SAFSRuntime.js";
	
	/** "ProcessContainer.js" */
	public static String DEFAULT_PROCESSCONTAINER_SCRIPT = "ProcessContainer.js";
	
	/** "[username]" */
	public static final String TAG_USERNAME = "[username]";
	
	/** "/Users/[username]/Library/Application Support/Instruments/" */
	public static String INSTRUMENTS_APPLICATIONSUPPORT_DIR = "/Users/"+ TAG_USERNAME +"/Library/Application Support/Instruments/";
	
	/** "PreviousScripts"
	 * Name of file used by Instruments to store script preferences */
	public static String INSTRUMENTS_PREVIOUSSCRIPTS_FILE = "PreviousScripts";

	/** "SAFSBackupScripts"
	 * Name of file used by SAFS to store original script preferences for restore. */
	public static String INSTRUMENTS_BACKUPSCRIPTS_FILE = "SAFSBackupScripts";

	/** "/Library/SAFS/IOS/instruments/" */
	public static String ROOT_INSTRUMENTS_DIR = rootRunDir + "/IOS/instruments/";
	
	/** "recent/SAFSRuntime" */
	public static String SAFSRUNTIME_INSTRUMENTS_PREFS = "recent/SAFSRuntime";
	
	/** "recent/ProcessContainer" */
	public static String PROCESSCONTAINER_INSTRUMENTS_PREFS = "recent/ProcessContainer";
	
	/** "/Library/SAFS/IOS/ibt/images/" */
	public static String ROOT_IBT_IMAGES_DIR = rootRunDir + "/IOS/ibt/images/";
	
	
	/************************************************************************** 
	 * null until set.
	 * Set to the root directory where the Instruments project files are located. 	 
	 **/
	public static String ROOT_INSTRUMENTS_PROJECT_DIR = null;
	
	/************************************************************************** 
	 * null until set.
	 * Set to the active Instruments output directory: where the Instruments Automation Results 
	 * and screenshots are actively being written. 	 
	 **/
	public static String ROOT_INSTRUMENTS_OUTPUT_DIR = null;
	
	/** 
	 * null until set.  Not really used yet.
	 * Set to the Trace Template that should be loaded into Instruments upon launch. 	 
	 **************************************************************************/
	public static String DEFAULT_INSTRUMENTS_TEMPLATE = null;
	
	/** "Instruments/launchInstrumentsTemplate.scpt" <br>
	 * Normally appended to {@link #ROOT_ASCRIPTS_DIR} <br>
	 * The default script requires 1 argument for the Instruments TraceTemplate to use.*/
	public static String LAUNCH_INSTRUMENTS_ASCRIPT = "Instruments/launchInstrumentsTemplate.scpt";	
	
	/** "Instruments/startInstrumentsTrace.scpt" <br>
	 * Normally appended to {@link #ROOT_ASCRIPTS_DIR} */
	public static String START_INSTRUMENTS_ASCRIPT    = "Instruments/startInstrumentsTrace.scpt";
	
	/** "Instruments/restartInstrumentsScript.scpt" <br>
	 * Normally appended to {@link #ROOT_ASCRIPTS_DIR} */
	public static String LOOP_INSTRUMENTS_ASCRIPT   = "Instruments/restartInstrumentsScript.scpt";

	/** "Instruments/restartInstruments5Script.scpt" <br>
	 * Normally appended to {@link #ROOT_ASCRIPTS_DIR} */
	public static String LOOP_INSTRUMENTS5_ASCRIPT   = "Instruments/restartInstruments5Script.scpt";

	/** "Instruments/restartInstruments5LionScript.scpt" <br>
	 * Normally appended to {@link #ROOT_ASCRIPTS_DIR} */
	public static String LOOP_INSTRUMENTS5LION_ASCRIPT   = "Instruments/restartInstruments5LionScript.scpt";

	/** "Instruments/captureInstrumentsHotspot.scpt" <br>
	 * Normally appended to {@link #ROOT_ASCRIPTS_DIR} */
	public static String CAPTURE_INSTRUMENTS_HOTSPOT_ASCRIPT   = "Instruments/captureInstrumentsHotspot.scpt";

	/**  "editormenu" */
	public static String CAPTURE_INSTRUMENTS_HOTSPOT_EDITORMENU_MODE = "editormenu";
	
	/**  default: ROOT_ASCRIPTS_DIR  */
	public static String CAPTURE_INSTRUMENTS_HOTSPOT_DIR = ROOT_ASCRIPTS_DIR;
	
	/**  "editormenuhotspot.txt"  */
	public static String CAPTURE_INSTRUMENTS_HOTSPOT_EDITORMENU_FILE = "editormenuhotspot.txt";
	
	/** "Instruments/stopInstrumentsTrace.scpt" <br>
	 * Normally appended to {@link #ROOT_ASCRIPTS_DIR} */
	public static String STOP_INSTRUMENTS_ASCRIPT   = "Instruments/stopInstrumentsTrace.scpt";
	
	/** "startscript/StartScript.bmp" <br>
	 * Normally appended to {@link #ROOT_IBT_IMAGES_DIR} */
	public static String STARTSCRIPT_IMAGE = "startscript/StartScript.bmp";

	/** ">message&lt;" UIAutomation lower-case log key tag identifying a Message String follows. */
    public static final String MESSAGE_TAG_LC = ">message<";//MESSAGE KEY
    
	/** "&lt;string>" UIAutomation lower-case open tag for enclosed log message. */
    public static final String OPEN_STRING_LC = "<string>";//prefix for value of MESSAGE
    
	/** "&lt;/string>" UIAutomation lower-case close tag for enclosed log message. */
    public static final String CLOSE_STRING_LC = "</string>";//suffix for value of MESSAGE
    
    public static final String EXCEPTION_TAG_LC = "<string>exception raised";
    
    public static final String UNCAUGHT_ERROR_TAG_LC = "<string>script threw an uncaught javascript error:";
    
    public static final String USER_STOPPED_SCRIPT_TAG_LC = "<string>script was stopped by the user";
    
	/** ":debug:" SAFS debug message prefix (lower-case) tagging a message to be delivered to the SAFS Debug Log. */
    public static final String DEBUG_TAG_LC = ":debug:";
    
	/** ":comment:" SAFS comment message prefix (lower-case) tagging a message to be used for SAFS execution status. */
    public static final String COMMENT_TAG_LC  = ":comment:";
    
	/** ":detail:" SAFS comment details message prefix (lower-case) tagging a message to be used for SAFS execution status. */
    public static final String DETAIL_TAG_LC  = ":detail:";
    
	/** ":status:" SAFS status message prefix (lower-case) tagging a message to be used for SAFS statusCode. 
	 * Sample message: ":STATUS:-1"<br>
	 * When seen in Results log: "&lt;string>:STATUS:-1&lt;/string>" */
    public static final String STATUS_TAG_LC    = ":status:";
    
	/** ":spcout:" IOS Process Container output message prefix (lower-case) tagging a message to be used for 
	 * IOS Process Container output. */
    public static final String SPCOUT_TAG_LC  = ":spcout:";
    
	/** ":spcmap:" IOS Process Container app map output message prefix (lower-case) tagging a message to be used for 
	 * IOS Process Container App Map output. */
    public static final String SPCMAP_TAG_LC  = ":spcmap:";
    
	/** ">script completed.&lt;" UIAutomation lower-case message substring indicating a script has completed execution. */
    public static final String COMPLETE_TAG_LC =">script completed.<";//might have to be localized !!!
    
	/** ">timestamp&lt;" UIAutomation lower-case message substring indicating a date/time timestamp is next to be logged. */
    public static final String TIME_TAG_LC = ">timestamp<";//TIMESTAMP KEY

	/** "&lt;date>" UIAutomation lower-case open tag for enclosed timestamp data. */
    public static final String OPEN_DATE_LC = "<date>";//prefix for value of TIMESTAMP

	/** "&lt;/date>" UIAutomation lower-case close tag for enclosed timestamp data. */
    public static final String CLOSE_DATE_LC = "</date>";//suffix for value of TIMESTAMP
    
	/** ">type&lt;" UIAutomation lower-case message substring indicating the type or level of the log message. */
    public static final String TYPE_TAG_LC = ">type<";

	/** "&lt;integer>" UIAutomation lower-case open tag for enclosed integer data. */
    public static final String OPEN_INTEGER_LC = "<integer>";
    
	/** "&lt;/integer>" UIAutomation lower-case close tag for enclosed integer data. */
    public static final String CLOSE_INTEGER_LC = "</integer>";
    
	/** "&lt;array>" UIAutomation lower-case open tag for an array, usually of log messages. */
    public static final String OPEN_ARRAY_LC = "<array>";

	/** "&lt;/array>" UIAutomation lower-case close tag for an array, usually of log messages. */
    public static final String CLOSE_ARRAY_LC ="</array>";
    
	/** "&lt;dict>" UIAutomation lower-case close tag for the wrapper of log message data. */
    public static final String OPEN_DICT_LC ="<dict>";
    
	/** "&lt;/dict>" UIAutomation lower-case close tag for the wrapper of log message data. */
    public static final String CLOSE_DICT_LC="</dict>";
	
    /** ";\;" */
    //public static final String GUIID_CHILD_SEPARATOR = ";\;"; // won't compile!
        
    /** ";\\\\;" */
    //public static final String GUIID_REPLACE_SEPARATOR = ";\\;";
        
   /** 20. Default timeout in seconds to wait for Instruments log commencement. */
    public static int INSTRUMENTS_LAUNCH_TIMEOUT = 20;
    
    /** Used internally for reusable storage of sought IBT images. */
	private static BufferedImage startImage = null;	
    
	/** Used internally for tracking our correct position within the constantly reloaded log file. */
	private static double logline = 0;
    
	/** Used internally for tracking our correct position within the constantly reloaded log file. */
	private static double lastline = 0;
	
    /** Used internally for tracking status updates sent from the Instruments JavaScript into the log. */
	private static int runtime_status = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;

	/** Used internally for tracking status updates sent from the Instruments JavaScript into the log. */
	private static String runtime_comment = null;
    
	/** Used internally for tracking status updates sent from the Instruments JavaScript into the log. */
	private static String runtime_detail = null;
	
	/** 
	 * Used internally for writing process container object information to output file. 
	 * An external ProcessContainer-type class should set this to a valid open BufferedWriter 
	 * to capture the Instruments output into the external file.  (Re)Set it to null when it 
	 * is not in use.
	 **/
	public static BufferedWriter spcOutputWriter = null;

	/**
	 * true if object information to output file should be in XML format.
	 */
	public static boolean spcOutputXML = false;

	public static final int STATE_INIT   = -1;
	public static final int STATE_SYSTEM = 0;
	public static final int STATE_APP    = 1;
	public static final int STATE_WIN    = 2;
	public static final int STATE_CHILD  = 3;
	public static final int STATE_CHILD_COMPLETE  = 4;
	public static final int STATE_WIN_PROP    = 5;
	public static final int STATE_CHILD_PROP    = 6;
	
	/**
	 * -1: init state;<br>
	 *  0: system out state;<br>
	 *  1: app out state;<br>
	 *  2: win out state;<br>
	 *  3: child out state;<br>
	 */
	public static int xmlstate = STATE_INIT;
	public static int newobjectdepth = 0;
	public static int currentobjectdepth = 0;
	
	/**
	 * Retrieved from System.getProperty("os.version");
	 */	
	public static String OS_VERSION = "";
	public static boolean IS_OSX = false;
	public static boolean IS_LION = false;
	public static boolean IS_SNOWLEOPARD = false;
	public static int MAJOR_VERSION = 0;
	public static int MINOR_VERSION = 0;
	public static int MINOR_RELEASE_VERSION = 0;
	
	/** Used internally for appending process container App Map information to output file. 
	 * An external ProcessContainer-type class should set this to a valid open BufferedWriter 
	 * to capture the Instruments output into the external file.  (Re)Set it to null when it 
	 * is not in use.
	 **/
	public static BufferedWriter spcAppMapWriter = null;

	static{
		if(OS_VERSION.length()==0){
			OS_VERSION = System.getProperty("os.version");
			int index = OS_VERSION.indexOf(".");
			try{ MAJOR_VERSION = Integer.parseInt(OS_VERSION.substring(0, index));}catch(Exception x){}
			int subindex = OS_VERSION.lastIndexOf(".");
			try{ MINOR_VERSION = Integer.parseInt(OS_VERSION.substring(index+1, subindex));}catch(Exception x){}
			try{ MINOR_RELEASE_VERSION = Integer.parseInt(OS_VERSION.substring(subindex+1));}catch(Exception x){}
			IS_OSX = (MAJOR_VERSION == 10);
			IS_LION = (IS_OSX && (MINOR_VERSION == 7));
			IS_SNOWLEOPARD = (IS_OSX && (MINOR_VERSION == 6));
		}
	}
	
	/**
  	 * Writes to SAFS Debug "Log" at this time.
	 * Initial POC debug logger wrote to System.out.
	 * @param message
	 */
	static void debug(String message){
		// System.out.println(message);
		Log.debug(message);
	}
	
	/**
	 * Reset the collection of fields used for a single call to execute an Instruments script.<br>
	 * <br>rc reset to SCRIPT_NOT_EXECUTED.
	 * <br>comment reset to null.
	 * <br>detail reset to null. 
	 */
	static void resetIStatus(){
		runtime_status = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
		runtime_comment = null;
		runtime_detail = null;
	}
	
	/**
	 * Return the current/last IStatus object generated from execution of an Instruments Script.
	 * <p>
	 * Note: this status information is only valid and updated after a call to the waitScriptComplete methods.
	 * @return IStatus
	 * @see #waitScriptComplete(String),
	 * @see #waitScriptComplete()
	 */
	static IStatus getIStatus(){
		return new IStatus(runtime_status, runtime_comment, runtime_detail);
	}
	
	/**
	 * Copy the bytes of one file overwriting or creating a new file.
	 * Since bytes are copied, no character coding issues are expected.
	 * We do no error detection or checking here other than catching Exceptions and 
	 * logging the information to debug().
	 * 
	 * @param source File should already be known to exist
	 * @param target File should already be known to be writable.
	 */
	public static void copyFile(File source, File target){
		try{
			byte[] scriptdata = new byte[1024];
			FileInputStream in = new FileInputStream(source);
			FileOutputStream out = new FileOutputStream(target);
			int read = 0;
			while(read > -1 && in.available()>0){
				read = in.read(scriptdata);
				if(read > 0){
					out.write(scriptdata, 0, read);
				}
			}
			in.close();
			out.flush();
			out.close();
			in = null;
			out = null;
		}catch(Exception x){
			debug("Copy File Erorr "+ x.getClass().getSimpleName()+":"+ x.getMessage());
		}		
	}

	/**
	 * Write data to filename in optional targetdir. Normally a JavaScript file needed by a keyword implementation.
	 * This would normally only be done between script executions. That is AFTER a Script Complete has 
	 * been detected from the previous script execution. Or, before the Record Trace has been 
	 * initiated.
	 * 
	 * The primary execution script is static and imports JSCRIPTS_HOOK.  It is that hook that 
	 * imports the JSCRIPTS_TRD.
	 * 
	 * @param data should not be null or zero-length. The String whose contents will be written to the file.
	 * If it is JavaScript, it should be valid including any or all line terminations.
	 * 
	 * @param targetdir null or the root directory holding the primary execution script(s).
	 * If null, the routine will use {@link ROOT_JSCRIPTS_DIR}. If provided, the value should end with 
	 * a valid file separator for the platform--normally "\" or "/". 
	 * 
	 * @param filename - the name of the file to create or append to.  If the file already exists, 
	 * it will be deleted before we attempt to write unless append (below) is true.  
	 * We do not automatically add any suffix to the filename.
	 * 
	 * @param append - if true, appends to the end of file instead of deleting and overwriting the file.
	 * 
	 * @throws InstrumentsTestRecordDataException if we cannot write this file for any reason.
	 */
	public static void writeDataToFile(String data, String targetdir, String filename, boolean append)throws InstrumentsTestRecordDataException{
		if(targetdir == null || targetdir.length()==0) targetdir = ROOT_JSCRIPTS_DIR;
		if(filename == null || filename.length()==0) throw new InstrumentsTestRecordDataException("Utilities.writeDataToFile invalid filename: "+filename);
		File hookfile = new CaseInsensitiveFile(targetdir + filename).toFile();
		if(!append && hookfile.exists()) hookfile.delete();
		try{
			Log.info("writeDataToFile attempting to write new data to: "+hookfile.getPath() );
			BufferedWriter out = new BufferedWriter(new FileWriter(hookfile, append));
			out.write(data);
			out.flush();
			out.close();
		}catch(IOException x){
			String detail = "writeDataToFile error exporting "+ filename +":"+x.getClass().getSimpleName()+":"+x.getMessage();
			Log.debug(detail);
			throw new InstrumentsTestRecordDataException(detail);
		}
		hookfile = null;
	}

	/**
	 * Set the Instruments tools preferred script.
	 * 
	 * This must be done BEFORE the tool is launched. 
	 * The routine will store a copy of the current script preference file then overwrite the 
	 * original with the new preference file.
	 * The script preference file is assumed to be located at:<br>
	 * /Users/[username]/Library/Application Support/Instruments/PreviousScripts
	 * 
	 * @param sourcedir null or path to root directory for stored preferences files. 
	 * If null, the routine will use {@link #ROOT_INSTRUMENTS_DIR}.
	 * 
	 * @param sourcepreference null or alternate preference file to use for Instruments scripts.
	 * If null, the routine will use the default {@link #WRAPPER_INSTRUMENTS_SCRIPT}.
	 * 
	 * @param supportdir null or alternate directory where Instruments stores its preference file.
	 * If null, the routine will use the default {@link #INSTRUMENTS_APPLICATIONSUPPORT_DIR}.
	 * 
	 * @param supportpreference null or alternate named preference file to overwrite.
	 * If null, the routine will overwrite the default {@link #INSTRUMENTS_PREVIOUSSCRIPTS_FILE}.
	 */
	public static void presetInstrumentsScript(String sourcedir, String sourcepreference, String supportdir, String supportpreference){
		if (sourcedir == null || sourcedir.length()==0) sourcedir = ROOT_INSTRUMENTS_DIR;
		if (sourcepreference == null || sourcepreference.length()==0) sourcepreference = SAFSRUNTIME_INSTRUMENTS_PREFS;
		if (supportdir == null || supportdir.length()==0) supportdir = INSTRUMENTS_APPLICATIONSUPPORT_DIR;
		
		if(supportdir.contains(TAG_USERNAME)){
			String user = System.getProperty("user.name");
			debug("UserName substitution using System.getProperty('user.name')="+ user);
			supportdir = supportdir.replace(TAG_USERNAME, user);
		}		
		if (supportpreference == null || supportpreference.length()==0) supportpreference = INSTRUMENTS_PREVIOUSSCRIPTS_FILE;
		String source = sourcedir + sourcepreference;
		String target = supportdir + supportpreference;
		String backup = supportdir + INSTRUMENTS_BACKUPSCRIPTS_FILE;
		try{
			File backupfile = new CaseInsensitiveFile(backup).toFile();
			File targetfile = new CaseInsensitiveFile(target).toFile();
			File sourcefile = new CaseInsensitiveFile(source).toFile();
			
			if(!sourcefile.exists()){
				debug("Invalid source file for Instrument script preference: "+ source);
				return;
			}
			if (targetfile.exists()&&targetfile.isFile()) copyFile(targetfile, backupfile);			
			copyFile(sourcefile, targetfile);
		}catch(Exception x){
			debug("Could not backup or overwrite Instruments script preference files using:");
			debug("  source: "+ source);
			debug("  target: "+ target);
			debug("  backup: "+ backup);
		}
	}
	
	/** calls presetInstrumentsScript using all default parameters. 
	 * This sets Instruments to choose the default runtime wrapper script when launched. */
	public static void presetWrapperInstrumentsScript(){
		presetInstrumentsScript(null,SAFSRUNTIME_INSTRUMENTS_PREFS,null,null);
	}
	
	/** calls presetInstrumentsScript for ProcessContainer using all other defaults. 
	 * This sets Instruments to choose the ProcessContainer wrapper script when launched. */
	public static void presetProcessContainerInstrumentsScript(){
		presetInstrumentsScript(null, PROCESSCONTAINER_INSTRUMENTS_PREFS, null, null);
	}
	
	/**
	 * Restore the Instruments tools preferred script from its backup.
	 * 
	 * This must be done BEFORE the tool is launched. 
	 * The routine will restore the original script preference file from any backup  
	 * preference file.
	 * The script preference file is assumed to be located at:<br>
	 * /Users/[username]/Library/Application Support/Instruments/PreviousScripts
	 * 
	 * The backup script preference file is assumed to be located at:<br>
	 * /Users/[username]/Library/Application Support/Instruments/SAFSBackupScripts
	 * 
	 * @param supportdir null or alternate directory where Instruments stores its preference file.
	 * If null, the routine will use the default {@link #INSTRUMENTS_APPLICATIONSUPPORT_DIR}.
	 * 
	 * @param supportpreference null or alternate named preference file to overwrite.
	 * If null, the routine will restore the default {@link #INSTRUMENTS_PREVIOUSSCRIPTS_FILE}.
	 * 
	 * @param supportbackup null or alternate named backup file to restore.
	 * If null, the routine will restore from the default {@link #INSTRUMENTS_BACKUPSCRIPTS_FILE}.
	 */
	public static void restoreInstrumentsScript(String supportdir, String supportpreference, String supportbackup){
		if(supportdir == null || supportdir.length()==0) supportdir = INSTRUMENTS_APPLICATIONSUPPORT_DIR;
		
		if(supportdir.contains(TAG_USERNAME)){
			String user = System.getProperty("user.name");
			supportdir = supportdir.replace(TAG_USERNAME, user);
		}		
		if (supportpreference == null || supportpreference.length()==0) supportpreference = INSTRUMENTS_PREVIOUSSCRIPTS_FILE;
		if (supportbackup == null || supportbackup.length()==0) supportbackup = INSTRUMENTS_BACKUPSCRIPTS_FILE; 
		String target = supportdir + supportpreference;
		String backup = supportdir + supportbackup;
		try{
			File backupfile = new CaseInsensitiveFile(backup).toFile();
			File targetfile = new CaseInsensitiveFile(target).toFile();			
			if(!backupfile.exists()){
				debug("Invalid backup file for Instrument script preference: "+ backup);
				return;
			}
			copyFile(backupfile, targetfile);			
		}catch(Exception x){
			debug("Could not restore or overwrite Instruments script preference files using:");
			debug("  backup: "+ backup);
			debug("  target: "+ target);
		}		
	}
	
	/** call restoreInstrumentsScript with nulls to use defaults. */
	public static void restoreInstrumentsScript(){
		restoreInstrumentsScript(null,null,null);
	}

	/**
	 * Attempt to kill all "Instruments" processes using "killall".
	 * Should normally only be called if Instruments did not launch and execute properly, 
	 * or does not seem to be shutting down as expected.
	 */
	public static void killAllInstruments(){
		String cmd = "killall -9 Instruments";
		debug("Preparing to kill all Instruments using: "+ cmd);
		try{ 
			new Thread(new ProcessConsole(Runtime.getRuntime().exec(cmd))).start();
		}catch(Exception x){			
			debug("killAllInstruments "+ x.getClass().getSimpleName()+":"+x.getMessage());
		}
	}
	
	/**
	 * Attempt to kill all IOS "Simulator" processes using "killall".
	 * Should normally only be called if Simulator did not launch and execute properly, 
	 * or does not seem to be shutting down as expected.
	 */
	public static void killAllSimulators(){
		String cmd = "killall -9 \"iPhone Simulator\"";
		debug("Preparing to kill all iPhone Simulators using: "+ cmd);
		try{ 
			new Thread(new ProcessConsole(Runtime.getRuntime().exec(cmd))).start();
		}catch(Exception x){			
			debug("killAllSimulators "+ x.getClass().getSimpleName()+":"+x.getMessage());
		}
	}
	
	/**
	 * Launch the Instruments SDK tool with the specified template.
	 * This is a convenience routine that really only calls the runAppleScript method with a 
	 * secstimeout of 10.
	 *  
	 * This does NOT start the Record Trace.  Call {@link #startInstrumentsTest(String, String)} 
	 * to start the Record Trace--the actual execution of the test.
	 * 
	 * @param sourcedir null or the root directory containing the script. 
	 * If null, the routine will use {@link #ROOT_ASCRIPTS_DIR}.
	 * 
	 * @param script null or the script path to an alternate Instruments launch script to use in sourcedir.
	 * A null value will use the {@link #LAUNCH_INSTRUMENTS_ASCRIPT} in sourcedir.
	 * 
	 * @param template The default script expects this to be the full path to the Instruments trace template.
	 * <p>
	 * An alternate script can also use this argument, or the caller should set 
	 * this value to null if none should be used in the alternate script.
	 * 
	 * @return ProcessCapture provided by runAppleScript.
	 * 
	 * @see #runAppleScript(String, String, String, boolean, long)
	 */
	public static ProcessCapture launchInstrumentsTest(String sourcedir, String script, String template)
	                             throws IllegalThreadStateException
	{
		if(sourcedir == null || sourcedir.length()==0) sourcedir = ROOT_ASCRIPTS_DIR;
		if (script == null || script.length()==0) script = LAUNCH_INSTRUMENTS_ASCRIPT;
		return runAppleScript(sourcedir, script, template, true, Utilities.INSTRUMENTS_LAUNCH_TIMEOUT);
	}
	
	/**
	 * Run an AppleScript via osascript.
	 *  
	 * @param sourcedir null or the root directory containing the script. 
	 * If null, the routine will use {@link #ROOT_ASCRIPTS_DIR}.
	 * 
	 * @param script to use in sourcedir.
	 * A null value will use the {@link #LAUNCH_INSTRUMENTS_ASCRIPT} in sourcedir.
	 * 
	 * @param optional arguments needed by the script, or null.
	 * 
	 * @param wait true if we should wait for the launched process to finish
	 * 
	 * @param secstimeout maximum time in seconds to wait before throwing a timeout exception.
	 * If wait for secstimeout < 0 is specified, the routine will wait indefinitely (dangerous?).
	 * 
	 * @return if successful, a ProcessCapture object will return allowing the caller to process the 
	 * out and err streams of the AppleScript process.  
	 * 
	 * @throws IllegalThreadStateException if a wait with timeout >= 0 was specified and exceeded.
	 */
	public static ProcessCapture runAppleScript(String sourcedir, String script, String arguments, boolean wait, long secstimeout)
	                                 throws IllegalThreadStateException
	{
		if(sourcedir == null || sourcedir.length()==0) sourcedir = ROOT_ASCRIPTS_DIR;
		if (script == null || script.length()==0) script = LAUNCH_INSTRUMENTS_ASCRIPT;
		String cmd = "osascript "+ sourcedir + script;
		if(arguments != null && arguments.length() > 0) cmd += " "+ arguments;
		debug("Preparing to run AppleScript: "+ cmd);
		Thread athread = null;
		ProcessCapture aconsole = null;
		try{ 
			aconsole = new ProcessCapture(Runtime.getRuntime().exec(cmd));
			athread = new Thread(aconsole);
			athread.start();
			while(wait && (secstimeout != 0)){
				if (athread.isAlive()){
					try{Thread.sleep(1000);}catch(Exception x){}
					if(secstimeout > 0) secstimeout--;
				}else{
					wait = false;
				}
			}
			if(wait && secstimeout < 1 && athread.isAlive())
				throw new IllegalThreadStateException("runAppleScript TIMEOUT for: "+ cmd);
		}catch(Exception x){			
			debug("runAppleScript "+ x.getClass().getSimpleName()+":"+x.getMessage());
			throw new IllegalThreadStateException(x.getMessage());
		}
		return aconsole;
	}

	/**
	 * Launch the Instruments SDK tool with the specified template using other default parameters.
	 *  
	 * This does NOT start the Record Trace.  Call {@link #startInstrumentsTest(String, String)} 
	 * to start the Record Trace--the actual execution of the test.
	 * 
	 * @param template The default script expects this to be the full path to the Instruments trace template.
	 * <p>
	 * An alternate script can also use this argument, or the caller should set 
	 * this value to null if none should be used in the alternate script.
	 * 
	 * @return ProcessCapture returned by runAppleScript
	 * 
	 * @see #runAppleScript(String, String, String, boolean, long)
	 */
	public static ProcessCapture launchInstrumentsTest(String template)
	                                 throws IllegalThreadStateException
	{

		return launchInstrumentsTest(null, null, template);
	}
	
	/**
	 * Start the Instruments SDK tool Record Trace.
	 * 
	 *  Assumes the Instruments tool is already up and running with the appropriate template.
	 *  {@link #launchInstrumentsTest(String, String, String)}.
	 * 
	 * @param sourcedir null or the root directory containing the script. 
	 * If null, the routine will use {@link #ROOT_ASCRIPTS_DIR}.
	 * 
	 * @param script null or the script path to an alternate Instruments start script to use in sourcedir.
	 * A null value will use the {@link #START_INSTRUMENTS_ASCRIPT} in sourcedir.
	 * 
	 * @return ProcessCapture from runAppleScript
	 * 
	 * @see #runAppleScript(String, String, String, boolean, long)
	 */
	public static ProcessCapture startInstrumentsTest(String sourcedir, String script)
	                                 throws IllegalThreadStateException
	{
		if (script == null || script.length()==0) script = START_INSTRUMENTS_ASCRIPT;
		return launchInstrumentsTest(sourcedir, script, null);		
	}

	/** calls startInstrumentsTest with null to use defaults. 
	 * 
	 * @return ProcessCapture from runAppleScript
	 * 
	 * @see #runAppleScript(String, String, String, boolean, long)
	 **/
	public static ProcessCapture startInstrumentsTest()
	                                 throws IllegalThreadStateException
	{

		return startInstrumentsTest(null, null);
	}
	
	/**
	 * Copy/Rename a JavaScript snippet to JSCRIPTS_HOOK and store for the next script execution.
	 * The routine also presets empty imports trd.js and coords.js for default execution.
	 * For this reason, prepareNextTestRecordData must be called AFTER this method.
	 * This should only be done between script executions. That is AFTER a Script Complete has 
	 * been detected from the previous script execution. Or, before the Record Trace has been 
	 * initiated.
	 * 
	 * The primary execution script is static and imports the hook.  It is the hook that we 
	 * dynamically alter/copy to change what the Instruments automation will do next.
	 * 
	 * {@link #nextInstrumentsTest(String, String, String, String)}.
	 * 
	 * @param sourcedir null or the root directory containing the script. 
	 * If null, the routine will use {@link #ROOT_JSCRIPTS_DIR}. 
	 * If JSCRIPTS_ABSOLUTE_PATH is specified then the routine will assume the script parameter 
	 * is NOT relative and contains the full absolute path to the desired script.
	 * 
	 * @param script cannot be null. The script name is expected to end with extension ".js".
	 * If it does not, the routine will add the ".js" extension for convenience.
	 * 
	 * @param targetdir null or the root directory holding the primary execution script(s).
	 * If null, the routine will use {@link ROOT_JSCRIPTS_DIR}.
	 * 
	 * @see #prepareNextTestRecordData(TestRecordData, String)
	 */
	public static void prepareNextInstrumentsTest(String sourcedir, String script, String targetdir){
		boolean absolute = JSCRIPTS_ABSOLUTE_PATH.equals(sourcedir);
		if(sourcedir == null || sourcedir.length()==0) sourcedir = ROOT_JSCRIPTS_DIR;		
		if(targetdir == null || targetdir.length()==0) targetdir = ROOT_JSCRIPTS_DIR;		
		if(! script.endsWith(".js")) script = script +".js";
		File hookfile = new CaseInsensitiveFile(targetdir + JSCRIPTS_HOOK).toFile();
		if(hookfile.exists()) hookfile.delete();
		File scriptfile = null;
		if(absolute) {
			scriptfile = new CaseInsensitiveFile(script).toFile();
		}else{
			scriptfile = new CaseInsensitiveFile(sourcedir + script).toFile();
		}
		if(scriptfile.exists()) { copyFile(scriptfile, hookfile);}
		hookfile = new CaseInsensitiveFile(targetdir + JSCRIPTS_TRD).toFile();
		if(hookfile.exists()) hookfile.delete();
		try { writeDataToFile("\n", targetdir, JSCRIPTS_TRD, false); }catch (Exception e) {}		
		
		scriptfile = null;
		hookfile = null;
	}

	/** calls prepareNexInstrumentsTest with null for sourcedir and targetdir for default operation. 
	 * @see #prepareNextInstrumentsTest(String, String, String)  */
	public static void prepareNextInstrumentsTest(String script){
		prepareNextInstrumentsTest(null, script, null);
	}

	/**
	 * Copy TestRecordHelper data to JSCRIPTS_TRD and store for the next script execution.
	 * This should only be done between script executions. That is AFTER a Script Complete has 
	 * been detected from the previous script execution. Or, before the Record Trace has been 
	 * initiated.
	 * 
	 * The primary execution script is static and imports JSCRIPTS_HOOK.  It is that hook that 
	 * may or may not import the JSCRIPTS_TRD.
	 * 
	 * @param trd cannot be null. The TestRecordData whose contents will be written to JSCRIPTS_TRD.
	 * 
	 * @param targetdir null or the root directory holding the primary execution script(s).
	 * If null, the routine will use {@link ROOT_JSCRIPTS_DIR}.
	 */
	public static void prepareNextTestRecordData(TestRecordData trd, String targetdir)throws InstrumentsTestRecordDataException{
		if(targetdir == null || targetdir.length()==0) targetdir = ROOT_JSCRIPTS_DIR;
		
		String data = "// "+ JSCRIPTS_TRD +"\n";
		data += "// TestRecordData file dynamically generated by SAFS/IOS.\n";
		data += "var trd = {\n";
		
		String value = trd.getCommand();
		if(value == null) value = "";
		data += "command:'"+value+"',\n";
		
		try{value = trd.getCompClass();}catch(SAFSException x){value = null;}
		if(value == null) value = "";
		data += "compClass:'"+value+"',\n";
		
		try{value = trd.getCompGuiId();}catch(SAFSException x){value = null;}
		if(value == null) value = "";
		//value = value.replaceAll(GUIID_CHILD_SEPARATOR, GUIID_REPLACE_SEPARATOR);
		data += "compGUIID:'"+value+"',\n";
		
		value = trd.getCompName();
		if(value == null) value = "";
		data += "compName:'"+trd.getCompName()+"',\n";
		
		try{value = trd.getCompType();}catch(SAFSException x){value = null;}
		if(value == null) value = "";
		data += "compType:'"+value+"',\n";
		
		value = trd.getInputRecord();
		if(value == null) value = "";
		data += "inputRecord:'"+value+"',\n";

		value = trd.getRecordType();
		if(value == null) value = "";
		data += "recordType:'"+value+"',\n";

		value = trd.getSeparator();
		if(value == null) value = "";
		data += "separator:'"+value+"',\n";

		value = trd.getTestLevel();
		if(value == null) value = "";
		data += "testLevel:'"+value+"',\n";
		
		try{value = trd.getWindowGuiId();}catch(SAFSException x){value = null;}
		if(value == null) value = "";
		//value = value.replaceAll(GUIID_CHILD_SEPARATOR, GUIID_REPLACE_SEPARATOR);
		data += "windowGUIID:'"+value+"',\n";
		
		try{value = trd.getWindowName();}catch(SAFSException x){value = null;}
		if(value == null) value = "";
		data += "windowName:'"+value+"',\n";			

		value = trd.getAppMapName();
		if(value == null) value = "";
		data += "appMapName:'"+value+"',\n";

		value = String.valueOf(trd.getLineNumber());
		if(value == null) value = "";
		data += "lineNumber:'"+value+"',\n";

		value = trd.getFilename();
		if(value == null) value = "";
		data += "fileName:'"+value+"',\n";

		value = trd.getFac();
		if(value == null) value = "";
		data += "fac:'"+value+"'};\n";
		writeDataToFile(data, targetdir, JSCRIPTS_TRD, false);
	}

	/**
	 * Copy TestRecordHelper data to JSCRIPTS_TRD and store for the next script execution.
	 * This should only be done between script executions. That is AFTER a Script Complete has 
	 * been detected from the previous script execution. Or, before the Record Trace has been 
	 * initiated.
	 * 
	 * The primary execution script is static and imports JSCRIPTS_HOOK.  It is that hook that 
	 * may or may not import the JSCRIPTS_TRD.
	 * 
	 * @param trd cannot be null. The TestRecordData whose contents will be written to JSCRIPTS_TRD.
	 * 
	 * @param targetdir null or the root directory holding the primary execution script(s).
	 * If null, the routine will use {@link ROOT_JSCRIPTS_DIR}.
	 */
	public static void prepareNextTestRecordData(TestRecordData trd)throws InstrumentsTestRecordDataException{
		prepareNextTestRecordData(trd, null);
	}
	
	/**
	 * Activates the Instruments application and attempts to find and click the "Start Script" button.
	 * <p>
	 * Activates Instruments with AppleScript, then uses AppleScript to click the "Start Script" button. 
	 * On IOS 4 SDK this is on the main Instruments screen.  On IOS 5 this is on the Script Editor pane.
	 * <p>
	 * OS X Lion has an additional defect of not properly locating the IOS 5 PopupMenu triggers "Trace Log", 
	 * "Editor Log", and "Script".  For this scenario we will use AppleScript to derive the Point location 
	 * on the screen for these items and then use java.awt.Robot to activate the necessary menu.  
	 * 
	 * 
	 * @param sourcedir null or the root directory containing the script. 
	 * If null, the routine will use {@link #ROOT_ASCRIPTS_DIR}.
	 * 
	 * @param script null or the script path to an alternate Instruments loop script to use in sourcedir.
	 * A null value will use the {@link #LOOP_INSTRUMENTS_ASCRIPT} in sourcedir.
	 * 
	 * @return ProcessCapture from runAppleScript
	 * @throws InstrumentsStartScriptException if there is any problem with the SAFS IBT portion of clicking Instruments "Start Script".
	 * 
	 * @see #runAppleScript(String, String, String, boolean, long)
	 */
	//public static void nextInstrumentsTest(String ascriptdir, String script, String ibtimagedir, String image)throws InstrumentsStartScriptException{
	public static ProcessCapture nextInstrumentsTest(String ascriptdir, String script)
	                                 throws InstrumentsStartScriptException, IllegalThreadStateException
	{	
		if(IS_LION && script == null){
			try{ 
				File afile = new CaseInsensitiveFile(CAPTURE_INSTRUMENTS_HOTSPOT_DIR + CAPTURE_INSTRUMENTS_HOTSPOT_EDITORMENU_FILE).toFile();
				if(afile.exists()) {
					try{ afile.delete(); }catch(Exception x){}
					try{ Thread.sleep(500);}catch(Exception x){}
				}
				runAppleScript(ROOT_ASCRIPTS_DIR, CAPTURE_INSTRUMENTS_HOTSPOT_ASCRIPT, CAPTURE_INSTRUMENTS_HOTSPOT_EDITORMENU_MODE +" "+ 
																					   CAPTURE_INSTRUMENTS_HOTSPOT_DIR +" "+ 
																					   CAPTURE_INSTRUMENTS_HOTSPOT_EDITORMENU_FILE, 
																					   true, 
																					   10);
				if(afile.canRead()){
					BufferedReader reader = new BufferedReader(new FileReader(afile));
					String line;
					try{ line = reader.readLine();}catch(IOException x){
						throw new Exception("IOException reading AppleScript IOS 5 coordinates from "+ afile.getAbsolutePath());
					}
					String[] data = line.split(",");
					int xp = 0;
					int yp = 0;
					try{ 
						xp = Integer.parseInt(data[0]);
						yp = Integer.parseInt(data[1]);
					}catch(Exception x){
						throw new Exception("Invalid format for AppleScript IOS 5 coordinates from "+ afile.getAbsolutePath());
					}
					Robot.click(xp,yp);
					script =  LOOP_INSTRUMENTS5LION_ASCRIPT;
					return launchInstrumentsTest(ascriptdir, script, null);
				}else{
					throw new Exception("Did not find readable AppleScript coordinates of IOS 5 Popup Menu at "+ afile.getAbsolutePath());
				}				
			}catch(IllegalThreadStateException x){
				debug("Utilities.nextInstrumentsTest timeout detected.");
				throw x;
			}catch(Exception x){
				debug("Utilities.nextInstrumentsTest error "+ x.getClass().getSimpleName()+", "+x.getMessage());
				throw new InstrumentsStartScriptException("OS X Lion AppleScript error locating 'Start' in Instruments");
			}			
		}else{
			if (script == null || script.length()==0) script = LOOP_INSTRUMENTS5_ASCRIPT;
			return launchInstrumentsTest(ascriptdir, script, null);
		}
	}

	/**
	 * Activates the Instruments application and attempts to find and click the "Start Script" button using default settings.
	 * @throws InstrumentsStartScriptException if there is any problem with the SAFS IBT portion of clicking Instruments "Start Script".
	 */
	public static ProcessCapture nextInstrumentsTest()
	                                 throws IllegalThreadStateException,InstrumentsStartScriptException
	{
		//nextInstrumentsTest(null,null,null,null);
		return nextInstrumentsTest(null,null);
	}
	
	/**
	 * Stop the Instruments SDK tool Record Trace. 
	 * The routine assumes the Instruments tool is already running and a Record Trace was 
	 * already initiated.
	 * 
	 * @param sourcedir null or the root directory containing the script. 
	 * If null, the routine will use {@link #ROOT_ASCRIPTS_DIR}.
	 * 
	 * @param script null or the script path to an alternate Instruments stop script to use in sourcedir.
	 * A null value will use the {@link #STOP_INSTRUMENTS_ASCRIPT} in sourcedir.
	 */
	public static ProcessCapture stopInstrumentsTest(String sourcedir, String script) 
	                                 throws IllegalThreadStateException
	{
		if (script == null || script.length()==0) script = STOP_INSTRUMENTS_ASCRIPT;
		return launchInstrumentsTest(sourcedir, script, null);		
	}
	
	/** calls stopInstrumentsTest with nulls to use defaults. */
	public static ProcessCapture stopInstrumentsTest() 
	                                 throws IllegalThreadStateException
	{
		return stopInstrumentsTest(null, null);
	}
	
	/**
	 * Used internally when deleting and opening Instruments active output.
	 * This filter matches on Directory.startsWith("Run ").
	 */
	public static class RunDirFilter implements FileFilter{
		static final String runpath = "Run ";
		public boolean accept(File pathname){ 
			if (pathname.isDirectory()){
				//debug("RunFilter evaluatinf directory: "+pathname.getName());
				return pathname.getName().startsWith(runpath);
			}			
			return false;
		}
	}
	
	/**
	 * Used internally when seeking Instruments active output.
	 * This filter matches on filename.equals("Automation Results.plist").
	 */
	public static class ResultsFileFilter implements FileFilter{
		static final String resultsname = "Automation Results.plist";
		public boolean accept(File pathname){
			return pathname.getName().equals(resultsname);
		}
	}
	
	/**
	 * Delete all previous Instruments test output results files and directories.
	 * Essentially, delete all the "Automation Results" and "Run" directories from previous runs.
	 * 
	 * @param rootRunDir the full path to the Instruments project space where the Run subdirectories 
	 * are located.  If null, the routine will use {@link Utilities#ROOT_INSTRUMENTS_PROJECT_DIR}.  
	 * However, that default is null unless set by other means.
	 */
	public static void deletePreviousRuns(String rootRunDir){
	    File[] runFiles = null;
	    File aDir = null;
	    File[] resultsFiles = null;
	    
	    if(rootRunDir == null || rootRunDir.length()==0) rootRunDir = ROOT_INSTRUMENTS_PROJECT_DIR;
	    if(rootRunDir == null){
	    	debug("Root Instruments Project Dir has not been set. Cannot locate automation output.");
	    	return;
	    }
	    CaseInsensitiveFile rootRunFiles = new CaseInsensitiveFile(rootRunDir);
	    
    	runFiles = rootRunFiles.listFiles(new RunDirFilter());
	    if(runFiles == null || runFiles.length == 0) return;
	    
	    //determine the latest Run directory, if more than one exists
	    for(int i = 0;i < runFiles.length;i++){
	    	aDir = runFiles[i];
	    	resultsFiles = aDir.listFiles();// all files, not just results files
	        if(resultsFiles != null){
	        	for(int j = 0; j < resultsFiles.length;j++) {
	        		try{
	        			resultsFiles[j].delete();
	        		}catch(Exception x){debug("previous run results file deletion may have failed.");}	        	
	        	}
	        }
	        try{aDir.delete();}catch(Exception x){debug("previous Run directory deletion may have failed.");}
	    }	    
	}
	
	/** calls deletePreviousRun with null for default operation.*/
	public static void deletePreviousRuns(){
		deletePreviousRuns(null);
	}
	
	/**
	 * Convenience routine to optionally change the Instruments Project, delete all 
	 * logs from previous runs, reset the Instruments log line counter, and preset 
	 * the correct Instruments "recent" scripts for a test automation run.
	 * 
	 * @param projectpath if not null, overwrites ROOT_INSTRUMENTS_PROJECT_DIR with 
	 * a new path to an Instruments Project.
	 * @see #ROOT_INSTRUMENTS_PROJECT_DIR, 
	 * @see #deletePreviousRuns(),
	 * @see #resetInstrumentsLogCounter(),
	 * @see #presetWrapperInstrumentsScript()
	 */
	public static void prepareNewAutomationTest(String projectpath){
		
		if (projectpath != null) ROOT_INSTRUMENTS_PROJECT_DIR = projectpath;
		deletePreviousRuns();
		resetInstrumentsLogCounter();

		presetWrapperInstrumentsScript();		
	}
	
	/**
	 * Convenience routine to delete all 
	 * logs from previous runs, reset the Instruments log line counter, and preset 
	 * the correct Instruments "recent" scripts for a test automation run.
	 * 
	 * @see #deletePreviousRuns(),
	 * @see #resetInstrumentsLogCounter(),
	 * @see #presetWrapperInstrumentsScript()
	 */
	public static void prepareNewAutomationTest(){
		prepareNewAutomationTest(null);
	}

	/**
	 * reset our Instruments log parser to monitor the beginning of the log.
	 */
	public static void resetInstrumentsLogCounter(){
		lastline = 0;
	}

	/**
	 * Locate and return a File reference to the most recent Results file from an active Instruments 
	 * Script.  This assumes older files were previously deleted.
	 * @param rootRunDir Full path to Instruments Project space where "Run" output 
	 * directories are written.  If null, the routine will use {@link Utilities#ROOT_INSTRUMENTS_PROJECT_DIR}.  
	 * However, that default is null unless set by other means.
	 * @return File to the running results file, or an InstrumentsStartScriptException
	 * @throws InstrumentsStartScriptException if we could not locate a Run directory or active output file.
	 */

	public static File verifyInstrumentsRecording(String rootRunDir) throws InstrumentsStartScriptException{
		//debug("\nExperiments checking file system...");
	    if(rootRunDir == null || rootRunDir.length()==0) rootRunDir = ROOT_INSTRUMENTS_PROJECT_DIR;
	    if(rootRunDir == null){
	    	debug("Root Instruments Project Dir has not been set. Cannot locate automation output.");
	    	throw new InstrumentsStartScriptException("Root Instruments Project Dir has not been set. Cannot locate automation output.");
	    }
	    debug("Monitoring "+ rootRunDir +" for results file.");
	    CaseInsensitiveFile rootRunFiles = new CaseInsensitiveFile(rootRunDir);
	    //debug("Root Run Dir: "+ x.rootRunDir);	  
	    //debug("absolutePath: "+ x.rootRunFiles.getAbsolutePath());
	    //debug(" isDirectory: "+ x.rootRunFiles.isDirectory());
	    File[] runFiles = null;
	    File latestDir = null;	    
	    File[] resultsFiles = null;
	    File   results  = null;
	    File latestResults = null;
	    int loop = 0;

	    do{
	    	ROOT_INSTRUMENTS_OUTPUT_DIR = null;
	    	runFiles = null;
	    	resultsFiles = null;
	    	latestDir = null;
	    	latestResults = null;
	    	
		    // loop until we detect ANY Run directory, if none exists wait up until timeout
	    	runFiles = rootRunFiles.listFiles(new RunDirFilter());
	    	if(runFiles!=null&&runFiles.length>0){
			    //debug("Found "+runFiles.length +" RUN directories.");	    
			    //determine the latest Run directory, if more than one exists
			    for(int i = 0;i < runFiles.length;i++){
			    	File aDir = runFiles[i];
			    	if (latestDir != null){
			    		if(latestDir.lastModified() < aDir.lastModified()){
			    			latestDir = aDir;
			    		}
			    	}else{
			    		latestDir = aDir;
			    	}
			    }	    
			    // we now have the latest Run N directory in latestDir
			    ROOT_INSTRUMENTS_OUTPUT_DIR = latestDir.getAbsolutePath();			    
			    //debug("Latest Run Directory is: "+ latestDir.getName());
			    
			    //get the "Automation Results.plist" file(s) (should be only 1)
			    // might have to wait for it to exist
		    	resultsFiles = latestDir.listFiles(new ResultsFileFilter());
		    	if(resultsFiles!=null && resultsFiles.length>0){
		    	    debug("Found "+resultsFiles.length +" RESULTS file(s).");
				    for(int i = 0;i < resultsFiles.length;i++){
				    	File aDir = resultsFiles[i];
				    	if (latestResults != null){
				    		if(latestResults.lastModified() < aDir.lastModified()){
				    			latestResults = aDir;
				    		}
				    	}else{
				    		latestResults = aDir;
				    	}
				    }	    
		    	}
	    	}
	    	if (latestResults == null) try{Thread.sleep(1000);}catch(Exception x){}
	    }while((latestResults==null)&&(++loop < INSTRUMENTS_LAUNCH_TIMEOUT));
	    if(loop == INSTRUMENTS_LAUNCH_TIMEOUT){
	    	ROOT_INSTRUMENTS_OUTPUT_DIR = null;
	    	if(runFiles==null || runFiles.length==0){
		    	debug("INSTRUMENTS LAUNCH TIMEOUT reached.  No RUN directory found in timeout period.");
		    	throw new InstrumentsStartScriptException("No RUN directory detected in timeout period.");
	    	}
	    	debug("INSTRUMENTS LAUNCH TIMEOUT reached.  No RESULTS file found in timeout period.");
	    	throw new InstrumentsStartScriptException("No RESULTS file detected in timeout period.");
	    }
	    return latestResults;	    
	}
	
	/**
	 * calls closeXMLState prior to its own output.
	 * output whatever is required to start the current xml node for the provided xml state.
	 */
	protected static void openXMLState(int state) throws IOException{
		if(spcOutputWriter != null){
			//closeXMLState(state);
			if(state==STATE_SYSTEM){
				spcOutputWriter.write("<system ");
			}else if(state==STATE_APP){
				spcOutputWriter.write(" />");
				spcOutputWriter.newLine();
				spcOutputWriter.write("<application ");
			}else if(state==STATE_WIN){	
				if(xmlstate==STATE_APP){
					spcOutputWriter.write(" >");
				}else if(xmlstate==STATE_WIN){
					spcOutputWriter.write(" />");
				}else{
					spcOutputWriter.write("</window>");
				}
				spcOutputWriter.newLine();
				spcOutputWriter.write("<window ");				
			}else if(state==STATE_CHILD){
				if((xmlstate==STATE_WIN)||(xmlstate==STATE_CHILD)){
					spcOutputWriter.write(" >");
					spcOutputWriter.newLine();
				}
				spcOutputWriter.write("<child ");				
			}else if((state==STATE_WIN_PROP)||(state==STATE_CHILD_PROP)){
				if((xmlstate!=STATE_WIN_PROP)&&(xmlstate!=STATE_CHILD_PROP)){
					spcOutputWriter.write(" >");
					spcOutputWriter.newLine();
				}
				spcOutputWriter.write("<property ");
			}
			xmlstate=state;
		}
	}
	/**
	 * output whatever is required to close the current xml for the current xml state.
	 */
	protected static void closeXMLState(int toState) throws IOException{
		if(spcOutputWriter != null){
			if(toState == STATE_INIT){
				if(xmlstate==STATE_CHILD_COMPLETE){
					spcOutputWriter.write("</window></application></object>");
					spcOutputWriter.newLine();
				}else if(xmlstate==STATE_WIN){
						spcOutputWriter.write(" /></application></object>");
						spcOutputWriter.newLine();
				}else if(xmlstate==STATE_WIN_PROP){
					spcOutputWriter.write("</window></application></object>");
					spcOutputWriter.newLine();
				}else if(xmlstate==STATE_APP){
					spcOutputWriter.write(" /></object>");
					spcOutputWriter.newLine();
				}else{
					spcOutputWriter.write("</object>");
					spcOutputWriter.newLine();
				}
			}else if((xmlstate==STATE_SYSTEM)||(xmlstate == STATE_CHILD)){
				spcOutputWriter.write(" />");
				spcOutputWriter.newLine();
			}else if((xmlstate == STATE_CHILD_COMPLETE)||(xmlstate==STATE_CHILD_PROP)){
				spcOutputWriter.write("</child>");
				spcOutputWriter.newLine();
			}else if((xmlstate == STATE_WIN)||(xmlstate==STATE_WIN_PROP)){
				spcOutputWriter.write("</window>");
				spcOutputWriter.newLine();
			}else if(xmlstate == STATE_APP){
				spcOutputWriter.write("</application>");
				spcOutputWriter.newLine();
			}
			xmlstate=toState;
		}
	}
	/**
	 * Assumes a node has already been started.
	 * Parses lines as: "ignore attributename: value". <br>
	 * Ex: "Window Class:UIAWindow",<br>
	 * Ex: "Application Name:UICatalog"
	 * 
	 * @param msgtext
	 */
	protected static void addXMLAttribute(String msgtext) throws IOException{
		if(spcOutputWriter != null){
			try{
				String[] split = msgtext.split(":", 2);
				String left = split[0].trim();
				String attribvalue = split[1].trim();
				split = left.split(" ");
				String attribname = split[split.length-1].trim();
				if(attribname.equalsIgnoreCase("toString")) return;
				spcOutputWriter.write(attribname +"=");
				String q = attribvalue.indexOf("\"") > -1 ? "'":"\"";
				spcOutputWriter.write(q + attribvalue + q +" ");
			}catch(PatternSyntaxException p){
				Log.debug("IOSPC XML attribute output "+ p.getClass().getSimpleName() +": "+ p.getMessage());
			}catch(IndexOutOfBoundsException p){
				Log.debug("IOSPC XML attribute output "+ p.getClass().getSimpleName() +": "+ p.getMessage());				
			}
		}
	}

	/**
	 * Assumes a property node has already been started.
	 * Parses lines as: "property: 'name' == 'value'. <br>
	 * 
	 * @param msgtext
	 */
	protected static void addXMLProperty(String msgtext) throws IOException{
		if(spcOutputWriter != null){
			try{
				String[] split = msgtext.split("'", 5);
				String attribvalue = split[3].trim();
				String attribname = split[1].trim();
				spcOutputWriter.write(attribname +"=");
				String q = attribvalue.indexOf("\"") > -1 ? "'":"\"";
				spcOutputWriter.write(q + attribvalue + q +" />");
				spcOutputWriter.newLine();
			}catch(PatternSyntaxException p){
				Log.debug("IOSPC XML property output "+ p.getClass().getSimpleName() +": "+ p.getMessage());
			}catch(IndexOutOfBoundsException p){
				Log.debug("IOSPC XML property output "+ p.getClass().getSimpleName() +": "+ p.getMessage());				
			}
		}
	}
	
	// used for Process Container output
	static String indent = "       ";
	protected static String makePCIndent(){
		if (newobjectdepth < 1) return "";
		while((newobjectdepth * 4) > indent.length()){ indent = indent.concat(indent);}
		return indent.substring(0, newobjectdepth*4);
	}	

	/**
	 * Process the assumed to be running Instruments Log file to identify the last valid Script Completed message.
	 * This ensures the system is ready to start acting on the NEXT output messages.
	 * @param rootRunDir
	 * @throws InstrumentsLaunchFailureException
	 */
	public static void findRunningLastLine(String rootRunDir) throws InstrumentsLaunchFailureException {
	    File   results  = null;
	    if(rootRunDir == null || rootRunDir.length()==0) rootRunDir = ROOT_INSTRUMENTS_PROJECT_DIR;
	    if(rootRunDir == null){
	    	debug("Root Instruments Project Dir has not been set. Cannot locate automation output.");
	    	throw new InstrumentsLaunchFailureException("Root Instruments Project Dir has not been set. Cannot locate automation output.");
	    }
	    try{ results = verifyInstrumentsRecording(rootRunDir); }catch(InstrumentsStartScriptException x){}
	    if (results == null) throw new InstrumentsLaunchFailureException("No RESULTS file detected in timeout period.");
	    resetInstrumentsLogCounter();
	    BufferedReader reader = null;
	    String line = null;
	    String lcline = null;
	    boolean inArray = false;
	    try{
	    	reader = new BufferedReader(new FileReader(results));
	    	logline = 0;
	    	while(reader.ready()){
    			try{ 
    				line = reader.readLine();
    				if(!(line==null)){
	    				lcline = line.toLowerCase().trim();
	    				if(!inArray){
	    					if(lcline.startsWith(OPEN_ARRAY_LC)) inArray=true;
	    				}else{
		    				if(lcline.startsWith(OPEN_DICT_LC)) {
		    					logline++;
		    				}
	    				}
    				}
    			}catch(Exception x){
    				 throw new InstrumentsLaunchFailureException("RESULTS file processing error seeking last Script Completed.");	    				
    			}
	    	}	    	
	    }catch(Exception x){
			 throw new InstrumentsLaunchFailureException("No success opening or processing RESULTS file.");	    				
	    }
	    try{ reader.close();}catch(Exception x){}
	    results = null;
	    lastline = logline;
	}
	
	/**
	 * Repetitively monitor the active "Automation Results" file for "Script Complete" during Instruments script execution.
	 * The call generally follows the call to startInstrumentsTest or nextInstrumentsTest. 
	 * The call is also automatically invoked inside the convenience routine executeNextInstrumentsTest.
	 * 
	 * Due to the nature of the output file, the routine must repetitively open new instances 
	 * of the output file as the system provides temporary snapshots.
	 * <p>
	 * The routine can handle multiple script executions within a single "Record Trace" session 
	 * by keeping track of which "script complete" is the newest.
	 * <p>
	 * While monitoring the log this routine intercepts Instruments log messages properly tagged for 
	 * SAFS Debug logging. It also captures SAFS status, log comments, and log details when 
	 * properly tagged for SAFS.  
	 * <p>
	 * Message prefixes:
	 * <p><ul>
	 * <li>":DEBUG:" for a SAFS Debug log message
	 * <li>":STATUS:" for SAFS integer return code to be used by SAFS Engine
	 * <li>":COMMENT:" for SAFS String comment/warning/error to log
	 * <li>":DETAIL:" for SAFS String detail to log with the comment
	 * <li>":SPCOUT:" for Process Container Object info output
	 * <li>":SPCMAP:" for Process Container App Map info output
	 * </ul>
	 * <p>
	 * @param rootRunDir Full path to Instruments Project space where "Run" output 
	 * directories are written.  If null, the routine will use {@link Utilities#ROOT_INSTRUMENTS_PROJECT_DIR}.  
	 * However, that default is null unless set by other means.
	 * @throws InstrumentsLaunchFailureException
	 */
	public static void waitScriptComplete(String rootRunDir) throws InstrumentsLaunchFailureException {
	    File   results  = null;
	    try{ results = verifyInstrumentsRecording(rootRunDir); }catch(InstrumentsStartScriptException x){}
	    if (results == null) throw new InstrumentsLaunchFailureException("No RESULTS file detected in timeout period.");
	    
	    BufferedReader reader = null;
	    boolean testing = true;
	    boolean ready = false;	    
	    String line = null;
	    String lcrawline = null;
	    String lcline = null;
	    String lcmsgtext = null;
	    boolean inDict    = false;
	    boolean inArray   = false;
	    boolean inMessage = false;
	    boolean inType    = false;
	    boolean inTime    = false;
	    String  msgprefix = "";
	    	    
	    try{
	    	reader = new BufferedReader(new FileReader(results));
	    	logline = 0;
	    	while(testing){
	    		try{ 
	    			ready=reader.ready();}
	    		catch(IOException io){
	    			debug("***** Ignoring reader.ready: "+ io.getMessage());}
	    		while(ready){
	    			try{ 
	    				line = reader.readLine();
	    				lcrawline = line.toLowerCase();
	    				lcline = lcrawline.trim();
	    				if(!inArray){
	    					if(lcline.startsWith(OPEN_ARRAY_LC)) inArray=true;
	    				}else{
		    				if(lcline.startsWith(OPEN_DICT_LC)) {
		    					logline++;
		    					inDict = true;
		    					inMessage = false;
		    					inTime=false;
		    					inType=false;
		    				}
		    				if (inDict){
		    					if (logline > lastline){		    				
				    				//debug(" LOG: "+ line);		    						
		    						if(inMessage){
		    							String msgtext = null;
		    							if( lcline.contains(COMPLETE_TAG_LC)){
		    								testing = false;
		    								lastline = logline;
		    								Log.info("Instruments Script Complete detected.");
		    							}//attempt to intercept messages meant for the SAFS Debug Log
		    							else if (lcline.contains(DEBUG_TAG_LC)){
		    								try{
		    									msgtext=line.substring(lcrawline.indexOf(DEBUG_TAG_LC) + DEBUG_TAG_LC.length());
		    									msgtext = msgtext.substring(0, msgtext.toLowerCase().indexOf(CLOSE_STRING_LC));
		    									Log.debug(msgtext);
		    								}
		    								catch(Exception x){
		    									Log.debug("waitScriptComplete "+ x.getClass().getSimpleName()+" for INVALID or INCOMPLETE SAFS DEBUG string from JavaScript: "+ lcline);
		    								}
		    							}
		    							else if (lcline.contains(COMMENT_TAG_LC)){
		    								try{
		    									msgtext=line.substring(lcrawline.indexOf(COMMENT_TAG_LC)+ COMMENT_TAG_LC.length());
		    									msgtext = msgtext.substring(0, msgtext.toLowerCase().indexOf(CLOSE_STRING_LC));
		    									runtime_comment = msgtext;
		    									Log.info("waitScriptComplete successfully captured SAFS Comment: "+ msgtext);
		    								}
		    								catch(Exception x){
		    									Log.debug("waitScriptComplete "+ x.getClass().getSimpleName()+" for INVALID or INCOMPLETE SAFS COMMENT string from JavaScript: "+ lcline);
		    								}
		    							}
		    							else if (lcline.contains(DETAIL_TAG_LC)){
		    								try{
		    									msgtext=line.substring(lcrawline.indexOf(DETAIL_TAG_LC)+ DETAIL_TAG_LC.length());
		    									msgtext = msgtext.substring(0, msgtext.toLowerCase().indexOf(CLOSE_STRING_LC));
		    									runtime_detail = msgtext;
		    									Log.info("waitScriptComplete successfully captured SAFS Detail: "+ msgtext);
		    								}
		    								catch(Exception x){
		    									Log.debug("waitScriptComplete "+ x.getClass().getSimpleName()+" for INVALID or INCOMPLETE SAFS DETAIL string from JavaScript: "+ lcline);
		    								}
		    							}
		    							else if (lcline.contains(STATUS_TAG_LC)){
		    								try{
		    									msgtext = line.substring(lcrawline.indexOf(STATUS_TAG_LC) + STATUS_TAG_LC.length());
		    									msgtext = msgtext.substring(0, msgtext.toLowerCase().indexOf(CLOSE_STRING_LC));
		    									runtime_status = Integer.parseInt(msgtext);
		    									Log.info("waitScriptComplete successfully captured SAFS Status: "+ msgtext);
		    								}
		    								catch(Exception x){
		    									Log.debug("waitScriptComplete "+ x.getClass().getSimpleName()+" for INVALID or INCOMPLETE SAFS STATUS from JavaScript: "+ lcline);
		    								}
		    							}else if (lcline.contains(EXCEPTION_TAG_LC)){
		    								try{
		    									msgtext = line.substring(lcrawline.indexOf(OPEN_STRING_LC) + OPEN_STRING_LC.length());
		    									if(lcrawline.contains(CLOSE_STRING_LC)){
		    										msgtext = msgtext.substring(0, msgtext.toLowerCase().indexOf(CLOSE_STRING_LC));
		    									}
		    									Log.debug("JavaScript "+ msgtext);
		    								}
		    								catch(Exception x){
		    									Log.debug("waitScriptComplete "+ x.getClass().getSimpleName()+" for INVALID or INCOMPLETE Exception from JavaScript: "+ lcline);
		    								}
		    							}else if (lcline.contains(UNCAUGHT_ERROR_TAG_LC)){
		    								testing = false;
		    								lastline = logline;
		    								try{
		    									msgtext = line.substring(lcrawline.indexOf(OPEN_STRING_LC) + OPEN_STRING_LC.length());
		    									if(lcrawline.contains(CLOSE_STRING_LC)){
		    										msgtext = msgtext.substring(0, msgtext.toLowerCase().indexOf(CLOSE_STRING_LC));
		    									}
			    								runtime_status = StatusCodes.GENERAL_SCRIPT_FAILURE;
			    								runtime_comment = msgtext;
			    								runtime_detail = msgtext;
		    									Log.debug("JavaScript "+ msgtext);
		    								}
		    								catch(Exception x){
		    									Log.debug("waitScriptComplete "+ x.getClass().getSimpleName()+" for INVALID or INCOMPLETE Exception from JavaScript: "+ lcline);
		    								}
		    							}else if (lcline.contains(USER_STOPPED_SCRIPT_TAG_LC)){
		    								testing = false;
		    								lastline = logline;
		    								try{
		    									msgtext = line.substring(lcrawline.indexOf(OPEN_STRING_LC) + OPEN_STRING_LC.length());
		    									if(lcrawline.contains(CLOSE_STRING_LC)){
		    										msgtext = msgtext.substring(0, msgtext.toLowerCase().indexOf(CLOSE_STRING_LC));
		    									}
			    								runtime_status = StatusCodes.GENERAL_SCRIPT_FAILURE;
			    								runtime_comment = msgtext;
			    								runtime_detail = msgtext;
		    									Log.debug("JavaScript "+ msgtext);
		    								}
		    								catch(Exception x){
		    									Log.debug("waitScriptComplete "+ x.getClass().getSimpleName()+" for INVALID or INCOMPLETE Exception from JavaScript: "+ lcline);
		    								}
		    							}else if (lcline.contains(SPCOUT_TAG_LC)){
		    								try{
		    									msgtext=line.substring(lcrawline.indexOf(SPCOUT_TAG_LC)+ SPCOUT_TAG_LC.length());
		    									msgtext = msgtext.substring(0, msgtext.toLowerCase().indexOf(CLOSE_STRING_LC));
		    									Log.info("SPCOUT: "+ msgtext);
		    									if(spcOutputWriter != null){
		    										try{
	    												lcmsgtext = msgtext.toLowerCase().trim();
    												    if(lcmsgtext.startsWith("application ")){
    														newobjectdepth = 0;
    												    }else if(lcmsgtext.startsWith("window ")){
    														newobjectdepth=1;
    												    }else if(lcmsgtext.startsWith("property:")){
    												    	msgprefix = "    ";
    												    }else if(lcmsgtext.indexOf(" hierarchy depth:") > 0){
															//parse Depth: N
															try{
																newobjectdepth = Integer.parseInt(lcmsgtext.split(":")[1].trim());
															}catch(Exception x){ }
    												    }else if(lcmsgtext.contains("child processing complete")){
    												    	newobjectdepth--;
    												    	msgprefix = "    ";
    												    }
		    											if(spcOutputXML){		    												
		    												if(lcmsgtext.length()==0){
		    													// do nothing with blank line
		    												}else if(lcmsgtext.startsWith("application ")){
		    													if(xmlstate != STATE_APP){
		    														openXMLState(STATE_APP);
		    													}
		    													addXMLAttribute(msgtext);		    													
		    												}else if(lcmsgtext.startsWith("window ")){
		    													if(xmlstate != STATE_WIN){
		    			    										openXMLState(STATE_WIN);
		    													}
		    													addXMLAttribute(msgtext);		    													
		    												}else if(lcmsgtext.startsWith("child ")){
		    													if(lcmsgtext.contains("processing complete")){
		    														closeXMLState(STATE_CHILD_COMPLETE);
		    													}else{
		    														if(lcmsgtext.indexOf(" hierarchy depth:") > 0){
		    															openXMLState(STATE_CHILD);
		    														}
		    														addXMLAttribute(msgtext);
		    													}
		    												}else if(lcmsgtext.startsWith("property:")){
		    													if((xmlstate==STATE_WIN)||(xmlstate==STATE_WIN_PROP)) {
		    														openXMLState(STATE_WIN_PROP);
		    													}else if((xmlstate==STATE_CHILD)||(xmlstate==STATE_CHILD_PROP)) {
		    														openXMLState(STATE_CHILD_PROP);
		    													}
		    													addXMLProperty(msgtext);		    													
		    												}else{ //assume System
		    													if(xmlstate != STATE_SYSTEM){
		    														newobjectdepth=0;
		    														openXMLState(STATE_SYSTEM);
		    													}
		    													addXMLAttribute(msgtext);
		    												}
		    											//not XML output
		    											}else{
			    											spcOutputWriter.write(makePCIndent().concat(msgprefix).concat(msgtext));
			    											spcOutputWriter.newLine();
			    											msgprefix = "";
			    										}
		    										}catch(IOException x){
		    											Log.debug("SPCOUT "+ x.getClass().getSimpleName()+ ": "+x.getMessage());
		    										}
		    									}
		    								}
		    								catch(Exception x){
		    									Log.debug("waitScriptComplete "+ x.getClass().getSimpleName()+" for INVALID or INCOMPLETE Exception from JavaScript: "+ lcline);
		    								}
		    							}else if (lcline.contains(SPCMAP_TAG_LC)){
		    								try{
		    									msgtext=line.substring(lcrawline.indexOf(SPCMAP_TAG_LC)+ SPCMAP_TAG_LC.length());
		    									msgtext = msgtext.substring(0, msgtext.toLowerCase().indexOf(CLOSE_STRING_LC));
		    									Log.info("SPCMAP: "+ msgtext);
		    									if(spcAppMapWriter != null){
		    										try{
		    											spcAppMapWriter.write(msgtext);
		    											spcAppMapWriter.newLine();
		    										}catch(IOException x){
		    											Log.debug("SPCMAP "+ x.getClass().getSimpleName()+ ": "+x.getMessage());
		    										}
		    									}
		    								}
		    								catch(Exception x){
		    									Log.debug("waitScriptComplete "+ x.getClass().getSimpleName()+" for INVALID or INCOMPLETE Exception from JavaScript: "+ lcline);
		    								}
				    					}
		    							inMessage = false;
				    				}
		    						else if(inTime){
				    					//do nothing yet
				    					inTime = false;
				    				}
				    				else if(inType){
				    					//do nothing yet
				    					inType = false;
				    				}
				    				else{				    					
				    					if(lcline.contains(TIME_TAG_LC)) {
				    						inTime = true;
				    					}
				    					else if(lcline.contains(TYPE_TAG_LC)) {
				    						inType = true;
				    					}
				    					else if(lcline.contains(MESSAGE_TAG_LC)) {
				    						inMessage = true;
				    					}
				    				}
		    					}else if(! testing){ //finish the script complete Dict block
					    			//debug(" LOG: "+ line);		    						
				    				if(inTime){
				    					//do nothing yet
				    					inTime = false;
				    				}else if(inType){
				    					//do nothing yet
				    					inType = false;
				    				}else{				    				
				    					if(lcline.contains(TIME_TAG_LC)) {
				    						inTime = true;
				    					}
				    					else if(lcline.contains(TYPE_TAG_LC)) {
				    						inType = true;
				    					}
				    				}
		    					}
			    				if(lcline.startsWith(CLOSE_DICT_LC)){ 
			    					inDict=false;
			    					inMessage=false;
			    					inTime=false;
			    					inType=false;
		    					}
		    				}
		    				else if(lcline.startsWith(CLOSE_ARRAY_LC)){
	    						inArray=false;
	    					}
	    				}
	    				ready = reader.ready();
	    				
	    			}catch(IOException io){
		    			debug("***** Ignoring reader exception: "+ io.getClass().getSimpleName() +": "+ 
		    					io.getMessage());	    				
	    			}
	    		}
	    		// we are NOT ready.  File might be unfinished snapshot because we are looking at it.
	    		if(testing) {
	    			//Instruments might have closed the log because we are reading it.
	    			//If so, we have to close the one we have and get a handle to the new one.
	    			if( line != null && line.startsWith("</plist>")){
		    			try{
		    				lastline = logline;
		    				logline = 0;
		    				reader.close();
		    				reader = null;
			    			try{Thread.sleep(400);}catch(Exception s){}	    				
		    				reader = new BufferedReader(new FileReader(results));
		    			}catch(IOException io){
		    				
		    			}
		    		//could just be a slow log writing cycle.	
	    			}else{
	        			try{Thread.sleep(500);}catch(Exception s){}	    					    				
	    			}
	    		}
	    	}	    	
	    }catch(FileNotFoundException e){
			debug("***** "+ e.getMessage());	    	
	    }
	    //debug("Experiments shutting down...");
	}

	/** 
	 * calls waitScriptComplete with null to use preset ROOT_INSTRUMENTS_PROJECT_DIR. 
	 * @throws InstrumentsLaunchFailureException
	 * @see #waitScriptComplete(String) 
	 */
	public static void waitScriptComplete() throws InstrumentsLaunchFailureException {
		waitScriptComplete(null);
	}
	
	/** calls the following routines using all defaults except the varying script to run:
	 * <code>
	 * prepareNextInstruments(script);
	 * nextInstrumentsTest();
	 * waitScriptComplete();
	 * </code>
	 * The routine expects that Instruments is already running and Record Trace has already 
	 * been started.
	 * 
	 * @param script the script to run using all other defaults.
	 * @throws Exception
	 */
	public static void executeNextInstrumentsTest(String script) throws InstrumentsLaunchFailureException{
		prepareNextInstrumentsTest(script);
		try{
			nextInstrumentsTest();			
		}catch(InstrumentsStartScriptException x){ 
			throw new InstrumentsLaunchFailureException(x.getMessage());}		
		waitScriptComplete();		
	}
	
	/**
	 * Original POC execution.  May not be up-to-date or maintained.
	 * @param args None
	 * @author Carl Nagle JULY 2011
	 */
	public static void main(String[] args) {
		//runExperiments();
			prepareNewAutomationTest("/UICatalog/UICatalog");

			launchInstrumentsTest("/UICatalog/UICatalogInstruments.tracetemplate");
			
			// set the do-nothing startup script
			prepareNextInstrumentsTest(DEFAULT_JSSTARTUP_IMPORT);			
			startInstrumentsTest();
			
			try {
				waitScriptComplete();
				
				executeNextInstrumentsTest("CheckBox/Check");
				//prepareNextInstrumentsTest("CheckBox/Check");
				//nextInstrumentsTest();
				//waitScriptComplete();

				executeNextInstrumentsTest("GenericObject/RightClick");
				executeNextInstrumentsTest("CheckBox/UnCheck");
			} catch (InstrumentsLaunchFailureException e) {
				debug("Instruments execution error: "+ e.getMessage());
			}

			stopInstrumentsTest();
			restoreInstrumentsScript();
			
	}

	/**
	 * Outputs
	 * @param targetdir
	 * @param doChildren
	 * @param doProperties
	 * @param appendMap
	 * @param addInfo
	 * @param doShortStrings
	 * @param windowRec
	 * @param objectRec
	 */
	public static void prepareNextProcessContainerData(String targetdir, boolean doChildren,
			boolean doProperties, boolean appendMap, boolean addInfo, boolean doShortStrings,
			String windowName, String windowRec, String objectRec) throws InstrumentsTestRecordDataException {

		if(targetdir == null || targetdir.length()==0) targetdir = ROOT_JSCRIPTS_DIR;
		String data = "// "+ JSCRIPTS_IOSPCDATA +"\n";
		data += "// ProcessContainer data file dynamically generated by SAFS/IOS.\n";
		data += "var doChildren = "+ Boolean.toString(doChildren) +";\n";
		data += "var doProperties = "+ Boolean.toString(doProperties) +";\n";
		data += "var appendMap = "+ Boolean.toString(appendMap) +";\n";
		data += "var doShortStrings = "+ Boolean.toString(doShortStrings) +";\n";
		data += "var windowName = \""+ windowName +"\";\n";
		data += "var windowRec = \""+ windowRec +"\";\n";
		data += "var objectRec = \""+ objectRec +"\";\n";
		writeDataToFile(data, targetdir, JSCRIPTS_IOSPCDATA, false);
	}
}
