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
 * @author  Carl Nagle
 * @since   NOV 04, 2003
 *   <br>   NOV 04, 2003    (Carl Nagle) Original Release
 *
 * @author Carl Nagle, NOV 05, 2003
 *         Now extends RuntimeException instead of SAFSException
 **/
package org.safs;

/** 
 * Flags the ommission of support for Regular Expressions needed by StringUtils 
 * and other classes.
 * 
 * Regular Expression support is part of core Java in V1.4 and later.  For pre-V1.4 
 * installations we can use the Jakarta regular expression support provided by 
 * the RegEx package downloaded from the Jakarta website: 
 * {@link http://jakarta.apache.org/regexp/index.html}
 * 
 * Missing Regular Expression support is considered a catastrophic engine failure.
 **/
public class SAFSRegExNotFoundException extends RuntimeException {
	
	public SAFSRegExNotFoundException (String message){ super(message);}
}

