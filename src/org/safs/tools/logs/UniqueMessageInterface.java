package org.safs.tools.logs;

import org.safs.tools.UniqueIDInterface;

public interface UniqueMessageInterface extends UniqueIDInterface {

	/** The message to log.**/
	public String getLogMessage();

	/** Optional. Additional details associated with the message.  In many logs, this
	 * appears as an additional descriptive line in log output.
	 * This may be null or an empty string, and should be ignored if it is such.**/
	public String getLogMessageDescription();

	/** The type of the message to log.
	 * For example, a GENERIC message, a PASSED message, a FAILED message, etc..
	 * This may be implementation specific.**/
	public int   getLogMessageType();
}

