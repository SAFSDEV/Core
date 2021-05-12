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
 *
 * History:<br>
 *
 *  JAN 29, 2014    (Lei Wang) Initial release.
 *  OCT 30, 2018    (Lei Wang) Added method waitReady(): wait the component ready before processing it.
 *  NOV 02, 2018    (Lei Wang) Modified method waitReady(): call super-class's waitReady(), if web-element is already ready then we will not wait here.
 */
package org.safs.selenium.webdriver;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.model.commands.CheckBoxFunctions;
import org.safs.selenium.webdriver.lib.CheckBox;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;

public class CFCheckBox extends CFComponent {

	CheckBox checkbox;

	public CFCheckBox() {
		super();
	}

	@Override
	protected CheckBox newLibComponent(WebElement webelement) throws SeleniumPlusException{
		return new CheckBox(webelement);
	}

	@Override
	protected void localProcess() throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(getClass(), "localProcess");

		if (action != null) {
			String msg = null;
			String detail = null;

			try{
				super.localProcess();
				checkbox = (CheckBox) libComponent;

				Log.debug(debugmsg+" processing command '"+action+"' with parameters "+params);
				iterator = params.iterator();

				if(action.equalsIgnoreCase(CheckBoxFunctions.CHECK_KEYWORD)){
					checkbox.check();

					testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
					msg = genericText.convert("success3", windowName +":"+ compName + " "+ action +" successful.",
							windowName, compName, action);
					log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);

				}else if(action.equalsIgnoreCase(CheckBoxFunctions.UNCHECK_KEYWORD)){
					checkbox.uncheck();

					testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
					msg = genericText.convert("success3", windowName +":"+ compName + " "+ action +" successful.",
							windowName, compName, action);
					log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);

				}else{
					testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
					Log.warn(debugmsg+action+" could not be handled here.");
				}

			}catch(SAFSException e){
				Log.error(debugmsg+"Selenium CheckBox Error processing '"+action+"'.", e);
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				msg = getStandardErrorMessage(windowName +":"+ compName +" "+ action);
				detail = "Met Exception "+e.getMessage();
				log.logMessage(testRecordData.getFac(),msg, detail, FAILED_MESSAGE);
			}
		}
	}

	@Override
	protected WebElement waitReady(WebElement element){
		WebElement readyElement = super.waitReady(element);
		if(!ready){
			//We only handle 2 actions: check and un-check, so we just need to wait that the check-box is click-able
			waiter.until(ExpectedConditions.elementToBeClickable(element));
			ready = true;
		}

		return readyElement;
	}

}
