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
 *  <br>  DEC 19, 2013    (DHARMESH) Initial release.
 *  <br>  DEC 25, 2013    (Lei Wang) Add javascript calling DOJO APIs.
 *  <br>  JAN 16, 2014    (DHARMESH) Updated reconnection browser support.
 *  <br>  JAN 16, 2014    (DHARMESH) Added WebDriver() for end user.
 *  <br>  MAR 10, 2014    (Lei Wang) Handle frames for HTML Application, Get browser window information by javascript.
 *  <br>  MAR 27, 2014    (Lei Wang) Add ability to search by XPATH and CSS.
 *  <br>  APR 14, 2014    (Lei Wang) Add ability to listener to a javascript event. Rename some functions.
 *  <br>  AUG 12, 2014    (Lei Wang) Modify method getObject(): return null if no webelement can be found.
 *  <br>  AUG 27, 2014    (Lei Wang) Modify method getObject(): Get element inside a frame if no FrameRS
 *                                      is preceding RS of a child in map file; we consider that the child is
 *                                      in the same frame as parent.
 *  <br>  AUG 29, 2014    (DHARMESH) Add selenium grid host and port support.
 *  <br>  OCT 22, 2014    (Lei Wang) Modify method getObject(): use webdriver to find frame webelement to avoid StaleElementException.
 *  <br>  OCT 29, 2014    (Lei Wang) Add support for qualifier 'ItemIndex', 'Path', 'Property' and 'PropertyContains'.
 *                                  Add support for prefix ':PASM:'.
 *  <br>  NOV 05, 2014    (Lei Wang) Add pageHasChanged(): to detect if the web page url has changed.
 *  <br>  NOV 28, 2014    (Lei Wang) Modify executeScript(): detect javascript error and throw JSException.
 *                                  Add js_xxx(): to manipulate javascript error global variable.
 *  <br>  DEC 05, 2014    (Lei Wang) Modify getObject(): Fix frame window recognition string definition error.
 *  <br>  DEC 11, 2014    (Lei Wang) Modify isSapComponent() etc: fix the domain-detection failure.
 *  <br>  JAN 05, 2015    (Lei Wang) Modify executeScript(): get javascript debug messages and write to debug log.
 *  <br>  JUL 23, 2015    (Lei Wang) Add js_getGlobalVariable(), js_setGlobalVariable()
 *                                  modify js_getGlobalBoolVariable(), js_setGlobalBoolVariable().
 *  <br>  OCT 30, 2015    (Lei Wang)  Move method isVisible(), isDisplayed and isStale() to this class from WDLibrary.
 *                                  Add method highlightThenClear(). Modify highlight(WebElement): check stale.
 *  <br>  NOV 12, 2015    (Lei Wang)  Modify method getObject(): continue to search component on the new page even 'lastFrame' is stale.
 *  <br>  NOV 26, 2015    (Lei Wang)  Modify method getObject(): reset 'lastFrame' to null if exception is thrown out during switch of frame.
 *  <br>  DEC 25, 2015    (Lei Wang)  Modify getObjectByText() and getObjectByTitle(): try to get partial, case-insensitive matched element.
 *  <br>  JAN 05, 2015    (Lei Wang)  Modify getObjectByQualifier() etc.: support one qualifier with "Contains", such as TextContains=, TitleContains=.
 *  <br>  MAR 17, 2016    (Carl Nagle)  Modified to support getObjects returning multiple matches.
 *  <br>  MAY 04, 2016    (Lei Wang)  Modified js_getErrorXXX(): call {@link #js_executeWithTimeout(String, long)} to avoid JavaScriptExecutor locking problem.
 *  <br>  MAY 06, 2016    (Lei Wang)  Modified js_executeWithTimeout(): throw SeleniumPlusException if the timeout is not enough for javascript execution.
 *                                  Modified js_getErrorCode(): enlarge the timeout to 5000 milliseconds.
 *  <br>  FEB 27, 2017    (Lei Wang)  Added setJsTimeout(): It is used to set timeout for javascript execution.
 *                                  Modified js_executeWithTimeout(): used local variable instead of global static variable.
 *  <br>  MAY 03, 2017    (Lei Wang)  Added methods switchWindow().
 *                                  Added the ability to delay javascript execution. NOT exposed yet.
 *                                  Modified getAllWindowTitles(): return titles for all opened window for all WebDrivers.
 *  <br>  MAY 22, 2017    (Lei Wang)  Added isValidBrowserID().
 *                                  Modified removeWebDriver(): call isValidBrowserID() to check the validity of browser ID.
 *  <br>  JUN 28, 2017    (Lei Wang)  Modified getObjectByQualifier(): support deprecated CSS function contains() to keep compatible with Selenium IDE's script.
 *  <br>  NOV 10, 2017    (Lei Wang)  Modified method SearchObject.js_executeWithTimeout() to catch NoSuchSessionException.
 *  <br>  DEC 19, 2017    (Lei Wang)  Added method getCurrentFrame() and getCurrentFrameName().
 *  <br>  DEC 19, 2017    (Lei Wang)  Modified getFrameWebElement(): it takes a very long time to find a frame with tag.
 *                                                                 It seems that tag 'iframe' is more often used then 'frame', so try 'iframe' first to save time.
 *                                  Modified getWebElement(): Use xpath "(E)[n]" to get the nth E element: while "E[n]" represents the nth child of element E, should not use it.
 *  <br>  JUL 18, 2018    (Lei Wang)  Added getPreMatchedObjectsByText(): search element by both "text()=xxx" and "@value=xxx". Use getText(webelement) instead of webelement.getText().
 *                                  Modified getObjectByText() and getObjectsByText() to call getPreMatchedObjectsByText().
 *  <br>  AUG 01, 2018    (Lei Wang)  Added back() and forward().
 *  <br>  AUG 29, 2018    (Lei Wang)  Modified getText(): if we can get the text by webelement.getText(),
 *                                                       we don't waste more time to call getValue(), which usually takes more time to process.
 *                                   Moved some code from getPreMatchedObjectsByText() to getPreMatchedObjectsByAttributeValue(): move the part of searching by element's attribute 'value'.
 *                                   Modified getObjectByText(): firstly try to match with elements returned from getPreMatchedObjectsByText(), if not found then  match with elements returned from getPreMatchedObjectsByAttributeValue().
 *  <br>  DEC 06, 2018    (Lei Wang)  Added method generateSAFSFrameRecognition().
 *  <br>  JAN 10, 2019    (Lei Wang)  Modified method switchFrameAsLastFrame(): create FrameElement before really switching the the new frame to avoid the StaleElementReferenceException.
 *  <br>  JAN 10, 2019    (Carl Nagle)   Support switching frame by bare frame name or id and id= and name= frameRS prefixes.
 *  <br>  JAN 11, 2019    (Lei Wang)  Modified method _switchFrames(): If we bypass the 'frame reset', then user will handle the frame-switch by himself.
 *  <br>  MAR 19, 2019    (Lei Wang)  Modified method containFrameRS(),isValidFrameRS(): check also 'IFRAMEID'.
 *  <br>  MAR 21, 2019    (Lei Wang)  Added checkWindowRS(): Reset the 'lastFrame' to null if the window's RS doesn't contain frame information.
 *  <br>  JUL 04, 2019    (Lei Wang)  Modified _switchFrames(): move some code to method initializeBrowserWindow().
 *                                   Modified getLastBrowserWindow(): if the lastBrowserWindow is null, then call initializeBrowserWindow().
 *  <br>  SEP 06, 2019    (Lei Wang)  Fixed error of method getCurrentFrame(): webdriver cannot get the frame webelement in itself frame context.
 *                                   Added methods switchToParentFrame(), getCurrentFrameID().
 *                                   Moved frames related methods from AISearchBase to here.
 *  <br>  SEP 11, 2019    (Lei Wang)  Added _getCurrentFrame(), modified _switchFrames(): print out the current frame before/after switching frame.
 *                                   Modified switchToFrame(): change the field 'lastFrame' at the same time.
 *  <br>  SEP 12, 2019    (Lei Wang)  Modified _switchFrames(): if the 'bypass frame reset' is true, we ignore the 'frame settings' in the RS.
 *  <br>  SEP 18, 2019    (Lei Wang)  Modified initializeBrowserWindow(): if we are not in the top most document, we will not modify the field 'lastBrowserWindow'.
 *                                   Modified switchToFrame(): call getLastBrowserWindow() so that the 'lastBrowserWindow' will be initialized.
 *  <br>  SEP 19, 2019    (Lei Wang)  Moved getProperty(), getProperties() and getCssValues() from WDLibrary to here.
 *                                   Modified FrameElement: add a map holding frame's properties.
 *                                   Added field needFreshWindow to indicate if we need the latest window's information.
 *                                   Modified generateFrameTree(): generate the frame tree to hold FrameElement as tree node's content.
 *  <br>  SEP 25, 2019    (Lei Wang)  Added resetLastFrame()
 */
package org.safs.selenium.webdriver.lib;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.safs.Arbre;
import org.safs.Constants.HTMLConst;
import org.safs.GuiObjectRecognition;
import org.safs.GuiObjectVector;
import org.safs.IndependantLog;
import org.safs.Processor;
import org.safs.SAFSException;
import org.safs.SAFSObjectNotFoundException;
import org.safs.StringUtils;
import org.safs.net.NetUtilities;
import org.safs.selenium.util.JSException;
import org.safs.selenium.util.JavaScriptFunctions;
import org.safs.selenium.webdriver.SeleniumPlus.WDTimeOut;
import org.safs.selenium.webdriver.WebDriverGUIUtilities;
import org.safs.selenium.webdriver.lib.RS.CSS;
import org.safs.selenium.webdriver.lib.RS.XPATH;
import org.safs.selenium.webdriver.lib.RemoteDriver.SessionInfo;
import org.safs.selenium.webdriver.lib.model.Accessibility;
import org.safs.selenium.webdriver.lib.model.Element;
import org.safs.selenium.webdriver.lib.model.TextMatchingCriterion;
import org.safs.tools.drivers.DriverConstant.SeleniumConfigConstant;
import org.safs.tools.stringutils.StringUtilities;

/**
 * Primary class used to find WebElements with Selenium WebDriver.
 * <p>
 * Supports various recognition string syntax, to include:
 * <p>
 * <ul>Identify the proper FRAME, if present:
 *     <p><ul><pre>
 *     FRAMEINDEX=N;\; (1-based, 1 means the first frame)
 *     FRAMEID=id;\;
 *     FRAMENAME=name;\;
 *     FRAMEXPATH=//iframe[@id='frameId'];\;
 *
 *     (note: parent/child elements separated by ";\;" )
 *     (note: FRAMEINDEX is NOT ready for using )
 *     (note: If there is no frame-expression in a RS, the last visited frame
 *            will be the frame where to find component. By default, the last visited
 *            frame is the main frame (the topmost html Document).
 *            And that is why if we specify frame for a window, then their children
 *            doesn't need to specify the frame information anymore. For example:
 *            [HelpPopup]
 *            HelpPopup="FRAMEID=VisualAnalyticsHubLogon_iframe;\;id=__popover8-popover"
 *            BtnHelpCenter="id=__item23"
 *
 *            BtnHelpCenter is supposed to be found in frame 'VisualAnalyticsHubLogon_iframe'
 *      )
 *     </pre></ul>
 * <p>Identify any supported "TYPE" qualifier, if desired:
 *     <p><ul><pre>
 *     TYPE=DOJOTabControl;
 *     TYPE=DOJOToolbar;
 *     TYPE=SAPTabControl;
 *     TYPE=SAPToolbar;
 *     (see [doclink here] for supported domain classes)
 *
 *     (note: type qualifier also includes domain information, like "DOJO", "SAP")
 *     (note: type qualifier and attribute identifiers separated by semi-colons)
 *     </pre></ul>
 *<p>Identify unique element attribute identifiers like:
 *     <p><ul><pre>
 *     ID=id;
 *     CLASS=class;
 *     NAME=name;
 *     TEXT=text;
 *     TITLE=title;
 *     LINK=linkInfo;
 *     PARTIALLINK=partialLinkInfo;
 *     TAG=tagname;
 *
 *     (note: multiple attribute identifiers separated by semi-colons ;)
 *     </pre></ul>
 *<p>Identify element by multiple native attributes:
 *     <p><ul><pre>
 *     property1=xxx;property2=yyy;property3=zzz;
 *
 *     </pre></ul>
 *<p>Identify explicit XPATH or CSS recognition:
 *     <p><ul><pre>
 *     XPATH=explicit xpath
 *     CSS=explicit css path
 *     </pre></ul>
 * <p>Identify any supported qualifier, if desired:
 *     <p><ul><pre>
 *     INDEX=n;                             (1-based, NOT used alone, indicates the Nth matched)
 *     ITEMINDEX=n;                         (1-based, NOT used alone, indicates the Nth item in a List, or ComboBox etc.)
 *     PATH=parent->child->grandchild;      (NOT used alone, indicates the sub-item in a List, ComboBox, Menu or Tree etc.)
 *     PROPERTY=prop:value;                 (can be used with others to enforce search condition)
 *     PROPERTYCONTAINS=prop:partialValue;  (can be used with others to enforce search condition)
 *
 *     (note: multiple qualifiers separated by semi-colons ;)
 *     (note: these "ID", "TEXT", "ITEMINDEX", "PATH" will normally be considered as qualifier, but if there is a
 *            prefix :PASM: infront, they will be considered as component's native properties too.
 *            Example:
 *            For "TEXT=myList;componentProperty=xxx;ITEMINDEX=n;"
 *            "TEXT" and "ITEMINDEX" are qualifiers, while "componentProperty" is native property
 *            For ":PASM:TEXT=myList;componentProperty=xxx;ITEMINDEX=n;"
 *            "TEXT", "ITEMINDEX" and "componentProperty" are all native properties
 *      )
 *     </pre></ul>
 * </ul>
 * <p>
 */
public class SearchObject {

	private static Hashtable<String, WebDriver> webDrivers = new Hashtable<String, WebDriver>();
	private static Vector<String> webDriverStack = new Vector<String>();

	/** "value","text","placeholder". An array of attribute used as component's text content.<br>
	 * @see #getValue(WebElement, String...)
	 */
	public static final String[] TEXT_VALUE_ATTRIBUTES = {"value","text","placeholder"};

	/**
	 * The Selenium 'WebDriver' currently used to manipulate the browser.
	 */
	protected static WebDriver lastUsedWD;
	/**
	 * The Selenium 'JavascriptExecutor' currently used to execute javascript on the browser.
	 */
	protected static JavascriptExecutor lastJS;

	/**
	 * If we need to check the current frame context matching the {@link #lastFrame}. The default value is false.
	 */
	public static boolean checkCurrentFrameContext = false;

	/**
	 * By default, search algorithms begin every search by switching back ("frames reset") to the topmost root HTML document (frame)
	 * and switch frame automatically according to the 'recognition string' during the component search.<br>
	 * Set bypassFramesReset to true if you wish to disable this "frames reset" and focus on the last frame context for component search.<br>
	 */
	public static void setBypassFramesReset(boolean bypass) {
		System.setProperty(SeleniumConfigConstant.PROPERTY_BYPASS_FRAME_RESET, Boolean.toString(bypass));
		if(bypass){
			IndependantLog.debug("User has set the '"+SeleniumConfigConstant.PROPERTY_BYPASS_FRAME_RESET+"' to true, "
					+ "selenium will not switch back to the topmost frame, it will search on the last switched Frame.");
		}
	}
	/**
	 * By default, search algorithms begin every search by switching back ("frames reset") to the topmost root HTML document (frame).<br>
	 * and switch frame automatically according to the 'recognition string' during the component search.<br>
	 * This 'bypassFramesReset' decides if this "frames reset" will be attempted at the beginning of every component search.<br>
	 * If it is true, that means that we don't do "frames reset" and focus on the last frame context for component search.<br>
	 * @return boolean, the current bypassFramesReset value.
	 */
	public static boolean getBypassFramesReset() {
		return Boolean.parseBoolean(System.getProperty(SeleniumConfigConstant.PROPERTY_BYPASS_FRAME_RESET));
	}

	/**
	 * By default, our library will handle click actions by Robot firstly, if failed then handle it by selenium.
	 * Set bypassRobot to true if you wish to skip the Robot and let the selenium handle the click directly.
	 */
	public static void setBypassRobot(boolean bypass) {
		System.setProperty(SeleniumConfigConstant.PROPERTY_BYPASS_ROBOT_ACTION, Boolean.toString(bypass));
		if(bypass){
			IndependantLog.debug("User has set the '"+SeleniumConfigConstant.PROPERTY_BYPASS_ROBOT_ACTION+"' to true, "
					+ "our library will handle click ations directly by selenium, they will not be attempted by Robot.");
		}
	}
	/**
	 * By default, our library will handle click actions by Robot firstly, if failed then handle it by selenium.<br>
	 * @return the current true/false setting the bypassRobot value. It decides if the click actions will be attempted by Robot or not.<br>
	 */
	public static boolean getBypassRobot() {
		return Boolean.parseBoolean(System.getProperty(SeleniumConfigConstant.PROPERTY_BYPASS_ROBOT_ACTION));
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
					IndependantLog.debug(debugmsg+"returned properties object is "+null);
				}
			}else{
				if(properties!=null) IndependantLog.debug("returned properties object '"+properties.getClass().getName()+"', needs to handle in code.");
				else IndependantLog.debug(debugmsg+"returned properties object is "+null);
			}

		} catch(Exception ignore) {
			IndependantLog.debug(debugmsg+"Htmlproperties, met "+StringUtils.debugmsg(ignore));
		}

		try{
			IndependantLog.debug(debugmsg+"SAP getAccessibilityControllerElementsInfo calling executeJavaScriptOnWebElement...");
			List<Accessibility> accessibilities = SearchObject.SAP.getSAPAccessibleElementsInfo(element);
			IndependantLog.debug(debugmsg+"received SAP AccessibilityElementsInfo from executeJavaScriptOnWebElement.");
			String keyfix = null;
			String putkey = null;
			Accessibility accessibility = null;
			for(int i=0;i<accessibilities.size();i++){
				keyfix = "sap_AccessibilityElement["+i+"].";
				try{
					accessibility = accessibilities.get(i);
					Map<?, ?> propertiesMap = accessibility.getMap();
					for(Object key:propertiesMap.keySet()){
						putkey = keyfix + key;
						map.put(putkey, propertiesMap.get(key));
					}
				}catch(Throwable t){
					IndependantLog.debug(debugmsg+"returned AccessibilityElement object is "+null);
				}
			}
		} catch(Exception ignore) {
			IndependantLog.debug(debugmsg+"sap_getAccessibilityControllerElementsInfo, met "+StringUtils.debugmsg(ignore));
		}

		WDTimeOut.resetImplicitlyWait(Processor.getSecsWaitForComponent(), TimeUnit.SECONDS);
		return map;
	}

	/**
	 * The frame object where the current GUI-component locates. If there is no frame, its value should be null.<br>
	 * It is important for getting correct screen location for a WebElement.<br>
	 * @see #_switchFrames(String)
	 */
	protected static FrameElement lastFrame = null;
	public static class FrameElement{
		private FrameElement parentFrame;
		/**
		 * If you want to operate on this webelement, you MUST switch to its parent's frame
		 * <pre>
		 * {@code
		 *  WDLibrary.getWebDriver().switchTo().parentFrame();
		 * }
		 * </pre>
		 */
		private WebElement webElement = null;
		private Dimension size = null;
		private Point location = null;
		private Map<String, String> properties = new HashMap<String, String>();

		public FrameElement(FrameElement parentFrame, WebElement frame){
			try{
				this.webElement = frame;
				if(parentFrame!=null){
					this.parentFrame = parentFrame;
					location = frame.getLocation().moveBy(parentFrame.getLocation().x, parentFrame.getLocation().y);
				}else{
					location = frame.getLocation();
				}
				size = frame.getSize();

				properties.put(HTMLConst.ATTRIBUTE_ID, SearchObject.getProperty(frame, HTMLConst.ATTRIBUTE_ID));
				properties.put(HTMLConst.ATTRIBUTE_NAME, SearchObject.getProperty(frame, HTMLConst.ATTRIBUTE_NAME));

			}catch(Exception e){
				IndependantLog.warn("Failed to initialize properties, due to "+StringUtils.debugmsg(e));
			}
		}

		public Dimension getSize(){ return size;}
		public Point getLocation(){ return location;}
		public FrameElement getParentFrame(){ return parentFrame; }
		public WebElement getWebElement(){ return webElement; }
		public String getProperty(String attribute){
			return properties.get(attribute);
		}

		@Override
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append("Frame: ");
			String temp = properties.get(HTMLConst.ATTRIBUTE_ID);
			if(StringUtils.isValid(temp)) sb.append(" ID="+temp+",");
			temp = properties.get(HTMLConst.ATTRIBUTE_NAME);
			if(StringUtils.isValid(temp)) sb.append(" Name="+temp+",");
			if(size!=null) sb.append(" Dimension="+size+",");
			if(location!=null) sb.append(" At location="+location);
			return sb.toString();
		}
	}

	/**
	 * The frame object where the current GUI-component locate. If there is no frame, its value should be null.
	 * @see #getObject(SearchContext, String)
	 */
	public static FrameElement getLastFrame() {
		return lastFrame;
	}
	/**
	 * <b style="color:red">Note: User should be very careful when calling this method,
	 * it will change the whole searching context!!!</b><br>
	 * Set the frame object where the current GUI-component locate.
	 * If a null value is set, then there is no frame to switch and
	 * object will be searched in default document (webdriver.switchTo().defaultContent()).
	 * @see #getObject(SearchContext, String)
	 */
	public static void setLastFrame(FrameElement lastFrame) {
		SearchObject.lastFrame = lastFrame;
	}

	/**
	 * If the window's recognition string doesn't contain any information about frame,
	 * we suppose the children locate in top document (no frame), and we should reset the 'lastFrame' to null.
	 *
	 * @param winrs String, the Window's recognition string.
	 * @return boolean, true if {@link #lastFrame} is reset to null.
	 */
	public static boolean checkWindowRS(String winrs){
        if(!containFrameRS(winrs) && !getBypassFramesReset() /*if bypass frame-reset, then 'lastFrame' will not be used.*/){
        	IndependantLog.info("SearchObject: winrs '"+ winrs+"' doesn't contain any information about frame, we reset the 'lastFrame' to null.");
        	lastFrame = null;
        	return true;
        }
        return false;
	}

	/**FRAMEINDEX, it should be placed in front of normal tokens like ID, CLASS, NAME etc. if exist.
	 *  NOT recommended to use. Use FRAMEID OR FRAMENAME Instead.*/
	public static final String SEARCH_CRITERIA_FRAMEINDEX 	= "FRAMEINDEX";
	/**FRAMEID, it should be placed in front of normal tokens like ID, CLASS, NAME etc. if exist.*/
	public static final String SEARCH_CRITERIA_FRAMEID 		= "FRAMEID";
	/**IFRAMEID, it should be placed in front of normal tokens like ID, CLASS, NAME etc. if exist.*/
	public static final String SEARCH_CRITERIA_IFRAMEID 		= "IFRAMEID";
	/**FRAMENAME, it should be placed in front of normal tokens like ID, CLASS, NAME etc. if exist.*/
	public static final String SEARCH_CRITERIA_FRAMENAME 	= "FRAMENAME";
	/**FRAMEXPATH, it should be placed in front of normal tokens like ID, CLASS, NAME etc. if exist.*/
	public static final String SEARCH_CRITERIA_FRAMEXPATH 	= "FRAMEXPATH";
	/**'frame' tag name*/
	public static final String TAG_FRAME 					= "frame";
	/**'iframe' tag name*/
	public static final String TAG_IFRAME 					= "iframe";

	/**'null' means the top most document, in no frame */
	public static final String TOP_DOCUMENT_ID 		= "null";

	//	/**DOMAIN*/
	//	public static final String SEARCH_CRITERIA_DOMAIN					= GuiObjectRecognition.CATEGORY_DOMAIN.toUpperCase();
	/**TYPE*/
	public static final String SEARCH_CRITERIA_TYPE						= GuiObjectRecognition.CATEGORY_TYPE.toUpperCase();
	/**ID*/
	public static final String SEARCH_CRITERIA_ID 						= GuiObjectRecognition.QUALIFIER_ID.toUpperCase();
	/**NAME*/
	public static final String SEARCH_CRITERIA_NAME 					= GuiObjectRecognition.QUALIFIER_NAME.toUpperCase();
	/**TEXT*/
	public static final String SEARCH_CRITERIA_TEXT 					= GuiObjectRecognition.QUALIFIER_TEXT.toUpperCase();
	/**PATH*/
	public static final String SEARCH_CRITERIA_PATH     				= GuiObjectRecognition.QUALIFIER_PATH.toUpperCase();
	/**PROPERTY*/
	public static final String SEARCH_CRITERIA_PROPERTY    				= GuiObjectRecognition.QUALIFIER_PROPERTY.toUpperCase();
	/**PROPERTYCONTAINS*/
	public static final String SEARCH_CRITERIA_PROPERTY_CONTAINS     	= GuiObjectRecognition.QUALIFIER_PROPERTY_CONTAINS.toUpperCase();
	/**INDEX*/
	public static final String SEARCH_CRITERIA_INDEX	    			= GuiObjectRecognition.QUALIFIER_INDEX.toUpperCase();
	/**CLASSCONTAINS*/
	public static final String SEARCH_CRITERIA_CLASS_CONTAINS     	    = GuiObjectRecognition.QUALIFIER_CLASS_CONTAINS.toUpperCase();
	/**ITEMINDEX*/
	public static final String SEARCH_CRITERIA_ITEMINDEX    = "ITEMINDEX";
	/**CLASS*/
	public static final String SEARCH_CRITERIA_CLASS 		= "CLASS";
	/**TITLE*/
	public static final String SEARCH_CRITERIA_TITLE 		= "TITLE";
	/**XPATH*/
	public static final String SEARCH_CRITERIA_XPATH 		= "XPATH";
	/**CSS*/
	public static final String SEARCH_CRITERIA_CSS	 		= "CSS";
	/**LINK*/
	public static final String SEARCH_CRITERIA_LINK	 		= "LINK";
	/**PARTIALLINK*/
	public static final String SEARCH_CRITERIA_PARTIALLINK	= "PARTIALLINK";
	/**TAG*/
	public static final String SEARCH_CRITERIA_TAG	 		= "TAG";

	/** "CONTAINS" */
	public static final String SEARCH_CRITERIA_CONTAINS_SUFFIX	 		= "CONTAINS";

	/**DOJO*/
	public static final String DOMAIN_DOJO         = "DOJO";
	public static final String DOMAIN_SAP          = "SAP";
	/**HTML*/
	public static final String DOMAIN_HTML         = "HTML";

	public static final int INVALID_INDEX = TextMatchingCriterion.INVALID_INDEX;

	/** separates parent/child relationships in recognition strings. (Default: "/" ) */
	public static final String XPATH_CHILD_SEPARATOR  	= "/";

	/** separates parent/child relationships in recognition strings. (Default: ";\;" ) */
	public static String childSeparator  	= GuiObjectVector.DEFAULT_CHILD_SEPARATOR;
	/** separates multiples qualifiers. (Default: ";" )*/
	public static String qulifierSeparator 	= GuiObjectRecognition.DEFAULT_QUALIFIER_SEPARATOR;
	/** separates type and value for one qualifier. (Default: "=" )*/
	public static String assignSeparator    = GuiObjectRecognition.DEFAULT_ASSIGN_SEPARATOR;
	/** separates property-name and property-value. (Default: ":" )*/
	public static String propertySeparator    = GuiObjectRecognition.DEFAULT_PROPERTY_QUALIFIER_SEPARATOR;
	/** the character to escape separators. (Default: '\\' ) */
	public static Character escapeChar			= StringUtils.REGEX_ESCAPE_CHARACTER;

	/** non-ui tags to be ignored in xpath searches and xpath generation.
	 * Like: br, head, title, meta, link, script, wbr */
	public static final String[] IGNORED_NONUI_TAGS = new String[]{
		"head", "title", "meta", "link", "script", "br", "wbr"
	};

	/**The last visited URL,*/
	protected static String lastVisitedURL = null;
	public static String getLastVisitedURL(){ return lastVisitedURL;}

	/**
	 * Indicate if we need the latest information of browser's window/frame.<br>
	 * If user moves/resizes the browser during the test, then the window location/size will change too.
	 * We need to call {@link #initializeBrowserWindow()} to get the latest window's location again.
	 * At this situation, we need to set this field to true, but this will affect the performance.<br>
	 * If we care about the performance, we should set this field to false.<br>
	 */
	private static boolean needFreshWindow = false;
	/**The last visited browser window, it is important for getting correct screen location for a WebElement. */
	protected static BrowserWindow lastBrowserWindow = null;
	/**
	 * This method will initialize the field {@link #lastBrowserWindow}.<br>
	 * <b>NOTE: Please call getWebDriver().switchTo().defaultContent() if we are not in the topmost document.</b>
	 * <b>NOTE: Please call setNeedFreshWindow(true) if we want the latest window's location/size information.</b>
	 */
	public static synchronized BrowserWindow getLastBrowserWindow(){
		try {
			if(needFreshWindow || lastBrowserWindow==null) initializeBrowserWindow();
		} catch (SeleniumPlusException e) {
			IndependantLog.error("Failed to initializeBrowserWindow, Met "+e.toString());
		}

		return lastBrowserWindow;
	}

	/**
	 * Get the value of field {@link #needFreshWindow}.
	 * @return boolean {@link #needFreshWindow}
	 */
	public static boolean isNeedFreshWindow() {
		return needFreshWindow;
	}
	/**
	 * Set the value for filed {@link #needFreshWindow}.
	 * @param needFreshWindow boolean
	 */
	public static void setNeedFreshWindow(boolean needFreshWindow) {
		SearchObject.needFreshWindow = needFreshWindow;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static class BrowserWindow{
		/**'height'*/
		public static final String PROPERTY_HEIGHT = "height";
		/**'width'*/
		public static final String PROPERTY_WIDTH = "width";
		/**'x'*/
		public static final String PROPERTY_LOCATION_X = "x";//relative to screen
		/**'y'*/
		public static final String PROPERTY_LOCATION_Y = "y";//relative to screen
		/**'clientHeight'*/
		public static final String PROPERTY_CLIENT_HEIGHT = "clientHeight";//The height between 'header(menubar, tabbar, toolbar)' and 'status bar'
		/**'clientWidth'*/
		public static final String PROPERTY_CLIENT_WIDTH = "clientWidth";//
		/**'clientX'*/
		public static final String PROPERTY_CLIENT_LOCATION_X = "clientX";//relative to window
		/**'clientY'*/
		public static final String PROPERTY_CLIENT_LOCATION_Y = "clientY";//relative to window
		/**'pageXOffset'*/
		public static final String PROPERTY_PAGE_X_OFFSET = "pageXOffset";//Scroll bar x offset
		/**'pageYOffset'*/
		public static final String PROPERTY_PAGE_Y_OFFSET = "pageYOffset";//Scroll bar y offset
		/**'borderWidth'*/
		public static final String PROPERTY_BORDER_WIDTH = "borderWidth";//The width of the browser's border
		/**'headerHeight'*/
		public static final String PROPERTY_HEADER_HEIGHT = "headerHeight";//The height of header(menubar, tabbar, toolbar)
		/**'maximized'*/
		public static final String PROPERTY_MAXIMIZED = "maximized";//If the browser is maximized
		/**'DriverWindow'*/
		public static final String PROPERTY_DRIVER_WINDOW = "DriverWindow";//The WebDriver manage()ed Window object, if present.

		/* the wrapped object, represents a browser window object.
		 * This object SHOULD be a Map returned by a javascript function*/
		@SuppressWarnings("unused")
		private Object object = null;
		/* convenient reference to wrapped object*/
		private Map map = null;

		private Window window = null;

		private float height;
		private float width;
		private float x;
		private float y;
		private float clientHeight;
		private float clientWidth;
		private float clientX;
		private float clientY;
		private float pageXOffset;
		private float pageYOffset;
		private float borderWidth;
		private float headerHeight;
		private boolean maximized;

		public void set(Map browserInfoMap){
			map = browserInfoMap;
			Object rc = null;
			IndependantLog.info("BrowserWindow set map processing: "+ browserInfoMap.getClass().getName());

			try{ window = (Window) map.get(PROPERTY_DRIVER_WINDOW);}catch(Exception ex){
				IndependantLog.info("BrowserWindow set map ignoring set Window: "+ ex.getClass().getName()+": "+ ex.getMessage());
			}
			try{ height = StringUtilities.getFloat(map, PROPERTY_HEIGHT); } catch (Exception ex) {
				IndependantLog.info("BrowserWindow set: height not set in Map...attempting fix.");
				if(window != null) {
					height = window.getSize().height;
					map.put(PROPERTY_HEIGHT, new Float(height));
					IndependantLog.info("BrowserWindow set: height set to "+height);
				}else{
  				    IndependantLog.info("BrowserWindow set: height was NOT fixed.");
				}
				//consider document.parentWindow.screen.height
			}
			try{ width = StringUtilities.getFloat(map, PROPERTY_WIDTH); } catch (Exception ex) {
				IndependantLog.info("BrowserWindow set: width not set in Map...attempting fix.");
				if(window != null) {
					width = window.getSize().width;
					map.put(PROPERTY_WIDTH, new Float(width));
					IndependantLog.info("BrowserWindow set: width set to "+width);
				}else{
  				    IndependantLog.info("BrowserWindow set: width was NOT fixed.");
				}
				//consider document.parentWindow.screen.width
			}
			try{ x = StringUtilities.getFloat(map, PROPERTY_LOCATION_X); } catch (Exception ex) {
				IndependantLog.info("BrowserWindow set: x not set in Map...attempting fix.");
				if(window != null) {
					x = window.getPosition().x;
					map.put(PROPERTY_LOCATION_X, new Float(x));
					IndependantLog.info("BrowserWindow set: x set to "+x);
				}else{
  				    IndependantLog.info("BrowserWindow set: x was NOT fixed.");
				}
				// consider document.parentWindow.screenLeft
			}
			try{ y = StringUtilities.getFloat(map, PROPERTY_LOCATION_Y); } catch (Exception ex) {
				IndependantLog.info("BrowserWindow set: y not set in Map...attempting fix.");
				if(window != null) {
					y = window.getPosition().y;
					map.put(PROPERTY_LOCATION_Y, new Float(y));
					IndependantLog.info("BrowserWindow set: y set to "+y);
				}else{
  				    IndependantLog.info("BrowserWindow set: y was NOT fixed.");
				}
			}
			try{ clientX = StringUtilities.getFloat(map, PROPERTY_CLIENT_LOCATION_X); } catch (Exception ex) {
			    IndependantLog.info("BrowserWindow set: clientX not set in Map...attempting fix.");
				// consider document.parentWindow.screenLeft
				try{ rc=null;
				    rc = WDLibrary.executeScript("return document.parentWindow.screenLeft;");
				    if(rc instanceof Number) {
				    	clientX = x - ((Number)rc).floatValue();
						map.put(PROPERTY_CLIENT_LOCATION_X, new Float(clientX));
						IndependantLog.info("BrowserWindow set: clientX set to "+clientX);
				    }else{
	  				    IndependantLog.info("BrowserWindow set: clientX was NOT fixed.");
				    }
				}
				catch(Exception ex2){
					IndependantLog.info("BrowserWindow setMap clientX ignoring "+ex2.getClass().getName()+", "+ ex2.getMessage());
					// DOJO alternatives?
				}
			}
			try{ clientY = StringUtilities.getFloat(map, PROPERTY_CLIENT_LOCATION_Y); } catch (Exception ex) {
			    IndependantLog.info("BrowserWindow set: clientY not set in Map...attempting fix...");
				// consider document.parentWindow.screenTop
				try{ rc=null;
				    rc = WDLibrary.executeScript("return document.parentWindow.screenTop;");
				    if(rc instanceof Number) {
				    	clientY = y - ((Number)rc).floatValue();
						map.put(PROPERTY_CLIENT_LOCATION_Y, new Float(clientY));
						IndependantLog.info("BrowserWindow set: clientY set to "+clientY);
				    }else{
	  				    IndependantLog.info("BrowserWindow set: clientY was NOT fixed.");
				    }
				}
				catch(Exception ex2){
					IndependantLog.info("BrowserWindow setMap clientY ignoring "+ex2.getClass().getName()+", "+ ex2.getMessage());
					// DOJO alternatives?
				}
			}
			try{ borderWidth = StringUtilities.getFloat(map, PROPERTY_BORDER_WIDTH); } catch (Exception ex) {
				IndependantLog.info("BrowserWindow set: borderWidth not set in Map...attempting fix.");
				// try to get it from CSS settings?
				// clientX - x ?
				// height - clientHeight - headerHeight ?
				borderWidth = clientX - x;
				map.put(PROPERTY_BORDER_WIDTH, new Float(borderWidth));
				IndependantLog.info("BrowserWindow set: borderWidth set to "+borderWidth);
			}
			if(borderWidth < 0){
				IndependantLog.info("BrowserWindow set: invalid borderWidth set in Map...attempting fix...");
				borderWidth = 0;
				map.put(PROPERTY_BORDER_WIDTH, new Float(borderWidth));
				IndependantLog.info("BrowserWindow set: borderWidth nomralized to "+ borderWidth);
				// try to get it from CSS settings?
				// height - clientHeight - headerHeight
			}
			try{ headerHeight = StringUtilities.getFloat(map, PROPERTY_HEADER_HEIGHT); } catch (Exception ex) {
				IndependantLog.info("BrowserWindow set: headerHeight not set in Map...attempting fix...");
				headerHeight = clientY - y;
				// height - clientHeight - borderWidth ?
				map.put(PROPERTY_HEADER_HEIGHT, new Float(headerHeight));
				IndependantLog.info("BrowserWindow set: headerHeight to "+headerHeight);
			}
			try{ clientWidth = StringUtilities.getFloat(map, PROPERTY_CLIENT_WIDTH); } catch (Exception ex) {
				IndependantLog.info("BrowserWindow set: clientWidth not set in Map...attempting fix...");
				// width - (borderWidth*2) ?
				clientWidth = width - (borderWidth * 2);
				map.put(PROPERTY_CLIENT_WIDTH, new Float(clientWidth));
				IndependantLog.info("BrowserWindow set: clientWidth set to "+ clientWidth);
			}
			try{ clientHeight = StringUtilities.getFloat(map, PROPERTY_CLIENT_HEIGHT); } catch (Exception ex) {
				IndependantLog.info("BrowserWindow set: clientHeight not set in Map...attempting fix.");
				// height - headerHeight - borderWidth ?
				clientHeight = height - headerHeight - borderWidth;  //can be wrong if bottom border != side borders
				map.put(PROPERTY_CLIENT_HEIGHT, new Float(clientHeight));
				IndependantLog.info("BrowserWindow set: clientHeight set to "+ clientHeight);
			}
			try{ pageXOffset = StringUtilities.getFloat(map, PROPERTY_PAGE_X_OFFSET); } catch (SAFSException ex) {	}
			try{ pageYOffset = StringUtilities.getFloat(map, PROPERTY_PAGE_Y_OFFSET); } catch (SAFSException ex) {	}
			try{ maximized = StringUtilities.getBoolean(map, PROPERTY_MAXIMIZED); } catch (SAFSException ex) {

			}
		}

		/**
		 * The embedded Mapped object does or does NOT contain a real value for a particular Property?
		 * @param propertyName
		 * @return true or false;
		 */
		protected boolean hasProperty(String propertyName){
			if(map == null || map.isEmpty()) return false;
			if (map.containsKey(propertyName)){
				return (map.get(propertyName)!= null);
			}
			return false;
		}

		public BrowserWindow(){}

		/**
		 * @param object Object containing information about the browser window.
		 * @see #getWindowObjectByJS(Window)
		 */
		public BrowserWindow(Object object) throws SeleniumPlusException{
			String debugmsg = StringUtils.debugmsg(BrowserWindow.class, "BrowserWindow");
			this.object = object;

			if(object instanceof Map){
				map = (Map<?,?>) object;
				set(map);

			}else if(object!=null){
				String errmsg = " DOM window Object is NOT the expected Map type, it is '"+object.getClass().getName()+"'";
				IndependantLog.error(debugmsg+errmsg);
				throw new SeleniumPlusException(errmsg);
			}else{
				String errmsg = " The emebedded DOM window Object is null, cannot handle it.";
				IndependantLog.error(debugmsg+errmsg);
				throw new SeleniumPlusException(errmsg);
			}
		}

		public float getHeight() { return height; }
		public float getWidth() { return width; }
		public float getX() { return x; }
		public float getY() { return y; }
		public float getClientHeight() { return clientHeight; }
		public float getClientWidth() { return clientWidth; }
		public float getClientX() { return clientX; }
		public float getClientY() { return clientY; }
		public float getPageXOffset() { return pageXOffset; }
		public float getPageYOffset() { return pageYOffset; }
		public float getBorderWidth() { return borderWidth; }
		public float getheaderHeight() { return headerHeight; }
		public boolean isMaximized(){ return maximized;}

		/**
		 * @param window Selenium WebDriver Window object
		 * @return A browser window object returned by javascript,
		 *         containing properties like height, clientWidth, pageXOffset etc.<br>
		 *         null, if some exception happens.
		 *         PROPERTY_DRIVER_WINDOW=Window object if we can store it.
		 */
		public static Object getWindowObjectByJS(Window window){
			String debugmsg = StringUtils.debugmsg(BrowserWindow.class, "getWindowObjectByJS");
			Hashtable<String, Object> windowProperties = new Hashtable<String, Object>();
			try{
				//getSize() is not supported by "chromedriver" for "android chrome"
				Dimension d = window.getSize();
				windowProperties.put(PROPERTY_HEIGHT, d.height);
				windowProperties.put(PROPERTY_WIDTH, d.width);
			}catch(Throwable th){
				IndependantLog.warn(debugmsg + getThrowableMessages(th));
			}
			try{
				//getPosition() is not supported by "chromedriver" for "android chrome"
				Point p = window.getPosition();
				windowProperties.put(PROPERTY_LOCATION_X, p.x);
				windowProperties.put(PROPERTY_LOCATION_Y, p.y);
			}catch(Throwable th){
				IndependantLog.warn(debugmsg + getThrowableMessages(th));
			}

			try{
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(JavaScriptFunctions.defineObject(windowProperties));
				jsScript.append(JavaScriptFunctions.getBrowserInformation());
				jsScript.append("return getBrowserInformation(defineObject());");

				Object result = WDLibrary.executeScript(jsScript.toString());
				if(result instanceof Map){
					try{((Map)result).put(PROPERTY_DRIVER_WINDOW, window);}
					catch(UnsupportedOperationException uo){
						Hashtable newmap = new Hashtable((Map)result);
						newmap.put(PROPERTY_DRIVER_WINDOW, window);
						return newmap;
					}
				}
				return result;
			}catch(Throwable th){
				IndependantLog.error(debugmsg + getThrowableMessages(th));
			}
			return null;
		}
	}

	/**
	 * Currently, shutdown will "quit()" every webdriver that is stored here.
	 * Ideally, we do NOT want to quit instances that are running attached to a
	 * Selenium Server--whether it is remote or local.
	 * @throws Throwable
	 */
	public synchronized static void shutdown()throws Throwable{
		lastUsedWD = null;
		WebDriver wd = null;
		String id = null;
		Enumeration<String> keys = webDrivers.keys();
		while(keys.hasMoreElements()){
			id = keys.nextElement();
			wd = removeWebDriver(id);
			wd.quit();
		}
		webDrivers.clear();
		webDriverStack.clear();
		RemoteDriver.deleteSessionFile();
	}

	/**
	 * @param tag
	 * @return true if the provided tag is in the list of nonui tags to be ignored for xpath
	 * @see #IGNORED_NONUI_TAGS
	 */
	public static boolean isIgnoredNonUITag(String tag){
		try{ for(String s:IGNORED_NONUI_TAGS)
			if (tag.equalsIgnoreCase(s)) return true;
		}catch(NullPointerException ignore){}
		return false;
	}

	/**
	 * Store the WebDriver in the Hashtable by id.<br>
	 * This also sets this WebDriver as the "current" or "lastUsed" WebDriver.
	 * If there exists already a WebDriver in the Hashtable with the same id, it will be replaced by the new one,<br>
	 * and the old one will be returned. User will decide how to handle the old one, either store it with an other id<br>
	 * or simply stop it by calling {@link WebDriver#quit()}<br>
	 * <br>
	 * @param id String, the key for the WebDriver to store in the Hashtable.<br>
	 *                   if user provides null, then the hashcode of WebDriver will be used as key.<br>
	 * @param wd WebDriver, the WebDriver to store in the Hashtable.
	 * @return WebDriver, the previous WebDriver stored in the Hashtable with the same id; null if no such WebDriver exists.
	 */
	protected synchronized static WebDriver addWebDriver(String id, WebDriver wd){
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "addWebDriver");
		WebDriver previous = null;

		if(id == null || id.equals("")){
			id = String.valueOf(wd.hashCode());
		}
		if(webDrivers.containsKey(id)){
			IndependantLog.warn(debugmsg+"You are going to override the old WebDriver with id '"+id+"'");
			previous = webDrivers.get(id);
		}
		IndependantLog.debug(debugmsg+"You are adding WebDriver with id '"+id+"'");

		org.safs.selenium.webdriver.CFComponent.clearInternalCache();

		webDrivers.put(id,wd);
		lastUsedWD = wd;
		webDriverStack.remove(id);
		webDriverStack.add(id);  // push it to be the last always
		try{ RemoteDriver.setLastSessionId(id);}
		catch(Exception x){
			IndependantLog.error(debugmsg+"RemoteDriver error during setLastSessionId().");
		}
		try{ refreshJSExecutor(); }catch(SeleniumPlusException ignored){}
		return previous;
	}

	/**
	 * Switch to browser indicated by ID. It will get the cached webdriver according to the browser's id <br>
	 * and set it to the {@link SearchObject#lastUsedWD} and set the {@link SearchObject#lastJS} if possible.<br>
	 * @param ID	String, the id to identify the browser
	 * @throws SeleniumPlusException
	 */
	public static void useBrowser(String ID) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "useBrowser");
		WebDriver webdriver = getWebDriver(ID);

		if(webdriver==null){
			throw new SeleniumPlusException(debugmsg+"cannot get webdriver according to id '"+ID+"'",SeleniumPlusException.CODE_OBJECT_IS_NULL);
		}
		try{ RemoteDriver.setLastSessionId(ID);}catch(Exception e){}

		org.safs.selenium.webdriver.CFComponent.clearInternalCache();

		webDriverStack.remove(ID);
		webDriverStack.add(ID); // make it the "lastUsed"
		if(lastUsedWD!=webdriver){
			lastUsedWD=webdriver;
			try{ refreshJSExecutor(); }catch(SeleniumPlusException ignored){ }
		}
	}


	/**
	 * At this time does NOT set the retrieved WebDriver to be the "current" or "lastUsed" WebDriver.
	 * @param id String, the ID for the WebDriver stored in the Hashtable.<br>
	 *                   if user provides null, then the last used WebDriver will be returned.<br>
	 * @return WebDriver, the WebDriver associated with id; null if no WebDriver is found.
	 */
	protected synchronized static WebDriver getWebDriver(String id){
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "getWebDriver");
		WebDriver temp = null;

		if(id != null) temp = webDrivers.get(id);
		else temp = lastUsedWD;

		if(temp==null) IndependantLog.warn(debugmsg+"did not get cached WebDriver for id '"+id+"'");

		return temp;
	}

	/**
	 * @param title String, the case-insensitive title within the WebDriver stored in the Hashtable.<br>
	 *                   if user provides null or a zero-length title then the last used WebDriver will be returned.<br>
	 *                   the routine does not (yet) set the found WebDriver to be the lastUsedWD.  That might change, though.
	 * @return WebDriver, the WebDriver associated with the browser title; null if no matching WebDriver is found.
	 */
	protected synchronized static WebDriver getWebDriverWithTitle(String title){
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "getWebDriverWithTitle");
		WebDriver temp = null;
		WebDriver match = null;
		String lctitle = null;
		String wdtitle = null;
		if(title != null && title.length()> 0) {
			lctitle = title.toLowerCase().trim();
			Enumeration<WebDriver> e = webDrivers.elements();
			while(e.hasMoreElements()){
				temp = e.nextElement();
				try{
					wdtitle = temp.getTitle().trim();
					IndependantLog.info(debugmsg+" evaluating WebDriver with title '"+ wdtitle +"'");
					if(lctitle.equals(wdtitle.toLowerCase())){
						match = temp;
						break;
					}
				}catch(NullPointerException notitle){}
			}
		}
		else match = lastUsedWD;

		if(match==null) IndependantLog.warn(debugmsg+"cannot get WebDriver with title '"+title+"'");

		return match;
	}

	/**
	 * @param title String, the case-insensitive title substring within the WebDriver stored in the Hashtable.<br>
	 *                   if user provides null or a zero-length title then the last used WebDriver will be returned.<br>
	 *                   the routine does not (yet) set the found WebDriver to be the lastUsedWD.  That might change, though.
	 * @return WebDriver, the WebDriver associated with the browser title; null if no matching WebDriver is found.
	 */
	protected synchronized static WebDriver getWebDriverContainsTitle(String title){
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "getWebDriverContainsTitle");
		WebDriver temp = null;
		WebDriver match = null;
		String lctitle = null;
		String wdtitle = null;
		if(title != null && title.length()> 0) {
			lctitle = title.toLowerCase().trim();
			Enumeration<WebDriver> e = webDrivers.elements();
			while(e.hasMoreElements()){
				temp = e.nextElement();
				try{
					wdtitle = temp.getTitle().trim();
					IndependantLog.info(debugmsg+" evaluating WebDriver with title '"+ wdtitle +"'");
					if(wdtitle.toLowerCase().contains(lctitle)){
						match = temp;
						break;
					}
				}catch(NullPointerException notitle){}
			}
		}
		else match = lastUsedWD;

		if(match==null) IndependantLog.warn(debugmsg+"cannot get WebDriver with title contains '"+title+"'");

		return match;
	}

	/**
	 * At this time does NOT set the processed WebDriver to be the "current" or "lastUsed" WebDriver.
	 * @param wd WebDriver to get our stored ID of.
	 * @return String ID of the WebDriver, or null if not found.
	 */
	public synchronized static String getIDForWebDriver(WebDriver wd){
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "getIDForWebDriver");
		WebDriver temp = null;
		String id = null;
		String match = null;
		if(wd == null) return null;
		Enumeration<String> e = webDrivers.keys();
		while(e.hasMoreElements()){
			id = e.nextElement();
			temp = webDrivers.get(id);
			if (temp.equals(wd)){
				match = id;
				break;
			}
		}
		if(match==null) IndependantLog.warn(debugmsg+"cannot get ID for the WebDriver instance provided.");

		return match;
	}

	/**
	 * @return String[] of all known WebDriver window titles, or an empty array.
	 */
	public synchronized static String[] getAllWindowTitles(){
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "getAllWindowTitles");
		if(webDrivers.isEmpty()) {
			getWebDriver();
			if(webDrivers.isEmpty()) return new String[0];
		}

		Entry<String/*Handle*/, String/*Title*/> originalWindow = null;
		if(lastUsedWD!=null){
			originalWindow = new SimpleEntry<>(lastUsedWD.getWindowHandle(), lastUsedWD.getTitle());
		}

		ArrayList<String> titles = new ArrayList<>();
		Collection<WebDriver> driverList = webDrivers.values();
		Set<String> winHandles = null;
		for(WebDriver webdriver: driverList){
			winHandles = webdriver.getWindowHandles();
			for(String handle: winHandles){
				try{
					webdriver.switchTo().window(handle);
					titles.add(webdriver.getTitle());
				}catch(Exception ignore){
					IndependantLog.warn(debugmsg+" failed to add title for window indentified by handle '"+handle+" for webdriver "+webdriver+", due to "+ignore);
				}
			}
		}
		IndependantLog.info(debugmsg+"retrieved "+ titles.size() +" window titles.");

		if(originalWindow!=null){
			try{
				lastUsedWD.switchTo().window(originalWindow.getKey());
				IndependantLog.debug(debugmsg+" switched back to original window '"+originalWindow.getValue()+"' by handle '"+originalWindow.getKey()+"'.");
			}catch(Exception e){
				IndependantLog.debug(debugmsg+" failed to switch back to original window '"+originalWindow.getValue()+"' by handle '"+originalWindow.getKey()+"', due to "+e);
			}
		}

		return titles.toArray(new String[0]);
	}

	/**
	 * This will reconnect/recreate working WebDrivers we know about to running instances on a Selenium Server.
	 * Each invocation causes that WebDriver to become the "current" or "lastUsed" WebDriver.
	 * Thus, you may wish to call WDLibrary.getBrowserWithID or getBrowserWithTitle to make the real target WebDriver
	 * the "current" or "lastUsed" WebDriver.
	 * @param info SessionInfo
	 * @return WebDriver instance restored or null if not successfully created.
	 */
	private static RemoteDriver reconnectWebDriverSession(SessionInfo info){
		String debugmsg = StringUtils.debugmsg(false);
		if(! (info instanceof SessionInfo)) {
			IndependantLog.warn(debugmsg+" parameter cannot be null!");
			return null;
		}
		String host = info.serverHost;
		if (host == null || host.isEmpty()) host = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_HOST);
		if (host == null || host.isEmpty()) host = SelectBrowser.DEFAULT_SELENIUM_HOST;
		String port = System.getProperty(SelectBrowser.SYSTEM_PROPERTY_SELENIUM_PORT);
		if (port == null || port.isEmpty()) port = SelectBrowser.DEFAULT_SELENIUM_PORT;

		DesiredCapabilities capabilities = SelectBrowser.getDesiredCapabilities(info.browser, info.extraParameters);
		capabilities.setJavascriptEnabled(true);
		capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
		capabilities.setCapability(RemoteDriver.CAPABILITY_ID, info.id);
		capabilities.setCapability(RemoteDriver.CAPABILITY_RECONNECT, true);
		capabilities.setCapability(RemoteDriver.CAPABILITY_REMOTESERVER, host);
		capabilities.setBrowserName(info.browser);
		try{
			// try to see if it is a valid session
			IndependantLog.debug(debugmsg+" instantiating unverified RemoteDriver to 'http://" + host + ":" + port +"/wd/hub' ... ");
			return RemoteDriver.instance(new URL("http://" + host + ":" + port +"/wd/hub"),capabilities);
		}catch(Throwable t){
			IndependantLog.warn(debugmsg+"ignoring reconnect RemoteDriver "+ t.getClass().getSimpleName()+", "+t.getMessage()+". RemoteServer may not be running or DOM may be in transition!");
		}
		return null;
	}

	/**
	 * Will only connect to the "current" session.  Normally only called after a catastrophic failure of a RemoteDriver.
	 * @return
	 */
	protected static WebDriver reconnectLastWebDriver(){
		String debugmsg = StringUtils.debugmsg(false);

		List<SessionInfo> list = null;
		RemoteDriver result = null;
		try {
			list = RemoteDriver.getSessionsFromFile();
			if(list == null||list.isEmpty()){
				IndependantLog.info(debugmsg+ " did not find session info to retrieve.");
				return null;
			}
		} catch (Exception e) {
			IndependantLog.debug(debugmsg+ " did not retrieve session info successfully.", e);
			return null;
		}
		IndependantLog.info(debugmsg+ " found "+list.size()+" stored sessions to process.");
		for(SessionInfo info: list){
			if(info.isCurrentSession){
				try{
					result = reconnectWebDriverSession(info);
					// NOT changing anything about the existing sessions
					//d.manage().window().setSize(d.manage().window().getSize());
					addWebDriver(info.id,result);
				}catch(Throwable t){
					IndependantLog.warn(debugmsg+"ignoring reconnect RemoteDriver "+ t.getClass().getSimpleName()+", "+t.getMessage()+". RemoteServer may not be running or DOM may be in transition!");
				}
			}
		}
		return lastUsedWD;
	}

	/**
	 * Get the last used "current" WebDriver.
	 * If we do not have a last used WebDriver, we will attempt to process the remote session file
	 * to reconnect existing webdriver sessions and return the one that was stored as the last
	 * used session.
	 * @return WebDriver, the lastUsed WebDriver or null if no WebDriver was found.
	 */
	public synchronized static WebDriver getWebDriver(){
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "getWebDriver");

		if (lastUsedWD == null){
			List<SessionInfo> list = null;
			try {
				list = RemoteDriver.getSessionsFromFile();
				if(list == null||list.isEmpty()){
					IndependantLog.info(debugmsg+ " did not find session info to retrieve.");
					return null;
				}
			} catch (Exception e) {
				IndependantLog.debug(debugmsg+ " did not retrieve session info successfully.", e);
				return null;
			}
			IndependantLog.info(debugmsg+ " found "+list.size()+" stored sessions to process.");
			RemoteDriver lastdriver = null;
			String lastid = null;
			for(SessionInfo info: list){
				try{
					RemoteDriver d = reconnectWebDriverSession(info);
					d.manage().window().setSize(d.manage().window().getSize());
					addWebDriver(info.id,d); // overwrites lastUsedWD every time!
					if(info.isCurrentSession) {
						lastdriver = d;
						lastid = info.id;
					}
				}catch(Throwable t){
					IndependantLog.warn(debugmsg+"ignoring reconnect RemoteDriver failure.  RemoteServer may not be running!");
					try{
						IndependantLog.info(debugmsg+ " attempting to remove remote session info for id: "+ info.id);
						RemoteDriver.deleteSessionIdFromFile(info.id);
					}catch(Throwable ignore){

					}
				}
			}
			if(lastdriver != null){
				addWebDriver(lastid, lastdriver);
			}
			if (lastUsedWD == null){
				IndependantLog.error(debugmsg+"session can't reconnected");
				return null;
			}
		}
		if(lastUsedWD instanceof RemoteDriver &&((RemoteDriver)lastUsedWD).hasQuit()){
			IndependantLog.error(debugmsg+"detecting lastUsedWD has quit. Resetting.");
			lastUsedWD = null;
		}
		return lastUsedWD;
	}

	/**
	 * if the id is valid (not null and not empty).
	 * @param ID String, the browser id.
	 * @return boolean true if the browser id is valid (not null and not empty).
	 */
	public static boolean isValidBrowserID(String ID){
		return (ID!=null && !ID.isEmpty());
	}

	/**
	 * Removes the provided webdriver from internal storage and deletes the session info
	 * in the RemoteDriver.  Will pop off the next "lastUsed" WebDriver off the stack and
	 * set it as the new "lastUsed" WebDriver and sets the RemoteDriver "lastSessionId"
	 * as needed.
	 * @param id String, the ID for the WebDriver to remove<br>
	 *                   if it is NOT {@link #isValidBrowserID(String)}, then the last used WebDriver will be removed.<br>
	 * @return	WebDriver, the WebDriver has been removed; null if no WebDriver is removed.
	 */
	protected synchronized static WebDriver removeWebDriver(String id){
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "removeWebDriver");
		WebDriver obj = null;
		try{
			org.safs.selenium.webdriver.CFComponent.clearInternalCache();

			if(isValidBrowserID(id)) obj = webDrivers.remove(id);
			else{
				IndependantLog.warn(debugmsg+" the parameter id '"+id+"' is not a valid browser-ID, try to remove the last used web-driver.");
				if(lastUsedWD!=null){
					Enumeration<String> keys = webDrivers.keys();
					String key = null;
					while(keys.hasMoreElements()){
						key = keys.nextElement();
						if(lastUsedWD.equals(webDrivers.get(key))){
							obj = lastUsedWD;
							id = getIDForWebDriver(obj);
							webDrivers.remove(key);
							break;
						}
					}
				}
			}
			if (id != null){
				webDriverStack.remove(id);
				RemoteDriver.deleteSessionIdFromFile(id);
				if(!webDriverStack.isEmpty()){
					String newid = webDriverStack.lastElement();
					useBrowser(newid);
				}
			}
		}catch(Throwable x){
			IndependantLog.error(debugmsg+"cannot remove WebDriver for id '"+id+"'",x);
		}
		return obj;
	}

	public static WebElement findElement(By by){

		return lastUsedWD.findElement(by);
	}

	/**
	 * Get the object based on the recognition string.
	 * Currently, the routine assumes the recognition string is for an item in
	 * the lastUsedWD--the last used WebDriver.
	 * This routine will be modified to detect a recognition string that may include
	 * the need to locate which WebDriver is the right one and not assume the last one
	 * is the right one.
	 * @param RS
	 * @return
	 * @see #getObject(SearchContext, String)
	 */
	public static WebElement getObject(String RS){
		return getObject(getWebDriver(),RS);
	}

	/**
	 * @param rs
	 * @return true if the RS contains a qualifier of "TAG=value" (case-insensitive comparison)
	 */
	public static boolean containTagRS(String rs){
		if(rs==null) return false;
		String uprs = rs.toUpperCase().replaceAll(" ", "");
		if(uprs.startsWith(SEARCH_CRITERIA_TAG + assignSeparator) ||
		   uprs.contains(qulifierSeparator + SEARCH_CRITERIA_TAG + assignSeparator)){
			return true;
		}else{
			return false;
		}
	}

	public static boolean containFrameRS(String rs){
		if(rs==null) return false;
		String uprs = rs.toUpperCase();
		if(uprs.contains(SEARCH_CRITERIA_FRAMEID) ||
				uprs.contains(SEARCH_CRITERIA_IFRAMEID) ||
				uprs.contains(SEARCH_CRITERIA_FRAMENAME) ||
				uprs.contains(SEARCH_CRITERIA_FRAMEINDEX) ||
				uprs.contains(SEARCH_CRITERIA_FRAMEXPATH)){
			return true;
		}else{
			return false;
		}
	}

	public static boolean isValidFrameRS(String frameRS){
		if(frameRS==null) return false;
		String uprs = frameRS.toUpperCase();
		if(uprs.contains(SEARCH_CRITERIA_FRAMEID) ||
				uprs.contains(SEARCH_CRITERIA_IFRAMEID) ||
				uprs.contains(SEARCH_CRITERIA_FRAMENAME) ||
				uprs.contains(SEARCH_CRITERIA_FRAMEINDEX) ||
				uprs.contains(SEARCH_CRITERIA_FRAMEXPATH)){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * @param recognitionString String
	 * @return boolean, true if the RS is a kind of frame-RS
	 */
	protected static boolean isFrameRS(String recognitionString){
		try{
			String upperCaseRS = recognitionString.trim().toUpperCase();
			if(  upperCaseRS.startsWith(SEARCH_CRITERIA_FRAMEID)
					||upperCaseRS.startsWith(SEARCH_CRITERIA_IFRAMEID)
					||upperCaseRS.startsWith(SEARCH_CRITERIA_FRAMENAME)
					||upperCaseRS.startsWith(SEARCH_CRITERIA_FRAMEINDEX)
					||upperCaseRS.startsWith(SEARCH_CRITERIA_FRAMEXPATH)
					){
				return true;
			}
		}catch(Exception e){ }
		return false;
	}

	/**
	 * if the frame-RS is specified as one qualifier of RS, find and return it.<br>
	 * For example, for "FrameID=yyy;property=xxx;property=xxx", we return "FrameID=yyy"<br>
	 * The rest "property=xxx;property=xxx" is not useful for now, as FrameId, FrameName<br>
	 * and FrameXpath, they are enough to find a frame.<br>
	 * @param recognitionString String
	 * @return String, the frame-RS
	 */
	protected static String retrieveFrameRS(String recognitionString){
		String frameRS = null;
		//if frame-RS is specified with multiple qualifiers, we just care about the frame-RS
		String[] tokens = StringUtils.getTokenArray(recognitionString, qulifierSeparator, escapeChar);
		for(String qualifier: tokens){
			if(isFrameRS(qualifier)){
				frameRS=qualifier;
				break;
			}
		}
		return frameRS;
	}

	/**
	 * Get the current frame WebElement.<br>
	 * <b>NOTE: The frame WebElement is attached to the parent frame, we cannot call any method on this frame WebElement (we are in itself) until we switch to its parent frame by calling webdriver.switchTo().parentFrame().</b>
	 * @param webdriver WebDriver, it should also be a JavascriptExecutor
	 * @return WebElement, the web element representing the current frame; null if we are not in any frame.
	 */
	public static WebElement getCurrentFrame(WebDriver webdriver) throws SeleniumPlusException{
		try{
			JavascriptExecutor jsExecutor = (JavascriptExecutor) webdriver;
			return (WebElement) jsExecutor.executeScript("if(window.frameElement) return window.frameElement; else return null;");
		}catch(Exception e){
			throw new SeleniumPlusException("Failed to get current frame, due to "+e);
		}
	}

	/**
	 * Get the ID of current frame.
	 * @param webdriver WebDriver, it should also be a JavascriptExecutor
	 * @return String, the ID of the current frame; null if we are not in any frame.
	 */
	public static String getCurrentFrameID(WebDriver webdriver) throws SeleniumPlusException{
		try{
			JavascriptExecutor jsExecutor = (JavascriptExecutor) webdriver;
			return (String) jsExecutor.executeScript("if(window.frameElement) return window.frameElement.id; else return null;");
		}catch(Exception e){
			throw new SeleniumPlusException("Failed to get current frame's ID, due to "+e);
		}
	}

	/**
	 * Get the name of current frame.
	 * @param webdriver WebDriver, it should also be a JavascriptExecutor
	 * @return String, the name of the current frame; null if we are not in any frame.
	 */
	public static String getCurrentFrameName(WebDriver webdriver) throws SeleniumPlusException{
		try{
			JavascriptExecutor jsExecutor = (JavascriptExecutor) webdriver;
			return (String) jsExecutor.executeScript("if(window.frameElement) return window.frameElement.name; else return null;");
			//return (String) jsExecutor.executeScript("return self.name");
		}catch(Exception e){
			throw new SeleniumPlusException("Failed to get current frame's name, due to "+e);
		}
	}

	/**
	 *
	 * Get the ID or name of current frame, or the frame object itself if there is no ID or name.
	 * @param webdriver WebDriver, it should also be a JavascriptExecutor
	 * @return Object, the ID or name of current frame, or the frame object itself if there is no ID or name; null if we are not in any frame context.
	 * @throws SeleniumPlusException
	 */
	public static Object _getCurrentFrame(WebDriver webdriver) throws SeleniumPlusException{
		Object currentFrame = null;
		try{
			JavascriptExecutor jsExecutor = (JavascriptExecutor) webdriver;
			StringBuffer scriptCommand = new StringBuffer();
			scriptCommand.append(JavaScriptFunctions.getCurrentFrame());
			scriptCommand.append("return getCurrentFrame();");
			currentFrame = jsExecutor.executeScript(scriptCommand.toString());
		}catch(Exception e){
			throw new SeleniumPlusException("Failed to get current frame's ID/NAME/OBJECT, due to "+e);
		}
		return currentFrame;
	}

	private static Object _getCurrentFrame() throws SeleniumPlusException{
		return _getCurrentFrame(getWebDriver());
	}

	/**
	 * Switch to a certain frame according to the parameter frameRS.<br>
	 * <b>Note:</b> User should be extremely careful to call this method, which will change the frame to search within.<br>
	 * <b>Note:</b> Before calling this method, make sure that <b>getWebDriver().switchTo().defaultContent()</b> is called.<br>
	 * <b>Note:</b> To make sure that we can get the correct screen location, we must call {@link #initializeBrowserWindow()} before calling this method.<br>
	 * This routine does NOT change the lastFrame setting at any time.
	 * @param webdriver WebDriver, the webdriver used to search frame and switch frame
	 * @param frameRS String, the recognition string of the frame to switch to.
	 * @return true if a frameRS was found and we have
	 * successfully switched the webdriver to use it. false otherwise.
	 */
	public static boolean switchFrame(WebDriver webdriver, String frameRS){
		WebElement frame = _getSwitchFrame(webdriver, frameRS);
		return _switchFrame(webdriver, frame);
	}

	/**
	 * Switch a WebDriver to toplevel defaultContent (no frame) context.<br>
	 * Clears the lastFrame FrameElement accordingly.<br>
	 * <b>Note:</b> User should be extremely careful to call this method, which will change the frame to search within.<br>
	 * @param webdriver WebDriver, the webdriver to switch to toplevel defaultContent context.
	 */
	public static void switchToDefaultContent(WebDriver webdriver){
		IndependantLog.info("SearchObject.switchToDefaultContent: switching to defaultContent--no frame. lastFrame = null.");
		webdriver.switchTo().defaultContent();
		setLastFrame(null);
	}

	/**
	 * Switch back to the parent frame or do nothing if we are already in the top document.
	 * @param webdriver WebDriver
	 */
	protected static void switchToParentFrame(WebDriver webdriver){
		webdriver.switchTo().parentFrame();
		IndependantLog.info("SearchObject.switchToParentFrame: switching back to parent frame.");
	}

	/**
	 * Switch to a certain frame according to the parameter frameRS.<br>
	 * Also sets/replaces/clears the lastFrame FrameElement accordingly.
	 * <b>Note:</b> User should be extremely careful to call this method, which will change the frame to search within.<br>
	 * <b>Note:</b> Before calling this method, make sure that <b>switchToDefaultContent(webdriver)</b> is called.<br>
	 * <b>Note:</b> To make sure that we can get the correct screen location, we must call {@link #initializeBrowserWindow()} before calling this method.<br>
	 * @param webdriver WebDriver, the webdriver used to search frame and switch frame
	 * @param frameRS String, the recognition string of the frame.<br>
	 * Ex: id=..., name=..., frameid=..., iframeid=..., framename=..., frameindex=..., framexpath=....<br>
	 * If a frame name is provided with no RS string prefix then an attempt will be made to find the frame by id and by name.<br>
	 * if the frameRS is null or empty, we will switchToDefaultContent (no frame) and set lastFrame as null.
	 * @return the FrameElement if it was found and set, or null if we are resetting to default content (no frame).
	 * @throws SeleniumPlusException if a problem occurs finding, setting, or using any non-null FrameElement.
	 * @see #switchToDefaultContent(WebDriver)
	 */
	public static FrameElement switchFrameAsLastFrame(WebDriver webdriver, String frameRS) throws SAFSObjectNotFoundException, SeleniumPlusException{
		WebElement frame = null;
		if(frameRS == null || frameRS.isEmpty()){
			switchToDefaultContent(webdriver);
			return getLastFrame();
		}
		String dbgmsg = StringUtils.debugmsg(false);
		frame = _getSwitchFrame(webdriver, frameRS);
		if (frame == null) throw new SAFSObjectNotFoundException(frameRS +" not found.");

	    FrameElement felement = null;
	    FrameElement parentFrame = getLastFrame();
	    if(parentFrame instanceof FrameElement)
		    IndependantLog.warn(dbgmsg+" uncommon scenario of frame within a frame. lastFrame = "+ parentFrame);
	    else
		    IndependantLog.debug(dbgmsg+" retrieved null lastFrame--as commonly expected in single-frame applications.");
		try{
		    IndependantLog.debug(dbgmsg+" creating FrameElement using parentFrame: "+ parentFrame +" and frame: "+ frame);
			felement = new FrameElement(parentFrame, frame);
		    IndependantLog.debug(dbgmsg+" created FrameElement: "+ felement);
			setLastFrame(felement);
		}catch(Throwable serx){
		    IndependantLog.debug(dbgmsg+" ignoring "+ serx.getClass().getSimpleName() +" and trying with a null parentFrame...");
		    try{
			    IndependantLog.debug(dbgmsg+" creating FrameElement using null parentFrame and frame: "+ frame);
				felement = new FrameElement(null, frame);
			    IndependantLog.debug(dbgmsg+" created FrameElement: "+ felement);
				setLastFrame(felement);
		    }catch(Throwable serx2){
			    IndependantLog.debug(dbgmsg+" ignoring 2nd "+ serx2.getClass().getSimpleName() +" and trying with original parentFrame: "+ parentFrame);
				try{
				    IndependantLog.debug(dbgmsg+" last try creating FrameElement using parentFrame: "+ parentFrame +" and frame: "+ frame);
					felement = new FrameElement(parentFrame, frame);
				    IndependantLog.debug(dbgmsg+" created FrameElement: "+ felement);
					setLastFrame(felement);
				}catch(Throwable serx3){
				    IndependantLog.warn(dbgmsg+" encounterd 3rd/final "+ serx3.getClass().getSimpleName() +" and giving up.");
				    IndependantLog.warn(dbgmsg+" !!! Internal lastFrame reference may be incorrect !!!");
               }
		    }
		}
		if(felement == null)
		    IndependantLog.warn(dbgmsg+" !!! Internal lastFrame reference may be invalid null !!!");
		else if(! _switchFrame(webdriver, frame)) {
			setLastFrame(parentFrame);
		    IndependantLog.warn(dbgmsg+" throwing SeleniumPlusException: Cannot change lastFrame due to reported _switchFrame problem!");
			throw new SeleniumPlusException("Cannot change lastFrame due to reported _switchFrame problem!");
		}

		return getLastFrame();
	}

	/**
	 * Switch to the frame indicated by the recognition string.<br>
	 * <b>NOTE: This method will switch back to the topmost document and start searching from there.<br>
	 *          If there are multiple layers of frame, the recognition string should be in parent-child format, such as frameid=...;\\;framename=...<br>
	 * </b>
	 * <p>
	 * This method will probably change the field {@link #lastBrowserWindow}<br>
	 * This method will probably change the filed {@link #lastFrame}<br>
	 *
	 * @param recognitionString String, the recognition string with frame information.
	 *        Ex: id=..., name=..., frameid=..., iframeid=..., framename=..., frameindex=..., framexpath=....<br>
	 *            frameid=...;\\;frameid=...<br>
	 *            frameid=...;\\;framename=...<br>
	 *
	 * @return boolean, if the frame has been switched successfully.
	 */
	public static synchronized boolean switchFrames(String recognitionString){
		boolean originBypassFrameReset = getBypassFramesReset();
		FrameElement originLastFrame = lastFrame;

		//set 'bypassFrameReset' to false, this will make sure we switch frame if we find it.
		setBypassFramesReset(false);
		//set 'lastFrame' to null, this will make sure we don't switch frame by the 'lastFrame'
		lastFrame = null;
		SwitchFramesResults switchResult = _switchFrames(recognitionString);
		//set 'bypassFrameReset' to its original value.
		setBypassFramesReset(originBypassFrameReset);
		boolean switched = switchResult.haveSwitchedFrames();
		//if we didn't switch the frame, we need to set 'lastFrame' to its original value.
		if(!switched) lastFrame = originLastFrame;

		return switched;
	}

	private static WebElement _getSwitchFrame(WebDriver  webdriver, String frameRS){
		String debugmsg = StringUtils.debugmsg(false);

		if(webdriver==null || frameRS==null) return null;

		WebElement frame = null;
		String[] tokens = StringUtils.getTokenArray(frameRS, assignSeparator, escapeChar);
		if(tokens==null || tokens.length==0){ // should never happen?
			IndependantLog.error(debugmsg+"frame recognition string '"+frameRS+"' could not be processed.");
			return null;
		}
		String searchCreteria = null;
		String value = null;
		if(tokens.length > 1) {
			searchCreteria = tokens[0].trim().toUpperCase();
			value = tokens[1];
		}else{
			value = tokens[0];
		}

		//Reset the 'lastFrame' to null, if user defines the frame's id/name as "null".
		if(TOP_DOCUMENT_ID.equalsIgnoreCase(value)){
			IndependantLog.debug(debugmsg+" frame RS is '"+frameRS+"', user wants to reset the lastFrame to null.");
			lastFrame = null;
			return null;
		}

		if(searchCreteria == null){
			frame = webdriver.findElement(By.id(value));
			if(frame == null)
				frame = webdriver.findElement(By.name(value));

		}else if(searchCreteria.equals(SEARCH_CRITERIA_FRAMEID) ||
				 searchCreteria.equals(SEARCH_CRITERIA_IFRAMEID)||
				 searchCreteria.equals(SEARCH_CRITERIA_ID)){
			frame = webdriver.findElement(By.id(value));

		}else if(searchCreteria.equals(SEARCH_CRITERIA_FRAMENAME) ||
				 searchCreteria.equals(SEARCH_CRITERIA_NAME)){
			frame = webdriver.findElement(By.name(value));

		}else if(searchCreteria.equals(SEARCH_CRITERIA_FRAMEINDEX)){
			int index = StringUtilities.parseIndex(value);
			//index is 1-based, needs to minus 1 to get 0-based index and feed it to method frame().

			//TODO How to get the frame's webelement???
			frame = getFrameWebElement(webdriver, index);//Not found???
			//frame = targetLocator.activeElement();//activeElement() is not the frame itself
			IndependantLog.warn(debugmsg+" currently it is NOT suggested to use index for frame!");

		}else if(searchCreteria.equals(SEARCH_CRITERIA_FRAMEXPATH)){
			frame = webdriver.findElement(By.xpath(value));

		}else{
			IndependantLog.warn(debugmsg+" frame recognition string '"+frameRS+"' is not supported.");
		}

		if(frame!=null){
			IndependantLog.debug(debugmsg+" get switching-frame '"+frame+"' according to '"+value+"'");
		}else{
			IndependantLog.debug(debugmsg+" requested frame '"+frameRS+"' was not found!");
		}

		return frame;
	}

	/**
	 * Execute a WebDriver.switchTo().frame with the provided frame WebElement.<br>
	 * This does NOT set our internal lastFrame reference to the new frame.
	 * @param webdriver
	 * @param frame
	 * @return true unless an Exception was thrown and caught indicating failure.
	 */
	private static boolean _switchFrame(WebDriver  webdriver, WebElement frame){
		if(webdriver==null || frame==null) return false;
		String debugmsg = StringUtils.debugmsg(false);

		try{
			webdriver.switchTo().frame(frame);
			IndependantLog.debug(debugmsg+" switched to frame '"+frame+"'");
			return true;
		}catch(Exception e){
			IndependantLog.debug(debugmsg+" failed to switch to frame '"+frame+"' due to "+StringUtils.debugmsg(e));
			return false;
		}
	}

	/**
	 * Generate a Tree object holding all the frames on the web page.
	 * @param selenium WebDriver
	 * @return Arbre&lt;FrameElement>>, Tree object holding all the frames on the web page.
	 */
	public static Arbre<FrameElement> getFrameTree(WebDriver selenium){
		Arbre<FrameElement> frameTree = new Arbre<FrameElement>();
		generateFrameTree(frameTree, selenium);
		return frameTree;
	}

	/**
	 * Generate the tree to hold all frames on the page.
	 *
	 * @param frameTree Arbre&lt;FrameElement>>, the tree to hold all the frames. It should be provided as an instance of Arbre by the caller.
	 * @param selenium WebDriver
	 */
	private static void generateFrameTree(Arbre<FrameElement> frameTree, WebDriver selenium){
		String dbgmsg = StringUtils.debugmsg(false);
		Arbre<FrameElement> child = null;

		//switch to current frame document
		switchToFrame(frameTree, selenium);

		//get all frame WebElements in the current frame document
		List<WebElement> elements = getFrames(selenium);

		if(elements==null||elements.isEmpty()){
			IndependantLog.info(dbgmsg+" found no frames in the current window.");
		}else{
			IndependantLog.info(dbgmsg+" found "+ elements.size() +" frames in the current window.");
			//We need to keep in the same 'parent frame context' so that we can operate on the child frame WebElement.
			//So we generate all the child nodes.
			for(WebElement frame: elements){
				child = new Arbre<FrameElement>();
				child.setUserObject(new FrameElement(frameTree.getUserObject(), frame));
				frameTree.addChild(child);
			}
			//Then we dig into each child
			for(Arbre<FrameElement> childNode: frameTree.getChildren()){
				generateFrameTree(childNode, selenium);
			}
		}
	}

	/**
	 * Get all frame WebElement on current frame document (we can call {@link #switchToFrame(Arbre, WebDriver)} to switch to a certain frame).
	 *
	 * @param selenium WebDriver
	 * @return List&lt;WebElement> a list of frame WebElement
	 *
	 * {@link #switchToFrame(Arbre, WebDriver)}
	 */
	public static List<WebElement> getFrames(WebDriver selenium){
		List<WebElement> results = new ArrayList<WebElement>();

		List<WebElement> elements = selenium.findElements(By.tagName(HTMLConst.TAG_IFRAME));

		if(elements!=null && !elements.isEmpty()){
			results.addAll(elements);
		}

		elements = selenium.findElements(By.tagName(HTMLConst.TAG_FRAME));
		if(elements!=null && !elements.isEmpty()){
			results.addAll(elements);
		}

		return results;
	}

	/**
	 * Pre-order traverse the tree and add each tree node into a list.
	 *
	 * @param frameTree Arbre&lt;FrameElement>>, the tree to traverse
	 * @param frames List&lt;Arbre&lt;FrameElement>>>, a list of frames (each node of the frame tree).
	 *                                              The first element will always be the top document (no frame document).
	 */
	public static void traverseFrameTree(Arbre<FrameElement> frameTree, List<Arbre<FrameElement>> frames){
		if(frameTree!=null){
			frames.add(frameTree);
			List<Arbre<FrameElement>> children = frameTree.getChildren();
			if(children!=null){
				for(Arbre<FrameElement> frame: children){
					traverseFrameTree(frame, frames);
				}
			}
		}
	}

	/**
	 * Switch to a certain frame. If the frame has parent-frame, we will switch to the parent-frame firstly.<br>
	 * <b>NOTE: This method will also modify the class-field {@link #lastFrame},
	 *          which is very important for searching web-element or calculating the element's location etc.</b><br>
	 *
	 * @param frameNode  Arbre&lt;FrameElement>>, the frame to switch. We will switch to the top document if this parameter is null.
	 * @param selenium WebDriver
	 * @return boolean true if success to switch frame
	 */
	public static boolean switchToFrame(Arbre<FrameElement> frameNode, WebDriver selenium){
		String debugmsg = StringUtils.debugmsg(false);

		try{
			Stack<WebElement> ancestorFrames = new Stack<WebElement>();
			//switch to the top document
			selenium.switchTo().defaultContent();
			//Initialize the lastbrowserWidnow
			initializeBrowserWindow();
			//put the frames into the stack, parents on top, children on bottom
			Arbre<FrameElement> parentNode = frameNode;
			FrameElement frameElement = null;
			WebElement frame = null;
			while(parentNode!=null){
				frameElement = parentNode.getUserObject();
				if(frameElement!=null){
					frame = frameElement.getWebElement();
					if(frame!=null) ancestorFrames.push(frame);
				}
				parentNode = parentNode.getParent();
			}

			FrameElement frameElem = null;
			//we switch frame from the ancestor to children
			while(!ancestorFrames.empty()){
				frame = ancestorFrames.pop();
				frameElem = new FrameElement(frameElem, frame);
				selenium.switchTo().frame(frame);
			}
			lastFrame = frameElem;
			IndependantLog.debug(debugmsg +" CHANGED frame context:\nOur 'lastFrame' is "+lastFrame+"\nThe current frame context is "+_getCurrentFrame(selenium));
			return true;
		}catch(Exception e){
			IndependantLog.error("Failed to switch frame "+(frameNode==null?"":frameNode.getUserObject())+", due to "+e);
			return false;
		}
	}

	/**
	 * Example: tag=span;propertycontains=innerHTML:some value
	 *
	 * @param RS -- Full MultiAttribute qualifier RS. (Does not have to be a single qualifier assignment segment.)
	 * The algorithm does NOT assume the TAG qualifier is first in the RS.
	 * @return the value associated with any TAG= qualifier setting, or null if it does not exist.
	 * @throws SeleniumPlusException if the RS is not valid.
	 */
	protected static String getTagQualifierValue(String RS) throws SeleniumPlusException{
		if (RS == null || RS.length() < 5) return null;
		String rc = null;
		String[] st = StringUtils.getTokenArray(RS, qulifierSeparator, escapeChar);

		for(int i=0;i< st.length;i++){
			if(st[i].toUpperCase().startsWith(SEARCH_CRITERIA_TAG)){
				try{
					String[] p = getFirstQualifierPair(st[i]);
					if(p[0].equalsIgnoreCase(SEARCH_CRITERIA_TAG)){
						rc = p[1];
						if(rc == null) continue;
						int s = rc.indexOf(qulifierSeparator);
						if(s == 0 || rc.length() == 0){
							rc = null;
						}else if (s > 0){
							rc = rc.substring(0, s);
						}
						if (rc != null) break;
					}
				}catch(Exception ignore){}
			}
		}
		return rc;
	}

	/**
	 * Split a string by separator {@link #assignSeparator}, and return the result.<br>
	 * If the RS contains multiple {@link #assignSeparator}, this method will ONLY<br>
	 * consider the first separator, the string before that separator will be considered<br>
	 * as qualifier; the string after will be all considered as value.<br>
	 *
	 * @param RS String, a recognition with a {@link #assignSeparator} inside.
	 * @return String[], the separated RS, including qualifier and value.
	 * @throws SeleniumPlusException if the RS is not valid.
	 */
	protected static String[] getFirstQualifierPair(String RS) throws SeleniumPlusException{
		String[] assignPair = null;
		String errmsg = null;

		try{
			int assignindex = RS.indexOf(assignSeparator);

			if(assignindex<0 || assignindex==RS.length()-1) {
				errmsg = " invalid recognition string: "+ RS;
				IndependantLog.debug(StringUtils.debugmsg(false)+errmsg);
				throw new SeleniumPlusException(errmsg);
			}

			assignPair = new String[2];
			assignPair[0] = RS.substring(0, assignindex).trim();
			assignPair[1] = RS.substring(assignindex+1);
		}catch(Exception e){
			errmsg = " met "+ StringUtils.debugmsg(e);
			IndependantLog.debug(StringUtils.debugmsg(false)+errmsg);
			throw new SeleniumPlusException(errmsg);
		}

		return assignPair;
	}

	/**
	 * To detect if the page has changed or not.<br>
	 * For example, if user click on a link, or a Log-Out menu item, the page may probably change.<br>
	 * The current implementation is not guarantee!<br>
	 * @return boolean, if the page has changed.
	 */
	public static boolean pageHasChanged(){
		String url = null;
		boolean changed = false;

		try{
			//TODO Does "url change" mean the page has changed???
			//Not enough, there are some cases, url changed, but page doesn't. we need another way to detect.
			//TODO we need to wait the page completely-loaded so that we can get the new url.
			StringUtilities.sleep(2000);
			url = getWebDriver().getCurrentUrl();
			IndependantLog.debug(StringUtils.debugmsg(false)+" '"+url+"'='"+lastVisitedURL+"' ?");
			if(!url.equals(lastVisitedURL)) changed = true;
		}catch(Exception e){
			IndependantLog.debug(StringUtils.debugmsg(false)+"Met "+StringUtils.debugmsg(e));
		}

		return changed;
	}

	public static class SwitchFramesResults{
	    private boolean haveSwitchedFrames = false;
	    private List<String> rsWithoutFrames = new ArrayList<String>();
	    public SwitchFramesResults setSwitchedFrames(boolean switched){
	    	haveSwitchedFrames = switched;
	    	return this;
	    }
	    public boolean haveSwitchedFrames() {
			return haveSwitchedFrames;
		}
		public SwitchFramesResults setRsWithoutFrames(List<String> withoutFrames){
	    	rsWithoutFrames = withoutFrames;
	    	return this;
	    }
	}

	/**
	 * Initialize the {@link SearchObject#lastBrowserWindow}, it is important for calculating the element location on screen.<br>
	 * If we failed to create it then we will try to connect to current session from session file.
	 * This may change the {@link SearchObject#lastUsedWD} and {@link SearchObject#lastVisitedURL}.<br>
	 * <b>NOTE: ONLY the top most window is useful to us. Please call getWebDriver().switchTo().defaultContent() before calling this method.
	 *          According to <a href="https://www.w3schools.com/jsref/obj_window.asp">javascript window</a>,
	 *          If a document contain frames, the browser creates one window object for the HTML document,
	 *          and one additional window object for each frame.
	 *          If we are NOT in the topmost document, this method will NOT change the field {@link SearchObject#lastBrowserWindow}</b>
	 *
	 * @return WebDriver
	 * @throws SeleniumPlusException
	 */
	public static WebDriver initializeBrowserWindow() throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		WebDriver webdriver = getWebDriver();

		boolean done = false;
		boolean retried = false;
		while(!done){
			//Get information about the browser window by "javascript code",
			//as Selenium window object doesn't provide enough information.
			Window window = webdriver.manage().window();
			Object windowObject = BrowserWindow.getWindowObjectByJS(window);
			if(windowObject != null){
				try{
					IndependantLog.warn(debugmsg +" DOM window Object retrieved successfully.");
					Object currentFrame = _getCurrentFrame(webdriver);
					if(currentFrame!=null){
						//we ONLY need the window info of the top most document, S1536070.
						IndependantLog.warn(debugmsg +" skip DOM window information, we are not in the top most document!");
					}else{
						lastBrowserWindow = new BrowserWindow(windowObject);
						IndependantLog.debug(debugmsg +" Evaluating DOM window information...");
					}
				}catch(Exception e){
					IndependantLog.warn(debugmsg+e.getMessage());
					if(lastBrowserWindow==null){
						IndependantLog.error(debugmsg +" lastBrowserWindow is null, create a default BrowserWindow.");
						lastBrowserWindow = new BrowserWindow();
					}else{
						IndependantLog.debug(debugmsg +"Retaining the last Browser window object instead.");
					}
				}
				done = true;
			}else{
				// Carl Nagle -- resolving problems in IE after "Login"
				// DEBUG Experimental -- IE not working the same as other browsers
				IndependantLog.debug(debugmsg +" DOM window Object could not be retrieved.");
				if(!retried){
					IndependantLog.debug(debugmsg +" retrying 'current' session from the session file ...");
					webdriver = reconnectLastWebDriver();

					IndependantLog.debug(debugmsg +" changing the last visited URL from '"+lastVisitedURL+"' to '"+webdriver.getCurrentUrl()+"'");
					lastVisitedURL = webdriver.getCurrentUrl();

					if(! getBypassFramesReset()) webdriver.switchTo().defaultContent();
					retried = true;
				}else{
					IndependantLog.debug(debugmsg +" retrying a new targetLocator FAILED.");
					done = true;
					throw new SeleniumPlusException("Suspected UnreachableBrowserException seeking Browser DOM window Object.");
				}
			}
		}

		return webdriver;
	}

	/**
	 * Before doing any component search, we make sure we are in the correct frame.<br>
	 * Frame information may or may not be within the provided recognition string.<br><br>
	 *
	 * This method will probably change the field {@link #lastBrowserWindow}<br>
	 * This method will probably change the filed {@link #lastFrame}<br>
	 * <b>NOTE: If {@link #getBypassFramesReset()} is true, we only strip off the frame recognition string and will not change frame context.</b>
	 *
	 * @param recognitionString String, the recognition string with/without frame information.
	 *
	 * @return SwitchFramesResults, contains the indicator if we have switched frame; and the normal recognition string to get element.
	 */
	protected static SwitchFramesResults _switchFrames(String recognitionString){
		String debugmsg = StringUtils.debugmsg(false);

		String[] st = StringUtils.getTokenArray(recognitionString, childSeparator, escapeChar);
		/* rsWithoutFrames: Recognition String may contain some Frame-RS, after handling the
		 * frames, these Frame-RS are no longer useful, so they will be removed and the rest
		 * RS will be kept in the list rsWithoutFrames
		 */
		List<String> rsWithoutFrames = new ArrayList<String>();
		boolean haveSwichedFrame = false;

		WebDriver webdriver = getWebDriver();
		//1. Handle the Frames Recognition String
		try{
			lastVisitedURL = webdriver.getCurrentUrl();
			IndependantLog.debug(debugmsg +" Before switching frame:\nOur 'lastFrame' is "+lastFrame+"\nThe current frame context is "+_getCurrentFrame(webdriver));
			if( ! getBypassFramesReset() ){
				//very IMPORTANT step: Switch back to the top window
				webdriver.switchTo().defaultContent();
				IndependantLog.debug(debugmsg +" reset the frame context to the top document. ");
			}else{
				IndependantLog.debug(debugmsg +" bypassed 'frame-reset', we are probably not in the top document. ");
			}

			webdriver = initializeBrowserWindow();

			//reset the frame before searching the frame element if it exists.
			FrameElement frameElement = null;
			WebElement frame = null;
			String frameRS = null;
			//search the frame element if RS contains frame-info
			for (String rst : st) {
				//pre-check if this RS contains anything about frame-RS
				frameRS = retrieveFrameRS(rst);

				if(frameRS!=null){
					//If getBypassFramesReset() is true, we will ignore the frame parts in the recognition string
					if(!getBypassFramesReset()){
						//Get frame WebElement according to frame's recognition string
						frame = _getSwitchFrame(webdriver, frameRS);
						if(frame!=null){
							frameElement = new FrameElement(frameElement, frame);
							haveSwichedFrame = _switchFrame(webdriver, frame);
							//Can we use frame as SearchContext for child frame? FrameID=parentFrame;\;FrameID=childFrame
							//NO, frame WebElement can NOT be used as SearchContext, will cause Exception
							//We should always use webdriver as the SearchContext to find frame WebElement

							//don't break, if there is frame in frame, as FRAMENAME=parent;\\;FRAMENAME=child
						}else{
							IndependantLog.warn(debugmsg+" cannot find the frame webelement according to RS '"+frameRS+"'.");
						}
					}
				}else{
					//IndependantLog.warn(debugmsg+" store normal recognition string '"+rst+"' for further processing.");
					rsWithoutFrames.add(rst);
				}
			}

			if(haveSwichedFrame){
				lastFrame = frameElement;
				IndependantLog.debug(debugmsg+" CHANGED the 'lastFrame' to "+lastFrame);
			}

		}catch(Exception e){
			StringBuilder errorMsg = new StringBuilder("\nFailed to switch frame for '"+recognitionString + "'");
			String pageContent = null;
			if(webdriver!=null){
				try {
					//webdriver.getPageSource() will only return the content of document in current frame, NOT the whole html page.
					//pageContent = webdriver.getPageSource();
					String url = webdriver.getCurrentUrl();
					pageContent = NetUtilities.readHttpURL(new URL(url), StringUtils.CHARSET_UTF8 /*what page encoding to use*/, 5000/*5 seconds enough?*/);
					if(pageContent!=null){
						errorMsg.append("on page");
						errorMsg.append("\n============ '"+url+"' ======================\n"+pageContent);
						errorMsg.append("\n======================================================\n");
					}
				} catch (Exception e1) {
				}
			}
			errorMsg.append("Met exception "+e.toString());
			IndependantLog.error(debugmsg+errorMsg);
		}

		//2. Before search an element, switch to the correct frame
		//   For the child component, we don't need to specify the frame-RS, they use the same frame-RS as
		//   their parent component.
		try{
			//If we bypass the 'frame reset', then user will handle the frame-switch by himself.
			if( !getBypassFramesReset() && !haveSwichedFrame && webdriver.switchTo()!=null){
				haveSwichedFrame = _switchByFrameElement(webdriver, lastFrame);
			}
			IndependantLog.debug(debugmsg +" After switching frame:\nOur 'lastFrame' is "+lastFrame+"\nThe current frame context is "+_getCurrentFrame(webdriver));
		}catch(Exception e){
			if(e instanceof StaleElementReferenceException && pageHasChanged()){
				IndependantLog.warn(debugmsg+" switching to the previous frame 'lastFrame' failed! The page has changed!");
				//Lei Wang: S1215754
				//if we click a link within a FRAME, as the link is in a FRAME, so the field 'lastFrame' will be assigned after clicking the link.
				//then the link will lead us to a second page, when we try to find something on that page, firstly we try to switch to 'lastFrame' and
				//get a StaleElementReferenceException (as we are in the second page, there is no such frame of the first page)
				//but we still want our program to find the web-element on the second page, so we will just set 'lastFrame' to null and let program continue.
//				[FirstPage]
//				FirstPage="FRAMEID=iframeResult"
//				Link="xpath=/html/body/a"
//
//				[SecondPage]
//				SecondPage="xpath=/html"
//				return null;
			}else{
				IndependantLog.warn(debugmsg+" switching to the previous frame 'lastFrame' failed! Met exception "+StringUtils.debugmsg(e));
			}
			//Lei Wang: S1215778
			//If we fail to switch to 'lastFrame', which perhaps means that it is not useful anymore
			//we should reset it to null, otherwise it will cause errors, such as calculate the element's screen location
			IndependantLog.debug(debugmsg+" 'lastFrame' is not useful anymore, reset it to null.");
			lastFrame = null;
		}
		return new SwitchFramesResults().setRsWithoutFrames(rsWithoutFrames).setSwitchedFrames(haveSwichedFrame);
	}

	/**
	 * Switch frame context.
	 * @param webdriver WebDriver
	 * @param frameElement FrameElement, the frame to be switched to.
	 * @return boolean true if successfully switched.
	 */
	protected static boolean _switchByFrameElement(WebDriver webdriver, FrameElement frameElement){
		boolean switched = false;

		try{
			Stack<FrameElement> frameStack = new Stack<FrameElement>();
			while(frameElement!=null){
				frameStack.push(frameElement);
				frameElement = frameElement.getParentFrame();
			}
			while(!frameStack.isEmpty() && frameStack.peek()!=null){
				webdriver.switchTo().frame(frameStack.pop().getWebElement());
				switched = true;
			}
		}catch(Exception e){
			IndependantLog.warn(StringUtils.debugmsg(false)+" failed, due to "+e.toString());
			switched = false;
		}

		return switched;
	}

	/**
	 * Reset the frame context to 'lastFrame' if the current frame context is different than 'lastFrame'.<br>
	 * <b>NOTE:</b> Please turn on the field {@link #checkCurrentFrameContext} before calling this method.<br>
	 * @return boolean true if we did reset the frame to 'lastFrame'
	 *
	 * @see #checkCurrentFrameContext
	 */
	public static boolean resetLastFrame(){
		boolean reset = false;
		boolean isEdge44 = false;
		String debugmsg = StringUtils.debugmsg(false);
		WebDriver webdriver = getWebDriver();

		if(webdriver instanceof RemoteWebDriver){
			RemoteWebDriver rwd = (RemoteWebDriver) webdriver;
			Capabilities cap = rwd.getCapabilities();
			if(SelectBrowser.BROWSER_NAME_EDGE.equalsIgnoreCase(cap.getBrowserName()) ){
				String version = rwd.getCapabilities().getVersion();
				if(StringUtils.isValid(version) && version.trim().startsWith("44")){
					isEdge44 = true;
					IndependantLog.debug(debugmsg+" testing with Edge 44, there is a bug losting frame context accidentally, we forced checking the current frame.");
				}
			}
		}

		if(lastFrame!=null && (checkCurrentFrameContext || isEdge44)){
			try {
				Object currentFrame = _getCurrentFrame();
				//TODO do we need to check if the currentFrame is not null, but it is different than the 'lastFrame'?
				if(currentFrame==null){
					IndependantLog.debug(debugmsg+" For some reason, we lost frame context, we are going to reset it to "+lastFrame);
					reset = _switchByFrameElement(webdriver, lastFrame);
				}
			} catch (SeleniumPlusException e) {
				IndependantLog.error(debugmsg+" failed, Met "+e.toString());
			}
		}

		return reset;
	}

	/**
	 * Primary entry point to seek a WebElement based on a provided recognition string.
	 * We support multiple types of recognition strings
	 * @param sc SearchContext, could be the WebDriver or a parent WebElement context.
	 * @param recognitionString String, The recognition string used to identify the sought element.
	 * @return WebElement found or null if not found.
	 */
	public static WebElement getObject(SearchContext sc, String recognitionString){

		String debugmsg = StringUtils.debugmsg(SearchObject.class, "getObject");
		IndependantLog.debug(debugmsg +"using SearchContext="+sc+" to find RS='"+ recognitionString+"'");

		if(sc==null){
			IndependantLog.error("SearchContext is null, cannot continue search!!!");
			return null;
		}

		SwitchFramesResults sfr =_switchFrames(recognitionString);

		SearchContext wel = sc;

		//3. Lei Wang, FIX defect S1138290
		//Error 1: Map doesn't contain a window definition (under a frame)
		//  [Window]
		//  Child="FrameId=xxx;\;Id=xxx"
		//If we are going to FIND element under certain FRAME,
		//and the parent is "[[RemoteDriver.... ] ->  xpath: /html]", which maybe the default parent "xpath=/html" without any FRAME;
		//the webelement to find is in a Frame, StaleElementReferenceException will be thrown out.
		//To avoid this, we need to modify the SearchContext to the "default-webdriver"
		if(sfr.haveSwitchedFrames && XPATH.isRootHtml(wel)){
			wel = getWebDriver();
			IndependantLog.debug(debugmsg+"Replace default SearchContext '/html' by WebDriver.");
		}
		//Error 2: Window definition ONLY has information about FRAME, no child information
		//  [Window]
		//  Window="FrameId=xxx"
		//  Child="FrameId=xxx;\;Id=xxx"
		//If 'recognition string' contains only frame information, rsWithoutFrames will be empty, and this method will
		//return null (ClassCastException from WebDriver to WebElement); to avoid this set "xpath=/html" as the default object
		if(sfr.rsWithoutFrames.isEmpty()) sfr.rsWithoutFrames.add(RS.xpath(XPATH.html()));

		//4. Search the target element within a frame (if exist) level by level, by handling the normal RS
		String[] st = sfr.rsWithoutFrames.toArray(new String[0]);//The normal RS without the frame-RS
		String[] firstQualifierPair = null;
		List<String> prefixes = new ArrayList<String>();
		boolean isPASM = false;
		String searchCriteria = null;
		String value = null;
		for (String rst : st) {
			try{
				prefixes.clear();
				rst = GuiObjectVector.removeRStringPrefixes(rst, prefixes);
				isPASM = GuiObjectVector.isPASMMode(prefixes);

				//rst MUST be in format "xxx=yyy", otherwise it is considered as invalid
				firstQualifierPair = getFirstQualifierPair(rst);
				searchCriteria = firstQualifierPair[0];
				value = firstQualifierPair[1];

				IndependantLog.debug(debugmsg+"qualifier='"+searchCriteria+"' value='"+value+"' isPASM="+isPASM);

				if ( SEARCH_CRITERIA_TYPE.equalsIgnoreCase(searchCriteria)){
					wel = getObjectForDomain(wel, rst, isPASM);

				}else if(SEARCH_CRITERIA_XPATH.equalsIgnoreCase(searchCriteria)){
					IndependantLog.debug(debugmsg+" searching xpath '"+value+"'");
					wel = getObjectByQualifier(wel, searchCriteria, value);

				}else if(SEARCH_CRITERIA_CSS.equalsIgnoreCase(searchCriteria)){
					IndependantLog.debug(debugmsg+" searching css '"+value+"'");
					wel = getObjectByQualifier(wel, searchCriteria, value);

				}else if (rst.contains(qulifierSeparator)){//Multiple Attributes and Qualifiers
					wel = getObjectByMultiAttributes(wel, rst, isPASM);

				} else {
					try{
						//Only ONE qualifier pair, qualifier=value
						String[] tokens = StringUtils.getTokenArray(rst, assignSeparator, escapeChar);
						wel = getObjectByQualifier(wel, tokens[0], tokens[1]);
					}catch(SeleniumPlusException se){
						IndependantLog.warn(debugmsg+se.getMessage());
						continue;
					}
				}
			}catch(NoSuchElementException nse){
				wel=null;
				IndependantLog.debug(debugmsg+" NoSuchElementException for '"+ rst +"'.");
			}catch(Throwable t){
				//wel is NOT null, it keeps the original value.
				//Do we permit to continue the search? Maybe we should stop.
				wel = null;
				IndependantLog.debug(debugmsg+" Met Exception "+StringUtils.debugmsg(t));
			}

			if (wel == null){
				IndependantLog.error(debugmsg+" cannot find child with RS '"+rst+"'");
				break;
			}
		}

		try{
			IndependantLog.debug(debugmsg +" Just before returning found web-element:\nOur 'lastFrame' is "+lastFrame+"\nThe current frame context is "+_getCurrentFrame());
		}catch(Exception x){
		}

		try{ return (WebElement) wel; }catch(Exception x){
			IndependantLog.error("Could not retrun the found element, due to "+x.toString());
			return null;
		}
	}

	/**
	 * Primary entry point to seek all WebElements matching a provided recognition string.
	 * We support multiple types of recognition strings
	 * @param sc SearchContext, could be the WebDriver or a parent WebElement context.
	 * @param recognitionString String, The recognition string used to identify the matching elements.
	 * @return List&lt;WebElement> with 0 or more WebElement entries.
	 */
	public static List<WebElement> getObjects(SearchContext sc, String recognitionString){

		String debugmsg = StringUtils.debugmsg(SearchObject.class, "getObjects");
		IndependantLog.debug(debugmsg +"using SearchContext="+sc+" to find RS='"+ recognitionString+"'");

		List<WebElement> list = new ArrayList<WebElement>();

		if(sc==null){
			IndependantLog.error("SearchContext is null, cannot continue search!!!");
			return list;
		}

		SearchContext wel = sc;
		SwitchFramesResults sfr =_switchFrames(recognitionString);

		//3. Lei Wang, FIX defect S1138290
		//Error 1: Map doesn't contain a window definition (under a frame)
		//  [Window]
		//  Child="FrameId=xxx;\;Id=xxx"
		//If we are going to FIND element under certain FRAME,
		//and the parent is "[[RemoteDriver.... ] ->  xpath: /html]", which maybe the default parent "xpath=/html" without any FRAME;
		//the webelement to find is in a Frame, StaleElementReferenceException will be thrown out.
		//To avoid this, we need to modify the SearchContext to the "default-webdriver"
		if(sfr.haveSwitchedFrames && XPATH.isRootHtml(wel)){
			wel = getWebDriver();
			IndependantLog.debug(debugmsg+"Replace default SearchContext '/html' by WebDriver.");
		}
		//Error 2: Window definition ONLY has information about FRAME, no child information
		//  [Window]
		//  Window="FrameId=xxx"
		//  Child="FrameId=xxx;\;Id=xxx"
		//If 'recognition string' contains only frame information, rsWithoutFrames will be empty, and this method will
		//return null (ClassCastException from WebDriver to WebElement); to avoid this set "xpath=/html" as the default object
		if(sfr.rsWithoutFrames.isEmpty()) sfr.rsWithoutFrames.add(RS.xpath(XPATH.html()));

		//4. Search the target element within a frame (if exist) level by level, by handling the normal RS
		String[] st = sfr.rsWithoutFrames.toArray(new String[0]);//The normal RS without the frame-RS
		String[] firstQualifierPair = null;
		List<String> prefixes = new ArrayList<String>();
		boolean isPASM = false;
		String searchCriteria = null;
		String value = null;
		String rst = null;
		boolean isLastRS = false;
		for (int i=0;i<st.length;i++) {
			try{
				prefixes.clear();
				rst = GuiObjectVector.removeRStringPrefixes(st[i], prefixes);
				isPASM = GuiObjectVector.isPASMMode(prefixes);

				//rst MUST be in format "xxx=yyy", otherwise it is considered as invalid
				firstQualifierPair = getFirstQualifierPair(rst);
				searchCriteria = firstQualifierPair[0];
				value = firstQualifierPair[1];
				isLastRS = (i == st.length-1);

				IndependantLog.debug(debugmsg+"qualifier='"+searchCriteria+"' value='"+value+"' isPASM="+isPASM);

				if ( SEARCH_CRITERIA_TYPE.equalsIgnoreCase(searchCriteria)){
					if(isLastRS){
						list = getObjectsForDomain(wel, rst, isPASM);
					}else{
						wel = getObjectForDomain(wel, rst, isPASM);
					}
				}else if(SEARCH_CRITERIA_XPATH.equalsIgnoreCase(searchCriteria)){
					IndependantLog.debug(debugmsg+" searching xpath '"+value+"'");
					if(isLastRS){
					    list = getObjectsByQualifier(wel, searchCriteria, value);
					}else{
					    wel = getObjectByQualifier(wel, searchCriteria, value);
					}
				}else if(SEARCH_CRITERIA_CSS.equalsIgnoreCase(searchCriteria)){
					IndependantLog.debug(debugmsg+" searching css '"+value+"'");
					if(isLastRS){
					    list = getObjectsByQualifier(wel, searchCriteria, value);
					}else{
					    wel = getObjectByQualifier(wel, searchCriteria, value);
					}
				}else if (rst.contains(qulifierSeparator)){//Multiple Attributes and Qualifiers
					if(isLastRS){
						list = getObjectsByMultiAttributes(wel, rst, isPASM);
					}else{
						wel = getObjectByMultiAttributes(wel, rst, isPASM);
					}
				} else {
					try{
						//Only ONE qualifier pair, qualifier=value
						String[] tokens = StringUtils.getTokenArray(rst, assignSeparator, escapeChar);
						if(isLastRS){
							list = getObjectsByQualifier(wel, tokens[0], tokens[1]);
						}else{
							wel = getObjectByQualifier(wel, tokens[0], tokens[1]);
						}
					}catch(SeleniumPlusException se){
						IndependantLog.warn(debugmsg+se.getMessage());
						continue;
					}
				}
			}catch(NoSuchElementException nse){
				wel=null;
				IndependantLog.debug(debugmsg+" NoSuchElementException for '"+ rst +"'.");
			}catch(Throwable t){
				//wel is NOT null, it keeps the original value.
				//Do we permit to continue the search? Maybe we should stop.
				wel = null;
				IndependantLog.debug(debugmsg+" Met Exception "+StringUtils.debugmsg(t));
			}

			if(isLastRS){
				if(list.isEmpty()){
					IndependantLog.error(debugmsg+" cannot find child with RS '"+rst+"'");
				}
			}else if(wel == null){
				IndependantLog.error(debugmsg+" cannot find child with RS '"+rst+"'");
				break;
			}
		}
		try{
			IndependantLog.debug(debugmsg +" Just before returning found web-elements:\nOur 'lastFrame' is "+lastFrame+"\nThe current frame context is "+_getCurrentFrame());
		}catch(Exception x){
		}

		return list;
	}

	public static boolean isFrameWebElement(WebElement frameTag){
		if(frameTag==null) return false;
		try{
			String tagName = frameTag.getTagName().toLowerCase();
			return TAG_FRAME.equals(tagName) || TAG_IFRAME.equals(tagName);
		}catch(Exception e){
			IndependantLog.warn(StringUtils.debugmsg(false)+"Met "+StringUtils.debugmsg(e));
			return false;
		}
	}

	/**
	 * Find the Nth frame webelement in the search context.<br>
	 * @param sc SearchContext, the search context, SHOULD be WebDriver
	 * @param index int, the index of the tag, 1-based
	 * @return WebElement, the webelement or null if not found
	 */
	protected static WebElement getFrameWebElement(SearchContext sc, int index){
		WebElement frame = null;
		if(frame==null) frame = getWebElement(sc, TAG_IFRAME , index);
		if(frame==null) frame = getWebElement(sc, TAG_FRAME , index);

		try{ if(frame==null) frame = sc.findElements(By.xpath("//"+TAG_IFRAME)).get(index-1); }catch(Exception ignore){}
		try{ if(frame==null) frame = sc.findElements(By.xpath("//"+TAG_FRAME)).get(index-1); }catch(Exception ignore){}

		return frame;
	}

	/**
	 * Find the Nth tag in the search context.<br>
	 * @param sc SearchContext, the search context
	 * @param tagName String, the tag name to find
	 * @param index int, the index of the tag, 1-based
	 * @return WebElement, the webelement or null if not found
	 */
	protected static WebElement getWebElement(SearchContext sc, String tagName, int index){
		WebElement result = null;
		try{
			//E[n] represents the nth child of element E
			//(E)[n] represents the nth E element
			result = sc.findElement(By.xpath("(//"+tagName+")["+index+"]"));
		}catch(Exception e){
			IndependantLog.error(StringUtils.debugmsg(false)+" met "+StringUtils.debugmsg(e));
		}
		return result;
	}

	protected static List<WebElement> findElements(SearchContext wel, String xpath){
		List<WebElement> preMatches = null;
		try{ preMatches = wel.findElements(By.xpath(xpath));}
		catch(InvalidSelectorException x){
			if(xpath.startsWith(".")){
				xpath = xpath.substring(1);
				preMatches = wel.findElements(By.xpath(xpath));
			}else{
				throw x;
			}
		}
		return preMatches;
	}

	/**
	 * @return element according to element's text or element's attribute 'value'.
	 *         Firstly we try to match with element's text; if not found we try to match with element's attribute 'value'.
	 */
	protected static SearchContext getObjectByText(SearchContext wel,String text, boolean partialMatch){
		List<WebElementWarpper> preMatches = getPreMatchedObjectsByText(wel, text, partialMatch);
		SearchContext result = getMatchedObject(preMatches, text, partialMatch);
		if(result==null){
			preMatches = getPreMatchedObjectsByAttributeValue(wel, text, partialMatch);
			result = getMatchedObject(preMatches, text, partialMatch);
		}
		return result;
	}

	/** @return 0 or more matched elements according to element's text and element's attribute 'value'. */
	protected static List<WebElement> getObjectsByText(SearchContext wel,String text, boolean partialMatch){
		List<WebElementWarpper> preMatches = getPreMatchedObjectsByText(wel, text, partialMatch);

		preMatches.addAll(getPreMatchedObjectsByAttributeValue(wel, text, partialMatch));

		return getMatchedObjects(preMatches, text, partialMatch);
	}

	/** @return 0 or more pre-matched elements according to element's text. */
	private static List<WebElementWarpper> getPreMatchedObjectsByText(SearchContext wel,String text, boolean partialMatch){
		String debugmsg = StringUtils.debugmsg(false);
		IndependantLog.debug(debugmsg +"using '"+ text+"', partialMatch="+partialMatch);

		List<WebElementWarpper> elements = new ArrayList<WebElementWarpper>();
		//xpath text()=xxx, for text node like <tag>xxx</tag>
		String xpath = XPATH.fromText(text, partialMatch, true);
		List<WebElement> preMatches = findElements(wel, xpath);
		for(WebElement item : preMatches) elements.add(new WebElementWarpper(item, getText(item)));

		return elements;
	}

	/** @return 0 or more pre-matched elements according to element's attribute 'value'. */
	private static List<WebElementWarpper> getPreMatchedObjectsByAttributeValue(SearchContext wel,String text, boolean partialMatch){
		String debugmsg = StringUtils.debugmsg(false);
		IndependantLog.debug(debugmsg +"using '"+ text+"', partialMatch="+partialMatch);

		List<WebElementWarpper> elements = new ArrayList<WebElementWarpper>();
		//xpath @value=, for tag like <input type="submit" name="Login" value="Sign in">
		String xpath = XPATH.fromAttribute("value", text, partialMatch, true);
		List<WebElement> preMatches = findElements(wel, xpath);
		for(WebElement item : preMatches) elements.add(new WebElementWarpper(item, getText(item)));

		return elements;
	}

	protected static SearchContext getObjectByTitle(SearchContext wel, String value, boolean partialMatch){
		String debugmsg = StringUtils.debugmsg(false);
		IndependantLog.debug(debugmsg +"using '"+ value+"', partialMatch="+partialMatch);
		String attribute = SEARCH_CRITERIA_TITLE.toLowerCase();

		String xpath = XPATH.fromAttribute(attribute, value, partialMatch, true);
		List<WebElement> preMatches = findElements(wel, xpath);

		List<WebElementWarpper> elements = new ArrayList<WebElementWarpper>();
		for(WebElement item : preMatches) elements.add(new WebElementWarpper(item, item.getAttribute(attribute)));

		return getMatchedObject(elements, value, partialMatch);
	}

	/** @return 0 or more matched elements */
	protected static List<WebElement> getObjectsByTitle(SearchContext wel, String value, boolean partialMatch){
		String debugmsg = StringUtils.debugmsg(false);
		IndependantLog.debug(debugmsg +"using '"+ value+"', partialMatch="+partialMatch);
		String attribute = SEARCH_CRITERIA_TITLE.toLowerCase();

		String xpath = XPATH.fromAttribute(attribute, value, partialMatch, true);
		List<WebElement> preMatches = findElements(wel, xpath);

		List<WebElementWarpper> elements = new ArrayList<WebElementWarpper>();
		for(WebElement item : preMatches) elements.add(new WebElementWarpper(item, item.getAttribute(attribute)));

		return getMatchedObjects(elements, value, partialMatch);
	}

	/**
	 * @param elements List&lt;WebElementWarpper>, contains WebElement to match
	 * @param value String, the value to match with
	 * @param partialMatch boolean, if we try to find the partial matched item
	 * @return SearchContext, the matched object
	 */
	protected static SearchContext getMatchedObject(List<WebElementWarpper> elements, String value, boolean partialMatch){
		if(partialMatch){
			//try to find the partial matched item
			for(WebElementWarpper element: elements){
				if(element.value.contains(value)) return element.element;
			}
			//try to find the partial matched item ignoring case
			for(WebElementWarpper element: elements){
				if(element.value.toLowerCase().contains(value.toLowerCase())) return element.element;
			}
		}else{
			//try to find the exact matched item
			for(WebElementWarpper element: elements){
				if(element.value.equals(value)) return element.element;
			}
			//try to find the exact matched item ignoring case
			for(WebElementWarpper element: elements){
				if(element.value.equalsIgnoreCase(value)) return element.element;
			}
		}
		//finally, return the first element if it exists
		if(elements.size()>0) return elements.get(0).element;

		return null;
	}

	/**
	 * @param elements List&lt;WebElementWarpper>, contains WebElements to match
	 * @param value String, the value to match with
	 * @param partialMatch boolean, if we try to find the partial matched item
	 * @return List&lt;WebElement> 0 or more matched WebElements.
	 */
	protected static List<WebElement> getMatchedObjects(List<WebElementWarpper> elements, String value, boolean partialMatch){
		List<WebElement> list = new ArrayList<WebElement>();
		if(partialMatch){
			//try to find the partial matched item
			for(WebElementWarpper element: elements){
				if(element.value.contains(value) || element.value.toLowerCase().contains(value.toLowerCase())) {
					list.add( element.element);
				}
			}
		}else{
			//try to find the exact matched items
			for(WebElementWarpper element: elements){
				if(element.value.equals(value) || element.value.equalsIgnoreCase(value)) {
					list.add(element.element);
				}
			}
		}
		//finally, return the list of 0 or more matched objects
		return list;
	}

	/**
	 * This class contains 2 fields, one is WebElement, the other is value.<br>
	 * The field value is a string, used to match. It can be assigned by WebElement.getText(),<br>
	 * WebElement.getAttribute("title") or some other text value of the WebElement, which can<br>
	 * be used to match with a text value.<br>
	 *@see SearchObject#getMatchedObject(List, String)
	 */
	private static class WebElementWarpper{
		public WebElement element = null;
		public String value = null;
		public WebElementWarpper(WebElement element, String value){
			this.element = element;
			this.value = value;
		}
	}

	/**
	 * According to pair(qualifier, value), find the matched WebElement.<br>
	 * Some qualifiers like "ID", can determine the WebElement uniquely and result<br>
	 * only one WebElement; Other qualifiers may result several WebElement, the first<br>
	 * one will be returned.<br>
	 *
	 * @param sc SearchContext, the search context, it is normally a WebElement.
	 * @param qualifier String, the qualifier like xpath, css, id, name, link etc.
	 * @param value String, the value of a qualifier.
	 * @return SearchContext, the WebElment found according to qualifier and its value.
	 * @throws SeleniumPlusException
	 */
	protected static SearchContext getObjectByQualifier(SearchContext sc, String qualifier, String value) throws SeleniumPlusException{
		SearchContext result = null;
		String qualifierUC = null;
		String debugmsg = StringUtils.debugmsg(false);

		if(qualifier==null){
			throw new SeleniumPlusException("ignore null qualifier.");
		}else{
			qualifierUC = qualifier.toUpperCase();
		}

		try{

			if(SEARCH_CRITERIA_XPATH.equals(qualifierUC)){
				result = sc.findElement(By.xpath(value));

			}else if(SEARCH_CRITERIA_CSS.equals(qualifierUC)){
				By by = null;
				if(CSS.isDeprecated(value)){
					IndependantLog.warn(debugmsg+" the css selector '"+value+"' is deprecated!");
					by = CSS.convert(value);
				}
				if(by==null){
					by = By.cssSelector(value);
				}
				result = sc.findElement(by);

			}else if(SEARCH_CRITERIA_TAG.equals(qualifierUC)){
				result = sc.findElement(By.tagName(value));
			}else{
				boolean partialMatch = qualifierUC.endsWith(SEARCH_CRITERIA_CONTAINS_SUFFIX);

				//The following qualifiers will support suffix "Contains":
				//idContains, classContains, nameContains, linkContains, textContains, titleContains, iframeidContains
				if(SEARCH_CRITERIA_ID.equals(qualifierUC)){
					result = sc.findElement(By.id(value));

				}else if(SEARCH_CRITERIA_CLASS.equals(qualifierUC)){
					result = sc.findElement(By.className(value));

				}else if(SEARCH_CRITERIA_NAME.equals(qualifierUC)){
					result = sc.findElement(By.name(value));

				}else if(SEARCH_CRITERIA_LINK.equals(qualifierUC)){
					result = sc.findElement(By.linkText(value));

				}else if(SEARCH_CRITERIA_PARTIALLINK.equals(qualifierUC)||
						(SEARCH_CRITERIA_LINK+SEARCH_CRITERIA_CONTAINS_SUFFIX).equals(qualifierUC)){
					result = sc.findElement(By.partialLinkText(value));

				}else if(qualifierUC.startsWith(SEARCH_CRITERIA_TEXT)){
					result = getObjectByText(sc, value, partialMatch);

				}else if(qualifierUC.startsWith(SEARCH_CRITERIA_TITLE)){
					result = getObjectByTitle(sc, value, partialMatch);

				}else if(qualifierUC.startsWith(SEARCH_CRITERIA_IFRAMEID)){
					result = sc.findElement(By.xpath("//iframe["+XPATH.condition("id", value, partialMatch)+"]"));

				}else if(partialMatch){
					//idContains, classContains, nameContains will be supported here.
					result = sc.findElement(By.xpath(XPATH.RELATIVE_MATCHING_ALL_START+XPATH.conditionContains(qualifier, value)+XPATH.END));

				}else{
					throw new SeleniumPlusException("ignore unknown qualifier '"+qualifierUC+"', value='"+value+"'");
				}
			}
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException) e;
			IndependantLog.error(StringUtils.debugmsg(false)+" met "+StringUtils.debugmsg(e));
		}

		return result;
	}

	/**
	 * According to pair(qualifier, value), find all matching WebElements.<br>
	 * Some qualifiers like "ID", can determine the WebElement uniquely and result<br>
	 * only one WebElement; Other qualifiers may result in several WebElements.<br>
	 *
	 * @param sc SearchContext, the search context, it is normally a WebElement.
	 * @param qualifier String, the qualifier like xpath, css, id, name, link etc.
	 * @param value String, the value of a qualifier.
	 * @return List&lt;WebElement> 0 or more
	 * @throws SeleniumPlusException
	 */
	protected static List<WebElement> getObjectsByQualifier(SearchContext sc, String qualifier, String value) throws SeleniumPlusException{
		List<WebElement> result = new ArrayList<WebElement>();;
		String qualifierUC = null;

		if(qualifier==null){
			throw new SeleniumPlusException("ignore null qualifier.");
		}else{
			qualifierUC = qualifier.toUpperCase();
		}

		try{

			if(SEARCH_CRITERIA_XPATH.equals(qualifierUC)){
				result = sc.findElements(By.xpath(value));

			}else if(SEARCH_CRITERIA_CSS.equals(qualifierUC)){
				result = sc.findElements(By.cssSelector(value));

			}else if(SEARCH_CRITERIA_TAG.equals(qualifierUC)){
				result = sc.findElements(By.tagName(value));
			}else{
				boolean partialMatch = qualifierUC.endsWith(SEARCH_CRITERIA_CONTAINS_SUFFIX);

				//The following qualifiers will support suffix "Contains":
				//idContains, classContains, nameContains, linkContains, textContains, titleContains, iframeidContains
				if(SEARCH_CRITERIA_ID.equals(qualifierUC)){
					result = sc.findElements(By.id(value));

				}else if(SEARCH_CRITERIA_CLASS.equals(qualifierUC)){
					result = sc.findElements(By.className(value));

				}else if(SEARCH_CRITERIA_NAME.equals(qualifierUC)){
					result = sc.findElements(By.name(value));

				}else if(SEARCH_CRITERIA_LINK.equals(qualifierUC)){
					result = sc.findElements(By.linkText(value));

				}else if(SEARCH_CRITERIA_PARTIALLINK.equals(qualifierUC)||
						(SEARCH_CRITERIA_LINK+SEARCH_CRITERIA_CONTAINS_SUFFIX).equals(qualifierUC)){
					result = sc.findElements(By.partialLinkText(value));

				}else if(qualifierUC.startsWith(SEARCH_CRITERIA_TEXT)){
					result = getObjectsByText(sc, value, partialMatch);

				}else if(qualifierUC.startsWith(SEARCH_CRITERIA_TITLE)){
					result = getObjectsByTitle(sc, value, partialMatch);

				}else if(qualifierUC.startsWith(SEARCH_CRITERIA_IFRAMEID)){
					result = sc.findElements(By.xpath("//iframe["+XPATH.condition("id", value, partialMatch)+"]"));

				}else if(partialMatch){
					//idContains, classContains, nameContains will be supported here.
					result = sc.findElements(By.xpath(XPATH.RELATIVE_MATCHING_ALL_START+XPATH.conditionContains(qualifier, value)+XPATH.END));

				}else{
					throw new SeleniumPlusException("ignore unknown qualifier '"+qualifierUC+"', value='"+value+"'");
				}
			}
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException) e;
			IndependantLog.error(StringUtils.debugmsg(false)+" met "+StringUtils.debugmsg(e));
		}

		return result;
	}

	/**
	 * <pre>
	 * Builds an XPath search string using multiple attributes provided in the recognition string
	 * then performs a findElement(By.xpath(string));
	 * If the recognition string contains special attributes, How to tell them from normal attributes???
	 *
	 * </pre>
	 * @param sc SearchContext, the search context
	 * @param RS String, the recognition string
	 * @param isPASM boolean, if true, each part of 'recognition string' will be treated as property.<br>
	 *                        if false, some reserved words will be treated as qualifiers, but not properties.<br>
	 *                        the reserved words are {@link #SEARCH_CRITERIA_ITEMINDEX}, {@link #SEARCH_CRITERIA_PATH} etc.<br>
	 * @return SearchContext (WebElement or WebDriver)
	 */
	protected static SearchContext getObjectByMultiAttributes(SearchContext sc, String RS, boolean isPASM){

		SearchContext obj = null;
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "getObjectByMultiAttri");
		IndependantLog.debug(debugmsg +"using recognition string: "+ RS);

		int count = 0;
		String[] st = StringUtils.getTokenArray(RS, qulifierSeparator, escapeChar);

		HashMap<String, String> qualifiers = new HashMap<String, String>();

		//
		// It does NOT look like we currently handle "qual=value;\;qual=value" syntax
		//

		try{
			String xpathStr = (!containTagRS(RS)) ?
					          XPATH.RELATIVE_MATCHING_ALL_START :
	                          XPATH.RELATIVE_MATCHING_TAG_START(getTagQualifierValue(RS));

			String[] props = null;
			String property = null;
			String value = null;
			String xpathCondition = null;
			for (String prop: st){
				props = StringUtils.getTokenArray(prop, assignSeparator, escapeChar);
				++count;
				property = props[0];
				value = props[1];

				// skip TAG if encountered. We already handled it.
				if(SEARCH_CRITERIA_TAG.equalsIgnoreCase(property)){
					continue;
				}

				if(isPASM){
					//all are properties
					xpathStr += XPATH.condition(property, value, false);
				}else{
					//handle some 'reserved qualifiers'
					if(  SEARCH_CRITERIA_ITEMINDEX.equalsIgnoreCase(property)
							|| SEARCH_CRITERIA_PATH.equalsIgnoreCase(property)
							|| SEARCH_CRITERIA_INDEX.equalsIgnoreCase(property)
							){
						qualifiers.put(property.toUpperCase(), value);
						continue;

					}else if (SEARCH_CRITERIA_PROPERTY.equalsIgnoreCase(property)){
						IndependantLog.debug(debugmsg +"add search condition '"+value+"'");
						xpathCondition = getXpathCondition(value, false);
						if(xpathCondition!=null) xpathStr += xpathCondition;

					}else if (SEARCH_CRITERIA_PROPERTY_CONTAINS.equalsIgnoreCase(property)){
						IndependantLog.debug(debugmsg +"add search condition '"+value+"'");
						xpathCondition = getXpathCondition(value, true);
						if(xpathCondition!=null) xpathStr += xpathCondition;

					}else if (SEARCH_CRITERIA_TEXT.equalsIgnoreCase(property)){
						xpathCondition = XPATH.conditionForText(value, false);
						if(xpathCondition!=null) xpathStr += xpathCondition;

					}else if ((SEARCH_CRITERIA_TEXT+SEARCH_CRITERIA_CONTAINS_SUFFIX).equalsIgnoreCase(property)){
						xpathCondition = XPATH.conditionForText(value, true);
						if(xpathCondition!=null) xpathStr += xpathCondition;

					}// try to generically handle any <something>Contains property values not handled above
					else if (property.toUpperCase().endsWith(SEARCH_CRITERIA_CONTAINS_SUFFIX)){
						//TextContains should be handled differently to get the xpath
						xpathStr += XPATH.conditionContains(property, value);
					}else{
						xpathStr += XPATH.condition(property, value, false);
					}
				}

				if (st.length != count) xpathStr += " "+XPATH.AND+" ";
			}
			//Remove the last "and", if there is no search-condition after that "and"
			if(xpathStr.trim().endsWith(XPATH.AND)){
				xpathStr = xpathStr.substring(0, xpathStr.lastIndexOf(XPATH.AND));
			}
			xpathStr += XPATH.END;

			obj = getObjectByXpathAndQualifiers(sc, xpathStr, qualifiers);

		}catch(Throwable t){
			IndependantLog.debug(debugmsg +"recognition string parsing error: "+ t.getClass().getSimpleName()+": "+t.getMessage());
		}

		return obj;
	}

	/**
	 * <pre>
	 * Builds an XPath search string using multiple attributes provided in the recognition string
	 * then performs a findElements(By.xpath(string));
	 * If the recognition string contains special attributes, How to tell them from normal attributes???
	 *
	 * </pre>
	 * @param sc SearchContext, the search context
	 * @param RS String, the recognition string
	 * @param isPASM boolean, if true, each part of 'recognition string' will be treated as property.<br>
	 *                        if false, some reserved words will be treated as qualifiers, but not properties.<br>
	 *                        the reserved words are {@link #SEARCH_CRITERIA_ITEMINDEX}, {@link #SEARCH_CRITERIA_PATH} etc.<br>
	 * @return List&lt;WebElement> with 0 or more matching elements.
	 */
	protected static List<WebElement> getObjectsByMultiAttributes(SearchContext sc, String RS, boolean isPASM){

		List<WebElement> list = new ArrayList<WebElement>();
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "getObjectByMultiAttri");
		IndependantLog.debug(debugmsg +"using recognition string: "+ RS);

		int count = 0;
		String[] st = StringUtils.getTokenArray(RS, qulifierSeparator, escapeChar);

		HashMap<String, String> qualifiers = new HashMap<String, String>();

		//
		// It does NOT look like we currently handle "qual=value;\;qual=value" syntax
		//

		try{
			String xpathStr = (!containTagRS(RS)) ?
					          XPATH.RELATIVE_MATCHING_ALL_START :
	                          XPATH.RELATIVE_MATCHING_TAG_START(getTagQualifierValue(RS));

			String[] props = null;
			String property = null;
			String value = null;
			String xpathCondition = null;
			for (String prop: st){
				props = StringUtils.getTokenArray(prop, assignSeparator, escapeChar);
				++count;
				property = props[0];
				value = props[1];

				// skip TAG if encountered. We already handled it.
				if(SEARCH_CRITERIA_TAG.equalsIgnoreCase(property)){
					continue;
				}

				if(isPASM){
					//all are properties
					xpathStr += XPATH.condition(property, value, false);
				}else{
					//handle some 'reserved qualifiers'
					if(  SEARCH_CRITERIA_ITEMINDEX.equalsIgnoreCase(property)
							|| SEARCH_CRITERIA_PATH.equalsIgnoreCase(property)
							|| SEARCH_CRITERIA_INDEX.equalsIgnoreCase(property)
							){
						qualifiers.put(property.toUpperCase(), value);
						continue;

					}else if (SEARCH_CRITERIA_PROPERTY.equalsIgnoreCase(property)){
						IndependantLog.debug(debugmsg +"add search condition '"+value+"'");
						xpathCondition = getXpathCondition(value, false);
						if(xpathCondition!=null) xpathStr += xpathCondition;

					}else if (SEARCH_CRITERIA_PROPERTY_CONTAINS.equalsIgnoreCase(property)){
						IndependantLog.debug(debugmsg +"add search condition '"+value+"'");
						xpathCondition = getXpathCondition(value, true);
						if(xpathCondition!=null) xpathStr += xpathCondition;

					}else if (SEARCH_CRITERIA_TEXT.equalsIgnoreCase(property)){
						xpathCondition = XPATH.conditionForText(value, false);
						if(xpathCondition!=null) xpathStr += xpathCondition;

					}else if ((SEARCH_CRITERIA_TEXT+SEARCH_CRITERIA_CONTAINS_SUFFIX).equalsIgnoreCase(property)){
						xpathCondition = XPATH.conditionForText(value, true);
						if(xpathCondition!=null) xpathStr += xpathCondition;

					}// try to generically handle any <something>Contains property values not handled above
					else if (property.toUpperCase().endsWith(SEARCH_CRITERIA_CONTAINS_SUFFIX)){
						//TextContains should be handled differently to get the xpath
						xpathStr += XPATH.conditionContains(property, value);
					}else{
						xpathStr += XPATH.condition(property, value, false);
					}
				}

				if (st.length != count) xpathStr += " "+XPATH.AND+" ";
			}
			//Remove the last "and", if there is no search-condition after that "and"
			if(xpathStr.trim().endsWith(XPATH.AND)){
				xpathStr = xpathStr.substring(0, xpathStr.lastIndexOf(XPATH.AND));
			}
			xpathStr += XPATH.END;

			list = getObjectsByXpathAndQualifiers(sc, xpathStr, qualifiers);

		}catch(Throwable t){
			IndependantLog.debug(debugmsg +"recognition string parsing error: "+ t.getClass().getSimpleName()+": "+t.getMessage());
		}

		return list;
	}

	/**
	 * According to TextMatchingCriterion to get the sub item within a container WebElement.
	 * @param itemContainer WebElement, the container WebElement, like list, menu etc.
	 * @param criterion TextMatchingCriterion, holding the searching condition
	 * @return WebElement, the matched sub item webelement
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected static WebElement getSubItem(WebElement itemContainer, TextMatchingCriterion criterion){
		String debugmsg = StringUtils.debugmsg(false);

		if(itemContainer instanceof WebElement){
			WebElement container = itemContainer;
			String type = WebDriverGUIUtilities.getLibraryType(container);
			IndependantLog.debug(debugmsg+" itemContainer's library type is '"+type+"'.");
			String classname = WebDriverGUIUtilities.getLibraryPackage()+"."+type;
			IndependantLog.debug(debugmsg+" instantiate class '"+classname+"'.");
			try {
				Class clazz = Class.forName(classname);
				Constructor<WebElement> cons = clazz.getConstructor(WebElement.class);
				Component component = (Component) cons.newInstance(container);

				Element element = component.getMatchedElement(criterion);
				element.refresh(false);//we must refresh to get the latest WebElement
				return element.getWebElement();
			} catch (Exception e) {
				IndependantLog.error(debugmsg+" Met "+StringUtils.debugmsg(e));
			}
		}else{
			IndependantLog.error(debugmsg+" itemContainer is not WebElement, cannot handle.");
		}

		return null;
	}

	/**
	 * According to xpath and qualifiers to find the WebElement.<br>
	 * @param sc SearchContext, the search context, it is normally a WebElement.
	 * @param xpathStr String, the xpath used to find a WebElment
	 * @param qualifiers HashMap<String, String>, qualifiers and their values used to find a WebElement
	 * @return WebElement
	 */
	protected static WebElement getObjectByXpathAndQualifiers(SearchContext sc, String xpathStr,
			HashMap<String, String> qualifiers){
		WebElement obj = null;
		String debugmsg = StringUtils.debugmsg(false);

		try{
			//Handle the qualifiers like "Index", "ItemIndex", "Path" etc.
			//"Index" has the most priority, will be handled firstly
			//Find the WebElement according to xpath, and the possible qualifier 'Index'
			String xpath = String.format(xpathStr);
			IndependantLog.debug(debugmsg+" handling xpath '"+xpath+"'");
			IndependantLog.debug(debugmsg+" qualifiers are: "+qualifiers);
			if(qualifiers.containsKey(SEARCH_CRITERIA_INDEX)){
				List<WebElement> preMatches = sc.findElements(By.xpath(xpath));
				int matchindex = StringUtilities.parseIndex(qualifiers.get(SEARCH_CRITERIA_INDEX));//1-based
				String typeInSAFS = qualifiers.containsKey(SEARCH_CRITERIA_TYPE)?qualifiers.get(SEARCH_CRITERIA_TYPE):null;
				IndependantLog.debug(debugmsg+" handling index='"+matchindex+"' type='"+typeInSAFS+"'");
				obj = getNthWebElement(preMatches, matchindex, typeInSAFS);
			}else{
				obj = sc.findElement(By.xpath(xpath));
			}

			//Handle the other qualifiers "ItemIndex", "Path"
			if(obj!=null && !qualifiers.isEmpty()){
				TextMatchingCriterion criterion = null;
				if(qualifiers.containsKey(SEARCH_CRITERIA_ITEMINDEX)){
					String itemIndex = qualifiers.get(SEARCH_CRITERIA_ITEMINDEX);//0-based
					IndependantLog.debug(debugmsg+" handling ItemIndex='"+itemIndex+"'");
					criterion = new TextMatchingCriterion(Integer.parseInt(itemIndex));

				}else if(qualifiers.containsKey(SEARCH_CRITERIA_PATH)){
					String path = qualifiers.get(SEARCH_CRITERIA_PATH);
					//TODO HANDLE the pathIndex "4->2->3", "Path=A->B;PathIndex=3->2"
					//TODO HANDLE the Index in Path "4->A->D", "Path=4->A->D"
					IndependantLog.debug(debugmsg+" handling Path='"+path+"'");
					criterion = new TextMatchingCriterion(path, false, (String)null);
				}
				obj = criterion == null ? obj : getSubItem(obj, criterion);
			}
		}catch(Exception e){
			IndependantLog.error(debugmsg+" met "+StringUtils.debugmsg(e));
		}

		return obj;
	}

	/**
	 * According to xpath and qualifiers to find matching WebElements.<br>
	 * While returning a List, it is likely the List will have only 1 matching item.
	 * @param sc SearchContext, the search context, it is normally a WebElement.
	 * @param xpathStr String, the xpath used to find matching WebElements
	 * @param qualifiers HashMap<String, String>, qualifiers and their values used to find matching WebElement subItems.
	 * @return List&lt;WebElement> 0 or more WebElements.
	 */
	protected static List<WebElement> getObjectsByXpathAndQualifiers(SearchContext sc, String xpathStr,
			HashMap<String, String> qualifiers){
		List<WebElement> list = new ArrayList<WebElement>();
		WebElement obj = null;
		String debugmsg = StringUtils.debugmsg(false);

		try{
			//Handle the qualifiers like "Index", "ItemIndex", "Path" etc.
			//"Index" has the most priority, will be handled firstly
			//Find the WebElement according to xpath, and the possible qualifier 'Index'
			String xpath = String.format(xpathStr);
			IndependantLog.debug(debugmsg+" handling xpath '"+xpath+"'");
			IndependantLog.debug(debugmsg+" qualifiers are: "+qualifiers);
			List<WebElement> preMatches = new ArrayList<WebElement>();
			if(qualifiers.containsKey(SEARCH_CRITERIA_INDEX)){
				preMatches = sc.findElements(By.xpath(xpath));
				int matchindex = StringUtilities.parseIndex(qualifiers.get(SEARCH_CRITERIA_INDEX));//1-based
				String typeInSAFS = qualifiers.containsKey(SEARCH_CRITERIA_TYPE)?qualifiers.get(SEARCH_CRITERIA_TYPE):null;
				IndependantLog.debug(debugmsg+" handling index='"+matchindex+"' type='"+typeInSAFS+"'");
				obj = getNthWebElement(preMatches, matchindex, typeInSAFS);
				preMatches.clear();
				if(obj != null) {
					preMatches.add(obj);
				}
			}else{
				preMatches = sc.findElements(By.xpath(xpath));
			}

			if(preMatches.isEmpty()) return list; // return empty list

			//Handle the other qualifiers "ItemIndex", "Path"
			if(!qualifiers.isEmpty()){
				for(int i=0;i<preMatches.size();i++){
					obj = preMatches.get(i);
					TextMatchingCriterion criterion = null;
					if(qualifiers.containsKey(SEARCH_CRITERIA_ITEMINDEX)){
						String itemIndex = qualifiers.get(SEARCH_CRITERIA_ITEMINDEX);//0-based
						IndependantLog.debug(debugmsg+" handling ItemIndex='"+itemIndex+"'");
						criterion = new TextMatchingCriterion(Integer.parseInt(itemIndex));

					}else if(qualifiers.containsKey(SEARCH_CRITERIA_PATH)){
						String path = qualifiers.get(SEARCH_CRITERIA_PATH);
						//TODO HANDLE the pathIndex "4->2->3", "Path=A->B;PathIndex=3->2"
						//TODO HANDLE the Index in Path "4->A->D", "Path=4->A->D"
						IndependantLog.debug(debugmsg+" handling Path='"+path+"'");
						criterion = new TextMatchingCriterion(path, false, (String)null);
					}
					if(criterion != null){
						// TODO would like this to potentially return a List of matched SubItems.
						obj = getSubItem(obj, criterion);
						if(obj != null){
							list.add(obj);
						}
					}
				}
			}else{
				list = preMatches;
			}
		}catch(Exception e){
			IndependantLog.error(debugmsg+" met "+StringUtils.debugmsg(e));
		}
		return list;
	}

	/**
	 * Find the Nth matched WebElement in a list.<br>
	 * @param preMatches List&lt;WebElement>, a list of WebElment from where to find a matched Element
	 * @param matchindex int, the expected index, 1-based
	 * @param typeInSAFS String, the expected type, like "Button", "List", "CheckBox". If null, match all elements.
	 * @return WebElement, the Nth matched WebElement. null if not found.
	 */
	protected static WebElement getNthWebElement(List<WebElement> preMatches, int matchindex, String typeInSAFS){
		String debugmsg = StringUtils.debugmsg(false);
		WebElement matchedElement = null;

		boolean matched = false;
		if(preMatches!=null && !preMatches.isEmpty()){
			IndependantLog.debug(debugmsg +"found "+ preMatches.size()+" matching elements to process for type: "+ typeInSAFS);

			int index = 0;
			for(int n=0;n<preMatches.size();n++){
				matched = false;
				matchedElement = preMatches.get(n);
				if(matchedElement.isDisplayed()){
					matched = (typeInSAFS==null? true: WebDriverGUIUtilities.isTypeMatched(matchedElement, typeInSAFS) );
					if(matched){
						String id = matchedElement.getAttribute("id");
						index++;
						IndependantLog.debug(debugmsg +"found matching element["+ n+ "] as INDEX "+ index +" with ID: "+id);
						if(index==matchindex) {
							IndependantLog.debug(debugmsg +"returning matching element["+ n +"] as INDEX "+ index +" with ID: "+id);
							return matchedElement;
						}
					}else{
						IndependantLog.debug(debugmsg +"element["+ n +"] does not match expected type: "+ typeInSAFS+". It does not count as a match.");
					}
				}else{
					IndependantLog.debug(debugmsg +"element["+ n +"].isDisplayed == false. This does not count towards visible matches.");
				}
			}
		}

		return null;
	}

	/**
	 * Alternate search for special framework elements like Dojo or SAP OpenUI5 components.
	 * @param sc SearchContext, the search context
	 * @param RS String, the recognition string
	 * @param isPropertyAllMode boolean, if true, each part of 'recognition string' will be treated as property.<br>
	 *                                   if false, some reserved words will be treated as qualifiers, but not properties.<br>
	 *                                   the reserved words are {@link #SEARCH_CRITERIA_ITEMINDEX}, {@link #SEARCH_CRITERIA_PATH} etc.<br>
	 * @return WebElement or null if no match found.
	 */
	protected static WebElement getObjectForDomain(SearchContext sc, String RS, boolean isPropertyAllMode){
		//search the target element within a frame (if exist) level by level
		WebElement obj = null;
		String debugmsg = StringUtils.debugmsg(false);
		IndependantLog.debug(debugmsg +"processing recognition fragment: "+ RS);
		String[] tokens = StringUtils.getTokenArray(RS, qulifierSeparator, escapeChar);
		String xpathStr = XPATH.RELATIVE_MATCHING_ALL_START+XPATH.TRUE_CONDITION;
		boolean isDojo = false;
		@SuppressWarnings("unused")
		boolean isSAP = false;

		HashMap<String, String> qualifiers = new HashMap<String, String>();
		String xpathCondition = null;

		try{
			String criteria = null;
			String value = null;
			int i = -1;
			for (String prop: tokens){
				criteria = null;
				value = null;
				i = prop.indexOf(assignSeparator);
				if (i > 0){
					criteria = prop.substring(0, i);
					if(prop.length()> i) value = prop.substring(i+1);
				}else{
					IndependantLog.warn(debugmsg+" RS '"+prop+"' doesn't contain '='. Just ignore it.");
					continue;
				}

				if (SEARCH_CRITERIA_TYPE.equalsIgnoreCase(criteria)){
					if (value.toUpperCase().startsWith(DOMAIN_DOJO)){
						// current DOJO always uses DIV as the high-level widget element
						xpathStr = XPATH.RELATIVE_MATCHING_DIV_START+XPATH.TRUE_CONDITION; // DOJO components all DIVs ?
						isDojo = true;
					}else if (value.toUpperCase().startsWith(DOMAIN_SAP)){
						xpathStr = XPATH.RELATIVE_MATCHING_ALL_START+XPATH.TRUE_CONDITION; // any element?
						isSAP = true;
					}
					qualifiers.put(SEARCH_CRITERIA_TYPE, value);

				}else{
					if(isPropertyAllMode){
						//all are treated as component's properties
						IndependantLog.debug(debugmsg +"add search condition @"+criteria+"='"+value+"'");
						xpathStr += " "+XPATH.AND+XPATH.condition(criteria, value, false);
					}else{
						//handle some 'reserved qualifiers'
						if (SEARCH_CRITERIA_ID.equalsIgnoreCase(criteria)){
							xpathStr += isDojo ?
									" and (starts-with(@id,'"+value+"') or "+
									"(substring(@id, string-length(@id) - string-length('"+value+"')+1)='"+value+"'))"
									: /* SAP */ " and @id='"+ value+"'";

						}else if (SEARCH_CRITERIA_NAME.equalsIgnoreCase(criteria)){
							xpathStr += isDojo ? /* TODO: do dojo names concat like their ids do? need to check. */
									" and (starts-with(@name,'"+value+"') or "+
									"(substring(@name, string-length(@name) - string-length('"+value+"')+1)='"+value+"'))"
									: /* SAP */ " and @name='"+ value+"'";

						}else if (SEARCH_CRITERIA_TITLE.equalsIgnoreCase(criteria)){
							xpathStr += " and @title='"+ value+"'";

						}else if (SEARCH_CRITERIA_TEXT.equalsIgnoreCase(criteria)){
							xpathStr += " and .='"+value+"'";

						}else if (SEARCH_CRITERIA_INDEX.equalsIgnoreCase(criteria)
								||SEARCH_CRITERIA_PATH.equalsIgnoreCase(criteria)
								||SEARCH_CRITERIA_ITEMINDEX.equalsIgnoreCase(criteria)
								){
							qualifiers.put(criteria.toUpperCase(), value);

						}else if (SEARCH_CRITERIA_PROPERTY.equalsIgnoreCase(criteria)){
							IndependantLog.debug(debugmsg +"add search condition '"+value+"'");
							xpathCondition = getXpathCondition(value, false);
							if(xpathCondition!=null) xpathStr += " "+XPATH.AND+xpathCondition;

						}else if (SEARCH_CRITERIA_PROPERTY_CONTAINS.equalsIgnoreCase(criteria)){
							IndependantLog.debug(debugmsg +"add search condition '"+value+"'");
							xpathCondition = getXpathCondition(value, true);
							if(xpathCondition!=null) xpathStr += " "+XPATH.AND+xpathCondition;

						}else{
							//the others will be simply considered as properties
							IndependantLog.debug(debugmsg +"add search condition @"+criteria+"='"+value+"'");
							xpathStr += " "+XPATH.AND+XPATH.condition(criteria, value, false);
						}
					}//end if isPropertyAllMode
				}//end if SEARCH_CRITERIA_TYPE
			}//end for tokens

			xpathStr += XPATH.END;

			obj = getObjectByXpathAndQualifiers(sc, xpathStr, qualifiers);

		}catch(Throwable t){
			IndependantLog.debug(debugmsg +"recognition string parsing error: "+ t.getClass().getSimpleName()+": "+t.getMessage());
		}
		if(obj == null)
			IndependantLog.debug(debugmsg +"did not find any matching elements.");
		return obj;
	}

	/**
	 * Alternate search for special framework elements like Dojo or SAP OpenUI5 components.
	 * @param sc SearchContext, the search context
	 * @param RS String, the recognition string
	 * @param isPropertyAllMode boolean, if true, each part of 'recognition string' will be treated as property.<br>
	 *                                   if false, some reserved words will be treated as qualifiers, but not properties.<br>
	 *                                   the reserved words are {@link #SEARCH_CRITERIA_ITEMINDEX}, {@link #SEARCH_CRITERIA_PATH} etc.<br>
	 * @return List&lt;WebElement> 0 or more matching WebElements.
	 */
	protected static List<WebElement> getObjectsForDomain(SearchContext sc, String RS, boolean isPropertyAllMode){
		//search the target element within a frame (if exist) level by level

		List<WebElement> list = new ArrayList<WebElement>();
		String debugmsg = StringUtils.debugmsg(false);
		IndependantLog.debug(debugmsg +"processing recognition fragment: "+ RS);
		String[] tokens = StringUtils.getTokenArray(RS, qulifierSeparator, escapeChar);
		String xpathStr = XPATH.RELATIVE_MATCHING_ALL_START+XPATH.TRUE_CONDITION;
		boolean isDojo = false;
		@SuppressWarnings("unused")
		boolean isSAP = false;

		HashMap<String, String> qualifiers = new HashMap<String, String>();
		String xpathCondition = null;

		try{
			String criteria = null;
			String value = null;
			int i = -1;
			for (String prop: tokens){
				criteria = null;
				value = null;
				i = prop.indexOf(assignSeparator);
				if (i > 0){
					criteria = prop.substring(0, i);
					if(prop.length()> i) value = prop.substring(i+1);
				}else{
					IndependantLog.warn(debugmsg+" RS '"+prop+"' doesn't contain '='. Just ignore it.");
					continue;
				}

				if (SEARCH_CRITERIA_TYPE.equalsIgnoreCase(criteria)){
					if (value.toUpperCase().startsWith(DOMAIN_DOJO)){
						// current DOJO always uses DIV as the high-level widget element
						xpathStr = XPATH.RELATIVE_MATCHING_DIV_START+XPATH.TRUE_CONDITION; // DOJO components all DIVs ?
						isDojo = true;
					}else if (value.toUpperCase().startsWith(DOMAIN_SAP)){
						xpathStr = XPATH.RELATIVE_MATCHING_ALL_START+XPATH.TRUE_CONDITION; // any element?
						isSAP = true;
					}
					qualifiers.put(SEARCH_CRITERIA_TYPE, value);

				}else{
					if(isPropertyAllMode){
						//all are treated as component's properties
						IndependantLog.debug(debugmsg +"add search condition @"+criteria+"='"+value+"'");
						xpathStr += " "+XPATH.AND+XPATH.condition(criteria, value, false);
					}else{
						//handle some 'reserved qualifiers'
						if (SEARCH_CRITERIA_ID.equalsIgnoreCase(criteria)){
							xpathStr += isDojo ?
									" and (starts-with(@id,'"+value+"') or "+
									"(substring(@id, string-length(@id) - string-length('"+value+"')+1)='"+value+"'))"
									: /* SAP */ " and @id='"+ value+"'";

						}else if (SEARCH_CRITERIA_NAME.equalsIgnoreCase(criteria)){
							xpathStr += isDojo ? /* TODO: do dojo names concat like their ids do? need to check. */
									" and (starts-with(@name,'"+value+"') or "+
									"(substring(@name, string-length(@name) - string-length('"+value+"')+1)='"+value+"'))"
									: /* SAP */ " and @name='"+ value+"'";

						}else if (SEARCH_CRITERIA_TITLE.equalsIgnoreCase(criteria)){
							xpathStr += " and @title='"+ value+"'";

						}else if (SEARCH_CRITERIA_TEXT.equalsIgnoreCase(criteria)){
							xpathStr += " and .='"+value+"'";

						}else if (SEARCH_CRITERIA_INDEX.equalsIgnoreCase(criteria)
								||SEARCH_CRITERIA_PATH.equalsIgnoreCase(criteria)
								||SEARCH_CRITERIA_ITEMINDEX.equalsIgnoreCase(criteria)
								){
							qualifiers.put(criteria.toUpperCase(), value);

						}else if (SEARCH_CRITERIA_PROPERTY.equalsIgnoreCase(criteria)){
							IndependantLog.debug(debugmsg +"add search condition '"+value+"'");
							xpathCondition = getXpathCondition(value, false);
							if(xpathCondition!=null) xpathStr += " "+XPATH.AND+xpathCondition;

						}else if (SEARCH_CRITERIA_PROPERTY_CONTAINS.equalsIgnoreCase(criteria)){
							IndependantLog.debug(debugmsg +"add search condition '"+value+"'");
							xpathCondition = getXpathCondition(value, true);
							if(xpathCondition!=null) xpathStr += " "+XPATH.AND+xpathCondition;

						}else{
							//the others will be simply considered as properties
							IndependantLog.debug(debugmsg +"add search condition @"+criteria+"='"+value+"'");
							xpathStr += " "+XPATH.AND+XPATH.condition(criteria, value, false);
						}
					}//end if isPropertyAllMode
				}//end if SEARCH_CRITERIA_TYPE
			}//end for tokens

			xpathStr += XPATH.END;

			list = getObjectsByXpathAndQualifiers(sc, xpathStr, qualifiers);

		}catch(Throwable t){
			IndependantLog.debug(debugmsg +"recognition string parsing error: "+ t.getClass().getSimpleName()+": "+t.getMessage());
		}
		if(list == null || list.isEmpty())
			IndependantLog.debug(debugmsg +"did not find any matching elements.");
		return list;
	}

	/**
	 * @param propertyValuePairStr String, the pair representing property and value, like property:value, separated by {@link #propertySeparator}
	 * @param partialMatch boolean, if the property's value will be matched partially (considered as a substring)
	 * @return String, the xpath condition for searching
	 * @see #getXpathCondition(String, String, boolean)
	 */
	protected static String getXpathCondition(String propertyValuePairStr, boolean partialMatch){
		String[] propertyValuePair = StringUtils.getTokenArray(propertyValuePairStr, propertySeparator, escapeChar);
		if(propertyValuePair==null || propertyValuePair.length<2){
			IndependantLog.warn(StringUtils.debugmsg(false) +" the property-value pair '"+propertyValuePairStr+"' is invalid! ignore it.");
			return null;
		}

		return XPATH.condition(propertyValuePair[0], propertyValuePair[1], partialMatch);
	}

	/**
	 * Get a boolean value from the WebElement according to the attributes.<br>
	 * @param webelement WebElement, the WebElement to get value of an attribute.<br>
	 * @param attributes String..., an array of attribute to get value for.
	 * @return boolean true if one of the attribute contains true value.
	 */
	public static boolean getBoolean(WebElement webelement, String... attributes){
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "getBoolean");
		boolean result = false;

		try{
			for(String attribute: attributes){
				result = StringUtilities.convertBool(webelement.getAttribute(attribute));
				if(result) break;
			}
		}catch(Throwable th){
			IndependantLog.warn(debugmsg+ "cannot get a true value, due to "+StringUtils.debugmsg(th));
		}
		return result;
	}

	/**
	 * Attempts to retrieve a text value for the WebElement.
	 * This is by combining webelement.getText() and our own getValue() and returning whichever gets
	 * us a text value.
	 * @param webelement WebElement, the web element to get text from.
	 * @return String, the element's text content
	 */
	public static String getText(WebElement webelement){
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "getText");
		String text = webelement.getText();
		IndependantLog.info(debugmsg+"webelement.getText()  received: "+ text);
		if(text==null || text.length()==0){
			//getValue() usually takes more time to process
			Object value = getValue(webelement, TEXT_VALUE_ATTRIBUTES);
			IndependantLog.info(debugmsg+"getValue() received: "+ value);
			text = value==null? "": value.toString();
		}
		//else if we can get the text by webelement.getText(), we don't want to waste more time

		IndependantLog.info(debugmsg+"returning: "+ text);
		return text;
	}

	/**
	 * Get a value from the WebElement according to the attributes.<br>
	 * The first non-null-value of the attributes will be returned, so the order<br>
	 * of attributes may affect the result.<br>
	 *
	 * @param webelement WebElement, the WebElement to get value of an attribute.<br>
	 * @param attributes String..., an array of attribute to get value for.
	 * @return Object the first non-null-value of the attributes; or null if all attributes have null value.
	 *
	 * @see #TEXT_VALUE_ATTRIBUTES
	 */
	public static Object getValue(WebElement webelement, String... attributes){
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "getValue");
		Object result = null;

		try{
			for(String attribute: attributes){
				try{
					result = WDLibrary.getProperty(webelement, attribute);
					if(result!=null) break;
				}catch(SeleniumPlusException e){
					//IndependantLog.warn(debugmsg+" fail to get '"+attribute+"', due to "+StringUtils.debugmsg(e));
				}
			}
		}catch(Throwable th){
			IndependantLog.error(debugmsg+ "cannot get a value, due to "+StringUtils.debugmsg(th));
		}
		return result;
	}

	/**
	 * Get the boolean value of a javascript global variable.
	 * @param variable String, the name of the global variable.
	 * @return boolean, the value of javascript global variable.
	 * @throws IllegalStateException if the value is NOT present or attainable.
	 * @see #js_getGlobalVariable(String)
	 */
	public static boolean js_getGlobalBoolVariable(String variable) throws IllegalStateException{
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "js_getGlobalBoolVariable");
		Object result = js_getGlobalVariable(variable);
		if(result instanceof Boolean){
			return ((Boolean) result).booleanValue();
		}else{
			IndependantLog.debug(debugmsg+" did NOT return Boolean value!");
			throw new IllegalStateException("did NOT return Boolean value!");
		}
	}

	/** '5000' milliseconds */
	public static final long DEFAULT_JS_TIMEOUT = 5000;
	/** timeout in milliseconds for javascript code to execute */
	private static long jsTimeout = DEFAULT_JS_TIMEOUT;
	/**
	 * Set the timeout for javascript code to execute.
	 * @param jsTimeout long, timeout in milliseconds for the javascript execution to finish
	 * @see #js_getErrorCode()
	 * @see #js_getErrorObject()
	 * @see #js_getGlobalVariable(String)
	 */
	public static void setJsTimeout(long jsTimeout){
		SearchObject.jsTimeout = jsTimeout;
	}
	public static long getJsTimeout(){
		return jsTimeout;
	}

	/**
	 *
	 * @param js	String, the javascript code to execute
	 * @param msTimeout	long, the timeout in milliseconds for the javascript execution
	 * @return Object, the javascript execution result
	 * @throws SeleniumPlusException if the timeout has been reached
	 * @throws InterruptedException if the thread (executing javascript) has been interrupted
	 */
	protected static Object js_executeWithTimeout(final String js, long msTimeout)throws SeleniumPlusException, InterruptedException{
		final List<Object> js_result = new ArrayList<Object>();
		js_result.add(null);
		Thread t = new Thread(new Runnable(){
			@Override
			public void run() {
				try{js_result.set(0, getJS().executeScript(js));}
				catch(org.openqa.selenium.UnhandledAlertException x){
					IndependantLog.warn(StringUtils.debugmsg(SearchObject.class, "js_executeWithTimeout") + " UnhandledAlertException: " + x);
				}
				catch(SeleniumPlusException x){
					IndependantLog.warn("SearchObject.js_executeWithTimeout(): Failed to execute script. Met SeleniumPlusException "+x.toString());
				}catch(NoSuchSessionException nsse){
					IndependantLog.warn("SearchObject.js_executeWithTimeout(): Failed to execute script. Met NoSuchSessionException "+nsse.toString());
				}
			}
		});
		t.setDaemon(true);
		t.start();
		t.join(msTimeout);
		if(t.isAlive()){
			String error = "Javascript execution timeout '"+msTimeout+"' millisecnods has been reached! Please use a larger timeout.";
			IndependantLog.error("SearchObject.js_executeWithTimeout(): "+error);
			throw new SeleniumPlusException(error);
		}

		return js_result.get(0);
	}

	/**
	 * Get the value of a javascript global variable.
	 * @param variable String, the name of the global variable.
	 * @return Object, the value of javascript global variable.
	 * @throws IllegalStateException if the value is NOT present or attainable.
	 */
	public static Object js_getGlobalVariable(String variable){
		String debugmsg = StringUtils.debugmsg(false);
		Object result = null;
		try{
			if(!StringUtils.isValid(variable)){
				IndependantLog.error(debugmsg+" parameter variable is not valid!");
				return null;
			}
			// TODO Carl Nagle
			//result = getJS().executeScript(JavaScriptFunctions.getGlobalVariable(variable));
			result = js_executeWithTimeout(JavaScriptFunctions.getGlobalVariable(variable), jsTimeout);
			if(result==null){
				IndependantLog.error(debugmsg+"The js returned result is null.");
			}
		}catch(Throwable ignore){
			IndependantLog.error(debugmsg, ignore);
		}
		return result;
	}
	/**
	 * Get the value for a javascript global boolean variable.
	 * @param variable String, the name of the global variable.
	 * @param onOrOff boolean, the value to set
	 * @see #js_setGlobalVariable(String, String)
	 */
	public static void js_setGlobalBoolVariable(String variable, boolean onOrOff){
		js_setGlobalVariable(variable, Boolean.toString(onOrOff));
	}
	/**
	 * Set the value for a javascript global variable.
	 * @param variable String, the name of the global variable.
	 * @param value String, the value to set
	 */
	public static void js_setGlobalVariable(String variable, String value){
		String debugmsg = StringUtils.debugmsg(false);
		try{
			if(!StringUtils.isValid(variable)){
				IndependantLog.error(debugmsg+" parameter variable is not valid!");
				return;
			}
			getJS().executeScript(JavaScriptFunctions.setGlobalVariable(variable, value));
		}catch(Throwable ignore){
			IndependantLog.error(debugmsg, ignore);
		}
	}

	/**
	 * Execute javascript to initialize global javascript variables.<br>
	 * @see WDLibrary#startBrowser(String, String, String, int, boolean, Map)
	 */
	protected static void js_initialize(){
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "js_initialize");
		StringBuffer scriptCommand = new StringBuffer();
		scriptCommand.append(JavaScriptFunctions.initPreviousHighlightElement());
		scriptCommand.append(JavaScriptFunctions.initJSError());
		scriptCommand.append(JavaScriptFunctions.initJSDebugArray());
		try {
			getJS().executeScript(scriptCommand.toString());
		} catch (Throwable e) {
			IndependantLog.error(debugmsg+" Failed.", e);
		}
	}

	/**
	 * Initialize the container to hold the debug message. This should be called before<br>
	 * executing a snippet of javascript<br>
	 * @throws SeleniumPlusException
	 * @see {@link #executeScript(boolean, String, Object...)}
	 */
	private static void js_initJSDebugArray() throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		try {
			getJS().executeScript(JavaScriptFunctions.initJSDebugArray());
		} catch (SeleniumPlusException e) {
			throw e;
		} catch (Throwable th){
			throw new SeleniumPlusException(debugmsg, th);
		}
	}

	/**
	 * Get the possible debug messages during executing a snippet of javascript.
	 * @return
	 * @throws SeleniumPlusException
	 * @see {@link #executeScript(boolean, String, Object...)}
	 */
	private static List<?> js_getJSDebugArray() throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		try {
			Object debugmessages = getJS().executeScript(JavaScriptFunctions.getJSDebugArray());
			if(debugmessages!=null){
				if(debugmessages instanceof List){
					return ((List<?>)debugmessages);
				}else{
					IndependantLog.debug(debugmsg+" Don't know how to analyse debug message of type "+debugmessages.getClass().getName());
				}
			}
			return new ArrayList<Object>();
		} catch (SeleniumPlusException e) {
			throw e;
		} catch (Throwable th){
			throw new SeleniumPlusException(debugmsg, th);
		}
	}

	/**
	 * Initialize the global variable {@link JavaScriptFunctions#SAFS_JAVASCRIPT_GLOBAL_ERROR_CODE_VAR},<br>
	 * set it value to {@link JavaScriptFunctions#ERROR_CODE_NOT_SET}. <br>
	 * Initialize the global variable {@link JavaScriptFunctions#SAFS_JAVASCRIPT_GLOBAL_ERROR_OBJECT_VAR} to undefined.<br>
	 */
	public static void js_initError() throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "js_initError");
		try {
			getJS().executeScript(JavaScriptFunctions.initJSError());
		} catch (SeleniumPlusException e) {
			throw e;
		} catch (Throwable th){
			throw new SeleniumPlusException(debugmsg, th);
		}
	}
	/**
	 * Clean the global variable {@link JavaScriptFunctions#SAFS_JAVASCRIPT_GLOBAL_ERROR_CODE_VAR},<br>
	 * reset it value to {@link JavaScriptFunctions#ERROR_CODE_NOT_SET}. <br>
	 * Clean the global variable {@link JavaScriptFunctions#SAFS_JAVASCRIPT_GLOBAL_ERROR_OBJECT_VAR}, reset it to undefined.<br>
	 */
	public static void js_cleanError() throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "js_cleanError");
		try {
			getJS().executeScript(JavaScriptFunctions.cleanJSError());
		} catch (SeleniumPlusException e) {
			throw e;
		} catch (Throwable th){
			throw new SeleniumPlusException(debugmsg, th);
		}
	}
	/**
	 * Get the value of global variable {@link JavaScriptFunctions#SAFS_JAVASCRIPT_GLOBAL_ERROR_CODE_VAR}.<br>
	 */
	public static int js_getErrorCode() throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "js_getErrorCode");
		try {
//			Object object = getJS().executeScript(JavaScriptFunctions.getJSErrorCode());
			Object object = js_executeWithTimeout(JavaScriptFunctions.getJSErrorCode(), jsTimeout/*give 5 seconds, it needs more time to get work done with Firefox*/);
			return ((Long) object).intValue();
		} catch (SeleniumPlusException e) {
			throw e;
		} catch (Throwable th){
			throw new SeleniumPlusException(debugmsg, th);
		}
	}
	/**
	 * Set the value of global variable {@link JavaScriptFunctions#SAFS_JAVASCRIPT_GLOBAL_ERROR_CODE_VAR}<br>
	 */
	public static void js_setErrorCode(int error) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "js_setErrorCode");
		try {
			getJS().executeScript(JavaScriptFunctions.setJSErrorCode(error));
		} catch (SeleniumPlusException e) {
			throw e;
		} catch (Throwable th){
			throw new SeleniumPlusException(debugmsg, th);
		}
	}
	/**
	 * Get the value of global variable {@link JavaScriptFunctions#SAFS_JAVASCRIPT_GLOBAL_ERROR_OBJECT_VAR}.<br>
	 */
	public static Object js_getErrorObject() throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		try {
//			Object object = getJS().executeScript(JavaScriptFunctions.getJSErrorObject());
			Object object = js_executeWithTimeout(JavaScriptFunctions.getJSErrorObject(), jsTimeout/*give 5 seconds, the error object may be big and takes more time to return*/);
			return object;
		} catch (SeleniumPlusException e) {
			throw e;
		} catch (Throwable th){
			throw new SeleniumPlusException(debugmsg, th);
		}
	}
	/**
	 * Set a string value to global variable {@link JavaScriptFunctions#SAFS_JAVASCRIPT_GLOBAL_ERROR_OBJECT_VAR}<br>
	 */
	public static void js_setErrorObject(String errorMessage) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		try {
			getJS().executeScript(JavaScriptFunctions.setJSErrorObject(StringUtils.quote(errorMessage)));
		} catch (SeleniumPlusException e) {
			throw e;
		} catch (Throwable th){
			throw new SeleniumPlusException(debugmsg, th);
		}
	}

	/**
	 * Highlight the webelement by drawing a red rectangle around it.<br>
	 * @param webelement WebElement, the web element to highlight.
	 * @param duration	int, how long in millisecond the red rectangle will stay.
	 * @return boolean, true if the element is highlighted and then cleaned.
	 * @see #highlight(WebElement)
	 * @see #clearHighlight()
	 */
	public static boolean highlightThenClear(WebElement webelement, int duration){
		if(WDLibrary.highlight(webelement)){
			StringUtilities.sleep(duration);
			return WDLibrary.clearHighlight();
		}else{
			return false;
		}
	}

	/**
	 * Highlight the webelement by drawing a red rectangle around it.<br>
	 * <p>
	 * @param idOrNameOrXpath	String, the id or name or xpath of web element to highlight.
	 * @return boolean, true if the element is highlighted.
	 */
	public static boolean highlight(String idOrNameOrXpath){
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "highlight");

		if(idOrNameOrXpath==null || idOrNameOrXpath.isEmpty()){
			IndependantLog.error(debugmsg+"'"+idOrNameOrXpath+"' cannot be used to search web element, cannot highlight.");
			return false;
		}else{
			try {
				StringBuffer scriptCommand = new StringBuffer();
				scriptCommand.append(JavaScriptFunctions.getHighlightFunction(true));
				scriptCommand.append("highlight(arguments[0]);");
				WDLibrary.executeScript(scriptCommand.toString(), idOrNameOrXpath);
				return true;
			} catch (SeleniumPlusException e) {
				IndependantLog.error(debugmsg+" Failed.", e);
				return false;
			}
		}
	}

	/**
	 * Highlight the webelement by drawing a red rectangle around it.<br>
	 * To clean the red rectangle, please call {@link #clearHighlight()}.<br>
	 * @param webelement	WebElement, the web element to highlight.
	 * @return boolean, true if the element is highlighted.
	 * @see #clearHighlight()
	 */
	public static boolean highlight(WebElement webelement){
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "highlight");

		try {
			if(!isDisplayed(webelement)){
				IndependantLog.error(debugmsg+"webelement is not visible, cannot be highlighted.");
				return false;
			}
		} catch (SeleniumPlusException e) {
			IndependantLog.error(debugmsg+" met exception during checking element's visibility.", e);
			return false;
		}

		try {
			StringBuffer scriptCommand = new StringBuffer();
			scriptCommand.append(JavaScriptFunctions.highlight2());
			scriptCommand.append("highlight2(arguments[0]);");
			WDLibrary.executeScript(scriptCommand.toString(), webelement);
			return true;
		} catch (SeleniumPlusException e) {
			IndependantLog.error(debugmsg+" Failed.", e);
			return false;
		}
	}

	/**
	 * clear the previous highlight of a webelement.<br>
	 * <b>Note:</b>The webelement should be highlighted by {@link #highlight(String)}<br>
	 * or {@link #highlight(WebElement)}, normally you don't need to call this method<br>
	 * to clear the highlight if you want to highlight element one by one continuously, as <br>
	 * the previous highlighted element will be automatically cleared before next element<br>
	 * is highlighted.<br>
	 *
	 * @return boolean, true if the element's highlight has been cleared.
	 *
	 * @see #highlight(String)
	 * @see #highlight(WebElement)
	 */
	public static boolean clearHighlight(){
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "clearHighlight");

		try {
			StringBuffer scriptCommand = new StringBuffer();
			scriptCommand.append(JavaScriptFunctions.clearHighlight());
			scriptCommand.append("clearHighlight();");
			WDLibrary.executeScript(scriptCommand.toString());
			return true;
		} catch (SeleniumPlusException e) {
			IndependantLog.error(debugmsg+" Failed.", e);
			return false;
		}
	}

	/**
	 * Get the visibility of the webelement.<br>
	 * Try Selenium's API isDisplayed() firstly, if it is false then try to test value of attribute 'visibility'.<br>
	 * @param element WebElement
	 * @return true if the webelement is visible
	 * @throws SeleniumPlusException if the WebElement is null
	 * @see #isDisplayed(WebElement)
	 */
	public static boolean isVisible(WebElement element) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		boolean visible = false;

		checkNotNull(element);

		try{
			WDTimeOut.implicitlyWait(0, TimeUnit.SECONDS);
			visible = element.isDisplayed();
		}catch(StaleElementReferenceException se){
			//If the webelement is stale, we should consider it as invisible.
			return false;
		}catch(Exception e){
			IndependantLog.warn(debugmsg+" Met "+StringUtils.debugmsg(e));
		}finally{
			WDTimeOut.implicitlyWait(Processor.getSecsWaitForComponent(), TimeUnit.SECONDS);
		}

		if(!visible){
			try {
				//if element is still invisible, we will check the value of attribute 'visibility'
				//if the value is not 'hidden', we consider the element is visible;
				//BUT, if isDisplayed() return false, the calling of Selenium's
				//API click() will throw org.openqa.selenium.ElementNotVisibleException, maybe calling
				//other APIs will throw also ElementNotVisibleException, this is the risk.
				String visibility = WDLibrary.getProperty(element, Component.ATTRIBUTE_VISIBILITY);
				IndependantLog.debug(debugmsg+"visibility is '"+visibility+"'");
				visible = !Component.VALUE_VISIBILITY_HIDDEN.equalsIgnoreCase(visibility);
			} catch (SeleniumPlusException e) {
				IndependantLog.warn(debugmsg+"Fail to check property '"+Component.ATTRIBUTE_VISIBILITY+"'");
			}
		}

		return visible;
	}

	/**
	 * Check if the webelement is displayed (Selenium's API isDisplayed()).<br>
	 * @param element WebElement, the element to check.
	 * @return true if the webelement is visible
	 * @throws SeleniumPlusException if the WebElement is null or is stale
	 * @see #isVisible(WebElement)
	 */
	public static boolean isDisplayed(WebElement element) throws SeleniumPlusException{
		checkNotNull(element);
		try{
			WDTimeOut.implicitlyWait(0, TimeUnit.SECONDS);
			return element.isDisplayed();
		}catch(StaleElementReferenceException se){
			throw new SeleniumPlusException("weblement is stale.");
		}catch(Exception e){
			throw new SeleniumPlusException(StringUtils.debugmsg(e));
		}finally{
			WDTimeOut.implicitlyWait(Processor.getSecsWaitForComponent(), TimeUnit.SECONDS);
		}
	}

	private static boolean __isStale = false;
	private static Throwable __isStaleException = null;
	private static WebElement __isStaleElement = null;
	/**
	 * Test if a WebElement is stale(DOM element is deleted or is repaint or disappear).
	 * @param element WebElement, the WebElement to detect.
	 * @return boolean true if the WebElement is stale.
	 * @throws SeleniumPlusException if the element is null.
	 */
	public static boolean isStale(WebElement element) throws SeleniumPlusException{
		__isStaleElement = element;
		checkNotNull(element);
		__isStale = false;
		__isStaleException = null;
		Thread t = new Thread(new Runnable(){
			@Override
			public void run(){
				try{
					//WebDriver will not throw StaleElementReferenceException until the 'implicit timeout' is reached.
					//We don't want to waste that time, so just set 'implicit timeout' to 0 and don't wait.
					WDTimeOut.implicitlyWait(0, TimeUnit.SECONDS);
					__isStaleElement.isDisplayed();
					__isStale = false;
				}catch(StaleElementReferenceException sere){
					__isStale = true;
				}catch(Throwable e){
					__isStaleException = e;
				}finally{
					WDTimeOut.implicitlyWait(Processor.getSecsWaitForComponent(), TimeUnit.SECONDS);
				}
			}
		});
		t.setDaemon(true);
		t.start();
		try{ t.join(600);}
		catch(InterruptedException x){
			IndependantLog.debug("SearchObject.isStale() execute wait Interrupted?");
			__isStaleException = x; //Carl Nagle -- Evaluate if we want this Exception thrown below
		}
		if(__isStaleException instanceof Throwable) throw new SeleniumPlusException("SearchObject.isStale() "+ StringUtils.debugmsg(__isStaleException));
		return __isStale;
	}

	/**
	 * A map containing pairs of (listenerID, listener). The listener will respond to a javasript event.<br>
	 */
	protected static Map<String, GenericJSEventListener> jsEventListenerMap = new HashMap<String, GenericJSEventListener>();
	/**
	 * A map containing pairs of (listenerID, PollingRunnable). The PollingRunnable will check a javascript<br>
	 * variable in a loop, when the variable is true, it will invoke the listener according to the listenerID.<br>
	 */
	protected static Map<String, PollingRunnable> jsEventListenerWaitingThreadMap = new HashMap<String, PollingRunnable>();

	/**
	 * A generic javascript event listener interface.<br>
	 *
	 * History:<br>
	 *  <br>   Apr 14, 2014    (Lei Wang) Initial release.
	 *
	 * @see SearchObject#addJavaScriptEventListener(WebElement, String, GenericJSEventListener)
	 */
	public interface GenericJSEventListener{
		/**javascript event 'mousedown'*/
		public static final String EVENT_JS_MOUSEDOWN = "mousedown";
		/**javascript event 'mouseup'*/
		public static final String EVENT_JS_MOUSEUP = "mouseup";
		/**javascript event 'click'*/
		public static final String EVENT_JS_click = "click";

		/** Defalut time to wait for the javascript 'mousedown' event, it is 1000 milliseconds */
		public static final int TIMEOUT_WAIT_FOR_EVENT_MOUSEDOWN = 1000;

		public void onEventFired();
	}

	/**
	 *
	 * A default javascript event listener.<br>
	 * Usage example:<br>
	 * <pre>
	 * String event = "mousedown";
	 * int timeout = 1000;//milliseconds
	 * DefaultJSEventListener listener = new DefaultJSEventListener(event);
	 * //add listener for event
	 * String listenerID = WDLibrary.addJavaScriptEventListener(webelement, event, listener);
	 * //do some mousedown related work, for example 'click'
	 * webelement.click();
	 * //wait for event happen
	 * if(listener.waitEventFired(timeout)){ //ok, event has been fired.}
	 * //remove the listener
	 * WDLibrary.removeJavaScriptEventListener(webelement, event, listenerID);
	 * </pre>
	 *
	 * History:<br>
	 *  <br>   Apr 15, 2014    (Lei Wang) Initial release.
	 *
	 * @see SearchObject#addJavaScriptEventListener(WebElement, String, GenericJSEventListener)
	 */
	protected static class DefaultJSEventListener implements GenericJSEventListener{
		private boolean eventFired = false;
		private String eventName = "event";

		public synchronized boolean isEventFired() {
			return eventFired;
		}

		public synchronized void setEventFired(boolean eventFired) {
			this.eventFired = eventFired;
			this.notifyAll();
		}

		public String getEventName() {
			return eventName;
		}

		public void setEventName(String eventName) {
			this.eventName = eventName;
		}

		public DefaultJSEventListener(){}
		public DefaultJSEventListener(String eventName){ this.eventName=eventName;}

		@Override
		public void onEventFired() {
			setEventFired(true);
		}

		/**
		 * @param timeout long, time to wait for the event fired. in milliseconds.
		 * @return boolean true if the event has been fired within timeout; false otherwise.
		 */
		public boolean waitEventFired(long timeout){
			String debugmsg = StringUtils.debugmsg(getClass(), "waitEventFired");
			synchronized(this){
				long endTime = System.currentTimeMillis() + timeout;
				while(!isEventFired() && System.currentTimeMillis()<endTime){
					try {
						IndependantLog.debug(debugmsg+"Waitting for "+eventName+" happen.");
						this.wait(timeout);
					} catch (InterruptedException e) {}
				}
			}

			return isEventFired();
		}
	}

	/**
	 * The PollingRunnable will check a javascript variable in a loop,<br>
	 * when the variable is true, it will invoke the listener according to the listenerID.<br>
	 *
	 * History:<br>
	 *  <br>   Apr 14, 2014    (Lei Wang) Initial release.
	 */
	private static class PollingRunnable implements Runnable{
		private boolean keepRunning = true;
		private String listenerID = null;

		@SuppressWarnings("unused")
		public PollingRunnable(){super();}

		public PollingRunnable(String jsVariableName){
			this.listenerID = jsVariableName;
		}

		public synchronized boolean isKeepRunning() {
			return keepRunning;
		}
		public synchronized void setKeepRunning(boolean keepRunning) {
			this.keepRunning = keepRunning;
		}

		@Override
		public void run() {
			final String debugmsg = StringUtils.debugmsg(PollingRunnable.class, "run");
			IndependantLog.debug(debugmsg+"pollingThread started for "+ listenerID);
			boolean eventfired = false;
			GenericJSEventListener listener = null;
			int pauseTime = 500;//milliseconds
			while(true && isKeepRunning()){
				try {
					eventfired = js_getGlobalBoolVariable(listenerID);
					if(eventfired){
						IndependantLog.debug(debugmsg+"Got signal, event has been fired for "+ listenerID);
						listener = jsEventListenerMap.get(listenerID);
						if(listener!=null) listener.onEventFired();
						//reset the global variable
						js_setGlobalBoolVariable(listenerID, false);
					}else{
						try { Thread.sleep(pauseTime); } catch (InterruptedException ignore) { }
					}
				} catch (Throwable e) {
					IndependantLog.error(debugmsg+" "+ listenerID+" pollingThread: ", e);
					break;
				}
			}
			IndependantLog.debug(debugmsg+"pollingThread ended for "+listenerID);
		}
	}

	/**
	 *
	 * @param element	WebElement, the element to which the listener will be added.
	 * @param eventName String, event name to attach this listener.
	 * @param listener	GenericJSEventListener, the listener to respond to fired-event.
	 * @return String, the ID of this listener. Will be used to remove the listener.
	 * @throws SeleniumPlusException
	 */
	public static String addJavaScriptEventListener(WebElement element, String eventName, GenericJSEventListener listener) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(WDLibrary.class, "addJavaScriptEventListener");
		String listenerID = eventName+"_"+System.currentTimeMillis();

		try {
			//Attach the real javascript event by JavaScriptFunctions, the javascript listener will
			//simply set a 'global variable' to true, that 'global variable' name is "window."+listenerID
			String script = null;
			if(WDLibrary.isSAPDomain(element)){
				script = JavaScriptFunctions.SAP.sap_ui_core_Control_attachBrowserEvent(true, listenerID);
				script += " sap_ui_core_Control_attachBrowserEvent(arguments[0],arguments[1]);";
				SearchObject.executeJavaScriptOnWebElement(script, element, eventName);
			}else if(WDLibrary.isDojoDomain(element)){
				script = JavaScriptFunctions.DOJO.dojo_dijit_WidgetBase_on(true, listenerID);
				script += " dojo_dijit_WidgetBase_on(arguments[0],arguments[1]);";
				SearchObject.executeJavaScriptOnWebElement(script, element, eventName);
			}else{//For HTML Standard element
				script = JavaScriptFunctions.addGenericEventListener(true, listenerID);
				script += " addGenericEventListener(arguments[0],arguments[1],arguments[2]);";
				//Do we need to add a capturing listener?
				//				SearchObject.executeJavaScriptOnWebElement(script, element, eventName, true);
				SearchObject.executeJavaScriptOnWebElement(script, element, eventName, false);
			}

			//Put the java listener into the Map
			jsEventListenerMap.put(listenerID, listener);

			//Check the 'global variable' ("window."+listenerID) status in a thread loop
			PollingRunnable pollingRunnable = new PollingRunnable(listenerID);
			Thread pollingThread = new Thread(pollingRunnable);

			//Put the polling runnable into the Map
			jsEventListenerWaitingThreadMap.put(listenerID, pollingRunnable);
			pollingThread.setDaemon(true);//so that it will not block the whole program
			pollingThread.start();
		} catch (SeleniumPlusException e1) {
			IndependantLog.error(debugmsg+"Fail to attach event '"+eventName+"' ", e1);
			throw new SeleniumPlusException("Fail to attach event '"+eventName+"' "+e1.getMessage());
		}

		return listenerID;
	}

	/**
	 * <b>Note:</b> It is important to call this method in a finally clause to make sure that the polling thread stop.
	 * <pre>
	 *   String listenerID = null;
	 *   try{
	 *   	listenerID = addJavaScriptEventListener(element, eventName, listener);
	 *   }catch(Exception e){
	 *   }finally{
	 *     if(listenerID!=null) removeJavaScriptEventListener(element, eventName, listenerID);
	 *   }
	 * </pre>
	 * @param element	WebElement, the element from which the listener will be removed.
	 * @param eventName String, event name to detach a listener.
	 * @param String, the ID of this listener, used to remove a listener.
	 * @throws SeleniumPlusException
	 */
	public static void removeJavaScriptEventListener(WebElement element, String eventName, String listenerID) throws SeleniumPlusException{
		final String debugmsg = StringUtils.debugmsg(WDLibrary.class, "removeJavaScriptEventListener");

		//Stop the polling thread and remove it from the jsEventListenerWaitingThreadMap
		//It is important to stop the polling thread before calling java script to remove 'generic javascript listener', as
		//'generic javascript listener' will remove the global-variable, and polling thread need to check that variable;
		//if we do thing in reverse order, polling thread will throw NullPointerException
		if(jsEventListenerWaitingThreadMap.containsKey(listenerID)){
			PollingRunnable pollingRunnable = jsEventListenerWaitingThreadMap.get(listenerID);
			pollingRunnable.setKeepRunning(false);
			jsEventListenerWaitingThreadMap.remove(listenerID);
		}

		//remove the java listener from jsEventListenerMap
		//then detach the javascript listener by JavaScriptFunctions
		try{
			if(jsEventListenerMap.containsKey(listenerID)){
				jsEventListenerMap.remove(listenerID);

				//Remove the javascript listener by JavaScriptFunctions
				String script = null;
				if(WDLibrary.isSAPDomain(element)){
					script = JavaScriptFunctions.SAP.sap_ui_core_Control_detachBrowserEvent(true, listenerID);
					script += " sap_ui_core_Control_detachBrowserEvent(arguments[0],arguments[1]);";
					SearchObject.executeJavaScriptOnWebElement(script, element, eventName);
				}else if(WDLibrary.isDojoDomain(element)){
					script = JavaScriptFunctions.DOJO.dojo_handle_remove(true, listenerID);
					script += " dojo_handle_remove();";
					SearchObject.executeScript(script);
				}else{//For HTML Standard element
					script = JavaScriptFunctions.removeGenericEventListener(true, listenerID);
					script += " removeGenericEventListener(arguments[0],arguments[1],arguments[2]);";
					//If we have added a capturing listener.
					//					SearchObject.executeJavaScriptOnWebElement(script, element, eventName, true);
					SearchObject.executeJavaScriptOnWebElement(script, element, eventName, false);
				}

			}
		} catch (SeleniumPlusException e1) {
			IndependantLog.error(debugmsg+"Fail to detach event '"+eventName+"' ", e1);
			throw new SeleniumPlusException("Fail to detach event '"+eventName+"' "+e1.getMessage());
		}

	}

	/**
	 *
	 * @param script	String, the javascript function to execute. Its first parameter is always DOM object.
	 * @param webelement	WebElement, used to find the DOM Object to operate.<br>
	 *                                  it is passed directly to javascript function and will be considered as a DOM element.
	 * @param args	Object[], the extra parameters used by the javascript function, from SECOND parameter.
	 *
	 * @return
	 * @throws SeleniumPlusException
	 */
	public static Object executeJavaScriptOnWebElement(String script, WebElement webelement, Object ... args) throws SeleniumPlusException{
		//Add webelement to the javascript function's parameter array
		int i = 0;
		Object[] params = new Object[args.length+1];
		params[i++] = webelement;

		//		IndependantLog.debug("Extra paramters are:");
		for(Object arg:args){
			params[i++] = arg;
			//IndependantLog.debug(arg.toString());
		}

		return executeScript(script, params);
	}

	/**
	 * @return Object or null.
	 * @see org.openqa.selenium.JavascriptExecutor#executeScript(String, Object...)
	 * @see #executeScript(boolean, String, Object...)
	 * @throws SeleniumPlusException
	 */
	public static Object executeScript(String script, Object... args) throws SeleniumPlusException{
		return executeScript(true, script, args);
	}
	/**
	 * @return Object or null.
	 * @see org.openqa.selenium.JavascriptExecutor#executeAsyncScript(String, Object...)
	 * @see #executeScript(boolean, String, Object...)
	 * @throws SeleniumPlusException
	 */
	public static Object executeAsyncScript(String script, Object... args) throws SeleniumPlusException{
		return executeScript(false, script, args);
	}

	/** 0 */
	public static final int DEFAULT_JS_EXECUTION_DELAY = 0;
	private static int jsExecutionDelay = DEFAULT_JS_EXECUTION_DELAY;

	/**
	 * <b>NOTE:</b>Sometimes we needs to wait for the WEB page FULLY loaded so that the javascript code can
	 *             return correctly.<br/>
	 *             Please set this delay back to {@link #DEFAULT_JS_EXECUTION_DELAY} after a special javascript call,
	 *             otherwise the performance of the SeleniumPlus framework will be slowed down.
	 *
	 * @param milliseconds int, the milliseconds to delay the javascript execution.
	 * @see #executeScript(boolean, String, Object...)
	 */
	protected static void setJsExecutionDelay(int milliseconds){
		if(milliseconds>0){
			jsExecutionDelay = milliseconds;
		}else{
			throw new IllegalArgumentException("The delay time '"+milliseconds+"' is negative.");
		}
	}
	/**
	 * @return int, the millisecond to delay the javascript's execution.
	 * @see #executeScript(boolean, String, Object...)
	 */
	public static int getJsExecutionDelay(){
		return jsExecutionDelay;
	}

	/**
	 * Execute a section of java-script code.<br>
	 * It has the ability to detect a javascript error and throw an JSException.<br>
	 * <pre>
	 * It is user's responsibility to set javascript global errorcode and errormessage in his
	 * javascript code, user can call
	 * {@link JavaScriptFunctions#setJSErrorCode(int)}
	 * {@link JavaScriptFunctions#setJSErrorObject(String)}
	 * for example, see {@link JavaScriptFunctions.SAP#sap_m_List_getItems(boolean)}.
	 * </pre>
	 * @see org.openqa.selenium.JavascriptExecutor#executeScript(String, Object...)
	 * @see org.openqa.selenium.JavascriptExecutor#executeAsyncScript(String, Object...)
	 * @throws SeleniumPlusException if there is no appropriate JavascriptExecutor to use.
	 */
	public static Object executeScript(boolean synch, String script, Object... args) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);

		if(jsExecutionDelay>0){
			IndependantLog.debug(debugmsg+" Delay JS execution '"+jsExecutionDelay+"' milliseconds ...");
			StringUtils.sleep(jsExecutionDelay);
		}

		try{
			getJS();
			//Reset the global error (code and message)
			js_cleanError();
			//Append the JS method 'throw_error()'
			script = JavaScriptFunctions.throw_error()+script;

			//Reset the global debug message array
			if(JavaScriptFunctions.jsDebugLogEnable) js_initJSDebugArray();
			//Append the JS method 'debug()'
			script = JavaScriptFunctions.debug()+script;

			if(JavaScriptFunctions.DEBUG_OUTPUT_JAVASCRIPT_FUNCTIONS){
				IndependantLog.debug(debugmsg+"Executing js \n[\n"+script);
			}

			Object object = synch? lastJS.executeScript(script, args) : lastJS.executeAsyncScript(script, args);
			if(object!=null){
				IndependantLog.debug(debugmsg+" Javascript returned "+object.getClass().getName()+": "+ object.toString());
			}else{
				IndependantLog.debug(debugmsg+" Javascript returned null.");
			}

			//Only check the 'jsErrorCode' for sync execution. For async execution, we don't even know if the execution finish or not.
			if(synch){
				int errorcode = js_getErrorCode();
				if(errorcode!=JavaScriptFunctions.ERROR_CODE_NOT_SET){
					Object error = js_getErrorObject();
					IndependantLog.error(debugmsg+" Fail. errorcode="+errorcode+" error="+error);
					throw JSException.instance(error, errorcode);
				}
			}

			IndependantLog.debug(debugmsg+" Javascript execution succeed. It is returning result ...");

			return object;

		}catch(SeleniumPlusException jse){
			IndependantLog.debug(debugmsg+ getThrowableMessages(jse));
			throw jse;
		}catch(Throwable th){
			// Carl Nagle: this is getting caught AFTER the attempt to return object; above is initiated!?
			IndependantLog.debug(debugmsg+ getThrowableMessages(th));
			throw new SeleniumPlusException(debugmsg, th);
		}finally{
			//Try to get the array of debug messages during the execution of 'javascript code'
			IndependantLog.debug(debugmsg+ "retrieving debug messages...");
			if(JavaScriptFunctions.jsDebugLogEnable){
				List<?> debugmessages = js_getJSDebugArray();
				for(Object msg:debugmessages){
					IndependantLog.debug(debugmsg+msg);
				}
			}
		}
	}

	/**
	 * @return JavascriptExecutor, the latest valid JavaSc
	 * @throws SeleniumPlusException
	 */
	public static JavascriptExecutor getJS() throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		if(lastJS==null && (lastUsedWD instanceof JavascriptExecutor)){
			IndependantLog.debug(debugmsg+"javascript executor is last Used WebDriver.");
			lastJS = (JavascriptExecutor) lastUsedWD;
		}
		if(lastJS==null){
			throw new SeleniumPlusException(debugmsg+"javascript executor is null.", SeleniumPlusException.CODE_NO_JS_EXECUTOR);
		}
		if(!lastJS.equals(lastUsedWD)){
			IndependantLog.debug(debugmsg+"javascript executor is obsolete.");
			refreshJSExecutor();
		}

		return lastJS;
	}
	/**
	 * Use the {@link #lastUsedWD} to refresh the {@link #lastJS}.
	 * @throws SeleniumPlusException if the {@link #lastUsedWD} cannot be casted to a JavascriptExecutor
	 */
	public static void refreshJSExecutor() throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "refreshJSExecutor");
		if(lastUsedWD instanceof JavascriptExecutor){
			lastJS = (JavascriptExecutor) lastUsedWD;
		}else{
			throw new SeleniumPlusException(debugmsg+"current webdriver cannot execute javascript.", SeleniumPlusException.CODE_NO_JS_EXECUTOR);
		}
	}

	/**
	 * Check that the object is not null.
	 * @param object	Object
	 * @throws SeleniumPlusException will be thrown out with {@link SeleniumPlusException#CODE_OBJECT_IS_NULL} if the object is null.
	 */
	public static void checkNotNull(Object object) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(SearchObject.class, "checkNotNull");
		if(object==null){
			String msg = "Object is null.";
			IndependantLog.error(debugmsg+msg);
			throw new SeleniumPlusException(msg, SeleniumPlusException.CODE_OBJECT_IS_NULL);
		}
	}


	public static final String CSS_SELECTOR_BY_ID = "CSS_SELECTOR_BY_ID";
	public static final String CSS_SELECTOR_BY_CLASSNAME = "CSS_SELECTOR_BY_CLASSNAME";
	/**
	 * According to the type, generate a css selector for the WebElement.
	 * @param type	String, the type of the css selector.
	 * @param element	WebElement, the WebElement from which a css selector will be generated.
	 * @return String, a css selector.
	 */
	public static String getCssSelector(String type, WebElement element){
		StringBuffer cssselector = new StringBuffer();
		String tagName = element.getTagName();

		if (CSS_SELECTOR_BY_CLASSNAME.equalsIgnoreCase(type)){
			String clazz = element.getAttribute(Component.ATTRIBUTE_CLASS);
			if(clazz!=null && !clazz.trim().isEmpty()){
				cssselector.append(tagName);
				cssselector.append("[class='"+clazz+"']");
			}
		}else if (CSS_SELECTOR_BY_ID.equalsIgnoreCase(type)){
			String id = element.getAttribute(Component.ATTRIBUTE_ID);
			if(id!=null && !id.trim().isEmpty()){
				cssselector.append("[id='"+id+"']");
			}
		}
		if(cssselector.toString().trim().isEmpty() && !CSS_SELECTOR_BY_ID.equalsIgnoreCase(type)){
			String id = element.getAttribute(Component.ATTRIBUTE_ID);
			if(id!=null && !id.trim().isEmpty()){
				cssselector.append("[id='"+id+"']");
			}
		}

		return cssselector.toString();
	}

	/**' '*/
	public static final String CSS_CLASS_SEPARATOR = " ";
	/**'sap'*/
	public static final String CSS_CLASS_SAP_PREFIX = "sap";
	/**'dijit'*/
	public static final String CSS_CLASS_DOJO_DIJIT_PREFIX = "dijit";

	/**
	 * Return a string to describe a selenium web element.<br>
	 * @param element WebElement
	 * @return String,
	 */
	static String getDescription(WebElement element){
		StringBuffer buffer = new StringBuffer();

		if(element!=null){
			buffer.append("<"+element.getTagName());
			String clazz = element.getAttribute(Component.ATTRIBUTE_CLASS);
			if(clazz!=null && !clazz.isEmpty()){
				buffer.append(" class='"+clazz+"'");
			}
			buffer.append("/>");
		}

		return buffer.toString();
	}

	/**
	 * Return the specified property of a WebElement.<br>
	 * For example, we can get the parentNode of a WebElement by calling this method with
	 * "parentNode" as the property.
	 * @param element WebElement
	 * @param property String property to retrieve.
	 * @return Object to be cast as needed by the specific request, can be null;
	 */
	public static Object getWebElementProperty(WebElement element, String property){
		Object o = null;
		try{ o = executeJavaScriptOnWebElement(
				"return arguments[0]."+ property +";", element);
		}catch(SeleniumPlusException x){
			IndependantLog.debug("SearchObject.getWebElementProperty "+ x.getClass().getSimpleName()+", "+x.getMessage());
		}
		return o;
	}

	/**
	 * Return the parent WebElement of the provided WebElement.<br>
	 * @param element WebElement
	 * @return WebElement parent or null if there is no parent, or an error occurred.
	 */
	public static WebElement getParentWebElement(WebElement element){
		WebElement e = null;
		if(element == null) return null;
		try{
			// TODO: what if the html is in a frame?  What do we see?
			if(element.getTagName().equalsIgnoreCase(HTML.TAG_HTML)) return null;
			e = element.findElement(By.xpath(".."));
		}catch(Exception ignore){}
		return e;
	}

	/**
	 * To check if the html tag's css class name containing a certain prefix.<br>
	 * @param element WebElement, The selenium web element
	 * @param prefix String, the prefix of the css class name
	 * @param separator String, the separator in the css class name, normally its a ' '
	 * @return boolean true if the css class name contains a certain prefix.
	 */
	static boolean checkCSSClassName(WebElement element, String prefix, String separator){
		boolean isSatisfied = false;

		try{
			String clazz = element.getAttribute(Component.ATTRIBUTE_CLASS);
			if(clazz!=null){
				isSatisfied= clazz.startsWith(prefix);
				// (Carl Nagle)if isSatisfied=true then we don't have to check any further, right?!!
				if(!isSatisfied){
					String[] classes = clazz.split(separator);
					// we only need to be satisfied on one class entry, not all class entries.
					for(String clz:classes){
						isSatisfied = clz.startsWith(prefix);
						if(isSatisfied) break;
					}
				}
			}
		}catch(Throwable th){
			IndependantLog.debug(StringUtils.debugmsg(SearchObject.class, "checkCSSClassName"),th);
		}

		return isSatisfied;
	}

	/**
	 * To test if the WebElement belongs to SAP OPENUI5 Domain.<br>
	 * @param element
	 * @return
	 * @throws SeleniumPlusException
	 */
	public static boolean isSAPDomain(WebElement element) throws SeleniumPlusException{
		checkNotNull(element);
		boolean issap = false;

		issap = SAP.isSapComponent(element);

		return issap;
	}

	/**
	 * To test if the WebElement belongs to DOJO Domain.<br>
	 * @param element
	 * @return
	 * @throws SeleniumPlusException
	 */
	public static boolean isDojoDomain(WebElement element) throws SeleniumPlusException{
		checkNotNull(element);
		boolean isdojo = false;

		isdojo = DOJO.isDijitComponent(element);

		return isdojo;
	}

	/**
	 * Generate a unique name. For example, it can be used as a javascript variable name<br>
	 * when calling functions in JavaScriptFunctions to avoid the conflict with variables defined<br>
	 * in the Application Under Test.<br>
	 *
	 * @param prefix String, the prefix of the unique name
	 * @return String, the unique name
	 * @deprecated This method has been moved to StringUtils, we can call <b>StringUtils.generateUniqueName()</b> directly.
	 */
	@Deprecated
	public static String generateUniqueName(String prefix){
		return StringUtils.generateUniqueName(prefix);
	}

//	/**
//	 * Generate a (hopefully) unique recognition string which might be:
//	 * <p><ul>
//	 * <li>Dojo recognition
//	 * <li>SAP recognition
//	 * <li>HTML recognition
//	 * <li>Other supported format ?
//	 * </ul<p>
//	 *
//	 * @param element
//	 * @return
//	 */
//	public static String generateSAFSRecognition(WebElement element){
//		String debugmsg = "SearchObject.generateSAFSRecognition: ";
//		boolean isDomain = false;
//		try{
//			isDomain = isDojoDomain(element);
//			if(isDomain) {
//				IndependantLog.info(debugmsg+"seeking DOJO recognition...");
//				return DOJO.getRecognition(element);
//			}
//		}
//		catch(Exception x){
//			IndependantLog.debug(debugmsg+"DOJO domain check: "+ x.getClass().getSimpleName()+", "+ x.getMessage());
//		}
//		if(!isDomain){
//			try{
//				isDomain = isSAPDomain(element);
//				if(isDomain) {
//					IndependantLog.info(debugmsg+"seeking SAP recognition...");
//					return SAP.getRecognition(element);
//				}
//			}
//			catch(Exception x){
//				IndependantLog.debug(debugmsg+"SAP domain check: "+ x.getClass().getSimpleName()+", "+ x.getMessage());
//			}
//		}
//		IndependantLog.info(debugmsg+"seeking generic HTML recognition...");
//		return HTML.getRecognition(element);
//	}

	//This method will return a SPCTreeNode contain "Recognition String", "Node's id", "Node's name"
//	public static SPCTreeNode generateSAFSRecognitionNode(WebElement element){
//		String rec = generateSAFSRecognition(element);
//		String id = element.getAttribute("id");
//		String name = element.getAttribute("name");
//		if (id==null) id = "";
//		if (name == null) name = "";
//		SPCTreeNode tree = new SPCTreeNode();
//		tree.setId(id);
//		tree.setName(name);
//		tree.setRecognitionString(rec);
//		return tree;
//	}

	/*
	 * used to store webelements in a cache when processing many elements for full xpathpaths
	 * that might be processed many iterative times.
	 */
	private static Hashtable<WebElement, String> xpathObjectCache = new Hashtable<>();
	/**
	 * reset the internal xpath object cache for a new round of full xpath generation.
	 * <p>
	 * This should be called by external routines getting ready to process an entire page
	 * of web elements to avoid processing the same parent nodes many times.
	 * This is strictly for performance reasons.
	 * <p>
	 * The routine should be called both BEFORE and AFTER the processing of nodes with the recursive
	 * generateGenericXPath routine.
	 * @see #generateFullGenericXPath(WebElement)
	 */
	public static void resetXPathObjectCache(){ xpathObjectCache.clear();}

	/**
	 * Attempt to generate a full generic Xpath string for this element including all parent
	 * path information.
	 * <p>
	 * Uses generateGenericXPath for each node in the hierarchy.
	 * <p>
	 * A caller getting ready to process many web elements--or even just one-should ALWAYS call resetXPathObjectCache
	 * before processing that first element and after processing the last element.
	 *
	 * @param element
	 * @return full xpath to object.  null if WebElement provided is null or the xpath cannot be deduced.
	 * @see #generateGenericXPath(WebElement)
	 * @see #resetXPathObjectCache()
	 */
	public static String generateFullGenericXPath(WebElement element){
		if(element == null) return null;
		if(xpathObjectCache.containsKey(element)) return xpathObjectCache.get(element);
		String xpath = generateGenericXPath(element);
		WebElement parent = getParentWebElement(element);
		String parentPath = null;
		if(parent != null) parentPath =generateFullGenericXPath(parent);
		if(parentPath !=null) xpath = parentPath +XPATH_CHILD_SEPARATOR+ xpath;
		xpathObjectCache.put(element, xpath);
		return xpath;
	}

	/**
	 * Attempt to generate a simple Xpath string for this one element without any parent
	 * path information.
	 * <p>
	 * Currently tries to differentiate the tag name by id, title, or class.
	 * <p>
	 * Examples:
	 * <p><ul>
	 * div[@id='myid']<br>
	 * div[@title='mytitle']<br>
	 * div[@class='myclass']<br>
	 * </ul>
	 * <p>
	 * if none of those have valid values, then only the tag name is returned. In that case,
	 * we check to see if it is the Nth sibling of the same tag type and add that, if needed.
	 *
	 * @param element
	 * @return
	 * @author Carl Nagle adding support for 'name' attribute if no other attribs were available
	 */
	public static String generateGenericXPath(WebElement element){
		String tag = element.getTagName();
		String attID = element.getAttribute("id");
		String attTITLE = element.getAttribute("title");
		String attCLASS = element.getAttribute("class");
		String att = (attID != null) && (attID.length()> 0) ? "[@id='"+attID+"']" :
			(attTITLE != null && attTITLE.length()> 0) ? "[@title='"+attTITLE+"']" :
				(attCLASS != null && attCLASS.length()> 0) ? "[@class='"+attCLASS+"']" :
					"";
		if( att.length()==0 ){
		    attID = element.getAttribute("name");
		    if(attID!=null && attID.length()>0)
		    	att = "[@name='"+ attID +"']";
		}
		List<WebElement> sibs = element.findElements(By.xpath("preceding-sibling::"+ tag + att));
		if(!sibs.isEmpty()){
			att += "["+String.valueOf(sibs.size()+1)+"]";
		}
		IndependantLog.info("SearchObject.generateGenericXPath made "+ tag + att);
		return tag + att;
	}

	/**
	 * Attempt to generate a simple frame recognition string without any parent path information.
	 * <p>
	 * Currently tries to differentiate the tag name by id, name, or index.
	 * @param framexpath from generateGenericXPath
	 * @return Examples:
	 * <p><ul>
	 * frameid='myid'<br>
	 * framename='myname'<br>
	 * frameindex='3'<br>
	 * </ul>
	 *
	 */
	public static String generateSAFSFrameRecognition(String framexpath){
		// processing examples:
		// frame
		// frame[2]
		// frame[@id='anid']
		// frame[@id='anid'][2]
		int i = framexpath.indexOf("[");
		String tag = framexpath;
		String att = "";
		if(i > 0){
			tag = framexpath.substring(0, i);
			try{
				att = framexpath.split("@")[1];
				String[] parts = att.split("=");
				att = parts[0];
				att += "=" + parts[1].split("'")[1];
			}catch(Exception badindex){
				att = framexpath.substring(i+1);
				try{
					i = att.indexOf("]");
					att = "index="+ att.substring(0,i);
				}catch(Exception ignore){}
			}
		}
		IndependantLog.info("SearchObject.generateSAFSFrameRecognition made "+ tag + att);
		return tag + att;
	}

	public static String generateSAFSFrameRecognition(WebElement node, String xpath){
		String frameRS = null;
		String t = node.getTagName();
		if(SearchObject.TAG_FRAME.equalsIgnoreCase(t) || SearchObject.TAG_IFRAME.equalsIgnoreCase(t)){
			if(xpath!=null){
				frameRS = generateSAFSFrameRecognition(xpath);
			}else{
				frameRS = generateSAFSFrameRecognition(generateGenericXPath(node));
			}
		}

		return frameRS;
	}

	/** <b>0</b> integer zero */
	public static final int MATCHED_ZERO_TIMES 	= 0;
	/** <b>1</b> integer one */
	public static final int MATCHED_ONE_TIME 	= 1;
	/** <b>false</b>*/
	public static final boolean DEFAULT_PARTIAL_MATCH = false;
	/** <b>true</b>*/
	public static final boolean DEFAULT_IGNORE_CASE = true;

	/**
	 * Switch to a certain window according to its title, this method will search it from all opened browsers.<br/>
	 * <b>NOTE:</b> Once successfully switched, {@link #lastUsedWD}, {@link #lastJS} may probably get changed.<br/>
	 * @param title String, the title of the window to switch to. It can be a normal string, a regexp string or a wildcard string.
	 *                      If it is regexp/wildcard string, the optional parameters will be ignored.
	 * @param optionals boolean[]
	 * <ul>
	 * <li>optionals[0] <b>partialMatch</b> boolean, if the title is provided as sub-string. The default value is false. It is valid ONLY when <b>title</b> is normal string;
	 * <li>optionals[1] <b>ignoreCase</b> boolean, if the title are case in-sensitive to compare. The default value is true. It is valid ONLY when <b>title</b> is normal string;
	 * </ul>
	 * @return boolean true if successfully switched to window matching parameter title
	 * @throws SeleniumPlusException
	 */
	public static boolean switchWindow(String title, boolean... optionals) throws SeleniumPlusException{
		return switchWindow(title, MATCHED_ONE_TIME, optionals);
	}
	/**
	 * Switch to a certain window according to its title, this method will search it from all opened browsers.<br/>
	 * <b>NOTE:</b> Once successfully switched, {@link #lastUsedWD}, {@link #lastJS} may probably get changed.<br/>
	 * @param title String, the title of the window to switch to. It can be a normal string, a regexp string or a wildcard string.
	 *                      If it is regexp/wildcard string, the optional parameters will be ignored.
	 * @param expectedMatchIndex int, the index of matched window if multiple windows match. These windows are ordered according to
	 *                                their title alphabet order. It is 1-based index and should be bigger than {@link #MATCHED_ZERO_TIMES}
	 * @param optionals boolean[]
	 * <ul>
	 * <li>optionals[0] <b>partialMatch</b> boolean, if the title is provided as sub-string. The default value is false. It is valid ONLY when <b>title</b> is normal string;
	 * <li>optionals[1] <b>ignoreCase</b> boolean, if the title are case in-sensitive to compare. The default value is true. It is valid ONLY when <b>title</b> is normal string;
	 * </ul>
	 * @return boolean true if successfully switched to window matching parameter title
	 * @throws SeleniumPlusException
	 */
	public static boolean switchWindow(String title, int expectedMatchIndex, boolean... optionals) throws SeleniumPlusException{
		List<String> browserIDList = new ArrayList<String>(webDrivers.keySet());
		Collections.sort(browserIDList);

		//The last-used-webdriver will be the first to search
		String lastUsedID = null;
		if(lastUsedWD!=null){
			lastUsedID = getIDForWebDriver(lastUsedWD);
		}
		if(lastUsedID!=null){
			browserIDList.remove(lastUsedID);
			browserIDList.add(0, lastUsedID);
		}

		boolean matched = false;
		int matchedTimes = MATCHED_ZERO_TIMES;

		for(String browserID: browserIDList){
			matchedTimes = switchWindow(browserID, title, expectedMatchIndex, optionals);
			if(expectedMatchIndex==matchedTimes){
				matched = true;
				break;
			}else{
				//We have already matched the window for 'matchedTimes', so extract 'matchedTimes' from 'expectedMatchIndex'
				//and try to find in another browser
				expectedMatchIndex = expectedMatchIndex - matchedTimes;
			}
		}

		return matched;
	}

	/**
	 * Switch to a certain window according to its title within browser identified by parameter 'browserID'.<br/>
	 * <b>NOTE:</b> Once successfully switched, {@link #lastUsedWD}, {@link #lastJS} may probably get changed.<br/>
	 * @param browserID String, the browser ID when calling {@link #startBrowser(String, String, String, int, boolean)}
	 * @param title String, the title of the window to switch to. It can be a normal string, a regexp string or a wildcard string.
	 *                      If it is regexp/wildcard string, the optional parameters will be ignored.
	 * @param optionals boolean[]
	 * <ul>
	 * <li>optionals[0] <b>partialMatch</b> boolean, if the title is provided as sub-string. The default value is false. It is valid ONLY when <b>title</b> is normal string;
	 * <li>optionals[1] <b>ignoreCase</b> boolean, if the title are case in-sensitive to compare. The default value is true. It is valid ONLY when <b>title</b> is normal string;
	 * </ul>
	 * @return boolean true if successfully switched
	 * @throws SeleniumPlusException
	 */
	public static boolean switchWindow(String browserID, String title, boolean... optionals) throws SeleniumPlusException{
		return switchWindow(browserID, title, MATCHED_ONE_TIME, optionals)==MATCHED_ONE_TIME;
	}

	/**
	 * Switch to a certain window according to its title within browser identified by parameter 'browserID'.<br/>
	 * <b>NOTE:</b> Once successfully switched, {@link #lastUsedWD}, {@link #lastJS} may probably get changed.<br/>
	 * @param browserID String, the browser ID when calling {@link #startBrowser(String, String, String, int, boolean)}
	 * @param title String, the title of the window to switch to. It can be a normal string, a regexp string or a wildcard string.
	 *                      If it is regexp/wildcard string, the optional parameters will be ignored.
	 * @param expectedMatchIndex int, the index of matched window if multiple windows match. These windows are ordered according to
	 *                                their title alphabet order. It is 1-based index and should be bigger than {@link #MATCHED_ZERO_TIMES}
	 * @param optionals boolean[]
	 * <ul>
	 * <li>optionals[0] <b>partialMatch</b> boolean, if the title is provided as sub-string. The default value is false. It is valid ONLY when <b>title</b> is normal string;
	 * <li>optionals[1] <b>ignoreCase</b> boolean, if the title are case in-sensitive to compare. The default value is true. It is valid ONLY when <b>title</b> is normal string;
	 * </ul>
	 * @return int the matched times.
	 * @throws SeleniumPlusException
	 */
	public static int switchWindow(String browserID, String title, int expectedMatchIndex, boolean... optionals) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);

		if(expectedMatchIndex<=MATCHED_ZERO_TIMES){
			throw new SeleniumPlusException("The expected Match Index should be bigger than "+MATCHED_ZERO_TIMES+".");
		}

		int matchedTimes = MATCHED_ZERO_TIMES;
		Entry<String/*window Handle*/, String/*window Title*/> originalWindow = null;
		if(lastUsedWD!=null){
			try{
				originalWindow = new SimpleEntry<>(lastUsedWD.getWindowHandle(), lastUsedWD.getTitle());
			}catch(NoSuchWindowException e){
				IndependantLog.warn(debugmsg+"Failed to create original window, due to "+e.toString());
			}
		}

		WebDriver driver = null;
		if(webDrivers.containsKey(browserID)){
			driver = webDrivers.get(browserID);
			if(driver==null){
				throw new SeleniumPlusException("Failed to get WebDriver for ID '"+browserID+"'", SeleniumPlusException.CODE_OBJECT_IS_NULL);
			}
		}

		boolean partialMatch = DEFAULT_PARTIAL_MATCH;
		boolean ignoreCase = DEFAULT_IGNORE_CASE;

		if(optionals!=null){
			if(optionals.length>0) partialMatch = optionals[0];
			if(optionals.length>1) ignoreCase = optionals[1];
		}

		Set<String> winHandles = driver.getWindowHandles();
		//driver.switchTo().window(currentTitle); does NOT work for title :-(, I have to create a pair to switch by handle
		List<Entry<String/*window Handle*/, String/*window Title*/>> windows = new ArrayList<>();
		for(String winHandle:winHandles){
			driver.switchTo().window(winHandle);
			windows.add(new SimpleEntry<String, String>(winHandle, driver.getTitle()));
		}
		Collections.sort(windows, new Comparator<Entry<String,String>>(){
			@Override
			public int compare(Entry<String, String> o1, Entry<String, String> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		for(Entry<String, String> window: windows){
			if(StringUtils.matchText(window.getValue(), title, partialMatch, ignoreCase)){
				if(expectedMatchIndex==++matchedTimes){
					driver.switchTo().window(window.getKey());
					break;
				}
			}
		}

		if(expectedMatchIndex==matchedTimes){
			//if we did get the matched window, we should switch the lastUsedWD, lastJS etc.
			useBrowser(browserID);
		}else{
			IndependantLog.warn(debugmsg+" failed to switch window by title '"+title+"' for browser indendified by '"+browserID+"'.'"
					+ "\n"+matchedTimes+"(matchedTimes)'!= '"+expectedMatchIndex+"'(expectedMatchIndex)");
			if(originalWindow!=null){
				try{
					IndependantLog.warn(debugmsg+"switching back to original window '"+originalWindow.getValue()+"'");
					lastUsedWD.switchTo().window(originalWindow.getKey());
				}catch(NoSuchWindowException e){
					IndependantLog.warn(debugmsg+" failed to switch back to original window '"+originalWindow.getValue()+"' by handle '"+originalWindow.getKey()+"', due to "+e.toString());
				}
			}
		}

		try{
			//as WebDriver.TargetLocator.window(): Switch the focus of future commands for this driver to the window with the given name/handle.
			//We have to call some command to get this window-switch happen immediately, TODO maybe we can use a method less time-consumed
			((TakesScreenshot)driver).getScreenshotAs(OutputType.BYTES);
		}catch(Exception e){
			IndependantLog.warn(debugmsg+" Failed to call selenium commmand to get the window-switch happen immediately, due to "+e.toString());
		}

		return matchedTimes;
	}

	/**
	 * Navigate to the previous page.
	 * @return boolean true if the navigation succeeds.
	 */
	public static boolean back(){
		try{
			getWebDriver().navigate().back();
			return true;
		}catch(Throwable e){
			IndependantLog.error(" Failed to navigate to previous page!", e);
			return false;
		}
	}

	/**
	 * Navigate to the next page.
	 * @return boolean true if the navigation succeeds.
	 */
	public static boolean forward(){
		try{
			getWebDriver().navigate().forward();
			return true;
		}catch(Throwable e){
			IndependantLog.error(" Failed to navigate to next page!", e);
			return false;
		}
	}

	public abstract static class SAP{
		/**
		 * To test if the WebElement represents a SAP OpenUI5 component.<br>
		 * @param element
		 * @return
		 * @throws SeleniumPlusException
		 */
		static boolean isSapComponent(WebElement element) throws SeleniumPlusException{
			checkNotNull(element);
			boolean isSAPUI5 = false;

			isSAPUI5 = checkCSSClassName(element, CSS_CLASS_SAP_PREFIX, CSS_CLASS_SEPARATOR);

			if(!isSAPUI5){
				IndependantLog.warn(getDescription(element)+", its css-class is not considered as a SAP OPENUI5 component, try get native class name.");
				try{
					String[] classes = WDLibrary.SAP.getSAPClassNames(element).toArray(new String[0]);
					for(String clazz:classes){
						if(clazz.startsWith(CSS_CLASS_SAP_PREFIX)){
							isSAPUI5 = true;
							break;
						}
					}
				}catch(Exception e){
					IndependantLog.warn("fail to get native class name, due to "+StringUtils.debugmsg(e));
				}
			}
			if(!isSAPUI5){
				IndependantLog.debug(getDescription(element)+" will not be considered as a SAP OPENUI5 component.");
			}

			return isSAPUI5;
		}

		/**
		 * Create a sap-specific recognition string we would use to find this element again.
		 * Currently this stub only returns {@link #DOMAIN_SAP}
		 * @param element
		 * @return
		 */
		public static String getRecognition(WebElement element) {
			// TODO: build SAP recognition string
			return "XPATH="+generateFullGenericXPath(element);
		}

		/**
		 * @param element WebElement
		 * @return List&lt;Accessibility>, a list of accessibility object
		 */
		public static List<Accessibility> getSAPAccessibleElementsInfo(WebElement element){
			String debugmsg = StringUtils.debugmsg(false);
			List<Accessibility> accessibilityList = new ArrayList<Accessibility>();

			StringBuffer jsScript = new StringBuffer();
			jsScript.append(JavaScriptFunctions.SAP.sap_getAccessibilityControllerElementsInfo(true));
			jsScript.append(" return sap_getAccessibilityControllerElementsInfo(arguments[0]);\n");

			try {
				Object accessibilities = executeJavaScriptOnWebElement(jsScript.toString(), element);

				//this call actually returns a List/Array of Objects/Maps?
				if(accessibilities instanceof Map){
					IndependantLog.debug(debugmsg+"received properties *IS* instanceof Map.");
					try{
						accessibilityList.add(new Accessibility(accessibilities));
					}catch(Throwable t){
						IndependantLog.debug(debugmsg+"failed to convert accessibilities object "+accessibilities+"\nMet exception "+t.toString());
					}
				}else if(accessibilities instanceof List){
					IndependantLog.debug(debugmsg+"received accessibilities *IS* instanceof List.");
					// should be an array of AccessibilityElementInfo Map objects
					List<?> accessibilityElementList = (List<?>) accessibilities;
					Object  accessibilityElement = null;
					for(int i=0;i<accessibilityElementList.size();i++){
						try{
							accessibilityElement = accessibilityElementList.get(i);
							accessibilityList.add(new Accessibility(accessibilityElement));
						}catch(Throwable t){
							IndependantLog.debug(debugmsg+"returned AccessibilityElement object is "+accessibilityElement+"\nFailed to convert it to Accessibility object, due to "+t);
						}
					}
				}else{
					if(accessibilities!=null) IndependantLog.debug("returned '"+accessibilities.getClass().getName()+"' accessibilities object "+accessibilities+"\nNeeds to handle in code.");
					else IndependantLog.debug(debugmsg+"returned accessibilities object is "+null);
				}

			} catch (SeleniumPlusException e) {
				IndependantLog.debug(debugmsg+" failed, Met exception.", e);
			}

			return accessibilityList;
		}

		/**
		 *
		 * @param element	WebElement, it MUST wrap a SAP object.
		 * @return	List<String> the class names of the SAP object and its superclass.
		 * @throws SeleniumPlusException
		 */
		public static List<String> getSAPClassNames(WebElement element) throws SeleniumPlusException{
			String debugmsg = StringUtils.debugmsg(SearchObject.SAP.class, "getSAPClassNames");
			Object classNames = null;
			List<String> classNameList = new ArrayList<String>();

			//To save time, if the element does not represent a SAP object, do not execute javascript to get the SAP class name
			//Lei comment this check, some times we need call getSAPClassNames() to get native class to tell the domain, following calling is a dead-loop.
//			if(!isSAPDomain(element)) throw new SeleniumPlusException(debugmsg+"Not SAP Object!");

			String id = element.getAttribute(Component.ATTRIBUTE_ID);
			if(id!=null){
				classNames = getSAPClassNamesById(id);
			}

			//TODO get SAP Class names by 'CSS selector'
			//		if(className==null && id!=null){
			//			StringBuffer cssselector = new StringBuffer();
			//			cssselector.append("[id='"+id+"']");
			//			className = getSAPClassNamesByCSSSelector(cssselector.toString());
			//		}
			//
			//		if(className==null){
			//			String clazz = element.getAttribute(Component.ATTRIBUTE_CLASS);
			//			if(clazz!=null){
			//				//http://dojotoolkit.org/reference-guide/1.9/dojo/query.html
			//				//http://www.w3schools.com/cssref/css_selectors.asp
			//				String cssselector = getCssSelector(CSS_SELECTOR_BY_CLASSNAME, element);
			//				className = getSAPClassNamesByCSSSelector(cssselector);
			//			}
			//		}

			if(classNames==null){
				throw new SeleniumPlusException(debugmsg+" cannot find SAP class name.");
			}
			//handle the class names,
			if(classNames instanceof List) classNameList.addAll(((List)classNames));
			else if(classNames instanceof String) classNameList.add((String) classNames);
			else{
				IndependantLog.debug("object return by javascript has type as "+classNames.getClass().getName());
			}

			return classNameList;
		}

		/**
		 *
		 * @param element	WebElement, it MUST wrap a SAP object.
		 * @return	String the class name of the SAP object.
		 * @throws SeleniumPlusException
		 */
		public static String getSAPClassName(WebElement element) throws SeleniumPlusException{
			String debugmsg = StringUtils.debugmsg(SearchObject.SAP.class, "getSAPClassName");
			Object className = null;

			//To save time, if the element does not represent a SAP object, do not execute javascript to get the SAP class name
			if(!isSAPDomain(element)){
				throw new SeleniumPlusException(debugmsg+"Not SAP Object!");
			}

			String id = element.getAttribute(Component.ATTRIBUTE_ID);
			if(id!=null){
				className = getSAPClassNameById(id);
			}

			//TODO get SAP Class name by 'CSS selector'
			//		if(className==null && id!=null){
			//			StringBuffer cssselector = new StringBuffer();
			//			cssselector.append("[id='"+id+"']");
			//			className = getSAPClassNameByCSSSelector(cssselector.toString());
			//		}
			//
			//		if(className==null){
			//			String clazz = element.getAttribute(Component.ATTRIBUTE_CLASS);
			//			if(clazz!=null){
			//				//http://dojotoolkit.org/reference-guide/1.9/dojo/query.html
			//				//http://www.w3schools.com/cssref/css_selectors.asp
			//				String cssselector = getCssSelector(CSS_SELECTOR_BY_CLASSNAME, element);
			//				className = getSAPClassNameByCSSSelector(cssselector);
			//			}
			//		}

			if(className==null){
				throw new SeleniumPlusException(debugmsg+" cannot find SAP class name.");
			}
			//handle the class names,
			if(className instanceof String) return (String)className;
			else{
				IndependantLog.warn("object return by javascript has type as "+className.getClass().getName());
				return "";
			}
		}

		/**
		 * Test if the element is supported. Supported means that the element is class of subclass of one
		 * of the supported class names.<br>
		 * @param element WebElement, the element to check.
		 * @param supportedClassNames String[], the supported class names
		 */
		public static boolean isSupported(WebElement element, String[] supportedClassNames){

			StringBuffer jsScript = new StringBuffer();
			jsScript.append(JavaScriptFunctions.SAP.sap_objectIsInstanceof(true));
			String varClassArray = JavaScriptFunctions.initializeJSArray(jsScript, false, "  ", 0, supportedClassNames);
			jsScript.append("  return sap_objectIsInstanceof(arguments[0],"+varClassArray+");\n");
			try {
				Object obj = executeJavaScriptOnWebElement(jsScript.toString(), element);
				if(obj instanceof Boolean) return ((Boolean)obj).booleanValue();
			} catch (SeleniumPlusException e) {
				IndependantLog.debug(" Met exception.",e);
			}

			boolean supported = false;
			String className = null;
			try {
				className = WDLibrary.SAP.getSAPClassName(element);
				for(String supportedClassName: supportedClassNames){
					if(supported = supportedClassName.equals(className)) break;
				}
			} catch (SeleniumPlusException e) {
				IndependantLog.debug(className==null? "Not supported.":className+" has not been supported.", e);
			}

			return supported;
		}

		/**
		 * <b>Attention:</b>
		 * Do <font color='red'>NOT</font> call this method directly!<br>
		 * The DOJO object is very big, selenium will spend a lot of time to convert it.<br>
		 * Calling this will make your program looks like dead.<br>
		 *
		 * TODO add timeout, maybe.
		 *
		 * @param element WebElement represents a DOJO object on page.
		 * @return Object, Selenium converted DOJO object.
		 */
		protected static Object toSAPObject(WebElement element){
			String debugmsg = StringUtils.debugmsg(SearchObject.SAP.class, "toDojoObject");
			Object obj = null;

			StringBuffer jsScript = new StringBuffer();
			jsScript.append(JavaScriptFunctions.SAP.sap_getObjectById());
			jsScript.append(" return sap_getObjectById(arguments[0]);");

			String id = element.getAttribute(Component.ATTRIBUTE_ID);
			if(id!=null){
				try {
					IndependantLog.debug("The parameter id is '"+id+"'");
					obj = executeScript(jsScript.toString(), id);
				} catch (SeleniumPlusException e) {
					IndependantLog.debug(debugmsg+" cannot get SAP object by id '"+id+"'",e);
				}
			}

			if(obj==null){
				IndependantLog.debug(debugmsg+" cannot get SAP object");
			}

			return obj;
		}

		/**
		 * @param id	String, the HTML id for an SAP element on page.
		 * @return	Object, the SAP class names or null.
		 */
		protected static Object getSAPClassNamesById(String id){
			String debugmsg = StringUtils.debugmsg(SearchObject.SAP.class, "getSAPClassNamesById");
			StringBuffer jsScript = new StringBuffer();

			if(id!=null && !id.trim().isEmpty()){
				jsScript.append(JavaScriptFunctions.SAP.getSAPClassNamesById(true));
				String arrayName = generateUniqueName("arrayOfClassName");
				jsScript.append(arrayName+" = new Array();");
				jsScript.append(" getSAPClassNamesById(arguments[0], "+arrayName+");");
				jsScript.append(" return "+arrayName+";");
				try {
					IndependantLog.debug("The parameter id is '"+id+"'");
					Object obj = executeScript(jsScript.toString(), id);
					return obj;
				} catch (SeleniumPlusException e) {
					IndependantLog.debug(debugmsg+" cannot get SAP class names by id '"+id+"'",e);
				}
			}
			return null;
		}

		/**
		 * @param id	String, the HTML id for an SAP element on page.
		 * @return	Object, the SAP class name or null.
		 */
		protected static Object getSAPClassNameById(String id){
			String debugmsg = StringUtils.debugmsg(SearchObject.SAP.class, "getSAPClassNameById");
			StringBuffer jsScript = new StringBuffer();

			if(id!=null && !id.trim().isEmpty()){
				jsScript.append(JavaScriptFunctions.SAP.getSAPClassNameById(true));
				jsScript.append(" return getSAPClassNameById(arguments[0]);");
				try {
					IndependantLog.debug("The parameter id is '"+id+"'");
					Object obj = executeScript(jsScript.toString(), id);
					return obj;
				} catch (SeleniumPlusException e) {
					IndependantLog.debug(debugmsg+" cannot get SAP class name by id '"+id+"'",e);
				}
			}
			return null;
		}
	}

	/*******************************************************
	 * Inner class to handle DOJO-specific functionality.   *
	 *******************************************************/
	public abstract static class DOJO{

		/** "DOJOTabControl" */
		public static final String DOJOTABCONTROL = "DOJOTABCONTROL";
		/** "DOJOToolbar" */
		public static final String DOJOTOOLBAR    = "DOJOTOOLBAR";

		/**
		 * When executed, should return a Map object with keys "dojo", "dijit", and "dojox".<br>
		 * Dojo libraries not present in the browser will not have keys in the Map.<br>
		 * Values of these keys are usually "dojo", "dijit", and "dojox"; respectively.
		 * If Dojo is not running in the browser, the returned Map will be null.<br>
		 * If there is a Dojo djConfig.scopeMap in the browser, the values returned will be
		 * whatever the mapped values are. Example: "dojo"="mappedDojo", "dijit"="mappedDijit", etc..
		 * @return Map of dojo reference scopeMappings, or null if Dojo is not running in the browser.
		 */
		@SuppressWarnings("rawtypes")
		public static Map getDojoScopemap(){

			String script = JavaScriptFunctions.DOJO.getDojoScopemap()+"return getDojoScopemap();\n";
			Map result = null;
			try{ result = (Map)WDLibrary.executeScript(script);}catch(Exception ignore){}
			if(result != null){
				IndependantLog.error("SearchObject.DOJO.getDojoScopemap retrieved "+ result.getClass().getName()+": "+ result.toString());
				if(result.containsKey(JavaScriptFunctions.DOJO.DOJO_KEY))
					JavaScriptFunctions.DOJO.dojo = "window."+result.get(JavaScriptFunctions.DOJO.DOJO_KEY).toString();
				else
					JavaScriptFunctions.DOJO.dojo = JavaScriptFunctions.DOJO.DOJO_DEFAULT;
				if(result.containsKey(JavaScriptFunctions.DOJO.DIJIT_KEY))
					JavaScriptFunctions.DOJO.dijit = "window."+result.get(JavaScriptFunctions.DOJO.DIJIT_KEY).toString();
				else
					JavaScriptFunctions.DOJO.dijit = JavaScriptFunctions.DOJO.DIJIT_DEFAULT;
				if(result.containsKey(JavaScriptFunctions.DOJO.DOJOX_KEY))
					JavaScriptFunctions.DOJO.dojox = "window."+result.get(JavaScriptFunctions.DOJO.DOJOX_KEY).toString();
				else
					JavaScriptFunctions.DOJO.dojox = JavaScriptFunctions.DOJO.DOJOX_DEFAULT;
			}else{
				IndependantLog.error("SearchObject.DOJO.getDojoScopemap returned null.");
				JavaScriptFunctions.DOJO.dojo = JavaScriptFunctions.DOJO.DOJO_DEFAULT;
				JavaScriptFunctions.DOJO.dijit = JavaScriptFunctions.DOJO.DIJIT_DEFAULT;
				JavaScriptFunctions.DOJO.dojox = JavaScriptFunctions.DOJO.DOJOX_DEFAULT;
			}
			return result;
		}

		/**
		 * To test if the WebElement represents a Dojo.Dijit component.<br>
		 * @param element
		 * @return
		 * @throws SeleniumPlusException
		 */
		static boolean isDijitComponent(WebElement element) throws SeleniumPlusException{
			checkNotNull(element);
			boolean isdijit = false;

			isdijit = checkCSSClassName(element, CSS_CLASS_DOJO_DIJIT_PREFIX, CSS_CLASS_SEPARATOR);
			//TODO Does attribute 'widgetid' must exist?
			//if attribute 'widgetid' exists, then we consider it as a dijit object.
			if(isdijit) isdijit &= (element.getAttribute(Component.ATTRIBUTE_WIDGETID)!=null);

			//Lei, for future use; For now, to save time, just comment it.
			//if checkCSSClassName() cannot detect the domain, we can use following codes.
			/*
			if(!isdijit){
				IndependantLog.warn(getDescription(element)+", its css is not considered as a dijit component, try get dojo native class name.");
				try{
					String clazz = WDLibrary.DOJO.getDojoClassName(element);
					if(clazz.startsWith(CSS_CLASS_DOJO_DIJIT_PREFIX)) isdijit = true;

				}catch(Exception e){
					IndependantLog.warn("fail get dojo native class name, due to "+StringUtils.debugmsg(e));
				}
			}
			*/

			if(!isdijit){
				IndependantLog.debug(getDescription(element)+" will not be considered as a dijit component.");
			}

			return isdijit;
		}

		/**
		 * Attempt to process a DOJO recognition string like:
		 * DOJOTabControl;id=mainTabs, or DOJOToolbar;id=toolbar, etc...
		 * <p>
		 * Currently supported Types:
		 * <p>
		 * <ul>
		 * DOJOTabControl<br>
		 * DOJOToolbar<br>
		 * </ul>
		 * <p>
		 * Currently supported qualifiers:
		 * <p>
		 * <ul>
		 * id=<br>
		 * name=<br>
		 * title=<br>
		 * text=<br>
		 * index=<br>
		 * </ul>
		 * @param sc
		 * @param rst
		 * @return SearchContext (WebDriver/WebElement) or null
		 */
		public static SearchContext findElement(SearchContext sc, String rst) {
			//search the target element within a frame (if exist) level by level
			SearchContext obj = null;
			String debugmsg = "SearchObject.DOJO.findElement ";
			List<WebElement> preMatches = null;
			IndependantLog.debug(debugmsg +"processing recognition fragment: "+ rst);
			int matchindex = 1;
			String[] st = StringUtils.getTokenArray(rst, qulifierSeparator, escapeChar);
			String xpathStr = ""; // current DOJO always uses DIV as the high-level widget element

			try{
				String type = null;
				String value = null;
				int i = -1;
				for (String prop: st){
					value = null;
					i = prop.indexOf(assignSeparator);
					if (i > -1){
						type = prop.substring(0, i);
						if(prop.length()> i) value = prop.substring(i+1);
					}else{
						type = prop;
					}
					if (DOJOTABCONTROL.equalsIgnoreCase(type)){
						xpathStr = "//div[contains(concat(' ',@class,' '),' dijitTabContainer ')";
					}else if (DOJOTOOLBAR.equalsIgnoreCase(type)){
						xpathStr = "//div[contains(concat(' ',@class,' '),' dijitToolbar ')";
					}else if (SEARCH_CRITERIA_ID.equalsIgnoreCase(type)){
						xpathStr += " and (starts-with(@id,'"+value+"') or "+
								"(substring(@id, string-length(@id) - string-length('"+value+"')+1)='"+value+"'))";
					}else if (SEARCH_CRITERIA_NAME.equalsIgnoreCase(type)){
						xpathStr += " and (starts-with(@name,'"+value+"') or "+
								"(substring(@name, string-length(@name) - string-length('"+value+"')+1)='"+value+"'))";
					}else if (SEARCH_CRITERIA_TITLE.equalsIgnoreCase(type)){
						xpathStr += " and (starts-with(@title,'"+value+"') or "+
								"(substring(@title, string-length(@title) - string-length('"+value+"')+1)='"+value+"'))";
					}else if (SEARCH_CRITERIA_TEXT.equalsIgnoreCase(type)){
						xpathStr += " and .='"+value+"'";
					}else if (SEARCH_CRITERIA_INDEX.equalsIgnoreCase(type)){
						matchindex = StringUtilities.parseIndex(value);
					}
				}
				xpathStr += "]";
				String xpath = String.format(xpathStr);
				IndependantLog.debug(debugmsg +"resulting xpath: "+ xpathStr);

				preMatches = sc.findElements(By.xpath(xpath));

				obj = getNthWebElement(preMatches, matchindex, null);

			}catch(Throwable t){
				IndependantLog.debug(debugmsg +"recognition string parsing error: "+ t.getClass().getSimpleName()+": "+t.getMessage());
			}
			if(obj == null)
				IndependantLog.debug(debugmsg +"did not find any matching elements.");
			return obj;
		}

		/**
		 * Create a dojo-specific recognition string we would use to find this element again.
		 * Currently this stub only returns {@link #DOMAIN_DOJO}
		 * @param element
		 * @return
		 */
		public static String getRecognition(WebElement element) {
			// TODO: build DOJO recognition string
			return "XPATH="+generateFullGenericXPath(element);
		}

		/**
		 *
		 * @param element	WebElement, it MUST wrap a DOJO object.
		 * @return	String the class name of the DOJO object.
		 * @throws SeleniumPlusException
		 */
		public static String getDojoClassName(WebElement element) throws SeleniumPlusException{
			String debugmsg = StringUtils.debugmsg(SearchObject.DOJO.class, "getDojoClassName");
			Object className = null;

			//To save time, if the element does not represent a DOJO object, do not execute javascript to get the dojo class name
			//Lei comment this check, some times we need call getSAPClassNames() to get native class to tell the domain, following calling is a dead-loop.
//			if(!isDojoDomain(element)) throw new SeleniumPlusException(debugmsg+"Not Dojo Object!");

			try{
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(JavaScriptFunctions.DOJO.getDojoClassName(true));
				jsScript.append("return getDojoClassName(arguments[0]);");
				className = SearchObject.executeJavaScriptOnWebElement(jsScript.toString(), element);
			}catch(SeleniumPlusException e){
				IndependantLog.debug(debugmsg+"Fail to get classname by selenium webelement.",e);
			}

			if(className==null){
				String id = element.getAttribute(Component.ATTRIBUTE_ID);
				if(id!=null) className = getDojoClassNameById(id);

				if(className==null && id!=null){
					StringBuffer cssselector = new StringBuffer();
					cssselector.append("[id='"+id+"']");
					className = getDojoClassNameByCSSSelector(cssselector.toString());
				}
			}

			if(className==null){
				String clazz = element.getAttribute(Component.ATTRIBUTE_CLASS);
				if(clazz!=null){
					//http://dojotoolkit.org/reference-guide/1.9/dojo/query.html
					//http://www.w3schools.com/cssref/css_selectors.asp
					String cssselector = getCssSelector(CSS_SELECTOR_BY_CLASSNAME, element);
					className = getDojoClassNameByCSSSelector(cssselector);
				}
			}

			if(className==null){
				throw new SeleniumPlusException(debugmsg+" cannot find dojo class name.");
			}

			return className.toString();
		}

		public static boolean isSupported(WebElement element, String[] supportedClassNames){

			StringBuffer jsScript = new StringBuffer();
			jsScript.append(JavaScriptFunctions.DOJO.dojo_objectIsInstanceof(true));
			String varClassArray = JavaScriptFunctions.initializeJSArray(jsScript, false, "  ", 0, supportedClassNames);
			jsScript.append("  return dojo_objectIsInstanceof(arguments[0],"+varClassArray+");\n");

			try {
				Object obj = executeJavaScriptOnWebElement(jsScript.toString(), element);
				if(obj instanceof Boolean) return ((Boolean)obj).booleanValue();
			} catch (SeleniumPlusException e) {
				IndependantLog.debug(" Met exception.",e);
			}

			boolean supported = false;
			String className = null;
			try {
				className = WDLibrary.DOJO.getDojoClassName(element);
				for(String supportedClassName: supportedClassNames){
					if(supported = supportedClassName.equals(className)) break;
				}
			} catch (SeleniumPlusException e) {
				IndependantLog.debug(className==null? "Not supported.":className+" has not been supported.", e);
			}

			return supported;
		}

		/**
		 * <b>Attention:</b>
		 * Do <font color='red'>NOT</font> call this method directly!<br>
		 * The DOJO object is very big, selenium will spend a lot of time to convert it.<br>
		 * Calling this will make your program looks like dead.<br>
		 *
		 * TODO add timeout, maybe.
		 *
		 * @param element WebElement represents a DOJO object on page.
		 * @return Object, Selenium converted DOJO object.
		 */
		protected static Object toDojoObject(WebElement element){
			String debugmsg = StringUtils.debugmsg(SearchObject.DOJO.class, "toDojoObject");
			Object obj = null;

			StringBuffer jsScript = new StringBuffer();
			jsScript.append(JavaScriptFunctions.DOJO.getDojoObject(true));
			jsScript.append(" return getDojoObject(arguments[0]);");

			try {
				obj = executeScript(jsScript.toString(), element);
			} catch (SeleniumPlusException e) {
				IndependantLog.debug(debugmsg+" cannot get dojo object ",e);
			}

			if(obj==null){
				IndependantLog.debug(debugmsg+" cannot get dojo object");
			}

			return obj;
		}

		/**
		 * @param id	String, the HTML id for an DOJO element on page.
		 * @return	String, the DOJO class name or null.
		 */
		protected static String getDojoClassNameById(String id){
			String debugmsg = StringUtils.debugmsg(SearchObject.DOJO.class, "getDojoClassNameById");
			StringBuffer jsScript = new StringBuffer();

			if(id!=null && !id.trim().isEmpty()){
				jsScript.append(JavaScriptFunctions.DOJO.getDojoClassNameById(true));
				jsScript.append(" return getDojoClassNameById(arguments[0]);");
				try {
					IndependantLog.debug("The parameter id is '"+id+"'");
					Object obj = executeScript(jsScript.toString(), id);
					if(obj instanceof String) return obj.toString();
				} catch (SeleniumPlusException e) {
					IndependantLog.debug(debugmsg+" cannot get dojo class name by id '"+id+"'",e);
				}
			}
			return null;
		}

		/**
		 * @param cssselector	String, the 'CSS Selector' for an DOJO element on page, it can be used to find the DOM object.
		 *              The 'CSS Selector' can be something like table[@class='dijitReset dijitStretch dijitButtonContents']
		 * @return	String, the DOJO class name or null.
		 */
		protected static String getDojoClassNameByCSSSelector(String cssselector){
			String debugmsg = StringUtils.debugmsg(SearchObject.DOJO.class, "getDojoClassNameByCSSSelector");
			StringBuffer jsScript = new StringBuffer();

			if(cssselector!=null && !cssselector.trim().isEmpty()){
				jsScript.append(JavaScriptFunctions.DOJO.getDojoClassNameByCSSSelector(true));
				jsScript.append(" return getDojoClassNameByCSSSelector(arguments[0]);");
				try {
					IndependantLog.debug("The parameter cssselector is '"+cssselector+"'");
					Object obj = executeScript(jsScript.toString(), cssselector);
					if(obj instanceof String) return obj.toString();
				} catch (SeleniumPlusException e) {
					IndependantLog.debug(debugmsg+" cannot get dojo class name by xpath '"+cssselector+"'",e);
				}
			}
			return null;
		}
	}

	/*
	 * Used to get a streamlined set of messages including causes from the Throwable.
	 * Without all the Other Selenium stuff regarding Capabilities, etc..
	 * @param th
	 * @return streamlined set of messages including causes from the Throwable.
	 */
	protected static String getThrowableMessages(Throwable th){
		StringBuffer buffer = new StringBuffer();
		buffer.append(th.getClass().getSimpleName()+": "+th.getMessage()+". \n");
		Throwable cause = th.getCause();
		while(cause != null){
			buffer.append("Caused by: "+ cause.getClass().getSimpleName()+": "+th.getMessage()+". \n");
			cause = cause.getCause();
		}
		return buffer.toString();
	}

	public abstract static class HTML{
		/** 'html' */
		public static final String TAG_HTML = "html";
		public static boolean isSupported(WebElement element, String[] supportedClassNames){
			String[] actualClassNames = html_getClassName(element);
			for(String clazzname: supportedClassNames){
				for(String actualClass: actualClassNames){
					if( clazzname.equalsIgnoreCase(actualClass)) return true;
				}
			}
			return false;
		}

		/**
		 * Return an array of classname, the order is important. They will be used to get<br>
		 * an appropriate type. The first classname in the array will be tried firstly, if<br>
		 * no type can be got then try next one until a type is returned.<br>
		 * @param element
		 * @return
		 * @throws SeleniumPlusException
		 */
		public static String[] html_getClassName(WebElement element){
			String debugmsg = StringUtils.debugmsg(HTML.class, "html_getClassName");
			List<String> classNames = new ArrayList<String>();

			String cssClassName = element.getAttribute(Component.ATTRIBUTE_CLASS);
			if(cssClassName!=null) classNames.add(cssClassName);

			String tag = element.getTagName();
			if(tag==null) IndependantLog.warn(debugmsg+" cannot get HTML tag name.");

			//For html input tag, use its type as class name.
			if(Component.TAG_HTML_INPUT.equalsIgnoreCase(tag)){
				String type = element.getAttribute(Component.ATTRIBUTE_TYPE);
				if(type!=null) classNames.add(type);
			}

			if(tag!=null)classNames.add(tag);

			return classNames.toArray(new String[0]);// cssClassName, input-type ,tagName
		}

		public static String getHTMLClassName(WebElement element){
			String[] classes = html_getClassName(element);
			if(classes != null && classes.length > 0) return classes[0];
			return TAG_HTML; // default?
		}

		/**
		 * Create a generic HTML(5) recognition string we would use to find this element again.
		 * Currently this stub only returns {@link #SEARCH_CRITERIA_XPATH}
		 * @param element
		 * @return
		 */
		public static String getRecognition(WebElement element) {
			return "XPATH="+generateFullGenericXPath(element);
		}

	}

}
