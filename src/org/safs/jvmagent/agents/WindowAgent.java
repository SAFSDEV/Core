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
package org.safs.jvmagent.agents;

import java.awt.*;

import org.safs.jvmagent.NoSuchPropertyException;

/**
 * @author Carl Nagle
 * <br/>MAY 26, 2005	(Carl Nagle) Standardized property names
 */
public class WindowAgent extends ContainerAgent {

	/** 
	 * "Window"  (Subclasses will override)
	 * The generic object type supported by this Agent helper class.  
	 * The generic object type is that returned by GuiClassData.getGenericObjectType.  
	 * Example:
	 *    Component
	 *    Button
	 *    Table
	 *    etc..
	 * @see org.safs.GuiClassData#getGenericObjectType(String)
	 */
	public static final String objectType = "Window";
	
	/**
	 * Constructor for WindowAgent.
	 */
	public WindowAgent() {
		super();
		
		this.setAlternateAncestorClassname(org.safs.jvmagent.agents.ContainerAgent.class.getName());
	}

	/**
	 * @see org.safs.jvmagent.LocalAgent#getChildCount(Object)
	 */
	public int getChildCount(Object object) {
		if (!(object instanceof java.awt.Window)) return 0;
		Window comp = (Window) object;
		int children = comp.getComponentCount();
		children += comp.getOwnedWindows().length;
		return children;
	}

	/**
	 * @see org.safs.jvmagent.LocalAgent#getChildren(Object)
	 */
	public Object[] getChildren(Object object) {
		if (!(object instanceof java.awt.Window)) return new Object[0];
		Window comp = (Window) object;
		Component[] child = comp.getComponents();
		int children = child.length;
		Window[] window = comp.getOwnedWindows();
		Object[] arrChild = new Object[children += window.length];
		for (int i=0;i<children;i++) arrChild[i] = child[i];
		for (int i=0;i<window.length;i++)	arrChild[i+children] = window[i];
		return arrChild;
	}

}
