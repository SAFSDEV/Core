/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.safs.robot.Robot;
import org.safs.selenium.util.JavaScriptFunctions;
import org.safs.selenium.webdriver.lib.ComboBox;
import org.safs.selenium.webdriver.lib.SearchObject;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.interpreter.WDLocator.WDType;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRUtilities;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.steptype.SendKeysToElement;
import com.sebuilder.interpreter.steptype.SetElementSelected;

public class TypeKeys implements StepType, SRunnerType {

	public static final String TEXT_PARAM = "text";
	@Override
	public void processParams(Step step, String[] params) {
		step.stringParams.put(TEXT_PARAM, params[1]);
	}
	@Override
	public boolean run(TestRun ctx) {
		//int d = Robot.DEFAULT_MILLIS_BETWEEN_KEYSTROKES;
		try {
			//d = WDLibrary.getDelayBetweenKeystrokes();
			//if (d < 10) WDLibrary.setDelayBetweenKeystrokes(10);
			WDLibrary.inputKeys(null, ctx.string(TEXT_PARAM));
			return true;
		} catch (Exception e) {
			ctx.log().error("SelRunner Step TypeKeys "+ e.getClass().getSimpleName()+", "+ e.getMessage());
			return false;
		}finally{
			//try{WDLibrary.setDelayBetweenKeystrokes(d);}catch(Exception ignore){}
		}
	}
}
