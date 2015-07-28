/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib;

import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.util.JavaScriptFunctions.SAP;
import org.safs.selenium.webdriver.lib.model.EmbeddedObject;
import org.safs.selenium.webdriver.lib.model.IOperable;

/**
 * 
 * History:<br>
 * 
 *  <br>   JAN 29, 2014    (Lei Wang) Initial release.
 *  <br>   FEB 12, 2014    (Lei Wang) Modify method SapCheckable_CheckBox.setChecked() to refresh the stale embedded webelement.
 *  <br>   JUN 12, 2015    (Lei Wang) Click to check/uncheck firstly, native-javascript will be a backup.
 */
public class CheckBox extends Component{
	
	Checkable checkable = null;
	
	/**
	 * @param checkbox	WebElement combo box object, for example an HTML tag &lt;select&gt;.
	 */
	public CheckBox(WebElement checkbox) throws SeleniumPlusException{
		initialize(checkbox);
	}

	protected void updateFields(){
		super.updateFields();
		checkable = (Checkable) anOperableObject;
	}
	
	protected Checkable createOperable(WebElement checkbox){
		String debugmsg = StringUtils.debugmsg(false);
		Checkable operable = null;
		try{
			//Try to get the possible Checkable
			if(WDLibrary.isDojoDomain(checkbox)){
				//TODO
			}else if(WDLibrary.isSAPDomain(checkbox)){
				operable = new SapCheckable_CheckBox(this); 
			}else{
				operable = new HtmlCheckable_InputCheckBox(this);
			}
		}catch(Exception e){ IndependantLog.debug(debugmsg+" Met Exception ", e); }
		
		if(operable==null){
			IndependantLog.error("Can not create a proper Checkable object.");
		}
		return operable;
	}
	
	public void check() throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(this.getClass(), "check");
		
		try{
			checkable.check();
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "check", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}
	
	public void uncheck() throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(this.getClass(), "uncheck");
		
		try{
			checkable.uncheck();
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "uncheck", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}
	
	interface Checkable extends IOperable{
		/**
		 * Try to check the checkbox, and then verify if the checkbox 
		 * has been really checked.
		 * @throws SeleniumPlusException
		 */
		public void check() throws SeleniumPlusException;
		/**
		 * Try to uncheck the checkbox, and then verify if the checkbox 
		 * has been really unchecked.
		 * @throws SeleniumPlusException
		 */
		public void uncheck() throws SeleniumPlusException;
		/**
		 * @return boolean if the the checkbox is checked or not
		 * @throws SeleniumPlusException
		 */
		public boolean isChecked() throws SeleniumPlusException;
	}
	
	abstract class AbstractCheckable extends EmbeddedObject implements Checkable{
		
		public void clearCache(){
			String methodName = StringUtils.getCurrentMethodName(true);
			IndependantLog.debug(methodName+" has not been implemented.");
		}
		
		public AbstractCheckable(Component component)throws SeleniumPlusException {
			super(component);
		}
		
		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.CheckBox.Checkable#check()
		 */
		public void check() throws SeleniumPlusException {
			setChecked(true);
			if(!isChecked()){
				throw new SeleniumPlusException("Check box has not been checked.");
			}
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.CheckBox.Checkable#uncheck()
		 */
		public void uncheck() throws SeleniumPlusException {
			setChecked(false);
			if(isChecked()){
				throw new SeleniumPlusException("Check box is still checked.");
			}
		}
		
		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.CheckBox.Checkable#isChecked()
		 */
		public boolean isChecked() throws SeleniumPlusException {
			return webelement().isSelected();
		}
		
		/**
		 * Set the value of property 'checked' for checkbox.<br>
		 * Here we will just call click to check/uncheck, if it doesn't work,<br>
		 * then we will call {@link #nativeSetChecked(boolean)} to set it again.<br>
		 * @param bChecked boolean the value to set for checkbox
		 * @throws SeleniumPlusException
		 */
		protected void setChecked(boolean bChecked)  throws SeleniumPlusException{
			String debugmsg = StringUtils.debugmsg(false);
			String action = bChecked? "check":"uncheck";

			try{
				//Firstly, call setChecked of superclass, it is simply a click to check/uncheck a check-box.
				if(!isStatusOk(bChecked)){
					IndependantLog.debug(debugmsg+" Clicking on check-box to "+action+" it.");
					WDLibrary.click(webelement());
					//calling WDLibrary.click() may make the embedded element stale
					//refresh the stale embedded webelement
					refresh(false);
					
					//After clicking, if the status is not OK, we will try to call native method provided by special checkbox
					if(!isStatusOk(bChecked)){
						IndependantLog.debug(debugmsg+" Clicking on check-box doesn't work, try native method provided by special checkbox.");
						nativeSetChecked(bChecked);
					}					
				}
			}catch(Exception ex){
				IndependantLog.error(debugmsg+" Met exception.",ex);
				throw new SeleniumPlusException("Fail to "+action+" checkbox.");
			}

		}
		
		/**
		 * Call the native method provide by special checkbox to check/incheck.<br>
		 * <b>
		 * Note: There are some callback methods associated with the checkbox, but setting
		 * property 'checked' may NOT invoke them, in the implementation of this method we
		 * should make sure those callbacks invoked (we can fire some events.)
		 * </b>
		 * @param bChecked
		 * @throws SeleniumPlusException
		 */
		protected void nativeSetChecked(boolean bChecked)  throws SeleniumPlusException{
			throw new SeleniumPlusException(StringUtils.debugmsg(false)+" has not been implementated yet.");
		}
		
		/**
		 * Check if the checkbox's status is the same as the 'expected status'.
		 * @param bChecked boolean, the 'expected status' of the checkbox.
		 * @return boolean, if the 'expected status' is the same as checkbox's status.
		 * @throws SeleniumPlusException
		 */
		protected boolean isStatusOk(boolean bChecked) throws SeleniumPlusException{
			return isChecked()==bChecked;
		}
	}
	
	abstract class SapCheckable extends AbstractCheckable{

		public SapCheckable(Component component) throws SeleniumPlusException {
			super(component);
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.Component.Supportable#isSupported(org.openqa.selenium.WebElement)
		 */
		public boolean isSupported(WebElement element){
			return WDLibrary.SAP.isSupported(element, getSupportedClassNames());
		}

	}
	
	abstract class DojoCheckable extends AbstractCheckable{

		public DojoCheckable(Component component) throws SeleniumPlusException {
			super(component);
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.Component.Supportable#isSupported(org.openqa.selenium.WebElement)
		 */
		public boolean isSupported(WebElement element){
			return WDLibrary.DOJO.isSupported(element, getSupportedClassNames());
		}
	}
	
	abstract class HtmlCheckable extends AbstractCheckable{

		public HtmlCheckable(Component component) throws SeleniumPlusException {
			super(component);
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.Component.Supportable#isSupported(org.openqa.selenium.WebElement)
		 */
		public boolean isSupported(WebElement element){
			boolean supported = WDLibrary.HTML.isSupported(element, getSupportedClassNames());
			if(supported){
				//check if the property 'type' has the value as 'checkbox'
				supported &=VALUE_CHECKBOX_ATTRIBUTE_TYPE.equalsIgnoreCase(element.getAttribute(ATTRIBUTE_TYPE));
			}
			return supported;
		}
		
	}
	
	class SapCheckable_CheckBox extends SapCheckable{
		public static final String CLASS_NAME_UI_COMMONS_COMBOBOX = "sap.ui.commons.CheckBox";
		public static final String CLASS_NAME_M_COMBOBOX = "sap.m.CheckBox";

		public SapCheckable_CheckBox(Component component) throws SeleniumPlusException {
			super(component);
		}

		protected void nativeSetChecked(boolean bChecked)  throws SeleniumPlusException{
			String debugmsg = StringUtils.debugmsg(false);
			
			StringBuffer jsScript = new StringBuffer();
			jsScript.append(SAP.sap_ui_commons_CheckBox_setChecked(true));
			jsScript.append("sap_ui_commons_CheckBox_setChecked(arguments[0],arguments[1]);");

			try {
				WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement(), bChecked);
				//calling javascript sap_ui_commons_CheckBox_setChecked() will make the embedded element stale
				//refresh the stale embedded webelement
				refresh(false);
				return;
			} catch(Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}
			
			throw new SeleniumPlusException("Fail to set value of property 'checked'");
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.CheckBox.Checkable#isChecked()
		 */
		public boolean isChecked() throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(false);
			
			if(super.isChecked()) return true;
			
			StringBuffer jsScript = new StringBuffer();
			jsScript.append(SAP.sap_ui_commons_CheckBox_getChecked(true));
			jsScript.append("return sap_ui_commons_CheckBox_getChecked(arguments[0]);");

			try {
				Object checked = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());
				if(checked instanceof Boolean){
					return ((Boolean)checked).booleanValue();
				}
				if(checked!=null){
					return Boolean.parseBoolean(checked.toString());
				}else{
					IndependantLog.error(debugmsg+" The value of property 'checked' is returned as null.");
				}
			} catch(Exception e) {
				IndependantLog.error(debugmsg+" Met exception.",e);
			}
			
			throw new SeleniumPlusException("Fail to get value of property 'checked'");
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.Component.Supportable#getSupportedClassNames()
		 */
		public String[] getSupportedClassNames() {
			String[] clazzes = {CLASS_NAME_UI_COMMONS_COMBOBOX, CLASS_NAME_M_COMBOBOX};
			return clazzes;
		}
	}
	
	class HtmlCheckable_InputCheckBox extends HtmlCheckable{
		public static final String CLASS_NAME = TAG_HTML_INPUT;

		public HtmlCheckable_InputCheckBox(Component component) throws SeleniumPlusException {
			super(component);
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.Component.Supportable#getSupportedClassNames()
		 */
		public String[] getSupportedClassNames() {
			String[] clazzes = {CLASS_NAME};
			return clazzes;
		}
		
	}
}

