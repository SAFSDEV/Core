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

import org.safs.Log;
import javax.swing.JList;


/**
 * 
 * @author Carl Nagle
 * @since Mar 4, 2005
 *
 * Feb 26, 2006 (Szucs) adding the getSubItemAtIndex( ) method
 */
public class JListAgent extends JChildlessAgent {

	/** 
	 * "JList"  (Subclasses will override)
	 * The generic object type supported by this Agent helper class.  
	 * The generic object type is that returned by GuiClassData.getGenericObjectType.  
	 * Example:
	 *    Component
	 *    Button
	 *    Table
	 *    etc..
	 * @see org.safs.GuiClassData#getGenericObjectType(String)
	 */
	public static final String objectType = "JList";
	

	/**
	 * Constructor for JComboBoxAgent.
	 */
	public JListAgent() {
		super();
		
		this.setAlternateAncestorClassname(org.safs.jvmagent.agents.JComponentAgent.class.getName());
	}
        
	public Object getSubItemAtIndex( Object object, int index ) throws Exception {
            JList list = ( JList )object;
            int size = list.getModel( ).getSize( );
            if ( ( index < 0 ) || ( index >= size ) ) {
                throw new Exception( "JList item index out of range!" );
            }
            
            return list.getModel( ).getElementAt( index );
        }

}
