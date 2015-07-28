package org.safs.staf.embedded;

import org.safs.staf.service.InfoInterface;

import com.ibm.staf.STAFResult;

public interface ServiceInterface{

	public STAFResult acceptRequest(InfoInterface.RequestInfo info );
	public STAFResult terminateService();
}
