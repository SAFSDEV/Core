/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
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
