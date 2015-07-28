/** 
 * Copyright (C) SAS Institute. All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.robot;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.Vector;

import org.safs.IndependantLog;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.StringUtils;
import org.safs.jvmagent.AgentClassLoader;
import org.safs.text.INIFileReader;
import org.safs.tools.input.CreateUnicodeMap;
import org.safs.tools.input.InputKeysParser;
import org.safs.tools.input.RobotClipboardPasteEvent;
import org.safs.tools.input.RobotKeyEvent;
import org.safs.tools.stringutils.StringUtilities;

/**
 * Utility functions for common user interactions on the system.
 * 
 * @author Carl Nagle Sept 09, 2008
 * @see java.awt.Robot
 * @see org.safs.tools.input.CreateUnicodeMap
 * @see org.safs.tools.input.InputKeysParser
 * 
 * <br> JunwuMa SEP 23, 2008  Added doEvents(Robot, Vector) running RobotClipboardPasteEvent with proper delay time
 *                            for ctrl+v(paste) job done. 
 * <br> Carl Nagle  MAR 25, 2009  Added MouseDrag support
 * <br> Carl Nagle  APR 03, 2009  Enhance MouseDrag support to work for more apps.
 * <br> LeiWang JUL 04, 2011  Add methods to maximize, minimize, restore, close window by key-mnemonic.
 * <br> LeiWang JAN 04, 2015  Add method mouseWheel().
 * <br> Carl Nagle  MAY 29, 2015  Add support for setMillisBetweenKeystrokes
 */
public class Robot {
	
	static java.awt.Robot robot = null;
	static InputKeysParser keysparser = null;
	static int millisBetweenKeystrokes = 1;
	
	public final static Dimension SCREENSZIE = Toolkit.getDefaultToolkit().getScreenSize();
	
	/**
	 * Set the delay in milliseconds between Robot key events.
	 * @param msBetween >= 0
	 * @see #inputKeys(String)
	 * @see #inputChars(String)
	 * @see #doEvents(java.awt.Robot, Vector, int)
	 */
	public static void setMillisBetweenKeystrokes(int msBetween){
		millisBetweenKeystrokes = msBetween >= 0 ? msBetween : millisBetweenKeystrokes;
	}
	
	/**
	 * Retrieve the active java.awt.Robot from the JVM.
	 * If one does not yet exist the routine will attempt to instantiate it.
	 * @return java.awt.Robot or a thrown java.awt.AWTException
	 * @throws java.awt.AWTException
	 */
	public static java.awt.Robot getRobot() throws java.awt.AWTException {
		if(robot==null) robot = new java.awt.Robot();
		return robot;
	}

	/**
	 * Retrieve the active InputKeysParser.
	 * If one does not yet exist the routine will attempt to instantiate it.
	 * @return InputKeysParser or null
	 * @see org.safs.tools.input.InputKeysParser
	 */
	public static InputKeysParser getInputKeysParser(){
		if(keysparser==null){
			URL mapurl = null;
			URL custmapurl = null;
			InputStream stream = null;
			try{
				//InputStream stream = ClassLoader.getSystemResourceAsStream(CreateUnicodeMap.DEFAULT_FILE + CreateUnicodeMap.DEFAULT_FILE_EXT);
				mapurl = getResourceURL(Robot.class,CreateUnicodeMap.DEFAULT_FILE + CreateUnicodeMap.DEFAULT_FILE_EXT);
				custmapurl = AgentClassLoader.findCustomizedJARResource(mapurl, CreateUnicodeMap.DEFAULT_FILE + CreateUnicodeMap.DEFAULT_FILE_EXT);
				if (custmapurl != null) stream = custmapurl.openStream();
				else stream = mapurl.openStream();
				INIFileReader reader = new INIFileReader(stream, 0, false);
				Log.debug("SAFS Robot InputKeysParser initialization: "+ reader);
				keysparser = new InputKeysParser(reader);
				Log.debug("SAFS Robot InputKeysParser: "+ keysparser);
			}catch(MissingResourceException mr){ // from getResourceURL
				Log.debug("SAFS Robot.getInputKeysParser *** MissingResourceException *** "+ mr.getMessage());
			}catch(IOException io){ // from URL.openStream
				Log.debug("SAFS Robot.getInputKeysParser *** URL.openStream IOException *** "+ io.getMessage());
			}
		}
		return keysparser;
	}

	protected static URL getResourceURL(Class clazz, String aresource){
		ClassLoader gcdloader = clazz.getClassLoader();
		Log.info("Robot ClassLoader:"+ gcdloader.toString()); // C:\SAFS\lib\safs.jar
		
		URL domain = clazz.getProtectionDomain().getCodeSource().getLocation();
		Log.info("Robot CodeSoure.Location Ptcl:"+ domain.getProtocol()); // file
		Log.info("Robot CodeSoure.Location Path:"+ domain.getPath()); // ...com.rational.test.ft.core_7.0.2...jar
		Log.info("Robot CodeSoure.Location File:"+ domain.getFile()); // ...com.rational.test.ft.core_7.0.2...jar

		URL jom =  clazz.getResource(aresource);
		if (jom == null) { //is null
			Log.info("Robot trying getClassLoader().getResource()");
			jom = gcdloader.getResource(aresource);
		}
		if (jom == null) { // is null
			Log.info("Robot trying getClassLoader().getSystemResource()");
			jom = gcdloader.getSystemResource(aresource);
		}
		if (jom == null) { // is null
			Log.info("Robot trying ClassLoader.getSystemClassLoader().getResource()");
			jom = ClassLoader.getSystemClassLoader().getResource(aresource);
		}
		if (jom == null) { // is null
			Log.info("Robot trying ClassLoader.getSystemClassLoader().getSystemResource()");
			jom = ClassLoader.getSystemClassLoader().getSystemResource(aresource);
		}
		ClassLoader contextloader = Thread.currentThread().getContextClassLoader();
		if (jom == null) { // is null
			Log.info("Robot trying contextloader getResource().");
			jom = contextloader.getResource(aresource);
			// !!! FINALLY WORKS !!! (as long as resource is in RFT project root directory :(
		}
		if(jom == null){ 
			Log.info("Robot trying contextloader getSystemResource().");
			jom = contextloader.getSystemResource(aresource);
		}
		if (jom == null){
			Log.info("Robot: trying AgentClassLoader with java.class.path="+ System.getProperty("java.class.path"));
			AgentClassLoader loader = new AgentClassLoader(System.getProperty("java.class.path"));
			jom = loader.getResource(aresource);
		}
		if (jom == null){
			Log.info("Robot: trying AgentClassLoader with SAFSDIR Env...");
			String safsjar = System.getenv("SAFSDIR")+ File.separator +"lib"+ File.separator +"safs.jar";
			Log.info("    "+ safsjar);
			AgentClassLoader loader = new AgentClassLoader(safsjar);
			jom = loader.getResource(aresource);
		}
		if (jom == null){
			Log.debug("Robot: dumping System Properties and throwing MissingResourceException for "+ aresource);
			java.util.Properties props = System.getProperties();
			java.util.Enumeration names = props.keys();
			String _name = null;
			while(names.hasMoreElements()){
				_name = (String) names.nextElement();
				Log.debug("    "+_name+"="+ System.getProperty(_name));
			}
			throw new java.util.MissingResourceException(aresource,aresource,aresource);
		}
		return jom;
	}
		
	/**
	 * Type keyboard input.
	 * The input goes to the current keyboard focus target.  The String input 
	 * can include all special characters and processing as documented in the  
	 * InputKeysParser class.
	 * 
	 * @param input -- the String of characters to enter.
	 * @return Object Currently we return a Boolean(true) object, but this may 
	 * be subject to change.
	 * @throws AWTException -- if there is a problem instantiating or using the 
	 * java.awt.Robot
	 * 
	 * @see org.safs.tools.input.InputKeysParser
	 * @see java.awt.Robot
	 * @see #setMillisBetweenKeystrokes(int)
	 * @see #doEvents(java.awt.Robot, Vector, int)
	 */
	public static Object inputKeys(String input) throws AWTException{
	   	Log.info("SAFS Robot processing InputKeys: "+ input);
	   	InputKeysParser parser = getInputKeysParser();
	   	java.awt.Robot bot = getRobot();
	   	Vector keystrokes = parser.parseInput(input);
	   	doEvents(bot, keystrokes, millisBetweenKeystrokes);
	   	return new Boolean(true);
	}
	
	/**
	 * Type keyboard input characters unmodified.  No special key processing.
	 * The input goes to the current keyboard focus target.  The String input 
	 * will be treated simply as literal text and typed as-is.
	 * 
	 * @param input -- the String of characters to enter.
	 * @return Object Currently we return a Boolean(true) object, but this may 
	 * be subject to change.
	 * @throws AWTException -- if there is a problem instantiating or using the 
	 * java.awt.Robot
	 * 
	 * @see org.safs.tools.input.InputKeysParser
	 * @see java.awt.Robot
	 * @see #setMillisBetweenKeystrokes(int)
	 * @see #doEvents(java.awt.Robot, Vector, int)
	 */
	public static Object inputChars(String input) throws AWTException{
	   	Log.info("SAFS Robot processing InputKeys: "+ input);
	   	InputKeysParser parser = getInputKeysParser();
	   	java.awt.Robot bot = getRobot();
	   	Vector keystrokes = parser.parseChars(input);
	   	doEvents(bot, keystrokes, millisBetweenKeystrokes);
	   	return new Boolean(true);
	}
	
	private static void doEvents(java.awt.Robot bot, Vector RobotKeys, int msBetweenEvents){
	   	if(bot == null)
	   		return;
		if (msBetweenEvents < 0) msBetweenEvents = 0;
	   	int pasteDelay = msBetweenEvents > 50 ? msBetweenEvents: 50;
	   	
		Iterator events = RobotKeys.iterator();
	   	Object event;	   	
	   	while(events.hasNext()){
   			event = events.next();
   			if(event instanceof RobotClipboardPasteEvent)
   				//setting ms_delay=50, for the Paste from Clipboard needs to wait until copying Clipboard finished.
   				((RobotClipboardPasteEvent)event).doEvent(bot, pasteDelay);
   			else
   				((RobotKeyEvent)event).doEvent(bot, msBetweenEvents);	   	
   		}		
	}

	private static void doEvents(java.awt.Robot bot, Vector RobotKeys){
		doEvents(bot, RobotKeys, 0);
	}
	
	/**
	 * Workhorse Click routine.  
	 * Allows us to Click--Press & Release--any combination of InputEvent.BUTTONn_MASK 
	 * any number of times. 
	 * 
	 * @param x screen X coordinate
	 * @param y screen Y coordinate
	 * @param buttonmask -- specific InputEvent.BUTTONn_MASK(s) 
	 * @param nclicks -- number of times to click (press and release)
	 * @return Object Currently we return a Boolean(true) object, but this may 
	 * be subject to change.
	 * 
	 * @throws java.awt.AWTException
	 * @see java.awt.Robot#mousePress(int)
	 * @see java.awt.event.InputEvent#BUTTON1_MASK
	 */
	public static Object click(int x, int y, int buttonmask, int nclicks)throws java.awt.AWTException{
		java.awt.Robot bot = getRobot();
		Log.info("Robot click at:"+ x +","+ y +" using button mask "+ buttonmask +" "+ nclicks +" times.");
		bot.mouseMove(x, y);
		for(int i = 0; i < nclicks; i++){
			bot.mousePress(buttonmask);
			bot.mouseRelease(buttonmask);
		}
		return new Boolean(true);
	}
	
	/**
	 * Workhorse Click with Keypress routine.  
	 * Allows us to Click--Press & Release--any combination of InputEvent.BUTTONn_MASK 
	 * any number of times with a single Key Press & Release. 
	 * 
	 * @param x screen X coordinate
	 * @param y screen Y coordinate
	 * @param buttonmask -- specific InputEvent.BUTTONn_MASK(s) 
	 * @param keycode -- specific keycode to press & release. Ex: KeyEvent.VK_SHIFT 
	 * @param nclicks -- number of times to click (press and release)
	 * @return Object Currently we return a Boolean(true) object, but this may 
	 * be subject to change.
	 * 
	 * @throws java.awt.AWTException
	 * @see java.awt.Robot#mousePress(int)
	 * @see java.awt.event.InputEvent#BUTTON1_MASK
	 */
	public static Object clickWithKeyPress(int x, int y, int buttonmask, int keycode, int nclicks)throws java.awt.AWTException{
		java.awt.Robot bot = getRobot();
		Log.info("Robot click at:"+ x +","+ y +" using button mask "+ buttonmask +", keycode "+ keycode +", "+ nclicks +" times.");
		bot.mouseMove(x, y);
		bot.keyPress(keycode);
		for(int i = 0; i < nclicks; i++){
			bot.mousePress(buttonmask);
			bot.mouseRelease(buttonmask);
		}
		bot.keyRelease(keycode);
		return new Boolean(true);
	}

	/**
	 * Move the mouse cursor to the specified x,y coordinates then perform a 
	 * single mousePress and Release to execute a Click.
	 * @param x
	 * @param y
	 * @return Object Currently we return a Boolean(true) object, but this may 
	 * be subject to change.
	 * @throws java.awt.AWTException if instantiating java.awt.Robot throws it.
	 * @see java.awt.Robot
	 */
	public static Object click(int x, int y) throws java.awt.AWTException{
		return click(x,y,InputEvent.BUTTON1_MASK,1);
	}

	
	/**
	 * Move the mouse cursor to the specified x,y coordinates then perform a 
	 * single mousePress and Release to execute a Click.
	 * @param x
	 * @param y
	 * @return Object Currently we return a Boolean(true) object, but this may 
	 * be subject to change.
	 * @throws java.awt.AWTException if instantiating java.awt.Robot throws it.
	 * @see java.awt.Robot
	 */
	public static Object doubleClick(int x, int y) throws java.awt.AWTException{
		return click(x,y,InputEvent.BUTTON1_MASK,2);
	}

	/**
	 * Move the mouse cursor to the specified x,y coordinates then perform a 
	 * single mousePress and Release to execute a RightClick.
	 * @param x
	 * @param y
	 * @return Object Currently we return a Boolean(true) object, but this may 
	 * be subject to change.
	 * @throws java.awt.AWTException if instantiating java.awt.Robot throws it.
	 * @see java.awt.Robot
	 */
	public static Object rightClick(int x, int y) throws java.awt.AWTException{
		return click(x,y,InputEvent.BUTTON3_MASK,1);
	}

	
	/**
	 * Move the mouse cursor to the specified Point coordinates then perform a 
	 * single mousePress and Release to execute a Click.
	 * This routine simply calls click with the x,y coordinates in the Point.
	 * @param Point
	 * @return Object returned from click above.
	 * @throws java.awt.AWTException if instantiating java.awt.Robot throws it.
	 * @see java.awt.Robot
	 * @see #click(int, int)
	 */
	public static Object click(java.awt.Point p) throws java.awt.AWTException{
		return click(p.x, p.y);
	}

	/**
	 * Move the mouse cursor to the specified Point coordinates then perform a 
	 * single mousePress and Release to execute a Click.
	 * This routine simply calls click with the x,y coordinates in the Point.
	 * @param Point
	 * @return Object returned from click above.
	 * @throws java.awt.AWTException if instantiating java.awt.Robot throws it.
	 * @see java.awt.Robot
	 * @see #click(int, int)
	 */
	public static Object rightClick(java.awt.Point p) throws java.awt.AWTException{
		return rightClick(p.x, p.y);
	}

	/**
	 * Move the mouse cursor to the specified Point coordinates then perform a 
	 * single mousePress and Release to execute a Click.
	 * This routine simply calls click with the x,y coordinates in the Point.
	 * @param Point
	 * @return Object returned from click above.
	 * @throws java.awt.AWTException if instantiating java.awt.Robot throws it.
	 * @see java.awt.Robot
	 * @see #click(int, int)
	 */
	public static Object doubleClick(java.awt.Point p) throws java.awt.AWTException{
		return doubleClick(p.x, p.y);
	}

	/**
	 * Move the mouse cursor to the specified start Point coordinates then perform a 
	 * single mousePress using buttonMasks and drag\move then Release the mouse button at the end point.
	 * @param Point screen coordinates to start of mouse press and drag.
	 * @param Point screen coordinates to end mouse drag and mouse release.
	 * @param int button masks to use during drag
	 * @return Object Currently we return a Boolean(true) object, but this may 
	 * be subject to change.
	 * @throws java.awt.AWTException if instantiating java.awt.Robot throws it.
	 * @see java.awt.Robot
	 */
	public static Object mouseDrag(java.awt.Point start, java.awt.Point end, int buttonMasks) throws java.awt.AWTException{
		java.awt.Robot bot = getRobot();
		Log.info("Robot mouseDrag from:"+ start +" to:"+ end +" using button mask "+ buttonMasks);
		bot.mouseMove(start.x+1, start.y+1);
		bot.delay(150);
		bot.mouseMove(start.x, start.y);
		bot.delay(400);
		bot.mousePress(buttonMasks);
		bot.delay(400);
		bot.mouseMove(end.x-1, end.y-1);
		bot.delay(150);
		bot.mouseMove(end.x, end.y);
		bot.delay(800);
	    bot.mouseRelease(buttonMasks);
		return new Boolean(true);
	}
	
	/**
	 * Move the mouse cursor to the specified start Point coordinates then perform a 
	 * single mousePress using buttonMasks and drag\move then Release the mouse button at the end point.
	 * During the mouse drag, there are a set of keys will be kept pressed.
	 * @param Point screen coordinates to start of mouse press and drag.
	 * @param Point screen coordinates to end mouse drag and mouse release.
	 * @param int button masks to use during drag
	 * @param int[] the keys kept pressed during mouse drag, the key can be {@link KeyEvent#VK_SHIFT}, {@link KeyEvent#VK_CONTROL} etc.
	 * @return Object Currently we return a Boolean(true) object, but this may 
	 * be subject to change.
	 * @throws java.awt.AWTException if instantiating java.awt.Robot throws it.
	 * @see java.awt.Robot
	 */
	private static Object mouseDrag(java.awt.Point start, java.awt.Point end, int buttonMasks, int[] keys) throws java.awt.AWTException{
		java.awt.Robot bot = getRobot();
		Log.info("Robot mouseDrag from:"+ start +" to:"+ end +" using button mask "+ buttonMasks+ " with keys '"+Arrays.toString(keys)+"' pressed.");
		
		try{
			for(int key:keys) bot.keyPress(key);
			mouseDrag(start, end, buttonMasks);			
		}catch(Exception e){
			if(e instanceof AWTException) throw (AWTException)e;
			AWTException wrapper = new AWTException(e.getMessage());
			wrapper.initCause(e);
			throw wrapper;
		}finally{
			for(int key:keys) bot.keyRelease(key);			
		}

		return new Boolean(true);
	}
	
	/**
	 * Move the mouse cursor to the specified start Point coordinates then perform a 
	 * single mousePress (Button1) and drag\move then Release the mouse button at the end point.
	 * During the drag, the key "ALT" is kept pressed.
	 * @param Point screen coordinates to start of mouse press and drag.
	 * @param Point screen coordinates to end mouse drag and mouse release.
	 * @return Object returned from generic mouseDrag.
	 * @throws java.awt.AWTException if instantiating java.awt.Robot throws it.
	 * @see java.awt.Robot
	 * @see #mouseDrag(Point, Point, int, int[])
	 */
	public static Object altLeftDrag(java.awt.Point start, java.awt.Point end) throws java.awt.AWTException{
		int[] keys = {KeyEvent.VK_ALT};
		return mouseDrag(start, end, InputEvent.BUTTON1_MASK, keys);
	}
	/**
	 * Move the mouse cursor to the specified start Point coordinates then perform a 
	 * single mousePress (Button1) and drag\move then Release the mouse button at the end point.
	 * During the drag, the key "CONTROL" and "ALT" are kept pressed.
	 * @param Point screen coordinates to start of mouse press and drag.
	 * @param Point screen coordinates to end mouse drag and mouse release.
	 * @return Object returned from generic mouseDrag.
	 * @throws java.awt.AWTException if instantiating java.awt.Robot throws it.
	 * @see java.awt.Robot
	 * @see #mouseDrag(Point, Point, int, int[])
	 */	
	public static Object ctrlAltLeftDrag(java.awt.Point start, java.awt.Point end) throws java.awt.AWTException{
		int[] keys = {KeyEvent.VK_CONTROL, KeyEvent.VK_ALT};
		return mouseDrag(start, end, InputEvent.BUTTON1_MASK, keys);
	}
	/**
	 * Move the mouse cursor to the specified start Point coordinates then perform a 
	 * single mousePress (Button1) and drag\move then Release the mouse button at the end point.
	 * During the drag, the key "CONTROL" is kept pressed.
	 * @param Point screen coordinates to start of mouse press and drag.
	 * @param Point screen coordinates to end mouse drag and mouse release.
	 * @return Object returned from generic mouseDrag.
	 * @throws java.awt.AWTException if instantiating java.awt.Robot throws it.
	 * @see java.awt.Robot
	 * @see #mouseDrag(Point, Point, int, int[])
	 */
	public static Object ctrlLeftDrag(java.awt.Point start, java.awt.Point end) throws java.awt.AWTException{
		int[] keys = {KeyEvent.VK_CONTROL};
		return mouseDrag(start, end, InputEvent.BUTTON1_MASK, keys);
	}
	/**
	 * Move the mouse cursor to the specified start Point coordinates then perform a 
	 * single mousePress (Button1) and drag\move then Release the mouse button at the end point.
	 * During the drag, the key "CONTROL" and "SHIFT" are kept pressed.
	 * @param Point screen coordinates to start of mouse press and drag.
	 * @param Point screen coordinates to end mouse drag and mouse release.
	 * @return Object returned from generic mouseDrag.
	 * @throws java.awt.AWTException if instantiating java.awt.Robot throws it.
	 * @see java.awt.Robot
	 * @see #mouseDrag(Point, Point, int, int[])
	 */
	public static Object ctrlShiftLeftDrag(java.awt.Point start, java.awt.Point end) throws java.awt.AWTException{
		int[] keys = {KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT};
		return mouseDrag(start, end, InputEvent.BUTTON1_MASK, keys);
	}
	/**
	 * Move the mouse cursor to the specified start Point coordinates then perform a 
	 * single mousePress (Button1) and drag\move then Release the mouse button at the end point.
	 * During the drag, the key "SHIFT" is kept pressed.
	 * @param Point screen coordinates to start of mouse press and drag.
	 * @param Point screen coordinates to end mouse drag and mouse release.
	 * @return Object returned from generic mouseDrag.
	 * @throws java.awt.AWTException if instantiating java.awt.Robot throws it.
	 * @see java.awt.Robot
	 * @see #mouseDrag(Point, Point, int, int[])
	 */
	public static Object shiftLeftDrag(java.awt.Point start, java.awt.Point end) throws java.awt.AWTException{
		int[] keys = {KeyEvent.VK_SHIFT};
		return mouseDrag(start, end, InputEvent.BUTTON1_MASK, keys);
	}

	/**
	 * Scroll the mouse wheel on the object which has focus.
	 * @param wheelAmt int, the wheel amount to scroll.
	 * @return boolean, true if success
	 */
	public static boolean mouseWheel(int wheelAmt){
		try{
			java.awt.Robot bot = getRobot();
			bot.mouseWheel(wheelAmt);
			return true;
		}catch(Throwable th){
			IndependantLog.error("Met "+StringUtils.debugmsg(th));
			return false;
		}
	}
	
	/**
	 * Press down a Key.
	 * @param keycode int, keycode Key to press (e.g. <code>KeyEvent.VK_A</code>)
	 * @return boolean, true if success
	 */
	public static boolean keyPress(int keycode){
		try{
			java.awt.Robot bot = getRobot();
			bot.keyPress(keycode);
			return true;
		}catch(Throwable th){
			IndependantLog.error("Met "+StringUtils.debugmsg(th));
			return false;
		}
	}
	
	/**
	 * Release a Key.
	 * @param keycode int, keycode Key to release (e.g. <code>KeyEvent.VK_A</code>)
	 * @return boolean, true if success
	 */
	public static boolean keyRelease(int keycode){
		try{
			java.awt.Robot bot = getRobot();
			bot.keyRelease(keycode);
			return true;
		}catch(Throwable th){
			IndependantLog.error("Met "+StringUtils.debugmsg(th));
			return false;
		}
	}
	
	/**
	 * Move the mouse cursor to the specified Point, stay for a period and move out
	 * @param point Point, the screen point to hover the mouse
	 * @param millisStay int, the period the hover the mouse, in milliseconds
	 * @throws SAFSException if some error happens
	 */
	public static void mouseHover(Point point, int millisStay) throws SAFSException{
		try{
			java.awt.Robot bot = getRobot();
			bot.mouseMove(point.x, point.y);
			StringUtilities.sleep(millisStay);
			bot.mouseMove(-SCREENSZIE.width, -SCREENSZIE.height);
		}catch(Throwable th){
			throw new SAFSException("Met "+StringUtils.debugmsg(th));
		}
	}

	/**
	 * Move the mouse cursor to the specified start Point coordinates then perform a 
	 * single mousePress (Button1) and drag\move then Release the mouse button at the end point.
	 * @param Point screen coordinates to start of mouse press and drag.
	 * @param Point screen coordinates to end mouse drag and mouse release.
	 * @return Object returned from generic mouseDrag.
	 * @throws java.awt.AWTException if instantiating java.awt.Robot throws it.
	 * @see java.awt.Robot
	 * @see #mouseDrag(Point,Point,int)
	 */
	public static Object leftDrag(java.awt.Point start, java.awt.Point end) throws java.awt.AWTException{
		return mouseDrag(start, end, InputEvent.BUTTON1_MASK);
	}

	/**
	 * Move the mouse cursor to the specified start Point coordinates then perform a 
	 * single mousePress (Button3) and drag\move then Release the mouse button at the end point.
	 * @param Point screen coordinates to start of mouse press and drag.
	 * @param Point screen coordinates to end mouse drag and mouse release.
	 * @return Object returned from generic mouseDrag.
	 * @throws java.awt.AWTException if instantiating java.awt.Robot throws it.
	 * @see java.awt.Robot
	 */
	public static Object rightDrag(java.awt.Point start, java.awt.Point end) throws java.awt.AWTException{
		return mouseDrag(start, end, InputEvent.BUTTON3_MASK);
	}

	/**
	 * Move the mouse cursor to the specified start Point coordinates then perform a 
	 * single mousePress (Button2) and drag\move then Release the mouse button at the end point.
	 * @param Point screen coordinates to start of mouse press and drag.
	 * @param Point screen coordinates to end mouse drag and mouse release.
	 * @return Object returned from generic mouseDrag.
	 * @throws java.awt.AWTException if instantiating java.awt.Robot throws it.
	 * @see java.awt.Robot
	 */
	public static Object centerDrag(java.awt.Point start, java.awt.Point end) throws java.awt.AWTException{
		return mouseDrag(start, end, InputEvent.BUTTON2_MASK);
	}
	
	/**
	 * Minimize all windows by the short cut 'Windows+M'<br>
	 * This works only for windows system.
	 * @throws AWTException
	 */
	public static void minimizeAllWindows() throws AWTException{
		java.awt.Robot robot = getRobot();
		
		robot.keyPress(KeyEvent.VK_WINDOWS);
		robot.keyPress(KeyEvent.VK_M);
		robot.keyRelease(KeyEvent.VK_M);
		robot.keyRelease(KeyEvent.VK_WINDOWS);
	}
	
	/**
	 * get mouse screen location.
	 * @return Point, the mouse screen location.
	 * @throws SAFSException
	 */
	public static Point getMouseLocation() throws SAFSException{
		try{
			return MouseInfo.getPointerInfo().getLocation();
		}catch(Throwable th){
			throw new SAFSException("Fail to get mouse screen location, due to "+StringUtils.debugmsg(th));
		}
	}
	
	/**
	 * <em>Pre-condition:</em> The window should be focused
	 * Get the window system menu by short-cut 'Alt+Space'<br>
	 * This works only for windows system.
	 * @throws AWTException
	 */
	public static void getWindowSystemMenu() throws AWTException{
		java.awt.Robot robot = getRobot();
		
		robot.keyPress(KeyEvent.VK_ALT);
		robot.keyPress(KeyEvent.VK_SPACE);
		robot.keyRelease(KeyEvent.VK_SPACE);
		robot.keyRelease(KeyEvent.VK_ALT);
		
		//After the popup-menu is shown, delay for a while so that the following "key-press" will
		//have time to trrigger the menu item.
		StringUtilities.sleep(500);
	}
	/**
	 * <em>Pre-condition:</em> The window should be focused
	 * Minimize window by mnemonic key 'n' of window-system-menu.<br>
	 * This works only for windows system.
	 * @throws AWTException
	 */
	public static void minimizeFocusedWindow() throws AWTException{
		getWindowSystemMenu();
		robot.keyPress(KeyEvent.VK_N);
	}
	/**
	 * <em>Pre-condition:</em> The window should be focused
	 * Minimize window by mnemonic key 'x' of window-system-menu.<br>
	 * This works only for windows system.
	 * @throws AWTException
	 */
	public static void maximizeFocusedWindow() throws AWTException{
		getWindowSystemMenu();
		robot.keyPress(KeyEvent.VK_X);
	}
	/**
	 * <em>Pre-condition:</em> The window should be focused
	 * Minimize window by mnemonic key 'R' of window-system-menu.<br>
	 * This works only for windows system.
	 * @throws AWTException
	 */
	public static void restoreFocusedWindow() throws AWTException{
		getWindowSystemMenu();
		robot.keyPress(KeyEvent.VK_R);
	}
	/**
	 * <em>Pre-condition:</em> The window should be focused
	 * Minimize window by mnemonic key 'C' of window-system-menu.<br>
	 * This works only for windows system.
	 * @throws AWTException
	 */
	public static void closeFocusedWindow() throws AWTException{
		getWindowSystemMenu();
		robot.keyPress(KeyEvent.VK_C);
	}
}
