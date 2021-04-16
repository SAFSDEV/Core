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
 * DEC 02, 2016    (Lei Wang) Make this class persistable.
 * NOV 03, 2017    (Lei Wang) Use empty string "" instead of "UNKONWN" as field's default value.
 * FEB 07, 2018    (Lei Wang) Added getHeaders() to return headers as a Map.
 */
package org.safs.rest.service;

import java.util.HashMap;
import java.util.Map;

import org.safs.Printable;
import org.safs.persist.PersistableDefault;

/**
 * @author Carl Nagle
 */
public class Response extends PersistableDefault{

	private final static Map<String, String> fieldToPersistKeyMap = new HashMap<String, String>();

	Request _request;
	String ID = "TO BE ASSIGNED";
	String _content_type = DEFAULT_VALUE;
	Object _entity_body = DEFAULT_VALUE;
	long _entity_length;
	Map<String,String> _headers;
	String _http_version = DEFAULT_VALUE;
	String _message_body = DEFAULT_VALUE;
	String _reason_phrase = DEFAULT_VALUE;
	int _status_code;
	String _status_line = DEFAULT_VALUE;

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
	 * @return String, the _response_header as a string
	 */
	public String get_headers() {

		return Headers.convertHeadersMapToMultiLineString(_headers);
	}
	/**
	 * @return Map, the _response_header as a Map
	 */
	public Map<String,String> getHeaders() {

		return _headers;
	}
	/**
	 * @param _response_header the _response_header to set
	 */
	void set_headers(Map<String,String> _header) {
		this._headers = _header;
	}

	/**
	 * Setting from stored external values.
	 * @param _headers the multiline String headers to set
	 */
	public void set_headers(String _headers){
		this._headers = Headers.convertHeadersMultiLineStringToMap(_headers);
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
		if(_request instanceof Printable){
			((Printable) _request).setTabulation(this.getTabulation()+1);
		}
		if(_request!=null){
			_request.setParent(this);
		}
		this._request = _request;
	}

	@Override
	public Map<String, String> getPersitableFields() {
		return fieldToPersistKeyMap;
	}
}
