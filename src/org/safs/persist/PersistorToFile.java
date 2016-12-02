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
import org.safs.tools.RuntimeDataInterface;

/**
 * @author sbjlwa
 */
public class PersistorToFile extends AbstractRuntimeDataPersistor{

	protected String filename = null;
	protected FileType fileType = null;
	
	public PersistorToFile(RuntimeDataInterface runtime, FileType fileType, String filename){
		super(runtime);
		this.fileType = fileType;
		this.filename = filename;
	}
	
	@Override
	public void persist(Persistable persistable) throws SAFSException {
		super.persist(persistable);
		
		//TODO details
	}

	@Override
	public void unpersist() throws SAFSException {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * If they have the same filename, then we consider them equivalent
	 */
	public boolean equals(Object o){
		if(o==null) return false;
		if(!(o instanceof PersistorToFile)) return false;
		PersistorToFile p = (PersistorToFile) o;
		
		if(filename==null){
			return p.filename==null;
		}else{
			return this.filename.equals(p.filename);
		}
	}

}
