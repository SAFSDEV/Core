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
 * DEC 11, 2017    (Lei Wang)  Modified run(): Create a browser id if user doesn't provide.
 *                                           Launch the selenium server if it has not started.
 * DEC 19, 2017    (Lei Wang)  Modified run(): Reset the frame info for WDLocator.
 */
package org.safs.selenium.webdriver.lib.interpreter.selrunner.steptype;

import org.safs.selenium.webdriver.WebDriverGUIUtilities;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.interpreter.WDLocator;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

/**
 * Requires a "url" parameter.<br>
 * Supports an 2nd optional "browserid" parameter which can be used to reference the specific browser instance
 * in other commands, like Close.
 * @author Carl Nagle
 * @see Close
 */
public class Open implements StepType, SRunnerType {

	public static String URL_PARAM = "url";
	public static String BROWSERID_PARAM = "browserid";

	/* (non-Javadoc)
	 * @see com.sebuilder.interpreter.StepType#run(com.sebuilder.interpreter.TestRun)
	 */
	@Override
	public boolean run(TestRun ctx) {
		String bid = ctx.string(BROWSERID_PARAM);
		String url = ctx.string(URL_PARAM);

		if(!WDLibrary.isValidBrowserID(bid)){
			bid = "sebuilder_run_open"+System.currentTimeMillis();
		}

		WDLocator.resetFrameInfo();

		try {
			WDLibrary.startBrowser(null, url, bid, 90, true);
			return true;
		}catch(Exception x){
			ctx.log().error("Failed to start browser, due to "+x);
			try{
				WebDriverGUIUtilities.launchSeleniumServers();
				WDLibrary.startBrowser(null, url, bid, 90, true);
			}catch(SeleniumPlusException se){
				try{
					ctx.log().warn("Failed to start browser, due to "+se+"\ntry selenium webdriver.get().");
					ctx.getDriver().get(url);
					return true;
				}catch(Exception e){
					ctx.log().error("Failed to start browser, due to "+se);
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * Requires params[1] "url" and has optional params[2] "browserid"
	 */
	@Override
	public void processParams(Step step, String[] params) {
		step.stringParams.put(URL_PARAM, params[1]);
		if(params.length>2)step.stringParams.put(BROWSERID_PARAM, params[2]);
	}

}
