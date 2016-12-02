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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.safs.IndependantLog;
import org.safs.StringUtils;

/**
 * @author sbjlwa
 */
public class PersistableDefault implements Persistable{
	protected static final String UNKNOWN_VALUE = "UNKNOWN";

	protected static final Map<String, String> fieldToPersistKeyMap = new HashMap<String, String>();
	
	protected boolean enabled = true;
	
	@Override
	public Map<String, Object> getContents() {
		String debugmsg = StringUtils.debugmsg(false);
		
		Map<String, String> fieldToPersistKeyMap= getPersitableFields();
		Map<String, Object> contents = new HashMap<String, Object>();

		Class<?> clazz = getClass();
		
		Set<String> fieldNames = fieldToPersistKeyMap.keySet();
		
		Field field = null;
		Object value = null;
		for(String fieldName: fieldNames){
			try{
				field = clazz.getDeclaredField(fieldName);
				value = field.get(this);
			}catch(Exception e){
				IndependantLog.warn(debugmsg+" can NOT get value for field '"+fieldName+"', met "+StringUtils.debugmsg(e));
			}
			
			if(value==null){
				IndependantLog.debug(debugmsg+" value is null for field '"+fieldName+"', set "+UNKNOWN_VALUE+" to as its value.");
				value = UNKNOWN_VALUE;
			}
			
			contents.put(fieldToPersistKeyMap.get(fieldName), value);
		}
		
		return contents;
	}

	@Override
	public Map<String, String> getPersitableFields() {
		return fieldToPersistKeyMap;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
