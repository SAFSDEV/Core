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

import org.openqa.selenium.WebElement;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRUtilities;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.Utils;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestRun;

/**
 * Gets the text of an element. This works for any element that contains text.
 *
 */
public class Text extends com.sebuilder.interpreter.steptype.Text implements SRunnerType {
	@Override
	public void processParams(Step step, String[] params) {
		SRUtilities.setLocatorParam(step, params[1]);
		//Lei: we should not remove it from variable store. It will cause NullPointerException in Verify, Assert and WaitFor.
//		try{
//			if(params[2].length() > 0){
//				step.stringParams.put(WDScriptFactory.TEXT_PARAM, params[2]);
//			}else{
//				step.stringParams.remove(WDScriptFactory.TEXT_PARAM);
//			}
//		}catch(Throwable ignore){
//			step.stringParams.remove(WDScriptFactory.TEXT_PARAM);
//		}
		if(params.length>2) Utils.setParam(step, this, params[2]);
	}

	@Override
	public String get(TestRun ctx) {
		WebElement e = ctx.locator("locator").find(ctx);
		if(e == null) {
			ctx.log().debug("Step Text did not successfully find the WebElement.");
			return null;
		}else{
			ctx.log().debug("Step Text found the WebElement.");
		}
		return WDLibrary.getText(e);
	}
}
