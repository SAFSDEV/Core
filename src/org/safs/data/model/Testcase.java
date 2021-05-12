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
 * @date 2018-03-05    (Lei Wang) Initial release.
 * @date 2018-05-02    (Lei Wang) Added one more field 'classname'.
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
public class Testcase extends UpdatableDefault<Testcase> implements RestModel{
	/** 'testcases' the base path to access entity */
	public static final String REST_BASE_PATH = "testcases";
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private Long testsuiteId;
	private String name;
	private String classname;
	private double time;

	public Testcase() {
		super();
	}
	/**
	 * @param testsuiteId
	 * @param name
	 * @param classname
	 * @param time
	 */
	public Testcase(Long testsuiteId, String name, String classname, double time) {
		super();
		this.testsuiteId = testsuiteId;
		this.name = name;
		this.classname = classname;
		this.time = time;
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
	 * @return the testsuiteId
	 */
	public Long getTestsuiteId() {
		return testsuiteId;
	}
	/**
	 * @param testsuiteId the testsuiteId to set
	 */
	public void setTestsuiteId(Long testsuiteId) {
		this.testsuiteId = testsuiteId;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the time
	 */
	public double getTime() {
		return time;
	}
	/**
	 * @param time the time to set
	 */
	public void setTime(double time) {
		this.time = time;
	}
	public String getClassname() {
		return classname;
	}
	public void setClassname(String classname) {
		this.classname = classname;
	}
	@Override
	public String getRestPath() {
		return REST_BASE_PATH;
	}
}
