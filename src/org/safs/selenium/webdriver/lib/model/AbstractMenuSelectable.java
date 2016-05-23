/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * 
 * History:<br>
 * 
 *  <br>   JUN 23, 2014    (sbjlwa) Initial release.
 *  <br>   MAY 23, 2016    (sbjlwa) Override method waitAndVerifyItemSelected() with empty implementation so that the post-verification
 *                                  will be totally turned off for Menu.
 */
package org.safs.selenium.webdriver.lib.model;

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.Component;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;


public abstract class AbstractMenuSelectable extends AbstractHierarchicalSelectable implements IMenuSelectable{

	/**
	 * @param parent
	 * @throws SeleniumPlusException
	 */
	public AbstractMenuSelectable(Component parent) throws SeleniumPlusException {
		super(parent);
	}
	
	public MenuItem[] getContent() throws SeleniumPlusException{
		try{
			return (MenuItem[]) super.getContent();
		}catch(SeleniumPlusException se){
			throw se;
		}catch(Exception e){
			String msg = "Fail to convert content to MenuItem[].";
			IndependantLog.error(msg, e);
			throw new SeleniumPlusException(msg);
		}
	}
	
	/**
	 * According to the "hierarchical path" to get a MenuItem object.
	 */
	public MenuItem getMatchedElement(TextMatchingCriterion criterion) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(getClass(), "getMatchedElement");
		try{
			return (MenuItem) super.getMatchedElement(criterion);
		}catch(SeleniumPlusException spe){
			throw spe;
		}catch(Exception e){
			IndependantLog.error(debugmsg+"Fail to get matched element.",e);
			throw new SeleniumPlusException("Fail to get matched element."+StringUtils.debugmsg(e));
		}
	}

	/**
	 * Cast the Element to MenuItem.
	 */
	protected MenuItem convertTo(Element element) throws SeleniumPlusException{
		if(!(element instanceof MenuItem)) throw new SeleniumPlusException("element is not a MenuItem object.");
		return (MenuItem) element;
	}
	
	/**
	 * Override the method in super-class to turn off the post-verification, we just log a debug message.<br>
	 * <p>
	 * {@link AbstractSelectable#waitAndVerifyItemSelected(Element)} will try to refresh the WebElment and then
	 * call {@link #verifyItemSelected(Element)} to verify item's selection. But currently
	 * {@link #verifyItemSelected(Element)} will do nothing, so it is a time-wasting to refresh the WebElement.
	 * We just override {@link #waitAndVerifyItemSelected(Element)} to do nothing but log a debug message.
	 * </p>
	 */
	protected void waitAndVerifyItemSelected(Element element) throws SeleniumPlusException {
		IndependantLog.debug(StringUtils.debugmsg(false)+" will NOT VERIFY if the item has been selected until we override the method AbstractMenuSelectable.verifyItemSelected()!");
	}
	
	/**
	 * <p>
	 * As it is hard to know if a MenuItem has been selected, it is so transient.
	 * For example, <a href="https://openui5.hana.ondemand.com/docs/api/symbols/sap.ui.unified.MenuItemBase.html">MenuItemBase API</a>
	 * does NOT provide a way to get menu item selection status.
	 * So we will do nothing in {@link #verifyItemSelected(Element)}, which could be overrode if we know a way to verify menu item selection.
	 * </p>
	 */
	protected void verifyItemSelected(Element element) throws SeleniumPlusException {
		//'selected' attribute is not possible to check for a menuitem.
		String debugmsg = StringUtils.debugmsg(getClass(), "verifyItemSelected");
		IndependantLog.debug(debugmsg+" What to check for a MenuItem being selected?");
	}
	
	public void verifyMenuItem(TextMatchingCriterion criterion, String expectedStatus) throws SeleniumPlusException{
		MenuItem menuitem = getMatchedElement(criterion);
		if(menuitem==null){
			String msg = "verification error: does not contain element '"+criterion.getText()+"'";
			throw new SeleniumPlusException(msg, SeleniumPlusException.CODE_VERIFICATION_FAIL);
		}
		if(!menuitem.matchStatus(expectedStatus)){
			String msg = "verification error: does not match status '"+expectedStatus+"'";
			throw new SeleniumPlusException(msg, SeleniumPlusException.CODE_VERIFICATION_FAIL);
		}
	}
	
}
