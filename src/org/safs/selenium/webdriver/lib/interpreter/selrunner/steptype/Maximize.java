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
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

/**
 * Maximize the browser with the optionally specified browserid.
 * By default, will maximize the last used webdriver browser.
 * @author Carl Nagle
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
