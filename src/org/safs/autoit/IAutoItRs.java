/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

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
