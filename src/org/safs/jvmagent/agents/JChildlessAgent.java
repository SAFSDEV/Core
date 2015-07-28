/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.jvmagent.agents;

import java.util.Enumeration;
import javax.accessibility.*;
import javax.swing.*;
import javax.swing.table.TableColumn;

/**
 * @author canagl
 */
public class JChildlessAgent extends JComponentAgent {

	/**
	 * Constructor for JChildlessAgent.
	 */
	public JChildlessAgent() {
		super();
		
		this.setAlternateAncestorClassname(org.safs.jvmagent.agents.JComponentAgent.class.getName());
	}

	/**
	 * @see org.safs.jvmagent.LocalAgent#getChildCount(Object)
	 */
	public int getChildCount(Object object) { return 0;}

	/**
	 * @see org.safs.jvmagent.LocalAgent#getChildren(Object)
	 */
	public Object[] getChildren(Object object) { return new Object[0];}

}
