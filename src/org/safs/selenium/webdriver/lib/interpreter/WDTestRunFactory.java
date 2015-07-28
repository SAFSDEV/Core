/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.factory.TestRunFactory;
import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;

/**
 * Overrides the Default TestRunFactory to provide instances of WDTestRun objects.
 * @author canagl
 * <br>JUL 28, 2015 SCNTAX Added SwitchToFrame and SwitchToFrameIndex support
 */
public class WDTestRunFactory extends TestRunFactory {

	/**
	 * 
	 */
	public WDTestRunFactory() {
		super();
		WDLocator.setFrameInfo("");	// reset any previously stored frame reference
	}

	/* (non-Javadoc)
	 * @see com.sebuilder.interpreter.factory.TestRunFactory#createTestRun(com.sebuilder.interpreter.Script)
	 */
	@Override
	public TestRun createTestRun(Script script) {
		return new WDTestRun(script, getImplicitlyWaitDriverTimeout(), getPageLoadDriverTimeout());
	}

	/* (non-Javadoc)
	 * @see com.sebuilder.interpreter.factory.TestRunFactory#createTestRun(com.sebuilder.interpreter.Script, java.util.Map)
	 */
	@Override
	public TestRun createTestRun(Script script, Map<String, String> initialVars) {
		return new WDTestRun(script, getImplicitlyWaitDriverTimeout(), getPageLoadDriverTimeout(), initialVars);
	}

	/* (non-Javadoc)
	 * @see com.sebuilder.interpreter.factory.TestRunFactory#createTestRun(com.sebuilder.interpreter.Script, org.apache.commons.logging.Log, com.sebuilder.interpreter.webdriverfactory.WebDriverFactory, java.util.HashMap)
	 */
	@Override
	public TestRun createTestRun(Script script, Log log,
			WebDriverFactory webDriverFactory,
			HashMap<String, String> webDriverConfig) {
		return new WDTestRun(script, log, webDriverFactory, webDriverConfig, getImplicitlyWaitDriverTimeout(), getPageLoadDriverTimeout());
	}

	/* (non-Javadoc)
	 * @see com.sebuilder.interpreter.factory.TestRunFactory#createTestRun(com.sebuilder.interpreter.Script, org.apache.commons.logging.Log, com.sebuilder.interpreter.webdriverfactory.WebDriverFactory, java.util.HashMap, java.util.Map)
	 */
	@Override
	public TestRun createTestRun(Script script, Log log,
			WebDriverFactory webDriverFactory,
			HashMap<String, String> webDriverConfig,
			Map<String, String> initialVars) {
		return new WDTestRun(script, log, webDriverFactory, webDriverConfig, getImplicitlyWaitDriverTimeout(), getPageLoadDriverTimeout(),initialVars);
	}

	/* (non-Javadoc)
	 * @see com.sebuilder.interpreter.factory.TestRunFactory#createTestRun(com.sebuilder.interpreter.Script, org.apache.commons.logging.Log, com.sebuilder.interpreter.webdriverfactory.WebDriverFactory, java.util.HashMap, java.util.Map, com.sebuilder.interpreter.TestRun)
	 */
	@Override
	public TestRun createTestRun(Script script, Log log,
			WebDriverFactory webDriverFactory,
			HashMap<String, String> webDriverConfig,
			Map<String, String> initialVars, TestRun previousRun) {
		if (script.usePreviousDriverAndVars && previousRun != null && previousRun.driver() != null) {
			return new WDTestRun(script, log, previousRun, getImplicitlyWaitDriverTimeout(), getPageLoadDriverTimeout(), initialVars);
		}
		return new WDTestRun(script, log, webDriverFactory, webDriverConfig, getImplicitlyWaitDriverTimeout(), getPageLoadDriverTimeout(), initialVars);
	}

}
