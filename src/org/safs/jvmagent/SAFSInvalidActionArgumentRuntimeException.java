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
