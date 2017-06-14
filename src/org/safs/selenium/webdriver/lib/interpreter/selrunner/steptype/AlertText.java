/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2017年6月14日    (Lei Wang) Initial release.
 */
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Step;

/**
 * @author Lei Wang
 */
public class AlertText extends com.sebuilder.interpreter.steptype.AlertText implements SRunnerType{
	@Override
	public void processParams(Step step, String[] params) {
		step.stringParams.put(cmpParamName(), params[1]);
	}
}
