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
public class Testsuite extends UpdatableDefault<Testsuite> implements RestModel{
	/** 'testsuites' the base path to access entity */
	public static final String REST_BASE_PATH = "testsuites";
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
////	@ManyToOne(fetch=FetchType.LAZY)
//	//Caused by: org.hibernate.LazyInitializationException: could not initialize proxy - no Session
//	@ManyToOne(fetch=FetchType.EAGER)
//	private Testcycle testcycle;
	private Long testcycleId;

	private String name;
	private int tests;
	private int errors;
	private int failures;
	private int skipped;
	private double time;
	private Date timestamp;
	/**
	 *
	 */
	public Testsuite() {
		super();
	}

	/**
	 * @param testcycleId
	 * @param name
	 * @param tests
	 * @param failures
	 * @param skipped
	 * @param time
	 * @param timestamp
	 */
	public Testsuite(Long testcycleId, String name, int tests, int errors, int failures, int skipped, double time, Date timestamp) {
		super();
		this.testcycleId = testcycleId;
		this.name = name;
		this.tests = tests;
		this.errors = errors;
		this.failures = failures;
		this.skipped = skipped;
		this.time = time;
		this.timestamp = timestamp;
	}

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public int getErrors() {
		return errors;
	}

	public void setErrors(int errors) {
		this.errors = errors;
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
	 * @param timestampe the timestamp to set
	 */
	public void setTimestamp(Date timestampe) {
		this.timestamp = timestampe;
	}

	/**
	 * @return the testcycleId
	 */
	public Long getTestcycleId() {
		return testcycleId;
	}

	/**
	 * @param testcycleId the testcycleId to set
	 */
	public void setTestcycleId(Long testcycleId) {
		this.testcycleId = testcycleId;
	}

	@Override
	public String getRestPath() {
		return REST_BASE_PATH;
	}

//	/**
//	 * @return the testcycle
//	 */
//	public Testcycle getTestcycle() {
//		return testcycle;
//	}
//
//	/**
//	 * @param testcycle the testcycle to set
//	 */
//	public void setTestcycle(Testcycle testcycle) {
//		this.testcycle = testcycle;
//	}
}
