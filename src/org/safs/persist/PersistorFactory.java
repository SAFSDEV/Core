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
import org.safs.text.FileUtilities.FileType;
import org.safs.tools.RuntimeDataInterface;

/**
 * @author sbjlwa
 *
 */
public class PersistorFactory {

	/**
	 *
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
			if(FileType.JSON.equals(fileType)){
				persistor = new PersistorToJSONFile(runtime, object.toString());
			}else if(FileType.XML.equals(fileType)){
				persistor = new PersistorToXMLFile(runtime, object.toString());
			}else if(FileType.PROPERTIES.equals(fileType)){
				persistor = new PersistorToPropertiesFile(runtime, object.toString());
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
