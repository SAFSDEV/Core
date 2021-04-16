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
 * @date 2020-09-29    (Lei Wang) Initial release.
 * @date 2020-11-27	   (Lei Wang) Break the version into small parts and compare each part.
 */
package org.safs.selenium.util;

import java.io.File;
import java.nio.file.Files;

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.Utils;
import org.safs.selenium.webdriver.lib.SelectBrowser;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.text.FileUtilities;

/**
 * @author Lei Wang
 *
 */
public abstract class AbstractDriverUpdater implements BrowserDriverUpdater{

	/**
	 * The browser's vendor name, such as "chrome", "firefox", "edge" etc.
	 */
	protected String vendor = null;

	/** indicate if the firefox is 32 bits */
	protected Boolean isBrowser32Bit = false;

	@Override
	public boolean isBrowser32Bit(){
		return isBrowser32Bit;
	}

	@Override
	public String downloadBrowserDriver(String driverVersion) {
		String debugmsg = StringUtils.debugmsg(false);
		String driverFolder = null;
		String url = getDriverDownloadURL(driverVersion);

		File driverZipFile = null;
		try {
			driverZipFile = Files.createTempFile(vendor+"_driver", ".zip").toFile();
			IndependantLog.debug(debugmsg+" downloading URL '"+url+"' to file "+driverZipFile.getCanonicalPath());
			Utils.downloadURL(url, driverZipFile);
			IndependantLog.debug(debugmsg+" download completed");

			//Save the matched browser-driver in "SeleniumPlus\extra\drivers\<vendor>\<version>".
			SePlusInstallInfo seplusinfo = SePlusInstallInfo.instance();
			File seplusExtra = seplusinfo.getExtraDir();
			File driverDir = new File(seplusExtra, "drivers"+File.separator+vendor+File.separator+driverVersion);
			driverFolder = driverDir.getCanonicalPath();
			IndependantLog.debug(debugmsg+" unzipping the driver zip file to directory "+driverDir);
			FileUtilities.unzipFile(driverZipFile.getCanonicalPath(), driverDir, true);

		} catch (Exception e) {
			IndependantLog.error(debugmsg+"Failed, Met "+e.toString());
		}finally{
			if(driverZipFile!=null) try{ driverZipFile.delete(); }catch(Exception e){}
		}

		return driverFolder;
	}

	@Override
	public boolean update(){
		try{
			System.out.println("****************************** TRYING TO UPDATE THE "+vendor+" driver ****************************** ");
			String driverVersion = getDriverVersion();
			System.out.println("The current "+vendor+" driver version is "+driverVersion);

			String browserVersion = getBrowserVersion();
			System.out.println("The current "+vendor+" version is "+browserVersion);

			String matchedDriverVersion = getMatchedDriverVerion(browserVersion);
			System.out.println("The matched "+vendor+" driver version is "+matchedDriverVersion);

			//if the matched driver is the same as the current driver, we will not download the driver
			if(matchedDriverVersion.equals(driverVersion)){
				System.out.println("The current "+vendor+" driver's version '"+driverVersion+"' is the same "
						+ "as the current driver's version '"+matchedDriverVersion+"', do not need update.\n");
				return true;
			}

			String downloadedDriverFolder = downloadBrowserDriver(matchedDriverVersion);
			if(downloadedDriverFolder==null){
				System.err.println("Failed to download the matched "+vendor+" driver (version:"+matchedDriverVersion+")!");
				return false;
			}
			System.out.println("The matched "+vendor+" driver has been downloaded to folder "+downloadedDriverFolder);

			//We need to compare the current driver's version and the matched driver's version to know if we should update
			boolean currentDriverIsNewer = true;
			try{
				currentDriverIsNewer = new Version(matchedDriverVersion).compareTo(new Version(driverVersion))<0;
			}catch(Exception e){
				currentDriverIsNewer = matchedDriverVersion.compareTo(driverVersion)<0;
			}
			if(currentDriverIsNewer){
				System.out.println("The current "+vendor+" driver's version '"+driverVersion+"' is newer than the downloaded driver's version '"+matchedDriverVersion+"'\n");
				System.out.println("We will not replace the current driver by the downloaded driver, user can do it manually.");
				return true;
			}

			//Copy the original driver to "SeleniumPlus\extra\drivers\<vendor>\backup".
			//and copy the latest driver to the folder "SeleniumPlus\extra\" to replace the original one
			WDLibrary.killBrowserDriver(null, vendor);//Kill the running process of the browser-driver, so that we can replace it by the latest driver.
			SePlusInstallInfo seplusinfo = SePlusInstallInfo.instance();
			File originalDriver = seplusinfo.getDriver(vendor);
			String driverName = originalDriver.getName();
			File driverBackUpDir = new File(seplusinfo.getExtraDir(), "drivers"+File.separator+vendor+File.separator+"backup");
			if(!driverBackUpDir.exists()) driverBackUpDir.mkdirs();
			File backupDriver = new File(driverBackUpDir, driverName);
			if(backupDriver.exists()) backupDriver.delete();
			FileUtilities.copyFileToFile(originalDriver, backupDriver);
			System.out.println("Copied original driver '"+originalDriver+"' to '"+backupDriver+"'.");
			originalDriver.delete();
			String downloadedDriverName = driverName;
			if(!isBrowser32Bit()){
				//if the browser is 64 bit, for Firefox, the driver name will be "geckodriver_64.exe" or "geckodriver_64" in SeleniumPlus
				//but the downloaded firefox driver name is just "geckodriver.exe" or "geckodriver", we should remove the suffix "_64"
				int index = downloadedDriverName.indexOf("_64");
				if(index>-1){
					downloadedDriverName = downloadedDriverName.substring(0, index)+downloadedDriverName.substring((index+"_64".length()));
				}
			}
			File matchedDriver = new File(downloadedDriverFolder, downloadedDriverName);
			FileUtilities.copyFileToFile(matchedDriver, originalDriver);
			System.out.println("Copied the matched driver '"+matchedDriver+"' to '"+originalDriver+"'.");
			System.out.println("****************************** Please stop the Selenium-Server Console and run the test again! ******************************\n");

			return true;

		}catch(Exception e){
			System.err.println("****************************** Failed to update the "+vendor+" driver, due to "+e.toString()+" ******************************\n");
			return false;
		}

	}

	public static BrowserDriverUpdater instance(String browserName){
		String debugmsg = StringUtils.debugmsg(false);
		BrowserDriverUpdater updater = null;

		if(SelectBrowser.BROWSER_NAME_CHROME.equalsIgnoreCase(browserName)){
			updater = new ChromeDriverUpdater();
		}else if(SelectBrowser.BROWSER_NAME_FIREFOX.equalsIgnoreCase(browserName)){
			updater = new FirefoxDriverUpdater();
		}else if(SelectBrowser.BROWSER_NAME_CHROMIUM_EDGE.equalsIgnoreCase(browserName)){
			updater = new MSEdgeDriverUpdater();
		}else{
			//TODO return updater for other browsers
			IndependantLog.warn(debugmsg+" Not implemented for browser '"+browserName+"'.");
		}

		return updater;
	}
}

class Version implements Comparable<Version> {

    private String version;

    public Version(String version) {
        if(version == null)
            throw new IllegalArgumentException("Version can not be null");
        if(!version.matches("[0-9]+(\\.[0-9]+)*"))
            throw new IllegalArgumentException("Invalid version format");
        this.version = version;
    }

    public final String get() {
    	return this.version;
    }

    @Override
    public int compareTo(Version version) {
        if(version == null)
            return 1;
        String[] thisParts = this.get().split("\\.");
        String[] thatParts = version.get().split("\\.");
        int length = Math.max(thisParts.length, thatParts.length);
        for(int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ? Integer.parseInt(thisParts[i]) : 0;
            int thatPart = i < thatParts.length ? Integer.parseInt(thatParts[i]) : 0;
            if(thisPart < thatPart)
                return -1;
            if(thisPart > thatPart)
                return 1;
        }
        return 0;
    }

}
