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
 * @date 2018-03-06    (Lei Wang) Initial release.
 * @date 2018-06-04    (Lei Wang) Removed field 'engineId', 'timestamp'; Added fields 'testName', 'beginTimestamp' and 'endTimestamp'.
 */
package org.safs.data.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

/**
 *
 * @author Lei Wang
 *
 */
@Entity
public class History extends UpdatableDefault<History>  implements RestModel{
	/** 'histories' the base path to access entity */
	public static final String REST_BASE_PATH = "histories";
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private Long machineId;
	private Long frameworkId;
	private String userId;
	private String testName;
	private Date beginTimestamp;
	private Date endTimestamp;
	@Lob
	private String commandLine;

	public History() {
		super();
	}
	/**
	 * @param machineId
	 * @param frameworkId
	 * @param userId
	 * @param beginTimestamp
	 * @param commandLine
	 */
	public History(Long machineId, Long frameworkId, String userId, String testName, Date beginTimestamp, String commandLine) {
		super();
		this.machineId = machineId;
		this.frameworkId = frameworkId;
		this.userId = userId;
		this.testName = testName;
		this.beginTimestamp = beginTimestamp;
		this.commandLine = commandLine;
	}
	/**
	 * @return the id
	 */
	@Override
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * @return the machineId
	 */
	public Long getMachineId() {
		return machineId;
	}
	/**
	 * @param machineId the machineId to set
	 */
	public void setMachineId(Long machineId) {
		this.machineId = machineId;
	}
	/**
	 * @return the frameworkId
	 */
	public Long getFrameworkId() {
		return frameworkId;
	}
	/**
	 * @param frameworkId the frameworkId to set
	 */
	public void setFrameworkId(Long frameworkId) {
		this.frameworkId = frameworkId;
	}
	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Date getBeginTimestamp() {
		return beginTimestamp;
	}
	public void setBeginTimestamp(Date beginTimestamp) {
		this.beginTimestamp = beginTimestamp;
	}
	public Date getEndTimestamp() {
		return endTimestamp;
	}
	public void setEndTimestamp(Date endTimestamp) {
		this.endTimestamp = endTimestamp;
	}
	public String getTestName() {
		return testName;
	}
	public void setTestName(String testName) {
		this.testName = testName;
	}
	/**
	 * @return the commandLine
	 */
	public String getCommandLine() {
		return commandLine;
	}
	/**
	 * @param commandLine the commandLine to set
	 */
	public void setCommandLine(String commandLine) {
		this.commandLine = commandLine;
	}
	@Override
	public String getRestPath() {
		return REST_BASE_PATH;
	}
}
