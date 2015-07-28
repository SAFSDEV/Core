package org.safs.tools.input;

public interface UniqueRecordIDInterface extends UniqueRecordInterface {

	/** 
	 * ID of the record/line.  Like a String BLOCKID.
	 * This will be implementation specific.**/
	public Object getRecordID ();
}

