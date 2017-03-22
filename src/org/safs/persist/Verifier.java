/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 05, 2016    (SBJLWA) Initial release.
 */
package org.safs.persist;

import org.safs.SAFSException;


/**
 * @author sbjlwa
 */
public interface Verifier {
	/**
	 * Verify an object against the contents stored in a persistence substance.
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
