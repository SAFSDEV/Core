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
package org.safs.model.annotations;

/**
 * Required interface for a class that intends to store the object instances that will
 * be automatically created by the automatic dependency injection and execution processes
 * in the Utilities Class.
 * <p>
 * This allows examination and use of these objects after the automated processing has completed.
 * @author Carl Nagle OCT 15, 2013
 * @see org.safs.model.tools.Runner
 * @see Utilities#autoConfigure(String, JSAFSConfiguredClassStore)
 */
public interface JSAFSConfiguredClassStore {

	/**
	 * Return the instantiated instance of the provided full Class name, if any.
	 * @param classname -- the full path case-sensitive Class name that identifies the
	 * object instance to be retrieved.
	 * @return the associated object instance for the provided class name.  Can be null
	 * if no such class instance is stored.
	 * @throws NullPointerException -- the implementation may throw this if the provided
	 * classname is null.
	 */
	public abstract Object getConfiguredClassInstance(String classname);

	/**
	 * Used internally.<br>
	 * Stores the object instance instantiated from the full Class name.
	 * @param classname -- the full path case-sensitive Class name that was used to
	 * instantiate the object.
	 * @param object -- the object that was instantiated and used for processing.
	 * @throws NullPointerException -- the implementation may throw this if the provided
	 * classname or object are null.
	 */
	public abstract void addConfiguredClassInstance(String classname, Object object);
}
