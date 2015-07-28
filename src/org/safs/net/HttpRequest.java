/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * org.safs.net.HttpRequest.java:
 * Logs for developers, not published to API DOC.
 *
 * History:
 * JUL 6, 2015    (sbjlwa) Initial release.
 * JUL 8, 2015    (sbjlwa) Made modification to encode URL and form content.
 */
package org.safs.net;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.safs.IndependantLog;
import org.safs.StringUtils;
/**
 * An utility to send HTTP Request like "Get", "Post", "Put" etc. by Java.<br>
 * If a PROXY server is needed to visit web site, we need to specify the <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html">PROXY server</a> as JVM parameter, such as:<br>
 * -Dhttp.proxyHost=proxy.host.name<br>
 * -Dhttp.proxyPort=portNumber<br>
 * 
 * @author sbjlwa
 */
public class HttpRequest{
	
	/**
	 * The enumeration represents the <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html">HTTP request command</a>, such as "GET", "POST", "PUT", "DELETE" etc.<br> 
	 * The enumeration has a method value(), which can return the command string format.<br>
	 */
	public static enum CommandHttp{
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
		
		private final String value;//the string format of this HTTP command
		CommandHttp(String value){
			this.value = value;
		}
		public String value() {
			return value;
		}
	}
	
	/**
	 * The enumeration represents part of the <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">HTTP header keys</a><br>
	 */
	public static enum HeaderHttp{
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
		
		private final String value;//the string format of this HTTP header
		HeaderHttp(String value){
			this.value = value;
		}
		public String value() {
			return value;
		}
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
	 */
	public static String excute(String command, String url, String content, String... headerValuePairs) throws Exception{
		HttpURLConnection connection = null;
		String debugmsg = StringUtils.debugmsg(false);
		
		try {
			StringBuffer response = new StringBuffer();
			String encodedContent = content;
			//Create connection
			IndependantLog.debug(debugmsg+" original url is '"+url+"'");
			url = StringUtils.urlEncode(url);
			IndependantLog.debug(debugmsg+" encoded url is '"+url+"'");
			connection = (HttpURLConnection) (new URL(url)).openConnection();
			connection.setRequestMethod(command.toUpperCase());
			
			/** A URL connection can be used for input and/or output. Set the DoOutput flag to true if you intend to use the URL connection for output, false if not.
			 *  Only "post", "put" need to write to connection???*/
			boolean doOutPut = false;
			boolean _includeRequestHeaders = includeRequestHeaders;
			boolean _includeResponseHeaders = includeResponseHeaders;
			if(CommandHttp.POST.value().equalsIgnoreCase(command) ||
			   CommandHttp.PUT.value().equalsIgnoreCase(command)){
				doOutPut = true;
			}
			if(CommandHttp.HEAD.value().equalsIgnoreCase(command)){
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
				connection.setRequestProperty(HeaderHttp.CONTENT_TYPE.value(), _contentType);
				connection.setRequestProperty(HeaderHttp.CONTENT_LANGUAGE.value(), contentLauguage);
				connection.setRequestProperty(HeaderHttp.CONTENT_LENGTH.value(), Integer.toString(encodedContent.getBytes().length));
			}

			connection.setUseCaches(useCaches);
			
			//Set extra headers, user custom headers, may override the default header settings
			String header = null;
			String value = null;
			if(headerValuePairs!=null && headerValuePairs.length>0){
				for(int i=0;i<headerValuePairs.length;i++){
					header = headerValuePairs[i];
					if(i+1<headerValuePairs.length){
						value=headerValuePairs[++i];
						connection.setRequestProperty(header, value);
					}
				}
			}

			if(_includeRequestHeaders){
				Map<String,List<String>> headers = connection.getRequestProperties();
				Set<String> keys = headers.keySet(); 
				if(!keys.isEmpty()){
					response.append("====== HTTP REQUEST HEADERS =====\n");
					List<String> values = null;
					for(String key:headers.keySet()){
						values = headers.get(key);
						response.append(key+" : "+values+"\n");
					}
				}else{
					IndependantLog.debug(debugmsg+"HTTP request header is empty.");
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
					response.append("====== HTTP RESPONSE HEADERS =====\n");
					List<String> values = null;
					for(String key:headers.keySet()){
						values = headers.get(key);
						response.append(key+" : "+values+"\n");
					}
				}else{
					IndependantLog.debug(debugmsg+"HTTP response header is empty.");
				}
			}
			
			//Get Response from connection 
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line = rd.readLine();
			if(line!=null){
				response.append("======  HTTP RESPONSE =====\n");
				while(line != null) {
					response.append(line+"\n");
					line = rd.readLine();
				}
			}else{
				IndependantLog.debug(debugmsg+"HTTP response is empty.");
			}
			rd.close();
			return response.toString();
		} catch (Exception e) {
			IndependantLog.error(debugmsg+StringUtils.debugmsg(e));
			throw e;
		} finally {
			if(connection != null)  connection.disconnect(); 
		}
	}

	/**
	 * 'application/x-www-form-urlencoded', it is a content type, refer to <a href="http://www.w3.org/TR/html401/interact/forms.html#form-content-type">its definition</a>
	 */
	public static final String CONTENT_TYPE_APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
	/** 'charset=', it is used to describe the content charset in {@link HeaderHttp#CONTENT_TYPE} */
	public static final String CHARSET_EQUAL = "charset=";
	/** false */
	public static final boolean DEFAULT_INCLUDE_HEARERS = false;
	
	/** The value to set to the HTTP header {@link HeaderHttp#CONTENT_TYPE}, the default value is {@link #CONTENT_TYPE_APPLICATION_FORM_URLENCODED}. */
	public static String contentType = CONTENT_TYPE_APPLICATION_FORM_URLENCODED;
	/** The charset to set to the HTTP header {@link HeaderHttp#CONTENT_TYPE}, the default value is {@link StringUtils#CHARSET_UTF8} */
	public static String contentCharset = StringUtils.CHARSET_UTF8;
	
	/** The value to set to the HTTP header {@link HeaderHttp#CONTENT_LANGUAGE}, the default value is {@link Locale#US} */
	public static String contentLauguage = Locale.US.toString();
	/** A boolean indicating whether or not to allow caching. The default is false.*/
	public static boolean useCaches = false;
	/** A boolean indicating whether or not to include request headers information in the response. The default is false.*/
	public static boolean includeRequestHeaders = DEFAULT_INCLUDE_HEARERS;
	/** A boolean indicating whether or not to include response headers information in the response. The default is false.*/
	public static boolean includeResponseHeaders = DEFAULT_INCLUDE_HEARERS;
		
	public static String getUsage(){
		StringBuilder sb = new StringBuilder();
		String clazzname = StringUtils.getClassName(1, true);
		
		sb.append("Usage: \n");
		sb.append("java " +clazzname + " COMMAND URL"+"\n");
		sb.append("java " +clazzname + " COMMAND URL ENCODED_CONTENT"+"\n");
		sb.append("java " +clazzname + " COMMAND URL ENCODED_CONTENT HTTP_HEADER1 VALUE1 HTTP_HEADER2 VALUE2 ..."+"\n");
		sb.append("\n");
		sb.append("COMMAND can be one of: "+ Arrays.toString(CommandHttp.values())+" \n");
		
		return sb.toString();
	}

	/**
	 * Send a simple HTTP request.<br>
	 * If a PROXY server is needed to visit web site, we need to specify the <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html">PROXY server</a> as JVM parameter, such as:<br>
	 * -Dhttp.proxyHost=proxy.host.name<br>
	 * -Dhttp.proxyPort=portNumber<br>
	 * @param args String[], the parameters for HTTP Request<br>
	 * <pre>
	 *        args[0] String, the HTTP request command, it can be one of {@link CommandHttp}.
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
		
		String result = null;
		try {
			System.out.println("HTTP Request: "+command+" "+url+" "+content+" "+ StringUtils.arrayToString(headerValuePairs));

			result = excute(command, url, content, headerValuePairs);
			System.out.println("Result: ");
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
