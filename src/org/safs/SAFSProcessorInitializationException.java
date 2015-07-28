/** 
 * @author  Carl Nagle
 * @since   NOV 10, 2003
 *   <br>   NOV 10, 2003    (CANAGL) Original Release
 *
 * Copyright (C) (SAS Institute) All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

/** 
 * Flags the ommission of proper initialization of a ProcessRequester.
 * 
 * ProcessRequester initialization errors are considered a catastrophic engine failure.
 **/
public class SAFSProcessorInitializationException extends RuntimeException {
	
	public SAFSProcessorInitializationException (String message){ super(message);}
}

