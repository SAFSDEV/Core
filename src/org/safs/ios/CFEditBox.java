/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/


package org.safs.ios;

import java.util.Iterator;

import org.safs.ComponentFunction;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSObjectNotFoundException;
import org.safs.SAFSObjectRecognitionException;
import org.safs.StatusCodes;
import org.safs.text.FAILStrings;

public class CFEditBox extends ComponentFunction {

	/** "EditBox/" 
	 * Subdir off of Utilities.ROOT_JSCRIPTS_DIR containing EditBox keyword implementation scripts. */
	public static final String JSCRIPTS_EDITBOX_SUBDIR="EditBox/";

	/** "pinch.js" implementation for supported setValue commands in JSCRIPTS_EDITBOX_SUBDIR. */
	public static final String JSCRIPTS_EDITBOX_SCRIPT="SetTextValue.js";
	
	
	/** "SETVALUE" */
	public static final String COMMAND_SETVALUE = "SETTEXTVALUE";
	
	protected String windowGUIID = null;
	protected String compGUIID = null;
	
	public CFEditBox() {
		super();
	}

	/**
	 * TestRecordData should already have most fields including the object recognition strings.
	 */
	public void process(){		
		testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
		try{ 
			getHelpers();
		}catch(Exception x){//assuming no exceptions are actually thrown
			Log.debug("CFWindow.process unexpected getHelpers() "+ x.getClass().getSimpleName()+":"+ x.getMessage());
		}
		windowGUIID = null;
		if(action==null){
			Log.debug("CFWindow could not process NULL action command.");
			return;
		}		
		try {
			if(action.equalsIgnoreCase(COMMAND_SETVALUE)){
				doSetTextValue();
			}
		} catch (SAFSObjectRecognitionException e) {
			Log.debug("CFWindow recognition string missing or invalid for: "+ e.getMessage());
			issueParameterValueFailure(e.getMessage());
		} catch (InstrumentsScriptExecutionException e){
			Log.debug("CFWindow "+ e.getClass().getSimpleName() +": "+ e.getMessage());
			issueUnknownErrorFailure(e.getMessage());
		} catch (SAFSObjectNotFoundException e){
			Log.debug("CFWindow "+ e.getClass().getSimpleName() +": "+ e.getMessage());
			issueErrorPerformingAction(FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND, 
									   e.getMessage() +" was not found.", e.getMessage()));
		}	
	}

	/**
	 * process the setValue commands...
	 * Appends the following variables into trd.js:
	 * <pre>
	 * var origin={x:value, y:value, width:value, height:value};
	 * var target={width:value, height:value};
	 * var seconds=N;  (in seconds)
	 * </pre>
	 * If no seconds is specified than the default of 1 second will be used.
	 */
	/**
	 * process the setValue command....
	 * Set Value to the EditBox
	 */
	protected void doSetTextValue() throws SAFSObjectRecognitionException, 
	                                InstrumentsScriptExecutionException,
	                                SAFSObjectNotFoundException{
		try{  windowGUIID = testRecordData.getWindowGuiId(); }catch(SAFSException x){}
		finally{
			if(windowGUIID == null || windowGUIID.length()==0)
				throw new SAFSObjectRecognitionException("windowName:"+windowName);
		}
		try{  compGUIID = testRecordData.getCompGuiId(); }catch(SAFSException x){}
		finally{
			if(compGUIID == null || compGUIID.length()==0)
				throw new SAFSObjectRecognitionException("compName:"+compName);
		}

		String trdAppend = null;
		String mapkey = null;

		if (params.size()> 0){
			mapkey = (String) params.toArray()[0];
		}else{
			this.issueParameterCountFailure("TextValue");
			return;
		}
		trdAppend = "\nvar settextvalue=\""+ mapkey +"\";\n";
		Log.info(action +" set text: \""+ mapkey +"\"");
		
		IStatus stat = CFComponent.processIOSScript(JSCRIPTS_EDITBOX_SUBDIR, JSCRIPTS_EDITBOX_SCRIPT, trdAppend, testRecordData); 
		//stat.rc == return code
		//stat.comment == already localized text message, if any.  Can be empty.
		//stat.detail == already localized text message, if any. Can be empty or null.
		if(stat.rc == IStatus.STAT_OK){
			issuePassedSuccessUsing("\""+ mapkey +"\"");
		}else if(stat.rc == IStatus.STAT_COMP_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName+":"+compName);
		}else if(stat.rc == IStatus.STAT_WINDOW_NOT_FOUND){
			throw new SAFSObjectNotFoundException(windowName);
		}else{
			testRecordData.setStatusCode(stat.rc);
			issueErrorPerformingAction(stat.comment);
		}		
	}	
}
