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
package org.safs.staf.embedded;

import org.safs.SAFSException;
import org.safs.STAFHelper;
import org.safs.staf.service.InfoInterface;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFHandle;
import com.ibm.staf.STAFResult;

/**
 * A handle subclass specifically used by Services, rather than clients of services.
 * @author Carl Nagle
 * @see EmbeddedHandle
 */
public class EmbeddedServiceHandle extends EmbeddedHandle implements ServiceInterface {

	protected String serviceId;
	protected ServiceInterface service;
	
	/**
	 * Create and Register a new Service Handle.
	 * @param handleId of the service--which is different than the service name.
	 * @param serviceId -- the service name for the service using the Handle.
	 * @param service the Service using the Handle.
	 * @throws STAFException if the arguments are invalid or already registered.
	 */
	public EmbeddedServiceHandle(String handleId, String serviceId, ServiceInterface service) throws STAFException {
		super(handleId);//this registers the handle but NOT the service
		setServiceInfo(handleId, serviceId, service);
	}
	
	public void setServiceInfo(String handleId, String serviceId, ServiceInterface service){
		super.setHandleId(handleId);
		this.serviceId = serviceId;
		this.service = service;
	}

	protected void registerHandle() throws STAFException {
		try{ 
			EmbeddedHandles.registerService(handleId, serviceId, this); 
			if( (!STAFHelper.no_staf_handles) && (stafHandle == null)) stafHandle = new STAFHandle(handleId);
		}
		catch(IllegalArgumentException x) {
			throw new STAFException(STAFResult.InvalidHandle, x.getMessage());}
		catch(SAFSException x){
			throw new STAFException(STAFResult.HandleAlreadyExists, x.getMessage());}
	}
	
	
	/**
	 * Accept a request sent via another handler's submit calls to the service using this Handle.
	 * Route the request to the underlying service and return the response.
	 * @return the response from the service handling the request.
	 */
	@Override
	public STAFResult acceptRequest(InfoInterface.RequestInfo info) {
		try{ register();}
		catch(STAFException x){
			return new STAFResult(STAFResult.STAFRegistrationError, "Registration Error");
		}
		return service.acceptRequest(info);
	}

	/** 
	 * Calls terminateService on the embedded service.
	 * This does NOT unregister the service.
	 */
	@Override
	public STAFResult terminateService() {
		return service.terminateService();
	}
}
