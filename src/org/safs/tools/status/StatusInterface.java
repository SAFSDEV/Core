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
 * @date 2018-12-12    (Lei Wang) Add methods for "remote tracking".
 */
package org.safs.tools.status;

/**
 * Provides access to the current status--usually a snapshot--of a status object.
 */
public interface StatusInterface {

	/** CYCLE, SUITE, STEP, or a Unique ID. **/
	public String getTestLevel ();

	public long getTotalRecords ();

	public long getTestFailures ();
	public long getTestWarnings ();
	public long getTestPasses   ();

	public long getGeneralFailures ();
	public long getGeneralWarnings ();
	public long getGeneralPasses   ();

	public long getIOFailures     ();
	public long getSkippedRecords ();
	public boolean isSuspended();

	public String getTrackingSystem();
	public String getTrackingStatus();
	public String getTrackingComment();

	public void setTrackingSystem(String trackingSystem);
	public void setTrackingStatus(String trackingStatus);
	public void setTrackingComment(String trackingComment);

}

