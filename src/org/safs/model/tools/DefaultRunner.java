/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * NOV 02, 2016    (Lei Wang) Initial release.
 */
package org.safs.model.tools;

import org.safs.IndependantLog;
import org.safs.JavaHook;

/**
 * The DefaultRunner will use the default Driver {@link org.safs.model.tools.Driver}<br>
 * if the current Driver is null.  
 *
 */
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
