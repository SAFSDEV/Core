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
 * @date 2019-06-26    (Lei Wang) Initial release.
 */
package org.safs.cukes.ai.selenium;

import java.util.List;

import org.safs.TestRecordData;
import org.safs.TestRecordHelper;
import org.safs.selenium.webdriver.SeleniumPlus;

import cucumber.api.java.en.Then;

/**
 * <b>Prerequisite</b><br>
 * A SeleniumPlus test has properly started.
 *
 * <p>
 * <b>Purpose</b><br>
 * This class is used to handle the "safs driver command" step definitions for gherkin feature files.
 *
 */
public class SAFSCommands extends SeleniumPlus {
	/**
	 * Run a SAFS Driver Command.
	 * The safs command takes no parameters.
	 * This method performs SAFS Expression processing on all input parameters.
	 * <p>
	 * Cucumber Expression: "do safs command {string}"<br>
	 * {string} Matches single-quoted or double-quoted strings, for example "banana split" or 'banana split' (but not banana split).<br>
	 * <p>
	 * Example Scenario Step:
	 * <p>
	 * <ul>Then do safs command "ClearClipboard"</ul>
	 *
	 * @param command The DriverCommand keyword (command) to perform
	 * @throws Throwable
	 */
	@Then("do safs command {string}")
	public TestRecordHelper runDriverCommand(String command) throws Throwable{
		return runDriverCommand(command, null);
	}

	/**
	 * Run a SAFS Driver Command.
	 * The safs command may take one or more parameters.
	 * This method performs SAFS Expression processing on all input parameters.
	 * <p>
	 * Cucumber Expression: "do safs command {string} using {list}<br>
	 * {string} Matches single-quoted or double-quoted strings, for example "banana split" or 'banana split' (but not banana split).<br>
	 * {list}   Represents a list of string parameters, the parameters can be delimited by {@link TestRecordData#POSSIBLE_SEPARATOR}.<br>
	 * <p>
	 * Example Scenario Step:
	 * <p>
	 * <ul>Then do safs command "GetAppMapValue" using " , ApplicationConstants, WebURL, varURL"</ul>
	 * <ul>Then do safs command "GetAppMapValue" using " $ ApplicationConstants$ WebURL$ varURL"</ul>
	 *
	 * @param command The DriverCommand keyword (command) to perform
	 * @param parameters List&lt;String> of parameters used by the command.  Can be null.
	 * @throws Throwable
	 */
	@Then("do safs command {string} using {list}")
	public TestRecordHelper runDriverCommand(String command, List<String> parameters) throws Throwable{
		if(parameters==null){
			return getRunner().getDriver().runDriverCommand(command);
		}else{
			return getRunner().getDriver().runDriverCommand(command, parameters.toArray(new String[]{}));
		}
	}

	/**
	 * Currently does nothing.  Subclasses can override to provide unit tests, etc...
	 */
	@Override
	public void runTest() throws Throwable {}
}
