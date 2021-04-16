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

public interface UniqueLogInterface extends UniqueLogLevelInterface {

	/** Use an alternate name for an enabled text log.
	 * Normally, the log will inherit the name given as the UniqueID. **/
	public String getTextLogName();

	/** Use an alternate name for an enabled XML log.
	 * Normally, the log will inherit the name given as the UniqueID. **/
	public String getXMLLogName();

	/**
	 * Get the log modes enabled for this log.
	 * These are normally values OR'd together to indicate which logs are enabled.
	 * These values will be implementation specific. **/
	public long getLogModes();
	
	/**
	 * Get the unique ID of any log linked or chained with this one.
	 */
	public UniqueIDInterface getLinkedFac();
}

