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
package org.safs.tools.drivers;
public class DebugInfo implements DebugInterface {

	protected boolean breakpointsOn  = false;
	protected boolean recordDebugOn  = false;
	protected boolean commandDebugOn = false;
	protected boolean testDebugOn    = false;

	/**
	 * Constructor for DebugInfo
	 */
	public DebugInfo() {
		super();
	}

	/**
	 * @see DebugInterface#isBreakpointEnabled()
	 */
	public boolean isBreakpointEnabled() {
		return breakpointsOn;
	}

	/**
	 * @see DebugInterface#isRecordDebugEnabled()
	 */
	public boolean isRecordDebugEnabled() {
		return recordDebugOn;
	}

	/**
	 * @see DebugInterface#isCommandDebugEnabled()
	 */
	public boolean isCommandDebugEnabled() {
		return commandDebugOn;
	}

	/**
	 * @see DebugInterface#isTestRecordDebugEnabled()
	 */
	public boolean isTestRecordDebugEnabled() {
		return testDebugOn;
	}

	/**
	 * @see DebugInterface#setBreakpointEnabled(boolean)
	 */
	public void setBreakpointEnabled(boolean enabled) { breakpointsOn=enabled;}

	/**
	 * @see DebugInterface#setRecordDebugEnabled(boolean)
	 */
	public void setRecordDebugEnabled(boolean enabled) { recordDebugOn=enabled;}

	/**
	 * @see DebugInterface#setCommandDebugEnabled(boolean)
	 */
	public void setCommandDebugEnabled(boolean enabled) { commandDebugOn=enabled;}

	/**
	 * @see DebugInterface#setTestRecordDebugEnabled(boolean)
	 */
	public void setTestRecordDebugEnabled(boolean enabled) { testDebugOn=enabled;}

}

