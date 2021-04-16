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
package org.safs.tools.drivers;
/**
 * History for developer:
 *
 * JAN 29, 2014    (Lei Wang) Expose embedded driver's ConfigureInterface object.
 * APR 26, 2016    (Lei Wang) Modify start(): Profit the configuration settings ability in JavaHook.
 *                          Add initConfigPaths(): Try to use the existing Configuration of the embedded driver.
 *
 */
import org.safs.DCGUIUtilities;
import org.safs.DDGUIUtilities;
import org.safs.JavaHook;
import org.safs.Log;
import org.safs.ProcessRequest;
import org.safs.Processor;
import org.safs.SAFSException;
import org.safs.SAFSSTAFRegistrationException;
import org.safs.STAFHelper;
import org.safs.SingletonSTAFHelper;
import org.safs.TestRecordHelper;
import org.safs.logging.LogUtilities;
import org.safs.model.tools.Driver;
import org.safs.staf.STAFProcessHelpers;
import org.safs.tools.counters.UniqueStringCounterInfo;
import org.safs.tools.status.StatusCounter;
import org.safs.tools.stringutils.StringUtilities;

/**
 * Allows custom JSAFS test development and execution inside a Java-based SAFS Engine.
 * Java-based SAFS Engines would need to implement an engine-specific subclass of this
 * EmbeddedHookDriver using a EmbeddedHookSTAFHelper to take advantage of this feature.
 * <p>
 * Usage is expected to be:
 * <p>
 * <pre><ul>
 * EmbeddedHookDriverSubclass hook = new EmbeddedHookDriverSubclass("UniqueName");
 * hook.run();
 * ..do stuff
 * hook.shutdown();
 * </ul></pre>
 * <p>
 * It is important to note that the JVM arg -Dsafs.project.config is now optional.
 * It is also possible to use -Dtestdesigner.project.config, or nothing.
 * If neither is provided, the engine will look for a "test.ini" file in the startup
 * working directory and also initially assume that might also be the Project Root directory.
 * @author Carl Nagle
 */
public class EmbeddedHookDriver extends JavaHook {

	/** "safs.project.config"<br>
	 * System Property identifying the path to an alternate project
	 * configuration file (other than safstid.ini in the default
	 * project configuration location.<br>
	 * JVM command line: -Dsafs.project.config=string|path **/
	public static final String SAFS_PROJECT_CONFIG = DriverConstant.PROPERTY_SAFS_PROJECT_CONFIG;
	/** "testdesigner.project.config"<br>
	 * System Property identifying the path to an alternate project
	 * configuration file (other than safstid.ini in the default
	 * project configuration location.<br>
	 * This property has a higher priority than {@link #SAFS_PROJECT_CONFIG}, if both
	 * of them are specified, then its value will be used.<br>
	 * JVM command line: -Dtestdesigner.project.config=string|path **/
	public static final String TESTDESIGNER_PROJECT_CONFIG = "testdesigner.project.config";
	/** "test.ini" <br>
	 * Default project's Configuration filename. **/
	public static final String TEST_INI_DEFAULT = DriverConstant.DEFAULT_CONFIGURE_FILENAME_TEST_INI;

	private static boolean running = false;
	private static boolean weStartedSTAF = false;

	JSAFSDriver jsafs;
	EmbeddedHookDriver hook;
	public static final String HOOK_DRIVER_NAME = "EmbeddedHookDriver";

	// the thread used for the running engine start() loop
	Thread hookThread = new Thread(){
		@Override
		public void run(){
			try{ hook.start();}
			catch(Exception x){
				Log.debug("EmbeddedDriver engine hook loop ending due to "+
			              x.getClass().getSimpleName()+": "+ x.getMessage(), x);
			}
		}
	};

	/**
	 * Instantiates a default instance with a default {@value #HOOK_DRIVER_NAME} engine name.
	 * Calls initialize()
	 * @see JavaHook#JavaHook(String)
	 * @see #initialize()
	 */
	public EmbeddedHookDriver(){
		this(HOOK_DRIVER_NAME);
	}

	/**
	 * Instantiate an instance using the provided engineName.
	 * Calls initialize()
	 * @param engineName process_name to use for the driver/engine STAF connection.
	 * @see JavaHook#JavaHook(String)
	 * @see #initialize()
	 */
	public EmbeddedHookDriver(String engineName){
		this(engineName, null);
	}

	/**
	 * Instantiate an instance using the provided engineName and LogUtilities
	 * Calls initialize()
	 * @param engineName process_name to use for the driver/engine STAF connection.
	 * @param LogUtilities to use.
	 * @see JavaHook#JavaHook(String, LogUtilities)
	 * @see #initialize()
	 */
	public EmbeddedHookDriver(String engineName, LogUtilities log){
		super(engineName, log);
		initialize();
	}

	/**
	 * Called automatically by the 3 Constructors.
	 * Subclasses may wish to override or extend this initialization.
	 * <p>
	 * sets the local hook field to the current hook driver instance and
	 * creates the internal JSAFSDriver instance.
	 * @see #setHelper(Object)
	 */
	protected void initialize(){
		hook = this;
		jsafs = new JSAFSDriver(process_name);
		Driver.setIDriver(jsafs);
		jsafs.systemExitOnShutdown = false;
		jsafs.removeShutdownHook();
	}

	/**
	 * Return the embedded JSAFSDriver (for advanced users).
	 * @return JSAFSDriver -- can be null if not yet initialized.
	 */
	public JSAFSDriver jsafs() {return jsafs;}

	/**
	 * return the Configure object so that subclass can get data from configuration files.<br>
	 * @return ConfigureInterface -- the Configure object
	 */
	protected ConfigureInterface config() {
		try{ return jsafs.getConfigureInterface();}
		catch(NullPointerException x){
			return null;}
	}

	/**
	 * Retrieve the TestRecordHelper used by the subclass.<br>
	 * This will almost always be overridden by an engine-specific subclass.
	 * <p>
	 * If no TestRecordHelper has been set (data == null), then the implementation is expected
	 * to instantiate and return a TestRecordHelper instance appropriate for the engine.
	 * <p>
	 * A newly instantiated TestRecordHelper should have critical helpers set:
	 * <p><code>
	 * <ul>
	 * <li>data.setSTAFHelper(getHelper());
	 * <li>data.setDDGUtils(getGUIUtilities());
	 * </ul></code>
	 **/
	@Override
	public TestRecordHelper getTRDData(){
		if (data==null) {
			data=new TestRecordHelper();
			data.setSTAFHelper(getHelper());
			data.setDDGUtils(getGUIUtilities());
		}
		return data;
	}

	/**
	 * Subclasses should override this method to retrieve/create the DDGUIUtilities instance
	 * needed by the specific engine subclass.
	 * <p>
	 * A newly instantiated DDGUIUtilities should have critical helpers set:
	 * <p><code>
	 * <ul>
	 * <li>utils.setSTAFHelper(getHelper());
	 * <li>utils.setTestRecordData(getTRDData());
	 * </ul></code>
	 * <p>
	 * Note that the call to getTRDData() may force the instantiation of the default
	 * TestRecordHelper if one has not already been set.
	 * <p>
	 * @return DDGUIUtilities which should be an instanceof org.safs.DDGUIUtilities subclass.
	 * This default superclass implementation instantiates a DCGUIUtilities object that cannot
	 * act on GUI components.
	 * @see #getHelper()
	 * @see #getTRDData()
	 * @see org.safs.DCGUIUtilities
	 */
	@Override
	public DDGUIUtilities getGUIUtilities() {
		if(utils == null){
			utils = new DCGUIUtilities();
			utils.setSTAFHelper(getHelper());
			utils.setTestRecordData(getTRDData());
		}
		return utils;
	}


	/**
	 * Use this method to retrieve/create the current/default LogUtilities instance.
	 * If the LogUtilities has not been set this routine will instance a
	 * new org.safs.LogUtilities.
	 *
	 * @return LogUtilities which should be an instanceof org.safs.LogUtilities.
	 *
	 * @see JavaHook#getLogUtilities()
	 * @see org.safs.LogUtilities
	 */
	@Override
	public LogUtilities getLogUtilities() {
		if(log==null) {
			try { log=new LogUtilities();}
			catch(Exception e) {}
		}
		return log;
	}

	/**
	 * Subclasses should instantiate an engine-specific DriverCommands Processor here.
	 * Other generic SAFS DriverCommand Processors will get chained to this primary Processor.
	 *
	 * @return Engine-specific DriverCommands Processor instance, or null.
	 */
	protected Processor getEngineDriverCommandProcessor(){
		return null;
	}

	/**
	 * Subclasses should instantiate an engine-specific TestStepProcessor Processor here, if any.
	 * Other generic SAFS Processors, if any, will get chained to this primary Processor.
	 *
	 * @return Engine-specific TestStepProcessor Processor instance, or null.
	 */
	protected Processor getEngineTestStepProcessor(){
		return null;
	}

	/**
	 * Subclasses should instantiate an engine-specific EngineCommandProcessor Processor here, if any.
	 * Other generic SAFS Processors, if any, will get chained to this primary Processor.
	 *
	 * @return Engine-specific EngineCommandProcessor Processor instance, or null.
	 */
	protected Processor getEngineEngineCommandProcessor(){
		return null;
	}

	/**
	 * This method to retrieve/create the current/default ProcessRequest instance.
	 * The user would not normally call or override this routine as it will be called
	 * internally.
	 * <p>
	 * If the ProcessRequest has not been set this routine will instance a
	 * new org.safs.ProcessRequest and populate it with the LogUtilities via
	 * getLogUtilities(), a TestRecordHelper via getTRDData(), and all Processors.
	 * <p>
	 * Any newly created ProcessRequest instance will invoke:
	 * <p><ul>
	 * getEngineDriverCommandProcessor()<br>
	 * getEngineTestStepProcessor()<br>
	 * getEngineEngineCommandProcessor()<br>
	 * </ul>
	 * <p>
	 * and attempt to initialize all Processors with the getLogUtilities() and getTRDData()
	 * calls and then chain all Processors appropriately.
	 * <p>
	 * Note that the first call to getTRDData() may force the instantiation of the default
	 * TestRecordHelper for the subclass, if one has not already been set.
	 * <p>
	 * The first call to getLogUtilities() may also force the instantiation of the default
	 * LogUtilities, if one has not already been set.
	 * <p>
	 * @return ProcessRequest which should be an instanceof org.safs.ProcessRequest
	 *
	 * @see TestRecordHelper
	 * @see #getTRDData()
	 * @see #getLogUtilities()
	 * @see org.safs.LogUtilities
	 * @see #getEngineDriverCommandProcessor()
	 * @see #getEngineTestStepProcessor()
	 * @see #getEngineEngineCommandProcessor()
	 */
	@Override
	public ProcessRequest getRequestProcessor() {
		if(processor==null){
	    	processor = new ProcessRequest(
            getTRDData(),                 // TestRecordHelper
            getLogUtilities());           // no custom test step support

            Processor linked = processor.getDriverCommandProcessor();
			linked.setLogUtilities(getLogUtilities());
			linked.setTestRecordData(getTRDData());

            Processor dc = getEngineDriverCommandProcessor();
            if(dc instanceof Processor){
            	dc.setLogUtilities(getLogUtilities());
            	dc.setTestRecordData(getTRDData());
	            dc.setChainedProcessor(linked);
	            processor.setDriverCommandProcessor(dc);
            }else{
            	processor.setDriverCommandProcessor(linked);
            }

            linked = processor.getTestStepProcessor();
            if(linked instanceof Processor){
    			linked.setLogUtilities(getLogUtilities());
    			linked.setTestRecordData(getTRDData());
            }
            Processor cf = getEngineTestStepProcessor();
            if(cf instanceof Processor){
            	cf.setLogUtilities(getLogUtilities());
            	cf.setTestRecordData(getTRDData());
            	cf.setChainedProcessor(linked);
            	processor.setTestStepProcessor(cf);
            }else{
            	processor.setTestStepProcessor(linked);
            }

            linked = processor.getEngineCommandProcessor();
            if(linked instanceof Processor){
    			linked.setLogUtilities(getLogUtilities());
    			linked.setTestRecordData(getTRDData());
            }
            Processor ec = getEngineEngineCommandProcessor();
            if(ec instanceof Processor){
            	ec.setLogUtilities(getLogUtilities());
            	ec.setTestRecordData(getTRDData());
            	ec.setChainedProcessor(linked);
            	processor.setEngineCommandProcessor(ec);
            }else{
            	processor.setEngineCommandProcessor(linked);
            }
		}
		return processor;
	}

    /**
     * Override superclass to delay STAFHelper initialization.
     */
    @Override
    protected void setProcessName (String process_name){
      	if ((this.process_name == null)&&(process_name != null)&&(process_name.length() > 0)){
      	  this.process_name = process_name;
      	  setSemaphoreName(process_name);
      	}
      	// cannot call setHelpers() until after config info is acquired.
      }

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
      @Override
	  public void setHelper(Object processhelper){
		if (helper instanceof STAFHelper && helper.isInitialized()){
			System.out.println("EmbeddedDriver.setHelper already initialized...exiting.");
			return;
		}
	  	if (processhelper == null) {
	  		System.out.println("EmbeddedDriver.setHelper processing null input...");
	  		if ((process_name == null)||(process_name.length() == 0)) {
	  	        throw new NullPointerException( getClass().getName() +
	  		    ":setHelper() must be a process_name String or an EmbeddedHookSTAFHelper instance!");
	  		}
	  		processhelper = process_name;
	  	}
	  	if (processhelper instanceof java.lang.String){
		  	System.out.println("EmbeddedDriver.setHelper attempting to use String: "+processhelper);
	  		String processname = (String) processhelper;
	  	    if ((process_name == null)&&(processname.length() > 0)){
	  	        process_name = processname;
	  	        setSemaphoreName(processname);
	  	    }
		  	System.out.println("EmbeddedDriver.setHelper using processname: "+processname);
			try{
				if(!STAFHelper.no_staf_handles){
					try{
						String temp = config().getNamedValue(DriverConstant.SECTION_STAF, "NOSTAF");
						if(temp != null) STAFHelper.no_staf_handles = StringUtilities.convertBool(temp);
					}catch(NullPointerException x){
					  	System.out.println("EmbeddedDriver.setHelper config() is not yet initialized!");
					}
				}
				if(STAFHelper.no_staf_handles){
		        	helper = STAFProcessHelpers.registerHelperClass(processname,EmbeddedHookSTAFHelper.class);
				}else{
		        	helper = STAFProcessHelpers.registerHelperClass(processname,STAFHelper.class);
				}
				SingletonSTAFHelper.setInitializedHelper(helper);
				helper.configEmbeddedServices(config()); // STAF could have already been started for Debug Log
	        }
	        catch (SAFSSTAFRegistrationException e) {
				try{
					if(STAFHelper.no_staf_handles){
						helper = STAFProcessHelpers.launchSTAFProcClass(processname, EmbeddedHookSTAFHelper.class);
					}else{
						helper = STAFProcessHelpers.launchSTAFProcClass(processname, STAFHelper.class);
					}
					SingletonSTAFHelper.setInitializedHelper(helper);
					weStartedSTAF = true;
					helper.configEmbeddedServices(config()); // STAF could have already been started for Debug Log
				}
				catch(org.safs.SAFSSTAFRegistrationException rx2){
					e.printStackTrace();
					System.out.println("EmbeddedHookDriver unable to launch STAF or register with STAF due to: "+
					                   e.getClass().getSimpleName()+": "+ e.getMessage());
					STAFHelper.configEmbeddedServices(config()); // STAF could have already been started for Debug Log
				}
	        }
	  	}
	  	else if (processhelper instanceof EmbeddedHookSTAFHelper){
		  	System.out.println("EmbeddedDriver.setHelper attempting to use EmbeddedHookSTAFHelper: "+processhelper);
	  		helper = (STAFHelper) processhelper;
	  		process_name = helper.getProcessName();
	  		setSemaphoreName(process_name);
			helper.configEmbeddedServices(config()); // STAF could have already been started for Debug Log
	  	}else{
	  		System.out.println("EmbeddedHookDriver requires an EmbeddedHookSTAFHelper for proper execution!");
	  		throw new NullPointerException("EmbeddedHookDriver requires an EmbeddedHookSTAFHelper for proper execution!");
	  	}
		if(helper instanceof STAFHelper && helper.isInitialized()) Log.setHelper(helper);
	  }

      /** Override the superclass's method, try to use the existing Configuration of the embedded driver.
       * If the existing Configuration is null, then call method of superclass. */
      @Override
	protected void initConfigPaths(){
    	  ConfigureInterface config = config();
    	  if(config!=null) TestRecordHelper.setConfig(config);
    	  else super.initConfigPaths();
      }

      @Override
      public void start(){
    	  if(log==null) log = getLogUtilities();
    	  checkConfiguration();
    	  super.start();
      }

	  /**
	   * Evaluate if the runtime hook should shutdown or continue.
	   * Currently, we only suggest shutdown on a USER_STOPPED_SCRIPT_REQUEST--
	   * which can be initiated by the SAFS Monitor or the value of the safs_driver_control
	   * variable.
	   */
	  @Override
	protected long evaluateRuntimeException(RuntimeException ex){
	  	  return (ex.getMessage().toLowerCase().contains(SHUTDOWN_RECORD.toLowerCase())) ?
	  			                                    REQUEST_USER_STOPPED_SCRIPT_REQUEST:
	  			                                    REQUEST_PROCEED_TESTING;
	  }


	/**
	 * Called to initialize the EmbeddedDriver and make it ready for use.
	 * The user is expected to call shutdown() when testing is completed.
	 * <p>
	 * Invokes:<br/>
	 * @see org.safs.tools.drivers.JSAFSDriver#removeShutdownHook()<br/>
	 * @see org.safs.tools.drivers.JSAFSDriver#validateRootConfigureParameters(boolean)<br/>
	 * @see org.safs.tools.drivers.JSAFSDriver#validateLogParameters()<br/>
	 * @see org.safs.tools.drivers.JSAFSDriver#initializeRuntimeInterface()<br/>
	 * @see org.safs.tools.drivers.JSAFSDriver#initializePresetVariables()<br/>
	 * @see org.safs.tools.drivers.JSAFSDriver#initializeMiscConfigInfo()<br/>
	 * @see org.safs.tools.drivers.JSAFSDriver#initializeRuntimeEngines()<br/>
	 * @see org.safs.tools.drivers.JSAFSDriver#openTestLogs()<br/>
	 * @see org.safs.tools.drivers.JSAFSDriver#statuscounter<br/>
	 */
	public void run(){
		if(running) return;
		jsafs.setUseSAFSMonitor(false);
		String safsDataServiceID = null;

		try{
			if( System.getProperty(TESTDESIGNER_PROJECT_CONFIG)!= null)
				try{ System.setProperty(SAFS_PROJECT_CONFIG, System.getProperty(TESTDESIGNER_PROJECT_CONFIG));}
			    catch(Throwable ignore){}
			if( System.getProperty(SAFS_PROJECT_CONFIG) == null ) System.setProperty(SAFS_PROJECT_CONFIG, TEST_INI_DEFAULT);
		    System.out.println("Validating Root Configure Parameters...");
		    jsafs.validateRootConfigureParameters(false);

		    // moved out of setProcessName for EmbeddedDrivers which might not use STAF.
		    setHelper(process_name);

		    System.out.println("Validating Test Parameters...");
		    jsafs.validateTestParameters();
		    System.out.println("Validating Log Parameters...");
		    jsafs.validateLogParameters();
		    System.out.println("Initializing Runtime Interfaces...");
		    jsafs.initializeRuntimeInterface();
	    	jsafs.launchSAFSMonitor();
		    jsafs.initializePresetVariables();
		    jsafs.initializeMiscConfigInfo();
		    jsafs.connectSAFSDataService();
		    jsafs.initializeRuntimeEngines();
		    jsafs.openTestLogs();
			jsafs.statuscounter = jsafs.statuscounts instanceof StatusCounter ? (StatusCounter)jsafs.statuscounts: new StatusCounter();
			jsafs.counterInfo = jsafs.counterInfo == null ?
					new UniqueStringCounterInfo(jsafs.cycleLog.getStringID(), DriverConstant.DRIVER_CYCLE_TESTLEVEL):
					jsafs.counterInfo;
			data = jsafs.initTestRecordData(getTRDData());

			jsafs.phoneHome();

			hookThread.setDaemon(true);
			hookThread.start();
			running = true;
		}
		catch(IllegalArgumentException iae){ System.err.println("Driver "+ iae.getClass().getSimpleName()+": "+ iae.getMessage());	}
		catch(Exception catchall){
			System.err.println("\n****  Unexpected CatchAll Exception handler  ****");
			System.err.println(catchall.getMessage());
			catchall.printStackTrace();
		}
	}

	@Override
	protected boolean allowSystemExit(){ return false; }


	  /**
	   * Perform final engine hook shutdown activities AND shutdown the internal
	   * JSAFSDriver.  This should shutdown all SAFS logs and services started by
	   * this process.
	   * <p>
	   * This default implementation unregisters our STAFProcessHelper if
	   * it is not null.  Subclasses may wish to provide additional functionality
	   * before calling super.shutdown().
	   *
	   * @return The default implementation will return true if no errors or exceptions are uncaught.
	   */
	  public boolean shutdown(){
		  if(hookThread.isAlive()){
			  Log.info("EmbeddedDriver attempting shutdown of engine hook '"+ process_name +"'");
			  try{
				  data.setInputRecord(SHUTDOWN_RECORD);
				  helper.postNextHookTestEvent(process_name, trd_name, data);
			  }catch(Exception x){
				  Log.debug("EmbeddedDriver hookThread shutdown "+x.getClass().getSimpleName()+": "+x.getMessage(), x);
			  }
		  }
		  Log.info("EmbeddedDriver shutting down embedded JSAFSDriver...");
		  try{
			  Driver.setIDriver(null);
			  jsafs.shutdown();
		  }catch(NullPointerException x){
			  Log.debug("EmbeddedDriver JSAFS shutdown "+x.getClass().getSimpleName()+": "+x.getMessage(), x);
		  }
		  catch(Throwable t){
			  Log.info("EmbeddedDriver JSAFS shutdown Throwable "+t.getClass().getSimpleName()+": "+t.getMessage());
			  t.printStackTrace();
		  }
	      // ** we may want to allow the helper to be unregistered separately, like
	      // ** by some main() or elsewhere if post-hook STAF utilization is to occur.
	      try {
	    	  if (helper != null) {
	    		  Log.debug("EmbeddedDriver disconnecting from STAF for process_name '"+ process_name +"'");
	    		  STAFProcessHelpers.unRegisterHelper(process_name);
	    	  }
	      }
	      catch(SAFSException ignore){;}
	      helper = null;
	      running = false;
	      if(weStartedSTAF){
	    	  STAFHelper.shutdownEmbeddedServices();
	    	  try{ STAFProcessHelpers.shutdownSTAFProc(); }
	    	  catch(SAFSException ignore){}
	      }

	      return true;
	  }
}
