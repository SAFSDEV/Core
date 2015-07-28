package org.safs.tools.engines;

import org.safs.SAFSException;
import org.safs.TestRecordHelper;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverInterface;
import org.safs.tools.consoles.ProcessConsole;

import org.safs.Log;
import org.safs.STAFHelper;
import org.safs.staf.STAFProcessHelpers;
import com.ibm.staf.STAFResult;
import java.util.ArrayList;

/**
 * A wrapper to the Abbot SAFS engine--the "Abbot" engine.
 * This engine can only be used if you have a valid install of Abbot (http://abbot.sourceforge.net)
 * <p>
 * The default SAFSDRIVER Tool-Independent Driver (TID) does not provide for any 
 * command-line options to configure the Engine.  All configuration information must 
 * be provided in config files.  By default, these are SAFSTID.INI files.  See 
 * {@link <a href="http://safsdev.sf.net/doc/JSAFSFrameworkContent.htm#configfile">SAFSTID Config Files</a> 
 * for more information.
 * <p>
 * The Abbot supported config file items:
 * <p>
 * <ul><pre>
 * <b>[SAFS_ABBOT]</b>
 * AUTOLAUNCH=FALSE            Defaults to FALSE because config info must be valid.
 * ;JVM=JVMpath                If "java" is not sufficient.
 * ;CLASSPATH=altClasspath     Generally overrides system classpath.
 * ;HOOK=org.safs.abbot.AbbotJavaHook  Class used as the engine Hook.
 * ;OPTIONS=                   Normally never used.
 * </ul>
 * @author CANAGL DEC 14, 2005 Refactored with DriverConfiguredSTAFInterface superclass
 */
public class SAFSABBOT extends GenericEngine {

	/** 
	 * "SAFS/RobotJ" -- The name of this engine as registered with STAF.
	 */
	static final String ENGINE_NAME = "SAFS/Abbot";

	/** 
	 * "com.rational.test.ft.rational_ft" -- The Rational class that is RobotJ/XDE Tester. 
	 */
	static final String HOOK_CLASS  = "org.safs.abbot.AbbotJavaHook";

	/**
	 * Constructor for SAFSROBOTJ.  Call launchInterface with an appropriate DriverInterface 
	 * before attempting to use this minimally initialized object.
	 */
	public SAFSABBOT() {
		super();
		servicename = ENGINE_NAME;
	}

	/**
	 * PREFERRED Constructor for SAFSROBOTJ.
	 */
	public SAFSABBOT(DriverInterface driver) {
		this();
		launchInterface(driver);
	}

	/**
	 * Extracts configuration information and launches RobotJ/XDE Tester in a new process.
	 * <p>
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
				String setting = config.getNamedValue(DriverConstant.SECTION_SAFS_ABBOT, 
           				                              "AUTOLAUNCH");

				if (setting==null) setting = "";
	
				// launch it if we dare!
				if ((setting.equalsIgnoreCase("TRUE"))||
				    (setting.equalsIgnoreCase("YES")) ||
				    (setting.equalsIgnoreCase("1"))){

					Log.info(ENGINE_NAME +" attempting AUTOLAUNCH...");

					String array = "";
					
					String tempstr = null;

					// JVM	
				    String jvm = "java";				    
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_ABBOT, 
				         		      "JVM");
				    if (tempstr != null) jvm=tempstr;
				    array = jvm +" ";
				    
					// CLASSPATH	
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_ABBOT, 
				         		      "CLASSPATH");
				    if (tempstr != null) {
				    	array += "-cp "+ tempstr +" ";
				    }
				    
					// HOOK CLASS  defaults to HOOK_CLASS
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_ABBOT, 
				         		      "HOOK");
				    if (tempstr == null) tempstr = HOOK_CLASS;

			    	array += tempstr +" ";

					// OPTIONS
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_ABBOT, 
				         		      "OPTIONS");
				    if (tempstr != null) array += tempstr +" ";

					Log.info(ENGINE_NAME +" preparing to execute external process...");
					Log.info(array);

				    // launch SAFSROBOTJ
					Runtime runtime = Runtime.getRuntime();
					process = runtime.exec(array);
					
					console = new ProcessConsole(process);
					Thread athread = new Thread(console);
					athread.start();
					
					int timeout = 45;
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
				// not supposed to autolaunch
				else{
					Log.generic(ENGINE_NAME +" AUTOLAUNCH is *not* enabled.");
					// ?we will hope the user is getting it online before we have to use it?
				}
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

