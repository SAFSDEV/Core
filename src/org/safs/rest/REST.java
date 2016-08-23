/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */
package org.safs.rest;

import java.util.Collection;

import org.safs.SAFSRuntimeException;
import org.safs.rest.service.Headers;
import org.safs.rest.service.RESTImpl;
import org.safs.rest.service.Response;
import org.safs.rest.service.Service;
import org.safs.rest.service.Services;

/**
 * @author Carl Nagle
 */
public class REST {

	public static final String GET_METHOD    = "GET";
	public static final String POST_METHOD   = "POST";
	public static final String DELETE_METHOD = "DELETE";
	public static final String PUT_METHOD    = "PUT";
	public static final String HEAD_METHOD   = "HEAD";
	public static final String PATCH_METHOD  = "PATCH";
	
	public static RESTImpl restImpl = new RESTImpl();
	
	/**
	 * Start a named Service session with a specific web service.<br>
	 * This doesn't actually make any type of Connection or call to the web service.
	 * @param serviceId - unique name of the new Service session.
	 * @param baseURL -- root URL used to interact with the service.
	 * @return Service
	 * @throws IllegalArgumentException if the serviceId is null or already exists.
	 * @see #EndServiceSession(String)
	 * @see Services#getService(String)
	 */
	public static Service StartServiceSession(String serviceId, String baseURL) throws IllegalArgumentException{
		Service service = new Service(serviceId, baseURL);
		Services.addService(service);
		return service;
	}
	
	/**
	 * End an existing named Service session with a specific web service.<br>
	 * This doesn't actually make any type of Connection or call to the web service.
	 * @param serviceId - unique name of the Service session.
	 * @throws IllegalArgumentException if the serviceId is null or does NOT exists.
	 * @see #StartServiceSession(String, String)
	 */
	public static void EndServiceSession(String serviceId) throws IllegalArgumentException{
		Services.deleteService(serviceId);
	}

	public static Response request(String serviceId, String requestMethod, String relativeURI, String headers, Object body){
		
		Response ret = null;
		
		try {
			ret = restImpl.request(serviceId, requestMethod, relativeURI, headers, body);
		}
		catch(SAFSRuntimeException safs_ex) {
			throw safs_ex;
		}
		catch(Exception ex) {
			throw new SAFSRuntimeException(ex);
		}
		return ret;
	}
	
	public static class GET{
		public static Response custom(String serviceId, String relativeURI, String headers, Object body){
			return request(serviceId, GET_METHOD, relativeURI, headers, body);
		}
				
		public static Response text(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getTextHeaders(), body);
		}
		
		public static Response html(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getHtmlHeaders(), body);
		}
	
		public static Response xml(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getXMLHeaders(), body);
		}
	
		public static Response json(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getJSONHeaders(), body);
		}
	
		public static Response image(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getImageHeaders(), body);
		}
	
		public static Response binary(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getBinaryHeaders(), body);
		}
		public static Response css(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getCSSHeaders(), body);
		}
		public static Response script(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getScriptHeaders(), body);
		}
	}
	public static class HEAD{
		public static Response custom(String serviceId, String relativeURI, String headers, Object body){
			return request(serviceId, HEAD_METHOD, relativeURI, headers, body);
		}
		
		public static Response text(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getTextHeaders(), body);
		}
		
		public static Response html(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getHtmlHeaders(), body);
		}
	
		public static Response xml(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getXMLHeaders(), body);
		}
	
		public static Response json(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getJSONHeaders(), body);
		}
	
		public static Response image(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getImageHeaders(), body);
		}
	
		public static Response binary(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getBinaryHeaders(), body);
		}
		public static Response css(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getCSSHeaders(), body);
		}
		public static Response script(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getScriptHeaders(), body);
		}
	}
	public static class POST{
		public static Response custom(String serviceId, String relativeURI, String headers, Object body){
			return request(serviceId, POST_METHOD, relativeURI, headers, body);
		}
		
		public static Response text(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getTextHeaders(), body);
		}
		
		public static Response html(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getHtmlHeaders(), body);
		}
	
		public static Response xml(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getXMLHeaders(), body);
		}
	
		public static Response json(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getJSONHeaders(), body);
		}
	
		public static Response image(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getImageHeaders(), body);
		}
	
		public static Response binary(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getBinaryHeaders(), body);
		}
		public static Response css(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getCSSHeaders(), body);
		}
		public static Response script(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getScriptHeaders(), body);
		}
	}
	public static class PUT{
		public static Response custom(String serviceId, String relativeURI, String headers, Object body){
			return request(serviceId, PUT_METHOD, relativeURI, headers, body);
		}
		
		public static Response text(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getTextHeaders(), body);
		}
		
		public static Response html(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getHtmlHeaders(), body);
		}
	
		public static Response xml(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getXMLHeaders(), body);
		}
	
		public static Response json(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getJSONHeaders(), body);
		}
	
		public static Response image(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getImageHeaders(), body);
		}
	
		public static Response binary(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getBinaryHeaders(), body);
		}
		public static Response css(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getCSSHeaders(), body);
		}
		public static Response script(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getScriptHeaders(), body);
		}
	}
	public static class PATCH{
		public static Response custom(String serviceId, String relativeURI, String headers, Object body){
			return request(serviceId, PATCH_METHOD, relativeURI, headers, body);
		}
		
		public static Response text(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getTextHeaders(), body);
		}
		
		public static Response html(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getHtmlHeaders(), body);
		}
	
		public static Response xml(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getXMLHeaders(), body);
		}
	
		public static Response json(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getJSONHeaders(), body);
		}
	
		public static Response image(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getImageHeaders(), body);
		}
	
		public static Response binary(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getBinaryHeaders(), body);
		}
		public static Response css(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getCSSHeaders(), body);
		}
		public static Response script(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getScriptHeaders(), body);
		}
	}
	public static class DELETE{
		public static Response custom(String serviceId, String relativeURI, String headers, Object body){
			return request(serviceId, DELETE_METHOD, relativeURI, headers, body);
		}
		
		public static Response text(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getTextHeaders(), body);
		}
		
		public static Response html(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getHtmlHeaders(), body);
		}
	
		public static Response xml(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getXMLHeaders(), body);
		}
	
		public static Response json(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getJSONHeaders(), body);
		}
	
		public static Response image(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getImageHeaders(), body);
		}
	
		public static Response binary(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getBinaryHeaders(), body);
		}
		public static Response css(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getCSSHeaders(), body);
		}
		public static Response script(String serviceId, String relativeURI, Object body){
			return custom(serviceId, relativeURI, Headers.getScriptHeaders(), body);
		}
	}
}
