/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter;

import org.safs.selenium.webdriver.lib.SearchObject;

import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.steptype.SwitchToFrameByIndex;

/**
 * Override standard SeBuilder switchToFrameByIndex. Call SE+ version switchFrame() to finish work.
 * @author SCNTAX
 * <br>JUL 28, 2015 SCNTAX Added SwitchToFrame and SwitchToFrameIndex support
 */
public class WDSwitchToFrameByIndex extends SwitchToFrameByIndex {
	public WDSwitchToFrameByIndex() {
		super();
	}
	
	@Override
	public boolean run(TestRun ctx) {
		ctx.getLog().debug("WDSwitchToFrameByIndex executing switching frame via WDLocator by index.");
		
		String frameInfo = "";
		String infoValue = ctx.string("index");
		
		if (null != infoValue && infoValue.length() > 0) {
			// SeBuilder frame index begins from 0, but SE+ frame index begins from 1.
			frameInfo = SearchObject.SEARCH_CRITERIA_FRAMEINDEX + "=" + (Integer.parseInt(infoValue) + 1) + ";\\;";
		} else {
			frameInfo = "";
		}		
		
		WDLocator.setFrameInfo(frameInfo);
		ctx.getLog().debug("WDSwitchToFrameByIndex.run() switching to '" + WDLocator.getFrameInfo() + "'.");
		ctx.driver().switchTo().frame(Integer.parseInt(infoValue));
		
		return true;
	}
}
