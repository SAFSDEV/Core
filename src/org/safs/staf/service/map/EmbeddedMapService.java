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
package org.safs.staf.service.map;

import org.safs.staf.embedded.EmbeddedServiceHandle;
import org.safs.staf.embedded.ServiceInterface;
import org.safs.staf.service.InfoInterface;
import org.safs.staf.service.InfoInterface.RequestInfo;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFResult;

public class EmbeddedMapService extends AbstractSAFSAppMapService implements ServiceInterface {

	public EmbeddedMapService() { }

	protected void registerHandle(String handleId)throws STAFException{
		String debugmsg = getClass().getName() + ".registerHandle():";
    	//debugLog.debugPrintln(debugmsg+" registering EmbeddedServiceHandle: "+ handleId +" as service "+ servicename);
		client = new EmbeddedServiceHandle(handleId, servicename, this);
		((EmbeddedServiceHandle)client).register();
	}
	
	/**
	 * Intercepts initializing the instance of the service to get servicename information.
	 * <p>
	 * This service is registered under process name 
	 * "STAF/Service/&lt;ServiceName>".
	 * <p>
	 */
	public STAFResult init(InfoInterface.InitInfo initInfo)
	{
		String debugmsg = getClass().getName() + ".init():";
		try {
			servicename = initInfo.name;
			serviceparms = initInfo.parms;
			registerHandle("STAF/Service/" + servicename);
		} catch (STAFException e) {
			//debugLog.debugTerm();
			return new STAFResult(STAFResult.STAFRegistrationError);
		}
		int code = doInit(client, servicename, serviceparms);
		return new STAFResult(code);		
	}
	
	/* (non-Javadoc)
	 * @see org.safs.staf.embedded.ServiceInterface#acceptRequest(org.safs.staf.service.InfoInterface.RequestInfo)
	 */
	@Override
	public STAFResult acceptRequest(RequestInfo info) {
		return doAcceptRequest(info.machine, info.handleName, info.handle, info.request);
	}

	/**********************************************************************
	 * 	Handle the request to shutdown the service from STAF
	 **********************************************************************/
	public final STAFResult  term(){
		// Carl Nagle -- removed "clients" code to increment/decrement "clients"
		// This may have to be reinserted.  I'm not sure why it was present.
		try{client.unRegister();client = null;}
		catch(STAFException ex){;}
		return new STAFResult(0);
	}

	@Override
	public STAFResult terminateService() {
		return term();
	}	

	
}
