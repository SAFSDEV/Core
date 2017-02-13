/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2016年12月21日    (Lei Wang) Initial release.
 */
package org.safs.auth;

import java.util.HashMap;
import java.util.Map;

import org.safs.persist.PersistableDefault;

/**
 * @author Lei Wang
 *
 */
//TODO it is just an example for testing Persistor, need to fill with more useful fields.
public class Content extends PersistableDefault{
	private final static Map<String, String> fieldToPersistKeyMap = new HashMap<String, String>();

	/**
	 * If the access token request is valid and authorized, the authorization server issues an access token.<br>
	 * This token token will be used to access client's resources on behalf of client without authentication.<br>
	 * Refer to <a href="https://tools.ietf.org/html/rfc6749#section-5">Access Token</a>
	 */
	private String accessToken = null;
	/**
	 * Currently, it could be 'bearer' or 'mac'.<br>
	 * Refer to <a href="https://tools.ietf.org/html/rfc6749#section-7.1">Token Type</a>
	 */
	private String accessTokenType = null;

	/**
	 * The secret/password used along with accessToken. It is not specified in the
	 * <a href="https://tools.ietf.org/html/rfc6749">The OAuth 2.0 Authorization Framework</a>,
	 * but it seems that it is required by Twitter, LinkedIn etc.
	 */
	private String accessTokenSecret = null;

	//tokenProviderServerName:tokenProviderServerPort/tokenProviderServiceName/tokenProviderAuthTokenResource
	/**
	 * The name of token provider server. Such as http://oauth2.token.server
	 * TODO not used yet, do we need it?
	 */
	private String tokenProviderServerName = null;
	/**
	 * The port number of the token provider server.
	 * TODO not used yet, do we need it?
	 */
	private int tokenProviderServerPort = 80;
	/**
	 * The base service name.
	 */
	private String tokenProviderServiceName = null;
	/**
	 * The resource path to get auth token.
	 */
	private String tokenProviderAuthTokenResource = null;

	/** The Identifier of client used to register with authorization server.
	 *  In the real world, it can also be called as 'consumerID', 'clientKey' or 'trustedUserid' etc.
	 *  But they are the same meaning.
	 */
	private String clientID = null;
	/** The password of client used to register with authorization server.
	 *  In the real world, it can also be called as 'consumerSecret', 'clientSecret' or 'trustedUserPassword' etc.
	 *  But they are the same meaning.
	 */
	private String clientSecret = null;

	static{
		fieldToPersistKeyMap.put("accessToken", "accessToken");
		fieldToPersistKeyMap.put("accessTokenType", "accessTokenType");
		fieldToPersistKeyMap.put("accessTokenSecret", "accessTokenSecret");
		fieldToPersistKeyMap.put("clientID", "clientID");
		fieldToPersistKeyMap.put("clientSecret", "clientSecret");
		fieldToPersistKeyMap.put("accessTokenSecret", "accessTokenSecret");
		fieldToPersistKeyMap.put("tokenProviderServerName", "tokenProviderServerName");
		fieldToPersistKeyMap.put("tokenProviderServerPort", "tokenProviderServerPort");
		fieldToPersistKeyMap.put("tokenProviderServiceName", "tokenProviderServiceName");
		fieldToPersistKeyMap.put("tokenProviderAuthTokenResource", "tokenProviderAuthTokenResource");
	}

	@Override
	public Map<String, String> getPersitableFields() {
		return fieldToPersistKeyMap;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}

	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}

	public String getAccessTokenType() {
		return accessTokenType;
	}

	public void setAccessTokenType(String accessTokenType) {
		this.accessTokenType = accessTokenType;
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getTokenProviderServiceName() {
		return tokenProviderServiceName;
	}

	public void setTokenProviderServiceName(String tokenProviderServiceName) {
		this.tokenProviderServiceName = tokenProviderServiceName;
	}

	public String getTokenProviderAuthTokenResource() {
		return tokenProviderAuthTokenResource;
	}

	public void setTokenProviderAuthTokenResource(
			String tokenProviderAuthTokenResource) {
		this.tokenProviderAuthTokenResource = tokenProviderAuthTokenResource;
	}

}
