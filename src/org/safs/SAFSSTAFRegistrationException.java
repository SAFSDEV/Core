/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

/** A simple extension stub for STAF Registration problems **/
public class SAFSSTAFRegistrationException extends SAFSException {
	
	/** -32767, unset error code. Otherwise, error code from exception source.*/
	public int rc = -1;
	public SAFSSTAFRegistrationException (String message){ super(message);}
	public SAFSSTAFRegistrationException (int anRC, String message){ super(message); rc=anRC;}
	public SAFSSTAFRegistrationException (Object obj, String message){ super(obj, message);}
	public SAFSSTAFRegistrationException (int anRC, Object obj, String message){ super(obj, message); rc=anRC;}
	public SAFSSTAFRegistrationException (Object obj, String method, String message){ super(obj, method, message);}
	public SAFSSTAFRegistrationException (int anRC, Object obj, String method, String message){ super(obj, method, message); rc=anRC;}
}

