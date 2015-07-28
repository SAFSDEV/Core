package org.safs.staf.service.logging.v3;

import org.safs.staf.service.InfoInterface;
import org.safs.staf.service.logging.AbstractSAFSLoggingService;
import org.safs.staf.service.logging.AbstractSTAFTextLogItem;
import org.safs.staf.service.logging.AbstractSTAFXmlLogItem;

import com.ibm.staf.STAFResult;
import com.ibm.staf.service.STAFServiceInterfaceLevel30;

/**
 * This class is Logging service of STAF version 3.
 * All detail are written in AbstractSAFSLoggingService.
 * 
 * Here we need only realize the methods defined in STAFServiceInterfaceLevel30
 * init(STAFServiceInterfaceLevel30.InitInfo)
 * acceptRequest(STAFServiceInterfaceLevel30.RequestInfo)
 * term()
 * To realize these methods, we need to convert the InitInfo and RequestInfo of 
 * STAFServiceInterfaceLevel30 to that of InfoInterface, and then call the method
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

public class SAFSLoggingService3 extends AbstractSAFSLoggingService implements STAFServiceInterfaceLevel30
{
	
	public SAFSLoggingService3() {}
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
		InfoInterface.RequestInfo info = new InfoInterface.RequestInfo(requestInfo.request);
	
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

	protected AbstractSTAFTextLogItem getSTAFTextLogItem(String name,String directory, String filename) {
		return new STAFTextLogItem3(name,directory,filename);
	}
	
	protected AbstractSTAFXmlLogItem getSTAFXmlLogItem(String name, String directory, String filename) {
		return new STAFXmlLogItem3(name,directory,filename);
	}
}