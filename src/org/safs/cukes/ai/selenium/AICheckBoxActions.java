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
 * @date 2019-07-03    (Lei Wang) Handle the "check/uncheck" command.
 * @date 2019-07-11    (Lei Wang) Refactor code.
 * @date 2019-07-30    (Lei Wang) Modified check_uncheck_the_checkbox(): accept recognition-string defined in map file.
 */
package org.safs.cukes.ai.selenium;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.safs.SAFSException;
import org.safs.model.commands.CheckBoxFunctions;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;

import cucumber.api.java.en.Then;

/**
 * Used to hold checkbox test step definitions for gherkin feature files.
 * CheckBoxes are generally found by matching the {@link Criteria}.
 */
public class AICheckBoxActions extends AIComponent {
	/** "CheckBox" */
	public static final String TYPE = "CheckBox";

	protected org.safs.selenium.webdriver.lib.CheckBox checkbox = null;

	@Override
	protected void initComponent(WebElement we) throws SeleniumPlusException{
		libComponent = new org.safs.selenium.webdriver.lib.CheckBox(we);
	}

	@Override
	protected String getType(){
		return TYPE;
	}

	@Override
	protected void localProcess(String action, List<String> parameters) throws SAFSException {
		try{
			checkbox = (org.safs.selenium.webdriver.lib.CheckBox) libComponent;
		}catch(Exception e){
			throw new SAFSException("Failed to converted "+libComponent.getClass().getSimpleName()+" to "+getType());
		}

		if(CheckBoxFunctions.CHECK_KEYWORD.equalsIgnoreCase(action)){
			checkbox.check();
		}else if(CheckBoxFunctions.UNCHECK_KEYWORD.equalsIgnoreCase(action)){
			checkbox.uncheck();
		}else{
			throw new org.safs.SAFSNotImplementedException("Unknown action '"+action+"' for "+getType()+".");
		}
	}

	//============================= "cucumber step definitions are defined" as below  ===============================
	/**
	 * Handle the <a href="/sqabasic2000/CheckBoxFunctionsIndex.htm">CheckBoxFunctions</a> against the CheckBox matching the provided textlabel.<br>
	 * <p>
	 * Cucumber Expression: "{checkbox_action} the {mapitem_or_string} checkbox"<br>
	 * {checkbox_action} Matches {@link TypeRegistryConfiguration#REGEX_CHECKBOX_ACTION}<br>
	 * {var_mapitem_or_string} Matches {@link TypeRegistryConfiguration#REGEX_VAR_MAPITEM_OR_STRING},
	 *                     represents a variable, or a map item, such as mapID:section.item or a double-quoted-string or a single-quoted-string.
	 *                     It will be parsed by {@link TypeRegistryConfiguration}<br>
	 * <p>
	 * Example Scenario Step:
	 * <p>
	 * <ul>
	 * <li><b>Then Check the "Allow Multiple Select" checkbox</b>
	 * <li><b>Then UnCheck the "Allow Multiple Select" checkbox</b>
	 * <li>check the checkbox identified by xpath (leaded by {@link AISearchBase#XPATH_PREFIX})<br>
	 *     <b>Then Check the "XPATH=//*[@id='layout.center.tab.basc.checkbox']" checkbox</b>
	 * <li>uncheck the checkbox identified by css selector (leaded by {@link AISearchBase#CSS_PREFIX})<br>
	 *     <b>Then UnCheck the "CSS=[id='layout.center.tab.basc.checkbox'][role='checkbox']" checkbox</b>
	 * <li>check the checkbox identified by the value of map item CheckBoxAMS_CSS<br>
	 *     == on map chain, a map containing =========<br>
	 *     [ApplicationConstants]<br>
	 *     CheckBoxCSS="CSS=[id='layout.center.tab.basc.checkbox'][role='checkbox']"<br>
	 *     ================================<br>
	 *     <b>Then Check the CheckBoxCSS checkbox</b>
	 * <li>uncheck the checkbox identified by the value of map item SAPDemoPageAI.CheckBox<br>
	 *     == on map chain, a map containing =========<br>
	 *     [SAPDemoPageAI]<br>
	 *     CheckBox="Allow Multiple Select"<br>
	 *     ================================<br>
	 *     <b>Then UnCheck the SAPDemoPageAI.CheckBox checkbox</b>
	 * <li>check the checkbox identified by the value of map item SapDemoApp:SAPDemoPageAI.CheckBox<br>
	 *     == SapDemoApp.map file =========<br>
	 *     [SAPDemoPageAI]<br>
	 *     CheckBox="Allow Multiple Select"<br>
	 *     ================================<br>
	 *     <b>Then Check the SapDemoApp:SAPDemoPageAI.CheckBox checkbox</b>
	 * </ul>
	 *
	 * @param action String, the action to perform on the CheckBox
	 * @param criteria String, the search-conditions helping to find the CheckBox<br>
	 * @return
	 * @throws SAFSException if target object cannot be found.
	 * @see AIComponent#process(String, String, int, List)
	 */
	@Then("{checkbox_action} the {var_mapitem_or_string} checkbox")
	public void check_uncheck_the_checkbox(String action, String criteria) throws SAFSException {
		process(action, criteria, 1, new ArrayList<String>());
	}

}
