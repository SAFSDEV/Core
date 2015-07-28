/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver;

import org.openqa.selenium.WebElement;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.model.commands.CheckBoxFunctions;
import org.safs.selenium.webdriver.lib.CheckBox;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;

/**
 * 
 * History:<br>
 * 
 *  <br>   JAN 29, 2014    (SBJLWA) Initial release.
 */
public class CFCheckBox extends CFComponent {

	CheckBox checkbox;

	public CFCheckBox() {
		super();
	}
	
	protected CheckBox newLibComponent(WebElement webelement) throws SeleniumPlusException{
		return new CheckBox(webelement);
	}
	
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


}
