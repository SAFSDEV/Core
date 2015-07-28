/** Copyright (C) (MSA, Inc) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

/** A simple extension stub for Application Object problems **/
public class SAFSObjectRecognitionException extends SAFSException {
	
	public SAFSObjectRecognitionException (String message){ super(message);}
	public SAFSObjectRecognitionException (Object obj, String message){ super(obj, message);}
	public SAFSObjectRecognitionException (Object obj, String method, String message){ super(obj, method, message);}
}

