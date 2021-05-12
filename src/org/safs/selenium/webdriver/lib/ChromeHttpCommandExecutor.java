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
 * @date 2019-06-03    (Lei Wang) Initial release. Copied codes from https://github.com/SeleniumHQ/selenium/blob/master/java/client/src/org/openqa/selenium/chromium/ChromiumDriverCommandExecutor.java
 * @date 2019-06-21    (Lei Wang) Added method defineAdditionalCommands(): add additional commands to 'commandCodec'. 
 */
package org.safs.selenium.webdriver.lib;

import java.net.URL;
import java.util.Map;

import org.openqa.selenium.remote.CommandInfo;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.http.HttpMethod;
import org.safs.Constants.BrowserConstants;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * @author Lei Wang
 *
 */
public class ChromeHttpCommandExecutor extends HttpCommandExecutor{

	/** "launchApp" */
	public static final String LAUNCH_APP 					= "launchApp";
	/** "getNetworkConditions" */
	public static final String GET_NETWORK_CONDITIONS 		= "getNetworkConditions";
	/** "setNetworkConditions" */
	public static final String SET_NETWORK_CONDITIONS 		= "setNetworkConditions";
	/** "deleteNetworkConditions" */
	public static final String DELETE_NETWORK_CONDITIONS 	= "deleteNetworkConditions";
	/** "executeCdpCommand" */
	public static final String EXECUTE_CDP_COMMAND 			= "executeCdpCommand";

	/** "network_conditions" */
	public static final String URI_NETWORK_CONDITIONS		= "network_conditions";

	/** These are additional commmands */
	private static final ImmutableMap<String, CommandInfo> CHROME_COMMAND_NAME_TO_URL = ImmutableMap.of(
			LAUNCH_APP,
			new CommandInfo("/session/:sessionId/chromium/launch_app", HttpMethod.POST),
			GET_NETWORK_CONDITIONS,
			new CommandInfo("/session/:sessionId/chromium/network_conditions", HttpMethod.GET),
			SET_NETWORK_CONDITIONS,
			new CommandInfo("/session/:sessionId/chromium/network_conditions", HttpMethod.POST),
			DELETE_NETWORK_CONDITIONS,
			new CommandInfo("/session/:sessionId/chromium/network_conditions", HttpMethod.DELETE),
			EXECUTE_CDP_COMMAND,
			new CommandInfo("/session/:sessionId/goog/cdp/execute", HttpMethod.POST));

	public ChromeHttpCommandExecutor(URL addressOfRemoteServer) {
		super(CHROME_COMMAND_NAME_TO_URL, addressOfRemoteServer);
	}

	/**
	 * <b>NOTE:</b> This MUST be called after the field 'commandCodec' has been initialized.
	 * This method will define "additional commands" for this CommandExecutor.<br>
	 * In HttpCommandExecutor, super{@link #defineCommand(String, CommandInfo)} will be called when executing 'newSession' (see {@link #execute(org.openqa.selenium.remote.Command)})<br>
	 * When we reconnect to a session, we need to call this method so that the "additional commands" defined by {@link #CHROME_COMMAND_NAME_TO_URL} will be recognized.<br>
	 */
	public void defineAdditionalCommands(){
		for (Map.Entry<String, CommandInfo> entry : CHROME_COMMAND_NAME_TO_URL.entrySet()) {
			defineCommand(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * If ChromeHttpCommandExecutor is needed to create RemoteWebDriver.<br>
	 * We check the capabilities, if it contains one of the keys in {@link #CHROME_COMMAND_NAME_TO_URL}, then the ChromeHttpCommandExecutor is needed.<br>
	 *
	 * @param capabilities DesiredCapabilities
	 * @return boolean
	 */
	public static boolean isRequired(DesiredCapabilities capabilities){
		ImmutableSet<String> keys = CHROME_COMMAND_NAME_TO_URL.keySet();

		if(!BrowserConstants.BROWSER_NAME_CHROME.equalsIgnoreCase(capabilities.getBrowserName())){
			return false;
		}

		for(String key:keys){
			if(capabilities.getCapability(key)!=null) return true;
		}

		return false;
	}
}
