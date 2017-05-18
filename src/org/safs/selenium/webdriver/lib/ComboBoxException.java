/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
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
