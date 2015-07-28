/** Copyright (C) (SAS Institute, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.cukes.steps;

import java.util.List;

import org.safs.TestRecordHelper;
import org.safs.cukes.StepDriver;
import org.safs.model.ComponentFunction;
import org.safs.model.DriverCommand;

import cucumber.api.java.Before;
import cucumber.api.java.en.Then;

/**
 * The included runDriverCommand and runComponentFunction methods serve as valid Cukes Steps using 
 * the &#064;Then annotation.  So, they can be invoked automatically by a running Cukes test if the 
 * feature file uses the appropriate Step syntax to match one of these Step Definitions.
 * <p>
 * They can also be called explicitly from other custom step definition implementations.
 * <p>
 * @author Carl Nagle
 * @see org.safs.tools.drivers.JSAFSDriver
 */
public class StepDefinitions {

	/**
	 * Must be called by Cukes at least once to initialize the JSAFSDriver.
	 * <p>  
	 * It can be called automatically by the execution of Scenarios, or be called 
	 * from an overriding test controller like a JUnit runner, if appropriate.
	 * <p>
	 * If the steps in this class match any steps in the executing Scenario, then 
	 * this method will automatically be called.  If not, then a &#064;JSAFSBefore step from 
	 * another step definition file for the executing Scenario(s) can invoke this method.
	 * <p>
	 * Example:
	 *  <pre><code>
     *  public class OtherStepdefs {
	 *
	 *      &#064;JSAFSBefore(order=10)
	 *      public void beforeAll(){
	 *          StepDriver.beforeAll();
	 *      }
	 *      ...
	 * }
	 * </code></pre>
	 */
	@Before(order=10)
	public void beforeAll(){
		StepDriver.beforeAll();
	}
	
	/**
	 * Run a SAFS Driver Command.
	 * The safs command takes no parameters.
	 * This method performs SAFS Expression processing on all input parameters.
	 * <p>
	 * Regexen: "do safs command ([^\"]*)"
	 * <p>
	 * Example Scenario Step:
	 * <p> 
	 * <ul>Then do safs command ClearClipboard</ul>
	 * 
	 * @param command The DriverCommand keyword (command) to perform
	 * @throws Throwable
	 */
	@Then("do safs command ([^\"]*)")
	public TestRecordHelper runDriverCommand(String command) throws Throwable{
		return runDriverCommand(command, null);
	}

	/**
	 * Run a SAFS Driver Command.
	 * The safs command may take one or more parameters.
	 * This method performs SAFS Expression processing on all input parameters.
	 * <p>
	 * Regexen: "do safs command ([^\"]*) using \"(.+)\"
	 * <p>
	 * Example Scenario Step:
	 * <p> 
	 * <ul>Then do safs command GetAppMapValue using " , ApplicationConstants, WebURL, varURL"</ul>
	 * 
	 * @param command The DriverCommand keyword (command) to perform
	 * @param parameters List&lt;String> of parameters used by the command.  Can be null.
	 * @throws Throwable
	 */
	@Then("do safs command ([^\"]*) using \"(.+)\"")
	public TestRecordHelper runDriverCommand(String command, List<String> parameters) throws Throwable{
		command = StepDriver.jsafs().processExpression(command);
		if(parameters instanceof List) parameters = StepDriver.processExpressions(parameters);
		return runDriverCommandConverted(command, parameters);
	}

	/**
	 * Run a SAFS Driver Command.
	 * The safs command may take one or more parameters.
	 * This method is typically called internally and performs no SAFS Expression processing on the input parameters.
	 * 
	 * @param command The DriverCommand keyword (command) to perform
	 * @param parameters List&lt;String> of parameters used by the command.  Can be null.
	 * @throws Throwable
	 */
	public TestRecordHelper runDriverCommandConverted(String command, List<String> parameters) throws Throwable{
		DriverCommand model = new DriverCommand(command);
		if(parameters instanceof List) model.addParameters(parameters.toArray(new String[]{}));
		return StepDriver.jsafs().runDriverCommand(model);
	}

	/**
	 * Run a SAFS Component Function on a top-level component (Window).  
	 * The particular safs action takes no additional parameters. 
	 * This method performs SAFS Expression processing on all input parameters.
	 * <p>
	 * Regexen: "do safs action ([^\"]*) on ([^\"]*)"
	 * <p>
	 * Example Scenario Step:
	 * <p> 
	 * <ul>Then do safs action GuiDoesExist on LoginWindow</ul>
	 * 
	 * @param command The ComponentFunction keyword (action) to perform
	 * @param parent The ComponentFunction parent window name to act on
	 * @return 
	 * @throws Throwable
	 */
	@Then("do safs action ([^\"]*) on ([^\"]*)")
	public TestRecordHelper runComponentFunction(String command, String parent) throws Throwable{
		return runComponentFunction(command, parent, parent);
	}

	/**
	 * Run a SAFS Component Function on a child component in a parent (Window).  
	 * The particular safs action takes no additional parameters.
	 * This method performs SAFS Expression processing on all input parameters.
	 * <p>
	 * Regexen: "do safs action ([^\"]*) on ([^\"]*) in ([^\"]*)"
	 * <p>
	 * Example Scenario Step:
	 * <p> 
	 * <ul>Then do safs action Click on Userid in LoginWindow</ul>
	 * 
	 * @param command The ComponentFunction keyword (action) to perform
	 * @param child The ComponentFunction child component name to act on
	 * @param parent The ComponentFunction parent window name to act on
	 * @throws Throwable
	 */
	@Then("do safs action ([^\"]*) on ([^\"]*) in ([^\"]*)")
	public TestRecordHelper runComponentFunction(String command, String child, String parent) throws Throwable{
		return runComponentFunction(command, parent, child, null);
	}


	/**
	 * Run a SAFS Component Function on a top-level parent (Window).
	 * The safs action may take one or more parameters.
	 * This method performs SAFS Expression processing on all input parameters.
	 * <p>
	 * Regexen: "do safs action ([^\"]*) on ([^\"]*) using \"(.+)\"
	 * <p>
	 * Example Scenario Step:
	 * <p> 
	 * <ul>Then do safs action VerifyProperty on LoginWindow using "Visible, True"</ul>
	 * 
	 * @param command The ComponentFunction keyword (action) to perform
	 * @param parent The ComponentFunction parent window name to act on
	 * @param parameters List&lt;String> of parameters used by the command.  Can be null.
	 * @throws Throwable
	 */
	@Then("do safs action ([^\"]*) on ([^\"]*) using \"(.+)\"")
	public TestRecordHelper runComponentFunction(String command, String parent, List<String> parameters) throws Throwable{
		return runComponentFunction(command, parent, parent, parameters);
	}
	
	/**
	 * Run a SAFS Component Function on a child component in a parent (Window).
	 * The safs action may take one or more parameters.
	 * This method performs SAFS Expression processing on all input parameters.
	 * <p>
	 * Regexen: "do safs action ([^\"]*) on ([^\"]*) in ([^\"]*) using \"(.+)\"
	 * <p>
	 * Example Scenario Step:
	 * <p> 
	 * <ul>Then do safs action VerifyProperty on Userid in LoginWindow using "Visible, True"</ul>
	 * 
	 * @param command The ComponentFunction keyword (action) to perform
	 * @param child The ComponentFunction child component name to act on
	 * @param parent The ComponentFunction parent window name to act on
	 * @param parameters List&lt;String> of parameters used by the command.  Can be null.
	 * @throws Throwable
	 */
	@Then("do safs action ([^\"]*) on ([^\"]*) in ([^\"]*) using \"(.+)\"")
	public TestRecordHelper runComponentFunction(String command, String child, String parent, List<String> parameters) throws Throwable{
		command = StepDriver.jsafs().processExpression(command);
		if(child instanceof String) child =  StepDriver.jsafs().processExpression(child);
		if(parent instanceof String) parent =  StepDriver.jsafs().processExpression(parent);
		if(parameters instanceof List) parameters =  StepDriver.processExpressions(parameters);
		return runComponentFunctionConverted(command, child, parent, parameters);
	}

	/**
	 * Run a SAFS Component Function on a child component in a parent (Window).
	 * The safs action may take one or more parameters.
	 * This method is typically called internally and performs no SAFS Expression processing on the input 
	 * parameters.
	 * 
	 * @param command The ComponentFunction keyword (action) to perform
	 * @param child The ComponentFunction child component name to act on
	 * @param parent The ComponentFunction parent window name to act on
	 * @param parameters List&lt;String> of parameters used by the command.  Can be null.
	 * @throws Throwable
	 */
	public TestRecordHelper runComponentFunctionConverted(String command, String child, String parent, List<String> parameters) throws Throwable{
		ComponentFunction model = new ComponentFunction(command, parent, child);
		if(parameters instanceof List) model.addParameters(parameters.toArray(new String[]{}));
		return StepDriver.jsafs().runComponentFunction(model);
	}
}
