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
 * MAR 14, 2017    (Lei Wang) Modified method create(): create persistor according to the file's suffix.
 */
package org.safs.persist;

import org.safs.SAFSException;
import org.safs.text.FileUtilities.FileType;
import org.safs.tools.RuntimeDataInterface;

/**
 * @author Lei Wang
 *
 */
public class PersistorFactory {

	/**
	 * @param persistenceType PersistenceType, what kind of persistence to store object, it can be file, variable or database etc.
	 * @param fileType FileType enum object, ONLY useful when persistenceType is PersistenceType.FILE
	 * @param runtime RuntimeDataInterface, required by a concrete Persistor.
	 * @param object Object, an object required by a concrete Persistor.
	 * @return Persistor
	 * @throws SAFSException if no Persistor has been created.
	 */
	public static Persistor create(PersistenceType persistenceType, FileType fileType, RuntimeDataInterface runtime , Object object)  throws SAFSException{
		Persistor persistor = null;

		if(PersistenceType.FILE.equals(persistenceType)){
			String filename = object.toString();
			if(FileType.JSON.equals(fileType)){
				persistor = new PersistorToJSONFile(runtime, filename);
			}else if(FileType.XML.equals(fileType)){
				persistor = new PersistorToXMLFile(runtime, filename);
			}else if(FileType.PROPERTIES.equals(fileType)){
				persistor = new PersistorToPropertiesFile(runtime, filename);
			}else{
				String filenameuc = filename.toUpperCase();
				if(filenameuc.endsWith(FileType.JSON.name())){
					persistor = new PersistorToJSONFile(runtime, filename);
				}else if(filenameuc.endsWith(FileType.XML.name())){
					persistor = new PersistorToXMLFile(runtime, filename);
				}else if(filenameuc.endsWith(FileType.PROPERTIES.name())){
					persistor = new PersistorToPropertiesFile(runtime, filename);
				}
			}
		}else if(PersistenceType.VARIABLE.equals(persistenceType)){
			persistor = new PersistorToVariable(runtime, object.toString());
		}

		if(persistor==null){
			throw new SAFSException("PersistorFactory does not support Persistor of\n"
					+ "persistence-type: '"+persistenceType.name+"'\n"
					+ "file-type: '"+fileType.name+"'!\n");
		}

		return persistor;
	}
}
