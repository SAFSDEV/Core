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
 * @date 2018-06-01    (Lei Wang) Initial release.
 */
package org.safs.data.model;

import java.io.Serializable;

/**
 * @author Lei Wang
 *
 */
@SuppressWarnings("serial")
public class HistoryEngineID implements Serializable{
	public static final String SEPARATOR_ID = "-";
	private Long historyId;
	private Long engineId;

	public HistoryEngineID(){

	}

	public HistoryEngineID(Long historyId, Long engineId){
		this.historyId = historyId;
		this.engineId = engineId;
	}

	public HistoryEngineID(String id){
		try{
			String[] ids = id.split(SEPARATOR_ID);
			historyId = Long.parseLong(ids[0]);
			engineId = Long.parseLong(ids[1]);
		}catch(Exception e){
			throw new IllegalArgumentException("IllegalArgument: The id '"+id+"' is NOT in format 'historyId"+SEPARATOR_ID+"engineId'.");
		}
	}

	/**
	 * @return the historyId
	 */
	public Long getHistoryId() {
		return historyId;
	}

	/**
	 * @param historyId the historyId to set
	 */
	public void setHistoryId(Long historyId) {
		this.historyId = historyId;
	}

	/**
	 * @return the engineId
	 */
	public Long getEngineId() {
		return engineId;
	}

	/**
	 * @param engineId the engineId to set
	 */
	public void setEngineId(Long engineId) {
		this.engineId = engineId;
	}
}
