/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
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
