/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 02, 2016    (Lei Wang) Initial release.
 * MAR 10, 2017    (Lei Wang) Override the method equals().
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
import org.safs.SAFSException;
import org.safs.StringUtils;
import org.safs.Utils;

/**
 * @author Lei Wang
 */
public abstract class PersistableDefault implements Persistable, Printable{
	protected static final String UNKNOWN_VALUE = "UNKNOWN";

	protected boolean enabled = true;
	protected Persistable parent = null;

	protected int tabulation = 0;
	protected int threshold = 0;
	protected boolean thresholdEnabled = false;

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
	public boolean setField(String tag, Object value){
		String debugmsg = StringUtils.debugmsg(false);

		Class<?> clazz = getClass();
		String fieldName = null;
		Field field = null;

		fieldName = getFieldName(tag);

		if(fieldName!=null){
			try {
				field = clazz.getDeclaredField(fieldName);
				field.setAccessible(true);
				field.set(this, parseFieldValue(field,value));
				return true;
			} catch (Exception e) {
				IndependantLog.error(debugmsg+"Failed to set field '"+fieldName+"', met "+e.toString());
			}
		}else{
			IndependantLog.warn(debugmsg+" cannot get field for field '"+tag+"'.");
		}

		return false;
	}

	/**
	 * Convert the parameter 'value' to an appropriate Object according to the field's type.
	 * @param field Field
	 * @param value Object, the filed's value to parse.
	 * @return Object, the converted Object for the field.
	 */
	protected static Object parseFieldValue(Field field, Object value) throws SAFSException{
		Class<?> type = null;
		Object fieldValue = value;

		if(value==null){
			return value;
		}

		try{
			type = field.getType();

			if(Persistable.class.isAssignableFrom(type)){
				if(!(value instanceof Persistable)){
					//convert an object to persistable
				}
			}else if(type.isAssignableFrom(String.class)){
				if(!(value instanceof String)){
					fieldValue = String.valueOf(value);
				}
			}else if(type.isAssignableFrom(Boolean.TYPE)){
				if(!(value instanceof Boolean)){
					fieldValue = Boolean.valueOf(value.toString());
				}
			}else if(type.isAssignableFrom(Integer.TYPE)){
				if(!(value instanceof Integer)){
					fieldValue = Integer.valueOf(value.toString());
				}
			}else if(type.isAssignableFrom(Short.TYPE)){
				if(!(value instanceof Short)){
					fieldValue = Short.valueOf(value.toString());
				}
			}else if(type.isAssignableFrom(Long.TYPE)){
				if(!(value instanceof Long)){
					fieldValue = Long.valueOf(value.toString());
				}
			}else if(type.isAssignableFrom(Double.TYPE)){
				if(!(value instanceof Double)){
					fieldValue = Double.valueOf(value.toString());
				}
			}else if(type.isAssignableFrom(Float.TYPE)){
				if(!(value instanceof Float)){
					fieldValue = Float.valueOf(value.toString());
				}
			}else if(type.isAssignableFrom(Byte.TYPE)){
				if(!(value instanceof Byte)){
					fieldValue = Byte.valueOf(value.toString());
				}
			}else if(type.isAssignableFrom(Character.TYPE)){
				if(!(value instanceof Character)){
					fieldValue = Character.valueOf(value.toString().charAt(0));
				}
			}
		}catch(NumberFormatException | IndexOutOfBoundsException e){
			//user's data error
			throw new SAFSException(e.toString());
		}catch(Exception e){
			//program error, a bug
			throw new SAFSException(e.toString());
		}

		return fieldValue;
	}

	/**
	 * According to the tag-name, get the field name.<br/>
	 */
	private String getFieldName(String tagName){
		if(tagName==null) return tagName;

		Map<String, String> fieldToPersistKeyMap= getPersitableFields();

		Set<String> fieldNames = fieldToPersistKeyMap.keySet();

		String persistKey = null;
		for(String fieldName: fieldNames){
			persistKey = fieldToPersistKeyMap.get(fieldName);
			if(tagName.equalsIgnoreCase(persistKey)){
				return fieldName;
			}
		}

		//If cannot find, it will be considered as a field name.
		return tagName;
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

	@Override
	public void setThreshold(int threshold){
		this.threshold = threshold;
	}
	@Override
	public int getThreshold(){
		return threshold;
	}

	@Override
	public boolean isThresholdEnabled() {
		return thresholdEnabled;
	}

	@Override
	public void setThresholdEnabled(boolean thresholdEnabled) {
		this.thresholdEnabled = thresholdEnabled;
	}

	@Override
	public int getTabulation(){
		return tabulation;
	}
	@Override
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

				if(value!=null && isThresholdEnabled() && value.toString().length()>getThreshold()){
					IndependantLog.debug("The value of '"+key+"' is too big, its size '"+value.toString().length()+"' is over threshold '"+getThreshold()+"'");
					sb.append(getTabs()+key+" : "+DATA_BIGGER_THAN_THRESHOLD+".\n");
				}else{
					sb.append(getTabs()+key+" : "+value+"\n");
				}
			}
		}

		//print out PersistableDefault object
		for(String key:complicatedChildren){
			sb.append(getTabs()+key+" : "+contents.get(key)+"\n");
		}
		sb.append(getTabs()+"============== "+clazzname+" END ================\n");

		return sb.toString();
	}

	public boolean equals(Object obj){
		if(obj==null) return false;
		if(!(obj instanceof Persistable)) return false;
		if(obj==this) return true;
		Persistable tempPersistable = (Persistable) obj;
		if(getPersitableFields().size()!=tempPersistable.getPersitableFields().size()) return false;

		Set<String> fields = getPersitableFields().keySet();
		String persistKey = null;
		Object value1 = null;
		Object value2 = null;
		for(String field:fields){
			if(!tempPersistable.getPersitableFields().containsKey(field)) return false;
			persistKey = getPersitableFields().get(field);
			value1 = getContents().get(persistKey);
			persistKey = tempPersistable.getPersitableFields().get(field);
			value2 = getContents().get(persistKey);
			if(value1==null){
				if(value2!=null) return false;
			}else if(!value1.equals(value2)){
				return false;
			}
		}

		return true;
	}
}
