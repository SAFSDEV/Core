/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.jvmagent;

import org.safs.SAFSRuntimeException;

/**
 * 
 * @author Carl Nagle
 * @since Apr 7, 2005
 */
public class SAFSSubItemsAgentUnsupportedRuntimeException extends SAFSRuntimeException {

	/**
	 * Constructor for SAFSSubItemsAgentUnsupported.
	 * @param msg
	 */
	public SAFSSubItemsAgentUnsupportedRuntimeException(String msg) {
		super(msg);
	}

  /** <br><em>Purpose:</em> constructor
   * @param  cause, Throwable 'cause' to pass along to our 'super'.
   * @param  msg, String, the string to pass along to our 'super'
   **/
  public SAFSSubItemsAgentUnsupportedRuntimeException ( Throwable cause) {
    super(cause);
  }

  /** <br><em>Purpose:</em> constructor
   * @param  cause, Throwable 'cause' to pass along to our 'super'.
   * @param  msg, String, the string to pass along to our 'super'
   **/
  public SAFSSubItemsAgentUnsupportedRuntimeException ( Throwable cause, String msg) {
    super(msg, cause);
  }

  /** <br><em>Purpose:</em> constructor
   * @param  cause, Throwable 'cause' to pass along to our 'super'.
   * @param  msg, String, the string to pass along to our 'super'
   **/
  public SAFSSubItemsAgentUnsupportedRuntimeException ( String msg, Throwable cause) {
    super(msg, cause);
  }

}
