/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 13, 2016    (Lei Wang) Initial release.
 */
package org.safs.persist;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.StringUtils;
import org.safs.Constants.JSONConstants;
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

	JSONObject jsonObject = null;

	/**
	 * @param runtime
	 * @param filename
	 */
	public VerifierToJSONFile(RuntimeDataInterface runtime, String filename){
		super(runtime, filename);
	}

	public void beforeCheck(Persistable persistable, boolean... conditions)  throws SAFSException, IOException{
		super.beforeCheck(persistable, conditions);

		actualContents = persistable.getContents(defaultElementValues, ignoredFields, false);

		try {
			JSONTokener tokener = new JSONTokener(reader);
			jsonObject = new JSONObject(tokener);
			parse(jsonObject, null);
		} catch (JSONException e) {
			IndependantLog.debug(StringUtils.debugmsg(false)+" Failed to create JSONObject, met "+e.getMessage());
			throw new SAFSException("Failed to create JSONObject");
		}
	}

	private void parse(JSONObject jsonObject, String parentKey) {

		String debugmsg = StringUtils.debugmsg(false);

		Persistable persistable = null;
		Object className = jsonObject.opt(JSONConstants.PROPERTY_CLASSNAME);

		if(className!=null){
			try {
				Object object = Class.forName((String)className).newInstance();
				if(object instanceof Persistable){
					persistable = (Persistable) object;
				}else{
					IndependantLog.warn(debugmsg+" class '"+className+"' is not Persistable, cannot be handled!");
				}
			} catch (Exception e) {
				IndependantLog.warn(debugmsg+" Failed to instantiate class '"+className+"', due to "+e.toString());
			}
		}

	    Iterator<String> iterator = jsonObject.keys();
	    String flatKey = null;
	    String key = null;
	    Object value = null;
	    for (; iterator.hasNext();) {
	    	key = iterator.next();

	    	//"$classname" (reserved for unpickle) is not a valid field, we don't verify it.
	    	if(JSONConstants.PROPERTY_CLASSNAME.equals(key)){
	    		continue;
	    	}

	    	if(jsonObject.isNull(key)){
	    		IndependantLog.warn(debugmsg+" the value is null for key '"+key+"'.");
	    		continue;
	    	}

	    	try {
	    		value = jsonObject.get(key);
	    	} catch (JSONException e) {
	    		IndependantLog.warn(debugmsg+" Failed to get value for key '"+key+"', met "+e.toString());
	    		continue;
	    	}

	    	flatKey = StringUtils.isValid(parentKey)? parentKey+"."+key:key;

	        if (value instanceof String)
	            expectedContents.put(flatKey, (String) value);
	        else if (value instanceof Integer)
	            expectedContents.put(flatKey, (Integer) value);
	        else if (value instanceof Long)
	            expectedContents.put(flatKey, (Long) value);
	        else if (value instanceof Double)
	            expectedContents.put(flatKey, (Double) value);
	        else if (value instanceof Boolean)
	            expectedContents.put(flatKey, (Boolean) value);
	        else if (value instanceof JSONObject)
	            parse((JSONObject) value, flatKey);
	        else if (value instanceof JSONArray){
	        	if(persistable!=null){
	        		persistable.setField(key, value);
	        		expectedContents.put(flatKey, persistable.getField(key));
	        	}else{
	        		parse((JSONArray) value, flatKey);
	        	}
	        }
	        else{
	        	IndependantLog.warn(debugmsg+"not prepared for converting instance of class " + value.getClass());
	        	expectedContents.put(flatKey, value.toString());
	        }
	    }
	}

	private void parse(JSONArray jsonArray, String parentKey) {
		String debugmsg = StringUtils.debugmsg(false);

	    String flatKey = null;
	    Object value = null;

	    for (int i = 0; i < jsonArray.length(); i++) {
	    	if(jsonArray.isNull(i)){
	    		IndependantLog.warn(debugmsg+" the value is null for jsonArray["+i+"].");
	    		continue;
	    	}

	    	try {
	    		value = jsonArray.get(i);
	    	} catch (JSONException e) {
	    		IndependantLog.warn(debugmsg+" Failed to get jsonArray["+i+"], met "+e.toString());
	    		continue;
	    	}

	    	flatKey = StringUtils.isValid(parentKey)? parentKey+"."+i : String.valueOf(i);


	    	if (value instanceof String)
	            expectedContents.put(flatKey, (String) value);
	        else if (value instanceof Integer)
	            expectedContents.put(flatKey, (Integer) value);
	        else if (value instanceof Long)
	            expectedContents.put(flatKey, (Long) value);
	        else if (value instanceof Double)
	            expectedContents.put(flatKey, (Double) value);
	        else if (value instanceof Boolean)
	            expectedContents.put(flatKey, (Boolean) value);
	        else if (value instanceof JSONObject)
	            parse((JSONObject) value, flatKey);
	        else if (value instanceof JSONArray)
	        	parse((JSONArray) value, flatKey);
	        else{
	        	IndependantLog.warn(debugmsg+"not prepared for converting instance of class " + value.getClass());
	        	expectedContents.put(flatKey, value.toString());
	        }
	    }
	}

	private static final Map<String,String> defaultElementValues = null;
//	/**
//	 * '<b>\n</b>'<br/>
//	 * For container Element, such as <b>Response</b> in XML
//	 * <pre>
//	 * &lt;Response&gt;
//	 *   &lt;StatusCode&gt;200&lt;/StatusCode&gt;
//	 * &lt;/Response&gt;
//	 * </pre>
//	 * actually it doesn't have any string value,
//	 * but the XML SAX parser will assign a "<b>\n</b>" to it.
//	 */
//	private static final String CONTAINER_ELEMENT_DEFAULT_VALUE = "";
//
//	static{
//		defaultElementValues = new HashMap<String,String>();
//		defaultElementValues.put(Persistable.CONTAINER_ELEMENT, CONTAINER_ELEMENT_DEFAULT_VALUE);
//	}

	void debug(String msg){
		System.out.println(msg);
	}
}
