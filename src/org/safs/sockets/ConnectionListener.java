/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.sockets;

/**
 * A NamedListener that wishes to sink connection state information or commands from remote sources.
 * 
 * @author canagl
 */
public interface ConnectionListener extends NamedListener {

    /**
     * Called when a 2-way sockets connection has been established.
     */
    public void onReceiveConnection();

    /**
     * A local source has issued a SHUTDOWN event/command.
     * @param shutdownCause is used to indicate whether the shutdown is due to normal 
     * or abnormal circumstances.
     * @see SocketProtocol#STATUS_SHUTDOWN_NORMAL
     * @see SocketProtocol#STATUS_SHUTDOWN_REMOTE_CLIENT
     */
    public void onReceiveLocalShutdown(int shutdownCause);
    
    /**
     * A remote source has issued a SHUTDOWN event/command. 
     * @param shutdownCause is used to indicate whether the shutdown is due to normal 
     * or abnormal circumstances.
     * @see SocketProtocol#STATUS_SHUTDOWN_NORMAL
     * @see SocketProtocol#STATUS_SHUTDOWN_REMOTE_CLIENT
     */
    public void onReceiveRemoteShutdown(int shutdownCause);
    
}
