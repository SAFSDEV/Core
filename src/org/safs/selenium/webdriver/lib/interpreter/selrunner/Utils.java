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
 * 2017-06-14    (Lei Wang) Initial release.
 * 2017-06-16    (Lei Wang) Modified Utils.setParam(): check the empty 'variable' parameter.
 * 2017-12-26    (Lei Wang) Modified executeScript(): add one more parameter 'sync'.
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
		return executeScript(ctx, script, true);
	}
	
	/**
	 * Prepend 'script' with the associate-array {@link Constants#PARAM_STOREDVARS_ARRAY}
	 * holding variables in the variable store of context (TestRun), and then execute them together.
	 *
	 * @param ctx TestRun, the context within which to execute the script
	 * @param script String, the script to execute, it should be normalized.
	 * @param sync boolean, if the script should be executed synchronously.
	 *                      If false, this method will return immediately.
	 * @return Object, the result
	 *
	 * @see #defineStoredVars(TestRun)
	 */
	public static Object executeScript(TestRun ctx, String script, boolean sync){
		String jscode = Utils.defineStoredVars(ctx) + script;
		ctx.getLog().debug("interpreter.selrunner.Utils.executeScript() javascript "+(sync? "synchronously":"asynchronously")+"\n"+jscode);
		if(sync){
			return ctx.driver().executeScript(jscode);
		}else{
			return ctx.driver().executeAsyncScript(jscode);
		}
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
			if(parameter.length()>0){
				step.stringParams.put(Constants.PARAM_VARIABLE/*"variable"*/, parameter);
			}else{
				throw new SAFSRuntimeException("Parameter 'variable' is an empty string, cannot be used as a valid variable!");
			}
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
