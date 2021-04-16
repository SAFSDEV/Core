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
package org.safs.tools.status;

import org.safs.tools.ConfigurableToolsInterface;
import org.safs.tools.CoreInterface;
import org.safs.tools.counters.CountersInterface;
import org.safs.tools.vars.VarsInterface;

public class SAFSSTATUS implements ConfigurableToolsInterface, StatusMonitorInterface {

	protected CountersInterface counters = null;
	protected VarsInterface     vars = null;
	
	/**
	 * Constructor for SAFSSTATUS
	 */
	public SAFSSTATUS() {
		super();
	}

	/**
	 * Expects a DriverInterface for initialization.
	 * @see ConfigurableToolsInterface#launchInterface(Object)
	 */
	public void launchInterface(Object configInfo) {
	}

    /** 
     * TRUE if the underlying tool is running.
     * Typically called AFTER initialization attempts have been made.
     */
	public boolean isToolRunning(){
		return false;
	}
	
	/**
	 * @see GenericToolsInterface#reset()
	 */
	public void reset() {
	}

	/**
	 * @see GenericToolsInterface#shutdown()
	 */
	public void shutdown() {
	}


	/**
	 * @see GenericToolsInterface#getCoreInterface()
	 * @throws IllegalStateException ALWAYS since there is no CoreInterface in this class.
	 */
	public CoreInterface getCoreInterface() throws IllegalStateException {
		throw new IllegalStateException("SAFSSTATUS does not interface with the core framework directly.");
	}
	
	/**
	 * @see StatusMonitorInterface#getStatusInfo(String)
	 */
	public StatusInfoInterface getStatusInfo(String testLevel) {
		return null;
	}

	/**
	 * @see StatusMonitorInterface#setStatusInfo(String, StatusInfoInterface)
	 */
	public void setStatusInfo(String testLevel, StatusInfoInterface statusInfo) {
	}

}

