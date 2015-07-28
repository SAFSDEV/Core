package org.safs.tools.status;

/**
 * Provides access to the current status--usually a snapshot--of a status object.
 */
public interface StatusInterface {

	/** CYCLE, SUITE, STEP, or a Unique ID. **/
	public String getTestLevel ();
	
	public long getTotalRecords ();
	
	public long getTestFailures ();
	public long getTestWarnings ();
	public long getTestPasses   ();
	
	public long getGeneralFailures ();
	public long getGeneralWarnings ();
	public long getGeneralPasses   ();

	public long getIOFailures     ();
	public long getSkippedRecords ();
	public boolean isSuspended();
}

