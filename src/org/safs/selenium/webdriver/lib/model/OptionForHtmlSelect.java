/**
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.model;

import org.safs.IndependantLog;
import org.safs.StringUtils;
//import org.safs.selenium.webdriver.lib.Component;
import org.safs.tools.stringutils.StringUtilities;


/**
 * This is an Option object to represent the &lt;option> for html &lt;select> <br>
 *
 *  <br>   MAY 18, 2017    (sbjlwa) Initial release.
 */
public class OptionForHtmlSelect extends Option{

	protected OptionForHtmlSelect(){}

	public OptionForHtmlSelect(Object object){
		initialize(object);
	}

	public void updateFields(){
		String debugmsg = StringUtils.debugmsg(getClass(), "updateFields");
		try{
			//we didn't call super.updateFields(),
			//instead we just copy-paste codes from super.updateFields() and comment out some to get better performance
//			super.updateFields();

			label = _getLabel();
			if(label!=null) label = StringUtilities.TWhitespace(label);
			try { selected = webelement.isSelected(); } catch (Exception e) { /* IndependantLog.debug(debugmsg+StringUtils.debugmsg(e));*/}

			//Neglect some fields to get a better performance
//			try { disabled = !webelement.isEnabled(); } catch (Exception e) { /* IndependantLog.debug(debugmsg+StringUtils.debugmsg(e));*/}
//			try { visible = webelement.isDisplayed(); } catch (Exception e) { /* IndependantLog.debug(debugmsg+StringUtils.debugmsg(e));*/}
//
//			value = getAttribute(Component.ATTRIBUTE_VALUE);
//			if(value!=null) value = StringUtilities.TWhitespace(value);

		}catch(Exception e){
			IndependantLog.warn(debugmsg+" Fail to update fields due to "+StringUtils.debugmsg(e));
		}
	}
}
