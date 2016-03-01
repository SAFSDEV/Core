/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

public class RunScript implements StepType, SRunnerType {

	public static final String SCRIPT_PARAM  = "script";

	@Override
	public boolean run(TestRun ctx) {
		String result = ctx.driver().executeScript(ctx.string(SCRIPT_PARAM)).toString();
		// what can we do with result?
		return true;
	}

	@Override
	public void processParams(Step step, String[] params) {
		step.stringParams.put(SCRIPT_PARAM, params[1]);
	}

}
