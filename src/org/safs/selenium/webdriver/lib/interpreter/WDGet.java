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
 * DEC 11, 2017 Lei Wang Modified method run(): Start a new session or even the selenium server if necessary.
 * DEC 12, 2017 Lei Wang Modified method run(): super.run(ctx) will return before the page is fully shown.
 *                                             With browser Edge, I met a stuck problem if the page is not fully shown.
 *                                             So pause 1 second to wait the page shown.
 * DEC 19, 2017 Lei Wang Modified method run(): Reset the frame info for WDLocator.
 */
package org.safs.selenium.webdriver.lib.interpreter;

import org.openqa.selenium.WebDriver;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.WebDriverGUIUtilities;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;

import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.steptype.Get;

/**
 * Override standard SeBuilder Get to launch a new Se+ session, if needed.
 * @author Carl Nagle
 */
public class WDGet extends Get {

	/**
	 * Initialize the superclass.
	 */
	public WDGet() {
		super();
	}

	@Override
	public boolean run(TestRun ctx){
		ctx.getLog().debug("Executing custom Get Step via WDLibrary.");
		boolean result = false;
		try{
			WDLocator.resetFrameInfo();

			int timeout = 60;
			WebDriver driver = ctx.getDriver();
			if(driver == null){
				//Start a new browser with empty URL.
				ctx.getLog().info("WDGet found no existing WebDriver session.  Attempting to start a new session.");
				String browserID = "sebuilder_run_get_"+System.currentTimeMillis();
				boolean sessionCreated = false;
				try{
					WDLibrary.startBrowser(null, null, browserID, timeout, true);
					sessionCreated = true;
				}catch(Throwable th){
					try {
						WebDriverGUIUtilities.launchSeleniumServers();
						try{
							WDLibrary.startBrowser(null, null, browserID, timeout, true);
							sessionCreated = true;
						}catch(Throwable th2){
							ctx.getLog().warn("WDGet.run failed to start a new WebDriver session:", th2);
						}
					} catch (SeleniumPlusException e) {
						ctx.getLog().warn("WDGet.run detected the expected RemoteServer is not running and cannot be started: "+StringUtils.debugmsg(e));
					}
				}
				if(sessionCreated){
					ctx.getLog().info("WDGet.run successful starting browser session.");
				}else{
					ctx.getLog().info("WDGet.run failed to start browser session.");
				}
			}

			result = super.run(ctx);
			//Pause to wait for the page shown.
			try{ Thread.sleep(1000); }catch(Exception e){}

		}catch(Throwable t){
			ctx.getLog().error("WDGet Error:", t);
		}
		return result;
	}
}
