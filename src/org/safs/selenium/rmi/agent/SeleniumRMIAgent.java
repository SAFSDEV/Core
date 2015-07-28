/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.selenium.rmi.agent;

import java.rmi.*;
import java.rmi.server.*;

/**
 * Defines a generic Selenium RMI Agent communicating with a Selenium RMI Server.
 * <p>
 * A Selenium RMI Server is the centralized controller embedded within each remote Selenium 
 * Standalone Server.  This provides an RMI gateway to the remote host machines running a Selenium 
 * Server allowing us to provide additional functional features on the machine hosting the browsers 
 * of a Selenium test.
 * <p>
 * The Selenium RMI Agent is typically started automatically by SAFS/Selenium classes needing to 
 * communicate with the remote SAFS RMI Server. 
 * <p>
 * Note: For consistent operation the <code>safs.server.hostname</code> needs to be set in code or 
 * on the command line before launching an Agent connecting to a remote Selenium RMI Server:
 * <p><ul>
 * <li>In Code:
 * <p>
 * <ul><code>System.setProperty("safs.server.hostname", "&lt;rmi server ip&gt;");</code></ul>
 * <p>
 * <ul><code>System.setProperty("safs.server.hostname", "hostname.company.internal.net");</code></ul>
 * <p>
 * <li>Command line:
 * <p>
 * <ul><code>-Dsafs.server.hostname=&lt;rmi server ip&gt;</code></ul>
 * <p>
 * <ul><code>-Dsafs.server.hostname=hostname.company.internal.net</code></ul>
 * </ul>
 * @author Carl Nagle MAR 03, 2015 Original Release
 * @see org.safs.selenium.util.SeleniumServerRunner
 * @see org.safs.selenium.rmi.server.SeleniumRMIServer
 * @see org.safs.selenium.rmi.server.SeleniumServer
 */
public interface SeleniumRMIAgent extends Remote {
	
	/** 'SeleniumAgent' */
	public static final String DEFAULT_RMI_AGENT = "SeleniumAgent";

	/** 
	 * 'safs.server.hostname' Name of the System Property signifying a remote SAFS RMI Server 
	 * hostname to seek for a SAFS RMI Server.
	 */
	public static final String SERVER_HOSTNAME_SYSTEM_PROPERTY = "safs.server.hostname";
	
	/**
	 * Called by an RMI Server.
	 * A simple RUThere call.  The server will generally receive a ConnectException 
	 * thrown by RMI if the Agent cannot be called because it has gone away.
	 */
	public void ping () throws RemoteException;
	
	/**
	 * Called by an RMI Server.
	 * Returns an ObjID to uniquely identify the RMI Agent.
	 */
	public ObjID getAgentID() throws RemoteException;

	/**
	 * Called by an RMI Server.
	 * Returns the name or remoteType of the RMI Agent.
	 */
	public String getAgentName() throws RemoteException;

    /**
	 * Called by an RMI Server.
     * The RMI Server will instruct the Agent to shutdown when the Server itself is being 
     * shutdown or finalized.  The Agent does not have to unRegister from the Server in this 
     * case because the Server will already be disconnecting from the Agent.
     * <p>
     * The Agent should accept the shutdown request and initiate a new shutdown Thread so 
     * that the call to shutdown from the Server can immediately return.
     * <p>
     * The Agent might normally reset itself to an idle state where it is polling once again 
     * for the existence of another RMI Server.
     */
    public void shutdown() throws RemoteException;
    
    /**
     * Called by an RMI Server.
     * This is a "do anything" function that the Server and Clients have a private contract 
     * to implement.  It essentially allows a Client/Server implementation to pass anything 
     * back and forth and act according to the shared design.
     * <p>
     * For example, the Client and Server may have been coded to pass string commands back 
     * and forth and the string commands can be parsed to provide an unlimited number of 
     * command possibilities.
     * 
     * @param command Object of a type expected by the Client implementation for this method.
     * @return Object of a type expected by the Server implementation calling this method.
     */
    public Object runCommand(Object command) throws RemoteException, Exception;

}
