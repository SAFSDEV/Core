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
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRUtilities;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.steptype.SendKeysToElement;
import com.sebuilder.interpreter.steptype.SetElementSelected;

public class Type extends SendKeysToElement implements SRunnerType {

	public static final String TEXT_PARAM = "text";
	@Override
	public void processParams(Step step, String[] params) {
		SRUtilities.setLocatorParam(step, params[1]);
		step.stringParams.put(TEXT_PARAM, params[2]);
	}
}
