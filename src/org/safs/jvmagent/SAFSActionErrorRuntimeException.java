/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
