/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rational;

import java.util.StringTokenizer;

import org.safs.Log;

import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.object.interfaces.TestObject;

/**
 * <br><em>Purpose:</em> Generic tree node object for handle the status of MenuItem
 *     
 * @author  Lei Wang
 * @since   APR 15, 2008
 *   <br>	APR 15, 2008	(LeiWang)	Original Release
 **/

public abstract class MenuTreeNode{

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
	
	//userObject contains the real object that this node contains
	protected Object userObject;
	protected int siblingCounter;
	protected int childrenCounter;

	public MenuTreeNode(){
		userObject = null;
		siblingCounter = 0;
		childrenCounter = 0;
	}
	
	public Object getUserObject() {
		return userObject;
	}
	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}
	
	/**
	 * <br><b>Note:</b>	To be overrided by it's subclass
	 * @return			A String which describe this node. For example, the text "File" on the menu.
	 */
	public String getNodeLabel(){
		return userObject.toString();
	}
	
	/**
	 * @return	boolean, Whether the menuItem is enabled
	 */
	abstract protected boolean isEnabled();
	
	/**
	 * @return	boolean, Whether this menuItem is selected
	 */
	abstract protected boolean isChecked();
	
	/**
	 * @return boolean, Whether this menuItem contains an Icon
	 */
	abstract protected boolean containsBitMap();
	
	/**
	 * @return boolean, Whether this is a Separator
	 */
	abstract protected boolean isSeparator();
	
	/**
	 * <b>Note:</b>		The separator must be a blank " "
	 * 
	 *  @param status	A String contains status of the node item, can be an combination of followings:
	 * 					<br>Supported: 	"Enabled" "Ungrayed" "Grayed" "Disabled" "Checked" "Unchecked" "Menu With N MenuItems" "BitMap" "Separator"
	 * 					<br>Future:		"BarBreak" "Break" "Hilited" "Default" "Unhilited" "Normal" 
	 * 					<br><b>Example:</b>"Enabled Unchecked"
	 * @return			If matched, return true
	 */
	public boolean matchStatus(String status){
		boolean matched = true;
		
		if(status==null) return matched;
		StringTokenizer tokens = new StringTokenizer(status," ");
		while(tokens.hasMoreTokens()){
			String token = tokens.nextToken();
			if(token.equalsIgnoreCase(STATUS_ENABLED)){
				matched = matched && isEnabled();
			}else if(token.equalsIgnoreCase(STATUS_UNGRAYED)){
				matched = matched && isEnabled();
			}else if(token.equalsIgnoreCase(STATUS_DISABLED)){
				matched = matched && !isEnabled();
			}else if(token.equalsIgnoreCase(STATUS_GRAYED)){
				matched = matched && !isEnabled();
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
	 * @param property, String describe a property of the object that test proxy object represent
	 * @return	object, Object represent a property value of the object that test proxy object represent
	 */
	protected Object getProperty(String property){
		String debugmsg = getClass().getName()+".getProperty() ";
		Object object = null;
		TestObject testObj = null;
		
		try{
			testObj = (TestObject) userObject;
			object = testObj.getProperty(property);
		}catch(ClassCastException e){
			Log.debug(debugmsg+" Can not cast "+userObject+" to TestObject!!! ");
		}catch(PropertyNotFoundException e){
			Log.debug(debugmsg+" property "+property+" not found for "+testObj.getObjectClassName());
		}
		
		return object;
	}
	
	/**
	 * @param subMenuNumber, String, Represent the number of submenus under this menu
	 * @return	boolean
	 */
	protected boolean subMenuCountOk(String subMenuNumber){
		String debugmsg = getClass().getName()+".subMenuCountOk() ";
		int subMenus = 0;
		try{
			subMenus = new Integer(subMenuNumber).intValue();
		}catch(NumberFormatException e){
			Log.debug(debugmsg+"Number format error for "+subMenuNumber);
			return false;
		}
		return childrenCounter==subMenus;
	}
	
	protected String getStatusString(){
		//TODO add other status
		StringBuffer status = new StringBuffer();
		status.append(isEnabled()? STATUS_ENABLED+" ":STATUS_DISABLED+" " );
		status.append(isChecked()? STATUS_CHECKED+" ":STATUS_UNCHECKED+" " );
		
		return status.toString();
	}
	
	public String toString(){
		return userObject.toString();
	}
}
