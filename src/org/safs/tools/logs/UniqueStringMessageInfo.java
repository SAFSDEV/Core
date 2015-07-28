package org.safs.tools.logs;

import org.safs.tools.UniqueStringID;

public class UniqueStringMessageInfo
	extends UniqueStringID
	implements UniqueMessageInterface {

	protected String message     = null;
	protected String description = null;
	protected int   type        = 0;
	
	/**
	 * Constructor for UniqueStringMessageInfo
	 */
	public UniqueStringMessageInfo() {
		super();
	}

	/**
	 * Constructor for UniqueStringMessageInfo
	 */
	public UniqueStringMessageInfo(String id) {
		super(id);
	}

	/**
	 * PREFERRED Constructor for UniqueStringMessageInfo
	 */
	public UniqueStringMessageInfo(String id, String message, 
	                               String description, int type) {
		this(id);
		setLogMessage(message);
		setLogMessageDescription(description);
		setLogMessageType(type);
	}

	/**
	 * Set message to String provided.
	 */
	public void setLogMessage(String message) {
		this.message = message;
	}

	/**
	 * @see UniqueMessageInterface#getLogMessage()
	 */
	public String getLogMessage() {
		return message;
	}

	/**
	 * Set message description to String provided.
	 */
	public void setLogMessageDescription(String description) {
		this.description = description;
	}

	/**
	 * @see UniqueMessageInterface#getLogMessageDescription()
	 */
	public String getLogMessageDescription() {
		return description;
	}

	/**
	 * Set message type to that provided.
	 */
	public void setLogMessageType(int type) {
		this.type = type;
	}

	/**
	 * @see UniqueMessageInterface#getLogMessageType()
	 */
	public int getLogMessageType() {
		return type;
	}

}

