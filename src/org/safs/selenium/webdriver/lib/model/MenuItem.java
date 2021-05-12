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
package org.safs.selenium.webdriver.lib.model;

import java.util.StringTokenizer;

import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.RS;
import org.safs.selenium.webdriver.lib.SearchObject;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;

/**
 *
 * History:<br>
 *
 *  <br>   Jun 23, 2014    (Lei Wang) Initial release.
 */
public class MenuItem extends HierarchicalElement {

	/**'submenuid' used to find the submenu WebElement of this menuitem*/
	public static final String PROPERTY_SUBMENUID 			= "submenuid";

	//========================  Status Constants =============================================
	public static final String STATUS_ENABLED	 	= "Enabled";
	public static final String STATUS_UNGRAYED	 	= "Ungrayed";
	public static final String STATUS_DISABLED	 	= "Disabled";
	public static final String STATUS_GRAYED	 	= "Grayed";
	public static final String STATUS_CHECKED	 	= "Checked";
	public static final String STATUS_UNCHECKED	 	= "UnChecked";
	public static final String STATUS_SUBMENU_NUM 	= "Menu";
	public static final String STATUS_BITMAP	 	= "Bitmap";
	public static final String STATUS_SEPARATOR 	= "Separator";
	//Future: "BarBreak" "Break" "Hilited" "Default" "Unhilited" "Normal"
	public static final String STATUS_BARBREAK	 	= "BarBreak";
	public static final String STATUS_BREAK		 	= "Break";
	public static final String STATUS_HILITED	 	= "Hilited";
	public static final String STATUS_UNHILITEED 	= "Unhilited";
	public static final String STATUS_DEFAULT	 	= "Default";
	public static final String STATUS_NORMAL	 	= "Normal";
	//========================  Status Constants =============================================

	protected String subMenuId = null;

	protected MenuItem(){}
	public MenuItem(Object object){ initialize(object); }

	/**
	 * set/update the class's fields through the underlying WebElement or Map.
	 */
	public void updateFields(){
		super.updateFields();

		if(map!=null){
			subMenuId = getAttribute(PROPERTY_SUBMENUID);
			iconURL = getAttribute(PROPERTY_ICON);

		}else if(webelement!=null){
			//
		}
	}

	protected MenuItem newInstance(Object object){
		return new MenuItem(object);
	}
	protected MenuItem[] newArray(int length){
		return new MenuItem[length];
	}

	public MenuItem getParent(){
		String debugmsg = StringUtils.debugmsg(getClass(), "getParent");
		if(parent!=null && (parent instanceof MenuItem)) return (MenuItem) parent;
		else{
			IndependantLog.error(debugmsg+"The parent should be "+getClass().getSimpleName()+".");
			return null;
		}
	}

	public MenuItem[] getChildren() {
		String debugmsg = StringUtils.debugmsg(getClass(), "getChildren");
		if(children==null) return null;
		else if(children instanceof MenuItem[]) return (MenuItem[])children;
		else{
			IndependantLog.error(debugmsg+"The children should be "+getClass().getSimpleName()+"[].");
			return null;
		}
	}

	/**
	 * @param children TreeNode[], an array of TreeNode
	 */
	public void setChildren(HierarchicalElement[] children) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(getClass(), "setChildren");
		if(children instanceof MenuItem[]){
			this.children = children;
		}else{
			String message = "Only "+getClass().getSimpleName()+"[] is accepted as parameter.";
			IndependantLog.error(debugmsg+message);
			throw new SeleniumPlusException(message);
		}
	}

	public void setSubMenuId(String subMenuId){
		this.subMenuId = subMenuId;
	}
	public String getSubMenuId(){
		return subMenuId;
	}
	public boolean hasSubMenu(){
		boolean hasSubMenu = (subMenuId!=null);
		if(!hasSubMenu){
			hasSubMenu = (children!=null && children.length>0);
		}
		return hasSubMenu;
	}

	/**
	 * Make it private, some Menu structure may don't have this so called 'sub-menu' Object.<br>
	 * For the traditional Menu, if a MenuItem has sub-items, the MenuItem will contain a SubMenu
	 * object, and the SubMenu object will contain the sub MenuItems.<br>
	 * But for other Menu definition, if a MenuItem has sub-items, the MenuItem will contain directly
	 * sub-items (the MenuItem object) without that intermediate SubMenu object.<br>
	 * @return
	 * @deprecated as we have {@link #getChildren()}
	 */
	private WebElement getSubMenu(){
		String debugmsg = StringUtils.debugmsg(getClass(), "getSubMenu");
		WebElement submenu = null;

		if(hasSubMenu()){
			String rs = RS.id(getSubMenuId());
			//Search under the context of WebElement of MenuItem
			if(getWebElement()!=null) submenu = SearchObject.getObject(getWebElement(), rs);
			//Search under the context of WebDriver (the whole application)
			if(submenu==null) submenu = SearchObject.getObject(rs);
			if(submenu==null) IndependantLog.debug(debugmsg+"can NOT get SubMenu by RS '"+rs+"'");
		}else{
			IndependantLog.debug(debugmsg+"This MenuItem does NOT have a SubMenu.");
		}

		return submenu;
	}

	/**
	 * <b>Note:</b>		The separator must be a blank " "
	 *
	 *  @param expectedStatus	A String contains status of the node item, can be an combination of followings:
	 * 					<br>Supported: 	"Enabled" "Ungrayed" "Grayed" "Disabled" "Checked" "Unchecked" "Menu With N MenuItems" "BitMap" "Separator"
	 * 					<br>Future:		"BarBreak" "Break" "Hilited" "Default" "Unhilited" "Normal"
	 * 					<br><b>Example:</b>"Enabled Unchecked"
	 * @return			If matched, return true
	 */
	public boolean matchStatus(String expectedStatus){
		boolean matched = true;

		if(expectedStatus==null) return matched;
		StringTokenizer tokens = new StringTokenizer(expectedStatus," ");
		while(tokens.hasMoreTokens()){
			String token = tokens.nextToken();
			if(token.equalsIgnoreCase(STATUS_ENABLED)){
				matched = matched && !isDisabled();
			}else if(token.equalsIgnoreCase(STATUS_UNGRAYED)){
				matched = matched && !isDisabled();
			}else if(token.equalsIgnoreCase(STATUS_DISABLED)){
				matched = matched && isDisabled();
			}else if(token.equalsIgnoreCase(STATUS_GRAYED)){
				matched = matched && isDisabled();
			}else if(token.equalsIgnoreCase(STATUS_CHECKED)){
				matched = matched && isChecked();
			}else if(token.equalsIgnoreCase(STATUS_UNCHECKED)){
				matched = matched && !isChecked();
			}else if(token.equalsIgnoreCase(STATUS_SUBMENU_NUM)){
				//get the third parameter
				tokens.nextToken();//skip "With"
				matched = matched && subMenuCountOk(tokens.nextToken());
				tokens.nextToken();//skip "MenuItems"
			}else if(token.equalsIgnoreCase(STATUS_BITMAP)){
				matched = matched && containsBitMap();
			}else if(token.equalsIgnoreCase(STATUS_SEPARATOR)){
				matched = matched && isSeparator();
			}else if(token.equalsIgnoreCase(STATUS_DEFAULT) ||
					 token.equalsIgnoreCase(STATUS_NORMAL)){
				//TODO
			}else if(token.equalsIgnoreCase(STATUS_BARBREAK)){
				//TODO
			}else if(token.equalsIgnoreCase(STATUS_BREAK)){
				//TODO
			}else if(token.equalsIgnoreCase(STATUS_HILITED)){
				//TODO
			}else if(token.equalsIgnoreCase(STATUS_UNHILITEED)){
				//TODO
			}
		}

		return matched;
	}

	/**
	 * Subclass may need to override this method to get a correct value.<br>
	 * @return	boolean, Whether this menuItem is selected
	 */
	protected boolean isChecked(){
		return false;
	}
	/**
	 * Subclass may need to override this method to get a correct value.<br>
	 * @return	boolean, Whether this menuItem contains an icon
	 */
	protected boolean containsBitMap(){
		return (iconURL!=null && !iconURL.isEmpty());
	}
	/**
	 * Subclass may need to override this method to get a correct value.<br>
	 * @return	boolean, Whether this menuItem is a separator
	 */
	protected boolean isSeparator(){
		return false;
	}
	/**
	 * @param subMenuNumber, String, Represent the number of submenus under this menu
	 * @return	boolean
	 */
	protected boolean subMenuCountOk(String subMenuNumber){
		String debugmsg = StringUtils.debugmsg(getClass(), "subMenuCountOk");
		int childrenCount = 0;
		try{
			childrenCount = new Integer(subMenuNumber).intValue();
			return getChildren().length == childrenCount;
		}catch(Exception e){
			IndependantLog.error(debugmsg+StringUtils.debugmsg(e));
			return false;
		}
	}
}
