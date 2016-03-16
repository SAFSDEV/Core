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
			WDLibrary.executeScript("arguments[0].selected=true;", new Object[]{option});			
		}
		catch(Exception x){
			ctx.log().error("Select Option select "+ x.getClass().getSimpleName()+", "+ x.getMessage());
			return false;
		}
		return true;
	}
}
