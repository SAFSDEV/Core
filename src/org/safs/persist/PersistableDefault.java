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
 * MAR 16, 2017    (Lei Wang) Added default implementation of method getPersitableFields().
 *                          Added caches holding result of getContents() and getPersitableFields().
 *                          Handled the field of type "array": setField(), equals().
 *
 */
package org.safs.persist;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
	protected static final String UNKNOWN_VALUE 		= "UNKNOWN";
	protected static final String FAILED_RETRIEVE_VALUE = "FAILED_RETRIEVE";

	/** a cache holding the Map of (fieldName, persistKey) */
	private Map<String/*fieldName*/, String/*persistKey*/> fieldNameToPersistKeyMap = null;
	/** a cache holding the Map of (persistKey, fieldValue) */
	private Map<String/*persistKey*/, Object/*fieldValue*/> persistKeyToFieldValueMap = null;

	protected boolean enabled = true;
	protected Persistable parent = null;

	protected int tabulation = 0;
	protected int threshold = 0;
	protected boolean thresholdEnabled = false;

	@Override
	public Map<String, String> getPersitableFields(){

		if(fieldNameToPersistKeyMap==null){
			Field[] fields = getClass().getDeclaredFields();
			fieldNameToPersistKeyMap = new HashMap<String, String>();
			for(Field field:fields){
				fieldNameToPersistKeyMap.put(field.getName(), field.getName());
			}
		}

		return fieldNameToPersistKeyMap;
	}

	/**
	 * This method use Java reflection to get the value of the field defined in {@link #getPersitableFields()}.
	 */
	@Override
	public Map<String, Object> getContents() {
		if(persistKeyToFieldValueMap!=null){
			return persistKeyToFieldValueMap;
		}

		String debugmsg = StringUtils.debugmsg(false);

		Map<String, String> fieldToPersistKeyMap = getPersitableFields();
		persistKeyToFieldValueMap = new TreeMap<String, Object>();

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
				IndependantLog.warn(debugmsg+" can NOT get value for field '"+fieldName+"', met "+StringUtils.debugmsg(e)+"\nset "+FAILED_RETRIEVE_VALUE+" as its value.");
				value = FAILED_RETRIEVE_VALUE;
			}

			if(value==null){
				IndependantLog.debug(debugmsg+" value is null for field '"+fieldName+"', set "+UNKNOWN_VALUE+" as its value.");
				value = UNKNOWN_VALUE;
			}

			persistKeyToFieldValueMap.put(fieldToPersistKeyMap.get(fieldName), value);
		}

		return persistKeyToFieldValueMap;
	}

	@Override
	public boolean setField(String persistKey, Object value){
		String debugmsg = StringUtils.debugmsg(false);

		Class<?> clazz = getClass();
		String fieldName = null;
		Field field = null;

		fieldName = getFieldName(persistKey);

		if(fieldName!=null){
			try {
				field = clazz.getDeclaredField(fieldName);
				field.setAccessible(true);
				field.set(this, Utils.parseValue(field.getType(),value));
				return true;
			} catch (Exception e) {
				IndependantLog.error(debugmsg+"Failed to set field '"+fieldName+"', due to "+e.toString());
			}
		}else{
			IndependantLog.warn(debugmsg+" cannot get field-name for persistKey '"+persistKey+"'.");
		}

		return false;
	}

	/**
	 * According to the persistKey, get the field name.<br/>
	 */
	private String getFieldName(String persistKey){
		if(persistKey==null) return persistKey;

		Map<String, String> fieldToPersistKeyMap = getPersitableFields();

		Set<String> fieldNames = fieldToPersistKeyMap.keySet();

		String tempKey = null;
		for(String fieldName: fieldNames){
			tempKey = fieldToPersistKeyMap.get(fieldName);
			if(persistKey.equalsIgnoreCase(tempKey)){
				return fieldName;
			}
		}

		//If cannot find, it will be considered as a field name.
		return persistKey;
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
			//The Persistable object itself doesn't have a real value, but it contains children;
			//while the SAX XML parser will treat it as an Element and assign it a default string "\n" as value
			//So we add the default string "\n" for Persistable object itself in the actualContents Map to
			//get the verification pass. See VerifierToXMLFile#beforeCheck().
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

				if(value.getClass().isArray()){
					try {
						value = Arrays.toString(Utils.getArray(value));
					} catch (SAFSException e) {
						IndependantLog.warn("Failed to get array field value, due to "+e.toString());
					}
				}

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

	/**
	 * If
	 * <ul>
	 * <li>The parameter obj is also a Persistable.
	 * <li>The size of {@link #getPersitableFields()} are same
	 * <li>The values of each field from {@link #getPersitableFields()} are same
	 * </ul>
	 * then they will be considered as equal.<br/>
	 */
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
			value2 = tempPersistable.getContents().get(persistKey);
			if(value1==null){
				if(value2!=null) return false;
			}else if(value1.getClass().isArray()){
				try {
					return Arrays.toString(Utils.getArray(value1)).equals(Arrays.toString(Utils.getArray(value2)));
				} catch (SAFSException e) {
					IndependantLog.warn("Not equal, due to "+e.toString());
					return false;
				}
			}else if(!value1.equals(value2)){
				return false;
			}
		}

		return true;
	}
}
