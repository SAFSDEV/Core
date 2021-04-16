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
/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2016年12月21日    (Lei Wang) Initial release.
 */
package org.safs.auth;

import org.safs.persist.PersistableDefault;

/**
 * This class contains the information of OAUTH2:<br/>
 * {@link #clientID} and {@link #clientSecret} are used to make request to the authorization server.<br/>
 * {@link #accessToken}, {@link #accessTokenType}, {@link #accessTokenSecret} and {@link #refreshToken}
 * are returned as response from the authorization server.<br/>
 *
 * @author Lei Wang
 *
 */
public class Content extends PersistableDefault{
	/**
	 * If the access token request is valid and authorized, the authorization server issues an access token.<br>
	 * This token will be used to access client's resources on behalf of client without authentication.<br>
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

	/**
	 * It is used to obtain new access tokens. Please refer to <a href="https://tools.ietf.org/html/rfc6749#section-5.1">refresh_token</a>.<br/>
	 * How to refresh to obtain new access tokens, please refer to <a href="https://tools.ietf.org/html/rfc6749#section-6">Refreshing an Access Token</a>.<br/>
	 */
	private String refreshToken = null;

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

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
