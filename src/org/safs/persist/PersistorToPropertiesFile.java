/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 05, 2016    (Lei Wang) Initial release.
 */
package org.safs.persist;

import java.io.IOException;

import org.safs.SAFSException;
import org.safs.tools.RuntimeDataInterface;

/**
 * @author Lei Wang
 *
 */
public class PersistorToPropertiesFile extends PersistorToFile{

	/**
	 * @param runtime
	 * @param filename
	 */
	public PersistorToPropertiesFile(RuntimeDataInterface runtime, String filename){
		super(runtime, filename);
	}

	@Override
	public void write(Persistable persistable) throws SAFSException, IOException{
		//TODO save to a properties file, or 2 properties files
		//one for response and one for request.
	}
}
