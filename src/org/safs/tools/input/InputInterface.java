package org.safs.tools.input;

import org.safs.tools.UniqueIDInterface;

public interface InputInterface {

	/** 
	 * Open a unique instance of an input source.
	 * The source "pointer" should be ready to read the first record if successful.
	 * @return TRUE if OPEN was successful.
	 */
	public boolean open (UniqueSourceInterface source);

	/** 
	 * Get the next record from the identified input source.
	 * Should return a valid InputRecordInterface, or an InputRecordInvalid object.
	 * @see InputRecordInterface
	 * @see InputRecordInvalid
	 */
	public InputRecordInterface nextRecord (UniqueIDInterface source);
	
	/** 
	 * Rewind the identified input source to its first record.
	 * The source "pointer" should be ready to read the first record on exit.
	 * @return TRUE if successful.
	 */
	public boolean gotoStart (UniqueIDInterface source);
	
	/** 
	 * Locate and return the requested record in the input source.
	 * The source "pointer" should be ready to read the record AFTER the identified 
	 * record on exit.
	 * Should return a valid InputRecordInterface, or an InputRecordInvalid object.
	 * @see InputRecordInterface
	 * @see InputRecordInvalid
	 */
	public InputRecordInterface gotoRecord (UniqueRecordInterface recordInfo);
	
	/** Close the identified input source and release resources.**/
	public void close (UniqueIDInterface source);
}

