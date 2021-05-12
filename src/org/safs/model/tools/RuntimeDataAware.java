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
package org.safs.model.tools;

import org.safs.tools.RuntimeDataInterface;

/**
 * Classes implementing this interface can get automatically configured by classes  
 * performing or facilitating Dependency Injection at runtime.
 * <p>EXPERIMENTAL<p>
 * It is IMPORTANT to note that some Dependency Injection models may create a temporary and 
 * non-cached instance of the RuntimeDataAware Class solely for the purpose of invoking the 
 * setRuntimeDataInterface instance Method.  In order for subsequent instances to make use of the 
 * RuntimeDataInterface object it is recommended that any and all instances should store it in a 
 * shared (static) field.
 * <p>   
 * @author Carl Nagle
 */
public interface RuntimeDataAware {

	/**
	 * Set the RuntimeDataInterface instance to be used by the RuntimeDataAware class instance.
     * <p>EXPERIMENTAL<p>
	 * It is IMPORTANT to note that some Dependency Injection models may create a temporary and 
	 * non-cached instance of the RuntimeDataAware Class solely for the purpose of invoking the 
	 * setRuntimeDataInterface instance Method.  In order for subsequent instances to make use of the 
	 * RuntimeDataInterface object it is recommended that any and all instances should store it in a 
	 * shared (static) field.
	 * <p>   
	 * @param helper
	 * @see RuntimeDataInterface
	 */
	public abstract void setRuntimeDataInterface(RuntimeDataInterface helper);
	
}
