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

import java.util.Properties;

/**
 * Interface definition for a local SocketProtocol Runner to  
 * receive asynchronous notification of AbstractProtocolRunner events and messages from a remote  
 * SocketProtocol Runner.
 * 
 * @author Carl Nagle, SAS Institute, Inc.
 */
public interface SocketProtocolListener extends ConnectionListener {

	public static final String DEFAULT_NAME = "SocketsTCPMessenger";

    /**
     * Remote client has issued the READY event 
     */
    public void onReceiveReady();
    
    /**
     * Remote client has issued the RUNNING event--usually resulting from a DISPATCH event. 
     */
    public void onReceiveRunning();
    
    /**
     * Remote client has issued the RESULT event and is returning a simple statuscode and statusinfo response.
     * @param rc -- int statuscode
     * @param info -- String statusinfo.  Can be null.  Can be an empty string.
     */
    public void onReceiveResult(int rc, String info);
    
    /**
     * Remote client has issued the RESULT event and is returning result information in Properties.
     * @param Properties list containing the results properties.
     */
    public void onReceiveResultProperties(Properties result);

    /**
     * Remote client has thrown/issued an Exception message.
     * @param message
     */
    public void onReceiveException(String message);
    
    /**
     * Remote client has sent an arbitrary message.
     * This is for custom communication between an engine and a remote client.
     * @param message
     */
    public void onReceiveMessage(String message);    
}
