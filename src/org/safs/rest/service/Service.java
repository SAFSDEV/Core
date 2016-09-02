/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */
package org.safs.rest.service;

import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.safs.SAFSRuntimeException;

/**
 * Contains the information needed to test a specific web service.
 * <p>
 * A test or ongoing interactive session with a specific web service will have a unique service Id 
 * and all information associated with that interactive service session will be retained here.
 *  
 * @author canagl
 */
public class Service{
	
	private String serviceId;
	private String proxyServerURL;
	private String baseURL;
	private String userId;
	private String password;
	private String authType;
	private ProtocolVersion protocolVersion = HttpVersion.HTTP_1_1;
	
	
	/**
	 * Constructor
	 * @param serviceId
	 */
	public Service(String serviceId){
		this.serviceId = serviceId;
	}
	
	/**
	 * Constructor
	 * @param serviceId
	 * @param baseURL
	 */
	public Service(String serviceId, String baseURL){
		this(serviceId);
		setBaseURL(baseURL);
	}

	/**
	 * Constructor
	 * @param serviceId
	 * @param baseURL
	 * @param protocolVersion
	 */
	public Service(String serviceId, String baseURL, String protocolVersion) {
		this(serviceId, baseURL);
		setProtocolVersion(protocolVersion);
	}

	/**
	 * @return the serviceId
	 */
	public String getServiceId() {
		return serviceId;
	}

	/**
	 * @param serviceId the serviceId to set
	 */
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	/**
	 * @return the proxyServerURL
	 */
	public String getProxyServerURL() {
		return proxyServerURL;
	}

	/**
	 * @param baseURL the baseURL to set
	 */
	public void setProxyServerURL(String proxyServerURL) {
		this.proxyServerURL = proxyServerURL;
	}

	/**
	 * @return the baseURL
	 */
	public String getBaseURL() {
		return baseURL;
	}

	/**
	 * @param baseURL the baseURL to set
	 */
	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the authType
	 */
	public String getAuthType() {
		return authType;
	}

	/**
	 * @param authType the authType to set
	 */
	public void setAuthType(String authType) {
		this.authType = authType;
	}

	/**
	 * @return the protocol version
	 */
	public String getProtocolVersion() {
		return protocolVersion.toString();
	}
	
	/**
	 * @param protocolVersion the protocol version to set
	 */
	public void setProtocolVersion(String protocolVersion) {
		if (protocolVersion == null) {
			throw new SAFSRuntimeException("protocolVersion should not be null");
		}
		switch (protocolVersion) {
		case "HTTP/1.0":
			this.protocolVersion = HttpVersion.HTTP_1_0;
			break;
		case "HTTP/1.1":
			this.protocolVersion = HttpVersion.HTTP_1_1;
			break;
		case "HTTP/2.0":
			this.protocolVersion = HttpVersion.HTTP_2_0;
			break;
		default:
			throw new SAFSRuntimeException("protocolVersion must be \"HTTP/1.0\", " +
			                               "\"HTTP/1.1\", or \"HTTP/2.0\"");
		}
	}
	
	// for internal use only
	ProtocolVersion getProtocolVersionObject() {
		return protocolVersion;
	}
}
