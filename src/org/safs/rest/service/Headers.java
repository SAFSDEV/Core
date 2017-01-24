/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */
package org.safs.rest.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.config.H1Config;
import org.apache.hc.core5.http.impl.BasicHttpTransportMetrics;
import org.apache.hc.core5.http.impl.io.AbstractMessageParser;
import org.apache.hc.core5.http.impl.io.SessionInputBufferImpl;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicLineParser;
import org.apache.hc.core5.http.message.HeaderGroup;
import org.apache.hc.core5.http.message.LineParser;
import org.apache.hc.core5.util.CharArrayBuffer;
import org.safs.SAFSException;
import org.safs.SAFSRuntimeException;
import org.safs.tools.RuntimeDataInterface;


/**
 * Provides for storing header Collections for predefined AND custom request types.
 *
 * @author canagl
 */
public class Headers {


	private static Map<String, HeaderGroup> _headerMap = new HashMap<String, HeaderGroup>();

	// All map keys will be used as uppercase
	public static final String BINARY_TYPE = "BINARY";
	public static final String JSON_TYPE   = "JSON";
	public static final String TEXT_TYPE   = "TEXT";
	public static final String XML_TYPE    = "XML";
	public static final String HTML_TYPE   = "HTML";
	public static final String IMAGE_TYPE  = "IMAGE";
	public static final String CSS_TYPE    = "CSS";
	public static final String SCRIPT_TYPE = "SCRIPT";

	// Constants for common Header types
	public static final String CONTENT_TYPE      = "Content-Type";
	public static final String ACCEPT            = "Accept";
	public static final String USER_AGENT        = "User-Agent";

	public static final String APPL_OCTET_STREAM = "application/octet-stream";
	public static final String APPL_JSON         = "application/json";
	public static final String TEXT_PLAIN        = "text/plain";
	public static final String TEXT_XML          = "text/xml";
	public static final String TEXT_HTML         = "text/html";
	public static final String IMAGE             = "image";
	public static final String TEXT_CSS          = "text/css";
	public static final String APPL_JAVASCRIPT   = "application/javascript";

	public static final String MOZILLA_GENERIC_AGENT = "Mozilla/5.0 Gecko/20110201";

	private static Map<String,String> defaultContentTypeMap;


	static {
		resetHeaders();
	}


	private static void checkType(String type) {
		if (type == null) {
			throw new SAFSRuntimeException("type cannot be null!");
		}
	}

	/**
	 * Returns the headers currently set for a defined resource/request type
	 * as a multi-line String.
	 * <p>
	 * There are predefined request resource/request headers that can be retrieved and even changed.
	 * The user is also able to retrieve custom headers previously stored using a custom type.
	 * <p>
	 * Ex:<ul>
	 *
	 * </ul>
	 * @param type
	 * @return
	 * @see #setHeadersForType(String,Collection)
	 * @see #addHeaderForType(String, String)
	 * @see #removeHeaderForType(String, String)
	 */
	public static String getHeadersForType(String type){

		HeaderGroup hg = getHeaderGroupForType(type);
		return convertHeaderGroupToMultiLineString(hg);
	}

	/**
	 * Returns the header group currently set for a defined resource/request
	 * type. If not set, then lazily sets defaults.
	 * @param type
	 * @return
	 */
	private static HeaderGroup getHeaderGroupForType(String type) {

		checkType(type);
		HeaderGroup hg = _headerMap.get(type.toUpperCase());
		if (hg == null) {
			String defContentType = defaultContentTypeMap.get(type.toUpperCase());
			if (defContentType == null) {
				// This would be the case for a "custom" type for which we do
				// not have default values...
				return new HeaderGroup();
			}
			else {
				Header[] defaultHeaders = {
					new BasicHeader(CONTENT_TYPE, defContentType),
					new BasicHeader(ACCEPT, defContentType)
				};
				setHeadersForType(type, defaultHeaders);
				hg = _headerMap.get(type.toUpperCase());
			}
		}
		return hg;
	}

	/**
	 * Wholesale set/replace the headers multi-line String to be used for a
	 * predefined or custom resource/request type.
	 * If the provided String is null then the headers mapped to the type and
	 * the type entry will be removed.
	 * @param type
	 * @param headerStr Multi-line String containing the HTTP header(s)
	 * @throws IOException
	 * @throws HttpException
	 * @see #getHeadersForType(String)
	 * @see #addHeaderForType(String, String)
	 * @see #removeHeaderForType(String, String)
	 */
	public static void setHeadersForType(String type, String headerStr) {

		checkType(type);
		if (headerStr == null) {
			_headerMap.remove(type.toUpperCase());
		} else {
			Header[] headerArr = parseHeadersInMultiLineString(headerStr);
			setHeadersForType(type, headerArr);
		}
	}


	private static void setHeadersForType(String type, Header[] headers) {

		checkType(type);
		HeaderGroup headerGrp = new HeaderGroup();
		headerGrp.setHeaders(headers);
		_headerMap.put(type.toUpperCase(), headerGrp);
	}


	/**
	 * Add one (or more) headers to the existing set of headers for the mapped type.
	 * @param type
	 * @param headerStr Multi-line String containing the HTTP headers
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @see #setHeadersForType(String,String)
	 * @see #getHeadersForType(String)
	 * @see #removeHeaderForType(String, String)
	 */
	public static String addHeadersForType(String type, String headerStr) {

		checkType(type);

		// Convert the new header(s) to be added from String to Header
		Header[] newHeaders = parseHeadersInMultiLineString(headerStr);

		// Get the preset or default values for this type
		HeaderGroup headers = getHeaderGroupForType(type);

		// NOTE: HTTP spec does allow duplicate Headers under certain conditions
		for (Header headerToBeAdded : newHeaders) {
			headers.addHeader(headerToBeAdded);
		}
		return convertHeaderGroupToMultiLineString(headers);
	}

	/**
	 * Remove one (or more) header(s) from the existing headers for the mapped type.
	 * @param type
	 * @param headerStr Multi-line String containing the HTTP header(s)
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @see #setHeadersForType(String,Collection)
	 * @see #getHeadersForType(String)
	 * @see #addHeaderForType(String, String)
	 */
	public static String removeHeadersForType(String type, String headerStr) {

		checkType(type);

		// Convert the new header to be removed from String to Header
		Header[] unwantedHeaders = parseHeadersInMultiLineString(headerStr);
		// Get the preset or default values for this type
		HeaderGroup headers = getHeaderGroupForType(type);

		for (Header headerToBeRemoved : unwantedHeaders) {
			headers.removeHeader(headerToBeRemoved);
		}
		return convertHeaderGroupToMultiLineString(headers);
	}

	/**
	 * Will lazily create the default headers for this type if they don't already exist.
	 * @return the _binaryHeaders currently in use for all requests for Binary resources.
	 * @see #setBinaryHeaders(Collection)
	 */
	public static String getBinaryHeaders() {

		return getHeadersForType(BINARY_TYPE);
	}

	/**
	 * @param _binaryHeaders the _binaryHeaders to set for all future requests for Binary resources.
	 * @see #getBinaryHeaders()
	 */
	public static void setBinaryHeaders(String _binaryHeaders) {
		setHeadersForType(BINARY_TYPE, _binaryHeaders);
	}

	/**
	 * @return the _jsonHeaders currently in use for all requests for JSON resources.
	 * @see #setJSONHeaders(Collection)
	 */
	public static String getJSONHeaders() {

		return getHeadersForType(JSON_TYPE);
	}

	/**
	 * @param _jsonHeaders the _jsonHeaders to set for all future requests for JSON resources.
	 * @see #getJSONHeaders()
	 */
	public static void setJSONHeaders(String _jsonHeaders) {
		setHeadersForType(JSON_TYPE, _jsonHeaders);
	}

	/**
	 * @return the _textHeaders currently in use for all requests for Text resources.
	 * @see #setTextHeaders(Collection)
	 */
	public static String getTextHeaders() {

		return getHeadersForType(TEXT_TYPE);
	}

	/**
	 * @param _textHeaders the _textHeaders to set for all future requests for Text resources.
	 * @see #getTextHeaders()
	 */
	public static void setTextHeaders(String _textHeaders) {
		setHeadersForType(TEXT_TYPE, _textHeaders);
	}

	/**
	 * @return the _xmlHeaders currently used for all requests for XML resources.
	 * @see #setXMLHeaders(Collection)
	 */
	public static String getXMLHeaders() {

		return getHeadersForType(XML_TYPE);
	}

	/**
	 * @param _xmlHeaders the _xmlHeaders to set for all future requests for XML resources.
	 * @see #getXMLHeaders()
	 */
	public static void setXMLHeaders(String _xmlHeaders) {
		setHeadersForType(XML_TYPE, _xmlHeaders);
	}

	/**
	 * @return the _htmlHeaders currently used for all requests for Html resources.
	 * @see #setHtmlHeaders(Collection)
	 */
	public static String getHtmlHeaders() {

		return getHeadersForType(HTML_TYPE);
	}

	/**
	 * @param _htmlHeaders the _htmlHeaders to set for all future requests for Html resources.
	 * @see #getHtmlHeaders()
	 */
	public static void setHtmlHeaders(String _htmlHeaders) {
		setHeadersForType(HTML_TYPE, _htmlHeaders);
	}

	/**
	 * @return the _imageHeaders currently used for all requests for Image resources.
	 * @see #setImageHeaders(Collection)
	 */
	public static String getImageHeaders() {

		return getHeadersForType(IMAGE_TYPE);
	}

	/**
	 * @param _imageHeaders the _imageHeaders to set for all future requests for Image resources.
	 * @see #getImageHeaders()
	 */
	public static void setImageHeaders(String _imageHeaders) {
		setHeadersForType(IMAGE_TYPE, _imageHeaders);
	}

	/**
	 * @return the _cssHeaders currently used for all requests for CSS resources.
	 * @see #setCSSHeaders(Collection)
	 */
	public static String getCSSHeaders() {

		return getHeadersForType(CSS_TYPE);
	}

	/**
	 * @param _cssHeaders the _cssHeaders to set for all future requests for CSS resources.
	 * @see #getCSSHeaders()
	 */
	public static void setCSSHeaders(String _cssHeaders) {
		setHeadersForType(CSS_TYPE, _cssHeaders);
	}

	/**
	 * @return the _scriptHeaders currently used for all requests for Script resources.
	 * @see #setScriptHeaders(Collection)
	 */
	public static String getScriptHeaders() {

		return getHeadersForType(SCRIPT_TYPE);
	}

	/**
	 * @param _scriptHeaders the _scriptHeaders to set for all future requests for Script resources.
	 * @see #getScriptHeaders()
	 */
	public static void setScriptHeaders(String _scriptHeaders) {
		setHeadersForType(SCRIPT_TYPE, _scriptHeaders);
	}


	private static String convertHeaderGroupToMultiLineString(HeaderGroup headerGrp) {

		if (headerGrp == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(80);
		for (Header header : headerGrp.getAllHeaders()) {
			sb.append(header.getName() + ":" + header.getValue() + "\n");
		}
		return sb.toString();
	}


	public static String convertHeadersMapToMultiLineString(Map<String,String> headerMap) {

		if (headerMap == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(80);
		for (Map.Entry<String, String> header : headerMap.entrySet()) {
			sb.append(header.getKey() + ":" + header.getValue() + "\n");
		}
		return sb.toString();
	}

	public static Map<String,String> convertHeadersMultiLineStringToMap(String headers){
		return getHeadersMapFromMultiLineString(headers);
	}

//	/**
//	 * @param _headerString The string of multi-line headers
//	 * @return Collection of separated header strings
//	 * @throws IOException
//	 * @throws HttpException
//	 */
//	public static Collection<String> parseHeaderListFromMultiLineString(String _headerString) {
//
//		Header[] headers = parseHeadersInMultiLineString(_headerString);
//
//		// Now, return the headers as Collection<String>.
//		Collection<String> ret = new ArrayList<String>();
//		for (Header header : headers) {
//			ret.add(header.getName() + ":" + header.getValue());
//		}
//		return ret;
//	}

	private static Header[] parseHeadersInMultiLineString(String _headerString) {

		/*
		 * This is pretty close to how Apache HTTP Core5 parses headers.
		 */
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(_headerString.getBytes("UTF-8"));

			int buffersize = org.apache.hc.core5.http.config.ConnectionConfig.DEFAULT.getBufferSize();
			BasicHttpTransportMetrics inTransportMetrics = new BasicHttpTransportMetrics();
			final H1Config h1Config = H1Config.DEFAULT;
			final CharsetDecoder chardecoder = null;
			SessionInputBufferImpl inbuffer = new SessionInputBufferImpl(inTransportMetrics, buffersize, -1,
			                                                             h1Config.getMaxLineLength(), chardecoder);
			final LineParser lineParser = BasicLineParser.INSTANCE;

			List<CharArrayBuffer> headerLines = new ArrayList<>();
			final Header[] headers = AbstractMessageParser.parseHeaders(
				inbuffer,
				inputStream,
				h1Config.getMaxHeaderCount(),
				h1Config.getMaxLineLength(),
				lineParser,
				headerLines
			);
			return headers;
		}
		catch (Exception ex) {
			throw new SAFSRuntimeException(ex);
		}
	}

	/**
	 * Utility method to convert headers multi-line string to a Map
	 * @param headers Multi-line String containing complete header info
	 * @return Map of String
	 */
	 static Map<String,String> getHeadersMapFromMultiLineString(String headers) {

		Header[] headersList = parseHeadersInMultiLineString(headers);

		// Now, return the headers as Collection<String>.
		Map<String, String> ret = new HashMap<String,String>();
		for (Header header : headersList) {
			ret.put(header.getName(), header.getValue());
		}
		return ret;
	}
	/**
	 * Given a multi-line header String, append a header value to an existing header name,
	 * or create the new header with the given value.
	 * <p>
	 * Example: appendHeaderValue(headers, "Accept", "text/html");
	 * <p>
	 * This does NOT act on prestored headers, only the header String provided.
	 * <p>
	 * When appending, this routine will prefix the value with a separator.
	 * <p>
	 * @param headers multi-line header string.
	 * @param headerName name of the header to be created or appended.
	 * @param headerValue value to add to the provided header name.
	 * @return multiline headers properly appended, or unmodified if invalid arguments are provided.
	 */
	public static String appendHeaderValue(String headers, String headerName, String headerValue){
		if (headerName==null ||
		    headerName.length()==0 ||
		    headerValue == null ||
		    headerValue.length()==0) return headers;
		Map<String,String> hm = getHeadersMapFromMultiLineString(headers);
		boolean found = false;
		String val;
		for(String key:hm.keySet()){
			if(headerName.equalsIgnoreCase(key)){
				val = hm.get(key);
				val +=", "+ headerValue;
				hm.put(key, val);
				found = true;
				break; // or do we want to handle duplicates?
			}
		}
		if(!found) hm.put(headerName, headerValue);
		return convertHeadersMapToMultiLineString(hm);
	}

	/**
	 * Given a multi-line header String, remove a header value from an existing header name,
	 * or create the new header without the given value.
	 * <p>
	 * Example: removeHeaderValue(headers, "Accept", "text/html");
	 * <p>
	 * This does NOT act on prestored headers, only the header String provided.
	 * <p>
	 * When removing, this routine will attempt to fix any separators.
	 * <p>
	 * @param headers multi-line header string.
	 * @param headerName name of the header to have a value removed.
	 * @param headerValue value to remove.
	 * @return multiline headers properly changed, or unmodified if invalid arguments are provided.
	 */
	public static String removeHeaderValue(String headers, String headerName, String headerValue){
		if (headerName==null ||
		    headerName.length()==0 ||
		    headerValue == null ||
		    headerValue.length()==0) return headers;
		Map<String,String> hm = getHeadersMapFromMultiLineString(headers);
		boolean found = false;
		String val;
		String ucval;
		String ucheaderValue;
		for(String key:hm.keySet()){
			if(headerName.equalsIgnoreCase(key)){
				val = hm.get(key);
				// if only value
				if(headerValue.equalsIgnoreCase(val)){
					hm.remove(key);
				}else{

				}
				val +=", "+ headerValue;
				hm.put(key, val);
				found = true;
				break; // or do we want to handle duplicates?
			}
		}
		if(!found) hm.put(headerName, headerValue);
		return convertHeadersMapToMultiLineString(hm);
	}

	 public static void loadHeaders(RuntimeDataInterface runtime, String headersFile, String method, String type) throws SAFSException{
		 //TODO Load header files
	 }

	 public synchronized static void resetHeaders(){
		 if(defaultContentTypeMap==null){
			 defaultContentTypeMap = new HashMap<String, String>(8);
		 }
		 defaultContentTypeMap.put(BINARY_TYPE, APPL_OCTET_STREAM);
		 defaultContentTypeMap.put(JSON_TYPE,   APPL_JSON);
		 defaultContentTypeMap.put(TEXT_TYPE,   TEXT_PLAIN);
		 defaultContentTypeMap.put(XML_TYPE,    TEXT_XML);
		 defaultContentTypeMap.put(HTML_TYPE,   TEXT_HTML);
		 defaultContentTypeMap.put(IMAGE_TYPE,  IMAGE);
		 defaultContentTypeMap.put(CSS_TYPE,    TEXT_CSS);
		 defaultContentTypeMap.put(SCRIPT_TYPE, APPL_JAVASCRIPT);
	 }
}
