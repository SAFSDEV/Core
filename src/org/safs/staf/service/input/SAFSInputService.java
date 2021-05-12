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
package org.safs.staf.service.input;

import org.safs.staf.service.InfoInterface;

import com.ibm.staf.STAFResult;
import com.ibm.staf.service.STAFServiceInterfaceLevel3;

/**
 * This class is Input service of STAF version 2.
 * All detail are written in AbstractSAFSInputService.
 *
 * Here we need only realize the methods defined in STAFServiceInterfaceLevel3
 * init(STAFServiceInterfaceLevel3.InitInfo)
 * acceptRequest(STAFServiceInterfaceLevel3.RequestInfo)
 * term()
 * To realize these methods, we need to convert the InitInfo and RequestInfo of
 * STAFServiceInterfaceLevel3 to that of InfoInterface, and then call the method
 * defined in the super class AbstractSAFSLoggingService
 * init(InfoInterface.InitInfo)
 * acceptRequest(InfoInterface.RequestInfo)
 * terminate()
 *
 * @see AbstractSAFSInputService
 */

public class SAFSInputService extends AbstractSAFSInputService implements STAFServiceInterfaceLevel3
{
	public SAFSInputService() {}

	/**
	 * Handles initializing this instance of the service for STAF.
	 * <p>
	 * This service is registered under process name
	 * "STAF/Service/&lt;ServiceName>".
	 * <p>
	 */
	@Override
	public final int init(STAFServiceInterfaceLevel3.InitInfo initInfo)
	{
		InfoInterface.InitInfo info = new InfoInterface.InitInfo(initInfo.name,initInfo.parms);
		STAFResult result = super.init(info);
		return result.rc;
	}

	/**
	 * Handles service request from STAF.
	 * <p>
	 */
	@Override
	public STAFResult acceptRequest(STAFServiceInterfaceLevel3.RequestInfo requestInfo)
	{
		//In STAF 3, the processName is replaced by handleName
		InfoInterface.RequestInfo info = new InfoInterface.RequestInfo(requestInfo.machine,
																	   requestInfo.handle,
																	   requestInfo.processName,
																	   requestInfo.request);
		return super.acceptRequest(info);
	}

	/**
	 * Handles removing this service from STAF.
	 * <p>
	 * All log facilities are forced to close.
	 * <p>
	 */
	@Override
	public final int term()
	{
		STAFResult result = super.terminate();
		return result.rc;
	}
}





















