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
 * @date 2019-07-01    (Lei Wang) Changed findSelectableItemFromText() to static so that we it can be called from other class easily.
 * @date 2019-07-11    (Lei Wang) Added findSelectableItemsFromText() to return a list of matched elements.
 * @date 2019-07-31    (Lei Wang) Modified click_the_labelled_item() and verify_text_is_displayed(): accept also text referred by map-item.
 * @date 2019-08-09    (Lei Wang) Modified click_the_labelled_item(): by handling "click" in the super.process().
 *                                         verify_text_is_displayed(): by handling "GUIDoesExist" in the super.process().
 * @date 2019-08-23    (Lei Wang) Moved click_the_labelled_item() and verify_text_is_displayed() to AIComponentActions.
 *                                'click' and 'verify existence' are also required by other component.
 * @date 2019-08-28    (Lei Wang) Moved findSelectableItemsFromText() and findSelectableItemFromText() to AISearchBase.
 */
package org.safs.cukes.ai.selenium;

/**
 * Used to hold generic TEXT test step definitions for gherkin feature files.<br>
 * Generally matches on any element whose displayed text value/node matches the provided text value.<br>
 * Assumes the currently active WebDriver session unless otherwise specified.
 */
public class AITextActions extends AIComponent {

	/** "Text" */
	public static final String TYPE = "Text";

	@Override
	protected String getType(){
		return TYPE;
	}

}
