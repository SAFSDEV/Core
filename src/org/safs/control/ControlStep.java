/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.control;

import org.safs.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * Description   : ControlStep, used to control an engine by sending step commands
 * @author dbauman
 * @since   NOV 24, 2003
 *
 *   <br>   NOV 24, 2003    (DBauman) Original Release, copied from RobotJHook.
 * <p>
 * a controller for teststeps: if the arg is testscript, then
 * it loads the file specifed and treats each line as a test script.
 * If no args are specified, the it uses Class.forName to load the
 * org.safs.control.swing.ControlStepUI  class which is the Observable
 * to our Observer.  Our 'update' method gets the commands from
 * that GUI and executes them
 *
 *
 */

public class ControlStep extends Control implements Observer {
  /** the instance name of the GUI,
   ** default value is "org.safs.control.swing.ControlStepUI"
   **/
  public static String instanceName = "org.safs.control.swing.ControlStepUI";
  /** method names of the GUI class **/
  public static final String METHOD_SETINPUTRECORD = "setInputRecord";
  public static final String METHOD_ADDOBSERVER    = "addObserver";
  public static final String METHOD_START          = "start";
  public static final String METHOD_SETAPPMAPITEM  = "setAppMapItem";
  public static final String METHOD_SETSTATUSCODE  = "setStatusCode";
  public static final String METHOD_SETTESTLOGMSG  = "setTestLogMsg";
  public static final String METHOD_SETDELIM       = "setDelim";

  private ControlProcessingUI1 instance = null;

  private boolean gui = false;

  /** <br><em>Purpose:</em> main method
   * @param                     args, String[],
   * <br>NO ARGS: then try to load the GUI piece to do the control
   * <br>args[0]: can be:
   * <br>  testscript: load step commands from file in args[1]
   * <br>  guiname: load step commands from file in args[1]
   * <br>args[1]: if testscript, then args[1] is the filename
   * <br>args[1]: if guiname, then args[1] is the gui class name
   * <br>args[2]: if testscript, then args[2] can be:
   * <br>  debug: show debug info
   * <br>  nodebug: do not show debug info
   * <br>  otherwise: use args[2] as the delimeter/separator in the step commands (default: ,)
   **/
  public static void main (String[] args) {
    ControlStep controlStep = new ControlStep();
    controlStep.initHelper();
    if (args.length>2 && args[2].equalsIgnoreCase("debug")) controlStep.debug = true;
    else if (args.length>2 && args[2].equalsIgnoreCase("nodebug")) controlStep.nodebug = true;
    else if (args.length>2 && args[2].equalsIgnoreCase("driver")) {
      if (args.length>3) {
        controlStep.driver = args[3];
        System.out.println("driver name: "+controlStep.driver);
      }
    }
    else if (args.length>2) {
      controlStep.delim = args[2];
      System.out.println("delimiter: "+controlStep.delim);
      if (args.length>3) {
        controlStep.driver = args[3];
        System.out.println("driver name: "+controlStep.driver);
      }
    }

    if (args.length>0 && args[0].equals("testscript")) {
      if (args.length>1) {
        if (controlStep.debug) System.out.println("args[1]: "+args[1]);
        Collection c = new ArrayList();
        try {
          c = StringUtils.readfile(args[1]);
          controlStep.testJHook(c, args[1]);
        } catch (IOException ioe) {
          System.err.println("ioe: "+ioe);
        } catch (SAFSException se) {
          System.err.println("se: "+se);
        }
      } else {
        System.out.println("unspecified input filename: args[1] ");
      }
    } else if (args.length>0 && args[0].equals("guiname")) {
      if (args.length>1) instanceName = args[1];
      controlStep.gui = true;
      controlStep.instance = (ControlProcessingUI1) controlStep.instanceGui(instanceName);
      controlStep.instance.addObserver(controlStep);
      controlStep.instance.start();
    } else {
      controlStep.gui = true;
      controlStep.instance = (ControlProcessingUI1) controlStep.instanceGui(instanceName);
      controlStep.instance.addObserver(controlStep);
      controlStep.instance.start();
    }
  }

  /** Tries to instance a Gui
   * <p>
   * @param                     instanceName, String
   * @return instance
   **/
  public Object instanceGui (String instanceName) {
    if (instanceName == null) return null;
    String methodName = "ControlStep.instanceGui: ";
    try { // next try using Class.forName...
      Log.info(methodName+"trying :"+instanceName);
      Class guiClass = Class.forName(instanceName);
      Log.info(methodName+"guiClass: "+guiClass.getName());
      Object instance = guiClass.newInstance();
      return instance;
    } catch (NoClassDefFoundError nc) {
      System.err.println(methodName+"no class definition found: "+instanceName);
    } catch (ClassCastException cc) {
      System.err.println(methodName+"can't Cast class: "+instanceName);
    } catch (InstantiationException ie) {
      System.err.println(methodName+"can't instantiate class: "+instanceName);
    } catch (ClassNotFoundException ex) {
      System.err.println(methodName+"can't find class: "+instanceName);
    } catch (IllegalAccessException iae) {
      System.err.println(methodName+iae.toString());
    }
    return null;
  }

  /** <br><em>Purpose:</em> this class is called when a GUI sends us data
   * @param                     obs, Observable
   * @param                     arg, Object
   **/
  public void update(Observable obs, Object arg) {
    ControlParameters st = (ControlParameters) arg;
    String command = st.getCommand();
    if (debug) System.out.println("NEXT: "+command);
    if (command.toLowerCase().indexOf("getitem")==0) {
      if (!st.hasNext()) return;
      String map = (st.nextToken()).trim();
      if (!st.hasNext()) return;
      String section = (st.nextToken()).trim();
      if (!st.hasNext()) return;
      String item = (st.nextToken()).trim();
      if (debug) System.out.println("getitem: "+map+", "+section+", "+item);
      String val = helper.getAppMapItem(map, section, item);
      instance.setAppMapItem(val);
      return;
    } else if (command.toLowerCase().indexOf("openmap")==0) {
      if (!st.hasNext()) return;
      String map = (st.nextToken()).trim();
      if (debug) System.out.println("Openmap: "+map);
      if (map.length() > 0) {
        String cmd = "STAF LOCAL safsmaps OPEN "+map+" FILE "+map;
        if (debug) System.out.println(" Executing command: "+cmd);
        Runtime r = Runtime.getRuntime();
        try {
          Process p = r.exec(cmd);
          BufferedReader i = new BufferedReader(new InputStreamReader(p.getInputStream()));
          StringBuffer b = new StringBuffer();
          for(;;) {
            String line = i.readLine();
            if (line == null) break;
            b.append(line+" ");
          }
          if (b.toString().toLowerCase().indexOf("error") < 0) {
            appMapName = map;
            firstMapMsg=true;
            firstFoundMsg=true;
          }
          instance.setStatusCode(b.toString());
          i.close();
        } catch (java.io.IOException io) {
          System.err.println("IOException: "+io.getMessage());
        }
      }
      return;
    } else if (command.toLowerCase().indexOf("submit")==0) {
      if (!st.hasNext()) return;
      String window = st.nextToken();
      if (!st.hasNext()) return;
      String component = st.nextToken();
      String action = "Click";
      String param = null;
      if (st.hasNext()) {
        String a = (st.nextToken()).trim();
        if (a.length() > 0) action = a;
        if (st.hasNext()) {
          String p = (st.nextToken()).trim();
          if (p.length() > 0) param = p;
        }
      }
      Collection arr = new ArrayList();
      String step = "T"+delim+window+delim+component+delim+action;
      if (param != null) step = step+delim+param;
      if (debug) System.out.println(step);
      arr.add("C"+delim+"ClearAppMapCache");
      arr.add(step);
      try {
        testJHook(arr, "");
      } catch (SAFSException se) {
        se.printStackTrace();
        System.err.println("se: "+se);
        instance.setStatusCode(se.getMessage());
      }
      if (debug) System.out.println("");
    } else if (command.toLowerCase().indexOf("stepfile")==0) {
      if (!st.hasNext()) return;
      String stepfile = st.nextToken();
      Collection arr = new ArrayList();
      try {
        arr = StringUtils.readfile(stepfile);
        testJHook(arr, stepfile);
      } catch (IOException ioe) {
        System.err.println("ioe: "+ioe);
        instance.setStatusCode(ioe.getMessage());
      } catch (SAFSException se) {
        System.err.println("se: "+se);
        instance.setStatusCode(se.getMessage());
      }
      if (debug) System.out.println("");
    } else if (command.toLowerCase().indexOf("driver")==0) {
      if (!st.hasNext()) return;
      driver = st.nextToken();
      initHelper(); // init with this driver
      if (!st.hasNext()) return;
      String timeoutStr = st.nextToken();
      try {
        timeout = (new Long(timeoutStr)).longValue();
      } catch (NumberFormatException nfe) {
        System.err.println(nfe);
      }
      if (!st.hasNext()) return;
      String d = st.nextToken();
      if (d == null || d.length() != 1) {
        if (d.indexOf("Illegal: ") < 0) {
          instance.setDelim("Illegal: "+d);
        }
      } else {
        delim = d;
      }
      return;
    } else if (command.toLowerCase().indexOf("shutdown")==0) {
      Collection arr = new ArrayList();
      String step = org.safs.JavaHook.SHUTDOWN_RECORD;
      arr.add(step);
      try {
        testJHook(arr, "");
      } catch (SAFSException se) {
        System.err.println("se: "+se);
        instance.setStatusCode(se.getMessage());
      }
    } else if (command.toLowerCase().indexOf("exit")==0) {
      System.exit(0);
    }
  }

  protected void invokeSetInputRecord (String request) {
    if (gui) instance.setInputRecord(request);
  }

  /** show status to the gui
   **/
  protected void showStatus () {
    if (!gui) return;
    String var = "UNKNOWN ERROR";
    try {
      String gvar = helper.getVariable(SAFS_SHARED_TRD_PREFIX + STAFHelper.SAFS_VAR_STATUSCODE);
      Integer code = new Integer(gvar);
      int icode = code.intValue();
      if (icode==StatusCodes.OK) var = StatusCodes.STR_OK;
      else if (icode==StatusCodes.OK) var = StatusCodes.STR_OK;
      else if (icode==StatusCodes.NO_SCRIPT_FAILURE) var = StatusCodes.STR_NO_SCRIPT_FAILURE;
      else if (icode==StatusCodes.SCRIPT_WARNING) var = StatusCodes.STR_SCRIPT_WARNING;
      else if (icode==StatusCodes.GENERAL_SCRIPT_FAILURE) var = StatusCodes.STR_GENERAL_SCRIPT_FAILURE;
      else if (icode==StatusCodes.INVALID_FILE_IO) var = StatusCodes.STR_INVALID_FILE_IO;
      else if (icode==StatusCodes.SCRIPT_NOT_EXECUTED) var = StatusCodes.STR_SCRIPT_NOT_EXECUTED;
      else if (icode==StatusCodes.EXIT_TABLE_COMMAND) var = StatusCodes.STR_EXIT_TABLE_COMMAND;
      else if (icode==StatusCodes.IGNORE_RETURN_CODE) var = StatusCodes.STR_IGNORE_RETURN_CODE;
      else if (icode==StatusCodes.NO_RECORD_TYPE_FIELD) var = StatusCodes.STR_NO_RECORD_TYPE_FIELD;
      else if (icode==StatusCodes.UNRECOGNIZED_RECORD_TYPE) var = StatusCodes.STR_UNRECOGNIZED_RECORD_TYPE;
      else if (icode==StatusCodes.WRONG_NUM_FIELDS) var = StatusCodes.STR_WRONG_NUM_FIELDS;
      else var = "";
      var = var + ": " + code.toString();
    } catch (SAFSException se) {
      var = se.toString();
    } catch (NumberFormatException nfe) {
      var = nfe.toString();
    }
    instance.setStatusCode(var);
    // finally, show the testLog msg
    String msg = "";
    try {
      msg = helper.getVariable(SAFS_TESTLOG_MSG);
    } catch (SAFSException se) {
    }
    if (msg!=null && msg.trim().length()>0) System.out.println(""+msg);
    instance.setTestLogMsg(msg);
  }
}
