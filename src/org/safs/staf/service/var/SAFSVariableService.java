package org.safs.staf.service.var;

import com.ibm.staf.*;
import com.ibm.staf.service.*;

import java.util.*;

import org.safs.Log;
import org.safs.staf.STAFHandleInterface;
import org.safs.staf.embedded.HandleInterface;

/**
 * This SAFSVariableService class is an external STAF service run by the JSTAF Service Proxy.
 * It extends AbstractSAFSVariableService for running on STAF V2.
 *
 * @author JunwuMa
 * @since   MAY 12, 2009
 *
 *   <br>   MAY 12, 2009    (JunwuMa) 	Original Release
 *   <br>   JUN 04, 2009    (Carl Nagle) 	Improving Thread Safety.
 *   
 * @see SAFSVariableService3  
 */

public class SAFSVariableService extends AbstractSAFSVariableService implements STAFServiceInterfaceLevel1 {

	protected static int        clients;
	protected static HandleInterface singletonClient = null;
	
	/**********************************************************************
	 * 	Initialize the class, primarily, the parser used to parse service requests.
	 **********************************************************************/
	public SAFSVariableService () {
	}
	
	// p/o STAFServiceInterfaceLevel1
	/**********************************************************************
	 * Handle initializing this instance of the service for STAF
	 **********************************************************************/
	public final int init (String name, String params){
		if (singletonClient == null){
			try{
				singletonClient = new STAFHandleInterface(SVS_SERVICE_PROCESS_NAME);
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
		return doAcceptRequest(request);
	}


	// p/o Interface STAFServiceInterfaceLevel1
	/**********************************************************************
	 * 	Handle the request to shutdown the service from STAF
	 **********************************************************************/
	public final int term(){
		// must unregister ourselves as a STAF client
		if(clients > 0) clients--;
		if(clients == 0) {
			try{singletonClient.unRegister();singletonClient = null;}
			catch(STAFException ex){;}
		}
		return 0;		
	}

	protected String stafGetCommmd(String varname) { 
		return "handle "+singletonClient.getHandle() +" GET "+ varname;
	} 	
	
	protected String stafSetCommmd(String assignExp) { 
		return "handle "+singletonClient.getHandle( ) +" SET "+ assignExp;
	} 
	
	protected String stafDeleteCommand(String varname) { 
		return "handle "+singletonClient.getHandle() +" DELETE "+ varname; 
	} 
	
	protected String stafListCommand() {
		return "onlyhandle "+ singletonClient.getHandle() +" LIST";
	}
	
	// handle command COUNT for SAFSVARS
	protected STAFResult handleCount() {
		STAFResult result = singletonClient.submit2("local", "var", stafListCommand());
	
		if (result.rc == STAFResult.Ok){
			if (result.result.length() > 0){
				StringTokenizer counter = new StringTokenizer(result.result, "\n\r");
				result.result = String.valueOf(counter.countTokens());
			}
			else{
				result.result = "0";
			}
		}
		return result;
	}
	
	// handle command RESET for SAFSVARS
	protected STAFResult handleReset() {
		STAFResult result = singletonClient.submit2("local", "var", stafListCommand());

		//debug
		//return result;

		if (result.rc == STAFResult.Ok){
			if (result.result.length() > 0){
				StringTokenizer counter = new StringTokenizer(result.result, "\n\r");
				int count = counter.countTokens();
				String[] tokens = new String[count];
				for(int index = 0; index < count; index++){
					try{
						String item = counter.nextToken();
						int eqindex = item.indexOf(eq);
						if (eqindex > 0){
							tokens[index] = item.substring(0, eqindex);
						}else{
							tokens[index] = empty;
						}
					}catch(Exception ex){
						result.result = "RESET";
						result.rc = STAFResult.InvalidServiceResult;
					}
				}
				String delete = "handle "+ singletonClient.getHandle();
				for(int index = 0; index < count; index++){
					try{
						String itemlen = String.valueOf(tokens[index].length()).trim();
						delete += " DELETE "+c+ itemlen +c+ tokens[index];
					}catch(Exception ex){;}
				}
				result = singletonClient.submit2("local", "var", delete);
			}
			else{
				result.result = "";
			}
		}
		return result;
	}
	
	// handle command LIST for SAFSVARS
	protected STAFResult handleList(STAFCommandParseResult parsedData){
		return singletonClient.submit2("local", "var", stafListCommand());
	}
}

