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
	 *                               such as if the match is exact, if the match is case-sensitive,
	 *                               if all the fields should be matched etc.
	 * @throws SAFSException when verification fails or something wrong happens.
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
