/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.selenium.webdriver.SeleniumPlus.WDTimeOut;
import org.safs.selenium.webdriver.lib.model.DefaultRefreshable;
import org.safs.selenium.webdriver.lib.model.Element;
import org.safs.selenium.webdriver.lib.model.IOperable;
import org.safs.selenium.webdriver.lib.model.IWebAccessibleInternetRole;
import org.safs.selenium.webdriver.lib.model.TextMatchingCriterion;

/**
 * 
 * History:<br>
 * 
 *  <br>   Dec 26, 2013    (Lei Wang) Initial release.
 *  <br>   Feb 12, 2014    (Lei Wang) Add method refresh() to refresh the stale embedded webelement.
 */
public class Component extends DefaultRefreshable implements IWebAccessibleInternetRole, IOperable{
	/**'id' a standard html attribute*/
	public static final String ATTRIBUTE_ID = "id";
	/**'name' a standard html attribute*/
	public static final String ATTRIBUTE_NAME = "name";
	/**'class' a standard html attribute*/
	public static final String ATTRIBUTE_CLASS = "class";
	/**'value' a standard html attribute*/
	public static final String ATTRIBUTE_VALUE = "value";
	/**'multiple' a html attribute*/
	public static final String ATTRIBUTE_MULTIPLE = "multiple";
	/**'index' a html attribute*/
	public static final String ATTRIBUTE_INDEX = "index";
	/**'visibility' a html attribute*/
	public static final String ATTRIBUTE_VISIBILITY = "visibility";
	/**value 'hidden' for 'visibility' attribute*/
	public static final String VALUE_VISIBILITY_HIDDEN = "hidden";
	/**value 'visible' for 'visibility' attribute*/
	public static final String VALUE_VISIBILITY_VISIBLE = "visible";
	
	/**'type' a standard html attribute*/
	public static final String ATTRIBUTE_TYPE = "type";
	/**'text' a value for standard html attribute 'type'*/
	public static final String VALUE_TEXT_ATTRIBUTE_TYPE = "text";
	/**'password' a value for standard html attribute 'type'*/
	public static final String VALUE_PASSWORD_ATTRIBUTE_TYPE = "password";
	/**'radio' a value for standard html attribute 'type'*/
	public static final String VALUE_RADIO_ATTRIBUTE_TYPE = "radio";
	/**'checkbox' a value for standard html attribute 'type'*/
	public static final String VALUE_CHECKBOX_ATTRIBUTE_TYPE = "checkbox";
	/**'submit' a value for standard html attribute 'type'*/
	public static final String VALUE_SUBMIT_ATTRIBUTE_TYPE = "submit";
	
	/**'select' a standard html tag*/
	public static final String TAG_HTML_SELECT = "select";
	/**'input' a standard html tag*/
	public static final String TAG_HTML_INPUT = "input";
	
	/**'widgetid' attribute used by dojo*/
	public static final String ATTRIBUTE_WIDGETID = "widgetid";
	/**'dijitpopupparent' attribute used by dojo*/
	public static final String ATTRIBUTE_DIJITPOPUPPARENT = "dijitpopupparent";
		
	/**widgetid is the html-dojo object id for this web element, it may be null*/
	protected String widgetid;
	/**dijitpopupparent is an attribute of the html-dojo popup object for this web element
	 * its value is the id of the dojo object to which this popup is related, it may be null*/
	protected String dijitpopupparent;
	/**attribute 'role' of 'Accessible Rich Internet Applications'*/
	protected String waiRole;
	/**indicate if this component follows the rules of 'Accessible Rich Internet Applications'*/
	protected boolean accessible = false;
	
	/**Represents an object can be operated.*/
	protected IOperable anOperableObject = null;
	/**A cache containing IOperable objects for a certain WebElement.*/
	protected Map<WebElement, IOperable> operableObjects = new HashMap<WebElement, IOperable>();
	
	protected Component(){}
	/**
	 * @param component	WebElement the component to operate.
	 * @throws SeleniumPlusException if the component is null or is not visible
	 */
	public Component(WebElement component) throws SeleniumPlusException{
		initialize(component);
	}
	
	/**
	 * According to WebElement, initialize necessary resources including 'embedded object', 'attribute'<br>
	 * and 'operable object' etc. In this method, the {@link #initialize(Object)} will be called to<br>
	 * do some of the initialization work.<br>
	 * <font color="red">NOTE: This method doesn't override {@link #initialize(Object)}, in the subclass, <br>
	 * user should call or override this method, but NOT {@link #initialize(Object)}.<br>
	 * </font>
	 * @param component WebElement, the embedded WebElement object.
	 * @throws SeleniumPlusException if the component is null or is not visible,
	 *                               or if no appropriate IOperable instance can be got
	 * @see {@link #initialize(Object)}
	 */
	public void initialize(WebElement component) throws SeleniumPlusException{
		WDLibrary.checkNotNull(component);
		
		// Carl Nagle -- RE EVALUATE this.  
		// It may be desirable we want to interrogate properties of HIDDEN form controls
		if(!WDLibrary.isVisible(component)){
			String msg = "The web element is NOT visible! You should not operate it.";
			throw new SeleniumPlusException(msg, SeleniumPlusException.CODE_OBJECT_IS_INVISIBLE);
		}

		super.initialize(component);
		
		if(anOperableObject==null){
			throw new SeleniumPlusException("Can not create a proper Operable object.", SeleniumPlusException.CODE_OBJECT_IS_NULL);
		}
	}
	
	protected void updateFields(){
		super.updateFields();
		widgetid = getAttribute(ATTRIBUTE_WIDGETID);		
		dijitpopupparent = getAttribute(ATTRIBUTE_DIJITPOPUPPARENT);
		waiRole = getAttribute(ATTRIBUTE_WAI_ROLE);
		accessible = (waiRole!=null);
		
		//Update the Operable object, get from cache or create a new one.
		synchronized(operableObjects){
			if(!operableObjects.containsKey(webelement)){
				anOperableObject = createOperable(webelement);
				//don't put a null in the HashMap 'operableObjects'
				if(anOperableObject!=null){
					operableObjects.put(webelement, anOperableObject);
				}
			}else{
				anOperableObject = operableObjects.get(webelement);
			}			
		}
		if(anOperableObject!=null) IndependantLog.debug("Using '"+anOperableObject.getClass().getName()+"'");
	}
	
	/**
	 * Create a IOperable object according to 'webelement'.<br>
	 * User needs to override this method to provide a real IOperable object.<br>
	 * @param webelement WebElement, from which the IOperable object will be created
	 * @return IOperable
	 */
	protected IOperable createOperable(WebElement webelement){
		IndependantLog.warn("Correct IOperable object support is missing in "+ getClass().getName());
		IndependantLog.warn("Using default IOperable Component with minimal functionality.");
		return this;
	}
	
	/**
	 * Clear the cache 'operableObjects'.<br>
	 * Also clear the cache associated with the IOperable objects.<br>
	 */
	public void clearCache(){
		synchronized(operableObjects){
			IOperable operable = null;
			Iterator<WebElement> keys = operableObjects.keySet().iterator();
			while(keys.hasNext()){
				operable = operableObjects.get(keys.next());
				// (operable != this) to avoid reentrant infinite loop 
				if(operable!=null && operable!=this) operable.clearCache();
			}
			operableObjects.clear();
		}
	}
	
	public boolean isAccessible(){ return accessible;}
	
	public String getWaiRole(){ return waiRole; }

	public String getWidgetid() {
		return widgetid;
	}

	public void setWidgetid(String widgetid) {
		this.widgetid = widgetid;
	}

	public String getDijitpopupparent() {
		return dijitpopupparent;
	}

	public void setDijitpopupparent(String dijitpopupparent) {
		this.dijitpopupparent = dijitpopupparent;
	}

	/**
	 * <br><em>Purpose:</em> setFocus
	 * @throws SeleniumPlusException if we are unable to process the keystrokes successfully.
	 * @see org.safs.robot.Robot#inputKeys(String)
	 **/
	public void setFocus() throws SeleniumPlusException{
		WDLibrary.windowSetFocus(getWebElement());
	}
	
	/**
	 * Does NOT clear any existing text in the control, but does attempt to insure the window/control has focus.
	 * <br><em>Purpose:</em> 
	 * <a href="http://safsdev.sourceforge.net/sqabasic2000/SeleniumGenericMasterFunctionsReference.htm#detail_InputKeys" alt="inputKeys Keyword Reference" title="inputKeys Keyword Reference">inputKeys</a>
	 * @throws SeleniumPlusException if we are unable to process the keystrokes successfully.
	 * @see org.safs.robot.Robot#inputKeys(String)
	 **/
	public void inputKeys(String keystrokes) throws SeleniumPlusException{
		WDLibrary.inputKeys(getWebElement(), keystrokes);
	}	
	
	public void inputChars(String keystrokes) throws SeleniumPlusException{
		WDLibrary.inputChars(getWebElement(), keystrokes);
	}	
	
	/**
	 * According to a certain 'search condition', get the matched subitem within this Component.<br>
	 * This method just return a null. Its sublcass SHOULD give a detail implementation.<br>
	 * @param criterion TextMatchingCriterion, the search condition
	 * @return Element, the matched element
	 * @throws SeleniumPlusException
	 */
	public Element getMatchedElement(TextMatchingCriterion criterion) throws SeleniumPlusException {
		return null;
	}
}
