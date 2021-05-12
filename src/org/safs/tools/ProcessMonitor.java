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

import org.safs.Log;
import org.safs.tools.consoles.GenericProcessCapture;
import org.safs.tools.consoles.ProcessCapture;

/**
 * Various utilities for monitoring or otherwise interrogating native system processes.
 * This subclass writes to org.safs.Log instead of to System.out and uses the SAFS 
 * ProcessCapture class instead of GenericProcessCapture class to also enable SAFS 
 * Debug Logging.
 * 
 * @author Carl Nagle 2011.12.23 Original Release 
 */
public class ProcessMonitor extends GenericProcessMonitor{

	/**
	 * Writes to Log.debug .
	 * @param message
	 */
	protected static void debug(String message){
		Log.debug(message);
	}
	
	/**
	 * Subclasses may wish to override to return a different subclass of GenericProcessCapture.
	 * @param aproc
	 * @return 
	 */
	protected static GenericProcessCapture getProcessCapture(Process aproc){
		return new ProcessCapture(aproc);
	}
	
}
