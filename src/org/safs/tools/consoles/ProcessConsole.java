/** Copyright (C) SAS Institute All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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

