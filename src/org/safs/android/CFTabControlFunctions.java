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

import org.safs.StatusCodes;
import org.safs.android.remotecontrol.SAFSMessage;

import com.jayway.android.robotium.remotecontrol.solo.RemoteResults;

/**
 * Processor handling SAFS Droid TabControl functions.
 */
public class CFTabControlFunctions extends CFComponentFunctions {
	private static String tag = "CFTabControlFunctions: ";

	/**
	 * Default constructor. 
	 */
	public CFTabControlFunctions() {
		super();
	}

	@Override
	protected void processResults(RemoteResults results){
		
		if(SAFSMessage.cf_tab_clicktab.equalsIgnoreCase(action)||
		   SAFSMessage.cf_tab_clicktabcontains.equalsIgnoreCase(action)  ||
		   SAFSMessage.cf_tab_selecttab.equalsIgnoreCase(action) ||
		   SAFSMessage.cf_tab_selecttabindex.equalsIgnoreCase(action) ||
		   SAFSMessage.cf_tab_unverifiedclicktab.equalsIgnoreCase(action) ||
		   SAFSMessage.cf_tab_makeselection.equalsIgnoreCase(action))
		{
			_oneParameterCommandsResults(results);
		}else{
			CFComponentFunctions processor = getProcessorInstance(SAFSMessage.target_safs_view);
			if(processor == null) return;
	    	processor.setTestRecordData(droiddata);
	    	processor.setParams(params);
	    	processor.processResults(results);
		}
	}
	
	/**
	 * Process the remote results following the execution of one parameter command.
	 * @param results
	 */
	protected void _oneParameterCommandsResults(RemoteResults results){
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
