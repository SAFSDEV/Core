/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * 2016年12月21日    (SBJLWA) Initial release.
 */
package org.safs.auth;

import java.util.HashMap;
import java.util.Map;

import org.safs.persist.PersistableDefault;

/**
 * @author sbjlwa
 *
 */
//TODO it is just an example for testing Persistor, need to fill with more useful fields.
public class Content extends PersistableDefault{
	private final static Map<String, String> fieldToPersistKeyMap = new HashMap<String, String>();

	private String consumerKey = null;
	private String consumerSecret = null;
	private String accessToken = null;
	private String accessTokenSecret = null;
	private String oauth2ServiceName = null;
	private String trustedUserid = null;
	private String trustedUserPassword = null;

	static{
		fieldToPersistKeyMap.put("consumerKey", "consumerKey");
		fieldToPersistKeyMap.put("consumerSecret", "consumerSecret");
		fieldToPersistKeyMap.put("accessToken", "accessToken");
		fieldToPersistKeyMap.put("accessTokenSecret", "accessTokenSecret");
		fieldToPersistKeyMap.put("oauth2ServiceName", "oauth2ServiceName");
		fieldToPersistKeyMap.put("trustedUserid", "trustedUserid");
		fieldToPersistKeyMap.put("trustedUserPassword", "trustedUserPassword");
	}

	@Override
	public Map<String, String> getPersitableFields() {
		return fieldToPersistKeyMap;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
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

	public String getOauth2ServiceName() {
		return oauth2ServiceName;
	}

	public void setOauth2ServiceName(String oauth2ServiceName) {
		this.oauth2ServiceName = oauth2ServiceName;
	}

	public String getTrustedUserid() {
		return trustedUserid;
	}

	public void setTrustedUserid(String trustedUserid) {
		this.trustedUserid = trustedUserid;
	}

	public String getTrustedUserPassword() {
		return trustedUserPassword;
	}

	public void setTrustedUserPassword(String trustedUserPassword) {
		this.trustedUserPassword = trustedUserPassword;
	}

}
