/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2017年6月14日    (SBJLWA) Initial release.
 */
package org.safs.selenium.webdriver.lib.interpreter.selrunner;

import java.util.Map;

import com.sebuilder.interpreter.TestRun;

/**
 * @author sbjlwa
 *
 */
public class Utils {
	/**
	 * The testing tool (such as Selenium IDE) generated-javascript may
	 * be embedded between {@link Constants#PARAM_SCRIPT_PREFIX} and {@link Constants#PARAM_SCRIPT_SUFFIX}.
	 * We need to remove them to get the real javascript code to execute.
	 *
	 * @param jscode String, the javascript code.
	 * @return String, the normalized javascript code.
	 */
	public static String normalize(String jscode){
		//strip the embedding prefix "javascript{" and suffix "}".
		String jscodeLC = jscode.toLowerCase();
		int start = jscodeLC.indexOf(Constants.PARAM_SCRIPT_PREFIX);
		int end = jscodeLC.indexOf(Constants.PARAM_SCRIPT_SUFFIX);

		if(start>-1 && end>-1 && end>start){
			jscode = jscode.substring(start+Constants.PARAM_SCRIPT_PREFIX.length(), end);
		}

		return jscode;
	}

	/**
	 * @param ctx TestRun, the context object.
	 * @return String, the javascript code to define the associate-array {@link Constants#PARAM_STOREDVARS_ARRAY} holding
	 *                 variables in the variable store of context (TestRun).
	 */
	public static String defineStoredVars(TestRun ctx){
		Map<String, String> storedVariables = ctx.vars();

		StringBuilder jscode = new StringBuilder();

		//Define a javascript object to represent the 'associate array'.
		//var storedVars = {};
		jscode.append("var "+Constants.PARAM_STOREDVARS_ARRAY+" = {};\n");
		for(String key:storedVariables.keySet()){
			//storedVars.key='value';
			jscode.append(Constants.PARAM_STOREDVARS_ARRAY+"."+key+"='"+storedVariables.get(key)+"';\n");
		}

		return jscode.toString();
	}

	/**
	 * Prepend 'script' with the associate-array {@link Constants#PARAM_STOREDVARS_ARRAY}
	 * holding variables in the variable store of context (TestRun), and then execute them together.
	 *
	 * @param ctx TestRun, the context within which to execute the script
	 * @param script String, the script to execute, it should be normalized.
	 * @return Object, the result
	 *
	 * @see #defineStoredVars(TestRun)
	 */
	public static Object executeScript(TestRun ctx, String script){
		String jscode = Utils.defineStoredVars(ctx) + script;
		ctx.getLog().debug("interpreter.selrunner.Utils.executeScript() javascript\n"+jscode);
		return ctx.driver().executeScript(jscode);
	}
}
