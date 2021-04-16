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
 * This class is used to handle the "safs action command" step definitions for gherkin feature files.
 *
 */
public class SAFSActions extends SeleniumPlus {

	/**
	 * Run a SAFS Component Function on a top-level component (Window).
	 * The particular safs action takes no additional parameters.
	 * This method performs SAFS Expression processing on all input parameters.
	 * <p>
	 * Cucumber Expression: "do safs action {string} on {string}"<br>
	 * {string} Matches single-quoted or double-quoted strings, for example "banana split" or 'banana split' (but not banana split).<br>
	 * <p>
	 * Example Scenario Step:
	 * <p>
	 * <ul>Then do safs action "GuiDoesExist" on "LoginWindow"</ul>
	 *
	 * @param command The ComponentFunction keyword (action) to perform
	 * @param parent The ComponentFunction parent window name to act on
	 * @return
	 * @throws Throwable
	 */
	@Then("do safs action {string} on {string}")
	public TestRecordHelper runComponentFunction(String command, String parent) throws Throwable{
		return runComponentFunction(command, parent, parent);
	}

	/**
	 * Run a SAFS Component Function on a child component in a parent (Window).
	 * The particular safs action takes no additional parameters.
	 * This method performs SAFS Expression processing on all input parameters.
	 * <p>
	 * Cucumber Expression: "do safs action {string} on {string} in {string}"
	 * {string} Matches single-quoted or double-quoted strings, for example "banana split" or 'banana split' (but not banana split).<br>
	 * <p>
	 * Example Scenario Step:
	 * <p>
	 * <ul>Then do safs action "Click" on "Userid" in "LoginWindow"</ul>
	 *
	 * @param command The ComponentFunction keyword (action) to perform
	 * @param child The ComponentFunction child component name to act on
	 * @param parent The ComponentFunction parent window name to act on
	 * @throws Throwable
	 */
	@Then("do safs action {string} on {string} in {string}")
	public TestRecordHelper runComponentFunction(String command, String child, String parent) throws Throwable{
		return runComponentFunction(command, parent, child, null);
	}


	/**
	 * Run a SAFS Component Function on a top-level parent (Window).
	 * The safs action may take one or more parameters.
	 * This method performs SAFS Expression processing on all input parameters.
	 * <p>
	 * Cucumber Expression: "do safs action {string} on {string} using {list}<br>
	 * {string} Matches single-quoted or double-quoted strings, for example "banana split" or 'banana split' (but not banana split).<br>
	 * {list}   Represents a list of string parameters, the parameters can be delimited by {@link TestRecordData#POSSIBLE_SEPARATOR}.<br>
	 * <p>
	 * Example Scenario Step:
	 * <p>
	 * <ul>Then do safs action "VerifyProperty" on "LoginWindow" using "Visible, True"</ul>
	 *
	 * @param command The ComponentFunction keyword (action) to perform
	 * @param parent The ComponentFunction parent window name to act on
	 * @param parameters List&lt;String> of parameters used by the command.  Can be null.
	 * @throws Throwable
	 */
	@Then("do safs action {string} on {string} using {list}")
	public TestRecordHelper runComponentFunction(String command, String parent, List<String> parameters) throws Throwable{
		return runComponentFunction(command, parent, parent, parameters);
	}

	/**
	 * Run a SAFS Component Function on a child component in a parent (Window).
	 * The safs action may take one or more parameters.
	 * This method performs SAFS Expression processing on all input parameters.
	 * <p>
	 * Cucumber Expression: "do safs action {string} on {string} in {string} using {list}
	 * {string} Matches single-quoted or double-quoted strings, for example "banana split" or 'banana split' (but not banana split).<br>
	 * {list}   Represents a list of string parameters, the parameters can be delimited by {@link TestRecordData#POSSIBLE_SEPARATOR}.<br>
	 * <p>
	 * Example Scenario Step:
	 * <p>
	 * <ul>Then do safs action "VerifyProperty" on "Userid" in "LoginWindow" using "Visible, True"</ul>
	 *
	 * @param command The ComponentFunction keyword (action) to perform
	 * @param child The ComponentFunction child component name to act on
	 * @param parent The ComponentFunction parent window name to act on
	 * @param parameters List&lt;String> of parameters used by the command.  Can be null.
	 * @throws Throwable
	 */
	@Then("do safs action {string} on {string} in {string} using {list}")
	public TestRecordHelper runComponentFunction(String command, String child, String parent, List<String> parameters) throws Throwable{
		if(parameters==null){
			return getRunner().getDriver().runComponentFunction(command, child, parent);
		}else{
			return getRunner().getDriver().runComponentFunction(command, child, parent, parameters.toArray(new String[]{}));
		}
	}

	/**
	 * Currently does nothing.  Subclasses can override to provide unit tests, etc...
	 */
	@Override
	public void runTest() throws Throwable {}
}
