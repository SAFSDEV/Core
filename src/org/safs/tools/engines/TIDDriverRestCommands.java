/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 */
package org.safs.tools.engines;

import java.util.Iterator;

import org.safs.DriverCommand;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.TestRecordHelper;
import org.safs.logging.AbstractLogFacility;
import org.safs.logging.LogUtilities;
import org.safs.model.commands.DDDriverRestCommands;
import org.safs.text.FAILStrings;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverInterface;

public class TIDDriverRestCommands extends GenericEngine{
	/** "SAFS/REST" */
	public static final String ENGINE_NAME = "SAFS/TIDDriverRestCommands";
	/** "TIDDRIVERREST: " */
	private static final String DEBUG_PREFIX = "TIDDRIVERREST: ";
	
    /** The special Processor for handling Driver Command keywords.*/
    protected DriverCommand dc = null;
	
	/**
	 * Constructor: It will set the service-name.<br>
	 */
	public TIDDriverRestCommands() {
		super();
		servicename = ENGINE_NAME;
	}

	/**
	 * PREFERRED Constructor: Constructs the instance and calls launchInterface to initialize.
	 */
	public TIDDriverRestCommands(DriverInterface driver) {
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
			if(log==null){
				log = new LogUtilities(this.staf);
			}
			dc = new RESTDriverCommand();
			dc.setLogUtilities(log);

		}catch(Exception x){
			IndependantLog.error(DEBUG_PREFIX+" requires a valid DriverInterface object for initialization!\n"+ x.getMessage());
		}
	}
	
	public long processRecord (TestRecordHelper testRecordData){
		this.testRecordData = testRecordData;
		
		boolean resetTRD = false;
		if (testRecordData.getSTAFHelper()==null){
		    testRecordData.setSTAFHelper(staf);
		    resetTRD = true;
		}
//The following code may not be necessary.
//		if(dc==null){
//			dc = new RESTDriverCommand();
//		}
//		if(dc.getLogUtilities()==null){
//			if(staf==null){
//				IndependantLog.debug(DEBUG_PREFIX+"STAFHelper is null, try to get the STAFHelper from the test record.");
//				staf = testRecordData.getSTAFHelper();
//			}
//			if(staf!=null){
//				log = new LogUtilities(staf);
//				dc.setLogUtilities(log);
//			}else{
//				IndependantLog.warn(DEBUG_PREFIX+"STAFHelper is null, cannot initialize the IndependantLog Utilities!");
//			}
//		}
		
		dc.setTestRecordData(testRecordData);
		dc.process();
		
		if(resetTRD) testRecordData.setSTAFHelper(null);
		return testRecordData.getStatusCode();
	}
	
	/**
	 * Internal Driver Command Processor.
	 */
	protected class RESTDriverCommand extends DriverCommand{
		protected String mapname;
		
		public RESTDriverCommand(){
			super();
		}
		
		protected void localProcess(){
			mapname = testRecordData.getAppMapName();
		}
		
		protected void commandProcess() {
			if(DDDriverRestCommands.RESTDELETERESPONSE_KEYWORD.equalsIgnoreCase(command)){
				deleteResponse();
			}else if(DDDriverRestCommands.RESTDELETERESPONSESTORE_KEYWORD.equalsIgnoreCase(command)){
				deleteResponseStore();
			}else if(DDDriverRestCommands.RESTSTORERESPONSE_KEYWORD.equalsIgnoreCase(command)){
				storeResponse();
			}
		}
		
		private void deleteResponse(){
			if (params.size() < 1) {
				issueParameterCountFailure();
				return;
			}
			String debugmsg = StringUtils.debugmsg(false);
			String message = command;
			String description = command;
			Iterator<?> iter = params.iterator();
			IndependantLog.debug(debugmsg+"processing"+command+"with parameters "+params);
			
			String variablePrefix = (String) iter.next();
			if(!StringUtils.isValid(variablePrefix)){
				message = failedText.convert("bad_param", "Invalid parameter value for variablePrefix", "variablePrefix");
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
				setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
				return;
			}
			
			try { 
				//TODO implement the details
				logMessage( message, description, AbstractLogFacility.STATUS_REPORT_TEST_PASSES);
				setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
				return;
			}catch(Exception e){
				String exceptionMsg = StringUtils.debugmsg(e);
				message = FAILStrings.convert(FAILStrings.GENERIC_ERROR, 
						"*** ERROR *** "+exceptionMsg, exceptionMsg);
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
				setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
				return;
			}
		}
		private void deleteResponseStore(){
			String debugmsg = StringUtils.debugmsg(false);
			String message = command;
			String description = command;
			IndependantLog.debug(debugmsg+"processing"+command+"with parameters "+params);
			
			try { 
				//TODO implement the details
				logMessage( message, description, AbstractLogFacility.STATUS_REPORT_TEST_PASSES);
				setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
				return;
			}catch(Exception e){
				String exceptionMsg = StringUtils.debugmsg(e);
				message = FAILStrings.convert(FAILStrings.GENERIC_ERROR, 
						"*** ERROR *** "+exceptionMsg, exceptionMsg);
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
				setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
				return;
			}
		}
		private void storeResponse(){
			if (params.size() < 2) {
				issueParameterCountFailure();
				return;
			}
			String debugmsg = StringUtils.debugmsg(false);
			String message = null;
			String description = null;
			Iterator<?> iter = params.iterator();
			IndependantLog.debug(debugmsg+"processing"+command+"with parameters "+params);
			
			String responseID = (String) iter.next();
			if(!StringUtils.isValid(responseID)){
				message = failedText.convert("bad_param", "Invalid parameter value for responseID", "responseID");
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
				setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
				return;
			}
			
			String variablePrefix = (String) iter.next();
			if(!StringUtils.isValid(variablePrefix)){
				message = failedText.convert("bad_param", "Invalid parameter value for variablePrefix", "variablePrefix ");
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
				setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
				return;
			}
			
			try { 
				//TODO implement the details
				logMessage( message, description, AbstractLogFacility.STATUS_REPORT_TEST_PASSES);
				setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
				return;
			}catch(Exception e){
				String exceptionMsg = StringUtils.debugmsg(e);
				message = FAILStrings.convert(FAILStrings.GENERIC_ERROR, 
						"*** ERROR *** "+exceptionMsg, exceptionMsg);
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
				setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
				return;
			}
		}
	}


}