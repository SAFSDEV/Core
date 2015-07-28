package org.safs.tools.status;
public interface StatusCounterInterface {
	
	/**	Add the provided counter to this counter. */
	public void addStatus(StatusInterface counter);
	public void setSuspended(boolean suspend);
}

