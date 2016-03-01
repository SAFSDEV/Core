/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.steptype.SaveScreenshot;

public class CaptureEntirePageScreenshot extends SaveScreenshot implements SRunnerType {

	@Override
	public void processParams(Step step, String[] params) {
		step.stringParams.put("file", params[1]);
	}
}
