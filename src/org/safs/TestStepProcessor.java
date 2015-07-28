/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

import java.util.ArrayList;
import java.util.Collection;

import org.safs.model.commands.GenericMasterFunctions;
import org.safs.text.FAILStrings;
import org.safs.text.GENStrings;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.engines.TIDComponent;

/**
 * <br><em>Purpose:</em> TestStepProcessor
 * <br><em>Lifetime:</em> instantiated by ProcessRequest
 * <p>
 * @author  Doug Bauman
 * @since   JUN 04, 2003
 *
 *   <br>   AUG 06, 2003    (DBauman)Original Release
 *   <br>   SEP 10, 2003    (CANAGL) Removed WARNING messages for cases "not implemented"
 *   <br>   SEP 16, 2003    (CANAGL) Implemented use of new SAFSLOGS logging.
 *   <br>   NOV 04, 2003    (CANAGL) Modified .process engine failures from GENERAL_FAILURES to 
 *                                   SCRIPT_NOT_EXECUTED so that other engines (like Classic)
 *                                   can attempt to execute the record.
 *   <br>   NOV 05, 2003    (CANAGL) .process will now catch and report RuntimeExceptions.
 *   <br>   NOV 10, 2003    (CANAGL) Added isSupportedRecordType() implementation.
 *   <br>   NOV 14, 2003    (DBauman/CANAGL) Added support for breakpoints.
 *   <br>   NOV 19, 2003    (CANAGL) Additional refactoring.
 *   <br>   FEB 04, 2004	(DBauman)Copy the status code to variable 'customStatusCode'.
 *   <br>	FEB 12, 2004	(BNat)	 Added the Throwable catch block.  This throwable catch block
 * 								     takes care of anything higher than Exceptions i.e. Error and Throwable.
 *   <br>   NOV 11, 2004    (CANAGL) Removed special handling of ClickLinkBeginning\Containing
 *   <br>   MAR 21, 2005    (CANAGL) Allow instantiation of Default Comp Function class when compType=null
 *   <br>   MAR 03, 2009    (CANAGL) Issue Failure instead of "Unknown Command" when Comp is not found in waitForObject.
 *   <br>   APR 08, 2010    (LeiWang)Modify method process(): 1. Treat the mixed mode RS.
 *                                                            2. Move out some logics to methods.
 *                                   Add methods waitForObject(), statusIsOK() and setActiveWindow(): contain some logics
 *                                   moved from method process().
 *   <br>   SEP 26, 2014    (LeiWang)Rename method statusIsOK() to waitForObjectAndCheck():
 *                                   if object cannot be found and there are more engines to try, the give them a chance.
 * 
 **/
public class TestStepProcessor extends Processor {


  /** "CF" 
   * Default prefix for short classname for test step processors.
   */
  public static final String DEFAULT_TEST_STEP_PREFIX = "CF";

  /** DEFAULT_TEST_STEP_PREFIX +"Component" 
   * Short classname appended to alternative/custom package names.
   * This is for dynamic instancing of Test Step processors.
   */
  public static final String DEFAULT_TEST_STEP_CLASSNAME = DEFAULT_TEST_STEP_PREFIX +"Component";
  
  /** 
   * All instances may enable/disable and force breakpoints where they 
   * deem this appropriate.  The static setting is shared by all our instances.
   * By default, our breakpoints are disabled.  However, a check is 
   * done for the global Processor.isBreakpoints enabled which overrides this setting.
   * our breakpoints can be enabled separately when all other global breakpoints are 
   * disabled.
   **/
  protected static boolean cfBreakpointsOn = false;
  
  /** This may be set by any means.  In a runtime debugging environment this 
   * will likely be set by a Driver Command.  
   **/
  public static void setCFBreakpointsOn(boolean enabled){ cfBreakpointsOn = enabled;}

  /** test if CF-specific breakpoints are enabled. 
   * @return true if cfBreakpointsOn is true.
   **/
  public static boolean isCFBreakpointsOn() {return cfBreakpointsOn;}
  
  /** test for enabled CF-specific breakpoints in addition to the standard 
   * Processor breakpoints. Overrides Process.checkMyBreakpoints.
   **/
  protected void checkMyBreakpoints(String breakpoint_message){
    if (isMyBreakpointsOn() || isCFBreakpointsOn())
      activateBreakpoint(breakpoint_message);
  }
  
  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public TestStepProcessor () {
    super();
  }

  /** Supports standard TEST STEP record types (T, TW, TF) **/
  public boolean isSupportedRecordType(String recordType){
  	return isComponentFunctionRecord(recordType);
  }


  /** Overrides Processor.getProcClassNames
   * Calls the super method to first see if the user has already specified a full path 
   * name for the Test Step Processor.  If not, this routine then assumes the 
   * stored procInstancePath identifies a package name in which to locate the 
   * "CFcompType" class.
   * <p>
   * The routine returns a list of:<br>
   *     super.getProcClassNames
   *     procInstancePath.CFcompType
   * <p>
   * Use validProcessorClassName before adding class names to the list.
   * <p>
   * @return a list of potential processor classnames to try.
   */
  public  ArrayList getProcClassNames() {  	
  	ArrayList classlist = super.getProcClassNames();
	String dot = "."; 
  	String rootname = getProcInstancePath();           
    if ((! rootname.endsWith(dot))&&(rootname.length() >0)) 
        rootname = rootname.concat(dot);
  	try{
  		String comptype  = testRecordData.getCompType();
  	    // don't append dot if no package (java "default" package) specified
        String classname = rootname + DEFAULT_TEST_STEP_PREFIX + comptype;
        Log.info("TSR trying ProcInstancePath: "+ classname);
        if(validProcessorClassName(classname)) classlist.add(classname);
  	}catch(Exception ex){}
    return classlist;
  }
  
  /** Overrides Processor.getCustomProcClassName
   * Calls the super method to first see if the user has already specified a full path 
   * name for any custom Driver Command Processor.  If not, this routine then assumes the 
   * stored customProcInstancePath identifies a package name in which to locate the 
   * custom "CFComponent" class.
   * <p>
   * The routine returns a list of:<br>
   *     super.getCustomProcClassNames
   *     customProcInstancePath.CFComponent
   *     customProcInstancePath.custom.CFComponent
   * <p>
   * Use validProcessorClassName before adding class names to the list.
   * <p>
   * @return a valid classname for the Processor or null.
   */
  public  ArrayList getCustomProcClassNames() {  
  	ArrayList classlist = super.getCustomProcClassNames();
	String dot = "."; 
  	String rootname = getCustomProcInstancePath();           
        if ((! rootname.endsWith(dot))&&(rootname.length() >0)) rootname = rootname.concat(dot);
  	try{
  		String comptype  = testRecordData.getCompType();
  	    // don't append dot if no package (java "default" package) specified
        String classname = rootname + DEFAULT_TEST_STEP_PREFIX + comptype;
        Log.info("TSR trying customProcInstancePath: "+ classname);
        if(validProcessorClassName(classname)) classlist.add(classname);
  	}catch(Exception ex){}
    return classlist;
  }
  

  /** 
   * Assumes the stored procInstancePath identifies a package name in which to locate the 
   * "CFComponent" class.  This is normally only called if processing for specific 
   * component types has already failed.
   * <p>
   * The routine returns a String of:<br>
   *     procInstancePath.CFComponent
   * <p>
   * @return a valid classname for the Processor or null.
   */
  protected    String getComponentProcClassName() {  	
  	String classname = null;
	String dot = "."; 
  	String rootname  = getProcInstancePath();           
    if ((! rootname.endsWith(dot))&&(rootname.length() >0)) 
        rootname = rootname.concat(dot);
    Log.info("TSR trying componentProcInstancePath: "+ rootname);
    classname = rootname +DEFAULT_TEST_STEP_CLASSNAME;
    if(validProcessorClassName(classname)) return classname;
    return null;
  }
  
  /**
   * Called internally or overridden by subclasses to find the windowObject and compObjects.
   * <p>
   * This routine is called only after all MixedUse recognition strings have separately been handled. 
   * This routine is called only after all non-GUI commands have separately been handled.
   * This routine is only called if it has been determined we have a "normal" GUI command in which 
   * OBT window and component objects need to be found.
   * <p> 
   * Found windowObjects will also have setActiveWindow invoked. 
   * @return
 * @throws SAFSException 
   */
  protected boolean getWinAndCompGUIObjects() throws SAFSException{
      Log.debug("TestStepProcessor.getWindAndCompGUIObjects calling waitForObject and verifying status...");
      // only do this for GUI type commands (also, waitForGui does it's gui stuff itself).
      //wait for the window. if window cannot be found within timeout then return
      if (!waitForObjectAndCheck(true)) return false;

      //set the window as the active window
      setActiveWindow();

      //wait for the component. if component cannot be found within timeout then return
      if (!waitForObjectAndCheck(false)) return false;
	  return true;
  }
  
  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <p>
   **      At this point the Driver has determined we are dealing with a Test Record.
   **      A Test Record is one acting on a window or a component within a window.
   ** <p><code>
   **      Field #1:   The TEST record type (T).
   ** </code><p>
   **      Subsequent fields would be as follows (with a separator between each field):
   ** <code>
   ** <br> Field:  #2            #3          #4          #5 - N
   ** <br> ==============  ==============  ========  ===============
   ** <br> WINDOWNAME,  COMPONENTNAME,   ACTION,  [PARAMETER(S),]
   ** </code>
   ** <p>
   **      <em>WINDOWNAME</em> is the name given the window in the appmap that you intend to
   **      have focus for this test step.
   ** <p>
   **      <em>COMPONENTNAME</em> is the name of the component within that window you intend
   **      to perform some function or test on.  If it is the window itself then
   **      the COMPONENTNAME should be the same as the WINDOWNAME.
   ** <p>
   **      <em>ACTION</em> is the command or test you wish to perform.  Different types of 
   **      components support different types of actions.  Almost all support some
   **      versions of VERIFY actions.  Pushbuttons can be CLICKed etc... Consult 
   **      each Component's TYPE or CLASS documentation for the actions available for
   **      the component.
   ** <p>
   **      <em>PARAMETER(s)</em> are the additional fields needed based upon the action to 
   **      be completed.  Each action can have its own unique set of parameters.
   **      Some actions may take no parameters at all.  Consult the component's
   **      TYPE or CLASS documentation for the parameters needed for a given action.
   ** <p>
   **      Although the separator used in the example above is a comma, any separator 
   **      can be used as long as it is specified at the time the file is provided or 
   **      in subsequent command lines which might change the separator in use.
   **      (Currently, changes are limited to a per file bases, but per line changes 
   **      will be easy to implement when the need arises.)
   ** <p>
   **      The test record is processed out to functions according to the "Type" of the
   **      component in the record.  Thus, components of Type=Window are sent to 
   **      WindowFunctions and components of Type=Pushbutton are sent to PushbuttonFunctions.
   **      Currently, some Types of Generic, Other, and other oddities are processed out 
   **      to Window or GenericObjectFunctions.  This will allow some property verifications 
   **      and maybe some image testcases but probably not too much else. 
   ** <p>
   **   ??   Some special handling occurs for certain Java components.
   **      In the event the compType, compModule, and compClass are ALL "Unknown"; we 
   **      perform a check to see if "Java" appears anywhere in the recognition method 
   **      provided by the user for the component.  If it does, then we attempt to 
   **      extract the component type out of the recognition method using the last 
   **      "Type=" part of the string.  If this is successful, we will then set 
   **      the compType to that component type and set the compModule to "Java".
   **      This enables us to try and process Java components to the correct 
   **      ComponentFunction even though Robot does not seem to properly recognize
   **      the type of component it is dealing with.
   ** <p>
   **   ??   Popup menus are not of a real Component Type.  The action commands 
   **      for Popup menus are intercepted here and routed to the PopupMenuFunctions
   **      without doing the normal processing and verification of window and child
   **      objects.  This is required due to the special nature of Popup menus and
   **      how they are handled by the operating system.
   ** <p>
   **   ??   The same holds true for DatabaseFunctions.  They are intercepted here and
   **      routed accordingly. (BETA)
   ** <p>  
   **
   **      <em>NOTE:</em>
   **      A user or developer would not normally call this routine.  This
   **      routine is intended to be called from the StepDriver routine as 
   **      deemed necessary by the input records of the data table provided to
   **      the StepDriver routine.  The internals of this routine and the declaration 
   **      and parameters are all subject to change as necessary.
   **
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing
   * <br><em>State Read:</em>   {@link #testRecordData}
   * <br><em>Assumptions:</em>  Various exceptions are caught here, and if so, the status 
   * code is set to StatusCodes.SCRIPT_NOT_EXECUTED and a WARNING message is logged.
   * <br>
   * Added by dbauman Feb, 2004  so that a variable remains for the next test with
   *  the status code. Copies the status code to variable 'customStatusCode'
   **/
  public void process() {
	String debugmsg = getClass().getName()+".process(): ";
	Log.info(debugmsg+" entry.");
  
    try {
      // first interpret the fields of the test record and put them into the
      // appropriate fields of testRecordData
      Collection params = interpretFields();

      //If the window RS is in OBT format and component RS in IBT format,
      //then we treat it specially in CFTIDComponent
      boolean isMixed = false;
      try{ isMixed = testRecordData.isMixedRsUsed();}
      catch(SAFSException ignore){ /* ignore missing recognition string information.  
    	 						      scenario should be caught below.*/
      }
      if(isMixed){
    	  Log.info(debugmsg+" Test record contains mixed Recognition String, CFTIDComponent will process it.");
    	  //1. We need to activate the top window of the application to be tested
          //Wait for the window. if window cannot be found within timeout then return.
          if (!waitForObjectAndCheck(true)) return;

          //Set the top window as the active window
          setActiveWindow();
          
    	  //2. We need to instantiate a special class to do the work.
          //   This class will wrap the class org.safs.tools.engines.TIDComponent to whom
          //   it will transfer the test record.
          testRecordData.setCompType("TIDComponent");
      }else{
    	  //If both the window RS and component are in OBT format, we handle here.
	      String currentwinrec = "";
	      try{
	    	  currentwinrec = testRecordData.getDDGUtils().staf.getAppMapItem(
	    			  testRecordData.getAppMapName(), 
	    			  testRecordData.getWindowName(), 
	    			  testRecordData.getCompName());
	      }catch(Exception all){
	    	  Log.debug("TestStepProcessor.process could not retrieve compGUIID due to "+ all.getClass().getSimpleName());
	      }
	      if (currentwinrec == null) currentwinrec = "";

	      String action = testRecordData.getCommand();
	      if (( GenericMasterFunctions.INPUTKEYS_KEYWORD.equalsIgnoreCase(action)||
	    		GenericMasterFunctions.INPUTCHARACTERS_KEYWORD.equalsIgnoreCase(action)) && 
	    		  currentwinrec.equalsIgnoreCase("CurrentWindow")){
	    	  Log.debug("TestStepProcessor.process using Window as CurrentWindow, Component compType.");
	          testRecordData.setCompType("Component");
	          testRecordData.setCompGuiId(currentwinrec);
	      } else if( TIDComponent.noWaitGUIExistence(action)) {
    	    Log.debug("TestStepProcessor.process command '"+ action +"' as Component compType.");
	        testRecordData.setCompType("Component");
	      } else if (DatabaseCommandsHelper.equalsDatabaseCommand(action)) {
    	    Log.debug("TestStepProcessor.process command '"+ action +"' as Database compType.");
	        testRecordData.setCompType("Database");
	      } else {
	    	  
	    	if(!getWinAndCompGUIObjects()) return;
	    	
	      }
      }//End if isMixedRsUsed
      
      Log.debug("TestStepProcessor.process setting default SCRIPT_NOT_EXECUTED status for instantiateComponentFunction()");
      //called script MUST set StepDriverTestInfo.statuscode accordingly.
      //this is one way we make sure the script executed and a script 
      //command failure was not encountered prematurely.
      testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);

      // check for my breakpoints, but maybe this should go ahead of the 'waitfor...' above??
      checkMyBreakpoints(getClass().getName() +" "+genericText.translate("Breakpoint"));

      boolean success = instantiateComponentFunction(params);
      if (! success) {      
        testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED); // just in case
        Log.debug(getClass().getName()+".process: NO COMPONENT FUNCTION FOR: "+testRecordData);
        Log.debug(getClass().getName()+".process: params:"+params);
      }
    } 

    // we don't seem to ever get this exception to here
    catch (RuntimeException re) {
      //re.printStackTrace();
      Log.debug("TestStepProcessor RuntimeException:", re);
      testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
      String message = re.getMessage();
      if ((message==null)||(message.length()==0)){ 
      	  message=re.getClass().getName();
      	  //re.printStackTrace();
      }
      log.logMessage(testRecordData.getFac(), message, WARNING_MESSAGE);
    } 
                     
    // these error messages need to be shorter and less ominous since other 
    // engines will continue to attempt to process the record.
    catch (SAFSException e) {
      testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
      String compType = null;
      
      try { compType = testRecordData.getCompType(); }
      catch (SAFSException ee) {} // ignore
      
      // we cannot have each engine log a warning message for unsupported commands.
      // another engine will likely support the command.  The driver, ultimately, 
      // will issue the final support failure message.
      // changed this to a Log.info message for debug output.
      Log.info("SAFS Engine "+ compType +" Functions did not properly execute " +
               "in table " + testRecordData.getFilename() + " at line " +
               testRecordData.getLineNumber()+ ".");
    } 
               
    // these error messages need to be shorter and less ominous since other 
    // engines will continue to attempt to process the record.
    catch (Exception ex) {
      testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
      ex.printStackTrace();
      Log.warn("Unexpected exception: "+ex+", "+ex.getMessage()+", "+
               getClass().getName()+".process.");
      postProcess();
      throw new SAFSRuntimeException(ex);
    } 

    // catch everything else -- which isn't always a good thing.
    // For example, if the user hits F11 in Rational products they are requesting 
    // to abort testing.  The HOOK for these products need to react according to 
    // what type of item has been thrown. ProcessRequest or something higher up 
    // the chain will have to deal with this.
    catch (Throwable th) {
	  testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
      th.printStackTrace();
      Log.warn("Unexpected Error: "+ th +", "+ th.getMessage()+", "+
                     getClass().getName() + ".process.");
      postProcess();
      throw new SAFSRuntimeException(th);
    }

	postProcess();
  }

  /**
   * Wait for the window/component occur.<br>
   * @param isWindow		boolean. If true, waiting for window object; otherwise waiting for component object.
   * @return				int		 0 represents the waiting status is ok.
   * @throws SAFSException
   * @see {@link #waitForObjectAndCheck(boolean)}
   */
  protected int waitForObject(boolean isWindow) throws SAFSException{
	  int status = 0;
	  String debugmsg = StringUtils.debugmsg(false);
	  
	  String mapName = testRecordData.getAppMapName();
	  String windowName = testRecordData.getWindowName();
	  String componentName = testRecordData.getCompName();
	  String item = isWindow ? windowName : windowName+":"+componentName;
	  
	  Log.debug(debugmsg+" mapName: "+mapName+" windowName: "+windowName+" componentName: "+componentName);
	  Log.info(debugmsg+item);
      try{
    	  if(isWindow){
    	      //Wait for the top window
    		  status = testRecordData.getDDGUtils().waitForObject(mapName,windowName,windowName, secsWaitForWindow);
    	  }else{
    		  //Wait for the component
    		  status = testRecordData.getDDGUtils().waitForObject(mapName,windowName,componentName, secsWaitForWindow);
    	  }
      }catch(SAFSObjectNotFoundException nf){
    	String message = null;
    	if(isWindow){
    		message = "TestStepProcess handling ObjectNotFoundException for Window "+item;
    	}else{
    		message = "TestStepProcess handling ObjectNotFoundException for Component "+item;
    	}
      	Log.debug(debugmsg+message);
      	status = -1;
      }
      
	  return status;
  }
  
  /**
   * Wait for the window/component to occur, and check if window/component can be found or not.<br>
   * If the window/component can not be found:<br>
   *   if there are more engines available then log warning and set status-code to "NotExecuted".<br>
   *   otherwise log error and set status-code to "Failure".<br>
   * 
   * @param isWindow		boolean  If true, the status is for window object; otherwise for component object.
   * @return				boolean	 true, if the window/component can be found.
   * @throws SAFSException
   * @see {@link #waitForObject(boolean)}
   */
  protected boolean waitForObjectAndCheck(boolean isWindow) throws SAFSException{
	  boolean statusOk = true;
	  String detail = null;
	  String debugmsg = StringUtils.debugmsg(false);

	  String windowName = testRecordData.getWindowName();
	  String componentName = testRecordData.getCompName();
	  
	  Log.debug(debugmsg+" windowName: "+windowName+" componentName: "+componentName);
	  //Wait for window/component
	  int status = waitForObject(isWindow);
      if (status != 0) {
    	  Log.debug(debugmsg+" The status is NOT ok.");
          //right now log a failure
          //future would be to check for and try to clear unexpected dialog boxes etc. (MONITOR)
    	  if(isWindow){
    		  detail = FAILStrings.convert(FAILStrings.NOT_FOUND_TIMEOUT, 
    				  					   windowName +" was not found within timeout "+ secsWaitForWindow, 
    				  					   windowName, String.valueOf(secsWaitForWindow));
    	  }else{
    		  detail = FAILStrings.convert(FAILStrings.NOT_FOUND_TIMEOUT, 
            							   componentName +" was not found within timeout "+ secsWaitForComponent, 
            							   componentName, String.valueOf(secsWaitForComponent));
    	  }
    	  
    	  String statusInfo = testRecordData.getStatusInfo();
    	  if(statusInfo!=null && statusInfo.contains(DriverConstant.MORE_ENGINES)){
    		  IndependantLog.warn(debugmsg+detail);
    		  IndependantLog.debug(debugmsg+"Have more engines, set status=SCRIPT_NOT_EXECUTED to give other engine a chance... ");
    		  //we need to set status of testrecord as 'NotExecuted'
    		  testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
//    		  componentWarningMessage(detail);
    	  }else{
    		  testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
    		  componentFailureMessage(detail);
    	  }
          
          statusOk = false;
      }
	  
	  return statusOk;
  }
  
  /**
   * <br><em>Note:</em>		This method will make the top window to become the active window.
   * @return				boolean	 If the window becomes active, return true.
   * @throws SAFSException
   */
  protected boolean setActiveWindow() throws SAFSException{
	  String debugmsg = this.getClass().getName()+".setActiveWindow(): ";

	  String mapName = testRecordData.getAppMapName();
	  String windowName = testRecordData.getWindowName();
	  
	  Log.debug(debugmsg+" mapName: "+mapName+" windowName: "+windowName);
      try{
    	  testRecordData.getDDGUtils().setActiveWindow(mapName,windowName,windowName);
      }catch(Exception act){
          	Log.debug(debugmsg+"Attempt to ignore TestStepProcessor window activation error:", act);
          	String msg = GENStrings.convert(GENStrings.ACTIVATE_WARN, windowName +" activation warning.  "+
          								    windowName +" may be disabled or obstructed.", windowName);
          	String detail = GENStrings.convert(GENStrings.WHERE_DETAIL, 
          			act.getMessage() +" in "+ testRecordData.getFilename() + " at Line " + testRecordData.getLineNumber(), 
          			act.getMessage(), testRecordData.getFilename(), String.valueOf(testRecordData.getLineNumber()));		
          	log.logMessage(testRecordData.getFac(), msg, detail, WARNING_MESSAGE);
          	return false;
      }
      
      return true;
  }
  
  /** 
   * A place to perform operations AFTER we have finished processing the record 
   * but before we return from the process function.
   * This can be overridden by subclasses.
   * <p>
   * added by dbauman Feb, 2004  so that a variable remains for the next test with
   * the status code.
   * copy the status code to variable 'customStatusCode'
   */
  protected void postProcess(){
    try {
        String var = "customStatusCode";
        String val = Integer.toString(testRecordData.getStatusCode());
        setVariable(var, val);
    } catch (SAFSException safsex) {
        // ignore
    }  	
  }
  
  
  /** <br><em>Purpose:</em> Interprets the fields of the test record and puts the appropriate
   ** values into the fields of testRecordData.
   ** <br><em>Side Effects:</em> {@link #testRecordData} fields are set from the inputRecord.
   ** <br><em>State Read:</em>   {@link #testRecordData}, the inputRecord field
   ** <br><em>Assumptions:</em>  The following order:
   ** <p><code>
   **      Field #1:   The TEST record type (T).
   ** </code><p>
   **      Subsequent fields would be as follows (with a separator between each field):
   ** <code>
   ** <br> Field:  #2            #3          #4          #5 - N
   ** <br> ==============  ==============  ========  ===============
   ** <br> WINDOWNAME,  COMPONENTNAME,   ACTION,  [PARAMETER(S),]
   ** </code>
   * @return Collection of the parameter(s)
   **/
  protected Collection interpretFields () throws SAFSException {
    String methodName = "interpretFields";
    Collection params = new ArrayList();
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
    } catch (IndexOutOfBoundsException ioobe) {
      log.logMessage(testRecordData.getFac(), getClass().getName()+".doRequest: tokenIndex:"+tokenIndex+", getting "+nextElem+
                ",\n   this is the inputRecord: "+
                testRecordData.getInputRecord(), FAILED_MESSAGE);
      throw new SAFSException(this, methodName, ioobe.getMessage()); // this should never happen
    } catch (SAFSException e) {
      log.logMessage(testRecordData.getFac(), getClass().getName()+".doRequest["+tokenIndex+"]: getting "+nextElem+
                ",\n   this is the inputRecord: "+
                testRecordData.getInputRecord() + "\n   message: " +
                e.getMessage(), FAILED_MESSAGE);
      throw e; // this only happens if we don't have the tokens (2, 3, 4)
    }
    return params;
  }

  /** <br><em>Purpose:</em>       
   ** This is where we call the component functions declared at the 
   ** top in the ComponentFunctions DECLARATION section.  When new 
   ** components are coded for recognition by this engine the declaration 
   ** must be made in that section and the call to them must be added here.
   * <br><em>Side Effects:</em> 
   * <br><em>State Read:</em>   {@link #testRecordData}
   * <br><em>Assumptions:</em>  uses Class.forName to instantiate, catches all exceptions
   * <br> Assumes that the qualified name of the component begins with org.safs.rational.CF
   * <br> So a tree would be class org.safs.raional.CFTree (where the compType was Tree).
   * <br> Note that the path is derived from testRecordData.getCompInstancePath() + "CF"+
   * compType, so the actual path can vary based on what the actual return value of that is.
   * <br> IF no class is found for the compType, then the generic version is instead
   * instantiated if possible: CFComponent is the generic type, try that before giving up...
   * <p>
   * Finally, we make use of a HashMap of components already instantiated,
   * and reuse them if possible, so that we do not have to incurr the cost of
   * instantiating over and over again.  field: '{@link #functionMap}'
   * <p>
   * NOTE: a custom type can be defined by placing it into a package like:
   * org.safs.rational.custom (that is tried second)
   * @param                     params, Collection
   * @return                    specific ComponentFunction instance, null if none found
   **/
  protected boolean instantiateComponentFunction (Collection params)
  //protected ComponentFunction instantiateComponentFunction (Collection params)
    throws SAFSException {
    String methodName = "TSR.ICF:";
    String compType = testRecordData.getCompType(); // most cases use this
    Log.info("instantiate, normal case, compType: "+compType);
    if (compType == null) {
      Log.info(getClass().getName()+
          ".instantiateComponentFunction: testRecordData.getCompType() returned null...");
    }
    Log.info(methodName +"Trying Custom Processors for "+compType);
	if(processCustomProcessor(params)) return true;    
    Log.info(methodName +"Trying SubClass Processors for "+compType);
	if(processSubclassProcessor(params)) return true;
    String classname = getComponentProcClassName();
    Log.info(methodName +"Trying Default Component Processor: "+classname);
    return instanceProcessorAndProcess(classname, params);
  }
  
  /** 
   * <br><em>Purpose:</em> log a standard component function FAILED_MESSAGE with detail. 
   * Expects testRecordData to already have filename, lineNumber, compName, and command.**/
  protected void componentFailureMessage(String detail) {
    String sfile = testRecordData.getFilename();
    String scomp = testRecordData.getCompName();
    String saction = testRecordData.getCommand();
    String sline = String.valueOf(testRecordData.getLineNumber());
    String altmsg = "Unable to perform "+ saction +" on "+ scomp +" in "+ sfile +" line "+ sline;

    String message = failedText.convert(LINE_FAILURE_4, altmsg, sfile, scomp, saction, sline);
    log.logMessage(testRecordData.getFac(), message,  FAILED_MESSAGE, detail);
  }
  
  /** 
   * <br><em>Purpose:</em> log a standard component function WARNING_MESSAGE with detail. 
   * Expects testRecordData to already have filename, lineNumber, compName, and command.**/
  protected void componentWarningMessage(String detail) {
	  String sfile = testRecordData.getFilename();
	  String scomp = testRecordData.getCompName();
	  String saction = testRecordData.getCommand();
	  String sline = String.valueOf(testRecordData.getLineNumber());
	  String altmsg = "Unable to perform "+ saction +" on "+ scomp +" in "+ sfile +" line "+ sline;
	  
	  String message = failedText.convert(LINE_FAILURE_4, altmsg, sfile, scomp, saction, sline);
	  log.logMessage(testRecordData.getFac(), message,  WARNING_MESSAGE, detail);
  }
  
  	
  
}
