/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
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

/**
 * 
 * History:<br>
 * 
 *  <br>   JAN 30, 2014    (Lei Wang) Initial release.
 */
public class CFMenuBar extends CFComponent {
	
	Menu menubar = null;
	
	public CFMenuBar() {
		super();		
	}
	
	protected Menu newLibComponent(WebElement webelement) throws SeleniumPlusException{
		return new Menu(webelement);
	}
	
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
	
}
	
