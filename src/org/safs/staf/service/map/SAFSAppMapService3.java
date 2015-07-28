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

