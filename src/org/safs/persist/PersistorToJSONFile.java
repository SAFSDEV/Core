/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 05, 2016    (SBJLWA) Initial release.
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
 * Write Persistable object to a JSON file, such as:
 * <pre>
 * {
 * "Response": {
 *   "StatusCode": "200",
 * 	 "Headers": {
 * 	    "ContentType": "text/xml"
 *   },
 *   "Request": {
 * 	    "Method": "GET",
 * 	    "Headers": "{Date=Tue, 06 Dec 2016 03:08:12 GMT, Content-Length=4574}"
 *   }
 *  }
 * }
 * </pre>
 *
 * @author sbjlwa
 *
 */
public class PersistorToJSONFile extends PersistorToFile{

	/**
	 * @param runtime
	 * @param filename
	 */
	public PersistorToJSONFile(RuntimeDataInterface runtime, String filename){
		super(runtime, filename);
	}

	@Override
	public void writeHeader(Persistable persistable) throws SAFSException, IOException {
		writer.write("{\n");
	}
	@Override
	public void writeTailer(Persistable persistable) throws SAFSException, IOException {
		writer.write("}");
	}

	public void write(Persistable persistable) throws SAFSException, IOException {

		validate(persistable);

		Map<String, Object> contents = persistable.getContents();
		String className = persistable.getClass().getSimpleName();
		Object value = null;

		writer.write(StringUtils.quote(className)+" : {\n");

		String[] keys = contents.keySet().toArray(new String[0]);
		String key = null;
		for(int i=0;i<keys.length;i++){
			key = keys[i];
			value = contents.get(key);
			if(value instanceof Persistable){
				try{
					//We should not break if some child is not persistable
					write((Persistable) value);
				}catch(SAFSPersistableNotEnableException pne){
					IndependantLog.warn(pne.getMessage());
				}
			}else{
				writer.write(StringUtils.quote(key)+" : "+StringUtils.quote(value.toString()));
			}
			if((i+1)<keys.length){
				writer.write(",\n");
			}else{
				writer.write("\n");
			}
		}

		writer.write("}");
	}
}
