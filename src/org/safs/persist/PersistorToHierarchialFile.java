/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 13, 2016    (Lei Wang) Initial release.
 */
package org.safs.persist;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.SAFSPersistableNotEnableException;
import org.safs.StringUtils;
import org.safs.tools.RuntimeDataInterface;

/**
 * Write the Persistable object to a persistence of hierarchical structure, such as JSON, XML file.<br/>
 *
 * @author Lei Wang
 */
public class PersistorToHierarchialFile extends PersistorToFile{

	public PersistorToHierarchialFile(RuntimeDataInterface runtime, String filename){
		super(runtime, filename);
	}

	/**
	 * Write the Persistable object to a persistence of hierarchical structure, such as JSON, XML file.<br/>
	 * This is a template method, it is calling the method below:
	 * <ol>
	 * <li>{@link #containerBegin(String)}
	 * <li>{@link #childBegin(String, String)}
	 * <li>{@link #childEnd(boolean)}
	 * <li>{@link #containerEnd(String)}
	 * </ol>
	 *
	 * @param persistable Persistable, the object to persist
	 * @throws SAFSException
	 * @throws IOException
	 */
	protected final void write(Persistable persistable)  throws SAFSException, IOException{
		validate(persistable);

		Map<String, Object> contents = persistable.getContents();
		String className = persistable.getClass().getName();
		Object value = null;

		if(contents==null){
			throw new SAFSException("NO contents got from Persistable object!");
		}

		containerBegin(className);

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
					write((Persistable) value);
				}catch(SAFSPersistableNotEnableException pne){
					//We should not break if some child is not persistable, just log a warning.
					IndependantLog.warn(pne.getMessage());
					continue;
				}
			}else{
				childBegin(key, parseFiledValue(value));
			}
			lastChild = (i+1)==keys.length;
			childEnd(lastChild);
		}

		containerEnd(className);
	}

	protected String getTagName(String className){
		return PersistableDefault.getTagName(className);
	}

	/**
	 * This is called inside {@link #write(Persistable)} to write the begin
	 * of a container such as:
	 * <ul>
	 * <li><b>&lt;tagNam&gt;</b> for XML file
	 * <li><b>"tagName" : {\n</b> for JSON file
	 * </ul>
	 * @param className String, the class name of a container. This is a full class-name, which may needs to
	 *                          treated to get the simple class name as the tag-name.
	 * @throws IOException
	 */
	protected void containerBegin(String className) throws IOException{}
	/**
	 * This is called inside {@link #write(Persistable)} to write "key"
	 * and "value" of a child.
	 * @param key String, the key name
	 * @param value String, the value
	 * @throws IOException
	 */
	protected void childBegin(String key, String value) throws IOException{}

	/**
	 * Parse the parameter value and convert it to a string which will be persisted to (File or Variable or ...)<br>
	 * The following are the ways to parse value:
	 * <ul>
	 * <li>The String value will be {@link #escape(String)} and double-quoted if needed.
	 * <li>The array value will be transformed into <b>[item1, item2, item3]</b>
	 * <li>The Number value will be returned as string without quote.
	 * </ul>
	 *
	 * @param value Object, the filed's value
	 * @return String, the parsed result of the field's value<br/>
	 *                 null if the parameter 'value' is null.
	 * @throws SAFSException
	 */
	protected String parseFiledValue(Object value){
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

			//By default, we use the JSON way to keep the array or List
			//[item1, item2, item3, item4, item5]
			int length = Array.getLength(arrayObject);
			Object arrayValue = null;
			result.append("[");
			for(int i=0;i<length;i++){
				arrayValue = Array.get(arrayObject, i);
				result.append(parseFiledValue(arrayValue));
				if(i<length-1){//not the last element, so append a comma
					result.append(", ");
				}
			}
			result.append("]");

		}else if(value instanceof Number){
			//simply add Number as a string without any modification
			result.append(value);
		}
		//TODO we might need to handle more types
		else{
			IndependantLog.warn(StringUtils.debugmsg(false)+" the type '"+value.getClass().getSimpleName()+"' is not supported yet.");
			result.append(escape(value.toString()));
		}

		return result.toString();
	}

	/**
	 * This is called inside {@link #write(Persistable)} to write the
	 * end of a child, here a newline "\n" is written.<br/>
	 * @param lastChild boolean, if this is the last child within the container.
	 *        it is useful for some file format, such as JSON, if this
	 *        is false (not the last child), a comma <font color="red">,</font>
	 *        needs to be added at the end as <b>"key" : "value" ,</b><br/>
	 * @throws IOException
	 */
	protected void childEnd(boolean lastChild) throws IOException{
		writer.write("\n");
	}
	/**
	 * This is called inside {@link #write(Persistable)} to write the end
	 * of a container such as:
	 * <ul>
	 * <li><b>&lt;/tagNam&gt;</b> for XML file
	 * <li><b>}</b> for JSON file.<br/>
	 * </ul>
	 * @param className String, the tag name, it is normally a container. This is a full class-name, which may needs to
	 *                          treated to get the simple class name as the tag-name.
	 * @throws IOException
	 */
	protected void containerEnd(String className) throws IOException{}

}
