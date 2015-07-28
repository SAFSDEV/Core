/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.safs.GuiObjectRecognition;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.RS;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;

/**
 * This class represents a hierarchical structure like Tree or Menu etc.<br>
 * 
 * <br>
 * History:<br>
 * 
 *  <br>   Jun 23, 2014    (sbjlwa) Initial release.
 */
public class HierarchicalElement extends Element{
	/**'children' */
	public static final String PROPERTY_CHILDREN 	= "children";
	/**'parent'*/
	public static final String PROPERTY_PARENT	 	= "parent";

	protected HierarchicalElement parent = null;
	protected HierarchicalElement[] children = null;
	private Object lock = new Object();
	
	protected HierarchicalElement(){}
	
	public HierarchicalElement(Object object){ initialize(object); }
	
	/**
	 * set/update the class's fields through the underlying WebElement or AbstractMap.
	 */
	public void updateFields(){
		super.updateFields();

		if(map!=null){
			parseChildren(map);

		}else if(webelement!=null){
			//comment parseChildren(), as we don't know how to get children from webelement
//			parseChildren(webelement);
			
		}
	}
	
	protected void parseChildren(WebElement element){
		String debugmsg = StringUtils.debugmsg(getClass(), "parseChildren");
		IndependantLog.debug(debugmsg+" Need to handle WebElement to get children.");
		//TODO But how to get children from a web-element???
	}
	
	/**
	 * parse the children of this element. To create the CORRECT child element, subclass<br>
	 * MUST OVERRIDE {@link #newArray(int)}/{@link #newInstance(Object)} to provide their own instance/array.<br>
	 * @param map Map, contains 'children' field.
	 * @see #newArray(int)
	 * @see #newInstance(Object)
	 */
	protected void parseChildren(Map<?, ?> map){
		String debugmsg = StringUtils.debugmsg(getClass(), "parseChildren");
		
		//analyze the 'children' field
		try {
			Object children = map.get(PROPERTY_CHILDREN);//List
			if(children instanceof List){
				Object[] objects = ((List<?>) children).toArray();
				HierarchicalElement[] childrenNodes = newArray(objects.length);
				for(int i=0;i<objects.length;i++){
					childrenNodes[i] = newInstance(objects[i]);
					childrenNodes[i].parent = this;
				}
				this.children = childrenNodes;
			}else{
				if(children!=null) IndependantLog.error(debugmsg+" Need to handle "+children.getClass().getName());
			}
		} catch (Exception e) { IndependantLog.error(debugmsg+StringUtils.debugmsg(e));}
		
	}
	
	/**
	 * Create an instance of HierarchicalElement.<br>
	 * Sub class MUST OVERRIDE this method to provide its own instance.<br>
	 * @param ojbect Object, to create an instance of this class.
	 * @return HierarchicalElement, an instance of HierarchicalElement
	 */
	protected HierarchicalElement newInstance(Object ojbect){
		return new HierarchicalElement(object);
	}
	/**
	 * Create an array of HierarchicalElement.<br>
	 * <b><font color='red'>Sub class MUST OVERRIDE this method to provide its own array.</font></b><br>
	 * @param length int, the length of the array
	 * @return an array of HierarchicalElement(or subclass).
	 */
	protected HierarchicalElement[] newArray(int length){
		return new HierarchicalElement[length];
	}
	
	public HierarchicalElement[] getChildren() {
		return children;
	}
	public void setChildren(HierarchicalElement[] children) throws SeleniumPlusException{
		synchronized(lock){
			this.children = children;
		}
	}
	public void addChild(HierarchicalElement[] childs){
		synchronized(lock){
			if(children==null){
				children = newArray(childs.length);
				children = Arrays.copyOf(childs, childs.length);
			}else{
				children = Arrays.copyOf(children, children.length+childs.length);
				int j = children.length-1;
				for(int i=childs.length-1;i>=0;i--){
					children[j--] = childs[i];
				}
			}
		}
	}
	public void addChild(HierarchicalElement child){
		synchronized(lock){
			if(children==null){
				children = newArray(1);
				children[0] = child;
			}else{
				children = Arrays.copyOf(children, children.length+1);
				children[children.length-1] = child;
			}
		}
	}
	public void reverseChildren(){
		synchronized(lock){
			StringUtils.reverseArray(children);
		}
	}

	public HierarchicalElement getParent() {
		return parent;
	}
	public void setParent(HierarchicalElement parent) {
		this.parent = parent;
	}
	
	/**
	 * Return the root's id (the root of this hierarchical structure like Tree or Menu)
	 * @return String, the root's id.
	 */
	public String getRootId(){
		String rootId = null;
		HierarchicalElement node = getParent();
		while(node!=null){
			rootId = node.getId();
			node = node.getParent();
		}
		return rootId;
	}
	
	/**
	 * Return the full path of this node in the hierarchical structure.
	 * @return String, the full path.
	 */
	public String getFullPath(){
		String fullPath = getLabel();
		
		HierarchicalElement node = getParent();
		while(node!=null){
			fullPath = node.getLabel()+GuiObjectRecognition.DEFAULT_PATH_SEPARATOR+fullPath;
			node = node.getParent();
		}
		
		return fullPath;
	}
	
	/**
	 * In this method, we are trying to get a WebElement occupying the precise area of the node.<br>
	 * The node's text is often represented by a tag &lt;span>, which is what we want, we will<br>
	 * try to get it by the node's text.<br>
	 * @return WebElement
	 */
	public WebElement getClickableWebElement(){
		String debugmsg = StringUtils.debugmsg(getClass(), "getClickableWebElement");
		if(clickableWebElement==null){
			try{
				super.getWebElement();
				clickableWebElement = webelement.findElement(By.xpath(RS.XPATH.fromText(label, false, true)));
			}catch(Exception e){
				IndependantLog.warn(debugmsg+"Fail to get the clickable WebElement object due to"+StringUtils.debugmsg(e));
				clickableWebElement = webelement;
			}
		}
		return clickableWebElement;
	}
}
