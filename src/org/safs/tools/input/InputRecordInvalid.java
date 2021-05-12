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
package org.safs.tools.input;


/**
 * The mere instance of this object conveys "invalid data" for any function accepting an
 * InputRecordInterface object.  This can convey an EOF condition, or other invalid
 * data scenarios.  These are calling-function specific.
 * @see InputRecordInfo
 */
public class InputRecordInvalid implements InputRecordInterface {


	/**
	 * @see InputRecordInterface#getRecordNumber()
	 */
	public long getRecordNumber() {
		return -1;
	}

	/**
	 * @see InputRecordInterface#getRecordData()
	 */
	public String getRecordData() {
		return null;
	}

	/**
	 * @see InputRecordInterface#isValid()
	 */
	public boolean isValid(){
		return false;
	}
}
