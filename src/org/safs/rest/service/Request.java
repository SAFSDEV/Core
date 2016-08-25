/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

package org.safs.rest.service;

import java.util.Map;

/**
 * @author canagl
 */
public class Request {

	String _request_method;
	String _request_uri;
	String _request_http_version;
	
	String _headers;
	
	Object _message_body;

	/**
	 * @return the _request_method
	 */
	public String get_request_method() {
		return _request_method;
	}

	/**
	 * @param _request_method the _request_method to set
	 */
	public void set_method(String _request_method) {
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
	public void set_uri(String _request_uri) {
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
	public void set_protocol_version(String _request_http_version) {
		this._request_http_version = _request_http_version;
	}
	
	/**
	 * @return the headers
	 */
	public String get_headers() {
		return _headers;
	}

	/**
	 * @param _headers the headers to set
	 */
	public void set_headers(String _headers) {
		this._headers = _headers;
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
