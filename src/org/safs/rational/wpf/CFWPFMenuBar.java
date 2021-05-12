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
package org.safs.rational.wpf;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.Tree;
import org.safs.rational.CFMenuBar;
import org.safs.rational.DotNetUtil;
import org.safs.rational.MenuTreeNode;

import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.object.interfaces.TestObject;

/**
 * <br><em>Purpose:</em> 	process a menubar component of domain .NET_WPF
 * <br><em>Lifetime:</em> 	instantiated by TestStepProcessor
 * <p>
 * @author  Lei	Wang
 * @since   Sep 27, 2009
 * 
 **/
public class CFWPFMenuBar extends CFMenuBar {
	//Standard class name
	public static final String CLASS_MENU_NAME 						= "System.Windows.Controls.Menu";
	public static final String CLASS_CONTEXTMENU_NAME 				= "System.Windows.Controls.ContextMenu";
	public static final String CLASS_MENUITEM_NAME 					= "System.Windows.Controls.MenuItem";
	public static final String CLASS_SEPARATOR_NAME 				= "System.Windows.Controls.Separator";

	//For Collection inside the standard menu class
	public static final String PROPERTY_COUNT        				= "Count";
	public static final String METHOD_GET_ITEM_AT					= "GetItemAt";
	//For menuitem class, but menu does NOT have this property
	public static final String PROPERTY_HEADER		  				= "Header";
	//For System.Windows.Controls.Menu
	public static final String PROPERTY_MENU_ITEMS        			= "Items";
	//UnderScore "_" will be used to represent the short-cut for a menu-item
	public static final String SHORT_CUT_KEY_UNDERSCORE				= "_";
	
	/**
	* <em>Note:</em> This method is used by the RDDGUIUtilities.java
	*/
	public static Tree staticExtractMenuItems(Object obj, int level)
			throws SAFSException {
		return new CFWPFMenuBar().extractMenuItems(obj, level);
	}
	
	/**
	 * <em>Note:</em> This is a static method, used by extractMenuItems()
	 * 
	 * @param aMenuObj -- Typically a MenuItem proxy. Will be cast to TestObject.
	 * @return Integer -- the number of child Menus or MenuItems or 0.
	 */
	protected Integer getSubMenuItemCount(TestObject aMenuObj) {
		String msg = this.getClass().getName()+".getSubMenuItemCount() ";
		Integer val = new Integer(0);

		try {
			TestObject subitems = null;
			try{
				subitems = (TestObject) aMenuObj.getProperty(PROPERTY_MENU_ITEMS);
			}catch(PropertyNotFoundException e1){}			
			
			if(subitems!=null){
				val = (Integer) subitems.getProperty(PROPERTY_COUNT);
			}else{
				Log.debug(msg+" can not get subitems for "+aMenuObj.getProperty(PROPERTY_HEADER));
			}
		} catch (Exception ex) {
			Log.debug(msg+ex.getMessage());
		}
		return val;
	}
	
	/**
	 * <em>Note:</em>		Override that of its superclass.
	 * 						In .NET_WPF, the text property of menuitem is represented by string "Header".
	 */
	protected String getPropertyText(TestObject testObject){
		String debugmsg = getClass().getName()+".getPropertyText(): ";
		String text = "";
		
		try{
			text = (String) testObject.getProperty(PROPERTY_HEADER);
			text = CFWPFMenuBar.removeFisrtChar(text, CFWPFMenuBar.SHORT_CUT_KEY_UNDERSCORE);
		}catch(Exception e){
			Log.debug(debugmsg+e.getMessage());
		}
		
		return text;
	}
	
	protected String getPropertyTextName(){
		return PROPERTY_HEADER;
	}
	
	protected boolean isMenuBar(TestObject menuObject) throws SAFSException{
		boolean isMenuBar = false;
		TestObject clazz = DotNetUtil.getClazz(menuObject);
		
		isMenuBar = (DotNetUtil.isSubclassOf(clazz, CLASS_CONTEXTMENU_NAME) || 
					 DotNetUtil.isSubclassOf(clazz, CLASS_MENU_NAME));
		
		return isMenuBar;
	}
	
	protected boolean isMenuItem(TestObject menuObject) throws SAFSException{
		boolean isMenuItem = false;
		TestObject clazz = DotNetUtil.getClazz(menuObject);

		isMenuItem = DotNetUtil.isSubclassOf(clazz, CLASS_MENUITEM_NAME);
		
		return isMenuItem;
	}
	
	protected boolean isPopupMenu(TestObject menuObject) throws SAFSException{
		boolean isPopupMenu = false;
		TestObject clazz = DotNetUtil.getClazz(menuObject);

		isPopupMenu = DotNetUtil.isSubclassOf(clazz, CLASS_CONTEXTMENU_NAME);
		
		return isPopupMenu;
	}
	
	protected MenuTreeNode getNewTreeNode(Object userObject,int siblingCounter,int childrenCounter){
		return new WPFMenuTreeNode(userObject,siblingCounter,childrenCounter);
	}

	public static String removeFisrtChar(String originalString, String toBeremoved){
		StringBuffer result = new StringBuffer();
		int lenght = toBeremoved.length();
		
		//Remove the first underscore character '_', the first letter following it will be shown
		//as short-cut key, and the '_' will not be shown as part of text, so we should remove it.
		int indexUnderscore = originalString.indexOf(toBeremoved);
		//If we find a '_', and it is not the last letter, we will remove it
		if(indexUnderscore>-1 && ((indexUnderscore+lenght)!=originalString.length())){
			result.append(originalString.substring(0, indexUnderscore)).append(originalString.substring(indexUnderscore+lenght));
		}
		
		return result.toString();
	}
}
