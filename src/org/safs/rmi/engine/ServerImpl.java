/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rmi.engine;

import org.safs.Log;
import org.safs.STAFHelper;
import org.safs.TestRecordData;
import org.safs.jvmagent.NoSuchPropertyException;
import org.safs.jvmagent.SAFSActionErrorRuntimeException;
import org.safs.jvmagent.SAFSActionUnsupportedRuntimeException;
import org.safs.SAFSRuntimeException;
import org.safs.jvmagent.LocalServer;
import org.safs.jvmagent.LocalSubItemsAgent;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.security.*;
import java.util.*;
import java.net.*;
import java.text.*;
import java.io.Serializable;

/**
 * A concrete implementation of our Server Interface for Java RMI test automation.
 * Specific Engine subclasses will extend this for specific tools -- like Abbot or others.
 * <p>
 * The server communicates with remote Agents on the local machine acting as Java Enablers.
 * The security policy for the JVM running this RMI server may need the additional permissions 
 * specified in:
 * <p>
 *    SAFS\lib\java.policy
 * <p>
 * Normally this RMI Server would be instanced in a JVM we control -- like the TID or a SAFS Engine.
 * Thus, these settings do not impact any AUT security policy since the AUT is running in a 
 * different JVM.
 * <p>
 * Because this is an RMI implementation, an additional Java rmic build process is necessary 
 * prior to creating the JAR file containing all classes.  The Java rmic program creates the 
 * Skeletons and Stubs needed by Java RMI.
 * <p>
 * Execute Java rmic from the root directory of the Java project:
 * <p>
 *    rmic -d . org.safs.rmi.engine.ServerImpl
 * <p>
 * Note: For consistent operation the <code>java.rmi.server.hostname</code> needs to be set in code or 
 * on the command line before launching an RMI Server:
 * <p>
 * <ul>
 * <li>In Code:
 * <p>
 * <ul>
 * <p><code>System.setProperty("java.rmi.server.hostname", "&lt;rmi server ip&gt;");</code></ul>
 * <p>
 * <li>Command line:<br/>
 * <p>
 * <ul>
 * <p><code>-Djava.rmi.server.hostname=&lt;rmi server ip&gt;</code></ul>
 * </ul>
 * <p>
 * Minimum <code>java.policy</code> permissions needed for successful execution:
 * <p><pre>
      // Allow RMI server objects to receive requests on this machine on port 1024 or higher.
      permission java.net.SocketPermission "*:1024-", "connect,accept,resolve";
      permission java.lang.RuntimePermission "shutdownHooks";
      permission java.util.PropertyPermission "safs.*", "write,read";
      permission java.util.PropertyPermission "java.rmi.server.hostname", "write,read";
 * </pre>
 * 
 * @author canagl  FEB 02, 2005  Original
 * Feb 20, 2006 (Szucs) correcting the getMatchingPathObject( ) method to return
 *                      the correct AgentWindow object
 * @author canagl  FEB 23, 2015  Updates supporting remote RMI Servers.
 */
public class ServerImpl extends RemoteRoot implements Server, LocalServer, LocalSubItemsAgent {

	private Hashtable agents = new Hashtable();
	
	/** 
	 * 'SAFS/RMIEngineServer'
	 */
	public static final String DEFAULT_RMI_SERVER = "SAFS/RMIEngineServer";
	
	/**
	 * Name of Server object in rmi Naming registry.
	 * Subclasses should override the value.  Defaults to DEFAULT_RMI_SERVER
	 */
	protected String serverName = DEFAULT_RMI_SERVER;
	
	/**
	 * Constructor for ServerImpl.
	 * Sets our remoteType to DEFAULT_RMI_SERVER
	 * @throws RemoteException
	 */
	public ServerImpl() throws RemoteException {
		super();
		System.setProperty(SERVER_SYSTEM_PROPERTY, DEFAULT_RMI_SERVER);
		remoteType = DEFAULT_RMI_SERVER;
		rebindLocalRMIRegistry(serverName, this);
	}


	/**
	 * @see org.safs.rmi.engine.Server#register(Agent)
	 */
	public void register(Agent anAgent) throws RemoteException {
		agents.put(anAgent.getAgentID(), anAgent);		
		String agentname = anAgent.getAgentName();
		System.out.println(agentname +" registered Agent "+ agents.size() +":ID="+ anAgent.getAgentID()); 
	}

	/**
	 * Uses a separate Thread to unRegister the Agent and perform other internal checks after 
	 * the Agent is unregistered.  This should return to the calling Agent immediately so that 
	 * we don't interfere with any remote JVM shutdowns.
	 * 
	 * @see org.safs.rmi.engine.Server#unRegister(Agent)
	 */
	public void unRegister(Agent anAgent) throws RemoteException {
		class UnregThread extends Thread {
			private ObjID theID;
			public UnregThread(ObjID objID){ 
				super(); 
				theID = objID;}
			public void run(){
				agents.remove(theID);		
				System.out.println(remoteType +" removed Agent ID="+ theID +". "+ agents.size() +" agents remain.");
				pingAgents();
			}
		}
		new UnregThread(anAgent.getAgentID()).run();
	}

	/**
	 * Simple routine to ping each registered agent to see if it is actually still there.
	 * The routine will unregister an agent when the attempt to contact it throws an exception.
	 * This can happen when a remote Agent's JVM does not call registered shutdown hooks and allow 
	 * the Agent to gracefully unregister with the RMI server.
	 */
	protected void pingAgents(){
		Enumeration keys = agents.keys();

		// PING all known agents to see if they are still alive
		while(keys.hasMoreElements()){
			Agent agent = null;
			ObjID objID = null;
			try{
				objID = (ObjID) keys.nextElement();
				agent = (Agent) agents.get(objID);
				agent.ping();
				System.out.println(remoteType +":Agent ID="+ objID +" OK");
			}
			catch(java.rmi.RemoteException x){
				agents.remove(objID);
				System.out.println(remoteType +" removing Disconnected Agent ID="+ objID +". "+ agents.size() +" agents remain.");
				//x.printStackTrace();
			}
			catch(Exception x){
				x.printStackTrace();
				agents.remove(objID);
				System.out.println(remoteType +" removing Missing Agent ID="+ objID +". "+ agents.size() +" agents remain.");
				//x.printStackTrace();				
			}
		}
	}	

	/**
	 * Return the number of currently active Top Level Windows from 
	 * all known Agents.
	 * 
	 * @see org.safs.jvmagent.LocalServer#getTopLevelCount()
	 */
    public int getTopLevelCount() {
    	int sum = 0;
    	if (agents.size()==0) return sum;
    	Enumeration enumerator = agents.elements();
    	while(enumerator.hasMoreElements()){
    		try{ 
    			sum += ((Agent) enumerator.nextElement()).getTopLevelCount();
    		}
    		// should we surface issues from individual JVMS?  How?
    		catch(Exception x){;}
    	}
    	return sum;
    }

    /**
     * Return an AgentWindow array representing the TopLevel windows from all known Agents.
     * 
     * @return AgentWindow[] representing all active top level windows in all JVMs.
     * This array will be Server/Agent specific and may be nothing more than arrays of 
     * AgentWindows used to uniquely identify objects in an Agent-maintained Hashtable.  
     * A zero-length array will be returned if no Top Level Windows are active.
     * 
	 * @see org.safs.jvmagent.LocalServer#getTopLevelWindows()
     */
    public Object[] getTopLevelWindows() {
    	AgentWindow[] rc = new AgentWindow[0];
    	if (agents.size()==0) return rc;
    	ArrayList roots = new ArrayList();
    	Object[] wins = null;
    	ObjID agentkey = null;
    	Agent aagent = null;
    	Enumeration enumerator = agents.keys();
    	while(enumerator.hasMoreElements()){
    		try{ 
    			agentkey = (ObjID) enumerator.nextElement();
    			aagent = (Agent) agents.get(agentkey);
    			wins = aagent.getTopLevelWindows();
    			// tag every window with the Agent that owns it
    			for(int i=0; i< wins.length;i++){
    			    roots.add(new AgentWindow(agentkey, wins[i]));
    			}
    		}
    		// should we surface issues from individual JVMS?  How?
    		catch(Exception x){;}
    	}
    	return roots.toArray(rc);
    }

	/**
	 * Return the number of children available in the remote parent (AgentWindow).
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid parent") if the provided parent cannot be located.
	 * @throws SAFSActionErrorRuntimeException(x.getMessage(), x) if an unexpected Exception is thrown from 
	 * @see org.safs.jvmagent.LocalServer#getChildCount(Object)
	 */
    public int getChildCount(Object parent) {
    	try{
	    	AgentWindow win = (AgentWindow) parent;
	    	// extract which Agent owns this parent
	    	Agent aagent = (Agent) agents.get(win.getAgentID());
    		return aagent.getChildCount(win.getWindowID());
    	}
    	catch(SAFSRuntimeException x) { throw x; }
    	catch(Exception x){    		
    		throw new SAFSActionErrorRuntimeException(x.getMessage(), x);
    	}
    }
    
	/**
	 * Return an array representing the children of the provided parent AgentWindow.
     * 
     * @param parent An object (AgentWindow) from getTopLevelWindows or from a previous call to getChildren.
	 * The parent is often one of the elements of the TopLevelWindow array or somewhere 
	 * lower in that same hierarchy.
	 * 
     * @return AgentWindow[] representing all known children of the provided parent.
     * This array may be Server/Agent specific and may be nothing more than arrays of 
     * AgentWindows used to uniquely identify objects in an Agent-maintained Hashtable.
     * A zero-length array will be returned if the parent has no children.
	 * 
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid parent") if the provided parent cannot be located.
	 * @throws SAFSActionErrorRuntimeException(x.getMessage(), x) if an unexpected Exception is thrown from 
	 * @see org.safs.jvmagent.LocalServer#getChildren(Object)
	 */
    public Object[] getChildren(Object parent){
    	try{
	    	AgentWindow win = (AgentWindow) parent;
	    	// extract which Agent owns this parent
	    	Agent aagent = (Agent) agents.get(win.getAgentID());
    		Object[] rc = aagent.getChildren(win.getWindowID());
    		AgentWindow [] children = new AgentWindow[rc.length];
    		// tag every child with the Agent that owns it
    		for (int i = 0; i < rc.length; i++){
    			children[i] = new AgentWindow(win.getAgentID(), rc[i]);
    		}
    		return children;
    	}
    	catch(SAFSRuntimeException x) { throw x; }
    	catch(Exception x){    		
    		throw new SAFSActionErrorRuntimeException(x.getMessage(), x);
    	}
    }
    
    /**
     * Make the provided object the "active" object or window (bring it forward).  
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid object") if the provided parent cannot be located.
	 * @throws SAFSActionErrorRuntimeException(x.getMessage(), x) if an unexpected Exception is thrown from 
     */
    public void setActiveWindow(Object object) {
    	try{
	    	AgentWindow win = (AgentWindow) object;
	    	// extract which Agent owns this parent
	    	Agent aagent = (Agent) agents.get(win.getAgentID());
    		aagent.setActiveWindow(win.getWindowID());
    	}
    	catch(SAFSRuntimeException x) { throw x; }
    	catch(Exception x){    		
    		throw new SAFSActionErrorRuntimeException(x.getMessage(), x);
    	}
    }
    
    /**
     * Return the Class name of the object.  
     * For example, we may get "javax.swing.JFrame" or the name of the subclass if 
     * it is a subclass of JFrame.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String Class name of the object.
	 * 
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid object") if the provided parent cannot be located.
	 * @throws SAFSActionErrorRuntimeException(x.getMessage(), x) if an unexpected Exception is thrown from 
	 * @see org.safs.jvmagent.LocalServer#getClassName(Object)
     */
    public String getClassName(Object object){
    	try{
	    	AgentWindow win = (AgentWindow) object;
	    	// extract which Agent owns this object
	    	Agent aagent = (Agent) agents.get(win.getAgentID());
    		return aagent.getClassName(win.getWindowID());
    	}
    	catch(SAFSRuntimeException x) { throw x; }
    	catch(Exception x){    		
    		throw new SAFSActionErrorRuntimeException(x.getMessage(), x);
    	}
    }

    /**
     * Return the array of all superclass names for the object.  
     * This should return the Class hierarchy for the object all the way to Class Object. 
     * A 0-length array will be returned if there are none (Class=java.lang.Object).
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String[] Class names for the superclass hierarchy.
	 * 
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid object") if the provided parent cannot be located.
	 * @throws SAFSActionErrorRuntimeException(x.getMessage(), x) if an unexpected Exception is thrown from 
	 * @see org.safs.jvmagent.LocalServer#getSuperClassNames(Object)
     */
    public String[] getSuperClassNames(Object object){
    	try{
	    	AgentWindow win = (AgentWindow) object;
	    	// extract which Agent owns this object
	    	Agent aagent = (Agent) agents.get(win.getAgentID());
    		return aagent.getSuperClassNames(win.getWindowID());
    	}
    	catch(SAFSRuntimeException x) { throw x; }
    	catch(Exception x){    		
    		throw new SAFSActionErrorRuntimeException(x.getMessage(), x);
    	}
    }

    /**
     * Retrieve the list of available properties for the object.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * 
	 * @return String[] the names of available properties.
	 * 
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid object") if the provided parent cannot be located.
	 * @throws SAFSActionErrorRuntimeException(x.getMessage(), x) if an unexpected Exception is thrown from 
	 * @see org.safs.jvmagent.LocalServer#getPropertyNames(Object)
     */
    public String[] getPropertyNames(Object object){
    	try{
	    	AgentWindow win = (AgentWindow) object;
	    	// extract which Agent owns this object
	    	Agent aagent = (Agent) agents.get(win.getAgentID());
    		return aagent.getPropertyNames(win.getWindowID());
    	}
    	catch(SAFSRuntimeException x) { throw x; }
    	catch(Exception x){    		
    		throw new SAFSActionErrorRuntimeException(x.getMessage(), x);
    	}
    }
    
    /**
     * Retrieve the property value of the object if the object has the property.
     * 
     * @param object -- An object from getTopLevelWindows or from a previous call to getChildren.
	 * @param property -- the case-sensitive name of the property to seek.
	 * 
	 * @return String the text value of the object property.  Since property values can theoretically 
	 * legally be zero-length, a null value will be returned if no value exists for the 
	 * object.
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid object") if the provided parent cannot be located.
	 * @throws SAFSInvalidActionArgumentRuntimeException(property)
	 * @throws SAFSActionErrorRuntimeException(x.getMessage(), x) if an unexpected Exception is thrown from 
	 * @see org.safs.jvmagent.LocalServer#getProperty(Object,String)
     */
    public String getProperty(Object object, String property) {
    	try{
	    	AgentWindow win = (AgentWindow) object;
	    	// extract which Agent owns this object
	    	Agent aagent = (Agent) agents.get(win.getAgentID());
    		return aagent.getProperty(win.getWindowID(), property);
    	}
    	catch(SAFSRuntimeException x) { throw x; }
    	catch(Exception x){    		
    		throw new SAFSActionErrorRuntimeException(x.getMessage(), x);
    	}
    }
    
    /**
     * Retrieve the Caption of the object if one exits.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String the caption of the object or an empty String.
	 * 
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid object") if the provided parent cannot be located.
	 * @throws SAFSActionUnsupportedRuntimeException("Caption Unsupported") if the object does not provide a caption.
	 * @throws SAFSActionErrorRuntimeException(x.getMessage(), x) if an unexpected Exception is thrown from 
	 * @see org.safs.jvmagent.LocalServer#getCaption(Object)
     */
    public String getCaption(Object object){
    	try{
	    	AgentWindow win = (AgentWindow) object;
	    	// extract which Agent owns this object
	    	Agent aagent = (Agent) agents.get(win.getAgentID());
    		return aagent.getCaption(win.getWindowID());
    	}
    	catch(SAFSRuntimeException x) { throw x; }
    	catch(Exception x){    		
    		throw new SAFSActionErrorRuntimeException(x.getMessage(), x);
    	}
    }

    /**
     * Retrieve the name of the object if the object is named.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String the name of the object or an empty String.
	 * 
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid object") if the provided parent cannot be located.
	 * @throws SAFSActionUnsupportedRuntimeException("Name Unsupported") if the object does not provide a caption.
	 * @throws SAFSActionErrorRuntimeException(x.getMessage(), x) if an unexpected Exception is thrown from 
	 * @see org.safs.jvmagent.LocalServer#getName(Object)
     */
    public String getName(Object object){
    	try{
	    	AgentWindow win = (AgentWindow) object;
	    	// extract which Agent owns this object
	    	Agent aagent = (Agent) agents.get(win.getAgentID());
    		return aagent.getName(win.getWindowID());
    	}
    	catch(SAFSRuntimeException x) { throw x; }
    	catch(Exception x){    		
    		throw new SAFSActionErrorRuntimeException(x.getMessage(), x);
    	}
    }

    /**
     * Retrieve the ID of the object if the object has an ID.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String the ID of the object or an empty String.
	 * 
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid object") if the provided parent cannot be located.
	 * @throws SAFSActionUnsupportedRuntimeException("ID Unsupported") if the object does not provide a caption.
	 * @throws SAFSActionErrorRuntimeException(x.getMessage(), x) if an unexpected Exception is thrown from 
	 * @see org.safs.jvmagent.LocalServer#getID(Object)
     */
    public String getID(Object object){
    	try{
	    	AgentWindow win = (AgentWindow) object;
	    	// extract which Agent owns this object
	    	Agent aagent = (Agent) agents.get(win.getAgentID());
    		return aagent.getID(win.getWindowID());
    	}
    	catch(SAFSRuntimeException x) { throw x; }
    	catch(Exception x){    		
    		throw new SAFSActionErrorRuntimeException(x.getMessage(), x);
    	}
    }

    /**
     * Retrieve the displayed text value of the object if the object has a text value.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * 
	 * @return String the text value of the object.  Since text values can theoretically 
	 * legally be zero-length, a null value will be returned if no value exists for the 
	 * object.
	 * 
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid object") if the provided parent cannot be located.
	 * @throws SAFSActionUnsupportedRuntimeException("Text Unsupported") if the object does not provide a caption.
	 * @throws SAFSActionErrorRuntimeException(x.getMessage(), x) if an unexpected Exception is thrown from 
	 * @see org.safs.jvmagent.LocalServer#getText(Object)
     */
    public String getText(Object object){
    	try{
	    	AgentWindow win = (AgentWindow) object;
	    	// extract which Agent owns this object
	    	Agent aagent = (Agent) agents.get(win.getAgentID());
    		return aagent.getText(win.getWindowID());
    	}
    	catch(SAFSRuntimeException x) { throw x; }
    	catch(Exception x){    		
    		throw new SAFSActionErrorRuntimeException(x.getMessage(), x);
    	}
    }
    
    /**
     * Return the Z-Order level of the object (generally for a top level window).  
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return int z-order value of the object among all objects if this can be determined.
	 *          0 normally indicates the topmost Window.  
	 *          1 is normally the Window behind that, etc..
	 * 
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid object") if the provided parent cannot be located.
     * @throws SAFSActionUnsupportedRuntimeException("Level Unsupported") if the level cannot be determined.
     * @throws SAFSActionErrorRuntimeException if an unexpected (design?) problem occurs.
	 * @see org.safs.jvmagent.LocalServer#getLevel(Object)
     */
    public int getLevel(Object object){
    	try{
	    	AgentWindow win = (AgentWindow) object;
	    	// extract which Agent owns this object
	    	Agent aagent = (Agent) agents.get(win.getAgentID());
    		return aagent.getLevel(win.getWindowID());
    	}
    	catch(SAFSRuntimeException x) { throw x; }
    	catch(Exception x){    		
    		throw new SAFSActionErrorRuntimeException(x.getMessage(), x);
    	}
    }

    /**
     * Return true if the object is showing/visible.  
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return boolean true if the object is showing/visible.
	 * 
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid object") if the provided parent cannot be located.
     * @throws SAFSActionUnsupportedRuntimeException("Showing Unsupported") if the level cannot be determined.
     * @throws SAFSActionErrorRuntimeException if an unexpected (design?) problem occurs.
	 * @see org.safs.jvmagent.LocalServer#isShowing(Object)
     */
    public boolean isShowing(Object object){
    	try{
	    	AgentWindow win = (AgentWindow) object;
	    	// extract which Agent owns this object
	    	Agent aagent = (Agent) agents.get(win.getAgentID());
    		return aagent.isShowing(win.getWindowID());
    	}
    	catch(SAFSRuntimeException x) { throw x; }
    	catch(Exception x){    		
    		throw new SAFSActionErrorRuntimeException(x.getMessage(), x);
    	}
    }

    /**
     * Return true if the object is still valid/finadable in the Remote JVM.  
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return boolean true if the object is still valid/findable.
     * @throws SAFSActionErrorRuntimeException if an unexpected (design?) problem occurs.
	 * @see org.safs.jvmagent.LocalAgent#isValid(Object)
     */
    public boolean isValid(Object object){
    	try{
	    	AgentWindow win = (AgentWindow) object;
	    	// extract which Agent owns this object
	    	Agent aagent = (Agent) agents.get(win.getAgentID());
    		return aagent.isValid(win.getWindowID());
    	}
    	catch(SAFSRuntimeException x) { throw x; }
    	catch(Exception x){    		
    		throw new SAFSActionErrorRuntimeException(x.getMessage(), x);
    	}
    }

    /**
     * Return whatever data is extractable (normally visible) from the object.
     * Some objects like Lists may only require 1D of the 2D array.  Some objects like 
     * Tables will use the full 2D array.  Objects like Trees may use special techniques of 
     * storing their multi-dimensional data in the 2D array.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
     * 
     * @param dataInfo Allows us to specify what type of data we want returned.  This will be 
     * specific to the types of objects from which we extract data.  For example, Tables might 
     * provide "Contents" or "Headers" or other different types of data.
     * 
     * @return String[][] 2D array of extracted data.  0-length arrays if no data is available 
     * or the ability to extract data is not supported.
     * 
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid object") if the provided parent cannot be located.
     * @throws SAFSActionUnsupportedRuntimeException("StringData Unsupported") if action is not supported.
     * @throws SAFSInvalidActionArgumentRuntimeException(dataInfo) if specified dataInfo is not supported.
     * @throws SAFSActionErrorRuntimeException if an unexpected (design?) problem occurs.
	 * @see org.safs.jvmagent.LocalServer#getStringData(Object,Object)
     */
    public String[][] getStringData(Object object, Object dataInfo){
    	try{
	    	AgentWindow win = (AgentWindow) object;
	    	// extract which Agent owns this object
	    	Agent aagent = (Agent) agents.get(win.getAgentID());
    		return aagent.getStringData(win.getWindowID(), "Contents");
    	}
    	catch(SAFSRuntimeException x) { throw x; }
    	catch(Exception x){    		
    		throw new SAFSActionErrorRuntimeException(x.getMessage(), x);
    	}
    }
	
	/**
	 * Process the action provided in the testRecordData. 
	 * Subclasses must override this do-nothing method. 
	 * An Agent may throw various types of Agent-specific SAFSRuntimeExceptions depending upon failure modes.
	 * A Server should catch the RemoteException and Exception but test for subclasses of RuntimeException 
	 * and possibly let them through.
	 * @param object must be serializable.
	 * @param testRecordData provides all the information needed by the Agent to perform the action.  This must 
	 * be a serializable version of testRecordData.
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid object") if the provided parent cannot be located.
     * @throws SAFSRuntimeException subclasses from the various Agents.
     * @throws SAFSActionErrorRuntimeException if an unexpected (design?) problem occurs.
	 */
	public TestRecordData process (Object object, TestRecordData testRecordData){ 
    	try{
	    	AgentWindow win = (AgentWindow) object;
	    	// extract which Agent owns this object
	    	Agent aagent = (Agent) agents.get(win.getAgentID());
   			return aagent.process( (Integer)(win.getWindowID()), testRecordData);
    	}
    	catch(SAFSRuntimeException x) { throw x; }
    	catch(Exception x){    		
    		throw new SAFSActionErrorRuntimeException(x.getMessage(), x);
    	}
	}

	/**
	 * @param object reference AgentWindow from which to locate the subitem.
	 * @param index of the subitem to retrieve.
	 * @return subitem object AgentWindow or String
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid object") if the provided parent cannot be located.
	 * @throws SAFSSubItemsAgentUnsupportedRuntimeException as necessay.
	 * @throws IndexOutOfBoundsException if index is invalid
	 * @throws SAFSObjectNotFoundException("Invalid SubItem") if subitem at index cannot be retrieved.
     * @throws SAFSActionErrorRuntimeException if an unexpected (design?) problem occurs.
	 * @see SubItemsAgent#getSubItemAtIndex(Object,int)
	 */
	public Object getSubItemAtIndex(Object object, int index) throws Exception{
    	try{
	    	AgentWindow win = (AgentWindow) object;
	    	// extract which Agent owns this object
	    	Agent aagent = (Agent) agents.get(win.getAgentID());
			Object item = ((SubItemsAgent)aagent).getSubItemAtIndex(win.getWindowID(), index);
			if (item instanceof String) return item;
			return new AgentWindow(win.getAgentID(), (Integer) item);
    	}
    	catch(SAFSRuntimeException x) { throw x; }
    	catch(Exception x){    		
    		throw new SAFSActionErrorRuntimeException(x.getMessage(), x);
    	}
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
	 * @param theObject--Object proxy for the object to be evaluated.
	 * 
	 * @param thePath information to locate another object or subitem relative to theObject.
	 *        this is usually something like a menuitem or tree node where supported.
	 * 
	 * @return Object child sub-object found relative to theObject
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid object") if the provided parent cannot be located.
	 * @throws SAFSSubItemsAgentUnsupportedRuntimeException as necessay.
	 * @throws SAFSObjectNotFoundException("Invalid SubItem") if subitem at index cannot be retrieved.
     * @throws SAFSActionErrorRuntimeException if an unexpected (design?) problem occurs.
	 * @see org.safs.jvmagent.LocalServer#getMatchingPathObject(Object,String)
	 **/
	public Object getMatchingPathObject (Object object, String path){ 
    	try{
	    	AgentWindow win = (AgentWindow) object;
	    	// extract which Agent owns this object
	    	Agent aagent = (Agent) agents.get(win.getAgentID());
                Object id = aagent.getMatchingPathObject(win.getWindowID(), path);
    		return new AgentWindow( win.getAgentID( ), id );
    	}
    	catch(SAFSRuntimeException x) { throw x; }
    	catch(Exception x){    		
    		throw new SAFSActionErrorRuntimeException(x.getMessage(), x);
    	}
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
	 * @param theObject--Object proxy for the object to be evaluated.
	 * 
	 * @param thePath information to locate another object or subitem relative to theObject.
	 *        this is usually something like a menuitem or tree node where supported.
	 * 
	 * @return true if the child sub-object was found relative to theObject.
	 * @throws SAFSObjectNotFoundRuntimeException("Invalid object") if the provided parent cannot be located.
	 * @throws SAFSSubItemsAgentUnsupportedRuntimeException as necessay.
     * @throws SAFSActionErrorRuntimeException if an unexpected (design?) problem occurs.
	 * @see org.safs.jvmagent.LocalServer#isMatchingPath(Object,String)
	 **/
	public boolean isMatchingPath	(Object object, String path){ 
    	try{
	    	AgentWindow win = (AgentWindow) object;
	    	// extract which Agent owns this object
	    	Agent aagent = (Agent) agents.get(win.getAgentID());
    		return aagent.isMatchingPath(win.getWindowID(), path);
    	}
    	catch(SAFSRuntimeException x) { throw x; }
    	catch(Exception x){    		
    		throw new SAFSActionErrorRuntimeException(x.getMessage(), x);
    	}
	}

	/**
	 * Default implementation throws SAFSActionUnsupportedRuntimeException("runCommand Unsupported").
	 * @return empty String
	 * @throws SAFSActionUnsupportedRuntimeException("runCommand Unsupported");
	 * @see org.safs.rmi.engine.Server#runCommand(Object)
	 */
	public Object runCommand(Object command) throws RemoteException, Exception {
		System.out.println("ServerImple processing command: "+ command);
		throw new SAFSActionUnsupportedRuntimeException("runCommand Unsupported");
	}

	/**
	 * Remove this server object from the RMI registry.
	 * Tell every registered Agent to disconnect via its shutdown function and then clear the 
	 * list of registered Agents.
	 */
	protected void finalize() throws Exception{
		Naming.unbind(serverName);
		Enumeration enumerator = agents.elements();
		while( enumerator.hasMoreElements()){
			Agent anAgent = (Agent) enumerator.nextElement();
			anAgent.shutdown();
		}
		agents.clear();
	}


	/**
	 * Primarily a test entry point.
	 * Sets an RMI Security Manager, instance a ServerImpl object, and 
	 * binds to the RMI Registry with superclass rebindLocalRMIRegistry.
	 * <p>
	 * Example command-line invocation:
	 * <p>
	 * <pre><code>
	 * java -cp %CLASSPATH% -Djava.security.policy=%SAFSDIR%\lib\java.policy
	 *                      -Djava.rmi.server.hostname="thishost.internal.net" 
	 *                      org.safs.rmi.engine.ServerImpl
	 * </pre></code>
	 * @param args -- none
	 */
	public static void main(String[] args) throws Exception{
		System.out.println("Server App executing main()");
		System.setSecurityManager(new java.rmi.RMISecurityManager());
		String host = System.getProperty(Server.JAVA_RMI_SERVER_HOSTNAME_PROPERTY);
		if(host != null && host.length() > 0) serverHost = new String(host);
		System.out.println("Creating SAFS RMI Server at "+ serverHost);

		Log.ENABLED = false;
		STAFHelper.no_staf_handles = true;
		ServerImpl server = new ServerImpl();		
	}	

}
