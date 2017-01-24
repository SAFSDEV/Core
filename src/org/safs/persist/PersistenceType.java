/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * DEC 08, 2016    (SBJLWA) Initial release.
 */
package org.safs.persist;

import java.util.Arrays;

import org.safs.IndependantLog;
import org.safs.StringUtils;

/**
 * This enum represents the possible persistence types.
 * @author sbjlwa
 */
public enum PersistenceType{
	FILE("FILE"),
	VARIABLE("VARIABLE");

	public final String name;
	PersistenceType(String name){
		this.name = name;
	}

	public static PersistenceType get(String name){
		PersistenceType type = PersistenceType.VARIABLE;
		if(FILE.name.equalsIgnoreCase(name)) type = PersistenceType.FILE;
		else if(VARIABLE.name.equalsIgnoreCase(name)) type = PersistenceType.VARIABLE;
		else{
			IndependantLog.warn(StringUtils.debugmsg(false)+"The persistence type '"+name+"' is NOT valid!\n"
					+ "The possible valid type can be "+Arrays.toString(PersistenceType.values())+"\n"
					+ "The default type "+type+" is returned.");
		}
		return type;
	}
}
