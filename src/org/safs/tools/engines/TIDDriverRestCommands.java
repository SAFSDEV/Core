/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 
 * NOV 23, 2016		(SBJLWA)	Initial release.
 * NOV 25, 2016		(SBJLWA)	Implemented RestStoreResponse, RestDeleteResponse and RestDeleteResponseStore.
 * DEC 01, 2016		(SBJLWA)	Implemented RestCleanResponseMap.
 *                              Modified code to make it easier to maintain.
 */
package org.safs.tools.engines;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.safs.DriverCommand;
import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.TestRecordHelper;
import org.safs.logging.LogUtilities;
import org.safs.model.commands.DDDriverRestCommands;
import org.safs.rest.service.Response;
import org.safs.text.FAILKEYS;
import org.safs.text.FAILStrings;
import org.safs.text.GENKEYS;
import org.safs.tools.drivers.DriverInterface;

/**
 * The class to handle the REST driver commands.
 * 
 */
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
	
	private static final Map<String, String> variablePrefixToResponse = new HashMap<String, String>();
	
	/**
	 * Internal Driver Command Processor.
	 */
	protected class RESTDriverCommand extends DriverCommand{
		protected String mapname;
		
		public RESTDriverCommand(){
			super();
		}
		
		protected void init() throws SAFSException{
			super.init();
			mapname = testRecordData.getAppMapName();
		}
		
		protected void commandProcess() {
			String debugmsg = StringUtils.debugmsg(false);
			
			try{
				IndependantLog.debug(debugmsg+"processing"+command+"with parameters "+params);
				
				if(DDDriverRestCommands.RESTDELETERESPONSE_KEYWORD.equalsIgnoreCase(command)){
					deleteResponse();
				}else if(DDDriverRestCommands.RESTDELETERESPONSESTORE_KEYWORD.equalsIgnoreCase(command)){
					deleteResponseStore();
				}else if(DDDriverRestCommands.RESTSTORERESPONSE_KEYWORD.equalsIgnoreCase(command)){
					storeResponse();
				}else if(DDDriverRestCommands.RESTCLEANRESPONSEMAP_KEYWORD.equalsIgnoreCase(command)){
					cleanResponseMap();
				}else if(DDDriverRestCommands.RESTHEADERSLOAD_KEYWORD.equalsIgnoreCase(command)){
					
				}else if(DDDriverRestCommands.RESTVERIFYRESPONSE_KEYWORD.equalsIgnoreCase(command)){
					
				}else if(DDDriverRestCommands.RESTVERIFYRESPONSECONTAINS_KEYWORD.equalsIgnoreCase(command)){
					
				}else{
					IndependantLog.debug(debugmsg+command+" is not suppported in this processor, so it was not handled yet.");
				}
				
			}catch(Exception e){
				String exceptionMsg = StringUtils.debugmsg(e);
				String message = FAILStrings.convert(FAILStrings.GENERIC_ERROR, 
						"*** ERROR *** "+exceptionMsg, exceptionMsg);
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
				setTRDStatus(testRecordData, StatusCodes.GENERAL_SCRIPT_FAILURE);
			}

		}
		
		private void cleanResponseMap(){
			String debugmsg = StringUtils.debugmsg(false);
			String message = null;
			String description = null;
			String responseID = null;
			
			if(iterator.hasNext()){
				responseID = iterator.next();
			}
			
			int status = StatusCodes.NO_SCRIPT_FAILURE;

			if(responseID==null){
				IndependantLog.warn(debugmsg+"Missing parameter 'responseID! CAUTION, ALL Responses will be removed from internal Map!'");
				TIDComponent.deleteRestResponseStore();
				description = passedText.text(GENKEYS.OBJECTS_REMOVED_FROM_MAP, "Objects have been removed from cache map.");
				
			}else{
				Response response = TIDComponent.deleteRestResponse(responseID);
				if(response!=null){
					description = passedText.convert(GENKEYS.ID_OBJECT_REMOVED_FROM_MAP, 
							"Object identified by '"+responseID+"' has been removed from cache map.", responseID);
				}else{
					status = StatusCodes.GENERAL_SCRIPT_FAILURE;
					message = failedText.convert(FAILKEYS.NO_SUCCESS_1, command+" was not successful.", command);
					description = failedText.convert(FAILKEYS.ID_NOT_FOUND_1, "Object identified by "+responseID+" was not found", responseID);
				}
			}
			
			setAtEndOfProcess(status, message, description);

		}
		
		private void deleteResponse(){
			if (params.size() < 1) {
				issueParameterCountFailure();
				return;
			}
			String message = null;
			String description = null;
			
			//TODO do we need to add "responseID" as the first parameter, if we want to delete it also from the internal map.
//			String responseID = null;
//			responseID = iterator.next();
			
			String variablePrefix = iterator.next();
			if(!StringUtils.isValid(variablePrefix)){
				issueParameterValueFailure("variablePrefix");
				return;
			}
			
			int status = StatusCodes.NO_SCRIPT_FAILURE;

			//TODO do we need to delete the Response from the internal map???
//			Response response = TIDComponent.deleteRestResponse(responseID);
//
//			if(response==null){
//				message = failedText.convert(FAILKEYS.NO_SUCCESS_1, command+" was not successful.", command);
//				description = failedText.convert(FAILKEYS.ID_NOT_FOUND_1, "Object identified by "+responseID+" was not found.", responseID);
//				status = StatusCodes.GENERAL_SCRIPT_FAILURE;
//			}else{
//				if(Response.delete(this, variablePrefix)){
//					description = passedText.convert(GENKEYS.PREFIX_VARS_DELETE_1, 
//							"Variables prefixed with '"+variablePrefix+"' have been delete.", variablePrefix);
//				}else{
//					message = failedText.convert(FAILKEYS.NO_SUCCESS_1, command+" was not successful.", command);
//					description = failedText.convert(FAILKEYS.COULD_NOT_DELETE_PREFIX_VARS_1, 
//							"Could not delete one or more variable values prefixed with '"+variablePrefix+"'.", variablePrefix);
//					status = StatusCodes.GENERAL_SCRIPT_FAILURE;
//				}
//			}

			if(variablePrefixToResponse.containsKey(variablePrefix)){
				if(Response.delete(this, variablePrefix)){
					description = passedText.convert(GENKEYS.PREFIX_VARS_DELETE_1, 
							"Variables prefixed with '"+variablePrefix+"' have been deleted.", variablePrefix);
					variablePrefixToResponse.remove(variablePrefix);
				}else{
					message = failedText.convert(FAILKEYS.NO_SUCCESS_1, command+" was not successful.", command);
					description = failedText.convert(FAILKEYS.COULD_NOT_DELETE_PREFIX_VARS_1, 
							"Could not delete one or more variable values prefixed with '"+variablePrefix+"'.", variablePrefix);
					status = StatusCodes.GENERAL_SCRIPT_FAILURE;
				}
			}else{
				message = failedText.convert(FAILKEYS.NO_SUCCESS_1, command+" was not successful.", command);
				description = failedText.convert(FAILKEYS.ID_NOT_FOUND_1, 
						"Object identified by "+variablePrefix+" was not found.", variablePrefix);
				status = StatusCodes.GENERAL_SCRIPT_FAILURE;
			}

			setAtEndOfProcess(status, message, description);
		}
		
		private void deleteResponseStore(){
			String message = null;
			String description = null;
			
			boolean success = true;
			Set<String> variablePrefixes = variablePrefixToResponse.keySet();
			StringBuffer successVariablePrefix = new StringBuffer();
			StringBuffer failVariablePrefix = new StringBuffer();

			for(String variablePrefix:variablePrefixes){
				success = Response.delete(this, variablePrefix);
				if(success){
					successVariablePrefix.append(variablePrefix+" ");
					variablePrefixToResponse.remove(variablePrefix);
				}else{
					failVariablePrefix.append(variablePrefix+" ");
				}
			}

			String param = null;
			int status = StatusCodes.NO_SCRIPT_FAILURE;
			if(success){
				param = successVariablePrefix.toString().trim();
				message = passedText.convert(GENKEYS.SUCCESS_1, command+" successful.", command);
				description = passedText.convert(GENKEYS.PREFIX_VARS_DELETE_1, 
						"Variables prefixed with '"+param+"' have been delete.", param);
			}else{
				param = failVariablePrefix.toString().trim();
				message = failedText.convert(FAILKEYS.NO_SUCCESS_1, command+" was not successful.", command);
				description = failedText.convert(FAILKEYS.COULD_NOT_DELETE_PREFIX_VARS_1, 
						"Could not delete one or more variable values prefixed with '"+param+"'.", param);
				status = StatusCodes.GENERAL_SCRIPT_FAILURE;
			}
				
			setAtEndOfProcess(status, message, description);
		}
		
		private void storeResponse(){
			if (params.size() < 2) {
				issueParameterCountFailure();
				return;
			}
			boolean saveRequest = false;
			String message = null;
			String description = null;
			
			String responseID = iterator.next();
			if(!StringUtils.isValid(responseID)){
				issueParameterValueFailure("responseID");
				return;
			}
			
			String variablePrefix = iterator.next();
			if(!StringUtils.isValid(variablePrefix)){
				issueParameterValueFailure("variablePrefix");
				return;
			}
			
			if(iterator.hasNext()){
				saveRequest = Boolean.parseBoolean(iterator.next());
			}
			
			Response response = TIDComponent.getRestResponse(responseID);
			int status = StatusCodes.NO_SCRIPT_FAILURE;

			if(response==null){
				message = failedText.convert(FAILKEYS.NO_SUCCESS_1, command+" was not successful.", command);
				description = failedText.convert(FAILKEYS.ID_NOT_FOUND_1, "Object identified by "+responseID+" was not found.", responseID);
				status = StatusCodes.GENERAL_SCRIPT_FAILURE;
			}else{
				if(response.save(this, variablePrefix, saveRequest)){
					description = passedText.convert(GENKEYS.ID_OBJECT_SAVED_TO_VARIABLE_PREFIX_2, 
							"Object identified by '"+responseID+"' has been saved to variables prefixed with '"+variablePrefix+"'", responseID, variablePrefix);
					variablePrefixToResponse.put(variablePrefix, responseID);
				}else{
					message = failedText.convert(FAILKEYS.NO_SUCCESS_1, command+" was not successful.", command);
					description = failedText.convert(FAILKEYS.COULD_NOT_SET_PREFIX_VARS_1, 
							"Could not delete one or more variable values prefixed with '"+variablePrefix+"'.", variablePrefix);
					status = StatusCodes.GENERAL_SCRIPT_FAILURE;
				}
			}

			setAtEndOfProcess(status, message, description);
		}
	}
}