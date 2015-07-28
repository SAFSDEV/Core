/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.rational;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.Tree;

import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.object.interfaces.TestObject;

/**
 * <br><em>Purpose:</em> 	Process DotNetMenuBar component. 
 * @author  Lei Wang
 * @since   JUL 24, 2008
 *   <br>	JUL 24, 2008	(LeiWang)	Original Release
 *   <br>	DEC 10, 2008	(LeiWang)	Add constant CLASS_TOOLSTRIPDROPDOWNMENU_NAME
 *   									Modify method isMenuBar(), isPopupMenu(): return true when class is
 *   									'System.Windows.Forms.ToolStripDropDownMenu'
 **/

public class CFDotNetMenuBar extends CFMenuBar {
	//Standard class name
	public static final String CLASS_MAINMENU_NAME 					= "System.Windows.Forms.MainMenu";
	public static final String CLASS_CONTEXTMENU_NAME 				= "System.Windows.Forms.ContextMenu";
	public static final String CLASS_MENUITEM_NAME 					= "System.Windows.Forms.MenuItem";
	public static final String CLASS_MENUSTRIP_NAME 				= "System.Windows.Forms.MenuStrip";
	public static final String CLASS_CONTEXTMENUSTRIP_NAME 			= "System.Windows.Forms.ContextMenuStrip";
	public static final String CLASS_TOOLSTRIPMENUITEM_NAME 		= "System.Windows.Forms.ToolStripMenuItem";
	public static final String CLASS_TOOLSTRIPSEPARATOR_NAME 		= "System.Windows.Forms.ToolStripSeparator";
	public static final String CLASS_TOOLSTRIPDROPDOWNMENU_NAME		= "System.Windows.Forms.ToolStripDropDownMenu";

	//For Collection inside the standard menu class
	public static final String PROPERTY_COUNT        				= "Count";
	public static final String METHOD_GET_ITEM						= "get_Item";
	//For most menu and menuitem class
	public static final String PROPERTY_TEXT		  				= "Text";
	//For System.Windows.Forms.Menu
	public static final String PROPERTY_MENU_ITEMS        			= "MenuItems";
	//For System.Windows.Forms.MenuStrip
	public static final String PROPERTY_TOOLSTRIP_ITEMS        		= "Items";
	public static final String PROPERTY_TOOLSTRIPDROPDOWN_ITEMS    	= "DropDownItems";
	
	/**
	* <em>Note:</em> This method is used by the RDDGUIUtilities.java
	*/
	public static Tree staticExtractMenuItems(Object obj, int level)
			throws SAFSException {
		return new CFDotNetMenuBar().extractMenuItems(obj, level);
	}
	
	/**
	 * <em>Note:</em> This is a static method, used by extractMenuItems()
	 * 
	 * @param aMenuObj --
	 *            Typically a MenuItem proxy. Will be cast to TestObject.
	 * @return Integer -- the number of child Menus or MenuItems or 0.
	 */
	protected Integer getSubMenuItemCount(TestObject aMenuObj) {
		String msg = CFDotNetMenuBar.class.getName()+".getSubMenuItemCount() ";
		Integer val = new Integer(0);

		try {
			TestObject subitems = null;
			try{
				subitems = (TestObject) aMenuObj.getProperty(PROPERTY_TOOLSTRIPDROPDOWN_ITEMS);
			}catch(PropertyNotFoundException e1){}
			
			try{
				if(subitems==null) subitems = (TestObject) aMenuObj.getProperty(PROPERTY_TOOLSTRIP_ITEMS);
			}catch(PropertyNotFoundException e2) {}
			
			try{
				if(subitems==null) subitems = (TestObject) aMenuObj.getProperty(PROPERTY_MENU_ITEMS);
			}catch(PropertyNotFoundException e2) {}
			
			
			if(subitems!=null){
				val = (Integer) subitems.getProperty(PROPERTY_COUNT);
			}else{
				Log.debug(msg+" can not get subitems for "+aMenuObj.getProperty(PROPERTY_TEXT));
			}
		} catch (Exception ex) {
			Log.debug(msg+ex.getMessage());
		}
		return val;
	}
	
	/**
	 * <em>Note:</em>		Override that of its superclass. In .NET, 
	 * 						the text property of menuitem is represented by string "Text".
	 */
	protected String getPropertyText(TestObject testObject){
		return (String) testObject.getProperty(PROPERTY_TEXT);
	}
	
	protected String getPropertyTextName(){
		return PROPERTY_TEXT;
	}
	
	protected boolean isMenuBar(TestObject menuObject) throws SAFSException{
		boolean isMenuBar = false;
		TestObject clazz = DotNetUtil.getClazz(menuObject);
		
		isMenuBar = DotNetUtil.isSubclassOf(clazz, CLASS_CONTEXTMENU_NAME) || 
					DotNetUtil.isSubclassOf(clazz, CLASS_CONTEXTMENUSTRIP_NAME) ||
					DotNetUtil.isSubclassOf(clazz, CLASS_TOOLSTRIPDROPDOWNMENU_NAME) ||
					DotNetUtil.isSubclassOf(clazz, CLASS_MAINMENU_NAME) ||
					DotNetUtil.isSubclassOf(clazz, CLASS_MENUSTRIP_NAME);
		
		return isMenuBar;
	}
	
	protected boolean isMenuItem(TestObject menuObject) throws SAFSException{
		boolean isMenuItem = false;
		TestObject clazz = DotNetUtil.getClazz(menuObject);

		isMenuItem = DotNetUtil.isSubclassOf(clazz, CLASS_MENUITEM_NAME) || 
					 DotNetUtil.isSubclassOf(clazz, CLASS_TOOLSTRIPMENUITEM_NAME);
		
		return isMenuItem;
	}
	
	protected boolean isPopupMenu(TestObject menuObject) throws SAFSException{
		boolean isPopupMenu = false;
		TestObject clazz = DotNetUtil.getClazz(menuObject);

		isPopupMenu = DotNetUtil.isSubclassOf(clazz, CLASS_CONTEXTMENU_NAME) || 
					  DotNetUtil.isSubclassOf(clazz, CLASS_CONTEXTMENUSTRIP_NAME) ||
					  DotNetUtil.isSubclassOf(clazz, CLASS_TOOLSTRIPDROPDOWNMENU_NAME);
		
		return isPopupMenu;
	}
	
	protected MenuTreeNode getNewTreeNode(Object userObject,int siblingCounter,int childrenCounter){
		return new DotNetMenuTreeNode(userObject,siblingCounter,childrenCounter);
	}

}
