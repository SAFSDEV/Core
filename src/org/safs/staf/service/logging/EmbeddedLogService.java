package org.safs.staf.service.logging;

import org.safs.staf.embedded.EmbeddedServiceHandle;
import org.safs.staf.embedded.ServiceInterface;
import org.safs.staf.service.InfoInterface;
import org.safs.staf.service.InfoInterface.RequestInfo;
import org.safs.staf.service.logging.v3.STAFTextLogItem3;
import org.safs.staf.service.logging.v3.STAFXmlLogItem3;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFResult;

public class EmbeddedLogService extends AbstractSAFSLoggingService implements ServiceInterface {

	public EmbeddedLogService() { }

	@Override
	protected void registerHandle(String handleId)throws STAFException{
		String debugmsg = getClass().getName() + ".registerHandle():";
    	debugLog.debugPrintln(debugmsg+" registering EmbeddedServiceHandle: "+ handleId +" as service "+ servicename);
		handle = new EmbeddedServiceHandle(handleId, servicename, this);
		((EmbeddedServiceHandle)handle).register();
	}
	
	/**
	 * Intercepts initializing the instance of the service to get servicename information.
	 * <p>
	 * This service is registered under process name 
	 * "STAF/Service/&lt;ServiceName>".
	 * <p>
	 */
	public STAFResult init(InfoInterface.InitInfo initInfo)
	{
		String debugmsg = getClass().getName() + ".init():";
		this.servicename = initInfo.name;
    	debugLog.debugPrintln(debugmsg+" getting servicename: "+ servicename);
		return super.init(initInfo);
	}

	/**
	 * Bypass STAFLog service initialization.  We don't need it. 
	 */
	@Override
	protected STAFResult initSTAFLogService(){
		return new STAFResult(STAFResult.Ok);
	}
	
	/**
	 * Handles removing this service from STAF.
	 * <p>
	 * All log facilities are forced to close.
	 * <p>
	 */
	public final STAFResult term()
	{
		// close all log facilities and unregister
		return super.terminate();
	}

	protected AbstractSTAFTextLogItem getSTAFTextLogItem(String name,String directory, String filename) {
		return new EmbeddedTextLogItem(name,directory,filename);
	}
	
	protected AbstractSTAFXmlLogItem getSTAFXmlLogItem(String name, String directory, String filename) {
		return new EmbeddedXMLLogItem(name,directory,filename);
	}

	@Override
	public STAFResult terminateService() {		
		return term();
	}
}
