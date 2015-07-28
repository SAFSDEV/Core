package org.safs.tools.status;

public interface StatusInfoInterface {

	/** Get a wrapped StatusInterface. **/	
	public StatusInterface getStatusInterface ();

	/** Get a wrapped TestRecordInterface. **/	
	public TestRecordInterface getTestRecordInterface ();
}

