/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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
