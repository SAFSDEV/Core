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
 *
 * History:
 * @date 2019-08-22    (Lei Wang) Moved codes from AISearchBase.
 * @date 2019-08-27    (Lei Wang) Overloaded method process(): handle one more parameter 'customerType'.
 * @date 2019-09-03    (Lei Wang) Modified method process(): decrement the 'matchedTime' if we meet an exception.
 */
package org.safs.cukes.ai.selenium;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.Log;
import org.safs.SAFSException;
import org.safs.SAFSObjectNotFoundException;
import org.safs.SAFSPlus;
import org.safs.StringUtils;
import org.safs.model.commands.GenericMasterFunctions;
import org.safs.model.commands.GenericObjectFunctions;
import org.safs.selenium.webdriver.WebDriverGUIUtilities;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;

/**
 * Used to hold checkbox test step definitions for gherkin feature files.
 * CheckBoxes are generally found by matching the {@link Criteria}.
 */
public class AIComponent extends AISearchBase {
	/** "Component" */
	public static final String TYPE = "Component";

	/** The library component */
	protected org.safs.selenium.webdriver.lib.Component libComponent = null;

	public static int MAX_TRY_DEFAULT = 5;
	/** The maximum time to retry 'enter the text' into Component box if the verification fails. */
	protected int maxRetry = MAX_TRY_DEFAULT;

	protected void initComponent(WebElement we) throws SeleniumPlusException{
		libComponent = new org.safs.selenium.webdriver.lib.Component(we);
	}

	/**
	 * Highlight the WebElement if user has turned on the highlight ability.
	 * Should be called in the {@link #initComponent(WebElement)} after the {@link #libComponent} has been initialized.<br>
	 * @return boolean true if the WebElement has been highlighted.
	 */
	protected boolean highlight(){
		if(WebDriverGUIUtilities.HIGHLIGHT){
			if(libComponent!=null)
				return WDLibrary.highlight(libComponent.getWebElement());
		}
		return false;
	}
	/**
	 * Clear the highlight after the execution if user has turned on the highlight ability.
	 * @return boolean if the highlighted rectangle has been cleared successfully.
	 */
	protected boolean clearHighlight(){
		if(WebDriverGUIUtilities.HIGHLIGHT){
			return WDLibrary.clearHighlight();
		}
		return false;
	}

	/**
	 *
	 * @param libName String, the name of the library, like "EditBox", "CheckBox" etc.
	 * @param text String, the characters or keys to input
	 * @param isCharacter boolean, if the text should be input as characters or as keys
	 * @param needVerify boolean, if need to verify the text has been input correctly
	 * @return boolean
	 * @throws SeleniumPlusException
	 */
	protected boolean doSetText(String libName, String text, boolean isCharacter, boolean needVerify) throws SeleniumPlusException{
		String dbg = StringUtils.debugmsg(false);

		IndependantLog.debug(dbg + "isCharacter="+isCharacter+", needVerify=" + needVerify + " proceeding with TEXT parameter '" + text + "'");
		if(needVerify){
			//For SetTextValue, If there're special keys, no verification.
			if(!isCharacter && StringUtils.containsSepcialKeys(text)){
				IndependantLog.debug(dbg+"Input text contains special keys, ignoring verification.");
				needVerify = false;
			}
		}

		setText(libName, isCharacter, text);

		boolean verified = false;
		if (needVerify) {
			IndependantLog.info("Verifying the " + libName + " ...");
			verified = libComponent.verifyComponentBox(libName, text);
			int count = 0;

			//If verification fails, then try to reenter text.
			while(!verified && (count++<maxRetry)){
				IndependantLog.debug(dbg+" retry to enter '"+text+"'");
				setText(libName, isCharacter, text);
				//we MAY need to slow down, so that we can get all text from component-box after setting.
				verified = libComponent.verifyComponentBox(libName, text);
			}

			return verified;
		}else{
			return true;
		}
	}

	/**
	 * Clear the content of Component box first, and then enter the text into it.
	 *
	 * @param libName String,         the concrete Component name of class, which calls 'setText()' method, like 'EditBox', 'ComboBox'.
	 * @param isCharacter boolean, 	  if true, the text'll be treated as plain text, without special key dealing; if false, the text'll be treated as special keys.
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

	/** @return String, represent this component's type. */
	protected String getType(){
		return TYPE;
	}

	/**
	 * Handle the <a href="/sqabasic2000/SAFSReference.php">SAFS Keywords</a> against the a component matching the provided textlabel.<br>
	 *
	 * @param action String, the action to perform
	 * @param criteria String, the search-conditions helping to find the component<br>
	 *                         it can be the 'text label' associated with (or near) the component<br>
	 *                         it can be the xpath (start with prefix <b>XPATH=</b>) to identify the component<br>
	 *                         it can be the css-selector (start with prefix <b>CSS=</b>) to find the component<br>
	 *                         It can be map-item (plain-text, SAFS-recognition-string) defined in a map file.<br>
	 * @param nthMatched int, 1-based index, the Nth matched component if there are multiple components matching the search criteria.
	 * @param parameters List<String>, the parameters required by the <b>action</b>
	 * @param customerType String, the customer-defined type for matching component.
	 * @return
	 * @throws SAFSException if target object cannot be found, or the keyword is not supported, or any exception has been met.
	 */
	protected void process(String action, String criteria, int nthMatched, List<String> parameters, String customerType) throws SAFSException{
		process(action, generateCriteria(criteria, customerType), nthMatched, parameters);
	}

	/**
	 * Handle the <a href="/sqabasic2000/SAFSReference.php">SAFS Keywords</a> against the a component matching the provided textlabel.<br>
	 *
	 * @param action String, the action to perform
	 * @param criteria String, the search-conditions helping to find the component<br>
	 *                         it can be the 'text label' associated with (or near) the component<br>
	 *                         it can be the xpath (start with prefix <b>XPATH=</b>) to identify the component<br>
	 *                         it can be the css-selector (start with prefix <b>CSS=</b>) to find the component<br>
	 *                         It can be map-item (plain-text, SAFS-recognition-string) defined in a map file.<br>
	 * @param nthMatched int, 1-based index, the Nth matched component if there are multiple components matching the search criteria.
	 * @param parameters List<String>, the parameters required by the <b>action</b>
	 * @return
	 * @throws SAFSException if target object cannot be found, or the keyword is not supported, or any exception has been met.
	 */
	protected void process(String action, String criteria, int nthMatched, List<String> parameters) throws SAFSException{
		process(action, generateCriteria(criteria), nthMatched, parameters);
	}

	/**
	 * Handle the <a href="/sqabasic2000/SAFSReference.php">SAFS Keywords</a> against the a component matching the provided textlabel.<br>
	 *
	 * @param action String, the action to perform
	 * @param criteria Criteria, the search-conditions helping to find the component
	 * @param nthMatched int, 1-based index, the Nth matched component if there are multiple components matching the search criteria.
	 * @param parameters List<String>, the parameters required by the <b>action</b>
	 * @return
	 * @throws SAFSException if target object cannot be found, or the keyword is not supported, or any exception has been met.
	 */
	protected void process(String action, Criteria criteria, int nthMatched, List<String> parameters) throws SAFSException{
		String dbgmsg = StringUtils.debugmsg(false);
		String msg = "perform "+ action+ " on the '"+ criteria +"'";
		try{
			//Initialize the library component
			if(ignoreCache(action)){
				libComponent = null;
			}else{
				libComponent = getCachedLibComponent(criteria);
			}

			if(libComponent==null){
				List<WebElement> elements = findElements(criteria);
				int matchedTime = 0;
				if(elements!=null && !elements.isEmpty()){
					for(int i=0;i<elements.size();i++){
						try{
							matchedTime++;
							if(matchedTime == nthMatched){
								initComponent(elements.get(i));
								break;
							}
						}catch(SeleniumPlusException se){
							IndependantLog.warn(dbgmsg + "could not create lib component, due to "+se);
							matchedTime--;
						}
					}
				}
				if(matchedTime!=nthMatched){
					throw new SAFSObjectNotFoundException("Found "+matchedTime+" times '"+ criteria +"' , BUT we need the "+nthMatched+"th matched one!");
				}
				saveCachedLibComponent(criteria, libComponent);
			}else{
				IndependantLog.debug(dbgmsg+" Found cached library component "+libComponent.getClass().getSimpleName()+".");
			}

			if(libComponent!=null){
				highlight();

				msg += " of ID='"+ libComponent.getId() +"'";
				if(parameters!=null && !parameters.isEmpty()) msg += ", with parameters: "+parameters.toString();

				localProcess(action, parameters);

				IndependantLog.info(dbgmsg + msg);
				Logging.LogTestSuccess(msg);
			}else{
				throw new SAFSObjectNotFoundException("Requested '"+ criteria +"' not found!");
			}
		}catch(Exception e){
			IndependantLog.error(dbgmsg+" Met exception "+e);
			Logging.LogTestFailure("Failed to " + msg+ ", due to internal "+e);
			if(_abort_on_find_failure && e instanceof SAFSObjectNotFoundException) throw e;
		}finally{
			clearHighlight();
		}
	}

	protected boolean ignoreCache(String action){
		return GenericMasterFunctions.GUIDOESEXIST_KEYWORD.equalsIgnoreCase(action);
	}

	/**
	 * Handle the specific keywords against a specific component.<br>
	 *
	 * @param action String, the action to perform
	 * @param parameters List<String>, the action's parameters
	 * @return
	 * @throws SAFSException if target object cannot be found, or the action is not supported, or any exception has been met.
	 */
	protected void localProcess(String action, List<String> parameters) throws SAFSException {

		IndependantLog.debug("handling '"+action+"' on element with ID '"+libComponent.getId()+"' with parameters "+Arrays.toString(parameters.toArray()));

		if("tap".equalsIgnoreCase(action) ||
			GenericObjectFunctions.CLICK_KEYWORD.equalsIgnoreCase(action)){

			java.awt.Point point = null;
			String autoscroll =null;
			String verify = null;
			String refresh = null;
			if(parameters!=null){
				if(parameters.size()>0) point = checkForCoord(parameters.get(0));
				if(parameters.size()>1) autoscroll = parameters.get(1);
				if(parameters.size()>2) verify = parameters.get(2);
				if(parameters.size()>3) refresh = parameters.get(3);
			}

			if (point==null) {
				WDLibrary.click(libComponent.getWebElement(), autoscroll, verify, refresh);
			}else{
				WDLibrary.click(libComponent.getWebElement(), point, autoscroll, verify, refresh);
			}

		}else if(GenericMasterFunctions.GUIDOESEXIST_KEYWORD.equalsIgnoreCase(action)){

			if(!WDLibrary.isVisible(libComponent.getWebElement())){
				throw new SeleniumPlusException("element is not visible.", SeleniumPlusException.CODE_OBJECT_IS_INVISIBLE);
			}

		}else{
			throw new org.safs.SAFSNotImplementedException("Unknown action '"+action+"' for "+getType()+".");
		}

	}

	protected java.awt.Point checkForCoord(String coordinate) {
		String debugmsg = StringUtils.debugmsg(false);
		String[] coordsPair = null;
		java.awt.Point point = null;

		Log.info(debugmsg+ "checking for coordinate: " + coordinate);
		if(!StringUtils.isValid(coordinate)){
			IndependantLog.warn(StringUtils.debugmsg(false)+"The passed in parameter '"+coordinate+"' is not valid");
			return null;
		}
		//Treat the parameter coordinate as a reference and try to get the coordinate String from the Map file.
		//TODO we need to consider the "mapID" and "section" in the future, they ("mapID" and "section") are specified in the "cucumber step" and processed by TypeRegistryConfiguration
		String lookup = SAFSPlus._getMappedValue(null, null, coordinate);
		//If we can not find the value for 'coordinate' from the Map file, we use it directly as coordinate String.
		if( lookup == null) lookup = coordinate;
		//convert the coordinate string "x, y" into an array [x, y]
		coordsPair = StringUtils.convertCoordsToArray(lookup, 2);
		//convert the coordinate array [x, y] based on the component's rectangle
		point = StringUtils.convertCoords(coordsPair, getComponentRectangle());
		IndependantLog.debug(debugmsg+" the final coordinate is '"+point+"'.");

		return point;
	}

	protected Rectangle getComponentRectangle(){
		Rectangle rectangle = null;
		try{
			rectangle = WDLibrary.getRectangleOnScreen(libComponent.getWebElement());
		}catch(Exception e){
			IndependantLog.warn(StringUtils.debugmsg(false)+" Met "+StringUtils.debugmsg(e));
		}

	    return rectangle;
	}

	/**
	 * @param recognitionString String, it can be
	 *        <ol>
	 *        <li>text-label
	 *        <li>a recognition-string containing both window's info and component's info separated by {@link TypeRegistryConfiguration#SEPARATOR_WIN_COMP}.
	 *        </ol>
	 * @return Criteria, it is used to find matched WebElement
	 */
	protected Criteria generateCriteria(String recognitionString){
		return generateCriteria(recognitionString, null);
	}

	/**
	 * @param recognitionString String, it can be
	 *        <ol>
	 *        <li>text-label
	 *        <li>a recognition-string containing both window's info and component's info separated by {@link TypeRegistryConfiguration#SEPARATOR_WIN_COMP}.
	 *        </ol>
	 * @param customerType String, the customer-defined type for matching component.
	 *
	 * @return Criteria, it is used to find matched WebElement
	 */
	protected Criteria generateCriteria(String recognitionString, String customerType){
		Criteria criteria = null;
		String type = customerType==null? getType(): customerType;

		String[] windowAndComponent = StringUtils.getTokenArray(recognitionString, TypeRegistryConfiguration.SEPARATOR_WIN_COMP);

		if(windowAndComponent.length>1){
			criteria = new Criteria(windowAndComponent[0], windowAndComponent[1], _substring_matches_allowed, type);
		}else{
			criteria = new Criteria(null, windowAndComponent[0], _substring_matches_allowed, type);
		}

		return criteria;
	}

}
