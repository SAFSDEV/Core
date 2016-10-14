/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib;
/**
*
* History:<br>
*
*  <br>   NOV 19, 2013    (CANAGL) Initial release.
*  <br>   DEC 18, 2013    (SBJLWA) Add codes to support ComboBox (HTML tag &lt;select&gt;).
*  <br>   DEC 26, 2013    (SBJLWA) Move ComboBox class out; add methods useBrowser() and stopBrowser().
*                                  Modify startBrowser() to permit passing more browser parameters, such as proxy settings.
*  <br>   JAN 16, 2014    (DHARMESH) Updated reconnection browser support.
*  <br>   FEB 02, 2014	   (DHARMESH) Add Resize and Maximize WebBrowser window KW.
*  <br>   MAR 05, 2014    (SBJLWA) Add some methods related to mouse-click (based on webdriver API).
*  <br>   MAR 27, 2014    (SBJLWA) Get element's screen location and use SAFS-Robot to do click-related keywords firstly.
*  <br>   APR 15, 2014    (SBJLWA) Listen to the 'mousedown' event for detecting if the click/double has happened.
*  <br>   APR 15, 2014    (DHARMESH) Add HighLight keyword.
*  <br>   AUG 29, 2014    (DHARMESH) Add selenium grid host and port support.
*  <br>   DEC 02, 2014    (CANAGL) Fix SeBuilder Script imports to use UTF-8 Character Encoding.
*  <br>   DEC 09, 2014    (SBJLWA) Modify isVisible(): catch the un-expected exception.
*  <br>   JAN 06, 2015    (SBJLWA) Add method leftDrag().
*  <br>   JAN 15, 2015    (SBJLWA) Add methods xxxDrag().
*  <br>   JAN 20, 2015    (DHARMESH) Add mobile support.
*  <br>   APR 16, 2015    (CANAGL) Try to fix Selenium Keyboard Actions in inputKeysSAFS2Selenium.
*  <br>   APR 29, 2015    (SBJLWA) Modify inputKeys/inputChars: if webelement cannot be focused, just log a warning message.
*                                  instead of throwing an Exception.
*  <br>   MAY 29, 2015    (CANAGL) Add support for setDelayBetweenKeystrokes
*  <br>   JUN 05, 2015    (SBJLWA) Add checkBeforeOperation(): check if element is stale or invisible before clicking.
*                                  Add isDisplayed(): not like isVisible(), it will not check the value of attribute 'visibility'.
*                                  Modify isVisible(), isStale(): set implicitWait directly, don't wait.
*  <br>   JUN 14, 2015    (SBJLWA) Modify focus(): if the component is "EditBox", use Robot Click to set focus.
*  <br>   JUN 15, 2015    (CANAGL) Add isPointInBounds to account for points that might be on the width & height edge.
*  <br>   JUL 24, 2015    (SBJLWA) Create class WD_XMLHttpRequest and its static instance AJAX.
*  <br>   JUL 25, 2015    (SBJLWA) Modify windowSetFocus(): remove the unnecessary parameter element.
*  <br>	  AUG 08, 2015    (Dharmesh) Added delayWaitReady for WaitOnClick.
*  <br>   SEP 07, 2015    (SBJLWA) Add method getElementOffsetScreenLocation().
*  <br>   OCT 12, 2015    (SBJLWA) Modify method getProperty(): get property by native SAP method.
*  <br>   OCT 30, 2015    (SBJLWA) Move method isVisible(), isDisplayed and isStale() to SearchObject class.
*  <br>   NOV 20, 2015    (SBJLWA) Add a unit test for "WDLibrary.AJAX.getURL".
*  <br>   NOV 26, 2015    (SBJLWA) Move some content from getScreenLocation() to getLocation().
*  <br>   DEC 02, 2015    (SBJLWA) Modify getLocation(): modify to get more accurate location.
*  <br>   DEC 03, 2015    (SBJLWA) Move "code of fixing browser client area offset problem" from getLocation() to getScreenLocation().
*  <br>   DEC 10, 2015    (SBJLWA) Add methods to handle clipboard on local machine or on RMI server machine.
*  <br>   DEC 24, 2015    (SBJLWA) Add methods to get browser's name, version, and selenium-server's version etc.
*                                  Add method checkKnownIssue().
*  <br>   FEB 05, 2016    (SBJLWA) Add method killChromeDriver().
*  <br>   FEB 26, 2016    (SBJLWA) Modify cilck(), doubleClick(): if the offset is out of element's boundary, disable the click listener.
*  <br>   FEB 29, 2016    (SBJLWA) Modify checkOffset(): if the offset is out of element's boundary, use the whole document as click event receiver.
*  <br>   MAR 02, 2016    (SBJLWA) Add clickUnverified(), closeAlert().
*                                  Add class RBT: To encapsulate the local Robot and Robot RMI agent.
*                                  Modify click() and doubleClick(): use RBT to do the click action.
*  <br>   MAR 14, 2016    (SBJLWA) Add isAlertPresent(), waitAlert().
*  <br>   MAR 29, 2016    (SBJLWA) Modify click() and doubleClick(): detect "Alert" after clicking.
*  <br>   APR 19, 2016    (SBJLWA) Modify click() doubleClick() etc.: Handle the optional parameter 'autoscroll'.
*/
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
import java.net.URL;
import java.rmi.ServerException;
import java.util.ArrayList;
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
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.safs.IndependantLog;
import org.safs.Processor;
import org.safs.SAFSException;
import org.safs.SAFSParamException;
import org.safs.StringUtils;
import org.safs.image.ImageUtils;
import org.safs.model.commands.DDDriverCommands;
import org.safs.natives.NativeWrapper;
import org.safs.net.IHttpRequest.Key;
import org.safs.net.XMLHttpRequest;
import org.safs.robot.Robot;
import org.safs.selenium.util.DocumentClickCapture;
import org.safs.selenium.util.JavaScriptFunctions;
import org.safs.selenium.util.MouseEvent;
import org.safs.selenium.webdriver.CFComponent;
import org.safs.selenium.webdriver.CFEditBox;
import org.safs.selenium.webdriver.SeleniumPlus.WDTimeOut;
import org.safs.selenium.webdriver.WebDriverGUIUtilities;
import org.safs.selenium.webdriver.lib.interpreter.WDScriptFactory;
import org.safs.selenium.webdriver.lib.interpreter.WDTestRunFactory;
import org.safs.text.FileUtilities;
import org.safs.text.INIFileReader;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.GenericProcessMonitor;
import org.safs.tools.GenericProcessMonitor.ProcessInfo;
import org.safs.tools.GenericProcessMonitor.WQLSearchCondition;
import org.safs.tools.input.CreateUnicodeMap;
import org.safs.tools.input.InputKeysParser;
import org.safs.tools.input.RobotKeyEvent;
import org.safs.tools.stringutils.StringUtilities;

import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.factory.StepTypeFactory;
import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;

/**
 *  <br>   NOV 19, 2013    (CANAGL) Initial release.
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
	 * <li> optional[0] autoscroll boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
	 * </ul>
	 * @throws SeleniumPlusException
	 */
	public static void click(WebElement clickable, String... optional) throws SeleniumPlusException{
		checkBeforeOperation(clickable, true);
		try {
			boolean autoscroll = parseAutoScroll(optional);
			if(autoscroll){
				try{new Actions(WDLibrary.getWebDriver()).moveToElement(clickable).perform();}
				catch(Throwable t){
					IndependantLog.error("Ignoring Selenium Click 'moveToElement' action failure caused by "+ t.getClass().getName());
				}				
			}
			clickable.click();
		}catch (Throwable th){
			IndependantLog.error("Selenium Click action failed.  Trying Robot...", th);
			click(clickable, null, null, MOUSE_BUTTON_LEFT);
		}
	}
	/**
	 * Click(Mouse Left Button) the WebElement at center with a special key pressed.
	 * @param clickable 	WebElement, the WebElement to click on
	 * @param specialKey	Keys, the special key to presse during the click
	 * @param optional String[], the optional parameters
	 * <ul>
	 * <li> optional[0] autoscroll boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
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
	 * <li> optional[0] autoscroll boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
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
	 * <li> optional[0] autoscroll boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
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
	 * <li> optional[0] autoscroll boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
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
	 * <li> optional[0] autoscroll boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
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
		
		if(optional!=null && optional.length>0 && optional[0]!=null){
			try{
				autoscroll = Boolean.parseBoolean(optional[0]);
			}catch(Exception e){
				IndependantLog.warn(StringUtils.debugmsg(false)+" Ignoring invalid parameter 'autoscroll' "+optional[0]+", met "+StringUtils.debugmsg(e));
			}
		}
		
		return autoscroll;
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
	 * <li> optional[0] autoscroll boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
	 * </ul>
	 * @throws SeleniumPlusException
	 */
	public static void click(WebElement clickable, Point offset, Keys specialKey, int mouseButtonNumber, String... optional) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(WDLibrary.class, "click");

		checkBeforeOperation(clickable, true);
		WebDriver wd = WDLibrary.getWebDriver();
		RemoteDriver rd = (wd instanceof RemoteDriver)? (RemoteDriver) wd : null;
		boolean autoscroll = parseAutoScroll(optional);
		
		if(autoscroll){
			try{new Actions(wd).moveToElement(clickable).perform();}
			catch(Throwable t){
				IndependantLog.error(debugmsg+"Ignoring Selenium Robot Click 'moveToElement' action failure caused by "+ t.getClass().getName());
			}			
		}
		
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
			RBT.click(rd, location, specialKey, mouseButtonNumber, 1);
			listener.startListening();
			
			//3. Wait for the 'click' event, check if the 'mousedown' event really happened.
			// CANAGL -- FIREFOX PROBLEM: A link that takes you to a new page (like the Google SignIn link) will
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
			try{Thread.sleep(DocumentClickCapture.LISTENER_LOOP_DELAY + DocumentClickCapture.delayWaitReady);}catch(Exception x){
				IndependantLog.debug(debugmsg + StringUtils.debugmsg(x));
			}

			try {
				//2. Perform the click action by Selenium
				IndependantLog.debug(debugmsg+" Try selenium API to click.");
				//Create a combined actions according to the parameters
				Actions actions = new Actions(getWebDriver());

				if(autoscroll){
					if(offset!=null) actions.moveToElement(clickable, offset.x, offset.y);
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
				try{
					//if the Robot click worked, but was not detected. If we clicked a link, original page has
					//disappeared, so the link doesn't exist neither, the WebElement is stale. WebDriver will
					//not throw StaleElementReferenceException until the 'implicit timeout' is reached.
					//But we don't want to waste that time, so just set 'implicit timeout' to 0 and don't wait.
					WDTimeOut.setImplicitlyWait(0, TimeUnit.SECONDS);
					actions.build().perform();
					listener.startListening();
					
					// Dharmesh: Not report waitForClick failure due to listener event not capture 
					// if click coordination out of component size or background. 
					// It is hard to find sibling component.
					try {event = listener.waitForClick(timeoutWaitClick);} 
					catch (Throwable the) {IndependantLog.debug(debugmsg+" waitForClick failed but not reported");};
					
					/*if(event != null)
						IndependantLog.debug(debugmsg+"click has been performed.");
					else{
						throw new SeleniumPlusException("Selenium Action.click failed to return the MouseEvent.");
					}*/
					
				}catch(StaleElementReferenceException x){
					listener.stopListening();  // chrome is NOT stopping!
					// the click probably was successful because the elements have changed!
					IndependantLog.debug(debugmsg+"StaleElementException (not found) suggests the click has been performed successfully.");
				}finally{
					IndependantLog.debug(debugmsg+"selenium API click finally stopping listener and resetting timeouts.");
					listener.stopListening();  // chrome is NOT stopping!
					WDTimeOut.resetImplicitlyWait(Processor.getSecsWaitForComponent(), TimeUnit.SECONDS);
				}
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
			IndependantLog.debug(debugmsg+"FINALLY stopping any ongoing listener, if any.");
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
	 * <li> optional[0] autoscroll boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
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
					if(offset!=null) actions.moveToElement(clickable, offset.x, offset.y);
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
				try{
					//unfortunately, if the Robot click worked, but was not detected, we have to wait the full
					//WebDriver implied timeout period for the perform() failure to occur.
					actions.build().perform();
					listener.startListening();
					event = listener.waitForClick(timeoutWaitClick);
					if(event != null)
						IndependantLog.debug(debugmsg+"doubleclick has been peformed.");
					else{
						throw new SeleniumPlusException("Selenium Action.doubleclick failed to detect the MouseEvent.");
					}
				}catch(StaleElementReferenceException x){
					// the click probably was successful because the elements have changed!
					IndependantLog.debug(debugmsg+"StaleElementException (not found) suggests the click has been performed successfully.");
				}
			} catch (Throwable th){
				IndependantLog.error(debugmsg, th);
				throw new SeleniumPlusException("doubleclick action failed: "+StringUtils.debugmsg(th));
			}
		}finally{
			listener.stopListening();
		}
	}

	/**
	 * Double-Click(Mouse Left Button) the WebElement at the center.
	 * @param clickable 	WebElement, the WebElement to click on
	 * @param optional String[], the optional parameters
	 * <ul>
	 * <li> optional[0] autoscroll boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
	 * </ul>
	 * @throws SeleniumPlusException
	 */
	public static void doubleClick(WebElement clickable, String... optional) throws SeleniumPlusException{
		doubleClick(clickable, null, null, MOUSE_BUTTON_LEFT, optional);
	}
	/**
	 * Double-Click(Mouse Left Button) the WebElement at the center with a special key pressed.
	 * @param clickable 	WebElement, the WebElement to click on
	 * @param specialKey	Keys, the special key to presse during the click
	 * @param optional String[], the optional parameters
	 * <ul>
	 * <li> optional[0] autoscroll boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
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
	 * <li> optional[0] autoscroll boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
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
			if(useOnPageFirstly){
				try{
					Coordinates c = ((RemoteWebElement)webelement).getCoordinates();
					p = c.onPage();
				}catch(UnsupportedOperationException x){
					IndependantLog.debug(debugmsg+"Selenium reports coordinates.onPage() is NOT yet supported.");
				}
				catch(Throwable t){
					IndependantLog.debug(debugmsg+"ignoring "+ StringUtils.debugmsg(t));
				}
			}
			if(p==null) p = webelement.getLocation();			
			IndependantLog.debug(debugmsg+"Selenium reports the WebElement 'CLIENT AREA' location as ("+p.x+","+p.y+")");

			//2. Add the frame's location (relative to the the browser client area)
			if(lastFrame!=null){
				p.x += lastFrame.getLocation().x;
				p.y += lastFrame.getLocation().y;
				//IndependantLog.debug(debugmsg+"added lastFrame offsets, new tentative 'CLIENT AREA' location ("+p.x+","+p.y+")");
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
			p.x += lastBrowserWindow.getClientX();
			p.y += lastBrowserWindow.getClientY();
			IndependantLog.debug(debugmsg+"added lastBrowserWindow ClientXY offsets, new tentative PAGE location ("+p.x+","+p.y+")");
			
			//2.1 Fix "client area LOCATION offset problem"
			if(lastFrame==null){
				//LEIWANG: I think that "client area LOCATION offset problem" is not related to the lastFrame.
				//Even the lastFrame is not null, that problem might exist too and we should try to fix it.
				//TODO remove the condition "if(lastFrame==null)" in future
				if (lastBrowserWindow.getClientX()==0 &&
					lastBrowserWindow.getBorderWidth()==0 && 
					lastBrowserWindow.getPageXOffset()==0 && 
					lastBrowserWindow.getWidth()>lastBrowserWindow.getClientWidth() ) {
					int diff = Math.round(lastBrowserWindow.getWidth()- lastBrowserWindow.getClientWidth())/2;
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
			p.x += lastBrowserWindow.getX();
			p.y += lastBrowserWindow.getY();
			//IndependantLog.debug(debugmsg+"added lastBrowserWindow XY offsets, new tentative SCREEN location ("+p.x+","+p.y+")");

			//4. minus scrollbar offset
			p.x -= lastBrowserWindow.getPageXOffset();
			p.y -= lastBrowserWindow.getPageYOffset();
			//IndependantLog.debug(debugmsg+"added lastBrowserWindow PageXY offsets, new tentative SCREEN location ("+p.x+","+p.y+")");

			if(debug){
				Robot.getRobot().mouseMove(p.x, p.y);
				Thread.sleep(500);
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
		ArrayList<RobotKeyEvent> list = new ArrayList();
		list.add(event);
		return keysparser.antiParse(list);
	}

	/**
	 * Does NOT clear any existing text in the control, but does attempt to insure the window/control has focus.
	 * <br><em>Purpose:</em>
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumGenericMasterFunctionsReference.htm#detail_InputKeys" alt="inputKeys Keyword Reference" title="inputKeys Keyword Reference">inputKeys</a>
	 * <p>
	 * Bypasses attempts to use AWT Robot for keystrokes.
	 * Attempts to convert SAFS keystrokes to Selenium low-level Actions keystrokes.
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
			Vector keys = keysparser.parseInput(keystrokes);
			Actions actions = new Actions(wd);

			if(we!=null) actions = actions.moveToElement(we);

			Iterator events = keys.iterator();
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
	 * Press down a Key by Java Robot. Call {@link #keyRelease(int)} to release the key.
	 * @param keycode int, keycode to press (e.g. <code>KeyEvent.VK_A</code>)
	 * @throws SeleniumPlusException if fail
	 * @see #keyRelease(int)
	 */
	public static void keyPress(int keycode) throws SeleniumPlusException{
		try {
			RemoteDriver wd = null;
			try{ wd = (RemoteDriver) getWebDriver();}catch(Exception x){}

			if(wd == null || wd.isLocalServer()){
				if(!Robot.keyPress(keycode)){
					throw new SeleniumPlusException("SAFS Robot Fail to press key "+KeyEvent.getKeyText(keycode));
				}
			}else {
				//try RMI server.
				wd.rmiAgent.remoteKeyPress(keycode);
			}
		} catch (Exception e) {
			throw new SeleniumPlusException("Unable to successfully complete keyPress due to "+ e.getMessage(), e);
		}
	}
	/**
	 * Release a Key by Java Robot. Release the key pressed by {@link #keyPress(int)}.
	 * @param keycode int, keycode to release (e.g. <code>KeyEvent.VK_A</code>)
	 * @throws SeleniumPlusException if fail
	 * @see #keyPress(int)
	 */
	public static void keyRelease(int keycode) throws SeleniumPlusException{
		try {
			RemoteDriver wd = null;
			try{ wd = (RemoteDriver) getWebDriver();}catch(Exception x){}

			if(wd == null || wd.isLocalServer()){
				if(!Robot.keyRelease(keycode)){
					throw new SeleniumPlusException("SAFS Robot Fail to release key "+KeyEvent.getKeyText(keycode));
				}
			}else {
				//try RMI server.
				wd.rmiAgent.remoteKeyRelease(keycode);
			}
		} catch (Exception e) {
			throw new SeleniumPlusException("Unable to successfully complete keyRelease due to "+ e.getMessage(), e);
		}
	}
	/**
	 * Scroll the mouse wheel by Java Robot.
	 * @param wheelAmt int, the wheel amount to scroll.
	 * @throws SeleniumPlusException if fail
	 */
	public static void mouseWheel(int wheelAmt) throws SeleniumPlusException{
		try {
			RemoteDriver wd = null;
			try{ wd = (RemoteDriver) getWebDriver();}catch(Exception x){}
			if(wd == null || wd.isLocalServer()){
				if(!Robot.mouseWheel(wheelAmt)){
					throw new SeleniumPlusException("SAFS Robot Fail to scroll mouse wheel.");
				}
			}else {
				//try RMI server.
				wd.rmiAgent.remoteMouseWheel(wheelAmt);
			}
		} catch (Exception e) {
			throw new SeleniumPlusException("Unable to successfully complete mouseWheel due to "+ e.getMessage(), e);
		}
	}

	/**
	 * Press down a Key by Selenium's Actions API. Call {@link #keyUp(Keys)} to release the key.
	 * @param keycode Keys, keycode to press (e.g. <code>Keys.CONTROL</code>)
	 * @throws SeleniumPlusException if fail
	 * @see #keyUp(Keys)
	 */
	public static void keyDown(Keys keycode) throws SeleniumPlusException{
		try {
			WebDriver wd = (WebDriver) getWebDriver();
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
			WebDriver wd = (WebDriver) getWebDriver();
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
		} catch (Exception e) {
			throw new SeleniumPlusException("Unable to successfully complete setWaitReaction due to "+ e.getMessage(), e);
		}
	}

	/**
	 * Does NOT clear any existing text in the control, but does attempt to insure the window/control has focus.
	 * <br><em>Purpose:</em>
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumGenericMasterFunctionsReference.htm#detail_InputKeys" alt="inputKeys Keyword Reference" title="inputKeys Keyword Reference">inputKeys</a>
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
		try {
			if(!focus(we)) IndependantLog.warn(debugmsg+" Fail to set focus to webelement "+we);

			RemoteDriver wd = null;
			try{ wd = (RemoteDriver) getWebDriver();}catch(Exception x){}
			if(wd == null || wd.isLocalServer()){
				IndependantLog.info(debugmsg+" sending '"+keystrokes+"' to local Robot.inputKeys.");
				Robot.inputKeys(keystrokes);
			}else {
				try{
					IndependantLog.info(debugmsg+" sending RMI Agent TypeKeys '"+keystrokes+"' to RMI Server");
					wd.rmiAgent.remoteTypeKeys(keystrokes);
				}catch(Exception e){
					IndependantLog.warn(debugmsg+" Fail RMI Agent TypeKeys '"+keystrokes+"' due to "+StringUtils.debugmsg(e));
					inputKeysSAFS2Selenium(we, keystrokes);
				}
			}
		} catch (Exception e) {
			throw new SeleniumPlusException("Unable to successfully complete InputKeys due to "+ e.getMessage(), e);
		}
	}

	/**
	 * Does NOT clear any existing text in the control, but does attempt to insure the window/control has focus.
	 * <br><em>Purpose:</em>
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumGenericMasterFunctionsReference.htm#detail_InputCharacters" alt="inputCharacters Keyword Reference" title="inputCharacters Keyword Reference">inputCharacters</a>
	 * @param we WebElement to send characters; if null, the keystrokes will be sent to the focused element.
	 * @param keystrokes/plain text to type.
	 * @throws SeleniumPlusException if we are unable to process the keystrokes successfully.
	 * @see org.safs.robot.Robot#inputKeys(String)
	 **/
	public static void inputChars(WebElement we, String keystrokes) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		try{
			if(!focus(we)) IndependantLog.warn(debugmsg+" Fail to set focus to webelement "+we);

			RemoteDriver wd = null;
			try{ wd = (RemoteDriver) getWebDriver();}catch(Exception x){}
			if(wd == null || wd.isLocalServer()){
				IndependantLog.info(debugmsg+" sending '"+keystrokes+"' to local Robot.inputChars.");
				Robot.inputChars(keystrokes);
			}else {
				try{
					IndependantLog.info(debugmsg+" sending RMI Agent TypeChars '"+keystrokes+"' to RMI Server");
					wd.rmiAgent.remoteTypeChars(keystrokes);
				}catch(Exception e){
					IndependantLog.warn(debugmsg+" Fail RMI Agent TypeChars '"+keystrokes+"' due to "+StringUtils.debugmsg(e));
					IndependantLog.info(debugmsg+" sending '"+keystrokes+"' to via WebElement.sendKeys.");
					we.sendKeys(keystrokes);
				}
			}
		} catch (Exception e) {
			throw new SeleniumPlusException("Unable to successfully complete InputCharacters due to "+ e.getMessage());
		}
	}
	
	/**
	 * Clear the clipboard on local machine or on machine where the RMI server is running.
	 * @throws SeleniumPlusException
	 */
	public static void clearClipboard() throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		
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
	 * @param millisStay double, the period to hover the mouse, in milliseconds
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

			//Pause a while
			StringUtilities.sleep(millisStay);

			//Move out the mouse
			//action.moveByOffset(-Robot.SCREENSZIE.width, -Robot.SCREENSZIE.height);//This will throw exception
			//action.build().perform();
			Robot.getRobot().mouseMove(-Robot.SCREENSZIE.width, -Robot.SCREENSZIE.height);

		}catch(Exception e) {
			IndependantLog.warn(StringUtils.debugmsg(false)+"Failed to hover mouse by Selenium API: "+ StringUtils.debugmsg(e));
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

		try{
			IndependantLog.debug(debugmsg+" drag from "+start+" to "+end+" relative to webelement.");
			translatePoints(we, start, end);
			Robot.altLeftDrag(start, end);

		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Failed to drag by SAFS Robot: "+ StringUtils.debugmsg(e));
			throw new SeleniumPlusException("Failed to drag.");
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

		try{
			IndependantLog.debug(debugmsg+" drag from "+start+" to "+end+" relative to webelement.");
			translatePoints(we, start, end);
			Robot.ctrlAltLeftDrag(start, end);

		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Failed to drag by SAFS Robot: "+ StringUtils.debugmsg(e));
			throw new SeleniumPlusException("Failed to drag.");
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

		try{
			IndependantLog.debug(debugmsg+" drag from "+start+" to "+end+" relative to webelement.");
			translatePoints(we, start, end);
			Robot.ctrlLeftDrag(start, end);

		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Failed to drag by SAFS Robot: "+ StringUtils.debugmsg(e));
			throw new SeleniumPlusException("Failed to drag.");
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

		try{
			IndependantLog.debug(debugmsg+" drag from "+start+" to "+end+" relative to webelement.");
			translatePoints(we, start, end);
			Robot.ctrlShiftLeftDrag(start, end);

		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Failed to drag by SAFS Robot: "+ StringUtils.debugmsg(e));
			throw new SeleniumPlusException("Failed to drag.");
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

		try{
			IndependantLog.debug(debugmsg+" drag from "+start+" to "+end+" relative to webelement.");
			translatePoints(we, start, end);
			Robot.shiftLeftDrag(start, end);

		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Failed to drag by SAFS Robot: "+ StringUtils.debugmsg(e));
			throw new SeleniumPlusException("Failed to drag.");
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

		try{
			IndependantLog.debug(debugmsg+" drag from "+start+" to "+end+" relative to webelement.");
			translatePoints(we, start, end);
			Robot.rightDrag(start, end);

		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Failed to drag by SAFS Robot: "+ StringUtils.debugmsg(e));
			throw new SeleniumPlusException("Failed to drag.");
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

		try{
			IndependantLog.debug(debugmsg+" drag from "+start+" to "+end+" relative to webelement.");
			translatePoints(we, start, end);
			Robot.leftDrag(start, end);

		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Failed to drag by SAFS Robot: "+ StringUtils.debugmsg(e));
			IndependantLog.debug(debugmsg+"Try to drag by Selenium API.");

			try{
				Actions actions = new Actions(lastUsedWD);
				actions = actions.moveToElement(we, start.x, start.y);
				actions = actions.clickAndHold().moveByOffset((end.x-start.x), (end.y-start.y)).release();
				actions.build().perform();
			}catch(Exception e1){
				IndependantLog.warn(debugmsg+"Failed to drag by Selenium API: "+ StringUtils.debugmsg(e1));
				throw new SeleniumPlusException("Failed to drag.");
			}
		}
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
     * @return Point, the offset point screen coordination; or null if any exception occured.
     *
     **/
    public static Point getElementOffsetScreenLocation(WebElement element, String offsetX, String offsetY){
    	String debugmsg = StringUtils.debugmsg(false);
    	
    	try {
    		Point screenLoc = WDLibrary.getScreenLocation(element);
    		Dimension dimemsion = element.getSize();
    		
    		//calc coords according to the offset and element's location and dimension
    		double dx, dy;
    		dx = ImageUtils.calculateAbsoluteCoordinate(screenLoc.getX(), dimemsion.getWidth(), offsetX);
    		dy = ImageUtils.calculateAbsoluteCoordinate(screenLoc.getY(), dimemsion.getHeight(), offsetY);

    		return new Point((int)dx, (int)dy);
    	}catch (Exception e) {
    		IndependantLog.error(debugmsg +": Exception", e);
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
	 * @param BrowserName - Browser name such as InternetExplorer, Chrome and FireFox.
	 * @param Url - Url including http protocol prefix.
	 * @param Id - Id or Title of the Browser incase of two instances needs.
	 * @param timeout - Implicit time out to be waited before throw exception.
	 * @param isRemote - Start interactive testcase development mode.
	 * @throws Exception
	 * @see {@link #startBrowser(String, String, String, int, boolean, HashMap)}
	 */
	public static void startBrowser(String BrowserName, String Url, String Id, int timeout, boolean isRemote) throws SeleniumPlusException{
		startBrowser(BrowserName, Url, Id, timeout, isRemote, null);
	}

	/**
	 * Start browser
	 * <p>
	 * Expects System Properties 'selenium.host' and 'selenium.port' to be set.<br>
	 * Otherwise, defaults to 'localhost' on port '4444'.
	 * <p>
	 * @param BrowserName - Browser name such as InternetExplorer, Chrome and FireFox.
	 * @param Url - Url including http protocol prefix.
	 * @param Id - Id or Title of the Browser incase of two instances needs.
	 * @param timeout - Implicit time out to be waited before throw exception.
	 * @param isRemote - Start interactive testcase development mode.
	 * @param extraParameters HashMap<String,Object>, can be used to pass more browser parameters, such as a firefox profile to use.
	 * @throws Exception
	 */
	public static void startBrowser(String BrowserName, String Url, String Id, int timeout, boolean isRemote, HashMap<String,Object> extraParameters) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(WDLibrary.class, "startBrowser");
		//previousDriver is the possible WebDriver with the same ID stored in the Cache.
		WebDriver previousDriver = null;

		String host = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_HOST);
			if (host == null || host.isEmpty()) host = SelectBrowser.DEFAULT_SELENIUM_HOST;
		String port = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_PORT);
			if (port == null || port.isEmpty()) port = SelectBrowser.DEFAULT_SELENIUM_PORT;

		if(BrowserName == null || BrowserName.length()==0){
			BrowserName = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_BROWSER_NAME);
			if(BrowserName == null || BrowserName.length()==0){
				BrowserName = SelectBrowser.BROWSER_NAME_FIREFOX;
				System.setProperty(SelectBrowser.SYSTEM_PROPERTY_BROWSER_NAME, BrowserName);
			}
		}
		if(Id == null || Id.equals("")){
			Id = String.valueOf("".hashCode());
		}

		if (!isRemote) {
			IndependantLog.warn(debugmsg+"attempting to start a local (not remote) browser instance...");
			SelectBrowser sb = new SelectBrowser();
			previousDriver = addWebDriver(Id,sb.getBrowserInstance(BrowserName, extraParameters));
			lastUsedWD.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
			lastUsedWD.manage().window().setSize(new Dimension(1024,768)); // default window size
			if(Url != null && Url.length()> 0) lastUsedWD.get(Url);

		} else {
			IndependantLog.warn(debugmsg+"attempting to start new session on remote server");
			try {
				SelectBrowser sb = new SelectBrowser();
				DesiredCapabilities capabilities = sb.getDesiredCapabilities(BrowserName, extraParameters);
				capabilities.setJavascriptEnabled(true);
				capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
				capabilities.setCapability(RemoteDriver.CAPABILITY_ID, Id); // custom id for session tracking
				capabilities.setCapability(RemoteDriver.CAPABILITY_RECONNECT, false); // custom id
				capabilities.setCapability(RemoteDriver.CAPABILITY_REMOTESERVER, host); // custom id
				//capabilities.setBrowserName(BrowserName); now it set from capabilities
				previousDriver = addWebDriver(Id,new RemoteDriver(new URL("http://" + host + ":" + port +"/wd/hub"),capabilities));
				lastUsedWD.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);

				try{
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
	 * Close browser (close all windows associated) indicated by ID.
	 * If the provided ID is associated with the "current" or "lastUsed" WebDriver
	 * the call to removeWebDriver will automatically "pop" the next WebDriver off
	 * the stack to be the new "current" or "lastUsed" WebDriver.
	 * @param ID	String, the id to identify the browser
	 * @throws IllegalArgumentException if the provided browser ID is null or not known as a running instance.
	 * @see #removeWebDriver(String)
	 */
	public static void stopBrowser(String ID) throws IllegalArgumentException{
		String debugmsg = StringUtils.debugmsg(WDLibrary.class, "stopBrowser");
		if (ID == null) throw new IllegalArgumentException("Browser ID provided was null.");
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
	 * Kill the process 'chromedriver.exe'.
	 * @param host String, the name of the machine on which the process 'chromedriver.exe' will be killed.
	 * @return List<ProcessInfo>, a list containing the killed process's id and kill-execution-return-code.
	 * @throws SAFSException
	 */
	public static List<ProcessInfo> killChromeDriver(String host) throws SAFSException{
		return killExtraProcess(host, "chromedriver.exe");
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
	 * Kill the process launched from executables located in %SAFSDIR%\samples\Selenium2.0\extra\ or %SELENIUM_PLUS%\extra\
	 * @param host String, the name of machine on which the process will be killed.
	 * @param processName String, the name of process to kill. like chromedriver.exe, IEDriverServer.exe etc.
	 * @return List<ProcessInfo>, a list containing the killed process's id and kill-execution-return-code.
	 * @throws SAFSException
	 */
	private static List<ProcessInfo> killExtraProcess(String host, String processName) throws SAFSException{
		if(!StringUtils.isValid(processName)){
			throw new SAFSParamException("The value of parameter 'processName' is NOT valid: "+processName);
		}
		IndependantLog.debug("WDLibrary.killExtraProcess(): killing process '"+processName+"' on machine '"+host+"'.");
		
		//wmic process where " commandline like '%d:\\seleniumplus\\extra\\chromedriver.exe%' and name = 'chromedriver.exe' "
		String wmiSearchCondition = GenericProcessMonitor.wqlCondition("commandline", "\\extra\\"+processName, true, false);
		wmiSearchCondition += " and "+ GenericProcessMonitor.wqlCondition("name", processName, false, false);
		WQLSearchCondition condition = new WQLSearchCondition(wmiSearchCondition);
		
		return GenericProcessMonitor.shutdownProcess(host, condition);
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
	 * @param element WebElement, the webelement to show.
	 *
	 * @return boolean, true if succeed
	 */
	public static boolean showOnPage(WebElement element){
		return showOnPage(element, new String[0]);
	}
	/**
	 * Try to show the webelement on the browser's page.<br>
	 * First, try to see if the web-element is already shown on page, if not shown on the page<br>
	 * then it will try to move the web-element to show it on page.<br>
	 * Finally, if the passed in parameter verify is true, it will return true only if the element
	 * is shown on the page;<br>
	 * if the passed in parameter verify is false, it will always return true.<br>
	 * @param element WebElement, the webelement to show.
	 * @param params optional<ul>
	 * <b>optionals[0] verify</b> boolean, verify that the component is shown on page. The default value is false.<br>
	 * </ul>
	 *
	 * @return boolean, true if succeed
	 *
	 * @history
	 * <br> May 18, 2015	sbjlwa	Get refreshed WebElement after moving it on the page.
	 */
	public static boolean showOnPage(WebElement element, String... optional){
		String debugmsg = StringUtils.debugmsg(false);

		if(isShowOnPage(element)) return true;

		boolean verify = false;
		if(optional!=null && optional.length>0){
			verify = StringUtilities.convertBool(optional[0]);
		}

		try {
			IndependantLog.debug(debugmsg+"make element visible in brower viewport.");
			Coordinates coordinate = ((Locatable) element).getCoordinates();
			coordinate.inViewPort();
			//after inViewPort(), the real element may move, but WebElement.getLocation() may still return the old value
			if(verify) {
				IndependantLog.debug(debugmsg+"refreshing element reference for verification.");
				element = CFComponent.refresh(element);
			}
			if(isShowOnPage(element)) return true;
		} catch (Exception e) {
			IndependantLog.warn(debugmsg+"Fail to show webelement in browser's viewport, due to "+StringUtils.debugmsg(e));
		}

		try {
			IndependantLog.debug(debugmsg+"scroll browser to the top-left corner of this component.");
			org.openqa.selenium.Point compLoc = element.getLocation();
			WDLibrary.scrollBrowserWindowTo(compLoc.x, compLoc.y, element);
			if(verify) {
				IndependantLog.debug(debugmsg+"refreshing element reference again for verification.");
				element = CFComponent.refresh(element);
			}
			if(isShowOnPage(element)) return true;
		} catch (Exception e) {
			IndependantLog.warn(debugmsg+"Fail to scroll browser to the top-left corner of webelement, due to "+StringUtils.debugmsg(e));
		}

		try {
			IndependantLog.debug(debugmsg+"align the top this component to top of browser's viewport.");
			WDLibrary.alignToTop(element);
			if(verify) {
				IndependantLog.debug(debugmsg+"refreshing element reference yet again for verification.");
				element = CFComponent.refresh(element);
			}
			if(isShowOnPage(element)) return true;
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

			int browserXOffset = (int) WDLibrary.lastBrowserWindow.getPageXOffset();
			int browserYOffset = (int) WDLibrary.lastBrowserWindow.getPageYOffset();
			//move the location according to the page-offset, get the location relative to the page
			elementLTLoc = elementLTLoc.moveBy(-browserXOffset, -browserYOffset);
			org.openqa.selenium.Point elementBRLoc = elementLTLoc.moveBy(elementD.width, elementD.height);

			//Get the bounds of the browser's page
			int browserClientW = (int) WDLibrary.lastBrowserWindow.getClientWidth();
			int browserClientH = (int) WDLibrary.lastBrowserWindow.getClientHeight();
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
				isShowOnPage = isLocationInBounds(elementLTLoc, browserPageBounds) && isPointInBounds(elementBRLoc, browserPageBounds);
			}

		}catch(Exception e){
			IndependantLog.error(debugmsg+"Fail due to "+StringUtils.debugmsg(e));
		}
		
		IndependantLog.debug(debugmsg+" return "+isShowOnPage);
		return isShowOnPage;
	}

	/**
	 * check if the point p locates in the Dimension.
	 * @param p Point, relative to the Dimension. Usually the top-left point of an item. 
	 * see if the Point is inside bounds of 0,0,bounds.width, bounds.height.
	 * @param bounds Dimension, the boundary
	 * @return boolean true if the point p locates in the Dimension.
	 * A point p on the extreme width or height boundary is NOT in-bounds.
	 */
	public static boolean isLocationInBounds(org.openqa.selenium.Point p, Dimension bounds){
		if(p==null || bounds==null) return false;
		return (0<p.x && p.x<bounds.width) && (0<p.y && p.y<bounds.height);
	}

	/**
	 * check if the Point p locates inside the Dimension.
	 * @param p Point, point relative to 0,0, bounds.width, bounds.height.  
	 * Usually this is the bottom-right point test of a boundary.
	 * @param bounds Dimension, the boundary
	 * @return boolean true if the point p locates in the Dimension. 
	 * A point p on the extreme width or height boundary IS considered in-bounds.
	 */
	public static boolean isPointInBounds(org.openqa.selenium.Point p, Dimension bounds){
		if(p==null || bounds==null) return false;
		return (0<p.x && p.x<=bounds.width) && (0<p.y && p.y<=bounds.height);
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
		stepTypes.setSecondaryPackage(factory.SRSTEPTYPE_PACKAGE);
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
	 * Run a SeBuilder JSON Script explictly using the existing WebDriver instance.
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

		try{
			focused = windowSetFocus(element);
			if(!focused) IndependantLog.warn("WDLibrary.focus fail to set focus to top window.");
			else IndependantLog.info("WDLibrary.focus successfully set focus to top window.");
			new Actions(getWebDriver()).moveToElement(element).perform();
			if(WebDriverGUIUtilities.isTypeMatched(element, CFEditBox.LIBRARY_NAME)){
				//Use Robot click to set focus. It is a little risky, it the click will invoke other reaction!
				IndependantLog.info("WDLibrary.focus using Click to set focus.");
				WDLibrary.click(element, null, null, WDLibrary.MOUSE_BUTTON_LEFT);
			}else{
				IndependantLog.info("WDLibrary.focus using sending empty String to set focus.");
				element.sendKeys("");
			}

		}catch(Exception e){
			IndependantLog.warn("Fail to set focus on WebElement due to "+ getThrowableMessages(e)+ ": "+ e.getMessage());
			focused = false;
		}
		return focused;
	}

	/**
	 * Get css values of the object
	 * @param element - WebElement
	 * @return - return map as key and value pair
	 */
	public static Map<String, String> getCssValues(WebElement element){

		String[] styleattr = {"color","display","float","font-family","font-size",
								"font-weight","height","white-space","width",
								"background-color","background-repeat",
								"visibility"};

		Map<String, String> map = new HashMap<String, String>();
		String val = null;
		for (int i = 0; i < styleattr.length; i++) {
			try{
				val = element.getCssValue(styleattr[i]);
				map.put(styleattr[i], val);
			}catch(Throwable t){}
		}
		return map;
	}

	/**
	 * get the value of a property. The property can be an attribute, a css-attribute, a true property field, or certain property methods.
	 * @param element WebElement, from which to retrieve the property
	 * @param property String, the property name
	 * @return String, the value of the property
	 * @throws SeleniumPlusException if the attribute or property is not found.
	 * @see #getProperties(WebElement)
	 */
	public static String getProperty(WebElement element, String property) throws SeleniumPlusException{
		String value = null;
		String dbg = "WDLibrary.getProperty() ";
		try {
			try{ value = element.getAttribute(property);}
			catch(Throwable x){
				IndependantLog.debug(dbg+ "getAttribute('"+property+"') threw "+ x.getClass().getName()+", "+x.getMessage());
			}
			if (value == null) {
				IndependantLog.debug(dbg+ "getAttribute('"+property+"') returned null.  Trying getCssValue.");
				try{ value = element.getCssValue(property); }
				catch(Throwable x){
					IndependantLog.debug(dbg+ "getCssValue('"+property+"') threw "+ x.getClass().getName()+", "+x.getMessage());
				}
				//for a non-exist css-property, SeleniumWebDriver will return "" instead of null
				if(value!=null  && value.isEmpty()){
					IndependantLog.debug(dbg+ "getCssValue('"+property+"') returned empty value. Resetting to null.");
					value = null;
				}
			}
			if (value == null){
				IndependantLog.debug(dbg+ "trying wide getProperties() net.");
				Map<String, Object> props = getProperties(element);
				if(props.containsKey(property)){
					value = props.get(property).toString();
				}else{
					IndependantLog.debug(dbg+ "getProperties() reports no property named '"+ property+"'.");
					String keys = "";
					for(String key:props.keySet()) keys += key +" ";
					IndependantLog.debug(dbg+ "propertyNames: "+ keys);
				}
			}
			if (value == null){
				IndependantLog.debug(dbg+ "trying *NATIVE JS function* to get property '"+ property +"'");
				StringBuffer script = new StringBuffer();
				script.append(JavaScriptFunctions.SAP.sap_getProperty(true, property));
				script.append("return sap_getProperty(arguments[0], arguments[1]);");
				Object result = null;
				try{
					result = WDLibrary.executeJavaScriptOnWebElement(script.toString(), element, property);
					if(result!=null){
						IndependantLog.debug(dbg+" got '"+result+"' by *NATIVE JS function*.");
						value = result.toString();
						if(!(result instanceof String)){
							IndependantLog.warn(dbg+" the result is not String, it is "+result.getClass().getName()+", may need more treatment.");
						}
					}
				}catch(SeleniumPlusException se){
					IndependantLog.error(dbg+ StringUtils.debugmsg(se));
				}
			}
			if(value == null){
				IndependantLog.error(dbg+ "got nothing but (null) for all getProperty attempts using '"+ property +"'");
				throw new SeleniumPlusException(property+" not found.");
			}
			return value;
		} catch(Exception e){
			IndependantLog.error(dbg+ "caught "+ e.getClass().getName()+": "+ e.getMessage());
			throw new SeleniumPlusException(property +" not found.", SeleniumPlusException.CODE_PropertyNotFoundException);
		}
	}

	/**
	 * Retrieve all properties of an element--attributes, css values, property fields and certain property methods.
	 * @param element WebElement, from which to retrieve all properties
	 * @return Map, a set of pair(property, value)
	 * @throws SeleniumPlusException
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getProperties(WebElement element) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);

		Map<String, Object> map = new HashMap<String, Object>();

		WDTimeOut.setImplicitlyWait(100, TimeUnit.MILLISECONDS, 1000);

		IndependantLog.debug(debugmsg+"calling getCssValues...");
		map.putAll(getCssValues(element));
		IndependantLog.debug(debugmsg+"returned from getCssValues...");
		//ADD other attributes by javascript
		StringBuffer jsScript = new StringBuffer();

		try {
			jsScript.append(JavaScriptFunctions.getAttributes());
			jsScript.append("return getAttributes(arguments[0]);\n");
			IndependantLog.debug(debugmsg+"DOM-attributes calling executeJavaScriptOnWebElement...");
			Object attributes = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), element);
			IndependantLog.debug(debugmsg+"receieved DOM-attributes Object from executeJavaScriptOnWebElement...");
			if(attributes instanceof Map){
				IndependantLog.debug(debugmsg+"received attributes Object *IS* instanceof Map.");
				Map<String, Object> attributesMap = (Map<String, Object>) attributes;
				map.putAll(attributesMap);
			}else{
				if(attributes!=null) IndependantLog.debug(debugmsg+"received attributes object '"+attributes.getClass().getName()+"', needs to handle in code.");
				else IndependantLog.debug(debugmsg+"received attributes object is "+null);
			}
		} catch(Exception ignore) {
			IndependantLog.debug(debugmsg+" DOM-attributes, met "+StringUtils.debugmsg(ignore));
		}

		try{
			jsScript = new StringBuffer();
			jsScript.append(JavaScriptFunctions.getHtmlProperties());
			jsScript.append("return getHtmlProperties(arguments[0]);\n");
			IndependantLog.debug(debugmsg+"Htmlproperties calling executeJavaScriptOnWebElement...");
			Object properties = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), element);
			IndependantLog.debug(debugmsg+"received Htmlproperties Object from executeJavaScriptOnWebElement.");
			if(properties instanceof Map){
				IndependantLog.debug(debugmsg+"received properties Object *IS* instanceof Map.");
				try{
					Map<String, ?> propertiesMap = (Map<String, ?>) properties;
					map.putAll(propertiesMap);
				}catch(Throwable t){
					IndependantLog.debug(debugmsg+"returned properties object is "+null);				}
			}else{
				if(properties!=null) IndependantLog.debug("returned properties object '"+properties.getClass().getName()+"', needs to handle in code.");
				else IndependantLog.debug(debugmsg+"returned properties object is "+null);
			}

		} catch(Exception ignore) {
			IndependantLog.debug(debugmsg+"Htmlproperties, met "+StringUtils.debugmsg(ignore));
		}

		WDTimeOut.resetImplicitlyWait(Processor.getSecsWaitForComponent(), TimeUnit.SECONDS);
		return map;
	}

	/**
	 * Attempts to fire (dispatchEvent) a MouseEvent.
	 * The MouseEvent should be suitable and contain all relevant and necessary information for the event.
	 * This is typically used to re-fire a MouseEvent that might have been captured with DocumentClickCapture
	 * but was not allowed to propogate to the actual element.
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
		public String getReadyState(){
			return String.valueOf(js_getGlobalVariable(VARIABLE_READY_STATE));
		}
		/**
		 * Get javascript global variable {@link #VARIABLE_RESPONSE_HEADERS}.
		 */
		public Object getResponseHeaders(){
			return js_getGlobalVariable(VARIABLE_RESPONSE_HEADERS);
		}
		/**
		 * Get javascript global variable {@link #VARIABLE_RESPONSE_TEXT}.
		 */
		public String getResponseText(){
			return String.valueOf(js_getGlobalVariable(VARIABLE_RESPONSE_TEXT));
		}
		/**
		 * Get javascript global variable {@link #VARIABLE_RESPONSE_XML}.
		 */
		public Object getResponseXml(){
			return js_getGlobalVariable(VARIABLE_RESPONSE_XML);
		}
		/**
		 * Get javascript global variable {@link #VARIABLE_STATUS}.
		 */
		public String getHttpStatus(){
			return String.valueOf(js_getGlobalVariable(VARIABLE_STATUS));
		}
		/**
		 * Get javascript global variable {@link #VARIABLE_STATUS_TEXT}.
		 */
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
	 * @param keyword String, the keyword to check for, such like "GetURL"
	 * @throws SeleniumPlusException will be thrown out if a known issue is checked.
	 */
	public static void checkKnownIssue(String keyword) throws SeleniumPlusException{

		if(DDDriverCommands.GETURL_KEYWORD.equalsIgnoreCase(keyword) ||
		   DDDriverCommands.SAVEURLTOFILE_KEYWORD.equalsIgnoreCase(keyword) ||		
		   DDDriverCommands.VERIFYURLCONTENT_KEYWORD.equalsIgnoreCase(keyword) ||		
		   DDDriverCommands.VERIFYURLTOFILE_KEYWORD.equalsIgnoreCase(keyword)){
			//Check the known issue with selenium-standalone2.47.1 and Firefox 42.0
			//These keywords will be skipped for FireFox until we find the reason why 'AJAX execution is stuck with FireFox'.
			if(SelectBrowser.BROWSER_NAME_FIREFOX.equalsIgnoreCase(getBrowserName())
//				&& "42.0".equals(getBrowserVersion())
//				&& "2.47.1".equals(getDriverVersion())
				){
//				throw new SeleniumPlusException("For keyword '"+keyword+"': known issue with selenium-standalone2.47.1 and Firefox ");
				throw new SeleniumPlusException("For keyword '"+keyword+"': execution stuck : known issue with Firefox!");
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
	 * <li> optional[0] autoscroll boolean, if the component will be scrolled into view automatically before clicking.
	 *                                      if not provided, the default value is true.
	 * </ul>
	 * @return boolean true if succeed.
	 * @see #click(WebElement, Point)
	 */
	public static boolean clickUnverified(WebElement component, Point offset, String... optional){
		String debugmsg = StringUtils.debugmsg(false);
		
		try {
			IndependantLog.debug(debugmsg+" click with parameter componet:"+component+", offset:"+offset);
			//Create a combined actions according to the parameters
			Actions actions = new Actions(getWebDriver());

			boolean autoscroll = parseAutoScroll(optional);
			if(autoscroll){
				if(offset!=null) actions.moveToElement(component, offset.x, offset.y);
				else actions.moveToElement(component);
			}
			IndependantLog.debug(debugmsg+" Try Selenium API to click.");
			actions.click().perform();
			
			return true;
		} catch (Exception e) {
			IndependantLog.warn(debugmsg+" Failed with Selenium API, met "+StringUtils.debugmsg(e)+". Try Robot click.");
			
			try {
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
				             SelectBrowser.BROWSER_NAME_IE, 
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
			List<ProcessInfo> killedList = killChromeDriver(host);
			for(ProcessInfo p:killedList){
				System.out.println("on host "+host+", process "+p.getId()+" has been terminated. The return code is "+p.getWmiTerminateRC());
			}
			
			killedList = killIEDriverServer(host);
			for(ProcessInfo p:killedList){
				System.out.println("on host "+host+", process "+p.getId()+" has been terminated. The return code is "+p.getWmiTerminateRC());
			}
			
			host = "tadsrv";
			killedList = killChromeDriver(host);
			for(ProcessInfo p:killedList){
				System.out.println("on host "+host+", process "+p.getId()+" has been terminated. The return code is "+p.getWmiTerminateRC());
			}
						
			killedList = killIEDriverServer(host);
			for(ProcessInfo p:killedList){
				System.out.println("on host "+host+", process "+p.getId()+" has been terminated. The return code is "+p.getWmiTerminateRC());
			}
			
		} catch (SAFSException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * This class represents a Robot, which can do work locally (thru local org.safs.robot.Robot) or remotely (thru a RMI agent).<br>
	 * 
	 * @author sbjlwa
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
		protected static void click(Point location, Keys specialKey, int mouseButtonNumber, int nclicks) throws Exception{
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
			if(rd==null || rd.isLocalServer()){
				if(specialKey==null){
					org.safs.robot.Robot.click(location.x, location.y, mouseButtonNumber, nclicks);
				}else{
					org.safs.robot.Robot.clickWithKeyPress(location.x, location.y, mouseButtonNumber, toJavaKeyCode(specialKey), nclicks);
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
	}

	/**
	 * Before running this method, please read java doc of {@link #test_ajax_call(String)}
	 * @param args
	 */
	public static void main(String[] args){
		test_ajax_call();
		test_kill_extraProcess();
	}
}
