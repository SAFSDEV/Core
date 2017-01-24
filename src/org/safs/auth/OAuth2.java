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

	protected Content content = null;
	protected SimpleAuth simpleAuth = null;

	static{
		fieldToPersistKeyMap.put("content", "content");
		fieldToPersistKeyMap.put("simpleAuth", "simpleAuth");
	}

	public SimpleAuth getSimpleAuth() {
		return simpleAuth;
	}
	public void setSimpleAuth(SimpleAuth simpleAuth) {
		this.simpleAuth = simpleAuth;
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
