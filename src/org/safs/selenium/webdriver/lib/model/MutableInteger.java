/**
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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
