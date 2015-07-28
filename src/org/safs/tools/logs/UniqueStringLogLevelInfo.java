package org.safs.tools.logs;

import org.safs.tools.UniqueStringID;

public class UniqueStringLogLevelInfo
	extends UniqueStringID
	implements UniqueLogLevelInterface {

	protected String loglevel = null;
	
	/**
	 * Constructor for UniqueStringLogLevelInfo
	 */
	public UniqueStringLogLevelInfo() {
		super();
	}

	/**
	 * Constructor for UniqueStringLogLevelInfo UniqueStringID
	 */
	public UniqueStringLogLevelInfo(String id) {
		super(id);
	}

	/**
	 * Constructor for UniqueStringLogLevelInfo
	 */
	public UniqueStringLogLevelInfo(String id, String loglevel) {
		this(id);
		setLogLevel(loglevel);
	}

	/**
	 * Set the log level to that provided.
	 */
	public void setLogLevel(String loglevel) {
		this.loglevel = loglevel;
	}

	/**
	 * @see UniqueLogLevelInterface#getLogLevel()
	 */
	public String getLogLevel() {
		return loglevel;
	}

}

