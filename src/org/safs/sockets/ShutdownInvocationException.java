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
