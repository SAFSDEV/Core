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
 * DEC 08, 2016    (Lei Wang) Initial release.
 */
package org.safs.persist;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.safs.SAFSException;
import org.safs.tools.RuntimeDataInterface;

/**
 * Verify a persistable object to an Properties file, such as:
 * <pre>
 * Response.ContentType : application/xml
 * Response.EntityBody : &lt;?xml version="1.0"?&gt;&lt;CUSTOMERList xmlns:xlink="http://www.w3.org/1999/xlink"&gt;\
 *     &lt;CUSTOMER xlink:href="http://www.thomas-bayer.com/sqlrest/CUSTOMER/0/"&gt;0&lt;/CUSTOMER&gt;\
 *     &lt;CUSTOMER xlink:href="http://www.thomas-bayer.com/sqlrest/CUSTOMER/1/"&gt;1&lt;/CUSTOMER&gt;\
 * &lt;/CUSTOMERList&gt;
 * Response.EntityLength : 0
 * Response.Headers : {Date=Mon, 12 Dec 2016 05:17:19 GMT, Content-Length=4762, Via=1.1 inetgw38 (squid), Connection=keep-alive, Content-Type=application/xml, X-Cache=MISS from inetgw38, Server=Apache-Coyote/1.1}
 * Response.Request.Headers : Content-Type:application/octet-stream
 * Accept:application/octet-stream
 * </pre>
 *
 * This class uses the Java SAX XML Reader to do the work.
 * @author Lei Wang
 *
 */
public class VerifierToPropertiesFile extends VerifierToFile{

	protected Properties properties = null;

	/**
	 * @param runtime
	 * @param filename
	 */
	public VerifierToPropertiesFile(RuntimeDataInterface runtime, String filename){
		super(runtime, filename);
		properties = new Properties();
	}

	@Override
	public void beforeCheck(Persistable persistable, boolean... conditions)  throws SAFSException, IOException{
		super.beforeCheck(persistable, conditions);

		actualContents = persistable.getContents(defaultElementValues, ignoredFields, PersistorToPropertiesFile.PERSIST_CONTAINER);

		properties.load(reader);
		Set<String> keys = properties.stringPropertyNames();
		for(String key:keys){
			expectedContents.put(key, properties.get(key));
		}
	}
}
