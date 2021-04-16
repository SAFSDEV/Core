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
package org.safs.jvmagent;

import org.safs.TestRecordData;

/**
 * 
 * @author  Carl Nagle
 * @since   FEB 16, 2005
 *
 * Feb 17, 2006 (Szucs) extending the prototypes of the methods getMatchingPathObject( ) and isMatchingPath( )
 *                      with Exception to unify them among the relevant interfaces
 **/
public interface LocalAgent {

	/**
	 * Return the number of children available in the provided parent.
	 */
    public int getChildCount(Object parent);
    
	/**
	 * Return an array representing the children of the provided parent object.
     * 
     * @param parent An object from getTopLevelWindows or from a previous call to getChildren.
	 * The parent is often one of the elements of the TopLevelWindow array or somewhere 
	 * lower in that same hierarchy.
	 * 
     * @return Object[] representing all known children of the provided parent.
     * This array will be Server/Agent specific and may be nothing more than arrays of the 
     * hashcodes used to uniquely identify objects in an Agent-maintained Hashtable.
     * A zero-length array will be returned if the parent has no children.
	 */
    public Object[] getChildren(Object parent);
    
    /**
     * Retrieve the Caption of the object if one exits.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String the caption of the object.
     */
    public String getCaption(Object object);

    /**
     * Retrieve the name of the object if the object is named.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String the name of the object.
     */
    public String getName(Object object);

    /**
     * Retrieve the ID of the object if the object has an ID.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String the ID of the object.
     */
    public String getID(Object object);

    /**
     * Retrieve the displayed text value of the object if the object has a text value.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * 
	 * @return String the text value of the object.  Since text values can theoretically 
	 * legally be zero-length, a null value will be returned if no value exists for the 
	 * object.
     */
    public String getText(Object object);
    
    /**
     * Retrieve the list of available properties for the object.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * 
	 * @return String[] the names of available properties.
     */
    public String[] getPropertyNames(Object object);
    
    /**
     * Retrieve the property value of the object if the object has the property.
     * 
     * @param object -- An object from getTopLevelWindows or from a previous call to getChildren.
	 * @param property -- the case-sensitive name of the property to seek.
	 * 
	 * @return String the text value of the object property.  Since property values can theoretically 
	 * legally be zero-length, a null value will be returned if no value exists for the 
	 * object.
     */
    public String getProperty(Object object, String property) throws NoSuchPropertyException;
    
    /**
     * Return the Class name of the object.  
     * For example, we may get "javax.swing.JFrame" or the name of the subclass if 
     * it is a subclass of JFrame.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String Class name of the object.
     */
    public String getClassName(Object object);

    /**
     * Return the Z-Order level of the object (generally for a top level window).  
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return int z-order value of the object among all objects if this can be determined.
	 *          0 normally indicates the topmost Window.  
	 *          1 is normally the Window behind that, etc..
     */
    public int getLevel(Object object);

    /**
     * Return true if the object is showing/visible.  
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return boolean true if the object is showing/visible.
     */
    public boolean isShowing(Object object);

    /**
     * Return true if the object is still valid/finadable in the JVM.  
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return boolean true if the object is still valid/findable.
     */
    public boolean isValid(Object object);

    /**
     * Return the array of all superclass names for the object.  
     * This should return the Class hierarchy for the object all the way to Class Object. 
     * A 0-length array will be returned if there are none (Class Object).
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String[] Class names for the superclass hierarchy.
     */
    public String[] getSuperClassNames(Object object);

	/**
	 * Mechanism to retrieve a subitem/object identified 
	 * by the provided Path.  Path is hierarchical information showing parent->child 
	 * relationships separated by '->'.  This is often used in Menus and Trees.
	 * <p>
	 * Ex:
	 * <p>
	 *     File->Exit<br/>
	 *     Root->Branch->Leaf
	 * 
	 * @param theObject--Object proxy for the object to be evaluated.
	 * 
	 * @param thePath information to locate another object or subitem relative to theObject.
	 *        this is usually something like a menuitem or tree node where supported.
	 * 
	 * @return Object child sub-object found relative to theObject
	 **/
	public Object getMatchingPathObject (Object theObject, String thePath) throws Exception;


	/**
	 * Mechanism to determine if the object contains a subitem/object identified 
	 * by the provided Path.  Path is hierarchical information showing parent->child 
	 * relationships separated by '->'.  This is often used in Menus and Trees.
	 * <p>
	 * Ex:
	 * <p>
	 *     File->Exit<br/>
	 *     Root->Branch->Leaf
	 * 
	 * @param theObject--Object proxy for the object to be evaluated.
	 * 
	 * @param thePath information to locate another object or subitem relative to theObject.
	 *        this is usually something like a menuitem or tree node where supported.
	 * 
	 * @return true if the child sub-object was found relative to theObject.
	 **/
	public boolean isMatchingPath	(Object theObject, String thePath) throws Exception;

    
    
    /**
     * Return whatever data is extractable (normally visible) from the object.
     * Some objects like Lists may only require 1D of the 2D array.  Some objects like 
     * Tables will use the full 2D array.  Objects like Trees may use special techniques of 
     * storing their multi-dimensional data in the 2D array.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
     * 
     * @param dataInfo Allows us to specify what type of data we want returned.  This will be 
     * specific to the types of objects from which we extract data.  For example, Tables might 
     * provide "Contents" or "Headers" or other different types of data.  This dataInfo is 
     * usually provided as a String value.
     * 
     * @return String[][] 2D array of extracted data.  0-length arrays if no data is available 
     * or the ability to extract data is not supported.
     */
    public String[][] getStringData(Object object, Object dataInfo);	

	/**
	 * Process the action provided in the testRecordData.  
	 * An Agent may throw various types of Agent-specific Exceptions depending upon failure modes.
	 * @param testRecordData provides all the information needed by the Agent to perform the action.
	 * @return TestRecordData with necessary info, especially the modified statuscode.
	 */
    public TestRecordData process(Object object, TestRecordData testRecordData);
    
}
