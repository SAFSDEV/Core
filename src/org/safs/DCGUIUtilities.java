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
package org.safs;

import java.util.List;

/**
 * Concrete subclass of DDGUIUtilities that provides no real implementation for the 
 * handling of GUI components.  This is primarily intended to be used by processors 
 * or engines that provide features and functions that do no act on GUI components--
 * for example, many non-component Driver Commands.
 */
public class DCGUIUtilities extends DDGUIUtilities {

	/**
	 * @return null
	 * @see DDGUIUtilities#extractMenuItems(Object)
	 */
	public Tree extractMenuItems(Object obj) throws SAFSException {
		return null;
	}

	/**
	 * @return null
	 * @see DDGUIUtilities#extractMenuBarItems(Object)
	 */
	public Tree extractMenuBarItems(Object obj) throws SAFSException {
		return null;
	}

	/**
	 * @return "item:"+i+", "+obj;
	 * @see DDGUIUtilities#getListItem(Object, int, String)
	 */
	public String getListItem(Object obj, int i, String itemProp)
		throws SAFSException {
      return "item:"+i+", "+obj;
	}

	/**
	 * @return List with "item: "+obj
	 * @see DDGUIUtilities#extractListItems(Object, String, String)
	 */
	public List extractListItems(Object obj, String countProp, String itemProp)
		throws SAFSException {
      java.util.List list = new java.util.LinkedList();
      list.add("item: "+obj);
      return list;
	}

	/**
	 * @return null
	 * @see DDGUIUtilities#findPropertyMatchedChild(Object, String, String, boolean)
	 */
	public Object findPropertyMatchedChild(
		Object obj,
		String property,
		String bench,
		boolean exactMatch)
		throws SAFSObjectNotFoundException {
		return null;
	}

	/**
	 * @return 0
	 * @see DDGUIUtilities#setActiveWindow(String, String, String)
	 */
	public int setActiveWindow(
		String appMapName,
		String windowName,
		String compName)
		throws SAFSObjectNotFoundException {
		return 0;
	}

	/**
	 * @return 0
	 * @see DDGUIUtilities#waitForObject(String, String, String, long)
	 */
	public int waitForObject(
		String appMapName,
		String windowName,
		String compName,
		long secTimeout)
		throws SAFSObjectNotFoundException {
		return 0;
	}

}

