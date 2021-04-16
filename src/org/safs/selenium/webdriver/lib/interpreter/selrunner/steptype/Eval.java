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
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.safs.selenium.webdriver.lib.interpreter.selrunner.Constants;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.Utils;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestRun;

/**
 * Eval will normally evaluate the javascript code passed as the first parameter.
 * This Eval will store the variables defined in {@link TestRun#vars()} into Javascript context
 * so that the expression like <b>storedVars['variable']</b> in javascript code will be resolved.
 * <BR/>
 * Below is an example copied from
 * <a href="http://www.seleniumhq.org/docs/02_selenium_ide.jsp#javascript-and-selenese-parameters">
 * Selenium IDE JavaScript Usage with Script Parameters</a>
 *
 * <table border=1>
 * <tr><th>Command</th><th>Target</th><th>Value</th></tr>
 * <tr><td>store</td><td>Edith Wharton</td><td>name</td></tr>
 * <tr><td>store<b>Eval</b></td><td>storedVars['name'].toUpperCase()</td><td>uc</td></tr>
 * </table>
 * The value "Edith Wharton" will be stored in variable <b>name</b>.
 * storeEval will evaluate the parameter <b>storedVars['name'].toUpperCase()</b>, it get the value
 * of variable <b>name</b> can make it uppercase, so the string "<b>EDITH WHARTON</b>" is stored in variable <b>uc</b>.
 *
 * @author Lei Wang
 */
public class Eval extends com.sebuilder.interpreter.steptype.Eval implements SRunnerType{

	public static final String PARAM_SCRIPT = "script";

	@Override
	public String get(TestRun ctx) {
		Object result = _eval(ctx, ctx.string(PARAM_SCRIPT));
		return result == null ? null : result.toString();
	}

	@Override
	public void processParams(Step step, String[] params) {
		step.stringParams.put(PARAM_SCRIPT, params[1]/*javascript expression*/);
		if(params.length>2) Utils.setParam(step, this, params[2]);
	}

	/**
	 * If the expression is enclosed by {@link Constants#PARAM_SCRIPT_PREFIX} and {@link Constants#PARAM_SCRIPT_SUFFIX}, they will be removed.<br/>
	 * Wrap the expression with "return" and ";" to create a javascript code to return result.<br/>
	 * @param expression String, javascript expression to evaluate,
	 *                           it may be enclosed by {@link Constants#PARAM_SCRIPT_PREFIX} and {@link Constants#PARAM_SCRIPT_SUFFIX}
	 * @return String, the normalized javascript code.
	 */
	protected static String normalize(String expression){
		String script = "return " + Utils.normalize(expression) +";";
		return script;
	}

	/**
	 * @param possibleExpression String
	 * @return boolean, if the parameter is a javascript expression.
	 */
	protected static boolean isJSExpression(String possibleExpression){
		if(possibleExpression==null) return false;
		String jscodeLC = possibleExpression.toLowerCase();
		int start = jscodeLC.indexOf(Constants.PARAM_SCRIPT_PREFIX);
		int end = jscodeLC.indexOf(Constants.PARAM_SCRIPT_SUFFIX);

		return (start>-1 && end>-1 && end>start);
	}

	/**
	 * Evaluate the expression if it is javascript code ({@link #isJSExpression(String)} return true).<br/>
	 * Otherwise return the expression as it is.<br/>
	 * @param ctx TestRun, the context within which the expression will be evaluated.
	 * @param expression String, the expression to evaluate
	 * @return Object the result
	 * @see #_eval(TestRun, String)
	 */
	public static Object eval(TestRun ctx, String expression){
		if(isJSExpression(expression)){
			return _eval(ctx, expression);
		}else{
			return expression;
		}
	}

	/**
	 * Evaluate the expression as javascript code.<br/>
	 * This method will
	 * <ol>
	 * <li>Remove from the expression the embedding prefix {@link Constants#PARAM_SCRIPT_PREFIX} and suffix {@link Constants#PARAM_SCRIPT_SUFFIX} if they exist.
	 * <li>Wrap the expression with "return " and ";"
	 * <li>Prepend the javascript definition of 'associate array' holding the variables in the variable store ({@link TestRun#vars()}).
	 * <li>Finally execute the javascript code and return its result.
	 * </ol>
	 * @param ctx TestRun, the context within which the expression will be evaluated.
	 * @param expression String, the expression to evaluate
	 * @return Object the result
	 *
	 * @see #normalize(String)
	 * @see Utils#executeScript(TestRun, String)
	 */
	protected static Object _eval(TestRun ctx, String expression){
		return Utils.executeScript(ctx, normalize(expression));
	}
}
