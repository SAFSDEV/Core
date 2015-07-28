package org.safs.tools.input;

import org.safs.tools.UniqueStringID;

public class UniqueStringRecordNum
	extends UniqueStringRecordClass
	implements UniqueRecordNumInterface {

	protected long recordnum = -1;
	
	/**
	 * PREFERRED Constructor for UniqueStringRecordNum
	 */
	public UniqueStringRecordNum(String id, String separator, long recordnum) {
		super(id, separator);
		this.recordnum = recordnum;
	}

	/**
	 * @see UniqueRecordNumInterface#getRecordNum()
	 */
	public long getRecordNum() {
		return recordnum;
	}

}

