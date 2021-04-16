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
* History:<br>
*
*  NOV 18, 2013    (DHARMESH) Initial release.
*  NOV 18, 2013    (Carl Nagle) Modified to allow us to change capabilities in one place for any kind of webdriver instantiated.
*  DEC 27, 2013    (Lei Wang) Modify code to permit start browser with more parameters, such as proxy setting.
*  JAN 28, 2014    (Lei Wang) Modify code to get proxy setting from system properties.
*  FEB 19, 2014    (DHARMESH) Fixed local browser support.
*  APR 15, 2014	   (DHARMESH) Fixed IE click issue to disable nativeEvents to false.
*  AUG 29, 2014    (DHARMESH) Add selenium grid host and port support.
*  SEP 04, 2014    (Lei Wang) Handle FireFox's profile.
*  MAR 17, 2015    (Lei Wang) Handle Chrome's custom data, profile.
*  APR 08, 2015    (Lei Wang) Modify to turn off Chrome's starting options.
*  MAR 07, 2016    (Lei Wang) Handle preference setting for "chrome" and "firefox".
*  MAR 23, 2016    (Lei Wang) Modify setChromeCapabilities(): handle "command line options" and "preferences" for "chrome".
*  APR 26, 2016    (Lei Wang) Modify getDesiredCapabilities(): Get value of 'unexpectedAlertBehaviour' from Processor and 'System property'
*                                                             and set it for all browsers.
*  NOV 09, 2016    (Lei Wang) Copied constants to org.safs.Contants and refer to them.
*  JAN 03, 2017    (Lei Wang) Modified getBrowserInstance() and getDesiredCapabilities() and added prepareHttpProxy(): Handle the HTTP proxy setting.
*  JAN 06, 2017    (Lei Wang) Modified addFireFoxPreference(): catch IllegalArgumentException inside the loop for each FirefoxProfile.setPreference().
*  JAN 06, 2017    (Lei Wang) Modified getDesiredCapabilities(): ONLY set capability 'unexpectedAlertBehaviour' for IE browser.
*                                     selenium 3.4 & firefox 53.0 & gecko v0.18.0 throw InvalidArgumentException: ignore was not a valid unhandledPromptBehavior value
*  DEC 25, 2017    (Lei Wang) Added isBrowser(): compare current browser name with the expected browser's name.
*  MAY 15, 2018    (Lei Wang) Modified setChromeCapabilities(): support option "--no-sandbox".
*  JUN 04, 2019    (Lei Wang) Modified setChromeCapabilities(): support command "setNetworkConditions".
*  JUL 06, 2019    (Lei Wang) Modified getDesiredCapabilities(): handle custom capabilities.
*  OCT 09, 2019    (Lei Wang) Modified setChromeCapabilities(): handle 'experimental options',
*                                                              accept also json string (instead of json file) for parameter 'KEY_CHROME_PREFERENCE', 'KEY_CHROME_EXPERIMENTAL_OPTIONS' and 'KEY_FIREFOX_PROFILE_PREFERENCE'.
*  OCT 10, 2019    (Lei Wang) Modified setChromeCapabilities(), Added fromJsonString(): accept also CSV string (an array of key:value, such as "key:value, key:value, key:value") for parameter 'KEY_CHROME_PREFERENCE', 'KEY_CHROME_EXPERIMENTAL_OPTIONS' and 'KEY_FIREFOX_PROFILE_PREFERENCE'.
*  OCT 11, 2019    (Lei Wang) Modified getDesiredCapabilities(): set the capability "unexpectedAlertBehaviour" for all browsers, user can turn if off if he wants.
*  APR 14, 2020    (Lei Wang) Modified getDesiredCapabilities(): Allow user to add custom capabilities as a json file.
*  APR 27, 2020    (Lei Wang) Modified setChromeCapabilities(): Load chrome's entensions.
*/
package org.safs.selenium.webdriver.lib;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.ProfilesIni;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.safs.Constants.BrowserConstants;
import org.safs.Constants.SeleniumConstants;
import org.safs.IndependantLog;
import org.safs.Processor;
import org.safs.StringUtils;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.drivers.DriverConstant.SeleniumConfigConstant;
import org.safs.tools.stringutils.StringUtilities;

/**
 * The helper class to handle browser related informations.<br>
 * Such as:<br>
 * create a browser related DesiredCapabilities, which is used to create WebDriver.<br>
 * get FirefoxProfile according to a file for firefox browser.<br>
 * get a WebDriver according to a browser name.<br>
 */
public class SelectBrowser {

	//If you add a new KEY_XXX, please update the method getExtraParameterKeys() by adding it.
	/** 'KEY_PROXY_SETTING' the key for proxy string;
	 * The value is colon separated string as "proxyserver:port" */
	public static final String KEY_PROXY_SETTING = BrowserConstants.KEY_PROXY_SETTING;
	/** 'KEY_PROXY_BYPASS_ADDRESS' the key for proxy bypass address string;
	 * The value is comma separated string as "localhost,tadsrv,rnd" */
	public static final String KEY_PROXY_BYPASS_ADDRESS = BrowserConstants.KEY_PROXY_BYPASS_ADDRESS;
	/** 'FirefoxProfile' the key for firefox profile name/filename string;
	 * The value is something like "myprofile" or "&lt;AbsolutePath>/ppc2784x.default" */
	public static final String KEY_FIREFOX_PROFILE = BrowserConstants.KEY_FIREFOX_PROFILE;//Name Or FilePath

	/** 'firefox.perference' the key for firefox preference file, which contains json data,
	 * such as { "intl.accept_languages":"zh-cn", "accessibility.accesskeycausesactivation":false, "browser.download.folderList":2 }<br>
	 * <b>Note: Be careful when creating the json data file, do NOT quote boolean or integer value.</b>*/
	public static final String KEY_FIREFOX_PROFILE_PREFERENCE = BrowserConstants.KEY_FIREFOX_PROFILE_PREFERENCE;//Firefox Preference file

	/** 'chrome.perference' the key for chrome command-line-options/preferences file, which contains
	 * <ol>
	 * <li><b>command-line-options</b> json data, such as { "lang":"zh-cn", "disable-download-notification":"" },
	 *     refer to <a href="http://peter.sh/experiments/chromium-command-line-switches/">detail options</a>
	 * <li><b>preferences</b> json data, it is indicated by a special key {@link #KEY_CHROME_PREFERENCE_JSON_KEY},
	 *     such as { "<b>seplus.chrome.preference.json.key</b>": { "intl.accept_languages":"zh-cn", "intl.charset_default":"utf-8" } },
	 *     refer to <a href="https://src.chromium.org/viewvc/chrome/trunk/src/chrome/common/pref_names.cc">detail preferences</a>
	 * <ol>
	 */
	public static final String KEY_CHROME_PREFERENCE = BrowserConstants.KEY_CHROME_PREFERENCE;//"Chrome Command Line Options" and "Chrome Preferences" file

	/** 'seplus.chrome.preference.json.key' the key for chrome preferences, which points to json data,
	 * such as { "intl.accept_languages":"zh-cn", "intl.charset_default":"utf-8" },
	 * refer to <a href="https://src.chromium.org/viewvc/chrome/trunk/src/chrome/common/pref_names.cc">detail preferences</a><br>
	 * <b>NOTE: We can also specify this by key "prefs" for {@link #KEY_CHROME_EXPERIMENTAL_OPTIONS}.</b>
	 */
	public static final String KEY_CHROME_PREFERENCE_JSON_KEY = BrowserConstants.KEY_CHROME_PREFERENCE_JSON_KEY;//Chrome Preferences

	/** The chrome Experimental Option <b>prefs</b>. Used internally. */
	private static final String KEY_CHROME_PREFS = BrowserConstants.KEY_CHROME_PREFS;//setExperimentalOption

	/** '<b>experimentalOptions</b>' the key for chrome Experimental Options, it contains:
	 * <ol>
	 * <li><b>experimental options</b> json data, such as { "useAutomationExtension":"false",
	 *                                                      "prefs": { "intl.accept_languages":"zh-cn", "intl.charset_default":"utf-8" },
	 *                                                      "excludeSwitches": ["enable-automation", "disable-component-update", "ignore-certificate-errors"]}
	 * </ol>
	 */
	public static final String KEY_CHROME_EXPERIMENTAL_OPTIONS = BrowserConstants.KEY_CHROME_EXPERIMENTAL_OPTIONS;//setExperimentalOption


	/**'user-data-dir' the parameter name for chrome options, a general custom data settings.<br>
	 * The value is specified in <a href="http://peter.sh/experiments/chromium-command-line-switches">chrome options</a><br>
	 * <b>Note:</b> As this {@value #KEY_CHROME_USER_DATA_DIR} contains minus, it could be interpreted as an arithmetic expression,
	 *             Use SeleniumPlus.quote({@value #KEY_CHROME_USER_DATA_DIR}) to keep its value.
	 * @see #KEY_CHROME_PROFILE_DIR
	 **/
	public static final String KEY_CHROME_USER_DATA_DIR = BrowserConstants.KEY_CHROME_USER_DATA_DIR;

	/**'profile-directory' the parameter name for chrome options, a user-specific settings, it indicates a sub-folder under "user data directory".<br>
	* The value is specified in <a href="http://peter.sh/experiments/chromium-command-line-switches">chrome options</a><br>
	* <b>Note:</b> As this {@value #KEY_CHROME_PROFILE_DIR} contains minus, it could be interpreted as an arithmetic expression,
	*             Use SeleniumPlus.quote({@value #KEY_CHROME_PROFILE_DIR}) to keep its value.
	* @see #KEY_CHROME_USER_DATA_DIR
	**/
	public static final String KEY_CHROME_PROFILE_DIR = BrowserConstants.KEY_CHROME_PROFILE_DIR;

	/**
	 * 'excludeSwitches' the experimental option name for chrome options, it is used to turn off chrome starting options.<br>
	 * The value is separated-options to exclude, the separator can be comma(,) or semicolon(;) , <br>
	 * like "disable-component-update, ignore-certificate-errors" or "disable-component-update; ignore-certificate-errors",<br>
	 * <b>be careful</b>, there are NO 2 hyphens before options, "--disable-component-update, --ignore-certificate-errors" is wrong.<br>
	 * <b>Note:</b> As the value, excluded-options, may contain minus like "disable-component-update", it could be interpreted as an arithmetic expression,
	 *             Use SeleniumPlus.quote("disable-component-update") to keep its value.<br>
	 * <b>NOTE: We can also specify this by key "excludeSwitches" for {@link #KEY_CHROME_EXPERIMENTAL_OPTIONS}.</b>
	 */
	public static final String KEY_CHROME_EXCLUDE_OPTIONS = BrowserConstants.KEY_CHROME_EXCLUDE_OPTIONS;

	/**
	 * '--disable-extensions' is used to disable the use of Chrome extensions. Usually, we use it
	 * as DEFAULT to avoid popping up 'Disable developer mode extensions' message.
	 *
	 * In 'SeleniumPlus.StartWebBrowser()' or 'WDLibrary.startBrowser()', we can use 'false' value to cancel this default 'disable' setting.
	 * E.g.
	 *     WDLibrary.startBrowser(BrowserName, Url, Id, timeout, isRemote, quote(SelectBrowser.KEY_CHROME_DISABLE_EXTENSIONS), "false");
	 *
	 */
	public static final String KEY_CHROME_DISABLE_EXTENSIONS = BrowserConstants.KEY_CHROME_DISABLE_EXTENSIONS;

	/**
	 * @see BrowserConstants#KEY_CHROME_LOAD_EXTENSIONS
	 */
	public static final String KEY_CHROME_LOAD_EXTENSIONS = BrowserConstants.KEY_CHROME_LOAD_EXTENSIONS;

	/**
	 * @see BrowserConstants#KEY_CHROME_EXTENSIONS
	 */
	public static final String KEY_CHROME_EXTENSIONS = BrowserConstants.KEY_CHROME_EXTENSIONS;

	/**
	 * @see BrowserConstants#KEY_CHROME_EXTENSION_MODHEADER_PROFILE
	 */
	public static final String KEY_CHROME_EXTENSION_MODHEADER_PROFILE = BrowserConstants.KEY_CHROME_EXTENSION_MODHEADER_PROFILE;

	/** 'selenium.node' the key for selenium grid node string;
	 * The value is as "node1:port:nodeconfig;node2:port:nodeconfig;" */
	public static final String KEY_GRID_NODES_SETTING = SeleniumConstants.KEY_GRID_NODES_SETTING;

	/**'http.proxyHost'*/
	public static final String SYSTEM_PROPERTY_PROXY_HOST = StringUtils.SYSTEM_PROPERTY_PROXY_HOST;
	/**'http.proxyPort'*/
	public static final String SYSTEM_PROPERTY_PROXY_PORT = StringUtils.SYSTEM_PROPERTY_PROXY_PORT;
	/**'http.proxyBypass'*/
	public static final String SYSTEM_PROPERTY_PROXY_BYPASS = StringUtils.SYSTEM_PROPERTY_PROXY_BYPASS;
	/**'selenium.host'*/
	public static final String SYSTEM_PROPERTY_SELENIUM_HOST = SeleniumConstants.SYSTEM_PROPERTY_SELENIUM_HOST;
	/**'selenium.port'*/
	public static final String SYSTEM_PROPERTY_SELENIUM_PORT = SeleniumConstants.SYSTEM_PROPERTY_SELENIUM_PORT;
	/**'selenium.node', its value is like node1:port:nodeconfig;node2:port:nodeconfig;<br>
	 * semi-colon(;) serves as separator between nodes,<br>
	 * colon(:) serves as separator between nodename, port, and node-configuration.<br>
	 */
	public static final String SYSTEM_PROPERTY_SELENIUM_NODE = SeleniumConstants.SYSTEM_PROPERTY_SELENIUM_NODE;
	/**'webdriver.ie.driver'*/
	public static final String SYSTEM_PROPERTY_WEBDRIVER_IE = SeleniumConstants.SYSTEM_PROPERTY_WEBDRIVER_IE;
	/**'webdriver.chrome.driver'*/
	public static final String SYSTEM_PROPERTY_WEBDRIVER_CHROME = SeleniumConstants.SYSTEM_PROPERTY_WEBDRIVER_CHROME;
	/**'webdriver.edge.driver'*/
	public static final String SYSTEM_PROPERTY_WEBDRIVER_EDGE = SeleniumConstants.SYSTEM_PROPERTY_WEBDRIVER_EDGE;

	/**'BROWSER'*/
	public static final String SYSTEM_PROPERTY_BROWSER_NAME = SeleniumConstants.SYSTEM_PROPERTY_BROWSER_NAME;
	/**'BROWSER_REMOTE'*/
	public static final String SYSTEM_PROPERTY_BROWSER_REMOTE = SeleniumConstants.SYSTEM_PROPERTY_BROWSER_REMOTE;

	/**'<b>explorer</b>'*/
	public static final String BROWSER_NAME_IE = BrowserConstants.BROWSER_NAME_IE;
	/**'<b>chrome</b>'*/
	public static final String BROWSER_NAME_CHROME = BrowserConstants.BROWSER_NAME_CHROME;
	/**'<b>firefox</b>'*/
	public static final String BROWSER_NAME_FIREFOX = BrowserConstants.BROWSER_NAME_FIREFOX;
	/**'<b>safari</b>'*/
	public static final String BROWSER_NAME_SAFARI = BrowserConstants.BROWSER_NAME_SAFARI;
	/**'<b>MicrosoftEdge</b>'*/
	public static final String BROWSER_NAME_EDGE = BrowserConstants.BROWSER_NAME_EDGE;
	/**'<b>ChromiumEdge</b>'*/
	public static final String BROWSER_NAME_CHROMIUM_EDGE = BrowserConstants.BROWSER_NAME_CHROMIUM_EDGE;
	/**'<b>htmlunit</b>'*/
	public static final String BROWSER_NAME_HTMLUNIT = BrowserConstants.BROWSER_NAME_HTMLUNIT;
	/** '<b>android.chrome</b>' chrome browser on android */
	public static final String BROWSER_NAME_ANDROID_CHROME = BrowserConstants.BROWSER_NAME_ANDROID_CHROME;
	/** '<b>ipad.safari</b>' safari browser on ios */
	public static final String BROWSER_NAME_IPAD_SAFARI = BrowserConstants.BROWSER_NAME_IPAD_SAFARI;
	/** '<b>ipad.sim.safari</b>' simulator on ios */
	public static final String BROWSER_NAME_IPAD_SIMULATOR_SAFARI = BrowserConstants.BROWSER_NAME_IPAD_SIMULATOR_SAFARI;

	/**'localhost'*/
	public static final String DEFAULT_SELENIUM_HOST = SeleniumConfigConstant.DEFAULT_SELENIUM_HOST;
	/** '127.0.0.1'*/
	public static final String DEFAULT_SELENIUM_HOST_IP = SeleniumConfigConstant.DEFAULT_SELENIUM_HOST_IP;

	/** 4444 */
	public static final int DEFAULT_SELENIUM_PORT_INT = SeleniumConfigConstant.DEFAULT_SELENIUM_PORT_INT;
	/**'4444'*/
	public static final String DEFAULT_SELENIUM_PORT = String.valueOf(DEFAULT_SELENIUM_PORT_INT);

	/**
	 *
	 * @param browserName String, the browser name, such as "explorer"
	 * @return WebDriver
	 * @see #getBrowserInstance(String, HashMap)
	 */
	public static WebDriver getBrowserInstance(String browserName) {
		return getBrowserInstance(browserName, null);
	}

	/**
	 * Get HTTP proxy settings from System properties and set them to Map object.
	 *
	 * @param extraParameters Map<String,Object>, the original parameters Map
	 * @return Map<String,Object>, the parameters Map containing proxy settings from System properties if they exist.
	 */
	private static Map<String,Object> prepareHttpProxy(Map<String,Object> extraParameters) {
		//Get proxy settings from System properties
		String proxy = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_PROXY_HOST);
		if(StringUtils.isValid(proxy)){
			if(extraParameters==null){
				extraParameters = new HashMap<String,Object>();
			}
			String proxysetting = proxy;
			String port = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_PROXY_PORT);
			if(StringUtils.isValid(port)) proxysetting += ":"+port;
			//should NOT over-write that provided in extraParameters
			if(!extraParameters.containsKey(KEY_PROXY_SETTING)){
				extraParameters.put(KEY_PROXY_SETTING, proxysetting);
			}
		}
		if(extraParameters!=null){
			String bypass = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_PROXY_BYPASS);
			if(StringUtils.isValid(bypass) &&
			   extraParameters.containsKey(KEY_PROXY_SETTING) &&
			   //should NOT over-write that provided in extraParameters
			   !extraParameters.containsKey(KEY_PROXY_BYPASS_ADDRESS)){

				extraParameters.put(KEY_PROXY_BYPASS_ADDRESS, bypass);
			}
		}

		return extraParameters;
	}

	/**
	 *
	 * @param browserName String, the browser name, such as "explorer".  If null, then the
	 * System.property {@link #SYSTEM_PROPERTY_BROWSER_NAME} is sought. If not set, then the
	 * default {@link #BROWSER_NAME_FIREFOX} is used.
	 *
	 * @param extraParameters HashMap<String,Object>, can be used to pass more browser parameters, such as proxy settings.
	 * @return WebDriver
	 */
	public static WebDriver getBrowserInstance(String browserName, Map<String,Object> extraParameters) {
		WebDriver instance = null;
		DesiredCapabilities caps = null;

		if(browserName == null || browserName.length()==0){
			browserName = System.getProperty(SYSTEM_PROPERTY_BROWSER_NAME);
			if(browserName == null || browserName.length()==0){
				browserName = BROWSER_NAME_FIREFOX;
				System.setProperty(SYSTEM_PROPERTY_BROWSER_NAME, browserName);
			}
		}

		String browserNameLC = browserName.toLowerCase();

		//Get proxy settings from System properties
		extraParameters = prepareHttpProxy(extraParameters);

		//Prepare the Capabilities
		if(extraParameters!=null && !extraParameters.isEmpty()){
			caps = getDesiredCapabilities(browserNameLC, extraParameters);
		}

		String installdir = System.getenv("SELENIUM_PLUS");

		//Create the Driver
		if (browserNameLC.contains(BROWSER_NAME_IE)) {
			System.setProperty(SYSTEM_PROPERTY_WEBDRIVER_IE, installdir + "/extra/IEDriverServer.exe");
			instance = (caps!=null)? new InternetExplorerDriver(caps):new InternetExplorerDriver();
		} else if (browserNameLC.equals(BROWSER_NAME_CHROME)) {
			System.setProperty(SYSTEM_PROPERTY_WEBDRIVER_CHROME, installdir + "/extra/chromedriver.exe");
			instance = (caps!=null)? new ChromeDriver(caps):new ChromeDriver();
		} else if (browserNameLC.equals(BROWSER_NAME_EDGE)) {
			System.setProperty(SYSTEM_PROPERTY_WEBDRIVER_EDGE, installdir + "/extra/MicrosoftWebDriver.exe");
			instance = (caps!=null)? new EdgeDriver(caps):new EdgeDriver();
		} else if (browserNameLC.equals(BROWSER_NAME_ANDROID_CHROME)) {
			System.setProperty(SYSTEM_PROPERTY_WEBDRIVER_CHROME, installdir + "/extra/chromedriver.exe");
			instance = (caps!=null)? new ChromeDriver(caps):new ChromeDriver();
		} else { // default browser always
			instance = (caps!=null)? new FirefoxDriver(caps):new FirefoxDriver();
		}
		return instance;
	}

	/**
	 *
	 * @param browserName String, the browser name, such as "explorer"
	 * @return DesiredCapabilities
	 * @see #getDesiredCapabilities(String, HashMap)
	 */
	public static DesiredCapabilities getDesiredCapabilities(String browserName) {
		return getDesiredCapabilities(browserName, null);
	}

	/**
	 * @param currentBrowser String, the name of current browser where the test is running on.<br>
	 *                               This is name is normally got from getDriver().getCapabilities().getBrowserName().<br>
	 * @param expectedBrowser String, the expected browser's name.
	 *                                It can be the simplified name such as<br>
	 *                                {@link #BROWSER_NAME_CHROME}<br>
	 *                                {@link #BROWSER_NAME_EDGE}<br>
	 *                                {@link #BROWSER_NAME_FIREFOX}<br>
	 *                                {@link #BROWSER_NAME_IE}<br>
	 *                                Or it can be the browser name got from Capabilities, such as<br>
	 *                                DesiredCapabilities.internetExplorer().getBrowserName()<br>
	 *                                DesiredCapabilities.chrome().getBrowserName()<br>
	 *                                DesiredCapabilities.edge().getBrowserName()<br>
	 *                                DesiredCapabilities.firefox().getBrowserName()<br>
	 *                                <br>
	 *
	 * @return boolean if currentBrowser equals expectedBrowser
	 */
	public static boolean isBrowser(String currentBrowser, String expectedBrowser) {
		if(currentBrowser==null || expectedBrowser==null){
			throw new IllegalArgumentException("Either currentBrowser is null or expectedBrowser is null.");
		}

		if(currentBrowser.trim().equalsIgnoreCase(expectedBrowser.trim())) return true;

		//If the expectedBrowser is simplified name, then we need to get the "browser name" from Capabilities to compare with the currentBrowser's name
		if(BROWSER_NAME_IE.equalsIgnoreCase(expectedBrowser)){
			expectedBrowser = DesiredCapabilities.internetExplorer().getBrowserName();
		}else if(BROWSER_NAME_CHROME.equalsIgnoreCase(expectedBrowser)){
			expectedBrowser = DesiredCapabilities.chrome().getBrowserName();
		}else if(BROWSER_NAME_EDGE.equalsIgnoreCase(expectedBrowser)){
			expectedBrowser = DesiredCapabilities.edge().getBrowserName();
		}else if(BROWSER_NAME_FIREFOX.equalsIgnoreCase(expectedBrowser)){
			expectedBrowser = DesiredCapabilities.firefox().getBrowserName();
		}

		if(currentBrowser.trim().equalsIgnoreCase(expectedBrowser.trim())) return true;

		return false;
	}

	/**
	 *
	 * @param browserName String, the browser name, such as "explorer"
	 * @param extraParameters Map<String,Object>, can be used to pass more browser parameters, such as proxy settings.
	 * @return DesiredCapabilities
	 */
	public static DesiredCapabilities getDesiredCapabilities(String browserName, Map<String,Object> extraParameters) {
		String debugmsg = StringUtils.debugmsg(false);
		DesiredCapabilities caps = null;

		//Get proxy settings from System properties
		extraParameters = prepareHttpProxy(extraParameters);

		if (browserName.equals(BROWSER_NAME_IE)) {
			System.setProperty(SYSTEM_PROPERTY_WEBDRIVER_IE, "IEDriverServer.exe");
			caps = DesiredCapabilities.internetExplorer();
			caps.setCapability("nativeEvents", true);
			caps.setCapability("requireWindowFocus", true);

		} else if (browserName.equals(BROWSER_NAME_CHROME)) {
			// does this change if testing Chrome on Linux or Mac?
			System.setProperty(SYSTEM_PROPERTY_WEBDRIVER_CHROME, "chromedriver.exe");
			caps = DesiredCapabilities.chrome();

		} else if (browserName.equals(BROWSER_NAME_EDGE)) {
			System.setProperty(SYSTEM_PROPERTY_WEBDRIVER_EDGE, "MicrosoftWebDriver.exe");
			caps = DesiredCapabilities.edge();

		}  else if (browserName.equals(BROWSER_NAME_CHROMIUM_EDGE)) {
			// does this change if testing Chromium Edge on Linux or Mac?  Is that supported?
			System.setProperty(SYSTEM_PROPERTY_WEBDRIVER_EDGE, "msedgedriver.exe");
			caps = DesiredCapabilities.edge();

		} else if (browserName.equals(BROWSER_NAME_ANDROID_CHROME)) {
			// does this change if testing Chrome on Linux or Mac?
			System.setProperty(SYSTEM_PROPERTY_WEBDRIVER_CHROME, "chromedriver.exe");
			caps = DesiredCapabilities.chrome();
			ChromeOptions chromeOptions = new ChromeOptions();
			chromeOptions.setExperimentalOption("androidPackage", "com.android.chrome");
			caps.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
			caps.setCapability(CapabilityType.BROWSER_NAME, BROWSER_NAME_CHROME);
		} else if (browserName.equals(BROWSER_NAME_IPAD_SIMULATOR_SAFARI)) {
			caps = new DesiredCapabilities();
			caps.setCapability("device", "ipad");
			caps.setCapability("simulator", "true");
			caps.setCapability(CapabilityType.BROWSER_NAME, BROWSER_NAME_SAFARI);
		} else if (browserName.equals(BROWSER_NAME_SAFARI)) {
			caps = DesiredCapabilities.safari();
		} else { // default browser always
			caps = DesiredCapabilities.firefox();
			caps.setCapability(CapabilityType.BROWSER_NAME, BROWSER_NAME_FIREFOX);
		}

		//Setting 'unexpectedAlertBehaviour' capability will cause error for FireFox with selenium3.4.0 and geckodriver-v0.18.0
		//Set 'unexpectedAlertBehaviour' capability was fixing problem in IE browser
		//So move these codes to be executed for IE browser ONLY
		//Lei: set capability "unexpectedAlertBehaviour" for all browsers, user can turn if off for the error case (FireFox with selenium3.4.0 and geckodriver-v0.18.0).
		String unexpectedAlertBehaviour = Processor.getUnexpectedAlertBehaviour();
		if(unexpectedAlertBehaviour==null) unexpectedAlertBehaviour = System.getProperty(DriverConstant.PROPERTY_SAFS_TEST_UNEXPECTEDALERTBEHAVIOUR);
		if(unexpectedAlertBehaviour!=null && !unexpectedAlertBehaviour.equalsIgnoreCase("off")){
			IndependantLog.debug(debugmsg+" Set '"+unexpectedAlertBehaviour+"' to '"+CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR+"'.");
			caps.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, unexpectedAlertBehaviour);
		}

		if(extraParameters!=null && !extraParameters.isEmpty()){
			//1. Add http proxy settings to Capabilities, if they exist
			Object proxysetting = extraParameters.get(KEY_PROXY_SETTING);
			if(proxysetting!=null && proxysetting instanceof String){
				org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
				proxy.setHttpProxy(proxysetting.toString());

				Object bypass = extraParameters.get(KEY_PROXY_BYPASS_ADDRESS);
				if(bypass!=null && bypass instanceof String){
					proxy.setNoProxy(bypass.toString());
				}

				caps.setCapability(CapabilityType.PROXY, proxy);
			}

			//2 Add firefox profile setting to Capabilities.
			if(BROWSER_NAME_FIREFOX.equals(browserName) ){
				//2.1 Add firefox profile setting to Capabilities, if it exists
				FirefoxProfile firefoxProfile = null;
				Object firefoxProfileParam = extraParameters.get(KEY_FIREFOX_PROFILE);
				if(firefoxProfileParam!=null && firefoxProfileParam instanceof String){
					//Can be profile's name or profile's file name
					String profileNameOrPath = firefoxProfileParam.toString();
					IndependantLog.debug(debugmsg+"Try to Set firefox profile '"+profileNameOrPath+"' to Capabilities.");

					firefoxProfile = getFirefoxProfile(profileNameOrPath);

					if(firefoxProfile!=null){
						caps.setCapability(KEY_FIREFOX_PROFILE, profileNameOrPath);//used to store in session file
					}else{
						IndependantLog.error(debugmsg+" Fail to set firefox profile to Capabilities.");
					}
				}
				//2.2 Add firefox profile preferences to Capabilities, if it exists
				Object prefsFileParam = extraParameters.get(KEY_FIREFOX_PROFILE_PREFERENCE);
				if(prefsFileParam!=null && prefsFileParam instanceof String){
					String preferenceFile = prefsFileParam.toString();
					IndependantLog.debug(debugmsg+"Try to Set firefox preference file '"+preferenceFile+"' to Firefox Profile.");
					caps.setCapability(KEY_FIREFOX_PROFILE_PREFERENCE, preferenceFile);//used to store in session file

					Map<?, ?> firefoxPreference = Json.readJSONFileUTF8(preferenceFile);
					if(firefoxProfile==null) firefoxProfile = new FirefoxProfile();
					if(firefoxPreference==null){
						//preferenceFile is not a file, try it as JSON-string
						IndependantLog.debug(debugmsg+"Try to Set firefox preference '"+preferenceFile+"' to Firefox Profile.");
						firefoxPreference = fromJsonString(preferenceFile, Map.class);
					}
					addFireFoxPreference(firefoxProfile, firefoxPreference);
				}

				if(firefoxProfile!=null){
					caps.setCapability(FirefoxDriver.PROFILE, firefoxProfile);
				}
			}

			//3. Add chrome-options-settings to Capabilities.
			if(BROWSER_NAME_CHROME.equals(browserName) ||
			   BROWSER_NAME_ANDROID_CHROME.equals(browserName)){
				setChromeCapabilities(caps, extraParameters);
			}

			//put extra grid-nodes information
			Object gridnodes = extraParameters.get(KEY_GRID_NODES_SETTING);
			if(gridnodes!=null && gridnodes instanceof String){
				caps.setCapability(KEY_GRID_NODES_SETTING, gridnodes);
			}

			//put the custom-capabilities
			try{
				String custom_capabilities = StringUtilities.getString(extraParameters, BrowserConstants.KEY_CUSTOM_CAPABILITIES);
				//custom_capabilities is probably provided as a file containing json data.
				IndependantLog.debug(debugmsg+"User provided custom capabilities '"+custom_capabilities+"'.");
				Map<?, ?> customCapabilitesMap = Json.readJSONFileUTF8(custom_capabilities);
				if(customCapabilitesMap==null){
					//custom_capabilities_file is not a file, try it as JSON-string
					customCapabilitesMap = fromJsonString(custom_capabilities, Map.class);
				}
				IndependantLog.debug(debugmsg+"Try to add custom capabilities '"+customCapabilitesMap+"'.");

				//used to store in session file
				caps.setCapability(BrowserConstants.KEY_CUSTOM_CAPABILITIES, customCapabilitesMap);

				//each key will also be stored separately in the capabilities
				String[] keys = customCapabilitesMap.keySet().toArray(new String[0]);
				for(String key:keys){
					caps.setCapability(key, customCapabilitesMap.get(key));
				}

			}catch(Exception e){
				IndependantLog.warn(debugmsg+"Fail to add custom capabilities: met "+e.getMessage());
			}

		}

		return caps;
	}

	/**
	 * Add chrome-options-settings to Capabilities for chrome browser.<br>
	 * How to set the capabilities, refer to following 2 links:<br>
	 * https://sites.google.com/a/chromium.org/chromedriver/capabilities<br>
	 * http://peter.sh/experiments/chromium-command-line-switches/<br>
	 * @param caps DesiredCapabilities, a chrome DesiredCapabilities.
	 * @param extraParameters Map<String,Object>, contains chrome specific parameters pair (key, value),
	 *                                            such as "user-data-dir", "profile-directory" and "excludeSwitches" etc.
	 * @see #getDesiredCapabilities(String, Map)
	 */
	private static void setChromeCapabilities(DesiredCapabilities caps, Map<String,Object> extraParameters){
		String debugmsg = StringUtils.debugmsg(false);
		ChromeOptions options = null;
		if(caps==null || extraParameters==null || extraParameters.isEmpty()){
			IndependantLog.debug(debugmsg+" caps is null or there are no browser specific parametes to set.");
			return;
		}

		try{
			//Get the general data setting directory, it is for all users
			String chromeUserDataDir = StringUtilities.getString(extraParameters, KEY_CHROME_USER_DATA_DIR);
			IndependantLog.debug(debugmsg+"Try to Set chrome user data directory '"+chromeUserDataDir+"' to ChromeOptions.");
			options = new ChromeOptions();
			if(!chromeUserDataDir.isEmpty()){
				caps.setCapability(KEY_CHROME_USER_DATA_DIR, chromeUserDataDir);//used to store in session file
				options.addArguments(KEY_CHROME_USER_DATA_DIR+"="+chromeUserDataDir);
			}
		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Fail to Set chrome user data directory to ChromeOptions: "+e.getMessage());
		}
		try{
			//Get user-specific settings directory, it is for one user
			String profiledir = StringUtilities.getString(extraParameters, KEY_CHROME_PROFILE_DIR);
			IndependantLog.debug(debugmsg+"Try to Set chrome profile directory '"+profiledir+"' to ChromeOptions.");
			if(options==null) options = new ChromeOptions();
			if(!profiledir.isEmpty()){
				caps.setCapability(KEY_CHROME_PROFILE_DIR, profiledir);//used to store in session file
				options.addArguments(KEY_CHROME_PROFILE_DIR+"="+profiledir);
			}
		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Fail to Set chrome profile directory to ChromeOptions: "+e.getMessage());
		}

		try {
			// Disable extensions to avoid popping up 'Disable developer mode extensions' message.
			String disableExtensionsOptions = StringUtilities.getString(extraParameters, KEY_CHROME_DISABLE_EXTENSIONS);
			boolean toDisable = BrowserConstants.DEFAULT_DISABLE_CHROME_EXTENSION;
			if(StringUtils.isValid(disableExtensionsOptions)){
				toDisable = disableExtensionsOptions.equalsIgnoreCase("true");
			}
			if(toDisable){
				if(options == null) options = new ChromeOptions();
				options.addArguments(KEY_CHROME_DISABLE_EXTENSIONS);
				IndependantLog.debug(debugmsg + "Disabled Chrome extensions: '" + disableExtensionsOptions + "'.");
			}
		} catch(Exception e){
			IndependantLog.warn(debugmsg + "Fail to set disable Chrome extensions for ChromeOptions: "+e.getMessage());
		}

		try{
			//Get user command-line-options/preferences file (it contains command-line-options and/or preferences), and set them to ChromeOptions
			String commandLineOptions_preferenceFile = StringUtilities.getString(extraParameters, KEY_CHROME_PREFERENCE);
			IndependantLog.debug(debugmsg+"Try to Set chrome command-line-options/preferences file '"+commandLineOptions_preferenceFile+"' to ChromeOptions.");
			Map<?, ?> commandLineOptions = Json.readJSONFileUTF8(commandLineOptions_preferenceFile);
			if(commandLineOptions==null){
				//commandLineOptions_preferenceFile is not a file, try it as JSON-string
				IndependantLog.debug(debugmsg+"Try to Set chrome command-line-options/preferences '"+commandLineOptions_preferenceFile+"' to ChromeOptions.");
				commandLineOptions = fromJsonString(commandLineOptions_preferenceFile, Map.class);
			}
			if(options==null) options = new ChromeOptions();
			caps.setCapability(KEY_CHROME_PREFERENCE, commandLineOptions_preferenceFile);//used to store in session file

			//Set Chrome Preferences
			if(commandLineOptions.containsKey(KEY_CHROME_PREFERENCE_JSON_KEY)){
				Map<?, ?> preferences = null;
				try{
					preferences = (Map<?, ?>) commandLineOptions.get(KEY_CHROME_PREFERENCE_JSON_KEY);
					IndependantLog.debug(debugmsg+"Setting preferences "+ preferences +" to ChromeOptions.");
					options.setExperimentalOption(KEY_CHROME_PREFS, preferences);
				}catch(Exception e){
					IndependantLog.warn(debugmsg+"Failed to Set chrome preferences to ChromeOptions, due to "+StringUtils.debugmsg(e));
				}
				//remove the preferences from the Map object chromeCommandLineOptions, then the map will only contain "command line options".
				commandLineOptions.remove(KEY_CHROME_PREFERENCE_JSON_KEY);
			}

			//Set Chrome Command Line Options
			IndependantLog.debug(debugmsg+"Setting command line options "+ commandLineOptions +" to ChromeOptions.");
			addChromeCommandLineOptions(options, commandLineOptions);

		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Fail to Set chrome preference file to ChromeOptions: "+e.getMessage());
		}

		try{
			//Get user-specific exclude options
			String excludeOptions = StringUtilities.getString(extraParameters, KEY_CHROME_EXCLUDE_OPTIONS);
			IndependantLog.debug(debugmsg+"Try to Set chrome excludeSwitches '"+excludeOptions+"' to ChromeOptions.");
			if(options==null) options = new ChromeOptions();
			if(!excludeOptions.isEmpty()){
				caps.setCapability(KEY_CHROME_EXCLUDE_OPTIONS, excludeOptions);//used to store in session file
				List<String> excludeOptionsList = null;
				if(excludeOptions.contains(StringUtils.COMMA)){
					excludeOptionsList = StringUtils.getTrimmedTokenList(excludeOptions, StringUtils.COMMA);
				}else{
					excludeOptionsList = StringUtils.getTrimmedTokenList(excludeOptions, StringUtils.SEMI_COLON);
				}

				options.setExperimentalOption(KEY_CHROME_EXCLUDE_OPTIONS, excludeOptionsList);
			}
		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Fail to Set chrome excludeSwitches to ChromeOptions: "+e.getMessage());
		}

		try{
			//Get experimental options, and set them to ChromeOptions
			String experimentalOptionsFile = StringUtilities.getString(extraParameters, KEY_CHROME_EXPERIMENTAL_OPTIONS);
			IndependantLog.debug(debugmsg+"Try to Set chrome experimental options file '"+experimentalOptionsFile+"' to ChromeOptions.");
			caps.setCapability(KEY_CHROME_EXPERIMENTAL_OPTIONS, experimentalOptionsFile);//used to store in session file
			Map<?, ?> experimentalOptions = Json.readJSONFileUTF8(experimentalOptionsFile);
			if(experimentalOptions==null){
				//experimentalOptionsFile is not a file, try it as JSON-string
				IndependantLog.debug(debugmsg+"Try to Set chrome experimental options '"+experimentalOptionsFile+"' to ChromeOptions.");
				experimentalOptions = fromJsonString(experimentalOptionsFile, Map.class);
			}
			if(options==null) options = new ChromeOptions();

			//Set Chrome experimental options
			IndependantLog.debug(debugmsg+"Setting experimental options "+ experimentalOptions +" to ChromeOptions.");
			addChromeExperimentalOptions(options, experimentalOptions);

		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Fail to Set chrome preference file to ChromeOptions: "+e.getMessage());
		}

		try{
			String loadExtensionFile = StringUtilities.getString(extraParameters, KEY_CHROME_LOAD_EXTENSIONS);
			IndependantLog.debug(debugmsg+"Try to Set chrome extensions file '"+loadExtensionFile+"' to ChromeOptions.");
			//for session file
			caps.setCapability(KEY_CHROME_LOAD_EXTENSIONS, loadExtensionFile);

			Map<?, ?> loadExtensions = Json.readJSONFileUTF8(loadExtensionFile);

			if(loadExtensions==null){
				//loadExtensionFile is not a file, try it as JSON-string
				IndependantLog.debug(debugmsg+"Try to Set chrome extensions '"+loadExtensionFile+"' to ChromeOptions.");
				loadExtensions = fromJsonString(loadExtensionFile, Map.class);
			}

			//an array of .crx extension files
			List<String> extensions = (List<String>) loadExtensions.get(KEY_CHROME_EXTENSIONS);
			List<File> extensionFiles = new ArrayList<File>();
			for(String extension: extensions){
				extensionFiles.add(new File(extension));
			}

			Object extensionModheaderProfile = loadExtensions.get(KEY_CHROME_EXTENSION_MODHEADER_PROFILE);
			if(extensionModheaderProfile!=null){
				caps.setCapability(KEY_CHROME_EXTENSION_MODHEADER_PROFILE, extensionModheaderProfile);
			}

			if(options==null) options = new ChromeOptions();
			options.addExtensions(extensionFiles);

		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Fail to Set chrome extension to ChromeOptions: "+e.getMessage());
		}

		try {
			//Parse '--no-sandbox' parameters to add into ChromeOptions
			String sandboxOption = StringUtilities.getString(extraParameters, BrowserConstants.KEY_CHROME_NO_SANDBOX);
			IndependantLog.debug(debugmsg + "Try to set Chrome --no-sandbox option '" + sandboxOption + "'.");
			if(options == null) options = new ChromeOptions();
			if(!sandboxOption.isEmpty() && sandboxOption.toLowerCase().equals("true")){
				options.addArguments(BrowserConstants.KEY_CHROME_NO_SANDBOX);
			}
		} catch(Exception e){
			IndependantLog.warn(debugmsg + "Fail to set Chrome --no-sandbox option for ChromeOptions: "+e.getMessage());
		}

		if(options!=null) caps.setCapability(ChromeOptions.CAPABILITY, options);

		try {
			//Parse 'setNetworkConditions' parameters to add into capabilities
			String networkConditions = StringUtilities.getString(extraParameters, BrowserConstants.KEY_SET_NETWORK_CONDITIONS);
			IndependantLog.debug(debugmsg + "Try to set Chrome networkConditions '" + networkConditions + "'.");
			caps.setCapability(BrowserConstants.KEY_SET_NETWORK_CONDITIONS, networkConditions);
		} catch(Exception e){
			IndependantLog.warn(debugmsg + "Fail to set Chrome --no-sandbox option for ChromeOptions: "+e.getMessage());
		}
	}

	/**
	 * Convert json data or CSV data into a certain type.
	 * @param jsonOrCSVData String, the json data or CSV data.
	 * @param type the type to convert the data.
	 * @return T
	 */
	private static <T> T fromJsonString(String jsonOrCSVData, Class<T> type){
		T result = null;
		String debugmsg = StringUtils.debugmsg(false);

		try{
			result = Json.fromJsonString(jsonOrCSVData, type);
		}catch(Exception e){
			IndependantLog.warn(debugmsg+ " For string "+jsonOrCSVData+"\nFailed to convert it to type "+type.getName()+", due to "+e.toString());
			IndependantLog.debug(debugmsg+" Suppose user provides CSV data, convert it into json string.");
			String[] pairs = StringUtils.getTokenArray(jsonOrCSVData, ",");
			StringBuilder sb = new StringBuilder();
			int index = -1;
			String key = null;
			String value = null;
			sb.append("{");
			for(String pair:pairs){
				index = pair.indexOf(":");
				if(index>0){
					key = pair.substring(0, index);
					value = pair.substring(index+1);
					key = key.trim();
					value = value.trim();
					if(!StringUtils.isQuoted(key)) key = StringUtils.quote(key);
					sb.append(key);
					sb.append(":");
					if(!StringUtils.isQuoted(value) &&
					   !"true".equals(value.toLowerCase()) &&
					   !"false".equals(value.toLowerCase()) &&
					   !value.matches("-?\\d+\\.?\\d*") &&
					   !value.startsWith("[") &&
					   !value.startsWith("{")){
						//don't quote "true", "false", number, array ([a,b,c]) or json-string
						value = StringUtils.quote(value);
					}
					sb.append(value);
					sb.append(",");
				}else{
					IndependantLog.warn(debugmsg+"skipped the non-valid pair "+pair);
				}
			}
			//remove the last comma
			sb.deleteCharAt(sb.length()-1);
			sb.append("}");

			result = Json.fromJsonString(sb.toString(), type);
		}

		return result;
	}

	/**
	 * Set <a href="http://peter.sh/experiments/chromium-command-line-switches/">chrome command line options</a> to ChromeOptions object.<br>
	 *
	 * @param options ChromeOptions, the chrome options object.
	 * @param commandLineOptions Map, the chrome preference to set.
	 */
	private static void addChromeCommandLineOptions(ChromeOptions options, Map<?, ?> commandLineOptions){

		try{
			//options.addArguments("--lang=zh-cn");
			//options.addArguments("--start-maximized");
			//options.addArguments("--disable-logging");

			String[] keys = commandLineOptions.keySet().toArray(new String[0]);
			for(String key:keys){
				//If the value is empty like options.addArguments("--disable-logging"), remove the "="?
				//It seems that it still work even with "=", options.addArguments("--disable-logging=")
				options.addArguments("--"+key.trim()+"="+commandLineOptions.get(key));
			}
		}catch(Exception e){
			IndependantLog.error(StringUtils.debugmsg(false)+" failed, due to "+StringUtils.debugmsg(e));
		}
	}

	private static void addChromeExperimentalOptions(ChromeOptions options, Map<?, ?> experimentalOptions){

		try{
			String[] keys = experimentalOptions.keySet().toArray(new String[0]);
			for(String key:keys){
				//TODO do we need to convert the value to a certain type? Probably NOT.
				//for "prefs", it needs a Map
				//for "excludeSwitches", it needs a List
				/** 'experimentalOption.dat' file does work, they got set correctly into the ChromeOptions
{
    "useAutomationExtension" : false,
    "prefs" : {
    	"credentials_enable_service" : false,
    	"profile" : { "password_manager_enabled" : false }
    },
    "excludeSwitches" : ["enable-automation"]
}
				 */
				options.setExperimentalOption(key, experimentalOptions.get(key));
			}

		}catch(Exception e){
			IndependantLog.error(StringUtils.debugmsg(false)+" failed, due to "+StringUtils.debugmsg(e));
		}
	}

	/**
	 * Set firefox preference to firefox Profile.<br>
	 *
	 * @param firefoxProfile FirefoxProfile, the firefox Profile object.
	 * @param firefoxPreference Map, the firefox preference to set.
	 */
	private static void addFireFoxPreference(FirefoxProfile firefoxProfile, Map<?, ?> firefoxPreference){

		String debugmsg = StringUtils.debugmsg(false);
		if(firefoxPreference==null){
			IndependantLog.error(debugmsg+" The parameter 'firefoxPreference' is null!");
			return;
		}

		try{
			//profile.setPreference("media.navigator.permission.disabled", true); boolean
			//profile.setPreference("browser.download.folderList",2); int
			//profile.setPreference("intl.accept_languages", "en-us" ); String

			String[] keys = firefoxPreference.keySet().toArray(new String[0]);
			Object value = null;
			for(String key:keys){
				try{
					value = firefoxPreference.get(key);
					if(value instanceof Boolean){
						firefoxProfile.setPreference(key, ((Boolean)value).booleanValue());
					}else if(value instanceof Number){
						firefoxProfile.setPreference(key, ((Number)value).intValue());
					}else{
						firefoxProfile.setPreference(key, value.toString());
					}
				}catch(IllegalArgumentException e){
					IndependantLog.warn(debugmsg+" failed set preference '"+key+"' to '"+value+"', due to "+StringUtils.debugmsg(e));
				}
			}

		}catch(Exception e){
			IndependantLog.error(debugmsg+" failed set preference to firefox profile, due to "+StringUtils.debugmsg(e));
		}
	}

	/**
	 * Create a FirefoxProfile according to a profile's name of profile's filename.
	 * @param profileNameOrPath String, Can be profile's name or profile's file name (absolute)
	 * @return FirefoxProfile, can be null
	 */
	public static FirefoxProfile getFirefoxProfile(String profileNameOrPath){
		String debugmsg = StringUtils.debugmsg(false);
		FirefoxProfile profile = null;

		IndependantLog.debug(debugmsg+" Get firefox profile by '"+profileNameOrPath+"'.");
		try{
			File profileFile = new File(profileNameOrPath);
			if(profileFile.exists()){
				profile = new FirefoxProfile(profileFile);
			}
		}catch(Exception ignore){}

		if(profile==null){
			ProfilesIni allProfiles = new ProfilesIni();
			profile = allProfiles.getProfile(profileNameOrPath);
		}

		if(profile==null) IndependantLog.error(debugmsg+" Fail to get firefox profile.");

		return profile;
	}

}
