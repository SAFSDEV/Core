/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.jvmagent;

import org.safs.SAFSRuntimeException;

/**
 * 
 * @author Carl Nagle
 * @since Mar 30, 2005
 */
public class SAFSActionUnsupportedRuntimeException
	extends SAFSRuntimeException {

	/**
	 * Constructor for SAFSActionUnsupportedRuntimeException.
	 * @param msg
	 */
	public SAFSActionUnsupportedRuntimeException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for SAFSActionUnsupportedRuntimeException.
	 * @param cause
	 */
	public SAFSActionUnsupportedRuntimeException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor for SAFSActionUnsupportedRuntimeException.
	 * @param msg
	 * @param cause
	 */
	public SAFSActionUnsupportedRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Constructor for SAFSActionUnsupportedRuntimeException.
	 * @param cause
	 * @param msg
	 */
	public SAFSActionUnsupportedRuntimeException(Throwable cause, String msg) {
		super(cause, msg);
	}

}
