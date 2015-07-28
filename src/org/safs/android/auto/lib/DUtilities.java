/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.android.auto.lib;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.safs.tools.GenericProcessMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;

/**
 * Utilities for the Droid testing environment and Android Developer SDK tools.
 * 
 * @author Carl Nagle
 * <br>(LeiWang)	AUG 16, 2013	Fix problem of un-installing APK from device/emulator .
 */
public class DUtilities {

	protected static DUtilities utils = new org.safs.android.auto.lib.DUtilities();
	
	protected void debugI(String message){
		System.out.println(message);		
	}
	
	/**
	 * Writes to System.out.  Subclasses should override to log to a different mechanism.
	 * @param message
	 */
	protected static void debug(String message){
		utils.debugI(message);
	}
	
	/************************************************************************** 
	 * Default: C:\Program Files\Android\android-sdk\<br>
	 * Set to the root directory where the Droid Development SDK is located. 	 
	 **/
	public static String ROOT_DROID_SDK_DIR = "C:\\Program Files\\Android\\android-sdk";
	
	/************************************************************************** 
	 * Default: C:\Program Files\Android\android-sdk\tools<br>
	 * Set to the directory where the Droid Development SDK Tools are located. 	 
	 **/
	public static String ROOT_DROID_SDK_TOOLS = ROOT_DROID_SDK_DIR +File.separator+"tools";
	
	/************************************************************************** 
	 * Default: D:\ant182<br>
	 * Set to the root directory where the ant Development SDK is located. 	 
	 **/
	public static String ROOT_ANT_SDK_DIR = "D:\\ant182";
	
	/************************************************************************** 
	 * Default: "C:\\SAFS\samples\Droid"
	 * Set to the root directory where the Droid project files are located. 	 
	 **/
	public static String ROOT_DROID_PROJECT_DIR = "C:\\SAFS\\samples\\Droid";
	
	/************************************************************************** 
	 * Default: SAFSMessenger\bin\SAFSTCPMessenger-debug.apk<br>
	 * Set to the path to the SAFS Communication Service app. 	 
	 **/
	public static String SAFS_SERVICE_APP = "SAFSTCPMessenger\\bin\\SAFSTCPMessenger-debug.apk";

	/************************************************************************** 
	 * Default: org.safs.android.messenger<br>
	 * Set to the expected package name of the SAFS Service. 	 
	 **/
	public static String SAFS_SERVICE_PACKAGE = "org.safs.android.messenger";

	/************************************************************************** 
	 * Default: ? RobotiumTestRunner\bin\RobotiumTestRunner-debug.apk ?<br>
	 * Set to the path to the Test Runner app. 	 
	 **/
	public static String TEST_RUNNER_APP = "RobotiumTestRunner\\bin\\RobotiumTestRunner-debug.apk";
	
	/************************************************************************** 
	 * Default: RobotiumTestRunner<br>
	 * Set to the path to the Test Runner app's source folder. 	 
	 **/
	public static String TEST_RUNNER_APP_SOURCE = "RobotiumTestRunner";
	
	/************************************************************************** 
	 * Default: com.jayway.android.robotium.remotecontrol.client<br>
	 * Set to the expected package name of the Test Runner. 	 
	 **/
	public static String TEST_RUNNER_PACKAGE = "com.jayway.android.robotium.remotecontrol.client";

	/************************************************************************** 
	 * Default: com.jayway.android.robotium.remotecontrol.client/com.jayway.android.robotium.remotecontrol.client.RobotiumTestRunner<br>
	 * Set to the expected package and class name of the Test Runner Instrument. 	 
	 **/
	public static String TEST_RUNNER_INSTRUMENT = "com.jayway.android.robotium.remotecontrol.client/com.jayway.android.robotium.remotecontrol.client.RobotiumTestRunner";
	
	/************************************************************************** 
	 * Default: pathTo\bin\ApiDemos-debug.apk<br>
	 * Set to the path to the target Application. 	 
	 **/
	public static String TEST_TARGET_APP = "SAFSAPIDemo\\bin\\ApiDemos-debug.apk";
	
	/************************************************************************** 
	 * Default: org.safs.android.engine<br>
	 * Set to the expected package name of the target Application. 	 
	 **/
	public static String TEST_TARGET_PACKAGE = "com.android.samples.apidemos";
	
	/************************************************************************** 
	 * If {@link #TEST_TARGET_PACKAGE} is set, this field will be true.	 
	 **/
	public static boolean IS_TEST_TARGET_PACKAGE_SET = false;
	
	/** 120 seconds to wait the installation process to finish. */
	protected final static int DEFAULT_INSTALL_APK_TIMEOUT = 120;
	/** The timeout in seconds to wait the installation of APK, it is set to default timeout 120 seconds. */
	public static int timeoutWaitInstallAPK = DEFAULT_INSTALL_APK_TIMEOUT;
	
	/**
	 * The parameters used by 'adb' to install an application on device/emulator<br>
	 * You should modify the third value in this array<br>
	 * Attention: use the clone of this field as it is static and shared by threads<br>
	 * <pre>
	 * Usage:
	 * String[] params = installParams.clone();
	 * params[2] = "yourApp.apk";
	 * </pre>
	 * 
	 * @see #SAFS_SERVICE_APP
	 * @see #SAFS_ENGINE_APP
	 * @see #TEST_TARGET_APP
	 * 
	 */
	static String[] installParams = {"install", "-r", "application.apk"};
	
	/**
	 * The parameters used by 'adb' to uninstall an application on device/emulator<br>
	 * This version also clears application data and cache.<br>
	 * You should modify the second value in this array<br>
	 * Attention: use the clone of this field as it is static and shared by threads<br>
	 * <pre>
	 * Usage:
	 * String[] params = uninstallParams.clone();
	 * params[1] = "your.app.package";
	 * </pre>
	 * 
	 * @see #SAFS_SERVICE_PACKAGE
	 * @see #TEST_RUNNER_PACKAGE
	 * @see #TEST_TARGET_PACKAGE
	 */
	static String[] uninstallParams = {"uninstall", "application.package"};
	
	/**
	 * The parameters used by 'adb' to uninstall an application on device/emulator<br>
	 * This version does NOT clear application data or cache.<br>
	 * You should modify the third value in this array<br>
	 * Attention: use the clone of this field as it is static and shared by threads<br>
	 * <pre>
	 * Usage:
	 * String[] params = uninstallParamsNoClear.clone();
	 * params[2] = "your.app.package";
	 * </pre>
	 * 
	 * @see #SAFS_SERVICE_PACKAGE
	 * @see #TEST_RUNNER_PACKAGE
	 * @see #TEST_TARGET_PACKAGE
	 */
	static String[] uninstallParamsNoClear = {"uninstall", "-k", "application.package"};
	/**
	 * The parameters used by 'adb shell command: pm - package manager' to uninstall an application on device/emulator<br>
	 * Sometimes 'adb uninstall -k application.package' will fail to un-install, so try<br>
	 * "adb shell pm uninstall -k application.package" to un-install<br>
	 * This version does NOT clear application data or cache.<br>
	 * You should modify the fifth value in this array<br>
	 * Attention: use the clone of this field as it is static and shared by threads<br>
	 * <pre>
	 * Usage:
	 * String[] params = uninstallParamsNoClear.clone();
	 * params[4] = "your.app.package";
	 * </pre>
	 * 
	 * @see #SAFS_SERVICE_PACKAGE
	 * @see #TEST_RUNNER_PACKAGE
	 * @see #TEST_TARGET_PACKAGE
	 */
	static String[] uninstallParamsNoClearByShell = {"shell", "pm", "uninstall", "-k", "application.package"};
	
	/**
	 * The parameters used by 'adb' to launch a test runner on device/emulator<br>
	 * You should modify the fourth value in this array<br>
	 * Attention: use the clone of this field as it is static and shared by threads<br>
	 * <pre>
	 * Usage:
	 * String[] params = launchTestCaseParams.clone();
	 * params[3] = "yourRunnerInstrument";
	 * </pre>
	 * 
	 * @see #SAFS_ENGINE_INSTRUMENT
	 * @see #ROBOTIUM_ENGINE_INSTRUMENT
	 */
	static String[] launchTestCaseParams = {"shell", "am", "instrument", "com.jayway.android.robotium.remotecontrol.client/com.jayway.android.robotium.remotecontrol.client.RobotiumTestRunner"};

	public static final String MANIFEST_XML_FILENAEM = "AndroidManifest.xml";
	
	/************************************************************************** 
	 * Empty until set.
	 * Set to the (optional) DeviceSerial stored in the INI config file. 	 
	 **/
	public static String DEFAULT_DEVICE_SERIAL = "";
	
	/************************************************************************** 
	 * Empty until set.
	 * Set to the value to be used by the Android Debug Bridge (adb).  
	 * This is normally empty, but may get set during device interrogation. 	
	 * When empty, it means use the one (and only) detected device/emulator. 
	 **/
	public static String USE_DEVICE_SERIAL = "";
			
	/************************************************************************** 
	 * null until set.
	 * Set to the name of the default EMULATOR AVD, if any. 	 
	 **/
	public static String DEFAULT_EMULATOR_AVD = null;
	
	public static final String DEVICE_STRING = "device";
	public static final String OFFLINE_STRING = "offline";

	/************************************************************************** 
	 * 180 (seconds) by default.
	 * Set to the default emulator/device launch timeout. 	 
	 **/
	public static int REMOTE_DROID_LAUNCH_TIMEOUT = 180;
	    
   /** 20. Default timeout in seconds to wait for remote log commencement. */
    public static int REMOTE_LAUNCH_TIMEOUT = 20;
    
    /** static android sdk tool to the one appropriate for the OS (Windows or Unix). */
	protected static AndroidTools androidsdk = null;
	
	 /** static ant sdk tool to the one appropriate for the OS (Windows or Unix). */
	protected static AntTool anttool = null;
	
	/**
	 * Default TRUE flag to (re)install the AUT Target Package APK upon launch.
	 */
	public static boolean installAUT = true;
	/**
	 * Default FALSE flag to (re)install the SAFS TCP Messenger Package APK upon launch.
	 * Normally you don't have to reinstall this since it is the same for all tested apps.
	 */
	public static boolean installMessenger = false;
	
	/**
	 * Default TRUE flag to (re)install the Test Runner APK upon launch.
	 */
	public static boolean installRunner = true;
	
	/** 
	 * Default FALSE flag to rebuild the Test Runner APK before installing it at launch time.
	 * Typically this is done if the Test Runner APK needs to be modified to accommodate a new 
	 * AUT Target Package.  Not too often. */
	public static boolean rebuildRunner = false;
	
	/** 
	 * Default NULL value of arguments to pass to the Ant launcher used to rebuild the  
	 * Test Runner APK before installing it at launch time.
	 * <p>
	 * On Windows, a very common Ant launcher arg to pass is "-noclasspath" to force the Ant build to 
	 * ignore the Windows CLASSPATH Environment variable. 
	 * <p>
	 * This value should be considered separate from the typical "debug" or "release" args 
	 * sent to Ant.  Instead, these are for the platform-specific Ant launchers: ant.bat, etc..
	 **/
	public static String[] rebuildRunnerAntArgs = null;

	/**
	 * Default FALSE flag.  Do not rebuild the runner if we think nothing has changed in the 
	 * AndroidManifest.XML.  Change this to True to force a rebuild that otherwise would not 
	 * proceed due to no detected changes in the automated processing of the AndroidManifest.
	 * <p>
	 * Note: rebuildRunner must also be true.  This force override only applies if a rebuild 
	 * has been requested, but the detection of unchanged AndroidManifest XML would otherwise 
	 * forego the actual rebuild.
	 */
	public static boolean rebuildRunnerForce = false;
	
	/** java.lang.SecurityException: Permission Denial */
	/** android.util.AndroidException: INSTRUMENTATION_FAILED:*/
	private final static Pattern ANDROID_EXCEPTION = Pattern.compile(".*Exception:.*");
	private final static Pattern INSTRUMENTATION_FAILED = Pattern.compile(".*INSTRUMENTATION_FAILED.*");	

	/**
	 * This field contains the resign jar file's absolute name, for example, "C:\safs\lib\re-sign.jar".
	 * This jar is used to resign the AUT {@link #TEST_TARGET_APP} automatically. If this field is 
	 * null, the AUT {@link #TEST_TARGET_APP} will not be resigned.
	 * <p>
	 * This field only take effect when field {@link #installAUT} is true.
	 * <p>
	 */
	public static String RESIGN_JAR_FULL_NAME = null;
	
	/**
	 * <em>Note:</em>	Risk!!! If some threads modify {@link #TEST_TARGET_APP}, {@link #TEST_RUNNER_APP}
	 *                  or {@link #SAFS_SERVICE_APP} during this method is called, wrong. use synchronized block?
	 * for each APK whose install boolean is still true, install the apk.<br>
	 * Will attempt to install the AUT APK, the TestRunner APK, and the TCP Messenger APK<br>
	 * 
	 * @return false if any installable APK did not successfully install.  
	 * Otherwise returns true.
	 */
	public static boolean installEnabledAPKs(){
		if(installAUT){
			debug("DUtilities flagged to install "+ TEST_TARGET_APP +"...");
			try{ installReplaceAPK(TEST_TARGET_APP);}
			catch(Exception x){
				debug("INSTALL AUT "+ x.getClass().getSimpleName()+" "+ x.getMessage());
				return false; }
		}else{
			debug("DUtilities is NOT flagged to install "+ TEST_TARGET_APP +"...");
		}
		if(installMessenger){
			debug("DUtilities flagged to install "+ SAFS_SERVICE_APP +"...");
			try{ installReplaceAPK(SAFS_SERVICE_APP);}
			catch(Exception x){
				debug("INSTALL MESSENGER "+ x.getClass().getSimpleName()+" "+ x.getMessage());
				return false; }
		}else{
			debug("DUtilities is NOT flagged to install "+ SAFS_SERVICE_APP +"...");
		}
		if(installRunner){
			debug("DUtilities flagged to install "+ TEST_RUNNER_APP +"...");
			try{ installReplaceAPK(TEST_RUNNER_APP);}
			catch(Exception x){
				debug("INSTALL RUNNER "+ x.getClass().getSimpleName()+" "+ x.getMessage());
				return false; }
		}else{
			debug("DUtilities is NOT flagged to install "+ TEST_RUNNER_APP +"...");
		}
		return true;
	}
	
	/**
	 * <em>Note:</em>	Risk!!! If some threads modify {@link #TEST_TARGET_APP}, {@link #TEST_RUNNER_APP}
	 *                  or {@link #TEST_RUNNER_APP_SOURCE} during this method is called, wrong. use synchronized block?
	 * Try to rebuild the test-runner apk.<br>
	 * This will modify the filed {@link #testRunnerApk}<br>
	 * You should call this method before calling {@link #installEnabledAPKs()}<br>
	 * 
	 * @return	boolean,	true if the rebuild succeed; false otherwise.
	 * @see #installEnabledAPKs()
	 */
	public static boolean rebuildRunner(){
		if(rebuildRunner){
			debug("DUtilities attempting to rebuild "+ TEST_RUNNER_APP +"...");
			try{
				TEST_RUNNER_APP = rebuildTestRunnerApk(TEST_RUNNER_APP_SOURCE, TEST_TARGET_APP, TEST_RUNNER_APP,TEST_RUNNER_INSTRUMENT, true);
				if(TEST_RUNNER_APP==null) {
					debug("DUtilities unexpected failure to rebuild "+ TEST_RUNNER_APP);
					return false;
				}
			}catch(Exception x){ 
				debug("DUtilities "+ x.getClass().getSimpleName()+" failure to rebuild "+ TEST_RUNNER_APP);
				return false; 
			}
			debug("DUtilities successful rebuilding "+ TEST_RUNNER_APP);
		}else {
			debug("DUtilities rebuild not indicated for "+ TEST_RUNNER_APP );
		}
		return true;
	}
	
	/**
	 * <em>Note:</em>	Risk!!! If some threads modify {@link #TEST_RUNNER_INSTRUMENT} 
	 *                  during this method is called, wrong. use synchronized block?
	 */
	public static boolean launchTestInstrumentation(){
		return launchTestInstrumentation(TEST_RUNNER_INSTRUMENT);
	}
	
	/**
	 * Set our static android sdk tool to the one appropriate for the OS (Windows or Unix).<br>
	 * The routine does nothing if the appropriate sdk instance is already set.<br>
	 * 
	 * For the sdk's tool home, it firstly try to set it to parameter 'androidToolHome'<br>
	 * If androidToolHome is not a valid home path:<br>
	 * it will try to get it from the 'VM properties' {@link AndroidTools#ANDROID_HOME_SYS_PROP}<br>
	 * or from 'systeme environment' {@link AndroidTools#ANDROID_HOME_ENV_VAR}<br>
	 * or from 'systeme environment' {@link AndroidTools#ANDROID_SDK_ENV_VAR}<br>
	 * 
	 * @param	androidToolHome		String, the android tool's sdk home path
	 * @see org.safs.android.auto.lib.AndroidTools
	 */
	public static AndroidTools getAndroidTools(String androidToolHome){
		if (androidsdk == null){
			debug("Attempting to initialize Android Tools...");
			androidsdk = AndroidTools.get();
			
			String toolHome = androidToolHome; 
			try{
				androidsdk.setToolHome(toolHome);
			}catch(IllegalStateException ise){
				debug(ise.getMessage());
				toolHome = androidsdk.getToolHome();
			}
			debug("Setting Android Tools SDK Dir to "+ toolHome);
		}
		
		return androidsdk;
	}
	
	/**
	 * Writes to System.err.  Subclasses should override to log to a different mechanism.
	 * @param message
	 */
	protected static void error(String message){
		System.err.println(message);
	}
	
	protected static GenericProcessMonitor mon = null;
	
	/**
	 * Returns a blank instance of GenericProcessMonitor on which to call the necessary 
	 * static methods.  Subclasses should override to get an appropriate subclass of 
	 * GenericProcessMonitor.
	 */
	protected static  GenericProcessMonitor getProcessMonitor(){
		return mon == null ? new GenericProcessMonitor(): mon;
	}
	
	/**
	 * You can always change the {@link #androidsdk}'s home path by calling this method.<br>
	 * But be careful, if you call this method, you will modify the tool-home of Singleton {@link AndroidTools},<br>
	 * and the other thread may be affected when the use AndroidTools.<br>
	 * 
	 * @param androidToolHome	String, the android tool's sdk home path
	 */
	public static void setAndroidToolsHome(String androidToolHome){
		if(androidsdk==null){
			getAndroidTools(androidToolHome);
		}else{
			try{
				androidsdk.setToolHome(androidToolHome);
			}catch(IllegalStateException ise){
				debug(ise.getMessage());
			}
		}
	}
	
	/**
	 * Before calling this method:<br>
	 * Remember to set {@link #ROOT_DROID_SDK_DIR}<br>
	 * or set 'vm properties' {@link AndroidTools#ANDROID_HOME_SYS_PROP}<br>
	 * or set 'systeme environment' {@link AndroidTools#ANDROID_HOME_ENV_VAR}<br>
	 * or set 'systeme environment' {@link AndroidTools#ANDROID_SDK_ENV_VAR}<br>
	 * 
	 */
	protected static void initAndroidTools(){
		getAndroidTools(ROOT_DROID_SDK_DIR);
	}
	
	/**
	 * Set our static ant sdk tool to the one appropriate for the OS (Windows or Unix).<br>
	 * The routine does nothing if the appropriate sdk instance is already set.<br>
	 * 
	 * For the sdk's tool home, it firstly try to set it to parameter 'antToolHome'<br>
	 * If antToolHome is not a valid home path:<br>
	 * it will try to get it from the 'VM properties' {@link AntTool#ANT_HOME_PROP}<br>
	 * or from 'systeme environment' {@link AntTool#ANT_HOME_ENV}<br>
	 * 
	 * @param	antToolHome		String, the ant tool's sdk home path
	 * @see org.safs.android.auto.lib.AntTool
	 */
	public static AntTool getAntTool(String antToolHome){
		if (anttool == null){
			debug("Attempting to initialize Ant Tool ...");
			anttool = AntTool.instance();
			
			String toolHome = antToolHome; 
			try{
				anttool.setToolHome(toolHome);
			}catch(IllegalStateException ise){
				debug(ise.getMessage());
				toolHome = anttool.getToolHome();
			}
			debug("Setting Ant Tool SDK Dir to "+ toolHome);
		}
		
		return anttool;
	}
	
	/**
	 * You can always change the {@link #anttool}'s home path by calling this method.<br>
	 * But be careful, if you call this method, you will modify the tool-home of Singleton {@link AntTool},<br>
	 * and the other thread may be affected when the use AntTool.<br>
	 * 
	 * @param antToolHome	String, the ant tool's sdk home path
	 */
	public static void setAntToolsHome(String antToolHome){
		if(anttool==null){
			getAntTool(antToolHome);
		}else{
			try{
				anttool.setToolHome(antToolHome);
			}catch(IllegalStateException ise){
				debug(ise.getMessage());
			}
		}
	}
	
	/**
	 * Before calling this method:<br>
	 * Remember to set {@link #ROOT_ANT_SDK_DIR}<br>
	 * or set 'vm properties' {@link AntTool#ANT_HOME_PROP}<br>
	 * or set 'systeme environment' {@link AntTool#ANT_HOME_ENV}<br>
	 * 
	 */
	protected static void initAntTool(){
		getAntTool(ROOT_ANT_SDK_DIR);
	}
	
	/**
	 * Extract the list of Android Debug Bridge attached devices from Android Tools
	 * <p>
	 * adb devices
	 * 
	 * @return String[] of 0 or more devices--which may be "device" or "offline".
	 * @throws RuntimeException if there is a problem executing the Android Debug Bridge (adb)
	 */
	public static List<String> getAttachedDevices() throws RuntimeException{
		Process2 process = null;
		ArrayList<String> rs = new ArrayList<String>();
		BufferedReader reader = null;
		try{
			if(androidsdk==null) initAndroidTools();
			process = androidsdk.adb("devices");
			boolean finished = false;
			reader = process.getStdoutReader();
			String line = null;
			while(!finished){
				try{
					int exitValue = 0;
					if((exitValue=process.exitValue())!=0){
						debug("Warning: The process exit value '"+exitValue+"' is not 0.");
					}
					finished = true;
				}
				catch(IllegalThreadStateException x){
					while((line=reader.readLine())!=null){
						if(line.trim().endsWith(DEVICE_STRING)) rs.add(line);
						if(line.trim().endsWith(OFFLINE_STRING)) rs.add(line);
					}
				}
			}
			
			while((line=reader.readLine())!=null){
				if(line.trim().endsWith(DEVICE_STRING)) rs.add(line);
				if(line.trim().endsWith(OFFLINE_STRING)) rs.add(line);
			}

			try{ reader.close();}catch(Exception x){}
		}catch(IOException x){
			debug("Error finding/running adb command: "+ x.getClass().getSimpleName()+", "+x.getMessage());
			if(reader!=null) 
				try{ reader.close(); reader = null;}catch(Exception x2){}
			if(process!=null)
				try{process.destroy();process = null;}catch(Exception x2){}			
			throw new RuntimeException("adb runtime error: "+ x.getClass().getSimpleName()+", "+x.getMessage());
		}		
		if(reader!=null) 
			try{ reader.close();reader = null;}catch(Exception x2){}
		if(process!=null)
			try{process.destroy();process = null;}catch(Exception x2){}			
		return rs;
	}

	/**
	 * ADB: The central point to communicate with any devices, emulators, or the applications running on them.
	 */
	public static AndroidDebugBridge bridge = null;
	
	/**
	 * Get the AndroidDebugBridge instance.
	 * @return AndroidDebugBridge
	 */
	public static AndroidDebugBridge getAndroidDebugBridge(){
		String debugmsg = "DU.getAndroidDebugBridge(): ";
        try {
        	if(bridge==null){
        		bridge = AndroidDebugBridge.getBridge();
        		if(bridge==null){
        			// init the lib
        			String adbLocation = System.getProperty("com.android.screenshot.bindir");
        			if (adbLocation != null && adbLocation.length() != 0) {
        				adbLocation += File.separator + "adb";
        			} else {
        				adbLocation = "adb";
        			}
        			AndroidDebugBridge.init(false /* debugger support */);
        			bridge = AndroidDebugBridge.createBridge(adbLocation, true /* forceNewBridge */);
        		}
        	}
        }catch(Exception e){
        	debug(debugmsg+"Met "+e.getClass().getSimpleName()+":"+e.getMessage());
        }
        
        return bridge;
	}
	
	/**
	 * Return the device that we want to test with.
	 * If there is only one device, we will return it.<br>
	 * If there are multiple devices, we will try to return the device defined by DUtilities.DEFAULT_DEVICE_SERIAL<br>
	 * If DUtilities.DEFAULT_DEVICE_SERIAL is not avaiable or we can't find a matched device, then return the first oen.<br>
	 * 
	 * @return IDevice
	 */
    public static IDevice getIDevice() {
        IDevice target = null;
        String debugmsg = "DU.getIDevice(): ";
        
        try {
        	bridge = getAndroidDebugBridge();
        	if(bridge==null){
        		debug(debugmsg+"Error: Can't get the Android Debug Bridge!!!");
        	}else{
        		// we can't just ask for the device list right away, as the internal thread getting
        		// them from ADB may not be done getting the first list.
        		// Since we don't really want getDevices() to be blocking, we wait here with a timeout 10 seconds.
        		int count = 0;
        		while (bridge.hasInitialDeviceList() == false) {
        			try {
        				Thread.sleep(100);
        				count++;
        			} catch (InterruptedException e) {
        				debug(debugmsg+"Ignore Exception "+e.getClass().getSimpleName()+":"+e.getMessage());
        			}
        			
        			if (count > 100) {
        				debug(debugmsg+"Timeout reached, can't get device list!");
        				return null;
        			}
        		}
        		
        		IDevice[] devices = bridge.getDevices();
        		
        		if (devices.length == 0) {
        			debug(debugmsg+"No devices found!");
        		}else if(devices.length == 1){
        			target = devices[0];
        		}else{
        			//If there are more than one device attached
        			String serialNumber = null;
        			if(DUtilities.DEFAULT_DEVICE_SERIAL.length()>0){
        				//If we have a default one defined by DUtilities.DEFAULT_DEVICE_SERIAL, we try to get it.
        				for(IDevice d: devices){
        					serialNumber = d.getSerialNumber();
        					debug(debugmsg+"Attempting match device '"+ serialNumber +"' with default '"+ DUtilities.DEFAULT_DEVICE_SERIAL +"'");
        					if(serialNumber!=null &&
        					   serialNumber.toLowerCase().startsWith(DUtilities.DEFAULT_DEVICE_SERIAL.toLowerCase())){
        						target = d;
        						break;
        					}
        				}
        				if(target==null){
        					debug(debugmsg+"We didn't find a device matching with the default one. Return the first one.");
        					target = devices[0];
        				}
        			}else{
        				//If we don't have a default one (DUtilities.DEFAULT_DEVICE_SERIAL is empty), get the first device
        				target = devices[0];
        			}
        		}
        		
        		if (target != null) {
        			debug(debugmsg+"Got device: " + target.getSerialNumber());
        		} else {
        			debug(debugmsg+"Could not find matching device/emulator.");
        		}
        	}
        }catch(Exception e){
        	debug(debugmsg+"Met Exception "+e.getClass().getSimpleName()+":"+e.getMessage());
        }
        
        return target;
    }
    
    /**
     * Grab an image from an ADB-connected device.
     * @param device, IDevice: the android device or emulator, it can be got by {@link #getIDevice()}
     * @param rotation, int: the device rotation in degree <br>
     *                       we should rotate the image with inverse degree (360-rotation)<br>
     *                       Only rotatable is true, rotation will take effects.
     * @param rotatable, boolean: if the application is rotatable
     * 
     * @see #getIDevice()
     */
    public static BufferedImage getDeviceScreenImage(IDevice device, int rotation, boolean rotatable){
        RawImage rawImage= null;
        BufferedImage image = null;
        String debugmsg = "DUtilities.getDeviceScreenImage(): ";

        try {
        	if(device.isEmulator()){
        		debug(debugmsg+"try to get screen image for emulator "+device.getAvdName());
        	}else{
        		debug(debugmsg+"try to get screen image for device "+device.getSerialNumber());
        	}
            rawImage = device.getScreenshot();
            // device/adb not available?
            if (rawImage != null){
            	image = ImageUtils.convertImage(rawImage);
            	if(rotatable){
            		//When rotate the image got from ImageUtils.convertImage(), sometimes an ImagingOpException
            		//will be thrown out. ImagingOpException: Unable to transform src image
            		//So get a copy of the original image and rotate it.
            		image = ImageUtils.getCopiedImage(image, image.getWidth(), image.getHeight(), null);
            		//According to the rotation, rotate the image back.
            		image = ImageUtils.rotateImage(image, (360-rotation)%360);
            	}
            }else{
            	debug(debugmsg+"Can't get raw image from the device.");
            }
            
        }catch (Exception e) {
            debug(debugmsg+"Met " + e.getClass().getSimpleName()+":"+e.getMessage());
        }
        
        return image;
    } 
	
	/**
	 * Attempt to launch a prestored Emulator -avd and a -no-snapstorage argument.
	 * <p>
	 * emulator -avd (avd)
	 * 
	 * @return boolean true if emulator launch detects a new adb "device" within REMOTE_DROID_LAUNCH_TIMEOUT.  
	 * false if a new device is not detected in that period.
	 * @throws AndroidRuntimeException if an error occurs while trying to launch the emulator or 
	 * trying to extract the number of adb devices.
	 */
	public static boolean launchEmulatorAVD(String avd)throws AndroidRuntimeException{
		try{
			if(androidsdk==null)initAndroidTools();
			
			StartEmulator em = new StartEmulator();
			em.setDoReaperThread(false);
			em.setDoSocketThread(false);
			em.setDoOpenSocket(false);
		    em.setDoCloseSocket(false);
			em.setChainedStdOut(System.out);
			
			// this call will block until the emulator boot process is complete
			em.run(new String[]{"-no-snapstorage", "-avd", avd});
			
			debug("Emulator launch appears successful.");
			if(androidsdk.isWindowsOS()) em.getEmulatorProcess().destroy();
			return true;
		}catch(AndroidRuntimeException x){
			debug("Error launching emulator : "+ x.getClass().getSimpleName()+", "+ x.getMessage());
			return false;
		}catch(Exception x){
			debug("Error launching/running emulator : "+ x.getClass().getSimpleName()+", "+ x.getMessage());
			throw new AndroidRuntimeException("emulator runtime error: "+ x.getMessage());
		}		
	}
	
	/**
	 * Attempt to shutdown a specific emulator Process.
	 * @param emulator -- known valid values: "emulator", "emulator-arm", "emulator-mips", "emulator-x86"
	 * @return true if we saw the Process and (hopefully) shut down the Process.
	 */
	public static boolean shutdownEmulatorProcess(String emulator) throws IOException{
		boolean shutdown = false;
		GenericProcessMonitor monitor = getProcessMonitor();
		shutdown = monitor.shutdownProcess(emulator);
		if(!shutdown && Console.isWindowsOS()) { 
			emulator += StartEmulator.EMULATOR_WIN_EXT;
			shutdown = monitor.shutdownProcess(emulator);
		}
		return shutdown;
	}
	
	/**
	 * Attempts to shutdown (process destroy!) any emulators we have launched.
	 * This is NOT a graceful shutdown, but an attempt to destroy any emulator processes 
	 * we have launched with the StartEmulator tool.  This should be OK for emulators 
	 * we normally launch with the -no-snapstorage option.
	 * <p>
	 * Note the built-in process to signal an emulator shutdown does not seem to work on 
	 * Windows.  Windows runs emulator.exe which launches emulator-arm.exe.  The shutdown 
	 * request does not seem to impact emulator-arm.exe.
	 * <p>
	 * @param shutdownAnyEmulator to attempt to kill any emulator, whether we started it or not.
	 * @return true if we detect at least one running emulator we have launched has responded 
	 * with a positive shutdown attempt.  We will look for this signal for no more than 5 seconds.
	 */
	public static boolean shutdownLaunchedEmulators(boolean shutdownAnyEmulator){
		String match = "true";
		System.setProperty(StartEmulator.EMULATOR_DESTROY_PROPERTY, match);
		int timeout = 0;
		boolean destroyed = false;		
		while (timeout++ < 11 && !destroyed){
			try{destroyed = match.equals(System.getProperty(StartEmulator.EMULATOR_DESTROYED_PROPERTY));}
			catch(Exception x){}
			if(!destroyed) try{Thread.sleep(500);}catch(Exception x){}
		}		
		if(destroyed){
			debug("StartEmulator reports success on receipt of shutdown request.");
		}
		if(shutdownAnyEmulator){		
			debug("DUtilities attempting ProcessMonitor shutdownProcess...");
			boolean success = false;
			int index = 0;
			String emu = null;
			while (!success && (index < StartEmulator.EMULATORS.length)){
				emu = StartEmulator.EMULATORS[index++];
				try{ success = shutdownEmulatorProcess(emu); }
				catch(Exception x){
					debug("Emulator shutdown "+ x.getClass().getSimpleName() +": "+ x.getMessage());
				}
			}
			destroyed = success;
		}
		debug("Emulator(s) shutdown? "+ destroyed);
		return destroyed;
	}
	
	/**
	 * Calls getAttachedDevices and returns true if any are offline.
	 * @return true if getAttchedDevices returns any device as "offline"
	 * @see #getAttachedDevices()
	 */
	public static boolean isDeviceOffline(){
		boolean result = false;
		List<String> devices = null;
		debug("Checking for devices going offline...");
		try{ devices = getAttachedDevices();}
		catch(Exception x){}
		String device = null;
		if(devices.size()> 0){
			debug("Checking "+ devices.size() + " for 'offline' status....");
			for(int i=0;i<devices.size();i++){
				device = (String)devices.get(i);
				if(device.trim().endsWith(OFFLINE_STRING)){
					debug("detected OFFLINE device: "+ device);
					result = true;
				}
			}
		}
		if(!result) debug("No 'offline' devices detected.");		
		return result;
	}
	
	/**
	 * Execute adb kill-server then adb start-server back-to-back with a 4 second delay between.
	 */
	public static void resetADBServer(){
		if(androidsdk==null) initAndroidTools();
		Process2 process = null;
		debug("Resetting ADB Server...");
		try{
			process = androidsdk.adb("kill-server");
			process.waitFor().destroy();
		}catch(Exception x){}
		try{Thread.sleep(4000);}catch(Exception x){}
		debug("Starting ADB Server...");
		try{
			process = androidsdk.adb("start-server");
			process.waitFor().destroy();
		}catch(Exception x){}
	}
	
	/**
	 * Attempt an adb "start-server" command.
	 */
	public static void startADBServer(){
		if(androidsdk==null) initAndroidTools();
		Process2 process = null;
		debug("adb start-server commencing...");
		try{ 
			process = androidsdk.adb("start-server");
			process.forwardOutput().waitFor();
		}catch(Exception x){
			debug("adb start-server "+x.getClass().getSimpleName()+", "+x.getMessage());
		}
		if(process != null) try{ process.destroy();}catch(Exception x){}
		process = null;
	}
	
	/**
	 * Attempt an adb "kill-server" command.
	 */
	public static void killADBServer(){
		if(androidsdk==null) initAndroidTools();
		Process2 process = null;
		debug("adb kill-server commencing...");
		try{ 
			process = androidsdk.adb("kill-server");
			process.forwardOutput().waitFor();
		}catch(Exception x){
			debug("adb kill-server "+x.getClass().getSimpleName()+", "+x.getMessage());
		}
		if(process != null) try{ process.destroy();}catch(Exception x){}
		process = null;
	}
	
	/**
	 * Install the single APK provided in apkPath with the -r (replace) option. 
	 * Currently the routine provides for a 20 second timeout for the install.
	 * <p>
	 * 
	 * @param apkPath to APK ready for installation.
	 * @throws RuntimeException if the process was interrupted, had an IOException, or 
	 * did not exit in the timeout period or did not exit with success.
	 */
	public static void installReplaceAPK(String apkPath)throws RuntimeException{
		debug("INSTALLING "+apkPath);

		if(androidsdk==null) initAndroidTools();
		if(!waitDevice()){
			throw new RuntimeException(("OFFLINE device failure... during installing '"+apkPath+"'"));
		}
		
		String[] params = installParams.clone();
		params[2] = apkPath;
		Process2 proc = null;
		try{
			params = addDeviceSerialParam(params);
			debug("ATTEMPTING ADB Install command: adb "+ params);
			proc = androidsdk.adb(params);
			proc.forwardOutput().waitForSuccess(timeoutWaitInstallAPK).destroy();
			debug("ADB Install command successful.");
			proc = null;
		}
		catch(Throwable x){
			String msg = "Failed to install '"+ apkPath+ "' due to "+ x.getClass().getSimpleName()+", "+ x.getMessage();
			debug(msg);
			if(proc != null) try{ proc.destroy(); }catch(Exception x2){}finally{proc = null;}
			throw new RuntimeException(msg);
		}
	}
	
	/**
	 * Uninstall (remove) the single APK provided in apkFullPath. 
	 * <p>
	 * 
	 * @param apkFullPath the full path name of the APK to uninstall
	 * @param clearData set true to force a clear of the app data and cache along with the uninstall.
	 * @throws RuntimeException if the process was interrupted, had an IOException, or 
	 * did not exit with success.
	 * 
	 * @see #uninstallAPKPackage(String, boolean)
	 */
	public static void uninstallAPK(String apkFullPath, boolean clearData)throws RuntimeException{
		try{
			String packageName = DUtilities.getTargetPackageValue(apkFullPath);
			DUtilities.uninstallAPKPackage(packageName, true);
		}catch(Exception e){
			String msg = "Fail to un-install APK '"+apkFullPath+"'! Met Exception "+e.getClass()+":"+e.getMessage();
			debug(msg);
			throw new RuntimeException(msg);
		}
	}
	
	/**
	 * Uninstall (remove) the single APK (package) provided in apkPackage. 
	 * <p>
	 * 
	 * @param apkPackage the name of the APK package to uninstall
	 * @param clearData set true to force a clear of the app data and cache along with the uninstall.
	 * @throws RuntimeException if the process was interrupted, had an IOException, or 
	 * did not exit with success.
	 * 
	 * @see #uninstallAPK(String, boolean)
	 */
	public static void uninstallAPKPackage(String apkPackage, boolean clearData)throws RuntimeException{
		debug("UNINSTALLING "+apkPackage +", CLEARDATA="+String.valueOf(clearData));

		if(androidsdk==null) initAndroidTools();
		if(!waitDevice()){
			throw new RuntimeException(("OFFLINE device failure... during installing '"+apkPackage+"'"));
		}
		
		int field = clearData ? 1:2;
		String[] params = clearData ? uninstallParams.clone():uninstallParamsNoClear.clone();
		params[field] = apkPackage;
		Process2 proc = null;
		try{
			proc = androidsdk.adb(addDeviceSerialParam(params));
			proc.forwardOutput().waitForSuccess().destroy();
			proc = null;
		}
		catch(Exception x){
			String msg = "Failed to uninstall '"+ apkPackage+ "' due to "+ x.getClass().getSimpleName()+", "+ x.getMessage();
			debug(msg);
			if(proc != null) try{ proc.destroy(); }catch(Exception x2){}finally{proc = null;}
			if(!clearData){
//				If -k option is used (clearData is false), uninstall may fail, error is as below
//				The -k option uninstalls the application while retaining the data/cache.
//				At the moment, there is no way to remove the remaining data.
//				You will have to reinstall the application with the same signature, and fully uninstall it.
//				If you truly wish to continue, execute 'adb shell pm uninstall -k com.sas.android.bimobile'
				debug("Try to uninstall by 'adb shell pm uninstall -k packageName'.");
				params = uninstallParamsNoClearByShell.clone();
				params[4] = apkPackage;
				try{
					proc = androidsdk.adb(addDeviceSerialParam(params));
					proc.forwardOutput().waitForSuccess().destroy();
					proc = null;
				}catch(Exception x3){
					msg = "Failed to uninstall '"+ apkPackage+ "' due to "+ x3.getClass().getSimpleName()+", "+ x.getMessage();
					debug(msg);
					if(proc != null) try{ proc.destroy(); }catch(Exception x2){}finally{proc = null;}
					throw new RuntimeException(msg);
				}
			}else{
				throw new RuntimeException(msg);
			}
		}
	}
	
	/**
	 * Launches the test instrumentation which should also automatically launch our remote TCP Messenger.
	 * There is normally a 5 second Thread sleep after a successful launch to let the channels get 
	 * synchronized.
	 * @return true if we successfully launched the test instrumentation.
	 */
	public static boolean launchTestInstrumentation(String instrumentArg) throws RuntimeException{
		debug("LAUNCHING "+instrumentArg);
		boolean instrumentLaunched = true;
		
		if(androidsdk==null) initAndroidTools();
		if(!waitDevice()){
			debug(("OFFLINE device failure... during launch '"+instrumentArg+"'"));
			instrumentLaunched = false;
		}else{
			String[] params = launchTestCaseParams.clone();
			params[3] = instrumentArg;
			Process2 proc = null;
			BufferedReader stdout = null;
			BufferedReader stderr = null;
			try{ 
				proc = androidsdk.adb(addDeviceSerialParam(params));
				//the process to execute "adb shell am instrument xxx" will not fail, but just print message to stdout
				//so we have to analyse the output to see if there are some problem
				//android.util.AndroidException: INSTRUMENTATION_FAILED: org.safs.android.engine/org.safs.android.engine.DSAFSTestRunner
				//java.lang.SecurityException: Permission Denial
				
				String tempstr = null;
				//stdout
				stdout = proc.getStdoutReader();
				while((tempstr=stdout.readLine())!=null){
					debug(tempstr);
					if(!instrumentLaunched) continue;
					if(ANDROID_EXCEPTION.matcher(tempstr).matches() || 
					   INSTRUMENTATION_FAILED.matcher(tempstr).matches()){
						instrumentLaunched = false;
					}
				}
				//stderr
				stderr = proc.getStderrReader();
				while((tempstr=stderr.readLine())!=null){
					error(tempstr);
					if(instrumentLaunched) instrumentLaunched = false;
				}
				
				proc.waitForSuccess();
				proc = null;
			}
			catch(Exception x){
				debug("May have failed to launch test instrument '"+ instrumentArg+ "' due to "+ x.getClass().getSimpleName()+", "+ x.getMessage());
				instrumentLaunched = false;
			}finally{
				if(proc != null) try{proc.destroy();}catch(Exception x2){}
				if(stdout != null) try{stdout.close();}catch(Exception x2){}
				if(stderr != null) try{stderr.close();}catch(Exception x2){}
			}
			try{Thread.sleep(5000);}catch(Exception x){}
			
		}
		
		return instrumentLaunched;
	}
	
	/**
	 * Send an appropriate adb command to unlock the screen.
	 * The method will use {@link #addDeviceSerialParam(String[])} to the default command.
	 * @return true upon success, false otherwise.
	 */
	public static boolean unlockDeviceScreen(){
		String[] params = new String[]{"shell", "input", "keyevent", "82"};
		if(androidsdk==null) initAndroidTools();
		Process2 proc = null;
		try{ 
			proc = androidsdk.adb(addDeviceSerialParam(params));
			proc.forwardOutput().waitForSuccess().destroy();
			proc = null;
		}
		catch(Exception x){
			debug("May have failed to unlock device screen due to "+ x.getClass().getSimpleName()+", "+ x.getMessage());
			if(proc != null) try{proc.destroy();}catch(Exception x2){}
			return false;
		}
		return true;
	}
	
	/**
	 * Add the petential parameter "-s serivalNumber" to the parameters passed to command 'adb'.<br>
	 * This petential parameter is stored in {@link #USE_DEVICE_SERIAL}<br>
	 * 
	 * @param params	String[], string array used by command 'adb'
	 * @return			String[], string array used by command 'adb'
	 * @see #USE_DEVICE_SERIAL
	 */
	public static String[] addDeviceSerialParam(String[] params){
		if(params==null){
			debug("Array params is null.");
			return params;
		}
		if("".equals(USE_DEVICE_SERIAL)) return params;
		else{
			String[] serialParams = USE_DEVICE_SERIAL.trim().split(" ");
			String[] newParams = new String[params.length+serialParams.length];

			for(int i=0;i<serialParams.length;i++){
				newParams[i] = serialParams[i];
			}
			
			for(int i=0;i<params.length;i++){
				newParams[i+serialParams.length] = params[i];
			}
			return newParams;
		}
	}
	/**
	 * The routine will attempt to make sure adb devices are valid and not offline 
	 * @return
	 */
	public static boolean waitDevice(){
		int count = 0;
		boolean deviceIsOffline = isDeviceOffline();
		while(deviceIsOffline && count++ < 2){
			try{Thread.sleep(4000);}catch(Exception x){}
			deviceIsOffline = isDeviceOffline();
		}
		if(deviceIsOffline) {
			debug("OFFLINE device failure..");
		}
		return !deviceIsOffline;
	}
	
	/**
	 * Get the value of attribute 'package' from the tag <manifest> of AndroidManifest.xml of apk.<br>
	 * 
	 * @param apkFile	String, the apk file from which we want to get the "package's value"
	 * @return	the "package's value", or null if not found
	 */
	public static String getTargetPackageValue(String apkFile){
		String targetPackage = null;
		String errmsg = "";
		
		//aapt d xmltree RobotiumTestRunner-debug.apk AndroidManifest.xml | findstr package=
//		String[] findAutPackageName = {"d", "xmltree", "", "AndroidManifest.xml", "|", "findstr", "package=" };
		String[] findAutPackageName = {"d", "xmltree", "", MANIFEST_XML_FILENAEM };
		findAutPackageName[2] = apkFile;
		String packagePrefix = "A: package=\"";
		String packageSuffix = "\"";
		int index = -1;
		BufferedReader in = null;
		BufferedReader err = null;
		boolean packageAttributFound = false;
		Process2 process = null;
		try {
			if(androidsdk==null) initAndroidTools();
			debug("aapt "+ argsToString(findAutPackageName));
			process = androidsdk.aapt(addDeviceSerialParam(findAutPackageName));
			String tmpmsg = null;

			//Try to get the aut's package value from process's stdout
			in = process.getStdoutReader();
			while ((tmpmsg = in.readLine()) != null) {
				if(packageAttributFound) continue;
				
				index = tmpmsg.indexOf(packagePrefix);
				if(index>-1){
					targetPackage = tmpmsg;
					packageAttributFound = true;
				}
			}

			if(packageAttributFound){
				debug("raw aut's package is '" + targetPackage + "'");
				index = targetPackage.indexOf(packagePrefix);
				if (index>-1){
					targetPackage = targetPackage.substring(index + packagePrefix.length());
					index = targetPackage.indexOf(packageSuffix);
					if (index >-1){
						targetPackage = targetPackage.substring(0, index);
					}
				}
				debug("aut's package is '" + targetPackage + "'");
			}
			if(targetPackage==null || targetPackage.trim().equals("")){
				debug("can't get aut's package value.");
				packageAttributFound = false;
			}
			
			//Try to get any error message
			err = process.getStderrReader();
			while ((tmpmsg = err.readLine()) != null) {
				errmsg += tmpmsg;
			}

			try {	
				process.waitForSuccess();
			} catch (InterruptedException e) {
				debug("During get aut's package, met Exception="+e.getMessage());
			} catch (RuntimeException re){
				debug("During get aut's package, met Exception="+re.getMessage());
			}
			
			if(!errmsg.equals("")){
				debug("Process Error Message: "+errmsg);
			}
			
		} catch (Throwable e) {
			debug("During get aut's package, met Exception="+e.getMessage());
		} finally{
			try { if(in!=null) in.close(); } catch (IOException e) {}
			try { if(err!=null) err.close(); } catch (IOException e) {}
		}
		
		try{ process.destroy(); process = null;}catch(Exception x){}
		
		if(!packageAttributFound){
			debug("AUT's package value NOT found!");
		}else{
			debug("GOT aut's package: '" + targetPackage + "'");
		}
		
		return targetPackage;
	}
	
	public static final int XML_MODIFIED_FAIL	 	= -1;
	public static final int XML_MODIFIED_SUCCESS 	= 0;
	public static final int XML_MODIFIED_NO_CHANGE 	= 1;
	/**
	 * Modify an attribute's value of a certain tag.
	 * 
	 * @param xmlFile					File, the xml file
	 * @param tagAttributeValueArray,   2-dimension array, the second dimension is 3, containing following	
	 *		  tag			String, the tag to be modified
	 *        attribute		String, the attribute to be modified
	 * 		  value			String, the value will be set to attribute
	 * 
	 * @return				int, XML_MODIFIED_SUCCESS,   if the modification has been done successfully.
	 *                           XML_MODIFIED_FAIL,      if the modification fail.
	 *                           XML_MODIFIED_NO_CHANGE, if no modification is need.
	 */
	public static int modifyAndroidManifestXml(File xmlFile, String[][] tagAttributeValueArray){
		
		try {
			String tag = null;//the tag to be modified
			String attribute = null;//the attribute to be modifie
			String value = null;//the value will be set to attribute
			int noChangeCount = 0;
			
			if(xmlFile==null || tagAttributeValueArray==null){
				debug("xmlFile or tagAttributeValueArray is null!");
				return XML_MODIFIED_FAIL;
			}
			Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
			
			for(int i=0;i<tagAttributeValueArray.length;i++){
				if(tagAttributeValueArray[i]==null || tagAttributeValueArray[i].length!=3){
					debug("tagAttributeValueArray["+i+"] is null or its size is not 3.");
					noChangeCount++;
				}else{
					tag = tagAttributeValueArray[i][0];
					attribute = tagAttributeValueArray[i][1];
					value = tagAttributeValueArray[i][2];
					
					if(tag==null || attribute==null || value==null){
						debug("tag or attribute or value is null!");
						noChangeCount++;
					}else{
						NodeList nodes = dom.getElementsByTagName(tag);
						if (nodes == null || nodes.getLength() == 0) {
							debug("Can't get Node for tag '" + tag + "'");
							return XML_MODIFIED_FAIL;
						}else{
							Node instrumentationNode = nodes.item(0);
							NamedNodeMap attributes = instrumentationNode.getAttributes();
							Node node = attributes.getNamedItem(attribute);
							if(node==null){
								debug("Can't get attribute '" + attribute + "'");
								return XML_MODIFIED_FAIL;
							}
							if(value.equals(node.getNodeValue())){
								debug("'"+value+"'=='"+node.getNodeValue()+"'");
								noChangeCount++;
							}
							node.setNodeValue(value);
						}
					}
				}
			}
			
			if(noChangeCount==tagAttributeValueArray.length){
				return XML_MODIFIED_NO_CHANGE;
			}

			//Write dom to xml file
	        // Prepare the DOM document for writing
	        Source source = new DOMSource(dom);

	        // Prepare the output file
	        Result result = new StreamResult(xmlFile);

	        // Write the DOM document to the file
	        Transformer xformer = TransformerFactory.newInstance().newTransformer();
	        
	        try{
	        	xformer.transform(source, result);
	        }catch(TransformerException e){
	        	result = new StreamResult(new FileOutputStream(xmlFile));
	        	xformer.transform(source, result);
	        }
			
		} catch (Exception e) {
			debug("Met Exception "+e.getMessage());
			return XML_MODIFIED_FAIL;
		}
		
		return XML_MODIFIED_SUCCESS;
	}
	
	/**
	 * We use ant to build the apk.<br>
	 * The method will depend on the build.xml, android-sdk-home, ant-home<br>
	 * 
	 * 
	 * @param appDirString		String, the path where apk source locates, an ant build.xml should exists there.
	 * @param debug				boolean, true --> build in debug; false --> build in release.
	 * @return					boolean, true if the build is successful.
	 */
	public static boolean buildAPK(String appDirString, boolean debug){
		boolean buildSuccess = false;
		if(anttool==null) initAntTool();

		//create a local.properties file to contain sdk.dir=android-sdk-path
		//This file will be needed by ant when building the apk
		String localPropertiesFileString = "local.properties";
		File localPropertiesFile = new File(appDirString+File.separator+localPropertiesFileString);
		if(!localPropertiesFile.exists()){
			String androidhome = System.getenv("ANDROID_HOME");
			if(androidhome==null){
				debug("can't get ANDROID_HOME from environment, please set it");
			}else{
				if(ConsoleTool.isWindowsOS()){
					androidhome = androidhome.replace("\\", "\\\\");
				}else{
					//TODO Do we need to modify androidhome for other OS???
					//this depends the format of file "local.properties"
				}
				String sdkProperty = "sdk.dir="+androidhome;
				PrintWriter wr = null;
				try {
					wr = new PrintWriter(new FileWriter(localPropertiesFile));
					wr.println(sdkProperty);
					wr.flush();
				} catch (IOException e) {
					debug("During write to '"+localPropertiesFileString+"', met Exception="+e.getMessage());
				}finally{
					if(wr!=null) wr.close();
				}
			}
		}else{
			debug(localPropertiesFileString+" file exists.");
		}
		
		//Try to build the apk
		String[] allArgs = null;
		int i=0;
		if(rebuildRunnerAntArgs instanceof String[] && rebuildRunnerAntArgs.length > 0){
			allArgs = new String[rebuildRunnerAntArgs.length + 1];
			for(String anArg: rebuildRunnerAntArgs) allArgs[i++] = anArg;
		}else{
			allArgs = new String[1];
		}
		allArgs[i] = debug ? "debug" : "release"; 
		Process2 process = null;
		
		try {
			File workingDir = new File(appDirString);
			process = anttool.ant(workingDir, allArgs).forwardOutput().waitForSuccess();
			buildSuccess = true;
			
		} catch (Exception e) {
			debug("During build apk, met Exception="+e.getMessage());
		}
		
		try{ process.destroy(); process = null;}catch(Exception x){}
		
		return buildSuccess;
	}
	
	public static String argsToString(String[] args){
		String arguments = "";
		for(int i=0;i<args.length;i++){
			arguments += args[i]+" ";
		}
		
		return arguments;
	}
	
	/**
	 * User may want to rebuild the TestRunner apk according to the "package" of "aut apk" <br>
	 * 
	 * @param	testRunnerSourceDir	String,	the full path where the 'test runner' source is stored.
	 * @param	autApk				String,	the full path of the aut's apk file.
	 * @param	testRunnerApk		String, the full path of the testrunner's apk file.
	 * @param	instrumentArg		String, the instrumentation string.
	 * @param	debug				boolean, true to make a debug build; false to make a release build.
	 * @return						String,	the full path of the rebuilt apk file.<br>
	 *                              null,   if the rebuild fail.<br>
	 */
	public static String rebuildTestRunnerApk(String testRunnerSourceDir, 
			                                   String autApk, 
			                                   String testRunnerApk,
			                                   String instrumentArg,
			                                   final boolean debug){
		String targetPackage = null;
 
		//<manifest package="com.example.android.apis"/>
		//Find the package's value of tag <manifest> in the AUT's AndroidManifest.xml
		if(IS_TEST_TARGET_PACKAGE_SET) targetPackage = TEST_TARGET_PACKAGE;
		else targetPackage = getTargetPackageValue(autApk);
		if(targetPackage==null || targetPackage.trim().equals("")){
			debug("can't get aut's package value.");
			return null;
		}
		
		//Update AndroidManifest.xml the of TestRunner, the following information may need to be modified
		//1. <manifest package="com.jayway.android.robotium.remotecontrol.client">
		//2. <instrumentation android:name="com.jayway.android.robotium.remotecontrol.client.RobotiumTestRunner">
		//3. <instrumentation android:targetPackage="com.example.android.apis">
		
		//Get the AndroidManifest.xml of TestRunner
		//Find the tag <instrumentation>, replace value of attribute 'android:targetPackage' by local variable targetPackage
		//Find the tag <instrumentation>, replace value of attribute 'android:name' according to instrumentArg
		//Find the tag <manifest>, replace value of attribute 'package' according to instrumentArg
		String instrumentationTag = "instrumentation";
		String targetPackageAttribute = "android:targetPackage";
		String nameAttribute = "android:name";
		
		String manifestTag = "manifest";
		String packageAttribute = "package";
		File xmlFile = new File(testRunnerSourceDir+File.separator+MANIFEST_XML_FILENAEM);
		String[][] tagAttributeValueArray = null;
		int secondDimensionLength = 1;
		
		debug("Modifying xml file '"+xmlFile.getAbsolutePath()+"'.");
		debug("set attribute '"+targetPackageAttribute+"' to '"+targetPackage+"' for tag '"+instrumentationTag+"'");
		
		String[] instruments = instrumentArg.split("/");
		if(instruments.length!=2){
			debug("instrument '"+instrumentArg+"' may be wrong.");
		}else{
			secondDimensionLength += 2;
			debug("package="+instruments[0]+"; instrument's name="+instruments[1]);
			debug("set attribute '"+packageAttribute+"' to '"+instruments[0]+"' for tag '"+manifestTag+"'");
			debug("set attribute '"+nameAttribute+"' to '"+instruments[1]+"' for tag '"+instrumentationTag+"'");
		}
		
		tagAttributeValueArray = new String[secondDimensionLength][3];
		//<instrumentation android:targetPackage="aut.package">
		tagAttributeValueArray[0][0] = instrumentationTag;
		tagAttributeValueArray[0][1] = targetPackageAttribute;
		tagAttributeValueArray[0][2] = targetPackage;
		if(secondDimensionLength==3){
			//<manifest package="runner.package">
			tagAttributeValueArray[1][0] = manifestTag;
			tagAttributeValueArray[1][1] = packageAttribute;
			tagAttributeValueArray[1][2] = instruments[0];
			//<instrumentation android:name="Runner">
			tagAttributeValueArray[2][0] = instrumentationTag;
			tagAttributeValueArray[2][1] = nameAttribute;
			tagAttributeValueArray[2][2] = instruments[1];
		}
		
		int modifyRC = modifyAndroidManifestXml(xmlFile, tagAttributeValueArray);
		if(XML_MODIFIED_FAIL==modifyRC){
			debug("Fail to modify xml file '"+xmlFile.getAbsolutePath()+"'.");
			return null;
		}else if(XML_MODIFIED_NO_CHANGE==modifyRC){
			if(!rebuildRunnerForce){
				debug("No need to rebuild test runner apk. '"+testRunnerApk+"'.");
				return testRunnerApk;
			}
			else{
				debug("Forced override for Runner rebuild despite unmodified AndroidManifest.xml.");
			}
		}
		
		//Rebuild the apk with the modified AndroidManifest.xml
		debug("Rebuilding the apk with the modified AndroidManifest.xml");
		if(!buildAPK(testRunnerSourceDir, debug)){
			debug("Fail to build the test runner apk.");
			return null;
			
		}else{
			String apkSubDir = "bin";
			//Replace testRunnerApk with the rebuilt apk
			File originalApk = new File(testRunnerApk);
			File generatedApkFile = new File(testRunnerSourceDir+File.separator+apkSubDir+File.separator+originalApk.getName());
			if(!generatedApkFile.exists()){
				//we need to get the apk from directory testRunnerSourceDir+File.separator+apkSubDir
				File builtDir = new File(testRunnerSourceDir+File.separator+apkSubDir);
				File[] apkfiles = builtDir.listFiles(new FilenameFilter(){
					public boolean accept(File dir, String name) {
						boolean accepted = false;
						if(name!=null){
							String lcname = name.toLowerCase();
							if(debug) accepted = lcname.endsWith("debug.apk");
							else      accepted = lcname.endsWith("release.apk");
						}
						return accepted;
					}
				});
				
				if(apkfiles!=null && apkfiles.length>0){
					generatedApkFile = apkfiles[0];				
				}
			}
			
			debug("Replacing testRunnerApk with '"+generatedApkFile.getAbsolutePath()+"'.");
			if(!generatedApkFile.exists() || !generatedApkFile.isFile()){
				debug("Fail to replace testRunnerApk with '"+generatedApkFile.getAbsolutePath()+"'.");
				return null;
			}
			
			return generatedApkFile.getAbsolutePath();
		}
		
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

	public static final String RESIGN_JAR_ENV = "RESIGN_JAR_ENV";
	public static String getResignJarFileName(){
		String resignjar = System.getenv(RESIGN_JAR_ENV);
		String fullname = null;
		try{
			File file = new File(resignjar);
			if(file.exists() && file.isFile()){
				fullname = file.getAbsolutePath();
			}
		}catch(Exception e){}
		
		return fullname;
	}
	
	/**
	 * This method is used to resign AUT {@link #TEST_TARGET_APP} automatically.<br>
	 * Before calling this method, the field {@link #RESIGN_JAR_FULL_NAME} needs to be set.<br>
	 * This method should be called before method {@link #installEnabledAPKs()}<br>
	 * @return true, if the aut is resigned successfully or the aut doesn't need to be resigned.
	 * 
	 * @see #TEST_TARGET_APP
	 * @see #RESIGN_JAR_FULL_NAME
	 * @see #installAUT
	 * @see #installEnabledAPKs()
	 */
	public static boolean resignAUTApk(){
		if(installAUT && RESIGN_JAR_FULL_NAME!=null){
			return resignAUTApk(RESIGN_JAR_FULL_NAME, TEST_TARGET_APP);
		}else{
			return true;
		}
	}
	/**
	 * This method is used to resign an android apk.<br>
	 * @param resignJar		The full name of re-sign.jar
	 * @param apkFullName	The full name of android apk to be resigned.
	 * @return	true, if the resign succeed.
	 */
	public static boolean resignAUTApk(String resignJar, String apkFullName){
		try {
			Console console = Console.get();
			Process2 process = console.batch(null, "java", "-jar", resignJar, apkFullName, apkFullName);
			process.forwardOutput().waitForSuccess(60).destroy();
			process = null;
			return true;
		} catch (Exception e) {
			debug("During resign aut apk, met Exception="+e.getMessage());
			return false;
		}
	}
}