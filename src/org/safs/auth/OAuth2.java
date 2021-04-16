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
