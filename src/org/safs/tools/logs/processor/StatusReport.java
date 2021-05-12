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
 * @date 2018-04-28    (Lei Wang) Added methods countXXX(): return count number for status of different type.
 */
package org.safs.tools.logs.processor;

import java.util.HashMap;
import java.util.Map;

import org.safs.IndependantLog;
import org.safs.logging.MessageTypeInfo;

/**
 * @author Lei Wang
 *
 */
public class StatusReport extends Tag{
	/** The name of this status report */
	protected String name = null;
	protected String dateTime = null;
	/** If the test level has stopped */
	protected boolean stopped = false;

	/** Holding the status items in a hash map (type, item) */
	private Map<String, StatusItem> items = new HashMap<String, StatusItem>();

	public StatusItem getItems(String type) {
		return items.get(type);
	}

	public void addItem(StatusItem item) {
		if(!stopped){
			items.put(item.type, item);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}

	public boolean isStopped() {
		return stopped;
	}

	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}

	private int count(String type){
		if(items.containsKey(type)){
			return items.get(type).count();
		}else{
			IndependantLog.warn("Status of type '"+type+"' doesn't exist in this report.");
			return 0;
		}
	}

	public int countTotalRecords(){
		return count(MessageTypeInfo.STATUS_REPORT_RECORDS_XML_PREFIX);
	}
	public int countSkipped(){
		return count(MessageTypeInfo.STATUS_REPORT_SKIPPED_XML_PREFIX);
	}
	public int countTests(){
		return count(MessageTypeInfo.STATUS_REPORT_TESTS_XML_PREFIX);//"TEST RECORDS"
	}
	public int countTestPassed(){
		return count(MessageTypeInfo.STATUS_REPORT_TEST_PASSES_XML_PREFIX);//"TESTS PASSED"
	}
	public int countTestWarnings(){
		return count(MessageTypeInfo.STATUS_REPORT_TEST_WARNINGS_XML_PREFIX);//"TEST WARNINGS"
	}
	public int countTestFailures(){
		return count(MessageTypeInfo.STATUS_REPORT_TEST_FAILURES_XML_PREFIX);//"TEST FAILURES"
	}
	public int countGeneralTests(){
		return count(MessageTypeInfo.STATUS_REPORT_GENERAL_XML_PREFIX);
	}
	public int countGeneralTestPassed(){
		return count(MessageTypeInfo.STATUS_REPORT_GENERAL_PASSES_XML_PREFIX);
	}
	public int countGeneralTestWarnings(){
		return count(MessageTypeInfo.STATUS_REPORT_GENERAL_WARNINGS_XML_PREFIX);
	}
	public int countGeneralTestFailures(){
		return count(MessageTypeInfo.STATUS_REPORT_GENERAL_FAILURES_XML_PREFIX);
	}
	public int countIOFailures(){
		return count(MessageTypeInfo.STATUS_REPORT_IO_FAILURES_XML_PREFIX);
	}
}
