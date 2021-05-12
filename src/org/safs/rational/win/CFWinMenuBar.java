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
package org.safs.rational.win;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.Tree;
import org.safs.rational.CFMenuBar;
import org.safs.rational.MenuTree;
import org.safs.rational.MenuTreeNode;

import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.script.CaptionText;

/**
 * <br><em>Purpose:</em> 	Process CFWinMenuBar component. 
 * @author  Lei Wang
 * @since   JUL 24, 2008
 *   <br>	JUL 24, 2008	(Lei Wang)	Original Release
 *   <br>	JUN 12, 2009	(Lei Wang)	Add method: getPropertyText(), getSubMenuItemCount(), getNewTreeNode()
 *   												extractMenuItems(), staticExtractMenuItems()
 **/

public class CFWinMenuBar extends CFMenuBar {
	//Standard class name
	public static final String CLASS_POPUPMENU_NAME 				= ".Menupopup";
	public static final String CLASS_MENUBAR_NAME 					= ".Menubar";
	public static final String CLASS_MENUITEM_NAME 					= ".Menuitem";
	public static final String CLASS_SEPARATOR_NAME 				= ".Separator";
	
	
	public static final String PROPERTY_NUM_CHILDREN	 			= ".numChildren";
	public static final String PROPERTY_NUM_SUBMENUITEM 			= ".numSubMenuitem";
	public static final String PROPERTY_NAME	 					= ".name";
	public static final String PROPERTY_TEXT	 					= ".text";
	
	
	protected boolean isMenuBar(TestObject menuObject) throws SAFSException{
		boolean isMenuBar = false;
		String objectClassName = menuObject.getObjectClassName();
		
		isMenuBar = CLASS_POPUPMENU_NAME.equalsIgnoreCase(objectClassName) ||
					CLASS_MENUBAR_NAME.equalsIgnoreCase(objectClassName);
		
		return isMenuBar;
	}
	
	protected boolean isMenuItem(TestObject menuObject) throws SAFSException{
		boolean isMenuItem = false;
		String objectClassName = menuObject.getObjectClassName();

		isMenuItem = CLASS_MENUITEM_NAME.equalsIgnoreCase(objectClassName);
		
		return isMenuItem;
	}
	
	protected boolean isPopupMenu(TestObject menuObject) throws SAFSException{
		boolean isPopupMenu = false;
		String objectClassName = menuObject.getObjectClassName();

		isPopupMenu = CLASS_POPUPMENU_NAME.equalsIgnoreCase(objectClassName);
		
		return isPopupMenu;
	}
	
	
	/**
	 * <em>Note:</em>		Override that of its superclass. In Win domain, 
	 * 						the text property of menuitem is represented by string ".text" or ".name".
	 */
	protected String getPropertyText(TestObject testObject){
		String debugmsg = getClass().getName()+".getPropertyText(): ";
		Object text = null;
		String result = "";
		
		try{
			text = testObject.getProperty(PROPERTY_TEXT);
		}catch(PropertyNotFoundException e){
			Log.debug(debugmsg+" property "+PROPERTY_TEXT+" does not exist.");
			Log.debug(debugmsg+" try to get property "+PROPERTY_NAME);
			try{
				text = testObject.getProperty(PROPERTY_NAME);
			}catch(PropertyNotFoundException e1){
				Log.debug(debugmsg+" property "+PROPERTY_NAME+" does not exist.");
			}
		}

		Log.debug(debugmsg+" text is "+text+". Its type is "+result.getClass().getName());
		if(text!=null){
			if(text instanceof String){
				result = (String) text;
			}else if(text instanceof CaptionText){
				result = ((CaptionText) text).getCaption();
			}else{
				Log.debug(debugmsg+" text is of type : "+result.getClass().getName());
				result = text.toString();
			}
		}
		
		return result;
	}
	
	/**
	 * Return the number of immediate child (.Menuitem or .Menupopup) from the provided .Menubar or .Menupopup. or .Menuitem
	 * Note: 	If a .Menuitem has submenu, then it has only one child of type .Menupopup
	 * 		the child of type .Menupopup will has .Menuitem as its children, which are
	 * 		the items that we see from the submenu.
	 * 
	 * The following tree show the structure of .Menubar	
	 * 	.Menubar
	 *     |____________________________________
	 *          |               |               |
	 *     .Menuitem        .Menuitem       .Menuitem
	 *          |               |               |
	 *     .Menupopup       .Menupopup      .Menupopup
	 *      ____|____
	 *     |         |
	 *  .Menuitem  .Menuitem
	 * 
	 * @param aMenuObj -- Typically a .Menubar or .Menupopup or .Menuitem proxy.  Will be cast to TestObject.
	 * @return Integer -- the number of child (.Menuitem) 
	 **/
	protected Integer getSubMenuItemCount(TestObject aMenuObj) {
		String debugmsg = getClass().getName() + ".getPropertyText(): ";
		Integer val = new Integer(0);
		TestObject anObj = null;
		String classname = "";

		try {
			anObj = (TestObject) aMenuObj;
			classname = anObj.getObjectClassName();
			Log.debug(debugmsg + classname);

			try {
				val = (Integer) anObj.getProperty(PROPERTY_NUM_CHILDREN);
				Log.debug(debugmsg + " val is " + val);
				// If the classname is '.Menuitem', if it has submenu, its child
				// will be '.Menupopup', to get the real children count, we should 
				//return child count of the '.Menupopup'
				if (val != 0 && CLASS_MENUITEM_NAME.equalsIgnoreCase(classname)) {
					Object[] objects = anObj.getMappableChildren();
					if (objects != null) {
						// The first child should be type of '.Menupopup'
						anObj = (TestObject) objects[0];
						Log.debug(debugmsg+" anObj class name is "+anObj.getObjectClassName());
						val = (Integer) anObj.getProperty(PROPERTY_NUM_CHILDREN);
						Log.debug(debugmsg + " val is " + val);
					} else {
						Log.debug(debugmsg + " can not get children with method getMappableChildren()");
						val = new Integer(0);
					}
				}

			} catch (PropertyNotFoundException pnf) {
				Log.debug(debugmsg + " can not get property "+ PROPERTY_NUM_CHILDREN);
			}
		} catch (Exception ex) {
		}

		return val;
	}
	
	protected MenuTreeNode getNewTreeNode(Object userObject,int siblingCounter,int childrenCounter){
		return new WinMenuTreeNode(userObject,siblingCounter,childrenCounter);
	}
	
	/** <br><em>Purpose:</em> 		Extract a menu hierarchy from a TestObject;
	 * 								The item is for .Menubar, .Menuitem, .Menupopup.
	 * 								This routine is reentrant until there are no more submenus to process.
	 * <br><em>Assumptions:</em>  	obj is a .Menubar, .Menuitem, .Menupopup TestObject proxy.
	 * <br><em>Note:</em>  			Override the method of its superclass CFMenuBar
	 * 
	 * The following tree show the structure of .Menubar	
	 * 	.Menubar
	 *     |____________________________________
	 *          |               |               |
	 *     .Menuitem        .Menuitem       .Menuitem
	 *          |               |               |
	 *     .Menupopup       .Menupopup      .Menupopup
	 *      ____|____
	 *     |         |
	 *  .Menuitem  .Menuitem
	 * 
	 * @param                     	obj, Object (TestObject)
	 * @param                     	level, what level in the tree are we processing
	 * @return                    	org.safs.Tree, the real instance is org.safs.rational.MenuTree
	 * @exception                 	SAFSException
	 **/
	protected Tree extractMenuItems(Object obj, int level) throws SAFSException {

		String debugmsg = getClass().getName() + ".extractMenuItems() ";
		Tree tree = null;
		TestObject[] subitems = null;

		try {
			TestObject tobj = (TestObject) obj;
			Integer itemCount = null;

			// The method getMappableChildren() will not return the object of type '.Separator'
			subitems = tobj.getMappableChildren();
			try {
				itemCount = new Integer(subitems.length);
				//We should skip the object of type .Menuitem, if it has submenu, its direct children
				//will be .Menupopup
				if(CFWinMenuBar.CLASS_MENUITEM_NAME.equalsIgnoreCase(tobj.getObjectClassName())){
					//If .Menuitem has child, it must be .Menupopup
					if(itemCount>0){
						if (subitems != null) {
							// The first child should be type of '.Menupopup'
							tobj = (TestObject) subitems[0];
							Log.debug(debugmsg+" anObj class name is "+tobj.getObjectClassName());
							subitems = tobj.getMappableChildren();
							try {
								itemCount = new Integer(subitems.length);
							}catch(Exception e){
								itemCount = new Integer(0);
							}
						} else {
							Log.debug(debugmsg + " can not get children with method getMappableChildren()");
							itemCount = new Integer(0);
						}
					}
				}
			} catch (Exception x) {
				itemCount = new Integer(0);
			}

			Tree lastjTree = null;

			for (int j = 0; j < itemCount.intValue(); j++) {
				TestObject gto2 = null;
				try {
					gto2 = subitems[j];
				} catch (ArrayIndexOutOfBoundsException aie) {
					Log.debug("ArrayIndexOutOfBoundsException for level: "
									+ level
									+ ", menuitem: "
									+ j
									+ ", probably your menu has a separator or some other unknown object, continuing...");
					continue;
				}

				String text2 = getPropertyText(gto2);

				// do NOT increment level for what appears to be a SubMenu
				// placeholder
				int inc = (text2 == null) ? 0 : 1;

				Integer itemCount2 = getSubMenuItemCount(gto2);
				if (itemCount2.intValue() == 0) {
					TestObject[] subkids = gto2.getMappableChildren();
					if (subkids != null)
						itemCount2 = new Integer(subkids.length);
				}

				Log.debug("level " + level + ": item " + j + ": "
						+ getPropertyTextName() + " \"" + text2 + "\" "
						+ " children: " + itemCount2);

				// Use test object to form a tree node
				Tree jtree = new MenuTree();
				MenuTreeNode treeNode = getNewTreeNode(gto2, itemCount.intValue(), itemCount2.intValue());
				jtree.setUserObject(treeNode);

				if (j == 0)
					tree = jtree;
				else {
					lastjTree.setNextSibling(jtree);
				}

				jtree.setLevel(new Integer(level));
				jtree.setSiblingCount(itemCount);
				jtree.setChildCount(itemCount2);
				if (itemCount2.intValue() > 0) {
					// inc only when a valid new level exists
					Tree subtree = extractMenuItems(gto2, level + inc);
					jtree.setFirstChild(subtree);
				}
				lastjTree = jtree;
			}
		} catch (Exception ee) {
			ee.printStackTrace();
			throw new SAFSException(debugmsg + ": " + ee.getMessage());
		}
		return tree;
	}
	
	  /**
	   * <em>Note:</em>			This method is used by the RDDGUIUtilities.java
	   */
	  public static Tree staticExtractMenuItems (Object obj, int level) throws SAFSException {
		  return new CFWinMenuBar().extractMenuItems(obj, level);
	  }
}
