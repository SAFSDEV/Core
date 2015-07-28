/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.sockets;

/**
 * @author Carl Nagle, SAS Institute, Inc.
 */
public class ShutdownInvocationException extends Exception {

	private boolean isRemoteShutdown = false;
	private int cause = -1;

	private ShutdownInvocationException(){}
	
	/**
	 * Constructor initializes the custom message and sets the local or remote 
	 * exception type and cause.
	 * @param message
	 * @param isRemote
	 */
	public ShutdownInvocationException(String message, boolean isRemote, int cause) {
		super(message);
		isRemoteShutdown = isRemote;
		this.cause = cause;
	}
	
	public boolean isRemoteShutdown() { return isRemoteShutdown; }

	public boolean isLocalShutdown() { return !isRemoteShutdown; }
	
	public int getShutdownCause(){ return cause; }
}
