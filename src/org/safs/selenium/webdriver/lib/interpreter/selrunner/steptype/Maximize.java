/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.openqa.selenium.WebDriver;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

/**
 * Maximize the browser with the optionally specified browserid.
 * By default, will maximize the last used webdriver browser.  
 * @author canagl
 */
public class Maximize implements StepType, SRunnerType {

	public static final String BROWSERID_PARAM = "browserid";

	@Override
	public void processParams(Step step, String[] params) {
		if(params.length > 1) step.stringParams.put(BROWSERID_PARAM, params[1]);		
	}

	@Override
	public boolean run(TestRun ctx) {
		String param = ctx.string(BROWSERID_PARAM);
		try {
			if(param != null && param.length() > 0)
				WDLibrary.getBrowserWithID(param);
			WDLibrary.maximizeBrowserWindow();
			return true;
		} catch (Exception e) {
			ctx.log().error("Maximize "+ e.getClass().getSimpleName()+","+e.getMessage());
			return false;
		}
	}
}
