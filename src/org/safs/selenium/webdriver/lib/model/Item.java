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
 *  APR 24, 2014    (Lei Wang) Initial release.
 *  OCT 29, 2015    (Lei Wang) Modify updateFields(): Trim the property 'value', the leading/ending spaces will be ignored.
 *  MAY 19, 2017	(Lei Wang) Modified constructor Item(Object object): for better performance, call initialize(object) instead of super(object); updateFields();
 */
package org.safs.selenium.webdriver.lib.model;

import org.safs.selenium.webdriver.lib.Component;
import org.safs.tools.stringutils.StringUtilities;

/**
 * This provides a uniformed Item object to represent the item<br>
 * within a container such as TabControl or ListView etc.<br>
 * It represents originally sap.ui.core.Item<br>
 *
 */
public class Item extends Element{

	public static final int INVALID_INDEX = -1;

	protected int index = INVALID_INDEX;
	protected String value = null;

	protected Item(){}

	/**
	 * Constructor used to create an uniformed Item object. User may override this one to parse their own object.
	 * @param object Object, the item object. It may be a Map returned from javascript function; It maybe a WebElement.
	 *
	 */
	public Item(Object object){
		initialize(object);
	}

	/**
	 * set/update the class's fields through the underlying WebElement or AbstractMap.
	 */
	public void updateFields(){
		super.updateFields();

		if(map!=null){
			value = getAttribute(PROPERTY_VALUE);
			try{ index = Integer.decode(getAttribute(PROPERTY_INDEX));} catch(Exception e){}

		}else if(webelement!=null){
			value = getAttribute(Component.ATTRIBUTE_VALUE);
			try{ index = Integer.parseInt(getAttribute(Component.ATTRIBUTE_INDEX)); }catch(Exception e){}
		}

		//Trim the value if it contains leading/ending space
		if(value!=null) value = StringUtilities.TWhitespace(value);
	}

	public int getIndex(){
		return index;
	}

	public String getValue(){
		return value;
	}

	/**
	 * @return String, the content of this Item (label, or value).
	 */
	public String contentValue(){
		if(super.contentValue()==null) return value;
		return super.contentValue();
	}

	public String toString(){
		return "id="+id+"; label="+label+" index="+index;
	}
}
