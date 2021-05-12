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
 * Logs for developers, not published to API DOC.
 *
 * History:
 * @date 2019-11-12    (Lei Wang) Initial created.
 */
package org.safs.cukes.ai.selenium;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.SAFSParamException;
import org.safs.StringUtils;
import org.safs.TestRecordData;
import org.safs.model.commands.ListViewFunctions;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.model.Item;
import org.safs.selenium.webdriver.lib.model.TextMatchingCriterion;
import org.safs.tools.stringutils.StringUtilities;

import cucumber.api.java.en.Then;

/**
 * Used to hold listview test step definitions for gherkin feature files.
 * ListViews are generally found by matching the {@link Criteria}.
 */
public class AIListViewActions extends AIComponent {
	/** "List" */
	public static final String TYPE = "List";

	protected org.safs.selenium.webdriver.lib.ListView listview = null;

	@Override
	protected void initComponent(WebElement we) throws SeleniumPlusException{
		libComponent = new org.safs.selenium.webdriver.lib.ListView(we);
	}

	@Override
	protected String getType(){
		return TYPE;
	}

	@Override
	protected void localProcess(String action, List<String> parameters) throws SAFSException {
		try{
			listview = (org.safs.selenium.webdriver.lib.ListView) libComponent;
		}catch(Exception e){
			throw new SAFSException("Failed to converted "+libComponent.getClass().getSimpleName()+" to "+getType());
		}

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

			if(parameters==null || parameters.size()<1){
				throw new SAFSParamException(action+" needs at the least one parameter!");
			}

			processWithOneRequiredParameter(action, parameters);

		}else if(action.equalsIgnoreCase(ListViewFunctions.ACTIVATETEXTITEMCOORDS_KEYWORD)
				|| action.equalsIgnoreCase(ListViewFunctions.ACTIVATEUNVERIFIEDTEXTITEMCOORDS_KEYWORD)
				|| action.equalsIgnoreCase(ListViewFunctions.RIGHTCLICKTEXTITEMCOORDS_KEYWORD)
				|| action.equalsIgnoreCase(ListViewFunctions.RIGHTCLICKUNVERIFIEDTEXTITEMCOORDS_KEYWORD)
				|| action.equalsIgnoreCase(ListViewFunctions.SELECTINDEXITEMCOORDS_KEYWORD)
				|| action.equalsIgnoreCase(ListViewFunctions.SELECTTEXTITEMCOORDS_KEYWORD)
				|| action.equalsIgnoreCase(ListViewFunctions.SELECTUNVERIFIEDTEXTITEMCOORDS_KEYWORD)
				){
			if(parameters==null || parameters.size()<2){
				throw new SAFSParamException(action+" needs at the least 2 parameters!");
			}

			processWithTwoRequiredParameters(action, parameters);

		}else{
			throw new org.safs.SAFSNotImplementedException("Unknown action '"+action+"' for "+getType()+".");
		}
	}

	/**
	 * process keywords who need at least one parameter; but it may be supplied with optional parameters.
	 * @return boolean true if the keyword has been handled successfully;<br>
	 *                 false if the keyword should not be handled in this method.<br>
	 * @throws SeleniumPlusException if there are any problem during handling keyword.
	 */
	private void processWithOneRequiredParameter(String action, List<String> parameters) throws SAFSException{
		String debugmsg = StringUtils.debugmsg(getClass(), "processWithOneRequiredParameter");
		String requiredParam = parameters.get(0);
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
			if(parameters.size()>1) {
				IndependantLog.info(debugmsg+" command '"+action+"' retrieving optional matchIndex...");
				matchIndex = StringUtilities.getIndex(parameters.get(1));
				IndependantLog.info(debugmsg+" command '"+action+"' matchIndex: "+ matchIndex);
			}
		}
		else if(action.equalsIgnoreCase(ListViewFunctions.CAPTUREITEMSTOFILE_KEYWORD)){
			//'encoding' optional parameter
			if(parameters.size()>1) {
				IndependantLog.info(debugmsg+" command '"+action+"' retrieving optional encoding...");
				encoding = parameters.get(1);
				IndependantLog.info(debugmsg+" command '"+action+"' encoding: "+ encoding);
			}

		}
		else if(action.equalsIgnoreCase(ListViewFunctions.SETLISTCONTAINS_KEYWORD)){
			//'variable' optional parameter
			if(parameters.size()>1) {
				IndependantLog.info(debugmsg+" command '"+action+"' retrieving optional variableName...");
				variable = parameters.get(1);
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
				_setVariable(variable, Boolean.toString(true));
			}catch(SeleniumPlusException se){
				if(!SeleniumPlusException.CODE_VERIFICATION_FAIL.equals(se.getCode())) throw se;
				_setVariable(variable, Boolean.toString(false));
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
			IndependantLog.warn(debugmsg+action+" could not be handled here.");
		}

	}

	/**
	 * process keywords who need at least two parameters; but it may be supplied with optional parameters.
	 * @return boolean true if the keyword has been handled successfully;<br>
	 *                 false if the keyword should not be handled in this method.<br>
	 * @throws SeleniumPlusException if there are any problem during handling keyword.
	 */
	private void processWithTwoRequiredParameters(String action, List<String> parameters) throws SAFSException{
		String debugmsg = StringUtils.debugmsg(getClass(), "processWithTwoRequiredParameters");
		String requiredParam = parameters.get(0);
		Point offset = checkForCoord(parameters.get(1));
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
			if(parameters.size()>2) matchIndex = StringUtilities.getIndex(parameters.get(2));
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
			IndependantLog.warn(debugmsg+action+" could not be handled here.");
		}

	}

	//============================= "cucumber step definitions are defined" as below  ===============================
	/**
	 * Handle the keyword
	 * <ol>
	 * <li><b>ActivateIndex</b>
	 * <li><b>ActivateIndexItem</b>
	 * <li><b>ClickIndex</b>
	 * <li><b>ClickIndexItem</b>
	 * <li><b>SelectIndex</b>
	 * <li><b>SelectIndexItem</b>
	 * <li><b>SelectIndexItemCoords</b>
	 * </ol>
	 *  of <a href="/sqabasic2000/ListViewFunctionsIndex.htm">ListViewFunctions</a> against the ListView matching the provided textlabel.<br>
	 * <p>
	 * Cucumber Expression: "{listview_action} {var_or_string} item in the {var_mapitem_or_string} list using {list}"<br>
	 * {listview_action} Matches {@link TypeRegistryConfiguration#REGEX_LISTVIEW_ACTION}<br>
	 * {var_or_string} Matches {@link TypeRegistryConfiguration#REGEX_VAR_OR_STRING}, represents a variable name (with an optional leading symbol ^) or a double-quoted-string or a single-quoted-string.<br>
	 * {var_mapitem_or_string} Matches {@link TypeRegistryConfiguration#REGEX_VAR_MAPITEM_OR_STRING},
	 *                     represents a variable, or a map item, such as mapID:section.item or a double-quoted-string or a single-quoted-string.
	 *                     It will be parsed by {@link TypeRegistryConfiguration}<br>
	 * {list} Matches {@link TypeRegistryConfiguration#REGEX_LIST}, represents a list of string parameters, the parameters can be delimited by {@link TestRecordData#POSSIBLE_SEPARATOR}.<br>
	 * <p>
	 * Example Scenario Step:
	 * <p>
	 * <ul>
	 * <li><b>Then ActivateIndex "2" item in the "States List Box" list using ""</b>
	 * <li><b>Then SelectIndexItemCoords 4 item in the "States List Box" list using "5,5"</b>
	 * </ul>
	 *
	 * @param action String, the action
	 * @param index int, the 1-based index of the item to select
	 * @param criteria String, the search-conditions helping to find the List<br>
	 * @return
	 * @throws SAFSException if target object cannot be found.
	 * @see AIComponent#process(String, String, int, List)
	 */
	@Then("{listview_action} {var_or_string} item in the {var_mapitem_or_string} list")
	public void select_index_in_the_list(String action, String index, String criteria) throws SAFSException {
		select_index_in_the_nth_matched_list(action, index, 1, criteria);
	}
	@Then("{listview_action} {var_or_string} item in the {int} {var_mapitem_or_string} list")
	private void select_index_in_the_nth_matched_list(String action, String index, int nthMatched, String criteria) throws SAFSException {
		List<String> allParameters = new ArrayList<String>();
		allParameters.add(index);
		process(action, criteria, nthMatched, allParameters);
	}
//	@Then("{listview_action} {var_or_string} item in the {var_mapitem_or_string} list using {list}")
//	public void select_index_in_the_list(String action, String index, String criteria, List<String> parameters) throws SAFSException {
//		select_index_in_the_nth_matched_list(action, index, 1, criteria, parameters);
//	}
//	@Then("{listview_action} {var_or_string} item in the {int} {var_mapitem_or_string} list using {list}")
//	private void select_index_in_the_nth_matched_list(String action, String index, int nthMatched, String criteria, List<String> parameters) throws SAFSException {
//		List<String> allParameters = new ArrayList<String>();
//		allParameters.add(index);
//		if(parameters!=null && !parameters.isEmpty()){
//			allParameters.addAll(parameters);
//		}
//		process(action, criteria, nthMatched, allParameters);
//	}


}
