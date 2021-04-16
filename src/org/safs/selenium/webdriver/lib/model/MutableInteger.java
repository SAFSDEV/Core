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
package org.safs.selenium.webdriver.lib.model;

/**
 *
 * History:<br>
 *
 *  <br>   Jun 6, 2014    (Lei Wang) Initial release.
 */
public class MutableInteger {

	private int value=0;

	public MutableInteger(int value){
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	/**
	 * increment the int value, return the int value before increment
	 * @return the int value before increment
	 */
	public int incrementBefore(){
		return value++;
	}
	/**
	 * increment the int value, return the int value after increment
	 * @return the int value after increment
	 */
	public int incrementAfter(){
		return ++value;
	}
	/**
	 * decrement the int value, return the int value before decrement
	 * @return the int value before decrement
	 */
	public int decrementBefore(){
		return value--;
	}
	/**
	 * decrement the int value, return the int value after decrement
	 * @return the int value after decrement
	 */
	public int decrementAfter(){
		return --value;
	}

}
