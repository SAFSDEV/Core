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
 * @date 2019-07-09    (Lei Wang) Handle the ComboBox keywords.
 * @date 2019-07-10    (Lei Wang) Handled the cached component.
 *                                Added the ability to operate the nth matched component.
 * @date 2019-07-11    (Lei Wang) Refacor code.
 * @date 2019-07-31    (Lei Wang) Modified code: accept 'recognition string' defined in the app map.
 */
package org.safs.cukes.ai.selenium;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.StringUtils;
import org.safs.TestRecordData;
import org.safs.model.commands.ComboBoxFunctions;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.tools.CaseInsensitiveFile;

import cucumber.api.java.en.Then;

/**
 * Used to hold combobox test step definitions for gherkin feature files.
 * ComboBoxes are generally found by matching the {@link Criteria}.
 */
public class AIComboBoxActions extends AIComponent {
	/** "ComboBox" */
	public static final String TYPE = "ComboBox";

	protected org.safs.selenium.webdriver.lib.ComboBox combobox = null;

	@Override
	protected void initComponent(WebElement we) throws SeleniumPlusException{
		libComponent = new org.safs.selenium.webdriver.lib.ComboBox(we);
	}

	@Override
	protected String getType(){
		return TYPE;
	}

	/**
	 * Handle the <a href="/sqabasic2000/ComboBoxFunctionsIndex.htm">ComboBoxFunctions</a> against the ComboBox matching the provided textlabel.<br>
	 *
	 * @param action String, the action to perform
	 * @param parameters List<String>, the action's parameters
	 * @return
	 * @throws SAFSException if target object cannot be found, or the action is not supported, or any exception has been met.
	 */
	@Override
	protected void localProcess(String action, List<String> parameters) throws SAFSException {
		try{
			combobox = (org.safs.selenium.webdriver.lib.ComboBox) libComponent;
		}catch(Exception e){
			throw new SAFSException("Failed to converted "+libComponent.getClass().getSimpleName()+" to "+getType());
		}

		if(ComboBoxFunctions.HIDELIST_KEYWORD.equalsIgnoreCase(action)){
			combobox.hidePopup();
		}else if(ComboBoxFunctions.SHOWLIST_KEYWORD.equalsIgnoreCase(action)){
			combobox.showPopup();
		}else if(ComboBoxFunctions.SELECT_KEYWORD.equalsIgnoreCase(action)){
			combobox.select(parameters.get(0), true, false, true);

		}else if(ComboBoxFunctions.SELECTINDEX_KEYWORD.equalsIgnoreCase(action)){
			int offset = 0;
			offset = Integer.parseInt(parameters.get(0));
			if (offset < 1){
				throw new NumberFormatException("IndexValue '"+parameters.get(0)+"' is not greater than 0.");
			}
			// test tables indices are 1-based but Selenium is 0-based
			offset--;
			combobox.selectIndex(offset, true, true);

		}else if(ComboBoxFunctions.SELECTPARTIALMATCH_KEYWORD.equalsIgnoreCase(action)){
			combobox.select(parameters.get(0), true, true, true);

		}else if(ComboBoxFunctions.SELECTUNVERIFIED_KEYWORD.equalsIgnoreCase(action)){
			combobox.select(parameters.get(0), false, false, true);

		}else if(ComboBoxFunctions.SELECTUNVERIFIEDPARTIALMATCH_KEYWORD.equalsIgnoreCase(action)){
			combobox.select(parameters.get(0), false, true, true);

		}else if(ComboBoxFunctions.VERIFYSELECTED_KEYWORD.equalsIgnoreCase(action)){
			combobox.verifySelected(parameters.get(0));

		}else if(ComboBoxFunctions.CAPTUREITEMSTOFILE_KEYWORD.equalsIgnoreCase(action)){
			String encoding = null;
			try{ encoding = parameters.get(1); }catch(Exception e){}
			captureItemsToFile(parameters.get(0), encoding);

		}else if(ComboBoxFunctions.SETTEXTVALUE_KEYWORD.equalsIgnoreCase(action)){
			doSetText(TYPE, parameters.get(0), false, true);

		}else if(ComboBoxFunctions.SETUNVERIFIEDTEXTVALUE_KEYWORD.equalsIgnoreCase(action)){
			doSetText(TYPE, parameters.get(0), false, false);

		}else{
			throw new org.safs.SAFSNotImplementedException("Unknown action '"+action+"' for "+getType()+".");
		}
	}

	private void captureItemsToFile (String filename, String encoding) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(getClass(), "captureItemsToFile");

		try {
			Log.info(debugmsg+"...filename: "+filename+" ; encoding:"+encoding);

			List<String> list = combobox.getDataList();
			Log.info(debugmsg+"list: "+list);

			File file = new CaseInsensitiveFile(filename).toFile();
			if (!file.isAbsolute()) {
				String testdir = GetVariableValue(STAFHelper.SAFS_VAR_TESTDIRECTORY);
				if (testdir != null) {
					file = new CaseInsensitiveFile(testdir, filename).toFile();
					filename = file.getAbsolutePath();
				}
			}
			Log.info(debugmsg+"Writing to file: "+filename);
			StringUtils.writeEncodingfile(filename, list, encoding);

		}catch (Exception se) {
			throw new SeleniumPlusException(se.getMessage());
		}
	}

	//============================= "cucumber step definitions are defined" as below  ===============================
	/**
	 * Handle the keyword <b>HideList</b> and <b>ShowList</b> of <a href="/sqabasic2000/ComboBoxFunctionsIndex.htm">ComboBoxFunctions</a> against the ComboBox matching the provided textlabel.<br>
	 * <p>
	 * Cucumber Expression: "{combobox_action} the {mapitem_or_string} combobox"<br>
	 * {combobox_action} Matches {@link TypeRegistryConfiguration#REGEX_COMBOBOX_ACTION}<br>
	 * {var_mapitem_or_string} Matches {@link TypeRegistryConfiguration#REGEX_VAR_MAPITEM_OR_STRING},
	 *                     represents a variable, or a map item, such as mapID:section.item or a double-quoted-string or a single-quoted-string.
	 *                     It will be parsed by {@link TypeRegistryConfiguration}<br>
	 * <p>
	 * Example Scenario Step:
	 * <p>
	 * <ul>
	 * <li><b>Then ShowList the "States Combo Box" combobox</b>
	 * <li><b>Then HideList the "States Combo Box" combobox</b>
	 * <li>show the drop-down list of the combobox identified by the value of map item SapDemoApp:SAPDemoPageAI.ComboBox<br>
	 *     == SapDemoApp.map file =========<br>
	 *     [SAPDemoPageAI]<br>
	 *     ComboBox="States Combo Box"<br>
	 *     ================================<br>
	 *     <b>Then ShowList the SapDemoApp:SAPDemoPageAI.ComboBox combobox</b>
	 * </ul>
	 *
	 * @param action String, the action <b>HideList</b> and <b>ShowList</b>
	 * @param criteria String, the search-conditions helping to find the ComboBox<br>
	 * @return
	 * @throws SAFSException if target object cannot be found.
	 * @see AIComponent#process(String, String, int, List)
	 */
	@Then("{combobox_action} the {var_mapitem_or_string} combobox")
	public void showHide_the_combobox(String action, String criteria) throws SAFSException {
		List<String> parameters = new ArrayList<String>();
		process(action, criteria, 1, parameters);
	}
	@Then("{combobox_action} the {int} {var_mapitem_or_string} combobox")
	private void showHide_the_nth_matched_combobox(String action, int nthMatched, String criteria) throws SAFSException {
		List<String> parameters = new ArrayList<String>();
		process(action, criteria, nthMatched, parameters);
	}

	/**
	 * Handle the keyword <b>SetTextValue</b> and <b>SetUnverifiedTextValue</b> of <a href="/sqabasic2000/ComboBoxFunctionsIndex.htm">ComboBoxFunctions</a> against the ComboBox matching the provided textlabel.<br>
	 * <p>
	 * Cucumber Expression: "{combobox_action} {var_or_string} in the {mapitem_or_string} combobox"<br>
	 * {combobox_action} Matches {@link TypeRegistryConfiguration#REGEX_COMBOBOX_ACTION}<br>
	 * {var_or_string} Matches {@link TypeRegistryConfiguration#REGEX_VAR_OR_STRING}, represents a variable name (with an optional leading symbol ^) or a double-quoted-string or a single-quoted-string.<br>
	 * {var_mapitem_or_string} Matches {@link TypeRegistryConfiguration#REGEX_VAR_MAPITEM_OR_STRING},
	 *                     represents a variable, or a map item, such as mapID:section.item or a double-quoted-string or a single-quoted-string.
	 *                     It will be parsed by {@link TypeRegistryConfiguration}<br>
	 * <p>
	 * Example Scenario Step:
	 * <p>
	 * <ul>
	 * <li><b>Then SetTextValue "Some Text" in the "States Combo Box" combobox</b>
	 * <li><b>Then SetUnverifiedTextValue "Some Text with special keys +(abcd)" in the "States Combo Box" combobox</b>
	 * <li>type string "Some Text" into the combobox identified by the value of map item SAPDemoPageAI.ComboBox<br>
	 *     == on map chain, a map containing =========<br>
	 *     [SAPDemoPageAI]<br>
	 *     ComboBox="States Combo Box"<br>
	 *     ================================<br>
	 *     <b>Then SetTextValue "Some Text" in the "States Combo Box" combobox</b>
	 * <li>type keys "Some Text with special keys +(abcd)" into the combobox identified by the value of map item SapDemoApp:SAPDemoPageAI.ComboBox<br>
	 *     == SapDemoApp.map file =========<br>
	 *     [SAPDemoPageAI]<br>
	 *     ComboBox="States Combo Box"<br>
	 *     ================================<br>
	 *     <b>Then SetUnverifiedTextValue "Some Text with special keys +(abcd)" in the SapDemoApp:SAPDemoPageAI.ComboBox combobox</b>
	 * </ul>
	 *
	 * @param action String, the action <b>SetTextValue</b> and <b>SetUnverifiedTextValue</b>
	 * @param value String, the value to set into the ComboBox
	 * @param criteria String, the search-conditions helping to find the ComboBox<br>
	 * @return
	 * @throws SAFSException if target object cannot be found.
	 * @see AIComponent#process(String, String, int, List)
	 */
	@Then("{combobox_action} {var_or_string} in the {var_mapitem_or_string} combobox")
	public void settextvalue_in_the_combobox(String action, String value, String criteria) throws SAFSException {
		List<String> parameters = new ArrayList<String>();
		parameters.add(value);
		process(action, criteria, 1, parameters);
	}
	@Then("{combobox_action} {var_or_string} in the {int} {var_mapitem_or_string} combobox")
	private void settextvalue_in_the_nth_matched_combobox(String action, String value, int nthMatched, String criteria) throws SAFSException {
		List<String> parameters = new ArrayList<String>();
		parameters.add(value);
		process(action, criteria, nthMatched, parameters);
	}

	/**
	 * Handle the keyword <b>Select</b>, <b>SelectIndex</b>, <b>SelectPartialMatch</b>, <b>SelectUnverified</b>, <b>SelectUnverifiedPartialMatch</b>, and <b>VerifySelected</b> of <a href="/sqabasic2000/ComboBoxFunctionsIndex.htm">ComboBoxFunctions</a> against the ComboBox matching the provided textlabel.<br>
	 * <p>
	 * Cucumber Expression: "{combobox_action} {var_or_string} item in the {mapitem_or_string} combobox"<br>
	 * {combobox_action} Matches {@link TypeRegistryConfiguration#REGEX_COMBOBOX_ACTION}<br>
	 * {var_or_string} Matches {@link TypeRegistryConfiguration#REGEX_VAR_OR_STRING}, represents a variable name (with an optional leading symbol ^) or a double-quoted-string or a single-quoted-string.<br>
	 * {var_mapitem_or_string} Matches {@link TypeRegistryConfiguration#REGEX_VAR_MAPITEM_OR_STRING},
	 *                     represents a variable, or a map item, such as mapID:section.item or a double-quoted-string or a single-quoted-string.
	 *                     It will be parsed by {@link TypeRegistryConfiguration}<br>
	 * <p>
	 * Example Scenario Step:
	 * <p>
	 * <ul>
	 * <li><b>Then Select "Alaska" item in the "States Combo Box" combobox</b>
	 * <li><b>Then SelectPartialMatch "kansa" item in the "States Combo Box" combobox</b>
	 * <li><b>Then SelectUnverified "Alabama" item in the "States Combo Box" combobox</b>
	 * <li><b>Then VerifySelected "Alabama" item in the "States Combo Box" combobox</b>
	 * <li><b>Then SelectUnverifiedPartialMatch "abama" item in the "States Combo Box" combobox</b>
	 * <li><b>Then SelectIndex "1" item in the "States Combo Box" combobox</b>
	 * <li>select "Alaska" item of the combobox identified by the value of map item SAPDemoPageAI.ComboBox<br>
	 *     == on map chain, a map containing =========<br>
	 *     [SAPDemoPageAI]<br>
	 *     ComboBox="States Combo Box"<br>
	 *     ================================<br>
	 *     <b>Then Select "Alaska" item in the SAPDemoPageAI.ComboBox combobox</b>
	 * </ul>
	 *
	 * @param action String, the action <b>Select</b>, <b>SelectIndex</b>, <b>SelectPartialMatch</b>, <b>SelectUnverified</b>, <b>SelectUnverifiedPartialMatch</b>, and <b>VerifySelected</b>
	 * @param item String, the item to select/verify in the ComboBox
	 * @param criteria String, the search-conditions helping to find the ComboBox<br>
	 * @return
	 * @throws SAFSException if target object cannot be found.
	 * @see AIComponent#process(String, String, int, List)
	 */
	@Then("{combobox_action} {var_or_string} item in the {var_mapitem_or_string} combobox")
	public void handle_item_in_the_combobox(String action, String item, String criteria) throws SAFSException {
		handle_item_in_the_nth_matched_combobox(action, item, 1, criteria);
	}
	@Then("{combobox_action} {var_or_string} item in the {int} {var_mapitem_or_string} combobox")
	private void handle_item_in_the_nth_matched_combobox(String action, String item, int nthMatched, String criteria) throws SAFSException {
		List<String> parameters = new ArrayList<String>();
		parameters.add(item);
		process(action, criteria, nthMatched, parameters);
	}

	/**
	 * Handle the keyword <b>CaptureItemsToFile</b> of <a href="/sqabasic2000/ComboBoxFunctionsIndex.htm">ComboBoxFunctions</a> against the ComboBox matching the provided textlabel.<br>
	 * <p>
	 * Cucumber Expression: "{combobox_action} the {mapitem_or_string} combobox using {list}"<br>
	 * {combobox_action} Matches {@link TypeRegistryConfiguration#REGEX_COMBOBOX_ACTION}<br>
	 * {var_mapitem_or_string} Matches {@link TypeRegistryConfiguration#REGEX_VAR_MAPITEM_OR_STRING},
	 *                     represents a variable, or a map item, such as mapID:section.item or a double-quoted-string or a single-quoted-string.
	 *                     It will be parsed by {@link TypeRegistryConfiguration}<br>
	 * {list} Matches {@link TypeRegistryConfiguration#REGEX_LIST}, represents a list of string parameters, the parameters can be delimited by {@link TestRecordData#POSSIBLE_SEPARATOR}.<br>
	 * <p>
	 * Example Scenario Step:
	 * <p>
	 * <ul>
	 * <li><b>Then CaptureItemsToFile the "States Combo Box" combobox using "ComboBoxData.txt"</b>
	 * <li><b>Then CaptureItemsToFile the "States Combo Box" combobox using "ComboBoxDataUTF8.txt, UTF-8"</b>
	 * <li>capture the content of combobox identified by the value of map item SAPDemoPageAI.ComboBox<br>
	 *     == on map chain, a map containing =========<br>
	 *     [SAPDemoPageAI]<br>
	 *     ComboBox="States Combo Box"<br>
	 *     ================================<br>
	 *     <b>Then CaptureItemsToFile the SAPDemoPageAI.ComboBox combobox using "ComboBoxData.txt"</b>
	 * </ul>
	 *
	 * @param action String, the action <b>CaptureItemsToFile</b>
	 * @param criteria String, the search-conditions helping to find the ComboBox<br>
	 * @param parameters List<String>, parameters[0] filename, parameters[1] encoding
	 * @return
	 * @throws SAFSException if target object cannot be found.
	 * @see AIComponent#process(String, String, int, List)
	 */
	@Then("{combobox_action} the {var_mapitem_or_string} combobox using {list}")
	public void captureitems_in_the_combobox(String action, String criteria, List<String> parameters) throws SAFSException {
    	process(action, criteria, 1, parameters);
    }

	/**
	 * If there are multiple matched component, we use parameter 'nthMatched' (1-based index) to indicate which one we are going to operate.<br>
	 *
	 * Example Scenario Step:
	 * <p>
	 * <ul>
	 * <li><b>Then CaptureItemsToFile the 1 "States Combo Box" combobox using "ComboBoxData.txt"</b>
	 * <li><b>Then CaptureItemsToFile the 2 "States Combo Box" combobox using "ComboBoxData.txt"</b>
	 * <li>capture the content of the 2nd combobox identified by the value of map item SAPDemoPageAI.ComboBox<br>
	 *     == on map chain, a map containing =========<br>
	 *     [SAPDemoPageAI]<br>
	 *     ComboBox="States Combo Box"<br>
	 *     ================================<br>
	 *     <b>Then CaptureItemsToFile the 2 SAPDemoPageAI.ComboBox combobox using "ComboBoxData.txt"</b>
	 * </ul>
	 *
	 * @param action String, the action <b>CaptureItemsToFile</b>
	 * @param nthMatched int, 1-based index, the Nth matched component if there are multiple components matching the search criteria.
	 * @param criteria String, the search-conditions helping to find the ComboBox.
	 * @param parameters List<String>, parameters[0] filename, parameters[1] encoding
	 * @throws SAFSException
	 * @see AIComponent#process(String, String, int, List)
	 */
	@Then("{combobox_action} the {int} {var_mapitem_or_string} combobox using {list}")
	private void captureitems_in_the_nth_matched_combobox(String action, int nthMatched, String criteria, List<String> parameters) throws SAFSException {
		process(action, criteria, nthMatched, parameters);
	}
}
