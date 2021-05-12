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
 * NOV 02, 2017    (Lei Wang) Removed the XML SAX parser, use PersistorToXMLString to convert a XML string to Persistable object to compare with the actual Persistable object.
 */
package org.safs.persist;

import java.io.IOException;

import org.safs.SAFSException;
import org.safs.tools.RuntimeDataInterface;

/**
 * Verify a persistable object to an XML file, such as:
 * <pre>
 * &lt;Response&gt;
 *   &lt;StatusCode&gt;200&lt;/StatusCode&gt;
 *   &lt;Headers>{Date=Tue, 13 Dec 2016 03:29:27 GMT, Content-Length=4574, Connection=keep-alive, Content-Type=application/xml}&lt;/Headers&gt;
 *   &lt;EntityBody&gt;<b>&lt;![CDATA[</b><font color="red">&lt;?xml</font> version="1.0"?&gt;&lt;CUSTOMERList xmlns:xlink="http://www.w3.org/1999/xlink"&gt;
 *     &lt;CUSTOMER xlink:href="http://www.thomas-bayer.com/sqlrest/CUSTOMER/0/"&gt;0&lt;/CUSTOMER&gt;
 *     &lt;CUSTOMER xlink:href="http://www.thomas-bayer.com/sqlrest/CUSTOMER/49/"&gt;49&lt;/CUSTOMER&gt;
 *     &lt;/CUSTOMERList&gt;<b>]]&gt;</b>&lt;/EntityBody&gt;
 *   &lt;Request&gt;
 *     &lt;Method&gt;GET&lt;/Method&gt;
 *     &lt;Headers&gt;
 *       Content-Type:application/octet-stream, Accept:application/octet-stream
 *     &lt;/Headers&gt;
 *   &lt;/Request&gt;
 * &lt;/Response&gt;
 * </pre>
 *
 * This class uses the Java SAX XML Reader to do the work.
 * @author Lei Wang
 *
 */
public class VerifierToXMLFile extends VerifierToFile{
	/**
	 * @param runtime
	 * @param filename
	 */
	public VerifierToXMLFile(RuntimeDataInterface runtime, String filename){
		super(runtime, filename);
	}

	@Override
	public void beforeCheck(Persistable persistable, boolean... conditions)  throws SAFSException, IOException{
		super.beforeCheck(persistable, conditions);

		actualContents = persistable.getContents(defaultElementValues, ignoredFields, true);

		PersistorToXMLString persistor = new PersistorToXMLString(reader);
		Persistable expectedPersistable = persistor.unpickle(null);
		expectedContents = expectedPersistable.getContents(defaultElementValues, ignoredFields, true);
	}
}
