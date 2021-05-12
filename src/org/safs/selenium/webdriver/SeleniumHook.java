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
package org.safs.selenium.webdriver;
/**
 * History for developer:
 * APR 08, 2015    (Lei Wang) Modify method setSystemProperties(): Call StringUtils.getSystemProperty to handle configuration values
 *                          and set them to system properties.
 * APR 25, 2016    (Lei Wang) Modify start() etc.: Profit the configuration settings ability in JavaHook.
 */

import org.safs.DDGUIUtilities;
import org.safs.JavaHook;
import org.safs.Log;
import org.safs.ProcessRequest;
import org.safs.Processor;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.TestRecordHelper;
import org.safs.logging.ApacheLogUtilities;
import org.safs.logging.LogUtilities;
import org.safs.selenium.SeleniumHookConfig;
import org.safs.selenium.webdriver.lib.SelectBrowser;
import org.safs.tools.drivers.ConfigureInterface;
import org.safs.tools.drivers.DriverConstant.SeleniumConfigConstant;

/**
 * This class is our STAF-enabled JavaHook (Selenium WebDriver) for the "SAFS/Selenium" Engine.
 * This is the class that registers with STAF and responds to the STAF-based SAFS Engine protocol.
 * <p>
 * The STAF name predefined for this Selenium Engine is "SAFS/Selenium".
 * <p>
 * As of this writing, this hook uses the ApacheLogUtilities subclass of LogUtilities.  This allows
 * us to use additional WebDriver and Selenium Builder/Interpreter features that require an
 * apache.commons.logging.Log Interface.
 *
 * <p>
 * The TestStepProcessor is the standard org.safs.TestStepProcessor.
 * <p>
 * The SAFS/Selenium engine does require Selenium-specific implementations of
 * <ul>
 * <li>org.safs.selenium.webdriver.WebDriverGUIUtilities
 * <li>org.safs.selenium.webdriver.WDTestRecordHelper
 * </ul>
 * <p>
 * If the engine is being launched manually or via a batch file or script then the following
 * Java System property must be set on the Java command line:
 * <p>
 * <ul>-Dsafs.config.paths=&lt;testpath>;&lt;projectpath>;&lt;driverpath></ul>
 * <p>
 * <i>testpath</i> is the file path to the test configuration INI file,<br>
 * <i>projectpath</i> is the file path to the project configuration INI file,<br>
 * <i>driverpath</i> is the file path to the SAFS driver configuration INI file.
 * <p>
 * At least one INI file path must be specified or the engine will abort due to insufficient
 * configuration information being provided.
 * <p>
 * The configuration file(s) can provide default target browser information:
 * <ul><p>
 * <li>BROWSER=&lt;Selenium browser id> Ex: BROWSER="explorer" or BROWSER="chrome" or BROWSER="firefox"
 * </ul>
 * <p>
 * <p>
 * The configuration file(s) can optionally also contain:
 * <ul><p>
 * <li>GATEWAYHOST=&lt;web proxy domain> Ex: GATEWAYHOST=your.normal.gateway.com
 * <li>GATEWAYPORT=&lt;web proxy port> Example: GATEWAYPORT=80 &lt;your normal gatewory port #>
 * <li>PROXY_BYPASS_ADDRESS=&lt;proxy bypass address> Example: PROXY_BYPASS_ADDRESS=localhost,127.0.0.1,mymachine.name
 * <li>BROWSER_REMOTE=false|true
 * </ul>
 * <p>
 * You can refer to the <a href="/sqabasic2000/JSAFSFrameworkContent.htm#configfile">[SAFS_SELENIUM]</a>
 * setion in SAFSDRIVER Configuration. <small><i>(See Settings for Selnium2.0 WebDriver)</i></small>
 * <p>
 * Assuming everything is properly CLASSPATHed and the config file(s) have proper settings
 * then the Engine can then be launched with:
 * <ul><p>
 * %SAFSDIR%/jre/bin/java -Dsafs.config.paths=&lt;paths> org.safs.selenium.webdriver.SeleniumHook
 * </ul>
 * <p>
 *
 *   <br>   JAN 16, 2007    (Carl Nagle) Initial Release.
 * @see WebDriverGUIUtilities
 * @see WDTestRecordHelper
 * @see org.safs.selenium.spc.SPC
 */
public class SeleniumHook extends JavaHook {

	/** Our predefined STAF process name: "SAFS/Selenium" */
	public static final String SELENIUM_COMMANDS = "SAFS/Selenium";
	public static String SAFS_SELENIUM_SERVER_BOOTUP_READY = "SAFS_SELENIUM_SERVER_BOOTUP_READY";

	/**
	 * No-arg constructor for SeleniumHook
	 * Simply calls the super()
	 */
	public SeleniumHook() {
		super();
	}

	/**
	 * Constructor for SeleniumHook
	 * Simply calls super(process_name)
	 *
	 * @param process_name the ID to use when registering with STAF.
	 * Normally this would be our predefined name "SAFS/Selenium".
	 */
	public SeleniumHook(String process_name) {
		super(process_name);
	}

	/**
	 * Constructor for SeleniumHook
	 * Simply calls super(process_name, logs)
	 *
	 * @param process_name the ID to use when registering with STAF.
	 * Normally this would be our predefined name "SAFS/Selenium".
	 *
	 * @param logs the LogUtilities to be used by the Engine.  Currently we can
	 * use the default org.safs.LogUtilities
	 *
	 * @see org.safs.LogUtilites
	 */
	public SeleniumHook(String process_name, ApacheLogUtilities logs) {
		super(process_name, logs);
	}

	/**
	 * Constructor for SeleniumHook
	 * Simply calls super(process_name, trd_name)
	 *
	 * @param process_name the ID to use when registering with STAF.
	 * Normally this would be our predefined name "SAFS/Selenium".
	 *
	 * @param trd_name -- The root name for specific TestRecordData to reference
	 * in STAF.  This should typically be STAFHelper.SAFS_HOOK_TRD which is used
	 * by default.
	 *
	 * @see org.safs.STAFHelper#SAFS_HOOK_TRD
	 */
	public SeleniumHook(String process_name, String trd_name) {
		super(process_name, trd_name);
	}


	/**
	 * Constructor for SeleniumHook
	 * Simply calls super(process_name, trd_name, logs)
	 *
	 * @param process_name the ID to use when registering with STAF.
	 * Normally this would be our predefined name "SAFS/Selenium".
	 *
	 * @param trd_name -- The root name for specific TestRecordData to reference
	 * in STAF.  This should typically be STAFHelper.SAFS_HOOK_TRD which is used
	 * by default.
	 *
	 * @param logs the LogUtilities to be used by the Engine.  Currently we can
	 * use the default org.safs.LogUtilities
	 *
	 * @see org.safs.STAFHelper#SAFS_HOOK_TRD
	 * @see org.safs.LogUtilites
	 */
	public SeleniumHook(String process_name, String trd_name,
			LogUtilities logs) {

		super(process_name, trd_name, logs);
	}

	/**
	 * Advanced Constructor for SeleniumHook.
	 * Simply calls super(process_name, trd_name, logs, trd_data, gui_utils, aprocessor)
	 *
	 * @param process_name the ID to use when registering with STAF.
	 * Normally this would be our predefined name "SAFS/Selenium".
	 *
	 * @param trd_name -- The root name for specific TestRecordData to reference
	 * in STAF.  This should typically be STAFHelper.SAFS_HOOK_TRD which is used
	 * by default.
	 *
	 * @param logs the LogUtilities to be used by the Engine.  Currently we can
	 * use the default org.safs.LogUtilities
	 *
	 * @param trd_data -- ATestRecordHelper to hold TestRecordData.
	 * The Selenium engine expects an instance of ATestRecordHelper.
	 *
	 * @param gui_utils -- WebDriverGUIUtilities the hook will use for handling components.
	 * The Selenium engine expects an instance of WebDriverGUIUtilities.
	 *
	 * @param aprocessor -- ProcessRequest object the hook will use for routing records.
	 * The Selenium engine uses the standard ProcessRequest class and a standard TestStepProcessor.
	 *
	 * @see org.safs.STAFHelper#SAFS_HOOK_TRD
	 * @see org.safs.LogUtilites
	 * @see WDTestRecordHelper
	 * @see WebDriverGUIUtilities
	 * @see org.safs.ProcessRequest
	 * @see org.safs.TestStepProcessor
	 */
	public SeleniumHook (String process_name, String trd_name,
			LogUtilities logs,
			TestRecordHelper trd_data,
			DDGUIUtilities gui_utils,
			ProcessRequest aprocessor) {

		super(process_name, trd_name, logs, trd_data, gui_utils, aprocessor);
	}


	/**
	 * Use this method to retrieve/create the current/default TestRecordHelper instance.
	 * If the TestRecordHelper has not been set this routine will instance a
	 * new org.safs.selenium.ATestRecordHelper and populate it with a STAFHelper via
	 * getHelper() and a DDGUIUtilities via getGUIUtilities.  Note that the call
	 * to getGUIUtilities may force the instantiation of the default WebDriverGUIUtilities
	 * if one has not already been set.
	 * <p>
	 * Note that there is a known circular execution between getTRDData and
	 * getGUIUtilities if neither was previously set.  Each routine
	 * calls the other which may result in a second call to the other.
	 * This has not been a problem.
	 *
	 * @return TestRecordHelper which should be an instanceof org.safs.selenium.ATestRecordHelper
	 *
	 * @see WDTestRecordHelper
	 * @see #getHelper()
	 * @see #getGUIUtilities()
	 * @see JavaHook#getTRDData()
	 */
	@Override
	public TestRecordHelper getTRDData() {
		if (data==null) {
			data=new WDTestRecordHelper();
			data.setSTAFHelper(getHelper());
			data.setDDGUtils(getGUIUtilities());
		}
		return data;
	}


	/**
	 * Use this method to retrieve/create the current/default DDGUIUtilities instance.
	 * If the DDGUIUtilities has not been set this routine will instance a
	 * new org.safs.selenium.webdriver.WebDriverGUIUtilities and populate it with a STAFHelper via
	 * getHelper() and a TestRecordHelper via getTRDData().  Note that the call
	 * to getTRDData() may force the instantiation of the default ATestRecordHelper
	 * if one has not already been set.
	 * <p>
	 * The WebDriverGUIUtilities talk with our embedded proxies over a SAFS RMI bridge.
	 * The local RMI server (AServerImpl) is instanced if not already set.
	 * <p>
	 * Note that there is a known circular execution between getTRDData and
	 * getGUIUtilities if neither was previously set.  Each routine
	 * calls the other which may result in a second call to the other.
	 * This has not been a problem.
	 *
	 * @return DDGUIUtilities which should be an instanceof org.safs.selenium.webdriver.WebDriverGUIUtilities.
	 *
	 * @see JavaHook#getGUIUtilities()
	 * @see #getHelper()
	 * @see #getTRDData()
	 * @see WebDriverGUIUtilities
	 * @see SServerImpl
	 */
	@Override
	public DDGUIUtilities getGUIUtilities() {
		if (utils==null) {
			utils=new WebDriverGUIUtilities();
			utils.setSTAFHelper(getHelper());
			utils.setTestRecordData(getTRDData());
		}
		return utils;
	}


	/**
	 * Use this method to retrieve/create the current/default LogUtilities instance.
	 * If the LogUtilities has not been set this routine will instance a
	 * new org.safs.logging.ApacheLogUtilities.
	 *
	 * @return LogUtilities which should be an instanceof org.safs.logging.ApacheLogUtilities.
	 *
	 * @see JavaHook#getLogUtilities()
	 * @see org.safs.logging.LogUtilities
	 */
	@Override
	public LogUtilities getLogUtilities() {
		if(log==null) {
			try {
				log=new ApacheLogUtilities(null, getTRDData().getFac());
			}
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
	public Processor getEngineDriverCommandProcessor() {
        return new DCDriverCommand();
	}

	/**
	 * Subclasses should instantiate an engine-specific TestStepProcessor Processor here, if any.
	 * Other generic SAFS Processors, if any, will get chained to this primary Processor.
	 *
	 * @return Engine-specific TestStepProcessor Processor instance, or null.
	 */
	protected Processor getEngineTestStepProcessor(){
		return new WDTestStepProcessor();
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
	 * Set the configurations to System-Properties.<br>
	 * If the System-Properties does NOT contain the 'property', then get the value from the ConfigureInterface and set to System-Properties;<br>
	 * Otherwise, keep the value in the System-Properties (don't override by the value from ConfigureInterface).<br>
	 * The checked properties are listed as following:<br>
	 * <ul>
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_BROWSER_NAME}
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_BROWSER_REMOTE}
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_SELENIUM_HOST}
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_SELENIUM_PORT}
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_PROXY_HOST}
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_PROXY_PORT}
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_PROXY_BYPASS}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_SELENIUMSERVER_JVM_Xmx}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_SELENIUMSERVER_JVM_Xms}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_SELENIUMSERVER_JVM_OPTIONS}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_SELENIUMSERVER_JVM}
	 * </ul>
	 * @param config ConfigureInterface, containing the configuration initial parameters
	 * @deprecated Call {@link SeleniumHookConfig#setSystemProperties(ConfigureInterface)} instead.
	 */
	@Deprecated
	public static void setSystemProperties(ConfigureInterface config){
		SeleniumHookConfig.setSystemProperties(config);
	}

	@Override
	protected void instantiateHookConfig(){
		hookconfig = new SeleniumHookConfig(TestRecordHelper.getConfig());
	}

	@Override
	public void start(){
		checkConfiguration();

		try{
			helper.setSTAFVariable(SAFS_SELENIUM_SERVER_BOOTUP_READY, "true");
		}catch(Exception e){
			Log.error("Error starting the selenium server." +e.getMessage(),e);
			//Should we exit?
		}
		try{
			super.start();
		}catch(Exception x){
			Log.debug(x.getMessage());
			stopJavaHOOK();
		}
	}

	/**
	 * Used by SPC to imitate sending a SHUTDOWN_RECORD to JavaHook<br>
	 * who is waiting in getNextHookTestEvent() for a record-dispatched event.<br>
	 */
	public void stopJavaHOOK(){
		Log.info(" processing user-initiated test abort shutdown...");
		try{
			if (helper != null){
				//Tell the JavaHook to stop running
				data.setInputRecord(SHUTDOWN_RECORD);
				//If the service SAFSVAR has not been started, method postNextHookTestEvent()
				//will throw exception, as it will store the test-record by SAFSVAR.
				helper.postNextHookTestEvent(semaphore_name,STAFHelper.SAFS_HOOK_TRD, data);
			}
		}catch(SAFSException ex){
			Log.error("*** ERROR *** during User Abort Test processing: "+ ex.getMessage(),ex);
		}finally{
			try {
				helper.resetHookEvents(semaphore_name);
				helper.postEvent(semaphore_name +"Shutdown");
				helper.releaseSTAFMutex(STAFHelper.SAFS_HOOK_TRD+"TRD");
			} catch (SAFSException e) {
				Log.error("*** ERROR *** during User Abort Test processing: "+ e.getMessage(),e);
			}
			if (hook_shutdown() && allowSystemExit()) System.exit(0);
		}
	}

	/**
	 * Perform final hook shutdown activities.
	 * This is called before allowSystemExit.
	 * @return the value returned from super.hook_shutdown()
	 * @see JavaHook#hook_shutdown()
	 * @see #allowSystemExit()
	 */
	@Override
	protected boolean hook_shutdown(){
		//shutdown Selenium Server
		Log.info("SAFS SeleniumHook is shutting down.");
		try{
			helper.setSTAFVariable(SAFS_SELENIUM_SERVER_BOOTUP_READY, "false");
		}catch(Exception x){
			Log.warn("SeleniumHook shutdown: met Exception "+x.getMessage());
		}
		return super.hook_shutdown();
	}

	/**
	 * Launches a default instance of the SAFS/Selenium engine.
	 * The "hook" is instanced and registered with STAF--which must already be running.
	 * The hook is started and will show up as "Ready" to SAFS Drivers once initialization
	 * is complete.
	 * <p>
	 * Assuming everything is properly CLASSPATHed, the default SAFS/Selenium Engine can be
	 * launched simply with:
	 * <ul>
	 * %SAFSDEV%/jre/bin/java org.safs.selenium.SeleniumHook
	 * </ul>
	 * However, more than likely additional command-line parameters and INI config file settings would be used.
	 * <p>
	 * @param args --Typically, there are no Class-specific command-line args.
	 * <p>
	 * These are different than the JVM Arguments passed to the JVM invocation preceding the classname on the command-line.
	 * <p>
	 */
	public static void main (String[] args) {
		Log.ENABLED = true;
		Log.setLogLevel(Log.DEBUG);

		System.out.println("SAFS/Selenium Hook starting...");
		ApacheLogUtilities logs= new ApacheLogUtilities();

		SeleniumHook hook = new SeleniumHook(SELENIUM_COMMANDS, logs);
		hook.setHelper(SELENIUM_COMMANDS);
		hook.getTRDData();
		hook.getRequestProcessor();
		// HOOK INITIALIZATION COMPLETE
		hook.start();
	}
}

