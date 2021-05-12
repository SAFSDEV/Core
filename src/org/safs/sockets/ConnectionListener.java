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
package org.safs.sockets;

/**
 * A NamedListener that wishes to sink connection state information or commands from remote sources.
 * 
 * @author Carl Nagle
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
