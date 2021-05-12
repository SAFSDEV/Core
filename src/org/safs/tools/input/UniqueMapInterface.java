/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
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

