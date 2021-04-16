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
package org.safs.tools;

public interface GenericToolsInterface {
	
	/** 
	 * Reset or clear any cached information in the underlying tool or service.
     * The implementation should be able to recognize if the underlying tool or service 
     * is not actually running.  If the tool is not running, then this should exit 
     * gracefully without adversely affecting anything. **/
	public void reset ();
	
    /** 
     * Shutdown the underlying tool or service. 
     * The implementation should be able to recognize if the underlying tool or service 
     * is not actually running.  If the tool is not running, then this should exit 
     * gracefully without adversely affecting anything. **/
	public void shutdown ();
	
    /** 
     * TRUE if the underlying tool is running.
     * Typically called AFTER initialization attempts have been made.
     */
	public boolean isToolRunning();
	
	/**
	 * Retrieve the instance of the CoreInterface used to communicate with the framework.
	 * @return CoreInterface instance.
	 * @throws IllegalStateException if the CoreInterface is null, has not been initialized yet, or is otherwise invalid.
	 */
	public CoreInterface getCoreInterface() throws IllegalStateException;
}

