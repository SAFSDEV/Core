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
 * <br>
 * History:<br>
 *
 * APR 25, 2014    (Lei Wang) Initial release.
 * OCT 15, 2014    (Lei Wang) Add method isShowOnPage().
 * JUL 05, 2017    (Lei Wang) Modified isShowOnPage(): WebElement.isDisplayed() is not reliable, call WDLibrary.isShowOnPage() to check again.
 * JUL 19, 2017    (Lei Wang) Modified isShowOnPage(): Don't call WDLibrary.isShowOnPage() to check again, it sometimes will prevent
 *                                                   an item (within a container) from being scrolled into view.
 *
 */
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
 * @author Lei Wang 	APR 25, 2014 Initial release.
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
	@Override
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
	 * To check if an Element is visible on page. This method will check:<br>
	 * <ul>
	 * <li>If the element is considered as visible by Selenium
	 * <li>If the center of the element is in the container
	 * <li>If the item is shown in browser
	 * </ul>
	 * @param element Element, the element to check
	 * @return boolean, true if the element is visible on page.
	 * @throws SeleniumPlusException
	 */
	protected boolean isShowOnPage(Element element) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(false);
		WDLibrary.checkNotNull(element);
		boolean isShown = false;
		WebElement item = null;

		try{
			item = element.getWebElement();
			org.openqa.selenium.Dimension itemD = item.getSize();
			org.openqa.selenium.Point itemCenterOffset = new org.openqa.selenium.Point(itemD.width/2, itemD.height/2);
			org.openqa.selenium.Point itemLoc = item.getLocation();
			org.openqa.selenium.Point itemCenterLoc = itemLoc.moveBy(itemCenterOffset.x, itemCenterOffset.y);

			//Check if the item is considered displayed by Selenium
			//WebElement.isDisplayed() is not reliable, sometimes it returns false even the element is totally visible on the page.
			isShown = item.isDisplayed();
			IndependantLog.debug(debugmsg+"WebElement.isDisplayed() returned '"+isShown+"'. But it is NOT reliable, "
					+ "sometimes it returns false even the element is totally visible on the page.");

			//Check if the center of item is shown in the container
			if(isShown){
				WebElement container = webelement();
				//WebElement.getSize() will not return the correct size, it is larger in most time and the element will be considered as 'visible' wrongly.
				//The funny thing is that this api's document is "What is the width and height of the rendered element?".
				org.openqa.selenium.Dimension containerD = container.getSize();
				org.openqa.selenium.Point containerLoc = container.getLocation();
				org.openqa.selenium.Point itemCenterLocRelativeToContainer = itemCenterLoc.moveBy(-containerLoc.x, -containerLoc.y);
				isShown = WDLibrary.isLocationInBounds(itemCenterLocRelativeToContainer, containerD);
				IndependantLog.debug(debugmsg+"Check if item center (relative to container) '"+itemCenterLocRelativeToContainer+"' is inside container '"+containerD+"', it is "+isShown+".");
			}

			//Check if the item is shown in browser
			if(isShown){
				isShown = WDLibrary.isShowOnPage(item, itemCenterOffset);
				IndependantLog.debug(debugmsg+"Chech if item is shown on page, it is "+isShown+".");
			}

		}catch(Exception e){
			IndependantLog.error(debugmsg+"Met "+StringUtils.debugmsg(e));
		}

		return isShown;
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
