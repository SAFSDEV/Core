/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2017年6月15日    (Lei Wang) Initial release.
 */
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.Utils;

import com.sebuilder.interpreter.Step;

/**
 * Get the current url location.
 * @author Lei Wang
 */
public class Location extends com.sebuilder.interpreter.steptype.CurrentUrl implements SRunnerType {
	@Override
	public void processParams(Step step, String[] params) {
		//set the first parameter
		Utils.setParam(step, this, params[1]);
	}
}
