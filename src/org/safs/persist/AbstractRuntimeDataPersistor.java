/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 02, 2016    (SBJLWA) Initial release.
 */
package org.safs.persist;

import org.safs.SAFSException;
import org.safs.SAFSNullPointerException;
import org.safs.tools.RuntimeDataInterface;

/**
 * A Persistor with RuntimeDataInterface providing underlying functionality.
 * @author sbjlwa
 */
public abstract class AbstractRuntimeDataPersistor implements Persistor{
	protected RuntimeDataInterface runtime = null;
	
	public AbstractRuntimeDataPersistor(RuntimeDataInterface runtime){
		this.runtime = runtime;
	}

	public void persist(Persistable persistable) throws SAFSException {
		if(persistable==null){
			throw new SAFSNullPointerException("The persistable object is null.");
		}
		if(!persistable.isEnabled()){
			throw new SAFSException("This persistable object is not enabled, so it cannot be persisted.");
		}
	}
}
