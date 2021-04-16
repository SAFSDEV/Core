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
package org.safs.staf.service.var;

import com.ibm.staf.*;
import com.ibm.staf.service.*;

import java.util.*;

import org.safs.staf.STAFHandleInterface;
import org.safs.staf.embedded.HandleInterface;

/**
 * This SAFSVariableService3 class is an external STAF service run by the JSTAF Service Proxy.
 * It extends AbstractSAFSVariableService for running on STAF V3.
 *
 * @author JunwuMa
 * @since   MAY 12, 2009
 *
 *   <br>   MAY 12, 2009    (JunwuMa) 	Original Release
 *   <br>   JUN 04, 2009    (Carl Nagle) 	Improving Thread Safety.
 *   
 * @see SAFSVariableService  
 */

public class SAFSVariableService3 extends AbstractSAFSVariableService implements STAFServiceInterfaceLevel30 {

	public static final String SVS3_RESERVED_VARS_KEY = "staf-map-class-name";

	protected static int        clients;
	protected static HandleInterface singletonClient = null;
	
	/**********************************************************************
	 * 	Initialize the class, primarily, the parser used to parse service requests.
	 **********************************************************************/
	public SAFSVariableService3 () {
	}

	// p/o STAFServiceInterfaceLevel30
	/**********************************************************************
	 * Handle initializing this instance of the service for STAF
	 **********************************************************************/
	public final STAFResult init (STAFServiceInterfaceLevel30.InitInfo info){
		if (singletonClient == null){
			try{
				singletonClient = new STAFHandleInterface(SVS_SERVICE_PROCESS_NAME);
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
		return doAcceptRequest(info.request);
	}


	// p/o Interface STAFServiceInterfaceLevel30
	/**********************************************************************
	 * 	Handle the request to shutdown the service from STAF
	 **********************************************************************/
	public final STAFResult  term(){
		// must unregister ourselves as a STAF client
		if(clients > 0) clients--;
		if(clients == 0) {
			try{singletonClient.unRegister();singletonClient = null;}
			catch(STAFException ex){;}
		}
		return new STAFResult(0);
	}

	protected String stafGetCommmd(String varname) { 
		return "GET handle "+singletonClient.getHandle() +" var "+ varname;
	} 	
	
	protected String stafSetCommmd(String assignExp) { 
		return "SET handle "+singletonClient.getHandle() +" var "+ assignExp;
	} 
	
	protected String stafDeleteCommand(String varname) { 
		return "DELETE handle "+singletonClient.getHandle() +" var "+ varname; 
	} 	
	
	protected String stafListCommand() {
		return "handle "+ singletonClient.getHandle() +" LIST";
	}	
	
	protected STAFResult handleCount() {
		STAFResult result = singletonClient.submit2("local", "var", stafListCommand());

		if (result.rc == STAFResult.Ok){
            STAFMarshallingContext mc = STAFMarshallingContext.unmarshall(result.result);
            HashMap resultMap = (HashMap)mc.getRootObject();			
			int size = resultMap.size()-1; // remove entry "staf-map-class-name" that is a reserved variable.
			result.result = String.valueOf(size);
		}
		return result;
	}	
	protected STAFResult handleReset() {
		
		STAFResult result = singletonClient.submit2("local", "var", stafListCommand());

		if (result.rc == STAFResult.Ok) {
            STAFMarshallingContext mc = STAFMarshallingContext.unmarshall(result.result);
            HashMap resultMap = (HashMap)mc.getRootObject();
            Iterator iter = resultMap.entrySet().iterator();
            String delete = "DELETE handle "+singletonClient.getHandle();
            while (iter.hasNext()){
            	java.util.Map.Entry entry = (java.util.Map.Entry) iter.next();
                String varname = (String)entry.getKey();
                if (!varname.equalsIgnoreCase(SVS3_RESERVED_VARS_KEY)) // reserved variable can't be deleted 
                	delete += " var " + varname;
            }  
            result = singletonClient.submit2("local", "var", delete);
		} 
		return result;
	}

	/**
	 * Return the STAF VAR LIST STAFResult.result in marshalled V3 format UNLESS the 
	 * V2 command option was given.  
	 * If so, return the data in the original V2 format of key=value\n\r for each item.
	 */
	protected STAFResult handleList(STAFCommandParseResult parsedData) {
		boolean convertV2 = parsedData.optionTimes(SVS_SERVICE_REQUEST_V2) > 0;
		STAFResult result = singletonClient.submit2("local", "var", stafListCommand());
		if (result.rc == STAFResult.Ok && convertV2) {
            STAFMarshallingContext mc = STAFMarshallingContext.unmarshall(result.result);
            HashMap resultMap = (HashMap)mc.getRootObject();
            Iterator iter = resultMap.entrySet().iterator();
            String varname;
            String varval;
            result.result = "";
            while (iter.hasNext()){
            	java.util.Map.Entry entry = (java.util.Map.Entry) iter.next();
                varname = (String)entry.getKey();
                varval = (String)entry.getValue();
                result.result += varname +"="+varval +"\n\r";
            }  
		} 
		return result;
	}
}

