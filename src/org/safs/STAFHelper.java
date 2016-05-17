/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;
/**
 * For developers only, not published in Java doc
 * History:
 *
 * <br> JUN 24, 2003    (DBauman) Original Release
 * <br> JUN 26, 2003    (Carl Nagle)  Added getInitializedHelper and some constants.
 * <br> JUN 30, 2003    (BAUMAN)  Separated out Singleton code to SingletonSTAFHelper.
 * <br> JUN 30, 2003    (Carl Nagle)  Added additional convenience constructor and made the 
 *                                function lentag... public.
 * <br> AUG 11, 2003    (DBAUMAN) added a Pause component function (delay too); adding debug statements in order to figure out how to add recog. strings to the appmap for custom components.
 * <br> AUG 19, 2003    (DBAUMAN) getAppMapNames added so that we can set the test record's appmapname with the current loaded appmap.
 * <br> SEP 05, 2003    (DBAUMAN) changed the wait so that RobotJ never times out (it must be instructed to stop).
 * <br> SEP 12, 2003    (Carl Nagle)  Added SAFSLOGS service data.
 * <br> NOV 09, 2003    (Carl Nagle)  Adding support for multiple different engine hooks.
 * <br> NOV 10, 2003    (Carl Nagle)  Capture process/hook name used to create an instance.
 * <br> APR 06, 2004    (Carl Nagle)  Corrected problems with SAFSLOGS service not being recognized.
 * <br> JUN 07, 2004    (Carl Nagle)  Added isServiceAvailable to compliment isToolAvailable.
 * <br> JUN 17, 2004    (Carl Nagle)  Added SAFSINPUT as a predefined tool.
 * <br> JUN 28, 2004    (Carl Nagle)  Added postNextTestEvent to compliment getNextTestEvent.
 * <br>                           Added set\getSAFSTestRecordData.
 * <br> OCT 31, 2004    (Carl Nagle)  Catch infinite loop in sendQueueMessage.
 * <br> DEC 07, 2004    (Carl Nagle)  Fixed setHookTestResult and added Results event timeout.
 * <br> APR 22, 2005    (Carl Nagle)  Enhanced ordering of initialize.
 * <br> MAY 11, 2005    (Carl Nagle)  Added missing SEM Event "Shutdown" in resetHookEvents.
 * <br> JUL 25, 2005    (Carl Nagle)  Changed Results event timeout from 3 seconds to 12 seconds.
 * <br> JUL 27, 2005    (Carl Nagle)  Added submit2WithVar for STAF submits writing results to SAFSVARS.
 * <br> NOV 15, 2005    (Bob Lawler)  Added support for new TRD statusinfo field (RJL).
 * <br> AUG 04, 2006    (Carl Nagle)  setVariable and getAppMapItem modified to accept legal null or empty values.
 * <br> MAY 18, 2009    (JunwuMa) Adding support for STAF3. 
 * <br> JUN 12, 2009    (JunwuMa) Added methods to centralize staf.submit and staf.sbumit2 calls into this class.
 * 								  Added method submit2ForFormatUnchangedService() for using only to call the unchangeable format services.
 *								  Lower the accessibility of submit() and submit2() from public to protected. 
 * <br> JUN 15, 2009    (Carl Nagle)  Fixed QUEUE message service STAFExceptions with STAF V3. 
 * <br> JUN 16, 2009    (LeiWang) Modify method sendQueueMessage(): If STAFException occur, try again.
 * <br> JUL 28, 2009    (Carl Nagle)  Added some waits\checks for shutdown operations. 
 * <br> SEP 11, 2009    (Carl Nagle)  Resolving some synch issues between driver and engines. 
 * <br> AUG 23, 2010    (Carl Nagle)  Add waitForWaiters to fix Driver\Engine EVENT synchronization. 
 * <br> APR 01, 2011 	(Dharmesh4) Modify JSTAF add service options para.
 * <br> JUL 01, 2011    (Carl Nagle)  Adding generic SAFSLOGS logMessage support.
 * <br> MAY 03, 2012    (Lei Wang)  Modify to adjust the backward compatibility of STAF.
 *                                Add method to check the condition of resetting "shutdown" semaphore.
 * <br> JUL 19, 2012    (Carl Nagle)  Adding more public constants.
 * <br> SEP 11, 2013    (Carl Nagle)  Added removeShutdownHook to prevent premature release of handle when necessary.
 * <br> JUL 15, 2014    (Carl Nagle)  Added support for STAFHandle registration to optionally NOT use STAF.
 * <br> JUL 01, 2015    (LeiWang) Added startProcess(): can start process on any host trusting us.
 * <br> AUG 20, 2015    (Carl Nagle)  Added INI STAF:EmbedDebugMainClass True|False support and 
 *                                -Dtestdesigner.debuglogname support for embedded debug log filenames.
 **/
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.safs.staf.AbstractSTAFHelperCompatible;
import org.safs.staf.STAFHandleInterface;
import org.safs.staf.STAFHelperCompatibleInterface;
import org.safs.staf.embedded.EmbeddedHandle;
import org.safs.staf.embedded.EmbeddedHandles;
import org.safs.staf.embedded.HandleInterface;
import org.safs.staf.service.InfoInterface;
import org.safs.staf.service.queue.EmbeddedQueueService;
import org.safs.staf.service.sem.EmbeddedSemService;
import org.safs.tools.CoreInterface;
import org.safs.tools.MainClass;
import org.safs.tools.drivers.ConfigureInterface;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.stringutils.StringUtilities;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFQueueMessage;
import com.ibm.staf.STAFResult;

/**
 * <br><em>Purpose:</em> a wrapper class for the STAFHandle, includes some common
 * functionality used many places associated with the SAFSVARS and SAFSMAPS
 *
 * @author  Doug Bauman
 * @since   JUN 24, 2003
 **/
public class STAFHelper implements CoreInterface{


  public static final String STAF_DELAY_SERVICE = "delay";
  public static final String STAF_GLOBALVARS_VERSION = "STAF/Version"; 
  
  /** "STAF/Service/STAF/ServiceLoader" **/
  public static final String STAF_SERVICELOADER_V2 = "STAF/Service/STAF/ServiceLoader";
  /** "STAF/Service/STAFServiceLoader" **/
  public static final String STAF_SERVICELOADER_V3 = "STAF/Service/STAFServiceLoader";
  
  /** "SAFSVariableService" **/
  public static final String SAFS_SAFSVARS_PROCESS  = "SAFSVariableService";
  /** "SAFSAppMapService" **/
  public static final String SAFS_SAFSMAPS_PROCESS  = "SAFSAppMapService";
  /** "SAFSInputService" **/
  public static final String SAFS_INPUT_PROCESS     = "SAFSInputService";
  /** "STAF/Service/SAFSLOGS" **/
  public static final String SAFS_SAFSLOGS_PROCESS  = "STAF/Service/SAFSLOGS";

  /** "SAFS/Process" **/
  public static final String SAFS_GENERIC_PROCESS   = "SAFS/Process";
  /** "SAFS/ProcessID" **/
  public static final String SAFS_GENERIC_PROCESS_ID= "SAFS/ProcessID";
  /** "SAFS/RobotJ" **/
  public static final String SAFS_ROBOTJ_PROCESS    = "SAFS/RobotJ";
  /** "SAFS/RobotJID" **/
  public static final String SAFS_ROBOTJ_PROCESS_ID = "SAFS/RobotJID";
  /** "SAFS/RobotClassic" **/
  public static final String SAFS_CLASSIC_PROCESS   = "SAFS/RobotClassic";
  /** "SAFS/RobotClassicID" **/
  public static final String SAFS_CLASSIC_PROCESS_ID= "SAFS/RobotClassicID";
  /** "SAFS/Abbot" **/
  public static final String SAFS_ABBOT_PROCESS    = "SAFS/Abbot";
  /** "SAFS/AbbotID" **/
  public static final String SAFS_ABBOT_PROCESS_ID = "SAFS/AbbotID";

  /** "SAFSVARS" **/
  public static final String SAFS_VARIABLE_SERVICE  = "SAFSVARS";
  /** "SAFSMAPS" **/
  public static final String SAFS_APPMAP_SERVICE    = "SAFSMAPS";
  /** "SAFSLOGS" **/
  public static final String SAFS_LOGGING_SERVICE     = "SAFSLOGS";
  public static final String SAFS_LOGGINGLOG_SERVICE  = "SAFSLOGSLOG";
  /** "SAFSINPUT" **/
  public static final String SAFS_INPUT_SERVICE    = "SAFSINPUT";

  public static final String STAF_VARIABLE_SERVICE = "VAR";

  /** "SAFS/HOOK/" trd root **/
  public static final String SAFS_HOOK_TRD        = "SAFS/Hook/";
  /** "SAFS/CYCLE/" trd root **/
  public static final String SAFS_CYCLE_TRD       = "SAFS/Cycle/";
  /** "SAFS/SUITE/" trd root **/
  public static final String SAFS_SUITE_TRD       = "SAFS/Suite/";
  /** "SAFS/STEP/" trd root **/
  public static final String SAFS_STEP_TRD        = "SAFS/Step/";
  
  /** ".../inputrecord" **/
  public static final String SAFS_VAR_INPUTRECORD = "inputrecord";
  /** ".../statuscode" **/
  public static final String SAFS_VAR_STATUSCODE  = "statuscode";
  /** ".../statusinfo" **/
  public static final String SAFS_VAR_STATUSINFO  = "statusinfo";
  /** ".../filename" **/
  public static final String SAFS_VAR_FILENAME    = "filename";
  /** ".../linenumber" **/
  public static final String SAFS_VAR_LINENUMBER  = "linenumber";
  /** ".../separator" **/
  public static final String SAFS_VAR_SEPARATOR   = "separator";
  /** ".../fac" **/
  public static final String SAFS_VAR_FAC         = "fac";
  /** ".../testlevel" **/
  public static final String SAFS_VAR_TESTLEVEL   = "testlevel";
  /** ".../appmapname" **/
  public static final String SAFS_VAR_APPMAPNAME  = "appmapname";

  /** 7 initial preset variables **/
  public static final String SAFS_VAR_BENCHDIRECTORY    = "safsbenchdirectory";
  public static final String SAFS_VAR_DATAPOOLDIRECTORY = "safsdatapooldirectory";
  public static final String SAFS_VAR_DIFDIRECTORY      = "safsdifdirectory";
  public static final String SAFS_VAR_LOGSDIRECTORY     = "safslogsdirectory";
  public static final String SAFS_VAR_PROJECTDIRECTORY  = "safsprojectdirectory";
  public static final String SAFS_VAR_TESTDIRECTORY     = "safstestdirectory";
  public static final String SAFS_VAR_SYSTEMUSERID      = "safssystemuserid";
  public static final String SAFS_VAR_SECSWAITFORWINDOW     = "safs.secswaitforwindow";
  public static final String SAFS_VAR_SECSWAITFORCOMPONENT  = "safs.secswaitforcomponent";
  public static final String SAFS_VAR_COMMANDLINEBREAKPOINT = "safs.commandlinebreakpoint";
  public static final String SAFS_VAR_SAFSACTIVECYCLE       = "safsactivecycle";
  public static final String SAFS_VAR_SAFSACTIVESUITE       = "safsactivesuite";
  public static final String SAFS_VAR_SAFSACTIVESTEP        = "safsactivestep";

  public static final String SAFS_VAR_ROOTVERIFYDIRECTORY   = "safsrootverifydirectory";
  public static final String SAFS_VAR_RUNTIMEREPOSITORY     = "safsruntimerepository";
 
  public static final String SAFS_VAR_GLOBAL_LAST_LOG_MSG     = "safs.global.last.log.msg";
  public static final String SAFS_VAR_GLOBAL_LAST_LOG_DESC     = "safs.global.last.log.desc";
  public static final String SAFS_VAR_GLOBAL_LAST_LOG_TYPE     = "safs.global.last.log.type";
  
  /**
   * "SAFS/Hook/TRD",
   * Mutex for ALL applications and processes wishing to use SAFS/Hook/TRD data **/
  public static final String SAFS_HOOK_TRD_MUTEX    = "SAFS/Hook/TRD";
  
  /** "SAFS/RobotJStart" **/
  public static final String ROBOTJ_EVENT_START     = "SAFS/RobotJStart";
  /** "SAFS/RobotJReady" **/
  public static final String ROBOTJ_EVENT_READY     = "SAFS/RobotJReady";
  /** "SAFS/RobotJDispatch" **/
  public static final String ROBOTJ_EVENT_DISPATCH  = "SAFS/RobotJDispatch";
  /** "SAFS/RobotJRunning" **/
  public static final String ROBOTJ_EVENT_RUNNING   = "SAFS/RobotJRunning";
  /** "SAFS/RobotJResults" **/
  public static final String ROBOTJ_EVENT_RESULTS   = "SAFS/RobotJResults";
  /** "SAFS/RobotJDone" **/
  public static final String ROBOTJ_EVENT_DONE      = "SAFS/RobotJDone";
  /** "SAFS/RobotJShutdown" **/
  public static final String ROBOTJ_EVENT_SHUTDOWN  = "SAFS/RobotJShutdown";

  /** "Start" **/
  public static final String SAFS_EVENT_START     = "Start";
  /** "Ready" **/
  public static final String SAFS_EVENT_READY     = "Ready";
  /** "Dispatch" **/
  public static final String SAFS_EVENT_DISPATCH  = "Dispatch";
  /** "Running" **/
  public static final String SAFS_EVENT_RUNNING   = "Running";
  /** "Results" **/
  public static final String SAFS_EVENT_RESULTS   = "Results";
  /** "Done" **/
  public static final String SAFS_EVENT_DONE      = "Done";
  /** "Shutdown" **/
  public static final String SAFS_EVENT_SHUTDOWN  = "Shutdown";
  
  /**
   * Set to true to bypass the registering of STAF Handles and STAF altogether.
   */
  public static boolean no_staf_handles = false;
  /**
   * Set to true to embed non-STAF SEM service.
   */
  public static boolean embedSEM = false;
  private static boolean embeddedSEMTried = false;
  
  public static boolean embedQUEUE = false;
  private static boolean embeddedQUEUETried = false;

  private static boolean embeddedDebug = false;
  private static boolean embeddedDebugTried = false;
  
  public static EmbeddedSemService sem = null;
  public static EmbeddedQueueService queue = null;

  /**
   * Evaluate [STAF] to determine if any or all Embeddable Services should be launched.<br>
   * Currently only the EmbeddedSemService and EmbeddedQueue services are handled.
   * <p>
   * We also will launch an Embedded Debug Log if the System properties or INI file are configured.
   */
  public static void configEmbeddedServices(ConfigureInterface config){
	String value = null;
	if(!embeddedDebugTried && ! embeddedSEMTried && ! embeddedQUEUETried){
		System.out.println("STAFHelper.configEmbeddedServices() checking config for embeddable services...");  
	    Log.info("STAFHelper.configEmbeddedServices() checking config for embeddable services...");
	}
	if(!embeddedDebug && !embeddedDebugTried){
		embeddedDebugTried = true;
		if(!no_staf_handles){
			value = config.getNamedValue(DriverConstant.SECTION_STAF, "NOSTAF");
			if(value != null) no_staf_handles = StringUtilities.convertBool(value);
		}
		if(no_staf_handles){
			boolean useMainClass = false;
			value = System.getProperty("testdesigner.debuglogname");
			if(value == null) {
				value = config.getNamedValue(DriverConstant.SECTION_STAF, "EMBEDDEBUG");
				String prefix = config.getNamedValue(DriverConstant.SECTION_STAF, "EMBEDDEBUGMAINCLASS");
				useMainClass = prefix == null ? false: StringUtilities.convertBool(prefix);
			}
			
			if(value != null && value.length()> 4){
				if(useMainClass){
					String mainclass = MainClass.getMainClass() == null ? 
							           "": MainClass.getMainClass();
					if(mainclass.lastIndexOf('.')> 0) 
						mainclass = mainclass.substring(mainclass.lastIndexOf('.')+1);
					
					if(mainclass.length() > 0) value = mainclass + value;
				}
				Log.runEmbedded(new String[]{"-file:"+value});
				System.out.println("STAFHelper.configEmbeddedServices() Embedded Debug Log: "+ value);
				embeddedDebug = true;
			}else{
				System.out.println("STAFHelper.configEmbeddedServices() Embedded Debug Logging is NOT enabled!");
			}
		}
	}
  	// check if embedded SEM and QUEUE are already enabled
  	if(sem instanceof EmbeddedSemService) {
  		return;
  	}
  	if(!embeddedSEMTried){
  		embeddedSEMTried = true;
		value = config.getNamedValue(DriverConstant.SECTION_STAF, "EMBEDSEM");
		if(value != null) embedSEM = StringUtilities.convertBool(value);
		if(no_staf_handles) embedSEM = true;
		if(embedSEM){
			System.out.println("STAFHelper.configEmbeddedServices() starting EmbeddedSEMService...");
			Log.info("STAFHelper.configEmbeddedServices() EmbeddedSemService is enabled!");
			Log.info("STAFHelper.configEmbeddedServices() launching EmbeddedSemService.");
			sem = new EmbeddedSemService();
			sem.init(new InfoInterface.InitInfo(EmbeddedSemService.servicename, null));
		}else{
			System.out.println("STAFHelper.configEmbeddedServices() EmbeddedSemService is NOT enabled!");
			Log.info("STAFHelper.configEmbeddedServices() EmbeddedSemService is NOT enabled!");
		}
  	}
	
  	if(!embeddedQUEUETried){
  		embeddedQUEUETried = true;
		value = config.getNamedValue(DriverConstant.SECTION_STAF, "EMBEDQUEUE");
		if(value != null) embedQUEUE = StringUtilities.convertBool(value);
		if(no_staf_handles) embedQUEUE = true;
		if(embedQUEUE){
			System.out.println("STAFHelper.configEmbeddedServices() starting EmbeddedQueueService...");
			Log.info("STAFHelper.configEmbeddedServices() EmbeddedQUEUEService is enabled!");
			Log.info("STAFHelper.configEmbeddedServices() launching EmbeddedQUEUEService.");
			startEmbeddedQueueService();
		}else{
			if(queue instanceof EmbeddedQueueService) {
				System.out.println("STAFHelper.configEmbeddedServices() EmbeddedQueueService already enabled!");
				Log.info("STAFHelper.configEmbeddedServices() EmbeddedQUEUEService is already enabled!");
			}else{
				System.out.println("STAFHelper.configEmbeddedServices() EmbeddedQueueService is NOT enabled!");
				Log.info("STAFHelper.configEmbeddedServices() EmbeddedQUEUService is NOT enabled!");
			}
		}	
  	}
  }
  
  /**Start the embedded Queue Service.*/
  public static void startEmbeddedQueueService(){
	  // check if embedded QUEUE is already enabled
	  if(queue instanceof EmbeddedQueueService) {
		  System.out.println("STAFHelper.configEmbeddedServices() EmbeddedQueueService already enabled!");
		  return;
	  }
	  System.out.println("STAFHelper.configEmbeddedServices() EmbeddedQueueService is enabled!");
	  System.out.println("STAFHelper.configEmbeddedServices() launching EmbeddedQueueService.");
	  queue = new EmbeddedQueueService();
	  queue.init(new InfoInterface.InitInfo(EmbeddedQueueService.DEFAULT_SERVICE_NAME, null));
  }
  
  /**
   * Currently only the EmbeddedSemService is handled.
   */
  public static void shutdownEmbeddedServices(){
	System.out.println("STAFHelper.shutdownEmbeddedServices() terminating embedded services...");    	
	Log.info("STAFHelper.shutdownEmbeddedServices() terminating embedded services...");    	
  	if(sem instanceof EmbeddedSemService) {
		sem.terminate();
		sem = null;
  	}
  	if(queue instanceof EmbeddedQueueService) {
  		queue.terminate();
  		queue = null;
  	}
  	if(embeddedDebug){
		Log.close();
  		embeddedDebug = false;
  	}
  }
  
  private final Thread SHUTDOWN_THREAD = new Thread(){
	  @Override
	  public void run(){
          try {unRegister();} catch(SAFSException se){}
	  }
  };

  /**
   * Only valid AFTER initialization and the automatic registration of the SHUTDOWN_THREAD.
   * The internal shutdown thread only unregisters and nulls the instance handle.
   * @return
   */
  public boolean removeShutdownHook(){
	  return Runtime.getRuntime().removeShutdownHook(SHUTDOWN_THREAD);
  }
  
  /**
   * Only valid AFTER initialization.  External classes that might have called removeShutdownHook() 
   * might want to reinstate the shutdown hook at a later time.  However, this is only valid while 
   * the JVM has not already initiated JVM shutdown and the invocation of registered JVM shutdown hooks.
   * The internal shutdown thread only unregisters and nulls the instance handle to STAF.
   */
  public void reinstateShutdownHook(){
	    try{
		    Runtime.getRuntime().addShutdownHook(SHUTDOWN_THREAD);
	    }catch(IllegalStateException ignore){
	    	// cannot addShutdownHook if shutdown is in progress!
	    }catch(IllegalArgumentException ignore){
	    	// cannot addShutdownHook if it is already registered
	    }catch(Exception e){
	    	System.out.println(e.getClass().getSimpleName()+"; "+ e.getMessage());
	    	e.printStackTrace();
	    	// java.policy?    	
	    }    
  }
  
  /** we wrap a STAFHandle instance called 'handle' **/
  private HandleInterface handle;
  
  
  private static int 	STAFVersion = 0;
  private static String	STAFVersionString = "";
  
  /** we wrap a STAFHandle instance called 'handle' **/
  private String process_name;
  /** the String name of the process/hook used at construction **/
  public String getProcessName(){ return process_name;}

  /** get the handle.  hopefull this won't be needed if all of the methods here do the work **/
  public HandleInterface getHandle(){ return handle;}
  /** get the handle's number **/
  public int getHandleNumber () {return handle.getHandle();}

  /** the normal "local" machine setting used by STAF **/
  public static final String LOCAL_MACHINE = "local";
  /** machine which is normally "local" **/
  private String machine = LOCAL_MACHINE;
  /** get the current value of 'machine' **/
  public String getMachine () {return machine;}
  /** set the machine to the specified parameter **/
  public void setMachine (String machine) {this.machine = machine;}
  /** reset the machine back to "local" **/
  public void setLocalMachine () {this.machine = LOCAL_MACHINE;}

  public static Class STAFQueueMessageClass = STAFQueueMessage.class;
  //handling unfortunate API change between STAF V2 and V3. String changed to Object.
  public static Field getQueueMessageField = null;
  static{
	  try{ getQueueMessageField = STAFQueueMessageClass.getField("message");}
	  catch(NoSuchFieldException x){
		  System.err.println("*** STAFQueueMessage.message Field cannot be determined! ***");
	  }
  }
  
  /** Used to adjust the backward compatibility of STAF 2.6.11
   * Before using it, please check if it is null*/
  protected STAFHelperCompatibleInterface stafCompatible = null;
  
  /** <br><em>Purpose:</em> constructor, must
   ** be 'initialized' by the user.
   **/
  public STAFHelper () {
  }

  /** <br><em>Purpose:</em> constructor that also initializes with the given processName.
   **/
  public STAFHelper (String processName) throws SAFSSTAFRegistrationException{
    initialize(processName);
  }

  /** <br><em>Purpose:</em> creates the STAFHandle
   * @param                     hookName, String
   * @exception                 SAFSSTAFRegistrationException (wraps the message from STAFException)
   **/
  public void initialize (String hookName) throws SAFSSTAFRegistrationException {
	  process_name = hookName;
	  try{ reinstateShutdownHook();}
	  // thrown when this JVM is already in the process of shutting down
	  catch(IllegalStateException is){
	      process_name = null;
	      handle = null;
	      throw new SAFSSTAFRegistrationException("STAFHelper.initialize("+hookName+"): "+is.getMessage());
	  }
      try {
    	  if(no_staf_handles){
    		  STAFVersion = 3; //default
	          stafCompatible = AbstractSTAFHelperCompatible.getCompatibleSTAF(STAFVersion);
	          try{ 
	        	  handle = new EmbeddedHandle(process_name);
	        	  handle.register();
	          }
	          catch(STAFException x){
	        	  Log.warn("STAFHelper.initialize(): ignoring EmbeddedHandle registration "+ x.getClass().getName()+", "+x.getMessage()+": rc="+ x.rc);
	          }
    	  }else{
	          handle = new STAFHandleInterface(hookName);
	          STAFVersion = getSTAFVersion();
	          stafCompatible = AbstractSTAFHelperCompatible.getCompatibleSTAF(STAFVersion);
    	  }
          if(stafCompatible==null){
        	  Log.warn("STAFHelper.initialize(): Can't get instance of STAFHelperCompatibleInterface.");
          }else{
    	      stafCompatible.setSTAFHelper(this);
          }
      } 
      catch (STAFException e) {
    	  String msg = "STAFHelper.initialize() "+ e.getClass().getName()+", rc: "+ e.rc +", "+ e.getMessage();
    	  try{ Log.warn(msg); }catch(Throwable t){}
    	  if(!no_staf_handles) System.out.println(msg);
	      process_name = null;
	      handle = null;
	      throw new SAFSSTAFRegistrationException(e.rc, "STAFHelper.initialize("+hookName+"), rc:"+e.rc);
      }
  }

  /** have we been initialized (initialized, but not unRegistered) **/
  public boolean isInitialized () {
    return handle != null;
  }

  /** unregister, passed along to 'handle' **/
  public void unRegister () throws SAFSException {
    if (handle != null) {
      try {
        handle.unRegister();
        handle = null;
      } catch (STAFException e) {
        handle = null;
        throw new SAFSException("unRegister: when doing handle.unRegister(), rc:"+e.rc);
      }
    }
  }
  /** <br><em>Purpose:</em> passed on to handle.submit2
   *  Used to call the services with unchangeable format. Usually for SAFSVARS, SAFSMAP, SAFSINPUT and SAFSLOG. 
   *  
   * @param                     where, String
   * @param                     name, String
   * @param                     command, String
   * @return                    STAFResult
   **/
  public STAFResult submit2ForFormatUnchangedService (String where, String name, String command) {
    return submit2(where, name, command);
  }
  /** <br><em>Purpose:</em> passed on to handle.submit2
   * @param                     where, String
   * @param                     name, String
   * @param                     command, String
   * @return                    STAFResult
   **/
  protected STAFResult submit2 (String where, String name, String command) {
	  if(EmbeddedHandles.isServiceRunning(name)){
		  try{ return EmbeddedHandles.getService(name).acceptRequest(new InfoInterface.RequestInfo(where, handle.getHandle(), process_name, command));}
	      catch(Exception x){
	    	  x.printStackTrace();
	      }
	  }
      return handle.submit2(where, name, command);
  }


  /** <br><em>Purpose:</em> submit to STAF and record varName.rc and varname.result 
   * into variable storage (SAFSVARS).  If an error occurs when trying to write the 
   * varName values STAFResult will return with result.rc=47 and result.result=varName.
   * Otherwise we return the STAFResult from the STAF call itself.
   * @param 					sysName, String
   * @param 					srvName, String
   * @param 					cmdName, String
   * @param 					varName, String
   * @return					STAFResult
   */ 
  public STAFResult submit2WithVar (String sysName, String srvName, String cmdName, String varName) throws STAFException, SAFSException {
  	STAFResult result = submit2(sysName, srvName, cmdName);

  	if (!setVariable(varName+".rc", Integer.toString(result.rc))){
  		result.rc = 47;
  		result.result = varName;
  		return result;
  	}
  	if(!setVariable(varName+".result", result.result)) {
  		result.rc = 47;
  		result.result = varName;
  	}
  	return result;
  }
  
  	

  /** <br><em>Purpose:</em> passed on to handle.submit
   * @param                     where, String
   * @param                     name, String
   * @param                     command, String
   * @return                    String, from STAF 3.3, the result is marshaled
   * @exception                 STAFException
   * 
   * @see {@link #getUnMarshallResult(String)}
   * @see #getUnMarshallStringResult(String)
   **/
  public String submit (String where, String name, String command) throws STAFException {
	  if(EmbeddedHandles.isServiceRunning(name)){
		  try{
			  STAFResult sr = EmbeddedHandles.getService(name).acceptRequest(
					  new InfoInterface.RequestInfo(where, handle.getHandle(), process_name, command));
			  if(sr.rc == STAFResult.Ok) return sr.result;
			  throw new STAFException(sr.rc, sr.result);
		  }catch(STAFException rethrow){
			  throw rethrow;
		  }catch(Exception x){
			  throw new STAFException(STAFResult.UnknownError, x.getClass().getName()+": "+ x.getMessage());
		  }
	  }
    return handle.submit(where, name, command);
  }
  
  /** <br><em>Purpose:</em> return the value in STAF lentag format,   :len:val
   * @param                     val, String
   * @return                    :val.length():val
   * 
   * @author Carl Nagle JUN 30, 2003  Made PUBLIC as external users will also need to 
   *                                  length tag there command strings too.
   **/
  public static String lentagValue(String val) {
    if (val.length() == 0) return "\"" + val + "\"";
    //val should not contain any space at the beginning, it will cause error
//   val = val.trim();
    return ":"+Integer.toString(val.length()).trim() +":"+ val;
  }

  /** <br><em>Purpose:</em> get collection of appmap names
   * <br><em>Assumptions:</em>  output of this command: <br>
   * staf local safsmaps list <br>
   * looks like: <br>
   * safsmaps:LIST:2<br>
   * c:\cycletest\datapool\dealapp.map:C:\CycleTest\Datapool\DealApp.MAP<br>
   * deal:C:\CycleTest\Datapool\DealApp.map<br>
   * @return                    Collection
   **/
  public Collection getAppMapNames () throws SAFSException {
    String service = SAFS_APPMAP_SERVICE;
    String command = "LIST";
    // commands for STAF V2 and V3 are same, the formats of the strings returned are also same 
    try {
      String input = submit(machine, service, command);
      StringTokenizer st = new SAFSStringTokenizer(input, " \n");
      String numtok = st.nextToken();
      StringTokenizer stn = new SAFSStringTokenizer(numtok, ":");
      stn.nextToken(); // ignore first, it should be 'safsmaps'
      stn.nextToken(); // ignore second, it should be 'LIST'
      String nt = stn.nextToken(); // should be an int representing the number of tokens.
      Integer num= new Integer(nt);
      Collection tokens = new LinkedList();
      for (int j=0; st.hasMoreTokens() && j<num.intValue(); j++) {
        String n = st.nextToken();
        String o = null;
        String p = null;
        String q = null;
        try {
          stn = new SAFSStringTokenizer(n, ":");
          n = stn.nextToken();
          o = stn.nextToken();
          p = stn.nextToken();
          q = stn.nextToken();
        } catch (Exception ee) {}
        //System.out.println("n: "+n+", o: "+o+", p: "+p+", q: "+q);
        if (q!=null) {n = n + ":" + o;}
        //System.out.println("NEW n: "+n);
        tokens.add(n);
      }
      return tokens;
    } catch (STAFException e) {
      throw new SAFSException(getClass().getName(), "getAppMapName ",
                              "rc: "+e.rc+ ", command: "+command);
    } catch (RuntimeException re) {
      re.printStackTrace();
      throw new SAFSException(getClass().getName(), "getAppMapName ",
                              " command: "+command+ ", e: "+re);
    }
  }

  /**
   * This method will check only the {@link #SAFS_EVENT_SHUTDOWN} is "posted"<br>
   * and other events in {@link #eventsToReset} are "reset".<br>
   * 
   * @param servicename
   * @return
   * @throws SAFSException
   */
  public boolean isOnlyShutdownPosted(String servicename) throws SAFSException {
	String stateReset = "Reset";
	String statePosted = "Posted";
	boolean serviceIsShutdown = false;

	try {
		List<String> serviceEvents = getServiceEvents(servicename);
		String state = stateReset;
		int i = 0;
		
		if(serviceEvents.size() >= eventsToReset.length){
			for(String event: eventsToReset){
				state = SAFS_EVENT_SHUTDOWN.equals(event)? statePosted:stateReset;
				for(String serviceevent: serviceEvents){
					if(serviceevent.indexOf(event)!=-1 && 
					   serviceevent.indexOf(state)!=-1 &&
					   serviceevent.indexOf(state)>serviceevent.indexOf(event)){
						i++;
						break;
					}
				}
			}
		}else{
			Log.debug("isServiceShutdown(): has not found enough event.");
		}
		
		serviceIsShutdown = (i==eventsToReset.length);
	} catch (RuntimeException re) {
		re.printStackTrace();
		throw new SAFSException(getClass().getName(), "isServiceShutdown() Exception="+re.getMessage());
	}
	
	return serviceIsShutdown;
  }
  
  /**
   * Use the embedded {@link #stafCompatible} to get all events related to service name.
   * @param servicename
   * @return
   * @throws SAFSException
   */
  public List<String> getServiceEvents(String servicename) throws SAFSException{
	  try{
		  return stafCompatible.getServiceEvents(machine, servicename);
	  }catch (RuntimeException re) {
			re.printStackTrace();
			throw new SAFSException(getClass().getName(), ".getServiceEvents(): Exception="+re.getMessage());
	  }
  }
  
  /**
   * Use the embedded {@link #stafCompatible} to get all running engine names.
   * @return Collection (Vector) of String engine names( ex: SAFS/RobotJ, SAFS/SELENIUM, etc..)
   * @throws SAFSException
   */
  public Collection getRunningEngineNames () throws SAFSException {
	  try{
		  return stafCompatible.getRunningEngineNames(machine);
	  }catch (RuntimeException re) {
			re.printStackTrace();
			throw new SAFSException(getClass().getName(), ".getRunningEngineNames(): Exception="+re.getMessage());
	  }
  }

  public String getLogName () throws SAFSException {
    String service = SAFS_LOGGING_SERVICE;
    String command = "LIST";
    // commands for STAF V2 and V3 are same, the formats of the strings returned are also same
    try {
      String result = submit(machine, service, command);
      return result;
    } catch (STAFException e) {
      throw new SAFSException(getClass().getName(), "getLogName ",
                              "rc: "+e.rc+ ", command: "+command);
    }
  }

  /** <br><em>Purpose:</em> getVariable: get a variable from safsvars service <br>
   * @param                     var, String, name of variable to get value for
   * @return                    the value in the VAR
   * @exception                 SAFSException, if not ok
   **/
  public String getVariable (String var) throws SAFSException {
    String service = SAFS_VARIABLE_SERVICE;
    String getCommand = "GET " + lentagValue(var);
    try {
      String result = submit(machine, service, getCommand);
      return result;
    } catch (STAFException e) {
      throw new SAFSException(getClass().getName(), "getVariable",
                              "rc: "+e.rc+ ", variable: "+ var+", getCommand: "+getCommand);
    }
  }

  /**
   * Send a generic message to a running SAFSLOGS log.<br>
   * If the optional facname is not provided, we will route the message to the first (or only) 
   * log reported as running in SAFSLOGS.
   * <p>
   * @param facname (Optional) Name of running log to receive message.  
   * If facname is null or empty, we will send the message to the the first (or only) 
   * log reported as running in SAFSLOGS.
   * @param message to be sent to the log.
   * @throws SAFSException if there is a STAF submission error or response of any kind.
   */  
  public void logMessage(String facname, String message) throws SAFSException {
	  try{
		  if(facname == null || facname.length()==0){
				  String logname = getLogName();
				  facname = logname.split(";")[1];
		  }
		  STAFResult src = null;
		  src = submit2ForFormatUnchangedService(getMachine(), SAFS_LOGGING_SERVICE, 
				                                 "LOGMESSAGE "+ facname +" MESSAGE "+
				                                 lentagValue(message));
		  if(src.rc != STAFResult.Ok)
			  throw new SAFSException("SAFSLOGS LOGMESSAGE STAF Error: "+ src.rc +", "+ src.result);
		  
	  }catch(Exception x){
		  throw new SAFSException(x.getClass().getSimpleName()+": "+ x.getMessage());
	  }
  }
  
  /** 
   * <br><em>Purpose:</em> setup a variable with it's value using STAF safsvars service.
   * <br> format used:
   * <br> " SET " + lentagValue(var) + " VALUE " + lentagValue(val);
   * <br> example: SET :19:STAF/hook/variable1 VALUE :31:This is the value for variable1
   * <br><em>Side Effects:</em> STAF is modified (safsvars service)
   * <br><em>State Read:</em> fields 'handle'
   * <br><em>Assumptions:</em>  we are using the SAFSVARS SERVICE registered in STAF
   * <br> note that currently after the VALUE there are quotes around the 'val'
   * @param                     var, String name of variable
   * @param                     val, String value of variable (can be null or empty)
   * @return                    true if successful, false if not
   * @exception                 SAFSException if var is invalid
   **/
  public boolean setVariable (String var, String val) throws SAFSException {
    if ((var == null)||(var.length()==0)) throw new SAFSException("setVariable: variable name cannot be empty.");
    String service = SAFS_VARIABLE_SERVICE;
    String command = " SET " + lentagValue(var)+ " VALUE ";
    if (!((val == null)||(val.length()==0)))  command += lentagValue(val);
    try {
      submit(machine, service, command);
      return true;
    } catch (STAFException e) {
      Log.info("setVariable: error, rc: "+e.rc+", var: "+var+", val: "+val);
      return false;
    }
  }
  
  /**
   * <br><em>Purpose:</em> get the STAF SYSTEM variable value.
   * <br>This method will NOT depend on "safsvars service", it use the STAF-self provided variable service.
   * <br>
   * @param 	var:	The name of a STAF SYSTEM variable
   * @return	the value of a STAF SYSTEM variable
   * @throws SAFSException
   */
  public String getSTAFVariable(String var) throws SAFSException {
		String service = null;
		String command = null;
		int stafVersion = getSTAFVersion();

		if (stafVersion == 2) {
			throw new SAFSException("getSTAFVariable: not supported in STAF 2.");
		} else if (stafVersion == 3) {
			service = STAF_VARIABLE_SERVICE;
			command = " GET SYSTEM VAR " + var;
		}
		Log.debug("getSTAFVariable: command=" + command);

		try {
			String result = submit(machine, service, command);
			return result;
		} catch (STAFException e) {
			throw new SAFSException(getClass().getName(), "getSTAFVariable",
					"rc: " + e.rc + ", variable: " + var + ", getCommand: "
							+ command);
		}
	}
  
  /**
   * <br><em>Purpose:</em> set the STAF SYSTEM variable value.
   * <br>This method will NOT depend on "safsvars service", it use the STAF-self provided variable service.
   * <br>
   * @param 	var:	The name of a STAF SYSTEM variable
   * @param     val:	The value to be set to the variable
   * @return	true if the value of a STAF SYSTEM variable is correctly set
   * @throws SAFSException
   */
  public boolean setSTAFVariable(String var, String val) throws SAFSException {
		if ((var == null) || (var.length() == 0))
			throw new SAFSException("setSTAFVariable: variable name cannot be empty.");
		String service = null;
		String command = null;
		int stafVersion = getSTAFVersion();

		if (stafVersion == 2) {
			throw new SAFSException("setSTAFVariable: not supported in STAF 2.");
		} else if (stafVersion == 3) {
			service = STAF_VARIABLE_SERVICE;
			command = " SET SYSTEM VAR " + var + "=";
			if (!((val == null) || (val.length() == 0))) command += val;
		}
		Log.debug("setSTAFVariable: command=" + command);

		try {
			submit(machine, service, command);
			return true;
		} catch (STAFException e) {
			Log.info("setSTAFVariable: error, rc: " + e.rc + ", var: " + var
					+ ", val: " + val);
			return false;
		}
	}

  /** <br><em>Purpose:</em> delay number of milliseconds
   * <br><em>Side Effects:</em> uses STAF delay service
   * <br><em>Assumptions:</em>  STAF loaded
   * @param                     millisec, int
   * @return                    true if successful, false if not
   **/
  public boolean delay (int millisec) {
    String service = STAF_DELAY_SERVICE;
    String command =
      " delay " + Integer.toString(millisec);
    try {
      submit(machine, service, command);
      return true;
    } catch (STAFException e) {
      Log.info("delay: error, rc: "+e.rc+", millisec: "+millisec);
      return false;
    }
  }

  /** <br><em>Purpose:</em> get a queue message using STAF
   * @param                     name, String , the string name of the queue, if null then omit name part from command
   * @param                     waitTime, Integer, milliseconds to wait, if null, then omit from the get command; indefinate wait
   * @return                    String, the returned message
   * @exception                 SAFSException, if timeout or error
   **/
  public String getQueueMessage (String name, Integer waitTime) throws SAFSException {
    String service = "QUEUE";
    String command = " GET";
    
    // commands for STAFV2 and V3 are same
    if (name != null) {
      command = command + " NAME " + lentagValue(name);
    }
    command = command + " WAIT";
    if (waitTime != null) {
      command = command + " " + waitTime.toString();
    }
    STAFQueueMessage message = null;
    String result = null;
    try {
    	result = submit(machine, service, command);
    	message = new STAFQueueMessage(result);
    	// the command seems to fail to be executed both in STAF V2 and STAF V3. need to look into it in details??? (Junwu)
    	// in STAF 3.X, message.message is an Object instead of a String like in STAF2.X
    	// cast it to String.
    	return (String) message.message; // gets rid if STAF pre-suff and gets only the actual message sent
    }catch(ClassCastException cce){
    	return result;//message got from EmbeddedQueueService
    	
    }catch(NoSuchFieldError x){
        try { return (String) getQueueMessageField.get(message);}
	    catch(IllegalAccessException iae){
	        System.err.println("STAFQueueMessage "+ iae.getClass().getSimpleName()+" "+ iae.getMessage());
	    }catch(IllegalArgumentException iae){
	        System.err.println("STAFQueueMessage "+ iae.getClass().getSimpleName()+" "+ iae.getMessage());
	    }
        throw new SAFSException("STAFQueueMessage Error: this version of SAFS cannot use STAF version "+STAFVersion);
    } catch (STAFException e) {
      if (e.rc == STAFResult.Timeout) {
        throw new SAFSException(getClass().getName(), "getQueueMessage",
                                "STAF QUEUE dispatch has timed out; " +
                                "rc: "+e.rc+ ", name: "+ name+", waitTime: "+waitTime);
      }else if (e.rc == 56) { // Request Cancelled ?  New in STAF 3.x
          //System.out.println("STAF getQueueMessage Request Cancelled in "+ e.getClass().getName()+", "+ e.getMessage());
    	  return result;
      } else {
        throw new SAFSException(getClass().getName(), "getQueueMessage",
                                "STAF QUEUE ERROR; " +
                                "rc: "+e.rc+ ", name: "+ name+", waitTime: "+waitTime);
      }
    }
  }

  /** <br><em>Purpose:</em> send a queue message using STAF
   * @param                     name, String , the string name of the queue
   * @param                     message, String, the message to send, it will be length tagged.
   * @return                    boolean, true if successful, false if not
   **/
  public boolean sendQueueMessage (String name, String message) {
    String service = "QUEUE";
    String command = " QUEUE";
    if (name != null) {
        command = command + " NAME " + lentagValue(name);
    }
    String lentagMsg = lentagValue(message);
    //Carl Nagle QUEUE service can use the same length tagged format for both STAF V2 and V3.    
    //length tagging IS required to prevent STAFExceptions!
    command += " MESSAGE " + lentagMsg ;
    try {
      STAFResult result = submit2(machine, service, command);
      
      if(result.rc==STAFResult.QueueFull){
    	  //Queue Full, try to sleep for a while waitting queue to be consumed
    	  try{Thread.sleep(100);}catch(Exception x){}
    	  result = submit2(machine, service, command);
      }
      
      return result.rc==STAFResult.Ok;
    }catch (Exception e) { 
    	// stop possible infinite loop between STAFHelper and Log
    	if ((name != null)&&(handle != null) && (name.equalsIgnoreCase(Log.SAFS_TESTLOG_PROCESS))) {
    	    System.err.println("sendQueueMessage target \""+name+"\" may not be running.");
    	    System.err.println(e.toString());
    	}else{
            Log.info("sendQueueMessage target \""+name+"\" may not be running.");
    	}
    	return false;
    }
  }

  /** <br><em>Purpose:</em> pulse a single staf event (sem service)
   * @param                     event, String
   * @exception                 SAFSException if staf has a problem
   **/
  public void pulseEvent (String event) throws SAFSException {
    String service = "SEM";
    Log.debug("Pulsing EVENT "+ event);
    //Carl Nagle STAF V3 order of keywords IS important!
    String command = (STAFVersion < 3) ?
    					"EVENT " + event + " PULSE" :
    					"PULSE EVENT " + event;	
    try{
      submit(machine, service, command);
    } catch (STAFException e) {
      throw new SAFSException(getClass().getName(), "pulseEvent",
                              "rc: "+e.rc+ ", event: "+ event);
    }
  }

  /** <br><em>Purpose:</em> post a single staf event (sem service)
   * @param                     event, String
   * @exception                 SAFSException if staf has a problem
   **/
  public void postEvent (String event) throws SAFSException {
    String service = "SEM";
    Log.debug("Posting EVENT "+ event);
    //Carl Nagle STAF V3 order of keywords IS important!
    String command = (STAFVersion < 3) ?
    					"EVENT " + event + " POST" :
    					"POST EVENT " + event;	
    try{
      submit(machine, service, command);
    } catch (STAFException e) {
      throw new SAFSException(getClass().getName(), "postEvent",
                              "rc: "+e.rc+ ", event: "+ event);
    }
  }

  /** <br><em>Purpose:</em> reset a single staf event (sem service)
   * @param                     event, String
   * @exception                 SAFSException if staf has a problem
   **/
  public void resetEvent (String event) throws SAFSException {
    String service = "SEM";
    Log.debug("Resetting EVENT "+ event);

    //Carl Nagle STAF V3 order of keywords IS required!
    String command = (STAFVersion < 3) ? "EVENT " + event + " RESET" : "RESET EVENT " + event;
    
    try{
      submit(machine, service, command);
    } catch (STAFException e) {
      throw new SAFSException(getClass().getName(), "resetEvent",
                              "rc: "+e.rc+ ", event: "+ event);
    }
  }

  /** 
   * calls resetHookEvents(SAFS_ROBOTJ_PROCESS);
   * @exception SAFSException if staf has a problem
   * @deprecated This is specific to IBM Rational Functional Tester
   **/
  protected void resetEvents () throws SAFSException {
    resetHookEvents(SAFS_ROBOTJ_PROCESS);
  }

  /**
   * The events need to be reset when hook shutdown.
   */
  public static String[] eventsToReset = {SAFS_EVENT_READY,
	                                      SAFS_EVENT_DISPATCH,
	                                      SAFS_EVENT_RUNNING,
	                                      SAFS_EVENT_RESULTS,
	                                      SAFS_EVENT_DONE,
	                                      SAFS_EVENT_SHUTDOWN};

  /** 
   * Reset staf hook events (ready, dispatch, running, results, done, shutdown) for the given process.
   * If we are resetting events for a shutdown the engine should POST the Shutdown *after* this reset.
   * @exception SAFSException if staf has a problem
   **/
  public void resetHookEvents (String process_name) throws SAFSException {
    Log.info(process_name +" Resetting ALL "+ process_name +" Hook Events.");
    for(String event: eventsToReset){
    	resetEvent(process_name + event);
    }
  }
  
  public static String getEventStartString(String process_name){
	  return process_name+SAFS_EVENT_START;
  }
  public static String getEventReadyString(String process_name){
	  return process_name+SAFS_EVENT_READY;
  }
  public static String getEventDispatchString(String process_name){
	  return process_name+SAFS_EVENT_DISPATCH;
  }
  public static String getEventRunningString(String process_name){
	  return process_name+SAFS_EVENT_RUNNING;
  }
  public static String getEventResultsString(String process_name){
	  return process_name+SAFS_EVENT_RESULTS;
  }
  public static String getEventDoneString(String process_name){
	  return process_name+SAFS_EVENT_DONE;
  }
  public static String getEventShutdownString(String process_name){
	  return process_name+SAFS_EVENT_SHUTDOWN;
  }

  /**
   * staf event wait indefinitely or for a set number of seconds.
   * To wait for n milliseconds use waitEventMillis
   * @param  event, String
   * @param  delaySeconds, long, if < 0, then no param used for wait 
   * and the wait will be indefinite.
   * @exception SAFSException if staf has a problem
   * @see #waitEventMillis(String, int)
   **/
  public void waitEvent (String event, long delaySeconds) throws SAFSException {
    String service = "SEM";
    Log.debug("Waiting for EVENT "+ event);
    //Carl Nagle STAF V3 order of keywords IS important!
    String command = (STAFVersion < 3)? 
    		            "EVENT " + event + " WAIT " : 
    		            "WAIT EVENT " + event;
    if (delaySeconds >= 0) {
    	String secString = Long.toString(delaySeconds*1000);
    	//Carl Nagle the TIMEOUT keyword is new and required for STAF V3
    	command = (STAFVersion < 3) ? command + secString : command + " TIMEOUT "  + secString;  
    }    
    try {
      submit(machine, service, command);
    } catch (STAFException e) {
      throw new SAFSException(getClass().getName(), "waitEvent",
                              "rc: "+e.rc+ ", event: "+ event+ ", delaySeconds: "+delaySeconds);
    }
  }

  /**
   * staf event wait indefinitely or for a set number of seconds for an event waiter.
   * <br>That is, wait for some process to signal it is waiting for the specified event.
   * <br>We poll for a waiter about every 10 milliseconds.
   * @param  event, String
   * @param  delaySeconds, long, if < 0 then we will wait indefinitely (dangerous?).
   * @return true if a waiter was detected, false if no waiter was detected.
   * @exception SAFSException if staf has a problem
   **/
  public boolean waitEventWaiter (String event, long delaySeconds) throws SAFSException {
    String service = "SEM";
    Log.debug("Waiting for WAITER of EVENT "+ event);
    //Carl Nagle STAF V3 order of keywords IS important!
    String command = (STAFVersion < 3)? 
    		            "EVENT " + event + " DELETE " : 
    		            "DELETE EVENT " + event;
    long endtime = System.currentTimeMillis();
    if(delaySeconds > 0) endtime += (delaySeconds * 1000);    
    boolean indefinite = delaySeconds < 0;    
    boolean dosleep = false; //don't sleep on first iteration
	do{
		// shouldn't sleep first time through
		if(dosleep) {
			try{Thread.sleep(10);}catch(Exception x){}
		}else {
			dosleep = true;//sleep always for each subsequent loop
		}
	    try {
	      submit(machine, service, command);
	    } catch (STAFException e) {
	    	if(e.rc == STAFResult.SemaphoreHasPendingRequests) 
	    		return true; // We have a WAITER !!!
	    	// if event never posted and does not yet exist, this is OK.
	    	// but for any other STAFException this is a problem.
	    	if(e.rc != STAFResult.SemaphoreDoesNotExist) 
	    		throw new SAFSException(getClass().getName(), "waitEventWaiter",
	                      "rc: "+e.rc+ ", event: "+ event+ ", delaySeconds: "+delaySeconds);	    	
	    }
	    //if no exception thrown then we DID delete the event. No waiter yet.
	    if(delaySeconds == 0) return false;
    }while(indefinite || (System.currentTimeMillis() < endtime));
	return false;
  }

  /** 
   * staf event wait for n milliseconds.
   * Use waitEvent function if no wait or indefinite wait is needed.
   * @param event, String
   * @param delayMillis, int, if <=0 then 1 millis will be defaulted
   * @return boolean true if the event was detected, false if timeout was reached with no event.
   * @exception SAFSException if staf has a problem
   * @see #waitEvent(String, long)
   **/
  public boolean waitEventMillis(String event, int delayMillis) throws SAFSException {
    String service = "SEM";
    //Carl Nagle STAF V3 order of keywords IS important!
    String command = (STAFVersion < 3)? 
    		            "EVENT " + event + " WAIT " : 
    		            "WAIT EVENT " + event;
    if (delayMillis < 0) delayMillis = 1;
   	String secString = Integer.toString(delayMillis);
   	//Carl Nagle the TIMEOUT keyword is new and required for STAF V3
   	command = (STAFVersion < 3) ? command + secString : command + " TIMEOUT "  + secString;  
    try {
      submit(machine, service, command);
    } catch (STAFException e) {      
      if (e.rc == STAFResult.Timeout) {
    	  //Log.debug("STAFHelper.waitEventMillis hit TIMEOUT.");
    	  return false;
      }
      throw new SAFSException(getClass().getSimpleName(), "waitEvent",
                              "rc: "+e.rc+ ", event: "+ event+ ", delayMillis: "+delayMillis);
    }
    return true;
  }


  /** getNextTestEvent:
   * @return getNextHookTestEvent(SAFS_ROBOTJ_PROCESS, SAFS_HOOK_TRD);
   * @exception SAFSException if staf has a problem
   * @deprecated This is specific to IBM Rational Functional Tester
   **/
  public String getNextTestEvent () throws SAFSException {
  	return getNextHookTestEvent(SAFS_ROBOTJ_PROCESS, SAFS_HOOK_TRD);
  }


  /**
   * Perform the complete engine-side protocol for processing a test record 
   * dispatched from a driver.
   * <p>
   * getNextHookTestEvent:
   * <br>waitEvent  "Dispatch";
   * <br>resetEvent "Ready";
   * <br>if(result.rc != 0) throw SAFSException;
   * <br>result = postEvent "Running"
   * <br>return getVariable(INPUTRECORD);
   * @param process_name -- the STAF-registered name of the engine involved.
   * @param trd_name -- the TestRecordData SAFSVARS storage root, normally "SAFS/HOOK/". 
   * @return String, the variable INPUTRECORD
   * @exception SAFSException if staf has a problem
   */
  public String getNextHookTestEvent (String process_name, String trd_name) throws SAFSException {
	String dispatchEvent = process_name +"Dispatch";
	String readyEvent = process_name +"Ready";
	String runningEvent = process_name +"Running";
	boolean dispatched = false;
    try {
      //wait indefinitely, but try not to deadlock
      Log.debug("Waiting for EVENT "+ dispatchEvent);// too many messages    	
      do{ 
    	dispatched = waitEventMillis(dispatchEvent, 90);
    	// future: add checks to exit indefinite\deadlock loop
    	if(!dispatched)try{Thread.sleep(10);}catch(InterruptedException x){}
      }while(!dispatched);
    } catch (SAFSException se) {
      throw se;
    } finally {
      resetEvent(readyEvent);
    }
    postEvent(runningEvent);
    return getVariable(trd_name + SAFS_VAR_INPUTRECORD); // SAFS_HOOK_TRD 
  }


  /**
   * Perform the complete driver-side protocol for dispatching an engine to process 
   * a test record.
   * <p>
   * postNextHookTestEvent:
   * <br>wait for process_name "Ready"
   * <br>acquire SAFSVARS trd_root Mutex  
   * <br>set SAFSVARS test record data
   * <br>"Dispatch" to specified process_name
   * <br>waitFor "Results" event
   * <br>retrieve the results from SAFSVARS test record data
   * <br>pulse the "Done" event
   * <br>release the SAFSVARS trd_root Mutex
   * <br>if(result.rc != 0) throw SAFSException;
   * <p>
   * @param process_name -- the STAF-registered name of the engine to dispatch.
   * @param trd_root -- the TestRecordData SAFSVARS storage root, normally "SAFS/HOOK/". 
   * @param trd -- the TestRecordData to place into SAFSVARS for execution by the engine.
   * <p>
   * @return the statuscode from the call
   * @exception SAFSException if staf has a problem
   */
  public int postNextHookTestEvent (String process_name, String trd_root, TestRecordData trd) throws SAFSException {
	boolean ready = false;
	boolean shutdown = false;
	String readyEvent = process_name + SAFS_EVENT_READY;
	String shutdownEvent = process_name + SAFS_EVENT_SHUTDOWN;
	String dispatchEvent = process_name + SAFS_EVENT_DISPATCH;
	String runningEvent = process_name + SAFS_EVENT_RUNNING;
	String resultsEvent = process_name + SAFS_EVENT_RESULTS;
	String trd_root_trd = trd_root +"TRD";
	
    try {
        //waitEvent(process_name + SAFS_EVENT_READY, -1);	// wait forever ???!!!
        Log.debug("Waiting for EVENTS "+ readyEvent +" or "+ shutdownEvent);    	
        do{ 
          // future: add checks to exit indefinite\deadlock loop
          ready = waitEventMillis(readyEvent, 80);
          if(!ready) {
        	  try{Thread.sleep(10);}catch(Exception x){}
        	  shutdown = waitEventMillis(shutdownEvent, 10);
        	  if(!shutdown) try{Thread.sleep(10);}catch(Exception x){}
          }
        }
        while(!ready && !shutdown);

        // abort if the engine has shut itself down unexpectedly
        if(shutdown) {
        	Log.debug("STAFHelper.postNextHookTestEvent detected an engine SHUTDOWN for "+ process_name);
        	return trd.getStatusCode();
        }

        waitSTAFMutex(trd_root_trd, -1);		// wait forever ???!!!

        setSAFSTestRecordData(trd_root, trd);

        // post event dispatch, wait event running, reset event dispatch
        // pulse event seems to be too transient
		// pulseEvent(process_name + SAFS_EVENT_DISPATCH);
        postEvent(dispatchEvent);
        
        int attempt = 0;
        shutdown = false;        
        Log.debug("Waiting for EVENTS "+ runningEvent+" or "+ shutdownEvent+" or timeout.");    	
        do{ 
          ready = waitEventMillis(runningEvent, 80);
          if(!ready){ 
        	  try{Thread.sleep(10);}catch(Exception x){}
        	  shutdown = waitEventMillis(shutdownEvent, 10);
        	  if(!shutdown)try{Thread.sleep(10);}catch(Exception x){}
          }
          if(!ready && !shutdown){
        	  shutdown = ++attempt > 25;
        	  if(shutdown) Log.debug("STAFHelper.postNextHookTestEvent detected a response TIMEOUT shutdown.");
          }
        }while(!ready && !shutdown);
        
        resetEvent(dispatchEvent);
        
        if(shutdown) {
            try{Thread.sleep(25);}catch(Exception x){}
      	    Log.debug("STAFHelper.postNextHookTestEvent recovering from unexpected remote engine shutdown.");
            releaseSTAFMutex(trd_root_trd);		            
            return trd.getStatusCode();
        }
        
		//waitEvent(process_name + SAFS_EVENT_RESULTS, -1);	// wait forever ???!!!
        shutdown = false;
        Log.debug("Waiting for EVENTS "+ resultsEvent+" or "+ shutdownEvent);    	
        do{ 
            ready = waitEventMillis(resultsEvent, 80);
            // future: add checks to exit indefinite\deadlock loop
            if(!ready) {
          	    try{Thread.sleep(10);}catch(Exception x){}
            	shutdown = waitEventMillis(shutdownEvent, 10);          
          	    if(!shutdown)try{Thread.sleep(10);}catch(Exception x){}
            }
        }while(!ready && !shutdown);
        
		getSAFSTestRecordData(trd_root, trd);

		// if we pulse too soon the engine won't ever see it!
		// that can result in many unnecessary timeout delays in a test.
		// But, we don't want to wait too long for an engine that might be "gone".
		waitEventWaiter(process_name + SAFS_EVENT_DONE, 2);
		pulseEvent(process_name + SAFS_EVENT_DONE);	
		
        releaseSTAFMutex(trd_root_trd);		
        
        return trd.getStatusCode();
    } 
    catch (SAFSException se) {
    	Log.debug("STAFHelper.postNextHookTestEvent rethrowing SAFSException: "+ se.getMessage());
    	throw se;
    } 
    catch (NullPointerException npx){
    	Log.debug("STAFHelper.postNextHookTestEvent NullPointerException:", npx);
    	throw new SAFSException("STAFHelper.postNextHookTestEvent invalid NULL parameter.");}    
  }


	/**
	 * Set the SAFSVARS Test Record Data for the given trd_root.
	 * The most common trd_root is "SAFS/HOOK/", which is used by all official 
	 * drivers and engines.  The routine does not deal with the TRD Mutex for the 
	 * trd_root provided.  It assumes the caller is handling the mutex.
	 * <p>
	 * <ul><b>Sets the following record data</b>:
	 * <p>
	 * <li>inputrecord
	 * <li>separator
	 * <li>filename
	 * <li>linenumber
	 * <li>testlevel
	 * <li>appmapname
	 * <li>fac
	 * <li>statuscode
	 * <li>statusinfo
	 * </ul>
	 */
	public void setSAFSTestRecordData(String trd_root, TestRecordData trd) throws SAFSException {
		try{
			setVariable(trd_root + SAFS_VAR_INPUTRECORD, trd.getInputRecord());
			setVariable(trd_root + SAFS_VAR_LINENUMBER, String.valueOf(trd.getLineNumber()).trim());
			setVariable(trd_root + SAFS_VAR_SEPARATOR, trd.getSeparator());
			setVariable(trd_root + SAFS_VAR_TESTLEVEL, trd.getTestLevel());
			setVariable(trd_root + SAFS_VAR_FILENAME, trd.getFilename());
			setVariable(trd_root + SAFS_VAR_APPMAPNAME, trd.getAppMapName());
			setVariable(trd_root + SAFS_VAR_FAC, trd.getFac());
			setVariable(trd_root + SAFS_VAR_STATUSCODE, String.valueOf(trd.getStatusCode()).trim());
			setVariable(trd_root + SAFS_VAR_STATUSINFO, trd.getStatusInfo());
		}
		catch(SAFSException se){ throw se;}
		catch(NullPointerException npx){ throw new SAFSException("STAFHelper.setSAFSTestRecordData invalid NULL parameter.");}
	}


	/**
	 * Get the SAFSVARS Test Record Data into the given trd_root.
	 * The most common trd_root is "SAFS/HOOK/", which is used by all official 
	 * drivers and engines.  The routine does not deal with the TRD Mutex for the 
	 * trd_root provided.  It assumes the caller is handling the mutex.
	 * <p>
	 * <ul><b>Gets the following record data</b>:
	 * <p>
	 * <li>inputrecord
	 * <li>separator
	 * <li>filename
	 * <li>linenumber
	 * <li>testlevel
	 * <li>appmapname
	 * <li>fac
	 * <li>statuscode
	 * <li>statusinfo
	 * </ul>
	 */
	public void getSAFSTestRecordData(String trd_root, TestRecordData trd) throws SAFSException {
		try{
			trd.setInputRecord(getVariable(trd_root + SAFS_VAR_INPUTRECORD));

			try{trd.setLineNumber (Integer.parseInt(getVariable(trd_root + SAFS_VAR_LINENUMBER)));}
			catch(NumberFormatException nfe){trd.setLineNumber(0);}

			trd.setSeparator(getVariable(trd_root + SAFS_VAR_SEPARATOR));
			trd.setTestLevel(getVariable(trd_root + SAFS_VAR_TESTLEVEL));
			trd.setFilename(getVariable(trd_root + SAFS_VAR_FILENAME));
			trd.setAppMapName(getVariable(trd_root + SAFS_VAR_APPMAPNAME));
			trd.setFac(getVariable(trd_root + SAFS_VAR_FAC));

			try{trd.setStatusCode (Integer.parseInt(getVariable(trd_root + SAFS_VAR_STATUSCODE)));}
			catch(NumberFormatException nfe){trd.setStatusCode(DriverConstant.STATUS_SCRIPT_NOT_EXECUTED);}
			
			trd.setStatusInfo(getVariable(trd_root + SAFS_VAR_STATUSINFO));
		}
		catch(SAFSException se){ throw se;}
		catch(NullPointerException npx){ throw new SAFSException("STAFHelper.getSAFSTestRecordData invalid NULL parameter.");}
	}


  /** 
   * setTestResults: makes call to 'setHookTestResultsWTimeout(SAFS_ROBOTJ_PROCESS, 3);'
   * This is deprecated for older RobotJ handling only.
   * @exception SAFSException if staf has a problem
   * @deprecated IBM Rational Functional Tester specific 
   **/
  public void setTestResults () throws SAFSException {
    setHookTestResultsWTimeout(SAFS_ROBOTJ_PROCESS, 12);
  }


  /** 
   * setHookTestResults: 
   * makes call to 'setHookTestResultsWTimeout(process_name, 12);'
   * @exception SAFSException if staf has a problem
   **/
  public void setHookTestResults (String process_name) throws SAFSException {
    setHookTestResultsWTimeout(process_name, 12);
  }

  /** 
   * setHookTestResultsWTimeout: 
   * makes call to 'postEvent(process_name + Results);'
   * This routines will drive the RESULTS event true. This routine  
   * waits for the corresponding DONE event from the calling driver for up to 
   * timeoutseconds before timing out.  Timing out allows us to continue in the event 
   * of a Driver that has been shutdown.
   * @exception SAFSException if staf has a problem
   **/
  public void setHookTestResultsWTimeout (String process_name, long timeoutseconds) throws SAFSException {
  	if (timeoutseconds < 0) timeoutseconds = 0;
    postEvent(process_name + SAFS_EVENT_RESULTS);
    try{ waitEvent(process_name + SAFS_EVENT_DONE, timeoutseconds);}catch(Exception x){;}
    resetEvent(process_name + SAFS_EVENT_RESULTS);
  }



  /** <br><em>Purpose:</em> getitem from safsmaps using STAF
   ** <br> format used:
   ** <br> " getitem "+appMapID+" SECTION "+section+" Item "+item + "";
   ** <br> example: staf local safsmaps getitem "classicc" SECTION "LoginWindow" Item "OKButton"
   * <br><em>Side Effects:</em> STAF is accessed (safsmaps service)
   * <br><em>State Read:</em> field 'handle'
   * <br><em>Assumptions:</em>  we are using the SAFSMAPS SERVICE registered in STAF
   * <br> note that currently after each value there are quotes.
   * @param                     appMapID, String (can be null or empty)
   * @param                     section, String (can be null or empty)
   * @param                     item, String
   * @return                    itemvalue, null if exception is encountered meaning item not found
   **/
  public String getAppMapItem (String appMapID, String section, String item) {
    String service = "safsmaps";
    String command = " GETITEM ";
    if (!((appMapID == null)||(appMapID.length()==0))) 
        command += lentagValue(appMapID);        
	if (!((section == null)||(section.length()==0))) 
		command += " SECTION "+ lentagValue(section);	
    command += " ITEM "+lentagValue(item);
    
    try{
      Log.debug("getAppMapItem: "+command);
      String result = submit(machine, service, command).trim();
      return result;
    } catch (STAFException e) {
      Log.info("getAppMapItem not found: "+command+", e.rc:"+e.rc);
      return null;
    }
  }
  
  /** <br><em>Purpose:</em> getitem from safsmaps using STAF, and check if the item has a static
   *  		recognition string.
   ** <br> format used:
   ** <br> " getitem "+appMapID+" SECTION "+section+" Item "+item + "";
   ** <br> example: staf local safsmaps getitem "classicc" SECTION "LoginWindow" Item "OKButton"
   * <br><em>Side Effects:</em> STAF is accessed (safsmaps service)
   * <br><em>State Read:</em> field 'handle'
   * <br><em>Assumptions:</em>  we are using the SAFSMAPS SERVICE registered in STAF
   * <br> note that currently after each value there are quotes.
   * @param                     appMapID, String (can be null or empty)
   * @param                     section, String (can be null or empty)
   * @param                     item, String
   * @param						isDynamic, boolean
   * @return                    itemvalue, null if exception is encountered meaning item not found
   **/
  public String getAppMapItem (String appMapID, String section, String item, boolean isDynamic) {
    String service = "safsmaps";
    String command = " GETITEM ";
    if (!((appMapID == null)||(appMapID.length()==0))) 
        command += lentagValue(appMapID);        
	if (!((section == null)||(section.length()==0))) 
		command += " SECTION "+ lentagValue(section);	
    command += " ITEM "+lentagValue(item);
    if(isDynamic)
    	command += " ISDYNAMIC ";
    
    try{
      Log.debug("getAppMapItem: "+command);
      String result = submit(machine, service, command).trim();
      return result;
    } catch (STAFException e) {
      Log.info("getAppMapItem not found: "+command+", e.rc:"+e.rc);
      return null;
    }
  }

  /**
   * Queries the STAF HANDLE service to see if a particular process name is
   * currently registered and running.
   * <p>
   * For example, the SAFSVARS service process name is "SAFSVariableService".
   * <p>
   * The routine will do a QUERY ALL on the HANDLE service and then evaluate 
   * if the requested tool appears anywhere in the returned list of named processes. 
   * <p>
   * Note, because we do a substring search, we can match on just the most 
   * significant portion of the process name.
   * <p>
   * @param toolname -- The name of the process of interest.  Check your documentation
   *        to find the name normally registered by the tool of interest.  
   *        This can be just a substring of the full tool name.
   *
   * @return  true  or false
   *
   * @author  Carl Nagle
   * @since   JUN 19, 2003
   *<br> History:
   *<br>
   *<br>      JUN 19, 2003    (DBauman) Original Release
   *<br>      JUN 26, 2003    (DBauman) ported to java
   *<br>      APR 04, 2004    (Carl Nagle) Enabled matching on substring of tool name.
   *                                   and made the function public.
   *<br>      APR 06, 2004    (Carl Nagle) Ignore Case on toolname matches.
   */
  public boolean isToolAvailable(String toolname) {
	  if(EmbeddedHandles.isToolRunning(toolname)) return true;
    String result;
    String service = "HANDLE";
    //(Carl Nagle) the below change is valid.  STAF V3 does not have QUERY ALL listed as supported.
    String command = (STAFVersion < 3) ? "QUERY ALL " : "LIST";
	
    try {
      result = submit(machine, service, command);
      try{
      	// in STAF 3, result is an unmarshalled string with service names returned in it, it is also no problem to find out 
    	//  the searching service by using String.indexof()  
      	int loc = result.toUpperCase().indexOf(toolname.toUpperCase());
      	return (loc > -1) ? true:false;
      }catch (NullPointerException e) { return false; }
    } 
    catch (STAFException e) {
      Log.debug(process_name +" "+ getClass().getName()+ ".isToolAvailable: error, " +
                "rc: "+e.rc+ ", toolname: "+ toolname);
      return false;
    } 
  }

  /**
   * Queries the STAF SERVICE service to see if a particular LIBRARY is already running.  
   * This is often necessary because, for example, we cannot specify the JVM= option for 
   * launching new JSTAF services if the JSTAF service library is already running.
   * <p>
   * For example, the SAFSVARS service libary name "JSTAF".
   * <p>
   * The routine will do a LIST on the SERVICE service and then evaluate 
   * if the requested Library appears anywhere in the returned list of named services. 
   * <p>
   * @param libname -- The name of the LIBRARY of interest.  Check your documentation
   *        to find the name normally registered for the library of interest.  
   *        This check is NOT case-sensitive.  
   *
   * @return  true  or false
   *
   * @author  Carl Nagle
   * @since   OCT 09, 2013
   *<br> History:
   *<br>
   *<br>      OCT 09, 2013    (Carl Nagle) Original Release
   */
  public boolean isServiceLibraryRunning(String libname) {
    String result;
    try {
        String test = libname.toUpperCase();
        result = submit(machine, "service", "list");
        try{
      	    // in STAF 3, result is an UNMARSHALLED string.  It is not the same as seen in a CMD window! 
        	// It is also no problem to find out existence by using String.indexof()  
            Log.info(process_name +" "+ getClass().getName()+ ".isServiceLibraryRunning received the following service list: "+ result);
      	    int loc = result.toUpperCase().indexOf(test);
      	    return (loc > -1) ? true:false;
        }catch (NullPointerException ignore) { }
    }
    catch(NullPointerException np){
        Log.debug(process_name +" "+ getClass().getName()+ ".isServiceLibraryRunning: " +
                np.getClass().getSimpleName()+ " for libname: "+ libname);
    }
    catch (STAFException e) {
      Log.debug(process_name +" "+ getClass().getName()+ ".isServiceLibraryRunning: error, " +
                "rc: "+e.rc+ ", libname: "+ libname);
    } 
    return false;
  }

  /**
   * Queries the STAF SERVICE service to see if a particular service is
   * currently registered and running.
   * For example, the SAFSVARS service is "SAFSVARS".
   * <p>
   * The routine will do a "LIST" on the SERVICE service and then evaluate 
   * if the requested service appears anywhere in the returned list of services. 
   * <p>
   * Note, because we do a substring search, we can match on just the most 
   * significant portion of the service name.
   *
   * @param
   *
   * servicename  unique substring of the service of interest.  Check your documentation
   *              to find the name normally registered by the service of interest.  
   *              This can be just a substring of the full service name.
   *               
   *
   * @return
   *
   *  true  or false
   *
   *
   * @author  Carl Nagle
   * @since   JUN 07, 2004
   *<br> History:
   *<br>
   *<br>      JUN 07, 2004    (Carl Nagle) Original Release
   */
  public boolean isServiceAvailable(String servicename) {
	if(EmbeddedHandles.isServiceRunning(servicename)) return true;
    int status;
    String result;
    String service = "SERVICE";
    String command = "LIST ";
    try {
      result = submit(machine, service, command);
      try{
    	// in STAF 3, result is unmarshalled string with service names in it, it is also no problem to find out the searching service by using String.indexof  
      	int loc = result.toUpperCase().indexOf(servicename.toUpperCase());
      	return (loc > -1) ? true:false;
      }catch (NullPointerException e) { return false; }
    } 
    catch (STAFException e) {
      Log.debug(process_name +" "+ getClass().getName()+ ".isServiceAvailable: error, " +
                "rc: "+e.rc+ ", servicename: "+ servicename);
      return false;
    } 
  }

  /**
   **Function isSAFSVARSAvailable()
   **<p>
   **DESCRIPTION:
   **
   **Queries to see if the "SAFSVARS" service is currently running.
   **
   **@return
   **
   **true or false
   **
   **<br>Orig Author: Carl Nagle
   **<br>Orig   Date: JUN 19, 2003
   **<br>History:
   **<br>
   **<br>JUN 19, 2003    Original Release
   **<br>JUN 26, 2003    dbauman: ported to java
   **<br>JUN 07, 2004    Carl Nagle: mod to use isServiceAvailable
   **/
  public boolean isSAFSVARSAvailable () {
    return isServiceAvailable(SAFS_VARIABLE_SERVICE);
  }

  /**
   **Function isSAFSMAPSAvailable()
   **<p>
   **DESCRIPTION:
   **<p>
   **Queries to see if the "SAFSMAPS" service is running.
   **<p>
   **
   **PARAMETERS:
   **<p>
   **(none)
   **<p>
   **
   **RETURNS:
   **<p>
   **true or false
   **<p>
   **
   **<p>
   **
   **<br>Orig Author: Carl Nagle
   **<br>Orig   Date: JUN 19, 2003
   **<br>History:
   **<br>
   **<br>JUN 19, 2003    Original Release
   **<br>JUN 26, 2003    dbauman: ported to java
   **<br>JUN 07, 2004    Carl Nagle: mod to use isServiceAvailable
   **/
  public boolean isSAFSMAPSAvailable () {
    return isServiceAvailable(SAFS_APPMAP_SERVICE);
  }

  /**
   **Function isSAFSLOGSAvailable()
   **<p>
   **DESCRIPTION:
   **
   **Queries to see if the "SAFSLOGSLog" service is running.  This service is launched 
   **and used by "SAFSLOGS" durings its initialization.
   **
   **@return
   **
   **true or false
   **
   **<br>Orig Author: Carl Nagle
   **<br>Orig   Date: SEP 12, 2003
   **<br>History:
   **<br>
   **<br>SEP 12, 2003    Original Release
   **<br>JUN 07, 2004    Carl Nagle: mod to use isServiceAvailable
   **/
  public boolean isSAFSLOGSAvailable () {
	  boolean running =isServiceAvailable(SAFS_LOGGINGLOG_SERVICE);
	  if(!running) running = EmbeddedHandles.isServiceRunning(SAFS_LOGGING_SERVICE);	  
	  return running;
  }

  /**
   **Function isSAFSINPUTAvailable()
   **<p>
   **DESCRIPTION:
   **<p>
   **Queries to see if the "SAFSINPUT" service is running.
   **<p>
   **
   **PARAMETERS:
   **<p>
   **(none)
   **<p>
   **
   **RETURNS:
   **<p>
   **true or false
   **<p>
   **
   **<p>
   **
   **<br>Orig Author: Carl Nagle
   **<br>Orig   Date: JUN 17, 2004
   **<br>History:
   **<br>
   **<br>JUN 17, 2004    Original Release
   **/
  public boolean isSAFSINPUTAvailable () {
    return isServiceAvailable(SAFS_INPUT_SERVICE);
  }

  /**
   **  Function STAFwaitMutex(String mutex, long delaySeconds)
   **<p>
   ** DESCRIPTION:
   **
   **  WAIT for a single STAF SEMaphore MUTEX
   **
   **
   ** @param
   **
   **  mutex       the name of the mutex
   **
   ** @param  delaySeconds  the number of seconds to wait ( < 0 = wait indefinitely)
   **
   ** @exception
   **
   **      SAFSException if STAFException is caught
   **
   **
   **<br> Orig Author: Carl Nagle
   **<br> Orig   Date: JUN 19, 2003
   **<br> History:
   **<br>
   **<br>      JUN 19, 2003    Original Release
   **<br>      JUN 26, 2003    dbauman: ported to java
   **/
  public void waitSTAFMutex (String mutex, long delaySeconds) throws SAFSException {
    String service = "SEM";
    Log.debug(process_name +" Waiting for MUTEX "+ mutex);
    //Carl Nagle STAF V3 order of keywords IS important!
    String command = (STAFVersion < 3) ?
    					"MUTEX "+ mutex +" REQUEST " :  "REQUEST MUTEX " + mutex;	
    if (delaySeconds >= 0) {
    	String secString = Long.toString(delaySeconds*1000);
    	//Carl Nagle the TIMEOUT keyword is new for STAF V3
    	command = (STAFVersion < 3) ? command + secString : command + " TIMEOUT "  + secString;  
    }
    
    try {
      submit(machine, service, command);
    } catch (STAFException e) {
      throw new SAFSException(getClass().getName(), "waitSTAFMutex",
                              "rc: "+e.rc+ ", mutex: "+ mutex+", delaySeconds: "+delaySeconds);
    }
  }

  /**
   **
   **  Function STAFreleaseMutex(String mutex)
   **<p>
   ** DESCRIPTION:
   **
   **  RELEASE a single STAF SEMaphore MUTEX
   **
   ** @param
   **
   **  mutex       the name of the mutex
   **
   ** @exception
   **
   **      SAFSException if STAFException is caught
   **
   **
   **<br> Orig Author: Carl Nagle
   **<br> Orig   Date: JUN 19, 2003
   **<br> History:
   **<br>
   **<br>      JUN 19, 2003    Original Release
   **<br>      JUN 26, 2003    dbauman: ported to java
   **/
  public void releaseSTAFMutex (String mutex) throws SAFSException {
    String service = "SEM";
    Log.debug(process_name +" Releasing MUTEX "+ mutex);
    //Carl Nagle STAF V3 order of keywords IS important!
    String command = (STAFVersion < 3) ?
    					"MUTEX "+ mutex +" RELEASE" : "RELEASE MUTEX "+ mutex;
    try {
      submit(machine, service, command);
      Log.debug(process_name +" MUTEX "+ mutex +" should now be released.");
    } catch (STAFException e) {
      throw new SAFSException(getClass().getName(), "releaseSTAFMutex",
                              "rc: "+e.rc+ ", mutex: "+ mutex);
    }
  }
  
  /**
   * @return the version number of STAF. E.g. return 2 for STAF2.6.11; 0 means no STAF loaded. 
   */
  public int getSTAFVersion() {
	  if (STAFVersion != 0) return STAFVersion;
	  getSTAFVersionString(handle);
	  int pos = STAFVersionString.indexOf(".");
	  if (pos > 0) {
		  try {
			  STAFVersion = Integer.parseInt(STAFVersionString.substring(0, pos));
		  }catch (NumberFormatException nfe){
			  Log.debug(process_name + getClass().getName()+ ".getSTAFVersion(): fail to parse STAF version string, " +
					  STAFVersionString + nfe.toString());
		  }
	  }
	  return STAFVersion;
  }
  
  /**
   * @return The staf version string, such as 3.3.3
   */
  public static String getSTAFVersionString(HandleInterface handle) {
	  if(STAFVersionString.length()>0) return STAFVersionString;
	  String debugMsg = STAFHelper.class.getName()+".getSTAFVersionString(): ";
	  String command = "VAR";
	  String staf2Request = " GLOBAL GET "+STAF_GLOBALVARS_VERSION;
	  String staf3rRequest = " GET SYSTEM VAR "+STAF_GLOBALVARS_VERSION;
	  String version = "2.0";
	  
	  STAFResult result = handle.submit2("localhost", command, staf2Request);
	  if(result.rc==STAFResult.Ok){
		  version = result.result;
	  }else{
		  //Try staf version3 command to get version
		  result = handle.submit2("localhost",command,staf3rRequest); 
		  if(result.rc==STAFResult.Ok){
			  version = result.result;
		  }else{
			  Log.debug(debugMsg+" can not get variable '"+STAF_GLOBALVARS_VERSION+"'. Consider staf version as 2.");
		  }
	  }
	  STAFVersionString = version.trim();
	  return STAFVersionString;
  }

  
	/** 
	 * getSTAFEnv
	 * returns string value or an empty string. null if not successfully executed or found.
	 */
	public String getSTAFEnv(String env) {
		String debugmsg = getClass().getName()+".getSTAFEnv(): ";
		int stafVersion = getSTAFVersion();
		String request = "";
		String resultValue = null;

		if (stafVersion < 3) {
			request = "GLOBAL GET STAF/Env/" + env;
		} else {
			request = "GET SYSTEM VAR STAF/Env/" + env;
		}

		STAFResult result = submit2("local", "VAR", request);
		if (result.rc == STAFResult.Ok){
			resultValue = result.result;
		}else if (result.rc == STAFResult.VariableDoesNotExist){
			Log.debug(debugmsg+" Can not get variable STAF/Env/"+env);
		}else{
			Log.debug(debugmsg+" failed. STAF Status Code: "+ result.rc);
		}
		return resultValue;
	}
	
	/**
	 * localPing to know if STAF has been initailized
	 * @return String if ok. "PONG" returned in STAF2.x and STAF3.3.3
	 * @throws STAFException 
	 */
	public String localPing() throws STAFException {
	    Log.debug(getClass().getName() +"localPing(): to know if STAF has been initailized.");
		return submit("local", "PING", "PING");	
	}
	
	/**
	 * shutDown STAF machine, Applies to STAF2.X and STAF3.X
	 * @return STAFResult
	 */
	public STAFResult shutDown(String machine) {
		if(handle == null){
			Log.info("STAFHelper IGNORING STAFHandle NULL in shutdown...");
			return new STAFResult(3, machine+" may already have shutdown.");
		}
	    Log.debug(getClass().getName() +"shutDown(): start to shutdown " + machine);
		STAFResult rc = submit2(machine, "SHUTDOWN", "SHUTDOWN");
		handle = null;
		return rc;
	}
	
	/**
	 * start local STAF process to execute "STAF local PROCESS START COMMAND ...".  Applies to STAF2.X and STAF3.X.
	 * @param appname String, the name of application to run
	 * @param workdir String, the directory serves as working directory for application
	 * @return STAFResult the result.
	 * @throws IOException
	 */
	public STAFResult localStartProcess(String appname, String workdir) throws IOException{
    	return startProcess("local", appname, workdir);
	}
	
	/**
	 * execute "STAF machine PROCESS START COMMAND ...".  Applies to STAF2.X and STAF3.X.
	 * @param machine String, the name of the machine where the application will run
	 * @param appname String, the name of application to run
	 * @param workdir String, the directory serves as working directory for application
	 * @return STAFResult the result.
	 * @throws IOException
	 */
	public STAFResult startProcess(String machine, String appname, String workdir) throws IOException{
		String debugmsg = getClass().getName()+".startProcess(): ";
		
		//appname should not contain any space at the beginning, it will cause error
		String lcommand = lentagValue(appname.trim());
		String lwdir = null;
		if(workdir != null) lwdir = lentagValue(workdir.trim());
		STAFResult rc = null;
		String fullcmd = "START COMMAND " + lcommand ;
		if (workdir==null) {
			fullcmd += " NEWCONSOLE";
			Log.info(debugmsg + " fullcmd: " + fullcmd);    	
			rc = submit2(machine, "PROCESS", fullcmd);
		} else {
			fullcmd += " WORKDIR "+ lwdir +" NEWCONSOLE";
			Log.info(debugmsg + " fullcmd: " + fullcmd);     	
			rc = submit2(machine, "PROCESS", fullcmd);
		}
		return rc;
	}
	
	/**
	 * add and launch standard SAFS services SAFSINPUT, SAFSMAPS and SAFSLOG
	 * @param machine
	 * @param servicename, one of three services SAFSINPUT, SAFSMAPS and SAFSLOG.
	 * @param classpath
	 * @param dir
	 * @param options
	 * @return STAFResult
	 */
	public STAFResult addService(String machine, String servicename, String classpath, String dir, String options) {
	    try{
	    	if (dir.indexOf(" ")>0){ 
	    		dir = "\""+dir+"\"";
	    		dir = StringUtilities.findAndReplace(dir, "\\", "/");
	    		dir = lentagValue(dir);
	    	}
	    }catch(Exception x){}
	    
	    String initstr = "ADD SERVICE "           + servicename +
	    				 " LIBRARY JSTAF EXECUTE "+ classpath;
	    
	    if (options.length() > 0) initstr += " "+ options;
   	    
	    if (dir.length() > 0) initstr += " PARMS DIR " + dir;

		Log.info("INITSTR="+initstr);				                 
		// launch SAFSINPUT
		return submit2(machine, "SERVICE", initstr);		
	}
	/**
	 * add and launch standard SAFS service, SAFSVARS.
	 * @param machine
	 * @param servicename, SAFSVARS or user-defined service that follows the rule of SAFSVARS.
	 * @param classpath
	 * @param mapService
	 * @param options
	 * @return
	 */
	public STAFResult addServiceSAFSVARS(String machine, String servicename, String classpath, String mapService, String options) {
		
		String initstr = "ADD SERVICE "           + servicename +
        				 " LIBRARY JSTAF EXECUTE "+ classpath;


		if (options.length()>0) initstr += " "+ options;
	
		if (mapService.length()>0) initstr += " PARMS SAFSMAPS "+ mapService;

		Log.info("INITSTR="+initstr);				                 
		// launch SAFSVARS
		return submit2(machine, "SERVICE", initstr);
	}
	
	/**
	 * unload a STAF service
	 * @param machine, STAF machine name
	 * @param service, service name
	 */
	public STAFResult removeService(String machine, String service) {
		if(handle == null){
			Log.info("STAFHelper IGNORING STAFHandle NULL in removeService...");
			return new STAFResult(3, machine+":"+ service +" may already have shutdown.");
		}
		Log.debug( getClass().getName()+ " removeService(): removing service " + service + " at " + machine);
		if(no_staf_handles || EmbeddedHandles.isServiceRunning(service)){
			return EmbeddedHandles.getService(service).terminateService();
		}
		STAFResult rc = submit2(machine, "SERVICE", "REMOVE SERVICE " + service);
		long timeout = System.currentTimeMillis()+ 6000;
		long now = 0;
		boolean loop = true;
		while (loop){
			loop = this.isServiceAvailable(service);
			now = System.currentTimeMillis();
			if(loop) {
				loop = now < timeout;
				try{Thread.sleep(100);}catch(Exception x){}
			}
		}
		return rc;
	}
	
}
