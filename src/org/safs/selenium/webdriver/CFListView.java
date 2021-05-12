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
 * APR 24, 2014    (Lei Wang) Initial release.
 * OCT 30, 2018    (Lei Wang) Added method waitReady(): wait the component ready before processing it.
 * NOV 02, 2018    (Lei Wang) Modified method waitReady(): call super-class's waitReady(), if web-element is already ready then we will not wait here.
 *                                                        don't wait 'ready' of click-able for certain keywords, this 'ready' status has been handled in Element.getClickableWebElement().
 */
package org.safs.selenium.webdriver;

import java.awt.Point;
import java.io.IOException;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.model.commands.ListViewFunctions;
import org.safs.selenium.webdriver.lib.ListView;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.model.Item;
import org.safs.selenium.webdriver.lib.model.TextMatchingCriterion;
import org.safs.text.GENKEYS;
import org.safs.tools.stringutils.StringUtilities;

public class CFListView extends CFComponent {

	ListView listview;

	public CFListView() {
		super();
	}

	@Override
	protected ListView newLibComponent(WebElement webelement) throws SeleniumPlusException{
		return new ListView(webelement);
	}

	@Override
	protected void localProcess(){
		String debugmsg = StringUtils.debugmsg(getClass(), "localProcess");

		if (action != null) {
			String msg = null;
			String detail = null;

			try{
				super.localProcess();
				listview = (ListView) libComponent;

				IndependantLog.debug(debugmsg+" processing command '"+action+"' with parameters "+params);
				iterator = params.iterator();

				//Handle keywords with one-required parameter
				if(action.equalsIgnoreCase(ListViewFunctions.ACTIVATEINDEX_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.ACTIVATEINDEXITEM_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.ACTIVATEPARTIALMATCH_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.ACTIVATETEXTITEM_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.ACTIVATEUNVERIFIEDTEXTITEM_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.CLICKINDEX_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.CLICKINDEXITEM_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.SELECTINDEX_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.SELECTINDEXITEM_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.SELECTPARTIALMATCH_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.SELECTTEXTITEM_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.SELECTUNVERIFIEDTEXTITEM_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.VERIFYITEMUNSELECTED_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.VERIFYLISTCONTAINS_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.VERIFYSELECTEDITEM_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.CAPTUREITEMSTOFILE_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.EXTENDSELECTIONTOTEXTITEM_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.SELECTANOTHERPARTIALMATCH_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.SELECTANOTHERTEXTITEM_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.SETLISTCONTAINS_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.RIGHTCLICKTEXTITEM_KEYWORD)
				   ||action.equalsIgnoreCase(ListViewFunctions.RIGHTCLICKUNVERIFIEDTEXTITEM_KEYWORD)
				   ){

					if(params.size() < 1){
						issueParameterCountFailure();
						return;
					}

					if(processWithOneRequiredParameter()){
						testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
						msg = genericText.convert(GENKEYS.SUCCESS_3A,
								windowName +":"+ compName + " "+ action +" successful  using "+params,
								windowName, compName, action, params.toString());
						log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
					}

				}else if(action.equalsIgnoreCase(ListViewFunctions.ACTIVATETEXTITEMCOORDS_KEYWORD)
						 || action.equalsIgnoreCase(ListViewFunctions.ACTIVATEUNVERIFIEDTEXTITEMCOORDS_KEYWORD)
						 || action.equalsIgnoreCase(ListViewFunctions.RIGHTCLICKTEXTITEMCOORDS_KEYWORD)
						 || action.equalsIgnoreCase(ListViewFunctions.RIGHTCLICKUNVERIFIEDTEXTITEMCOORDS_KEYWORD)
						 || action.equalsIgnoreCase(ListViewFunctions.SELECTINDEXITEMCOORDS_KEYWORD)
						 || action.equalsIgnoreCase(ListViewFunctions.SELECTTEXTITEMCOORDS_KEYWORD)
						 || action.equalsIgnoreCase(ListViewFunctions.SELECTUNVERIFIEDTEXTITEMCOORDS_KEYWORD)
						 ){
					if(params.size() < 2){
						issueParameterCountFailure();
						return;
					}

					if(processWithTwoRequiredParameters()){
						testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
						msg = genericText.convert(GENKEYS.SUCCESS_3A,
								windowName +":"+ compName + " "+ action +" successful  using "+params,
								windowName, compName, action, params.toString());
						log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
					}

				}else{
					testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
					IndependantLog.warn(debugmsg+action+" could not be handled here.");
				}

			}catch(Exception e){
				IndependantLog.error(debugmsg+"Selenium ListView Error processing '"+action+"'.", e);
				testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
				msg = getStandardErrorMessage(windowName +":"+ compName +" "+ action);
				detail = "Met Exception "+StringUtils.debugmsg(e);
				log.logMessage(testRecordData.getFac(),msg, detail, FAILED_MESSAGE);
			}
		}
	}

	/**
	 * process keywords who need at least one parameter; but it may be supplied with optional parameters.
	 * @return boolean true if the keyword has been handled successfully;<br>
	 *                 false if the keyword should not be handled in this method.<br>
	 * @throws SeleniumPlusException if there are any problem during handling keyword.
	 */
	private boolean processWithOneRequiredParameter() throws SAFSException{
		String debugmsg = StringUtils.debugmsg(getClass(), "processWithOneRequiredParameter");
		String requiredParam = (String) iterator.next();
		TextMatchingCriterion criterion = null;

		//Handle optionl parameters
		int matchIndex = 1;
		String encoding = null;
		String variable = null;

		if(action.equalsIgnoreCase(ListViewFunctions.ACTIVATETEXTITEM_KEYWORD)
		   ||action.equalsIgnoreCase(ListViewFunctions.SELECTTEXTITEM_KEYWORD)
		   ||action.equalsIgnoreCase(ListViewFunctions.ACTIVATEPARTIALMATCH_KEYWORD)
		   ||action.equalsIgnoreCase(ListViewFunctions.SELECTPARTIALMATCH_KEYWORD)
		   ||action.equalsIgnoreCase(ListViewFunctions.ACTIVATEUNVERIFIEDTEXTITEM_KEYWORD)
//		   ||action.equalsIgnoreCase(ListViewFunctions.VERIFYLISTCONTAINS_KEYWORD)
		   ||action.equalsIgnoreCase(ListViewFunctions.SELECTUNVERIFIEDTEXTITEM_KEYWORD)
		   ||action.equalsIgnoreCase(ListViewFunctions.RIGHTCLICKTEXTITEM_KEYWORD)
		   ||action.equalsIgnoreCase(ListViewFunctions.RIGHTCLICKUNVERIFIEDTEXTITEM_KEYWORD)
		   ){

			//'matchIndex' optional parameter
			if(iterator.hasNext()) {
				IndependantLog.info(debugmsg+" command '"+action+"' retrieving optional matchIndex...");
				matchIndex = StringUtilities.getIndex((String)iterator.next());
				IndependantLog.info(debugmsg+" command '"+action+"' matchIndex: "+ matchIndex);
			}
		}
		else if(action.equalsIgnoreCase(ListViewFunctions.CAPTUREITEMSTOFILE_KEYWORD)){
			//'encoding' optional parameter
			if(iterator.hasNext()) {
				IndependantLog.info(debugmsg+" command '"+action+"' retrieving optional encoding...");
				encoding = (String)iterator.next();
				IndependantLog.info(debugmsg+" command '"+action+"' encoding: "+ encoding);
			}

		}
		else if(action.equalsIgnoreCase(ListViewFunctions.SETLISTCONTAINS_KEYWORD)){
			//'variable' optional parameter
			if(iterator.hasNext()) {
				IndependantLog.info(debugmsg+" command '"+action+"' retrieving optional variableName...");
				variable = (String)iterator.next();
				IndependantLog.info(debugmsg+" command '"+action+"' variableName: "+ variable);
			}

		}

		matchIndex--;//convert 1-based index to 0-based index

		if(action.equalsIgnoreCase(ListViewFunctions.ACTIVATETEXTITEM_KEYWORD)){
			criterion = new TextMatchingCriterion(requiredParam, false, matchIndex);
			listview.activateItem(criterion, true, null, null);

		}else if(action.equalsIgnoreCase(ListViewFunctions.SELECTTEXTITEM_KEYWORD)){
			criterion = new TextMatchingCriterion(requiredParam, false, matchIndex);
			listview.selectItem(criterion, true);

		}else if(action.equalsIgnoreCase(ListViewFunctions.ACTIVATEPARTIALMATCH_KEYWORD)){
			criterion = new TextMatchingCriterion(requiredParam, true, matchIndex);
			listview.activateItem(criterion, false, null, null);

		}else if(action.equalsIgnoreCase(ListViewFunctions.SELECTPARTIALMATCH_KEYWORD)){
			criterion = new TextMatchingCriterion(requiredParam, true, matchIndex);
			listview.selectItem(criterion, false);

		}else if(action.equalsIgnoreCase(ListViewFunctions.ACTIVATEINDEX_KEYWORD)
				||action.equalsIgnoreCase(ListViewFunctions.ACTIVATEINDEXITEM_KEYWORD)){
			int index = StringUtilities.getIndex(requiredParam);
			listview.activateItem(index, false, null, null);

		}else if(action.equalsIgnoreCase(ListViewFunctions.CLICKINDEX_KEYWORD)
				||action.equalsIgnoreCase(ListViewFunctions.CLICKINDEXITEM_KEYWORD)){
			int index = StringUtilities.getIndex(requiredParam);
			listview.selectItem(index-1, false);

		}else if(action.equalsIgnoreCase(ListViewFunctions.SELECTINDEX_KEYWORD)
				||action.equalsIgnoreCase(ListViewFunctions.SELECTINDEXITEM_KEYWORD) ){
			int index = StringUtilities.getIndex(requiredParam);
			listview.selectItem(index-1, true);

		}else if(action.equalsIgnoreCase(ListViewFunctions.ACTIVATEUNVERIFIEDTEXTITEM_KEYWORD)){
			criterion = new TextMatchingCriterion(requiredParam, false, matchIndex);
			listview.activateItem(criterion, false, null, null);

		}else if(action.equalsIgnoreCase(ListViewFunctions.SELECTUNVERIFIEDTEXTITEM_KEYWORD) ){
			criterion = new TextMatchingCriterion(requiredParam, false, matchIndex);
			listview.selectItem(criterion, false);

		}else if(action.equalsIgnoreCase(ListViewFunctions.VERIFYITEMUNSELECTED_KEYWORD)){
			criterion = new TextMatchingCriterion(requiredParam, false, TextMatchingCriterion.INDEX_TRY_ALL_MATCHED_ITEMS);
			listview.verifyItemSelection(criterion, false);

		}else if(action.equalsIgnoreCase(ListViewFunctions.VERIFYLISTCONTAINS_KEYWORD)){
			criterion = new TextMatchingCriterion(requiredParam, false, matchIndex);
			listview.verifyContains(criterion);

		}else if(action.equalsIgnoreCase(ListViewFunctions.VERIFYSELECTEDITEM_KEYWORD)){
			criterion = new TextMatchingCriterion(requiredParam, false, TextMatchingCriterion.INDEX_TRY_ALL_MATCHED_ITEMS);
			listview.verifyItemSelection(criterion, true);

		}else if(action.equalsIgnoreCase(ListViewFunctions.CAPTUREITEMSTOFILE_KEYWORD)){
			Item[] items = listview.getContent();
			try {
				String filename = deduceTestFile(requiredParam).getAbsolutePath();
				IndependantLog.info(debugmsg+" filename='"+filename+"'; encoding='"+encoding+"'.");
				StringUtils.writeEncodingfile(filename, convertElementArrayToList(items), encoding);
			} catch (IOException e) {
				IndependantLog.error(debugmsg,e);
				throw new SAFSException("Fail to write file due to '"+e.getMessage()+"'");
			}

		}else if(action.equalsIgnoreCase(ListViewFunctions.EXTENDSELECTIONTOTEXTITEM_KEYWORD)){
			criterion = new TextMatchingCriterion(requiredParam, true, matchIndex);
			listview.selectItem(criterion, true, Keys.SHIFT, null, WDLibrary.MOUSE_BUTTON_LEFT);

		}else if(action.equalsIgnoreCase(ListViewFunctions.SELECTANOTHERPARTIALMATCH_KEYWORD)){
			criterion = new TextMatchingCriterion(requiredParam, true, matchIndex);
			listview.selectItem(criterion, true, Keys.CONTROL, null, WDLibrary.MOUSE_BUTTON_LEFT);

		}else if(action.equalsIgnoreCase(ListViewFunctions.SELECTANOTHERTEXTITEM_KEYWORD)){
			criterion = new TextMatchingCriterion(requiredParam, false, matchIndex);
			listview.selectItem(criterion, true, Keys.CONTROL, null, WDLibrary.MOUSE_BUTTON_LEFT);

		}else if(action.equalsIgnoreCase(ListViewFunctions.SETLISTCONTAINS_KEYWORD)){
			try{
				criterion = new TextMatchingCriterion(requiredParam, false, 0);
				listview.verifyContains(criterion);
				setVariable(variable, Boolean.toString(true));
			}catch(SeleniumPlusException se){
				if(!SeleniumPlusException.CODE_VERIFICATION_FAIL.equals(se.getCode())) throw se;
				setVariable(variable, Boolean.toString(false));
			}

		}else if(action.equalsIgnoreCase(ListViewFunctions.RIGHTCLICKTEXTITEM_KEYWORD)){
			//TODO How to verify the right-click???
			criterion = new TextMatchingCriterion(requiredParam, false, matchIndex);
			listview.selectItem(criterion, false, null, null, WDLibrary.MOUSE_BUTTON_RIGHT);

		}else if(action.equalsIgnoreCase(ListViewFunctions.RIGHTCLICKUNVERIFIEDTEXTITEM_KEYWORD)){
			criterion = new TextMatchingCriterion(requiredParam, false, matchIndex);
			listview.selectItem(criterion, false, null, null, WDLibrary.MOUSE_BUTTON_RIGHT);

		}
		else{
			testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
			IndependantLog.warn(debugmsg+action+" could not be handled here.");
			return false;
		}

		return true;
	}

	/**
	 * process keywords who need at least two parameters; but it may be supplied with optional parameters.
	 * @return boolean true if the keyword has been handled successfully;<br>
	 *                 false if the keyword should not be handled in this method.<br>
	 * @throws SeleniumPlusException if there are any problem during handling keyword.
	 */
	private boolean processWithTwoRequiredParameters() throws SAFSException{
		String debugmsg = StringUtils.debugmsg(getClass(), "processWithTwoRequiredParameters");
		String requiredParam = (String) iterator.next();
		Point offset = checkForCoord((String) iterator.next());
		TextMatchingCriterion criterion = null;

		//Handle optionl parameters
		int matchIndex = 1;
		if(action.equalsIgnoreCase(ListViewFunctions.ACTIVATETEXTITEMCOORDS_KEYWORD)
		   ||action.equalsIgnoreCase(ListViewFunctions.ACTIVATEUNVERIFIEDTEXTITEMCOORDS_KEYWORD)
		   ||action.equalsIgnoreCase(ListViewFunctions.RIGHTCLICKTEXTITEMCOORDS_KEYWORD)
		   ||action.equalsIgnoreCase(ListViewFunctions.RIGHTCLICKUNVERIFIEDTEXTITEMCOORDS_KEYWORD)
		   ||action.equalsIgnoreCase(ListViewFunctions.SELECTTEXTITEMCOORDS_KEYWORD)
		   ||action.equalsIgnoreCase(ListViewFunctions.SELECTUNVERIFIEDTEXTITEMCOORDS_KEYWORD)
		   ){

			//'matchIndex' optional parameter
			if(iterator.hasNext()) matchIndex = StringUtilities.getIndex((String)iterator.next());
		}
		matchIndex--;//convert 1-based index to 0-based index

		if(action.equalsIgnoreCase(ListViewFunctions.ACTIVATETEXTITEMCOORDS_KEYWORD)){
			criterion = new TextMatchingCriterion(requiredParam, false, matchIndex);
			listview.activateItem(criterion, true, null, offset);

		}else if(action.equalsIgnoreCase(ListViewFunctions.ACTIVATEUNVERIFIEDTEXTITEMCOORDS_KEYWORD)){
			criterion = new TextMatchingCriterion(requiredParam, false, matchIndex);
			listview.activateItem(criterion, false, null, offset);

		}else if(action.equalsIgnoreCase(ListViewFunctions.RIGHTCLICKTEXTITEMCOORDS_KEYWORD)){
			//TODO How to verify the right-click???
			criterion = new TextMatchingCriterion(requiredParam, false, matchIndex);
			listview.selectItem(criterion, false, null, offset, WDLibrary.MOUSE_BUTTON_RIGHT);

		}else if(action.equalsIgnoreCase(ListViewFunctions.RIGHTCLICKUNVERIFIEDTEXTITEMCOORDS_KEYWORD)){
			criterion = new TextMatchingCriterion(requiredParam, false, matchIndex);
			listview.selectItem(criterion, false, null, offset, WDLibrary.MOUSE_BUTTON_RIGHT);

		}else if(action.equalsIgnoreCase(ListViewFunctions.SELECTTEXTITEMCOORDS_KEYWORD)){
			criterion = new TextMatchingCriterion(requiredParam, false, matchIndex);
			listview.selectItem(criterion, true, null, offset, WDLibrary.MOUSE_BUTTON_LEFT);

		}else if(action.equalsIgnoreCase(ListViewFunctions.SELECTUNVERIFIEDTEXTITEMCOORDS_KEYWORD)){
			criterion = new TextMatchingCriterion(requiredParam, false, matchIndex);
			listview.selectItem(criterion, false, null, offset, WDLibrary.MOUSE_BUTTON_LEFT);

		}else if(action.equalsIgnoreCase(ListViewFunctions.SELECTINDEXITEMCOORDS_KEYWORD)){
			int index = StringUtilities.getIndex(requiredParam);
			listview.selectItem(index, true, null, offset, WDLibrary.MOUSE_BUTTON_LEFT);

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
			//According to the actions, we will wait in different ways
			if(action.equalsIgnoreCase(ListViewFunctions.VERIFYITEMUNSELECTED_KEYWORD)
					||action.equalsIgnoreCase(ListViewFunctions.VERIFYLISTCONTAINS_KEYWORD)
					||action.equalsIgnoreCase(ListViewFunctions.VERIFYSELECTEDITEM_KEYWORD)
					||action.equalsIgnoreCase(ListViewFunctions.CAPTUREITEMSTOFILE_KEYWORD)
					){

				//Wait the component to be click-able
				readyElement = waiter.until(ExpectedConditions.visibilityOf(element));

			}
			//WE don't need to wait element to be click-able here: ListView's click actions will be handled by AbstractSelectable.clickElement(), which
			//will click at web-element returned by Element.getClickableWebElement() in which we wait the web-element to be click-able.
//			else if(action.equalsIgnoreCase(ListViewFunctions.ACTIVATEINDEX_KEYWORD)
//					||action.equalsIgnoreCase(ListViewFunctions.ACTIVATEINDEXITEM_KEYWORD)
//					||action.equalsIgnoreCase(ListViewFunctions.ACTIVATEPARTIALMATCH_KEYWORD)
//					||action.equalsIgnoreCase(ListViewFunctions.ACTIVATETEXTITEM_KEYWORD)
//					||action.equalsIgnoreCase(ListViewFunctions.ACTIVATEUNVERIFIEDTEXTITEM_KEYWORD)
//					||action.equalsIgnoreCase(ListViewFunctions.CLICKINDEX_KEYWORD)
//					||action.equalsIgnoreCase(ListViewFunctions.CLICKINDEXITEM_KEYWORD)
//					||action.equalsIgnoreCase(ListViewFunctions.SELECTINDEX_KEYWORD)
//					||action.equalsIgnoreCase(ListViewFunctions.SELECTINDEXITEM_KEYWORD)
//					||action.equalsIgnoreCase(ListViewFunctions.SELECTPARTIALMATCH_KEYWORD)
//					||action.equalsIgnoreCase(ListViewFunctions.SELECTTEXTITEM_KEYWORD)
//					||action.equalsIgnoreCase(ListViewFunctions.SELECTUNVERIFIEDTEXTITEM_KEYWORD)
//					||action.equalsIgnoreCase(ListViewFunctions.EXTENDSELECTIONTOTEXTITEM_KEYWORD)
//					||action.equalsIgnoreCase(ListViewFunctions.SELECTANOTHERPARTIALMATCH_KEYWORD)
//					||action.equalsIgnoreCase(ListViewFunctions.SELECTANOTHERTEXTITEM_KEYWORD)
//					||action.equalsIgnoreCase(ListViewFunctions.SETLISTCONTAINS_KEYWORD)
//					||action.equalsIgnoreCase(ListViewFunctions.RIGHTCLICKTEXTITEM_KEYWORD)
//					||action.equalsIgnoreCase(ListViewFunctions.RIGHTCLICKUNVERIFIEDTEXTITEM_KEYWORD)
//					||action.equalsIgnoreCase(ListViewFunctions.ACTIVATETEXTITEMCOORDS_KEYWORD)
//					|| action.equalsIgnoreCase(ListViewFunctions.ACTIVATEUNVERIFIEDTEXTITEMCOORDS_KEYWORD)
//					|| action.equalsIgnoreCase(ListViewFunctions.RIGHTCLICKTEXTITEMCOORDS_KEYWORD)
//					|| action.equalsIgnoreCase(ListViewFunctions.RIGHTCLICKUNVERIFIEDTEXTITEMCOORDS_KEYWORD)
//					|| action.equalsIgnoreCase(ListViewFunctions.SELECTINDEXITEMCOORDS_KEYWORD)
//					|| action.equalsIgnoreCase(ListViewFunctions.SELECTTEXTITEMCOORDS_KEYWORD)
//					|| action.equalsIgnoreCase(ListViewFunctions.SELECTUNVERIFIEDTEXTITEMCOORDS_KEYWORD)
//					){
//
//				//Wait the component to be click-able
//				readyElement = waiter.until(ExpectedConditions.elementToBeClickable(element));
//
//			}
			else{
				IndependantLog.warn(action+" could not be handled here.");
			}
		}

		return readyElement;
	}
}
