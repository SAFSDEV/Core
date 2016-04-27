/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * MAR 25, 2016    (Lei Wang) Initial release.
 */
package org.safs;

import org.safs.tools.drivers.ConfigureInterface;
import org.safs.tools.drivers.DefaultDriver;
import org.safs.tools.drivers.DriverConstant;

public class DefaultHookConfig implements HookConfig{
	protected ConfigureInterface config = null;
	
	public DefaultHookConfig(ConfigureInterface config){
		this.config = config;
	}

	/**
	 * Check the ConfigureInterface to get the settings and set them to 'system properties' if they don't exist in 'system properties'.<br>
	 * But if the 'system properties' contains a setting, then we will use it without checking it from ConfigureInterface.<br>
	 * <b>Then, at hook side we can refer to these settings from 'system properties' at any time.</b><br>
	 */
	public void checkConfiguration() {
		String debugmsg = StringUtils.debugmsg(false);
		if(config==null){
			String message = debugmsg+"The Configure object is null!";
			IndependantLog.error(message);
			System.err.println(message);
			return;
		}
		
		IndependantLog.debug(debugmsg+" Checking 'system properties/configurtion settings ... '");
		DefaultHookConfig.check(config);

	}

	/**
	 * Set the configuration settings to System-Properties and probably to Processor.<br>
	 * If the System-Properties does NOT contain the 'property', then get the value from the ConfigureInterface and set it to System-Properties;<br>
	 * Otherwise, keep the value in the System-Properties (don't override it by the value from ConfigureInterface).<br>
	 * Finally these configuration settings may also be kept in the Processor so that the concrete Processor will be able to use it easily.
	 * <br>
	 * <b>Note: If the Hook run in an other JVM, the Engine's system settings will not take effect!</b><br>
	 * 
	 * <br>
	 * The checked properties (configuration values) are listed as following:<br>
	 * <ul>
	 * <li> {@link DriverConstant#PROERTY_SAFS_TEST_UNEXPECTEDALERTBEHAVIOUR} <br>
	 *      ([{@link DriverConstant#SECTION_SAFS_TEST}] {@link DriverConstant#SECTION_SAFS_TEST_UNEXPECTEDALERTBEHAVIOUR})
	 * <li> {@link DriverConstant#PROPERTY_SAFS_TEST_NUMLOCKON} <br>
	 *      ([{@link DriverConstant#SECTION_SAFS_TEST}] {@link DriverConstant#SECTION_SAFS_TEST_NUMLOCKON})
	 * </ul>
	 * 
	 * @see #checkConfiguration()
	 * @see DefaultDriver#validateTestParameters()
	 */
	public static void check(ConfigureInterface config){
		String unexpectedAlertBehaviour = StringUtils.getSystemProperty(DriverConstant.PROERTY_SAFS_TEST_UNEXPECTEDALERTBEHAVIOUR, 
				config, DriverConstant.SECTION_SAFS_TEST, DriverConstant.SECTION_SAFS_TEST_UNEXPECTEDALERTBEHAVIOUR, DriverConstant.DEFAULT_UNEXPECTED_ALERT_BEHAVIOUR);				
		IndependantLog.debug("the system property '" + DriverConstant.PROERTY_SAFS_TEST_UNEXPECTEDALERTBEHAVIOUR+"' is '"+unexpectedAlertBehaviour+"'.");
		Processor.setUnexpectedAlertBehaviour(unexpectedAlertBehaviour);

		String numberlock = StringUtils.getSystemProperty(DriverConstant.PROPERTY_SAFS_TEST_NUMLOCKON, 
				config, DriverConstant.SECTION_SAFS_TEST, DriverConstant.SECTION_SAFS_TEST_NUMLOCKON);				
		IndependantLog.debug("the system property '" + DriverConstant.PROPERTY_SAFS_TEST_NUMLOCKON+"' is '"+numberlock+"'.");

	}
	
}
