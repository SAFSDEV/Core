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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safs.SAFSException;
import org.safs.tools.RuntimeDataInterface;

/**
 * Write Persistable object to a Properties file, such as:
 * <pre>
 * Response.ContentType : application/xml
 * Response.EntityBody : &lt;?xml version="1.0"?&gt;&lt;CUSTOMERList xmlns:xlink="http://www.w3.org/1999/xlink"&gt;\
 * \n    &lt;CUSTOMER xlink:href="http://www.thomas-bayer.com/sqlrest/CUSTOMER/0/"&gt;0&lt;/CUSTOMER&gt;\
 * \n    &lt;CUSTOMER xlink:href="http://www.thomas-bayer.com/sqlrest/CUSTOMER/1/"&gt;1&lt;/CUSTOMER&gt;\
 * \n&lt;/CUSTOMERList&gt;
 * Response.EntityLength : 0
 * Response.Headers : {Date=Mon, 12 Dec 2016 05:17:19 GMT, Content-Length=4762, Via=1.1 inetgw38 (squid), Connection=keep-alive, Content-Type=application/xml, X-Cache=MISS from inetgw38, Server=Apache-Coyote/1.1}
 * Response.Request.Headers : Content-Type:application/octet-stream
 * Accept:application/octet-stream
 * </pre>
 *
 * NOTE: Be careful with the value occupying multiple lines in the properties file, which should be escaped
 * as characters <font color="red">\</font> and <font color="red">\n</font>, as showed in the example above.
 *
 * @author Lei Wang
 *
 */
public class PersistorToPropertiesFile extends PersistorToFile{

	public static final boolean PERSIST_CONTAINER = false;

	/**
	 * @param runtime
	 * @param filename
	 */
	public PersistorToPropertiesFile(RuntimeDataInterface runtime, String filename){
		super(runtime, filename);
	}

	@Override
	public void write(Persistable persistable) throws SAFSException, IOException{
		Set<String> ignoredFields = new HashSet<String>();
		Map<String, Object> actualContents = persistable.getContents(null, ignoredFields, PERSIST_CONTAINER);

		List<String> keys = new ArrayList<String>(actualContents.keySet());

		Object value = null;
		for(String key:keys){
			value = actualContents.get(key);
			writer.write(key+" : "+ escape(value.toString()) +"\n");
		}
	}

	/**
	 * Escape special characters such as value occupying multiple lines in the properties file,
	 * which should be escaped as characters <font color="red">\</font> and <font color="red">\n</font>.<br/>
	 *
	 * @param value String, the value to escape
	 * @return String, the escaped string
	 */
	protected String escape(String value){
		Pattern newLine = Pattern.compile("(\\n|\\r|\\r\\n)");
		Matcher m = newLine.matcher(value);
		StringBuffer sb = new StringBuffer();
		String nl = null;
		String escapedNL = null;
		while(m.find()){
			nl = m.group(1);
			if("\n".equals(nl)){
				escapedNL = "\\\\n";
			}else if("\r".equals(nl)){
				escapedNL = "\\\\r";
			}else if("\r\n".equals(nl)){
				escapedNL = "\\\\r\\\\n";
			}
			m.appendReplacement(sb, "\\\\"+m.group(1)+escapedNL);
		}
		m.appendTail(sb);
		return sb.toString();
	}
}
