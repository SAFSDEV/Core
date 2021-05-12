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
package org.safs.projects.common.projects.pojo;

/**
 * This class is meant to be a POJO implementation of a Path that is similar to the Eclipse
 * IPath.
 * 
 * For projects that use something like Eclipse, they are expected to use a subclass
 * that will hold something like Eclipse's IPath and delegate calls to it.
 *
 */
public class POJOPath {
	private String pathStr;

	/**
	 * This constructor will most likely be used from a subclass that will hold
	 * something like Eclipse's IPath.
	 */
	protected POJOPath() {

	}

	/**
	 * This constructor will be used with a project that does not use Eclipse.
	 * @param pathStr
	 */
	public POJOPath(String pathStr) {
		this.pathStr = pathStr;
	}

	@Override
	public String toString() {
		return pathStr;
	}

}
