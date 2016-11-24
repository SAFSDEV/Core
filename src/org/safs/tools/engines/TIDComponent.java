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
import java.util.Map;
import java.util.StringTokenizer;

import org.safs.ComponentFunction;
import org.safs.Log;
import org.safs.RSA;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.TestRecordHelper;
import org.safs.TestStepProcessor;
import org.safs.image.ImageUtils;
import org.safs.logging.AbstractLogFacility;
import org.safs.logging.LogUtilities;
import org.safs.model.commands.DriverCommands;
import org.safs.model.commands.GenericMasterFunctions;
import org.safs.model.commands.TIDRestFunctions;
import org.safs.rest.REST;
import org.safs.rest.service.Headers;
import org.safs.rest.service.Response;
import org.safs.rest.service.Service;
import org.safs.rest.service.Services;
import org.safs.text.FAILKEYS;
import org.safs.text.FAILStrings;
import org.safs.text.FileUtilities;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverInterface;
import org.safs.tools.ocr.OCREngine;
import org.safs.tools.stringutils.StringUtilities;

/**
 * Provides local in-process support for GUIless Component Functions.
 * <p>
 * These are Component Functions that do not actually reference any GUI component.  
 * Some examples of these are commands like VerifyValues and certain database commands 
 * that allow the Window and Component references to be "Anything" "At All".
 * <p>
 * This engine does not assume the use of STAF. Instead, it uses the
 * various org.safs.tools Interfaces to talk with the rest of the framework (as made
 * available via the DriverInterface configuration).
 * <BR> CANAGL  MAR 25, 2009 Adding IBT support for mouse Drag operations and Click with coordinates.
 * <BR> CANAGL  JUL 14, 2009 Fixed some logging issues in VerifyValues commands.
 * <BR> LeiWang APR 07, 2010 Modify method CFComponent.process(): If the parent RS is specified in
 * 							 OBT format, then set record status to SCRIPT_NOT_EXECUTED and return. Let 
 * 							 other engine (ex. RJ engine) to handle the parent RS.
 * 
 * 							 Add method processIndependently(): If the parent RS is in OBT format, other 
 * 							 engine (ex. RJ engine) will handle the parent RS, and inside that engine, a
 * 							 new TIDComponent object will be instantiated, you should pass the test-record 
 * 							 to the method processIndependently() of this new object.
 * <BR> JunwuMa APR 14, 2010 Adding IBT support for GetTextFromGUI and SaveTextFromGUI. 
 *                           Move setRectVars() to its super(), ComponentFunction. 
 * <br>	LeiWang APR 20, 2010 Modify method getSaveTextFromGUI(): use static method of OCREngine to get
 *                           an OCR engine to use.                          
 * <br>	CANAGL MAY 10, 2012  Modify process() call to isMixedUse to gracefully handle missing App Map 
 *                           recognition entries.
 */
public class TIDComponent extends GenericEngine {

	/** "SAFS/TIDComponent" */
	static final String ENGINE_NAME  = "SAFS/TIDComponent";

	/** "TIDComponent" */
	static final String TIDCOMPONENT_ENGINE  = "TIDComponent";

	/** "SAFSREST" */
	public static final String FLAG_SAFSREST = "SAFSREST";
    
	// START: LOCALLY SUPPORTED COMMANDS

	/** "VerifyValues" */
	static final String COMMAND_CLEARAPPMAPCACHE       = "ClearAppMapCache";

	/** "VerifyValues" */
	static final String COMMAND_VERIFYCLIPBOARDTOFILE  = "VerifyClipboardToFile";

	/** "VerifyValues" */
	static final String COMMAND_VERIFYVALUES           = "VerifyValues";

	/** "VerifyValuesNotEqual" */
	static final String COMMAND_VERIFYVALUESNOTEQUAL   = "VerifyValuesNotEqual";

	/** "VerifyValuesIgnoreCase" */
	static final String COMMAND_VERIFYVALUESIGNORECASE = "VerifyValuesIgnoreCase";

	/** "VerifyValueContains" */
	static final String COMMAND_VERIFYVALUECONTAINS    = "VerifyValueContains";

	/** "VerifyValueContainsIgnoreCase" */
	static final String COMMAND_VERIFYVALUECONTAINSIGNORECASE = "VerifyValueContainsIgnoreCase";

	/** "VerifyValueDoesNotContain" */
	static final String COMMAND_VERIFYVALUEDOESNOTCONTAIN = "VerifyValueDoesNotContain";

	/** "VerifyValueEquals" */
	static final String COMMAND_VERIFYVALUEEQUALS      = "VerifyValueEquals";

	/** "VerifyBinaryFileToFile" */
    static final String COMMAND_VERIFYBINARYFILETOFILE = "VerifyBinaryFileToFile";    

	/** "VerifyFileToFile" */
    static final String COMMAND_VERIFYFILETOFILE       = "VerifyFileToFile";

	/** "VerifyTextFileToFile" */
    static final String COMMAND_VERIFYTEXTFILETOFILE   = "VerifyTextFileToFile";        

	/** "Click" */
    static final String COMMAND_CLICK 			       = "Click";        

	/** "CtrlClick" */
    static final String COMMAND_CTRLCLICK 			   = "CtrlClick";        

	/** "ShiftClick" */
    static final String COMMAND_SHIFTCLICK 			   = "ShiftClick";        

	/** "MultiClick" */
    static final String COMMAND_MULTICLICK 			   = "MultiClick";        

	/** "ClickScreenImage" */
    static final String COMMAND_CLICKSCREENIMAGE       = "ClickScreenImage";        

	/** "CtrlClickScreenImage" */
    static final String COMMAND_CTRLCLICKSCREENIMAGE   = "CtrlClickScreenImage";        

	/** "ShiftClickScreenImage" */
    static final String COMMAND_SHIFTCLICKSCREENIMAGE   = "ShiftClickScreenImage";        

	/** "RightClick" */
    static final String COMMAND_RIGHTCLICK             = "RightClick";        

	/** "CtrlRightClick" */
    static final String COMMAND_CTRLRIGHTCLICK         = "CtrlRightClick";        

	/** "LeftDrag" */
    static final String COMMAND_LEFTDRAG               = "LeftDrag";        

	/** "RightDrag" */
    static final String COMMAND_RIGHTDRAG              = "RightDrag";        

	/** "RightClickScreenImage" */
    static final String COMMAND_RIGHTCLICKSCREENIMAGE  = "RightClickScreenImage";        

	/** "CtrlRightClickScreenImage" */
    static final String COMMAND_CTRLRIGHTCLICKSCREENIMAGE  = "CtrlRightClickScreenImage";        

	/** "DoubleClick" */
    static final String COMMAND_DOUBLECLICK            = "DoubleClick";        

	/** "DoubleClickScreenImage" */
    static final String COMMAND_DOUBLECLICKSCREENIMAGE = "DoubleClickScreenImage";        

	/** "MultiClickScreenImage" */
    static final String COMMAND_MULTICLICKSCREENIMAGE = "MultiClickScreenImage";        

	/** "LocateScreenImage" */
    static final String COMMAND_LOCATESCREENIMAGE      = "LocateScreenImage";        

	/** "GetGUIImage" */
    static final String COMMAND_GETGUIIMAGE		       = "GetGUIImage";        

	/** "HoverMouse" */
    static final String COMMAND_HOVERMOUSE		       = "HoverMouse";        

	/** "VerifyGUIImageToFile" */
    static final String COMMAND_VERIFYGUIIMAGETOFILE   = "VerifyGUIImageToFile";        

    /** "GUIDoesExist" */
    static final String COMMAND_GUIDOESEXIST	       = "GUIDoesExist";        

	/** "GUIDoesNotExist" */
    static final String COMMAND_GUIDOESNOTEXIST	       = "GUIDoesNotExist";        

	/** "ClickScreenPoint" */
    static final String COMMAND_CLICKSCREENPOINT       = "ClickScreenPoint";        

	/** "RightClickScreenPoint" */
    static final String COMMAND_RIGHTCLICKSCREENPOINT  = "RightClickScreenPoint";        

	/** "DoubleClickScreenPoint" */
    static final String COMMAND_DOUBLECLICKSCREENPOINT = "DoubleClickScreenPoint";        

	/** "HoverScreenLocation" */
    static final String COMMAND_HOVERSCREENLOCATION       = "HoverScreenLocation";        

    /** "ClickScreenLocation" */
    static final String COMMAND_CLICKSCREENLOCATION       = "ClickScreenLocation";        

	/** "RightClickScreenLocation" */
    static final String COMMAND_RIGHTCLICKSCREENLOCATION  = "RightClickScreenLocation";        

	/** "DoubleClickScreenLocation" */
    static final String COMMAND_DOUBLECLICKSCREENLOCATION = "DoubleClickScreenLocation";        

	/** "TypeKeys" */
    static final String COMMAND_TYPEKEYS		       = "TypeKeys";        

	/** "InputKeys" */
    static final String COMMAND_INPUTKEYS		       = "InputKeys";        

    /** "TypeChars" */
    static final String COMMAND_TYPECHARS	       	   = "TypeChars";        

    /** "TypeEncryption" */
    static final String COMMAND_TYPEENCRYPTION	       = GenericMasterFunctions.TYPEENCRYPTION_KEYWORD;

	/** "InputCharacterss" */
    static final String COMMAND_INPUTCHARS	       	   = "InputCharacters";     
    
	/** "GetTextFromGUI" */
    static final String COMMAND_GETTEXTFROMGUI     	   = "GetTextFromGUI";   
    
	/** "SaveTextFromGUI" */
    static final String COMMAND_SAVETEXTFROMGUI    	   = "SaveTextFromGUI";     

    /** "SuppressValue" */
    static final String PARAM_SUPPRESSVALUE      	   = "SuppressValue";     
    
	// END: LOCALLY SUPPORTED COMMANDS

    /** The special Processor for handling TID Component Function keywords.*/
    protected ComponentFunction cf;
	
	/** The special Processor for handling REST Component Function keywords.*/
    protected ComponentFunction restCF = null;
    
    /** The map holding the response object returned from a rest service. */
    private static Map<String, Response> responseMap = new HashMap<String, Response>();
    
	/**
	 * Constructor for TIDComponent
	 */
	public TIDComponent() {
		super();
		servicename = ENGINE_NAME;
	}

	/**
	 * PREFERRED Constructor for TIDComponent.
	 * Constructs the instance and calls launchInterface to initialize.
	 */
	public TIDComponent(DriverInterface driver) {
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
		
		restCF = new RESTComponent();
		restCF.setLogUtilities(log);
	}

	public long processRecord (TestRecordHelper testRecordData){
		
		Log.info("TIDC:processing \""+ testRecordData.getCommand() +"\".");
		this.testRecordData = testRecordData;
		boolean resetTRD = false;
		if (testRecordData.getSTAFHelper()==null){
		    testRecordData.setSTAFHelper(staf);
		    resetTRD = true;
		}
		Collection<String> params = interpretFields(testRecordData);
		if(params instanceof Collection){
			boolean restful = false;
			try {
				restful = isRESTFunction(testRecordData.getWindowGuiId());
			} catch (SAFSException e) {
			}
			if(!restful){
				try {
					restful = isRESTFunction(testRecordData.getWindowName());
				} catch (SAFSException e) {
				}
			}
			if(restful){
				restCF.setTestRecordData(testRecordData);			
				restCF.setParams(params);	
				restCF.setIterator(params.iterator());
				restCF.process();
			}
		
			if(StatusCodes.SCRIPT_NOT_EXECUTED==testRecordData.getStatusCode()){
				cf.setTestRecordData(testRecordData);			
				cf.setParams(params);	
				cf.setIterator(params.iterator());
				cf.process();
			}
		}
		
		if(resetTRD) testRecordData.setSTAFHelper(null);
		return testRecordData.getStatusCode();
	}

	/**
	 * Note:	Now this method is called only by a special processor CFTIDComponent
	 * 			Before calling this method, you must make sure the test record data
	 * 			is correctly initialized.
	 * @param testRecordData
	 */
	public void processIndependently(TestRecordHelper testRecordData){
		String debugmsg = getClass().getName()+".processIndependently(): ";
		Log.info("TIDC:processing \""+ testRecordData.getCommand() +"\" independently.");
		this.testRecordData = testRecordData;
		cf = new CFComponent();
		//As from a processor we call constructor TIDComponent(), not TIDComponent(DriverInterface),
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
	
	
	/**
	 * @param responseID String, the unique ID to identify the rest service Response.
	 * @return Response, a cached Response returned from a rest service.
	 */
	public static synchronized Response getResponseMapValue(String responseID) {
		return responseMap.get(responseID);
	}

	/**
	 * @param responseID String, the unique ID to identify the rest service Response.
	 * @param response Response, a Response object to be cached into a map.
	 * @return response Response, the previous Response stored in the map with the same responseID; 
	 *                            or null if no Response was previously stored with the key responseID.
	 */
	public static synchronized Response addToResponseMap(String responseID, Response response) {
		if(responseMap.containsKey(responseID)){
			Log.warn("responseMap has alreday contained ID "+responseID);
		}
		if(responseMap.containsValue(response)){
			Log.warn("responseMap has alreday contained Response "+ response);
		}
		
		return responseMap.put(responseID, response);
	}

	/******************************************************
	 * Local CFComponent 
	 * @author canagl
	 ******************************************************/
	class CFComponent extends org.safs.ComponentFunction {
		
		CFComponent (){
			super();			
		}
		
		protected void snapshotScreen(){
			try{
				Log.info("TID CFComponent initiating a SCREEN SNAPSHOT...");
				ImageUtils.recaptureScreen();
			}catch(Exception x){
				Log.debug("TID CFComponent IGNORING screen snapshot failure.", x);
			}
		}
		
		/**
		 * Process the record present in the provided testRecordData.
		 */
		public void process(){
			String debugmsg = getClass().getName()+".process(): ";
			updateFromTestRecordData();
			Log.info("TIDC:processing '"+action+"'; win: "+ windowName +"; comp: "+ compName+"; with params: "+params);
			// preset failure
			try{
				//For those GUI related keywords: they may use mixed mode RS.
				//Firstly, we need to deal with the mixed Recognition String.
				//If the parent is specified in OBT format RS, we will set
				//test record status to SCRIPT_NOT_EXECUTED so other engine will treat it.
				if( action.equalsIgnoreCase(COMMAND_CLICKSCREENIMAGE) ||
				    action.equalsIgnoreCase(COMMAND_RIGHTCLICKSCREENIMAGE) ||
				    action.equalsIgnoreCase(COMMAND_CTRLCLICKSCREENIMAGE) ||
				    action.equalsIgnoreCase(COMMAND_CTRLRIGHTCLICKSCREENIMAGE) ||
				    action.equalsIgnoreCase(COMMAND_SHIFTCLICKSCREENIMAGE) ||
					action.equalsIgnoreCase(COMMAND_DOUBLECLICKSCREENIMAGE) ||
					action.equalsIgnoreCase(COMMAND_MULTICLICKSCREENIMAGE) ||
					action.equalsIgnoreCase(COMMAND_CLICK) ||
					action.equalsIgnoreCase(COMMAND_RIGHTCLICK) ||
					action.equalsIgnoreCase(COMMAND_DOUBLECLICK) ||
					action.equalsIgnoreCase(COMMAND_CTRLCLICK) ||
					action.equalsIgnoreCase(COMMAND_CTRLRIGHTCLICK) ||
					action.equalsIgnoreCase(COMMAND_SHIFTCLICK) ||
					action.equalsIgnoreCase(COMMAND_MULTICLICK) ||
					action.equalsIgnoreCase(COMMAND_HOVERMOUSE ) ||
					action.equalsIgnoreCase(COMMAND_LOCATESCREENIMAGE) ||
					action.equalsIgnoreCase(COMMAND_GETGUIIMAGE) ||
					action.equalsIgnoreCase(COMMAND_VERIFYGUIIMAGETOFILE) ||
					action.equalsIgnoreCase(COMMAND_GUIDOESEXIST) ||
					action.equalsIgnoreCase(COMMAND_GUIDOESNOTEXIST) ||
					action.equalsIgnoreCase(COMMAND_INPUTKEYS) ||
					action.equalsIgnoreCase(COMMAND_INPUTCHARS) ||
					action.equalsIgnoreCase(COMMAND_LEFTDRAG) ||
					action.equalsIgnoreCase(COMMAND_RIGHTDRAG) ||
					action.equalsIgnoreCase(COMMAND_GETTEXTFROMGUI) ||
					action.equalsIgnoreCase(COMMAND_SAVETEXTFROMGUI)){
					
					// TODO isMixedRsUsed() may throw out a SAFSException, if the
					// exception occurs, we are going to assume this could be a 
					// "CurrentWindow" type of recognition String, or an 
					// "Anything", "AtAll" type of recognition.
                    try{
                    	if (testRecordData.isMixedRsUsed()){	                    
	                        Log.info(debugmsg+" Recognition String is mixed mode, let dynamic engine to processe top window.");
	                        testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
	                        return;
                    	}
                    }catch(SAFSException x){  // normally means App Map entry not present
                    	// we definitely don't process a missing app map string,
                    	// but this could be a "CurrentWindow" type of recognition String, 
                    	// or an "Anything", "AtAll" type of recognition.
                   		try{ 
                   			if(testRecordData.getWindowGuiId() == null);
                   			testRecordData.setWindowGuiId(windowName);
                   		}catch(Exception x2){ testRecordData.setWindowGuiId(windowName); }
                   		try{
                   			if(testRecordData.getCompGuiId() == null); 
                   			testRecordData.setCompGuiId(compName);
                   		}catch(Exception x2){ testRecordData.setCompGuiId(compName); }
                    }
				}
				
			    if (action.equalsIgnoreCase(COMMAND_VERIFYVALUES) ||
		        	action.equalsIgnoreCase(COMMAND_VERIFYVALUESNOTEQUAL) ||
		        	action.equalsIgnoreCase(COMMAND_VERIFYVALUEDOESNOTCONTAIN) ||
		        	action.equalsIgnoreCase(COMMAND_VERIFYVALUECONTAINS) ||
		        	action.equalsIgnoreCase(COMMAND_VERIFYVALUECONTAINSIGNORECASE) ||
		        	action.equalsIgnoreCase(COMMAND_VERIFYVALUEEQUALS) ||
		            action.equalsIgnoreCase(COMMAND_VERIFYVALUESIGNORECASE)) {
		            verifyValues();
                } else if ( action.equalsIgnoreCase( COMMAND_VERIFYFILETOFILE ) || 
                            action.equalsIgnoreCase( COMMAND_VERIFYTEXTFILETOFILE ) ) {
                    verifyFileToFile( true );
                } else if ( action.equalsIgnoreCase( COMMAND_VERIFYBINARYFILETOFILE ) ) {
                    verifyFileToFile ( false );
                } else if ( action.equalsIgnoreCase( COMMAND_CLICKSCREENIMAGE ) || 
                            action.equalsIgnoreCase( COMMAND_RIGHTCLICKSCREENIMAGE )||
                            action.equalsIgnoreCase( COMMAND_DOUBLECLICKSCREENIMAGE )||
                            action.equalsIgnoreCase( COMMAND_CTRLCLICKSCREENIMAGE )||
                            action.equalsIgnoreCase( COMMAND_CTRLRIGHTCLICKSCREENIMAGE )||
                            action.equalsIgnoreCase( COMMAND_SHIFTCLICKSCREENIMAGE )||
                            action.equalsIgnoreCase( COMMAND_MULTICLICKSCREENIMAGE )) {
                    String winrec = testRecordData.getCompGuiId();
                    snapshotScreen();
           			clickScreenImage( winrec );
                } else if ( action.equalsIgnoreCase( COMMAND_CLICKSCREENLOCATION ) || 
                        action.equalsIgnoreCase( COMMAND_RIGHTCLICKSCREENLOCATION )||
                        action.equalsIgnoreCase( COMMAND_DOUBLECLICKSCREENLOCATION )||
                        action.equalsIgnoreCase( COMMAND_HOVERSCREENLOCATION )) {
                	clickHoverScreenLocation( );
                } else if ( action.equalsIgnoreCase( COMMAND_CLICK )||
                        action.equalsIgnoreCase( COMMAND_RIGHTCLICK )||
                        action.equalsIgnoreCase( COMMAND_DOUBLECLICK ) ||
                        action.equalsIgnoreCase( COMMAND_SHIFTCLICK )||
                        action.equalsIgnoreCase( COMMAND_CTRLCLICK )||
                        action.equalsIgnoreCase( COMMAND_CTRLRIGHTCLICK )||
                        action.equalsIgnoreCase( COMMAND_MULTICLICK ) ) {
                	String winrec = testRecordData.getCompGuiId();
                    // if not image-based let another engine handle it
        	        if(! ImageUtils.isImageBasedRecognition(winrec)){
        	        	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
        	        	return;
        	        }                	
                    snapshotScreen();
                	clickScreenImage( winrec );
                } else if ( action.equalsIgnoreCase( COMMAND_HOVERMOUSE )){
                	String winrec = testRecordData.getCompGuiId();
                    // if not image-based let another engine handle it
        	        if(! ImageUtils.isImageBasedRecognition(winrec)){
        	        	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
        	        	return;
        	        }                	
                    snapshotScreen();
                	hoverScreenImage( winrec );
                } else if ( action.equalsIgnoreCase( COMMAND_LOCATESCREENIMAGE ) ) {
                	String winrec = testRecordData.getCompGuiId();
                    // if not image-based let another engine handle it
        	        if(! ImageUtils.isImageBasedRecognition(winrec)){
        	        	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
        	        	return;
        	        } 
                    snapshotScreen();
                    locateScreenImage ( );
                } else if ( action.equalsIgnoreCase( COMMAND_GETGUIIMAGE ) ) {
                	String winrec = testRecordData.getCompGuiId();
                    // if not image-based let another engine handle it
        	        if(! ImageUtils.isImageBasedRecognition(winrec)){
        	        	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
        	        	return;
        	        }                	
                    snapshotScreen();
                    action_getGuiImage ( );
                } else if ( action.equalsIgnoreCase( COMMAND_VERIFYGUIIMAGETOFILE ) ) {
                	String winrec = testRecordData.getCompGuiId();
                    // if not image-based let another engine handle it
        	        if(! ImageUtils.isImageBasedRecognition(winrec)){
        	        	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
        	        	return;
        	        }                	
                    snapshotScreen();
                    action_verifyGuiImageToFile ( );
                } else if (( action.equalsIgnoreCase( COMMAND_GUIDOESEXIST ))||
                		   ( action.equalsIgnoreCase( COMMAND_GUIDOESNOTEXIST ))){
                	String winrec = testRecordData.getCompGuiId();
                    // if not image-based let another engine handle it
        	        if(! ImageUtils.isImageBasedRecognition(winrec)){
        	        	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
        	        	return;
        	        }                	
                    snapshotScreen();
                    guiDoesExist ();
                } else if ( action.equalsIgnoreCase( COMMAND_CLICKSCREENPOINT ) || 
                        action.equalsIgnoreCase( COMMAND_RIGHTCLICKSCREENPOINT )||
                        action.equalsIgnoreCase( COMMAND_DOUBLECLICKSCREENPOINT )) {
                    clickScreenPoint ( );
                } else if ( action.equalsIgnoreCase( COMMAND_TYPEKEYS ) ||
                		    action.equalsIgnoreCase( COMMAND_TYPECHARS)){
                    inputKeys ( );
                } else if (( action.equalsIgnoreCase( COMMAND_INPUTKEYS ) )||
                		( action.equalsIgnoreCase( COMMAND_INPUTCHARS ))){
                    String winrec = testRecordData.getCompGuiId();
                    // if not image-based let another engine handle it
        	        if(! ImageUtils.isImageBasedRecognition(winrec)){
        	        	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
        	        	return;
        	        }                	
                    inputKeys ( );
                }else if ( action.equalsIgnoreCase(COMMAND_TYPEENCRYPTION)){
                	inputEncryptions ( );
                	
                } else if ( action.equalsIgnoreCase( COMMAND_LEFTDRAG )||
                        action.equalsIgnoreCase( COMMAND_RIGHTDRAG ) ) {
                	String winrec = testRecordData.getCompGuiId();
                    // if not image-based let another engine handle it
        	        if(! ImageUtils.isImageBasedRecognition(winrec)){
        	        	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
        	        	return;
        	        }                	
                    snapshotScreen();
                    mouseDrag( winrec );
                } else if ( action.equalsIgnoreCase(COMMAND_GETTEXTFROMGUI)||
                			action.equalsIgnoreCase(COMMAND_SAVETEXTFROMGUI) ) {
                   	String winrec = testRecordData.getCompGuiId(); 
                	// if not image-based let another engine handle it
        	        if(! ImageUtils.isImageBasedRecognition(winrec)){
        	        	testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
        	        	return;
        	        }                	
                    snapshotScreen();
                    getSaveTextFromGUI();       	
                }
                
			}catch(SAFSException x){
				Log.info("TIDC:exception \""+ x.getMessage() +"\".");
	  			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	  		    log.logMessage(testRecordData.getFac(),
	  		                   " SAFSException: " + x.toString(),
	  		                   FAILED_MESSAGE);
		    }
		}
		

		/**
		 * Retrieve specific Image= path from the recognition modifiers.
		 * Issues Action Failure and FAILED status if path is not in winrec.
		 * @param imagex Should be MOD_IMAGE, MOD_IMAGEW, or MOD_IMAGEH
		 * @param modifiers the array of recognition modifiers
		 * @param winrec the recognition string originating the modifiers
		 * @return String or null if not found.
		 */
		protected String getRecImagePath(String imagex, String[] modifiers, String winrec){
			return getRecImagePath(imagex, modifiers, winrec, true);
		}

		/**
		 * Retrieve specific Image= path from the recognition modifiers.
		 * Issues Action Failure and FAILED status if path is not in winrec.
		 * @param imagex Should be MOD_IMAGE, MOD_IMAGEW, or MOD_IMAGEH
		 * @param modifiers the array of recognition modifiers
		 * @param winrec the recognition string originating the modifiers
		 * @param logit -- send false if you don't want logging and status to occur
		 * Ex: when seeking ImageW and ImageH info which is optional
		 * @return
		 */
		protected String getRecImagePath(String imagex, String[] modifiers, String winrec, boolean logit){
            String imagepath = ImageUtils.extractImagePath(imagex, modifiers);
            if(imagepath==null){
            	if(logit){
	            	String sub = imagex + ImageUtils.MOD_EQ;
	            	this.issueActionFailure(FAILStrings.convert(FAILStrings.SUBSTRING_NOT_FOUND_2, 
	            			"Substring '"+ sub +"' not found in '"+ winrec +"'",
	            			sub, winrec));
            	}
            	return null;
            }
            return imagepath;
		}
		
		/**
		 * T  WindowName  CompName  LocateScreenImage  VarName
		 * 
		 * image(s) are in App Map under windowName:compName
		 * 
		 * param1 varname
		 * 
		 * variables set:
		 *   varname=x y w h  (space delimited)
		 *   varname.x=x
		 *   varname.y=y
		 *   varname.w=w
		 *   varname.h=h
		 */
		protected void locateScreenImage(){
	        testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
	        
	        if ( params.size( ) < 1 ) {
	            this.issueParameterCountFailure("VarName");
	            return;
	        } 
            String varname = ( String ) iterator.next( );
            try{ if(varname.indexOf("^")==0) varname = varname.substring(1); }
            catch(Exception x){ varname = null; }
            if((varname==null)||(varname.length()==0)){
            	this.issueParameterValueFailure("VarName="+varname);
            	return;
            }
            Rectangle winloc = null;
            String who = windowName+":"+compName;
            try{
            	winloc = ImageUtils.findComponentRectangle(testRecordData, secsWaitForWindow);
            }catch(SAFSException x){
            	//bad_app_map_item  :Item '%1%' was not found in App Map '%2%'
            	this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.BAD_APP_MAP_ITEM, 
            			"Item '"+who+"' was not found in App Map '"+mapname+"'", 
            			who, mapname));
            	return;
	        }catch(AWTException iox){
	        	this.issueActionFailure(FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
            			"Support for 'AWT Robot' not found.", "AWT Robot"));
            	return;
	        }catch(java.io.IOException iox){
	        	who +=" "+ iox.getMessage();
        		this.issueActionFailure(FAILStrings.convert(FAILStrings.FILE_ERROR, 
        			"Error opening or reading or writing file '"+ who +"'", 
        			who));
	            testRecordData.setStatusCode( StatusCodes.INVALID_FILE_IO );
    	        return;
            }
            if (winloc==null){
            	String snap = saveTestRecordScreenToTestDirectory(testRecordData);
            	this.issueErrorPerformingActionOnX(who, 
            			FAILStrings.convert(FAILStrings.NOT_FOUND_ON_SCREEN, 
            					who +" was not found on screen", who+":"+snap));
            	return; 
            }

            if(setRectVars(winloc, varname)){	        	
	        	//varAssigned2:Value '%1%' was assigned to variable '%2%'.
	        	String vals = winloc.x +" "+winloc.y+" "+winloc.width+" "+ winloc.height;
	        	String vars = varname+".x, "+ varname+".y, "+ varname+".w, "+ varname+".h";
	        	this.issuePassedSuccess(GENStrings.convert(GENStrings.VARASSIGNED2, 
	        			"Value '"+ vals +"' was assigned to variable '"+ vars +"'", 
	        			vals, vars));
	        	return;
        	}else{
        		//could_not_set_vars :Could not set one or more variable values.
            	this.issueActionFailure(FAILStrings.text(FAILStrings.COULD_NOT_SET_VARS, 
            			"Could not set one or more variable values.") +" "+ varname.toUpperCase());
            	return;
        	}
		}

	    /**
		 * Capture a screen GUI image to a file.  Currently, the available
	     * formats for the output file are BMP and JPG.  Optionally, the user may utilize the SubArea
	     * parameter to only capture a portion of the Component.
	     *
	     * This routine utilizes Java Advanced Imaging (JAI) to output the screen image to the file.
	     * JAI must be installed at compile time and runtime.
	     **/
	    protected Rectangle getComponentRectangle (){
	      Rectangle compRect = null;;
          String who = windowName+":"+compName;
          try{
          	compRect = ImageUtils.findComponentRectangle(testRecordData,secsWaitForWindow);
          }catch(SAFSException x){
          	//bad_app_map_item  :Item '%1%' was not found in App Map '%2%'
          	this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.BAD_APP_MAP_ITEM, 
          			"Item '"+who+"' was not found in App Map '"+mapname+"'", 
          			who, mapname));
          	return null;
	        }catch(AWTException iox){
	        	this.issueActionFailure(FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
          			"Support for 'AWT Robot' not found.", "AWT Robot"));
          	return null;
	        }catch(java.io.IOException iox){
	        	who +=" "+ iox.getMessage();
        		this.issueActionFailure(FAILStrings.convert(FAILStrings.FILE_ERROR, 
      			"Error opening or reading or writing file '"+ who +"'", 
      			who));
  	            testRecordData.setStatusCode( StatusCodes.INVALID_FILE_IO );
  	        return null;
          }
	      // was not found
          if (compRect==null){
        	  String snap = saveTestRecordScreenToTestDirectory(testRecordData);
           	this.issueErrorPerformingActionOnX(who, 
          		FAILStrings.convert(FAILStrings.NOT_FOUND_ON_SCREEN, 
          					who +" was not found on screen", who+":"+snap));
           	return null; 
          }
          return compRect;
	  }
		
		/**
		 * T  WindowName  CompName  GUIDoesExist
		 * T  WindowName  CompName  GUIDoesNotExist
		 * 
		 * image(s) are in App Map under windowName:compName
		 * 
		 */
		protected void guiDoesExist(){
	        testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
	        boolean isGuiDoesExist = action.equalsIgnoreCase(COMMAND_GUIDOESEXIST);
            Rectangle winloc = null;
            String who = windowName+":"+compName;
            try{
            	winloc = ImageUtils.findComponentRectangle(testRecordData, 0);
            }catch(SAFSException x){
            	//bad_app_map_item  :Item '%1%' was not found in App Map '%2%'
            	this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.BAD_APP_MAP_ITEM, 
            			"Item '"+who+"' was not found in App Map '"+mapname+"'", 
            			who, mapname));
            	return;
	        }catch(AWTException iox){
	        	this.issueActionFailure(FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
            			"Support for 'AWT Robot' not found.", "AWT Robot"));
            	return;
	        }catch(java.io.IOException iox){
	        	who +=" "+ iox.getMessage();
        		this.issueActionFailure(FAILStrings.convert(FAILStrings.FILE_ERROR, 
        			"Error opening or reading or writing file '"+ who +"'", 
        			who));
	            testRecordData.setStatusCode( StatusCodes.INVALID_FILE_IO );
    	        return;
            }
	        // was not found	        
            if (winloc==null){
            	if(isGuiDoesExist){ // is supposed to exist
            		String snap = saveTestRecordScreenToTestDirectory(testRecordData);
	            	this.issueErrorPerformingActionOnX(who, 
	            			GENStrings.convert(GENStrings.NOT_EXIST, 
	            					who +" does not exist.", who+":"+snap));
            	}else{ // is not supposed to exist
            	    testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
            		this.componentSuccessMessage(GENStrings.convert(GENStrings.NOT_EXIST, 
        					who +" does not exist.", who));
            	}
            // was found
            }else{
            	if(isGuiDoesExist){ // is supposed to exist
            	    testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
            		this.componentSuccessMessage(GENStrings.convert(GENStrings.EXISTS, 
        					who +" exists.", who));
            	}else{ // is not supposed to exist
            		String snap = saveTestRecordScreenToTestDirectory(testRecordData);
	            	this.issueErrorPerformingActionOnX(who, 
	            			GENStrings.convert(GENStrings.EXISTS, 
	            					who +" exists.", who+":"+snap));
            	}
            }
		}

		/**
		 * T  WindowName  CompName  ClickScreenImage
		 * 
		 * image(s) are in App Map under windowName:compName
		 * 
		 */
		protected void clickScreenImage(String winrec){
	        testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );	        
            Rectangle winloc = null;
            String who = windowName+"."+compName;
            String mapitem = null; //raw inputrecord appmapsubkey item
            String coords = null;  //raw coords from app map OR literal text
            java.awt.Point point = null; //final calculated click point
            String strcount = null;
            int clickcount = 3; //default for multiclick command
            boolean isMultiClick = action.equalsIgnoreCase(TIDComponent.COMMAND_MULTICLICK)||action.equalsIgnoreCase(TIDComponent.COMMAND_MULTICLICKSCREENIMAGE);

            // requires AppMapSubKey or literal text
            if (params.size() > 0){
            	try{ 
            		mapitem = (String)iterator.next();
            		if((mapitem==null)||(mapitem.length()==0)){
                		Log.debug("TIDComponent "+ action +" will not use invalid AppMapSubKey param: "+ mapitem);
            			mapitem = null;
            		}
            	}
            	catch(Exception x){
            		Log.debug("TIDComponent "+ action +" will not use invalid AppMapSubKey param from input record. "+ x.getClass().getSimpleName());
            	}
            }else{
        		Log.debug("TIDComponent "+ action +" optional AppMapSubKey param was not found.");
            }
            //MULTICLICK PARAM ClickCount
        	if(isMultiClick){
        		if(params.size() > 1){
                	try{ 
                		strcount = (String)iterator.next();
                		if((strcount==null)||(strcount.length()==0)){
                    		Log.debug("TIDComponent "+ action +" will not use invalid 'ClickCount' param: "+ strcount);
                			strcount = null;
                		}else{
                			clickcount = (int) Math.floor(Float.parseFloat(strcount));
                			if(clickcount < 1) {
                        		Log.debug("TIDComponent "+ action +" invalid 'ClickCount' param: "+ strcount+". Using 1, instead.");
                        		clickcount = 1;
                			}
                		}
                	}
                	catch(Exception x){
                		Log.debug("TIDComponent "+ action +" will not use invalid 'ClickCount' param: "+ strcount +" = "+ x.getClass().getSimpleName() +". Using default...");
                	}
        		}else{
            		Log.debug("TIDComponent "+ action +" optional 'ClickCount' param was not found.  Using default...");
        		}
        	}
            //seem to have a param for AppMapSubKey
            if(mapitem != null){
            	coords = getAppMapItem(null, compName, mapitem);	            
	            if((coords==null)||(coords.length()==0)){
	            	Log.info("TIDComponent "+ action +" AppMapSubKey not in AppMap. Trying as literal text: "+ mapitem);
	            	coords = mapitem;
	            }
	            // coords now contains something with length > 0
	            //convert any semis to commas
	            point = convertCoords(coords);
	            if(point==null){
            		Log.debug("TIDComponent "+ action +" will not use invalid AppMapSubKey param. Could not convert: "+ coords);
	            }else{
	                Log.info("TIDComponent "+ action +" "+ who +" using relative coordinates "+ point);
	            }
            }
            try{
            	winloc = ImageUtils.findComponentRectangle(testRecordData, secsWaitForWindow);
            }catch(SAFSException x){
            	//bad_app_map_item  :Item '%1%' was not found in App Map '%2%'
            	this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.BAD_APP_MAP_ITEM, 
            			"Item '"+who+"' was not found in App Map '"+mapname+"'", 
            			who, mapname));
            	return;
	        }catch(AWTException iox){
	        	this.issueActionFailure(FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
            			"Support for 'AWT Robot' not found.", "AWT Robot"));
            	return;
	        }catch(java.io.IOException iox){
	        	who +=" "+ iox.getMessage();
        		this.issueActionFailure(FAILStrings.convert(FAILStrings.FILE_ERROR, 
        			"Error opening or reading or writing file '"+ who +"'", 
        			who));
	            testRecordData.setStatusCode( StatusCodes.INVALID_FILE_IO );
    	        return;
            }
            if (winloc==null){
            	String snap = saveTestRecordScreenToTestDirectory(testRecordData);
            	this.issueErrorPerformingActionOnX(who, 
            			FAILStrings.convert(FAILStrings.NOT_FOUND_ON_SCREEN, 
            					who +" was not found on screen", who+":"+snap));
            	return; 
            }
            
            //calculate hotspot
            String[] modifiers = winrec.split(ImageUtils.MOD_SEP);
            java.awt.Point hs = ImageUtils.extractHotspot(modifiers);
            int pr = ImageUtils.extractPointRelative(modifiers);
            hs = ((pr == -1)&&(point != null)&&(hs==null)) ? 
               	 ImageUtils.calcHotspotPoint(hs, winloc, ImageUtils.INT_TOPLEFT) :
               	 ImageUtils.calcHotspotPoint(hs, winloc, pr);

            if(point != null){
            	hs.x += point.x;
            	hs.y += point.y;
            	if (hs.x < 0) hs.x = 0;
            	if (hs.x > ImageUtils.getScreenWidth()-1) hs.x = ImageUtils.getScreenWidth()-1;
            	if (hs.y < 0) hs.y = 0;
            	if (hs.y > ImageUtils.getScreenHeight()-1) hs.y = ImageUtils.getScreenHeight()-1;
            }
            if(action.equalsIgnoreCase(COMMAND_MULTICLICK)){
            	Log.info("TIDComponent "+ action +" "+ String.valueOf(clickcount) +" on "+ who +" calculated screen coordinates "+ hs);
            }else{
            	Log.info("TIDComponent "+ action +" "+ who +" calculated screen coordinates "+ hs);
            }
            
            try{
        		if((action.equalsIgnoreCase(TIDComponent.COMMAND_CLICK))||
        		   (action.equalsIgnoreCase(TIDComponent.COMMAND_CLICKSCREENIMAGE)))
        			org.safs.robot.Robot.click(hs);
        		else if((action.equalsIgnoreCase(TIDComponent.COMMAND_RIGHTCLICK))||
        				(action.equalsIgnoreCase(TIDComponent.COMMAND_RIGHTCLICKSCREENIMAGE)))
        			org.safs.robot.Robot.rightClick(hs);
        		else if((action.equalsIgnoreCase(TIDComponent.COMMAND_DOUBLECLICK))||
        				(action.equalsIgnoreCase(TIDComponent.COMMAND_DOUBLECLICKSCREENIMAGE)))
        			org.safs.robot.Robot.doubleClick(hs);
        		else if(action.equalsIgnoreCase(TIDComponent.COMMAND_CTRLCLICK)||
        				action.equalsIgnoreCase(TIDComponent.COMMAND_CTRLCLICKSCREENIMAGE))
        			org.safs.robot.Robot.clickWithKeyPress(hs.x, hs.y, InputEvent.BUTTON1_MASK, KeyEvent.VK_CONTROL, 1);
        		
        		else if(action.equalsIgnoreCase(TIDComponent.COMMAND_SHIFTCLICK)||
        				action.equalsIgnoreCase(TIDComponent.COMMAND_SHIFTCLICKSCREENIMAGE))
        			org.safs.robot.Robot.clickWithKeyPress(hs.x, hs.y, InputEvent.BUTTON1_MASK, KeyEvent.VK_SHIFT, 1);
        		
        		else if(action.equalsIgnoreCase(TIDComponent.COMMAND_CTRLRIGHTCLICK)||
        				action.equalsIgnoreCase(TIDComponent.COMMAND_CTRLRIGHTCLICKSCREENIMAGE))
        			org.safs.robot.Robot.clickWithKeyPress(hs.x, hs.y, InputEvent.BUTTON3_MASK, KeyEvent.VK_CONTROL, 1);
        		
        		else if(isMultiClick)
        			org.safs.robot.Robot.click(hs.x, hs.y, InputEvent.BUTTON1_MASK, clickcount);
        			
        		if (point == null) 
        			this.issuePassedSuccess("");
        		else
        			this.issuePassedSuccessUsing(mapitem);
        			
        	}catch(AWTException awtx){
            	this.issueActionFailure(FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
            			"Support for 'AWT Robot' not found.", "AWT Robot"));
        	}
		}

		/**
		 * T  WindowName  CompName  ClickScreenLocation
		 * T  WindowName  CompName  HoverScreenLocation
		 * 
		 * Coords CAN be in the App Map under windowName:compName and\or AppMapSubkey
		 * 
		 */
		protected void clickHoverScreenLocation(){
	        testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );	        
            String who = windowName+"."+compName;
            String mapitem = null; //raw inputrecord appmapsubkey item
            String coords = null;  //raw coords from app map OR literal text
            java.awt.Point root = null; //any root coords for windowName:compName
            java.awt.Point point = null; //final calculated click point
            String delayitem = null;
            long delaymillis = 0;
            
            //see if AppMap has predefined coords
            mapitem = getAppMapItem(null, windowName, compName);
            if(mapitem!=null){
        		Log.info("TIDComponent "+ action +" checking "+who+" for coordinate info in:"+ mapitem);
	            try{
	            	root = convertCoords(mapitem);
	            }catch(Exception x){
            		Log.debug("TIDComponent "+ action +" will not use invalid "+ who +" coordinate info...");
	            	root = null;
	            }
            }
            mapitem = null;
            
            // possibly optional AppMapSubKey or literal text
            if (params.size() > 0){
            	try{ 
            		mapitem = (String)iterator.next();
            		if((mapitem==null)||(mapitem.length()==0)){
                		Log.debug("TIDComponent "+ action +" will not use invalid AppMapSubKey param: "+ mapitem);
            			mapitem = null;
            		}
            	}
            	catch(Exception x){
            		Log.debug("TIDComponent "+ action +" will not use invalid AppMapSubKey param from input record. "+ x.getClass().getSimpleName());
            	}
            }else{
        		Log.debug("TIDComponent "+ action +" optional AppMapSubKey param was not found.");
            }
            //seem to have a param for AppMapSubKey
            if(mapitem != null){
            	coords = getAppMapItem(null, compName, mapitem);	            
	            if((coords==null)||(coords.length()==0)){
	            	Log.info("TIDComponent "+ action +" AppMapSubKey not in AppMap. Trying as literal text: "+ mapitem);
	            	coords = mapitem;
	            }
	            // coords now contains something with length > 0
	            //convert any semis to commas
	            point = convertCoords(coords);
	            if(point==null){
            		Log.debug("TIDComponent "+ action +" will not use invalid AppMapSubKey param. Could not convert: "+ coords);
	            }else{
	                Log.info("TIDComponent "+ action +" "+ who +" using coordinates "+ point);
	            }
            }
            // optional delay in milliseconds for hover
            if (action.equalsIgnoreCase(TIDComponent.COMMAND_HOVERSCREENLOCATION) && params.size() > 1){
            	try{ 
            		delayitem = (String)iterator.next();
            		if((delayitem==null)||(delayitem.length()==0)){
                		Log.debug("TIDComponent "+ action +" will not use invalid HoverTime param: "+ delayitem);
            			delayitem = null;
            		}
            	}
            	catch(Exception x){
            		Log.debug("TIDComponent "+ action +" will not use invalid HoverTime param from input record. "+ x.getClass().getSimpleName());
            	}
                if(delayitem != null){
    	            try{
    	            	delaymillis = Long.parseLong(delayitem);
    	            }catch(NumberFormatException nfe){
                		Log.debug("TIDComponent "+ action +" will not use invalid HoverTime param. Could not convert: "+ delayitem);
                		delayitem = null;
    	            }
                }
            }
            
            //calculate hotspot
            java.awt.Point hs = (root==null)? new java.awt.Point(0,0): root;

            if(point != null){
            	hs.x += point.x;
            	hs.y += point.y;
            	if (hs.x < 0) hs.x = 0;
            	if (hs.x > ImageUtils.getScreenWidth()-1) hs.x = ImageUtils.getScreenWidth()-1;
            	if (hs.y < 0) hs.y = 0;
            	if (hs.y > ImageUtils.getScreenHeight()-1) hs.y = ImageUtils.getScreenHeight()-1;
            }
            Log.info("TIDComponent "+ action +" "+ who +" calculated screen coordinates "+ hs);
            
            try{
        		if(action.equalsIgnoreCase(TIDComponent.COMMAND_HOVERSCREENLOCATION)){
        			org.safs.robot.Robot.getRobot().mouseMove(hs.x, hs.y);
           			if(delaymillis > 0){
           				try{Thread.sleep(delaymillis);}catch(Exception x){}
           			}
        		}else if(action.equalsIgnoreCase(TIDComponent.COMMAND_CLICKSCREENLOCATION))
        			org.safs.robot.Robot.click(hs);
        		else if(action.equalsIgnoreCase(TIDComponent.COMMAND_RIGHTCLICKSCREENLOCATION))
        			org.safs.robot.Robot.rightClick(hs);
        		else if(action.equalsIgnoreCase(TIDComponent.COMMAND_DOUBLECLICKSCREENLOCATION))
        			org.safs.robot.Robot.doubleClick(hs);
        			
        		if (point == null) 
        			this.issuePassedSuccess("");
        		else
        			this.issuePassedSuccessUsing(mapitem);
        			
        	}catch(AWTException awtx){
            	this.issueActionFailure(FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
            			"Support for 'AWT Robot' not found.", "AWT Robot"));
        	}
		}

		/**
		 * T  WindowName  CompName  HoverMouse
		 * 
		 * image(s) are in App Map under windowName:compName
		 * 
		 */
		protected void hoverScreenImage(String winrec){
	        testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );	        
            Rectangle winloc = null;
            String who = windowName+"."+compName;
            String mapitem = null; //raw inputrecord appmapsubkey item
            String delayitem = null; //raw inputrecord delay millis
            long delaymillis = 2000; //default delay
            String coords = null;  //raw coords from app map OR literal text
            java.awt.Point point = null; //final calculated click point

            // optional AppMapSubKey or literal text
            if (params.size() > 0){
            	try{ 
            		mapitem = (String)iterator.next();
            		if((mapitem==null)||(mapitem.length()==0)){
                		Log.debug("TIDComponent.hoverScreenImage will not use invalid AppMapSubKey param: "+ mapitem);
            			mapitem = null;
            		}
            	}
            	catch(Exception x){
            		Log.debug("TIDComponent.hoverScreenImage will not use invalid AppMapSubKey param from input record. "+ x.getClass().getSimpleName());
            	}
            }else{
        		Log.debug("TIDComponent.hoverScreenImage optional AppMapSubKey param was not found.");
            }
            //seem to have a param for AppMapSubKey
            if(mapitem != null){
            	coords = getAppMapItem(null, compName, mapitem);	            
	            if((coords==null)||(coords.length()==0)){
	            	Log.info("TIDComponent.hoverScreenImage AppMapSubKey not in AppMap. Trying as literal text: "+ mapitem);
	            	coords = mapitem;
	            }
	            // coords now contains something with length > 0
	            //convert any semis to commas
	            point = convertCoords(coords);
	            if(point==null){
            		Log.debug("TIDComponent.hoverScreenImage will not use invalid AppMapSubKey param. Could not convert: "+ coords);
	            }else{
	                Log.info("TIDComponent hoverScreenImage "+ who +" using relative coordinates "+ point);
	            }
            }
            // optional delay in milliseconds
            if (params.size() > 1){
            	try{ 
            		delayitem = (String)iterator.next();
            		if((delayitem==null)||(delayitem.length()==0)){
                		Log.debug("TIDComponent.hoverScreenImage will not use invalid HoverTime param: "+ delayitem);
            			delayitem = null;
            		}
            	}
            	catch(Exception x){
            		Log.debug("TIDComponent.hoverScreenImage will not use invalid HoverTime param from input record. "+ x.getClass().getSimpleName());
            	}
            }else{
        		Log.debug("TIDComponent.hoverScreenImage optional HoverTime param was not found.");
            }
            if(delayitem != null){
	            try{
	            	delaymillis = Long.parseLong(delayitem);
	            }catch(NumberFormatException nfe){
            		Log.debug("TIDComponent.hoverScreenImage will not use invalid HoverTime param. Could not convert: "+ delayitem);
            		delayitem = null;
	            }
            }
            try{
            	winloc = ImageUtils.findComponentRectangle(testRecordData, secsWaitForWindow);
            }catch(SAFSException x){
            	//bad_app_map_item  :Item '%1%' was not found in App Map '%2%'
            	this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.BAD_APP_MAP_ITEM, 
            			"Item '"+who+"' was not found in App Map '"+mapname+"'", 
            			who, mapname));
            	return;
	        }catch(AWTException iox){
	        	this.issueActionFailure(FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
            			"Support for 'AWT Robot' not found.", "AWT Robot"));
            	return;
	        }catch(java.io.IOException iox){
	        	who +=" "+ iox.getMessage();
        		this.issueActionFailure(FAILStrings.convert(FAILStrings.FILE_ERROR, 
        			"Error opening or reading or writing file '"+ who +"'", 
        			who));
	            testRecordData.setStatusCode( StatusCodes.INVALID_FILE_IO );
    	        return;
            }
            if (winloc==null){
            	String snap = saveTestRecordScreenToTestDirectory(testRecordData);
            	this.issueErrorPerformingActionOnX(who, 
            			FAILStrings.convert(FAILStrings.NOT_FOUND_ON_SCREEN, 
            					who +" was not found on screen", who+":"+snap));
            	return; 
            }
            
            //calculate hotspot
            String[] modifiers = winrec.split(ImageUtils.MOD_SEP);
            java.awt.Point hs = ImageUtils.extractHotspot(modifiers);
            int pr = ImageUtils.extractPointRelative(modifiers);
            hs = ((pr == -1)&&(point != null)&&(hs==null)) ? 
               	 ImageUtils.calcHotspotPoint(hs, winloc, ImageUtils.INT_TOPLEFT) :
               	 ImageUtils.calcHotspotPoint(hs, winloc, pr);

            if(point != null){
            	hs.x += point.x;
            	hs.y += point.y;
            	if (hs.x < 0) hs.x = 0;
            	if (hs.x > ImageUtils.getScreenWidth()-1) hs.x = ImageUtils.getScreenWidth()-1;
            	if (hs.y < 0) hs.y = 0;
            	if (hs.y > ImageUtils.getScreenHeight()-1) hs.y = ImageUtils.getScreenHeight()-1;
            }
            Log.info("TIDComponent hoverScreenImage "+ who +" calculated screen coordinates "+ hs);
            
            try{
       			org.safs.robot.Robot.getRobot().mouseMove(hs.x, hs.y);
       			if(delaymillis > 0){
       				try{Thread.sleep(delaymillis);}catch(Exception x){}
       			}
       			//org.safs.robot.Robot.getRobot().mouseMove(hs.x, hs.y);
        		if (point == null) 
        			this.issuePassedSuccess("");
        		else
        			this.issuePassedSuccessUsing(mapitem);
        			
        	}catch(AWTException awtx){
            	this.issueActionFailure(FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
            			"Support for 'AWT Robot' not found.", "AWT Robot"));
        	}
		}

		/**
		 * T  WindowName  CompName  LeftDrag   AppMapSubKey
		 * T  WindowName  CompName  RightDrag  AppMapSubKey
		 * T  WindowName  CompName  LeftDrag   "x1,y1,x2,y2"
		 * T  WindowName  CompName  RightDrag   "Coords=x1;y1;x2;y2"
		 * 
		 * image(s) are in App Map under windowName:compName
		 * 
		 *  AppMapSubKey would be in App Map under CompName section:
		 *  [CompName]
		 *  AppMapSubKey="Coords=x1,y1,x2,y2"   or
		 *  AppMapSubKey="x1;y1;x2;y2"
		 *  
		 *  Supports both comma (,) and semi-colon (;) coordinate delimiters.
		 *  The default Hotspot for Right/LeftDrag is TopLeft, not Center.
		 */
		protected void mouseDrag(String winrec){
	        testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );	        
            Rectangle winloc = null;
            String who = windowName+"."+compName;
            String mapitem = null; //raw inputrecord appmapsubkey item
            String coords = null;  //raw coords from app map OR literal text
            java.awt.Point startDrag = null; //final calculated drag start
            java.awt.Point endDrag = null;   //final calculated drag end
            
            // requires AppMapSubKey or literal text
            if (params.size()< 1){
            	this.issueParameterCountFailure("AppMapSubKey");
            	return;
            }else{
            	try{ 
            		mapitem = (String)iterator.next();
            		if((mapitem==null)||(mapitem.length()==0)){
                    	this.issueParameterCountFailure("AppMapSubKey");
                    	return;
            		}
            	}
            	catch(Exception x){
            		Log.debug("TIDComponent.mouseDrag could not get AppMapSubKey param from input record. "+ x.getClass().getSimpleName());
            		this.issueParameterValueFailure("AppMapSubKey");
            		return;
            	}
            }
            coords = getAppMapItem(null, compName, mapitem);
            if((coords==null)||(coords.length()==0)){
            	Log.info("TIDComponent.mouseDrag AppMapSubKey not in AppMap. Trying as literal text: "+ mapitem);
            	coords = mapitem;
            }
            // coords now contains something with length > 0
            //convert any semis to commas
            coords = StringUtilities.findAndReplace(coords, ";", ",");
            //strip off Coords= if present
            if (coords.toLowerCase().startsWith("coords=")){
            	try{ coords = coords.substring(7);}
            	catch(Exception x){ //index out of bounds possible
            		Log.debug("TIDComponent.mouseDrag failed--could not strip coords: "+ x.getClass().getSimpleName());
            		this.issueParameterValueFailure("Coords");
            		return;
            	}
            }
            // now should have "x1,y1,x2,y2"
            String[] values = coords.split(",");
            if((values==null)||(values.length != 4)){
        		Log.debug("TIDComponent.mouseDrag failed--coords appear invalid: "+ coords);
        		this.issueParameterValueFailure("Coords");
        		return;
            }
            try{
            	startDrag = new java.awt.Point(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
            	endDrag = new java.awt.Point(Integer.parseInt(values[2]), Integer.parseInt(values[3]));
            }catch(Exception x){
        		Log.debug("TIDComponent.mouseDrag failed--coords appear invalid: "+ coords +" : "+ x.getClass().getSimpleName());
        		this.issueParameterValueFailure("Coords");
        		return;
            }
            Log.info("TIDComponent mouseDrag "+ who +" using relative coordinates "+ startDrag +" to "+ endDrag);
            try{
            	winloc = ImageUtils.findComponentRectangle(testRecordData, secsWaitForWindow);
            }catch(SAFSException x){
            	//bad_app_map_item  :Item '%1%' was not found in App Map '%2%'
            	this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.BAD_APP_MAP_ITEM, 
            			"Item '"+who+"' was not found in App Map '"+mapname+"'", 
            			who, mapname));
            	return;
	        }catch(AWTException iox){
	        	this.issueActionFailure(FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
            			"Support for 'AWT Robot' not found.", "AWT Robot"));
            	return;
	        }catch(java.io.IOException iox){
	        	who +=" "+ iox.getMessage();
        		this.issueActionFailure(FAILStrings.convert(FAILStrings.FILE_ERROR, 
        			"Error opening or reading or writing file '"+ who +"'", 
        			who));
	            testRecordData.setStatusCode( StatusCodes.INVALID_FILE_IO );
    	        return;
            }
            if (winloc==null){
            	String snap = saveTestRecordScreenToTestDirectory(testRecordData);
            	this.issueErrorPerformingActionOnX(who, 
            			FAILStrings.convert(FAILStrings.NOT_FOUND_ON_SCREEN, 
            					who +" was not found on screen", who+":"+snap));
            	return; 
            }
            
            //calculate hotspot for start of Drag, defaults to 0,0 of component
            String[] modifiers = winrec.split(ImageUtils.MOD_SEP);
            java.awt.Point hs = ImageUtils.extractHotspot(modifiers);
            if (hs==null) hs = new java.awt.Point(0,0);
            int pr = ImageUtils.extractPointRelative(modifiers);
            hs = (pr == -1) ? 
            	 ImageUtils.calcHotspotPoint(hs, winloc, ImageUtils.INT_TOPLEFT) :
               	 ImageUtils.calcHotspotPoint(hs, winloc, pr);
            
            //convert component relative to absolute screen coordinates
            startDrag.x += hs.x;
            startDrag.y += hs.y;
            endDrag.x += hs.x;
            endDrag.y += hs.y;
            
            //check and keep in screen boundaries
            if(startDrag.x < 0) startDrag.x = 0;
            if(startDrag.y < 0) startDrag.y = 0;
            if(startDrag.x > ImageUtils.getScreenWidth()-1) startDrag.x = ImageUtils.getScreenWidth()-1;
            if(startDrag.y > ImageUtils.getScreenHeight()-1) startDrag.y = ImageUtils.getScreenHeight()-1;

            if(endDrag.x < 0) endDrag.x = 0;
            if(endDrag.y < 0) endDrag.y = 0;
            if(endDrag.x > ImageUtils.getScreenWidth()-1) endDrag.x = ImageUtils.getScreenWidth()-1;
            if(endDrag.y > ImageUtils.getScreenHeight()-1) endDrag.y = ImageUtils.getScreenHeight()-1;

            Log.info("TIDComponent mouseDrag "+ who +" calculated screen coordinates "+ startDrag +" to "+ endDrag);

            try{
        		if(action.equalsIgnoreCase(TIDComponent.COMMAND_LEFTDRAG))
        			org.safs.robot.Robot.leftDrag(startDrag, endDrag);
        		else //RightDrag
        			org.safs.robot.Robot.rightDrag(startDrag, endDrag);
        			
        		this.issuePassedSuccessUsing(mapitem);
        	}catch(AWTException awtx){
            	this.issueActionFailure(FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
            			"Support for 'AWT Robot' not found.", "AWT Robot"));
        	}
		}

		/**
		 * ClickScreenPoint  x,y
		 *
		 ********************************************************************/
		protected void clickScreenPoint(){
	        testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );	        
	        if ( params.size( ) < 1 ) {
	            this.issueParameterCountFailure("ScreenPoint");
	            return;
	        } 
            String text = ( String ) iterator.next( );
			if((text==null)||(text.length()==0)){
	            this.issueParameterValueFailure("ScreenPoint");
	            return;
			}
			//System.out.println((String)sHelper.getCompTestObject()+":searchthis:"+ keys);
			text=text.trim();
		   	Log.info("TIDC "+ action +" processing: "+ text);
		   	try{
				String sep = " ";
				boolean sep_space = true;
				if(text.indexOf(",")> -1) {
					sep = ",";
					sep_space= false;
				}else if(text.indexOf(";")> -1) {
					sep = ";";
					sep_space= false;
				} 
				int x = 0;
				int y = 0;
				
				if(sep_space){
					StringTokenizer toker = new StringTokenizer(text, " ");
					if(toker.countTokens()< 2){
			            this.issueParameterCountFailure("ScreenPoint");
			            return;
					}
			   		x = Integer.parseInt(toker.nextToken());
			   		y = Integer.parseInt(toker.nextToken());
				}else{
			   		String[] vals = text.split(sep);
			   		x = Integer.parseInt(vals[0]);
			   		y = Integer.parseInt(vals[1]);
				}
		   		if (x < 0 ) x = 0;
		   		if (x > ImageUtils.getScreenWidth() ) x = ImageUtils.getScreenWidth();
		   		if (y < 0 ) y = 0;
		   		if (y > ImageUtils.getScreenHeight() ) y = ImageUtils.getScreenHeight();
		   		java.awt.Point hs = new java.awt.Point(x,y);
		   		String hsstr = hs.x +","+ hs.y;
        		if((action.equalsIgnoreCase(TIDComponent.COMMAND_CLICKSCREENPOINT)))
             			org.safs.robot.Robot.click(hs);
             		else if((action.equalsIgnoreCase(TIDComponent.COMMAND_RIGHTCLICKSCREENPOINT)))
             			org.safs.robot.Robot.rightClick(hs);
             		else if((action.equalsIgnoreCase(TIDComponent.COMMAND_DOUBLECLICKSCREENPOINT)))
             			org.safs.robot.Robot.doubleClick(hs);		   		
			   	testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
				// log success message and status
				log.logMessage(testRecordData.getFac(), 
						genericText.convert("success3a", windowName +":"+ compName + " "+ action
								+" successful using "+ hsstr, windowName, compName, action, hsstr), 
						PASSED_MESSAGE);
		   	}catch(ArrayIndexOutOfBoundsException x){
		   		this.issueParameterValueFailure("ScreenPoint="+text);
		   	}catch(NumberFormatException x){
		   		this.issueParameterValueFailure("ScreenPoint="+text);
		   	}catch(AWTException x){
            	this.issueActionFailure(FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
            			"Support for 'AWT Robot' not found.", "AWT Robot"));
		   	}catch(Exception x){
		   		this.issueErrorPerformingActionOnX(text, x.getClass().getSimpleName()+": "+ x.getMessage());
		   	}
		}

		/** 
		 * Send keyboard input to the current input focus target.
		 * The command does not attempt to change keyboard focus from 
		 * where it already is.
		 **/
		protected void inputKeys() {
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
			//System.out.println((String)sHelper.getCompTestObject()+":searchthis:"+ keys);
		   	Log.info("TIDC InputKeys processing: "+ text);
		   	try{
		   		if((action.equalsIgnoreCase(TIDComponent.COMMAND_INPUTKEYS))||
		   		   (action.equalsIgnoreCase(TIDComponent.COMMAND_TYPEKEYS)))
		   			org.safs.robot.Robot.inputKeys(text);
		   		else if(action.equalsIgnoreCase(TIDComponent.COMMAND_TYPECHARS) ||
		   				action.equalsIgnoreCase(TIDComponent.COMMAND_INPUTCHARS)){
		   			// handle TypeChars and InputCharacters here
		   			org.safs.robot.Robot.inputChars(text);
		   		}
		   		
			   	testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
				// log success message and status
				log.logMessage(testRecordData.getFac(), 
						genericText.convert("success3a", windowName +":"+ compName + " "+ action
								+" successful using "+ text, windowName, compName, action, text), 
						PASSED_MESSAGE);
		   	}catch(AWTException x){
            	this.issueActionFailure(FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
            			"Support for 'AWT Robot' not found.", "AWT Robot"));
		   	}catch(Exception x){
		   		this.issueErrorPerformingActionOnX(text, x.getClass().getSimpleName()+": "+ x.getMessage());
		   	}
		}
		
		/**
		 * Decrypt the "encrypted text" and then send keyboard input to the current input focus target.
		 * The command does not attempt to change keyboard focus from where it already is.<br>
		 * <b>note:</b> Be careful, decrypted-text MUST NOT appear in any logs during implementation.
		 **/
		protected void inputEncryptions() {
			String debugmsg = StringUtils.debugmsg(this.getClass(), "inputEncryptions");
			boolean debuglogEnabled = false;
	        testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );	        
	        if ( params.size() < 2 ) {
	            this.issueParameterCountFailure("Insufficient parameters provided.");
	            return;
	        } 
            String dataPath = ( String ) iterator.next( );
			if((dataPath==null)||(dataPath.length()==0)){
	            this.issueParameterValueFailure("EncryptedDataPath");
	            return;
			}
			String privatekeyPath = ( String ) iterator.next( );
			if((privatekeyPath==null)||(privatekeyPath.length()==0)){
				this.issueParameterValueFailure("PrivateKeyPath");
				return;
			}
		   	Log.info("TIDC "+action+" processing ... ");
		   	//Turn off debug log before we decrypt the "encrypted text"
		   	debuglogEnabled = Log.ENABLED;
		   	if(debuglogEnabled){
		   		Log.info("Turning off debug temporarily ... ");
		   		Log.ENABLED = false;
		   	}
		   	
		   	try{
		   		if(action.equalsIgnoreCase(TIDComponent.COMMAND_TYPEENCRYPTION)){
		   			String encryptedData = FileUtilities.readStringFromUTF8File(dataPath);
		   			String privateKey = FileUtilities.readStringFromUTF8File(privatekeyPath);
		   			String decryptedData = RSA.decryptByPrivateKey(encryptedData, privateKey);
		   			org.safs.robot.Robot.inputChars(decryptedData);
		   		}
		   		
		   		if(debuglogEnabled){
		   			Log.info("Turning on debug ... ");
		   			Log.ENABLED = true;
		   		}
			   	testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);

				//log success message and status
				log.logMessage(testRecordData.getFac(), 
						genericText.convert(org.safs.text.GENStrings.SUCCESS_3, windowName +":"+ compName + " "+ action
								+" successful.", windowName, compName, action), 
						PASSED_MESSAGE);
		   	}catch(AWTException x){
		   		Log.ENABLED = debuglogEnabled;
            	this.issueActionFailure(FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
            			"Support for 'AWT Robot' not found.", "AWT Robot"));
		   	}catch(Exception x){
		   		Log.ENABLED = debuglogEnabled;
		   		Log.error(debugmsg+" met exception ", x);
		   		this.issueErrorPerformingAction(StringUtils.debugmsg(x));
		   	}
		   	
		}
		
		/**
		 * VerifyValues, VerifyValueEquals, VerifyValuesNotEqual, VerifyValuesIgnoreCase, 
		 * VerifyValueContains, VerifyValueContainsIgnoreCase, 
		 * VerifyValueDoesNotContain  
		 */
		protected void verifyValues() throws SAFSException {
			testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
			if ( params.size( ) < 2 ) {
				paramsFailedMsg( windowName, compName );
			} else {
				String altText = "";
				String value = ( String ) iterator.next( );
				String compare =  ( String ) iterator.next( );
				boolean ignorecase = (action.equalsIgnoreCase(COMMAND_VERIFYVALUESIGNORECASE) ||
						             (action.equalsIgnoreCase(COMMAND_VERIFYVALUECONTAINSIGNORECASE)));
				boolean suppress = iterator.hasNext() ? iterator.next().equalsIgnoreCase(PARAM_SUPPRESSVALUE) : false;
				Log.info( ".....CFComponent.process; ready to "+ action +" "+ value + " with: " + compare );
		
				String testval = ( ignorecase ) ? compare.toUpperCase( ) : compare;
				String testrval = ( ignorecase ) ? value.toUpperCase( ) : value;
				
				if(suppress) value = "SUPPRESSED_VALUE";
				
				// verifyvaluecontains, verifyvaluecontainsignorecase
				if (action.equalsIgnoreCase(COMMAND_VERIFYVALUECONTAINSIGNORECASE) || 
					action.equalsIgnoreCase(COMMAND_VERIFYVALUECONTAINS)){
					//benchmark only "contains" empty string if it too is empty string!
					if ( testval.length()==0){
						if(testrval.length()> 0){
							// failed
							testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
							altText = genericText.convert("not_equal", 
								  "\""+value +"\" does not equal \""+ compare +"\"",
								  value, compare);				
							altText = getStandardErrorMessage(action +":"+ altText);				
							logMessage(altText, testRecordData.getInputRecord(), FAILED_MESSAGE);
						} else {
							// set status to ok
							testRecordData.setStatusCode( StatusCodes.OK );
							altText = genericText.convert("equals", 
									  "\""+value +"\" equals \""+ compare +"\"",
									  value, compare);
							logMessage(action+":"+ altText, null, PASSED_MESSAGE);
						}
					}else if ( testrval.indexOf(testval) < 0 ) {
						// failed
						testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
						altText = genericText.convert("bench_not_contains", 
							  "\""+value +" did not contain expected substring \""+ compare +"\"",
							  value, compare);				
						altText = getStandardErrorMessage(action+":"+ altText);				
						logMessage(altText, testRecordData.getInputRecord(), FAILED_MESSAGE);
					} else {
						// set status to ok
						testRecordData.setStatusCode( StatusCodes.OK );
						altText = genericText.convert("bench_contains", 
								  "\""+value +"\" contains expected substring \""+ compare +"\"",
								  value, compare);
						logMessage(action+":"+ altText, null, PASSED_MESSAGE);
					}
				}
				// VerifyValues, VerifyValueEquals
				else if ((action.equalsIgnoreCase(COMMAND_VERIFYVALUES))||
					(action.equalsIgnoreCase(COMMAND_VERIFYVALUEEQUALS))||
					(action.equalsIgnoreCase(COMMAND_VERIFYVALUESIGNORECASE))){
					if ( ! testrval.equals(testval) ) {
						// failed
						testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
						altText = genericText.convert("not_equal", 
							  "\""+ value +"\" does not equal \""+ compare +"\"",
							  value, compare);
						altText = getStandardErrorMessage(action+": " + altText);				
						logMessage(altText, testRecordData.getInputRecord(), FAILED_MESSAGE);
					} else {
						// set status to ok
						testRecordData.setStatusCode( StatusCodes.OK );
						altText = genericText.convert("equals", 
								  "\""+value +"\" equals \""+ compare +"\"",
								  value, compare);
						logMessage(action+": "+ altText, null, PASSED_MESSAGE);
					}
				}
				// VerifyValuesNotEqual
				else if (action.equalsIgnoreCase(COMMAND_VERIFYVALUESNOTEQUAL)){
					if ( testrval.equals(testval) ) {
						// failed
						testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
						altText = genericText.convert("equals", 
								  "\""+value +"\" equals \""+ compare +"\"",
								  value, compare);
						altText = getStandardErrorMessage(action+":"+ altText);				
						logMessage(altText, testRecordData.getInputRecord(), FAILED_MESSAGE);
					} else {
						// set status to ok
						testRecordData.setStatusCode( StatusCodes.OK );
						altText = genericText.convert("not_equal", 
								  "\""+ value +"\" does not equal \""+ compare +"\"",
								  value, compare);
						logMessage(action +": "+ altText, null, PASSED_MESSAGE);
					}
				}
				// VerifyValueDoesNotContain
				else {
					//if benchmark is empty then only make sure we are NOT empty
					if ( testval.length()==0){
						if(testrval.length()> 0){
							// set status to ok
							testRecordData.setStatusCode( StatusCodes.OK );
							altText = genericText.convert("not_contain", 
									  "\""+value +"\" does not contain \"[EMPTY]\"",
									  value, "[EMPTY]");
							logMessage(action+":"+ altText, null, PASSED_MESSAGE);
						} else {
							// failed
							testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
							altText = genericText.convert("contains", 
								  "\""+value +"\" contains \""+ compare +"\"",
								  value, compare);				
							altText = getStandardErrorMessage(action +":"+ altText);				
							logMessage(altText, testRecordData.getInputRecord(), FAILED_MESSAGE);
						}
					// if we are not testing for empty
					}else if ( testrval.indexOf(testval) < 0 ) {
						// set status to ok
						testRecordData.setStatusCode( StatusCodes.OK );
						altText = genericText.convert("not_contain", 
								  "\""+value +"\" does not contain \""+ compare +"\"",
								  value, compare);
						logMessage(action+":"+ altText, null, PASSED_MESSAGE);
					} else {
						// failed
						testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
						altText = genericText.convert("contains", 
							  "\""+value +" contains \""+ compare +"\"",
							  value, compare);				
						altText = getStandardErrorMessage(action+":"+ altText);				
						logMessage(altText, testRecordData.getInputRecord(), FAILED_MESSAGE);
					}
				}
			}
		}
	
	    /** <br><em>Purpose:</em> verifyFileToFile
	     ** @param text, boolean, if true, then text files, else binary files
	     **/
		@SuppressWarnings("unchecked")
		protected void verifyFileToFile( boolean text ) throws SAFSException {
	        testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
	        String debugmsg = StringUtils.debugmsg(false);
	        
	        if ( params.size( ) < 2 ) {
	            paramsFailedMsg( windowName, compName );
	        } else {
	            String benchfilename = ( String ) iterator.next( );
	            benchfilename = deduceBenchFile(benchfilename).getAbsolutePath();

	            String testfilename = ( String ) iterator.next( );
	            testfilename = deduceTestFile(testfilename).getAbsolutePath();

	            String filterMode = iterator.hasNext()? (String) iterator.next(): null;
	            String filterOptions = iterator.hasNext()? (String) iterator.next(): null;
	            int bitTolerance = 100;
	            
	            boolean isImage = ImageUtils.isImageFormatSupported(benchfilename);
	            
	            if(!isImage){
	            	Log.info(".....TIDComponent.process; ready to do the VFTF for bench file : "
	            			+ benchfilename + ", test file: " + testfilename );
	            }else{
	            	if(FileUtilities.FilterMode.TOLERANCE.name.equalsIgnoreCase(filterMode)){
	            		try{
	            			bitTolerance = Integer.parseInt(filterOptions);
	            			if(bitTolerance<0) bitTolerance = 0;
	            			if(bitTolerance>100) bitTolerance = 100;
	            		}catch(NumberFormatException nfe){
	            			Log.warn(debugmsg+" ignore invalid filter options '"+filterOptions+"', it should be an integer.");
	            		}
	            	}else{
	            		Log.warn(debugmsg+" ignore invalid filter mode '"+filterMode+"'");
	            	}
		            Log.info(".....TIDComponent.process; VFTF performing IMAGE comparison for bench file : "
		                    + benchfilename + ", test file: " + testfilename );
	            }
	            
	            // now compare the two files
	            Collection<String> benchcontents = new ArrayList<String>( );
	            Collection<String> testcontents = new ArrayList<String>( );
	            BufferedImage bimage = null;
	            BufferedImage timage = null;
	            BufferedImage diffimage = null;
	            try {
	                boolean success = false;
	                if(isImage){
	                	try{
	                		bimage = ImageUtils.getStoredImage(benchfilename);
	                		timage = ImageUtils.getStoredImage(testfilename);
	                		success = ImageUtils.compareImage(bimage, timage, bitTolerance);
	                	}catch(Exception x){
	    		            Log.debug( ".....TIDComponent.process; VFTF IMAGE comparison failed due to "+
	                	     x.getClass().getName()+", "+x.getMessage());
	    		            if(bimage == null){
	    		            	issueParameterValueFailure("BenchmarkFile: "+ benchfilename);
	    		            	return;
	    		            }else if(timage == null){
	    		            	issueParameterValueFailure("ActualFile: "+ testfilename);
	    		            	return;
	    		            }
	                	}
	                }else if (text){
	                    benchcontents = StringUtils.readfile( benchfilename );
	                    testcontents = StringUtils.readfile( testfilename );
	                    success = benchcontents.equals( testcontents );
	                } else {
	                    String bench = ( StringUtils.readBinaryFile( benchfilename ) ).toString( );
	                    String test  = ( StringUtils.readBinaryFile( testfilename ) ).toString( );
	                    benchcontents.add( bench );
	                    testcontents.add( test );
	                    Log.info(debugmsg+ "benchcontents.length: " + bench.length( ) );
	                    Log.info(debugmsg+ "testcontents.length: " + test.length( ) );
	                    success = benchcontents.equals( testcontents );
	                }
	                if ( !success ) {	             
	                	if(isImage){	                		
	                		File diffout = null;
							try{ 
								diffimage = ImageUtils.createDiffImage(timage, bimage);
								diffout = deduceDiffFile(FileUtilities.deduceMatchingUUIDFilename(benchfilename));
								ImageUtils.saveImageToFile(diffimage, diffout);
							}catch(Exception x){
								Log.info(debugmsg+action +" failed to create Diff Image due to: "+x.getClass().getName()+", "+ x.getMessage());
							}
							StringBuffer message = new StringBuffer();
							message.append(GENStrings.convert(GENStrings.CONTENT_NOT_MATCHES_KEY,
									"the content of '"+ testfilename +"' does not match the content of "+benchfilename,
									testfilename,benchfilename));
							//see_difference_file		: Please see difference in file '%1%'.
							if(diffimage != null){
								message.append(" "+ GENStrings.convert(GENStrings.SEE_DIFFERENCE_FILE,
										"Please see difference in file '"+ diffout.getAbsolutePath() +"'.",
										diffout.getAbsolutePath()));
							}
							issueErrorPerformingActionOnX(testfilename, message.toString());
	                	}else{
				            String alttext = passedText.convert("contents_do_not_match",
		                        "contents of '"+ testfilename +"' did not match contents of '"+ benchfilename +"'",
		                        testfilename,benchfilename);
				            logMessage(action+" "+alttext, testRecordData.getInputRecord(), FAILED_MESSAGE);
	                	}
			        } else {
			            // set status to ok
			            testRecordData.setStatusCode(DriverConstant.STATUS_NO_SCRIPT_FAILURE);
			            String alttext = passedText.convert("content_matches",
	                        "contents of '"+ testfilename +"' matches contents of '"+ benchfilename +"'",
	                        testfilename,benchfilename);
			            logMessage(action+" "+alttext, null, PASSED_MESSAGE);
			        }
	                
	            } catch ( IOException ioe ) {
	                componentFailureMessage( testRecordData.getCommand( )
	                        + " failure, file can't be read: " + benchfilename + ", or: "
	                        + testfilename + ", msg: " + ioe.getMessage( ) );
	                testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
	            }
	        }
	    }
	    /**
	     * Run two keywords: GetTextFromGUI and SaveTextFromGUI
	     * @throws SAFSException, thrown when keywords fail to execute
	     */
	    protected void getSaveTextFromGUI() throws SAFSException {
	    	String debugmsg = getClass().getName()+".getSaveTextFromGUI(): ";
	    	Log.info(debugmsg + "in processing ...");		  	  
		  	String command = testRecordData.getCommand();
		    if (params.size() < 1) {
		  	    Log.debug("require at least one paramater, exit!");
		    	issueParameterCountFailure("output");
		        return;
		    }
		    
		    //get the params
		    //1st parameter is a variable name if keyword is GetTextFromImage, or a file name if keyword is SaveTextFromImage 
		    String outputVar  = (String) iterator.next();		    // 1st param, a variable name 
		    String subareaKey = "";                        	     	// 2nd optional param, setting default.A subkey in appmap  
		    String ocrId      = OCREngine.OCR_DEFAULT_ENGINE_KEY;	 // 3th optional param, setting default 
		    String langId     = OCREngine.getOCRLanguageCode(staf);  // 4th optional param, setting the language defined in STAF as default
		    float  scaleRatio  = -1; 							    // 5th optional param, setting default 
		   
		    if (iterator.hasNext()) 
		    	subareaKey = (String) iterator.next();  
		    if (iterator.hasNext()) {
		    	ocrId = (String) iterator.next();
		      	if(ocrId.equals("")) ocrId = OCREngine.OCR_DEFAULT_ENGINE_KEY;
		    }
		    if (iterator.hasNext()) {
		    	langId = (String) iterator.next();
		      	if(langId.equals("")) langId = OCREngine.getOCRLanguageCode(staf);
		    }
		    if (iterator.hasNext()) 
		    	try {
		    		scaleRatio = (float)Double.parseDouble((String) iterator.next());
		      	} catch(NumberFormatException nfe){}
  
		    // search Component GUI 
		    // get subarea from current Component	  
		    Rectangle imageRect;
		    Rectangle wholeRect = getComponentRectangle();
		    if (wholeRect == null) {
		    	Log.debug("...component unable to be found!");
		      	throw new SAFSException("TID: component unable to be found");
		    }
		        
		    String subarea;
		    if(subareaKey.equals("")) {
		    	Log.info ("using whole area as SubAreaKey not provided...");
		      	imageRect = wholeRect;
		    } else {
		    	subarea = lookupAppMapReference(subareaKey);
		        if (subarea == null) {
	       			//subareaKey not found in AppMap, take subareaKey as the value, which is in format ("x1,y1,x2,y2" or "x1%,y1%,x2%,y2%").
	      			Log.info(debugmsg + " subareaKey not in AppMap. Trying as literal text: "+ subareaKey);
		        	subarea = subareaKey;
		       	}    	
		        imageRect = ImageUtils.getSubAreaRectangle(wholeRect, subarea);
		      	if (imageRect == null) {
		      		throw new SAFSException("TID: SubArea not found with value: " + subarea);
		      	}	
		    }
		    
		    //capture GUI
		    BufferedImage buffimg;
		    try {
		    	Log.debug(" ...Capture Screen Area: "+ imageRect);
		       	buffimg = ImageUtils.captureScreenArea(imageRect);
		    } catch (java.awt.AWTException ae) {
		       	this.issueActionFailure(FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
		              			"Support for 'AWT Robot' not found.", "AWT Robot"));
		        return;
		    }

		    //detect the text on GUI	  
		    OCREngine ocrEngine = OCREngine.getOCREngine(ocrId, staf);
		        
		    scaleRatio = (scaleRatio <=0 )? ocrEngine.getdefaultZoomScale():scaleRatio;
		        
		    //detect the text in the image file
		    String text = ocrEngine.imageToText(buffimg, 
		    	                              langId, 
		    	                              null, 
		    	                              scaleRatio);

		    //get optional SubArea parameter
		  	if (command.equalsIgnoreCase(COMMAND_GETTEXTFROMGUI)) {
		  		// write the detected text to outputVar, which should be a variable name 
		  		if (!setVariable(outputVar, text)) {
		  			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		  		    log.logMessage(testRecordData.getFac(),
		  		                   " setVariable failure, variable: " + outputVar,
		  		                   FAILED_MESSAGE);
		  		    return;
		  		}
		  	} else if (command.equalsIgnoreCase(COMMAND_SAVETEXTFROMGUI)) {
		  		//write the detected text to outputVar, which should be a file name  
		  		outputVar = FileUtilities.normalizeFileSeparators(outputVar);
		  		//build File
		  		File  fn = new CaseInsensitiveFile(outputVar).toFile();
		  		if (!fn.isAbsolute()) {
		  			String pdir = null;
		  		    try{
		  		    	if( outputVar.indexOf(File.separator) > -1 ) {
		  		    		pdir = getVariable(STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY);
		  			    }else{
		  					pdir = getVariable(STAFHelper.SAFS_VAR_TESTDIRECTORY);
		  			    }
		  		    }catch(Exception x){}
		  		    	if ((pdir == null)||(pdir.equals(""))){
		  		    		String error = FAILStrings.text(FAILStrings.COULD_NOT_GET_VARS, 
		  		  				"Could not get one or more variable values.")+ 
		  		  				" "+ STAFHelper.SAFS_VAR_DATAPOOLDIRECTORY+", "+STAFHelper.SAFS_VAR_TESTDIRECTORY;
		  		    		this.issueActionOnXFailure(outputVar, error);
		  		    		return;
		  		    }
		  			fn = new CaseInsensitiveFile(pdir, outputVar).toFile();
		  		}
		  		  
		  		try {
		  			FileUtilities.writeStringToUTF8File(fn.getAbsolutePath(), text);
		  		} catch(Exception ex) {
		  		      throw new SAFSException(ex.toString());
		  		}
		  		  outputVar = fn.getAbsolutePath();
		  	} else {
		  		Log.debug("No code responsible for keyword: " + command);
		  		return;
		  	}

		  	//set status to ok
		  	String detail = genericText.convert(TXT_SUCCESS_2a, command + " successful using " + ocrId, command, ocrId) 
		  	                  + ". " + 
		       				  genericText.convert(GENStrings.BE_SAVED_TO, 
		       						              "'" + text + "' has been saved to '" + outputVar + "'", 
		       						              text, outputVar); 		 

		  	log.logMessage(testRecordData.getFac(), detail, GENERIC_MESSAGE);
		  	testRecordData.setStatusCode(StatusCodes.OK);
		}
    }	
	
	/**
	 * Note: As these are GUILess actions, so we will NOT force to wait for GUI's existence.<br> 
	 * @param action String, the keyword to execute
	 * @return boolean true if the keyword is GUI independant, means it can be executed without 'Window' and 'Component'.
	 */
	public static boolean isGUILess(String action){
		if( COMMAND_VERIFYVALUES.equalsIgnoreCase(action) ||
		    COMMAND_VERIFYVALUESNOTEQUAL.equalsIgnoreCase(action) ||
		    COMMAND_VERIFYVALUESIGNORECASE.equalsIgnoreCase(action) ||
			COMMAND_VERIFYVALUEEQUALS.equalsIgnoreCase(action) ||
			COMMAND_VERIFYVALUECONTAINS.equalsIgnoreCase(action) ||
			COMMAND_VERIFYVALUECONTAINSIGNORECASE.equalsIgnoreCase(action) ||
			COMMAND_VERIFYVALUEDOESNOTCONTAIN.equalsIgnoreCase(action) ||
			COMMAND_VERIFYTEXTFILETOFILE.equalsIgnoreCase(action) ||
			COMMAND_VERIFYFILETOFILE.equalsIgnoreCase(action) ||
			COMMAND_VERIFYBINARYFILETOFILE.equalsIgnoreCase(action) ||
			COMMAND_VERIFYCLIPBOARDTOFILE.equalsIgnoreCase(action) ||
			COMMAND_CLICKSCREENLOCATION.equalsIgnoreCase(action) ||
			COMMAND_CLICKSCREENPOINT.equalsIgnoreCase(action) ||
			COMMAND_DOUBLECLICKSCREENLOCATION.equalsIgnoreCase(action) ||
			COMMAND_DOUBLECLICKSCREENPOINT.equalsIgnoreCase(action) ||
			COMMAND_RIGHTCLICKSCREENLOCATION.equalsIgnoreCase(action) ||
			COMMAND_RIGHTCLICKSCREENPOINT.equalsIgnoreCase(action) ||
			COMMAND_HOVERSCREENLOCATION.equalsIgnoreCase(action) ||
			COMMAND_TYPEKEYS.equalsIgnoreCase(action) ||
			COMMAND_TYPECHARS.equalsIgnoreCase(action) ||
			COMMAND_TYPEENCRYPTION.equalsIgnoreCase(action) ||
			COMMAND_CLEARAPPMAPCACHE.equalsIgnoreCase(action)) {
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Note: We don't if the GUI exist or not, so we will NOT force to wait for GUI's existence.<br> 
	 * @param action String, the keyword to execute
	 * @return boolean, true if the keyword is to check GUI's existence
	 */
	public static boolean isCheckGUIExistence(String action){
		if(DriverCommands.WAITFORGUI_KEYWORD.equalsIgnoreCase(action) ||
		   GenericMasterFunctions.GUIDOESEXIST_KEYWORD.equalsIgnoreCase(action) ||
		   GenericMasterFunctions.GUIDOESNOTEXIST_KEYWORD.equalsIgnoreCase(action)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Note: For some component keywords, the existence of GUI is required, like Click, GetGUIImage etc.<br>
	 * While for others, the existence of GUI in NOT necessary, like VerifyValues, TypeKeys etc., for this<br>
	 * kind of keywords, we will not wait for GUI existence in {@link TestStepProcessor#process()}.<br>
	 * If the GUI existence is not necessary, then this method will return true.<br>
	 * @param action String, the keyword to execute
	 * @return boolean, true if there is no need to wait the existence of GUI.
	 * @see TestStepProcessor#process()
	 */
	public static boolean noWaitGUIExistence(String action){
		return TIDComponent.isCheckGUIExistence(action) || TIDComponent.isGUILess(action);
	}
	
	/**
	 * Check if the action is a REST action.
	 * @param flag String, it could be the WindowID or the WindowName
	 * @return boolean true if the action is RESTful.
	 */
	public static boolean isRESTFunction(String flag){
		return FLAG_SAFSREST.equalsIgnoreCase(flag);
	}
	
	/**
	 * Internal Component Function Processor to handle REST actions.
	 */
	protected class RESTComponent extends org.safs.ComponentFunction {
		protected String restFlag = null;
		protected String sessionID = null;
		/** If the action is going to be handled without session. 
		 * Default is false, which means the action will be handled within session. */
		protected boolean oneShotSessionStarted = false;
				
		RESTComponent (){
			super();
		}
		
		/**
		 * Process the record present in the provided testRecordData.
		 */
		@SuppressWarnings("unchecked")
		@Override
		public void process(){
			updateFromTestRecordData();

			if(action==null){
				componentFailureMessage(FAILStrings.text(FAILKEYS.ACTION_NOT_VALID)+":"+testRecordData.getInputRecord());
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				return;
			}
			
			String debugmsg = "TIDRestFunctions$RESTComponent.process(): ";
			
			testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
			try{
				restFlag = testRecordData.getWindowGuiId();
			}catch(SAFSException e){
				Log.warn(debugmsg + "window '"+windowName+"' recognition string is missing or invalid for restFlag.");
			}
			if(restFlag==null) restFlag = windowName;
			
			try{
				sessionID = testRecordData.getCompGuiId();
			}catch(SAFSException e){
				Log.warn(debugmsg + "component '"+compName+"' recognition string is missing or invalid for sessionID.");
			}

			try {
				oneShotSessionStarted = false;
				
				if(sessionID==null){
					if(requireSessionID()){
						throw new SAFSException("The session ID is missing for action '"+action+"'!");
					}else{
						//This is one-shot connection, we ourselves will close the session after keyword execution.
						startSession();
					}
				}
				if (params != null) {
					iterator = params.iterator();
					Log.info(debugmsg + "action: "+action+" win: "+ windowName +"; comp: "+ compName+"; with params: "+params);
				} else{
					Log.info(debugmsg +  "action: "+action+" win: "+ windowName +"; comp: "+ compName+" without parameters.");
				}

				if ( TIDRestFunctions.RESTENDSERVICESESSION_KEYWORD.equalsIgnoreCase(action)) {
					actionEndServiceSession();
				}
				else if ( TIDRestFunctions.RESTDELETEBINARY_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.DELETE_METHOD, Headers.BINARY_TYPE);
				} else if ( TIDRestFunctions.RESTDELETECSS_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.DELETE_METHOD, Headers.CSS_TYPE);
				} else if ( TIDRestFunctions.RESTDELETEHTML_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.DELETE_METHOD, Headers.HTML_TYPE);
				} else if ( TIDRestFunctions.RESTDELETEIMAGE_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.DELETE_METHOD, Headers.IMAGE_TYPE);
				} else if ( TIDRestFunctions.RESTDELETEJSON_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.DELETE_METHOD, Headers.JSON_TYPE);
				} else if ( TIDRestFunctions.RESTDELETESCRIPT_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.DELETE_METHOD, Headers.SCRIPT_TYPE);
				} else if ( TIDRestFunctions.RESTDELETETEXT_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.DELETE_METHOD, Headers.TEXT_TYPE);
				} else if ( TIDRestFunctions.RESTDELETEXML_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.DELETE_METHOD, Headers.XML_TYPE);
				}
				else if ( TIDRestFunctions.RESTGETBINARY_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.GET_METHOD, Headers.BINARY_TYPE);
				} else if ( TIDRestFunctions.RESTGETCSS_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.GET_METHOD, Headers.CSS_TYPE);
				} else if ( TIDRestFunctions.RESTGETHTML_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.GET_METHOD, Headers.HTML_TYPE);
				} else if ( TIDRestFunctions.RESTGETIMAGE_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.GET_METHOD, Headers.IMAGE_TYPE);
				} else if ( TIDRestFunctions.RESTGETJSON_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.GET_METHOD, Headers.JSON_TYPE);
				} else if ( TIDRestFunctions.RESTGETSCRIPT_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.GET_METHOD, Headers.SCRIPT_TYPE);
				} else if ( TIDRestFunctions.RESTGETTEXT_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.GET_METHOD, Headers.TEXT_TYPE);
				} else if ( TIDRestFunctions.RESTGETXML_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.GET_METHOD, Headers.XML_TYPE);
				}
				else if ( TIDRestFunctions.RESTHEADBINARY_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.HEAD_METHOD, Headers.BINARY_TYPE);
				} else if ( TIDRestFunctions.RESTHEADCSS_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.HEAD_METHOD, Headers.CSS_TYPE);
				} else if ( TIDRestFunctions.RESTHEADHTML_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.HEAD_METHOD, Headers.HTML_TYPE);
				} else if ( TIDRestFunctions.RESTHEADIMAGE_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.HEAD_METHOD, Headers.IMAGE_TYPE);
				} else if ( TIDRestFunctions.RESTHEADJSON_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.HEAD_METHOD, Headers.JSON_TYPE);
				} else if ( TIDRestFunctions.RESTHEADSCRIPT_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.HEAD_METHOD, Headers.SCRIPT_TYPE);
				} else if ( TIDRestFunctions.RESTHEADTEXT_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.HEAD_METHOD, Headers.TEXT_TYPE);
				} else if ( TIDRestFunctions.RESTHEADXML_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.HEAD_METHOD, Headers.XML_TYPE);
				}
				else if ( TIDRestFunctions.RESTPATCHBINARY_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.PATCH_METHOD, Headers.BINARY_TYPE);
				} else if ( TIDRestFunctions.RESTPATCHCSS_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.PATCH_METHOD, Headers.CSS_TYPE);
				} else if ( TIDRestFunctions.RESTPATCHHTML_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.PATCH_METHOD, Headers.HTML_TYPE);
				} else if ( TIDRestFunctions.RESTPATCHIMAGE_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.PATCH_METHOD, Headers.IMAGE_TYPE);
				} else if ( TIDRestFunctions.RESTPATCHJSON_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.PATCH_METHOD, Headers.JSON_TYPE);
				} else if ( TIDRestFunctions.RESTPATCHSCRIPT_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.PATCH_METHOD, Headers.SCRIPT_TYPE);
				} else if ( TIDRestFunctions.RESTPATCHTEXT_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.PATCH_METHOD, Headers.TEXT_TYPE);
				} else if ( TIDRestFunctions.RESTPATCHXML_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.PATCH_METHOD, Headers.XML_TYPE);
				}
				else if ( TIDRestFunctions.RESTPOSTBINARY_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.POST_METHOD, Headers.BINARY_TYPE);
				} else if ( TIDRestFunctions.RESTPOSTCSS_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.POST_METHOD, Headers.CSS_TYPE);
				} else if ( TIDRestFunctions.RESTPOSTHTML_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.POST_METHOD, Headers.HTML_TYPE);
				} else if ( TIDRestFunctions.RESTPOSTIMAGE_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.POST_METHOD, Headers.IMAGE_TYPE);
				} else if ( TIDRestFunctions.RESTPOSTJSON_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.POST_METHOD, Headers.JSON_TYPE);
				} else if ( TIDRestFunctions.RESTPOSTSCRIPT_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.POST_METHOD, Headers.SCRIPT_TYPE);
				} else if ( TIDRestFunctions.RESTPOSTTEXT_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.POST_METHOD, Headers.TEXT_TYPE);
				} else if ( TIDRestFunctions.RESTPOSTXML_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.POST_METHOD, Headers.XML_TYPE);
				}
				else if ( TIDRestFunctions.RESTPUTBINARY_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.PUT_METHOD, Headers.BINARY_TYPE);
				} else if ( TIDRestFunctions.RESTPUTCSS_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.PUT_METHOD, Headers.CSS_TYPE);
				} else if ( TIDRestFunctions.RESTPUTHTML_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.PUT_METHOD, Headers.HTML_TYPE);
				} else if ( TIDRestFunctions.RESTPUTIMAGE_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.PUT_METHOD, Headers.IMAGE_TYPE);
				} else if ( TIDRestFunctions.RESTPUTJSON_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.PUT_METHOD, Headers.JSON_TYPE);
				} else if ( TIDRestFunctions.RESTPUTSCRIPT_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.PUT_METHOD, Headers.SCRIPT_TYPE);
				} else if ( TIDRestFunctions.RESTPUTTEXT_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.PUT_METHOD, Headers.TEXT_TYPE);
				} else if ( TIDRestFunctions.RESTPUTXML_KEYWORD.equalsIgnoreCase(action) ) {
					actionRequest(REST.PUT_METHOD, Headers.XML_TYPE);
				}
				else if ( TIDRestFunctions.RESTREQUEST_KEYWORD.equalsIgnoreCase(action) ) {
					actionHttpRequest();
				} else if ( TIDRestFunctions.RESTSTARTSERVICESESSION_KEYWORD.equalsIgnoreCase(action) ) {
					actionStartServiceSession();
				} else {
					throw new SAFSException("The action "+action+" is NOT supported in RESTComponent.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
				}

			}catch( SAFSException se ) {
				if(SAFSException.CODE_ACTION_NOT_SUPPORTED.equals(se.getCode())){
					//Let the other engine to process
					Log.info(debugmsg+" '"+action+"' is not supported here.");
					testRecordData.setStatusCode( StatusCodes.SCRIPT_NOT_EXECUTED );
				}else{
					testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
					String message = "Met "+StringUtils.debugmsg(se);
					String detail = FAILStrings.convert(FAILStrings.STANDARD_ERROR, 
							action +" failure in table "+ testRecordData.getFilename() + " at Line " + testRecordData.getLineNumber(), 
							action, testRecordData.getFilename(), String.valueOf(testRecordData.getLineNumber()));

					log.logMessage(testRecordData.getFac(), message, detail, FAILED_MESSAGE);
				}

			}catch (Throwable e){
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				String message = "Met "+StringUtils.debugmsg(e);
				String detail = FAILStrings.convert(FAILStrings.STANDARD_ERROR, 
						action +" failure in table "+ testRecordData.getFilename() + " at Line " + testRecordData.getLineNumber(), 
						action, testRecordData.getFilename(), String.valueOf(testRecordData.getLineNumber()));
					log.logMessage(testRecordData.getFac(), message, detail, FAILED_MESSAGE);

			}finally{
				if(oneShotSessionStarted){
					try{
						endSession();
					}catch(SAFSException e){
						Log.warn(debugmsg+"Failed to stop REST Session, due to "+StringUtils.debugmsg(e));
					}
				}
			}
			
			if (testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED) {
				//componentProcess();//handle Generic keywords
			} else {
				Log.debug(debugmsg+"'"+action+"' has been processed\n with testrecorddata"+testRecordData+"\n with params "+params);
			}
		}
		
		/**
		 * For some keywords the "sessionID" is optional; for others it is required.
		 * @return boolean true if the "sessionID" is required.
		 */
		protected boolean requireSessionID(){
			return TIDRestFunctions.RESTENDSERVICESESSION_KEYWORD.equalsIgnoreCase(action) ||
				   TIDRestFunctions.RESTSTARTSERVICESESSION_KEYWORD.equalsIgnoreCase(action);
		}
		
		private synchronized String getUniqueID(String value){
			return FLAG_SAFSREST+value+System.currentTimeMillis();
		}
		
		protected void startSession() throws SAFSException{
			//Create the Service object: 
			Service service = new Service(getUniqueID("_session_"));
			//Assign sessionID
			sessionID = service.getServiceId();
			
			//TODO Load related assets for starting the session, like authentication information.
			//TODO Do we need to register the service, probably NOT.
			
			oneShotSessionStarted = true;
		}
		protected void endSession() throws SAFSException{
			//TODO Clean up everything for a session.
			sessionID = null;
			oneShotSessionStarted = false;
		}

		/**
		 * Write the details of response and request into the debug log.<br>
		 * Store the Response object into a Map with a generated ID, which
		 * will be saved to the variable responseIdVar.<br>
		 * @param response Response, the response returned from invocation of a rest service.
		 * @param responseIdVar String, the variable to store the response ID
		 * @throws SAFSException if it failed to store the responseID to a variable
		 */
		protected void handleResponse(Response response, String responseIdVar) throws SAFSException{
			Log.debug(response);
			String responseID = getUniqueID("_response_"); 
			addToResponseMap(responseID, response);
			setVariable(responseIdVar, responseID);
		}
		
		/**
		 * Check the field sessionID is not null.<br>
		 * If it is null, log failure message about sessionID and set the status code to failure.<br> 
		 * 
		 * @return boolean true if the sessionID is not null.
		 */
		private boolean checkSessionID(){
			if (sessionID==null) {
				//It is not really a parameter, it is the field #3 in the test record.
				issueParameterValueFailure("sessionID");
				return false;
			}
			return true;
		}
		
		/**
		 * Handle the keywords like:<br/>
		 * <ul>
		 * 	 <li>{@link TIDRestFunctions#RESTGETBINARY_KEYWORD}
		 *   <li>{@link TIDRestFunctions#RESTGETXML_KEYWORD}
		 *   <li>{@link TIDRestFunctions#RESTPOSTBINARY_KEYWORD}
		 *   <li>{@link TIDRestFunctions#RESTPOSTXML_KEYWORD}
		 *   <li>...
		 * </ul>
		 * @param method String, the HTTP method to handle, like "GET" "POST" "PUT" etc.
		 * @param type   String, the header type, like "BINARY" "JOSN" etc.
		 */
		private void actionRequest(String method, String type){
			String message = null;
			String description = null;
			Response response = null;
			
			if (!checkSessionID()) return;
			
			if (params.size() < 2) {
				issueParameterCountFailure();
				return;
			}
			String relativeURI = iterator.next();
			String responseIdVar = iterator.next();
			String body = iterator.hasNext()? iterator.next():null;
			String customHeaders = iterator.hasNext()? iterator.next():null;
			String customeAuthentication = iterator.hasNext()? iterator.next():null;
			
			try {
				//TODO handle the extra authentication information
				if(customeAuthentication!=null){
					
				}
				//TODO handle the extra headers information
				if(customHeaders!=null){
					
				}
				response = REST.request(sessionID, method, relativeURI, Headers.getHeadersForType(type), body);
				
				handleResponse(response, responseIdVar);
				
				message = GENStrings.convert(GENStrings.SUCCESS_3, 
						restFlag+":"+sessionID+" "+action+" successful.", 
						restFlag, sessionID, action);
				description = "relativeURI: "+relativeURI+"\n"+
						      responseIdVar+": "+getVariable(responseIdVar);
				
				logMessage( message, description, PASSED_MESSAGE);
				setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
			}catch(Exception e){
				String exceptionMsg = StringUtils.debugmsg(e);
				message = FAILStrings.convert(FAILStrings.GENERIC_ERROR, 
						"*** ERROR *** "+exceptionMsg, exceptionMsg);
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
				setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
			}
		}
		
		/** 
		 * Handle the custom request.<br/>
		 * User will set the real HTTP headers by himself.<br/>
		 */
		private void actionHttpRequest(){
			String message = null;
			String description = null;
			Response response = null;
			
			if (!checkSessionID()) return;
			
			if (params.size() < 3) {
				issueParameterCountFailure();
				return;
			}
			String method = iterator.next();
			String relativeURI = iterator.next();
			String responseIdVar = iterator.next();
			String body = iterator.hasNext()? iterator.next():null;
			String customHeaders = iterator.hasNext()? iterator.next():null;
			String customeAuthentication = iterator.hasNext()? iterator.next():null;
			
			try { 
				//TODO handle the extra authentication information
				if(customeAuthentication!=null){
					
				}
				
				response = REST.request(sessionID, method, relativeURI, customHeaders, body);
				
				handleResponse(response, responseIdVar);
				
				String actionMsg = action +" "+method;
				message = GENStrings.convert(GENStrings.SUCCESS_3, 
						restFlag+":"+sessionID+" "+actionMsg+" successful.", 
						restFlag, sessionID, actionMsg);
				description = "relativeURI: "+relativeURI+"\n"+
						      responseIdVar+": "+getVariable(responseIdVar);
				
				logMessage( message, description, PASSED_MESSAGE);
				setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
			}catch(Exception e){
				String exceptionMsg = StringUtils.debugmsg(e);
				message = FAILStrings.convert(FAILStrings.GENERIC_ERROR, 
						"*** ERROR *** "+exceptionMsg, exceptionMsg);
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
				setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
			}
		}
		
		private void actionEndServiceSession(){
			String message = null;
			String description = null;
			
			if (!checkSessionID()) return;
			
			try { 
				Services.deleteService(sessionID);
				
				message = GENStrings.convert(GENStrings.SUCCESS_3, 
						restFlag+":"+sessionID+" "+action+" successful.", restFlag, sessionID, action);
				
				logMessage( message, description, PASSED_MESSAGE);
				setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
			}catch(Exception e){
				String exceptionMsg = StringUtils.debugmsg(e);
				message = FAILStrings.convert(FAILStrings.GENERIC_ERROR, 
						"*** ERROR *** "+exceptionMsg, exceptionMsg);
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
				setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
			}
		}
		
		private void actionStartServiceSession(){
			String message = null;
			String description = null;
			
			if (!checkSessionID()) return;
			
			if (params.size() < 1) {
				issueParameterCountFailure();
				return;
			}
			String baseURL = iterator.next();
			String customeAuthentication = iterator.hasNext()? iterator.next():null;
			
			try{
				//Create the Service object 
				Service service = new Service(sessionID, baseURL);
				Services.addService(service);
				
				//TODO handle the extra authentication information
				if(customeAuthentication!=null){
					
				}
				
				message = GENStrings.convert(GENStrings.SUCCESS_3, 
						restFlag+":"+sessionID+" "+action+" successful.", restFlag, sessionID, action);
						
				logMessage( message, description, PASSED_MESSAGE);
				setTRDStatus(testRecordData, DriverConstant.STATUS_NO_SCRIPT_FAILURE);
			}catch(Exception e){
				String exceptionMsg = StringUtils.debugmsg(e);
				message = FAILStrings.convert(FAILStrings.GENERIC_ERROR, 
						"*** ERROR *** "+exceptionMsg, exceptionMsg);
				standardErrorMessage(testRecordData, message, testRecordData.getInputRecord());
				setTRDStatus(testRecordData, DriverConstant.STATUS_GENERAL_SCRIPT_FAILURE);
			}
		}

	}
}

