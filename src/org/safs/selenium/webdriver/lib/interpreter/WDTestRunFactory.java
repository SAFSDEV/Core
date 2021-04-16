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
 * @author Carl Nagle
 * <br>JUL 28, 2015 Tao Xie Added SwitchToFrame and SwitchToFrameIndex support
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
