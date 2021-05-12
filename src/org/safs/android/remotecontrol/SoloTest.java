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
package org.safs.android.remotecontrol;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.safs.android.auto.lib.DUtilities;
import org.safs.sockets.RemoteException;
import org.safs.sockets.ShutdownInvocationException;

import com.jayway.android.robotium.remotecontrol.solo.RemoteSoloException;
import com.jayway.android.robotium.remotecontrol.solo.RobotiumUtils;
import com.jayway.android.robotium.remotecontrol.solo.Solo;
import com.jayway.android.robotium.remotecontrol.solo.Timeout;

/**
 * This is used to test Remote Solo Implementation with a SAFSTestRunner.<br>
 * The tested Activity is "com.android.example.spinner.SpinnerActivity"<br>
 * If testing with another Activity, we suggest to subclass this program.<br>
 * <p>
 * 
 * <b>Prerequisite:</b>
 * <pre>
 * 1. Set the android sdk dir (use one of 2 ways):
 * 
 *    1.1 set environment ANDROID_HOME
 *    1.2 launch application with JVM property as: -Dandroid-home="D:\\your\\android-sdk-dir"
 *    
 * 2. (Optional)Set the ant sdk dir if you need rebuild the test-runner-apk (use one of 2 ways):
 * 
 *    2.1 set environment ANT_HOME
 *    2.2 launch application with JVM property as: -Dant-home="D:\\your\\ant-sdk-dir"
 *       
 * 3. Command-line args to set the APK of AUT, SAFSMessenger and SAFSTestRunner to be installed:
 *    
 *    aut="C:\\SAFS\\samples\\Droid\\SpinnerSample\\bin\\SpinnerActivity-debug.apk"  (sets installAUT=true)
 *    messenger="C:\\SAFS\\samples\\Droid\\SAFSTCPMessenger\\bin\\SAFSTCPMessenger-debug.apk"  (sets installMessenger=true)
 *    runner="C:\\SAFS\\samples\\Droid\\SAFSTestRunner\\bin\\SAFSTestRunner-debug.apk"   (sets installRunner=true)
 *      (or runnersource=d:\\testRunnerSourcePath like below:)
 *    runnersource="C:\\SAFS\\samples\\Droid\\SAFSTestRunner" (sets rebuildRunner=true) 
 *    
 *    OR, to bypass individual installs and use what is already installed:
 *    
 *    -noaut
 *    -nomessenger
 *    -norunner
 *    
 *    
 * 4. Command-line arg to set the instrument:
 * 
 *    instrument=org.safs.android.engine/org.safs.android.engine.DSAFSTestRunner
 * 
 * 5. Command-line arg to set the avd name to launch, or the device/emulator serial number to look for:
 *    
 *    avd="avdName" or avd="devOrEmuSerial"
 *    
 * 6. Command-line arg to set the persistence of launched emulator:
 *    
 *    persistavd="True" or persistavd="False" (default=false)
 *    
 * 7. Command-line arg to set to resign AUT automatically:
 *    
 *    resignjar=C:\\safs\\lib\\re-sign.jar
 *    
 * 8. Command-line arg to set to remove installed APKs automatically after test finish:
 *    
 *    -removeinstalledapk
 *    
 * </pre>
 *   
 * <p><pre>  
 * Run as:
 * 
 *   java org.safs.android.remotecontrol.SoloTest aut="C:\\buildFilePath\\SpinnerActivity-debug.apk" messenger="c:\\buildFilePath\\SAFSTCPMessenger-debug.apk" runner="c:\\buildFilePath\\SAFSTestRunner-debug.apk" instrument=org.safs.android.engine/org.safs.android.engine.DSAFSTestRunner
 *   
 *   or, rebuild testrunner and use it
 *   
 *   java -Dant-home="C:\\pathTo\\ant-sdk" org.safs.android.remotecontrol.SoloTest aut="C:\\buildFilePath\\SpinnerActivity-debug.apk" messenger="c:\\buildFilePath\\SAFSTCPMessenger-debug.apk" runnersource="c:\\buildSourcePath\\" instrument=org.safs.android.engine/org.safs.android.engine.DSAFSTestRunner
 *   
 *   or, assuming everything is already installed:
 *   
 *   java -Dandroid-home="C:\\pathTo\\android-sdk" org.safs.android.remotecontrol.SoloTest -noaut -nomessenger -norunner instrument=org.safs.android.engine/org.safs.android.engine.DSAFSTestRunner
 *   
 *   
 * </pre>
 * @author Carl Nagle, SAS Institute, Inc.
 * <br>(Lei Wang)	AUG 16, 2013	Add option to permit remove installed apks automatically after test finish.
 *
 */
public class SoloTest{
	public Solo solo = null;
	public SAFSEngineCommands engine = null;
	public SAFSDriverCommands driver = null;
	public RobotiumUtils robotiumUtils = null;
	public Timeout robotiumTimeout = null;
	
	private boolean enableProtocolDebug = true;
	private boolean enableRunnerDebug = true;
	
	private boolean installAUT = true;
	private boolean installMessenger = true;
	private boolean installRunner = true;
	
	/**
	 * Rebuilding testrunner will depend on {@link #autApk} and {@link #testRunnerSourceDir}
	 * 
	 */
	private boolean rebuildRunner = false;
	
	/**
	 * resignJar is the path to JAR file used to resign<br>
	 * it will be modified by the value pass in as 'resignjar=' argument<br>
	 */
	private String resignJar = null;
	
	/**
	 * autApk is the path to AUT's apk<br>
	 * it is set to {@link #DEFAULT_AUT_APK} by default<br>
	 * it will be modified by the value pass in as 'aut=' argument<br>
	 */
	private String autApk = DEFAULT_AUT_APK;
	/**
	 * messengerApk is the path to messenger's apk<br>
	 * it is set to {@link #DEFAULT_MESSENGER_APK} by default<br>
	 * it will be modified by the value pass in as 'messenger=' argument<br>
	 */
	private String messengerApk = DEFAULT_MESSENGER_APK;
	/**
	 * messengerApk is the path to testrunner's apk<br>
	 * it is set to {@link #DEFAULT_TESTRUNNER_APK} by default<br>
	 * it will be modified by the value pass in as 'runner=' argument<br>
	 */
	private String testRunnerApk = DEFAULT_TESTRUNNER_APK;
	
	/**
	 * testRunnerSourceDir is the path where the testrunner's source locates<br>
	 * it is set to {@link #DEFAULT_TESTRUNNER_SOURCE_DIR} by default<br>
	 * it will be modified by the value pass in as 'runnersource=' argument<br>
	 */
	private String testRunnerSourceDir = DEFAULT_TESTRUNNER_SOURCE_DIR;
	
	/**
	 * messengerApk is the path to messenger's apk<br>
	 * it is set to {@link #DEFAULT_INSTRUMENT_ARG} by default<br>
	 * it will be modified by the value pass in as 'instrument=' argument<br>
	 */
	private String instrumentArg = DEFAULT_INSTRUMENT_ARG;
	
	/**
	 * avdSerialNo is device or emulator's serial number, it is "" by default<br>
	 * it will be modified by the value pass in as 'avd=' argument<br>
	 * 
	 * If there is no device/emulator is attached/launched, we will try to<br>
	 * launch the emulator with this serial number.<br>
	 * If there is one device/emulator is attached/launched, we will ignore<br>
	 * this serial number.<br>
	 * If there are multiple devices/emulators are attached/launched, we<br>
	 * use this serial number to locate the device/emulator.<br>
	 *  
	 */
	protected String avdSerialNo = "";

	/**
	 * If this is true, we will keep the launched emulator running even after the<br>
	 * test has finished.<br>
	 * The default value is false.<br>
	 * it will be modified by the value pass in as 'persistavd=' argument<br>
	 * 
	 */
	protected boolean persistEmulators = false;

	protected boolean weLaunchedEmulator = false;
	
	/** flag defaults to true to unlock the screen of any emulator we wish to connect to. */
	protected boolean unlockEmulatorScreen = true;
	
	/**
	 * This field will store the activity UID of you launch AUT.<br>
	 * It will be set in method {@link #initialize()}<br>
	 * 
	 * It will be used in method {@link #goBackToViewUID(String)} to prevent infinite loop.<br>
	 */
	protected String mainActivityUID = null;
	
	/** true if we received the 'aut' command-line argument, or setAUTApk() was called. */
	public boolean argAUTpassed = false;
	/** true if we received the 'messenger' command-line argument, or setMessengerApk() was called. */
	public boolean argMESSENGERpassed = false;
	/** true if we received the 'runner' command-line argument, or setTestRunnerApk() was called. */
	public boolean argRUNNERpassed = false;
	/** true if we received the 'instrument' command-line argument, or setInstrumentArg() was called. */
	public boolean argINSTRUMENTpassed = false;
	/** true if we received the 'resignjar' command-line argument, or setResignJar() was called. */
	public boolean argRESIGNJARpassed = false;
	/** true if we received the 'runnersource' command-line argument, or setTestRunnerSourceDir() was called. */
	public boolean argRUNNERSOURCEpassed = false;
	/**
	 * default is false. 
	 * true if we received the '-removeinstalledapk' command-line argument, or setRemoveinstalledapk(true) was called. 
	 * If this is true, all installed apk including 'aut', 'runner' and 'messenger' will be removed.*/
	public boolean removeinstalledapk = false;
	
	/** 
	 * Modify {@link #DEFAULT_AUT_APK} according to your system.<br>
	 * Modify {@link #DEFAULT_MESSENGER_APK} according to your system.<br>
	 * Modify {@link #DEFAULT_TESTRUNNER_APK} according to your system.<br>
	 * Modify {@link #DEFAULT_TESTRUNNER_SOURCE_DIR} according to your system.<br>
	 * Modify {@link #DEFAULT_INSTRUMENT_ARG} according to your system.<br>
	 */
	public static final String DEFAULT_AUT_APK = "C:\\SAFS\\samples\\Droid\\SpinnerSample\\bin\\SpinnerActivity-debug.apk";
	public static final String DEFAULT_MESSENGER_APK = "C:\\SAFS\\samples\\Droid\\SAFSTCPMessenger\\bin\\SAFSTCPMessenger-debug.apk";
	public static final String DEFAULT_TESTRUNNER_APK = "C:\\SAFS\\samples\\Droid\\SAFSTestRunner\\bin\\SAFSTestRunner-debug.apk";
	public static final String DEFAULT_TESTRUNNER_SOURCE_DIR = "C:\\SAFS\\samples\\Droid\\SAFSTestRunner";
	public static final String DEFAULT_INSTRUMENT_ARG = "org.safs.android.engine/org.safs.android.engine.DSAFSTestRunner";
	
	public static final String ARG_KEY_RESIGN_JAR = "resignjar";
	public static final String ARG_KEY_AUT_APK = "aut";
	public static final String ARG_KEY_MESSENGER_APK = "messenger";
	public static final String ARG_KEY_TESTRUNNER_APK = "runner";
	public static final String ARG_KEY_TESTRUNNER_SOURCE = "runnersource";
	public static final String ARG_KEY_INSTRUMENT_ARG = "instrument";
	public static final String ARG_KEY_AVD 			  = "avd";
	public static final String ARG_KEY_PERSIST_AVD	  = "persistavd";

	public static final String ARG_KEY_NO_MESSENGER = "-nomessenger";
	public static final String ARG_KEY_NO_RUNNER = "-norunner";
	public static final String ARG_KEY_NO_AUT = "-noaut";
	
	public static final String ARG_KEY_REMOVE_INSTALLED_APK = "-removeinstalledapk";
	
	/**
	 * Instantiates the Solo instance but does not yet initialize it.
	 * @see #solo
	 * @see #engine
	 */
	public SoloTest(){
		solo = new Solo();
		
		//By default, we disable the debug message of Protocol/Runner so that the console
		//show only the test log message.
		setProtocolDebug(false);
		setRunnerDebug(false);
	}
	
	public SoloTest(String messengerApk, String testRunnerApk, String instrumentArg){
		this();
		this.setMessengerApk(messengerApk);
		this.setTestRunnerApk(testRunnerApk);
		this.setInstrumentArg(instrumentArg);
	}
	
	/**
	 * @param args	Array of Strings: "aut=xxx", "messenger=xxx", "runner=xxx", "runnersource=xxx", "instrument=xxx"
	 */
	public SoloTest(String[] args){
		this();
		//Get 'apk path' and 'instrument' from program's parameters
		//and set them to soloTest object.
		String temp = "";
		String[] tempArray = null;
		for(int i=0;i<args.length;i++){
			temp=args[i];
			tempArray = temp.split("=");
			if(tempArray!=null && tempArray.length==2){
				//use temp to contain the parameter key
				temp = tempArray[0].trim();
				if(ARG_KEY_AUT_APK.equalsIgnoreCase(temp)){
					setAUTApk(tempArray[1]); 
				}else if(ARG_KEY_RESIGN_JAR.equalsIgnoreCase(temp)){
					setResignJar(tempArray[1]); 
				}else if(ARG_KEY_MESSENGER_APK.equalsIgnoreCase(temp)){
					setMessengerApk(tempArray[1]); 
				}else if(ARG_KEY_TESTRUNNER_APK.equalsIgnoreCase(temp)){
					setTestRunnerApk(tempArray[1]);
				}else if(ARG_KEY_INSTRUMENT_ARG.equalsIgnoreCase(temp)){
					setInstrumentArg(tempArray[1]); 						
				}else if(ARG_KEY_TESTRUNNER_SOURCE.equalsIgnoreCase(temp)){
					setTestRunnerSourceDir(tempArray[1]);
				}else if(ARG_KEY_AVD.equalsIgnoreCase(temp)){
					avdSerialNo = tempArray[1];
				}else if(ARG_KEY_PERSIST_AVD.equalsIgnoreCase(temp)){
					try{ persistEmulators = Boolean.parseBoolean(tempArray[1]);} catch(Exception e){}
				}
			}else{
				if(tempArray.length == 1){
					if(ARG_KEY_NO_AUT.equalsIgnoreCase(temp)){
						installAUT = false; 
					}else if(ARG_KEY_NO_MESSENGER.equalsIgnoreCase(temp)){
						installMessenger = false; 
					}else if(ARG_KEY_NO_RUNNER.equalsIgnoreCase(temp)){
						installRunner = false;
					}
				}
			}
		}
	}
	
	public String getTestRunnerSourceDir() {
		return testRunnerSourceDir;
	}

	public void setTestRunnerSourceDir(String testRunnerSourceDir) {
		this.testRunnerSourceDir = testRunnerSourceDir;
		if(testRunnerSourceDir != null) {
			this.argRUNNERSOURCEpassed = true;
			this.setRebuildRunner(true);
		}
	}
	
	public boolean isRemoveinstalledapk() {
		return removeinstalledapk;
	}

	public void setRemoveinstalledapk(boolean removeinstalledapk) {
		this.removeinstalledapk = removeinstalledapk;
	}
	
	public String getResignJar() {
		return resignJar;
	}

	public void setResignJar(String resignJar) {
		this.resignJar = resignJar;
		if(resignJar != null) this.argRESIGNJARpassed = true;
	}

	public String getAUTApk() {
		return autApk;
	}

	public void setAUTApk(String autApk) {
		this.autApk = autApk;
		if(autApk != null) this.argAUTpassed = true;
	}

	public String getMessengerApk() {
		return messengerApk;
	}

	public void setMessengerApk(String messengerApk) {
		this.messengerApk = messengerApk;
		if(messengerApk != null) this.argMESSENGERpassed = true;
	}

	public String getTestRunnerApk() {
		return testRunnerApk;
	}

	public void setTestRunnerApk(String testRunnerApk) {
		this.testRunnerApk = testRunnerApk;
		if(testRunnerApk != null) this.argRUNNERpassed = true;
	}

	public String getInstrumentArg() {
		return instrumentArg;
	}

	public void setInstrumentArg(String instrumentArg) {
		this.instrumentArg = instrumentArg;
		if(instrumentArg != null) this.argINSTRUMENTpassed = true;
	}
	
	/**
	 * Turn on or off the protocol's debug message
	 * @param enable
	 */
	public void setProtocolDebug(boolean enableProtocolDebug){
		this.enableProtocolDebug = enableProtocolDebug;
	}
	/**
	 * Turn on or off the runner's debug message
	 * @param enable
	 */
	public void setRunnerDebug(boolean enableRunnerDebug){
		this.enableRunnerDebug = enableRunnerDebug;
	}
		
	public void setInstallAUT(boolean installAUT) {
		this.installAUT = installAUT;
	}

	public void setInstallMessenger(boolean installMessenger) {
		this.installMessenger = installMessenger;
	}

	public void setInstallRunner(boolean installRunner) {
		this.installRunner = installRunner;
	}

	/**
	 * Rebuilding testrunner will depend on {@link #autApk} and {@link #testRunnerSourceDir}
	 * 
	 * @param rebuildRunner
	 */
	public void setRebuildRunner(boolean rebuildRunner) {
		this.rebuildRunner = rebuildRunner;
	}

	/**
	 * A template method.<br>
	 * 
	 * @see #preparation()
	 * @see #initialize()
	 * @see #test()
	 * @see #terminate()
	 */
	final public void process(){
		if(!preparation()){
			error("Preparation error");
			//stop the emulator if we have launched it.
			if(!stopEmulator()){
				warn("We fail to stop the emulator launched by us.");
			}
			return;
		}
		try{
			if(initialize()){
				debug("Begin Test.");
				test();
				debug("End Test.");
			}			
		}catch(Exception e){
			error("Met Exception "+e.getClass().getSimpleName()+":"+e.getMessage());
		}finally{
			//Whether the 'remote engine' has been started or not
			//we will call the method terminate() to stop the local controller runner.
			if(!terminate()){
				warn("Termination of Solo fail!");
			}			
		}
		
	}
	
	/**
	 * Install the apk of SAFSTCPMessenger and SAFSTestRunner<br>
	 * Forward the tcp port from on-computer-2411 to on-device-2410<br>
	 * 
	 * @return	True if the preparation is finished successfully.
	 */
	final protected boolean preparation(){
		String debugPrefix = ".preparation() ";

		try{
			if(!prepareDevice()){
				throw new RuntimeException("Can't detect connected device/emulator!");
			}
			
			// 1. Install apks
			if (installAUT) {
				if(getResignJar()!=null){
					debug("RESIGNING " + autApk);
					DUtilities.resignAUTApk(resignJar, autApk);
				}
				debug("INSTALLING " + autApk);
				DUtilities.installReplaceAPK(autApk);
			} else {
				debug("BYPASSING INSTALLATION of target AUT...");
			}
	
			if (installMessenger) {
				debug("INSTALLING " + messengerApk);
				DUtilities.installReplaceAPK(messengerApk);
			} else {
				debug("BYPASSING INSTALLATION of SAFS Messenger...");
			}
	
			// Before installing the TestRunner apk, we may repackage it.
			if (installRunner) {
				if (rebuildRunner) {
					debug("REBUILDING " + testRunnerApk);
					testRunnerApk = DUtilities.rebuildTestRunnerApk(testRunnerSourceDir, autApk, testRunnerApk, instrumentArg, true);
					if (testRunnerApk==null) {
						throw new RuntimeException(debugPrefix + " Fail to repackage the TestRunner apk!");
					}
				}
	
				debug("INSTALLING " + testRunnerApk);
				DUtilities.installReplaceAPK(testRunnerApk);
			} else {
				debug("BYPASSING INSTALLATION of Instrumentation Test Runner...");
			}
	
			// 2. Launch the InstrumentationTestRunner
			if(!DUtilities.launchTestInstrumentation(instrumentArg)){
				throw new RuntimeException("Fail to launch instrument '"+instrumentArg+"'");
			}
			// 3. Forward tcp port is needed? how to know the way of connection, by
			// USB? by WIFI?
			boolean portForwarding = true;
			solo.setPortForwarding(portForwarding);
				
			debug("Prepare for test successfully.");

		}catch(RuntimeException e){
			error("During preparation, met exception="+e.getMessage());
			return false;
		}
		
		return true;
	}
	
	public boolean prepareDevice(){
		// see if any devices is attached
		boolean havedevice = false;
		
		List<String> devices = null;
		try{
			devices = DUtilities.getAttachedDevices();
			info("Detected "+ devices.size() +" device/emulators attached.");
			if(devices.size() == 0){
				DUtilities.DEFAULT_EMULATOR_AVD = avdSerialNo;
				if((DUtilities.DEFAULT_EMULATOR_AVD != null) && (DUtilities.DEFAULT_EMULATOR_AVD.length()> 0)){
					//DUtilities.killADBServer();
					//try{Thread.sleep(5000);}catch(Exception x){}
					info("Attempting to launch EMULATOR_AVD: "+ DUtilities.DEFAULT_EMULATOR_AVD);
					if (! DUtilities.launchEmulatorAVD(DUtilities.DEFAULT_EMULATOR_AVD)){
						String msg = "Unsuccessful launching EMULATOR_AVD: "+DUtilities.DEFAULT_EMULATOR_AVD +", or TIMEOUT was reached.";
						debug(msg);
						return false;							
					}else{
						weLaunchedEmulator = true;
						info("Emulator launch appears to be successful...");
						havedevice = true;
						if(unlockEmulatorScreen) {
							String stat = DUtilities.unlockDeviceScreen()? " ":" NOT ";
							info("Emulator screen was"+ stat +"successfully unlocked!");
						}						
					}
				}else{
					String msg = "No Devices found and no EMULATOR_AVD specified in configuration file.";
					debug(msg);
					return false;							
				}				
			}else if(devices.size() > 1){
				// if multiple device attached then user DeviceSerial to target device
				DUtilities.DEFAULT_DEVICE_SERIAL = avdSerialNo;
				if(DUtilities.DEFAULT_DEVICE_SERIAL.length() > 0){
					boolean matched = false;
					int d = 0;
					String lcserial = DUtilities.DEFAULT_DEVICE_SERIAL.toLowerCase();
					String lcdevice = null;
					for(;(d < devices.size())&&(!matched);d++){
						lcdevice = ((String)devices.get(d)).toLowerCase();
						info("Attempting match device '"+ lcdevice +"' with default '"+ lcserial +"'");
						matched = lcdevice.startsWith(lcserial);
					}
					// if DeviceSerial does not match one of multiple then abort
					if(matched){
						havedevice = true;
						DUtilities.USE_DEVICE_SERIAL = " -s "+ DUtilities.DEFAULT_DEVICE_SERIAL +" ";
					}else{
						String msg = "Requested Device '"+ DUtilities.DEFAULT_DEVICE_SERIAL +"' was not found.";
						debug(msg);
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
						debug(msg);
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
			
		}catch(Exception x){
			debug("Aborting due to "+x.getClass().getSimpleName()+", "+ x.getMessage());
			return false;
		}			
		
		return havedevice;
	}
	
	/**
	 * Initialize the Solo object and the SAFSEngineCommands instance and launches the main application.<br>
	 * You will not modify this method in the sub-class, normally<br>
	 * 
	 * @return true if the initialization is successful.
	 */
	final protected boolean initialize(){
		boolean success = false;
		
		try {
			solo.initialize();
			engine = new SAFSEngineCommands(solo);
			driver = new SAFSDriverCommands(solo);
			robotiumUtils = new RobotiumUtils(solo);
			robotiumTimeout = new Timeout(solo);
			
			//We can enable/disable the debug message of Protocol or Runner
			solo.turnProtocolDebug(enableProtocolDebug);
			solo.turnRunnerDebug(enableRunnerDebug);
			
			//Start the main Activity
			success = solo.startMainLauncher();
			//Set the mainActivityUID
			mainActivityUID = solo.getCurrentActivity();
			debug("mainActivityUID="+mainActivityUID);
			
		} catch (IllegalThreadStateException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (ShutdownInvocationException e) {
			e.printStackTrace();
		}
		
		if(success){
			debug("Launch application successfully.");
		}else{
			error("Fail to launch application.");
		}
		return success; 
	}
	
	/**
	 * Terminate the remote engine.<br>
	 * Terminate the local controller runner.<br>
	 * Terminate the emulator if we have started it.<br>
	 * 
	 * You will not modify this method in the sub-class.<br>
	 * 
	 * @return true if the initialization is successful.
	 */
	final protected boolean terminate(){
		boolean success = false;

		driver = null;
		engine = null;
		robotiumUtils = null;
		robotiumTimeout = null;
		
		try {			
			if(!solo.shutdownRemote()){
				warn("Fail to shutdown remote service.");
			}
			//Even if we fail to shutdown remote service, we will shutdown RemoteControlRunner
			solo.shutdown();
			
			if(removeinstalledapk) removeInstalledAPK();
			
			if(!stopEmulator()){
				warn("We fail to stop the emulator launched by us.");
			}
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(success){
			debug("terminate successfully.");
		}else{
			warn("Fail to terminate.");
		}
		return success;
	}
	
	final void removeInstalledAPK(){
		try{
			DUtilities.uninstallAPK(autApk, true);
		}catch(Exception e){
			warn(e.getMessage());
		}
		try{
			DUtilities.uninstallAPK(messengerApk, true);
		}catch(Exception e){
			warn(e.getMessage());
		}
		try{
			DUtilities.uninstallAPK(testRunnerApk, true);
		}catch(Exception e){
			warn(e.getMessage());
		}
	}
	
	/**
	 * Stop the emulator launched by us only if we have launched it and<br>
	 * we don't want to persist it.<br>
	 * 
	 * @return boolean, true if the emulator is stopped successfully or we don't need to stop it.
	 */
	final protected boolean stopEmulator(){
		boolean stopped = true;
		
		if(weLaunchedEmulator){
			//Then we will shutdown any emulator lauched by us.
			if(!persistEmulators) {
				info(" checking for launched emulators...");
				stopped = DUtilities.shutdownLaunchedEmulators(weLaunchedEmulator);		  	
			}else{
				info(" attempting to PERSIST any launched emulators...");
			}
		}else{
			info("we didn't start any emulators.");
		}
		
		return stopped;
	}
	
	/**
	 * Use solo and engine to test.<br>
	 * You will extend this method in the sub-class, normally<br>
	 * 
	 * In the SAFSTestRunner project, there is a file AndroidManifest.xml:<br>
	 * There is a tag <instrumentation> like following:
	 * 
	 * <instrumentation android:name="org.safs.android.engine.DSAFSTestRunner"
	 *                  android:targetPackage="com.android.example.spinner"
	 *                  android:label="General-Purpose SAFS Test Runner"/>
	 *                  
	 * If you want to test another application, you should modify the property "android:targetPackage"<br>
	 * For example, android:targetPackage="your.test.application.package"<br>
	 * 
	 * And you need to override this method to do the test work.<br>
	 */
	protected void test(){
		//Begin the testing
		try {
			
			String activityID = solo.getCurrentActivity();
			Properties props = solo._last_remote_result;
			String activityName = props.getProperty(SAFSMessage.PARAM_NAME);
			String activityClass = props.getProperty(SAFSMessage.PARAM_CLASS);

			info("CurrentActivity   UID: "+ activityID);
			info("CurrentActivity Class: "+ activityClass);				
			info("CurrentActivity  Name: "+ activityName);
			
			//DEBUG: Verifying the Name we return is the same one used by waitForActivity
			if(solo.waitForActivity(activityName, 1000)){
				pass("'"+activityName+"' was found in timeout period.");
			}else{
				warn("*** '"+activityName+"' was NOT FOUND in timeout period. ***");
			}
			// NEGATIVE TEST
			if(solo.waitForActivity("BoguActivity", 1000)){
				warn("*** BogusActivity was reported as found but is not a valid Activity! ***");
			}else{
				pass("BogusActivity was not found and was not expected to be found.");
			}

			String layoutUID = solo.getView("android.widget.LinearLayout", 0);			
			info("Layout UID= "+layoutUID);
			
			String listUID = solo.getView("android.widget.Spinner", 0);
			info("Spinner UID= "+listUID);
			
			// SCREENSHOT TESTS			
			BufferedImage image = solo.takeScreenshot("ActivityScreenshot");
			if(image != null) {
				info("Solo Screenshot image  width:"+ image.getWidth());
				info("Solo Screenshot image height:"+ image.getHeight());
				info("Solo Screenshot stored at: "+ solo._last_remote_result.getProperty(SAFSMessage.PARAM_NAME+"FILE"));
			}
			else info("Solo Screenshot returned as NULL!");

			// SAFSDriverCommands SCREENSHOT TEST
			image = driver.getScreenShot(true);
			if(image != null){
				info("SAFS Screenshot image  width:"+ image.getWidth());
				info("SAFS Screenshot image height:"+ image.getHeight());
			}
			else info("SAFS Screenshot returned as NULL!");
			
			// SAFS ENGINE COMMANDS HERE
			int ccount = engine.getChildCount(layoutUID);
			info("Layout childCount= "+ccount);

			ccount = engine.getChildCount(listUID);
			info("Spinner childCount= "+ccount);
			
			String win = engine.getCurrentWindow();
			info("CurrentWindow UID= "+win);
			
			String svalue = engine.getClassname(win);
			info("CurrentWindow class= "+svalue);
			
			String[] sarray = engine.getSuperclassNames(win);
			for(int i=0;i<sarray.length;i++){
				info("CurrentWindow superclass= "+ sarray[i]);
			}
			
			sarray = engine.getPropertyNames(win);
			String propname = null;
			for(int i=0;i<sarray.length;i++){
				propname = sarray[i];
				svalue = null;
				try{ 
					svalue = engine.getProperty(win, propname);
				}catch(Exception x){
					svalue = x.getClass().getSimpleName()+" "+x.getMessage();
				}
				info("CurrentWindow "+ propname +" = '"+ svalue +"'");
			}
			
			// testing Solo cacheReference chaining to EngineProcessor cache
			boolean clickok = solo.clickOnView(win);
			info("CurrentWindow solo.clickOnView = "+clickok);
			info("CurrentWindow solo.clickOnView clicked on class: "+ solo._last_remote_result.getProperty(SAFSMessage.PARAM_CLASS));
			
			// SAFS DRIVER COMMANDS HERE
			try{
				svalue = driver.getClipboard();
				info("driver.getClipboard = '"+ svalue +"'");
				
				driver.clearClipboard();
				info("driver.clearClipboard()");
	
				svalue = driver.getClipboard();			
				info("driver.getClipboard = '"+ svalue +"'");
				
				driver.setClipboard("SAFS Driver placed this on the ClipBoard.");
				info("driver.setClipboard()");
				
				svalue = driver.getClipboard();
				info("driver.getClipboard = '"+ svalue +"'");
			}catch(Exception x){
				warn("Older Android API may not support Clipboard Manager: "+ x.getMessage());
			}

			clickok = driver.showSoftKeyboard();
			info("driver.showSoftKeyboard() success ? "+ clickok);

			clickok = driver.hideSoftKeyboard();
			info("driver.hideSoftKeyboard() success ? "+ clickok);
			
			// SHUTDOWN all Activities.  Done Testing.
			if(solo.finishOpenedActivities()){
				info("Application finished/shutdown without error.");				
			}else{
				warn("Application finished/shutdown with error.");
			}

		} catch (IllegalThreadStateException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (ShutdownInvocationException e) {
			e.printStackTrace();
		} catch (RemoteSoloException e) {
			e.printStackTrace();
		}
	}
	
	public void debug(String message) {
		System.out.println("SoloTest DEBUG: "+message);
	}
	public void info(String message) {
		System.out.println("SoloTest INFO: "+message);
	}
	public void warn(String message) {
		System.err.println("SoloTest WARN: "+message);
	}

	public void pass(String message) {
		System.out.println("SoloTest PASS: "+message);		
	}
	public void fail( String message) {
		System.err.println("SoloTest FAIL: "+message);
	}
	public void error( String message) {
		System.err.println("SoloTest ERROR: "+message);
	}
	
	/**
	 * @param viewUID
	 * @throws Exception
	 */
	protected void goBackToViewUID(String viewUID) throws Exception{
		int loopLimit = 10;
		int looptime = 0;
		
		do{
			if(solo.waitForViewUID(viewUID, 50, true)){
				debug("Back to view "+viewUID);
				return;
			}else{
				//solo.getCurrentActivity() will never return null? There is always an activity.
				String currentActivity = solo.getCurrentActivity();
				debug("Current Activity is "+currentActivity);
				if(currentActivity==null || currentActivity.equals(mainActivityUID)){
					debug("Exit the main activity!!!");
					break;
				}else{
					debug("Trying go back...");
				}
			}
			looptime++;
		}while((looptime<loopLimit) && solo.goBack());
		
		debug("Can not go back to view "+viewUID);
	}
	
	protected void pause(int millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected void scrollToTop(){
		//scroll to the top of the list
		try {
			while(solo.scrollUp()){
				debug("Scrolling up......");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void scrollToBottoum(){
		//scroll to the bottom of the list
		try {
			while(solo.scrollDown()){
				debug("Scrolling down......");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Wrap the text with regex prefix and suffix ".*"<br>
	 * When you call some method like {@link Solo#clickOnText(String)}, the parameter<br>
	 * can be regex string to match more, you can call this method to wrap the parameter.<br>
	 * 
	 * @param text	String
	 * @return		String, .*text.*
	 */
	protected String wrapRegex(String text){
		if(text==null) return text;
		return ".*"+text+".*";
	}
	
	/**
	 * @param args	Array of String passed from command line:
	 *   messenger=xxx  
	 *   runner=xxx 
	 *   runnersource=xxx  
	 *   instrument=xxx 
	 */
	public static void main(String[] args){
		
		SoloTest soloTest = new SoloTest(args);
//		soloTest.installAUT = false;
//		soloTest.installMessenger = false;
//		soloTest.installRunner = false;
		//You can turn on the debug log to see the 'debug message' from protocol or runner
//		soloTest.setProtocolDebug(true);
//		soloTest.setRunnerDebug(true);
//		soloTest.setRebuildRunner(true);
		
		soloTest.process();
		System.exit(0);
	}

}
