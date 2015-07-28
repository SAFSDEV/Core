/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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
