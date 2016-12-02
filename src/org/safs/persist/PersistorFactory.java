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

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.persist.Persistor.FileType;
import org.safs.persist.Persistor.Type;
import org.safs.tools.RuntimeDataInterface;

/**
 * @author Lei Wang
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
			if(Type.FILE.name.equals(persistenceType.name)){
				new PersistorToFile(runtime, fileType, object.toString());
			}else if(Type.VARIABLE.equals(persistenceType.name)){
				new PersistorToVariable(runtime, object.toString());
			}
		}catch(Exception e){
			IndependantLog.error(StringUtils.debugmsg(false)+" Failed to create Persistor, met "+StringUtils.debugmsg(e));
		}
		
		return persistor;

	}
}
