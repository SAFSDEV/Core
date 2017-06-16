/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2017年6月14日    (Lei Wang) Initial release.
 */
package org.safs.selenium.webdriver.lib.interpreter.selrunner;

import java.util.Map;

import org.safs.SAFSRuntimeException;

import com.sebuilder.interpreter.Getter;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.Store;
import com.sebuilder.interpreter.TestRun;

/**
 * @author Lei Wang
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

	/**
	 * According the step type, set the parameter into the step's variable store.
	 *
	 * @param step Step, for which to set the parameter
	 * @param getter Getter, from which to get the parameter's name
	 * @param parameter String, the parameter to store. <br>
	 *                          It may be literal string to compare;<br>
	 *                          or a variable name to hold the returned value.<br>
	 */
	public static void setParam(Step step, Getter getter, String parameter){
		if(step==null || getter==null || parameter==null){
			throw new SAFSRuntimeException("Parameter is null.\nstep="+step+"\ngetter="+getter+"\nparameter="+parameter);
		}
		if(step.type instanceof Store){
			//Store will always use "variable" to get the parameter
			step.stringParams.put(Constants.PARAM_VARIABLE/*"variable"*/, parameter);
		}else{
			//verify, waitFor, assert, the second parameter is the value to compare
			//the getter provides the parameter's name we should store the parameter as
			//later the Verify, WaitFor, Assert will use that name to get this parameter
			if(getter.cmpParamName()!=null){
				step.stringParams.put(getter.cmpParamName(), parameter);
			}//else if getter.cmpParamName() is null, that means this setter (such as ElementNotPresent, TextPresent etc.) doesn't need that parameter.
		}
	}

	/**
	 * Some command (such as 'assert', 'verify' and 'waitFor') can be
	 * appended with a getter (such as 'Text', 'Location' etc.) to form
	 * a positive command (assertText, verifyLocation); they can also be
	 * appended '<b>Not</b>' before the getter to form a negative command
	 * (assertNotText, verifyNotLocation).<br>
	 * This method will tell us if the command is a negative command.<br>
	 *
	 * @param command String, the name of command
	 * @return boolean, if the command is negative
	 */
	public static boolean isNegativeCommand(String command){
		if(command==null){
			throw new SAFSRuntimeException("command is null.");
		}
		String trimmedCommand = command.trim();
		return (trimmedCommand.startsWith(Constants.COMMAND_ASSERT_NOT) ||
				trimmedCommand.startsWith(Constants.COMMAND_VERIFY_NOT) ||
				trimmedCommand.startsWith(Constants.COMMAND_WAITFOR_NOT));
	}
}
