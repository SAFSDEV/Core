/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 08, 2016    (SBJLWA) Initial release.
 * MAR 14, 2017    (SBJLWA) Modified method create(): create verifier according to the file's suffix.
 */
package org.safs.persist;

import org.safs.SAFSException;
import org.safs.text.FileUtilities.FileType;
import org.safs.tools.RuntimeDataInterface;

/**
 * @author sbjlwa
 *
 */
public class VerifierFactory {

	/**
	 *
	 * @param persistenceType PersistenceType, by what means the benchmark is provided.
	 * @param fileType FileType enum object, ONLY useful when persistenceType is PersistenceType.FILE
	 * @param runtime RuntimeDataInterface, required by a concrete Verifier.
	 * @param object Object, an object required by a concrete Verifier.
	 * @return Verifier
	 * @throws SAFSException if no Verifier has been created.
	 */
	public static Verifier create(PersistenceType persistenceType, FileType fileType, RuntimeDataInterface runtime , Object object) throws SAFSException{
		Verifier verifier = null;

		if(PersistenceType.FILE.equals(persistenceType)){
			String filename = object.toString();
			if(FileType.JSON.equals(fileType)){
				verifier = new VerifierToJSONFile(runtime, object.toString());
			}else if(FileType.XML.equals(fileType)){
				verifier = new VerifierToXMLFile(runtime, object.toString());
			}else if(FileType.PROPERTIES.equals(fileType)){
				verifier = new VerifierToPropertiesFile(runtime, object.toString());
			}else{
				String filenameuc = filename.toUpperCase();
				if(filenameuc.endsWith(FileType.JSON.name())){
					verifier = new VerifierToJSONFile(runtime, filename);
				}else if(filenameuc.endsWith(FileType.XML.name())){
					verifier = new VerifierToXMLFile(runtime, filename);
				}else if(filenameuc.endsWith(FileType.PROPERTIES.name())){
					verifier = new VerifierToPropertiesFile(runtime, filename);
				}
			}
		}else if(PersistenceType.VARIABLE.equals(persistenceType)){
//						verifier = new VerifierToVariable(runtime, object.toString());
		}

		if(verifier==null){
			throw new SAFSException("VerifierFactory does not support Verifier of\n"
					+ "persistence-type: '"+persistenceType.name+"'\n"
					+ "file-type: '"+fileType.name+"'!\n");
		}

		return verifier;
	}
}
