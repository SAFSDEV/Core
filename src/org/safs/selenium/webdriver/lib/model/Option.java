/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
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
	
	public Option(Object object){
		super(object);
		//If this class overrides the method updateFields(), don't forget the call it here.
		//otherwise the local fileds will be initialized to default value.
//		updateFields();
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
}//End of Option Class
