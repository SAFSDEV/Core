/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * org.safs.net.IHttpRequest.java:
 * Logs for developers, not published to API DOC.
 *
 * History:
 * Jul 22, 2015    (sbjlwa) Initial release.
 */
package org.safs.net;

import java.util.Map;

import org.safs.SAFSException;
import org.safs.StringUtils;
import org.safs.net.XMLHttpRequest.AjaxReadyState;

/**
 * An interface for sending HTTP request and receiving response.<br>
 * There are 2 implementations:<br>
 * one is {@link HttpRequest}, implemented by HttpURLConnection.<br>
 * the other is {@link XMLHttpRequest}, implemented by javascript AJAX.<br>
 * 
 * @see HttpRequest
 * @see XMLHttpRequest
 * @author sbjlwa
 */
public interface IHttpRequest {
	
	/**
	 * The enumeration represents some of <a href="http://www.w3schools.com/dom/dom_http.asp">XMLHttpRequest Object Properties</a>,
	 * such as "status", "statusText", "responseText" etc. and some other strings like "responseHeaders", "requestHeaders".<br>
	 * They will be used as keys in Map result returned by {@link IHttpRequest#execute(HttpCommand, String, boolean, Map, String)}.<br>
	 * The enumeration has a method value(), which can return the string format.<br>
	 */
	public static enum Key{
		/**
		 * 'readyState'
		 * <pre> 
		 * Holds the status of the XMLHttpRequest. Changes from 0 to 4:
		 * 0: request not initialized
		 * 1: server connection established
		 * 2: request received
		 * 3: processing request
		 * 4: request finished and response is ready
		 * </pre>
		 * @see AjaxReadyState
		 */
		READY_STATE ("readyState"),
		/**'responseText' the HTTP response data as a string */
		RESPONSE_TEXT ("responseText"),
		/**'responseXML' the HTTP response data as XML data*/
		RESPONSE_XML ("responseXML"),
		/**
		 * 'status' the status-number (e.g. "404" for "Not Found" or "200" for "OK")
		 * @see org.safs.net.HttpRequest.HttpResponseStatus 
		 */
		RESPONSE_STATUS ("status"),
		/**'statusText' the status-text (e.g. "Not Found" or "OK")*/
		RESPONSE_STATUS_TEXT ("statusText"),
		/**'responseHeaders' the HTTP response headers as a string*/
		RESPONSE_HEADERS ("responseHeaders"),
		/**'requestHeaders' the HTTP request headers as a string*/
		REQUEST_HEADERS ("requestHeaders");
		
		/**string format for the key*/
		private final String value;
		private Key(String value){
			this.value = value;
		}
		/**getter of {@link #value}*/
		public String value() {
			return value;
		}
	}
	
	/**
	 * The enumeration represents the <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html">HTTP request command</a>, such as "GET", "POST", "PUT", "DELETE" etc.<br> 
	 * The enumeration has a method value(), which can return the string format.<br>
	 */
	public static enum HttpCommand{
		/**"OPTIONS"*/
		OPTIONS ("OPTIONS"),
		/**"GET"*/
		GET ("GET"),
		/**"HEAD"*/
		HEAD ("HEAD"),
		/**"POST"*/
		POST ("POST"),
		/**"PUT"*/
		PUT ("PUT"),
		/**"DELETE"*/
		DELETE ("DELETE"),
		/**"TRACE"*/
		TRACE ("TRACE"),
		/**"CONNECT"*/
		CONNECT ("CONNECT");
		
		/**the string format of this HTTP command*/
		private final String value;
		private HttpCommand(String value){
			this.value = value;
		}
		/** getter of {@link #value} */
		public String value() {
			return value;
		}
		/**
		 * Return a matched {@link HttpCommand} according to the command string.
		 * @param command String, the string command to compare with {@link HttpCommand#value()}.
		 * @return HttpCommand
		 */
		public static HttpCommand get(String command){
			if(!StringUtils.isValid(command)) return null;
			for(HttpCommand anEnum: HttpCommand.class.getEnumConstants()){
				if(anEnum.value().equalsIgnoreCase(command.trim())) return anEnum;
			}
			return null;
		}
	}
	
	/**
	 * The enumeration represents subset of the <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">HTTP header keys</a><br>
	 * The enumeration has a method value(), which can return the string format.<br>
	 */
	public static enum HttpHeader{
		/**"Accept"*/
		ACCEPT ("Accept"),
		/**"Content-Charset"*/
		ACCEPT_CHARSET ("Accept-Charset"),
		/**"Content-Encoding"*/
		CONTENT_ENCODING ("Content-Encoding"),
		/**"Content-Language"*/
		CONTENT_LANGUAGE ("Content-Language"),
		/**"Content-Length"*/
		CONTENT_LENGTH ("Content-Length"),
		/**"Content-Location"*/
		CONTENT_LOCATION ("Content-Location"),
		/**"Content-MD5"*/
		CONTENT_MD5 ("Content-MD5"),
		/**"Content-Range"*/
		CONTENT_RANGE ("Content-Range"),
		/**"Content-Type"*/
		CONTENT_TYPE ("Content-Type");//text/html; charset=ISO-8859-1
		
		/**the string format of this HTTP header*/
		private final String value;//
		private HttpHeader(String value){
			this.value = value;
		}
		/**getter of {@link #value}*/
		public String value() {
			return value;
		}
	}
	
	/**
	 * The enumeration represents subset of the <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP request Response Status Code</a>, such as "200", "403", "404", "500" etc.<br> 
	 * The enumeration has a method value(), which can return the "HTTP status code" string format.<br>
	 * The enumeration has a method text(), which can return the "HTTP status text" string format.<br>
	 */
	public static enum HttpResponseStatus{
		/**"200", "OK"*/
		OK ("200", "OK"),
		/**"201", "Created"*/
		CREATED ("201", "Created"),
		/**"202", "Accepted"*/
		ACCEPTED ("202", "Accepted"),
		/**"400", "Bad Request"*/
		BAD_REQUEST ("400", "Bad Request"),
		/**"401", "Unauthorized"*/
		UNAUTHORIZED ("401", "Unauthorized"),
		/**"403", "Forbidden"*/
		FORBIDDEN ("403", "Forbidden"),
		/**"404", "Not Found"*/
		NOTFOUND ("404", "Not Found"),
		/**"500", "Internal Server Error"*/
		INTERNAL_SERVER_ERROR ("500", "Internal Server Error");
		
		/**the string format for HTTP Status*/
		private final String value;
		/**the string format for HTTP Status Text*/
		private final String text;
		private HttpResponseStatus(String value, String text){
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
		 * Return a matched {@link HttpResponseStatus} according to the status code.
		 * @param statusCode String, the status code to compare with {@link HttpResponseStatus#value()}.
		 * @return HttpResponseStatus
		 */
		public static HttpResponseStatus get(String statusCode){
			if(!StringUtils.isValid(statusCode)) return null;
			for(HttpResponseStatus anEnum: HttpResponseStatus.class.getEnumConstants()){
				if(anEnum.value().equals(statusCode.trim())) return anEnum;
			}
			return null;
		}
	}
	
	/**
	 * Send a HTTP Request and get the response from the server.<br>
	 * <b>Note:</b>
	 * <ol>
	 * <li>If this is performed by "AJAX XMLHttpRequest", it is probable not permitted to request 
	 *     an URL of domain other than the Application under test.
	 * </ol>
	 * @param command HttpCommand, the method to execute, such as 'GET', 'POST', 'PUT' etc.
	 * @param url String, the URL to request
	 * @param async boolean, if the HTTP request will be executed asynchronously.<br>
	 *                       <ul>
	 *                       <li>false, the request execution is synchronous;<br>
	 *                       <li><b>true, the request execution is asynchronous, the returned Map may contain nothing.</b>
	 *                       </ul>
	 * @param headers Map<String, String>, pairs of (key,value) to set the headers for the request.
	 * @param data String, the data to send with the request.
	 * @return Map<String,Object> response from the server<br>
	 * If the execution is synchronous, it may contains key as:
	 * <ul>
	 *         <li>{@link Key#READY_STATE}:				ready state, changes from 0 to 4, ONLY for AJAX XMLHttpRequest execution.
	 *         <li>{@link Key#RESPONSE_STATUS}:			HTTP response status number
	 *         <li>{@link Key#RESPONSE_STATUS_TEXT}:	HTTP response status text
	 *         <li>{@link Key#RESPONSE_HEADERS}: 	 	HTTP response headers
	 *         <li>{@link Key#RESPONSE_TEXT}: 	 		HTTP response as string
	 *         <li>{@link Key#RESPONSE_XML}: 	 		HTTP response as XML data
	 * </ul>
	 * If the execution is asynchronous, it contains key as:
	 * <pre>
	 *   {@link Key#READY_STATE}: ready state, changes from 0 to 4, ONLY for AJAX XMLHttpRequest execution.
	 *   for other values, they are not in the Map result, we can get them from following methods:
	 *       {@link #getReadyState()}
	 *       {@link #getHttpStatus()}
	 *       {@link #getHttpStatusText()}
	 *       {@link #getResponseText()}
	 *       {@link #getResponseXml()}
	 *       {@link #getResponseHeaders()}
	 * </pre>
	 * @throws SAFSException when there are some error occurs.
	 * @see {@link #getReadyState()}
	 */
	public Map<String, Object> execute(HttpCommand command, String url, boolean async, Map<String, String> headers, String data) throws SAFSException;

	/**
	 * After executing HTTP request, call this method to get the value of {@link Key#RESPONSE_HEADERS}.
	 * This is more useful for asynchronous execution; for synchronous execution the Map result contains this value.
	 * @return Object, the value of {@link Key#RESPONSE_HEADERS}.
	 * @see #execute(HttpCommand, String, boolean, Map, String)
	 */
	public Object getResponseHeaders();
	/**
	 * After executing HTTP request, call this method to get the value of {@link Key#RESPONSE_TEXT}.
	 * This is more useful for asynchronous execution; for synchronous execution the Map result contains this value.
	 * @return String, the value of {@link Key#RESPONSE_TEXT}.
	 * @see #execute(HttpCommand, String, boolean, Map, String)
	 */
	public String getResponseText();
	/**
	 * After executing HTTP request, call this method to get the value of {@link Key#RESPONSE_XML}.
	 * This is more useful for asynchronous execution; for synchronous execution the Map result contains this value.
	 * @return Object, the value of {@link Key#RESPONSE_XML}.
	 * @see #execute(HttpCommand, String, boolean, Map, String)
	 */
	public Object getResponseXml();
	/**
	 * After executing HTTP request, call this method to get the value of {@link Key#RESPONSE_STATUS}.
	 * This is more useful for asynchronous execution; for synchronous execution the Map result contains this value.
	 * @return String, the value of {@link Key#RESPONSE_STATUS}.
	 * @see #execute(HttpCommand, String, boolean, Map, String)
	 */
	public String getHttpStatus();
	/**
	 * After executing HTTP request, call this method to get the value of {@link Key#RESPONSE_STATUS_TEXT}.
	 * This is more useful for asynchronous execution; for synchronous execution the Map result contains this value.
	 * @return String, the value of {@link Key#RESPONSE_STATUS_TEXT}.
	 * @see #execute(HttpCommand, String, boolean, Map, String)
	 */
	public String getHttpStatusText();
}
