/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.jvmagent;

import org.safs.SAFSRuntimeException;

/**
 * 
 * @author Carl Nagle
 * @since Apr 1, 2005
 */
public class SAFSInvalidActionRuntimeException extends SAFSRuntimeException {

	/**
	 * Constructor for SAFSInvalidActionRuntimeException.
	 * @param msg
	 */
	public SAFSInvalidActionRuntimeException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for SAFSInvalidActionRuntimeException.
	 * @param cause
	 */
	public SAFSInvalidActionRuntimeException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor for SAFSInvalidActionRuntimeException.
	 * @param msg
	 * @param cause
	 */
	public SAFSInvalidActionRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Constructor for SAFSInvalidActionRuntimeException.
	 * @param cause
	 * @param msg
	 */
	public SAFSInvalidActionRuntimeException(Throwable cause, String msg) {
		super(cause, msg);
	}

}
