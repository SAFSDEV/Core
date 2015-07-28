/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rmi.engine;

import java.rmi.*;

/**
 * Defines a generic SAFS Engine RMI Server to communicate with multiple SAFS Engine RMI Agents.
 * <p>
 * A SAFS Engine RMI Server is the centralized controller that will talk with each RMI Agent 
 * embedded within each enabled JVM--local or remote.  This provides an RMI alternative to STAF for multi-JVM 
 * communications between the very visible SAFS Engine and the invisible Agents in each JVM.
 * <p>
 * The typical scenario here would be that a SAFS Engine like SAFS/Abbot would still present 
 * a single STAF-based event-driven interface for the TID and any other Driver wishing to 
 * use it, but underneath the engine will be communicating with multiple JVMs over RMI.
 * <p>
 * This is somewhat analogous to how other tools like RobotJ, Robot, and WinRunner communicate 
 * with all running JVMs through a single script engine or controller.
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
 * @author canagl JAN 25, 2005 Original Release
 */
public interface Server extends Remote {

	/** 
	 * 'safs.server.running' System Property signifying this JVM contains the RMI Server 
	 * and RMI Agents should shutdown and not attempt to connect to the RMI Server.
	 * Concrete implementation of this interface should set this System Property to 
	 * some value (not null) to disable local Agents from running.
	 */
	public static final String SERVER_SYSTEM_PROPERTY = "safs.server.running";
	
	/** 
	 * "java.rmi.server.hostname" System property or command-line JVM setting specifying 
	 * the IP address to be used by the RMI Server.
	 */
	public static final String JAVA_RMI_SERVER_HOSTNAME_PROPERTY = "java.rmi.server.hostname";
	
	/**
	 * A JVM Agent will register with the RMI Server after it has become available in 
	 * the RMI Naming registry.  The Agent usually is polling the registry at regular 
	 * intervals waiting for the appropriate Server object to exist.  The Server object 
	 * is not created until the actual controlling SAFS Engine is launched.
	 * <p>
	 * Registering with the Server makes that remote JVM available for testing through 
	 * the controlling SAFS Engine.
	 */
    public void register(Agent anAgent) throws RemoteException;

	/**
	 * Unregisters the JVM Agent from the Server.  Normally, this would only be done when 
	 * the Agent is being finalized because its JVM is shutting down.  The Agent does not 
	 * need to unregister itself if the Server has initiated the Agent shutdown.
	 */
    public void unRegister(Agent anAgent) throws RemoteException;
    
    /**
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
    
}
