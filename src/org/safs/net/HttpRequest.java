/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * org.safs.net.HttpRequest.java:
 * Logs for developers, not published to API DOC.
 *
 * History:
 * JUL 06, 2015    (sbjlwa) Initial release.
 * JUL 08, 2015    (sbjlwa) Made modification to encode URL and form content.
 * JUL 22, 2015    (sbjlwa) Refactor to implement IHttpRequest.
 */
package org.safs.net;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.safs.IndependantLog;
import org.safs.SAFSException;
import org.safs.StringUtils;

/**
 * Send HTTP Request like "Get", "Post", "Put" etc. by Java HttpURLConnection.<br>
 * If a PROXY server is needed to visit web site, we need to specify the <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html">PROXY server</a> as JVM parameter, such as:<br>
 * -Dhttp.proxyHost=proxy.host.name<br>
 * -Dhttp.proxyPort=portNumber<br>
 * -Dhttps.proxyHost=proxy.host.name<br>
 * -Dhttps.proxyPort=portNumber<br>
 * 
 * @author sbjlwa
 */
public class HttpRequest implements IHttpRequest{
	/**
	 * 'application/x-www-form-urlencoded', it is a content type, refer to <a href="http://www.w3.org/TR/html401/interact/forms.html#form-content-type">its definition</a>
	 */
	public static final String CONTENT_TYPE_APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
	/** 'charset=', it is used to describe the content charset in {@link HttpHeader#CONTENT_TYPE} */
	public static final String CHARSET_EQUAL = "charset=";
	/** false */
	public static final boolean DEFAULT_INCLUDE_HEARERS = false;
	
	/** The value to set to the HTTP header {@link HttpHeader#CONTENT_TYPE}, the default value is {@link #CONTENT_TYPE_APPLICATION_FORM_URLENCODED}. */
	protected String contentType = CONTENT_TYPE_APPLICATION_FORM_URLENCODED;
	/** The charset to set to the HTTP header {@link HttpHeader#CONTENT_TYPE}, the default value is {@link StringUtils#CHARSET_UTF8} */
	protected String contentCharset = StringUtils.CHARSET_UTF8;
	
	/** The value to set to the HTTP header {@link HttpHeader#CONTENT_LANGUAGE}, the default value is {@link Locale#US} */
	protected String contentLauguage = Locale.US.toString();
	/** A boolean indicating whether or not to allow caching. The default is false.*/
	protected boolean useCaches = false;
	/** A boolean indicating whether or not to include request headers information in the response. The default is false.*/
	protected boolean includeRequestHeaders = DEFAULT_INCLUDE_HEARERS;
	/** A boolean indicating whether or not to include response headers information in the response. The default is false.*/
	protected boolean includeResponseHeaders = DEFAULT_INCLUDE_HEARERS;
	
	/**getter for {@link #contentType}*/
	public String getContentType() {
		return contentType;
	}
	/**getter for {@link #contentCharset}*/
	public String getContentCharset() {
		return contentCharset;
	}
	/**getter for {@link #contentLauguage}*/
	public String getContentLauguage() {
		return contentLauguage;
	}
	/**getter for {@link #useCaches}*/
	public boolean isUseCaches() {
		return useCaches;
	}
	/**getter for {@link #includeRequestHeaders}*/
	public boolean isIncludeRequestHeaders() {
		return includeRequestHeaders;
	}
	/**setter for {@link #includeResponseHeaders}*/
	public boolean isIncludeResponseHeaders() {
		return includeResponseHeaders;
	}
	/**setter for {@link #contentType}*/
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	/**setter for {@link #contentCharset}*/
	public void setContentCharset(String contentCharset) {
		this.contentCharset = contentCharset;
	}
	/**setter for {@link #contentLauguage}*/
	public void setContentLauguage(String contentLauguage) {
		this.contentLauguage = contentLauguage;
	}
	/**setter for {@link #useCaches}*/
	public void setUseCaches(boolean useCaches) {
		this.useCaches = useCaches;
	}
	/**setter for {@link #includeRequestHeaders}*/
	public void setIncludeRequestHeaders(boolean includeRequestHeaders) {
		this.includeRequestHeaders = includeRequestHeaders;
	}
	/**setter for {@link #includeResponseHeaders}*/
	public void setIncludeResponseHeaders(boolean includeResponseHeaders) {
		this.includeResponseHeaders = includeResponseHeaders;
	}

	/**
	 * Send a simple HTTP request.<br>
	 * If a PROXY server is needed to visit web site, we need to specify the <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html">PROXY server</a> as JVM parameter, such as:<br>
	 * -Dhttp.proxyHost=proxy.host.name<br>
	 * -Dhttp.proxyPort=portNumber<br>
	 * 
	 * @param command String, the HTTP request command, it can be one of {@link CommandHttp}.
	 * @param url String, the HTTP URL accepting HTTP request
	 * @param content String, the content to send for this HTTP request.
	 * @param headerValuePairs String[], the pair of (header,value) for HTTP request, the header key can be one of {@link HeaderHttp}
	 * @return String, the response of the request
	 * @throws Exception when there are some error occurs.
	 * @deprecated Please use {@link #execute(org.safs.net.IHttpRequest.HttpCommand, String, boolean, Map, String)} instead.
	 */
	public static String execute(String command, String url, String content, String... headerValuePairs) throws Exception{
		HttpRequest request = new HttpRequest();

		//convert String... headerValuePairs to map
		Map<String, String> requestHeaders = new HashMap<String, String>();
		String header = null;
		String headerValue = null;
		for(int i=0;i<headerValuePairs.length;i++){
			header = headerValuePairs[i];
			if((i+1)<headerValuePairs.length){
				headerValue = headerValuePairs[++i];
				requestHeaders.put(header, headerValue);
			}else{
				IndependantLog.warn(StringUtils.debugmsg(false)+"Parameter request headers "+StringUtils.arrayToString(headerValuePairs)+" are not properly provided, is should be pairs of (header, value)");
			}
		}
		
		Map<String, Object> resultMap = request.execute(HttpCommand.get(command), url, false/*run synchronously*/, requestHeaders, content);
		return String.valueOf(resultMap);
	}
	
	/**
	 * Send a simple HTTP request.<br>
	 * If a PROXY server is needed to visit a site, we need to specify the <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html">PROXY server</a> as JVM parameter, such as:<br>
	 * -Dhttp.proxyHost=proxy.host.name<br>
	 * -Dhttp.proxyPort=portNumber<br>
	 * -Dhttps.proxyHost=proxy.host.name<br>
	 * -Dhttps.proxyPort=portNumber<br>
	 * <br>
	 * Besides the keys mentioned {@link IHttpRequest#execute(org.safs.net.IHttpRequest.HttpCommand, String, boolean, Map, String)}, the returned Map may also contain {@link Key#REQUEST_HEADERS}.<br>
	 * 
	 * @see IHttpRequest#execute(org.safs.net.IHttpRequest.HttpCommand, String, boolean, Map, String)
	 */
	public Map<String, Object> execute(HttpCommand command, String url, boolean async, Map<String, String> requestHeaders, String content) throws SAFSException{
		HttpURLConnection connection = null;
		String debugmsg = StringUtils.debugmsg(false);
		
		try {
			Map<String, Object> response = new HashMap<String, Object>();
			String encodedContent = content;
			//Create connection
			IndependantLog.debug(debugmsg+" original url is '"+url+"'");
			url = StringUtils.urlEncode(url);
			IndependantLog.debug(debugmsg+" encoded url is '"+url+"'");
			connection = (HttpURLConnection) (new URL(url)).openConnection();
			connection.setRequestMethod(command.value().toUpperCase());
			
			/** A URL connection can be used for input and/or output. Set the DoOutput flag to true if you intend to use the URL connection for output, false if not.
			 *  Only "post", "put" need to write to connection???*/
			boolean doOutPut = false;
			boolean _includeRequestHeaders = includeRequestHeaders;
			boolean _includeResponseHeaders = includeResponseHeaders;
			if(HttpCommand.POST.value().equals(command.value()) ||
			   HttpCommand.PUT.value().equals(command.value())){
				doOutPut = true;
			}
			if(HttpCommand.HEAD.value().equals(command.value())){
				_includeResponseHeaders = true;
			}
			
			if(doOutPut){
				connection.setDoOutput(doOutPut);
				String _contentType = contentType;
				String _contentCharset = contentCharset;
				if(_contentType.contains(CONTENT_TYPE_APPLICATION_FORM_URLENCODED)){
					/**the content should follow the rule defined at http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.1*/
					/*replaceAll(" ", "+"); replaceAll("@", "%40");*/
					
					if(_contentType.contains(CHARSET_EQUAL)){
						_contentCharset = _contentType.substring(_contentType.indexOf(CHARSET_EQUAL)+CHARSET_EQUAL.length());
					}else{
						_contentType += "; "+CHARSET_EQUAL+_contentCharset;
					}
					IndependantLog.debug(debugmsg+" using charset '"+_contentCharset+"' for request content '"+encodedContent+"'");
					encodedContent = StringUtils.urlEncode(encodedContent, _contentCharset);
					IndependantLog.debug(debugmsg+" encoded content is '"+encodedContent+"'");
				}
				connection.setRequestProperty(HttpHeader.CONTENT_TYPE.value(), _contentType);
				connection.setRequestProperty(HttpHeader.CONTENT_LANGUAGE.value(), contentLauguage);
				if(encodedContent==null){
					IndependantLog.warn(debugmsg+" the content to send is null! Change it to a void string to avoid problem.");
					encodedContent = "";
				}
				connection.setRequestProperty(HttpHeader.CONTENT_LENGTH.value(), Integer.toString(encodedContent.getBytes().length));
			}

			connection.setUseCaches(useCaches);
			
			//Set extra headers, user custom headers, may override the default header settings
			if(requestHeaders!=null){
				for(String header:requestHeaders.keySet()){
					connection.setRequestProperty(header, requestHeaders.get(header));
				}
			}

			if(_includeRequestHeaders){
				Map<String,List<String>> headers = connection.getRequestProperties();
				Set<String> keys = headers.keySet(); 
				if(!keys.isEmpty()){
					IndependantLog.debug(debugmsg+"====== HTTP REQUEST HEADERS =====\n"+headers);
					response.put(Key.REQUEST_HEADERS.value(), headers);
				}else{
					IndependantLog.debug(debugmsg+"HTTP request header is empty!");
				}
			}
			
			//Establish the actual network connection 
			connection.connect();
			
			//Write content to connection
			if(doOutPut){
				DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
				wr.writeBytes(encodedContent);
				wr.close();
			}

			if(_includeResponseHeaders){
				Map<String,List<String>> headers = connection.getHeaderFields();
				Set<String> keys = headers.keySet(); 
				if(!keys.isEmpty()){
					IndependantLog.debug(debugmsg+"====== HTTP RESPONSE HEADERS =====\n"+headers);
					response.put(Key.RESPONSE_HEADERS.value(), headers);
				}else{
					IndependantLog.debug(debugmsg+"HTTP response header is empty!");
				}
			}
			
			//Get Response from connection
			StringBuilder sb = new StringBuilder();
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line = rd.readLine();
			if(line!=null){
				IndependantLog.debug(debugmsg+"======  HTTP RESPONSE =====\n");
				while(line != null) {
					sb.append(line+"\n");
					line = rd.readLine();
				}
				IndependantLog.debug(sb.toString());
				response.put(Key.RESPONSE_TEXT.value(), sb.toString());
			}else{
				IndependantLog.debug(debugmsg+"HTTP response is empty.");
			}
			rd.close();
			return response;
		} catch (Exception e) {
			IndependantLog.error(debugmsg+StringUtils.debugmsg(e));
			throw new SAFSException(e);
		} finally {
			if(connection != null)  connection.disconnect(); 
		}
	}
		
	public static String getUsage(){
		StringBuilder sb = new StringBuilder();
		String clazzname = StringUtils.getClassName(1, true);
		
		sb.append("Usage: \n");
		sb.append("java " +clazzname + " COMMAND URL"+"\n");
		sb.append("java " +clazzname + " COMMAND URL ENCODED_CONTENT"+"\n");
		sb.append("java " +clazzname + " COMMAND URL ENCODED_CONTENT HTTP_HEADER1 VALUE1 HTTP_HEADER2 VALUE2 ..."+"\n");
		sb.append("\n");
		sb.append("COMMAND can be one of: "+ Arrays.toString(HttpCommand.values())+" \n");
		
		return sb.toString();
	}

	/**
	 * Send a simple HTTP request.<br>
	 * If a PROXY server is needed to visit web site, we need to specify the <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html">PROXY server</a> as JVM parameter, such as:<br>
	 * -Dhttp.proxyHost=proxy.host.name<br>
	 * -Dhttp.proxyPort=portNumber<br>
	 * @param args String[], the parameters for HTTP Request<br>
	 * <pre>
	 *        args[0] String, the HTTP request command, it can be one of enumeration {@link HttpCommand}.
	 *        args[1] String, the HTTP URL accepting this HTTP request.
	 *        args[2] String, the content to send for this HTTP request, it can be empty string.
	 *        from args[3], pairs of (header-key, value) will be provided to set headers for HTTP request
	 *        for example, args[3] is "Accept", args[4] is "text/plain, text/html"
	 *                     args[5] is "Expires", args[6] is "Thu, 01 Dec 1994 16:00:00 GMT"
	 * </pre>
	 * 
	 * @example
	 * <pre>
	 * java org.safs.net.HttpRequest "GET" "https://www.google.com"
	 * java org.safs.net.HttpRequest "HEAD" "https://www.google.com"
	 * java org.safs.net.HttpRequest "GET" "http://www.w3schools.com/html/action_page.php?firstname=Mickey&lastname=Mouse"
	 * java org.safs.net.HttpRequest "POST" "http://www.w3schools.com/php/welcome.php" "name=Mickey Mouse&email=mickey.mouse@disneyland.com"
	 * </pre>
	 */
	public static void main(String[] args){
		StringUtils.initIndependantLogByConsole();
		
		if(args==null || args.length<2){
			System.out.println("The parameter is not sufficient!");
			System.out.print(getUsage());
			return;
		}
		
		String command = args[0];
		String url = args[1];
		String content = args.length>2? args[2]:"";
		String[] headerValuePairs = new String[0];
		//prepare the pair of (header, value)
		if(args.length>3) headerValuePairs = Arrays.copyOfRange(args, 3, args.length);
		
		Map<String, Object> result = null;
		try {
			System.out.println("HTTP Request: "+command+" "+url+content+ StringUtils.arrayToString(headerValuePairs));
			HttpRequest request = new HttpRequest();
			
			Map<String, String> requestHeaders = new HashMap<String, String>();
			String header = null;
			String headerValue = null;
			for(int i=0;i<headerValuePairs.length;i++){
				header = headerValuePairs[i];
				if((i+1)<headerValuePairs.length){
					headerValue = headerValuePairs[++i];
					requestHeaders.put(header, headerValue);
				}else{
					System.err.println("Parameter request headers "+StringUtils.arrayToString(headerValuePairs)+" are not properly provided, is should be pairs of (header, value)");
				}
			}

			result = request.execute(HttpCommand.get(command), url, false/*run synchronously*/, requestHeaders, content);
			System.out.println("Result: ");
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Object getResponseHeaders() {
		// TODO
		IndependantLog.debug(StringUtils.debugmsg(false)+"Not implemented yet");
		return null;
	}

	public String getResponseText() {
		// TODO
		IndependantLog.debug(StringUtils.debugmsg(false)+"Not implemented yet");
		return null;
	}

	public Object getResponseXml() {
		// TODO
		IndependantLog.debug(StringUtils.debugmsg(false)+"Not implemented yet");
		return null;
	}

	public String getHttpStatus() {
		// TODO
		IndependantLog.debug(StringUtils.debugmsg(false)+"Not implemented yet");
		return null;
	}

	public String getHttpStatusText() {
		// TODO
		IndependantLog.debug(StringUtils.debugmsg(false)+"Not implemented yet");
		return null;
	}
}
