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
 * NOV 03, 2017    (Lei Wang) Modified unpickleParse(): If a field is a Persistable object, then set the current Persistable object as its parent.
 */
package org.safs.persist;

import java.io.Reader;
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

/**
 * Write Persistable object to a JSON string, such as:
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
public class PersistorToJSONString extends PersistorToHierarchialString{

	JSONObject jsonObject = null;

	public PersistorToJSONString(){
		super();
	}

	public PersistorToJSONString(String stringFormat){
		super(stringFormat);
	}

	public PersistorToJSONString(Reader stringFormatReader){
		super(stringFormatReader);
	}

	@Override
	protected void writeHeader(Persistable persistable) throws SAFSException {
		stringFormat.append("{\n");
	}
	@Override
	protected void writeTailer(Persistable persistable) throws SAFSException{
		stringFormat.append("}");
	}

	@Override
	protected void beforeUnpickle()  throws SAFSException{
		super.beforeUnpickle();

		try {
			jsonObject = new JSONObject(new JSONTokener(getStringFormatReader()));

		} catch (JSONException e) {
			throw new SAFSException("Failed to create JSON Object! Met "+e.toString());
		}
	}

	@Override
	protected Persistable doUnpickle()  throws SAFSException{
		if(jsonObject==null || jsonObject.length()!=1){
			throw new SAFSException("JsonObject is null or the size is not 1. JsonObject should contain only one field, which is a Persistable object.");
		}
		JSONObject persistableObj = null;
		Iterator<String> keys = jsonObject.keys();
		if(keys.hasNext()){
			String persistableObject = keys.next();
			IndependantLog.debug("unpickling '"+persistableObject+"' of persistence '"+getPersistenceName()+"'.");
			persistableObj = jsonObject.getJSONObject(persistableObject);
		}else{
			throw new SAFSException("There are no more objects in JsonObject.");
		}

		return unpickleParse(persistableObj);
	}

	/**
	 * Try to convert <a href="/configure/auth2.xml">JSON File</a> to a {@link Persistable} object.<br/>
	 * In JSON file, the special JSON key {@link JSONConstants#PROPERTY_CLASSNAME} holds the name of the class which the
	 * JSONObject represents; the other keys are the name of the fields of that class.<br/>
	 *
	 * @param body JSONObject
	 * @return Persistable
	 * @throws SAFSException
	 */
	public static Persistable unpickleParse(JSONObject body) throws SAFSException{
		Persistable persistable = null;

		try {
			String className = body.getString(JSONConstants.PROPERTY_CLASSNAME);
			Object object = Class.forName(className).newInstance();
			Persistable child = null;
			if(object instanceof Persistable){
				persistable = (Persistable) object;

				body.remove(JSONConstants.PROPERTY_CLASSNAME);
				Iterator<String> fields =  body.keys();
				String field = null;
				Object value = null;
				JSONArray jsonArray = null;
				Object arrayItem = null;
				int arrayLength = 0;
				while(fields.hasNext()){
					field = fields.next();
					value = body.get(field);
					if(value instanceof JSONObject){
						//A JSONObject object represents a Persistable object which is the child of current Persistable object
						child = unpickleParse((JSONObject)value);
						child.setParent(persistable);
						persistable.setField(field, child);

					}else if(value instanceof JSONArray){
						jsonArray = (JSONArray) value;
						arrayLength = jsonArray.length();
						if(arrayLength>0){
							arrayItem = jsonArray.get(0);
//							System.out.println("arrayItem type is "+arrayItem.getClass().getName());
							//We suppose the JSON object is a Persistable object
							if(arrayItem instanceof JSONObject){
								for(int i=0;i<arrayLength;i++){
									arrayItem = jsonArray.get(i);
									jsonArray.put(i, unpickleParse((JSONObject)arrayItem));
								}
							}
						}

						persistable.setField(field, jsonArray);

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
	@Override
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

	@Override
	protected String getContainerBegin(String className, boolean needLeadingName) {
		StringBuilder sb = new StringBuilder();
		if(needLeadingName){
			sb.append(StringUtils.quote(getTagName(className))+" : {\n");
		}else{
			//JSON file does not need leading name for array object.
			//"[ "name": {"a":value, "b":value}, "name": {"a":value, "b":value} ]" is not allowed
			//"[ {"a":value, "b":value}, {"a":value, "b":value} ]" is allowed
			sb.append(" {\n");
		}
		sb.append(StringUtils.quote(JSONConstants.PROPERTY_CLASSNAME)+" : "+StringUtils.quote(className)+",\n"/*The container should have more children, so add a comma after the field '$classname'*/);
		return sb.toString();
	}

	@Override
	protected String getContainerEnd(String className) {
		StringBuilder sb = new StringBuilder();
		sb.append("}\n");
		return sb.toString();
	}

	@Override
	protected String getChildBegin(String key, String value, String classname) {
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtils.quote(key)+" : "+value);
		return sb.toString();
	}
	@Override
	protected String getChildEnd(boolean lastChild) {
		StringBuilder sb = new StringBuilder();
		if(lastChild){
			sb.append("\n");
		}else{
			sb.append(",\n");
		}
		return sb.toString();
	}
}
