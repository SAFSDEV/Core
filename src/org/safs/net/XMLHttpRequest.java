/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * org.safs.net.XMLHttpRequest.java:
 * Logs for developers, not published to API DOC.
 *
 * History:
 * Jul 21, 2015    (Lei Wang) Initial release.
 */
package org.safs.net;

import java.util.Map;

import org.safs.SAFSException;
import org.safs.StringUtils;

/**
 * Represents <a href="http://www.w3schools.com/dom/dom_http.asp">XMLHttpRequest Object</a>, used to
 * execute HTTP Request within web page through javascript.<br>
 * <b>NOTE:</b> As this is performed by "AJAX XMLHttpRequest", it is probably <br>
 * not permitted to request an URL of domain other than the Application under test.<br>
 * AJAX Requests are only possible if port, protocol and domain of sender and receiver are equal.<br>
 * This means, that the following requests generally won’t work:<br>
 * Requesting https://foo.bar/target.php from http://foo.bar/source.php<br>
 * Requesting http://sub.foo.bar from http://foo.bar<br>
 * Requesting http://foo.bar:5000 from http://foo.bar<br>
 * If we meet the "cross-domain" problem, we can call {@link HttpRequest#execute(String, String, String, String...)} to perform HTTP request.
 * 
 * @author Lei Wang
 * @see org.safs.net.HttpRequest
 */
public abstract class XMLHttpRequest implements IHttpRequest{

	/**
	 * The enumeration represents <a href="http://www.w3schools.com/ajax/ajax_xmlhttprequest_onreadystatechange.asp">Ajax Ready State Code</a>, such as "0", "1", "2", "3" and "4".<br> 
	 * The enumeration has a method value(), which can return the "Ready State code" string format.<br>
	 * The enumeration has a method text(), which can return the "Ready State text" string format.<br>
	 */
	public static enum AjaxReadyState{
		/**"0", "request not initialized"*/
		NOT_INITIALIZED ("0", "request not initialized"),
		/**"1" , "server connection established"*/
		CONNECTED ("1", "server connection established"),
		/**"2", " request received"*/
		REQUEST_RECEIEVED ("2", "request received"),
		/**"3", "processing request"*/
		REQUEST_PROCESSING ("3", "processing request"),
		/**"4", "request finished and response is ready"*/
		RESPONSE_READY ("4", "request finished and response is ready");

		/**the string format for "ready state code"*/
		private final String value;
		/**the string format for "ready state text"*/
		private final String text;
		private AjaxReadyState(String value, String text){
			this.value = value;
			this.text = text;
		}
		/**getter of {@link #value}*/
		public String value() {
			return value;
		}
		/**getter of {@link #text}*/
		public String text() {
			return text;
		}
		/**
		 * Return a matched {@link AjaxReadyState} according to the ready state code.
		 * @param stateCode String, the ready state code to compare with {@link AjaxReadyState#value()}.
		 * @return HttpResponseStatus
		 */
		public static AjaxReadyState get(String stateCode){
			if(!StringUtils.isValid(stateCode)) return null;
			for(AjaxReadyState anEnum: AjaxReadyState.class.getEnumConstants()){
				if(anEnum.value().equals(stateCode.trim())) return anEnum;
			}
			return null;
		}
	}
	
	/**
	 * Send a HTTP "GET" Request and get the response from the server synchronously.<br>
	 * @param url String, the URL to request
	 * @return Map<String,String> response from the server. For detail, please refer to return result of {@link #execute(HttpCommand, String, boolean, Map, String)}
	 * @throws SAFSException
	 * @see {@link #execute(HttpCommand, String, boolean, Map, String)}
	 */
	public Map<String, Object> getURL(String url) throws SAFSException {
		return execute(HttpCommand.GET, url, false);
	}
	/**
	 * Send a HTTP "GET" Request and get the response from the server synchronously.<br>
	 * @param url String, the URL to request
	 * @param headers Map<String, String>, pairs of (key,value) to set the headers for the request.
	 * @return Map<String,String> response from the server. For detail, please refer to return result of {@link #execute(HttpCommand, String, boolean, Map, String)}
	 * @throws SAFSException
	 * @see {@link #execute(HttpCommand, String, boolean, Map, String)}
	 */
	public Map<String, Object> getURL(String url, Map<String, String> headers) throws SAFSException {
		return execute(HttpCommand.GET, url, false, headers);
	}
	
	/**
	 * Send a HTTP "POST" Request and get the response from the server synchronously.<br>
	 * @param url String, the URL to request
	 * @param data String, the data to send with the request.
	 * @return Map<String,String> response from the server. For detail, please refer to return result of {@link #execute(HttpCommand, String, boolean, Map, String)}
	 * @throws SAFSException
	 * @see {@link #execute(HttpCommand, String, boolean, Map, String)}
	 */
	public Map<String, Object> postURL(String url, String data) throws SAFSException {
		return execute(HttpCommand.POST, url, false, data);
	}
	/**
	 * Send a HTTP "POST" Request and get the response from the server.<br>
	 * @param url String, the URL to request
	 * @param async boolean, if the HTTP request will be executed asynchronously
	 * @param data String, the data to send with the request.
	 * @return Map<String,String> response from the server. For detail, please refer to return result of {@link #execute(HttpCommand, String, boolean, Map, String)}
	 * @throws SAFSException
	 * @see {@link #execute(HttpCommand, String, boolean, Map, String)}
	 */
	public Map<String, Object> postURL(String url, boolean async, String data) throws SAFSException {
		return execute(HttpCommand.POST, url, async, data);
	}
	/**
	 * Send a HTTP "POST" Request and get the response from the server.<br>
	 * @param url String, the URL to request
	 * @param async boolean, if the HTTP request will be executed asynchronously
	 * @param headers Map<String, String>, pairs of (key,value) to set the headers for the request.
	 * @param data String, the data to send with the request.
	 * @return Map<String,String> response from the server. For detail, please refer to return result of {@link #execute(HttpCommand, String, boolean, Map, String)}
	 * @throws SAFSException
	 * @see {@link #execute(HttpCommand, String, boolean, Map, String)}
	 */
	public Map<String, Object> postURL(String url, boolean async, Map<String, String> headers, String data) throws SAFSException {
		return execute(HttpCommand.POST, url, async, headers, data);
	}

	/**
	 * Send a HTTP Request and get the response from the server.<br>
	 * @param command CommandHttp, the method to execute, such as 'GET', 'POST', 'PUT' etc.
	 * @param url String, the URL to request
	 * @param async boolean, if the HTTP request will be executed asynchronously
	 * @return Map<String,String> response from the server. For detail, please refer to return result of {@link #execute(HttpCommand, String, boolean, Map, String)}
	 * @throws SAFSException
	 * @see {@link #execute(HttpCommand, String, boolean, Map, String)}
	 */
	public Map<String, Object> execute(HttpCommand command, String url, boolean async) throws SAFSException {
		return execute(command, url, async, null, null);
	}
	/**
	 * Send a HTTP Request and get the response from the server.<br>
	 * @param command CommandHttp, the method to execute, such as 'GET', 'POST', 'PUT' etc.
	 * @param url String, the URL to request
	 * @param async boolean, if the HTTP request will be executed asynchronously
	 * @param headers Map<String, String>, pairs of (key,value) to set the headers for the request.
	 * @return Map<String,String> response from the server. For detail, please refer to return result of {@link #execute(HttpCommand, String, boolean, Map, String)}
	 * @throws SAFSException
	 * @see {@link #execute(HttpCommand, String, boolean, Map, String)}
	 */
	public Map<String, Object> execute(HttpCommand command, String url, boolean async, Map<String, String> headers) throws SAFSException {
		return execute(command, url, async, headers, null);
	}
	/**
	 * Send a HTTP Request and get the response from the server.<br>
	 * @param command CommandHttp, the method to execute, such as 'GET', 'POST', 'PUT' etc.
	 * @param url String, the URL to request
	 * @param async boolean, if the HTTP request will be executed asynchronously
	 * @param data String, the data to send with the request.
	 * @return Map<String,String> response from the server. For detail, please refer to return result of {@link #execute(HttpCommand, String, boolean, Map, String)}
	 * @throws SAFSException
	 * @see {@link #execute(HttpCommand, String, boolean, Map, String)}
	 */
	public Map<String, Object> execute(HttpCommand command, String url, boolean async, String data) throws SAFSException {
		return execute(command, url, async, null, data);
	}

	/**'XMLHttpRequest_' is used a the prefix of javascript global variable.*/
	private static final String VARIABLE_PREFIX 	= "XMLHttpRequest_";
	
	/**'XMLHttpRequest_readyState', used as a javascript global variable 'window.XMLHttpRequest_readyState' to store the ajax's 'readyState' value */
	public static final String VARIABLE_READY_STATE 		= VARIABLE_PREFIX+Key.READY_STATE.value();
	/**'XMLHttpRequest_responseText', used as a javascript global variable 'window.XMLHttpRequest_responseText' to store the ajax's 'responseText' value */
	public static final String VARIABLE_RESPONSE_TEXT		= VARIABLE_PREFIX+Key.RESPONSE_TEXT.value();
	/**'XMLHttpRequest_responseXML', used as a javascript global variable 'window.XMLHttpRequest_responseXML' to store the ajax's 'responseXML' value */
	public static final String VARIABLE_RESPONSE_XML 		= VARIABLE_PREFIX+Key.RESPONSE_XML.value();
	/**'XMLHttpRequest_status', used as a javascript global variable 'window.XMLHttpRequest_status' to store the ajax's 'status' value */
	public static final String VARIABLE_STATUS				= VARIABLE_PREFIX+Key.RESPONSE_STATUS.value();
	/**'XMLHttpRequest_statusText', used as a javascript global variable 'window.XMLHttpRequest_statusText' to store the ajax's 'statusText' value */
	public static final String VARIABLE_STATUS_TEXT 		= VARIABLE_PREFIX+Key.RESPONSE_STATUS_TEXT.value();
	/**'XMLHttpRequest_responseHeaders', used as a javascript global variable 'window.XMLHttpRequest_responseHeaders' to store the ajax's 'responseHeaders' value */
	public static final String VARIABLE_RESPONSE_HEADERS	= VARIABLE_PREFIX+Key.RESPONSE_HEADERS.value();
	
	/**
	 * After AJAX executing HTTP request, call this method to get the value of {@link Key#READY_STATE}.
	 * This is more useful for asynchronous execution, as synchronous execution the Map result contains this value.
	 * @return String, the value of {@link Key#READY_STATE}.
	 * @see #execute(HttpCommand, String, boolean, Map, String)
	 */
	public abstract String getReadyState();

}
