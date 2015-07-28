package org.safs.tools.input;

import org.safs.tools.UniqueStringID;

public class UniqueStringItemInfo
	extends UniqueStringID
	implements UniqueItemInterface {

	protected String sectionname = null;
	protected String itemname    = null;
	
	/**
	 * Constructor for UniqueStringItemInfo
	 */
	public UniqueStringItemInfo() {
		super();
	}

	/**
	 * Constructor for UniqueStringItemInfo
	 */
	public UniqueStringItemInfo(String id) {
		super(id);
	}

	/**
	 * PREFERRED Constructor for UniqueStringItemInfo
	 */
	public UniqueStringItemInfo(String id, String sectionname, String itemname) {
		this(id);
		setSectionName(sectionname);
		setItemName(itemname);
	}

	/**
	 * Set itemname to that provided.
	 */
	public void setItemName(String itemname) {
		this.itemname = itemname;
	}

	/**
	 * @see UniqueItemInterface#getItemName()
	 */
	public String getItemName() {
		return itemname;
	}

	/**
	 * Set sectionname to that provided.
	 */
	public void setSectionName(String sectionname) {
		this.sectionname = sectionname;
	}

	/**
	 * @see UniqueSectionInterface#getSectionName()
	 */
	public String getSectionName() {
		return sectionname;
	}

}

