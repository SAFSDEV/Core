/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 */

/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * NOV 09, 2016    (SBJLWA) Initial release: Created BrowserConstants and SeleniumConstants: copied constants from org.safs.selenium.webdriver.lib.SelectBrowser.
 * MAR 07, 2017    (SBJLWA) Added constants for setting of the browser-driver in Selenium.
 * MAR 10, 2017    (SBJLWA) Added RestConstants.
 * APR 05, 2017    (SBJLWA) Added AutoItConstants.
 * APR 11, 2017    (SBJLWA) Added EclipseConstants and some other constant fields.
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

	/** '<b>2000</b>' in milliseconds, the default time for the mouse to hover.
	 * The mouse may move out of screen if this time expires. */
	public static final int TIMEOUT_HOVERMOUSE_DEFAULT 					= 2000;
	/** '<b>-1</b>' the timeout for the mouse to hover for ever */
	public static final int TIMEOUT_HOVERMOUSE_STAY_FOREVER				= -1;

	public static abstract class BrowserConstants{
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
		/** '<b>KEY_PROXY_SETTING</b>' the key for proxy string;
		 * The value is colon separated string as "proxyserver:port" */
		public static final String KEY_PROXY_SETTING = "KEY_PROXY_SETTING";
		/** '<b>KEY_PROXY_BYPASS_ADDRESS</b>' the key for proxy bypass address string;
		 * The value is comma separated string as "localhost,tadsrv,rnd.sas.com" */
		public static final String KEY_PROXY_BYPASS_ADDRESS = "KEY_PROXY_BYPASS_ADDRESS";
		/** '<b>FirefoxProfile</b>' the key for firefox profile name/filename string;
		 * The value is something like "myprofile" or "&lt;AbsolutePath>/ppc2784x.default" */
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
		 *     such as { "<b>seplus.chrome.preference.json.key</b>": { "intl.accept_languages":"zh-cn", intl.charset_default:"utf-8" } },
		 *     refer to <a href="https://src.chromium.org/viewvc/chrome/trunk/src/chrome/common/pref_names.cc">detail preferences</a>
		 * <ol>
		 */
		public static final String KEY_CHROME_PREFERENCE = "chrome.perference";//"Chrome Command Line Options" and "Chrome Preferences" file

		/** '<b>seplus.chrome.preference.json.key</b>' the key for chrome preferences, which points to json data,
		 * such as { "intl.accept_languages":"zh-cn", intl.charset_default:"utf-8" },
		 * refer to <a href="https://src.chromium.org/viewvc/chrome/trunk/src/chrome/common/pref_names.cc">detail preferences</a>*/
		public static final String KEY_CHROME_PREFERENCE_JSON_KEY = "seplus.chrome.preference.json.key";//Chrome Preferences

		/** '<b>prefs</b>' the key used to set chrome Experimental Option. Used internally. */
		public static final String KEY_CHROME_PREFS = "prefs";//setExperimentalOption

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
		 *             Use SeleniumPlus.quote("disable-component-update") to keep its value.
		 */
		public static final String KEY_CHROME_EXCLUDE_OPTIONS = "excludeSwitches";

		/**
		 * '<b>--disable-extensions</b>' is used to disable the use of Chrome extensions. Usually, we use it
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

	public static abstract class EclipseConstants{

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

	public static abstract class SeleniumConstants{
		/**'<b>selenium.host</b>'*/
		public static final String SYSTEM_PROPERTY_SELENIUM_HOST = "selenium.host";
		/**'<b>selenium.port</b>'*/
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
		 * "<b>WEB_DRIVERS</b>" defines a set of drivers to start with the selenium-server.<br>
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
		/** "<b>safs.selenium.web.drivers</b>" defines a set of drivers to start with the selenium-server.<br>
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

	public static abstract class JSONConstants{
		/** "<b>$classname</b>" special reserved key to track the Object class name.*/
		public static final String PROPERTY_CLASSNAME = "$classname";
	}

	public static abstract class XMLConstants{
		/**  "<b>&lt;![CDATA[</b>" */
		public static final String CDATA_START = "<![CDATA[";
		/** "<b>]]></b>" */
		public static final String CDATA_END = "]]>";
		/** "<b>&lt;?XML</b>" */
		public static final String XML_START = "<?XML";
		/** "<b>classname</b>" */
		public static final String PROPERTY_CLASSNAME = "classname";
		/** "<b>package</b>" */
		public static final String PROPERTY_PACKAGE = "package";
		/** "<b>&</b>" */
		public static final String SYMBOL_AND = "&";
		/** "<b>'</b>" */
		public static final String SYMBOL_APOS = "'";
		/** "<b>\"</b>" */
		public static final String SYMBOL_QUOTE = "\"";
		/** "<b>&lt;</b>" */
		public static final String SYMBOL_LESS = "<";
		/** "<b>&gt;</b>" */
		public static final String SYMBOL_BIGGER = ">";
		/** An array of symbols needing escape in XML document, such as <b>& ' " < ></b> */
		public static final String[] SYMBOL_TO_ESCAPE = {SYMBOL_AND, SYMBOL_APOS, SYMBOL_QUOTE, SYMBOL_LESS, SYMBOL_BIGGER};
	}

	public static abstract class RestConstants{
		/**
		 * "<b>SAFS_REST</b>" is the section where the REST related information will be defined.
		 */
		public static final String SECTION_SAFS_REST = "SAFS_REST";

		/**
		 * "<b>AUTH</b>" is the "map item" (under section {@link #SECTION_SAFS_REST}) defining a file holding the authorization/authentication information.<br>
		 * The file can be relative to the project root directory; or it can be an absolute path.<br>
		 * Examples:<br>
		 * [SAFS_REST]<br>
		 * AUTH=<b>config\auth2.xml</b> The file &lt;projectRoot>\config\auth2.xml will be used as authorization/authentication information.<br>
		 * <br/>
		 * [SAFS_REST]<br>
		 * AUTH=<b>C:\Users\xxx\simpleAuth.xml</b> The file C:\Users\xxx\simpleAuth.xml will be used as authorization/authentication information.<br>
		 *
		 * */
		public static final String ITEM_AUTH ="AUTH";
		/** "<b>safs.rest.auth</b>" is the JVM property defining a file holding the authorization/authentication information.<br>
		 * The file can be relative to the project root directory; or it can be an absolute path.<br>
		 * Examples:<br>
		 * -Dsafs.rest.auth=<b>config\auth2.xml</b> The file &lt;projectRoot>\config\auth2.xml will be used as authorization/authentication information.<br>
		 * -Dsafs.rest.auth=<b>C:\Users\xxx\simpleAuth.xml</b> The file C:\Users\xxx\simpleAuth.xml will be used as authorization/authentication information.<br>
		 * **/
		public static final String PROPERTY_AUTH ="safs.rest.auth";
	}

	public static abstract class AutoItConstants{
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

		/** <b>WinTitleMatchMode</b> It is the first parameter of method <a href="https://www.autoitscript.com/autoit3/docs/functions/AutoItSetOption.htm" target="_blank">AutoItSetOption</a>,<br/>
		 * It is used to alter the way (matching window titles) of the methods during search operations.<br/>
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

	public static abstract class LogConstants{

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

}
