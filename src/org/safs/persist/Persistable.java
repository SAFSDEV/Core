/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 02, 2016    (Lei Wang) Initial release.
 */
package org.safs.persist;

import java.util.Map;

/**
 * Represent an object that can be persisted.
 * @author Lei Wang
 */
public interface Persistable {

	/**
	 * @return Map<String, Object>, a Map containing a pair(persistKey, value) to persist. 
	 */
	public Map<String, Object> getContents();
	
	/**
	 * @return Map<String, String>, a Map containing a pair(fieldName, persistKey) to persist. 
	 */
	public Map<String, String> getPersitableFields();
	
	/**
	 * Tell us if this object will be persisted or not.
	 * @return boolean, if true, this object will be persisted; otherwise, will not. 
	 */
	public boolean isEnabled();
	
	/**
	 * Sometimes, we don't want to persist an object, we disable it.
	 * @param enabled boolean, if true, this object will be persisted; otherwise, will not.
	 */
	public void setEnabled(boolean enabled);
}
