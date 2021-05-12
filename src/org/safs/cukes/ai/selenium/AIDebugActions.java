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
package org.safs.cukes.ai.selenium;

import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.StringUtils;
import cucumber.api.java.en.Then;

/** 
 * Concept: To hold debug test step definitions callable from gherkin feature files.
 */ 
public class AIDebugActions extends AISearchBase {

	/** 
	 * Run a predefined set of debugging activities.<br>
	 * Currently, this does nothing until debug activities are added to the function.
	 * <p>
	 * Examples invocations:
	 * <p><ul><code>
	 * Then run AISearch debug tests<br>
	 * Or<br>
	 * And run AISearch debug tests<br>
	 * </code></ul>
	 */
	@Then("run AISearch debug tests")
	public void run_AISearch_debug_tests() throws SAFSException{
		String dbgmsg = StringUtils.debugmsg(false);
		String msg = null;
		try{
			// session = WDLibrary.getWebDriver();
			// String browser = WDLibrary.getIDForWebDriver(session);
			
			// put stuff in here to test 

			msg = "No debug code currently exists for this step.";
			IndependantLog.info(dbgmsg +" "+msg);
			Logging.LogTestSuccess(msg);

		}catch(Exception spx){
			msg = "Did not find any running browser session!";
			IndependantLog.error(dbgmsg + " "+ msg);
			Assert.fail(msg);
			if(_abort_on_find_failure) throw new org.safs.SAFSObjectNotFoundException(msg);
		}
	}	
}
