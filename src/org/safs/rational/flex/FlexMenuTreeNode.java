/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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
