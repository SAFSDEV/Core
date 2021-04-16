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
/**
 * Developer History:
 * <br> Lei Wang  DEC 10, 2015  Add method execute(): to handle parameters separately.
 */
package org.safs.selenium.rmi.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.safs.selenium.rmi.agent.SeleniumRMIAgent;

/**
 * Defines a generic Selenium RMI Server to communicate with multiple Selenium RMI Agents.
 * <p>
 * A Selenium RMI Server is the centralized controller embedded within each remote Selenium 
 * Standalone Server.  This provides an RMI gateway to the remote host machines running a Selenium 
 * Server allowing us to provide additional functional features on the machine hosting the browsers 
 * of a Selenium test.
 * <p>
 * Note: For consistent operation the <code>java.rmi.server.hostname</code> needs to be set in code or 
 * on the command line before launching an RMI Server:
 * <p><ul>
 * <li>In Code:
 * <p>
 * <ul><code>System.setProperty("java.rmi.server.hostname", "&lt;rmi server ip&gt;");</code></ul>
 * <p>
 * <ul><code>System.setProperty("java.rmi.server.hostname", "hostname.company.internal.net");</code></ul>
 * <p>
 * <li>Command line:
 * <p>
 * <ul><code>-Djava.rmi.server.hostname=&lt;rmi server ip&gt;</code></ul>
 * <p>
 * <ul><code>-Djava.rmi.server.hostname=hostname.company.internal.net</code></ul>
 * </ul>
 * @author Carl Nagle MAR 03, 2015 Original Release
 * @see org.safs.selenium.util.SeleniumServerRunner
 * @see org.safs.selenium.rmi.agent.SeleniumRMIAgent
 * @see org.safs.selenium.rmi.agent.SeleniumAgent
 */
public interface SeleniumRMIServer extends Remote {

	/** 
	 * 'SAFS/SeleniumRMIServer'
	 */
	public static final String DEFAULT_RMI_SERVER = "SAFS/SeleniumRMIServer";
	
	/** 
	 * 'safs.server.running' System Property signifying this JVM contains the RMI Server 
	 * and any RMI Agents should shutdown and not attempt to connect to the RMI Server.
	 * Concrete implementation of this interface should set this System Property to 
	 * some value (not null) to disable local Agents from running.
	 */
	public static final String SERVER_SYSTEM_PROPERTY = "safs.server.running";
	
	/** 
	 * "java.rmi.server.hostname"<br> 
	 * System property or command-line JVM setting (-Djava.rmi.server.hostname=) 
	 * specifying the IP address to be used by the RMI Server.
	 * <p>
	 * This property MUST be set prior to starting the RMI Server/Registry or the RMI Server object 
	 * will set its IP to 127.0.01 "localhost"--which will prevent remote Agents from properly 
	 * finding it.
	 */
	public static final String JAVA_RMI_SERVER_HOSTNAME_PROPERTY = "java.rmi.server.hostname";
	
	/**
	 * Called by an RMI Agent.
	 * A RMI Agent must register with the RMI Server to commence test coordination.  
	 * The Agent usually is polling the registry at regular intervals waiting for the 
	 * appropriate Server object to exist.
	 */
    public void register(SeleniumRMIAgent anAgent) throws RemoteException;

	/**
	 * Called by an RMI Agent.
	 * Unregisters the RMI Agent from the Server.  Normally, this would only be done when 
	 * the Agent is being finalized because its JVM is shutting down.  The Agent does not 
	 * need to unregister itself if the Server has initiated the Agent shutdown.
	 */
    public void unRegister(SeleniumRMIAgent anAgent) throws RemoteException;
    
    /**
	 * Called by an RMI Agent.
     * This is a "do anything" function that the Server and Clients have a private contract 
     * to implement.  It essentially allows a Client/Server implementation to pass anything 
     * back and forth and act according to their shared designs.
     * <p>
     * For example, the Client and Server may have been coded to pass string commands back 
     * and forth and the string commands can be parsed to provide an unlimited number of 
     * command possibilities.
     * 
     * @param command Object of a type expected by the Server implementation for this method.
     * @return Object of a type expected by the Client implementation calling this method.
     */
    public Object runCommand(Object command) throws RemoteException, Exception;
    
    /**
     * Called by an RMI Agent.
     * This is a "do anything" function that the Server and Clients have a private contract 
     * to implement.  It essentially allows a Client/Server implementation to pass anything 
     * back and forth and act according to their shared designs.
     * <p>
     * The parameters should be Serializable.<br>
     * 
     * @param command Object of a type expected by the Server implementation for this method.
     * @param parameters Object... of a type expected by the Server implementation for this method.
     * @return Object of a type expected by the Client implementation calling this method.
     */
    public Object execute(Object command, Object... parameters) throws RemoteException, Exception;
    
}
