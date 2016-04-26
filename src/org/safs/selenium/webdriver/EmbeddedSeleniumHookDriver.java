/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver;

import org.safs.DCTestRecordHelper;
import org.safs.DDGUIUtilities;
import org.safs.Log;
import org.safs.JavaHook;
import org.safs.Processor;
import org.safs.StringUtils;
import org.safs.TestRecordHelper;
import org.safs.logging.ApacheLogUtilities;
import org.safs.logging.LogUtilities;
import org.safs.selenium.webdriver.lib.SelectBrowser;
import org.safs.tools.drivers.ConfigureInterface;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.EmbeddedHookDriver;

/**
 * Default EmbeddedHookDriver subclass for the SAFS Selenium WebDriver Engine.
 * <p>
 * This is primarily a proof-of-concept at this time.
 * Users would use a subclass of this class for runtime execution inside the Selenium engine.
 * <p>
 * The class has the same config requirements as JSAFSDriver.  However, the new implementations of 
 * the EmbeddedHookDriver will attempt to use default values (test.ini) if 
 * @author Carl Nagle
 * @see EmbeddedHookDriver
 * @see org.safs.tools.drivers.JSAFSDriver
 */
public class EmbeddedSeleniumHookDriver extends EmbeddedHookDriver {

	static boolean enableLogs = false;
	
	/**
	 * Default Constructor
	 */
	public EmbeddedSeleniumHookDriver() {
		super("WebDriver"+HOOK_DRIVER_NAME);
	}

	/**
	 * Instantiates a default DCTestRecordHelper for this engine.
	 * @see JavaHook#getTRDData()
	 * @see DCTestRecordHelper
	 */
	@Override
	public TestRecordHelper getTRDData() {
          if (data == null){
            data = new WDTestRecordHelper();
            data.setSTAFHelper(getHelper());
            data.setDDGUtils(getGUIUtilities());
          }
          return data;
	}

	@Override
    public LogUtilities getLogUtilities(){
		if(log == null){
			try{log = new ApacheLogUtilities();}
		    catch(Exception x){}
		}
		return log;
	}
	
	@Override
	public Processor getEngineDriverCommandProcessor() {
        return new DCDriverCommand();
	}
	
	@Override
	public Processor getEngineTestStepProcessor() {
        return new WDTestStepProcessor();
	}
	
	@Override
	public DDGUIUtilities getGUIUtilities() {
		if(utils == null){
			utils = new WebDriverGUIUtilities();
			utils.setSTAFHelper(getHelper());
			utils.setTestRecordData(getTRDData());
		}
		return utils;
	}
	
	/**
	 * (TerenceXie) APR 20, 2016	Set the 'DismissUnexpectedAlerts' value based on command-line or INI file. 
	 */
	protected void executeDismissUnexpectedAlerts(){
		if(jsafs().getDismissUnexpectedAlerts()){
			SelectBrowser.dismissUnexpectedAlertsIEValue = "accept";
		} else {
			SelectBrowser.dismissUnexpectedAlertsIEValue = "ignore";
		}
	}
	
	public void start(){
		SeleniumHook.setSystemProperties(config());		
		executeDismissUnexpectedAlerts();
		super.start();
	}
	
	@Override
	public boolean shutdown(){
		
		/**
		 * Shutdown browser must be done by user
		 * 
		try{ SearchObject.shutdown(); }
		catch(Throwable t){
			Log.debug("WebDriver shutdown "+ t.getClass().getSimpleName()+": "+ t.getMessage());
		}	
		*/	
		return super.shutdown();
	}
}

