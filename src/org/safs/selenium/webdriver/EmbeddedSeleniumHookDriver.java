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
 * History for developer:
 * APR 25, 2016    (Lei Wang) Remove start() and executeDismissUnexpectedAlerts(): Profit the configuration settings ability in JavaHook.
 */
package org.safs.selenium.webdriver;

import org.safs.DCTestRecordHelper;
import org.safs.DDGUIUtilities;
import org.safs.JavaHook;
import org.safs.Processor;
import org.safs.TestRecordHelper;
import org.safs.logging.ApacheLogUtilities;
import org.safs.logging.LogUtilities;
import org.safs.selenium.SeleniumHookConfig;
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
	public static final String HOOK_DRIVER_NAME = "WebDriver"+EmbeddedHookDriver.HOOK_DRIVER_NAME;

	/**
	 * Default Constructor
	 */
	public EmbeddedSeleniumHookDriver() {
		super(HOOK_DRIVER_NAME);
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
			try{log = new ApacheLogUtilities(null, getTRDData().getFac());}
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

	@Override
	protected void instantiateHookConfig(){
		hookconfig = new SeleniumHookConfig(TestRecordHelper.getConfig());
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

