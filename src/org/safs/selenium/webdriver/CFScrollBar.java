/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

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
}
