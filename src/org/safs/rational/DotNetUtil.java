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

import java.util.Hashtable;

import org.safs.Log;
import org.safs.SAFSException;

import com.rational.test.ft.MethodNotFoundException;
import com.rational.test.ft.PropertyNotFoundException;
import com.rational.test.ft.object.interfaces.TestObject;

/**
 * <br><em>Purpose:</em> 	Provide general method for .NET application
 * @author  Lei Wang
 * @since   JUL 28, 2008
 *   <br>	JUL 28, 2008	(Lei Wang)	Original Release
 *   <br>	SEP 03, 2008	(Lei Wang)	Add method getSuperClazz(),getClazzFullName()
 *   <br>	DEC 03, 2008	(Lei Wang)	Add constant METHOD_TOOLSTRIPITEM_PERFORMACLICK, CLASS_TOOLSTRIPITEM_NAME
 *   <br>	DEC 09, 2008	(Lei Wang)	Add constant METHOD_TOOLSTRIPITEM_ONCLICK, METHOD_TOOLSTRIPITEM_SELECT
 *   									Modify method getMatchingPathTestObject(): modify to get the first matched test object from a tree.
 *   <br>	DEC 10, 2008	(Lei Wang)	Add constants CLASS_COMBOBOX_NAME, CLASS_TOOLSTRIPCOMBOBOX_NAME, CLASS_TOOLSTRIPCOMBOBOXANDCONTROL_NAME
 *   									RFT can not process "System.Windows.Forms.ToolStripComboBox", but it can process
 *   									"System.Windows.Forms.ToolStripComboBox+ToolStripComboBoxControl"
 **/

public class DotNetUtil {
	
	public static final String CLASS_OBJECT_NAME 					= "System.Object";
	public static final String METHOD_GET_TYPE						= "GetType";
	public static final String METHOD_TOOLSTRIPITEM_ONCLICK			= "OnClick";
	public static final String METHOD_TOOLSTRIPITEM_SELECT			= "Select";
	public static final String METHOD_TOOLSTRIPITEM_PERFORMACLICK	= "PerformClick";
	//Property of standard class
	//For System.Type
	public static final String PROPERTY_TYPE_FULLNAME 				= "FullName";
	public static final String PROPERTY_TYPE_BASETYPE 				= "BaseType";
	public static final String PROPERTY_TYPE_TEXT	 				= "Text";
	
	public static final String CLASS_TOOLBARBUTTON_NAME				= "System.Windows.Forms.ToolBarButton";
	public static final String CLASS_TOOLSTRIPITEM_NAME				= "System.Windows.Forms.ToolStripItem";
	public static final String CLASS_COMBOBOX_NAME					= "System.Windows.Forms.ComboBox";
	public static final String CLASS_TOOLSTRIPCOMBOBOX_NAME			= "System.Windows.Forms.ToolStripComboBox";
	public static final String CLASS_TOOLSTRIPCOMBOBOXANDCONTROL_NAME		= "System.Windows.Forms.ToolStripComboBox+ToolStripComboBoxControl";
	public static final String CLASS_LISTBOX_NAME					= "System.Windows.Forms.ListBox";
	public static final String CLASS_LISTVIEW_NAME					= "System.Windows.Forms.ListView";
	public static final String CLASS_TEXTBOXBASE_NAME				= "System.Windows.Forms.TextBoxBase";
	
	public static final String CLASS_TEXTBLOCK_NAME					= "System.Windows.Controls.TextBlock";
	
	/**
	 * @param clazz			A TestObject represents an object System.Type
	 * @param className		A fullname of a dotnet class
	 * @return 				True if parameter clazz is the same class or subclass described
	 *         				by parameter classname; False otherwise.
	 */
	public static boolean isSubclassOf(TestObject clazz,String className) throws SAFSException{
		boolean isSubclass = false;
		String debugmsg = DotNetUtil.class.getName()+".isSubclassOf() ";
		
		if(clazz==null || className==null) return false;
		//All classes are subclass of System.Object in .NET
		if(CLASS_OBJECT_NAME.equalsIgnoreCase(className)) return true;
		String fullName = null;
		try{
			fullName = (String) clazz.getProperty(PROPERTY_TYPE_FULLNAME);
		}catch(PropertyNotFoundException e){
			Log.debug(debugmsg+"Property "+PROPERTY_TYPE_FULLNAME+" is not found.");
			throw new SAFSException("Property "+PROPERTY_TYPE_FULLNAME+" is not found.");
		}
		//If clazz is the same class described by className, return true.
		if(className.equalsIgnoreCase(fullName)){
			isSubclass = true;
		//Else, we need to see if parent class of clazz is the same calss described by className
		}else{
			//if the fullName is "System.Object", there is not parent class.
			//We treate only the situation that fullName is not "System.Object"
			if (!fullName.equalsIgnoreCase(CLASS_OBJECT_NAME)) {
				try{
					isSubclass = isSubclassOf((TestObject)clazz.getProperty(PROPERTY_TYPE_BASETYPE),className);
				}catch(PropertyNotFoundException e){
					Log.debug(debugmsg+"Property "+PROPERTY_TYPE_BASETYPE+" is not found.");
					throw new SAFSException("Property "+PROPERTY_TYPE_BASETYPE+" is not found.");
				}
			}
		}

		return isSubclass;
	}
	
	/**
	 * @param testObject		Represent a proxy TestObject for a .NET object
	 * @return					Return a proxy TestObject for the System.Type of parameter testObject
	 * @throws SAFSException
	 */
	public static TestObject getClazz(TestObject testObject) throws SAFSException{
		String debugmsg = DotNetUtil.class.getName()+".getClazz() ";
		TestObject clazz = null;
		
		try{
			clazz = (TestObject) testObject.invoke(METHOD_GET_TYPE);	
		}catch(MethodNotFoundException e){
			Log.debug(debugmsg+"Method "+METHOD_GET_TYPE+" not found for "+testObject.getObjectClassName());
			throw new SAFSException("Method "+METHOD_GET_TYPE+" not found for "+testObject.getObjectClassName());
		}
		
		return clazz;
	}
	
	/**
	 * @param clazz				Represent a proxy TestObject for a .NET 'System.Type' object
	 * @return					A super class of type 'System.Type' of the parameter clazz
	 * @throws SAFSException
	 */
	public static TestObject getSuperClazz(TestObject clazz) throws SAFSException{
		String debugmsg = DotNetUtil.class.getName()+".getSuperClazz() ";
		TestObject superclazz = null;
		
		try{
			superclazz = (TestObject)clazz.getProperty(PROPERTY_TYPE_BASETYPE);
		}catch(PropertyNotFoundException e){
			Log.debug(debugmsg+"Property "+PROPERTY_TYPE_BASETYPE+" is not found.");
			throw new SAFSException("Property "+PROPERTY_TYPE_BASETYPE+" is not found.");
		}
		
		return superclazz;
	}
	
	/**
	 * @param clazz				Represent a proxy TestObject for a .NET 'System.Type' object				
	 * @return					The full name of the parameter clazz
	 * @throws SAFSException
	 */
	public static String  getClazzFullName(TestObject clazz) throws SAFSException{
		String debugmsg = DotNetUtil.class.getName()+".getClazzFullName() ";
		String fullName = null;
		
		try{
			fullName = (String) clazz.getProperty(PROPERTY_TYPE_FULLNAME);
		}catch(PropertyNotFoundException e){
			Log.debug(debugmsg+"Property "+PROPERTY_TYPE_FULLNAME+" is not found.");
			throw new SAFSException("Property "+PROPERTY_TYPE_FULLNAME+" is not found.");
		}
		
		return fullName;
	}
	
	public static TestObject getMatchingPathTestObject(RGuiObjectRecognition recognition, TestObject testObject, String path, String pathSeparator){
		String debugmsg = DotNetUtil.class.getName()+".getMatchingPathTestObject() ";
		TestObject matchedTestObject = null;
		
		if(testObject==null || path==null){
			Log.debug(debugmsg+"testObject or path is null. Can not get object matching path.");
			return null;
		}
		
		
		int separatorIndex = path.indexOf(pathSeparator);
		boolean isLastElementInPath = (separatorIndex==-1);
		String currentElement = "";
		String nextPath = "";
		if(!isLastElementInPath){
			currentElement = path.substring(0,separatorIndex);
			if(pathSeparator.length()+separatorIndex<path.length()){
				nextPath = path.substring(pathSeparator.length()+separatorIndex);
			}
		}else{
			currentElement = path;
		}
		
		TestObject[] children = testObject.getChildren();
		String childName = "";
		for(int i=0;i<children.length;i++){
			childName = recognition.getText(children[i]);
			if(currentElement.equals(childName)){
				if(isLastElementInPath){
					matchedTestObject = children[i];
					break;
				}else{
					matchedTestObject = getMatchingPathTestObject(recognition, children[i], nextPath, pathSeparator);
					if(matchedTestObject!=null)	break;
				}
			}
		}
		
		return matchedTestObject;
	}
	
	/**
	 * <em>Note:</em>		Print the heritage tree for the special object
	 * @param clazz
	 */
	public static void printHierarchy(TestObject testObject){
		String debugmsg = DotNetUtil.class.getName()+".isSubclassOf() ";
		
		try{
			TestObject clazz = (TestObject) testObject.invoke(METHOD_GET_TYPE);
			String fullname = (String) clazz.getProperty(PROPERTY_TYPE_FULLNAME);
			if (!fullname.equalsIgnoreCase(CLASS_OBJECT_NAME)) {
				System.out.println(fullname);
				printHierarchy((TestObject) clazz.getProperty(PROPERTY_TYPE_BASETYPE));
			} else {
				System.out.println(fullname);
			}
		}catch(PropertyNotFoundException e1){
			System.out.println(debugmsg+e1.getMessage());
		}catch(MethodNotFoundException e2){
			System.out.println(debugmsg+e2.getMessage());
		}
	}
	
	/**
	 * <em>Note:</em>	This method will try to get all properties of a test object
	 *                  and get the value of the Text property directly
	 * @param obj
	 * @return
	 */
	public static String getText(TestObject obj){
		String text = "";
		Hashtable hs = obj.getProperties();
		if(hs!=null){
			text= (String) hs.get(PROPERTY_TYPE_TEXT);
			if(text==null) text ="";
		}
		Log.debug("We get Text property: "+text);
		
		return text;
	}
}
