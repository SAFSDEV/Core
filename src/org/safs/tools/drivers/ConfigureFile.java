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
