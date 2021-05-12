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
package org.safs.selenium.webdriver.lib;

/**
 *
 * History:<br>
 *
 *  <br>   DEC 18, 2013    (Lei Wang) Initial release.
 */
public class ComboBoxException extends SeleniumPlusException {

	private static final long serialVersionUID = 4269841911061638450L;

	public static final String CODE_FAIL_VERIFICATION = ComboBoxException.class.getSimpleName()+":CODE_FAIL_VERIFICATION";
	public static final String CODE_NO_MATCHING_ITEM = ComboBoxException.class.getSimpleName()+":CODE_NO_MATCHING_ITEM";
	public static final String CODE_INDEX_OUTOF_RANGE = ComboBoxException.class.getSimpleName()+":CODE_INDEX_OUTOF_RANGE";
	public static final String CODE_NOTHING_SELECTED = ComboBoxException.class.getSimpleName()+":CODE_NOTHING_SELECTED";
	public static final String CODE_FAIL_CLOSE_POPUP = ComboBoxException.class.getSimpleName()+":CODE_FAIL_CLOSE_POPUP";
	public static final String CODE_FAIL_OPEN_POPUP = ComboBoxException.class.getSimpleName()+":CODE_FAIL_OPEN_POPUP";

	public ComboBoxException(String detailMessage) {
		super(detailMessage);
	}

	public ComboBoxException(String detailMessage, String code) {
		super(detailMessage, code);
	}

	public ComboBoxException(String detailMessage, String code, String info) {
		super(detailMessage, code, info);
	}

	public ComboBoxException(Object obj, String msg) {
		super(obj, msg);
	}

	public ComboBoxException(Object obj, String methodName, String msg) {
		super(obj, methodName, msg);
	}
}
