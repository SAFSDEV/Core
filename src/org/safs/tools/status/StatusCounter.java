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
public class StatusCounter extends StatusInfo implements StatusCounterInterface {

	/**
	 * Constructor for StatusCounter
	 */
	public StatusCounter() {
		super();
	}

	/**
	 * Constructor for StatusCounter
	 */
	public StatusCounter(String testlevel,
				 		 long totalrecords,	   long skippedrecords,  long iofailures,
						 long testfailures,    long testwarnings,    long testpasses,
 						 long generalfailures, long generalwarnings, long generalpasses,
 						 boolean suspended) {

		super(testlevel,       
		      totalrecords,    skippedrecords,  iofailures,
			  testfailures,    testwarnings,    testpasses,
			  generalfailures, generalwarnings, generalpasses,
			  suspended);
	}
	
	/** set or clear the suspended status. */
	public void setSuspended(boolean suspend){ suspended = suspend; }

	/**	
	 * If counts are not suspended, increments the count for both skipped records and total records. 
	 * @return the count of skipped records. */
	public long incrementSkippedRecords(){ 
		if(!suspended) {
			totalrecords++; 
			return ++skippedrecords;
		}else 
			return skippedrecords;
	}

	/**	
	 * If counts are not suspended, increments the count for both iofailures and total records. 
	 * @return the count of iofailures. */
	public long incrementIOFailures(){ 
		if(!suspended) {
			totalrecords++; 
			return ++iofailures;
		}else
			return iofailures;
	}

	/**	
	 * If counts are not suspended, increments the count for iofailures, test failures, and total records.
	 * @return the count of iofailures. */	 
	public long incrementTestIOFailures(){ 
		if(!suspended) {
			totalrecords++; 
			testfailures++; 
			return ++iofailures;
		}else
			return iofailures;
	}


	/**	If counts are not suspended, increments the count for both test failures and total records. 
	 * @return the count of test failures. */
	public long incrementTestFailures(){ 
		if(!suspended){
			totalrecords++; 
			return ++testfailures;
		}else
			return testfailures;
	}

	/**	If counts are not suspended, increments the count for both test warnings and total records. 
	 * @return the count of test warnings */
	public long incrementTestWarnings(){ 
		if(!suspended){
			totalrecords++; 
			return ++testwarnings;
		}else
			return testwarnings;
	}

	/**	If counts are not suspended, increments the count for both test passes and total records. 
	 * @return the count of test passes */
	public long incrementTestPasses(){ 
		if(!suspended){
			totalrecords++; 
			return ++testpasses;
		}else
			return testpasses;
	}


	/**	If counts are not suspended, increments the count for both general failures and total records. 
	 * @return the count of general failures */
	public long incrementGeneralFailures(){ 
		if(!suspended){
			totalrecords++; 
			return ++generalfailures;
		}else
			return generalfailures;
	}

	/**	If counts are not suspended, increments the count for both general warnings and total records. 
	 * @return the count of general warnings */
	public long incrementGeneralWarnings(){ 
		if(!suspended){
			totalrecords++; 
			return ++generalwarnings;
		}else
			return generalwarnings;
	}

	/**	If counts are not suspended, increments the count for both general passes and total records. 
	 * @return the count of general passes */
	public long incrementGeneralPasses(){ 
		if(!suspended){
			totalrecords++; 
			return ++generalpasses;
		}else
			return generalpasses;
	}

	/**	If counts are not suspended, add the provided counter to this counter. */
	public void addStatus(StatusInterface counter){ 
		if(!suspended){
			totalrecords += counter.getTotalRecords();
			testfailures += counter.getTestFailures();
			testwarnings += counter.getTestWarnings();
			testpasses   += counter.getTestPasses();
			generalfailures += counter.getGeneralFailures();
			generalwarnings += counter.getGeneralWarnings();
			generalpasses   += counter.getGeneralPasses();
			iofailures      += counter.getIOFailures();
			skippedrecords  += counter.getSkippedRecords();
		}
	}
}

