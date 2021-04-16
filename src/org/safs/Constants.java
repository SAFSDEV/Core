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
 * NOV 09, 2016    (Lei Wang) Initial release: Created BrowserConstants and SeleniumConstants: copied constants from org.safs.selenium.webdriver.lib.SelectBrowser.
 * MAR 07, 2017    (Lei Wang) Added constants for setting of the browser-driver in Selenium.
 * MAR 10, 2017    (Lei Wang) Added RestConstants.
 * APR 05, 2017    (Lei Wang) Added AutoItConstants.
 * APR 11, 2017    (Lei Wang) Added EclipseConstants and some other constant fields.
 * AUT 09, 2017    (Lei Wang) Mapped firefox browser to short name 'gecko' for starting selenium server 3.4.0.
 * APR 13, 2018    (Lei Wang) Added SAFS_LogConstants and SAFS_XML_LogConstants.
 * JUN 13, 2018    (Lei Wang) Added method escape() and needEscape() to XMLConstants.
 *                            Added constants (for generating JUNIT report) to SAFS_XML_LogConstants.
 *                            Added constants COUNTER_UNIT_XXX and related field/method.
 * JUL 03, 2018    (Lei Wang) Added RegistryConstants.
 * DEC 14, 2018    (Lei Wang) Added constants KEY_XXX, SEPARATOR_TESTCASE_STATUS.
 * JUN 17, 2019    (Lei Wang) Added constants KEY_SET_NETWORK_CONDITIONS.
 * APR 27, 2020    (Lei Wang) Added constants KEY_CHROME_LOAD_EXTENSIONS, KEY_CHROME_EXTENSIONS and KEY_CHROME_EXTENSION_MODHEADER_PROFILE.
 */
package org.safs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.safs.android.auto.lib.Console;
import org.safs.tools.drivers.DriverConstant.SeleniumConfigConstant;

/**
 * Containing different kinds of Constant.<br>
 *
 */
public abstract class Constants {

	/**'<b>localhost</b>'*/
	public static final String LOCAL_HOST = "localhost";
	/** '<b>127.0.0.1</b>'*/
	public static final String LOCAL_HOST_IP = "127.0.0.1";

	/** '<b>NOAUTHENTICATION</b>'*/
	public static final String NO_AUTHENTICATION = "NOAUTHENTICATION";

	/** '<b>META-INF</b>' */
	public static final String METAINF  = "META-INF";
	/** '<b>MANIFEST.MF</b>' */
	public static final String MANIFEST = "MANIFEST.MF";

	/** '<b>SELENIUM_PLUS</b>' environment storing the installation directory of Selenium Plus. */
	public static final String ENV_SELENIUM_PLUS 		= "SELENIUM_PLUS";
	/** '<b>SAFSDIR</b>' environment storing the installation directory of SAFS. */
	public static final String ENV_SAFSDIR 				= "SAFSDIR";

	/** '<b>PATH</b>' environment storing the path to find executable, .dll etc. */
	public static final String ENV_PATH 			= "PATH";
	/** '<b>CLASSPATH</b>' environment storing path to find the jar files. */

	/** '<b>50%, 50%, 50%, 50%</b>' the center offsets for 2 components. */
	public static final String OFFSET_CENTER = "50%, 50%, 50%, 50%";

	/** '<b>50%</b>' offset fifty percent, means the center */
	public static final String OFFSET_FIFTY_PERCENT = "50%";

	/** '<b>trackingSystem</b>' the key to hold the remote tracking system in a map */
	public static final String KEY_TRACKING_SYSTEM 	= "trackingSystem";
	/** '<b>status</b>' the key to hold the test case status in a map */
	public static final String KEY_TRACKING_TESTCASE_STATUS 		= "status";
	/** '<b>comment</b>' the key to hold the test case comment in a map */
	public static final String KEY_TRACKING_TESTCASE_COMMENT 	= "comment";
	/** '<b>name</b>' the key to hold a name value in a map */
	public static final String KEY_NAME 					= "name";

	/** '<b>\t</b>' the separator to split the fields in the testcase status description. */
	public static final String SEPARATOR_TESTCASE_STATUS	= "\t";

	/** <b>{^</b> the prefix for the embedded variable inside a string */
	public static final String EMBEDDED_VAR_PREFIX              = "{^";
	/** <b>}</b> the suffix for the embedded variable inside a string */
	public static final String EMBEDDED_VAR_SUFFIX              = "}";

	public abstract static class RegistryConstants{
		/** 'HKLM\' registry path for HKEY_CURRENT_USER */
		public static final String HKCU  							= "HKCU\\";

		/** 'HKLM\' registry path for HKEY_LOCAL_MACHINE */
		public static final String HKLM  							= "HKLM\\";

		/** '<b>(Default)</b>' the registry default value */
		public static final String VALUE_DEFAULT					= "(Default)";

		/** '<b>HKLM\HARDWARE\DESCRIPTION\System\CentralProcessor\0</b>' the registry key for the first central processor */
		public static final String KEY_CENTRALPROCESSOR_0 			= HKLM+"HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0";

		/** '<b>Identifier</b>' the registry value for the central processor */
		public static final String VALUE_CENTRALPROCESSOR_ID 		= "Identifier";

		/** '<b>HKLM\System\CurrentControlSet\Control\Session Manager\Environment</b>' the registry key for the environment */
		public static final String KEY_ENVIRONMENT 					= HKLM+"System\\CurrentControlSet\\Control\\Session Manager\\Environment";

		/** '<b>Identifier</b>' the registry value for the 'PROCESSOR_ARCHITECTURE' */
		public static final String VALUE_PROCESSOR_ARCHITECTURE 	= "PROCESSOR_ARCHITECTURE";

		/** '<b>Identifier</b>' the registry value for the 'PROCESSOR_IDENTIFIER' */
		public static final String VALUE_PROCESSOR_IDENTIFIER 		= "PROCESSOR_IDENTIFIER";

		/** 'Wow6432Node' the field to append on registry path for 32-bit application on 64-bit OS */
		public static final String KEY_FIELD_WOW6432   				= "Wow6432Node";

		/** 'HKLM\Software\' registry path for 32-bit application on 32-bit OS or 64-bit application on 64-bit OS */
		public static final String HKLM_ST  						= HKLM+"Software\\";

		/** 'HKLM\Software\Wow6432Node' registry path for 32-bit application on 64-bit OS */
		public static final String HKLM_ST_WOW6432   				= HKLM_ST+KEY_FIELD_WOW6432+"\\";

	}

	/** '<b>2000</b>' in milliseconds, the default time for the mouse to hover.
	 * The mouse may move out of screen if this time expires. */
	public static final int TIMEOUT_HOVERMOUSE_DEFAULT 					= 2000;
	/** '<b>-1</b>' the timeout for the mouse to hover for ever */
	public static final int TIMEOUT_HOVERMOUSE_STAY_FOREVER				= -1;

	/** "customerSpringConfig.xml" */
	public static final String SPRING_CONFIG_CUSTOM_FILE = "customerSpringConfig.xml";
	/** "springConfig.xml" */
	public static final String SPRING_CONFIG_DEFAULT_FILE = "springConfig.xml";

	/** "log4j2.xml" */
	public static final String LOG4J2_CONFIG_FILE = "log4j2.xml";

	public abstract static class BrowserConstants{
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
		/**'<b>ChromiumEdge</b>'*/
		public static final String BROWSER_NAME_CHROMIUM_EDGE = "ChromiumEdge";
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
		/** '<b>KEY_PROXY_SETTING</b>' the key for proxy string;
		 * The value is colon separated string as "proxyserver:port" */
		public static final String KEY_PROXY_SETTING = "KEY_PROXY_SETTING";
		/** '<b>KEY_PROXY_BYPASS_ADDRESS</b>' the key for proxy bypass address string;
		 * The value is comma separated string as "localhost, internal.server" */
		public static final String KEY_PROXY_BYPASS_ADDRESS = "KEY_PROXY_BYPASS_ADDRESS";
		/** '<b>FirefoxProfile</b>' the key for firefox profile name/filename string;
		 * The value is something like "myprofile" or "&lt;AbsolutePath&gt;/ppc2784x.default" */
		public static final String KEY_FIREFOX_PROFILE = "FirefoxProfile";//Name Or FilePath

		/** '<b>firefox.perference</b>' the key for firefox preference file, which contains json data,
		 * such as { "intl.accept_languages":"zh-cn", "accessibility.accesskeycausesactivation":false, "browser.download.folderList":2 }<br>
		 * <b>Note: Be careful when creating the json data file, do NOT quote boolean or integer value.</b>*/
		public static final String KEY_FIREFOX_PROFILE_PREFERENCE = "firefox.perference";//Firefox Preference file

		/** '<b>chrome.perference</b>' the key for chrome command-line-options/preferences file, which contains
		 * <ol>
		 * <li><b>command-line-options</b> json data, such as { "lang":"zh-cn", "disable-download-notification":"" },
		 *     refer to <a href="http://peter.sh/experiments/chromium-command-line-switches/">detail options</a>
		 * <li><b>preferences</b> json data, it is indicated by a special key {@link #KEY_CHROME_PREFERENCE_JSON_KEY},
		 *     such as { "<b>seplus.chrome.preference.json.key</b>": { "intl.accept_languages":"zh-cn", "intl.charset_default":"utf-8" } },
		 *     refer to <a href="https://src.chromium.org/viewvc/chrome/trunk/src/chrome/common/pref_names.cc">detail preferences</a>
		 * </ol>
		 */
		public static final String KEY_CHROME_PREFERENCE = "chrome.perference";//"Chrome Command Line Options" and "Chrome Preferences" file

		/** '<b>seplus.chrome.preference.json.key</b>' the key for chrome preferences, which points to json data,
		 * such as { "intl.accept_languages":"zh-cn", "intl.charset_default":"utf-8" },
		 * refer to <a href="https://src.chromium.org/viewvc/chrome/trunk/src/chrome/common/pref_names.cc">detail preferences</a><br>
	     * <b>NOTE: We can also specify this by key "prefs" for {@link #KEY_CHROME_EXPERIMENTAL_OPTIONS}.</b>
		 */
		public static final String KEY_CHROME_PREFERENCE_JSON_KEY = "seplus.chrome.preference.json.key";//Chrome Preferences

		/** The chrome Experimental Option <b>prefs</b>. Used internally. */
		public static final String KEY_CHROME_PREFS = "prefs";

		/** '<b>experimentalOptions</b>' the key for chrome Experimental Options, it contains:
		 * <ol>
		 * <li><b>experimental options</b> json data, such as { "useAutomationExtension":"false",
		 *                                                      "prefs": { "intl.accept_languages":"zh-cn", "intl.charset_default":"utf-8" },
		 *                                                      "excludeSwitches": ["enable-automation", "disable-component-update", "ignore-certificate-errors"]}
		 * </ol>
		 */
		public static final String KEY_CHROME_EXPERIMENTAL_OPTIONS = "experimentalOptions";//setExperimentalOption

		/**'<b>user-data-dir</b>' the parameter name for chrome options, a general custom data settings.<br>
		 * The value is specified in <a href="http://peter.sh/experiments/chromium-command-line-switches">chrome options</a><br>
		 * <b>Note:</b> As this {@value #KEY_CHROME_USER_DATA_DIR} contains minus, it could be interpreted as an arithmetic expression,
		 *             Use SeleniumPlus.quote({@value #KEY_CHROME_USER_DATA_DIR}) to keep its value.
		 * @see #KEY_CHROME_PROFILE_DIR
		 **/
		public static final String KEY_CHROME_USER_DATA_DIR = "user-data-dir";

		/**'<b>profile-directory</b>' the parameter name for chrome options, a user-specific settings, it indicates a sub-folder under "user data directory".<br>
		* The value is specified in <a href="http://peter.sh/experiments/chromium-command-line-switches">chrome options</a><br>
		* <b>Note:</b> As this {@value #KEY_CHROME_PROFILE_DIR} contains minus, it could be interpreted as an arithmetic expression,
		*             Use SeleniumPlus.quote({@value #KEY_CHROME_PROFILE_DIR}) to keep its value.
		* @see #KEY_CHROME_USER_DATA_DIR
		**/
		public static final String KEY_CHROME_PROFILE_DIR = "profile-directory";

		/**
		 * '<b>excludeSwitches</b>' the experimental option name for chrome options, it is used to turn off chrome starting options.<br>
		 * The value is separated-options to exclude, the separator can be comma(,) or semicolon(;) , <br>
		 * like "disable-component-update, ignore-certificate-errors" or "disable-component-update; ignore-certificate-errors",<br>
		 * <b>be careful</b>, there are NO 2 hyphens before options, "--disable-component-update, --ignore-certificate-errors" is wrong.<br>
		 * <b>Note:</b> As the value, excluded-options, may contain minus like "disable-component-update", it could be interpreted as an arithmetic expression,
		 *             Use SeleniumPlus.quote("disable-component-update") to keep its value.<br>
		 * <b>NOTE: We can also specify this by key "excludeSwitches" for {@link #KEY_CHROME_EXPERIMENTAL_OPTIONS}.</b>
		 */
		public static final String KEY_CHROME_EXCLUDE_OPTIONS = "excludeSwitches";

		/**
		 * '<b>--disable-extensions</b>' is used to disable the use of Chrome extensions. Usually, we use it
		 * as DEFAULT to avoid popping up 'Disable developer mode extensions' message.<br>
		 *
		 * In 'SeleniumPlus.StartWebBrowser()' or 'WDLibrary.startBrowser()', we can use 'false' value to cancel this default 'disable' setting.<br>
		 * E.g.<br>
		 *     WDLibrary.startBrowser(BrowserName, Url, Id, timeout, isRemote, quote(SelectBrowser.KEY_CHROME_DISABLE_EXTENSIONS), "false");<br>
		 *
		 */
		public static final String KEY_CHROME_DISABLE_EXTENSIONS = "--disable-extensions";

		/**
		 * '<b>load-extensions</b>' is used to load Chrome extensions, and probably load profile for extensions when starting web browser.<br>
		 * It can be specified by a json file which contains {@link #KEY_CHROME_EXTENSIONS} and {@link #KEY_CHROME_EXTENSION_MODHEADER_PROFILE}.<br>
		 * {@link #KEY_CHROME_EXTENSIONS} is required, while {@link #KEY_CHROME_EXTENSION_MODHEADER_PROFILE} is optional.<br>
		 *
		 * In 'SeleniumPlus.StartWebBrowser()' or 'WDLibrary.startBrowser()', we can set browser's extensions to start with, and the profile to load for a certain extension.<br>
		 * The
		 * E.g.<br>
		 *     <pre>
		 *     WDLibrary.startBrowser(BrowserName, Url, Id, timeout, isRemote, quote(SelectBrowser.KEY_CHROME_LOAD_EXTENSIONS), quote("loadExtensions.json"));
		 *     The file "loadExtensions.json" contains "extensions" (required) and "extension-modheader-profile" (optional), such as:
		 *
		 *     {
		 *     "extensions": ["C:\\SeleniumPlus\\extra\\ModHeader.crx"],
		 *     }
		 *
		 *     or
		 *     {
		 *     "extensions": ["C:\\SeleniumPlus\\extra\\ModHeader.crx"],
		 *     "extension-modheader-profile": "C:\\SeleniumPlus\\extra\\ModHeader.json"
		 *     }
		 *
		 *     or
		 *     {
		 *     "extensions": ["C:\\SeleniumPlus\\extra\\ModHeader.crx"],
		 *     "extension-modheader-profile": "https://bewisse.com/modheader/p/#NobwRAhgDlCmB2ATAsge0bMAuAZhANgM6wA0YARhAMYDWA5gE6oCuSAwqvqg9mAMQA2cgFYAzMIBMYMjgCW+AC6wGhbMAC6ZABawIGFWvBVUAWxMIFvaWAQRy+WImwKGzUmHgRzvAB4BaQghCPygmKAg6CCU-WScyADcCN15EUwhZeGZiHgBfTTAtWNgOMwtnV3cGWEIoAAldfVUsDTJCLW4FABVZBQdeAEZrJR8FDi4eLH4caZmhnr7JgAUmOQcAAkGyZgZ8ACVYKHxqWHN4BSaNPKA"
		 *     }
		 *
		 *     or
		 *     {
		 *     "extensions": ["C:\\SeleniumPlus\\extra\\ModHeader.crx"],
		 *     "extension-modheader-profile": [{"appendMode":false,"backgroundColor":"#6b5352","filters":[],"headers":[{"comment":"","enabled":true,"name":"x-sas-propagate-id","value":"domainuser"}],"hideComment":true,"respHeaders":[],"shortTitle":"1","textColor":"#ffffff","title":"Profile 1","urlReplacements":[]}]
		 *     }
		 *     </pre>
		 *
		 * @see #KEY_CHROME_EXTENSIONS
		 * @see #KEY_CHROME_EXTENSION_MODHEADER_PROFILE
		 */
		public static final String KEY_CHROME_LOAD_EXTENSIONS = "load-extensions";

		/**
		 * '<b>extensions</b>' the key to indicate the browser's extensions in a json file, the extensions are expressed as an array.<br>
		 * This key:value pair will be used in the json file indicated by {@link #KEY_CHROME_LOAD_EXTENSIONS}.<br>
		 *
		 * E.g.<br>
		 *     <pre>
		 *     "extensions": ["C:\\SeleniumPlus\\extra\\ModHeader.crx", "C:\\SeleniumPlus\\extra\\anOtherExtension.crx"]
		 *     </pre>
		 *
		 * @see #KEY_CHROME_LOAD_EXTENSIONS
		 */
		public static final String KEY_CHROME_EXTENSIONS = "extensions";

		/**
		 * '<b>extension-modheader-profile</b>' the key to indicate the extension ModHeader's profile, the profile can be:<br>
		 *  <pre>
		 *  1. a json file containing the profile, such as "C:\\SeleniumPlus\\extra\\ModHeader.json"
		 *  2. a json string representing the profile, such as [{"appendMode":false,"backgroundColor":"#6b5352","filters":[],"headers":[{"comment":"","enabled":true,"name":"x-sas-propagate-id","value":"domainuser"}],"hideComment":true,"respHeaders":[],"shortTitle":"1","textColor":"#ffffff","title":"Profile 1","urlReplacements":[]}]
		 *  3. an "url" representing the profile, such as "https://bewisse.com/modheader/p/#NobwRAhgDlCmB2ATAsge0bMAuAZhANgM6wA0YARhAMYDWA5gE6oCuSAwqvqg9mAMQA2cgFYAzMIBMYMjgCW+AC6wGhbMAC6ZABawIGFWvBVUAWxMIFvaWAQRy+WImwKGzUmHgRzvAB4BaQghCPygmKAg6CCU-WScyADcCN15EUwhZeGZiHgBfTTAtWNgOMwtnV3cGWEIoAAldfVUsDTJCLW4FABVZBQdeAEZrJR8FDi4eLH4caZmhnr7JgAUmOQcAAkGyZgZ8ACVYKHxqWHN4BSaNPKA"
		 *  </pre>
		 *  This key:value pair will be used in the json file indicated by {@link #KEY_CHROME_LOAD_EXTENSIONS}.<br>
		 *
		 * E.g.<br>
		 *     <pre>
		 *     "extension-modheader-profile": "C:\\SeleniumPlus\\extra\\ModHeader.json"
		 *     "extension-modheader-profile": [{"appendMode":false,"backgroundColor":"#6b5352","filters":[],"headers":[{"comment":"","enabled":true,"name":"x-sas-propagate-id","value":"domainuser"}],"hideComment":true,"respHeaders":[],"shortTitle":"1","textColor":"#ffffff","title":"Profile 1","urlReplacements":[]}]
		 *     "extension-modheader-profile": "https://bewisse.com/modheader/p/#NobwRAhgDlCmB2ATAsge0bMAuAZhANgM6wA0YARhAMYDWA5gE6oCuSAwqvqg9mAMQA2cgFYAzMIBMYMjgCW+AC6wGhbMAC6ZABawIGFWvBVUAWxMIFvaWAQRy+WImwKGzUmHgRzvAB4BaQghCPygmKAg6CCU-WScyADcCN15EUwhZeGZiHgBfTTAtWNgOMwtnV3cGWEIoAAldfVUsDTJCLW4FABVZBQdeAEZrJR8FDi4eLH4caZmhnr7JgAUmOQcAAkGyZgZ8ACVYKHxqWHN4BSaNPKA"
		 *     </pre>
		 *
		 * @see #KEY_CHROME_LOAD_EXTENSIONS
		 */
		public static final String KEY_CHROME_EXTENSION_MODHEADER_PROFILE = "extension-modheader-profile";

		/**
		 * '<b>--no-sandbox</b>' is used to start Chrome without sandbox. Be default, this option is NOT set, which means the Chrome
		 * will run in a sandbox.<br>
		 * It is NOT recommended to set on this option, it is NOT safe. Please refer to <a href="https://chromium.googlesource.com/chromium/src/+/master/docs/design/sandbox.md">Google Sandbox Design</a> and
		 * <a href="https://www.google.com/googlebooks/chrome/med_26.html">Google Sandbox Cartoon</a>.<br>
		 * But sometimes this setting can help avoid the bugs in the implementation of sandbox or wrong configuration of system, we can temporally
		 * turn this option on. We MUST turn it off as soon as we find another solution.<br>
		 *
		 * <p>
		 * In 'SeleniumPlus.StartWebBrowser()' or 'WDLibrary.startBrowser()', we can set this option as below: <br>
		 * E.g.<br>
		 *     SeleniumPlus.StartWebBrowser(URL, BrowserID, BrowserName, timeout, isRemote, quote(BrowserConstants.KEY_CHROME_NO_SANDBOX), "true");<br>
		 *     WDLibrary.startBrowser(BrowserName, Url, Id, timeout, isRemote, quote(BrowserConstants.KEY_CHROME_NO_SANDBOX), "true");<br>
		 *
		 */
		public static final String KEY_CHROME_NO_SANDBOX 		 = "--no-sandbox";

		/**
		 * '<b>setNetworkConditions</b>' is used to start browser (now, for Chrome only) with network conditions (turn on this ability), which should be in JSON string.<br>
		 *                                  This JSON can contain keys of "offline"(boolean), "latency"(integer ms), "download_throughput"(integer bps), "upload_throughput"(integer bps).
		 *
		 * <p>
		 * In 'SeleniumPlus.StartWebBrowser()' or 'SAFSPlus.DriverCommand.StartWebBrowser()', we can set this option as below: <br>
		 * E.g.<br>
		 *     SeleniumPlus.StartWebBrowser(URL, BrowserID, BrowserName, timeout, isRemote, quote(BrowserConstants.KEY_SET_NETWORK_CONDITIONS), "{ \"offline\":false, \"latency\":5, \"download_throughput\":500000 , \"upload_throughput\":500000}");<br>
		 *     SAFSPlus.DriverCommand.StartWebBrowser(URL, BrowserID, BrowserName, timeout, isRemote, quote(BrowserConstants.KEY_SET_NETWORK_CONDITIONS), "{ \"offline\":false, \"latency\":5, \"download_throughput\":500000 , \"upload_throughput\":500000}");<br>
		 *
		 * <p>
		 * Once we started browser (now, for Chrome only) with this option '<b>setNetworkConditions</b>', we can get/delete/set 'network conditions' as below:<br>
		 *     String networkConditions = SAFSPlus.DriverCommand.GetNetworkConditions();<br>
		 *     boolean success = SAFSPlus.DriverCommand.DeleteNetworkConditions();<br>
		 *     SAFSPlus.DriverCommand.SetNetworkConditions("{ \"offline\":false, \"latency\":5, \"download_throughput\":500000 , \"upload_throughput\":500000}");<br>
		 *
		 */
		public static final String KEY_SET_NETWORK_CONDITIONS 		 = "setNetworkConditions";

		/** '<b>custom.capabilities</b>' the key for custom capabilities, which is json data,
		 *     such as { "key-1":"value", "key-2":"value" }, they will be added to the standard capabilities.
		 */
		public static final String KEY_CUSTOM_CAPABILITIES = "custom.capabilities";

		/**
		 * <b>true</b> Disable extensions to avoid popping up 'Disable developer mode extensions' message in chrome browser.
		 */
		public static final boolean DEFAULT_DISABLE_CHROME_EXTENSION = true;

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
		 * @see #KEY_CHROME_EXPERIMENTAL_OPTIONS
		 * @see #KEY_CHROME_USER_DATA_DIR
		 * @see #KEY_CHROME_PROFILE_DIR
		 * @see #KEY_CHROME_EXCLUDE_OPTIONS
		 * @see #KEY_CHROME_DISABLE_EXTENSIONS
		 * @see #KEY_CHROME_LOAD_EXTENSIONS
		 * @see SeleniumConstants#KEY_GRID_NODES_SETTING
		 * @see #KEY_CHROME_NO_SANDBOX
		 * @see #KEY_SET_NETWORK_CONDITIONS
		 * @see #KEY_CUSTOM_CAPABILITIES
		 */
		public static String[] getExtraParameterKeys(){
			List<String> keys = new ArrayList<String>();

			keys.add(KEY_PROXY_SETTING);
			keys.add(KEY_PROXY_BYPASS_ADDRESS);
			keys.add(KEY_FIREFOX_PROFILE);
			keys.add(KEY_FIREFOX_PROFILE_PREFERENCE);
			keys.add(KEY_CHROME_PREFERENCE);
			keys.add(KEY_CHROME_EXPERIMENTAL_OPTIONS);
			keys.add(KEY_CHROME_USER_DATA_DIR);
			keys.add(KEY_CHROME_PROFILE_DIR);
			keys.add(KEY_CHROME_EXCLUDE_OPTIONS);
			keys.add(KEY_CHROME_DISABLE_EXTENSIONS);
			keys.add(KEY_CHROME_LOAD_EXTENSIONS);
			keys.add(SeleniumConstants.KEY_GRID_NODES_SETTING);
			keys.add(KEY_CHROME_NO_SANDBOX);
			keys.add(KEY_SET_NETWORK_CONDITIONS);
			keys.add(KEY_CUSTOM_CAPABILITIES);

			return keys.toArray(new String[0]);

		}
	}

	public abstract static class EclipseConstants{

		/** "version" the property in file ".eclipseproduct" indicating the Eclipse's version. */
		public static final String PROPERTY_VERSION = "version";

		/** "eclipse.buildId" the property in file "config.ini" indicating the Eclipse's build ID. */
		public static final String PROPERTY_BUILDID = "eclipse.buildId";

		/** "4.5" for Eclipse Mars, refer to https://en.wikipedia.org/wiki/Eclipse_(software) */
		public static final String VERSION_NUMBER_MARS = "4.5";
		/** "4.5.2" is the version of Eclipse Mars (without CVS plugins) once provided with SeleniumPlus. */
		public static final String VERSION_NUMBER_MARS_4_5_2 = "4.5.2";

		public static final String[] PATTERN_PLUGINS_CVS_FOR_MARS = {
			"org\\.eclipse\\.cvs_.*\\.jar",
			"org\\.eclipse\\.team\\.cvs\\..*\\.jar"
		};

	}

	public abstract static class TestCompleteConstants{
		/** 'TESTCOMPLETE_HOME' environment variable to save the TestComplete installation directory. */
		public static final String TCHomeEnv = "TESTCOMPLETE_HOME";
		/** 'TESTCOMPLETE_EXE' environment variable to save the "name" of TestComplete's executable. */
		public static final String TCExeEnv  = "TESTCOMPLETE_EXE";

		/** 'TCAFS.pjs' the name of the test-complete suite name of TCAFS project. It is ususally under the folder c:\safs\tcafs*/
		public static final String DEFAULT_SUITENAME  	= "TCAFS.pjs";

		/** 'TCAFS' the name TCAFS project. */
		public static final String DEFAULT_PROJECTNAME 	= "TCAFS";

		/** 'StepDriver' the name of the script of TCAFS project. */
		public static final String DEFAULT_SCRIPTNAME 	= "StepDriver";

		/** '/rt:Main /e /SilentMode /ns' the default parameters to launch TestComplete . */
		public static final String DEFAULT_PARAMETERS 	= "/rt:Main /e /SilentMode /ns";

	}

	public abstract static class SeleniumConstants{
		/**'<b>selenium.host</b>' Indicates on which machine the selenium server is going to run. */
		public static final String SYSTEM_PROPERTY_SELENIUM_HOST = "selenium.host";
		/**'<b>selenium.port</b>' Indicates on which port the selenium server is going to run. */
		public static final String SYSTEM_PROPERTY_SELENIUM_PORT = "selenium.port";
		/**'<b>selenium.node</b>', its value is like node1:port:nodeconfig;node2:port:nodeconfig;<br>
		 * semi-colon(;) serves as separator between nodes,<br>
		 * colon(:) serves as separator between node-name, port, and node-configuration.<br>
		 */
		public static final String SYSTEM_PROPERTY_SELENIUM_NODE = "selenium.node";
		/**'<b>webdriver.ie.driver</b>'*/
		public static final String SYSTEM_PROPERTY_WEBDRIVER_IE = "webdriver.ie.driver";
		/**'<b>webdriver.chrome.driver</b>'*/
		public static final String SYSTEM_PROPERTY_WEBDRIVER_CHROME = "webdriver.chrome.driver";
		/**'<b>webdriver.edge.driver</b>'*/
		public static final String SYSTEM_PROPERTY_WEBDRIVER_EDGE = "webdriver.edge.driver";

		/**'<b>BROWSER</b>' Indicates the browser's name */
		public static final String SYSTEM_PROPERTY_BROWSER_NAME = "BROWSER";
		/**'<b>BROWSER_REMOTE</b>' true or false. */
		public static final String SYSTEM_PROPERTY_BROWSER_REMOTE = "BROWSER_REMOTE";

		/** '<b>selenium.node</b>' the key for selenium grid node string;
		 * The value is as "node1:port:nodeconfig;node2:port:nodeconfig;" */
		public static final String KEY_GRID_NODES_SETTING = SYSTEM_PROPERTY_SELENIUM_NODE;

		/**
		 * @deprecated please use {@link SeleniumConfigConstant#ITEM_WEB_DRIVERS} instead.
		 */
		@Deprecated
		public static final String ITEM_WEB_DRIVERS ="WEB_DRIVERS";
		/**
		 * @deprecated please use {@link SeleniumConfigConstant#PROPERTY_WEB_DRIVERS} instead.
		 */
		@Deprecated
		public static final String PROPERTY_WEB_DRIVERS ="safs.selenium.web.drivers";

		/**
		 * When starting the selenium server, the browser-drivers can be specified as VM parameters, such as<br>
		 * -Dwebdriver.<b>ie</b>.driver=path\to\IEDriverServer.exe<br>
		 * -Dwebdriver.<b>ie</b>.logfile=workspace\log\ie.console<br>
		 * The VM parameter is in format webdriver.<b>&lt;browserShortName&gt;</b>.driver and webdriver.<b>&lt;browserShortName&gt;</b>.logfile,
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
			DRIVER_SHORT_NAME_MAP.put(BrowserConstants.BROWSER_NAME_CHROMIUM_EDGE, "edge");

			DRIVER_SHORT_NAME_MAP.put(BrowserConstants.BROWSER_NAME_FIREFOX, "gecko");
			DRIVER_SHORT_NAME_MAP.put(BrowserConstants.BROWSER_NAME_SAFARI, "safari");
			DRIVER_SHORT_NAME_MAP.put(BrowserConstants.BROWSER_NAME_OPERA, "opera");
		}

		/**
		 * The default browsers whose drivers will be loaded with Selenium Sever when starting.<br>
		 * <ul>
		 * <li>{@link BrowserConstants#BROWSER_NAME_CHROME}
		 * <li>{@link BrowserConstants#BROWSER_NAME_EDGE}
		 * <li>{@link BrowserConstants#BROWSER_NAME_FIREFOX}
		 * <li>{@link BrowserConstants#BROWSER_NAME_IE}
		 * </ul>
		 */
		public final static String[] DEFAULT_SUPPORTED_BROWSERS = {
				BrowserConstants.BROWSER_NAME_CHROME,
				BrowserConstants.BROWSER_NAME_EDGE,
				BrowserConstants.BROWSER_NAME_FIREFOX,
				BrowserConstants.BROWSER_NAME_IE
		};

		public static String[] getDefaultSupportedBrowsers(){
			String[] defaultSupportedBrowsers = null;
			if(!Console.isWindowsOS()){
				defaultSupportedBrowsers = new String[2];
				defaultSupportedBrowsers[0] = BrowserConstants.BROWSER_NAME_CHROME;
				defaultSupportedBrowsers[1] = BrowserConstants.BROWSER_NAME_FIREFOX;
			}else{
				defaultSupportedBrowsers = DEFAULT_SUPPORTED_BROWSERS;
			}

			return defaultSupportedBrowsers;
		}
	}

	public abstract static class JSONConstants{
		/** "<b>$classname</b>" special reserved key to track the Object class name.*/
		public static final String PROPERTY_CLASSNAME = "$classname";
	}

	public abstract static class XMLConstants{
		/**  "<b>&lt;![CDATA[</b>" */
		public static final String CDATA_START = "<![CDATA[";
		/** "<b>]]&gt;</b>" */
		public static final String CDATA_END = "]]>";
		/** "<b>&lt;?XML</b>" */
		public static final String XML_START = "<?XML";
		/** "<b>classname</b>" */
		public static final String PROPERTY_CLASSNAME = "classname";
		/** "<b>package</b>" */
		public static final String PROPERTY_PACKAGE = "package";
		/** "<b>&amp;</b>" */
		public static final String SYMBOL_AND = "&";
		/** "<b>'</b>" */
		public static final String SYMBOL_APOS = "'";
		/** "<b>\"</b>" */
		public static final String SYMBOL_QUOTE = "\"";
		/** "<b>&lt;</b>" */
		public static final String SYMBOL_LESS = "<";
		/** "<b>&gt;</b>" */
		public static final String SYMBOL_BIGGER = ">";
		/** An array of symbols needing escape in XML document, such as <b>&amp; ' " &lt; &gt; </b> */
		public static final String[] SYMBOL_TO_ESCAPE = {SYMBOL_AND, SYMBOL_APOS, SYMBOL_QUOTE, SYMBOL_LESS, SYMBOL_BIGGER};

		/**
		 * Wrap the string in "<![CDATA[]]>" if it contains special symbols {@link XMLConstants#SYMBOL_TO_ESCAPE},
		 * please refer to {@link XMLConstants#needEscape(String)}.
		 *
		 * @param value String, the value to be escaped if it contains special symbols
		 * @return String, the escaped string
		 */
		public static String escape(String value){
			String result = value;

			if(needEscape(result)){
				result = XMLConstants.CDATA_START+result+XMLConstants.CDATA_END;
			}

			return result;
		}

		/**
		 * Some symbols, Such as "<?XML", ">", "<", "&", "'", "\"" etc., are not permitted in XML document and they need to be escaped.<br>
		 * Currently the symbols {@link #SYMBOL_TO_ESCAPE} will be checked.<br>
		 *
		 * @param value String, the value to test
		 * @return boolean if the value needs to be escaped.
		 */
		public static boolean needEscape(String value){
			if(value==null)
				return false;
			String valueUpperCase = value.toUpperCase().trim();
			if(valueUpperCase.startsWith(XML_START)){
				return true;
			}
			if(valueUpperCase.startsWith(CDATA_START) && valueUpperCase.endsWith(CDATA_END)){
				return false;
			}
			for(String symbol: SYMBOL_TO_ESCAPE){
				if(value.contains(symbol)) return true;
			}
			return false;
		}
	}

	/** "TESTSTEP" count test's failures, skipped etc. based on unit 'test step' */
	public static final String COUNTER_UNIT_TESTSTEP = "TESTSTEP";
	/** "TESTCASE" count test's failures, skipped etc. based on unit 'test case' */
	public static final String COUNTER_UNIT_TESTCASE = "TESTCASE";

	/**
	 * "TESTSTEP", "TESTCASE"
	 */
	public static final String[] VALID_COUTNER_UNITS = {COUNTER_UNIT_TESTSTEP, COUNTER_UNIT_TESTCASE};

	public static boolean isCounterUnitValid(String counterUnit){
		for(String level: VALID_COUTNER_UNITS){
			if(level.equals(counterUnit)) return true;
		}
		return false;
	}

	/** Constants used when writing a SAFS Log. */
	public abstract static class SAFS_LogConstants{
		//Be careful if you want to change " " to other value!
		/** " " */
		private static final String FORMAT_DATE_TIME_SEPARATOR = " ";
		/** "MM-dd-yyyy" */
		public static final String FORMAT_DATE = "MM-dd-yyyy";
		//HH represents the military time, 24 hours (from 0 to 23)
		/** "HH:mm:ss" */
		public static final String FORMAT_MILITARY_TIME = "HH:mm:ss";
		/** "MM-dd-yyyy HH:mm:ss" */
		public static final String FORMAT_DATE_TIME = FORMAT_DATE+FORMAT_DATE_TIME_SEPARATOR+FORMAT_MILITARY_TIME;

		/**
		 * Returns the string representation of the current date or time.
		 * <p>
		 * @param dateOrTime	0 to return date; 1 to return time
		 * @return String, date in format {@link #FORMAT_DATE} or time in format {@link #FORMAT_MILITARY_TIME}.
		 */
		public static String dateTime(int dateOrTime)
		{
			String pattern = (dateOrTime == 0)? FORMAT_DATE : FORMAT_MILITARY_TIME;
			SimpleDateFormat formatter = new SimpleDateFormat(pattern);
			return formatter.format(new Date());
		}

		/**
		 * Convert a String dateTime to a Java Date.
		 * @param dateTime String, in format {@link #FORMAT_DATE_TIME}
		 * @return Date
		 */
		public static Date getDate(String dateTime){
			try {
				SimpleDateFormat format = new SimpleDateFormat(FORMAT_DATE_TIME);
				return format.parse(dateTime);
			} catch (ParseException e) {
				IndependantLog.debug(" can't convert date '" + dateTime+"', due to "+e.getMessage());
				return null;
			}
		}

		/**
		 * @param date String, in format {@link #FORMAT_DATE}
		 * @param time String, in format {@link #FORMAT_MILITARY_TIME}
		 * @return String, Concatenate data string and time string by a separator {@link #FORMAT_DATE_TIME_SEPARATOR}
		 */
		public static String getDateTime(String date, String time){
			return date+FORMAT_DATE_TIME_SEPARATOR+time;
		}
	}

	/** Constants used when writing a SAFS XML Log. */
	public abstract static class SAFS_XML_LogConstants{
		/** 'LOG_OPENED' */
		public static final String TAG_LOG_OPENED 		= "LOG_OPENED";
		/** 'LOG_VERSION' */
		public static final String TAG_LOG_VERSION 		= "LOG_VERSION";
		/** 'LOG_CLOSED' */
		public static final String TAG_LOG_CLOSED 		= "LOG_CLOSED";
		/** 'SAFS_LOG' */
		public static final String TAG_SAFS_LOG 		= "SAFS_LOG";

		/** 'LOG_MESSAGE' */
		public static final String TAG_LOG_MESSAGE 		= "LOG_MESSAGE";
		/** 'MESSAGE_TEXT' */
		public static final String TAG_MESSAGE_TEXT		= "MESSAGE_TEXT";
		/** 'MESSAGE_DETAILS' */
		public static final String TAG_MESSAGE_DETAILS	= "MESSAGE_DETAILS";

		/** 'STATUS_REPORT' */
		public static final String TAG_STATUS_REPORT 		= "STATUS_REPORT";
		/** 'STATUS_ITEM' */
		public static final String TAG_STATUS_ITEM	 		= "STATUS_ITEM";
		/** 'STATUS_ITEM_TEXT' */
		public static final String TAG_STATUS_ITEM_TEXT		= "STATUS_ITEM_TEXT";
		/** 'STATUS_ITEM_DETAILS' */
		public static final String TAG_STATUS_ITEM_DETAILS	= "STATUS_ITEM_DETAILS";
		/** 'STATUS_START_TEXT' */
		public static final String TAG_STATUS_START_TEXT	= "STATUS_START_TEXT";
		/** 'STATUS_START_DETAILS' */
		public static final String TAG_STATUS_START_DETAILS	= "STATUS_START_DETAILS";
		/** 'STATUS_END_TEXT' */
		public static final String TAG_STATUS_END_TEXT		= "STATUS_END_TEXT";
		/** 'STATUS_END_DETAILS' */
		public static final String TAG_STATUS_END_DETAILS	= "STATUS_END_DETAILS";

		/** 'type' */
		public static final String PROPERTY_TYPE = "type";
		/** 'date' */
		public static final String PROPERTY_DATE = "date";
		/** 'time' */
		public static final String PROPERTY_TIME = "time";
		/** 'user' */
		public static final String PROPERTY_USER 	= "user";
		/** 'machine' */
		public static final String PROPERTY_MACHINE = "machine";
		/** 'ip' */
		public static final String PROPERTY_IP 		= "ip";

		/** 'name' */
		public static final String PROPERTY_NAME = "name";
		/** 'classname' */
		public static final String PROPERTY_CLASSNAME = "classname";

		/** 'major' */
		public static final String PROPERTY_MAJOR = "major";
		/** 'minor' */
		public static final String PROPERTY_MINOR = "minor";

		//Following are used to generate JUNIT XML Log
		/** 'error' */
		public static final String TAG_ERROR 		= "error";
		/** 'failure' */
		public static final String TAG_FAILURE 		= "failure";
		/** 'skipped' */
		public static final String TAG_SKIPPED		= "skipped";

		/** 'message' */
		public static final String PROPERTY_MESSAGE		= "message";

		/** 'details' */
		public static final String PROPERTY_DETAILS		= "details";

		/** 'hostname' */
		public static final String PROPERTY_HOSTNAME 	= "hostname";
		/** 'timestamp' */
		public static final String PROPERTY_TIMESTAMP 	= "timestamp";
		/** 'tests' */
		public static final String PROPERTY_TESTS 		= "tests";
		/** 'failures' */
		public static final String PROPERTY_FAILURES 	= "failures";
		/** 'errors' */
		public static final String PROPERTY_ERRORS 		= "errors";
		/** 'skipped' */
		public static final String PROPERTY_SKIPPED 	= "skipped";
		/** 'line' */
		public static final String PROPERTY_LINE 		= "line";

	}

	public abstract static class RestConstants{
		/**
		 * "<b>SAFS_REST</b>" is the section where the REST related information will be defined.
		 */
		public static final String SECTION_SAFS_REST = "SAFS_REST";

		/**
		 * "<b>AUTH</b>" is the "map item" (under section {@link #SECTION_SAFS_REST}) defining a file holding the authorization/authentication information.<br>
		 * The file can be relative to the project root directory; or it can be an absolute path.<br>
		 * Examples:<br>
		 * [SAFS_REST]<br>
		 * #The file &lt;projectRoot&gt;\config\auth2.xml will be used as authorization/authentication information.<br>
		 * AUTH=<b>config\auth2.xml</b>
		 * <br>
		 * [SAFS_REST]<br>
		 * #The file C:\Users\xxx\simpleAuth.xml will be used as authorization/authentication information.<br>
		 * AUTH=<b>C:\Users\xxx\simpleAuth.xml</b>
		 *
		 * */
		public static final String ITEM_AUTH ="AUTH";
		/** "<b>safs.rest.auth</b>" is the JVM property defining a file holding the authorization/authentication information.<br>
		 * The file can be relative to the project root directory; or it can be an absolute path.<br>
		 * Examples:<br>
		 * -Dsafs.rest.auth=<b>config\auth2.xml</b> The file &lt;projectRoot&gt;\config\auth2.xml will be used as authorization/authentication information.<br>
		 * -Dsafs.rest.auth=<b>C:\Users\xxx\simpleAuth.xml</b> The file C:\Users\xxx\simpleAuth.xml will be used as authorization/authentication information.<br>
		 * **/
		public static final String PROPERTY_AUTH ="safs.rest.auth";

		/**
		 * "<b>PROXY</b>" is the "map item" (under section {@link #SECTION_SAFS_REST}) defining a proxy server for Internet connection.<br>
		 * Examples:<br>
		 * [SAFS_REST]<br>
		 * #proxy.host.name:port will be used as proxy server for Internet connection.<br>
		 * PROXY=<b>proxy.host.name:port</b>
		 *
		 * */
		public static final String ITEM_PROXY ="PROXY";
		/** "<b>safs.rest.proxy</b>" is the JVM property defining a proxy server for Internet connection.<br>
		 * Examples:<br>
		 * -Dsafs.rest.proxy=<b>proxy.host.name:port</b> proxy.host.name:port will be used as proxy server for Internet connection.<br>
		 * **/
		public static final String PROPERTY_PROXY ="safs.rest.proxy";
	}

	public abstract static class AutoItConstants{
	    /** "<b>:</b>" */
	    public static final String AUTOIT_RSKEY_VALUE_DELIMITER = StringUtils.COLON;
		/** "<b>=</b>" */
		public static final String AUTOIT_ASSIGN_SEPARATOR 	= GuiObjectRecognition.DEFAULT_ASSIGN_SEPARATOR;
		/** "<b>[</b>" */
		public static final String AUTOIT_ENGINE_RS_START 	= "[";
		/** "<b>]</b>" */
		public static final String AUTOIT_ENGINE_RS_END 	= "]";

		/** <b>":autoit:"</b> */
		public static final String AUTOIT_PREFIX = ":autoit:";

		/** <b>WinTitleMatchMode</b> It is the first parameter of method <a href="https://www.autoitscript.com/autoit3/docs/functions/AutoItSetOption.htm" target="_blank">AutoItSetOption</a>,<br>
		 * It is used to alter the way (matching window titles) of the methods during search operations.<br>
		 * <b>Setting this will only affect the way of matching window's title. NOT the window text.</b>
		 */
		public static final String MODE_WIN_TITLE_MATCH 	= "WinTitleMatchMode";

		/** <b>1</b> Matches partial text from the start. The case is sensitive. */
		public static final int MATCHING_PATIAL 	= 1;
		/** <b>2</b> Matches any substring in the text. The case is sensitive. */
		public static final int MATCHING_SUBSTRING = 2;
		/** <b>3</b> Exact match. The case is sensitive. */
		public static final int MATCHING_EXACT 	= 3;
		/** <b>4</b> Advanced mode. The case is sensitive.
		 * @deprecated  (Kept for backward compatibility) replaced by <a href="https://www.autoitscript.com/autoit3/docs/intro/windowsadvanced.htm" target="_blank">Advanced Window Descriptions</a>
		 */
		@Deprecated
		public static final int MATCHING_ADVANCE 	= 4;
		/** <b>1</b> Matches partial text from the start. The case is sensitive. */
		public static final int MATCHING_DEFAULT 	= MATCHING_PATIAL;

		/** <b>-1</b> Matches partial text from the start. The case is NOT sensitive. */
		public static final int MATCHING_PATIAL_CASE_INSENSITIVE 		= -1;
		/** <b>-2</b> Matches any substring in the text. The case is NOT sensitive. */
		public static final int MATCHING_SUBSTRING_CASE_INSENSITIVE 	= -2;
		/** <b>-3</b> Exact match. The case is NOT sensitive. */
		public static final int MATCHING_EXACT_CASE_INSENSITIVE 		= -3;
		/** <b>-44</b> Advanced mode. The case is NOT sensitive.
		 * @deprecated  (Kept for backward compatibility) replaced by <a href="https://www.autoitscript.com/autoit3/docs/intro/windowsadvanced.htm" target="_blank">Advanced Window Descriptions</a>
		 */
		@Deprecated
		public static final int MATCHING_ADVANCE_CASE_INSENSITIVE 		= -4;

		//Properties used uniquely by Control
		public static final String RS_KEY_ID 			= "id";
		public static final String RS_KEY_CLASSNN 		= "classnn";
		public static final String RS_KEY_NAME 			= "name";

		//Properties used uniquely by Window
		public static final String RS_KEY_TITLE 		= "title";
		/** "<b>caption</b>" */
		public final static String RS_KEY_CAPTION 		= "caption";
		public static final String RS_KEY_REGEXPTITLE 	= "regexptitle";
		public static final String RS_KEY_LAST 			= "last";
		public static final String RS_KEY_ACTIVE 		= "active";

		//Properties used by both Control and Window
		public static final String RS_KEY_CLASS 		= "class";
		public static final String RS_KEY_REGEXPCLASS 	= "regexpclass";
		public static final String RS_KEY_X 			= "x";
		public static final String RS_KEY_Y 			= "y";
		public static final String RS_KEY_W 			= "w";
		public static final String RS_KEY_H 			= "h";
		/** "index" is the same as "instance" */
		public static final String RS_KEY_INDEX 		= "index";
		public static final String RS_KEY_INSTANCE 		= "instance";
		//This property 'TEXT' exists for Control, does NOT exist for window; But it can be provided as the parameter of most AUTOIT method to help finding a Window.
		public static final String RS_KEY_TEXT 			= "text";

		/** '<b>rs_gen_</b>' the prefix of properties used for generating recognition string. */
		public static final String PREFIX_PROPERTY_FOR_RS 		= "rs_gen_";

		public static String getTitleMatchMode(int titleMatchMode){
			switch(titleMatchMode){
			case MATCHING_PATIAL:
				return "beginPatial Mode";
			case MATCHING_SUBSTRING:
				return "substring Mode";
			case MATCHING_EXACT:
				return "exact Mode";
			case MATCHING_ADVANCE:
				return "advance Mode";
			default:
				return "";
			}
		}
	}

	public abstract static class LogConstants{

		/** "-1" invalid log level.*/
		public static final int INVALID_LEVEL   = -1;

		/** "0" DEBUG log level.*/
		public static final int DEBUG   = 0;

		/** "1" INFO log level.*/
		public static final int INFO    = 1;

		/** "2" INDEX log level.*/
		public static final int INDEX   = 2;

		/** "3" GENERIC log level.*/
		public static final int GENERIC = 3;

		/** "4" PASS log level.*/
		public static final int PASS    = 4;

		/** "5" WARN log level.*/
		public static final int WARN    = 5;

		/** "6" ERROR log level.*/
		public static final int ERROR   = 6;

		private LogConstants(){}

		/**
		 * An array holding the log level names as
		 * "DEBUG", "INFO", "INDEX", "GENERIC", "PASS", "WARN", "ERROR"
		 */
		private static final String[] logLevelNames = new String[]{
			"DEBUG",
			"INFO",
			"INDEX",
			"GENERIC",
			"PASS" ,
			"WARN",
			"ERROR"
		};

		/**
		 * @param level int, the log level, it can be
		 * <ul>
		 * <li>{@link #DEBUG}
		 * <li>{@link #INFO}
		 * <li>{@link #INDEX}
		 * <li>{@link #GENERIC}
		 * <li>{@link #PASS}
		 * <li>{@link #WARN}
		 * <li>{@link #ERROR}
		 * </ul>
		 * @return String, the string name of log level; or empty string if the log level is not valid.
		 */
		public static final String getLogLevelName(int level){
			try{
				return logLevelNames[level];
			}catch(IndexOutOfBoundsException e ){
				return "";
			}
		}

		/**
		 *
		 * @return String[], An array holding the log level names as
		 * "DEBUG", "INFO", "INDEX", "GENERIC", "PASS", "WARN", "ERROR"
		 */
		public static final String[] getLogLevelNames(){
			return logLevelNames;
		}

		/**
		 *
		 * @param logLevelName String, the log level name, can be one of {@link #logLevelNames}
		 * @return int, the log level; or {@link #INVALID_LEVEL} if the parameter logLevelName is not valid.
		 */
		public static final int getLogLevel(String logLevelName){
			int level = INVALID_LEVEL;
			if(logLevelName!=null && !logLevelName.isEmpty()){
				for(int i=0;i<logLevelNames.length;i++){
					if(logLevelNames[i].equalsIgnoreCase(logLevelName.trim())){
						level = i;
						break;
					}
				}
			}
			return level;
		}

		public static void main(String[] args){
			String logLevelName = null;
			int level = INVALID_LEVEL;
			for(int i=0;i<logLevelNames.length;i++){
				logLevelName = getLogLevelName(i);
				System.out.println(i+" : "+logLevelName);
				level = getLogLevel(logLevelName);
				assert i==level: "For log level '"+logLevelName+"' original level '"+i+"'!='"+level+"', please check method getLogLevelName() and getLogLevel()!";
			}
		}
	}

	/** <b>100</b> 100 means 100% match when comparing 2 images */
	public static final int IMAGE_BIT_TOLERATION_DEFAULT = 100;

	/** <b>0</b> 0 means no change will be made to an image */
	public static final int IMAGE_ALTER_IMAGE_FACTOR_DEFAULT = 0;

	/** <b>75</b> 75 DPI is used to convert PDF to image */
	public static final int IMAGE_PDF_CONVERSION_RESOLUTION_DEFAULT = 75;

	/** <b>ABCDEFGHIJKLMNOPQRSTUVWXYZ</b> alphabet in upper case */
	public static final String ALPHABET 					= "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	/** <b>abcdefghijklmnopqrstuvwxyz</b> alphabet in lower case */
	public static final String alphabet 					= ALPHABET.toLowerCase();
	/** a TAB indent */
	public static final String INDENT       = "    ";

	public abstract static class HTMLConst{
		/** <b>*</b> represents any tag name */
		public static final String TAG_ANY  					= "*";
		/** <b>DIV</b> tag name */
		public static final String TAG_DIV  					= "DIV";
		/** <b>input</b> tag name */
		public static final String TAG_INPUT  					= "input";
		/** <b>label</b> tag name */
		public static final String TAG_LABEL    				= "label";
		/** <b>iframe</b> tag name */
		public static final String TAG_IFRAME      				= "iframe";
		/** <b>frame</b> tag name */
		public static final String TAG_FRAME       				= "frame";

		/** <b>id</b> attribute name */
		public static final String ATTRIBUTE_ID  				= "id";
		/** <b>class</b> attribute name */
		public static final String ATTRIBUTE_CLASS         		= "class";
		/** <b>for</b> attribute name */
		public static final String ATTRIBUTE_FOR         		= "for";
		/** <b>name</b> attribute name */
		public static final String ATTRIBUTE_NAME        		= "name";
		/** <b>value</b> attribute name */
		public static final String ATTRIBUTE_VALUE       		= "value";
		/** <b>title</b> attribute name */
		public static final String ATTRIBUTE_TITLE       		= "title";
		/** <b>role</b> attribute name */
		public static final String ATTRIBUTE_ROLE        		= "role";
		/** <b>type</b> attribute name */
		public static final String ATTRIBUTE_TYPE       		= "type";

		//https://github.com/w3c/html-aam/issues/69 the conflict between "placeholder" and "aria-placeholder"
		/** <b>placeholder</b> attribute name */
		public static final String ATTRIBUTE_PLACEHOLDER 		= "placeholder";
		/** <b>aria-placeholder</b> attribute name */
		public static final String ATTRIBUTE_AIRA_PLACEHOLDER 	= "aria-placeholder";
		/** <b>target</b> attribute name */
		public static final String ATTRIBUTE_TARGET      		= "target";
		/** <b>alt</b> attribute name */
		public static final String ATTRIBUTE_ALT         		= "alt";
	}
}
