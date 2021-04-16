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
 * The interface is the superclass of other Listeners used to receive notifications for 
 * the SocketProtocol.  
 * In many cases, a conforming class implements more than one Listener interface.  The classes 
 * registering the listeners typically stores all listener types in a single storage mechanism 
 * but only notifies the appropriate listeners, as needed.
 * @author Carl Nagle
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
