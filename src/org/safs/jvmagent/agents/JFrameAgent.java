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
import java.util.*;
import javax.swing.JFrame;

import org.safs.jvmagent.NoSuchPropertyException;


/**
 * @author Carl Nagle
 */
public class JFrameAgent extends FrameAgent {

	/** 
	 * "JFrame"  (Subclasses will override)
	 * The generic object type supported by this Agent helper class.  
	 * The generic object type is that returned by GuiClassData.getGenericObjectType.  
	 * Example:
	 *    Component
	 *    Button
	 *    Table
	 *    etc..
	 * @see org.safs.GuiClassData#getGenericObjectType(String)
	 */
	public static final String objectType = "JFrame";
	
	/**
	 * Constructor for JFrameAgent.
	 */
	public JFrameAgent() {
		super();
		
		this.setAlternateAncestorClassname(org.safs.jvmagent.agents.FrameAgent.class.getName());
	}

	/**
	 * @see org.safs.jvmagent.LocalAgent#getChildCount(Object)
	 */
	public int getChildCount(Object object) {
		return getChildren(object).length;
	}

	/**
	 * Some returned children might be null.
	 * @see org.safs.jvmagent.LocalAgent#getChildren(Object)
	 */
	public Object[] getChildren(Object object) {
		if(!(object instanceof JFrame)) return super.getChildren(object);
		JFrame frame = (JFrame) object;
		
		//Object[] children = super.getChildren(object);
		Object[] children = new Object[0];
		
		Object[] morechildren = new Object[]{
			frame.getRootPane()
		};
		ArrayList allkids = new ArrayList(children.length + morechildren.length);
		for(int i=0;i<children.length;i++) allkids.add(children[i]);
		Object child = null;
		for(int i=0;i<morechildren.length;i++) {
			child = morechildren[i];
			//if(child instanceof Component) System.out.println("FrameChild("+i+"):"+child.getClass().getName());
			if(child instanceof Component) allkids.add(child);
		}
		allkids.trimToSize();
		return (Object[]) allkids.toArray(new Object[0]);
	}

}
