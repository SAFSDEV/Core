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
public interface DebugInterface {

	/**
	 * During debugging, do we STOP at BP (Breakpoint) records?
	 */
	public boolean isBreakpointEnabled();

	/**
	 * During debugging, do we STOP on EVERY record?
	 */
	public boolean isRecordDebugEnabled();

	/**
	 * During debugging, do we STOP on C,CW,CF Driver Command records?
	 */
	public boolean isCommandDebugEnabled();

	/**
	 * During debugging, do we STOP on T,TW,TF Test records?
	 */
	public boolean isTestRecordDebugEnabled();

	/**
	 * Enable/Disable BP (Breakpoint) record debugging.
	 */
	public void setBreakpointEnabled(boolean enabled);

	/**
	 * Enable/Disable debugging at EVERY record.
	 */
	public void setRecordDebugEnabled(boolean enabled);

	/**
	 * Enable/Disable C,CW,CF (Driver Command) record debugging.
	 */
	public void setCommandDebugEnabled(boolean enabled);

	/**
	 * Enable/Disable T,TW,TF (Test) record debugging.
	 */
	public void setTestRecordDebugEnabled(boolean enabled);

}

