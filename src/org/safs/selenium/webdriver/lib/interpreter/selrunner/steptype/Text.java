/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.openqa.selenium.WebElement;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.interpreter.WDLocator;
import org.safs.selenium.webdriver.lib.interpreter.WDScriptFactory;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRUtilities;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Getter;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

public class Text extends com.sebuilder.interpreter.steptype.Text implements SRunnerType {

	@Override
	public void processParams(Step step, String[] params) {
		SRUtilities.setLocatorParam(step, params[1]);
		try{ 
			if(params[2].length() > 0){
				step.stringParams.put(WDScriptFactory.TEXT_PARAM, params[2]);
			}else{
				step.stringParams.remove(WDScriptFactory.TEXT_PARAM);
			}
		}catch(Throwable ignore){
			step.stringParams.remove(WDScriptFactory.TEXT_PARAM);
		}
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
		String tt = e.getText();
		String t = tt == null ? "" : tt;
		Object ov = WDLibrary.getValue(e, new String[]{"value","text","placeholder"});
		String v = ov == null ? "" : ov.toString();	
		ctx.log().debug("Step Text getText() received: "+ t);
		ctx.log().debug("Step Text getValu() received: "+ v);
		String rc = t.length() > 0 ? t: v;
		ctx.log().debug("Step Text.getText() returning: "+ rc);
		return rc;
	}
}
