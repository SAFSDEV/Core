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

import org.safs.selenium.webdriver.lib.interpreter.WDLocator;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRUtilities;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.Utils;

import com.sebuilder.interpreter.Getter;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestRun;

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
		if(params.length>2) Utils.setParam(step, this, params[2]);
	}

	@Override
	public String cmpParamName() {
		return null;
	}
}
