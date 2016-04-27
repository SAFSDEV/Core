/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

package org.safs.rest.service;

/**
 * @author Carl Nagle
 */
public class Request {

	String _request_line;
	
	String _request_method;
	String _request_uri;
	String _request_http_version;
	
	String _request_header;
	String _general_header;
	String _entity_header;
	
	Object _message_body;

	/**
	 * @return the _request_line
	 */
	public String get_request_line() {
		return _request_line;
	}

	/**
	 * @param _request_line the _request_line to set
	 */
	public void set_request_line(String _request_line) {
		this._request_line = _request_line;
	}

	/**
	 * @return the _request_method
	 */
	public String get_request_method() {
		return _request_method;
	}

	/**
	 * @param _request_method the _request_method to set
	 */
	public void set_request_method(String _request_method) {
		this._request_method = _request_method;
	}

	/**
	 * @return the _request_uri
	 */
	public String get_request_uri() {
		return _request_uri;
	}

	/**
	 * @param _request_uri the _request_uri to set
	 */
	public void set_request_uri(String _request_uri) {
		this._request_uri = _request_uri;
	}

	/**
	 * @return the _request_http_version
	 */
	public String get_request_http_version() {
		return _request_http_version;
	}

	/**
	 * @param _request_http_version the _request_http_version to set
	 */
	public void set_request_http_version(String _request_http_version) {
		this._request_http_version = _request_http_version;
	}

	/**
	 * @return the _request_header
	 */
	public String get_request_header() {
		return _request_header;
	}

	/**
	 * @param _request_header the _request_header to set
	 */
	public void set_request_header(String _request_header) {
		this._request_header = _request_header;
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
	 * @return the _message_body
	 */
	public Object get_message_body() {
		return _message_body;
	}

	/**
	 * @param _message_body the _message_body to set
	 */
	public void set_message_body(Object _message_body) {
		this._message_body = _message_body;
	}
}
