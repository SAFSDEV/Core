/** Copyright (C) SAS Institute All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * SEP 30, 2015 CANAGL CFComponent return NOT_EXECUTED if RS is empty, missing, or not AutoIt RS. 
 * DEC 23, 2015 SBJLWA Add method activate(): focus both window and component.
 *                     Modify process(): Wait for window and component's existence and focus before execution.
 * JUL 12, 2016 SBJLWA Implement 'SetPosition' keyword.
 */
package org.safs.tools.engines;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;

import org.safs.ComponentFunction;
import org.safs.Log;
import org.safs.Processor;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.TestRecordHelper;
import org.safs.autoit.AutoIt;
import org.safs.autoit.AutoItRs;
import org.safs.logging.AbstractLogFacility;
import org.safs.logging.LogUtilities;
import org.safs.model.commands.DriverCommands;
import org.safs.model.commands.EditBoxFunctions;
import org.safs.model.commands.GenericObjectFunctions;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverInterface;

import autoitx4java.AutoItX;

/**
 * Provides local in-process support for AutoIt Component Functions on Windows.
 * <p>
 * This engine does not assume the use of STAF. Instead, it uses the
 * various org.safs.tools Interfaces to talk with the rest of the framework (as made
 * available via the DriverInterface configuration).
 * @author Dharmesh Patel
 */
public class AutoItComponent extends GenericEngine {

	/** "SAFS/AUTOITComponent" */
	static final String ENGINE_NAME  = "SAFS/AUTOITComponent";

	/** "AUTOITComponent" */
	static final String AUTOITCOMPONENT_ENGINE  = "AUTOITComponent";

	// START: LOCALLY SUPPORTED COMMANDS
	
	/** "SetFocus" */
    static final String COMMAND_SETFOCUS		       = DriverCommands.SETFOCUS_KEYWORD;

	/** "Click" */
    static final String COMMAND_CLICK 			       = GenericObjectFunctions.CLICK_KEYWORD;       

    /** "SetTextValue" */
    static public final String SETTEXTVALUE_KEYWORD = EditBoxFunctions.SETTEXTVALUE_KEYWORD;
	       
	// END: LOCALLY SUPPORTED COMMANDS

	ComponentFunction cf = null;
	
	/**
	 * Constructor for AUTOITComponent
	 */
	public AutoItComponent() {
		super();
		servicename = ENGINE_NAME;
	}

	/**
	 * PREFERRED Constructor for AUTOITComponent.
	 * Constructs the instance and calls launchInterface to initialize.
	 */
	public AutoItComponent(DriverInterface driver) {
		this();
		
		launchInterface(driver);
	}

	/**
	 * @see GenericEngine#launchInterface(Object)
	 */
	public void launchInterface(Object configInfo){
		super.launchInterface(configInfo);
		log = new LogUtilities(this.staf);
		cf = new CFComponent();
		cf.setLogUtilities(log);
	}

	public long processRecord (TestRecordHelper testRecordData){
		
		if(!Processor.RECTYPE_TEST_STEP.equalsIgnoreCase(testRecordData.getRecordType())){
			Log.info("AUTOITC: only handles CF TestRecords at this time....");
        	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
        	return StatusCodes.SCRIPT_NOT_EXECUTED;
		}
		Log.info("AUTOITC:processing \""+ testRecordData.getCommand() +"\".");
		this.testRecordData = testRecordData;
		boolean resetTRD = false;
		if (testRecordData.getSTAFHelper()==null){
		    testRecordData.setSTAFHelper(staf);
		    resetTRD = true;
		}
		Collection<String> params = interpretFields(testRecordData);
		if(params instanceof Collection){
			cf.setTestRecordData(testRecordData);			
			cf.setParams(params);	
			cf.setIterator(params.iterator());
			cf.process();
		}
		if(resetTRD) testRecordData.setSTAFHelper(null);
		return testRecordData.getStatusCode();
	}

	/**
	 * Note:	Now this method is called only by a special processor CFAUTOITComponent
	 * 			Before calling this method, you must make sure the test record data
	 * 			is correctly initialized.
	 * @param testRecordData
	 */
	public void processIndependently(TestRecordHelper testRecordData){
		String debugmsg = getClass().getName()+".processIndependently(): ";
		Log.info("AUTOITC:processing \""+ testRecordData.getCommand() +"\" independently.");
		this.testRecordData = testRecordData;
		cf = new CFComponent();
		//As from a processor we call constructor AUTOITComponent(), not AUTOITComponent(DriverInterface),
		//so the new instantiated TID engine's field staf is null, but we can get the
		//staf helper from the testRecordData,so we can initiate its logUtilities with this staf
		staf = testRecordData.getSTAFHelper();
		if(staf!=null){
			Log.debug(debugmsg+" We got the STAF from the test record helper.");
		}else{
			Log.debug(debugmsg+" Can NOT get STAF from the test record helper.");
		}
		cf.setLogUtilities(new LogUtilities(staf));
		Collection<String> params = interpretFields(testRecordData);
		if(params instanceof Collection){
			cf.setTestRecordData(testRecordData);			
			cf.setParams(params);	
			cf.setIterator(params.iterator());
			cf.process();
		}
	}
	
	protected Collection<String> interpretFields (TestRecordHelper testRecordData) {
	    Collection<String> params = new ArrayList<String>();
	    String nextElem = ""; // used to log errors in the catch blocks below
	    int tokenIndex = 1; // start from 1, because we already have the recordType which was 0
	    try {
	        nextElem = "windowName"; //..get the windowName, the second token (from 1)
	        String windowName = testRecordData.getTrimmedUnquotedInputRecordToken(tokenIndex);
	        testRecordData.setWindowName(windowName);
	      
	        tokenIndex = 2;
	        nextElem = "compName"; //..get the compName, the third token (from 1)
	        String compName = testRecordData.getTrimmedUnquotedInputRecordToken(tokenIndex);
	        testRecordData.setCompName(compName);
	      
	        tokenIndex = 3;
	        nextElem = "command"; //..get the command, the fourth token (from 1)
	        String command = testRecordData.getTrimmedUnquotedInputRecordToken(tokenIndex);
	        testRecordData.setCommand(command);
	      
	        for(tokenIndex = 4; tokenIndex < testRecordData.inputRecordSize(); tokenIndex++) {
	        	nextElem = "param"; //..get the param, tokens #5 - N (from 1)
	        	String param = testRecordData.getTrimmedUnquotedInputRecordToken(tokenIndex);
	        	params.add(param);
	        }
	    } catch (Exception ioobe) {
    		String message = failedText.convert("invalid_missing", 
	                 "Invalid or missing '"+ nextElem +"' parameter in "+ 
                    testRecordData.getFilename() +" at line "+
                    String.valueOf(testRecordData.getLineNumber()),
                    nextElem, testRecordData.getFilename(), String.valueOf(testRecordData.getLineNumber()));
	        logMessage(message, testRecordData.getInputRecord(), AbstractLogFacility.FAILED_MESSAGE);    		    		
	        testRecordData.setStatusCode(DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
	        return null;
	    } 
	    return params;
	}
	
	/******************************************************
	 * Local CFComponent 
	 * @author canagl
	 ******************************************************/
	class CFComponent extends org.safs.ComponentFunction {
		/**
		 * The AutoIt instance got by AutoIt.AutoItObject().<br>
		 * This instance may be shared by multiple threads.<br>
		 */
		protected AutoItX it = null;
		/** '1', the default timeout in seconds to wait the window become focused. */
		protected int DEFAULT_TIMEOUT_WAIT_WIN_FOCUSED = 1;
		/** AutoItRs, the object representing the component */
		protected AutoItRs rs = null;
		
		CFComponent (){
			super();
			try{
				it = AutoIt.AutoItObject();
			}catch(Exception e){
				Log.error(StringUtils.debugmsg(false)+" Fail to get AutoIt Instance, due to "+StringUtils.debugmsg(e));
			}
		}	
		
		/**
		 * Process the record present in the provided testRecordData.
		 */
		public void process(){
			updateFromTestRecordData();
			String debugmsg = "AutoItComponent$CFComponent.process() "+ action +": ";
			
			String winrec = null;
			String comprec = null;
			// GUILess commands may NOT have a recognition string
			try{
				winrec = testRecordData.getWindowGuiId();
				comprec = testRecordData.getCompGuiId();
			}catch(SAFSException e){
				Log.debug(debugmsg + "recognition strings missing or invalid for AutoIt engine.");
	        	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
	        	return;
			}
			if (!AutoItRs.isAutoitBasedRecognition(winrec)){
				Log.info(debugmsg + " skipping due to non-AutoIt recognition.");
	        	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
	        	return;
	        }
			Log.info(debugmsg + " win: "+ windowName +"; comp: "+ compName+"; with params: "+params);

			// prepare Autoit RS
			rs = new AutoItRs(winrec,comprec);
			
			//Wait the window to exist
			if(!it.winWait(rs.getWindowsRS(), "", secsWaitForWindow)){
				String errormsg = "Failed due to not finding the window '"+windowName+"'";
				Log.debug(debugmsg+" Failed  '"+errormsg);
				testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );	 
				this.issueErrorPerformingAction(errormsg);
				return;
			}
			//Wait the window to be active, if not then try to activate it.
			if(!it.winWaitActive(rs.getWindowsRS(), "", DEFAULT_TIMEOUT_WAIT_WIN_FOCUSED)){
				Log.debug(debugmsg+" try to set focus to '"+testRecordData.getWinCompName()+"'");
				if(!activate(rs)){
					Log.warn(debugmsg+" '"+testRecordData.getWinCompName()+"' is not focused!");
				}
			}
			
			if ( action.equalsIgnoreCase( COMMAND_CLICK )){		                
				click(rs);	                
			} else if ( action.equalsIgnoreCase( COMMAND_SETFOCUS )) {
			    setFocus(rs);
			} else if ( action.equalsIgnoreCase(SETTEXTVALUE_KEYWORD)) {
				setText(rs);
			}
			
			if (testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED) {
				componentProcess();//handle Generic keywords
			} else {
				Log.debug(debugmsg+"'"+action+"' has been processed\n with testrecorddata"+testRecordData+"\n with params "+params);
			}
		}
		
		/**
		 * Try to set focus to the window and component.
		 * @param rs AutoItRs, the object representing the component
		 * @return boolean, true if the window and component are focused.
		 */
		protected boolean activate(AutoItRs rs){
			String debugmsg = StringUtils.debugmsg(false);
			boolean success = true;
			try {
				it.winActivate(rs.getWindowsRS());
				if(it.getError()==1){
					success = false;
					Log.debug(debugmsg+" failed to activate window '"+windowName+"'.");
				}

				if(success && testRecordData.targetIsComponent()){
					success = it.controlFocus(rs.getWindowsRS(), "", rs.getComponentRS());
					if(!success) Log.debug(debugmsg+" failed to activate component '"+compName+"'.");
				}
			} catch (Exception x) {
				success = false;
				Log.debug(debugmsg+" Met "+StringUtils.debugmsg(x));
			}
			return success;
		}
		
		/** setfoucs **/
		protected void setFocus(AutoItRs ars){
			String debugmsg = StringUtils.debugmsg(false);
			
			testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );	 
			try {
				Log.debug(debugmsg+" trying to activate '"+testRecordData.getWinCompName()+"'.");
				if(activate(ars)){
					Log.debug(debugmsg+" '"+testRecordData.getWinCompName()+"' has been successfully focused.");
					testRecordData.setStatusCode(StatusCodes.OK);
					// log success message and status
					String altText = windowName +":"+ compName + " "+ action +" successful.";
					String msg = genericText.convert("success3", altText, windowName, compName, action);
					log.logMessage(testRecordData.getFac(),msg, PASSED_MESSAGE);
				}else{
					issueErrorPerformingActionOnX(testRecordData.getWinCompName(), "AUTOIT fails with error "+it.getError());
				}

			} catch (Exception x) {
				this.issueErrorPerformingAction(x.getClass().getSimpleName()+": "+ x.getMessage());
			}		
		}
		
		/** click **/
		protected void click(AutoItRs ars){
			testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );	 
			try {

				boolean rc = it.controlClick(ars.getWindowsRS(), "", ars.getComponentRS());

				if (rc){
					testRecordData.setStatusCode(StatusCodes.OK);
					// log success message and status
					String altText = windowName +":"+ compName + " "+ action +" successful.";
					String msg = genericText.convert("success3", altText, windowName, compName, action);
					log.logMessage(testRecordData.getFac(),msg, PASSED_MESSAGE);
				} else {
					testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );	 
					this.issueErrorPerformingAction("Failed with rc= " + it.getError());	
				}

			} catch (Exception x) {
				this.issueErrorPerformingAction(x.getClass().getSimpleName()+": "+ x.getMessage());
			}		
		}		
		
		/** 
		 * Send keyboard input to the current input focus target.
		 * The command does not attempt to change keyboard focus from 
		 * where it already is.
		 **/
		protected void setText(AutoItRs ars) {
			
			String debugmsg = StringUtils.debugmsg(getClass(), "setText");
			
	        testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );	        
	        if ( params.size( ) < 1 ) {
	            this.issueParameterCountFailure("Insufficient parameters provided.");
	            return;
	        }
	        
            String text = ( String ) iterator.next( );
			if((text==null)||(text.length()==0)){
	            this.issueParameterValueFailure("TextValue");
	            return;
			}
			
		   	Log.info(debugmsg + " processing: "+ text);
		   	try{
				boolean rc = it.ControlSetText(ars.getWindowsRS(), "", ars.getComponentRS(), text);
				if (rc) {
			   	testRecordData.setStatusCode(StatusCodes.OK);
				// log success message and status
				log.logMessage(testRecordData.getFac(), 
						genericText.convert("success3a", windowName +":"+ compName + " "+ action
								+" successful using "+ text, windowName, compName, action, text), 
						PASSED_MESSAGE);
				} else {
					testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );	 
					this.issueErrorPerformingAction("Failed with rc= " + it.getError());	
				}
		   
		   	}catch(Exception x){
		   		this.issueErrorPerformingAction(x.getClass().getSimpleName()+": "+ x.getMessage());
		   	}
		}
		
		/** <br><em>Purpose:</em> restore
		 **/
		protected void _restore() throws SAFSException{
			String debugmsg = StringUtils.debugmsg(false);
			try{
//				it.winSetState(rs.getWindowsRS(), "", AutoItX.SW_RESTORE);
				it.winSetState(rs.getWindowsRS(), "", AutoItX.SW_SHOWNORMAL);
			}catch(Exception e){
				String msg = "Fail to resotre window due to Exception "+e.getMessage();
				Log.error(debugmsg+msg);
				throw new SAFSException(msg);
			}    
		}

		protected void _setPosition(Point position) throws SAFSException {
			String debugmsg = StringUtils.debugmsg(false);
			try{
				it.winMove(rs.getWindowsRS(), "", position.x, position.y);
			}catch(Exception e){
				String msg = "Fail to set window to position "+position+", due to Exception "+e.getMessage();
				Log.error(debugmsg+msg);
				throw new SAFSException(msg);
			}
		}

		protected void _setSize(Dimension size) throws SAFSException {
			String debugmsg = StringUtils.debugmsg(false);
			try{
				int x = it.winGetPosX(rs.getWindowsRS(), "");
				int y = it.winGetPosY(rs.getWindowsRS(), "");
				
				it.winMove(rs.getWindowsRS(), "", x, y, size.width, size.height);
				
			}catch(Exception e){
				String msg = "Fail to set window to size "+size+" ,due to Exception "+e.getMessage();
				Log.error(debugmsg+msg);
				throw new SAFSException(msg);
			}
		}

		/** <br><em>Purpose:</em> minimize
		 **/
		protected void _minimize() throws SAFSException{
			String debugmsg = StringUtils.debugmsg(false);
			try{
				it.winSetState(rs.getWindowsRS(), "", AutoItX.SW_SHOWMINIMIZED);
			}catch(Exception e){
				String msg = "Fail to minimize window due to Exception "+e.getMessage();
				Log.error(debugmsg+msg);
				throw new SAFSException(msg);
			}
		}
		/** <br><em>Purpose:</em> maximize
		 **/
		protected void _maximize() throws SAFSException{
			String debugmsg = StringUtils.debugmsg(false);
			try{
				it.winSetState(rs.getWindowsRS(), "", AutoItX.SW_SHOWMAXIMIZED);
			}catch(Exception e){
				String msg = "Fail to maximize window due to Exception "+e.getMessage();
				Log.error(debugmsg+msg);
				throw new SAFSException(msg);
			}
		}
	
	}
}