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
public class SimpleAuth extends PersistableDefault implements Auth{
	protected final static Map<String, String> fieldToPersistKeyMap = new HashMap<String, String>();

	private String userName = null;
	private String password = null;


	static{
		fieldToPersistKeyMap.put("userName", "userName");
		fieldToPersistKeyMap.put("password", "password");
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

	@Override
	public Map<String, String> getPersitableFields() {
		return fieldToPersistKeyMap;
	}
}
