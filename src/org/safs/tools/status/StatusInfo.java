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
 * @date 2018-12-12    (Lei Wang) Add methods for "remote tracking".
 */
package org.safs.tools.status;

import org.safs.testrail.TestRailConstants;

public class StatusInfo implements StatusInterface {

	protected boolean suspended = false;
	protected String testlevel = null;

	protected long totalrecords = 0;

	protected long skippedrecords = 0;
	protected long iofailures = 0;

	protected long testfailures = 0;
	protected long testwarnings = 0;
	protected long testpasses = 0;

	protected long generalfailures = 0;
	protected long generalwarnings = 0;
	protected long generalpasses = 0;

	protected String trackingSystem = null;
	protected String trackingStatus = TestRailConstants.TESTCASE_RESULT_STATUS_INVALID;
	protected String trackingComment = null;

	/**
	 * Constructor for StatusInfo to produce a new all-zeros set of counts.
	 * The TestLevel must be set with a call to setTestLevel after using this
	 * constructor.
	 */
	public StatusInfo() {
		super();
	}

	/** @return true if status counts are suspended. */
	@Override
	public boolean isSuspended(){ return suspended; }
	/**
	 * Constructor for StatusInfo to pre-initilize non-zero counts in a new instance.
	 */
	public StatusInfo(String testlevel,
	                  long totalrecords, long skippedrecords, long iofailures,
					  long testfailures, long testwarnings, long testpasses,
					  long generalfailures, long generalwarnings, long generalpasses,
					  boolean suspended) {
		this();
		setTestLevel(testlevel);
		this.totalrecords=totalrecords;
		this.skippedrecords=skippedrecords;
		this.iofailures=iofailures;

		this.testfailures=testfailures;
		this.testwarnings=testwarnings;
		this.testpasses=testpasses;

		this.generalfailures=generalfailures;
		this.generalwarnings=generalwarnings;
		this.generalpasses=generalpasses;
		this.suspended=suspended;
	}

	/**
	 * Set the Test Level for this instance.
	 * 'CYCLE', 'SUITE', 'STEP', or another implementation-specified String.
	 * Normally only set once after a call to the no-arg constructor.
	 */
	public void setTestLevel(String testlevel) {
		this.testlevel = testlevel;
	}

	/**
	 * @see StatusInterface#getTestLevel()
	 */
	@Override
	public String getTestLevel() {
		return testlevel;
	}

	/**
	 * @see StatusInterface#getTotalRecords()
	 */
	@Override
	public long getTotalRecords() {
		return totalrecords;
	}

	/**
	 * @see StatusInterface#getTestFailures()
	 */
	@Override
	public long getTestFailures() {
		return testfailures;
	}

	/**
	 * @see StatusInterface#getTestWarnings()
	 */
	@Override
	public long getTestWarnings() {
		return testwarnings;
	}

	/**
	 * @see StatusInterface#getTestPasses()
	 */
	@Override
	public long getTestPasses() {
		return testpasses;
	}

	/**
	 * @see StatusInterface#getGeneralFailures()
	 */
	@Override
	public long getGeneralFailures() {
		return generalfailures;
	}

	/**
	 * @see StatusInterface#getGeneralWarnings()
	 */
	@Override
	public long getGeneralWarnings() {
		return generalwarnings;
	}

	/**
	 * @see StatusInterface#getGeneralPasses()
	 */
	@Override
	public long getGeneralPasses() {
		return generalpasses;
	}

	/**
	 * @see StatusInterface#getIOFailures()
	 */
	@Override
	public long getIOFailures() {
		return iofailures;
	}

	/**
	 * @see StatusInterface#getSkippedRecords()
	 */
	@Override
	public long getSkippedRecords() {
		return skippedrecords;
	}

	@Override
	public String getTrackingSystem(){
		return trackingSystem;
	}
	@Override
	public String getTrackingStatus(){
		return trackingStatus;
	}
	@Override
	public String getTrackingComment(){
		return trackingComment;
	}

	@Override
	public void setTrackingSystem(String trackingSystem) {
		this.trackingSystem = trackingSystem;
	}

	@Override
	public void setTrackingStatus(String trackingStatus) {
		this.trackingStatus = trackingStatus;
	}

	@Override
	public void setTrackingComment(String trackingComment) {
		this.trackingComment = trackingComment;
	}

}

