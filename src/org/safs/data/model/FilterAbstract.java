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
 * @date 2018-03-30    (Lei Wang) Initial release.
 */
package org.safs.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lei Wang
 *
 */
public abstract class FilterAbstract<T> implements Filter<T>{

	/**
	 * Pass each element to method {@link #shouldBeIgnored(Object)}, if it returns true then the element should be filtered.
	 */
	@Override
	public List<T> filter(List<T> elements) {
		List<T> filteredElements = new ArrayList<T>();

		for(T T:elements){
			if(!shouldBeIgnored(T)){
				filteredElements.add(T);
			}
		}

		return filteredElements;
	}

	/**
	 * @param element T, the element to test.
	 * @return boolean, true if the element should be ignored.
	 */
	public abstract boolean shouldBeIgnored(T element);
}
