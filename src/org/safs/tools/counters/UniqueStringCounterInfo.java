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
package org.safs.tools.counters;

import org.safs.tools.UniqueStringID;

public class UniqueStringCounterInfo
	extends UniqueStringID
	implements UniqueCounterInterface {

	protected String testlevel = null;
	
	/**
	 * Constructor for UniqueStringCounterInfo
	 */
	public UniqueStringCounterInfo(String id, String testlevel) {
		super(id);
		this.testlevel = testlevel;
	}

	/**
	 * @see UniqueCounterInterface#getTestLevel()
	 */
	public String getTestLevel() {
		return testlevel;
	}
}

