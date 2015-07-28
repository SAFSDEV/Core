/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

public class SAFSParamException extends SAFSException{

	public SAFSParamException(String msg) {
		super(msg);
	}

	public SAFSParamException(Object obj, String methodName, String msg) {
		super(obj, methodName, msg);
	}

	public SAFSParamException(Object obj, String msg) {
		super(obj, msg);
	}

}
