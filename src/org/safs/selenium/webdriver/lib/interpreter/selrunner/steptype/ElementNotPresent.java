/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.safs.selenium.webdriver.lib.interpreter.WDLocator;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRUtilities;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Getter;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.steptype.ElementPresent;

public class ElementNotPresent implements Getter, SRunnerType {

	@Override
	public String get(TestRun ctx) {
		Locator l = ctx.currentStep().locatorParams.get("locator");
		WDLocator wdl = (l instanceof WDLocator) ? (WDLocator)l : new WDLocator(l.type,l.value);
		if(!(l instanceof WDLocator)) ctx.currentStep().locatorParams.put("locator", wdl);
		return String.valueOf(wdl.findElementNotPresent(ctx));
	}

	@Override
	public void processParams(Step step, String[] params) {
		SRUtilities.setLocatorParam(step, params[1]);
	}

	@Override
	public String cmpParamName() {
		return null;
	}
}
