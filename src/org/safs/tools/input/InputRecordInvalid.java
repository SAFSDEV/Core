package org.safs.tools.input;


/**
 * The mere instance of this object conveys "invalid data" for any function accepting an
 * InputRecordInterface object.  This can convey an EOF condition, or other invalid
 * data scenarios.  These are calling-function specific.
 * @see InputRecordInfo
 */
public class InputRecordInvalid implements InputRecordInterface {


	/**
	 * @see InputRecordInterface#getRecordNumber()
	 */
	public long getRecordNumber() {
		return -1;
	}

	/**
	 * @see InputRecordInterface#getRecordData()
	 */
	public String getRecordData() {
		return null;
	}

	/**
	 * @see InputRecordInterface#isValid()
	 */
	public boolean isValid(){
		return false;
	}
}
