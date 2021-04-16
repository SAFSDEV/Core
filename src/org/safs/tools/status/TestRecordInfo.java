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
public class TestRecordInfo implements TestRecordInterface {

	protected String testlevel = null;
	protected String inputid   = null;
	protected String inputname = null;
	protected String inputseparator = null;
	protected String inputrecord = null;
	protected long   inputlinenumber = -1;
	protected String mapid = null;
	protected String logid = null;
	protected long   statuscode = -1;
	
	/**
	 * Constructor for TestRecordInfo
	 */
	public TestRecordInfo() {
		super();
	}

	/**
	 * PREFERRED Constructor for TestRecordInfo
	 */
	public TestRecordInfo(String testlevel,
	                      String inputid, String inputname, 
	                      String inputrecord, String inputseparator, 
	                      long inputlinenumber,
	                      String mapid, String logid,
	                      long statuscode) {
		this();
		setTestLevel(testlevel);
		setInputUID(inputid);
		setInputName(inputname);
		setInputRecord(inputrecord);
		setInputSeparator(inputseparator);
		setInputLineNumber(inputlinenumber);
		setAppMapUID(mapid);
		setLogUID(logid);
		setStatusCode(statuscode);
	}

	/**
	 * Set testlevel to the provided value.
	 */
	public void setTestLevel(String testlevel) {
		this.testlevel = testlevel;
	}

	/**
	 * @see TestRecordInterface#getTestLevel()
	 */
	public String getTestLevel() {
		return testlevel;
	}

	/**
	 * Set inputid to the provided value.
	 */
	public void setInputUID(String inputid) {
		this.inputid = inputid;
	}

	/**
	 * @see TestRecordInterface#getInputUID()
	 */
	public String getInputUID() {
		return inputid;
	}

	/**
	 * Set inputname to the provided value.
	 */
	public void setInputName(String inputname) {
		this.inputname = inputname;
	}

	/**
	 * @see TestRecordInterface#getInputName()
	 */
	public String getInputName() {
		return inputname;
	}

	/**
	 * Set inputseparator to the provided value.
	 */
	public void setInputSeparator(String inputseparator) {
		this.inputseparator = inputseparator;
	}

	/**
	 * @see TestRecordInterface#getInputSeparator()
	 */
	public String getInputSeparator() {
		return inputseparator;
	}

	/**
	 * Set inputrecord to the provided value.
	 */
	public void setInputRecord(String inputrecord) {
		this.inputrecord = inputrecord;
	}

	/**
	 * @see TestRecordInterface#getInputRecord()
	 */
	public String getInputRecord() {
		return inputrecord;
	}

	/**
	 * Set inputlinenumber to the provided value.
	 */
	public void setInputLineNumber(long inputlinenumber) {
		this.inputlinenumber = inputlinenumber;
	}

	/**
	 * @see TestRecordInterface#getInputLineNumber()
	 */
	public long getInputLineNumber() {
		return inputlinenumber;
	}

	/**
	 * Set mapid to the provided value.
	 */
	public void setAppMapUID(String mapid) {
		this.mapid = mapid;
	}

	/**
	 * @see TestRecordInterface#getAppMapUID()
	 */
	public String getAppMapUID() {
		return mapid;
	}

	/**
	 * Set logid to the provided value.
	 */
	public void setLogUID(String logid) {
		this.logid = logid;
	}

	/**
	 * @see TestRecordInterface#getLogUID()
	 */
	public String getLogUID() {
		return logid;
	}

	/**
	 * Set statuscode to the provided value.
	 */
	public void setStatusCode(long statuscode) {
		this.statuscode = statuscode;
	}

	/**
	 * @see TestRecordInterface#getStatusCode()
	 */
	public long getStatusCode() {
		return statuscode;
	}

}

