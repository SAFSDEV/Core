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
package org.safs.tools.counters;

import org.safs.tools.status.StatusCounter;

public class Counter extends StatusCounter implements CountStatusInterface{

	protected long mode = CountersInterface.ALL_STATUS_INFO_MODE;
	
	/**
	 * Constructor for Counter
	 */
	public Counter() {
		super();
	}

	/**
	 * Constructor for Counter
	 */
	public Counter(	    
		long totalrecords,
		long skippedrecords,
		long iofailures,
		long testfailures,
		long testwarnings,
		long testpasses,
		long generalfailures,
		long generalwarnings,
		long generalpasses) {
		super(
			null,
			totalrecords,
			skippedrecords,
			iofailures,
			testfailures,
			testwarnings,
			testpasses,
			generalfailures,
			generalwarnings,
			generalpasses,
			false);
	}
	
	public void suspend(){ suspended = true;}
	public void resume(){ suspended = false;}

	public void setMode(long mode){ this.mode = mode; }
	public long getMode(){ return mode; }
}

