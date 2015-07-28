/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.jvmagent;

/**
 * @author Carl Nagle
 */
public class InvalidAgentException extends Exception {

	/**
	 * Constructor for InvalidAgentException.
	 */
	public InvalidAgentException() {
		super();
	}

	/**
	 * Constructor for InvalidAgentException.
	 * @param arg0
	 */
	public InvalidAgentException(String arg0) {
		super(arg0);
	}

	/**
	 * Constructor for InvalidAgentException.
	 * @param arg0
	 */
	public InvalidAgentException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * Constructor for InvalidAgentException.
	 * @param arg0
	 * @param arg1
	 */
	public InvalidAgentException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}