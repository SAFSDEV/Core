package org.safs.tools.input;
public class UniqueStringRecordClass implements UniqueRecordInterface {

	protected String id = "";
	protected String separator = "";

	public UniqueStringRecordClass(String id, String separator){
		super();
		this.id = id;
		this.separator = separator;
	}
		
	/**
	 * @see UniqueRecordInterface#getSeparator()
	 */
	public String getSeparator() {
		return separator;
	}

	/**
	 * @see UniqueIDInterface#getUniqueID()
	 */
	public Object getUniqueID() {
		return id;
	}
}

