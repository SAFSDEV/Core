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
import java.util.Map;

import org.safs.Constants.XMLConstants;
import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.SAFSPersistableNotEnableException;
import org.safs.tools.RuntimeDataInterface;

/**
 * Write Persistable object to an XML file, such as:
 * <pre>
 * &lt;Response&gt;
 *   &lt;StatusCode&gt;200&lt;/StatusCode&gt;
 *   &lt;Headers&gt;
 *     &lt;ContentType&gt;text/xml&lt;/ContentType&gt;
 *   &lt;/Headers&gt;
 *   &lt;Request&gt;
 *     &lt;Method&gt;GET&lt;/Method&gt;
 *     &lt;Headers&gt;
 *       Content-Type:application/octet-stream, Accept:application/octet-stream
 *     &lt;/Headers&gt;
 *   &lt;/Request&gt;
 * &lt;/Response&gt;
 * </pre>
 * @author Lei Wang
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
		validate(persistable);

		Map<String, Object> contents = persistable.getContents();
		String className = persistable.getClass().getSimpleName();
		Object value = null;

		writer.write("<"+className+">\n");

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
				writer.write("<"+key+">"+wrapInCDATA(value)+"</"+key+">");
			}
			writer.write("\n");
		}

		writer.write("</"+className+">\n");
	}

	/**
	 * Wrap the string in "<![CDATA[]]>" if it starts with "<?XML".
	 * @param value Object, the object to write to an XML file.
	 * @return String, the wrapped String.
	 */
	protected String wrapInCDATA(Object value){
		String result = value.toString();

		if(result.toUpperCase().startsWith(XMLConstants.XML_START)){
			result = XMLConstants.CDATA_START+result+XMLConstants.CDATA_END;
		}

		return result;
	}
}
