package org.safs.tools.status;

public interface TestRecordInterface {

	/** CYCLE, SUITE, or STEP. **/
	public String getTestLevel ();
	
	/** The unique ID of the input source for this test level. **/
	public String getInputUID  ();

	/** 
	 * The name of the input source. 
	 * This might be a filename or some other type of String.
	 * Note, the same named file or item can be "opened" multiple times 
	 * with unique IDs for each instance. **/
	public String getInputName  ();

	/** Field separator String for the input records from this source.**/
	public String getInputSeparator ();	
	
	/** Current input record. **/
	public String getInputRecord ();
	
	/** Current input line number (if appropriate).**/
	public long getInputLineNumber ();
	
	/** Current App Map ID in use.**/
	public String getAppMapUID();
	
	/** Current active log for this test level.**/
	public String getLogUID();
	
	/** Current stored status code for this test level. **/
	public long getStatusCode();
}

