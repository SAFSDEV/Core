/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.jvmagent.agents;

import org.safs.Log;
import javax.swing.JList;


/**
 * 
 * @author canagl
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
