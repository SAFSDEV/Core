/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * MAY 22, 2017    (SBJLWA)  Modified run(): call WDLibrary.isValidBrowserID() to check the validity of browser ID.
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
 * @author canagl
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
			ctx.log().error("");
			return false;
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
