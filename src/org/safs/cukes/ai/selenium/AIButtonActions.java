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
 * @date 2019-08-23    (Lei Wang) Call method process() to execute "GUIDOESEXIST" to verify button's existence.
 * @date 2019-08-27    (Lei Wang) Removed verify_the_labelled_button_is_displayed():
 */
package org.safs.cukes.ai.selenium;

/**
 * Used to hold BUTTON test step definitions for gherkin feature files.
 * Buttons are generally found by matching their displayed text.
 */
public class AIButtonActions extends AIComponent {
	/** "Button" */
	public static final String TYPE = "Button";

	@Override
	protected String getType(){
		return TYPE;
	}

}
