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

import java.util.concurrent.TimeoutException;

import org.safs.StatusCodes;
import org.safs.android.remotecontrol.SAFSMessage;
import org.safs.sockets.RemoteException;
import org.safs.sockets.ShutdownInvocationException;

import com.jayway.android.robotium.remotecontrol.solo.RemoteResults;

/**
 * Processor handling SAFS Droid CheckBox functions.
 * 
 * History:
 * 	AUG, 29, 2012	(Lei Wang)	Initial implementation
 */
public class CFCheckBoxFunctions extends CFComponentFunctions {
	private static String tag = "CFCheckBoxFunctions: ";

	/**
	 * Default constructor. 
	 */
	public CFCheckBoxFunctions() {
		super();
	}

	@Override
	protected void processResults(RemoteResults results){
		
		if(SAFSMessage.cf_comprouting_check.equalsIgnoreCase(action)||
		   SAFSMessage.cf_comprouting_uncheck.equalsIgnoreCase(action))
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
}
