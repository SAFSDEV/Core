/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.model;

import java.awt.Point;

import org.openqa.selenium.Keys;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;

/**
 * 
 * History:<br>
 * 
 * <pre>
 *  Apr 25, 2014    (Lei Wang) Initial release.
 *  May 05, 2014    (Lei Wang) Modify methods selectItem and verifyItemSelection to accept one more parameter 'matchIndex'.
 *  May 05, 2014    (Lei Wang) Modify methods selectItem to accept one more parameter 'key'.
 *  May 29, 2014    (Lei Wang) Modify methods selectItem to accept more parameter 'offset' and 'mouseButtonNumber'.
 *                           Add methods activateItem to double click item.
 * </pre>
 */
public interface ISelectable extends IOperable{
	
	/**
	 * Try to select (single-click) the item according to the item's text (fully or partially given), 
	 * and then verify if the item has been really selected according to the parameter 'verify'.
	 * If the parameter key is provided, the selection will happen with that key pressed at the same time.
	 * If the parameter offset is provided, then click at offset relative to the top-left corner of view; otherwise at the center.
	 * The parameter mouseButtonNumber decides right-click or left-click.
	 * <br>
	 * <b>
	 * Note: There are maybe some callback methods associated with the item-container, but setting
	 * select text MAY NOT invoke them, in the implementation of this method we
	 * should make sure those callbacks are invoked (we can fire some events.)
	 * </b>
	 * @param criterion TextMatchingCriterion, containing text, partialMatch, matchedIndex as search-criterion.
	 * @param verify boolean, if true then verify the selection;
	 * @param key org.openqa.selenium.Keys, it is the key to press during selection.<br>
	 *                  for example {@link Keys#SHIFT}, or {@link Keys#CONTROL}.<br>
	 *                  If user doesn't want any key pressed, please provide null<br>
	 * @param offset Point, the position relative to the up-left corner to click at<br>
	 *                      If user provides null, then the center of the item will be used<br>
	 * @param mouseButtonNumber int, the mouse-button-number representing right, middle, or left button.<br>
	 * 								 it can be {@link WDLibrary#MOUSE_BUTTON_LEFT} or {@link WDLibrary#MOUSE_BUTTON_RIGHT}<br>
	 * 								 or {@link WDLibrary#MOUSE_BUTTON_MIDDLE}.<br>
	 * @throws SeleniumPlusException
	 */
	public void selectItem(TextMatchingCriterion criterion, boolean verify, Keys key, Point offset, int mouseButtonNumber) throws SeleniumPlusException;
	/**
	 * Try to select (single-click) the item according to the index, and then verify if the item 
	 * has been really selected according to the parameter 'verify'.
	 * If the parameter key is provided, the selection will happen with that key pressed at the same time.
	 * If the parameter offset is provided, then click at offset relative to the top-left corner of view; otherwise at the center.
	 * The parameter mouseButtonNumber decides right-click or left-click.
	 * <br>
	 * <b>
	 * Note: There are maybe some callback methods associated with the item-container, but setting
	 * select index MAY NOT invoke them, in the implementation of this method we
	 * should make sure those callbacks are invoked (we can fire some events.)
	 * </b>
	 * @param index int, the index of the item to select, it is 0-based index.
	 * @param verify boolean, if true then verify the selection;
	 * @param key org.openqa.selenium.Keys, it is the key to press during selection.<br>
	 *                  for example {@link Keys#SHIFT}, or {@link Keys#CONTROL}.<br>
	 *                  If user doesn't want any key pressed, please provide null<br>
	 * @param offset Point, the position relative to the up-left corner to click at<br>
	 *                      If user provides null, then the center of the item will be used<br>
	 * @param mouseButtonNumber int, the mouse-button-number representing right, middle, or left button.<br>
	 * 								 it can be {@link WDLibrary#MOUSE_BUTTON_LEFT} or {@link WDLibrary#MOUSE_BUTTON_RIGHT}<br>
	 * 								 or {@link WDLibrary#MOUSE_BUTTON_MIDDLE}.<br>
	 * @throws SeleniumPlusException
	 */
	public void selectItem(int index, boolean verify, Keys key, Point offset, int mouseButtonNumber) throws SeleniumPlusException;
	/**
	 * Try to activate (double-click) the item according to the item's text (fully or partially given), 
	 * and then verify if the item has been really selected according to the parameter 'verify'.
	 * If the parameter key is provided, the selection will happen with that key pressed at the same time.
	 * <br>
	 * @param criterion TextMatchingCriterion, containing text, partialMatch, matchedIndex as search-criterion.
	 * @param verify boolean, if true then verify the selection;
	 * @param key org.openqa.selenium.Keys, it is the key to press during selection.<br>
	 *                  for example {@link Keys#SHIFT}, or {@link Keys#CONTROL}.<br>
	 *                  If user doesn't want any key pressed, please provide null<br>
	 * @param offset Point, the position relative to the up-left corner to click at<br>
	 *                      If user provides null, then the center of the item will be used<br>
	 * @throws SeleniumPlusException
	 */
	public void activateItem(TextMatchingCriterion criterion, boolean verify, Keys key, Point offset) throws SeleniumPlusException;
	/**
	 * Try to activate (double-click) the item according to the index, and then verify if the item 
	 * has been really selected according to the parameter 'verify'.
	 * If the parameter key is provided, the selection will happen with that key pressed at the same time.
	 * <br>
	 * @param index int, the index of the item to select, it is 0-based index.
	 * @param verify boolean, if true then verify the selection;
	 * @param key org.openqa.selenium.Keys, it is the key to press during selection.<br>
	 *                  for example {@link Keys#SHIFT}, or {@link Keys#CONTROL}.<br>
	 *                  If user doesn't want any key pressed, please provide null<br>
	 * @param offset Point, the position relative to the up-left corner to click at<br>
	 *                      If user provides null, then the center of the item will be used<br>
	 * @throws SeleniumPlusException
	 */
	public void activateItem(int index, boolean verify, Keys key, Point offset) throws SeleniumPlusException;

	/**
	 * Verify the item (specified by text, partialMatch and matchIndex) is selected or un-selected.
	 * @param criterion TextMatchingCriterion, containing text, partialMatch, matchedIndex as search-criterion.
	 * @param expectSelected boolean, true if the item is expected 'selected'; false if expected 'unselected'.
	 * @throws SeleniumPlusException if the verification fails.
	 */
	public void verifyItemSelection(TextMatchingCriterion criterion, boolean expectSelected) throws SeleniumPlusException;
	/**
	 * Verify the item (specified by index) is selected or un-selected.
	 * @param index int, the index of the item to select, it is 0-based index.
	 * @param expectSelected boolean, true if the item is expected 'selected'; false if expected 'unselected'.
	 * @throws SeleniumPlusException if the verification fails.
	 */
	public void verifyItemSelection(int index, boolean expectSelected) throws SeleniumPlusException;
	/**
	 * Verify the item (specified by text, partialMatch and matchIndex) is contained.
	 * @param criterion TextMatchingCriterion, containing text, partialMatch, matchedIndex as search-criterion.
	 * @throws SeleniumPlusException if the verification fails.
	 */
	public void verifyContains(TextMatchingCriterion criterion) throws SeleniumPlusException;
	
	/**
	 * according to the element's label to get a Element object.
	 * @param criterion TextMatchingCriterion, containing text, partialMatch, matchedIndex as search-criterion.
	 * @return Element, the matched Element object.
	 * @throws SeleniumPlusException if Element cannot be found
	 */
	public Element getMatchedElement(TextMatchingCriterion criterion) throws SeleniumPlusException;

	/**
	 * Get all elements.
	 * @return Element[] an array of the element
	 * @throws SeleniumPlusException
	 */
	public Element[] getContent() throws SeleniumPlusException;
}
