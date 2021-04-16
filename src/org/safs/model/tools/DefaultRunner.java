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
 * Logs for developers, not published to API DOC.
 *
 * History:
 * NOV 02, 2016    (Lei Wang) Initial release.
 * SEP 25, 2018    (Lei Wang) Annotate this class as @Component("org.safs.model.tools.DefaultRunner") so that it will be loaded by spring.
 *                           We need to provide a specific name "org.safs.model.tools.DefaultRunner" for this annotation so that spring
 *                           will not be confused with other sub-classes of AbstractRunner.
 */
package org.safs.model.tools;

import org.safs.IndependantLog;
import org.safs.JavaHook;
import org.springframework.stereotype.Component;

/**
 * The DefaultRunner will use the default Driver {@link org.safs.model.tools.Driver}<br>
 * if the current Driver is null.
 *
 */
@Component("org.safs.model.tools.DefaultRunner")
public class DefaultRunner extends AbstractRunner{

	public DefaultRunner(){
		super();
	}

	/**
	 * Currently, it returns the default {@link org.safs.model.tools.Driver}
	 */
	@Override
	public AbstractDriver getDriver() {
		if(driver==null){
			synchronized(this){
				if(driver==null){
					driver = new Driver();
				}
			}
		}
		return driver;
	}

	@Override
	public JavaHook hookDriver() {
		if(driver instanceof EmbeddedHookDriverDriver){
			return ((EmbeddedHookDriverDriver) driver).driver;
		}else{
			IndependantLog.debug("DefaultRunner.hookDriver(): There is no embedded hook to return.");
			return null;
		}
	}

}
