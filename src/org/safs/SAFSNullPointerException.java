/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

/** A simple extension stub for Application Object problems **/
public class SAFSNullPointerException extends SAFSException {
	
	public SAFSNullPointerException (String message){ super(message);}
	public SAFSNullPointerException (Object obj, String message){ super(obj, message);}
	public SAFSNullPointerException (Object obj, String method, String message){ super(obj, method, message);}
}

