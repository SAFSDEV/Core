package org.safs.tools.logs;

import org.safs.tools.UniqueIDInterface;
import org.safs.logging.AbstractLogFacility;

/** 
 * Possible values for logLevels are defined in AbstractLogFacility.
 * @see AbstractLogFacility
 **/
public interface UniqueLogLevelInterface extends UniqueIDInterface {
	
	/** Return the stored log level setting.*/
	public String getLogLevel();
}
