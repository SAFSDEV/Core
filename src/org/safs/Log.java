/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

import java.io.*;
import java.nio.charset.Charset;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFResult;

import java.util.Date;
import java.text.SimpleDateFormat;

import org.safs.staf.STAFProcessHelpers;
import org.safs.staf.embedded.EmbeddedHandle;
import org.safs.tools.*;

/**
 * <em>SAFS Debug Log</em>
 * <p>
 * This class provides an independent logging capability available to both Java and non-Java
 * SAFS clients.  Non-Java clients will write to this log through STAF using the STAF QUEUE
 * of a registered "SAFS/TESTLOG" client of this class.  Java clients will simply write to
 * the static methods of this Log class.
 * <p>
 * The class is intended to act as a development, debugging, and troubleshooting log
 * allowing SAFS clients to write detailed information to this separate
 * log that will not clutter the normal log output during production testing.
 * <p>
 * Typical usage of this class requires the following steps:
 * <p><ol>
 * <li>STAF must be running.  Start STAF if necessary.
 * <br><em>!!! DO NOT SHUTDOWN STAF UNTIL AFTER YOU HAVE SHUTDOWN THIS DEBUG LOG !!!</em>
 * <p>
 * <li>Launch the debug Log as a standalone application:
 * <p>
 * <ul><b>Example debug Log invocations:</b>
 * <p>
 * a) java org.safs.Log<p>
 * b) java org.safs.Log 1<p>
 * c) java org.safs.Log INFO<p>
 * d) java org.safs.Log -file:c:/safs/data/debuglog.txt<p>
 * e) java org.safs.Log DEBUG -file:c:/safs/data/debuglog.txt<p>
 * </ul>
 * <p>
 * Example (a) shows launching the debug Log with the default DEBUG log level.<br>
 * Examples (b) and (c) are examples launching the debug Log with the INFO log level.
 * <p>
 * The static main method will launch the debug console and receive 'logmsg' messages
 * on it's STAF QUEUE.
 * <p>
 * <li><em>Java classes</em> call the appropriate static methods to write to the open
 * debug log. The public methods (debug/info/generic/pass/warn/error) are provided to do
 * logging during Java development/maintenance/debugging of the code when the SAFSLOGS
 * service is not being used.
 * <p>
 * Some log levels, like INFO and INDEX, will always log to this
 * class since they are ONLY used during development/maintenance and will never be
 * written to SAFSLOGS.  The org.safs.logging.LogUtilities class can also be enabled
 * to forward messages to this class, if that is desirable.
 * <p>
 * The internal 'message' method will first try to send using STAF using the 'logmsg'
 * method, if that fails, then it will simply do a System.out.println.
 * <p>
 * <li><em>Non-Java STAF clients:</em><br>
 * Any STAF client wishing to send a message to this debug log
 * will send a specially formatted STAF QUEUE service message like this:
 * <p>
 * <ul>
 * QUEUE NAME SAFS/TESTLOG MESSAGE "&lt;formatted_message>"
 * </ul>
 * <p>
 * The formatted_message is expected to be in the following format:
 * <ul>
 * "n|message"
 * </ul>
 * where 'n' is the loglevel intended for this message.
 * <ul>
 * <li>0 = DEBUG
 * <li>1 = INFO
 * <li>2 = INDEX
 * <li>3 = GENERIC
 * <li>4 = PASS
 * <li>5 = WARN
 * <li>6 = ERROR
 * </ul>
 * <p>
 * <li>
 * </ol>
 * <p>
 * The debug Log has some special reserved messages:
 * <ul>
 * <br> SHUTDOWN: used to shutdown this static main log
 * <br> CLS: used to clear lines to separate log sessions. Also clears and restarts any file log.
 * <br> LEVELn: change the log level (n=0 to 6)
 * <br> LEVELdebug : change the log level (0:DEBUG)
 * <br> LEVELinfo : change the log level (1:INFO)
 * <br> LEVELindex : change the log level (2:INDEX)
 * <br> LEVELgeneric : change the log level (3:GENERIC)
 * <br> LEVELpass : change the log level (4:PASS)
 * <br> LEVELwarn : change the log level (5:WARN)
 * <br> LEVELerror : change the log level (6:ERROR)
 * <br> SUSPEND : Suspend debug log recording
 * <br> RESUME  : Resume devug log recording
 * <br> HELP : list HELP text
 * <br> LIST : list HELP text
 * </ul>
 * <p> So, in order to change the level to WARN, do this:
 * <p>
 * <ul>
 * <b>staf local queue queue name SAFS/TESTLOG message LEVEL5</b>
 * <p>
 * OR
 * <p>
 * <b>staf local queue queue name SAFS/TESTLOG message LEVELwarn</b>
 * </ul>
 * <p>
 * And to shutdown the debug Log, do this:
 * <p>
 * <ul>
 * <b>staf local queue queue name SAFS/TESTLOG message SHUTDOWN</b>
 * </ul>
 * <p>
 * @author  Doug Bauman
 * @since   JUN 03, 2003
 *
 *   <br>   AUG 01, 2003 (DBauman) Original Release
 *   <br>   Aug 01, 2003 (Carl Nagle) Added GENERIC log level between INFO and PASS.
 *   <br>   Sep 02, 2003 (DBauman) Added INDEX log level between INFO and GENERIC to give and indication of the index= levels used for recognition strings
 *   <br>   Sep 16, 2003 (Carl Nagle) Removed references to defunct org.safs.LogUtilities
 *   <br>   JUL 01, 2004 (Carl Nagle) Lotsa cleanup and user-friendly additions.
 *   <br>   AUG 12, 2004 (Carl Nagle) Added file output option.
 *   <br>   NOV 05, 2004 (Carl Nagle) Log.class disabled by default for performance.
 *   <br>   APR 15, 2005 (Carl Nagle) Removed static initialization System.err.println
 *   <br>   APR 15, 2005 (Carl Nagle) Added some clarifying documentation for non-Java clients
 *                                    writing to this log.  Improved the displayed HELP accordingly.
 *   <br>   JAN 08, 2007 (Carl Nagle) Added more clarifying documentation.
 *   <br>   JUL 30, 2009 (Carl Nagle) Added Output Filename to debug console at startup.
 *   <br>   MAY 12, 2014 (Carl Nagle) Added support for SUSPEND and RESUME of debug logging.
 **/
public class Log {

  /** Enables/Disables the use of this class for performance reasons. */
  public static boolean ENABLED  = false;

  /**
   * True to log to System.out (Console).  False to disable. Typically disabled when Embedded.
   */
  private static boolean console_enabled = true;
  private static boolean isEmbedded = false;

  private static boolean isSuspended = false;

  /** "SAFS/TESTLOG" - registered STAF Process name for the primary debug console. */
  public static final String SAFS_TESTLOG_PROCESS  = "SAFS/TESTLOG";

  /**
   * "SAFSTESTLOGClient" -- registered STAF Process name for other JVM/processes.
   * Must not have exact root name (SAFS/TESTLOG) as the primary process or the
   * STAFHelper.isToolAvailable function will return erroneous 'true'.*/
  public static final String SAFS_TESTLOG_CLIENT   = "SAFSTESTLOGClient";

  /** "SAFS/TESTLOG/MSG" -- SAFSVARS variable to monitor sent messages. */
  public static final String SAFS_TESTLOG_VARIABLE = "SAFS/TESTLOG/MSG";

  /** "SHUTDOWN" -- Command to close the debug console and unregister with STAF.*/
  public static final String SAFS_TESTLOG_SHUTDOWN = "SHUTDOWN";

  /** "SUSPEND" -- Command to SUSPEND debug logging temporarily.*/
  public static final String SAFS_TESTLOG_SUSPEND = "SUSPEND";

  /** "RESUME" -- Command to RESUME debug logging.*/
  public static final String SAFS_TESTLOG_RESUME = "RESUME";

  /** "CLS" -- Command to clear lines in the debug console to separate logging sessions.*/
  public static final String SAFS_TESTLOG_CLS      = "CLS";

  /** "LEVEL" -- root command for setting the log level. */
  public static final String SAFS_TESTLOG_LEVEL    = "LEVEL";

  /** "LIST" -- Command to show the Help info in the debug console.*/
  public static final String SAFS_TESTLOG_LIST     = "LIST";

  /** "HELP" -- Command to show the Help info in the debug console.*/
  public static final String SAFS_TESTLOG_HELP     = "HELP";

  /** "-file:" -- Command-line prefix to enable output to file.*/
  public static final String SAFS_TESTLOG_FILE     = "-file:";

  /** log is suspended as long as counter > 0 */
  private static int suspendCounter = 0;

  /** "0" DEBUG log level.*/
  public static final int DEBUG   = 0;

  /** "1" INFO log level.*/
  public static final int INFO    = 1;

  /** "2" INDEX log level.*/
  public static final int INDEX   = 2;

  /** "3" GENERIC log level.*/
  public static final int GENERIC = 3;

  /** "4" PASS log level.*/
  public static final int PASS    = 4;

  /** "5" WARN log level.*/
  public static final int WARN    = 5;

  /** "6" ERROR log level.*/
  public static final int ERROR   = 6;

  static int level = GENERIC;

  protected static final String[] strlevel = new String[]{ "DEBUG",
  	                                                       "INFO",
  	                                                       "INDEX",
  	                                                       "GENERIC",
  	                                                       "PASS" ,
  	                                                       "WARN",
  	                                                       "ERROR"};

  protected static String log_processname = SAFS_TESTLOG_CLIENT;

  private static void console(String consoleMessage){
	if (console_enabled) System.out.println(consoleMessage);
  }

  /**
   * Call to make the SAFS Debug Log--normally a separate Java Process--run embedded inside
   * the test process in a separate Thread.  This call will start the new Daemon Thread and the caller
   * will return immediately.
   * @param args -- Same as for main(String[] args)--which will be invoked with a new Thread.
   */
  public static void runEmbedded(final String[] args){
	console_enabled = false;
	isEmbedded = true;
	ENABLED = true;
    initDebugLog(args);
  }
  public static boolean isEmbedded(){ return isEmbedded;}

  // -1 means no match; 0-6 is our level.
  /** Parse a string log level into its equivalent int log level value.
   * @return -1 if not a match, otherwise, 0 thru 6--the available log levels.*/
  protected static int getStrLevel(String slevel){
  	 int match = -1;
  	 int index = 0;
  	 if (slevel == null) return match;
  	 slevel = slevel.trim();
  	 do{
  	 	if (slevel.equalsIgnoreCase(strlevel[index])) match = index;
  	 }while((match < 0) &&(++index < strlevel.length));
  	 return match;
  }

  public static void setLogLevel (int loglevel){
  	if((loglevel >= DEBUG)&&(loglevel <= ERROR)) level = loglevel;
  }

  public static int getLogLevel(){
	  return level;
  }

  private static boolean doLogMsg = false;
  public static void setDoLogMsg (boolean _doLogMsg){ doLogMsg = _doLogMsg; }

  private static SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss.SSS");

  /** <br><em>Purpose:</em> logs using 'logmsg' if that fails, then does console.
   ** the format is [LEVEL: message], where LEVEL is one of DEBUG, INFO, INDEX, GENERIC, PASS, WARN, or ERROR.
   * @param                     local, int
   * @param                     levelMsg, String
   * @param                     msg, Object, message to send (.toString() is used)
   **/
  static void message(int local, String levelMsg, Object msg) {
    String fullmsg = "["+levelMsg+" "+ time.format(new Date())+":"+log_processname+": "+(msg==null?(String)msg:msg.toString())+" ]";
    if (!logmsg(local, fullmsg)) {
      if (local >= level) {
       console(fullmsg);
      }
    }
  }


  /** <br><em>Purpose:</em> logs using 'logmsg' if that fails, then does console.
   ** the format is [LEVEL: message], where LEVEL is one of DEBUG, INFO, INDEX, GENERIC, PASS, WARN, or ERROR.
   * @param                     local, int
   * @param                     msg, Object, message to send (.toString() is used)
   **/
  static public void message(int local, Object msg) {
  	if (!ENABLED) return;
    String levelMsg="??";
    switch(local) {
      case DEBUG:
        levelMsg = strlevel[0];
        break;
      case INFO:
        levelMsg = strlevel[1];
        break;
      case INDEX:
        levelMsg = strlevel[2];
        break;
      case GENERIC:
        levelMsg = strlevel[3];
        break;
      case PASS:
        levelMsg = strlevel[4];
        break;
      case WARN:
        levelMsg = strlevel[5];
        break;
      case ERROR:
        levelMsg = strlevel[6];
        break;
    }
    message(local, levelMsg, msg);
  }

  static public boolean suspend(){
	  ENABLED = false;
	  if(isEmbedded) {
		  doSuspend();
		  return true;
	  }
	  if(helper != null) return helper.sendQueueMessage(SAFS_TESTLOG_PROCESS, SAFS_TESTLOG_SUSPEND);
	  return false;
  }
  private static void doSuspend(){
	  suspendCounter++;
      isSuspended = true;
      if(suspendCounter == 1){ // first time
          console(SAFS_TESTLOG_SUSPEND);
          if (writer != null) {
          	try{ writer.write(SAFS_TESTLOG_SUSPEND); writer.newLine();}
          	catch(IOException iox){;}
          }
      }
  }

  static public boolean resume(){
	  if(isEmbedded) {
		  doResume();
		  ENABLED = !isSuspended;
		  return true;
	  }
	  ENABLED = true;
	  if(helper != null) return helper.sendQueueMessage(SAFS_TESTLOG_PROCESS, SAFS_TESTLOG_RESUME);
	  return false;
  }
  private static void doResume(){
	  if(--suspendCounter < 0) {
		  suspendCounter = 0;
	  }else{
          isSuspended = (suspendCounter > 0);
          if(!isSuspended){ // all suspensions lifted
              console(SAFS_TESTLOG_RESUME);
              if (writer != null) {
              	try{ writer.write(SAFS_TESTLOG_RESUME); writer.newLine();}
              	catch(IOException iox){;}
              }
          }
	  }
  }


  static public void debug(Object msg) {
  	if (!ENABLED) return;
    message(DEBUG, strlevel[0], msg);
  }
  static public void info(Object msg) {
  	if (!ENABLED) return;
    message(INFO, strlevel[1], msg);
  }
  static public void index(Object msg) {
  	if (!ENABLED) return;
    message(INDEX, strlevel[2], msg);
  }
  static public void generic(Object msg) {
  	if (!ENABLED) return;
    message(GENERIC, strlevel[3], msg);
  }
  static public void pass(Object msg) {
  	if (!ENABLED) return;
    message(PASS, strlevel[4], msg);
  }
  static public void warn(Object msg) {
  	if (!ENABLED) return;
    message(WARN, strlevel[5], msg);
  }
  static public void error(Object msg) {
  	if (!ENABLED) return;
    message(ERROR, strlevel[6], msg);
  }



  static public void error(Object msg, Throwable ex) {
  	if (!ENABLED) return;
    OutputStream os = new ByteArrayOutputStream();
    ex.printStackTrace(new PrintStream(os));
    error(""+msg+",\n"+os.toString());
    try {os.close();} catch (IOException io){}
  }

  static public void info(Object msg, Throwable ex) {
  	if (!ENABLED) return;
    OutputStream os = new ByteArrayOutputStream();
    ex.printStackTrace(new PrintStream(os));
    info(""+msg+",\n"+os.toString());
    try {os.close();} catch (IOException io){}
  }

  static public void debug(Object msg, Throwable ex) {
  	if (!ENABLED) return;
    OutputStream os = new ByteArrayOutputStream();
    ex.printStackTrace(new PrintStream(os));
    debug(""+msg+",\n"+os.toString());
    try {os.close();} catch (IOException io){}
  }

  /** Doesn't do anything unless we are running Embedded.
   * Otherwise, will shutdown the embedded debug log. */
  static public void close(){
	  if(isEmbedded) closeDebugLog();
  ;}

  /** Returns the Help text seen in the SAFS/TESTLOG console. */
  public static String getHelp(){
  	return "Send QUEUE messages to this STAF Client with:\n\n"+
  	       "staf local queue queue name safs/testlog message <command>\n\n"+
  	       "Available Commands:\n"+
  	       "===================\n"+
  	       "\"n|message to log\"  (where n = loglevel 0 thru 6)\n"+
  	       "    0 = DEBUG\n"+
  	       "    1 = INFO\n"+
  	       "    2 = INDEX\n"+
  	       "    3 = GENERIC\n"+
  	       "    4 = PASS\n"+
  	       "    5 = WARNING\n"+
  	       "    6 = ERROR\n"+
  	       "\"SHUTDOWN\"  terminate this logging process.\n"+
  	       "\"CLS\"       clear lines to show new activity\n"+
  	       "\"LEVEL0\" - \"LEVEL6\"   set the logging level\n"+
  	       "\"LEVELdebug\" - \"LEVELerror\"   set the logging level\n"+
  	       "\"LIST\"      this HELP message\n"+
  	       "\"SUSPEND\"   Suspend debug log output from all sources message\n"+
  	       "\"RESUME\"   Resume debug log output from all sources message\n"+
  	       "\"LIST\"      this HELP message\n"+
  	       "\"HELP\"      this HELP message\n";
  }

  private static BufferedWriter writer;
  private static String filepath;
  private static String slevel = null;
  private static void initDebugLog(String[] args){
    ENABLED= true;
    level = DEBUG;
    String filepath = null;
    File outFile = null;
    if ((!(args==null))&&(args.length > 0)){
    	boolean filehappy = false;

    	for (int i=0; i< args.length;i++){
	    	slevel = args[i];
	    	slevel = slevel.toLowerCase();

	    	// see if it is "-file:"
	    	if (slevel.startsWith(SAFS_TESTLOG_FILE)){
	    	    if ((slevel.length() > SAFS_TESTLOG_FILE.length()) &&
	    	       (!filehappy)){
	    		    slevel = slevel.substring(SAFS_TESTLOG_FILE.length());
	    		    //try to open the file...delete if necessary.
	    		    try{
	    		    	outFile = new CaseInsensitiveFile(slevel).toFile();
	    		    	writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile),Charset.forName("UTF-8")));
	    		    }
	    		    catch(IOException iox){
	    		    	System.err.println ("Error opening output file "+ slevel);
	    		    	System.err.println (iox.getMessage());
	    		    	writer = null;
	    		    	outFile = null;
	    		    }finally{
	    		    	if (outFile != null) filepath = outFile.getAbsolutePath();
	    		    }
	    	    }
	    	}
	    	// otherwise process as a LEVEL arg
	    	else{
		    	try{
		            Integer num = new Integer(slevel);
		            level = num.intValue();
		        } catch (Exception ee) {
		          	int ilevel = getStrLevel(slevel);
		          	if (ilevel != -1){
		          		level = ilevel;
		          	}else{
		          		System.err.println("Error parsing Debug Log command-line.!");
		          	}
		        }
	    	}
    	}
    }
  }
  private static void closeDebugLog(){
      if (writer != null){
      	  try{ writer.flush();writer.close();writer=null;}
      	  catch(IOException iox){ writer = null; }
      }
  }
  private static void clearLog(){
      console("\n\n\n\n\n\n\n\n\n\n");
      // close/reopen any active file
      if (writer != null){
      	  try{
      	  	  writer.flush();
      	  	  writer.close();
      	  	  writer = null;
      	  	  writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new CaseInsensitiveFile(slevel).toFile()),Charset.forName("UTF-8")));
      	  }
	      catch(IOException iox){
	    	  System.err.println ("Error closing/reopening output file "+ slevel);
	    	  System.err.println (iox.getMessage());
	    	  writer = null;
	      }
      }
  }
  private static void doLogLevel(String testrecord){
      try{
        Integer num = new Integer(testrecord);
        level = num.intValue();
      } catch (Exception ee) {
      	int ilevel = getStrLevel(testrecord);
      	if (ilevel != -1){
      		level = ilevel;
      	}
      }
      console("New Log Level:"+level +":"+ strlevel[level]);
  }
  private static void doMessage(String testrecord){
      int i = testrecord.indexOf("|");
      int local = DEBUG;
      if (i>=0) {
      	slevel = testrecord.substring(0, i);
        try{
          Integer num = new Integer(slevel);
          local = num.intValue();
        } catch (Exception ee) {
          int ilevel = getStrLevel(slevel);
          if (ilevel != -1) local = ilevel;
        }
        testrecord = testrecord.substring(i+1, testrecord.length());
      }
      if ((local >= level) && !isSuspended) {
        console(testrecord);
        if (writer != null) {
        	try{
        		writer.write(testrecord);
        		writer.newLine();
        		writer.flush();
        	}
        	catch(IOException iox){;}
        }
      }
  }

  /**
   * Run standalone to activate the debug console.
   * used for development/debugging log messages.
   * <p>
   * Accepts a command line argument that allows you to specify the initial log
   * level.  The format is strictly the numeric log level ("0" thru "6") or the equivalent
   * text ("debug" thru "error").
   * <p>
   * Examples:
   * <p>
   * <ul>
   * java org.safs.Log<p>
   * java org.safs.Log 1<p>
   * java org.safs.Log INFO<p>
   * </ul>
   * <p>
   * Also accepts a command line argument to write log messages to file.
   * <p>
   * Examples:
   * <p>
   * <ul>
   * java org.safs.Log -file:afilepath<p>
   * </ul>
   * <p>
   * And, of course, both command-line arguments can be used in the same call.
   **/
  public static void main (String[] args) {
    boolean shutdown = false;
    String  testrecord = null;

    initDebugLog(args);

    try{
      final STAFHelper helper =  SingletonSTAFHelper.getHelper(); // get Singleton
      helper.initialize(SAFS_TESTLOG_PROCESS);
      setHelper(helper);
      console("\n"+ getHelp() +"\n");
      console(SAFS_TESTLOG_PROCESS +" handle: " + helper.getHandleNumber());
      console("Current Log  Level: "+level +":"+ strlevel[level]);
      if(filepath != null) console("Current Log Output: "+ filepath);
      console("Waiting for message QUEUE dispatch...");
      do{
        try{
          testrecord = helper.getQueueMessage(null, null);
        }catch(SAFSException e){
          console("Error getting message from STAF QUEUE:" + e.getMessage());
          testrecord = SAFS_TESTLOG_SHUTDOWN;
        }
        // show the record
        if (testrecord.equalsIgnoreCase(SAFS_TESTLOG_SHUTDOWN)) {
              shutdown = true;
        }else if (testrecord.equalsIgnoreCase(SAFS_TESTLOG_SUSPEND)) {
              doSuspend();
        }else if (testrecord.equalsIgnoreCase(SAFS_TESTLOG_RESUME)) {
        	  doResume();
        }else if (testrecord.equalsIgnoreCase(SAFS_TESTLOG_CLS)) {
        	  clearLog();
        } else if ((testrecord.equalsIgnoreCase(SAFS_TESTLOG_LIST))||
        		   (testrecord.equalsIgnoreCase(SAFS_TESTLOG_HELP))){
              console("\n"+ getHelp() +"\n");
        } else if (testrecord.length()>SAFS_TESTLOG_LEVEL.length() &&
        		   testrecord.substring(0, SAFS_TESTLOG_LEVEL.length()).equalsIgnoreCase(SAFS_TESTLOG_LEVEL)) {
        	  doLogLevel(testrecord.substring(SAFS_TESTLOG_LEVEL.length(), testrecord.length()));
        } else { // it must be a message
        	  doMessage(testrecord);
        }
      }while(!shutdown);
      closeDebugLog();
      // don't unregister someone elses STAFHelper (should't happen here anyway?)
      if (helper.getProcessName()==SAFS_TESTLOG_PROCESS)  helper.unRegister();
    }
    catch(SAFSException e){
      console("Error talking with STAF subsystem; " + e.getMessage());
    }
    finally{
      console("Testlog shutting down "+ filepath);
    }
  }


  /**************************************************************************************
   ** <br><em>Purpose:</em>      used to do STAF calls for the queue to send/get log messages.
   ** <br><em>Initialized:</em>  to null
   ** <br><em>Re-set:</em>  by 'setHelper'
   **************************************************************************************/
  private static  STAFHelper helper = null;

  private static boolean clientTried = false;

  // APR 22, 2005 (Carl Nagle) Should not unRegister existing helper because it usually
  // comes from some other process owner.  It kills the other process owner if we
  // unRegister it out from under them.
  public static void setHelper(STAFHelper aHelper) {
  	 if (helper != null) {
  	 	//try{helper.unRegister();}catch(Exception x){;}
  	 	helper=null;
  	 	clientTried = false;
  	 }
  	 helper=aHelper;
  	 log_processname = helper.getProcessName();
  }

  /**
   * Used to preset a Log processName prior to any STAFHelper initialization.
   * @param _processName
   */
  public static void setLogProcessName(String _processName){
	  log_processname = _processName;
  }

  /*
   * Performed once per JVM at Log.class initialization
   */
  static{
  	if ((helper == null)&&(!clientTried)){
  		clientTried = true;
  		int index = 1;
  		String sindex = null;
  		boolean done = false;
  		do{
  			try{
	  			sindex = String.valueOf(index).trim();
	  			helper = new STAFHelper(SAFS_TESTLOG_CLIENT + sindex);
	  			ENABLED = helper.isToolAvailable(SAFS_TESTLOG_PROCESS);
	  			done   = true;
	  			log_processname = helper.getProcessName();
  			}
  			catch(SAFSSTAFRegistrationException x){
  				if (x.rc==STAFResult.STAFNotRunning) done = true;
  				if (x.rc==STAFResult.HandleAlreadyExists) {
  					index ++;
  				}else{
  					done = true;
  					/*
  					 * should not log this since every JVM using SAFS Agents will
  					 * issue this in a console even if we are not doing testing.
  					 *
  					 */
  					//System.err.println(SAFS_TESTLOG_CLIENT +" could not register with STAF. STAF.RC="+x.rc);
  				}
  			}
  			//STAF not even in available
  			//catch(NoClassDefFoundError e){
  			catch(Throwable e){
  				done =true;
  			}
  		}while(!done);
  	}
  }

  private static boolean logmsg(int local, String msg) {

    if (helper == null && !isEmbedded) {
      if (local >= level) {
        //console(msg);
      }
      return false;
    }
    StringBuffer buf = new StringBuffer();
    for(int i=0; i<msg.length(); i++) { // convert " to '
      if (msg.charAt(i) == '\"') buf.append("'");
      else buf.append(msg.substring(i, i+1));
    }
    msg = buf.toString();
    if (local >= GENERIC || doLogMsg) {
      try {
        helper.setVariable(SAFS_TESTLOG_VARIABLE, msg);
      } catch (Throwable se) {} // ignore
    }
    if(isEmbedded){
    	doMessage(Integer.toString(local) + "|" + msg);
    	return true;
    }else{
    	return helper.sendQueueMessage(SAFS_TESTLOG_PROCESS, Integer.toString(local) + "|" + msg);
    }
  }
}
