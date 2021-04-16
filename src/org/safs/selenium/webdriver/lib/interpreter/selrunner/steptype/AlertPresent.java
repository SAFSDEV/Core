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
/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * @date 2018-01-03    (Lei Wang) Initial release.
 */
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.Utils;

import com.sebuilder.interpreter.Step;

/**
 * @author Lei Wang
 *
 */
public class AlertPresent extends com.sebuilder.interpreter.steptype.AlertPresent implements SRunnerType{

	@Override
	public void processParams(Step step, String[] params) {
		Utils.setParam(step, this, params[1]);
	}

}
