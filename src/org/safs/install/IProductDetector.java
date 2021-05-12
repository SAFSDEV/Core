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
 * Logs for developers, not published to API DOC.
 *
 * History:
 * OCT 26, 2016    (Lei Wang) Initial release.
 * JUN 29, 2018    (Lei Wang) Defined methods to handle product's bit, installer, uninstaller, options for installer/uninstaller.
 */
package org.safs.install;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import org.safs.install.InstallerImpl.BIT_OPTION;

/**
 * If a product (multiple versions of one product) has been installed, we may want to know:
 * <ul>
 * <li>where it has (they have) been installed.
 * <li>what installation path is preferable to use if multiples are installed
 * <li>what are the possible executables
 * </ul>
 *
 * @author Lei Wang
 */
interface IProductDetector{
	/**
	 * Note: The map's key is used for choosing product to use.<br>
	 *       User will decide key's format. It could be "ProductName+VersionNumber" or "VersionNumber".<br>
	 * @return Map<String, String>, a map containing pairs of (key, product_installation_full_path).<br>
	 *                   			an empty map if non installation has been detected.<br>
	 */
	Map<String, String> getHomes();
	/**
	 * @return String, the preferred product_installation_full_path if multiple products have been installed.<br>
	 *                 null if non product installation is detected.
	 */
	String getPreferredHome();

	/**
	 * @return String, the default home directory to install the product
	 */
	String getDefaultHome();

	/**
	 * @return String[], an array of sub folder (relative to the product home) holding executables.
	 *                   An empty array if there is no possible value.<br>
	 */
	String[] getBins();

	/**
	 * @return String[], an array of "relative path" for possible executables (relative to the product home).<br>
	 *                   The first element is the most favorable; the last the least favorable.<br>
	 *                   An empty array if no executable is possible.<br>
	 */
	String[] getPossibleExecutables();

	/**
	 * Find the valid executable for the product.<br>
	 * The search logic could be:<br>
	 * 1. find the default executable under product home<br>
	 * 2. find the default executable under the {@link #getBins()} sub folder of product home<br>
	 * 3. find the {@link #getPossibleExecutables()} under the product home<br>
	 *
	 * @param home	File, the product home directory
	 * @param defaultExecutable String, the default executable. It could be name or "path relative to home".
	 * @return File, the valid executable
	 * @throws FileNotFoundException if no executable could be found
	 * @see ProductDetectorDefault
	 */
	File getValidExecutable(File home, String defaultExecutable) throws FileNotFoundException;

	/**
	 * @return BIT_OPTION, the bit (32 or 64) of the product to detect.
	 */
	BIT_OPTION getProductBit();
	/**
	 * @param productBit BIT_OPTION, the bit (32 or 64) of the product to detect.
	 */
	void setProductBit(BIT_OPTION productBit);

	/**
	 * The un-installer is under the product itself folder.
	 * @return String, the un-installer's file name, it might be different between 32 bits and 64 bits product.
	 */
	String getUnInstaller();


	/**
	 * The installer is always under the SAFS/SeleniumPlus install's folder.
	 * @return String, the installer's file name, it might be different between 32 bits and 64 bits product.
	 */
	String getInstaller();

	/**
	 * @param home String, the product's home directory
	 * @return String, the full path to the un-installer executable
	 * @throws FileNotFoundException if not found
	 */
	String findUnInstaller(String home) throws FileNotFoundException;

	//TODO perhaps we need to define appendXXXOption() for installer and uninstaller separately.
	//The installer and uninstaller may not use the same options.
	/** Append silent option to installer and uninstaller */
	String appendSientOption(String command);
	/** Append installation directory option to installer and uninstaller */
	String appendInstallDirOption(String command, String installDirectory);
	/** Append options to installer and uninstaller */
	String appendOptions(String command, String... options);
}

