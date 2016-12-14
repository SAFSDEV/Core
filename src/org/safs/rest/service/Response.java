/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */
/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 02, 2016    (Lei Wang) Make this class persistable.
 */
package org.safs.rest.service;

import java.util.HashMap;
import java.util.Map;

import org.safs.persist.PersistableDefault;

/**
 * @author Carl Nagle
 */
public class Response extends PersistableDefault{

	protected final static Map<String, String> fieldToPersistKeyMap = new HashMap<String, String>();

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

	static{
		fieldToPersistKeyMap.put("_request", "Request");
		fieldToPersistKeyMap.put("ID", "ID");
		fieldToPersistKeyMap.put("_content_type", "ContentType");
		fieldToPersistKeyMap.put("_entity_body", "EntityBody");
		fieldToPersistKeyMap.put("_entity_length", "EntityLength");
		fieldToPersistKeyMap.put("_headers", "Headers");
		fieldToPersistKeyMap.put("_http_version", "HttpVersion");
		fieldToPersistKeyMap.put("_message_body", "MessageBody");
		fieldToPersistKeyMap.put("_reason_phrase", "ReasonPhrase");
		fieldToPersistKeyMap.put("_status_code", "StatusCode");
		fieldToPersistKeyMap.put("_status_line", "StatusLine");
	}

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
		this._request.setParent(this);
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
				get_entity_body()
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

	@Override
	public Map<String, String> getPersitableFields() {
		return fieldToPersistKeyMap;
	}
}