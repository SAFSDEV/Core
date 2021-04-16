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

import org.safs.Log;
import org.safs.SAFSException;

import com.rational.test.ft.MethodNotFoundException;
import com.rational.test.ft.object.interfaces.TestObject;

/**
 * <br><em>Purpose:</em> Handle the status of DotNet MenuItem
 *     
 * @author  Lei Wang
 * @since   JUL 28, 2008
 *   <br>	JUL 28, 2008	(Lei Wang)	Original Release
 **/

public class DotNetMenuTreeNode extends MenuTreeNode {
	//userObject inherited from super class:
	//For .NET Application:
	//It may be a proxy object for "System.Windows.Forms.MenuItem" or "System.Windows.Forms.ToolStripMenuItem"
	//"MenuItem" has properties: "Enabled"
	//"ToolStripMenuItem" has properties: "Enabled", "Selected", "Image"
	public static final String PROPERTY_ENABLED 	= "Enabled";
	public static final String PROPERTY_SELECTED 	= "Checked";
	public static final String PROPERTY_ICON	 	= "Image";
	
	
	public DotNetMenuTreeNode() {
		super();
	}
	public DotNetMenuTreeNode(Object userObject) {
		this.userObject = userObject;
	}

	public DotNetMenuTreeNode(Object userObject,int siblingCounter,int childrenCounter) {
		this(userObject);
		this.siblingCounter = siblingCounter;
		this.childrenCounter = childrenCounter;
	}
	
	/**
	 * @return			A String which describes this node. For example, the text "File" on the menu.
	 */
	public String getNodeLabel(){
		String label = null;
		Object property = getProperty(CFDotNetMenuBar.PROPERTY_TEXT);
		
		if(property!=null){
			label = property.toString();
		}else{
			label = userObject.toString();
		}
		return label;
	}
	
	
	protected boolean containsBitMap() {
		Object property = getProperty(PROPERTY_ICON);
		
		return property!=null;
	}

	
	protected boolean isChecked() {
		boolean checked = false;
		Object property = getProperty(PROPERTY_SELECTED);
		
		if(property!=null){
			checked = ((Boolean) property).booleanValue();
		}
		return checked;
	}

	
	protected boolean isEnabled() {
		boolean enabled = false;
		Object property = getProperty(PROPERTY_ENABLED);
		
		if(property!=null){
			enabled = ((Boolean) property).booleanValue();
		}
		return enabled;
	}

	
	/**
	 * <em>Note:</em>		Only works for ToolStripSeparator and its subclass
	 */
	protected boolean isSeparator() {
		String debugmsg = getClass().getName()+".isSeparator() ";
		TestObject testObj = null;
		boolean isSeparator = false;
		
		try{
			testObj = (TestObject) userObject;
			Log.info(debugmsg+testObj.getObjectClassName());
			TestObject clazz = (TestObject) testObj.invoke(DotNetUtil.METHOD_GET_TYPE);
			isSeparator = DotNetUtil.isSubclassOf(clazz, CFDotNetMenuBar.CLASS_TOOLSTRIPSEPARATOR_NAME);
		}catch(ClassCastException e){
			Log.debug(debugmsg+" Can not cast "+userObject+" to TestObject!!! ");
		}catch(SAFSException se){
			Log.debug(debugmsg+se.getMessage());
		}catch(MethodNotFoundException e){
			Log.debug(debugmsg+"Method "+DotNetUtil.METHOD_GET_TYPE+" not found for "+testObj.getObjectClassName());
		}
		
		return isSeparator;
	}

}
