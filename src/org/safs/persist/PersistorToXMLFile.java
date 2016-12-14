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

import org.safs.Constants.XMLConstants;
import org.safs.tools.RuntimeDataInterface;

/**
 * Write Persistable object to an XML file, such as:
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
 * NOTE: Be careful with the value starting with <font color="red">&lt;?xml</font>, which should be wrapped
 * as &lt;![CDATA[...]]>, as showed in the example above.
 *
 * @author Lei Wang
 *
 */
public class PersistorToXMLFile extends PersistorToHierarchialFile{

	/**
	 * @param runtime
	 * @param filename
	 */
	public PersistorToXMLFile(RuntimeDataInterface runtime, String filename){
		super(runtime, filename);
	}

	@Override
	protected void containerBegin(String tagName) throws IOException{
		writer.write("<"+tagName+">\n");
	}
	@Override
	protected void childBegin(String key, String value) throws IOException{
		writer.write("<"+key+">"+value+"</"+key+">");
	}
	@Override
	protected void containerEnd(String tagName) throws IOException{
		writer.write("</"+tagName+">\n");
	}

	/**
	 * Wrap the string in "<![CDATA[]]>" if it starts with "<?XML".
	 *
	 * @param value String, the value to escape
	 * @return String, the escaped string
	 */
	protected String escape(String value){
		String result = value;

		if(result.toUpperCase().startsWith(XMLConstants.XML_START)){
			result = XMLConstants.CDATA_START+result+XMLConstants.CDATA_END;
		}

		return result;
	}
}
