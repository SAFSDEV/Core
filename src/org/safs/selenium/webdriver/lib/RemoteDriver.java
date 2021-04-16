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
package org.safs.selenium.webdriver.lib;

/**
* History:<br>
*
*  <br>   DEC 19, 2013    (DHARMESH4) Initial release.
*  <br>	  JAN 16, 2014    (DHARMESH4) Updated reconnection browser support.
*  <br>	  APR 09, 2014 	  (DHARMESH4) Fixed javascript support for webdriver reconnection.
*  <br>	  SEP 04, 2014    (Lei Wang) Store Capabilities's firefoxProfile info to session file.
*  <br>   OCT 30, 2014    (Carl Nagle) Initial fix for IE JSON problems after new page load/redirection.
*  <br>	  MAR 17, 2015    (Lei Wang) Store Capabilities's chrome custom data info to session file.
*  <br>   JUN 29, 2015	  (Lei Wang) Get the RMI Server from the selenium-grid-node machine.
*                                   Add main(): as a entry point for starting Remote Server (standalone or grid).
*  <br>   DEC 24, 2015	  (Lei Wang) Add methods to get browser's name, version, and selenium-server's version etc.
*  <br>   FEB 29, 2016	  (Lei Wang) Remove the import of org.seleniumhq.jetty7.util.ajax.JSON
*  <br>   MAR 07, 2016	  (Lei Wang) Handle firefox preference.
*  <br>   SEP 27, 2016	  (Lei Wang) Modified main(): Added parameter "-project"/"-Dselenium.project.location", and
*                                   adjusted the java doc.
*                                   Wrote debug message to a file on disk c.
*  <br>   FEB 27, 2016	  (Lei Wang) Modified startSession(): quit the appropriate webdriver when cleaning up.
*  <br>   MAY 05, 2017	  (Lei Wang) Added isConnectionFine(): test the connection according the execution time of a certain selenium driver command.
*  <br>   JUL 19, 2017	  (Lei Wang) Added filed 'capabilities', method init() and getCapabilities().
*                                   Modified constructors to call init() after calling super().
*                                   Removed 'static' modifier for field 'newSession'.
*                                   We solved 2 problems: 1. capabilities is null if we reconnect a session.
*                                                         2. newSession cannot be correctly set if we reconnect a session.
*  <br>   JUL 20, 2017	  (Lei Wang) Stored 2 more parameters (browser-version, platform) into session file.
*                                   Restore parameters into current capabilities when reconnecting a session.
*  <br>   AUG 08, 2017	  (Lei Wang) Modified startSession(): Delete obsolete session from session-file even no WebDriver is got from cache.
*                                                            Set the Executor to original after 'clean up', otherwise it will prevent new session being started.
*                                   For Selenium 3.X:
*                                     Override method setSessionId(): reset 'CommandExecutor' from cache if reconnecting a session.
*                                     Modified storeSession(): store the 'CommandExecutor' into a cache.
*  <br>   AUG 09, 2017	  (Lei Wang) Modified quit(): catch exception when calling super.quit(), selenium 3.4 & firefox 53.0 & gecko v0.18.0 will throw WebDriverException
*  <br>   JUL 31, 2018	  (Lei Wang) Modified quit(): check null carefully
*                                                    write the exception's stack trace into debug log so that we can examine easily.
*  <br>   FEB 14, 2019	  (Lei Wang) Modified startSession(): Removed the second parameter 'Capabilities requiredCapabilities'.
*                                                            Selenium removed the method startSession(Capabilities desiredCapabilities, Capabilities requiredCapabilities) at commit https://github.com/SeleniumHQ/selenium/commit/05151a4aa10d795bc49f0027c5f8d35384abf55e#diff-fa76272f866d8067bb11a32f27773026
*                                                            I am upgrading SE+ to use selenium-server-standalone-3.14.0.jar, which doesn't contain that method in RemoteWebDriver.
*  <br>   APR 30, 2019	  (Lei Wang) Modified startSession(): Commented out the code of "checking obsolete sessions", see S1502701.
*                                                            I will let this code out when I find a way to clear the "restored session" from current WebDriver.
*  <br>   JUN 04, 2019	  (Lei Wang) Supported the special command 'setNetworkConditions' related to the chrome browser:
*                                   Modified constructor RemoteDriver(): move code of "starting RMI Agent" to method init().
*                                   Modified init(): Execute special command 'setNetworkConditions' related to the chrome browser.
*                                   Created factory method instance(): to create appropriate RemoteWebDriver.
*  <br>   JUN 20, 2019	  (Lei Wang) Added a key 'setNetworkConditions' to SessionInfo.optionalKeys: store 'network conditions' into session file.
*  <br>   JUN 21, 2019	  (Lei Wang) Modified restoreSession(): define additional commands for ChromeHttpCommandExecutor.
*  <br>   AUG 15, 2019	  (Lei Wang) Modified restoreSession(): set commandExecutor's codec by trying the "codec stored in session", "w3c codec" and "json codec" one by one.
*  <br>   DEC 12, 2019	  (Lei Wang) Modified init():  we will use the "localhost" as the rmi-host if "rmi.port.forward" is set to true, even we detect the "node" is running remotely.
*                                   Modified isLocalServer(): we will return false, if "rmi.port.forward" is set to true. Even we map the "remote port" to "local port", but the "RMI server" is still running on a remote machine.
*  <br>   FEB 27, 2020	  (Lei Wang) JAVA_TMPDIR gives a path without ending file-separator on Linux, we failed to create session file with that path. see S1562137
*
*/
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.internal.WebElementToJsonConverter;
import org.safs.Constants.BrowserConstants;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.Utils;
import org.safs.net.NetUtilities;
import org.safs.selenium.rmi.agent.SeleniumAgent;
import org.safs.selenium.util.GridInfoExtractor;
import org.safs.selenium.util.SeleniumServerRunner;
import org.safs.selenium.webdriver.WebDriverGUIUtilities;
import org.safs.sockets.DebugListener;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverConstant.SeleniumConfigConstant;
import org.safs.tools.stringutils.StringUtilities;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Handle the Session information {@link SessionInfo}.<br>
 * Handle a SeleniumRMIAgent, if enabled, to communicate with a remote SAFS Selenium RMI Server.<br>
 *
 */
public class RemoteDriver extends RemoteWebDriver implements TakesScreenshot {

	private static boolean logDriverCommand = false;
	private boolean newSession = true;
	private boolean _quit = false;

	/** The URL provided at construction time. */
	public URL remote_URL = null;
	/**
	 * parsed hostname of the remote selenium server (standalone or grid-hub)
	 * might be needed for additional Selenium RMI Server communication. */
	public String remote_hostname = null;
	/**
	 * parsed port number of the remote selenium server (standalone or grid-hub)
	 * might be needed for get grid-node information. */
	public int remote_port = SeleniumConfigConstant.DEFAULT_SELENIUM_PORT_INT;

	/**
	 * hostname of the remote grid-node.
	 * might be needed for additional Selenium RMI Server communication. */
	public String rmi_hostname = null;

	//default registry port to look for RMI
	public int rmi_registry_port = SeleniumConfigConstant.DEFAULT_REGISTRY_PORT;

	/**
	 * A SeleniumRMIAgent, if enabled, to communicate with a remote SAFS Selenium RMI Server.<br>
	 * This may be null if we could not initialize an agent or find a server.<br>
	 * This may also be non-null, but not connected to an RMI server if the server is not running.
	 * <p>
	 * @see org.safs.selenium.rmi.agent.SeleniumAgent
	 * @see org.safs.selenium.rmi.agent.SeleniumAgent#server
	 * @see org.safs.selenium.rmi.agent.SeleniumRMIAgent
	 * @see org.safs.selenium.rmi.server.SeleniumRMIServer
	 */
	public SeleniumAgent rmiAgent = null;

	/**
	 * "_SP_", used to separate fields (such as sessionid, browser, platform etc.) stored for a session
	 */
	private final static String SPLITTER = "_SP_";
	/** "LASTSESSION" */
	public static final String LAST_SESSION_KEY = "LASTSESSION";
	/** "java.io.tmpdir" */
	public static final String JAVA_TMPDIR = "java.io.tmpdir";
	/** "selenium.session.tmp" */
	public static final String SESSION_FILE = "selenium.session.tmp";
	/** "ID" */
	public static final String CAPABILITY_ID = "ID";
	/** "RECONNECT" */
	public static final String CAPABILITY_RECONNECT = "RECONNECT";
	/** "REMOTESERVER" */
	public static final String CAPABILITY_REMOTESERVER = "REMOTESERVER";

	/**if this is a grid-hub server*/
	protected boolean isGrid = false;

	/**
	 * In superclass RemoteWebDriver, there is a private field named 'capabilities', which is set in the method
	 * {@link #startSession(Capabilities, Capabilities)}; But this {@link #startSession(Capabilities, Capabilities)} has been
	 * overridden in this class. If we reconnect a session, we will not call the super.startSession() and the private field
	 * 'capabilities' will not be set.<br>
	 * Here a protected field with the same name 'capabilities' is created and will be set in method {@link #init(Capabilities)}
	 * and it can be got by the overridden method {@link #getCapabilities()}.
	 *
	 * @see #getCapabilities()
	 * @see #init(Capabilities)
	 * @see #RemoteDriver(DesiredCapabilities)
	 * @see #RemoteDriver(URL, DesiredCapabilities)
	 */
	protected Capabilities capabilities = null;

	/**
	 * Not used by SAFS Selenium.<br>
	 * @param capabilities
	 * @see #RemoteDriver(URL, DesiredCapabilities)
	 */
	public RemoteDriver(DesiredCapabilities capabilities){
		super(capabilities);
		init(null, capabilities);
	}
	/**
	 * Connect to a remote selenium standalone server.<br>
	 * We will also parse the URL to attempt to connect to a SAFS Selenium RMI Server on RMI-SERVER-HOST.<br>
	 * The RMI-SERVER-HOST can be:<br>
	 * 1. The same host as this Selenium Server (Standalone).<br>
	 * 2. The node host assigned by this Selenium Server (grid-hub).<br>
	 * @param selenium_server_url -- URL of the remote Selenium Server.
	 * <p>
	 * @param capabilities -- to enable the possibility of SAFS Selenium RMI Server/Agent communication
	 * you must set a "capability" of REMOTESERVER to be the hostname of the remote selenium server.
	 * <p>
	 * Ex: capabilities.setCapability(RemoteDriver.CAPABILITY_REMOTESERVER, hostname);
	 * <p>
	 * This is done automatically within the SAFS Selenium code where needed.  Only advanced users doing
	 * custom initialization and instantiation need set this capability.<br>
	 *
	 * <b>NOTE: Please call {@link #instance(URL, DesiredCapabilities)} instead. This constructor will be non-visible.</b>
	 */
	public RemoteDriver(URL selenium_server_url, DesiredCapabilities capabilities){
		super(selenium_server_url, capabilities);
		init(selenium_server_url, capabilities);
	}
	/**
	 * @param command HttpCommandExecutor, the CommandExecutor used to create WebDriver
	 * @param capabilities DesiredCapabilities, the capabilities used to create WebDriver
	 * @see #instance(URL, DesiredCapabilities)
	 */
	RemoteDriver(HttpCommandExecutor command, DesiredCapabilities capabilities){
		super(command, capabilities);
		init(command.getAddressOfRemoteServer(), capabilities);
	}

	/**
	 * Connect to a remote selenium standalone server.<br>
	 * We will also parse the URL to attempt to connect to a SAFS Selenium RMI Server on RMI-SERVER-HOST.<br>
	 * The RMI-SERVER-HOST can be:<br>
	 * 1. The same host as this Selenium Server (Standalone).<br>
	 * 2. The node host assigned by this Selenium Server (grid-hub).<br>
	 * @param selenium_server_url -- URL of the remote Selenium Server.
	 * <p>
	 * @param capabilities DesiredCapabilities, the capabilities used to create WebDriver.<br>
	 * To enable the possibility of SAFS Selenium RMI Server/Agent communication
	 * you must set a "capability" of REMOTESERVER to be the hostname of the remote selenium server.
	 * <p>
	 * Ex: capabilities.setCapability(RemoteDriver.CAPABILITY_REMOTESERVER, hostname);
	 * <p>
	 * This is done automatically within the SAFS Selenium code where needed.  Only advanced users doing
	 * custom initialization and instantiation need set this capability.<br>
	 * <p>
	 * To enable the possibility of control the network-conditions for chrome browser
	 * you must set a "capability" of {@link BrowserConstants#KEY_SET_NETWORK_CONDITIONS} to a initial network-conditions (it can be an empty string).
	 * <p>
	 * Ex: capabilities.setCapability(ChromeHttpCommandExecutor.SET_NETWORK_CONDITIONS, "");
	 * Ex: capabilities.setCapability(ChromeHttpCommandExecutor.SET_NETWORK_CONDITIONS, "{"offline":false, "latency":5, "download_throughput":5000 , "upload_throughput":5000}");
	 * <p>
	 *
	 */
	public static RemoteDriver instance(URL selenium_server_url, DesiredCapabilities capabilities){
		RemoteDriver remotedriver = null;

		if(ChromeHttpCommandExecutor.isRequired(capabilities)){
			remotedriver = new RemoteDriver(new ChromeHttpCommandExecutor(selenium_server_url),capabilities);
		}else{
			remotedriver = new RemoteDriver(selenium_server_url, capabilities);
		}

		return remotedriver;
	}

	@Override
	public Capabilities getCapabilities() {
		return capabilities;
	}

	/**
	 * This method will initialize some local fields:
	 * <ul>
	 * <li>{@link #remote_URL}
	 * <li>{@link #newSession}
	 * <li>{@link #capabilities}
	 * <li>{@link #remote_hostname}
	 * <li>{@link #rmi_hostname}
	 * </ul>
	 * This method will also store session-information into a session file<br>
	 * and restore session-information from a session file.<br>
	 * <p>
	 * We will also parse the URL to attempt to connect to a SAFS Selenium RMI Server on RMI-SERVER-HOST.<br>
	 * The RMI-SERVER-HOST can be:<br>
	 * 1. The same host as this Selenium Server (Standalone).<br>
	 * 2. The node host assigned by this Selenium Server (grid-hub).<br>
	 * <p>
	 * We will also try to execute special command 'setNetworkConditions' related to the chrome browser.
	 * <p>
	 *
	 * @param seleniumServerURL URL, the URL representing the selenium server, such as new URL("http://localhost:4444/wd/hub")
	 * @param myCapabilities Capabilities to set
	 * @see #RemoteDriver(DesiredCapabilities)
	 * @see #RemoteDriver(URL, DesiredCapabilities)
	 * @see #instance(URL, DesiredCapabilities)
	 */
	protected void init(URL seleniumServerURL, Capabilities myCapabilities) {
		String debugmsg = StringUtils.debugmsg(false);
		DesiredCapabilities desiredCapabilities = null;

		parseSeleniumServerURL(seleniumServerURL);

		newSession = true;
		if(myCapabilities!=null){
			try{
				newSession = !Boolean.parseBoolean(myCapabilities.getCapability(CAPABILITY_RECONNECT).toString());
			}catch(Exception e){
				IndependantLog.warn(debugmsg+"Failed to detect session is '"+CAPABILITY_RECONNECT+"', due to "+e.toString());
			}
			desiredCapabilities = new DesiredCapabilities(myCapabilities);
		}else{
			desiredCapabilities = new DesiredCapabilities();
		}

		if(!newSession){
			//Restore information from the session file into Capabilities
			try {
				String ID = (String) myCapabilities.getCapability(CAPABILITY_ID);
				SessionInfo info = retrieveSessionInfoFromFile(ID);
				if (info != null){
					info.restore(desiredCapabilities);
				}else{
					IndependantLog.debug(debugmsg+"failed to get cached session of ID '"+ID+"'.");
				}
			} catch (Exception e) {
				IndependantLog.error(debugmsg+"Met "+e.toString());
			}
		}else{
			//After calling super class constructor, super class Capabilities should get loaded
			//We need to merge it with our desiredCapabilities
			desiredCapabilities.merge(super.getCapabilities());
			//Store information into the session file
			try {
				storeSession(getSessionId().toString(), desiredCapabilities);
			} catch (Exception e) {
				IndependantLog.debug("RemoteDriver error storing sessionid to file due to: "+ e);
			}
		}

		try{
			String remoteserver = (String) desiredCapabilities.getCapability(CAPABILITY_REMOTESERVER);
			if(remote_hostname == null) remote_hostname = remoteserver;
		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Failed to set field 'remote_hostname', due to "+e.toString());
		}

		capabilities = desiredCapabilities;

		//start the RMIAgent
		//if it is grid, we need to set RMIAgent with the RMIServer on the grid-node (not on the grid-hub)
		Object gridnodes = capabilities.getCapability(SelectBrowser.KEY_GRID_NODES_SETTING);
		if(gridnodes!=null && gridnodes instanceof String) isGrid = StringUtils.isValid((String)gridnodes);
		if(!isGrid) isGrid = WebDriverGUIUtilities.isGridRunning(remote_hostname, String.valueOf(remote_port));

		if(isGrid ){
			SessionId id = getSessionId();
			String[] node  = GridInfoExtractor.getHostNameAndPort(remote_hostname, remote_port, id);
			IndependantLog.debug(StringUtils.debugmsg(false)+"connected to grid NODE, host="+node[0]+"; port="+node[1]);
			rmi_hostname = node[0];
		}
		if(getRMIPortForward()){
			IndependantLog.debug("We are using 'RMI port forwarding', all docker container's ports have been mapped to localhost. So we changed the rmi_hostname from '"+rmi_hostname+"' to 'localhost'. ");
			rmi_hostname = "localhost";
		}

		try{
			rmi_registry_port = Integer.parseInt(System.getProperty(SeleniumConfigConstant.PROPERTY_REGISTRY_PORT));
		}catch(NumberFormatException nf){
			IndependantLog.warn("failed to set registry port, due to "+nf.toString()+"\nUse the default port "+SeleniumConfigConstant.DEFAULT_REGISTRY_PORT);
			rmi_registry_port = SeleniumConfigConstant.DEFAULT_REGISTRY_PORT;
		}

		startRMIAgent(rmi_hostname, rmi_registry_port);

		//Execute special command 'setNetworkConditions' related to the chrome browser
		if(SelectBrowser.BROWSER_NAME_CHROME.equalsIgnoreCase(capabilities.getBrowserName())){
			String networkConditions = String.valueOf(capabilities.getCapability(BrowserConstants.KEY_SET_NETWORK_CONDITIONS));
			WDLibrary.setNetworkConditions(this, networkConditions);
		}
	}

	private boolean getRMIPortForward(){
		return Boolean.parseBoolean(System.getProperty(SeleniumConfigConstant.PROPERTY_RMI_PORT_FORWARD));
	}

	/**
	 * @return String, the name of the browser where test is running. Or null if something wrong happens.
	 */
	public String getBrowserName(){
		String debugmsg = StringUtils.debugmsg(false);
		try{
			String name = getCapabilities().getBrowserName();
			IndependantLog.debug(debugmsg+name);
			return name;
		}catch(Exception e){
			IndependantLog.error(debugmsg+" Met "+StringUtils.debugmsg(e));
			try{
				JavascriptExecutor js = this;
				String useragent = (String)js.executeScript("return navigator.userAgent;");
				IndependantLog.debug("useragent is '"+useragent+"'");
			}catch(Exception e1){
				IndependantLog.error(debugmsg+" Met "+StringUtils.debugmsg(e1));
			}
			return null;
		}
	}

	/**
	 * @return String, the version of the browser where test is running. Or null if something wrong happens.
	 */
	public String getBrowserVersion(){
		String debugmsg = StringUtils.debugmsg(false);
		try{
			String version = getCapabilities().getVersion().toString();
			IndependantLog.debug(debugmsg+version);
			return version;
		}catch(Exception e){
			IndependantLog.error(debugmsg+" Met "+StringUtils.debugmsg(e));
			try{
				JavascriptExecutor js = this;
				String useragent = (String)js.executeScript("return navigator.userAgent;");
				IndependantLog.debug("useragent is '"+useragent+"'");
			}catch(Exception e1){
				IndependantLog.error(debugmsg+" Met "+StringUtils.debugmsg(e1));
			}
			return null;
		}
	}

	/**
	 * @return String, the name of the platform where the browser is running. Or null if something wrong happens.
	 */
	public String getPlatform(){
		String debugmsg = StringUtils.debugmsg(false);
		try{
			String platform = getCapabilities().getPlatform().toString();
			IndependantLog.debug(debugmsg+platform);
			return platform;
		}catch(Exception e){
			IndependantLog.error(debugmsg+" Met "+StringUtils.debugmsg(e));
			try{
				JavascriptExecutor js = this;
				String useragent = (String)js.executeScript("return navigator.userAgent;");
				IndependantLog.debug("useragent is '"+useragent+"'");
			}catch(Exception e1){
				IndependantLog.error(debugmsg+" Met "+StringUtils.debugmsg(e1));
			}
			return null;
		}
	}

	/**
	 * @return String, the version of 'selenium server' with which the test is running. Or null if something wrong happens.
	 */
	public String getDriverVersion(){
		String debugmsg = StringUtils.debugmsg(false);
		String hostname = null;
		String port = null;
		String version = null;

		if(isGrid ){
			//according to the session id, get the node hostname and port
			SessionId id = getSessionId();
			String[] node  = GridInfoExtractor.getHostNameAndPort(remote_hostname, remote_port, id);
			IndependantLog.debug(debugmsg+"connected to grid NODE, host="+node[0]+"; port="+node[1]);
			hostname = node[0];
			port = node[1];
		}else{
			hostname = remote_hostname;
			port = String.valueOf(remote_port);
		}

		String result = WebDriverGUIUtilities.readHubStaticURL(hostname, port);
		//result from error stream, it is something like following:
//		{"sessionId":null,
//			"status":13,
//			"state":"unhandled error",
//			"value":{
//			   "message":"GET /static/resource\nBuild info: version: '2.48.2', revision: '41bccdd', time: '2015-10-09 19:59:12'\nSystem info: host: 'xxx', ip: '172.27.17.89', os.name: 'Windows 7', os.arch: 'x86', os.version: '6.1', java.version: '1.7.0_45'\nDriver info: driver.version: unknown",
//			   "suppressed":[],
//			   "localizedMessage":"GET /static/resource\nBuild info: version: '2.48.2', revision: '41bccdd', time: '2015-10-09 19:59:12'\nSystem info: host: 'xxx', ip: '172.27.17.89', os.name: 'Windows 7', os.arch: 'x86', os.version: '6.1', java.version: '1.7.0_45'\nDriver info: driver.version: unknown","buildInformation":null,"cause":null,"systemInformation":"System info: host: 'xxx', ip: '172.27.17.89', os.name: 'Windows 7', os.arch: 'x86', os.version: '6.1', java.version: '1.7.0_45'","supportUrl":null,"class":"...(line truncated)...
//			   "additionalInformation":"\nDriver info: driver.version: unknown",
//			   "hCode":19885966,
//		       "stackTrace":[null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null]
//		     },
//			 "class":"org.openqa.selenium.remote.Response",
//			 "hCode":17632485}
		IndependantLog.debug(debugmsg+"Selenium Driver Information:\n"+result);
		try {
			JSONObject hubInformaiton = new JSONObject(result);
			String token = "version:";
//			"message":"GET /static/resource\nBuild info: version: '2.48.2', revision: '41bccdd', time: '2015-10-09 19:59:12'\nSystem info: host: 'xxx', ip: '172.27.17.89', os.name: 'Windows 7', os.arch: 'x86', os.version: '6.1', java.version: '1.7.0_45'\nDriver info: driver.version: unknown",
			version = hubInformaiton.getJSONObject("value").getString("message");
			int index = version.indexOf(token);
			if(index>-1){
				version = version.substring(index+token.length());
				index = version.indexOf(",");
				if(index>-1){
					version = version.substring(0, index);
				}
				version = StringUtilities.removeSingleQuotes(version.trim());
			}

			IndependantLog.debug(debugmsg+version);
		} catch (Exception e) {
			IndependantLog.error(debugmsg+" Met "+StringUtils.debugmsg(e));
		}

		return version;
	}

	/**
	 * try to start a SAFS Selenium RMI Agent if our "selenium server" is NOT on localhost or it is a "grid hub server".
	 * @param rmihost String, the host name of the RMI server
	 * @param rmiRegistryPort int, the port number of the registry for looking up the RMI server, default is 1099
	 */
	protected void startRMIAgent(String rmihost, int rmiRegistryPort){
		if(isLocalServer()) return;
		try{
			rmiAgent = new SeleniumAgent();
			rmiAgent.setServerHost(rmihost);
			rmiAgent.setRegistryPort(rmiRegistryPort);
			rmiAgent.initialize();
		}catch(RemoteException rx){
			rx.printStackTrace();
		}
	}

	/**
	 * @return true if our standalone Selenium Server is local (localhost), otherwise false.
	 */
	public boolean isLocalServer(){
		if(isGrid) return false;//If server is grid-hub, then we need RMI server. But if the node is local machine?
		if(getRMIPortForward()) return false;//If we use the "RMI port forward", even we map the "remote port" to "local port", but the "RMI server" is still running on a remote machine.
		return remote_hostname == null || NetUtilities.isLocalHost(remote_hostname);
	}

	/**
	 * Extract the needed remote_hostname and remote_port from the selenium server URL.
	 * @param URL - URL of the Selenium Server used.
	 */
	protected void parseSeleniumServerURL(URL url){
		remote_URL = url;
		if(url==null) return;

		try{
			remote_hostname = url.getHost();
			if(remote_hostname.startsWith("[")) remote_hostname = remote_hostname.substring(1);
			if(remote_hostname.endsWith("]")) remote_hostname = remote_hostname.substring(0, remote_hostname.length()-1);
			//Set the "RMI host" the same as "remote host"
			rmi_hostname = remote_hostname;
		}catch(NullPointerException np){
			// ignore
		}
		try{
			if(url.getPort()!=-1) remote_port = url.getPort();
		}catch(Exception e){
			remote_port = SeleniumConfigConstant.DEFAULT_SELENIUM_PORT_INT;
		}
	}

	@Override
	public void quit(){
		try{
			SessionId id = getSessionId();
			String sessionid = null;
			if(id!=null){
				sessionid = id.toString();
			}else{
				IndependantLog.warn("RemoteDriver.quit(): could not get session id!");
			}

			super.quit();
			IndependantLog.debug("RemoteDriver.quit(): has quitted session '"+sessionid+"'.");
			if(sessionid!=null){
				//Remove the 'CommnadExecutor' from the cache 'executorMap'.
				removeExecutor(sessionid);
			}
		}catch(Exception e){
			IndependantLog.error("RemoteDriver.quit(): met "+e.toString(), e);
		}
		try{ deleteSessionIdFromFile((String)this.getCapabilities().getCapability(CAPABILITY_ID));}
		catch(Throwable t){}
		try{ rmiAgent.disconnect();}
		catch(Throwable t){}
		_quit = true;
	}

	public boolean hasQuit(){ return _quit; }

	/**
	 * Started from Selenium 3.X: A Map to store CommandExecutor, the key is sessionID.
	 */
	protected static Map<String, CommandExecutor> executorMap = new HashMap<String, CommandExecutor>();

	protected synchronized static CommandExecutor removeExecutor(String sessionid){
		if(SeleniumServerRunner.isSelenium3X()){
			IndependantLog.debug("RemoteDriver.removeExecutor(): removing Executor for session '"+sessionid+"'");
			return executorMap.remove(sessionid);
		}
		return null;
	}
	protected synchronized static void storeExecutor(String sessionid, CommandExecutor executor){
		if(SeleniumServerRunner.isSelenium3X()){
			IndependantLog.debug("RemoteDriver.storeExecutor(): storing Executor '"+executor+"' for session '"+sessionid+"'");
			executorMap.put(sessionid, executor);
		}
	}
	protected synchronized static CommandExecutor getExecutor(String sessionid){
		if(SeleniumServerRunner.isSelenium3X()){
			return executorMap.get(sessionid);
		}
		return null;
	}

	/** "org.openqa.selenium.remote.http.JsonHttpCommandCodec" */
	protected final static String CLASSNAME_JSONHTTP_COMMANDCODEC 	= "org.openqa.selenium.remote.http.JsonHttpCommandCodec";
	/** "org.openqa.selenium.remote.http.JsonHttpResponseCodec" */
	protected final static String CLASSNAME_JSONHTTP_RESPONSECODEC = "org.openqa.selenium.remote.http.JsonHttpResponseCodec";
	/** "org.openqa.selenium.remote.http.W3CHttpCommandCodec" */
	protected final static String CLASSNAME_W3CHTTP_COMMANDCODEC 	= "org.openqa.selenium.remote.http.W3CHttpCommandCodec";
	/** "org.openqa.selenium.remote.http.W3CHttpResponseCodec" */
	protected final static String CLASSNAME_W3CHTTP_RESPONSECODEC 	= "org.openqa.selenium.remote.http.W3CHttpResponseCodec";

	/** "commandCodec" */
	protected final static String FIELDNAME_COMMANDCODEC 	= "commandCodec";
	/** "responseCodec" */
	protected final static String FIELDNAME_RESPONSECODEC 	= "responseCodec";

	//This method is called from startSession().
	//DO NOT modify any class field in this method, they will be re-initialized!!!
	private void restoreSession(SessionInfo sessionInfo){
		String debugmsg = "RemoteDriver.restoreSession(): ";
		String sessionid = sessionInfo.session;

		//Started from Selenium 3.X, setSessionId() is NOT enough to restore a session.
		//The HttpCommandExecutor will throw a WebDriverException if a command other than 'newSession' is called.
		setSessionId(sessionid);

		//We need also to
		//1. restore CommandExecutor form our cache 'executorMap'
		//2. OR update current CommandExecutor by adding 'CommandCodec' and 'ResponseCodec'
		if(SeleniumServerRunner.isSelenium3X()){
			CommandExecutor executor = getExecutor(sessionid);
			if(executor!=null){
				//restore CommandExecutor form our cache 'executorMap'
				IndependantLog.debug(debugmsg+" set Executor '"+executor+"' for session '"+sessionid+"'.");
				setCommandExecutor(executor);
			}else{
				IndependantLog.info(debugmsg+" failed to get Executor from internal cache 'executorMap' for session '"+sessionid+"'.");
				//update current CommandExecutor by adding 'CommandCodec' and 'ResponseCodec'
				executor = this.getCommandExecutor();

				LinkedHashMap<String/*fieldClassName*/, LinkedHashMap<Class<?>, Object>/*arguments*/> commandCodecArgs = new LinkedHashMap<String, LinkedHashMap<Class<?>, Object>>();
				LinkedHashMap<String/*fieldClassName*/, LinkedHashMap<Class<?>, Object>/*arguments*/> responseCodecArgs = new LinkedHashMap<String, LinkedHashMap<Class<?>, Object>>();
				LinkedHashMap<Class<?>, Object> arguments = new LinkedHashMap<Class<?>, Object>();

				//Try commandCodec and responseCodec stored in the session file
				commandCodecArgs.put(sessionInfo.commandCodecClassName, arguments);//What arguments should be provided?
				arguments.clear();
				responseCodecArgs.put(sessionInfo.responseCodecClassName, arguments);//What arguments should be provided?

				if(!setCodec(executor, commandCodecArgs, responseCodecArgs)){
					//try W3CHttpCommandCodec and W3CHttpResponseCodec
					commandCodecArgs.clear();
					commandCodecArgs.put(CLASSNAME_W3CHTTP_COMMANDCODEC, null);
					responseCodecArgs.clear();
					responseCodecArgs.put(CLASSNAME_W3CHTTP_RESPONSECODEC, null);

					if(!setCodec(executor, commandCodecArgs, responseCodecArgs)){
						//try JsonHttpCommandCodec and JsonHttpResponseCodec
						commandCodecArgs.clear();
						commandCodecArgs.put(CLASSNAME_JSONHTTP_COMMANDCODEC, null);
						responseCodecArgs.clear();
						responseCodecArgs.put(CLASSNAME_JSONHTTP_RESPONSECODEC, null);

						if(!setCodec(executor, commandCodecArgs, responseCodecArgs)){
							IndependantLog.error(debugmsg+" Failed to set proper commandCodec and responseCodec to CommandExecutor!");
						}
					}
				}

				//define the additional commands into CommandExecutor
				if(executor instanceof ChromeHttpCommandExecutor){
					((ChromeHttpCommandExecutor) executor).defineAdditionalCommands();
				}
			}
		}
	}

	/**
	 * @param executor CommandExecutor, the executor to set commandCodec and responseCodec
	 * @param commandCodecArgs LinkedHashMap the command codec's classname and parameters
	 * @param responseCodecArgs LinkedHashMap the response codec's classname and parameters
	 * @return boolean true if the command executor can work properly.
	 */
	private boolean setCodec(CommandExecutor executor,
			                 LinkedHashMap<String, LinkedHashMap<Class<?>, Object>> commandCodecArgs,
			                 LinkedHashMap<String, LinkedHashMap<Class<?>, Object>> responseCodecArgs){
		try{
			//Set executor's commandCodec
			Utils.setField(executor, FIELDNAME_COMMANDCODEC, commandCodecArgs);
			//Set executor's responseCodec
			Utils.setField(executor, FIELDNAME_RESPONSECODEC, responseCodecArgs);
			//calling getSize() to verify the command executor can work properly
			manage().window().getSize();
			return true;
		}catch(Exception e){
			IndependantLog.warn(StringUtils.debugmsg(false)+" Failed: due to "+e.toString());
			return false;
		}
	}

	//This method is called from startSession().
	private void deleteSession(SessionInfo sessionInfo) throws Exception{
		WebDriver wdToDelete = SearchObject.getWebDriver(sessionInfo.id);
		if(wdToDelete!=null){
			wdToDelete.quit();
		}else{
			IndependantLog.error("RemoteDriver.deleteSession(): cannot get 'WebDriver' for session '"+sessionInfo.id+"' from cache, so failed to delete it!");
		}
		//The following 2 steps will not be necessary, if RemoteDriver.quit() has been called.
		//But we still call them in case the driver is not a RemoteDriver or is null.
		if(!(wdToDelete instanceof RemoteDriver)){
			//Remove the 'CommnadExecutor' from the cache 'executorMap' if Selenium3.X is being used.
			removeExecutor(sessionInfo.session);
			//We will delete the obsolete session from the session file even we cannot get a valid WebDriver from SearchObject
			deleteSessionIdFromFile(sessionInfo.id);
		}
	}

	/**
	 * Called internally by OpenQA RemoteWebDriver during Constructor initialization.<br/>
	 * <b>NOTE:</b> Do <b>NOT</b> set any local fields in this method, they will be <b>RESET</b>. Set them in {@link #init(Capabilities)}. <br/>
	 */
	@Override
	public void startSession(Capabilities desiredCapabilities){
		String debugmsg = "RemoteDriver.startSession(): ";
		String ID = (String) desiredCapabilities.getCapability(CAPABILITY_ID);
		Boolean reconnect = (Boolean) desiredCapabilities.getCapability(CAPABILITY_RECONNECT);

		//Clean up any obsolete sessions
//		CommandExecutor originalExecutor = this.getCommandExecutor();
//		try {
//			List<SessionInfo> list = getSessionsFromFile();
//			for (SessionInfo info : list) {
//				try {
//					restoreSession(info);
//					getCurrentUrl();
//				} catch (WebDriverException check){
//					IndependantLog.debug(debugmsg+"deleting selenium obsolete session '"+ info.id +"', see reason : "+ check);
//					deleteSession(info);
//				}
//			}
//		} catch (Exception e1) {}
//		finally{
//			//set the Executor to original after 'clean up', otherwise it will prevent new session being started.
//			IndependantLog.debug(debugmsg+"after clean up, reset current Executor to original one '"+originalExecutor+".");
//			setCommandExecutor(originalExecutor);
//			//TODO clear the "restored session", otherwise it will prevent from starting a new session!
//			setSessionId(null);//this cannot clear the restored session :-(.
//		}

		//Start the session
		if (reconnect.booleanValue()){
		    SessionInfo info = null;
			try {
				info = retrieveSessionInfoFromFile(ID);
				if (info != null){
					restoreSession(info);
					getCurrentUrl();
				}else{
					IndependantLog.debug(debugmsg+"Failed to get cached session '"+ID+"'.");
				}
			} catch (WebDriverException we) {
				IndependantLog.debug(debugmsg+" Failed reconnecting session '"+ ID +"', which is obsolete. Deleting it, see reason: "+ we);
				try {
					//This session might be obsolete, delete it from the session file
					deleteSession(info);
				} catch (Exception e) {
					IndependantLog.error(debugmsg+"Failed to delete obsolete session '"+ID+"', Met "+e.toString());
				}
			} catch (Exception e) {
				IndependantLog.debug(debugmsg+"Failed reconnecting session '"+ ID +"', due to: "+ e);
			}

		} else {
			super.startSession(desiredCapabilities);
		}
	}

	/**
	 * "<b>getAlertText</b>" defines the default <a href="https://seleniumhq.github.io/selenium/docs/api/java/org/openqa/selenium/remote/DriverCommand.html">selenium-driver-command</a>
	 * used to test the connection between WebDriver and BrowserDriver.<br>
	 */
	public static final String DEFAULT_CONNECTION_TEST_COMMAND 			= DriverCommand.GET_ALERT_TEXT;

	/**
	 * Test if the connection between WebDriver and the browser-driver is good enough to satisfy user's need.<br>
	 * @param command String, the selenium driver command to use to test execution time
	 * @param maxDuration long, the maximum execution duration that user can accept
	 * @param timeoutWaitForComponent int, the original timeout to wait for a GUI component
	 * @return boolean true if the connection is good enough
	 * @throws SeleniumPlusException if the parameter command is null or empty
	 */
	public boolean isConnectionFine(String command, long maxDuration, int timeoutWaitForComponent) throws SeleniumPlusException{
		long consumedTime = getExecutionTime(command, timeoutWaitForComponent);
		return  maxDuration>consumedTime;
	}

	/**
	 * @param command String, the selenium driver command to use to test execution time
	 * @param timeoutWaitForComponent int, the original timeout to wait for a GUI component
	 * @return long, the time (in milliseconds) consumed by the command execution
	 * @throws SeleniumPlusException if the parameter command is null or empty
	 */
	public long getExecutionTime(String command, int timeoutWaitForComponent) throws SeleniumPlusException{
		long consumedTime = 0;
		if(command==null||command.isEmpty()){
			throw new SeleniumPlusException("The command '"+command+"' is not valid.");
		}

		try{
			//don't wait so that the NoAlertPresentException will be thrown out immediately
			//and we know the time duration is the duration for executing command, not the timeout
			manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
			long start = System.currentTimeMillis();
			execute(command);
			consumedTime = System.currentTimeMillis()-start;
		}catch(WebDriverException wde){
//			Command duration or timeout: 12 milliseconds
//			Build info: version: '2.52.0', revision: '4c2593c', time: '2016-02-11 19:06:42'
//			System info: host: 'rdcesx05043', ip: '10.21.17.32', os.name: 'Windows 8.1', os.arch: 'amd64', os.version: '6.3', java.version: '1.7.0_79'
//			Session ID: 7ee4e4f45bc4305704e49c93fbb4f382
//			Driver info: org.openqa.selenium.chrome.ChromeDriver
//
//			Command duration or timeout: 85 milliseconds
//			Build info: version: '2.52.0', revision: '4c2593c', time: '2016-02-11 19:06:42'
//			System info: host: 'rdcesx05043', ip: '10.21.17.32', os.name: 'Windows 8.1', os.arch: 'amd64', os.version: '6.3', java.version: '1.7.0_79'
//			Session ID: f6a006ab-48d1-4067-acc4-59217c36c179
//			Driver info: org.safs.selenium.webdriver.lib.RemoteDriver

			//This prefix may change if selenium source code change it.
			String prefix = "Command duration or timeout:";
			String message = wde.getMessage();
			int beginIndex = message.indexOf(prefix);

			if(beginIndex>-1){
				beginIndex += prefix.length();
				//skip the first duration, it is for browser driver such as "org.openqa.selenium.chrome.ChromeDriver"
				beginIndex = message.indexOf(prefix, beginIndex);
				if(beginIndex>-1){
					beginIndex += prefix.length();
					int endIndex = message.indexOf("milliseconds", beginIndex);
					if(endIndex>-1 && endIndex>beginIndex){
						try{
							consumedTime = Long.parseLong(message.substring(beginIndex, endIndex).trim());
						}catch(Exception e){}
					}
				}
			}

		}finally{
			manage().timeouts().implicitlyWait(timeoutWaitForComponent, TimeUnit.SECONDS);
		}

		IndependantLog.debug("getExecutionTime(): time consumed '"+consumedTime+"' milliseconds for command '"+command+"'");

		return  consumedTime;
	}

	/*
	 * Trying to fix a problem with IE JSON values containing "-1.IND"
	 * (non-Javadoc)
	 * @see org.openqa.selenium.remote.RemoteWebDriver#execute(java.lang.String, java.util.Map)
	 */
	@Override
    protected Response execute(String driverCommand, Map<String, ?> parameters) {
		final String debugmsg = "RemoteDriver.execute(): ";
		if(logDriverCommand) IndependantLog.debug(debugmsg+"starting '"+driverCommand+"' with parameters: "+parameters);
		Response response = null;
		response = super.execute(driverCommand, parameters);
//		try{
//			response = super.execute(driverCommand, parameters);
//		}catch(Exception e){
//			IndependantLog.debug(debugmsg + driverCommand +" failed when calling super.execute(): met "+e.toString());
//		}
    	if(logDriverCommand) IndependantLog.debug(debugmsg+"ended '"+driverCommand+"' with response: "+response);
    	if((response != null) && (response.getValue() instanceof java.lang.String)){
    		String v = (String) response.getValue();
    		v = v.trim();
    		if(v.startsWith("{") && v.endsWith("}")){
        		IndependantLog.info(debugmsg + driverCommand +" attempting to convert uncertain JSON Response to Map Response: "+ v);
	    		v = v.replace("-1.#IND", "0");
	    		v = v.replace("{'", "{\"");
	    		v = v.replace("':", "\":");
	    		v = v.replace(":'", ":\"");
	    		v = v.replace("',", "\",");
	    		v = v.replace(",'", ",\"");
        		try{
        			Object nv = Json.convert(Map.class, v);
        			if(nv instanceof Map){
    	        		IndependantLog.info(debugmsg + driverCommand +" JSON Map conversion SUCCESSFUL: "+ nv.toString());
    	        		Map<?, ?> m = (Map<?, ?>) nv;
    	        		final String SESSIONID = "sessionid";
    	        		final String STATUS = "status";
    	        		final String VALUE = "value";
    	        		for(Object key: m.keySet()){
    	        			if(key.toString().equalsIgnoreCase(SESSIONID)) response.setSessionId(m.get(key).toString());
    	        			try{if(key.toString().equalsIgnoreCase(STATUS)) response.setStatus(((Number)m.get(key)).intValue());}
    	        			catch(Exception x){
    	    	        		IndependantLog.info(debugmsg + driverCommand +" STATUS conversion "+ x.getClass().getName()+", "+x.getMessage());
    	    	        	}
            				if(key.toString().equalsIgnoreCase(VALUE)) response.setValue(m.get(key));
    	        		}
        			}else{
    	        		IndependantLog.info(debugmsg + driverCommand +" JSON conversion WAS NOT successful.");
        			}
        		}catch(Throwable t){
        			IndependantLog.debug(debugmsg + driverCommand +" ignoring JSON problem\n", t);
	    		}
    		}
    	}else{
			if(response == null) {
				IndependantLog.debug(debugmsg + driverCommand +" failed to get any type of response.");
			}else{
				// DEBUG IndependantLog.debug(debugmsg + driverCommand +" received a "+ response.getClass().getSimpleName()+" response.");
			}
    	}
    	return response;
    }

	public boolean isNewSession(){
		return newSession;
	}

	private static String getString(Capabilities capabilities, String key){
		try{
			Object value = capabilities.getCapability(key);
			return (value==null? "": value.toString());
		}catch(Exception e){
			IndependantLog.error(StringUtils.debugmsg(false)+" Met "+StringUtils.debugmsg(e));
			return "";
		}
	}

	public static File getSessionFile(){
		String dirname = System.getProperty(JAVA_TMPDIR);
		File file = new File(dirname, SESSION_FILE);

		return file;
	}

	protected synchronized void storeSessionIdToFile(String serverHostname, String Id,  String browserName, String sessionid, Capabilities desiredCapabilities) throws Exception{

		try {
			IndependantLog.debug("RemoteDriver.storeSessionIDToFile attempting to store session id: "+ Id);

			Properties prop = new Properties();
			File afile = getSessionFile();
			String file = afile.getCanonicalPath();

			if (afile.exists()){
				IndependantLog.debug("RemoteDriver.storeSessionIDToFile file DOES exist: "+ file);
				prop.load(new FileInputStream(file));
			}else{
				IndependantLog.debug("RemoteDriver.storeSessionIDToFile file does NOT exist yet: "+ file);
			}
			//get browser-version from capabilities
			String browserVersion = getString(desiredCapabilities, CapabilityType.VERSION);
			//get platform from capabilities
			String platform = getString(desiredCapabilities, CapabilityType.PLATFORM);

			//create the session-content used for reconnection
			StringBuilder sessionContent = new StringBuilder(serverHostname +SPLITTER+ sessionid +SPLITTER+ browserName + SPLITTER +browserVersion+ SPLITTER +platform);
			if(SeleniumServerRunner.isSelenium3X()){
				//store the CommandExecutor's fields: 'commandCodec' and 'responseCodec'
				CommandExecutor executor = this.getCommandExecutor();
				sessionContent.append(SPLITTER + Utils.getFieldInstanceClassName(executor, FIELDNAME_COMMANDCODEC));
				sessionContent.append(SPLITTER + Utils.getFieldInstanceClassName(executor, FIELDNAME_RESPONSECODEC));
			}

			for(String key:SessionInfo.optionalKeys){
				sessionContent.append(SPLITTER+getString(desiredCapabilities, key));
			}

			prop.put(Id, sessionContent.toString());
			prop.put(LAST_SESSION_KEY, Id);

			OutputStream out = new FileOutputStream(afile);
			prop.store(out, file);

		} catch (Exception e) {
			IndependantLog.debug("RemoteDriver error writing session file due to: "+ e.getClass().getSimpleName());
			throw new Exception("Session store issue " + e.getMessage());
		}

	}

	protected synchronized void storeSession(String sessionid, Capabilities desiredCapabilities) throws Exception{
		String remoteServer = (String) desiredCapabilities.getCapability(CAPABILITY_REMOTESERVER);
		String id = (String) desiredCapabilities.getCapability(CAPABILITY_ID);
		String browserName = desiredCapabilities.getBrowserName();
		storeSessionIdToFile(remoteServer, id,browserName,sessionid, desiredCapabilities);
		//Store the executor into a cache 'executorMap'.
		storeExecutor(sessionid, this.getCommandExecutor());
	}

	/**
	 * Delete the session from the RemoteDriver file with the given Id.
	 * If the Id is also set as the LAST_SESSION, then the LAST_SESSION_KEY
	 * will also be removed showing the last or "current" session is uncertain.
	 * @param Id
	 * @throws Exception if there is a problem reading or deleting the session info.
	 */
	public static synchronized void deleteSessionIdFromFile(String Id) throws Exception {
		try {
			Properties prop = new Properties();
			IndependantLog.debug("RemoteDriver.deleteSessionIDFromFile attempting to delete session ID: "+ Id);
			File afile = getSessionFile();
			String file = afile.getCanonicalPath();
			if (! afile.exists()){
				IndependantLog.debug("RemoteDriver.deleteSessionIDFromFile file does NOT already exist: "+ file);
			}else{
				IndependantLog.debug("RemoteDriver.deleteSessionIDFromFile file '"+ file +" DOES exist.");
			}
			prop.load(new FileInputStream(file));
			String lastSessionID = prop.getProperty(LAST_SESSION_KEY);
			try{
				prop.remove(Id);
				if(Id.equals(lastSessionID)){
					prop.remove(LAST_SESSION_KEY);
					IndependantLog.debug("RemoteDriver.deleteSessionIDFromFile deleting LASTSESSION from session file.");
				}
			}catch(NullPointerException x){
				IndependantLog.debug("RemoteDriver.deleteSessionIDFromFile ignoring NullPointerException: Id="+ Id +", lastSessionID="+ lastSessionID);
			}
			OutputStream out = new FileOutputStream(afile);
			prop.store(out, file);
		} catch (Exception e) {
			IndependantLog.debug("RemoteDriver.deleteSessionIDFromFile file error due to: "+ e.getClass().getSimpleName(), e);
			throw new Exception("Session delete issue");
		}
	}

	/**
	 * Retrieve the remote server's session information for the session with the given id.
	 * @param Id
	 * @return SessionInfo or null if not found.
	 * @throws Exception
	 */
	public static synchronized SessionInfo retrieveSessionInfoFromFile(String Id) throws Exception{

		try {
			if(Id == null || Id.length()==0) throw new IllegalArgumentException("Id cannot be null or zero-length.");

			Properties prop = new Properties();

			File afile = getSessionFile();
			String file = afile.getCanonicalPath();
			if (! afile.exists()){
				IndependantLog.debug("RemoteDriver.retrieveSessionIDFromFile file does NOT already exist: "+ file);
			}
			prop.load(new FileInputStream(file));
			String sessionInfo = prop.getProperty(Id,null);

			if (sessionInfo == null)return null;

			String last = prop.getProperty(LAST_SESSION_KEY);
			SessionInfo info = new SessionInfo(Id, Id.equalsIgnoreCase(last), sessionInfo);
			return info;
		}
		catch (Exception e) {
			IndependantLog.debug("RemoteDriver.retrieveSessionIDFromFile file error due to: "+ e.getClass().getSimpleName()+ ", "+ e.getMessage());
			throw new Exception("Session retrieve issue.  Session info may not be valid or complete.");
		}
	}
	/**
	 * Retrieve the remote server's session string for the session with the given id.
	 * @param Id
	 * @return
	 * @throws Exception
	 */
	public static synchronized String retriveSessionIdFromFile(String Id) throws Exception{

		return retrieveSessionInfoFromFile(Id).session;
	}

	public static synchronized void setLastSessionId(String Id) throws Exception{

		try {
			if( Id==null || Id.length()==0) throw new IllegalArgumentException("Id cannot be null or zero-length.");
			Properties prop = new Properties();

			File afile = getSessionFile();
			String file = afile.getCanonicalPath();
			if (afile.exists()){
				prop.load(new FileInputStream(afile));
				prop.put(LAST_SESSION_KEY, Id);
			}else{
				IndependantLog.debug("RemoteDriver.setLastSessionID file does NOT already exist: "+ file);
			}

			//if (!afile.exists()) afile.createNewFile();
			OutputStream out = new FileOutputStream(afile);
			prop.store(out, file);

		} catch (Exception e) {
			IndependantLog.debug("RemoteDriver.setLastSessionID error due to: "+ e.getClass().getSimpleName()+", "+e.getMessage());
			throw new Exception("Session retrive issue");
		}
	}

	/**
	 * Retrieve SessionInfo representing the "current" or "last" session
	 * @return SessionInfo -- can be null if there is no session running or stored.
	 * @throws Exception
	 */
	public static synchronized SessionInfo retriveLastSessionInfoFromFile() throws Exception{

		try {
			Properties prop = new Properties();

			File afile = getSessionFile();
			String file = afile.getCanonicalPath();
			if (!afile.exists()){
				IndependantLog.debug("RemoteDriver.retrieveLastSessionIDFromFile file does NOT already exist: "+ file);
			}
			prop.load(new FileInputStream(file));
			String browserId = prop.getProperty(LAST_SESSION_KEY,null);
			if(browserId == null) return null;
			String sessionContent = prop.getProperty(browserId,null);

			return new SessionInfo(browserId, true, sessionContent);

		} catch (Exception e) {
			IndependantLog.debug("RemoteDriver.retrieveLastSessionIDFromFile error due to: "+ e.getClass().getSimpleName());
			throw new Exception("Session Key retrieval issue");
		}

	}

	/**
	 * Returns a List of SessionInfo objects.
	 * @return List --can be empty if no sessions and no Exception is thrown.
	 * @throws Exception if the sessions are not retrievable (do not exist).
	 */
	public static synchronized List<SessionInfo> getSessionsFromFile() throws Exception{

		try {
			Properties prop = new Properties();

			File afile = getSessionFile();
			String file = afile.getCanonicalPath();
			if (!afile.exists()){
				IndependantLog.debug("RemoteDriver.getSessionsFromFile file does NOT already exist: "+ file);
			}
			prop.load(new FileInputStream(file));
			List<SessionInfo> list = new ArrayList<SessionInfo> ();
			String last = prop.getProperty(LAST_SESSION_KEY);
			Enumeration<Object> en = prop.keys();
			String key = null;
			String sessionContent = null;
			while(en.hasMoreElements()){
				key = (String)en.nextElement();
				if(key.equals(LAST_SESSION_KEY)) continue;
				sessionContent = prop.getProperty(key);

				try{ list.add(new SessionInfo(key, key.equals(last), sessionContent));}
				catch(Exception ignore){}
			}
			return list;

		} catch (Exception e) {
			IndependantLog.debug("RemoteDriver.getSessionsFromFile error due to: "+ e.getClass().getSimpleName());
			throw new Exception("Session Key retrieval issue", e);
		}
	}

	public static synchronized void deleteSessionFile(){
		File afile = getSessionFile();
		if (afile.exists()) afile.delete();
	}

	@Override
	public <X> X getScreenshotAs(OutputType<X> target)
				throws WebDriverException {

			return target
					.convertFromBase64Png(execute(DriverCommand.SCREENSHOT)
							.getValue().toString());
	}

	/*
	 * Override Javascript executescript method due to reconnection
	 * webdriver throw null exception
	 */
	@Override
	public Object executeScript(String script, Object... args) {

	    // Escape the quote marks
	    script = script.replaceAll("\"", "\\\"");

	    Iterable<Object> convertedArgs = Iterables.transform(
	        Lists.newArrayList(args), new WebElementToJsonConverter());

	    Map<String, ?> params = ImmutableMap.of(
	        "script", script,
	        "args", Lists.newArrayList(convertedArgs));

	    return execute(DriverCommand.EXECUTE_SCRIPT, params).getValue();
	 }

	/**
	 * This class contains information for reconnecting a session.<br>
	 * The session info string contains some fields separated by {@link RemoteDriver#SPLITTER}<br>
	 *
	 * The current format of the session info string is as below:<br>
	 * <p>
	 * ID=<b>serverHost</b>_SP_<b>sessionid</b>_SP_<b>browserName</b>_SP_<b>browserVersion</b>_SP_<b>platform</b>_SP_<b>commandCodecClassName</b>_SP_<b>responseCodecClassName</b>_SP_<b>&lt;extraParameters></b><br>
	 * <b>&lt;extraParameters></b> are optional parameter as below
	 * <ul>
	 * <li>firefoxProfile
	 * <li>chromeUserDataDir
	 * <li>chromeProfileDir
	 * <li>chromePreference
	 * <li>chromeExcludeOptions
	 * <li>firefoxPreference
	 * <li>networkConditions
	 * <li>customCapabilites
	 * </ul>
	 * <br>
	 */
	public static class SessionInfo {
		public String id = null;
		public boolean isCurrentSession = false;
		//Required parameters
		public String serverHost = null;
		public String browser = null;
		public String session = null;
		public String browserVersion = null;
		public String platform = null;
		public String commandCodecClassName = null;
		public String responseCodecClassName = null;
		//Optional parameters
		public HashMap<String,Object> extraParameters = new HashMap<String,Object>();
		public static final String[] optionalKeys = {
				//firefox-profile (name or filename) from capabilities
				SelectBrowser.KEY_FIREFOX_PROFILE,
				//chrome user-data-directory from capabilities
				SelectBrowser.KEY_CHROME_USER_DATA_DIR,
				//chrome profile-directory from capabilities
				SelectBrowser.KEY_CHROME_PROFILE_DIR,
				//chrome preference from capabilities
				SelectBrowser.KEY_CHROME_PREFERENCE,
				//chrome excluded options from capabilities, it is comma-separated string
				SelectBrowser.KEY_CHROME_EXCLUDE_OPTIONS,
				//firefox preference from capabilities
				SelectBrowser.KEY_FIREFOX_PROFILE_PREFERENCE,
				//chrome set network conditions
				BrowserConstants.KEY_SET_NETWORK_CONDITIONS,
				//custom capabilities
				BrowserConstants.KEY_CUSTOM_CAPABILITIES
				};

		@SuppressWarnings("unused")
		private SessionInfo(){super();}

		/**
		 * Builds a new SessionInfo object from a real new session.
		 *
		 * @param serverHost
		 * @param id
		 * @param browser
		 * @param session
		 * @param isCurrentSession
		 */
		public SessionInfo(String serverHost, String id, String browser, String session, boolean isCurrentSession){
			this.id = id;
			this.isCurrentSession = isCurrentSession;
			this.serverHost = serverHost;
			this.browser = browser;
			this.session = session;
		}

		/**
		 * Builds a SessionInfo object from (probably) stored sessionContent information.
		 * @param id
		 * @param isCurrentSession
		 * @param sessionContent String, read from a session file
		 */
		public SessionInfo(String id, boolean isCurrentSession, String sessionContent){
			this.id = id;
			this.isCurrentSession = isCurrentSession;

			try{
				String[] sessionInfos = StringUtils.getTokenArray(sessionContent, SPLITTER);
				this.serverHost = sessionInfos[0];
				this.session = sessionInfos[1];
				this.browser = sessionInfos[2];
				this.browserVersion = sessionInfos[3];
				this.platform = sessionInfos[4];
				this.commandCodecClassName = sessionInfos[5];
				this.responseCodecClassName = sessionInfos[6];
				int requiredParamLength = 7;
				String tempInfo = null;
				for(int i=requiredParamLength;i<sessionInfos.length;i++){
					tempInfo = sessionInfos[i];
					if(StringUtils.isValid(tempInfo)){
						extraParameters.put(optionalKeys[i-requiredParamLength], tempInfo);
					}
				}
			}catch(Exception e){
				IndependantLog.error("Fail to initialize SessionInfo due to "+StringUtils.debugmsg(e));
			}
		}

		public void restore(DesiredCapabilities capabilities){
			DesiredCapabilities extraCapabilities = new DesiredCapabilities(extraParameters);
			capabilities.merge(extraCapabilities);

			//Restore browser-version and platform
			capabilities.setCapability(CapabilityType.VERSION, browserVersion);
			capabilities.setCapability(CapabilityType.PLATFORM, platform);
		}
	}

	public static final String debugLogFileName =  RemoteDriver.class.getName().replaceAll("\\.", "_")+"_debug_log.txt";
	/** The debug log file containing debug message when calling main() to start "selenium server". */
	public static final File debugLogFile = new File(System.getProperty("user.home"), debugLogFileName);
	private static PrintStream pstream = null;

	/** "-project" */
	public static final String PARAM_PROJECT 			= "-project";

	/**
	 * This main method will start a Selenium Server (standalone, hub, or node). It will automatically detect the Java to use,
	 * which file to log messages for each browser, and what driver (IE CHROME) to be used to start with Selenium Server.<br>
	 * <br>
	 * @param args String[], it accepts following arguments:<br>
	 * <b>"-port N"</b>, optional, the port number for Selenium Server. If not provided, the default port will be used.
	 *                   For "standalone" and "hub" the default port number is 4444; While for "node", it is 5555.
	 *                   <br>
	 * <b>"SELENIUMSERVER_JVM_OPTIONS=JVM OPTIONS"</b>, optional, the JVM Options to start the Selenium Server<br>
	 * <b>"-role TheServerRole"</b>, optional, if not provided, a standalone server will be launched.<br>
	 *                                         TheServerRole could be <b>"hub"</b>, and selenium server will be launched
	 *                                         as a hub for other node to connect.<br>
	 *                                         TheServerRole could be <b>"node"</b>, and selenium server will be launched
	 *                                         as a node to connect a hub. <b>**Note**</b> Hub's information must also
	 *                                         be provided. Ex: <b>-role node -hub http://hub.machine:port/grid/register</b><br>
	 * <b>"-project ProjectLocationAbsDir"</b>, optional, the absolute directory holding the project to test; If not provided, use
	 *                                        SeleniumPlus or SAFS installation directory as default. This parameter has higher
	 *                                        priority than JVM parameter "-Dsafs.project.root".<br>
	 * <b>"-timeout=N"</b>, optional, The timeout (default is {@link SeleniumConfigConstant#DEFAULT_TIMEOUT}) in seconds before the hub automatically releases a node that hasn't received any requests for more than the specified number of seconds. Ex: "-timeout=120",
	 * <b>"-browserTimeout=N"</b>, optional, The timeout (default is {@link SeleniumConfigConstant#DEFAULT_BROWSER_TIMEOUT} ) in seconds a node is willing to hang inside the browser. Ex: "-browserTimeout=120"
	 * <br>
	 * Example:<br>
	 * <ul>
	 * <li>Start "selenium standalone server on port 4567 with JVM option -Xms512m -Xmx2g"<br>
	 * java org.safs.selenium.webdriver.lib.RemoteDriver "-port 4567" "SELENIUMSERVER_JVM_OPTIONS=-Xms512m -Xmx2g"<br>
	 *
	 * <li>Start "selenium hub on port 4567 with JVM option -Xms512m -Xmx2g"<br>
	 * java org.safs.selenium.webdriver.lib.RemoteDriver "-role hub" "-port 4567" "SELENIUMSERVER_JVM_OPTIONS=-Xms512m -Xmx2g"<br>
	 *
	 * <li>Start "selenium node" on port 5555 with JVM option -Xms512m -Xmx2g", and register it to hub.machine:4567<br>
	 * java org.safs.selenium.webdriver.lib.RemoteDriver "-role node -hub http://hub.machine:4567/grid/register" "-port 5678" "SELENIUMSERVER_JVM_OPTIONS=-Xms512m -Xmx2g"<br>
	 *
	 * </ul>
	 * <br>
	 * We can also provide the following <b>JVM parameters</b>.<br>
	 * <ul>
	 * <li> {@link SeleniumConfigConstant#PROPERTY_SELENIUMSERVER_JVM_OPTIONS}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_SELENIUMSERVER_JVM_Xms}
	 * <li> {@link SeleniumConfigConstant#PROPERTY_SELENIUMSERVER_JVM_Xmx}
	 * <li> {@link DriverConstant#PROPERTY_SAFS_PROJECT_ROOT}
	 * </ul>
	 * Example:<br>
	 * <ul>
	 * <li>-Dsafs.selenium.server.jvm.options="-Xms256m -Xmx1g"
	 * <li>-Dsafs.selenium.server.jvm.xmx=4g
	 * <li>-Dsafs.selenium.server.jvm.xms=512m
	 * <li>-Dsafs.project.root=d:\full_path\to\test_project
	 * </ul>
	 */
	public static void main(String[] args){
		String debugmsg = StringUtils.debugmsg(false);

		try {
			pstream = new PrintStream(new BufferedOutputStream(new FileOutputStream(debugLogFile)));
			IndependantLog.setDebugListener(new DebugListener(){
				@Override
				public String getListenerName() {
					return null;
				}
				@Override
				public void onReceiveDebug(String message) {
					if(pstream!=null) pstream.println(message);
					System.out.println(message);
				}
			});
			IndependantLog.debug(debugmsg+ RemoteDriver.class.getName() + " launching, the debug message will be in file '"+RemoteDriver.debugLogFile.getAbsolutePath()+"'.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		IndependantLog.debug(debugmsg+" Current System properties: "+System.getProperties());

		String projectLocation = System.getProperty(DriverConstant.PROPERTY_SAFS_PROJECT_ROOT);

		for(int i=0;i<args.length;i++){
			if(args[i].toLowerCase().startsWith(PARAM_PROJECT)){
				projectLocation = args[i].substring(PARAM_PROJECT.length());
				break;
			}
		}

		IndependantLog.debug(debugmsg+"Project location is "+projectLocation);

		if(!WebDriverGUIUtilities.startRemoteServer(projectLocation, args)){
			IndependantLog.error(debugmsg+"Fail to start Remote Server with parameter "+Arrays.toString(args));
		}

		if(pstream!=null){
			pstream.flush();
			pstream.close();
		}

		IndependantLog.setDebugListener(null);
	}
}
