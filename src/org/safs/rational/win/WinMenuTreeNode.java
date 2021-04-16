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

import java.util.StringTokenizer;

import org.safs.Log;
import org.safs.rational.MenuTreeNode;

import com.rational.test.ft.object.interfaces.TestObject;

/**
 * <br><em>Purpose:</em> Handle the status of DotNet MenuItem
 *     
 * @author  Lei Wang
 * @since   JUN 10, 2009
 *   <br>	JUN 10, 2009	(Lei Wang)	Original Release
 **/

public class WinMenuTreeNode extends MenuTreeNode {
	//userObject inherited from super class:
	//For Win domain application
	public static final String PROPERTY_ENABLED 	= ".enabled";
	//It seems that .state can only return 0 or 1
	//it will NOT return the same value as we can see from the RFT Inspector
	public static final String PROPERTY_STATE	 	= ".state";
	
	public static final String CONSTANT_SELECTED 		= "148";
	public static final String CONSTANT_NORMAL		 	= "132";
	public static final String CONSTANT_GRAYED		 	= "133";
	
	public WinMenuTreeNode() {
		super();
	}
	public WinMenuTreeNode(Object userObject) {
		this.userObject = userObject;
	}

	public WinMenuTreeNode(Object userObject,int siblingCounter,int childrenCounter) {
		this(userObject);
		this.siblingCounter = siblingCounter;
		this.childrenCounter = childrenCounter;
	}
	
	/**
	 * Note:			It seems that the property '.text' or '.name' will return also the
	 * 					string of mnemonic something like 'New(N)	Ctrl+N', but maybe RFT will
	 * 					not recognize the mnemonic part 'Ctrl+N' when it perform a click, so we
	 * 					should remove the mnemonic part from the string of property '.text' or '.name'
	 * @return			A String which describes this node. For example, the text "File" on the menu.
	 */
	public String getNodeLabel(){
		String label = null;
		Object property = getProperty(CFWinMenuBar.PROPERTY_TEXT);
		
		if(property!=null){
			label = property.toString();
		}else{
			//Try the property '.name'
			property = getProperty(CFWinMenuBar.PROPERTY_NAME);
			if(property!=null){
				label = property.toString();
			}else{
				label = userObject.toString();
			}
		}
		
		//We should remove the mnemonic part from the label.
		//The normal label string is separated by Tab from the mnemonic string
		StringTokenizer tokens = new StringTokenizer(label,"\t");
		
		return tokens.nextToken();
	}
	
	
	protected boolean containsBitMap() {
		//Has no idea how to test this.
		return false;
	}

	
	protected boolean isChecked() {
		boolean checked = false;
		Object property = getProperty(PROPERTY_STATE);
		
		if(property!=null){
			checked = property.toString().equals(CONSTANT_SELECTED);
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
	 * <em>Note:</em>		Only works for testing .Separator
	 */
	protected boolean isSeparator() {
		String debugmsg = getClass().getName()+".isSeparator() ";
		TestObject testObj = null;
		boolean isSeparator = false;
		
		try{
			testObj = (TestObject) userObject;
			Log.info(debugmsg+testObj.getObjectClassName());
			String classname = testObj.getObjectClassName();
			//We should find a way to test the subclass of .Separator
			isSeparator = CFWinMenuBar.CLASS_SEPARATOR_NAME.equalsIgnoreCase(classname);
		}catch(ClassCastException e){
			Log.debug(debugmsg+" Can not cast "+userObject+" to TestObject!!! ");
		}
		
		return isSeparator;
	}

}
