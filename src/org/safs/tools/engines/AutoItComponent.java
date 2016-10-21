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
 * AUG 05, 2016 SBJLWA Equipped a special DriverCommandProcessor to this engine. Implemented 'WaitForGUI'/'WaitForGUIGone'.
 * SEP 21, 2016 SCNTAX Add AutoIt workhorse click(): Allow click action combined with specified mouse button, number of click times, and clicking offset position.
 * SEP 23, 2016 SCNTAX Replace 'AutoItX' as SAFS version 'AutoItXPlus', and move 'isValidMouseButton()' to AutoItXPlus.
 * 					   Refactor workhorse 'click()' to support special key holding while clicking.
 * 					   Override the 'componentClick()' function in 'AutoItComponent.CFComponent' class to deal with groups of CLICK keywords.
 * 					   Add keyword 'DoubleClick', 'RightClick', 'CtrlClick' and 'ShiftClick' implementations.
 * OCT 21, 2016 SCNTAX Add '@Override' annotation for 'AutoItComponent.CFComponent#process()'.
 *                     Refactor 'AutoItComponent.CFComponent#process()': as all the ComponentFunction keywords will be dealt at 'compnentProcess()' in
 *                     the last 'if cause' in this 'process()', delete the 'Click' series keywords check for their duplication.
 */
package org.safs.tools.engines;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.safs.ComponentFunction;
import org.safs.DriverCommand;
import org.safs.IndependantLog;
import org.safs.Log;
import org.safs.Processor;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.TestRecordHelper;
import org.safs.autoit.AutoIt;
import org.safs.autoit.AutoItRs;
import org.safs.autoit.lib.AutoItXPlus;
import org.safs.logging.AbstractLogFacility;
import org.safs.logging.LogUtilities;
import org.safs.model.commands.DDDriverCommands;
import org.safs.model.commands.DriverCommands;
import org.safs.model.commands.EditBoxFunctions;
import org.safs.text.FAILStrings;
import org.safs.text.GENKEYS;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverInterface;


/**
 * Provides local in-process support Component Functions and Driver Commands
 * by AutoIT engine on Windows.
 * <p>
 * This engine does not assume the use of STAF. Instead, it uses the
 * various org.safs.tools Interfaces to talk with the rest of the framework (as made
 * available via the DriverInterface configuration).
 * @author Dharmesh Patel
 */
public class AutoItComponent extends GenericEngine {

	/** "SAFS/AUTOITComponent" */
	public static final String ENGINE_NAME  = "SAFS/AUTOITComponent";

	/** "AUTOITComponent" */
	public static final String AUTOITCOMPONENT_ENGINE  = "AUTOITComponent";

	/** The special AutoIT Processor for handling Component Function keywords.*/
    protected ComponentFunction cf = null;
    /** The special AutoIT Processor for handling Driver Command keywords.*/
    protected AutoItDriverCommand adc = null;
	
	/**
	 * The AutoIt instance got by AutoIt.AutoItObject().<br>
	 * This instance may be shared by multiple threads.<br>
	 * 
	 * @see #AutoItComponent()
	 */
	protected AutoItXPlus it = null;
	
	/**
	 * Constructor for AUTOITComponent.<br>
	 * It will also initialize the shared AutoIT object.<br>
	 */
	public AutoItComponent() {
		super();
		try{
			it = AutoIt.AutoItObject();
		}catch(Exception e){
			Log.error(StringUtils.debugmsg(false)+" Fail to get AutoIt Instance, due to "+StringUtils.debugmsg(e));
		}
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
		adc = new AutoItDriverCommand();
		adc.setLogUtilities(log);
	}

	public long processRecord (TestRecordHelper testRecordData){
		
		if(Processor.isComponentFunctionRecord(testRecordData.getRecordType()) ||
		   Processor.isDriverCommandRecord(testRecordData.getRecordType())){
			Log.info("AUTOITC:processing \""+ testRecordData.getCommand() +"\".");
		}else{
			Log.info("AUTOITC: only handles ComponentFunction/DriverCommand TestRecords at this time....");
			testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
			return StatusCodes.SCRIPT_NOT_EXECUTED;
		}
		
		this.testRecordData = testRecordData;
		boolean resetTRD = false;
		if (testRecordData.getSTAFHelper()==null){
		    testRecordData.setSTAFHelper(staf);
		    resetTRD = true;
		}

		if(Processor.isComponentFunctionRecord(testRecordData.getRecordType())){
			_processComponentFunction();			
		}else if(Processor.isDriverCommandRecord(testRecordData.getRecordType())){
			_processDriverCommand();						
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
		
		if(Processor.isComponentFunctionRecord(testRecordData.getRecordType())){
			_processComponentFunction();			
		}else if(Processor.isDriverCommandRecord(testRecordData.getRecordType())){
			_processDriverCommand();						
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
	
	private void _processComponentFunction(){
		Collection<String> params = interpretFields(testRecordData);
		if(params instanceof Collection){
			cf.setTestRecordData(testRecordData);			
			cf.setParams(params);	
			cf.setIterator(params.iterator());
			cf.process();
		}
	}
	
	private void _processDriverCommand(){
		String debugmsg = StringUtils.debugmsg(false);

		if(adc==null){
			adc = new AutoItDriverCommand();
		}
		if(adc.getLogUtilities()==null){
			if(staf==null){
				IndependantLog.debug(debugmsg+"STAFHelper is null, try to get the STAFHelper from the test record.");
				staf = testRecordData.getSTAFHelper();
			}
			if(staf!=null){
				log = new LogUtilities(staf);
				adc.setLogUtilities(log);
			}else{
				IndependantLog.warn(debugmsg+"STAFHelper is null, cannot initialize the Log Utilities!");
			}
		}
		
		adc.setTestRecordData(testRecordData);
		adc.process();
		
	}
	
	/**
	 * Prerequisite: {@link #it} and {@link #testRecordData} should be initialized.
	 * 
	 * @param rs AutoItRs, the recognition string representing the AutoIT component.
	 * @return	boolean, if the AutoIT component exists.
	 * @throws SAFSException
	 */
	protected boolean waitForGUI(AutoItRs rs) throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false);
		String controlHandle = null;
		int errorCode = 0;
		boolean found = false;
		
		IndependantLog.debug(debugmsg+" wait for "+testRecordData.getWinCompName());
		
		if(it.winWait(rs.getWindowsRS(), "", 1)){
			if(testRecordData.targetIsComponent()){
				controlHandle = it.controlGetHandle(rs.getWindowsRS(), "", rs.getComponentRS());
				IndependantLog.debug(debugmsg+" waited control's handle is "+controlHandle);
				if(windowHandleIsValid(controlHandle)){
					found = true;
				}else{
					found = false;
					errorCode = it.getError();
					IndependantLog.debug(debugmsg+" wait control return error code "+errorCode);
				}
			}else{
				found = true;
			}
		}else{
			found = false;
		}
		
		IndependantLog.debug(debugmsg+testRecordData.getWinCompName()+ " was "+ (found?"found.":"NOT found!"));
		
		return found;
	}
	
	private boolean windowHandleIsValid(String handle){
		//It seems that '0x00000000' is not a valid value.
		return (StringUtils.isValid(handle) && !handle.equalsIgnoreCase("0x00000000"));
	}
	
	/**************************  Handle Driver Commands **********************/
	class AutoItDriverCommand extends DriverCommand{
		
		/** AutoItRs, the object representing the component */
		protected AutoItRs rs = null;
		protected String mapname;
		protected String windowName = null;
		protected String compName = null;
		protected String winrec = null;
		protected String comprec = null;
		
		public AutoItDriverCommand(){
			super();
		}
		
		protected void localProcess(){
			String debugmsg = StringUtils.debugmsg(false);
			mapname = testRecordData.getAppMapName();

			//GUILess commands may NOT have information related to window/component, such as window/component name, recognition string
			try{
				windowName = testRecordData.getWindowName();
				compName = testRecordData.getCompName();
				winrec = testRecordData.getWindowGuiId();
				comprec = testRecordData.getCompGuiId();
				Log.info(debugmsg + "handling "+command+" for windowName: "+ windowName +"; compName: "+ compName+"; with params: "+params);
				
				if(winrec==null && StringUtils.isValid(windowName)){
					winrec = staf.getAppMapItem(mapname, windowName, windowName);
					testRecordData.setWindowGuiId(winrec);
				}
				if(comprec==null && StringUtils.isValid(windowName) && StringUtils.isValid(compName)){
					comprec = staf.getAppMapItem(mapname, windowName, compName);
					testRecordData.setCompGuiId(comprec);
				}
				
				Log.info(debugmsg + " winrec: "+ winrec +"; comprec: "+ comprec);
				
				//prepare Autoit RS
				if(StringUtils.isValid(winrec) && StringUtils.isValid(comprec)){
					rs = new AutoItRs(winrec,comprec);					
				}
			}catch(SAFSException e){
				Log.debug(debugmsg + "recognition strings missing or invalid for AutoIt engine.");
			}

		}
		
		protected void commandProcess() {
			if(DDDriverCommands.WAITFORGUI_KEYWORD.equalsIgnoreCase(command)){
				waitForGui(true);
			}else if(DDDriverCommands.WAITFORGUIGONE_KEYWORD.equalsIgnoreCase(command)){
				waitForGui(false);
			}
		}
		
		private void waitForGui(boolean waitforgui){
			if (params.size() < 2) {
				issueParameterCountFailure();
				return;
			}
			String debugmsg = StringUtils.debugmsg(false);
			int tseconds = 15;
			String timeout = String.valueOf(tseconds);
			String message = null;
			Iterator<?> iter = params.iterator();
			
			windowName = (String) iter.next();
			if(!StringUtils.isValid(windowName)){
				message = failedText.convert("bad_param", "Invalid parameter value for WINDOWID", "WINDOWID");
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
				setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
				return;
			}
			compName = (String) iter.next();
			if(!StringUtils.isValid(compName)){
				message = failedText.convert("bad_param", "Invalid parameter value for COMPONENTID", "COMPONENTID");
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
				 setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
				 return;
			}
			//Handle the optional parameter 'timeout'
			if(iter.hasNext()){
				timeout = (String) iter.next();					
				try{
					tseconds = Integer.parseInt(timeout);
					if(tseconds < 0) tseconds = 0;
				}catch(NumberFormatException nf){
					Log.debug(debugmsg+" ignoring invalid TIMEOUT value. Using Default value "+tseconds);
				}
				
				timeout = String.valueOf(tseconds).trim();
			}
			
			try { 
				boolean found = false;
				String who = windowName+":"+ compName;
				long currenttime = System.currentTimeMillis();
				long endtime = currenttime + (1000* tseconds);
				SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss.SSS");
				boolean notDone = true;
				do{
					found = waitForGUI(rs);
					// evaluate our wait success
					if (!found){
						if(!waitforgui){  // waitforguiGone
							message = genericText.convert("gone_timeout", 
									who +" was gone within timeout "+ timeout, 
									who, timeout);
							logMessage( message, null, AbstractLogFacility.GENERIC_MESSAGE);
							setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
							return;
						}
					}else{
						if(waitforgui){
							message = genericText.convert("found_timeout", 
									who +" was found within timeout "+ timeout, 
									who, timeout);
							logMessage( message, null, AbstractLogFacility.GENERIC_MESSAGE);
							setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
							return;
						}
					}
					currenttime = System.currentTimeMillis();
					if(currenttime < (endtime - 400)){
						Log.debug("WAIT 400 millisecond wait commencing at:"+ time.format(new Date()));
						try{Thread.sleep(400);}catch(InterruptedException x){}
						Log.debug("WAIT 400 millisecond complete at:"+ time.format(new Date()));
					}else{
						notDone = false;
					}
				}while(notDone);
				//not_found_timeout   :%1% was not found within timeout %2%
				//not_gone_timeout    :%1% was not gone within timeout %2%
				if(!waitforgui){  // waitforguiGone
					message = genericText.convert("not_gone_timeout", 
							who +" was not gone within timeout "+ timeout, 
							who, timeout);
				}else{ //waitforgui
					message = genericText.convert("not_found_timeout", 
							who +" was not found within timeout "+ timeout, 
							who, timeout);
				}
				logMessage( message, who, AbstractLogFacility.WARNING_MESSAGE);
				setTRDStatus(testRecordData, DriverConstant.STATUS_SCRIPT_WARNING);
				return;
			}catch(Exception a){
				message = FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
						"Support for 'AWT Robot' not found.", "AWT Robot");
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
				setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
				return;
			}
		}
	}
	
	/******************************************************
	 * Local CFComponent 
	 * @author canagl
	 ******************************************************/
	class CFComponent extends org.safs.ComponentFunction {
		/** '1', the default timeout in seconds to wait the window become focused. */
		protected int DEFAULT_TIMEOUT_WAIT_WIN_FOCUSED = 1;
		/** AutoItRs, the object representing the component */
		protected AutoItRs rs = null;
				
		CFComponent (){
			super();
		}	
		
		/**
		 * Process the record present in the provided testRecordData.
		 * 
		 * SCNTAX Note: 
		 *     Add '@Override' annotation.
		 */
		@Override
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
			if (params != null) {
				iterator = params.iterator();
				Log.info(debugmsg + " win: "+ windowName +"; comp: "+ compName+"; with params: "+params);
			} else{
				Log.info(debugmsg + " win: "+ windowName +"; comp: "+ compName+" without parameters.");
			}

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
			
			/**
			 * OCT 21, 2016  SCNTAX
			 *     As all the Component functions will be dealt at the 'compnentProcess()' 
			 *     in the final 'if' cause, here we should ONLY check keywords NOT belong to
			 *     keywords of ComponentFunction.
			 *     
			 *     Thus, delete the 'Click' series keywords check for their duplication.
			 *     
			 *     Also note, in each specified engine, we only need to provide the overrided
			 *     execution method, which will be used in 'ComponentFunction#componentProcess()'. 
			 *     Here, we've provided the overrided function 'componentClick()' in this 
			 *     class, which will be called in 'ComponentFunction#componentProcess()'. 
			 */
			if ( action.equalsIgnoreCase( DriverCommands.SETFOCUS_KEYWORD )) {
			    setFocus(rs);
			} else if ( action.equalsIgnoreCase(EditBoxFunctions.SETTEXTVALUE_KEYWORD)) {
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
		
		/**
		 * Override componentClick() to deal with groups of CLICK keywords.
		 * 
		 * @author scntax
		 */
		@Override
		protected void componentClick() throws SAFSException {
			String dbgmsg = StringUtils.getMethodName(0, false);
			Point point = checkForCoord(iterator);
			
			String autoscroll = null;
			if(iterator.hasNext()) {
				autoscroll = iterator.next();
				//TODO: SEP 23, 2016 SCNTAX, Support this parameter later. 
				throw new SAFSException(dbgmsg + "(): " + "currently NOT support 'autoscroll' parameter!");
			}
			
			/**
			 * Deal with testRecordData's StatusCode in AutoIt's click workhorse routine.
			 * Here, only record the time consuming .
			 */			
			long begin = System.currentTimeMillis();
			
			if (action.equalsIgnoreCase(ComponentFunction.CLICK) 
					|| action.equalsIgnoreCase(ComponentFunction.COMPONENTCLICK)) {
				click(rs, point);
			} else if (action.equalsIgnoreCase(ComponentFunction.DOUBLECLICK)){
				doubleClick(rs, point);
			} else if (action.equalsIgnoreCase(ComponentFunction.RIGHTCLICK)){
				rightClick(rs, point);
			} else if (action.equalsIgnoreCase(ComponentFunction.CTRLCLICK)) {
				ctrlClick(rs, point);
			} else if (action.equalsIgnoreCase(ComponentFunction.SHIFTCLICK)) {
				shiftClick(rs, point);
			}
			
			long timeConsumed = System.currentTimeMillis() - begin;
			Log.debug(dbgmsg + "(): " + "took " + timeConsumed + " milliseconds or " + (timeConsumed/1000) + " seconds to perform " + action);
		}
		
		/**
		 * Click with position offset.
		 * 
		 * @param autoArgs		AutoItRs,	AutoIt engine's recognition string.
		 * @param offset		Point,		the offset position to click within the target component. Also can be null.
		 * 
		 * @author scntax
		 */
		protected void click(AutoItRs autoArgs, Point offset) {
			click(autoArgs, AutoItXPlus.AUTOIT_MOUSE_BUTTON_LEFT, 1, offset, null);
		}
		
		/**
		 * Double click with position offset.
		 * @param autoArgs		AutoItRs,	AutoIt engine's recognition string.
		 * @param offset		Point,		the offset position to click within the target component. Also can be null.
		 * 
		 * @author scntax
		 */
		protected void doubleClick(AutoItRs autoArgs, Point offset) {
			click(autoArgs, AutoItXPlus.AUTOIT_MOUSE_BUTTON_LEFT, 2, offset, null);
		}
		
		/**
		 * Right click with position offset.
		 * @param autoArgs		AutoItRs,	AutoIt engine's recognition string.
		 * @param offset		Point,		the offset position to click within the target component. Also can be null.
		 * 
		 * @author scntax
		 */
		protected void rightClick(AutoItRs autoArgs, Point offset) {
			click(autoArgs, AutoItXPlus.AUTOIT_MOUSE_BUTTON_RIGHT, 1, offset, null);
		}
		
		/**
		 * Ctrl click with position offset.
		 * @param autoArgs		AutoItRs,	AutoIt engine's recognition string.
		 * @param offset		Point,		the offset position to click within the target component. Also can be null.
		 * 
		 * @author scntax
		 */
		protected void ctrlClick(AutoItRs autoArgs, Point offset) {
			click(autoArgs, AutoItXPlus.AUTOIT_MOUSE_BUTTON_LEFT, 1, offset, AutoItXPlus.AUTOIT_SUPPORT_PRESS_CTRL);
		}
		
		/**
		 * Shift click with position offset.
		 * @param autoArgs		AutoItRs,	AutoIt engine's recognition string.
		 * @param offset		Point,		the offset position to click within the target component. Also can be null.
		 * 
		 * @author scntax
		 */
		protected void shiftClick(AutoItRs autoArgs, Point offset) {
			click(autoArgs, AutoItXPlus.AUTOIT_MOUSE_BUTTON_LEFT, 1, offset, AutoItXPlus.AUTOIT_SUPPORT_PRESS_SHIFT);
		}
		
		/**
		 * Workhorse of AutoIt click routine.
		 * It allows us to use specified 'mouse button' to click target component at assigned position with a number of times.  
		 * 
		 * Note: at current stage, we don't support the parameter 'text' in AutoIt's API controlClick: https://www.autoitscript.com/autoit3/docs/functions/ControlClick.htm .
		 *       We just treat parameter 'text' as empty string. 
		 *  
		 * @param autoArgs		AutoItRs,	AutoIt engine's recognition string.
		 * @param mouseButton	String,		the button to click, "left", "right", or "middle". Default is the left button, which means if mouseButton is null or empty,
		 * 									it'll use the "left" as its value.
		 * @param nClicks		int,		number of times to click the mouse.
		 * @param offset		Point,		the offset position to click within the target component. Also can be null.
		 * @param specialKey	String,		keyboard key that be hold when click action happening.
		 * 
		 * @author scntax
		 * 
		 */
		protected void click(AutoItRs autoArgs, String mouseButton, int nClicks, Point offset, String specialKey) {
			String dbgmsg = StringUtils.getMethodName(0, false);
			testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
			
			/**
			 * Only need to check the autoArgs parameter, the remaining 
			 * parameters will be checked in AutoItXPlus.controlClick().
			 */
			if(autoArgs == null) {
				issueParameterCountFailure(dbgmsg + "(): invalid AutoIt parameters provided!");
				return;
			}
			
			try{
				boolean rc = it.click(autoArgs.getWindowsRS(), "", autoArgs.getComponentRS(), mouseButton, nClicks, offset, specialKey);
				
				if (rc){
					testRecordData.setStatusCode(StatusCodes.OK);
					
					String altText = windowName + ":" + compName + " " + action + " successful.";
					String msg = genericText.convert(GENKEYS.SUCCESS_3, altText, windowName, compName, action);
					log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
				} else{
					testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
					issueErrorPerformingAction(dbgmsg + "(): failed with rc = " + it.getError());
				}
				
			} catch(Exception x){
				issueErrorPerformingAction(dbgmsg + "(): " + x.getMessage());
			}
			
		}
		
		/** click **/
		/** 
		 * SEP 23, 2016 SCNTAX,	It's better to use the same click() workhorse for consistency. 
		 * 						Depreciate this original implementation.
		 */
		@Deprecated
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
				it.winSetState(rs.getWindowsRS(), "", AutoItXPlus.SW_SHOWNORMAL);
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
				it.winSetState(rs.getWindowsRS(), "", AutoItXPlus.SW_SHOWMINIMIZED);
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
				it.winSetState(rs.getWindowsRS(), "", AutoItXPlus.SW_SHOWMAXIMIZED);
			}catch(Exception e){
				String msg = "Fail to maximize window due to Exception "+e.getMessage();
				Log.error(debugmsg+msg);
				throw new SAFSException(msg);
			}
		}
	
		protected Rectangle getComponentRectangle(){
			return getComponentRectangleOnScreen();
		}
		
		protected Rectangle getComponentRectangleOnScreen(){
			String debugmsg = StringUtils.debugmsg(false);
			Rectangle rectangle = null;
			try{
				int x = it.winGetPosX(rs.getWindowsRS(), "");
				int y = it.winGetPosY(rs.getWindowsRS(), "");
				int width = 0;
				int height = 0;
				
				if(rs.isWindow()){
					width = it.winGetPosWidth(rs.getWindowsRS(), "");
					height = it.winGetPosHeight(rs.getWindowsRS(), "");
				}else{
					width = it.controlGetPosWidth(rs.getWindowsRS(), "", rs.getComponentRS());
					height = it.controlGetPosHeight(rs.getWindowsRS(), "", rs.getComponentRS());
					int controlX = it.controlGetPosX(rs.getWindowsRS(), "", rs.getComponentRS());
					int controlY = it.controlGetPosY(rs.getWindowsRS(), "", rs.getComponentRS());
					x += controlX;
					y += controlY;
					//controlX and controlY are the control's position relative to the window, BUT the 
					//value returned seems not accurate, there are some pixel difference!
					x +=7;// the controlX returned by AUTOIT is not correct, there is about 7 pixel difference. 
					y +=50;// the controlY returned by AUTOIT is not correct, there is about 50 pixel difference.
					it.controlFocus(rs.getWindowsRS(), "", rs.getComponentRS());
				}
				
				rectangle = new Rectangle(x, y, width, height);
			}catch(Exception e){
				IndependantLog.warn(debugmsg+"Fail to get component rectangle on screen, due to "+StringUtils.debugmsg(e));
			}
			
		    if(rectangle==null) IndependantLog.warn(debugmsg+"Fail to get bounds for "+ windowName+":"+compName +" on screen.");
		    return rectangle;
		}
	}
}