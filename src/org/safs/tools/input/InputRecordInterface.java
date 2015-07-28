package org.safs.tools.input;

/**
 * This is the required interface for any InputInterface function that returns an 
 * input record to the calling Driver.  In general, null or values less than 1 
 * indicate unsuccessful attempts to retrieve the desired record, or that the source 
 * has reached the end of input (EOF).  
 * <p>
 * For this reason, the InputRecordInvalid concrete implementation is provided and 
 * an instance of this should be returned to indicate invalid data.
 * @see InputRecordInvalid
 */
public interface InputRecordInterface {

	/** 
	 * Return the row# or line# of the encapsulated data.
	 * Values less than 1 indicate no such record or EOF.
	 */
	public long getRecordNumber();

	/** 
	 * Return the actual string data of the input record.
	 * A null value indicates no such record or EOF.
	 */
	public String getRecordData();
	
	/**
	 * Returns TRUE if the values for recordnum and recorddata are valid.
	 */
	public boolean isValid();
}

