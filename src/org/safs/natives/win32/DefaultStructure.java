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
 * APR 29, 2016    (Lei Wang) Initial release.
 */
package org.safs.natives.win32;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Structure;

/**
 * In the latest version of JNA, the abstract class Structure requires that<br>
 * its sub-class must provide implementation of method getFieldOrder().<br>
 * For convenience, we provide a default implementation of getFieldOrder(), if<br>
 * sub-class doesn't care about the field's order, it can extend this one.<br>
 */
public class DefaultStructure extends Structure{

	/**
	 * We return the class's fields. The fields may be returned in order not predictable.
	 * The subclasses may override it if they really care about the order.
	 */
	protected List<String> getFieldOrder() {
		Field[] fields = getClass().getDeclaredFields();
		
		List<String> result = new ArrayList<String>();

		if(fields!=null){
			for(int i=0;i<fields.length;i++){
				result.add(fields[i].getName());
			}
		}
		
		return result;
	}
}
