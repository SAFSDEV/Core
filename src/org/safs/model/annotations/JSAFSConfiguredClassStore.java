/******************************************************************************
 * Copyright (c) by SAS Institute Inc., Cary, NC 27513
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 ******************************************************************************/ 
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
