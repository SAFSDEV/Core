package org.safs.tools.status;

import org.safs.tools.counters.CountersInterface;
import org.safs.tools.vars.VarsInterface;

public interface StatusMonitorInterface {
	
	/** Get the current status info for the specified test level.**/
	public StatusInfoInterface getStatusInfo (String testLevel);

	/** Set/Overwrite the current status info for the specified test level.**/
	public void setStatusInfo (String testLevel, StatusInfoInterface statusInfo);
}

