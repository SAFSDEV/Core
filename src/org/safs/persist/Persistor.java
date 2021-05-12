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
 * DEC 02, 2016    (Lei Wang) Initial release.
 */
package org.safs.persist;

import java.util.List;
import java.util.Map;

import org.safs.SAFSException;

/**
 * Provides the abilities to
 * <ul>
 * <li>Persist a Persistable object to the persistence (a file, the variables etc.)
 * <li>Delete the persistence (a file, the variables etc.)
 * <li>Convert the contents stored in a persistence substance into a Persistable object.
 * </ul>
 *
 * @author Lei Wang
 */
public interface Persistor {
	/**
	 * Persist a Persistable object.
	 * @param persistable Persistable, the object to persist
	 * @throws SAFSException when persistence fails or something wrong happens.
	 */
	public void persist(Persistable persistable) throws SAFSException;

	/**
	 * Delete the persistence (a file, the variables etc.)
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
