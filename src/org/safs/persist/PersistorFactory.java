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

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.persist.Persistor.FileType;
import org.safs.persist.Persistor.Type;
import org.safs.tools.RuntimeDataInterface;

/**
 * @author sbjlwa
 *
 */
public class PersistorFactory {
	
	/**
	 * 
	 * @param persistenceType Type enum object.
	 * @param fileType FileType enum object, ONLY useful when persistenceType is Type.FILE
	 * @param runtime RuntimeDataInterface, required by a concrete Persistor.
	 * @param object Object, an object required by a concrete Persistor.
	 * @return Persistor
	 */
	public static Persistor create(Type persistenceType, FileType fileType, RuntimeDataInterface runtime , Object object){
		Persistor persistor = null;
		
		try{
			if(Type.FILE.equals(persistenceType)){
				if(FileType.JSON.equals(fileType)){
					persistor = new PersistorToJSONFile(runtime, object.toString());
				}else if(FileType.XML.equals(fileType)){
					persistor = new PersistorToXMLFile(runtime, object.toString());
				}else if(FileType.PROPERTIES.equals(fileType)){
					persistor = new PersistorToPropertiesFile(runtime, object.toString());
				}else{
					IndependantLog.debug(StringUtils.debugmsg(false)+" file type '"+fileType.name+"' has not been supported yet!");
				}
			}else if(Type.VARIABLE.equals(persistenceType)){
				persistor = new PersistorToVariable(runtime, object.toString());
			}
		}catch(Exception e){
			IndependantLog.error(StringUtils.debugmsg(false)+" Failed to create Persistor, met "+StringUtils.debugmsg(e));
		}
		
		return persistor;

	}
}
