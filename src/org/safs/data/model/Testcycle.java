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
 * @date 2018-06-05    (Lei Wang) Added field 'user', 'machine', 'ip' and 'testName'.
 */
package org.safs.data.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author Lei Wang
 *
 */
@Entity
public class Testcycle extends UpdatableDefault<Testcycle> implements RestModel{
	/** 'testcycles' the base path to access entity */
	public static final String REST_BASE_PATH = "testcycles";
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
//	//mappedBy="testcycle" will create a 'testcycle_id' references testcycle (id) in the Testsuite table.
//	@OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="testcycle")
//	private List<Testsuite> testsuites;
	private Long orderableId;
	private String name;
	private int tests;
	private int errors;
	private int failures;
	private int skipped;
	private double time;
	private Date timestamp;

	//The following fields are used to link with History table
	private String testName;
	private String user;
	private String machine;
	private String ip;

	public Testcycle() {
		super();
	}

	public Testcycle(Long orderableId, String name, int tests, int errors, int failures, int skipped, double time, Date timestamp, String testName, String user, String machine, String ip) {
		this(orderableId, name, errors, tests, failures, skipped, time, timestamp);
		this.testName = testName;
		this.user = user;
		this.machine = machine;
		this.ip = ip;
	}

	public Testcycle(String name, int tests, int errors, int failures, int skipped, double time, Date timestamp, String testName, String user, String machine, String ip) {
		this(name, tests, errors, failures, skipped, time, timestamp);
		this.testName = testName;
		this.user = user;
		this.machine = machine;
		this.ip = ip;
	}

	public Testcycle(Long orderableId, String name, int tests, int errors, int failures, int skipped, double time, Date timestamp) {
		this(name, tests, errors, failures, skipped, time, timestamp);
		this.orderableId = orderableId;
	}

	@Deprecated
	public Testcycle(String name, int tests, int errors, int failures, int skipped, double time, Date timestamp) {
		this();
		this.name = name;
		this.tests = tests;
		this.errors = errors;
		this.failures = failures;
		this.skipped = skipped;
		this.time = time;
		this.timestamp = timestamp;
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
	 * @return the orderableId
	 */
	public Long getOrderableId() {
		return orderableId;
	}
	/**
	 * @param orderableId the orderableId to set
	 */
	public void setOrderableId(Long orderableId) {
		this.orderableId = orderableId;
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
	public int getErrors() {
		return errors;
	}

	public void setErrors(int errors) {
		this.errors = errors;
	}

	/**
	 * @return the tests
	 */
	public int getTests() {
		return tests;
	}
	/**
	 * @param tests the tests to set
	 */
	public void setTests(int tests) {
		this.tests = tests;
	}
	/**
	 * @return the failures
	 */
	public int getFailures() {
		return failures;
	}
	/**
	 * @param failures the failures to set
	 */
	public void setFailures(int failures) {
		this.failures = failures;
	}
	/**
	 * @return the skipped
	 */
	public int getSkipped() {
		return skipped;
	}
	/**
	 * @param skipped the skipped to set
	 */
	public void setSkipped(int skipped) {
		this.skipped = skipped;
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
	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return timestamp;
	}
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getMachine() {
		return machine;
	}

	public void setMachine(String machine) {
		this.machine = machine;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Override
	public String getRestPath() {
		return REST_BASE_PATH;
	}

//	/**
//	 * @return the testsuites
//	 */
//	public List<Testsuite> getTestsuites() {
//		return testsuites;
//	}
//	/**
//	 * @param testsuites the testsuites to set
//	 */
//	public void setTestsuites(List<Testsuite> testsuites) {
//		this.testsuites = testsuites;
//	}

}
