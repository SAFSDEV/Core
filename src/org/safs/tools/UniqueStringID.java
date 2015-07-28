package org.safs.tools;

public class UniqueStringID implements UniqueIDInterface {

	protected String id = null;
	
	/**
	 * Constructor for UniqueStringID
	 */
	public UniqueStringID() {
		super();
	}

	/**
	 * Constructor for UniqueStringID
	 */
	public UniqueStringID(String id) {
		this();
		setStringID(id);
	}

	/**
	 * @see UniqueIDInterface#getUniqueID()
	 */
	public Object getUniqueID() {
		return id;
	}

	/**
	 * Sets the unique id String.
	 */
	public void setStringID(String id) {
		this.id = id;
	}

	/**
	 * Get the unique id already cast as a String.
	 */
	public String getStringID() {
		return id;
	}
}

