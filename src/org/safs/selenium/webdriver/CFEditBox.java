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
 *  JAN 07, 2014    (DHARMESH4) Initial release.
 *  JUL 02, 2015    (Tao Xie) Add doSetTextCharacters(): set edit box text without special key dealing.
 *                           Change doSetTextValue(): set edit box text with special key dealing.
 *  DEC 09, 2015    (Lei Wang) Refactor to reduce redundancy.
 *                            Add a loop to reenter the text if the verification fails. Double check the verification.
 *  OCT 30, 2018    (Lei Wang) Added method waitReady(): wait the component ready before processing it.
 *  NOV 02, 2018    (Lei Wang) Modified method waitReady(): call super-class's waitReady(), if web-element is already ready then we will not wait here.
 */
package org.safs.selenium.webdriver;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.model.commands.EditBoxFunctions;
import org.safs.selenium.webdriver.lib.EditBox;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;

/**
 * Handle the keywords related to 'EditBox', such as SetTextValue, SetTextCharacters etc.
 *
 */
public class CFEditBox extends CFComponent {

	/** "EditBox" */
	public static final String LIBRARY_NAME = CFEditBox.class.getSimpleName().substring("CF".length());

    private EditBox editbox;

	public CFEditBox() {
		super();
	}

	@Override
	protected EditBox newLibComponent(WebElement webelement) throws SeleniumPlusException{
		return new EditBox(webelement);
	}

	@Override
	protected void localProcess(){
		String debugmsg = StringUtils.debugmsg(getClass(), "localProcess");
		if (action != null) {
			IndependantLog.debug(debugmsg+" processing command '"+action+"'...");

			try{
				super.localProcess();
				editbox = (EditBox) libComponent;

				if(EditBoxFunctions.SETTEXTCHARACTERS_KEYWORD.equalsIgnoreCase(action)) {
					doSetText(LIBRARY_NAME, true, true);
				} else if(EditBoxFunctions.SETUNVERIFIEDTEXTCHARACTERS_KEYWORD.equalsIgnoreCase(action)) {
					doSetText(LIBRARY_NAME, true, false);
				} else if(EditBoxFunctions.SETTEXTVALUE_KEYWORD.equalsIgnoreCase(action)){
					doSetText(LIBRARY_NAME, false, true);
				} else if(EditBoxFunctions.SETUNVERIFIEDTEXTVALUE_KEYWORD.equalsIgnoreCase(action)) {
					doSetText(LIBRARY_NAME, false, false);
				}
			}catch(Exception e){
				IndependantLog.error(debugmsg+"Error performing "+ action, e);
				issueErrorPerformingActionOnX(compName, e.getMessage());
			}
		}else{

		}
	}

	@Override
	protected WebElement waitReady(WebElement element){
		WebElement readyElement = super.waitReady(element);
		if(!ready){
			//We want to input something into the edit box, so we need to wait that the check-box is click-able
			readyElement = waiter.until(ExpectedConditions.elementToBeClickable(element));
		}
		return readyElement;
	}
}
