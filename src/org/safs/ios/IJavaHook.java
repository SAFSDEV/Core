/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.ios;

import org.safs.DDGUIUtilities;
import org.safs.JavaHook;
import org.safs.Log;
import org.safs.ProcessRequest;
import org.safs.Processor;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.TestRecordHelper;
import org.safs.logging.LogUtilities;
import org.safs.tools.drivers.ConfigureInterface;
import org.safs.tools.drivers.DriverConstant;

/**
 * This class is our STAF-enabled JavaHook for the SAFS "IOS" Engine.
 * This is the class that registers with STAF and responds to the STAF-based 
 * SAFS Engine protocol defined at http://sourceforge.net/docman/display_doc.php?docid=17505&group_id=56751 
 * <p>
 * The STAF name predefined for this IOS Engine is "SAFS/IOS".
 * <p>
 * As of this writing, this hook uses standard org.safs.LogUtilities.
 * <p>
 * The SAFS/IOS engine does require IOS-specific implementations of 
 * <ul>
 * <li>org.safs.ios.ITestRecordHelper
 * <li>org.safs.ios.IGUIUtilities
 * </ul>
 * <p>
 * If the engine is being launched manually or via a batch file or script then the following  
 * Java System property MUST be set on the Java command line:
 * <p>
 * <i>testpath</i> is the file path to the test configuration INI file,<br>
 * <i>projectpath</i> is the file path to the project configuration INI file,<br>
 * <i>driverpath</i> is the file path to the SAFS driver configuration INI file.
 * <p>
 * <ul><pre>
 * -Dsafs.config.paths=&lt;testpath>:&lt;projectpath>:&lt;driverpath>
 * 
 * Example: 
 * -Dsafs.config.paths=/Library/safs/Project/iostest.ini:/Library/safs/Project/safstid.ini:/Library/safs/safstid.ini
 * </pre></ul>
 * <p>
 * At least one INI file path must be specified or the engine will abort due to insufficient 
 * configuration information being provided.
 * <p>
 * The configuration file(s) must minimally provide the following information:
 * <ul><p>
 * <li>PROJECT=&lt;path to SAFS IOS Instruments project space>
 * </ul>
 * <p>
 * The configuration file(s) can also provide the following:
 * <ul><p>
 * <li>TEMPLATE=path to IOS Instruments tracetemplate (not used)
 * <p>
 * <li>ASCRIPTS=root dir for SAFS AppleScript (Ex: /Libary/SAFS/IOS/ascript/)
 * <li>ASINSTRUMENTS_LAUNCH=relative path to Instruments launch script (Ex: Instruments/launchInstrumentsTemplate.scpt)
 * <li>ASINSTRUMENTS_START=relative path to Instruments Record Trace (Ex: Instruments/startInstrumentsTrace.scpt)
 * <li>ASINSTRUMENTS_RESTART=relative path to Instruments StartScript (Ex: Instruments/restartInstrumentsScript.scpt)
 * <li>ASINSTRUMENTS_STOP=relative path to Instruments shutdown script (Ex: Instruments/stopInstrumentsTrace.scpt)
 * <p>
 * <li>INSTRUMENTS_APPSUPPORT=root dir for Instruments app data <br>
 * (Ex:/Users/[username]/Library/Application Support/Instruments/)
 * <li>INSTRUMENTS_PREFS=root dir for SAFS Instruments Assets (Ex: /Library/SAFS/IOS/instruments/)
 * <li>INSTRUMENTS_RUNTIME_PREFS=relative path to Instruments SAFSRuntime Prefs (Ex: recent/SAFSRuntime)
 * <li>INSTRUMENTS_PROCESSCONTAINER_PREFS=relative path to Instruments ProcessContainer Prefs (Ex: recent/ProcessContainer)
 * <p>
 * <li>INSTRUMENTS_PREVIOUSSCRIPT_FILE=name of active Instruments Prefs file (Ex: PreviousScripts)
 * <li>INSTRUMENTS_SAFSBACKUP_FILE=name of SAFS Prefs backup file (Ex: SAFSBackupScripts)
 * <p>
 * <li>JSCRIPTS=root dir for IOS UIAutomation JavaScript (Ex: /Library/SAFS/IOS/jscript/)
 * <li>JSRUNTIME=relative path to main SAFS JavaScript Wrapper/Hook (Ex: SAFSRuntime.js)
 * <li>JSPROCESSCONTAINER=relative path to SAFS ProcessContainer JS (Ex: ProcessContainer.js)
 * <li>JSTARTUP=relative path to SAFS JS startup import (Ex: Utilities/startup.js)
 * <p>
 * <li>IBTIMAGES=root dir for SAFS internal IBT images (Ex: /Libary/SAFS/IOS/ibt/images/)
 * <li>IBTSTARTSCRIPT=relative path to StartScript image(s) (Ex: startscript/StartScript.bmp)
 * </ul>
 * <p>
 * The configuration file(s) can optionally also contain other relevant information as described in
 * the SAFSIOS Java Engine.  Also see 
 * {@link <a href="http://safsdev.sf.net/sqabasic2000/JSAFSFrameworkContent.htm#configfile">SAFSTID Config Files</a>}
 * for more information.
 * <p>
 * Assuming everything is properly CLASSPATHed and the config file(s) have proper settings 
 * then the Engine can then be launched with:
 * <ul><p>
 * java -Dsafs.config.paths=&lt;paths> org.safs.ios.IJavaHook
 * </ul>
 * <br>   JUN 30, 2011    (CANAGL) Original Release.
 * @see SeleniumGUIUtilities
 * @see STestRecordHelper
 * @see org.safs.jvmagent.JVMAgentTestStepProcessor
 */
public class IJavaHook extends JavaHook {

		/** Our predefined STAF process name: "SAFS/IOS" */
		public static final String IOS_COMMANDS = "SAFS/IOS";

		/**
		 * No-arg constructor for IJavaHook
		 * Simply calls the super()
		 */
		public IJavaHook() {
			super();
		}

		/**
		 * Constructor for IJavaHook
		 * Simply calls super(process_name)
		 * 
		 * @param process_name the ID to use when registering with STAF.
		 * Normally this would be our predefined name "SAFS/IOS".
		 */
		public IJavaHook(String process_name) {
			super(process_name);
		}

		/**
		 * Constructor for IJavaHook
		 * Simply calls super(process_name, logs)
		 * 
		 * @param process_name the ID to use when registering with STAF.
		 * Normally this would be our predefined name "SAFS/IOS".
		 * 
		 * @param logs the LogUtilities to be used by the Engine.  Currently we can 
		 * use the default org.safs.LogUtilities
		 * 
		 * @see org.safs.LogUtilites
		 */
		public IJavaHook(String process_name, LogUtilities logs) {
			super(process_name, logs);
		}

		/**
		 * Constructor for IJavaHook
		 * Simply calls super(process_name, trd_name)
		 * 
		 * @param process_name the ID to use when registering with STAF.
		 * Normally this would be our predefined name "SAFS/IOS".
		 * 
		 * @param trd_name -- The root name for specific TestRecordData to reference 
		 * in STAF.  This should typically be STAFHelper.SAFS_HOOK_TRD which is used 
		 * by default.
		 * 
		 * @see org.safs.STAFHelper#SAFS_HOOK_TRD
		 */
		public IJavaHook(String process_name, String trd_name) {
			super(process_name, trd_name);
		}


		/**
		 * Constructor for IavaHook
		 * Simply calls super(process_name, trd_name, logs)
		 * 
		 * @param process_name the ID to use when registering with STAF.
		 * Normally this would be our predefined name "SAFS/IOS".
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
		public IJavaHook(String process_name, String trd_name, 
		                      LogUtilities logs) {
		                      	
			super(process_name, trd_name, logs);
		}

		/**
		 * Advanced Constructor for IJavaHook.
		 * Simply calls super(process_name, trd_name, logs, trd_data, gui_utils, aprocessor)
		 * 
		 * @param process_name the ID to use when registering with STAF.
		 * Normally this would be our predefined name "SAFS/IOS".
		 * 
		 * @param trd_name -- The root name for specific TestRecordData to reference 
		 * in STAF.  This should typically be STAFHelper.SAFS_HOOK_TRD which is used 
		 * by default.
		 * 
		 * @param logs the LogUtilities to be used by the Engine.  
		 * 
		 * @param trd_data -- ITestRecordHelper to hold TestRecordData.  
		 * The IOS engine expects an instance of ITestRecordHelper.
		 * 
		 * @param gui_utils -- IGuiUtilities the hook will use for handling components.
		 * The IOS engine expects an instance of IGUIUtilities.
		 * 
		 * @param aprocessor -- ProcessRequest object the hook will use for routing records.
		 * The IOS engine uses the standard ProcessRequest class and a standard TestStepProcessor.
		 * 
		 * @see org.safs.STAFHelper#SAFS_HOOK_TRD
		 * @see org.safs.LogUtilites
		 * @see ITestRecordHelper
		 * @see IGUIUtilities
		 * @see org.safs.ProcessRequest
		 * @see org.safs.TestStepProcessor
		 */
		public IJavaHook (String process_name, String trd_name, 
		                     LogUtilities logs,
		                     TestRecordHelper trd_data,
		                     DDGUIUtilities gui_utils,
		                     ProcessRequest aprocessor) {

			super(process_name, trd_name, logs, trd_data, gui_utils, aprocessor);
		}


		/**
		 * Use this method to retrieve/create the current/default TestRecordHelper instance.
		 * If the TestRecordHelper has not been set this routine will instance a 
		 * new org.safs.ios.ITestRecordHelper and populate it with a STAFHelper via 
		 * getHelper() and a DDGUIUtilities via getGUIUtilities.  Note that the call 
		 * to getGUIUtilities may force the instantiation of the default IGUIUtilities 
		 * if one has not already been set.  
		 * <p>
		 * Note that there is a known circular execution between getTRDData and 
		 * getGUIUtilities if neither was previously set.  Each routine 
		 * calls the other which may result in a second call to the other. 
		 * This has not been a problem.
		 * 
		 * @return TestRecordHelper which should be an instanceof org.safs.ios.ITestRecordHelper
		 * 
		 * @see ITestRecordHelper
		 * @see #getHelper()
		 * @see #getGUIUtilities()
		 * @see JavaHook#getTRDData()
		 */
		public TestRecordHelper getTRDData() {
			if (data==null) {
					data=new ITestRecordHelper();
					data.setSTAFHelper(getHelper());
					data.setDDGUtils(getGUIUtilities());
				}
			return data;
		}


		/**
		 * Use this method to retrieve/create the current/default DDGUIUtilities instance.
		 * If the DDGUIUtilities has not been set this routine will instance a 
		 * new org.safs.ios.IGUIUtilities and populate it with a STAFHelper via 
		 * getHelper() and a TestRecordHelper via getTRDData().  Note that the call 
		 * to getTRDData() may force the instantiation of the default ITestRecordHelper 
		 * if one has not already been set.  
		 * <p>
		 * Note that there is a known circular execution between getTRDData and 
		 * getGUIUtilities if neither was previously set.  Each routine 
		 * calls the other which may result in a second call to the other. 
		 * This has not been a problem.
		 * 
		 * @return DDGUIUtilities which should be an instanceof org.safs.ios.IGUIUtilities.
		 * 
		 * @see JavaHook#getGUIUtilities()
		 * @see #getHelper()
		 * @see #getTRDData()
		 * @see IGUIUtilities
		 */
		public DDGUIUtilities getGUIUtilities() {
			if (utils==null) {
				utils=new IGUIUtilities();
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
		 * getLogUtilities(), a TestRecordHelper via getTRDData(), and 
		 * new IDriverCommand and ITestStepProcessor processors.<p>  
		 * Note that the call to getTRDData() may force the instantiation of the default 
		 * ITestRecordHelper if one has not already been set.  A call to getLogUtilities() 
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
		 * @see org.safs.ios.IDriverCommand
		 * @see org.safs.ios.ITestStepProcessor
		 */
		public ProcessRequest getRequestProcessor() {

			if(processor==null){
		    	processor = new ProcessRequest(
	            getTRDData(),                 // TestRecordHelper
	            getLogUtilities());           // no custom test step support
	            
	            Processor dc = new IDriverCommand();
	            dc.setLogUtilities(getLogUtilities());
	            dc.setTestRecordData(getTRDData());
	            processor.setDriverCommandProcessor(dc);
	            
	            Processor cf = new ITestStepProcessor();
	            cf.setLogUtilities(getLogUtilities());
	            cf.setTestRecordData(getTRDData());
	            processor.setTestStepProcessor(cf);
			}
			return processor;
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
		  	Log.info(IOS_COMMANDS +" is shutting down.");
		  	
		  	// do stuff in here
		  	
		  	return super.hook_shutdown();
		  }

		/**
		 * Insert this SAFS Engine hook into the STAF system.  
		 * The hook will then be enabled and ready for test events from any STAF client
		 * using the correct Event protocols.
		 **/
		public void start () {
			
			ConfigureInterface config = getTRDData().getConfig();
			String SAFS_IOS = DriverConstant.SECTION_SAFS_IOS;			
			String value = null;
			
			value = config.getNamedValue(SAFS_IOS, "Project");
			if (value == null || value.length()==0){
				String msg = "REQUIRED SAFS_IOS 'Project' configuration setting is invalid or missing. SAFS/IOS will not start.";
				Log.debug("");
				System.out.println("SAFS/IOS Aborting: "+ msg);
				return;
			}else{ 
				Log.debug("Using config file 'Project' setting: "+ value);
				Utilities.ROOT_INSTRUMENTS_PROJECT_DIR = value;
			}
			value = config.getNamedValue(SAFS_IOS, "Template");
			if(value != null) {
				Log.debug("Using config file 'Template' setting: "+ value);
				Utilities.DEFAULT_INSTRUMENTS_TEMPLATE = value;
			}			
			value = config.getNamedValue(SAFS_IOS, "ASCRIPTS");
			if(value != null) {
				Log.debug("Using config file 'ASCRIPTS' setting: "+ value);
				Utilities.ROOT_ASCRIPTS_DIR = value;
			}			
			value = config.getNamedValue(SAFS_IOS, "ASINSTRUMENTS_LAUNCH");
			if(value != null) {
				Log.debug("Using config file 'ASINSTRUMENTS_LAUNCH' setting: "+ value);
				Utilities.LAUNCH_INSTRUMENTS_ASCRIPT = value;
			}
			value = config.getNamedValue(SAFS_IOS, "ASINSTRUMENTS_START");
			if(value != null) {
				Log.debug("Using config file 'ASINSTRUMENTS_START' setting: "+ value);
				Utilities.START_INSTRUMENTS_ASCRIPT = value;
			}
			value = config.getNamedValue(SAFS_IOS, "ASINSTRUMENTS_RESTART");
			if(value != null) {
				Log.debug("Using config file 'ASINSTRUMENTS_RESTART' setting: "+ value);
				Utilities.LOOP_INSTRUMENTS_ASCRIPT = value;
			}
			value = config.getNamedValue(SAFS_IOS, "ASINSTRUMENTS_STOP");
			if(value != null) {
				Log.debug("Using config file 'ASINSTRUMENTS_STOP' setting: "+ value);
				Utilities.STOP_INSTRUMENTS_ASCRIPT = value;
			}
			value = config.getNamedValue(SAFS_IOS, "INSTRUMENTS_APPSUPPORT");
			if(value != null) {
				Log.debug("Using config file 'INSTRUMENTS_APPSUPPORT' setting: "+ value);
				Utilities.INSTRUMENTS_APPLICATIONSUPPORT_DIR = value;
			}
			value = config.getNamedValue(SAFS_IOS, "INSTRUMENTS_PREFS");
			if(value != null) {
				Log.debug("Using config file 'INSTRUMENTS_PREFS' setting: "+ value);
				Utilities.ROOT_INSTRUMENTS_DIR = value;
			}
			value = config.getNamedValue(SAFS_IOS, "INSTRUMENTS_RUNTIME_PREFS");
			if(value != null) {
				Log.debug("Using config file 'INSTRUMENTS_RUNTIME_PREFS' setting: "+ value);
				Utilities.SAFSRUNTIME_INSTRUMENTS_PREFS = value;
			}
			value = config.getNamedValue(SAFS_IOS, "INSTRUMENTS_PROCESSCONTAINER_PREFS");
			if(value != null) {
				Log.debug("Using config file 'INSTRUMENTS_PROCESSCONTAINER_PREFS' setting: "+ value);
				Utilities.PROCESSCONTAINER_INSTRUMENTS_PREFS = value;
			}
			value = config.getNamedValue(SAFS_IOS, "INSTRUMENTS_PREVIOUSSCRIPTS_FILE");
			if(value != null) {
				Log.debug("Using config file 'INSTRUMENTS_PREVIOUSSCRIPTS_FILE' setting: "+ value);
				Utilities.INSTRUMENTS_PREVIOUSSCRIPTS_FILE = value;
			}
			value = config.getNamedValue(SAFS_IOS, "INSTRUMENTS_SAFSBACKUP_FILE");
			if(value != null) {
				Log.debug("Using config file 'INSTRUMENTS_SAFSBACKUP_FILE' setting: "+ value);
				Utilities.INSTRUMENTS_BACKUPSCRIPTS_FILE = value;
			}
			value = config.getNamedValue(SAFS_IOS, "JSCRIPTS");
			if(value != null) {
				Log.debug("Using config file 'JSCRIPTS' setting: "+ value);
				Utilities.ROOT_JSCRIPTS_DIR = value;
			}
			value = config.getNamedValue(SAFS_IOS, "JSRUNTIME");
			if(value != null) {
				Log.debug("Using config file 'JSRUNTIME' setting: "+ value);
				Utilities.DEFAULT_SAFSRUNTIME_SCRIPT = value;
			}
			value = config.getNamedValue(SAFS_IOS, "JSPROCESSCONTAINER");
			if(value != null) {
				Log.debug("Using config file 'JSPROCESSCONTAINER' setting: "+ value);
				Utilities.DEFAULT_PROCESSCONTAINER_SCRIPT = value;
			}
			value = config.getNamedValue(SAFS_IOS, "JSTARTUP");
			if(value != null) {
				Log.debug("Using config file 'JSTARTUP' setting: "+ value);
				Utilities.DEFAULT_JSSTARTUP_IMPORT = value;
			}
			value = config.getNamedValue(SAFS_IOS, "IBTIMAGES");
			if(value != null) {
				Log.debug("Using config file 'IBTIMAGES' setting: "+ value);
				Utilities.ROOT_IBT_IMAGES_DIR = value;
			}
			value = config.getNamedValue(SAFS_IOS, "IBTSTARTSCRIPT");
			if(value != null) {
				Log.debug("Using config file 'IBTSTARTSCRIPT' setting: "+ value);
				Utilities.STARTSCRIPT_IMAGE = value;
			}
			
			super.start();
		}
		  
		/**
		 * Launches a default instance of the SAFS/IOS engine.
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
			
			System.out.println(IOS_COMMANDS +" Hook starting...");
			LogUtilities logs=new LogUtilities();
			
			IJavaHook hook = new IJavaHook(IOS_COMMANDS, logs);
			hook.setHelper(IOS_COMMANDS);
			hook.getTRDData();
			
			//Log.setHelper(hook.getHelper());
			
			hook.getRequestProcessor();
			hook.initConfigPaths();
			// HOOK INITIALIZATION COMPLETE
			hook.start();
		}
	}

