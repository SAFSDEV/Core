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
package org.safs.rational.flex;

import org.safs.rational.*;

/**
 * <br><em>Purpose:</em> Hold a Flex MenuItem.
 * @author  Junwu Ma
 * @since   JAN 12, 2008
 **/

public class FlexMenuTreeNode extends MenuTreeNode {
	// Support MenuItemRenderer (mx.controls.menuClasses.MenuItemRenderer).
	// property 'enabled' of MenuItemRenderer returns blank in RFT8.0. don't know why? using 'mouseEnabled' instead.
	// how to get the status of flex menu items? This issue will be resolved with new RFT build released. 
	public static final String PROPERTY_ENABLED 	= "mouseEnabled"; //
	public static final String PROPERTY_SELECTED 	= "currentState"; // ?
	//public static final String PROPERTY_ICON	 	= "Image";
	
	
	public FlexMenuTreeNode() {
		super();
	}
	public FlexMenuTreeNode(Object userObject) {
		this.userObject = userObject;
	}

	public FlexMenuTreeNode(Object userObject,int siblingCounter,int childrenCounter) {
		this(userObject);
		this.siblingCounter = siblingCounter;
		this.childrenCounter = childrenCounter;
	}
	
	/**
	 * @return			A String which describes this node. For example, the text "File" on the menu.
	 */
	public String getNodeLabel(){
		String label = null;
		Object property = getProperty(FlexUtil.PROPERTY_TYPE_AUTOMATIONNAME);
		
		if(property!=null){
			label = property.toString();
		}else{
			label = userObject.toString();
		}
		return label;
	}
	
	
	protected boolean containsBitMap() {
		//Object property = getProperty(PROPERTY_ICON);
		//		return property!=null;
		return false;
	}

	
	protected boolean isChecked() {
		
		return false;
	}

	
	protected boolean isEnabled() {
		boolean enabled = false;
		Object property = getProperty(PROPERTY_ENABLED);
		
		if(property!=null){
			enabled = Boolean.parseBoolean((String) property);
		}
		return enabled;
	}
	
	protected boolean isSeparator() {
		return false;
	}

}
