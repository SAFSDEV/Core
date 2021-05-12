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
 * History:<br>
 *
 * NOV 19, 2013    (Carl Nagle) Initial release.
 * DEC 18, 2013    (Lei Wang) Update to support ComboBox.
 * JAN 08, 2014    (DHARMESH) Update to support EditBox.
 * JAN 16, 2014    (DHARMESH) Update start/stop browser call.
 * JAN 26, 2014    (Lei Wang) Add method combineParams().
 *                                  Modify API ComboBox.CaptureItemsToFile(): show the required parameter filename.
 *                                  Add keyword Misc.Expressions().
 * FEB 02, 2014	   (DHARMESH) Add Resize and Maximize WebBrowser window KW.
 * MAR 05, 2014    (Lei Wang) Implement click-related keywords.
 * APR 15, 2014    (DHARMESH) Added HighLight keyword
 * APR 22, 2014    (Lei Wang) Implement keywords of TabControl.
 * APR 29, 2014    (Lei Wang) Add the ability of execution of javascript;
 * MAY 13, 2014    (DHARMESH) Add Component class and update other component references;
 * JUL 08, 2014    (Lei Wang) Add inner class WDTimeOut to provide methods setting WebDriver's timeout thread-safely.
 * NOV 10, 2014    (Lei Wang) Add support for Strings.
 * NOV 19, 2014    (Lei Wang) Add support for Files.
 * NOV 21, 2014    (Dharmesh/Carl Nagle) Update ExecuteScript Javadoc.
 * NOV 25, 2014    (Lei Wang) Add support for Misc.
 * DEC 19, 2014    (Lei Wang) Adjust some method qualifiers:
 *                                    make some "public" so that user can use.
 *                                    turn some "package private" to "private" so that generated java doc will not contain them.
 *                                  Remove generateParams(): use combineParams instead.
 *                                  Move detail-doc-reference in front of parameter specification, it is reference for whole keyword.
 *                                  Modify detail-doc-reference'link from xxxKeyword.html to xxx.htm#detail_xxxKeyword, this format
 *                                    link will load the whole html page, it contains css definition and make page colorful and net.
 *                                    As it will load the whole html page, so it is slow for the first view. The following visit will
 *                                    be very quick (got from the cache).
 *                                  Change the font of optional parameters to bold, so it looks clear for user.
 *                                  Modify method ExecuteScript(): force parameter 'script' to be required.
 * JAN 04, 2015    (Lei Wang) Add LeftDrag and ScrollWheel.
 * JAN 15, 2015    (Lei Wang) Add mouse-drag-related keywords.
 * FEB 26, 2015    (Lei Wang) Add method GetGUIImage to filter children of type org.safs.model.Component.
 *                                  Modify Misc.GetAppMapValue(), SeleniumPlus.getObject(): don't log message.
 * JUN 11, 2015    (Lei Wang) Modify Misc.SetVariableValueEx(), SetVariableValues(): resolve DDVariable if EXPRESSIONS is off.
 *                                  Add Misc.ResolveExpression().
 * JUN 15, 2015    (DHARMESH4) Added SendMail support.
 * JUN 23, 2015    (Lei Wang) Modify for test auto run.
 * JUN 24, 2015	   (Tao Xie) Add Misc.WaitForPropertyValue() and Misc.WaitForPropertyValueGone() keywords.
 * JUL 07, 2015    (Tao Xie) Add EditBox.SetTextCharacters(), EditBox.SetUnverifiedTextCharacters() and EditBox.SetUnverifiedTextValue() keywords.
 *                          Change comments of EditBox.SetTextValue().
 * JUL 24, 2015    (Lei Wang) Add GetURL, SaveURLToFile, VerifyURLContent, VerifyURLToFile in Misc.
 * AUG 17, 2015    (DHARMESH4) Add SetFocus call in Window.
 * AUG 20, 2015    (Carl Nagle) Document -Dtestdesigner.debuglogname support in main().
 * SEP 07, 2015    (Lei Wang) Add method DragTo(): parameter 'offset' will also support pixel format;
 *                                                       optional parameter 'FromSubItem' and 'ToSubItem' are not supported yet.
 * JAN 07, 2016    (Carl Nagle) Make System.exit() optional and allowExit=false, by default.
 * MAR 02, 2016    (Lei Wang) Add Misc.AlertAccept(), Misc.AlertDismiss() and ClickUnverified().
 * MAR 07, 2016    (Lei Wang) Add example for StartWebBrowser() with preference settings for "chrome" and "firefox".
 * MAR 14, 2016    (Lei Wang) Add IsAlertPresent().
 * MAR 24, 2016    (Lei Wang) Modify comments for StartWebBrowser(): adjust examples and add links to specify "custom profile" and "preferences".
 * MAR 31, 2016    (Lei Wang) Add IsComponentExists(), OnGUIXXXBlockID().
 *                                  Modify testStatusCode(): the status code BRANCH_TO_BLOCKID will be considered successful execution.
 * APR 19, 2016    (Lei Wang) Modify comments/examples for Click() CtrlClick() ShiftClick() etc.: Handle the optional parameter 'autoscroll'.
 * MAY 17, 2016    (Carl Nagle) Add support for -junit:classname command-line parameter.
 * DEC 12, 2016    (Lei Wang) Modified TypeKeys() and TypeChars(): call actionGUILess() instead of Component.TypeXXX() to keep the log message consistent.
 * JUL 25, 2017    (Lei Wang) Modified ThreadSafeTimeOut.setTimeout()/resetTimeout(): catch WebDriverException instead of SessionNotFoundException (it disappears in Selenium3.X)
 * APR 18, 2018    (Lei Wang) Modified constructor SeleniumPlus() to set product's name, version and description.
 * APR 19, 2018    (Lei Wang) Modified constructor SeleniumPlus() to add embedded SELENIUMENGINE to driver.
 * APR 19, 2018    (Lei Wang) Moved the code of setting Runner's diver out of constructor and put them into static block:
 *                                  Runner is static and is shared by all SeleniumPlus instances, we don't need to set the driver's data every time we instantiate a new instance.
 * MAY 15, 2018    (Lei Wang) Add an example for method StartWebBrowser(): to run chrome with option "--no-sandbox".
 * SEP 25, 2018    (Lei Wang) Loaded the 'Runner' from spring context.
 * MAR 21, 2019    (Lei Wang) Modified getObject(): Check window's recognition string to see if we need to reset the WDLibrary's lastFrame to null.
 * APR 18, 2019    (Lei Wang) Instantiate the 'Runner' in normal way if it is not loaded from spring context.
 * JUN 04, 2019    (Lei Wang) Modified StartWebBrowser(): add example for 'setNetworkConditions'.
 */
package org.safs.selenium.webdriver;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.concurrent.TimeUnit;

//SessionNotFoundException has disappeared in Selenium3.X, NoSuchSessionException occurs instead
//But I am not sure if NoSuchSessionException is equivalent as SessionNotFoundException, so use their super-class WebDriverException
//import org.openqa.selenium.remote.SessionNotFoundException;
//import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.safs.Constants;
import org.safs.Constants.BrowserConstants;
import org.safs.IndependantLog;
import org.safs.Processor;
import org.safs.SAFSPlus;
import org.safs.StringUtils;
import org.safs.image.ImageUtils.AlterImageStyle;
import org.safs.model.commands.GenericMasterFunctions;
import org.safs.model.tools.EmbeddedHookDriverRunner;
import org.safs.selenium.util.DocumentClickCapture;
import org.safs.selenium.webdriver.lib.SelectBrowser;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.text.FileUtilities.FilterMode;
import org.safs.tools.engines.SAFSSELENIUM;

/**
 * <pre>
 * <Font color="red">NOTE 1: Auto-evaluated expression.</Font>
 * As SeleniumPlus inherits from SAFS, so it has the ability to process string as an expression.
 * For example, "36+9", "25*6" will be calculated as "45", "150" automatically; "^" is considered
 * as the leading char of a variable, hence "^var" will be considered as a variable "var",
 * if variable "var" exists, then "^var" will be replaced by its value, otherwise replaced by empty string "".
 *
 * This ability is very useful for user, but sometimes it will cause UN-EXPECTED result during calling
 * SeleniumPlus's API. For example, user wants to input a string "this is a combined-word" to an EditBox,
 * the EditBox will receive "this is a 0", which is not an expected result; user wants to select all
 * text of an EditBox, he uses "Ctrl+a" by calling SeleniumPlus.TypeKeys("^a") and he finds that doesn't
 * work (the reason is that "^a" is parsed to "", because "^a" is considered as variable).
 *
 * To avoid the problem caused by arithmetic char "+ - * /", we can call API Misc.Expressions(false) to
 * turn off the parse of an expression.
 * {@code
 * Misc.Expressions(false);
 * SeleniumPlus.TypeChars("this is a combined-word");
 * ComboBox.CaptureItemsToFile(combobox, "ComboBoxData.txt", "UTF-8");
 * }
 * To avoid the problem caused by caret ^ or by arithmetic char "+ - * /", we can double-quote the parameter
 * {@code
 * SeleniumPlus.TypeKeys("\"^p\""));
 * SeleniumPlus.TypeKeys(quote("^p")));//quote is a static method provided by SeleniumPlus
 * SeleniumPlus.TypeChars(quote("this is a combined-word"));
 * }
 *
 * <Font color="red">NOTE 2: File path deducing.</Font>
 * In SeleniumPlus, there are some APIs like CaptureXXXToFile, VerifyXXXToFile, they require file-path as parameter.
 * As our doc is not very clear, user may confuse with the file-path parameter. Let's make it clear:
 *   There are 2 types of file, the test-file and bench-file. User can provide absolute or relative file-path for them.
 *   If it is absolute, there is not confusion.
 *   If it is relative, we will combine it with a base-directory to form an absolute file. The base-directory depends
 *   on the type of file (test or bench):
 *     if it is test-file, the base-directory will be the test-directory, <ProjectDir>/Actuals/
 *     if it is bench-file, the base-directory will be the bench-directory, <ProjectDir>/Benchmarks/
 *     After the combination, the combined-file-name will be tested, if it is not valid, the project-directory will
 *     be used as base-directory.
 *
 *
 * <Font color="red">NOTE 3: <a href="/sqabasic2000/UsingDDVariables.htm">DDVariable</a></Font>
 * To use DDVariable ability, PLEASE remember to turn on the Expression by Misc.Expressions(true);
 * The DDVariable is a variable reference, it can be expressed by a leading symbol ^ and the "variable name".
 * For example:
 * ^user.name
 * ^user.password
 *
 * DDVariable can be used along with an assignment or by itself, example as following:
 * {@code
 * Misc.Expressions(true);
 * //set value "UserA" to variable "user.name", set "Password1" to variable "user.password"
 * Misc.SetVariableValues("^user.name=UserA","^user.password=Password1");
 * //input the value of variable "user.name"
 * Component.InputCharacters(Map.AUT.UserInput, "^user.name");
 * //input the value of variable "user.password"
 * Component.InputCharacters(Map.AUT.PassWord, "^user.password");
 * }
 *
 * <Font color="red">NOTE 4: a known issue about clicking on wrong item</Font>
 * Please to TURN OFF the browser's status bar.
 *
 * </pre>
 * For more info on command-line options, see {@link #main(String[])}.
 *
 * @author Carl Nagle
 */
public abstract class SeleniumPlus extends SAFSPlus{
	/**
	 * The Runner object providing access to the underlying Selenium Engine.
	 * This is the main object subclasses would use to execute SeleniumPlus actions and commands
	 * and to gain references to more complex services like the running JSAFSDriver or the
	 * Selenium WebDriver object(s).
	 */
//	public static EmbeddedHookDriverRunner Runner = new EmbeddedHookDriverRunner(EmbeddedSeleniumHookDriver.class);
	public static EmbeddedHookDriverRunner Runner = null;
	/** 'SeleniumPlus'  */
	public static final String PRODUCT_NAME = "SeleniumPlus";
	/** '1.0' */
	public static final String PRODUCT_VERSION = "1.0";
	/** 'The driver with embedded selenium engine to run test script in Java.' */
	public static final String PRODUCT_DESCRIPTION = "The driver with embedded selenium engine to run test script in Java.";

	static{
		try{
			//Runner = new EmbeddedHookDriverRunner(EmbeddedSeleniumHookDriver.class);
			Object[] arguments = {EmbeddedSeleniumHookDriver.class};
			Runner = (EmbeddedHookDriverRunner) springApplicationContext.getBean(org.safs.model.tools.EmbeddedHookDriverRunner.class.getName(), arguments);
			System.out.println("SeleniumPlus: Spring got Runner"+Runner);

		}catch(Exception e){
			debug("SeleniumPlus: Failed to initialize the SeleniumPlus.Runner by spring in the static clause! Met "+e.getClass().getSimpleName()+":"+e.getMessage());
			Runner = new EmbeddedHookDriverRunner(EmbeddedSeleniumHookDriver.class);
			System.out.println("SeleniumPlus: got Runner"+Runner+" by normal instantiation.");
		}

		try{
			//Set SeleniumPlus product's name, version and description
			Runner.iDriver().setProductName(PRODUCT_NAME);
			Runner.iDriver().setVersion(PRODUCT_VERSION);
			Runner.iDriver().setDescription(PRODUCT_DESCRIPTION);
			//Add the embedded engine 'SAFSSELENIUM' to the driver. This engine will not handle request, it
			//is only used to track user's history into our repository.
			Runner.iDriver().addEmbeddedEngine(new SAFSSELENIUM());
		}catch(Exception e){
			System.err.println("SeleniumPlus: Failed to initialize the SeleniumPlus.Runner's driver in the static clause! Met "+e.getClass().getSimpleName()+":"+e.getMessage());
		}
	}
	/**
	 * Internal framework use only.
	 * Required Default no-arg constructor.
	 * Any subclass instantiation should also invoke this super(); */
	public SeleniumPlus() {
		super();
		//To get the SAFSPlus work for SeleniumPlus, we MUST set its Runner to EmbeddedHookDriverRunner.
		setRunner(Runner);
	}

	/**
	 * Start WebBrowser
	 * See <a href="/sqabasic2000/DDDriverCommandsReference.htm#detail_StartWebBrowser">Detailed Reference</a>
	 * @param URL String,
	 * @param BrowserID String, Unique application/browser ID.
	 * @param params optional
	 * <p><ul>
	 * <b>params[0] browser name</b> String, (default is {@link DCDriverCommand#DEFAULT_BROWSER}), it can be one of:
	 * <p>
	 * 		<ul>
	 *          <li>{@link SelectBrowser#BROWSER_NAME_CHROME}
	 *          <li>{@link SelectBrowser#BROWSER_NAME_FIREFOX}
	 *          <li>{@link SelectBrowser#BROWSER_NAME_IE}
	 * 		</ul>
	 * <p>
	 * <b>params[1] timeout</b> int, in seconds. Implicit timeout for search elements<br>
	 * <b>params[2] isRemote</b> boolean, (no longer used -- everything is now "remote")<br>
	 * <br>
	 * Following parameters indicate the <b>extra parameters</b>, they <b>MUST</b> be given by <b>PAIR(key, value)</b>
	 * <p>
	 * The key can be one of:
	 * <p><ul>
	 * {@link BrowserConstants#getExtraParameterKeys()}<br>
	 * </ul><p>
	 * params[3] extra parameter key1<br>
	 * params[4] extra parameter value for key1<br>
	 * <br>
	 * params[5] extra parameter key2<br>
	 * params[6] extra parameter value for key2<br>
	 * <br>
	 * params[7] extra parameter key3<br>
	 * params[8] extra parameter value for key3<br>
	 * ...
	 * </ul>
	 * @return true on success
	 * @example
	 * <pre>
	 * StartWebBrowser("http://www.google.com", "GoogleMain");
	 * StartWebBrowser("http://www.google.com", "GoogleMain", SelectBrowser.BROWSER_NAME_CHROME);
	 * StartWebBrowser("http://www.google.com", "GoogleMain", SelectBrowser.BROWSER_NAME_IE, "10");
	 *
	 * <b>
	 * The following gives some examples to start web browser with "custom profile" and "preferences".
	 * For the detail explanation of starting browser with "custom profile" and/or "preferences", please visit the section "<font color="red">Start Browser</font>" at <a href="/selenium/doc/SeleniumPlus-Welcome.html">Selenium Welcome Document</a>.
	 * </b>
	 *
	 * //Start firefox browser with custom profile "myprofile" ( <a href="https://support.mozilla.org/en-US/kb/profile-manager-create-and-remove-firefox-profiles">Create custom profile</a>)
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_FIREFOX,
	 *                                                        "10",
	 *                                                        "true",
	 *                                                        SelectBrowser.KEY_FIREFOX_PROFILE,
	 *                                                        "myprofile"
	 *                                                        });
	 *
	 * //Start firefox browser with some preference to set.
	 * String absolutePreferenceFile = "c:\\firefoxPref.json.dat";//A json file containing chrome preferences, like { "intl.accept_languages":"zh-cn", "accessibility.accesskeycausesactivation":false, "browser.download.folderList":2 }
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_FIREFOX,
	 *                                                        "10",
	 *                                                        "true",
	 *                                                        quote(SelectBrowser.KEY_FIREFOX_PROFILE_PREFERENCE),
	 *                                                        quote(absolutePreferenceFile)
	 *                                                        });
	 * //Start firefox browser with preference by json string (not a file).
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_FIREFOX,
	 *                                                        "10",
	 *                                                        "true",
	 *                                                        quote(SelectBrowser.KEY_FIREFOX_PROFILE_PREFERENCE),
	 *                                                        "{ \"intl.accept_languages\":\"zh-cn\", \"accessibility.accesskeycausesactivation\":false, \"browser.download.folderList\":2 }"
	 *                                                        });
	 * //Start firefox browser with preference by CSV array string, such as "key:value, key:value, key:value".
	 * <b>Misc.Expressions(false);//This must be called to avoid expression-evaluation.</b>
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_FIREFOX,
	 *                                                        "10",
	 *                                                        "true",
	 *                                                        quote(SelectBrowser.KEY_FIREFOX_PROFILE_PREFERENCE),
	 *                                                        "intl.accept_languages:zh-cn, accessibility.accesskeycausesactivation:false, browser.download.folderList:2 "
	 *                                                        });
	 *
	 * //Start chrome browser with default data pool (chrome://version/, see "Profile Path") , and using the last-used user.
	 * String datapool = "C:\\Users\\some-user\\AppData\\Local\\Google\\Chrome\\User Data";
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_CHROME,
	 *                                                        "10",
	 *                                                        "true",
	 *                                                        quote(SelectBrowser.KEY_CHROME_USER_DATA_DIR),
	 *                                                        datapool
	 *                                                        });
	 * //Start chrome browser with default data pool (chrome://version/, see "Profile Path") , and using the default user.
	 * String datapool = "C:\\Users\\some-user\\AppData\\Local\\Google\\Chrome\\User Data";
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_CHROME,
	 *                                                        "10",
	 *                                                        "true",
	 *                                                        quote(SelectBrowser.KEY_CHROME_USER_DATA_DIR),
	 *                                                        datapool,
	 *                                                        quote(SelectBrowser.KEY_CHROME_PROFILE_DIR),
	 *                                                        "Default"
	 *                                                        });
	 *
	 * //Start chrome browser with custom data, and using the last-used user.
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_CHROME,
	 *                                                        "10",
	 *                                                        "true",
	 *                                                        quote(SelectBrowser.KEY_CHROME_USER_DATA_DIR),
	 *                                                        "c:\\chrome_custom_data"//<a href="http://www.chromium.org/developers/creating-and-using-profiles">Create custom data pool</a>
	 *                                                        });
	 * //Start chrome browser with custom data, and using the 1th user.
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_CHROME,
	 *                                                        "10",
	 *                                                        "true",
	 *                                                        quote(SelectBrowser.KEY_CHROME_USER_DATA_DIR),
	 *                                                        "c:\\chrome_custom_data",//<a href="http://www.chromium.org/developers/creating-and-using-profiles">Create custom data pool</a>
	 *                                                        quote(SelectBrowser.KEY_CHROME_PROFILE_DIR),
	 *                                                        "Profile 1"
	 *                                                        });
	 *
	 * //Start chrome browser with some options to be turned off, these options will be set to key {@link SelectBrowser#KEY_CHROME_EXCLUDE_OPTIONS} (<b>excludeSwitches</b>).
	 * String optionsToExclude = "disable-component-update";//comma separated options to exclude, like "disable-component-update, ignore-certificate-errors", be careful, there are NO 2 hyphens before options.
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_CHROME,
	 *                                                        "10",
	 *                                                        "true",
	 *                                                        quote(SelectBrowser.KEY_CHROME_EXCLUDE_OPTIONS),
	 *                                                        quote(optionsToExclude)
	 *                                                        });
	 *
	 * //Start chrome browser with some chrome-command-line-options/preferences to set.
	 * String absolutePreferenceFile = "c:\\chromePref.json.dat";//A json file containing chrome command-line-options/preferences, like { "lang":"zh-cn", "start-maximized":"",  "<b>seplus.chrome.preference.json.key</b>":{ "intl.accept_languages":"zh-CN-pseudo", "intl.charset_default"  :"utf-8"} }
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_CHROME,
	 *                                                        "10",
	 *                                                        "true",
	 *                                                        quote(SelectBrowser.KEY_CHROME_PREFERENCE),
	 *                                                        quote(absolutePreferenceFile)
	 *                                                        });
	 * //Start chrome browser with command-line-options/preferences by json string (not a file).
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_CHROME,
	 *                                                        "10",
	 *                                                        "true",
	 *                                                        quote(SelectBrowser.KEY_CHROME_PREFERENCE),
	 *                                                        "{ \"lang\":\"zh-cn\", \"start-maximized\":\"\",  \"seplus.chrome.preference.json.key\":{ \"intl.accept_languages\":\"zh-CN-pseudo\", \"intl.charset_default\" : \"utf-8\"} }"
	 *                                                        });
	 * //Start chrome browser with command-line-options/preferences by CSV array string, such as "key:value, key:value, key:value".
	 * <b>Misc.Expressions(false);//This must be called to avoid expression-evaluation.</b>
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_CHROME,
	 *                                                        "10",
	 *                                                        "true",
	 *                                                        quote(SelectBrowser.KEY_CHROME_PREFERENCE),
	 *                                                        "lang:zh-cn, start-maximized: "
	 *                                                        });
	 *
	 * //Start chrome browser with some chrome experimental options to set.
	 * String experimentalOptionsFile = "c:\\chromeExperimentalOptions.json.dat";//A json file containing chrome experimental options, like {"useAutomationExtension" : false, "excludeSwitches" : ["enable-automation"], "prefs" : { "credentials_enable_service" : false, "profile" : { "password_manager_enabled" : false } } }
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_CHROME,
	 *                                                        "10",
	 *                                                        "true",
	 *                                                        quote(SelectBrowser.KEY_CHROME_EXPERIMENTAL_OPTIONS),
	 *                                                        quote(experimentalOptionsFile)
	 *                                                        });
	 * //Start chrome browser with experimental options by json string (not a file).
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_CHROME,
	 *                                                        "10",
	 *                                                        "true",
	 *                                                        quote(SelectBrowser.KEY_CHROME_EXPERIMENTAL_OPTIONS),
	 *                                                        "{\"useAutomationExtension\" : false, \"excludeSwitches\" : [\"enable-automation\"], \"prefs\" : { \"credentials_enable_service\" : false, \"profile\" : { \"password_manager_enabled\" : false } } }"
	 *                                                        });
	 * //Start chrome browser with experimental options by CSV array string, such as "key:value, key:value, key:value".
	 * <b>Misc.Expressions(false);//This must be called to avoid expression-evaluation.</b>
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_CHROME,
	 *                                                        "10",
	 *                                                        "true",
	 *                                                        quote(SelectBrowser.KEY_CHROME_EXPERIMENTAL_OPTIONS),
	 *                                                        " useAutomationExtension:false, excludeSwitches:[\"enable-automation\"] "
	 *                                                        });
	 *
	 * //Start chrome browser out of sandbox (It is NOT suggested to use that way). But it can be used to avoid some chrome crash problem.
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_CHROME,
	 *                                                        "10",
	 *                                                        "true",
	 *                                                        quote(BrowserConstants.KEY_CHROME_NO_SANDBOX),
	 *                                                        "true"
	 *                                                        });
	 * //Start chrome browser with the 'network-conditions' of <b>{"offline":false, "latency":5, "download_throughput":500000, "upload_throughput":500000}</b>
	 * "latency" is in milliseconds, "download_throughput" is in bps, "upload_throughput" is in bps.<br>
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_CHROME,
	 *                                                        "10",
	 *                                                        "true",
	 *                                                        quote(BrowserConstants.KEY_SET_NETWORK_CONDITIONS),
	 *                                                        "{ \"offline\":false, \"latency\":5, \"download_throughput\":500000 , \"upload_throughput\":500000}"
	 *                                                        });
	 *
	 * //Start chrome browser with an empty 'network-conditions'. <b>This call is needed if you want to adjust (set, get, delete) the 'network-conditions'.</b>
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_CHROME,
	 *                                                        "10",
	 *                                                        "true",
	 *                                                        quote(BrowserConstants.KEY_SET_NETWORK_CONDITIONS),
	 *                                                        ""
	 *                                                        });
	 *
	 * //Start chrome browser with custom capabilities. <b>The 'custom capabilities' will be a set of key:value pairs json data, each key:value will be stored in the Capabilities.</b>
	 * String customCapsDat = "c:\\custom.caps.json.dat";//A json file containing 'custom capabilities', like {"goog:loggingPrefs": {"browser": "ALL", "client": "ALL", "driver": "ALL", "performance": "ALL", "server": "ALL"}}
	 * //This will set 'goog:loggingPrefs' as value {"browser": "ALL", "client": "ALL", "driver": "ALL", "performance": "ALL", "server": "ALL"} in the selenium capabilities to turn on all levels of logs.
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_CHROME,
	 *                                                        "10",
	 *                                                        "true",
	 *                                                        quote(BrowserConstants.KEY_CUSTOM_CAPABILITIES),
	 *                                                        quote(customCapsDat)
	 *                                                        });
	 *
	 * //Start chrome browser with a chrome ModHeader extension to load, and load the ModHeader's profile if it is provided.
	 * The file "<b>loadExtensions.json</b>" contains "extensions" (required) and "extension-modheader-profile" (optional), such as:
	 *
	 * //only load the ModHeader extension
	 * {
	 * "extensions": ["C:\\SeleniumPlus\\extra\\ModHeader.crx"],
	 * }
	 *
	 * //or load ModHeader extension and load the ModHeader's profile by a json file
	 * {
	 * "extensions": ["C:\\SeleniumPlus\\extra\\ModHeader.crx"],
	 * "extension-modheader-profile": "C:\\SeleniumPlus\\extra\\ModHeader.json"
	 * }
	 *
	 * //or load ModHeader extension and load the ModHeader's profile by an URL
	 * {
	 * "extensions": ["C:\\SeleniumPlus\\extra\\ModHeader.crx"],
	 * "extension-modheader-profile": "https://bewisse.com/modheader/p/#NobwRAhgDlCmB2ATAsge0bMAuAZhANgM6wA0YARhAMYDWA5gE6oCuSAwqvqg9mAMQA2cgFYAzMIBMYMjgCW+AC6wGhbMAC6ZABawIGFWvBVUAWxMIFvaWAQRy+WImwKGzUmHgRzvAB4BaQghCPygmKAg6CCU-WScyADcCN15EUwhZeGZiHgBfTTAtWNgOMwtnV3cGWEIoAAldfVUsDTJCLW4FABVZBQdeAEZrJR8FDi4eLH4caZmhnr7JgAUmOQcAAkGyZgZ8ACVYKHxqWHN4BSaNPKA"
	 * }
	 *
	 * //or load ModHeader extension and load the ModHeader's profile by a json object
	 * {
	 * "extensions": ["C:\\SeleniumPlus\\extra\\ModHeader.crx"],
	 * "extension-modheader-profile": [{"appendMode":false,"backgroundColor":"#6b5352","filters":[],"headers":[{"comment":"","enabled":true,"name":"x-sas-propagate-id","value":"domainuser"}],"hideComment":true,"respHeaders":[],"shortTitle":"1","textColor":"#ffffff","title":"Profile 1","urlReplacements":[]}]
	 * }
	 *
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_CHROME,
	 *                                                        "10",
	 *                                                        "true",
	 *                                                        quote(BrowserConstants.KEY_CHROME_LOAD_EXTENSIONS),
	 *                                                        quote("loadExtensions.json")
	 *                                                        });
	 *
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 * @see #StopWebBrowser(String)
	 * @see #SwitchWebBrowser(String)
	 */
	public static boolean StartWebBrowser(String URL,String BrowserID, String... params){
		return DriverCommand.StartWebBrowser(URL, BrowserID, params);
	}

	/**
	 * Stop WebBrowser by ID.
	 * During test, multiple browsers can be opened by {@link #StartWebBrowser(String, BrowserID, String...)}<br>
	 * If user wants to stop one of these opened browser, use can call this method.<br>
	 * This method requires a parameter 'ID', which is given by user when he calls {@link #StartWebBrowser(String, BrowserID, String...)}<br>
	 * @param BrowserID String, the BrowserID served as key to get the WebDriver from cache.<br>
	 * @return - true on success<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @example
	 * <pre>
	 * {@code
	 * String browserID = "GoogleMain";
	 * StartWebBrowser("http://www.google.com", browserID);
	 * //do some testing, then
	 * StopWebBrowser(browserID);
	 * }
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 * @see #StartWebBrowser(String, String, String...)
	 */
	public static boolean StopWebBrowser(String BrowserID){
		return DriverCommand.StopWebBrowser(BrowserID);
	}

	/**
	 * Switch WebBrowser by ID.
	 * During test, multiple browsers can be opened by {@link #StartWebBrowser(String, int, String...)}<br>
	 * If user wants to switch between these opened browser, use can call this method.<br>
	 * This method requires a parameter 'ID', which is given by user when he calls {@link #StartWebBrowser(String, int, String...)}<br>
	 * See <a href="/sqabasic2000/DDDriverCommandsReference.htm#detail_UseWebBrowser">Detailed Reference</a>
	 * @param ID String, the ID served as key to get the WebDriver from cache.<br>
	 * @return true on success
	 * @example
	 * <pre>
	 * {@code
	 * SwitchWebBrowser("GoogleNewWindow");
	 * }
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 * @see #StartWebBrowser(String, String, String...)
	 */
	public static boolean SwitchWebBrowser(String ID){
		return DriverCommand.UseWebBrowser(ID);
	}

	/**
	 * Sends keystrokes to the current focused Component.<br>
	 * See <a href="/sqabasic2000/GenericMasterFunctionsReference.htm#detail_TypeKeys">Detailed Reference</a>
	 * <p>
	 * This supports special key characters like:
	 * <p><pre>
	 *     {Enter} = ENTER Key
	 *     {Tab} = TAB Key
	 *     ^ = CONTROL Key with another key ( "^s" = CONTROL + s )
	 *     % = ALT Key with another key ( "%F" = ALT + F )
	 *     + = SHIFT Key with another key ( "+{Enter}" = SHIFT + ENTER )
	 * </pre>
	 * We are generally providing this support through our generic <a href="/doc/org/safs/tools/input/CreateUnicodeMap.html">InputKeys Support</a>.
	 * <p>
	 * @param keystrokes String, to send via input to the current focused Component.
	 * @return true on success
	 * @see org.safs.robot.Robot#inputKeys(String)
	 * @see SeleniumPlus#quote(String)
	 * @example
	 * <pre>
	 * {@code
	 * SeleniumPlus.TypeKeys("% n");//"Alt+Space+n" Minimize the current window
	 * SeleniumPlus.TypeKeys(quote("^p"));//"Ctrl+p" Open a printer window for current window
	 * }
	 * </pre>
	 */
	public static boolean TypeKeys(String keystrokes){
		//call actionGUILess() instead of Component.TypeKeys(): to keep the log message consistent
		return actionGUILess(GenericMasterFunctions.TYPEKEYS_KEYWORD, normalizeTextForInput(keystrokes));
//		return Component.TypeKeys(keystrokes);
	}

	/**
	 * Sends characters to the current focused Component.<br>
	 * See <a href="/sqabasic2000/GenericMasterFunctionsReference.htm#detail_TypeChars">Detailed Reference</a>
	 * <p>
	 * @param textvalue String, to send via input to the current focused Component.
	 * @return true on success
	 * @see org.safs.robot.Robot#inputChars(String)
	 * @see SeleniumPlus#quote(String)
	 * @example
	 * <pre>
	 * {@code
	 * SeleniumPlus.TypeChars("Test Value");
	 * SeleniumPlus.TypeChars(quote("UTF-8"));
	 * SeleniumPlus.TypeChars(quote("^NotVariable"));
	 * }
	 * </pre>
	 */
	public static boolean TypeChars(String textvalue){
		//call actionGUILess() instead of Component.TypeChars(): to keep the log message consistent
		return actionGUILess(GenericMasterFunctions.TYPECHARS_KEYWORD, normalizeTextForInput(textvalue));
//		return Component.TypeChars(textvalue);
	}

	/**
	 * Sends secret-text (such as password) to the current focused Component.<br>
	 * See <a href="/sqabasic2000/GenericMasterFunctionsReference.htm#detail_TypeEncryption">Detailed Reference</a>
	 *
	 * <p>
	 * @param encryptedDataFile String, the file containing 'encrypted data' to send to the current focused Component.
	 *                                  It can be an absolute path, or a path relative the the test project's root.
	 * @param privateKeyFile String, the file containing 'private key' to decrypt the 'encrypted data'
	 *                               It can be an absolute path, or a path relative the the test project's root.
	 * @return true on success
	 * @see org.safs.robot.Robot#inputChars(String)
	 * @see org.safs.RSA
	 * @example
	 * <pre>
	 * //D:\secretPath\private.key contains "private key", which is generated by {@link org.safs.RSA}
	 * //C:\safs\passwords\encrypted.pass contains the encrypted-data, which is encrypted by {@link org.safs.RSA} with publickey
	 * {@code
	 * SeleniumPlus.TypeEncryption("C:\safs\passwords\encrypted.pass", "D:\secretPath\private.key" );
	 * }
	 * </pre>
	 */
	public static boolean TypeEncryption(String encryptedDataFile, String privateKeyFile){
		return Component.TypeEncryption(encryptedDataFile, privateKeyFile);
	}

	/**
	 * Hover the mouse over a specified screen location.
	 * <p>See <a href="/sqabasic2000/GenericMasterFunctionsReference.htm#detail_HoverScreenLocation">Detailed Reference</a>
	 * @param coordination String, The screen location, such as "200;400", or a mapKey defined under "ApplicationConstants" in map file
	 * @param optionals
	 * <ul>
	 * <b>optionals[0] hoverTime</b> int, milliseconds to hover
	 * </ul>
	 * @return true if hover succeeds, false otherwise.
	 * @example
	 * <pre>
	 * {@code
	 * boolean success = SeleniumPlus.HoverScreenLocation("500, 300", "20");
	 * boolean success = SeleniumPlus.HoverScreenLocation("locKey", "20");//locKey="500, 300" defined in map file under "ApplicationConstants"
	 * }
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean HoverScreenLocation(String coordination, String... optionals){
		return Component.HoverScreenLocation(coordination, optionals);
	}

	/**
	 * Verify the current contents of a binary (image, PDF) file with a benchmark file.
	 * <p>See <a href="/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyBinaryFileToFile">Detailed Reference</a>
	 * @param benchFile String, File used as the comparison benchmark.
	 * @param actualFile String, File used as the comparison file under test.
	 * @param optionals
	 * <ul>
	 * <b>optionals[0] FilterMode</b> String, one of {@link FilterMode}. {@link FilterMode#TOLERANCE} is valid only when the binary files are images.<br>
	 * <b>optionals[1] FilterOption</b>
	 *                                  <ul>
	 *                                  <li>int, if the {@link FilterMode} is {@link FilterMode#TOLERANCE}, a number between 0 and 100,
	 *                                        the percentage of bits need to be the same.
	 *                                        100 means only 100% match, 2 images will be considered matched;
	 *                                        0 means even no bits match, 2 images will be considered matched.<br>
	 *                                   <li>other type, if the FilterMode is FilterMode.XXX<br>
	 *                                   </ul>
	 * <b>optionals[2] alterImageStyle</b> {@link AlterImageStyle}, it is used to alter the diff image when 2 image/PDF doesn't match.<br>
	 * <b>optionals[3] alterImageFactor</b> double, how much lighter or darker to make the original pixel (with no difference), it is a number between 0 and 1.<br>
	 *                                      It only takes effect if the parameter 'alterImageStyle' is {@link AlterImageStyle#TINT} or {@link AlterImageStyle#SHADE}.<br>
	 *                                      For SHADE, the bigger the factor, the darker the shade.<br>
	 *                                      For TINT, the bigger the factor, the lighter the tint.<br>
	 * <b>optionals[4] pdfResolution</b> int, the resolution (DPI) used to convert PDF to image. The default is {@link Constants#IMAGE_PDF_CONVERSION_RESOLUTION_DEFAULT}.<br>
	 * </ul>
	 * @return true if the 2 files contain the same content, false otherwise.
	 * @example
	 * <pre>
	 * {@code
	 * boolean success = SeleniumPlus.VerifyBinaryFileToFile("signIn.png", "signIn.png");
	 * boolean success = SeleniumPlus.VerifyBinaryFileToFile("c:\bench\signIn.png", "d:\test\signIn.png");
	 * boolean success = SeleniumPlus.VerifyBinaryFileToFile("c:\bench\signIn.png", "d:\test\signIn.png", FilterMode.TOLERANCE.name, "90");
	 * boolean success = SeleniumPlus.VerifyBinaryFileToFile("bench.pdf", "actual.pdf", FilterMode.TOLERANCE.name, "100", AlterImageStyle.TINT.name, "0.9", "100");
	 * }
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean VerifyBinaryFileToFile(String benchFile, String actualFile, String... optionals){
		return Component.VerifyBinaryFileToFile(benchFile, actualFile, optionals);
	}
	/**
	 * Verify the current contents of a text file with a benchmark file (same as VerifyTextFileToFile).
	 * <p>See <a href="/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyFileToFile">Detailed Reference</a>
	 * @param benchFile String, File used as the comparison benchmark.
	 * @param actualFile String, File used as the comparison file under test.
	 * @param optionals -- NOT used yet
	 * @return true if the 2 files contain the same content, false otherwise.
	 * @example
	 * <pre>
	 * {@code
	 * boolean success = SeleniumPlus.VerifyFileToFile("benchFile.txt", "actualFile.txt");
	 * boolean success = SeleniumPlus.VerifyFileToFile("c:\bench\benchFile.txt", "d:\test\actualFile.txt");
	 * }
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean VerifyFileToFile(String benchFile, String actualFile, String... optionals){
		return Component.VerifyFileToFile(benchFile, actualFile, optionals);
	}
	/**
	 * Verify the current contents of a text file with a benchmark file (same as VerifyFileToFile).
	 * <p>See <a href="/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyTextFileToFile">Detailed Reference</a>
	 * @param benchFile String, File used as the comparison benchmark.
	 * @param actualFile String, File used as the comparison file under test.
	 * @param optionals -- NOT used yet
	 * @return true if the 2 files contain the same content, false otherwise.
	 * @example
	 * <pre>
	 * {@code
	 * boolean success = SeleniumPlus.VerifyTextFileToFile("benchFile.txt", "actualFile.txt");
	 * boolean success = SeleniumPlus.VerifyTextFileToFile("c:\bench\benchFile.txt", "d:\test\actualFile.txt");
	 * }
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean VerifyTextFileToFile(String benchFile, String actualFile, String... optionals){
		return Component.VerifyTextFileToFile(benchFile, actualFile, optionals);
	}
	/**
	 * Verify that a string value contains a substring.
	 * <p>See <a href="/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyValueContains">Detailed Reference</a>
	 * @param wholeString String, the string value to verify.
	 * @param substring String, the substring
	 * @param optionals
	 * <ul>
	 * <b>optionals[0] </b>String, Set to "SuppressValue" to prevent the logging of ugly multi-line values<br>
	 * </ul>
	 * @return true if a string value does contain a substring, false otherwise.
	 * @example
	 * <pre>
	 * {@code
	 * String labelVar = "labelVariable";
	 * Component.AssignPropertyVariable(Map.AUT.Lable,"textContent", labelVar);
	 * String label = SeleniumPlus.GetVariableValue(labelVar);
	 * boolean success = SeleniumPlus.VerifyValueContains(label, "labelContent");
	 * //or
	 * boolean success = SeleniumPlus.VerifyValueContains("^"+labelVar, "labelContent");
	 * }
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean VerifyValueContains(String wholeString, String substring, String... optionals){
		return Component.VerifyValueContains(wholeString, substring, optionals);
	}
	/**
	 * Verify that a string value contains a substring, ignoring case.
	 * <p>See <a href="/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyValueContainsIgnoreCase">Detailed Reference</a>
	 * @param wholeString String, the string value to verify.
	 * @param substring String, the substring
	 * @param optionals
	 * <ul>
	 * <b>optionals[0] </b>String, Set to "SuppressValue" to prevent the logging of ugly multi-line values<br>
	 * </ul>
	 * @return true if a string value does contain a substring ignoring case, false otherwise.
	 * @example
	 * <pre>
	 * {@code
	 * String labelVar = "labelVariable";
	 * Component.AssignPropertyVariable(Map.AUT.Lable,"textContent", labelVar);
	 * boolean success = SeleniumPlus.VerifyValueContainsIgnoreCase("^"+labelVar, "subcontent");
	 * }
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean VerifyValueContainsIgnoreCase(String wholeString, String substring, String... optionals){
		return Component.VerifyValueContainsIgnoreCase(wholeString, substring, optionals);
	}
	/**
	 * Verify that a string value does NOT contain a substring.
	 * <p>See <a href="/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyValueDoesNotContain">Detailed Reference</a>
	 * @param wholeString String, the string value to verify.
	 * @param substring String, the substring
	 * @param optionals
	 * <ul>
	 * <b>optionals[0] </b>String, Set to "SuppressValue" to prevent the logging of ugly multi-line values<br>
	 * </ul>
	 * @return true if a string value does NOT contain a substring, false otherwise.
	 * @example
	 * <pre>
	 * {@code
	 * String labelVar = "labelVariable";
	 * Component.AssignPropertyVariable(Map.AUT.Lable,"textContent", labelVar);
	 * boolean success = SeleniumPlus.VerifyValueDoesNotContain("^"+labelVar, "substr");
	 * }
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean VerifyValueDoesNotContain(String wholeString, String substring, String... optionals){
		return Component.VerifyValueDoesNotContain(wholeString, substring, optionals);
	}
	/**
	 * Verify that two string values are identical.
	 * <p>See <a href="/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyValues">Detailed Reference</a>
	 * @param value1 String, the first value to compare.
	 * @param value2 String, the second value to compare.
	 * @param optionals
	 * <ul>
	 * <b>optionals[0] </b>String, Set to "SuppressValue" to prevent the logging of ugly multi-line values<br>
	 * </ul>
	 * @return true if the two values do equal, false otherwise.
	 * @example
	 * <pre>
	 * {@code
	 * String labelVar = "labelVariable";
	 * Component.AssignPropertyVariable(Map.AUT.Lable,"textContent", labelVar);
	 * String label = SeleniumPlus.GetVariableValue(labelVar);
	 * boolean success = SeleniumPlus.VerifyValues(label, "labelContent");
	 * //or
	 * boolean success = SeleniumPlus.VerifyValues("^"+labelVar, "labelContent");
	 * }
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean VerifyValues(String value1, String value2, String... optionals){
		return Component.VerifyValues(value1, value2, optionals);
	}
	/**
	 * Verify that two string values are identical, ignoring case.
	 * <p>See <a href="/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyValuesIgnoreCase">Detailed Reference</a>
	 * @param value1 String, the first value to compare.
	 * @param value2 String, the second value to compare.
	 * @param optionals
	 * <ul>
	 * <b>optionals[0] </b>String, Set to "SuppressValue" to prevent the logging of ugly multi-line values<br>
	 * </ul>
	 * @return true if the two values do equal, false otherwise.
	 * @example
	 * <pre>
	 * {@code
	 * String labelVar = "labelVariable";
	 * Component.AssignPropertyVariable(Map.AUT.Lable,"textContent", labelVar);
	 * boolean success = SeleniumPlus.VerifyValuesIgnoreCase("^"+labelVar, "labelcontent");
	 * }
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean VerifyValuesIgnoreCase(String value1, String value2, String... optionals){
		return Component.VerifyValuesIgnoreCase(value1, value2, optionals);
	}
	/**
	 * Verify that two string values are NOT identical.
	 * <p>See <a href="/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyValuesNotEqual">Detailed Reference</a>
	 * @param value1 String, the first value to compare.
	 * @param value2 String, the second value to compare.
	 * @param optionals
	 * <ul>
	 * <b>optionals[0] </b>String, Set to "SuppressValue" to prevent the logging of ugly multi-line values<br>
	 * </ul>
	 * @return true if the two values do NOT equal, false otherwise.
	 * @example
	 * <pre>
	 * {@code
	 * String labelVar = "labelVariable";
	 * Component.AssignPropertyVariable(Map.AUT.Lable,"textContent", labelVar);
	 * boolean success = SeleniumPlus.VerifyValuesNotEqual("^"+labelVar, "labelContent");
	 * }
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean VerifyValuesNotEqual(String value1, String value2, String... optionals){
		return Component.VerifyValuesNotEqual(value1, value2, optionals);
	}

	/**
	 * Click on any visible component.
	 * <p>See <a href="/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_Click">Detailed Reference</a>
	 * @param comp -- Component (from App Map) to Click
	 * @param params optional
	 * <ul>
	 * <li><b>params[0] X,Y coordinate</b>. Example: "50,50", or "AppMapSubkey" defined in App Map<br>
	 * <b>For SE+</b>, params[0] X,Y coordinate also support percentage format for X, or Y. Example: "20%,30%", "20%,30", "20,30%".<br>
	 * <li><b>params[1] auto-scroll boolean</b> if the component will be scrolled into view automatically before clicking.
	 *                                        if not provided, the default value is true.
	 * <li><b>params[2] verify boolean</b> check if the component is visible. We try several ways to scroll the component into view: if this
	 *                                     parameter 'verify' is true then we will not try other ways if the component is already visible; otherwise
	 *                                     we will try all the ways to show the element even the component may have already shown on the page.
	 *                                     if not provided, the default value is true.
	 * </ul>
	 * @return true if successfully executed, false otherwise.<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @example
	 * <pre>
	 * {@code
	 * 1) boolean success = Click(Map.Google.Apps);//Click at the center
	 * 2) boolean success = Click(Map.Google.Apps,"20,20");//Click at the coordinate (20,20)
	 * 3) boolean success = Click(Map.Google.Apps,"20%,30%"); // Click at the coordinate: its X value equals 20% width of component, its Y value equals 30% height of component.
	 * 4) boolean success = Click(Map.Google.Apps,"AppMapSubkey");//Click at the coordinate defined by entry "AppMapSubkey" in App Map.
	 * 5) boolean success = Click(Map.Google.Apps,"20,20", "false");//Click at the coordinate (20,20), and web-element will NOT be automatically scrolled into view
	 * 6) boolean success = Click(Map.Google.Apps,"", "", "false");//Click at the center, and web-element will be automatically scrolled into view by all means
	 *  // one of the above and then,
	 * int rc = prevResults.getStatusCode();      // if useful
	 * String info = prevResults.getStatusInfo(); // if useful
	 * }
	 *
	 * Pay attention: If you use percentage format in SE+, you'd better use 'Misc.Expressions(false);' first.
	 *
	 * "AppMapSubkey" is expected to be an AppMap entry in an "Apps" section in the App Map.
	 * See <a href="/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_Click">Detailed Reference</a>
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean Click(org.safs.model.Component comp, String... params){
		return Component.Click(comp, params);
	}

	/**
	 * Control-Click on any visible component.
	 * <p>See <a href="/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_CtrlClick">Detailed Reference</a>
	 * @param comp -- Component (from App Map) to Click
	 * @param params optional
	 * <ul>
	 * <li><b>params[0] X,Y coordinate</b>. Example: "50,50", or "AppMapSubkey" defined in App Map<br>
	 * <b>For SE+</b>, params[0] X,Y coordinate also support percentage format for X, or Y. Example: "20%,30%", "20%,30", "20,30%".<br>
	 * <li><b>params[1] auto-scroll boolean</b> if the component will be scrolled into view automatically before clicking.
	 *                                        if not provided, the default value is true.
	 * <li><b>params[2] verify boolean</b> check if the component is visible. We try several ways to scroll the component into view: if this
	 *                                     parameter 'verify' is true then we will not try other ways if the component is already visible; otherwise
	 *                                     we will try all the ways to show the element even the component may have already shown on the page.
	 *                                     if not provided, the default value is true.
	 * </ul>
	 * @return true if successfully executed, false otherwise.<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @example
	 * <pre>
	 * {@code
	 * 1) boolean success = CtrlClick(Map.Google.Apps);//Control-Click at the center
	 * 2) boolean success = CtrlClick(Map.Google.Apps,"20,20");//Control-Click at the coordinate (20,20)
	 * 3) boolean success = CtrlClick(Map.Google.Apps,"20%,30%"); // Control-Click at the coordinate: its X value equals 20% width of component, its Y value equals 30% height of component.
	 * 4) boolean success = CtrlClick(Map.Google.Apps,"AppMapSubkey");//Control-Click at the coordinate defined by entry "AppMapSubkey" in App Map.
	 * 5) boolean success = CtrlClick(Map.Google.Apps,"20,20", "false");//Control-Click at the coordinate (20,20) and web-element will not be scrolled into view automatically
	 * 6) boolean success = CtrlClick(Map.Google.Apps,"", "", "false");//Control-Click at the center, and web-element will be automatically scrolled into view by all means
	 *  // one of the above and then,
	 * int rc = prevResults.getStatusCode();      // if useful
	 * String info = prevResults.getStatusInfo(); // if useful
	 * }
	 *
	 * Pay attention: If you use percentage format in SE+, you'd better use 'Misc.Expressions(false);' first.
	 *
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean CtrlClick(org.safs.model.Component comp, String... params){
		return Component.CtrlClick(comp, params);
	}

	/**
	 * Control-Right-Click on any visible component.
	 * <p>See <a href="/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_CtrlRightClick">Detailed Reference</a>
	 * @param comp -- Component (from App Map) to Click
	 * @param params optional
	 * <ul>
	 * <li><b>params[0] X,Y coordinate</b>. Example: "50,50", or "AppMapSubkey" defined in App Map<br>
	 * <b>For SE+</b>, params[0] X,Y coordinate also support percentage format for X, or Y. Example: "20%,30%", "20%,30", "20,30%".<br>
	 * <li><b>params[1] auto-scroll boolean</b> if the component will be scrolled into view automatically before clicking.
	 *                                        if not provided, the default value is true.
	 * <li><b>params[2] verify boolean</b> check if the component is visible. We try several ways to scroll the component into view: if this
	 *                                     parameter 'verify' is true then we will not try other ways if the component is already visible; otherwise
	 *                                     we will try all the ways to show the element even the component may have already shown on the page.
	 *                                     if not provided, the default value is true.
	 * </ul>
	 * @return true if successfully executed, false otherwise.<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @example
	 * <pre>
	 * {@code
	 * 1) boolean success = CtrlRightClick(Map.Google.Apps);//Control-Right-Click at the center
	 * 2) boolean success = CtrlRightClick(Map.Google.Apps,"20,20");//Control-Right-Click at the coordinate (20,20)
	 * 3) boolean success = CtrlRightClick(Map.Google.Apps,"20%,30%"); // Control-Right-Click at the coordinate: its X value equals 20% width of component, its Y value equals 30% height of component.
	 * 4) boolean success = CtrlRightClick(Map.Google.Apps,"AppMapSubkey");//Control-Right-Click at the coordinate defined by entry "AppMapSubkey" in App Map.
	 * 5) boolean success = CtrlRightClick(Map.Google.Apps,"20,20", "false");//Control-Right-Click at the coordinate (20,20) and web-element will not be scrolled into view automatically
	 * 5) boolean success = CtrlRightClick(Map.Google.Apps,"", "", "false");//Control-Right-Click at the center and web-element will be scrolled into view automatically by all means
	 *  // one of the above and then,
	 * int rc = prevResults.getStatusCode();      // if useful
	 * String info = prevResults.getStatusInfo(); // if useful
	 * }
	 *
	 * Pay attention: If you use percentage format in SE+, you'd better use 'Misc.Expressions(false);' first.
	 *
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean CtrlRightClick(org.safs.model.Component comp, String... params){
		return Component.CtrlRightClick(comp, params);
	}

	/**
	 * Double-Click on any visible component.
	 * <p>See <a href="/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_DoubleClick">Detailed Reference</a>
	 * @param comp -- Component (from App Map) to Click
	 * @param params optional
	 * <ul>
	 * <li><b>params[0] X,Y coordinate</b>. Example: "50,50", or "AppMapSubkey" defined in App Map<br>
	 * <b>For SE+,</b> params[0] X,Y coordinate also support percentage format for X, or Y. Example: "20%,30%", "20%,30", "20,30%".<br>
	 * <li><b>params[1] auto-scroll boolean</b> if the component will be scrolled into view automatically before clicking.
	 *                                        if not provided, the default value is true.
	 * <li><b>params[2] verify boolean</b> check if the component is visible. We try several ways to scroll the component into view: if this
	 *                                     parameter 'verify' is true then we will not try other ways if the component is already visible; otherwise
	 *                                     we will try all the ways to show the element even the component may have already shown on the page.
	 *                                     if not provided, the default value is true.
	 * </ul>
	 * @return true if successfully executed, false otherwise.<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @example
	 * <pre>
	 * {@code
	 * 1) boolean success = DoubleClick(Map.Google.Apps);//Double-Click at the center
	 * 2) boolean success = DoubleClick(Map.Google.Apps,"20,20");//Double-Click at the coordinate (20,20)
	 * 3) boolean success = DoubleClick(Map.Google.Apps,"20%,30%"); // Double-Click at the coordinate: its X value equals 20% width of component, its Y value equals 30% height of component.
	 * 4) boolean success = DoubleClick(Map.Google.Apps,"AppMapSubkey");//Double-Click at the coordinate defined by entry "AppMapSubkey" in App Map.
	 * 5) boolean success = DoubleClick(Map.Google.Apps,"20,20", "false");//Double-Click at the coordinate (20,20) and web-element will not be scrolled into view automatically
	 * 5) boolean success = DoubleClick(Map.Google.Apps,"", "", "false");//Double-Click at the center and web-element will be scrolled into view automatically by all means
	 *  // one of the above and then,
	 * int rc = prevResults.getStatusCode();      // if useful
	 * String info = prevResults.getStatusInfo(); // if useful
	 * }
	 *
	 * Pay attention: If you use percentage format in SE+, you'd better use 'Misc.Expressions(false);' first.
	 *
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean DoubleClick(org.safs.model.Component comp, String... params){
		return Component.DoubleClick(comp, params);
	}

	/**
	 * Right-Click on any visible component.
	 * <p>See <a href="/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_RightClick">Detailed Reference</a>
	 * @param comp -- Component (from App Map) to Click
	 * @param params optional
	 * <ul>
	 * <li><b>params[0] X,Y coordinate</b>. Example: "50,50", or "AppMapSubkey" defined in App Map<br>
	 * <b>For SE+</b>, params[0] X,Y coordinate also support percentage format for X, or Y. Example: "20%,30%", "20%,30", "20,30%".<br>
	 * <li><b>params[1] auto-scroll boolean</b> if the component will be scrolled into view automatically before clicking.
	 *                                        if not provided, the default value is true.
	 * <li><b>params[2] verify boolean</b> check if the component is visible. We try several ways to scroll the component into view: if this
	 *                                     parameter 'verify' is true then we will not try other ways if the component is already visible; otherwise
	 *                                     we will try all the ways to show the element even the component may have already shown on the page.
	 *                                     if not provided, the default value is true.
	 * </ul>
	 * @return true if successfully executed, false otherwise.<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @example
	 * <pre>
	 * {@code
	 * 1) boolean success = RightClick(Map.Google.Apps);//Right-Click at the center
	 * 2) boolean success = RightClick(Map.Google.Apps,"20,20");//Right-Click at the coordinate (20,20)
	 * 3) boolean success = RightClick(Map.Google.Apps,"20%,30%"); // Right-Click at the coordinate: its X value equals 20% width of component, its Y value equals 30% height of component.
	 * 4) boolean success = RightClick(Map.Google.Apps,"AppMapSubkey");//Right-Click at the coordinate defined by entry "AppMapSubkey" in App Map.
	 * 5) boolean success = RightClick(Map.Google.Apps,"20,20", "false");//Right-Click at the coordinate (20,20) and web-element will not be scrolled into view automatically
	 * 5) boolean success = RightClick(Map.Google.Apps,"", "", "false");//Right-Click at the center and web-element will be scrolled into view automatically by all means
	 *  // one of the above and then,
	 * int rc = prevResults.getStatusCode();      // if useful
	 * String info = prevResults.getStatusInfo(); // if useful
	 * }
	 *
	 * Pay attention: If you use percentage format in SE+, you'd better use 'Misc.Expressions(false);' first.
	 *
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean RightClick(org.safs.model.Component comp, String... params){
		return Component.RightClick(comp, params);
	}

	/**
	 * Shift-Click on any visible component.
	 * <p>See <a href="/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_ShiftClick">Detailed Reference</a>
	 * @param comp -- Component (from App Map) to Click
	 * @param params optional
	 * <ul>
	 * <li><b>params[0] X,Y coordinate</b>. Example: "50,50", or "AppMapSubkey" defined in App Map<br>
	 * <b>For SE+</b>, params[0] X,Y coordinate also support percentage format for X, or Y. Example: "20%,30%", "20%,30", "20,30%".<br>
	 * <li><b>params[1] auto-scroll boolean</b> if the component will be scrolled into view automatically before clicking.
	 *                                        if not provided, the default value is true.
	 * <li><b>params[2] verify boolean</b> check if the component is visible. We try several ways to scroll the component into view: if this
	 *                                     parameter 'verify' is true then we will not try other ways if the component is already visible; otherwise
	 *                                     we will try all the ways to show the element even the component may have already shown on the page.
	 *                                     if not provided, the default value is true.
	 * </ul>
	 * @return true if successfully executed, false otherwise.<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @example
	 * <pre>
	 * {@code
	 * 1) boolean success = ShiftClick(Map.Google.Apps);//Shift-Click at the center
	 * 2) boolean success = ShiftClick(Map.Google.Apps,"20,20");//Shift-Click at the coordination (20,20)
	 * 3) boolean success = ShiftClick(Map.Google.Apps,"20%,30%"); // Shift-Click at the coordinate: its X value equals 20% width of component, its Y value equals 30% height of component.
	 * 4) boolean success = ShiftClick(Map.Google.Apps,"AppMapSubkey");//Shift-Click at the coordination defined by entry "AppMapSubkey" in App Map.
	 * 5) boolean success = ShiftClick(Map.Google.Apps,"20,20", "false");//Shift-Click at the coordination (20,20) and web-element will not be scrolled into view automatically
	 * 5) boolean success = ShiftClick(Map.Google.Apps,"", "", "false");//Shift-Click at the center, and web-element will be automatically scrolled into view by all means
	 *  // one of the above and then,
	 * int rc = prevResults.getStatusCode();      // if useful
	 * String info = prevResults.getStatusInfo(); // if useful
	 * }
	 *
	 * Pay attention: If you use percentage format in SE+, you'd better use 'Misc.Expressions(false);' first.
	 *
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean ShiftClick(org.safs.model.Component comp, String... params){
		return Component.ShiftClick(comp, params);
	}

	/**
	 * A left mouse drag is performed on the object based on the stored coordinates relative to this object.
	 * <p>See <a href="/sqabasic2000/GenericObjectFunctionsReference.htm#detail_LeftDrag">Detailed Reference</a>
	 * @param comp Component, the component (from App Map) relative to which to calculate coordinates to drag
	 * @param coordinates String, the relative coordinates. Example: "Coords=3,10,12,20", or "coordsKey" defined in App Map<br>
	 * @return true if successfully executed, false otherwise.<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @example
	 * <pre>
	 * {@code
	 * boolean success = LeftDrag(Map.Google.Apps,"3,10,12,20");//Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
	 * boolean success = LeftDrag(Map.Google.Apps,"Coords=3,10,12,20");//Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
	 * boolean success = LeftDrag(Map.Google.Apps,"coordsKey");//"coordsKey" is defined in map file under section [Apps]
	 * //one of the above and then,
	 * int rc = prevResults.getStatusCode();      // if useful
	 * String info = prevResults.getStatusInfo(); // if useful
	 * }
	 *
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean LeftDrag(org.safs.model.Component comp, String coordinates){
		return Component.LeftDrag(comp, coordinates);
	}
	/**
	 * A Shift left mouse drag is performed on the object based on the stored coordinates relative to this object.
	 * <p>See <a href="/sqabasic2000/GenericObjectFunctionsReference.htm#detail_ShiftLeftDrag">Detailed Reference</a>
	 * @param comp Component, the component (from App Map) relative to which to calculate coordinates to drag
	 * @param coordinates String, the relative coordinates. Example: "Coords=3,10,12,20", or "coordsKey" defined in App Map<br>
	 * @return true if successfully executed, false otherwise.<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @example
	 * <pre>
	 * {@code
	 * boolean success = ShiftLeftDrag(Map.Google.Apps,"3,10,12,20");//Shift-Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
	 * boolean success = ShiftLeftDrag(Map.Google.Apps,"Coords=3,10,12,20");//Shift-Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
	 * boolean success = ShiftLeftDrag(Map.Google.Apps,"coordsKey");//"coordsKey" is defined in map file under section [Apps]
	 * //one of the above and then,
	 * int rc = prevResults.getStatusCode();      // if useful
	 * String info = prevResults.getStatusInfo(); // if useful
	 * }
	 *
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean ShiftLeftDrag(org.safs.model.Component comp, String coordinates){
		return Component.ShiftLeftDrag(comp, coordinates);
	}
	/**
	 * A Ctrl Shift left mouse drag is performed on the object based on the stored coordinates relative to this object.
	 * <p>See <a href="/sqabasic2000/GenericObjectFunctionsReference.htm#detail_CtrlShiftLeftDrag">Detailed Reference</a>
	 * @param comp Component, the component (from App Map) relative to which to calculate coordinates to drag
	 * @param coordinates String, the relative coordinates. Example: "Coords=3,10,12,20", or "coordsKey" defined in App Map<br>
	 * @return true if successfully executed, false otherwise.<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @example
	 * <pre>
	 * {@code
	 * boolean success = CtrlShiftLeftDrag(Map.Google.Apps,"3,10,12,20");//Ctrl-Shift-Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
	 * boolean success = CtrlShiftLeftDrag(Map.Google.Apps,"Coords=3,10,12,20");//Ctrl-Shift-Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
	 * boolean success = CtrlShiftLeftDrag(Map.Google.Apps,"coordsKey");//"coordsKey" is defined in map file under section [Apps]
	 * //one of the above and then,
	 * int rc = prevResults.getStatusCode();      // if useful
	 * String info = prevResults.getStatusInfo(); // if useful
	 * }
	 *
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean CtrlShiftLeftDrag(org.safs.model.Component comp, String coordinates){
		return Component.CtrlShiftLeftDrag(comp, coordinates);
	}
	/**
	 * A Ctrl left mouse drag is performed on the object based on the stored coordinates relative to this object.
	 * <p>See <a href="/sqabasic2000/GenericObjectFunctionsReference.htm#detail_CtrlLeftDrag">Detailed Reference</a>
	 * @param comp Component, the component (from App Map) relative to which to calculate coordinates to drag
	 * @param coordinates String, the relative coordinates. Example: "Coords=3,10,12,20", or "coordsKey" defined in App Map<br>
	 * @return true if successfully executed, false otherwise.<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @example
	 * <pre>
	 * {@code
	 * boolean success = CtrlLeftDrag(Map.Google.Apps,"3,10,12,20");//Ctrl-Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
	 * boolean success = CtrlLeftDrag(Map.Google.Apps,"Coords=3,10,12,20");//Ctrl-Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
	 * boolean success = CtrlLeftDrag(Map.Google.Apps,"coordsKey");//"coordsKey" is defined in map file under section [Apps]
	 * //one of the above and then,
	 * int rc = prevResults.getStatusCode();      // if useful
	 * String info = prevResults.getStatusInfo(); // if useful
	 * }
	 *
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean CtrlLeftDrag(org.safs.model.Component comp, String coordinates){
		return Component.CtrlLeftDrag(comp, coordinates);
	}
	/**
	 * A Alt left mouse drag is performed on the object based on the stored coordinates relative to this object.
	 * <p>See <a href="/sqabasic2000/GenericObjectFunctionsReference.htm#detail_AltLeftDrag">Detailed Reference</a>
	 * @param comp Component, the component (from App Map) relative to which to calculate coordinates to drag
	 * @param coordinates String, the relative coordinates. Example: "Coords=3,10,12,20", or "coordsKey" defined in App Map<br>
	 * @return true if successfully executed, false otherwise.<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @example
	 * <pre>
	 * {@code
	 * boolean success = AltLeftDrag(Map.Google.Apps,"3,10,12,20");//Alt-Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
	 * boolean success = AltLeftDrag(Map.Google.Apps,"Coords=3,10,12,20");//Alt-Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
	 * boolean success = AltLeftDrag(Map.Google.Apps,"coordsKey");//"coordsKey" is defined in map file under section [Apps]
	 * //one of the above and then,
	 * int rc = prevResults.getStatusCode();      // if useful
	 * String info = prevResults.getStatusInfo(); // if useful
	 * }
	 *
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean AltLeftDrag(org.safs.model.Component comp, String coordinates){
		return Component.AltLeftDrag(comp, coordinates);
	}
	/**
	 * A Ctrl Alt left mouse drag is performed on the object based on the stored coordinates relative to this object.
	 * <p>See <a href="/sqabasic2000/GenericObjectFunctionsReference.htm#detail_CtrlAltLeftDrag">Detailed Reference</a>
	 * @param comp Component, the component (from App Map) relative to which to calculate coordinates to drag
	 * @param coordinates String, the relative coordinates. Example: "Coords=3,10,12,20", or "coordsKey" defined in App Map<br>
	 * @return true if successfully executed, false otherwise.<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @example
	 * <pre>
	 * {@code
	 * boolean success = CtrlAltLeftDrag(Map.Google.Apps,"3,10,12,20");//Ctrl-Alt-Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
	 * boolean success = CtrlAltLeftDrag(Map.Google.Apps,"Coords=3,10,12,20");//Ctrl-Alt-Left-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
	 * boolean success = CtrlAltLeftDrag(Map.Google.Apps,"coordsKey");//"coordsKey" is defined in map file under section [Apps]
	 * //one of the above and then,
	 * int rc = prevResults.getStatusCode();      // if useful
	 * String info = prevResults.getStatusInfo(); // if useful
	 * }
	 *
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean CtrlAltLeftDrag(org.safs.model.Component comp, String coordinates){
		return Component.CtrlAltLeftDrag(comp, coordinates);
	}
	/**
	 * A right mouse drag is performed on the object based on the stored coordinates relative to this object.
	 * <p>See <a href="/sqabasic2000/GenericObjectFunctionsReference.htm#detail_RightDrag">Detailed Reference</a>
	 * @param comp Component, the component (from App Map) relative to which to calculate coordinates to drag
	 * @param coordinates String, the relative coordinates. Example: "Coords=3,10,12,20", or "coordsKey" defined in App Map<br>
	 * @return true if successfully executed, false otherwise.<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @example
	 * <pre>
	 * {@code
	 * boolean success = RightDrag(Map.Google.Apps,"3,10,12,20");//Right-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
	 * boolean success = RightDrag(Map.Google.Apps,"Coords=3,10,12,20");//Right-Drag from (3,10) to (12,20), relative to the Left Up corner of component Map.Google.Apps
	 * boolean success = RightDrag(Map.Google.Apps,"coordsKey");//"coordsKey" is defined in map file under section [Apps]
	 * //one of the above and then,
	 * int rc = prevResults.getStatusCode();      // if useful
	 * String info = prevResults.getStatusInfo(); // if useful
	 * }
	 *
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean RightDrag(org.safs.model.Component comp, String coordinates){
		return Component.RightDrag(comp, coordinates);
	}

	/**
	 * Highlight object
	 * @param OnOff -- true or false for object highlight
	 * @return true on success
	 */
	public static boolean Highlight(boolean OnOff){
		return DriverCommand.Highlight(OnOff);
	}

	/**
	 * Pause test-case flow in seconds. If you want to pause in millisecond, use {@link Misc#Delay(int)}.
	 * @param seconds int, the seconds to pause
	 * @return true if successfully executed, false otherwise.<p>
	 * @example
	 * <pre>
	 * {@code
	 * Pause(20);
	 * }
	 * </pre>
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 * @see Misc#Delay(int)
	 */
	public static boolean Pause(int seconds){
		return DriverCommand.Pause(seconds);
	}

	/**
	 * Take a screenshot of windows or component.<br>
	 * See <a href="/sqabasic2000/GenericMasterFunctionsReference.htm#detail_GetGUIImage">Detailed Reference</a><p>
	 * @param comp Component, the component to get its image.
	 * @param fileName String, the file name to store image. Suggest to save as .png image.<br>
	 *                         It can be relative or absolute. If it is relative, the file will be stored to TestDirectory "Actuals".<br>
	 *                         Supported file extensions are ".jpeg", ".tif", ".gif", ".png", ".pnm", ".bmp" etc.<br>
	 *                         If the file extension is not supported, then suffix ".bmp" will be appended to filename.<br>
	 * @param params optional
	 * <ul>
	 * <b>params[1] SubArea</b> String, (x1,y1,x2,y2) indicating partial image of the component to capture, such as "0,0,50%,50%", <br>
	 *                                  it can be app map subkey under component name.<br>
	 * <b>params[2] FilteredAreas</b> String, (x1,y1,x2,y2 x1,y1,x2,y2) a set of areas to filter the current GUI image.<br>
	 *                          it has a prefix "<font color='red'>Filter</font>", and followed by a set of subareas. Such as "<font color='red'>Filter</font>=0,0,5,5 50%,50%,15,15"<br>
	 *                          Multiple areas are separated by a space character. The filtered area is covered by black.<br>
	 * </ul>
	 * @return boolean, true on success; false otherwise
	 * @example
	 * <pre>
	 * {@code
	 * GetGUIImage(Map.Google.SignIn,"SignIn");//will be saved at <testProject>\Actuals\SignIn.bmp
	 * GetGUIImage(Map.Google.SignIn,"c:/temp/SignIn.gif");
	 *
	 * //Following example will store part of the SingIn image,
	 * GetGUIImage(Map.Google.SignIn,"SignInPartial.png", "0,0,50%,50%");
	 * //"subarea" is defined in map file
	 * //[SignIn]
	 * //subarea="0,0,50%,50%"
	 * GetGUIImage(Map.Google.SignIn,"SignInPartial.png", "subarea");
	 * //or
	 * //"subarea" is defined in map file
	 * //[ApplicationConstants]
	 * //subarea="0,0,50%,50%"
	 * GetGUIImage(Map.Google.SignIn,"SignInPartial.png", Map.subarea);
	 * GetGUIImage(Map.Google.SignIn,"SignInPartial.png", Map.subarea());
	 *
	 * //Filter the SingIn image and save it
	 * GetGUIImage(Map.Google.SignIn,"SignInFiltered.gif", "", quote("Filter=0,0,10,10 60,60,10,10"));
	 * //"filterAreas" is defined in map file
	 * //[SignIn]
	 * //filterAreas="Filter=0,0,10,10 60,60,10,10"
	 * GetGUIImage(Map.Google.SignIn,"SignInFiltered.gif", "", "filterAreas");
	 * //"filterAreas" is defined in map file
	 * //[ApplicationConstants]
	 * //filterAreas="Filter=0,0,10,10 60,60,10,10"
	 * GetGUIImage(Map.Google.SignIn,"SignInFiltered.gif", "", Map.filterAreas);
	 * GetGUIImage(Map.Google.SignIn,"SignInFiltered.gif", "", Map.filterAreas());
	 * }
	 * </pre>
	 *
	 */
	public static boolean GetGUIImage(org.safs.model.Component comp, String fileName, String... params){
		return Component.GetGUIImage(comp, fileName, params);
	}
	/**
	 * Take a screenshot of windows or component and filter some children inside.<br>
	 * @param comp org.safs.model.Component, the component to get its image.
	 * @param fileName String, the file name to store image. Suggest to save as .png image.<br>
	 *                         It can be relative or absolute. If it is relative, the file will be stored to TestDirectory "Actuals".<br>
	 *                         Supported file extensions are ".jpeg", ".tif", ".gif", ".png", ".pnm", ".bmp" etc.<br>
	 *                         If the file extension is not supported, then suffix ".bmp" will be appended to filename.<br>
	 * @param subArea String, (x1,y1,x2,y2) indicating partial image of the component to capture, such as "0,0,50%,50%", <br>
	 *                         it can be app map subkey under component name.<br>
	 * @param childrenToMask org.safs.model.Component[], an array of child to filter,
	 *                       if some child is outside of parent, then it will be ignored.
	 * @return boolean, true on success; false otherwise
	 * <pre>
	 * {@code
	 * org.safs.model.Component[] filterChildren = new org.safs.model.Component[5];
	 * filterChildren[0]=Map.SAPDemoPage.Basc_Button;
	 * filterChildren[1]=Map.SAPDemoPage.Basc_Radio;
	 * filterChildren[2]=Map.SAPDemoPage.Basc_Link;
	 * filterChildren[3]=Map.SAPDemoPage.Basc_Password;
	 * filterChildren[4]=Map.SAPDemoPage.Basc_TextArea_L;
	 * GetGUIImage(Map.SAPDemoPage.Panel_Basc, "Panel_Basc_Filtered.png", "", filterChildren);
	 * //compare with image "Panel_Basc.png", which is not filtered
	 * GetGUIImage(Map.SAPDemoPage.Panel_Basc, "Panel_Basc.png");
	 * }
	 * </pre>
	 */
	public static boolean GetGUIImage(org.safs.model.Component comp, String fileName, String subArea, org.safs.model.Component[] childrenToMask){
		try {
			String filterAreas = deduceFilterAreas(comp, childrenToMask);
			//quote the filterAreas to avoid SASF-Expression-Evaluation removing "Filter=" part, which is needed by GetGUIImage
			GetGUIImage(comp, fileName, subArea, quote(filterAreas));
			return true;
		} catch (Exception e) {
			IndependantLog.error(StringUtils.debugmsg(false)+"Fail due to "+StringUtils.debugmsg(e));
			return false;
		}
	}
	/**
	 * Generate a "filter string" (Filter=x1,y1,x2,y2 x1,y1,x2,y2) according to a parent component and an array of children.<br>
	 * The deduced "filter string" can be used in {@link #GetGUIImage(org.safs.model.Component, String, String...)} or<br>
	 * {@link #VerifyGUIImageToFile(org.safs.model.Component, String, String...)}. <br>
	 * As the deduced "filter string" contains "Filter=", it will be considered variable-assignment and "Filter=" will be removed,<br>
	 * while GetGUIImage/VerifyGUIImageToFile needs the filterAreas parameter containing "Filter=". To avoid this problem:<br>
	 * It is SUGGESTED to call {@link #quote(String)} to wrap the "filter string" and then pass it to GetGUIImage/VerifyGUIImageToFile<br>
	 * Or make sure the expression-evaluation is off by calling {@link Misc#Expressions(boolean)}<br>
	 * @param parent org.safs.model.Component, the parent component
	 * @param childrenToMask org.safs.model.Component[], the children inside the parent component; the child outside of
	 *                       parent component will be ignored.
	 * @return a filter string such as Filter=x1,y1,x2,y2 x1,y1,x2,y2
	 */
	public static String deduceFilterAreas(org.safs.model.Component parent, org.safs.model.Component[] childrenToMask){
		String debugmsg = StringUtils.debugmsg(false);

		StringBuffer filterAreas = new StringBuffer();
		try {
			Rectangle parentRec = WDLibrary.getRectangleOnScreen(getObject(parent));

			if(childrenToMask!=null && childrenToMask.length>0){
				Rectangle childRec = null;
				int x1, y1, x2, y2;

				filterAreas.append("Filter=");
				for(org.safs.model.Component child:childrenToMask){
					childRec = WDLibrary.getRectangleOnScreen(getObject(child));
					x1 = childRec.x-parentRec.x;
					y1 = childRec.y-parentRec.y;
					x2 = x1+childRec.width;
					y2 = y1+childRec.height;
					if(x1<0 || x1>parentRec.width ||
					   y1<0 || y1>parentRec.height ||
					   x2<0 || x2>parentRec.width ||
					   y2<0 || y2>parentRec.height){
						IndependantLog.warn(debugmsg+"outof parent, ignore child "+child.getName());
						continue;
					}
					filterAreas.append(x1+","+y1+","+x2+","+y2+" ");
				}
			}
			IndependantLog.debug(debugmsg+"deduced filterAreas="+filterAreas);

		} catch (Exception e) {
			IndependantLog.error(debugmsg+"Fail due to "+StringUtils.debugmsg(e));
		}

		return filterAreas.toString();
	}

	/**
	 * Verify the screen shot of a GUI component with a benchmark image file.
	 * See <a href="/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyGUIImageToFile">Detailed Reference</a><p>
	 * @param comp Component, the component to get its image.
	 * @param benchFile String, the benchmark file name. Suggest to compare with .png image.<br>
	 *                         It can be relative or absolute. If it is relative, the file will be found at BenchDirectory "Benchmarks".<br>
	 *                         Supported file extensions are ".jpeg", ".tif", ".gif", ".png", ".pnm", ".bmp" etc.<br>
	 *                         If the file extension is not supported, then suffix ".bmp" will be appended to filename.<br>
	 * @param params optional
	 * <ul>
	 * <b>params[1] SubArea</b> String, indicating partial image of the component to capture, such as "0,0,50%,50%", <br>
	 *                                  it can be app map subkey under component name.<br>
	 * <b>params[2] PercentageTolerance</b> int, the percentage of bits need to be matched. it is between 0 and 100.<br>
	 *                                100 means only all bits of images match, the images will be considered matched.<br>
	 *                                0 means even no bits match, the images will be considered matched.<br>
	 * <b>params[3] UUIDFlag</b> boolean, set to quote("UUID=False") to prevent runtime Test/Actual filenames appended with Universally Unique IDs<br>
	 *                     This essentially allows the runtime Test/Actual filename to be the same as the Benchmark.<br>
	 * <b>params[4] FilteredAreas</b> String, a set of areas to filter the current GUI image and the bench image before comparing.<br>
	 *                          it has a prefix "<font color='red'>Filter</font>", and followed by a set of subareas. Such as "<font color='red'>Filter</font>=0,0,5,5 50%,50%,15,15"<br>
	 *                          Multiple areas are separated by a space character. The filtered area is covered by black.
	 * </ul>
	 * @return boolean, true if verification success; false otherwise
	 * @example
	 * <pre>
	 * {@code
	 * VerifyGUIImageToFile(Map.Google.SignIn,"SignIn");//will be compared with file <testProject>\Benchmarks\SignIn.bmp
	 * VerifyGUIImageToFile(Map.Google.SignIn,"c:/benchDir/SignIn.gif");
	 * VerifyGUIImageToFile(Map.Google.SignIn,"c:/benchDir/SignIn.gif", "", "", quote("UUID=False"));// Simple output filename, no UUID.
	 * VerifyGUIImageToFile(Map.Google.SignIn,"c:/benchDir/SignIn.gif", "", "95");//if 95% bits match, the verification will pass.
	 *
	 * //Following example will verify part of the SingIn image,
	 * GetGUIImage(Map.Google.SignIn,"SignInPartial.png", quote("0,0,50%,50%"));
	 * //"subarea" is defined in map file
	 * //[SignIn]
	 * //subarea="0,0,50%,50%"
	 * VerifyGUIImageToFile(Map.Google.SignIn,"SignInPartial.png", "subarea");
	 * //or
	 * //"subarea" is defined in map file
	 * //[ApplicationConstants]
	 * //subarea="0,0,50%,50%"
	 * VerifyGUIImageToFile(Map.Google.SignIn,"SignInPartial.png", Map.subarea);
	 * VerifyGUIImageToFile(Map.Google.SignIn,"SignInPartial.png", Map.subarea());
	 *
	 * //Filter the SingIn image and the bench image at certain areas and compare them
	 * VerifyGUIImageToFile(Map.Google.SignIn,"c:/benchDir/SignIn.gif", "", "", "", quote("Filter=0,0,10,10 60,60,10,10"));
	 * //"filterAreas" is defined in map file
	 * //[SignIn]
	 * //filterAreas="Filter=0,0,10,10 60,60,10,10"
	 * VerifyGUIImageToFile(Map.Google.SignIn,"c:/benchDir/SignIn.gif", "", "", "", "filterAreas");
	 * //"filterAreas" is defined in map file
	 * //[ApplicationConstants]
	 * //filterAreas="Filter=0,0,10,10 60,60,10,10"
	 * VerifyGUIImageToFile(Map.Google.SignIn,"c:/benchDir/SignIn.gif", "", "", "", Map.filterAreas);
	 * VerifyGUIImageToFile(Map.Google.SignIn,"c:/benchDir/SignIn.gif", "", "", "", Map.filterAreas());
	 * }
	 * </pre>
	 */
	public static boolean VerifyGUIImageToFile(org.safs.model.Component comp, String benchFile, String... params){
		return Component.VerifyGUIImageToFile(comp, benchFile, params);
	}

	/**
	 * Execute a simple piece of javascript on component synchronously.
	 * See <a href="/sqabasic2000/GenericMasterFunctionsReference.htm#detail_ExecuteScript">Detailed Reference</a><p>
	 * If the script will return a string value, call SeleniumPlus.prevResults.getStatusInfo() to get it. <br>
	 * Object result is NOT supported yet.<br>
	 * You can also call {@link #executeScript(String, Object...)} instead, it is more efficient.<br>
	 * @param comp org.safs.model.Component, (from generated Map.java).<br>
	 *                                       In your script you reference this DOM WebElement as '<b>arguments[0]</b>'.<br>
	 * @param script String, the javascript to execute.<br>
	 * @param scriptParams optional, Script arguments must be a number, a boolean, a String, WebElement, or a List of any combination of the above.
	 *                               An exception will be thrown if the arguments do not meet these criteria.
	 *                               The arguments will be made available to the JavaScript via the "arguments" variable.
	 * <ul>
	 * scriptParams[0] : Passed to the script as '<b>arguments[1]</b>', if used.<br>
	 * scriptParams[1] : Passed to the script as '<b>arguments[2]</b>', if used.<br>
	 * ... more script's parameter<br>
	 * </ul>
	 * @return true if no errors were encountered.
	 * @example
	 * <pre>
	 * {@code
	 * SeleniumPlus.ExecuteScript(
	 *     Map.Google.SignIn,                       // The WebElement passed as 'arguments[0]' to the script.
	 *     "arguments[0].innerHTML=arguments[1];",  // Script to set the WebElements innerHTML value.
	 *     "my text value");                        // The value passed as 'arguments[1]' to set to innerHTML.
	 *
	 * SeleniumPlus.ExecuteScript(
	 *     Map.Google.SignIn,                       // The WebElement passed as 'arguments[0]' to the script.
	 *     "return arguments[0].innerHTML;");       // A script to return the WebElemenbts innerHTML.
	 *
	 *  // scriptResult should get the innerHTML value returned.
	 * String scriptResult = SeleniumPlus.prevResults.getStatusInfo();
	 * }
	 * </pre>
	 * @see #executeScript(String, Object...)
	 * @see #executeAsyncScript(String, Object...)
	 */
	public static boolean ExecuteScript(org.safs.model.Component comp, String script, String... scriptParams){
		return Component.ExecuteScript(comp, script, scriptParams);
	}

	public static final String TEMP_SELENIUM_PLUS_RS_VAR = "___temp_selenium_plus_rs___";
	/**
	 * Find the WebElement according to the SAFS Component.<br>
	 * This method will use the last webdriver as search context to find element,<br>
	 * if user wants to find an element within an other context, please call {@link #getObject(SearchContext, String)}<br>
	 * @param Component, The component to search.
	 * @return WebElement, the sought webelement
	 * @throws SeleniumPlusException
	 */
	public static WebElement getObject(org.safs.model.Component component) throws SeleniumPlusException{
		if (component.getParent() == null) {
			String prs = Misc.GetAppMapValue(component, "", "false");
			return WDLibrary.getObject(prs);
		} else {
			String prs = Misc.GetAppMapValue(component.getParent(), "", "false");
			WDLibrary.checkWindowRS(prs);//To see if we need to reset the WDLibrary's lastFrame to null.

			WebElement pel = WDLibrary.getObject(prs);
			String crs = Misc.GetAppMapValue(component, "", "false");
			return WDLibrary.getObject(pel,crs);
		}
	}

	/**
	 * Find the WebElement according to the SAFS Component.<br>
	 * @param sc could be the WebDriver or a parent WebElement context.
	 * @param Component, The component to search.
	 * @return WebElement, the sought webelement
	 * @throws SeleniumPlusException
	 */
	public static WebElement getObject(SearchContext sc, org.safs.model.Component component) throws SeleniumPlusException{
		String rs = Misc.GetAppMapValue(component, "", "false");
		if (component.getParent() != null) {
			String prs = Misc.GetAppMapValue(component.getParent(), "", "false");
			WDLibrary.checkWindowRS(prs);//To see if we need to reset the WDLibrary's lastFrame to null.

			WebElement pel = WDLibrary.getObject(sc, prs);
			return WDLibrary.getObject(pel, rs);

		}else{
			return WDLibrary.getObject(sc, rs);
		}

	}

	/**
	 * Wait for a Window or Component to become valid.<br>
	 * <b>Note:</b><br>
	 * <font color=red>This API doesn't fit for switching according to GUI existence</font>, please use {@link Misc#IsComponentExists(org.safs.model.Component, String...)} instead.<br>
	 * If the GUI doesn't become valid within timeout, this method will return false.<br>
	 * If the execution doesn't end properly, this method will also return false.<br>
	 * User cannot distinguish these 2 cases; and in the same time, a failure message will be written into Log.<br>
	 *
	 * @param comp -- Component (from generated Map.java)
	 * @param time - time in second
	 * @return true if the GUI appear within the timeout.<br>
	 *         false there 2 possibilities:<br>
	 *               1. The execution finishes properly but the GUI doesn’t appear within the timeout.<br>
	 *               2. The execution doesn't finish properly.<br>
	 *
	 * @example
	 * <pre>
	 * {@code
	 * WaitForGUI(Map.Google.SignIn,10);
	 * }
	 * </pre>
	 *
	 * @see SAFSPlus.Misc#IsComponentExists(org.safs.model.Component, String...)
	 */
	public static boolean WaitForGUI(org.safs.model.Component comp, long time){
		return DriverCommand.WaitForGUI(comp, time);
	}

	/**
	 * Start capturing test activity counts for a specific application feature or test-case.
	 * @param tcname The name of the test-case to start using a Counter on.
	 * @return false only if a failure of some kind was reported in attempting to start the counter.<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean StartTestCase(String tcname){
		return Counters.StartTestCase(tcname);
	}

	/**
	 * Start capturing test activity counts for the named test suite.
	 * @param suitename The name of the suite counter to start.
	 * @return false only if a failure of some kind was reported in attempting to start the counter.<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean StartTestSuite(String suitename){
		return Counters.StartTestSuite(suitename);
	}

	/**
	 * Stop capturing test activity counts for a specific application feature or test-case.
	 * @param tcname The name of the test-case to stop.  The name must match a counter that was
	 * previously started.
	 * @return false only if a failure of some kind was reported in attempting to stop the counter.<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @see #prevResults
	 * @see #StartTestCase(String)
	 * @see #PrintTestCaseSummary(String)
	 */
	public static boolean StopTestCase(String tcname){
		return Counters.StopTestCase(tcname);
	}

	/**
	 * Convenience routine to retrieve the value of a SAFS Variable stored in SAFSVARS.
	 * <br>This will exploit the <a href="/sqabasic2000/CreateAppMap.htm#ddv_lookup" target="_blank">SAFSMAPS look-thru</a>
	 * and <a href="/sqabasic2000/TestDesignGuidelines.htm#AppMapChaining" target="_blank">app map chaining</a> mechanism.
	 * <br>That is, any variable that does NOT exist in SAFSVARS will be sought as an
	 * ApplicationConstant in the SAFSMAPS service.
	 * <p>
	 * See <a href="/sqabasic2000/TestDesignGuidelines.htm" target="_blank">Test Design Guidelines for Localization</a>.
	 * @param variableName
	 * @return String value, or an empty String.  Null if an Exception or Error was encountered.<p>
	 * Does not change prevResults.
	 * @see #prevResults
	 */
	public static String GetVariableValue(String variableName){
		return SAFSPlus.GetVariableValue(variableName);
	}

	/**
	 * Convenience routine to set the value of a SAFS Variable stored in SAFSVARS.<br>
	 * The act of logging success or failure will change prevResults.
	 * @param variableName -- Name of variable to set.
	 * @param variableValue -- value to store in variableName.
	 * @return true if successfully executed, false otherwise.<p>
	 * @see #prevResults
	 * @see Misc#SetVariableValues(String, String...)
	 * @see Misc#SetVariableValueEx(String, String)
	 */
	public static boolean SetVariableValue(String variableName, String variableValue){
		return SAFSPlus.SetVariableValue(variableName, variableValue);
	}

	/**
	 * Stop capturing test activity counts for a specific application feature or test-case if it is
	 * still active, then print a summary report of all tests counted, passed, failed, and skipped, etc...
	 * @param tcname The name of the test-case to start using a Counter on.  The name must match a counter
	 * that was previously started.
	 * @return false only if a failure of some kind was reported in attempting to stop the counter
	 * or print the summary report into the log.<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @see #prevResults
	 * @see #StartTestCase(String)
	 */
	public static boolean PrintTestCaseSummary(String tcname){
		return Counters.PrintTestCaseSummary(tcname);
	}

	/**
	 * Stop capturing test activity counts for the overall suite of tests if it is
	 * still active, then print a summary report of all counted, passed, failed, and skipped tests etc...
	 * @param suitename The name of the suite to stop (if still running) and process.
	 * The name must match a counter that was previously started.
	 * @return false only if a failure of some kind was reported in attempting to stop the counter
	 * or print the summary report into the log.<p>
	 * Sets prevResults TestRecordHelper to the results received or null if an error occurred.
	 * @see #prevResults
	 */
	public static boolean PrintTestSuiteSummary(String suitename){
		return Counters.PrintTestSuiteSummary(suitename);
	}

	/**
	 * Abort running test flow.
	 * Prints a detailed abort message to the log and throws a RuntimeException to abort the test run.
	 * @param reason will be prepended to the detailed abort information.<p>
	 * @example
	 * <pre>
	 * {@code
	 * AbortTest("reason for abort");
	 * }
	 * </pre>
	 * Clears prevResults TestRecordHelper to null.
	 * @see #prevResults
	 */
	public static void AbortTest(String reason) throws Throwable{
		SAFSPlus.AbortTest(reason);
	}

	/**
	 * Wrapper class to handle <a href="//sqabasic2000/WindowFunctionsIndex.htm">Window keywords</a>, like Maximize, Minimize, SetPosition etc.<br>
	 *
	 * @see SAFSPlus.Window
	 */
	public static class Window extends SAFSPlus.Window{}

	/**
	 * Wrapper class to handle
	 * <a href="/sqabasic2000/GenericMasterFunctionsIndex.htm">GenericMasterFunctions Reference</a> and
	 * <a href="/sqabasic2000/GenericObjectFunctionsIndex.htm">GenericObjectFunctions Reference</a>, like VerifyProperty, IsPropertyExist etc.<br>
	 *
	 * @see SAFSPlus.Component
	 */
	public static class Component extends SAFSPlus.Component{}

	/**
	 * Wrapper class providing APIs to handle <a href="/sqabasic2000/ComboBoxFunctionsIndex.htm">ComboBox keywords</a>, like Select, ShowList, SetTextValue etc.<br>
	 *
	 * @see SAFSPlus.ComboBox
	 */
	public static class ComboBox extends SAFSPlus.ComboBox{}
	/**
	 * Wrapper class providing APIs to handle <a href="/sqabasic2000/ScrollBarFunctionsIndex.htm">ScrollBar keywords</a>, like OneDown, PageDown, PageUp etc.<br>
	 *
	 * @see SAFSPlus.ScrollBar
	 */
	public static class ScrollBar extends SAFSPlus.ScrollBar{}
	/**
	 * A set of assertions methods for tests.  Only failed assertions are recorded.
	 *
	 * @see SAFSPlus.Assert
	 */
	public static class Assert extends SAFSPlus.Assert{}
	/**
	 * Wrapper class providing APIs to handle <a href="/sqabasic2000/CheckBoxFunctionsIndex.htm">CheckBox keywords</a>, like Check, UnCheck.<br>
	 *
	 * @see SAFSPlus.CheckBox
	 */
	public static class CheckBox extends SAFSPlus.CheckBox{}

	/**
	 * Wrapper class providing APIs to handle <a href="/sqabasic2000/EditBoxFunctionsIndex.htm">EditBox keywords</a>, like SetTextValue, SetTextCharacters etc.<br>
	 *
	 * @see SAFSPlus.EditBox
	 */
	public static class EditBox extends SAFSPlus.EditBox{}
	/**
	 * Wrapper class providing APIs to handle <a href="/sqabasic2000/TreeViewFunctionsIndex.htm">Tree keywords</a>, like ClickTextNode, ExpandTextNode etc.<br>
	 * <pre>
	 * By default, all parameters will be processed as an expression (math and string). As the parameter
	 * tree-path may contain separator "->", for example "Root->Child1->GrandChild", it will be evaluated
	 * and 0 will be returned as parameter, this is not expected by user. To avoid the evaluation of
	 * expression, PLEASE CALL
	 *
	 * {@code
	 * Misc.Expressions(false);
	 * }
	 * </pre>
	 *
	 * @see SAFSPlus.Tree
	 */
	public static class Tree extends SAFSPlus.Tree{}

	/**
	 * Wrapper class providing APIs to handle <a href="/sqabasic2000/TabControlFunctionsIndex.htm">TabControl keywords</a>, like ClickTab, SelectTabIndex etc.<br>
	 *
	 * @see SAFSPlus.TabControl
	 */
	public static class TabControl extends SAFSPlus.TabControl{}
	/**
	 * Wrapper class providing APIs to handle <a href="/sqabasic2000/ListViewFunctionsIndex.htm">ListView keywords</a>, like ClickIndex, VerifyListContains etc.<br>
	 *
	 * @see SAFSPlus.ListView
	 */
	public static class ListView extends SAFSPlus.ListView{}

	/**
	 * Wrapper class providing APIs to handle <a href="/sqabasic2000/JavaMenuFunctionsIndex.htm">MenuBar/Menu keywords</a>, like SelectMenuItem, VerifyMenuItemContains etc.<br>
	 *
	 * @see SAFSPlus.Menu
	 */
	public static class Menu extends SAFSPlus.Menu{}

	/**
	 * Convenience class for miscellaneous Driver Commands.<br>
	 * This is a sub-class of {@link SAFSPlus.Misc} and it provides more convenient wrapper APIs related to Selenium.<br>
	 * The class {@link SAFSPlus.Misc} is a sub-class of {@link DriverCommand}.<br>
	 *
	 * @see SAFSPlus.Misc
	 * @see SAFSPlus.DriverCommand
	 */
	public static class Misc extends SAFSPlus.Misc{
		//TODO: Once we implement the following keywords, we can move these methods to the super-class SAFSPlus.Misc
		//Which one should be considered as general keyword?
		//SetClickCapture, SetCheckAlertTimeout, GetCheckAlertTimeout
		//IsAlertPresent, AlertAccept, AlertDismiss
		/**
		 * Turn on/off 'click capture'.<br>
		 * <b>Note:</b> Calling this will not write success/failure message into the test log.
		 * <p>
		 * To make sure that 'click' action happens, we use 'ClickCapture' to monitor 'click event' of the target component.
		 * But sometimes for some reason, even click does happen, the 'ClickCapture' cannot receive the 'click event' and this will
		 * cause different kinds of troubles. At this situation, to avoid this problem 'click capture' could be turned off.<br>
		 * Later, we could always to turn on 'click capture' to guarantee that click happens.
		 * </p>
		 *
		 * @param on boolean, if true then the 'click capture' will be turned on; otherwise turned off.
		 * @return boolean, true if succeed.
		 */
		public static boolean SetClickCapture(boolean on){
			try{
				//TODO we could provide driver command 'SetClickCapture' for traditional SAFS users later.
				DocumentClickCapture.ENABLE_CLICK_CAPTURE = on;
				IndependantLog.debug(StringUtils.debugmsg(false)+" Set ClickCapture to "+on);
				return true;
			}catch(Exception e){
				IndependantLog.error(StringUtils.debugmsg(false)+" Fail to Set ClickCapture, due to "+StringUtils.debugmsg(e));
				return false;
			}
		}

		/**
		 * Test the presence of an Alert Dialog associated with a browser.<br>
		 * This command will wait 2 seconds by default for the presence of Alert.<br>
		 * @param optionals String
		 * <ul>
		 * <b>optionals[0] timeoutWaitAlertPresence</b> int, timeout in seconds to wait for the presence of Alert.
		 *                                                   If not provided, default is 2 seconds.<br>
		 * <b>optionals[0] browserID</b> String, the ID to get the browser on which the 'alert' will be closed.
		 *                                       If not provided, the current browser will be used.<br>
		 * </ul>
		 * @return boolean if the Alert exist
		 * @throws SeleniumPlusException if there is any un-expected error.
		 * @example
		 * <pre>
		 * {@code
		 * 1) boolean success = IsAlertPresent();//Test the presence of Alert (belongs to current browser) with 2 seconds timeout.
		 * 2) boolean success = IsAlertPresent("0");//Test the presence of Alert (belongs to current browser) immediately
		 * 3) boolean success = IsAlertPresent("5", "browser-id");//Test the presence of Alert (belongs to browser identified by "browser-id"),
		 *                                                        //before that it will wait 5 seconds for the presence of the Alert
		 * }
		 * @see SAFSPlus#StartWebBrowser(String, String, String...)
		 */
		public static boolean IsAlertPresent(String... optionals) throws SeleniumPlusException{
			try{
				//TODO we could provide driver command 'IsAlertPresent' for traditional SAFS users later.
				boolean success = WDLibrary.isAlertPresent(optionals);
				if(success){
					Logging.LogTestSuccess("An Alert dialog was present.");
				}else{
					Logging.LogTestSuccess("No Alert dialog was present.");
				}
				return success;
			}catch(Exception e){
				Logging.LogTestFailure("IsAlertPresent Failed.");
				if(e instanceof SeleniumPlusException) throw e;
				throw new SeleniumPlusException("IsAlertPresent Failed.", e);
			}
		}

		/**
		 * Accept (Clicking OK button) the Alert Dialog associated with a browser.<br>
		 * This command will wait 2 seconds by default for the presence of Alert.<br>
		 * @param optionals String
		 * <ul>
		 * <b>optionals[0] timeoutWaitAlertPresence</b> int, timeout in seconds to wait for the presence of Alert.
		 *                                                   If not provided, default is 2 seconds.<br>
		 * <b>optionals[0] browserID</b> String, the ID to get the browser on which the 'alert' will be closed.
		 *                                       If not provided, the current browser will be used.<br>
		 * </ul>
		 * @example
		 * <pre>
		 * {@code
		 * 1) boolean success = AlertAccept();//Close Alert (belongs to current browser) by clicking the OK button
		 * 2) boolean success = AlertAccept("5");//Close Alert (belongs to current browser) by clicking the OK button,
		 *                                       //before that it will wait 5 seconds for the presence of the Alert
		 * 3) boolean success = AlertAccept("5", "browser-id");//Close Alert (belongs to browser identified by "browser-id") by clicking the OK button,
		 *                                                     //before that it will wait 5 seconds for the presence of the Alert
		 * }
		 * @see SAFSPlus#StartWebBrowser(String, String, String...)
		 */
		public static boolean AlertAccept(String... optionals){
			try{
				//TODO we could provide driver command 'AlertAccept' for traditional SAFS users later.
				WDLibrary.closeAlert(true, optionals);
				Logging.LogTestSuccess("AlertAccept Succeeded.");
				return true;
			}catch(Exception e){
				Logging.LogTestFailure("AlertAccept Failed.");
				return false;
			}
		}

		/**
		 * Dismiss (Clicking Cancel button) the Alert Dialog associated with a browser.<br>
		 * This command will wait 2 seconds by default for the presence of Alert.<br>
		 * @param optionals String
		 * <ul>
		 * <b>optionals[0] timeoutWaitAlertPresence</b> int, timeout in seconds to wait for the presence of Alert.
		 *                                                   If not provided, default is 2 seconds.<br>
		 * <b>optionals[0] browserID</b> String, the ID to get the browser on which the 'alert' will be closed.
		 *                                       If not provided, the current browser will be used.<br>
		 * </ul>
		 * @example
		 * <pre>
		 * {@code
		 * 1) boolean success = AlertAccept();//Close Alert (belongs to current browser) by clicking the Cancel button
		 * 2) boolean success = AlertAccept("5");//Close Alert (belongs to current browser) by clicking the Cancel button,
		 *                                       //before that it will wait 5 seconds for the presence of the Alert
		 * 3) boolean success = AlertAccept("5", "browser-id");//Close Alert (belongs to browser identified by "browser-id") by clicking the Cancel button,
		 *                                                     //before that it will wait 5 seconds for the presence of the Alert
		 * }
		 * @see SAFSPlus#StartWebBrowser(String, String, String...)
		 */
		public static boolean AlertDismiss(String... optionals){
			try{
				//TODO we could provide driver command 'AlertDismiss' for traditional SAFS users later.
				WDLibrary.closeAlert(false, optionals);
				Logging.LogTestSuccess("AlertDismiss Succeeded.");
				return true;
			}catch(Exception e){
				Logging.LogTestFailure("AlertDismiss Failed.");
				return false;
			}
		}

		/**
		 * Set the 'Check Alert Timeout'. Usually, it is used to expand the time to wait for
		 * the 'Alert Box' popping up. By default, the 'Check Alert Timeout' is 0.
		 * @param seconds int, the value of time to set.
		 */
		public static void SetCheckAlertTimeout(int seconds){
			//TODO we could provide driver command 'SetCheckAlertTimeout' for traditional SAFS users later.
			IndependantLog.info("Set 'Check Alert Timeout as: '" + seconds);
			WDLibrary.setTimeoutCheckAlertForClick(seconds);
		}

		/**
		 * Get the current 'Check Alert Timeout' value.
		 */
		public static int GetCheckAlertTimeout(){
			//TODO we could provide driver command 'GetCheckAlertTimeout' for traditional SAFS users later.
			return WDLibrary.getTimeoutCheckAlertForClick();
		}
	}

	/**
	 * Wrapper class providing APIs to handle <a href="/sqabasic2000/DDDriverLogCommandsIndex.htm">Logging keywords</a>, like LogMessage, LogTestWarning etc.<br>
	 *
	 * @see SAFSPlus.Logging
	 */
	public static class Logging extends SAFSPlus.Logging{}

	/**
	 * Wrapper class providing APIs to handle <a href="/sqabasic2000/DDDriverFileCommandsIndex.htm">File keywords</a>, like OpenFile, ReadFileLine etc.<br>
	 * <pre>
	 * Convenience class for File handling Commands.
	 * If you meet some errors when calling these API, please try to run
	 * {@link Misc#Expressions(boolean)} to turn off the expression as
	 * Misc.Expressions(false);
	 * and then call the string method
	 * Files.xxx();
	 * </pre>
	 *
	 * @see SAFSPlus.Files
	 */
	public static class Files extends SAFSPlus.Files{}

	/**
	 * Wrapper class providing APIs to handle <a href="/sqabasic2000/DDDriverStringCommandsIndex.htm">String keywords</a>, like Compare, GetMultiDelimitedField etc.<br>
	 * <pre>
	 * Convenience class for String handling Commands.
	 * If you meet some errors when calling these API, please try to run
	 * {@link Misc#Expressions(boolean)} to turn off the expression as
	 * Misc.Expressions(false);
	 * and then call the string method
	 * Strings.xxx();
	 * </pre>
	 *
	 * @see SAFSPlus.Strings
	 */
	public static class Strings extends SAFSPlus.Strings{}

	/**
	 * Wrapper class providing APIs to handle <a href="/sqabasic2000/DDDriverCounterCommandsIndex.htm">DriverCounter keywords</a>, like StartTestSuite, StartCounter, LogCounterInfo etc.<br>
	 *
	 * @see SAFSPlus.Counters
	 */
	public static class Counters extends SAFSPlus.Counters{}

	/**
	 * Wrapper class providing APIs to handle
	 * <a href="/sqabasic2000/TIDRestFunctionsIndex.htm">TIDRestFunctions Reference</a> and
	 * <a href="/sqabasic2000/DDDriverRestCommandsIndex.htm">DriverRestCommands Reference</a>, like RestGetBinary, RestStoreResponse etc.<br>
	 *
	 * @see SAFSPlus.Rest
	 */
	public static class Rest extends SAFSPlus.Rest{}

	/**
	 * Click on any visible component without verification.<br>
	 * This API will not guarantee that the click does happen, it simply clicks. If user wants<br>
	 * to make sure of that, he can call {@link #Click(org.safs.model.Component, String...)} instead.<br>
	 * <br>
	 * @param comp -- Component (from App Map) to Click
	 * @param offset Point, the offset relative to the component to click
	 * @return true if successfully executed, false otherwise.<p>
	 * @example
	 * <pre>
	 * {@code
	 * 1) boolean success = ClickUnverified(Map.Google.Apps);//Click at the center
	 * 2) boolean success = ClickUnverified(Map.Google.Apps, new Point(20,20));//Click at the coordinate (20,20)
	 * }
	 *
	 * </pre>
	 * @see #Click(org.safs.model.Component, String...)
	 */
	public static boolean ClickUnverified(org.safs.model.Component comp, Point offset){
		boolean success = false;
		try {
			//TODO we could provide component command 'ClickUnverified' for traditional SAFS users later.
			WebElement we = getObject(comp);
			success = WDLibrary.clickUnverified(we, offset);
		} catch (SeleniumPlusException e) {
			IndependantLog.error(StringUtils.debugmsg(false)+" failed, due to "+StringUtils.debugmsg(e));
			success = false;
		}

		if(success) Logging.LogTestSuccess("ClickUnverified Succeeded on "+comp.getParentName()+":"+comp.getName()+" at "+offset);
		else Logging.LogTestFailure("ClickUnverified Failed on "+comp.getParentName()+":"+comp.getName()+" at "+offset);

		return success;
	}

	/**
	 * Retrieve a reference to the Selenium WebDriver object used by the currently active (last) session.
	 * @return The currently active (last) WebDriver object, or null if there isn't one.
	 */
	public static WebDriver WebDriver(){
		return WDLibrary.getWebDriver();
	}

	/**
	 * Following explanations come from <a href="http://selenium.googlecode.com/git/docs/api/java/org/openqa/selenium/JavascriptExecutor.html#executeAsyncScript-java.lang.String-java.lang.Object...-">Selenium Java Doc</a>.<br>
	 * Execute an asynchronous piece of JavaScript in the context of the currently selected frame or window.
	 * Unlike executing synchronous JavaScript, scripts executed with this method must explicitly signal they are finished by invoking the provided callback.
	 * This callback is always injected into the executed function as the last argument.<br>
	 * <br>
	 * The first argument passed to the callback function will be used as the script's result. This value will be handled as follows:<br>
	 * <ul>
	 * <li>For an HTML element, this method returns a WebElement
	 * <li>For a number, a Long is returned
	 * <li>For a boolean, a Boolean is returned
	 * <li>For all other cases, a String is returned.
	 * <li>For an array, return a List&lt;Object> with each object following the rules above. We support nested lists.
	 * <li>Unless the value is null or there is no return value, in which null is returned
	 * </ul>
	 *
	 * The default timeout for a script to be executed is 0ms. In most cases, including the examples below,
	 * one must set the script timeout WebDriver.Timeouts.setScriptTimeout(long, java.util.concurrent.TimeUnit) beforehand to a value sufficiently large enough.<br>
	 *
	 * @param script String, the script to execute
	 * @param scriptParams optional, Script arguments must be a number, a boolean, a String, WebElement, or a List of any combination of the above.
	 *                               An exception will be thrown if the arguments do not meet these criteria.
	 *                               The arguments will be made available to the JavaScript via the "arguments" variable.
	 * <ul>
	 * scriptParams[0] Object, Passed to the script as '<b>arguments[0]</b>', if used.<br>
	 * scriptParams[1] Object, Passed to the script as '<b>arguments[1]</b>', if used.<br>
	 * ... more script's parameter<br>
	 * </ul>
	 * @return Object or null.
	 * @example
	 * <pre>
	 * {@code
	 * //Example #1: Performing a sleep in the browser under test.
	 * long start = System.currentTimeMillis();
	 * String script = "window.setTimeout(arguments[arguments.length - 1], 500);";
	 * SeleniumPlus.executeAsyncScript(script);
	 * System.out.println("Elapsed time: " + System.currentTimeMillis() - start);
	 *
	 * //Example #2: Synchronizing a test with an AJAX application:
	 * Click(Map.Mail.ComposeButton);
	 * String script = "var callback = arguments[arguments.length - 1];" +
	 *                 "mailClient.getComposeWindowWidget().onload(callback);";
	 * Object result = SeleniumPlus.executeAsyncScript(script);
	 * Component.InputCharacters(Map.Mail.To, "bog@example.com");
	 *
	 * //Example #3: Injecting a XMLHttpRequest and waiting for the result:
	 * String script =  "var callback = arguments[arguments.length - 1];" +
	 *                  "var xhr = new XMLHttpRequest();" +
	 *                  "xhr.open('GET', '/resource/data.json', true);" +
	 *                  "xhr.onreadystatechange = function() {" +
	 *                  "  if (xhr.readyState == 4) {" +
	 *                  "    callback(xhr.responseText);" +
	 *                  "  }" +
	 *                  "};" +
	 *                  "xhr.send();";
	 * Object result = SeleniumPlus.executeAsyncScript(script);
	 * JsonObject json = new JsonParser().parse((String) response);
	 *
	 * }
	 * @see org.safs.selenium.webdriver.lib.WDLibrary#executeAsyncScript(String, Object...)
	 * @throws SeleniumPlusException
	 */
	public static Object executeAsyncScript(String script, Object... scriptParams) throws SeleniumPlusException{
		return WDLibrary.executeAsyncScript(script, scriptParams);
	}

	/**
	 * Following explanations come from <a href="http://selenium.googlecode.com/git/docs/api/java/org/openqa/selenium/JavascriptExecutor.html#executeScript-java.lang.String-java.lang.Object...-">Selenium Java Doc</a>.<br>
	 * Executes JavaScript synchronously in the context of the currently selected frame or window.
	 * The script fragment provided will be executed as the body of an anonymous function.<br>
	 * <br>
	 * Within the script, use document to refer to the current document.
	 * Note that local variables will not be available once the script has finished executing, though global variables will persist.<br>
	 * <br>
	 * If the script has a return value (i.e. if the script contains a return statement), then the following steps will be taken:
	 * <ul>
	 * <li>For an HTML element, this method returns a WebElement
	 * <li>For a decimal, a Double is returned
	 * <li>For a non-decimal number, a Long is returned
	 * <li>For a boolean, a Boolean is returned
	 * <li>For all other cases, a String is returned.
	 * <li>For an array, return a List&lt;Object> with each object following the rules above. We support nested lists.
	 * <li>Unless the value is null or there is no return value, in which null is returned
	 * </ul>
	 * @param script String, the script to execute
	 * @param scriptParams optional, Script arguments must be a number, a boolean, a String, WebElement, or a List of any combination of the above.
	 *                               An exception will be thrown if the arguments do not meet these criteria.
	 *                               The arguments will be made available to the JavaScript via the "arguments" variable.
	 * <ul>
	 * scriptParams[0] Object, Passed to the script as '<b>arguments[0]</b>', if used.<br>
	 * scriptParams[1] Object, Passed to the script as '<b>arguments[1]</b>', if used.<br>
	 * ... more script's parameter<br>
	 * </ul>
	 * @return Object or null.
	 * @example
	 * <pre>
	 * {@code
	 * //set "your text" to innerHTML of component Map.Google.SignIn
	 * WebElement we = SeleniumPlus.getObject(Map.Google.SignIn);
	 * String script = "arguments[0].innerHTML=arguments[1];";
	 * List<Object> params = new ArrayList<Object>();
	 * params.add(we);//arguments[0]
	 * params.add("your text");//arguments[1]
	 * SeleniumPlus.executeScript(script, params.toArray(new Object[0]));
	 *
	 * //get innerHTML of component Map.Google.SignIn
	 * script = "return arguments[0].innerHTML;";
	 * params.clear();
	 * params.add(we);//arguments[0]
	 * Object result = SeleniumPlus.executeScript(script, params.toArray(new Object[0]));
	 * }
	 * @see org.safs.selenium.webdriver.lib.WDLibrary#executeAsyncScript(String, Object...)
	 * @throws SeleniumPlusException
	 */
	public static Object executeScript(String script, Object... scriptParams) throws SeleniumPlusException{
		return WDLibrary.executeScript(script, scriptParams);
	}


	/**
	 * <pre>
	 * This class provides some static methods to set Selenium WebDriver's timeout thread-safely.
	 * </pre>
	 * <br>
	 * History:<br>
	 *  <br>   Jul 8, 2014    (Lei Wang) Initial release.
	 *  <br>   Jun 5, 2014    (Lei Wang) Add some non-thread safe method to set timeout for implicit-wait, page-load, and script.
	 */
	public static class WDTimeOut{
		private static final long TIMEOUT_WAIT_FOREVER = -1;

		private static final int TYPE_INVALID = -1;
		private static final int TYPE_IMPLICITLY_WAIT = 0;
		private static final int TYPE_ASYNCHRONOUS_JAVASCRIPT_WAIT = 1;
		private static final int TYPE_PAGELOAD_WAIT = 2;

		private static final String TYPE_INVALID_NAME = "invalid type";
		private static final String TYPE_IMPLICITLY_WAIT_NAME = "Implicit Wait";
		private static final String TYPE_ASYNCHRONOUS_JAVASCRIPT_WAIT_NAME = "Asynchronous JavaScript Wait";
		private static final String TYPE_PAGELOAD_WAIT_NAME = "Page Load Wait";

		/**
		 * The critical resource 'Timeouts' to protect as thread safe.<br>
		 * Note: User still can use SeleniumPlus.WebDriver().manage().timeouts() to set timeout outside,<br>
		 * and that will break the thread-safe!!!<br>
		 */
		private static Timeouts aTimeout = getWebDriverTimeouts();
		private static Timeouts getWebDriverTimeouts(){return SeleniumPlus.WebDriver().manage().timeouts();}


		/**
		 * Set the Web Driver's ImplicitlyWait timeout, the amount of time the driver should wait
		 * when searching for an element if it is not immediately present.<br>
		 * <b>Note:</b>This method is <b>not thread-safe</b>. If multiple threads call this method,
		 * the change of 'implicitlyWait' in one thread will affect the other thread.<br>
		 *
		 * @param timeout long, the timeout to set
		 * @param unit TimeUnit, the timeout's unit, seconds or milliseconds etc.
		 * @return Timeouts
		 * @see #setImplicitlyWait(long, TimeUnit)
		 * @see #setImplicitlyWait(long, TimeUnit, long)
		 */
		public static Timeouts implicitlyWait(long time, TimeUnit unit){
			Timeouts timeout = null;
			try{
				timeout = WDLibrary.getWebDriver().manage().timeouts();
				timeout.implicitlyWait(time, unit);
			}catch(Exception e){
				IndependantLog.warn(StringUtils.debugmsg(false)+StringUtils.debugmsg(e));
			}
			return timeout;
		}
		/**
		 * Set the Web Driver's pageLoad timeout, the amount of time to wait for a page load to complete
		 * before throwing an error. If the timeout is negative, page loads can be indefinite.<br>
		 * <b>Note:</b>This method is <b>not thread-safe</b>. If multiple threads call this method,
		 * the change of 'implicitlyWait' in one thread will affect the other thread.<br>
		 *
		 * @param timeout long, the timeout to set
		 * @param unit TimeUnit, the timeout's unit, seconds or milliseconds etc.
		 * @return Timeouts
		 * @see #setPageLoadTimeout(long, TimeUnit)
		 * @see #setPageLoadTimeout(long, TimeUnit, long)
		 */
		public static Timeouts pageLoadTimeout(long time, TimeUnit unit){
			Timeouts timeout = null;
			try{
				timeout = WDLibrary.getWebDriver().manage().timeouts();
				timeout.pageLoadTimeout(time, unit);
			}catch(Exception e){
				IndependantLog.warn(StringUtils.debugmsg(false)+StringUtils.debugmsg(e));
			}
			return timeout;
		}
		/**
		 * Set the Web Driver's Script timeout, the amount of time to wait for an asynchronous
		 * script to finish execution before throwing an error. If the timeout is negative,
		 * then the script will be allowed to run indefinitely.<br>
		 * <b>Note:</b>This method is <b>not thread-safe</b>. If multiple threads call this method,
		 * the change of 'implicitlyWait' in one thread will affect the other thread.<br>
		 *
		 * @param timeout long, the timeout to set
		 * @param unit TimeUnit, the timeout's unit, seconds or milliseconds etc.
		 * @return Timeouts
		 * @see #setScriptTimeout(long, TimeUnit)
		 * @see #setScriptTimeout(long, TimeUnit, long)
		 */
		public static Timeouts scriptTimeout(long time, TimeUnit unit){
			Timeouts timeout = null;
			try{
				timeout = WDLibrary.getWebDriver().manage().timeouts();
				timeout.setScriptTimeout(time, unit);
			}catch(Exception e){
				IndependantLog.warn(StringUtils.debugmsg(false)+StringUtils.debugmsg(e));
			}
			return timeout;
		}

		/**
		 * <pre>
		 * Set the 'Web Driver's ImplicitlyWait timeout' and lock the 'timeout setting'.
		 * This method is <b>thread-safe</b>. If one thread has called this method,
		 * it will set a lock and the other threads will have to wait until the first thread
		 * unlock by calling {@link #resetImplicitlyWait(long, TimeUnit)}.
		 * The lock range is within the caller method.
		 * See the following example:
		 * <code>
		 * try{
		 *   setImplicitlyWait(timeout, unit);
		 *   //doSomething();
		 * }catch(Exception e){
		 * }finally{
		 *   resetImplicitlyWait(originalTimeout, originalUnit);
		 * }
		 * </code>
		 * </pre>
		 * @param timeout long, the timeout to set
		 * @param unit TimeUnit, the timeout's unit, seconds or milliseconds etc.
		 * @return boolean, true if the timeout has been set successfully.
		 * @see #resetImplicitlyWait(long, TimeUnit)
		 */
		public static boolean setImplicitlyWait(long timeout, TimeUnit unit){
			String caller = StringUtils.getCallerID(true);
			return ImplicitWaitTimeOut.instance().setTimeout(caller, timeout, unit, TIMEOUT_WAIT_FOREVER);
		}
		/**
		 * Set the 'Web Driver's ImplicitlyWait timeout'<br>
		 * This method is <b>thread-safe</b>.
		 * To set 'timeout', thread needs to possess a lock. But if the lock is possessed by an other<br>
		 * thread, this thread must wait. If this thread doesn't want to wait for the lock for a too<br>
		 * time, this method provides a 'waitLockTimeout', after that timeout, this thread will quit.<br>
		 * The lock range is within the caller method.
		 * @param timeout long, the timeout to set
		 * @param unit TimeUnit, the timeout's unit, seconds or milliseconds etc.
		 * @param waitLockTimeout long, the timeout to wait for obtaining the lock, in milliseconds
		 * @return boolean, true if the timeout has been set successfully.
		 * @see #setImplicitlyWait(long, TimeUnit)
		 * @see #resetImplicitlyWait(long, TimeUnit)
		 */
		public static boolean setImplicitlyWait(long timeout, TimeUnit unit, long waitLockTimeout){
			String caller = StringUtils.getCallerID(true);
			return ImplicitWaitTimeOut.instance().setTimeout(caller, timeout, unit, waitLockTimeout);
		}
		/**
		 * Reset the 'Web Driver's ImplicitlyWait timeout' and unlock the 'timeout setting'.<br>
		 * @param timeout long, the timeout to set
		 * @param unit TimeUnit, the timeout's unit, seconds or milliseconds etc.
		 * @return boolean, true if the timeout has been reset successfully.
		 * @see #setImplicitlyWait(long, TimeUnit)
		 */
		public static boolean resetImplicitlyWait(long timeout, TimeUnit unit){
			String caller = StringUtils.getCallerID(true);
			return ImplicitWaitTimeOut.instance().resetTimeout(caller, timeout, unit);
		}
		/**
		 * <pre>
		 * Set the 'Web Driver's JavaScript Execution timeout' and lock the 'timeout setting'.
		 * This method is <b>thread-safe</b>. If one thread has called this method,
		 * it will set a lock and the other threads will have to wait until the first thread
		 * unlock by calling {@link #resetScriptTimeout(long, TimeUnit)}.
		 * The lock range is within the caller method.
		 * See the following example:
		 * <code>
		 * try{
		 *   setScriptTimeout(timeout, unit);
		 *   //doSomething();
		 * }catch(Exception e){
		 * }finally{
		 *   resetScriptTimeout(originalTimeout, originalUnit);
		 * }
		 * </code>
		 * </pre>
		 * @param timeout long, the timeout to set
		 * @param unit TimeUnit, the timeout's unit, seconds or milliseconds etc.
		 * @return boolean, true if the timeout has been set successfully.
		 * @see #resetScriptTimeout(long, TimeUnit)
		 */
		public static boolean setScriptTimeout(long timeout, TimeUnit unit){
			String caller = StringUtils.getCallerID(true);
			return AnsynScriptTimeOut.instance().setTimeout(caller, timeout, unit, TIMEOUT_WAIT_FOREVER);
		}
		/**
		 * Set the 'Web Driver's JavaScript Execution timeout'<br>
		 * This method is <b>thread-safe</b>.
		 * To set 'timeout', thread needs to possess a lock. But if the lock is possessed by an other<br>
		 * thread, this thread must wait. If this thread doesn't want to wait for the lock for a too<br>
		 * time, this method provides a 'waitLockTimeout', after that timeout, this thread will quit.<br>
		 * The lock range is within the caller method.
		 * @param timeout long, the timeout to set
		 * @param unit TimeUnit, the timeout's unit, seconds or milliseconds etc.
		 * @param waitLockTimeout long, the timeout to wait for obtaining the lock, in milliseconds
		 * @return boolean, true if the timeout has been set successfully.
		 * @see #setScriptTimeout(long, TimeUnit)
		 * @see #resetScriptTimeout(long, TimeUnit)
		 */
		public static boolean setScriptTimeout(long timeout, TimeUnit unit, long waitLockTimeout){
			String caller = StringUtils.getCallerID(true);
			return AnsynScriptTimeOut.instance().setTimeout(caller, timeout, unit, waitLockTimeout);
		}
		/**
		 * Reset the 'Web Driver's JavaScript Execution timeout' and unlock the 'timeout setting'.<br>
		 * @param timeout long, the timeout to set
		 * @param unit TimeUnit, the timeout's unit, seconds or milliseconds etc.
		 * @return boolean, true if the timeout has been reset successfully.
		 * @see #setScriptTimeout(long, TimeUnit)
		 */
		public static boolean resetScriptTimeout(long timeout, TimeUnit unit){
			String caller = StringUtils.getCallerID(true);
			return AnsynScriptTimeOut.instance().resetTimeout(caller, timeout, unit);
		}

		/**
		 * <pre>
		 * Set the 'Web Driver's PageLoad timeout' and lock the 'timeout setting'.
		 * This method is <b>thread-safe</b>. If one thread has called this method,
		 * it will set a lock and the other threads will have to wait until the first thread
		 * unlock by calling {@link #resetPageLoadTimeout(long, TimeUnit)}.
		 * The lock range is within the caller method.
		 * See the following example:
		 * <code>
		 * try{
		 *   setPageLoadTimeout(timeout, unit);
		 *   //doSomething();
		 * }catch(Exception e){
		 * }finally{
		 *   resetPageLoadTimeout(originalTimeout, originalUnit);
		 * }
		 * </code>
		 * </pre>
		 * @param timeout long, the timeout to set
		 * @param unit TimeUnit, the timeout's unit, seconds or milliseconds etc.
		 * @return boolean, true if the timeout has been set successfully.
		 * @see #resetPageLoadTimeout(long, TimeUnit)
		 */
		public static boolean setPageLoadTimeout(long timeout, TimeUnit unit){
			String caller = StringUtils.getCallerID(true);
			return PageLoadTimeOut.instance().setTimeout(caller, timeout, unit, TIMEOUT_WAIT_FOREVER);
		}
		/**
		 * Set the 'Web Driver's PageLoad timeout'<br>
		 * This method is <b>thread-safe</b>.
		 * To set 'timeout', thread needs to possess a lock. But if the lock is possessed by an other<br>
		 * thread, this thread must wait. If this thread doesn't want to wait for the lock for a too<br>
		 * time, this method provides a 'waitLockTimeout', after that timeout, this thread will quit.<br>
		 * The lock range is within the caller method.
		 * @param timeout long, the timeout to set
		 * @param unit TimeUnit, the timeout's unit, seconds or milliseconds etc.
		 * @param waitLockTimeout long, the timeout to wait for obtaining the lock, in milliseconds
		 * @return boolean, true if the timeout has been set successfully.
		 * @see #setPageLoadTimeout(long, TimeUnit)
		 * @see #resetPageLoadTimeout(long, TimeUnit)
		 */
		public static boolean setPageLoadTimeout(long timeout, TimeUnit unit, long waitLockTimeout){
			String caller = StringUtils.getCallerID(true);
			return PageLoadTimeOut.instance().setTimeout(caller, timeout, unit, waitLockTimeout);
		}

		/**
		 * Reset the 'Web Driver's PageLoad timeout' and unlock the 'timeout setting'.<br>
		 * @param timeout long, the timeout to set
		 * @param unit TimeUnit, the timeout's unit, seconds or milliseconds etc.
		 * @return boolean, true if the timeout has been reset successfully.
		 * @see #setPageLoadTimeout(long, TimeUnit)
		 */
		public static boolean resetPageLoadTimeout(long timeout, TimeUnit unit){
			String caller = StringUtils.getCallerID(true);
			return PageLoadTimeOut.instance().resetTimeout(caller, timeout, unit);
		}

		/**
		 * @param type int, the type of the timeout
		 * @return String, the string name for a certain type of timeout
		 */
		private static String name(int type){
			switch(type){
			case TYPE_IMPLICITLY_WAIT: return TYPE_IMPLICITLY_WAIT_NAME;
			case TYPE_ASYNCHRONOUS_JAVASCRIPT_WAIT: return TYPE_ASYNCHRONOUS_JAVASCRIPT_WAIT_NAME;
			case TYPE_PAGELOAD_WAIT: return TYPE_PAGELOAD_WAIT_NAME;
			default: return TYPE_INVALID_NAME;
			}
		}

		/**
		 * <pre>
		 * There is a 'timeout' property, and it will be shared by multiple threads. We want it to be
		 * thread-safe, which means that if one thread possesses a lock and sets a value to 'timeout',
		 * the other threads will not be able to set a value to 'timeout' until the first thread finishes
		 * using the 'timeout' and explicitly release the lock.
		 * </pre>
		 */
		private interface ITimeOut{
			/**
			 * Wait for a lock to set the property 'timeout'.
			 * @param lockID String, the lock's ID, which defines the range that 'timeout' property can be shared.
			 *                       For example,
			 *                       if the id is provided as Thread.currentThread().getId(), then 'timeout'
			 *                       property can be shared in within current thread no matter which method the current
			 *                       thread is executing;
			 *                       if the id is provided as caller-method-full-qualified-name+Thread.currentThread().getId(),
			 *                       then timeout-property's share range will be limited to caller method of current thread.
			 * @param timeout long, the value to set to 'timeout' property
			 * @param unit TimeUnit, the unit to measure 'timeout'
			 * @param waitLockTimeout long, the timeout to wait for the lock. If timeout is reached and the lock has not
			 *                              been obtained, then stop waiting and return false.
			 * @return boolean, true if the lock has been obtained and timeout has been set.
			 */
			boolean setTimeout(String lockID, long timeout, TimeUnit unit, long waitLockTimeout);
			/**
			 * Reset the property 'timeout' and release the lock if the caller is the owner of the lock.
			 * @param lockID String, the lock's ID, which is used to verify the owner of the lock
			 * @param timeout long, the original timeout to set back to property 'timeout'
			 * @param unit TimeUnit, the unit to measure 'timeout'
			 * @return boolean, true if the lock has been released and timeout has been reset.
			 */
			boolean resetTimeout(String lockID, long timeout, TimeUnit unit);
		}
		/**
		 * A singleton for setting timeout of 'Implicit Wait'.
		 */
		private static class ImplicitWaitTimeOut extends ThreadSafeTimeOut implements ITimeOut{
			private static ITimeOut instance = new ImplicitWaitTimeOut();
			private ImplicitWaitTimeOut(){}

			public static ITimeOut instance(){ return instance;}

			@Override
			public boolean setTimeout(String lockID, long timeout, TimeUnit unit, long waitLockTimeout) {
				return setTimeout(lockID, timeout, unit, TYPE_IMPLICITLY_WAIT, waitLockTimeout);
			}

			@Override
			public boolean resetTimeout(String lockID, long timeout, TimeUnit unit) {
				return resetTimeout(lockID, timeout, unit, TYPE_IMPLICITLY_WAIT);
			}
		}
		/**
		 * A singleton for setting timeout of 'Asynchronous JavaScript Execution'.
		 */
		private static class AnsynScriptTimeOut extends ThreadSafeTimeOut implements ITimeOut{
			private static ITimeOut instance = new AnsynScriptTimeOut();
			private AnsynScriptTimeOut(){}

			public static ITimeOut instance(){ return instance;}

			@Override
			public boolean setTimeout(String lockID, long timeout, TimeUnit unit, long waitLockTimeout) {
				return setTimeout(lockID, timeout, unit, TYPE_ASYNCHRONOUS_JAVASCRIPT_WAIT, waitLockTimeout);
			}

			@Override
			public boolean resetTimeout(String lockID, long timeout, TimeUnit unit) {
				return resetTimeout(lockID, timeout, unit, TYPE_ASYNCHRONOUS_JAVASCRIPT_WAIT);
			}
		}
		/**
		 * A singleton for setting timeout of 'Page Load'.
		 */
		private static class PageLoadTimeOut extends ThreadSafeTimeOut implements ITimeOut{
			private static ITimeOut instance = new PageLoadTimeOut();
			private PageLoadTimeOut(){}

			public static ITimeOut instance(){ return instance;}

			@Override
			public boolean setTimeout(String lockID, long timeout, TimeUnit unit, long waitLockTimeout) {
				return setTimeout(lockID, timeout, unit, TYPE_PAGELOAD_WAIT, waitLockTimeout);
			}

			@Override
			public boolean resetTimeout(String lockID, long timeout, TimeUnit unit) {
				return resetTimeout(lockID, timeout, unit, TYPE_PAGELOAD_WAIT);
			}
		}

		private abstract static class ThreadSafeTimeOut{
			private boolean locked = false;
			private String owner = null;
			private int timeoutType = TYPE_INVALID;

			private ThreadSafeTimeOut(){}

			/**
			 * <pre>
			 * Set the 'Web Driver's timeout' and lock the 'timeout setting'.
			 * This method is thread-safe. If one thread has called this method,
			 * it will set a lock and the other threads will have to wait until the first thread
			 * unlock by calling {@link #resetTimeout(String, long, TimeUnit, int)}.
			 * <font color='red'>NOTE: User must call {@link #resetTimeout(String, long, TimeUnit, int)} after calling this method.</font>
			 * See the following example:
			 * <code>
			 * try{
			 *   setTimeout(lockID, timeout, unit, type);
			 *   //doSomething();
			 * }catch(Exception e){
			 * }finally{
			 *   resetTimeout(lockID, originalTimeout, originalUnit, type);
			 * }
			 * </code>
			 * </pre>
			 * @param lockID String, the lock's ID.
			 * @param timeout long, the timeout to set
			 * @param unit TimeUnit, the timeout's unit, seconds or milliseconds etc.
			 * @param type int, the timeout's type, 'implicit wait' or 'asynchronous javascript execution' or 'page load'
			 * @param waitLockTimeout long, the time in milliseconds to wait for the release of a lock possessed by another owner(lockID, type)
			 * @return boolean, true if the timeout has been set successfully.
			 * @see #resetTimeout(String, long, TimeUnit, int)
			 */
			protected synchronized boolean setTimeout(String lockID, long timeout, TimeUnit unit, int type, long waitLockTimeout){
				String debugmsg = StringUtils.debugmsg(false);

				IndependantLog.info(debugmsg+"Se+ '"+ lockID +"' seeking '"+name(type)+"' lock object...");
				boolean firstloop = true;
				try {
					while(locked){
						//if the owner(lockID, type) want to set the timeout again, it is permitted.
						if(lockID.equals(owner) && (type==timeoutType)) break;
						//for others, they have to wait.
						if(firstloop) {
							firstloop = false;
							IndependantLog.info(debugmsg+"waiting for owner '"+ owner +"' to release the '"+ name(type)+"' lock object...");
						}
						if(waitLockTimeout==TIMEOUT_WAIT_FOREVER) wait();
						else{
							wait(waitLockTimeout);
							if(locked){//waitLockTimeout reached, we haven't obtain the lock, we will quit
								IndependantLog.debug(debugmsg+"The wait-timeout for '"+name(type)+"' lock has been reached! Cannot obtain lock, returning false.");
								return false;
							}
						}
					}
					locked = true;
					owner = lockID;
					timeoutType = type;
					int retry = 0;;
					while(++retry < 3){
						try{
							switch(type){
							case TYPE_IMPLICITLY_WAIT:
								aTimeout.implicitlyWait(timeout, unit);
								break;
							case TYPE_ASYNCHRONOUS_JAVASCRIPT_WAIT:
								aTimeout.setScriptTimeout(timeout, unit);
								break;
							case TYPE_PAGELOAD_WAIT:
								aTimeout.pageLoadTimeout(timeout, unit);
								break;
							}
							return true;
						}catch(WebDriverException nf){
							IndependantLog.warn(debugmsg+"Failed to set tiemout for '"+name(type)+"' by owner '"+ owner +"'. Due to '"+nf.toString()+"'\n"
									+ "Retrieving new WebDriver Session WDTimeouts ...");
							aTimeout = getWebDriverTimeouts();
						}
					}
				} catch (Throwable th) {
					IndependantLog.error(debugmsg+"Failed to set timeout for '"+name(type)+"' by owner '"+owner+"'. Due to Exception "+StringUtils.debugmsg(th)+"\n"
							+ "Reset '"+name(type)+"' to original value.");
					try{
						//Reset the original timeout value
						int retry = 0;
						while(++retry < 3 ){
							try{
								switch(type){
								case TYPE_IMPLICITLY_WAIT:
									aTimeout.implicitlyWait(Processor.getSecsWaitForWindow(), TimeUnit.SECONDS);
									break;
								case TYPE_ASYNCHRONOUS_JAVASCRIPT_WAIT:
									//TODO What is the original timeout value?
									//aTimeout.setScriptTimeout(timeout, unit);
									break;
								case TYPE_PAGELOAD_WAIT:
									//TODO What is the original timeout value?
									//aTimeout.pageLoadTimeout(timeout, unit);
									break;
								}
								return false;
							}catch(WebDriverException nf){
								IndependantLog.warn(debugmsg+"Failed to reset tiemout for '"+name(type)+"' by owner '"+ owner +"'. Due to '"+nf.toString()+"'\n"
										+ "Retrieving new WebDriver Session WDTimeouts ...");
								aTimeout = getWebDriverTimeouts();
							}
						}
					} catch (Throwable ignore) {}
				}
				return false;
			}

			/**
			 * Reset the 'Web Driver's timeout' and unlock the 'timeout setting'.<br>
			 * @param lockID String, the lock's ID.
			 * @param timeout long, the timeout to set
			 * @param unit TimeUnit, the timeout's unit, seconds or milliseconds etc.
			 * @param type int, the timeout's type, 'implicit wait' or 'asynchronous javascript execution' or 'page load'
			 * @return boolean, true if the timeout has been reset successfully.
			 * @see #setTimeout(String, long, TimeUnit, int)
			 */
			protected synchronized boolean resetTimeout(String lockID, long timeout, TimeUnit unit, int type){
				String debugmsg = StringUtils.debugmsg(false);
				//Only the OWNER of the lock can reset the timeout of the SAME type
				if(locked && lockID.equals(owner) && (timeoutType==type)){
					locked = false;
					owner = null;
					timeoutType = TYPE_INVALID;

					try{
						int retry = 0;
						while(++retry < 3){
							try{
								switch(type){
								case TYPE_IMPLICITLY_WAIT:
									aTimeout.implicitlyWait(timeout, unit);
									break;
								case TYPE_ASYNCHRONOUS_JAVASCRIPT_WAIT:
									aTimeout.setScriptTimeout(timeout, unit);
									break;
								case TYPE_PAGELOAD_WAIT:
									aTimeout.pageLoadTimeout(timeout, unit);
									break;
								}
							}catch(WebDriverException nf){
								IndependantLog.warn(debugmsg+"Failed to set tiemout for '"+name(type)+"' by owner '"+ owner +"'. Due to '"+nf.toString()+"'\n"
										+ "Retrieving new WebDriver Session WDTimeouts.");
								aTimeout = getWebDriverTimeouts();
							}
						}
					}catch(Throwable th){
						IndependantLog.warn(debugmsg+"Failed to reset timeout for '"+name(type)+"' by owner '"+owner+"'. Due to Exception "+StringUtils.debugmsg(th));
						return false;
					}finally{
						//after reset the timeout, notify the other threads
						notifyAll();
					}
					return true;
				}else{
					IndependantLog.warn(debugmsg+"Cannot reset timeout for '"+name(type)+"' of WebDriver. Because you are not the owner '"+owner+"'. Call setXXXTimeout() firstly.");
					return false;
				}
			}
		}
	}

	/**
	 * Internal framework use only.
	 * Main inherited by subclasses is required.
	 * Subclasses should not override this main method.
	 * <p>
	 * Any subclass specific initialization should be done in the default no-arg constructor
	 * for the subclass.  That Constructor will be instantiated and invoked automatically by
	 * this main startup method.
	 * <p>
	 * By default will seek an AppMap.order file.  However, the user can specify an alternate
	 * AppMap order file by using the following JVM argument:
	 * <p>
	 * <ul>Examples:
	 * <p>
	 * <li>-Dtestdesigner.appmap.order=AppMap_en.order, or
	 * <li>-Dtestdesigner.appmap.order=AppMap_ja.order, or
	 * <li>-Dtestdesigner.appmap.order=AppMap_win.order, or
	 * <li>-Dtestdesigner.appmap.order=AppMap_mac.order
	 * <li>etc...
	 * </ul>
	 * <p>By default, the Browser Type that will be used is FireFox.  The user can specify a
	 * different default browser by using the following JVM argument {@link SelectBrowser#SYSTEM_PROPERTY_BROWSER_NAME}:
	 * <p>
	 * <ul>
	 * -DBROWSER=explorer<br>
	 * -DBROWSER=chrome<br>
	 * -DBROWSER=firefox<br>
	 * etc...<br>
	 * </ul>
	 * <p>By default, the RemoteServer that will be used is at host and port:  localhost:4444 .
	 * The user can specify a different host and/or port (grid) by using the JVM arguments
	 * {@link SelectBrowser#SYSTEM_PROPERTY_SELENIUM_HOST} and {@link SelectBrowser#SYSTEM_PROPERTY_SELENIUM_PORT}:
	 * <p>
	 * <ul>
	 * -Dselenium.host=localhost  or<br>
	 * -Dselenium.host=L12345.company.com<br>
	 * -Dselenium.port=5555<br>
	 * </ul>
	 * <p>By default, a Debug Log is usually enabled and named in the test configuration (INI) file.
	 * The user can specify or override the name of this debug log file by using the following JVM argument:
	 * <p>
	 * <ul>
	 * -Dtestdesigner.debuglogname=mydebuglog.txt
	 * </ul>
	 * <p>
	 * @param args --
	 * <p>
	 * -safsvar:name=value
	 * <p><ul>
	 * (Any number of these can be provided to preset variables or override App Map Constants.
	 *  Note the entire argument must be enclosed in quotes if there are spaces in it.)
	 * <pre>
	 * -safsvar:platform=win8 -safsvar:browserType=firefox "-safsvar:spacedpath=C:\Project With Spaces\Special Directory"</pre>
	 * </ul>
	 * -autorun -- Perform Dependency Injection, AutoConfig, and AutoExecution if "-autorun" is provided.
	 * <p>
	 * -autorunclass -- followed by the class that is requesting the automatic configuration and execution, only take effect when parameter '-autorun' is present.
	 * <ul>-autorunclass autorun.full.classname</ul>
	 * <p>
	 * -junit:classname -- perform a JUnit test instead of executing runTest() in the SeleniumPlus subclass.<br>
	 * The normal SeleniumPlus bootstrap process and initialization is performed prior to executing the JUnit test.
	 * <ul>-junit:com.sas.spock.tests.SpockExperiment</ul>
	 * <p>
	 * @see org.safs.model.annotations.AutoConfigureJSAFS
	 * @see org.safs.model.annotations.JSAFSBefore
	 * @see org.safs.model.annotations.JSAFSAfter
	 * @see org.safs.model.annotations.JSAFSTest
	 * @see org.safs.model.annotations.InjectJSAFS
	 */
	public static void main(String[] args) {
		SAFSPlus.main(args);
	}
}
