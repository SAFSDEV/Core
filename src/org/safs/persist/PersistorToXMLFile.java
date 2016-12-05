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
import org.safs.tools.RuntimeDataInterface;

/**
 * Write Response/Request object to an XML file, such as:
 * <pre>
 * &lt;Response>
 *   &lt;StatusCode>200&lt;/StatusCode>
 *   &lt;Headers>
 *     &lt;ContentType>text/xml&lt;/ContentType>
 *   &lt;/Headers>
 *   &lt;Request>
 *     &lt;Method>GET&lt;/Method>
 *     &lt;Headers>
 *       &lt;Accept>text/xml;application/json&lt;/Accept>
 *     &lt;/Headers>
 *   &lt;/Request>
 * &lt;/Response>
 * </pre>
 * @author sbjlwa
 *
 */
public class PersistorToXMLFile extends PersistorToFile{

	/**
	 * @param runtime
	 * @param filename
	 */
	public PersistorToXMLFile(RuntimeDataInterface runtime, String filename){
		super(runtime, filename);
	}
	
	@Override
	public void write(Persistable persistable) throws SAFSException, IOException{
		
		Map<String, Object> contents = persistable.getContents();
		String className = persistable.getClass().getSimpleName();
		Object value = null;
		
		bufferedWriter.write("<"+className+">\n");
		
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
				bufferedWriter.write("<"+key+">"+value.toString()+"</"+key+">");
			}
			bufferedWriter.write("\n");
		}
		
		bufferedWriter.write("</"+className+">\n");
	}
}
