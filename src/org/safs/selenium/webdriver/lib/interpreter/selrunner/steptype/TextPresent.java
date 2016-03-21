/*
* Copyright 2012 Sauce Labs
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRUtilities;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Step;

public class TextPresent extends com.sebuilder.interpreter.steptype.TextPresent implements SRunnerType {

	@Override
	public void processParams(Step step, String[] params) {
		step.stringParams.put("text", params[1]);
		try{ 
			if(params[2].length() > 0){
				step.stringParams.put("variable", params[2]);
			}
		}catch(Throwable ignore){}
	}
}
