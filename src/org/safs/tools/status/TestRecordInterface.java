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
package org.safs.tools.status;

public interface TestRecordInterface {

	/** CYCLE, SUITE, or STEP. **/
	public String getTestLevel ();
	
	/** The unique ID of the input source for this test level. **/
	public String getInputUID  ();

	/** 
	 * The name of the input source. 
	 * This might be a filename or some other type of String.
	 * Note, the same named file or item can be "opened" multiple times 
	 * with unique IDs for each instance. **/
	public String getInputName  ();

	/** Field separator String for the input records from this source.**/
	public String getInputSeparator ();	
	
	/** Current input record. **/
	public String getInputRecord ();
	
	/** Current input line number (if appropriate).**/
	public long getInputLineNumber ();
	
	/** Current App Map ID in use.**/
	public String getAppMapUID();
	
	/** Current active log for this test level.**/
	public String getLogUID();
	
	/** Current stored status code for this test level. **/
	public long getStatusCode();
}

