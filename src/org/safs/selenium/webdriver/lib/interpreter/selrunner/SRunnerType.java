/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter.selrunner;

import com.sebuilder.interpreter.Step;

/**
 * @author Carl Nagle
 *
 */
public interface SRunnerType {

	public void processParams(Step step, String[] params);

}
