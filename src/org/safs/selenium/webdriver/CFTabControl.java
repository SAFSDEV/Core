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
 * APR 21, 2014    (Lei Wang) Initial release.
 * OCT 30, 2018    (Lei Wang) Added method waitReady(): wait the component ready before processing it.
 * NOV 02, 2018    (Lei Wang) Modified method waitReady(): call super-class's waitReady(), if web-element is already ready then we will not wait here.
 *                                                        don't wait 'ready' of click-able for certain keywords, this 'ready' status has been handled in Element.getClickableWebElement().
 */
package org.safs.selenium.webdriver;

import org.openqa.selenium.WebElement;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.model.commands.TabControlFunctions;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.TabControl;
import org.safs.tools.stringutils.StringUtilities;

public class CFTabControl extends CFComponent {

	TabControl tabcontrol;

	public CFTabControl() {
		super();
	}

	@Override
	protected TabControl newLibComponent(WebElement webelement) throws SeleniumPlusException{
		return new TabControl(webelement);
	}

	@Override
	protected void localProcess(){
		String debugmsg = StringUtils.debugmsg(getClass(), "localProcess");

		if (action != null) {
			String msg = null;
			String detail = null;

			try{
				super.localProcess();
				tabcontrol = (TabControl) libComponent;

				Log.debug(debugmsg+" processing command '"+action+"' with parameters "+params);
				iterator = params.iterator();

				if(action.equalsIgnoreCase(TabControlFunctions.CLICKTAB_KEYWORD)
				   ||action.equalsIgnoreCase(TabControlFunctions.CLICKTABCONTAINS_KEYWORD)
				   ||action.equalsIgnoreCase(TabControlFunctions.MAKESELECTION_KEYWORD)
				   ||action.equalsIgnoreCase(TabControlFunctions.SELECTTAB_KEYWORD)
				   ||action.equalsIgnoreCase(TabControlFunctions.SELECTTABINDEX_KEYWORD)
				   ||action.equalsIgnoreCase(TabControlFunctions.UNVERIFIEDCLICKTAB_KEYWORD)){

					if(params.size() < 1){
						issueParameterCountFailure();
						return;
					}

					if(processWithOneParameter()){
						testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
						msg = genericText.convert("success3", windowName +":"+ compName + " "+ action +" successful.",
								windowName, compName, action);
						log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
					}

				}else{
					testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
					Log.warn(debugmsg+action+" could not be handled here.");
				}

			}catch(Exception e){
				Log.error(debugmsg+"Selenium TabControl Error processing '"+action+"'.", e);
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				msg = getStandardErrorMessage(windowName +":"+ compName +" "+ action);
				detail = "Met Exception "+e.getMessage();
				log.logMessage(testRecordData.getFac(),msg, detail, FAILED_MESSAGE);
			}
		}
	}

	/**
	 * @return boolean true if the keyword has been handled successfully;<br>
	 *                 false if the keyword should not be handled in this method.<br>
	 * @throws SeleniumPlusException
	 */
	private boolean processWithOneParameter() throws SAFSException{
		String debugmsg = StringUtils.debugmsg(getClass(), "processWithOneParameter");
		String parameter = (String) iterator.next();
		int matchIndex = 1;
		if(iterator.hasNext()){
			matchIndex = StringUtilities.parseIndex((String)iterator.next());
		}
		matchIndex--;//convert 1-based index to 0-based index

		if(action.equalsIgnoreCase(TabControlFunctions.CLICKTAB_KEYWORD)
		  ||action.equalsIgnoreCase(TabControlFunctions.MAKESELECTION_KEYWORD)
		  ||action.equalsIgnoreCase(TabControlFunctions.SELECTTAB_KEYWORD)){
			tabcontrol.selectTab(parameter, false, matchIndex, true);

		}else if(action.equalsIgnoreCase(TabControlFunctions.CLICKTABCONTAINS_KEYWORD)){
			tabcontrol.selectTab(parameter, true, matchIndex, true);

		}else if(action.equalsIgnoreCase(TabControlFunctions.SELECTTABINDEX_KEYWORD)){
			int index = StringUtilities.getIndex(parameter);
			tabcontrol.selectTab(index-1, true);

		}else if(action.equalsIgnoreCase(TabControlFunctions.UNVERIFIEDCLICKTAB_KEYWORD)){
			tabcontrol.selectTab(parameter, false, matchIndex, false);

		}else{
			testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
			Log.warn(debugmsg+action+" could not be handled here.");
			return false;
		}

		return true;
	}

	@Override
	protected WebElement waitReady(WebElement element){
		WebElement readyElement = super.waitReady(element);
//		if(!ready){
//			//WE don't need to wait element to be click-able here: TabControl's click actions will be handled by AbstractSelectable.clickElement(), which
//			//will click at web-element returned by Element.getClickableWebElement() in which we wait the web-element to be click-able.
//			readyElement = waiter.until(ExpectedConditions.elementToBeClickable(element));
//		}

		return readyElement;
	}

}
