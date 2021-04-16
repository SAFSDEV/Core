/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 * Processor handling SAFS Droid ProgressBar functions.
 * 
 * History:
 * 	DEC, 18, 2012	(Lei Wang)	Initial implementation
 */
public class CFProgressBarFunctions extends CFComponentFunctions {
	private static String tag = "CFTimePickerFunctions: ";

	/**
	 * Default constructor. 
	 */
	public CFProgressBarFunctions() {
		super();
	}

	@Override
	protected void processResults(RemoteResults results){
		
		if(SAFSMessage.cf_progressbar_getprogress.equalsIgnoreCase(action)||
		   SAFSMessage.cf_progressbar_setprogress.equalsIgnoreCase(action) ||
		   SAFSMessage.cf_progressbar_getrating.equalsIgnoreCase(action) ||
		   SAFSMessage.cf_progressbar_setrating.equalsIgnoreCase(action))
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
			if(SAFSMessage.cf_progressbar_getprogress.equalsIgnoreCase(action)){
				getProgress(results);
			}else if(SAFSMessage.cf_progressbar_setprogress.equalsIgnoreCase(action)){
				setProgress(results);
			}else if(SAFSMessage.cf_progressbar_getrating.equalsIgnoreCase(action)){
				getRating(results);
			}else if(SAFSMessage.cf_progressbar_setrating.equalsIgnoreCase(action)){
				setRating(results);
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
	 * The progressbar's progress is returned by SAFSMessage.PARAM_9, and it is in format "xx%"<br>
	 * This method will retrieve the progress and store it in a variable.<br>
	 * If some exceptions occur, we will set Failure to statuscode, so after calling this method<br>
	 * user needs to check the statuscode again.<br>
	 * 
	 * @param results RemoteResults, containing all results from device side.
	 */
	private void getProgress(RemoteResults results){
		String varname = null;
		String progress = null;
		Iterator<?> iter = null;
		
		try{
			iter = params.iterator();
			if(iter.hasNext()){
				varname = (String) iter.next();
				//The "progress string" is returned from remote side through SAFSMessage.PARAM_9
				progress = results.getString(SAFSMessage.PARAM_9);
				Log.debug(tag+" save progress '"+progress+"' to variable '"+varname+"'");

				if(staf.setVariable(varname, progress)){
					//Success
					List<String> messageParams = new ArrayList<String>();
					messageParams.add(progress);
					messageParams.add(varname);
					
					ResourceMessageInfo detailMessage = new ResourceMessageInfo(ResourceMessageInfo.BUNDLENAME_GENERICTEXT,
                            													GENStrings.VARASSIGNED2,messageParams);
					droiddata.setDetailMessage(detailMessage);
					setSuccessResourceMessageInfo(detailMessage);
					
				}else{
					Log.error(tag+" Fail to save '"+progress+"' to variable '"+varname+"'");
					droiddata.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
					droiddata.setStatusInfo(" Fail to save '"+progress+"' to variable '"+varname+"'");					
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
	 * This method will set the detail success message for command setProgress.<br>
	 * If some exceptions occur, we will set Failure to statuscode, so after calling this method<br>
	 * user needs to check the statuscode again.<br>
	 * 
	 * @param results RemoteResults, containing all results from device side.
	 */
	private void setProgress(RemoteResults results){
		String progress = null;
		Iterator<?> iter = null;
		
		try{
			iter = params.iterator();
			if(iter.hasNext()){
				progress = (String) iter.next();
				Log.debug(tag+" progress '"+progress+"' has been set to ProgressBar.");

				List<String> messageParams = new ArrayList<String>();
				messageParams.add(progress);
				messageParams.add("ProgressBar");
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
	
	/**
	 * The ratingbar's rating is returned by SAFSMessage.PARAM_9, and it is a float number.<br>
	 * This method will retrieve the rating and store it in a variable.<br>
	 * If some exceptions occur, we will set Failure to statuscode, so after calling this method<br>
	 * user needs to check the statuscode again.<br>
	 * 
	 * @param results RemoteResults, containing all results from device side.
	 */
	private void getRating(RemoteResults results){
		String varname = null;
		String rating = null;
		Iterator<?> iter = null;
		
		try{
			iter = params.iterator();
			if(iter.hasNext()){
				varname = (String) iter.next();
				//The "rating string" is returned from remote side through SAFSMessage.PARAM_9
				rating = results.getString(SAFSMessage.PARAM_9);
				Log.debug(tag+" save rating '"+rating+"' to variable '"+varname+"'");

				if(staf.setVariable(varname, rating)){
					//Success
					List<String> messageParams = new ArrayList<String>();
					messageParams.add(rating);
					messageParams.add(varname);
					
					ResourceMessageInfo detailMessage = new ResourceMessageInfo(ResourceMessageInfo.BUNDLENAME_GENERICTEXT,
                            													GENStrings.VARASSIGNED2,messageParams);
					droiddata.setDetailMessage(detailMessage);
					setSuccessResourceMessageInfo(detailMessage);
					
				}else{
					Log.error(tag+" Fail to save '"+rating+"' to variable '"+varname+"'");
					droiddata.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
					droiddata.setStatusInfo(" Fail to save '"+rating+"' to variable '"+varname+"'");					
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
	 * This method will set the detail success message for command setRating.<br>
	 * If some exceptions occur, we will set Failure to statuscode, so after calling this method<br>
	 * user needs to check the statuscode again.<br>
	 * 
	 * @param results RemoteResults, containing all results from device side.
	 */
	private void setRating(RemoteResults results){
		String rating = null;
		Iterator<?> iter = null;
		
		try{
			iter = params.iterator();
			if(iter.hasNext()){
				rating = (String) iter.next();
				Log.debug(tag+" rating '"+rating+"' has been set to RatingBar.");

				List<String> messageParams = new ArrayList<String>();
				messageParams.add(rating);
				messageParams.add("RatingBar");
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
