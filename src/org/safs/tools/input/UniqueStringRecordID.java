package org.safs.tools.input;

import org.safs.tools.UniqueStringID;

public class UniqueStringRecordID
	extends UniqueStringRecordClass implements UniqueRecordIDInterface{
	protected String recordid = null;
	
	/**
	 * PREFERRED Constructor for UniqueStringRecordID
	 */
	public UniqueStringRecordID(String id, String separator, String recordid) {
		super(id, separator);
		this.recordid = recordid;
	}

	/**
	 * @see UniqueRecordIDInterface#getRecordID()
	 */
	public Object getRecordID() {
		return recordid;
	}

}