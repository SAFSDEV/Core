/**
 * Copyright (C) SAS Institute, All rights reserved.
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
/**
 * 	History for developer:
 *   <br>   NOV 09, 2003    (Carl Nagle) Original Release
 *   <br>   NOV 11, 2003    (Carl Nagle) Fixed bug: give TestRecordHelper the STAFHelper it needs.
 *   <br>   DEC 08, 2004    (Carl Nagle) Added support for evaluateRuntimeExceptions
 *   <br>   JAN 21, 2005    (Carl Nagle) Added support for semaphoreRoot and disabling System.exit.
 *   <br>   AUG 01, 2007    (Carl Nagle) Added support for hook_shutdown() for subclasses to add 
 *                                   shutdown activities.
 *   <br>	SEP 28, 2010   	(JunwuMa) Add status STEP_RETRY and STEPPING_RETRY for SAFSVARS variable 
 *                                   'SAFS_DRIVER_CONTROL' about step retry feature.
 *   <br>	APR 26, 2016	(Lei Wang) Provide the ability to get configuration settings for Hook. 
 */
package org.safs;

import java.io.File;
import java.util.Locale;

import org.safs.logging.AbstractLogFacility;
import org.safs.logging.LogUtilities;
import org.safs.staf.STAFProcessHelpers;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.drivers.ConfigureFile;
import org.safs.tools.drivers.ConfigureInterface;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverInterface;

/**
 * Generic Java Hook for tool-independent SAFS Engines.
 * This abstract class provides the implementation for the initialization and event 
 * handling of all Java-based SAFS Engines that will be controlled via our STAF protocols.
 * <p>
 * If the engine is being launched manually or via a batch file or script then the following  
 * Java System property should be set on the Java command line:
 * <p>
 * <ul>-Dsafs.config.paths=&lt;testpath>;&lt;projectpath>;&lt;driverpath></ul>
 * <p>
 * <i>testpath</i> is the file path to the test configuration INI file,<br>
 * <i>projectpath</i> is the file path to the project configuration INI file,<br>
 * <i>driverpath</i> is the file path to the SAFS driver configuration INI file.
 * <p>
 * Concrete subclasses will have to provide their specific flavors of subclasses for:
 * <ul>
 * <li>DDGUIUtilities
 * <li>LogUtilities
 * <li>TestRecordHelper
 * </ul>
 * <p>
 * Concrete subclasses need to consider overriding the following methods if they need to 
 * alter the default behavior:
 * <ul>
 * <li> evaluateRuntimeException
 * <li> allowSystemExit
 * </ul>
 * 
 * @author Carl Nagle, SAS Institute
 * @since   NOV 09, 2003
 **/
public abstract class JavaHook implements HookConfig{

  /** "SHUTDOWN_HOOK" **/
  public static final String SHUTDOWN_RECORD    = "SHUTDOWN_HOOK";

  /** "PAUSE" **/
  public static final String PAUSE_EXECUTION    = "PAUSE";

  /** "STEP" **/
  public static final String STEP_EXECUTION     = "STEP";

  /** "STEPPING" **/
  public static final String STEPPING_EXECUTION = "STEPPING";

  /** "RUNNING" **/
  public static final String RUNNING_EXECUTION  = "RUNNING";

  /** "STEP_RETRY" **/
  public static final String STEP_RETRY_EXECUTION 		= "STEP_RETRY";
  
  /** "STEPPING_RETRY" **/
  public static final String STEPPING_RETRY_EXECUTION	= "STEPPING_RETRY";
  
  /** "ON" for SAFSVARS variable 'SAFS_DRIVER_CONTROL_POF'/'SAFS_DRIVER_CONTROL_POW' **/
  public static final String PAUSE_SWITCH_ON  			= "ON";
  
  /** "OFF" for SAFSVARS variable 'SAFS_DRIVER_CONTROL_POF'/'SAFS_DRIVER_CONTROL_POW' **/
  public static final String PAUSE_SWITCH_OFF 			= "OFF";
  
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
  
  /**Used to get settings from ConfigureInterface. 
   * @see #instantiateHookConfig()*/
  protected HookConfig hookconfig = null;
  
  /** 
   * The name of the process this hook supports. 
   * This is typically the process name we have registered with STAF.
   * NOTE: calling setProcessName or setHelper will automatically reset the semaphore_name 
   * to be the same as the process_name.  This is the default mode of operation and should 
   * be OK for most applications.  However, if the semaphore_name is to be different, 
   * then you must also call setSemaphoreName.
   */
  protected String process_name = null; //STAFHelper.SAFS_ROBOTJ_PROCESS
  public String    getProcessName (){ return process_name; }
  /** 
   * Set the process name for this hook for an instance created from an empty constructor.
   * The function will not overwrite an existing process name.  A STAFHelper will be 
   * initialized with the given process name if accepted.  
   * 
   * NOTE: calling setProcessName or setHelper will automatically reset the semaphore_name 
   * to be the same as the process_name.  This is the default mode of operation and should 
   * be OK for most applications.  However, if the semaphore_name is to be different, 
   * then you must also call setSemaphoreName.
   **/
  protected void setProcessName (String process_name){
  	if ((this.process_name == null)&&(process_name != null)&&(process_name.length() > 0)){
  	  this.process_name = process_name;
  	  setSemaphoreName(process_name);
      setHelper(null);
  	}
  }
  

  /** 
   * The root name used in STAF Semaphore EVENT communication. 
   * In most cases this is the same as the process_name.  An exception would be 
   * where multiple STAF clients all have the same process_name.  A unique 
   * semaphore name must exist for these.
   * NOTE: calling setProcessName or setHelper will automatically reset the semaphore_name 
   * to be the same as the process_name.  This is the default mode of operation and should 
   * be OK for most applications.  However, if the semaphore_name is to be different, 
   * then you must also call setSemaphoreName.
   */
  protected String semaphore_name = ""; 
  protected  void setSemaphoreName(String semname){ semaphore_name = semname;}
  public    String getSemaphoreName(){ return semaphore_name;}
  
  /** Root Name of TestRecordData store in SAFSVARS.
   * Default is STAFHelper.SAFS_HOOK_TRD **/
  protected String  trd_name     = STAFHelper.SAFS_HOOK_TRD;// default for all engines  

  public String     getTRDName () { return trd_name; }
  public void      setTRDName (String trdname){ if (trdname != null) trd_name = trdname; }


  /** The LogUtilities the hook will use for logging. **/
  protected LogUtilities       log = null;

  /** Retrieve the LogUtilities used by the subclass.
   * If no LogUtilities has been set (log == null), then the implementation is expected 
   * to instantiate and return a LogUtilities instance appropriate for the hook. **/
  public abstract LogUtilities getLogUtilities();
  public void                  setLogUtilities(LogUtilities lu) { log = lu; }


  /** The STAFHelper used to interact with STAF. **/
  protected STAFHelper helper = null;

  public STAFHelper getHelper() {return helper;}
  /** Set our STAFHelper to be the one provided.
   * Optionally, the input parameter can be a String which represents the process name 
   * for the desired STAFHelper (that may or may not exist).
   * <p>
   * The user can initialize both the process name and the STAFHelper by 
   * calling this routine with a valid process name String or STAFHelper.  
   * <p>
   * If the current instance already has a defined process name, then any process name 
   * provided here is ignored and the STAFHelper is initialized with the existing value.
   * <p>
   * If the user provides a valid STAFHelper; then the process_name will be set to the 
   * name used to create the STAFHelper.
   * <p>
   * NOTE: This will automatically reset the semaphore_name to be the 
   * same as the process_name.  This is the default mode of operation and should be OK 
   * for most applications.  However, if the semaphore_name is to be different, 
   * then you must also call setSemaphoreName after this call.
   * <p>
   * @param processhelper -- Object: String process name used to create/retrieve a 
   *        STAFHelper; or an already instanced STAFHelper object.
   * @exception NullPointerException (with message) -- if null is provided.
   **/
  public void setHelper(Object processhelper){
  	if (processhelper == null) {
  		if ((process_name == null)||(process_name.length() == 0)) {
  	        throw new NullPointerException( getClass().getName() +
  		    ":setHelper() parameter cannot be null!");
  		}
  		processhelper = process_name;
  	}

  	if (processhelper instanceof java.lang.String){
  		String processname = (String) processhelper;
  	    if ((this.process_name == null)&&(processname.length() > 0)){
  	        this.process_name = processname;
  	        setSemaphoreName(processname);
  	    }
        try { 
        	helper = STAFProcessHelpers.registerHelper(process_name); 
        } 
        catch (SAFSSTAFRegistrationException e) { 
        	if(!STAFHelper.no_staf_handles){
				try{
					helper = STAFProcessHelpers.launchSTAFProc(process_name);
					SingletonSTAFHelper.setInitializedHelper(helper);
				}
				catch(org.safs.SAFSSTAFRegistrationException rx2){
					e.printStackTrace();
					Log.info("STAFProcessContainer unable to launch STAF or register with STAF.", e);
				}
        	}
        }
  	}
  	else if (processhelper instanceof org.safs.STAFHelper){
  		helper = (STAFHelper) processhelper;
  		this.process_name = helper.getProcessName();
  		setSemaphoreName(this.process_name);
  	}
	if(helper.isInitialized()) Log.setHelper(helper);
  }

  
  /** The TestRecordHelper the hook will use for handling TestRecordData. **/
  protected TestRecordHelper       data = null;

  /** Retrieve the TestRecordHelper used by the subclass.
   * If no TestRecordHelper has been set (data == null), then the implementation is expected 
   * to instantiate and return a TestRecordHelper instance appropriate for the hook. **/
  public abstract TestRecordHelper getTRDData();
  public          void             setTRDData(TestRecordHelper trd_data){
  	data = trd_data;
  }


  /** The DDGUIUtilities the hook will use for handling components. **/
  protected DDGUIUtilities       utils = null;

  /** Retrieve the DDGUIUtilities used by the subclass.
   * If no DDGUIUtilities has been set (utils == null), then the implementation is expected 
   * to instantiate and return a DDGUIUtilities instance appropriate for the hook. **/
  public abstract DDGUIUtilities getGUIUtilities();
  public          void           setGUIUtilities(DDGUIUtilities gui_utils){
    utils = gui_utils;
  }


  /** The ProcessRequest object the hook will use for handling input records. **/
  protected ProcessRequest       processor = null;

  /** Retrieve the ProcessRequest instance used by the subclass.
   * If no ProcessRequest has been set (processor == null), then the implementation is expected 
   * to instantiate and return a ProcessRequest instance appropriate for the hook. **/
  public abstract ProcessRequest getRequestProcessor();
  public          void           setRequestProcessor(ProcessRequest aprocessor){
    processor = aprocessor;
  }


  /** "failedSAFSTextResourceBundle" -- our default source of error messages  **/
  protected GetText errorText = new GetText("failedSAFSTextResourceBundle", 
                                             Locale.getDefault());
                                             
  /** "generic_error" **/
  protected static final String GENERIC_ERROR = "generic_error";
  /** "staf_error" **/
  protected static final String STAF_ERROR    = "staf_error";


  /**
   * Empty Constructor.
   * All class initialization must still be performed.
   */  
  public JavaHook (){;}


  /**
   * Minimal initialization constructor.  Additional initialization still required.
   * This constructor will initializae the STAF connection with the process_name 
   * given.  The hook will use the default global TestRecordData storage of 
   * SAFS/Hook/TRD
   * <p>
   * @param process_name -- The process name associated with STAF for this hook.
   */  
  public JavaHook (String process_name){  
    setProcessName(process_name);
  }


  /**
   * Minimal initialization constructor.  Additional initialization still required.
   * This constructor will initializae the STAF connection with the process_name 
   * given.  The hook will use the default global TestRecordData storage of 
   * SAFS/Hook/TRD
   * <p>
   * @param process_name -- The process name associated with STAF for this hook.
   * @param logs -- The LogUtilities to use for STAF logging.
   */  
  public JavaHook (String process_name, LogUtilities logs){  
    setProcessName(process_name);
    setLogUtilities(logs);
  }


  /**
   * Minimal initialization constructor.  Additional initialization still required.
   * This constructor will initializae the STAF connection with the process_name 
   * given.  The hook will use the global TestRecordData storage with the root 
   * TestRecordData name given.
   * <p>
   * @param process_name -- The process name associated with STAF for this hook.
   * @param trd_name -- The root name for specific TestRecordData to reference in 
   *                    STAF.  This should typically be STAFHelper.SAFS_HOOK_TRD 
   *                    which is used by default.
   */  
  public JavaHook (String process_name, String trd_name){
  
    this(process_name);
    setTRDName(trd_name);
  }
  

  /**
   * Standard initialization constructor.
   * This constructor will initialize the STAF connection with the process_name 
   * given.  The hook will use the global TestRecordData storage with the root 
   * TestRecordData name given. The LogUtilities provided will be used for all logging.
   * <p>
   * @param process_name -- The process name associated with STAF for this hook.
   * @param trd_name -- The root name for specific TestRecordData to reference in 
   *                    STAF.  This should typically be STAFHelper.SAFS_HOOK_TRD 
   *                    which is used by default.
   * @param logs -- The LogUtilities to use for STAF logging.
   */  
  public JavaHook (String process_name, String trd_name, LogUtilities logs){
  
    this(process_name);
    setTRDName(trd_name);
    setLogUtilities(logs);
  }


  /**
   * Advanced Constructor.
   * This constructor allows an advanced engine to override some of the default values 
   * or objects used by the standard constructor.  Thus, a hook can be setup to use 
   * alternative implementations; or object instances more tailored for varying needs.
   * <p>
   * The hook will use the global TestRecordData storage with the root 
   * TestRecordData name given. The LogUtilities provided will be used for all logging.
   * <p>
   * @param process_name -- The process name associated with STAF for this hook.
   * @param trd_name -- The root name for specific TestRecordData to reference in 
   *                    SAFSVARS.  This should typically be STAFHelper.SAFS_HOOK_TRD 
   *                    which is used by default.
   * @param logs -- The LogUtilities to use for STAF logging.
   * @param trd_data -- TestRecordHelper to hold TestRecordData.
   * @param gui_utils -- DDGUIUtilities the hook will use for handling components.
   * @param aprocessor -- ProcessRequest object the hook will use for routing records.
   */  
  public JavaHook (String process_name, String trd_name, LogUtilities logs,
                   TestRecordHelper trd_data, 
                   DDGUIUtilities gui_utils, 
                   ProcessRequest aprocessor){
  
    this(process_name, trd_name, logs);
    setTRDData(trd_data);
    setGUIUtilities(gui_utils);
    setRequestProcessor(aprocessor);
  }

  /**
   * Initialize access to chained ConfigurationInterface data.
   * We initialize the chained data based on the availability and contents of 
   * the System Property "safs.config.paths", as defined:<br>
   * @see org.safs.tools.drivers.DriverConstant#PROPERTY_SAFS_CONFIG_PATHS 
   * @see org.safs.tools.drivers.ConfigureInterface
   * @see org.safs.TestRecordHelper#setConfig(ConfigureInterface)
   * @see org.safs.TestRecordHelper#getConfig()
   */
  protected void initConfigPaths(){
		String path = System.getProperty(DriverConstant.PROPERTY_SAFS_CONFIG_PATHS);
		String [] paths = new String[0];
		if(path != null){
			paths = path.split(File.pathSeparator);
		} else {
			String msg = "SAFS -Dsafs.config.paths is not available for this instance.";
			Log.warn(msg);
			System.out.println(msg);
			return;
		}
		
		ConfigureInterface config = null;
		for(int i = 0; i < paths.length; i++){
			File f = new CaseInsensitiveFile(paths[i]).toFile();
			if(f.exists()){
				if(config == null){
					config = new ConfigureFile(f);
				} else {
					config.addConfigureInterface(new ConfigureFile(f));
				}
			}			
		}
		TestRecordHelper.setConfig(config);
  }
  
  /** Instantiate the HookConfig, used to get settings from ConfigureInterface. <br>
   * Custom Hooks may override this method to provide their own HookConfig.<br> 
   * <b>Note: This should be called after {@link #initConfigPaths()}. </b>
   * @see #initConfigPaths() */
  protected void instantiateHookConfig(){
	  hookconfig = new DefaultHookConfig(TestRecordHelper.getConfig());
  }
  
  /**
   * Call {@link #initConfigPaths()} to initialize the ConfigureInterface instance.<br>
   * Call {@link #instantiateHookConfig()} to initialize the HookConfig instance.<br>
   * Call {@link HookConfig#checkConfiguration()} to check configuration settings for Hook.<br>
   */
  public void checkConfiguration(){
	  initConfigPaths();
	  instantiateHookConfig();
	  hookconfig.checkConfiguration();
  }

  /**
   * Insert this SAFS Engine hook into the STAF system.  
   * The hook will then be enabled and ready for test events from any STAF client
   * using the correct Event protocols.
   **/
  public void start () {

    if (getHelper() == null) 
        throw new SAFSProcessorInitializationException 
        (getClass().getName()+":STAFHelper not initialized for this process!");
    
    boolean shutdown = false;
    String  testrecord = null;                
    int rc = 99;  // 99 is just some bogus RC
    
    
    try {
      helper.resetHookEvents(semaphore_name);
      
      if (utils == null) utils = getGUIUtilities();
      if (data == null) data = getTRDData();
      data.setSTAFHelper(helper);
      data.setDDGUtils(utils);
      utils.setTestRecordData(data);
      log.setSTAFHelper(helper);

      if (processor == null) processor = getRequestProcessor();
            
      do{
        helper.postEvent(semaphore_name + "Ready");

        try {
          testrecord = helper.getNextHookTestEvent(semaphore_name, trd_name);
          if (testrecord.equals(SHUTDOWN_RECORD)) {
            shutdown = true;
          }
        } catch (SAFSException safsex) {
          log.logMessage(null, safsex.toString(), WARNING_MESSAGE);
          shutdown = true;
        }

        if (!shutdown) {
          data.reinit(); // reset the data to starting point
          try{
            data.setInstanceName(trd_name);
            data.populateDataFromVar();
            
            try{ processor.doRequest();}
            catch (SAFSRuntimeException ex) {
                long response = evaluateRuntimeException (ex);
                if (response == REQUEST_USER_STOPPED_SCRIPT_REQUEST) shutdown = true;
            } 
            data.sendbackResponse();
          } 
          catch (SAFSException ex) {
            System.err.println( errorText.convert(GENERIC_ERROR,
                                                 "ERROR :"+ ex.getMessage(),
                                                  ex.getMessage()));
          }
        }

        helper.setHookTestResults(semaphore_name);
        helper.resetEvent(semaphore_name + "Running");                
      } while(!shutdown);                

      helper.resetHookEvents(semaphore_name);
      helper.postEvent(semaphore_name +"Shutdown");
      
    } catch(SAFSException e){
        System.err.println( errorText.convert(STAF_ERROR,
                                             "STAF ERROR :"+ e.getMessage(),
                                              e.getMessage()));    
    } finally {    	
      if (hook_shutdown() && allowSystemExit()) System.exit(0);
    }
  }

  /**
   * Evaluate if the runtime hook should shutdown, proceed, or some other action 
   * following the receipt of a RuntimeException.
   * <p>
   * By default, this implementation will proceed with testing.  Subclasses may 
   * wish to override this function to evaluate the RuntimeException and react 
   * accordingly.
   */
  protected long evaluateRuntimeException(RuntimeException ex){
  	  return REQUEST_PROCEED_TESTING;
  }

  /**
   * @return true if we detect the external SAFS_DRIVER_CONTROL variable has been set to 'SHUTDOWN_HOOK'
   */
  protected boolean driverShutdownRequest(){
	  String result = null;
	  try{ result = helper.getVariable(DriverInterface.DRIVER_CONTROL_VAR); }
	  catch(Exception x){}
	  return SHUTDOWN_RECORD.equalsIgnoreCase(result);
	}
	
  
  /**
   * Perform final hook shutdown activities before the check for allowSystemExit.
   * <p>
   * This default implementation simply unregisters our STAFProcessHelper if 
   * it is not null.  Subclasses may wish to provide additional functionality 
   * before calling super.hook_shutdown().
   * 
   * @return The default implementation will return true allowing the additional 
   * check of allowSystemExit.  If a subclass overrides and returns false then 
   * the attempt to allowSystemExit will not happen.
   * 
   * @see #allowSystemExit()
   */
  protected boolean hook_shutdown(){
    // ** we may want to allow the helper to be unregistered separately, like 
    // ** by some main() or elsewhere if post-hook STAF utilization is to occur.    
    try { if (helper != null) STAFProcessHelpers.unRegisterHelper(process_name); }
    catch(SAFSException e){;}
    helper = null;
    return true;
  }
  
  /**
   * Evaluate if the runtime hook should exit with System.exit(0).
   * <p>
   * The default implementation will return true allowing the System.exit(0).  
   * Subclasses may wish to override this function to prevent the execution 
   * of System.exit(0) when this is not appropriate.
   */
  protected boolean allowSystemExit(){ return true; }
      
}
