/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.safs.selenium.webdriver.lib.WDLibrary;

import com.sebuilder.interpreter.TestRun;

public class AddSelection extends Select {

	@Override
	protected boolean performSelect(WebElement select, WebElement option, TestRun ctx){
		try{
			Actions action = new Actions(ctx.driver());
			action.keyDown(Keys.CONTROL);
			action.perform();
			
			option.click();
			
			action = new Actions(ctx.driver());
			action.keyUp(Keys.CONTROL);
			action.perform();
			return true;
		}catch(Exception x){
			ctx.log().error("Select Option CTRL+click "+ x.getClass().getSimpleName()+", "+ x.getMessage());
			return false;
		}
	}
}
