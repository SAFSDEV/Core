package org.safs.tools.input;

public interface UniqueFileInterface extends UniqueSourceInterface {

	/** 
	 * Get the filename of the input file for the unique ID.
	 */
	public String getFilename();

	/** 
	 * Set the filename of the input file for the unique ID.
	 * Thus, an InputInterface can set the fullpath for a found file.**/
	public void setFilename(String filename);
}

