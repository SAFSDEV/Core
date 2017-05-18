/**
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.model;

import java.util.Map;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.Component;
import org.safs.selenium.webdriver.lib.RS;
import org.safs.selenium.webdriver.lib.SearchObject;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.tools.stringutils.StringUtilities;

/**
 *
 * History:<br>
 *
 *  <br>   Jun 12, 2014    (sbjlwa) Initial release.
 *  <br>   Nov 05, 2014    (sbjlwa) Modify refresh(): if the 'id' is not null and we cannot get object by id, then stop searching.
 *                                                    if we cannot get fresh-webelement, we will not update fields like id, tagname etc.
 *  <br>   FEB 25, 2016    (SBJLWA) Modify refresh(): log a warning message instead of throwing out an Exception if we cannot get webelement by id.
 *                                  Modify searchWebElement(): Use WebDriver as SearchContext to find the object, if it cannot be found with given SearchContext.
 */
public class DefaultRefreshable implements IRefreshable {

	/**
	 * The wrapped object, represents a control object on the web application<br>
	 * This object is refreshable, which means it can be reset if the control object<br>
	 * of web application has changed its status.<br>
	 * For now, 2 kinds of type can be accepted, Map returned from a javascript fucntion;<br>
	 * WebElement returned by Selenium WebDriver (with certain search criterion). There are convinient<br>
	 * references for them, {@link #map} and {@link #webelement}.<br>
	 * For now, Only WebElement can be refreshed by method {@link #refresh(boolean)}.<br>
	 *
	 */
	protected Object object = null;

	/**
	 * The convenient reference to embedded element object.
	 *
	 */
	protected Map<?,?> map = null;
	/**
	 * The convenient reference to embedded element object.<br>
	 * This element may become stale, see <a href='http://docs.seleniumhq.org/exceptions/stale_element_reference.jsp'>stale_element</a><br>
	 * If the element is stale, we need to refresh it by getting it again (by id or css or other ways)<br>
	 *
	 * @see #refresh(boolean)
	 */
	protected WebElement webelement;

	/**tagName is the html tag name for this web element, should never be null*/
	protected String tagName;
	/**id is the html id for this web element, it may be null*/
	protected String id;
	/**cssClass is the html css value for this web element, it may be null*/
	protected String cssClass;
	/**searchContext is used to refresh the WebElement according to id, tag, cssclass etc, it may be null*/
	protected SearchContext searchContext;
	/**possibleRecognitionStrings is used to refresh the WebElement, it may be null. Example as "id=xxx", "text=xxx" etc.*/
	protected String[] possibleRecognitionStrings;

	/**Counstructor ONLY for subclass, cannot be used to create an instance outside.*/
	protected DefaultRefreshable(){}

	/**
	 * Constructor used to create an uniformed DefaultRefreshable object.<br>
	 * User may override this one to parse their own object, if there are some new attritues<br>
	 * to analyze, then user should override {@link #updateFields()} and he MUST TAKE CARE:<br>
	 * in the overrided constructor, DO NOT forget to call {@link #updateFields()} after calling<br>
	 * super(object), below is an exmaple of implementation in subclass.<br>
	 * <pre>
	 * {@code
	 * super(object);
	 * updateFields();
	 * }
	 * </pre>
	 * The reason is that all fields will be initialized after calling super() or this() in a constructor.<br>
	 * This is a nature of Java Language. So even here we call {@link #updateFields()} to set value for<br>
	 * some fields of sub-class during instantiation of sub-class which overrides method {@link #updateFields()},<br>
	 * but after calling of super(object) in the constructor of sub-class, all fields of sub-class will be<br>
	 * initialized to the default value; What is set HERE will be LOST!!!<br>
	 *
	 * Or there is a better way, in the sub class's constructor user just need to call {@link #initialize(Object)}.<br>
	 * in that constructor, the no-parameter constructor {@link #DefaultRefreshable()} will be called implicitly,<br>
	 * and the fileds of subclass will be initialized, then the method {@link #initialize(Object)} will be called,<br>
	 * no fileds of subclass will be reinitialized!<br>
	 * But we should NEVER do anything except calling {@link #initialize(Object)} in the constructor, the reason is<br>
	 * that the no-parameter constructor will be called in subclass's constructor, and everything we added here will <br>
	 * not be visible for constructor in subclass. We MUST do that work in the method {@link #initialize(Object)} or {@link #updateFields()}<br>
	 *
	 * @param object Object, the element object. It may be a Map returned from javascript; it may be a WebElement.
	 */
	public DefaultRefreshable(Object object){
		initialize(object);
	}

	/**
	 * Set the embedded object and update the component's fields.<br>
	 * @param object
	 */
	protected void initialize(Object object){
		String debugmsg = StringUtils.debugmsg(this.getClass(), "setObject");

		try{
			this.object = object;
			if(object instanceof Map){
				map = (Map<?,?>) object;

			}else if(object instanceof WebElement){
				webelement = (WebElement) object;

			}else{
				if(object!=null) IndependantLog.error(debugmsg+" Need to handle "+object.getClass().getName());
				else IndependantLog.error(debugmsg+" Embedded object is null, cannot handle it.");
			}
		}catch(Exception e){
			IndependantLog.error(debugmsg+" Met Exception ",e);
		}

		updateFields();
	}

	/**
	 * set/update the class's local fields through the underlying WebElement.<br>
	 * Especially after calling {@link #refresh(boolean)}, the underlying WebElement may have been<br>
	 * refreshed, and the associated attributes may have different value, it is recommanded<br>
	 * to call this method to update the class's local field.<br>
	 * @see #refresh(boolean)
	 */
	protected void updateFields(){
		String debugmsg = StringUtils.debugmsg(getClass(), "updateFields");
		try{
			if(webelement!=null){
				id = getAttribute(Component.ATTRIBUTE_ID);
				cssClass = getAttribute(Component.ATTRIBUTE_CLASS);
				tagName = webelement.getTagName();
			}
		}catch(Exception e){
			IndependantLog.warn(debugmsg+" Fail to update fields due to "+StringUtils.debugmsg(e));
		}
	}

	/**
	 * Get the value of an attribute from the underlying WebElement object.
	 * @param attribute	String, the attribute name
	 * @return String, the value of the attribute
	 */
	public String getAttribute(String attribute){
		String debugmsg = StringUtils.debugmsg(getClass(), "getAttribute");
		String value = null;
		try{
			try{
				//If we refresh, it is sure that we will get the latest attribute's value, but it may take too much time.
				//DefaultRefreshable.this.refresh(false);
				if(map!=null)             value = StringUtilities.getString(map, attribute);
				else if(webelement!=null) value = webelement.getAttribute(attribute);
			}catch(StaleElementReferenceException sre){
				IndependantLog.warn(debugmsg+" WebElement is stale, refresh it.");
				//ONLY need to refresh the underlying WebElement object, sub-class may override refresh() to
				//get attributes' value from WebElement or something else. So we call DefaultRefreshable.this.refresh(false);
				DefaultRefreshable.this.refresh(false);
				value = webelement.getAttribute(attribute);
			}
		}catch(Throwable th){
			//IndependantLog.error(debugmsg+" Fail to get value for attribute '"+attribute+"' due to '"+StringUtils.debugmsg(th)+"'.");
		}

//		if(value==null) IndependantLog.warn("value is null for attribute '"+attribute+"'");
		return value;
	}

	/**
	 * Try to get the Selenium WebElement object 'webelement', if is null then try to find it by id.<br>
	 * It can be used to verify the status.<br>
	 * @return WebElement the embedded WebElement object or null.
	 */
	public WebElement getWebElement(){
		String debugmsg = StringUtils.debugmsg(getClass(), "getWebElement");
		if(webelement==null){
			if(getId()==null) IndependantLog.error(debugmsg+"Element's id is null.");
			else              webelement=WDLibrary.getObject(RS.id(getId()));
		}
		if(webelement==null) IndependantLog.error(debugmsg+"Cannot find webelement.");
		return webelement;
	}

	public void setWebElement(WebElement element) {
		this.object = element;
		this.webelement = element;
	}

	/**
	 * @return Object, the embedded element object
	 */
	public Object getEmbeddedObject(){
		return object;
	}
	/**
	 * The embedded element object (a Map) is returned from a javascript function.<br>
	 * @return Map, convenient Map reference of the embedded item object
	 */
	public Map<?, ?> getMap() {
		return map;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}
	/**
	 * searchContext is used to find the WebElement according to id, tag, cssclass etc, it may be null.<br>
	 * if it is null, {@link SearchObject#getWebDriver()} will be used a SearchContext.<br>
	 * if it is not null, it may be stale itself!<br>
	 */
	public SearchContext getSearchContext() {
		return searchContext;
	}
	public void setSearchContext(SearchContext searchContext){
		this.searchContext = searchContext;
	}

	public String[] getPossibleRecognitionStrings() {
		return possibleRecognitionStrings;
	}
	public void setPossibleRecognitionStrings(String[] possibleRecognitionStrings) {
		this.possibleRecognitionStrings = possibleRecognitionStrings;
	}

	/**
	 * Refresh the webelement by its 'ID' or 'CSS CLASS' or preset 'Recognition Strings'.<br>
	 * And update the value of fields according to the refreshed-WebElement<br>
	 *
	 * @param checkStale boolean, whether to check if the webelement is stable before refresh.
	 *                            true, check stable; false, force refresh directly without check.
	 * @return boolean, true if the refresh succeed.
	 */
	public boolean refresh(boolean checkStale) {
		// TODO How to make sure that the refreshed WebElement is the "same" object as before.
		// "same" means that is the object we want, with "ID" it is quite sure that they are
		// same; but with "CSS CLASS" or other "Recognition String", it is not so sure.
		String debugmsg = StringUtils.debugmsg(false);
		boolean needRefreshed = true;
		String recognitionString = null;
		WebElement freshWebElement = null;

		try{
			if(checkStale){
				try{
					needRefreshed = WDLibrary.isStale(getWebElement());
				}catch(SeleniumPlusException se){
					IndependantLog.warn(StringUtils.debugmsg(se));
				}
			}

			if(needRefreshed){
				//User defined RS have higher priority
				if(freshWebElement==null && getPossibleRecognitionStrings()!=null){
					for(String rs: getPossibleRecognitionStrings()){
						freshWebElement = searchWebElement(getSearchContext(), rs);
						if(freshWebElement!=null) break;
					}
				}

				if(freshWebElement==null && getId()!=null && !getId().trim().isEmpty()){
					recognitionString=RS.id(getId());
					freshWebElement = searchWebElement(getSearchContext(), recognitionString);
					//In the real test (S1234426), the webelement's id will change at runtime, no so reliable!
					//if we cannot find webelement by id, just log a warning message.
					if(freshWebElement==null) IndependantLog.warn(debugmsg+"cannot find webelement by id '"+getId()+"'");
				}

				if(freshWebElement==null && getCssClass()!=null){
					recognitionString = RS.css(getTagName(), getCssClass());
					freshWebElement = searchWebElement(getSearchContext(), recognitionString);
				}

				if(freshWebElement!=null){
					setWebElement(freshWebElement);
					//update the class's fields according to the refreshed-webelement
					updateFields();
					return true;
				}
				IndependantLog.warn(debugmsg+"Fail to refresh the embedded web element.");
			}

		}catch(Throwable th){
			IndependantLog.error(debugmsg+"Fail to refresh the embedded web element.", th);
		}
		return false;
	}

	private WebElement searchWebElement(SearchContext sc, String recognitionString){
		String debugmsg = StringUtils.debugmsg(getClass(), "searchWebElement");
		WebElement temp = null;

		try{
			if(sc!=null) temp = SearchObject.getObject(sc, recognitionString);
			//Use WebDriver as SearchContext to find the object, if it cannot be found with given SearchContext
			if(temp==null) temp = SearchObject.getObject(recognitionString);
		}catch(Throwable th){
			IndependantLog.debug(debugmsg+"Cannot get webelement by rs "+recognitionString, th);
		}

		return temp;
	}

}
