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
import org.safs.tools.drivers.DriverConstant;

public class DefaultHookConfig implements HookConfig{
	protected ConfigureInterface config = null;
	
	public DefaultHookConfig(ConfigureInterface config){
		this.config = config;
	}

	/**
	 * Set the configurations to System-Properties.<br>
	 * If the System-Properties does NOT contain the 'property', then get the value from the ConfigureInterface and set to System-Properties;<br>
	 * Otherwise, keep the value in the System-Properties (don't override by the value from ConfigureInterface).<br>
	 * <b>Note: If the Hook run in an other JVM, the Engine's system settings will not take effect!</b><br>
	 * 
	 * <br>
	 * The checked properties (configuration values) are listed as following:<br>
	 * <ul>
	 * <li> {@link DriverConstant#PROERTY_SAFS_TEST_UNEXPECTEDALERTBEHAVIOUR} <br>
	 *      ([{@link DriverConstant#SECTION_SAFS_TEST}] {@link DriverConstant#SECTION_SAFS_TEST_UNEXPECTEDALERTBEHAVIOUR})
	 * </ul>
	 */
	public void checkConfiguration() {
		if(config==null){
			String message = StringUtils.debugmsg(false)+"The Configure object is null!";
			IndependantLog.error(message);
			System.err.println(message);
			return;
		}
		
		String unexpectedAlertBehaviour = StringUtils.getSystemProperty(DriverConstant.PROERTY_SAFS_TEST_UNEXPECTEDALERTBEHAVIOUR, 
				config, DriverConstant.SECTION_SAFS_TEST, DriverConstant.SECTION_SAFS_TEST_UNEXPECTEDALERTBEHAVIOUR, DriverConstant.DEFAULT_UNEXPECTED_ALERT_BEHAVIOUR);				
		IndependantLog.debug("system properyt 'safs.test.unexpected_alert_behaviour' was set to " + unexpectedAlertBehaviour);
		//Set this value to Processor so that the concrete Processor will be able to use it.
		Processor.setUnexpectedAlertBehaviour(unexpectedAlertBehaviour);

	}

}
