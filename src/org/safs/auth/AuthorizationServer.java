/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2017年2月14日    (SBJLWA) Initial release.
 */
package org.safs.auth;

import java.util.HashMap;
import java.util.Map;

import org.safs.persist.PersistableDefault;

/**
 * This class encapsulates the information about the authorization server, where client/application/consumer
 * can get the 'authorization code', 'access token', 'refresh token' etc.<br/>
 *
 * @author sbjlwa
 *
 */
public class AuthorizationServer extends PersistableDefault{
	private final static Map<String, String> fieldToPersistKeyMap = new HashMap<String, String>();
	//protocol://host:port/baseServiceName/authCodeResource
	//https://github.com/login/oauth/authorize

	//protocol://host:port/baseServiceName/authTokenResource
	//https://github.com/login/oauth/token

	/**
	 * The rootUrl of the authorization server. Such as "http://oauth2.authorization.server:80".
	 */
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

	static{
		fieldToPersistKeyMap.put("rootUrl", "rootUrl");
		fieldToPersistKeyMap.put("baseServiceName", "baseServiceName");
		fieldToPersistKeyMap.put("authCodeResource", "authCodeResource");
		fieldToPersistKeyMap.put("authTokenResource", "authTokenResource");
	}

	@Override
	public Map<String, String> getPersitableFields() {
		return fieldToPersistKeyMap;
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
