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
 * JUN 13, 2017 (Lei Wang) Stripped the embedding prefix "javascript{" and suffix "}" from the script code.
 *                        Resolved the variable expression "storedVars['var']" in script code.
 * DEC 26, 2017 (Lei Wang) Support attribute 'sync' to execute script synchronously/asynchronously.
 */
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import java.util.List;
import java.util.Map;

import org.safs.selenium.webdriver.lib.interpreter.WDStep;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.Utils;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

public class RunScript implements StepType, SRunnerType {
	/**
	 * attribute 'sync' of command which is param[0]
	 */
	public static final String ATTRIB_SYNC  = "sync";

	public static final String SCRIPT_PARAM  = "script";

	@Override
	public boolean run(TestRun ctx) {
		boolean sync = true;

		//get parameter's attributes of the current step
		Step step = ctx.currentStep();
		if(step instanceof WDStep){
			List<Map<String, String>> paramAttributes = ((WDStep)step).getParamAttributes();
			if(paramAttributes!=null && paramAttributes.size()>0){
				//The first parameter (at index 0) is command itself. Handle the command's attribute 'sync'.
				Map<String, String> commandAttributes = paramAttributes.get(0);
				String attribute = commandAttributes.get(ATTRIB_SYNC);
				if(attribute!=null){
					if("false".equalsIgnoreCase(attribute.trim())) sync=false;
				}
			}
		}

		Utils.executeScript(ctx, ctx.string(SCRIPT_PARAM), sync);
		// what can we do with result?
		return true;
	}

	@Override
	public void processParams(Step step, String[] params) {
		step.stringParams.put(SCRIPT_PARAM, Utils.normalize(params[1]));
	}
}
