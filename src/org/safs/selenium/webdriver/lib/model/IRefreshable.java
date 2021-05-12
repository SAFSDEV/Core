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
package org.safs.selenium.webdriver.lib.model;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

/**
 * <pre>
 * According to a 'Recognition String', we can get a WebElement through Selenium.
 * This WebElement represents a control on the web application, and it can be used
 * to manipulate the control by Selenium. But sometimes, this WebElement will
 * become stale (calling a javascript function on the same control) and cannot
 * be used anymore. At this situation, the WebElement needs to be refreshed.
 * </pre>
 *
 * <br>
 * History:<br>
 *
 *  <br>   Jun 12, 2014    (Lei Wang) Initial release.
 */
public interface IRefreshable {
	/**get the WebElement's ID.*/
	public String getId();
	/**get the WebElement's Tag Name.*/
	public String getTagName();
	/**get the WebElement's CSS Class.*/
	public String getCssClass();

	/**
	 * get the SearchContext, used during refresh of WebElement.<br>
	 * If it is null, the search of WebElement will be processed through WebDriver,<br>
	 * which means to search on the whole web page.<br>
	 */
	public SearchContext getSearchContext();
	/**
	 * get the possible recognition strings, they will be tried one by one until<br>
	 * a valid WebElement is got.<br>
	 */
	public String[] getPossibleRecognitionStrings();

	/**get the embedded WebElement object.*/
	public WebElement getWebElement();
	/**set the embedded WebElement object.*/
	public void setWebElement(WebElement webelement);

	/**
	 * Refresh the embedded WebElement object.
	 * A stale element reference exception is thrown in one of two cases, the first being more common than the second:<br>
	 * 1. The element has been deleted entirely.<br>
	 *    The most frequent cause of this is that page that the element was part of has been refreshed,<br>
	 *    or the user has navigated away to another page. A less common, but still common cause is <br>
	 *    where a JS library has deleted an element and replaced it with one with the same ID or attributes. <br>
	 *    In this case, although the replacement elements may look identical they are different; <br>
	 *    the driver has no way to determine that the replacements are actually what's expected.<br>
	 *
	 * 2. The element is no longer attached to the DOM.<br>
	 *
	 * <br>
	 * If some native javascript APIs are called, the web element may become stale. In this case, this <br>
	 * method needs to be called.<br>
	 *
	 * @param checkStale boolean, if true then check if the element is stale or not before refresh;
	 *                            ohterwise, then refresh the element directly.
	 *                            The check will spend some time, if you don't want to waste time, use false.
	 * @return boolean, true if the refresh succeed.
	 */
	public boolean refresh(boolean checkStale);
}
