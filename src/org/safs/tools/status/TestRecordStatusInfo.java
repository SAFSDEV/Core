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
public class TestRecordStatusInfo implements StatusInfoInterface {

	protected StatusInterface     statusinfo     = null;
	protected TestRecordInterface testrecordinfo = null;

	/**
	 * Constructor for TestRecordStatusInfo
	 */
	public TestRecordStatusInfo() {
		super();
	}

	/**
	 * PREFERRED Constructor for TestRecordStatusInfo
	 */
	public TestRecordStatusInfo(StatusInterface statusinfo, 
	                            TestRecordInterface testrecordinfo) {
		this();
		setStatusInterface(statusinfo);
		setTestRecordInterface (testrecordinfo);
	}

	/**
	 * Set the statusinfo to the provided value.
	 */
	public void setStatusInterface(StatusInterface statusinfo) {
		this.statusinfo = statusinfo;
	}

	/**
	 * @see StatusInfoInterface#getStatusInterface()
	 */
	public StatusInterface getStatusInterface() {
		return statusinfo;
	}

	/**
	 * Set the testrecordinfo to the provided value.
	 */
	public void setTestRecordInterface(TestRecordInterface testrecordinfo) {
		this.testrecordinfo = testrecordinfo;
	}

	/**
	 * @see StatusInfoInterface#getTestRecordInterface()
	 */
	public TestRecordInterface getTestRecordInterface() {
		return testrecordinfo;
	}

}

