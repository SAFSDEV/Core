package org.safs.staf.service.input;

import org.safs.staf.service.InfoInterface;

import com.ibm.staf.STAFResult;
import com.ibm.staf.service.STAFServiceInterfaceLevel3;

/**
 * This class is Input service of STAF version 2.
 * All detail are written in AbstractSAFSInputService.
 * 
 * Here we need only realize the methods defined in STAFServiceInterfaceLevel3
 * init(STAFServiceInterfaceLevel3.InitInfo)
 * acceptRequest(STAFServiceInterfaceLevel3.RequestInfo)
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

public class SAFSInputService extends AbstractSAFSInputService implements STAFServiceInterfaceLevel3
{
	public SAFSInputService() {}
	
	/**
	 * Handles initializing this instance of the service for STAF.
	 * <p>
	 * This service is registered under process name 
	 * "STAF/Service/&lt;ServiceName>".
	 * <p>
	 */
	public final int init(STAFServiceInterfaceLevel3.InitInfo initInfo)
	{
		InfoInterface.InitInfo info = new InfoInterface.InitInfo(initInfo.name,initInfo.parms);
		STAFResult result = super.init(info);
		return result.rc;
	}
	
	/**
	 * Handles service request from STAF.
	 * <p>
	 */
	public STAFResult acceptRequest(STAFServiceInterfaceLevel3.RequestInfo requestInfo)
	{
		//In STAF 3, the processName is replaced by handleName
		InfoInterface.RequestInfo info = new InfoInterface.RequestInfo(requestInfo.machine,
																	   requestInfo.handle,
																	   requestInfo.processName,
																	   requestInfo.request);
		return super.acceptRequest(info);
	}

	/**
	 * Handles removing this service from STAF.
	 * <p>
	 * All log facilities are forced to close.
	 * <p>
	 */
	public final int term()
	{
		STAFResult result = super.terminate();
		return result.rc;
	}
}





















