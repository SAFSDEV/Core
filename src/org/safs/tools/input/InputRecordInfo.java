package org.safs.tools.input;

/**
 * An instance of this object conveys the presence of valid data to any function 
 * expecting an InputRecordInterface return value.  To convey an EOF or INVALID data 
 * scenario, it is easier to use the InputRecordInvalid class.
 * @see InputRecordInvalid
 */
public class InputRecordInfo implements InputRecordInterface {

	protected long recordnum    = 0;
	protected String recorddata = null;
	
	/**
	 * Constructor for InputRecordInfo
	 */
	public InputRecordInfo (String recorddata, long recordnum){
		super();
		this.recorddata = recorddata;
		this.recordnum  = recordnum;
	}
	
	/**
	 * @see InputRecordInterface#getRecordNumber()
	 */
	public long getRecordNumber() {
		return recordnum;
	}

	/**
	 * @see InputRecordInterface#getRecordData()
	 */
	public String getRecordData() {
		return recorddata;
	}
	
	/**
	 * @see InputRecordInterface#isValid()
	 */
	public boolean isValid(){
		if(recordnum < 1)    return false;
		if(recorddata==null) return false;
		return true;
	}
}

