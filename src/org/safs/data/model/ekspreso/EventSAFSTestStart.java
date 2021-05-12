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
public class EventSAFSTestStart extends EventSAFSTest{

	public EventSAFSTestStart() {
		super();
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
	public EventSAFSTestStart(String machine, String user, String testName, String timestamp, String commandline,
			String driver, String[] engines) {
		super(machine, user, testName, timestamp, commandline, driver, engines);
	}

	/**
	 * @param machine
	 * @param user
	 * @param testName
	 * @param timestamp
	 * @param commandline
	 * @param driver
	 */
	public EventSAFSTestStart(String machine, String user, String testName, String timestamp, String commandline,
			String driver) {
		super(machine, user, testName, timestamp, commandline, driver);
	}

	@Override
	public String getName() {
		return SAFS_TEST_START;
	}

}
