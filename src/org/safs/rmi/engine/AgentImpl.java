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
package org.safs.rmi.engine;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;

import org.safs.Log;
import org.safs.STAFHelper;
import org.safs.TestRecordData;
import org.safs.jvmagent.SAFSActionUnsupportedRuntimeException;
import org.safs.jvmagent.SAFSSubItemsAgentUnsupportedRuntimeException;

/**
 * This is an initial concrete implementation of our RMI Agent Interface.
 * This class can be inserted into each JVM by our Bootstrap AgentClassLoader and 
 * subsequently communicates with a local or remote SAFS RMI server for automated testing.
 * <p>
 * To insert this class via the AgentClassLoader the following minimum settings must 
 * appear in the safsjvmagent.properties file:
 * <P>
 * safsjvmagent.properties:
 * <p>
 * <ul>
 * safs.jvmagent.classes=org.safs.rmi.engine.AgentImpl<br/>
 * safs.jvmagent.classpath=c:/safs/lib/jaccess.jar;c:/safs/lib/safs.jar 
 * </ul>
 * <p>
 * To seek a remote SAFS RMI Server the System Property 'sas.server.hostname' 
 * must be set and contain the hostname of the machine expected to contain the RMI Server.
 * <p>
 * Because this is an RMI implementation, an additional Java rmic build process is necessary 
 * prior to creating the JAR file containing all classes.  The Java rmic program creates the 
 * Skeletons and Stubs needed by Java RMI.
 * <p>
 * Execute Java rmic from the root directory of the Java project:
 * <p>
 *    rmic -d . org.safs.rmi.engine.AgentImpl
 * <p>
 * Minimum <code>java.policy</code> permissions needed for successful execution:
 * <p><pre>
      // Allow RMI server objects to receive requests on this machine on port 1024 or higher.
      permission java.net.SocketPermission "*:1024-", "connect,accept,resolve";
      permission java.lang.RuntimePermission "shutdownHooks";
 * </pre>
 * 
 * @author Carl Nagle FEB 01, 2005 Original 
 * @author Carl Nagle MAR 23, 2006 added debugLogSystemProperties 
 * @author Carl Nagle FEB 18, 2015 added RMI support to remote hosts 
 * @see org.safs.rmi.engine.Agent
 * @see org.safs.rmi.engine.Server
 */
public class AgentImpl extends RemoteRoot implements Agent, SubItemsAgent{

	/** 
	 * @see org.safs.rmi.engine.ServerImpl#DEFAULT_RMI_SERVER
	 */
	public static final String DEFAULT_RMI_SERVER = ServerImpl.DEFAULT_RMI_SERVER;

	/** 
	 * 'Agent':Subclasses will override to provide unique Agent remoteType.
	 */
	public static final String DEFAULT_RMI_AGENT = "Agent";

	/** 
	 * Subclasses will override to seek out different RMI Server Objects.
	 */
	protected String serverName = DEFAULT_RMI_SERVER;
	
	protected Server server = null;       	// may require a sync object
	protected boolean shutdown = false;  	// may require a sync object
	protected boolean stafshutdown = false;  // may require a sync object
	protected ServerMonitor monitor = null;
	protected STAFMonitor stafmonitor = null;
	protected ObjID objID = null;				// set by the ServerMonitor
	protected boolean no_staf = false;
	
	protected STAFHelper stafHelper = new STAFHelper();	
	
	/**
	 * Constructor for AgentImpl.
	 * This will create a ServerMonitor object that will periodically poll for the localhost 
	 * RMI Server object with serverName.  The class will also attempt to register a ShutdownHook 
	 * in the Java Runtime so that we can try to gracefully unregister from the RMI Server object 
	 * when the JVM is shutting down.
	 * <p>
	 * Some web browsers launching a JVM may allow us to register a shutdown hook but may not 
	 * actually execute the hook upon JVM shutdown.
	 * 
	 * @throws RemoteException
	 */
	public AgentImpl() throws RemoteException {
		super();
		remoteType = DEFAULT_RMI_AGENT;
	}

	/** MUST be called by subclasses after object creation **/
	protected void initialize(){
		monitor = new ServerMonitor();
		monitor.setName("ServerMonitor");
		monitor.setDaemon(true);
		monitor.start();		
		stafmonitor = new STAFMonitor();
		stafmonitor.setName("STAFMonitor");
		stafmonitor.setDaemon(true);
		stafmonitor.start();		
	}
	
	/**
	 * Does nothing but verify the integrity of the RMI connection and log entry.
	 * @see org.safs.rmi.engine.Agent#ping()
	 */
	public void ping() throws RemoteException{
		Log.info(remoteType +".ping");
	}
	
	/**
	 * @see org.safs.rmi.engine.Agent#getAgentID()
	 */
	public ObjID getAgentID() throws RemoteException {
		return objID;
	}

	/**
	 * @see org.safs.rmi.engine.Agent#getAgentName()
	 */
	public String getAgentName() throws RemoteException {
		return remoteType;
	}

	/**
	 * Default implementation simply throws a SAFSActionUnsupportedRuntimeException("process Unsupported").
	 * An Agent may throw various types of Agent-specific RuntimeExceptions depending upon failure modes.
	 * A Server should catch the RemoteException and Exception but test for subclasses of RuntimeException 
	 * and possibly let them through.
	 * @throws SAFSActionUnsupportedRuntimeException("process Unsupported")
	 * @see org.safs.rmi.engine.Agent#process(Object,TestRecordData)
	 */
	public TestRecordData process(Object object, TestRecordData testRecordData) throws RemoteException, Exception {
		throw new SAFSActionUnsupportedRuntimeException("process Unsupported");
	}

	/**
	 * Removes any RMI server reference we might already have and restarts a new 
	 * ServerMonitor to watch for a new RMI server object.
	 * @see org.safs.rmi.engine.Agent#shutdown()
	 */
	public void shutdown() throws RemoteException {
		Log.info(remoteType+".shutdown");
		server = null;
		
		// with an active server, monitor should have already died.
		if(! monitor.isAlive()) {
			monitor = new ServerMonitor();
			monitor.start();
		}
	}

	/**
	 * Default implementation simply throws SAFSActionUnsupportedRuntimeException("TopLevelCount Unsupported").
	 * @throws SAFSActionUnsupportedRuntimeException("TopLevelCount Unsupported")
	 * @see org.safs.rmi.engine.Agent#getTopLevelCount()
	 */
	public int getTopLevelCount() throws RemoteException, Exception {
		throw new SAFSActionUnsupportedRuntimeException("TopLevelCount Unsupported");
	}

	/**
	 * Default implementation simply throws SAFSActionUnsupportedRuntimeException("TopLevelWindows Unsupported").
	 * @throws SAFSActionUnsupportedRuntimeException("TopLevelWindows Unsupported")
	 * @see org.safs.rmi.engine.Agent#getTopLevelWindows()
	 */
	public Object[] getTopLevelWindows() throws RemoteException, Exception {
		throw new SAFSActionUnsupportedRuntimeException("TopLevelWindows Unsupported");
	}

	/**
	 * Default implementation simply throws SAFSActionUnsupportedRuntimeException("ChildCount Unsupported").
	 * @throws SAFSActionUnsupportedRuntimeException("ChildCount Unsupported")
	 * @see org.safs.rmi.engine.Agent#getChildCount(Object)
	 */
	public int getChildCount(Object parent) throws RemoteException, Exception {
		throw new SAFSActionUnsupportedRuntimeException("ChildCount Unsupported");
	}

	/**
	 * Default implementation simply throws SAFSActionUnsupportedRuntimeException("Children Unsupported").
	 * @throws SAFSActionUnsupportedRuntimeException("Children Unsupported")
	 * @see org.safs.rmi.engine.Agent#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) throws RemoteException, Exception {
		throw new SAFSActionUnsupportedRuntimeException("Children Unsupported");
	}

	/**
	 * Default implementation throws SAFSActionUnsupportedRuntimeException("Caption Unsupported").
	 * @throws SAFSActionUnsupportedRuntimeException("Caption Unsupported")
	 * @see org.safs.rmi.engine.Agent#getCaption(Object)
	 */
	public String getCaption(Object object) throws RemoteException, Exception {
		throw new SAFSActionUnsupportedRuntimeException("Caption Unsupported");
	}

	/**
	 * Default implementation throws SAFSActionUnsupportedRuntimeException("Name Unsupported").
	 * @throws SAFSActionUnsupportedRuntimeException("Name Unsupported")
	 * @see org.safs.rmi.engine.Agent#getName(Object)
	 */
	public String getName(Object object) throws RemoteException, Exception {
		throw new SAFSActionUnsupportedRuntimeException("Name Unsupported");
	}

	/**
	 * Default implementation throws SAFSActionUnsupportedRuntimeException("ID Unsupported").
	 * @throws SAFSActionUnsupportedRuntimeException("ID Unsupported")
	 * @see org.safs.rmi.engine.Agent#getID(Object)
	 */
	public String getID(Object object) throws RemoteException, Exception {
		throw new SAFSActionUnsupportedRuntimeException("ID Unsupported");
	}

	/**
	 * Default implementation throws SAFSActionUnsupportedRuntimeException("Text Unsupported").
	 * @throws SAFSActionUnsupportedRuntimeException("Text Unsupported")
	 * @see org.safs.rmi.engine.Agent#getText(Object)
	 */
	public String getText(Object object) throws RemoteException, Exception {
		throw new SAFSActionUnsupportedRuntimeException("Text Unsupported");
	}

	/**
	 * Default implementation throws SAFSActionUnsupportedRuntimeException("PropertyNames Unsupported").
	 * @throws SAFSActionUnsupportedRuntimeException("PropertyNames Unsupported")
	 * @see org.safs.rmi.engine.Agent#getProperty(Object, String)
	 */
	public String[] getPropertyNames(Object object) throws RemoteException, Exception {
		throw new SAFSActionUnsupportedRuntimeException("PropertyNames Unsupported");
	}

	/**
	 * Default implementation throws SAFSActionUnsupportedRuntimeException("Property Unsupported").
	 * @throws SAFSActionUnsupportedRuntimeException("Property Unsupported")
	 * @see org.safs.rmi.engine.Agent#getProperty(Object, String)
	 */
	public String getProperty(Object object, String property) throws RemoteException, Exception {
		throw new SAFSActionUnsupportedRuntimeException("Property Unsupported");
	}

	/**
	 * Default implementation throws SAFSActionUnsupportedRuntimeException("ClassName Unsupported").
	 * @throws SAFSActionUnsupportedRuntimeException("ClassName Unsupported")
	 * @see org.safs.rmi.engine.Agent#getClassName(Object)
	 */
	public String getClassName(Object object) throws RemoteException, Exception {
		throw new SAFSActionUnsupportedRuntimeException("ClassName Unsupported");
	}

	/**
	 * Default implementation throws SAFSActionUnsupportedRuntimeException("Levelv").
	 * @throws SAFSActionUnsupportedRuntimeException("Level Unsupported")
	 * @see org.safs.rmi.engine.Agent#getLevel(Object)
	 */
	public int getLevel(Object object) throws RemoteException, Exception {
		throw new SAFSActionUnsupportedRuntimeException("Level Unsupported");
	}

	/**
	 * Default implementation throws SAFSActionUnsupportedRuntimeException("Showing Unsupported").
	 * @throws SAFSActionUnsupportedRuntimeException("Showing Unsupported")
	 * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @see org.safs.rmi.engine.Agent#isShowing(Object)
	 */
	public boolean isShowing(Object object) throws RemoteException, Exception {
		throw new SAFSActionUnsupportedRuntimeException("Showing Unsupported");
	}

    /**
	 * Default implementation throws SAFSActionUnsupportedRuntimeException("Valid Unsupported").
	 * @throws SAFSActionUnsupportedRuntimeException("Valid Unsupported")
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
     */
    public boolean isValid(Object object) throws RemoteException, Exception {
		throw new SAFSActionUnsupportedRuntimeException("Valid Unsupported");
    }

	/**
	 * Default implementation throws SAFSActionUnsupportedRuntimeException("SuperClassNames Unsupported").
	 * @throws SAFSActionUnsupportedRuntimeException("SuperClassNames Unsupported")
	 * @see org.safs.rmi.engine.Agent#getSuperClassNames(Object)
	 */
	public String[] getSuperClassNames(Object object) throws RemoteException, Exception {
		throw new SAFSActionUnsupportedRuntimeException("SuperClassNames Unsupported");
	}

	/**
	 * Default implementation throws SAFSActionUnsupportedRuntimeException("StringData Unsupported").
	 * @throws SAFSActionUnsupportedRuntimeException("StringData Unsupported")
	 * @see org.safs.rmi.engine.Agent#getStringData(Object, Object)
	 */
	public String[][] getStringData(Object object, Object dataInfo) throws RemoteException, NoSuchMethodException, Exception {
		throw new SAFSActionUnsupportedRuntimeException("StringData Unsupported");
	}

	/**
	 * Mechanism to retrieve a subitem/object identified 
	 * by the provided Path.  Path is hierarchical information showing parent->child 
	 * relationships separated by '->'.  This is often used in Menus and Trees.
	 * <p>
	 * Ex:
	 * <p>
	 *     File->Exit<br/>
	 *     Root->Branch->Leaf
	 * 
	 * Default implementation throws SAFSSubItemsAgentUnsupportedRuntimeException("MatchingPathObject Unsupported").
	 * 
	 * @param theObject--Object proxy for the object to be evaluated.
	 * 
	 * @param thePath information to locate another object or subitem relative to theObject.
	 *        this is usually something like a menuitem or tree node where supported.
	 * 
	 * @throws SAFSSubItemsAgentUnsupportedRuntimeException("MatchingPathObject Unsupported")
	 * @see org.safs.rmi.engine.Agent#getMatchingPathObject(Object, String)
	 **/
	public Object getMatchingPathObject (Object theObject, String thePath) throws RemoteException, Exception { 
		throw new SAFSSubItemsAgentUnsupportedRuntimeException("MatchingPathObject Unsupported");
	}


	/**
	 * Mechanism to determine if the object contains a subitem/object identified 
	 * by the provided Path.  Path is hierarchical information showing parent->child 
	 * relationships separated by '->'.  This is often used in Menus and Trees.
	 * <p>
	 * Ex:
	 * <p>
	 *     File->Exit<br/>
	 *     Root->Branch->Leaf
	 * 
	 * Default implementation throws SAFSSubItemsAgentUnsupportedRuntimeException("MatchingPath Unsupported").
	 * @param theObject--Object proxy for the object to be evaluated.
	 * 
	 * @param thePath information to locate another object or subitem relative to theObject.
	 *        this is usually something like a menuitem or tree node where supported.
	 * 
	 * @throws SAFSSubItemsAgentUnsupportedRuntimeException("MatchingPath Unsupported")
	 * @see org.safs.rmi.engine.Agent#getMatchingPath(Object, String)
	 **/
	public boolean isMatchingPath	(Object theObject, String thePath) throws RemoteException, Exception { 
		throw new SAFSSubItemsAgentUnsupportedRuntimeException("MatchingPath Unsupported");
	}
    
	/**
	 * Default implementation throws SAFSActionUnsupportedRuntimeException("runCommand Unsupported").
	 * @throws SAFSActionUnsupportedRuntimeException("runCommand Unsupported");
	 * @see org.safs.rmi.engine.Agent#runCommand(Object)
	 */
	public Object runCommand(Object command) throws RemoteException, Exception {
		System.out.println("AgentImpl running command: "+ command);
		throw new SAFSActionUnsupportedRuntimeException("runCommand Unsupported");
	}

    /**
     * Attempts to set theObject as the active (topmost?) Window or Component in the JVM.
	 * Default implementation throws SAFSActionUnsupportedRuntimeException("runCommand Unsupported").
	 * @throws SAFSActionUnsupportedRuntimeException("ActiveWindow Unsupported");
     * @param theObject Object of a type expected by the Client implementation for this method.
     */
    public void setActiveWindow(Object theObject) throws RemoteException, Exception{
		throw new SAFSActionUnsupportedRuntimeException("ActiveWindow Unsupported");
    }

	/**
	 * Default no-op simply throws SAFSSubItemsAgentUnsupportedRuntimeException("SubItemAtIndex Unsupported").
	 * @param object reference from which to locate the subitem.
	 * @param index of the subitem to retrieve.
	 * @return subitem object or String
	 * @throws IndexOutOfBoundsException if index is invalid
	 * @throws SAFSObjectNotFoundException if subitem at index cannot be retrieved
	 * @throws SAFSSubItemsAgentUnsupportedRuntimeException("SubItemAtIndex Unsupported") as necessay.
	 * @see SubItemsAgent#getSubItemAtIndex(Object,int)
	 */
	public Object getSubItemAtIndex(Object object, int index) throws RemoteException, Exception{
		throw new SAFSSubItemsAgentUnsupportedRuntimeException("SubItemAtIndex Unsupported");
	}

	/**
	 * Log.debug all available Java System Properties.
	 * @author Carl Nagle Mar 23, 2006
	 */
	protected static void debugLogSystemProperties(){
		java.util.Properties _props = System.getProperties();
		java.util.Enumeration props = _props.keys();
		String key;
		while(props.hasMoreElements()){
			key = (String)props.nextElement();
			Log.debug("System: "+ key +" = "+ _props.getProperty(key));
		}
	}
	
	/**
	 * If we have registered with a RMI Server object then unRegister with that object.
	 * If our ServerMonitor thread is still looking for a server then interrupt the thread
	 * and let the thread die.
	 */
	protected void finalize() throws Exception{
		Log.info(remoteType+".finalize");
		if(server != null){
			System.out.println("Agent.finalize unregistering with RMI server.");					
			server.unRegister((Agent)this);
			server = null;
			objID = null;
			
			// monitor should not be alive
		}
		else{
			// monitor likely polling for an RMI Server
			if (monitor.isAlive()) {
				System.out.println("Monitor forced shutdown commencing...");					
				shutdown = true;
				monitor.interrupt();
			}
		}
		
		if(stafHelper.isInitialized()){
			//System.out.println("Agent.finalize unregistering with STAF.");					
			try{stafHelper.unRegister();}catch(Exception x){;}
			stafHelper = new STAFHelper();
			
			// monitor should not be alive
		}
		// monitor likely polling for an RMI Server
		if (stafmonitor.isAlive()) {
			//System.out.println("Monitor forced shutdown commencing...");					
			stafshutdown = true;
			stafmonitor.interrupt();
		}
	}

	/**
	 * Polls the server host for a Java Registry every few seconds until one with the proper 
	 * server object is found.  This is started at JVM bootup and remains running until 
	 * satisfied.  The thread will die once we have successfully registered with a server object.
	 * The Thread will also terminate if it detects the JVM is running the RMI Server 
	 * by polling the System Property 'safs.server.running'.
	 */
	protected class ServerMonitor extends Thread {
		Registry registry = null;
		String host = null;
		public void run(){
			while(!shutdown && (server==null)){
				try{
					if(System.getProperty(Server.SERVER_SYSTEM_PROPERTY) instanceof Object) {
						System.out.println("JAVA JVM appears to contain the SAFS RMI Server: ");
						shutdown = true;
						objID = null;
						break;
					}
				}catch(Exception re){
					System.out.println("Agent ignoring "+ re.getClass().getName()+", "+re.getMessage());
				}
				try{
					host = System.getProperty(Agent.SERVER_HOSTNAME_SYSTEM_PROPERTY);
					if(host != null && host.length() > 0) serverHost = new String(host);
					System.out.println("Seeking SAFS RMI Server at "+ serverHost);
					registry = LocateRegistry.getRegistry(serverHost, 1099);
					server = (Server)(registry.lookup(serverName));
					System.out.println("SAFS RMI registry lookup provided Server class "+ server.getClass().getName());
					System.out.println("Monitor registering "+remoteType);	
					objID = new ObjID();				
					server.register((Agent) AgentImpl.this);
				}
				catch(Exception re){ 
					System.out.println("SAFS RMI Server at "+ serverHost +" not found: "+ re.getClass().getName()+", "+re.getMessage());
					re.printStackTrace();
					try{ sleep(3000);}
					catch(InterruptedException ie){;}
				}
			}
		}
	}

	/**
	 * Polls for the existence of STAF every few seconds until found.
	 * This is started at JVM bootup and remains running until 
	 * satisfied.  The thread will die once we have successfully registered with STAF.
	 * The Thread will also terminate if it detects the JVM is running the RMI Server 
	 * by polling the System Property 'safs.server.running'.
	 */
	protected class STAFMonitor extends Thread {
		public void run(){
			while(!no_staf && !stafshutdown){
				if(! stafHelper.isInitialized()){
					if(System.getProperty(Server.SERVER_SYSTEM_PROPERTY) instanceof Object) {
						System.out.println("STAFMonitor detecting SAFS RMI Server. Initiating STAF Monitor shutdown.");
						stafshutdown = true;
						no_staf = true;
						break;
					}
					try{
						stafHelper.initialize(remoteType);
						Log.setHelper(stafHelper);
						Log.info("STAFMonitor registering "+remoteType);	
					}
					catch(Exception re){ 
						try{ sleep(3000);}
						catch(InterruptedException ie){;}
					}
				}
				// stafHelper IS inititialized
				else{
					try{
						stafHelper.localPing();
						try{sleep(2000);}
						catch(InterruptedException ie){;}
					}
					catch(Exception x){
						Log.info("STAFMonitor UNregistering "+remoteType);	
						try{stafHelper.unRegister();}
						catch(Exception x2){;}
						stafHelper = new STAFHelper();
					}
				}
			}
		}
	}
	
	/** 
	 * Primarily just a test instantiation to seek an RMI server.
	 * Example command-line invocation:
	 * <p>
	 * <pre><code>
	 * java -cp %CLASSPATH% -Djava.security.policy=%SAFSDIR%\lib\java.policy
	 *                      -Dsafs.server.hostname="hostname.internal.net" 
	 *                      org.safs.rmi.engine.AgentImpl
	 * </pre></code>
	 * @param args -- none
	 **/
	public static void main(String[] args){
		AgentImpl agent = null;
		try{
			System.out.println("Starting Server-seeking Agent");
			agent = new AgentImpl();
			agent.no_staf = true;
			agent.initialize();
			while(agent.monitor.isAlive()){
				
			}
			System.out.println("Server-seeking Agent may have found a server...");
			if(agent.server != null){
				Object rc = null;
				boolean done = false;
				do{
					System.out.println("AgentImpl trying server.runCommand...");
					try{ 
						rc= agent.server.runCommand("Do something Special");
						if(rc != null) System.out.println(rc.toString());
						else System.out.println("Server returned a NULL command resposne.");
						done = true;
					}
					catch(SAFSActionUnsupportedRuntimeException x){
						System.out.println("Agent received expected ActionNotSupportedException.");
						done = true;
					}
					if(!done)try{ Thread.sleep(3000);}catch(InterruptedException ix){}
				}while(!done);
			}else{
				System.out.println("Server-seeking Agent does NOT have a reference to a Server.");				
			}
		}catch(Exception rx){
			rx.printStackTrace();
		}
	}
}
