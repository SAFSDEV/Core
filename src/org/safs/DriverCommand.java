/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * Logs for developers, not published to API DOC.
 * History:<br>
 * 
 *   <br>   JUN 14, 2003    (DBauman) Original Release
 *   <br>   NOV 10, 2003    (Carl Nagle) Added isSupportedRecordType() implementation.
 *   <br>   NOV 14, 2003    (Carl Nagle) Refactor with changes to Processor.
 *   <br>   APR 07, 2016    (Lei Wang) Refactor to handle some driver commands generally like OnGUIExistsGotoBlockID/OnGUINotExistGotoBlockID.
 */
package org.safs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.safs.model.commands.DDDriverFlowCommands;
import org.safs.text.FAILStrings;
import org.safs.text.GENStrings;

/**
 * <br><em>Purpose:</em> abstract DriverCommand, enforces that the 'process' method be implemented
 * <p>
 * <ul>
 * <li>DRIVER COMMANDS (so far...):
 * <ul>
 * <li>  DCDriverCommand
 * <ul>
 * <li>    callScript
 * <ul>
 * <li>      1st param: RobotJ script to call
 * <li>      Example: C,CallScript,TestScript2
 * </ul>
 * </ul>
 * </ul>
 * </ul>
 * @author  Doug Bauman
 * @since   JUN 14, 2003
 **/
public abstract class DriverCommand extends Processor {

  /**
   * The current command keyword being executed.
   */
  protected String command = null;
	
  /**
   * Used to iterate over any params provided for a command.
   */
  protected Iterator<String> iterator = null;
  
  /**
   * Provide the GUI Utilities, sub-class may has its own implementation.
   */
  protected DDGUIUtilities utils = null;

  /** <br><em>Purpose:</em> constructor
   **/
  public DriverCommand () {
    super();
  }
  
   /** Supports standard DRIVER COMMAND record types (C, CW, CF) **/
  public boolean isSupportedRecordType(String recordType){
  	return isDriverCommandRecord(recordType);
  }

  /** <br><em>Purpose:</em> Interprets the fields of the driver command record and puts the
   ** appropriate values into the fields of testRecordData.
   ** <br><em>Side Effects:</em> {@link #testRecordData} fields are set from the inputRecord.
   ** <br><em>State Read:</em>   {@link #testRecordData}, the inputRecord field
   ** <br><em>Assumptions:</em>  The following order:
   ** <p><code>
   **      Field #1:   The DRIVER COMMAND record type (C).
   ** </code><p>
   **      Subsequent fields would be as follows (with a separator between each field):
   ** <code>
   ** <br> Field:  #2        #3 - N
   ** <br> ==============  ===============
   ** <br> COMMAND,        [PARAMETER(S),]
   ** </code>
   * @return Collection of the parameter(s)
   **/
  protected Collection interpretFields () throws SAFSException {
	String methodName = "interpretFields";
	Collection params = new ArrayList();
	String nextElem = ""; // used to log errors in the catch blocks below
	int tokenIndex = 1; // start from 1, because we already have the recordType which was 0
	try {
	  nextElem = "command"; //..get the driver command, the second token (from 1)
	  String command = testRecordData.getTrimmedUnquotedInputRecordToken(tokenIndex);
	  testRecordData.setCommand(command);
      
	  for(tokenIndex = 2; tokenIndex < testRecordData.inputRecordSize(); tokenIndex++) {
		nextElem = "param"; //..get the param, tokens #3 - N (from 1)
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
	  throw e; // this only happens if we don't have the token (2)
	}
	return params;
  }

  public void process() {
	  String dbg = StringUtils.debugmsg(false);
	  
	  try{
		  init();
	  } catch (SAFSException ex) {
		  String message = "Failed to Initialize test data, due to "+StringUtils.debugmsg(ex);
		  IndependantLog.error(dbg+message);
		  issueInputRecordFailure(message);
		  return;
	  }

	  localProcess();

	  if(testRecordData.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
		  generalCommandProcess();
	  }
	  
	  if(testRecordData.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
		  commandProcess();
	  }

	  if(testRecordData.getStatusCode()==StatusCodes.SCRIPT_NOT_EXECUTED){
		  super.process();
	  }
  }
  
  /** 
   * Initialize test record, utilities. Subclass may override to provide more specific implementation.
   **/
  protected void init() throws SAFSException{
	  IndependantLog.debug("DriverCommand.init() initializing test data ... ");
	  // first interpret the fields of the test record and put them into the
	  // appropriate fields of testRecordData		
	  setParams(interpretFields());

	  //Initialize the status code to "Not Executed"
	  testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
	  
	  //Initialize those protected fileds
	  utils = testRecordData.getDDGUtils();
	  command = testRecordData.getCommand();
	  iterator = params.iterator();
	  
	  //Set the window and component name in test-record-data
	  try{
		  Iterator iter = params.iterator();
		  if(DDDriverFlowCommands.ONGUIEXISTSGOTOBLOCKID_KEYWORD.equalsIgnoreCase(command) ||
			 DDDriverFlowCommands.ONGUINOTEXISTGOTOBLOCKID_KEYWORD.equalsIgnoreCase(command)){
			  //Skip the field BlockId in TestRecord
			  iter.next();
		  }
		  if(iter.hasNext()) testRecordData.setWindowName((String)iter.next());
		  if(iter.hasNext()) testRecordData.setCompName((String)iter.next());
	  }catch(Exception e){
		  IndependantLog.warn("DriverCommand.init() failed to set window/component name in testrecord-data. Ignore "+StringUtils.debugmsg(e));
	  }
  }
  
  /** 
   * This superclass implementation does absolutely nothing.
   * Subclasses should implement this to provide their new or overriding functionality. 
   **/
  protected void localProcess(){ }

  /**
   * Handle some driver commands generally.
   */
  private void generalCommandProcess(){
	  String dbg = StringUtils.debugmsg(false);

	  Log.debug(dbg+" try to handle command "+command);
	  if(command.equalsIgnoreCase(DDDriverFlowCommands.ONGUIEXISTSGOTOBLOCKID_KEYWORD)){
		  onGUIGotoCommands(true);
	  } else if(command.equalsIgnoreCase(DDDriverFlowCommands.ONGUINOTEXISTGOTOBLOCKID_KEYWORD)){
		  onGUIGotoCommands(false);
	  } else if(command.equalsIgnoreCase(DDDriverFlowCommands.CALLSCRIPT_KEYWORD)){
		  callScript();
	  }
  }
  
  public static String callJUnitScript(String classname) throws ClassNotFoundException, SAFSException{
	  String debugmsg = StringUtils.debugmsg(false);

	  JUnitCore junit = new JUnitCore();
	  Result jresult = junit.run(Class.forName(classname));

	  if(jresult == null){
		  String detail = "JUnit execution returned a null Result.";
		  throw new SAFSException(detail);
	  }else{
		  StringBuffer sb = new StringBuffer();
		  sb.append(jresult.getRunCount()+ " tests run.\n");
		  sb.append(jresult.getIgnoreCount()+" tests ignored.\n");
		  sb.append(jresult.getFailureCount()+ " tests failed.\n");
		  sb.append("Runtime: "+ jresult.getRunTime() +" milliseconds.\n");
		  List<Failure> failures = jresult.getFailures();
		  sb.append("");
		  for(Failure failure:failures){
			  sb.append("   "+ failure.toString()+"\n");
			  sb.append("\n");
		  }
		  IndependantLog.debug(debugmsg+" succeeded with result:\n "+sb.toString());
		  return sb.toString();
	  }   

  }
  
  private void callScript(){
	  if (params.size() < 1) {
		  this.issueParameterCountFailure("ScriptName");
		  return;
	  }

	  testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
	  String debugmsg = StringUtils.debugmsg(false);

	  Iterator iterator = params.iterator();
	  String scriptName = (String) iterator.next();
	  IndependantLog.info(debugmsg+"............................. handling [JUnit] script: "+scriptName);
	  try {
		  String result = callJUnitScript(scriptName);

		  log.logMessage(testRecordData.getFac(),
				  genericText.convert(TXT_SUCCESS_2,
						  getTestRecordData().getCommand()+" "+ scriptName +" successful.",
						  getTestRecordData().getCommand(), scriptName),
						  GENERIC_MESSAGE,
						  result);
		  testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
	  } catch (ClassNotFoundException e) {
		  //This will not be considered as a failure, the script will be tried in a specific engine later. Just log a warning.
		  IndependantLog.warn(debugmsg+"'"+scriptName+"' was not executed! Due to "+StringUtils.debugmsg(e));
		  testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
	  } catch (SAFSException e) {
		  String detail = e.getMessage();
		  String errmsg = FAILStrings.convert(FAILStrings.SCRIPT_ERROR, "Script '"+ scriptName +"' error: "+ detail, scriptName, detail);
		  IndependantLog.error(debugmsg+errmsg);
		  issueErrorPerformingAction(errmsg);
		  testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	  }
  }
  
  /**
   * Process the driver command specifically. 
   * Here an empty implementation is provided, sub-class will give the detail.
   */
  protected void commandProcess() { }

  /** 
   * issue OK status and generic message (not a PASSED message):<br>
   * [action] successful. [comment]<br>
   * [detail]<br>
   * string comment and detail are expected to already be localized, but can be null.
   **/
  protected void issueGenericSuccess(String comment, String detail){
      testRecordData.setStatusCode(StatusCodes.OK);
      String success = GENStrings.convert(GENStrings.SUCCESS_1, 
                       testRecordData.getCommand() +" successful.",
                       testRecordData.getCommand());
      success += comment != null ? " "+ comment : "";
      log.logMessage(testRecordData.getFac(), success, detail, GENERIC_MESSAGE);
  }

  /** 
   * issue OK status and Generic message (not a PASSED message):<br>
   * [action] successful using [x]. [Comment]<br>
   * Comment is expected to already be localized if present, but can be null.
   **/
  protected void issueGenericSuccessUsing(String x, String comment){
      testRecordData.setStatusCode(StatusCodes.OK);
      String success = GENStrings.convert(GENStrings.SUCCESS_2A, 
                       testRecordData.getCommand() +" successful using "+ x,
                       testRecordData.getCommand(), x);
      success += comment != null ? " "+ comment : "";
      log.logMessage(testRecordData.getFac(), success, GENERIC_MESSAGE);
  }

  /** 
   * issue OK status and PASSED message.<br>
   * [action] successful. [comment]<br>
   * string comment is expected to already be localized, but can be null.
   **/
  protected void issuePassedSuccess(String comment){
      testRecordData.setStatusCode(StatusCodes.OK);
      String success = GENStrings.convert(GENStrings.SUCCESS_1, 
                       testRecordData.getCommand() +" successful.",
                       testRecordData.getCommand());
      success += comment != null ? " "+ comment : "";
      log.logMessage(testRecordData.getFac(), success, PASSED_MESSAGE);	  
  }
  
  /** issue OK status and generic message (not a PASSED message)<br>
   * [action] successful. [comment]<br>
   * string comment is expected to already be localized, but can be null.
   **/
  protected void issueGenericSuccess(String comment){
      testRecordData.setStatusCode(StatusCodes.OK);
      String success = GENStrings.convert(GENStrings.SUCCESS_1, 
                       testRecordData.getCommand() +" successful.",
                       testRecordData.getCommand());
      success += comment != null ? " "+ comment : "";
      log.logMessage(testRecordData.getFac(), success, GENERIC_MESSAGE);
  }

  /** 
   * Uses testRecordData to create standard failure text.
   * @return [command] failure in table [filename] at line [n].
   * Called by other routines. 
   */
  protected String getStandardFailureDetail(){
      return FAILStrings.convert(FAILStrings.STANDARD_ERROR, 
              testRecordData.getCommand() +
              " failure in table "+ testRecordData.getFilename() +
              " at line "+ testRecordData.getLineNumber(),
              testRecordData.getCommand(),
              testRecordData.getFilename(),
              String.valueOf(testRecordData.getLineNumber()));
  }
  
  /** 
   * Uses testRecordData to create standard warning text.
   * @return [command] warning in table [filename] at line [n].
   * Called by other routines. 
   */
  protected String getStandardWarningDetail(){
      return FAILStrings.convert(FAILStrings.STANDARD_WARNING, 
              testRecordData.getCommand() +
              " warning in table "+ testRecordData.getFilename() +
              " at line "+ testRecordData.getLineNumber(),
              testRecordData.getCommand(),
              testRecordData.getFilename(),
              String.valueOf(testRecordData.getLineNumber()));
  }
  
  /** 
   * Issue [command] was not successful. [warning]. <br>
   * [command warning in table filename at line n].
   * <p>
   * Sets status to WARNING and issues a WARNING message.
   * string warning is expected to already be localized.
   **/
  protected void issueActionWarning(String warning){
      testRecordData.setStatusCode(StatusCodes.SCRIPT_WARNING);
      String no_success = FAILStrings.convert(FAILStrings.NO_SUCCESS_1, 
                       testRecordData.getCommand() +" was not successful.",
                       testRecordData.getCommand());
      String detail = getStandardWarningDetail();
      log.logMessage(testRecordData.getFac(), no_success+" "+warning, detail, WARNING_MESSAGE);
  }

  /** Issue parameter count error and failure message **/
  protected void issueParameterCountFailure(){
      issueInputRecordFailure(FAILStrings.text(FAILStrings.PARAMSIZE, 
                         "Insufficient Parameters."));
  }    
  
  /** Issue parameter count error and failure message 
   * @param detail detail about specific missing params or command format
   ***/
  protected void issueParameterCountFailure(String detail){
      issueInputRecordFailure(FAILStrings.text(FAILStrings.PARAMSIZE, detail));
  }    
  
  /** Issue parameter value error and failure message along with a failure status. 
   * @param paramName -- the Name of the action parameter in error. **/
  protected void issueParameterValueFailure(String paramName){
      String error = FAILStrings.convert(FAILStrings.BAD_PARAM,
    	             "Invalid parameter value for "+ paramName +".",
    	             paramName);
      issueInputRecordFailure(error);
  }    
  
  /** Issue a file error and failure message 
   * Filename does not need to be localized. **/
  protected void issueFileErrorFailure(String filename){
      issueActionFailure(FAILStrings.convert(FAILStrings.FILE_ERROR, 
                         "Error opening or using "+filename,
                         filename));
  }    
  
  /** Issue a generic error and failure message 
   * The cause parameter is expected to already be localized. **/
  protected void issueUnknownErrorFailure(String cause){
      issueActionFailure(FAILStrings.convert(FAILStrings.GENERIC_ERROR, 
                         "*** Error *** "+cause,
                         cause));
  }
  
  /**
   * "Action processed with a negative result."<br>
   * "[detail]"
   * <br><em>Purpose:</em> set failure status and log a simple failure message when a test record got processed with negative result. 
   *            When an action intends to make a judgment, a negative result is gotten if the logical result of the judgment 
   *            is false; the test step will be logged as 'FAIL' although it got executed correctly. Called by some comparing actions.
   *
   * * @param detail -- a detail message already properly localized, if necessary.
   */
  protected void issueExecutionNegativeMessage(String detail) {
    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	String action = testRecordData.getCommand();
    String altMsg = action+" processed with a negative result.";
    String message = failedText.convert(FAILStrings.EXECUTED_WITH_NEGATIVERESULT, altMsg,"", "", action);
    log.logMessage(testRecordData.getFac(), message,  FAILED_MESSAGE, detail);      
  }

  /**
   * "[Action] using [x] finished with a negative result."<br>
   * "[detail]"
   *
   * * @param detail -- a detail message already properly localized, if necessary.
   */
  protected void issueActionUsingNegativeMessage(String x, String detail) {
    testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	String action = testRecordData.getCommand();
    String message = failedText.convert(FAILStrings.EXECUTED_USING_NEGATIVERESULT, 
    		         action +" using " + x +" finished with a negative result.", 
    		         action, x);
    log.logMessage(testRecordData.getFac(), message,  FAILED_MESSAGE, detail);      
  }
  
  /**
   * Check the existence of the window/component and return the satisfaction of the existence.<br>
   * The satisfaction depends on component's existence and our expectation of its existence.<br>
   * Subclass only need to implement this method to get the following keywords work.
   * <ul>
   * <li>OnGUIExistsGotoBlockID
   * <li>OnGUINotExistGotoBlockID
   * </ul>
   * 
   * @param expectedExist		boolean, the expected existence of the window/component.<br>
   *                            	     when <b>true</b> is provided, if the component is present within timeout, then it return true; otherwise return false;<br>
   *                            	     when <b>false</b> is provided, if the component is not present or disappears within timeout, then it return true; otherwise return false;<br>
   * @param mapNam				String, the map name
   * @param window				String, the window's name
   * @param component			String, the component's name
   * @param timeoutInSeconds	int, the timeout in seconds
   * @return					boolean, true if the expected presence is satisfied
   * @throws SAFSException		is thrown out if there is something wrong.
   * 
   * @see #onGUIGotoCommands(boolean)
   */
  protected boolean checkGUIExistence(boolean expectedExist, String mapNam, String window, String component, int timeoutInSeconds) throws SAFSException{
	  throw new SAFSException(StringUtils.debugmsg(false)+" has not been implemented!");
  }
  
  /** <br><em>Purpose:</em> OnGUI(Not)Exist(s)GotoBlockID
   * e.g. C, OnGUIExistsGotoBlockID, BlockID, Window, Component[, Timeout]
   * 
   * @author Bob Lawler	11.15.2005	Created (RJL) 
   * @author Lei Wang 04.05.1016	Refactor code: Move this method from package org.safs.rational to org.safs<br>
   *                                           Abstract some content to method {@link #checkGUIExistence(boolean, String, String, String, int)}, which is left to be implemented in subclass.
   * <p>
   * This method first determines if branching should occur based on whether or not the GUI is found. Then,
   * it sets the TestRecordData status to BRANCH_TO_BLOCKID so that the driver knows to attempt a branch when
   * control returns to the driver.  This method utilizes the TestRecordData field statusinfo to store the 
   * name of the blockID.
   * 
   * @see #checkGUIExistence(boolean, String, String, String, int)
   **/
  private void onGUIGotoCommands(boolean expectedExist) {
	  if (params.size() < 3) {
		  issueParameterCountFailure();
		  return;
	  }
	  String debugmsg = StringUtils.debugmsg(false);
	  iterator = params.iterator();
	  String DEFAULT_TIMEOUT = "15";

	  String blockName = (String) iterator.next();
	  String windowName = (String) iterator.next();
	  String compName = (String) iterator.next();
	  String seconds = null;

	  int timeoutInSeconds = 0;
	  try { // optional param, timeout
		  seconds = (String)iterator.next();
		  timeoutInSeconds = Integer.parseInt(seconds);
	  } catch (Exception e) {
		  IndependantLog.warn("Failed to convert timeout parameter '"+seconds+"' to integer, it is probably wrong. Met "+StringUtils.debugmsg(e));
		  seconds = DEFAULT_TIMEOUT;
		  timeoutInSeconds = Integer.parseInt(seconds);
	  }
	  IndependantLog.debug(debugmsg+" optional parameter '"+ "TIMEOUT" +"' set to '"+ seconds +"'.");

	  if (timeoutInSeconds < 0) timeoutInSeconds = 0;
	  Log.info(debugmsg +command+": blockid: "+blockName+", window:"+windowName+", component:"+compName+", seconds:"+seconds);

	  try {
		  String msg = "";
		  // wait for the window/component
		  boolean satisfied = checkGUIExistence(expectedExist, testRecordData.getAppMapName(),windowName, compName, timeoutInSeconds);

		  if (satisfied) {
			  msg = GENStrings.convert(GENStrings.BRANCHING, 
					  command +" attempting branch to "+ blockName +".", 
					  command, blockName);
			  if(expectedExist){
				  msg += "  "+ GENStrings.convert(GENStrings.FOUND_TIMEOUT, 
						  compName +" found within timeout "+ seconds, 
						  compName, seconds);					  
			  }else{
				  msg += "  "+ GENStrings.convert(GENStrings.NOT_EXIST, 
						  compName +" does not exist", 
						  compName);
			  }
			  //set statuscode and statusinfo fields so driver will know to branch
			  testRecordData.setStatusCode(StatusCodes.BRANCH_TO_BLOCKID);
			  testRecordData.setStatusInfo(blockName);
		  }
		  else {
			  msg = GENStrings.convert(GENStrings.NOT_BRANCHING, 
					  command +" not branching to "+ blockName +".", 
					  command, blockName);
			  if(expectedExist){
				  msg += "  "+ FAILStrings.convert(FAILStrings.NOT_FOUND_TIMEOUT, 
						  compName +" not found within timeout "+ seconds, 
						  compName, seconds);					  
			  }else{
				  msg += "  "+ GENStrings.convert(GENStrings.EXISTS, 
						  compName +" exists", 
						  compName);
			  }
			  testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		  }

		  log.logMessage(testRecordData.getFac(), msg, GENERIC_MESSAGE); 
	  } catch (SAFSException se) {
		  IndependantLog.error(debugmsg +command+" failed. Met Exception", se);
		  issueErrorPerformingAction(se.getMessage());
	  }
  }
}
