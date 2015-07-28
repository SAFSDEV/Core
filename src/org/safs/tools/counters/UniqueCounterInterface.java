package org.safs.tools.counters;

import org.safs.tools.UniqueIDInterface;

public interface UniqueCounterInterface extends UniqueIDInterface {

	/** 
	 * Retrieve the current executing test level.
	 * This is used by counters to compare against their stored counting mode.
	 * Some counters are set to only count on the STEP test level.
	 */
	public String getTestLevel();

}

