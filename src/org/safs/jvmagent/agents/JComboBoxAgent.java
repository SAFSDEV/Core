/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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
