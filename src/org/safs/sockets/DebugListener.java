/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.sockets;

/**
 * A NamedListener that wishes to sink/log debug messages from remote sources.
 * @author canagl
 */
public interface DebugListener extends NamedListener {

    /**
     * A remote client has sent a Debug message to be logged by the listener.
     * @param message
     */
    public void onReceiveDebug(String message);
    
}
