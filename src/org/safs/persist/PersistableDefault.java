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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.safs.IndependantLog;
import org.safs.Printable;
import org.safs.StringUtils;
import org.safs.Utils;

/**
 * @author sbjlwa
 */
public abstract class PersistableDefault implements Persistable, Printable{
	protected static final String UNKNOWN_VALUE = "UNKNOWN";

	protected boolean enabled = true;
	protected Persistable parent = null;

	protected int tabulation = 0;

	/**
	 * This method use Java reflection to get the value of the field defined in {@link #getPersitableFields()}.
	 */
	@Override
	public Map<String, Object> getContents() {
		String debugmsg = StringUtils.debugmsg(false);

		Map<String, String> fieldToPersistKeyMap= getPersitableFields();
		Map<String, Object> contents = new TreeMap<String, Object>();

		Class<?> clazz = getClass();

		Set<String> fieldNames = fieldToPersistKeyMap.keySet();

		Field field = null;
		Object value = null;
		for(String fieldName: fieldNames){
			try{
				field = clazz.getDeclaredField(fieldName);
				field.setAccessible(true);
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
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public void setParent(Persistable parent){
		this.parent = parent;
	}

	@Override
	public Persistable getParent(){
		return parent;
	}

	@Override
	public String getFlatKey(){
		String key = getClass().getSimpleName();
		if(parent!=null){
			key = parent.getFlatKey()+"."+key;
		}

		return key;
	}

	@Override
	public Map<String, Object> getContents(Map<String,String> elementAlternativeValues, Set<String> ignoredFields, boolean includeContainer){
		String flatKey = getFlatKey();
		Map<String, Object> actualContents = null;
		Map<String, Object> childContents = null;

		if(!isEnabled()){
			IndependantLog.debug("The class '"+getClass().getSimpleName()+"' is not enabled and it has been added to Set ignoredFields.");
			ignoredFields.add(flatKey);
			return null;
		}

		actualContents = new TreeMap<String, Object>();

		if(includeContainer){
			//The Persistable object itself doesn't have a value, and it contains children
			//while the SAX XML parser will treat it as Element and assign it a default string "\n" as value
			//So we add the default string "\n" for Persistable object itself in the actualContents Map to
			//get the verification pass.
			Object containerValue = Utils.getMapValue(elementAlternativeValues, CONTAINER_ELEMENT, "");
			actualContents.put(flatKey, containerValue);
		}

		Map<String, Object> contents = getContents();

		Object value = null;
		for(String key:contents.keySet()){
			value = contents.get(key);

			if(value instanceof Persistable){
				childContents = ((Persistable)value).getContents(elementAlternativeValues, ignoredFields, includeContainer);
				if(childContents!=null){
					actualContents.putAll(childContents);
				}
			}else{
				String tempKey = flatKey+"."+key;
				Object altValue = Utils.getMapValue(elementAlternativeValues, tempKey, null);
				actualContents.put(tempKey, altValue==null?value:altValue);
			}
		}

		return actualContents;
	}

	public int getTabulation(){
		return tabulation;
	}
	public void setTabulation(int tabulation){
		this.tabulation = tabulation;
	}

	private String getTabs(){
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<getTabulation();i++){
			sb.append("\t");
		}
		return sb.toString();
	}

	public String toString(){
		String clazzname = getClass().getSimpleName();
		Map<String, Object> contents = getContents();
		StringBuilder sb = new StringBuilder();

		List<String> complicatedChildren = new ArrayList<String>();
		Object value = null;

		sb.append("\n"+getTabs()+"============== "+clazzname+" BEGIN ================\n");
		for(String key:contents.keySet()){
			value = contents.get(key);
			if(value instanceof PersistableDefault){
				//complicatedChildren will hold the key for PersistableDefault object to print out later
				complicatedChildren.add(key);
			}else{
				sb.append(getTabs()+key+" : "+value+"\n");
			}
		}

		//print out PersistableDefault object
		for(String key:complicatedChildren){
			sb.append(getTabs()+key+" : "+contents.get(key)+"\n");
		}
		sb.append(getTabs()+"============== "+clazzname+" END ================\n");

		return sb.toString();
	}
}
