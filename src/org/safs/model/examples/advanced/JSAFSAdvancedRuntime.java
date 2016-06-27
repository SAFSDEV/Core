package org.safs.model.examples.advanced;

import javax.swing.JOptionPane;

import org.safs.JavaHook;
import org.safs.Log;
import org.safs.STAFHelper;
import org.safs.TestRecordHelper;
import org.safs.logging.AbstractLogFacility;
import org.safs.model.commands.DDDriverCommands;
import org.safs.model.commands.GenericMasterFunctions;
import org.safs.text.FAILStrings;
import org.safs.text.GENStrings;
import org.safs.tools.drivers.AbstractDriver;
import org.safs.tools.drivers.ConfigureInterface;
import org.safs.tools.drivers.DefaultDriver;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.InputProcessor;
import org.safs.tools.engines.EngineInterface;
import org.safs.tools.engines.SAFSROBOTJ;
import org.safs.tools.engines.TIDComponent;
import org.safs.tools.engines.TIDDriverCommands;
import org.safs.tools.engines.SAFSDRIVERCOMMANDS;
import org.safs.tools.input.MapsInterface;
import org.safs.tools.input.UniqueStringItemInfo;
import org.safs.tools.logs.LogsInterface;
import org.safs.tools.vars.VarsInterface;
import org.safs.tools.status.StatusCounter;
import org.safs.tools.status.StatusInterface;

/**
 * JSAFSAdvancedRuntime is a comprehensive sample implementation of a 
 * <a href="http://safsdev.sourceforge.net/sqabasic2000/UsingJSAFS.htm#advancedruntime" alt="Using Driver Doc" target="_blank">Driver Advanced Runtime</a>. 
 * Execution expects {@link SAFSROBOTJ} (IBM Rational Functional Tester) be available for demonstrating 
 * certain command execution results.  However, the sourcecode is a valuable usage reference 
 * even if the SAFSROBOTJ engine is not available at runtime.
 * <p>
 * This class is intended to be executed as a standalone Java application--typically, 
 * in its own JVM.  The {@link #main(String[])} entry point will create a new instance and immediately invoke {@link DefaultDriver#run()}.
 * <p>
 * Command-line Options and Configuration File Options are linked below. &nbsp;Custom sections 
 * and items in associated configuration file(s) can readily be processed at runtime via 
 * {@link AbstractDriver#configInfo} {@link ConfigureInterface}.
 * <p>
 * The default name of configuration files is "SAFSTID.INI".  There is a hierarchy of
 * configuration files that will be sought based on command-line parameters provided.  
 * This hierarchy is summarized in the Configuration File Options doc linked below and 
 * detailed below::
 * <p>
 * <ol>
 * <li><b>command-line specified PROJECT config file:</b> &nbsp;-Dsafs.project.config
 * <p>
 * a project or test-specific config file, other than SAFSTID.INI, containing config 
 * information intended to override settings in the SAFSTID.INI file located in the 
 * PROJECT ROOT directory.
 * <p>
 * <li><b>default PROJECT config file</b>
 * <p>
 * SAFSTID.INI located in the PROJECT ROOT directory.  The PROJECT ROOT would normally 
 * be provided as a command-line parameter.  It may instead be provided in the previous 
 * specified PROJECT config file.  This file will normally contain settings specific 
 * to an entire project and these override any similar settings in the DRIVER config 
 * files.
 * <p>
 * <li><b>command-line specified DRIVER config file</b>
 * <p>
 * Rarely used. A config file, other than SAFSTID.INI, intended to override settings 
 * in any SAFSTID.INI file located in the DRIVER ROOT directory.
 * <p>
 * <li><b>default DRIVER config file</b>
 * <p>
 * SAFSTID.INI located in the DRIVER ROOT directory.  The DRIVER ROOT would normally 
 * be provided in a previous config file or as a command-line parameter.  This config 
 * file will contain configuration information that is specific to 
 * the driver and independent of any project or test being executed.
 * </ol>
 * <p>
 * In general, you want to provide the bare minimum of command-line parameters and 
 * place all remaining info in one or more configuration files.  The total of all 
 * command-line parameters and config file information must enable the driver to 
 * locate valid driver and project root directories, project subdirectories, and all 
 * other items necessary to run a specified test.  See the DefaultDriver.run() link 
 * below for all the exciting things the driver will do prior to launching the test!
 * <p>
 * An example invocation, providing the bare minimum command-line parameters:
 * <p>
 * <ul>
 *     java&nbsp;-Dsafs.project.config=c:\SAFS\Project\JSAFSExample.INI&nbsp;org.safs.model.examples.advanced.JSAFSAdvancedRuntime
 * </ul>
 * The above invocation expects JSAFSExample.INI to contain any information expected by the Driver test,  
 * and where the PROJECT ROOT and maybe the DRIVER ROOT directories are located. 
 * &nbsp;Any remaining configuration information--such as which SAFS Engines to launch--can reside in 
 * either the JSAFSExample.INI file or default SAFSTID.INI files located in the 
 * PROJECT ROOT or DRIVER ROOT directories.
 * <p>
 * Sample JSAFSExample.INI in c:\SAFS\Project specific to one test:
 * <p>
 * <ul><pre>
 * [SAFS_PROJECT]
 * ProjectRoot="C:\safs\project"
 * 
 * [SAFS_TEST]
 * CycleLogName="JSAFSExample"
 * CycleLogMode="41"
 *      (or)
 * CycleLogMode="TOOLLOG CONSOLELOG TEXTLOG"
 * 
 * [SAFS_ENGINES]
 * First=org.safs.tools.engines.SAFSROBOTJ
 * 
 * [SAFS_ROBOTJ]
 * AUTOLAUNCH=TRUE
 * DATASTORE="C:\SAFS\DatastoreJ"
 * TESTDOMAINS=HTML,JAVA
 * </pre>
 * </ul>
 * <p>
 * Sample SAFSTID.INI in c:\SAFS\Project used by all tests:
 * <p>
 * <ul><pre>
 * [SAFS_DRIVER]
 * DriverRoot="C:\safs"
 * 
 * [SAFS_MAPS]
 * AUTOLAUNCH=TRUE
 * 
 * [SAFS_INPUT]
 * AUTOLAUNCH=TRUE
 * 
 * [SAFS_VARS]
 * AUTOLAUNCH=TRUE
 * 
 * [SAFS_LOGS]
 * AUTOLAUNCH=TRUE
 * </pre>
 * </ul>
 * <p>
 * And that is enough for us to run the Driver test.
 * <p>
 * Of course, more of the configuration parameters necessary for desired engines 
 * will have to be in those configuration files once the engines actually become 
 * available.
 * <p>
 * @see DefaultDriver#run()
 * @see <A HREF="http://safsdev.sf.net/sqabasic2000/JSAFSFrameworkContent.htm#driveroptions">Command-Line Options</A>
 * @see <A HREF="http://safsdev.sf.net/sqabasic2000/JSAFSFrameworkContent.htm#configfile">Configuration File Options</A>
 */
public class JSAFSAdvancedRuntime extends DefaultDriver {

	/** "JSAFSRuntime" custom Driver Name. **/
	public static final String JSAFSRUNTIME = "JSAFSRuntime";
	
	/** Optional, command-line args exposed more globally. **/
	public static String[] _args;	
	
	/**
	 * Default constructor using "JSAFSRuntime" Driver name. 
	 * The constructor does nothing more than initialize internals and superclasses.
	 * Nothing else happens until {@link DefaultDriver#run()} is invoked.
	 */
	public JSAFSAdvancedRuntime (){
		super();
		driverName = JSAFSRUNTIME;
		org.safs.Log.setLogProcessName(this.driverName);
	}
	
	/** 
	 * Empty routine overrides superclass {@link DefaultDriver#validateTestParameters()}
	 * No initialization validation required for this sample advanced runtime.
	 * Superclass MUST be overridden or invalid/missing test table information 
	 * will cause the test initialization to fail and abort the test.
	 **/
	protected void validateTestParameters(){
		//test table data validation not required
	}
	
	/**
	 * Convienience (re)initializer for a TestRecordHelper.
	 * <p>
	 * Instantiates and\or reinits a {@link TestRecordHelper} and initializes key properties:
	 * <p><ul>
	 * <li>setFac=cycleLog ID (required)
	 * <li>setFileID="Driver" (arbitrary ID since we are not processing a test table)
	 * <li>setFilename="Advanced Runtime" (arbitrary since we are not processing a test table)
	 * <li>setTestLevel="STEP"
	 * <li>setSeparator="," (comma separator for records)
	 * <li>setStatusCode={@link DriverConstant#STATUS_SCRIPT_NOT_EXECUTED}
	 * <li>setStatusInfo="" (empty)
	 * </ul>
	 * @param store - A TestRecordHelper to reinitialize or null to get a new one initialized.
	 * @return TestRecordHelper
	 * @see TestRecordHelper#reinit();
	 */
	protected TestRecordHelper initTestRecordData(TestRecordHelper store){
		TestRecordHelper trd = (store == null) ? new TestRecordHelper(): store;
		if (store!=null){trd.reinit();}
		trd.setFac(cycleLog.getStringID());
		trd.setFileID("Driver"); // can probably be anything
		trd.setFilename("Advanced Runtime"); // can probably be anything
		trd.setTestLevel("STEP");// CYCLE, SUITE, STEP, maybe even nothing!		
		trd.setSeparator(",");
		trd.setStatusCode(DriverConstant.STATUS_SCRIPT_NOT_EXECUTED);
		trd.setStatusInfo("");
		return trd;
	}
	
	
	/**
	 * Convenience routine to get a value out of an app map.
	 * 
	 * (We probably should "fix" the MapsInterface and the implementations 
	 * to eliminate the need for this convenience routine!)
	 *  
	 * @param mapname Name\ID of App Map to use.  If null, use default map.
	 * @param section Named Section of App Map to search.  If null, use default section.
	 * @param item Name of item in section to lookup.
	 * @return String or null
	 */
	protected String getAppMapItem(String mapname, String section, String item){
		if (mapname==null) mapname = (String)maps.getDefaultMap().getUniqueID();
		if (section==null) section = maps.getDefaultMapSection().getSectionName();
		return maps.getMapItem(new UniqueStringItemInfo(mapname, section, item));
	}
	
	
	/**********************************************************************************
	 * This is where the Driver developer implements the application-specific test!
	 * <p>
	 * Here you can invoke any test setup that might have to occur after standard SAFS 
	 * initialization. &nbsp;That would be followed by the actual test code including:
	 * <p>
	 * <ul>
	 * <li>readily access INI file entries via {@link AbstractDriver#configInfo} {@link ConfigureInterface}
	 * <li>increment pass/fail counters via a {@link StatusCounter}
	 * <li>log test messages via {@link AbstractDriver#logs} {@link LogsInterface}
	 * <li>use localizable message resource bundles with {@link GENStrings} and {@link FAILStrings}.<br/>
	 *     <ul>
	 *     "SAFSTextResourceBundles" and "failedSAFSTextResourceBundles" are in SAFS.JAR for review.
	 *     </ul> 
	 * <li>log debug messages to org.safs.{@link Log}
	 * <li>get/set shared SAFS variables and NLS values via {@link AbstractDriver#vars} {@link VarsInterface}
	 * <li>retrieve AppMap values including NLS values via {@link AbstractDriver#maps} {@link MapsInterface}
	 * <li>process SAFS expressions for dynamic values via {@link AbstractDriver#maps} {@link MapsInterface}
	 * <li>provide test flow logic
	 * <li>invoke SAFS test table(s) via an {@link InputProcessor} 
	 * <li>issue SAFS Driver Command, Component Function, and EngineCommand records directly via {@link EngineInterface}
	 * <p><ul>
	 *    <li>INI-specified SAFS_ENGINES like "SAFSROBOTJ" in {@link DefaultDriver#engineObjects} via {@link DefaultDriver#getPreferredEngine(String)}
	 *    <li>Internal SAFS TID and SDC ENGINES--including <a href="" alt="SAFS Image-Based Testing Doc" target="_blank">Image-Based Testing</a> support: 
	 *        <ul>
	 *        <li>TID {@link TIDDriverCommands} via {@link AbstractDriver#getTIDDriverCommands()}
	 *        <li>SDC {@link SAFSDRIVERCOMMANDS} via {@link AbstractDriver#getIPDriverCommands()}
	 *        <li>TID/IBT {@link TIDComponent} via {@link AbstractDriver#getTIDGUIlessComponentSupport()}
	 *        </ul>
	 *    </ul>
	 * <li>use STAF directly, if desired, via {@link STAFHelper}
	 * </ul>
	 * <p>
	 * When this function returns, standard SAFS test shutdown will commence including 
	 * the closing of all logs, engines, and STAF--as appropriate.
	 * <p>
	 * @see AbstractDriver#processTest()
	 */
	protected StatusInterface processTest(){

		// Write additional debug information to the SAFS Debug Log.
		// This will be ignored gracefully if the Debug Log is not running.
		Log.debug("Driver Advance Runtime now executing...");

		// cast our default StatusInterface to its real implementation class for convenience
		StatusCounter statuscounter = statuscounts instanceof StatusCounter ? (StatusCounter)statuscounts: new StatusCounter();

		// a GENERIC_MESSAGE doesn't print any prefix in the logged message
		logMessage("Starting Driver Example Testing", 
				   null, 
				   AbstractLogFacility.GENERIC_MESSAGE);				
		// increment TOTAL RECORDS, but not TEST RECORDS
		statuscounter.incrementGeneralPasses();
		
		// a FAILED MESSAGE prints a **FAILED** prefix in the logged message
		logMessage("Intentionally recording a failure...", 
				   "Testing out different message types and counting", 
				   AbstractLogFacility.FAILED_MESSAGE);				
		// increment TOTAL RECORDS and GENERAL FAILURES, but not TEST RECORDS or TEST FAILURES
		statuscounter.incrementGeneralFailures();

		// retrieve a value from our configuration files.  
		// The value will be sought in all the INI files 
		// that have been loaded and "chained" together.
		// In this case, this example should find the value 
		// in the chained INI file at C:\SAFS\SAFSTID.INI 
		String value = configInfo.getNamedValue("SAFS_INPUT", "AUTOLAUNCH");		
		if(value != null){
			// increment TOTAL RECORDS, TEST RECORDS, and TEST PASSES
			statuscounter.incrementTestPasses();
			// a PASSED MESSAGE will print the OK prefix in the logged message
			logMessage("SAFSINPUT AUTOLAUNCH set to: "+ value, 
					   null, 
					   AbstractLogFacility.PASSED_MESSAGE);
		}else{
			// increment TOTAL RECORDS, TEST RECORDS, and TEST FAILURES
			statuscounter.incrementTestFailures();
			logMessage("Failed to find AUTOLAUNCH for SAFSINPUT service.", 
					   "Configuration files may not contain SAFS_INPUT info.", 
					   AbstractLogFacility.FAILED_MESSAGE);
		}

		Log.debug("Driver Advance Runtime checking SAFS_DRIVER_CONTROL...");
		
		//you can get and set SAFS global variables in SAFSVARS:
		vars.setValue(DRIVER_CONTROL_VAR, JavaHook.PAUSE_EXECUTION);
		logMessage("The test has been temporarily suspended...", 
				   "SAFSVARS 'SAFS_DRIVER_CONTROL' variable should = PAUSE",AbstractLogFacility.GENERIC_MESSAGE);

		//sample of using your own Java code to do something non-SAFS related:
		JOptionPane.showMessageDialog(null, 
				"We have PAUSED the test.\n"+
				"Please use the SAFS Monitor dialog \n"+
				"to RUN the test further.", 
				"Driver Message", JOptionPane.INFORMATION_MESSAGE);
		
		// you can optionally check if the user has PAUSED the test thru SAFS Monitor Dialog
		int timeout = 0;
		while(timeout++ < 10){ //we choose to only wait for up to 10 seconds
			value = vars.getValue(DRIVER_CONTROL_VAR);
			if(value.equalsIgnoreCase(JavaHook.RUNNING_EXECUTION)) break;
			try{Thread.sleep(1000);}catch(Exception x){}
		}

		//you can get and set SAFS global variables in SAFSVARS:
		vars.setValue(DRIVER_CONTROL_VAR, JavaHook.RUNNING_EXECUTION);
		logMessage("The test has has now resumed...", 
				   "SAFSVARS 'SAFS_DRIVER_CONTROL' variable is: "+ value,AbstractLogFacility.GENERIC_MESSAGE);
		
		Log.debug("Driver Advance Runtime now trying to send a record to the SAFS/RFT engine.");

		// these next two items should be reusable objects and one-time calls
		EngineInterface rft = getPreferredEngine(SAFSROBOTJ.class.getName());
		TestRecordHelper trd = initTestRecordData(null);// null first time only for instantiation, then reuse
		
		// prepare an SAFS Record invocation -- this is one way to directly invoke arbitrary commands
		// rather than in-line, these calls would probably be in separate functions or libraries 
		// in order to handle the differing log messages, etc...
		trd.setInputRecord("C,ClearAppMapCache");//separator already set to comma (,)
		
		//execute the record in SAFSROBOTJ engine
		long rc = rft.processRecord(trd);
		
		Log.debug("Driver SAFS/RFT call returned statuscode: "+ rc);

		// if we want, we can route the same command to other engines.
		// if one engine fails, we could try others
		// but here we just log appropriate PASS/FAIL information and increment counters
		// again, the prep, call, and logging would likely be a single callable function.
		if(rc == DriverConstant.STATUS_SCRIPT_NOT_EXECUTED){
			statuscounter.incrementTestFailures();
			logMessage("SAFS/RFT failed to recognize the record or command. Result: "+ rc, 
					   "EngineInterface returned SCRIPT NOT EXECUTED", 
					   AbstractLogFacility.FAILED_MESSAGE);
			
		}// handle the normal OK or PASSED status here
		else if(rc == DriverConstant.STATUS_NO_SCRIPT_FAILURE){
			//rft already logged success with the command!
			statuscounter.incrementTestPasses();
			
		}// handle everything else here 
		else{
			//rft should have logged any failure already
			statuscounter.incrementTestFailures();
		}

		// preparing for use of other internally available engines...
		EngineInterface tid = this.getTIDGUIlessComponentSupport();
		EngineInterface tiddc = this.getTIDDriverCommands();

		// let's try to use some component commands and application constants!
		// these almost always require an App Map to be loaded.
		// we could do this directly via the maps interface, but it wouldn't 
		// be automatically logged.  So we use SDC command instead.

		// reinit for a new SAFS command
		// again, this prep, call, and logging would likely be a separate callable function.
		trd = initTestRecordData(trd);
		trd.setAppMapName("TIDTest.map");

		// this time we use the org.safs.model to give us a properly formatted test 
		// record using our predefined comma separator.  The IDE gives developers 
		// Help with available method info and parameter expectations.
		trd.setInputRecord(DDDriverCommands.setApplicationMap("TIDTest.map").exportTestRecord(trd.getSeparator()));		
		rc = tiddc.processRecord(trd);
		
		if(rc == DriverConstant.STATUS_NO_SCRIPT_FAILURE){
			//engine already logged success with the command!
			statuscounter.incrementGeneralPasses();
			
		}// handle everything else here 
		else{
			//engine should have logged any failure already
			// we might also consider aborting the whole test here!
			statuscounter.incrementGeneralFailures();
		}		

		Log.debug("Driver to get item(s) from the App Map...");
				
		value = getAppMapItem(null, "Section1", "EmbedVar");
		
		Log.debug("Driver received EmbedVar value: "+ value);
		logMessage("Driver received EmbedVar value: "+ value, null, AbstractLogFacility.PASSED_MESSAGE);
		statuscounter.incrementTestPasses();

		vars.setValue("constant", "actually a safsvars variable");
		value = getAppMapItem(null, null, "hiddenconst");
		
		Log.debug("Driver received hiddenconst value: "+ value);
		logMessage("Driver received hiddenconst value: "+ value, null, AbstractLogFacility.PASSED_MESSAGE);
		statuscounter.incrementTestPasses();

		// now we try using some SAFS commands that use App Map Values
		
		trd = initTestRecordData(trd);		
		trd.setWindowName("AnyWindow");  // not necessary, but useful
		trd.setCompName("AnyComponent"); // not necessary, but useful
		try{ 
			trd.setInputRecord(GenericMasterFunctions.typeKeys(trd.getWindowName(),	trd.getCompName(), "{ESC}"
				               ).exportTestRecord(trd.getSeparator()));
		}catch(Exception x){
			//log unlikely error detection here
		}
		
		// note we can log that subsequent failure(s) are OK
		logMessage("The following failure is expected because RFT does NOT support the command.", 
				   null, AbstractLogFacility.FAILED_OK_MESSAGE);
		
		Log.debug("Driver SAFS/RFT will return FAILURE ("+ DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE +") or SCRIPT NOT EXECUTED ("+ DriverConstant.STATUS_SCRIPT_NOT_EXECUTED +" for 'TypeKeys' command.");

		// we can see that RFT will not support TypeKeys command by giving it a try
		rc = rft.processRecord(trd);

		// if one engine (rft) can't do it, then let's try another
		if(!(rc==DriverConstant.STATUS_NO_SCRIPT_FAILURE)){
			trd.setStatusInfo(""); //just to be sure...statusCode is already correct for a new call
			Log.debug("Driver SAFS/RFT 'TypeKeys' call returned statuscode: "+ rc);
			
			//trying the correct TID engine this time...
			rc = tid.processRecord(trd);
			Log.debug("Driver SAFS TID 'TypeKeys' call returned statuscode: "+ rc);
			
		}else{
			Log.debug("Driver SAFS/RFT 'TypeKeys' call returned statuscode: "+ rc);
		}
		// now show the results of trying multiple engines
		// this time using "standard" localizable text resource bundles
		String amessage = null;
		String adetail = null;
		
		if(rc == DriverConstant.STATUS_SCRIPT_NOT_EXECUTED){
			statuscounter.incrementTestFailures();
			amessage = FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
					   "Support for TypeKeys was not found!", "TypeKeys");
			adetail = FAILStrings.convert("unknown_record", 
					   "Unknown RECORD TYPE or COMMAND in Driver Advanced Runtime. "+ trd.getInputRecord(), 
					   trd.getFileID(), 
					   trd.getFilename());
			logMessage(amessage, adetail, AbstractLogFacility.FAILED_MESSAGE);
			
		}// handle the normal OK or PASSED status here
		else if(rc == DriverConstant.STATUS_NO_SCRIPT_FAILURE){
			// messages already logged by engine
			statuscounter.incrementTestPasses();
		}
		// handle everything else here 
		else{
			//failures already logged???
			statuscounter.incrementTestFailures();
		}
		
		// execute the record in the TID engine (also supports Image-Based Testing! :)
		
		Log.debug("Driver SAFS/RFT call returned statuscode: "+ rc);
		
//		UniqueStringFileInfo sourceInfo = new UniqueStringFileInfo(
//		                                      testName,
//											  testName, 
//											  getTestLevelSeparator(testLevel),
//											  testLevel);
//
//		UniqueStringID testid = new UniqueStringID(getLogID(testLevel));
//		
//		InputProcessor driver = new InputProcessor(this, sourceInfo, testid);
//		
//		StatusInterface statusinfo = driver.processTest();
//		
//		return statusinfo;
		
		Log.debug("Driver Advanced Runtime is logging status and exiting...");
		logs.logStatusInfo(cycleLog, statuscounter, "JSAFSExample");
		return statuscounter;
	}

	/**
	 * Entry point for standalone Java execution.
	 * <p>
	 * Instances a new driver object and immediately executes run() for standard SAFS initialization. 
	 * &nbsp;The developer overrides {@link #processTest()} to proceed with any additional non-SAFS test setup, 
	 * execution, and non-SAFS teardown.
	 * <p>
	 * SAFS shutdown including engine and log closures will happen automatically after {@link #processTest()} 
	 * returns to the {@link DefaultDriver#run()} function that invoked it.
	 * <p>
	 * @see org.safs.tools.drivers.DefaultDriver#run()
	 * @see org.safs.tools.drivers.AbstractDriver#processTest()
	 * @see #processTest()
	 * @see <A HREF="http://safsdev.sf.net/sqabasic2000/JSAFSFrameworkContent.htm#driveroptions">Command-Line Options</A> 
	 * @see <A HREF="http://safsdev.sf.net/sqabasic2000/JSAFSFrameworkContent.htm#configfile">Configuration File Options</A> 
	 */
	public static void main(String[] args) {

		_args = args.clone(); // optionally make args available more globally
		JSAFSAdvancedRuntime driver = new JSAFSAdvancedRuntime();
		driver.run();
		System.runFinalization();
		System.exit(0);
	}
}

