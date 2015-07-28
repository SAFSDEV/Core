/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.safs.DDGUIUtilities;
import org.safs.Domains;
import org.safs.DriverCommand;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSObjectNotFoundException;
import org.safs.STAFHelper;
import org.safs.StatusCodes;
import org.safs.TestRecordHelper;
import org.safs.image.ImageUtils;
import org.safs.logging.LogUtilities;
import org.safs.rational.win.CFWinMenuBar;
import org.safs.text.FAILStrings;
import org.safs.text.GENStrings;
import org.safs.tools.engines.TIDDriverCommands;

import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.ProcessTestObject;
import com.rational.test.ft.object.interfaces.RootTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.script.IOptionName;
import com.rational.test.ft.script.RationalTestScript;
import com.rational.test.ft.script.RunException;


/**
 * <br><em>Purpose:</em> DCDriverCommand, process a default generic driver command
 * <br><em>Lifetime:</em> instantiated by DriverCommandProcessor
 * im
 * <p>
 * @author  Doug Bauman
 * @since   JUN 14, 2003
 *
 *   <br>   Aug 25, 2003    (DBauman) Original Release
 *   <br>   Carl Nagle, SEP 16, 20003 Enabled the use of new LogUtilities
 *   <br>   Nov 14, 2003    (DBauman) adding commands:<br>
 *    CloseApplication, LaunchApplication, 
 *   <br>   Nov 15, 2005	(bolawl) 	Added OnGUI(Not)Exist(s)GotoBlockID commands RJL.
 *   <br>   Dec 09, 2005	(bolawl) 	Added SetContext command RJL.
 *   <br>   Oct 23, 2007	(CANAGL) 	Fixed NullPointerException in waitForPropertyValue
 *   <br>   May 05, 2008    (JunwuMa) 	Added OnMenuItemContainsStateGotoBlockID
 *   <br>   JUL 28, 2008    (LeiWang) 	Modify method OnMenuItemContainsStateGotoBlockID to adapt .NET application.
 *   <br>   JUL 31, 2008    (CANAGL) 	Add CASEINSENSITIVE support to WaitForPropertyValue commands.
 *   <br>   JUN 12, 2008    (LeiWang)	Modify method: OnMenuItemContainsStateGotoCommand()
 *   								 	For supporting the menu (type is .Menubar) of win domain.
 *   <br>   JUL 20, 2009    (CANAGL) 	Added more significant cache clearing for some driver commands.
 *   <br>   AUG 05, 2008    (LeiWang)	Add keyword: SetSecsSeekComponent,SetSecsDelayRetryComponent,SetSecsAfterWindowActive
 *   												 GetSecsSeekComponent,GetSecsDelayRetryComponent,GetSecsAfterWindowActive
 *   <br>   DEC 14, 2009    (singrk)    Added keyword:ScrollWheel.  
 *   <br>   JAN 12, 2008    (singrk)	Added keyword: GetSecsBeforeMouseUp,GetSecsBeforeMouseDown,SetSecsBeforeMouseUp
 *   												   SetSecsBeforeMouseDown
 *   <br>   AUG 23, 2010	(LeiWang)	Modify method process(): If we use Mixed-Mode-RS, that is OBT-RS for window, IBT-RS for component,
 *                                                               we need to change the window's RS to IBT-RS and let the TIDDriverCommands to
 *                                                               process it. Only WaitForGUI, WaitForGUIGone, OnGUIExistsGotoBlockID and 
 *                                                               OnGUINotExistGotoBlockID will be affected as the other GUI-related keywords
 *                                                               are not supported in TIDXXXCommands (Driver, Log, Flow, Counter etc.)
 **/
public class DCDriverCommand extends DriverCommand {

  public static final String CALLSCRIPT                    = "CallScript";
  public static final String WAITFORGUI                    = "WaitForGUI";
  public static final String WAITFORGUIGONE                = "WaitForGUIGone";
  public static final String CLEARAPPMAPCACHE              = "ClearAppMapCache";
  public static final String CLOSEAPPLICATION              = "CloseApplication";
  public static final String LAUNCHAPPLICATION             = "LaunchApplication";
  public static final String WAITFORPROPERTYVALUE          = "WaitForPropertyValue";
  public static final String WAITFORPROPERTYVALUEGONE      = "WaitForPropertyValueGone";
  public static final String WAITFORWEBPAGE                = "WaitForWebPage";
  public static final String SETCONTEXT                    = "SetContext";
  public static final String SETFOCUS                      = "SetFocus";
  public static final String CAPTUREMOUSEPOSITIONONSCREEN  = "CaptureMousePositionOnScreen";
  public static final String ONGUIEXISTSGOTOBLOCKID        = "OnGUIExistsGotoBlockID";
  public static final String ONGUINOTEXISTGOTOBLOCKID      = "OnGUINotExistGotoBlockID";
  public static final String ENABLE_DOMAIN                 = "EnableDomain";
  public static final String DISABLE_DOMAIN                = "DisableDomain";
  public static final String ONMENUITEMCONTAINSSTATEGOTOBLOCKID = "OnMenuItemContainsStateGotoBlockID";
  
  public static final String SET_SECS_SEEK_COMPONENT	 		= "SetSecsSeekComponent";//MAXIMUM_FIND_OBJECT_TIME
  public static final String SET_SECS_DELAY_RETRY_COMPONENT		= "SetSecsDelayRetryComponent";//WAIT_FOR_EXISTENCE_DELAY_BETWEEN_RETRIES
  public static final String SET_SECS_AFTER_WIN_ACTIVE	 		= "SetSecsAfterWindowActive";//DELAY_AFTER_WINDOW_ACTIVATE 
  
  public static final String GET_SECS_SEEK_COMPONENT	 		= "GetSecsSeekComponent";//MAXIMUM_FIND_OBJECT_TIME
  public static final String GET_SECS_DELAY_RETRY_COMPONENT		= "GetSecsDelayRetryComponent";//WAIT_FOR_EXISTENCE_DELAY_BETWEEN_RETRIES
  public static final String GET_SECS_AFTER_WIN_ACTIVE	 		= "GetSecsAfterWindowActive";//DELAY_AFTER_WINDOW_ACTIVATE 
  public static final String SCROLLWHEEL                        = "ScrollWheel";
  public static final String GET_SECS_BEFORE_MOUSE_UP	 		= "GetSecsBeforeMouseUp";//DELAY_BEFORE_MOUSE_UP
  public static final String GET_SECS_BEFORE_MOUSE_DOWN	 		= "GetSecsBeforeMouseDown";//DELAY_BEFORE_MOUSE_DOWN
  public static final String SET_SECS_BEFORE_MOUSE_UP	 		= "SetSecsBeforeMouseUp";//DELAY_BEFORE_MOUSE_UP
  public static final String SET_SECS_BEFORE_MOUSE_DOWN	 		= "SetSecsBeforeMouseDown";//DELAY_BEFORE_MOUSE_DOWN
  
  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public DCDriverCommand () {
    super();
  }

  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <br>This is a driver command processor.
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing; processedCommand is set to false
   * if we do not recognize this command
   * <br><em>State Read:</em>   {@link #testRecordData}, {@link #params}
   * <br><em>Assumptions:</em>  none
   **/
  public void process() {
    try {
      String cmd = testRecordData.getCommand();
      
      //For those keywords supported in both TID and RJ engines, we should test if the
      //RS is in Mixed-Mode, if yes, we should change the parent's RS to IBT-format RS
      //and re-send the TestRecord to the TID engine,who will do the real work
      if(cmd.equalsIgnoreCase(WAITFORGUI) ||
         cmd.equalsIgnoreCase(WAITFORGUIGONE) ||
         cmd.equalsIgnoreCase(ONGUINOTEXISTGOTOBLOCKID) ||
         cmd.equalsIgnoreCase(ONGUIEXISTSGOTOBLOCKID)){
    	  Iterator iter = params.iterator();
    	  if(cmd.equalsIgnoreCase(ONGUIEXISTSGOTOBLOCKID) ||
    		 cmd.equalsIgnoreCase(ONGUINOTEXISTGOTOBLOCKID)){
    		  //Skip the field BlockId in TestRecord
    		  iter.next();
    	  }
    	  testRecordData.setWindowName((String)iter.next());
    	  testRecordData.setCompName((String)iter.next());

    	  //If we use Mixed-Mode for RS, we should modify its parent RS to IBT-format
	      if(testRecordData.isMixedRsUsed()){
	    	  RDDGUIUtilities utils = (RDDGUIUtilities) ((TestRecordHelper)testRecordData).getDDGUtils();
	    	  String windowName = testRecordData.getWindowName();
	    	  //Find top window test object through RJ engine
	    	  GuiTestObject windowGuiObject = (GuiTestObject) utils.getTestObject(testRecordData.getAppMapName(), windowName,windowName);
	    	  if(windowGuiObject!=null){
	    		  Rectangle windowRect = windowGuiObject.getClippedScreenRectangle();
	    		  if(windowRect!=null){
	    	    		String windowRS = ImageUtils.MOD_IMAGE_RECT+ImageUtils.MOD_EQ+
						  windowRect.x+ImageUtils.MOD_COMMA+
						  windowRect.y+ImageUtils.MOD_COMMA+
						  windowRect.width+ImageUtils.MOD_COMMA+
						  windowRect.height;
	    	    		//Set parent window's RS to a IBT-format RS
	    	    		testRecordData.setWindowGuiId(windowRS);
	    		  }else{
	    			  throw new SAFSException(testRecordData.getCommand(),"can't get ClippedScreenRectangle for "+windowName);    			  
	    		  }
	    	  }else{
	    		  throw new SAFSException(testRecordData.getCommand(),windowName+" can't be found!");
	    	  }
	    	  
	    	  //Send the TestRecord back to TIDDriverCommands.java (an tid engine) to process
	    	  //But TIDDriverCommands is an engine, we need to initialize it with a driver?
	    	  //Here we just initialize it with a LogUtility, NOT sure if this if safe enough????
	    	  STAFHelper staf = testRecordData.getSTAFHelper();
	    	  if(staf==null){
	    		  String errmsg = "RS is Mixed-Mode, but STAF can't be retrieved from TestRecord, so TIDDriverCommands can't be created.";
	    		  throw new SAFSException(testRecordData.getCommand(),errmsg);
	    	  }
	    	  TIDDriverCommands tidDC = new TIDDriverCommands(new LogUtilities(staf));
	    	
	    	  tidDC.processRecord(testRecordData);
	    	  return;
	      }
      }

      if (cmd.equalsIgnoreCase(CALLSCRIPT))                         { callScript();                  } 
      else if (cmd.equalsIgnoreCase( WAITFORGUI))                   { waitForGui();                 } 
      else if (cmd.equalsIgnoreCase( WAITFORGUIGONE))               { waitForGuiGone();             } 
      else if (cmd.equalsIgnoreCase( CLEARAPPMAPCACHE))             { clearAppMapCache();           } 
      else if (cmd.equalsIgnoreCase( CLOSEAPPLICATION))             { application(false);          } 
      else if (cmd.equalsIgnoreCase( LAUNCHAPPLICATION))            { application(true);           } 
      else if (cmd.equalsIgnoreCase( WAITFORPROPERTYVALUE))         { waitForPropertyValue(false); } 
      else if (cmd.equalsIgnoreCase( WAITFORPROPERTYVALUEGONE))     { waitForPropertyValue(true);  } 
      else if (cmd.equalsIgnoreCase( WAITFORWEBPAGE))               { waitForWebPage();             } 
      else if (cmd.equalsIgnoreCase( SETCONTEXT) ||
      		    cmd.equalsIgnoreCase( SETFOCUS))                     { setFocus();                   } 
      else if (cmd.equalsIgnoreCase( CAPTUREMOUSEPOSITIONONSCREEN)) { captureMousePositionOnScreen();} 
      else if (cmd.equalsIgnoreCase( ONGUIEXISTSGOTOBLOCKID))       { onGUIGotoCommands(true);      } 
      else if (cmd.equalsIgnoreCase( ONGUINOTEXISTGOTOBLOCKID))     { onGUIGotoCommands(false);     } 
      else if (cmd.equalsIgnoreCase( ONMENUITEMCONTAINSSTATEGOTOBLOCKID)){ OnMenuItemContainsStateGotoCommand();     } 
      else if (cmd.equalsIgnoreCase( ENABLE_DOMAIN))                { enableDomain(true);           } 
      else if (cmd.equalsIgnoreCase( DISABLE_DOMAIN))               { enableDomain(false);          } 
      else if (cmd.equalsIgnoreCase(SET_SECS_SEEK_COMPONENT) ||
    		   cmd.equalsIgnoreCase(SET_SECS_DELAY_RETRY_COMPONENT) ||
    		   cmd.equalsIgnoreCase(SET_SECS_BEFORE_MOUSE_UP)||
    		   cmd.equalsIgnoreCase(SET_SECS_BEFORE_MOUSE_DOWN)||
    		   cmd.equalsIgnoreCase(SET_SECS_AFTER_WIN_ACTIVE)){
    	  setScriptOptions();
      }else if (cmd.equalsIgnoreCase(GET_SECS_SEEK_COMPONENT) ||
   		   		cmd.equalsIgnoreCase(GET_SECS_DELAY_RETRY_COMPONENT) ||
   		     	cmd.equalsIgnoreCase(GET_SECS_BEFORE_MOUSE_UP)||
 		        cmd.equalsIgnoreCase(GET_SECS_BEFORE_MOUSE_DOWN)||
   		   		cmd.equalsIgnoreCase(GET_SECS_AFTER_WIN_ACTIVE)){
    	  getScriptOptions();
      }else if (cmd.equalsIgnoreCase(SCROLLWHEEL)){
    	  scrollWheel(); 	  
      }else {
        setRecordProcessed(false);
      }
    } catch (SAFSException ex) {
      //ex.printStackTrace();
      testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
      log.logMessage(testRecordData.getFac(),
                     "SAFSException: "+ex.getMessage(),
                     FAILED_MESSAGE);
    }
  }
  
  /** <br><em>Purpose:</em> scrollWheel
   **/
  
  private void scrollWheel() {
    String action = testRecordData.getCommand();
    if (params.size() <= 1 || params.size() > 2) {
    	testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(), getClass().getName()+", "+action +
                           ": only one parameter required,which supposed to be the number of mouse wheel clicks to move.",
                           FAILED_MESSAGE);
        return;
    }
  	  
  	try{
		Iterator iterator = params.iterator();
		String param = (String) iterator.next();
		Integer iClicks = new Integer(param);
		Log.info(" iClicks: "+iClicks);
		//A positive value indicates the wheel was moved forward (up) and a negative value indicates the wheel was moved backward (down).
		Script.getRootTestObject().emitLowLevelEvent(Script.mouseWheel(iClicks));
		  	
		log.logMessage(testRecordData.getFac(),genericText.convert(TXT_SUCCESS_2, action, action, param),GENERIC_MESSAGE);
		testRecordData.setStatusCode(StatusCodes.OK);	
			
			  
	}catch (NumberFormatException nfe) {
		testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
        log.logMessage(testRecordData.getFac(), getClass().getName()+", "+action + ": "+ nfe.getMessage()+
                    ": Param value is not an integer: "+action+ " requires integer value.",FAILED_MESSAGE);
  	  
	}
  }

	/** <br><em>Purpose:</em> set script's options
   **/
  private void setScriptOptions() {
	String debugmsg = getClass().getName()+".setScriptOptions() ";  
    if (params.size() < 1) {
    	this.issueParameterCountFailure("Option Value");
        return;
    }
    Iterator iterator = params.iterator();
    //get the option value
    String optionValue = (String) iterator.next();
    Log.info(debugmsg +" optionValue: "+optionValue);
    //Convert to double
    Double time = 0.0;
    try{
    	time = Double.parseDouble(optionValue);
    	Log.info(debugmsg +" time: "+time);
    }catch(NumberFormatException e){
       	this.issueParameterValueFailure("Option Value");
        return;
    }
    String command = getTestRecordData().getCommand();
    String optionName = "";

    try{
      if(command.equalsIgnoreCase(SET_SECS_SEEK_COMPONENT)){
    	  optionName = IOptionName.MAXIMUM_FIND_OBJECT_TIME;
      }else if(command.equalsIgnoreCase(SET_SECS_DELAY_RETRY_COMPONENT)){
    	optionName = IOptionName.WAIT_FOR_EXISTENCE_DELAY_BETWEEN_RETRIES;
      }else if(command.equalsIgnoreCase(SET_SECS_AFTER_WIN_ACTIVE)){
    	optionName = IOptionName.DELAY_AFTER_WINDOW_ACTIVATE;
      }else if(command.equalsIgnoreCase(SET_SECS_BEFORE_MOUSE_UP)){
    	optionName = IOptionName.DELAY_BEFORE_MOUSE_UP;
      }else if(command.equalsIgnoreCase(SET_SECS_BEFORE_MOUSE_DOWN)){
    	optionName = IOptionName.DELAY_BEFORE_MOUSE_DOWN;
      }
    
      Script.setOption(optionName, time);
      log.logMessage(testRecordData.getFac(),
                     genericText.convert(TXT_SUCCESS_2,
                     command+" "+ optionValue +" successful.",command, optionValue),
                     GENERIC_MESSAGE);
      testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
    }catch (Exception e){
        Log.debug(debugmsg+e.getMessage());
        this.issueActionFailure(FAILStrings.convert(FAILStrings.COULD_NOT_SET,
      		  "Could not set '"+ optionName +"' to '"+ optionValue +"'.", optionName,optionValue));
    }
  }
 
  /** <br><em>Purpose:</em> get script's options value, and save it to a staf variable
   **/
  private void getScriptOptions() {
	String debugmsg = getClass().getName()+".getScriptOptions() ";  
    if (params.size() < 1) {
    	this.issueParameterCountFailure("staf variable");
        return;
    }
    Iterator iterator = params.iterator();
    //get the variable to store the option's value
    String stafVariable = (String) iterator.next();
    Log.info(debugmsg +" staf variable: "+stafVariable);
    
    String command = getTestRecordData().getCommand();
    String optionName = "";
    String optionValue = null;
    
    try{
      if(command.equalsIgnoreCase(GET_SECS_SEEK_COMPONENT)){
    	  optionName = IOptionName.MAXIMUM_FIND_OBJECT_TIME;
      }else if(command.equalsIgnoreCase(GET_SECS_DELAY_RETRY_COMPONENT)){
    	optionName = IOptionName.WAIT_FOR_EXISTENCE_DELAY_BETWEEN_RETRIES;
      }else if(command.equalsIgnoreCase(GET_SECS_AFTER_WIN_ACTIVE)){
    	optionName = IOptionName.DELAY_AFTER_WINDOW_ACTIVATE;
      }else if(command.equalsIgnoreCase(GET_SECS_BEFORE_MOUSE_UP)){
      	optionName = IOptionName.DELAY_BEFORE_MOUSE_UP;
        }else if(command.equalsIgnoreCase(GET_SECS_BEFORE_MOUSE_DOWN)){
      	optionName = IOptionName.DELAY_BEFORE_MOUSE_DOWN;
        }  	
      
    
      optionValue = Script.getOption(optionName).toString();
      Log.info(debugmsg +" optionValue: "+optionValue);
      //Save the optionValue to a staf variable
      STAFHelper helper = getTestRecordData().getSTAFHelper();
      boolean saveOk = helper.setVariable(stafVariable, optionValue);
      
      if(saveOk){
    	  log.logMessage(testRecordData.getFac(),
                  genericText.convert(TXT_SUCCESS_2,
                  command+" "+ optionValue +" successful.",command, optionValue),
                  GENERIC_MESSAGE);
    	  testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);  
      }else{
    	  //error occurs when saving option's value to a staf variable
    	  Log.debug(debugmsg+" Can not save option value "+optionValue +" to a staf variable.");
          this.issueActionFailure(FAILStrings.text(FAILStrings.COULD_NOT_SET_VARS,
        		  								   "Could not set one or more variable values."));
      }
    }catch (Exception e){
      Log.debug(debugmsg+e.getMessage());
      this.issueActionFailure(FAILStrings.convert(FAILStrings.COULD_NOT_GET,
    		  "Could not get '"+ optionName +"'.", optionName));
    }
  }
  
  /** <br><em>Purpose:</em> callScript
   **/
  private void callScript () {
    if (params.size() < 1) {
    	this.issueParameterCountFailure("ScriptName");
        return;
    }
    Iterator iterator = params.iterator();
    // get the script name
    String scriptName = (String) iterator.next();
    Log.info(".............................scriptName: "+scriptName);
    // now call the script
    try {
      Script script = ((RTestRecordData)testRecordData).getScript();
      script.localCallScript(scriptName);    
      log.logMessage(testRecordData.getFac(),
                     genericText.convert(TXT_SUCCESS_2,
                     getTestRecordData().getCommand()+" "+ scriptName +" successful.",
                     getTestRecordData().getCommand(), scriptName),
                     GENERIC_MESSAGE);
      testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
    } catch (com.rational.test.ft.ObjectNotFoundException onfe) {
      this.issueActionFailure(FAILStrings.convert(FAILStrings.SCRIPT_NOT_FOUND, 
    		  		"Script '"+ scriptName +"' not found.", scriptName));
    } catch (com.rational.test.ft.RationalTestException rte) {
        this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.SCRIPT_ERROR, 
		  		"Script '"+ scriptName +"' error: "+ rte.getMessage(), scriptName, rte.getMessage()));
    }
  }

  /** <br><em>Purpose:</em> waitForGui
   **/
  private void waitForGui () {
    if (params.size() < 2) {
    	this.issueParameterCountFailure("WindowID, ComponentID");
        return;
    }
    Iterator iterator = params.iterator();
    // get the window, comp
    String windowName = (String) iterator.next();
    String compName = (String) iterator.next();
    String command = testRecordData.getCommand().toLowerCase();
    int secii = 15;
    String seconds = "15"; // default
    try { // optional param
      seconds = (String)iterator.next();
    } catch (Exception e) {}
    Log.info("............................."+command+": window:"+windowName+", component:"+compName+", seconds:"+seconds);
    try {
      Float seci = new Float(seconds);
      secii = seci.intValue();

      try
      { localClearAppMapCache();}
      catch(Exception x)
      {Log.info("DCDC.waitForGui ignoring "+ x.getClass().getSimpleName());}

      String wc = "COMPONENT: ";
      if (compName.equalsIgnoreCase(windowName)) wc = "WINDOW: ";
      wc = wc+compName;
      // wait for the window/component
      int status = -1;
      boolean isDone = false;
      long starttime = System.currentTimeMillis();
      long endtime = 0;
      double wait_active_seconds = ((Double)Script.getOption(IOptionName.DELAY_AFTER_WINDOW_ACTIVATE)).doubleValue();
      Script script = ((RTestRecordData)testRecordData).getScript();
      script.sleep(wait_active_seconds);
      while(!isDone){
	      try{status =((TestRecordHelper)testRecordData).getDDGUtils().
	        waitForObject(testRecordData.getAppMapName(),
	                      windowName, compName, secii);
	      }catch(SAFSObjectNotFoundException nf){
	    	  Log.info("DCDC.waitForGui handling "+ nf.getClass().getSimpleName());
	    	  status = -1;
	      }catch(Exception x){
	    	  Log.info("DCDC.waitForGui handling "+ x.getClass().getSimpleName());
	    	  status = -1;
	      }
	      endtime = System.currentTimeMillis();
	      if ((endtime - starttime)/1000 > secii) isDone = true;
	      if (status == 0) isDone = true;
      }
      //if it cannot be found within timeout
      if (status != 0) {
          this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.NOT_FOUND_TIMEOUT, 
  		  		wc + " could not be found in timeout "+ seconds,wc, seconds));
          return;
      }
      this.issueGenericSuccess(GENStrings.convert(GENStrings.FOUND_TIMEOUT, 
    		  wc +" was found in timeout "+ seconds,
    		  wc, seconds));
    } catch (NumberFormatException nfe) {
        this.issueParameterValueFailure("TIMEOUT="+seconds);
    }
  }
  /** <br><em>Purpose:</em> waitForGuiGone
   **/
  private void waitForGuiGone () {
    if (params.size() < 2) {
    	this.issueParameterCountFailure("WindowID, ComponentID");
        return;
    }
    Iterator iterator = params.iterator();
    // get the window, comp
    String windowName = (String) iterator.next();
    String compName = (String) iterator.next();
    String command = testRecordData.getCommand().toLowerCase();
    int secii = 15;
    String seconds = "15"; // default
    try { // optional param
      seconds = (String)iterator.next();
    } catch (Exception e) {}
    Log.info("............................."+command+": window:"+windowName+", component:"+compName+", seconds:"+seconds);
    try {
      Float seci = new Float(seconds);
      secii = seci.intValue();

      long msecTimeout = secii * 1000;
      long ms0 = System.currentTimeMillis();
      long ms1 = ms0 + msecTimeout;
      long msn = 0;
      Log.info("..waitForGuiGone before : ms0: "+ms0+",   timeout: "+ms1);
      int j=0;
      Object obj = null;
      boolean warnFlag = true;
      for(; ; j++) { // try several times
        try {
          // wait for the window/component
          obj = null;
          try {
        	  localClearAppMapCache();
        	  obj = localGetTestObject(windowName, compName);}
          catch(Exception x){
        	  Log.info("DCDC.waitForGuiGone ignoring "+ x.getClass().getSimpleName());
          }
          Log.info("localClearAppMapCache.., obj:"+obj);
          if (obj == null) {
            // means the object is gone
            Log.info("The object is now gone: obj == null");
            warnFlag = false;
            break;
          }else if (obj instanceof GuiTestObject){
        	  try{
	        	  if(!RGuiObjectRecognition.isObjectShowing((GuiTestObject)obj)){
	                  Log.info("The object is present, but not showing...");
	                  warnFlag = false;
	                  break;
	        	  }
        	  }catch(Exception unk){
                  Log.debug("The object is present. isShowing IGNORING "+ unk.getClass().getSimpleName()+":"+ unk.getMessage());        		  
        	  }
          }
        } catch (Exception tge) {//likely TargetGoneException
          // means the object is gone
          Log.info("The object is now gone: "+ tge.getClass().getSimpleName());
          warnFlag = false;
          break;
        }
        msn = System.currentTimeMillis();
        if (msn > ms1) break;
        delay(500);
        Log.info("..trying again...waitForGuiGone ..... : msn: "+msn+", j: "+j);
      }
      if (warnFlag) {
    	  this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.NOT_GONE_TIMEOUT, 
    			  compName +" not gone in timeout "+ seconds, compName, seconds));
          return;
      }
      this.issueGenericSuccess(GENStrings.convert(GENStrings.GONE_TIMEOUT, 
                     compName +" was gone in timeout "+ seconds,
                     compName, seconds));
    } catch (NumberFormatException nfe) {
    	this.issueParameterValueFailure("TIMEOUT="+seconds);
    }
  }

  /**
   * Would normally be called ONLY after localClearAppMapCache
   ** return the new TestObject for the windowName and compName
   ** <br> this version is the worker, and does not set status or log a message
   ** @param windowName, String, if null then uses a dummy name
   ** @param compName, String, if null then uses a dummy name
   ** @return, if windowName or compName are null then uses ___any ___comp, 
   *  else returns the new TestObject if found, or null.
   */
  private Object localGetTestObject(String windowName, String compName){
    if (windowName==null) windowName = "___any";
    if (compName==null) compName = "___comp";
    String mapname = testRecordData.getAppMapName();
    DDGUIUtilities utils = ((TestRecordHelper)testRecordData).getDDGUtils();
    Object obj = ((RDDGUIUtilities)utils).getTestObject(mapname, windowName, compName, true);
    return obj;
  }

  /** 
   * clear local and remote caches of the test objects in the appmap class and in RFT.
   **/
  private void localClearAppMapCache() {
    Script script = ((RTestRecordData)testRecordData).getScript();
    if(script != null) 
    	try{
    		script.localUnregisterAll();
    	}
        catch(Exception x){
        	Log.debug("DCDC.localClearAppMapCache ignoring "+x.getClass().getSimpleName()+" "+ x.getMessage());
        }
    DDGUIUtilities utils = ((TestRecordHelper)testRecordData).getDDGUtils();
    utils.clearAllAppMapCaches();
  }

  /** clear the cache of the test objects maintained by the appmap class
   **/
  private void clearAppMapCache() {
    String action = testRecordData.getCommand();
    localClearAppMapCache();

    
    // obj should be null, and is not used.
    // set status to ok
    testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
    log.logMessage(testRecordData.getFac(),
                   genericText.convert(TXT_SUCCESS_1, action +" successful.", action),
                   GENERIC_MESSAGE);
  }

  /** used to keep instances of ProcessTestObject instances, so that later we can kill one **/
  private HashMap ptoMap = new HashMap();

  /** application: launchApplication or closeApplication
   * @param                     launch, boolean, if true launch, else close app
   **/
  private void application (boolean launch) throws SAFSException {
    if (params.size() < (launch?2:1)) {
    	String info = "ApplicationID";
    	if (launch) info +=", ExecutablePath";
    	this.issueParameterCountFailure(info);
        return;
    }
    Iterator iterator = params.iterator();
    String idname = (String) iterator.next();
    String appname = (launch?(String) iterator.next():(String)null);
    String workdir = null;
    String param = null;
    String appmap = null;
    try {
      workdir = (String) iterator.next();
      param = (String) iterator.next();
      if (param != null) appname = appname + " " + param;
      appmap = (String) iterator.next();
      if (appmap != null) {
        Log.generic(" appmap specified: "+appmap+", but not yet known how to use it!");
      }
    } catch (Exception ee) {} // ignore
    Log.generic(".............................appname: "+appname);
    try {
      Script script = ((RTestRecordData)testRecordData).getScript();
      if (launch) {
        ProcessTestObject pto = null;
        if (workdir==null) {
          pto = script.localRun(appname, null);
        } else {
          pto = script.localRun(appname, workdir);
        }
        ptoMap.put(idname, pto);
      } else {
        ProcessTestObject pto = (ProcessTestObject) ptoMap.get(idname);
        if (pto != null) {
          pto.kill();
          ptoMap.remove(idname);
        } else {
        	this.issueActionFailure(FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND,
        			"ApplicationID:"+ idname +" not found.",
        			"ApplicationID:"+ idname));
            return;
        }
      }
      log.logMessage(testRecordData.getFac(), 
                     genericText.convert(TXT_SUCCESS_2, 
                     testRecordData.getCommand()+" "+ idname +" successul.", 
                     testRecordData.getCommand(), idname),
                     GENERIC_MESSAGE);
      testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
    } catch (RunException re) {
    	this.issueActionFailure(re.getMessage()+(workdir!=null?", WORKDIR: "+workdir: ""));
    }
  }

  /** <br><em>Purpose:</em> Wait for a specif Window or Component property value
   ** to match an expected value.
   ** @param gone, boolean, if false, then wait for a value to become equal, else
   ** wait for the value to go away
   **/
  private void waitForPropertyValue (boolean gone) {
    if (params.size() < 4) {
    	this.issueParameterCountFailure("WindowID, ComponentID, PropertyName, ExpectedValue");
        return;
    }
    Iterator iterator = params.iterator();
    // get the window, comp
    String windowName = (String) iterator.next();
    String compName = (String) iterator.next();
    String propertyName = (String) iterator.next();
    String expectedValue = (String) iterator.next();
    String command = testRecordData.getCommand().toLowerCase();
    int secii = 15;
    String seconds = "15"; // default
    String nocase = ""; // CASEINSENSITIVE, CASE-INSENSITIVE, or FALSE
    boolean ignoreCase = false;
    Float seci = new Float(seconds);
    double retry_seconds = ((Double)Script.getOption(IOptionName.WAIT_FOR_EXISTENCE_DELAY_BETWEEN_RETRIES)).doubleValue();
    
    try { // optional params
      String tseconds = (String)iterator.next();
      try{
    	  seci = new Float(tseconds);
    	  seconds = tseconds;
      }
      catch(NumberFormatException nf){/* ignore comments, non-numeric data, and empty fields */}
      nocase =  (String)iterator.next();
      if ((nocase.equalsIgnoreCase(CASEINSENSITIVE_FLAG))||
         (nocase.equalsIgnoreCase(CASE_INSENSITIVE_FLAG))||
         (nocase.equalsIgnoreCase(String.valueOf(false))))
    	  ignoreCase = true;
    } catch (Exception e) {}
    Log.info("............................."+command+": window:"+windowName+", component:"+compName+"propertyname: "+propertyName+", expectedvalue:"+expectedValue+", seconds:"+seconds);
    try {
      localClearAppMapCache();
      TestObject tobj = (TestObject) localGetTestObject(windowName, compName);
      secii = seci.intValue();
      long msecTimeout = secii * 1000;
      long ms0 = System.currentTimeMillis();
      long ms1 = ms0 + msecTimeout;
      long msn = 0;
      Log.info("..waitforprop before : ms0: "+ms0+",   timeout: "+ms1);
      int j=0;
      Object rval = null;
      boolean warnFlag = true;      
      Script script = ((RTestRecordData)testRecordData).getScript();      
      for(; ; j++) { // try several times
    	// We have found that some properties might only exist if the instance of the 
    	// object is the right class.  For example, the object might be able to be a 
    	// GuiTestObject, but since we have it as a TestObject (superclass) some of the 
    	// properties available via GuiTestObject might not show up
        if (tobj != null) {
          try{ rval = tobj.getProperty(propertyName);}
          catch(PropertyNotFoundException pnfe){
        	  try{ rval = RDDGUIUtilities.getGuiTestObject(tobj).getProperty(propertyName);}
        	  catch(NullPointerException npe){;}
        	  // consider other TestObject subclasses here in the future
          }
          if (rval == null) rval="";
          Log.debug("..expectedValue: "+expectedValue+", rval: "+rval.toString()); 
          if (gone) { // wait for it to go away
        	  if(ignoreCase){
  	            if (!expectedValue.equalsIgnoreCase(rval.toString())) {
  	              warnFlag = false;
  	              break;
  	            }
        	  }else{
	            if (!expectedValue.equals(rval.toString())) {
	              warnFlag = false;
	              break;
	            }
        	  }
          } else { // wait for it to become equal
        	  if(ignoreCase){
  	            if (expectedValue.equalsIgnoreCase(rval.toString())) {
  	              warnFlag = false;
  	              break;
  	            }
        	  }else{
	            if (expectedValue.equals(rval.toString())) {
	              warnFlag = false;
	              break;
	            }
        	  }
          }
        }
        msn = System.currentTimeMillis();
        script.sleep(retry_seconds);
        if (msn > ms1) break;
        if(tobj==null) Log.info("..target object '"+ windowName +":"+ compName +"' not yet found...");
        Log.info("..trying again...waitforprop ..... : msn: "+msn+", j: "+j);
        localClearAppMapCache();
        tobj = (TestObject) localGetTestObject(windowName, compName);
      }
      if (rval==null) rval = "";
      if (warnFlag) {
    	  if(!gone)
    		  this.issueErrorPerformingActionOnX(compName, FAILStrings.convert(FAILStrings.SOMETHING_NOT_MATCH, 
        					 propertyName +" value '"+ rval +
        					 "' does not match expected value '"+ expectedValue +"'.", 
        					 propertyName, rval.toString(), expectedValue));
          else
        	  this.issueErrorPerformingActionOnX(compName, GENStrings.convert(GENStrings.EQUALS, 
     		         propertyName +" equals '"+ expectedValue +"'.", 
     		         propertyName, expectedValue));
          return;
      }
      if(!gone)
    	  this.issueGenericSuccess(GENStrings.convert(GENStrings.SOMETHING_MATCHES, 
    		         propertyName +" matches expected value '"+ rval.toString() +"'.", 
    		         propertyName, rval.toString()));
      else
    	  this.issueGenericSuccess(GENStrings.convert(GENStrings.NOT_EQUAL, 
 		         propertyName +" does not equal '"+ expectedValue +"'.", 
 		         propertyName, expectedValue));
    } catch (PropertyNotFoundException pnfe) {
    	this.issueErrorPerformingActionOnX(compName, FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND,
    			"PropertyName:"+ propertyName +" not found.",
    			"PropertyName:"+ propertyName));
    } catch (NumberFormatException nfe) { // should not happen?
    	this.issueParameterValueFailure("TIMEOUT="+seconds);
    }
  }

  /** Wait for a web document to finish loading within a timeout period.
   * Currently, we can only seek the topmost Html.HtmlBrowser object and check its readyState.
   * This may not be the most robust solution.
   **/
  private void waitForWebPage () {  	
    Iterator iterator = params.iterator();
    String windowName = ""; //optional
    String compName = "";   //optional
    String timeout = "30";    //optional
    int default_timeout = 30;
    int itimeout = 30;
    String command = getTestRecordData().getCommand();  //WaitForWebPage
    try{windowName = (String) iterator.next();}
    catch(NoSuchElementException x){
    	Log.debug(command +" ignoring missing optional WindowName.");
    }
    try{compName = (String) iterator.next();}
    catch(NoSuchElementException x){
    	Log.debug(command +" ignoring missing optional CompName.");
    }
    if (compName.length()==0) compName = windowName;
    try{
    	timeout = (String) iterator.next();
        if(timeout.length() > 0) itimeout = Integer.parseInt(timeout);
    }
    catch(NoSuchElementException x){
    	Log.debug(command +" ignoring missing optional TIMEOUT value. Using default timeout instead.");
    }
    catch(NumberFormatException x){
    	this.issueParameterValueFailure("TIMEOUT="+timeout);
    	return;
    }
    Log.info("............................"+command+": window:"+windowName+", component:"+compName +", timeout:"+itimeout);
    TestObject tempobj = null;
    GuiTestObject tobj = null;
    RDDGUIUtilities utils = (RDDGUIUtilities)((TestRecordHelper)testRecordData).getDDGUtils();
    Script script = utils.getScript();
    //give a new refreshing web browser an initial time to "activate"
    Double wait_before = (Double)script.getOption(IOptionName.DELAY_AFTER_WINDOW_ACTIVATE);
    script.sleep(wait_before.doubleValue());
    //we want to temporarily override the RFT object setting.
    Double max_find = (Double)script.getOption(IOptionName.MAXIMUM_FIND_OBJECT_TIME);
    script.setOption(IOptionName.MAXIMUM_FIND_OBJECT_TIME, 1);
    Double wait_between = (Double)script.getOption(IOptionName.FIND_OBJECT_DELAY_BETWEEN_RETRIES);
    long currenttimemillis = System.currentTimeMillis();
    long maxtimemillis = currenttimemillis + (itimeout *1000);
    int icount = 0;
    boolean ready = false;
    while ((currenttimemillis < maxtimemillis)&&(!ready)){
    	tobj = null;
	    if (windowName.length()> 0){
	        try{
	        	tempobj = utils.getTestObject(testRecordData.getAppMapName(),windowName, compName, true);
	        	tobj = new GuiTestObject(tempobj);
	        }
	        catch(Exception x){
	        	Log.debug(command +" IGNORING getTestObject Exception at iteration "+ icount +": "+ x.getClass().getSimpleName());
	        }
	    }else{
	    	try{
	    		RootTestObject root = RootTestObject.getRootTestObject();
	    		TestObject[] tos = root.find(script.atChild(".domain", "Html", ".class", "Html.HtmlBrowser"));
	    		if ((tos != null)&&(tos.length > 0)) {
	    			tobj = new GuiTestObject(tos[0]);	    		
	    		}
	    	}
	    	// oldest versions of RFT MUST have WindowName because we cannot "find" with a RootTestObject
	    	catch(Throwable x){
	    		this.issueParameterValueFailure("WindowName="+windowName);
	    		Log.debug(command +" RootTestObject error: "+ x.getClass().getSimpleName());
	    		return;
	    	}
	    }	    
	    if(tobj == null) {
	    	script.sleep(wait_between.doubleValue());
	    	currenttimemillis = System.currentTimeMillis();
	    	continue;
	    }
	    
	    // ensure Html.HtmlBrowser object
	    String classname = tobj.getObjectClassName();
	    try{
	    	if(! classname.equalsIgnoreCase("HTML.HTMLBROWSER")){
	    		tobj = RDDGUIUtilities.getGuiTestObject(tobj.getTopParent());
	    		//tobj = (GuiTestObject)tobj.getTopMappableParent();
	    	}
	    }catch(Exception x){ /* ignore NullPointer and other exceptions for now */ }
    	// tobj can be null here!
	    Object state = null;
	    // valid properties to check: .readyState, readyState for values Integer(4) or "complete"
	    try{ state = tobj.getProperty(".readyState");} 
	    catch(PropertyNotFoundException x){;}
	    catch(NullPointerException x){ 
	    	script.sleep(wait_between.doubleValue());
	    	currenttimemillis = System.currentTimeMillis();
	    	continue; /* tobj == null */ 
	    }
	    if(state==null)
		    try{ state = tobj.getProperty("readyState");} 
	    	catch(PropertyNotFoundException x){;}
	    Log.info(command +" readyState="+ state);
	    if (state == null) {
	    	script.sleep(wait_between.doubleValue());
	    	currenttimemillis = System.currentTimeMillis();
	    	continue;
	    }
	    String sstate = state.toString();
	    ready = (sstate.equals("4"))||(sstate.equalsIgnoreCase("COMPLETE"));
    	script.sleep(wait_between.doubleValue());
	    currenttimemillis=System.currentTimeMillis();
    }
    script.setOption(IOptionName.MAXIMUM_FIND_OBJECT_TIME, max_find.doubleValue());

    if(windowName.length()==0) windowName = "Browser";    
    //if timeout reached and !ready
    if(!ready){
    	this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.NOT_FOUND_TIMEOUT, 
				windowName +" was not found within timeout "+ timeout,
	            windowName, timeout));
    	return;
    }
    //success
    this.issueGenericSuccess(GENStrings.convert(GENStrings.FOUND_TIMEOUT, 
    		windowName +" was found in timeout "+ timeout,
    		windowName, timeout));
  }

  /** <br><em>Purpose:</em> Give a window or Window Component input focus.
   * 
   * 12.09.2005 - bolawl  Added conditional logic for new SetContext vs. SetFocus.  
   *                      SetContext was added for migration purposes from Robot 
   *                      Classic. It only requires that we SetActiveWindow.  RJL
   **/
  private void setFocus () throws SAFSException {
    if (params.size() < 2) {
    	this.issueParameterCountFailure("WindowID, ComponentID");
        return;
    }
    Iterator iterator = params.iterator();
    // get the window, comp
    String windowName = (String) iterator.next();
    String compName = (String) iterator.next();
    String command = testRecordData.getCommand().toLowerCase();
    Log.info("............................"+command+": window:"+windowName+", component:"+compName);
    TestObject tempobj = ((RDDGUIUtilities)((TestRecordHelper)testRecordData).getDDGUtils()).getTestObject(testRecordData.getAppMapName(),windowName, compName);
    GuiTestObject tobj = RDDGUIUtilities.getGuiTestObject(tempobj);
    if (tobj == null) {
    	this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND,
    			windowName +" was not found.", windowName));
        return;
    }
    //wait for the component
    int status = ((TestRecordHelper)testRecordData).getDDGUtils().
      waitForObject(testRecordData.getAppMapName(),
                    windowName, compName, 1);
    //if component cannot be found
    if (status != 0) {
    	this.issueErrorPerformingAction(FAILStrings.convert(FAILStrings.SOMETHING_NOT_FOUND,
    			compName +" was not found.", compName));
        return;
    }
    //set the window as the active window
    ((TestRecordHelper)testRecordData).getDDGUtils().
      setActiveWindow(testRecordData.getAppMapName(), windowName, windowName);
    
    //if necessary, set the focus
    // TODO: Need to find some way other than Click
    if (testRecordData.getCommand().equalsIgnoreCase(SETFOCUS)) {
    	tobj.click();
    }
    
    //no need to output both windowName and compName if they are the same
    if (windowName.equalsIgnoreCase(compName))
    	log.logMessage(testRecordData.getFac(), 
                       genericText.convert(TXT_SUCCESS_2, 
		               windowName +" "+ getTestRecordData().getCommand()+" successful.",
                       windowName, getTestRecordData().getCommand()),
                       GENERIC_MESSAGE);
    else
    	log.logMessage(testRecordData.getFac(), 
                       genericText.convert(TXT_SUCCESS_3, 
   		               windowName +":"+ compName +" "+getTestRecordData().getCommand()+" successful.",
                       windowName, compName, getTestRecordData().getCommand()),
                       GENERIC_MESSAGE);
    testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
  }

  private void captureMousePositionOnScreen () throws SAFSException {
      if (params.size() < 2) {
    	  this.issueParameterCountFailure("X_VariableName, Y_VariableName");
          return;
      }      
      Iterator iterator = params.iterator();
      String xVar = (String) iterator.next();
      String yVar = (String) iterator.next();
      String command = testRecordData.getCommand().toLowerCase();
      Log.info("............................"+command+": xVar:"+xVar+", yVar:"+yVar);

      //Use Rational libraries to return the current position of the mouse on the screen.
      //Note: Sun's Java 1.5 contains a MouseInfo class which could be used in the same way by
      //a general driver, like the TID, when SAFS eventually moves to a newer version of Java.
      //Java 1.5 usage: Point mousePt = new Point(MouseInfo.getPointerInfo().getLocation());
      Point mousePt = null;
      try{
    	  mousePt = new Point(RationalTestScript.getScreen().getMousePosition());
      }catch(NullPointerException np){
    	  Log.debug(command +" handling NullPointerException while receiving coordinates from RFT: "+ np.getMessage());
    	  try{ mousePt = new Point(MouseInfo.getPointerInfo().getLocation());}
    	  catch(Exception x){
        	  Log.debug(command +" handling "+ x.getClass().getSimpleName() +" while receiving coordinates from Java: "+ x.getMessage());
    		  this.issueActionFailure(x.getClass().getSimpleName()+":"+ x.getMessage());
    		  return;
    	  }
      }

      //turns the double values returned by getX() and getY() into strings and
      //then attempts to save the string to the variable names given as parameters
      if (!setVariable(xVar, String.valueOf(mousePt.getX())) || 
          !setVariable(yVar, String.valueOf(mousePt.getY()))) {
    	  this.issueErrorPerformingAction(FAILStrings.text(FAILStrings.COULD_NOT_SET_VARS,
    			  "Could not set one or more variable values."));
      }
      else {
    	  this.issueGenericSuccess(GENStrings.convert(GENStrings.MOUSE_POSITION_SAVED, 
    			  "Mouse position "+ mousePt.getX()+","+ mousePt.getY()+" saved.", 
    			  String.valueOf(mousePt.getX()), String.valueOf(mousePt.getY())));
      }
      return;
    }
  
  /** <br><em>Purpose:</em> OnGUI(Not)Exist(s)GotoBlockID
   * e.g. C, OnGUIExistsGotoBlockID, BlockID, Window, Component[, Timeout]
   * 
   * @author bolawl	11.15.2005	Created (RJL) 
   * 
   * This method first determines if branching should occur based on whether or not the GUI is found. Then,
   * it sets the TestRecordData status to BRANCH_TO_BLOCKID so that the driver knows to attempt a branch when
   * control returns to the driver.  This method ustilizes the TestRecordData field statusinfo to store the 
   * name of the blockID.
   **/
  private void onGUIGotoCommands (boolean exists) {
  	String methodName = "onGUIGotoCommands";
  	String command = testRecordData.getCommand();
  	//String table = testRecordData.getFilename();
  	//String recordnum = String.valueOf(testRecordData.getLineNumber());
  	
  	//validate number of params
    if (params.size() < 3) {
    	this.issueParameterCountFailure("BlockID, WindowID, ComponentID");
        return;
    }

    // get params
    Iterator iterator = params.iterator();
    String blockName  = (String) iterator.next();
    String windowName = (String) iterator.next();
    String compName   = (String) iterator.next();
    int timeout = 15;	// default timeout
    String seconds = String.valueOf(timeout);
    int secii;
    try { // optional timeout param
      seconds = (String)iterator.next();
    }
    catch (NoSuchElementException nse) {
		//exception is okay, timeout is optional parameter
    	Log.info (methodName +":"+ nse);
		Log.info (methodName +": Optional timeout value not specified;" +
				  " using default " + seconds + " seconds instead.");
      }
    // create timeout value
    try {
    	Float seci = new Float(seconds);
        secii = seci.intValue();
    }catch (Exception nfe) {
    	//exception is okay, use default timeout value instead
    	secii = timeout;
    	Log.info (methodName +":"+ nfe);
    	Log.info (methodName +": Optional timeout value likely not a number;" +
				  " using default " + seconds + " seconds instead.");
    }
    Log.info(".."+command+": window:"+windowName+", component:"+compName+", seconds:"+seconds);
    
    long msecTimeout = secii * 1000;
    long ms0 = System.currentTimeMillis();
    long ms1 = ms0 + msecTimeout;
    Log.info("..begin search : ms0: "+ms0+",   timeout: "+ms1);
    try {
      //clear appmapcache to start fresh search
      TestObject tempobj = null;
      GuiTestObject tobj = null;
      try{
    	localClearAppMapCache();
    	tempobj = (TestObject) localGetTestObject(windowName, compName);
      }catch(Exception x){
    	Log.info("DCDC."+ command +" ignoring "+ x.getClass().getSimpleName());  
      }
      if(tempobj != null) {
    	try{ tobj = RDDGUIUtilities.getGuiTestObject(tempobj);}
    	catch(Exception x){
           Log.info("DCDC."+ command +" ignoring "+ x.getClass().getSimpleName());  
    	}
      }
      
      //search within timeout value
      long msn = 0;
      double retry_seconds = ((Double)Script.getOption(IOptionName.WAIT_FOR_EXISTENCE_DELAY_BETWEEN_RETRIES)).doubleValue();
      int j=0;
      Object rval = null;
      boolean match = false;
      Script script = ((RTestRecordData)testRecordData).getScript();      
      for(; !match ; j++) { // try several times
        if (tobj != null) {
          //if match, break loop, success!
          //PROBLEM: RFT isShowing is NOT reliable! Gives wrong answer sometimes!
          if (tobj.isShowing() == exists) {
              match= true;
              break;
          }
        }// if it wasn't found AND we are waiting for it to go away
        else if (!exists){
        	match = true;
        	break;
        }
        msn = System.currentTimeMillis();
        if (msn > ms1) break;
        script.sleep(retry_seconds);
        Log.info("..trying again... "+ command +" "+ windowName +":"+ compName +" ... : msn: "+msn+", j: "+j);
        //clear appmapcache again to start fresh search
        tempobj = null;
        try{
        	localClearAppMapCache();
        	tempobj = (TestObject) localGetTestObject(windowName, compName); }
        catch(Exception x){
      	  Log.info("DCDC."+ command +" ignoring "+ x.getClass().getSimpleName());  
        }
        if (tempobj != null) {
        	try{ tobj = RDDGUIUtilities.getGuiTestObject(tempobj);}
        	catch(Exception x){
          	  Log.info("DCDC."+ command +" ignoring "+ x.getClass().getSimpleName());  
        	}
        }
        else{
        	tobj = null;
        }
      }
      
      String msg;
      //onguiexists...
      if (exists) {
      	if (match) {
      		//we were searching for gui, since it was found, attempt branch
      		msg = GENStrings.convert(GENStrings.BRANCHING, 
      				command +" attempting branch to "+ blockName +".", 
      				command, blockName);
      		msg += "  "+ GENStrings.convert(GENStrings.FOUND_TIMEOUT, 
      				compName +" found within timeout "+ String.valueOf(secii), 
      				compName, String.valueOf(secii));
      		//set statuscode and statusinfo fields so driver will know to branch
      		testRecordData.setStatusCode(StatusCodes.BRANCH_TO_BLOCKID);
      		testRecordData.setStatusInfo(blockName);
      	}
      	else {
      		//we were searching for gui, since it wasn't found, don't branch
      		msg = GENStrings.convert(GENStrings.NOT_BRANCHING, 
      				command +" not branching to "+ blockName +".", 
      				command, blockName);
      		msg += "  "+ FAILStrings.convert(FAILStrings.NOT_FOUND_TIMEOUT, 
      				compName +" not found within timeout "+ String.valueOf(secii), 
      				compName, String.valueOf(secii));
      		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
      	}
      }
      //onguinotexist...
      else {
      	if (match) {
      		//we were searching for no gui, since it wasn't found, branch
      		msg = GENStrings.convert(GENStrings.BRANCHING, 
      				command +" attempting branch to "+ blockName +".", 
      				command, blockName);
      		msg += "  "+ GENStrings.convert(GENStrings.NOT_EXIST, 
      				compName +" does not exist", 
      				compName);
      		//set statuscode and statusinfo fields so driver will know to branch
      		testRecordData.setStatusCode(StatusCodes.BRANCH_TO_BLOCKID);
      		testRecordData.setStatusInfo(blockName);	
      	}
      	else {
      		//we were searching for no gui, since it was found, don't branch
      		msg = GENStrings.convert(GENStrings.NOT_BRANCHING, 
      				command +" not branching to "+ blockName +".", 
      				command, blockName);
      		msg += "  "+ GENStrings.convert(GENStrings.EXISTS, 
      				compName +" exists", 
      				compName);
      		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
      	}
      }
    log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE);  
    }
    catch (Exception e) {
    	Log.error(methodName +": Exception", e);
    	this.issueErrorPerformingAction(e.getMessage());
    }
  }
    /**
     * Used to enable or disable a particular test domain.
     * At the time of this writing valid domains were:
     * 
     *    Java
     *    Html
     *    Net
     *    Win   
     *    SWT
     *    Flex
     * @author CANAGL, JAN 30, 2006 Original Release
     * @author CANAGL, MAY 20, 2009 Show SWT and Flex domains
     */
    private void enableDomain (boolean enabled){
	    if (params.size() < 1) {
	    	this.issueParameterCountFailure("Domain");
	        return;
	    }
	    Iterator iterator = params.iterator();
    	String domainname = (String) iterator.next();
    	String status = enabled ? "engine_domain_enabled":"engine_domain_disabled";
    	try{ 
    		if (enabled) { Domains.enableDomain(domainname);}
    		else { Domains.disableDomain( domainname);}
	        issueGenericSuccess( genericText.convert(status, 
		                                   "SAFS/RobotJ "+ domainname +" "+ status,
		                                   "SAFS/RobotJ", domainname));
	        return;
    	}
    	catch(IllegalArgumentException ix){
    		this.issueParameterValueFailure("DOMAIN="+domainname);
    	}
    }
    
    /**
     * perform OnMenuItemContainsStateGotoBlockID going to the defined block if menuitem's state is as same as discribed
     * Example: C, OnMenuItemContainsStateGotoBlockID, "NoError", MainWindow, MainWindow, "View->Error", "Disabled" 
     * @exception SAFSException
     */
    private void OnMenuItemContainsStateGotoCommand() throws SAFSException {
     	String debugInf = getClass().getName()+ ".OnMenuItemContainsStateGotoCommand() ";
        //validate number of params
        if (params.size() < 5) {
        	this.issueParameterCountFailure("BlockID, WindowName, ComponentName, MenuItem, State");
            return;
        }
        String command = testRecordData.getCommand();
        // get params
        Iterator iterator = params.iterator();
        String blockName  = (String) iterator.next();
        String windowName = (String) iterator.next();
        String compName   = (String) iterator.next();
        String path   	  = (String) iterator.next();
        String status	  = (String) iterator.next();  
        
        Log.info("............................."+command+": ");
        Log.info(".............................param blockName: "+blockName);
        Log.info(".............................param windowName: "+windowName);
        Log.info(".............................param compName: "+compName);
        Log.info(".............................param path: "+path);
        Log.info(".............................param status: "+status);
     
        localClearAppMapCache();
        TestObject tempobj = (TestObject) localGetTestObject(windowName, compName);
        GuiTestObject guiObj = RDDGUIUtilities.getGuiTestObject(tempobj);
        TestObject[] children = null;
        boolean isDotnetDomain = CFComponent.isDotnetDomain(guiObj);
        boolean isJavaDomain = CFComponent.isJavaDomain(guiObj);
        boolean isWinDomain = CFComponent.isWinDomain(guiObj);
        
        //For instance, only java swing and .NET applications are supported.
        if(!(isDotnetDomain || isJavaDomain || isWinDomain) ){
        	this.issueActionFailure(FAILStrings.convert(FAILStrings.SUPPORT_NOT_FOUND, 
        			"Support for '"+ guiObj.getDomain().getImplementationName().toString()+"' not found.", 
        			guiObj.getDomain().getImplementationName().toString()));
            return;
        }
        
		// find MenuBar components, only valid for JavaSwing so far.
        if(isDotnetDomain){
        	//TODO if the .net menubar inherits from the two standard menubar class, our code here will not work
        	//need to find a better way.
        	children = guiObj.find(Script.atDescendant(".class",CFDotNetMenuBar.CLASS_MENUSTRIP_NAME));
        	if(children==null){
        		children = guiObj.find(Script.atDescendant(".class",CFDotNetMenuBar.CLASS_MAINMENU_NAME));
        	}
        }else if(isJavaDomain){
        	//For swing appliation, the subclass of JMenuBar will have the same value for property "uIClassID"
        	children = guiObj.find(Script.atDescendant(CFMenuBar.UITYPE_PROPERTY,CFMenuBar.UITYPE_MENUBAR));
        }else if(isWinDomain){
        	children = guiObj.find(Script.atDescendant(".class",CFWinMenuBar.CLASS_MENUBAR_NAME));
        }
        String msg;
		if(children!=null) {
		    // visit every menubar captured until finding the matched menu item; 
		    for (int count = 0; count<children.length; count++) {
		        GuiSubitemTestObject menuBarGuiObj = new GuiSubitemTestObject(children[count].getObjectReference());
		        try {
		            //see if current menubar contanis the path of menu item. 
		            MenuTree atree = null;
		            
		            if(isDotnetDomain){
		            	atree = (MenuTree)CFDotNetMenuBar.staticExtractMenuItems(menuBarGuiObj, 0);
		            }else if(isJavaDomain){
		            	atree = (MenuTree)CFMenuBar.staticExtractMenuItems(menuBarGuiObj, 0);
		            }else if(isWinDomain){
		            	atree = (MenuTree)CFWinMenuBar.staticExtractMenuItems(menuBarGuiObj, 0);
		            }
		            Log.info(testRecordData.getFac()+":"+command+" atree: "+atree);
		            //	do the work of matching..., verify the path
		            String matchedPath = atree.matchPath(path, false, status);          	    
          	    
		            Log.info("...item status: " +status);
		            Log.info("...matched path: "+matchedPath);   
		            if (matchedPath == null) // no match on path with strStatus
		                continue;
		            else {
		          		msg = GENStrings.convert(GENStrings.BRANCHING, 
		          				command +" attempting branch to "+ blockName +".", 
		          				command, blockName);
		          		msg += "  "+ GENStrings.convert(GENStrings.EQUALS, 
		   	                 "'"+ path +"' equals '"+ status +"'", 
		   	                 path, status);
		                log.logMessage(testRecordData.getFac(),msg,GENERIC_MESSAGE);
		          		testRecordData.setStatusCode(StatusCodes.BRANCH_TO_BLOCKID);
		          		testRecordData.setStatusInfo(blockName);		                
		                return;
		            }
		        } catch (SAFSException se) {
		            msg = debugInf+se.getMessage();
		            Log.debug(msg);
		            throw new SAFSException(msg); //stop the loop
		        }   
		    }
    
		}
        testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
  		msg = GENStrings.convert(GENStrings.NOT_BRANCHING, 
  				command +" did not branch to "+ blockName +".", 
  				command, blockName);
  		msg += "  "+ GENStrings.convert(GENStrings.NOT_EQUAL, 
	                 "'"+ path +"' does not equal '"+ status +"'", 
	                 path, status);
        log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE);   
    }

     	  
          
}

