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
package org.safs.rmi.engine;

import java.rmi.*;
import java.rmi.server.*;

/**
 * Defines the interface necessary to extract items out of referenced objects by 
 * some qualifying factor like index or text.  This interface represents the remote agent side 
 * of the implementation and is intended to match the server SubItemsServer interface.
 * 
 * @author Carl Nagle
 * @since Apr 7, 2005
 * @see org.safs.jvmagent.SubItemsServer
 */
public interface SubItemsAgent extends Remote {

	/**
	 * Return the subitem at the specified index from the given object.
	 * This may be a ComboBox item, a List item, or a Tree node, etc...
	 * The returned item may be a component object or perhaps a String representing the 
	 * text of the item.  The return type is object specific.
	 * @param object reference from which to locate the subitem.
	 * @param index of the subitem to retrieve.
	 * @return subitem object or String
	 * @throws an Exception if the subitem index is invalid or subitem is unobtainable.
	 */
	public Object getSubItemAtIndex(Object object, int index) throws RemoteException, Exception;

	/**
	 * Return the subitem at the specified string path from the given object.
	 * This is a hierarchical path of parent->child relationships separated by "->".
	 * The returned item may be a component object or perhaps a String representing the 
	 * text of the item.  The return type is object specific.
	 * @param child
	 * @param path to desired subitem using item->subitem->subitem format.
	 * @return subitem object or String
	 */
	public Object getMatchingPathObject(Object object, String path) throws RemoteException, Exception;

	/**
	 * Determine if the object contains a subitem/object identified 
	 * by the provided Path.  Path is hierarchical information showing parent->child 
	 * relationships separated by '->'.  This is often used in Menus and Trees.
	 * <p>
	 * Ex:
	 * <p>
	 *     File->Exit<br/>
	 *     Root->Branch->Leaf
	 * 
	 * @param object--Object proxy for the object to be evaluated.
	 * 
	 * @param path information to locate another object or subitem relative to object.
	 *        this is usually something like a menuitem or tree node where supported.
	 * 
	 * @return true if the child sub-object was found relative to object.
	 **/
	public boolean isMatchingPath	(Object object, String path) throws RemoteException, Exception;
}
