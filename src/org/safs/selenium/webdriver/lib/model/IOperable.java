/**
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.model;

/**
 * This represents something that can be operated.<br>
 * It may have some caches containing objects difficult to get (time-consuming)<br>
 * <br>
 * History:<br>
 *
 *  <br>   Jul 11, 2014    (Lei Wang) Initial release.
 */
public interface IOperable{
	public void clearCache();
//	public void clearOtherCache();
}
