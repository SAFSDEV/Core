package org.safs.staf.service.input;

import org.safs.staf.service.InfoInterface;

import com.ibm.staf.STAFResult;
import com.ibm.staf.service.STAFServiceInterfaceLevel30;

/**
 * This class is Input service of STAF version 3.
 * All detail are written in AbstractSAFSInputService.
 * 
 * Here we need only realize the methods defined in STAFServiceInterfaceLevel30
 * init(STAFServiceInterfaceLevel30.InitInfo)
 * acceptRequest(STAFServiceInterfaceLevel30.RequestInfo)
 * term()
 * To realize these methods, we need to convert the InitInfo and RequestInfo of 
 * STAFServiceInterfaceLevel3 to that of InfoInterface, and then call the method
 * defined in the super class AbstractSAFSLoggingService
 * init(InfoInterface.InitInfo)
 * acceptRequest(InfoInterface.RequestInfo)
 * terminate()
 * 
 * @see AbstractSAFSInputService
 */

public class SAFSInputService3 extends AbstractSAFSInputService implements STAFServiceInterfaceLevel30
{
	
	public SAFSInputService3() {}
	/**
	 * Handles initializing this instance of the service for STAF.
	 * <p>
	 * This service is registered under process name 
	 * "STAF/Service/&lt;ServiceName>".
	 * <p>
	 */
	public STAFResult init(STAFServiceInterfaceLevel30.InitInfo initInfo) {
		InfoInterface.InitInfo info = new InfoInterface.InitInfo(initInfo.name,initInfo.parms);
		return super.init(info);
	}
	
	/**
	 * Handles service request from STAF.
	 * <p>
	 */
	public STAFResult acceptRequest(STAFServiceInterfaceLevel30.RequestInfo requestInfo)
	{
		InfoInterface.RequestInfo info = new InfoInterface.RequestInfo(requestInfo.machine,
																	   requestInfo.handle,
																	   requestInfo.handleName,
																	   requestInfo.request);
	
		return super.acceptRequest(info);
	}

	/**
	 * Handles removing this service from STAF.
	 * <p>
	 * All log facilities are forced to close.
	 * <p>
	 */
	public final STAFResult term()
	{
		// close all log facilities
		return super.terminate();
	}
}