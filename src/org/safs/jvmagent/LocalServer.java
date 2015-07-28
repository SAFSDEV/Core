/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.jvmagent;

/**
 * 
 * @author  CANAGL
 * @since   FEB 09, 2005
 **/
public interface LocalServer extends LocalAgent {

	/**
	 * Return the number of currently active Top Level Windows from 
	 * all known Agents.
	 */
    public int getTopLevelCount();

    /**
     * Return an array representing the TopLevel windows from all known Agents.
     * 
     * @return Object[] representing all active top level windows in all JVMs.
     * This array will be Server/Agent specific and may be nothing more than arrays of the 
     * hashcodes used to uniquely identify objects in an Agent-maintained Hashtable.  
     * A zero-length array will be returned if no Top Level Windows are active.
     */
    public Object[] getTopLevelWindows();

    /**
     * Attempts to set anObject as the active (topmost?) Window or Component in the JVM.
     * @param anObject Object of a type expected by the implementation for this method.
     */
    public void setActiveWindow(Object anObject);

}
