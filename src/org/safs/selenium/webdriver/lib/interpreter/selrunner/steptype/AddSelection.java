/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.sebuilder.interpreter.TestRun;

public class AddSelection extends Select {

	@Override
	protected boolean performSelect(WebElement select, WebElement option, TestRun ctx){
		Actions actions = new Actions(ctx.driver());
		actions.keyDown(Keys.CONTROL)
		    .click(option)
		    .keyUp(Keys.CONTROL)
		    .build()
		    .perform();
		return true;
	}
}
