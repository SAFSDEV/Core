/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.android;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.safs.Log;
import org.safs.StatusCodes;
import org.safs.android.remotecontrol.SAFSMessage;
import org.safs.text.GENStrings;
import org.safs.text.ResourceMessageInfo;

import com.jayway.android.robotium.remotecontrol.solo.RemoteResults;

/**
 * Processor handling SAFS Droid TimePicker functions.
 * 
 * History:
 * 	DEC, 10, 2012	(Lei Wang)	Initial implementation
 */
public class CFTimePickerFunctions extends CFComponentFunctions {
	private static String tag = "CFTimePickerFunctions: ";

	/**
	 * Default constructor. 
	 */
	public CFTimePickerFunctions() {
		super();
	}

	@Override
	protected void processResults(RemoteResults results){
		
		if(SAFSMessage.cf_timepicker_gettime.equalsIgnoreCase(action)||
		   SAFSMessage.cf_timepicker_settime.equalsIgnoreCase(action))
		{
			_checkCommandsResults(results);
		}else{
			CFComponentFunctions processor = getProcessorInstance(SAFSMessage.target_safs_view);
			if(processor == null) return;
	    	processor.setTestRecordData(droiddata);
	    	processor.setParams(params);
	    	processor.processResults(results);
		}
	}
	
	/**
	 * Process the remote results following the execution of the setText command(s).
	 * @param results
	 */
	protected void _checkCommandsResults(RemoteResults results){
		setRecordProcessed(true);
		int statusCode = droiddata.getStatusCode();	
		
		if(statusCode==StatusCodes.NO_SCRIPT_FAILURE){
			//Handle some results returned from device side and set detail result message.
			if(SAFSMessage.cf_timepicker_gettime.equalsIgnoreCase(action)){
				getTime(results);
			}else if(SAFSMessage.cf_timepicker_settime.equalsIgnoreCase(action)){
				setTime(results);
			}
		}
		
		statusCode = droiddata.getStatusCode();	
		if(statusCode==StatusCodes.NO_SCRIPT_FAILURE){
			if(processResourceMessageInfoResults(PASSED_MESSAGE)) 
				return;
			issuePassedSuccess("");// no additional comment
		}
		else if(statusCode==StatusCodes.SCRIPT_NOT_EXECUTED){
			// driver will log warning...
			setRecordProcessed(false);
		}
		else{ //object not found? what?		
			logResourceMessageFailure();
		}
	}
	
	/**
	 * The timepicker's time is returned by SAFSMessage.PARAM_9, and it is in format "HH:mm"<br>
	 * This method will retrieve the time and store it in a variable.<br>
	 * If some exceptions occur, we will set Failure to statuscode, so after calling this method<br>
	 * user needs to check the statuscode again.<br>
	 * 
	 * @param results RemoteResults, containing all results from device side.
	 */
	private void getTime(RemoteResults results){
		String varname = null;
		String time = null;
		Iterator<?> iter = null;
		
		try{
			iter = params.iterator();
			if(iter.hasNext()){
				varname = (String) iter.next();
				//The "time string" is returned from remote side through SAFSMessage.PARAM_9
				time = results.getString(SAFSMessage.PARAM_9);
				Log.debug(tag+" save time '"+time+"' to variable '"+varname+"'");

				if(staf.setVariable(varname, time)){
					//Success
					List<String> messageParams = new ArrayList<String>();
					messageParams.add(time);
					messageParams.add(varname);
					
					ResourceMessageInfo detailMessage = new ResourceMessageInfo(ResourceMessageInfo.BUNDLENAME_GENERICTEXT,
                            													GENStrings.VARASSIGNED2,messageParams);
					droiddata.setDetailMessage(detailMessage);
					setSuccessResourceMessageInfo(detailMessage);
					
				}else{
					Log.error(tag+" Fail to save '"+time+"' to variable '"+varname+"'");
					droiddata.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
					droiddata.setStatusInfo(" Fail to save '"+time+"' to variable '"+varname+"'");					
				}
				
			}else{
				//Normally, program will not comes here, At remote side, we checked the parameter's number. 
				Log.error(tag+" miss parameter variable.");
				droiddata.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				droiddata.setStatusInfo(" Miss parameter variable.");
			}
			
		}catch(Exception e){
			Log.error(tag+" Met "+e.getClass().getSimpleName()+" : "+e.getMessage());
			droiddata.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			droiddata.setStatusInfo(" Met "+e.getClass().getSimpleName()+" : "+e.getMessage());
		}
	}
	
	/**
	 * This method will set the detail success message for command setTime.<br>
	 * If some exceptions occur, we will set Failure to statuscode, so after calling this method<br>
	 * user needs to check the statuscode again.<br>
	 * 
	 * @param results RemoteResults, containing all results from device side.
	 */
	private void setTime(RemoteResults results){
		String time = null;
		Iterator<?> iter = null;
		
		try{
			iter = params.iterator();
			if(iter.hasNext()){
				time = (String) iter.next();
				Log.debug(tag+" time '"+time+"' has been set to TimePicker.");

				List<String> messageParams = new ArrayList<String>();
				messageParams.add(time);
				messageParams.add("TimePicker");
				ResourceMessageInfo detailMessage = new ResourceMessageInfo(ResourceMessageInfo.BUNDLENAME_GENERICTEXT,
						                                                    GENStrings.SOMETHING_SET,messageParams);
				droiddata.setDetailMessage(detailMessage);
				setSuccessResourceMessageInfo(detailMessage);
				
			}else{
				//Normally, program will not comes here, At remote side, we checked the parameter's number. 
				Log.error(tag+" miss parameter variable.");
				droiddata.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				droiddata.setStatusInfo(" Miss parameter variable.");
			}
			
		}catch(Exception e){
			Log.error(tag+" Met "+e.getClass().getSimpleName()+" : "+e.getMessage());
			droiddata.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			droiddata.setStatusInfo(" Met "+e.getClass().getSimpleName()+" : "+e.getMessage());
		}
	}
}
