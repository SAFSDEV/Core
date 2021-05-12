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
 * History:
 *
 *  MAY 19, 2017	(Lei Wang) Modified constructor Item(Object object): for better performance, call initialize(object) instead of super(object); updateFields();
 */
package org.safs.selenium.webdriver.lib.model;

import java.util.Hashtable;

import org.safs.selenium.util.JavaScriptFunctions;


/**
 * This provide a uniformed Option object to represent the option<br>
 * within a container such as ComboBox etc.<br>
 *
 *  <br>   Jan 15, 2014    (Lei Wang) Initial release.
 */
public class Option extends Item{
	public static final String PROPERTY_NAME = "name";

	protected Option(){}

	public Option(Object object){
		initialize(object);
	}

	public Option setIndex(int index){
		this.index = index;
		return this;
	}

	/**
	 * create a javascript item object defined by the dojo/store/api/Store.<br>
	 * For example, item object is like { id="AL", value="AL", name="Alabama"}<br>
	 * @return
	 */
	public String defineStoreItemObject(){
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.put(PROPERTY_ID, id);
		properties.put(PROPERTY_VALUE, value);
		properties.put(PROPERTY_NAME, label);
		return JavaScriptFunctions.defineObject(properties);
	}
}
