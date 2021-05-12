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
 * @date 2020-09-27    (Lei Wang) Initial release.
 */
package org.safs.selenium.util;

import java.util.Map;

import org.safs.Constants.BrowserConstants;
import org.safs.Constants.RegistryConstants;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.android.auto.lib.Console;
import org.safs.natives.NativeWrapper;
import org.safs.net.HttpRequest;
import org.safs.net.IHttpRequest;
import org.safs.net.IHttpRequest.HttpCommand;

/**
 * @author Lei Wang
 *
 */
public class ChromeDriverUpdater extends AbstractDriverUpdater{

	/** The Windows registry key to find the executable path of the browser */
	public static final String REGISTRY_EXECUTABLE_PATH = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\chrome.exe";
	public static final String REGISTRY_SOFTWARE_PATH = "HKEY_LOCAL_MACHINE\\SOFTWARE";
	/** The Windows registry key suffix to get Edge information, should be prefixed by {@link #REGISTRY_SOFTWARE_PATH} */
	public static final String REGISTRY_CHROME_UNINSTALL_PATH = "Microsoft\\Windows\\CurrentVersion\\Uninstall\\Google Chrome";

	/** The prefix to create an URL for getting the latest driver's version. */
	public static final String URL_PREFIX_CHROME_LATEST_DRIVER = "https://chromedriver.storage.googleapis.com/LATEST_RELEASE_";
	/** The prefix to create an URL for getting the latest driver. */
	public static final String URL_PREFIX_CHROME_DRIVER_STORAGE = "https://chromedriver.storage.googleapis.com/";

	public static final String VENDOR = BrowserConstants.BROWSER_NAME_CHROME;

	public ChromeDriverUpdater(){
		vendor = VENDOR;
	}

	@Override
	public String getBrowserVersion() {
		String currentVersion = null;
		String debugmsg = StringUtils.debugmsg(false);

		if(Console.isWindowsOS()){
			//reg query "HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\chrome.exe" /v (Default)
			//wmic datafile where name="C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe" get Version /value
			Object executable = NativeWrapper.GetRegistryKeyValue(REGISTRY_EXECUTABLE_PATH, RegistryConstants.VALUE_DEFAULT);
			if(executable instanceof String){
				currentVersion = NativeWrapper.wmicGetVersion(executable.toString());
			}

			//reg query "HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\Google Chrome" /v Version
			if(!StringUtils.isValid(currentVersion)){
				currentVersion = NativeWrapper.getRegistry64Prodcut(REGISTRY_SOFTWARE_PATH, REGISTRY_CHROME_UNINSTALL_PATH, "Version");
			}
			//reg query "HKEY_LOCAL_MACHINE\SOFTWARE\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall\Google Chrome" /v Version
			if(!StringUtils.isValid(currentVersion)){
				currentVersion = NativeWrapper.getRegistry32Prodcut(REGISTRY_SOFTWARE_PATH, REGISTRY_CHROME_UNINSTALL_PATH, "Version");
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
			driverVersion = NativeWrapper.getDriverVersion(seplusInfo.getChromeDriver().getCanonicalPath());
		} catch (Exception e) {
			IndependantLog.error(debugmsg+"Met "+e.toString());
		}

		return driverVersion;
	}

	@Override
	public String getMatchedDriverVerion(String browserVersion) {
		//In the following link talks about how to get the matched driver
		//https://sites.google.com/a/chromium.org/chromedriver/downloads/version-selection

		String matchedDriverVersion = null;
		String url = null;

		//For example, with Chrome version 72.0.3626.81, you'd get a URL "https://chromedriver.storage.googleapis.com/LATEST_RELEASE_72.0.3626".
		url = URL_PREFIX_CHROME_LATEST_DRIVER+browserVersion.substring(0, browserVersion.lastIndexOf("."));
		matchedDriverVersion = getDriverVersionByURL(url);

		//Use the browser's major version to try
		//"https://chromedriver.storage.googleapis.com/LATEST_RELEASE_72"
		String browserMajorVersion = browserVersion.substring(0, browserVersion.indexOf("."));
		if(!StringUtils.isValid(matchedDriverVersion)){
			url = URL_PREFIX_CHROME_LATEST_DRIVER+browserMajorVersion;
			matchedDriverVersion = getDriverVersionByURL(url);
		}

		//decrement the browser's major version to try
		//"https://chromedriver.storage.googleapis.com/LATEST_RELEASE_71"
		if(!StringUtils.isValid(matchedDriverVersion)){
			url = URL_PREFIX_CHROME_LATEST_DRIVER+(Integer.parseInt(browserMajorVersion)-1);
			matchedDriverVersion = getDriverVersionByURL(url);
		}

		//TODO should we continue decrement the major version to try?

		return matchedDriverVersion;
	}

	private String getDriverVersionByURL(String url){
		String driverVersion = null;
		String debugmsg = StringUtils.debugmsg(false);
		HttpRequest request = new HttpRequest();
		Map<String, Object> result = null;
		try {
			IndependantLog.debug(debugmsg+" Querying URL "+url+" ... ");
			result = request.execute(HttpCommand.GET, url, false/*run synchronously*/, null, null);
			IndependantLog.debug(debugmsg+" Got Result: "+result);
			if(result!=null){
				driverVersion = (String) result.get(IHttpRequest.Key.RESPONSE_TEXT.value());
			}
			if(driverVersion!=null){
				driverVersion = driverVersion.trim();
			}
		} catch (Exception e) {
			IndependantLog.error(debugmsg+ "Met "+e.toString());
		}

		return driverVersion;
	}

	@Override
	public String getDriverDownloadURL(String driverVersion){
		String debugmsg = StringUtils.debugmsg(false);
		//https://chromedriver.storage.googleapis.com/72.0.3626.69/chromedriver_win32.zip
		//https://chromedriver.storage.googleapis.com/72.0.3626.69/chromedriver_mac64.zip
		//https://chromedriver.storage.googleapis.com/72.0.3626.69/chromedriver_linux64.zip
		String url = URL_PREFIX_CHROME_DRIVER_STORAGE+driverVersion+"/";
		String driverFileName = null;
		if(Console.isWindowsOS()){
			driverFileName = "chromedriver_win32.zip";
		}else if(Console.isMacOS()){
			driverFileName = "chromedriver_mac64.zip";
		}else if(Console.isUnixOS()){
			driverFileName = "chromedriver_linux64.zip";
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
