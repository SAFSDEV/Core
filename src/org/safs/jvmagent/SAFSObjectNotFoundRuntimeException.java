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
public class SAFSObjectNotFoundRuntimeException extends SAFSRuntimeException {

	/**
	 * Constructor for SAFSObjectNotFoundRuntimeException.
	 * @param msg
	 */
	public SAFSObjectNotFoundRuntimeException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for SAFSObjectNotFoundRuntimeException.
	 * @param cause
	 */
	public SAFSObjectNotFoundRuntimeException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor for SAFSObjectNotFoundRuntimeException.
	 * @param msg
	 * @param cause
	 */
	public SAFSObjectNotFoundRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Constructor for SAFSObjectNotFoundRuntimeException.
	 * @param cause
	 * @param msg
	 */
	public SAFSObjectNotFoundRuntimeException(Throwable cause, String msg) {
		super(cause, msg);
	}

}
