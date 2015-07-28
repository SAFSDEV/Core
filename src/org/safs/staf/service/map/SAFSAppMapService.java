package org.safs.staf.service.map;

import org.safs.staf.STAFHandleInterface;
import org.safs.staf.embedded.HandleInterface;

import com.ibm.staf.*;
import com.ibm.staf.service.*;

/**
 * This SAFSAppMapService class is an external STAF service run by the JSTAF Service Proxy.
 * It extends AbstractSAFSAppMapService for running on STAF V2.
 *
 * @author JunwuMa
 * @since   MAY 12, 2009
 *
 *   <br>   MAY 12, 2009    (JunwuMa) 	Original Release
 *   
 * @see SAFSAppMapService3 
 */

public class SAFSAppMapService extends AbstractSAFSAppMapService implements STAFServiceInterfaceLevel1 {

	protected static int        clients;
	protected static HandleInterface singletonClient = null;
	
	/**********************************************************************
	 * 	Initialize the class, primarily, the parser used to parse service requests.
	 **********************************************************************/
	public SAFSAppMapService () {
	}


	// p/o STAFServiceInterfaceLevel1
	/**********************************************************************
	 * Handle initializing this instance of the service for STAF
	 **********************************************************************/
	public final int init (String name, String params){
		if (singletonClient == null){
			try{ 
				singletonClient = new STAFHandleInterface(SAM_SERVICE_PROCESS_NAME);
				clients = 1;
			}
			catch(STAFException ex){ System.err.println(ex.rc +":"+ ex.getMessage());}
		}else{
			clients++;
		}		
		return doInit(singletonClient, name, params);
	}

	
	// p/o STAFServiceInterfaceLevel1
	/**********************************************************************
	 * Handle service request from STAF
	 **********************************************************************/
	public final STAFResult acceptRequest(String machine, String process, int handle, String request) {
		return doAcceptRequest(machine, process, handle, request);
	}


	// p/o Interface STAFServiceInterfaceLevel1
	/**********************************************************************
	 * 	Handle the request to shutdown the service from STAF
	 **********************************************************************/
	public final int term(){ 
		doTerm();
		if(clients > 0) clients--;
		if(clients == 0) {
			try{singletonClient.unRegister();singletonClient = null;}
			catch(STAFException ex){;}
		}
		return 0;		
	}
	
}

