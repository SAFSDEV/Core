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
 * 2017年4月1日    (Lei Wang) Initial release.
 */
package org.safs.autoit;

import org.safs.SAFSException;

/**
 * @author Lei Wang
 */
public interface IAutoItRs {

	/**
	 * @return String return AUTOIT <a href="https://www.autoitscript.com/autoit3/docs/intro/windowsadvanced.htm">window recognition string</a>.
	 */
	public String getWindowsRS();

	/**
	 * @return String return AUTOIT <a href="https://www.autoitscript.com/autoit3/docs/intro/windowsbasic.htm#specialtext">window text</a>.
	 */
	public String getWindowText();

	/**
	 * @return String return AUTOIT <a href="https://www.autoitscript.com/autoit3/docs/intro/controls.htm">control recognition string</a>.
	 */
	public String getComponentRS();

	/**
	 * @return boolean true if the recognition string represents a window.
	 * @throws SAFSException
	 */
	public boolean isWindow() throws SAFSException;
}
