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
 *
 * History:<br>
 * <pre>
 *  Apr 25, 2014    (Lei Wang) Initial release.
 *  May 29, 2014    (Lei Wang) Use single-click for selectItem, double-click for activateItem.
 *  Oct 15, 2014    (Lei Wang) Modify showOnPage(): move the code 'test visibility' to super class.
 *  Nov 05, 2014    (Lei Wang) Add waitAndVerifyItemSelected(): used to verify item selection.
 *  JUN 30, 2014    (Lei Wang) Modified clickElement(): do NOT throw SeleniumPlusException even webelement.isDisplayed() is false.
 *  JUL 18, 2017	(Lei Wang) Modified showOnPage(): use "Selenium inViewPort()" to scroll item into view if the browser is Edge.
 *                                                    In future, we may always use the "Selenium inViewPort()" firstly instead of "javascript scrollIntoView()."
 *  JUN 01, 2018	(Lei Wang) Modified clickElement(): Don't call isShowOnPage() before calling showOnPage().
 *                            Added overloaded method showOnPage(): check isShowOnPage() before trying to scroll element into view.
 *  JUN 12, 2018	(Lei Wang) Modified clickElement(): Restore the code and check WDLibrary.enableForceScroll and isShowOnPage() to decide if showOnPage() will be called.
 *                            Deleted the overloaded showOnPage() and restore the original showOnPage().
 *
 *  </pre>
 */
package org.safs.selenium.webdriver.lib.model;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.Component;
import org.safs.selenium.webdriver.lib.SelectBrowser;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;
import org.safs.tools.stringutils.StringUtilities;

/**
 * <pre>
 *  APR 25, 2014    (Lei Wang) Initial release.
 *  </pre>
 */
public abstract class AbstractSelectable extends EmbeddedObject implements ISelectable{

	protected int DEFAULT_WAIT_TIME = 200;//in milliseconds
	/**
	 * A cache, which stores a pair(WebElement, Element[])
	 */
	protected Map<WebElement, Element[]> contentObjects = new HashMap<WebElement, Element[]>();

	public AbstractSelectable(Component parent)throws SeleniumPlusException {
		super(parent);
	}

	/**
	 * Try to get the cached content according to the WebElement.<br>
	 * If no cached content can be found, then get the real-time content by calling {@link #getCacheableContent()},<br>
	 * and put the content in the cache and return it.<br>
	 * If subclass doesn't need this functionality, then just override this method.<br>
	 * If subclass needs this functionality, then override method {@link #getCacheableContent()}<br>
	 * @see #getCacheableContent()
	 */
	@Override
	public Element[] getContent() throws SeleniumPlusException{
		WebElement element = webelement();
		Element[] content = null;

		if(element==null) throw new SeleniumPlusException("WebElement is null, cannot get Content.");
		synchronized(contentObjects){
			if(!contentObjects.containsKey(element)){
				content = getCacheableContent();
				contentObjects.put(element, content);
			}else{
				content = contentObjects.get(element);
				//TODO, maybe we need to traverse the whole content to refresh each node's content.
				//But this may take a long time, if some nodes contain staled WebElement.
				//refreshContent(content);
				IndependantLog.debug("Get Content from cache.");
			}
		}
		return content;
	}

	/**
	 * Get the real-time content from WebElement.<br>
	 * For some components like Tree, Menu etc. it is time-consuming to retrieve content, <br>
	 * user may implement this method instead of {@link #getContent()} so that the retrived content<br>
	 * will be kept in cache, and {@link #getContent()} will use the content in the cache.<br>
	 * @see #getContent()
	 */
	protected Element[] getCacheableContent() throws SeleniumPlusException{
		throw new SeleniumPlusException("Not implemented yet.");
	}

	/**clear the cache 'contentObjects'.*/
	@Override
	public void clearCache(){
		synchronized(contentObjects){
			contentObjects.clear();
		}
	}

	/**
	 * Refresh the content, to get latest information from the application component.<br>
	 * @param content Element[], the content
	 */
	@SuppressWarnings("unused")
	private void refreshContent(Element[] content){
		if(content==null) return;
		for(Element element: content){
			element.refresh(true);
			if(element instanceof HierarchicalElement){
				refreshContent(((HierarchicalElement)element).getChildren());
			}
		}
	}

	@Override
	public void selectItem(TextMatchingCriterion criterion, boolean verify, Keys key, Point offset, int mouseButtonNumber) throws SeleniumPlusException {
		Element element = getMatchedElement(criterion);
		if(element.isDisabled()){
			String msg = "This element is disabled, it cannot be selected.";
			throw new SeleniumPlusException(msg);
		}
		clickElement(element, key, offset, mouseButtonNumber, 1);

		if(verify) waitAndVerifyItemSelected(element);
	}

	@Override
	public void activateItem(TextMatchingCriterion criterion, boolean verify, Keys key, Point offset) throws SeleniumPlusException {
		Element element = getMatchedElement(criterion);
		if(element.isDisabled()){
			String msg = "This element is disabled, it cannot be activated.";
			throw new SeleniumPlusException(msg);
		}
		clickElement(element, key, offset, WDLibrary.MOUSE_BUTTON_LEFT, 2);

		if(verify) waitAndVerifyItemSelected(element);
	}

	@Override
	public void verifyContains(TextMatchingCriterion criterion) throws SeleniumPlusException{
		Element element = null;
		try{
			element = getMatchedElement(criterion);
		}catch(SeleniumPlusException se){
			//If we get this SeleniumPlusException, which means we cannot get the matched element.
			element = null;
		}

		if(element==null){
			String msg = "verification error: does not contain element '"+criterion.getText()+"'";
			throw new SeleniumPlusException(msg, SeleniumPlusException.CODE_VERIFICATION_FAIL);
		}
	}

	@Override
	public void verifyItemSelection(TextMatchingCriterion criterion, boolean expectSelected) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(false);

		if(TextMatchingCriterion.INDEX_TRY_ALL_MATCHED_ITEMS==criterion.getExpectedMatchedIndex()){
			boolean verified = false;
			int index = 0;
			Element element = null;
			while(!verified){
				try{
					criterion.setExpectedMatchedIndex(index++);
					element = getMatchedElement(criterion);
					try{
						verifyItemSelection(element, expectSelected);
						verified = true;
					}catch(SeleniumPlusException ignore){}
				}catch(SeleniumPlusException e){
					//If no more matched element can be found, break the loop
					IndependantLog.warn(debugmsg+" cannot find "+index+"th matched element '"+criterion.getText()+"'");
					break;
				}
			}
			if(!verified){
				String msg = "verification error: element='"+criterion.getText()+"' is "+(expectSelected?"unselected":"selected");
				throw new SeleniumPlusException(msg, SeleniumPlusException.CODE_VERIFICATION_FAIL);
			}
		}else{
			Element element = getMatchedElement(criterion);
			verifyItemSelection(element, expectSelected);
		}
	}
	/**
	 *
	 * @param element	Element, the element to check.
	 * @param expectSelected boolean, true if the element is expected 'selected'; false if expected 'unselected'.
	 * @throws SeleniumPlusException if the verification fails.
	 */
	protected void verifyItemSelection(Element element, boolean expectSelected) throws SeleniumPlusException{
		WDLibrary.checkNotNull(element);
		if(element.isDisabled()){
			String msg = "This element is disabled, it cannot be selected.";
			throw new SeleniumPlusException(msg);
		}
		boolean verificationOK = false;

		try{
//			waitElementReady(element);
//			verifyItemSelected(element);
			waitAndVerifyItemSelected(element);
			verificationOK = expectSelected;
		}catch(SeleniumPlusException spe){
			if(spe.getCode().equals(SeleniumPlusException.CODE_VERIFICATION_FAIL)){
				verificationOK = !expectSelected;
			}else{
				throw spe;
			}
		}

		if(!verificationOK){
			String msg = "verification error: element='"+element+"' is "+(expectSelected?"unselected":"selected");
			throw new SeleniumPlusException(msg, SeleniumPlusException.CODE_VERIFICATION_FAIL);
		}
	}

	/**
	 * Try to get the Selenium WebElement object from parameter 'element', then click that WebElement object.<br>
	 * After calling this method, the parent of Element may become stale. We may need to call {@link #refresh(boolean)}.<br>
	 * After calling this method, user may need to verify that element has been selected. Please call {@link #waitAndVerifyItemSelected(Element)}<br>
	 * instead of {@link #verifyItemSelected(Element)}, as the first method will also try the wait for element<br>
	 * to be ready and call {@link #refresh(boolean)}.<br>
	 *
	 * @param element Element, the element to select
	 * @param key org.openqa.selenium.Keys, it is the key to press during selection.<br>
	 *                  for example {@link Keys#SHIFT}, or {@link Keys#CONTROL}.<br>
	 *                  If user doesn't want any key pressed, please provide null.<br>
	 * @param offset Point, the position relative to the up-left corner to click at<br>
	 * @param mouseButtonNumber int, the mouse-button-number representing right, middle, or left button.
	 * 								 it can be {@link WDLibrary#MOUSE_BUTTON_LEFT} or {@link WDLibrary#MOUSE_BUTTON_RIGHT}<br>
	 * 								 or {@link WDLibrary#MOUSE_BUTTON_MIDDLE}.
	 * @param numberOfClick int, 1 means single-click, 2 means double-click
	 * @throws SeleniumPlusException
	 * @see {@link #refresh(boolean)}
	 * @see #waitAndVerifyItemSelected(Element)
	 */
	protected void clickElement(Element element, Keys key, Point offset, int mouseButtonNumber, int numberOfClick) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(false);
		WDLibrary.checkNotNull(element);

		try {
			//If the elementElement is not visible on the page, click on it will throw exception
			//if(!element.isDisplayed()) showOnPage(element);
			//sometimes elementElement.isDisplayed() will return true even if the element is not really shown,
			//for example "node in Tree" out of scroll area, so we always to call showOnPage() to make sure the item is shown.
			//The method isShowOnPage() is not so reliable. Sometimes it returns true even the element is still hidden.
			//At this situation, we need to set WDLibrary.enableForceScroll to true to force the scroll into view.
			//check WDLibrary.enableForceScroll and isShowOnPage() to decide if showOnPage() will be called
			if(WDLibrary.enableForceScroll || !isShowOnPage(element)) showOnPage(element);

			WebElement webelement = element.getClickableWebElement();
			//S1352432: isDisplayed() is NOT stable, it return true on Chrome, but false on Edge.
			//here we will NOT check it anymore, in the WDLibrary.click()/doubleClick(), it (isDisplayed()) will still be checked, but no Exception will be thrown out and
			//a warning message will be written into debug log.
			//if(!webelement.isDisplayed()) throw new SeleniumPlusException("not visible element.");

			if(numberOfClick==1){
				WDLibrary.click(webelement, offset, key, mouseButtonNumber);
			}else if(numberOfClick==2){
				WDLibrary.doubleClick(webelement, offset, key, mouseButtonNumber);
			}

		} catch(Exception e) {
			IndependantLog.error(debugmsg+" Met exception.",e);
			throw new SeleniumPlusException("Fail to select element by id '"+element.getId()+"'. due to '"+e.getMessage()+"'");
		}
	}

	/**
	 * Make the element is inside the container, and visible on the page.<br>
	 * For example, some element may not be visible if there are too many elements in its container;<br>
	 * User can scroll to make the element visible on the page.<br>
	 * User may need to wait for a while so that the element related object can be updated (like position-onscreen, visibility etc.)<br><br>
	 * This method gives a default implementation:<br>
	 * it will scroll and align the top of element to the top of container.<br>
	 * Before this method is called, it is suggested to call {@link #isShowOnPage(Element)} firstly.<br>
	 * <font color="red">This method will be override by sub-class.</font>
	 * @param element Element, the element needs to be visible on page
	 * @throws SeleniumPlusException
	 * @see {@link #isShowOnPage(Element)}
	 */
	protected void showOnPage(Element element) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(false);
		WDLibrary.checkNotNull(element);
		boolean isEdgeBrowser = false;

		try{
			RemoteWebDriver wd = (RemoteWebDriver) WDLibrary.getWebDriver();
			isEdgeBrowser = wd.getCapabilities().getBrowserName().equalsIgnoreCase(SelectBrowser.BROWSER_NAME_EDGE);
		}catch(Exception e){
			IndependantLog.warn(debugmsg+"Ignore "+e.getMessage());
		}

		//The Javascript's scrollIntoView() will scroll too much!
		//For example, if a listview locates in a pop-up window and a list-item is not visible:
		//it will scroll the list-item to top/bottom of the whole browser, so the pop-up window will be moved too.
		//This will make the whole page looks very strange.
		//While the Selenium's Coordinates.inViewPort() will scroll the list-item according to the listview
		//This problem is firstly found in Edge browser, so for Edge we will use Selenium's Coordinates.inViewPort() in first place
		//to scroll item into view; then try javascript's code.
		//TODO In future we may always use Selenium's Coordinates.inViewPort() in first place if it is stable enough.
		if(isEdgeBrowser){
			try{
				//make the item visible in the container's viewport
				Coordinates coordinate = ((Locatable) element.getWebElement()).getCoordinates();
				coordinate.inViewPort();
			}catch(Exception e){
				String message = "Fail to show item '"+element.getLabel()+"' in browser's viewport, due to "+StringUtils.debugmsg(e);
				IndependantLog.warn(debugmsg+message);

				try {
					WDLibrary.alignToTop(element.getWebElement());
				} catch (Exception ex) {
					IndependantLog.warn(debugmsg+"Fail to scroll to item '"+element.getLabel()+"'. Met "+StringUtils.debugmsg(ex));
					throw new SeleniumPlusException(message);
				}
			}finally{
				refresh(false);
			}
		}else{
			try{
				WDLibrary.alignToTop(element.getWebElement());
			}catch(Exception e){
				String message = "Fail to scroll to item '"+element.getLabel()+"'. Met "+StringUtils.debugmsg(e);
				IndependantLog.warn(debugmsg+message);

				try {
					//make the item visible in the container's viewport
					Coordinates coordinate = ((Locatable) element.getWebElement()).getCoordinates();
					coordinate.inViewPort();
				} catch (Exception ex) {
					IndependantLog.warn(debugmsg+"Fail to show item '"+element.getLabel()+"' in browser's viewport, due to "+StringUtils.debugmsg(ex));
					throw new SeleniumPlusException(message);
				}
			}finally{
				refresh(false);
			}
		}
	}

	/**
	 * Wait for element ready, means that element's attribute has been updated.<br>
	 * This method just wait some time, subclass may give a more detail implementation.<br>
	 * When this method will be called?<br>
	 * For example, before calling {@link #verifyItemSelected(Element)}, because<br>
	 * {@link #verifyItemSelected(Element)} needs to check element's attributes.<br>
	 *
	 * @param element Element, the element needs to be ready
	 * @param milliseconds int, the time to wait
	 * @throws SeleniumPlusException
	 * @see {@link #verifyItemSelected(Element)}
	 */
	protected void waitElementReady(Element element, int milliseconds) throws SeleniumPlusException {
		StringUtilities.sleep(milliseconds);
	}
	/**
	 * Wait for element ready, means that element's attribute has been updated.<br>
	 * This method just wait 200 milliseconds, subclass may give a more detail implementation.<br>
	 *
	 * @param element Element, the element needs to be ready
	 * @throws SeleniumPlusException
	 */
	protected void waitElementReady(Element element) throws SeleniumPlusException {
		waitElementReady(element, DEFAULT_WAIT_TIME);
	}

	/**
	 * Wait Item ready, and verify the item has been selected.<br>
	 * Before verification, we need to refresh the parent if the page doesn't change.<br>
	 * @param element Element, the element has been selected or clicked or double-clicked.
	 * @throws SeleniumPlusException
	 * @see {@link #verifyItemSelected(Element)}
	 */
	protected void waitAndVerifyItemSelected(Element element) throws SeleniumPlusException {
		waitElementReady(element);
		//click() or doubleClick() may make the webelement (List, Menu, Tree) stale, refresh to get the latest webelement.
		//BUT before refreshing, we need to make sure the page has not been changed. sometimes, clicking an item
		//will cause the page change, and the original List/Menu will not be found anymore, searching them will
		//waste a lot of time and make the program hang.
		if(!WDLibrary.pageHasChanged()){
			refresh(false);
			StringUtilities.sleep(500);
			verifyItemSelected(element);
		}else{
			String errmsg = " Web page has changed. Original Element cannot be found. Cannot verify!";
			IndependantLog.warn(StringUtils.debugmsg(false)+errmsg);
			throw new SeleniumPlusException(errmsg);
		}
	}

	/**
	 * <b>Note:</b>In subclass, we need to update the 'selected' property of Element.<br>
	 * Please call {@link #waitAndVerifyItemSelected(Element)} instead to verify selection,<br>
	 * as the first method will also try the wait for element to be ready and call {@link #refresh(boolean)}.<br>
	 * @param element Element, the element to verify that it is selected.
	 * @throws SeleniumPlusException
	 */
	protected void verifyItemSelected(Element element) throws SeleniumPlusException {
		WDLibrary.checkNotNull(element);
		if(!element.isSelected()){
			String msg = "verification error: node '"+element.getLabel()+"' has not been selected.";
			throw new SeleniumPlusException(msg, SeleniumPlusException.CODE_VERIFICATION_FAIL);
		}
	}
}
