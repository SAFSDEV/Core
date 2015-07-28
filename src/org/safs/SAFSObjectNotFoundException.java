/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

/** A simple extension stub for Application Object problems **/
public class SAFSObjectNotFoundException extends SAFSException {
	
	public SAFSObjectNotFoundException (String message){ super(message);}
	public SAFSObjectNotFoundException (Object obj, String message){ super(obj, message);}
	public SAFSObjectNotFoundException (Object obj, String method, String message){ super(obj, method, message);}
}

