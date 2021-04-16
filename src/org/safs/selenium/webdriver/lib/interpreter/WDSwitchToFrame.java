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
 * History:
 * JUL 28, 2015 Tao Xie  Added SwitchToFrame and SwitchToFrameIndex support
 * DEC 18, 2017 Lei Wang Handled frame with more formats. Used WDLibrary.switchFrame() to switch frame.
 */
package org.safs.selenium.webdriver.lib.interpreter;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.SearchObject;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;

import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.steptype.SwitchToFrame;

/**
 * Override standard SeBuilder switchToFrame. Call SE+ version switchFrame() to finish work.
 * @author Tao Xie
 */
public class WDSwitchToFrame extends SwitchToFrame {
	public WDSwitchToFrame() {
		super();
	}

	@Override
	public boolean run(TestRun ctx) {
		ctx.getLog().debug("WDSwitchToFrame executing switching frame via WDLocator by ID.");

		String frameInfo = "";
		String parameter = ctx.string("identifier");
		RemoteWebDriver driver = ctx.getDriver();
		boolean switched = false;

		try{
			ctx.getLog().debug("WDSwitchToFrame.run(): parameter 'identifier' is '" +parameter+"'.");

			if (null!=parameter && !parameter.trim().isEmpty()) {
				String uppercaseParam = parameter.toUpperCase();

				if(uppercaseParam.startsWith(SearchObject.SEARCH_CRITERIA_FRAMEID) ||
						uppercaseParam.startsWith(SearchObject.SEARCH_CRITERIA_FRAMENAME) ||
						uppercaseParam.startsWith(SearchObject.SEARCH_CRITERIA_FRAMEXPATH) ||
						uppercaseParam.startsWith(SearchObject.SEARCH_CRITERIA_FRAMEINDEX)
						){
					frameInfo = parameter;
				}else if(uppercaseParam.contains("=")){
					String value = parameter.split("=")[1];
					if(uppercaseParam.startsWith("ID")){
						frameInfo = SearchObject.SEARCH_CRITERIA_FRAMEID + "=" + value;
					}else if(uppercaseParam.startsWith("NAME")){
						frameInfo = SearchObject.SEARCH_CRITERIA_FRAMENAME + "=" + value;
					}else if(uppercaseParam.startsWith("XPATH")){
						frameInfo = SearchObject.SEARCH_CRITERIA_FRAMEXPATH + "=" + value;
					}else if(uppercaseParam.startsWith("INDEX")){
						frameInfo = SearchObject.SEARCH_CRITERIA_FRAMEINDEX + "=" + value;
					}else if(uppercaseParam.startsWith("RELATIVE")){
						//"relative=xxx", WDLibrary.switchFrame() doesn't know how to handle it
						if("top".equalsIgnoreCase(value)){
							driver.switchTo().defaultContent();
							ctx.getLog().debug("WDSwitchToFrame.run() switching to top document.");
							switched = true;
						}else if("up".equalsIgnoreCase(value)){
							driver.switchTo().parentFrame();
							try{
								String currentFrame = WDLibrary.getCurrentFrameName(driver);
								if(StringUtils.isValid(currentFrame)){
									frameInfo = SearchObject.SEARCH_CRITERIA_FRAMENAME + "=" + currentFrame;
								}
							}catch(SeleniumPlusException e){
								ctx.getLog().error("WDSwitchToFrame.run(): failed to get parent frame's name, due to "+e);
								return false;
							}
							switched = true;
						}else{
							ctx.getLog().error("WDSwitchToFrame.run(): parameter 'identifier' of value '"+parameter+"' has not been supported yet.");
							return false;
						}
					}
				}else if(!uppercaseParam.contains("=")){
					//Without prefix, it might be 'name' or 'id'.
					frameInfo = SearchObject.SEARCH_CRITERIA_FRAMEID + "=" + parameter;
					driver.switchTo().defaultContent();
					ctx.getLog().debug("WDSwitchToFrame.run() trying switch to frame '"+frameInfo+"'.");
					switched = WDLibrary.switchFrame(driver, frameInfo);
					if(!switched){
						frameInfo = SearchObject.SEARCH_CRITERIA_FRAMENAME + "=" + parameter;
					}
				}else{
					ctx.getLog().error("WDSwitchToFrame.run(): parameter 'identifier' of value '"+parameter+"' has not been supported yet.");
					return false;
				}

				if(!switched){
					driver.switchTo().defaultContent();
					ctx.getLog().debug("WDSwitchToFrame.run() trying switch to frame '"+frameInfo+"'.");
					switched = WDLibrary.switchFrame(driver, frameInfo);
				}
			}else{
				//identifier is not provided, then we go back to the top document
				driver.switchTo().defaultContent();
				ctx.getLog().debug("WDSwitchToFrame.run() switching to top document.");
				switched = true;
			}

			//Set frame information to WDLocator so that it can search for element under correct frame.
			ctx.getLog().debug("WDSwitchToFrame.run() set frameInfo '"+frameInfo+"' to WDLocator.");
			WDLocator.setFrameInfo(frameInfo);
			//ctx.driver().switchTo().frame(parameter);

			return switched;
		}catch(Exception e){
			ctx.getLog().debug("WDSwitchToFrame.run() failed to set frame by '"+parameter+"', met "+e);
			return false;
		}
	}
}
