/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.tools.engines;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSSTAFRegistrationException;
import org.safs.STAFHelper;
import org.safs.StringUtils;
import org.safs.TestRecordHelper;
import org.safs.staf.STAFProcessHelpers;
import org.safs.tools.consoles.ProcessConsole;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverInterface;
import org.safs.tools.stringutils.StringUtilities;


/**
 * A wrapper to Test Complete SAFS engine--the "TC" engine.
 * This engine can only be used if you have a valid install of the
 * Test Complete product.
 * <p>
 * The default SAFSDRIVER Tool-Independent Driver (TID) does not provide for any
 * command-line options to configure the Engine.  All configuration information must
 * be provided in config files.  By default, these are SAFSTID.INI files.  See
 * {@link <a href="http://safsdev.sf.net/sqabasic2000/JSAFSFrameworkContent.htm#configfile">SAFSTID Config Files</a>}
 * for more information.
 * <p>
 * The TC supported config file items:
 * <p>
 * <ul><pre>
 * <b>[SAFS_TC]</b>
 * AUTOLAUNCH=FALSE                     Defaults to FALSE because config info must be valid.
 * HOOK=C:\SAFS\TCAFS\TCAFS.VBS         (TC Automation Object Model startup via WSH cscript.exe)
 * ;HOOK=C:\PathTo\AnyExecutable.EXT    (Alternate method of starting like via a .BAT file)
 * ;TIMEOUT=45                          (Alternate AutoLaunch timeout value in seconds)
 * ;SuiteName="C:\SAFS\TCAFS\TCAFS.pjs" (Alternate project suite to use instead of default TCAFS.pjs)
 * ;ProjectName="TCAFS"                 (Alternate project to use instead of default TCAFS)
 * ;ScriptName="StepDriver"             (Alternate script to use instead of default StepDriver)
 * ;OPTIONS="/customArg:value /Another" (Optional passthru args to go to TC scripts needing them)
 * ;ConvertSAFSInputKeysSyntax=ON|OFF   (ON to use SAFS InputKeys syntax in Test Complete)
 * </pre><p>
 * Note: The HOOK item can be a VBS script that will be used as an argument to the
 * CSCRIPT.EXE executable for Windows Script Host.  This script is intended to use the TC
 * Automation Object Model to prepare and then launch TC as desired by the tester.
 * <p>
 * The HOOK item can also be a valid full path to any other executable -- like a batch file (.BAT).
 * The value of HOOK in this case will be used "as is".  The Java Runtime.exec function will attempt
 * to launch this, so whatever limitations placed by the Runtime.exec from Java apply.  Mostly this
 * may only be an issue if spaces or tabs exist in the string.
 * <p>
 * We do use a ProcessConsole to keep the Process in, out, and err streams from filling up.
 * <p>
 * {@link <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Runtime.html#exec(java.lang.String)">Runtime.exec</a>}<br>
 * {@link <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Process.html">Runtime.exec Process</a>}<br>
 *
 * <br/>@author Carl Nagle NOV 18, 2005 Modified CScript launch command for VBScript.
 * <br/>@author Carl Nagle DEC 14, 2005 Refactored with DriverConfiguredSTAFInterface superclass
 * <br/>@author JCRUNK AUG 01, 2006 Modified CScript launch command for VBScript.
 * <br/>@author Carl Nagle MAR 18, 2011 Accept additional TC/VBS INI command-line parameters
 * <br/>@author Lei Wang JAN 18, 2012 Modify to receive the console log messages from TC through STAF-Queue
 * @see org.safs.tools.consoles.ProcessConsole
 */
public class SAFSTC extends GenericEngine {

	/**
	 * "SAFS/TC" -- The name of this engine as registered with STAF.
	 */
	static final String ENGINE_NAME = "SAFS/TC";

	/**
	 * "SAFS/TC/CONSOLEMSG" -- The name of the handle registered with STAF, 
	 * we use this handle's queue to receive TC's console message.
	 * If you change it value, change the same constant defined in LogUtilities_X.SVB
	 * to make sure they have the same value.
	 */
	static final String CONSOLE_MSG_QUEUE_NAME = "SAFS/TC/CONSOLEMSG";
	
	/**
	 * CONSOLE_MSG_SEPARATOR is the separator defined in LogUtilities_X.SVB
	 * From Test Complete, we combine the message and description by this separator,
	 * and sent the whole message to the queue CONSOLE_MSG_QUEUE_NAME.
	 * We MUST keep it has the same value as the same constant defined in LogUtilities_X.SVB
	 */
	static final String CONSOLE_MSG_SEPARATOR = "$=|=$";
	/**
	 * Constructor for TC.  Call launchInterface with an appropriate DriverInterface
	 * before attempting to use this minimally initialized object.
	 */
	public SAFSTC() {
		super();
		servicename = ENGINE_NAME;
	}

	/**
	 * PREFERRED Constructor for SAFSTC.
	 */
	public SAFSTC(DriverInterface driver) {
		this();
		launchInterface(driver);
	}

	/**
	 * Extracts configuration information and launches TC initialization script
	 * in a new process.
	 *
     * <br/>@author JCRUNK AUG 1, 2006 Modified CScript launch command for VBScript.
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
				String setting = config.getNamedValue(DriverConstant.SECTION_SAFS_TC,
           				                              "AUTOLAUNCH");

				if (setting==null) setting = "";

				// launch it if we dare!
				if (StringUtilities.convertBool(setting)){

					Log.info(ENGINE_NAME +" attempting AUTOLAUNCH...");

					String array;
					String tempstr;
					String hookext;
					String suitename = "";
					String projectname = "";
					String scriptname = "";
					String safsconfig = "";
					String passthru = "";

					// SuiteName
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_TC, "SuiteName");
					if(tempstr != null) suitename = " -suitename \""+ tempstr +"\"";
					
					// ProjectName
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_TC, "ProjectName");
					if(tempstr != null) projectname = " -projectname \""+ tempstr +"\"";
					
					// ScriptName
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_TC, "ScriptName");
					if(tempstr != null) scriptname = " -scriptname \""+ tempstr +"\"";
					
					// safs.project.config
					tempstr = config.getConfigurePaths();
					if((tempstr != null) && (tempstr.length()> 0)) safsconfig = " -safs.project.config \""+ tempstr +"\"";
					
					// OPTIONS (PassThru)
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_TC, "Options");
					if(tempstr != null) passthru = " -passthru \""+ tempstr +"\"";
					
					// HOOK script
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_TC, "HOOK");
				    if ((tempstr == null)||(tempstr.length()<4)) {
						Log.generic(ENGINE_NAME +" HOOK parameter is missing or invalid!");
				    }
				    else{
						hookext = tempstr.substring(tempstr.length()-3);

						if (hookext.equalsIgnoreCase("VBS")){
					    	array = "CMD /C start cscript.exe //B //NoLogo "+ "\""+ tempstr +"\"";
						}else{
					    	array = tempstr;
						}

						array += suitename + projectname + scriptname + safsconfig + passthru;
						
						Log.info(ENGINE_NAME +" preparing to execute external process...");
						Log.info(array);

					    // launch TC
						Runtime runtime = Runtime.getRuntime();
						process = runtime.exec(array);

						console = new ProcessConsole(process);
						Thread athread = new Thread(console);
						athread.start();

						int timeout = 60;
						//Get the timeout from the config file JCRUNK
						try{
							String t = config.getNamedValue(DriverConstant.SECTION_SAFS_TC,
				         		              "TIMEOUT");
							if((t !=null)&&(t.length()>0)) 
								timeout = Integer.parseInt(t);
						}catch(NumberFormatException nf){
							Log.info(ENGINE_NAME +" ignoring invalid config info for TIMEOUT.");
						}
				        //If no timeout supplied, set it to 45 seconds. JCRUNK
						//TC/TE sometimes doesn't start by first try. so try twice within timeout
						//It could be TC/TE bug or Win2008 OS issue.
						//Remove below restart code if the Issue# M0088464 fix from AutomatedQA.
						
						timeout = timeout/2; 
						
						int loop    = 0;
						running = false;

						for(;((loop < timeout)&&(! running));loop++){
							running = isToolRunning();
							if(! running)
							   try{Thread.sleep(1000);}catch(InterruptedException ix){}
						}
						
						//second time try to start TC/TE
						if (! running){
							Log.info("Try to restart "+ ENGINE_NAME +
					          " again within timeout!");
							
							console.shutdown();
							process.destroy();
							
							Thread.sleep(5000); // wait allow to clean up
							runtime = Runtime.getRuntime();
							process = runtime.exec(array);							
							console = new ProcessConsole(process);
							athread = new Thread(console);
							athread.start();
							
							loop = 0; // reset loop for remaining time
							
							for(;((loop < timeout)&&(! running));loop++){
								running = isToolRunning();
								if(! running)
									try{Thread.sleep(1000);}catch(InterruptedException ix){}
							}
				    	}
						// Finally check
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
				}
				// not supposed to autolaunch
				else{
					Log.generic(ENGINE_NAME +" AUTOLAUNCH is *not* enabled.");
					
					// ?we will hope the user is getting it online before we have to use it?
				}
			}
			
			//Try to get console message from Queue, these messages are generated by TestComplete and put into the queue.
			if(running){
				java.lang.Runnable runnable = new java.lang.Runnable(){					
					public void run() {
						String messageAndDesc = null;
						String message = null;
						boolean stop = false;
						java.util.List<String> list = null;
						STAFHelper consoleQueueHandle = null;
						
						try {
							consoleQueueHandle = STAFProcessHelpers.registerHelper(CONSOLE_MSG_QUEUE_NAME);
							while(!stop){
						        try {
						        	//Get the messages from queue, these messages are added by handle "SAFS/TC"
						        	messageAndDesc = consoleQueueHandle.getQueueMessage(getEngineName(),null);
						        	list = StringUtils.getTokenList(messageAndDesc, CONSOLE_MSG_SEPARATOR);
						        	for(int i=0;i<list.size();i++){
						        		message = list.get(i);
						        		if(message!=null && message.length()!=0){
						        			System.out.println(message);
						        		}
						        	}
								} catch (Exception e) {
									Log.error("Error getting message from STAF QUEUE:" +CONSOLE_MSG_QUEUE_NAME+". Error:"+ e.getMessage());
									STAFProcessHelpers.unRegisterHelper(CONSOLE_MSG_QUEUE_NAME);
									stop = true;
								}
							}
						} catch (SAFSSTAFRegistrationException stre) {
							Log.error("Error Registration with HANDLE: " +CONSOLE_MSG_QUEUE_NAME+". Error:"+ stre.getMessage());
						}
					}
				};
				Thread thread = new Thread(runnable);
				thread.setDaemon(true);
				thread.start();
			}
			
		}catch(Exception x){
			Log.error(
			ENGINE_NAME +" requires a valid DriverInterface object for initialization!  "+
			x.getMessage());
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

