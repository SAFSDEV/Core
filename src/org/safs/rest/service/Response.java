/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

package org.safs.rest.service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.safs.IndependantLog;
import org.safs.tools.RuntimeDataInterface;

/**
 * @author canagl
 */
public class Response {
    
	private static final String UNKNOWN_VALUE = "UNKNOWN";
	Request _request;
	
	String ID = "TO BE ASSIGNED";
	String _content_type = UNKNOWN_VALUE;
	Object _entity_body = UNKNOWN_VALUE;
	long _entity_length;
	Map<String,String> _headers;
	String _http_version = UNKNOWN_VALUE;
	String _message_body = UNKNOWN_VALUE;
	String _reason_phrase = UNKNOWN_VALUE;
	int _status_code;
	String _status_line = UNKNOWN_VALUE;

	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public String get_content_type() {
		return _content_type;
	}
	public void set_content_type(String _content_type) {
		this._content_type = _content_type;
	}
	/**
	 * @return the _status_line
	 */
	public String get_status_line() {
		return _status_line;
	}
	/**
	 * @param _status_line the _status_line to set
	 */
	public void set_status_line(String _status_line) {
		this._status_line = _status_line;
	}
	/**
	 * @return the _http_version
	 */
	public String get_http_version() {
		return _http_version;
	}
	/**
	 * @param _http_version the _http_version to set
	 */
	public void set_http_version(String _http_version) {
		this._http_version = _http_version;
	}
	/**
	 * @return the _status_code
	 */
	public int get_status_code() {
		return _status_code;
	}
	/**
	 * @param _status_code the _status_code to set
	 */
	public void set_status_code(int _status_code) {
		this._status_code = _status_code;
	}
	/**
	 * @return the _reason_phrase
	 */
	public String get_reason_phrase() {
		return _reason_phrase;
	}
	/**
	 * @param _reason_phrase the _reason_phrase to set
	 */
	public void set_reason_phrase(String _reason_phrase) {
		this._reason_phrase = _reason_phrase;
	}
	/**
	 * @return the _response_header
	 */
	public String get_headers() {
		
		return Headers.convertHeadersMapToMultiLineString(_headers);
	}
	/**
	 * @param _response_header the _response_header to set
	 */
	void set_headers(Map<String,String> _header) {
		this._headers = _header;
	}
	/**
	 * @return the _message_body
	 */
	public String get_message_body() {
		return _message_body;
	}
	/**
	 * @param _message_body the _message_body to set
	 */
	public void set_message_body(String _message_body) {
		this._message_body = _message_body;
	}
	/**
	 * @return the _entity_length
	 */
	public long get_entity_length() {
		return _entity_length;
	}
	/**
	 * @param _entity_length the _entity_length to set
	 */
	public void set_entity_length(long _entity_length) {
		this._entity_length = _entity_length;
	}
	/**
	 * @return the _entity_body
	 */
	public Object get_entity_body() {
		return _entity_body;
	}
	/**
	 * @param _entity_body the _entity_body to set
	 */
	public void set_entity_body(Object _entity_body) {
		this._entity_body = _entity_body;
	}
	/**
	 * @return the _request
	 */
	public Request get_request() {
		return _request;
	}
	/**
	 * @param _request the _request to set
	 */
	public void set_request(Request _request) {
		this._request = _request;
	}
	
	/**
	 * @return String, the response information returned from rest service.
	 */
	public String getResponseInfo(){
		return  "ID: "+ID+ "\n"+
				get_status_code() +":"+ get_status_line() +"\n"+
				get_reason_phrase() +"\n"+
				get_headers() +"\n"+
				"Message Body:\n"+
				get_message_body()+"\n"+
				"Entity Length: "+ get_entity_length() +"\n"+
				"Entity Body:\n"+ 
				get_entity_body().toString()
				;  
	}

	/**
	 * @return String, the original request information.
	 */
	public String getRequestInfo(){
		Request r = get_request();
		if(r==null) return null;
		return r.get_request_method() +" : "+ r.get_request_uri() +"\n"+
			   r.get_headers() +"\n"+
			   "Message Body:\n"+
			   r.get_message_body()+"\n";
	}

	/**
	 * This is the one to call for ALL information AFTER the REQUEST has been returned in the RESPONSE
	 * @return String, the original request information and the response information returned from rest service.
	 */
	public String toString(){
		return "\n========\nRequest: " + getRequestInfo() +"\n"+
				"=========\nResponse: "+ getResponseInfo();                
	}
    
	/**
	 * Save Response, original Request to variables prefixed with 'variablePrefix'.<br>
	 * <pre>
	 * Response is saved to variables:
	 * variablePrefix.Response.Id
	 * variablePrefix.Response.StatusCode
	 * variablePrefix.Response.HttpVersion
	 * variablePrefix.Response.ContentType
	 * ...
	 * Request is saved to variables:
	 * variablePrefix.Request.Method
	 * variablePrefix.Request.URI
	 * variablePrefix.Request.HttpVersion
	 * variablePrefix.Request.Headers
	 * ...
	 * </pre>
	 * @param runtime	RuntimeDataInterface, it provides the ability to save variable
	 * @param variablePrefix String, the variable prefix
	 * @param saveRequest boolean, if the original request should be saved
	 * @return boolean if the save operation succeed
	 */
	public boolean save(RuntimeDataInterface runtime, String variablePrefix, boolean saveRequest){
		boolean success = true;
		String debugmsg = "Response.save(): ";
		
		if(runtime==null){
			IndependantLog.error(debugmsg+"runtime is null.");
			return false;
		}
		if(variablePrefix==null || variablePrefix.trim().isEmpty()){
			IndependantLog.error(debugmsg+"variable prefix is null or empty.");
			return false;
		}


		String variable = null;
		String varResponsPrefix = variablePrefix+".Response.";
		Class<?> clazz = getClass();

		Field[] fields = clazz.getDeclaredFields();

		Object value = null;
		String fieldName = null;

		for(Field field:fields){
			try {
				fieldName = field.getName();
				if(Modifier.isFinal(field.getModifiers())){
					continue;
				}
				if("_headers".equals(fieldName)){
					value = clazz.getDeclaredMethod("get_headers").invoke(this);
				}else if("_request".equals(fieldName)){
					//do not save it in response variables
					continue;
				}else{
					value = field.get(this);
				}

				variable = varResponsPrefix+fieldName;
				if(value==null){
					value = UNKNOWN_VALUE;
					IndependantLog.debug(debugmsg+" value is null for field '"+fieldName+"', set "+UNKNOWN_VALUE+" to variable '"+variable+"'.");
				}

				runtime.setVariable(variable, value.toString());

			} catch (Exception e) {
				IndependantLog.error(debugmsg+" Met Exception "+e.getClass().getSimpleName()+", due to "+e.getMessage());
				success = false;
			}
		}

		if(saveRequest){
			String varRequestPrefix = variablePrefix+".Request.";

			if(_request!=null){
				clazz = _request.getClass();
				fields = clazz.getDeclaredFields();
				
				for(Field field:fields){
					try {
						fieldName = field.getName();
						if(Modifier.isFinal(field.getModifiers())){
							continue;
						}
						if("_message_body".equals(fieldName)){
							//TODO do not save it in request variables???
							continue;
						}else{
							value = field.get(this);
						}
						
						variable = varRequestPrefix+fieldName;
						if(value==null){
							value = UNKNOWN_VALUE;
							IndependantLog.debug(debugmsg+" value is null for field '"+fieldName+"', set "+UNKNOWN_VALUE+" to variable '"+variable+"'.");
						}
						
						runtime.setVariable(variable, value.toString());
						
					} catch (Exception e) {
						IndependantLog.error(debugmsg+" Met Exception "+e.getClass().getSimpleName()+", due to "+e.getMessage());
						success = false;
					}
				}
			}else{
				IndependantLog.error(debugmsg+"The original request is null! Cannot save it to variables.");
				success = false;
			}
		}
		
		return success;
	}
	
	/**
	 * Delete Response variables and original Request variables prefixed with 'variablePrefix'.<br>
	 * <pre>
	 * Response variables to be deleted:
	 * variablePrefix.Response.Id
	 * variablePrefix.Response.StatusCode
	 * variablePrefix.Response.HttpVersion
	 * variablePrefix.Response.ContentType
	 * ...
	 * Request variables to be deleted
	 * variablePrefix.Request.Method
	 * variablePrefix.Request.URI
	 * variablePrefix.Request.HttpVersion
	 * variablePrefix.Request.Headers
	 * ...
	 * </pre>
	 * @param runtime	RuntimeDataInterface, it provides the ability to delete variable
	 * @param variablePrefix String, the variable prefix
	 * @return boolean if the delete operation succeed
	 */
	public static boolean delete(RuntimeDataInterface runtime, String variablePrefix){
		boolean success = true;
		String debugmsg = "Response.delete(): ";
		
		if(runtime==null){
			IndependantLog.error(debugmsg+"runtime is null.");
			return false;
		}
		if(variablePrefix==null || variablePrefix.trim().isEmpty()){
			IndependantLog.error(debugmsg+"variable prefix is null or empty.");
			return false;
		}

		String variable = null;
		String varResponsPrefix = variablePrefix+".Response.";
		Class<?> clazz = Response.class;

		Field[] fields = clazz.getDeclaredFields();

		String fieldName = null;

		for(Field field:fields){
			try {
				fieldName = field.getName();
				if(Modifier.isFinal(field.getModifiers())){
					continue;
				}
				if("_request".equals(fieldName)){
					//it wasn't saved, not need to delete
					continue;
				}

				variable = varResponsPrefix+fieldName;

				//TODO can "set null to variable" really delete the variable? we need a real API to delete variable
				runtime.setVariable(variable, null);

			} catch (Exception e) {
				IndependantLog.error(debugmsg+" Met Exception "+e.getClass().getSimpleName()+", due to "+e.getMessage());
				success = false;
			}
		}

		String varRequestPrefix = variablePrefix+".Request.";

		clazz = Request.class;
		fields = clazz.getDeclaredFields();

		for(Field field:fields){
			try {
				fieldName = field.getName();
				if(Modifier.isFinal(field.getModifiers())){
					continue;
				}
				if("_message_body".equals(fieldName)){
					//TODO Have it been saved? If not, we don't need to delete it
					continue;
				}

				variable = varRequestPrefix+fieldName;

				//TODO can "set null to variable" really delete the variable? we need a real API to delete variable
				runtime.setVariable(variable, null);

			} catch (Exception e) {
				IndependantLog.error(debugmsg+" Met Exception "+e.getClass().getSimpleName()+", due to "+e.getMessage());
				success = false;
			}
		}
		
		return success;
	}
}