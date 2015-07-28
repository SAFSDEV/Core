/** Copyright (C) (SAS Institute) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

/** A simple extension stub for Application Object problems **/
public class SAFSRegExException extends SAFSException {
	
	public SAFSRegExException (String message){ super(message);}
	public SAFSRegExException (Object obj, String message){ super(obj, message);}
	public SAFSRegExException (Object obj, String method, String message){ super(obj, method, message);}
}

