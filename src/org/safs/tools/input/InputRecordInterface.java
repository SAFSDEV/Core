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
 * This is the required interface for any InputInterface function that returns an 
 * input record to the calling Driver.  In general, null or values less than 1 
 * indicate unsuccessful attempts to retrieve the desired record, or that the source 
 * has reached the end of input (EOF).  
 * <p>
 * For this reason, the InputRecordInvalid concrete implementation is provided and 
 * an instance of this should be returned to indicate invalid data.
 * @see InputRecordInvalid
 */
public interface InputRecordInterface {

	/** 
	 * Return the row# or line# of the encapsulated data.
	 * Values less than 1 indicate no such record or EOF.
	 */
	public long getRecordNumber();

	/** 
	 * Return the actual string data of the input record.
	 * A null value indicates no such record or EOF.
	 */
	public String getRecordData();
	
	/**
	 * Returns TRUE if the values for recordnum and recorddata are valid.
	 */
	public boolean isValid();
}

