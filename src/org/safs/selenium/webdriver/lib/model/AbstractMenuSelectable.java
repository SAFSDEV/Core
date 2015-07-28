/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.model;

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.Component;
import org.safs.selenium.webdriver.lib.SeleniumPlusException;

/**
 * 
 * History:<br>
 * 
 *  <br>   Jun 23, 2014    (sbjlwa) Initial release.
 */
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
