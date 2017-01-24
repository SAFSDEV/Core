/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * Developer history:
 * 
 * AUG 23, 2010	(LeiWang)	Modify method cmdWaitForGUI(): assign the testRecord's windowGuiId to winrec.
 *                                  If we use Mixed-Mode-RS (OBT-RS for window, IBT-RS for component), we will set
 *                                  the windowGuiId to an IBT-RS in the RJ engine side. In this method, if we still
 *                                  take RS from map, the window's RS will be still OBT-RS, then the TestRecord 
 *                                  can NOT be processed here;
 *                                  Add a constructor with parameter of type LogUtilities, and set LogUtilities for
 *                                  itself and its inner TID engines like Log, Flow, Counter engines.
 * SEP 30, 2010	(LeiWang)   Implement keyword UseSAFSFunctions and UseSeleniumFunctions.
 * SEP 17, 2014  CANAGL     Fixing SAFS Crashes due to incomplete initialization.
 * JUN 09, 2015  DHARMESH4  Added result email support.
 * SEP 24, 2015  LeiWang	Modify sendEMail(): get more parameters from configuration file to initialize Mailer.
 * MAY 05, 2016 (LeiWang) 	Fix the disorder problem of attachment.
 * MAY 18, 2016 (LeiWang) 	Implement the keyword 'CallJUnit'.
 * MAY 19, 2016 (CANAGL) 	Enhance CallJUnit output and increment SAFS StatusCounters with JUnit results.
 */
package org.safs.tools.engines;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Message;
import javax.mail.internet.ParseException;

import org.safs.IndependantLog;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSNullPointerException;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.TestRecordHelper;
import org.safs.image.ImageUtils;
import org.safs.logging.AbstractLogFacility;
import org.safs.logging.LogUtilities;
import org.safs.staf.STAFProcessHelpers;
import org.safs.text.FAILStrings;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.ConfigureInterface;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverConstant.MailConstant;
import org.safs.tools.drivers.DriverInterface;
import org.safs.tools.input.MapsInterface;
import org.safs.tools.input.UniqueStringMapInfo;
import org.safs.tools.mail.Mailer;
import org.safs.tools.mail.Mailer.MimeContent;
import org.safs.tools.mail.Mailer.MimeType;
import org.safs.tools.mail.Mailer.Protocol;
import org.safs.tools.stringutils.StringUtilities;

/**
 * Provides local in-process support for Driver Commands.Data
 * <p>
 * These are Driver Commands generally needed exclusively by the Driver to perform
 * Driver-specific functions, or to configure the various engines.  This is just a
 * subset of all Driver Commands generally available.
 * <p>
 * This DriverCommands engine does not assume the use of STAF. Instead, it uses the
 * various org.safs.tools Interfaces to talk with the rest of the framework (as made
 * available via the DriverInterface configuration).
 *
 */
public class TIDDriverCommands extends GenericEngine {

	/** "SAFS/TIDDriverCommands" */
	static final String ENGINE_NAME  = "SAFS/TIDDriverCommands";

	// Below engine names should contain the SimpleClass (short) name of each engine
	// That is, org.safs.tools.engines.SAFSROBOTOJ should be all upper-case "SAFSROBOTJ"
	
	/** "SAFSROBOTJ" */
	static final String ROBOTJ_ENGINE  = "SAFSROBOTJ";
	/** "SAFSDRIVERCOMMANDS" */
	static final String SDC_ENGINE  = "SAFSDRIVERCOMMANDS";
	/** "SAFSIOS" */
	static final String IOS_ENGINE  = "SAFSIOS";
	/** "SAFSQTP" */
	static final String QTP_ENGINE  = "SAFSQTP";
	/** "SAFSABBOT" */
	static final String ABBOT_ENGINE  = "SAFSABBOT";
	/** "SAFSTC" */
	static final String TCAFS_ENGINE  = "SAFSTC";
	/** "SAFSSELENIUM" */
	static final String SELENIUM_ENGINE  = "SAFSSELENIUM";
	/** "SAFSDROID" */
	static final String DROID_ENGINE  = "SAFSDROID";
	
	// START: LOCALLY SUPPORTED DRIVER COMMANDS
	
	/** "SetApplicationMap" */
	static final String COMMAND_SETAPPLICATIONMAP = "SetApplicationMap";

	/** "SetVariableValues" */
	static final String COMMAND_SETVARIABLEVALUES = "SetVariableValues";

	/** "CloseApplicationMap" */
	static final String COMMAND_CLOSEAPPLICATIONMAP = "CloseApplicationMap";

	/** "Expressions" */
	static final String COMMAND_EXPRESSIONS       = "Expressions";

	/** "Version" */
	static final String COMMAND_VERSION           = "Version";

	/** "UseRobotJFunctions" */
	static final String COMMAND_USEROBOTJFUNCTIONS= "UseRobotJFunctions";
	
	/** UseSAFSFunctions */
	static final String COMMAND_USESAFSFUNCTIONS= "UseSAFSFunctions";
	
	/** UseTestCompleteFunctions */
	static final String COMMAND_USETESTCOMPLETEFUNCTIONS= "UseTestCompleteFunctions";
	
	/** UseIOSFunctions */
	static final String COMMAND_USEIOSFUNCTIONS= "UseIOSFunctions";
	
	/** UseQTPFunctions */
	static final String COMMAND_USEQTPFUNCTIONS= "UseQTPFunctions";
	
	/** UseABBOTFunctions */
	static final String COMMAND_USEABBOTFUNCTIONS= "UseABBOTFunctions";
	
	/** UseSeleniumFunctions */
	static final String COMMAND_USESELENIUMFUNCTIONS= "UseSeleniumFunctions";

	/** UseDroidFunctions */
	static final String COMMAND_USEDROIDFUNCTIONS= "UseDroidFunctions";

	/** TakeScreenShot */
	static final String COMMAND_TAKESCREENSHOT = "TakeScreenShot";
	
	/** SendEmail */
	static final String COMMAND_SENDEMAIL = "SendEmail";
	
	/** "SetBenchDirectory" */
	public static final String COMMAND_SETBENCHDIRECTORY             = "SetBenchDirectory";
	/** "SetDifDirectory" */
	public static final String COMMAND_SETDIFDIRECTORY               = "SetDifDirectory";
	/** "SetProjectDirectory" */
	public static final String COMMAND_SETPROJECTDIRECTORY           = "SetProjectDirectory";
	/** "SetTestDirectory" */
	public static final String COMMAND_SETTESTDIRECTORY              = "SetTestDirectory";
	/** "SetRootVerifyDirectory" */
	public static final String COMMAND_SETROOTVERIFYDIRECTORY        = "SetRootVerifyDirectory";
	/** "WaitForGUI" */
	public static final String COMMAND_WAITFORGUI                    = "WaitForGUI";
	/** "WaitForGUIGone" */
	public static final String COMMAND_WAITFORGUIGONE                = "WaitForGUIGone";
	/** "SetImageDebug" */
	public static final String COMMAND_SETIMAGEDEBUG                 = "SetImageDebug";
	/** "SetImageFuzzyMatching" */
	public static final String COMMAND_SETIMAGEFUZZYMATCHING         = "SetImageFuzzyMatching";
	
	public static final String SET_MILLIS_BETWEEN_RECORDS	 		= "SetMillisBetweenRecords";//AbstractDriver.millisBetweenRecords
	public static final String GET_MILLIS_BETWEEN_RECORDS	 		= "GetMillisBetweenRecords";//AbstractDriver.millisBetweenRecords
	public static final String SETMULTIPLETHREADSEARCH       		= "SetMultipleThreadSearch";
	
	// END: LOCALLY SUPPORTED DRIVER COMMANDS

	TIDDriverLogCommands  dcLog  = new TIDDriverLogCommands();
	TIDDriverFlowCommands dcFlow = new TIDDriverFlowCommands();
	TIDDriverCounterCommands dcCounters = new TIDDriverCounterCommands();
	TIDDriverRestCommands dcRest = new TIDDriverRestCommands();

	String command = "";
	String message;
	String detail;
	
	/**
	 * Constructor for TIDDriverCommands
	 */
	public TIDDriverCommands() {
		super();
		servicename = ENGINE_NAME;
	}
	/**
	 * Instantiate a new TIDDriverCommands,
	 * Call setLogUtilities() of its super class for itself and its inner TID engines
	 * like Log, Flow, Counter TIDEngines.<br>
	 * 
	 * Normally, we should instantiate an object of this class with a driver. But if TestRecord
	 * has Mixed-Mode-RS, we have to instantiate an object of this class from a Processor (ex. DCDriverCommand),
	 * to keep working the log functionality, we call this constructor. For other cases, we should NOT
	 * call this constructor.
	 * 
	 * @param log
	 */
	public TIDDriverCommands(LogUtilities log){
		this();
		setLogUtilities(log);
	}
	
	/**
	 * Calls super.setLogUtilities for this class and then setLogUtilities for each 
	 * chained processor in this class (dcLog, dcFlow, dcCounters, dcRest).
	 */
	@Override
	public void setLogUtilities(LogUtilities log){
		super.setLogUtilities(log);
		dcLog.setLogUtilities(log);
		dcFlow.setLogUtilities(log);
		dcCounters.setLogUtilities(log);
		dcRest.setLogUtilities(log);
	}
	
	/**
	 * Calls super.setLogUtilities for this class and then setLogUtilities for each 
	 * chained processor in this class (dcLog, dcFlow, dcCounters, dcRest).
     * @param log
     * @param copyLogClass set to true to copy everything to the debug log (Log.class)
	 */
	@Override
	public void setLogUtilities(LogUtilities log, boolean copyLogClass){
		super.setLogUtilities(log, copyLogClass);
		dcLog.setLogUtilities(log, copyLogClass);
		dcFlow.setLogUtilities(log, copyLogClass);
		dcCounters.setLogUtilities(log, copyLogClass);
		dcRest.setLogUtilities(log, copyLogClass);
	}
	
	/**
	 * PREFERRED Constructor for TIDDriverCommands.
	 * Constructs the instance and calls launchInterface to initialize.
	 */
	public TIDDriverCommands(DriverInterface driver) {
		this();
		launchInterface(driver);
	}

	/**
	 * @see GenericEngine#launchInterface(Object)
	 */
	public void launchInterface(Object configInfo){
		// not sure why we don't super.launchInterface(configInfo) here?
		super.launchInterface(configInfo);
		try{
			driver = (DriverInterface) configInfo;
			if (processName == null) processName = driver.getDriverName();
			dcLog.launchInterface(driver);
			dcFlow.launchInterface(driver);
			dcCounters.launchInterface(driver);
			dcRest.launchInterface(driver);
			if(log == null) setLogUtilities(new LogUtilities(this.staf), false);
			
		}catch(Exception x){
			Log.error(
			"TIDDriverCommands requires a valid DriverInterface object for initialization!\n"+
			x.getMessage());
		}
	}

	/**
	 * Process the record present in the provided testRecordData.
	 * Route to other TIDDriverXXXCommands classes as necessary.
	 */
	public long processRecord (TestRecordHelper testRecordData){

		this.testRecordData = testRecordData;
		long rc = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
		if(staf==null) {
			staf = testRecordData.getSTAFHelper();
			if(staf==null) {
				Log.debug("TIDDC attempting to retrieve STAFHelper...");
				try{getSTAFHelper();}
				catch(Exception x){
					Log.debug("TIDDC could not retrieve STAFHelper due to "+ x.getClass().getName()+", "+x.getMessage());
				}
				if(staf==null) {
					Log.debug("TIDDC attempting to acquire registered STAFProcessHelper...");
					try{ 
						staf = STAFProcessHelpers.registerHelper(ENGINE_NAME);
					}
					catch(Exception x){
						Log.debug("TIDDC WARNING: unable to acquire STAFHelper. Ignored.");
					}
				}
			}
		}
		this.testRecordData.setSTAFHelper(this.staf);

		command = testRecordData.getCommand();
		if(command==null || command.length()==0){
			try{
				command = testRecordData.getInputRecordToken(1);
				testRecordData.setCommand(command);
			}catch(Exception x){
				Log.debug("TIDDC WARNING: unable to acquire COMMAND");
				standardErrorMessage(testRecordData, 
						"unknown command:"+ x.getClass().getSimpleName()+":"+ x.getMessage(), 
						testRecordData.getInputRecord());
				return DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE;
			}
		}
		Log.info("TIDDC:processing \""+ command +"\".");
		try{
		    if (command.equalsIgnoreCase(COMMAND_SETAPPLICATIONMAP)) 
		        {return cmdSetApplicationMap();}
		    else if (command.equalsIgnoreCase(COMMAND_SETVARIABLEVALUES)) 
		    	{return cmdSetVariableValues();}
		    else if (command.equalsIgnoreCase(COMMAND_CLOSEAPPLICATIONMAP)) 
	        	{return cmdCloseApplicationMap();}
			else if (command.equalsIgnoreCase(COMMAND_EXPRESSIONS)) 
				{return cmdExpressions();}
			else if (command.equalsIgnoreCase(COMMAND_WAITFORGUI)) 
				{return cmdWaitForGUI(true);}
			else if (command.equalsIgnoreCase(COMMAND_WAITFORGUIGONE)) 
				{return cmdWaitForGUI(false);}
			else if (command.equalsIgnoreCase(COMMAND_VERSION)) 
				{return cmdVersion();}
			else if (command.equalsIgnoreCase(COMMAND_USEROBOTJFUNCTIONS) ||
					 command.equalsIgnoreCase(COMMAND_USESAFSFUNCTIONS) ||
					 command.equalsIgnoreCase(COMMAND_USEIOSFUNCTIONS) ||
					 command.equalsIgnoreCase(COMMAND_USEQTPFUNCTIONS) ||
					 command.equalsIgnoreCase(COMMAND_USEABBOTFUNCTIONS) ||
					 command.equalsIgnoreCase(COMMAND_USETESTCOMPLETEFUNCTIONS) ||
					 command.equalsIgnoreCase(COMMAND_USESELENIUMFUNCTIONS) ||
					 command.equalsIgnoreCase(COMMAND_USEDROIDFUNCTIONS)) 
				{return cmdUseFunctions();}
			else if (command.equalsIgnoreCase(COMMAND_SETIMAGEDEBUG)) 
				{return cmdSetImageDebug();}
			else if (command.equalsIgnoreCase(COMMAND_SETIMAGEFUZZYMATCHING)) 
				{return cmdSetImageFuzzyMatching();}
			else if ((command.equalsIgnoreCase(COMMAND_SETPROJECTDIRECTORY))||
					 (command.equalsIgnoreCase(COMMAND_SETBENCHDIRECTORY))||
					 (command.equalsIgnoreCase(COMMAND_SETTESTDIRECTORY))||
					 (command.equalsIgnoreCase(COMMAND_SETDIFDIRECTORY))||
					 (command.equalsIgnoreCase(COMMAND_SETROOTVERIFYDIRECTORY))) 
				{return cmdSetDirectories();}
			else if ( command.equalsIgnoreCase(SET_MILLIS_BETWEEN_RECORDS)){
				return setDriverOptions();
			}else if (command.equalsIgnoreCase(GET_MILLIS_BETWEEN_RECORDS)){
				return getDriverOptions();
			}else if (command.equalsIgnoreCase(SETMULTIPLETHREADSEARCH)){
				return setMutipleThreadSearch();
			}else if (command.equalsIgnoreCase(COMMAND_TAKESCREENSHOT)){
				return takeScreenShot();
			}else if (command.equalsIgnoreCase(COMMAND_SENDEMAIL)){
				return sendEmail();
			}
		    
		    rc = dcLog.processRecord(this.testRecordData);
			if (rc == DriverConstant.STATUS_SCRIPT_NOT_EXECUTED) rc = dcFlow.processRecord(this.testRecordData);
			if (rc == DriverConstant.STATUS_SCRIPT_NOT_EXECUTED) rc = dcCounters.processRecord(this.testRecordData);
			if (rc == DriverConstant.STATUS_SCRIPT_NOT_EXECUTED) rc = dcRest.processRecord(this.testRecordData);
			return rc;	
		}
		catch(Exception x){
			Log.debug("DEBUG TRAPPED EXCEPTION:",x);
			standardErrorMessage(testRecordData, 
					command+":"+ x.getClass().getSimpleName()+":"+ x.getMessage(), 
					testRecordData.getInputRecord());
			return DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE;
		}
	}

	protected long takeScreenShot() throws SAFSException {
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		String filename = null;
		try{ filename = testRecordData.getTrimmedUnquotedInputRecordToken(2);} catch(Exception npx){ }
		filename = ImageUtils.normalizeFileNameSuffix(filename);
		File fn = deduceTestFile(filename);
		
		// get optional SubArea parameter
		String subarea = null;
		Rectangle imageRect = ImageUtils.getScreenSize();
		try{ subarea = testRecordData.getTrimmedUnquotedInputRecordToken(4);} catch(Exception npx){ }
		
		if (subarea==null || subarea.isEmpty()) {
			Log.info("SubArea not provided, use the whole screen size to take snapshot.");
		} else {
			imageRect = ImageUtils.getSubAreaRectangle(imageRect, subarea);
		}
		
		try {
			Log.debug("imageRect resolves to: " + imageRect);
			BufferedImage buffimg = ImageUtils.captureScreenArea(imageRect);
			Log.debug("captured image resolves to: " + buffimg);
			ImageUtils.saveImageToFile(buffimg, fn, 1.0F);
		} catch (java.awt.AWTException ae) {
			message = FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND,"Support for 'AWT Robot' not found.", "AWT Robot");
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
	        return setTRDStatus(testRecordData,DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		} catch (java.lang.SecurityException se) {
			// error, security problems accessing output file
			message = FAILStrings.convert(FAILStrings.CANT_CREATE_FILE,
					                      "Can not create file '"+ fn.getAbsolutePath() + "': "+ se.getClass().getSimpleName(),
					                      fn.getAbsolutePath()+ ": " + se.getClass().getSimpleName());
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData,DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		} catch (java.lang.IllegalArgumentException se) {
			// error, bad parameters sent to JAI.create call
			// error, security problems accessing output file
			message = FAILStrings.convert(FAILStrings.CANT_CREATE_FILE,
                    "Can not create file '"+ fn.getAbsolutePath() + "': "+ se.getClass().getSimpleName(),
                    fn.getAbsolutePath()+ ": " + se.getClass().getSimpleName());
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData,DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		} catch (NoClassDefFoundError ncdfe) {
			// error, JAI not installed
			message = FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND,
										  "Support for Java Advanced Imaging (JAI) not found!",
										  "Java Advanced Imaging (JAI)");
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData,DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		} catch (Exception e) {
			// error, unable to capture the screen image
			message = "Met Exception "+e.getClass().getSimpleName()+":"+e.getMessage();
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData,DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}

		// success! set status to ok
		message = GENStrings.convert(GENStrings.BE_SAVED_TO,
									 "Image has been saved to '" + fn.getAbsolutePath() + "'",
									 "Image",
									 fn.getAbsolutePath());
		simpleSuccessMessage(testRecordData, command, message);
		return setTRDStatus(testRecordData,DriverConstant.STATUS_NO_SCRIPT_FAILURE);
	}
	
	 /** <br><em>Purpose:</em> set driver's options: like AbstractDriver.millisBetweenRecords
	   **/
	  private long setDriverOptions(){
		String debugmsg = getClass().getName()+".setDriverOptions() ";  
		
		String optionValue = null;
		try{ optionValue = testRecordData.getTrimmedUnquotedInputRecordToken(2);}
		catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){
			message = FAILStrings.convert(FAILStrings.PARAMSIZE_1, command+", wrong number of parameters.", command);
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
	        return setTRDStatus(testRecordData,DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}

	    Log.info(debugmsg +" optionValue: "+optionValue);
	    Integer time = 0;
	    try{
	    	time = Integer.parseInt(optionValue);
	    	Log.info(debugmsg +" time: "+time);
	    }catch(NumberFormatException e){
			message = FAILStrings.convert(FAILStrings.BAD_PARAM, "Invalid parameter value for optionValue", "optionValue");
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
	        return setTRDStatus(testRecordData,DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
	    }
	    
	    String optionName = "";

	    try{
	    	if(command.equalsIgnoreCase(SET_MILLIS_BETWEEN_RECORDS)){
	    		optionName = "MillisBetweenRecords";
	    		driver.setMillisBetweenRecords(time);
	    	}else{
	    		Log.debug(debugmsg+" keyword "+command+" has not been supported yet!");
				message = FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, "Support for "+command+" not found!", command);
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
		        return setTRDStatus(testRecordData,DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
	    	}
	    	
	    	message = GENStrings.convert(GENStrings.SOMETHING_SET, optionValue +" set to "+optionName, optionValue, optionName);
	    	logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);
	    	return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);	
	    }catch(Exception e){
	        Log.debug(debugmsg+e.getMessage());
			message = FAILStrings.convert(FAILStrings.COULD_NOT_SET, 
					                      "Could not set '"+ optionName +"' to '"+ optionValue +"'.",
					                      optionName,
					                      optionValue);
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
	        return setTRDStatus(testRecordData,DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
	    }
	  }
	 
	  /** <br><em>Purpose:</em> get driver's options value, and save it to a staf variable
	   **/
	  private long getDriverOptions(){
		String debugmsg = getClass().getName()+".getDriverOptions() ";  
		
		String stafVariable = null;
		try{ stafVariable = testRecordData.getTrimmedUnquotedInputRecordToken(2);}
		catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){
			message = FAILStrings.convert(FAILStrings.PARAMSIZE_1, command+", wrong number of parameters.", command);
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
	        return setTRDStatus(testRecordData,DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
		
	    Log.info(debugmsg +" staf variable: "+stafVariable);
	    
	    String optionName = "";
	    String optionValue = null;
	    
	    try{
	    	if(command.equalsIgnoreCase(GET_MILLIS_BETWEEN_RECORDS)){
	    		optionName = "MillisBetweenRecords";
	    		optionValue = String.valueOf(driver.getMillisBetweenRecords());
	    	}else{
	    		Log.debug(debugmsg+" keyword "+command+" has not been supported yet!");
				message = FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, "Support for "+command+" not found!", command);
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
		        return setTRDStatus(testRecordData,DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
	    	}

	    	Log.info(debugmsg +" optionValue: "+optionValue);
	    	//Save the optionValue to a staf variable
	    	STAFHelper helper = testRecordData.getSTAFHelper();
	    	boolean saveOk = helper.setVariable(stafVariable, optionValue);
	      
	    	if(saveOk){
		    	message = GENStrings.convert(GENStrings.SUCCESS_2, command +" "+optionValue+" successful.", command, optionValue);
		    	logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);
		    	return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
			}else{
				// error occurs when saving option's value to a staf variable
				Log.debug(debugmsg + " Can not save option value "+ optionValue + " to a staf variable.");
				message = FAILStrings.text(FAILStrings.COULD_NOT_SET_VARS, "Could not set one or more variable values.");
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
		        return setTRDStatus(testRecordData,DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
			}
	    }catch (Exception e){
	    	Log.debug(debugmsg+e.getMessage());
			message = FAILStrings.convert(FAILStrings.COULD_NOT_GET, "Could not get '"+ optionName +"'.", optionName);
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
	        return setTRDStatus(testRecordData,DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
	    }
	  }
	
	private long cmdWaitForGUI(boolean waitforgui){		
		String winname = null;
		String compname = null;
		String timeout = "15";
		int tseconds = 15;

		try{ winname = testRecordData.getTrimmedUnquotedInputRecordToken(2);}
		// should never happen by this point
		catch(SAFSNullPointerException npx){winname="";}
		catch(IndexOutOfBoundsException ibx){winname="";}
		if ( winname.length()==0 ) {
			message = failedText.convert("bad_param", "Invalid parameter value for WINDOWID", "WINDOWID");
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
		try{ compname = testRecordData.getTrimmedUnquotedInputRecordToken(3);}
		// should never happen by this point
		catch(SAFSNullPointerException npx){compname="";}
		catch(IndexOutOfBoundsException ibx){compname="";}
		if ( compname.length()==0 ) {
			message = failedText.convert("bad_param", "Invalid parameter value for COMPONENTID", "COMPONENTID");
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
		String mapname = testRecordData.getAppMapName();
        String winrec = null;
        try{
        	testRecordData.setWindowName(winname);
        	testRecordData.setCompName(compname);
        	winrec = testRecordData.getWindowGuiId();
        }catch(SAFSException e){}
		if (( winrec==null)||(winrec.length()==0 )) {
	        winrec = staf.getAppMapItem(mapname, winname, winname);        
		}
		if (( winrec==null)||(winrec.length()==0 )) {
			//bad_app_map_item  :Item '%1%' was not found in App Map '%2%'
			message = failedText.convert("bad_app_map_item", 
					"Item '"+winname +"' was not found in App Map '"+ mapname+"'", 
					winname, mapname);
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
        // if not image-based let another engine handle it
        if(! ImageUtils.isImageBasedRecognition(winrec)){
        	return setTRDStatus(testRecordData, StatusCodes.SCRIPT_NOT_EXECUTED);
        }
        // we know at least winname is OK in length
		testRecordData.setWindowName(winname);
		testRecordData.setCompName(compname);
		testRecordData.setWindowGuiId(winrec);
		try{ 
			timeout = testRecordData.getTrimmedUnquotedInputRecordToken(4);
			tseconds = Integer.parseInt(timeout);
			if(tseconds < 0) tseconds = 0;
		}
		catch(NumberFormatException nf){
			Log.debug("TIDDC ignoring invalid TIMEOUT value in WaitForGUI. Using Default.");
		}
		catch(SAFSNullPointerException npx){;}
		catch(IndexOutOfBoundsException ibx){;}
		timeout = String.valueOf(tseconds).trim();
		
		try { 
			ImageUtils.recaptureScreen();		
	        Rectangle winloc = null;
	        String who = winname+":"+compname;
	        long currenttime = System.currentTimeMillis();
	        long endtime = currenttime + (1000* tseconds);
	        SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss.SSS");
	        boolean notDone = true;
	        do{
	        	winloc = null;
		        try{
		        	winloc = ImageUtils.findComponentRectangle(testRecordData,0);
		        }catch(SAFSException x){
					message = failedText.convert("bad_app_map_item", 
							"Item '"+winname +"' was not found in App Map '"+ mapname+"'", 
							winname, mapname);
					standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
					return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		        }catch(java.io.IOException iox){
		        	who +=" "+ iox.getMessage();
					message = FAILStrings.convert(FAILStrings.FILE_ERROR, 
		        			"Error opening or reading or writing file '"+ who +"'", 
		        			who);
					standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
					return setTRDStatus(testRecordData, DriverConstant.STATUS_INVALID_FILE_IO);
		        }
		        // evaluate our wait success
		        if (winloc==null){
		        	if(!waitforgui){  // waitforguiGone
		        		message = genericText.convert("gone_timeout", 
		        				who +" was gone within timeout "+ timeout, 
		        				who, timeout);
		        		logMessage( message, null, AbstractLogFacility.GENERIC_MESSAGE);
		        		return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
		        	}
		        }else{
		        	if(waitforgui){
		        		message = genericText.convert("found_timeout", 
		        				who +" was found within timeout "+ timeout, 
		        				who, timeout);
		        		logMessage( message, null, AbstractLogFacility.GENERIC_MESSAGE);
		        		return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
		        	}
		        }
		        currenttime = System.currentTimeMillis();
		        if(currenttime < (endtime - 400)){
			        Log.debug("WAIT 400 millisecond wait commencing at:"+ time.format(new Date()));
	        		try{Thread.sleep(400);}catch(InterruptedException x){}
			        Log.debug("WAIT 400 millisecond complete at:"+ time.format(new Date()));
	        		ImageUtils.recaptureScreen();
			        Log.debug("WAIT recaptureScreen complete at:"+ time.format(new Date()));
		        }else{
		        	notDone = false;
		        }
	        }while(notDone);
	        //not_found_timeout   :%1% was not found within timeout %2%
	        //not_gone_timeout    :%1% was not gone within timeout %2%
	        String snap = saveTestRecordScreenToTestDirectory(testRecordData);	        
	    	if(!waitforgui){  // waitforguiGone
	    		message = genericText.convert("not_gone_timeout", 
	    				who +" was not gone within timeout "+ timeout, 
	    				who, timeout);
			}else{ //waitforgui
	    		message = genericText.convert("not_found_timeout", 
	    				who +" was not found within timeout "+ timeout, 
	    				who, timeout);
			}
			logMessage( message, who+":"+snap, AbstractLogFacility.WARNING_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_SCRIPT_WARNING);
        }
		catch(AWTException a){
			message = FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
        			"Support for 'AWT Robot' not found.", "AWT Robot");
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
        }
	}

	/**
	 * SetBenchDirectory, SetTestDirectory, SetDifDirectory, SetRootVerifyDirectory, SetProjectDirectory
	 */
	private long cmdSetDirectories(){
		String _path = "";
		try{ _path = testRecordData.getTrimmedUnquotedInputRecordToken(2);}
		// should never happen by this point
		catch(SAFSNullPointerException npx){;}catch(IndexOutOfBoundsException ibx){;}

		if ( _path.length()==0 ) {
			message = failedText.convert("bad_param", "Invalid parameter value for PATH", "PATH");
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
		File dir = new CaseInsensitiveFile(_path).toFile();
		if (!dir.isAbsolute()) {
			String pdir = driver.getProjectRootDir();
			if (pdir != null) dir = new CaseInsensitiveFile(pdir, _path).toFile();
		}
		
		if (!dir.isDirectory()) {
			message = failedText.convert("bad_param", "Invalid parameter value for PATH", "PATH");
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
		_path = dir.getAbsolutePath();
		long _status = StatusCodes.SCRIPT_NOT_EXECUTED;
		
		//based on which Set keyword send to driver.set???Directory methods
		if (command.equalsIgnoreCase(COMMAND_SETPROJECTDIRECTORY)){
			_status = driver.setProjectRootDir(_path);
		} else if (command.equalsIgnoreCase(COMMAND_SETBENCHDIRECTORY)){
			_status = driver.setBenchDir(_path);
		}else if (command.equalsIgnoreCase(COMMAND_SETTESTDIRECTORY)){
			_status = driver.setTestDir(_path);
		}else if (command.equalsIgnoreCase(COMMAND_SETDIFDIRECTORY)){
			_status = driver.setDifDir(_path);			
		}else if (command.equalsIgnoreCase(COMMAND_SETROOTVERIFYDIRECTORY)){
			_status = driver.setRootVerifyDir(_path);
		}
		
		if ( _status == StatusCodes.NO_SCRIPT_FAILURE){		
			simpleSuccessUsingMessage(testRecordData, command, _path, null);
		}else if ( _status == StatusCodes.GENERAL_SCRIPT_FAILURE){
			standardErrorMessage(testRecordData, command, testRecordData.getInputRecord());
		}
		return setTRDStatus(testRecordData, _status);
	}
	/**
	 * SetApplicationMap DriverCommand processing.
	 */
	private long cmdSetApplicationMap(){
		String mapname = "";

		try{ mapname = testRecordData.getTrimmedUnquotedInputRecordToken(2);}
		catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){;}

        if(mapname.length()==0){
        	String missing = failedText.text("missing_app_map", "Missing App Map specification");
        	standardErrorMessage(testRecordData, missing, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
        }

		UniqueStringMapInfo mapinfo = new UniqueStringMapInfo(mapname, mapname);
		String mappath = (String)mapinfo.getMapPath(driver);

		if (mappath==null){
        	String invalid = failedText.text("invalid_app_map", "Invalid App Map specification [" + mapname + "]");
        	standardErrorMessage(testRecordData, invalid, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}

		MapsInterface maps = driver.getMapsInterface();

		maps.openMap(mapinfo);
		
		String note = genericText.convert("app_map_set", "Application Map set to "+ mappath, mappath);
		logMessage( note, null, AbstractLogFacility.GENERIC_MESSAGE);
		return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
	}

	/**
	 * SetApplicationMap DriverCommand processing.
	 */
	private long cmdCloseApplicationMap(){
		String mapname = "";

		try{ mapname = testRecordData.getTrimmedUnquotedInputRecordToken(2);}
		catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){;}

        if(mapname.length()==0){
        	String missing = failedText.text("missing_app_map", "Missing App Map specification");
        	standardErrorMessage(testRecordData, missing, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
        }

		UniqueStringMapInfo mapinfo = new UniqueStringMapInfo(mapname, mapname);
		String mappath = (String)mapinfo.getMapPath(driver);

		if (mappath==null){
        	String invalid = failedText.text("invalid_app_map", "Invalid App Map specification [" + mapname + "]");
        	standardErrorMessage(testRecordData, invalid, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}

		MapsInterface maps = driver.getMapsInterface();

		maps.closeMap(mapinfo);
		
		String note = genericText.convert("app_map_close", "Application Map '"+ mappath +"' closed.", mappath);
		logMessage( note, null, AbstractLogFacility.GENERIC_MESSAGE);
		return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
	}

	/**
	 * Expressions DriverCommand processing.
	 */
	private long cmdExpressions(){

		String mode = "";

		try{ mode = testRecordData.getTrimmedUnquotedInputRecordToken(2);}
		catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){;}

        if(mode.length()==0){
        	String missing = failedText.convert("unknownDetail", "Unknown EXPRESSIONS MODE in "+
        	                                    testRecordData.getFilename() +" at line "+
        	                                    String.valueOf(testRecordData.getLineNumber()),
        	                                    command, "MODE", testRecordData.getFilename(), String.valueOf(testRecordData.getLineNumber()));
        	logMessage(missing, testRecordData.getInputRecord(), AbstractLogFacility.FAILED_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
        }
		boolean set = StringUtilities.convertBool(mode);
		driver.setExpressionsEnabled(set);
		String bool = String.valueOf(set).toUpperCase();
		String message = genericText.convert("something_set", command +" set to "+ bool, command, bool);
		logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);
		return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);		
	}

	/**
	 * Do-nothing command.  The InputProcessor and SAFSINPUT has already set the 
	 * variables by this point.
	 */
	private long cmdSetVariableValues(){

		StringBuffer varsraw = new StringBuffer();
		boolean done = false;
		int field = 2;
		while(! done){
			try{
				varsraw.append("'"+ testRecordData.getTrimmedUnquotedInputRecordToken(field++) +"', ");
			}catch(Exception x){ done = true;}
		}
		String vars = varsraw.length()> 0 ? 
				      varsraw.substring(0, varsraw.lastIndexOf(",")) :
				      varsraw.toString();
		String note = command+" "+ vars +" ";
		simpleSuccessMessage(testRecordData, note, null);
		return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);		
	}

	/**
	 * Version DriverCommand processing.
	 */
	private long cmdVersion(){

		String mode = "";

		try{ mode = testRecordData.getTrimmedUnquotedInputRecordToken(2);}
		catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){;}

        if(mode.length()==0){
        	String missing = failedText.convert("unknownDetail", "Unknown VERSION Version in "+
        	                                    testRecordData.getFilename() +" at line "+
        	                                    String.valueOf(testRecordData.getLineNumber()),
        	                                    command, "Version", testRecordData.getFilename(), String.valueOf(testRecordData.getLineNumber()));
        	logMessage(missing, testRecordData.getInputRecord(), AbstractLogFacility.WARNING_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_SCRIPT_WARNING);
        }
		//driver.setVersion(mode);
		String message = genericText.convert("something_set", command +" set to "+ mode, command, mode);
		logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);
		return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);		
	}

    /**
     * 
     */
    private long startEnginePreference(TestRecordHelper testRecordData, String preference){
    	try{
    		driver.startEnginePreference(preference);
    		String message = genericText.convert("functions_preferred", 
    		                 "Preference for "+ preference +" Functions ENABLED.", preference);
			logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);    		
			return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);		
    	}catch(IllegalArgumentException iax){    		
    		String message = failedText.convert("invalid_missing", 
    		                 "Invalid or missing '"+ preference +"' parameter in "+ 
                             testRecordData.getFilename() +" at line "+
                             String.valueOf(testRecordData.getLineNumber()),
                             preference, testRecordData.getFilename(), String.valueOf(testRecordData.getLineNumber()));
			logMessage(message, testRecordData.getInputRecord(), AbstractLogFacility.FAILED_MESSAGE);    		    		
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);		
    	}
    }
    
    /**
     * 
     */
    private long endEnginePreference(TestRecordHelper testRecordData, String preference){
    	try{ 
    		driver.endEnginePreference(preference); 
    	}
    	catch(IllegalArgumentException iax){ }
		String message = genericText.convert("functions_not_preferred", 
		                 "Preference for "+ preference +" Functions DISABLED.", preference);
		logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);    		
		return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);		
    }
    
	/**
	 * UseFunctions DriverCommand processing.
	 */
	private long cmdUseFunctions(){

		String mode = "";

		try{ mode = testRecordData.getTrimmedUnquotedInputRecordToken(2);}
		catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){;}

        if(mode.length()==0){
        	String missing = failedText.convert("unknownDetail", "Unknown "+command+" MODE in "+
        	                                    testRecordData.getFilename() +" at line "+
        	                                    String.valueOf(testRecordData.getLineNumber()),
        	                                    command, "MODE", testRecordData.getFilename(), String.valueOf(testRecordData.getLineNumber()));
        	logMessage(missing, testRecordData.getInputRecord(), AbstractLogFacility.WARNING_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_SCRIPT_WARNING);
        }
		boolean set = StringUtilities.convertBool(mode);
		String engineName = "";
		if(command.equalsIgnoreCase(COMMAND_USEROBOTJFUNCTIONS)){
			engineName = ROBOTJ_ENGINE;
		}else if(command.equalsIgnoreCase(COMMAND_USESAFSFUNCTIONS)){
			engineName = SDC_ENGINE;
		}else if(command.equalsIgnoreCase(COMMAND_USETESTCOMPLETEFUNCTIONS)){
			engineName = TCAFS_ENGINE;
		}else if(command.equalsIgnoreCase(COMMAND_USEIOSFUNCTIONS)){
			engineName = IOS_ENGINE;
		}else if(command.equalsIgnoreCase(COMMAND_USEQTPFUNCTIONS)){
			engineName = QTP_ENGINE;
		}else if(command.equalsIgnoreCase(COMMAND_USEABBOTFUNCTIONS)){
			engineName = ABBOT_ENGINE;
		}else if(command.equalsIgnoreCase(COMMAND_USESELENIUMFUNCTIONS)){
			engineName = SELENIUM_ENGINE;
		}else if(command.equalsIgnoreCase(COMMAND_USEDROIDFUNCTIONS)){
			engineName = DROID_ENGINE;
		}
		
		return ((set)?startEnginePreference(testRecordData,engineName):endEnginePreference(testRecordData,engineName));
	}

	/**
	 * SetImageDebug DriverCommand processing.
	 */
	private long cmdSetImageDebug(){

		String mode = "";

		try{ mode = testRecordData.getTrimmedUnquotedInputRecordToken(2);}
		catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){;}

        if(mode.length()==0){
        	String missing = failedText.convert("unknownDetail", "Unknown SetImageDebug MODE in "+
        	                                    testRecordData.getFilename() +" at line "+
        	                                    String.valueOf(testRecordData.getLineNumber()),
        	                                    command, "MODE", testRecordData.getFilename(), String.valueOf(testRecordData.getLineNumber()));
        	logMessage(missing, testRecordData.getInputRecord(), AbstractLogFacility.WARNING_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_SCRIPT_WARNING);
        }
		ImageUtils.debug = StringUtilities.convertBool(mode);
		//driver.setVersion(mode);
		String message = genericText.convert("something_set", command +" set to "+ mode, command, mode);
		logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);
		return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);		
	}
	
	/**
	 * SetImageFuzzyMatching DriverCommand processing.
	 */
	private long cmdSetImageFuzzyMatching(){

		String mode = "";

		try{ mode = testRecordData.getTrimmedUnquotedInputRecordToken(2);}
		catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){;}

        if(mode.length()==0){
        	String missing = failedText.convert("unknownDetail", "Unknown SetImageFuzzyMatching MODE in "+
        	                                    testRecordData.getFilename() +" at line "+
        	                                    String.valueOf(testRecordData.getLineNumber()),
        	                                    command, "MODE", testRecordData.getFilename(), String.valueOf(testRecordData.getLineNumber()));
        	logMessage(missing, testRecordData.getInputRecord(), AbstractLogFacility.WARNING_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_SCRIPT_WARNING);
        }
		ImageUtils.USE_FUZZY_MATCHING = StringUtilities.convertBool(mode);
		//driver.setVersion(mode);
		String message = genericText.convert("something_set", command +" set to "+ mode, command, mode);
		logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);
		return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);		
	}
	
	  /**
	   * set USE_MULTIPLE_THREADS to true or false.
	   */
	  private long setMutipleThreadSearch(){
			String trueOrFalse = "";

			try{ trueOrFalse = testRecordData.getTrimmedUnquotedInputRecordToken(2);}
			catch(SAFSNullPointerException npx){;} //should never happen by this point
			catch(IndexOutOfBoundsException ibx){;}

	        if(trueOrFalse.length()==0){
	        	String missing = failedText.convert("unknownDetail", "Unknown setMutipleThreadSearch value in "+
	        	                                    testRecordData.getFilename() +" at line "+
	        	                                    String.valueOf(testRecordData.getLineNumber()),
	        	                                    command, "value", testRecordData.getFilename(), String.valueOf(testRecordData.getLineNumber()));
	        	logMessage(missing, testRecordData.getInputRecord(), AbstractLogFacility.WARNING_MESSAGE);
				return setTRDStatus(testRecordData, DriverConstant.STATUS_SCRIPT_WARNING);
	        }
			ImageUtils.USE_MULTIPLE_THREADS = StringUtilities.convertBool(trueOrFalse);

			String message = genericText.convert("something_set", command +" set to "+ trueOrFalse, command, trueOrFalse);
			logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
	  }
	  
	  /**
	   * Send email of result. User needs to specify Mail server information into .INI file as below:
	   * <pre>  
	   *   [SAFS_DRIVERCOMMANDS]
	   *   OUT_MAILSERVER="mail server"
	   *   OUT_MAILSERVERPORT=25|465|587
	   *   OUT_MAILSERVERPROTOCOL=SMTP|SMTPS|TLS
	   *   OUT_MAILUSER=user.name@mail.com
	   *   OUT_MAILPASS=*******
	   *   
	   *   [SAFS_DRIVER]
	   *   #SMTP=xxx (deprecated, replaced by OUT_MAILSERVER)
	   *   #PORT=xxx (deprecated, replaced by OUT_MAILSERVERPORT)
	   *   
	   *</pre>   
	   */
	  private long sendEmail() {
		  
		  String debugmsg = StringUtils.debugmsg(false);
		  
		  String from = "";
		  String tos = "";
		  String subject = "";
		  String msg = "";
		  String attachment = "";
		  Mailer mailer;
		  List<String> recipientsTo = new ArrayList<String>();
		  Map<Message.RecipientType,List<String>> recipients = new HashMap<Message.RecipientType,List<String>>();
		  Map<String,String> attachments_alias = new LinkedHashMap<String,String>();
		  MimeType msg_type = MimeType.html; // default support

		  //Get configuration parameters for initialize a Mailer
		  ConfigureInterface config = driver.getConfigureInterface();
		  String host = config.getNamedValue(DriverConstant.SECTION_SAFS_DRIVERCOMMANDS, MailConstant.OUT_MAILSERVER);
		  if(host==null) host = config.getNamedValue(DriverConstant.SECTION_SAFS_DRIVER,"SMTP");
		  if (host == null){
			  String param = "'Mail Server' not found.";
			  message = failedText.convert("bad_param", "Invalid parameter value for "+param, param);
			  standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
			  return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		  }
		  host = host.trim();
		  
		  int port = Mailer.DEFAULT_PORT;
		  String portStr = config.getNamedValue(DriverConstant.SECTION_SAFS_DRIVERCOMMANDS, MailConstant.OUT_MAILSERVERPORT);
		  if(portStr==null) portStr = config.getNamedValue(DriverConstant.SECTION_SAFS_DRIVER, "PORT");
		  try{
			  portStr = portStr.trim();
			  port = Integer.parseInt(portStr);
		  }catch(Exception e){
			  String param = portStr==null? "Mail Server PORT not found.": "Mail Server PORT '"+portStr+"' is not integer.";
			  message = failedText.convert("bad_param", "Invalid parameter value for "+param, param);
			  standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
			  return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		  }
		  
		  String protocolStr = config.getNamedValue(DriverConstant.SECTION_SAFS_DRIVERCOMMANDS, MailConstant.OUT_MAILSERVERPROTOCOL);
		  Protocol protocol = Mailer.DEFAULT_PROTOCOL;
		  if(protocolStr!=null){
			  try{
				  protocolStr = protocolStr.trim();
				  protocol = Protocol.get(protocolStr);
			  }catch(ParseException e){
				  IndependantLog.error(debugmsg+e.getMessage());
				  String param = "protocol "+protocolStr;
				  message = failedText.convert("bad_param", "Invalid parameter value for "+param, param);
				  standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
				  return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
			  }
		  }
		  
		  String user = config.getNamedValue(DriverConstant.SECTION_SAFS_DRIVERCOMMANDS, MailConstant.OUT_MAILUSER);
		  if(user!=null) user = user.trim();
		  String password = config.getNamedValue(DriverConstant.SECTION_SAFS_DRIVERCOMMANDS, MailConstant.OUT_MAILPASS);
		  if(password!=null) password = password.trim();
		  
		  IndependantLog.debug(debugmsg+" configuration parameters: host="+host+"; port="+port+"; protocol="+protocol+"; user="+user+"; password=******");
		  
		  // from email address
		  try{ from = testRecordData.getTrimmedUnquotedInputRecordToken(2);}
			catch(SAFSNullPointerException npx){;} //should never happen by this point
			catch(IndexOutOfBoundsException ibx){;}		  
		  if ( from.length()==0 ) {
				message = failedText.convert("bad_param", "Invalid parameter value for From", "FROM");
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
				return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		  }
		 
		 // tos email address
		  try{ tos = testRecordData.getTrimmedUnquotedInputRecordToken(3);}
			catch(SAFSNullPointerException npx){;} //should never happen by this point
			catch(IndexOutOfBoundsException ibx){;}		  
		  if ( from.length()==0 ) {
				message = failedText.convert("bad_param", "Invalid parameter value for To", "TO");
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
				return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		  }
		  
		  // subjectline
		  try{ subject = testRecordData.getTrimmedUnquotedInputRecordToken(4);}
			catch(SAFSNullPointerException npx){;} //should never happen by this point
			catch(IndexOutOfBoundsException ibx){;}
		  if ( subject.length()==0 ) {
				message = failedText.convert("bad_param", "Invalid parameter value for Subject", "SUBJECT");
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
				return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		  }
		  
		  // message
		  try{ msg = testRecordData.getTrimmedUnquotedInputRecordToken(5);}
			catch(SAFSNullPointerException npx){;} //should never happen by this point
			catch(IndexOutOfBoundsException ibx){;}
		  if ( msg.length()==0 ) {
				message = failedText.convert("bad_param", "Invalid parameter value for Message", "MESSAGE");
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
				return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		  }
		  
		  // attachment
		  try{ attachment = testRecordData.getTrimmedUnquotedInputRecordToken(6);}
			catch(SAFSNullPointerException npx){;} //should never happen by this point
			catch(IndexOutOfBoundsException ibx){;}
		 
		  
		  try{
			  if(user!=null && password!=null){
				  mailer = new Mailer(host, port, protocol, user, password);
			  }else{
				  mailer = new Mailer(host, port, protocol);
			  }
			  
			  // set sender
			  mailer.setSender(from);

			  // set recipients
			  Mailer.handleRecipients(tos, recipientsTo);
			  recipients.put(Message.RecipientType.TO, recipientsTo);
			 
			  //Handle attachments
			  Mailer.handleAttachments(attachment, attachments_alias);
				
			  //Handle message header, message footer, message content and its type
			  HashMap<Integer/*the message order*/, MimeContent> contents = new HashMap<Integer/*the message order*/, MimeContent>();
			  			 
			  Mailer.addMessag(msg, msg_type, contents);
				
			  mailer.send(recipients, subject, contents , attachments_alias);
			  
	      }catch (Exception mex) {
	         IndependantLog.error(debugmsg+" Fail to send email, due to "+StringUtils.debugmsg(mex));
	         message = "Fail to send email.";
			 standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
		     return setTRDStatus(testRecordData,DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
	      }		  
		  
		// success! set status to ok
		  
		  simpleSuccessMessage(testRecordData, command, message);
		  return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
	  }
	  
}

