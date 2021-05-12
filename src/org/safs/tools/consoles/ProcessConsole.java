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
package org.safs.tools.consoles;

import java.lang.Process;

import org.safs.Log;

/**
 * Simply overrides the GenericProcessConsole debug() method to use the SAFS Debug Log.
 * @see GenericProcessConsole#debug(String)
 * @see #debug(String)
 */
public class ProcessConsole extends GenericProcessConsole{

	/**
	 * Constructor for ProcessConsole
	 * @see #setShowOutStream(boolean)
	 * @see #setShowErrStream(boolean)
	 */
	public ProcessConsole(Process process) {
		super(process);
	}

	/**
	 * Writes to {@link org.safs.Log#debug(Object)}--the SAFS Debug Log.
	 * Subclasses should override to log to alternate sinks.
	 * @param message
	 */
	protected void debug(String message){
		Log.debug(message);
	}
}

