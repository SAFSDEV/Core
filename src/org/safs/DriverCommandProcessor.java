/**
 * Copyright (C) (MSA, Inc), All rights reserved.
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
package org.safs;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <br><em>Purpose:</em> DriverCommandProcessor
 * <br><em>Lifetime:</em> instantiated by ProcessRequest
 * <p>
 * @author  Doug Bauman
 * @since   JUN 14, 2003
 *
 *   <br>   JUN 14, 2003    (DBauman) Original Release
 *   <br>   SEP 10, 2003    (Carl Nagle) Removed WARNING for "unknown driver command"
 *   <br>   SEP 16, 2003    (Carl Nagle) Implemented use of new SAFSLOGS logging.
 *   <br>   OCT 02, 2003    (DBAUMAN) Refactored to daisy-chain here, not in the instances.
 *   <br>   NOV 10, 2003    (Carl Nagle) Added isSupportedRecordType() implementation.
 *   <br>   NOV 13, 2003    (Carl Nagle) Added support for stored processors.
 *   <br>   NOV 13, 2003    (DBauman/Carl Nagle) Added support for breakpoints.
 *   <br>   NOV 19, 2003    (Carl Nagle) Additional refactoring.
 *   <br>   FEB 04, 2004	(DBauman) Copy the status code to variable 'customStatusCode'.
 *   <br>	FEB 12, 2004	(BNat)	Added the Throwable catch block.  This throwable catch block
 * 								    takes care of anything higher than Exceptions i.e. Error and Throwable.
 *   <br>	FEB 04, 2010	(Carl Nagle) Added DCDriverFlowCommand support.
 *
 **/
public class DriverCommandProcessor extends Processor {

  /** <br><em>Purpose:</em> constructor, calls super
   **/
  public DriverCommandProcessor () {
    super();
  }

  /** "DCDriverCommand"
   * Short classname appended to alternative/custom package names.
   * This is for dynamic instancing of DriverCommand processors.
   */
  public static final String DEFAULT_DRIVER_COMMAND_CLASSNAME = "DCDriverCommand";


   /** Supports standard DRIVER COMMAND record types (C, CW, CF) **/
  @Override
public boolean isSupportedRecordType(String recordType){
  	return isDriverCommandRecord(recordType);
  }

  /**
   * All DriverCommandProcessor instances may enable/disable and force breakpoints where they
   * deem this appropriate.  The static setting is shared by all DriverCommandProcessors.
   * By default, DriverCommandProcessor breakpoints are disabled.  However, a check is
   * done for the global Processor.isBreakpoints enabled which overrides this setting.
   * DriverCommand breakpoints can be enabled separately when all other global breakpoints are
   * disabled.
   **/
  protected static boolean dcBreakpointsOn = false;

  /** This may be set by any means.  In a runtime debugging environment this
   * will likely be set by a Driver Command.
   **/
  public static void setDCBreakpointsOn(boolean enabled){ dcBreakpointsOn = enabled;}

  /** test if DC-specific breakpoints are enabled.
   * @return true if dcBreakpointsOn is true.
   **/
  public static boolean isDCBreakpointsOn() {return dcBreakpointsOn;}

  /** test for enabled DC-specific breakpoints in addition to the standard
   * Processor breakpoints. Overrides Process.checkMyBreakpoints.
   **/
  @Override
protected void checkMyBreakpoints(String breakpoint_message){
    if (isMyBreakpointsOn() || isDCBreakpointsOn())
      activateBreakpoint(breakpoint_message);
  }


  /** True if the processor is suppose to use Standard Driver Command processors.
   * By default this is enabled.
   **/
  protected boolean standardDC = true;
  public void       setStandardDriverCommandsEnabled(boolean enabled){ standardDC = enabled;}
  public boolean    isStandardDriverCommandsEnabled(){ return standardDC;}

  protected DriverCommand dcMisc;
  protected DriverCommand dcFile;
  protected DriverCommand dcData;
  protected DriverCommand dcStrg;
  protected DriverCommand dcFlow;
  protected DriverCommand dcCustom;
  protected DriverCommand dcTimer;

  protected boolean processStandardDriverCommands(Collection params){
    if (dcFile == null) dcFile = new DCDriverFileCommands();
    if (initProcessorAndProcess(dcFile, params)) return true; // this one must be first(because of ifExist)
    if (dcMisc == null) dcMisc = new DCDriverMiscCommands();
    if (initProcessorAndProcess(dcMisc, params)) return true;
    if (dcData == null) dcData = new DCDriverDatabaseCommands();
    if (initProcessorAndProcess(dcData, params)) return true;
    if (dcStrg == null) dcStrg = new DCDriverStringCommands();
    if (initProcessorAndProcess(dcStrg, params)) return true;
    if (dcFlow == null) dcFlow = new DCDriverFlowCommands();
    if (initProcessorAndProcess(dcFlow, params)) return true;
    if (dcTimer == null) dcTimer = new DCDriverTimerCommands();
    if (initProcessorAndProcess(dcTimer,params)) return true;
    if (dcCustom == null) dcCustom = new org.safs.custom.DCDriverCommand();
    if (initProcessorAndProcess(dcCustom, params)) return true;
    return false;
  }

  /** Overrides Processor.getProcClassNames
   * The routine returns a list of:<br>
   *     super.getProcClassNames
   *     procInstancePath.DCDriverCommand
   * <p>
   * Use validProcessorClassName before adding class names to the list.
   * <p>
   * @return a list of potential processor classnames to try.
   */
  @Override
public  ArrayList getProcClassNames() {
      ArrayList classlist = super.getProcClassNames();
      String dot = ".";
  	  String classname = getProcInstancePath();
      // don't append dot if no package (java "default" package) specified
      if ((! classname.endsWith(dot))&&(classname.length() >0))
          classname = classname.concat(dot);
      //try packagename.DCDriverCommand
      classname = classname.concat(DEFAULT_DRIVER_COMMAND_CLASSNAME);
      if(validProcessorClassName(classname)) classlist.add(classname);
      return classlist;
  }

  /** Overrides Processor.getCustomProcClassName
   * The routine returns a list of:<br>
   *     super.getCustomProcClassNames
   *     customProcInstancePath.DCDriverCommand
   *     customProcInstancePath.custom.DCDriverCommand
   * <p>
   * Use validProcessorClassName before adding class names to the list.
   * <p>
   * @return a list of potential processor classnames to try.
   */
  @Override
public  ArrayList getCustomProcClassNames() {
      ArrayList classlist = super.getCustomProcClassNames();
      String dot = ".";
  	  String rootname = getCustomProcInstancePath();
      // don't append dot if no package (java "default" package) specified
      if ((! rootname.endsWith(dot))&&(rootname.length() >0))
          rootname = rootname.concat(dot);
      //try packagename.DCDriverCommand
      String classname = rootname + DEFAULT_DRIVER_COMMAND_CLASSNAME;
      if(validProcessorClassName(classname)) classlist.add(new String(classname));
      classname = rootname + "custom."+ DEFAULT_DRIVER_COMMAND_CLASSNAME;
      if(validProcessorClassName(classname)) classlist.add(classname);
      return classlist;
  }


  /** <br><em>Purpose:</em> process: process the testRecordData
   ** <p>
   **      At this point the Driver has determined we are dealing with a Driver Command Record.
   ** <p><code>
   **      Field #1:   The record type (C).
   ** </code><p>
   **      Subsequent fields would be as follows (with a separator between each field):
   ** <code>
   ** <br> Field:  #2         #3 - N
   ** <br> ==============  ==============
   ** <br> COMMAND,        [PARAMETER(S),]
   ** </code>
   ** <p>
   **      <em>COMMAND</em> the driver command
   ** <p>
   * <br><em>Side Effects:</em> {@link #testRecordData} statusCode is set
   * based on the result of the processing
   * <br><em>State Read:</em>   {@link #testRecordData}
   * <br><em>Assumptions:</em>  an 'SAFSException' is caught here, and if so, the status code
   * is set to StatusCodes.WRONG_NUM_FIELDS, which is the only error which should come
   * from the interpretFields() method which we call. This only happens if we don't have
   * the token (2)
   * <br>
   * Added by dbauman Feb, 2004  so that a variable remains for the next test with
   *  the status code. Copies the status code to variable 'customStatusCode'
   **/
  @Override
public void process() {
    try {
      // first interpret the fields of the test record and put them into the
      // appropriate fields of testRecordData
      Collection params = interpretFields();

      //called script MUST set StepDriverTestInfo.statuscode accordingly.
      //this is one way we make sure the script executed and a script
      //command failure was not encountered prematurely.
      testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);

      checkMyBreakpoints(getClass().getName() +" "+genericText.translate("Breakpoint"));

      //This is where we do the work.
      // process the component function if it exists
      boolean success = instantiateAndProcessDriverCommand(params);

      // just makin sure...
      if (! success) testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);

    } catch (SAFSException e) {
      //e.printStackTrace();
      testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);

      // we cannot have each engine log a warning message for unsupported commands.
      // another engine will likely support the command.  The driver, ultimately,
      // will issue the final support failure message.
      // changed this to a Log.info message for debug output.
      Log.debug("DriverCommand "+testRecordData.getCommand()+" did not properly execute " +
               "in table " + testRecordData.getFilename() + " at line " +
               testRecordData.getLineNumber()+ ", " + e.getMessage(),e);
    }
    catch (Exception ex) {
      Log.debug("DriverCommand "+testRecordData.getCommand()+" did not properly execute " +
                "in table " + testRecordData.getFilename() + " at line " +
                testRecordData.getLineNumber()+ ", " + ex.getMessage(), ex);
      //ex.printStackTrace();
      testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
      log.logMessage(testRecordData.getFac(), "unexpected exception: "+ex+", "+ex.getMessage(), FAILED_MESSAGE);
      log.logMessage(testRecordData.getFac(), getClass().getName()+".process: setting statusCode to: "+
               testRecordData.getStatusCode(), WARNING_MESSAGE);
    }
    catch (Throwable th) {
      //th.printStackTrace();
      Log.debug("DriverCommand "+testRecordData.getCommand()+" did not properly execute " +
                "in table " + testRecordData.getFilename() + " at line " +
                testRecordData.getLineNumber()+ ", " + th.getMessage());
	  testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
      log.logMessage(testRecordData.getFac(), "Unexpected Error: "+ th +", "+ th.getMessage(),
                     getClass().getName() + ".process.", WARNING_MESSAGE);
    }
    // added by dbauman Feb, 2004  so that a variable remains for the next test with
    // the status code.
    // copy the status code to variable 'customStatusCode'
    try {
        String var = "customStatusCode";
        String val = Integer.toString(testRecordData.getStatusCode());
        setVariable(var, val);
    } catch (SAFSException safsex) {
        // ignore
    }
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

  /** <br><em>Purpose:</em>       instantiateAndProcessDriverCommand
   * <br><em>Side Effects:</em>
   * <br><em>State Read:</em>   {@link #testRecordData}
   * <br><em>Assumptions:</em>  If it cannot process a local (org.safs) driver command, then
   * <br> assumes that the driver command is DCDriverCommand with the path
   * <br> taken from testRecordData.getCompInstancePath()
   * <p> for driver commands in different packages,
   * we make use of a HashMap of commands already instantiated,
   * and reuse them if possible, so that we do not have to incur the cost of
   * instantiating over and over again.  field: '{@link #commandMap}'
   * @param                     params, Collection
   * @return                    boolean, status
   **/
  protected boolean instantiateAndProcessDriverCommand (Collection params)
    throws SAFSException {
    String methodName = "DCP.IAPDC:";
    String command = testRecordData.getCommand();
    Log.info(methodName + "*************** attempting to run driver command: "+command);

    if (command == null) return false;

    // first try the commands in our package (org.safs) if enabled
    Log.info(methodName +"Trying Standard Processors for "+command+" (if enabled)");
    if ((isStandardDriverCommandsEnabled())     &&
        (processStandardDriverCommands(params)))
        return true;

    Log.info(methodName +"Trying Custom Processors for "+command);
	if(processCustomProcessor(params)) return true;
    Log.info(methodName +"Trying SubClass Processors for "+command);
	return processSubclassProcessor(params);
  }
}
