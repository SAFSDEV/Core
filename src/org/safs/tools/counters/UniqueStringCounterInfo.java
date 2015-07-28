package org.safs.tools.counters;

import org.safs.tools.UniqueStringID;

public class UniqueStringCounterInfo
	extends UniqueStringID
	implements UniqueCounterInterface {

	protected String testlevel = null;
	
	/**
	 * Constructor for UniqueStringCounterInfo
	 */
	public UniqueStringCounterInfo(String id, String testlevel) {
		super(id);
		this.testlevel = testlevel;
	}

	/**
	 * @see UniqueCounterInterface#getTestLevel()
	 */
	public String getTestLevel() {
		return testlevel;
	}
}

