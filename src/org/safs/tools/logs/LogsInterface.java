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
package org.safs.tools.logs;

import org.safs.tools.UniqueIDInterface;
import org.safs.tools.status.StatusInterface;

public interface LogsInterface {

	/** 
	 * "NOT PROVIDED", Default StatusInfo ID for logStatusInfo.
	 **/
	public static final String DEFAULT_STATUSINFO_ID = "NOT PROVIDED";	
	

	/** Initialize/Open the specified log.**/
	public void initLog (UniqueLogInterface logInfo);
	
	/** 
	 * Set/Change the log level or filter for log messages.
	 * This will likely be implementation specific.**/
	public void setLogLevel (UniqueLogLevelInterface loglevel);
	
	/** Log a message to a particular log.**/
	public void logMessage (UniqueMessageInterface message);
	
	/** 
	 * Log a Status Report to a particular log.
	 * @param log -- the facname of the log to generate the report.
	 * @param status -- the StatusInterface containing the counts to report.
	 * @param infoID -- Name or ID to give to the status information in the log.
	 *                  This is usually used to show the subject of the status info.
	 *                  For example, "Regression Test", "TestCase 123456", etc..
	 *                  DEFAULT_STATUSINFO_ID used if null or zero-length.
	 **/
	public void logStatusInfo (UniqueIDInterface log, StatusInterface status, String infoID);
	
	/** Suspend logging for a particular log.**/
	public void suspendLog (UniqueIDInterface log);
	
	/** Suspend logging to ALL open logs.**/
	public void suspendAllLogs ();
	
	/** Resume logging to a previously suspended log.**/
	public void resumeLog (UniqueIDInterface log);
	
	/** Resume loggint to ALL previosly suspended logs.**/
	public void resumeAllLogs ();
	
	/** Enable truncation of logged messages to the numchars length provided. */
	public void truncate(int numchars);
	
	/** Enable/Disable the truncation of logged messages. */
	public void truncate(boolean enabled);
	
	/** 
	 * Close the specified log. 
	 * Perform any log post-close activities and then release resources .**/
	public void closeLog (UniqueIDInterface log);
	
	/** 
	 * Close ALL logs. 
	 * Perform any log post-close activities and then release resources .**/
	public void closeAllLogs ();
}

