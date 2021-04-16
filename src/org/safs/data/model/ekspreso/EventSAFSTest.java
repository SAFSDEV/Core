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
 * @date 2018-06-21    (Lei Wang) Initial release.
 */
package org.safs.data.model.ekspreso;

/**
 * @author Lei Wang
 *
 */
public abstract class EventSAFSTest extends Event{
	private String machine;
	private String user;
	private String testName;
	private String timestamp;//it can be 'start_time' or 'stop_time'
	private String commandline;
	private String driver;
	private String[] engines;

	public EventSAFSTest() {
		super();
	}

	/**
	 * @param machine
	 * @param user
	 * @param testName
	 * @param timestamp
	 * @param commandline
	 * @param driver
	 */
	public EventSAFSTest(String machine, String user, String testName, String timestamp, String commandline, String driver) {
		super();
		this.machine = machine;
		this.user = user;
		this.testName = testName;
		this.timestamp = timestamp;
		this.driver = driver;
		this.commandline = commandline;
	}

	/**
	 * @param machine
	 * @param user
	 * @param testName
	 * @param timestamp
	 * @param commandline
	 * @param driver
	 * @param engines
	 */
	public EventSAFSTest(String machine, String user, String testName, String timestamp, String commandline, String driver, String[] engines) {
		this(machine, user, testName, timestamp, commandline, driver);
		this.engines = engines;
	}

	/**
	 * @return the machine
	 */
	public String getMachine() {
		return machine;
	}

	/**
	 * @param machine the machine to set
	 */
	public void setMachine(String machine) {
		this.machine = machine;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the testName
	 */
	public String getTestName() {
		return testName;
	}

	/**
	 * @param testName the testName to set
	 */
	public void setTestName(String testName) {
		this.testName = testName;
	}

	/**
	 * @return the timestamp
	 */
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the driver
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * @param driver the driver to set
	 */
	public void setDriver(String driver) {
		this.driver = driver;
	}

	/**
	 * @return the commandline
	 */
	public String getCommandline() {
		return commandline;
	}

	/**
	 * @param commandline the commandline to set
	 */
	public void setCommandline(String commandline) {
		this.commandline = commandline;
	}

	/**
	 * @return the engines
	 */
	public String[] getEngines() {
		return engines;
	}

	/**
	 * @param engines the engines to set
	 */
	public void setEngines(String[] engines) {
		this.engines = engines;
	}

}
