/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

import java.util.ArrayList;
import java.util.Collection;

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
 *
 *   <br>   JUN 14, 2003    (DBauman) Original Release
 *   <br>   NOV 10, 2003    (CANAGL) Added isSupportedRecordType() implementation.
 *   <br>   NOV 14, 2003    (CANAGL) Refactoring with changes to Processor.
 **/
public abstract class DriverCommand extends Processor {


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
}
