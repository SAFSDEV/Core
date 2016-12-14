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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.SAFSPersistableNotEnableException;
import org.safs.tools.RuntimeDataInterface;

/**
 * @author Lei Wang
 */
public class PersistorToVariable extends AbstractRuntimeDataPersistor{
	protected String variablePrefix = null;
	protected Set<String> storedVariables = new HashSet<String>();


	public PersistorToVariable(RuntimeDataInterface runtime, String variablePrefix){
		super(runtime);
		this.variablePrefix = variablePrefix;
	}

	@Override
	public void persist(Persistable persistable) throws SAFSException {

		validate(persistable);

		Map<String, Object> contents = persistable.getContents();
		String className = persistable.getClass().getSimpleName();
		String variableName = null;
		Object value = null;

		for(String key:contents.keySet()){
			value = contents.get(key);
			if(value==null){
				IndependantLog.warn("value is null for key '"+key+"'");
				continue;
			}
			if(value instanceof Persistable){
				try{
					persist((Persistable) value);
				}catch(SAFSPersistableNotEnableException pne){
					//We should not break if some child is not persistable
					IndependantLog.warn(pne.toString());
					continue;
				}
			}else{
				variableName = variablePrefix+"."+className+"."+key;
				//TODO value is not always a String, more codes are needed.
				runtime.setVariable(variableName, value.toString());
				storedVariables.add(variableName);
			}
		}
	}

	@Override
	public void unpersist() throws SAFSException {
		Iterator<String> variables = storedVariables.iterator();
		while(variables.hasNext()){
			//TODO we need to add a deleteVarialbe in the runtime
			runtime.setVariable(variables.next(), null);
			variables.remove();
		}
	}

	public PersistenceType getType(){
		return PersistenceType.VARIABLE;
	}

	public String getPersistenceName(){
		return variablePrefix;
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
