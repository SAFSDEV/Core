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
 * OCT 24, 2017    (Lei Wang) Moved most functionalities out of class AbstractRuntimeDataPersistor.
 */
package org.safs.persist;

import java.util.List;
import java.util.Map;

import org.safs.IndependantLog;
import org.safs.SAFSException;

/**
 * @author Lei Wang
 */
public abstract class AbstractPersistor extends PersistableChecker implements Persistor{

	@Override
	public void persist(Persistable persistable) throws SAFSException {
		validate(persistable);
		IndependantLog.debug("Persisting\n"+persistable);
	}

	/**
	 * Provided default implementation, simply throw out a SAFSException.
	 */
	@Override
	public void unpersist() throws SAFSException{
		throw new SAFSException("Method 'unpersist()' not supported yet!");
	}

	/**
	 * Provided default implementation, simply throw out a SAFSException.
	 */
	@Override
	public Persistable unpickle(Map<String/*className*/, List<String>/*field-names*/> ignoredFields) throws SAFSException{
		throw new SAFSException("Method 'unpickle()' not supported yet!");
	}
}
