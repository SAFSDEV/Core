package org.safs.staf.service.input;

import org.safs.SAFSException;
import org.safs.staf.embedded.EmbeddedHandles;
import org.safs.staf.embedded.EmbeddedServiceHandle;
import org.safs.staf.embedded.ServiceInterface;
import org.safs.staf.service.InfoInterface;
import org.safs.tools.vars.VarsInterface;

import com.ibm.staf.STAFException;
import com.ibm.staf.STAFHandle;
import com.ibm.staf.STAFResult;

/**
 * A subclass of SAFSINPUT service intending to run embedded in the same JVM/Process as the Driver using it.
 * That is, it does NOT run as a STAF service in a separate JVM. However, it can interface to STAF as needed.
 * <p>
 * For this to work, the InputInterface classes must be properly initialized to know to use 
 * this SAFSINPUT service instead of the standard STAF SAFSINPUT service.
 * <p>
 * @author canagl
 */
public class EmbeddedInputService extends AbstractSAFSInputService implements ServiceInterface {
	
	public EmbeddedInputService() {	}
	
	@Override
	protected void registerHandle(String handleId)throws STAFException{
		String debugmsg = getClass().getName() + ".registerHandle():";
    	debugLog.debugPrintln(debugmsg+" registering EmbeddedServiceHandle: "+ handleId +" as service "+ servicename);
		fHandle = new EmbeddedServiceHandle(handleId, servicename, this);
		((EmbeddedServiceHandle)fHandle).register();
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

	@Override
	public STAFResult terminateService() {
		return terminate();
	}
}
