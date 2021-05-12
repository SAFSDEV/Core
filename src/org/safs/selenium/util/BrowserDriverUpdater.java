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
 * @date 2020-09-27    (Lei Wang) Initial release.
 */
package org.safs.selenium.util;

/**
 * @author Lei Wang
 *
 */
public interface BrowserDriverUpdater {

	/**
	 * @return String, the current installed browser's version.
	 */
	public String getBrowserVersion();

	/**
	 * @return boolean, if the current installed browser is 32 bits.
	 */
	public boolean isBrowser32Bit();

	/**
	 * @return String, the current installed browser driver's version.
	 */
	public String getDriverVersion();

	/**
	 * According to the browser's version, find the matched driver version.<br>
	 *
	 * @param browserVersion String, the current installed browser's version
	 * @return String, the matched browser-driver's version
	 */
	public String getMatchedDriverVerion(String browserVersion);

	/**
	 * According to the driver's version, get the RUL containing this driver.<br>
	 *
	 * @param driverVersion String, the driver's version
	 * @return String, the URL pointing to the driver to download
	 */
	public String getDriverDownloadURL(String driverVersion);

	/**
	 * Download the matched browser-driver.
	 * Save the matched browser-driver in "SeleniumPlus\extra\drivers\<vendor>\<version>".
	 *
	 * @param driverVersion String, the matched browser-driver's version
	 * @return String, the folder containing the downloaded driver
	 */
	public String downloadBrowserDriver(String driverVersion);

	/**
	 * Update the browser's driver to the latest matched one.<br>
	 * Copy the original driver to "SeleniumPlus\extra\drivers\<vendor>\backup".<br>
	 * Copy the latest driver to the folder "SeleniumPlus\extra\" to replace the original one.<br>
	 * @return boolean, if the browser's driver has been updated successfully.
	 */
	public boolean update();
}
