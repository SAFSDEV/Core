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
 * A wrapper to the Mercury QuickTest Pro SAFS engine--the "QTP" engine.
 * This engine can only be used if you have a valid install of Mercury's 
 * QuickTest Pro product.
 * <p>
 * The default SAFSDRIVER Tool-Independent Driver (TID) does not provide for any 
 * command-line options to configure the Engine.  All configuration information must 
 * be provided in config files.  By default, these are SAFSTID.INI files.  See 
 * {@link <a href="http://safsdev.sf.net/doc/JSAFSFrameworkContent.htm#configfile">SAFSTID Config Files</a>}
 * for more information.
 * <p>
 * The QTP supported config file items:
 * <p>
 * <ul><pre>
 * <b>[SAFS_QTP]</b>
 * AUTOLAUNCH=FALSE                    Defaults to FALSE because config info must be valid.
 * HOOK=C:\PathTo\AnyAOM.VBS           (QTP Automation Object Model startup via WSH cscript.exe)
 * ;HOOK=C:\PathTo\AnyExecutable.EXT   (Alternate method of starting like via a .BAT file)
 * ;TIMEOUT=45                         (Timeout in seconds before issuing autolaunch failure)
 * </pre><p>
 * Note: The HOOK item can be a VBS script that will be used as an argument to the 
 * CSCRIPT.EXE executable for Windows Script Host.  This script is intended to use the QTP 
 * Automation Object Model to prepare and then launch QTP as desired by the tester.
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
 * @see org.safs.tools.consoles.ProcessConsole
 */
public class SAFSQTP extends GenericEngine {

	/** 
	 * "SAFS/QTP" -- The name of this engine as registered with STAF.
	 */
	static final String ENGINE_NAME            = "SAFS/QTP";
	static final String TIMEOUT_OPTION         = "TIMEOUT";

	/**
	 * Constructor for QTP.  Call launchInterface with an appropriate DriverInterface 
	 * before attempting to use this minimally initialized object.
	 */
	public SAFSQTP() {
		super();
		servicename = ENGINE_NAME;
	}

	/**
	 * PREFERRED Constructor for SAFSQTP.
	 */
	public SAFSQTP(DriverInterface driver) {
		this();
		launchInterface(driver);
	}

	/**
	 * Extracts configuration information and launches QTP initialization script 
	 * in a new process.
	 * 
     * <br/>@author Carl Nagle NOV 18, 2005 Modified CScript launch command for VBScript.
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
				String setting = config.getNamedValue(DriverConstant.SECTION_SAFS_QTP, 
           				                              "AUTOLAUNCH");

				if (setting==null) setting = "";
	
				// launch it if we dare!
				if ((setting.equalsIgnoreCase("TRUE"))||
				    (setting.equalsIgnoreCase("YES")) ||
				    (setting.equalsIgnoreCase("1"))){

					Log.info(ENGINE_NAME +" attempting AUTOLAUNCH...");

					String array;					
					String tempstr;
					String hookext;

					// HOOK scrip 
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_QTP, 
				         		      "HOOK");
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
	
						Log.info(ENGINE_NAME +" preparing to execute external process...");
						Log.info(array);
	
					    // launch QTP
						Runtime runtime = Runtime.getRuntime();
						process = runtime.exec(array);
						
						console = new ProcessConsole(process);
						Thread athread = new Thread(console);
						athread.start();
						
						int timeout = 45;
						int loop    = 0;
						running = false;

						//try optional config file timeout
						try{
							String t = config.getNamedValue(DriverConstant.SECTION_SAFS_QTP, TIMEOUT_OPTION);
							if((t !=null)&&(t.length()>0)) timeout = Integer.parseInt(t);
						}catch(NumberFormatException nf){
							Log.info(ENGINE_NAME +" ignoring invalid config info for TIMEOUT.");
						}
						
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

