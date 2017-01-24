/** Copyright (C) (SAS Institute) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs;

public class SAFSPersistableNotEnableException extends SAFSException {
	
	public SAFSPersistableNotEnableException (String message){ super(message);}
	public SAFSPersistableNotEnableException (Object obj, String message){ super(obj, message);}
	public SAFSPersistableNotEnableException (Object obj, String method, String message){ super(obj, method, message);}
}

