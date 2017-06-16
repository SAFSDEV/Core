/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2017年6月14日    (Lei Wang) Initial release.
 * 2017年6月16日    (Lei Wang) Modified processParams(): properly set parameter, so that storeAlertText is supported.
 */
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.Utils;

import com.sebuilder.interpreter.Step;

/**
 * @author Lei Wang
 */
public class AlertText extends com.sebuilder.interpreter.steptype.AlertText implements SRunnerType{
	@Override
	public void processParams(Step step, String[] params) {
		Utils.setParam(step, this, params[1]);
	}
}