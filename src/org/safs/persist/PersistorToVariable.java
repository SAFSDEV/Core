/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 02, 2016    (Lei Wang) Initial release.
 */
package org.safs.persist;

import java.util.Map;

import org.safs.SAFSException;
import org.safs.tools.RuntimeDataInterface;

/**
 * @author Lei Wang
 */
public class PersistorToVariable extends AbstractRuntimeDataPersistor{
	protected String variablePrefix = null;
	
	public PersistorToVariable(RuntimeDataInterface runtime, String variablePrefix){
		super(runtime);
		this.variablePrefix = variablePrefix;
	}
	
	@Override
	public void persist(Persistable persistable) throws SAFSException {
		super.persist(persistable);
		
		//TODO details
		Map<String, Object> contents = persistable.getContents();
		
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
		if(!(o instanceof PersistorToVariable)) return false;
		PersistorToVariable p = (PersistorToVariable) o;
		
		if(variablePrefix==null){
			return p.variablePrefix==null;
		}else{
			return this.variablePrefix.equals(p.variablePrefix);
		}
	}
	
}
