/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.selenium;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;

import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumServer;
import org.safs.DDGUIUtilities;
import org.safs.JavaHook;
import org.safs.Log;
import org.safs.ProcessRequest;
import org.safs.Processor;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.TestRecordHelper;
import org.safs.logging.LogUtilities;
import org.safs.selenium.spc.SPC;
import org.safs.selenium.spc.SPCGUI;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.ConfigureFile;
import org.safs.tools.drivers.ConfigureInterface;
import org.safs.tools.drivers.DriverConstant;

/**
 * This class is our STAF-enabled JavaHook for the "SAFS/Selenium" Engine.
 * This is the class that registers with STAF and responds to the STAF-based 
 * SAFS Engine protocol defined at http://sourceforge.net/docman/display_doc.php?docid=17505&group_id=56751 
 * <p>
 * The STAF name predefined for this Selenium Engine is "SAFS/Selenium".
 * <p>
 * As of this writing, this hook uses standard org.safs.LogUtilities.  The TestStepProcessor 
 * is the "standard" org.safs.TestStepProcessor.
 * <p>
 * The SAFS/Selenium engine does require Selenium-specific implementations of 
 * <ul>
 * <li>org.safs.selenium.SeleniumGuiUtilities
 * <li>org.safs.selenium.STestRecordHelper
 * </ul>
 * <p>
 * If the engine is being launched manually or via a batch file or script then the following  
 * Java System property must be set on the Java command line:
 * <p>
 * <ul>-Dsafs.config.paths=&lt;testpath>;&lt;projectpath>;&lt;driverpath></ul>
 * <p>
 * <i>testpath</i> is the file path to the test configuration INI file,<br>
 * <i>projectpath</i> is the file path to the project configuration INI file,<br>
 * <i>driverpath</i> is the file path to the SAFS driver configuration INI file.
 * <p>
 * At least one INI file path must be specified or the engine will abort due to insufficient 
 * configuration information being provided.
 * <p>
 * The configuration file(s) must minimally provide the following information:
 * <ul><p>
 * <li>BROWSER=&lt;Selenium browser id> Ex: BROWSER="*piiexplore" or BROWSER="*pifirefox"
 * </ul>
 * <p>
 * The configuration file(s) can optionally also contain:
 * <ul><p>
 * <li>GATEWAYHOST=&lt;web proxy domain> Ex: GATEWAYHOST=your.normal.gateway.com
 * <li>GATEWAYPORT=&lt;web proxy port> Example: GATEWAYPORT=80 &lt;your normal gatewory port #>
 * <li>SELENIUMPORT=&lt;selenium server port> Example: SELENIUMPORT=4444  (default)
 * <li>DEBUGLOG=&lt;full_path_to>\ASeleniumDebugLog.txt
 * </ul>
 * <p>
 * <small><i>(PROXY and PORT options previously supported have been deprecated in favor of the 
 * GATEWAYHOST and GATEWAYPORT options. &nbsp;However, PROXY and PORT still work.)</i></small>
 * <p>
 * You can refer to the <a href="http://safsdev.sourceforge.net/sqabasic2000/JSAFSFrameworkContent.htm#configfile">[SAFS_SELENIUM]</a> 
 * setion in SAFSDRIVER Configuration. <small><i>(See Settings for Selnium1.0)</i></small>
 * <p>
 * Assuming everything is properly CLASSPATHed and the config file(s) have proper settings 
 * then the Engine can then be launched with:
 * <ul><p>
 * java -Dsafs.config.paths=&lt;paths> org.safs.selenium.SeleniumJavaHook
 * </ul>
 * <p>
 * There is also the ability to launch the engine in a "Process Container" mode by adding 
 * a single command-line argument, "SPC" as in:
 * <ul><p>
 * java -Dsafs.config.paths=&lt;paths> org.safs.selenium.SeleniumJavaHook SPC
 * </ul>
 * 
 *   <br>   JAN 16, 2007    (CANAGL) Graceful abort when no config provided.
 *   <br>   JAN 16, 2007    (CANAGL) Update of use documentation.
 *   <br>   AUG 01, 2007    (CANAGL) Update documentation and faulty shutdown activities.
 *   <br>   FEB 19, 2008    (CANAGL) Support alternate Selenium Server ports and Selenium Debug Log.
 *   <br>   APR 18, 2008    (CANAGL) Primarily updated documentation.
 *   <br>   MAY 27, 2008    (CANAGL) Documented the "SPC" argument for Selenium Process Container.
 *   <br>   MAR 31, 2011    (LEIWANG) Modify to fit Selenium 1.0
 *   <br>	AUG 08, 2012	(SBJLWA) Modify method stopJavaHOOK(): release the mutex "SAFS/Hook/TRD"
 * @see SeleniumGUIUtilities
 * @see STestRecordHelper
 * @see org.safs.selenium.spc.SPC
 */
public class SeleniumJavaHook extends JavaHook {

		/** Our predefined STAF process name: "SAFS/Selenium" */
		public static final String SELENIUM_COMMANDS = "SAFS/Selenium";
		public static final String SAFS_USER_EXTENSIONS = "org/safs/selenium/user-extensions.js";
		public static File temp_user_extensions = null;
		public static String SAFS_SELENIUM_SERVER_BOOTUP_READY = "SAFS_SELENIUM_SERVER_BOOTUP_READY";

		/** "4444" */
		public static final String DEFAULT_SELENIUMPORT = "4444";
		/** 4444 */
		public static final int    DEFAULT_INT_SELENIUMPORT  = 4444;

		/** 1000*60*5 */
		public static final int    DEFAULT_SELENIUMTIMEOUT  = 1000*60*5;

		public static boolean RUNNING_SPC = false;
		
		private SeleniumServer _server = null;
		
		/**
		 * No-arg constructor for SeleniumJavaHook
		 * Simply calls the super()
		 */
		public SeleniumJavaHook() {
			super();
		}

		/**
		 * Constructor for SeleniumJavaHook
		 * Simply calls super(process_name)
		 * 
		 * @param process_name the ID to use when registering with STAF.
		 * Normally this would be our predefined name "SAFS/Selenium".
		 */
		public SeleniumJavaHook(String process_name) {
			super(process_name);
		}

		/**
		 * Constructor for SeleniumJavaHook
		 * Simply calls super(process_name, logs)
		 * 
		 * @param process_name the ID to use when registering with STAF.
		 * Normally this would be our predefined name "SAFS/Selenium".
		 * 
		 * @param logs the LogUtilities to be used by the Engine.  Currently we can 
		 * use the default org.safs.LogUtilities
		 * 
		 * @see org.safs.LogUtilites
		 */
		public SeleniumJavaHook(String process_name, LogUtilities logs) {
			super(process_name, logs);
		}

		/**
		 * Constructor for SeleniumJavaHook
		 * Simply calls super(process_name, trd_name)
		 * 
		 * @param process_name the ID to use when registering with STAF.
		 * Normally this would be our predefined name "SAFS/Selenium".
		 * 
		 * @param trd_name -- The root name for specific TestRecordData to reference 
		 * in STAF.  This should typically be STAFHelper.SAFS_HOOK_TRD which is used 
		 * by default.
		 * 
		 * @see org.safs.STAFHelper#SAFS_HOOK_TRD
		 */
		public SeleniumJavaHook(String process_name, String trd_name) {
			super(process_name, trd_name);
		}


		/**
		 * Constructor for SeleniumJavaHook
		 * Simply calls super(process_name, trd_name, logs)
		 * 
		 * @param process_name the ID to use when registering with STAF.
		 * Normally this would be our predefined name "SAFS/Selenium".
		 * 
		 * @param trd_name -- The root name for specific TestRecordData to reference 
		 * in STAF.  This should typically be STAFHelper.SAFS_HOOK_TRD which is used 
		 * by default.
		 * 
		 * @param logs the LogUtilities to be used by the Engine.  Currently we can 
		 * use the default org.safs.LogUtilities
		 * 
		 * @see org.safs.STAFHelper#SAFS_HOOK_TRD
		 * @see org.safs.LogUtilites
		 */
		public SeleniumJavaHook(String process_name, String trd_name, 
		                      LogUtilities logs) {
		                      	
			super(process_name, trd_name, logs);
		}

		/**
		 * Advanced Constructor for SeleniumJavaHook.
		 * Simply calls super(process_name, trd_name, logs, trd_data, gui_utils, aprocessor)
		 * 
		 * @param process_name the ID to use when registering with STAF.
		 * Normally this would be our predefined name "SAFS/Selenium".
		 * 
		 * @param trd_name -- The root name for specific TestRecordData to reference 
		 * in STAF.  This should typically be STAFHelper.SAFS_HOOK_TRD which is used 
		 * by default.
		 * 
		 * @param logs the LogUtilities to be used by the Engine.  Currently we can 
		 * use the default org.safs.LogUtilities
		 * 
		 * @param trd_data -- ATestRecordHelper to hold TestRecordData.  
		 * The Selenium engine expects an instance of ATestRecordHelper.
		 * 
		 * @param gui_utils -- SeleniumGuiUtilities the hook will use for handling components.
		 * The Selenium engine expects an instance of SeleniumGuiUtilities.
		 * 
		 * @param aprocessor -- ProcessRequest object the hook will use for routing records.
		 * The Selenium engine uses the standard ProcessRequest class and a standard TestStepProcessor.
		 * 
		 * @see org.safs.STAFHelper#SAFS_HOOK_TRD
		 * @see org.safs.LogUtilites
		 * @see STestRecordHelper
		 * @see SeleniumGUIUtilities
		 * @see org.safs.ProcessRequest
		 * @see org.safs.TestStepProcessor
		 */
		public SeleniumJavaHook (String process_name, String trd_name, 
		                     LogUtilities logs,
		                     TestRecordHelper trd_data,
		                     DDGUIUtilities gui_utils,
		                     ProcessRequest aprocessor) {

			super(process_name, trd_name, logs, trd_data, gui_utils, aprocessor);
		}


		/**
		 * Use this method to retrieve/create the current/default TestRecordHelper instance.
		 * If the TestRecordHelper has not been set this routine will instance a 
		 * new org.safs.selenium.ATestRecordHelper and populate it with a STAFHelper via 
		 * getHelper() and a DDGUIUtilities via getGUIUtilities.  Note that the call 
		 * to getGUIUtilities may force the instantiation of the default SeleniumGuiUtilities 
		 * if one has not already been set.  
		 * <p>
		 * Note that there is a known circular execution between getTRDData and 
		 * getGUIUtilities if neither was previously set.  Each routine 
		 * calls the other which may result in a second call to the other. 
		 * This has not been a problem.
		 * 
		 * @return TestRecordHelper which should be an instanceof org.safs.selenium.ATestRecordHelper
		 * 
		 * @see STestRecordHelper
		 * @see #getHelper()
		 * @see #getGUIUtilities()
		 * @see JavaHook#getTRDData()
		 */
		public TestRecordHelper getTRDData() {
			if (data==null) {
					data=new STestRecordHelper();
					data.setSTAFHelper(getHelper());
					data.setDDGUtils(getGUIUtilities());
				}
			return data;
		}


		/**
		 * Use this method to retrieve/create the current/default DDGUIUtilities instance.
		 * If the DDGUIUtilities has not been set this routine will instance a 
		 * new org.safs.selenium.SeleniumGUIUtilities and populate it with a STAFHelper via 
		 * getHelper() and a TestRecordHelper via getTRDData().  Note that the call 
		 * to getTRDData() may force the instantiation of the default ATestRecordHelper 
		 * if one has not already been set.  
		 * <p>
		 * The SeleniumGUIUtilities talk with our embedded proxies over a SAFS RMI bridge.  
		 * The local RMI server (AServerImpl) is instanced if not already set.
		 * <p>
		 * Note that there is a known circular execution between getTRDData and 
		 * getGUIUtilities if neither was previously set.  Each routine 
		 * calls the other which may result in a second call to the other. 
		 * This has not been a problem.
		 * 
		 * @return DDGUIUtilities which should be an instanceof org.safs.selenium.SeleniumGUIUtilities.
		 * 
		 * @see JavaHook#getGUIUtilities()
		 * @see #getHelper()
		 * @see #getTRDData()
		 * @see SeleniumGUIUtilities
		 * @see SServerImpl
		 */
		public DDGUIUtilities getGUIUtilities() {
			if (utils==null) {
				if(RUNNING_SPC){
					utils=new SPC();
					((SPC) utils).setHook(this);
				}else
					utils=new SeleniumGUIUtilities();
				utils.setSTAFHelper(getHelper());
				utils.setTestRecordData(getTRDData());
			}
			return utils;
		}


		/**
		 * Use this method to retrieve/create the current/default LogUtilities instance.
		 * If the LogUtilities has not been set this routine will instance a 
		 * new org.safs.LogUtilities.  
		 * 
		 * @return LogUtilities which should be an instanceof org.safs.LogUtilities.
		 * 
		 * @see JavaHook#getLogUtilities()
		 * @see org.safs.LogUtilities
		 */
		public LogUtilities getLogUtilities() {
			if(log==null) {
				try { log=new LogUtilities();} 
				catch(Exception e) {}
			}
			return log;
		}


		/**
		 * Use this method to retrieve/create the current/default ProcessRequest instance.
		 * If the ProcessRequest has not been set this routine will instance a 
		 * new org.safs.ProcessRequest and populate it with the LogUtilities via 
		 * getLogUtilities(), a TestRecordHelper via getTRDData(), and a new TestStepProcessor.  
		 * Note that the call to getTRDData() may force the instantiation of the default 
		 * ATestRecordHelper if one has not already been set.  A call to getLogUtilities() 
		 * may also force the instantiation of the default LogUtilities if one has not already 
		 * been set.
		 * 
		 * @return ProcessRequest which should be an instanceof org.safs.ProcessRequest
		 * 
		 * @see JavaHook#getRequestProcessor()
		 * @see org.safs.ProcessRequest
		 * @see STestRecordHelper
		 * @see #getTRDData()
		 * @see #getLogUtilities()
		 * @see org.safs.LogUtilities
		 * @see org.safs.TestStepProcessor
		 */
		public ProcessRequest getRequestProcessor() {

			if(processor==null){
		    	processor = new ProcessRequest(
	            getTRDData(),                 // TestRecordHelper
	            getLogUtilities());           // no custom test step support
	            
	            Processor linked = processor.getDriverCommandProcessor();
				linked.setLogUtilities(getLogUtilities());
				linked.setTestRecordData(getTRDData());
	            
	            Processor dc = new DCDriverCommand();
	            dc.setChainedProcessor(linked);
	            processor.setDriverCommandProcessor(dc);
			}
			return processor;
		}
		
		public void start(){
			String path = System.getProperty("safs.config.paths");
			String [] paths = new String[0];
			if(path != null){
				paths = path.split(File.pathSeparator);
			} else {
				String msg = "SAFS configuration paths are not set.  Aborting.";
				Log.error(msg);
				System.err.println(msg);
				return;
			}
			
			ConfigureInterface config = null;
			for(int i = 0; i < paths.length; i++){
				File f = new File(paths[i]);
				if(f.exists()){
					if(config == null){
						config = new ConfigureFile(f);
					} else {
						config.addConfigureInterface(new ConfigureFile(f));
					}
				}
				
			}
			((STestRecordHelper)data).setConfig(config);

			// The real internet gateway
			String gatewayhost = config.getNamedValue(DriverConstant.SECTION_SAFS_SELENIUM,"GATEWAYHOST");
			if((gatewayhost==null)||(gatewayhost.length()==0)) gatewayhost=config.getNamedValue(DriverConstant.SECTION_SAFS_SELENIUM,"PROXY");
			String gatewayport = config.getNamedValue(DriverConstant.SECTION_SAFS_SELENIUM,"GATEWAYPORT");
			if((gatewayport==null)||(gatewayport.length()==0)) gatewayport=config.getNamedValue(DriverConstant.SECTION_SAFS_SELENIUM,"PORT");

			if((gatewayhost!=null)&&(gatewayhost.length()>0)) System.setProperty("http.proxyHost",gatewayhost);
			if((gatewayport!=null)&&(gatewayport.length()>0)) System.setProperty("http.proxyPort",gatewayport);
			
			int intPort = DEFAULT_INT_SELENIUMPORT;
			String seleniumPort = config.getNamedValue(DriverConstant.SECTION_SAFS_SELENIUM,"SELENIUMPORT");
			if ((seleniumPort!=null)&&(seleniumPort.length()>0)) {
				try{ intPort = Integer.parseInt(seleniumPort);}
				catch(NumberFormatException nfe){
					Log.warn("SAFS_SELENIUM SELENIUMPORT=\""+ seleniumPort +"\" invalid and ignored...");
				}
			}
			
			String debuglog = config.getNamedValue(DriverConstant.SECTION_SAFS_SELENIUM,"DEBUGLOG");
			String templog = debuglog;
			if((debuglog!=null)&&(debuglog.length()==0)) debuglog = null;
			File logfile = null;
			if (debuglog!=null){
				logfile = new CaseInsensitiveFile(debuglog).toFile();
				if (! logfile.isDirectory()){
					if(! logfile.exists()){ // if logfile exists then it is valid
						File parentdir = logfile.getParentFile();
						if (parentdir == null){ //if no parent dir then it must be invalid
							debuglog = null;
						}else{
							// parent dir must exist
							if (! parentdir.exists()) debuglog = null;
						}
					}
				}else{
					debuglog = null;
				}
				if (debuglog==null){
					Log.warn("SAFS_SELENIUM DEBUGLOG=\""+ templog +"\" invalid and ignored...");
					logfile = null;
				}
			}
			try{
				RemoteControlConfiguration seleConfig = new RemoteControlConfiguration();
				if(logfile!=null) seleConfig.setLogOutFile(logfile);
//				seleConfig.setProxyInjectionModeArg(true);
				seleConfig.setTimeoutInSeconds(1000*60*5);// 5 minutes?
				seleConfig.setTrustAllSSLCertificates(true);
				seleConfig.setPort(intPort);
				
				//Inject user custom javascript code
				URL userExtensionsJS = ClassLoader.getSystemResource(SAFS_USER_EXTENSIONS);
				if(userExtensionsJS==null){
					Log.warn("Can NOT get the custom Javascript file, fail to inject it to selenium server.");
				}else{
					Log.debug("User Extensions JavaScript File: "+userExtensionsJS.getFile());
					File user_extensions_js = new File(userExtensionsJS.getFile());
					
					if (user_extensions_js.exists()) {
						seleConfig.setUserExtensions(user_extensions_js);
					}else{
						Log.warn("User Extensions file doesn't exist: " + user_extensions_js.getAbsolutePath());
						
						String tmpdir = System.getProperty("java.io.tmpdir");
						if(tmpdir==null){
							File tmpFile = File.createTempFile("user-extensions",".js");
							temp_user_extensions = new File(tmpFile.getParent()+File.separator+"user-extensions.js");
							tmpFile.delete();
						}else{
							temp_user_extensions = new File(tmpdir+File.separator+"user-extensions.js");
						}
						temp_user_extensions.deleteOnExit();
						Log.debug("SeleniumJavaHook user-extensions injection commencing from: "+ temp_user_extensions.getAbsolutePath());
						BufferedReader _reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(SAFS_USER_EXTENSIONS)));
						BufferedWriter _writer = new BufferedWriter(new FileWriter(temp_user_extensions));
						String _line = null;			
						while(_reader.ready()){
							_line = _reader.readLine();
							_writer.write(_line);
//							Log.debug("    "+ _line);
							_writer.newLine();
						}
						_writer.flush();
						_writer.close();
						_reader.close();
						seleConfig.setUserExtensions(temp_user_extensions);
					}
					
//					seleConfig.setProxyInjectionModeArg(true);
//					seleConfig.setUserJSInjection(true);
					Log.debug("user extensions: "+seleConfig.getUserExtensions().getAbsolutePath());
				}
				
				_server = new SeleniumServer(seleConfig);
				Log.debug("Proxy Injection Mode: "+_server.getConfiguration().getProxyInjectionModeArg());
				
				//We should call boot() instead of start(), so that the injected Javascript can be used.
				_server.boot();
				
				//After the selenium has boot up, we set a STAF Variable to inform engine that we are ready
				helper.setSTAFVariable(SAFS_SELENIUM_SERVER_BOOTUP_READY, "true");
			}catch(Exception e){
				Log.error("Error starting the selenium server." +e.getMessage(),e);
				//Should we exit?
			}
			try{
				if(RUNNING_SPC)
					new SPCGUI((SPC)this.utils);
				super.start();	
			}
			// should never happen?
			catch(Exception x){
				Log.debug(x.getMessage());
				stopJavaHOOK();
			}
		}

		/**
		 * Used by SPC to imitate sending a SHUTDOWN_RECORD to JavaHook<br>
		 * who is waiting in getNextHookTestEvent() for a record-dispatched event.<br>
		 */
		public void stopJavaHOOK(){
	    	Log.info(" processing user-initiated test abort shutdown...");
	        try{
	            if (helper != null){
	            	//Tell the JavaHook to stop running
	            	data.setInputRecord(SHUTDOWN_RECORD);
	            	//If the service SAFSVAR has not been started, method postNextHookTestEvent() 
	            	//will throw exception, as it will store the test-record by SAFSVAR.
	            	helper.postNextHookTestEvent(semaphore_name,STAFHelper.SAFS_HOOK_TRD, data);
	            }
	        }catch(SAFSException ex){
	            Log.error("*** ERROR *** during User Abort Test processing: "+ ex.getMessage(),ex);
	        }finally{
	            try {
					helper.resetHookEvents(semaphore_name);
					helper.postEvent(semaphore_name +"Shutdown");
					helper.releaseSTAFMutex(STAFHelper.SAFS_HOOK_TRD+"TRD");
				} catch (SAFSException e) {
					Log.error("*** ERROR *** during User Abort Test processing: "+ e.getMessage(),e);
				}
				if (hook_shutdown() && allowSystemExit()) System.exit(0);
	        }
		}
		
		  /**
		   * Perform final hook shutdown activities.
		   * This is called before allowSystemExit.
		   * @return the value returned from super.hook_shutdown()
		   * @see JavaHook#hook_shutdown()
		   * @see #allowSystemExit()
		   */
		  protected boolean hook_shutdown(){
		  	//shutdown Selenium Server
		  	Log.info("SAFS SeleniumJavaHook is shutting down.");
		  	_server.stop();
			try{
				helper.setSTAFVariable(SAFS_SELENIUM_SERVER_BOOTUP_READY, "false");
				temp_user_extensions.delete();
			}catch(Exception x){
				Log.warn("SeleniumJavaHook shutdown: met Exception "+x.getMessage());
			}
		  	return super.hook_shutdown();
		  }
		  		
		/**
		 * Launches a default instance of the SAFS/Selenium engine.
		 * The "hook" is instanced and registered with STAF--which must already be running.
		 * The hook is started and will show up as "Ready" to SAFS Drivers once initialization 
		 * is complete.
		 * <p>
		 * Assuming everything is properly CLASSPATHed, the default SAFS/Selenium Engine can be 
		 * launched simply with:
		 * <ul>
		 * java org.safs.selenium.SeleniumJavaHook
		 * </ul>
		 */
		public static void main (String[] args) {
			Log.ENABLED = true;
			Log.setLogLevel(Log.DEBUG);
			
			if(args.length > 0 && args[0].equalsIgnoreCase("SPC")){
				RUNNING_SPC = true;
				System.out.println("Selenium Process Container starting...");
				LogUtilities logs=new LogUtilities();
				SeleniumJavaHook hook = new SeleniumJavaHook(SELENIUM_COMMANDS, logs);
				hook.setHelper(SELENIUM_COMMANDS);
				hook.getTRDData();
				hook.getRequestProcessor();
				// HOOK INITIALIZATION COMPLETE
				hook.start();
			} else {
				System.out.println("SAFS/Selenium Hook starting...");
				LogUtilities logs=new LogUtilities();
				
				SeleniumJavaHook hook = new SeleniumJavaHook(SELENIUM_COMMANDS, logs);
				hook.setHelper(SELENIUM_COMMANDS);
				hook.getTRDData();
				hook.getRequestProcessor();
				// HOOK INITIALIZATION COMPLETE
				hook.start();
			}
		}
	}

