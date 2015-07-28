/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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
