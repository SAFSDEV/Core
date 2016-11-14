/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * OCT 26, 2016    (Lei Wang) Initial release.
 */
package org.safs.install;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

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
	String gerPreferredHome();
	
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
}

