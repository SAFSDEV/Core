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
 * @date 2019-07-01    (Lei Wang) Modified findTextFieldFromLabel(): find the real editbox element around the label element.
 * @date 2019-07-11    (Lei Wang) Refactor code. Added findTextFieldsFromLabel().
 * @date 2019-07-31    (Lei Wang) Modified code: accept 'recognition string' defined in the app map.
 * @date 2019-08-28    (Lei Wang) Changed some methods from static to non-static: they call non-static method of super class.
 */
package org.safs.cukes.ai.selenium;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.safs.Arbre;
import org.safs.Constants;
import org.safs.Constants.HTMLConst;
import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.SAFSObjectNotFoundException;
import org.safs.SAFSObjectRecognitionException;
import org.safs.StringUtils;
import org.safs.model.commands.EditBoxFunctions;
import org.safs.selenium.webdriver.WebDriverGUIUtilities;
import org.safs.selenium.webdriver.lib.RS;
import org.safs.selenium.webdriver.lib.RS.XPATH;
import org.safs.selenium.webdriver.lib.RemoteDriver;
import org.safs.selenium.webdriver.lib.SearchObject.FrameElement;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;

import cucumber.api.java.en.Then;

/**
 * Used to hold TEXTAREA and TEXT INPUT test step definitions for gherkin feature files.
 * EditBoxes are generally found by matching their @placeholder, @aria-placeholder, or displayed text values;
 * or by matching  the {@link Criteria}.
 * <p>Future support needs to include identifying the textfield by its @aria-labelledby or @for (or equivalent) attributes
 * and finding the text label associated with the text field that contains the matching label text.
 */
public class AIEditBoxActions extends AIComponent {
	/** "EditBox" */
	public static final String TYPE = "EditBox";

	protected org.safs.selenium.webdriver.lib.EditBox editbox = null;

	@Override
	protected void initComponent(WebElement we) throws SeleniumPlusException{
		libComponent = new org.safs.selenium.webdriver.lib.EditBox(we);
	}

	@Override
	protected String getType(){
		return TYPE;
	}

	@Override
	protected List<WebElement> findElements(Criteria criteria){
		List<WebElement> results = super.findElements(criteria);
		List<WebElement> elements = null;
		if(results==null || results.isEmpty()){
			elements = findTextFieldsFromLabel(criteria.getComponentRS());
			if(elements!=null && !elements.isEmpty()) results.addAll(elements);
		}

		return results;
	}

	@Override
	protected void localProcess(String action, List<String> parameters) throws SAFSException {
		try{
			editbox = (org.safs.selenium.webdriver.lib.EditBox) libComponent;
		}catch(Exception e){
			throw new SAFSException("Failed to converted "+libComponent.getClass().getSimpleName()+" to "+getType());
		}

		boolean success = false;
		String contents = parameters.get(0);
		if("type".equalsIgnoreCase(action) || "typechars".equalsIgnoreCase(action) ||
			EditBoxFunctions.SETUNVERIFIEDTEXTCHARACTERS_KEYWORD.equalsIgnoreCase(action)){
			success = doSetText(TYPE, contents, true, false);

		}else if(EditBoxFunctions.SETUNVERIFIEDTEXTVALUE_KEYWORD.equalsIgnoreCase(action) || "typekeys".equalsIgnoreCase(action)){
			success = doSetText(TYPE, contents, false, false);

		}else if(EditBoxFunctions.SETTEXTCHARACTERS_KEYWORD.equalsIgnoreCase(action)){
			success = doSetText(TYPE, contents, true, true);

		}else if(EditBoxFunctions.SETTEXTVALUE_KEYWORD.equalsIgnoreCase(action)){
			success = doSetText(TYPE, contents, false, true);

		}else{
			throw new org.safs.SAFSNotImplementedException("Unknown action '"+action+"' for "+getType()+".");
		}

		if(!success){
			throw new org.safs.SAFSException("EditBox value '"+libComponent.getValue()+"' does not match expected value '"+contents+"'");
		}
	}

	/**
	 * Find all EditBox-type fields that are associated with the provided textlabel in some way.<br>
	 * Possible matches can be a TEXTAREA or INPUT tag with a 'placeholder' attribute, or any type
	 * of tag with an 'aria-placeholder' attribute, with the attribute value matching the provided
	 * textlabel.<br>
	 * We can also match on a field if the provided textlabel matches the contents (value) of the
	 * field.<br>
	 * We can also match on a field if the provided textlabel matches the label-component around the text-field.<br>
	 * This routine does not yet support partial text matching, but it probably should once fully evaluated.<br>
	 * <br>
	 * We will try to search in the current frame if 'bypass frame reset' is set to true, otherwise we search in each frame.<br>
	 * If the editbox is not found, we will attempt to find the editbox with label matched the 'textlabel' parameter.<br>
	 * @param textlabel -- placeholder text or displayed text value or the label (associated with EditBox) to match against.
	 * @return the first editbox-type field matching the textlabel criteria, or null if not found.
	 */
	public List<WebElement> findTextFieldsFromLabel(String textlabel){

		RemoteDriver selenium = (RemoteDriver) WDLibrary.getWebDriver();
		List<WebElement> elements = null;

		if(WDLibrary.getBypassFramesReset()){
			//we just search in the current frame
			elements = _findTextFieldsFromLabel(textlabel);
		}else{
			//Try to find TextField-type elements on each frame.
			initializeFrames(selenium);
			for(Arbre<FrameElement> frame: frames){
				WDLibrary.switchToFrame(frame, selenium);
				elements = _findTextFieldsFromLabel(textlabel);
				if(elements!=null && !elements.isEmpty()){
					return elements;
				}
			}
		}

		//If we cannot find the TextField-type elements, then we try to find it by its label
		//If we can find the label element, the real-editbox is must be around (perhaps in the parent-tree).
		WebElement labelElement = findSelectableItemFromText(textlabel);
		if(labelElement instanceof WebElement){
			return findElementsInParent(labelElement, XPATH.relativeDescendants(XPATH.EDITBOX));
		}

		return null;
	}

	/**
	 * Find all EditBox-type fields that are associated with the provided textlabel in some way.<br>
	 * Possible matches can be a TEXTAREA or INPUT tag with a 'placeholder' attribute, or any type
	 * of tag with an 'aria-placeholder' attribute, with the attribute value matching the provided
	 * textlabel.<br>
	 * We can also match on a field if the provided textlabel matches the contents (value) of the
	 * field.<br>
	 * This routine does not yet support partial text matching, but it probably should once fully evaluated.<br>
	 * @param textlabel -- placeholder text or displayed text value to match against.
	 * @return the first editbox-type field matching the textlabel criteria, or null if not found.
	 */
	private List<WebElement> _findTextFieldsFromLabel(String textlabel){
		/*
		 * find a text box based on a hint text label inside it,
		 * or an external text node associated with it,
		 * or actual text value inside it?
		 */
		String dbgmsg  = StringUtils.debugmsg(false);
		RemoteDriver selenium = (RemoteDriver) WDLibrary.getWebDriver();
		List<WebElement> elements = null;
		String xpath = "//textarea[@placeholder='"+textlabel+"']"
			         +"|//input[@placeholder='"+textlabel+"']"
				     +"|//*[@aria-placeholder='"+textlabel+"']"
				     +"|//textarea[.='"+textlabel+"']"
				     +"|//input[.='"+textlabel+"']";
		try{
			elements = selenium.findElements(By.xpath(xpath));
			if(elements==null||elements.isEmpty()){
				IndependantLog.info(dbgmsg+" found no text input elements with placeholder-text or with text '"+ textlabel +"'");
			}else{
				// these are MATCHES!
				IndependantLog.info(dbgmsg+" got "+ elements.size() +" matched text input elements with placeholder-text or with text '"+ textlabel +"'");
				return elements;
			}
		}catch(Exception e){
			IndependantLog.warn(dbgmsg+" found no text input elements with placeholder-text or with text '"+ textlabel +"', met "+e);
		}

		return null;
	}

	/**
	 * Find an EditBox-type field that is associated with the provided textlabel in some way.<br>
	 * Possible matches can be a TEXTAREA or INPUT tag with a 'placeholder' attribute, or any type
	 * of tag with an 'aria-placeholder' attribute, with the attribute value matching the provided
	 * textlabel.<br>
	 * We can also match on a field if the provided textlabel matches the contents (value) of the
	 * field.<br>
	 * We can also match on a field if the provided textlabel matches the label-component around the text-field.<br>
	 * This routine does not yet support partial text matching, but it probably should once fully evaluated.<br>
	 * If the editbox is not found, we will attempt to look in a child iframe, if present.
	 * @param textlabel -- placeholder text or displayed text value to match against.
	 * @return the first editbox-type field matching the textlabel criteria, or null if not found.
	 */
	public WebElement findTextFieldFromLabel(String textlabel){
		String dbgmsg  = StringUtils.debugmsg(false);
		List<WebElement> elements = findTextFieldsFromLabel(textlabel);

		if(elements==null||elements.isEmpty()){
			IndependantLog.info(dbgmsg+" found no text input elements with text '"+ textlabel +"'");
		}else{
			// these are MATCHES!
			if (elements.size() == 1){
				IndependantLog.info(dbgmsg+" returning only text input element with text '"+ textlabel +"'");
			}else{
				IndependantLog.info(dbgmsg+" retrieving first of "+ elements.size() +" text input elements with text '"+ textlabel +"'");
			}
			return elements.get(0);
		}

		return null;
	}

	/* one-time loaded mapping of editbox classes */
	protected static List<String> _editbox_map = null;
	/* one-time deduced xpath of all possible editbox class matching */
	protected static String _editbox_elements_xpath = null;
	/* one-time loading of class2type mapped values stored in external properties file. */
	protected static synchronized void loadEditBoxMap() throws SAFSObjectRecognitionException{
		if(_editbox_map != null) return;
		String[] _editbox_classes = WebDriverGUIUtilities.getTypeClassesMapping("EditBox");
		if(_editbox_classes == null || _editbox_classes.length==0)
			throw new SAFSObjectRecognitionException("No Class2Type mappings found for 'EditBox'.");
		IndependantLog.info("\nAISearch Loaded and listing known EditBox classes: ");
		_editbox_map = new ArrayList<String>();
		_editbox_elements_xpath = RS.XPATH.ofType("EditBox");
		for(String _aclass: _editbox_classes){
			IndependantLog.info(Constants.INDENT + _aclass);
			_editbox_map.add(_aclass);
			if(!_editbox_elements_xpath.isEmpty()) 	_editbox_elements_xpath += "|";
			_editbox_elements_xpath += "//*[contains(concat(' ',@class,' '),' "+_aclass+" ')]";
		}
		IndependantLog.info("\nAISearch created EditBox XPATH:");
		IndependantLog.info(Constants.INDENT + _editbox_elements_xpath + "\n");
	}

	/**
	 * Not yet fully functional.  Intent is to be able to locate EditBox-type WebElements based on their
	 * CSS classes matching the Class2Type mappings loaded from external properties files or other sources.
	 * If the element is not found, and an embedded iframe exists, then we will attempt to switch to that iframe
	 * and continue the search there.
	 * @param textlabel
	 * @return WebElement matching or null if not found.
	 * @throws SAFSException
	 */
	public WebElement findTextFieldFromClass(String textlabel) throws SAFSException{
		/*
		 * find a "text" box based on class mapping, a hint text label inside it,
		 * or an external text node associated with it,
		 * or actual text value inside it.
		 */
		String dbgmsg  = StringUtils.debugmsg(false);
		RemoteDriver selenium = (RemoteDriver) WDLibrary.getWebDriver();
		List<WebElement> elements = null;
		WebElement element = null;
		String matching_css = null;
		String matching_tag = null;
		if(_editbox_map == null) loadEditBoxMap();

        elements = selenium.findElements(By.xpath(_editbox_elements_xpath));
		if(elements==null||elements.isEmpty()){
			IndependantLog.debug(dbgmsg+" found no elements matching Class2Type 'EditBox' classnames.");
		}else{
			// these are all elements that might be editbox fields!
			element = elements.get(0);
			matching_css = element.getAttribute(HTMLConst.ATTRIBUTE_CLASS);
			matching_tag = element.getTagName();
			if (elements.size() == 1){
				IndependantLog.info(dbgmsg+" returning '"+ matching_tag +"' element matching EditBox css: '"+ matching_css +"'");
			}else{
				IndependantLog.info(dbgmsg+" retrieving first of "+ elements.size() +" elements with tag '"+ matching_tag +"' matching EditBox css: '"+ matching_css +"'");
			}
			// now we have to filter these by placeholder or associated text value,
			// or associated aria attributes...
			return element;
		}
		WebElement result = getFirstIFrame(selenium);
		if(result instanceof WebElement){
			//selenium.switchTo().frame(result);
			return findTextFieldFromClass(textlabel);
		}
		return null;
	}

	//============================= "cucumber step definitions are defined" as below  ===============================
	/**
	 * Handle the <a href="/sqabasic2000/EditBoxFunctionsIndex.htm">EditBoxFunctions</a> against the text-field matching the provided textlabel.<br>
	 * Some actions (such as typekeys, SetTextValue, SetUnverifiedTextValue) can handle <a href="/doc/org/safs/tools/input/CreateUnicodeMap.html">special keys</a>
	 * <p>
	 * Cucumber Expression: "{editbox_action} {var_or_string} in the {mapitem_or_string} {editbox}"<br>
	 * {checkbox_action} Matches {@link TypeRegistryConfiguration#REGEX_EDITBOX_ACTION}<br>
	 * {var_or_string} represents a variable name (with an optional leading symbol ^) or a double-quoted-string or a single-quoted-string.<br>
	 * {var_mapitem_or_string} Matches {@link TypeRegistryConfiguration#REGEX_VAR_MAPITEM_OR_STRING},
	 *                     represents a variable, or a map item, such as mapID:section.item or a double-quoted-string or a single-quoted-string.
	 *                     It will be parsed by {@link TypeRegistryConfiguration}<br>
	 * {editbox} Matches possible names referring to an "editbox", {@link TypeRegistryConfiguration#REGEX_EDITBOX}<br>
	 * <p>
	 * Examples invocations:
	 * <p><ul><code>
	 * <li>Then type "My Search Query" in the "Search" field
	 * <li>And type "MyUserID" in the "UserId" box
	 * <li>But type "Something Else" in the "Comment" textfield
	 * <li>Then typechars "to delete things" in the "TextArea" textarea
	 * <li>Then typekeys "^a{Delete}" in the "TextArea" textarea
	 * <li>Then SetTextCharacters "+(Hello World) {a 10}" in the "TextArea" editbox
	 * <li>Then SetTextValue "+(Hello World) {a 10}" in the "TextArea" editbox
	 * <li>Then SetUnverifiedTextCharacters "+(Hello World)" in the "TextArea" textarea
	 * <li>Then SetUnverifiedTextValue "+(Hello World)" in the "TextArea" textarea
	 * <li>type something into the text-field identified by the value of map item SapDemoApp:SAPDemoPageAI.TextArea<br>
	 *     == SapDemoApp.map file =========<br>
	 *     [SAPDemoPageAI]<br>
	 *     TextArea="TextArea"<br>
	 *     ================================<br>
	 *     <b>Then type "My Search Query" in the SapDemoApp:SAPDemoPageAI.TextArea field</b>
	 * </code></ul>
	 * @param action String, the action to perform. It should be one of {@link TypeRegistryConfiguration#REGEX_EDITBOX_ACTION}.
	 * @param contents String, the characters/keys to input
	 * @param criteria String, the search-conditions helping to find the EditBox<br>
	 * @param type String, the possible name referring to an "editbox"
	 * @throws SAFSObjectNotFoundException if the item is not found and abort on find failure is enabled.
	 * @see AIMiscActions#abort_testing_on_item_not_found()
	 * @see AIMiscActions#continue_testing_on_item_not_found()
	 * @see AIComponent#process(String, String, int, List)
	 */
	@Then("{editbox_action} {var_or_string} in the {var_mapitem_or_string} {editbox}")
	public void type_in_editbox(String action, String contents, String criteria, String type) throws SAFSException {
		loadEditBoxMap();
		List<String> parameters = new ArrayList<String>();
		parameters.add(contents);
		process(action, criteria, 1, parameters);
	}

}
