package org.safs.tools.engines;

import org.safs.Log;
import org.safs.SAFSNullPointerException;
import org.safs.TestRecordHelper;
import org.safs.logging.AbstractLogFacility;
import org.safs.tools.counters.*;
import org.safs.tools.UniqueStringID;
import org.safs.tools.input.*;
import org.safs.tools.drivers.*;
import org.safs.tools.logs.LogsInterface;
import org.safs.tools.status.*;
import org.safs.tools.stringutils.StringUtilities;
import org.safs.tools.vars.VarsInterface;

/**
 * Provides local in-process support for DriverCounterCommands.  This class is 
 * normally only called by the overall TIDDriverCommands class.
 * <p>
 * This DriverCommands engine does not assume the use of STAF. Instead, it uses the
 * various org.safs.tools Interfaces to talk with the rest of the framework (as made
 * available via the DriverInterface configuration).
 * @author CANAGL DEC 14, 2005 Refactored with DriverConfiguredSTAFInterface superclass
 * @author CANAGL FEB 08, 2010 Fixed some status counter suspend\resume issues.
 *                             Added support for more Start and Stop counter keywords.
 *         CANAGL SEP 17, 2014 Fixing SAFS Crashes due to incomplete initialization.
 */
public class TIDDriverCounterCommands extends GenericEngine {

	/** "SAFS/TIDDriverFlowCommands" */
	static final String ENGINE_NAME  = "SAFS/TIDDriverCounterCommands";

	// START: SUPPORTED DRIVER COMMANDS

	/** "StartCounter" */
	static final String COMMAND_START_COUNTER                      = "StartCounter";
	static final String COMMAND_START_CYCLE                        = "StartCycle";
	static final String COMMAND_START_PROCEDURE                    = "StartProcedure";
	static final String COMMAND_START_REQUIREMENT                  = "StartRequirement";
	static final String COMMAND_START_SUITE                        = "StartSuite";
	static final String COMMAND_START_TESTCASE                     = "StartTestcase";

	/** "StopCounter" */
	static final String COMMAND_STOP_COUNTER                      = "StopCounter";
	static final String COMMAND_STOP_CYCLE                        = "StopCycle";
	static final String COMMAND_STOP_PROCEDURE                    = "StopProcedure";
	static final String COMMAND_STOP_REQUIREMENT                  = "StopRequirement";
	static final String COMMAND_STOP_SUITE                        = "StopSuite";
	static final String COMMAND_STOP_TESTCASE                     = "StopTestcase";

	/** "ResetCounter" */
	static final String COMMAND_RESET_COUNTER                      = "ResetCounter";

	/** "DeleteCounter" */
	static final String COMMAND_DELETE_COUNTER                      = "DeleteCounter";

	/** "SuspendStatusCounts" */
	static final String COMMAND_SUSPEND_STATUS_COUNTS             = "SuspendStatusCounts";

	/** "ResumeStatusCounts" */
	static final String COMMAND_RESUME_STATUS_COUNTS              = "ResumeStatusCounts";

	/** "SetCounterMode" */
	static final String COMMAND_SET_COUNTER_MODE                  = "SetCounterMode";

	/** "StoreCounterInfo" */
	static final String COMMAND_STORE_COUNTER_INFO                  = "StoreCounterInfo";

	/** "LogCounterInfo" */
	static final String COMMAND_LOG_COUNTER_INFO                  = "LogCounterInfo";

	// shared string
	String command = "";
	String message = "";
	String id = "";
	String desc = "";
	
	CountersInterface counters = null;	
		
	// END: SUPPORTED DRIVER COMMANDS

	/** 
	 * 'AllStatusInfo',
	 * 'StepTestsOnly' */
	public static final String[] COUNTER_MODE_TEXT     = {"AllStatusInfo","StepTestsOnly"};


	/**
	 * Constructor for TIDDriverCommands
	 */
	public TIDDriverCounterCommands() {
		super();
		servicename = ENGINE_NAME;
	}

	/**
	 * PREFERRED Constructor for TIDDriverCommands.
	 * Constructs the instance and calls launchInterface to initialize.
	 */
	public TIDDriverCounterCommands(DriverInterface driver) {
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
			counters = driver.getCountersInterface();

		}catch(Exception x){
			Log.error(
			"TIDDriverCounterCommands requires a valid DriverInterface object for initialization!\n"+
			x.getMessage());
		}
	}

	/**
	 * Process the record present in the provided testRecordData.
	 */
	public long processRecord (TestRecordHelper testRecordData){

		this.testRecordData = testRecordData;

		command = testRecordData.getCommand();
		
		Log.info("TIDCounterDC:processing "+String.valueOf(command.length())+":\""+ command +"\".");

		if (command.equalsIgnoreCase(COMMAND_START_COUNTER)||
		    command.equalsIgnoreCase(COMMAND_START_CYCLE)||
		    command.equalsIgnoreCase(COMMAND_START_SUITE)||
		    command.equalsIgnoreCase(COMMAND_START_PROCEDURE)||
		    command.equalsIgnoreCase(COMMAND_START_REQUIREMENT)||
		    command.equalsIgnoreCase(COMMAND_START_TESTCASE))   
		   {return cmdStartCounter(testRecordData);}
		     
		else if(command.equalsIgnoreCase(COMMAND_STOP_COUNTER)||
				command.equalsIgnoreCase(COMMAND_STOP_CYCLE)||
				command.equalsIgnoreCase(COMMAND_STOP_SUITE)||
				command.equalsIgnoreCase(COMMAND_STOP_PROCEDURE)||
				command.equalsIgnoreCase(COMMAND_STOP_REQUIREMENT)||
				command.equalsIgnoreCase(COMMAND_STOP_TESTCASE)) 
		     {return cmdStopCounter(testRecordData);}
		
		else if(command.equalsIgnoreCase(COMMAND_RESET_COUNTER)) 
		     {return cmdResetCounter(testRecordData);}
		else if(command.equalsIgnoreCase(COMMAND_DELETE_COUNTER)) 
		     {return cmdDeleteCounter(testRecordData);}
		else if((command.equalsIgnoreCase(COMMAND_SUSPEND_STATUS_COUNTS))|| 
		        (command.equalsIgnoreCase(COMMAND_RESUME_STATUS_COUNTS)))
		     {return cmdSuspendResumeCounts(testRecordData);}
		else if(command.equalsIgnoreCase(COMMAND_SET_COUNTER_MODE)) 
		     {return cmdSetCounterMode(testRecordData);}
		else if(command.equalsIgnoreCase(COMMAND_STORE_COUNTER_INFO)) 
		     {return cmdStoreCounterInfo(testRecordData);}
		else if(command.equalsIgnoreCase(COMMAND_LOG_COUNTER_INFO)) 
		     {return cmdLogCounterInfo(testRecordData);}

		return DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
	}

	// internal convenience routine for String id and String desc
	private UniqueStringID getCounterUID(TestRecordHelper testRecordData) throws IllegalArgumentException{
		id = "";
		desc = null;
		// extract the id and optional description
		try{ 
			id      = testRecordData.getTrimmedUnquotedInputRecordToken(2);
			desc = testRecordData.getTrimmedUnquotedInputRecordToken(3);
		}catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){;}
		
		// validate our counterID
		if (id.length()==0){
			throw new IllegalArgumentException(failedText.convert("missing_parameter", 
			                             "Missing COUNTERID in table "+ 
			                             testRecordData.getFilename() +
			                             " at line "+ testRecordData.getLineNumber(), 
			                             "COUNTERID", testRecordData.getFilename(), String.valueOf(testRecordData.getLineNumber())));
		}
		return new UniqueStringID(id);
	}
	
	/**
	 * StartCounter DriverCommand processing.
	 */
	private long cmdStartCounter(TestRecordHelper testRecordData){
		
		UniqueStringID uid = null;
		// extract the testname and separator
		try{ uid = getCounterUID(testRecordData); }
		catch(IllegalArgumentException iax){
			logMessage( iax.getMessage(), testRecordData.getInputRecord(), AbstractLogFacility.FAILED_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
		
		int msgtype = AbstractLogFacility.START_COUNTER;
		if (command.equalsIgnoreCase(COMMAND_START_TESTCASE)) 
			msgtype = AbstractLogFacility.START_TESTCASE;
		else if (command.equalsIgnoreCase(COMMAND_START_SUITE)) 
			msgtype = AbstractLogFacility.START_SUITE;
		else if (command.equalsIgnoreCase(COMMAND_START_REQUIREMENT)) 
			msgtype = AbstractLogFacility.START_REQUIREMENT;
		else if (command.equalsIgnoreCase(COMMAND_START_PROCEDURE)) 
			msgtype = AbstractLogFacility.START_PROCEDURE;
		else if (command.equalsIgnoreCase(COMMAND_START_CYCLE))
			msgtype = AbstractLogFacility.START_CYCLE;
		
		try{ 
			counters.initCounter(uid);
			logMessage(id, desc, msgtype);			
		}catch(IllegalArgumentException iax){ // might already exist
			
			try{
				counters.resumeCounter(uid);
				logMessage(id, desc, msgtype);				
				
			}catch(IllegalArgumentException iax2){
				desc = iax2.getMessage();
				message = failedText.convert("failure2", "Unable to perform "+
				                             command +" on "+ id, command, id);
			    standardErrorMessage(testRecordData, message, desc);
				return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
			}
		}
		return setTRDStatus(testRecordData, DriverConstant.STATUS_IGNORE_RETURN_CODE);
	}

	/**
	 * StopCounter DriverCommand processing.
	 */
	private long cmdStopCounter(TestRecordHelper testRecordData){
		
		UniqueStringID uid = null;
		// extract the testname and separator
		try{ uid = getCounterUID(testRecordData); }
		catch(IllegalArgumentException iax){
			logMessage( iax.getMessage(), testRecordData.getInputRecord(), AbstractLogFacility.FAILED_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
		
		int msgtype = AbstractLogFacility.END_COUNTER;
		if (command.equalsIgnoreCase(COMMAND_STOP_TESTCASE)) 
			msgtype = AbstractLogFacility.END_TESTCASE;
		else if (command.equalsIgnoreCase(COMMAND_STOP_SUITE))
			msgtype = AbstractLogFacility.END_SUITE;
		else if (command.equalsIgnoreCase(COMMAND_STOP_PROCEDURE)) 
			msgtype = AbstractLogFacility.END_PROCEDURE;
		else if (command.equalsIgnoreCase(COMMAND_STOP_REQUIREMENT)) 
			msgtype = AbstractLogFacility.END_REQUIREMENT;
		else if (command.equalsIgnoreCase(COMMAND_STOP_CYCLE)) 
			msgtype = AbstractLogFacility.END_CYCLE;
		
		try{ 
			counters.initCounter(uid);
			counters.suspendCounter(uid);
			logMessage(id, desc, msgtype);
			
		}catch(IllegalArgumentException iax){ // might already exist
			
			try{
				counters.suspendCounter(uid);
				logMessage(id, desc, msgtype);				
				
			}catch(IllegalArgumentException iax2){
				desc = iax2.getMessage();
				message = failedText.convert("failure2", "Unable to perform "+
				                             command +" on "+ id, command, id);
			    standardErrorMessage(testRecordData, message, desc);
				return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
			}
		}
		return setTRDStatus(testRecordData, DriverConstant.STATUS_IGNORE_RETURN_CODE);
	}


	/**
	 * ResetCounter DriverCommand processing.
	 */
	private long cmdResetCounter(TestRecordHelper testRecordData){
		
		UniqueStringID uid = null;
		// extract the testname and separator
		try{ uid = getCounterUID(testRecordData); }
		catch(IllegalArgumentException iax){
			logMessage( iax.getMessage(), testRecordData.getInputRecord(), AbstractLogFacility.FAILED_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}

		message = genericText.convert("reset_counter", 
		          "Reset status counter "+id, id);
		
		try{ 
			counters.initCounter(uid);
			logMessage(message, desc, AbstractLogFacility.GENERIC_MESSAGE);
			
		}catch(IllegalArgumentException iax){ // might already exist
			
			try{
				counters.clearCounter(uid);
				counters.resumeCounter(uid);
				logMessage(message, desc, AbstractLogFacility.GENERIC_MESSAGE);				
				
			}catch(IllegalArgumentException iax2){
				desc = iax2.getMessage();
				message = failedText.convert("failure2", "Unable to perform "+
				                             command +" on "+ id, command, id);
			    standardErrorMessage(testRecordData, message, desc);
				return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
			}
		}
		return setTRDStatus(testRecordData, DriverConstant.STATUS_IGNORE_RETURN_CODE);
	}

	/**
	 * DeleteCounter DriverCommand processing.
	 */
	private long cmdDeleteCounter(TestRecordHelper testRecordData){
		
		UniqueStringID uid = null;
		// extract the testname and separator
		try{ uid = getCounterUID(testRecordData); }
		catch(IllegalArgumentException iax){
			logMessage( iax.getMessage(), testRecordData.getInputRecord(), AbstractLogFacility.FAILED_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
		
		message = genericText.convert("deleted_counter", 
		          "Deleted status counter "+id, id);
		try{ 
			counters.deleteCounter(uid);
			logMessage(message, desc, AbstractLogFacility.GENERIC_MESSAGE);
			
		}catch(IllegalArgumentException iax){ // might already exist
			
			logMessage(message, desc, AbstractLogFacility.GENERIC_MESSAGE);				
		}
		return setTRDStatus(testRecordData, DriverConstant.STATUS_IGNORE_RETURN_CODE);
	}

	/**
	 * SuspendResumeCounts DriverCommand processing.
	 */
	private long cmdSuspendResumeCounts(TestRecordHelper testRecordData){

		desc = null;
		// extract optional description
		try{ 
			desc = testRecordData.getTrimmedUnquotedInputRecordToken(2);
			if (desc.length()==0) desc = null;		
		}catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){;}
				
		if(command.equalsIgnoreCase(COMMAND_SUSPEND_STATUS_COUNTS)){
			counters.suspendAllCounting();
			if(driver.getStatusInterface() instanceof StatusCounterInterface){
				((StatusCounterInterface)driver.getStatusInterface()).setSuspended(true);
			}
			message = genericText.text("disabled_counters", 
			          "All counters, public and private, are temporarily disabled.");
			logMessage(message, desc, AbstractLogFacility.SUSPEND_STATUS_COUNTS);
		}else{
			counters.resumeAllCounting();
			if(driver.getStatusInterface() instanceof StatusCounterInterface){
				((StatusCounterInterface)driver.getStatusInterface()).setSuspended(false);
			}
			message = genericText.text("enabled_counters", 
			          "All counters, public and private, shall resume counting.");
			logMessage(message, desc, AbstractLogFacility.RESUME_STATUS_COUNTS);
		}
		return setTRDStatus(testRecordData, DriverConstant.STATUS_IGNORE_RETURN_CODE);
	}		

	/**
	 * SetCounterMode DriverCommand processing.
	 */
	private long cmdSetCounterMode(TestRecordHelper testRecordData){
		
		UniqueStringID uid = null;
		// extract the testname and separator
		try{ uid = getCounterUID(testRecordData); }
		catch(IllegalArgumentException iax){
			logMessage( iax.getMessage(), testRecordData.getInputRecord(), AbstractLogFacility.FAILED_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
		
		if ((desc==null)||(desc.length()==0)){
			message = failedText.convert("missing_parameter", 
			                             "Missing MODE in table "+ 
			                             testRecordData.getFilename() +
			                             " at line "+ testRecordData.getLineNumber(), 
			                             "MODE", testRecordData.getFilename(), String.valueOf(testRecordData.getLineNumber()));
		    logMessage(message, testRecordData.getInputRecord(), AbstractLogFacility.FAILED_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
		
		long mode = 0;
		try{
			mode = Long.parseLong(desc);
			if((mode < 1)||(mode > 2)) mode = 0; //invalid
		}catch(NumberFormatException nfx){
			if(desc.equalsIgnoreCase(COUNTER_MODE_TEXT[0]))      {mode = 1;}
			else if(desc.equalsIgnoreCase(COUNTER_MODE_TEXT[1])) {mode = 2;}
		}
		
		if (mode == 0){
			message = failedText.convert("bad_param", "Invalid parameter value for MODE ",
										 "MODE");
		    standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
		
		message = genericText.convert("set_counter_mode", 
		          "Set status counter "+ id +" to MODE "+ COUNTER_MODE_TEXT[((int)(mode-1))],
		          id, COUNTER_MODE_TEXT[((int)(mode-1))]);
		try{ 
			counters.initCounter(uid);
			counters.setCounterMode(uid, mode);
			logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);
			
		}catch(IllegalArgumentException iax){ // might already exist
			
			try{
				counters.setCounterMode(uid, mode);
				logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);				
				
			}catch(IllegalArgumentException iax2){
				desc = iax2.getMessage();
				message = failedText.convert("failure2", "Unable to perform "+
				                             command +" on "+ id, command, id);
			    standardErrorMessage(testRecordData, message, desc);
				return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
			}
		}
		return setTRDStatus(testRecordData, DriverConstant.STATUS_IGNORE_RETURN_CODE);
	}

	/**
	 * StoreCounterInfo DriverCommand processing.
	 */
	private long cmdStoreCounterInfo(TestRecordHelper testRecordData){
		
		UniqueStringID uid = null;
		
		// extract the testname and separator
		try{ uid = getCounterUID(testRecordData); }
		catch(IllegalArgumentException iax){
			logMessage( iax.getMessage(), testRecordData.getInputRecord(), AbstractLogFacility.FAILED_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
		
		if ((desc==null)||(desc.length()==0)){
			message = failedText.convert("missing_parameter", 
			                             "Missing VARPREFIX in table "+ 
			                             testRecordData.getFilename() +
			                             " at line "+ testRecordData.getLineNumber(), 
			                             "VARPREFIX", testRecordData.getFilename(), String.valueOf(testRecordData.getLineNumber()));
		    logMessage(message, testRecordData.getInputRecord(), AbstractLogFacility.FAILED_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
				
		message = genericText.convert("stored_counter_info", 
		          "Stored status counter "+ id +" to base variable "+ desc,
		          id, desc);
		try{ 
			VarsInterface vars = driver.getVarsInterface();
			CountStatusInterface counter = counters.getStatus(uid);

			vars.setValue( desc +".successes", String.valueOf(counter.getGeneralPasses()));
        	vars.setValue( desc +".warnings", String.valueOf(counter.getGeneralWarnings()));
        	vars.setValue( desc +".general_failures", String.valueOf(counter.getGeneralFailures()));
        	vars.setValue( desc +".IO_failures", String.valueOf(counter.getIOFailures()));

        	vars.setValue( desc +".test_passes", String.valueOf(counter.getTestPasses()));
        	vars.setValue( desc +".test_warnings", String.valueOf(counter.getTestWarnings()));
        	vars.setValue( desc +".test_failures", String.valueOf(counter.getTestFailures()));
        	vars.setValue( desc +".test_records", String.valueOf((counter.getTestFailures() +
        	                                                     counter.getTestWarnings() +
        	                                                     counter.getTestPasses())));

        	vars.setValue( desc +".skipped_records", String.valueOf(counter.getSkippedRecords()));
        	vars.setValue( desc +".total_records", String.valueOf(counter.getTotalRecords()));
        
        	vars.setValue( desc +".filename", id);
        	vars.setValue( desc +".id", id);
        	vars.setValue( desc +".mode", String.valueOf(counter.getMode()));
        	vars.setValue( desc +".suspended", String.valueOf(counter.isSuspended()));
			
			logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_IGNORE_RETURN_CODE);
			
		}catch(IllegalArgumentException iax){ // might not exist
			
			String detail = iax.getMessage();
			message = failedText.convert("failure2", "Unable to perform "+
			                             command +" on "+ id, command, id);
		    standardErrorMessage(testRecordData, message, desc);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
	}

	/**
	 * LogCounterInfo DriverCommand processing.
	 */
	private long cmdLogCounterInfo(TestRecordHelper testRecordData){
		
		UniqueStringID uid = null;
		
		// extract the testname and separator
		try{ uid = getCounterUID(testRecordData); }
		catch(IllegalArgumentException iax){
			logMessage( iax.getMessage(), testRecordData.getInputRecord(), AbstractLogFacility.FAILED_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
		
		try{ 
			CountStatusInterface counter = counters.getStatus(uid);
			LogsInterface logs = driver.getLogsInterface();
			UniqueStringID log = new UniqueStringID(testRecordData.getFac());
			logs.logStatusInfo(log, (StatusInterface)counter, id);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_IGNORE_RETURN_CODE);
			
		}catch(IllegalArgumentException iax){ // might not exist
			
			String detail = iax.getMessage();
			message = failedText.convert("failure2", "Unable to perform "+
			                             command +" on "+ id, command, id);
		    standardErrorMessage(testRecordData, message, desc);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
	}
}

