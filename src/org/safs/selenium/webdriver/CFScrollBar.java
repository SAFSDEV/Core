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
 * NOV 07, 2018    (Lei Wang) Added method waitReady(): currently we only call the super class method to make sure the page is fully loaded.
 *                                                     It might be enough for handling SAP scroll bar, we call the native SAP javascript APIs.
 *                                                     For other domains (HTML, DOJO etc.), we may need add more codes to wait "ready".
 */
package org.safs.selenium.webdriver;

import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.model.commands.ScrollBarFunctions;
import org.safs.selenium.webdriver.lib.ScrollBar;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.text.FAILStrings;

public class CFScrollBar extends CFComponent {

	ScrollBar scrollbar;

	public CFScrollBar () {
		super();
	}

	@Override
	protected ScrollBar newLibComponent(WebElement webelement) throws SeleniumPlusException{
		return new ScrollBar(webelement);
	}

	/**
	 * <br><em>Purpose:</em> The scrollbar actions handled here are:
	 * <br><ul>
	 * <li>OneDown
	 * <li>OneLeft
	 * <li>OneRight
	 * <li>OneUp
	 * <li>PageDown
	 * <li>PageLeft
	 * <li>PageRight
	 * <li>PageUp
	 * </ul><br>
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void localProcess() throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(getClass(), "localProcess");

		String msg = null;
		String detail = null;

		try{
			super.localProcess();
			scrollbar = (ScrollBar) libComponent;

			IndependantLog.debug(debugmsg+" processing command '"+action+"' with parameters "+params+"; win: "+ windowName +"; comp: "+compName);
			iterator = params.iterator();

			//optional parameter 'times to perform action'
			int steps=1;
			if(iterator.hasNext()){
				String param = iterator.next();

				try {
					Integer ni = new Integer(param);
					steps = ni.intValue();
					if(steps < 1){
						IndependantLog.error(debugmsg+": number should be >= 1: "+param);
						throw new NumberFormatException(": number should be >= 1: "+param);
					}
				} catch (NumberFormatException nfe) {
					testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
					log.logMessage(testRecordData.getFac(), ": bad number: "+param, FAILED_MESSAGE);
					return;
				}
			}

			if (action != null) {

				if (ScrollBarFunctions.ONEDOWN_KEYWORD.equalsIgnoreCase(action)){
					scrollbar.scroll(ScrollBar.TYPE_SCROLLBAR_VERTICAL, steps);

				} else if (ScrollBarFunctions.ONEUP_KEYWORD.equalsIgnoreCase(action)) {
					scrollbar.scroll(ScrollBar.TYPE_SCROLLBAR_VERTICAL, -steps);

				} else if (ScrollBarFunctions.ONELeft_KEYWORD.equalsIgnoreCase(action)) {
					scrollbar.scroll(ScrollBar.TYPE_SCROLLBAR_HORIZONTAL, -steps);

				} else if (ScrollBarFunctions.ONERIGHT_KEYWORD.equalsIgnoreCase(action)) {
					scrollbar.scroll(ScrollBar.TYPE_SCROLLBAR_HORIZONTAL, steps);

				} else if (ScrollBarFunctions.PAGEDOWN_KEYWORD.equalsIgnoreCase(action)) {
					scrollbar.page(ScrollBar.TYPE_SCROLLBAR_VERTICAL, steps);

				} else if (ScrollBarFunctions.PAGEUP_KEYWORD.equalsIgnoreCase(action)) {
					scrollbar.page(ScrollBar.TYPE_SCROLLBAR_VERTICAL, -steps);

				} else if (ScrollBarFunctions.PAGELEFT_KEYWORD.equalsIgnoreCase(action)) {
					scrollbar.page(ScrollBar.TYPE_SCROLLBAR_HORIZONTAL, -steps);

				} else if (ScrollBarFunctions.PAGERIGHT_KEYWORD.equalsIgnoreCase(action)) {
					scrollbar.page(ScrollBar.TYPE_SCROLLBAR_HORIZONTAL, steps);

				}else{
					msg = failedText.convert(FAILStrings.SUPPORT_NOT_FOUND, "Support for"+action+"not found!", action);
					IndependantLog.error(debugmsg+msg);
					throw new SeleniumPlusException(msg);
				}

				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
				detail = "scroll '"+steps+"' "+ (action.toLowerCase().startsWith("page")? "pages.":"steps.");
				componentSuccessMessage(detail);

			}else{
				throw new SeleniumPlusException("The action is null.");
			}

		}catch(SeleniumPlusException e){
			IndependantLog.error(debugmsg+"Selenium ScrollBar Error processing '"+action+"'.", e);
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			msg = getStandardErrorMessage(windowName +":"+ compName +" "+ action);
			detail = "Met Exception "+e.getMessage();
			log.logMessage(testRecordData.getFac(),msg, detail, FAILED_MESSAGE);
		}
	}

	@Override
	protected WebElement waitReady(WebElement element){
		WebElement readyElement = super.waitReady(element);

		//We have waited the page fully loaded in super.waitReady(), is that enough for executing javascript?
		if(!ready){
//			if (ScrollBarFunctions.ONEDOWN_KEYWORD.equalsIgnoreCase(action)
//				|| ScrollBarFunctions.ONEUP_KEYWORD.equalsIgnoreCase(action)
//				|| ScrollBarFunctions.ONELeft_KEYWORD.equalsIgnoreCase(action)
//				|| ScrollBarFunctions.ONERIGHT_KEYWORD.equalsIgnoreCase(action)) {
////				scrollbar.scroll(ScrollBar.TYPE_SCROLLBAR_HORIZONTAL, steps);
//				//TODO scrollbar.scroll() will call the javascript API for SAP object, what should we wait?
////				readyElement = waiter.until(ExpectedConditions.elementToBeClickable(element));
//
//			} else if (ScrollBarFunctions.PAGEDOWN_KEYWORD.equalsIgnoreCase(action)
//					|| ScrollBarFunctions.PAGEUP_KEYWORD.equalsIgnoreCase(action)
//					|| ScrollBarFunctions.PAGELEFT_KEYWORD.equalsIgnoreCase(action)
//					|| ScrollBarFunctions.PAGERIGHT_KEYWORD.equalsIgnoreCase(action)) {
////				scrollbar.page(ScrollBar.TYPE_SCROLLBAR_HORIZONTAL, steps);
//				//TODO scrollbar.page() will call the javascript API for SAP object, what should we wait?
////				readyElement = waiter.until(ExpectedConditions.elementToBeClickable(element));
//
//			}
		}

		return readyElement;
	}
}
