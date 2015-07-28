package org.safs.tools.input;

import org.safs.tools.UniqueIDInterface;

public interface UniqueSectionInterface extends UniqueIDInterface {
	
	/** @return the name of an identified Section, usually for an App Map.**/
	public String getSectionName();
}

