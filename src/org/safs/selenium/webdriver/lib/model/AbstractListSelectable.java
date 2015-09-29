/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.model;

import java.awt.Point;

import org.openqa.selenium.Keys;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.Component;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;
import org.safs.selenium.webdriver.lib.WDLibrary;

/**
 * 
 * History:<br>
 * 
 *  <br>   May 30, 2014    (Lei Wang) Initial release.
 *  <br>   Oct 15, 2014    (Lei Wang) Add isShowOnPage(): override the superclass method, just return false.
 *  <br>   Oct 29, 2014    (Lei Wang) Modify getMatchedElement(): if TextMatchingCriterion only contains 'index', 
 *                                                              no text to match, get by index.
 *  <br>   Mar 27, 2015    (Lei Wang) Remove isShowOnPage(): the method in superclass has been fixed.
 */
abstract public class AbstractListSelectable extends AbstractSelectable implements IListSelectable{

	public AbstractListSelectable(Component parent)throws SeleniumPlusException {
		super(parent);
	}

	public Item[] getContent() throws SeleniumPlusException{
		try{
			return (Item[]) super.getContent();
		}catch(SeleniumPlusException se){
			throw se;
		}catch(Exception e){
			String msg = "Fail to convert content to Item[].";
			IndependantLog.error(msg, e);
			throw new SeleniumPlusException(msg);
		}
	}

	public Item getMatchedElement(TextMatchingCriterion criterion) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(this.getClass(), "getMatchedElement(String, boolean, int)");
		Item matchedItem = null;

		try{
			int matchIndex = criterion.getExpectedMatchedIndex();
			if(criterion.getText()!=null){
				Item[] items = getContent();
				if(items==null || items.length==0){
					IndependantLog.error(debugmsg+" ====== Did NOT get any elements from container.");
				}else{
					IndependantLog.info(debugmsg+" processing "+ items.length +" items.");
				}
				int matchedIndex = 0;
				Item item = null;
				String label = null;
				for(int i=0;i<items.length;i++){
					item = items[i];
					label = item.getLabel();
					if(criterion.matchText(label)){
						if(matchedIndex++ == matchIndex){
							matchedItem = item;
							IndependantLog.info(debugmsg+" matched item '"+ label +"' at child index "+ i);
							break;
						}
					}
				}
			}else{//criterion.getText()==null, only index is valid for searching
				matchedItem = getMatchedItem(matchIndex);
			}			
		} catch (SeleniumPlusException e) {
			IndependantLog.error(debugmsg+"Cannot get elements from container.", e);
		}

		if(matchedItem==null){
			IndependantLog.error(debugmsg+"Failed to find matching element "+criterion.toString());
			throw new SeleniumPlusException("Failed to find matching element '"+criterion.getText()+"'.");
		}
		return matchedItem;
	}

	/**
	 * according to the index to get a Item object.
	 * @param index int, the index of the item.
	 * @return Item, the matched Item object.
	 * @throws SeleniumPlusException
	 */
	protected Item getMatchedItem(int index) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(this.getClass(), "getMatchedItem(int)");
		Item matchedItem = null;
		try {
			Item[] items = getContent();
			for(Item item: items){
				if(item.getIndex()==index){
					matchedItem=item;
					break;
				}
			}
		} catch (SeleniumPlusException e) {
			IndependantLog.error(debugmsg+"Cannot get items from container.", e);
		}

		if(matchedItem==null){
			IndependantLog.error(debugmsg+"Fail to find item by index '"+index+"'");
			throw new SeleniumPlusException("Fail to find item by index '"+index+"'");
		}

		return matchedItem;
	}

	public void selectItem(int index, boolean verify, Keys key, Point offset, int mouseButtonNumber) throws SeleniumPlusException {
		Item item = getMatchedItem(index);
		if(item.isDisabled()){
			String msg = "This item is disabled, it cannot be selected.";
			throw new SeleniumPlusException(msg);
		}
		clickElement(item, key, offset, mouseButtonNumber, 1);

		if(verify) waitAndVerifyItemSelected(item);
	}

	public void activateItem(int index, boolean verify, Keys key, Point offset) throws SeleniumPlusException {
		Item item = getMatchedItem(index);
		if(item.isDisabled()){
			String msg = "This item is disabled, it cannot be activated.";
			throw new SeleniumPlusException(msg);
		}
		clickElement(item, key, offset, WDLibrary.MOUSE_BUTTON_LEFT, 2);

		if(verify) waitAndVerifyItemSelected(item);
	}

	public void verifyItemSelection(int index, boolean expectSelected) throws SeleniumPlusException {
		Item item = getMatchedItem(index);
		verifyItemSelection(item, expectSelected);
	}

	protected Item convertToItem(Element element) throws SeleniumPlusException{
		if(!(element instanceof Item)) throw new SeleniumPlusException("element is not an Item object.");
		return (Item) element;
	}

	protected void verifyItemSelected(Element element) throws SeleniumPlusException {	
		WDLibrary.checkNotNull(element);
		//get all contents, and check is one of them can match the 'element' and is selected
		String debugmsg = StringUtils.debugmsg(false);
		IndependantLog.warn(debugmsg+" get all contents to check if item '"+element.getLabel()+"' is selected.");

		boolean selected = false;
		try {
			Item[] items = getContent();
			for(Item item: items){
				if(item.equals(element)){
					IndependantLog.warn(debugmsg+" testing matching Element Item "+ item.getLabel());
					if(item.isSelected()) {
						selected = true;
						break;
					}
				}
			}
		} catch (SeleniumPlusException e) {
			IndependantLog.warn(debugmsg+" getContent could not get items from container. Met "+StringUtils.debugmsg(e));
		}

		if(!selected){
			IndependantLog.warn(debugmsg+" check all items, the item '"+element.getLabel()+"' is not selected.");
			super.verifyItemSelected(element);
		}

	}
}
