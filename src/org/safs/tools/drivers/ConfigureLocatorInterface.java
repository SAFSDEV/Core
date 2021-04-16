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
/**
 * Developer Logs:
 * MAY 18, 2018		Lei Wang	Moved the content of method AbstractDriver.getConfigureLocator() to here as a static method.
 */
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

	/**
	 * Locate a ConfigureLocatorInterface given the locatorInfo, presumably
	 * provided from command-line options.
	 * <p>
	 * @exception IllegalArgumentException if appropriate locator class cannot be
	 * instantiated.**/
	public static ConfigureLocatorInterface getConfigureLocator(String locatorClass){

		ConfigureLocatorInterface locator = null;
		try{
			locator = (ConfigureLocatorInterface) Class.forName(locatorClass).newInstance();
			return locator;
		}
		catch(ClassNotFoundException cnfe){
			throw new IllegalArgumentException(
			"ClassNotFoundException:Invalid or Missing Configuration Locator class: "+ locatorClass);
		}
		catch(InstantiationException ie){
			throw new IllegalArgumentException(
			"InstantiationException:Invalid or Missing Configuration Locator class: "+ locatorClass);
		}
		catch(IllegalAccessException iae){
			throw new IllegalArgumentException(
			"IllegalAccessException:Invalid or Missing Configuration Locator class: "+ locatorClass);
		}
	}
}

