/**
 ** Copyright (C) Continental Teves Hungary Ltd., All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.jvmagent.agents;

/**
 * 
 * @author Szucs
 * @since Feb 21, 2006
 */
public class JCheckBoxAgent extends JChildlessAgent {
    
    public static final String objectType = "JCheckBox";
        
        
    /** Creates a new instance of JCheckBoxAgent */
    public JCheckBoxAgent() {
        super( );		
        setAlternateAncestorClassname( org.safs.jvmagent.agents.JComponentAgent.class.getName( ) );        
    }
    
}
