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
 *  <br>   Jun 03, 2014    (sbjlwa) Initial release.
 */
public interface IMenuSelectable extends IHierarchicalSelectable{

	/**
	 * Get all items withing a Menu.
	 * @return MenuItem[] an array of the MenuItem
	 * @throws SeleniumPlusException
	 */
	public MenuItem[] getContent() throws SeleniumPlusException;

	public MenuItem getMatchedElement(TextMatchingCriterion textMatchingCriterion) throws SeleniumPlusException;

	public void verifyMenuItem(TextMatchingCriterion criterion, String expectedStatus) throws SeleniumPlusException;
}
