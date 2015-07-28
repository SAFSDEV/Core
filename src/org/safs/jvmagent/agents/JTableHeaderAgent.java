/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.jvmagent.agents;

/**
 * 
 * @author canagl
 * @since Mar 4, 2005
 */
public class JTableHeaderAgent extends JChildlessAgent {

	/** 
	 * "JTableHeader"  (Subclasses will override)
	 * The generic object type supported by this Agent helper class.  
	 * The generic object type is that returned by GuiClassData.getGenericObjectType.  
	 * Example:
	 *    Component
	 *    Button
	 *    Table
	 *    etc..
	 * @see org.safs.GuiClassData#getGenericObjectType(String)
	 */
	public static final String objectType = "JTableHeader";
	
	/**
	 * Constructor for Agent.
	 */
	public JTableHeaderAgent() {
		super();
		
		this.setAlternateAncestorClassname(org.safs.jvmagent.agents.JComponentAgent.class.getName());
	}

}
