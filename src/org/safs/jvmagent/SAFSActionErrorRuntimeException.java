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
public class SAFSActionErrorRuntimeException extends SAFSRuntimeException {

	/**
	 * Constructor for SAFSActionErrorRuntimeException.
	 * @param msg
	 */
	public SAFSActionErrorRuntimeException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for SAFSActionErrorRuntimeException.
	 * @param cause
	 */
	public SAFSActionErrorRuntimeException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor for SAFSActionErrorRuntimeException.
	 * @param msg
	 * @param cause
	 */
	public SAFSActionErrorRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Constructor for SAFSActionErrorRuntimeException.
	 * @param cause
	 * @param msg
	 */
	public SAFSActionErrorRuntimeException(Throwable cause, String msg) {
		super(cause, msg);
	}

}
