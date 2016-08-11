/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib;

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
*  SEP 04, 2014    (LeiWang) Handle FireFox's profile.
*  MAR 17, 2015    (LeiWang) Handle Chrome's custom data, profile.
*  APR 08, 2015    (LeiWang) Modify to turn off Chrome's starting options.
*  MAR 07, 2016    (LeiWang) Handle preference setting for "chrome" and "firefox".
*  MAR 23, 2016    (LeiWang) Modify setChromeCapabilities(): handle "command line options" and "preferences" for "chrome".
*  APR 26, 2016    (LeiWang) Modify getDesiredCapabilities(): Get value of 'unexpectedAlertBehaviour' from Processor and 'System property'
*                                                             and set it for all browsers.
*/

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
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
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
	public static final String KEY_PROXY_SETTING = "KEY_PROXY_SETTING";
	/** 'KEY_PROXY_BYPASS_ADDRESS' the key for proxy bypass address string; 
	 * The value is comma separated string as "localhost,***REMOVED***,***REMOVED***" */
	public static final String KEY_PROXY_BYPASS_ADDRESS = "KEY_PROXY_BYPASS_ADDRESS";
	/** 'FirefoxProfile' the key for firefox profile name/filename string; 
	 * The value is something like "myprofile" or "&lt;AbsolutePath>/ppc2784x.default" */
	public static final String KEY_FIREFOX_PROFILE = "FirefoxProfile";//Name Or FilePath

	/** 'firefox.perference' the key for firefox preference file, which contains json data, 
	 * such as { "intl.accept_languages":"zh-cn", "accessibility.accesskeycausesactivation":false, "browser.download.folderList":2 }<br>
	 * <b>Note: Be careful when creating the json data file, do NOT quote boolean or integer value.</b>*/
	public static final String KEY_FIREFOX_PROFILE_PREFERENCE = "firefox.perference";//Firefox Preference file
	
	/** 'chrome.perference' the key for chrome command-line-options/preferences file, which contains 
	 * <ol>
	 * <li><b>command-line-options</b> json data, such as { "lang":"zh-cn", "disable-download-notification":"" }, 
	 *     refer to <a href="http://peter.sh/experiments/chromium-command-line-switches/">detail options</a>
	 * <li><b>preferences</b> json data, it is indicated by a special key {@link #KEY_CHROME_PREFERENCE_JSON_KEY}, 
	 *     such as { "<b>seplus.chrome.preference.json.key</b>": { "intl.accept_languages":"zh-cn", intl.charset_default:"utf-8" } }, 
	 *     refer to <a href="https://src.chromium.org/viewvc/chrome/trunk/src/chrome/common/pref_names.cc">detail preferences</a>
	 * <ol>
	 */
	public static final String KEY_CHROME_PREFERENCE = "chrome.perference";//"Chrome Command Line Options" and "Chrome Preferences" file
	
	/** 'seplus.chrome.preference.json.key' the key for chrome preferences, which points to json data, 
	 * such as { "intl.accept_languages":"zh-cn", intl.charset_default:"utf-8" }, 
	 * refer to <a href="https://src.chromium.org/viewvc/chrome/trunk/src/chrome/common/pref_names.cc">detail preferences</a>*/
	public static final String KEY_CHROME_PREFERENCE_JSON_KEY = "seplus.chrome.preference.json.key";//Chrome Preferences
	
	/** 'prefs' the key used to set chrome Experimental Option. Used internally. */
	private static final String KEY_CHROME_PREFS = "prefs";//setExperimentalOption
	
	/**'user-data-dir' the parameter name for chrome options, a general custom data settings.<br>
	 * The value is specified in <a href="http://peter.sh/experiments/chromium-command-line-switches">chrome options</a><br>
	 * <b>Note:</b> As this {@value #KEY_CHROME_USER_DATA_DIR} contains minus, it could be interpreted as an arithmetic expression, 
	 *             Use SeleniumPlus.quote({@value #KEY_CHROME_USER_DATA_DIR}) to keep its value.
	 * @see #KEY_CHROME_PROFILE_DIR
	 **/
	public static final String KEY_CHROME_USER_DATA_DIR = "user-data-dir";
	
	/**'profile-directory' the parameter name for chrome options, a user-specific settings, it indicates a sub-folder under "user data directory".<br>
	* The value is specified in <a href="http://peter.sh/experiments/chromium-command-line-switches">chrome options</a><br>
	* <b>Note:</b> As this {@value #KEY_CHROME_PROFILE_DIR} contains minus, it could be interpreted as an arithmetic expression, 
	*             Use SeleniumPlus.quote({@value #KEY_CHROME_PROFILE_DIR}) to keep its value.
	* @see #KEY_CHROME_USER_DATA_DIR
	**/
	public static final String KEY_CHROME_PROFILE_DIR = "profile-directory";
	
	/**
	 * 'excludeSwitches' the experimental option name for chrome options, it is used to turn off chrome starting options.<br>
	 * The value is separated-options to exclude, the separator can be comma(,) or semicolon(;) , <br>
	 * like "disable-component-update, ignore-certificate-errors" or "disable-component-update; ignore-certificate-errors",<br>
	 * <b>be careful</b>, there are NO 2 hyphens before options, "--disable-component-update, --ignore-certificate-errors" is wrong.<br>
	 * <b>Note:</b> As the value, excluded-options, may contain minus like "disable-component-update", it could be interpreted as an arithmetic expression, 
	 *             Use SeleniumPlus.quote("disable-component-update") to keep its value.
	 */
	public static final String KEY_CHROME_EXCLUDE_OPTIONS = "excludeSwitches";
	
	/**
	 * '--disable-extensions' is used to disable the use of Chrome extensions. Usually, we use it
	 * as DEFAULT to avoid popping up 'Disable developer mode extensions' message.
	 * 
	 * In 'SeleniumPlus.StartWebBrowser()' or 'WDLibrary.startBrowser()', we can use 'false' value to cancel this default 'disable' setting.
	 * E.g.
	 *     WDLibrary.startBrowser(BrowserName, Url, Id, timeout, isRemote, quote(SelectBrowser.KEY_CHROME_DISABLE_EXTENSIONS), "false");
	 * 
	 */
	public static final String KEY_CHROME_DISABLE_EXTENSIONS = "--disable-extensions";

	/**'http.proxyHost'*/
	public static final String SYSTEM_PROPERTY_PROXY_HOST = StringUtils.SYSTEM_PROPERTY_PROXY_HOST;
	/**'http.proxyPort'*/
	public static final String SYSTEM_PROPERTY_PROXY_PORT = StringUtils.SYSTEM_PROPERTY_PROXY_PORT;
	/**'http.proxyBypass'*/
	public static final String SYSTEM_PROPERTY_PROXY_BYPASS = StringUtils.SYSTEM_PROPERTY_PROXY_BYPASS;
	/**'selenium.host'*/
	public static final String SYSTEM_PROPERTY_SELENIUM_HOST = "selenium.host";
	/**'selenium.port'*/
	public static final String SYSTEM_PROPERTY_SELENIUM_PORT = "selenium.port";
	/**'selenium.node', its value is like node1:port:nodeconfig;node2:port:nodeconfig;<br>
	 * semi-colon(;) serves as separator between nodes,<br>
	 * colon(:) serves as separator between nodename, port, and node-configuration.<br>
	 */
	public static final String SYSTEM_PROPERTY_SELENIUM_NODE = "selenium.node";
	/**'webdriver.ie.driver'*/
	public static final String SYSTEM_PROPERTY_WEBDRIVER_IE = "webdriver.ie.driver";
	/**'webdriver.chrome.driver'*/
	public static final String SYSTEM_PROPERTY_WEBDRIVER_CHROME = "webdriver.chrome.driver";
	/**'webdriver.edge.driver'*/
	public static final String SYSTEM_PROPERTY_WEBDRIVER_EDGE = "webdriver.edge.driver";
	
	/**'BROWSER'*/
	public static final String SYSTEM_PROPERTY_BROWSER_NAME = "BROWSER";
	/**'BROWSER_REMOTE'*/
	public static final String SYSTEM_PROPERTY_BROWSER_REMOTE = "BROWSER_REMOTE";
	
	/** 'selenium.node' the key for selenium grid node string; 
	 * The value is as "node1:port:nodeconfig;node2:port:nodeconfig;" */
	public static final String KEY_GRID_NODES_SETTING = SYSTEM_PROPERTY_SELENIUM_NODE;
	
	/**'<b>explorer</b>'*/
	public static final String BROWSER_NAME_IE = "explorer";
	/**'<b>chrome</b>'*/
	public static final String BROWSER_NAME_CHROME = "chrome";
	/**'<b>firefox</b>'*/
	public static final String BROWSER_NAME_FIREFOX = "firefox";
	/**'<b>safari</b>'*/
	public static final String BROWSER_NAME_SAFARI = "safari";
	/**'<b>MicrosoftEdge</b>'*/
	public static final String BROWSER_NAME_EDGE = "MicrosoftEdge";
	/**'<b>htmlunit</b>'*/
	public static final String BROWSER_NAME_HTMLUNIT = "htmlunit";
	/** '<b>android.chrome</b>' chrome browser on android */
	public static final String BROWSER_NAME_ANDROID_CHROME = "android.chrome";
	/** '<b>ipad.safari</b>' safari browser on ios */
	public static final String BROWSER_NAME_IPAD_SAFARI = "ipad.safari";
	/** '<b>ipad.sim.safari</b>' simulator on ios */
	public static final String BROWSER_NAME_IPAD_SIMULATOR_SAFARI = "ipad.sim.safari";
	
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
	 * 
	 * @param browserName String, the browser name, such as "explorer".  If null, then the 
	 * System.property {@link #SYSTEM_PROPERTY_BROWSER_NAME} is sought. If not set, then the 
	 * default {@link #BROWSER_NAME_FIREFOX} is used.
	 * 
	 * @param extraParameters HashMap<String,Object>, can be used to pass more browser parameters, such as proxy settings.
	 * @return WebDriver
	 */
	public static WebDriver getBrowserInstance(String browserName, HashMap<String,Object> extraParameters) {
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
		
		//Prepare the Capabilities
		if(extraParameters==null || extraParameters.isEmpty()){
			//Get proxy settings from System properties
			String proxy = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_PROXY_HOST);
			String port = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_PROXY_PORT);
			if(proxy!=null && !proxy.isEmpty()){
				String proxysetting = proxy;
				if(port!=null && !port.isEmpty()) proxysetting += ":"+port;
				extraParameters.put(KEY_PROXY_SETTING, proxysetting);
				
				String bypass = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_PROXY_BYPASS);
				if(proxy!=null && !proxy.isEmpty()){
					extraParameters.put(KEY_PROXY_BYPASS_ADDRESS, bypass);
				}
			}
		}
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
	 * 
	 * @param browserName String, the browser name, such as "explorer"
	 * @param extraParameters Map<String,Object>, can be used to pass more browser parameters, such as proxy settings.
	 * @return DesiredCapabilities
	 */
	public static DesiredCapabilities getDesiredCapabilities(String browserName, Map<String,Object> extraParameters) {
		String debugmsg = StringUtils.debugmsg(false);
		DesiredCapabilities caps = null;
		
		if (browserName.equals(BROWSER_NAME_IE)) {
			System.setProperty(SYSTEM_PROPERTY_WEBDRIVER_IE, "IEDriverServer.exe");
			caps = DesiredCapabilities.internetExplorer();
			caps.setCapability("nativeEvents", true);
			caps.setCapability("requireWindowFocus", true);
			//caps.setCapability("browserName", BROWSER_NAME_IE);
		} else if (browserName.equals(BROWSER_NAME_CHROME)) {
			System.setProperty(SYSTEM_PROPERTY_WEBDRIVER_CHROME, "chromedriver.exe");
			caps = DesiredCapabilities.chrome();
			
			// Disable extensions to avoid popping up 'Disable developer mode extensions' message by default.
			if(!extraParameters.containsKey(KEY_CHROME_DISABLE_EXTENSIONS)) {
				// Only execute if no user's setting
				extraParameters.put(KEY_CHROME_DISABLE_EXTENSIONS, "true");					
			}
			
			//caps.setCapability("browserName", BROWSER_NAME_CHROME);
		} else if (browserName.equals(BROWSER_NAME_EDGE)) {
			System.setProperty(SYSTEM_PROPERTY_WEBDRIVER_EDGE, "MicrosoftWebDriver.exe");
			caps = DesiredCapabilities.edge();
			//caps.setCapability("browserName", BROWSER_NAME_EDGE);
		} else if (browserName.equals(BROWSER_NAME_ANDROID_CHROME)) {
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
			caps.setCapability(CapabilityType.BROWSER_NAME, "safari");
		} else { // default browser always
			caps = DesiredCapabilities.firefox();
			caps.setCapability(CapabilityType.BROWSER_NAME, BROWSER_NAME_FIREFOX);
		}
		
		String unexpectedAlertBehaviour = Processor.getUnexpectedAlertBehaviour();
		if(unexpectedAlertBehaviour==null) unexpectedAlertBehaviour = System.getProperty(DriverConstant.PROERTY_SAFS_TEST_UNEXPECTEDALERTBEHAVIOUR);
		if(unexpectedAlertBehaviour!=null){
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
			IndependantLog.warn(debugmsg+"Fail to Set chrome user data directory to ChromeOptions.");
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
			IndependantLog.warn(debugmsg+"Fail to Set chrome profile directory to ChromeOptions.");
		}		
		
		try {
			// Parse '--disable-extensions' parameters to add into ChromeOptions
			String disableExtensionsOptions = StringUtilities.getString(extraParameters, KEY_CHROME_DISABLE_EXTENSIONS);
			IndependantLog.debug(debugmsg + "Try to disable Chrome extensions: '" + disableExtensionsOptions + "'.");
			if(options == null) options = new ChromeOptions();
			if(!disableExtensionsOptions.isEmpty() && disableExtensionsOptions.toLowerCase().equals("true")){
				options.addArguments(KEY_CHROME_DISABLE_EXTENSIONS);
			}
		} catch(Exception e){
			IndependantLog.warn(debugmsg + "Fail to set disable Chrome extensions for ChromeOptions.");
		}
		
		try{
			//Get user command-line-options/preferences file (it contains command-line-options and/or preferences), and set them to ChromeOptions
			String commandLineOptions_preferenceFile = StringUtilities.getString(extraParameters, KEY_CHROME_PREFERENCE);
			IndependantLog.debug(debugmsg+"Try to Set chrome command-line-options/preferences file '"+commandLineOptions_preferenceFile+"' to ChromeOptions.");
			Map<?, ?> commandLineOptions = Json.readJSONFileUTF8(commandLineOptions_preferenceFile);
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
			IndependantLog.warn(debugmsg+"Fail to Set chrome preference file to ChromeOptions.");
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
			IndependantLog.warn(debugmsg+"Fail to Set chrome excludeSwitches to ChromeOptions.");
		}
		if(options!=null) caps.setCapability(ChromeOptions.CAPABILITY, options);
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
	
	/**
	 * Set firefox preference to firefox Profile.<br>
	 * 
	 * @param firefoxProfile FirefoxProfile, the firefox Profile object.
	 * @param firefoxPreference Map, the firefox preference to set.
	 */
	private static void addFireFoxPreference(FirefoxProfile firefoxProfile, Map<?, ?> firefoxPreference){
		
		try{
			//profile.setPreference("media.navigator.permission.disabled", true); boolean
			//profile.setPreference(“browser.download.folderList”,2); int
			//profile.setPreference( “intl.accept_languages”, “en-us” ); String
			
			String[] keys = firefoxPreference.keySet().toArray(new String[0]);
			Object value = null;
			for(String key:keys){
				value = firefoxPreference.get(key);
				if(value instanceof Boolean){
					firefoxProfile.setPreference(key, ((Boolean)value).booleanValue());
				}else if(value instanceof Number){
					firefoxProfile.setPreference(key, ((Number)value).intValue());					
				}else{
					firefoxProfile.setPreference(key, value.toString());					
				}
			}
			
		}catch(Exception e){
			IndependantLog.error(StringUtils.debugmsg(false)+" failed set preference to firefox profile, due to "+StringUtils.debugmsg(e));
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
	
	/**
	 * Return an array of keys for extra parameters.<br>
	 * The format of value for each key is different, please refer to comment of the key.<br>
	 * 
	 * @return String[], an array of keys used to pass extra parameter in SeleniumPlus
	 * @see #KEY_PROXY_BYPASS_ADDRESS
	 * @see #KEY_PROXY_SETTING
	 * @see #KEY_FIREFOX_PROFILE
	 * @see #KEY_FIREFOX_PROFILE_PREFERENCE
	 * @see #KEY_CHROME_PREFERENCE
	 * @see #KEY_CHROME_USER_DATA_DIR
	 * @see #KEY_CHROME_PROFILE_DIR
	 * @see #KEY_CHROME_EXCLUDE_OPTIONS
	 */
	public static String[] getExtraParameterKeys(){
		List<String> keys = new ArrayList<String>();
		
		keys.add(KEY_PROXY_SETTING);
		keys.add(KEY_PROXY_BYPASS_ADDRESS);
		keys.add(KEY_FIREFOX_PROFILE);
		keys.add(KEY_FIREFOX_PROFILE_PREFERENCE);
		keys.add(KEY_CHROME_PREFERENCE);
		keys.add(KEY_CHROME_USER_DATA_DIR);
		keys.add(KEY_CHROME_PROFILE_DIR);
		keys.add(KEY_CHROME_EXCLUDE_OPTIONS);
		
		return keys.toArray(new String[0]);
		
	}
}
