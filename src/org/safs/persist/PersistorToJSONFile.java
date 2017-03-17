/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 05, 2016    (SBJLWA) Initial release.
 * MAR 15, 2017    (SBJLWA) Supported the unpickle functionality.
 */
package org.safs.persist;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.safs.Constants.JSONConstants;
import org.safs.IndependantLog;
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

	JSONObject jsonObject = null;

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
	protected void containerBegin(String className) throws IOException{
		writer.write(StringUtils.quote(getTagName(className))+" : {\n");
		writer.write(StringUtils.quote(JSONConstants.PROPERTY_CLASSNAME)+" : "+StringUtils.quote(className)+",\n"/*The container should have more children, so add a comma after the field '$classname'*/);
	}
	@Override
	protected void childBegin(String key, String value) throws IOException{
		writer.write(StringUtils.quote(key)+" : "+value);
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
	protected void containerEnd(String className) throws IOException{
		writer.write("}\n");
	}

	protected void beforeUnpickle()  throws SAFSException, IOException{
		super.beforeUnpickle();

		try {
			jsonObject = new JSONObject(new JSONTokener(reader));

		} catch (JSONException e) {
			throw new SAFSException("Failed to create JSON Object! Met "+e.toString());
		}
	}

	protected Persistable doUnpickle()  throws SAFSException, IOException{
		if(jsonObject==null || jsonObject.length()!=1){
			throw new SAFSException("JsonObject is null or the size is not 1. JsonObject should contain only one field, which is a Persistable object.");
		}
		JSONObject persistableObj = null;
		Iterator<String> keys = jsonObject.keys();
		if(keys.hasNext()){
			String persistableObject = keys.next();
			String persistenceName = persistFile==null? getPersistenceName():persistFile.getAbsolutePath();
			IndependantLog.debug("unpickling '"+persistableObject+"' of persistence '"+persistenceName+"'.");
			persistableObj = jsonObject.getJSONObject(persistableObject);
		}else{
			throw new SAFSException("There are no more objects in JsonObject.");
		}

		return unpickleParse(persistableObj);
	}

	/**
	 * Try to convert <a href="http://safsdev.github.io/configure/auth2.xml">JSON File</a> to a {@link Persistable} object.<br/>
	 * In JSON file, the special JSON key {@link JSONConstants#PROPERTY_CLASSNAME} holds the name of the class which the
	 * JSONObject represents; the other keys are the name of the fields of that class.<br/>
	 *
	 * @param body JSONObject
	 * @return Persistable
	 * @throws SAFSException
	 */
	private Persistable unpickleParse(JSONObject body) throws SAFSException{
		Persistable persistable = null;

		try {
			String className = body.getString(JSONConstants.PROPERTY_CLASSNAME);
			Object object = Class.forName(className).newInstance();
			if(object instanceof Persistable){
				persistable = (Persistable) object;

				body.remove(JSONConstants.PROPERTY_CLASSNAME);
				Iterator<String> fields =  body.keys();
				String field = null;
				Object value = null;
				JSONArray arrayValue = null;
				while(fields.hasNext()){
					field = fields.next();
					value = body.get(field);
					if(value instanceof JSONObject){
						persistable.setField(field, unpickleParse((JSONObject)value));

					}else if(value instanceof JSONArray){
						arrayValue = (JSONArray) value;
						Object[] values = arrayValue.toList().toArray();
						persistable.setField(field, values);

					}else{
						persistable.setField(field, value);
					}
				}

			}else{
				throw new SAFSException(className+" is not a Persistable!");
			}
		} catch (ClassNotFoundException|JSONException | InstantiationException | IllegalAccessException e) {
			throw new SAFSException(e.toString());
		}

		return persistable;
	}

	@Override
	protected boolean stringNeedQuoted(){
		return true;
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

		return super.escape(result);
	}
}
