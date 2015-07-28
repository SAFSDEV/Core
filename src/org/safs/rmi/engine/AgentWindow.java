/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.rmi.engine;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.io.Serializable;

/**
 * Class used to store retrieved Window and Component pseudo-references with 
 * an ObjID of the Agent that owns the Window or Component.  This is used 
 * during find operations for Windows and Components.
 * 
 * @author Carl Nagle
 * @since Apr 5, 2005
 */
public class AgentWindow implements Serializable {

	private ObjID agentID;
	private Object windowID;

	/**
	 * Constructor for AgentWindow.
	 */
	public AgentWindow() {
		super();
	}
	
	/**
	 * @param theWindow must be Serializable
	 */
	public AgentWindow (ObjID theAgent, Object theWindow)
	{
		agentID  = theAgent;
		windowID = theWindow;
	}

	public ObjID getAgentID()  { return agentID;  }
	public void setAgentID(ObjID id){ agentID = id;}
	
	/**
	 * @return typically a Serializable object
	 */
	public Object getWindowID(){ return windowID; }
	/**
	 * @param theWindow must be Serializable
	 */
	public void setWindowID(Object id){ windowID = id;}
}
