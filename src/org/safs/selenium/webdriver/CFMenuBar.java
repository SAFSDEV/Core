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
 *  JAN 30, 2014    (Lei Wang) Initial release.
 *  NOV 02, 2018    (Lei Wang) Added method waitReady(): wait the component ready before processing it.
 */
package org.safs.selenium.webdriver;

import org.openqa.selenium.WebElement;
import org.safs.GuiObjectRecognition;
import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.model.commands.JavaMenuFunctions;
import org.safs.selenium.webdriver.lib.Menu;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.model.TextMatchingCriterion;

public class CFMenuBar extends CFComponent {

	Menu menubar = null;

	public CFMenuBar() {
		super();
	}

	@Override
	protected Menu newLibComponent(WebElement webelement) throws SeleniumPlusException{
		return new Menu(webelement);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void localProcess(){
		String debugmsg = StringUtils.debugmsg(getClass(), "localProcess");

		if (action != null) {
			String msg = null;
			String detail = null;

			try{
				super.localProcess();
				menubar = (Menu) libComponent;

				IndependantLog.debug(debugmsg+" processing command '"+action+"' with parameters "+params);
				iterator = params.iterator();

				//Handle keywords with one-required parameter
				if(action.equalsIgnoreCase(JavaMenuFunctions.SELECTMENUITEM_KEYWORD)
				   ||action.equalsIgnoreCase(JavaMenuFunctions.SELECTMENUITEMCONTAINS_KEYWORD)
				   ||action.equalsIgnoreCase(JavaMenuFunctions.SELECTUNVERIFIEDMENUITEM_KEYWORD)
				   ||action.equalsIgnoreCase(JavaMenuFunctions.VERIFYMENUITEM_KEYWORD)
				   ||action.equalsIgnoreCase(JavaMenuFunctions.VERIFYMENUITEMCONTAINS_KEYWORD)
				   ){

					if(params.size() < 1){
						issueParameterCountFailure();
						return;
					}

					if(processWithOneRequiredParameter()){
						testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
						msg = genericText.convert("success3", windowName +":"+ compName + " "+ action +" successful.",
								windowName, compName, action);
						log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
					}

				}else{
					testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
					IndependantLog.warn(debugmsg+action+" could not be handled here.");
				}

			}catch(Exception e){
				IndependantLog.error(debugmsg+"Selenium TreeView Error processing '"+action+"'.", e);
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				msg = getStandardErrorMessage(windowName +":"+ compName +" "+ action);
				detail = "Met Exception "+StringUtils.debugmsg(e);
				log.logMessage(testRecordData.getFac(),msg, detail, FAILED_MESSAGE);
			}
		}
	}

	private boolean processWithOneRequiredParameter() throws SAFSException{
		String debugmsg = StringUtils.debugmsg(getClass(), "processWithOneRequiredParameter");
		TextMatchingCriterion criterion = null;

		String requiredParam = (String) iterator.next();
		//Handle optionl parameters
		String matchIndex = null;
		String expectedStatus = null;

		if(action.equalsIgnoreCase(JavaMenuFunctions.SELECTMENUITEM_KEYWORD)
		   ||action.equalsIgnoreCase(JavaMenuFunctions.SELECTMENUITEMCONTAINS_KEYWORD)
			||action.equalsIgnoreCase(JavaMenuFunctions.SELECTUNVERIFIEDMENUITEM_KEYWORD)
		   ){

			//'matchIndex' optional parameter
			if(iterator.hasNext()) matchIndex = (String)iterator.next();
		}
		else if(action.equalsIgnoreCase(JavaMenuFunctions.VERIFYMENUITEM_KEYWORD)
				||action.equalsIgnoreCase(JavaMenuFunctions.VERIFYMENUITEMCONTAINS_KEYWORD)
				){
			//'expectedStatus' optional parameter
			if(iterator.hasNext()) expectedStatus = (String)iterator.next();
			if(iterator.hasNext()) matchIndex = (String)iterator.next();

		}

		if(action.equalsIgnoreCase(JavaMenuFunctions.SELECTMENUITEM_KEYWORD)
			){
			criterion = new TextMatchingCriterion(requiredParam, false, matchIndex, GuiObjectRecognition.DEFAULT_PATH_SEPARATOR);
			menubar.selectItem(criterion, true, null, null, WDLibrary.MOUSE_BUTTON_LEFT);

		}else if(action.equalsIgnoreCase(JavaMenuFunctions.SELECTMENUITEMCONTAINS_KEYWORD)
			){
			criterion = new TextMatchingCriterion(requiredParam, true, matchIndex, GuiObjectRecognition.DEFAULT_PATH_SEPARATOR);
			menubar.selectItem(criterion, true, null, null, WDLibrary.MOUSE_BUTTON_LEFT);

		}else if(action.equalsIgnoreCase(JavaMenuFunctions.SELECTUNVERIFIEDMENUITEM_KEYWORD)
			){
			criterion = new TextMatchingCriterion(requiredParam, false, matchIndex, GuiObjectRecognition.DEFAULT_PATH_SEPARATOR);
			menubar.selectItem(criterion, false, null, null, WDLibrary.MOUSE_BUTTON_LEFT);

		}else if(action.equalsIgnoreCase(JavaMenuFunctions.VERIFYMENUITEM_KEYWORD)
				){
			criterion = new TextMatchingCriterion(requiredParam, false, matchIndex, GuiObjectRecognition.DEFAULT_PATH_SEPARATOR);
			menubar.verifyMenuItem(criterion, expectedStatus);

		}else if(action.equalsIgnoreCase(JavaMenuFunctions.VERIFYMENUITEMCONTAINS_KEYWORD)
				){
			criterion = new TextMatchingCriterion(requiredParam, true, matchIndex, GuiObjectRecognition.DEFAULT_PATH_SEPARATOR);
			menubar.verifyMenuItem(criterion, expectedStatus);

		}
		else{
			testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
			IndependantLog.warn(debugmsg+action+" could not be handled here.");
			return false;
		}

		return true;
	}

	@Override
	protected WebElement waitReady(WebElement element){
		WebElement readyElement = super.waitReady(element);

		if(!ready){
			if(action.equalsIgnoreCase(JavaMenuFunctions.VERIFYMENUITEM_KEYWORD)
			 ||action.equalsIgnoreCase(JavaMenuFunctions.VERIFYMENUITEMCONTAINS_KEYWORD)
			 ){
				//TODO What should we wait for the menu's verification?
				//For verification, we firstly call getContent() to get the whole menu's content, and then we verify.
				//In the super class, its waitReady() method has waited the page fully loaded.
				//Currently we call the SAP javascript APIs to implement getContent(), "page fully loaded" might be enough

			}
			//WE don't need to wait element to be click-able here: Menubar's click actions will be handled by AbstractSelectable.clickElement(), which
			//will click at web-element returned by Element.getClickableWebElement() in which we wait the web-element to be click-able.
//			else if(action.equalsIgnoreCase(JavaMenuFunctions.SELECTMENUITEM_KEYWORD)
//					||action.equalsIgnoreCase(JavaMenuFunctions.SELECTMENUITEMCONTAINS_KEYWORD)
//					||action.equalsIgnoreCase(JavaMenuFunctions.SELECTUNVERIFIEDMENUITEM_KEYWORD)
//					){
//				readyElement = waiter.until(ExpectedConditions.elementToBeClickable(element));
//			}

		}

		return readyElement;
	}
}

