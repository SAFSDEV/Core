/** Copyright (C) (SAS) All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
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
