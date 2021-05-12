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
package org.safs.selenium.webdriver.lib.interpreter;

import org.openqa.selenium.WebElement;
import org.safs.selenium.webdriver.lib.WDLibrary;

import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.steptype.ClickElement;

/**
 * @author Carl Nagle
 *
 */
public class WDClickElement extends ClickElement {

	public WDClickElement() {
		super();
	}

	@Override
	public boolean run(TestRun ctx){
		ctx.getLog().debug("WDClick executing custom Find and Click via WDLibrary.");
		try{
			WebElement e = ctx.locator(WDScriptFactory.LOCATOR_PARAM).find(ctx);
			if(e == null){
				ctx.getLog().info("WDClick locator did NOT find the expected WebElement!");
				return false;
			}else{
				ctx.getLog().info("WDClick locator found the expected WebElement.");
			}
			WDLibrary.windowSetFocus(e);
			WDLibrary.click(e);
			ctx.getLog().info("WDClick finished without known errors.");
			return true;
		}
		catch(Throwable t){
			ctx.getLog().debug("WDClick error: "+ t.getMessage(), t);
		}
		return false;
	}
}
