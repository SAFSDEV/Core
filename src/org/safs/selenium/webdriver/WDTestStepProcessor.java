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
package org.safs.selenium.webdriver;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.TestStepProcessor;

/**
 * @author Carl Nagle
 *
 */
public class WDTestStepProcessor extends TestStepProcessor {

	/**
	 * 
	 */
	public WDTestStepProcessor() {
		super();
	}

	  /**
	   * Called from superclass to find the windowObject and compObjects WebElements for GUI commands.
	   * <p>
	   * This routine is called only after all MixedUse recognition strings have separately been handled. 
	   * This routine is called only after all non-GUI commands have separately been handled.
	   * This routine is only called if it has been determined we have a "normal" GUI command in which 
	   * OBT window and component objects need to be found.
	   * <p> 
	   * setActiveWindow is NOT invoked since that is not implemented in WebDriverGUIUtilities.
	   * <p>
	   * The primary reason for this override is to prevent duplicate Window searches and JavaScript 
	   * execution that are negatively impacting performance unnecessarily.
	   * @return
	   * @throws SAFSException 
	   */
	  @Override
	  protected boolean getWinAndCompGUIObjects() throws SAFSException{
	      Log.debug("WDTestStepProcessor.getWindAndCompGUIObjects calling waitForObject and verifying status...");
	      //get both window and component (if any) in a single call.
	      return waitForObjectAndCheck(false);
	  }
	  
	
}
