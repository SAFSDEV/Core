/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.sockets;

/**
 * General-Purpose exception thrown to indicate something bad happened on "the other side".
 * <p>
 * Receipt of this on the remote controller side indicates an Exception occurred or was reported by 
 * the remote client.
 * 
 * @author Carl Nagle, SAS Institute, Inc.
 */
public class RemoteException extends Exception {

	public RemoteException() {
		super();
	}

	public RemoteException(String message, Throwable cause) {
		super(message, cause);
	}

	public RemoteException(String message) {
		super(message);
	}

	public RemoteException(Throwable cause) {
		super(cause);
	}

}
