/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.jvmagent.agents;

import java.awt.*;

import org.safs.jvmagent.NoSuchPropertyException;

/**
 * @author canagl
 * <br/>MAY 26, 2005	(CANAGL) Standardized property names
 */
public class FrameAgent extends WindowAgent {

	/** 
	 * "Frame"  (Subclasses will override)
	 * The generic object type supported by this Agent helper class.  
	 * The generic object type is that returned by GuiClassData.getGenericObjectType.  
	 * Example:
	 *    Component
	 *    Button
	 *    Table
	 *    etc..
	 * @see org.safs.GuiClassData#getGenericObjectType(String)
	 */
	public static final String objectType = "Frame";
	
	/**
	 * Constructor for FrameAgent.
	 */
	public FrameAgent() {
		super();
		
		this.setAlternateAncestorClassname(org.safs.jvmagent.agents.WindowAgent.class.getName());
	}

}
