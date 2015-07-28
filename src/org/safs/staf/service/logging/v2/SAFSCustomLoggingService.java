package org.safs.staf.service.logging.v2;

import org.safs.staf.service.InfoInterface;
import org.safs.staf.service.logging.AbstractSAFSCustomLoggingService;

import com.ibm.staf.STAFResult;
import com.ibm.staf.service.STAFServiceInterfaceLevel3;

/**
 * This class is custom Logging service of STAF version 2.
 * All detail are written in AbstractSAFSCustomLoggingService.
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
 * @see AbstractSAFSCustomLoggingService
 */

public class SAFSCustomLoggingService extends AbstractSAFSCustomLoggingService implements STAFServiceInterfaceLevel3
{

	public SAFSCustomLoggingService() {}

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
		InfoInterface.RequestInfo info = new InfoInterface.RequestInfo(requestInfo.request);
		
		return super.acceptRequest(info);
	}

	/**
	 * Handles removing this service from STAF.
	 * <p>
	 */
	public final int term()
	{
		STAFResult result = super.terminate();
		return result.rc;
	}
}