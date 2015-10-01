/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.model;

import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.Component;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;

/**
 * This class represents an object to be included in the org.safs.selenium.webdriver.lib.Component<br>
 * or its subclass. This class provides an ability to detect whether the webelement is supported for<br>
 * a certain Component (Tree, ListView) whatever the domain (DOJO, SAP, HTML) this webelement belongs to.<br>
 * Subclass needs to provide the method {@link #getSupportedClassNames()}.<br>
 * 
 * <br>
 * History:<br>
 * 
 *  <br>   Apr 25, 2014    (Lei Wang) Initial release.
 *  <br>   Oct 15, 2014    (Lei Wang) Add method isShowOnPage().
 */
public abstract class EmbeddedObject implements Supportable{
	/**
	 * The Component object who is the parent of this embedded object.
	 */
	protected Component parent = null;
	
	public EmbeddedObject(Component parent) throws SeleniumPlusException{
		if(parent==null){
			String msg = "The parent is null, cannot create the embedded object.";
			IndependantLog.error(msg);
			throw new SeleniumPlusException(msg, SeleniumPlusException.CODE_OBJECT_IS_NULL);
		}
		if(!isSupported(parent.getWebElement())){
			String msg = "WebElement <"+parent.getTagName()+" class='"+parent.getCssClass()+"'> is not an approperiate type or is not supported yet.";
			IndependantLog.error(msg);
			throw new SeleniumPlusException(msg, SeleniumPlusException.CODE_TYPE_IS_WRONG);
		}
		this.parent = parent;
	}
	
	/**
	 * Test if the element is supported. Handle different domains such as SAP, DOJO, HTML etc.
	 */
	public boolean isSupported(WebElement element){
		boolean rc = false;
		try {
			if(WDLibrary.isDojoDomain(element)){
				rc = WDLibrary.DOJO.isSupported(element, getSupportedClassNames());
			} 
			if(!rc && WDLibrary.isSAPDomain(element)){
				rc = WDLibrary.SAP.isSupported(element, getSupportedClassNames());
			}
			if(!rc){
				rc = WDLibrary.HTML.isSupported(element, getSupportedClassNames());
			}
		} catch (SeleniumPlusException e) {
			IndependantLog.error("not supported due to "+StringUtils.debugmsg(e));
		}
		return rc;
	}
	
	/**
	 * If some native javascript APIs are called, the parent's under-lying web element may become stale. <br>
	 * In this case, this refresh() method needs to be called.<br>
	 * Normally before calling {@link #webelement()} to get a webelement, you may call this {@link #refresh(boolean)}.<br>
	 * 
	 * @param checkStale boolean, if true then check if the element is stale or not before refresh;
	 *                            ohterwise, then refresh the element directly.
	 *                            The check will spend some time, if you don't want to waste time, use false.
	 * @see #webelement()
	 * @return boolean, true if the refresh succeed
	 */
	public boolean refresh(boolean checkStale){
		return parent.refresh(checkStale);
	}
	
	/**
	 * To check if an Element is visible on page.<br>
	 * If the center of the element is in the container and is shown in the browser's client area, the<br>
	 * element will be considered as visible on page.<br>
	 * @param element Element, the element to check
	 * @return boolean, true if the element is visible on page.
	 * @throws SeleniumPlusException
	 */
	protected boolean isShowOnPage(Element element) throws SeleniumPlusException {
		WDLibrary.checkNotNull(element);

		try{
			WebElement item = element.getWebElement();
			org.openqa.selenium.Dimension itemD = item.getSize();
			org.openqa.selenium.Point itemCenterOffset = new org.openqa.selenium.Point(itemD.width/2, itemD.height/2);
			org.openqa.selenium.Point itemLoc = item.getLocation();
			org.openqa.selenium.Point itemCenterLoc = itemLoc.moveBy(itemCenterOffset.x, itemCenterOffset.y);
			
			WebElement container = webelement();
			org.openqa.selenium.Dimension containerD = container.getSize();
			org.openqa.selenium.Point containerLoc = container.getLocation();
			org.openqa.selenium.Point itemCenterLocRelativeToContainer = itemCenterLoc.moveBy(-containerLoc.x, -containerLoc.y);
			
			if( item.isDisplayed() //the item is considered displayed by Selenium
					&& WDLibrary.isLocationInBounds(itemCenterLocRelativeToContainer, containerD) //the center of item is shown in the container
					&& WDLibrary.isShowOnPage(item, itemCenterOffset) //the center of the item is shown in browser
					){
				return true;
			}
		}catch(Exception e){
			IndependantLog.error(StringUtils.debugmsg(false)+"Met "+StringUtils.debugmsg(e));
		}
		
		return false;
	}
	
	public WebElement webelement(){
		try{
			return parent.getWebElement();
		}catch(Throwable th){
			IndependantLog.error("Fail to get web element.", th);
			return null;
		}
	}
}
