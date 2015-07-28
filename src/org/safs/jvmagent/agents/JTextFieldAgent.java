/**
 ** Copyright (C) Continental Teves Hungary Ltd., All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.jvmagent.agents;

/**
 * 
 * @author Szucs
 * @since Feb 22, 2006
 */
public class JTextFieldAgent extends JChildlessAgent {
    
    public static final String objectType = "JTextField";
        
        
    /** Creates a new instance of JTextFieldAgent */
    public JTextFieldAgent( ) {
        super( );
	setAlternateAncestorClassname( org.safs.jvmagent.agents.JComponentAgent.class.getName( ) );
    }
    
}
