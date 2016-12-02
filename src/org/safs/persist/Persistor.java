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

import org.safs.SAFSException;

/**
 * @author sbjlwa
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
			return type;
		}
	}
}
