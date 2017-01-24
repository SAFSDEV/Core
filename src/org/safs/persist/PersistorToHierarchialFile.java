/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 13, 2016    (SBJLWA) Initial release.
 */
package org.safs.persist;

import java.io.IOException;
import java.util.Map;

import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.SAFSPersistableNotEnableException;
import org.safs.StringUtils;
import org.safs.tools.RuntimeDataInterface;

/**
 * Write the Persistable object to a persistence of hierarchical structure, such as JSON, XML file.<br/>
 *
 * @author sbjlwa
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
		String escapedValue = null;

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
				escapedValue = escape(value.toString());
				childBegin(key, escapedValue);
			}
			lastChild = (i+1)==keys.length;
			childEnd(lastChild);
		}

		containerEnd(className);
	}

	protected String getTagName(String className){
		return StringUtils.getLastDelimitedToken(className, ".");
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
