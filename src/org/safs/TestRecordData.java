/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.engines.EngineInterface;

/**
 * <br><em>Purpose:</em>TestRecordData: holds key data used by a driver, like step driver.
 * Based on the SAFS Test Record Data doc.
 * <p>
 * This is the RobotJ equivalent class to the AUGUIInfo seen in Classic.
 * <p>
 * This can be populated from a STAF Variables pool, although that mechanism would be
 * provided by a helper class ({@link TestRecordHelper}.java).  Notice that
 * the DEDUCED fields are not populated from the Variables pool.  They would later be
 * set by a processor or Driver object one time.
 * <p>
 * This object can then be used by a Driver object.
 * <p>
 * The RobotJ Driver class should be a RobotJ test script(object) called with nothing
 * more than the this TestRecordData object as parameter.  The driver should have everything
 * it needs inside the TestRecordData instance.  In order to be sure of that, the processor
 * must derive the DEDUCED fields once before proceeding.
 * <p>
 * Initially, we expect to only forward "C" Driver Commands and "T" step-level test records
 * to RobotJ.  So the driver will then route this instance to either a DriverCommands
 * processor (another RobotJ object/script), or a ComponentFunctions processor
 * (another object/script) based on the record type. Although none of that logic is
 * found here, this is stated for context of how this data will later be used.
 * <p>
 * @author  Doug Bauman
 * @since   MAY 30, 2003
 *
 * <br>     MAY 30, 2003    (DBauman) Original Release
 * <br>     APR 12, 2004    (Carl Nagle) Added unique fileID field.
 * <br>     JUN 28, 2004    (Carl Nagle) Added setShutdownData.
 * <br>     NOV 15, 2005    (Bob Lawler) Added support for new TRD statusinfo field (RJL).
 **/
public abstract class TestRecordData implements java.io.Serializable {


  /** <br><em>Purpose:</em>  fileID: property: unique id of executing input stream
   ** <br><em>Initialized:</em>  by a helper class the first time, only set once.
   **/
  private String fileID;

  /** <br><em>Purpose:</em>  filename: property: name of executing input stream
   ** <br><em>Initialized:</em>  by a helper class the first time, only set once.
   **/
  private String filename;

  /** <br><em>Purpose:</em>  linenumber: property: linenumber within executing test stream
   ** <br><em>Initialized:</em>  by a helper class the first time, only set once.
   **/
  private long lineNumber;

  /** <br><em>Purpose:</em>  inputRecord: property: the current executing test record
   ** (See SAFS Record Formats)
   ** <br><em>Initialized:</em>  by a helper class the first time, only set once.
   ** <br><em>Example:</em>"T LoginWindow InfoGroupBox   VerifyProperty Text  \"Account Info\""
   **/
  private String inputRecord;

  /** <br><em>Purpose:</em>      lazily stores inputRecord tokens (based on separator)
   ** <br><em>Initialized:</em>  initally to null, set by method getInputRecordToken first time
   ** it is called
   **/
  private AbstractList inputRecordTokens = null;

  /** <br><em>Purpose:</em>  separator: property: char(s) delimiting the fields in the inputRecord
   ** <br><em>Initialized:</em> by a helper class the first time, only set once.
   ** <br><em>Example:</em>"\t"
   **/
  private String separator;

  /** <br><em>Purpose:</em>  testlevel:  the current executing test level (CYCLE, SUITE, STEP)
   ** <br><em>Initialized:</em>  by a helper class the first time, only set once.
   **/
  private String testLevel;

  /** <br><em>Purpose:</em>  appmapname:  Name of AppMap storing references (if applies)
   ** <br><em>Initialized:</em>  by a helper class the first time, only set once.
   **/
  private String appMapName;

  /** <br><em>Purpose:</em>  recordType:  record type DEDUCED from inputRecord, it is
   ** up to a processor do use the setter method to update this. (usually first token)
   ** <br><em>Initialized:</em>  by a processor
   ** <br><em>Example:</em>"C"
   ** <br><em>Example:</em>"T"
   **/
  private String recordType; // getter method deduces this.

  /** <br><em>Purpose:</em>  command:  command DEDUCED from inputRecord (? fourth token)
   ** <br><em>Initialized:</em>  by a processor
   ** <br><em>Example:</em>"VerifyProperty"
   **/
  private String command;  // getter method deduces this.

  /** <br><em>Purpose:</em>  windowname:  DEDUCED AppMap reference for parent window (if applies)
   ** <br><em>Initialized:</em>  by a processor
   ** <br><em>Example:</em>"LoginWindow"
   **/
  private String windowName; // getter method deduces this.

  /** <br><em>Purpose:</em>  windowguiid:  DEDUCED AppMap recognition string for windowname
   ** <br><em>Initialized:</em>
   ** <br><em>Re-set:</em> setter method
   ** <br><em>Example:</em>"Type=Window;Name=frmExistingLogin"
   **/
  private String windowGuiId;

  /** <br><em>Purpose:</em>  compname:  DEDUCED AppMap reference for child of window (if applies)
   ** <br><em>Initialized:</em>  by a processor
   ** <br><em>Example:</em>"InfoGroupBox"
   **/
  private String compName; //getter method deduces this.

  /** <br><em>Purpose:</em>  compguiid:  DEDUCED AppMap recognition string for compname
   ** <br><em>Initialized:</em>  by a processor
   ** <br><em>Example:</em>"Type=GroupBox;Name=frameLogin"
   **/
  private String compGuiId;

  /** <br><em>Purpose:</em>  comptype:  DEDUCED type of component (EditBox, Window, CheckBox, etc.),
   ** it can be obtained from the component or TestObject itself
   ** <br><em>Initialized:</em>  by a processor
   ** <br><em>Example:</em>"GroupBox"
   **/
  private String compType;

  /** <br><em>Purpose:</em>  compClass:  DEDUCED/derived from the component itself
   ** <br><em>Initialized:</em>  by a processor
   **/
  private String compClass;

  /** <br><em>Purpose:</em>  compModule:  DEDUCED/derived from the operating system
   ** (where did comp come from)
   ** <br><em>Initialized:</em>  by a processor
   **/
   private String compModule;

  /** <br><em>Purpose:</em>  environment:  DEDUCED  GUI implementation, it can be
   ** gotten normally from the tool, component, or TestObject tree itself
   ** <br><em>Initialized:</em>  by a processor
   ** <br><em>Example:</em>(Java, HTML, VisualBasic, etc.)
   **/
  private String environment;

  /** <br><em>Purpose:</em>  fac:  log facility name to use when logging
   ** <br><em>Initialized:</em>  by a helper class the first time, only set once.
   **/
  private String fac;

  /** <br><em>Purpose:</em>  statuscode:  status used and return during processing
   ** <br><em>Initialized:</em>  by a helper class the first time, only set once.
   **/
  private int statusCode;

  /** <br><em>Purpose:</em>  statusinfo:  used with statuscode, carries more status information
   ** <br><em>Initialized:</em>  by a helper class the first time.
   **/
  private String statusInfo;

  /**
   **/
  private String altCompType;

  /**
   * An array of possible test-record-separator. As following:
   * <ul>
   * <li>"\t"
   * <li>","
   * <li>";"
   * <li>"_"
   * <li>":"
   * <li>"|"
   * <li>"#"
   * <li>"@"
   * <li>"$"
   * <li>" "
   * </ul>
   */
  public static String[] POSSIBLE_SEPARATOR = {"\t", ",", ";", "_", ":", "|", "#", "@", "$", " "};

  /** <br><em>Purpose:</em> no-arg constructor to make this fully qualified javabean
   **/
  public TestRecordData() {
  }

  /** <br><em>Purpose:</em> reinit this object to be reused over again.
   * <br><em>Assumptions:</em>  fields set to null (or primitive values 0, false, ...):
   * <br> fileID
   * <br> filename
   * <br> lineNumber
   * <br> inputRecord
   * <br> inputRecordTokens
   * <br> separator
   * <br> testLevel
   * <br> appMapName
   * <br> recordType
   * <br> command
   * <br> windowName
   * <br> windowGuiId
   * <br> compName
   * <br> compGuiId
   * <br> compType
   * <br> compClass
   * <br> compModule
   * <br> environment
   * <br> fac
   * <br> statusCode
   * <br> statusInfo
   **/
  public void reinit () {
    fileID = null;
    filename = null;
    lineNumber = 0;
    inputRecord = null;
    inputRecordTokens = null;
    separator = null;
    testLevel = null;
    appMapName = null;
    recordType = null;
    command = null;
    windowName = null;
    windowGuiId = null;
    compName = null;
    compGuiId = null;
    compType = null;
    compClass = null;
    compModule = null;
    environment = null;
    fac = null;
    statusCode = 0;
    statusInfo = null;	//this field is always used, but not always set.
  }

  /** <br><em>Purpose:</em> accessor method(s)
   **/
  public String getFileID () {return fileID;}
  public void setFileID (String fileID) {this.fileID = fileID;}

  /** <br><em>Purpose:</em> accessor method(s)
   **/
  public String getFilename () {return filename;}
  public void setFilename (String filename) {this.filename = filename;}

  /** <br><em>Purpose:</em> accessor method(s)
   **/
  public long getLineNumber () {return lineNumber;}
  public void setLineNumber (long lineNumber) {this.lineNumber = lineNumber;}

  /** <br><em>Purpose:</em> accessor method(s)
   **/
  public String getInputRecord () {return inputRecord;}
  public void setInputRecord (String inputRecord) {this.inputRecord = inputRecord;}

  /** <br><em>Purpose:</em> accessor method(s)
   **/
  public String getSeparator () {return separator;}
  public void setSeparator (String separator) {this.separator = separator;}

  /** <br><em>Purpose:</em> accessor method(s)
   **/
  public String getTestLevel () {return testLevel;}
  public void setTestLevel (String testLevel) {this.testLevel = testLevel;}

  /** <br><em>Purpose:</em> accessor method(s)
   **/
  public String getAppMapName () {return appMapName;}
  public void setAppMapName (String appMapName) {this.appMapName = appMapName;}

  /** <br><em>Purpose:</em> accessor method(s)
   ** <br> The first token before the separator is the record type (from the inputRecord)
   ** <br> There is no setter method because this is a deduced property
   **/
  public String getRecordType () {return recordType;}
  public void setRecordType (String recordType) {this.recordType = recordType;}

  /** <br><em>Purpose:</em> accessor method(s)
   **/
  public String getCommand () {return command;}
  public void setCommand (String command) {this.command = command;}

  /** <br><em>Purpose:</em> accessor method(s)
   **/
  public String getWindowName () throws SAFSException {return windowName;}
  public void setWindowName (String windowName) {this.windowName = windowName;}

  /** <br><em>Purpose:</em> accessor method(s)
   **/
  public String getWindowGuiId () throws SAFSException {return windowGuiId;}
  public void setWindowGuiId (String windowGuiId) {this.windowGuiId = windowGuiId;}

  /** <br><em>Purpose:</em> accessor method(s)
   **/
  public String getCompName () {return compName;}
  public void setCompName (String compName) {this.compName = compName;}

  /**
   * Return the windowName+componentName.<br>
   *
   * @return String, windowName+":"+componentName. <br>
   *                 null if windowName is null. <br>
   *                 windowName+":"+windowName if componentName is null. <br>
   */
  public String getWinCompName(){
	  if(windowName==null) return null;
	  if(compName==null) return windowName+":"+windowName;
	  return windowName+":"+compName;
  }

  /** <br><em>Purpose:</em> accessor method(s)
   **/
  public String getCompGuiId () throws SAFSException {return compGuiId;}
  public void setCompGuiId (String compGuiId) {this.compGuiId = compGuiId;}

  /** <br><em>Purpose:</em> accessor method(s)
   **/
  public String getCompType () throws SAFSException {return compType;}
  public void setCompType (String compType) {this.compType = compType;}

  /** <br><em>Purpose:</em> accessor method(s)
   **/
  public String getAltCompType () {return altCompType;}
  public void setAltCompType (String altCompType) {
    //Log.debug(".....setAltCompType: "+altCompType);
    this.altCompType = altCompType;
  }

  /** <br><em>Purpose:</em> accessor method(s)
   **/
  public String getCompClass () throws SAFSException {return compClass;}
  public void setCompClass (String compClass) {this.compClass = compClass;}

  /** <br><em>Purpose:</em> accessor method(s)
   **/
  public String getCompModule () throws SAFSException {return compModule;}
  public void setCompModule (String compModule) {this.compModule = compModule;}

  /** <br><em>Purpose:</em> accessor method(s)
   **/
  public String getEnvironment () {return environment;}
  public void setEnvironment (String environment) {this.environment = environment;}

  /** <br><em>Purpose:</em> accessor method(s)
   **/
  public String getFac () {return fac;}
  public void setFac (String fac) {this.fac = fac;}

  /** <br><em>Purpose:</em> accessor method(s)
   **/
  public int getStatusCode () {return statusCode;}
  public void setStatusCode (int statusCode) {this.statusCode = statusCode;}

  /** <br><em>Purpose:</em> accessor method(s)
   **/
  public String getStatusInfo () {return statusInfo;}
  public void setStatusInfo (String statusInfo) {this.statusInfo = statusInfo;}

  /** <br><em>Purpose:</em> get the {@link #inputRecord} token, based on 'separator'
   ** starting from 0.
   * <br><em>Side Effects:</em> {@link #inputRecordTokens} (lazily set first time in)
   * <br><em>State Read:</em>  {@link #inputRecordTokens}, {@link #inputRecord}
   * <br><em>Assumptions:</em>  We use lazy instantiation to populate the ArrayList
   * {@link #inputRecordTokens}.  Initially it is null.  We use StringTokenizer first time only.
   * <br> We also assume that inputRecord and separator are already setup when we are called.
   * @param                     n, int, the index of which token to return,
   * @return The n(th) token from the 'inputRecord' based on  'separator' is returned.
   * if either 'inputRecord' or 'separator' are null, then SAFSException is thrown
   * @exception IndexOutOfBoundsException - if n is out of range
   * (n < 0 || n >= inputRecordTokens.size()).
   * @exception SAFSNullPointerException, if either 'inputRecord' or 'separator' are null
   **/
  public String getInputRecordToken (int n) throws SAFSNullPointerException {
    if (inputRecordTokens == null) {
      inputRecordTokens = new ArrayList();
      String input = getInputRecord();
      String sep = getSeparator();
      if (input != null && sep != null) {
        for (StringTokenizer st = new SAFSStringTokenizer(input, sep); st.hasMoreTokens(); ) {
          inputRecordTokens.add(st.nextToken());
        }
      } else {
        throw new SAFSNullPointerException(this, "getInputRecordToken",
                                "either 'inputRecord' or 'separator' are null");
      }
    }
    return (String) inputRecordTokens.get(n);
  }

  /** <br><em>Purpose:</em> int, the number of tokens in the inputRecord (based on separator)
   * <br><em>Assumptions:</em>  don't pass along IndexOutOfBoundsException, that is caught
   * and a length of zero is the correct size.
   * @return                    int, the number of tokens in the inputRecord
   * @exception SAFSNullPointerException, if either 'inputRecord' or 'separator' are null
   **/
  public int inputRecordSize () throws SAFSNullPointerException {
    try {
      getInputRecordToken(0);
    } catch (IndexOutOfBoundsException ioobe) {} // not an error, it will be zero length
    return inputRecordTokens.size();
  }

  /** <br><em>Purpose:</em> This method takes the index 'n', retrieves the token
   ** from getInputRecordToken, trim() leading and trailing whitespace,
   ** and strips one leading and/or trailing quotation mark(#34)--if they exist.
   * @param                     n, int, the index of which token to return,
   * @return The n(th) token from the 'inputRecord' based on  'separator' is returned.
   * if either 'inputRecord' or 'separator' are null, then SAFSException is thrown
   * @exception IndexOutOfBoundsException - if n is out of range
   * (n < 0 || n >= inputRecordTokens.size()).
   * @exception SAFSNullPointerException, if either 'inputRecord' or 'separator' are null
   **/
  public String getTrimmedUnquotedInputRecordToken (int n) throws SAFSNullPointerException {
    String result = getInputRecordToken(n);
    if (result == null) return result;
    result = StringUtils.getTrimmedUnquotedStr(result);
    return result;
  }

  public String getNotSeparatorString(){
	  String stringOtherthanSeparator = ":";
	  for(int i=0;i<POSSIBLE_SEPARATOR.length;i++){
		  if(!POSSIBLE_SEPARATOR[i].equalsIgnoreCase(separator)){
			  stringOtherthanSeparator = POSSIBLE_SEPARATOR[i];
			  break;
		  }
	  }
	  return stringOtherthanSeparator;
  }

  /** toString method
   **/
  public String toString() {
    StringBuffer buf = new StringBuffer(768);
    buf.append(" [TestRecordData: ");
    buf.append("\n   fileID: ");
    buf.append(getFileID());
    buf.append("\n   filename: ");
    buf.append(getFilename());
    buf.append("\n   lineNumber: ");
    buf.append(getLineNumber());
    buf.append("\n   inputRecord: ");
    buf.append(getInputRecord());
    buf.append("\n   separator: ");
    buf.append(getSeparator());
    buf.append("\n   testLevel: ");
    buf.append(getTestLevel());
    buf.append("\n   appMapName: ");
    buf.append(getAppMapName());
    buf.append("\n   recordType: ");
    buf.append(getRecordType());
    buf.append("\n   command: ");
    buf.append(getCommand());
    buf.append("\n   windowName: ");
    try {
      buf.append(getWindowName());
    } catch (SAFSException ss1) {}
    //buf.append("\n   windowGuiId: ");
    //buf.append(getWindowGuiId());
    buf.append("\n   compName: ");
    buf.append(getCompName());
    //buf.append("\n   compGuiId: ");
    //buf.append(getCompGuiId());
    buf.append("\n   compType: ");
    try {
      buf.append(getCompType());
    } catch (SAFSException ss1) {}
    buf.append("\n   compClass: ");
    try {
      buf.append(getCompClass());
    } catch (SAFSException ss1) {}
    buf.append("\n   compModule: ");
    try {
      buf.append(getCompModule());
    } catch (SAFSException ss1) {}
    buf.append("\n   environment: ");
    buf.append(getEnvironment());
    buf.append("\n   fac: ");
    buf.append(getFac());
    buf.append("\n   statusCode: ");
    buf.append(getStatusCode()+" "+StatusCodes.getStatusString(getStatusCode()));
    buf.append("\n   statusInfo: ");
    buf.append(getStatusInfo());
    buf.append("\n ]");
    return buf.toString();
  }

  public void setShutdownData(){
  	setInputRecord(EngineInterface.COMMAND_SHUTDOWN_HOOK);
  	setFilename("");
  	setLineNumber(0);
  	setTestLevel("");
  	setFac("");
  	setAppMapName("");
  	setSeparator("");
  	setStatusCode(DriverConstant.STATUS_SCRIPT_NOT_EXECUTED);
  	setStatusInfo("");
  }

  private String instanceName = STAFHelper.SAFS_HOOK_TRD;
  /**
   * Prefix used to retrieve and store SAFSVARS TestRecordData values.
   * The most common instance name is STAFHelper.SAFS_HOOK_TRD
   * This rarely ever needs to be changed by subclasses.
   **/
  public String getInstanceName() {return instanceName;}
  /**
   * Prefix used to retrieve and store SAFSVARS TestRecordData values.
   * The most common instance name is STAFHelper.SAFS_HOOK_TRD
   * This rarely ever needs to be set/changed by subclasses.
   **/
  public void setInstanceName(String instanceName) {this.instanceName = instanceName;}

  /**
   * Copies the object's data into the target data and returns the target.
   */
  public TestRecordData copyData(TestRecordData target){
     target.setAltCompType(this.getAltCompType());
     target.setAppMapName(this.getAppMapName());
     target.setCommand(this.getCommand());
     try{target.setCompClass(this.getCompClass());}catch(Exception x){;}
     try{target.setCompGuiId(this.getCompGuiId());}catch(Exception x){;}
     try{target.setCompModule(this.getCompModule());}catch(Exception x){;}
     target.setCompName(this.getCompName());
     try{target.setCompType(this.getCompType());}catch(Exception x){;}
     target.setEnvironment(this.getEnvironment());
     target.setFac(this.getFac());
     target.setFileID(this.getFileID());
     target.setFilename(this.getFilename());
     target.setInputRecord(this.getInputRecord());
     target.setLineNumber(this.getLineNumber());
     target.setRecordType(this.getRecordType());
     target.setSeparator(this.getSeparator());
     target.setStatusCode(this.getStatusCode());
     target.setStatusInfo(this.getStatusInfo());
     target.setTestLevel(this.getTestLevel());
     try{target.setWindowGuiId(this.getWindowGuiId());}catch(Exception x){;}
     try{target.setWindowName(this.getWindowName());}catch(Exception x){;}
     return target;
  }
}

