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
*
* History:<br>
*
* NOV 19, 2013    (Carl Nagle) Initial release.
* DEC 18, 2013    (Lei Wang) Add codes to support ComboBox (HTML tag &lt;select&gt;).
* DEC 26, 2013    (Lei Wang) Move ComboBox class out; add methods useBrowser() and stopBrowser().
*                                  Modify startBrowser() to permit passing more browser parameters, such as proxy settings.
* JAN 16, 2014    (DHARMESH) Updated reconnection browser support.
* FEB 02, 2014	   (DHARMESH) Add Resize and Maximize WebBrowser window KW.
* MAR 05, 2014    (Lei Wang) Add some methods related to mouse-click (based on webdriver API).
* MAR 27, 2014    (Lei Wang) Get element's screen location and use SAFS-Robot to do click-related keywords firstly.
* APR 15, 2014    (Lei Wang) Listen to the 'mousedown' event for detecting if the click/double has happened.
* APR 15, 2014    (DHARMESH) Add HighLight keyword.
* AUG 29, 2014    (DHARMESH) Add selenium grid host and port support.
* DEC 02, 2014    (Carl Nagle) Fix SeBuilder Script imports to use UTF-8 Character Encoding.
* DEC 09, 2014    (Lei Wang) Modify isVisible(): catch the un-expected exception.
* JAN 06, 2015    (Lei Wang) Add method leftDrag().
* JAN 15, 2015    (Lei Wang) Add methods xxxDrag().
* JAN 20, 2015    (DHARMESH) Add mobile support.
* APR 16, 2015    (Carl Nagle) Try to fix Selenium Keyboard Actions in inputKeysSAFS2Selenium.
* APR 29, 2015    (Lei Wang) Modify inputKeys/inputChars: if webelement cannot be focused, just log a warning message.
*                                  instead of throwing an Exception.
* MAY 29, 2015    (Carl Nagle) Add support for setDelayBetweenKeystrokes
* JUN 05, 2015    (Lei Wang) Add checkBeforeOperation(): check if element is stale or invisible before clicking.
*                                  Add isDisplayed(): not like isVisible(), it will not check the value of attribute 'visibility'.
*                                  Modify isVisible(), isStale(): set implicitWait directly, don't wait.
* JUN 14, 2015    (Lei Wang) Modify focus(): if the component is "EditBox", use Robot Click to set focus.
* JUN 15, 2015    (Carl Nagle) Add isPointInBounds to account for points that might be on the width & height edge.
* JUL 24, 2015    (Lei Wang) Create class WD_XMLHttpRequest and its static instance AJAX.
* JUL 25, 2015    (Lei Wang) Modify windowSetFocus(): remove the unnecessary parameter element.
*  <br>	  AUG 08, 2015    (Dharmesh) Added delayWaitReady for WaitOnClick.
* SEP 07, 2015    (Lei Wang) Add method getElementOffsetScreenLocation().
* OCT 12, 2015    (Lei Wang) Modify method getProperty(): get property by native SAP method.
* OCT 30, 2015    (Lei Wang) Move method isVisible(), isDisplayed and isStale() to SearchObject class.
* NOV 20, 2015    (Lei Wang) Add a unit test for "WDLibrary.AJAX.getURL".
* NOV 26, 2015    (Lei Wang) Move some content from getScreenLocation() to getLocation().
* DEC 02, 2015    (Lei Wang) Modify getLocation(): modify to get more accurate location.
* DEC 03, 2015    (Lei Wang) Move "code of fixing browser client area offset problem" from getLocation() to getScreenLocation().
* DEC 10, 2015    (Lei Wang) Add methods to handle clipboard on local machine or on RMI server machine.
* DEC 24, 2015    (Lei Wang) Add methods to get browser's name, version, and selenium-server's version etc.
*                                  Add method checkKnownIssue().
* FEB 05, 2016    (Lei Wang) Add method killChromeDriver().
* FEB 26, 2016    (Lei Wang) Modify click(), doubleClick(): if the offset is out of element's boundary, disable the click listener.
* FEB 29, 2016    (Lei Wang) Modify checkOffset(): if the offset is out of element's boundary, use the whole document as click event receiver.
* MAR 02, 2016    (Lei Wang) Add clickUnverified(), closeAlert().
*                                  Add class RBT: To encapsulate the local Robot and Robot RMI agent.
*                                  Modify click() and doubleClick(): use RBT to do the click action.
* MAR 14, 2016    (Lei Wang) Add isAlertPresent(), waitAlert().
* MAR 29, 2016    (Lei Wang) Modify click() and doubleClick(): detect "Alert" after clicking.
* APR 19, 2016    (Lei Wang) Modify click() doubleClick() etc.: Handle the optional parameter 'autoscroll'.
* APR 27, 2016    (Lei Wang) Added switchWindow(): switch to a certain window according to its title.
* MAY 05, 2016    (Lei Wang) Modified startBrowser(): restart browser if the connection between WebDriver and BrowserDriver is not good.
* MAY 22, 2017    (Lei Wang) Modified startBrowser(): use isValidBrowserID() instead of StringUtils.isValid() to check the validity of parameter browser ID.
* JUN 06, 2017    (Lei Wang) Modified focus(): catch exception separately so that each way will be tried to set focus.
* AUG 22, 2017    (Lei Wang) Added stopSeleniumServer(): clean up the SeleniumPlus testing environment.
* SEP 19, 2017    (Lei Wang) Added method keyPress, keyRelease, inputKeys and inputChars to class RBT.
* NOV 01, 2017    (Lei Wang) Modified showOnPage(): add Actions(driver).moveToElement() to show an element.
*                                  Modified click(), doubleClick(), clickUnverified(): call showOnPage() before clicking by Robot.
* NOV 01, 2017    (Lei Wang) Added method isTopLeftInBounds(): (0, 0) should be considered in bounds.
*                                  Changed method's name isPointInBounds() to isBottomRightInBounds().
* NOV 15, 2017    (Lei Wang) Added comments for method inputKeysSAFS2Selenium(): It doesn't work well with the Firefox 55/56 and geckodriver 0.18.0/0.19.0.
* NOV 24, 2017    (Lei Wang) Modified showOnPage(): add optional parameter 'verify' and 'refresh'.
*                                  Modified click related method to call showOnPage() with optional parameter 'verify' and 'refresh'.
* NOV 28, 2017    (Lei Wang) Added comments for method inputKeysSAFS2Selenium(): It doesn't work well with the IE 11 and IEDriverServer 3.4.0
* DEC 06, 2017    (Lei Wang) Added handleBasicAuthentication(): input 'user', 'password' and '{Enter}' with a certain delay.
* DEC 06, 2017    (Lei Wang) Modified getElementOffsetScreenLocation(): Use the default offset (center) to calculate the location if we meet an exception.
* DEC 04, 2018    (Carl Nagle) Modified focus(): Changed to not setFocus on the topmost frame window *IF* SearchObject’s bypassFramesReset is true.
* DEC 21, 2018    (Lei Wang) Handle the 'bypass Robot' for some methods:
*                                  1. Supported by selenium: inputKeys, inputChars, mouseHover, altLeftDrag, ctrlAltLeftDrag, ctrlLeftDrag, ctrlShiftLeftDrag, shiftLeftDrag, leftDrag
*                                  2. Not supported by selenium: mouseWheel, setDelayBetweenKeystrokes, setWaitReaction, clearClipboard, setClipboard, getClipboard, rightDrag
*                           Modified class RBT: throw SeleniumPlusException if 'bypass Robot' is true.
* DEC 25, 2018    (Lei Wang) Modified method setWaitReaction(): log message instead of throwing out the exception when 'bypass Robot' is true.
*                           Modified method inputChars(): WebElement.sendKeys() doesn't work, use Actions to input chars.
* DEC 26, 2018    (Lei Wang) Modified method inputChars(): Use both WebElement.sendKeys() and Actions.sendKeys() to input chars.
*                           Modified method inputKeys(): Still use robot to handle "{Esc}"/"{Escape}" even 'bypass Robot' is true. (Selenium cannot work)
* DEC 29, 2018    (Lei Wang) Modified method keyRelease(), keyRelease(): Handle them by both Robot and Selenium.
*                           Moved method mouseWheel() to RBT class, added a private mouseWheel() to send "MouseWheel Event" to DOM element by javascript.
* JAN 11, 2019    (Carl Nagle)  Modified getLocation() to show results of Coordinates.onPage() and Element.getLocation()
* JUN 04, 2019   (Lei Wang)  Modified startBrowser(): support 'setNetworkConditions' for chrome browser.
*                           Added deleteNetworkConditions(), setNetworkConditions() and getNetworkConditions().
* JUN 21, 2019   (Lei Wang)  Overload method setNetworkConditions(): provide the version with parameter WebDriver.
* AUG 20, 2019   (Lei Wang)  Modified method checkKnownIssue(): check known issue with chrome 76.0.3809.100
* SEP 19, 2019   (Lei Wang)  Moved getProperty(), getProperties() and getCssValues() to the parent class SearchObject.
* SEP 25, 2019   (Lei Wang)  Modified isShowOnPage(): if we meet the StaleElementReferenceException, we will try to reset to the 'lastFrame'.
* OCT 17, 2019   (Lei Wang)  Modified click(): if the web-element is not visible, we will wait for a while.
* NOV 21, 2019   (Lei Wang)  Modified dragTo(), rightDrag(): give the implementation in selenium way.
*                           Added moveToElement(): try to fix the in-consistency in W3C implementation, we need to set isW3Cimplementation to true to get this fix.
* MAR 28, 2020   (Lei Wang)  Supported killProcess(), killExtraProcess(), killXXXDriver() in Linux.
* APR 27, 2020   (Lei Wang)  Added loadModHeaderProfile(), modified startBrowser(): load the extension ModHeader's profile.
* MAY 20, 2020   (Lei Wang)  Modified getScreenLocation(): If the selenium can return screen coordinates, then return it directly. Added more log messages.
* OCT 12, 2020   (Lei Wang)  Modified killGeckoDriver(): kill both "geckodriver" and "geckodriver_64" processes, we don't know if 32 or 64 bit driver is running.
* APR 27, 2021   (Lei Wang) Moved some third-party-jar-independent-methods to 'org.safs.UtilsIndependent'.
*
*
*/
package org.safs.selenium.webdriver.lib;

import static org.openqa.selenium.interactions.PointerInput.Kind.MOUSE;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.PointerInput.MouseButton;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.safs.Constants;
import org.safs.IndependantLog;
import org.safs.Processor;
import org.safs.SAFSException;
import org.safs.StringUtils;
import org.safs.Utils;
import org.safs.UtilsIndependent;
import org.safs.android.auto.lib.Console;
import org.safs.image.ImageUtils;
import org.safs.model.commands.DDDriverCommands;
import org.safs.natives.NativeWrapper;
import org.safs.net.IHttpRequest.Key;
import org.safs.net.XMLHttpRequest;
import org.safs.robot.Robot;
import org.safs.selenium.util.AbstractDriverUpdater;
import org.safs.selenium.util.DocumentClickCapture;
import org.safs.selenium.util.JavaScriptFunctions;
import org.safs.selenium.util.MouseEvent;
import org.safs.selenium.webdriver.CFComponent;
import org.safs.selenium.webdriver.CFEditBox;
import org.safs.selenium.webdriver.SeleniumPlus.WDTimeOut;
import org.safs.selenium.webdriver.WebDriverGUIUtilities;
import org.safs.selenium.webdriver.lib.interpreter.WDScriptFactory;
import org.safs.selenium.webdriver.lib.interpreter.WDTestRunFactory;
import org.safs.selenium.webdriver.lib.model.Element;
import org.safs.sockets.DebugListener;
import org.safs.text.FileUtilities;
import org.safs.text.INIFileReader;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.GenericProcessMonitor.ProcessInfo;
import org.safs.tools.drivers.DriverConstant.SeleniumConfigConstant;
import org.safs.tools.input.CreateUnicodeMap;
import org.safs.tools.input.InputKeysParser;
import org.safs.tools.input.RobotKeyEvent;
import org.safs.tools.stringutils.StringUtilities;

import com.google.common.collect.ImmutableMap;
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.factory.StepTypeFactory;
import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;

/**
 *  <br>   NOV 19, 2013    (Carl Nagle) Initial release.
 */
public class WDLibrary extends SearchObject {

	/** {@link InputEvent#BUTTON1_MASK} */
    public static final int MOUSE_BUTTON_LEFT = InputEvent.BUTTON1_MASK;
    /** {@link InputEvent#BUTTON2_MASK} */
    public static final int MOUSE_BUTTON_MIDDLE = InputEvent.BUTTON2_MASK;
    /** {@link InputEvent#BUTTON3_MASK} */
    public static final int MOUSE_BUTTON_RIGHT = InputEvent.BUTTON3_MASK;
    public static boolean isLeftMouseButton(int buttonNumber) { return (buttonNumber==MOUSE_BUTTON_LEFT); }
    public static boolean isMiddleMouseButton(int buttonNumber) { return (buttonNumber==MOUSE_BUTTON_MIDDLE); }
    public static boolean isRightMouseButton(int buttonNumber) { return (buttonNumber==MOUSE_BUTTON_RIGHT); }

    protected static boolean debug = false;

    /** 2 seconds to wait for a click action finished on page. */
	public static final int DEFAULT_TIMEOUT_WAIT_CLICK = 2;//seconds

	/**
	 * The timeout in seconds to wait for the alert's presence.<br>
	 * The default value is 2 seconds.<br>
	 */
	public static final int DEFAULT_TIMEOUT_WAIT_ALERT = 2;//seconds

	/** 0, means no waiting. */
	public static final int TIMEOUT_NOWAIT = 0;
	/** -1, means wait for ever */
	public static final int TIMEOUT_WAIT_FOREVER = -1;

    /** 10 seconds to wait for a Robot click action finished on page. */
	public static final int DEFAULT_TIMEOUT_WAIT_ROBOT_CLICK = 10;//seconds

	/** time (in seconds) to wait for a click action finished on page. */
	public static int timeoutWaitClick = DEFAULT_TIMEOUT_WAIT_CLICK;//seconds

	/** time (in seconds) to wait for an Alert appear on page. */
	public static int timeoutWaitAlert = DEFAULT_TIMEOUT_WAIT_ALERT;//seconds

	/** time (in seconds) to check if an Alert is present on page before clicking.
	 * The default value is set to {@link #TIMEOUT_NOWAIT}, means we check immediately
	 * without waiting for presence of Alert. */
	public static int timeoutCheckAlertForClick = TIMEOUT_NOWAIT;//seconds


	/** time (in seconds) to wait for a Robot click action finished on page. */
	public static int timeoutWaitRobotClick = DEFAULT_TIMEOUT_WAIT_ROBOT_CLICK;//seconds

	static protected InputKeysParser keysparser = null;


	public static int getTimeoutCheckAlertForClick() {
		return timeoutCheckAlertForClick;
	}

	public static void setTimeoutCheckAlertForClick(int timeoutCheckAlertForClick) {
		WDLibrary.timeoutCheckAlertForClick = timeoutCheckAlertForClick;
	}

	/**
     * Default true.
     * Set to true if we want an Exception to be thrown when we perform a Click and attempt to verify through event
     * listeners that the Click actually happened.  The problem is, that so many Click verifications fail due to the
     * page and or WebElement going stale or being overwritten that Click verifications issue failures even though
     * the Click was successful.
     * <p>
     * Set to 'false' when you wish to avoid these types of failures.
     */
    public static boolean enableClickListenerFailures = true;

    /**
     * The indicator to tell SeleniumPlus to scroll an item (an item inside a scroll-able container) to make it visible.<br>
     * It is recommended to turn it off if the force-auto-scroll is not needed anymore, it may cause the page jump up and down.<br>
     * The default value is false.<br>
     */
    public static boolean enableForceScroll = false;//Used in EmbeddedObject.isShowOnPage().

    static {
		if(keysparser == null){
			try{
				InputStream stream = ClassLoader.getSystemResourceAsStream(CreateUnicodeMap.DEFAULT_FILE + CreateUnicodeMap.DEFAULT_FILE_EXT);
				INIFileReader reader = new INIFileReader(stream, 0, false);
				keysparser = new InputKeysParser(reader);
			}
			catch(Exception x){
				IndependantLog.debug("WDLibrary keysparser INI Reader or parser instantiation exception:", x);
			}

		}
    }

    /**
     * Check if the webelement is ready (not stale, visible) for any operation like click, double-click etc.<br>
     * If the webelement is stale, a SeleniumPlusException will be thrown out.<br>
     * If something unexpected happened, a SeleniumPlusException will be thrown out.<br>
     * If the webelement is not displayed (webdriver API), log a warning message or a SeleniumPlusException thrown out.<br>
     * @param clickable WebElement, the WebElement to check.
     * @param warnNotDisplayed boolean, if true, log a warning message when the element is not displayed; if false, throw a SeleniumPlusException.
     * @throws SeleniumPlusException if the WebElement is not ready (null, stale or invisible).
     */
    private static void checkBeforeOperation(WebElement clickable, boolean warnNotDisplayed) throws SeleniumPlusException{
    	if(!isDisplayed(clickable)){
    		//As isDisplayed() will not reflect the real visibility of component
    		//some visible object will be considered as not visible, which will not be processed if
    		//an Exception is thrown out, so a warning is logged instead of exception.
    		String msg = "weblement is not displayed.";
    		if(warnNotDisplayed) IndependantLog.warn(StringUtils.debugmsg(false)+msg);
    		else throw new SeleniumPlusException(msg);
    	}
    }

	/**
	 * Click(Mouse Left Button) - Click on WebElement or WebDriver object at the center
	 * @param clickable WebElement, as WebElement obj
	 * @param optional String[], the optional parameters
	 * <ul>
	 * <b>optional[0] autoscroll</b> boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
	 * <b>optional[1] verify</b> boolean, verify if the component is shown on page. The default value is true.<br>
	 * <b>optional[2] refresh</b> boolean, if the element will be refreshed after scrolling.
	 *                                      Normally we should refresh the component so that selenium API will return correct coordinates of the component.
	 *                                      But refresh will cost a lot of time and it will slow down the whole test, so we will not refresh the component by default.<br>
	 * </ul>
	 * @throws SeleniumPlusException
	 */
	public static void click(WebElement clickable, String... optional) throws SeleniumPlusException{
		checkBeforeOperation(clickable, true);
		try {
			boolean autoscroll = parseAutoScroll(optional);
			if(autoscroll) showOnPage(clickable, removeFirstParameter(optional));
			if(!clickable.isDisplayed()){
				IndependantLog.debug("The web-element is not visible, we wait 2 seconds maximum.");
				//If not visible, then we wait 2 seconds maximum.
//				WebDriverWait wait = new WebDriverWait(getWebDriver(), Processor.getSecsWaitForComponent());
				WebDriverWait wait = new WebDriverWait(getWebDriver(), 2);
				wait.until(ExpectedConditions.visibilityOf(clickable));
				IndependantLog.debug("clickable.isDisplayed()= "+clickable.isDisplayed());
			}

			clickable.click();
		}catch (Throwable th){
			IndependantLog.warn("Selenium Click failed due to "+StringUtils.debugmsg(th)+"\nTrying Robot Click ...");
			click(clickable, null, null, MOUSE_BUTTON_LEFT);
		}
	}
	/**
	 * Click(Mouse Left Button) the WebElement at center with a special key pressed.
	 * @param clickable 	WebElement, the WebElement to click on
	 * @param specialKey	Keys, the special key to press during the click
	 * @param optional String[], the optional parameters
	 * <ul>
	 * <b>optional[0] autoscroll</b> boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
	 * <b>optional[1] verify</b> boolean, verify if the component is shown on page. The default value is true.<br>
	 * <b>optional[2] refresh</b> boolean, if the element will be refreshed after scrolling.
	 *                                      Normally we should refresh the component so that selenium API will return correct coordinates of the component.
	 *                                      But refresh will cost a lot of time and it will slow down the whole test, so we will not refresh the component by default.<br>
	 * </ul>
	 * @throws SeleniumPlusException
	 */
	public static void click(WebElement clickable, Keys specialKey, String... optional) throws SeleniumPlusException{
		click(clickable, null, specialKey, MOUSE_BUTTON_LEFT, optional);
	}
	/**
	 * Click(Mouse Left Button) the WebElement at a certain coordination.
	 * @param clickable 	WebElement, the WebElement to click on
	 * @param offset		Point, the coordination relative to this WebElement to click at
	 * @param optional String[], the optional parameters
	 * <ul>
	 * <b>optional[0] autoscroll</b> boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
	 * <b>optional[1] verify</b> boolean, verify if the component is shown on page. The default value is true.<br>
	 * <b>optional[2] refresh</b> boolean, if the element will be refreshed after scrolling.
	 *                                      Normally we should refresh the component so that selenium API will return correct coordinates of the component.
	 *                                      But refresh will cost a lot of time and it will slow down the whole test, so we will not refresh the component by default.<br>
	 * </ul>
	 * @throws SeleniumPlusException
	 */
	public static void click(WebElement clickable, Point offset, String... optional) throws SeleniumPlusException{
		click(clickable, offset, null, MOUSE_BUTTON_LEFT, optional);
	}
	/**
	 * Click(Mouse Right Button) - Click on WebElement or WebDriver object at the center
	 * @param clickable WebElement, as WebElement obj
	 * @param optional String[], the optional parameters
	 * <ul>
	 * <b>optional[0] autoscroll</b> boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
	 * <b>optional[1] verify</b> boolean, verify if the component is shown on page. The default value is true.<br>
	 * <b>optional[2] refresh</b> boolean, if the element will be refreshed after scrolling.
	 *                                      Normally we should refresh the component so that selenium API will return correct coordinates of the component.
	 *                                      But refresh will cost a lot of time and it will slow down the whole test, so we will not refresh the component by default.<br>
	 * </ul>
	 * @throws SeleniumPlusException
	 */
	public static void rightclick(WebElement clickable, String... optional) throws SeleniumPlusException{
		click(clickable, null, null, MOUSE_BUTTON_RIGHT, optional);
	}
	/**
	 * Click(Mouse Right Button) the WebElement at center with a special key pressed.
	 * @param clickable 	WebElement, the WebElement to click on
	 * @param specialKey	Keys, the special key to presse during the click
	 * @param optional String[], the optional parameters
	 * <ul>
	 * <b>optional[0] autoscroll</b> boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
	 * <b>optional[1] verify</b> boolean, verify if the component is shown on page. The default value is true.<br>
	 * <b>optional[2] refresh</b> boolean, if the element will be refreshed after scrolling.
	 *                                      Normally we should refresh the component so that selenium API will return correct coordinates of the component.
	 *                                      But refresh will cost a lot of time and it will slow down the whole test, so we will not refresh the component by default.<br>
	 * </ul>
	 * @throws SeleniumPlusException
	 */
	public static void rightclick(WebElement clickable, Keys specialKey, String... optional) throws SeleniumPlusException{
		click(clickable, null, specialKey, MOUSE_BUTTON_RIGHT, optional);
	}
	/**
	 * Click(Mouse Right Button) the WebElement at a certain coordination.
	 * @param clickable 	WebElement, the WebElement to click on
	 * @param offset		Point, the coordination relative to this WebElement to click at
	 * @param optional String[], the optional parameters
	 * <ul>
	 * <b>optional[0] autoscroll</b> boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
	 * <b>optional[1] verify</b> boolean, verify if the component is shown on page. The default value is true.<br>
	 * <b>optional[2] refresh</b> boolean, if the element will be refreshed after scrolling.
	 *                                      Normally we should refresh the component so that selenium API will return correct coordinates of the component.
	 *                                      But refresh will cost a lot of time and it will slow down the whole test, so we will not refresh the component by default.<br>
	 * </ul>
	 * @throws SeleniumPlusException
	 */
	public static void rightclick(WebElement clickable, Point offset, String... optional) throws SeleniumPlusException{
		click(clickable, offset, null, MOUSE_BUTTON_RIGHT, optional);
	}

	/**
	 * Check if the point is inside of the boundary of WebElement.<br>
	 * @param element	WebElement,	The element to get boundary to check with.
	 * @param p			Point,	The point to check.
	 * @return boolean, true if the point is inside of the boundary of WebElement
	 */
	public static boolean inside(WebElement element, Point p){
		if(p==null) return true;

		Dimension dimension = element.getSize();
		Rectangle rect = new Rectangle(0, 0, dimension.width, dimension.height);

		return rect.contains(p);
	}

	/**
	 * Enlarge the listening area of DocumentClickCapture, if click offset is outside of the WebElement's boundary.<br>
	 * @param clickable	WebElement, The element to click.
	 * @param offset	Point, The offset relative to the WebElement to click at.
	 * @param listener	DocumentClickCapture, The click listener to capture the click event.
	 */
	private static void checkOffset(WebElement clickable, Point offset, DocumentClickCapture listener){
		String debugmsg = StringUtils.debugmsg(false);

		if(clickable==null || offset==null || listener==null){
			//We have nothing to check, or have no listener to disable
			return;
		}

		if(!inside(clickable, offset)){
			IndependantLog.warn(debugmsg+"Enlarge the listening area of DocumentClickCapture, the click point "+offset+" is outside of the WebElement "+clickable.getSize());
			listener.setEnlargeListeningArea(true);
		}
	}

	/**
	 * 'true', the default value for scrolling the web-element into view automatically before performing click ation on it.
	 */
	public static final boolean DEFAUT_AUTOSCROLL = true;

	/**
	 * parse the optional parameter to get the value of 'autoscroll'.
	 * @see #click(WebElement, String...)
	 * @see #click(WebElement, Point, Keys, int, String...)
	 * @see #doubleClick(WebElement, Point, Keys, int, String...)
	 * @see #clickUnverified(WebElement, Point, String...)
	 */
	private static boolean parseAutoScroll(String... optional){
		boolean autoscroll = DEFAUT_AUTOSCROLL;

		if(optional!=null && optional.length>0 && StringUtils.isValid(optional[0])){
			try{
				autoscroll = Boolean.parseBoolean(optional[0]);
			}catch(Exception e){
				IndependantLog.warn(StringUtils.debugmsg(false)+" Ignoring invalid parameter 'autoscroll' "+optional[0]+", met "+StringUtils.debugmsg(e));
			}
		}

		return autoscroll;
	}

	/**
	 * @param optional String[], the optional parameters
	 * @return String[] the rest parameters without the first one.
	 */
	private static String[] removeFirstParameter(String... optional){
		if(optional!=null && optional.length>1){
			String[] copy = new String[optional.length-1];
			for(int i=1;i<optional.length;i++){
				copy[i-1] = optional[i];
			}
			return copy;
		}else{
			return new String[0];
		}
	}

	private static void lastWaitForClickListener(DocumentClickCapture listener, String debugmsg){
		try{
			listener.startListening();

			// Dharmesh: Not report waitForClick failure due to listener event not capture
			// if click coordination out of component size or background.
			// It is hard to find sibling component.
			try {listener.waitForClick(timeoutWaitClick);}
			catch (Throwable the) {IndependantLog.debug(debugmsg+" waitForClick failed but will not be recorded as a failure.");};

		}catch(StaleElementReferenceException x){
			// the click probably was probably successful because the elements have changed!
			IndependantLog.debug(debugmsg+"StaleElementException (not found) suggests the action has been performed successfully.");
		}finally{
			IndependantLog.debug(debugmsg+"selenium API click finally stopping listener and resetting timeouts.");
			listener.stopListening();  // chrome is NOT stopping!
			WDTimeOut.resetImplicitlyWait(Processor.getSecsWaitForComponent(), TimeUnit.SECONDS);
		}
	}

	/**
	 * Click the WebElement at a certain coordination with a special key pressed.<br>
	 * Firstly it will try to get webelement's location and use Robot to click. At the same<br>
	 * time, it will listen to a 'javascript mouse down' event to find out if the click really<br>
	 * happened; If not, it will try to use Selenium's API to do the work.<br>
	 * If the click point is outside of the boundary of the WebElement, which means we are going<br>
	 * to click on the sibling component. At this situation, our click-listener will never receive<br>
	 * the click event, we will turn off the click-listener.<br>
	 *
	 * @param clickable 	WebElement, the WebElement to click on
	 * @param offset		Point, the coordination relative to this WebElement to click at.<br>
	 * 							   if the offset is null, then click at the center.
	 * @param specialKey	Keys, the special key to press during the click
	 * @param mouseButtonNumber int, the mouse-button-number representing right, middle, or left button.
	 * 								 it can be {@link #MOUSE_BUTTON_LEFT} or {@link #MOUSE_BUTTON_RIGHT}.<br>
	 * 								 {@link #MOUSE_BUTTON_MIDDLE} NOT supported yet.
	 * @param optional String[], the optional parameters
	 * <ul>
	 * <b>optional[0] autoscroll</b> boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
	 * <b>optional[1] verify</b> boolean, verify if the component is shown on page. The default value is true.<br>
	 * <b>optional[2] refresh</b> boolean, if the element will be refreshed after scrolling.
	 *                                      Normally we should refresh the component so that selenium API will return correct coordinates of the component.
	 *                                      But refresh will cost a lot of time and it will slow down the whole test, so we will not refresh the component by default.<br>
	 * </ul>
	 * @throws SeleniumPlusException
	 */
	public static void click(WebElement clickable, Point offset, Keys specialKey, int mouseButtonNumber, String... optional) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(WDLibrary.class, "click");

		checkBeforeOperation(clickable, true);
		WebDriver wd = WDLibrary.getWebDriver();
		RemoteDriver rd = (wd instanceof RemoteDriver)? (RemoteDriver) wd : null;
		boolean autoscroll = parseAutoScroll(optional);

		if(autoscroll) showOnPage(clickable, removeFirstParameter(optional));

		MouseEvent event = null;
		DocumentClickCapture listener = new DocumentClickCapture(true, clickable);
		checkOffset(clickable, offset, listener);

		try {
			//2. Perform the click action by Robot
			Point location = getScreenLocation(clickable);
			if(offset!=null) location.translate(offset.x, offset.y);
			else	{
				Dimension d = clickable.getSize();
				location.translate(d.width/2, d.height/2);
			}
			listener.addListeners(false);
			IndependantLog.debug(debugmsg+"RBT is going to click at screen location "+location);
			RBT.click(rd, location, specialKey, mouseButtonNumber, 1);
			listener.startListening();

			//3. Wait for the 'click' event, check if the 'mousedown' event really happened.
			// Carl Nagle -- FIREFOX PROBLEM: A link that takes you to a new page (like the Google SignIn link) will
			// trigger the default action and apparently will NOT allow us to detect the Click occurred.
			// So this WILL generate a waitForClick InterruptedException (Timeout)
			event = listener.waitForClick(timeoutWaitRobotClick);
			if(event == null){
				IndependantLog.resumeLogging();
				IndependantLog.warn(debugmsg+" Robot may fail to perform click. Click screen location is "+location);
				throw new SeleniumPlusException("The Robot click action didn't happen.");
			}else{
				IndependantLog.resumeLogging();
				IndependantLog.debug(debugmsg+"Robot click successful.");
			}
		} catch (Throwable thr){
			IndependantLog.resumeLogging();
			IndependantLog.warn(debugmsg+"Met Exception "+StringUtils.debugmsg(thr));

			// let the failed listeners exit.
			try{Thread.sleep(DocumentClickCapture.LISTENER_LOOP_DELAY + DocumentClickCapture.delayWaitReady);}
			catch(Exception x){
				IndependantLog.debug(debugmsg + StringUtils.debugmsg(x));
			}

			try {
				//2. Perform the click action by Selenium
				IndependantLog.debug(debugmsg+" Try selenium API to click.");
				//Create a combined actions according to the parameters
				Actions actions = new Actions(getWebDriver());

				if(autoscroll){
//					if(offset!=null) actions.moveToElement(clickable, offset.x, offset.y);
					if(offset!=null) actions = moveToElement(actions, clickable, offset.x, offset.y);
					else actions.moveToElement(clickable);
				}

				if(specialKey!=null) actions.keyDown(specialKey);
				if(isRightMouseButton(mouseButtonNumber)) actions.contextClick();
				else if(isLeftMouseButton(mouseButtonNumber))	actions.click();
				else if(isMiddleMouseButton(mouseButtonNumber)){
					throw new SeleniumPlusException("Click 'mouse middle button' has not been supported yet.");
				}else{
					throw new SeleniumPlusException("Mouse button number '"+mouseButtonNumber+"' cannot be recognized.");
				}
				if(specialKey!=null) actions.keyUp(specialKey);

				IndependantLog.debug(debugmsg+"click with key '"+specialKey+"', mousebutton='"+mouseButtonNumber+"'");

				//Perform the actions
				listener.addListeners(false);
				//if the Robot click worked, but was not detected. If we clicked a link, original page has
				//disappeared, so the link doesn't exist neither, the WebElement is stale. WebDriver will
				//not throw StaleElementReferenceException until the 'implicit timeout' is reached.
				//But we don't want to waste that time, so just set 'implicit timeout' to 0 and don't wait.
				WDTimeOut.setImplicitlyWait(0, TimeUnit.SECONDS);
				actions.build().perform();
				lastWaitForClickListener(listener, debugmsg);
			} catch (Throwable th){
				listener.stopListening();  // chrome is NOT stopping!
				if(enableClickListenerFailures) {
					IndependantLog.error(debugmsg, th);
					throw new SeleniumPlusException("click action failed: "+StringUtils.debugmsg(th));
				}
				else{
					IndependantLog.debug(debugmsg+"ignoring selenium API click failure caused by "+th.getClass().getName()+", "+th.getMessage());
				}
			}
		}finally{
			IndependantLog.debug(debugmsg+"FINALLY stopping ongoing listener, if any.");
			listener.stopListening();  // chrome is NOT stopping!
		}
	}

	/**
	 * Double-Click the WebElement at a certain coordination with a special key pressed.<br>
	 * Firstly it will try to get webelement's location and use Robot to double click. At the same<br>
	 * time, it will listen to a 'javascript mouse down' event to find out if the double click really<br>
	 * happened; If not, it will try to use Selenium's API to do the work.<br>
	 * If the click point is outside of the boundary of the WebElement, which means we are going<br>
	 * to click on the sibling component. At this situation, our click-listener will never receive<br>
	 * the click event, we will turn off the click-listener.<br>
	 *
	 * @param clickable 	WebElement, the WebElement to click on
	 * @param offset		Point, the coordination relative to this WebElement to click at.<br>
	 * 							   if the offset is null, then click at the center.
	 * @param specialKey	Keys, the special key to press during the click
	 * @param mouseButtonNumber int, the mouse-button-number representing right, middle, or left button.
	 * 								 it can be {@link #MOUSE_BUTTON_LEFT}<br>
	 * 								 {@link #MOUSE_BUTTON_MIDDLE} and {@link #MOUSE_BUTTON_RIGHT} NOT supported yet.
	 * @param optional String[], the optional parameters
	 * <ul>
	 * <b>optional[0] autoscroll</b> boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
	 * <b>optional[1] verify</b> boolean, verify if the component is shown on page. The default value is true.<br>
	 * <b>optional[2] refresh</b> boolean, if the element will be refreshed after scrolling.
	 *                                      Normally we should refresh the component so that selenium API will return correct coordinates of the component.
	 *                                      But refresh will cost a lot of time and it will slow down the whole test, so we will not refresh the component by default.<br>
	 * </ul>
	 * @throws SeleniumPlusException
	 */
	public static void doubleClick(WebElement clickable, Point offset, Keys specialKey, int mouseButtonNumber, String... optional) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(WDLibrary.class, "doubleClick");

		checkBeforeOperation(clickable, true);

		MouseEvent event = null;
		DocumentClickCapture listener = new DocumentClickCapture(true, clickable);
		checkOffset(clickable, offset, listener);
		boolean autoscroll = parseAutoScroll(optional);

		if(autoscroll) showOnPage(clickable, removeFirstParameter(optional));

		try {
			//2. Perform the click action by Robot
			Point location = getScreenLocation(clickable);
			if(offset!=null) location.translate(offset.x, offset.y);
			else	location.translate(clickable.getSize().width/2, clickable.getSize().height/2);
			listener.addListeners(false);
			RBT.click(location, specialKey, mouseButtonNumber, 2);
			listener.startListening();

			//3. Wait for the 'click' event, check if the 'mousedown' event really happened.
			event = listener.waitForClick(timeoutWaitClick);
			if(event == null){
				IndependantLog.warn(debugmsg+" Robot may fail to perform doubleclick. Click screen location is "+location);
				throw new SeleniumPlusException("The doubleclick action didn't happen.");
			}else{
				IndependantLog.debug(debugmsg+"doubleclick has been peformed.");
			}
		} catch (Throwable thr){
			IndependantLog.warn(debugmsg+"Met Exception "+StringUtils.debugmsg(thr));
			try {
				//2. Perform the click action by Selenium
				IndependantLog.debug(debugmsg+" Try selenium API to doubleclick.");
				//Create a combined actions according to the parameters
				Actions actions = new Actions(WDLibrary.getWebDriver());

				if(autoscroll){
//					if(offset!=null) actions.moveToElement(clickable, offset.x, offset.y);
					if(offset!=null) actions = moveToElement(actions, clickable, offset.x, offset.y);
					else actions.moveToElement(clickable);
				}

				if(specialKey!=null) actions.keyDown(specialKey);
				if(isLeftMouseButton(mouseButtonNumber)) actions.doubleClick();
				else if(isMiddleMouseButton(mouseButtonNumber) || isRightMouseButton(mouseButtonNumber)){
					throw new SeleniumPlusException("Double click 'mouse middle/right button' has not been supported yet.");
				}
				else throw new SeleniumPlusException("Mouse button number '"+mouseButtonNumber+"' cannot be recognized.");

				if(specialKey!=null) actions.keyUp(specialKey);

				IndependantLog.debug(debugmsg+"doubleclick with key '"+specialKey+"', mousebutton='"+mouseButtonNumber+"'");

				//Perform the actions
				listener.addListeners(false);
				//unfortunately, if the Robot click worked, but was not detected, we have to wait the full
				//WebDriver implied timeout period for the perform() failure to occur.
				actions.build().perform();
				lastWaitForClickListener(listener, debugmsg);
			} catch (Throwable th){
				listener.stopListening();  // chrome is NOT stopping!
				if(enableClickListenerFailures) {
					IndependantLog.error(debugmsg, th);
					throw new SeleniumPlusException("doubleclick action failed: "+StringUtils.debugmsg(th));
				}
				else{
					IndependantLog.debug(debugmsg+"ignoring selenium API doubleclick failure caused by "+th.getClass().getName()+", "+th.getMessage());
				}
			}
		}finally{
			IndependantLog.debug(debugmsg+"FINALLY stopping ongoing listener, if any.");
			listener.stopListening();  // chrome is NOT stopping!
		}
	}

	/**
	 * Double-Click(Mouse Left Button) the WebElement at the center.
	 * @param clickable 	WebElement, the WebElement to click on
	 * @param optional String[], the optional parameters
	 * <ul>
	 * <b>optional[0] autoscroll</b> boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
	 * <b>optional[1] verify</b> boolean, verify if the component is shown on page. The default value is true.<br>
	 * <b>optional[2] refresh</b> boolean, if the element will be refreshed after scrolling.
	 *                                      Normally we should refresh the component so that selenium API will return correct coordinates of the component.
	 *                                      But refresh will cost a lot of time and it will slow down the whole test, so we will not refresh the component by default.<br>
	 * </ul>
	 * @throws SeleniumPlusException
	 */
	public static void doubleClick(WebElement clickable, String... optional) throws SeleniumPlusException{
		doubleClick(clickable, null, null, MOUSE_BUTTON_LEFT, optional);
	}
	/**
	 * Double-Click(Mouse Left Button) the WebElement at the center with a special key pressed.
	 * @param clickable 	WebElement, the WebElement to click on
	 * @param specialKey	Keys, the special key to press during the click
	 * @param optional String[], the optional parameters
	 * <ul>
	 * <b>optional[0] autoscroll</b> boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
	 * <b>optional[1] verify</b> boolean, verify if the component is shown on page. The default value is true.<br>
	 * <b>optional[2] refresh</b> boolean, if the element will be refreshed after scrolling.
	 *                                      Normally we should refresh the component so that selenium API will return correct coordinates of the component.
	 *                                      But refresh will cost a lot of time and it will slow down the whole test, so we will not refresh the component by default.<br>
	 * </ul>
	 * @throws SeleniumPlusException
	 */
	public static void doubleClick(WebElement clickable, Keys specialKey, String... optional) throws SeleniumPlusException{
		doubleClick(clickable, null, specialKey, MOUSE_BUTTON_LEFT, optional);
	}
	/**
	 * Double-Click(Mouse Left Button) the WebElement at a certain coordination.
	 * @param clickable 	WebElement, the WebElement to click on
	 * @param offset		Point, the coordination relative to this WebElement to click at
	 * @param optional String[], the optional parameters
	 * <ul>
	 * <b>optional[0] autoscroll</b> boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
	 * <b>optional[1] verify</b> boolean, verify if the component is shown on page. The default value is true.<br>
	 * <b>optional[2] refresh</b> boolean, if the element will be refreshed after scrolling.
	 *                                      Normally we should refresh the component so that selenium API will return correct coordinates of the component.
	 *                                      But refresh will cost a lot of time and it will slow down the whole test, so we will not refresh the component by default.<br>
	 * </ul>
	 * @throws SeleniumPlusException
	 */
	public static void doubleClick(WebElement clickable, Point offset, String... optional) throws SeleniumPlusException{
		doubleClick(clickable, offset, null, MOUSE_BUTTON_LEFT, optional);
	}

	/**
	 * Get the web-element's location relative to the browser. If the web-element is inside frames,
	 * the frame's location will also be added.<br>
	 * <b>Note: </b>The location got by this method might be slightly shifted. To get more accurate
	 *             location, please call {@link #getLocation(WebElement, boolean)} with 2th parameter
	 *             given as false.<br>
	 *
	 * @param webelement WebElement, the element to get location
	 * @return Point, the element's location inside a browser
	 * @throws SeleniumPlusException
	 * @see {@link #getLocation(WebElement, boolean)}
	 */
	public static Point getLocation(WebElement webelement) throws SeleniumPlusException{
		return getLocation(webelement, true);
	}

	/**
	 * Get the web-element's location relative to the browser client area. If the web-element is inside frames,
	 * the frame's location will also be added.<br>
	 * <b>Note: </b>If the 2th parameter is given false, you might get a more accurate location.<br>
	 * @param webelement WebElement, the element to get location
	 * @param useOnPageFirstly boolean, There are 2 ways to get element's location relative to the page:
	 *                                  one is {@link Coordinates#onPage()}, the other is {@link WebElement#getLocation()}.
	 *                                  it seems that the 2th method ({@link WebElement#getLocation()}) is more accurate.
	 *                                  But historically, we called {@link Coordinates#onPage()} in first place.<br>
	 *                                  If this parameter is true, {@link Coordinates#onPage()} will be used firstly as before.<br>
	 *                                  Otherwise, {@link WebElement#getLocation()} will be used directly<br>
	 *
	 * @return Point, the element's location relative to the browser client area
	 * @throws SeleniumPlusException
	 */
	public static Point getLocation(WebElement webelement, boolean useOnPageFirstly) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);

		try{
			//1. Get the element's location relative to the frame
			org.openqa.selenium.Point p=null;
			Coordinates c = null;
			try{
				c = ((RemoteWebElement)webelement).getCoordinates();
				IndependantLog.debug(debugmsg+"Selenium reports Coordinates.onPage(): "+ c.onPage().x +","+ c.onPage().y);
			}catch(UnsupportedOperationException x){
				IndependantLog.debug(debugmsg+"Selenium reports Coordinates.onPage() is NOT yet supported.");
			}
			catch(Throwable t){
				IndependantLog.debug(debugmsg+"ignoring "+ StringUtils.debugmsg(t));
			}

			p = webelement.getLocation();
			IndependantLog.debug(debugmsg+"Selenium reports Element.getLocation(): "+ p.x +","+ p.y);

			if(useOnPageFirstly && (c instanceof Coordinates)){
				p = c.onPage();
			}
			IndependantLog.debug(debugmsg+"using WebElement 'CLIENT AREA' location as ("+p.x+","+p.y+")");

			//2. Add the frame's location (relative to the the browser client area)
			if(lastFrame!=null){
				p.x += lastFrame.getLocation().x;
				p.y += lastFrame.getLocation().y;
				IndependantLog.debug(debugmsg+"added lastFrame offsets "+lastFrame.getLocation().toString()+", new tentative 'CLIENT AREA' location ("+p.x+","+p.y+")");
			}

			return new Point(p.x, p.y);
		}catch (Exception e){
			IndependantLog.error(debugmsg, e);
			throw new SeleniumPlusException("getLocation failed: "+StringUtils.debugmsg(e));
		}
	}

	/**
	 * Get the absolute coordination on the screen of the webelement object.<br>
	 * Hope Selenium will give this function one day!<br>
	 *
	 * @param webelement WebElement, a selenium webelement object
	 * @return Point, the absolute coordination on the screen of the webelement object.
	 * @throws SeleniumPlusException
	 */
	public static Point getScreenLocation(WebElement webelement)throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(WDLibrary.class, "getScreenLocation");

		Point p = null;
		try{
			try{
				Coordinates c = ((RemoteWebElement)webelement).getCoordinates();
				org.openqa.selenium.Point screen = c.onScreen();
				IndependantLog.debug(debugmsg+"Selenium reports the WebElement SCREEN location as ("+screen.x+","+screen.y+")");
				return new Point(screen.x, screen.y);
			}
			catch(UnsupportedOperationException x){
				IndependantLog.debug(debugmsg+"Selenium reports coordinates.onScreen() is NOT yet supported.");
			}
			catch(Throwable t){
				IndependantLog.debug(debugmsg+"ignoring "+ StringUtils.debugmsg(t));
			}

			//1. Get the element's location relative to the browser client area
			p = getLocation(webelement);

			//2. Add the browser client area's location (relative to the browser's window), which is different according to browser
			float clientx = getLastBrowserWindow().getClientX();
			float clienty = getLastBrowserWindow().getClientY();
			p.x += clientx;
			p.y += clienty;
			IndependantLog.debug(debugmsg+"added lastBrowserWindow ClientXY offsets ("+clientx+","+clienty+"), new tentative PAGE location ("+p.x+","+p.y+")");

			//2.1 Fix "client area LOCATION offset problem"
			if(lastFrame==null){
				//Lei Wang: I think that "client area LOCATION offset problem" is not related to the lastFrame.
				//Even the lastFrame is not null, that problem might exist too and we should try to fix it.
				//TODO remove the condition "if(lastFrame==null)" in future
				if (getLastBrowserWindow().getClientX()==0 &&
					getLastBrowserWindow().getBorderWidth()==0 &&
					getLastBrowserWindow().getPageXOffset()==0 &&
					getLastBrowserWindow().getWidth()>getLastBrowserWindow().getClientWidth() ) {
					int diff = Math.round(getLastBrowserWindow().getWidth()- getLastBrowserWindow().getClientWidth())/2;
					IndependantLog.debug(debugmsg + "detecting potential client area LOCATION offset problem of "+ diff +" pixels");
					if (diff < 12) {
						p.x += diff;
						p.y += diff;
						IndependantLog.debug(debugmsg + "added lastBrowserWindow suspected location offset error, new tentative PAGE location ("
								+ p.x + ","
								+ p.y + ")");
					}
				}
			}

			//3. Add the browser window's location (relative to the screen)
			float windowx = getLastBrowserWindow().getX();
			float windowy = getLastBrowserWindow().getY();
			p.x += windowx;
			p.y += windowy;
			IndependantLog.debug(debugmsg+"added lastBrowserWindow XY offsets ("+windowx+","+windowy+"), new tentative SCREEN location ("+p.x+","+p.y+")");

			//4. minus scrollbar offset
			float scrollbarx = getLastBrowserWindow().getPageXOffset();
			float scrollbary = getLastBrowserWindow().getPageYOffset();
			p.x -= scrollbarx;
			p.y -= scrollbary;
			IndependantLog.debug(debugmsg+"added lastBrowserWindow PageXY offsets ("+scrollbarx+","+scrollbary+"), new tentative SCREEN location ("+p.x+","+p.y+")");

			if(debug){
				if(!getBypassRobot()){
					Robot.getRobot().mouseMove(p.x, p.y);
					Thread.sleep(500);
				}
			}
			IndependantLog.debug(debugmsg+"The Left-Upper corner's SCREEN coordinate is ("+p.x+","+p.y+")");

			return p;
		} catch (Throwable th){
			IndependantLog.error(debugmsg, th);
			throw new SeleniumPlusException("getScreenLocation failed: "+StringUtils.debugmsg(th));
		}
	}

	/**
	 * Convert a SAFS RobotKeyEvent to a Selenium WebDriver Keys Enum
	 * @param event RobotKeyEvent
	 * @return Keys enum for (primarily) non-printable (control) characters, or null.
	 */
	public static Keys convertToKeys(RobotKeyEvent event){
		try{
			return convertToKeys(event.get_keycode());
		}catch(Exception e){
			IndependantLog.error(StringUtils.debugmsg(false)+" Met Exception "+StringUtils.debugmsg(e));
			return null;
		}
	}

	/**
	 * Convert a Java KEYCODE to a Selenium WebDriver Keys Enum
	 * @param keycode int, a java keycode
	 * @return Keys enum for (primarily) non-printable (control) characters, or null.
	 */
	public static Keys convertToKeys(int keycode){
		Keys key = null;
		switch(keycode){
			case java.awt.event.KeyEvent.VK_ADD:
				key = Keys.ADD;
				break;
			case java.awt.event.KeyEvent.VK_ALT:
				key = Keys.ALT;
				break;
			case java.awt.event.KeyEvent.VK_KP_DOWN:
				key = Keys.ARROW_DOWN;
				break;
			case java.awt.event.KeyEvent.VK_KP_LEFT:
				key = Keys.ARROW_LEFT;
				break;
			case java.awt.event.KeyEvent.VK_KP_RIGHT:
				key = Keys.ARROW_RIGHT;
				break;
			case java.awt.event.KeyEvent.VK_KP_UP:
				key = Keys.ARROW_UP;
				break;
			case java.awt.event.KeyEvent.VK_BACK_SPACE:
				key = Keys.BACK_SPACE;
				break;
			case java.awt.event.KeyEvent.VK_CANCEL:
				key = Keys.CANCEL;
				break;
			case java.awt.event.KeyEvent.VK_CLEAR:
				key = Keys.CLEAR;
				break;
			case java.awt.event.KeyEvent.VK_WINDOWS:
				key = Keys.COMMAND;
				break;
			case java.awt.event.KeyEvent.VK_CONTROL:
				key = Keys.CONTROL;
				break;
			case java.awt.event.KeyEvent.VK_DECIMAL:
				key = Keys.DECIMAL;
				break;
			case java.awt.event.KeyEvent.VK_DELETE:
				key = Keys.DELETE;
				break;
			case java.awt.event.KeyEvent.VK_DIVIDE:
				key = Keys.DIVIDE;
				break;
			case java.awt.event.KeyEvent.VK_DOWN:
				key = Keys.DOWN;
				break;
			case java.awt.event.KeyEvent.VK_END:
				key = Keys.END;
				break;
			case java.awt.event.KeyEvent.VK_ENTER:
				key = Keys.ENTER;
				break;
			case java.awt.event.KeyEvent.VK_EQUALS:
				key = Keys.EQUALS;
				break;
			case java.awt.event.KeyEvent.VK_ESCAPE:
				key = Keys.ESCAPE;
				break;
			case java.awt.event.KeyEvent.VK_F1:
				key = Keys.F1;
				break;
			case java.awt.event.KeyEvent.VK_F2:
				key = Keys.F2;
				break;
			case java.awt.event.KeyEvent.VK_F3:
				key = Keys.F3;
				break;
			case java.awt.event.KeyEvent.VK_F4:
				key = Keys.F4;
				break;
			case java.awt.event.KeyEvent.VK_F5:
				key = Keys.F5;
				break;
			case java.awt.event.KeyEvent.VK_F6:
				key = Keys.F6;
				break;
			case java.awt.event.KeyEvent.VK_F7:
				key = Keys.F7;
				break;
			case java.awt.event.KeyEvent.VK_F8:
				key = Keys.F8;
				break;
			case java.awt.event.KeyEvent.VK_F9:
				key = Keys.F9;
				break;
			case java.awt.event.KeyEvent.VK_F10:
				key = Keys.F10;
				break;
			case java.awt.event.KeyEvent.VK_F11:
				key = Keys.F11;
				break;
			case java.awt.event.KeyEvent.VK_F12:
				key = Keys.F12;
				break;
			case java.awt.event.KeyEvent.VK_HELP:
				key = Keys.HELP;
				break;
			case java.awt.event.KeyEvent.VK_HOME:
				key = Keys.HOME;
				break;
			case java.awt.event.KeyEvent.VK_INSERT:
				key = Keys.INSERT;
				break;
			case java.awt.event.KeyEvent.VK_LEFT:
				key = Keys.LEFT;
				break;
			case java.awt.event.KeyEvent.VK_META:
				key = Keys.META;
				break;
			case java.awt.event.KeyEvent.VK_MULTIPLY:
				key = Keys.MULTIPLY;
				break;
			case java.awt.event.KeyEvent.VK_NUMPAD0:
				key = Keys.NUMPAD0;
				break;
			case java.awt.event.KeyEvent.VK_NUMPAD1:
				key = Keys.NUMPAD1;
				break;
			case java.awt.event.KeyEvent.VK_NUMPAD2:
				key = Keys.NUMPAD2;
				break;
			case java.awt.event.KeyEvent.VK_NUMPAD3:
				key = Keys.NUMPAD3;
				break;
			case java.awt.event.KeyEvent.VK_NUMPAD4:
				key = Keys.NUMPAD4;
				break;
			case java.awt.event.KeyEvent.VK_NUMPAD5:
				key = Keys.NUMPAD5;
				break;
			case java.awt.event.KeyEvent.VK_NUMPAD6:
				key = Keys.NUMPAD6;
				break;
			case java.awt.event.KeyEvent.VK_NUMPAD7:
				key = Keys.NUMPAD7;
				break;
			case java.awt.event.KeyEvent.VK_NUMPAD8:
				key = Keys.NUMPAD8;
				break;
			case java.awt.event.KeyEvent.VK_NUMPAD9:
				key = Keys.NUMPAD9;
				break;
			case java.awt.event.KeyEvent.VK_PAGE_DOWN:
				key = Keys.PAGE_DOWN;
				break;
			case java.awt.event.KeyEvent.VK_PAGE_UP:
				key = Keys.PAGE_UP;
				break;
			case java.awt.event.KeyEvent.VK_PAUSE:
				key = Keys.PAUSE;
				break;
			case java.awt.event.KeyEvent.VK_RIGHT:
				key = Keys.RIGHT;
				break;
			case java.awt.event.KeyEvent.VK_SEMICOLON:
				key = Keys.SEMICOLON;
				break;
			case java.awt.event.KeyEvent.VK_SEPARATOR:
				key = Keys.SEPARATOR;
				break;
			case java.awt.event.KeyEvent.VK_SHIFT:
				key = Keys.SHIFT;
				break;
			case java.awt.event.KeyEvent.VK_SPACE:
				key = Keys.SPACE;
				break;
			case java.awt.event.KeyEvent.VK_SUBTRACT:
				key = Keys.SUBTRACT;
				break;
			case java.awt.event.KeyEvent.VK_TAB:
				key = Keys.TAB;
				break;
			case java.awt.event.KeyEvent.VK_UP:
				key = Keys.UP;
				break;
		}
		return key;
	}

	/**
	 * Convert a SAFS RobotKeyEvent to a standard CharSequence character.
	 * @param event
	 * @return CharSequence character, or null.
	 */
	static CharSequence convertToCharacter(RobotKeyEvent event){
		List<RobotKeyEvent> list = new ArrayList<RobotKeyEvent>();
		list.add(event);
		return keysparser.antiParse(list);
	}

	/**
	 * Does NOT clear any existing text in the control, but does attempt to insure the window/control has focus.
	 * <br><em>Purpose:</em>
	 * <a href="/sqabasic2000/SeleniumGenericMasterFunctionsReference.htm#detail_InputKeys" alt="inputKeys Keyword Reference" title="inputKeys Keyword Reference">inputKeys</a>
	 * <p>
	 * Bypasses attempts to use AWT Robot for keystrokes.
	 * Attempts to convert SAFS keystrokes to Selenium low-level Actions keystrokes.
	 *
	 * <b>Note:</b> This method doesn't work well with the Firefox 55/56 and geckodriver 0.18.0/0.19.0.<br>
	 * <b>Note:</b> This method doesn't work well with the IE 11 and IEDriverServer 3.4.0.<br>
	 *
	 * @param we WebElement to send SAFS keystrokes (or plain text).
	 * @param keystrokes SAFS keystrokes or plain text to type.
	 * @throws SeleniumPlusException if we are unable to process the keystrokes successfully.
	 **/
	public static void inputKeysSAFS2Selenium(WebElement we, String keystrokes) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		IndependantLog.debug(debugmsg+" processing '"+ keystrokes +"' on webelement "+we);
		if(!focus(we)) IndependantLog.warn(debugmsg+" Fail to set focus to webelement "+we);

		RemoteDriver wd = null;
		try{ wd = (RemoteDriver) getWebDriver();}catch(Exception x){}
		// convert to Selenium low-level Action keystrokes.
		if(keysparser != null){
			Vector<?> keys = keysparser.parseInput(keystrokes);
			Actions actions = new Actions(wd);

			if(we!=null) actions = actions.moveToElement(we);

			Iterator<?> events = keys.iterator();
			RobotKeyEvent event;
			Keys k = null;
			CharSequence c = null;
			while(events.hasNext()){
				try{
					event = (RobotKeyEvent) events.next();
					c = null;
					k =  convertToKeys(event);
					if( k == null) {
						c = convertToCharacter(event);
					}else{

					}
					switch(event.get_event()){

						case RobotKeyEvent.KEY_PRESS:
							if(k != null){
								IndependantLog.debug(debugmsg+" handling keyDown '"+ k.name() +"'");
								actions = actions.keyDown(k);
							}else{
								IndependantLog.debug(debugmsg+" send char '"+ c +"'");
								actions = actions.sendKeys(c);
							}
							break;

						case RobotKeyEvent.KEY_RELEASE:
							if(k != null){
								IndependantLog.debug(debugmsg+" handling keyUp '"+ k.name() +"'");
								actions = actions.keyUp(k);
							}else{
								IndependantLog.debug(debugmsg+" send char '"+ c +"'");
								actions = actions.sendKeys(c);
							}
							break;

						case RobotKeyEvent.KEY_TYPE:
							if(k != null){
								IndependantLog.debug(debugmsg+" send Key '"+ k.name() +"'");
								actions = actions.sendKeys(k);
							}else{
								IndependantLog.debug(debugmsg+" send char '"+ c +"'");
								actions = actions.sendKeys(c);
							}
							break;
						default:
					}
				}catch(Exception x){
					IndependantLog.debug(debugmsg+" IGNORING RobotKeyEvent exception:", x);
				}
			}
			try{
				actions.build().perform();
			}catch(StaleElementReferenceException x){
				// the click probably was successful because the elements have changed!
				IndependantLog.debug(debugmsg+" StaleElementException (not found).");
			}catch(Throwable x){
				IndependantLog.debug(debugmsg+" "+ x.getClass().getName()+", "+ x.getMessage());
			}finally{
				IndependantLog.debug(debugmsg+" selenium actions.build().perform() complete.");
			}
		}else{
			// TODO what if keyparser cannot load a keys converter???
		}
	}

	/**
	 * Press down a Key. Call {@link #keyRelease(int)} to release the key.
	 * @param keycode int, Java keycode to press (e.g. <code>KeyEvent.VK_A</code>)
	 * @throws SeleniumPlusException if fail
	 * @see #keyRelease(int)
	 */
	public static void keyPress(int keycode) throws SeleniumPlusException{
		boolean done = false;
		String debugmsg = StringUtils.debugmsg(false);

		try{
			done = RBT.keyPress(keycode);
		}catch(SeleniumPlusException e){
			IndependantLog.warn(debugmsg+" Failed to press key '"+keycode+"' by Robot, due to "+e.getMessage());
		}
		if(!done){
			try{
				keyDown(convertToKeys(keycode));
			}catch(SeleniumPlusException e){
				IndependantLog.warn(debugmsg+" Failed to press key '"+keycode+"' by Selenium, due to "+e.getMessage());
			}
		}
	}

	/**
	 * Release a Key. Release the key pressed by {@link #keyPress(int)}.
	 * @param keycode int, Java keycode to release (e.g. <code>KeyEvent.VK_A</code>)
	 * @throws SeleniumPlusException if fail
	 * @see #keyPress(int)
	 */
	public static void keyRelease(int keycode) throws SeleniumPlusException{
		boolean done = false;
		String debugmsg = StringUtils.debugmsg(false);

		try{
			done = RBT.keyRelease(keycode);
		}catch(SeleniumPlusException e){
			IndependantLog.warn(debugmsg+" Failed to release key '"+keycode+"' by Robot, due to "+e.getMessage());
		}

		if(!done){
			try{
				keyUp(convertToKeys(keycode));
			}catch(SeleniumPlusException e){
				IndependantLog.warn(debugmsg+" Failed to release key '"+keycode+"' by Selenium, due to "+e.getMessage());
			}
		}
	}

	/**
	 * Scroll the mouse wheel.
	 * @param wheelAmt int, the wheel amount to scroll.
	 * @throws SeleniumPlusException if fail
	 */
	public static void mouseWheel(int wheelAmt) throws SeleniumPlusException{
		boolean done = false;
		String debugmsg = StringUtils.debugmsg(false);

		try{
			done = RBT.mouseWheel(wheelAmt);
		}catch(SeleniumPlusException e){
			IndependantLog.warn(debugmsg+" Failed to complete mouseWheel by Robot, due to "+e.getMessage());
		}

		if(!done){
			try{
				WebElement element = getObject("xpath=/html");
				mouseWheel(element, wheelAmt);
				done = true;
			}catch(SeleniumPlusException e){
				IndependantLog.warn(debugmsg+" Failed to complete mouseWheel by Javascript, due to "+e.getMessage());
			}
		}
	}

	/**
	 * Send Mouse WheelEvent to the DOM element by javascript.<br>
	 * <b>Note:</b> We can send the WheelEvent, but it is not guarantee that the DOM element will react as expected.<br>
	 *
	 * @param element WebElement, the target to receive the WheelEvent.
	 * @param wheelAmt int, the wheel amount to scroll.
	 * @throws SeleniumPlusException if fail
	 */
	private static void mouseWheel(WebElement element, int wheelAmt)  throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);

		StringBuffer jsScript = new StringBuffer();
		jsScript.append(JavaScriptFunctions.mouseWheel());
		jsScript.append("mouseWheel(arguments[0],arguments[1],arguments[2],arguments[3], arguments[4]);");

		try {
			WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), element, 0, wheelAmt, 0, 0);
			return;
		} catch(Exception e) {
			IndependantLog.debug(debugmsg+" Met exception.",e);
		}

		throw new SeleniumPlusException("Failed to complete mouseWheel by Javascript");
	}

	/**
	 * Press down a Key by Selenium's Actions API. Call {@link #keyUp(Keys)} to release the key.
	 * @param keycode Keys, keycode to press (e.g. <code>Keys.CONTROL</code>)
	 * @throws SeleniumPlusException if fail
	 * @see #keyUp(Keys)
	 */
	public static void keyDown(Keys keycode) throws SeleniumPlusException{
		try {
			WebDriver wd = getWebDriver();
			Actions actions = new Actions(wd);
			actions.keyDown(keycode);
			actions.build().perform();
		} catch (Exception e) {
			throw new SeleniumPlusException("Unable to successfully complete Selenium keyDown for key '"+keycode+"' due to "+ e.getMessage(), e);
		}
	}

	/**
	 * Release a Key by Selenium's Actions API. Release the key pressed by {@link #keyDown(Keys)}.
	 * @param keycode Keys, keycode to release (e.g. <code>Keys.CONTROL</code>)
	 * @throws SeleniumPlusException if fail
	 * @see #keyDown(Keys)
	 */
	public static void keyUp(Keys keycode) throws SeleniumPlusException{
		try {
			WebDriver wd = getWebDriver();
			Actions actions = new Actions(wd);
			actions.keyUp(keycode);
			actions.build().perform();
		} catch (Exception e) {
			throw new SeleniumPlusException("Unable to successfully complete Selenium keyUp for key '"+keycode+"' due to "+ e.getMessage(), e);
		}
	}

	/**
	 * Sets a delay in milliseconds between Robot keystrokes for both local and remote servers.
	 * @param millisDelay between keystrokes.
	 * @throws SeleniumPlusException if we are unable to process the keystrokes successfully.
	 * @see org.safs.robot.Robot#setMillisBetweenKeystrokes(int)
	 **/
	public static void setDelayBetweenKeystrokes(int millisDelay) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		if(getBypassRobot()) throw new SeleniumPlusException("setDelayBetweenKeystrokes is not supported by Selenium.");

		try {
			RemoteDriver wd = null;
			try{ wd = (RemoteDriver) getWebDriver();}catch(Exception x){}
			// change local to match remote for local getMillisBetweenKeystrokes
			IndependantLog.info(debugmsg+" sending '"+String.valueOf(millisDelay)+"' to local Robot.");
			Robot.setMillisBetweenKeystrokes(millisDelay);
			if(wd != null && !wd.isLocalServer()){
				try{
					IndependantLog.info(debugmsg+" sending RMI Agent SetKeyDelay '"+String.valueOf(millisDelay)+"' to RMI Server");
					wd.rmiAgent.remoteSetKeyDelay(millisDelay);
				}catch(Exception e){
					IndependantLog.warn(debugmsg+" Fail RMI Agent SetKeyDelay '"+String.valueOf(millisDelay)+"' due to "+StringUtils.debugmsg(e));
				}
			}
		} catch (Exception e) {
			throw new SeleniumPlusException("Unable to successfully complete setDelayBetweenKeystrokes due to "+ e.getMessage(), e);
		}
	}

	/**
	 * Gets the current delay in milliseconds between Robot keystrokes for both local and remote servers.
	 * @throws SeleniumPlusException if we are unable to process the keystrokes successfully.
	 * @see org.safs.robot.Robot#setMillisBetweenKeystrokes(int)
	 **/
	public static int getDelayBetweenKeystrokes() throws SeleniumPlusException{
		int d = Robot.getMillisBetweenKeystrokes();
		// should be the same for local and remote
		IndependantLog.info("WDLibrary.getDelayBetweenKeystrokes returning  '"+String.valueOf(d)+"' from Robot.");
		return d;
	}

	/**
	 * Set if wait for reaction to "input keys/chars" for both local and remote servers.
	 * @param wait boolean if wait or not.
	 * @throws SeleniumPlusException if fail.
	 * @see org.safs.robot.Robot#setWaitReaction(boolean)
	 **/
	public static void setWaitReaction(boolean wait) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);

		try {
			if(getBypassRobot()){
				RemoteDriver wd = null;
				try{ wd = (RemoteDriver) getWebDriver();}catch(Exception x){}
				if(wd == null || wd.isLocalServer()){
					IndependantLog.info(debugmsg+" sending SetWaitReaction  '"+String.valueOf(wait)+"' to local Robot.");
					Robot.setWaitReaction(wait);
				}else {
					try{
						IndependantLog.info(debugmsg+" sending RMI Agent SetWaitReaction '"+String.valueOf(wait)+"' to RMI Server");
						wd.rmiAgent.remoteWaitReaction(wait);
					}catch(Exception e){
						IndependantLog.warn(debugmsg+" Fail RMI Agent SetWaitReaction '"+String.valueOf(wait)+"' due to "+StringUtils.debugmsg(e));
					}
				}
			}else{
				IndependantLog.info("setWaitReaction is not supported by Selenium.");
			}
		} catch (Exception e) {
			throw new SeleniumPlusException("Unable to successfully complete setWaitReaction due to "+ e.getMessage(), e);
		}
	}
	/**
	 * Set if wait for reaction to "input keys/chars" for both local and remote servers.
	 * @param wait boolean, if wait or not.
	 * @param tokenLength int, the length of a token. Only if the string is longer than this
	 *                         then we wait the reaction after input-keys a certain time
	 *                         indicated by the parameter dealyForToken.
	 * @param dealyForToken int, The delay in millisecond to wait the reaction after input-keys
	 *                           for the string as long as a token.
	 * @param dealy int, The constant delay in millisecond to wait the reaction after input-keys.
	 * @throws SeleniumPlusException if fail.
	 * @see org.safs.robot.Robot#setWaitReaction(boolean, int, int, int)
	 **/
	public static void setWaitReaction(boolean wait, int tokenLength, int dealyForToken, int dealy) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);

		try {
			if(getBypassRobot()){
				RemoteDriver wd = null;
				try{ wd = (RemoteDriver) getWebDriver();}catch(Exception x){}
				if(wd == null || wd.isLocalServer()){
					IndependantLog.info(debugmsg+" sending SetWaitReaction  '"+String.valueOf(wait)+"' to local Robot.");
					Robot.setWaitReaction(wait, tokenLength, dealyForToken, dealy);
				}else {
					try{
						IndependantLog.info(debugmsg+" sending RMI Agent SetWaitReaction '"+String.valueOf(wait)+"' to RMI Server");
						wd.rmiAgent.remoteWaitReaction(wait, tokenLength, dealyForToken, dealy);
					}catch(Exception e){
						IndependantLog.warn(debugmsg+" Fail RMI Agent SetWaitReaction '"+String.valueOf(wait)+"' due to "+StringUtils.debugmsg(e));
					}
				}
			}else{
				IndependantLog.info("setWaitReaction is not supported by Selenium.");
			}
		} catch (Exception e) {
			throw new SeleniumPlusException("Unable to successfully complete setWaitReaction due to "+ e.getMessage(), e);
		}
	}

	/**
	 * Does NOT clear any existing text in the control, but does attempt to insure the window/control has focus.
	 * <br><em>Purpose:</em>
	 * <a href="/sqabasic2000/SeleniumGenericMasterFunctionsReference.htm#detail_InputKeys" alt="inputKeys Keyword Reference" title="inputKeys Keyword Reference">inputKeys</a>
	 * <p>
	 * Attempts to use AWT Robot for keystrokes for both local and remote servers.
	 * If the remote server does NOT have an RMI Server running to receive the request, then we
	 * will attempt to convert SAFS keystrokes to Selenium Actions keystrokes and try that way.
	 * @param we WebElement to send SAFS keystrokes; if null, the keystrokes will be sent to the focused element.
	 * @param keystrokes in SAFS format to type.
	 * @throws SeleniumPlusException if we are unable to process the keystrokes successfully.
	 * @see org.safs.robot.Robot#inputKeys(String)
	 * @see #inputKeysSAFS2Selenium(WebElement)
	 **/
	public static void inputKeys(WebElement we, String keystrokes) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		boolean done = false;
		if(!focus(we)) IndependantLog.warn(debugmsg+" Fail to set focus to webelement "+we);

		try {
			try{
				//Try Robot
				RBT.inputKeys(keystrokes);
				done = true;
			}catch(SeleniumPlusException e){
				IndependantLog.warn(debugmsg+" Failed TypeKeys '"+keystrokes+"' due to "+StringUtils.debugmsg(e));
				String esc = "{esc}";
				String escape = "{escape}";
				String lc = keystrokes.toLowerCase();
				if(lc.indexOf(esc)>-1 || lc.indexOf(escape)>-1){
					IndependantLog.warn(debugmsg+" Selenium cannot handle "+esc+" properly!");
					if(lc.equals(esc) || lc.equals(escape)){
						IndependantLog.warn(debugmsg+" We risk to handle "+esc+" by Robot!");
						RBT._typeKeys(esc);
						done = true;
					}
				}
			}

			if(!done){
				//Try selenium
				inputKeysSAFS2Selenium(we, keystrokes);
			}

		} catch (Exception e) {
			throw new SeleniumPlusException("Unable to successfully complete InputKeys due to "+ e.getMessage(), e);
		}
	}

	/**
	 * Does NOT clear any existing text in the control, but does attempt to insure the window/control has focus.
	 * <br><em>Purpose:</em>
	 * <a href="/sqabasic2000/SeleniumGenericMasterFunctionsReference.htm#detail_InputCharacters" alt="inputCharacters Keyword Reference" title="inputCharacters Keyword Reference">inputCharacters</a>
	 * @param we WebElement to send characters; if null, the keystrokes will be sent to the focused element.
	 * @param keystrokes/plain text to type.
	 * @throws SeleniumPlusException if we are unable to process the keystrokes successfully.
	 * @see org.safs.robot.Robot#inputKeys(String)
	 **/
	public static void inputChars(WebElement we, String keystrokes) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		boolean done = false;

		if(!focus(we)) IndependantLog.warn(debugmsg+" Fail to set focus to webelement "+we);

		try{
			//Try Robot
			RBT.inputChars(keystrokes);
			done = true;
		}catch(Exception e){
			IndependantLog.warn(debugmsg+" Fail InputChars '"+keystrokes+"' due to "+StringUtils.debugmsg(e));
		}

		if(!done){
			IndependantLog.info(debugmsg+" InputChars '"+keystrokes+"' by selenium.");
			try{
				//WebElement.sendKeys() doesn't work sometimes.
				we.sendKeys(keystrokes);
				done =true;
			}catch(Exception e){
				IndependantLog.warn(debugmsg+" Failed to InputChars '"+keystrokes+"' by WebElement.sendKeys(), due to "+StringUtils.debugmsg(e));
			}
			if(!done){
				try{
					//Try another selenium way to send keys.
					Actions actions = new Actions(getWebDriver());
					actions.sendKeys(we, keystrokes).build().perform();
					done = true;
				}catch(Exception x){
					IndependantLog.warn(debugmsg+" Failed to InputChars '"+keystrokes+"' by Actions.sendKeys(), due to "+StringUtils.debugmsg(x));
				}
			}
		}

		if(!done){
			throw new SeleniumPlusException("Unable to successfully complete InputChars.");
		}
	}

	/**
	 * Clear the clipboard on local machine or on machine where the RMI server is running.
	 * @throws SeleniumPlusException
	 */
	public static void clearClipboard() throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		if(getBypassRobot()) throw new SeleniumPlusException("clearClipboard is not supported by Selenium.");

		try{
			RemoteDriver wd = null;
			try{ wd = (RemoteDriver) getWebDriver();}catch(Exception x){}
			if(wd == null || wd.isLocalServer()){
				IndependantLog.info(debugmsg+" clear local clipboard by Robot.");
				Robot.clearClipboard();
			}else {
				try{
					IndependantLog.info(debugmsg+" clear clipboard on RMI Server");
					wd.rmiAgent.clearClipboard();
				}catch(Exception e){
					IndependantLog.error(debugmsg+" Fail to clear RMI Server clipboard due to "+StringUtils.debugmsg(e));
					throw e;
				}
			}
		} catch (Exception e) {
			throw new SeleniumPlusException("Unable to successfully complete ClearClipboard due to "+ e.getMessage());
		}
	}

	/**
	 * Set content to the clipboard on local machine or on machine where the RMI server is running.
	 * @param content String, the content to set to clipboard
	 * @throws SeleniumPlusException
	 */
	public static void setClipboard(String content) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		if(getBypassRobot()) throw new SeleniumPlusException("setClipboard is not supported by Selenium.");

		try{
			RemoteDriver wd = null;
			try{ wd = (RemoteDriver) getWebDriver();}catch(Exception x){}
			if(wd == null || wd.isLocalServer()){
				IndependantLog.info(debugmsg+" set '"+content+"' to local clipboard by Robot.");
				Robot.setClipboard(content);
			}else {
				try{
					IndependantLog.info(debugmsg+" set '"+content+"' to clipboard on RMI Server");
					wd.rmiAgent.setClipboard(content);
				}catch(Exception e){
					IndependantLog.error(debugmsg+" Fail to set '"+content+"' to RMI Server clipboard due to "+StringUtils.debugmsg(e));
					throw e;
				}
			}
		} catch (Exception e) {
			throw new SeleniumPlusException("Unable to successfully complete SetClipboard due to "+ e.getMessage());
		}
	}

	/**
	 * Get the content from the clipboard on local machine or on machine where the RMI server is running.
	 * @param dataFlavor DataFlavor, the data flavor for the content in clipboard
	 * @return Object, the content of the clipboard
	 * @throws SeleniumPlusException
	 */
	public static Object getClipboard(DataFlavor dataFlavor) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		if(getBypassRobot()) throw new SeleniumPlusException("getClipboard is not supported by Selenium.");

		try{
			RemoteDriver wd = null;
			try{ wd = (RemoteDriver) getWebDriver();}catch(Exception x){}
			if(wd == null || wd.isLocalServer()){
				IndependantLog.info(debugmsg+" get content from local clipboard by Robot.");
				return Robot.getClipboard(dataFlavor);
			}else {
				try{
					IndependantLog.info(debugmsg+" get content from clipboard on RMI Server");
					return wd.rmiAgent.getClipboard(dataFlavor);
				}catch(Exception e){
					IndependantLog.error(debugmsg+" Fail to get content from RMI Server clipboard due to "+StringUtils.debugmsg(e));
					throw e;
				}
			}
		} catch (Exception e) {
			throw new SeleniumPlusException("Unable to successfully complete GetClipboard due to "+ e.getMessage());
		}
	}

	/**
	 * Get the screen rectangle for a WebElement object.
	 * @param we WebElement, the webelement object to get screen rectangle
	 * @return Rectangle, the screen rectangle of a webelement object
	 * @throws SeleniumPlusException
	 */
	public static Rectangle getRectangleOnScreen(WebElement we) throws SeleniumPlusException{
		Rectangle rectangle = null;
		try {
			Point p = getScreenLocation(we);
			org.openqa.selenium.Dimension dim = we.getSize();
			rectangle = new Rectangle();
			rectangle.setBounds(p.x, p.y, dim.getWidth(), dim.getHeight());
			return rectangle;
		} catch (Exception e) {
			throw new SeleniumPlusException("Fail get screen rectangle for webelement, due to "+StringUtils.debugmsg(e));
		}
	}

	/**
	 *
	 * @param key Keys, the selenium Keys value
	 * @return int the value of java KeyEvent
	 * @throws SeleniumPlusException
	 */
	static int toJavaKeyCode(Keys key) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(WDLibrary.class, "toJavaKeyCode");
		if(Keys.SHIFT.equals(key)) return KeyEvent.VK_SHIFT;
		else if(Keys.LEFT_SHIFT.equals(key)) return KeyEvent.VK_SHIFT;
		else if(Keys.CONTROL.equals(key)) return KeyEvent.VK_CONTROL;
		else if(Keys.LEFT_CONTROL.equals(key)) return KeyEvent.VK_CONTROL;
		else if(Keys.ALT.equals(key)) return KeyEvent.VK_ALT;
		else if(Keys.LEFT_ALT.equals(key)) return KeyEvent.VK_ALT;
		else{
			String msg = " No handled key '"+(key==null?"null":key.toString())+"'.";
			IndependantLog.debug(debugmsg+msg);
			throw new SeleniumPlusException(msg);
		}
	}

	/**
	 * Capture entire current HTML page
	 * @param image - Image full path with name.
	 * @throws SeleniumPlusException
	 */
	public static void captureScreen(String image) throws SeleniumPlusException {

	    try {
	        File source = ((TakesScreenshot)lastUsedWD).getScreenshotAs(OutputType.FILE);
	        FileUtils.copyFile(source, new File(image));
	    }
	    catch(IOException e) {
	    	throw new SeleniumPlusException("Failed to capture screenshot "+ e.getMessage());
	    }
	    catch(NullPointerException e){
	    	throw new SeleniumPlusException("Failed to capture screenshot "+ e.getMessage());
	    }
	}

	/**
	 * @param we WebElement, the component to hover mouse
	 * @param point Point, the position relative to the component to hover the mouse
	 * @param millisStay int, in milliseconds, the period to hover the mouse; the mouse will
	 *                        be moved out of screen if it expires.<br/>
	 *                        if it equals {@link Constants#TIMEOUT_HOVERMOUSE_STAY_FOREVER}, then
	 *                        the mouse will always stay there forever.
	 *
	 * @throws SeleniumPlusException if the hover fail
	 */
	public static void mouseHover(WebElement we, Point point, int millisStay) throws SeleniumPlusException {

		try {
			//Hover mouse on the webelement
			Actions action = new Actions(lastUsedWD);
			action.moveToElement(we);
			if(point!=null){
				int xOffset = point.x - we.getSize().width/2;
				int yOffset = point.y - we.getSize().height/2;
				action.moveByOffset(xOffset, yOffset);
			}
			action.build().perform();

			if(Constants.TIMEOUT_HOVERMOUSE_STAY_FOREVER!=millisStay){
				//Pause a while
				StringUtilities.sleep(millisStay);

				//Move out the mouse
				if(!getBypassRobot()){
					Robot.getRobot().mouseMove(-Robot.SCREENSZIE.width, -Robot.SCREENSZIE.height);
				}else{
					//TODO by Selenium action
					//action.moveByOffset(-Robot.SCREENSZIE.width, -Robot.SCREENSZIE.height);//This will throw exception
					//action.build().perform();
				}
			}

		}catch(Exception e) {
			IndependantLog.warn(StringUtils.debugmsg(false)+"Failed to hover mouse by Selenium API: "+ StringUtils.debugmsg(e));
			if(!getBypassRobot()){
				Point screenPoint = new Point(point.x, point.y);
				try{
					translatePoint(we, screenPoint);
					IndependantLog.warn(StringUtils.debugmsg(false)+"Try SAFS Robot to hover mouse at screen point ["+screenPoint.x+","+screenPoint.y+"]");
					Robot.mouseHover(screenPoint, millisStay);
				}catch(Exception e2){
					throw new SeleniumPlusException("Failed to hover mouse at point ["+point.x+","+point.y+"] relative to webelement.");
				}
			}
		}
	}

	/**
	 * Perform a left-drag from start point to end point relative to webelement (LeftUp corner)
	 * with the "ALT" key pressed during the drag.
	 * @param we WebElement, the component relative to which to drag
	 * @param start Point, the start point relative to the webelement
	 * @param end Point, the end point relative to the webelement
	 * @throws SeleniumPlusException
	 */
	public static void altLeftDrag(WebElement we, Point start, Point end) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		boolean done = false;
		IndependantLog.debug(debugmsg+" drag from "+start+" to "+end+" relative to webelement.");

		try{
			if(!getBypassRobot()){
				IndependantLog.debug(debugmsg+"Try to drag by Robot.");
				translatePoints(we, start, end);
				Robot.altLeftDrag(start, end);
				done = true;
			}

		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Failed to drag by SAFS Robot: "+ StringUtils.debugmsg(e));
		}

		if(!done){
			try{
				IndependantLog.debug(debugmsg+"Try to drag by Selenium API.");
				Actions actions = new Actions(lastUsedWD);
				actions = actions.keyDown(Keys.ALT);
				actions = moveToElement(actions, we, start.x, start.y);
				actions.clickAndHold().moveByOffset((end.x-start.x), (end.y-start.y)).release().keyUp(Keys.ALT).build().perform();
			}catch(Exception e1){
				IndependantLog.warn(debugmsg+"Failed to drag by Selenium API: "+ StringUtils.debugmsg(e1));
				throw new SeleniumPlusException("Failed to drag.");
			}
		}
	}
	/**
	 * Perform a left-drag from start point to end point relative to webelement (LeftUp corner)
	 * with the "CONTROL" and "ALT" key pressed during the drag.
	 * @param we WebElement, the component relative to which to drag
	 * @param start Point, the start point relative to the webelement
	 * @param end Point, the end point relative to the webelement
	 * @throws SeleniumPlusException
	 */
	public static void ctrlAltLeftDrag(WebElement we, Point start, Point end) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		boolean done = false;
		IndependantLog.debug(debugmsg+" drag from "+start+" to "+end+" relative to webelement.");

		try{
			if(!getBypassRobot()){
				IndependantLog.debug(debugmsg+"Try to drag by Robot.");
				translatePoints(we, start, end);
				Robot.ctrlAltLeftDrag(start, end);
				done = true;
			}

		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Failed to drag by SAFS Robot: "+ StringUtils.debugmsg(e));
		}

		if(!done){
			try{
				IndependantLog.debug(debugmsg+"Try to drag by Selenium API.");
				Actions actions = new Actions(lastUsedWD);
				actions = actions.keyDown(Keys.CONTROL).keyDown(Keys.ALT);
				actions = moveToElement(actions, we, start.x, start.y);
				actions.clickAndHold().moveByOffset((end.x-start.x), (end.y-start.y)).release().keyUp(Keys.ALT).keyUp(Keys.CONTROL).build().perform();

			}catch(Exception e1){
				IndependantLog.warn(debugmsg+"Failed to drag by Selenium API: "+ StringUtils.debugmsg(e1));
				throw new SeleniumPlusException("Failed to drag.");
			}
		}
	}
	/**
	 * Perform a left-drag from start point to end point relative to webelement (LeftUp corner)
	 * with the "CONTROL" key pressed during the drag.
	 * @param we WebElement, the component relative to which to drag
	 * @param start Point, the start point relative to the webelement
	 * @param end Point, the end point relative to the webelement
	 * @throws SeleniumPlusException
	 */
	public static void ctrlLeftDrag(WebElement we, Point start, Point end) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		boolean done = false;

		IndependantLog.debug(debugmsg+" drag from "+start+" to "+end+" relative to webelement.");
		try{
			if(!getBypassRobot()){
				IndependantLog.debug(debugmsg+"Try to drag by Robot.");
				translatePoints(we, start, end);
				Robot.ctrlLeftDrag(start, end);
				done = true;
			}

		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Failed to drag by SAFS Robot: "+ StringUtils.debugmsg(e));
		}

		if(!done){
			try{
				IndependantLog.debug(debugmsg+"Try to drag by Selenium API.");
				Actions actions = new Actions(lastUsedWD);
				actions = actions.keyDown(Keys.CONTROL);
				actions = moveToElement(actions, we, start.x, start.y);
				actions.clickAndHold().moveByOffset((end.x-start.x), (end.y-start.y)).release().keyUp(Keys.CONTROL).build().perform();

			}catch(Exception e1){
				IndependantLog.warn(debugmsg+"Failed to drag by Selenium API: "+ StringUtils.debugmsg(e1));
				throw new SeleniumPlusException("Failed to drag.");
			}
		}
	}
	/**
	 * Perform a left-drag from start point to end point relative to webelement (LeftUp corner)
	 * with the "CONTROL" and "SHIFT" key pressed during the drag.
	 * @param we WebElement, the component relative to which to drag
	 * @param start Point, the start point relative to the webelement
	 * @param end Point, the end point relative to the webelement
	 * @throws SeleniumPlusException
	 */
	public static void ctrlShiftLeftDrag(WebElement we, Point start, Point end) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		boolean done = false;
		IndependantLog.debug(debugmsg+" drag from "+start+" to "+end+" relative to webelement.");

		try{
			if(!getBypassRobot()){
				IndependantLog.debug(debugmsg+"Try to drag by Robot.");
				translatePoints(we, start, end);
				Robot.ctrlShiftLeftDrag(start, end);
				done = true;
			}

		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Failed to drag by SAFS Robot: "+ StringUtils.debugmsg(e));
		}

		if(!done){
			try{
				IndependantLog.debug(debugmsg+"Try to drag by Selenium API.");
				Actions actions = new Actions(lastUsedWD);
				actions = actions.keyDown(Keys.CONTROL).keyDown(Keys.SHIFT);
				actions = moveToElement(actions, we, start.x, start.y);
				actions.clickAndHold().moveByOffset((end.x-start.x), (end.y-start.y)).release().keyUp(Keys.SHIFT).keyUp(Keys.CONTROL).build().perform();

			}catch(Exception e1){
				IndependantLog.warn(debugmsg+"Failed to drag by Selenium API: "+ StringUtils.debugmsg(e1));
				throw new SeleniumPlusException("Failed to drag.");
			}
		}
	}
	/**
	 * Perform a left-drag from start point to end point relative to webelement (LeftUp corner)
	 * with the "SHIFT" key pressed during the drag.
	 * @param we WebElement, the component relative to which to drag
	 * @param start Point, the start point relative to the webelement
	 * @param end Point, the end point relative to the webelement
	 * @throws SeleniumPlusException
	 */
	public static void shiftLeftDrag(WebElement we, Point start, Point end) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		boolean done = false;

		IndependantLog.debug(debugmsg+" drag from "+start+" to "+end+" relative to webelement.");
		try{
			if(!getBypassRobot()){
				IndependantLog.debug(debugmsg+"Try to drag by Robot.");
				translatePoints(we, start, end);
				Robot.shiftLeftDrag(start, end);
				done = true;
			}

		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Failed to drag by SAFS Robot: "+ StringUtils.debugmsg(e));
		}

		if(!done){
			try{
				IndependantLog.debug(debugmsg+"Try to drag by Selenium API.");
				Actions actions = new Actions(lastUsedWD);
				actions = actions.keyDown(Keys.SHIFT);
				actions = moveToElement(actions, we, start.x, start.y);
				actions.clickAndHold().moveByOffset((end.x-start.x), (end.y-start.y)).release().keyUp(Keys.SHIFT).build().perform();

			}catch(Exception e1){
				IndependantLog.warn(debugmsg+"Failed to drag by Selenium API: "+ StringUtils.debugmsg(e1));
				throw new SeleniumPlusException("Failed to drag.");
			}
		}
	}

	/**
	 * Perform a right-drag from start point to end point relative to webelement (LeftUp corner).
	 * @param we WebElement, the component relative to which to drag
	 * @param start Point, the start point relative to the webelement
	 * @param end Point, the end point relative to the webelement
	 * @throws SeleniumPlusException
	 */
	public static void rightDrag(WebElement we, Point start, Point end) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		boolean done = false;

		try{
			if(!getBypassRobot()){
				IndependantLog.debug(debugmsg+" drag from "+start+" to "+end+" relative to webelement.");
				translatePoints(we, start, end);
				Robot.rightDrag(start, end);
				done = true;
			}

		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Failed to drag by SAFS Robot: "+ StringUtils.debugmsg(e));
		}

		if(!done){
			try{
				IndependantLog.debug(debugmsg+"Try to right drag by Selenium API.");
				Actions actions = new Actions(lastUsedWD);
				actions = moveToElement(actions, we, start.x, start.y);
				actions = seleniumW3CMouseDown(actions, MouseButton.RIGHT);
				actions = actions.moveByOffset((end.x-start.x), (end.y-start.y));
				actions = seleniumW3CMouseUp(actions, MouseButton.RIGHT);
				actions.build().perform();
			}catch(Exception e1){
				IndependantLog.warn(debugmsg+"Failed to drag by Selenium API: "+ StringUtils.debugmsg(e1));
				throw new SeleniumPlusException("Failed to drag.");
			}
		}
	}

	/**
	 * Perform a left-drag from start point to end point relative to webelement (LeftUp corner).
	 * @param we WebElement, the component relative to which to drag
	 * @param start Point, the start point relative to the webelement
	 * @param end Point, the end point relative to the webelement
	 * @throws SeleniumPlusException
	 */
	public static void leftDrag(WebElement we, Point start, Point end) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		boolean done = false;

		IndependantLog.debug(debugmsg+" drag from "+start+" to "+end+" relative to webelement.");
		try{
			if(!getBypassRobot()){
				IndependantLog.debug(debugmsg+"Try to drag by Robot.");
				translatePoints(we, start, end);
				Robot.leftDrag(start, end);
				done = true;
			}
		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Failed to drag by SAFS Robot: "+ StringUtils.debugmsg(e));
			done = false;
		}
		if(!done){
			try{
				IndependantLog.debug(debugmsg+"Try to drag by Selenium API.");
				Actions actions = new Actions(lastUsedWD);
				actions = moveToElement(actions, we, start.x, start.y);
				actions.clickAndHold().moveByOffset((end.x-start.x), (end.y-start.y)).release().build().perform();

			}catch(Exception e1){
				IndependantLog.warn(debugmsg+"Failed to drag by Selenium API: "+ StringUtils.debugmsg(e1));
				throw new SeleniumPlusException("Failed to drag.");
			}
		}
	}

	/**
	 * @param fromElem WebElement, from where the drag begins
	 * @param toElem WebElement, to where the drag stops
	 * @param offsets String, the offsets relative to 'fromElem' and to 'toElem'<br>
	 *                        in format x1;y1;x2;y2 or x1,y1,x2,y2 or Coords=x1;y1;x2;y2  or Coords=x1,y1,x2,y2<br>
	 *                        each offset is in pixel or in percentage, for example 15 or 30%<br>
	 *                        Example "40, 20, 5%, 5%", "6%, 6%, 21%, 21%, or "40, 25, 30, 45"<br>
	 * @param dndReleaseDelay int, in milliseconds, the delay before releasing the mouse
	 * @return Point[], 2 points for the screen location of the from-point and to-point
	 * @throws SeleniumPlusException
	 */
	public static Point[] dragTo(WebElement fromElem, WebElement toElem, String[] offsets, int dndReleaseDelay /* milliseconds*/) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		boolean done = false;

		if(offsets==null || offsets.length<4){
			throw new SeleniumPlusException(" Offsets '"+offsets+"' is not valid! The valid examples could be an array like ['20%','10%','%50','%60'], ['30','55','70','80'], or even ['20%','10%','70','80'].");
		}

		IndependantLog.debug(debugmsg+" drag from ("+offsets[0]+","+offsets[1]+") (relative to webelement "+fromElem+") to ("+offsets[2]+","+offsets[3]+") (relative to webelement "+toElem+").");
		//calculate the 2 screen locations by adding the offset to fromElement and toElement
		Point[] points = new Point[2];/* 0:fromPoint, 1:toPoint */
		points[0] = getElementOffsetScreenLocation(fromElem, offsets[0], offsets[1]);
		points[1] = getElementOffsetScreenLocation(toElem, offsets[2], offsets[3]);

		try{
			if(!getBypassRobot()){
				IndependantLog.debug(debugmsg+"Try to drag by Robot.");
				Robot.leftDrag(points[0], points[1], dndReleaseDelay);
				done = true;
			}
		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Failed to drag by SAFS Robot: "+ StringUtils.debugmsg(e));
			done = false;
		}
		if(!done){
			try{
				IndependantLog.debug(debugmsg+"Try to drag by Selenium API.");

				Point relatvieStartPoint = StringUtils.convertCoords(Arrays.copyOfRange(offsets, 0, 2), toJavaRectangle(fromElem.getRect()));
//				Point relatvieEndPoint = StringUtils.convertCoords(Arrays.copyOfRange(offsets, 2, 4), toJavaRectangle(toElem.getRect()));

				//Calculate the offset moving from fromElement to toElement
				Point offset = new Point(points[1].x-points[0].x, points[1].y-points[0].y);

				Actions actions = new Actions(lastUsedWD);
				actions = moveToElement(actions, fromElem, relatvieStartPoint.x, relatvieStartPoint.y);
				actions.clickAndHold();
				actions.moveByOffset(offset.x, offset.y).pause(dndReleaseDelay);
				actions.release();
				actions.build().perform();

//				Actions actions = mouseDrag(points[0], points[1], MouseButton.LEFT, dndReleaseDelay);
//				actions.build().perform();

			}catch(Exception e1){
				IndependantLog.warn(debugmsg+"Failed to drag by Selenium API: "+ StringUtils.debugmsg(e1));
				throw new SeleniumPlusException("Failed to drag.");
			}
		}

		return points;
	}

	public static java.awt.Rectangle toJavaRectangle(org.openqa.selenium.Rectangle rect){
		java.awt.Rectangle javaRect = new java.awt.Rectangle(rect.x, rect.y, rect.getWidth(), rect.getHeight());
		return javaRect;
	}

	/** W3C selenium mouse, be careful, this mouse is a mouse independant from the selenium's 'default mouse',
	 *  user needs to handle everything including mouse-move, click etc. and it can not be used with the instance of
	 *  Actions which is using the 'default mouse'.
	 */
	private static final PointerInput seleniumMouse = new PointerInput(MOUSE, "selenium mouse in SAFS");

//	/**
//	 * To make mouse drag correctly, after mouse key is pressed down,
//	 * the mouse needs to be moved slightly so that the drag action will be triggered.<br>
//	 * If the drag action cannot be triggered, this field needs to be modified by
//	 * method {@link #setDragStartPointOffset(int, int)}.
//	 */
//	private static Point dragStartPointOffset = new Point(3, 3);
//	private static Point dragEndPointOffset = new Point(-3, -3);
//
//	private static Actions mouseDrag(java.awt.Point start, java.awt.Point end, PointerInput.MouseButton button, int dndReleaseDelay) throws java.awt.AWTException{
//		Log.info("Selenium mouseDrag from:"+ start +" to:"+ end +" using button mask "+ button.toString());
//		Actions actions = new Actions(lastUsedWD);
//		PointerInput mouse = getMouse(actions);
//		if(mouse==null) mouse = seleniumMouse;
//		actions.moveByOffset(start.x, start.y);
//		actions.pause(400);
//		actions.tick(mouse.createPointerDown(button.asArg()));
//		actions.pause(400);
//		actions.moveByOffset(start.x+dragStartPointOffset.x, start.y+dragStartPointOffset.y);
//		actions.pause(150);
//		actions.moveByOffset(end.x+dragEndPointOffset.x, end.y+dragEndPointOffset.y);
//		actions.pause(150);
//		actions.moveByOffset(end.x, end.y);
//		actions.pause(dndReleaseDelay);
//		actions.tick(mouse.createPointerUp(button.asArg()));
//		return actions;
//	}

	private static Actions seleniumW3CMouseClick(Actions actions, PointerInput.MouseButton button) {

		PointerInput mouse = getMouse(actions);
		if(mouse==null) mouse = seleniumMouse;

		actions.pause(100);
		actions.tick(mouse.createPointerDown(button.asArg()));
		actions.pause(100);
		actions.tick(mouse.createPointerUp(button.asArg()));
		actions.pause(100);
		return actions;
	}
	private static Actions seleniumW3CMouseDown(Actions actions, PointerInput.MouseButton button) {
		PointerInput mouse = getMouse(actions);
		if(mouse==null) mouse = seleniumMouse;
		actions.pause(100);
		actions.tick(mouse.createPointerDown(button.asArg()));
		actions.pause(100);
		return actions;
	}
	private static Actions seleniumW3CMouseUp(Actions actions, PointerInput.MouseButton button) {
		PointerInput mouse = getMouse(actions);
		if(mouse==null) mouse = seleniumMouse;
		actions.pause(100);
		actions.tick(mouse.createPointerUp(button.asArg()));
		actions.pause(100);
		return actions;
	}

	/**
	 * @param actions Actions
	 * @return PointerInput, the Mouse (W3C implementation) in Actions
	 */
	private static PointerInput getMouse(Actions actions){
		PointerInput mouse = null;
		try {
			Field field = actions.getClass().getDeclaredField("defaultMouse");
			field.setAccessible(true);
			Object result = field.get(actions);

			if(result instanceof PointerInput){
				mouse = (PointerInput) result;
			}
		} catch (Exception e) {
			IndependantLog.error("Met "+e.toString());
		}
		return mouse;
	}

	public final static String BROWSER_DRIVER_IMPLEMENTATION_JSON = "JSON_WIRE_PROTOCOL";
	public final static String BROWSER_DRIVER_IMPLEMENTATION_W3C  = "W3C_PROTOCOL";
	public final static String DEFAULT_BROWSER_DRIVER_IMPLEMENTATION = BROWSER_DRIVER_IMPLEMENTATION_JSON;
	/** If the browser's driver is implemented by W3C protocol */
	public static boolean isW3Cimplementation = DEFAULT_BROWSER_DRIVER_IMPLEMENTATION.equals(BROWSER_DRIVER_IMPLEMENTATION_W3C);

	/**
	 * Move to web-element by some offsets.<br>
	 * <b>NOTE: Normally the offset is calculate from the top-left corner.
	 *          But with "W3C protocol", the start location becomes the center.
	 *          To keep the compatibility, user can set {@link #isW3Cimplementation} to true.</b>
	 */
	private static Actions moveToElement(Actions actions, WebElement we, int xOffset, int yOffset){

		if(isW3Cimplementation){
			//with "w3c protocol", the moveToElement() will calculate the offset from the component's center
			//this is not consistent with the selenium's API specification, we made this fix to get it consistent with selenium's API.
			int topleftX =  (we.getSize().getWidth()/2) - we.getSize().getWidth();
			int topleftY = ((we.getSize().getHeight()/2) - we.getSize().getHeight());
			xOffset += topleftX;
			yOffset += topleftY;
		}

		return actions.moveToElement(we, xOffset, yOffset);
	}

	/**
	 * Adjust the relative coordinate to screen absolute coordinate according to the webelement.
	 * @param we WebElement, the component relative to which to adjust coordinate
	 * @param start Point, the start relative point, it will bring back the adjusted screen coordinate
	 * @param end Point, the end relative point, it will bring back the adjusted screen coordinate
	 * @throws Exception
	 */
	private static void translatePoints(WebElement we, Point start, Point end) throws Exception{
		translatePoint(we, start);
		translatePoint(we, end);
	}

	/**
	 * Adjust the relative coordinate to screen absolute coordinate according to the webelement.
	 * @param we WebElement, the component relative to which to adjust coordinate
	 * @param point Point, the relative point, it will bring back the adjusted screen coordinate
	 * @throws Exception
	 */
	private static void translatePoint(WebElement we, Point point) throws Exception{
		String debugmsg = StringUtils.debugmsg(false);

		Rectangle rec = WDLibrary.getRectangleOnScreen(we);
		IndependantLog.debug(debugmsg+" webelement screen location is ["+rec.getX()+","+rec.getY()+"]");

		//Translate the point according to webelement's left-up corner
		point.translate(rec.x, rec.y);

		//check and keep in screen boundaries
		if(point.x < 0) point.x = 0;
		if(point.y < 0) point.y = 0;
		if(point.x > ImageUtils.getScreenWidth()-1) point.x = ImageUtils.getScreenWidth()-1;
		if(point.y > ImageUtils.getScreenHeight()-1) point.y = ImageUtils.getScreenHeight()-1;
	}

    /**
     * Given the element, and the (offsetX, offsetY) relative to element.
     * This function will calculate the offset point screen coordination.
     *
     * @param element WebElement, the element relative to which the coordination will be calculated.
     * @param offsetX String, the offset on x axis, in pixel or in percentage, for example 15 or 30%.
     * @param offsetX String, the offset on y axis, in pixel or in percentage, for example 45 or 50%.
     *
     * @return Point, the offset point screen coordination; or null if any exception occurred.
     *
     **/
    public static Point getElementOffsetScreenLocation(WebElement element, String offsetX, String offsetY){
    	String debugmsg = StringUtils.debugmsg(false);
    	Point screenLoc = null;
    	Dimension dimemsion = null;
    	double dx, dy;

    	try {
    		screenLoc = WDLibrary.getScreenLocation(element);
    		dimemsion = element.getSize();

    		//calc coords according to the offset and element's location and dimension
    		dx = ImageUtils.calculateAbsoluteCoordinate(screenLoc.getX(), dimemsion.getWidth(), offsetX);
    		dy = ImageUtils.calculateAbsoluteCoordinate(screenLoc.getY(), dimemsion.getHeight(), offsetY);

    		return new Point((int)dx, (int)dy);
    	}catch (Exception e) {
    		IndependantLog.debug(debugmsg +" (offsetX, offsetX)=("+offsetX+","+offsetY+"). Met Exception", e);

    		try{
    			IndependantLog.debug(debugmsg +" Use the default offset ("+Constants.OFFSET_FIFTY_PERCENT+","+Constants.OFFSET_FIFTY_PERCENT+")");
        		dx = ImageUtils.calculateAbsoluteCoordinate(screenLoc.getX(), dimemsion.getWidth(), Constants.OFFSET_FIFTY_PERCENT);
        		dy = ImageUtils.calculateAbsoluteCoordinate(screenLoc.getY(), dimemsion.getHeight(), Constants.OFFSET_FIFTY_PERCENT);
        		return new Point((int)dx, (int)dy);
    		}catch(Exception ex){
    			IndependantLog.error(debugmsg +"Met Exception", e);
    		}

    		return null;
    	}
    }

	/**
	 * Capture component image
	 * @param we - WebElement object.
	 * @param imgName - Image full path with name
	 * @throws Exception
	 */
	public static void captureScreen(WebElement we, String imgName, String fileformat) throws SeleniumPlusException{
		try {
			Rectangle rect = new Rectangle(we.getLocation().x, we.getLocation().y, we.getSize().width, we.getSize().height);
			BufferedImage img = captureBrowserArea(rect);
			File outputfile = new File(imgName + "." + fileformat);
			ImageIO.write(img, fileformat, outputfile);
		} catch (Exception e){
			throw new SeleniumPlusException("Failed to capture screenshot "+ e.getMessage());
		}
	}

	/**
	 * Capture the image on browser according to the rectangle relative to the browser.
	 * @param rectangle Rectangle, within which to capture the image.
	 * @return BufferedImage, the image within the rectangle on a browser, or the whole browser if rectangle is null.
	 * @throws SeleniumPlusException
	 */
	public static BufferedImage captureBrowserArea(Rectangle rectangle) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		try {
			//Get the whole page image
			byte[] imageBytes = ((TakesScreenshot)lastUsedWD).getScreenshotAs(OutputType.BYTES);
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
			//img = new PngImage().read(new ByteArrayInputStream(imageBytes), true);
			//Get sub image according to the rectangle
			if(rectangle==null) return img;
			else{
				IndependantLog.debug(debugmsg+" the initial subarea is "+rectangle);
				//If the rectangle is beyond the browser, throw exception
				if(rectangle.x>=img.getWidth() || rectangle.y>=img.getHeight()){
					throw new SeleniumPlusException("The component is totally outside of browser!");
				}
				if(rectangle.x<0){
					IndependantLog.debug(debugmsg+" subarea x coordinate should NOT be negative, set it to 0.");
					rectangle.x=0;
				}
				if(rectangle.y<0){
					IndependantLog.debug(debugmsg+" subarea y coordinate should NOT be negative, set it to 0.");
					rectangle.y=0;
				}

				//if the rectangle is larger than the captured browser image, then
				//we need to reduce the rectangle
				int tempW = rectangle.x+rectangle.width;
				int tempH = rectangle.y+rectangle.height;
				if(tempW>img.getWidth()) rectangle.width=img.getWidth()-rectangle.x;
				if(tempH>img.getHeight()) rectangle.height=img.getHeight()-rectangle.y;

				if(tempW>img.getWidth() || tempH>img.getHeight()){
					IndependantLog.debug(debugmsg+" subarea has been adjusted to "+rectangle);
				}
				return img.getSubimage(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
			}
		} catch (Exception e){
			String message = (rectangle==null? "of whole browser":"on browser area '"+rectangle+"'" );
			message = "Failed to capture screenshot "+message;
			message += " due to "+ e.getMessage();
			IndependantLog.error(debugmsg+message);
			throw new SeleniumPlusException(message);
		}
	}

	/**
	 * Start browser
	 * @param BrowserName String, Browser name such as InternetExplorer, Chrome and FireFox.
	 * @param Url String, Url including HTTP protocol prefix.
	 * @param Id String, Id or Title of the Browser in case of two instances needs.
	 * @param timeout String, Implicit time out to be waited before throw exception.
	 * @param isRemote String, Start interactive testcase development mode.
	 * @throws SeleniumPlusException
	 * @see {@link #startBrowser(String, String, String, int, boolean, Map)}
	 */
	public static void startBrowser(String BrowserName, String Url, String Id, int timeout, boolean isRemote) throws SeleniumPlusException{
		startBrowser(BrowserName, Url, Id, timeout, isRemote, null);
	}

	private static String getSystemProperty(String property, String defaultValue){
		String value = System.getProperty(property);
		if (!StringUtils.isValid(value)) value = defaultValue;
		return value;
	}

	private static long getSystemProperty(String property, long defaultValue){
		long value = defaultValue;
		String tempValue = null;
		try{
			tempValue = System.getProperty(property);
			if(StringUtils.isValid(tempValue)){
				value = Long.parseLong(tempValue);
			}
		}catch(NumberFormatException e){
			IndependantLog.warn("Bad value '"+tempValue+"' is provided for property '"+property+"'");
		}
		return value;
	}

	/**
	 * Start browser
	 * <p>
	 * Expects System Properties 'selenium.host' and 'selenium.port' to be set.<br>
	 * Otherwise, defaults to 'localhost' on port '4444'.
	 * <p>
	 * @param browserName String, Browser name such as InternetExplorer, Chrome and FireFox.
	 * @param Url String, Url including HTTP protocol prefix.
	 * @param Id String, Id or Title of the Browser in case of two instances needs.
	 * @param timeout String, Implicit time out to be waited before throw exception.
	 * @param isRemote String, Start interactive testcase development mode.
	 * @param extraParameters Map<String,Object>, can be used to pass more browser parameters, such as a firefox profile to use.
	 * @throws SeleniumPlusException
	 */
	public static void startBrowser(String browserName, String Url, String Id, int timeout, boolean isRemote, Map<String, Object> extraParameters) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		//previousDriver is the possible WebDriver with the same ID stored in the Cache.
		WebDriver previousDriver = null;

		String host = getSystemProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_HOST, SelectBrowser.DEFAULT_SELENIUM_HOST);
		String port = getSystemProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_PORT, SelectBrowser.DEFAULT_SELENIUM_PORT);
		String connectionTestCommand = getSystemProperty(SeleniumConfigConstant.PROPERTY_CONNECTION_TEST_COMMAND, RemoteDriver.DEFAULT_CONNECTION_TEST_COMMAND);
		long connectionTestMaxDuration = getSystemProperty(SeleniumConfigConstant.PROPERTY_CONNECTION_TEST_MAX_DURATION, SeleniumConfigConstant.DEFAULT_CONNECTION_TEST_MAX_DURATION);
		long connectionTestMaxTry = getSystemProperty(SeleniumConfigConstant.PROPERTY_CONNECTION_TEST_MAX_TRY, SeleniumConfigConstant.DEFAULT_CONNECTION_TEST_MAX_TRY);

		IndependantLog.info(debugmsg+" VM parameters:\nhost="+host+
				                                    "\nport="+port+
				                                    "\nconnectionTestCommand="+connectionTestCommand+
				                                    "\nconnectionTestMaxDuration="+connectionTestMaxDuration+
				                                    "\nconnectionTestMaxTry="+connectionTestMaxTry);

		if(!StringUtils.isValid(browserName)){
			browserName = getSystemProperty(SelectBrowser.SYSTEM_PROPERTY_BROWSER_NAME, SelectBrowser.BROWSER_NAME_FIREFOX);
		}
		if(!isValidBrowserID(Id)){
			Id = String.valueOf("".hashCode()+"_"+System.currentTimeMillis());
			IndependantLog.warn(debugmsg+" the provided browser ID is NOT valid, generate a random ID '"+Id+"' to use.");
		}

		if (!isRemote) {
			IndependantLog.warn(debugmsg+"attempting to start a local (not remote) browser instance...");
			previousDriver = addWebDriver(Id,SelectBrowser.getBrowserInstance(browserName, extraParameters));
			lastUsedWD.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
			lastUsedWD.manage().window().setSize(new Dimension(1024,768)); // default window size
			if(Url != null && Url.length()> 0) lastUsedWD.get(Url);

		} else {
			IndependantLog.warn(debugmsg+"attempting to start new session on remote server");
			try {
				URL seleniumHub = new URL("http://" + host + ":" + port +"/wd/hub");
				DesiredCapabilities capabilities = SelectBrowser.getDesiredCapabilities(browserName, extraParameters);
				capabilities.setJavascriptEnabled(true);
				capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
				capabilities.setCapability(RemoteDriver.CAPABILITY_ID, Id); // custom id for session tracking
				capabilities.setCapability(RemoteDriver.CAPABILITY_RECONNECT, false); // custom id
				capabilities.setCapability(RemoteDriver.CAPABILITY_REMOTESERVER, host); // custom id

				RemoteDriver remotedriver = RemoteDriver.instance(seleniumHub,capabilities);

				//Re-create RemoteWebDriver if the communication between WebDriver and browserDriver is not good
				int triedTimes = 0;
				while(!remotedriver.isConnectionFine(connectionTestCommand, connectionTestMaxDuration, Processor.getSecsWaitForComponent())){
					if(++triedTimes>connectionTestMaxTry){
						IndependantLog.warn(debugmsg+" the connection between WebDriver and BrowserDriver seems NOT good. "
								+ "The execution time exceed the max accepted duration '"+connectionTestMaxDuration+"' for command '"+connectionTestCommand+"'.");
						break;
					}
					remotedriver.quit();
					StringUtils.sleep(500);
					remotedriver = new RemoteDriver(seleniumHub, capabilities);
				}

				previousDriver = addWebDriver(Id,remotedriver);
				lastUsedWD.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);

				try{
					//load ModHeader's profile, Modheader is used to modify the http request/response heder's content
					loadModHeaderProfile(lastUsedWD, capabilities.getCapability(SelectBrowser.KEY_CHROME_EXTENSION_MODHEADER_PROFILE));

					//setSize() is not supported by "chromedriver" for "android chrome"
					lastUsedWD.manage().window().setSize(new Dimension(1024,768)); // default window size
				}catch(Exception e){
					IndependantLog.warn(debugmsg+StringUtils.debugmsg(e));
				}

				if(Url != null && Url.length()> 0) lastUsedWD.get(Url);
			}catch(Throwable t){
				IndependantLog.warn(debugmsg+"new RemoteDriver failure.  RemoteServer may not be running!");
				IndependantLog.warn(debugmsg+"Caused By: "+ t.getClass().getName()+", "+ t.getMessage());
				Throwable c = t;
				do{
					c = c.getCause();
					if( c instanceof Throwable){
						IndependantLog.warn(debugmsg+"Caused By: "+ c.getClass().getName()+", "+ c.getMessage());
					}
				}while(c != null);

				if(t instanceof SessionNotCreatedException){
					//TODO Update the browser-driver to the latest matched one, what is the trigger for updating?
					IndependantLog.debug(debugmsg+" Tried to update the browser driver.");
					try{ AbstractDriverUpdater.instance(browserName).update();} catch(Exception e){
						IndependantLog.warn(debugmsg+" Failed to update driver for "+browserName+", due to "+e.getMessage());
					}
				}

				throw new SeleniumPlusException(t);
			}
		}

		if(previousDriver!=null){
			//Just simply stop duplicate session.
			IndependantLog.warn(debugmsg+"There is a WebDriver previously stored in cache with id '"+Id+"', going to stop it.");
			previousDriver.quit();
		}
		//Initialize javascript's variables.
		js_initialize();
	}

	/**
	 * Prerequisite: the extension "ModHeader" should have been installed into the chrome browser.<br>
	 *
	 * This method is going to load ModHeader's profile.<br>
	 * Currently it opens the ModHeader's setting page to load the profile.<br>
	 * In the furture we probably find a better way to set profile.<br>
	 *
	 * @param driver WebDriver
	 * @param modheaderProfile String, the "ModHeader" profile. It can be
	 *                         <ol>
	 *                         <li>a json file containing the profile, such as "C:\\SeleniumPlus\\extra\\ModHeader.json"
	 *                         <li>a json string representing the profile
	 *                         <li>an object representing the profile, it will be converted to "json" string.
	 *                         <li>an "url" representing the profile, such as "https://bewisse.com/modheader/p/#NobwRAhgDlCmB2ATAsge0bMAuAZhANgM6wA0YARhAMYDWA5gE6oCuSAwqvqg9mAMQA2cgFYAzMIBMYMjgCW+AC6wGhbMAC6ZABawIGFWvBVUAWxMIFvaWAQRy+WImwKGzUmHgRzvAB4BaQghCPygmKAg6CCU-WScyADcCN15EUwhZeGZiHgBfTTAtWNgOMwtnV3cGWEIoAAldfVUsDTJCLW4FABVZBQdeAEZrJR8FDi4eLH4caZmhnr7JgAUmOQcAAkGyZgZ8ACVYKHxqWHN4BSaNPKA"
	 *                         </ol>
	 * @throws SeleniumPlusException
	 */
	public static void loadModHeaderProfile(WebDriver driver, Object modheaderProfile) throws SeleniumPlusException{

		String profile = null;
		String debugmsg = StringUtils.debugmsg(false);

		if(modheaderProfile!=null){
			IndependantLog.debug(debugmsg+"loading chrome extension ModHeader's profile "+modheaderProfile);
			if(!(modheaderProfile instanceof String)){
				profile = Utils.toJsonString(modheaderProfile);
			}else{
				profile = modheaderProfile.toString();
			}
		}

		if(profile==null){
			IndependantLog.warn(debugmsg+"the profile is null, cannot load it!");
			return;
		}

		try{
			// invoke the extensions popup settings page
			driver.get("chrome-extension://idgpnmonknjnojddfkpgkljpfnnfcklj/popup.html");

			//wait for the "ModHeader" settings page
			StringUtils.sleep(5000);

			//click the "..." button to show the pupup menu
			driver.findElement(By.xpath("//button[@title='More']")).click();
			StringUtils.sleep(3000);

			//click the "import profile" menuitem
			driver.findElement(By.xpath("//*[text()[contains(.,'Import profile')]]")).click();
			StringUtils.sleep(3000);

			if(new File(profile).exists()){
				//click the button "load from file", this will bring a Windows "Open File Dialog"
				driver.findElement(By.xpath("//*[text()[contains(.,'Load from file')]]")).click();
				StringUtils.sleep(3000);

				//"load from file" will bring a Windows "Open File Dialog", we have to use Robot to interact with it
				//input the profile json file
				RBT.inputChars(profile);
				StringUtils.sleep(3000);

				RBT.inputKeys("{Enter}");
				StringUtils.sleep(3000);
			}else{
				//we suppose this profile is URL or a JSON string, input the profile json/url string into the textarea
				driver.findElement(By.xpath("//*[@id='dialog-content']/textarea")).sendKeys(profile);
				StringUtils.sleep(3000);

				//selenium cannot find the "Import" button by text :-(, we have to find it by another xpath
//				String importBthXpath = "//*[text()[contains(.,'Import')]]";
//				String importBthXpath = "//button[text()[contains(.,'Import')]]";
//				String importBthXpath = "//button[contains(text(),'Import')]";
				String[] importBthXpaths = {"//div[@class='mdc-dialog mdc-dialog--open']//div[@class='mdc-dialog__actions']/button[2]",
						                    "/html/body/div[2]/div[1]/div[3]/div[1]/div/div[2]/button[2]"};
				WebElement importBth = null;

				for(String importBthXpath: importBthXpaths){
					try{
						importBth = driver.findElement(By.xpath(importBthXpath));
						if(importBth!=null) break;
					}catch(NoSuchElementException nse){}
				}

				if(importBth!=null){
					importBth.click();
					IndependantLog.debug(debugmsg+" click button to import profile.");
				}else{
					//If the focus is not on page, the Robot keys will fail.
					RBT.inputKeys("{Tab}");
					RBT.inputKeys("{Tab}");
					RBT.inputKeys("{Enter}");
					IndependantLog.debug(debugmsg+" use robot keys to import profile.");
				}

				StringUtils.sleep(3000);
			}

			//This is important step: refresh to get the profile loaded.
			driver.navigate().refresh();

		}catch(Exception e){
			IndependantLog.error(debugmsg+" met exception "+e.toString());
			if(e instanceof SeleniumPlusException) throw e;
			throw new SeleniumPlusException(e);
		}
	}

	/**
	 * <b>NOTE: This only works for Chrome browser started with option 'setNetworkConditions'.</b>
	 * @return boolean true if the current 'network condition' has been removed.
	 */
	public static boolean deleteNetworkConditions(){
		WebDriver webdriver = getWebDriver();
		RemoteDriver remotedriver = null;

		if(webdriver instanceof RemoteDriver){
			remotedriver = ((RemoteDriver) webdriver);
			CommandExecutor executor = remotedriver.getCommandExecutor();

			try {
				Response response = executor.execute(new Command(remotedriver.getSessionId(),"deleteNetworkConditions", ImmutableMap.of()));
				IndependantLog.debug("deleteNetworkConditions finished with response: "+response.toString());
				return response.getStatus()==0;
			} catch (Exception e) {
				IndependantLog.error("Met "+e.toString());
			}
		}else{
			IndependantLog.error("The current webdriver is not RemoteDriver, cannot proceed 'deleteNetworkConditions'.");
		}
		return false;
	}

	/**
	 * <b>NOTE: This only works for Chrome browser started with option 'setNetworkConditions'.</b>
	 * @return Object, the current network conditions, it is normally a JSON string, such as <b>{"offline":false, "latency":5, "download_throughput":5000 , "upload_throughput":5000}</b>.<br>
	 *                 it will be null if no 'network conditions' is set.
	 */
	public static Object getNetworkConditions(){
		WebDriver webdriver = getWebDriver();
		RemoteDriver remotedriver = null;

		if(webdriver instanceof RemoteDriver){
			remotedriver = ((RemoteDriver) webdriver);
			CommandExecutor executor = remotedriver.getCommandExecutor();

			try {
				Response response = executor.execute(new Command(remotedriver.getSessionId(),"getNetworkConditions", ImmutableMap.of()));
				IndependantLog.debug("getNetworkConditions finished with response: "+response.toString());
				if(response.getStatus()==0){
					return response.getValue();
				}else{
					IndependantLog.debug("getNetworkConditions failed with status: "+response.getStatus());
				}
			} catch (Exception e) {
				IndependantLog.error("Met "+e.toString());
			}
		}else{
			IndependantLog.error("The current webdriver is not RemoteDriver, cannot proceed 'getNetworkConditions'.");
		}
		return null;
	}

	/**
	 * <b>NOTE: This only works for Chrome browser started with option 'setNetworkConditions'.</b>
	 * @param networkConditions	String, JSON string contains the 'network conditions' to set.<br>
	 *                                  This JSON can contain keys of "offline"(boolean), "latency"(integer ms), "download_throughput"(integer bps), "upload_throughput"(integer bps).
	 *                                  such as <b>{"offline":false, "latency":5, "download_throughput":5000 , "upload_throughput":5000}</b>
	 * @return boolean true if 'network conditions' have been set successfully.
	 */
	public static boolean setNetworkConditions(String networkConditions){
		return setNetworkConditions(getWebDriver(), networkConditions);
	}

	/**
	 * <b>NOTE: This only works for Chrome browser started with option 'setNetworkConditions'.</b>
	 * @param webdriver	WebDriver, used to execute this command.<br>
	 * @param networkConditions	String, JSON string contains the 'network conditions' to set.<br>
	 *                                  This JSON can contain keys of "offline"(boolean), "latency"(integer ms), "download_throughput"(integer bps), "upload_throughput"(integer bps).
	 *                                  such as <b>{"offline":false, "latency":5, "download_throughput":5000 , "upload_throughput":5000}</b>
	 * @return boolean true if 'network conditions' have been set successfully.
	 */
	public static boolean setNetworkConditions(WebDriver webdriver, String networkConditions){
		try {
			Map<?, ?> map = Utils.fromJsonString(networkConditions, Map.class);
			return setNetworkConditions(webdriver, map);
		} catch (Exception e) {
			IndependantLog.error("Met "+e.toString());
		}
		return false;
	}

	/**
	 * <b>NOTE: This only works for Chrome browser started with option 'setNetworkConditions'.</b>
	 * @param networkConditions	Map, contains the 'network conditions' to set.<br>
	 *                               This map can contain keys of "offline"(boolean), "latency"(integer ms), "download_throughput"(integer bps), "upload_throughput"(integer bps).
	 * @return boolean true if 'network conditions' have been set successfully.
	 */
	public static boolean setNetworkConditions(Map<?, ?> networkConditions){
		return setNetworkConditions(getWebDriver(), networkConditions);
	}

	/**
	 * <b>NOTE: This only works for Chrome browser started with option 'setNetworkConditions'.</b>
	 * @param webdriver	WebDriver, used to execute this command.<br>
	 * @param networkConditions	Map, contains the 'network conditions' to set.<br>
	 *                               This map can contain keys of "offline"(boolean), "latency"(integer ms), "download_throughput"(integer bps), "upload_throughput"(integer bps).
	 * @return boolean true if 'network conditions' have been set successfully.
	 */
	private static boolean setNetworkConditions(WebDriver webdriver, Map<?, ?> networkConditions){
		RemoteDriver remotedriver = null;

		if(webdriver instanceof RemoteDriver){
			remotedriver = ((RemoteDriver) webdriver);
			CommandExecutor executor = remotedriver.getCommandExecutor();

			try {
				IndependantLog.debug("setNetworkConditions "+networkConditions);
				Command cmdSetNetworkConditions = new Command(remotedriver.getSessionId(), ChromeHttpCommandExecutor.SET_NETWORK_CONDITIONS,
						ImmutableMap.of(ChromeHttpCommandExecutor.URI_NETWORK_CONDITIONS, ImmutableMap.copyOf(networkConditions)));
				Response response = executor.execute(cmdSetNetworkConditions);
				IndependantLog.debug("setNetworkConditions finished with response: "+response.toString());
				if(response.getStatus()==0){
					return true;
				}else{
					IndependantLog.debug("setNetworkConditions failed with status: "+response.getStatus());
				}
			} catch (Exception e) {
				IndependantLog.error("Met "+e.toString());
			}
		}else{
			IndependantLog.error("The current webdriver is not RemoteDriver, cannot proceed 'setNetworkConditions'.");
		}
		return false;
	}

	/**
	 * Close browser (close all windows associated) indicated by ID.
	 * If the provided ID is associated with the "current" or "lastUsed" WebDriver
	 * the call to removeWebDriver will automatically "pop" the next WebDriver off
	 * the stack to be the new "current" or "lastUsed" WebDriver.
	 * @param ID	String, the id to identify the browser
	 * @throws IllegalArgumentException if the provided browser ID is NOT {@link #isValidBrowserID(String)} or not known as a running instance.
	 * @see #removeWebDriver(String)
	 */
	public static void stopBrowser(String ID) throws IllegalArgumentException{
		String debugmsg = StringUtils.debugmsg(WDLibrary.class, "stopBrowser");
		if (!isValidBrowserID(ID)) throw new IllegalArgumentException("Browser provided ID '"+ID+"' was NOT valid.");
		WebDriver webdriver = removeWebDriver(ID);
		if(webdriver==null){
			IndependantLog.warn(debugmsg+"cannot get webdriver according to unknown id '"+ID+"'");
			throw new IllegalArgumentException("Browser ID '"+ ID +"' is not a valid ID for a running browser session.");
		}
		webdriver.quit();
	}

	/**
	 * Resize current browser window
	 * @param width int, width in pixels
	 * @param height int, height in pixels
	 * @throws SeleniumPlusException
	 */
	public static void resizeBrowserWindow(int width, int height) throws SeleniumPlusException{

		try{
			Dimension size = new Dimension(width, height);
			lastUsedWD.manage().window().setSize(size);
		} catch (Exception e){
			throw new SeleniumPlusException("Failed to resize current browser window to ("+width+","+height+") "+ e.getMessage());
		}
	}

	/**
	 * Clean up the SeleniumSever and browser-drivers left behind the SeleniumPlus testing.
	 *
	 * @param host String, the name of the machine on which the process will be killed
	 * @throws SAFSException
	 */
	public static void stopSeleniumServer(String host) throws SAFSException {

		String javaProcess = "java";
		if(Console.isWindowsOS()) javaProcess += ".exe";
		UtilsIndependent.killProcess(host, javaProcess, "org.safs.selenium.util.SeleniumServerRunner");//WebDriverGUIUtilities.startRemoteServer(String projectdir, String... extraParams)
		String notJUnit = "JUnitTestRunner"; // don't kill JUnit process
		UtilsIndependent.killProcess(host, javaProcess, "selenium-server-standalone", notJUnit);//selenium-server-standalone-xxx.jar

		if(Console.isWindowsOS()){
			UtilsIndependent.killProcess(host, "cmd.exe", File.separator+"extra"+File.separator+"RemoteServer.bat");
		}else if(Console.isUnixOS() || Console.isMacOS()){
			UtilsIndependent.killProcess(host, "bash", File.separator+"extra"+File.separator+"RemoteServer.sh");
		}

		UtilsIndependent.killChromeDriver(host);
		UtilsIndependent.killIEDriverServer(host);
		UtilsIndependent.killMicrosoftWebDriver(host);
		UtilsIndependent.killGeckoDriver(host);
	}

	/**
	 * Kill the process 'geckodriver.exe/geckodriver_64.exe' on windows, or "geckodriver/geckodriver_64" on linux.
	 * @param host String, the name of the machine on which the process 'geckodriver.exe' will be killed.
	 * @return List<ProcessInfo>, a list containing the killed process's id and kill-execution-return-code.
	 * @deprecated Please use {@link UtilsIndependent#killGeckoDriver(String)} instead.
	 * @throws SAFSException
	 */
	@Deprecated
	public static List<ProcessInfo> killGeckoDriver(String host) throws SAFSException{
		return UtilsIndependent.killGeckoDriver(host);
	}

	/**
	 * Kill the process 'chromedriver.exe' on windows, or "chromedrver" on linux.
	 * @param host String, the name of the machine on which the process 'chromedriver.exe' will be killed.
	 * @return List<ProcessInfo>, a list containing the killed process's id and kill-execution-return-code.
	 * @deprecated Please use {@link UtilsIndependent#killChromeDriver(String)} instead.
	 * @throws SAFSException
	 */
	@Deprecated
	public static List<ProcessInfo> killChromeDriver(String host) throws SAFSException{
		return UtilsIndependent.killChromeDriver(host);
	}

	/**
	 * Kill the process 'IEDriverServer.exe'.
	 * @param host String, the name of the machine on which the process 'IEDriverServer.exe' will be killed.
	 * @return List<ProcessInfo>, a list containing the killed process's id and kill-execution-return-code.
	 * @deprecated Please use {@link UtilsIndependent#killIEDriverServer(String)} instead.
	 * @throws SAFSException
	 */
	@Deprecated
	public static List<ProcessInfo> killIEDriverServer(String host) throws SAFSException{
		return UtilsIndependent.killExtraProcess(host, "IEDriverServer.exe");
	}

	/**
	 * Kill the process 'MicrosoftWebDriver.exe'.
	 * @param host String, the name of the machine on which the process 'MicrosoftWebDriver.exe' will be killed.
	 * @return List<ProcessInfo>, a list containing the killed process's id and kill-execution-return-code.
	 * @deprecated Please use {@link UtilsIndependent#killMicrosoftWebDriver(String)} instead.
	 * @throws SAFSException
	 */
	@Deprecated
	public static List<ProcessInfo> killMicrosoftWebDriver(String host) throws SAFSException{
		return UtilsIndependent.killExtraProcess(host, "MicrosoftWebDriver.exe");
	}

	/**
	 * Kill the process 'msedgedriver.exe'.
	 * @param host String, the name of the machine on which the process 'msedgedriver.exe' will be killed.
	 * @return List<ProcessInfo>, a list containing the killed process's id and kill-execution-return-code.
	 * @deprecated Please use {@link UtilsIndependent#killMSEdgeDriver(String)} instead.
	 * @throws SAFSException
	 */
	@Deprecated
	public static List<ProcessInfo> killMSEdgeDriver(String host) throws SAFSException{
		return UtilsIndependent.killExtraProcess(host, "msedgedriver.exe");
	}

	/**
	 * Kill the driver process by browserName.
	 * @param host String, the name of the machine on which the driver process will be killed.
	 * @param browserName String, the name of the browser for which the driver process should be killed.
	 * @return List<ProcessInfo>, a list containing the killed process's id and kill-execution-return-code.
	 * @deprecated Please use {@link UtilsIndependent#killBrowserDriver(String, String)} instead.
	 * @throws SAFSException
	 */
	@Deprecated
	public static List<ProcessInfo> killBrowserDriver(String host, String browserName) throws SAFSException{
		return UtilsIndependent.killBrowserDriver(host, browserName);
	}

	/**
	 * Kill the process launched from executables located in %SAFSDIR%\samples\Selenium2.0\extra\ or %SELENIUM_PLUS%\extra\
	 * @param host String, the name of machine on which the process will be killed.
	 * @param processName String, the name of process to kill. like chromedriver.exe, IEDriverServer.exe etc.
	 * @return List<ProcessInfo>, a list containing the killed process's id and kill-execution-return-code.
	 * @deprecated Please use UtilsIndependent instead.
	 * @throws SAFSException
	 */
	@Deprecated
	public static List<ProcessInfo> killExtraProcess(String host, String processName) throws SAFSException{
		return UtilsIndependent.killExtraProcess(host, processName);
	}

	/**
	 * Kill the process according to the process name and partial command line.
	 * @param host String, the name of machine on which the process will be killed.
	 * @param processName String, the name of process to kill. like chromedriver.exe, IEDriverServer.exe, java.exe etc.
	 * @param commandline String, the partial commandline of the process to be killed.
	 * @return List<ProcessInfo>, a list containing the killed process's id and kill-execution-return-code.
	 * @deprecated Please use {@link UtilsIndependent#killProcess(String, String, String)} instead.
	 * @throws SAFSException
	 */
	@Deprecated
	public static List<ProcessInfo> killProcess(String host, String processName, String commandline) throws SAFSException{
		return UtilsIndependent.killProcess(host, processName, commandline);
	}

	/**
	 * Kill the process according to the process name and partial command line.
	 * @param host String, the name of machine on which the process will be killed.
	 * @param processName String, the name of process to kill. like chromedriver.exe, IEDriverServer.exe, java.exe etc.
	 * @param commandline String, the partial commandline of the process to be killed.
	 * @param notCommandline String, the partial commandline of the process NOT to be killed.
	 * @return List<ProcessInfo>, a list containing the killed process's id and kill-execution-return-code.
	 * @deprecated Please use {@link UtilsIndependent#killProcess(String, String, String, String)} instead.
	 * @throws SAFSException
	 */
	@Deprecated
	public static List<ProcessInfo> killProcess(String host, String processName, String commandline, String notCommandline) throws SAFSException{
		return UtilsIndependent.killProcess(host, processName, commandline, notCommandline);
	}

	/**
	 * scroll browser window by x and/or y number of pixels.
	 * Only works if the associated scrollbar(s) are actually visible.
	 * Synonymous to Javascript window.scrollBy(x,y).
	 * @param x int, horizontal scrolling in pixels
	 * @param y int, vertical scrolling in pixels
	 * @param element, scroll the topmost window containing this element.
	 * @throws SeleniumPlusException
	 */
	public static void scrollBrowserWindowBy(int x, int y, WebElement element) throws SeleniumPlusException{
		try{
			executeJavaScriptOnWebElement("window.scrollBy("+ x +","+ y +");", element);
		} catch (Exception e){
			throw new SeleniumPlusException("Failed to scroll browser window by ("+x+","+y+") pixels. "+e.getClass().getName()+", "+ e.getMessage());
		}
	}

	/**
	 * scroll browser window to x and/or y scroll position.
	 * Only works if the associated scrollbar(s) are actually visible.
	 * Synonymous to Javascript window.scrollTo(x,y).
	 * @param x int, horizontal position in pixels
	 * @param y int, vertical position in pixels
	 * @param element, scroll the topmost window containing this element.
	 * @throws SeleniumPlusException
	 */
	public static void scrollBrowserWindowTo(int x, int y, WebElement element) throws SeleniumPlusException{
		try{
			executeJavaScriptOnWebElement("window.scrollTo("+ x +","+ y +");", element);
		} catch (Exception e){
			throw new SeleniumPlusException("Failed to scroll browser window to ("+x+","+y+") pixels. "+e.getClass().getName()+", "+ e.getMessage());
		}
	}

	/**
	 * Align the top of webelement to the top of browser.
	 * @param element WebElement, the element to scroll to align with browser's top
	 * @throws SeleniumPlusException
	 */
	public static void alignToTop(WebElement element) throws SeleniumPlusException{
		try{
			StringBuffer jsScript = new StringBuffer();
			jsScript.append(JavaScriptFunctions.scrollIntoView());
			jsScript.append("scrollIntoView(arguments[0], arguments[1]);\n");

			executeJavaScriptOnWebElement(jsScript.toString(), element, true);

		} catch (Exception e){
			throw new SeleniumPlusException("Failed to align to top of browser. Met "+StringUtils.debugmsg(e));
		}
	}
	/**
	 * Align the bottom of webelement to the bottom of browser.
	 * @param element WebElement, the element to scroll to align with browser's bottom
	 * @throws SeleniumPlusException
	 */
	public static void alignToBottom(WebElement element) throws SeleniumPlusException{
		try{
			StringBuffer jsScript = new StringBuffer();
			jsScript.append(JavaScriptFunctions.scrollIntoView());
			jsScript.append("scrollIntoView(arguments[0], arguments[1]);\n");

			executeJavaScriptOnWebElement(jsScript.toString(), element, false);

		} catch (Exception e){
			throw new SeleniumPlusException("Failed to align to bottom of browser. Met "+StringUtils.debugmsg(e));
		}
	}

	/**
	 * Try to show the webelement on the browser's page.<br>
	 * Within this method, we try several ways to scroll the element into view:<br>
	 * If the parameter 'verify' is true then we will stop try other ways if the element is already shown on the page.<br>
	 * Else if 'verify' parameter is false then we will try all ways to show the element: our isShowOnPage() is not so reliable all the time.<br>
	 * @param element WebElement, the webelement to show.
	 * @param params optional<ul>
	 * <b>optionals[0] verify</b> boolean, verify if the component is shown on page. The default value is true.<br>
	 * <b>optionals[1] refresh</b> boolean, if the element will be refreshed after scrolling.
	 *                                      Normally we should refresh the component so that selenium API will return correct coordinates of the component.
	 *                                      But refresh will cost a lot of time and it will slow down the whole test, so we will not refresh the component by default.<br>
	 * </ul>
	 *
	 * @return boolean, true if the element has been shown on the page. If the passed in parameter 'verify' is false, it will always return true.
	 *
	 * @history
	 * <br> May 18, 2015	Lei Wang	Get refreshed WebElement after moving it on the page.
	 */
	public static boolean showOnPage(WebElement element, String... optional){
		String debugmsg = StringUtils.debugmsg(false);

		boolean verify = true;
		boolean refresh = false;
		if(optional!=null){
			if(optional.length>0 && StringUtils.isValid(optional[0])) verify = StringUtilities.convertBool(optional[0]);
			if(optional.length>1 && StringUtils.isValid(optional[1])) refresh = StringUtilities.convertBool(optional[1]);
		}

		if(verify && isShowOnPage(element)) return true;

		try {
			IndependantLog.debug(debugmsg+"make element visible in brower viewport.");
			Coordinates coordinate = ((Locatable) element).getCoordinates();
			coordinate.inViewPort();
			//after inViewPort(), the real element may move, but WebElement.getLocation() may still return the old value
			if(refresh) {
				IndependantLog.debug(debugmsg+"refreshing element reference for verification.");
				element = CFComponent.refresh(element);
			}
			if(verify && isShowOnPage(element)) return true;
		} catch (Exception e) {
			IndependantLog.warn(debugmsg+"Fail to show webelement in browser's viewport, due to "+StringUtils.debugmsg(e));
		}

		try{
			//Moves the mouse to the middle of the element. The element is scrolled into view and its location is calculated using getBoundingClientRect.
			new Actions(WDLibrary.getWebDriver()).moveToElement(element).perform();
			if(refresh) {
				IndependantLog.debug(debugmsg+"refreshing element reference for verification.");
				element = CFComponent.refresh(element);
			}
			if(verify && isShowOnPage(element)) return true;
		}catch(Throwable t){
			IndependantLog.warn("'moveToElement' action failure caused by "+ t.getClass().getName());
		}

		try {
			IndependantLog.debug(debugmsg+"scroll browser to the top-left corner of this component.");
			org.openqa.selenium.Point compLoc = element.getLocation();
			WDLibrary.scrollBrowserWindowTo(compLoc.x, compLoc.y, element);
			if(refresh) {
				IndependantLog.debug(debugmsg+"refreshing element reference again for verification.");
				element = CFComponent.refresh(element);
			}
			if(verify && isShowOnPage(element)) return true;
		} catch (Exception e) {
			IndependantLog.warn(debugmsg+"Fail to scroll browser to the top-left corner of webelement, due to "+StringUtils.debugmsg(e));
		}

		try {
			IndependantLog.debug(debugmsg+"align the top this component to top of browser's viewport.");
			WDLibrary.alignToTop(element);
			if(refresh) {
				IndependantLog.debug(debugmsg+"refreshing element reference yet again for verification.");
				element = CFComponent.refresh(element);
			}
			if(verify && isShowOnPage(element)) return true;
		} catch (Exception e) {
			IndependantLog.warn(debugmsg+"Fail to align top of webelement to browser-viewport-top, due to "+StringUtils.debugmsg(e));
		}

		if(verify){
			IndependantLog.debug(debugmsg+"check if the element 'Top-Left Corner' is located in the page.");
			org.openqa.selenium.Point offset = new org.openqa.selenium.Point(1, 1);
			return isShowOnPage(element, offset);
		}

		return true;
	}

	/**
	 * @param element WebElement, to check if it is shown on the page
	 * @return boolean true if the web-element is fully shown on the page
	 */
	public static boolean isShowOnPage(WebElement element){
		return isShowOnPage(element, null);
	}

	/**
	 * To check if a certain point of web-element is shown on the page.<br>
	 * If the point is not given or it is out of boundary of web-element, method will<br>
	 * check if the web-element is fully shown on the page.<br>
	 *
	 * @param element WebElement, to check if it is shown on the page
	 * @param offset Point, the offset from Left-Top of web-element; the point to check. can be null
	 * @return boolean if the offest-point or the full-web-element is shown on the page.
	 */
	public static boolean isShowOnPage(WebElement element, org.openqa.selenium.Point offset){
		String debugmsg = StringUtils.debugmsg(false);
		boolean isShowOnPage = false;

		try{
			WDLibrary.checkNotNull(element);

			//The location(Left-Top) is relative to the viewport
			org.openqa.selenium.Point elementLTLoc = element.getLocation();
			Dimension elementD = element.getSize();

			int browserXOffset = (int) getLastBrowserWindow().getPageXOffset();
			int browserYOffset = (int) getLastBrowserWindow().getPageYOffset();
			//move the location according to the page-offset, get the location relative to the page
			elementLTLoc = elementLTLoc.moveBy(-browserXOffset, -browserYOffset);
			org.openqa.selenium.Point elementBRLoc = elementLTLoc.moveBy(elementD.width, elementD.height);

			//Get the bounds of the browser's page
			int browserClientW = (int) getLastBrowserWindow().getClientWidth();
			int browserClientH = (int) getLastBrowserWindow().getClientHeight();
			Dimension browserPageBounds = new Dimension(browserClientW, browserClientH);

			IndependantLog.debug(debugmsg+" offset="+offset+"; element dimension="+elementD+"; ");

			if(offset!=null && isLocationInBounds(offset, elementD)){
				//check the offset point is shown browser's page
				elementLTLoc = elementLTLoc.moveBy(offset.x, offset.y);
				IndependantLog.debug(debugmsg+"check if 'element (Top Left+offset) Corner'="+elementLTLoc+" is in 'browser window dimension'="+browserPageBounds+"; ");
				isShowOnPage = isLocationInBounds(elementLTLoc, browserPageBounds);
			}else{
				//if Left-Top and Bottom-Right is in the browser, the element is fully shown
				IndependantLog.debug(debugmsg+"check if 'element Top Left Corner'="+elementLTLoc+" and 'Bottom Right Corner'="+elementBRLoc+" are both in 'browser window dimension'="+browserPageBounds+"; ");
				isShowOnPage = isTopLeftInBounds(elementLTLoc, browserPageBounds) && isBottomRightInBounds(elementBRLoc, browserPageBounds);
			}

		}catch(Exception e){
			IndependantLog.error(debugmsg+"Fail due to "+StringUtils.debugmsg(e));
			if(e instanceof StaleElementReferenceException){
				//If we met this stale exception, we probably lost the frame context (a bug with Selenium & Edge 44).
				resetLastFrame();
			}
		}

		IndependantLog.debug(debugmsg+" return "+isShowOnPage);
		return isShowOnPage;
	}

	/**
	 * Check if the point p locates in the Dimension.
	 * @param p Point, relative to the Dimension. see if the Point is inside bounds of 0,0,bounds.width, bounds.height.
	 * @param bounds Dimension, the boundary
	 * @return boolean true if the point p locates in the Dimension.
	 * A point p on the extreme width or height boundary is NOT in-bounds.
	 */
	public static boolean isLocationInBounds(org.openqa.selenium.Point p, Dimension bounds){
		if(p==null || bounds==null) return false;
		return (0<p.x && p.x<bounds.width) && (0<p.y && p.y<bounds.height);
	}

	/**
	 * Check if the topLeft Point p locates inside the Dimension.
	 * @param topLeft Point, an element's top left point relative to 0,0, bounds.width, bounds.height.
	 * @param bounds Dimension, the boundary
	 * @return boolean true if the point p locates in the Dimension.
	 * A topLeft point p on the extreme (0, 0) boundary IS considered in-bounds.
	 * A topLeft point p on the extreme width or height boundary is NOT considered in-bounds.
	 */
	private static boolean isTopLeftInBounds(org.openqa.selenium.Point topLeft, Dimension bounds){
		if(topLeft==null || bounds==null) return false;
		return (0<=topLeft.x && topLeft.x<bounds.width) && (0<=topLeft.y && topLeft.y<bounds.height);
	}

	/**
	 * Check if the bottomRight Point p locates inside the Dimension.
	 * @param bottomRight Point, an element's bottom right point relative to 0,0, bounds.width, bounds.height.
	 * @param bounds Dimension, the boundary
	 * @return boolean true if the point p locates in the Dimension.
	 * A bottomRight point p on the extreme (0, 0) boundary is NOT considered in-bounds.
	 * A bottomRight point p on the extreme width or height boundary IS considered in-bounds.
	 */
	private static boolean isBottomRightInBounds(org.openqa.selenium.Point bottomRight, Dimension bounds){
		if(bottomRight==null || bounds==null) return false;
		return (0<bottomRight.x && bottomRight.x<=bounds.width) && (0<bottomRight.y && bottomRight.y<=bounds.height);
	}

	/**
	 * Set position of current browser window.
	 * Synonymous to Javascript window.moveTo().
	 * @param x int, x-coordination in pixels
	 * @param y int, y-coordination in pixels
	 * @throws SeleniumPlusException
	 */
	public static void setPositionBrowserWindow(int x, int y) throws SeleniumPlusException{

		try{
			org.openqa.selenium.Point position = new org.openqa.selenium.Point(x, y);
			lastUsedWD.manage().window().setPosition(position);
		} catch (Exception e){
			throw new SeleniumPlusException("Failed to set position of current browser window to ("+x+","+y+") "+ e.getMessage());
		}
	}

	/**
	 * Maximize current browser window
	 * @throws SeleniumPlusException
	 */
	public static void maximizeBrowserWindow() throws SeleniumPlusException{

		try{
			lastUsedWD.manage().window().maximize();
		} catch (Exception e){
			throw new SeleniumPlusException("Failed to maximize current browser window"+ e.getMessage());
		}
	}

	/**
	 * This implementation does NOT really minimize the window, it is a workaround.<br>
	 * Note: set the size to (0,0) can NOT minimize the window. So move the window out of the screen.<br>
	 * Minimize current browser window
	 * @throws SeleniumPlusException
	 */
	public static void minimizeBrowserWindow() throws SeleniumPlusException{

		try{
			org.openqa.selenium.Point outOfScreenPosition = new org.openqa.selenium.Point(-1000, -1000);
			lastUsedWD.manage().window().setPosition(outOfScreenPosition);
			lastUsedWD.manage().window().setSize(new Dimension(0, 0));
		} catch (Exception e){
			throw new SeleniumPlusException("Failed to minimize current browser window"+ e.getMessage());
		}
	}

	/**
	 * Retrieves the WebDriver/Browser with the given title.
	 * This also makes that WebDriver the "current" or "lastUsedWD".
	 * @param title
	 * @return
	 * @throws SeleniumPlusException
	 */
	public static WebDriver getBrowserWithTitle(String title) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(WDLibrary.class, "getBrowserWithTitle");
		WebDriver webdriver = getWebDriverWithTitle(title);
		if(webdriver==null){
			throw new SeleniumPlusException(debugmsg+"cannot get webdriver according to title '"+title+"'",SeleniumPlusException.CODE_OBJECT_IS_NULL);
		}
		String ID = getIDForWebDriver(webdriver);
		try{ RemoteDriver.setLastSessionId(ID);}catch(Exception e){}

		if(lastUsedWD!=webdriver){
			lastUsedWD=webdriver;
			try{ refreshJSExecutor(); }catch(SeleniumPlusException ignored){ }
		}
		return webdriver;
	}

	/**
	 * Close the Browser/WebDriver associated with the last WebElement accessed.
	 * So, we will assume the lastUsedWD is the browser to close.
	 * Currently, the call to stopBrowser will set the new lastUsedWD.
	 * However, in the future the search algorithms for each component will set the
	 * lastUsedWD independently.
	 * @throws SeleniumPlusException if the lastUsed WebDriver is not valid or is null.
	 * @see #stopBrowser(String)
	 */
	public static void closeBrowser() throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(WDLibrary.class, "closeBrowser");
		WebDriver webdriver = lastUsedWD;
		if(webdriver==null){
			throw new SeleniumPlusException(debugmsg+"lastUsed webdriver is null!",SeleniumPlusException.CODE_OBJECT_IS_NULL);
		}
		String ID = getIDForWebDriver(webdriver);
		try{
			stopBrowser(ID);
		}
		catch(IllegalArgumentException i){
			throw new SeleniumPlusException(debugmsg+"illegal or invalid browser ID.",SeleniumPlusException.CODE_OBJECT_IS_NULL);
		}
	}

	/**
	 * Retrieves the WebDriver/Browser with the given id.
	 * This also makes that WebDriver the "current" or "lastUsedWD".
	 * @param id
	 * @return
	 * @throws SeleniumPlusException
	 */
	public static WebDriver getBrowserWithID(String id) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(WDLibrary.class, "getBrowserWithID");
		WebDriver webdriver = getWebDriver(id);
		if(webdriver==null)
			throw new SeleniumPlusException(debugmsg+"cannot get webdriver with id '"+id+"'",SeleniumPlusException.CODE_OBJECT_IS_NULL);

		try{ RemoteDriver.setLastSessionId(id);}catch(Exception e){}

		if(lastUsedWD!=webdriver){
			lastUsedWD=webdriver;
			try{ refreshJSExecutor(); }catch(SeleniumPlusException ignored){ }
		}
		return webdriver;
	}

	/**
	 * Highlight Element / object
	 * @param element to be highlighted
	 *
	 * @deprecated it is replaced by {@link #highlight(WebElement)}.
	 */
	@Deprecated
	public static void highlightElement(WebElement element) throws SeleniumPlusException{

		try{
			for (int i = 0; i < 3; i++) {
				JavascriptExecutor js = (JavascriptExecutor) getWebDriver();;
				js.executeScript("arguments[0].setAttribute('style', arguments[1]);",element, "color: yellow; border: 2px solid yellow;");
				Thread.sleep(3);
				js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "");

			}
		} catch (Exception e ){
				throw new SeleniumPlusException("Failed to highlight object "+ e.getMessage());
		}
	}

	/**
	 * @param driver -- can be null to use the last used WebDriver session.
	 * @return String of browser name or null if there is no WebDriver session.
	 * @see SelectBrowser#BROWSER_NAME_CHROME
	 * @see SelectBrowser#BROWSER_NAME_FIREFOX
	 * @see SelectBrowser#BROWSER_NAME_HTMLUNIT
	 * @see SelectBrowser#BROWSER_NAME_IE
	 * @see SelectBrowser#BROWSER_NAME_SAFARI
	 */
	public static String getBrowserName(WebDriver driver){
		if(driver == null) driver = getWebDriver();
		if(driver instanceof FirefoxDriver) return SelectBrowser.BROWSER_NAME_FIREFOX;
		if(driver instanceof ChromeDriver) return SelectBrowser.BROWSER_NAME_CHROME;
		if(driver instanceof InternetExplorerDriver) return SelectBrowser.BROWSER_NAME_IE;
		if(driver instanceof SafariDriver) return SelectBrowser.BROWSER_NAME_SAFARI;
		if(driver instanceof HtmlUnitDriver) return SelectBrowser.BROWSER_NAME_HTMLUNIT;
		if(driver instanceof RemoteWebDriver) {
			RemoteWebDriver rdriver = (RemoteWebDriver) driver;
			try{ return rdriver.getCapabilities().getBrowserName();}catch(NullPointerException ignore){}
		}
		return null;
	}
	/**
	 * @param driver -- can be null to use last used webdriver session.
	 * @return true if browser name matches BROWSER_NAME_HTMLUNIT
	 * @see #getBrowserName(WebDriver)
	 * @see SelectBrowser#BROWSER_NAME_HTMLUNIT
	 */
	public static boolean isHtmlUnit(WebDriver driver){
		return SelectBrowser.BROWSER_NAME_HTMLUNIT.equalsIgnoreCase(getBrowserName(driver));
	}
	/**
	 * @param driver -- can be null to use last used webdriver session.
	 * @return true if browser name matches BROWSER_NAME_SAFARI
	 * @see #getBrowserName(WebDriver)
	 * @see SelectBrowser#BROWSER_NAME_SAFARI
	 */
	public static boolean isSafari(WebDriver driver){
		return SelectBrowser.BROWSER_NAME_SAFARI.equalsIgnoreCase(getBrowserName(driver));
	}
	/**
	 * @param driver -- can be null to use last used webdriver session.
	 * @return true if browser name matches BROWSER_NAME_FIREFOX
	 * @see #getBrowserName(WebDriver)
	 * @see SelectBrowser#BROWSER_NAME_FIREFOX
	 */
	public static boolean isFireFox(WebDriver driver){
		return SelectBrowser.BROWSER_NAME_FIREFOX.equalsIgnoreCase(getBrowserName(driver));
	}
	/**
	 * @param driver -- can be null to use last used webdriver session.
	 * @return true if browser name matches BROWSER_NAME_IE
	 * @see #getBrowserName(WebDriver)
	 * @see SelectBrowser#BROWSER_NAME_IE
	 */
	public static boolean isInternetExplorer(WebDriver driver){
		return SelectBrowser.BROWSER_NAME_IE.equalsIgnoreCase(getBrowserName(driver));
	}
	/**
	 * @param driver -- can be null to use last used webdriver session.
	 * @return true if browser name matches BROWSER_NAME_CHROME
	 * @see #getBrowserName(WebDriver)
	 * @see SelectBrowser#BROWSER_NAME_CHROME
	 */
	public static boolean isChrome(WebDriver driver){
		return SelectBrowser.BROWSER_NAME_CHROME.equalsIgnoreCase(getBrowserName(driver));
	}

	/**
	 * Load a SeBuilder JSON Script from the provided path so it can be started or run.
	 * @param path -- full path to the JSON script.
	 * @return Script
	 * @throws FileNotFoundException -- if a provided path is null or otherwise invalid
	 */
	public static Script getSeleniumBuilderScript(String path) throws FileNotFoundException{

		File source = new CaseInsensitiveFile(path).toFile();
		BufferedReader reader = FileUtilities.getUTF8BufferedFileReader(source.getAbsolutePath());
		WDScriptFactory factory = new WDScriptFactory();
		StepTypeFactory stepTypes = new StepTypeFactory();
		stepTypes.setSecondaryPackage(WDScriptFactory.SRSTEPTYPE_PACKAGE);
		factory.setStepTypeFactory(stepTypes);
		try{ return factory.parse(reader, source).get(0);}
		catch(IOException x){
			throw new FileNotFoundException("IOException reading or processing "+path +", "+x.getMessage());
		}
	}

	/**
	 * @return the "current" or "lastUsed" WebDriver as a WebDriverFactory usable by the
	 * Selenium Builder Interpreter.
	 */
	public static WebDriverFactory getWebDriverAsWebDriverFactory(){
		return (new WebDriverFactory(){
				@Override
				public RemoteWebDriver make(HashMap<String, String> config){
					return (RemoteWebDriver) WDLibrary.getWebDriver();
				}
		});
	}

	/**
	 * Run a SeBuilder JSON Script.
	 * @param path - fullpath to JSON script file.
	 * <p>
	 * @param log - An appropriate logging interface--such as the ApacheLogUtilities we instantiate
	 * when launching our Selenium engine.
	 * <p>
	 * @param driverFactory -- if null, we will use a default Factory that does NOT instantiate a new
	 * WebDriver but uses the "current" or "lastUsed" WebDriver ignoring any webDriverConfig that may
	 * or may not be provided.
	 * <p>
	 * Consult the Selenium Interpreter documentation for any additional info on using the
	 * <a href="http://github.com/sebuilder/se-builder/wiki/Se-Interpreter">Selenium Builder Interpreter</a>
	 * if you are NOT intending to use the current or lastUsed WebDriver.
	 * <p>
	 * @param webDriverConfig -- can be null if driverFactory is null and we will be using an existing
	 * WebDriver.
	 * @param initialVars -- can be null.
	 * @param allowClose -- set to false to prevent the script from closing the Driver.
	 * @return
	 * @throws FileNotFoundException
	 * @throws RuntimeException
	 */
	public static boolean runSeleniumBuilderScript(String path,
                                                   org.apache.commons.logging.Log log,
			                                       WebDriverFactory driverFactory,
			                                       HashMap<String, String> webDriverConfig,
			                                       Map<String, String> initialVars,
			                                       boolean allowClose)
	                      throws FileNotFoundException, RuntimeException{

		Map<String, String> vars = initialVars == null ? new HashMap<String, String>(): initialVars;
		WebDriverFactory factory = getWebDriverAsWebDriverFactory();
		Script script = getSeleniumBuilderScript(path);
		script.closeDriver = allowClose;
		script.testRunFactory = new WDTestRunFactory();
		return script.run(log, factory, webDriverConfig, vars);
	}

	/**
	 * Run a SeBuilder JSON Script explicitly using the existing WebDriver instance.
	 * <p>
	 * @param path - fullpath to JSON script file.
	 * <p>
	 * @param log - An appropriate logging interface--such as the ApacheLogUtilities we instantiate
	 * when launching our Selenium engine.
	 * <p>
	 * @param allowClose -- set to false to prevent the script from closing the Driver.
	 * @return
	 * @throws FileNotFoundException
	 * @throws RuntimeException
	 */
	public static boolean runSeleniumBuilderScript(String path,
            									   org.apache.commons.logging.Log log,
            									   boolean allowClose)
            									   throws FileNotFoundException, RuntimeException{

		return runSeleniumBuilderScript(path, log, null, null, null, allowClose);
	}

	/**
	 * Attempt to SetFocus on the topmost window containing the WebElement.
	 * @param element WebElement from which to get the proper RemoteWebDriver
	 * @return boolean true if the window is focused.
	 * @throws SeleniumPlusException if an error occured during the execution attempt.
	 * @see #windowSetFocus(WebDriver)
	 */
	public static boolean windowSetFocus(WebElement element)throws SeleniumPlusException{
		//we don't really need element as parameter for executing js code "window.top.focus();"
		//and if element is dynamically added by javascript and will be considered as stale
		//which will cause executeJavaScriptOnWebElement to throw SeleniumPlusException
		//executeJavaScriptOnWebElement("try{ window.top.focus();}catch(error){ debug(error); }", element);

		executeScript("try{ window.top.focus();}catch(error){ debug(error); }");
		return true;
	}

	/**
	 * Attempt to SetFocus on the topmost window for the provided WebDriver.
	 * @param adriver WebDriver from which to get the window title
	 * @return boolean true if the window is focused.
	 * @throws SeleniumPlusException if an error occured during the execution attempt.
	 * @see #windowSetFocus(String)
	 */
	public static boolean windowSetFocus(WebDriver adriver)throws SeleniumPlusException{
		boolean rc = false;
		IndependantLog.info("WDLibrary.windowSetFocus set focus via WebDriver window title");
		try{
			rc = windowSetFocus(adriver.getTitle());
		}catch(Exception x){
			IndependantLog.info("WDLibrary.windowSetFocus RemoteWebElement ignoring "+ getThrowableMessages(x)+ ": "+ x.getMessage());
		}
		return rc;
	}

	/**
	 * Set a specific Window matching the provided title to be the foreground window.
	 * @param titleRegExp -- title or regular expression matching the target window's caption.
	 * @return true on success
	 * @throws SeleniumPlusException
	 * @see {@link NativeWrapper#SetForegroundWindow(String)}
	 */
	public static boolean windowSetFocus(String titleRegExp) throws SeleniumPlusException{
		IndependantLog.info("WDLibrary.windowSetFocus set focus window title: " + titleRegExp);
		boolean rc = NativeWrapper.SetForegroundWindow(titleRegExp);
		if (!rc) throw new SeleniumPlusException("Failed to setfocus, return:" + rc);
		return rc;
	}

	/**
	 * Attempt to SetFocus on the WebElement.
	 * @param element WebElement, to get focus.
	 * @return boolean true if the webelement is focused.
	 * @throws SeleniumPlusException
	 */
	public static boolean focus(WebElement element)throws SeleniumPlusException{
		boolean focused = false;
		if(element==null) return false;
		String debugmsg = "WDLibrary.focus() ";

		try{
			if(! SearchObject.getBypassFramesReset()){
				focused = windowSetFocus(element);
				if(!focused) IndependantLog.warn(debugmsg+"fail to set focus to top window.");
				else IndependantLog.info(debugmsg+"successfully set focus to top window.");
			}else{
				IndependantLog.warn(debugmsg+ " skipping top window setFocus since bypassFramesReset is enabled.");
			}
		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Failed to set focus on top window due to "+ getThrowableMessages(e)+ ": "+ e.getMessage());
			focused = false;
		}

		try{
			new Actions(getWebDriver()).moveToElement(element).perform();
			focused = true;
		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Failed to set focus on WebElement due to "+ getThrowableMessages(e)+ ": "+ e.getMessage());
			focused = false;
		}

		try{
			//Try to set focus by click or by send empty string.
			if(WebDriverGUIUtilities.isTypeMatched(element, CFEditBox.LIBRARY_NAME)){
				//Use Robot click to set focus. It is a little risky, as the click might invoke other reaction of the AUT.
				IndependantLog.info(debugmsg+"using Click to set focus.");
				WDLibrary.click(element, null, null, WDLibrary.MOUSE_BUTTON_LEFT);
			}else{
				IndependantLog.info(debugmsg+"using sending empty String to set focus.");
				element.sendKeys("");
			}
			focused = true;
		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Failed to set focus on WebElement due to "+ getThrowableMessages(e)+ ": "+ e.getMessage());
			focused = false;
		}

		if(!focused){
			IndependantLog.error(debugmsg+"Failed to set focus on WebElement!");
		}

		return focused;
	}

	/**
	 * Attempts to fire (dispatchEvent) a MouseEvent.
	 * The MouseEvent should be suitable and contain all relevant and necessary information for the event.
	 * This is typically used to re-fire a MouseEvent that might have been captured with DocumentClickCapture
	 * but was not allowed to propagate to the actual element.
	 * @param event
	 * @throws SeleniumPlusException on an execution error.
	 * @throws NullPointerException if required parameters in the MouseEvent are null.
	 */
	public static void fireMouseEvent(MouseEvent event) throws SeleniumPlusException {
		if(event == null) throw new NullPointerException("fireMouseEvent event cannot be null.");
		if(event.EVENT_TARGET == null)throw new NullPointerException("fireMouseEvent event.EVENT_TARGET cannot be null.");

		StringBuffer jsScript = new StringBuffer();
		jsScript.append(JavaScriptFunctions.fireMouseEvent(event));
		jsScript.append("fireMouseEvent(arguments[0],arguments[1]);\n");
		IndependantLog.info("WDLibrary.fireMouseEvent firing...");
		WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), event.EVENT_TARGET, event.EVENT_VIEW);
	}

	/**An instance of WD_XMLHttpRequest, used to send HTTP request.*/
	public static WD_XMLHttpRequest AJAX = new WD_XMLHttpRequest();

	/**
	 * Implements {@link XMLHttpRequest}, through {@link WDLibrary} to execute javascript
	 * {@link JavaScriptFunctions#sendHttpRequest(Map)} to handle HTTP Request. If the execution
	 * is asynchronous, some value of {@link Key} will be stored in javascript global variables,
	 * and they can be retrieved later.
	 *
	 * @see org.safs.net.XMLHttpRequest
	  */
	public static class WD_XMLHttpRequest extends XMLHttpRequest{

		/**
		 * Implementation: through {@link WDLibrary}, execute {@link JavaScriptFunctions#sendHttpRequest(Map)}.<br>
		 * For synchronous execution, all results will be stored in the returned Map result with one value of {@link Key};<br>
		 * For asynchronous execution, except {@link Key#READY_STATE}, the other values will not be in the returned Map result,
		 * they will be stored in javascript global variables:<br>
		 * <pre>
		 *   {@link #VARIABLE_STATUS}:            HTTP response status number
		 *   {@link #VARIABLE_STATUS_TEXT}:       HTTP response status text
		 *   {@link #VARIABLE_RESPONSE_HEADERS}:  HTTP response headers
		 *   {@link #VARIABLE_RESPONSE_TEXT}:     HTTP response as string
		 *   {@link #VARIABLE_RESPONSE_XML}:      HTTP response as XML data
		 * </pre>
		 *
		 * @throws SeleniumPlusException
		 * @see {@link #getReadyState()}
		 */
		@Override
		@SuppressWarnings({ "unchecked" })
		public Map<String, Object> execute(HttpCommand command, String url, boolean async, Map<String, String> headers, String data) throws SeleniumPlusException {
			if(!StringUtils.isValid(url)) throw new SeleniumPlusException("The request url is null or is empty!");

			String debugmsg = StringUtils.debugmsg(false);

			String parameters = ""+(data==null?"":"with data '"+data+"'")+((headers==null||headers.isEmpty())?"":" with request headers "+headers);
			IndependantLog.info(debugmsg+" performing '"+command.value()+"' on '"+url+"' "+parameters);

			Object result = null;
			StringBuffer jsScript = new StringBuffer();
			jsScript.append(JavaScriptFunctions.sendHttpRequest(headers));
			//jsScript.append(" return sendHttpRequest(url , command, async, data, headers);\n");
			jsScript.append(" return sendHttpRequest(arguments[0],arguments[1],arguments[2],arguments[3],arguments[4]);\n");
			//TODO Do we need to encode the url and form-data?
			//		url = StringUtils.urlEncode(url);
			//		data = StringUtils.urlEncode(data);
			if(data==null){
				result = executeScript(jsScript.toString(), url, command.value(), async);
			}else{
				result = executeScript(jsScript.toString(), url, command.value(), async, data);
			}

			if(async){
				IndependantLog.debug(debugmsg+" the http request is executed aynchronously. We should not expect any result immediately.");
			}
			if(result==null){
				IndependantLog.warn(debugmsg+" the response is null!");
				return null;
			}

			if(result instanceof Map){
				return (Map<String, Object>) result;
			}else{
				IndependantLog.warn(debugmsg+" the returned result is not a Map! Need modify source code to parse it!");
				return null;
			}
		}

		/**
		 * Get javascript global variable {@link #VARIABLE_READY_STATE}.
		 */
		@Override
		public String getReadyState(){
			return String.valueOf(js_getGlobalVariable(VARIABLE_READY_STATE));
		}
		/**
		 * Get javascript global variable {@link #VARIABLE_RESPONSE_HEADERS}.
		 */
		@Override
		public Object getResponseHeaders(){
			return js_getGlobalVariable(VARIABLE_RESPONSE_HEADERS);
		}
		/**
		 * Get javascript global variable {@link #VARIABLE_RESPONSE_TEXT}.
		 */
		@Override
		public String getResponseText(){
			return String.valueOf(js_getGlobalVariable(VARIABLE_RESPONSE_TEXT));
		}
		/**
		 * Get javascript global variable {@link #VARIABLE_RESPONSE_XML}.
		 */
		@Override
		public Object getResponseXml(){
			return js_getGlobalVariable(VARIABLE_RESPONSE_XML);
		}
		/**
		 * Get javascript global variable {@link #VARIABLE_STATUS}.
		 */
		@Override
		public String getHttpStatus(){
			return String.valueOf(js_getGlobalVariable(VARIABLE_STATUS));
		}
		/**
		 * Get javascript global variable {@link #VARIABLE_STATUS_TEXT}.
		 */
		@Override
		public String getHttpStatusText(){
			return String.valueOf(js_getGlobalVariable(VARIABLE_STATUS_TEXT));
		}
	}

	/**
	 * @return String, the name of the browser where test is running. Or null if something wrong happens.
	 */
	public static String getBrowserName(){
		WebDriver wd = WDLibrary.getWebDriver();
		if(wd instanceof RemoteDriver){
			return ((RemoteDriver) wd).getBrowserName();
		}
		return null;
	}

	/**
	 * @return String, the version of the browser where test is running. Or null if something wrong happens.
	 */
	public static String getBrowserVersion(){
		WebDriver wd = WDLibrary.getWebDriver();
		if(wd instanceof RemoteDriver){
			return ((RemoteDriver) wd).getBrowserVersion();
		}
		return null;
	}

	/**
	 * @return String, the name of the platform where the browser is running. Or null if something wrong happens.
	 */
	public static String getPlatform(){
		WebDriver wd = WDLibrary.getWebDriver();
		if(wd instanceof RemoteDriver){
			return ((RemoteDriver) wd).getPlatform();
		}
		return null;
	}

	/**
	 * @return String, the version of 'selenium server' with which the test is running. Or null if something wrong happens.
	 */
	public static String getDriverVersion(){
		WebDriver wd = WDLibrary.getWebDriver();
		if(wd instanceof RemoteDriver){
			return ((RemoteDriver) wd).getDriverVersion();
		}
		return null;
	}

	/**
	 * Check some known issue for a certain keyword.<br>
	 * <b>NOTE: We should call {@link #startBrowser(String, String, String, int, boolean, Map)} before calling this method.
	 *          so that we can get the browser's name and browser's version to check.</b>
	 * @param keyword String, the keyword to check for, such like "GetURL"
	 * @throws SeleniumPlusException will be thrown out if a known issue is checked.<br>
	 */
	public static void checkKnownIssue(String keyword) throws SeleniumPlusException{
		String browserName = getBrowserName();
		String browserVersion = getBrowserVersion();
		IndependantLog.debug(StringUtils.debugmsg(false)+" browserName="+browserName+" browserVersion="+browserVersion);

		if(DDDriverCommands.GETURL_KEYWORD.equalsIgnoreCase(keyword) ||
		   DDDriverCommands.SAVEURLTOFILE_KEYWORD.equalsIgnoreCase(keyword) ||
		   DDDriverCommands.VERIFYURLCONTENT_KEYWORD.equalsIgnoreCase(keyword) ||
		   DDDriverCommands.VERIFYURLTOFILE_KEYWORD.equalsIgnoreCase(keyword)){
			//Check the known issue with selenium-standalone2.47.1 and Firefox 42.0
			//These keywords will be skipped for FireFox until we find the reason why 'AJAX execution is stuck with FireFox'.
			//With Firefox 65.0.1 and gecko-driver 0.24.0, the keyword works!
			if(SelectBrowser.BROWSER_NAME_FIREFOX.equalsIgnoreCase(browserName)
				&& browserVersion.compareTo("65.0.1")<0
//				&& "2.47.1".equals(getDriverVersion())
				){
//				throw new SeleniumPlusException("For keyword '"+keyword+"': known issue with selenium-standalone2.47.1 and Firefox ");
				throw new SeleniumPlusException("For keyword '"+keyword+"': execution stuck : known issue with Firefox!");
			}
			//Check the known issue with Chrome 76.0.3809.100, see defect S1529467.
			if(SelectBrowser.BROWSER_NAME_CHROME.equalsIgnoreCase(browserName)
					&& browserVersion.startsWith("76.0.3809")
//				&& "2.47.1".equals(getDriverVersion())
					){
				throw new SeleniumPlusException("For keyword '"+keyword+"': execution fails with stale element : known issue with Chrome '"+browserVersion+"'!");
			}
		}else if(DDDriverCommands.STARTWEBBROWSER_KEYWORD.equalsIgnoreCase(keyword)){
			//Check the known issue with Chrome 77.0.3865.120 and 78.0.3904.70, see defect S1529467.
			if(SelectBrowser.BROWSER_NAME_CHROME.equalsIgnoreCase(browserName)
					&& (browserVersion.startsWith("77.0.3865")||browserVersion.startsWith("78.0.3904"))
					){
				throw new SeleniumPlusException("For keyword '"+keyword+"': execution fails to load page : known issue with Chrome '"+browserVersion+"'!");
			}
		}
	}

	/**
	 * Click a component with an offset. This API will not verify that the click does happen.<br>
	 * If you want to make sure of that, please call {@link #click(WebElement, Point)} instead.<br>
	 * <br>
	 * Sometimes we want to click without verification, for example, to show an Alert.<br>
	 * With presence of Alert, any call to Selenium API will throw out UnhandledAlertException<br>
	 * and close the Alert automatically. Our API {@link #click(WebElement, Point)} will call<br>
	 * Selenium API for verification, so it is unable to open the Alert successfully.<br>
	 *
	 * @param component WebElement, the component to click
	 * @param offset Point, the offset (relative to component) to click at
	 * @param optional String[], the optional parameters
	 * <ul>
	 * <b>optional[0] autoscroll</b> boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
	 * <b>optional[1] verify</b> boolean, verify if the component is shown on page. The default value is true.<br>
	 * <b>optional[2] refresh</b> boolean, if the element will be refreshed after scrolling.
	 *                                      Normally we should refresh the component so that selenium API will return correct coordinates of the component.
	 *                                      But refresh will cost a lot of time and it will slow down the whole test, so we will not refresh the component by default.<br>
	 * </ul>
	 * @return boolean true if succeed.
	 * @see #click(WebElement, Point)
	 */
	public static boolean clickUnverified(WebElement component, Point offset, String... optional){
		String debugmsg = StringUtils.debugmsg(false);

		boolean autoscroll = parseAutoScroll(optional);

		try {
			IndependantLog.debug(debugmsg+" click with parameter componet:"+component+", offset:"+offset);
			//Create a combined actions according to the parameters
			Actions actions = new Actions(getWebDriver());

			if(autoscroll){
//				if(offset!=null) actions.moveToElement(component, offset.x, offset.y);
				if(offset!=null) actions = moveToElement(actions, component, offset.x, offset.y);
				else actions.moveToElement(component);
			}
			IndependantLog.debug(debugmsg+" Try Selenium API to click.");
			actions.click().perform();

			return true;
		} catch (Exception e) {
			IndependantLog.warn(debugmsg+" Failed with Selenium API, met "+StringUtils.debugmsg(e)+". Try Robot click.");

			try {
				if(autoscroll) showOnPage(component, removeFirstParameter(optional));
				Point p = WDLibrary.getScreenLocation(component);

				if(offset!=null) p.translate(offset.x, offset.y);
				else p.translate(component.getSize().width/2, component.getSize().height/2);

				RBT.click(p, null, WDLibrary.MOUSE_BUTTON_LEFT, 1);

				return true;
			} catch (Exception e1) {
				IndependantLog.error(debugmsg+" Failed with Robot click!");
			}
			return false;
		}
	}

	/**
	 * Get the text of each element object in an array, and add it to a list, then return the list.
	 * @param elements Element[], an array of element object
	 * @return List<String>, a list of element's text
	 */
	public static List<String> convertElementArrayToList(Element[] elements){
		String debugmsg = StringUtils.debugmsg(false);
		List<String> content = new ArrayList<String>();
		try{
			String value = null;
			for(Element element:elements){
				value = element.contentValue();
				if(value!=null) content.add(value);
			}
		}catch(Exception e){
			IndependantLog.debug(debugmsg+StringUtils.debugmsg(e));
		}

		return content;
	}

	/**
	 * Close the Alert-Modal-Dialog associated with a certain browser identified by ID.<br>
	 * It will get the cached webdriver according to the browser's id, and close the 'alert' through that webdriver.<br>
	 * <b>Note:</b>This API will NOT change the current WebDriver. {@link #getWebDriver()} will still return the same object.<br>
	 *
	 * @param accept boolean, if true then accept (click OK) the alert; otherwise dismiss (click Cancel) the alert.
	 * @param optionals String
	 * <ul>
	 * <b>optionals[0] timeoutWaitAlertPresence</b> int, timeout in seconds to wait for the presence of Alert.
	 *                                                   If not provided, default is 2 seconds.<br>
	 * <b>optionals[1] browserID</b> String, the ID to get the browser on which the 'alert' will be closed.
	 *                                       If not provided, the current browser will be used.<br>
	 * </ul>
	 * @throws SeleniumPlusException, if WebDriver cannot be got according to the parameter browserID.<br>
	 *                                if Alert is not present with timeout.<br>
	 */
	public static void closeAlert(boolean accept, String... optionals) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		String browserID = null;

		if(optionals!=null && optionals.length>0){
			if(optionals.length>1 && StringUtils.isValid(optionals[1])) browserID=optionals[1];
		}

		try{
			Alert alert = waitAlert(optionals);

			if(accept){
				alert.accept();//OK button
			}else{
				alert.dismiss();//Cancel button
			}
		}catch(Exception e){
			String message = "Fail to "+(accept?"accept":"dismiss")+" alert dialog associated with "+(browserID==null? "current browser.":"browser '"+browserID+"'.");
			IndependantLog.error(debugmsg+message+" due to "+StringUtils.debugmsg(e));
			throw new SeleniumPlusException(message);
		}
	}

	/**
	 *
	 * Test the presence of Alert-Modal-Dialog associated with a certain browser identified by ID.<br>
	 * It will get the cached webdriver according to the browser's id, and get the 'alert' through that webdriver.<br>
	 * <b>Note:</b>This API will NOT change the current WebDriver. {@link #getWebDriver()} will still return the same object.<br>
	 *
	 * @param optionals String...
	 * <ul>
	 * <b>optionals[0] timeoutWaitAlertPresence</b> int, timeout in seconds to wait for the presence of Alert.
	 *                                                   If not provided, default is 2 seconds.<br>
	 *                                                   If it is provided as {@link #TIMEOUT_NOWAIT}, this method will try to get Alert without waiting.<br>
	 * <b>optionals[1] browserID</b> String, the ID to get the browser on which the 'alert' will be closed.
	 *                                       If not provided, the current browser will be used.<br>
	 * </ul>
	 * @return boolean, true if the Alert is present.
	 * @throws SeleniumPlusException if the WebDriver is null
	 * @see #waitAlert(String...)
	 */
	public static boolean isAlertPresent(String... optionals) throws SeleniumPlusException{
		String debugmsg = "WDLibrary.isAlertPresent(): ";
		try {
			if(waitAlert(optionals)!=null){
				return true;
			}else{
				IndependantLog.debug(debugmsg+"Failed to wait for the Alert. A null object was returned.");
			}
		} catch (SeleniumPlusException e) {
			IndependantLog.warn(debugmsg+"Failed to wait for the Alert. Met "+StringUtils.debugmsg(e));
			//If we didn't get the webdriver, then we will throw the exception out.
			if(SeleniumPlusException.CODE_OBJECT_IS_NULL.equals(e.getCode())) throw e;
		}
		return false;
	}

	/**
	 * Wait for the presence of Alert-Modal-Dialog associated with a certain browser identified by ID.<br>
	 * It will get the cached webdriver according to the browser's id, and get the 'alert' through that webdriver.<br>
	 * <b>Note:</b>This API will NOT change the current WebDriver. {@link #getWebDriver()} will still return the same object.<br>
	 *
	 * @param optionals String...
	 * <ul>
	 * <b>optionals[0] timeoutWaitAlertPresence</b> int, timeout in seconds to wait for the presence of Alert.
	 *                                                   If not provided, default is 2 seconds.<br>
	 *                                                   If it is provided as {@link #TIMEOUT_NOWAIT}, this method will try to get Alert without waiting.<br>
	 * <b>optionals[1] browserID</b> String, the ID to get the browser on which the 'alert' will be closed.
	 *                                       If not provided, the current browser will be used.<br>
	 * </ul>
	 * @return Alert, the current Alert on browser; it could be null if it is not present within timeout.
	 * @throws SeleniumPlusException, if WebDriver cannot be got according to the parameter browserID.<br>
	 *                                if Alert is not present within timeout.<br>
	 */
	private static Alert waitAlert(String... optionals) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		String browserID = null;
		int timeout = timeoutWaitAlert;
		Alert alert = null;

		if(optionals!=null && optionals.length>0){
			if(StringUtils.isValid(optionals[0])){
				try{ timeout = Integer.parseInt(optionals[0]); }catch(NumberFormatException e){}
			}
			if(optionals.length>1 && StringUtils.isValid(optionals[1])) browserID=optionals[1];
		}

		WebDriver webdriver = getWebDriver(browserID);

		if(webdriver==null){
			throw new SeleniumPlusException("cannot get webdriver according to id '"+browserID+"'",SeleniumPlusException.CODE_OBJECT_IS_NULL);
		}

		try{
			if(timeout==TIMEOUT_NOWAIT){
				alert = webdriver.switchTo().alert();//NoAlertPresentException
			}else{
				WebDriverWait wait = new WebDriverWait(webdriver, timeout);
				alert = wait.until(ExpectedConditions.alertIsPresent());//TimeoutException
			}

			return alert;
		}catch(Exception e){
			String message = "Fail to get alert dialog associated with "+(browserID==null? "current browser.":"browser '"+browserID+"'.");
			IndependantLog.warn(debugmsg+message+" due to "+StringUtils.debugmsg(e));
			throw new SeleniumPlusException(message);
		}
	}

	/**
	 * <b>Before running this test, the Selenium Sever should have already started</b> (it can be launched by "java org.safs.selenium.webdriver.lib.RemoteDriver" ).<br>
	 *
	 * @see RemoteDriver#main(String[])
	 */
	private static void test_ajax_call(String browser){
		final String debugmsg = StringUtils.debugmsg(false);
		String url = "http://www.thomas-bayer.com/";
		final String ID = "thomas";
		int timeout = 10;
		boolean isRemote = true;

		final String ajaxRequestURL = "http://www.thomas-bayer.com/sqlrest/";
		final Map<String, String> headers = new HashMap<String, String>();
		final Map<String, Object> resultMap = new HashMap<String, Object>();
		final AtomicBoolean resultReady = new AtomicBoolean(false);
		//Open the URL by Selenium, on which page the AJAX request will be sent out
		try {
			System.out.println(debugmsg+" launching page '"+url+"' in browser '"+browser+"'.");
			WDLibrary.startBrowser(browser, url, ID, timeout, isRemote);

			checkKnownIssue(DDDriverCommands.GETURL_KEYWORD);

			Thread threadGetUrl = new Thread(new Runnable(){
				@Override
				public void run() {
					try {
						//For firefox42.0 with selenium2.47.1, following call will block for ever
						Map<String, Object> results = WDLibrary.AJAX.getURL(ajaxRequestURL, headers);
						for(String key:results.keySet()){
							resultMap.put(key, results.get(key));
						}
						resultReady.set(true);
					} catch (Throwable e) {
						System.err.println(debugmsg+" AJAX.getURL Thread: Met "+StringUtils.debugmsg(e));
					}
				}
			});
			threadGetUrl.setDaemon(true);
			threadGetUrl.start();
			System.out.println(debugmsg+"Waitting for the response from ajax request.");
			threadGetUrl.join(10*1000);
			if(!resultReady.get()){
				System.err.println("Cannot get result ready from url '"+ajaxRequestURL+"'");
			}else{
				String content = String.valueOf(resultMap.get(Key.RESPONSE_TEXT.value()));
				System.out.println(debugmsg+" Got http response\n"+content);
			}
		}catch(Exception e){
			System.err.println(debugmsg+" Met "+StringUtils.debugmsg(e));
		}finally{
			Thread threadGetUrl = new Thread(new Runnable(){
				@Override
				public void run() {
					try {
						//For firefox42.0 with selenium2.47.1, following call will block for ever
						WDLibrary.stopBrowser(ID);
						System.out.println(debugmsg+" page '"+ID+"' has been stopped.\n");
					} catch (Throwable e) {
						System.err.println(debugmsg+" Stop browser: Met "+StringUtils.debugmsg(e));
					}
				}
			});
			threadGetUrl.setDaemon(true);
			threadGetUrl.start();
		}
	}

	private static void test_ajax_call(){
		String[] browsers = {
				             SelectBrowser.BROWSER_NAME_CHROME,
				             SelectBrowser.BROWSER_NAME_EDGE,
				             SelectBrowser.BROWSER_NAME_FIREFOX
				             };
		for(String browser:browsers){
			test_ajax_call(browser);
		}
	}

	/**
	 * Before calling this method, we could start chromedriver.exe firstly.
	 */
	private static void test_kill_extraProcess(){
		try {
			String host = "";
			List<ProcessInfo> killedList = UtilsIndependent.killChromeDriver(host);
			for(ProcessInfo p:killedList){
				System.out.println("on host "+host+", process "+p.getId()+" has been terminated. The return code is "+p.getWmiTerminateRC());
			}

			killedList = UtilsIndependent.killIEDriverServer(host);
			for(ProcessInfo p:killedList){
				System.out.println("on host "+host+", process "+p.getId()+" has been terminated. The return code is "+p.getWmiTerminateRC());
			}

			host = "tadsrv";
			killedList = UtilsIndependent.killChromeDriver(host);
			for(ProcessInfo p:killedList){
				System.out.println("on host "+host+", process "+p.getId()+" has been terminated. The return code is "+p.getWmiTerminateRC());
			}

			killedList = UtilsIndependent.killIEDriverServer(host);
			for(ProcessInfo p:killedList){
				System.out.println("on host "+host+", process "+p.getId()+" has been terminated. The return code is "+p.getWmiTerminateRC());
			}

		} catch (SAFSException e) {
			e.printStackTrace();
		}

	}

	/**
	 * This method will input 'user', 'password' and '{Enter}' into a Basic Authentication dialog.<br>
	 * But the input action will be delayed amount of time, which is specified by parameter 'delayInSecond'.<br>
	 * <b>Note: It is supposed that the focus is on the 'user name' input box. And TAB can switch focus between components.</b>
	 *
	 * @param user String, the user name
	 * @param password String, the password
	 * @param delayInSecond int, delay in second, before the thread starts
	 */
	public static void handleBasicAuthentication(final String user, final String password, final int delayInSecond){
		Thread worker = new Thread(
				new Runnable(){
					@Override
					public void run() {
						int pause = 1000;
						String debugmsg = "WDLibrary.handleBasicAuthentication(): ";
						StringUtils.sleep(delayInSecond*1000);

						Object result = null;
						try{
							result = RBT.inputKeys(user+"{Tab}");
							if(StringUtils.convertBool(result)){
								StringUtils.sleep(pause);
								result = RBT.inputKeys(password+"{Tab}");
								if(StringUtils.convertBool(result)){
									StringUtils.sleep(pause);
									result = RBT.inputKeys("{Enter}");
									if(StringUtils.convertBool(result)){
										StringUtils.sleep(pause);
									}else{
										IndependantLog.error(debugmsg+" Failed to input 'Enter'.");
									}
								}else{
									IndependantLog.error(debugmsg+" Failed to input password.");
								}
							}else{
								IndependantLog.error(debugmsg+" Failed to input user '"+user+"'.");
							}
						}catch(Exception e){
							IndependantLog.error(debugmsg+" Failed due to "+e.getMessage());
						}
					}
				}
		);
		worker.start();
	}

	/**
	 * This class represents a Robot, which can do work locally (thru local org.safs.robot.Robot) or remotely (thru a RMI agent).<br>
	 *
	 * @author Lei Wang
	 *
	 */
	public static class RBT{
		/**
		 * Use Robot to click locally or remotely (through RMI).
		 * Use the current WebDriver as a RemoteDriver to provide an RMI agent.
		 *
		 * @param location	Point, The screen location to click at.
		 * @param specialKey	Keys, The selenium Keys value, representing the key (such as Ctrl, Alt) pressed during mouse click.
		 * @param mouseButtonNumber	int, Representing the mouse button to click, such as {@link WDLibrary#MOUSE_BUTTON_LEFT}.
		 * @param nclicks	int, How many times to click mouse.
		 *
		 * @throws Exception
		 */
		public static void click(Point location, Keys specialKey, int mouseButtonNumber, int nclicks) throws Exception{
			if(getBypassRobot()) throw new SeleniumPlusException("Bypass Robot, click action will be handled by other means, such as selenium.");

			WebDriver wd = getWebDriver();
			RemoteDriver rd = (wd instanceof RemoteDriver)? (RemoteDriver) wd : null;
			click(rd, location, specialKey, mouseButtonNumber, nclicks);
		}

		/**
		 * Use Robot to click locally or remotely (through RMI).
		 *
		 * @param rd	RemoteDriver, The Remote Driver with an RMI agent.
		 * @param location	Point, The screen location to click at.
		 * @param specialKey	Keys, The selenium Keys value, representing the key (such as Ctrl, Alt) pressed during mouse click.
		 * @param mouseButtonNumber	int, Representing the mouse button to click, such as {@link WDLibrary#MOUSE_BUTTON_LEFT}.
		 * @param nclicks	int, How many times to click mouse.
		 *
		 * @throws Exception
		 */
		public static void click(RemoteDriver rd, Point location, Keys specialKey, int mouseButtonNumber, int nclicks) throws Exception{
			if(getBypassRobot()) throw new SeleniumPlusException("Bypass Robot, click action will be handled by other means, such as selenium.");

			if(rd==null || rd.isLocalServer()){
				if(specialKey==null){
					Robot.click(location.x, location.y, mouseButtonNumber, nclicks);
				}else{
					Robot.clickWithKeyPress(location.x, location.y, mouseButtonNumber, toJavaKeyCode(specialKey), nclicks);
				}
			}
			else{// handle remote selenium
				if(rd.rmiAgent != null){
					if(specialKey==null){
						rd.rmiAgent.remoteClick(location.x, location.y, mouseButtonNumber, nclicks);
					}else{
						rd.rmiAgent.remoteClick(location.x, location.y, toJavaKeyCode(specialKey), nclicks);
					}
				}else{
					throw new ServerException("RMI Agent is not available.");
				}
			}
		}

		/**
		 * Press down a Key locally if the current RemoteDriver is null or is local;
		 * otherwise if the current RemoteDriver is remote, then press down a key remotely.
		 *
		 * @param keycode int, keycode Key to press (e.g. <code>KeyEvent.VK_A</code>)
		 * @return boolean, true if success
		 */
		public static boolean keyPress(int keycode) throws SeleniumPlusException{
			if(getBypassRobot()) throw new SeleniumPlusException("Bypass Robot, keyPress action will be handled by other means, such as selenium.");

			WebDriver wd = getWebDriver();
			RemoteDriver rd = (wd instanceof RemoteDriver)? (RemoteDriver) wd : null;
			return keyPress(rd, keycode);
		}

		/**
		 * Press down a Key locally if the RemoteDriver 'rd' is null or is local;
		 * otherwise if the RemoteDriver 'rd' is remote, then press down a key remotely.
		 *
		 * @param rd	RemoteDriver, The Remote Driver with an RMI agent.
		 * @param keycode int, keycode Key to press (e.g. <code>KeyEvent.VK_A</code>)
		 * @return boolean, true if success
		 */
		public static boolean keyPress(RemoteDriver rd, int keycode) throws SeleniumPlusException{
			if(getBypassRobot()) throw new SeleniumPlusException("Bypass Robot, keyPress action will be handled by other means, such as selenium.");

			String debugmsg = "RBT.keyPress(): ";
			boolean result = false;

			if(rd==null || rd.isLocalServer()){
				result = Robot.keyPress(keycode);
			}else{// handle remote selenium
				if(rd.rmiAgent != null){
					try {
						rd.rmiAgent.remoteKeyPress(keycode);
						result = true;
					} catch (Exception e) {
						IndependantLog.error(debugmsg+"Met "+e.toString());
						result = false;
					}
				}else{
					IndependantLog.error(debugmsg+"RMI Agent is not available.");
					result = false;
				}
			}

			return result;
		}

		/**
		 * Release a Key locally if the current RemoteDriver is null or is local;
		 * otherwise if the current RemoteDriver is remote, then release a key remotely.
		 *
		 * @param keycode int, keycode Key to release (e.g. <code>KeyEvent.VK_A</code>)
		 * @return boolean, true if success
		 */
		public static boolean keyRelease(int keycode) throws SeleniumPlusException{
			if(getBypassRobot()) throw new SeleniumPlusException("Bypass Robot, keyRelease action will be handled by other means, such as selenium.");

			WebDriver wd = getWebDriver();
			RemoteDriver rd = (wd instanceof RemoteDriver)? (RemoteDriver) wd : null;
			return keyRelease(rd, keycode);
		}

		/**
		 * Release a Key locally if the RemoteDriver 'rd' is null or is local;
		 * otherwise if the RemoteDriver 'rd' is remote, then release a key remotely.
		 *
		 * @param rd	RemoteDriver, The Remote Driver with an RMI agent.
		 * @param keycode int, keycode Key to release (e.g. <code>KeyEvent.VK_A</code>)
		 * @return boolean, true if success
		 */
		public static boolean keyRelease(RemoteDriver rd, int keycode) throws SeleniumPlusException{
			if(getBypassRobot()) throw new SeleniumPlusException("Bypass Robot, keyRelease action will be handled by other means, such as selenium.");

			String debugmsg = "RBT.keyRelease(): ";
			boolean result = false;

			if(rd==null || rd.isLocalServer()){
				result = Robot.keyRelease(keycode);
			}else{// handle remote selenium
				if(rd.rmiAgent != null){
					try {
						rd.rmiAgent.remoteKeyRelease(keycode);
						result = true;
					} catch (Exception e) {
						IndependantLog.error(debugmsg+"Met "+e.toString());
						result = false;
					}
				}else{
					IndependantLog.error(debugmsg+"RMI Agent is not available.");
					result = false;
				}
			}

			return result;
		}

		/**
		 * Type keyboard input.
		 * The input goes to the current keyboard focus target.  The String input
		 * can include all special characters and processing as documented in the
		 * {@link CreateUnicodeMap} class.<br>
		 *
		 * The input happens locally if the current RemoteDriver is null or is local;
		 * otherwise if the current RemoteDriver is remote, then it happens remotely.
		 *
		 * @param keys String,  the keys to enter.
		 * @return Object Currently we return a Boolean(true) object, but this may be subject to change.
		 * @throws SeleniumPlusException if the "bypass robot" is set to true.
		 */
		public static Object inputKeys(String keys) throws SeleniumPlusException{
			if(getBypassRobot()) throw new SeleniumPlusException("Bypass Robot, inputKeys action will be handled by other means, such as selenium.");

			WebDriver wd = getWebDriver();
			RemoteDriver rd = (wd instanceof RemoteDriver)? (RemoteDriver) wd : null;
			return inputKeys(rd, keys);
		}

		/**
		 * Type keyboard input.
		 * The input goes to the current keyboard focus target.  The String input
		 * can include all special characters and processing as documented in the
		 * {@link CreateUnicodeMap} class.<br>
		 *
		 * The input happens locally if the current RemoteDriver is null or is local;
		 * otherwise if the current RemoteDriver is remote, then it happens remotely.
		 *
		 * @param keys String,  the keys to enter.
		 * @return Object Currently we return a Boolean(true) object, but this may be subject to change.
		 */
		private static Object _typeKeys(String keys) throws SeleniumPlusException{
			WebDriver wd = getWebDriver();
			RemoteDriver rd = (wd instanceof RemoteDriver)? (RemoteDriver) wd : null;
			return _typeKeys(rd, keys);
		}

		/**
		 * Type keyboard input.
		 * The input goes to the current keyboard focus target.  The String input
		 * can include all special characters and processing as documented in the
		 * {@link CreateUnicodeMap} class.<br>
		 *
		 * The input happens locally if the RemoteDriver 'rd' is null or is local;
		 * otherwise if the RemoteDriver 'rd' is remote, then it happens remotely.
		 *
		 * @param rd	RemoteDriver, The Remote Driver with an RMI agent.
		 * @param keys String,  the keys to enter.
		 * @return Object Currently we return a Boolean(true) object, but this may be subject to change.
		 * @throws SeleniumPlusException if the "bypass robot" is set to true.
		 */
		public static Object inputKeys(RemoteDriver rd, String keys) throws SeleniumPlusException{
			if(getBypassRobot()) throw new SeleniumPlusException("Bypass Robot, inputKeys action will be handled by other means, such as selenium.");
			return _typeKeys(rd, keys);
		}

		/**
		 * Type keyboard input.
		 * The input goes to the current keyboard focus target.  The String input
		 * can include all special characters and processing as documented in the
		 * {@link CreateUnicodeMap} class.<br>
		 *
		 * The input happens locally if the RemoteDriver 'rd' is null or is local;
		 * otherwise if the RemoteDriver 'rd' is remote, then it happens remotely.
		 *
		 * @param rd	RemoteDriver, The Remote Driver with an RMI agent.
		 * @param keys String,  the keys to enter.
		 * @return Object Currently we return a Boolean(true) object, but this may be subject to change.
		 */
		private static Object _typeKeys(RemoteDriver rd, String keys) throws SeleniumPlusException{

			String debugmsg = "RBT._typeKeys(): ";
			Object result = null;

			if(rd==null || rd.isLocalServer()){
				try {
					result = Robot.inputKeys(keys);
				} catch (AWTException e) {
					IndependantLog.error(debugmsg+"Met "+e.toString());
					result = false;
				}
			}else{// handle remote selenium
				if(rd.rmiAgent != null){
					try {
						rd.rmiAgent.remoteTypeKeys(keys);
						result = true;
					} catch (Exception e) {
						IndependantLog.error(debugmsg+"Met "+e.toString());
						result = false;
					}
				}else{
					IndependantLog.error(debugmsg+"RMI Agent is not available.");
					result = false;
				}
			}

			return result;
		}

		/**
		 * Type keyboard input characters unmodified.  No special key processing.
		 * The input goes to the current keyboard focus target.  The String input
		 * will be treated simply as literal text and typed as-is.<br>
		 *
		 * The input happens locally if the current RemoteDriver is null or is local;
		 * otherwise if the current RemoteDriver is remote, then it happens remotely.
		 *
		 * @param chars String,  the characters to enter.
		 * @return Object Currently we return a Boolean(true) object, but this may be subject to change.
		 */
		public static Object inputChars(String chars) throws SeleniumPlusException{
			if(getBypassRobot()) throw new SeleniumPlusException("Bypass Robot, inputChars action will be handled by other means, such as selenium.");

			WebDriver wd = getWebDriver();
			RemoteDriver rd = (wd instanceof RemoteDriver)? (RemoteDriver) wd : null;
			return inputChars(rd, chars);
		}

		/**
		 * Type keyboard input characters unmodified.  No special key processing.
		 * The input goes to the current keyboard focus target.  The String input
		 * will be treated simply as literal text and typed as-is.<br>
		 *
		 * The input happens locally if the RemoteDriver 'rd' is null or is local;
		 * otherwise if the RemoteDriver 'rd' is remote, then it happens remotely.
		 *
		 * @param rd	RemoteDriver, The Remote Driver with an RMI agent.
		 * @param chars String,  the characters to enter.
		 * @return Object Currently we return a Boolean(true) object, but this may be subject to change.
		 */
		public static Object inputChars(RemoteDriver rd, String chars) throws SeleniumPlusException{
			if(getBypassRobot()) throw new SeleniumPlusException("Bypass Robot, inputChars action will be handled by other means, such as selenium.");

			String debugmsg = "RBT.inputChars(): ";
			Object result = null;

			if(rd==null || rd.isLocalServer()){
				try {
					result = Robot.inputChars(chars);
				} catch (AWTException e) {
					IndependantLog.error(debugmsg+"Met "+e.toString());
					result = false;
				}
			}else{// handle remote selenium
				if(rd.rmiAgent != null){
					try {
						rd.rmiAgent.remoteTypeChars(chars);
						result = true;
					} catch (Exception e) {
						IndependantLog.error(debugmsg+"Met "+e.toString());
						result = false;
					}
				}else{
					IndependantLog.error(debugmsg+"RMI Agent is not available.");
					result = false;
				}
			}

			return result;
		}

		/**
		 * Scroll the mouse wheel by Java Robot.<br>
		 *
		 * The scroll happens locally if the current RemoteDriver is null or is local;
		 * otherwise if the current RemoteDriver is remote, then it happens remotely.
		 *
		 * @param wheelAmt int, the wheel amount to scroll.
		 * @return boolean true, if the scroll succeeds.
		 * @throws SeleniumPlusException if the "bypass robot" is set to true.
		 */
		public static boolean mouseWheel(int wheelAmt) throws SeleniumPlusException{
			if(getBypassRobot()) throw new SeleniumPlusException("Bypass Robot, mouseWheel action will be handled by other means, such as selenium.");

			return _mouseWheel(wheelAmt);
		}

		/**
		 * Scroll the mouse wheel by Java Robot.<br>
		 *
		 * The scroll happens locally if the RemoteDriver 'rd' is null or is local;
		 * otherwise if the RemoteDriver 'rd' is remote, then it happens remotely.
		 *
		 * @param rd	RemoteDriver, The Remote Driver with an RMI agent.
		 * @param wheelAmt int, the wheel amount to scroll.
		 * @return boolean true, if the scroll succeeds.
		 * @throws SeleniumPlusException if the "bypass robot" is set to true.
		 */
		public static boolean mouseWheel(RemoteDriver rd, int wheelAmt) throws SeleniumPlusException{
			if(getBypassRobot()) throw new SeleniumPlusException("Bypass Robot, mouseWheel action will be handled by other means, such as selenium.");

			return _mouseWheel(rd, wheelAmt);
		}

		/**
		 * Scroll the mouse wheel by Java Robot.<br>
		 *
		 * The scroll happens locally if the current RemoteDriver is null or is local;
		 * otherwise if the current RemoteDriver is remote, then it happens remotely.
		 *
		 * @param wheelAmt int, the wheel amount to scroll.
		 * @return boolean true, if the scroll succeeds.
		 */
		private static boolean _mouseWheel(int wheelAmt) throws SeleniumPlusException{
			WebDriver wd = getWebDriver();
			RemoteDriver rd = (wd instanceof RemoteDriver)? (RemoteDriver) wd : null;
			return _mouseWheel(rd, wheelAmt);
		}

		/**
		 * Scroll the mouse wheel by Java Robot.<br>
		 *
		 * The scroll happens locally if the RemoteDriver 'rd' is null or is local;
		 * otherwise if the RemoteDriver 'rd' is remote, then it happens remotely.
		 *
		 * @param rd	RemoteDriver, The Remote Driver with an RMI agent.
		 * @param wheelAmt int, the wheel amount to scroll.
		 * @return boolean true, if the scroll succeeds.
		 */
		private static boolean _mouseWheel(RemoteDriver rd, int wheelAmt) throws SeleniumPlusException{
			String debugmsg = "RBT._mouseWheel(): ";
			boolean result = false;

			if(rd==null || rd.isLocalServer()){
				result = Robot.mouseWheel(wheelAmt);

			}else{// handle remote selenium
				if(rd.rmiAgent != null){
					try {
						rd.rmiAgent.remoteMouseWheel(wheelAmt);
						result = true;
					} catch (Exception e) {
						IndependantLog.error(debugmsg+"Met "+e.toString());
						result = false;
					}
				}else{
					IndependantLog.error(debugmsg+"RMI Agent is not available.");
					result = false;
				}
			}

			return result;
		}
	}

	/**
	 * Before running this method, please read java doc of {@link #test_ajax_call(String)}
	 * @param args
	 */
	public static void main(String[] args){
		IndependantLog.setDebugListener(new DebugListener(){

			@Override
			public String getListenerName() {
				return null;
			}

			@Override
			public void onReceiveDebug(String message) {
				System.out.println(message);
			}

		});
		test_ajax_call();
		test_kill_extraProcess();

		try {
			stopSeleniumServer(null);
		} catch (SAFSException e) {
			IndependantLog.error("Met "+e.toString());
		}
	}
}
