/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.safs.STAFHelper;
import org.safs.selenium.webdriver.WebDriverGUIUtilities;
import org.safs.selenium.webdriver.lib.RemoteDriver;
import org.safs.selenium.webdriver.lib.SelectBrowser;
import org.safs.selenium.webdriver.lib.WDLibrary;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.Verify;
import com.sebuilder.interpreter.steptype.ClickElement;
import com.sebuilder.interpreter.steptype.Get;
import com.sebuilder.interpreter.steptype.SwitchToFrame;
import com.sebuilder.interpreter.steptype.SwitchToFrameByIndex;
import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;

/**
 * The primary purpose of this class is to extend the TestRun support to include Step peeking, skipping, 
 * and Retry of specific Script Steps.  It also provides WDLocator instances using enhanced SearchObject 
 * search algorithms.
 * @author canagl
 * <br>JUL 28, 2015 SCNTAX Added SwitchToFrame and SwitchToFrameIndex support
 */
public class WDTestRun extends TestRun {

	public WDTestRun(Script script, int implicitlyWaitDriverTimeout,
			int pageLoadDriverTimeout, Map<String, String> initialVars) {
		super(script, implicitlyWaitDriverTimeout, pageLoadDriverTimeout, initialVars);
	}

	public WDTestRun(Script script, int implicitlyWaitDriverTimeout,
			int pageLoadDriverTimeout) {
		super(script, implicitlyWaitDriverTimeout, pageLoadDriverTimeout);
	}

	public WDTestRun(Script script, Log log, TestRun previousRun,
			int implicitlyWaitDriverTimeout, int pageLoadDriverTimeout,
			Map<String, String> initialVars) {
		super(script, log, previousRun, implicitlyWaitDriverTimeout,
				pageLoadDriverTimeout, initialVars);
	}

	public WDTestRun(Script script, Log log,
			WebDriverFactory webDriverFactory,
			HashMap<String, String> webDriverConfig,
			int implicitlyWaitDriverTimeout, int pageLoadDriverTimeout,
			Map<String, String> initialVars) {
		super(script, log, webDriverFactory, webDriverConfig,
				implicitlyWaitDriverTimeout, pageLoadDriverTimeout, initialVars);
	}

	public WDTestRun(Script script, Log log,
			WebDriverFactory webDriverFactory,
			HashMap<String, String> webDriverConfig,
			int implicitlyWaitDriverTimeout, int pageLoadDriverTimeout) {
		super(script, log, webDriverFactory, webDriverConfig,
				implicitlyWaitDriverTimeout, pageLoadDriverTimeout);
	}

	public WDTestRun(Script script, Log log,
			WebDriverFactory webDriverFactory,
			HashMap<String, String> webDriverConfig,
			Map<String, String> initialVars) {
		super(script, log, webDriverFactory, webDriverConfig, initialVars);
	}

	public WDTestRun(Script script, Log log,
			WebDriverFactory webDriverFactory,
			HashMap<String, String> webDriverConfig) {
		super(script, log, webDriverFactory, webDriverConfig);
	}

	public WDTestRun(Script script, Log log) {
		super(script, log);
	}

	public WDTestRun(Script script) {
		super(script);
	}
	
	/**
	 * Fetches a Locator parameter from the current step.
	 * @param paramName The parameter's name.
	 * @return The parameter's value.
	 */
	@Override
	public Locator locator(String paramName) {
		Locator l = new WDLocator(currentStep().locatorParams.get(paramName));
		getLog().info("");
		if (l == null) {
			throw new RuntimeException("Missing parameter \"" + paramName + "\" at step #" +
					(stepIndex + 1) + ".");
		}else{
			try{ getLog().info("Locator class: "+ l.getClass().getName());}catch(Exception x){}
			try{ getLog().info("Locator Type: "+ l.type);}catch(Exception x){}
			try{ getLog().info("Locator Value: "+ l.value);}catch(Exception x){}
		}
		// This kind of variable substitution makes for short code, but it's inefficient.
		for (Map.Entry<String, String> v : vars().entrySet()) {
			l.value = l.value.replace("${" + v.getKey() + "}", v.getValue());
		}
		return l;
	}
	
	/** 
	 * Retrieve the next Step that would be executed by this TestRun without incrementing the 
	 * stepIndex counter.
	 * <p>
	 * Does NOT use hasNext()--which can prematurely shutdown the WebDriver.
	 * <p>
	 * If avoiding hasNext() the user should make sure cleanup() is called when finished to perform 
	 * the normal cleanup operations done by hasNext() when it detects there are no more Steps to 
	 * execute.
	 * @return Step -- null if there are no more steps. 
	 */
	public Step peekNext(){
		try{
			getLog().debug("WDTestRun Peeking step " + (stepIndex + 2) + ":" +
			         getScript().steps.get(stepIndex + 1).type.getClass().getSimpleName());
		}catch(Exception x){
			getLog().debug("WDTestRun: There are no more Steps to Peek. null.");
			return null;
		}
		return getScript().steps.get(stepIndex + 1); 
	}
	
	/**
	 * Increment the stepIndex counter and return the Step that can be skipped or executed by this TestRun.
	 * Uses peekNext() to check for a valid next Step.  
	 * <p>
	 * Does NOT use hasNext()--which can prematurely shutdown the WebDriver.
	 * <p>
	 * If avoiding hasNext() the user should make sure cleanup() is called when finished to perform 
	 * the normal cleanup operations done by hasNext() when it detects there are no more Steps to 
	 * execute.
	 * @return Step -- null if there are no more steps.
	 * @see #cleanup()
	 */
	public Step popNext(){
		Step step = peekNext();
		if(step ==null){
			getLog().debug("WDTestRun: There are no more Steps to retrieve. null.");
			return null;
		}
		stepIndex++;
		getLog().debug("WDTestRun: retrieved step "+ (stepIndex + 1));
		return step;
	}
	
	/**
	 * Perform the normal cleanup done at the end of script execution.
	 * Essentially, calls hasNext() with stepIndex indicating all steps have been executed.
	 */
	public void cleanup(){
		getLog().debug("WDTestRun.cleanup invoked after step "+ (stepIndex + 1));
		stepIndex = getScript().steps.size();
		if (!hasNext() && (getDriver()!= null) && (getScript().closeDriver)) {
			getLog().debug("WDTestRun.cleanup closing RemoteWebDriver.");
			try{ WDLibrary.closeBrowser(); }
			catch(Exception x){
				getLog().debug("WDTestRun.cleanup error closing RemoteWebDriver: "+ x.getMessage());
			}
		}
	}
	
	/** @return True if there is another step to execute. */
	public boolean hasNext() {
		return stepIndex < getScript().steps.size() - 1;
	}
		
	/**
	 * Execute the action represented in the Step.
	 * This does NOT change or increment any counters of what Step is being executed 
	 * in the Script.
	 * <p>
	 * If the StepType is an instanceof ClickElement we will use our own WDClickElement 
	 * which uses our enhanced WDLibrary to perform the click.<br>
	 * 
	 * @param step
	 * @return
	 */
	public boolean runStep(Step step){

		initRemoteWebDriver();
		
		try {
			StepType type = step.type;
			if(type instanceof ClickElement){
				type = new WDClickElement();
			}else if(type instanceof Get){
				type = new WDGet();
			} else if(type instanceof SwitchToFrame) {
				type = new WDSwitchToFrame();
			} else if (type instanceof SwitchToFrameByIndex) {
				type = new WDSwitchToFrameByIndex();
			}			
			return type.run(this);
		} catch (Exception e) {
			throw new RuntimeException(currentStep() + " failed.", e);
		}
	}
	
	/**
	 * Executes the next step.  
	 * Extracts the Step and executes via runStep for enhanced 
	 * handling of some StepTypes.
	 * @return True on success.
	 * @see #runStep(Step)
	 */
	@Override
	public boolean next() {
		if (stepIndex == -1) {
			getLog().debug("Starting test run.");
		}

		initRemoteWebDriver();

		getLog().debug("Running step " + (stepIndex + 2) + ":" +
		getScript().steps.get(stepIndex + 1).type.getClass().getSimpleName() + " step.");
		boolean result = runStep(getScript().steps.get(++stepIndex));
		if (!result) {
			// If a verify failed, we just note this but continue.
			if (currentStep().type instanceof Verify) {
				getLog().error(currentStep() + " failed.");
				return false;
			}
			// In all other cases, we throw an exception to stop the run.
			RuntimeException e = new RuntimeException(currentStep() + " failed.");
			e.fillInStackTrace();
			getLog().fatal(e);
			throw e; // continue?
		} else {
			return true;
		}
	}

	/* (non-Javadoc)
	 * @see com.sebuilder.interpreter.TestRun#getDriver()
	 */
	@Override
	public RemoteWebDriver getDriver() {
		getLog().info("WDTestRun.getDriver() invoked.");
		try{
			WebDriver webDriver = WDLibrary.getWebDriver();
			if(webDriver == null) {
				getLog().debug("WDTestRun.getDriver() is null.");
			}else{
				return (RemoteWebDriver) webDriver;
			}
		}catch(Exception x){
			getLog().debug("WDTestRun.getDriver() "+ x.getMessage());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.sebuilder.interpreter.TestRun#driver()
	 */
	@Override
	public RemoteWebDriver driver() {
		getLog().info("WDTestRun.driver() invoked. Calling getDriver();");
		// watch out for recursive loop!
		return getDriver();
	}

	/* (non-Javadoc)
	 * CANAGL -- this seems to be called prior to EVERY single Step in the script.
	 * So only initialize IF we don't already have one.
	 * @see com.sebuilder.interpreter.TestRun#initRemoteWebDriver()
	 */
	@Override
	public void initRemoteWebDriver() {
		getLog().info("WDTestRun.initRemoteWebDriver invoked.");
		if(getDriver() != null) {
			getLog().info("WDTestRun.initRemoteWebDriver detected session already exists...");
			return;
		}
		int timeout = 30; // TODO get from parameters in this object!
		try{ WDLibrary.startBrowser(null, null, null, timeout, true);}
		catch(Throwable th){
			String thmsg = th.getMessage();
			getLog().info("WDTestRun.initRemoteWebDriver initially failed to create a new WebDriver session. RemoteServer may not be running.");
			String seleniumhost = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_HOST, SelectBrowser.DEFAULT_SELENIUM_HOST);
			String seleniumport = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_PORT, SelectBrowser.DEFAULT_SELENIUM_PORT);
			if( seleniumhost.equals(SelectBrowser.DEFAULT_SELENIUM_HOST)){			
				getLog().info("WDTestRun.initRemoteWebDriver attempting to start a local RemoteServer...");
				boolean success = false;
				try{
					String projectdir = WebDriverGUIUtilities._LASTINSTANCE.getSTAFHelper().getVariable(STAFHelper.SAFS_VAR_PROJECTDIRECTORY);
					success = WebDriverGUIUtilities.startRemoteServer(projectdir);
					if(success) getLog().info("WDTestRun.initRemoteWebDriver started RemoteServer successfully.");
					else getLog().info("WDTestRun.initRemoteWebDriver starting RemoteServer internally not successful.");
				}catch(Exception x){
					getLog().info("WDTestRun.initRemoteWebDriver attempt to start RemoteServer internally: "+ x.getClass().getName()+", "+x.getMessage());					
				}
				if(!success) {
					getLog().info("WDTestRun.initRemoteWebDriver attempting to start RemoteServer from external script...");
					success = WebDriverGUIUtilities.startRemoteServer();
				}
				if(success){
					try{
						WDLibrary.startBrowser(null, null, null, timeout, true);
						getLog().info("WDTestRun.initRemoteWebDriver successful starting browser session.");
					}catch(Throwable th2){
						getLog().debug("WDTestRun.initRemoteWebDriver failed to start a new WebDriver session:", th2);
					}
				}else{
					getLog().debug("WDTestRun.initRemoteWebDriver failed to start the local RemoteServer!");
				}
			}else{
				getLog().info("WDTestRun.initRemoteWebDriver detected the expected RemoteServer '"+ seleniumhost+":"+ seleniumport +"' is not running and cannot be started locally.");
			}
		}
	}
	
}
