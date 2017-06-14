/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * JUN 13, 2017 (SBJLWA) Stripped the embedding prefix "javascript{" and suffix "}" from the script code.
 *                       Resolved the variable expression "storedVars['var']" in script code.
 */
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.Utils;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

public class RunScript implements StepType, SRunnerType {

	public static final String SCRIPT_PARAM  = "script";

	@Override
	public boolean run(TestRun ctx) {
		Utils.executeScript(ctx, ctx.string(SCRIPT_PARAM));
		// what can we do with result?
		return true;
	}

	@Override
	public void processParams(Step step, String[] params) {
		step.stringParams.put(SCRIPT_PARAM, Utils.normalize(params[1]));
	}

}
