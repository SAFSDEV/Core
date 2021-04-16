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

import org.safs.staf.embedded.EmbeddedServiceHandle;
import org.safs.staf.embedded.ServiceInterface;
import org.safs.staf.service.InfoInterface;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFResult;

/**
 * A subclass of SAFSINPUT service intending to run embedded in the same JVM/Process as the Driver using it.
 * That is, it does NOT run as a STAF service in a separate JVM. However, it can interface to STAF as needed.
 * <p>
 * For this to work, the InputInterface classes must be properly initialized to know to use
 * this SAFSINPUT service instead of the standard STAF SAFSINPUT service.
 * <p>
 * @author Carl Nagle
 */
public class EmbeddedInputService extends AbstractSAFSInputService implements ServiceInterface {

	public EmbeddedInputService() {	}

	@Override
	protected void registerHandle(String handleId)throws STAFException{
		String debugmsg = getClass().getName() + ".registerHandle():";
    	debugLog.debugPrintln(debugmsg+" registering EmbeddedServiceHandle: "+ handleId +" as service "+ servicename);
		fHandle = new EmbeddedServiceHandle(handleId, servicename, this);
		((EmbeddedServiceHandle)fHandle).register();
	}

	/**
	 * Intercepts initializing the instance of the service to get servicename information.
	 * <p>
	 * This service is registered under process name
	 * "STAF/Service/&lt;ServiceName>".
	 * <p>
	 */
	@Override
	public STAFResult init(InfoInterface.InitInfo initInfo)
	{
		String debugmsg = getClass().getName() + ".init():";
		this.servicename = initInfo.name;
    	debugLog.debugPrintln(debugmsg+" getting servicename: "+ servicename);
		return super.init(initInfo);
	}

	@Override
	public STAFResult terminateService() {
		return terminate();
	}
}
