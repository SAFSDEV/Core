/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

/** A simple extension stub for verification problems **/
public class SAFSVerificationException extends SAFSException {

	public SAFSVerificationException (String message){ super(message);}
	public SAFSVerificationException (Object obj, String message){ super(obj, message);}
	public SAFSVerificationException (Object obj, String method, String message){ super(obj, method, message);}
}

