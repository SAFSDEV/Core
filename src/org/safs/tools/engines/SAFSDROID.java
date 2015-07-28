/** Copyright (C) SAS Institute All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.tools.engines;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.safs.TestRecordHelper;
import org.safs.android.auto.lib.Process2;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverInterface;
import org.safs.tools.stringutils.StringUtilities;
import org.safs.tools.consoles.ProcessCapture;
import org.safs.tools.consoles.ProcessConsole;

import org.safs.Log;

/**
 * A wrapper to Google's Android Automation SAFS engine--the "Droid" engine.
 * This engine can only be used if you have a valid install of the Android Developer 
 * SDK tools (>=r16)--initially on a Windows computer, but intended to support Unix/Mac 
 * systems as well.
 * <p>
 * This is intended for testing of Android Smartphone and Tablet applications.
 * <p>
 * The default SAFSDRIVER Tool-Independent Driver (TID) does not provide for any
 * command-line options to configure the Engine.  All configuration information must
 * be provided in config files.  By default, these are SAFSTID.INI files.  See
 * {@link <a href="http://safsdev.sf.net/sqabasic2000/JSAFSFrameworkContent.htm#configfile">SAFSTID Config Files</a>}
 * for more information.
 * <p>
 * The Droid supported config file items are listed below.  Remember, this engine has only been tested on Windows, 
 * so all sample file paths are in Windows format:
 * <p>
 * <ul><pre>
 * <b>[SAFS_DROID]</b>
 * AUTOLAUNCH=FALSE                     (Defaults to FALSE because config info must be valid.)
 * DroidProject=C:\SAFS\samples\Droid   (Sample Droid Automation Project Location)
 * Console2Debug=True/False             (Allow the process console (adb) into the driver console/debug log.)
 * ;ShutdownDelay=N                      (default: 0, Delay in seconds to delay the shutdown to allow remote process completion.)
 * ;HOOK=org.safs.android.DJavaHook     (Java SAFS Engine Class)
 * ;EMULATOR_AVD="SprintEVO"            (User-Defined Emulator AVD to use, if any)
 * ;ANDROID-SDK=Android SDK Root Dir    (Root dir for Android SDK)
 * ;ANDROID-TOOLS=Android SDK Tools Dir ([Sub]Dir for Android SDK Tools)
 * ;TCPMessengerAPK=path to Messenger   (default: SAFSTCPMessenger\bin\SAFSTCPMessenger-debug.apk)
 * ;TCPMessengerPackage=packageName     (default: org.safs.android.messenger )
 * ;TestRunnerAPK=path to Engine APK    (default: SAFSTestRunner\bin\SAFSTestRunner-debug.apk)
 * ;TestRunnerPackage=packageName       (default: org.safs.android.engine )
 * ;TestRunnerSource=path to source     (Ex: "SAFSTestRunner"  (Project relative or absolute)
 * ;DeviceSerial=serial number          (a specific adb -s device/emulator if multiple are present)
 * ;PersistEmulators=True/False         (default: false. True to NOT force shutdown of emulators)
 * ;PersistJVM=True/False               (default: false. True to try to stop the shutdown of JVM.)
 * ;TIMEOUT=120                         (Alternate AutoLaunch timeout value in seconds)
 * ;STAFID="SAFS/DROID"                 (Normally never used.)
 * ;JVM=JVMpath                         (Full path to desired Java executable if "java" is not sufficient.)
 * ;JVMARGS=JVM Args                    (Ex: "-Xms512m -Xmx512m", will be used unmodified.)
 * ;CLASSPATH=altClasspath              (Generally overrides system classpath.)
 * ;XBOOTCLASSPATH=&lt;CLASSPATH>       (CLASSPATH needed for rare cases. Normally never used.)
 * ;ConvertSAFSInputKeysSyntax=ON|OFF   ([FUTURE]ON to use SAFS InputKeys syntax on Android)
 * </pre>
 * <p>
 * We do use a ProcessConsole to keep the Process in, out, and err streams from filling up.
 * <p>
 * {@link <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Runtime.html#exec(java.lang.String)">Runtime.exec</a>}<br>
 * {@link <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Process.html">Runtime.exec Process</a>}<br>
 *
 * <br/>@author Carl Nagle DEC 15, 2011 Original Release
 * @see org.safs.tools.consoles.ProcessCapture
 */
public class SAFSDROID extends GenericEngine {

	/**
	 * "SAFS/DROID" -- The name of this engine as registered with STAF.
	 */
	public static final String ENGINE_NAME = "SAFS/DROID";
	
	/** 
	 * "org.safs.ios.JavaHook"  
	 */
	public static final String HOOK_CLASS       = "org.safs.android.DJavaHook";

	public static final String AUTOLAUNCH_OPTION      = "AUTOLAUNCH";
	public static final String HOOK_OPTION            = "HOOK";
	public static final String DEBUG2CONSOLE_OPTION   = "Console2Debug";
	public static final String SHUTDOWN_DELAY_OPTION  = "ShutdownDelay";
	public static final String PROJECT_OPTION         = "PROJECT";
	public static final String EMULATOR_AVD_OPTION    = "EMULATOR_AVD";
	public static final String JVM_OPTION             = "JVM";
	public static final String JVMARGS_OPTION         = "JVMARGS";
	public static final String XBOOTCLASSPATH_OPTION  = "XBOOTCLASSPATH";
	public static final String TIMEOUT_OPTION         = "TIMEOUT";
	public static final int DEFAULT_INIT_TIMEOUT      = 120;	

	/**
	 * Default 0.  
	 * A delay (in seconds) during shutdown allowing the remote engine process to complete 
	 * shutdown activities.
	 */
	int shutdown_delay_seconds = 0;
	
	/**
	 * Constructor for SAFS/DROID.  Call launchInterface with an appropriate DriverInterface
	 * before attempting to use this minimally initialized object.
	 */
	public SAFSDROID() {
		super();
		servicename = ENGINE_NAME;
	}

	/**
	 * PREFERRED Constructor for SAFS/DROID.
	 */
	public SAFSDROID(DriverInterface driver) {
		this();
		launchInterface(driver);
	}

	/**
	 * Extracts configuration information and launches SAFS/DROID initialization in a new process.
	 *
	 * @see GenericEngine#launchInterface(Object)
	 */
	public void launchInterface(Object configInfo){
		super.launchInterface(configInfo);

		try{
			// see if we are already running
			// launch it if our config says AUTOLAUNCH=TRUE and it is not running
			// otherwise don't AUTOLAUNCH it.
			if( ! isToolRunning()){

				Log.info(ENGINE_NAME +" is not running. Evaluating AUTOLAUNCH...");

				//check to see if AUTOLAUNCH exists in ConfigureInterface
				String setting = config.getNamedValue(DriverConstant.SECTION_SAFS_DROID,
           				                              AUTOLAUNCH_OPTION);

				if (setting==null) setting = "";

				// exit with message if AUTOLAUNCH is not enabled.
				/************************************************************/
				if (! StringUtilities.convertBool(setting)){
					Log.generic(ENGINE_NAME +" AUTOLAUNCH is *not* enabled.");
					return;
				}/***********************************************************/

				Log.info(ENGINE_NAME +" attempting AUTOLAUNCH...");
				
				String array = "";				
				String tempstr = null;

				// JVM	
			    String jvm = "java";				    
			    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_DROID, JVM_OPTION);
			    if (tempstr != null) {
			    	CaseInsensitiveFile afile = new CaseInsensitiveFile(tempstr);
			    	if(! afile.isFile()){
						Log.error(ENGINE_NAME +" Option 'JVM' Java executable path is invalid: "+ tempstr);
						return;
			    	}
			    	jvm=makeQuotedPath(tempstr, true);
			    //try to derive
			    }
		    	Log.generic(ENGINE_NAME +" using 'JVM' path: "+ jvm);
			    array = jvm +" ";
			    
				// JVMARGS -- append unmodified, if present
			    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_DROID, JVMARGS_OPTION);
			    if (tempstr != null) { 
			    	array += tempstr +" ";
			    }

		    	// XBOOTCLASSPATH 
			    // separate RJ JVM needs some jars to be in CLASSPATH, append them to end of bootstrap classpath.
			    // When java.exe calls rational_ft.jar using option -jar, the JVM only takes rational_ft.jar as its classpath. 
			    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_DROID, XBOOTCLASSPATH_OPTION);
			    if(tempstr != null){
		    		array += "-Xbootclasspath/a:" + makeQuotedString(tempstr) + " ";	
			    }			    
			    
			    //-Dsafs.config.paths=
			    String cpaths = config.getConfigurePaths();
			    cpaths = DriverConstant.PROPERTY_SAFS_CONFIG_PATHS +"="+ cpaths;
			    //wrap in quotes if embedded space exists
			    if (cpaths.indexOf(" ")>0) cpaths = "\""+ cpaths +"\"";
			    array += "-D"+ cpaths +" ";
			
				// HOOK script
				String hook = HOOK_CLASS;
			    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_DROID, HOOK_OPTION);
			    if (tempstr != null){
			    	hook = tempstr;
			    }
			    array += hook;

			    Log.info(ENGINE_NAME +" preparing to execute external process...");
				Log.info(" ");
				Log.info(array);
				Log.info(" ");
			    Log.info("Using runtime environment variables...");
				Log.info(" ");

				boolean debug2console = false;
			    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_DROID, DEBUG2CONSOLE_OPTION);
			    if (tempstr != null){
			    	debug2console = StringUtilities.convertBool(tempstr);
			    }
		    	Log.generic(ENGINE_NAME +" using Console2Debug: "+ debug2console);

			    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_DROID, SHUTDOWN_DELAY_OPTION);
			    if (tempstr != null){
			    	shutdown_delay_seconds = StringUtilities.convertToInteger(tempstr);
			    }
		    	Log.generic(ENGINE_NAME +" will be using ShutdownDelay: "+ shutdown_delay_seconds);

			    // launch Droid Java Hook
				process = Runtime.getRuntime().exec(array);
				console = new ProcessCapture(process, null, true, debug2console);

				int timeout = DEFAULT_INIT_TIMEOUT;
				//try optional config file timeout
				try{
					String t = config.getNamedValue(DriverConstant.SECTION_SAFS_DROID, TIMEOUT_OPTION);
					if((t !=null)&&(t.length()>0)) timeout = Integer.parseInt(t);
				}catch(NumberFormatException nf){
					Log.info(ENGINE_NAME +" ignoring invalid config info for TIMEOUT.");
				}

				int loop    = 0;
				running = false;

				for(;((loop < timeout)&&(! running));loop++){
					running = isToolRunning();
					if(! running)
					   try{Thread.sleep(1000);}catch(InterruptedException ix){}
				}
				if(! running){
					Log.error("Unable to detect running "+ ENGINE_NAME +
					          " within timeout period!");
					console.shutdown();
					process.destroy();
					return;
				}
				else{
					weStartedService = true;
					Log.info(ENGINE_NAME + " detected.");
				}
		    }
		}catch(Exception x){
			Log.error(
			ENGINE_NAME +" requires a valid DriverInterface object for initialization!  "+
			x.getMessage());
		}
	}

	/**
	 * Allow any emulator shutdown activities to complete before JVM terminates.
	 * We will wait {@link #shutdown_delay_seconds} seconds.
	 */
	@Override
	protected void postShutdownServiceDelay(){		
		long now = System.currentTimeMillis();
		long secs = shutdown_delay_seconds * 1000;
		long end = now + secs;
		while(System.currentTimeMillis() < end){
			try{Thread.sleep(secs);}
			catch(Exception x){
				secs = 500; 
				// in-case we got interrupted before the actual timeout
				// we don't want to wait another full N secs
			}
		}		
	}
	
	// this may be more correctly refactored into the GenericEngine superclass.
	/** Override superclass to catch unsuccessful initialization scenarios. */
	public long processRecord(TestRecordHelper testRecordData) {
		if (running) return super.processRecord(testRecordData);
		running = isToolRunning();
		if (running) return super.processRecord(testRecordData);
		return DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
	}
}

