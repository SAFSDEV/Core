/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 02, 2016    (SBJLWA) Initial release.
 */
package org.safs.persist;

import java.util.Map;

/**
 * Represent an object that can be persisted.
 * @author sbjlwa
 */
public interface Persistable {

	/**
	 * @return Map<String, Object>, a Map containing a pair(persistKey, value) to persist. 
	 */
	public Map<String, Object> getContents();
	
	/**
	 * This Map contains a pair, the key is the 'class field name' telling us which field needs
	 * to be persisted; the value is a 'unique string' representing this class field in the
	 * persistence. The 'class field name' and the 'persist key' can be same or different.<br>
	 * When persisting a class, not all the fields need to be persisted; Only those fields
	 * which need to be persisted will be put into this Map.<br>
	 * 
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
