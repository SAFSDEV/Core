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
/**
 * AUG 09, 2017 (Lei Wang) Used WDLibrary's keyPress and keyRelease as a backup, "W3C Actions API" has not been implemented in geckodriver.
 */
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import java.awt.event.KeyEvent;

import org.openqa.selenium.Keys;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.safs.selenium.webdriver.lib.WDLibrary;

import com.sebuilder.interpreter.TestRun;

public class AddSelection extends Select {

	@Override
	protected boolean performSelect(WebElement select, WebElement option, TestRun ctx){
		try{
			Actions action = null;
			try{
				action = new Actions(ctx.driver());
				action.keyDown(Keys.CONTROL);
				action.perform();
			}catch(UnsupportedCommandException uce){
				WDLibrary.keyPress(KeyEvent.VK_CONTROL);
			}

			option.click();

			try{
				action = new Actions(ctx.driver());
				action.keyUp(Keys.CONTROL);
				action.perform();
			}catch(UnsupportedCommandException uce){
				WDLibrary.keyRelease(KeyEvent.VK_CONTROL);
			}
			return true;
		}catch(Exception x){
			ctx.log().error("Select Option CTRL+click "+ x.getClass().getSimpleName()+", "+ x.getMessage());
			return false;
		}
	}
}
