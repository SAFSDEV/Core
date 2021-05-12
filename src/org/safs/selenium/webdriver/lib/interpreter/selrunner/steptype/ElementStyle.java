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
 * @date 2018-01-04    (Lei Wang) Initial release.
 */
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.safs.selenium.webdriver.lib.interpreter.selrunner.Constants;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRUtilities;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.Utils;

import com.sebuilder.interpreter.Step;

/**
 * @author Lei Wang
 *
 * This a getter class for get element's css style, its parameters are:<br>
 * <b>parameter[0]</b> the command itself, such as storeElementStyle, verifyElementStyle etc.<br>
 * <b>parameter[1]</b> the element locator.<br>
 * <b>parameter[2]</b> the element's css style name.<br>
 * <b>parameter[3]</b> the variable name (if the command begins with store); the value to compare (if the command begins with verify).<br>
 */
public class ElementStyle extends com.sebuilder.interpreter.steptype.ElementStyle implements SRunnerType{
	@Override
	public void processParams(Step step, String[] params) {
		SRUtilities.setLocatorParam(step, params[1]);
		step.stringParams.put(Constants.PARAM_PROPERTY_NAME, params[2]);
		if(params.length>2) Utils.setParam(step, this, params[3]);
	}
}
