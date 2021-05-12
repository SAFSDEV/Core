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
 * DEC 05, 2016    (Lei Wang) Initial release.
 */
package org.safs.persist;

import org.safs.SAFSException;


/**
 * Provides the ability to verify a Persistable object against the contents stored in a persistence substance.
 *
 * @author Lei Wang
 */
public interface Verifier {
	/**
	 * Verify a Persistable object against the contents stored in a persistence substance.
	 * @param persistable Persistable, the object to verify
	 * @param conditions boolean..., the boolean array to control the verification.
	 *                               such as
	 *                               <ul>
	 *                               <li>If the all the fields of persistable object need to match with those in the persistent benchmark.<br/>
	 *                                   If this is true, all the fields of actual object need to be verified.<br/>
	 *                                   Otherwise, only the fields specified in the persistent benchmark need to be verified.<br/>
	 *                               <li>if the match is partial, which means if the bench-text is provided as sub-string
	 *                               <li>if the match is case-sensitive
	 *                               </ul>
	 *                               etc.
	 * @throws SAFSVerificationException when verification fails.
	 * @throws SAFSException when something wrong happens.
	 */
	public void verify(Persistable persistable, boolean... conditions) throws SAFSException;

	/**
	 * The persistence Type.
	 * @return PersistenceType
	 */
	public PersistenceType getType();

	public static final boolean BOOL_MATCH_ALL_FIELDS 		= true;
	public static final boolean BOOL_VALUE_CONTAINS 		= false;
	public static final boolean BOOL_VALUE_CASESENSITIVE 	= true;

}
