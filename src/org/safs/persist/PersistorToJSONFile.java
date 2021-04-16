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
 * DEC 05, 2016    (Lei Wang) Initial release.
 * MAR 15, 2017    (Lei Wang) Supported the unpickle functionality.
 * OCT 18, 2017    (Lei Wang) Modified unpickleParse(): Convert the each JSONObject (item in JSONArray) to Persistable.
 * OCT 24, 2017    (Lei Wang) Moved most functionalities to class PersistorToJSONString.
 */
package org.safs.persist;

import java.io.IOException;

import org.safs.SAFSException;
import org.safs.tools.RuntimeDataInterface;

/**
 * Write Persistable object to a JSON file, such as:
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
 * NOTE: Be careful with the value occupying multiple lines, which should be escaped
 * as characters <font color="red">\n</font>; the double quote " should be escaped as
 * <font color="red">\"</font>. The example is shown as above.
 *
 * @author Lei Wang
 *
 */
public class PersistorToJSONFile extends PersistorToFileDelegate{
	/**
	 * @param runtime
	 * @param filename
	 */
	public PersistorToJSONFile(RuntimeDataInterface runtime, String filename){
		super(runtime, filename);
	}

	@Override
	protected void instantiateDelegatePersitor() {
		delegatePersistor = new PersistorToJSONString();
	}

	@Override
	protected void writeHeader(Persistable persistable) throws SAFSException, IOException {
		writer.write("{\n");
	}
	@Override
	protected void writeTailer(Persistable persistable) throws SAFSException, IOException {
		writer.write("}");
	}
}
