package org.safs.tools.engines;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.safs.ComponentFunction;
import org.safs.IndependantLog;
import org.safs.Log;
import org.safs.RSA;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.TestRecordHelper;
import org.safs.TestStepProcessor;
import org.safs.autoit.AutoIt;
import org.safs.autoit.AutoItRs;
import org.safs.image.ImageUtils;
import org.safs.logging.AbstractLogFacility;
import org.safs.logging.LogUtilities;
import org.safs.model.commands.DriverCommands;
import org.safs.model.commands.EditBoxFunctions;
import org.safs.model.commands.GenericMasterFunctions;
import org.safs.text.FAILStrings;
import org.safs.text.FileUtilities;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverInterface;
import org.safs.tools.engines.GenericEngine;
import org.safs.tools.ocr.OCREngine;
import org.safs.tools.stringutils.StringUtilities;

import autoitx4java.AutoItX;

/**
 * Provides local in-process support for AutoIt Component Functions on Windows.
 * <p>
 * This engine does not assume the use of STAF. Instead, it uses the
 * various org.safs.tools Interfaces to talk with the rest of the framework (as made
 * available via the DriverInterface configuration).
 */
public class AutoItComponent extends GenericEngine {

	/** "SAFS/AUTOITComponent" */
	static final String ENGINE_NAME  = "SAFS/AUTOITComponent";

	/** "AUTOITComponent" */
	static final String AUTOITCOMPONENT_ENGINE  = "AUTOITComponent";

	// START: LOCALLY SUPPORTED COMMANDS
	
	/** "SetFocus" */
    static final String COMMAND_SETFOCUS		       = "SetFocus";       

	/** "Click" */
    static final String COMMAND_CLICK 			       = "Click";        

    /** "SetTextValue" */
    static public final String SETTEXTVALUE_KEYWORD = EditBoxFunctions.SETTEXTVALUE_KEYWORD;
	       
	// END: LOCALLY SUPPORTED COMMANDS

	ComponentFunction cf;
	
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
		
		CFComponent (){
			super();			
		}	
		
		
		/**
		 * Process the record present in the provided testRecordData.
		 */
		public void process(){
			String debugmsg = getClass().getName()+".process(): ";
			updateFromTestRecordData();
			
			try {
			
				String winrec = testRecordData.getWindowGuiId();
				String comprec = testRecordData.getCompGuiId();
				if (!AutoItRs.isAutoitBasedRecognition(winrec)){
					Log.info(debugmsg + " processing " + action + " for AutoIt Engine.");
		        	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
		        	return;
		        }
				Log.info(debugmsg + " processing '"+action+"'; win: "+ windowName +"; comp: "+ compName+"; with params: "+params);

				// prepare Autoit RS
				AutoItRs rs = new AutoItRs(winrec,comprec);
				
				if ( action.equalsIgnoreCase( COMMAND_CLICK )){		                
					click(rs);	                
				} else if ( action.equalsIgnoreCase( COMMAND_SETFOCUS )) {
				    setFocus(rs);
				} else if ( action.equalsIgnoreCase(SETTEXTVALUE_KEYWORD)) {
					setText(rs);
				}
	             
			}catch(SAFSException x){
				Log.info( debugmsg + " exception \""+ x.getMessage() +"\".");
	  			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	  		    log.logMessage(testRecordData.getFac(),
	  		                   " SAFSException: " + x.toString(),
	  		                   FAILED_MESSAGE);
		    }
		}
		
		
		
		
		/** setfoucs **/
		protected void setFocus(AutoItRs ars){
			testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );	 
			try {
					AutoItX it = AutoIt.AutoItObject();
					if (!it.winExists(ars.getTitle())){
						testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );	 
						this.issueErrorPerformingActionOnX("SetFocus","failed due to the windows didn't found");
						return;
					}
					it.winActivate(ars.getTitle());
					
					testRecordData.setStatusCode(StatusCodes.OK);
					// log success message and status
					String altText = windowName +":"+ compName + " "+ action +" successful.";
					String msg = genericText.convert("success3", altText, windowName, compName, action);
					log.logMessage(testRecordData.getFac(),msg, PASSED_MESSAGE);
					
			} catch (Exception x) {
				this.issueErrorPerformingActionOnX("SetFocus", x.getClass().getSimpleName()+": "+ x.getMessage());
			}		
		}
		
		/** click **/
		protected void click(AutoItRs ars){
			testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );	 
			try {
					AutoItX it = AutoIt.AutoItObject();
					
					if (!it.winExists(ars.getTitle())){
						testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );	 
						this.issueErrorPerformingActionOnX("Click","failed due to the windows didn't found");
						return;
					}
							
					
					it.controlClick(ars.getTitle(), ars.getText(), ars.getControl());
					
					
					testRecordData.setStatusCode(StatusCodes.OK);
					// log success message and status
					String altText = windowName +":"+ compName + " "+ action +" successful.";
					String msg = genericText.convert("success3", altText, windowName, compName, action);
					log.logMessage(testRecordData.getFac(),msg, PASSED_MESSAGE);
					
			} catch (Exception x) {
				this.issueErrorPerformingActionOnX("Click", x.getClass().getSimpleName()+": "+ x.getMessage());
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

		   		AutoItX it = AutoIt.AutoItObject();
				
				if (!it.winExists(ars.getTitle())){
					testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );	 
					this.issueErrorPerformingActionOnX("SetText","failed due to the windows didn't found");					
					return;
				}
				
				boolean rc = it.ControlSetText(ars.getTitle(), ars.getText(), ars.getControl(), text);
				if (rc) {
			   	testRecordData.setStatusCode(StatusCodes.OK);
				// log success message and status
				log.logMessage(testRecordData.getFac(), 
						genericText.convert("success3a", windowName +":"+ compName + " "+ action
								+" successful using "+ text, windowName, compName, action, text), 
						PASSED_MESSAGE);
				} else {
					testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );	 
					this.issueErrorPerformingActionOnX("SetText","failed with rc= " + it.getError());	
				}
		   
		   	}catch(Exception x){
		   		this.issueErrorPerformingActionOnX(text, x.getClass().getSimpleName()+": "+ x.getMessage());
		   	}
		}
	
	}
}

