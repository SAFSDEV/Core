/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rmi.engine;

import org.safs.Log;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.net.*;
import java.text.*;

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
 * @author canagl
 * @see ServerImpl
 * @see AgentImpl
 */
public class RemoteRoot extends UnicastRemoteObject {

	/** 'localhost' */
	public static final String DEFAULT_RMI_SERVER_HOST = "localhost";
	
	/** defaults to 'localhost' */
	protected static String serverHost = DEFAULT_RMI_SERVER_HOST;
	

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

		// some JVMs may not allow us to register a shutdown hook
		try{Runtime.getRuntime().addShutdownHook(new ShutdownHook(this));}
		catch(Exception x){
			Log.error(remoteType +" registering Shutdown Hook permission denied.", x);
		}
	}


	/**
	 * rebind a RMI server object to the localhost RMI Registry.
	 * This is only needed by server objects calling any form of Registry.rebind().  
	 * Client objects don't normally call this.
	 * <p>
	 * If no valid localhost registry is found then we will attempt to create one.
	 * Currently, we attempt to create one on port 1099, which is the Java RMI default 
	 * port for RMI.
	 * <p>
	 * The security policy for the JVM running this RMI Registry may need the additional permissions 
	 * specified in:
	 * <p>
	 *    SAFS\lib\java.policy
	 */
	protected static void rebindLocalRMIRegistry(String serverName, Remote server) throws RemoteException{
		try{
			System.out.println(serverName +" attempting RMI Registry bind to "+serverHost); 
			LocateRegistry.getRegistry(serverHost, 1099).rebind(serverName, server);
			System.out.println(serverName +" bind attempt successful to "+ serverHost);
		}
		// may not have a registry
		catch(Throwable acx){
			System.out.println(serverName +" failed first bind attempt...trying to createRegistry.");
			try{
				LocateRegistry.createRegistry(1099);
				LocateRegistry.getRegistry(serverHost, 1099).rebind(serverName, server);
				System.out.println(serverName +" bind attempt successful.");
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
        protected void finalize() throws Throwable { super.finalize( ); }
 
        
	/**
	 * This is how we try to ensure our finalize method is called at JVM shutdown.
	 */
	protected class ShutdownHook extends Thread {
                RemoteRoot hook;		
		public ShutdownHook(RemoteRoot hookitem){ 
			hook=hookitem;
		}
		public void run(){ 
			try{hook.finalize();} 
			catch(Throwable x){;}
		}		
	}
}
