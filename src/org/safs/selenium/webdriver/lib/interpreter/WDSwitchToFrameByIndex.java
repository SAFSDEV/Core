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
package org.safs.selenium.webdriver.lib.interpreter;

import org.safs.selenium.webdriver.lib.SearchObject;

import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.steptype.SwitchToFrameByIndex;

/**
 * Override standard SeBuilder switchToFrameByIndex. Call SE+ version switchFrame() to finish work.
 * @author Tao Xie
 * <br>JUL 28, 2015 Tao Xie Added SwitchToFrame and SwitchToFrameIndex support
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
