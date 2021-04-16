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

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.safs.Log;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.android.remotecontrol.SAFSMessage;
import org.safs.sockets.RemoteException;
import org.safs.sockets.ShutdownInvocationException;
import org.safs.tools.CaseInsensitiveFile;

import com.jayway.android.robotium.remotecontrol.solo.RemoteResults;

/**
 * Processor handling SAFS Droid ComboBox functions.
 * 
 * History:
 * 	SEP, 03, 2012	(Lei Wang)	Initial implementation
 */
public class CFComboBoxFunctions extends CFComponentFunctions {
	private static String tag = "CFComboBoxFunctions: ";

	/**
	 * Default constructor. 
	 */
	public CFComboBoxFunctions() {
		super();
	}


	@Override
	protected void processResults(RemoteResults results){
		
		if(isKeywordSupported())
		{
			setRecordProcessed(true);
			int statusCode = droiddata.getStatusCode();	
			
			if(statusCode==StatusCodes.NO_SCRIPT_FAILURE){
				//Handle some results returned from device side.
				if(SAFSMessage.cf_comprouting_captureitemstofile.equalsIgnoreCase(action)){
					_captureItems(results);
				}
			}
			
			//Get the statuscode again
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
		}else{
			CFComponentFunctions processor = getProcessorInstance(SAFSMessage.target_safs_view);
			if(processor == null) return;
	    	processor.setTestRecordData(droiddata);
	    	processor.setParams(params);
	    	processor.processResults(results);
		}
	}
	
	/**
	 * This method is used to check if the keyword can be handled in this special Processor.<br>
	 * 
	 * @return	boolean, True, if the keyword can be handled here.
	 * 					 False, if the keyword can not be handled here. But we will not set
	 * 						    the status code to Failure, let other processors to handle. 
	 */
	protected boolean isKeywordSupported(){
		if(SAFSMessage.cf_comprouting_captureitemstofile.equalsIgnoreCase(action)||
		   SAFSMessage.cf_comprouting_select.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_selectindex.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_selectpartialmatch.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_selectunverified.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_settextvalue.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_setunverifiedtextvalue.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_verifyselected.equalsIgnoreCase(action)){
			return true;
		}else{
			Log.debug(tag+"The keyword '"+action+"' has not been supported.");
			return false;
		}
	}
	
	/**
	 * The combobox's items are returned by SAFSMessage.PARAM_9, and it is separated by<br>
	 * SAFSMessage.cf_combobox_items_separator<br>
	 * This method will retrieve the items and store them in a file.<br>
	 * If some exception occur, we will set Failure to statuscode, so after calling this method<br>
	 * user needs to check the statuscode again.<br>
	 * 
	 * @param results RemoteResults, containing all results from device side.
	 */
	private void _captureItems(RemoteResults results){
		String itemTexts = "";
		List<?> itemList = null;
		String filename = null;
		String encoding = null;
		Iterator<?> iter = null;
		
		try{
			iter = params.iterator();
			if(iter.hasNext()) filename = (String) iter.next();
			if(iter.hasNext()) encoding = (String) iter.next();
			
	        File file = new CaseInsensitiveFile(filename).toFile();
			if (!file.isAbsolute()) {
				String testdir = getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
				if (testdir != null) {
					file = new CaseInsensitiveFile(testdir, filename).toFile();
					filename = file.getAbsolutePath();
				}
			}
			Log.debug(tag+" Write to file '"+filename+"' with encoding '"+encoding+"'");
			
			itemTexts = results.getString(SAFSMessage.PARAM_9);
			itemList = StringUtils.getTokenList(itemTexts, SAFSMessage.cf_combobox_items_separator);

			StringUtils.writeEncodingfile(filename, itemList, encoding);
			
		}catch(Exception e){
			Log.error(tag+" Met "+e.getClass().getSimpleName()+" : "+e.getMessage());
			droiddata.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			droiddata.setStatusInfo(" Met "+e.getClass().getSimpleName()+" : "+e.getMessage());
		}
	}

}
