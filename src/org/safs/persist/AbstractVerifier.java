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
 * OCT 24, 2017    (Lei Wang) Moved most functionalities out of class AbstractRuntimeDataVerifier.
 */
package org.safs.persist;

import org.safs.IndependantLog;
import org.safs.SAFSException;

/**
 * @author Lei Wang
 */
public abstract class AbstractVerifier extends PersistableChecker implements Verifier{

	@Override
	public void verify(Persistable persistable, boolean... conditions) throws SAFSException {
		validate(persistable);
		IndependantLog.debug("Verifying\n"+persistable);
	}
}
