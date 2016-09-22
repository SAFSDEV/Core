/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * @author JunwuMa SEP 27, 2010 Added controls for retrying a test record that is modified in SAFSMonitorFrame. 
 * <br>SEP 03, 2013 	(SBJLWA) Delay the "flow control" if the execution is going to be paused. 
 * <br>SEP 11, 2013 	(SBJLWA) Store the "record separator" to variable "SAFS/Hook/separator" before executing test record.
 * <br>SEP 26, 2014 	(SBJLWA) Modify routeToPreferredEngines()/routeToEngines(): 
 *                               set 'MORE_ENGINES' to testrecord's status-info if there are more engines can be used.
 */
package org.safs.tools.drivers;

import java.util.ListIterator;

import org.safs.JavaHook;
import org.safs.Log;
import org.safs.SAFSNullPointerException;
import org.safs.STAFHelper;
import org.safs.StringUtils;
import org.safs.TestRecordData;
import org.safs.TestRecordHelper;
import org.safs.logging.AbstractLogFacility;
import org.safs.model.tools.Driver;
import org.safs.staf.STAFProcessHelpers;
import org.safs.text.FAILStrings;
import org.safs.text.GENStrings;
import org.safs.tools.CoreInterface;
import org.safs.tools.UniqueIDInterface;
import org.safs.tools.UniqueStringID;
import org.safs.tools.counters.CountersInterface;
import org.safs.tools.counters.UniqueStringCounterInfo;
import org.safs.tools.engines.EngineInterface;
import org.safs.tools.input.InputInterface;
import org.safs.tools.input.InputRecordInterface;
import org.safs.tools.input.MapsInterface;
import org.safs.tools.input.SourceInterface;
import org.safs.tools.input.UniqueSourceInterface;
import org.safs.tools.input.UniqueStringFileInfo;
import org.safs.tools.input.UniqueStringRecordID;
import org.safs.tools.logs.UniqueStringMessageInfo;
import org.safs.tools.status.StatusInterface;

/**
 * Processes a single input source test table.  
 * All initialization of the overall Test Driver is assumed to be completed.  
 * This is, essentially, the class that loops through the input records and 
 * gets the testing done.
 * 
 */
public class InputProcessor extends AbstractInputProcessor {

	/**
	 * The input source name provided when initialized.
	 * This is NOT the same as our <i>unique</i> input source.  Use {@link #getUniqueSourceInfo()} 
	 * for that, instead.  This field has only the test/file name, test level, and 
	 * separator.
	 */
	protected SourceInterface sourceid = null;

	/**
	 * The unique ID of the log we are to use.
	 */
	protected UniqueIDInterface logid = null;
	
	private boolean isCycle = false;
	private boolean isSuite = false;
	private boolean isStep  = false;
    private String activeTableVar = null;
    
	/***************************************************************************
	 * Constructor for InputProcessor
	 */
	public InputProcessor(DriverInterface driver, SourceInterface sourceid, UniqueIDInterface logid) {
		super(driver);
		this.sourceid = sourceid;
		this.logid    = logid;
		counterInfo   = new UniqueStringCounterInfo( null, sourceid.getTestLevel());

		isCycle = (sourceid.getTestLevel().equalsIgnoreCase(DriverConstant.DRIVER_CYCLE_TESTLEVEL));
		isSuite = (sourceid.getTestLevel().equalsIgnoreCase(DriverConstant.DRIVER_SUITE_TESTLEVEL));
		isStep = (sourceid.getTestLevel().equalsIgnoreCase(DriverConstant.DRIVER_STEP_TESTLEVEL));
		if (isCycle) activeTableVar = "safsActiveCycle";
		if (isSuite) activeTableVar = "safsActiveSuite";
		if (isStep) activeTableVar = "safsActiveStep";
	}
	
	/**
	 * <p>
	 * The current InputProcessor will be set to org.safs.model.tools.Driver, so that it can be used to execute 'test record'
	 * within Java code. If the 'test record' is 'driver command', there is no problem; while if the 'test record' is 
	 * 'component function' and the InputProcessor is NOT at Step level, the 'component function' will be treated as 'Switch Suite'/'Switch Step'
	 * and fail. For example, if InputProcessor is at Cycle level, then "T, Window, Component, VerifyValues, a, b", will be treated to execute Window.STD suite file.
	 * To avoid this situation, we need to set the field 'isStep' before executing 'component function'.
	 * This function is for this purpose.
	 * </p>
	 * 
	 * @return boolean, if the private filed {@link #isStep} has been changed.
	 * 
	 * @see #processTest()
	 * @see #resetTestLevel()
	 * @ee {@link #processTestRecord(TestRecordHelper)}
	 * @see org.safs.model.tools.AbstractDriver#runComponentFunctionConverted(String, String, String, String...)
	 */
	public boolean checkTestLevelForStepExecution(){
		if(!DriverConstant.DRIVER_STEP_TESTLEVEL.equalsIgnoreCase(getTestLevel())){
			Log.debug("InputProcessor: Current Test Level is "+getTestLevel()+", 'Step Test Record' cannot be executed! Change private field 'isStep' to true.");
			isStep = true;
			return true;
		}
		return false;
	}
	
	/**
	 * Reset those private fields about the 'test level'.
	 * 
	 * @see #checkTestLevelForStepExecution()
	 * @see org.safs.model.tools.AbstractDriver#runComponentFunctionConverted(String, String, String, String...)
	 */
	public void resetTestLevel(){
		isCycle = (sourceid.getTestLevel().equalsIgnoreCase(DriverConstant.DRIVER_CYCLE_TESTLEVEL));
		isSuite = (sourceid.getTestLevel().equalsIgnoreCase(DriverConstant.DRIVER_SUITE_TESTLEVEL));
		isStep = (sourceid.getTestLevel().equalsIgnoreCase(DriverConstant.DRIVER_STEP_TESTLEVEL));
	}
	
	/***************************************************************************
	 * Convenience routine for building the appropriate MessageInfo and logging
	 * a message to our active log.  Consult the AbstractLogFacility for valid 
	 * msgtype values.
	 * 
	 * @see org.safs.logging.AbstractLogFacility 
	 */
	public void logMessage(String msg, String msgdescription, int msgtype){
		UniqueStringMessageInfo msgInfo = new UniqueStringMessageInfo(
											  (String)logid.getUniqueID(),
											  msg, msgdescription, msgtype);
											  
		getLogsInterface().logMessage(msgInfo);
	}
	
	/***************************************************************************
	 * Overridden by specific SourceInterface we own.
	 * @see SourceInterface#getSourceName()
	 */
	public String getTestName() {
		return sourceid.getSourceName();
	}

	/***************************************************************************
	 * Overridden by specific SourceInterface we own.
	 * @see SourceInterface#getTestLevel()
	 */
	public String getTestLevel() {
		return sourceid.getTestLevel();
	}
	
	/***************************************************************************
	 * Overridden by specific SourceInterface we own.
	 * @see SourceInterface#getDefaultSeparator()
	 */
	public String getDefaultSeparator() {
		return sourceid.getDefaultSeparator();
	}

	/***************************************************************************
	 * Generate a unique ID with the testname as root. 
	 */
	protected UniqueStringFileInfo getUniqueSourceInfo (){
		
		String testname = sourceid.getSourceName();
		Object hash = new Object();
		
		return new UniqueStringFileInfo(testname + hash.hashCode(), 
		    						    testname,
		    						    sourceid.getDefaultSeparator(), 
		    						    sourceid.getTestLevel());		
	}

	/***************************************************************************
	 * Attempts to move the input pointer to the beginning of the record AFTER 
	 * the blockID specified.  Logs Success or Failure message if the blockID is 
	 * or is not successfully found.
	 * <p>
	 * On failure, increments the general failure count of the local statuscounter 
	 * and all active public counters.  Does not increment counters on success as we 
	 * are just attempting to move the input pointer for the next input record.
	 * <p>
	 * @return 
	 * DriverConstant.STATUS_NO_SCRIPT_FAILURE on successfully locating the desired 
	 * blockID.<br>
	 * DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE if not successful.
	 * 
	 * 11.15.2005	bolawl	-Corrected method to use testRecordData to retrieve linenumber
	 *                       on failed attempts. Updated messages to be more in line with 
	 *                       messages from other engines. (RJL)
	 */
	public long locateBlockID(InputInterface input, 
	                          UniqueSourceInterface sourceinfo,
	                          String blockID){

		InputRecordInterface inputrecord = input.gotoRecord(new UniqueStringRecordID(
		                                                    (String)sourceinfo.getUniqueID(),
		                                                    sourceinfo.getDefaultSeparator(),
		                                                    blockID));
		String message = null;
		String description = null;
		String table = sourceinfo.getSourcePath(driver);

		if(inputrecord.isValid()) {
			String blocklinenum = String.valueOf(inputrecord.getRecordNumber());
			message = GENStrings.convert("transfer_to_block", 
			                             "TRANSFERRING EXECUTION TO BLOCKID '"+ blockID + "'",
			                             blockID);
			message = GENStrings.convert("extended_info", 
                                         message + " in table " + table + " at line "+ blocklinenum + ".",
                                         message, table, blocklinenum);
			logMessage(message, null, AbstractLogFacility.GENERIC_MESSAGE);
			return DriverConstant.STATUS_NO_SCRIPT_FAILURE;
		}
		else{
			String recordlinenum = String.valueOf(testRecordData.getLineNumber());
			message = FAILStrings.convert("unable_to_transfer", 
			                              "UNABLE TO TRANSFER EXECUTION TO BLOCKID '"+ blockID + "'",
			                              blockID);
			message = GENStrings.convert("extended_info", 
			                              message + " in table " + table + " at line "+ recordlinenum + ".",
			                              message, table, recordlinenum);				
			description = FAILStrings.convert("blockid_not_found", 
                                              "BlockId '" + blockID + "' not found.",
											  blockID);
			logMessage(message, description, AbstractLogFacility.FAILED_MESSAGE);
			logMessage(testRecordData.getInputRecord(), null, AbstractLogFacility.GENERIC_MESSAGE);
			statusCounter.incrementGeneralFailures();
			CountersInterface counts = getCountersInterface();
			counts.incrementAllCounters( counterInfo, counts.STATUS_GENERAL_FAILURE);
			return DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE;
		}			
	}
	
	/***************************************************************************
	 * Route the input record to preferred engines only in the order of preference.
	 * Normally, we forward the input record to each engine until one of the 
	 * engines signals that it processed the record.
	 */
	protected long routeToPreferredEngines(TestRecordHelper trd){
		long result = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;	
		ListIterator<?> list = null;
		
		
		// try preferred engines first in the order they are preferred
		if (hasEnginePreferences()){
			list = getEnginePreferences();
			String enginename = null;
			while((list.hasNext())&&(result==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)){
				try{
					enginename = (String) list.next();
					Log.info("InputProcessor trying preferred engine '"+ enginename +"'");
					EngineInterface theEngine = getPreferredEngine(enginename);
					//Add "MORE_ENGINES" status-info to testrecord so that the current engine will know
					//there are still more engines can be tried to execute the keyword
//					if(list.hasNext() || hasMoreEnginesToTry()) 
					trd.setStatusInfo(trd.getStatusInfo()+" "+DriverConstant.MORE_ENGINES);
					result = theEngine.processRecord(trd);
					Log.info("InputProcessor preferred engine '"+ enginename +"' returned result:"+ result);
				}catch(IllegalArgumentException iax){
					// this should not happen!
					System.out.println(iax.getMessage());
					Log.error(iax.getMessage());
				}
			}
		}
		return result;
	}
	
	/**
	 * Test if driver has more engines than preferredEngines
	 * @return
	 */
	private boolean hasMoreEnginesToTry(){
		ListIterator<?> engines = driver.getEngines();
		ListIterator<?> preferredEngines = driver.getEnginePreferences();
		while(engines.hasNext()){
			engines.next();
			if(preferredEngines.hasNext()){
				preferredEngines.next();
			}else{
				return true;
			}
		}
		return false;
	}
	
	/***************************************************************************
	 * Route the input record to one or more engines, or all engines.
	 * Normally, we forward the input record to each engine until one of the 
	 * engines signals that it processed the record.
	 * <p>
	 * If 'sendAll' is TRUE, it means the record needs to be processed by EVERY 
	 * engine, regardless of the response from any one engine.  For example, 
	 * if the driver must tell each engine to clear a cache, or something.
	 * <p>
	 * if 'sendAll' is FALSE, we will not route to any engine that is 'preferred' 
	 * because all 'preferred' engines should have already been tried.
	 * 
	 * @see #routeToPreferredEngines(TestRecordHelper)
	 **************************************************************************/
	protected long routeToEngines( TestRecordHelper trd, boolean sendAll) {
	
		long result = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;			
		ListIterator list = driver.getEngines();
		
		while((list.hasNext())&&
		      ((result==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)||
		       (sendAll))){
			EngineInterface theEngine = (EngineInterface) list.next();
			try{
				// don't call it if we already called it as a 'preferred engine
				if((!sendAll)&&(isPreferredEngine(theEngine))) continue;
				Log.info("InputProcessor trying engine '"+ theEngine.getEngineName() +"'");
				//Add "MORE_ENGINES" status-info to testrecord so that the current engine will know
				//there are still more engines can be tried to execute the keyword
				if(list.hasNext()){
					if(sendAll){
						trd.setStatusInfo(trd.getStatusInfo()+" "+DriverConstant.MORE_ENGINES);
					}else if(!isPreferredEngine((EngineInterface) list.next())){
						trd.setStatusInfo(trd.getStatusInfo()+" "+DriverConstant.MORE_ENGINES);
						list.previous();
					}
				}
				result = theEngine.processRecord(trd);
				Log.info("InputProcessor engine '"+ theEngine.getEngineName() +"' returned result:"+ result);
			}catch(IllegalArgumentException iax){
				// this should not happen!
				System.out.println(iax.getMessage());
				Log.error(iax.getMessage());
			}
		}
		
		if (sendAll) result = DriverConstant.STATUS_NO_SCRIPT_FAILURE;
		return result;
	}
	
	/***************************************************************************
	 * Process a Driver Command (C,CW,or CF) input record.
	 * This is called internally by processTest as necessary.
	 */
	public long processDriverCommand(TestRecordHelper trd){

		String command = "";
		String message = "";

		// extract the command
		try{ command = trd.getTrimmedUnquotedInputRecordToken(1);}
		catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){;}

		// validate our ACTION/TESTNAME
		if (command.length()==0){
			message = FAILStrings.convert("missing_parameter", 
			                      "Missing Driver Command in table "+ 
			                       trd.getFilename() +
						          " at line "+ String.valueOf(trd.getLineNumber()),
						          "Driver Command", trd.getFilename(),String.valueOf(trd.getLineNumber()));
			logMessage( message, trd.getInputRecord(), AbstractLogFacility.FAILED_MESSAGE);

			//return DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE;
			return DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
		}

		long result = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
				
		// set the TestRecordData command field 
		trd.setCommand(command);
		
		if (!PREFERRED_ENGINES_OVERRIDE ||
		   (!hasEnginePreferences())){
			// try important driver control commands first
			Log.debug("InputProcessor.processDriverCommand trying TIDDriverCommands BEFORE preferred engines...");
			try{ result = getTIDDriverCommands().processRecord(trd);}
				 catch(NullPointerException x){;}
			
			Log.debug("InputProcessor.processDriverCommand trying AutoIT BEFORE preferred engines...");
			// try AutoIt next if not executed
			if ((result==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
				(! trd.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)))
				result =  getAutoItComponentSupport().processRecord(trd);
		}

		// try preferred engines 
		if ((result==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
			(! trd.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)))
		    result=routeToPreferredEngines(trd);			

		if (PREFERRED_ENGINES_OVERRIDE && 
		   (result==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
		   (! trd.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD))&&
		   (hasEnginePreferences()))
		{
			Log.debug("InputProcessor trying TIDDriverCommands AFTER preferred engines...");
			// try important driver control commands first
			try{ result = getTIDDriverCommands().processRecord(trd);}
				 catch(NullPointerException x){;}
		}

		// try all remaining engines
		if ((result==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
			(! trd.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)))
		    result = routeToEngines(trd, false);
		    
		// try in-process handlers 
		if ((result==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
			(! trd.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)))
			try{ result = getIPDriverCommands().processRecord(trd);}
			catch(NullPointerException x){;}
		
		if ((result==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
			(! trd.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD))){
			message = FAILStrings.convert("unknownDetail", "Unknown Driver Command \""+ 
			                              command +"\" in table "+ trd.getFilename() +
			                              " at line "+ trd.getLineNumber(),
			                              "Driver Command", command, trd.getFilename(), String.valueOf(trd.getLineNumber())); 
			logMessage( message, trd.getInputRecord(), AbstractLogFacility.WARNING_MESSAGE);
			return DriverConstant.STATUS_SCRIPT_WARNING;
		}
		return result;
	}

	/***************************************************************************
	 * Process an Engine Command (E) input record.
	 * This is called internally by processTest as necessary.
	 */
	protected long processEngineCommand(TestRecordHelper trd){

		String command = "";
		String message = "";

		// extract the command
		try{ command = trd.getTrimmedUnquotedInputRecordToken(1);}
		catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){;}

		// validate our ACTION/TESTNAME
		if (command.length()==0){
			message = FAILStrings.convert("missing_parameter", 
			                      "Missing Engine Command in table "+ 
			                       trd.getFilename() +
						          " at line "+ String.valueOf(trd.getLineNumber()),
						          "Engine Command", trd.getFilename(),String.valueOf(trd.getLineNumber()));
			logMessage( message, trd.getInputRecord(), AbstractLogFacility.FAILED_MESSAGE);

			//return DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE;
			return DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
		}

		long result = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
				
		// set the TestRecordData command field 
		trd.setCommand(command);
		
		// try preferred engines 
		if ((result==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
			(! trd.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)))
		    result=routeToPreferredEngines(trd);			

		// try all remaining engines
		if ((result==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
			(! trd.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)))
		    result = routeToEngines(trd, false);
		    
		if ((result==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
			(! trd.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD))){
			message = FAILStrings.convert("unknownDetail", "Unknown Engine Command \""+ 
			                              command +"\" in table "+ trd.getFilename() +
			                              " at line "+ trd.getLineNumber(),
			                              "Engine Command", command, trd.getFilename(), String.valueOf(trd.getLineNumber())); 
			logMessage( message, trd.getInputRecord(), AbstractLogFacility.WARNING_MESSAGE);
			return DriverConstant.STATUS_SCRIPT_WARNING;
		}
		return result;
	}

	/***************************************************************************
	 * Process a Step Level test record -- a component function. (T,TW,or TF)
	 * This is called internally by processTest as necessary.
	 */
	protected long processComponentFunction(TestRecordHelper trd){
		
		String command = "";
		String message = "";

		// extract the command
		try{ command = trd.getTrimmedUnquotedInputRecordToken(3);}
		catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){;}

		// validate our ACTION COMMAND
		if (command.length()==0){
			message = FAILStrings.convert("missing_parameter", 
			                      "Missing Action Command in table "+ 
			                       trd.getFilename() +
						          " at line "+ String.valueOf(trd.getLineNumber()),
						          "Action Command", trd.getFilename(),String.valueOf(trd.getLineNumber()));
			logMessage( message, trd.getInputRecord(), AbstractLogFacility.FAILED_MESSAGE);

			//return DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE;
			return DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
		}

		long rc = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
				
		// set the TestRecordData command field
		trd.setCommand(command);

		if (!PREFERRED_ENGINES_OVERRIDE ||
		   (!hasEnginePreferences())){
			// try internal CF support
			Log.debug("InputProcessor.processComponentFunction trying internal TIDComponent BEFORE preferred engines...");
			try{ rc = getTIDGUIlessComponentSupport().processRecord(trd);}
		    catch(NullPointerException x){;}
			
			// try AutoIt next if not executed
			Log.debug("InputProcessor.processComponentFunction trying AutoIT BEFORE preferred engines...");
			if ((rc==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
				(! trd.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)))
				rc =  this.getAutoItComponentSupport().processRecord(trd);
		}
		
		// try preferred engines next
		if ((rc==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
			(! trd.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)))
			rc =  routeToPreferredEngines(trd);

		if (PREFERRED_ENGINES_OVERRIDE && 
		   (rc==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
		   (! trd.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD))&&
		   (hasEnginePreferences()))
		{
			// try internal CF support
			try{ rc = getTIDGUIlessComponentSupport().processRecord(trd);}
		    catch(NullPointerException x){;}
		}
		
		// finally try any remaining engines
		if ((rc==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
			(! trd.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD))){
			rc =  routeToEngines(trd, false);
		}

		if ((rc==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
			(! trd.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD))){
			message = FAILStrings.convert("unknownDetail", "Unknown Action Command \'"+ 
			                              command +"\' in table "+ trd.getFilename() +
			                              " at line "+ trd.getLineNumber(),
			                              "Action Command", command, trd.getFilename(), String.valueOf(trd.getLineNumber())); 
			logMessage( message, trd.getInputRecord(), AbstractLogFacility.WARNING_MESSAGE);
			rc = DriverConstant.STATUS_SCRIPT_WARNING;
		}
		return rc;
	}

	/***************************************************************************
	 * Process a Test (T,TW,or TF) input record.
	 * This is called internally by processTest as necessary.
	 */
	public long processTestRecord(TestRecordHelper trd){
		
		// if a STEP, call engines
		if (isStep) return processComponentFunction(trd);

		String theLogID = "";
		String theSeparator = "";
		String theTest = "";
		String theTestLevel = DriverConstant.DRIVER_STEP_TESTLEVEL;
		
		if (isCycle) theTestLevel = DriverConstant.DRIVER_SUITE_TESTLEVEL;

		// extract the testname and separator
		try{ 
			theTest      = trd.getTrimmedUnquotedInputRecordToken(1);
			theSeparator = trd.getTrimmedUnquotedInputRecordToken(2);
		}catch(SAFSNullPointerException npx){;} //should never happen by this point
		catch(IndexOutOfBoundsException ibx){;}

		// validate our ACTION/TESTNAME
		if (theTest.length()==0){
			logMessage( "Missing ACTION/TESTNAME in table "+ trd.getFilename() +
			            " at line "+ trd.getLineNumber(), trd.getInputRecord(), 
			            AbstractLogFacility.FAILED_MESSAGE);

			//return DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE;
			return DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
		}
		
		// make sure we use the right separator
		if (theSeparator.length()==0) theSeparator = trd.getSeparator();

		// make sure we use the right log
		if (isCycle) { theLogID = driver.getSuiteLogName(); }
		else{ theLogID = driver.getStepLogName();}
		if (theLogID.length()==0) theLogID = trd.getFac();
		
		UniqueStringID alog = new UniqueStringID(theLogID);
		UniqueStringFileInfo theSource = new UniqueStringFileInfo( theTest, theTest,
		                                                           theSeparator,
		                                                           theTestLevel);

		// run the test and add status info to existing status info
		InputProcessor nextLevel = new InputProcessor(this, theSource, alog);
		StatusInterface nextStatus = nextLevel.processTest();
		Driver.setIDriver(this);
		statusCounter.addStatus(nextStatus);
		TestRecordData nltrd = nextLevel.getTestRecordData();
		if ((nltrd.getStatusCode()==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED)&&
			(nltrd.getStatusInfo().equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD))){
			trd.setStatusCode(DriverConstant.STATUS_SCRIPT_NOT_EXECUTED);
			trd.setStatusInfo(JavaHook.SHUTDOWN_RECORD);
			return DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
		}
		return DriverConstant.STATUS_NO_SCRIPT_FAILURE;
	}
	
	/***************************************************************************
	 * Process a Skipped (S) input record.
	 * This is called internally by processTest as necessary.
	 */
	protected long processSkippedRecord(TestRecordHelper trd){
		String text = "";
		try{ text = trd.getTrimmedUnquotedInputRecordToken(1); }
		catch(SAFSNullPointerException npx){;}
		logMessage (text, null, AbstractLogFacility.SKIPPED_TEST_MESSAGE);
		return DriverConstant.STATUS_NO_SCRIPT_FAILURE;	                          	
	}
	
	/***************************************************************************
	 * Process a BlockID (B) input record.
	 * This is called internally by processTest as necessary.
	 */
	protected long processBlockIDRecord(TestRecordHelper trd){

		String text = "";
		try{ text = trd.getTrimmedUnquotedInputRecordToken(1); }
		catch(SAFSNullPointerException npx){;}
		logMessage ("Begin Block '"+ text + "'", null, AbstractLogFacility.GENERIC_MESSAGE);
		return DriverConstant.STATUS_NO_SCRIPT_FAILURE;	                          	
	}
	
	/***************************************************************************
	 * Process an Implied CallScript (unknown record type) input record .
	 * This is called internally by processTest as necessary.
	 */
	protected long processImpliedCallScriptRecord(TestRecordHelper trd){

		// TODO: continue adding functionality here

		return DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;	                          	
	}
	
	public TestRecordHelper initTestRecordData(String record, String separator){
	    // initialize a new TestRecordData object
		String trimdata = record.trim();
	    testRecordData.reinit();
	    testRecordData.setInputRecord(trimdata);
	    testRecordData.setSeparator(separator);
	    try{ 
	    	String defaultMap = (String) getMapsInterface().getDefaultMap().getUniqueID();
	    	testRecordData.setAppMapName(defaultMap);
	    }
	    catch(ClassCastException ccx){testRecordData.setAppMapName("null");}
	    catch(NullPointerException npx){testRecordData.setAppMapName("null");}

	    String rt = null;

	    // extract field 1, the record type
	    try{ 
	    	rt = testRecordData.getTrimmedUnquotedInputRecordToken(0);
	    	if (rt.length()==0)	return testRecordData;				
	    }				
	    catch(IndexOutOfBoundsException inx){ return testRecordData; }
	    catch(SAFSNullPointerException npx) { return testRecordData; }			

    	// start filling in the test record data			
    	testRecordData.setRecordType(rt.toUpperCase()); 
    	testRecordData.setTestLevel(sourceid.getTestLevel());
    	testRecordData.setFilename(sourceid.getSourcePath(driver));
    	testRecordData.setFac((String)logid.getUniqueID());
    	testRecordData.setStatusCode(DriverConstant.STATUS_SCRIPT_NOT_EXECUTED);
    	testRecordData.setStatusInfo("");
    	
    	return testRecordData;
	}
	
	/***************************************************************************
	 *  This is the one that actually opens and loops through our tests records!
	 * @see #getUniqueSourceInfo()
	 */
	public StatusInterface processTest(){
	
		UniqueStringFileInfo sourceinfo = getUniqueSourceInfo();
		InputInterface            input = getInputInterface();
		DebugInterface            debug = getDebugInterface();
		CountersInterface        counts = getCountersInterface();
		MapsInterface              maps = getMapsInterface();
		String                  message = "";
		String               statusInfo = "";
		String			   driverStatus = "";
		
		if (! input.open(sourceinfo)){
			Log.debug("InputProcessor.processTest unable to locate or open '"+ sourceinfo.getSourceName()+"'.");
			logMessage ("Unable to locate or open "+ sourceinfo.getTestLevel() +
			            " TABLE: \""+ sourceinfo.getSourceName() +"\".", null,
			            AbstractLogFacility.FAILED_MESSAGE);			            
			statusCounter.incrementTestFailures();
			counts.incrementAllCounters(counterInfo, counts.STATUS_TEST_FAILURE);
			return statusCounter;
		}
		
		Driver.setIDriver(this);
		
		logMessage(sourceinfo.getTestLevel() +" TABLE: "+ sourceinfo.getSourcePath(driver), 
		           null, AbstractLogFacility.START_DATATABLE);
		
		InputRecordInterface inputrecord = input.nextRecord(sourceinfo);

		String rawdata = null;
		String trimdata = null;
		boolean inputerr = false;
		String rt = null;
		long result = DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
		boolean breakpointActive = false;
		/** delayFlowControl, If the execution will be paused this field will be set to true.*/
		boolean delayFlowControl = false;
		FlowControlInterface  flow = null;
		
mainloop: while (inputrecord.isValid()){
loopbody: {
			result=DriverConstant.STATUS_SCRIPT_NOT_EXECUTED;
    		// In STEP_RETRY_EXECUTION, rawdata is updated in the end of this loop, and shall be reused again
    		if (!driverStatus.equalsIgnoreCase(JavaHook.STEP_RETRY_EXECUTION))
    			rawdata  = inputrecord.getRecordData();
			
			// remove leading whitespace, except for TABS--which may be field delimiter
			trimdata = StringUtils.leftTrimSpace(rawdata);
			if (trimdata.length()==0) break loopbody;
			
			// ignore comment lines and blank lines
			char firstchar = trimdata.charAt(0);
			if ((firstchar=='\'')||
			    (firstchar==';' )){
				break loopbody;
			}

			if ((breakpointActive)||(debug.isRecordDebugEnabled())){
			     breakpointActive=false;

			     // TODO:throw a breakpoint exception here
			}			
			
			// DEBUGMODE: You are now in the INPUT PROCESSOR processing a (BP)BREAKPOINT
			// OR, you are debugging ALL record types.
			
			// process variables and/or expressions
			if (RESOLVE_SKIPPED_RECORDS){
			    trimdata = getVarsInterface().resolveExpressions(
			                                  trimdata, sourceinfo.getDefaultSeparator());
			}else{   // do not resolveExpressions 
				try{ // for SKIPPED records
					rt = StringUtils.getInputToken(trimdata, 0, sourceinfo.getDefaultSeparator());
					rt = StringUtils.getTrimmedUnquotedStr(rt);
					if( ! rt.equalsIgnoreCase(DriverConstant.RECTYPE_S))
					    trimdata = getVarsInterface().resolveExpressions(
                                trimdata, sourceinfo.getDefaultSeparator());
				}catch(SAFSNullPointerException snp){ // ? should not happen ever ?
				    trimdata = getVarsInterface().resolveExpressions(
                            trimdata, sourceinfo.getDefaultSeparator());
				}
			}
	    
		    // initialize a new TestRecordData object with expressions already resolved
			testRecordData = initTestRecordData(trimdata, sourceinfo.getDefaultSeparator());
		    // extract field 1, the record type
		    try{ 
		    	rt = testRecordData.getTrimmedUnquotedInputRecordToken(0);
		    	if (rt.length()==0)	break loopbody;				
		    }				
		    catch(IndexOutOfBoundsException inx){ inputerr=true; }
		    catch(SAFSNullPointerException npx) { inputerr=true; }			

	    	// invalid record or separator -- should never happen?.
	    	if (inputerr){
	    		inputerr=false;
	    		break loopbody;
	    	}
	    	testRecordData.setFileID(sourceinfo.getStringID());
	    	testRecordData.setLineNumber(inputrecord.getRecordNumber());

	    	// set DDVariable for active test table like safsActiveCycle=
	    	getVarsInterface().setValue(activeTableVar, sourceinfo.getFilename());
	    	//Set the current separator to variable "SAFS/Hook/separator"
	    	getVarsInterface().setValue(STAFHelper.SAFS_HOOK_TRD+STAFHelper.SAFS_VAR_SEPARATOR, testRecordData.getSeparator());
		    
			// branch according to the record type
			if ((rt.equals(DriverConstant.RECTYPE_C))  ||
				(rt.equals(DriverConstant.RECTYPE_CW)) ||		
				(rt.equals(DriverConstant.RECTYPE_CF))){

				result = processDriverCommand(testRecordData);
				
			}else 
			// branch according to the record type
			if (rt.equals(DriverConstant.RECTYPE_E)){

				result = processEngineCommand(testRecordData);
				
			}else 
			if ((rt.equals(DriverConstant.RECTYPE_T))  ||
				(rt.equals(DriverConstant.RECTYPE_TW)) ||		
				(rt.equals(DriverConstant.RECTYPE_TF))){
				result = processTestRecord(testRecordData);				
			}else 
			if (rt.equals(DriverConstant.RECTYPE_S)){
				result = processSkippedRecord(testRecordData);				
			}else 
			if (rt.equals(DriverConstant.RECTYPE_B)){
				result = processBlockIDRecord(testRecordData);				
			}else 
			if (rt.equals(DriverConstant.RECTYPE_BP)){
				if (debug.isBreakpointEnabled()) {
					breakpointActive=true;
					logMessage("Breakpoint at line "+ inputrecord.getRecordNumber() +
					           " in "+ sourceinfo.getFilename(), null,
					           AbstractLogFacility.GENERIC_MESSAGE); }
				break loopbody;
			}else {
				result = processImpliedCallScriptRecord(testRecordData);
				statusInfo = testRecordData.getStatusInfo();
				if (result==DriverConstant.STATUS_SCRIPT_NOT_EXECUTED) {
					if (! statusInfo.equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)){
                        message=FAILStrings.convert("unknown_record", "Unknown RECORD TYPE, SCRIPT NAME, or COMMAND "+
                                            "in table "+ testRecordData.getFilename() +
                                            " at line "+ String.valueOf(testRecordData.getLineNumber()),
                                            testRecordData.getFilename(), String.valueOf(testRecordData.getLineNumber()));                                            
                       logMessage(message, testRecordData.getInputRecord(), AbstractLogFacility.WARNING_MESSAGE);
					   result = DriverConstant.STATUS_SCRIPT_WARNING;
					   testRecordData.setStatusCode(DriverConstant.STATUS_SCRIPT_WARNING);
					}
					// is USER ABORT SHUTDOWN
					else{
						// handling down below
					}
				}
			}
			statusInfo = testRecordData.getStatusInfo();

			//////////////////////////////////////////////////////////////
			// evaluate the results here, use flow control as warranted //
			//////////////////////////////////////////////////////////////
			
			flow = getFlowControlInterface(sourceinfo.getTestLevel());

			if(isExitSuite() || isExitCycle()){
				result=DriverConstant.STATUS_EXIT_TABLE_COMMAND;
				statusCounter.incrementGeneralWarnings();
				counts.incrementAllCounters(counterInfo, counts.STATUS_GENERAL_WARNING);
				message = GENStrings.convert("terminating_early", getTestLevel() +
						" terminating prematurely by command.",
						getTestLevel());
				logMessage(message, null, AbstractLogFacility.WARNING_MESSAGE);
			}
			
			if(isSuite && isExitSuite()) setExitSuite(false);
			if(isCycle && isExitCycle()) setExitCycle(false);

			// SCRIPT_NOT_EXECUTED			
			if (result == DriverConstant.STATUS_SCRIPT_NOT_EXECUTED){
				statusCounter.incrementGeneralFailures();
				counts.incrementAllCounters(counterInfo, counts.STATUS_GENERAL_FAILURE);
				if(! statusInfo.equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)){
					if (flow.isScriptNotExecutedBlockValid()){					
						result = locateBlockID(input, sourceinfo,
						                       flow.getScriptNotExecutedBlock());
					}
				}
				break loopbody;				
			}
			
			// EXIT_TABLE_COMMAND
			if (result == DriverConstant.STATUS_EXIT_TABLE_COMMAND){
					
				statusCounter.incrementGeneralPasses();
				counts.incrementAllCounters(counterInfo, counts.STATUS_GENERAL_PASS);					
				
				if(! flow.isExitTableBlockValid()) break mainloop;				
				result = locateBlockID(input, sourceinfo,
				                       flow.getExitTableBlock());	
				break loopbody;	
			}
			
			// INCREMENT appropriate status/counters for TEST RECORDS
			if ((rt.equals(DriverConstant.RECTYPE_T))  ||
			    (rt.equals(DriverConstant.RECTYPE_TW)) ||
			    (rt.equals(DriverConstant.RECTYPE_TF))){
				
				if (result == DriverConstant.STATUS_NO_SCRIPT_FAILURE){
					statusCounter.incrementTestPasses();
					counts.incrementAllCounters(counterInfo, counts.STATUS_TEST_PASS);					
				}
				else if (result == DriverConstant.STATUS_SCRIPT_WARNING){
					statusCounter.incrementTestWarnings();
					counts.incrementAllCounters(counterInfo, counts.STATUS_TEST_WARNING);					
				}
				else if (result == DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE){
					statusCounter.incrementTestFailures();
					counts.incrementAllCounters(counterInfo, counts.STATUS_TEST_FAILURE);					
				}
				else if (result == DriverConstant.STATUS_INVALID_FILE_IO){
					statusCounter.incrementTestIOFailures();
					counts.incrementAllCounters(counterInfo, counts.STATUS_TEST_IO_FAILURE);
				}					
			}
			// INCREMENT appropriate status/counters for OTHER RECORDS
			else {
				
				if (rt.equals(DriverConstant.RECTYPE_S)){
					statusCounter.incrementSkippedRecords();
					counts.incrementAllCounters(counterInfo, counts.STATUS_SKIPPED_RECORD);
				}

				if (result == DriverConstant.STATUS_NO_SCRIPT_FAILURE){
					statusCounter.incrementGeneralPasses();
					counts.incrementAllCounters(counterInfo, counts.STATUS_GENERAL_PASS);					
				}
				else if (result == DriverConstant.STATUS_SCRIPT_WARNING){
					statusCounter.incrementGeneralWarnings();
					counts.incrementAllCounters(counterInfo, counts.STATUS_GENERAL_WARNING);					
				}
				else if (result == DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE){
					statusCounter.incrementGeneralFailures();
					counts.incrementAllCounters(counterInfo, counts.STATUS_GENERAL_FAILURE);					
				}
				else if (result == DriverConstant.STATUS_INVALID_FILE_IO){
					statusCounter.incrementIOFailures();
					counts.incrementAllCounters(counterInfo, counts.STATUS_IO_FAILURE);
				}
				
				//does driver command require branching to BlockID?  (11.15.2005 bolawl RJL)
				else if (result == DriverConstant.STATUS_BRANCH_TO_BLOCKID){
					//get blockid from TRD statusinfo field 
					String blockid = testRecordData.getStatusInfo().trim();
					if (blockid == null || blockid.equals("")) {
						//error retrieving blockid!
						statusCounter.incrementGeneralFailures();
						counts.incrementAllCounters(counterInfo, counts.STATUS_GENERAL_FAILURE);
						result = DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE;
						Log.error("IP: Error retrieving blockid from statusInfo field.");
					}
					else {
						//attempt branching to blockid
						result = locateBlockID(input, sourceinfo, blockid);
						if (result == DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE){
							//error branching to blockid!
							Log.error("IP: Error branching to blockid.");
						}
						else {
							//don't forget to count this testrecord successful
							// locateBlockID() only updates on failure because there wasn't 
							// a previous part of the testrecord that succeeded.
							statusCounter.incrementGeneralPasses();
							counts.incrementAllCounters(counterInfo, counts.STATUS_GENERAL_PASS);
						}
					}
				}

				// handle DriverCommands issuing TEST results
				else if (result == DriverConstant.STATUS_TESTFAILURE_LOGGED){
					statusCounter.incrementTestFailures();
					counts.incrementAllCounters(counterInfo, counts.STATUS_TEST_FAILURE);
					result = DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE;
				}					
				else if (result == DriverConstant.STATUS_TESTSUCCESS_LOGGED){
					statusCounter.incrementTestPasses();
					counts.incrementAllCounters(counterInfo, counts.STATUS_TEST_PASS);
					result = DriverConstant.STATUS_NO_SCRIPT_FAILURE;
				}					
				else if (result == DriverConstant.STATUS_TESTWARNING_LOGGED){
					statusCounter.incrementTestWarnings();
					counts.incrementAllCounters(counterInfo, counts.STATUS_TEST_WARNING);
					result = DriverConstant.STATUS_SCRIPT_WARNING;
				}			
			}

			//////////////////////////////////////////////////////////////
			// transfer to any error recovery/flow control block if set //
			//////////////////////////////////////////////////////////////
			//If the execution will be paused and user retry the previous step, then we should
			//not jump to an other block.
			delayFlowControl = false;
			if(pauseExecution(result)){
				delayFlowControl = true;
			}else{
				result = handleFlowControl(result, flow, input, sourceinfo);
			}
			
			try {
				//set executed record to SAFS variables for org.safs.tools.consoles.SAFSMonitorFrame to watch/edit
				testRecordData.setInputRecord(rawdata); //use unparsed rawdata
				if(testRecordData.getSTAFHelper()==null){
					testRecordData.setSTAFHelper(STAFProcessHelpers.registerHelper(getDriverName()));
				}
				testRecordData.getSTAFHelper().setSAFSTestRecordData(STAFHelper.SAFS_HOOK_TRD, testRecordData);
			}catch (org.safs.SAFSException se){
				Log.debug("InputProcessor.processTest failed to write to SAFSVARS variables:" + se.toString());
			}
			
    	  } // end of loopbody:
			
			if( (result == DriverConstant.STATUS_SCRIPT_NOT_EXECUTED) &&
				(statusInfo.equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD))){
				Log.info("InputProcessor.processTest processing ENGINE SHUTDOWN REQUEST...");
				logMessage(GENStrings.text("user_abort",
						"User-initiated shutdown requested.  Stopping all tests."),
						null, AbstractLogFacility.WARNING_MESSAGE);
				break mainloop;
			}
			
			//set pause for SAFSMonitorFrame to watch/edit
			if( pauseExecution(result)) {
				getVarsInterface().setValue(DriverInterface.DRIVER_CONTROL_VAR, JavaHook.PAUSE_EXECUTION);
			}
			
			// Delay Between Records (Commands)
			if (getMillisBetweenRecords() > 0){
				try{ Thread.sleep(getMillisBetweenRecords());}catch(Exception x){;}
			}
			driverStatus = getVarsInterface().getValue(DriverInterface.DRIVER_CONTROL_VAR);
			
holdloop:	while(! driverStatus.equalsIgnoreCase(JavaHook.RUNNING_EXECUTION)){
				// PAUSE
				if (driverStatus.equalsIgnoreCase(JavaHook.PAUSE_EXECUTION)){
					//check every 350 millis
					try{ Thread.sleep(350);}catch(Exception x){;}
				// STEPPING
				}else if (driverStatus.equalsIgnoreCase(JavaHook.STEPPING_EXECUTION)){
					getVarsInterface().setValue(DriverInterface.DRIVER_CONTROL_VAR, JavaHook.PAUSE_EXECUTION);
				// STEP	
				}else if (driverStatus.equalsIgnoreCase(JavaHook.STEP_EXECUTION)){
					getVarsInterface().setValue(DriverInterface.DRIVER_CONTROL_VAR, JavaHook.STEPPING_EXECUTION);
					break holdloop;
				// SHUTDOWN
				}else if (driverStatus.equalsIgnoreCase(JavaHook.SHUTDOWN_RECORD)){
					Log.info("InputProcessor.processTest processing USER SHUTDOWN REQUEST...");
					logMessage(GENStrings.text("user_abort",
							"User-initiated shutdown requested.  Stopping all tests."),
							null, AbstractLogFacility.WARNING_MESSAGE);
					break mainloop;
				// STEP_RETRY_EXECUTION
				}else if (driverStatus.equalsIgnoreCase(JavaHook.STEP_RETRY_EXECUTION)){
					getVarsInterface().setValue(DriverInterface.DRIVER_CONTROL_VAR, JavaHook.STEPPING_RETRY_EXECUTION);
					break holdloop;
				// STEPPING_RETRY_EXECUTION
				}else if (driverStatus.equalsIgnoreCase(JavaHook.STEPPING_RETRY_EXECUTION)){
					getVarsInterface().setValue(DriverInterface.DRIVER_CONTROL_VAR, JavaHook.PAUSE_EXECUTION);
				}else{
					Log.info("InputProcessor.processTest unknown or invalid SAFS_DRIVER_CONTROL status. ReSet to RUNNING!");
					getVarsInterface().setValue(DriverInterface.DRIVER_CONTROL_VAR, JavaHook.RUNNING_EXECUTION);
					break holdloop;
				}
				driverStatus = getVarsInterface().getValue(DriverInterface.DRIVER_CONTROL_VAR);
			}// end of holdloop:
			
			System.gc();			
			
			if (!driverStatus.equalsIgnoreCase(JavaHook.STEP_RETRY_EXECUTION)){
				//If user doesn't retry the previous step, then handle the delayed flow control.
				if(delayFlowControl) result = handleFlowControl(result, flow, input, sourceinfo);
				// get next input record
				inputrecord = input.nextRecord(sourceinfo);
			}else {
				// update rawdata for retry
				try {
					rawdata =  testRecordData.getSTAFHelper().getVariable(testRecordData.getInstanceName() + STAFHelper.SAFS_VAR_INPUTRECORD);
				}catch(org.safs.SAFSException x){
					Log.debug("InputProcessor.processTest failed to read SAFSVARS variable safs/hook/inputrecord...");
				}
				
				// before retry a component command, run ClearAppMapCache to eliminate cached Object on engine side
				// In case a component with wrong R-Strings still can be found by its cached Object.
				if((testRecordData.getRecordType().equals(DriverConstant.RECTYPE_T))  ||
					(testRecordData.getRecordType().equals(DriverConstant.RECTYPE_TW)) ||
					(testRecordData.getRecordType().equals(DriverConstant.RECTYPE_TF)))
					runDriverCmdClearAppMapCache(testRecordData.getSTAFHelper());
			}
			
		}// end of mainloop:
		
		logMessage(sourceinfo.getTestLevel() +" TABLE: "+ sourceinfo.getSourcePath(driver), 
		           null, AbstractLogFacility.END_DATATABLE);
		input.close(sourceinfo);

		if (!(sourceinfo.getTestLevel().equalsIgnoreCase(DriverConstant.DRIVER_STEP_TESTLEVEL)))
		    driver.getLogsInterface().logStatusInfo(logid, statusCounter, sourceinfo.getSourcePath(driver));

		return statusCounter;
	}	
	
	/**
	 * According to the execution's return_code and the global_variable to decide if we need to pause the execution.<br>
	 * @param result, long, the execution's return code
	 * @return boolean, if to pause the execution.
	 */
	private boolean pauseExecution(long result){
		if(result == DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE)
			return JavaHook.PAUSE_SWITCH_ON.equalsIgnoreCase(getVarsInterface().getValue(DriverInterface.DRIVER_CONTROL_POF_VAR));
		else if(result == DriverConstant.STATUS_SCRIPT_WARNING)
			return JavaHook.PAUSE_SWITCH_ON.equalsIgnoreCase(getVarsInterface().getValue(DriverInterface.DRIVER_CONTROL_POW_VAR));
		else
			return false;
	}
	
	/**
	 * According to the return code of last execution and the setting of "script handler",
	 * transfer to any error recovery/flow control block.<br>
	 * @param result, long, the return code of last execution
	 * @param flow
	 * @param input
	 * @param sourceinfo
	 * @return	long, the return code of execution of "block shifting" if "block shifting" has been performed.
	 *                the input parameter return code.
	 *                
	 * @see #locateBlockID(InputInterface, UniqueSourceInterface, String)
	 */
	private long handleFlowControl(long result,
			                       FlowControlInterface flow,
			                       InputInterface input,
			                       UniqueStringFileInfo sourceinfo){
		if ((result == DriverConstant.STATUS_NO_SCRIPT_FAILURE)
				&& (flow.isNoScriptFailureBlockValid())) {
			String tmpstr = flow.getNoScriptFailureBlock(); // this is a one-shot
			flow.setNoScriptFailureBlock(""); // this is a one-shot
			return locateBlockID(input, sourceinfo, tmpstr);
		} else if ((result == DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE)
				&& (flow.isScriptFailureBlockValid())) {
			return locateBlockID(input, sourceinfo,
					flow.getScriptFailureBlock());
		} else if ((result == DriverConstant.STATUS_SCRIPT_WARNING)
				&& (flow.isScriptWarningBlockValid())) {
			return locateBlockID(input, sourceinfo,
					flow.getScriptWarningBlock());
		} else if ((result == DriverConstant.STATUS_INVALID_FILE_IO)
				&& (flow.isIOFailureBlockValid())) {
			return locateBlockID(input, sourceinfo, flow.getIOFailureBlock());
		}
		return result;
	}
	
	//Run driver command ClearAppMapCache, which is to clear the internal cache on engine side
	private void runDriverCmdClearAppMapCache(STAFHelper staf){
		TestRecordHelper clsCache = new TestRecordHelper();
		clsCache.setSTAFHelper(staf);
		clsCache.setInstanceName(STAFHelper.SAFS_HOOK_TRD);
		clsCache.setSeparator(",");
		clsCache.setInputRecord("C, ClearAppMapCache");
		clsCache.setStatusCode(DriverConstant.STATUS_SCRIPT_NOT_EXECUTED);
		clsCache.setStatusInfo("");
		processDriverCommand(clsCache);		
	}

	/**
	 * Attempts to return an interface to the core framework through the existing DriverInterface.
	 * @see DriverInterface#getCoreInterface()
	 */
	public CoreInterface getCoreInterface() { 
		return driver.getCoreInterface();
	}

	
}

