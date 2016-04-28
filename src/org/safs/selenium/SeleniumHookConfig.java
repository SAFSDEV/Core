/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * APR 25, 2016    (Lei Wang) Initial release.
 */
package org.safs.selenium;

import org.safs.DefaultHookConfig;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.SelectBrowser;
import org.safs.tools.drivers.ConfigureInterface;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverConstant.SeleniumConfigConstant;

public class SeleniumHookConfig extends DefaultHookConfig {

	/**
	 * @param config
	 */
	public SeleniumHookConfig(ConfigureInterface config) {
		super(config);
	}

	/**
	 * Set the configurations to System-Properties.<br>
	 * The checked properties are listed as below:<br>
	 * <ul>
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_BROWSER_NAME}
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_BROWSER_REMOTE}
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_SELENIUM_HOST}
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_SELENIUM_PORT}
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_PROXY_HOST}
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_PROXY_PORT}
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_PROXY_BYPASS}
	 * <li> {@link SeleniumConfigConstant#SELENIUMSERVER_JVM_Xmx}
	 * <li> {@link SeleniumConfigConstant#SELENIUMSERVER_JVM_Xms}
	 * <li> {@link SeleniumConfigConstant#SELENIUMSERVER_JVM_OPTIONS}
	 * <li> {@link SeleniumConfigConstant#SELENIUMSERVER_JVM}
	 * </ul>
	 * 
	 * @see #setSystemProperties(ConfigureInterface)
	 */
	public void checkConfiguration() {
		super.checkConfiguration();

		setSystemProperties(config);
	}
	
	/**
	 * Set the configurations to System-Properties.<br>
	 * If the System-Properties does NOT contain the 'property', then get the value from the ConfigureInterface and set to System-Properties;<br>
	 * Otherwise, keep the value in the System-Properties (don't override by the value from ConfigureInterface).<br>
	 * The checked properties are listed as below:<br>
	 * <ul>
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_BROWSER_NAME}
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_BROWSER_REMOTE}
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_SELENIUM_HOST}
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_SELENIUM_PORT}
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_PROXY_HOST}
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_PROXY_PORT}
	 * <li> {@link SelectBrowser#SYSTEM_PROPERTY_PROXY_BYPASS}
	 * <li> {@link SeleniumConfigConstant#SELENIUMSERVER_JVM_Xmx}
	 * <li> {@link SeleniumConfigConstant#SELENIUMSERVER_JVM_Xms}
	 * <li> {@link SeleniumConfigConstant#SELENIUMSERVER_JVM_OPTIONS}
	 * <li> {@link SeleniumConfigConstant#SELENIUMSERVER_JVM}
	 * </ul>
	 * This method will be shared by EmbeddedSeleniumHookDriver.<br>
	 * @param config ConfigureInterface, containing the configuration initial parameters
	 */
	public static void setSystemProperties(ConfigureInterface config){
		try{
			//Set browserName
			StringUtils.getSystemProperty(SelectBrowser.SYSTEM_PROPERTY_BROWSER_NAME, 
					config, DriverConstant.SECTION_SAFS_SELENIUM, SelectBrowser.SYSTEM_PROPERTY_BROWSER_NAME);

			//Set browserRemote
			StringUtils.getSystemProperty(SelectBrowser.SYSTEM_PROPERTY_BROWSER_REMOTE, 
					config, DriverConstant.SECTION_SAFS_SELENIUM, SelectBrowser.SYSTEM_PROPERTY_BROWSER_REMOTE);

			//Set SELENIUM "remote server/grid" host and port
			StringUtils.getSystemProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_HOST, 
					config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.SELENIUMHOST,
					SeleniumConfigConstant.DEFAULT_SELENIUM_HOST);
			StringUtils.getSystemProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_PORT,
					config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.SELENIUMPORT,
					SeleniumConfigConstant.DEFAULT_SELENIUM_PORT);
			StringUtils.getSystemProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_NODE,
					config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.SELENIUMNODE);

			//Set The real Internet gateway (host, port and bypass-address)
			StringUtils.getSystemProperty(SelectBrowser.SYSTEM_PROPERTY_PROXY_HOST,
					config, DriverConstant.SECTION_SAFS_SELENIUM, DriverConstant.GATEWAYHOST);
			StringUtils.getSystemProperty(SelectBrowser.SYSTEM_PROPERTY_PROXY_PORT,
					config, DriverConstant.SECTION_SAFS_SELENIUM, DriverConstant.GATEWAYPORT);
			StringUtils.getSystemProperty(SelectBrowser.SYSTEM_PROPERTY_PROXY_BYPASS,
					config, DriverConstant.SECTION_SAFS_SELENIUM, DriverConstant.PROXY_BYPASS_ADDRESS);

			//Set maximum/minimum memory to use for SELENIUM server
			StringUtils.getSystemProperty(SeleniumConfigConstant.SELENIUMSERVER_JVM_Xmx,
					config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.SELENIUMSERVER_JVM_Xmx);
			StringUtils.getSystemProperty(SeleniumConfigConstant.SELENIUMSERVER_JVM_Xms,
					config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.SELENIUMSERVER_JVM_Xms);
			//Set JVM Options for SELENIUM server
			StringUtils.getSystemProperty(SeleniumConfigConstant.SELENIUMSERVER_JVM_OPTIONS,
					config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.SELENIUMSERVER_JVM_OPTIONS);
			//Set JVM for SELENIUM server
			StringUtils.getSystemProperty(SeleniumConfigConstant.SELENIUMSERVER_JVM,
					config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.SELENIUMSERVER_JVM);
			
		}catch(Exception e){
			IndependantLog.warn(StringUtils.debugmsg(false)+" Fail. Met "+StringUtils.debugmsg(e));
		}
	}

}
