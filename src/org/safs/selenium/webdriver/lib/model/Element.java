/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * History:
 * 
 *  May 30, 2014    (sbjlwa) Initial release.
 *  Oct 15, 2014    (sbjlwa) Add 'visible' property.
 *  Oct 29, 2015    (sbjlwa) Modify updateFields(): Trim the property 'label', the leading/ending spaces will be ignored.
 */
package org.safs.selenium.webdriver.lib.model;

import org.openqa.selenium.WebElement;
import org.safs.tools.stringutils.StringUtilities;

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
	/**'class' property returns the value of attribut 'class' of the specified DOM node.*/
	public static final String PROPERTY_CLASS	= "class";
	/**'visible' if the element is visible.*/
	public static final String PROPERTY_VISIBLE	= "visible";
	
	/**clickableWebElement represents the WebElement that is clickable. It may be different than 
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
	public void updateFields(){
		super.updateFields();

		label = _getLabel();
		//Trim the label if it contains leading/ending spaces
		if(label!=null) label = StringUtilities.TWhitespace(label);
		
		if(map!=null){
			id = getAttribute(PROPERTY_ID);
			cssClass = getAttribute(PROPERTY_CLASS);
			disabled = StringUtilities.convertBool(getAttribute(PROPERTY_DISABLED));
			selected = StringUtilities.convertBool(getAttribute(PROPERTY_SELECTED));
			visible = StringUtilities.convertBool(getAttribute(PROPERTY_VISIBLE));
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
		return getWebElement();
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
	
	public String toString(){
		return "id="+getId()+"; label="+label;
	}
	
	public boolean equals(Object node){
		if(node==null) return false;
		if(!(node instanceof Element)) return false;
		Element elementNode = (Element) node;
		
		//If id is availabe to compare
		if(id!=null && !id.isEmpty()){
			if(id.equals(elementNode.getId())) return true;
			else return false;
		}
		
		//If id is not available, then use a bunch of attributes to compare
		boolean ok = true;
		if(label!=null && !label.isEmpty()) ok &= label.equals(elementNode.getLabel());
		if(ok && cssClass!=null && !cssClass.isEmpty()) ok &= cssClass.equals(elementNode.getCssClass());
		if(ok && tagName!=null && !tagName.isEmpty()) ok &= tagName.equals(elementNode.getTagName());
		
		return ok;
	}
}
