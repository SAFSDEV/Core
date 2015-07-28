/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.jvmagent.agents;

import javax.accessibility.*;
import javax.swing.*;

import org.safs.jvmagent.NoSuchPropertyException;

/**
 * @author canagl
 */
public class JComponentAgent extends ContainerAgent {

	/** 
	 * "JComponent"  (Subclasses will override)
	 * The generic object type supported by this Agent helper class.  
	 * The generic object type is that returned by GuiClassData.getGenericObjectType.  
	 * Example:
	 *    Component
	 *    Button
	 *    Table
	 *    etc..
	 * @see org.safs.GuiClassData#getGenericObjectType(String)
	 */
	public static final String objectType = "JComponent";
	
	/**
	 * Constructor for JComponentAgent.
	 */
	public JComponentAgent() {
		super();
		
		this.setAlternateAncestorClassname(org.safs.jvmagent.agents.ContainerAgent.class.getName());
	}

}
