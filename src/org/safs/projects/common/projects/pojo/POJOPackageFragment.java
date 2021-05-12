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
 * This is a POJO implementation if a PackageFragment similar to the Eclipse PackageFragment.
 * This implementation will be used by a project that does not use
 * something like Eclipse.
 * 
 * For projects that use something like Eclipse, they will likely use
 * a subclass that will hold an Eclipse PackageFragment and will delegate calls to it.
 *
 */
public class POJOPackageFragment {
	private String elementName;

	/**
	 * This constructor will likely be used by a subclass that holds something
	 * like Eclipse's PackageFragment and delegates to it.
	 */
	protected POJOPackageFragment() {

	}

	public POJOPackageFragment(String elementName) {
		this.elementName = elementName;
	}

	/**
	 * Gets the element name of this POJOPackageFragment.
	 * 
	 * Subclasses are expected to overwrite this method and call a delegate's
	 * getElementName method (such as Eclipse's PackageFragment).
	 * 
	 * @return the element name
	 */
	public String getElementName() {
		return elementName;
	}
}
