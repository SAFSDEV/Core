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
 */
package org.safs.data.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author Lei Wang
 *
 */
@Entity
public class Teststep extends UpdatableDefault<Teststep> implements RestModel{
	/** 'teststeps' the base path to access entity */
	public static final String REST_BASE_PATH = "teststeps";
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private Long testcaseId;
	private Long statusId;
	private String logMessage;
	/**
	 *
	 */
	public Teststep() {
		super();
	}

	/**
	 * @param testcaseId
	 * @param statusId
	 * @param logMessage
	 */
	public Teststep(Long testcaseId, Long statusId, String logMessage) {
		super();
		this.testcaseId = testcaseId;
		this.statusId = statusId;
		this.logMessage = logMessage;
	}

	/**
	 * @return the id
	 */
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
	 * @return the testcaseId
	 */
	public Long getTestcaseId() {
		return testcaseId;
	}

	/**
	 * @param testcaseId the testcaseId to set
	 */
	public void setTestcaseId(Long testcaseId) {
		this.testcaseId = testcaseId;
	}

	/**
	 * @return the statusId
	 */
	public Long getStatusId() {
		return statusId;
	}

	/**
	 * @param statusId the statusId to set
	 */
	public void setStatusId(Long statusId) {
		this.statusId = statusId;
	}

	/**
	 * @return the logMessage
	 */
	public String getLogMessage() {
		return logMessage;
	}

	/**
	 * @param logMessage the logMessage to set
	 */
	public void setLogMessage(String logMessage) {
		this.logMessage = logMessage;
	}

	@Override
	public String getRestPath() {
		return REST_BASE_PATH;
	}
}
