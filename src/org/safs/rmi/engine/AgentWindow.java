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
