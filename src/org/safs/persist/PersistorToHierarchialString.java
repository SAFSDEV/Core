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
 * OCT 24, 2017    (Lei Wang) Initial release.
 * NOV 03, 2017    (Lei Wang) Modified parseFieldValue(): For a field of type Map, convert it to JSON string for persisting.
 */
package org.safs.persist;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.SAFSPersistableNotEnableException;
import org.safs.StringUtils;
import org.safs.Utils;

/**
 * Write the Persistable object to a persistence of hierarchical structure, such as JSON, XML format.<br/>
 *
 * @author Lei Wang
 */
public abstract class PersistorToHierarchialString extends PersistorToString{

	public PersistorToHierarchialString(){
		super();
	}

	public PersistorToHierarchialString(String stringFormat){
		super(stringFormat);
	}

	public PersistorToHierarchialString(Reader stringFormatReader){
		super(stringFormatReader);
	}

	/**
	 * Convert a Persistable Object to a hierarchical string. The concrete format depends on the sub-class.
	 *
	 * @param persistable
	 * @param needLeadingNameForContainer
	 * @return
	 * @throws SAFSException
	 */
	@Override
	protected final String parse(Persistable persistable, boolean needLeadingNameForContainer)  throws SAFSException{
		validate(persistable);

		Map<String, Object> contents = persistable.getContents();
		String className = persistable.getClass().getName();
		Object value = null;

		if(contents==null){
			throw new SAFSException("NO contents got from Persistable object!");
		}

		StringBuffer sb = new StringBuffer();

		sb.append(getContainerBegin(className, needLeadingNameForContainer));

		String[] keys = contents.keySet().toArray(new String[0]);
		String key = null;
		boolean lastChild = false;
		for(int i=0;i<keys.length;i++){
			key = keys[i];
			value = contents.get(key);
			if(value==null){
				IndependantLog.warn("value is null for key '"+key+"'");
				continue;
			}
			if(value instanceof Persistable){
				try{
					sb.append(parse((Persistable) value, true));
				}catch(SAFSPersistableNotEnableException pne){
					//We should not break if some child is not persistable, just log a warning.
					IndependantLog.warn(pne.getMessage());
					continue;
				}
			}else{
				sb.append(getChildBegin(key, parseFieldValue(value), value.getClass().getName()));
			}
			lastChild = (i+1)==keys.length;
			sb.append(getChildEnd(lastChild));
		}

		sb.append(getContainerEnd(className));

		return sb.toString();
	}

	protected String getTagName(String className){
		return PersistableDefault.getTagName(className);
	}

	/**
	 * This is called inside {@link #parse(Persistable, boolean)} to write the begin
	 * of a container such as:
	 * <ul>
	 * <li><b>&lt;tagNam&gt;</b> for XML format
	 * <li><b>"tagName" : {\n</b> for JSON format
	 * </ul>
	 * @param className String, the class name of a container. This is a full class-name, which may needs to
	 *                          treated to get the simple class name as the tag-name.
	 * @param needLeadingName boolean, if the leading tag name is needed. For example sometimes we don't need the "tagName" for JSON format.
	 * @throws IOException
	 */
	protected abstract String getContainerBegin(String className, boolean needLeadingName);
	/**
	 * This is called inside {@link #parse(Persistable, boolean)} to write "key"
	 * and "value" of a child.
	 * @param key String, the key name
	 * @param value String, the value
	 * @param classname String, the class name of this value. It is not useful with JSON format, but it is useful for XML format.
	 * @throws IOException
	 */
	protected abstract String getChildBegin(String key, String value, String classname);
	/**
	 * This is called inside {@link #parse(Persistable, boolean)} to write the
	 * end of a child, here a newline "\n" is written.<br/>
	 * @param lastChild boolean, if this is the last child within the container.
	 *        it is useful for some file format, such as JSON, if this
	 *        is false (not the last child), a comma <font color="red">,</font>
	 *        needs to be added at the end as <b>"key" : "value" ,</b><br/>
	 * @throws IOException
	 */
	protected String getChildEnd(boolean lastChild){
		return "\n";
	}
	/**
	 * This is called inside {@link #parse(Persistable, boolean)} to write the end
	 * of a container such as:
	 * <ul>
	 * <li><b>&lt;/tagNam&gt;</b> for XML file
	 * <li><b>}</b> for JSON file.<br/>
	 * </ul>
	 * @param className String, the tag name, it is normally a container. This is a full class-name, which may needs to
	 *                          treated to get the simple class name as the tag-name.
	 * @throws IOException
	 */
	protected abstract String getContainerEnd(String className);

	/**
	 * Parse the parameter value and convert it to a string which will be persisted to (File or Variable or ...)<br>
	 * The following are the ways to parse value:
	 * <ul>
	 * <li>The String value will be {@link #escape(String)} and double-quoted if needed.
	 * <li>The array value will be transformed into <b>[item1, item2, item3]</b>
	 * <li>The Number value will be returned as string without quote.
	 * </ul>
	 *
	 * @param value Object, the Persistable object field's value
	 * @return String, the parsed result of the field's value<br/>
	 *                 null if the parameter 'value' is null.
	 * @throws SAFSException
	 */
	protected String parseFieldValue(Object value){
		if(value==null){
			return null;
		}

		StringBuffer result = new StringBuffer();
		String debugmsg = StringUtils.debugmsg(false);
		Class<?> fieldClass = value.getClass();
		IndependantLog.debug(debugmsg+" parsing field value of type '"+fieldClass.getName()+"'");

		if(value instanceof String){
			result.append(escape(value.toString()));

		}else if(fieldClass.isArray() ||
				(value instanceof Collection) ){

			Object arrayObject = value;
			if(value instanceof Collection){
				//The Collection object will also be saved as Array
				arrayObject = ((Collection<?>)value).toArray();
			}

			result.append(parseArrayField(arrayObject));

		}else if(value instanceof Number){
			//simply add Number as a string without any modification
			result.append(value);
		}else if(value instanceof Persistable){
			try {
				//We do NOT need the leading name for JSON file: "name: { }" is not accepted, "{ }" is accepted.
				result.append(parse((Persistable)value, false));
			} catch (Exception e) {
				IndependantLog.error(debugmsg+"Met "+e.toString());
			}
		}else if(value instanceof Map){
			//convert the Map to a JSON string which can be converted back to Map easily
			result.append(escape(Utils.toJsonString(value)));
		}
		//TODO we might need to handle more types
		else{
			IndependantLog.warn(StringUtils.debugmsg(false)+" the type '"+value.getClass().getSimpleName()+"' is not supported yet.");
			result.append(escape(value.toString()));
		}

		return result.toString();
	}

	/** "[" the start bracket to wrap json array items */
	protected static final String JSON_ARRAY_BRACKET_LEFT 	= "[";
	/** "]" the end bracket to wrap the json array items */
	protected static final String JSON_ARRAY_BRACKET_RIGHT 	= "]";
	/** "," separator to separate the json array items */
	protected static final String JSON_ARRAY_COMMA_SEP 		= ",";

	/**
	 * @param arrayFieldObject Object, it is the object represent an array field of a class
	 * @return String, the string format of an array object
	 */
	protected String parseArrayField(Object arrayFieldObject){
		StringBuffer result = new StringBuffer();
		//By default, we use the JSON way to keep the array or List
		//[item1, item2, item3, item4, item5]
		int length = Array.getLength(arrayFieldObject);
		Object arrayValue = null;
		result.append(JSON_ARRAY_BRACKET_LEFT);
		for(int i=0;i<length;i++){
			arrayValue = Array.get(arrayFieldObject, i);
			result.append(parseFieldValue(arrayValue));
			if(i<length-1){//not the last element, so append a comma
				result.append(JSON_ARRAY_COMMA_SEP+" ");
			}
		}
		result.append(JSON_ARRAY_BRACKET_RIGHT);

		return result.toString();
	}

}
