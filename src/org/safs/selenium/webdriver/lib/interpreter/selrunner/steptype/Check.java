/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.safs.selenium.util.JavaScriptFunctions;
import org.safs.selenium.webdriver.lib.ComboBox;
import org.safs.selenium.webdriver.lib.SearchObject;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.interpreter.WDLocator.WDType;
import org.safs.selenium.webdriver.lib.interpreter.WDScriptFactory;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRUtilities;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;
import org.safs.text.Comparator;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.steptype.SetElementSelected;

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
