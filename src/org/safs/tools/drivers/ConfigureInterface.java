package org.safs.tools.drivers;

public interface ConfigureInterface {

	/** 
	 * Add a ConfigureInterface to the search chain for Named Values.
	 * The item is added at the end of the chain to be searched.<br>
	 * This object will search its getNamedValue() before it resorts 
	 * to any stored ConfigureInterface objects in the stored chain. **/
	public void addConfigureInterface (ConfigureInterface configSource);
	
	/** 
	 * Insert a ConfigureInterface to the search chain for Named Values.
	 * The item is inserted at the beginning of the chain to be searched.<br>
	 * This object will search its getNamedValue() before it resorts 
	 * to any stored ConfigureInterface objects in the stored chain. **/
	public void insertConfigureInterface (ConfigureInterface configSource);
	
	/** 
	 * Retrieve an item that may be in the configuration sources.
	 * An item is identified by a parent key or section name--like in 
	 * an INI file "section"--and the  name of the item in that section.<br>
	 * This object will search its getNamedValue() before it resorts 
	 * to any stored ConfigureInterface objects in the stored chain.
	 * @return the retrieved String value or null if no such value exists.**/
	public String getNamedValue (String keyName, String itemName);
	
	/**
	 * Retrieve a String containing a File.pathSeparator delimited list of 
	 * all the file paths in the configuration file chain.  
	 **/ 
	public String getConfigurePaths();
}

