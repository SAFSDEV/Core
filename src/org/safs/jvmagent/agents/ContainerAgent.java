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

import javax.accessibility.AccessibleContext;

import org.safs.jvmagent.NoSuchPropertyException;
import org.safs.jvmagent.SAFSActionUnsupportedRuntimeException;

/**
 * @author Carl Nagle
 * <br/>MAY 26, 2005	(Carl Nagle) Standardized property names
 */
public class ContainerAgent extends ComponentAgent {

	/** 
	 * "Container"  (Subclasses will override)
	 * The generic object type supported by this Agent helper class.  
	 * The generic object type is that returned by GuiClassData.getGenericObjectType.  
	 * Example:
	 *    Component
	 *    Button
	 *    Table
	 *    etc..
	 * @see org.safs.GuiClassData#getGenericObjectType(String)
	 */
	public static final String objectType = "Container";
	
	/**
	 * Constructor for ContainerAgent.
	 */
	public ContainerAgent() {
		super();
		
		this.setAlternateAncestorClassname(org.safs.jvmagent.agents.ComponentAgent.class.getName());
	}

	/**
	 * @throws SAFSActionUnsupportedRuntimeException("ChildCount Unsupported");
	 * @see org.safs.jvmagent.LocalAgent#getChildCount(Object)
	 */
	public int getChildCount(Object object) {
		if (!(object instanceof java.awt.Container)) 
			throw new SAFSActionUnsupportedRuntimeException("ChildCount Unsupported");
		Container comp = (Container) object;
		return comp.getComponentCount();
	}

	/**
	 * @throws SAFSActionUnsupportedRuntimeException("Children Unsupported");
	 * @see org.safs.jvmagent.LocalAgent#getChildren(Object)
	 */
	public Object[] getChildren(Object object) {
		if (!(object instanceof java.awt.Container)) 
			throw new SAFSActionUnsupportedRuntimeException("Children Unsupported");
		Container comp = (Container) object;
		return comp.getComponents();
	}

}
