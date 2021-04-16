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

