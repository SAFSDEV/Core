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
