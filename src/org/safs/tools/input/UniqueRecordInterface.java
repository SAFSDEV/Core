package org.safs.tools.input;

import org.safs.tools.UniqueIDInterface;

public interface UniqueRecordInterface extends UniqueIDInterface {
	
	/** Retrieve the field separator used for this record. */
	public String getSeparator();
	
}

