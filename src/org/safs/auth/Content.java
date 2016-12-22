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
	protected final static Map<String, String> fieldToPersistKeyMap = new HashMap<String, String>();

	protected String customData = null;
	private boolean expected = false;

	int count = 0;
	float fly = 0.0f;
	char hi = 'a';

	public String getCustomData() {
		return customData;
	}

	public void setCustomData(String customData) {
		this.customData = customData;
	}

	static{
		fieldToPersistKeyMap.put("customData", "customData");
		fieldToPersistKeyMap.put("expected", "Expected");
		fieldToPersistKeyMap.put("count", "count");
		fieldToPersistKeyMap.put("fly", "fly");
		fieldToPersistKeyMap.put("hi", "hi");
	}

	@Override
	public Map<String, String> getPersitableFields() {
		return fieldToPersistKeyMap;
	}

	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public float getFly() {
		return fly;
	}
	public void setFly(float fly) {
		this.fly = fly;
	}
	public char getHi() {
		return hi;
	}
	public void setHi(char hi) {
		this.hi = hi;
	}
	public boolean isExpected() {
		return expected;
	}
	public void setExpected(boolean expected) {
		this.expected = expected;
	}
}
