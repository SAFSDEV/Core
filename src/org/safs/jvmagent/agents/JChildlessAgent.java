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

import java.util.Enumeration;
import javax.accessibility.*;
import javax.swing.*;
import javax.swing.table.TableColumn;

/**
 * @author Carl Nagle
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
