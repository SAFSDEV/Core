package org.safs.tools.drivers;

import java.io.File;

public interface ConfigureLocatorInterface {

	/** 
	 * Attempt to locate DRIVER root/install directory.<br>
	 * @param rootDir -- The rootDir may be a String 
	 * containing a path to a directory, or to a file, or to some 
	 * other type of object as needed by the specific implementation.  
	 * <p>
	 * @return a File object or null if not located. **/
	public File locateRootDir(String rootDir);
	
	/** 
	 * Attempt to locate DRIVER configuration data.
	 * @param rootDir -- The rootDir may be a String 
	 * containing a path to a directory, or to a file, or to some 
	 * other type of object as needed by the specific implementation.  
	 * @param configPath -- The configPath may be a String 
	 * containing a path to a directory, or to a file, or to some 
	 * other type of object as needed by the specific implementation.
	 * <p>
	 * The rootDir and configPath may be used in combination to resolve to the ConfigureInterface.
	 * @return ConfigureInterface if located, or null if not.**/
	public ConfigureInterface locateConfigureInterface (String rootDir, String configPath);

}

