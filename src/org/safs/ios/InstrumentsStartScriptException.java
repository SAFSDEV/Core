/** Copyright (C) SAS Institute All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs.ios;

import org.safs.SAFSException;

/**
 * Thrown when we detect we have not successfully executed StartScript on IOS Instruments.
 */
public class InstrumentsStartScriptException extends SAFSException {


	  /**
	   * Constructor takes the comment concerning the nature of the Exception.
	   * 
	   * @param String msg, comment about the nature of the Exception
	   **/
	public InstrumentsStartScriptException(String msg) {
		super(msg);
	}

	  /**
	   * Constructor takes a 'this' reference FROM the caller as well as the comment.
	   * 
	   * @param Object, normally the caller ('this') so that we can add their getClass().getName() to the msg; 
	   * if null then not used.
	   * @param String msg, comment about the nature of the Exception
	   **/
	public InstrumentsStartScriptException(Object obj, String msg) {
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
	public InstrumentsStartScriptException(Object obj, String methodName,
			String msg) {
		super(obj, methodName, msg);
	}

}
