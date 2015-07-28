/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.control;

import org.safs.*;
import java.io.*;
import java.util.*;

/**
 * Description   : Control, commands used to control an engine by sending step commands
 * @author dbauman
 * @since   NOV 24, 2003
 *
 *   <br>   NOV 24, 2003    (DBauman) Original Release, cut from ControlStep, which extends us
 * <p>
 *
 */

public abstract class Control {
  public static final String ROBOTJ_PROCESS_NAME     = STAFHelper.SAFS_ROBOTJ_PROCESS;
  public static final String SAFS_SHARED_TRD_PREFIX  = STAFHelper.SAFS_HOOK_TRD;
  public static final String SAFS_TESTLOG_MSG        = "SAFS/TestLog/msg";

  /** driver, any of the engines, possibly SAFS/RobotJ or SAFS/DriverCommands **/
  protected String driver = ROBOTJ_PROCESS_NAME;
  /** number of seconds to allow for the driver's response.
   **              ( < 0 = wait indefinitely)
   **/
  protected long timeout = 300;
  protected STAFHelper helper = null;
  /** the Test Step delimiter **/
  protected String delim = ",";
  protected TestRecordData data = new HookTestRecordData();
  protected String filename="";
  /** if the appmapName is null, the code will search for the appmapname
   ** list from the safsmaps service, and use the first one **/
  protected String appMapName = null;
  protected String fac = "fac"; //??
  protected String testLevel="STEP";
  protected boolean debug=false;
  protected boolean nodebug=true;

  /**
   **  setSAFSTestRecordData
   **<p>
   ** DESCRIPTION:
   **
   **  Populates the shared "SAFS/Hook/" TestRecordData in SAFSVARS
   **  This is used to pass info TO and FROM other tools (like RobotJ).
   **
   **
   ** @exception SAFSException
   **
   **      Throws exception if STAF is not installed.
   **
   **<br> Author: Doug Bauman
   **<br> Date: JUN 19, 2003
   **<br> History:
   **<br>
   **<br>      JUN 19, 2003    Original Release
   **<br>      JUN 26, 2003    dbauman: ported to java
   **<br>      NOV 24, 2003    dbauman: copyied from RobotJHook
   **/
  public void setSAFSTestRecordData () throws SAFSException {
    if (!helper.isSAFSVARSAvailable()) {
      throw new SAFSException("STAF_NOT_INSTALLED: SAFSVARS");
    }
    helper.setVariable(SAFS_SHARED_TRD_PREFIX+ STAFHelper.SAFS_VAR_FILENAME, data.getFilename());
    helper.setVariable(SAFS_SHARED_TRD_PREFIX+ STAFHelper.SAFS_VAR_LINENUMBER, Long.toString(data.getLineNumber()));
    helper.setVariable(SAFS_SHARED_TRD_PREFIX+ STAFHelper.SAFS_VAR_INPUTRECORD, data.getInputRecord());
    helper.setVariable(SAFS_SHARED_TRD_PREFIX+ STAFHelper.SAFS_VAR_SEPARATOR, data.getSeparator());
    helper.setVariable(SAFS_SHARED_TRD_PREFIX+ STAFHelper.SAFS_VAR_TESTLEVEL, data.getTestLevel());
    helper.setVariable(SAFS_SHARED_TRD_PREFIX+ STAFHelper.SAFS_VAR_APPMAPNAME, data.getAppMapName());
    helper.setVariable(SAFS_SHARED_TRD_PREFIX+ STAFHelper.SAFS_VAR_STATUSCODE, Integer.toString(data.getStatusCode()));
    try {
      helper.setVariable(SAFS_SHARED_TRD_PREFIX+ STAFHelper.SAFS_VAR_FAC,        data.getFac());
    } catch (SAFSException se) {
      se.printStackTrace();
    }
  }

  /**
   **  getSAFSTestRecordData
   **<p>
   ** DESCRIPTION:
   **
   **  Populates the data structure FROM shared "SAFS/Hook/" 
   **  TestRecordData in SAFSVARS.  This is used to pass info TO and FROM other 
   **  tools (like RobotJ).
   **
   ** @exception SAFSException
   **
   **      Throws "Error in loading DLL" if STAF is not installed.
   **
   **<br> Orig Author: Carl Nagle
   **<br> Orig   Date: JUN 19, 2003
   **<br> History:
   **<br>
   **<br>      JUN 19, 2003    Original Release
   **<br>      JUN 26, 2003    dbauman: ported to java
   **<br>      NOV 24, 2003    dbauman: copyied from RobotJHook
   **/
  public void getSAFSTestRecordData() throws SAFSException {
    
    if (!helper.isSAFSVARSAvailable()) {
      throw new SAFSException("STAF_NOT_INSTALLED: SAFSVARS");
    }
    data.setFilename(helper.getVariable(SAFS_SHARED_TRD_PREFIX+ STAFHelper.SAFS_VAR_FILENAME));
    try{
      data.setLineNumber(Long.parseLong(helper.getVariable(SAFS_SHARED_TRD_PREFIX+ STAFHelper.SAFS_VAR_LINENUMBER)));
    }catch (NumberFormatException nfe) {
      throw new SAFSException("bad number for line number: "+
                              helper.getVariable(SAFS_SHARED_TRD_PREFIX+ STAFHelper.SAFS_VAR_LINENUMBER));
    }
    data.setInputRecord(helper.getVariable(SAFS_SHARED_TRD_PREFIX+ STAFHelper.SAFS_VAR_INPUTRECORD));
    data.setSeparator(helper.getVariable(SAFS_SHARED_TRD_PREFIX+ STAFHelper.SAFS_VAR_SEPARATOR));
    data.setTestLevel(helper.getVariable(SAFS_SHARED_TRD_PREFIX+ STAFHelper.SAFS_VAR_TESTLEVEL));
    data.setAppMapName(helper.getVariable(SAFS_SHARED_TRD_PREFIX+ STAFHelper.SAFS_VAR_APPMAPNAME));
    data.setFac(helper.getVariable(SAFS_SHARED_TRD_PREFIX+ STAFHelper.SAFS_VAR_FAC));
    try{
      data.setStatusCode(Integer.parseInt(helper.getVariable(SAFS_SHARED_TRD_PREFIX+ STAFHelper.SAFS_VAR_STATUSCODE)));
    }catch (NumberFormatException nfe) {
      throw new SAFSException("bad number for status code: "+
                              helper.getVariable(SAFS_SHARED_TRD_PREFIX+ STAFHelper.SAFS_VAR_STATUSCODE));
    }
  }

  /**
   **  callSAFSJHook
   **<p>
   ** DESCRIPTION:
   **
   **  Perform the full event-driven exchange for Classic to invoke a driver 
   **  to process a test input record.
   **
   ** @exception SAFSException if STAF is not installed.
   **
   **
   **<br> Orig Author: Doug Bauman
   **<br> Orig   Date: NOV 24, 2003
   **<br> History:
   **<br>
   **<br>      NOV 24, 2003    dbauman: copyied from RobotJHook
   **/
  public void callSAFSJHook () throws SAFSException {
    //wait for JReady
    if (debug) Log.info( "Waiting for "+driver+"Ready...");
    helper.waitEvent(driver+"Ready", timeout);

    //gain mutex
    //Log.info( "Waiting for MUTEX...");
    //helper.waitSTAFMutex(STAFHelper.J_MUTEX, timeout);

    //set inputrecord
    if (debug) Log.info( "Setting TestRecordData...");
    setSAFSTestRecordData();

    //dispatch 
    if (debug) Log.info( "Pulsing "+driver+"Dispatch...");
    helper.pulseEvent(driver+"Dispatch");

    //wait for result
    if (debug) Log.info( "Waiting for Results...");
    helper.waitEvent(driver+"Results", timeout);

    //get result
    if (debug) Log.info( "Getting TestRecordData...");
    getSAFSTestRecordData();

    //signal done
    if (debug) Log.info( "Pulsing "+driver+"Done...");
    helper.pulseEvent(driver+"Done");

    //Log.info( "Releasing the MUTEX...");
    //helper.releaseSTAFMutex(STAFHelper.J_MUTEX);
  }

  static class HookTestRecordData extends TestRecordData {
    public String getCompInstancePath(){ return "org.safs."; }
    public String getInstanceName() {return "";}
  }

  protected boolean firstMapMsg=true;
  protected boolean firstFoundMsg=true;
  /** <br><em>Purpose:</em> prepare the test record data
   * @return                    TestRecordData
   **/
  public TestRecordData prepTestRecordData (long ln) {
    data.setTestLevel(testLevel);
    data.setFilename(filename);
    data.setLineNumber(ln);
    data.setSeparator(delim);
    data.setFac(fac);
    if (appMapName != null) {
      data.setAppMapName(appMapName);
      if (firstMapMsg) {
        System.out.println("map: "+data.getAppMapName());
        firstMapMsg=false;
        firstFoundMsg=true;
      }
    } else {
      try {
        Collection names = helper.getAppMapNames();
        //System.out.println("..........................appmap names: "+names);
        String name = (String) names.iterator().next();
        data.setAppMapName(name);
        if (firstFoundMsg) {
          System.out.println("using first found map: "+data.getAppMapName());
          firstFoundMsg=false;
          firstMapMsg=true;
        }
      } catch (Exception ex) {
        data.setAppMapName("classicc");// default
        System.out.println("error, no map name, using: "+data.getAppMapName());
      }
    }
    //System.out.println("..........................appmap name: "+data.getAppMapName());
    return data;
  }

  /** <br><em>Purpose:</em> fireJRequest, execute a test steps.
   ** First sets the status code to SCRIPT_NOT_EXECUTED and
   ** the inputRecord to the request, then calls 'callSAFSJHook'
   * @param                     request, one test step
   * @exception                 SAFSException
   **/
  public void fireJRequest (String request) throws SAFSException {
    try { // initialize
      helper.setVariable(SAFS_TESTLOG_MSG, "");
    } catch (SAFSException se) {
    }
    data.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED); //script not executed
    data.setInputRecord(request);
    invokeSetInputRecord(request);
    callSAFSJHook();
    showStatus();
  }

  /** <br><em>Purpose:</em> testJHook, execute a collection of test steps
   * @param                     filename, String
   * @param                     c, Collection of Test Steps
   * @exception                 SAFSException
   **/
  public void testJHook (Collection c, String filename) throws SAFSException {
    this.filename = filename;
    if (driver==null) driver = ROBOTJ_PROCESS_NAME;
    if (debug) Log.setLogLevel(1);
    else Log.setLogLevel(2);
    if (debug) System.out.println("test of JHook, handle: " + helper.getHandleNumber());
    //STAFregisterClassic();
    long ln=1;
    for(Iterator i = c.iterator(); i.hasNext(); ln++) {
      String command = ((String) i.next()).trim();
      if (command == null) continue;
      command = command.trim();
      if (command.length()==0) continue;
      // ignore if it begins with a ;
      if (command.substring(0, 1).equals(";")) {
        continue;
      }
      TestRecordData data = prepTestRecordData(ln);
      fireJRequest(command);
      String back = helper.getVariable(SAFS_SHARED_TRD_PREFIX + STAFHelper.SAFS_VAR_STATUSCODE);
      if (!nodebug) {
        if (debug || !back.equals("-1")) {
          System.out.println("command: "+command);
          System.out.println("Received: "+ back);
        }
      }
    }
  }

  protected void initHelper () {
    if (helper == null) {
      helper = SingletonSTAFHelper.getHelper(); // get Singleton
    }
    try {
      helper.initialize("TEST/"+driver);
    } catch (SAFSException se) {
      System.err.println(se.toString());
    }
  }

  protected abstract void invokeSetInputRecord (String request);
  protected abstract void showStatus ();
}
