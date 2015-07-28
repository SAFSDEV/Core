/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.jvmagent;

/**
 * @author canagl
 */
public class NoSuchPropertyException extends Exception {

	/**
	 * Constructor for NoSuchPropertyException.
	 */
	public NoSuchPropertyException() {
		super();
	}

	/**
	 * Constructor for NoSuchPropertyException.
	 * @param arg0
	 */
	public NoSuchPropertyException(String arg0) {
		super(arg0);
	}

	/**
	 * Constructor for NoSuchPropertyException.
	 * @param arg0
	 */
	public NoSuchPropertyException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * Constructor for NoSuchPropertyException.
	 * @param arg0
	 * @param arg1
	 */
	public NoSuchPropertyException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
