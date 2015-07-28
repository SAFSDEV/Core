/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.sockets;

/**
 * The interface is the superclass of other Listeners used to receive notifications for 
 * the SocketProtocol.  
 * In many cases, a conforming class implements more than one Listener interface.  The classes 
 * registering the listeners typically stores all listener types in a single storage mechanism 
 * but only notifies the appropriate listeners, as needed.
 * @author canagl
 * @see DebugListener
 * @see ConnectionListener
 */
public interface NamedListener{

    /**
     * Unique name to identify the listener. 
     * @return name of listener.  Might be null.
     */
    public String getListenerName();
}
