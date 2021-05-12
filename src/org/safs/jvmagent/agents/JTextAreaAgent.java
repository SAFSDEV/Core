/**
 * Copyright (C) Continental Teves Hungary Ltd., All rights reserved.
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

/**
 * 
 * @author Szucs
 * @since Feb 22, 2006
 */
public class JTextAreaAgent extends JChildlessAgent {
    
    public static final String objectType = "JTextArea";
    
    
    /** Creates a new instance of JTextAreaAgent */
    public JTextAreaAgent( ) {
        super( );
	setAlternateAncestorClassname( org.safs.jvmagent.agents.JComponentAgent.class.getName( ) );        
    }
    
}
