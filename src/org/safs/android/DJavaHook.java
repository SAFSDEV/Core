/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.android;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.safs.DDGUIUtilities;
import org.safs.JavaHook;
import org.safs.JavaSocketsHook;
import org.safs.JavaSocketsUtils;
import org.safs.Log;
import org.safs.ProcessRequest;
import org.safs.Processor;
import org.safs.SAFSRuntimeException;
import org.safs.SocketTestRecordHelper;
import org.safs.TestRecordHelper;
import org.safs.android.DUtilities;
import org.safs.android.remotecontrol.SAFSRemoteControl;
import org.safs.android.remotecontrol.SAFSWorker;
import org.safs.logging.AbstractLogFacility;
import org.safs.logging.LogUtilities;
import org.safs.text.FAILStrings;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.ConfigureInterface;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.stringutils.StringUtilities;

import com.jayway.android.robotium.remotecontrol.solo.LogsInterface;

/**
 * This class is our STAF-enabled JavaHook for the SAFS "DROID" Engine.
 * This is the class that registers with STAF and responds to the STAF-based 
 * SAFS Engine protocol defined at http://sourceforge.net/docman/display_doc.php?docid=17505&group_id=56751 
 * <p>
 * The STAF name predefined for this Droid Engine is "SAFS/DROID".
 * <p>
 * As of this writing, this hook uses standard org.safs.LogUtilities.
 * <p>
 * The SAFS/DROID engine does require Droid-specific implementations of 
 * <ul>
 * <li>org.safs.android.DTestRecordHelper
 * <li>org.safs.android.DGUIUtilities
 * <li>org.safs.android.DDriverCommand
 * <li>org.safs.android.DTestStepProcessor
 * <li>org.safs.android.DEngineCommandProcessor
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
 * -Dsafs.config.paths=&lt;testpath>;&lt;projectpath>;&lt;driverpath>
 * 
 * Example: 
 * 
 * -Dsafs.config.paths=C:\safs\samples\Droid\droidtest.ini;C:\safs\samples\Droid\safstid.ini;C:\safs\safstid.ini
 * </pre></ul>
 * <p>
 * At least one INI file path must be specified or the engine will abort due to insufficient 
 * configuration information being provided.
 * <p>
 * The configuration file(s) must minimally provide the following information:
 * <ul><p>
 * <li>DroidProject=&lt;path to SAFS/DROID automation project space>
 * </ul><p>
 * The configuration file(s) can also provide the following:
 * <ul>
 * <p>
 * <li>Console2Debug=True/False enable engine output to console and debug log.
 * <p>
 * <li>ShutdownDelay=N seconds (default: 0) Delay in seconds to delay the shutdown to allow remote process completion.
 * <p>
 * <li>EMULATOR_AVD=name of an emulator avd to start upon launch, if any.
 * <p>
 * <li>ANDROID-SDK=root dir for Android SDK  (default: "C:\Program Files\Android\android-sdk")
 * <p>
 * <li>ANDROID-TOOLS=root dir for Android SDK Tools  (default: "C:\Program Files\Android\android-sdk\tools")
 * <p>
 * <li>FORCESTOP=True/False (default=True) some devices require an APK uninstall to force stop the application. 
 * <p>
 * <li>FORCECLEAR=True/False (default=False) if true, FORCESTOP will also clear app data and cache. 
 * <p>
 * <li>TCPMessengerAPK=path to TCP Messenger APK -- (default: "C:\SAFS\samples\droid\SAFSTCPMessenger\bin\SAFSTCPMessenger-debug.apk")<br>
 * Providing this parameter enforces existence validation AND informs the engine to install this APK.  
 * If not provided, the engine will assume the desired TCPMessenger APK is already installed on the device.
 * <p>
 * <li>TCPMessengerPackage=Java package for TCPMessenger -- (default: "org.safs.android.messenger")<br>
 * Normally don't need to provide this unless using a custom (non-SAFS) TCPMessenger.
 * <p>
 * <li>TestRunnerSource=path to InstrumentationTestRunner project  (Ex: "C:\SAFS\samples\droid\SAFSTestRunner")<br>
 * Providing this parameter enforces existence validation AND informs the engine to rebuild the TestRunner APK 
 * before installing it.  
 * <p>
 * <li>TestRunnerAPK=path to InstrumentationTestRunner APK  (Ex: "C:\SAFS\samples\droid\SAFSTestRunner\bin\SAFSTestRunner-debug.apk")<br>
 * Providing this parameter enforces existence validation AND informs the engine to install this APK.  
 * If not provided, the engine will assume the desired TestRunner APK is already installed on the device.<br>
 * This setting (or default being valid) is required if using TestRunnerSource to rebuild the TestRunner prior to execution.
 * <p>
 * <li>TestRunnerPackage=Java package for TestRunner  (default: "org.safs.android.engine")<br>
 * This setting (or default being valid) is required if using TestRunnerSource to rebuild the TestRunner prior to execution.
 * <p>
 * <li>TestInstrument=Java package and class for TestRunner  (default: "org.safs.android.engine/org.safs.android.engine.DSAFSTestRunner")<br>
 * This setting (or default being valid) is required if using TestRunnerSource to rebuild the TestRunner prior to execution.
 * <p>
 * <li>TargetAPK=path to Target Application APK  (Ex: "C:\SAFS\samples\droid\APIDemo\bin\APIDemo-debug.apk")
 * Providing this parameter enforces existence validation AND informs the engine to install this APK.  
 * If not provided, the engine will assume the desired Target APK is already installed on the device.
 * <p>
 * <li>TargetPackage=Java package for Target APK  (Ex: "com.app.package")
 * <p>
 * <li>AntRebuildArgs=space-delimited args to pass to Ant launcher (Ex: "-noclasspath")<br>
 * The Ant launcher is platform-specific and delivered with Ant. 
 * <p>
 * <li>AntRebuildForce=True/False (default False) True will force a rebuild to occur instead of merely checking for changes to the AndroidManifest.XML
 * <p>
 * <li>PersistEmulators=True/False  (default: false) True to avoid killing emulators we might have launched.
 * <p>
 * <li>PersistJVM=True/False  (default: false) True to avoid the JVM from shutting down normally.
 * <p>
 * <li>DeviceSerial=serial number  (a specific adb -s device/emulator if multiple are present)
 * </ul>
 * <p>
 * The configuration file(s) can optionally also contain other relevant information as described in
 * the SAFSDROID Java Engine.  Also see 
 * {@link <a href="http://safsdev.sf.net/sqabasic2000/JSAFSFrameworkContent.htm#configfile">SAFSTID Config Files</a>}
 * for more information.
 * <p>
 * Assuming everything is properly CLASSPATHed and the config file(s) have proper settings 
 * then the Engine can then be launched with:
 * <ul><p>
 * java -Dsafs.config.paths=&lt;paths> org.safs.android.DJavaHook
 * </ul>
 * <br>   DEC 15, 2011    (Carl Nagle) Original Release.
 * <br>   APR 27, 2012    (Lei Wang) Modify method main(): remove the reset of semaphores
 */
public class DJavaHook extends JavaSocketsHook implements LogsInterface{

		/** Our predefined STAF process name: "SAFS/DROID" */
		public static final String DROID_COMMANDS = "SAFS/DROID";

		public static final String DEFAULT_SAFS_SERVICE_APP = "SAFSTCPMessenger\\bin\\SAFSTCPMessenger-debug.apk";
		public static final String DEFAULT_SAFS_SERVICE_PACKAGE = "org.safs.android.messenger";
		public static final String DEFAULT_TEST_RUNNER_APP = "SAFSTestRunner\\bin\\SAFSTestRunner-debug.apk";
		public static final String DEFAULT_TEST_RUNNER_PACKAGE = "org.safs.android.engine";
		public static final String DEFAULT_TEST_RUNNER_SOURCE = "SAFSTestRunner";
		public static final String DEFAULT_TEST_RUNNER_INSTRUMENT = "org.safs.android.engine/org.safs.android.engine.DSAFSTestRunner";
		
		protected String remoteComment = null;
		protected String remoteDetail = null;
		protected int remoteLogtype;
		
		protected boolean weLaunchedEmulator = false;
		protected boolean persistEmulators = false;
		protected boolean persistJVM = false;
		protected boolean _unlockEmulatorScreen = true;
		protected boolean _forceStop = true;
		protected boolean _forceClear = false;

		protected boolean _weSetTargetAPP = false;
		protected boolean _weSetTargetPackage = false;

		protected boolean portForwarding = true;
		
		/**
		 * No-arg constructor for DJavaHook
		 * Simply calls the super()
		 */
		public DJavaHook() {
			super();
		}

		/**
		 * Constructor for DJavaHook
		 * Simply calls super(process_name)
		 * 
		 * @param process_name the ID to use when registering with STAF.
		 * Normally this would be our predefined name "SAFS/DROID".
		 */
		public DJavaHook(String process_name) {
			super(process_name);
		}

		/**
		 * Constructor for DJavaHook
		 * Simply calls super(process_name, logs)
		 * 
		 * @param process_name the ID to use when registering with STAF.
		 * Normally this would be our predefined name "SAFS/DROID".
		 * 
		 * @param logs the LogUtilities to be used by the Engine.  Currently we can 
		 * use the default org.safs.LogUtilities
		 * 
		 * @see org.safs.LogUtilites
		 */
		public DJavaHook(String process_name, LogUtilities logs) {
			super(process_name, logs);
		}

		/**
		 * Constructor for DJavaHook
		 * Simply calls super(process_name, trd_name)
		 * 
		 * @param process_name the ID to use when registering with STAF.
		 * Normally this would be our predefined name "SAFS/DROID".
		 * 
		 * @param trd_name -- The root name for specific TestRecordData to reference 
		 * in STAF.  This should typically be STAFHelper.SAFS_HOOK_TRD which is used 
		 * by default.
		 * 
		 * @see org.safs.STAFHelper#SAFS_HOOK_TRD
		 */
		public DJavaHook(String process_name, String trd_name) {
			super(process_name, trd_name);
		}


		/**
		 * Constructor for DJavaHook
		 * Simply calls super(process_name, trd_name, logs)
		 * 
		 * @param process_name the ID to use when registering with STAF.
		 * Normally this would be our predefined name "SAFS/DROID".
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
		public DJavaHook(String process_name, String trd_name, 
		                      LogUtilities logs) {
		                      	
			super(process_name, trd_name, logs);
		}

		/**
		 * Advanced Constructor for DJavaHook.
		 * Simply calls super(process_name, trd_name, logs, trd_data, gui_utils, aprocessor)
		 * 
		 * @param process_name the ID to use when registering with STAF.
		 * Normally this would be our predefined name "SAFS/DROID".
		 * 
		 * @param trd_name -- The root name for specific TestRecordData to reference 
		 * in STAF.  This should typically be STAFHelper.SAFS_HOOK_TRD which is used 
		 * by default.
		 * 
		 * @param logs the LogUtilities to be used by the Engine.  
		 * 
		 * @param trd_data -- DTestRecordHelper to hold TestRecordData.  
		 * The Droid engine expects an instance of SocketTestRecordHelper.
		 * 
		 * @param gui_utils -- DGUIUtilities the hook will use for handling components.
		 * The Droid engine expects an instance of DGUIUtilities.
		 * 
		 * @param aprocessor -- ProcessRequest object the hook will use for routing records.
		 * The Droid engine uses the standard ProcessRequest class and a standard TestStepProcessor.
		 * 
		 * @see org.safs.STAFHelper#SAFS_HOOK_TRD
		 * @see org.safs.LogUtilites
		 * @see DTestRecordHelper
		 * @see DGUIUtilities
		 * @see org.safs.ProcessRequest
		 * @see org.safs.TestStepProcessor
		 */
		public DJavaHook (String process_name, String trd_name, 
		                     LogUtilities logs,
		                     SocketTestRecordHelper trd_data,
		                     DDGUIUtilities gui_utils,
		                     ProcessRequest aprocessor) {

			super(process_name, trd_name, logs, trd_data, gui_utils, aprocessor);
		}

		/**
		 * Use this method to retrieve/create the current/default TestRecordHelper instance.
		 * If the TestRecordHelper has not been set this routine will instance a 
		 * new org.safs.android.DTestRecordHelper and populate it with a STAFHelper via 
		 * getHelper() and a DGUIUtilities via getGUIUtilities.  Note that the call 
		 * to getGUIUtilities may force the instantiation of the default DGUIUtilities 
		 * if one has not already been set.  
		 * <p>
		 * Note that there is a known circular execution between getTRDData and 
		 * getGUIUtilities if neither was previously set.  Each routine 
		 * calls the other which may result in a second call to the other. 
		 * This has not been a problem.
		 * 
		 * @return TestRecordHelper which should be an instanceof org.safs.android.DTestRecordHelper
		 * 
		 * @see DTestRecordHelper
		 * @see #getHelper()
		 * @see #getGUIUtilities()
		 * @see JavaHook#getTRDData()
		 */
		public TestRecordHelper getTRDData() {
			if (data==null) {
					data=new DTestRecordHelper();
					data.setSTAFHelper(getHelper());
					data.setDDGUtils(getGUIUtilities());
				}
			return data;
		}


		/**
		 * Use this method to retrieve/create the current/default DDGUIUtilities instance.
		 * If the DDGUIUtilities has not been set this routine will instance a 
		 * new org.safs.android.DGUIUtilities and populate it with a STAFHelper via 
		 * getHelper() and a TestRecordHelper via getTRDData().  Note that the call 
		 * to getTRDData() may force the instantiation of the default DTestRecordHelper 
		 * if one has not already been set.  
		 * <p>
		 * Note that there is a known circular execution between getTRDData and 
		 * getGUIUtilities if neither was previously set.  Each routine 
		 * calls the other which may result in a second call to the other. 
		 * This has not been a problem.
		 * 
		 * @return DDGUIUtilities which should be an instanceof org.safs.android.DGUIUtilities.
		 * 
		 * @see JavaHook#getGUIUtilities()
		 * @see #getHelper()
		 * @see #getTRDData()
		 * @see DGUIUtilities
		 */
		public DDGUIUtilities getGUIUtilities() {
			if (utils==null) {
				utils=new DGUIUtilities();
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
		 * getLogUtilities(), a SocketTestRecordHelper via getTRDData(), and 
		 * new DDriverCommand and DTestStepProcessor processors.<p>  
		 * Note that the call to getTRDData() may force the instantiation of the default 
		 * DTestRecordHelper if one has not already been set.  A call to getLogUtilities() 
		 * may also force the instantiation of the default LogUtilities if one has not already 
		 * been set.
		 * 
		 * @return ProcessRequest which should be an instanceof org.safs.ProcessRequest
		 * 
		 * @see JavaHook#getRequestProcessor()
		 * @see org.safs.ProcessRequest
		 * @see DTestRecordHelper
		 * @see #getTRDData()
		 * @see #getLogUtilities()
		 * @see org.safs.LogUtilities
		 * @see org.safs.android.DDriverCommand
		 * @see org.safs.android.DTestStepProcessor
		 */
		public ProcessRequest getRequestProcessor() {

			if(processor==null){
		    	processor = new ProcessRequest(
	            getTRDData(),                 // TestRecordHelper
	            getLogUtilities());           // no custom test step support
	            
	            Processor dc = new DDriverCommand();
	            dc.setLogUtilities(getLogUtilities());
	            dc.setTestRecordData(getTRDData());
	            processor.setDriverCommandProcessor(dc);
	            
	            Processor cf = new DTestStepProcessor();
	            cf.setLogUtilities(getLogUtilities());
	            cf.setTestRecordData(getTRDData());
	            processor.setTestStepProcessor(cf);

	            Processor ec = new DEngineCommandProcessor();
	            ec.setLogUtilities(getLogUtilities());
	            ec.setTestRecordData(getTRDData());
	            processor.setEngineCommandProcessor(ec);
	            
			}
			return processor;
		}

		/**
		 * Invoked during start loop initialization.
		 * Creates the default instance of our abstractProtocolRunner with the hook process_name and provides 
		 * this hook as the default NamedListener for the server.  
		 * <ol>
		 * <li>normal initialization for STAFHelper, GUIUtilities, TestRecordHelper, and RequestProcessor.
		 * <li>{@link #createProtocolRunner()}
		 * <li>{@link #beforeLaunchRemoteEngine()}
		 * <li>{@link #launchRemoteEngine()}
		 * <li>{@link #afterLaunchRemoteEngine()}
		 * <li>{@link #startProtocolRunner()}
		 * <li>{@link #waitForRemoteConnection(int)}
		 * </ol>
		 * @return true to allow normal execution to proceed.
		 * Returning false will cause an abort of the engine startup procedure.
		 */
		protected boolean createProtocolRunner(){
			tcpServer = new SAFSRemoteControl();
			tcpServer.addListener(this);
			((DTestRecordHelper)data).setRemoteController(tcpServer);
			return true;
		}
		
		
		/**
		 * Perform some initial validation of SAFS_DROID project settings and availability of 
		 * test assets defined there.
		 * 
		 * Invoked during start loop initialization.
		 * <ol>
		 * <li>normal initialization for STAFHelper, GUIUtilities, TestRecordHelper, and RequestProcessor.
		 * <li>{@link #createProtocolRunner()}
		 * <li>{@link #beforeLaunchRemoteEngine()}
		 * <li>{@link #launchRemoteEngine()}
		 * <li>{@link #afterLaunchRemoteEngine()}
		 * <li>{@link #startProtocolRunner()}
		 * <li>{@link #waitForRemoteConnection(int)}
		 * </ol>
		 */
		public boolean beforeLaunchRemoteEngine(){
			ConfigureInterface config = getTRDData().getConfig();
			String SAFS_DROID = DriverConstant.SECTION_SAFS_DROID;			
			String value = null;
			CaseInsensitiveFile afile = null;
			
			value = config.getNamedValue(SAFS_DROID, "DROIDProject");
			if (value != null){
				Log.debug("Trying config file 'DROIDProject' setting: "+ value);
				DUtilities.ROOT_DROID_PROJECT_DIR = value;
			}
			afile = new CaseInsensitiveFile(DUtilities.ROOT_DROID_PROJECT_DIR);
			if(afile.isAbsolute()&&afile.isDirectory()){
				Log.info("Using 'Project' setting: "+ DUtilities.ROOT_DROID_PROJECT_DIR);
			}else{
				String msg = "REQUIRED SAFS_DROID 'Project' configuration is NOT a valid directory. SAFS/DROID cannot start.";
				Log.debug(msg);
				System.out.println("SAFS/DROID Aborting: "+ msg);
				return false;
			}
			
			value = config.getNamedValue(SAFS_DROID, "EMULATOR_AVD");
			if(value != null) {
				Log.info("Using config file 'EMULATOR_AVD' setting: "+ value);
				DUtilities.DEFAULT_EMULATOR_AVD = value;
			}			

			value = config.getNamedValue(SAFS_DROID, "DeviceSerial");
			if(value != null) {
				Log.info("Using config file 'DeviceSerial' setting: "+ value);
				DUtilities.DEFAULT_DEVICE_SERIAL = value;
			}			
			
			value = config.getNamedValue(SAFS_DROID, "ANDROID-SDK");
			if(value != null) {
				Log.info("Using config file 'ANDROID-SDK' setting: "+ value);
				DUtilities.ROOT_DROID_SDK_DIR = value;
			}	
			afile = new CaseInsensitiveFile(DUtilities.ROOT_DROID_SDK_DIR);
			if((!afile.isAbsolute())||(!afile.isDirectory())){
				String msg = "'SDK_ROOT' configuration is invalid or missing: "+ DUtilities.ROOT_DROID_SDK_DIR;
				Log.debug(msg);
				System.out.println("SAFS/DROID Aborting: "+ msg);
				return false;
			}
			tcpServer.ROOT_DROID_SDK_DIR = value;
			Log.info("Verified SDK Directory: "+ DUtilities.ROOT_DROID_SDK_DIR);

			DUtilities.ROOT_DROID_SDK_TOOLS = DUtilities.ROOT_DROID_SDK_DIR + File.separator +"tools"+ File.separator;			
			value = config.getNamedValue(SAFS_DROID, "ANDROID-TOOLS");
			if(value != null) {
				Log.info("Using config file 'SDK_TOOLS' setting: "+ value);
				DUtilities.ROOT_DROID_SDK_TOOLS = value;
			}
			afile = new CaseInsensitiveFile(DUtilities.ROOT_DROID_SDK_DIR, DUtilities.ROOT_DROID_SDK_TOOLS);
			if(afile.isAbsolute()&& afile.isDirectory()){
				DUtilities.ROOT_DROID_SDK_TOOLS = afile.getAbsolutePath();
			}else{
				afile = new CaseInsensitiveFile(DUtilities.ROOT_DROID_SDK_TOOLS);
				if(afile.isAbsolute() && afile.isDirectory()){
					// already OK
				}else{
					String msg = "'SDK_TOOLS' configuration is invalid or missing: "+ DUtilities.ROOT_DROID_SDK_TOOLS;
					Log.debug(msg);
					System.out.println("SAFS/DROID Aborting: "+ msg);
					return false;
				}
			}
			tcpServer.ROOT_DROID_SDK_TOOLS = afile.getAbsolutePath();
			Log.info("Verified TOOLS Directory: "+ DUtilities.ROOT_DROID_SDK_TOOLS);
			
			// this should be an optional setting.
			// if not present, then no attempt to install the APK should be made
			DUtilities.installMessenger = false;
			DUtilities.SAFS_SERVICE_APP = DEFAULT_SAFS_SERVICE_APP;
			value = config.getNamedValue(SAFS_DROID, "TCPMessengerAPK");
			if(value != null) {
				Log.info("Trying config file 'TCPMessengerAPK' setting: "+ value);
				DUtilities.SAFS_SERVICE_APP = value;
				afile = new CaseInsensitiveFile(DUtilities.ROOT_DROID_PROJECT_DIR, DUtilities.SAFS_SERVICE_APP);
				if((afile.isAbsolute())&&(afile.isFile())){
					DUtilities.SAFS_SERVICE_APP = afile.getAbsolutePath();
					DUtilities.installMessenger = true;
				}else{
					afile = new CaseInsensitiveFile(DUtilities.SAFS_SERVICE_APP);
					if(afile.isAbsolute()&&afile.isFile()){
						DUtilities.SAFS_SERVICE_APP = afile.getAbsolutePath();
						DUtilities.installMessenger = true;
					}else{
						String msg = "'TCPMessengerAPK' configuration is invalid or missing: "+ DUtilities.SAFS_SERVICE_APP;
						Log.debug(msg);
						System.out.println("SAFS/DROID Aborting: "+ msg);
						return false;
					}
				}
				Log.info("Using derived 'TCPMessengerAPK' setting: "+ DUtilities.SAFS_SERVICE_APP);
			}else{
				Log.info("Assuming 'TCPMessengerAPK' is already installed on the device or emulator...");
			}
			
			DUtilities.SAFS_SERVICE_PACKAGE = DEFAULT_SAFS_SERVICE_PACKAGE;
			value = config.getNamedValue(SAFS_DROID, "TCPMessengerPackage");
			if(value != null) {
				DUtilities.SAFS_SERVICE_PACKAGE = value;
			}			
			Log.info("Using 'TCPMessengerPackage' setting: "+ DUtilities.SAFS_SERVICE_PACKAGE);

			// this should be an optional setting.
			// if not present, then no attempt to install the APK should be made
			DUtilities.installRunner = false;
			DUtilities.TEST_RUNNER_APP = DEFAULT_TEST_RUNNER_APP;
			value = config.getNamedValue(SAFS_DROID, "TestRunnerAPK");
			if(value != null) {
				Log.info("Trying config file 'TestRunnerAPK' setting: "+ value);
				DUtilities.TEST_RUNNER_APP = value;
				afile = new CaseInsensitiveFile(DUtilities.ROOT_DROID_PROJECT_DIR, DUtilities.TEST_RUNNER_APP);
				if((afile.isAbsolute())&&(afile.isFile())){
					DUtilities.TEST_RUNNER_APP = afile.getAbsolutePath();
					DUtilities.installRunner = true;
				}else{
					afile = new CaseInsensitiveFile(DUtilities.TEST_RUNNER_APP);
					if(afile.isAbsolute()&&afile.isFile()){
						DUtilities.TEST_RUNNER_APP = afile.getAbsolutePath();
						DUtilities.installRunner = true;
					}else{
						String msg = "'TestRunnerAPK' configuration is invalid or missing: "+ DUtilities.TEST_RUNNER_APP;
						Log.debug(msg);
						System.out.println("SAFS/DROID Aborting: "+ msg);
						return false;
					}
				}			
				Log.info("Using derived 'TestRunnerAPK' setting: "+ DUtilities.TEST_RUNNER_APP);
			}else{
				Log.info("Assuming 'TestRunnerAPK' is already installed on the device or emulator...");
			}

			DUtilities.TEST_RUNNER_PACKAGE = DEFAULT_TEST_RUNNER_PACKAGE;
			value = config.getNamedValue(SAFS_DROID, "TestRunnerPackage");
			if(value != null) {
				DUtilities.TEST_RUNNER_PACKAGE = value;
			}			
			Log.info("Using 'TestRunnerPackage' setting: "+ DUtilities.TEST_RUNNER_PACKAGE);

			// this should be an optional setting.
			// if not present, then no attempt to rebuild the TestRunner should be made
			DUtilities.rebuildRunner = false;
			DUtilities.TEST_RUNNER_APP_SOURCE = DEFAULT_TEST_RUNNER_SOURCE;
			value = config.getNamedValue(SAFS_DROID, "TestRunnerSource");
			if(value != null) {
				Log.info("Trying config file 'TestRunnerSource' setting: "+ value);
				DUtilities.TEST_RUNNER_APP_SOURCE = value;
				afile = DJavaHook.getAbsoluteFile(DUtilities.ROOT_DROID_PROJECT_DIR, DUtilities.TEST_RUNNER_APP_SOURCE);
	
				if (afile!=null) {
					DUtilities.TEST_RUNNER_APP_SOURCE = afile.getAbsolutePath();
					DUtilities.rebuildRunner = true;
				} else {
					String msg = "'TestRunnerSource' configuration is invalid or missing: " + DUtilities.TEST_RUNNER_APP_SOURCE;
					Log.debug(msg);
					System.out.println("SAFS/DROID Aborting: " + msg);
					return false;
				}		
				Log.info("Using derived 'TestRunnerSource' setting: "+ DUtilities.TEST_RUNNER_APP_SOURCE);
			}else{
				Log.info("Assuming 'TestRunnerAPK' doesn't need to rebuild ...");
			}

			// this should be an optional setting.
			// if not present, then no attempt to install the APK should be made
			DUtilities.installAUT = false;
			value = config.getNamedValue(SAFS_DROID, "TargetAPK");
			if(value != null) {
				Log.info("Trying config file 'TargetAPK' setting: "+ value);
				DUtilities.TEST_TARGET_APP = value;
				afile = new CaseInsensitiveFile(DUtilities.ROOT_DROID_PROJECT_DIR, DUtilities.TEST_TARGET_APP);
				if((afile.isAbsolute())&&(afile.isFile())){
					DUtilities.TEST_TARGET_APP = afile.getAbsolutePath();
					DUtilities.installAUT = true;
				}else{
					afile = new CaseInsensitiveFile(DUtilities.TEST_TARGET_APP);
					if(afile.isAbsolute()&&afile.isFile()){
						DUtilities.TEST_TARGET_APP = afile.getAbsolutePath();
						DUtilities.installAUT = true;
					}else{
						String msg = "'TargetAPK' configuration is invalid or missing: "+ DUtilities.TEST_TARGET_APP;
						Log.debug(msg);
						System.out.println("SAFS/DROID Aborting: "+ msg);
						return false;
					}
				}
				_weSetTargetAPP = DUtilities.installAUT;
				Log.info("Using derived 'TargetAPK' setting: "+ DUtilities.TEST_TARGET_APP);
			}else{
				Log.info("Assuming 'TargetAPK' is already installed on the device or emulator...");
			}
			
			//If rebuild runner, get the targetPackage from AUT and assign it to DUtilities.TEST_TARGET_PACKAGE
			if(DUtilities.installAUT && DUtilities.rebuildRunner){
				DUtilities.TEST_TARGET_PACKAGE = DUtilities.getTargetPackageValue(DUtilities.TEST_TARGET_APP);
				if(DUtilities.TEST_TARGET_PACKAGE!=null) DUtilities.IS_TEST_TARGET_PACKAGE_SET = true;
			}
			
			value = config.getNamedValue(SAFS_DROID, "TargetPackage");
			if(value != null) {
				DUtilities.TEST_TARGET_PACKAGE = value;
				_weSetTargetPackage = true;
				DUtilities.IS_TEST_TARGET_PACKAGE_SET = true;
			}			
			Log.info("Tentative 'TargetPackage' setting: "+ DUtilities.TEST_TARGET_PACKAGE);
			//If the TEST_TARGET_PACKAGE is not set, we will not rebuild the TEST RUNNER
			DUtilities.rebuildRunner = DUtilities.rebuildRunner && DUtilities.IS_TEST_TARGET_PACKAGE_SET;
			

			DUtilities.TEST_RUNNER_INSTRUMENT = DEFAULT_TEST_RUNNER_INSTRUMENT;
			value = config.getNamedValue(SAFS_DROID, "TestInstrument");
			if(value != null) {
				DUtilities.TEST_RUNNER_INSTRUMENT = value;
			}			
			Log.info("Using 'TestInstrument' setting: "+ DUtilities.TEST_RUNNER_INSTRUMENT);
			
			value = config.getNamedValue(SAFS_DROID, "AntRebuildArgs");
			if(value != null) {
				DUtilities.rebuildRunnerAntArgs = value.split(" ");
			}			
			Log.info("Using 'AntRebuildArgs' setting: "+ value);
			
			value = config.getNamedValue(SAFS_DROID, "AntRebuildForce");
			if(value != null) {
				DUtilities.rebuildRunnerForce = StringUtilities.convertBool(value);
			}			
			Log.info("Using 'AntRebuildForce' setting: "+ DUtilities.rebuildRunnerForce);
			
			value = config.getNamedValue(SAFS_DROID, "PersistEmulators");
			if(value != null) {
				persistEmulators = StringUtilities.convertBool(value);
			}			
			Log.info("Using 'PersistEmulators' setting: "+ persistEmulators);
			
			value = config.getNamedValue(SAFS_DROID, "PersistJVM");
			if(value != null) {
				persistJVM = StringUtilities.convertBool(value);
			}			
			Log.info("Using 'PersistJVM' setting: "+ persistJVM);
			
			value = config.getNamedValue(SAFS_DROID, "ForceStop");
			if(value != null) {
				_forceStop = StringUtilities.convertBool(value);
			}			
			Log.info("Using 'ForceStop' setting: "+ _forceStop);
			
			value = config.getNamedValue(SAFS_DROID, "ForceClear");
			if(value != null) {
				_forceClear = StringUtilities.convertBool(value);
			}			
			Log.info("Using 'ForceClear' setting: "+ _forceClear);

			value = config.getNamedValue(SAFS_DROID, "PortForwarding");
			if(value != null) {
				portForwarding = StringUtilities.convertBool(value);
			}			
			Log.info("Using 'PortForwarding' setting: "+ portForwarding);
			
			value = config.getNamedValue(SAFS_DROID, "TargetResignJar");
			if(value != null) {
				DUtilities.RESIGN_JAR_FULL_NAME = value;
			}
			Log.info("Using 'TargetResignJar' setting: "+ DUtilities.RESIGN_JAR_FULL_NAME);
			
			return true;
		}

		/**
		 * Get an CaseInsensitiveFile
		 * @param dir	String, the directory where the file is stored
		 * @param file	String, the file relative/absolute name
		 * @return
		 */
		public static CaseInsensitiveFile getAbsoluteFile(String dir, String file) {
			CaseInsensitiveFile afile = null;
			
			if(dir==null){
				afile = new CaseInsensitiveFile(file);
				if ( !afile.isAbsolute() || !afile.isFile()){
					String msg = "Can't get file '"+file+"'";
					Log.debug(msg);
				}
			}else{
				if (file != null) {
					afile = new CaseInsensitiveFile(dir, file);
					if ( !afile.isAbsolute() || !afile.isFile()) {
						afile = new CaseInsensitiveFile(file);
						if ( !afile.isAbsolute() || !afile.isFile()){
							String msg = "Can't get file from dir='"+dir+"' file='"+file+"'";
							Log.debug(msg);
						}
					}
				} else {
					Log.info("The file is null, can't get a CaseInsensitiveFile.");
				}
			}
			
			return afile;
		}
		/**
		 * If no devices/emulators are running, then we will launch one based on DEFAULT or INI settings.
		 * <p>
		 * We include the loading of required Android packages as part of the launch.  
		 * We also start the instrumentation runner on the device which starts the TCP Messenger service 
		 * so that we can start 2-way communication.
		 * 
		 * Invoked during start loop initialization.
		 * <ol>
		 * <li>normal initialization for STAFHelper, GUIUtilities, TestRecordHelper, and RequestProcessor.
		 * <li>{@link #createProtocolRunner()}
		 * <li>{@link #beforeLaunchRemoteEngine()}
		 * <li>{@link #launchRemoteEngine()}
		 * <li>{@link #afterLaunchRemoteEngine()}
		 * <li>{@link #startProtocolRunner()}
		 * <li>{@link #waitForRemoteConnection(int)}
		 * </ol>
		 */		
		public boolean launchRemoteEngine(){
			// see if any one adb devices is attached
			boolean havedevice = false;
			
			List<String> devices = null;
			try{
				devices = DUtilities.getAttachedDevices();
				Log.info("Detected "+ devices.size() +" device/emulators attached.");
				if(devices.size() == 0){				
					if((DUtilities.DEFAULT_EMULATOR_AVD != null) && (DUtilities.DEFAULT_EMULATOR_AVD.length()> 0)){
						//DUtilities.killADBServer();
						//try{Thread.sleep(5000);}catch(Exception x){}
						Log.info("Attempting to launch EMULATOR_AVD: "+ DUtilities.DEFAULT_EMULATOR_AVD);
						if (! DUtilities.launchEmulatorAVD(DUtilities.DEFAULT_EMULATOR_AVD)){
							String msg = "Unsuccessful launching EMULATOR_AVD: "+DUtilities.DEFAULT_EMULATOR_AVD +", or TIMEOUT was reached.";
							Log.debug(msg);
							System.out.println("SAFS/DROID Aborting: "+ msg);
							return false;							
						}else{
							weLaunchedEmulator = true;
							Log.info("Emulator launch appears to be successful...");
							havedevice = true;
							
							if(_unlockEmulatorScreen) {
								String stat = DUtilities.unlockDeviceScreen()? " ":" NOT ";
								info("Emulator screen was"+ stat +"successfully unlocked!");
							}						
						}
					}else{
						String msg = "No Devices found and no EMULATOR_AVD specified in configuration file.";
						Log.debug(msg);
						System.out.println("SAFS/DROID Aborting: "+ msg);
						return false;							
					}				
				}else if(devices.size() > 1){
					// if multiple device attached then user DeviceSerial to target device
					if(DUtilities.DEFAULT_DEVICE_SERIAL.length() > 0){
						boolean matched = false;
						int d = 0;
						String lcserial = DUtilities.DEFAULT_DEVICE_SERIAL.toLowerCase();
						String lcdevice = null;
						for(;(d < devices.size())&&(!matched);d++){
							lcdevice = ((String)devices.get(d)).toLowerCase();
							Log.info("Attempting match device '"+ lcdevice +"' with default '"+ lcserial +"'");
							matched = lcdevice.startsWith(lcserial);
						}
						// if DeviceSerial does not match one of multiple then abort
						if(matched){
							havedevice = true;
							DUtilities.USE_DEVICE_SERIAL = " -s "+ DUtilities.DEFAULT_DEVICE_SERIAL +" ";
						}else{
							String msg = "Requested Device '"+ DUtilities.DEFAULT_DEVICE_SERIAL +"' was not found.";
							Log.debug(msg);
							System.out.println("SAFS/DROID Aborting: "+ msg);
							return false;							
						}
					}else{
						// if no DeviceSerial present then use first device
						String device = null;
						String tdev = (String)devices.get(0);
						if(tdev.endsWith("device")){
							device = tdev.substring(0, tdev.length() -6).trim();
						}else if(tdev.endsWith("emulator")){// not known to be used
							device = tdev.substring(0, tdev.length() -8).trim();
						}else{
							String msg = "Unknown Device Listing Format: "+ tdev;
							Log.debug(msg);
							System.out.println("SAFS/DROID Aborting: "+ msg);
							return false;							
						}
						havedevice = true;
						DUtilities.USE_DEVICE_SERIAL = " -s "+ device +" ";						
					}
				}else{
					// if one device, we don't need to specify -s DEVICE_SERIAL
					// DUtilities.USE_DEVICE_SERIAL should already be empty ("");
					havedevice = true;
				}
				
				tcpServer.setPortForwarding(portForwarding);
				
			}catch(SAFSRuntimeException x){
				Log.debug("Aborting due to "+x.getMessage());
				System.out.println("SAFS/DROID Aborting: "+ x.getMessage());
				return false;
			}

			if(havedevice) havedevice = DUtilities.rebuildRunner();	
			if(havedevice) havedevice = DUtilities.resignAUTApk();
			if(havedevice) havedevice = DUtilities.installEnabledAPKs();			
			if(havedevice) havedevice = DUtilities.launchTestInstrumentation();				
			
			return havedevice;
		}
		
		/**
		 * Minimally, forward TCP Port if we launched an Emulator.
		 * Needs further implementation.<br>
		 * Check/Install SAFSMessenger Service on device/emulator.<br>
		 * <p>
		 * Check/Install SAFSDroidAutomation Engine on device/emulator.<br>
		 * Note: The engine has to be repackaged with specific AdroidManifest entries 
		 * for the target AUT package.  We can make the users do this manually, or we 
		 * may be able to find a way to do this on-the-fly based on INI settings.
		 * <p>
		 * Check/Install target AUT package on device/emulator.<br>
		 * 
		 * Invoked during start loop initialization.
		 * <ol>
		 * <li>normal initialization for STAFHelper, GUIUtilities, TestRecordHelper, and RequestProcessor.
		 * <li>{@link #createProtocolRunner()}
		 * <li>{@link #beforeLaunchRemoteEngine()}
		 * <li>{@link #launchRemoteEngine()}
		 * <li>{@link #afterLaunchRemoteEngine()}
		 * <li>{@link #startProtocolRunner()}
		 * <li>{@link #waitForRemoteConnection(int)}
		 * </ol>
		 */
		public boolean afterLaunchRemoteEngine(){
			
			boolean result = true;
			// TODO: Check for installed SAFS packages (handled during launchRemoteEngine()? )
			return result;
		}

		/**
		 * Performs the normal startProtocolRunner of the superclass.
		 * If the superclass call was successful then we also instance the SAFSWorker that will 
		 * be used by this engine and provide it our existing tcpServer object (remote controller) for use.
		 * @return true if we have successfully started our tcpServer protocol runner.
		 */
		@Override
		public boolean startProtocolRunner(){
			boolean proceed = super.startProtocolRunner();
			if(proceed){
				SAFSWorker worker = new SAFSWorker();
				worker.setRemoteControl(tcpServer, true);
				worker.setLogsInterface(this);
				((DTestRecordHelper)socketdata).setSAFSWorker(worker);
			}
			return proceed;
		}
		
		@Override
		protected void interpretResultProperties(Properties remoteResultProperties){
			super.interpretResultProperties(remoteResultProperties);
			  String temp;
			  // check for logtype
			  temp = remoteResultProperties.getProperty(JavaSocketsUtils.KEY_LOGTYPE);
			  if(temp != null) remoteLogtype = Integer.parseInt(temp);
			  				            				  
			  // check for log_comment
			  temp = remoteResultProperties.getProperty(JavaSocketsUtils.KEY_LOGCOMMENT);
			  if(temp != null) remoteComment = temp;
			  				            				  
			  // check for log_detail
			  temp = remoteResultProperties.getProperty(JavaSocketsUtils.KEY_LOGDETAIL);
			  if(temp != null) remoteDetail = temp;
			  				            				  
			  String[] params = null;
			  Collection collect = null;
			  String key = null;
			  String PARAM_SEP = ",";
	
			  // check for nls comment conversion
			  temp = remoteResultProperties.getProperty(JavaSocketsUtils.KEY_LOGCOMMENT_PARAMS);
			  if(temp != null) {
				  params = temp.split(PARAM_SEP);
				  collect = new ArrayList();
				  for(int i=0;i<params.length;i++) collect.add(params[i]);				            					  
				  key = remoteResultProperties.getProperty(JavaSocketsUtils.KEY_LOGCOMMENT_GENERIC);
				  if(key != null){
					  remoteComment = GENStrings.convert(key, remoteComment, collect);
				  }else{
					  key = remoteResultProperties.getProperty(JavaSocketsUtils.KEY_LOGCOMMENT_FAILED);
					  if(key != null){
						  remoteComment = FAILStrings.convert(key, remoteComment, collect);					            						  
					  }else{
						  // use comment as-is
					  }
				  }
			  }
			  // check for nls detail conversion
			  temp = remoteResultProperties.getProperty(JavaSocketsUtils.KEY_LOGDETAIL_PARAMS);
			  if(temp != null) {
				  params = temp.split(PARAM_SEP);
				  collect = new ArrayList();
				  for(int i=0;i<params.length;i++) collect.add(params[i]);				            					  
				  key = remoteResultProperties.getProperty(JavaSocketsUtils.KEY_LOGDETAIL_GENERIC);
				  if(key != null){
					  remoteDetail = GENStrings.convert(key, remoteDetail, collect);
				  }else{
					  key = remoteResultProperties.getProperty(JavaSocketsUtils.KEY_LOGDETAIL_FAILED);
					  if(key != null){
						  remoteDetail = FAILStrings.convert(key, remoteDetail, collect);					            						  
					  }else{
						  // use detail as-is
					  }
				  }
			  }					
		}
		
		@Override
		protected void logResultsMessage(){
		  if(remoteDetail != null){
			  log.logMessage(data.getFac(), remoteComment, remoteLogtype, remoteDetail);
		  }else{
			  log.logMessage(data.getFac(), remoteComment, remoteLogtype);
		  }
		}
		
		/**
		 * Perform final hook shutdown activities--such as killing emulators, if needed.
		 * This is called before allowSystemExit.
		 * @return the value returned from super.hook_shutdown()
		 * @see JavaHook#hook_shutdown()
		 * @see #allowSystemExit()
		 * @see DUtilities#shutdownLaunchedEmulators(boolean)
		 */
		protected boolean hook_shutdown(){
		 	Log.info(DROID_COMMANDS +" is shutting down.");
		  	boolean rc = false;
		  	if(_forceStop){
		  		if(_weSetTargetPackage){
				 	Log.info(DROID_COMMANDS +" attempting shutdown of TARGET PACKAGE: "+ DUtilities.TEST_TARGET_PACKAGE);
			  		try{ DUtilities.uninstallAPKPackage(DUtilities.TEST_TARGET_PACKAGE, _forceClear);}
			  		catch(Exception x){ 
			  			Log.debug("hook_shutdown could not uninstall "+ DUtilities.TEST_TARGET_PACKAGE+": "+x.getClass().getSimpleName()+": "+x.getMessage());
			  		}
		  		}else{
				 	Log.info(DROID_COMMANDS +" bypassing shutdown of uncertain TARGET PACKAGE: "+ DUtilities.TEST_TARGET_PACKAGE);
		  		}
			 	Log.info(DROID_COMMANDS +" attempting shutdown of MESSENGER PACKAGE: "+ DUtilities.SAFS_SERVICE_PACKAGE);
		  		try{ DUtilities.uninstallAPKPackage(DUtilities.SAFS_SERVICE_PACKAGE, _forceClear);}
		  		catch(Exception x){ 
		  			Log.debug("hook_shutdown could not uninstall "+ DUtilities.SAFS_SERVICE_PACKAGE+": "+x.getClass().getSimpleName()+": "+x.getMessage());
		  		}
			 	Log.info(DROID_COMMANDS +" attempting shutdown of RUNNER PACKAGE: "+ DUtilities.TEST_RUNNER_PACKAGE);
		  		try{ DUtilities.uninstallAPKPackage(DUtilities.TEST_RUNNER_PACKAGE, _forceClear);}
		  		catch(Exception x){ 
		  			Log.debug("hook_shutdown could not uninstall "+ DUtilities.TEST_RUNNER_PACKAGE+": "+x.getClass().getSimpleName()+": "+x.getMessage());
		  		}
		  	}
		  	if(!persistEmulators) {
			 	Log.info(DROID_COMMANDS +" checking for launched emulators...");
		  		DUtilities.shutdownLaunchedEmulators(weLaunchedEmulator);		  	
		  	}else{
			 	Log.info(DROID_COMMANDS +" attempting to PERSIST any launched emulators...");
		  	}
	  		if(!persistJVM){
			 	Log.info(DROID_COMMANDS +" super.hookShutdown()...");
	  			rc = super.hook_shutdown();
	  		}else{
			 	Log.info(DROID_COMMANDS +" attempting to PERSIST the JVM...");
	  		}
		  	return rc;
		}

		/** Solo Remote control LogsInterface implementation. 
		 * Logs a PASSED_MESSAGE via our current LogUtilities.*/
		public void pass(String action, String message) {
			if(_soloLogsEnabled) log.logMessage(socketdata.getFac(), action +":"+message, AbstractLogFacility.PASSED_MESSAGE);
		}

		/** Solo Remote control LogsInterface implementation. 
		 * Logs a FAILED_MESSAGE via our current LogUtilities.*/
		public void fail(String action, String message) {
			if(_soloLogsEnabled) log.logMessage(socketdata.getFac(), action +":"+message, AbstractLogFacility.FAILED_MESSAGE);
		}

		/** Solo Remote control LogsInterface implementation.  
		 * Logs a WARNING_MESSAGE via our current LogUtilities.*/
		public void warn(String action, String message) {
			if(_soloLogsEnabled) log.logMessage(socketdata.getFac(), action +":"+message, AbstractLogFacility.WARNING_MESSAGE);
		}

		/** Solo Remote control LogsInterface implementation.  
		 * Logs a a SAFS Debug Log info message if debug logging is enabled. 
		 * @see #enableDebug(boolean)*/
		public void info(String message) {
			if(_debugEnabled) Log.info(message);
		}

		/** Solo Remote control LogsInterface implementation.  
		 * Logs a a SAFS Debug Log debug message if debug logging is enabled.
		 * @see #enableDebug(boolean)*/
		public void debug(String message) {
			if(_debugEnabled) Log.debug(message);
		}

		private boolean _debugEnabled = true;
		
		/** Solo Remote control LogsInterface implementation. */
		public void enableDebug(boolean enabled) {
			_debugEnabled = enabled;
		}

		/** Solo Remote control LogsInterface implementation. */
		public boolean isDebugEnabled() {
			return _debugEnabled;
		}
		
		private boolean _soloLogsEnabled = true;
		
		/** Enable/Disable log output from the Solo LogsInterface methods pass, fail, warn. */
		public void enableSoloLogs(boolean enabled){
			_soloLogsEnabled = enabled;
		}
		
		/** @return true if the pass, fail, and warn methods of the Solo LogsInterface are enabled. */
		public boolean isSoloLogsEnabled(){ 
			return _soloLogsEnabled;
		}
		
		/**
		 * Launches a default instance of the SAFS/DROID engine.
		 * The "hook" is instanced and registered with STAF--which must already be running.
		 * The hook is started and will show up as "Ready" to SAFS Drivers once initialization 
		 * is complete.
		 * <p>
		 * Assuming everything is properly CLASSPATHed, the default SAFS/Droid Engine can be 
		 * launched simply with:
		 * <ul><p>
		 * java -Dsafs.config.paths=&lt;paths> org.safs.android.DJavaHook
		 * </ul>
		 */
		public static void main (String[] args) {
			Log.ENABLED = true;
			Log.setLogLevel(Log.DEBUG);
			
			System.out.println(DROID_COMMANDS +" Hook starting...");
			LogUtilities logs=new LogUtilities();
			
			DJavaHook hook = new DJavaHook(DROID_COMMANDS, logs);
			hook.setHelper(DROID_COMMANDS);
			hook.getTRDData();
			
			//Log.setHelper(hook.getHelper());
			
			hook.getRequestProcessor();
			hook.initConfigPaths();

			// HOOK INITIALIZATION COMPLETE
			hook.start();
		}

	}

