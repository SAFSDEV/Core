package org.safs.tools.counters;

import org.safs.tools.UniqueIDInterface;
import org.safs.tools.status.StatusInterface;

public interface CountersInterface {

	/** 1L */
	public static final long ALL_STATUS_INFO_MODE = 1;

	/** 2L */
	public static final long STEP_TESTS_ONLY_MODE = 2;

	
    /** '0L' status type for incrementing counters.**/
	public static final long STATUS_TEST_PASS = 0;
	
    /** '1L' status type for incrementing counters.**/
	public static final long STATUS_TEST_WARNING = 1;
	
    /** '2L' status type for incrementing counters.**/
	public static final long STATUS_TEST_FAILURE = 2;
	
    /** '3L' status type for incrementing counters.**/
	public static final long STATUS_GENERAL_PASS = 3;
	
    /** '4L' status type for incrementing counters.**/
	public static final long STATUS_GENERAL_WARNING = 4;
	
    /** '5L' status type for incrementing counters.**/
	public static final long STATUS_GENERAL_FAILURE = 5;

    /** '6L' status type for incrementing counters.**/
	public static final long STATUS_SKIPPED_RECORD = 6;
	
    /** '7L' status type for incrementing counters.**/
	public static final long STATUS_IO_FAILURE = 7;
	
    /** '8L' status type for incrementing counters.**/
	public static final long STATUS_TEST_IO_FAILURE = 8;
	
	
	/** Initialize/Start a new counter.**/
	public void initCounter (UniqueIDInterface counter);

	/** Get the status counts from the specified counter.**/
	public CountStatusInterface getStatus (UniqueIDInterface counter);

	/** Copy the from_counter counts to the specified to_counter.**/
	public void copyCounter (UniqueIDInterface from_counter, UniqueIDInterface to_counter);

	/** Copy the from_status counts to the specified to_counter.**/
	public void copyStatus (StatusInterface from_status, UniqueIDInterface to_counter);

	/** 
	 * Add the add_counter counts to the specified to_counter.
	 * The results are stored in the to_counter.**/
	public void sumCounters (UniqueIDInterface add_counter, UniqueIDInterface to_counter);

	/** 
	 * Add the add_status counts to the specified to_counter.
	 * The results are stored in the to_counter.**/
	public void sumStatus (StatusInterface add_status, UniqueIDInterface to_counter);

	/** 
	 * Substract the sub_counter counts from the specified from_counter.
	 * The results are stored in the from_counter.**/
	public void diffCounters (UniqueIDInterface sub_counter, UniqueIDInterface from_counter);

	/** 
	 * Substract the sub_status counts from the specified from_counter.
	 * The results are stored in the from_counter.**/
	public void diffStatus (StatusInterface sub_status, UniqueIDInterface from_counter);

	/** Clear all counts on the specified counter.**/
	public void clearCounter (UniqueIDInterface counter);

	/** Suspend counting on a specific counter.**/
	public void suspendCounter (UniqueIDInterface counter);

	/** 
	 * Suspends counting on ALL counters until counting is resumed with resumeAllCounting.
	 * Counters already suspended prior to this call will not resume counting due to a call 
	 * to resumeAllCounting.  You must use resumeCounter to resume counting for these.**/
	public void suspendAllCounting ();

	/** 
	 * Resume counting on a single counter suspended by suspendCounter.
	 * Counting will not resume if suspendAllCounters is still "active". In this case, 
	 * counting will resume once resumeAllCounting has been called.**/
	public void resumeCounter (UniqueIDInterface counter);

	/** 
	 * Resumes counting suspended by suspendAllCounting.
	 * Counters specifically suspended by suspendCounter are NOT resumed.  You must 
	 * use resumeCounter to resume counting for these.**/
	public void resumeAllCounting ();

	/** Increment the Total Records and the appropriate status count for ALL counters.**/
	public void incrementAllCounters (UniqueCounterInterface testlevel, long status);

	/** Increment the Total Records and Test Failures count for the counter.**/
	public long incrementTestFailures (UniqueCounterInterface counter);

	/** Increment the Total Records and Test Warnings count for the counter.**/
	public long incrementTestWarnings (UniqueCounterInterface counter);

	/** Increment the Total Records and Test Passes count for the counter.**/
	public long incrementTestPasses (UniqueCounterInterface counter);

	/** Increment the Total Records and General Failures count for the counter.**/
	public long incrementGeneralFailures (UniqueCounterInterface counter);

	/** Increment the Total Records and General Warnings count for the counter.**/
	public long incrementGeneralWarnings (UniqueCounterInterface counter);

	/** Increment the Total Records and General Passes count for the counter.**/
	public long incrementGeneralPasses (UniqueCounterInterface counter);

	/** Increment the Total Records and IO Failure Records count for the counter.**/
	public long incrementIOFailures (UniqueCounterInterface counter);

	/** Increment the Total Records, Test Failures, and IO Failure Records count for the counter.**/
	public long incrementTestIOFailures (UniqueCounterInterface counter);

	/** Increment the Total Records and Skipped Records count for the counter.**/
	public long incrementSkippedRecords (UniqueCounterInterface counter);

	/** Delete a counter out of storage.**/
	public void deleteCounter (UniqueIDInterface counter);
	
	/** Set a counter's mode.
	 * mode==1: count everything (default).
	 * mode==2: count only STEP level results.**/
	public void setCounterMode (UniqueIDInterface counter, long mode);

	/** Get a counter's mode.
	 * mode==1: count everything (default).
	 * mode==2: count only STEP level results.**/
	public long getCounterMode (UniqueIDInterface counter);
}

