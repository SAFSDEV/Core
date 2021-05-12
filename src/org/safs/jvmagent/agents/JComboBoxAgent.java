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

import javax.accessibility.*;
import javax.swing.*;

import org.safs.jvmagent.NoSuchPropertyException;


/**
 * 
 * @author Carl Nagle
 * @since Mar 4, 2005
 *
 * Feb 22, 2006 (Szucs) adding the getSubItemAtIndex( ) method
 */
public class JComboBoxAgent extends JChildlessAgent {

	/** 
	 * "JComboBox"  (Subclasses will override)
	 * The generic object type supported by this Agent helper class.  
	 * The generic object type is that returned by GuiClassData.getGenericObjectType.  
	 * Example:
	 *    Component
	 *    Button
	 *    Table
	 *    etc..
	 * @see org.safs.GuiClassData#getGenericObjectType(String)
	 */
	public static final String objectType = "JComboBox";
	

	/**
	 * Constructor for JComboBoxAgent.
	 */
	public JComboBoxAgent() {
		super();
		
		this.setAlternateAncestorClassname(org.safs.jvmagent.agents.JComponentAgent.class.getName());
	}

        
	public Object getSubItemAtIndex( Object object, int index ) throws Exception {
            JComboBox box = ( JComboBox )object;
            if ( ( index  < 0 ) || ( index >= box.getItemCount( ) ) ) {
                throw new Exception( "JComboBox item index is out of range" );
            } else {
                return box.getItemAt( index );
            }
        }                
}
