/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

package org.safs.rest.service;


/**
 * @author canagl
 */
public class Response {
    
	Request _request;
	
	String _status_line;
    
    String _http_version;
    int _status_code;
    String _reason_phrase;
    
    String _general_header;
    String _response_header;
    
    String _message_body;    

    String _entity_header;
    long _entity_length;
    Object _entity_body;
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
	 * @return the _general_header
	 */
	public String get_general_header() {
		return _general_header;
	}
	/**
	 * @param _general_header the _general_header to set
	 */
	public void set_general_header(String _general_header) {
		this._general_header = _general_header;
	}
	/**
	 * @return the _response_header
	 */
	public String get_response_header() {
		return _response_header;
	}
	/**
	 * @param _response_header the _response_header to set
	 */
	public void set_response_header(String _response_header) {
		this._response_header = _response_header;
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
	 * @return the _entity_header
	 */
	public String get_entity_header() {
		return _entity_header;
	}
	/**
	 * @param _entity_header the _entity_header to set
	 */
	public void set_entity_header(String _entity_header) {
		this._entity_header = _entity_header;
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
