/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.rmi.engine;

import java.lang.NoSuchFieldException;
import java.rmi.*;
import java.rmi.server.*;
import org.safs.TestRecordData;

/**
 * Defines a generic SAFS Engine RMI Agent communicating with a SAFS Engine RMI Server.
 * <p>
 * A SAFS Engine RMI Server is the centralized controller that will talk with each RMI Agent 
 * embedded within each enabled JVM.  This provides an RMI alternative to STAF for multi-JVM 
 * communications between the very visible SAFS Engine and the invisible Agents in each JVM.
 * <p>
 * The typical scenario here would be that a SAFS Engine like SAFS/Abbot would still present 
 * a single STAF-based event-driven interface for the TID and any other Driver wishing to 
 * use it, but underneath the engine will be communicating with multiple JVMs over localhost RMI.
 * <p>
 * The RMI Agent is normally automatically loaded into each enabled JVM as a Java Extension 
 * exploiting the Accessibility hooks in the JVM.  This will be done through the SAFS Bootstrap 
 * AgentClassLoader.
 * <p>
 * This is somewhat analogous to how other tools like RobotJ, Robot, and WinRunner communicate 
 * with all running JVMs through a single script engine or controller.
 * <p>
 * Note: For consistent operation the <code>safs.server.hostname</code> needs to be set in code or 
 * on the command line before launching an Agent connecting to a SAFS RMI Server:
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
 * @author Carl Nagle FEB 01, 2005 Original Release
 * @see org.safs.jvmagent.Bootstrap
 * @see org.safs.jvmagent.AgentClassLoader
 */
public interface Agent extends Remote {
	
	/** 
	 * 'safs.server.hostname' Name of the System Property signifying a remote SAFS RMI Server 
	 * hostname to seek for a SAFS RMI Server.
	 */
	public static final String SERVER_HOSTNAME_SYSTEM_PROPERTY = "safs.server.hostname";
	
	/**
	 * A simple RUThere call.  The server will generally receive a ConnectException 
	 * thrown by RMI if the Agent cannot be called because it has gone away.
	 */
	public void ping () throws RemoteException;
	
	/**
	 * Returns an ObjID to uniquely identify the JVM Agent.
	 */
	public ObjID getAgentID() throws RemoteException;

	/**
	 * Returns the name or remoteType of the JVM Agent.
	 */
	public String getAgentName() throws RemoteException;

	/**
	 * Process the action provided in the testRecordData.  
	 * <p>
	 * The Agent is not intended to be logging to STAF.  Each Agent is intended to be strictly 
	 * an RMI client that will attempt actions and return success or failure information to 
	 * the controlling SAFS Engine RMI Server.
	 * 
	 * @param testRecordData provides all the information needed by the Agent to perform an action.
	 * The RMI version of TestRecordData is likely going to be much sparser than the 
	 * typical TestRecordHelper.  It will also be pretty specific to the concrete 
	 * implementations for the Server and Agent.	 
	 * @return TestRecordData which includes the statuscode
	 */
    public TestRecordData process(Object object, TestRecordData testRecordData) throws RemoteException, Exception;
    
    /**
     * The RMI Server will instruct the Agent to shutdown when the Server itself is being 
     * shutdown or finalized.  The Agent does not have to unRegister from the Server in this 
     * case because the Server will already be disconnecting from the Agent.
     * <p>
     * The Agent should accept the shutdown request and initiate a new shutdown Thread so 
     * that the call to shutdown from the Server can immediately return.
     * <p>
     * The Agent will normally reset itself to an idle state where it is polling once again 
     * for the existence of another RMI Server object in the RMI Registry.
     */
    public void shutdown() throws RemoteException;
    
	/**
	 * Return the number of currently active Top Level Windows.
	 */
    public int getTopLevelCount() throws RemoteException, Exception;

    /**
     * Return an array representing the TopLevel windows in the Agent JVM.
     * 
     * @return Object[] representing all active top level windows in the Agent JVM.
     * This array will be Server/Agent specific and may be nothing more than arrays of the 
     * hashcodes used to uniquely identify objects in an Agent-maintained Hashtable.  
     * A zero-length array will be returned if no Top Level Windows are active.
     */
    public Object[] getTopLevelWindows() throws RemoteException, Exception;

	/**
	 * Return the number of children available in the provided parent.
	 */
    public int getChildCount(Object parent) throws RemoteException, Exception;
    
	/**
	 * Return an array representing the children of the provided parent object.
     * 
     * @param parent An object from getTopLevelWindows or from a previous call to getChildren.
	 * The parent is often one of the elements of the TopLevelWindow array or somewhere 
	 * lower in that same hierarchy.
	 * 
     * @return Object[] representing all known children of the provided parent.
     * This array will be Server/Agent specific and may be nothing more than arrays of the 
     * hashcodes used to uniquely identify objects in an Agent-maintained Hashtable.
     * A zero-length array will be returned if the parent has no children.
	 */
    public Object[] getChildren(Object parent) throws RemoteException, Exception;
    
    /**
     * Retrieve the Caption of the object if one exits.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String the caption of the object.
	 * @throws NoSuchFieldException if the object does not provide a caption.
     */
    public String getCaption(Object object) throws RemoteException, NoSuchFieldException, Exception;

    /**
     * Retrieve the name of the object if the object is named.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String the name of the object.
	 * @throws NoSuchFieldException if the object does not provide a name.
     */
    public String getName(Object object) throws RemoteException, NoSuchFieldException, Exception;

    /**
     * Retrieve the ID of the object if the object has an ID.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String the ID of the object.
	 * @throws NoSuchFieldException if the object does not provide an ID.
     */
    public String getID(Object object) throws RemoteException, NoSuchFieldException, Exception;

    /**
     * Retrieve the displayed text value of the object if the object has a text value.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * 
	 * @return String the text value of the object.  Since text values can theoretically 
	 * legally be zero-length, a null value will be returned if no value exists for the 
	 * object.
     */
    public String getText(Object object) throws RemoteException, Exception;
    
    /**
     * Retrieve the list of available properties for the object.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * 
	 * @return String[] the names of available properties.
     */
    public String[] getPropertyNames(Object object) throws RemoteException, Exception;
    
    /**
     * Retrieve the property value of the object if the object has the property.
     * 
     * @param object -- An object from getTopLevelWindows or from a previous call to getChildren.
	 * @param property -- the case-sensitive name of the property to seek.
	 * 
	 * @return String the text value of the object property.  Since property values can theoretically 
	 * legally be zero-length, a null value will be returned if no value exists for the 
	 * object.
     */
    public String getProperty(Object object, String property) throws RemoteException, NoSuchFieldException, Exception;
    
    /**
     * Return the Class name of the object.  
     * For example, we may get "javax.swing.JFrame" or the name of the subclass if 
     * it is a subclass of JFrame.
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String Class name of the object.
     */
    public String getClassName(Object object) throws RemoteException, Exception;

    /**
     * Return the Z-Order level of the object (generally for a top level window).  
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return int z-order value of the object among all objects if this can be determined.
	 *          0 normally indicates the topmost Window.  
	 *          1 is normally the Window behind that, etc..
     */
    public int   getLevel(Object object) throws RemoteException, NoSuchFieldException, Exception;

    /**
     * Return the array of all superclass names for the object.  
     * This should return the Class hierarchy for the object all the way to Class Object. 
     * A 0-length array will be returned if there are none (Class Object).
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return String[] Class names for the superclass hierarchy.
     */
    public String[] getSuperClassNames(Object object) throws RemoteException, Exception;
    
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
     * @throws NoSuchMethodException if the object type does not support the extraction of data 
     * or the specific type of data requested.
     */
    public String[][] getStringData(Object object, Object dataInfo) throws RemoteException, NoSuchMethodException, Exception;
    
	/**
	 * Return true if the specified object is showing/visible.
	 * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return boolean
	 */
	public boolean isShowing(Object object) throws RemoteException, Exception;

    /**
     * Return true if the object is still valid/finadable in the JVM.  
     * 
     * @param object An object from getTopLevelWindows or from a previous call to getChildren.
	 * @return boolean true if the object is still valid/findable.
     */
    public boolean isValid(Object object) throws RemoteException, Exception;

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
	 **/
	public Object getMatchingPathObject (Object theObject, String thePath) throws RemoteException, Exception;


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
	 **/
	public boolean isMatchingPath	(Object theObject, String thePath) throws RemoteException, Exception;

    /**
     * This is a "do anything" function that the Server and Clients have a private contract 
     * to implement.  It essentially allows a Client/Server implementation to pass anything 
     * back and forth and act according to their shared designs.
     * <p>
     * For example, the Client and Server may have been coded to pass string commands back 
     * and forth and the string commands can be parsed to provide an unlimited number of 
     * command possibilities.
     * 
     * @param command Object of a type expected by the Client implementation for this method.
     * @return Object of a type expected by the Server implementation calling this method.
     */
    public Object runCommand(Object command) throws RemoteException, Exception;

    /**
     * Attempts to set theObject as the active (topmost?) Window or Component in the JVM.
     * @param theObject Object of a type expected by the Client implementation for this method.
     */
    public void setActiveWindow(Object theObject) throws RemoteException, Exception;
}
