/**
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.model;

import org.safs.selenium.webdriver.lib.SeleniumPlusException;

/**
 *
 * History:<br>
 *
 *  <br>   May 30, 2014    (Lei Wang) Initial release.
 */
public interface IListSelectable extends ISelectable {
	/**
	 * Get all items.
	 * @return Item[] an array of the item
	 * @throws SeleniumPlusException
	 */
	public Item[] getContent() throws SeleniumPlusException;
}
