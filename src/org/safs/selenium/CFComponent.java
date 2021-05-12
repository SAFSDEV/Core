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
package org.safs.selenium;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.safs.ComponentFunction;
import org.safs.IndependantLog;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSObjectNotFoundException;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.TestRecordHelper;
import org.safs.model.commands.GenericMasterFunctions;
import org.safs.robot.Robot;
import org.safs.selenium.util.JavaScriptFunctions;
import org.safs.text.INIFileReader;
import org.safs.tools.drivers.DriverConstant;
import org.safs.tools.input.CreateUnicodeMap;
import org.safs.tools.input.InputKeysParser;
import org.safs.tools.input.RobotKeyEvent;
import org.safs.tools.stringutils.StringUtilities;

import com.thoughtworks.selenium.Selenium;
/**
 * Component Functions for Selenium.
 * <p>
 * For some functionality we use the java.awt.Robot and our own InputKeysParser classes.
 * <p>
 * InputKeysParser requires the availability of a SAFSKeycodeMap.dat file which is normally
 * located in the SAFS\lib directory.  This file will be loaded through ClassLoader.getSystemResourceAsStream.
 *
 * @author  Philippe Sabourin
 * @since   OCT 10, 2006
 *
 *   <br>   OCT 10, 2006    (PHSABO) Original Release
 *   <br>   JAN 16, 2007    (Carl Nagle) Removed Java 1.5 dependency.
 *   <br>   AUG 01, 2007    (Carl Nagle) Made search for SAFSKeycodeMap.dat use the whole System CLASSPATH.
 *   <br>   FEB 01, 2010    (Carl Nagle) expanded casting of SeleniumGUIUtilities where needed.
 *   <br>   JUN 21, 2011    (Lei Wang) Add two propeties winObject and compObject of type SGuiObject.
 *                                    Modify method process(), componentProcess() and maximize(): use
 *                                    the new focus and maximize method provided by SeleniumGUIUtilities.
 *   <br>   JUN 28, 2011    (Lei Wang) Update 12 keywords.
 *   <br>   JUL 04, 2011    (Lei Wang) Update keywords: maximize, minimize, restore, add keyword shiftclick.
 *   <br>   MAR 05, 2014    (Lei Wang) Move some keywrod constants to ComponentFunction.
 *
 * @see java.awt.Robot
 * @see org.safs.tools.input.InputKeysParser
 * @see org.safs.tools.input.CreateUnicodeMap
 * @see java.lang.ClassLoader#getSystemResourceAsStream(java.lang.String)
 **/
public class CFComponent extends ComponentFunction{

	protected Selenium selenium;
	protected SeleniumGUIUtilities sUtils;
	protected STestRecordHelper sHelper;
	protected SGuiObject winObject;
	protected SGuiObject compObject;
	public static java.awt.Robot robot;
	public static InputKeysParser keysparser;

	static {
		try{
			robot = new java.awt.Robot();
			Log.debug("Selenium CFComponent awt.Robot ="+ robot);
		}
		catch(Exception x){
			Log.debug("Selenium CFComponent awt.Robot instantiation exception:", x);
		}
		try{
			InputStream stream = ClassLoader.getSystemResourceAsStream(CreateUnicodeMap.DEFAULT_FILE + CreateUnicodeMap.DEFAULT_FILE_EXT);
			//InputStream stream = CFComponent.class.getResourceAsStream(CreateUnicodeMap.DEFAULT_FILE + CreateUnicodeMap.DEFAULT_FILE_EXT);
			INIFileReader reader = new INIFileReader(stream, 0, false);
			Log.debug("Selenium CFComponent INIFileReader="+ reader);
			keysparser = new InputKeysParser(reader);
			Log.debug("Selenium CFComponent InputKeysParser="+ keysparser);
		}
		catch(Exception x){
			Log.debug("Selenium CFComponent INI Reader or parser instantiation exception:", x);
		}
	}

	public CFComponent(){
		super();
	}

	@Override
	public void process() {
		String debugmsg = this.getClass().getName()+".process(): ";
		// assume this for now..
		sHelper = (STestRecordHelper)testRecordData;
		try {
			selenium = SApplicationMap.getSelenium(sHelper.getWindowName());
		} catch (SAFSException e) {
			selenium = SApplicationMap.getSelenium("");
		}
		testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
		try {
			getHelpers( );
			sUtils = (SeleniumGUIUtilities)utils;
		} catch ( SAFSException ex ) {
			testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
			log.logMessage( testRecordData.getFac( ), "SAFSException: "
					+ ex.getClass( ).getName( ) + ", msg: " + ex.getMessage( ), FAILED_MESSAGE );
			return;
		}

		winObject = sHelper.getWindowTestObject();
		compObject = sHelper.getCompTestObject();
		if(winObject==null){
			Log.warn(debugmsg +" Window Object is null, try to get it through SeleniumGUIUtility. ");
			try{
				if ( sUtils.waitForObject( mapname, windowName, windowName, secsWaitForWindow) == 0 ) {
					Log.debug(debugmsg+" we got the window object through SeleniumGUIUtility.");
					winObject = ( ( STestRecordHelper )testRecordData ).getWindowTestObject();
				}else{
					Log.debug(debugmsg+" can not get window object through SeleniumGUIUtility: waitForObject() error.");
				}
			}catch(SAFSObjectNotFoundException e){
				Log.debug(debugmsg+" can not get window object through SeleniumGUIUtility: Exception="+e.getMessage());
			}
			if(winObject==null){
				Log.error(debugmsg +"  Window Object is still null");
				testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
				log.logMessage( testRecordData.getFac( ), action +" failed, due to 'Window Object is null'", FAILED_MESSAGE );
				return;
			}

		}

		if(compObject==null){
			Log.warn(debugmsg +" Component Object is null, try to get it through SeleniumGUIUtility.");
			if(!windowName.equals(compName)){
				try{
					if ( sUtils.waitForObject( mapname, windowName, compName, secsWaitForComponent) == 0 ) {
						Log.debug(debugmsg+" we get the component object through SeleniumGUIUtility.");
						compObject = ( ( STestRecordHelper )testRecordData ).getCompTestObject( );
					}else{
						Log.debug(debugmsg+" can not get component object through SeleniumGUIUtility: waitForObject() error.");
					}
				}catch(SAFSObjectNotFoundException e){
					Log.debug(debugmsg+" can not get component object through SeleniumGUIUtility: Exception="+e.getMessage());
				}

				if(compObject==null){
					Log.warn(debugmsg +"  Component Object is still null.");
				}
			}
		}

//		updateFromTestRecordData();//why this is commented?

		// do the work
		localProcess();

		if (testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED) {
			componentProcess(); //handle Generic keywords
		} else {
			Log.debug(debugmsg+" processed "+testRecordData);
			Log.debug(debugmsg+" params "+params);
		}

	}

	protected void localProcess() { }

	private void setFocusToWindow() throws SAFSException{
		if(!sUtils.setWindowFocus(winObject.getWindowId(),selenium)){
			Log.warn("Not able to focus window: " + testRecordData.getWindowGuiId());
		}
	}

	@Override
	protected void restore() throws SAFSException{
		setFocusToWindow();
		super.restore();
	}
	@Override
	protected void maximize() throws SAFSException{
		setFocusToWindow();
		super.maximize();
	}
	@Override
	protected void minimize() throws SAFSException{
		setFocusToWindow();
		super.minimize();
	}
	@Override
	protected void action_getGuiImage () throws SAFSException{
		setFocusToWindow();
		super.action_getGuiImage();
	}
	@Override
	protected void action_verifyGuiImageToFile () throws SAFSException {
		setFocusToWindow();
		super.action_verifyGuiImageToFile();
	}

	/**
	 *Subclass should give its own implementation<br>
	 */
	@Override
	protected void _setPosition(Point position) throws SAFSException {
		selenium.getEval("function move(){ window.moveTo("+position.x+","+position.y+"); return 0;} move();");
	}
	/**
	 *Subclass should give its own implementation<br>
	 */
	@Override
	protected void _setSize(Dimension size) throws SAFSException {
		selenium.getEval("function resize(){ window.resizeTo("+size.width+","+size.height+"); return 0;} resize();");
	}

	/**
	 * Perform the "restore" action on the current window.
	 */
	@Override
	protected void _restore() throws SAFSException{
		String message = null;

		try{
			sUtils.restoreWindow(testRecordData.getWindowGuiId());
			return;
		}catch(Throwable se){
			message = "Can not resotre window by native function. Exception="+se.getMessage();
			Log.warn(message);
			//If we fail to restore window, try the SAFS Robot to do.
			try{
				super._restore();
				return;
			}catch(Exception e){
				message = "Can not resotre window by SAFS Robot. Exception="+se.getMessage();
				Log.error(message);
				throw new SAFSException(message);
			}
		}
	}

	/**
	 * Maximize the current window
	 */
	@Override
	protected void _maximize() throws SAFSException{
		String message = null;

		//The window can't be resotred if it is maximized by selenium API.
		//So use the SAFS Robot to maximize the windows firstly
		try {
			super._maximize();
			return;
		} catch (SAFSException e) {
			message = "Can't maximize window by SAFS Robot. Try Selenium API to maximize. Exception="+e.getMessage();
			Log.warn(message);
			if(!sUtils.maximizeWindow(winObject.getWindowId(), selenium)){
				throw new SAFSException("Fail to maximize by Selenium API.");
			}
		}
	}

	/**
	 * Minimize the current window
	 */
	@Override
	protected void _minimize() throws SAFSException{
		String message = null;

		try{
			sUtils.minimizeWindow(testRecordData.getWindowGuiId());
			return;
		}catch(Throwable se){
			message = "Can not minimize window with native function. Exception="+se.getMessage();
			Log.warn(message);
			//If we fail to minimize window, try the SAFS Robot to do.
			try{
				super._minimize();
				return;
			}catch(Exception e){
				message = "Fail to minimize window by SAFS Robot. Exception="+message;
				Log.error(message);
				throw new SAFSException(message);
			}
		}
	}

	/**
	 * Close the current window
	 */
	@Override
	protected void closeWindow() throws SAFSException{
		setFocusToWindow();

		selenium.close();
		testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		String msg = genericText.convert("success3", windowName +":"+ compName + " "+ action +" successful.",
				windowName, compName, action);
		log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
		selenium.stop();// closes FF browser, but not IE6
		SApplicationMap.removeSelenium(windowName);
	}

	@Override
	protected void setPosition() throws SAFSException {
		setFocusToWindow();
		super.setPosition();
	}

//	protected int waitForObject(String mapname, String windowName, String compName, int secii) throws SAFSException{
//		return sUtils.waitForObject( mapname, windowName, compName, secii );
//	}

	/**
	 * Checks to see whether an element is visible in the browser
	 * @param locator xpath of the element to be check
	 * @return whether the element is visible
	 */
	private boolean compIsVisible(String locator){
		boolean isShowing = false;
		try{ isShowing = selenium.isVisible(locator);}
		catch(RuntimeException rx){
			Log.debug("Selenium.compIsVisible RuntimeException ignored (false).");
		}
		return isShowing;
	}

	@Override
	protected boolean exist() throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false);

		try {
			boolean isShowing = false;
			// wait for the window - secTimout of 0 means a single try to search for given comps
			// waitForObject checks only component validity and tells nothing about its visibility
			int status = sUtils.waitForObject(mapname, windowName, compName, 0);

			if(status==0){
				// component search succeeded
				isShowing = compIsVisible("//HTML[1]");
				SGuiObject obj;

				obj = sHelper.getCompTestObject();
				if( isShowing && (obj != null)){
					isShowing = compIsVisible(obj.getLocator());
				}
			}
			//else status!=0, which means that component search failed
			return isShowing;

		}catch(SAFSObjectNotFoundException sonfe){
			Log.warn(debugmsg+" Met Exception "+StringUtils.debugmsg(sonfe));
		}

		return false;
	}

	/** <br><em>Purpose:</em> verifyProperty
	 **/
	@Override
	protected void verifyProperty() throws SAFSException {
		testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
		if ( params.size( ) < 2 ) {
			paramsFailedMsg( windowName, compName );
			return;
		}
		String prop = ( String ) iterator.next( );
		String val =  ( String ) iterator.next( );
		String strcase = "";
		boolean ignorecase = false;
		try { strcase = ( String ) iterator.next( ); } catch( Exception x ) {;}
		ignorecase = !StringUtils.isCaseSensitive(strcase);

		Log.info( ".....CFComponent.process; ready to do the VP for prop : " + prop + " val: " + val );
		String rval = null;

		int status = sUtils.waitForObject( mapname, windowName, compName, 15 );

		SGuiObject comp = null;
		String _type = null;

		if ( status == 0 ) {
			comp = ( ( STestRecordHelper )testRecordData ).getCompTestObject( );
			_type = ( ( STestRecordHelper )testRecordData ).getCompType();
			//TODO: Changed this!

			//rval = selenium.getEval("var xpath = \""+comp.getLocator()+"\";var prop = \""+prop+"\";SAFSgetAttribute(xpath,prop);");
			rval = sUtils.getAttribute(selenium, comp.getLocator(), prop);
			if(rval == null){
				Log.info("Selenium property '"+ prop +"' for component '"+ compName +"' of type '"+ _type +"' not found.  Trying alternatives...");
				//assume attribute not found
				if (((_type.equalsIgnoreCase("CHECKBOX"))||
				     (_type.equalsIgnoreCase("RADIOBUTTON")))
				    &&
				    (prop.equalsIgnoreCase("CHECKED"))
				    &&
				     (val.equalsIgnoreCase("FALSE"))
				    ){
				    rval = "False";
				}
				if (((_type.equalsIgnoreCase("LISTBOX"))||
					 (_type.equalsIgnoreCase("COMBOBOX")))
					&&
					(prop.equalsIgnoreCase("SELECTED"))
					&&
					 (val.equalsIgnoreCase("FALSE"))
					){
					rval = "False";
				}
			}
		}

		Log.info("Selenium property '"+ prop +"' for component '"+ compName +"' of type '"+ _type +"' contains value '"+ rval +"'");

		// it is possible the property name is not valid for Selenium
		// it may be a property valid in a different engine
		// TODO: but do we want to assume we are running with other engines?
		if ( rval == null || rval.equals("null") ) {
			testRecordData.setStatusCode( StatusCodes.SCRIPT_NOT_EXECUTED );
			return;
		}

		if ( ( ( !ignorecase ) && ( val.equals( rval ) ) )
				|| ( ignorecase && ( val.equalsIgnoreCase( rval ) ) ) ) {
			// set status to ok
			testRecordData.setStatusCode( StatusCodes.NO_SCRIPT_FAILURE );
			altText = genericText.convert("bench_matches",
					  compName+":"+prop +" matches expected value \""+ val +"\"",
					  compName+":"+prop, val);
			log.logMessage(testRecordData.getFac(), altText, PASSED_MESSAGE);
		} else {
			// failed
			testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
			altText = genericText.convert("bench_not_match",
	  			  compName+":"+prop +" did not match expected result \""+ val +"\"",
	  			  compName+":"+prop, val);
			String detail = genericText.convert("actual_value", "ActualValue='"+ rval +"'", rval);
			standardFailureMessage(altText, compName+":"+prop+" "+detail);
		}
	}


	/** <br><em>Purpose:</em> componentClick
	 **/
	@Override
	protected void componentClick() throws SAFSException{
		if(compObject==null){
			throw new SAFSException("Component SGuiObject is null.");
		}
		String locator = compObject.getLocator();
		Log.debug("component's locator="+locator);

		setFocusToWindow();

		try{
			if ( action.equalsIgnoreCase( CLICK ) || action.equalsIgnoreCase( COMPONENTCLICK )){
				selenium.click(locator);
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			} else if ( action.equalsIgnoreCase( CTRLCLICK ) ){
				selenium.controlKeyDown();
				selenium.click(locator);
				selenium.controlKeyUp();
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			}else if ( action.equalsIgnoreCase( DOUBLECLICK ) ){
				selenium.doubleClick(locator);
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			} else if ( action.equalsIgnoreCase( RIGHTCLICK ) ){
				selenium.mouseDownRight(locator);
				selenium.mouseUpRight(locator);
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			}else if ( action.equalsIgnoreCase( CTRLRIGHTCLICK ) ){
				selenium.controlKeyDown();
				selenium.mouseDownRight(locator);
				selenium.mouseUpRight(locator);
				selenium.controlKeyUp();
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			}else if(action.equalsIgnoreCase(SHIFTCLICK)){
				selenium.shiftKeyDown();
				selenium.click(locator);
				selenium.shiftKeyUp();
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			}
		}catch(Exception e){
			//Try SFAS robot to do the work.
			scrollToComponent(compObject);
			Rectangle compRect = getComponentBounds(compObject);

			if(compRect==null){
				Log.error("component rectangle is null.");
				throw new SAFSException("component rectangle is null.");
			}

			//Log.info("Selenium mouseMove to: " +(int)compRect.getCenterX()+":"+(int)compRect.getCenterY());
			if ( action.equalsIgnoreCase( CLICK ) || action.equalsIgnoreCase( COMPONENTCLICK )){
				robot.mouseMove((int)compRect.getCenterX(),(int)compRect.getCenterY());
				robot.mousePress(KeyEvent.BUTTON1_MASK);
				robot.mouseRelease(KeyEvent.BUTTON1_MASK);
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			} else if ( action.equalsIgnoreCase( CTRLCLICK ) ){
				robot.keyPress(KeyEvent.VK_CONTROL);
				robot.mouseMove((int)compRect.getCenterX(),(int)compRect.getCenterY());
				robot.mousePress(KeyEvent.BUTTON1_MASK);
				robot.mouseRelease(KeyEvent.BUTTON1_MASK);
				robot.keyRelease(KeyEvent.VK_CONTROL);
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			}else if ( action.equalsIgnoreCase( DOUBLECLICK ) ){
				robot.mouseMove((int)compRect.getCenterX(),(int)compRect.getCenterY());
				robot.mousePress(KeyEvent.BUTTON1_MASK);
				robot.mouseRelease(KeyEvent.BUTTON1_MASK);
				robot.mousePress(KeyEvent.BUTTON1_MASK);
				robot.mouseRelease(KeyEvent.BUTTON1_MASK);
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			} else if ( action.equalsIgnoreCase( RIGHTCLICK ) ){
				robot.mouseMove((int)compRect.getCenterX(),(int)compRect.getCenterY());
				robot.mousePress(KeyEvent.BUTTON3_MASK);
				robot.mouseRelease(KeyEvent.BUTTON3_MASK);
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			}else if ( action.equalsIgnoreCase( CTRLRIGHTCLICK ) ){
				robot.keyPress(KeyEvent.VK_CONTROL);
				robot.mouseMove((int)compRect.getCenterX(),(int)compRect.getCenterY());
				robot.mousePress(KeyEvent.BUTTON3_MASK);
				robot.mouseRelease(KeyEvent.BUTTON3_MASK);
				robot.keyRelease(KeyEvent.VK_CONTROL);
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			}else if(action.equalsIgnoreCase(SHIFTCLICK)){
				robot.keyPress(KeyEvent.VK_SHIFT);
				robot.mousePress(KeyEvent.BUTTON1_MASK);
				robot.mouseRelease(KeyEvent.BUTTON1_MASK);
				robot.keyRelease(KeyEvent.VK_SHIFT);
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			}
		}

		// log success message and status
		if(testRecordData.getStatusCode() == StatusCodes.NO_SCRIPT_FAILURE){
			String msg = genericText.convert("success3", windowName +":"+ compName + " "+ action +" successful.",
					windowName, compName, action);
			log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
			return;
			// just in case. (normally any failure should have issued an Exception)
		} else {
			log.logMessage(testRecordData.getFac(),
					action + "\n" +
					testRecordData.getFilename() +
					" at Line " + testRecordData.getLineNumber() + ", " +
					testRecordData.getFac(),
					FAILED_MESSAGE);
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
		}
	}

	/**
	 * <em>Purpose:</em> Remove the content from an input box.<br>
	 * @param _comp
	 */
	private void clearText(SGuiObject _comp){
		String debugmsg = getClass().getName()+".clearText(): ";

		try{
			selenium.type(_comp.getLocator(), "");
		}catch(Exception e){
			Log.debug(debugmsg+" Exception occur. "+e.getMessage());
			selenium.click(_comp.getLocator());
			RobotKeyEvent.doKeystrokes(keysparser.parseInput("^a{ExtDelete}"), robot, 0);
			//RobotKeyEvent.doKeystrokes(keysparser.parseInput("{ExtHome}+{ExtEnd}{ExtDelete}"), robot, 0);
		}
	}

	/** <br><em>Purpose:</em> inputKeys and inputCharacters
	 **/
	@Override
	protected void inputKeystrokes() throws SAFSException{
		String keys = "";

		if ( params.size( ) < 1 ) {
			Log.error("Need at least one parameter.");
			paramsFailedMsg( windowName, compName );
			return;
		}
		keys = (String) iterator.next();
		Log.debug("Input Parameter '"+keys+"'");

		if(GenericMasterFunctions.INPUTKEYS_KEYWORD.equalsIgnoreCase(action)){
			try {
				setFocusToWindow();
				if(compObject==null){
					Log.warn("component object is null.");
				}else{
					clearText(compObject);
				}
				Robot.inputKeys(keys);

				issuePassedSuccessUsing(keys);
			} catch (AWTException e) {
				Log.debug("Exception occur: "+e.getMessage());
				issueActionFailure("Exception occur "+e.getMessage());
			}

		}else{//InputCharacters
			try{
				if(compObject==null){
					Log.debug("component object is null");
					throw new SAFSException("component object is null");
				}else{
					clearText(compObject);
					selenium.type(compObject.getLocator(), keys);
					issuePassedSuccessUsing(keys);
				}
			}catch(Exception e){
				try {
					Log.debug("Can not input character by selenium API, try SAFS Robot.");
					Robot.inputChars(keys);
					issuePassedSuccessUsing(keys);
				} catch (AWTException awte) {
					Log.debug("Fail to inpu the character, Exception "+awte.getMessage());
					issueActionFailure("Exception occur "+awte.getMessage());
				}
			}
		}
	}

	/** @return Rectangle, the absolute rectangle on screen for the window */
	@Override
	protected Rectangle getWindowRectangleOnScreen(){
		Rectangle rect = getComponentBounds(winObject);

	    if(rect==null) IndependantLog.warn("Fail to get bounds for "+windowName+":"+windowName +" on screen.");
	    return rect;
	}

	/** @return Rectangle, the absolute rectangle on screen for the component */
	@Override
	protected Rectangle getComponentRectangleOnScreen(){
		return getComponentRectangle();
	}

	/** @return Rectangle, the absolute rectangle on screen for the component */
	@Override
	protected Rectangle getComponentRectangle(){
		Rectangle compRect = getComponentBounds(component());

	    if(compRect==null) IndependantLog.warn("Fail to get bounds for "+windowName+":"+compName +" on screen.");
	    return compRect;
	}

	@Override
	protected Collection<String> captureObjectData() throws SAFSException {
		Collection<String> contents = new ArrayList<String>();

		String prop = PROPERTY_innerText;  //this isn't the property for all objects...
		String browser = TestRecordHelper.getConfig().getNamedValue(DriverConstant.SECTION_SAFS_SELENIUM, "BROWSER");
		if(!(browser.equals("*iexplore") || browser.equals("*piiexplore"))){
			prop = PROPERTY_textContent;
		}

		String myClass = getClass().getSimpleName();
		if (myClass.equals("CFList") || myClass.equals("CFComboBox")) {
			prop = PROPERTY_DOT_itemText;
		}

		//Get text from object...
		Object rval = null;
		if(compObject!=null){
			StringBuffer jsScript = new StringBuffer();
			jsScript.append(JavaScriptFunctions.getSAFSgetElementFromXpathFunction());
			jsScript.append(JavaScriptFunctions.getSAFSgetAttributeFunction());
			jsScript.append(" var xpath = \""+compObject.getLocator()+"\";");
			jsScript.append(" var prop = \""+prop+"\";");
			jsScript.append(" SAFSgetAttribute(xpath,prop);");
			rval = selenium.getEval(jsScript.toString());
		}
		if (rval==null || rval.equals("null")) {
			throw new SAFSException("read property("+prop+") value is null", SAFSException.CODE_CONTENT_ISNULL);
		}

		Log.info("..... real value is: "+rval+", "+rval.getClass().getName());

		String value = null;
		if(rval instanceof Collection){
			@SuppressWarnings("rawtypes")
			Iterator ii = ((Collection)rval).iterator();

			while(ii.hasNext()){
				value = getStringValue(ii.next());
				contents.add(value);
			}
		}else{
			value = getStringValue(rval);
			contents.add(value);
		}

		return contents;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Map<String, Object> getProperties() throws SAFSException{
		if(compObject!=null){
			return sUtils.getAttributes(selenium, compObject.getLocator());
		}else{
			Log.debug("compObject is null, can't get properties.");
			return null;
		}
	}

	@Override
	protected Object getPropertyObject(String propertyName) throws SAFSException{
		String rval = null;

		if(compObject==null){
			int status = waitForObject( mapname, windowName, compName, secsWaitForComponent);
			if(status==0) compObject = ((STestRecordHelper)testRecordData).getCompTestObject( );
		}

		if ( compObject != null) {
			Log.debug("assignPropVar for "+ compObject.getLocator()+":"+propertyName);
			rval = sUtils.getAttribute(selenium, compObject.getLocator(), propertyName);
			if(rval==null){
				rval = selenium.getEval("var xpath = \""+compObject.getLocator()+"\";var prop = \""+propertyName+"\";SAFSgetAttribute(xpath,prop);");
			}
		}else{
			Log.warn("component object is null.");
		}

		return rval;
	}

	/**
	 * Scrolls to a component then left clicks on it.
	 * @param o component to be clicked
	 */
	protected void scrollToAndClickComponent(SGuiObject o){
		scrollToComponent(o);
		Rectangle compRect = getComponentBounds(o);
		try{
			robot.mouseMove((int)compRect.getCenterX(),(int)compRect.getCenterY());
			robot.mousePress(KeyEvent.BUTTON1_MASK);
			robot.mouseRelease(KeyEvent.BUTTON1_MASK);
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		}
		catch(NullPointerException npe){
			Log.debug("IGNORING Selenium NPE for scrollToAndClick '"+ o.getWindowId()+"':", npe);
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
		}
	}

	/**
	 * Scrolls to a component on the page
	 * @param o component to be scrolled to
	 */
	protected void scrollToComponent(SGuiObject o){
		String xpath = sUtils.navigateFrames(o.getLocator(), selenium);
		long left = 0;
		long top = 0;

		try{ left = selenium.getElementPositionLeft(xpath).longValue();}catch(Exception x){}
		try{ top = selenium.getElementPositionTop(xpath).longValue();}catch(Exception x){}

		String eval = selenium.getEval("SAFSgetClientScrollInfo();");
		String[] values = eval.split(";");

		long width = 0;
		long height = 0;
		long scrollTop = 0;
		long scrollLeft = 0;
		try{ width  = Integer.parseInt(values[0]);}catch(Exception x){;}
		try{ height = Integer.parseInt(values[1]);}catch(Exception x){;}
		try{ scrollLeft = Integer.parseInt(values[2]);}catch(Exception x){;}
		try{ scrollTop = Integer.parseInt(values[3]);}catch(Exception x){;}

		if((left < scrollLeft || left > scrollLeft+width)|| (top < scrollTop || top > scrollTop+height)){
			eval = selenium.getEval("window.scrollTo("+ left +","+ top +");");
		}
	}

	/**
	 * Gets the size of the component
	 * @param o component
	 * @return size of the component
	 */
	private Rectangle getComponentBounds(SGuiObject o){
		return sUtils.getComponentBounds(o, selenium);
	}

	/**
	 * Gets the size of the component
	 * @param s component
	 * @return size of the component
	 */
	protected Rectangle getComponentBounds(String s){
		return sUtils.getComponentBounds(s, selenium);
	}

	@Override
	protected boolean performHoverMouse(Point point, int milliseconds) throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false);

		try{
			//TODO handle the point
			String locator = component().getLocator();
			IndependantLog.debug(debugmsg+" hover on element '"+locator+"'");
			selenium.mouseOver(locator);
			StringUtilities.sleep(milliseconds);
			selenium.mouseOut(locator);

		}catch(Exception e){
			IndependantLog.error(debugmsg+" fail to hover, due to "+StringUtils.debugmsg(e));
			return false;
		}
		return true;
	}

	private SGuiObject component(){
		String debugmsg = StringUtils.debugmsg(false);

		if(compObject==null){
			IndependantLog.warn(debugmsg+" the 'componet object' is null, use the 'window object' instead.");
			//winObject will never be null, as in process() it has been checked.
			return winObject;
		}
		return compObject;
	}
}
