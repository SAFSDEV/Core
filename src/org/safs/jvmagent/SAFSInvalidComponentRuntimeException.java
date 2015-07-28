/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.jvmagent;

import org.safs.SAFSRuntimeException;

/**
 * 
 * @author canagl
 * @since Apr 1, 2005
 */
public class SAFSInvalidComponentRuntimeException
	extends SAFSRuntimeException {

	/**
	 * Constructor for SAFSInvalidComponentRuntimeException.
	 * @param msg
	 */
	public SAFSInvalidComponentRuntimeException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for SAFSInvalidComponentRuntimeException.
	 * @param cause
	 */
	public SAFSInvalidComponentRuntimeException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor for SAFSInvalidComponentRuntimeException.
	 * @param msg
	 * @param cause
	 */
	public SAFSInvalidComponentRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Constructor for SAFSInvalidComponentRuntimeException.
	 * @param cause
	 * @param msg
	 */
	public SAFSInvalidComponentRuntimeException(Throwable cause, String msg) {
		super(cause, msg);
	}

}
