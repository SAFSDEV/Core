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

import org.safs.Printable;
import org.safs.persist.PersistableDefault;

/**
 * @author Lei Wang
 *
 */
public class OAuth2 extends PersistableDefault implements Auth{
	protected final static Map<String, String> fieldToPersistKeyMap = new HashMap<String, String>();

	private String userName = null;
	private String password = null;

	protected Content content = null;

	static{
		fieldToPersistKeyMap.put("userName", "userName");
		fieldToPersistKeyMap.put("password", "password");
		fieldToPersistKeyMap.put("content", "content");
	}

	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public Content getContent() {
		return content;
	}
	public void setContent(Content content) {
		if(content instanceof Printable){
			((Printable) content).setTabulation(getTabulation()+1);
		}
		this.content = content;
	}

	@Override
	public Map<String, String> getPersitableFields() {
		return fieldToPersistKeyMap;
	}
}
