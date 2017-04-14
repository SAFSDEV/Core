/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2017年2月14日    (SBJLWA) Initial release.
 * MAR 10, 2017  (SBJLWA) Modified comments as we replaced field 'protocol', 'host' and 'port' by one field 'rootUrl'.
 * APR 14, 2017  (SBJLWA) Removed the static map field and method getPersitableFields().
 *                        Made the default constructor ignoring "static&final" fields when persisting ("pickle" and "unpickle").
 */
package org.safs.auth;

import java.lang.reflect.Modifier;

import org.safs.persist.PersistableDefault;

/**
 * This class encapsulates the information about the authorization server, where client/application/consumer
 * can get the 'authorization code', 'access token', 'refresh token' etc.<br/>
 *
 * @author sbjlwa
 */
public class AuthorizationServer extends PersistableDefault{
	/**
	 * The rootUrl of the authorization server. It is in format <b>protocol://host:port</b>,
	 * such as "http://oauth2.authorization.server:80".
	 */
	//rootUrl/baseServiceName/authCodeResource
	//https://github.com/login/oauth/authorize

	//rootUrl/baseServiceName/authTokenResource
	//https://github.com/login/oauth/token
	private String rootUrl = null;
	/**
	 * The base name of authorization service. Such as "login".
	 */
	private String baseServiceName = null;
	/**
	 * The resource path to get authorization code. Such as "oauth/authorize".
	 */
	private String authCodeResource = DEFAULT_AUTH_CODE_RESOURCE;
	/**
	 * The resource path to get access token. Such as "oauth/token".
	 */
	private String authTokenResource = DEFAULT_AUTH_TOKEN_RESOURCE;

	/** 'oauth/token' */
	public static final String DEFAULT_AUTH_TOKEN_RESOURCE 	= "oauth/token";
	/** 'oauth/authorize' */
	public static final String DEFAULT_AUTH_CODE_RESOURCE 	= "oauth/authorize";

	/**
	 * This default constructor will create an AuthorizationServer ignoring "static&final" fields
	 * when persisting ("pickle" and "unpickle").
	 */
	public AuthorizationServer(){
		super(Modifier.STATIC|Modifier.FINAL);
	}

	public String getRootUrl() {
		return rootUrl;
	}
	public void setRootUrl(String rootUrl) {
		this.rootUrl = rootUrl;
	}

	public String getBaseServiceName() {
		return baseServiceName;
	}

	public void setBaseServiceName(String baseServiceName) {
		this.baseServiceName = baseServiceName;
	}

	public String getAuthCodeResource() {
		return authCodeResource;
	}

	public void setAuthCodeResource(String authCodeResource) {
		this.authCodeResource = authCodeResource;
	}

	public String getAuthTokenResource() {
		return authTokenResource;
	}

	public void setAuthTokenResource(String authTokenResource) {
		this.authTokenResource = authTokenResource;
	}
}
