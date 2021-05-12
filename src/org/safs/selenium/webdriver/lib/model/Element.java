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
 * History:
 *
 *  May 30, 2014    (Lei Wang) Initial release.
 *  OCT 15, 2014    (Lei Wang) Add 'visible' property.
 *  OCT 29, 2015    (Lei Wang) Modify updateFields(): Trim the property 'label', the leading/ending spaces will be ignored.
 *  JUL 05, 2014    (Lei Wang) Modified equals(): Instead of returning false, Log a warning message if the ID does not match.
 *                                              Only Label and TagName will be used to compare, the css (easily changed) will not be counted.
 *  OCT 31, 2018    (Lei Wang) Modified getClickableWebElement(): wait the web-element to be click-able.
 */
package org.safs.selenium.webdriver.lib.model;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.safs.IndependantLog;
import org.safs.Processor;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.tools.drivers.DriverConstant.SeleniumConfigConstant;

/**
 * It represents originally sap.ui.core.Element<br>
 *
 */
public class Element extends DefaultRefreshable{
	/**'id' used to find the element on the page*/
	public static final String PROPERTY_ID 			= "id";
	/**'label' the text of the element shown on the page*/
	public static final String PROPERTY_LABEL 		= "label";
	/**'value' the value/key of the element, be transfered during submit*/
	public static final String PROPERTY_VALUE 		= "value";
	/**'index' the index of the element within its container ComboBox/ListBox etc.*/
	public static final String PROPERTY_INDEX 		= "index";
	/**'selected' if the element is selected.*/
	public static final String PROPERTY_SELECTED 	= "selected";
	/**'disabled' if the element is disabled.*/
	public static final String PROPERTY_DISABLED 	= "disabled";
	/**'expanded' if the element is expanded*/
	public static final String PROPERTY_EXPANDED 	= "expanded";
	/**'selectable' if the element is selectable.*/
	public static final String PROPERTY_SELECTABLE 	= "selectable";
	/**'icon' the url of icon, if the element contains an icon.*/
	public static final String PROPERTY_ICON 		= "icon";
	/**'textContent' property returns the textual content of the specified DOM node, and all its descendants.*/
	public static final String PROPERTY_TEXTCONTENT	= "textContent";
	/**'class' property returns the value of attribute 'class' of the specified DOM node.*/
	public static final String PROPERTY_CLASS	= "class";
	/**'visible' if the element is visible.*/
	public static final String PROPERTY_VISIBLE	= "visible";

	/**clickableWebElement represents the WebElement that is click-able. It may be different than
	 * webelement, which contain the element's properties.*/
	protected WebElement clickableWebElement = null;
	/**
	 * The property {@link #label} is text that is shown on the component.<br>
	 * If the text containing leading/ending spaces, then it will be trimmed and stored in this property.<br>
	 */
	protected String label = null;

	protected String iconURL = null;
	protected boolean disabled = false;
	protected boolean selected = false;
	protected boolean visible = false;

	protected Element(){}

	public Element(Object object){
		initialize(object);
	}

	/**
	 * set/update the class's fields through the underlying WebElement or AbstractMap.
	 */
	@Override
	public void updateFields(){
		super.updateFields();

		label = _getLabel();
		//Trim the label if it contains leading/ending spaces
		if(label!=null) label = StringUtils.TWhitespace(label);

		if(map!=null){
			id = getAttribute(PROPERTY_ID);
			cssClass = getAttribute(PROPERTY_CLASS);
			disabled = StringUtils.convertBool(getAttribute(PROPERTY_DISABLED));
			selected = StringUtils.convertBool(getAttribute(PROPERTY_SELECTED));
			visible = StringUtils.convertBool(getAttribute(PROPERTY_VISIBLE));
			if(!selected && cssClass!=null){
				//if the cssClass contains substring "selected", we will consider this item is selected
				selected = cssClass.toLowerCase().indexOf("selected")>0;
			}

		}else if(webelement!=null){
			try { disabled = !webelement.isEnabled(); } catch (Exception e) { /* IndependantLog.debug(debugmsg+StringUtils.debugmsg(e));*/}
			try { selected = webelement.isSelected(); } catch (Exception e) { /* IndependantLog.debug(debugmsg+StringUtils.debugmsg(e));*/}
			try { visible = webelement.isDisplayed(); } catch (Exception e) { /* IndependantLog.debug(debugmsg+StringUtils.debugmsg(e));*/}
		}
	}

	/**
	 * It is used to retrieve the text value shown on the web component from the embedded object.<br>
	 */
	protected String _getLabel(){
		String text = null;
		try{
			if(map!=null) text = getAttribute(PROPERTY_LABEL);
			else if(webelement!=null){
				text = parseWebElementText(webelement);
			}
		}catch(Throwable ignore){}
		return text;
	}

	public static String parseWebElementText(WebElement aWebElement){
		if(aWebElement==null) return null;
		String text = aWebElement.getText();
		if(text==null || text.trim().isEmpty()){
			text = aWebElement.getAttribute(PROPERTY_TEXTCONTENT);
		}
		return text;
	}

	/**
	 * This method tries to return a precise WebElement to click at. Sometimes the WebElement<br>
	 * containning the informations about 'label', 'disabled', 'selected' etc. may not be suitable<br>
	 * for clicking, it may occupy a larger area on screen and we need to adjust this method to get<br>
	 * an appropriate WebElement occupying correct area for clicking.<br>
	 * Refer to {@link HierarchicalElement#getClickableWebElement()} for a detail implementation.<br>
	 * <b>NOTE:If you need to verify the status of this Element, please call {@link #getWebElement()} instead.</b><br>
	 *
	 * For example:<br>
	 * For some implementations of tree, the tree node is implemented by &lt;li>, but this tag<br>
	 * may very probably contain some child-nodes, in this case the area returned by Selenium for<br>
	 * this tree node will be larger than it looks like, the area will include all its children.<br>
	 * But for clicking, we will click the center of the tree node (area returned by Selenium), so it<br>
	 * the center of that larger area that we click(not the center of the tree node itself), it's wrong!<br>
	 *
	 * @param webelement Element, the element to get WebElement
	 * @return WebElement
	 * @see HierarchicalElement#getClickableWebElement()
	 */
	protected WebElement getClickableWebElement(){
		//Maybe we just need to move the implementation in HierarchicalElement to here.
		clickableWebElement = getWebElement();
		if(clickableWebElement!=null && Boolean.parseBoolean(System.getProperty(SeleniumConfigConstant.PROPERTY_WAIT_READY))){
			WebDriverWait wait = new WebDriverWait(WDLibrary.getWebDriver(), Processor.getSecsWaitForComponent());
			clickableWebElement = wait.until(ExpectedConditions.elementToBeClickable(clickableWebElement));
		}

		return clickableWebElement;
	}

	public String getIconURL(){
		return iconURL;
	}

	public String getLabel(){
		return label;
	}
	public boolean isDisabled(){
		return disabled;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	/**
	 * @return boolean if the element is selected.
	 */
	public boolean isSelected() {
		return selected;
	}
	/**
	 * @return boolean if the element is visible.
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @return String, the content of this Element (the value of label).
	 */
	public String contentValue(){
		return label;
	}

	@Override
	public String toString(){
		return "id="+getId()+"; label="+label;
	}

	/**
	 * If ID equals, then 2 Items are considered as identical. Otherwise, JUST log a warning message.<br>
	 * If Label and TagName equal, then 2 Items will be consider as identical.
	 */
	@Override
	public boolean equals(Object node){
		if(node==null) return false;
		if(!(node instanceof Element)) return false;
		Element elementNode = (Element) node;

		//If id is available to compare
		if(id!=null && !id.isEmpty()){
			if(id.equals(elementNode.getId())) return true;
			else{
				//Once the item is clicked or selected, some application will redraw the page and change the item's ID!!!
				//But they are still the same item, so we just log a warning message instead of returning false.
				IndependantLog.warn(StringUtils.debugmsg(false)+"ID does NOT equal! '"+id+"' != '"+elementNode.getId()+"'");
			}
		}

		//If id is not available or NOT equivalent, we use the tagName and label to check
		//"css" is not suitable, after selection the "css" will VERY probably change.
		boolean ok = true;
		if(label!=null && !label.isEmpty()) ok &= label.equals(elementNode.getLabel());
		if(ok && tagName!=null && !tagName.isEmpty()) ok &= tagName.equals(elementNode.getTagName());

		return ok;
	}
}
