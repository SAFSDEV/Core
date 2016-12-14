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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safs.SAFSException;
import org.safs.StringUtils;
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
 * @author sbjlwa
 *
 */
public class PersistorToJSONFile extends PersistorToHierarchialFile{

	/**
	 * @param runtime
	 * @param filename
	 */
	public PersistorToJSONFile(RuntimeDataInterface runtime, String filename){
		super(runtime, filename);
	}

	@Override
	protected void writeHeader(Persistable persistable) throws SAFSException, IOException {
		writer.write("{\n");
	}
	@Override
	protected void writeTailer(Persistable persistable) throws SAFSException, IOException {
		writer.write("}");
	}

	@Override
	protected void containerBegin(String tagName) throws IOException{
		writer.write(StringUtils.quote(tagName)+" : {\n");
	}
	@Override
	protected void childBegin(String key, String value) throws IOException{
		writer.write(StringUtils.quote(key)+" : "+StringUtils.quote(value));
	}
	@Override
	protected void childEnd(boolean lastTag) throws IOException{
		if(lastTag){
			writer.write("\n");
		}else{
			writer.write(",\n");
		}
	}
	@Override
	protected void containerEnd(String tagName) throws IOException{
		writer.write("}\n");
	}

	/**
	 * Escape special characters such as value occupying multiple lines, which should be escaped
	 * as characters <font color="red">\n</font>; the double quote should be escaped as
	 * <font color="red">\"</font>.<br/>
	 *
	 * @param value String, the value to escape
	 * @return String, the escaped string
	 */
	protected String escape(String value){
		String result = null;
		//escape new line
		Pattern pattern = Pattern.compile("(\\n|\\r|\\r\\n)");
		Matcher m = pattern.matcher(value);
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
			m.appendReplacement(sb, escapedNL);
		}
		m.appendTail(sb);

		//escape double quote, replace " by \"
		result = sb.toString().replace("\"", "\\\"");

		return result;
	}
}
