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

public interface UniqueMessageInterface extends UniqueIDInterface {

	/** The message to log.**/
	public String getLogMessage();

	/** Optional. Additional details associated with the message.  In many logs, this
	 * appears as an additional descriptive line in log output.
	 * This may be null or an empty string, and should be ignored if it is such.**/
	public String getLogMessageDescription();

	/** The type of the message to log.
	 * For example, a GENERIC message, a PASSED message, a FAILED message, etc..
	 * This may be implementation specific.**/
	public int   getLogMessageType();
}

