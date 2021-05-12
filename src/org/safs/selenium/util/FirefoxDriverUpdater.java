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
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class FirefoxDriverUpdater extends AbstractDriverUpdater{

	/** The Windows registry key to find the executable path of the browser */
	public static final String REGISTRY_EXECUTABLE_PATH = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\firefox.exe";
	public static final String REGISTRY_SOFTWARE_PATH = "HKEY_LOCAL_MACHINE\\SOFTWARE";
	/** The Windows registry key suffix to get Edge information, should be prefixed by {@link #REGISTRY_SOFTWARE_PATH} */
	public static final String REGISTRY_FIREFOX_PATH = "Mozilla\\Mozilla Firefox";

	/** The prefix to create an URL for getting the latest driver. */
	public static final String URL_PREFIX_FIREFOX_DRIVER_STORAGE = "https://github.com/mozilla/geckodriver/releases/download/";

	public static final String VENDOR = BrowserConstants.BROWSER_NAME_FIREFOX;

	public FirefoxDriverUpdater(){
		vendor = VENDOR;
		getBrowserVersion();//initialize the field 'isBrowser32Bit'
	}

	@Override
	public boolean isBrowser32Bit(){
		return isBrowser32Bit;
	}

	/**
	 * This method will not only find out the version of the installed firefox;
	 * it will also detect the bitness (32 or 64 bits) of the installed firefox, the result will be kept in {@link #isBrowser32Bit}.<br>
	 */
	@Override
	public String getBrowserVersion() {
		String currentVersion = null;
		String debugmsg = StringUtils.debugmsg(false);

		if(Console.isWindowsOS()){
			//reg query "HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\firefox.exe" /v (Default)
			//wmic datafile where name="C:\\Program Files\\Mozilla Firefox\\firefox.exe" get Version /value
			Object executable = NativeWrapper.GetRegistryKeyValue(REGISTRY_EXECUTABLE_PATH, RegistryConstants.VALUE_DEFAULT);
			if(executable instanceof String){
				currentVersion = NativeWrapper.wmicGetVersion(executable.toString());
				isBrowser32Bit = executable.toString().contains("Program Files (x86)");//"C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe"
			}

			//reg query "HKEY_LOCAL_MACHINE\SOFTWARE\Mozilla\Mozilla Firefox" /v (Default)
			if(!StringUtils.isValid(currentVersion)){
				currentVersion = NativeWrapper.getRegistry64Prodcut(REGISTRY_SOFTWARE_PATH, REGISTRY_FIREFOX_PATH, RegistryConstants.VALUE_DEFAULT);
				if(StringUtils.isValid(currentVersion)) isBrowser32Bit = false;
			}
			//reg query "HKEY_LOCAL_MACHINE\SOFTWARE\WOW6432Node\Mozilla\Mozilla Firefox" /v (Default)
			if(!StringUtils.isValid(currentVersion)){
				currentVersion = NativeWrapper.getRegistry32Prodcut(REGISTRY_SOFTWARE_PATH, REGISTRY_FIREFOX_PATH, RegistryConstants.VALUE_DEFAULT);
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
			driverVersion = NativeWrapper.getDriverVersion(seplusInfo.getDriver(BrowserConstants.BROWSER_NAME_FIREFOX).getCanonicalPath());
		} catch (Exception e) {
			IndependantLog.error(debugmsg+"Met "+e.toString());
		}

		return driverVersion;
	}

	/**
	 * Unlike Chrome, Firefox doesn't provide an URL to give the matched driver version according to the browser's version.
	 * In https://github.com/mozilla/geckodriver/releases, it describes the relationship between the driver and the browser, but
	 * it is for human to read not for computer, we will just keep these mappings (driver-version, required-minimum-browser-version) in our code.<br>
	 * With times, we need to update this map when firefox make a new release with a required-minimum-browser-version.<br>
	 * Holding the pair(<b>driver-version</b>, <b>required-minimum-browser-version</b>).<br/>
	 */
	private static final Map<String, String> driverBrowserMap = new TreeMap<String, String>();
	static{
		driverBrowserMap.put("0.19.0", "55");//Firefox 55 (and greater)
		driverBrowserMap.put("0.21.0", "57");//Firefox 57 (and greater)
		driverBrowserMap.put("0.26.0", "60");//Firefox 60 (and greater)
	}

	@Override
	public String getMatchedDriverVerion(String browserVersion) {
		String matchedDriverVersion = null;

		Set<String> driverVersions = driverBrowserMap.keySet();
		String requiredBrowserVersion = null;
		for(String driverVersion: driverVersions){
			requiredBrowserVersion = driverBrowserMap.get(driverVersion);
			//continue to use the next driver until we find the "requiredBrowserVersion" is bigger than the current "browserVersion"
			if(browserVersion.compareTo(requiredBrowserVersion)>=0) matchedDriverVersion=driverVersion;
			else break;
		}

		//if the "matchedDriverVersion" is the latest version in the "driverBrowserMap",
		//we will try to get the latest version from URL "https://github.com/mozilla/geckodriver/releases/latest".
		if(((TreeMap<String, String>)driverBrowserMap).lastKey().equals(matchedDriverVersion)){
			String url = "https://github.com/mozilla/geckodriver/releases/latest";
			matchedDriverVersion = getDriverVersionByURL(url);
		}

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

			//use pattern to get the "0.27.0" from string "/mozilla/geckodriver/releases/tag/v0.27.0"
			Pattern pattern = Pattern.compile("/mozilla/geckodriver/releases/tag/v(\\d+\\.\\d+\\.\\d+)");//use pattern to find "0.27.0"
			Matcher matcher = pattern.matcher(driverVersion);

			if(matcher.find()){
				driverVersion = matcher.group(1);
			}

		} catch (Exception e) {
			IndependantLog.error(debugmsg+ "Met "+e.toString());
		}

		return driverVersion;
	}

	@Override
	public String getDriverDownloadURL(String driverVersion){
		String debugmsg = StringUtils.debugmsg(false);
		//https://github.com/mozilla/geckodriver/releases/download/v0.27.0/geckodriver-v0.27.0-win32.zip
		//https://github.com/mozilla/geckodriver/releases/download/v0.27.0/geckodriver-v0.27.0-win64.zip
		//https://github.com/mozilla/geckodriver/releases/download/v0.27.0/geckodriver-v0.27.0-linux32.tar.gz
		//https://github.com/mozilla/geckodriver/releases/download/v0.27.0/geckodriver-v0.27.0-linux64.tar.gz
		//https://github.com/mozilla/geckodriver/releases/download/v0.27.0/geckodriver-v0.27.0-macos.tar.gz
		String url = URL_PREFIX_FIREFOX_DRIVER_STORAGE+"v"+driverVersion+"/";
		String driverFileName = null;
		if(Console.isWindowsOS()){
			driverFileName = "geckodriver-v"+driverVersion;
			if(isBrowser32Bit) driverFileName += "-win32.zip";
			else driverFileName += "-win64.zip";
		}else if(Console.isMacOS()){
			driverFileName = "geckodriver-v"+driverVersion+"-macos.tar.gz";
		}else if(Console.isUnixOS()){
			driverFileName = "geckodriver-v"+driverVersion+"-linux32.tar.gz";
			if(isBrowser32Bit) driverFileName += "-linux32.tar.gz";
			else driverFileName += "-linux64.tar.gz";
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

//		String bv = updater.getBrowserVersion();
//		System.out.println("current browser version is "+bv);
//
//		String dv = updater.getDriverVersion();
//		System.out.println("current driver version is "+dv);
//
//		String matchedDV = updater.getMatchedDriverVerion(bv);
//		System.out.println("the matched driver version is "+matchedDV);
//
//		String downloadURL = updater.getDriverDownloadURL(matchedDV);
//		System.out.println("download the driver from URL "+downloadURL);

	}
}
