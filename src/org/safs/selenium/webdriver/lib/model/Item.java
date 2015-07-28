/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.model;

import org.safs.selenium.webdriver.lib.Component;

/**
 * This provides a uniformed Item object to represent the item<br> 
 * within a container such as TabControl or ListView etc.<br>
 * It represents originally sap.ui.core.Item<br>
 * 
 * History:<br>
 * 
 *  <br>   Apr 24, 2014    (sbjlwa) Initial release.
 */
public class Item extends Element{
	
	public static final int INVALID_INDEX = -1;
	
	protected int index = INVALID_INDEX;
	protected String value = null;
	
	/**
	 * Constructor used to create an uniformed Item object. User may override this one to parse their own object.
	 * @param object Object, the item object. It may be a Map returned from javascript function; It maybe a WebElement.
	 * 
	 */
	public Item(Object object){
		super(object);
		//We need to call updateFields() again even it has been called in super constructor!!!
		//otherwise the local fileds will be initialized to default value. This is the nature of Java Language.
		updateFields();
	}
	
	/**
	 * set/update the class's fields through the underlying WebElement or AbstractMap.
	 */
	public void updateFields(){
		super.updateFields();

		if(map!=null){
			value = getAttribute(PROPERTY_VALUE);
//			try { index = StringUtilities.getInteger(map, PROPERTY_INDEX); } catch (Exception e) {}
			try{ index = Integer.decode(getAttribute(PROPERTY_INDEX));} catch(Exception e){}

		}else if(webelement!=null){
			selected = webelement.isSelected();
			value = getAttribute(Component.ATTRIBUTE_VALUE);
			try{ index = Integer.parseInt(getAttribute(Component.ATTRIBUTE_INDEX)); }catch(Exception e){}
			
		}
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
