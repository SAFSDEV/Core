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
 * MAR 25, 2016    (Lei Wang) Initial release.
 * SEP 30, 2016    (Lei Wang) Modified check(): set the number lock if it is provided.
 * JUL 11, 2018    (Lei Wang) Modified check(): check the property appcept.slf4j.debug and appcept.slf4j.test 
 */
package org.safs;

import org.safs.logging.slf4j.SAFSLoggerFactory;
import org.safs.tools.drivers.ConfigureInterface;
import org.safs.tools.drivers.DefaultDriver;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverConstant.SAFSLogsConstant;
import org.safs.tools.stringutils.StringUtilities;

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
	@Override
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
	 * <b>Note: If the Hook run in an other JVM, the Engine's system property settings will not take effect!</b><br>
	 *
	 * <br>
	 * The checked "JVM properties"/"configuration values" are listed as following:<br>
	 * <ul>
	 * <li> {@link DriverConstant#PROPERTY_SAFS_TEST_UNEXPECTEDALERTBEHAVIOUR} <br>
	 *      ([{@link DriverConstant#SECTION_SAFS_TEST}] {@link DriverConstant#KEY_SAFS_TEST_UNEXPECTEDALERTBEHAVIOUR})
	 * <li> {@link DriverConstant#PROPERTY_SAFS_TEST_NUMLOCKON} <br>
	 *      ([{@link DriverConstant#SECTION_SAFS_TEST}] {@link DriverConstant#KEY_SAFS_TEST_NUMLOCKON})
	 * </ul>
	 *
	 * @see #checkConfiguration()
	 * @see DefaultDriver#validateTestParameters()
	 */
	public static void check(ConfigureInterface config){
		String unexpectedAlertBehaviour = StringUtils.getSystemProperty(DriverConstant.PROPERTY_SAFS_TEST_UNEXPECTEDALERTBEHAVIOUR,
				config, DriverConstant.SECTION_SAFS_TEST, DriverConstant.KEY_SAFS_TEST_UNEXPECTEDALERTBEHAVIOUR, DriverConstant.DEFAULT_UNEXPECTED_ALERT_BEHAVIOUR);
		IndependantLog.debug("the system property '" + DriverConstant.PROPERTY_SAFS_TEST_UNEXPECTEDALERTBEHAVIOUR+"' is '"+unexpectedAlertBehaviour+"'.");
		Processor.setUnexpectedAlertBehaviour(unexpectedAlertBehaviour);

		//Set the keyboard's 'NumLock' on/off.
		String numberlock = StringUtils.getSystemProperty(DriverConstant.PROPERTY_SAFS_TEST_NUMLOCKON,
				config, DriverConstant.SECTION_SAFS_TEST, DriverConstant.KEY_SAFS_TEST_NUMLOCKON);
		IndependantLog.debug("the system property '" + DriverConstant.PROPERTY_SAFS_TEST_NUMLOCKON+"' is '"+numberlock+"'.");
		if(StringUtils.isValid(numberlock)){
			//TODO Need to work remotely on RMI server.
			Utils.setNumLock(StringUtilities.convertBool(numberlock));
			IndependantLog.info("set KeyBoard 'NumLock' to "+ numberlock+"; The original 'NumLock' is "+DriverConstant.DEFAULT_NUMLOCK_STATUS);
		}

		//Accept slf4j debug log message
		String value = StringUtils.getSystemProperty(SAFSLogsConstant.PROPERTY_ACCPET_SLF4J_DEBUG,
				config, DriverConstant.SECTION_SAFS_LOGS, SAFSLogsConstant.ITEM_ACCPET_SLF4J_DEBUG, SAFSLogsConstant.DEFAULT_ACCPET_SLF4J_DEBUG);
		IndependantLog.debug("the system property '" + SAFSLogsConstant.PROPERTY_ACCPET_SLF4J_DEBUG+"' is '"+value+"'.");
		SAFSLoggerFactory.accpetDebugLog = StringUtils.convertBool(value);

		//Accept slf4j test log message
		value = StringUtils.getSystemProperty(SAFSLogsConstant.PROPERTY_ACCPET_SLF4J_TEST,
				config, DriverConstant.SECTION_SAFS_LOGS, SAFSLogsConstant.ITEM_ACCPET_SLF4J_TEST, SAFSLogsConstant.DEFAULT_ACCPET_SLF4J_TEST);
		IndependantLog.debug("the system property '" + SAFSLogsConstant.PROPERTY_ACCPET_SLF4J_TEST+"' is '"+value+"'.");
		SAFSLoggerFactory.accpetTestLog = StringUtils.convertBool(value);
	}

}
