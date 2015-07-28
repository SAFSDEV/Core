package org.safs.tools.counters;

import org.safs.tools.status.StatusCounter;

public class Counter extends StatusCounter implements CountStatusInterface{

	protected long mode = CountersInterface.ALL_STATUS_INFO_MODE;
	
	/**
	 * Constructor for Counter
	 */
	public Counter() {
		super();
	}

	/**
	 * Constructor for Counter
	 */
	public Counter(	    
		long totalrecords,
		long skippedrecords,
		long iofailures,
		long testfailures,
		long testwarnings,
		long testpasses,
		long generalfailures,
		long generalwarnings,
		long generalpasses) {
		super(
			null,
			totalrecords,
			skippedrecords,
			iofailures,
			testfailures,
			testwarnings,
			testpasses,
			generalfailures,
			generalwarnings,
			generalpasses,
			false);
	}
	
	public void suspend(){ suspended = true;}
	public void resume(){ suspended = false;}

	public void setMode(long mode){ this.mode = mode; }
	public long getMode(){ return mode; }
}

