/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.safs.ComponentFunction;
import org.safs.Log;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.TestRecordHelper;
import org.safs.android.remotecontrol.SAFSMessage;
import org.safs.android.remotecontrol.SAFSRemoteControl;
import org.safs.logging.AbstractLogFacility;
import org.safs.sockets.RemoteException;
import org.safs.sockets.ShutdownInvocationException;
import org.safs.text.GENStrings;
import org.safs.text.ResourceMessageInfo;

import com.jayway.android.robotium.remotecontrol.solo.RemoteResults;

/**
 * Superclass of all Droid ComponentFunction processors.
 * 
 * @author canagl
 */
public class CFComponentFunctions extends ComponentFunction {

	/** simple class cast of existing testRecordData */
	protected DTestRecordHelper droiddata = null; //cast of testRecordData
	protected Properties props = null;
	protected SAFSRemoteControl control = null;
	protected STAFHelper staf = null;
	protected boolean useExplicitTimeout = false;
	
	/**
	 * 
	 */
	public CFComponentFunctions() {
		super();		
	}

	/**
	 * Transfers all DTestRecordHelper data into the receiving Processor.
	 * This includes droiddata, utils, staf, props, control, action, windowName, compName, and mapname.
	 */
	public void setTestRecordData(TestRecordHelper data){
		super.setTestRecordData(data);
		droiddata = (DTestRecordHelper) data;
		utils = droiddata.getDDGUtils();
		staf = droiddata.getSTAFHelper();
		props = droiddata.getKeywordProperties();
		control = droiddata.controller;
		action = droiddata.getCommand();
		try{windowName = droiddata.getWindowName();}catch(Exception x){windowName = null;}
		try{compName = droiddata.getCompName();}catch(Exception x){compName = null;}
		try{mapname = droiddata.getAppMapName();}catch(Exception x){mapname = null;}
	}
	
	/**
	 * All action props should have already been completed during {@link #setTestRecordData(org.safs.TestRecordHelper)} 
	 * and {@link DTestStepProcessor#interpretFields()}
	 * <p>
	 * Sets KEY_TARGET to {@link SAFSMessage#target_safs_comprouting} in the Properties to be remotely processed.
	 * <p>  
	 * 
	 * @throws TimeoutException if the Ready, Running, or Results signals timeout.
	 * @throws IllegalThreadStateException if sockets communications failed.
	 * @throws ShutdownInvocationException if sockets communication signals a shutdown has commenced.
	 * @throws RemoteException if the remote client has issued an Exception.
	 */
	protected void processCommand()throws IllegalThreadStateException,
    									  RemoteException,
    									  TimeoutException, 
    									  ShutdownInvocationException{
		
		/*
		 * Intercept possible Solo commands here like "GoBack"
		 */
		if (action.equalsIgnoreCase(SAFSMessage.cmd_goback)){
			Log.info(getClass().getSimpleName()+ " attempting Solo.goBack()...");
			boolean success = droiddata.safsworker.goBack();
			props = droiddata.safsworker._last_remote_result;
			droiddata.setKeywordProperties(props);
			RemoteResults results = new RemoteResults(props);
			droiddata = DUtilities.captureRemoteResultsProperties(results, droiddata);
			Log.info(getClass().getSimpleName()+ " " + action +" returned rc: "+ droiddata.getStatusCode()+", info: "+ droiddata.getStatusInfo());
			setRecordProcessed(true);
			if(success){
				issuePassedSuccess("");
			}else{
				issueErrorPerformingAction("");
			}
			return;
		}
		//Firstly, set the target which decides what processor will handle command at device side.
		props.setProperty(SAFSMessage.KEY_TARGET, SAFSMessage.target_safs_comprouting);
				
		int timeout = 30;
		useExplicitTimeout = false;
		processProperties(timeout);
	}
	
	/**
	 * process the completed remote control action properties.
	 * This method is called internally by {@link #processCommand()}. 
	 * After returning from remote processing the routine updates the {@link #droiddata} 
	 * via {@link DUtilities#captureRemoteResultsProperties(RemoteResults, DTestRecordHelper)}. 
	 * This routine then calls {@link #processResults(RemoteResults)} if results.isRemoteResult() is true.
	 * @throws TimeoutException if the Ready, Running, or Results signals timeout.
	 * @throws IllegalThreadStateException if sockets communications failed.
	 * @throws ShutdownInvocationException if sockets communication signals a shutdown has commenced.
	 * @throws RemoteException if the remote client has issued an Exception.
	 * @see DUtilities#captureRemoteResultsProperties(RemoteResults, DTestRecordHelper)
	 */
	protected void processProperties(int param_timeout)throws IllegalThreadStateException,
	                                         RemoteException,
	                                         TimeoutException, 
	                                         ShutdownInvocationException{
		props.setProperty(SAFSMessage.PARAM_TIMEOUT, String.valueOf(param_timeout));
		droiddata.setKeywordProperties(props);
		int secsCommandTimeout = Math.max(DTestStepProcessor.secsWaitForWindow, 
				                          DTestStepProcessor.secsWaitForComponent) +
				                          param_timeout;
		props = control.performRemotePropsCommand(props, 
                droiddata.getReadyTimeout(), 
                droiddata.getRunningTimeout(), 
                secsCommandTimeout);
		RemoteResults results = new RemoteResults(props);
		droiddata = DUtilities.captureRemoteResultsProperties(results, droiddata);
		Log.info(getClass().getSimpleName()+ " " + action +" returned rc: "+ droiddata.getStatusCode()+", info: "+ droiddata.getStatusInfo());
		
		if(results.isRemoteResult()) { 
			
			processResults(results);
			
		}else if(StatusCodes.SCRIPT_NOT_EXECUTED==droiddata.getStatusCode()){
			// driver will log warning...
			setRecordProcessed(false);
		}else{ //object not found? what?		
			logResourceMessageFailure();
		}
	}
	
	protected CFComponentFunctions getProcessorInstance(String target){
		CFComponentFunctions processor = null;
		try{
			if(processorMap.containsKey(target)){
				processor = (CFComponentFunctions) processorMap.get(target);
			}else{
				processor = (CFComponentFunctions) Class.forName(droiddata.getCompInstancePath()+"CF"+ target +"Functions").newInstance();
				processor.setLogUtilities(getLogUtilities());
				processorMap.put(target, processor);
			}
		}catch(ClassCastException x){
			Log.debug(x.getClass().getSimpleName()+": "+ x.getMessage(), x);
		}catch(ClassNotFoundException x){			
			Log.debug(x.getClass().getSimpleName()+": "+ x.getMessage(), x);			
		}catch(InstantiationException x){			
			Log.debug(x.getClass().getSimpleName()+": "+ x.getMessage(), x);			
		}catch(IllegalAccessException x){			
			Log.debug(x.getClass().getSimpleName()+": "+ x.getMessage(), x);			
		}
    	return processor;
	}
	
	/**
	 * called internally by {@link #processProperties(int)} AFTER the remote execution has completed.
	 * The routine routes to other CF Function libraries based on the KEY_TARGET returned by the  
	 * remote execution.
	 */
	protected void processResults(RemoteResults results){
		
		CFComponentFunctions processor = null;
		try{
			String target = props.getProperty(SAFSMessage.KEY_TARGET);
			if(target == null) {			
				issueActionFailure("Contact SAFS Development. Remote results did not provide required KEY_TARGET.");
				throw new InstantiationException("Results Properties missing KEY_TARGET");
			}
			processor = getProcessorInstance(target);
	    	processor.setTestRecordData(droiddata);
	    	processor.setParams(params);
	    	processor.processResults(results);
	    	
		}catch(InstantiationException x){			
			Log.debug(x.getClass().getSimpleName()+": "+ x.getMessage(), x);			
		}
	}
			
	
	/**
	 * @param msgType -- int message type identifier constant as defined in 
	 * {@link AbstractLogFacility}.  
	 * @return true if we did find and handle a ResoureMessageInfo result.
	 */
	protected boolean processResourceMessageInfoResults(int msgType){
		ResourceMessageInfo msg = droiddata.getMessage();
		if(msg == null) return false;
		String message = msg.getMessage();
		if(message == null) return false;
		ResourceMessageInfo det = droiddata.getDetailMessage();
		String detail = (det == null) ? null: det.getMessage();
		if(detail == null){
			log.logMessage(droiddata.getFac(), message, msgType);
		}else{
			log.logMessage(droiddata.getFac(), message, msgType, detail);
		}
		return true;
	}
	
	/**
	 * Log a FAILED_MESSAGE using whatever ResourceMessageInfo data is returned in {@link #droiddata}.
	 * Calls {@link #processResourceMessageInfoResults(int)} to attempt the logging.
	 * If there was no ResourceMessageInfo, we still log a generic failed message via {@link #issueActionFailure(String)}.
	 */
	protected void logResourceMessageFailure(){
		if(processResourceMessageInfoResults(FAILED_MESSAGE)) 
			return;		
		//log a generic failure if no ResourceMessageInfo was provided
		int status = droiddata.getStatusCode();
		issueActionFailure("SAFS StatusCode: "+ String.valueOf(status) +
				           "SAFS StatusInfo: "+ droiddata.getStatusInfo());
		droiddata.setStatusCode(status);
	}

	/**
	 * Set generic success message to TestRecordData<br>
	 * After calling this method, we need to call {@link #processResourceMessageInfoResults(int)} to<br>
	 * write message to Log.<br>
	 * 
	 * @param detailMessage	ResourceMessageInfo the detail success message to set to TestRecordData
	 * @see #processResourceMessageInfoResults(int)
	 */
	protected void setSuccessResourceMessageInfo(ResourceMessageInfo detailMessage){
			List<String> messageParams = new ArrayList<String>();
			messageParams.add(windowName);
			messageParams.add(compName);
			messageParams.add(action);
			ResourceMessageInfo message = new ResourceMessageInfo(ResourceMessageInfo.BUNDLENAME_GENERICTEXT,
					                                              GENStrings.SUCCESS_3,messageParams);
			
			droiddata.setMessage(message);
			
			if(detailMessage!=null){
				droiddata.setDetailMessage(detailMessage);
			}
	}
}
