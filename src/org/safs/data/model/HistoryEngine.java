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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

/**
 *
 * @author Lei Wang
 *
 */
@Entity
@IdClass(HistoryEngineID.class)
public class HistoryEngine extends UpdatableDefault<HistoryEngine>  implements RestModel{
	/** 'histories' the base path to access entity */
	public static final String REST_BASE_PATH = "historyEngines";

	//We use @IdClass(HistoryEngineID.class) as the composite id
	//we use @Id to annotate field historyId and engineId
	@Id
	private Long historyId;
	@Id
	private Long engineId;

	public HistoryEngine() {
		super();
	}
	/**
	 * @param historyId
	 * @param engineId
	 */
	public HistoryEngine(Long historyId, Long engineId) {
		super();
		this.historyId = historyId;
		this.engineId = engineId;
	}
	public Long getHistoryId() {
		return historyId;
	}
	public void setHistoryId(Long historyId) {
		this.historyId = historyId;
	}
	public Long getEngineId() {
		return engineId;
	}
	public void setEngineId(Long engineId) {
		this.engineId = engineId;
	}
	@Override
	public String getRestPath() {
		return REST_BASE_PATH;
	}
	@Override
	public Object getId() {
		return new HistoryEngineID(historyId, engineId);
	}
}
