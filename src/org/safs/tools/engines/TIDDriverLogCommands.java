package org.safs.tools.engines;

import org.safs.Log;
import org.safs.SAFSNullPointerException;
import org.safs.TestRecordHelper;
import org.safs.logging.AbstractLogFacility;
import org.safs.text.GENStrings;
import org.safs.tools.UniqueStringID;
import org.safs.tools.input.MapsInterface;
import org.safs.tools.input.UniqueStringMapInfo;
import org.safs.tools.input.UniqueRecordIDInterface;
import org.safs.tools.input.InputInterface;
import org.safs.tools.input.InputRecordInterface;
import org.safs.tools.input.UniqueStringRecordID ;
import org.safs.tools.logs.LogsInterface;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverInterface;

/**
 * Provides local in-process support for DriverLogCommands.  This class is
 * normally only called by the overall TIDDriverCommands class.
 * <p>
 * This DriverCommands engine does not assume the use of STAF. Instead, it uses the
 * various org.safs.tools Interfaces to talk with the rest of the framework (as made
 * available via the DriverInterface configuration).
 * @author Carl Nagle 		DEC 14, 2005 	Refactored with DriverConfiguredSTAFInterface superclass
 *         Bob Lawler 		Jan 18, 2006 	Updated cmdLogMessage to better handle empty descriptions (RJL)
 * 		   LeiWang		Apr 08, 2008	Add SuspendLogging and ResumeLogging
 * 		   Carl Nagle		FEB 04, 2010	Added missing SuspendLogging and ResumeLogging messages
 *         Carl Nagle       SEP 17, 2014 Fixing SAFS Crashes due to incomplete initialization.
 */
public class TIDDriverLogCommands extends GenericEngine {

	/** "SAFS/TIDDriverLogCommands" */
	static final String ENGINE_NAME  = "SAFS/TIDDriverLogCommands";

	// START: SUPPORTED DRIVER COMMANDS

	/** "LogMessage" */
	static final String COMMAND_LOGMESSAGE = "LogMessage";
	/** "LogTestFailure" */
	static final String COMMAND_LOGTESTFAILURE = "LogTestFailure";
	/** "LogTestSuccess" */
	static final String COMMAND_LOGTESTSUCCESS = "LogTestSuccess";
	/** "LogTestWarning" */
	static final String COMMAND_LOGTESTWARNING = "LogTestWarning";
	/** "LogFailureOK" */
	static final String COMMAND_LOGFAILUREOK = "LogFailureOK";
	/** "LogWarningOK" */
	static final String COMMAND_LOGWARNINGOK = "LogWarningOK";
	
	static final String SUSPENDLOGGING		 = "SuspendLogging";
	static final String RESUMELOGGING 		 = "ResumeLogging";
	
	// END: SUPPORTED DRIVER COMMANDS

	/**
	 * Constructor for TIDDriverCommands
	 */
	public TIDDriverLogCommands() {
		super();
		servicename = ENGINE_NAME;
	}

	/**
	 * PREFERRED Constructor for TIDDriverCommands.
	 * Constructs the instance and calls launchInterface to initialize.
	 */
	public TIDDriverLogCommands(DriverInterface driver) {
		this();
		launchInterface(driver);
	}

	/**
	 * @see GenericEngine#launchInterface(Object)
	 */
	public void launchInterface(Object configInfo){
		super.launchInterface(configInfo);
		try{
			driver = (DriverInterface) configInfo;

		}catch(Exception x){
			Log.error(
			"TIDDriverLogCommands requires a valid DriverInterface object for initialization!\n"+
			x.getMessage());
		}
	}

	/**
	 * Process the record present in the provided testRecordData.
	 */
	public long processRecord (TestRecordHelper testRecordData){

		this.testRecordData = testRecordData;

		String command = testRecordData.getCommand();
		Log.info("TIDLogDC:processing \""+ command +"\".");

	         if(command.equalsIgnoreCase(COMMAND_LOGMESSAGE)) {return cmdLogMessage(testRecordData, AbstractLogFacility.GENERIC_MESSAGE);}
		else if(command.equalsIgnoreCase(COMMAND_LOGTESTFAILURE)) {return cmdLogMessage(testRecordData, AbstractLogFacility.FAILED_MESSAGE);}
		else if(command.equalsIgnoreCase(COMMAND_LOGTESTSUCCESS)) {return cmdLogMessage(testRecordData, AbstractLogFacility.PASSED_MESSAGE);}
		else if(command.equalsIgnoreCase(COMMAND_LOGTESTWARNING)) {return cmdLogMessage(testRecordData, AbstractLogFacility.WARNING_MESSAGE);}
		else if(command.equalsIgnoreCase(COMMAND_LOGFAILUREOK)) {return cmdLogMessage(testRecordData, AbstractLogFacility.FAILED_OK_MESSAGE);}
		else if(command.equalsIgnoreCase(COMMAND_LOGWARNINGOK)) {return cmdLogMessage(testRecordData, AbstractLogFacility.WARNING_OK_MESSAGE);}
		else if(command.equalsIgnoreCase(SUSPENDLOGGING)){
			return suspendLogging();
		}else if(command.equalsIgnoreCase(RESUMELOGGING)){
			return resumeLogging();
		}
		return DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
	}

	/**
	 * LogMessage DriverCommand processing
	 * 
	 * updates:
	 * 01.18.2006 (Bob Lawler) - better handle empty descriptions (RJL)
	 */
	private long cmdLogMessage(TestRecordHelper testRecordData, int msgType) {
		String msg = "";
		String desc = null;
		try {
			msg  = testRecordData.getTrimmedUnquotedInputRecordToken(2);
			desc = testRecordData.getTrimmedUnquotedInputRecordToken(3);
			if ((desc != null) && (desc.length()==0)) desc = null;				//RJL
		}
		catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){;}

		if(msg.length()==0) msg = " ";
		logMessage(msg,desc,msgType);
		switch (msgType){

			case AbstractLogFacility.FAILED_MESSAGE:
				return setTRDStatus(testRecordData, DriverConstant.STATUS_TESTFAILURE_LOGGED);

			case AbstractLogFacility.PASSED_MESSAGE:
				return setTRDStatus(testRecordData, DriverConstant.STATUS_TESTSUCCESS_LOGGED);

			case AbstractLogFacility.WARNING_MESSAGE:
				return setTRDStatus(testRecordData, DriverConstant.STATUS_TESTWARNING_LOGGED);

			default:
				return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
		}
	}
	
	private long suspendLogging(){
		LogsInterface log = driver.getLogsInterface();
		String logfac = null;
		String message = null;
		try {
			logfac  = testRecordData.getTrimmedUnquotedInputRecordToken(2);
			if ((logfac != null) && (logfac.length()==0)) logfac = null;
		}
		catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){;}
		if(logfac==null){
			message = GENStrings.text(GENStrings.LOGGING_SUSPENDED, "SUSPENDING ALL LOGGING.");
			logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);
			log.suspendAllLogs();			
		}else{
			message = GENStrings.convert(GENStrings.LOGNAME_SUSPENDED, "SUSPENDING LOGNAME '"+ logfac +"'", logfac);
			logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);
			log.suspendLog(new UniqueStringID(logfac));			
		}
		return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
	}
	
	private long resumeLogging(){
		LogsInterface log = driver.getLogsInterface();
		String logfac = null;
		String message = null;
		try {
			logfac  = testRecordData.getTrimmedUnquotedInputRecordToken(2);
			if ((logfac != null) && (logfac.length()==0)) logfac = null;
		}
		catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){;}
		if(logfac==null){
			log.resumeAllLogs();			
			message = GENStrings.text(GENStrings.LOGGING_RESUMED, "RESUMING ALL LOGGING.");
			logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);
		}else{
			log.resumeLog(new UniqueStringID(logfac));			
			message = GENStrings.convert(GENStrings.LOGNAME_RESUMED, "RESUMING LOGNAME '"+ logfac +"'", logfac);
			logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);
		}
		return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
	}
}

