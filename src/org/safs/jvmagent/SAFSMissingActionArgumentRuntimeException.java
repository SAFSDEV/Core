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
public class SAFSMissingActionArgumentRuntimeException
	extends SAFSRuntimeException {

	/**
	 * Constructor for SAFSMissingActionArgumentRuntimeException.
	 * @param msg
	 */
	public SAFSMissingActionArgumentRuntimeException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for SAFSMissingActionArgumentRuntimeException.
	 * @param cause
	 */
	public SAFSMissingActionArgumentRuntimeException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor for SAFSMissingActionArgumentRuntimeException.
	 * @param msg
	 * @param cause
	 */
	public SAFSMissingActionArgumentRuntimeException(
		String msg,
		Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Constructor for SAFSMissingActionArgumentRuntimeException.
	 * @param cause
	 * @param msg
	 */
	public SAFSMissingActionArgumentRuntimeException(
		Throwable cause,
		String msg) {
		super(cause, msg);
	}

}
