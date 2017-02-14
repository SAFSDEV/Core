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

import org.safs.Printable;
import org.safs.persist.PersistableDefault;

/**
 * @author sbjlwa
 *
 */
public class OAuth2 extends PersistableDefault implements Auth{
	private final static Map<String, String> fieldToPersistKeyMap = new HashMap<String, String>();

	protected AuthorizationServer authorizationServer = null;
	protected Content content = null;
	protected SimpleAuth simpleAuth = null;

	static{
		fieldToPersistKeyMap.put("content", "content");
		fieldToPersistKeyMap.put("authorizationServer", "authorizationServer");
		fieldToPersistKeyMap.put("simpleAuth", "simpleAuth");
	}

	public SimpleAuth getSimpleAuth() {
		return simpleAuth;
	}
	public void setSimpleAuth(SimpleAuth simpleAuth) {
		adjustTabulation(simpleAuth);
		this.simpleAuth = simpleAuth;
	}

	public Content getContent() {
		return content;
	}
	public void setContent(Content content) {
		adjustTabulation(content);
		this.content = content;
	}

	public AuthorizationServer getAuthorizationServer() {
		return authorizationServer;
	}
	public void setAuthorizationServer(AuthorizationServer authorizationServer) {
		adjustTabulation(authorizationServer);
		this.authorizationServer = authorizationServer;
	}

	protected void adjustTabulation(Printable printable){
		printable.setTabulation(getTabulation()+1);
	}

	@Override
	public Map<String, String> getPersitableFields() {
		return fieldToPersistKeyMap;
	}
}
