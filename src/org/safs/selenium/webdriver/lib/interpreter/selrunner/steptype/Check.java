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
import org.safs.selenium.webdriver.lib.interpreter.WDScriptFactory;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRUtilities;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

public class Check implements StepType, SRunnerType {

	@Override
	public void processParams(Step step, String[] params) {
		SRUtilities.setLocatorParam(step, params[1]);
	}

	@Override
	public boolean run(TestRun ctx) {

		// select locator
		WebElement checkbox = ctx.locator(WDScriptFactory.LOCATOR_PARAM).find(ctx);
		if(checkbox == null) {
			ctx.log().error("Check did not find any matching WebElement.");
			return false;
		}
		try{
			if(! checkbox.isSelected()){
				checkbox.click();
			}else{
				ctx.log().info("Item is already selected. No attempt to Check will be made.");
			}
			return true;
		}catch(Exception x){
			ctx.log().error("Check "+ x.getClass().getSimpleName()+", "+ x.getMessage());
			return false;
		}
	}
}
