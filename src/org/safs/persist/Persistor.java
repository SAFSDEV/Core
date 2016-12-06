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

import java.util.Arrays;

import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.StringUtils;

/**
 * @author Lei Wang
 */
public interface Persistor {
	/**
	 * Persist an object.
	 * @param persistable Persistable, the object to persist
	 * @throws SAFSException when persistence fails or something wrong happens.
	 */
	public void persist(Persistable persistable) throws SAFSException;
	
	/**
	 * Delete the persistence.
	 * @throws SAFSException when failing to delete the persistence or something wrong happens.
	 */
	public void unpersist() throws SAFSException;
	
	/**
	 * The persistence Type.
	 * @return Type
	 */
	public Type getType();
	
	/**
	 * The name of the persistence.
	 * @return String, the file-name or the variable-name etc.
	 */
	public String getPersistenceName();
	
	public static enum Type{
		FILE("FILE"),
		VARIABLE("VARIABLE");

		public final String name;
		Type(String name){
			this.name = name;
		}
		
		public static Type get(String name){
			Type type = Type.VARIABLE;
			if(FILE.name.equalsIgnoreCase(name)) type = Type.FILE;
			else if(VARIABLE.name.equalsIgnoreCase(name)) type = Type.VARIABLE;
			else{
				IndependantLog.warn(StringUtils.debugmsg(false)+"The persistence type '"+name+"' is NOT valid!\n"
						+ "The possible valid type can be "+Arrays.toString(Type.values())+"\n"
						+ "The default type "+type+" is returned.");
			}
			return type;
		}
	}
	
	public static enum FileType{
		JSON("JSON"),
		XML("XML"),
		PROPERTIES("PROPERTIES");

		public final String name;
		FileType(String name){
			this.name = name;
		}
		
		public static FileType get(String name){
			FileType type = FileType.JSON;
			if(JSON.name.equalsIgnoreCase(name)) type = FileType.JSON;
			else if(XML.name.equalsIgnoreCase(name)) type = FileType.XML;
			else if(PROPERTIES.name.equalsIgnoreCase(name)) type = FileType.PROPERTIES;
			else{
				IndependantLog.warn(StringUtils.debugmsg(false)+"The file type '"+name+"' is NOT valid!\n"
						+ "The possible valid type can be "+Arrays.toString(FileType.values())+"\n"
						+ "The default type "+type+" is returned.");
			}
			return type;
		}
	}
}
