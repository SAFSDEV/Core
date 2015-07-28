/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.rational.wpf;

import org.safs.Log;
import org.safs.SAFSException;
import org.safs.rational.DotNetUtil;
import org.safs.rational.MenuTreeNode;

import com.rational.test.ft.MethodNotFoundException;
import com.rational.test.ft.object.interfaces.TestObject;

/**
 * <br><em>Purpose:</em> 	Handle the status of .NET_WPF MenuItem 
 * <p>
 * @author  Lei	Wang
 * @since   Sep 28, 2009
 * 
 **/
public class WPFMenuTreeNode extends MenuTreeNode {
	//userObject inherited from super class:
	//For .NET_WPF Application:
	//It may be a proxy object for "System.Windows.Controls.MenuItem"
	//"MenuItem" has properties: "IsCheckable", "IsChecked", "IsEnabled"
	public static final String PROPERTY_ENABLED 	= "IsEnabled";
	public static final String PROPERTY_SELECTED 	= "IsChecked";
	//public static final String PROPERTY_ICON	 	= "Image";
	
	
	public WPFMenuTreeNode() {
		super();
	}
	public WPFMenuTreeNode(Object userObject) {
		this.userObject = userObject;
	}

	public WPFMenuTreeNode(Object userObject,int siblingCounter,int childrenCounter) {
		this(userObject);
		this.siblingCounter = siblingCounter;
		this.childrenCounter = childrenCounter;
	}
	
	/**
	 * @return			A String which describes this node. For example, the text "File" on the menu.
	 */
	public String getNodeLabel(){
		String label = null;
		Object property = getProperty(CFWPFMenuBar.PROPERTY_HEADER);
		
		if(property!=null){
			label = CFWPFMenuBar.removeFisrtChar(property.toString(), CFWPFMenuBar.SHORT_CUT_KEY_UNDERSCORE);
		}else{
			if(isSeparator()){
				label = "Separator";
			}else{
				label = userObject.toString();
			}
		}
		return label;
	}
	
	
	protected boolean containsBitMap() {
		boolean containsBitMap = false;
		//TODO 
		//<Image></Image>
		
		return containsBitMap;
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
			isSeparator = DotNetUtil.isSubclassOf(clazz, CFWPFMenuBar.CLASS_SEPARATOR_NAME);
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
