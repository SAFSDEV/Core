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
 * @date 2017-10-19    (Lei Wang) Initial release.
 */
package org.safs.persist;

import org.safs.SAFSException;
import org.safs.SAFSNullPointerException;
import org.safs.SAFSPersistableNotEnableException;

/**
 * @author Lei Wang
 *
 */
public class PersistableChecker {
	/**
	 * Check if this Persistable object is valid (not null, not disabled etc.).
	 * @param persistable Persistable, the object to persist
	 * @throws SAFSException if the persistable is null or is not enabled
	 */
	protected void validate(Persistable persistable) throws SAFSException {
		if(persistable==null){
			throw new SAFSNullPointerException("The persistable object is null.");
		}
		if(!persistable.isEnabled()){
			throw new SAFSPersistableNotEnableException("The class '"+persistable.getClass().getSimpleName()+"' is not enabled.");
		}
	}
}
