/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * History:<br>
 *
 *   <br>   NOV 19, 2013    (Carl Nagle) Original Release.
 *   <br>   DEC 18, 2013    (LeiWang) Update to support ComboBox.
 *   <br>   MAR 05, 2014    (Lei Wang) Move some keywrod constants to ComponentFunction, implement click-related keywords.
 *   <br>   APR 29, 2014    (Lei Wang) Support keyword ExecuteScript.
 *   <br>   SEP 15, 2014    (Lei Wang) Modify showComponentAsMuchPossible(): before scrolling, check if the component is fully shown.
 *   <br>   MAY 18, 2015    (Lei Wang) Add refresh(): Try to refresh a stale WebElement.
 *   <br>   JUL 24, 2015    (Lei Wang) Add refresh(boolean): call it in localProcess to refresh WebElement if it is stale.
 *   <br>   SEP 07, 2015    (Lei Wang) Add method dragTo().
 *   <br>   OCT 30, 2015    (Lei Wang) Modify exist(): highlight component for keyword GUIDESOEXIST etc.
 *   <br>   NOV 26, 2015    (Lei Wang) Remove checkForCoord() and _lookupAppMapCoordReference(), their functionality will be provided by super class.
 *   <br>   NOV 26, 2015    (Lei Wang) Modify method getComponentRectangle(): include the frame's location for a webelement.
 *   <br>   FEB 25, 2016    (Lei Wang) Modify localProcess(): Set 'SearchContext' and 'RecognitionString' to libComponent for refreshing.
 *   <br>   APR 19, 2016    (Lei Wang) Modify componentClick(): Handle the optional parameter 'autoscroll'.
 */
package org.safs.selenium.webdriver;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.safs.ComponentFunction;
import org.safs.IndependantLog;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSParamException;
import org.safs.StatusCodes;
import org.safs.StringUtils;
import org.safs.autoit.AutoItRs;
import org.safs.image.ImageUtils;
import org.safs.model.commands.GenericMasterFunctions;
import org.safs.model.commands.GenericObjectFunctions;
import org.safs.model.commands.WindowFunctions;
import org.safs.robot.Robot;
import org.safs.selenium.webdriver.lib.Component;
import org.safs.selenium.webdriver.lib.Json;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.selenium.webdriver.lib.model.Element;
import org.safs.selenium.webdriver.lib.model.HierarchicalElement;
import org.safs.selenium.webdriver.lib.model.IHierarchicalSelectable;
import org.safs.selenium.webdriver.lib.model.ISelectable;
import org.safs.text.FAILKEYS;
import org.safs.text.FileUtilities;
import org.safs.text.GENKEYS;
import org.safs.text.GENStrings;
import org.safs.tools.CaseInsensitiveFile;
import org.safs.tools.engines.TIDComponent;
import org.safs.tools.stringutils.StringUtilities;

/**
 * Component Functions for Selenium WebDriver.
 *
 **/
public class CFComponent extends ComponentFunction{

	public static final String SAFS_JAVASCRIPT_RESULT  	= "__$_SAFS_JAVASCRIPT_RESULT_$__";
	public static final String INDENT_MARK 				= "\t";

	protected WebDriverGUIUtilities wdUtils;
	protected WDTestRecordHelper wdHelper;
	protected WebElement winObject;
	protected WebElement compObject;

	protected Component libComponent = null;
	
	/**A cache, which stores a pair(WebElement, Component)*/
	protected static Map<WebElement, Component> libComponentCache = new HashMap<WebElement, Component>();
	
	/** 5, the default maximum time to retry enter text if verification fails.*/
    public static int DEFAULT_MAX_RETRY_ENTER = 5;
    
    /** The maximum time to retry 'enter the text' into Component box if the verification fails. */
	private int maxRetry = DEFAULT_MAX_RETRY_ENTER;

	public CFComponent(){
		super();
	}

	/**
	 * @param maxRetry int, the max times to retry 'enter the text' into Component box if the verification fails.
	 * 
	 */
	public void setMaxRetry(int maxRetry) {
		this.maxRetry = maxRetry;
	}

	public void process() {
		String debugmsg = this.getClass().getName()+".process(): ";
		// assume this for now..
		updateFromTestRecordData();
		wdHelper = (WDTestRecordHelper)testRecordData;

		testRecordData.setStatusCode(StatusCodes.SCRIPT_NOT_EXECUTED);
		try {
			getHelpers();
			wdUtils = (WebDriverGUIUtilities)utils;
		} catch ( SAFSException ex ) {
			testRecordData.setStatusCode( StatusCodes.GENERAL_SCRIPT_FAILURE );
			log.logMessage( testRecordData.getFac( ), "SAFSException: "
					+ ex.getClass( ).getName( ) + ", msg: " + ex.getMessage( ), FAILED_MESSAGE );
			return;
		}

		winObject = wdHelper.getWindowTestObject();
		compObject = wdHelper.getCompTestObject();
		
		if(TIDComponent.noWaitGUIExistence(action)){
			//If check the GUI's existence, don't waste time to wait here. waitForObject() will be called later.
			//If action is GUILess, NO window/component to wait.
			Log.debug(debugmsg +" action '"+action+"' is GUILess or CheckGUIExistence, don't need to wait GUI.");
			if(action.equalsIgnoreCase(GenericMasterFunctions.TYPEKEYS_KEYWORD)|| 
			   action.equalsIgnoreCase(GenericMasterFunctions.TYPECHARS_KEYWORD)) {
				String keystrokes = null;
				try {
					iterator = params.iterator();
					if(!iterator.hasNext()){
						issueParameterValueFailure("TextValue");
					}else{
						keystrokes = iterator.next();
						Log.debug(debugmsg+" processing command '"+action+"' with TextValue "+keystrokes);
						if(action.equalsIgnoreCase(GenericMasterFunctions.TYPEKEYS_KEYWORD)){
							WDLibrary.inputKeys(null, keystrokes);									
						}else{
							WDLibrary.inputChars(null, keystrokes);
						}
						issuePassedSuccessUsing(keystrokes);
					}
				} catch (Exception e) {
					Log.error(debugmsg+"SeleniumPlus Error processing '"+action+"'.", e);
					issueErrorPerformingActionUsing(keystrokes, e.getClass().getSimpleName()+", "+ e.getMessage());
				}
				return;
			}
		}else{
			if(winObject==null){
				String winRec = null;
				try{ winRec = testRecordData.getWindowGuiId();}catch(Exception ignore){}
				if( (winRec != null) && ImageUtils.isImageBasedRecognition(winRec)){
						testRecordData.setStatusCode( StatusCodes.SCRIPT_NOT_EXECUTED );
						Log.info(debugmsg +" returning SCRIPT_NOT_EXECUTED assuming Image-Based Testing...");
					return;					
				} else if( (winRec != null) && AutoItRs.isAutoitBasedRecognition(winRec)){  
					testRecordData.setStatusCode( StatusCodes.SCRIPT_NOT_EXECUTED );  
					Log.info(debugmsg +" returning SCRIPT_NOT_EXECUTED assuming AutoIt Testing...");  
					return;  
				}  
			}

			if(compObject==null){
				Log.warn(debugmsg +" Component Object is null, try to get it through WebDriverGUIUtility.");
				if(!windowName.equals(compName)){
					try{
						if ( wdUtils.waitForObject( mapname, windowName, compName, secsWaitForWindow) == 0 ) {
							Log.debug(debugmsg+" we get the component object through WebDriverGUIUtility.");
							winObject = ( ( WDTestRecordHelper )testRecordData ).getWindowTestObject( );
							compObject = ( ( WDTestRecordHelper )testRecordData ).getCompTestObject( );
						}else{
							Log.debug(debugmsg+" can not get component object through WebDriverGUIUtility: waitForObject() error.");						
						}
					}catch(SAFSException e){
						Log.debug(debugmsg+" can not get component object through WebDriverGUIUtility: Exception="+e.getMessage());
					}

					if(compObject==null){
						Log.warn(debugmsg +"  Component Object is still null.");				
					}
				}
				else{ // windowName equals windowName
					try{
						if ( wdUtils.waitForObject( mapname, windowName, windowName, secsWaitForWindow) == 0 ) {
							Log.debug(debugmsg+" we got the window object through WebDriverGUIUtility.");
							winObject = ( ( WDTestRecordHelper )testRecordData ).getWindowTestObject();
							compObject = ( ( WDTestRecordHelper )testRecordData ).getCompTestObject( );
						}else{
							Log.debug(debugmsg+" can not get window object through WebDriverGUIUtility: waitForObject() error.");
						}
					}catch(SAFSException e){
						Log.debug(debugmsg+" can not get window object through WebDriverGUIUtility: Exception="+e.getMessage());
					}
				}
			}

			//If 'Hightlight' has been turned on, then highlight the componet before action
			//As we are going to call javascript function to change the style of WebElement, not suer if this
			//will cause the WebElement becomes stale, if so, we need to get the WebElement again.
			if(WebDriverGUIUtilities.HIGHLIGHT){
				if(compObject!=null) WDLibrary.highlight(compObject);
				else if(winObject!=null) WDLibrary.highlight(winObject);
			}

			try {
				// do the work
				localProcess();

				if (testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED && action != null){
					if(action.equalsIgnoreCase(WindowFunctions.SETFOCUS_KEYWORD)) {
						try{								
							Log.debug(debugmsg+" processing command '"+action+"' with parameters "+params);
							iterator = params.iterator();
						    _setFocus();	
						}catch(SAFSException e){
							Log.error(debugmsg+"SeleniumPlus Error processing '"+action+"'.", e);
							testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
							String msg = getStandardErrorMessage(windowName +":"+ compName +" "+ action);
							String detail = "Met Exception "+e.getMessage();
							log.logMessage(testRecordData.getFac(),msg, detail, FAILED_MESSAGE);
						}
					}
				}										
			} catch (SeleniumPlusException ignore) {
				IndependantLog.debug(debugmsg+" ignore Exception "+StringUtils.debugmsg(ignore));
			}
		}
		
		if (testRecordData.getStatusCode() == StatusCodes.SCRIPT_NOT_EXECUTED) {
			componentProcess();//handle Generic keywords
		} else {
			Log.debug(debugmsg+"'"+action+"' has been processed\n with testrecorddata"+testRecordData+"\n with params "+params);
		}

		//If 'Highlight' has been turned on, then clean highlight the component after action
		if(WebDriverGUIUtilities.HIGHLIGHT){
			StringUtilities.sleep(1000);
			WDLibrary.clearHighlight();
		}
	}
	
	/**
	 * Handle the cache for component-library.<br>
	 * In this class CFComponent, component-library is not required for some keywords like Maximize, Minimize etc.<br>
	 * So in the method {@link #process()} of this class, when calling {@link #localProcess()}, <br>
	 * if any Exception is caught, just ignore it.<br>
	 * But for subclass, component-library is required, so if this method is overrided, user needs to<br>
	 * call super.localProcess() in a try-catch clause like following:<br>
	 * <pre>
	 * <code>
	 * try{
	 *   super.localProcess();
	 *   subLibrary = (SubComponentLibrary) libComponent;
	 *   
	 * }catch(SeleniumPlusException se){
	 *   testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
	 * }
	 * </code>
	 * </pre>
	 * @throws SeleniumPlusException
	 */
	protected void localProcess() throws SeleniumPlusException{
		
		String debugmsg = StringUtils.debugmsg(getClass(), "localProcess");
		
		try{
			synchronized(libComponentCache){
				//compObject may be null, which will cause Exception, and libComponent will be null
				if(!libComponentCache.containsKey(compObject)){
					IndependantLog.debug("CFComponent.localProcess contains NO CACHED library for this WebElement.");
					libComponent = newLibComponent(compObject);
					//Set the winObject as search context and compRS as 'recognition string' for refreshing
					libComponent.setSearchContext(winObject);
					String compRS = testRecordData.getCompGuiId();
					if(compRS!=null) libComponent.setPossibleRecognitionStrings(new String[]{compRS});
					else IndependantLog.warn(debugmsg+" Recognition String for "+compName+" is null. It cannot be used to refresh webelement!");
					
					WDLibrary.checkNotNull(libComponent);
					libComponentCache.put(compObject, libComponent);
				}else{
					IndependantLog.debug("CFComponent.localProcess retrieving CACHED library for this WebElement.");
					libComponent = libComponentCache.get(compObject);
				}
				if(libComponent==null) IndependantLog.warn("component library is null!");
			}
			if(libComponent==null) IndependantLog.warn("component library is null!");
			//refresh the compObject if stable, 
			//TODO do we need to refresh winObject too???
			refresh(false);
			
		}catch(SeleniumPlusException se){
			se.setInfo("Fail to get the component's library! "+se.getInfo());
			throw se;
		}catch(Exception e){
			throw new SeleniumPlusException("Fail to get the component's library! Met Exception "+StringUtils.debugmsg(e));
		}
			
	}

	/** sub class MUST to override this method and provide its own library Component.*/
	protected Component newLibComponent(WebElement webelement) throws SeleniumPlusException{
		return new Component(webelement);
	}
	
	/**clear the cache 'libComponentCache'*/
	public static void clearInternalCache(){
		synchronized(libComponentCache){
			Iterator<Component> components = libComponentCache.values().iterator();
			while(components.hasNext()){
				components.next().clearCache();
			}
			libComponentCache.clear();
		}
	}
	
	public void _setFocus() throws SAFSException{				
		boolean rc = libComponent.setFocus(); 
		if (!rc) throw new SAFSException("setFocus failed on window");
		issuePassedSuccess(null);
	}
	
	
	/**clear the cache 'libComponentCache'*/
	public void clearCache(){
		clearInternalCache();
		issuePassedSuccess(null);
	}

	/**
	 * handles InputKeys and InputCharacters.
	 */
	protected void inputKeystrokes() throws SAFSException {
		if(params.size() < 1){
			issueParameterCountFailure();
			return;
		}
		String text = null;
		try{ text = iterator.next();}
		catch(NoSuchElementException x){
			issueParameterValueFailure("TextValue");
			return;
		}
		try{

			if(action.equalsIgnoreCase(GenericMasterFunctions.INPUTKEYS_KEYWORD)){
				libComponent.inputKeys(text);
			}else{
				libComponent.inputChars(text);
			}
			issuePassedSuccessUsing(text);
			
		}catch(Exception x){
			String errormsg = StringUtils.debugmsg(x);
			IndependantLog.debug(action+" failed. Met Exception "+errormsg);
			issueErrorPerformingActionOnX(compName, errormsg);
			return;
		}
	}

	/**
	 *Subclass should give its own implementation<br>
	 */
	protected void _setPosition(Point position) throws SAFSException {
		WDLibrary.setPositionBrowserWindow(position.x, position.y);
	}
	/**
	 *Subclass should give its own implementation<br>
	 */
	protected void _setSize(Dimension size) throws SAFSException {
		WDLibrary.resizeBrowserWindow(size.width, size.height);
	}

	/**
	 * Maximize the window.
	 * @throws SeleniumPlusException 
	 */
	protected void _maximize() throws SeleniumPlusException {
		WDLibrary.maximizeBrowserWindow();	
	}

	/**
	 * Close the window.
	 * @throws SeleniumPlusException 
	 */
	protected void _close() throws SeleniumPlusException {
		WDLibrary.closeBrowser();	
	}

	/**
	 * Minimize the window.
	 * @throws SeleniumPlusException 
	 */
	protected void _minimize() throws SeleniumPlusException{
		WDLibrary.minimizeBrowserWindow();	
	}
	
	/** <br><em>Purpose:</em> componentClick
	 **/
	protected void componentClick() throws SAFSException{	

		if(compObject==null) throw new SAFSException("Component WebElement is null.");

		try{
			java.awt.Point point = checkForCoord(iterator);
			String autoscroll = null;
			if(iterator.hasNext()) autoscroll = iterator.next();
			
			long begin = System.currentTimeMillis();
			if (action.equalsIgnoreCase(CLICK) ||
					action.equalsIgnoreCase(COMPONENTCLICK)) {
				if (point==null) {
					WDLibrary.click(compObject, autoscroll);
				}else{
					WDLibrary.click(compObject, point, autoscroll);
				}
			} else if (action.equalsIgnoreCase(DOUBLECLICK)) {
				WDLibrary.doubleClick(compObject, point, autoscroll);
			} else if (action.equalsIgnoreCase(RIGHTCLICK)) {
				WDLibrary.rightclick(compObject, point, autoscroll);
			} else if (action.equalsIgnoreCase(CTRLCLICK)) {
				WDLibrary.click(compObject, point, Keys.CONTROL, WDLibrary.MOUSE_BUTTON_LEFT, autoscroll);
			} else if (action.equalsIgnoreCase(CTRLRIGHTCLICK)) {
				WDLibrary.click(compObject, point, Keys.CONTROL, WDLibrary.MOUSE_BUTTON_RIGHT, autoscroll);
			} else if (action.equalsIgnoreCase(SHIFTCLICK)) {
				WDLibrary.click(compObject, point, Keys.SHIFT, WDLibrary.MOUSE_BUTTON_LEFT, autoscroll);
			}
			long timeConsumed = System.currentTimeMillis()-begin;
			IndependantLog.debug("it took "+timeConsumed+" milliseconds or "+(timeConsumed/1000)+" seconds to perform "+action);
			
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			String msg = null;
			if ( point != null ) {
				Log.info("clicking point: "+point);
				String use = "X="+  String.valueOf(point.x) + " Y="+ String.valueOf(point.y);
				altText = windowName+":"+compName+" "+action+" successful using "+ use;
				msg = genericText.convert("success3a", altText, windowName, compName, action, use);
			}else{
				altText = windowName +":"+ compName + " "+ action +" successful.";
				msg = genericText.convert("success3", altText, windowName, compName, action);
			}
			log.logMessage(testRecordData.getFac(),msg, PASSED_MESSAGE);

		}catch(Exception e){
			String errormsg = StringUtils.debugmsg(e);
			IndependantLog.debug(action+" failed. Met Exception "+errormsg);
			issueErrorPerformingActionOnX(compName, errormsg);
		}		

	}

	/** <br><em>Purpose:</em> executeScript
	 **/
	protected void executeScript() throws SAFSException{
		String debugmsg = StringUtils.debugmsg(getClass(), "executeScript");
		if(compObject==null){
			throw new SAFSException("Component WebElement is null.");
		}

		if(params.size() < 1){
			issueParameterCountFailure();
			return;
		}

		Iterator<?> iterator = params.iterator();
		String script = (String) iterator.next();
		Log.info(debugmsg+" executing script :\n "+ script);

		List<Object> args = new ArrayList<Object>();
		while(iterator.hasNext()){
			args.add(iterator.next());
		}

		try{
			Object result = WDLibrary.executeJavaScriptOnWebElement(script, compObject, args.toArray(new Object[0]));
			//TODO need a way to bring back the Object result returned from javascript.
			if(result!=null){
				String resultStr = result.toString();
				IndependantLog.debug(debugmsg+"return result: "+resultStr);
				testRecordData.setStatusInfo(resultStr);//for jsafs user
				setVariable(SAFS_JAVASCRIPT_RESULT, resultStr);
			}
			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			String msg = genericText.convert("success3", windowName +":"+ compName + " "+ action +" successful.",
					windowName, compName, action);
			log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
			return;
		}catch(Exception e){
			String errormsg = StringUtils.debugmsg(e);
			IndependantLog.debug(action+" failed. Met Exception "+errormsg);
			issueErrorPerformingActionOnX(compName, errormsg);
		}		
	}

	/** <br><em>Purpose:</em> verifyComputedStyle()
	 **/
	protected void action_ComputedStyle(boolean verification) throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false)+(verification? "Verify: ":"Get: ");
		if(compObject==null){
			throw new SAFSException("Component WebElement is null.");
		}

		if(params.size() < 1){
			issueParameterCountFailure();
			return;
		}

		String filename = (String) iterator.next();
		IndependantLog.info(debugmsg+" received filename: "+ filename);

		try{
			//Save the computed style to test file
			JSONObject actualJsonObj = null;
			Map<String, String> map = WDLibrary.getCssValues(compObject);
			actualJsonObj = new JSONObject(map);
			File testfile = deduceTestFile(filename);
			IndependantLog.debug(debugmsg+" component's computed style has been saved to '"+testfile.getAbsolutePath()+"'.");
			FileUtilities.writeStringToUTF8File(testfile.getCanonicalPath(), actualJsonObj.toString());
			
			//Do the verification
			if(verification){
				File benchfile = null;
				JSONObject benchJsonObj = null;
				try {
					if (filename.contains(":") || filename.startsWith("/") || filename.startsWith("\\")  ) {
						benchfile = new CaseInsensitiveFile(filename).toFile(); // user specified
					} else {
						benchfile = deduceBenchFile(filename);				
					}
					
				} catch (Exception ef){
					IndependantLog.info(debugmsg+" file exception"+ ef);
				}
				InputStream benchStream = new FileInputStream(benchfile);
				String jsonStr = IOUtils.toString(benchStream);
				benchJsonObj = new JSONObject(jsonStr);
				
				if(!Json.jsonsEqual(actualJsonObj, benchJsonObj)){
					testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
					String p1 = benchfile.getAbsolutePath();
					String p2 = testfile.getAbsolutePath();
					String alttext = "the content of '"+p1+"' does not match the content of '"+p2+"'";
					String message = GENStrings.convert(GENKEYS.CONTENT_NOT_MATCHES_KEY, alttext, p1, p2);
					componentExecutedFailureMessage(message);
					return;
				}
			}

			testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
			String msg = genericText.convert("success3", windowName +":"+ compName + " "+ action +" successful.",
					windowName, compName, action);
			log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
			
		}catch(Exception e){
			String errormsg = StringUtils.debugmsg(e);
			IndependantLog.debug(action+" failed. Met Exception "+errormsg);
			issueErrorPerformingActionOnX(compName, errormsg);
		}		

	}
	
	protected Object getPropertyObject(String propertyName) throws SAFSException{
		return WDLibrary.getProperty(compObject, propertyName);
	}

	protected Map<String, Object> getProperties() throws SAFSException{
		return WDLibrary.getProperties(compObject);
	}

	/**
	 * Sub class may override this method to get its own data.<br>
	 */
	protected Collection<String> captureObjectData() throws SAFSException {
		String debugmsg = StringUtils.debugmsg(false);
		Collection<String> contents = null;
		
		try{
			if(libComponent instanceof IHierarchicalSelectable){
				contents = new ArrayList<String>();
				captureHierarchicalData(contents, ((IHierarchicalSelectable)libComponent).getContent(), INDENT_MARK, 0);
			}
			else if(libComponent instanceof ISelectable){
				ISelectable selectable = (ISelectable) libComponent;
				contents = convertElementArrayToList(selectable.getContent());
			}
		}catch(SAFSException se){
			IndependantLog.warn(debugmsg+"Fail to capture data by interface ISelectable. due to "+StringUtils.debugmsg(se));
		}
		
		if(contents==null || contents.isEmpty()){
			String value = null;
			if(contents==null) contents = new ArrayList<String>();

			try{ value = getProperty(PROPERTY_DOT_itemText); } catch(SAFSException ignore){}
			try{ if(value==null) value = getProperty(PROPERTY_textContent); } catch(SAFSException ignore){}
			try{ if(value==null) value = getProperty(PROPERTY_innerHTML); } catch(SAFSException ignore){}
			
			if(value!=null) contents.add(value);
		}
		
		if(contents==null || contents.isEmpty()){
			IndependantLog.warn(debugmsg+" Cannot get any content!");
		}
		
		return contents;
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
	 * Caputre the content of hierarchical structure like tree or menu.
	 * @param contents Collection<String>, out, provide a empty Collection and will be filled with content. MUST NOT be null.
	 * @param nodes HierarchicalElement[], an array of hierarchical structure's nodes
	 * @param indentMark String, The character(s) to indent the child nodes from the parent branch. 
	 * @param level int, the level of the node in the whole hierarchical structure. 0 at root level.
	 * @throws SeleniumPlusException
	 */
	public static void captureHierarchicalData(Collection<String> contents, HierarchicalElement[] nodes, String indentMark, int level) throws SeleniumPlusException{
		StringBuffer sb = new StringBuffer();
		
		if(contents==null){
			IndependantLog.error("The content collection container is null!");
			return;
		}
		if(nodes==null){
			IndependantLog.error("The nodes array is null!");
			return;
		}
		for(HierarchicalElement node: nodes){
			try{
				sb.setLength(0);
				if(indentMark!=null && !indentMark.isEmpty()) for(int i=0;i<level;i++) sb.append(indentMark);
				sb.append(node.contentValue());
				contents.add(sb.toString());
			}catch(Exception e) {
//				IndependantLog.warn("Fail to get content of node due to "+StringUtils.debugmsg(e));
			}
			captureHierarchicalData(contents, node.getChildren(), indentMark, level+1);
		}
	}

	/**
	 * @param webelement WebElement, for who to get the screen rectangle
	 * @return Rectangle, the absolute rectangle on screen for the webelement object.
	 */
	private Rectangle getRectangleOnScreen(WebElement webelement){
		String debugmsg = StringUtils.debugmsg(false);
		Rectangle rectangle = null;
		try{
			rectangle = WDLibrary.getRectangleOnScreen(webelement);
		}catch(Exception e){
			IndependantLog.warn(debugmsg+" Met "+StringUtils.debugmsg(e));
		}

	    return rectangle;
	}
	
	/** @return Rectangle, the absolute rectangle on screen for the window */
	protected Rectangle getWindowRectangleOnScreen(){
		Rectangle rectangle = getRectangleOnScreen(winObject);
	    if(rectangle==null) IndependantLog.warn("Fail to get bounds for "+ windowName+":"+windowName +" on screen.");
	    return rectangle;
	}
	/** @return Rectangle, the absolute rectangle on screen for the component */
	protected Rectangle getComponentRectangleOnScreen(){
		WebElement component = (compObject!=null? compObject:winObject);
		Rectangle rectangle = getRectangleOnScreen(component);
	    if(rectangle==null) IndependantLog.warn("Fail to get bounds for "+ windowName+":"+compName +" on screen.");
	    return rectangle;
	}
	
	/**
	 * Show the component in the browser's viewport as much as possible.
	 */
	protected boolean showComponentAsMuchPossible(boolean verify) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(true);
		
		WebElement component = (compObject!=null? compObject:winObject);
		IndependantLog.debug(debugmsg+" verify="+verify);

		//show the component in the browser's viewport
		if(WDLibrary.showOnPage(component, String.valueOf(verify))){
			return true;
		}else{
			IndependantLog.warn(debugmsg+"Fail to show "+windowName+":"+compName +" on screen.");
			return false;
		}
	}

	/**
	 * Refresh the winObject/compObject if stable. Do nothing if not stable.<br>
	 * @param includingWinObject boolean, if true this method will also refresh window object.
	 *                                    if false, only refresh component object.
	 */	
	protected void refresh(boolean includingWinObject){
		refresh(includingWinObject, true);
	}
	/**
	 * Refresh the winObject/compObject if stable or refresh is forced by setting parameter 'checkStale' to false.<br>
	 * @param includingWinObject boolean, if true this method will also refresh window object.
	 *                                    if false, only refresh component object.
	 * @param checkStale boolean, whether to check if the webelement is stable before refresh.
	 *                            true, check stable; false, force refresh directly without check.
	 */
	protected void refresh(boolean includingWinObject, boolean checkStable){
		String debugmsg = StringUtils.debugmsg(false);
		if(winObject==null || compObject==null){
			IndependantLog.error(debugmsg+"the winObject or compObject is null!");
			return;
		}
		//We need to refresh winObject if it is not the same as compObject
		if(winObject!=compObject && includingWinObject){
			winObject = refresh(winObject, checkStable);
		}
		//Refresh the compObject
		if(libComponent!=null){
			libComponent.refresh(checkStable);
		}else{
			IndependantLog.warn(debugmsg + "The libComponent component is null!");
			compObject = refresh(compObject, checkStable);
		}
	}
	
	/**
	 * Try to get a fresh WebElement.<br>
	 * When the page change, the WebElement may be stale, or the information (like its location, size etc.) may be stale,
	 * so we need to get the fresh WebElement on the page to get latest information.<br>
	 * <b>Note:</b> This method may consume some time for getting refreshed WebElement.<br>
	 * @param webelement WebElement, to be refreshed.
	 * @return WebElement, the new refreshed WebElement; or the original WebElement if there is something wrong.
	 * @see #refresh(WebElement, boolean)
	 */
	public static WebElement refresh(WebElement webelement){
		return refresh(webelement, false);
	}
	/**
	 * Try to get a fresh WebElement if it is stale.<br>
	 * When the page change, the WebElement may be stale, or the information (like its location, size etc.) may be stale,
	 * so we need to get the fresh WebElement on the page to get latest information.<br>
	 * <b>Note:</b> This method may consume some time for getting refreshed WebElement.<br>
	 * @param webelement WebElement, to be refreshed.
	 * @param checkStale boolean, whether to check if the webelement is stable before refresh.
	 *                            true, check stable; false, force refresh directly without check.
	 * @return WebElement, the new refreshed WebElement;<br>
	 *                     or the original WebElement if it is not stale or if there is something wrong.<br>
	 */	
	public static WebElement refresh(WebElement webelement, boolean checkStale){
		synchronized(libComponentCache){
			Component libComponent = null;
			if(libComponentCache.containsKey(webelement)){
				libComponent = libComponentCache.get(webelement);
			}else{
				try {libComponent = new Component(webelement); } catch (SeleniumPlusException e) {
					IndependantLog.debug(StringUtils.debugmsg(false)+StringUtils.debugmsg(e));
				}
				//Do we need to put the libComponent to cache libComponentCache? maybe not.
			}

			if(libComponent!=null && libComponent.refresh(checkStale)){
				return libComponent.getWebElement();
			}

			return webelement;
		}
	}
	
	/** @return Rectangle, the rectangle relative to the browser. 
	 * 
	 * If this return 'absolute rectangle on screen', we SHOULD remove override method {@link #getRectangleImage(Rectangle)}.<br>
	 * But we need to be careful, Robot will be used to get image, we have to implement RMI to get image if the <br>
	 * browser is running on a remote machine.<br>
	 */
	protected Rectangle getComponentRectangle(){
		String debugmsg = StringUtils.debugmsg(false);
		WebElement component = (compObject!=null? compObject:winObject);
		try {
			//Get component's location relative to the browser
			Point p = WDLibrary.getLocation(component, false);
			org.openqa.selenium.Dimension dim = component.getSize();
			return  new Rectangle(p.x, p.y, dim.getWidth(), dim.getHeight());
		} catch (Exception e) {
			IndependantLog.warn(debugmsg+"Fail to get bounds for "+windowName+":"+compName +" on screen due to "+StringUtils.debugmsg(e));
		}
		return null;
	}
	
	protected BufferedImage getRectangleImage(Rectangle imageRect) throws SAFSException{
		return WDLibrary.captureBrowserArea(imageRect);
	}
	
	protected boolean exist()throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false);

		// wait for the window - secTimout of 0 means a single try to search for given comps
		// waitForObject checks only component validity and tells nothing about its visibility
		WebElement compObject = wdUtils.getTestObject(mapname, windowName, compName, true);
		if(compObject==null){
			IndependantLog.warn(debugmsg+ windowName+":"+compName+" does not exist.");
			return false;
		}else{
			if(WebDriverGUIUtilities.HIGHLIGHT) WDLibrary.highlight(compObject);			
		}
		
		return WDLibrary.isVisible(compObject);
	}
	
	protected boolean performHoverMouse(Point point, int milliseconds) throws SAFSException{  	
		String debugmsg = StringUtils.debugmsg(false);

		try{
			WebElement we = (compObject!=null? compObject:winObject);
			WDLibrary.mouseHover(we, point, milliseconds);
			return true;
		}catch(Exception e){
			IndependantLog.error(debugmsg+" Met "+StringUtils.debugmsg(e));
		}
		return false;
	}

	/**
	 * perform LeftDrag or RightDrag on component moving from (x1,y1) to (x2,y2).
	 * Format: "T,SwingApp,component,LeftDrag,"Coords=x1;y1;x2;y2" 
	 * @exception SAFSException
	 */
	protected void performDrag() throws SAFSException{
		String debugInf = StringUtils.debugmsg(false);
		if (params.size()<1) {
			paramsFailedMsg(windowName, compName);  
			return;
		}

		try{
			//format of preset: Coords=0,0,640,480
			String preset = (String)params.iterator().next();
			preset = getPossibleMapItem(preset);
			Polygon polygon = StringUtils.convertLine(preset);
			Point p1 = new java.awt.Point(polygon.xpoints[0], polygon.ypoints[0]);
			Point p2 = new java.awt.Point(polygon.xpoints[1], polygon.ypoints[1]);			
			String msg = " :"+action+" from "+p1.toString()+" to "+p2.toString();

			if(action.equalsIgnoreCase(LEFTDRAG)){
				WDLibrary.leftDrag(compObject, p1, p2);
				
			}else if(action.equalsIgnoreCase(SHIFTLEFTDRAG)){ 
				WDLibrary.shiftLeftDrag(compObject, p1, p2);
				
			}else if(action.equalsIgnoreCase(CTRLSHIFTLEFTDRAG)){ 
				WDLibrary.ctrlShiftLeftDrag(compObject, p1, p2);
				
			}else if(action.equalsIgnoreCase(CTRLLEFTDRAG)){ 
				WDLibrary.ctrlLeftDrag(compObject, p1, p2);
				
			}else if(action.equalsIgnoreCase(ALTLEFTDRAG)){ 
				WDLibrary.altLeftDrag(compObject, p1, p2);
				  
			}else if(action.equalsIgnoreCase(CTRLALTLEFTDRAG)){ 
				WDLibrary.ctrlAltLeftDrag(compObject, p1, p2);
				  
			}else if(action.equalsIgnoreCase(RIGHTDRAG)){
				WDLibrary.rightDrag(compObject, p1, p2);
				
			}

			testRecordData.setStatusCode(StatusCodes.OK);
			log.logMessage(testRecordData.getFac(),
					genericText.convert(TXT_SUCCESS_4, altText+msg, windowName, compName, action, msg),
					PASSED_MESSAGE);	    

		} catch (Exception e) {
			Log.debug(debugInf+e.getMessage());
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			componentFailureMessage(e.getMessage());
		}       
	}
	/**
	 * perform dragTo from component1 to component2 with offset.
	 * @exception SAFSException
	 */
	protected void dragTo() throws SAFSException{
		String debugmsg = StringUtils.debugmsg(false);
		if (params.size()<2) {
			paramsFailedMsg(windowName, compName);  
			return;
		}
		
		try{
			String toWindow = (String)iterator.next();
			String toComponent = (String)iterator.next();
			
			IndependantLog.debug(debugmsg+"Source component is '"+windowName+":"+compName+"'");
			IndependantLog.debug(debugmsg+"Before getting target component '"+toWindow+":"+toComponent+"',"
					+ "\n TestRecordData: winrec="+testRecordData.getWindowGuiId()+"; comprec="+testRecordData.getCompGuiId());
			//BE CAREFUL!!! This calling (wdUtils.getTestObject) may change the testRecordData object, which will contain information of the target component!!! 
			WebElement toElement = wdUtils.getTestObject(mapname, toWindow, toComponent, true);
			IndependantLog.debug(debugmsg+"After getting target component '"+toWindow+":"+toComponent+"',"
					+ "\n TestRecordData: winrec="+testRecordData.getWindowGuiId()+"; comprec="+testRecordData.getCompGuiId());
			
			//offset
			String offset = "50%, 50%, 50%, 50%";
			if(iterator.hasNext()){
				offset = (String) iterator.next();
				offset = getPossibleMapItem(offset);
			}
			IndependantLog.debug(debugmsg+" offset is "+offset);
			String[] offsetArray = StringUtils.convertCoordsToArray(offset, 4);
			
			if(offsetArray==null){
				throw new SAFSParamException(" Offset '"+offset+"' is not valid! The valid examples could be '20%,10%,%50,%60', '30,55,70,80', or even '20%,10%,70,80'.");
			}
			
			Point p1 = WDLibrary.getElementOffsetScreenLocation(compObject, offsetArray[0], offsetArray[1]);
			Point p2 = WDLibrary.getElementOffsetScreenLocation(toElement, offsetArray[2], offsetArray[3]);
			
			if(action.equalsIgnoreCase(GenericObjectFunctions.DRAGTO_KEYWORD)){
				Robot.leftDrag(p1, p2);
			}else{
				throw new SAFSException("Not supported yet.", SAFSException.CODE_ACTION_NOT_SUPPORTED);
			}
			
			String msg = " :"+action+" from "+p1.toString()+" to "+p2.toString();
			testRecordData.setStatusCode(StatusCodes.OK);
			log.logMessage(testRecordData.getFac(),
					genericText.convert(TXT_SUCCESS_4, altText+msg, windowName, compName, action, msg),
					PASSED_MESSAGE);	    
			
		} catch (Exception e) {
			String errorMsg = "Met "+StringUtils.debugmsg(e);
			IndependantLog.error(debugmsg+errorMsg);
			testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
			componentFailureMessage(errorMsg);
		}       
	}
	
	/**
	 * Set the text of Component box.
	 * 
	 * @param libName String,         the concrete Component name of class, which calls 'doSetText()' method, 
	 * 						          like 'EditBox', 'ComboBox'.
	 * @param isCharacter boolean, 	  if true, the text'll be treated as plain text, without special key dealing; <br>
	 * 							      if false, the text'll be treated as special keys.
	 * @param needVerify boolean, 	  if true, verify if the text has been correctly entered.(But if the text contains special keys, 
	 * 										   i.e. isCharacter is false, there's no verification.)
	 * 								  if false, no verification. 
	 */
	protected void doSetText(String libName, boolean isCharacter, boolean needVerify) {
		String dbg = StringUtils.debugmsg(false);
		String msg = "";
		
		if(params.size() < 1) {
			issueParameterCountFailure();
			return;
		}
		
		iterator = params.iterator();
		String text = iterator.next();
		IndependantLog.debug(dbg + "isCharacter="+isCharacter+", needVerify=" + needVerify + " proceeding with TEXT parameter '" + text + "'");
		if(needVerify){
			//For SetTextValue, If there're special keys, no verification.
			if(!isCharacter && StringUtils.containsSepcialKeys(text)){
				IndependantLog.debug(dbg+"Input text contains special keys, ignoring verification.");
				needVerify = false;
			}
		}
		
		try {
			setText(libName, isCharacter, text);

			if (needVerify) {
				IndependantLog.info("Verifying the " + libName + " ...");
				boolean verified = libComponent.verifyComponentBox(libName, text);
				int count = 0;
				
				//If verification fails, then try to reenter text.
				while(!verified && (count++<maxRetry)){
					IndependantLog.debug(dbg+" retry to enter '"+text+"'");
					setText(libName, isCharacter, text);
					//we MAY need to slow down, so that we can get all text from component-box after setting.
					verified = libComponent.verifyComponentBox(libName, text);
				}
				
				if (verified) {
					testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
					msg = genericText.convert(GENKEYS.SUCCESS_2,
							action + " '"+ "verifying" + "' successful",
							action,
							"verifying");
					log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);
				} else {
					testRecordData.setStatusCode(StatusCodes.GENERAL_SCRIPT_FAILURE);
					msg = failedText.convert(FAILKEYS.ERROR_PERFORMING_2,
										"Error performing '" + "verification" + "' on " + action,
										"verification",
										action);
					standardFailureMessage(msg, testRecordData.getInputRecord());
				}
			}else{
				//If we don't need to verify, we just set status as OK.
				testRecordData.setStatusCode(StatusCodes.NO_SCRIPT_FAILURE);
				msg = genericText.convert(GENKEYS.SUCCESS_3, 
						                  windowName +":"+ compName + " "+ action +" successful.",
						                  windowName, compName, action);
				log.logMessage(testRecordData.getFac(), msg, PASSED_MESSAGE);	
			}
		} catch(SeleniumPlusException spe) {
			IndependantLog.error(dbg + " failed due to: " + spe.getMessage());
			issueActionOnXFailure(compName, spe.getMessage());
		}
	}
	
	/**
	 * Clear the content of Component box first, and then enter the text into it.
	 * 
	 * @param libName String,         the concrete Component name of class, which calls 'setText()' method, 
	 * 						          like 'EditBox', 'ComboBox'.
	 * @param isCharacter boolean, 	  if true, the text'll be treated as plain text, without special key dealing; <br>
	 * 							      if false, the text'll be treated as special keys.
	 * @param text String,			  the content to be entered into Component box.
	 * 
	 * @throws SeleniumPlusException
	 */
	protected void setText(String libName, boolean isCharacter, String text) throws SeleniumPlusException{
		libComponent.clearComponentBox(libName);
		if(isCharacter){
			libComponent.inputComponentBoxChars(libName, text);
		}else{
			libComponent.inputComponentBoxKeys(libName, text);
		}
	}
}
