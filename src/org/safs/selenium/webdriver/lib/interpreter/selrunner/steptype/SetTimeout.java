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

import java.util.concurrent.TimeUnit;

import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

/**
 * Set WebDriver action "timeout" in milliseconds.
 * <p>
 * parameters:
 * <p>
 * <ol><li>"timeout", in milliseconds.
 * </ol>
 * <p>
 * @author Carl Nagle
 */
public class SetTimeout implements StepType, SRunnerType {

	public static final String TIMEOUT_PARAM = "timeout";

	/* (non-Javadoc)
	 * @see org.safs.selenium.webdriver.lib.interpreter.selrunner.SRunnerType#processParams(com.sebuilder.interpreter.Step, java.lang.String[])
	 */
	@Override
	public void processParams(Step step, String[] params) {
		step.stringParams.put(TIMEOUT_PARAM, params[1]);
	}

	@Override
	public boolean run(TestRun ctx) {
		String debugmsg = StringUtils.debugmsg(false);
		try{
			String tf = ctx.string(TIMEOUT_PARAM);
			ctx.log().info(debugmsg +"TIMEOUT parameter: "+ tf);

			Long toLong = Long.parseLong(tf);
			long to = toLong.longValue();
			if(to < 0){
				ctx.log().error(debugmsg + " specified TIMEOUT parameter cannot be less than 0.");
				return false;
			}
			// this is a permanent setting!  not a temporary one.
			// this will override whatever timeouts WebDriverGUIUtilities has set
			ctx.driver().manage().timeouts().implicitlyWait(to, TimeUnit.MILLISECONDS);
			return true;
		}catch(NullPointerException np){
			ctx.log().error(debugmsg + np.getClass().getSimpleName()+", "+ np.getMessage()+", probably because parameter TIMEOUT was not provided.");
		}catch(NumberFormatException nf){
			ctx.log().error(debugmsg + nf.getClass().getSimpleName()+", "+ nf.getMessage());
		}
		return false;
	}

}
