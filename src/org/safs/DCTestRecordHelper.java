package org.safs;

/**
 * Concrete subclass of TestRecordHelper that provides no additional functionality.  
 * This is primarily intended to be used by processors or engines that provide 
 * features and functions that do no act on GUI components--for example, many 
 * non-component Driver Commands.
 */
public class DCTestRecordHelper extends TestRecordHelper {

	/**
	 * Constructor for DCTestRecordHelper
	 */
	public DCTestRecordHelper() {
		super();
	}

	/**
	 * @see TestRecordData#getCompInstancePath()
	 */
	public String getCompInstancePath() {
		return "org.safs.";
	}

}

