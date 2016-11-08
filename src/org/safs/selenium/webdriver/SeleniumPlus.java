/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver;
/**
 * Logs for developers, not published to API DOC.
 * History:<br>
 * 
 *  <br>   NOV 19, 2013    (CANAGL) Initial release.
 *  <br>   DEC 18, 2013    (SBJLWA) Update to support ComboBox.
 *  <br>   JAN 08, 2014    (DHARMESH) Update to support EditBox.
 *  <br>   JAN 16, 2014    (DHARMESH) Update start/stop browser call.
 *  <br>   JAN 26, 2014    (SBJLWA) Add method combineParams().
 *                                  Modify API ComboBox.CaptureItemsToFile(): show the required parameter filename.
 *                                  Add keyword Misc.Expressions().
 *  <br>   FEB 02, 2014	   (DHARMESH) Add Resize and Maximize WebBrowser window KW.   
 *  <br>   MAR 05, 2014    (SBJLWA) Implement click-related keywords.        
 *  <br>   APR 15, 2014    (DHARMESH) Added HighLight keyword                     
 *  <br>   APR 22, 2014    (SBJLWA) Implement keywords of TabControl.        
 *  <br>   APR 29, 2014    (SBJLWA) Add the ability of execution of javascript;
 *  <br>   MAY 13, 2014    (DHARMESH) Add Component class and update other component references; 
 *  <br>   JUL 08, 2014    (SBJLWA) Add inner class WDTimeOut to provide methods setting WebDriver's timeout thread-safely.
 *  <br>   NOV 10, 2014    (SBJLWA) Add support for Strings.
 *  <br>   NOV 19, 2014    (SBJLWA) Add support for Files.
 *  <br>   NOV 21, 2014    (Dharmesh/CANAGL) Update ExecuteScript Javadoc.
 *  <br>   NOV 25, 2014    (SBJLWA) Add support for Misc.
 *  <br>   DEC 19, 2014    (SBJLWA) Adjust some method qualifiers:
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
 *  <br>   JAN 04, 2015    (SBJLWA) Add LeftDrag and ScrollWheel.
 *  <br>   JAN 15, 2015    (SBJLWA) Add mouse-drag-related keywords.
 *  <br>   FEB 26, 2015    (SBJLWA) Add method GetGUIImage to filter children of type org.safs.model.Component.
 *                                  Modify Misc.GetAppMapValue(), SeleniumPlus.getObject(): don't log message.
 *  <br>   JUN 11, 2015    (SBJLWA) Modify Misc.SetVariableValueEx(), SetVariableValues(): resolve DDVariable if EXPRESSIONS is off.
 *                                  Add Misc.ResolveExpression().
 *  <br>   JUN 15, 2015    (DHARMESH4) Added SendMail support.
 *  <br>   JUN 23, 2015    (SBJLWA) Modify for test auto run.
 *  <br>   JUN 24, 2015	   (SCNTAX) Add Misc.WaitForPropertyValue() and Misc.WaitForPropertyValueGone() keywords.
 *  <br>   JUL 07, 2015    (SCNTAX) Add EditBox.SetTextCharacters(), EditBox.SetUnverifiedTextCharacters() and EditBox.SetUnverifiedTextValue() keywords.
 *  <br>                            Change comments of EditBox.SetTextValue(). 
 *  <br>   JUL 24, 2015    (SBJLWA) Add GetURL, SaveURLToFile, VerifyURLContent, VerifyURLToFile in Misc.
 *  <br>   AUG 17, 2015    (DHARMESH4) Add SetFocus call in Window.
 *  <br>   AUG 20, 2015    (CANAGL) Document -Dtestdesigner.debuglogname support in main().
 *  <br>   SEP 07, 2015    (SBJLWA) Add method DragTo(): parameter 'offset' will also support pixel format; 
 *                                                       optional parameter 'FromSubItem' and 'ToSubItem' are not supported yet.
 *  <br>   JAN 07, 2016    (CANAGL) Make System.exit() optional and allowExit=false, by default.
 *  <br>   MAR 02, 2016    (SBJLWA) Add Misc.AlertAccept(), Misc.AlertDismiss() and ClickUnverified().
 *  <br>   MAR 07, 2016    (SBJLWA) Add example for StartWebBrowser() with preference settings for "chrome" and "firefox".
 *  <br>   MAR 14, 2016    (SBJLWA) Add IsAlertPresent().
 *  <br>   MAR 24, 2016    (SBJLWA) Modify comments for StartWebBrowser(): adjust examples and add links to specify "custom profile" and "preferences".
 *  <br>   MAR 31, 2016    (SBJLWA) Add IsComponentExists(), OnGUIXXXBlockID().
 *                                  Modify testStatusCode(): the status code BRANCH_TO_BLOCKID will be considered successful execution.
 *  <br>   APR 19, 2016    (SBJLWA) Modify comments/examples for Click() CtrlClick() ShiftClick() etc.: Handle the optional parameter 'autoscroll'.
 *  <br>   MAY 17, 2016    (CANAGL) Add support for -junit:classname command-line parameter.
 */
import java.awt.Point;
import java.awt.Rectangle;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.SessionNotFoundException;
import org.safs.IndependantLog;
import org.safs.Processor;
import org.safs.SAFSPlus;
import org.safs.StringUtils;
import org.safs.model.tools.EmbeddedHookDriverRunner;
import org.safs.selenium.webdriver.lib.SelectBrowser;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;

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
 * <Font color="red">NOTE 3: <a href="http://safsdev.sourceforge.net/sqabasic2000/UsingDDVariables.htm">DDVariable</a></Font>
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
 * @author CANAGL
 */
public abstract class SeleniumPlus extends SAFSPlus{
	/**
	 * The Runner object providing access to the underlying Selenium Engine.
	 * This is the main object subclasses would use to execute SeleniumPlus actions and commands 
	 * and to gain references to more complex services like the running JSAFSDriver or the 
	 * Selenium WebDriver object(s).
	 */
	public static final EmbeddedHookDriverRunner Runner = new EmbeddedHookDriverRunner(EmbeddedSeleniumHookDriver.class);

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
	 * Retrieve a reference to the Selenium WebDriver object used by the currently active (last) session.
	 * @return The currently active (last) WebDriver object, or null if there isn't one.
	 */
	public static WebDriver WebDriver(){
		return WDLibrary.getWebDriver();
	}
	
	/**
	 * Start WebBrowser
	 * See <a href="http://safsdev.sourceforge.net/sqabasic2000/DDDriverCommandsReference.htm#detail_StartWebBrowser">Detailed Reference</a>	
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
	 * {@link SelectBrowser#getExtraParameterKeys()}<br>
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
	 * For the detail explanation of starting browser with "custom profile" and/or "preferences", please visit the section "<font color="red">Start Browser</font>" at <a href="http://safsdev.sourceforge.net/selenium/doc/SeleniumPlus-Welcome.html">Selenium Welcome Document</a>. 
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
	 *                                                        
	 * //Start chrome browser with default data pool (chrome://version/, see "Profile Path") , and using the last-used user. 
	 * String datapool = "C:\\Users\\xxx\\AppData\\Local\\Google\\Chrome\\User Data";
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_CHROME, 
	 *                                                        "10", 
	 *                                                        "true", 
	 *                                                        quote(SelectBrowser.KEY_CHROME_USER_DATA_DIR), 
	 *                                                        datapool
	 *                                                        });
	 * //Start chrome browser with default data pool (chrome://version/, see "Profile Path") , and using the default user. 
	 * String datapool = "C:\\Users\\xxx\\AppData\\Local\\Google\\Chrome\\User Data";
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
	 * //Start chrome browser with some options to be turned off. 
	 * String optionsToExclude = "disable-component-update";//comma separated options to exclude, like "disable-component-update, ignore-certificate-errors", be careful, there are NO 2 hyphens before options.                                                      
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_CHROME, 
	 *                                                        "10", 
	 *                                                        "true", 
	 *                                                        quote(SelectBrowser.KEY_CHROME_EXCLUDE_OPTIONS), 
	 *                                                        quote(optionsToExclude)
	 *                                                        });
	 * //Start chrome browser with some chrome-command-line-options/preferences to set. 
	 * String absolutePreferenceFile = "c:\\chromePref.json.dat";//A json file containing chrome command-line-options/preferences, like { "lang":"zh-cn", "start-maximized":"",  "<b>seplus.chrome.preference.json.key</b>":{ "intl.accept_languages":"zh-CN-pseudo", "intl.charset_default"  :"utf-8"} }
	 * StartWebBrowser("http://www.google.com", "GoogleMain", new String[]{
	 *                                                        SelectBrowser.BROWSER_NAME_CHROME, 
	 *                                                        "10", 
	 *                                                        "true", 
	 *                                                        quote(SelectBrowser.KEY_CHROME_PREFERENCE), 
	 *                                                        quote(absolutePreferenceFile)
	 *                                                        });
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
	 * Switch WebBrowser by ID. 
	 * During test, multiple browsers can be opened by {@link #StartWebBrowser(String, int, String...)}<br>
	 * If user wants to switch between these opened browser, use can call this method.<br>
	 * This method requires a parameter 'ID', which is given by user when he calls {@link #StartWebBrowser(String, int, String...)}<br>
	 * See <a href="http://safsdev.sourceforge.net/sqabasic2000/DDDriverCommandsReference.htm#detail_UseWebBrowser">Detailed Reference</a>	
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
	 * See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_TypeKeys">Detailed Reference</a>
	 * <p>
	 * This supports special key characters like:
	 * <p><pre>
	 *     {Enter} = ENTER Key
	 *     {Tab} = TAB Key
	 *     ^ = CONTROL Key with another key ( "^s" = CONTROL + s )
	 *     % = ALT Key with another key ( "%F" = ALT + F )
	 *     + = SHIFT Key with another key ( "+{Enter}" = SHIFT + ENTER )  
	 * </pre>
	 * We are generally providing this support through our generic <a href="http://safsdev.sourceforge.net/doc/org/safs/tools/input/CreateUnicodeMap.html">InputKeys Support</a>.
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
		return Component.TypeKeys(keystrokes);
	}
	
	/**
	 * Sends characters to the current focused Component.<br>
	 * See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_TypeChars">Detailed Reference</a>
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
		return Component.TypeChars(textvalue);
	}
	
	/**
	 * Sends secret-text (such as password) to the current focused Component.<br>
	 * See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_TypeEncryption">Detailed Reference</a>
	 * <p>
	 * @param encryptedDataFile String, the file containing 'encrypted data' to send to the current focused Component.
	 * @param privateKeyFile String, the file containing 'private key' to decrypt the 'encrypted data'
	 * @return true on success
	 * @see org.safs.robot.Robot#inputChars(String)
	 * @example	 
	 * <pre>
	 * {@code
	 * //the publickey and privatekey are generated by org.safs.RSA
	 * //C:\safs\passwords\encrypted.pass contained the encrypted-data (by public key)
	 * SeleniumPlus.TypeEncryption("C:\safs\passwords\encrypted.pass", "D:\secretPath\private.key" );
	 * }
	 * </pre>
	 */
	public static boolean TypeEncryption(String encryptedDataFile, String privateKeyFile){
		return Component.TypeEncryption(encryptedDataFile, privateKeyFile);
	}
	
	/**
	 * Hover the mouse over a specified screen location.
	 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_HoverScreenLocation">Detailed Reference</a>
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
	 * Verify the current contents of a binary (image) file with a benchmark file.
	 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyBinaryFileToFile">Detailed Reference</a>
	 * @param benchFile String, File used as the comparison benchmark.
	 * @param actualFile String, File used as the comparison file under test.
	 * @param optionals
	 * <ul>
	 * <b>optionals[0] FilterMode</b> String, one of FileUtilities.FilterMode. FilterMode.TOLERANCE is valid only when the binary files are images.<br>
	 * <b>optionals[1] FilterOptions</b> int, if the FilterMode is FilterMode.TOLERANCE, a number between 0 and 100, 
	 *                                        the percentage of bits need to be the same.
	 *                                        100 means only 100% match, 2 images will be considered matched;
	 *                                        0 means even no bits match, 2 images will be considered matched.<br>
	 *                                   other type, if the FilterMode is FilterMode.XXX<br>
	 * </ul>
	 * @return true if the 2 files contain the same content, false otherwise.
	 * @example	 
	 * <pre>
	 * {@code
	 * boolean success = SeleniumPlus.VerifyBinaryFileToFile("signIn.png", "signIn.png");
	 * boolean success = SeleniumPlus.VerifyBinaryFileToFile("c:\bench\signIn.png", "d:\test\signIn.png");
	 * boolean success = SeleniumPlus.VerifyBinaryFileToFile("c:\bench\signIn.png", "d:\test\signIn.png", FilterMode.TOLERANCE.name, "90");
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
	 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyFileToFile">Detailed Reference</a>
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
	 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyTextFileToFile">Detailed Reference</a>
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
	 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyValueContains">Detailed Reference</a>
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
	 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyValueContainsIgnoreCase">Detailed Reference</a>
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
	 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyValueDoesNotContain">Detailed Reference</a>
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
	 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyValues">Detailed Reference</a>
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
	 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyValuesIgnoreCase">Detailed Reference</a>
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
	 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyValuesNotEqual">Detailed Reference</a>
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
	 * Click on any visible component. 
	 * <p>See <a href="http://safsdev.github.io/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_Click">Detailed Reference</a>
	 * @param comp -- Component (from App Map) to Click
	 * @param params optional
	 * <ul>
	 * <li><b>params[0] X,Y coordinate</b>. Example: "50,50", or "AppMapSubkey" defined in App Map<br>
	 * <b>For SE+</b>, params[0] X,Y coordinate also support percentage format for X, or Y. Example: "20%,30%", "20%,30", "20,30%".<br>
	 * <li><b>params[1] autocroll boolean</b> if the component will be scrolled into view automatically before clicking.
	 *                                        if not provided, the default value is true.
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
	 * 5) boolean success = Click(Map.Google.Apps,"20,20", "false");//Click at the coordinate (20,20), and web-element will not be scrolled into view automatically
	 *  // one of the above and then,
	 * int rc = prevResults.getStatusCode();      // if useful
	 * String info = prevResults.getStatusInfo(); // if useful
	 * }
	 * 
	 * Pay attention: If you use percentage format in SE+, you'd better use 'Misc.Expressions(false);' first.
	 * 
	 * "AppMapSubkey" is expected to be an AppMap entry in an "Apps" section in the App Map.
	 * See <a href="http://safsdev.github.io/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_Click">Detailed Reference</a>
	 * </pre>	 
	 * @see #prevResults
	 * @see org.safs.TestRecordHelper#getStatusCode()
	 * @see org.safs.TestRecordHelper#getStatusInfo()
	 */
	public static boolean Click(org.safs.model.Component comp, String... params){
		return Component.Click(comp, params);
	}
	
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
	 * Control-Click on any visible component. 
	 * <p>See <a href="http://safsdev.github.io/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_CtrlClick">Detailed Reference</a>
	 * @param comp -- Component (from App Map) to Click
	 * @param params optional
	 * <ul>
	 * <li><b>params[0] X,Y coordinate</b>. Example: "50,50", or "AppMapSubkey" defined in App Map<br>
	 * <b>For SE+</b>, params[0] X,Y coordinate also support percentage format for X, or Y. Example: "20%,30%", "20%,30", "20,30%".<br>
	 * <li><b>params[1] autocroll boolean</b> if the component will be scrolled into view automatically before clicking.
	 *                                        if not provided, the default value is true.
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
	 * <p>See <a href="http://safsdev.github.io/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_CtrlRightClick">Detailed Reference</a>
	 * @param comp -- Component (from App Map) to Click
	 * @param params optional
	 * <ul>
	 * <li><b>params[0] X,Y coordinate</b>. Example: "50,50", or "AppMapSubkey" defined in App Map<br>
	 * <b>For SE+</b>, params[0] X,Y coordinate also support percentage format for X, or Y. Example: "20%,30%", "20%,30", "20,30%".<br>
	 * <li><b>params[1] autocroll boolean</b> if the component will be scrolled into view automatically before clicking.
	 *                                        if not provided, the default value is true.
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
	 * <p>See <a href="http://safsdev.github.io/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_DoubleClick">Detailed Reference</a>
	 * @param comp -- Component (from App Map) to Click
	 * @param params optional
	 * <ul>
	 * <li><b>params[0] X,Y coordinate</b>. Example: "50,50", or "AppMapSubkey" defined in App Map<br>
	 * <b>For SE+,</b> params[0] X,Y coordinate also support percentage format for X, or Y. Example: "20%,30%", "20%,30", "20,30%".<br>
	 * <li><b>params[1] autocroll boolean</b> if the component will be scrolled into view automatically before clicking.
	 *                                        if not provided, the default value is true.
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
	 * <p>See <a href="http://safsdev.github.io/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_RightClick">Detailed Reference</a>
	 * @param comp -- Component (from App Map) to Click
	 * @param params optional
	 * <ul>
	 * <li><b>params[0] X,Y coordinate</b>. Example: "50,50", or "AppMapSubkey" defined in App Map<br>
	 * <b>For SE+</b>, params[0] X,Y coordinate also support percentage format for X, or Y. Example: "20%,30%", "20%,30", "20,30%".<br>
	 * <li><b>params[1] autocroll boolean</b> if the component will be scrolled into view automatically before clicking.
	 *                                        if not provided, the default value is true.
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
	 * A left mouse drag is performed on the object based on the stored coordinates relative to this object. 
	 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericObjectFunctionsReference.htm#detail_LeftDrag">Detailed Reference</a>
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
	 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericObjectFunctionsReference.htm#detail_ShiftLeftDrag">Detailed Reference</a>
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
	 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericObjectFunctionsReference.htm#detail_CtrlShiftLeftDrag">Detailed Reference</a>
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
	 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericObjectFunctionsReference.htm#detail_CtrlLeftDrag">Detailed Reference</a>
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
	 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericObjectFunctionsReference.htm#detail_AltLeftDrag">Detailed Reference</a>
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
	 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericObjectFunctionsReference.htm#detail_CtrlAltLeftDrag">Detailed Reference</a>
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
	 * <p>See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericObjectFunctionsReference.htm#detail_RightDrag">Detailed Reference</a>
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
	 * Shift-Click on any visible component. 
	 * <p>See <a href="http://safsdev.github.io/sqabasic2000/SeleniumGenericObjectFunctionsReference.htm#detail_ShiftClick">Detailed Reference</a>
	 * @param comp -- Component (from App Map) to Click
	 * @param params optional
	 * <ul>
	 * <li><b>params[0] X,Y coordinate</b>. Example: "50,50", or "AppMapSubkey" defined in App Map<br>
	 * <b>For SE+</b>, params[0] X,Y coordinate also support percentage format for X, or Y. Example: "20%,30%", "20%,30", "20,30%".<br>
	 * <li><b>params[1] autocroll boolean</b> if the component will be scrolled into view automatically before clicking.
	 *                                        if not provided, the default value is true.
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
	 * Highlight object
	 * @param OnOff -- true or false for object highlight 
	 * @return true on success
	 */
	public static boolean Highlight(boolean OnOff){
		return DriverCommand.Highlight(OnOff);
	}
	
	/**
	 * Pause testcase flow in seconds. If you want to pause in millisecond, use {@link Misc#Delay(int)}.
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
	 * See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_GetGUIImage">Detailed Reference</a><p>
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
	 * See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_VerifyGUIImageToFile">Detailed Reference</a><p>
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
	 * See <a href="http://safsdev.sourceforge.net/sqabasic2000/GenericMasterFunctionsReference.htm#detail_ExecuteScript">Detailed Reference</a><p>
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
		return WDLibrary.getObject(sc, rs);
	}
	
	/**
	 * Wait for object in seconds
	 * @param comp -- Component (from generated Map.java)
	 * @param time - time in second
	 * @return
	 * @example	 
	 * <pre>
	 * {@code
	 * WaitForGUI(Map.Google.SignIn,10);
	 * }
	 * </pre>
	 */
	public static boolean WaitForGUI(org.safs.model.Component comp, long time){		
		return DriverCommand.WaitForGUI(comp, time);
	}
	
	/**
	 * Start capturing test activity counts for a specific application feature or testcase.
	 * @param tcname The name of the testcase to start using a Counter on.
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
	 * Stop capturing test activity counts for a specific application feature or testcase.
	 * @param tcname The name of the testcase to stop.  The name must match a counter that was 
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
	 * <br>This will exploit the <a href="http://safsdev.sourceforge.net/sqabasic2000/CreateAppMap.htm#ddv_lookup" target="_blank">SAFSMAPS look-thru</a> 
	 * and <a href="http://safsdev.sourceforge.net/sqabasic2000/TestDesignGuidelines.htm#AppMapChaining" target="_blank">app map chaining</a> mechanism.  
	 * <br>That is, any variable that does NOT exist in SAFSVARS will be sought as an 
	 * ApplicationConstant in the SAFSMAPS service.
	 * <p>
	 * See <a href="http://safsdev.sourceforge.net/sqabasic2000/TestDesignGuidelines.htm" target="_blank">Test Design Guidelines for Localization</a>.
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
	 * Stop capturing test activity counts for a specific application feature or testcase if it is 
	 * still active, then print a summary report of all tests counted, passed, failed, and skipped, etc...
	 * @param tcname The name of the testcase to start using a Counter on.  The name must match a counter 
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
	 * Wrapper class to handle <a href="http://safsdev.github.io//sqabasic2000/WindowFunctionsIndex.htm">Window keywords</a>, like Maximize, Minimize, SetPosition etc.<br>
	 * For detail APIs, please refer to {@link SAFSPlus.Window}.<br>
	 * 
	 * @see SAFSPlus.Window
	 */
	public static class Window extends SAFSPlus.Window{}
	
	/**
	 * Wrapper class to handle 
	 * <a href="http://safsdev.github.io/sqabasic2000/GenericMasterFunctionsIndex.htm">GenericMasterFunctions Reference</a> and 
	 * <a href="http://safsdev.github.io/sqabasic2000/GenericObjectFunctionsIndex.htm">GenericObjectFunctions Reference</a>, like VerifyProperty, IsPropertyExist etc.<br>
	 * For detail APIs, please refer to {@link SAFSPlus.Component}.<br>
	 * 
	 * @see SAFSPlus.Component
	 */
	public static class Component extends SAFSPlus.Component{}
	
	/**
	 * The wrapper providing APIs to operate ComboBox in SeleniumPlus.<br>
	 */
	public static class ComboBox extends SAFSPlus.ComboBox{}
	/**
	 * The wrapper providing APIs to operate ScrollBar in SeleniumPlus.<br>
	 */
	public static class ScrollBar extends SAFSPlus.ScrollBar{}
	/**
	 * A set of assertions methods for tests.  Only failed assertions are recorded.  
	 */
	public static class Assert extends SAFSPlus.Assert{}
	/**
	 * The wrapper providing APIs to operate CheckBox in SeleniumPlus.<br>
	 */
	public static class CheckBox extends SAFSPlus.CheckBox{}
	
	/**
	 * The wrapper providing APIs to operate EditBox in SeleniumPlus.<br>
	 */
	public static class EditBox extends SAFSPlus.EditBox{}
	/**
	 * The wrapper providing APIs to operate Tree in SeleniumPlus.<br>
	 * <pre>
	 * By default, all parameters will be processed as an expression (math and string). As the parameter
	 * treepath may contain separator "->", for example "Root->Child1->GrandChild", it will be evaluated 
	 * and 0 will be returned as parameter, this is not expected by user. To avoid the evaluation of
	 * expression, PLEASE CALL
	 * 
	 * {@code
	 * Misc.Expressions(false);
	 * }
	 * </pre>
	 */
	public static class Tree extends SAFSPlus.Tree{}
	
	/**
	 * The wrapper providing APIs to operate TabControl in SeleniumPlus.<br>
	 */
	public static class TabControl extends SAFSPlus.TabControl{}
	/**
	 * The wrapper providing APIs to operate ListView in SeleniumPlus.<br>
	 */
	public static class ListView extends SAFSPlus.ListView{}
	
	/**
	 * The wrapper providing APIs to operate MenuBar/Menu in SeleniumPlus.<br>
	 */	
	public static class Menu extends SAFSPlus.Menu{}
	
	/**
	 * Convenience class for miscellaneous Driver Commands. 
	 */
	public static class Misc extends SAFSPlus.Misc{}
	
	/**
	 * Convenience class for Logging Commands. 
	 */
	public static class Logging extends SAFSPlus.Logging{}
	
	/**
	 * <pre>
	 * Convenience class for File handling Commands. 
	 * If you meet some errors when calling these API, please try to run 
	 * {@link Misc#Expressions(boolean)} to turn off the expression as
	 * Misc.Expressions(false);
	 * and then call the string method
	 * Files.xxx();
	 * </pre>
	 */
	public static class Files extends SAFSPlus.Files{}
	
	/**
	 * <pre>
	 * Convenience class for String handling Commands.
	 * If you meet some errors when calling these API, please try to run 
	 * {@link Misc#Expressions(boolean)} to turn off the expression as
	 * Misc.Expressions(false);
	 * and then call the string method
	 * Strings.xxx();
	 * </pre>
	 */
	public static class Strings extends SAFSPlus.Strings{}
	
	/**
	 * Convenience class for Counter Commands.
	 * 
	 * @see SAFSPlus.Counters
	 */
	public static class Counters extends SAFSPlus.Counters{}
	
	/**
	 * <pre>
	 * This class provides some static methods to set Selenium WebDriver's timeout thread-safely.
	 * </pre>
	 * <br>
	 * History:<br>
	 *  <br>   Jul 8, 2014    (sbjlwa) Initial release.
	 *  <br>   Jun 5, 2014    (sbjlwa) Add some non-thread safe method to set timeout for implicit-wait, page-load, and script.
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
			 *                       then timeout-property's sahre range will be limited to caller method of current thread.
			 * @param timeout long, the value to set to 'timeout' property
			 * @param unit TimeUnit, the unit to measure 'timeout'
			 * @param waitLockTimeout long, the timeout to wait for the lock. If timeout is reached and the lock has not
			 *                              been obtained, then stop waitting and return false.
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

			public boolean setTimeout(String lockID, long timeout, TimeUnit unit, long waitLockTimeout) {
				return setTimeout(lockID, timeout, unit, TYPE_IMPLICITLY_WAIT, waitLockTimeout);
			}

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

			public boolean setTimeout(String lockID, long timeout, TimeUnit unit, long waitLockTimeout) {
				return setTimeout(lockID, timeout, unit, TYPE_ASYNCHRONOUS_JAVASCRIPT_WAIT, waitLockTimeout);
			}

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

			public boolean setTimeout(String lockID, long timeout, TimeUnit unit, long waitLockTimeout) {
				return setTimeout(lockID, timeout, unit, TYPE_PAGELOAD_WAIT, waitLockTimeout);
			}

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
				IndependantLog.info("Se+ '"+ lockID +"' seeking '"+name(type)+"' lock object...");
				boolean firstloop = true;
				try {
					while(locked){
						//if the owner(lockID, type) want to set the timeout again, it is permitted.
						if(lockID.equals(owner) && (type==timeoutType)) break;
						//for others, they have to wait.
						if(firstloop) {
							firstloop = false;
							IndependantLog.info("Se+ waiting for owner '"+ owner +"' to release the '"+ name(type)+"' lock object...");						
						}
						if(waitLockTimeout==TIMEOUT_WAIT_FOREVER) wait();
						else{
							wait(waitLockTimeout);
							if(locked){//waitLockTimeout reached, we haven't obtain the lock, we will quit
								IndependantLog.debug("The wait timeout for '"+name(type)+"' lock has been reached! Cannot obtain lock, returning false.");
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
						}catch(SessionNotFoundException nf){
							IndependantLog.error("WDTimeout WebDriver SessionNotFoundException setTimeout for '"+name(type)+"' by '"+ owner +"'. Retrieving new WebDriver Session WDTimeouts");
							aTimeout = getWebDriverTimeouts();
						}
					}
				} catch (Throwable th) {
					IndependantLog.error("Fail to set timeout for '"+name(type)+"' by '"+ owner +"' of WebDriver.", th);
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
									//						aTimeout.setScriptTimeout(timeout, unit);
									break;
								case TYPE_PAGELOAD_WAIT:
									//TODO What is the original timeout value?
									//						aTimeout.pageLoadTimeout(timeout, unit);
									break;
								}
								return false;
							}catch(SessionNotFoundException nf){
								IndependantLog.error("WDTimeout WebDriver SessionNotFoundException resetting setTimeout for '"+name(type)+"' by '"+ owner +"'. Retrieving new WebDriver Session WDTimeouts");
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
							}catch(SessionNotFoundException nf){
								IndependantLog.error("WDTimeout WebDriver SessionNotFoundException resetTimeout for '"+name(type)+"' by '"+ owner +"'. Retrieving new WebDriver Session WDTimeouts");
								aTimeout = getWebDriverTimeouts();
							}
						}
					}catch(Throwable th){
						IndependantLog.warn("Fail to reset timeout for '"+name(type)+"' of WebDriver. Due to Exception "+StringUtils.debugmsg(th));
						return false;
					}finally{
						//after reset the timeout, notify the other threads
						notifyAll();
					}
					return true;
				}else{
					IndependantLog.warn("Cannot reset timeout for '"+name(type)+"' of WebDriver. Because you are not the owner. Call setXXXTimeout() firstly.");
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
	 * -autorunclass -- followed by the class that is requesting the automatic configuration and execution, only take effect when paraemter '-autorun' is present.
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
