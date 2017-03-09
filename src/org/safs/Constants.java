/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * NOV 09, 2016    (SBJLWA) Initial release: Created BrowserConstants and SeleniumConstants: copied constants from org.safs.selenium.webdriver.lib.SelectBrowser.
 * MAR 07, 2017    (SBJLWA) Add constants for setting of the browser-driver in Selenium.
 */
package org.safs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Containing different kinds of Constant.<br>
 *
 */
public class Constants {

	/**'localhost'*/
	public static final String LOCAL_HOST = "localhost";
	/** '127.0.0.1'*/
	public static final String LOCAL_HOST_IP = "127.0.0.1";

	/** 'NOAUTHENTICATION'*/
	public static final String NO_AUTHENTICATION = "NOAUTHENTICATION";

	public static class BrowserConstants{
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
		/**'<b>operablink</b>'*/
		public static final String BROWSER_NAME_OPERA_BLINK = "operablink";
		/**'<b>opera</b>'*/
		public static final String BROWSER_NAME_OPERA = "opera";
		/**'<b>htmlunit</b>'*/
		public static final String BROWSER_NAME_HTMLUNIT = "htmlunit";
		/** '<b>android.chrome</b>' chrome browser on android */
		public static final String BROWSER_NAME_ANDROID_CHROME = "android.chrome";
		/** '<b>ipad.safari</b>' safari browser on ios */
		public static final String BROWSER_NAME_IPAD_SAFARI = "ipad.safari";
		/** '<b>ipad.sim.safari</b>' simulator on ios */
		public static final String BROWSER_NAME_IPAD_SIMULATOR_SAFARI = "ipad.sim.safari";

		//If you add a new KEY_XXX, please update the method getExtraParameterKeys() by adding it.
		/** 'KEY_PROXY_SETTING' the key for proxy string;
		 * The value is colon separated string as "proxyserver:port" */
		public static final String KEY_PROXY_SETTING = "KEY_PROXY_SETTING";
		/** 'KEY_PROXY_BYPASS_ADDRESS' the key for proxy bypass address string;
		 * The value is comma separated string as "localhost,tadsrv,rnd.sas.com" */
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
		public static final String KEY_CHROME_PREFS = "prefs";//setExperimentalOption

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
		 * @see #KEY_CHROME_DISABLE_EXTENSIONS
		 * @see SeleniumConstants#KEY_GRID_NODES_SETTING
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
			keys.add(KEY_CHROME_DISABLE_EXTENSIONS);
			keys.add(SeleniumConstants.KEY_GRID_NODES_SETTING);

			return keys.toArray(new String[0]);

		}
	}

	public static class SeleniumConstants{
		/**'selenium.host'*/
		public static final String SYSTEM_PROPERTY_SELENIUM_HOST = "selenium.host";
		/**'selenium.port'*/
		public static final String SYSTEM_PROPERTY_SELENIUM_PORT = "selenium.port";
		/**'selenium.node', its value is like node1:port:nodeconfig;node2:port:nodeconfig;<br>
		 * semi-colon(;) serves as separator between nodes,<br>
		 * colon(:) serves as separator between node-name, port, and node-configuration.<br>
		 */
		public static final String SYSTEM_PROPERTY_SELENIUM_NODE = "selenium.node";
		/**'webdriver.ie.driver'*/
		public static final String SYSTEM_PROPERTY_WEBDRIVER_IE = "webdriver.ie.driver";
		/**'webdriver.chrome.driver'*/
		public static final String SYSTEM_PROPERTY_WEBDRIVER_CHROME = "webdriver.chrome.driver";
		/**'webdriver.edge.driver'*/
		public static final String SYSTEM_PROPERTY_WEBDRIVER_EDGE = "webdriver.edge.driver";

		/**'BROWSER' Indicates the browser's name */
		public static final String SYSTEM_PROPERTY_BROWSER_NAME = "BROWSER";
		/**'BROWSER_REMOTE' true or false. */
		public static final String SYSTEM_PROPERTY_BROWSER_REMOTE = "BROWSER_REMOTE";

		/** 'selenium.node' the key for selenium grid node string;
		 * The value is as "node1:port:nodeconfig;node2:port:nodeconfig;" */
		public static final String KEY_GRID_NODES_SETTING = SYSTEM_PROPERTY_SELENIUM_NODE;

		/**
		 * "WEB_DRIVERS" defines a set of drivers to start with the selenium-server.<br>
		 * The value can be a combination (separated by a colon :) of<br>
		 * <ul>
		 * <li>{@link BrowserConstants#BROWSER_NAME_IE}
		 * <li>{@link BrowserConstants#BROWSER_NAME_CHROME}
		 * <li>{@link BrowserConstants#BROWSER_NAME_EDGE}
		 * <li>BrowserConstants.BROWSER_NAME_XXX might be supported.
		 * </ul>
		 * Examples:<br>
		 * <b>explorer</b> Only IEDriver will start with the selenium-server.<br>
		 * <b>explorer:chrome:MicrosoftEdge</b> IEDriver, ChromeDriver and EdgeDriver will start with the selenium-server.<br>
		 * */
		public static final String ITEM_WEB_DRIVERS ="WEB_DRIVERS";
		/** "safs.selenium.web.drivers" defines a set of drivers to start with the selenium-server.<br>
		 * The value can be a combination (separated by a colon :) of<br>
		 * <ul>
		 * <li>{@link BrowserConstants#BROWSER_NAME_IE}
		 * <li>{@link BrowserConstants#BROWSER_NAME_CHROME}
		 * <li>{@link BrowserConstants#BROWSER_NAME_EDGE}
		 * <li>BrowserConstants.BROWSER_NAME_XXX might be supported.
		 * </ul>
		 * Examples:<br>
		 * -Dsafs.selenium.web.drivers=<b>explorer</b> Only IEDriver will start with the selenium-server.<br>
		 * -Dsafs.selenium.web.drivers=<b>explorer:chrome:MicrosoftEdge</b> IEDriver, ChromeDriver and EdgeDriver will start with the selenium-server.<br>
		 * **/
		public static final String PROPERTY_WEB_DRIVERS ="safs.selenium.web.drivers";

		/**
		 * When starting the selenium server, the browser-drivers can be specified as VM parameters, such as<br/>
		 * -Dwebdriver.<b>ie</b>.driver=path\to\IEDriverServer.exe<br/>
		 * -Dwebdriver.<b>ie</b>.logfile=workspace\log\ie.console<br/>
		 * The VM parameter is in format webdriver.<b>&lt;browserShortName></b>.driver and webdriver.<b>&lt;browserShortName></b>.logfile,
		 * the browser names (NOT short name) have been defined in class {@link BrowserConstants} such as {@link BrowserConstants#BROWSER_NAME_EDGE} etc. But not all
		 * of them is suitable to specify the VM parameter, so this Map is created for mapping 'browser-name' to 'browser-short-name'. The 'browser-short-name'
		 * will be used to specify the VM parameter.
		 *
		 */
		public final static Map<String, String> DRIVER_SHORT_NAME_MAP = new HashMap<String, String>();
		static{
			DRIVER_SHORT_NAME_MAP.put(BrowserConstants.BROWSER_NAME_CHROME, "chrome");
			DRIVER_SHORT_NAME_MAP.put(BrowserConstants.BROWSER_NAME_IE, "ie");
			DRIVER_SHORT_NAME_MAP.put(BrowserConstants.BROWSER_NAME_EDGE, "edge");

			DRIVER_SHORT_NAME_MAP.put(BrowserConstants.BROWSER_NAME_FIREFOX, "firefox");
			DRIVER_SHORT_NAME_MAP.put(BrowserConstants.BROWSER_NAME_SAFARI, "safari");
			DRIVER_SHORT_NAME_MAP.put(BrowserConstants.BROWSER_NAME_OPERA, "opera");
		}
	}

	public static class XMLConstants{
		/**  "<![CDATA[" */
		public static final String CDATA_START = "<![CDATA[";
		/** "]]>" */
		public static final String CDATA_END = "]]>";
		/** "<?XML" */
		public static final String XML_START = "<?XML";
		/** "classname" */
		public static final String PROPERTY_CLASSNAME = "classname";
		/** "package" */
		public static final String PROPERTY_PACKAGE = "package";
		/** "&" */
		public static final String SYMBOL_AND = "&";
		/** "'" */
		public static final String SYMBOL_APOS = "'";
		/** "\"" */
		public static final String SYMBOL_QUOTE = "\"";
		/** "<" */
		public static final String SYMBOL_LESS = "<";
		/** ">" */
		public static final String SYMBOL_BIGGER = ">";
		/** An array of symbols needing escape in XML document, such as & ' " < > */
		public static final String[] SYMBOL_TO_ESCAPE = {SYMBOL_AND, SYMBOL_APOS, SYMBOL_QUOTE, SYMBOL_LESS, SYMBOL_BIGGER};
	}
}
