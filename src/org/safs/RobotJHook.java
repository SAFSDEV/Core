/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

import org.safs.logging.*;
import java.io.*;
import java.util.*;

import com.ibm.staf.STAFResult;

/**
 * Description   : RobotJ Hook Script
 * 
 * *** This class needs to be eliminated with RRobotJHook subclassing 'JavaHook' ***
 * 
 * @author canagl
 * @since   JUN 12, 2003
 *
 *   <br>   JUN 12, 2003    (DBauman) Original Release
 *   <br>   JUN 25, 2003    dbauman: modified to be part of org.safs and use STAFHelper.java
 *   <br>   SEP 16, 2003    CANAGL : modified to use new LogUtilities instance for logging
 *   <br>   DEC 08, 2004    (CANAGL) Added support for evaluateRuntimeExceptions
 *   <br>   FEB 27, 2009    (CANAGL) Moving START into RRobotJHook
 *
 * @safsinclude RobotJHook.include.htm
 * @see org.safs.JavaHook
 * @see org.safs.rational.RRobotJHook
 */

public abstract class RobotJHook {

  public static final String SHUTDOWN_RECORD         = "SHUTDOWN_HOOK";
  public static final String ROBOTJ_PROCESS_NAME     = STAFHelper.SAFS_ROBOTJ_PROCESS;

  protected static STAFHelper helper = null;
  static {
    try {
      helper =
        SingletonSTAFHelper.getInitializedHelper(ROBOTJ_PROCESS_NAME); // get Singleton
      Log.setHelper(helper);
    } catch (SAFSSTAFRegistrationException e) {
      e.printStackTrace();
    }
  }
  public STAFHelper getHelper() {return helper;}
  protected LogUtilities log;

  /** Convenience for local referencing instead of referencing AbstractLogFacility. **/
  protected static final int DEBUG_MESSAGE      = AbstractLogFacility.DEBUG_MESSAGE;

  /** Convenience for local referencing instead of referencing AbstractLogFacility. **/
  protected static final int GENERIC_MESSAGE    = AbstractLogFacility.GENERIC_MESSAGE;

  /** Log MessageType for local referencing. Inherits AbstractLogFacility value. **/
  protected static final int FAILED_MESSAGE     = AbstractLogFacility.FAILED_MESSAGE;

  /** Log MessageType for local referencing. Inherits AbstractLogFacility value. **/
  protected static final int FAILED_OK_MESSAGE  = AbstractLogFacility.FAILED_OK_MESSAGE;

  /** Log MessageType for local referencing. Inherits AbstractLogFacility value. **/
  protected static final int PASSED_MESSAGE     = AbstractLogFacility.PASSED_MESSAGE;

  /** Log MessageType for local referencing. Inherits AbstractLogFacility value. **/
  protected static final int WARNING_MESSAGE    = AbstractLogFacility.WARNING_MESSAGE;

  /** Log MessageType for local referencing. Inherits AbstractLogFacility value. **/
  protected static final int WARNING_OK_MESSAGE = AbstractLogFacility.WARNING_OK_MESSAGE;

  /** -1
   * The user has requested to STOP or ABORT execution of this engine.
   * This is often done by a special HOTKEY or some other tool-specific means.
   */
  public static final long REQUEST_USER_STOPPED_SCRIPT_REQUEST = -1;
  
  /** 0
   * Proceed with normal testing
   */
  public static final long REQUEST_PROCEED_TESTING = 0;
  

  public RobotJHook (){}

  /**
   * Allows the running script to set the appropriate LogUtilities class/subclass
   * to manage logging
   * 
   * @param lu LogUtilities used for logging
   ***/
  public void setLogUtil(LogUtilities lu) {
  	log = lu;
  }

  protected DDGUIUtilities utils = null;
  protected TestRecordHelper data = null;
  protected ProcessRequest processor = null;
  
  /**
   * Allows subclasses to change some portions of the initialization that occurs 
   * inside the start() method.
   * Initializes DDGUIUitlities, TestRecordHelper, ProcessRequest objects.
   * @see #getUtilitiesFactory()
   * @see #getTestRecordDataFactory(DDGUIUtilities
   * @see ProcessRequest#ProcessRequest(TestRecordHelper, LogUtilities))
   */
  protected void initializeUtilities(){
    if (utils==null) utils = getUtilitiesFactory();
    if (data==null) data = getTestRecordDataFactory(utils);
    utils.setTestRecordData(data);
    if (processor==null) processor = new ProcessRequest(data, log);
  }
  
  protected abstract DDGUIUtilities getUtilitiesFactory();
  protected abstract TestRecordHelper getTestRecordDataFactory();
  protected abstract TestRecordHelper getTestRecordDataFactory(DDGUIUtilities utils);

}
