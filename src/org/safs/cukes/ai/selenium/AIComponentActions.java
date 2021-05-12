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
 * @date 2019-08-23    (Lei Wang) Initial release.
 * @date 2019-08-27    (Lei Wang) Overloaded method verify_component_is_displayed(): handle one more parameter 'type'.
 */
package org.safs.cukes.ai.selenium;

import java.util.ArrayList;
import java.util.List;

import org.safs.SAFSException;
import org.safs.SAFSObjectNotFoundException;
import org.safs.model.commands.GenericMasterFunctions;
import org.safs.model.commands.GenericObjectFunctions;

import cucumber.api.java.en.Then;

/**
 * @author Lei Wang
 *
 */
public class AIComponentActions extends AIComponent{

	//============================= "cucumber step definitions are defined" as below  ===============================
	/**
	 * Click the component whose text matches the text provided.  Partial matching is supported if enabled.<br>
	 * <p>
	 * Cucumber Expression: "click/tap the {mapitem_or_string} {word}"<br>
	 * {var_mapitem_or_string} Matches {@link TypeRegistryConfiguration#REGEX_VAR_MAPITEM_OR_STRING},
	 *                     represents a variable, or a map item, such as mapID:section.item or a double-quoted-string or a single-quoted-string.
	 *                     It will be parsed by {@link TypeRegistryConfiguration}<br>
	 * {word} Matches words without whitespace, for example banana<br>
	 * They will be parsed by {@link TypeRegistryConfiguration}<br>
	 * <p>
	 * Examples invocations:
	 * <p><ul>
	 * <li><b>Then click the "Help" option</b>
	 * <li><b>And click the "About" item</b>
	 * <br>or<br>
	 * <li><b>Then tap the "Help" option</b>
	 * <li><b>And tap the "About" item</b>
	 * <li>click the tab identified by the text of map item SAPDemoPageAI.BascCompLabel<br>
	 *     == on map chain, a map containing =========<br>
	 *     [SAPDemoPageAI]<br>
	 *     BascCompLabel="Basc.Comp"<br>
	 *     ================================<br>
	 *     <b>Then click the SAPDemoPageAI.BascCompLabel tab</b>
	 * <li>click the tab identified by the recognition string of map item SAPDemoPageAI.BascCompLabel<br>
	 *     == on map chain, a map containing =========<br>
	 *     [SAPDemoPage]<br>
	 *     SAPDemoPage="class=sapUiBody"<br>
	 *     Basc_ToggleButton="id=layout.center.tab.basc.togglebutton"<br>
	 *     ================================<br>
	 *     <b>Then click the SAPDemoPage.Basc_ToggleButton button</b>
	 * </ul>
	 * @param criteria String, the search-conditions helping to find the component to click.
	 * @param type String, allows the user to specify a 'type' (editbox, button, item, option, etc..) to help locate the exact web-element.
	 * @throws SAFSObjectNotFoundException if not found and abort on find failure is enabled.
	 * @see #findSelectableItemFromText(String)
	 * @see AIMiscActions#accept_partial_text_matches()
	 * @see AIMiscActions#deny_partial_text_matches()
	 * @see AIMiscActions#abort_testing_on_item_not_found()
	 * @see AIMiscActions#continue_testing_on_item_not_found()
	 * @see AIComponent#process(String, String, int, List, String)
	 */
	@Then("click/tap the {var_mapitem_or_string} {word}")
	public void click_component(String criteria, String type) throws SAFSException {
		List<String> parameters = new ArrayList<String>();
		process(GenericObjectFunctions.CLICK_KEYWORD, criteria, 1, parameters, type);
	}

	/**
	 * Verify the requested component is displayed anywhere on the page. Partial matching is supPorted if enabled.
	 * <p>
	 * Cucumber Expression: "verify {mapitem_or_string} is displayed"<br>
	 * {var_mapitem_or_string} Matches {@link TypeRegistryConfiguration#REGEX_VAR_MAPITEM_OR_STRING},
	 *                     represents a variable, or a map item, such as mapID:section.item or a double-quoted-string or a single-quoted-string.
	 *                     It will be parsed by {@link TypeRegistryConfiguration}<br>
	 * <p>
	 * Examples invocations:
	 * <p>
	 * <ul>
	 * <li><b>Then verify "Help" is displayed</b>
	 * <li><b>And verify "Something else" is displayed</b>
	 * <li>verify the appearance of the text of map item verifMsg_AllowMultiSelect<br>
	 *     == on map chain, a map containing =========<br>
	 *     [ApplicationConstants]<br>
	 *     verifMsg_AllowMultiSelect="allow multiple selection"<br>
	 *     ================================<br>
	 *     <b>Then verify verifMsg_AllowMultiSelect is displayed</b>
	 * <li>verify the appearance of the component identified by map item SAPDemoPage.Basc_ToggleButton<br>
	 *     == on map chain, a map containing =========<br>
	 *     [SAPDemoPage]<br>
	 *     SAPDemoPage="class=sapUiBody"<br>
	 *     Basc_ToggleButton="id=layout.center.tab.basc.togglebutton"<br>
	 *     ================================<br>
	 *     <b>Then verify SAPDemoPage.Basc_ToggleButton is displayed</b>
	 * </ul>
	 * @param criteria String, the search-conditions helping to find the component to be verified.
	 * @throws SAFSObjectNotFoundException if not found and abort on find failure is enabled.
	 * @see #findSelectableItemFromText(String)
	 * @see AIMiscActions#accept_partial_text_matches()
	 * @see AIMiscActions#deny_partial_text_matches()
	 * @see AIMiscActions#abort_testing_on_item_not_found()
	 * @see AIMiscActions#continue_testing_on_item_not_found()
	 * @see AIComponent#process(String, String, int, List)
	 */
	@Then("verify {var_mapitem_or_string} is displayed/visible")
	public void verify_component_is_displayed(String criteria) throws SAFSException {
		List<String> parameters = new ArrayList<String>();
		process(GenericMasterFunctions.GUIDOESEXIST_KEYWORD, criteria, 1, parameters);
	}

	/**
	 * Verify the requested component is displayed anywhere on the page. Partial matching is supPorted if enabled.
	 * <p>
	 * Cucumber Expression: "verify the {mapitem_or_string} {word} is displayed/visible"<br>
	 * {var_mapitem_or_string} Matches {@link TypeRegistryConfiguration#REGEX_VAR_MAPITEM_OR_STRING},
	 *                     represents a variable, or a map item, such as mapID:section.item or a double-quoted-string or a single-quoted-string.
	 *                     It will be parsed by {@link TypeRegistryConfiguration}<br>
	 * {word} Matches words without whitespace, for example banana (but not banana split)<br>
	 * <p>
	 * Examples invocations:
	 * <p>
	 * <ul>
	 * <li><b>Then verify the "Help" label is displayed</b>
	 * <li><b>Then verify the "Submit" button is displayed</b>
	 * <li><b>And verify the "Cancel" button is visible</b>
	 * <li>verify the existence of the editbox identified by xpath<br>
	 *     <b>Then verify the "XPATH=//*[@id='layout.center.tab.basc.firstname.editbox']" editbox is visible</b>
	 * <li>verify the existence of the label identified by css selector<br>
	 *     <b>Then verify the "CSS=[id='layout.center.tab.basc.firstname.label']" label is visible</b>
	 * <li>verify the appearance of the text of map item verifMsg_AllowMultiSelect<br>
	 *     == on map chain, a map containing =========<br>
	 *     [ApplicationConstants]<br>
	 *     verifMsg_AllowMultiSelect="allow multiple selection"<br>
	 *     ================================<br>
	 *     <b>Then verify the verifMsg_AllowMultiSelect text is displayed</b>
	 * <li>verify the existence of the button identified by the value of map item Basc_ToggleButton<br>
	 *     == on map chain, a map containing =========<br>
	 *     [ApplicationConstants]<br>
	 *     Basc_ToggleButton="ToggleButton"<br>
	 *     ================================<br>
	 *     <b>Then verify the Basc_ToggleButton button is visible</b>
	 * <li>verify the existence of the button identified by the value of map item SAPDemoPage.Basc_ToggleButton<br>
	 *     == on map chain, a map containing =========<br>
	 *     [SAPDemoPage]<br>
	 *     Basc_ToggleButton="id=layout.center.tab.basc.togglebutton"<br>
	 *     ================================<br>
	 *     <b>Then verify the SAPDemoPage.Basc_ToggleButton button is displayed</b>
	 * <li>verify the existence of the button identified by the value of map item SapDemoApp:SAPDemoPageAI.Basc_ToggleButton<br>
	 *     == SapDemoApp.map file =========<br>
	 *     [SAPDemoPageAI]<br>
	 *     Basc_ToggleButton="ToggleButton"<br>
	 *     ================================<br>
	 *     <b>Then verify the SapDemoApp:SAPDemoPageAI.Basc_ToggleButton button is displayed</b>
	 * </ul>
	 * @param criteria String, the search-conditions helping to find the component to be verified.
	 * @param type String, allows the user to specify a 'type' (editbox, button, item, option, etc..) to help locate the exact web-element.
	 * @throws SAFSObjectNotFoundException if not found and abort on find failure is enabled.
	 * @see #findSelectableItemFromText(String)
	 * @see AIMiscActions#accept_partial_text_matches()
	 * @see AIMiscActions#deny_partial_text_matches()
	 * @see AIMiscActions#abort_testing_on_item_not_found()
	 * @see AIMiscActions#continue_testing_on_item_not_found()
	 * @see AIComponent#process(String, String, int, List)
	 */
	@Then("verify the {var_mapitem_or_string} {word} is displayed/visible")
	public void verify_component_is_displayed(String criteria, String type) throws SAFSException {
		List<String> parameters = new ArrayList<String>();
		process(GenericMasterFunctions.GUIDOESEXIST_KEYWORD, criteria, 1, parameters, type);
	}
}
