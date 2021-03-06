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
package org.safs.android;

import org.safs.SAFSException;

/**
 * Exceptions relating to the transfer of TestRecordData between local SAFS Java and remote Droid Java.
 */
public class DroidTestRecordDataException extends SAFSException {

	  /**
	   * Constructor using the standard Exception message info only.
	   * 
	   * @param String msg, comment about the nature of the Exception
	   **/
	public DroidTestRecordDataException(String msg) {
		super(msg);
	}

	  /**
	   * Constructor takes a 'this' reference FROM the caller as well as the standard Exception message.
	   * 
	   * @param Object, normally the caller ('this') so that we can add their getClass().getName() to the msg; 
	   * if null then not used.
	   * @param String msg, comment about the nature of the Exception
	   **/
	public DroidTestRecordDataException(Object obj, String msg) {
		super(obj, msg);
	}

	  /**
	   * Constructor takes a 'this' reference FROM the caller as well as the name of the 
	   * active Method at the time of the Exception.
	   * 
	   * @param Object, normally the caller ('this') so that we can add their getClass().getName() to the msg; 
	   * if null then not used.
	   * @param String methodName, name of the method to make part of msg.
	   * @param String msg, comment about the nature of the Exception
	   **/
	public DroidTestRecordDataException(Object obj, String methodName,
			String msg) {
		super(obj, methodName, msg);
	}
}
