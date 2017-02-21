package org.safs.tools.drivers;

import org.safs.text.INIFileReader;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

public class ConfigureFile implements ConfigureInterface {
	protected File configFile = null;
	protected Vector store = new Vector(3,1);
	protected INIFileReader reader = null;
	
	/**
	 * Empty Constructor for Configure. The instance won't be able to do anything until 
	 * a valid File object representing a readable INI file has been provided via 
	 * setConfigurationFile.
	 */
	public ConfigureFile() {
		super();
	}

	/**
	 * Primary Constructor for Configure. The File object must represent a readable 
	 * INI file and will be forwarded to setConfigurationFile.
	 */
	public ConfigureFile(File configFile) {
		super();
		setConfigurationFile(configFile);
	}

	/**
	 * The File object must represent a readable INI file and will be used to 
	 * initialize an internal INIFileReader.
	 */
	public void setConfigurationFile(File configFile){
		if (this.configFile==null) {			
		    this.configFile = configFile;
		    reader = new INIFileReader(this.configFile, INIFileReader.IFR_MEMORY_MODE_STORED);
		}
	}
	/**
	 * @see ConfigureInterface#addConfigureInterface(ConfigureInterface)
	 */
	public void addConfigureInterface(ConfigureInterface configSource) {
		store.addElement(configSource);
	}

	/**
	 * @see ConfigureInterface#insertConfigureInterface(ConfigureInterface)
	 */
	public void insertConfigureInterface(ConfigureInterface configSource) {
		store.insertElementAt(configSource, 0);
	}

	/**
	 * @see ConfigureInterface#getNamedValue(String, String)
	 */
	public String getNamedValue(String keyName, String itemName) {
		String value = null;
		ConfigureInterface anInterface = null;

		value = reader.getAppMapItem(keyName, itemName);
		Enumeration enumerator = store.elements();
		while((enumerator.hasMoreElements())&&(value==null)){
			anInterface = (ConfigureInterface) enumerator.nextElement();
			value = anInterface.getNamedValue(keyName, itemName);
		}
		return value;
	}

	/**
	 * @see ConfigureInterface#getConfigurePaths()
	 */
	public String getConfigurePaths() {
		String configPaths;		
		// prepend our path to those in the chain
		if (configFile != null) {
			configPaths = configFile.getAbsolutePath();
		}else{
			configPaths = "";
		}
		if (! store.isEmpty()){
			ConfigureInterface config;
			for (int i=0;i < store.size();i++){
				config = (ConfigureInterface) store.get(i);
				configPaths += File.pathSeparator + config.getConfigurePaths();
			}
		}
		return configPaths;
	}
}
