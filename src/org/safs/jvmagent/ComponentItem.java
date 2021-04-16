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

import java.util.*;
import java.awt.Component;
import java.awt.Container;

/**
 * Represents a single component.
 * The CompID is an Integer of the component's hashCode.
 * The CompParents is the ArrayList of Integer hashCodes of the parent 
 * hierarchy path to the component.  ArrayList item 0 would be the Integer 
 * hashCode to the TopLevel Window.  The last Integer hashCode in the 
 * ArrayList is the target component itself -- the same as the CompID.
 * @author Carl Nagle
 */
public class ComponentItem {
	private Integer compID;
	private ArrayList compParents;
	/**
	 * Construct a new ComponentItem from an existing Integer hashcode and parent tree.
	 */
	public ComponentItem(Integer id, ArrayList parents){
		compID = id;
		compParents = addChild(parents, id);
	}
	/**
	 * Construct a new ComponentItem from a child Component and existing parent tree.
	 */
	public ComponentItem(Component child, ArrayList parents){
		compID = new Integer(child.hashCode());
		compParents = addChild(parents, compID);
	}
	/**
	 * Construct a new TopLevel ComponentItem (typically) with a Container.
	 */
	public ComponentItem(Component parent){
		compID = new Integer(parent.hashCode());
		compParents = new ArrayList(1);
		compParents.add(compID);
	}
	
	protected ArrayList addChild(ArrayList parents, Integer child){
		ArrayList childpath = new ArrayList(parents.size()+1);
		for(int i=0;i<parents.size();i++) childpath.add(parents.get(i));
		childpath.add(child);
		return childpath;		
	}
	
	/**
	 * Return the stored CompID (Integer hashcode).
	 */
	public Integer getCompID(){ return compID; }
	/**
	 * Return the stored CompParents (ArrayList Integer hashcode hierarchy).
	 */
	public ArrayList getCompParents(){ return compParents; }
}

