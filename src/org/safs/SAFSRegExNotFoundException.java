/** 
 * @author  Carl Nagle
 * @since   NOV 04, 2003
 *   <br>   NOV 04, 2003    (Carl Nagle) Original Release
 *
 * @author Carl Nagle, NOV 05, 2003
 *         Now extends RuntimeException instead of SAFSException
 *
 * Copyright (C) (SAS Institute) All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/

package org.safs;

/** 
 * Flags the ommission of support for Regular Expressions needed by StringUtils 
 * and other classes.
 * 
 * Regular Expression support is part of core Java in V1.4 and later.  For pre-V1.4 
 * installations we can use the Jakarta regular expression support provided by 
 * the RegEx package downloaded from the Jakarta website: 
 * {@link http://jakarta.apache.org/regexp/index.html}
 * 
 * Missing Regular Expression support is considered a catastrophic engine failure.
 **/
public class SAFSRegExNotFoundException extends RuntimeException {
	
	public SAFSRegExNotFoundException (String message){ super(message);}
}

