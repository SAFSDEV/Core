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
 * DEC 02 2019 (Lei Wang) Provided user a way to set the "registry port" and set the "RMI server port".
 *"
 */
package org.safs.rmi.engine;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.safs.IndependantLog;

/**
 * Root RMI class for many RMI Server/Agent classes.
 * Because this superclass does not implement any Remote calls, we do not have
 * to build Skels or Stubs with rmic.
 * <p>
 * Subclasses will generally extend this class while implementing a Remote interface.
 * <p>
 * Ex:
 *    public class ServerImpl extends RemoteRoot implements Server<br/>
 *    public class AgentImpl  extends RemoteRoot implements Agent
 * <p>
 * @author Carl Nagle
 * @see ServerImpl
 * @see AgentImpl
 */
public class RemoteRoot extends UnicastRemoteObject {

	/** 'localhost' */
	public static final String DEFAULT_RMI_SERVER_HOST = "localhost";

	/** defaults to 'localhost' */
	protected static String serverHost = DEFAULT_RMI_SERVER_HOST;

	/** "registry.port", used to set registry port on which the registry is created. */
	public static final String PROPERTY_REGISTRY_PORT = "registry.port";

	/** "server.port", used to set server port number on which to export the remote object, on which the remote object receives calls. */
	public static final String PROPERTY_SERVER_PORT = "server.port";

	/** the port used by the registry for looking up the RMI server, the default is {@link Registry#REGISTRY_PORT 1099} */
	protected int registryPort = Registry.REGISTRY_PORT;

	/**
	 * 'Remote':Override to indicate Remote RMI type in RMI and debug messages.
	 * Each subclass will normally set this to a unique value for the subclass
	 * during the construction of a new subclass instance.
	 */
	protected String remoteType = "Remote";

	/**
	 * Constructor for RemoteRoot
	 * Subclasses should call super() for this superclass and should also set the
	 * remoteType to whatever they need.
	 *
	 * @throws RemoteException
	 */
	public RemoteRoot() throws RemoteException {
		super();
		//calling instance method in constructor is dangerous because the object is not yet fully initialized
		init();
	}

	public RemoteRoot(int port) throws RemoteException {
		super(port);
		//calling instance method in constructor is dangerous because the object is not yet fully initialized
		init();
	}

	public void init(){
		try{
			String registryPort = System.getProperty(PROPERTY_REGISTRY_PORT);
			if(registryPort!=null){
				IndependantLog.warn("RemoteRoot.init(): Tried to set registry-port to "+registryPort);
				this.registryPort = Integer.parseInt(registryPort);
			}
		}catch(Exception e){
			IndependantLog.warn("RemoteRoot.init(): Failed to set registry-port, met "+e.toString());
		}
		// some JVMs may not allow us to register a shutdown hook
		try{Runtime.getRuntime().addShutdownHook(new ShutdownHook(this));}
		catch(Exception x){
			IndependantLog.error(remoteType +" registering Shutdown Hook permission denied.", x);
		}
	}

	/**
	 * rebind a RMI server object to the localhost RMI Registry.
	 * This is only needed by server objects calling any form of Registry.rebind().
	 * Client objects don't normally call this.
	 * <p>
	 * If no valid localhost registry is found then we will attempt to create one.
	 * Currently, we attempt to create one on port {@link #registryPort} , which is the Java RMI default
	 * port for RMI.
	 * <p>
	 * The security policy for the JVM running this RMI Registry may need the additional permissions
	 * specified in:
	 * <p>
	 *    SAFS\lib\java.policy
	 */
	protected void rebindLocalRMIRegistry(String serverName, Remote server) throws RemoteException{
		try{
			System.out.println(serverName +" attempting RMI Registry bind to "+serverHost+" at port "+registryPort);
			LocateRegistry.getRegistry(serverHost, registryPort).rebind(serverName, server);
			System.out.println(serverName +" bind attempt successful to "+ serverHost+" at port "+registryPort);
		}
		// may not have a registry
		catch(Throwable acx){
			System.out.println(serverName +" failed first bind attempt...trying to createRegistry.");
			try{
				LocateRegistry.createRegistry(registryPort);
				System.out.println("Registry was created successful at port "+registryPort);
				LocateRegistry.getRegistry(serverHost, registryPort).rebind(serverName, server);
				System.out.println(serverName +" bind attempt successful at port "+registryPort);
			}
			catch(java.security.AccessControlException ac2){
				System.out.println("AccessControlException when attempting to create registry.");
				ac2.printStackTrace();
				throw new RemoteException("LocateRegistry:", ac2);
			}
			catch(RemoteException rx){
				System.out.println("RemoteException when attempting to create registry.");
				rx.printStackTrace();
				throw new RemoteException("LocateRegistry:", rx);
			}
			catch(Throwable tx){
				System.out.println("Throwable (not Exception) when attempting to create registry:"+ tx.getMessage());
				throw new RemoteException("LocateRegistry:", tx);
			}
		}
	}


	/**
	 * without this the bootstrap loading mechanism of the JVMAgent class fails with some VerifyError
	 * this occurs on some computer environments in not clarified circumstances
	 */
	@Override
	protected void finalize() throws Throwable { super.finalize( ); }


	/**
	 * This is how we try to ensure our finalize method is called at JVM shutdown.
	 */
	protected class ShutdownHook extends Thread {
		RemoteRoot hook;
		public ShutdownHook(RemoteRoot hookitem){
			hook=hookitem;
		}
		@Override
		public void run(){
			try{hook.finalize();}
			catch(Throwable x){;}
		}
	}
}
