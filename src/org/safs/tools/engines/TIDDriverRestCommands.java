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
import java.util.HashSet;
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
import org.safs.persist.Persistor;
import org.safs.persist.Persistor.FileType;
import org.safs.persist.Persistor.Type;
import org.safs.persist.PersistorFactory;
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
	
	private static final Map<String, Set<Persistor>> responseIdToPersistorSet = new HashMap<String, Set<Persistor>>();
	
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
			String responseID = iterator.next();
			if(!StringUtils.isValid(responseID)){
				issueParameterValueFailure("responseID");
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
			

			Set<Persistor> persistorSet = responseIdToPersistorSet.get(responseID);
			
			if(persistorSet!=null){
				boolean success = true;
				StringBuilder sb = new StringBuilder();
				for(Persistor p:persistorSet){
					try{
						p.unpersist();
					}catch(SAFSException e){
						success = false;
						sb.append(e.getMessage()+"\n");
					}
				}
				
				if(success){
					//TODO use more appropriate success message.
					description = passedText.convert(GENKEYS.PREFIX_VARS_DELETE_1, 
							"Variables prefixed with '"+responseID+"' have been deleted.", responseID);
					responseIdToPersistorSet.remove(responseID);
				}else{
					message = failedText.convert(FAILKEYS.NO_SUCCESS_1, command+" was not successful.", command);
					//TODO use more appropriate failure message.
					description = sb.toString();
//					description = failedText.convert(FAILKEYS.COULD_NOT_DELETE_PREFIX_VARS_1, 
//							"Could not delete one or more variable values prefixed with '"+responseID+"'.", responseID);
					status = StatusCodes.GENERAL_SCRIPT_FAILURE;
				}
			}else{
				message = failedText.convert(FAILKEYS.NO_SUCCESS_1, command+" was not successful.", command);
				description = failedText.convert(FAILKEYS.ID_NOT_FOUND_1, 
						"Object identified by "+responseID+" was not found.", responseID);
				status = StatusCodes.GENERAL_SCRIPT_FAILURE;
			}

			setAtEndOfProcess(status, message, description);
		}
		
		private void deleteResponseStore(){
			String message = null;
			String description = null;
			
			boolean success = true;
			boolean persistorSuccess = true;
			Set<String> responsIDSet = responseIdToPersistorSet.keySet();
			Set<Persistor> persistorSet = null;
			StringBuffer successResponseIDs = new StringBuffer();
			StringBuffer failResponseIDs = new StringBuffer();

			for(String responseID:responsIDSet){
				persistorSet = responseIdToPersistorSet.get(responseID);
				
				persistorSuccess = true;
				StringBuilder sb = new StringBuilder();
				for(Persistor p:persistorSet){
					try{
						p.unpersist();
					}catch(SAFSException e){
						persistorSuccess = false;
						sb.append(e.getMessage()+"\n");
					}
				}
				
				if(persistorSuccess){
					successResponseIDs.append(responseID+" ");
					responseIdToPersistorSet.remove(responseID);
				}else{
					failResponseIDs.append("==== ERROR FOR RESPONSE ID: "+responseID+"\n"+sb.toString());
					success = false;
				}
			}

			int status = StatusCodes.NO_SCRIPT_FAILURE;
			if(success){
				message = passedText.convert(GENKEYS.SUCCESS_1, command+" successful.", command);
				//TODO use more appropriate success message.
				description = successResponseIDs.toString();
			}else{
				message = failedText.convert(FAILKEYS.NO_SUCCESS_1, command+" was not successful.", command);
				//TODO use more appropriate failure message.
				description = failResponseIDs.toString();
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
			boolean persistFile = false;
			FileType fileType = FileType.JSON;
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
			if(iterator.hasNext()){
				persistFile = Boolean.parseBoolean(iterator.next());
			}
			if(iterator.hasNext()){
				fileType = FileType.get(iterator.next());
			}
			
			Response response = TIDComponent.getRestResponse(responseID);
			int status = StatusCodes.NO_SCRIPT_FAILURE;

			if(response==null){
				message = failedText.convert(FAILKEYS.NO_SUCCESS_1, command+" was not successful.", command);
				description = failedText.convert(FAILKEYS.ID_NOT_FOUND_1, "Object identified by "+responseID+" was not found.", responseID);
				status = StatusCodes.GENERAL_SCRIPT_FAILURE;
			}else{
				response.setEnabled(true);
				response.get_request().setEnabled(saveRequest);
				Persistor persistor = null;
				
				if(persistFile){
					persistor = PersistorFactory.create(Type.FILE, fileType, this, variablePrefix); 
				}else{
					persistor = PersistorFactory.create(Type.VARIABLE, null, this, variablePrefix);
				}
				
				try{
					persistor.persist(response);
					
					synchronized(responseIdToPersistorSet){
						Set<Persistor> persistorSet = responseIdToPersistorSet.get(responseID);
						if(persistorSet==null){
							persistorSet = new HashSet<Persistor>();
							responseIdToPersistorSet.put(responseID, persistorSet);
						}
						if(!persistorSet.add(persistor)){
							IndependantLog.warn(StringUtils.debugmsg(false)+"Failed to add Persistor '"+persistor+"' into internal cache.");
						}
					}
					
					//TODO use more appropriate success message.
					description = passedText.convert(GENKEYS.ID_OBJECT_SAVED_TO_VARIABLE_PREFIX_2, 
							"Object identified by '"+responseID+"' has been saved to variables prefixed with '"+variablePrefix+"'", responseID, variablePrefix);
					
				}catch(SAFSException se){
					IndependantLog.error(StringUtils.debugmsg(false)+" Failed, met "+StringUtils.debugmsg(se));
					
					//TODO use more appropriate failure message.
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