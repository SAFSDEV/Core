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
 * Logs for developers, not published to API DOC.
 *
 * History:
 * APR 25, 2016    (Lei Wang) Initial release.
 * MAR 07, 2017    (Lei Wang) Read setting of browser-drivers.
 * APR 16, 2018    (Lei Wang) Modified setSystemProperties(): check the 'selenium server launch script'.
 */
package org.safs.selenium;

import org.safs.DefaultHookConfig;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.RemoteDriver;
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
	 * <li> {@link SeleniumConfigConstant#PROPERTY_SELENIUMSERVER_JVM_Xmx}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_SELENIUMSERVER_JVM_Xms}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_SELENIUMSERVER_JVM_OPTIONS}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_SELENIUMSERVER_JVM}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_CONSOLE_STATE}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_WEB_DRIVERS}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_CONNECTION_TEST_COMMAND}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_CONNECTION_TEST_MAX_DURATION}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_CONNECTION_TEST_MAX_TRY}
	 * </ul>
	 *
	 * @see #setSystemProperties(ConfigureInterface)
	 */
	@Override
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
	 * <li> {@link SeleniumConfigConstant#PROPERTY_SELENIUMSERVER_JVM_Xmx}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_SELENIUMSERVER_JVM_Xms}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_SELENIUMSERVER_JVM_OPTIONS}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_SELENIUMSERVER_JVM}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_CONSOLE_STATE}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_WEB_DRIVERS}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_CONNECTION_TEST_COMMAND}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_CONNECTION_TEST_MAX_DURATION}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_CONNECTION_TEST_MAX_TRY}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_DELAY_GET_CONTENT}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_BYPASS_FRAME_RESET}
	 * <li> {@link SeleniumConfigConstant#ITEM_BYPASS_ROBOT_ACTION}
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

			//Set The real Internet gateway (host, port and bypass-address)
			StringUtils.getSystemProperty(SelectBrowser.SYSTEM_PROPERTY_PROXY_HOST,
					config, DriverConstant.SECTION_SAFS_SELENIUM, DriverConstant.GATEWAYHOST);
			StringUtils.getSystemProperty(SelectBrowser.SYSTEM_PROPERTY_PROXY_PORT,
					config, DriverConstant.SECTION_SAFS_SELENIUM, DriverConstant.GATEWAYPORT);
			StringUtils.getSystemProperty(SelectBrowser.SYSTEM_PROPERTY_PROXY_BYPASS,
					config, DriverConstant.SECTION_SAFS_SELENIUM, DriverConstant.PROXY_BYPASS_ADDRESS);

			//Set selenium-server related stuffs
			setSeleniumServerSettings(config);

			//Set the delay (milliseconds) waiting for the refresh of a webelement before getting content from it.
			StringUtils.getSystemProperty(SeleniumConfigConstant.PROPERTY_DELAY_GET_CONTENT,
					config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.ITEM_DELAY_WAIT_REFRESH, String.valueOf(SeleniumConfigConstant.DEFAULT_DELAY_GET_CONTENT));

			//Set the 'bypass.frame.reset' property
			String bypassFramesReset = StringUtils.getSystemProperty(SeleniumConfigConstant.PROPERTY_BYPASS_FRAME_RESET,
					config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.ITEM_BYPASS_FRAME_RESET, String.valueOf(SeleniumConfigConstant.DEFAULT_BYPASS_FRAME_RESET));
			if(Boolean.parseBoolean(bypassFramesReset)){
				IndependantLog.debug("User has set the '"+SeleniumConfigConstant.PROPERTY_BYPASS_FRAME_RESET+"' to true, "
						+ "selenium will not switch back to the topmost frame, it will search on the last switched Frame.");
			}

			//Set the 'bypass.robot.action' property
			String bypassRobot = StringUtils.getSystemProperty(SeleniumConfigConstant.PROPERTY_BYPASS_ROBOT_ACTION,
					config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.ITEM_BYPASS_ROBOT_ACTION, String.valueOf(SeleniumConfigConstant.DEFAULT_BYPASS_ROBOT_ACTION));
			if(Boolean.parseBoolean(bypassRobot)){
				IndependantLog.debug("User has set the '"+SeleniumConfigConstant.PROPERTY_BYPASS_ROBOT_ACTION+"' to true, "
						+ "our library will handle click ations directly by selenium, they will not be attempted by Robot.");
			}

			//Set the 'safs.selenium.rmi.port.forward' property
			String dockerPortForward = StringUtils.getSystemProperty(SeleniumConfigConstant.PROPERTY_RMI_PORT_FORWARD,
					config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.ITEM_RMI_PORT_FORWARD, String.valueOf(SeleniumConfigConstant.DEFAULT_RMI_PORT_FORWARD));
			if(Boolean.parseBoolean(dockerPortForward)){
				IndependantLog.debug("User has set the '"+SeleniumConfigConstant.PROPERTY_RMI_PORT_FORWARD+"' to true, "
						+ "our library will connect the RMI server running on docker container by 'localhost' and the mapped port");
			}

			//Set the 'safs.selenium.registry.port' property
			String registryPort = StringUtils.getSystemProperty(SeleniumConfigConstant.PROPERTY_REGISTRY_PORT,
					config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.ITEM_REGISTRY_PORT, String.valueOf(SeleniumConfigConstant.DEFAULT_REGISTRY_PORT));
			IndependantLog.debug("User has set the '"+SeleniumConfigConstant.PROPERTY_REGISTRY_PORT+"' to "+registryPort+", "
					+ "which is used to get the registry for looking up the RMI server ");

		}catch(Exception e){
			IndependantLog.warn(StringUtils.debugmsg(false)+" Fail. Met "+StringUtils.debugmsg(e));
		}
	}

	/**
	 * Read selenium server settings from VM parameter and from .ini configuration file; and set them as VM parameter for later use.
	 *
	 * @param config ConfigureInterface, containing the configuration initial parameters
	 */
	private static void setSeleniumServerSettings(ConfigureInterface config){
		//Set the 'server launch script'
		StringUtils.getSystemProperty(SeleniumConfigConstant.PROPERTY_SERVER_LAUNCH_SCRIPT,
				config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.ITEM_SERVER_LAUNCH_SCRIPT);

		//Set SELENIUM "remote server/grid" host and port
		StringUtils.getSystemProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_HOST,
				config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.SELENIUMHOST,
				SeleniumConfigConstant.DEFAULT_SELENIUM_HOST);
		StringUtils.getSystemProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_PORT,
				config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.SELENIUMPORT,
				SeleniumConfigConstant.DEFAULT_SELENIUM_PORT);
		StringUtils.getSystemProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_NODE,
				config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.SELENIUMNODE);

		//Set maximum/minimum memory to use for SELENIUM server
		StringUtils.getSystemProperty(SeleniumConfigConstant.PROPERTY_SELENIUMSERVER_JVM_Xmx,
				config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.SELENIUMSERVER_JVM_Xmx, SeleniumConfigConstant.DEFAULT_JVM_MEMORY_MAXIMUM);
		StringUtils.getSystemProperty(SeleniumConfigConstant.PROPERTY_SELENIUMSERVER_JVM_Xms,
				config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.SELENIUMSERVER_JVM_Xms, SeleniumConfigConstant.DEFAULT_JVM_MEMORY_MINIMUM);
		//Set JVM Options for SELENIUM server
		StringUtils.getSystemProperty(SeleniumConfigConstant.PROPERTY_SELENIUMSERVER_JVM_OPTIONS,
				config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.SELENIUMSERVER_JVM_OPTIONS);
		//Set JVM for SELENIUM server
		StringUtils.getSystemProperty(SeleniumConfigConstant.PROPERTY_SELENIUMSERVER_JVM,
				config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.SELENIUMSERVER_JVM);

		//Set window's state for "Remote Server Console": minimize, maximize
		StringUtils.getSystemProperty(SeleniumConfigConstant.PROPERTY_CONSOLE_STATE,
				config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.ITEM_CONSOLE_STATE);

		//Set browser-drivers to start with selenium-server: explorer, chrome, MicrosoftEdge etc.
		StringUtils.getSystemProperty(SeleniumConfigConstant.PROPERTY_WEB_DRIVERS,
				config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.ITEM_WEB_DRIVERS);

		//Set "command", "duration" and "max try" for testing the connection between client and server.
		StringUtils.getSystemProperty(SeleniumConfigConstant.PROPERTY_CONNECTION_TEST_COMMAND,
				config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.ITEM_CONNECTION_TEST_COMMAND, RemoteDriver.DEFAULT_CONNECTION_TEST_COMMAND);
		StringUtils.getSystemProperty(SeleniumConfigConstant.PROPERTY_CONNECTION_TEST_MAX_DURATION,
				config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.ITEM_CONNECTION_TEST_MAX_DURATION, String.valueOf(SeleniumConfigConstant.DEFAULT_CONNECTION_TEST_MAX_DURATION));
		StringUtils.getSystemProperty(SeleniumConfigConstant.PROPERTY_CONNECTION_TEST_MAX_TRY,
				config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.ITEM_CONNECTION_TEST_MAX_TRY, String.valueOf(SeleniumConfigConstant.DEFAULT_CONNECTION_TEST_MAX_TRY));

		//Set the "timeout" and "browserTimeout" for starting the selenium server
		StringUtils.getSystemProperty(SeleniumConfigConstant.PROPERTY_TIMEOUT,
				config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.ITEM_TIMEOUT, String.valueOf(SeleniumConfigConstant.DEFAULT_TIMEOUT));
		StringUtils.getSystemProperty(SeleniumConfigConstant.PROPERTY_BROWSER_TIMEOUT,
				config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.ITEM_BROWSER_TIMEOUT, String.valueOf(SeleniumConfigConstant.DEFAULT_BROWSER_TIMEOUT));

		//Set the "wait component's ready" property
		StringUtils.getSystemProperty(SeleniumConfigConstant.PROPERTY_WAIT_READY,
				config, DriverConstant.SECTION_SAFS_SELENIUM, SeleniumConfigConstant.ITEM_WAIT_READY, String.valueOf(SeleniumConfigConstant.DEFAULT_WAIT_READY));

	}

}
