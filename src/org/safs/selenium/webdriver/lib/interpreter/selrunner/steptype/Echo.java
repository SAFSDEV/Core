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
 * JAN 12, 2018 (Lei Wang) Modified method run(): write the echo message into SAFS Log if possible.
 */
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.safs.selenium.webdriver.lib.interpreter.WDTestRun;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

/**
 * Echo the parameter provided by 'value' on the console.
 * @author Lei Wang
 * @see Open
 */
public class Echo implements StepType, SRunnerType {

	public static String VALUE_PARAM = "value";

	@Override
	public boolean run(TestRun ctx) {
		String value = ctx.string(VALUE_PARAM);

		try{
			if(ctx instanceof WDTestRun){
				((WDTestRun)ctx).getSAFSLog().info(value);
			}else{
				ctx.log().info(value);
			}
			return true;
		}catch(Exception x){
			ctx.log().warn("Failed to echo value "+value+", due to "+x);
			return false;
		}
	}

	/**
	 * Optional params[1] "value"
	 */
	@Override
	public void processParams(Step step, String[] params) {
		try{
			step.stringParams.put(VALUE_PARAM, params[1]);
		}catch(Exception ignore){}
	}

}
