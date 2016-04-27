/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */
package org.safs.rest.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides for storing header Collections for predefined AND custom request types.
 * 
 * @author canagl
 */
public class Headers {

	private static Map<String, Collection<String>> _headerMap = new HashMap<String, Collection<String>>();
	
	// All map keys will be used as uppercase
	public static final String BINARY_HEADERS = "BINARY";
	public static final String CSS_HEADERS    = "CSS";
	public static final String HTML_HEADERS   = "HTML";
	public static final String JSON_HEADERS   = "JSON";
	public static final String IMAGE_HEADERS  = "IMAGE";
	public static final String SCRIPT_HEADERS = "SCRIPT";
	public static final String TEXT_HEADERS   = "TEXT";
	public static final String XML_HEADERS    = "XML";
	
	/**
	 * Returns the Collection of headers currently set for a defined resource/request type.
	 * <p>
	 * There are predefined request resource/request headers that can be retrieved and even changed.  
	 * The user is also able to retrieve custom header Collections previously stored using a custom mapKey.
	 * <p>
	 * Ex:<ul>
	 * 
	 * </ul>
	 * @param mapKey
	 * @return
	 * @see #setHeadersForType(String,Collection)
	 * @see #addHeaderForType(String, String)
	 * @see #removeHeaderForType(String, String)
	 */
	public static Collection<String> getHeadersForType(String mapKey){
		try{ return _headerMap.get(mapKey.toUpperCase());}
		catch(NullPointerException ignore){}
		return null;
	}
	
	/**
	 * Wholesale set/replace the headers Collection to be used for a predefined or custom resource/request type.
	 * If the provided Collection is null then the headers mapped to the mapKey and the mapKey entry will be removed. 
	 * @param mapKey
	 * @param headers
	 * @see #getHeadersForType(String)
	 * @see #addHeaderForType(String, String)
	 * @see #removeHeaderForType(String, String)
	 */
	public static void setHeadersForType(String mapKey, Collection<String> headers){
		// toUpperCase() might generate a NullPointerException
		if(headers == null){
			_headerMap.remove(mapKey.toUpperCase());
		}else{
			// might generate a NullPointerException
			_headerMap.put(mapKey.toUpperCase(), headers);
		}
	}

	/**
	 * Add a single header String to the existing Collection of header strings for the mapped type.
	 * @param mapKey
	 * @param header
	 * @return
	 * @see #setHeadersForType(String,Collection)
	 * @see #getHeadersForType(String)
	 * @see #removeHeaderForType(String, String)
	 */
	public static Collection<String> addHeaderForType(String mapKey, String header){
		// toUpperCase() might generate a NullPointerException
		Collection<String> headers = _headerMap.get(mapKey.toUpperCase());
		if(headers instanceof Collection){
			if(! headers.contains(header)){
				headers.add(header);
			}
		}
		return headers;
	}

	/**
	 * Remove a single header String to the existing Collection of header strings for the mapped type.
	 * @param mapKey
	 * @param header
	 * @return
	 * @see #setHeadersForType(String,Collection)
	 * @see #getHeadersForType(String)
	 * @see #addHeaderForType(String, String)
	 */
	public static Collection<String> removeHeaderForType(String mapKey, String header){
		// toUpperCase() might generate a NullPointerException
		Collection<String> headers = _headerMap.get(mapKey.toUpperCase());
		if(headers instanceof Collection){
			if(headers.contains(header)){
				headers.remove(header);
			}
		}
		return headers;
	}
	
	/**
	 * Will lazily create the default headers for this type if they don't already exist.
	 * @return the _binaryHeaders currently in use for all requests for Binary resources.
	 * @see #setBinaryHeaders(Collection)
	 */
	public static Collection<String> getBinaryHeaders() {
		Collection<String> headers = getHeadersForType(BINARY_HEADERS);
		if(headers == null){
			headers = new ArrayList<String>();
			//
			// TODO Fill in the default headers to use for generic BINARY data
			//
			setBinaryHeaders(headers);
		}
		return headers;
	}

	/**
	 * @param _binaryHeaders the _binaryHeaders to set for all future requests for Binary resources.
	 * @see #getBinaryHeaders()
	 */
	public static void setBinaryHeaders(Collection<String> _binaryHeaders) {
		setHeadersForType(BINARY_HEADERS, _binaryHeaders);
	}

	/**
	 * @return the _jsonHeaders currently in use for all requests for JSON resources.
	 * @see #setJSONHeaders(Collection)
	 */
	public static Collection<String> getJSONHeaders() {
		Collection<String> headers = getHeadersForType(JSON_HEADERS);
		if(headers == null){
			headers = new ArrayList<String>();
			//
			// TODO Fill in the default headers to use for generic JSON data
			//
			setJSONHeaders(headers);
		}
		return headers;
	}

	/**
	 * @param _jsonHeaders the _jsonHeaders to set for all future requests for JSON resources.
	 * @see #getJSONHeaders()
	 */
	public static void setJSONHeaders(Collection<String> _jsonHeaders) {
		setHeadersForType(JSON_HEADERS, _jsonHeaders);
	}

	/**
	 * @return the _textHeaders currently in use for all requests for Text resources.
	 * @see #setTextHeaders(Collection)
	 */
	public static Collection<String> getTextHeaders() {
		Collection<String> headers = getHeadersForType(TEXT_HEADERS);
		if(headers == null){
			headers = new ArrayList<String>();
			//
			// TODO Fill in the default headers to use for plain TEXT data
			//
			setTextHeaders(headers);
		}
		return headers;
	}

	/**
	 * @param _textHeaders the _textHeaders to set for all future requests for Text resources.
	 * @see #getTextHeaders()
	 */
	public static void setTextHeaders(Collection<String> _textHeaders) {
		setHeadersForType(JSON_HEADERS, _textHeaders);
	}

	/**
	 * @return the _xmlHeaders currently used for all requests for XML resources.
	 * @see #setXMLHeaders(Collection)
	 */
	public static Collection<String> getXMLHeaders() {
		Collection<String> headers = getHeadersForType(XML_HEADERS);
		if(headers == null){
			headers = new ArrayList<String>();
			//
			// TODO Fill in the default headers to use for generic XML data
			//
			setXMLHeaders(headers);
		}
		return headers;
	}

	/**
	 * @param _xmlHeaders the _xmlHeaders to set for all future requests for XML resources.
	 * @see #getXMLHeaders()
	 */
	public static void setXMLHeaders(Collection<String> _xmlHeaders) {
		setHeadersForType(XML_HEADERS, _xmlHeaders);
	}

	/**
	 * @return the _htmlHeaders currently used for all requests for Html resources.
	 * @see #setHtmlHeaders(Collection)
	 */
	public static Collection<String> getHtmlHeaders() {
		Collection<String> headers = getHeadersForType(HTML_HEADERS);
		if(headers == null){
			headers = new ArrayList<String>();
			//
			// TODO Fill in the default headers to use for generic HTML data
			//
			setHtmlHeaders(headers);
		}
		return headers;
	}

	/**
	 * @param _htmlHeaders the _htmlHeaders to set for all future requests for Html resources.
	 * @see #getHtmlHeaders()
	 */
	public static void setHtmlHeaders(Collection<String> _htmlHeaders) {
		setHeadersForType(HTML_HEADERS, _htmlHeaders);
	}

	/**
	 * @return the _imageHeaders currently used for all requests for Image resources.
	 * @see #setImageHeaders(Collection)
	 */
	public static Collection<String> getImageHeaders() {
		Collection<String> headers = getHeadersForType(IMAGE_HEADERS);
		if(headers == null){
			headers = new ArrayList<String>();
			//
			// TODO Fill in the default headers to use for generic IMAGE data
			//
			setImageHeaders(headers);
		}
		return headers;
	}

	/**
	 * @param _imageHeaders the _imageHeaders to set for all future requests for Image resources.
	 * @see #getImageHeaders()
	 */
	public static void setImageHeaders(Collection<String> _imageHeaders) {
		setHeadersForType(IMAGE_HEADERS, _imageHeaders);
	}

	/**
	 * @return the _cssHeaders currently used for all requests for CSS resources.
	 * @see #setCSSHeaders(Collection)
	 */
	public static Collection<String> getCSSHeaders() {
		Collection<String> headers = getHeadersForType(CSS_HEADERS);
		if(headers == null){
			headers = new ArrayList<String>();
			//
			// TODO Fill in the default headers to use for generic IMAGE data
			//
			setCSSHeaders(headers);
		}
		return headers;
	}

	/**
	 * @param _cssHeaders the _cssHeaders to set for all future requests for CSS resources.
	 * @see #getCSSHeaders()
	 */
	public static void setCSSHeaders(Collection<String> _cssHeaders) {
		setHeadersForType(CSS_HEADERS, _cssHeaders);
	}

	/**
	 * @return the _scriptHeaders currently used for all requests for Script resources.
	 * @see #setScriptHeaders(Collection)
	 */
	public static Collection<String> getScriptHeaders() {
		Collection<String> headers = getHeadersForType(SCRIPT_HEADERS);
		if(headers == null){
			headers = new ArrayList<String>();
			//
			// TODO Fill in the default headers to use for generic Script data
			//
			setScriptHeaders(headers);
		}
		return headers;
	}

	/**
	 * @param _scriptHeaders the _scriptHeaders to set for all future requests for Script resources.
	 * @see #getScriptHeaders()
	 */
	public static void setScriptHeaders(Collection<String> _scriptHeaders) {
		setHeadersForType(SCRIPT_HEADERS, _scriptHeaders);
	}
	
}
