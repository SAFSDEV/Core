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
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

public class TypeKeys implements StepType, SRunnerType {

	public static final String TEXT_PARAM = "text";
	@Override
	public void processParams(Step step, String[] params) {
		step.stringParams.put(TEXT_PARAM, params[1]);
	}
	@Override
	public boolean run(TestRun ctx) {
		//int d = Robot.DEFAULT_MILLIS_BETWEEN_KEYSTROKES;
		try {
			//d = WDLibrary.getDelayBetweenKeystrokes();
			//if (d < 10) WDLibrary.setDelayBetweenKeystrokes(10);
			WDLibrary.inputKeys(null, ctx.string(TEXT_PARAM));
			return true;
		} catch (Exception e) {
			ctx.log().error("SelRunner Step TypeKeys "+ e.getClass().getSimpleName()+", "+ e.getMessage());
			return false;
		}finally{
			//try{WDLibrary.setDelayBetweenKeystrokes(d);}catch(Exception ignore){}
		}
	}
}
