/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

/**
 * @author canagl
 *
 */
public class Open implements StepType, SRunnerType {

	public static String URL_PARAM = "url";
	
	/* (non-Javadoc)
	 * @see com.sebuilder.interpreter.StepType#run(com.sebuilder.interpreter.TestRun)
	 */
	@Override
	public boolean run(TestRun ctx) {
		ctx.getDriver().get(ctx.string(URL_PARAM));
		return true;
	}

	@Override
	public void processParams(Step step, String[] params) {
		step.stringParams.put(URL_PARAM, params[1]);
	}

}
