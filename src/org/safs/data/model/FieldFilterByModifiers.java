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
import java.lang.reflect.Modifier;

/**
 * @author Lei Wang
 *
 */
public class FieldFilterByModifiers extends FilterAbstract<Field>{

	/**
	 * The field modifiers to be ignored. It can be bit-or combination of {@link Modifier#fieldModifiers()}.<br>
	 * By default it is 0x00000000, which means nothing will be ignored.<br>
	 * @example
	 * {@link Modifier#PRIVATE} | {@link Modifier#STATIC}, then the static and private fields will be ignored.
	 *
	 */
	private int modifiers = 0x00000000;

	/**
	 * @param modifiers
	 */
	public FieldFilterByModifiers(int modifiers) {
		this.modifiers = modifiers;
	}

	@Override
	public boolean shouldBeIgnored(Field field) {
		try{
			//If one bit of 'modifiers' matches with the field's modifier, then the field is ignored.
			if((field.getModifiers() & modifiers)!=0){
				return true;
			}
		}catch(Exception e){

		}
		return false;
	}

}
