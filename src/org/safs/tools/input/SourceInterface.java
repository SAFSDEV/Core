package org.safs.tools.input;

import org.safs.tools.PathInterface;

public interface SourceInterface {

	/** Get default separator used to delimit input record fields.**/
	public String getDefaultSeparator();

	/** Get the Test Level of the Source.**/
	public String getTestLevel();

	/** 
	 * Get the Name of the Source.  For example, the Filename.
	 * This is different than the ID. Since many instances of the same named source 
	 * can be opened with different Unique IDs.
	 * **/
	public String getSourceName();

	/** 
	 * Get the full path to the Source.  For example, the full path filename.
	 * **/
	public String getSourcePath(PathInterface driver);
}

