package org.safs.tools.counters;

import org.safs.tools.status.StatusInterface;

/**
 * Provides access to the current status--usually a snapshot--of a status counter object.
 */
public interface CountStatusInterface extends StatusInterface {

	public long getMode();

}

