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
 * 2017年6月13日    (Lei Wang) Initial release.
 */
package org.safs.selenium.webdriver.lib.interpreter.selrunner;

/**
 * @author Lei Wang
 */
public class Constants {
	/** '<b>javascript{</b>'*/
	public static final String PARAM_SCRIPT_PREFIX  	= "javascript{";
	/** '<b>}</b>'*/
	public static final String PARAM_SCRIPT_SUFFIX  	= "}";

	/** '<b>storedVars</b>'*/
	public static final String PARAM_STOREDVARS_ARRAY  = "storedVars";
	/** '<b>storedVars[</b>'*/
	public static final String PARAM_STOREDVARS_PREFIX  = PARAM_STOREDVARS_ARRAY+"[";
	/** '<b>]</b>'*/
	public static final String PARAM_STOREDVARS_SUFFIX  = "]";

	/** '<b>variable</b>'*/
	public static final String PARAM_VARIABLE  			= "variable";
	/** '<b>name</b>'*/
	public static final String PARAM_NAME	  			= "name";
	/** '<b>attributeName</b>'*/
	public static final String PARAM_ATTRIBUTE_NAME		= "attributeName";
	/** '<b>propertyName</b>'*/
	public static final String PARAM_PROPERTY_NAME		= "propertyName";
	/** '<b>value</b>'*/
	public static final String PARAM_VALUE				= "value";
	/** '<b>file</b>'*/
	public static final String PARAM_FILE				= "file";
	/** '<b>width</b>'*/
	public static final String PARAM_WIDTH				= "width";
	/** '<b>height</b>'*/
	public static final String PARAM_HEIGHT				= "height";
	/** '<b>identifier</b>'*/
	public static final String PARAM_IDENTIFIER			= "identifier";
	/** '<b>index</b>'*/
	public static final String PARAM_INDEX				= "index";
	/** '<b>title</b>'*/
	public static final String PARAM_TITLE				= "title";

	/** '<b>assert</b>'*/
	public static final String COMMAND_ASSERT	  			= "assert";
	/** '<b>store</b>'*/
	public static final String COMMAND_STORE	  			= "store";
	/** '<b>verify</b>'*/
	public static final String COMMAND_VERIFY	  			= "verify";
	/** '<b>waitFor</b>'*/
	public static final String COMMAND_WAITFOR  			= "waitFor";

	/** '<b>Not</b>'*/
	public static final String QULIFIER_NOT		  			= "Not";

	/** '<b>assertNot</b>'*/
	public static final String COMMAND_ASSERT_NOT	  			= COMMAND_ASSERT+QULIFIER_NOT;
//	/** '<b>storeNot</b>'*/
//	public static final String COMMAND_STORE_NOT	  			= COMMAND_STORE+QULIFIER_NOT;
	/** '<b>verifyNot</b>'*/
	public static final String COMMAND_VERIFY_NOT	  			= COMMAND_VERIFY+QULIFIER_NOT;
	/** '<b>waitForNot</b>'*/
	public static final String COMMAND_WAITFOR_NOT  			= COMMAND_WAITFOR+QULIFIER_NOT;

}
