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
package org.safs.tools.logs;

import org.safs.tools.UniqueStringID;

public class UniqueStringLogLevelInfo
	extends UniqueStringID
	implements UniqueLogLevelInterface {

	protected String loglevel = null;
	
	/**
	 * Constructor for UniqueStringLogLevelInfo
	 */
	public UniqueStringLogLevelInfo() {
		super();
	}

	/**
	 * Constructor for UniqueStringLogLevelInfo UniqueStringID
	 */
	public UniqueStringLogLevelInfo(String id) {
		super(id);
	}

	/**
	 * Constructor for UniqueStringLogLevelInfo
	 */
	public UniqueStringLogLevelInfo(String id, String loglevel) {
		this(id);
		setLogLevel(loglevel);
	}

	/**
	 * Set the log level to that provided.
	 */
	public void setLogLevel(String loglevel) {
		this.loglevel = loglevel;
	}

	/**
	 * @see UniqueLogLevelInterface#getLogLevel()
	 */
	public String getLogLevel() {
		return loglevel;
	}

}

