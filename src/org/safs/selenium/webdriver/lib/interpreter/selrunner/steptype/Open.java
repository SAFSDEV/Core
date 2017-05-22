/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
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
		if(WDLibrary.isValidBrowserID(bid)){
			try{
				WDLibrary.startBrowser(null, url, bid, 90, true);
				return true;
			}catch(Exception x){
				ctx.log().error("");
				return false;
			}
		}else{
			ctx.getDriver().get(url);
		}
		return true;
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
