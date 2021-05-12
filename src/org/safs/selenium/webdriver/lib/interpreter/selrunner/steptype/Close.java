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
 * MAY 22, 2017    (Lei Wang)  Modified run(): call WDLibrary.isValidBrowserID() to check the validity of browser ID.
 */
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

/**
 * Supports an optional "browserid" parameter which can be used to reference the specific browser instance
 * to close.  Generally, it is the same browserid that would be used in the Open command.
 * @author Carl Nagle
 * @see Open
 */
public class Close implements StepType, SRunnerType {

	public static String BROWSERID_PARAM = "browserid";

	/* (non-Javadoc)
	 * @see com.sebuilder.interpreter.StepType#run(com.sebuilder.interpreter.TestRun)
	 */
	@Override
	public boolean run(TestRun ctx) {
		String bid = ctx.string(BROWSERID_PARAM);
		if(!WDLibrary.isValidBrowserID(bid)){
			bid = WDLibrary.getIDForWebDriver(ctx.getDriver());
		}
		try{
			WDLibrary.stopBrowser(bid);
			return true;
		}catch(Exception x){
			try{
				ctx.log().warn("Failed to close browser, due to "+x+"\n try ctx.getDriver().quit();");
				ctx.getDriver().quit();
				return true;
			}catch(Exception e){
				ctx.log().error("Failed to close browser, due to "+e);
				return false;
			}
		}
	}

	/**
	 * Optional params[1] "browserid"
	 */
	@Override
	public void processParams(Step step, String[] params) {
		try{
			step.stringParams.put(BROWSERID_PARAM, params[1]);
		}catch(Exception ignore){}
	}

}
