package org.safs.tools.engines;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.safs.DCDriverFileCommands;
import org.safs.IndependantLog;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSNullPointerException;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.TestRecordData;
import org.safs.TestRecordHelper;
import org.safs.image.ImageUtils;
import org.safs.logging.AbstractLogFacility;
import org.safs.model.commands.DDDriverFlowCommands;
import org.safs.natives.NativeWrapper;
import org.safs.text.FAILStrings;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.DefaultTestRecordStackable;
import org.safs.tools.ITestRecordStackable;
import org.safs.tools.UniqueStringID;
import org.safs.tools.counters.CountersInterface;
import org.safs.tools.counters.UniqueStringCounterInfo;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverInterface;
import org.safs.tools.drivers.FlowControlInterface;
import org.safs.tools.drivers.InputProcessor;
import org.safs.tools.input.InputInterface;
import org.safs.tools.input.InputRecordInterface;
import org.safs.tools.input.UniqueStringFileInfo;
import org.safs.tools.input.UniqueStringRecordID;
import org.safs.tools.status.StatusCounterInterface;
import org.safs.tools.status.StatusInterface;
import org.safs.tools.stringutils.StringUtilities;

/**
 * Provides local in-process support for DriverFlowCommands.  This class is 
 * normally only called by the overall TIDDriverCommands class.
 * <p>
 * This DriverCommands engine does not assume the use of STAF. Instead, it uses the
 * various org.safs.tools Interfaces to talk with the rest of the framework (as made
 * available via the DriverInterface configuration).
 * @author CANAGL DEC 14, 2005 Refactored with DriverConfiguredSTAFInterface superclass<br>
 *         JunwuMa May 5, 2008 Added OnInRangeGotoBlockID and OnNotInRangeGotoBlockID<br>
 *         LeiWang AUG 23, 2010	Modify method cmdOnGuiExists(): assign the testRecord's windowGuiId to winrec.<br>
 *         CANAGL SEP 17, 2014 Fixing SAFS Crashes due to incomplete initialization.<br>
 *         CANAGL MAY 20, 2016 Added CallJUnit support (moved from TIDDriverCommands).<br>
 */
public class TIDDriverFlowCommands extends GenericEngine implements ITestRecordStackable{

	/** "SAFS/TIDDriverFlowCommands" */
	static final String ENGINE_NAME  = "SAFS/TIDDriverFlowCommands";
	
	/** The ITestRecordStackable used to store 'Test Record' in a FILO. */
	protected ITestRecordStackable testrecordStackable = new DefaultTestRecordStackable();

	// START: SUPPORTED DRIVER COMMANDS

	/** "ExitTable" */
	static final String COMMAND_EXIT_TABLE                       = DDDriverFlowCommands.EXITTABLE_KEYWORD;

	/** "ExitSuite" */
	static final String COMMAND_EXIT_SUITE                       = DDDriverFlowCommands.EXITSUITE_KEYWORD;

	/** "ExitCycle" */
	static final String COMMAND_EXIT_CYCLE                       = DDDriverFlowCommands.EXITCYCLE_KEYWORD;

	/** "CallCycle" */
	static final String COMMAND_CALL_CYCLE                       = DDDriverFlowCommands.CALLCYCLE_KEYWORD;

	/** "CallSuite" */
	static final String COMMAND_CALL_SUITE                       = DDDriverFlowCommands.CALLSUITE_KEYWORD;

	/** "CallStep" */
	static final String COMMAND_CALL_STEP                        = DDDriverFlowCommands.CALLSTEP_KEYWORD;

	/** "CallJUnit" */
	static final String COMMAND_CALLJUNIT						 = DDDriverFlowCommands.CALLJUNIT_KEYWORD;
	
	/** "GotoBlockID" */
	static final String COMMAND_GOTO_BLOCKID                     = DDDriverFlowCommands.GOTOBLOCKID_KEYWORD;
	
	/** "OnContainsGotoBlockID" */
	static final String COMMAND_ON_CONTAINS_GOTO_BLOCKID         = DDDriverFlowCommands.ONCONTAINSGOTOBLOCKID_KEYWORD;
	
	/** "OnNotContainsGotoBlockID" */
	static final String COMMAND_ON_NOT_CONTAINS_GOTO_BLOCKID     = DDDriverFlowCommands.ONNOTCONTAINSGOTOBLOCKID_KEYWORD;
	
	/** "OnEqualGotoBlockID" */
	static final String COMMAND_ON_EQUAL_GOTO_BLOCKID            = DDDriverFlowCommands.ONEQUALGOTOBLOCKID_KEYWORD;
	
	/** "OnNotEqualGotoBlockID" */
	static final String COMMAND_ON_NOT_EQUAL_GOTO_BLOCKID        = DDDriverFlowCommands.ONNOTEQUALGOTOBLOCKID_KEYWORD;
	
	/** "OnGreaterThanGotoBlockID" */
	static final String COMMAND_ON_GREATER_THAN_GOTO_BLOCKID     = DDDriverFlowCommands.ONGREATERTHANGOTOBLOCKID_KEYWORD;
	
	/** "OnNotGreaterThanGotoBlockID" */
	static final String COMMAND_ON_NOT_GREATER_THAN_GOTO_BLOCKID = DDDriverFlowCommands.ONNOTGREATERTHANGOTOBLOCKID_KEYWORD;
	
	/** "OnLessThanGotoBlockID" */
	static final String COMMAND_ON_LESS_THAN_GOTO_BLOCKID        = DDDriverFlowCommands.ONLESSTHANGOTOBLOCKID_KEYWORD;
	
	/** "OnNotLessThanGotoBlockID" */
	static final String COMMAND_ON_NOT_LESS_THAN_GOTO_BLOCKID    = DDDriverFlowCommands.ONNOTLESSTHANGOTOBLOCKID_KEYWORD;
	
	/** "OnInRangeGotoBlockID" */	
	static final String COMMAND_ON_IN_RANGE_GOTO_BLOCKID    	 = DDDriverFlowCommands.ONINRANGEGOTOBLOCKID_KEYWORD;
	
	/** "OnNotInRangeGotoBlockID" */
	static final String COMMAND_ON_NOT_IN_RANGE_GOTO_BLOCKID     = DDDriverFlowCommands.ONNOTINRANGEGOTOBLOCKID_KEYWORD;	
	
	/** "OnFileExistGotoBlockID" */
	static final String COMMAND_ON_FILE_EXIST_GOTO_BLOCKID       = DDDriverFlowCommands.ONFILEEXISTGOTOBLOCKID_KEYWORD;

	/** "OnFileNotExistGotoBlockID" */
	static final String COMMAND_ON_FILE_NOT_EXIST_GOTO_BLOCKID   = DDDriverFlowCommands.ONFILENOTEXISTGOTOBLOCKID_KEYWORD;

	/** "OnRegistryKeyExistGotoBlockID" */
	static final String COMMAND_ON_REGISTRY_KEY_EXIST_GOTO_BLOCKID       = DDDriverFlowCommands.ONREGISTRYKEYEXISTGOTOBLOCKID_KEYWORD;

	/** "OnRegistryKeyNotExistGotoBlockID" */
	static final String COMMAND_ON_REGISTRY_KEY_NOT_EXIST_GOTO_BLOCKID   = DDDriverFlowCommands.ONREGISTRYKEYNOTEXISTGOTOBLOCKID_KEYWORD;

	/** "OnDirectoryExistGotoBlockID" */
	static final String COMMAND_ON_DIRECTORY_EXIST_GOTO_BLOCKID  = DDDriverFlowCommands.OnDirectoryExistGotoBlockID_KEYWORD;

	/** "OnDirectoryNotExistGotoBlockID" */
	static final String COMMAND_ON_DIRECTORY_NOT_EXIST_GOTO_BLOCKID = DDDriverFlowCommands.ONDIRECTORYNOTEXISTGOTOBLOCKID_KEYWORD;
 
	/** "OnGuiExistsGotoBlockID" */
	static final String COMMAND_ONGUIEXISTS_GOTO_BLOCKID         = DDDriverFlowCommands.ONGUIEXISTSGOTOBLOCKID_KEYWORD;
	
	/** "OnGuiNotExistGotoBlockID" */
	static final String COMMAND_ONGUINOTEXIST_GOTO_BLOCKID       = DDDriverFlowCommands.ONGUINOTEXISTGOTOBLOCKID_KEYWORD;
	
	/** Internal Driver Command: UseLocalFlowControl */
	static final String COMMAND_USE_LOCAL_FLOW_CONTROL           = DDDriverFlowCommands.USELOCALFLOWCONTROL_KEYWORD;

	/** Internal Driver Command: SetExitTableBlock */
	static final String COMMAND_SET_EXIT_TABLE_BLOCK             = DDDriverFlowCommands.SETEXITTABLEBLOCK_KEYWORD;

	/** Internal Driver Command: SetGeneralScriptFailureBlock */
	static final String COMMAND_SET_GENERAL_SCRIPT_FAILURE_BLOCK = DDDriverFlowCommands.SETGENERALSCRIPTFAILUREBLOCK_KEYWORD;

	/** Internal Driver Command: SetInvalidFileIOBlock */
	static final String COMMAND_SET_INVALID_FILE_IO_BLOCK        = DDDriverFlowCommands.SETINVALIDFILEIOBLOCK_KEYWORD;

	/** Internal Driver Command: SetNoScriptFailureBlock */
	static final String COMMAND_SET_NO_SCRIPT_FAILURE_BLOCK      = DDDriverFlowCommands.SETNOSCRIPTFAILUREBLOCK_KEYWORD;

	/** Internal Driver Command: SetScriptNotExecutedBlock */
	static final String COMMAND_SET_SCRIPT_NOT_EXECUTED_BLOCK    = DDDriverFlowCommands.SETSCRIPTNOTEXECUTEDBLOCK_KEYWORD;

	/** Internal Driver Command: SetScriptWarningBlock */
	static final String COMMAND_SET_SCRIPT_WARNING_BLOCK         = DDDriverFlowCommands.SETSCRIPTWARNINGBLOCK_KEYWORD;

	  
	// shared string
	String command = "";
	String message = "";
	String detail = "";
	String rtype = "";
		
	// END: SUPPORTED DRIVER COMMANDS
	
	// the safs variable containing the Datapool directory
	static final String SAFS_DATAPOOL_DIR = "safsdatapooldirectory" ;

	/**
	 * Constructor for TIDDriverCommands
	 */
	public TIDDriverFlowCommands() {
		super();
		servicename = ENGINE_NAME;
	}

	/**
	 * PREFERRED Constructor for TIDDriverCommands.
	 * Constructs the instance and calls launchInterface to initialize.
	 */
	public TIDDriverFlowCommands(DriverInterface driver) {
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
			"TIDDriverFlowCommands requires a valid DriverInterface object for initialization!\n"+
			x.getMessage());
		}
	}

	/**
	 * Process the record present in the provided testRecordData.
	 */
	public long processRecord (TestRecordHelper testRecordData){

		this.testRecordData = testRecordData;
		//should have been set by TIDDriverCommands.processRecord		
		staf = testRecordData.getSTAFHelper(); 
		command = testRecordData.getCommand();
		rtype = testRecordData.getRecordType();
		Log.info("TIDFlow:processing \""+ command +"\".");

		if (command.equalsIgnoreCase(COMMAND_CALLJUNIT))
		{return callJUnit();}

		else if (command.equalsIgnoreCase(COMMAND_EXIT_TABLE))   
		{return cmdExitTable(testRecordData);}

		else if(command.equalsIgnoreCase(COMMAND_EXIT_SUITE)) 
		{return cmdExitSuite(testRecordData);}

		else if(command.equalsIgnoreCase(COMMAND_EXIT_CYCLE)) 
		{return cmdExitCycle(testRecordData);}

		else if((command.equalsIgnoreCase(COMMAND_CALL_STEP))  ||
				(command.equalsIgnoreCase(COMMAND_CALL_SUITE)) ||
				(command.equalsIgnoreCase(COMMAND_CALL_CYCLE)))
		{return cmdCallTable(testRecordData);}

		else if(command.equalsIgnoreCase(COMMAND_GOTO_BLOCKID)) 
		{return cmdGotoBlockID(testRecordData);}

		else if(command.equalsIgnoreCase(COMMAND_USE_LOCAL_FLOW_CONTROL)) 
		{return cmdUseLocalFlowControl(testRecordData);}

		else if((command.equalsIgnoreCase(COMMAND_SET_EXIT_TABLE_BLOCK))             ||
				(command.equalsIgnoreCase(COMMAND_SET_GENERAL_SCRIPT_FAILURE_BLOCK)) ||
				(command.equalsIgnoreCase(COMMAND_SET_INVALID_FILE_IO_BLOCK))        ||
				(command.equalsIgnoreCase(COMMAND_SET_NO_SCRIPT_FAILURE_BLOCK))      ||
				(command.equalsIgnoreCase(COMMAND_SET_SCRIPT_NOT_EXECUTED_BLOCK))    ||
				(command.equalsIgnoreCase(COMMAND_SET_SCRIPT_WARNING_BLOCK)))
		{return cmdSetXXXBlock(testRecordData);}

		else if((command.equalsIgnoreCase(COMMAND_ON_CONTAINS_GOTO_BLOCKID))        ||
				(command.equalsIgnoreCase(COMMAND_ON_NOT_CONTAINS_GOTO_BLOCKID))    ||
				(command.equalsIgnoreCase(COMMAND_ON_EQUAL_GOTO_BLOCKID))           ||
				(command.equalsIgnoreCase(COMMAND_ON_NOT_EQUAL_GOTO_BLOCKID))       ||
				(command.equalsIgnoreCase(COMMAND_ON_LESS_THAN_GOTO_BLOCKID))       ||
				(command.equalsIgnoreCase(COMMAND_ON_NOT_LESS_THAN_GOTO_BLOCKID))   ||
				(command.equalsIgnoreCase(COMMAND_ON_GREATER_THAN_GOTO_BLOCKID))    ||
				(command.equalsIgnoreCase(COMMAND_ON_NOT_GREATER_THAN_GOTO_BLOCKID)))
		{return cmdOnCompareTwoValues(testRecordData);}

		else if(
				command.equalsIgnoreCase(COMMAND_ON_FILE_EXIST_GOTO_BLOCKID) ||
				command.equalsIgnoreCase(COMMAND_ON_FILE_NOT_EXIST_GOTO_BLOCKID) ||
				command.equalsIgnoreCase(COMMAND_ON_DIRECTORY_EXIST_GOTO_BLOCKID) ||
				command.equalsIgnoreCase(COMMAND_ON_DIRECTORY_NOT_EXIST_GOTO_BLOCKID)

				) {return cmdFilesystemExistGoto(testRecordData);}
		else if( 
				command.equalsIgnoreCase(COMMAND_ON_IN_RANGE_GOTO_BLOCKID) ||
				command.equalsIgnoreCase(COMMAND_ON_NOT_IN_RANGE_GOTO_BLOCKID)
				) {return cmdOnValueInRangeGoto(testRecordData);}
		else if( command.equalsIgnoreCase(COMMAND_ONGUIEXISTS_GOTO_BLOCKID)
				) {return cmdOnGuiExists(testRecordData, true);}
		else if( command.equalsIgnoreCase(COMMAND_ONGUINOTEXIST_GOTO_BLOCKID)
				) {return cmdOnGuiExists(testRecordData, false);}
		else if( command.equalsIgnoreCase(COMMAND_ON_REGISTRY_KEY_EXIST_GOTO_BLOCKID)
				) {return cmdOnRegistryKeyExistGoto(testRecordData, true);}
		else if( command.equalsIgnoreCase(COMMAND_ON_REGISTRY_KEY_NOT_EXIST_GOTO_BLOCKID)
				) {return cmdOnRegistryKeyExistGoto(testRecordData, false);
		}

		return DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
	}

	private long cmdOnGuiExists(TestRecordHelper testRecordData, boolean isOnGuiExists){		
		String blockid = "";
		String winname = "";
		String compname = "";
		String timeout = "15";
		int tseconds = 15;

		try{ blockid = testRecordData.getTrimmedUnquotedInputRecordToken(2);}
		// should never happen by this point
		catch(SAFSNullPointerException npx){}
		catch(IndexOutOfBoundsException ibx){}
		if ( blockid.length()==0 ) {
			message = failedText.convert("bad_param", "Invalid parameter value for BLOCKID", "BLOCKID");
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
		try{ winname = testRecordData.getTrimmedUnquotedInputRecordToken(3);}
		// should never happen by this point
		catch(SAFSNullPointerException npx){}
		catch(IndexOutOfBoundsException ibx){}
		if ( winname.length()==0 ) {
			message = failedText.convert("bad_param", "Invalid parameter value for WINDOWID", "WINDOWID");
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
		try{ compname = testRecordData.getTrimmedUnquotedInputRecordToken(4);}
		// should never happen by this point
		catch(SAFSNullPointerException npx){}
		catch(IndexOutOfBoundsException ibx){}
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
			timeout = testRecordData.getTrimmedUnquotedInputRecordToken(5);
			tseconds = Integer.parseInt(timeout);
			if(tseconds < 0) tseconds = 0;
		}
		catch(NumberFormatException nf){
			Log.debug("TIDFlow ignoring invalid TIMEOUT value in "+ testRecordData.getCommand()+". Using Default.");
		}
		catch(SAFSNullPointerException npx){;}
		catch(IndexOutOfBoundsException ibx){;}

		timeout = String.valueOf(tseconds).trim();

        //branching :%1% attempting branch to %2%.
		detail = GENStrings.convert(GENStrings.BRANCHING, 
        		testRecordData.getCommand()+" attempting branch to "+ blockid, 
        		testRecordData.getCommand(), blockid);
		try { 
			ImageUtils.recaptureScreen();		
	        Rectangle winloc = null;
	        String who = winname+":"+compname;
	        int count = tseconds;
	        do{
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
		        	if(!isOnGuiExists){  // OnGuiNotExist
		        		message = genericText.convert("gone_timeout", 
		        				who +" was gone within timeout "+ timeout, 
		        				who, timeout);
		        		logMessage( detail +" "+message , null, AbstractLogFacility.GENERIC_MESSAGE);
		        		testRecordData.setStatusInfo(blockid);
		        		return setTRDStatus(testRecordData, DriverConstant.STATUS_BRANCH_TO_BLOCKID);
		        	}
		        }else{
		        	if(isOnGuiExists){
		        		message = genericText.convert("found_timeout", 
		        				who +" was found within timeout "+ timeout, 
		        				who, timeout);
		        		logMessage( detail +" "+message , null, AbstractLogFacility.GENERIC_MESSAGE);
		        		testRecordData.setStatusInfo(blockid);
		        		return setTRDStatus(testRecordData, DriverConstant.STATUS_BRANCH_TO_BLOCKID);
		        	}
		        }
        		try{Thread.sleep(530);}
        		catch(InterruptedException x){}
        		ImageUtils.recaptureScreen();
	        }while(count-- > 0);
	        //not_found_timeout   :%1% was not found within timeout %2%
	        //not_gone_timeout    :%1% was not gone within timeout %2%
	        //not_branching :%1% did not branch to %2%.
	        detail = GENStrings.convert(GENStrings.NOT_BRANCHING, 
	        		testRecordData.getCommand()+" did not branch to "+ blockid, 
	        		testRecordData.getCommand(), blockid);
	    	if(!isOnGuiExists){  // OnGuiNotExist
	    		message = genericText.convert("not_gone_timeout", 
	    				who +" was not gone within timeout "+ timeout, 
	    				who, timeout);
			}else{ 
	    		message = genericText.convert("not_found_timeout", 
	    				who +" was not found within timeout "+ timeout, 
	    				who, timeout);
			}
			logMessage( detail +" "+message , null, AbstractLogFacility.GENERIC_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
        }
		catch(AWTException a){
			message = FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
        			"Support for 'AWT Robot' not found.", "AWT Robot");
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
        }
	}
	
	/**
	 * ExitTable DriverCommand processing.
	 */
	private long cmdExitTable(TestRecordHelper testRecordData){
		return setTRDStatus(testRecordData, DriverConstant.STATUS_EXIT_TABLE_COMMAND);
	}

	/**
	 * ExitSuite DriverCommand processing.
	 */
	private long cmdExitSuite(TestRecordHelper testRecordData){
		if ((testRecordData.getTestLevel().equalsIgnoreCase(DriverConstant.DRIVER_STEP_TESTLEVEL))||
		    (testRecordData.getTestLevel().equalsIgnoreCase(DriverConstant.DRIVER_SUITE_TESTLEVEL))){			
			driver.setExitSuite(true); // allow error recovery processing
		    return setTRDStatus(testRecordData, DriverConstant.STATUS_EXIT_TABLE_COMMAND);
		}		
		message = failedText.convert("failure1", "Unable to perform "+command, command);
		logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);
	    return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
	}

	/**
	 * ExitCycle DriverCommand processing.
	 */
	private long cmdExitCycle(TestRecordHelper testRecordData){
		driver.setExitCycle(true);
	    return setTRDStatus(testRecordData, DriverConstant.STATUS_EXIT_TABLE_COMMAND);
	}

	
	/**
	 * CallStep, CallSuite, CallCycle DriverCommand processing.
	 */
	private long cmdCallTable(TestRecordHelper testRecordData){
		
		Log.info("TIDFlow.cmdCallTable inputrecord: "+ testRecordData.getInputRecord());
		
		String theTest      = "";
		String theSeparator = "";
		String theLogID     = "";
		String theTestLevel = "";

		boolean isStep = testRecordData.getTestLevel().equalsIgnoreCase(DriverConstant.DRIVER_STEP_TESTLEVEL);
		boolean isSuite = testRecordData.getTestLevel().equalsIgnoreCase(DriverConstant.DRIVER_SUITE_TESTLEVEL);
		boolean isCycle = testRecordData.getTestLevel().equalsIgnoreCase(DriverConstant.DRIVER_CYCLE_TESTLEVEL);

		boolean callStep = command.equalsIgnoreCase(COMMAND_CALL_STEP);
		boolean callSuite = command.equalsIgnoreCase(COMMAND_CALL_SUITE);
		boolean callCycle = command.equalsIgnoreCase(COMMAND_CALL_CYCLE);
		
		// extract the testname and separator
		try{ 
			theTest      = testRecordData.getTrimmedUnquotedInputRecordToken(2);
			theSeparator = testRecordData.getTrimmedUnquotedInputRecordToken(3);
		}catch(SAFSNullPointerException npx){
			Log.debug(command +" ignoring NullPointerException: "+ npx.getMessage());
		} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){
			Log.debug(command +" ignoring IndexOutOfBoundsException: "+ ibx.getMessage());
		}

		Log.info(command +" attempting to look for table '"+ theTest +"' using separator '"+ theSeparator +"'.");

		// CallStep only valid for STEP tables at this time.
		// CallSuite only valid for SUITE and STEP tables at this time.
		if (((callStep)&&(! isStep))  ||
		    ((callSuite)&&(isCycle))) {			
			message = failedText.convert("failure2", 
			                             "Unable to perform "+ command +" on \""+ 
			                             theTest +"\"",
			                             theTest, command);
			message = genericText.convert("extended_info", message +
			                              " in table "+ testRecordData.getFilename() +
			                              " at line "+ String.valueOf(testRecordData.getLineNumber()),
			                              message, testRecordData.getFilename(), String.valueOf(testRecordData.getLineNumber()));
			String detail = failedText.text("improper_testlevel", "Not supported at this test level.");                              
			logMessage( message, detail, AbstractLogFacility.FAILED_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}

		if (callStep)  theTestLevel = DriverConstant.DRIVER_STEP_TESTLEVEL;		
		if (callSuite) theTestLevel = DriverConstant.DRIVER_SUITE_TESTLEVEL;
		if (callCycle) theTestLevel = DriverConstant.DRIVER_CYCLE_TESTLEVEL;

		// validate our ACTION/TESTNAME
		if (theTest.length()==0){
			message = failedText.convert("missing_parameter", 
			                             "Missing ACTION/TESTNAME in table "+ 
			                             testRecordData.getFilename() +
			                             " at line "+ testRecordData.getLineNumber(), 
			                             "ACTION/TESTNAME", testRecordData.getFilename(), String.valueOf(testRecordData.getLineNumber()));
			logMessage( message, testRecordData.getInputRecord(), AbstractLogFacility.FAILED_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
		
		// make sure we use the right separator
		if (theSeparator.length()==0) theSeparator = testRecordData.getSeparator();

		// make sure we use the right log
		if (callCycle) { theLogID = driver.getCycleLogName(); }
		else if (callSuite) { theLogID = driver.getSuiteLogName(); }
		else{ theLogID = driver.getStepLogName();}
		
		if (theLogID.length()==0) theLogID = testRecordData.getFac();
		
		UniqueStringID alog = new UniqueStringID(theLogID);
		UniqueStringFileInfo theSource = new UniqueStringFileInfo( theTest, theTest,
		                                                           theSeparator,
		                                                           theTestLevel);

		// run the test and add status info to existing status info
		InputProcessor nextLevel = new InputProcessor(driver, theSource, alog);
		StatusInterface nextStatus = nextLevel.processTest();
		try{
			StatusCounterInterface statusCounter = (StatusCounterInterface) driver.getStatusInterface();
		    statusCounter.addStatus(nextStatus);
		}catch(Exception ex){;}
		
		//Within CallCycle, CallSuite or CallStep, if ExitSuite or ExitCycle is called
		//we need to populate the command to upper level
		if(driver.isExitCycle()) return cmdExitCycle(testRecordData);
		if(driver.isExitSuite()) return cmdExitSuite(testRecordData);
		
		return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
	}
	
	
	/**
	 * GotoBlockID DriverCommand processing
	 */
	private long cmdGotoBlockID(TestRecordHelper testRecordData) {

		String blockname = "";
		String fileid    = "";
		String sep       = "";

		try{ blockname = testRecordData.getTrimmedUnquotedInputRecordToken(2);}
		catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){;}

        if(blockname.length()==0){
        	String problem = failedText.text("missing_blockid", "Missing BlockID specification");
        	standardErrorMessage(testRecordData, problem, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
        }

		return branch2BlockID(testRecordData, blockname);
	}


	/**
	 * UseLocalFlowControl DriverCommand processing
	 */
	private long cmdUseLocalFlowControl(TestRecordHelper testRecordData) {
		String state = "";

		try{ state = testRecordData.getTrimmedUnquotedInputRecordToken(2);}
		catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){;}

		boolean set = (state.length()==0)? true:StringUtilities.convertBool(state);
		driver.setPerTableFlowControl(set);
		String bool = String.valueOf(set).toUpperCase();
		message = genericText.convert("something_set", command +" set to "+ bool, command, bool);
		logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);
		return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
	}			

	/**
	 * SetXXXBlock DriverCommand processing
	 */
	private long cmdSetXXXBlock(TestRecordHelper testRecordData) {

		FlowControlInterface  flow = driver.getFlowControlInterface(testRecordData.getTestLevel());
		long result = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
		String block = "";

		try{ block = testRecordData.getTrimmedUnquotedInputRecordToken(2);}
		catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){;}
		
		if (command.equalsIgnoreCase(COMMAND_SET_EXIT_TABLE_BLOCK)){

			flow.setExitTableBlock(block);
			result = DriverConstant.STATUS_NO_SCRIPT_FAILURE;
			
		}else if (command.equalsIgnoreCase(COMMAND_SET_GENERAL_SCRIPT_FAILURE_BLOCK)){

			flow.setScriptFailureBlock(block);
			result = DriverConstant.STATUS_NO_SCRIPT_FAILURE;
			
		}else if (command.equalsIgnoreCase(COMMAND_SET_INVALID_FILE_IO_BLOCK)){
			
			flow.setIOFailureBlock(block);
			result = DriverConstant.STATUS_NO_SCRIPT_FAILURE;

		}else if (command.equalsIgnoreCase(COMMAND_SET_NO_SCRIPT_FAILURE_BLOCK)){

			flow.setNoScriptFailureBlock(block);
			result = DriverConstant.STATUS_IGNORE_RETURN_CODE;

		}else if (command.equalsIgnoreCase(COMMAND_SET_SCRIPT_NOT_EXECUTED_BLOCK)){
			
			flow.setScriptNotExecutedBlock(block);
			result = DriverConstant.STATUS_NO_SCRIPT_FAILURE;

		}else if (command.equalsIgnoreCase(COMMAND_SET_SCRIPT_WARNING_BLOCK)){
			
			flow.setScriptWarningBlock(block);
			result = DriverConstant.STATUS_NO_SCRIPT_FAILURE;
		}
		// if we processed one of the above SET commands:
		if (! (result==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)){
			
			message = genericText.convert("something_set", command +" set to \""+ block +"\"", command, "\""+ block +"\"");
		    logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);
			return setTRDStatus(testRecordData, result);
		}
		return result;
    }
    
	private long cmdOnFileEOFGoToBlockId(TestRecordHelper testRecordData) {
		
		String block = "" ;
		String fnum = "" ;
		boolean passed = false;
		
		try { 
			block = testRecordData.getTrimmedUnquotedInputRecordToken(2);
			fnum = testRecordData.getTrimmedUnquotedInputRecordToken(3);
		} catch(Exception npx){;}

        if(block.length()==0){
        	String problem = failedText.text("missing_blockid", "Missing BlockID specification");
        	standardErrorMessage(testRecordData, problem, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
        }

        if(fnum.length()==0){
        	String problem = failedText.text("missing_filenum", "Missing FileNumber specification");
        	standardErrorMessage(testRecordData, problem, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
        }
        
        // have blockID and a file number, so do the work evaluating EOF
        // code adapted from DCDriverFileCommands isEndOfFile method
        
        try {
        	// DCDriverFileCommands class variable holds file map
        	Map fileMap = DCDriverFileCommands.getfileMap();
        	// files will either be a Reader or a Writer or possibly null
        	Object rw = fileMap.get(fnum);
        	if (rw instanceof Reader) {
        		Reader reader = (Reader) rw;
        		if (reader != null) {
        			if (reader.markSupported()) reader.mark(10);
        			Boolean eof = new Boolean(reader.read() == -1);
        			if( eof.booleanValue() ) {
        				passed=true ;
        			}
        		}
        	} else if (rw instanceof Writer) {
        		// our writers are always at eof
        		passed=true ;
        	}
        } catch (IOException ioe) {
        	// the rw threw an exception.
        	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        }

        // write message and branch if passed
		if (passed) { 
			message = genericText.convert("branching", 
			command +" attempting branch to "+ block,
			command, block); 			
		}else{        
			message = genericText.convert("not_branching", 
			command +" did not branch to "+ block,
			command, block);
		}
		logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);

		if (passed) return branch2BlockID(testRecordData, block);
        return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);

	}
	
	private long cmdOnRegistryKeyExistGoto(TestRecordHelper testRecordData, boolean cmdexists){
		final String TIMEOUT_DEFAULT = "15";
		String blockid = "";
		String keyname = "";
		String valuename = "";
		String strtimeout = "";		
		int tseconds = 15;
		boolean isWarnOK = rtype.equalsIgnoreCase(DriverConstant.RECTYPE_CW);
		long status_warn = isWarnOK ? DriverConstant.STATUS_NO_SCRIPT_FAILURE:DriverConstant.STATUS_SCRIPT_WARNING;
		try{ blockid = testRecordData.getTrimmedUnquotedInputRecordToken(2);}
		catch(Exception npx){}
		if ( blockid.length()==0 ) {
			message = failedText.convert("bad_param", "Invalid parameter value for BLOCKID", "BLOCKID");
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}

		try{ keyname = testRecordData.getTrimmedUnquotedInputRecordToken(3);}
		catch(Exception npx){}
		if ( keyname.length()==0 ) {
			message = failedText.convert("bad_param", "Invalid parameter value for KEY", "KEY");
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
		String msgval = keyname;
		
		try{ valuename = testRecordData.getTrimmedUnquotedInputRecordToken(4);}
		catch(Exception npx){}
		if ( valuename.length()==0 ) valuename = null;
	    if (valuename == null){
			Log.info(command +" seeking a Key.  No KeyValue specified...");
		}else{
			msgval +=":"+ valuename;
		}

	    try{ strtimeout = testRecordData.getTrimmedUnquotedInputRecordToken(5);}
		catch(Exception npx){}
		if ( strtimeout.length()==0 ) strtimeout = TIMEOUT_DEFAULT;
	    try{ 
			tseconds = Integer.parseInt(strtimeout);
			if(tseconds < 0) {
				tseconds = 0;
				strtimeout = "0";
			}
			Log.info(command +" using TIMEOUT value "+ strtimeout);
		}
		catch(NumberFormatException nf){
			strtimeout = TIMEOUT_DEFAULT;
			Log.info(command +" IGNORING invalid TIMEOUT value. Using Default "+ TIMEOUT_DEFAULT);
		}
		boolean exists = false;
		boolean matched = false;
		for(int i=0;!matched && i<=tseconds;i++){ 
			exists = NativeWrapper.DoesRegistryKeyExist(keyname, valuename);
			matched = (cmdexists == exists);
		  	if(!matched && i<tseconds) try{Thread.sleep(1000);}catch(Exception x){;}	  		
		}
		if(matched){
	  		message = genericText.convert(GENStrings.BRANCHING, 
					command +" attempting branch to "+ blockid, 
					command, blockid);
			if(cmdexists){
		  		detail = genericText.convert(GENStrings.FOUND_TIMEOUT, 
						msgval +" was found within timeout "+ strtimeout,
						msgval, strtimeout);
			}else{
		  		detail = genericText.convert(GENStrings.GONE_TIMEOUT, 
						msgval +" was gone within timeout "+ strtimeout,
						msgval, strtimeout);
			}
	  		simpleGenericMessage(testRecordData, message, detail);
	  		return branch2BlockID(testRecordData, blockid) ;
		// not matched
		}else{
	  		message = genericText.convert(GENStrings.NOT_BRANCHING, 
					command +" did not branch to "+ blockid, 
					command, blockid);
			if(cmdexists){
		  		detail = failedText.convert(FAILStrings.NOT_FOUND_TIMEOUT, 
						msgval +" was not found within timeout "+ strtimeout,
						msgval, strtimeout);
			}else{
		  		detail = failedText.convert(FAILStrings.NOT_GONE_TIMEOUT, 
						msgval +" was not gone within timeout "+ strtimeout,
						msgval, strtimeout);
			}
	  		if(isWarnOK){
		  		simpleGenericMessage(testRecordData, message, detail);
	  		}else{
	  			simpleGenericWarningMessage(testRecordData, message +" "+ detail);
	  		}
	        return setTRDStatus(testRecordData, status_warn);
        }		  
	}
	
	
	private long cmdFilesystemExistGoto(TestRecordHelper testRecordData) {
		/* this function handles the keywords  OnFileExistGotoBlockID,
					 OnFileNotExistGotoBlockID, OnDirectoryExistGotoBlockID, and
			 		 OnDirectoryNotExistGotoBlockID
			all these keywords have the same input format:
			BLOCKID, FILENAME
 		 */
 		 
		String block = "" ;
		String fname = "" ;
		
		try { 
			block = testRecordData.getTrimmedUnquotedInputRecordToken(2);
			fname = testRecordData.getTrimmedUnquotedInputRecordToken(3);
		} catch(Exception npx){;}

        if(block.length()==0){
        	String problem = failedText.text("missing_blockid", "Missing BlockID specification");
        	standardErrorMessage(testRecordData, problem, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
        }

		// may have absolute or relative file specified
		File theFile = new CaseInsensitiveFile(fname).toFile() ;
		String fullFilePath = null ;
		if( theFile.isAbsolute() ) {
			// absolute path, so can use it directly
 			fullFilePath = fname ;
	 	} else {
			// file is relatvie to Datapool directory
			// would like to use org.safs.STAFHelper for the static string for safsdatapooldirectory
			// but the header comment for this class says DriverCommands engine does not assume the use of STAF
			String datapooldir = driver.getVarsInterface().getValue(SAFS_DATAPOOL_DIR);
			fullFilePath = datapooldir + fname ;
		}
		Log.info("resolved full file path is: " + fullFilePath) ;

		File fullFile = new CaseInsensitiveFile(fullFilePath).toFile() ;
		// the story about the file
		boolean exists = fullFile.exists() ;
		boolean isdir  = fullFile.isDirectory() ;

		// the specifics are handled based on the keyword and whether the file/directory
		// exists and is or is not a directory
		if(command.equalsIgnoreCase(COMMAND_ON_FILE_EXIST_GOTO_BLOCKID)) {
			// should exist and not be a directory
			if( exists && ! isdir ) return branch2BlockID(testRecordData, block) ;
		} else if(command.equalsIgnoreCase(COMMAND_ON_FILE_NOT_EXIST_GOTO_BLOCKID)) {
			// should not exist or is a directory
			if( ! exists || isdir ) return branch2BlockID(testRecordData, block) ;
		} else if(command.equalsIgnoreCase(COMMAND_ON_DIRECTORY_EXIST_GOTO_BLOCKID)) {
			// should exist and be a directory
			if( exists && isdir ) return branch2BlockID(testRecordData, block) ;
		} else if(command.equalsIgnoreCase(COMMAND_ON_DIRECTORY_NOT_EXIST_GOTO_BLOCKID)) {
			// should not exist or is not a dir
			if( ! exists || ! isdir) return branch2BlockID(testRecordData, block) ;
		}		

		return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);

	}

	/**
	 * branchToBlockID: called by internal Goto Driver Command processors.
	 * The blockname is expected to be non-null and length > 0. 
	 */
	private long cmdOnCompareTwoValues(TestRecordHelper testRecordData) {

		String block = "";
		String val1  = "";
		String val2  = "";
		String mode  = "";
		boolean caseSensitive = true;
		boolean passed = false;
		boolean isNumeric = false;
		String ucval1 = "";
		String ucval2 = "";
		
		try{ 
			block = testRecordData.getTrimmedUnquotedInputRecordToken(2);
			val1  = testRecordData.getTrimmedUnquotedInputRecordToken(3);
			val2  = testRecordData.getTrimmedUnquotedInputRecordToken(4);
			mode  = testRecordData.getTrimmedUnquotedInputRecordToken(5);
		}catch(Exception npx){;}
		
		if (mode.equalsIgnoreCase("CaseInsensitive")) caseSensitive = false;
		
        if(block.length()==0){
        	String problem = failedText.text("missing_blockid", "Missing BlockID specification");
        	standardErrorMessage(testRecordData, problem, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
        }

		double dval1 = 0;
		double dval2 = 0;
		
		// try as numbers first
		try{
			dval1 = Double.parseDouble(val1);
			dval2 = Double.parseDouble(val2);
			isNumeric = true;
		}
		// do as strings if not numbers
		catch(NumberFormatException nfe){;}

		if (! caseSensitive){
			ucval1 = val1.toUpperCase();
			ucval2 = val2.toUpperCase();
		}else{
			ucval1 = val1;
			ucval2 = val2;
		}
		
		String criteria = "";

		// CONTAINS -- always a string comparison
		if((command.equalsIgnoreCase(COMMAND_ON_CONTAINS_GOTO_BLOCKID)) ||
		   (command.equalsIgnoreCase(COMMAND_ON_NOT_CONTAINS_GOTO_BLOCKID))){
		   	boolean contains = false;
			int i = ucval1.indexOf(ucval2);
			contains = (i > -1);
	   		if (command.equalsIgnoreCase(COMMAND_ON_CONTAINS_GOTO_BLOCKID)){
	   		    passed = contains;
	   		}else{ 
	   			passed = ! contains;
	   		}
			if (contains){
				criteria = genericText.convert("contains", 
				           val1 +" contains "+ val2,
				           val1, val2);
			} else {
				criteria = genericText.convert("not_contain", 
				           val1 +" did not contain "+ val2,
				           val1, val2);
			}		
		}else
		// EQUALS / NOT EQUAL
		if((command.equalsIgnoreCase(COMMAND_ON_EQUAL_GOTO_BLOCKID))||
		   (command.equalsIgnoreCase(COMMAND_ON_NOT_EQUAL_GOTO_BLOCKID))){
		   	boolean equals;
		  
		   	if(isNumeric){
		   		equals = (dval1 == dval2);		   		
		   	// not Numeric
			} else {
				equals = ucval1.equals(ucval2);
			}		
	   		if (command.equalsIgnoreCase(COMMAND_ON_EQUAL_GOTO_BLOCKID)){
	   		    passed = equals;
	   		}else{ 
	   			passed = ! equals;
	   		}
   		    if (equals) {criteria = genericText.convert("equals", 
   		    	         val1 +" equals "+ val2,
   		    	         val1, val2);}
   		    else {criteria = genericText.convert("not_equal", 
   		    	  val1 +" did not equal "+ val2,
   		    	  val1, val2);}
			
		}else
		// LESS THAN
		if((command.equalsIgnoreCase(COMMAND_ON_LESS_THAN_GOTO_BLOCKID))||
		   (command.equalsIgnoreCase(COMMAND_ON_NOT_LESS_THAN_GOTO_BLOCKID))){
		   	boolean lesser;
		   	
		   	if(isNumeric){
		   		lesser = (dval1 < dval2);		   		
		   	// not Numeric
			} else {
				lesser = ((ucval1.compareTo(ucval2)) < 0);
			}		
	   		if (command.equalsIgnoreCase(COMMAND_ON_LESS_THAN_GOTO_BLOCKID)){
	   		    passed = lesser;
	   		}else{ 
	   			passed = ! lesser;
	   		}
   		    if (lesser) {criteria = genericText.convert("less", 
   		    	         val1 +" is less than "+ val2,
   		    	         val1, val2);}
   		    else {criteria = genericText.convert("not_less", 
   		    	  val1 +" is not less than "+ val2,
   		    	  val1, val2);}
		}
		// GREATER THAN
		if((command.equalsIgnoreCase(COMMAND_ON_GREATER_THAN_GOTO_BLOCKID))||
		   (command.equalsIgnoreCase(COMMAND_ON_NOT_GREATER_THAN_GOTO_BLOCKID))){
		   	boolean greater;
		   	
		   	if(isNumeric){
		   		greater = (dval1 > dval2);		   		
		   	// not Numeric
			} else {
				greater = ((ucval1.compareTo(ucval2)) > 0);
			}		
	   		if (command.equalsIgnoreCase(COMMAND_ON_GREATER_THAN_GOTO_BLOCKID)){
	   		    passed = greater;
	   		}else{ 
	   			passed = ! greater;
	   		}
   		    if (greater) {criteria = genericText.convert("greater", 
   		    	         val1 +" is greater than "+ val2,
   		    	         val1, val2);}
   		    else {criteria = genericText.convert("not_greater", 
   		    	  val1 +" is not greater than "+ val2,
   		    	  val1, val2);}
		}

		// write message and branch if passed
		if (passed) { 
			message = genericText.convert("branching", 
			command +" attempting branch to "+ block,
			command, block); 			
		}else{        
			message = genericText.convert("not_branching", 
			command +" did not branch to "+ block,
			command, block);
		}
		message += "  "+ criteria;
		if (! caseSensitive) message += " (CASEINSENSITIVE)";
		logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);
		
		if (passed) return branch2BlockID(testRecordData, block);
		return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
	}
	
	/**
	 * perform OnInRangeGotoBlockID and OnNotInRangeGotoBlockID.
	 * Goto block "NoError" if val1 is (not) between val2 and val3.
	 * if val1, val2 and val3 are not numeric, they will be treated as strings for comparison
	 * Example:  C, OnInRangeGotoBlockID, "NoError", val1, val2, val3, [CaseSensitive]
	 * 
	 */
	private long cmdOnValueInRangeGoto(TestRecordHelper testRecordData) {

		String block = "";
		String val1  = "";
		String val2  = "";
		String val3  = "";
		String mode  = "";
		boolean caseSensitive = true;
		boolean passed = false;
		boolean isNumeric = false;
		String ucval1 = "";
		String ucval2 = "";
		String ucval3 = "";
		Log.info("........starting: "+testRecordData.getCommand());
		try{ 
			block = testRecordData.getTrimmedUnquotedInputRecordToken(2);
			val1  = testRecordData.getTrimmedUnquotedInputRecordToken(3);
			val2  = testRecordData.getTrimmedUnquotedInputRecordToken(4);
			val3  = testRecordData.getTrimmedUnquotedInputRecordToken(5);
			mode  = testRecordData.getTrimmedUnquotedInputRecordToken(6);
		}catch(Exception npx){;}
		
		if (mode.equalsIgnoreCase("CaseInsensitive")) caseSensitive = false;
		
        if(block.length()==0){
        	String problem = failedText.text("missing_blockid", "Missing BlockID specification");
        	standardErrorMessage(testRecordData, problem, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
        }

		double dval1 = 0;
		double dval2 = 0;
		double dval3 = 0;		
		// try as numbers first
		try{
			dval1 = Double.parseDouble(val1);
			dval2 = Double.parseDouble(val2);
			dval3 = Double.parseDouble(val3);
			isNumeric = true;
		}
		// do as strings if not numbers
		catch(NumberFormatException nfe){
		    Log.debug("not numeric! be treated as strings for comparison");
		}

		if (! caseSensitive){
			ucval1 = val1.toUpperCase();
			ucval2 = val2.toUpperCase();
			ucval3 = val3.toUpperCase();
		}else{
			ucval1 = val1;
			ucval2 = val2;
			ucval3 = val3;
		}
		
	   	boolean IsInRange;
		if(isNumeric){
		    IsInRange = (dval1 >= dval2)&&(dval1 <= dval3);		   		
	   	// not Numeric
		} else {
			IsInRange = ((ucval1.compareTo(ucval2)) >= 0)&&((ucval3.compareTo(ucval1)) >= 0);
		}	
        if (command.equalsIgnoreCase(COMMAND_ON_IN_RANGE_GOTO_BLOCKID))
            passed = IsInRange;
        if (command.equalsIgnoreCase(COMMAND_ON_NOT_IN_RANGE_GOTO_BLOCKID))
            passed = !IsInRange;            
	        
		if (passed) { 
			message = genericText.convert("branching", 
			command +" attempting branch to "+ block,
			command, block); 			
		}else{        
			message = genericText.convert("not_branching", 
			command +" did not branch to "+ block,
			command, block);
		}
		
	    if (IsInRange)
	        message += genericText.convert("in_range",val1+" in range "+val2+" to "+val3,val1,val2,val3);
	    else
	        message += genericText.convert("not_in_ range",val1+" not in range "+val2+" to "+val3,val1,val2,val3);
        
	    Log.info(message);
        
	    // TIDDriverFlowCommands.message (not a local variable) is used in branch2BlockID
	    if (passed) 
	        return branch2BlockID(testRecordData, block);
	    else
		    logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE); // noly for not passed. If passed, message will be logged in branch2BlockID 

	    return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
	}	
	
	/**
	 * branchToBlockID: called by internal Goto Driver Command processors.
	 * The blockname is expected to be non-null and length > 0. 
	 * 
	 * 11.15.2005	bolawl	-Corrected method to use testRecordData to retrieve linenumber
	 *                       on failed attempts and InputRecordInterface on successful
	 *                       branches. Updated messages to be more in line with those of
	 *                       ther engines. (RJL)
	 */
	private long branch2BlockID(TestRecordHelper testRecordData, String blockname) {

		String fileid    = "";
		String sep       = "";

		InputInterface input = driver.getInputInterface();

		fileid = testRecordData.getFileID();
		sep    = testRecordData.getSeparator();
		// need to get UniqueRecordInterface recordInfo to pass to input.gotoRecord
		UniqueStringRecordID recordinfo = new UniqueStringRecordID(fileid,sep, blockname) ;
		InputRecordInterface status = input.gotoRecord(recordinfo);
		
		// return according to the status of the input.gotoRecord method
		String table = testRecordData.getFilename();
		if( status.isValid() ) {
			String blocklinenum = String.valueOf(status.getRecordNumber());
			String note = genericText.convert("transfer_to_block", 
					                          "TRANSFERRING EXECUTION TO BLOCKID '"+ blockname + "'", 
											  blockname);
			note = genericText.convert("extended_info", 
                                       note + " in table " + table + " at line "+ blocklinenum + ".",
                                       note, table, blocklinenum);
		    logMessage(note, null, AbstractLogFacility.GENERIC_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
		} else {
			String recordlinenum = String.valueOf(testRecordData.getLineNumber());
			String problem = failedText.convert("unable_to_transfer", 
			                                    "UNABLE TO TRANSFER EXECUTION TO BLOCKID '"+ blockname + "'",
			                                    blockname);
			problem = genericText.convert("extended_info", 
			                              problem + " in table " + table + " at line "+ recordlinenum + ".",
			                              problem, table, recordlinenum);				
			String description = failedText.convert("blockid_not_found", 
                                              "BlockId '" + blockname + "' not found.",
											  blockname);
			logMessage(problem, description, AbstractLogFacility.FAILED_MESSAGE);
		    logMessage(testRecordData.getInputRecord(), null, AbstractLogFacility.GENERIC_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}
	}
	/**
	 * Execute a JUnit test.
	 * @param classname	String, the 'JUnit test class' name to be executed.
	 * @return String, the JUnit test result
	 * @throws ClassNotFoundException if the 'JUnit test class' can be found.
	 * @throws SAFSException if the 'JUnit execution' return null.
	 */
	private String callJUnit(String classname) throws ClassNotFoundException, SAFSException{
		String debugmsg = StringUtils.debugmsg(false);

		JUnitCore junit = new JUnitCore();
		Result jresult = junit.run(Class.forName(classname));

		if(jresult == null){
			String detail = "JUnitCore executed '"+classname+"', and returned a null Result!";
			IndependantLog.debug(debugmsg+" failure: "+detail);
			throw new SAFSException(detail);
		}
		StringBuffer sb = new StringBuffer();
		sb.append(jresult.getRunCount()+ " tests run.\n");
		sb.append(jresult.getIgnoreCount()+" tests ignored.\n");
		sb.append(jresult.getFailureCount()+ " tests failed.\n");
		sb.append("Runtime: "+ jresult.getRunTime() +" milliseconds.\n");
		List<Failure> failures = jresult.getFailures();
		for(Failure failure:failures){
			sb.append("\n");
			sb.append("   "+ failure.toString()+"\n");
		}
		// increment our overall SAFS test status with equivalent JUnit test results.
		int testPassCount = jresult.getRunCount()-jresult.getIgnoreCount()-jresult.getFailureCount();
		for(int i=0;i < testPassCount;i++){
			driver.getCountersInterface().incrementAllCounters(
					new UniqueStringCounterInfo(null, testRecordData.getTestLevel()),
					CountersInterface.STATUS_TEST_PASS);
		}
		for(int i=0;i < jresult.getIgnoreCount();i++){
			driver.getCountersInterface().incrementAllCounters(
					new UniqueStringCounterInfo(null, testRecordData.getTestLevel()),
					CountersInterface.STATUS_SKIPPED_RECORD);
		}
		for(int i=0;i < jresult.getFailureCount();i++){
			driver.getCountersInterface().incrementAllCounters(
					new UniqueStringCounterInfo(null, testRecordData.getTestLevel()),
					CountersInterface.STATUS_TEST_FAILURE);
		}
		IndependantLog.debug(debugmsg+" completed with result:\n "+sb.toString());
		return sb.toString();
	}
	
	/**
	 * Handle the CallJUnit Keyword.<br>
	 * 
	 * C, CallJUnit, package.p1.class;package.p2.class<br>
	 * 
	 * Supports classname separators sem-colon, colon, comma, and space.<br>
	 * 
	 * @return long, the execution status code.
	 * @throws SAFSException if the 'JUnit execution' return null
	 */
	private long callJUnit() {

		String debugmsg = StringUtils.debugmsg(false);
		String classnames = null;
		//get the JUnit test class names, it could be semi-colon separated string, like package.p1.class;package.p2.class;
		try{ classnames = testRecordData.getTrimmedUnquotedInputRecordToken(2);}
		catch(SAFSNullPointerException | IndexOutOfBoundsException e){;}

		if (!StringUtils.isValid(classnames)){
			message = failedText.convert("bad_param", "Invalid parameter value for ClassNames", "ClassNames");
			standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
			return setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
		}

		testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
		IndependantLog.info(debugmsg+"............................. handling CallJUnit: "+classnames);

		IndependantLog.debug(debugmsg+" begin command '"+command+"' with Test Record "+StringUtils.toStringWithAddress(testRecordData));
		
		//CACHE TestRecordData. Within callJUnit() if we call some SAFS/SE+/JSAFS test, the class field testRecordData
		//might get changed, we need to cache it. Then after calling of callJUnit(), we set it back.
		pushTestRecord(testRecordData);

		String[] clazzes = null;
		if(classnames.contains(StringUtils.SEMI_COLON)) 
			clazzes = classnames.split(StringUtils.SEMI_COLON);
		else if(classnames.contains(StringUtils.COLON)) 
			clazzes = classnames.split(StringUtils.COLON);
		else if(classnames.contains(StringUtils.COMMA)) 
			clazzes = classnames.split(StringUtils.COMMA);
		else clazzes = classnames.split(StringUtils.SPACE);

		boolean withWarning = false;
		StringBuffer result = new StringBuffer();
		for(String clazz:clazzes){
			if(!StringUtils.isValid(clazz)){
				IndependantLog.warn(debugmsg+" the class name '+clazz+' is not valid!");
				continue;
			}
			String message = genericText.convert("something_set", "'"+command+"' set to '"+clazz+"'", command, clazz);
			logMessage(message, null, AbstractLogFacility.START_PROCEDURE);
			result.append("\n---------------------------CallJUnit '"+clazz+"' Begin Results -----------------------------------\n");
			try {
				result.append(callJUnit(clazz));
			} catch (Exception e) {
				//This will be considered as a warning.
				withWarning = true;
				String msg = "'"+clazz+"' was not executed! Due to "+StringUtils.debugmsg(e);
				IndependantLog.warn(debugmsg+msg);
				result.append(msg);
			} 
			result.append("---------------------------CallJUnit '"+clazz+"' End Results -----------------------------------\n");
		}

		//Set CACHED TestRecordData back.
		popTestRecord();
		command = testRecordData.getCommand();

		//Set the JUnit test result to the test record's status for future use.
		testRecordData.setStatusInfo(result.toString());
		
		IndependantLog.debug(debugmsg+" finished command '"+command+"' with Test Record "+StringUtils.toStringWithAddress(testRecordData));

		if(withWarning){
			String message = genericText.convert("standard_warn", command+" warning in table "+testRecordData.getFilename()+" at line "+testRecordData.getLineNumber()+".", command, classnames);
			logMessage(message, result.toString(), AbstractLogFacility.WARNING_MESSAGE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_SCRIPT_WARNING);
		}else{
			String message = genericText.convert("success2", command+" '"+ classnames +"' successful.", command, classnames);
			logMessage(message, result.toString(), AbstractLogFacility.END_PROCEDURE);
			return setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
		}		  
	}
	
	/**
	 * <p>
	 * Push the current 'test record' into the Stack before the execution of a keyword.
	 * This should be called after the 'test record' is properly set.
	 * </p>
	 * 
	 * @param trd TestRecordData, the test record to push into a stack
	 * @see #callJUnit()
	 * @see #popTestRecord()
	 */
	public void pushTestRecord(TestRecordData trd) {
		testrecordStackable.pushTestRecord(trd);
	}

	/**
	 * Retrieve the Test-Record from the the Stack after the execution of a keyword.<br>
	 * <p>
	 * After execution of a keyword, pop the test record from Stack and return is as the result.
	 * Replace the class field 'Test Record' by that popped from the stack if they are not same.
	 * </p>
	 * 
	 * @see #callJUnit()
	 * @see #pushTestRecord()
	 * @return TestRecordData, the 'Test Record' on top of the stack
	 */
	public TestRecordData popTestRecord() {
		String debugmsg = StringUtils.debugmsg(false);
		DefaultTestRecordStackable.debug(debugmsg+"Current test record: "+StringUtils.toStringWithAddress(testRecordData));
		
		TestRecordData history = testrecordStackable.popTestRecord();
		
		if(!testRecordData.equals(history)){
			DefaultTestRecordStackable.debug(debugmsg+"Reset current test record to: "+StringUtils.toStringWithAddress(history));
			//The cast should be safe, as we push TestRecordHelper into the stack.
			testRecordData = (TestRecordHelper) history;
		}
		
		return history;
	}
}

