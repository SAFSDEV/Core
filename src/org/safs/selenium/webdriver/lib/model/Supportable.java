/**
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.model;

import org.openqa.selenium.WebElement;

/**
 *
 * History:<br>
 *
 *  <br>   Apr 25, 2014    (sbjlwa) Initial release.
 */
public interface Supportable{
	/**
	 * Test if the element is supported.<br>
	 * @param element WebElement, the element to check.
	 */
	public boolean isSupported(WebElement element);

	/**
	 * @return String[], an array of class-name supported by this class
	 */
	public String[] getSupportedClassNames();
}
