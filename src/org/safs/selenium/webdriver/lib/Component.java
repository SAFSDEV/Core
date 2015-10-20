/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * History:
 * 
 *  DEC 26, 2013    (sbjlwa) Initial release.
 *  FEB 12, 2014    (sbjlwa) Add method refresh() to refresh the stale embedded webelement.
 *  OCT 16, 2015    (sbjlwa) Refector to create IOperable object properly. 
 */
package org.safs.selenium.webdriver.lib;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.model.DefaultRefreshable;
import org.safs.selenium.webdriver.lib.model.Element;
import org.safs.selenium.webdriver.lib.model.IOperable;
import org.safs.selenium.webdriver.lib.model.IWebAccessibleInternetRole;
import org.safs.selenium.webdriver.lib.model.TextMatchingCriterion;

/** 
 * A library class to handle generic functionalities, such as Click, HoverMouse, GetGUIImage etc. for
 * all kinds of component.
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
		String debugmsg = StringUtils.debugmsg(false);
		WDLibrary.checkNotNull(component);
		
		// CANAGL -- RE EVALUATE this.  
		// It may be desirable we want to interrogate properties of HIDDEN form controls
		if(!WDLibrary.isVisible(component)){
			String msg = "The web element is NOT visible! You should not operate it.";
			IndependantLog.warn(debugmsg+msg);
			if(permitInvisible(component)){
				IndependantLog.debug(debugmsg+" Invisible component allowed.");
			}else{
				throw new SeleniumPlusException(msg, SeleniumPlusException.CODE_OBJECT_IS_INVISIBLE);
			}

		}

		super.initialize(component);
		
		if(anOperableObject==null){
			throw new SeleniumPlusException("Can not create a proper Operable object.", SeleniumPlusException.CODE_OBJECT_IS_NULL);
		}
	}
	
	/**
	 * This method will decide if the invisible web element is permitted to be operated.<br>
	 * Subclass could override this method to provide the detail implementation.<br>
	 * @param component WebElement the component to check
	 * @return boolean true if this invisible component is permitted to operate.
	 * @see #initialize(WebElement)
	 */
	protected boolean permitInvisible(WebElement component){
		boolean permitted = false;

		IndependantLog.debug(StringUtils.debugmsg(false)+" Trying to see if the invisible component is allowed to be handled.");
		
		return permitted;
	}
	
	protected void updateFields(){
		String debugmsg = StringUtils.debugmsg(false);
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

		if(anOperableObject==null){
			IndependantLog.error(debugmsg+"The Operable object is null!");
		}else{
			IndependantLog.debug(debugmsg+"Using Operable '"+anOperableObject.getClass().getName()+"'");
			try{
				castOperable();
			}catch(Exception e){
				IndependantLog.warn(debugmsg+"fail to convert the Operable object to specific one, it can ONLY support generic functionality!");
			}
		}
	}
	
	/**
	 * Cast the IOperable object to the specific one.<br>
	 * The subclasses will override this method as they know what specific Operable to use.<br>
	 * Here a void implementation is given, as not all subclass need the specific Operable, such as EditBox.<br>
	 * Cast may throw Exception, we should catch it if calling this method.<br>
	 * This method should be called after {@link #anOperableObject} has been initialized.<br>
	 * 
	 * @see #anOperableObject
	 * @see #updateFields()
	 */
	protected void castOperable(){
		IndependantLog.info(StringUtils.debugmsg(false)+" Casting Operable ojbect '"+anOperableObject.getClass().getName()+"' to specific one.");
	}
	
	/**
	 * Create a IOperable object according to 'webelement'.<br>
	 * User could to override this method to provide an IOperable object, but mostly<br>
	 * user should override {@link #createDOJOOperable()}, {@link #createGenericOperable()}, <br>
	 * {@link #createHTMLOperable()} and {@link #createSAPOperable()} to provide specific <br>
	 * IOperable of certain domain.<br> 
	 * @param webelement WebElement, from which the IOperable object will be created
	 * @return IOperable
	 * @see #createDOJOOperable()
	 * @see #createGenericOperable()
	 * @see #createHTMLOperable()
	 * @see #createSAPOperable()
	 */
	protected IOperable createOperable(WebElement webelement){
		String debugmsg = StringUtils.debugmsg(false);
		IOperable operable = null;
		try{			
			if(WDLibrary.isDojoDomain(webelement)){
				IndependantLog.info(debugmsg+" trying to create IOperable for DOJO.");
				operable = createDOJOOperable();
			}else if(WDLibrary.isSAPDomain(webelement)){
				IndependantLog.info(debugmsg+" trying to create IOperable for SAP/OpenUI5.");
				operable = createSAPOperable();
			}else{
				IndependantLog.info(debugmsg+" trying to create IOperable for HTML.");
				operable = createHTMLOperable();
			}
		}catch(Exception e){ IndependantLog.debug(debugmsg+" Met Exception ", e); }
		
		if(operable==null){
			IndependantLog.info(debugmsg+" trying to create default IOperable for "+getClass().getName());
			operable = createDefaultOperable();
		}
		
		if(operable==null){
			IndependantLog.warn(debugmsg+"Correct IOperable object support is missing in "+ getClass().getName());
			operable = createGenericOperable();
		}
		
		return operable;
	}
	/**
	 * Create the IOperable object for DOJO domain.<br>
	 * Subclass SHOULD override this method if DOJO will be supported.<br>
	 * @see #createOperable(WebElement)
	 */
	protected IOperable createDOJOOperable(){
		IndependantLog.warn(StringUtils.debugmsg(false)+" Cannot create IOperable for DOJO at this time.");
		return null;
	}
	/**
	 * Create the IOperable object for SAP domain.<br>
	 * Subclass SHOULD override this method if SAP will be supported.<br>
	 * @see #createOperable(WebElement)
	 */
	protected IOperable createSAPOperable(){
		IndependantLog.warn(StringUtils.debugmsg(false)+" Cannot create IOperable for SAP at this time.");
		return null;
	}
	/**
	 * Create the IOperable object for HTML domain.<br>
	 * Subclass SHOULD override this method if HTML will be supported.<br>
	 * @see #createOperable(WebElement)
	 */
	protected IOperable createHTMLOperable(){
		IndependantLog.warn(StringUtils.debugmsg(false)+" Cannot create IOperable for HTML at this time.");
		return null;
	}
	/**
	 * Create the default IOperable object for a certain specific component.<br>
	 * It is different from the {@link #createGenericOperable()}, which will provide an IOperable<br>
	 * to support the minimal generic functionalities, like GetGUIImage, Click, HoverMouse etc.<br>
	 * Subclass COULD override this method to provide a default backup IOperable when other ways fail to create IOperable.<br>
	 * @see #createOperable(WebElement)
	 * @see #createDOJOOperable()
	 * @see #createHTMLOperable()
	 * @see #createSAPOperable()
	 */
	protected IOperable createDefaultOperable(){
		IndependantLog.warn(StringUtils.debugmsg(false)+" Cannot create Default IOperable at this time.");
		return null;
	}
	
	/**
	 * Create the generic IOperable object to support the minimal generic functionalities, like GetGUIImage, Click, HoverMouse etc.<br>
	 * This generic IOperable will be used if no other specific IOperable is available.<br>
	 * Normally, subclass should NOT override this method.<br>
	 */
	protected IOperable createGenericOperable(){
		IndependantLog.warn("Using generic IOperable Component with minimal functionalities.");
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
	public boolean setFocus() throws SeleniumPlusException{
		return WDLibrary.windowSetFocus(getWebElement());
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
