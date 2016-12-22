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

import java.util.List;
import java.util.Map;

import org.safs.SAFSException;

/**
 * @author sbjlwa
 */
public interface Persistor {
	/**
	 * Persist an object.
	 * @param persistable Persistable, the object to persist
	 * @throws SAFSException when persistence fails or something wrong happens.
	 */
	public void persist(Persistable persistable) throws SAFSException;

	/**
	 * Delete the persistence.
	 * @throws SAFSException when failing to delete the persistence or something wrong happens.
	 */
	public void unpersist() throws SAFSException;

	/**
	 * Convert the contents stored in a persistence substance into a Persistable object.
	 * @param ignoredFields Map&lt;String, List&lt;String>>, a Map containing the fields of each class to be ignored when un-pickling.
	 * @return Persistable, the Persistable object got from the persistence material.
	 * @throws SAFSException
	 */
	public Persistable unpickle(Map<String/*className*/, List<String>/*field-names*/> ignoredFields) throws SAFSException;

	/**
	 * The persistence Type.
	 * @return PersistenceType
	 */
	public PersistenceType getType();

	/**
	 * The name of the persistence material holding the content of an Object.<br>
	 * It can be a file name or a variable name etc.<br>
	 * It could be useful when deleting the persistence.<br>
	 *
	 * @return String, The name of the persistence material, such as the file name or the variable name etc.
	 * @see #unpersist()
	 */
	public String getPersistenceName();

}
