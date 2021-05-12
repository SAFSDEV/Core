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
 * DEC 13, 2016    (Lei Wang) Initial release.
 * NOV 02, 2017    (Lei Wang) Removed the JSON parser, use PersistorToJSONString to convert a JSON string to Persistable object to compare with the actual Persistable object.
 */
package org.safs.persist;

import java.io.IOException;

import org.safs.SAFSException;
import org.safs.tools.RuntimeDataInterface;

/**
 * Verify a persistable object to a JSON file, such as:
 * <pre>
 * {
 * "Response": {
 *   "StatusCode": "200",
 *   "Headers" : "{Date=Tue, 13 Dec 2016 03:32:13 GMT, Content-Length=4574, Content-Type=application/xml}",
 *   "EntityBody" : "&lt;?xml version=\"1.0\"?>&lt;CUSTOMERList xmlns:xlink=\"http://www.w3.org/1999/xlink\"&gt;\n    &lt;CUSTOMER xlink:href=\"http://www.thomas-bayer.com/sqlrest/CUSTOMER/0/\"&gt;0&lt;/CUSTOMER&gt;    \n&lt;/CUSTOMERList&gt;",
 *   "Request": {
 *      "Method": "GET",
 *      "Headers": "{Date=Tue, 06 Dec 2016 03:08:12 GMT, Content-Length=4574}"
 *   }
 *  }
 * }
 * </pre>
 *
 * This class uses the Java SAX XML Reader to do the work.
 * @author Lei Wang
 *
 */
public class VerifierToJSONFile extends VerifierToFile{
	/**
	 * @param runtime
	 * @param filename
	 */
	public VerifierToJSONFile(RuntimeDataInterface runtime, String filename){
		super(runtime, filename);
	}

	@Override
	public void beforeCheck(Persistable persistable, boolean... conditions)  throws SAFSException, IOException{
		super.beforeCheck(persistable, conditions);

		actualContents = persistable.getContents(defaultElementValues, ignoredFields, false);

		PersistorToJSONString persistor = new PersistorToJSONString(reader);
		Persistable expectedPersistable = persistor.unpickle(null);
		expectedContents = expectedPersistable.getContents(defaultElementValues, ignoredFields, false);
	}
}
