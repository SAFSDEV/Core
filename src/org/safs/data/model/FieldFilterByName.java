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
 * @date 2018-03-29    (Lei Wang) Initial release.
 */
package org.safs.data.model;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author Lei Wang
 *
 */
public class FieldFilterByName extends FilterAbstract<Field>{

	/**
	 * A list of names for the filed that should be ignored.
	 */
	private List<String> names = null;

	/**
	 * @param names
	 */
	public FieldFilterByName(List<String> names) {
		super();
		this.names = names;
	}

	@Override
	public boolean shouldBeIgnored(Field element) {
		try{
			for(String name:names){
				if(name.equals(element.getName())){
					return true;
				}
			}
		}catch(Exception e){

		}
		return false;
	}

}
