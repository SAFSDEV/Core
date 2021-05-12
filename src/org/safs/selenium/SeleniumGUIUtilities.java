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
 * @author Carl Nagle, PHSABO FEB 06, 2007 - Fixed getWindowID and getXPath calls
 * @author Carl Nagle  FEB 07, 2007 - prepend Type=HTML; if no type specified in recString
 *                               in getXPathString function.
 * @author Carl Nagle  FEB 01, 2010 - moved ComponentFunctions.DLL usage here until it is factored out
 * @author Lei Wang MAR 31, 2011 - Use DocumentParser to parse HTML page instead of user-extensions.js
 * @author Lei Wang JUN 01, 2011 - In method getGuiObject():  initialize domParser before using it.
 *                                In method normalizeXPath(): Modify xpath so that it begins with string "//".
 * @author Lei Wang JUN 21, 2011 - Modify method getDocumentParser(): If selenium switches between frame,
 *                                we will not modify the main url stored in domParser.
 *                                Overload method setWindowFocus() and maximizeWindow(): use selenium's API
 *                                instead of native call to to the job.
 * @author Lei Wang JUN 28, 2011 - Add method getAttribute() and getAttributes().
 * @author Lei Wang JUL 27, 2017 - Added method getWebDriver(): Use Java reflection to get WebDriver from WebDriverBackedSelenium,
 *                                                             in this way we can avoid the conflict between Selenium 1.0 and 2.0
 *
 */
package org.safs.selenium;

import java.awt.Rectangle;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.openqa.selenium.WebDriver;
//selenium 1.0, the class 'WebDriverBackedSelenium' is in package 'org.openqa.selenium'
//import org.openqa.selenium.WebDriverBackedSelenium;
//From selenium 2.42, the class 'WebDriverBackedSelenium' has been moved to another package
//import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;
import org.safs.ApplicationMap;
import org.safs.DDGUIUtilities;
import org.safs.IndependantLog;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSObjectNotFoundException;
import org.safs.STAFHelper;
import org.safs.StringUtils;
import org.safs.TestRecordData;
import org.safs.Tree;
import org.safs.logging.LogUtilities;
import org.safs.selenium.util.HtmlFrameComp;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;

/**
 * SAFS/Selenium-specific subclass of DDGUIUtilities.
 * The primary feature is that the SeleniumGUIUtilities uses the new SAFS RMI bridge to
 * communicate with our own Java Proxies embedded in each JVM.
 *
 * @author  Carl Nagle, PHSABO
 * @since   AUG 15, 2006
 * @see org.safs.DDGUIUtilities
 **/
@SuppressWarnings("deprecation")
public class SeleniumGUIUtilities extends DDGUIUtilities {

	private long gSecTimeout;
//	protected final Pattern framePattern = Pattern.compile("(//.*FRAME\\[.+?\\])//.+");
	public static final Pattern FRAME_PATTERN = Pattern.compile("(/.*FRAME(\\[.+?\\])?)(/.+)?");
	public boolean frameSwitched = false;
	public String UNNAMED_WINDOW = "null";
	public DocumentParser domParser = null;

	//This map contains the pairs(frameXpath, HtmlFrameComp)
	//The frameXpath should be counted from the "root url" of DocumentParser
	private Map<String,HtmlFrameComp> frames = new TreeMap<String,HtmlFrameComp>(new Comparator<String>(){
		@Override
		public int compare(String o1, String o2) {
			//We just compare the length of the frameXpath, put the longest at the first, shortest at the last, it is descending
			//As TreeMap will be in ascending key order, so inverse the compare calculation.
			int compareInt = o2.length()-o1.length();
			if(compareInt==0){
				compareInt = o2.compareTo(o1);
			}
			return compareInt;
		}
	});

	/**
	 * No-argument constructor.
	 * Simply calls super()
	 * @see DDGUIUtilities#DDGUIUtilities()
	 */
	public SeleniumGUIUtilities() {
		super();
	}

	/**
	 * Constructor providing the STAFHelper and LogUtilities needed for
	 * proper operation.
	 * @param helper The STAFHelper for performing STAF requests.
	 * @param log the LogUtilities for logging.
	 * @see org.safs.STAFHelper
	 * @see org.safs.logging.LogUtilities
	 **/
	public SeleniumGUIUtilities(STAFHelper helper, LogUtilities log) {
		super();
		setLogUtilities(log); //must be first
		setSTAFHelper(helper);
	}

	/**
	 * Constructor providing the STAFHelper, LogUtilities, and TestRecordData for proper operation.
	 * The SAFS/Selenium engine generally uses an instanceof STestRecordHelper for TestRecordData.
	 *
	 * @param helper The STAFHelper for performing STAF requests.
	 * @param data the TestRecordData generally an instanceof STestRecordHelper.
	 * @param log the LogUtilities for logging.
	 * @see org.safs.STAFHelper
	 * @see STestRecordHelper
	 * @see org.safs.logging.LogUtilities
	 **/
	public SeleniumGUIUtilities(STAFHelper helper,TestRecordData data, LogUtilities log) {
		this(helper, log);
		setTestRecordData(data);
	}

	/**
	 * Uses the Application Map caching mechanism provided by the superclass.
	 * Retrieves cached references or recognition strings from the App Map and attempts to
	 * identify the matching component via the remote AUT JVM proxies.
	 * <p>
	 * @param appMapName  the name/ID of the App Map currently in use.
	 * @param windowName  the name/ID of the window as predefined in the App Map.
	 * @param compName    the name/ID of the child component of the window as predefined
	 * in the App Map.
	 * @param secTimeout the number of seconds allowed to located the object before a
	 * SAFSObjectNotFoundException is thrown.
	 *
	 * @return 0 on success. throw SAFSObjectNotFoundException if not successful.
	 * @throws SAFSObjectNotFoundException if specified parent or child cannot be found.
	 * @see org.safs.DDGUIUtilities#waitForObject(String,String,String,long)
	 */
	@Override
	public int waitForObject (String appMapName, String windowName, String compName, long secTimeout)
	                           throws SAFSObjectNotFoundException {

		gSecTimeout = secTimeout;
		ApplicationMap map = null;
		STestRecordHelper trdata = (STestRecordHelper) this.trdata;
		Selenium selenium = SApplicationMap.getSelenium(windowName);

	    try{
	    	Log.info("SGU: Looking for "+windowName+"."+compName+" using AppMap:"+appMapName);
	    	map = getAppMap(appMapName);

		    if (map == null) {
		      if (registerAppMap(appMapName, appMapName)) {
		        map = getAppMap(appMapName);
		        if (map == null) {
		          Log.debug("SGU: WFO could NOT retrieve registered AppMap "+ appMapName);
		          throw new SAFSObjectNotFoundException("Could not retrieve App Map "+ appMapName);
		        }
		      }
		      // what if NOT registered?
		      else{
		          Log.debug("SGU: WFO could NOT register AppMap "+ appMapName);
		          throw new SAFSObjectNotFoundException("Could not register App Map "+ appMapName);
		      }
		    }
		    //TODO: Find and inject into window not launched by us.
	    	String winRec = map.getParentGUIID(windowName, true);
            Log.info("SGU: winRec retrieved: "+ winRec);
			SGuiObject winObj = (SGuiObject)map.getParentObject(windowName);
            Log.info("SGU: winObj retrieved: "+ winObj);

            if(winObj != null && winObj.isDynamic())
            	winObj = null;

            boolean notDone = true;

			while(winObj==null && notDone){
				winObj = getWindowIdFromTitle(selenium,winRec);
				if(winObj != null){
					map.setParentObject(windowName, winObj);
				}
				notDone = (winObj == null);
		    	if(notDone){
		    		gSecTimeout--;
		    		if (gSecTimeout >= 0) {
		    			try{Thread.sleep(1000);}catch(Exception t){}
		    		} else {
		    			notDone = false;
		    		}
		    	}
			}

			if(winObj==null){
				throw new SAFSObjectNotFoundException("Could not find Window "+ windowName);
			}

			if(! selectWindow(selenium, winObj.getWindowId(), gSecTimeout))
				throw new SAFSObjectNotFoundException("Could not find Window "+ windowName);

	    	//these may not be needed if seeking parent window only
	    	String compRec = null;
	    	SGuiObject compObj = null;

	    	boolean isParent = windowName.equalsIgnoreCase(compName);
	    	notDone = true;
	    	boolean winValid = false;
	    	boolean compValid = false;

	    	// get values if a child component is the target and not just the window
	    	if(! isParent){
		    	compRec = map.getChildGUIID(windowName, compName, true);
	            Log.info("SGU: compRec retrieved: "+ compRec);
		    	compObj = (SGuiObject)map.getChildObject(windowName, compName);
	            Log.info("SGU: compObj retrieved: "+ compObj);
	    	} else {
	    		//throw NullPointerExceptions if not found/valid
	    		trdata.setWindowGuiId(winRec);
	    		trdata.setWindowTestObject(winObj);
				Log.info( "SGU Matched: "+ winObj.toString());
				return 0;
	    	}

	    	do{
		    	Log.debug("SGU:Trying "+windowName+"."+compName+" using AppMap:"+appMapName);


		        // can add validate winObj still alive code in here
	    	    if (! winValid) {
	    	    	notDone = true;
	    			while(notDone){
	    				try{
	    					winValid = (selenium.getTitle() != null);
	    					notDone = false;
	    				} catch (SeleniumException e){
	    					if(notDone){
	    			    		gSecTimeout--;
	    			    		if (gSecTimeout >= 0) {
	    			    			try{Thread.sleep(1000);}catch(Exception t){}
	    			    		} else {
	    			    			notDone = false;
	    			    		}
	    			    	}
	    				}
	    			}
	    	    	if (!winValid) {
	    	    		compValid = false;
	    	    		winObj = null;
	    	    		compObj = null;
	    	    		map.setParentObject(windowName, winObj);
	    	    		map.setChildObject(windowName, compName, compObj);
	    	    	}
	    	    }
		    	map.setParentObject(windowName, winObj);
		    	Log.info("SGU Window: "+ winObj);
		    	// only seek the child if we already have the parent windowGood2go
		    	if((! isParent)&&(winObj != null)){
		    		if (compObj == null || compObj.isDynamic()) {
		    			Log.debug("Trying xpath script for: '" + compName + "' with recognition '"+ compRec +"'");
		    			//System.out.println("Trying xpath script for: '" + compName + "' with recognition '"+ compRec +"'");
						try{
							compObj = getGuiObject(compRec, selenium, winObj.getWindowId());
							if(compObj!=null && !selenium.isVisible(compObj.getLocator()))
								compObj = null;
						}catch(Exception e){
							Log.debug("IGNORING SGU:getXPathString error:", e);
						}

						// may throw NullPointerException?
						if((compObj != null)&&(compObj.getLocator().equals(""))){
							compObj = null; //If compObj is "", getXPathString did not find the object
						}
						// delete following line to remove caching components
						if (compObj != null) map.setChildObject(windowName, compName, compObj);
				    	Log.info("SGU Component: "+ compObj);
		    		}
			    	else{
			    	    // can add validate compObj still alive code in here
			    	    if (! compValid) {
			    	    	compValid = selenium.isElementPresent(compObj.getLocator()) && selenium.isVisible(compObj.getLocator());
			    	    	if (! compValid) {
		    	    			compObj = null;
		    	    			map.setChildObject(windowName, compName, compObj);
		    	    		}
			    	    }
			    	}
		    	}

		    	notDone = (compObj == null);
		    	if(notDone){
		    		gSecTimeout--;
		    		//sleep 1 second before trying again if timeout > 0 specified
		    		if (gSecTimeout >= 0) {
		    			try{Thread.sleep(1000);}catch(Exception t){;}
		    		}
		    		// we have timed out
		    		else{
		    			notDone = false;
		    		}
		    	}
		    	// is done
		    	else{
		    		trdata.setCompGuiId(compRec);
		    		trdata.setWindowGuiId(winRec);
		    		trdata.setCompTestObject(compObj);
		    		trdata.setWindowTestObject(winObj);
		    		trdata.setCompType(compObj.getCompType());
		    	}
	    	}while( notDone );
//	    	 throw NullPointerExceptions if not found/valid
			Log.info( "SGU Matched: "+ compObj.toString());

			return 0;
	    }
	    catch(Exception x){
	    	String msg = windowName+"."+compName+" using MapName:"+appMapName +";ApplicationMap:";
	    	msg += (map == null) ? "NULL":map.getMapName();
	       	Log.debug("SGU:WFO EXCEPTION:"+ x.getMessage() + " using "+ msg +"\n", x);
			throw new SAFSObjectNotFoundException( msg );
		}
	}

	/**
	 * Gets the object defined by the windowName and childName
	 * @param mapName  the name/ID of the App Map currently in use.
	 * @param windowName  the name/ID of the window as predefined in the App Map.
	 * @param childName    the name/ID of the child component of the window as predefined in the App Map.
	 * @param ignoreCache if true, try getting the component from the appmap
	 * @return the object defined by the windowName and childName
	 */
	public SGuiObject getTestObject(String mapname,String windowName,String childName,boolean ignoreCache){
		if(mapname==null||windowName==null) return null;
		ApplicationMap map = getAppMap(mapname);

		if (map == null) {
			if (registerAppMap(mapname, mapname)) {
				map = getAppMap(mapname);
				if (map == null) {
					Log.info("SDDG: gto1 could NOT retrieve registered AppMap "+ mapname);
					return null;
				}
			}
			// what if NOT registered?
			else{
				Log.info("SDDG: gto1 could NOT register AppMap "+ mapname);
				return null;
			}
		}

		//Selenium selenium = SApplicationMap.getSelenium(windowName);

		SGuiObject tobj = null;
		if ((childName == null)||(windowName.equalsIgnoreCase(childName))){

			if (!ignoreCache) {
				tobj = (SGuiObject)map.getParentObject(windowName);
			}
			if (tobj == null){
				//map.setParentObject(windowName, null);
				try {
					waitForObject(trdata.getAppMapName(),windowName,windowName,0);
				} catch (SAFSObjectNotFoundException e) {
					Log.info("SDDG: Could not waitForObject "+ windowName);
					return null;
				}
				tobj = (SGuiObject)map.getParentObject(windowName);
				if (tobj == null) {
					Log.info("SDDG: Could not waitForObject "+ windowName);
					return null;
				}
			}else{
				return tobj;
			}

		} else {

			if (!ignoreCache) {
				tobj = (SGuiObject)map.getChildObject(windowName, childName);
			}
			if (tobj == null) {
				//map.setChildObject(windowName, childName, null);
				try {
					waitForObject(trdata.getAppMapName(),windowName,childName,0);
				} catch (SAFSObjectNotFoundException e) {
					Log.info("SDDG: Could not waitForObject "+ childName);
					return null;
				}
				tobj = (SGuiObject)map.getChildObject(windowName,childName);
				if (tobj == null) {
					Log.info("SDDG: Could not waitForObject "+ childName);
					return null;
				}
				((STestRecordHelper)trdata).setCompTestObject(tobj);
			}else{
				Log.info("SDDG: returning cached object: "+tobj.getCompType());
				return tobj;
			}
		}

		try{
			Log.info("SDDG: compTestObject : "+ tobj.getCompType());
		}catch(Exception npe2){
			Log.info("SDDG: No Mapped TestObject named \""+ childName +"\" found.");
		}

		return tobj;
	}

	/**
	 * selects a window to send future commands to
	 * @param selenium Current Selenium object
	 * @param id windowId--can be null
	 * @param timeout how long to wait for the window in seconds
	 * @return whether window was found
	 */
	public boolean selectWindow(Selenium selenium, String id, long timeout){

		// workaround Selenium.selectWindow deadlock defect
		ThreadedSelectWindow selectWindow =
			new ThreadedSelectWindow(Thread.currentThread(), selenium, id, timeout);
		try{
			selectWindow.start();
			Thread.sleep((timeout * 1000)+ 1000); //backup timeout for deadlocks
		} catch (Exception e){}
		if(selectWindow.isSearching()){
			Log.debug("SGU: selectWindow thread may have deadlocked after "+
					  selectWindow.searchSeconds() +" seconds.");
		}
		// hasten cleanup
		boolean found = selectWindow.isSelected();
		selectWindow = null;

		if(!found){
			WebDriver driver = null;
			try{
				driver = getWebDriver(selenium);
			}catch(Throwable th){
				Log.debug("SGU: selectWindow: Failed to get WebDriver from Selenium, met "+ th.toString());
			}
			if (driver != null) {
				driver.switchTo().window(driver.getWindowHandle());
				found = true;
			}
		}

		Log.info("SGU: selectWindow found window? "+ found);
		return found;
	}



	/**
	 * Wraps Selenium.getAllWindowNames to handle Selenium defect.
	 * Something buggy about later versions of Selenium.
	 * Later versions return the window names in one comma-delimited string
	 * in the first array item.  This wrapper will handle this situation
	 * if present.
	 * <p>
	 * @return String array.  Empty window names are replaced with INVALID_WINDOWNAME
	 * in the array before it is returned.  An array of one "null" item will
	 * be returned if no windows were found.
	 */
	public String[] getAllWindowNames(Selenium selenium){
		String[] names = null;
		String comma = ",";
		try{
			names = selenium.getAllWindowTitles();
			// check for defect of comma-delimited single string
			// catch will catch a NullPointerException
			if(names.length == 1){
				Log.debug("SGU: getAllWindowNames single array item: "+ names[0]);
				if( names[0].indexOf(comma) > -1){
					names = names[0].split(comma);
				}
			}
		} catch(Exception e){
			names = new String[1];
			Log.debug("SGU: getAllWindowNames ignoring Exception: ", e);

			try{
				names[0] = getWebDriver(selenium).getTitle();
			}catch(Throwable th){
				Log.debug("SGU: getAllWindowNames: Failed to get WebDriver from Selenium, met "+ th.toString());
			}

		} finally {
			String thename = null;
			for(int i = 0; i < names.length; i++){
				thename = names[i].trim();
				Log.debug("SGU: found windowName: "+ thename);
				if(thename.equals("")){
					Log.debug("SGU: making windowName: '"+ UNNAMED_WINDOW +"'");
					names[i] = UNNAMED_WINDOW;
				}
			}
		}
		return names;
	}

	//Selenium 1.0, the class 'WebDriverBackedSelenium' is in package 'org.openqa.selenium', it is org.openqa.selenium.WebDriverBackedSelenium;
	//getUnderlyingWebDriver() is the method name to get WebDriver
	//From selenium 2.42, the class 'WebDriverBackedSelenium' has been moved to another package, it is com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium
	//getWrappedDriver() is the method name to get WebDriver
	//To avoid the compilation error, we use "Java Reflection" to get the method directly
	private static final String METHOD_GET_WEBDRIVER_V1 = "getUnderlyingWebDriver";
	private static final String METHOD_GET_WEBDRIVER_V2 = "getWrappedDriver";

	private WebDriver getWebDriver(Selenium selenium){//The Selenium should be a WebDriverBackedSelenium
		if(selenium==null) return null;
		String debugmsg = StringUtils.debugmsg(false);

		WebDriver driver = null;
		Method getDriverMethod = null;

		try{
			getDriverMethod = selenium.getClass().getMethod(METHOD_GET_WEBDRIVER_V2);
		}catch(Exception e){
			IndependantLog.error(debugmsg+"Met "+e.toString());
			try {
				getDriverMethod = selenium.getClass().getMethod(METHOD_GET_WEBDRIVER_V1);
			} catch (Exception e1) {
				IndependantLog.error(debugmsg+"Met "+e1.toString());
			}
		}

		if(getDriverMethod!=null){
			try {
				driver = (WebDriver) getDriverMethod.invoke(selenium);
			} catch (Exception e) {
				IndependantLog.error(debugmsg+"Met "+e.toString());
			}
		}

		return driver;
	}

	/**
	 * Get's the window's id and returns it in the form of an SGuiObject
	 * @param selenium current selenium object
	 * @param winRec window recognition string
	 * @return object representing the window defined by the winRec
	 */
	public SGuiObject getWindowIdFromTitle(Selenium selenium, String winRec ){
		String [] windowNames = null;
		selectWindow(selenium, null, 5);
		windowNames = getAllWindowNames(selenium);
		if(windowNames != null && windowNames.length > 0){
			boolean isDynamic = ApplicationMap.isGUIIDDynamic(winRec);
			winRec = ApplicationMap.extractTaggedGUIID(winRec);
			winRec = winRec.substring(winRec.lastIndexOf("=")+1);
			if(winRec.indexOf("{") == 0 && winRec.indexOf("}") == winRec.length()-1){
				winRec = winRec.substring(1);
				winRec = winRec.substring(0,winRec.length()-1);
			}
			boolean windowLoaded = false;
			String window = null;
			String title = null;
			boolean titleObtained = false;
			int time = 0;
			Log.info("SGU: getWindowIdFromTitle: "+ winRec);
			for(int i = 0; i < windowNames.length; i++){
				window = windowNames[i].trim();
				// invalid windows
				windowLoaded = selectWindow(selenium, window, 5);
				if(windowLoaded){
					title = "";
					time = 0;
					titleObtained = false;
					do {
						try{
							title = selenium.getTitle();
							titleObtained = true;
						} catch (Exception e){
							try {
								Thread.sleep(1000);
								time++;
							} catch (InterruptedException e1) {}
						}
					} while(!titleObtained && time < 5);

					if( titleObtained && checkTitle(title,winRec)){
						Log.debug("SGU: Selecting window: '" + title + "' with id: '" + window + "'");
						return new SGuiObject("//HTML[1]","Window",window,isDynamic);
					}else{
						Log.debug("SGU: getWindowIdFromTitle could not retrieve title of '"+ window +"'.");
					}
				}else{
					Log.debug("SGU: getWindowIdFromTitle failed loading of '"+ window +"'.");
				}
			}
		}
		return null;
	}

	/**
	 * Checks whether the window title matches the regular expression contained by the winRec
	 * @param title window title obtained by Selenium
	 * @param winRec windows recognition string
	 * @return true if matched, false otherwise
	 */
	private boolean checkTitle(String title, String winRec){
		String aRec = org.safs.StringUtils.convertWildcardsToRegularExpression(winRec);
		try{ return org.safs.StringUtils.matchRegex(aRec, title);}
		catch(SAFSException e){return false;}
	}
	/**
	 * Attempts to "activate" or give focus to the specified window/object via the remote
	 * AUT JVM proxies.
	 *
	 * @return 0 on success.
	 * @throws SAFSObjectNotFoundException if the specified component cannot be found.
	 * @see org.safs.DDGUIUtilities#setActiveWindow(String,String,String)
	 */
	@Override
	public int setActiveWindow (String appMapName, String windowName, String compName)
	                             throws SAFSObjectNotFoundException {
    	Log.info("SGU:Activating "+windowName+"."+compName+" from AppMap:"+appMapName);
    	ApplicationMap map = null;
    	try{
	    	map = getAppMap(appMapName);
	    	boolean isParent = windowName.equalsIgnoreCase(compName);
			Selenium selenium = SApplicationMap.getSelenium(windowName);
			SGuiObject winObj = (SGuiObject)map.getParentObject(windowName);
	    	Object compObj;
	    	if(winObj == null) waitForObject(appMapName, windowName, windowName, 0);
	    	if(!isParent){
	    		compObj = map.getChildObject(windowName, compName);
	    	   	// if not previously found then find it with no timeout value
			    // routine will pass thru exception thrown from waitForObject
		    	if (compObj == null) waitForObject(appMapName, windowName, compName, 0);
		    	compObj = map.getChildObject(windowName, compName);
		    	if(compObj == null) throw new NullPointerException();
	    	}



	    	if (winObj == null) throw new NullPointerException();

			// server setActive or server.invoke, etc..
	    	selectWindow(selenium, winObj.getWindowId(),5);
			return 0;
    	}
    	catch(NullPointerException np){
	    	String msg = windowName+"."+compName+" using MapName:"+appMapName +";ApplicationMap:";
	    	msg += (map == null) ? "NULL":map.getMapName();
	       	Log.debug("SGU:SAW EXCEPTION with "+ msg);
			throw new SAFSObjectNotFoundException( msg );
    	}
    	catch(SAFSObjectNotFoundException nf){ throw nf; }
	}

	/**
	 * Selects the top frame if its not already selected
	 * @param selenium Current selenium object
	 */
	public void selectTopFrame(Selenium selenium){
		if(frameSwitched){
			selenium.selectFrame("relative=top");
			frameSwitched = false;
		}
	}

	private String _getProperty(Selenium selenium, String xpath, String property){
		String val = null;
		try { val = selenium.getAttribute(xpath+"@"+property);}
		catch(Exception x){
			if ((val != null)&&(val.equals("undefined"))){
				val = null;
			}
		}
		return val;
	}

	/**
	 * Return the value of a specific attribute/property
	 * @param selenium current Selenium object
	 * @param xpath Selenium XPATH to desired element
	 * @param attribute case-sensitive property name
	 * @return value of property or null
	 */
	public String getProperty(Selenium selenium, String xpath, String attribute){
		String val = null;
		if( attribute.equalsIgnoreCase("text") ||
			attribute.equalsIgnoreCase("innerText")){

			val = _getProperty(selenium, xpath, "innerText");
			if(val==null) val = _getProperty(selenium, xpath, "innertext");
			if(val==null) val = _getProperty(selenium, xpath, "textContent");
			if((val != null) && (val.length()==0)){
				val = null;
				val = _getProperty(selenium, xpath, "value");
			}
			if(val==null) val = _getProperty(selenium, xpath, "alt");
		} else {
			val = _getProperty(selenium, xpath, attribute);
		}
		return val;
	}

	/**
	 * Returns the xpath of the component represented by a robot recognition string
	 * @param recString robot recognition string
	 * @param selenium current selenium object
	 * @param windowId window containing the object
	 * @return xpath string
	 */
	public SGuiObject getGuiObject(String recString,Selenium selenium, String windowId){
		String xPath = "";

		selectTopFrame(selenium);

		boolean isDynamic = ApplicationMap.isGUIIDDynamic(recString);
		recString = ApplicationMap.extractTaggedGUIID(recString);
		recString = recString.replaceAll("\"",""); // removes extra " in the recognition string.
		if (!(recString.toUpperCase().startsWith("TYPE="))){
			recString = "Type=HTML;"+ recString;
		}
		String [] parts = recString.split(";\\\\;");

		domParser = getDocumentParser(selenium);

		String url = domParser.getUrl();
		String type = "";
		Pattern typePattern = Pattern.compile("Type=(.+?)$");
		Pattern idPattern = Pattern.compile("(.+?)=(.+?)\"?$");
		for(int i = 0; i < parts.length; i++){
			String [] subParts = parts[i].split(";");

			Matcher typeMatcher = typePattern.matcher(subParts[0]);
			Log.debug("SGU Pattern Matching subPart:"+ subParts[0]);

			if(typeMatcher.find()){
				type = typeMatcher.group(1);
			} else {
				type="HTML";
			}
			Log.debug("SGU typeMatcher matching: "+ type);


			String [] idTypes = new String[subParts.length-1];
			String [] ids = new String[subParts.length-1];
			for(int j = 1; j < subParts.length; j++){
				Matcher idMatcher = idPattern.matcher(subParts[j]);
				Log.debug("SGU SubPart Matching: "+ subParts[j]);

				if(idMatcher.find()){
					idTypes[j-1] = idMatcher.group(1);
					ids[j-1] = idMatcher.group(2);
				} else {
					//TODO:Error Report
					Log.debug("SGU: ERROR: match not found for: " + subParts[j]);
				}
			}

			Document doc = domParser.getDocument(url, false);
			String xPart = typeToXPath(doc,type,idTypes,ids,selenium);
			HtmlFrameComp precedingFrame = null;
			if(!xPart.equals("")){
				xPath += xPart;
				if(xPart.indexOf("FRAME") != -1){
					//If the xpart contains the "FRAME", we should navigate to the
					//deepest frame and change the current url also.
//					this.navigateFrames(domParser.getUrl(), xPath + "//f", selenium);
					precedingFrame = navigateFrames(url, xPath , selenium);
					url = precedingFrame.getSrc();
					Log.debug("Navigate to Frame, whose src is "+url);
					if(url==null || url.equals("")){
						Log.error("Frame's src does NOT exist!!! Can NOT continue. Abort.");
						return null;
					}
				}
			}else{
				//If xPart is "", that means, we fail to match the whole RS
				//For example, "Type=HTMLFrame;name=toc1;\;Type=HTMLLink;Index=111"
				//We find xpath for "Type=HTMLFrame;name=toc1", but fail for "Type=HTMLLink;Index=111"
				//Maybe we should break the loop and return null
				return null;
			}
		}

		xPath = SeleniumGUIUtilities.normalizeXPath(xPath);

		SGuiObject co = new SGuiObject(xPath,type,windowId,isDynamic);
		Log.debug("XPATH SCRIPT RETURNED: " + co.getLocator() );
		return co;
	}

	public static String normalizeXPath(String xPath){
		xPath = xPath.trim();
		Log.debug("Before normalization, xpath="+xPath);
		//remove the last // from the xpath
		if(xPath.lastIndexOf("//") == xPath.length()-2 && xPath.length() > 0){
			xPath = xPath.substring(0,xPath.length()-2);
		}

		//For selenium, the xpath must start with "//" so that is can be processed.
		//Without an explicit locator prefix, Selenium uses the following default strategies:
		//xpath, for locators starting with "//"
		if(xPath.startsWith("/")){
			if(xPath.length()>1 && !xPath.substring(1,1).equals("/")){
				xPath = "/"+xPath;
			}
		}else{
			xPath = "//"+xPath;
		}

		Log.debug("After normalization, xpath="+xPath);

		return xPath;
	}

	public static Matcher matchPattern(Pattern pattern, String xpath){
		Matcher m = pattern.matcher(xpath);

		return m;
	}

	//TODO If the frame is not in format /HTML/FRAMSET/FRAME[1]/HTML/BODY
	//but /HTML/FRAMSET/FRAME/HTML/BODY, This function will give the wrong RS, need to be improved.
	protected String getFramePath(String xpath){
		String framepath = "";
		Matcher m = FRAME_PATTERN.matcher(xpath);
		if(m.find()){
			//TODO Here problem comes from substring
			framepath = "Type=HTMLFrame;name="+m.group().substring(m.group().lastIndexOf("[")+1,m.group().lastIndexOf("]"))+DocumentParser.RECOGNITION_LEVEL_SEPARATOR;
			return framepath+getFramePath(xpath.substring(m.end()));
		} else {
			return "";
		}
	}

	/**
	 * Selects any frames that are part of the xpath and <br>
	 * returns the last component's xpath<br>
	 *
	 * @param xpath xpath of the frames and component that needs to be interacted with
	 * @param selenium current selenium object
	 * @return the last component's xpath
	 */
	protected String navigateFrames(String xpath, Selenium selenium){
		String childXpath = null;

		HtmlFrameComp frameComp = navigateFrames(null, xpath,selenium, null);
		if(frameComp != null){
			childXpath = frameComp.getChildXpath();
		}

		return childXpath;
	}

	/**
	 * Selects any frames that are part of the xpath and <br>
	 * returns the last Frame preceding final component xpath<br>
	 *
	 * @param url   from where the xpath will be parsed, that is, the page indicated by url
	 *              should contain the element described by xpath
	 * @param xpath xpath of the frames and component that needs to be interacted with
	 * @param selenium current selenium object
	 * @return HtmlFrameComp, it contains<br>
	 *         src:               the url (the last frame's src), where the last XPATH is located<br>
	 *         locator:           the frame's locator recognized by selenium<br>
	 *         fullXpath:         the full xpath to represent the last preceding frame<br>
	 *         recognitionString: the recognition string of preceding frames<br>
	 *         childXpath:        last part XPATH, without preceding frame's xpath<br>
	 */
	protected HtmlFrameComp navigateFrames(String url, String xpath, Selenium selenium){
		return navigateFrames(url, xpath,selenium, null);
	}

	/**
	 * Selects any frames that are part of the xpath and <br>
	 * returns the last Frame preceding final component xpath<br>
	 *
	 * @param url   from where the xpath will be parsed, that is, the page indicated by url
	 *              should contain the element described by xpath
	 * @param xpath xpath of the frames and component that needs to be interacted with
	 * @param selenium current selenium object
	 * @param boundsOut object that will contain the bounds of the component after its found
	 * @return HtmlFrameComp, it contains<br>
	 *         src:               the url (the last frame's src), where the last XPATH is located<br>
	 *         locator:           the frame's locator recognized by selenium<br>
	 *         fullXpath:         the full xpath to represent the last preceding frame<br>
	 *         recognitionString: the recognition string of preceding frames<br>
	 *         childXpath:        last part XPATH, without preceding frame's xpath<br>
	 */
	protected HtmlFrameComp navigateFrames(String url, String xpath, Selenium selenium, Rectangle boundsOut){
		selectTopFrame(selenium);

		if(url==null){
			//We suppose that the xpath is to be searched within the current page, that is,
			//the url stored in the HtmlDomParser.
			DocumentParser parser = getDocumentParser(selenium);
			url = parser.getUrl();
		}
		//Try to remove the longest frame-prefix-xpath
		//And store the final component xpath in it.
		HtmlFrameComp frame = getPrefixingFrame(xpath);
		if(frame!=null){
			selenium.selectFrame(frame.getLocator());
			frameSwitched = true;
		}else{
			frame = new HtmlFrameComp(url,xpath);
		}

		//IMPORTANT!!!, If xpath does not contain FRAME, we should return directly<br>
		//and avoid to call navigateFramesR(), which will cost a lot of time<br>
		Matcher m = FRAME_PATTERN.matcher(frame.getChildXpath());
		if(!m.find()){
			return frame;
		}

		return navigateFramesR(frame,selenium, boundsOut);
	}

	/**
	 * Helper method for the navigateFrames() method
	 * @param frame   Contains the information for navigation. Its field childXpath is to be navigated.
	 * @param selenium current selenium object
	 * @param boundsOut object that will contain the bounds of the component after its found
	 * @return HtmlFrameComp, it contains<br>
	 *         src:               the url (the last frame's src), where the last XPATH is located<br>
	 *         locator:           the frame's locator recognized by selenium<br>
	 *         fullXpath:         the full xpath to represent the last preceding frame<br>
	 *         recognitionString: the recognition string of preceding frames<br>
	 *         childXpath:        last part XPATH, without preceding frame's xpath<br><br>
	 *
	 *         For example:<br>
	 *         1. The childXpath does NOT contain FRAME<br>
	 *            Input frame:
	 *            src="http://tadsrv/safs/"<br>
	 *            locator=""<br>
	 *            fullXpath=""<br>
	 *            recognitionString=""<br>
	 *            childXpath="/HTML/BODY/TABLE"<br>
	 *            The output frame will be exactly the same object.<br><br>
	 *
	 *         2. The childXpath DOES contain some FRAMEs<br>
	 *            Input frame:
	 *            src="http://tadsrv/safs/"<br>
	 *            locator=""<br>
	 *            fullXpath=""<br>
	 *            recognitionString=""<br>
	 *            childXpath="/HTML/FRAMESET/FRAME[1]/HTML/BODY/TABLE"<br><br>
	 *
	 *            We assume that the src of HTML/FRAMESET/FRAME[1] is 'framePage.htm'<br>
	 *            We assume that the name of HTML/FRAMESET/FRAME[1] is 'frame1'<br>
	 *            Output frame will be:
	 *            src="http://tadsrv/safs/framePage.htm"<br>
	 *            locator="name=frame1"<br>
	 *            fullXpath="HTML/FRAMESET/FRAME[1]"<br>
	 *            recognitionString="Type=HTMLFrame;Index=1;\\;"<br>
	 *            childXpath="/HTML/BODY/TABLE"<br>
	 */
	private HtmlFrameComp navigateFramesR(HtmlFrameComp frame, Selenium selenium, Rectangle boundsOut){
		String debugmsg = getClass().getName()+".navigateFramesR() ";
		Document document = null;
		String xpath = frame.getChildXpath();
		String url = frame.getSrc();
		String frameRS = frame.getRecognitionString();
		DocumentParser parser = getDocumentParser(selenium);

		Log.debug(debugmsg +" navigating xpath: " + xpath);
		Log.debug(debugmsg+" from url: "+url);
		Log.debug(debugmsg+" parent's frameRS: "+frameRS);

		if(url==null){
			Log.debug(debugmsg+"  the url is null, can not continue!!!");
			return frame;
		}

		Matcher m = FRAME_PATTERN.matcher(xpath);
		if(m.find()){
			String firstFrameXpath = m.group(1);
			Log.debug("Matched: " + firstFrameXpath);
			HtmlFrameComp childFrame = new HtmlFrameComp();
			try{
				if(boundsOut != null){
					Rectangle frameBounds = getComponentBounds(firstFrameXpath,selenium);
					boundsOut.x += frameBounds.x;
					boundsOut.y += frameBounds.y;
				}

//				String frameName = selenium.getEval("SAFSgetAttribute('"+g+"', 'name');");
				document = parser.getDocument(url, false);
				String frameName = parser.getAttribute(document,firstFrameXpath, "name");
				String locator = "name="+frameName;
				if(frameName==null || "".equals(frameName)){
//					String frameIndex = selenium.getEval("SAFSgetFrameIndex('"+g+"')");
					int frameIndex = parser.getFrameIndex(document,firstFrameXpath);
					locator = "index="+frameIndex;
				}
				childFrame.setLocator(locator);
				selenium.selectFrame(locator);
				Log.debug(debugmsg+", switch to frame "+locator);

				if(frameRS==null){
					frameRS = "";
				}
				frameRS += "Type=HTMLFrame;"+locator+DocumentParser.RECOGNITION_LEVEL_SEPARATOR;

				frameSwitched = true;
			} catch(SeleniumException e){
				Log.debug("SGU: Handling SeleniumException:", e);
//				selenium.selectFrame(xpath.substring(0,m.end()));
				selenium.selectFrame(firstFrameXpath);
				frameSwitched = true;
			}

			String childFrameURL = parser.getFrameSrcURL(document, firstFrameXpath, url);
			String fullFrameXpath = frame.getFullXpath()+firstFrameXpath;
			String childFrameChildXpath = xpath.substring(firstFrameXpath.length());

			childFrame.setSrc(childFrameURL);
			childFrame.setFullXpath(fullFrameXpath);
			childFrame.setRecognitionString(frameRS);
			childFrame.setChildXpath(childFrameChildXpath);
			//Put the frame to the cache
			addPrefixingFrameToCache(fullFrameXpath,childFrame);

			if(childFrameURL.equals(url)){
				Log.warn("Frame '"+firstFrameXpath+"', it does NOT has attribut 'src'.");
				Log.warn("Frame '"+firstFrameXpath+"' does NOT contain any content.");
				return childFrame;
			}

			return navigateFramesR(childFrame,selenium, boundsOut);
		} else {
			Log.debug("SGU: navigateFramesR found no FRAME to evaluate.");
			return frame;
		}
	}

	/**
	 * Returns the bounds of a component as a Rectangle object
	 * @param o object describing the component
	 * @param selenium current selenium object
	 * @return bounds as a rectangle object
	 */
	protected Rectangle getComponentBounds(SGuiObject o, Selenium selenium){
		return getComponentBounds(o.getLocator(), selenium);
	}

	/**
	 * Returns the absolute bounds of a component on the screen as a Rectangle object
	 * @param xpath xpath string describing the component
	 * @param selenium current selenium object
	 * @return bounds as a rectangle object or null
	 */
	protected Rectangle getComponentBounds(String xpath, Selenium selenium){
		DocumentParser parser = getDocumentParser(selenium);
		String [] compBounds = null;
		Rectangle compRect = null;
		Rectangle frameBounds = new Rectangle();
		HtmlFrameComp precedingFrame = null;
		String xend = null;
		String temp = null;
		String boundsSeparator = "#";
		int browser_left = 0;
		int browser_top = 0;

		try{
//			String xpathBoundsSeparator = selenium.getEval("SAFSGetBoundsSeparator();");
			String xpathBoundsSeparator = parser.getBoundsSeparator();
			if(xpathBoundsSeparator!=null && !xpathBoundsSeparator.trim().equals("")){
				Log.debug("Selenium SPC get xpathBoundsSeparator: "+xpathBoundsSeparator);
				boundsSeparator = xpathBoundsSeparator.trim();
			}
		}catch(Exception e){
			Log.debug("SGU: SAFSGetBoundsSeparator() Can NOT get boundsSeparator.");
		}

		try{
//			temp = selenium.getEval("SAFSgetBrowserClientScreenPosition();");
			temp = parser.getBrowserClientScreenPosition(selenium);
			//Log.debug("SGU: SAFSgetBrowserClientScreenPosition() eval: "+ temp);
			compBounds = temp.split(boundsSeparator);
			browser_left = Integer.parseInt(compBounds[0]);
			browser_top = Integer.parseInt(compBounds[1]);
		}catch(Exception fx){
			Log.debug("SGU: IGNORING SAFSgetBrowserClientScreenPosition() eval Exception.", fx);
		}

		int scrollLeft = 0;
		int scrollTop = 0;
		String[] scrollInfo = null;
		try{
//			temp = selenium.getEval("SAFSgetClientScrollInfo();");
			temp = parser.getClientScrollInfo(selenium);
			//Log.debug("SGU: SAFSgetBrowserClientScreenPosition() eval: "+ temp);
			scrollInfo = temp.split(boundsSeparator);
			// skipped extracting innerWidth and height
			scrollLeft = Integer.parseInt(scrollInfo[2]);
			scrollTop = Integer.parseInt(scrollInfo[3]);
		}catch(Exception fx){
			Log.debug("SGU: IGNORING SAFSgetClientScrollInfo() eval Exception.", fx);
		}

		precedingFrame = navigateFrames(domParser.getUrl(), xpath, selenium, frameBounds);
		xend = precedingFrame.getChildXpath();

		if(frameBounds.x == 0 && frameBounds.y == 0){
			frameBounds.x = browser_left;
			frameBounds.y = browser_top;
		}
		Number top = null;
		Number left = null;
		Number width = null;
		Number height = null;
		if(xend==null) xend = xpath;
		try{
			top = selenium.getElementPositionTop(xend);
			left = selenium.getElementPositionLeft(xend);
			width = selenium.getElementWidth(xend);
			height = selenium.getElementHeight(xend);
			compRect = new Rectangle(left.intValue(), top.intValue(), width.intValue(),height.intValue());
			compRect.x += frameBounds.x;
			compRect.y += frameBounds.y;
			// account for scrolling
			compRect.x -= scrollLeft;
			compRect.y -= scrollTop;
		}
		catch(Exception nx){
			Log.debug("SGU: IGNORING getComponentBounds Rectangle Exception for '"+ xend +"':", nx);
		}
		return compRect;
	}

	/**
	 * Helper method for the getXpathString method
	 * @param type component type
	 * @param idTypes attribute types, this is SAFS-Robot attribute's type
	 * @param ids attribute values
	 * @param selenium current selenium object
	 * @return xpath string representing the component
	 */
	private String typeToXPath(Document doc, String type, String [] idTypes, String [] ids,Selenium selenium){
		String xPath = "";

		if(type.equalsIgnoreCase("HTMLFrame")){
			String[] tags = {"FRAME","IFRAME"};
			xPath = this.buildXPathAttributes(doc, idTypes, ids, tags, selenium);
		} else if(type.equalsIgnoreCase("Window")){
			String page = ids[0];
			page = page.substring(1,page.length()-2);
			xPath = page;
		} else if(type.equalsIgnoreCase("HTMLLink")) {
			xPath = this.buildXPathAttributes(doc, idTypes,ids,"A", selenium);
		} else if(type.equalsIgnoreCase("HTMLDocument")){
			if(idTypes[0].equalsIgnoreCase("HTMLTitle")&&ids[0].equals(selenium.getTitle())){
				xPath = "//HTML[1]";
			} else {
				xPath = this.buildXPathAttributes(doc, idTypes,ids,"HTML", selenium);
			}
		} else if(type.equalsIgnoreCase("HTMLTable")){
			xPath = this.buildXPathAttributes(doc, idTypes,ids,"TABLE", selenium);
		} else if(type.equalsIgnoreCase("HTMLTableCell")){
			String[] tags = {"TD","TH"};
			xPath = this.buildXPathAttributes(doc, idTypes,ids,tags, selenium);
		}  else if(type.equalsIgnoreCase("HTMLTableHeaderCell")){
			xPath = this.buildXPathAttributes(doc, idTypes,ids,"TH", selenium);
		} else if(type.equalsIgnoreCase("HTMLImage")){
			xPath = this.buildXPathAttributes(doc, idTypes,ids,"IMG", selenium);
		} else if(type.equalsIgnoreCase("HTMLMap")){
			xPath = this.buildXPathAttributes(doc, idTypes,ids,"MAP", selenium);
		}else if(type.equalsIgnoreCase("HTMLMapArea")){
			xPath = this.buildXPathAttributes(doc, idTypes,ids,"AREA", selenium);
		}else if(type.equalsIgnoreCase("EditBox")){
			String[] tags = {"INPUT","TEXTAREA"};
			String[] idTTemp = new String[idTypes.length+1];
			String[] idTemp = new String[ids.length+1];
			idTTemp[0] = "type";
			idTemp[0] = "text|password|undefined"; // no 'type' attrib seems to imply type=text
			for(int i = 1; i < idTTemp.length; i++){
				idTTemp[i] = idTypes[i-1];
				idTemp[i] = ids[i-1];
			}
			xPath = this.buildXPathAttributes(doc, idTTemp,idTemp,tags, selenium);
		}else if(type.equalsIgnoreCase("CheckBox")){
			String[] idTTemp = new String[idTypes.length+1];
			String[] idTemp = new String[ids.length+1];
			idTTemp[0] = "type";
			idTemp[0] = "checkbox";
			for(int i = 1; i < idTTemp.length; i++){
				idTTemp[i] = idTypes[i-1];
				idTemp[i] = ids[i-1];
			}
			xPath = this.buildXPathAttributes(doc, idTTemp,idTemp,"INPUT", selenium);
		}else if(type.equalsIgnoreCase("RadioButton")){
			String[] idTTemp = new String[idTypes.length+1];
			String[] idTemp = new String[ids.length+1];
			idTTemp[0] = "type";
			idTemp[0] = "radio";
			for(int i = 1; i < idTTemp.length; i++){
				idTTemp[i] = idTypes[i-1];
				idTemp[i] = ids[i-1];
			}
			xPath = this.buildXPathAttributes(doc, idTTemp,idTemp,"INPUT", selenium);
		}else if(type.equalsIgnoreCase("ComboBox")){
			xPath = this.buildXPathAttributes(doc, idTypes,ids,"SELECT", selenium);
		}else if(type.equalsIgnoreCase("ListBox")){
			xPath = this.buildXPathAttributes(doc, idTypes,ids,"SELECT", selenium);
		}else if(type.equalsIgnoreCase("PushButton")){
			String[] tags = {"INPUT","BUTTON"};
			String[] idTTemp = new String[idTypes.length+1];
			String[] idTemp = new String[ids.length+1];
			idTTemp[0] = "type";
			idTemp[0] = "submit|button";
			for(int i = 1; i < idTTemp.length; i++){
				idTTemp[i] = idTypes[i-1];
				idTemp[i] = ids[i-1];
			}
			xPath = this.buildXPathAttributes(doc, idTTemp,idTemp,tags, selenium);
		}else if(type.equalsIgnoreCase("HTMLHidden")){
			String[] idTTemp = new String[idTypes.length+1];
			String[] idTemp = new String[ids.length+1];
			idTTemp[0] = "type";
			idTemp[0] = "hidden";
			for(int i = 1; i < idTTemp.length; i++){
				idTTemp[i] = idTypes[i-1];
				idTemp[i] = ids[i-1];
			}
			xPath = this.buildXPathAttributes(doc, idTTemp,idTemp,"INPUT", selenium);
		}else if(type.equalsIgnoreCase("HTML")){
			xPath = this.buildXPathAttributes(doc, idTypes, ids, "DIV", selenium);
		}else {
			Log.warn("Unrecognized: " + type);
			Log.debug("SGU:typeToXpath Unrecognized component 'type': "+ type);
		}

		return xPath;
	}

	/**
	 * Helper method for getXpathString()
	 * @param idTypes attribute types, this is SAFS-Robot attribute's type
	 * @param ids attribute values
	 * @param tag html tag of the component
	 * @param selenium current selenium object
	 * @return xpath string of the component
	 */
	private String buildXPathAttributes(Document doc, String [] idTypes, String [] ids, String tag, Selenium selenium){
		String [] tags = {tag};
		return buildXPathAttributes(doc, idTypes, ids, tags, selenium);
	}

	/**
	 * Helper method for getXpathString()
	 * @param idTypes attribute types, this is SAFS-Robot attribute's type
	 * @param ids attribute values
	 * @param tags potential html tags of the component
	 * @param selenium current selenium object
	 * @return xpath string of the component
	 */
	private String buildXPathAttributes(Document doc , String [] idTypes, String [] ids, String [] tags, Selenium selenium){
		//attributes contains the conditions for matching an element
		String [][] attributes = new String[idTypes.length][2];
		String xPath = "";
		//Convert the idTypes[i] to html-attribute and put it in attributes[i][0],
		//put the ids[i] (the attribute value) in attributes[i][1]
		for(int i = 0; i < idTypes.length; i++){
			if(idTypes[i].equalsIgnoreCase("HTMLId")){
				attributes[i][0] = "id";
				attributes[i][1] = ids[i];
			} else if(idTypes[i].equalsIgnoreCase("name")){
				attributes[i][0] = "name";
				attributes[i][1] = ids[i];
			} else if(idTypes[i].equalsIgnoreCase("HTMLTitle")){
				attributes[i][0] = "title";
				attributes[i][1] = ids[i];
			} else if(idTypes[i].equalsIgnoreCase("HTMLText") ||idTypes[i].equalsIgnoreCase("Text")){
				attributes[i][0] = "text";
				attributes[i][1] = ids[i];
			} else if(idTypes[i].equalsIgnoreCase("type")){
				attributes[i][0] = "type";
				attributes[i][1] = ids[i];
			} else if(idTypes[i].equalsIgnoreCase("Index")){
				//Get the Index
				int index = Integer.parseInt(ids[i]) - 1;
				//Copy the rest attributes as criteria for matching node.
				String [][] criteria = new String[attributes.length-1][2];
				for(int j = 0; j < criteria.length; j++){
					criteria[j][0] = attributes[j][0];
					criteria[j][1] = attributes[j][1];
				}
				//Pass criteria and index to get the matching component's xpath
				xPath = getXPath(doc, tags,criteria,index, selenium);
				//TODO If we can not find, do we need continue???
			} else {
				attributes[i][0] = idTypes[i];
				attributes[i][1] = ids[i];
			}
		}
		//If we don't have a "Index=aInt", we are just happy to return the first matching
		if(xPath.equals("")){
			xPath = getXPath(doc, tags,attributes,0, selenium);
		}
		return xPath;
	}

	/**
	 * getXpath() helper method
	 * @param document, from which document to search the matching element
	 * @param tags potential html tags of the object
	 * @param attributes html-tags-attributes and values
	 * @param index index of the component on the page
	 * @param selenium current selenium object
	 * @return xpath string of component
	 */
	private String getXPath(Document document, String [] tags, String [][] attributes, int index, Selenium selenium){
		DocumentParser parser = getDocumentParser(selenium);

		String xpath = parser.getXpath(document, tags, attributes, index, false, true);
		Log.debug("SGU DocumentParser getXpath returned: "+ xpath);

		return xpath;
	}

	/**
	 * <em>Purpose:</em>  Create a new HtmlDoParser, it will replace the older one.
	 * @param selenium
	 * @return
	 */
	protected DocumentParser initDocumentParser(Selenium selenium){
		if(selenium==null){
			Log.error("The parameter selenium should NOT be null");
			return null;
		}
		domParser = new DocumentParser(selenium,this);

		return domParser;
	}

	/**
	 * <em>Purpose:</em>  Return a HtmlDoParser, if not exist, create a new one.
	 * @param selenium
	 * @return
	 */
	public DocumentParser getDocumentParser(Selenium selenium){
		if(selenium==null){
			Log.error("The parameter selenium should NOT be null");
			return null;
		}

		if(domParser==null){
			domParser = new DocumentParser(selenium,this);
		}else{
			try{
				if(!selenium.getLocation().equals(domParser.getUrl())){
					//Selenium API getLocation() will return "the absolute URL of the current page."
					//if it is different than that of the main url stored in the documentParser
					//There are 2 situations
					//1. If we have navigated between the frames within the same page ( for example,
					//   calling selenium.selectFrame(frameLocator) ). Although getLocation() will return
					//   a different url, but we do NOT need to change the main url of DocumentParser.

					//2. If we click a link to visit another web page.
					//   getLocation() will return a different url and we need to change the main url of DocumentParser.
					Log.debug("selenium.getLocation(): "+selenium.getLocation());
					Log.debug("domParser.getUrl(): "+domParser.getUrl());
					boolean changeMainPageURL = !frameSwitched;
					domParser.setDocument(selenium.getLocation(), selenium.getHtmlSource(),changeMainPageURL);
				}else{
					//If the url is the same, We consider the Document is the same
				}
			}catch(Exception e){
				Log.warn("Exception occur "+e.getMessage());
			}
		}

		return domParser;
	}

	/**
	 * <em>Purpose:</em>	Return the value of attribute for an element indicated by xpath
	 * <em>Note:</em>		This method will try to DocumentParser to get the value<br>
	 *                      If it can not get the value, it will try the selenium's API getAttribute.<br>
	 * @param selenium      The instance of Selenium
	 * @param xpath         Xpath to represent the element in html page
	 * @param attribute     The name of attribute
	 * @return              The value of attribute
	 * @see org.safs.selenium.DocumentParser#getAttribute(String, String, String)
	 */
	public String getAttribute(Selenium selenium, String xpath, String attribute){
		String value = null;
		DocumentParser parser = getDocumentParser(selenium);
		if(parser==null){
			Log.error("Can NOT get the document parser, it is null");
			return null;
		}

		Log.debug("Object's selenium xpath is "+xpath+" ; try to get value for attribute "+attribute);
		value = parser.getAttribute(parser.getUrl(), xpath, attribute);

		return value;
	}

	/**
	 * <em>Purpose:</em>	Return the attributes for an element indicated by xpath
	 * <em>Note:</em>		This method will try to DocumentParser to get the properties<br>
	 * @param selenium      The instance of Selenium
	 * @param xpath         Xpath to represent the element in html page
	 * @return              The attributes
	 * @see org.safs.selenium.DocumentParser#getAttributes(String, String)
	 */
	public HashMap getAttributes(Selenium selenium, String xpath){
		HashMap value = null;
		DocumentParser parser = getDocumentParser(selenium);
		if(parser==null){
			Log.error("Can NOT get the document parser, it is null");
			return null;
		}

		Log.debug("Object's selenium xpath is "+xpath+" ; try to get all its attributes ");
		value = parser.getAttributes(parser.getUrl(), xpath);

		return value;
	}

	/**
	 * Gets all the xpaths for a certain html tag
	 * @param tag html tag of the components to get the xpaths for
	 * @param selenium current selenium object
	 * @return array of all the xpath strings
	 */
	public String[] getAllXPaths(String tag, Selenium selenium){
//		String attributeString = "var attrcheck = new Array();";
//		String tagString = "var tags = new Array();tags[0] = \"" + tag + "\";";
//		String result = selenium.getEval(attributeString+tagString+"SAFSgetXpath(tags,attrcheck);");
//		String [] results = result.split(";");
//		return results;

		DocumentParser parser = getDocumentParser(selenium);
		String[] tags = {tag};
		Document doc = parser.getDocument(parser.getUrl(), false);
		List<String> allXPaths = parser.getAllXPath(doc, tags);

		return allXPaths.toArray(new String[0]);
	}

	@Override
	public List<?> extractListItems(Object obj, String countProp, String itemProp)
	throws SAFSException {
		//TODO Auto-generated method stub
		return null;
	}

	@Override
	public Tree extractMenuBarItems(Object obj) throws SAFSException {
		//TODO Auto-generated method stub
		return null;
	}

	@Override
	public Tree extractMenuItems(Object obj) throws SAFSException {
		//TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object findPropertyMatchedChild(Object obj, String property,
			String bench, boolean exactMatch)
	throws SAFSObjectNotFoundException {
		//TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getListItem(Object obj, int i, String itemProp)
	throws SAFSException {
		//TODO Auto-generated method stub
		return null;
	}

    private native boolean focusWindowByCaption(String title);
    private native void maximizeWindowByCaption(String title);
    private native void minimizeWindowByCaption(String title);
    private native void restoreWindowByCaption(String title);

    static {
    	System.loadLibrary("ComponentFunctions");
    }
	/**
	 * Uses ComponentFunctions.dll to focus the window with the given recognition string.
	 * @param winRec window to be focused
	 * @return true if window was found, false otherwise
	 */
	public boolean setWindowFocus(String winRec){
		winRec = parseWinRecForRegex(winRec);
		boolean focused = focusWindowByCaption(winRec);
		if(focused){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {}
		}
		return focused;
	}

	/**
	 * Uses Selenium to focus the window with the given window id.
	 * @param windowId  window's name or title
	 * @param selenium  the instance of Selenium object
	 * @return true if window was found, false otherwise
	 */
	public boolean setWindowFocus(String windowId, Selenium selenium){
		boolean focused=false;

		try{
			if(selectWindow(selenium, windowId, 1)){
				selenium.windowFocus();
				focused=true;
			}
		}catch(Exception e){
			Log.debug("Focused fail: "+e.getMessage());
			focused=false;
		}

		if(focused){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {}
		}
		return focused;
	}

	/**
	 * Uses Selenium to maximize the window with the given window id.<br>
	 * <em>Note:</em>   The window can't be resotre after it is maximized by this method.
	 * @param windowId  window's name or title
	 * @param selenium  the instance of Selenium object
	 * @return true if window was maximized, false otherwise
	 */
	public boolean maximizeWindow(String windowId, Selenium selenium){
		boolean maximized = false;

		try{
			if(selectWindow(selenium, windowId, 1)){
				selenium.windowMaximize();
				maximized = true;
			}
		}catch(Exception e){
			Log.debug("Maximize fail: "+e.getMessage());
			maximized = false;
		}

		return maximized;
	}

	/**
	 * Uses ComponentFunctions.dll to maximize the window with the given recognition string.
	 * @param winRec window to be maximize
	 */
	public void maximizeWindow(String winRec){
		winRec = parseWinRecForRegex(winRec);
		maximizeWindowByCaption(winRec);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
	}

	/**
	 * Uses ComponentFunctions.dll to maximize the window with the given recognition string.
	 * @param winRec window to be maximize
	 */
	public void minimizeWindow(String winRec){
		winRec = parseWinRecForRegex(winRec);
		minimizeWindowByCaption(winRec);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
	}

	/**
	 * Uses ComponentFunctions.dll to maximize the window with the given recognition string.
	 * @param winRec window to be maximize
	 */
	public void restoreWindow(String winRec){
		winRec = parseWinRecForRegex(winRec);
		restoreWindowByCaption(winRec);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
	}

	private String parseWinRecForRegex(String winRec){
		if(winRec.indexOf(";")> -1){
			winRec = (winRec.split(";")[1]).split("=")[1];
		}
		//TODO:Escape anything else that could be reg ex operator.
		if(winRec.indexOf("{") == 0 && winRec.indexOf("}") == winRec.length()-1){
			winRec = winRec.substring(1);
			winRec = winRec.substring(0,winRec.length()-1);
			winRec = winRec.replaceAll("\\*", ".*");
			winRec = winRec.replaceAll("\\?", ".?");
		} else {
			winRec = winRec.replaceAll("\\*", "\\*");
			winRec = winRec.replaceAll("\\?", "\\?");
		}
		return winRec;
	}

	/**
	 * This method will try to match the longest-frame-prefix-xpath for the input xpath.<br>
	 * We try the key (xpathOfFrame) one by one of cache frames, the keys are descending ordered<br>
	 * that is to say, the longest xpath will be tried first, the shortest last<br>
	 * If we find one kye is the prefix of input xpath, we get the object HtmlFrameComp from cache<br>
	 * and return it.<br>
	 * This method will also store the final component's xpath in the instance of HtmlFrameComp<br>
	 * as its childXpath<br>
	 * @param xpath
	 * @return
	 */
	private HtmlFrameComp getPrefixingFrame(String xpath){
		HtmlFrameComp frameComp = null;
		Set<String> keys = null;
		Iterator<String> iter = null;
		String frameXpath = null;
		String childXpath = null;

		//frames is a TreeMap, it stores the frame's xpath as key in descending order
		//according to its length
		if(frames!=null && !frames.isEmpty()){
			keys = frames.keySet();
			iter = keys.iterator();
			while(iter.hasNext()){
				frameXpath = iter.next();
				if(xpath.startsWith(frameXpath)){
					Log.debug("SGU.getPrefixingFrame() ################## MATCHED PREFIX = "+frameXpath+"; FOR XPATH="+xpath);
					//Need to check the child xpath, if it begin with "/", if not continue find the next match
					//If we have /HTML/FRAMESET/FRAME in the cache, but we try to match prefix for /HTML/FRAMESET/FRAMESET/FRAME/HTML/BODY
					//we will get a wrong result, it will consider SET/FRAME/HTML/BODY as child contained in /HTML/FRAMESET/FRAME; while
					//the correct result is that /HTML/BODY is child of /HTML/FRAMESET/FRAMESET/FRAME
					childXpath = xpath.substring(frameXpath.length());
					if(childXpath.startsWith("/")){
						frameComp = frames.get(frameXpath);
						frameComp.setChildXpath(childXpath);
						//Should never happen
						if(!frameXpath.equals(frameComp.getFullXpath())){
							frameComp.setFullXpath(frameXpath);
							Log.error("SGU.getPrefixingFrame() ############# key not match frame's full xpath");
						}
						break;
					}
				}
			}
		}

		return frameComp;
	}

	private void addPrefixingFrameToCache(String xpath, HtmlFrameComp frame){
		if(!frames.containsKey(xpath)){
			frames.put(xpath, frame);
		}
	}

	public void clearFramesCache(){
		frames.clear();
	}
}
