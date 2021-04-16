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
package org.safs.staf.service.logging.v3;

import org.safs.staf.service.InfoInterface;
import org.safs.staf.service.logging.AbstractSAFSCustomLoggingService;

import com.ibm.staf.STAFResult;
import com.ibm.staf.service.STAFServiceInterfaceLevel30;

/**
 * This class is custom Logging service of STAF version 3.
 * All detail are written in AbstractSAFSCustomLoggingService.
 * 
 * Here we need only realize the methods defined in STAFServiceInterfaceLevel30
 * init(STAFServiceInterfaceLevel30.InitInfo)
 * acceptRequest(STAFServiceInterfaceLevel30.RequestInfo)
 * term()
 * To realize these methods, we need to convert the InitInfo and RequestInfo of 
 * STAFServiceInterfaceLevel30 to that of InfoInterface, and then call the method
 * defined in the super class AbstractSAFSLoggingService
 * init(InfoInterface.InitInfo)
 * acceptRequest(InfoInterface.RequestInfo)
 * terminate()
 * 
 * @see AbstractSAFSCustomLoggingService
 */

public class SAFSCustomLoggingService3 extends AbstractSAFSCustomLoggingService implements STAFServiceInterfaceLevel30
{
	public SAFSCustomLoggingService3() {}

	/**
	 * Handles initializing this instance of the service for STAF.
	 * <p>
	 * This service is registered under process name 
	 * "STAF/Service/&lt;ServiceName>".
	 * <p>
	 */
	public STAFResult init(STAFServiceInterfaceLevel30.InitInfo initInfo)
	{
		InfoInterface.InitInfo info = new InfoInterface.InitInfo(initInfo.name,initInfo.parms);
		STAFResult result = super.init(info);
		return result;
	}


	/**
	 * Handles service request from STAF.
	 * <p>
	 */
	public STAFResult acceptRequest(STAFServiceInterfaceLevel30.RequestInfo requestInfo)
	{
		InfoInterface.RequestInfo info = new InfoInterface.RequestInfo(requestInfo.request);
		
		return super.acceptRequest(info);
	}

	/**
	 * Handles removing this service from STAF.
	 * <p>
	 */
	public STAFResult term()
	{
		STAFResult result = super.terminate();
		return result;
	}
}
