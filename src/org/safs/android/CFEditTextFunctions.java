/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.android;

import java.util.concurrent.TimeoutException;

import org.safs.Log;
import org.safs.StatusCodes;
import org.safs.android.remotecontrol.SAFSMessage;
import org.safs.sockets.RemoteException;
import org.safs.sockets.ShutdownInvocationException;

import com.jayway.android.robotium.remotecontrol.solo.RemoteResults;

/**
 * Processor handling SAFS Droid EditText functions.
 */
public class CFEditTextFunctions extends CFComponentFunctions {
	private static String tag = "CFEditTextFunctions: ";

	/**
	 * Default constructor. 
	 */
	public CFEditTextFunctions() {
		super();
	}

	@Override
	protected void processResults(RemoteResults results){
		
		if(SAFSMessage.cf_comprouting_settextcharacters.equalsIgnoreCase(action)||
		   SAFSMessage.cf_comprouting_settextvalue.equalsIgnoreCase(action)  ||
		   SAFSMessage.cf_comprouting_setunverifiedtextcharacters.equalsIgnoreCase(action) ||
		   SAFSMessage.cf_comprouting_setunverifiedtextvalue.equalsIgnoreCase(action))
		{
			_setTextCommandsResults(results);
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
	protected void _setTextCommandsResults(RemoteResults results){
		setRecordProcessed(true);
		int statusCode = droiddata.getStatusCode();		
		if(statusCode==StatusCodes.NO_SCRIPT_FAILURE){
			if(processResourceMessageInfoResults(PASSED_MESSAGE)) 
				return;
			if(props.containsKey(SAFSMessage.PARAM_1)){
				issuePassedSuccessUsing(props.getProperty(SAFSMessage.PARAM_1));
			}else{
				issuePassedSuccess("");// no additional comment
			}
		}
		else if(statusCode==StatusCodes.SCRIPT_NOT_EXECUTED){
			// driver will log warning...
			setRecordProcessed(false);
		}
		else{ //object not found? what?		
			logResourceMessageFailure();
		}
	}
}
