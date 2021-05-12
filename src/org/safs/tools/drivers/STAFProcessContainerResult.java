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
package org.safs.tools.drivers;

import org.safs.tools.engines.EngineInterface;

/**
 * Used to capture status information and response from SAFS engines in a Driver environment.
 * An instance of this class can hold the EngineInterface that executed the command, and the 
 * statuscode and statusinfo resulting from that command.
 * <p>
 * This is normally used in a non-test scenario such as when a special-case Driver like 
 * STAFProcessContainer is used to communicate with running engines. 
 * @author Carl Nagle
 */
public class STAFProcessContainerResult {
	
	EngineInterface _engine;
	long _statusCode;
	String _statusInfo;

	/**
	 * Instantiate a result that captures the engine, statuscode, and statusinfo.
	 * @param _engine EngineInterface that processed the command.
	 * @param _statusCode TestRecordHelper.statusCode received as result.
	 * @param _statusInfo TestRecordHelper.statusInfo received as result.
	 */
	public STAFProcessContainerResult(EngineInterface _engine, long _statusCode, String _statusInfo){
		this._engine     = _engine;
		this._statusCode = _statusCode;
		this._statusInfo = _statusInfo;
	}
	
	/**
	 * Normally stores the EngineInterface to the engine that processed a command.
	 * @return Returns the _engine.
	 */
	public EngineInterface get_engine() {
		return _engine;
	}
	/**
	 * Normally stores a TestRecordHelper.statusCode value resulting from an engine dispatch
	 * result.
	 * @return Returns the _statusCode.
	 */
	public long get_statusCode() {
		return _statusCode;
	}
	/**
	 * Normally stores a TestRecordHelper.statusInfo value resulting from an engine dispatch
	 * result.
	 * @return Returns the _statusInfo.
	 */
	public String get_statusInfo() {
		return _statusInfo;
	}
}
