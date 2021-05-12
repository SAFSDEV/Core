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
package org.safs.rational;

import java.util.StringTokenizer;

import org.safs.Log;

import com.rational.test.ft.object.interfaces.TestObject;

/**
 * <br><em>Purpose:</em> Handle the status of Java MenuItem
 *     
 * @author  Lei Wang
 * @since   APR 15, 2008
 *   <br>	APR 15, 2008	(Lei Wang)	Original Release
 **/

public class JavaMenuTreeNode extends MenuTreeNode{
	//userObject inherited from super class:
	//For Java Application:
	//It may be a proxy object for "JMenuItem" (includs "JMenu" "JCheckBoxMenuItem" "JRadioButtonMenuItem") or for "java.awt.MenuItem" 
	//Properties "enabled" "selected" "defaultIcon" work for "javax.swing.JMenuItem"
	//For java.awt.MenuItem, only "enabled" property can be used.
	public static final String PROPERTY_ENABLED 	= "enabled";
	public static final String PROPERTY_SELECTED 	= "selected";
	public static final String PROPERTY_ICON	 	= "defaultIcon";
	
	
	public JavaMenuTreeNode() {
		super();
	}
	public JavaMenuTreeNode(Object userObject) {
		this.userObject = userObject;
	}

	public JavaMenuTreeNode(Object userObject,int siblingCounter,int childrenCounter) {
		this(userObject);
		this.siblingCounter = siblingCounter;
		this.childrenCounter = childrenCounter;
	}
	
	/**
	 * @return			A String which describes this node. For example, the text "File" on the menu.
	 */
	public String getNodeLabel(){
		String label = null;
		Object property = getProperty(CFMenuBar.TEXT_PROPERTY);
		
		if(property!=null){
			label = property.toString();
		}else{
			label = userObject.toString();
		}
		return label;
	}

	/**
	 * @return	boolean, Whether the menuItem is enabled
	 */
	protected boolean isEnabled(){
		boolean enabled = false;
		Object property = getProperty(PROPERTY_ENABLED);
		
		if(property!=null){
			enabled = ((Boolean) property).booleanValue();
		}
		return enabled;
	}
	
	/**
	 * @return	boolean, Whether this menuItem is selected
	 */
	protected boolean isChecked(){
		boolean checked = false;
		Object property = getProperty(PROPERTY_SELECTED);
		
		if(property!=null){
			checked = ((Boolean) property).booleanValue();
		}
		return checked;
	}
	
	/**
	 * @return boolean, Whether this menuItem contains an Icon
	 */
	protected boolean containsBitMap() {
		Object property = getProperty(PROPERTY_ICON);
		
		return property!=null;
	}
	
	/**
	 * <em>Note:</em>	Only works for swing application for instance.	
	 * @return 			boolean, Whether this is a Separator
	 */
	protected boolean isSeparator(){
		String debugmsg = getClass().getName()+".isSeparator() ";
		TestObject testObj = null;
		boolean isSeparator = false;
		
		try{
			testObj = (TestObject) userObject;
			//TODO maybe we should call getClass method on the testObject,
			//we can then test if it is a subclass of the JSeparator
			String proxyClassName = testObj.getObjectClassName();
			Log.info(debugmsg+proxyClassName);
			isSeparator = ( proxyClassName.equalsIgnoreCase("javax.swing.JToolBar$Separator")||
							proxyClassName.equalsIgnoreCase("javax.swing.JSeparator"));
		}catch(ClassCastException e){
			Log.debug(debugmsg+" Can not cast "+userObject+" to TestObject!!! ");
		}
		
		return isSeparator;
	}
	
	public static void main(String[] args){
		StringTokenizer tokens = new StringTokenizer("Disabled Checked Menu With N MenuItems Unchecked"," ");
		while(tokens.hasMoreTokens()){
			System.out.println(tokens.nextToken());
		}
	}
}
