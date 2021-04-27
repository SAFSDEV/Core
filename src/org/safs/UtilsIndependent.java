/**
* Copyright (C) SAS Institute, All rights reserved.fsa
* General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
**/


/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * @date 2021-04-27    (Lei Wang) Initial release: Moved some third-party-jar-independent-methods from 'org.safs.Utils' and 'org.safs.selenium.webdriver.lib.WDLibrary'.
 */
package org.safs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.safs.Constants.BrowserConstants;
import org.safs.android.auto.lib.Console;
import org.safs.tools.GenericProcessMonitor;
import org.safs.tools.GenericProcessMonitor.ProcessInfo;
import org.safs.tools.GenericProcessMonitor.SearchCondition;
import org.safs.tools.GenericProcessMonitor.UnixProcessSearchCondition;
import org.safs.tools.GenericProcessMonitor.WQLSearchCondition;

/**
 * This class contains some utility methods, and they must be independent from third-party jar files.
 *
 * @author Lei Wang
 *
 */
public class UtilsIndependent {

	/**
	 * @param url String, the URL to verify
	 * @return boolean if the URL exist, return true.
	 */
	public static boolean isURLExist(String url){
		String debugmsg = StringUtils.debugmsg(false);
		boolean exist = false;
		try {
			HttpURLConnection huc = (HttpURLConnection) URI.create(url).toURL().openConnection();
			huc.setRequestMethod("HEAD");
			//response code is 200, then OK.
			exist = (HttpURLConnection.HTTP_OK==huc.getResponseCode());
		} catch (Exception e) {
			IndependantLog.error(debugmsg+ "Met "+e.toString());
		}

		return exist;
	}

	/**
	 * Download the content from an URL and save it to a local file.
	 *
	 * @param url String, the URL to download
	 * @param outfile File, the destination file
	 * @throws IOException
	 */
	public static void downloadURL(String url, File outfile) throws IOException{
		downloadURL(url, outfile, true);
	}

	/**
	 * Download the content from an URL and save it to a local file.
	 *
	 * @param url String, the URL to download
	 * @param outfile File, the destination file
	 * @param checkContent boolean, if need to check the response content
	 * @throws IOException
	 */
	public static void downloadURL(String url, File outfile, boolean checkContent) throws IOException{
		String debugmsg = StringUtils.debugmsg(false);
		InputStream in = null;
		BufferedOutputStream fout = null;
		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection) URI.create(url).toURL().openConnection();
			int response = con.getResponseCode();
			if(response != HttpURLConnection.HTTP_OK)
				throw new MalformedURLException("Bad Server Response ("+ response +") for "+ url);
			if(checkContent){
				long conlength = con.getContentLengthLong();
				if(conlength < 1024)
					throw new MalformedURLException("Suspect content length ("+ conlength +") for "+ url);
			}
			//System.out.println("Response: "+ response);
			//System.out.println("Length: "+ conlength);
			in = con.getInputStream();
			fout = new BufferedOutputStream(new FileOutputStream(outfile), 1000 * 1024);
			byte data[] = new byte[1000 * 1024];
			int count;
			while((count = in.read(data))!= -1){
				fout.write(data, 0, count);
			}
			fout.flush();
		} catch (Exception e) {
			IndependantLog.error(debugmsg+"Unable to retrieve URL "+ url +" due to "+ e.getClass().getName()+": "+e.getMessage());
			throw e;
		}

		finally{
			if(con != null)try{ con.disconnect();}catch(Exception ignore){}
			if(in != null)try{ in.close();}catch(Exception ignore){}
			if(fout != null)try{ fout.close();}catch(Exception ignore){}
		}
	}


	/**
	 * Kill the process 'geckodriver.exe/geckodriver_64.exe' on windows, or "geckodriver/geckodriver_64" on linux.
	 * @param host String, the name of the machine on which the process 'geckodriver.exe' will be killed.
	 * @return List<ProcessInfo>, a list containing the killed process's id and kill-execution-return-code.
	 * @throws SAFSException
	 */
	public static List<ProcessInfo> killGeckoDriver(String host) throws SAFSException{
		//kill both "geckodriver" and "geckodriver_64" processes, we don't know 32 or 64 bit driver is running.
		List<ProcessInfo> killedProcesses = new ArrayList<ProcessInfo>();
		String process = "geckodriver";
		if(Console.isWindowsOS()) process += ".exe";
		killedProcesses.addAll(killExtraProcess(host, process));

		if(Console.is64BitOS()){
			process = "geckodriver_64";
			if(Console.isWindowsOS()) process += ".exe";
			killedProcesses.addAll(killExtraProcess(host, process));
		}

		return killedProcesses;
	}

	/**
	 * Kill the process 'chromedriver.exe' on windows, or "chromedrver" on linux.
	 * @param host String, the name of the machine on which the process 'chromedriver.exe' will be killed.
	 * @return List<ProcessInfo>, a list containing the killed process's id and kill-execution-return-code.
	 * @throws SAFSException
	 */
	public static List<ProcessInfo> killChromeDriver(String host) throws SAFSException{
		String process = "chromedriver";
		if(Console.isWindowsOS()) process += ".exe";
		return killExtraProcess(host, process);
	}

	/**
	 * Kill the process 'IEDriverServer.exe'.
	 * @param host String, the name of the machine on which the process 'IEDriverServer.exe' will be killed.
	 * @return List<ProcessInfo>, a list containing the killed process's id and kill-execution-return-code.
	 * @throws SAFSException
	 */
	public static List<ProcessInfo> killIEDriverServer(String host) throws SAFSException{
		return killExtraProcess(host, "IEDriverServer.exe");
	}

	/**
	 * Kill the process 'MicrosoftWebDriver.exe'.
	 * @param host String, the name of the machine on which the process 'MicrosoftWebDriver.exe' will be killed.
	 * @return List<ProcessInfo>, a list containing the killed process's id and kill-execution-return-code.
	 * @throws SAFSException
	 */
	public static List<ProcessInfo> killMicrosoftWebDriver(String host) throws SAFSException{
		return killExtraProcess(host, "MicrosoftWebDriver.exe");
	}

	/**
	 * Kill the process 'msedgedriver.exe'.
	 * @param host String, the name of the machine on which the process 'msedgedriver.exe' will be killed.
	 * @return List<ProcessInfo>, a list containing the killed process's id and kill-execution-return-code.
	 * @throws SAFSException
	 */
	public static List<ProcessInfo> killMSEdgeDriver(String host) throws SAFSException{
		return killExtraProcess(host, "msedgedriver.exe");
	}

	/**
	 * Kill the driver process by browserName.
	 * @param host String, the name of the machine on which the driver process will be killed.
	 * @param browserName String, the name of the browser for which the driver process should be killed.
	 * @return List<ProcessInfo>, a list containing the killed process's id and kill-execution-return-code.
	 * @throws SAFSException
	 */
	public static List<ProcessInfo> killBrowserDriver(String host, String browserName) throws SAFSException{
		List<ProcessInfo> processList = null;
		if(BrowserConstants.BROWSER_NAME_CHROME.equalsIgnoreCase(browserName)){
			processList = killChromeDriver(host);
		}else if(BrowserConstants.BROWSER_NAME_FIREFOX.equalsIgnoreCase(browserName)){
			processList = killGeckoDriver(host);
		}else if(BrowserConstants.BROWSER_NAME_IE.equalsIgnoreCase(browserName)){
			processList = killIEDriverServer(host);
		}else if(BrowserConstants.BROWSER_NAME_EDGE.equalsIgnoreCase(browserName)){
			processList = killMicrosoftWebDriver(host);
		}else if(BrowserConstants.BROWSER_NAME_CHROMIUM_EDGE.equalsIgnoreCase(browserName)){
			processList = killMSEdgeDriver(host);
		}else{
			IndependantLog.warn(StringUtils.debugmsg(false)+"Not implemented for browser '"+browserName+"'.");
		}

		return processList;
	}

	/**
	 * Kill the process launched from executables located in %SAFSDIR%\samples\Selenium2.0\extra\ or %SELENIUM_PLUS%\extra\
	 * @param host String, the name of machine on which the process will be killed.
	 * @param processName String, the name of process to kill. like chromedriver.exe, IEDriverServer.exe etc.
	 * @return List<ProcessInfo>, a list containing the killed process's id and kill-execution-return-code.
	 * @throws SAFSException
	 */
	public static List<ProcessInfo> killExtraProcess(String host, String processName) throws SAFSException{
		if(!StringUtils.isValid(processName)){
			throw new SAFSParamException("The value of parameter 'processName' is NOT valid: "+processName);
		}
		IndependantLog.debug("WDLibrary.killExtraProcess(): killing process '"+processName+"' on machine '"+host+"'.");

		String searchCondition = null;
		SearchCondition condition = null;
		if(Console.isWindowsOS()){
			//wmic process where " commandline like '%d:\\seleniumplus\\extra\\chromedriver.exe%' and name = 'chromedriver.exe' "
			searchCondition = GenericProcessMonitor.wqlCondition("commandline", File.separator+"extra"+File.separator+processName, true, false);
			searchCondition += " and "+ GenericProcessMonitor.wqlCondition("name", processName, false, false);
			condition = new WQLSearchCondition(searchCondition);
		}else if(Console.isUnixOS()){
			//we use unix grep command to search process
			//-f                   full-format, including command lines
			//-C <command>         command name
			searchCondition = " ps -f -C "+processName;
			//the first line is "UID        PID  PPID  C STIME TTY          TIME CMD"
			//We use the 'tail' command to remove the first heading line so that only process list is left
			searchCondition += " | tail -n +2 ";
			//-i, --ignore-case         ignore case distinctions
			searchCondition += " | grep -i "+File.separator+"extra"+File.separator+processName;
			//Finally, use the 'awk' to get the process id list, the second field is the 'PID'
			searchCondition += " | awk '{print $2}' ";

			//"ps -f -C command | tail -n +2 | grep -i commandline | grep -v notCommandline | awk '{print $2}'"
			condition = new UnixProcessSearchCondition(searchCondition);
		}else{
			throw new SAFSException(Console.getOsFamilyName() +" has NOT been supported yet.");
		}

		return GenericProcessMonitor.shutdownProcess(host, condition);
	}

	/**
	 * Kill the process according to the process name and partial command line.
	 * @param host String, the name of machine on which the process will be killed.
	 * @param processName String, the name of process to kill. like chromedriver.exe, IEDriverServer.exe, java.exe etc.
	 * @param commandline String, the partial commandline of the process to be killed.
	 * @return List<ProcessInfo>, a list containing the killed process's id and kill-execution-return-code.
	 * @throws SAFSException
	 */
	public static List<ProcessInfo> killProcess(String host, String processName, String commandline) throws SAFSException{
		return killProcess(host, processName, commandline, null);
	}

	/**
	 * Kill the process according to the process name and partial command line.
	 * @param host String, the name of machine on which the process will be killed.
	 * @param processName String, the name of process to kill. like chromedriver.exe, IEDriverServer.exe, java.exe etc.
	 * @param commandline String, the partial commandline of the process to be killed.
	 * @param notCommandline String, the partial commandline of the process NOT to be killed.
	 * @return List<ProcessInfo>, a list containing the killed process's id and kill-execution-return-code.
	 * @throws SAFSException
	 */
	public static List<ProcessInfo> killProcess(String host, String processName, String commandline, String notCommandline) throws SAFSException{
		if(!StringUtils.isValid(processName)){
			throw new SAFSParamException("The value of parameter 'processName' is NOT valid: "+processName);
		}
		IndependantLog.debug("WDLibrary.killProcess(): killing process '"+processName+"' containing commandline '"+commandline+"' on machine '"+host+"'.");

		SearchCondition condition = null;
		String searchCondition = null;
		if(Console.isWindowsOS()){
			searchCondition = GenericProcessMonitor.wqlCondition("commandline", commandline, true, false);
			if (notCommandline != null) {
				searchCondition += " and not "+ GenericProcessMonitor.wqlCondition("commandline", notCommandline, true, false);
			}
			searchCondition += " and "+ GenericProcessMonitor.wqlCondition("name", processName, false, false);
			condition = new WQLSearchCondition(searchCondition);

		}else if(Console.isUnixOS()){
			//we use unix grep command to search process
			//-f                   full-format, including command lines
			//-C <command>         command name
			searchCondition = " ps -f -C "+processName;
			//the first line is "UID        PID  PPID  C STIME TTY          TIME CMD"
			//We use the 'tail' command to remove the first heading line so that only process list is left
			searchCondition += " | tail -n +2 ";
			//-i, --ignore-case         ignore case distinctions
			searchCondition += " | grep -i "+commandline;
			//-v, --invert-match        select non-matching lines
			if (notCommandline != null) {
				searchCondition += " | grep -v "+notCommandline;
			}
			//Finally, use the 'awk' to get the process id list
			searchCondition += " | awk '{print $2}' ";

			//"ps -f -C command | tail -n +2 | grep -i commandline | grep -v notCommandline | awk '{print $2}'"
			condition = new UnixProcessSearchCondition(searchCondition);
		}else{
			throw new SAFSException(Console.getOsFamilyName() +" has NOT been supported yet.");
		}

		IndependantLog.debug("WDLibrary.killProcess(): searchCondition="+condition);

		return GenericProcessMonitor.shutdownProcess(host, condition);
	}
}
