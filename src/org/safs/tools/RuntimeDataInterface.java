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
package org.safs.tools;

import org.safs.SAFSException;

/**
 * Classes implementing this Interface are providing access to various assets provided by the 
 * running test framework.  For example, access to global variables and App Map items.
 * @author Carl Nagle
 */
public interface RuntimeDataInterface {

	/**
	 * Retrieve the current runtime value of the global variable (SAFSVARS).
	 * @param varName -- Variable names are NOT case-sensitive. 
	 * @return String value which can be a zero-length value.  
	 * null if no such item was found or some type of error occurred during execution.
	 */
	public String getVariable(String varName) throws SAFSException;

	/**
	 * Set a new runtime value of a global variable (SAFSVARS).
	 * @param varName -- Variable names are NOT case-sensitive.
	 * @param varValue -- value to set.  Can be an empty string.
	 * @return value of the variable after it has been set.  
	 * can be null if the set was not successful or some other error occurred. 
	 */
	public boolean setVariable(String varName, String varValue) throws SAFSException;

	/**
	 * Retrieve the resolved value of an item stored in the App Map chain (SAFSMAPS).
	 * @param appMapId -- can be null to get from the "default/current" app map.
	 * @param sectionName -- section names are NOT case-sensitive. 
	 * @param itemName -- item names are NOT case-sensitive. 
	 * @return String value which can be a zero-length value.  
	 * null if no such item was found or some type of error occurred during execution.
	 */
	public String getAppMapItem(String appMapId, String sectionName, String itemName)throws SAFSException;
}
