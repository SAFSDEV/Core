package org.safs.tools.input;

import org.safs.tools.UniqueIDInterface;
import org.safs.tools.PathInterface;

public interface UniqueMapInterface extends UniqueIDInterface {
	
	/** Get the name of the stored Map.  It is always possible that one Map file or source 
	 * is "opened" more than once with different unique IDs.  The name may be a simple name, 
	 * or it may be the name of a file, a fullpath to a file, or some other Object as 
	 * needed by the implementation. **/
	public Object getMapInfo();

	/**
	 * Get the full path of the specified Map.
	 * @param driver allows the object to use relative paths based on different 
	 * directories (Datapool, Project, Bench). 
	 */
	public Object getMapPath(PathInterface driver);	
}

