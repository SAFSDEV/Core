/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * JUN 13, 2017 (SBJLWA) Stripped the embedding prefix "javascript{" and suffix "}" from the script code.
 *                       Resolved the variable expression "storedVars['var']" in script code.
 */
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import java.util.Map;

import org.safs.selenium.webdriver.lib.interpreter.selrunner.Constants;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

public class RunScript implements StepType, SRunnerType {

	public static final String SCRIPT_PARAM  = "script";

	@Override
	public boolean run(TestRun ctx) {
		String jscode = defineStoredVars(ctx) + ctx.string(SCRIPT_PARAM);
		ctx.driver().executeScript(jscode);
		// what can we do with result?
		return true;
	}

	@Override
	public void processParams(Step step, String[] params) {
		//strip the embedding prefix "javascript{" and suffix "}".
		String jscode = params[1];

		int start = jscode.toLowerCase().indexOf(Constants.PARAM_SCRIPT_PREFIX);
		int end = jscode.toLowerCase().indexOf(Constants.PARAM_SCRIPT_SUFFIX);

		if(start>-1 && end>-1 && end>start){
			jscode = jscode.substring(start+Constants.PARAM_SCRIPT_PREFIX.length(), end);
		}

		step.stringParams.put(SCRIPT_PARAM, jscode);
	}

	/**
	 * @param ctx TestRun, the context object.
	 * @return String, the javascript code to define the 'associate array' holding
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

}
