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
 * @date 2018-04-27    (Lei Wang) Initial release.
 * @date 2018-06-13    (Lei Wang) Added field 'parent' and 'messages'.
 * @date 2018-09-19    (Lei Wang) We don't increment the count of 'tests' inside the incrementErrors(), incrementFailures() and incrementSkipped().
 */
package org.safs.tools.logs.processor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Lei Wang
 *
 */
public abstract class TestLevel extends LogMessage{
	protected TestLevel parent = null;
	/** The start date of this test level */
	protected Date timestamp = null;
	/** The duration in seconds of this test level */
	protected long time = 0;
	/** The name of this test level */
	protected String name = null;
	/** Indicate the start of a certain test level; false means the end. */
	protected boolean start = true;

	/** The total test cases in this testsuite */
	protected int tests = 0;
	/** The total failed test cases in this testsuite */
	protected int failures = 0;
	/** The total errored test cases in this testsuite */
	protected int errors = 0;
	/** The total skipped test cases in this testsuite */
	protected int skipped = 0;

	public TestLevel getParent() {
		return parent;
	}
	public void setParent(TestLevel parent) {
		this.parent = parent;
	}
	/** Holding the message to be printed out with this test case */
	private List<LogMessage> messages = new ArrayList<LogMessage>();

	public void addMessage(LogMessage message){
		messages.add(message);
	}
	public List<LogMessage> getMessages() {
		return messages;
	}

	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isStart() {
		return start;
	}
	public void setStart(boolean start) {
		this.start = start;
	}
	public int getTests() {
		return tests;
	}
	public int incrementTests() {
		return ++tests;
	}
	public void setTests(int tests) {
		this.tests = tests;
	}
	public int getFailures() {
		return failures;
	}
	public int incrementFailures() {
		return ++failures;
	}
	public void setFailures(int failures) {
		this.failures = failures;
	}
	public int getErrors() {
		return errors;
	}
	public void setErrors(int errors) {
		this.errors = errors;
	}
	public int incrementErrors() {
		return ++errors;
	}
	public int getSkipped() {
		return skipped;
	}
	public int incrementSkipped() {
		return ++skipped;
	}
	public void setSkipped(int skipped) {
		this.skipped = skipped;
	}
}
