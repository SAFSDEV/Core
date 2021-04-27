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
 * @date 2020-10-14    (Lei Wang) Initial release.
 */
package org.safs.selenium.util;

import java.io.File;
import java.nio.file.Files;

import org.safs.Constants.BrowserConstants;
import org.safs.Constants.RegistryConstants;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.UtilsIndependent;
import org.safs.android.auto.lib.Console;
import org.safs.natives.NativeWrapper;
import org.safs.text.FileUtilities;

/**
 * @author Lei Wang
 *
 */
public class MSEdgeDriverUpdater extends AbstractDriverUpdater{

	/** The Windows registry key to find the executable path of the browser */
	public static final String REGISTRY_EXECUTABLE_PATH = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\msedge.exe";
	public static final String REGISTRY_SOFTWARE_PATH = "HKEY_LOCAL_MACHINE\\SOFTWARE";
	/** The Windows registry key suffix to get Edge information, should be prefixed by {@link #REGISTRY_SOFTWARE_PATH} */
	public static final String REGISTRY_MSEDGE_UNINSTALL_PATH = "Microsoft\\Windows\\CurrentVersion\\Uninstall\\Microsoft Edge";

	/** The prefix to create an URL for getting the latest driver's version. */
	public static final String URL_PREFIX_MSEDGE_LATEST_DRIVER = "https://msedgedriver.azureedge.net/LATEST_RELEASE_";
	/** The prefix to create an URL for getting the latest driver. */
	public static final String URL_PREFIX_MSEDGE_DRIVER_STORAGE = "https://msedgedriver.azureedge.net/";

	public static final String VENDOR = BrowserConstants.BROWSER_NAME_CHROMIUM_EDGE;

	public MSEdgeDriverUpdater(){
		vendor = VENDOR;
	}

	@Override
	public String getBrowserVersion() {
		String currentVersion = null;
		String debugmsg = StringUtils.debugmsg(false);

		if(Console.isWindowsOS()){
			//reg query "HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\msedge.exe" /v (Default)
			//wmic datafile where name="C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe" get Version /value
			Object executable = NativeWrapper.GetRegistryKeyValue(REGISTRY_EXECUTABLE_PATH, RegistryConstants.VALUE_DEFAULT);
			if(executable instanceof String){
				currentVersion = NativeWrapper.wmicGetVersion(executable.toString());
				isBrowser32Bit = executable.toString().contains("Program Files (x86)");//"C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe"
			}

			//reg query "HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\Microsoft Edge" /v Version
			if(!StringUtils.isValid(currentVersion)){
				currentVersion = NativeWrapper.getRegistry64Prodcut(REGISTRY_SOFTWARE_PATH, REGISTRY_MSEDGE_UNINSTALL_PATH, "Version");
				if(StringUtils.isValid(currentVersion)) isBrowser32Bit = false;
			}
			//reg query "HKEY_LOCAL_MACHINE\SOFTWARE\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall\Microsoft Edge" /v Version
			if(!StringUtils.isValid(currentVersion)){
				currentVersion = NativeWrapper.getRegistry32Prodcut(REGISTRY_SOFTWARE_PATH, REGISTRY_MSEDGE_UNINSTALL_PATH, "Version");
				if(StringUtils.isValid(currentVersion)) isBrowser32Bit = true;
			}

			if(!StringUtils.isValid(currentVersion)){
				IndependantLog.error(debugmsg+" failed to detect the version.");
			}
		}else{
			IndependantLog.warn(debugmsg+"'"+Console.getOsFamilyName()+"' is not supported yet.");
		}

		return currentVersion;
	}

	@Override
	public String getDriverVersion() {
		String driverVersion = null;
		String debugmsg = StringUtils.debugmsg(false);

		SePlusInstallInfo seplusInfo = null;
		try {
			seplusInfo = SePlusInstallInfo.instance();
			driverVersion = NativeWrapper.getDriverVersion(seplusInfo.getChromniumEdgeDriver().getCanonicalPath());
		} catch (Exception e) {
			IndependantLog.error(debugmsg+"Met "+e.toString());
		}

		return driverVersion;
	}

	@Override
	public String getMatchedDriverVerion(String browserVersion) {
		String matchedDriverVersion = browserVersion;
		//The link https://msedgewebdriverstorage.z22.web.core.windows.net/ contains all available drivers
		//It seems that Microsoft provides a driver for each version of msedge browser, so we will try the browser's version as driver's version
		//First we verify that we can use that browser's version to create a valid URL containing the driver to download
		String driverURL = getDriverDownloadURL(matchedDriverVersion);
		boolean driverURLExist = UtilsIndependent.isURLExist(driverURL);

		//Use the browser's major version to try, below are example URLs to download a file containing the matched driver version
		//https://msedgedriver.azureedge.net/LATEST_RELEASE_85
		//https://msedgedriver.azureedge.net/LATEST_RELEASE_85_MACOS
		//https://msedgedriver.azureedge.net/LATEST_RELEASE_85_WINDOWS
		if(!driverURLExist){
			String browserMajorVersion = browserVersion.substring(0, browserVersion.indexOf("."));
			String defaultDriverVersionFileURL = URL_PREFIX_MSEDGE_LATEST_DRIVER+browserMajorVersion;
			String driverVersionFileURL = defaultDriverVersionFileURL;
			if(Console.isWindowsOS()){
				driverVersionFileURL += "_WINDOWS";
			}else if(Console.isMacOS()){
				driverVersionFileURL += "_MACOS";
			}
			//From version 85, Microsoft provides separate URLs "majorVersion_WINDOWS" and "majorVersion_MACOS" for driver's version
			//For previous version, that kind of URL doesn't exist, we need to check it.
			if(!UtilsIndependent.isURLExist(driverVersionFileURL)){
				driverVersionFileURL = defaultDriverVersionFileURL;
			}

			matchedDriverVersion = getDriverVersionByURL(driverVersionFileURL);
		}

		return matchedDriverVersion;
	}

	/**
	 * @param url String, the URL to download driver version file.
	 * @return String, the driver's version
	 */
	private String getDriverVersionByURL(String url){
		String driverVersion = null;
		String debugmsg = StringUtils.debugmsg(false);
		File driverVersionFile = null;
		try {
			driverVersionFile = Files.createTempFile(vendor+"_driver", ".version").toFile();
			IndependantLog.debug(debugmsg+" downloading URL '"+url+"' to file "+driverVersionFile.getCanonicalPath());
			UtilsIndependent.downloadURL(url, driverVersionFile, false);
			IndependantLog.debug(debugmsg+" download completed");

			String[] contents = FileUtilities.readLinesFromFile(driverVersionFile.getCanonicalPath());
			//The first line is the driver's version
			if(contents!=null && contents.length>0) driverVersion = contents[0];
			if(StringUtils.isValid(driverVersion)){
				driverVersion = driverVersion.trim();
			}

		} catch (Exception e) {
			IndependantLog.error(debugmsg+ "Met "+e.toString());
		}finally{
			if(driverVersionFile!=null) try{ driverVersionFile.delete(); }catch(Exception e){}
		}

		return driverVersion;
	}

	@Override
	public String getDriverDownloadURL(String driverVersion){
		String debugmsg = StringUtils.debugmsg(false);
		//https://msedgedriver.azureedge.net/87.0.658.0/edgedriver_arm64.zip TODO for what OS?
		//https://msedgedriver.azureedge.net/87.0.658.0/edgedriver_mac64.zip
		//https://msedgedriver.azureedge.net/87.0.658.0/edgedriver_linux64.zip
		//https://msedgedriver.azureedge.net/87.0.658.0/edgedriver_win64.zip
		//https://msedgedriver.azureedge.net/87.0.658.0/edgedriver_win32.zip
		String url = URL_PREFIX_MSEDGE_DRIVER_STORAGE+driverVersion+"/";
		String driverFileName = null;
		if(Console.isWindowsOS()){
			driverFileName = "edgedriver";
			if(isBrowser32Bit) driverFileName += "_win32.zip";
			else driverFileName += "_win64.zip";
		}else if(Console.isMacOS()){
			driverFileName = "edgedriver_mac64.zip";
		}else if(Console.isUnixOS()){
			driverFileName = "edgedriver_linux64.zip";
		}else{
			IndependantLog.warn(debugmsg+"'"+Console.getOsFamilyName()+"' is not supported yet.");
		}

		if(!StringUtils.isValid(driverFileName)) return null;

		url += driverFileName;

		return url;
	}

	public static void main(String[] args){
		System.setProperty(SePlusInstallInfo.PROPERTY_PRODUCT_NAME, SePlusInstallInfo.PRODUCT_SELENIUM_PLUS);
		BrowserDriverUpdater updater = AbstractDriverUpdater.instance(VENDOR);
		updater.update();
	}
}
