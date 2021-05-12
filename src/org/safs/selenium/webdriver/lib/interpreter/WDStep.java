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
 * @date 2017-12-22    (Lei Wang) Initial release.
 * @date 2017-12-25    (Lei Wang) Modified handleAttributes():
 *                                         Support multiple browsers to ignore, the browser names are separated by comma.
 *                                         Support simplified browser name, which is defined as BROWSER_NAME_XXX in SelectBrowser.
 * @date 2018-01-16    (Lei Wang) Added method isUnModifiableParameter().
 */
package org.safs.selenium.webdriver.lib.interpreter;

import java.util.List;
import java.util.Map;

import org.safs.Constants.BrowserConstants;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.SelectBrowser;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

/**
 * @author Lei Wang
 *
 */
public class WDStep extends Step{

	/**
	 * The attribute 'ignoredBrowser' of script command, which is param[0].<br>
	 * Its value can be a comma-separated-string, such as "chrome, explorer, firefox".<br>
	 * Each browser name can be<br>
	 * {@link BrowserConstants#BROWSER_NAME_IE}<br>
	 * {@link BrowserConstants#BROWSER_NAME_CHROME}<br>
	 * {@link BrowserConstants#BROWSER_NAME_FIREFOX}<br>
	 * {@link BrowserConstants#BROWSER_NAME_EDGE}<br>
	 */
	public static final String ATTRIB_IGNORED_BROWSER  = "ignoredBrowser";

	/**
	 * This list of Map holds the attributes for every parameter of this step, they are attributes in tag <td>
	 */
	protected List<Map<String, String>> paramAttributes = null;

	/**
	 * This Map holds the attributes for this step, they are attributes in the tag <tr>
	 */
	protected Map<String, String> stepAttributes = null;

	public WDStep(StepType type) {
		super(type);
	}

	/**
	 * @return the paramAttributes
	 */
	public List<Map<String, String>> getParamAttributes() {
		return paramAttributes;
	}

	/**
	 * @param paramAttributes the paramAttributes to set
	 */
	public void setParamAttributes(List<Map<String, String>> paramAttributes) {
		this.paramAttributes = paramAttributes;
	}

	/**
	 * @return the stepAttributes
	 */
	public Map<String, String> getStepAttributes() {
		return stepAttributes;
	}

	/**
	 * @param stepAttributes the stepAttributes to set
	 */
	public void setStepAttributes(Map<String, String> stepAttributes) {
		this.stepAttributes = stepAttributes;
	}

	/**
	 * Save the command's parameter into the Map {@link Step#stringParams}.
	 * @param key String, the key of command's parameter
	 * @param value String, the value of the command's parameter
	 */
	public void putParam(String key, String value){
		stringParams.put(key, value);
	}

	/**
	 * @param param String, the parameter's name
	 * @return String, the parameter's value
	 */
	public String getParam(String param){
		return stringParams.get(param);
	}

	/**
	 * @param ctx TestRun, the context.
	 * @throws IgnoredStepException
	 */
	public void handleAttributes(TestRun ctx) throws IgnoredStepException{
		String debugmsg = "WDStep.handleAttributes(): ";

		if(stepAttributes!=null){
			//'ignoredBrowser' is a step's attribute
			Object attribute = stepAttributes.get(ATTRIB_IGNORED_BROWSER);
			if(attribute!=null){
				String currentBrowser = ctx.getDriver().getCapabilities().getBrowserName();
				String[] ignoredBrowsers = attribute.toString().split(StringUtils.COMMA);
				ctx.getLog().debug(debugmsg+ "currentBrowser '"+currentBrowser+"' ==? ignoredBrowsers '"+ignoredBrowsers+"'");

				for(String ignoredBrowser: ignoredBrowsers){
					if(SelectBrowser.isBrowser(currentBrowser, ignoredBrowser)){
						String message = "The step '"+name+"' is ignored for browser '"+currentBrowser+"'\n"+this.toPrettyString();
						ctx.getLog().debug(debugmsg+message);
						throw new IgnoredStepException(message);
					}
				}
			}
		}

		//paramAttributes should be handled in 'StepType'?
//		if(paramAttributes!=null){
//			Map<String, String> commandAttribs = paramAttributes.get(0);
//			Object attribute = commandAttribs.get(ATTRIB_IGNORED_BROWSER);
//			if(attribute!=null){
//
//			}
//		}
	}

	/**
	 * The name of step's parameter whose value should not be modified during debug.<br>
	 * "negated", "type", "step_name".<br>
	 */
	private static final String[] UN_MODIFIABLE_PARAMETERS = {"negated", "type", "step_name"};
	/**
	 * To test if the step's parameter is modifiable. Some parameters should not be modified, see {@link #UN_MODIFIABLE_PARAMETERS}.
	 *
	 * @param parameter String, the parameter's name.
	 * @return boolean
	 */
	public static boolean isUnModifiableParameter(String parameter){
		for(String unModifiableParam: UN_MODIFIABLE_PARAMETERS){
			if(unModifiableParam.equalsIgnoreCase(parameter)) return true;
		}
		return false;
	}
}
