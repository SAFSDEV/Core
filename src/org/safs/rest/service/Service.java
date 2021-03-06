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
package org.safs.rest.service;
/**
 * History:
 *  OCT 13, 2017 (Lei Wang) Modified getClientAdapter(): Set Auth object to Adapter.
 */
import org.apache.hc.client5.http.testframework.HttpClient5Adapter;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.safs.SAFSRuntimeException;
import org.safs.auth.Auth;
import org.safs.auth.OAuth2;
import org.safs.auth.SSOAuth;
import org.safs.rest.service.models.consumers.SafsrestAdapter;

/**
 * Contains the information needed to test a specific web service.
 * <p>
 * A test or ongoing interactive session with a specific web service will have a unique service Id
 * and all information associated with that interactive service session will be retained here.
 *
 * @author Carl Nagle
 */
public class Service{

	private String serviceId;
	private String proxyServerURL;
	private String baseURL;
	private String userId;
	private String password;
	private String authType;
	private ProtocolVersion protocolVersion = HttpVersion.HTTP_1_1;
	private String clientAdapterClassName;
	private Object clientAdapter;
	private Auth auth = null;

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
		if ((userId == null) && (auth != null)) {
			// If userId not set yet, try to get from the OAuth2 information.
			if (OAuth2.class.isAssignableFrom(auth.getClass())) {
				userId = ((OAuth2) auth).getSimpleAuth().getUserName();
			}else if (SSOAuth.class.isAssignableFrom(auth.getClass())) {
				userId = ((SSOAuth) auth).getUserName();
			}
		}
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
		if ((password == null) && (auth != null)) {
			// If password not set yet, try to get from the OAuth2 information.
			if (OAuth2.class.isAssignableFrom(auth.getClass())) {
				password = ((OAuth2) auth).getSimpleAuth().getPassword();
			}else if (SSOAuth.class.isAssignableFrom(auth.getClass())) {
				password = ((SSOAuth) auth).getPassword();
			}
		}
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

	public Auth getAuth() {
		return auth;
	}

	public void setAuth(Auth auth) {
		this.auth = auth;
		if (auth!=null && OAuth2.class.isAssignableFrom(auth.getClass())) {
			clientAdapterClassName = "org.safs.rest.service.DelegateSafsrestAdapter";
			//ClientAdapter will be obtained in method getClientAdapter().
		}
	}

	/**
	 * To specify the use of a different client adapter, call this
	 * with the class name of the adapter.
	 *
	 * @param clientAdapterClassName the class name of the client adapter
	 */
	public Object setClientAdapterClassName(String clientAdapterClassName) throws Exception {
		this.clientAdapterClassName = clientAdapterClassName;
		return getClientAdapter();
	}

	// for internal use only
	String getClientAdapterClassName() {
		return clientAdapterClassName;
	}

	// for internal use only
	Object getClientAdapter() throws Exception {
		if (clientAdapter == null) {
			if (clientAdapterClassName == null) {
				// use HttpClient5 by default
				clientAdapter = new HttpClient5Adapter();
				HttpClient5Adapter adapter = (HttpClient5Adapter) clientAdapter;
				adapter.setProxyServerURL(getProxyServerURL());
				adapter.setAuth(auth);
			} else {
				Class<?> clazz = Class.forName(clientAdapterClassName);
				clientAdapter = clazz.newInstance();
				if(clientAdapter instanceof DelegateSafsrestAdapter && auth!=null){
					if(auth instanceof OAuth2){
						OAuth2 auth2 = (OAuth2) auth;
						SafsrestAdapter adapter = ((DelegateSafsrestAdapter) clientAdapter).getAdapter();
						String rootUrl = auth2.getAuthorizationServer().getRootUrl();
						if (rootUrl != null) {
							adapter.setTokenProviderRootUrl(rootUrl);
						}
						adapter.setTokenProviderServiceName(auth2.getAuthorizationServer().getBaseServiceName());
						adapter.setTokenProviderAuthTokenResource(auth2.getAuthorizationServer().getAuthTokenResource());
						adapter.setTrustedUserid(auth2.getContent().getClientID());
						adapter.setTrustedPassword(auth2.getContent().getClientSecret());
					}
				}
			}
		}

		return clientAdapter;
	}
}
