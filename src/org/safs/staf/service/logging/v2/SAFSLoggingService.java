package org.safs.staf.service.logging.v2;

import org.safs.staf.service.InfoInterface;
import org.safs.staf.service.logging.AbstractSAFSLoggingService;
import org.safs.staf.service.logging.AbstractSTAFTextLogItem;
import org.safs.staf.service.logging.AbstractSTAFXmlLogItem;

import com.ibm.staf.STAFResult;
import com.ibm.staf.service.STAFServiceInterfaceLevel3;

/**
 * This class is Logging service of STAF version 2.
 * All detail are written in AbstractSAFSLoggingService.
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
 * @see AbstractSAFSLoggingService
 * 
 * @since	MAY 19 2009		(LW)	Moved from package org.safs.staf.service.logging and
 * 									remove the original one,
 * 									Realize the method getSTAFTextLogItem() and getSTAFXmlLogItem()
 */

public class SAFSLoggingService extends AbstractSAFSLoggingService implements STAFServiceInterfaceLevel3
{

	public SAFSLoggingService() {}

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
	 * All log facilities are forced to close.
	 * <p>
	 */
	public final int term()
	{
		STAFResult result = super.terminate();
		return result.rc;
	}

	protected AbstractSTAFTextLogItem getSTAFTextLogItem(String name,String directory, String filename) {
		return new STAFTextLogItem(name,directory,filename);
	}

	protected AbstractSTAFXmlLogItem getSTAFXmlLogItem(String name,String directory, String filename) {
		return new STAFXmlLogItem(name,directory,filename);
	}


}