/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.jvmagent;

import org.safs.SAFSRuntimeException;

/**
 * 
 * @author canagl
 * @since Apr 5, 2005
 */
public class SAFSInvalidActionArgumentRuntimeException
	extends SAFSRuntimeException {

	/**
	 * Constructor for SAFSInvalidActionArgumentRuntimeException.
	 * @param msg
	 */
	public SAFSInvalidActionArgumentRuntimeException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for SAFSInvalidActionArgumentRuntimeException.
	 * @param cause
	 */
	public SAFSInvalidActionArgumentRuntimeException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor for SAFSInvalidActionArgumentRuntimeException.
	 * @param msg
	 * @param cause
	 */
	public SAFSInvalidActionArgumentRuntimeException(
		String msg,
		Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Constructor for SAFSInvalidActionArgumentRuntimeException.
	 * @param cause
	 * @param msg
	 */
	public SAFSInvalidActionArgumentRuntimeException(
		Throwable cause,
		String msg) {
		super(cause, msg);
	}

}
