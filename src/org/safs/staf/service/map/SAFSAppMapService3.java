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

import com.ibm.staf.*;
import com.ibm.staf.service.*;
import java.util.*;

import org.safs.staf.STAFHandleInterface;
import org.safs.staf.embedded.HandleInterface;

/**
 * This SAFSAppMapService3 class is an external STAF service run by the JSTAF Service Proxy.
 * It extends AbstractSAFSAppMapService for running on STAF V3.
 *
 * @author JunwuMa
 * @since   MAY 12, 2009
 *
 *   <br>   MAY 12, 2009    (JunwuMa) 	Original Release
 *   
 * @see SAFSAppMapService 
 */

public class SAFSAppMapService3 extends AbstractSAFSAppMapService implements STAFServiceInterfaceLevel30 {

	protected static int        clients;
	protected static HandleInterface singletonClient = null;

	/**********************************************************************
	 * 	Initialize the class, primarily, the parser used to parse service requests.
	 **********************************************************************/
	public SAFSAppMapService3() {
	}

	// p/o STAFServiceInterfaceLevel30
	/**********************************************************************
	 * Handle initializing this instance of the service for STAF
	 **********************************************************************/
//	public final int init (String name, String params){
	public final STAFResult init (STAFServiceInterfaceLevel30.InitInfo info){
		if (singletonClient == null){
			try{ 
				singletonClient = new STAFHandleInterface(SAM_SERVICE_PROCESS_NAME);
				clients = 1;
			}
			catch(STAFException ex){ System.err.println(ex.rc +":"+ ex.getMessage());}
		}else{
			clients++;
		}	
		
		int code = doInit(singletonClient, info.name, info.parms);
		return new STAFResult(code);
	}
	
	// p/o STAFServiceInterfaceLevel30
	/**********************************************************************
	 * Handle service request from STAF
	 **********************************************************************/
	public final STAFResult acceptRequest(STAFServiceInterfaceLevel30.RequestInfo info) {
		return doAcceptRequest(info.machine, info.handleName, info.handle, info.request);
	}


	// p/o Interface STAFServiceInterfaceLevel30
	/**********************************************************************
	 * 	Handle the request to shutdown the service from STAF
	 **********************************************************************/
	public final STAFResult  term(){
		doTerm();
		if(clients > 0) clients--;
		if(clients == 0) {
			try{singletonClient.unRegister();singletonClient = null;}
			catch(STAFException ex){;}
		}
		return new STAFResult(0);
	}

}

