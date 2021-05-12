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
package org.safs.staf.service.logging;

import org.safs.staf.embedded.EmbeddedServiceHandle;
import org.safs.staf.embedded.ServiceInterface;
import org.safs.staf.service.InfoInterface;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFResult;

public class EmbeddedLogService extends AbstractSAFSLoggingService implements ServiceInterface {

	public EmbeddedLogService() { }

	@Override
	protected void registerHandle(String handleId)throws STAFException{
		String debugmsg = getClass().getName() + ".registerHandle():";
    	debugLog.debugPrintln(debugmsg+" registering EmbeddedServiceHandle: "+ handleId +" as service "+ servicename);
		handle = new EmbeddedServiceHandle(handleId, servicename, this);
		((EmbeddedServiceHandle)handle).register();
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

	/**
	 * Bypass STAFLog service initialization.  We don't need it.
	 */
	@Override
	protected STAFResult initSTAFLogService(){
		return new STAFResult(STAFResult.Ok);
	}

	/**
	 * Handles removing this service from STAF.
	 * <p>
	 * All log facilities are forced to close.
	 * <p>
	 */
	public final STAFResult term()
	{
		// close all log facilities and unregister
		return super.terminate();
	}

	@Override
	protected AbstractSTAFTextLogItem getSTAFTextLogItem(String name,String directory, String filename) {
		return new EmbeddedTextLogItem(name,directory,filename);
	}

	@Override
	protected AbstractSTAFXmlLogItem getSTAFXmlLogItem(String name, String directory, String filename) {
		return new EmbeddedXMLLogItem(name,directory,filename);
	}

	@Override
	public STAFResult terminateService() {
		return term();
	}
}
