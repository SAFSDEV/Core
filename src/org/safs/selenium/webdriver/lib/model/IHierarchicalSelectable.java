/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.model;

import org.safs.selenium.webdriver.lib.SeleniumPlusException;

/**
 * This interface. Sub interfaces are:<br>
 * <ul>
 * <li> {@link ITreeSelectable}
 * <li> {@link IMenuSelectable}
 * <li> {@link ITableSelectable}
 * </ul>
 * <br>
 * History:<br>
 * 
 *  <br>   Aug 7, 2014    (Lei Wang) Initial release.
 *  @see 
 */
public interface IHierarchicalSelectable extends ISelectable{
	public HierarchicalElement[] getContent() throws SeleniumPlusException;
}