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
package cukes.stepdef;

import org.safs.TestRecordHelper;
import org.safs.selenium.webdriver.SeleniumPlus;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.runtime.Runtime;
import cukes.Map;

public class SeSample extends SeleniumPlus {

	@Given("^I am initializing my test automation$")
	public void I_am_initializing_my_test_automation() throws Throwable{
		Logging.LogMessage("'I am initializing my NEW test automation'.");
		StartWebBrowser(Map.TestURL, Map.SessionID, GetVariableValue(Map.Browser));
	}

	// this will be in a central reusable Se+ cukes support package
	@Then("do safs command ([^\"]*) using \"([^\"]*)\"")
	public TestRecordHelper runSAFSDriverCommandUsingParameters(String command, String parameters) throws Throwable{
		String[] items = parameters.split(",");
		return Runner.command(command, items);
	}

	// this will be in a central reusable Se+ cukes support package
	@Then("do safs command ([^\"]*)")
	public TestRecordHelper runSAFSDriverCommand(String command) throws Throwable{
		return Runner.command(command);
	}

	// this will be in a central reusable Se+ cukes support package
	@Then("do safs action ([^\"]*) on ([^\"]*)")
	public TestRecordHelper runSAFSComponentFunction(String command, String window) throws Throwable{
		return Runner.action(command, window, window, new String[]{});
	}

	// this will be in a central reusable Se+ cukes support package
	@Then("do safs action ([^\"]*) on ([^\"]*) in ([^\"]*)")
	public TestRecordHelper runSAFSComponentFunction(String command, String component, String window) throws Throwable{
		return Runner.action(command, window, component, new String[]{});
	}

	// this will be in a central reusable Se+ cukes support package
	@Then("do safs action ([^\"]*) on ([^\"]*) using \"([^\"]*)\"")
	public TestRecordHelper runSAFSComponentFunctionUsingParameters(String command, String window, String parameters) throws Throwable{
		String[] items = parameters.split(",");
		return Runner.action(command, window, window, items);
	}

	// this will be in a central reusable Se+ cukes support package
	@Then("do safs action ([^\"]*) on ([^\"]*) in ([^\"]*) using \"([^\"]*)\"")
	public TestRecordHelper runSAFSComponentFunctionUsingParameters(String command, String component, String window, String parameters) throws Throwable{
		String[] items = parameters.split(",");
		return Runner.action(command, window, component, items);
	}

	/**
	 * Execute my "simple" Cucumber tests using my cukes Step Definition files.
	 */
	@Override
	public void runTest() throws Throwable {
		final Runtime runtime = Runtime.builder()
				 .withArgs(new String[]{
		    		"--glue", "cukes",
		    		".\\resources\\simple"
		    	 })
		         .withClassLoader(Thread.currentThread().getContextClassLoader())
		         .build();

		runtime.run();
		runtime.exitStatus();
	}
}
