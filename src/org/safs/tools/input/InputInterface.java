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

import org.safs.tools.UniqueIDInterface;

public interface InputInterface {

	/** 
	 * Open a unique instance of an input source.
	 * The source "pointer" should be ready to read the first record if successful.
	 * @return TRUE if OPEN was successful.
	 */
	public boolean open (UniqueSourceInterface source);

	/** 
	 * Get the next record from the identified input source.
	 * Should return a valid InputRecordInterface, or an InputRecordInvalid object.
	 * @see InputRecordInterface
	 * @see InputRecordInvalid
	 */
	public InputRecordInterface nextRecord (UniqueIDInterface source);
	
	/** 
	 * Rewind the identified input source to its first record.
	 * The source "pointer" should be ready to read the first record on exit.
	 * @return TRUE if successful.
	 */
	public boolean gotoStart (UniqueIDInterface source);
	
	/** 
	 * Locate and return the requested record in the input source.
	 * The source "pointer" should be ready to read the record AFTER the identified 
	 * record on exit.
	 * Should return a valid InputRecordInterface, or an InputRecordInvalid object.
	 * @see InputRecordInterface
	 * @see InputRecordInvalid
	 */
	public InputRecordInterface gotoRecord (UniqueRecordInterface recordInfo);
	
	/** Close the identified input source and release resources.**/
	public void close (UniqueIDInterface source);
}

