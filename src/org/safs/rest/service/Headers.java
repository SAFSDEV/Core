/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */
package org.safs.rest.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.config.MessageConstraints;
import org.apache.hc.core5.http.impl.io.HttpTransportMetricsImpl;
import org.apache.hc.core5.http.impl.io.SessionInputBufferImpl;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicLineParser;
import org.apache.hc.core5.util.CharArrayBuffer;
import org.safs.SAFSRuntimeException;
import org.apache.hc.core5.http.message.HeaderGroup;


/**
 * Provides for storing header Collections for predefined AND custom request types.
 * 
 * @author Carl Nagle
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
	
	// Constants for default Content/Accept types
	public static final String CONTENT_TYPE      = "Content-Type";
	public static final String ACCEPT            = "Accept";
	
	public static final String APPL_OCTET_STREAM = "application/octet-stream";
	public static final String APPL_JSON         = "application/json";
	public static final String TEXT_PLAIN        = "text/plain";
	public static final String TEXT_XML          = "text/xml";
	public static final String TEXT_HTML         = "text/html";
	public static final String IMAGE             = "image";
	public static final String TEXT_CSS          = "text/css";
	public static final String APPL_JAVASCRIPT   = "application/javascript";
	
	private static Map<String,String> defaultContentTypeMap; 
	
	
	static {
		defaultContentTypeMap = new HashMap<String, String>(8);
		defaultContentTypeMap.put(BINARY_TYPE, APPL_OCTET_STREAM);
		defaultContentTypeMap.put(JSON_TYPE,   APPL_JSON);
		defaultContentTypeMap.put(TEXT_TYPE,   TEXT_PLAIN);
		defaultContentTypeMap.put(XML_TYPE,    TEXT_XML);
		defaultContentTypeMap.put(HTML_TYPE,   TEXT_HTML);
		defaultContentTypeMap.put(IMAGE_TYPE,  IMAGE);
		defaultContentTypeMap.put(CSS_TYPE,    TEXT_CSS);
		defaultContentTypeMap.put(SCRIPT_TYPE, APPL_JAVASCRIPT);
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
		
		// TODO: allow duplicates if the spec allows it.  Otherwise, replace the value.
		for (Header headerToBeAdded : newHeaders) {
			if (! headers.containsHeader(headerToBeAdded.getName())) {
				headers.addHeader(headerToBeAdded);
			}
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
	
	
	static String convertHeadersMapToMultiLineString(Map<String,String> headerMap) {
		
		if (headerMap == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(80);
		for (Map.Entry<String, String> header : headerMap.entrySet()) {
			sb.append(header.getKey() + ":" + header.getValue() + "\n");
		}
		return sb.toString();
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
		 * This is pretty close to how Apache HTTP Client5 parses headers.
		 */
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(_headerString.getBytes("UTF-8"));
			
			int buffersize = org.apache.hc.core5.http.config.ConnectionConfig.DEFAULT.getBufferSize();
			HttpTransportMetricsImpl inTransportMetrics = new HttpTransportMetricsImpl();
			MessageConstraints messageConstraints = MessageConstraints.DEFAULT;
			SessionInputBufferImpl inbuffer = new SessionInputBufferImpl(inTransportMetrics, buffersize, -1, messageConstraints, null);
			BasicLineParser parser = org.apache.hc.core5.http.message.BasicLineParser.INSTANCE;
			
			inbuffer.bind(inputStream);
			List<CharArrayBuffer> headerLines = new ArrayList<CharArrayBuffer>();
			Header[] headers = org.apache.hc.core5.http.impl.io.AbstractMessageParser.parseHeaders(
				inbuffer,
				messageConstraints.getMaxHeaderCount(),
				messageConstraints.getMaxLineLength(),
				parser,
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
	

}
