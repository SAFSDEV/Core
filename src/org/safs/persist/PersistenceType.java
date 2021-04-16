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
 * DEC 08, 2016    (Lei Wang) Initial release.
 */
package org.safs.persist;

import java.util.Arrays;

import org.safs.IndependantLog;
import org.safs.StringUtils;

/**
 * This enum represents the possible persistence types.
 * @author Lei Wang
 */
public enum PersistenceType{
	FILE("FILE"),
	VARIABLE("VARIABLE"),
	STRING("STRING");

	public final String name;
	PersistenceType(String name){
		this.name = name;
	}

	public static PersistenceType get(String name){
		PersistenceType type = PersistenceType.VARIABLE;
		if(FILE.name.equalsIgnoreCase(name)) type = PersistenceType.FILE;
		else if(VARIABLE.name.equalsIgnoreCase(name)) type = PersistenceType.VARIABLE;
		else if(STRING.name.equalsIgnoreCase(name)) type = PersistenceType.STRING;
		else{
			IndependantLog.warn(StringUtils.debugmsg(false)+"The persistence type '"+name+"' is NOT valid!\n"
					+ "The possible valid type can be "+Arrays.toString(PersistenceType.values())+"\n"
					+ "The default type "+type+" is returned.");
		}
		return type;
	}
}
