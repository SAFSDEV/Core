package org.safs.tools.engines;

import java.io.File;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.TestRecordHelper;
import org.safs.selenium.SeleniumJavaHook;
import org.safs.selenium.webdriver.WebDriverGUIUtilities;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.consoles.ProcessConsole;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverInterface;

/**
 * A wrapper to the Selenium SAFS engine--the "Selenium 2.0 WebDriver" engine.
 * This engine can only be used if you have a valid install of Selenium (http://www.openqa.org/selenium/)
 * <p>
 * The default SAFSDRIVER Tool-Independent Driver (TID) does not provide for any
 * command-line options to configure the Engine.  All configuration information must
 * be provided in config files.  By default, these are SAFSTID.INI files.
 * <p>
 * See {@link <a href="../../selenium/webdriver/SeleniumHook.html">SAFS Selenium Hook</a>} for config options of Selenium2.0
 * <p>
 * Also see
 * {@link <a href="http://safsdev.sf.net/sqabasic2000/JSAFSFrameworkContent.htm#configfile" target="_blank">SAFSDRIVER Configuration File</a>}
 * for more information.
 * </ul>
 * @author PHSABO AUG 14, 2006
 * @author CANAGL APR 18, 2008 Primarily updating documentation.
 * @author CANAGL NOV 03, 2015 More complete conversion to Selenium 2.0 WebDriver usage.
 */
public class SAFSSELENIUM extends GenericEngine {

	/**
	 * "SAFS/Selenium" -- The name of this engine as registered with STAF.
	 */
	static final String ENGINE_NAME = "SAFS/Selenium";
	static final String XBOOTCLASSPATH_OPTION  = "XBOOTCLASSPATH";

	/**
	 * "org.safs.selenium.SeleniumJavaHook" -- The Selenium class for the SAFS hook.
	 */
	static final String HOOK_CLASS  = "org.safs.selenium.SeleniumJavaHook";
	static final String HOOK_CLASS2  = "org.safs.selenium.webdriver.SeleniumHook";

	/**
	 * Constructor for SAFSSELENIUM.  Call launchInterface with an appropriate DriverInterface
	 * before attempting to use this minimally initialized object.
	 */
	public SAFSSELENIUM() {
		super();
		servicename = ENGINE_NAME;
	}

	/**
	 * PREFERRED Constructor for SAFSSELENIUM.
	 */
	public SAFSSELENIUM(DriverInterface driver) {
		this();
		launchInterface(driver);
	}

	/**
	 * Extracts configuration information and launches SELENIUM in a new process.
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
				String setting = config.getNamedValue(DriverConstant.SECTION_SAFS_SELENIUM,
           				                              "AUTOLAUNCH");

				if (setting==null) setting = "";

				// launch it if we dare!
				if ((setting.equalsIgnoreCase("TRUE"))||
				    (setting.equalsIgnoreCase("YES")) ||
				    (setting.equalsIgnoreCase("1"))){

					Log.info(ENGINE_NAME +" attempting AUTOLAUNCH...");

					String array = "";

					String tempstr = null;

					boolean isSAFS = WebDriverGUIUtilities.isSAFS();

					// JVM
				    String jvm = isSAFS ?
				        	System.getenv("SAFSDIR") + File.separator + "jre" + File.separator + "bin" + File.separator +"java":
				        	System.getenv("SELENIUM_PLUS") + File.separator + "Java"+ File.separator + "jre" + File.separator + "bin" + File.separator +"java";
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_SELENIUM,
				         		      "JVM");
				    if (tempstr != null) jvm=tempstr;
				    array = jvm +" ";

			    	// XBOOTCLASSPATH -- not used by Selenium 2.0  -- previous JARs are not even delivered anymore
				    // separate RJ JVM needs some jars to be in CLASSPATH, append them to end of bootstrap classpath.
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_SELENIUM, XBOOTCLASSPATH_OPTION);
				    if(tempstr != null){
			    		array += "-Xbootclasspath/a:" + makeQuotedString(tempstr) + " ";
				    }

					// CLASSPATH
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_SELENIUM,
				         		      "CLASSPATH");
				    if (tempstr != null) {
				    	array += "-cp "+ tempstr +" ";
				    }

				    // CONFIGPATHS
				    tempstr   = config.getConfigurePaths();
				    if (tempstr != null) {
				    	array += "-Dsafs.config.paths="+ tempstr +" ";
				    }

					// HOOK CLASS  defaults to HOOK_CLASS
				    tempstr   = config.getNamedValue(DriverConstant.SECTION_SAFS_SELENIUM,
				         		      "HOOK");
				    if (tempstr == null) tempstr = HOOK_CLASS;

			    	array += tempstr +" ";

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
			} else {
				Log.info(ENGINE_NAME +" already running.");
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

	//override the superclass, we must wait for the hook starting the Selenium Server
	public boolean isToolRunning() {
		boolean running = super.isToolRunning();

		//If STAF HANDLE "SAFS/Selenium" has been set up
		//We also need wait for the boot up of selenium server
		if(running){
			try {
				String seleniumServerReady = null;
				seleniumServerReady = staf.getSTAFVariable(SeleniumJavaHook.SAFS_SELENIUM_SERVER_BOOTUP_READY);
				Log.debug("STAF variable SAFS_SELENIUM_SERVER_BOOTUP_READY="+seleniumServerReady);
				running = running&&Boolean.parseBoolean(seleniumServerReady);
			} catch (SAFSException e) {
				Log.warn("SAFSSELNIUM ENGINE: error when getting STAF varialbe SAFS_SELENIUM_SERVER_BOOTUP_READY.");
				running=false;
			}
		}

		return running;
	}
}

