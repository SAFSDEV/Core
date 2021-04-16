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

import org.safs.tools.UniqueStringID;

public class UniqueStringMessageInfo
	extends UniqueStringID
	implements UniqueMessageInterface {

	protected String message     = null;
	protected String description = null;
	protected int   type        = 0;
	
	/**
	 * Constructor for UniqueStringMessageInfo
	 */
	public UniqueStringMessageInfo() {
		super();
	}

	/**
	 * Constructor for UniqueStringMessageInfo
	 */
	public UniqueStringMessageInfo(String id) {
		super(id);
	}

	/**
	 * PREFERRED Constructor for UniqueStringMessageInfo
	 */
	public UniqueStringMessageInfo(String id, String message, 
	                               String description, int type) {
		this(id);
		setLogMessage(message);
		setLogMessageDescription(description);
		setLogMessageType(type);
	}

	/**
	 * Set message to String provided.
	 */
	public void setLogMessage(String message) {
		this.message = message;
	}

	/**
	 * @see UniqueMessageInterface#getLogMessage()
	 */
	public String getLogMessage() {
		return message;
	}

	/**
	 * Set message description to String provided.
	 */
	public void setLogMessageDescription(String description) {
		this.description = description;
	}

	/**
	 * @see UniqueMessageInterface#getLogMessageDescription()
	 */
	public String getLogMessageDescription() {
		return description;
	}

	/**
	 * Set message type to that provided.
	 */
	public void setLogMessageType(int type) {
		this.type = type;
	}

	/**
	 * @see UniqueMessageInterface#getLogMessageType()
	 */
	public int getLogMessageType() {
		return type;
	}

}

