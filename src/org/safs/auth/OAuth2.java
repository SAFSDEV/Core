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

import org.safs.Printable;
import org.safs.persist.PersistableDefault;

/**
 * @author Lei Wang
 *
 */
public class OAuth2 extends PersistableDefault implements Auth{
	protected AuthorizationServer authorizationServer = null;
	protected Content content = null;
	protected SimpleAuth simpleAuth = null;

	public SimpleAuth getSimpleAuth() {
		return simpleAuth;
	}
	public void setSimpleAuth(SimpleAuth simpleAuth) {
		adjustTabulation(simpleAuth);
		simpleAuth.setParent(this);
		this.simpleAuth = simpleAuth;
	}

	public Content getContent() {
		return content;
	}
	public void setContent(Content content) {
		adjustTabulation(content);
		content.setParent(this);
		this.content = content;
	}

	public AuthorizationServer getAuthorizationServer() {
		return authorizationServer;
	}
	public void setAuthorizationServer(AuthorizationServer authorizationServer) {
		adjustTabulation(authorizationServer);
		authorizationServer.setParent(this);
		this.authorizationServer = authorizationServer;
	}

	protected void adjustTabulation(Printable printable){
		printable.setTabulation(getTabulation()+1);
	}
}
