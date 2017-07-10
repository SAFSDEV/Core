/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

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
