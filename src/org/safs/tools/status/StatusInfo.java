package org.safs.tools.status;
public class StatusInfo implements StatusInterface {

	protected boolean suspended = false;
	protected String testlevel = null;

	protected long totalrecords = 0;
	
	protected long skippedrecords = 0;
	protected long iofailures = 0;
	
	protected long testfailures = 0;
	protected long testwarnings = 0;
	protected long testpasses = 0;

	protected long generalfailures = 0;
	protected long generalwarnings = 0;
	protected long generalpasses = 0;
	
	/**
	 * Constructor for StatusInfo to produce a new all-zeros set of counts.
	 * The TestLevel must be set with a call to setTestLevel after using this 
	 * constructor.
	 */
	public StatusInfo() {
		super();
	}

	/** @return true if status counts are suspended. */
	public boolean isSuspended(){ return suspended; }
	/**
	 * Constructor for StatusInfo to pre-initilize non-zero counts in a new instance.
	 */
	public StatusInfo(String testlevel, 
	                  long totalrecords, long skippedrecords, long iofailures,
					  long testfailures, long testwarnings, long testpasses,
					  long generalfailures, long generalwarnings, long generalpasses,
					  boolean suspended) {
		this();
		setTestLevel(testlevel);
		this.totalrecords=totalrecords;
		this.skippedrecords=skippedrecords;
		this.iofailures=iofailures;

		this.testfailures=testfailures;
		this.testwarnings=testwarnings;
		this.testpasses=testpasses;

		this.generalfailures=generalfailures;
		this.generalwarnings=generalwarnings;
		this.generalpasses=generalpasses;
		this.suspended=suspended;
	}

	/**
	 * Set the Test Level for this instance.
	 * 'CYCLE', 'SUITE', 'STEP', or another implementation-specified String.
	 * Normally only set once after a call to the no-arg constructor.
	 */
	public void setTestLevel(String testlevel) {
		this.testlevel = testlevel;
	}

	/**
	 * @see StatusInterface#getTestLevel()
	 */
	public String getTestLevel() {
		return testlevel;
	}

	/**
	 * @see StatusInterface#getTotalRecords()
	 */
	public long getTotalRecords() {
		return totalrecords;
	}

	/**
	 * @see StatusInterface#getTestFailures()
	 */
	public long getTestFailures() {
		return testfailures;
	}

	/**
	 * @see StatusInterface#getTestWarnings()
	 */
	public long getTestWarnings() {
		return testwarnings;
	}

	/**
	 * @see StatusInterface#getTestPasses()
	 */
	public long getTestPasses() {
		return testpasses;
	}

	/**
	 * @see StatusInterface#getGeneralFailures()
	 */
	public long getGeneralFailures() {
		return generalfailures;
	}

	/**
	 * @see StatusInterface#getGeneralWarnings()
	 */
	public long getGeneralWarnings() {
		return generalwarnings;
	}

	/**
	 * @see StatusInterface#getGeneralPasses()
	 */
	public long getGeneralPasses() {
		return generalpasses;
	}

	/**
	 * @see StatusInterface#getIOFailures()
	 */
	public long getIOFailures() {
		return iofailures;
	}

	/**
	 * @see StatusInterface#getSkippedRecords()
	 */
	public long getSkippedRecords() {
		return skippedrecords;
	}

}

