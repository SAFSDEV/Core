/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.server.handler.GetLogHandler;
import org.safs.selenium.webdriver.lib.SearchObject;

import sun.util.logging.resources.logging;

import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.steptype.SwitchToFrame;
import com.sebuilder.interpreter.steptype.SwitchToFrameByIndex;

/**
 * Override standard SeBuilder switchToFrame. Call SE+ version switchFrame() to finish work.
 * @author SCNTAX
 * <br>JUL 28, 2015 SCNTAX Added SwitchToFrame and SwitchToFrameIndex support
 */
public class WDSwitchToFrame extends SwitchToFrame {
	public WDSwitchToFrame() {
		super();
	}
		
	@Override
	public boolean run(TestRun ctx) {
		ctx.getLog().debug("WDSwitchToFrame executing switching frame via WDLocator by ID.");
		
		String frameInfo = "";
		String infoValue = ctx.string("identifier");
		
		if (null != infoValue && infoValue.length() > 0) {
			frameInfo = SearchObject.SEARCH_CRITERIA_FRAMEID + "=" + infoValue + ";\\;";
		} else {
			frameInfo = "";
		}
		
		WDLocator.setFrameInfo(frameInfo);
		ctx.getLog().debug("WDSwitchToFrame.run() switching to '" + WDLocator.getFrameInfo() + "'.");
		ctx.driver().switchTo().frame(infoValue);
		
		return true;
	}
}
