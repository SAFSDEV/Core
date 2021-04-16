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
import org.safs.text.FAILKEYS;
import org.safs.text.GENKEYS;
import org.safs.text.ResourceMessageInfo;
import org.safs.tools.CaseInsensitiveFile;

import com.jayway.android.robotium.remotecontrol.solo.RemoteResults;

/**
 * Processor handling SAFS Droid ListView functions.
 * 
 * History:
 * 	SEP, 11, 2012	(Carl Nagle)	Initial implementation
 */
public class CFListViewFunctions extends CFComponentFunctions {
	private static String tag = "CFListViewFunctions: ";

	/**
	 * Default constructor. 
	 */
	public CFListViewFunctions() {
		super();
	}

	@Override
	protected void processResults(RemoteResults results){
		
		if(isKeywordSupported())
		{
			setRecordProcessed(true);
			int statusCode = droiddata.getStatusCode();	
			
			if(statusCode==StatusCodes.NO_SCRIPT_FAILURE){
				//Handle some results that require additional processing.
				if(SAFSMessage.cf_comprouting_captureitemstofile.equalsIgnoreCase(action)){
					_captureItems(results);
					return;
				}
			}
			
			//Get the statuscode again
			statusCode = droiddata.getStatusCode();
			if(statusCode==StatusCodes.NO_SCRIPT_FAILURE){				
				if(processResourceMessageInfoResults(PASSED_MESSAGE)) return;
				issuePassedSuccess("");// default with no additional comment
			}
			else if(statusCode==StatusCodes.SCRIPT_NOT_EXECUTED){
				// driver will log warning...
				setRecordProcessed(false);
			}
			else{ //object not found? what?		
				logResourceMessageFailure();
			}
		}else{ //try to chain to CFViewFunctions for more generic commands
			CFComponentFunctions processor = getProcessorInstance(SAFSMessage.target_safs_view);
			if(processor == null) return;
	    	processor.setTestRecordData(droiddata);
	    	processor.setParams(params);
	    	processor.processResults(results);
		}
	}
	
	/**
	 * This method is used to check if the keyword can be handled in this special Processor.<br>
	 * This method needs to be called with {@link #checkParameterSize()} at the same time, but before it<br>
	 * such as if(isKeywordSupported() && checkParameterSize()), which check the condition if we can<br>
	 * call {@link #processProperties(int)} to handle the keyword.<br>
	 * 
	 * @return	boolean, True, if the keyword can be handled here.
	 * 					 False, if the keyword can not be handled here. But we will not set
	 * 						    the status code to Failure, let other processors to handle. 
	 */
	protected boolean isKeywordSupported(){
		if(SAFSMessage.cf_comprouting_activatepartialmatch.equalsIgnoreCase(action)||
		   SAFSMessage.cf_comprouting_activateindex.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_activateindexitem.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_activatetextitem.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_activateunverifiedtextitem.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_captureitemstofile.equalsIgnoreCase(action)||
		   SAFSMessage.cf_comprouting_clickindex.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_clickindexitem.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_clicktextitem.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_selectindex.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_selectindexitem.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_selectpartialmatch.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_selecttextitem.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_selectunverifiedtextitem.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_selectunverifiedpartialmatch.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_setlistcontains.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_verifylistcontains.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_verifylistdoesnotcontain.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_verifyitemunselected.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_verifypartialmatch.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_verifyselectedpartialmatch.equalsIgnoreCase(action)||	
		   SAFSMessage.cf_comprouting_verifyselecteditem.equalsIgnoreCase(action)){
			return true;
		}else{
			Log.debug(tag+"Keyword '"+action+"' is not supported.");
			return false;
		}
	}
	
	/**
	 * The listview's items are returned by SAFSMessage.KEY_REMOTERESULTINFO, and it is separated by 
	 * SAFSMessage.cf_combobox_items_separator
	 * <p>
	 * This method will retrieve the items and store them in a file.<br>
	 * The routine will log its own success or failure.<br>
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
			
			itemTexts = results.getString(SAFSMessage.KEY_REMOTERESULTINFO);
			itemList = StringUtils.getTokenList(itemTexts, SAFSMessage.cf_combobox_items_separator);

			StringUtils.writeEncodingfile(filename, itemList, encoding);
			issuePassedSuccessUsing(filename);
		}
		catch(Exception e){
			Log.error(tag+" Met "+e.getClass().getSimpleName()+" : "+e.getMessage());
			droiddata.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			results.getResultsProperties().setProperty(SAFSMessage.RESOURCE_BUNDLE_NAME_FOR_MSG, ResourceMessageInfo.BUNDLENAME_FAILEDTEXT);
			results.getResultsProperties().setProperty(SAFSMessage.RESOURCE_BUNDLE_KEY_FOR_MSG, FAILKEYS.GENERIC_ERROR);
			results.getResultsProperties().setProperty(SAFSMessage.RESOURCE_BUNDLE_PARAMS_FOR_MSG, ";"+ e.getClass().getSimpleName()+": "+ e.getMessage());
			logResourceMessageFailure();
		}
	}
}
