package org.safs.tools.logs;

import org.safs.tools.UniqueIDInterface;

public interface UniqueLogInterface extends UniqueLogLevelInterface {

	/** Use an alternate name for an enabled text log.
	 * Normally, the log will inherit the name given as the UniqueID. **/
	public String getTextLogName();

	/** Use an alternate name for an enabled XML log.
	 * Normally, the log will inherit the name given as the UniqueID. **/
	public String getXMLLogName();

	/**
	 * Get the log modes enabled for this log.
	 * These are normally values OR'd together to indicate which logs are enabled.
	 * These values will be implementation specific. **/
	public long getLogModes();
	
	/**
	 * Get the unique ID of any log linked or chained with this one.
	 */
	public UniqueIDInterface getLinkedFac();
}

