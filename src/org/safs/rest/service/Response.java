/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

package org.safs.rest.service;

import java.util.Map;

/**
 * @author canagl
 */
public class Response {
    
	Request _request;
	
	String _status_line;
    
    String _http_version;
    int _status_code;
    String _reason_phrase;
    
    
    Map<String,String> _headers;

    String _message_body;

    long _entity_length;
    Object _entity_body;

    String _content_type;

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
    
}
