/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.util.JavaScriptFunctions.DOJO;
import org.safs.selenium.util.JavaScriptFunctions.SAP;
import org.safs.selenium.webdriver.lib.model.AbstractListSelectable;
import org.safs.selenium.webdriver.lib.model.Element;
import org.safs.selenium.webdriver.lib.model.IListSelectable;
import org.safs.selenium.webdriver.lib.model.Item;
import org.safs.selenium.webdriver.lib.model.TextMatchingCriterion;

/**
 * 
 * History:<br>
 * 
 *  <br>   APR 21, 2014    (Lei Wang) Initial release.
 *  <br>   APR 23, 2014    (Lei Wang) Update to support DOJO domain.
 */
public class TabControl extends Component{
	
	IListSelectable tabbable = null;
	
	/**
	 * @param tabcontrol	WebElement tabcontrol object, for example a sap.ui.commons.TabStrip object.
	 */
	public TabControl(WebElement tabcontrol) throws SeleniumPlusException{
		initialize(tabcontrol);
	}

	protected void updateFields(){
		super.updateFields();
		tabbable = (IListSelectable) anOperableObject;
	}
	/**
	 * @param tabcontrol	WebElement tabcontrol object, for example a sap.ui.commons.TabStrip object.
	 */
	protected IListSelectable createOperable(WebElement tabcontrol){
		String debugmsg = StringUtils.debugmsg(false);
		IListSelectable operable = null;
		try{			
			if(WDLibrary.isDojoDomain(tabcontrol)){
				operable = new DojoTabbable_TabContainer(this);
			}else if(WDLibrary.isSAPDomain(tabcontrol)){
				operable = new SapTabbable_TabStrip(this);
			}
			
		}catch(Exception e){ IndependantLog.debug(debugmsg+" Met Exception ", e); }
		
		if(operable==null){
			IndependantLog.error(debugmsg + "Can not create a proper Selectable object.");
		}
		
		return operable;
	}
	
	/**
	 * Try to select the tab according to the name (fully or partially given), 
	 * and then verify if the tab has been really selected according to the parameter 'verify'.
	 * @param tabName String, the tab to select
	 * @param partialMatch boolean, if the parameter tabName is given partially;
	 * @param matchIndex int, allows to match item N in a list containing duplicate entries, it is 0-based index.
	 * @param verify boolean, if true then verify the selection;
	 * @throws SeleniumPlusException
	 */
	public void selectTab(String tabName, boolean partialMatch, int matchIndex, boolean verify) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(this.getClass(), "selectTab");
		
		try{
			TextMatchingCriterion criterion = new TextMatchingCriterion(tabName, partialMatch, matchIndex);
			tabbable.selectItem(criterion, verify, null, null, WDLibrary.MOUSE_BUTTON_LEFT);
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "selectTab", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	/**
	 * Try to select the tab according to the index, and then verify if the tab 
	 * has been really selected according to the parameter 'verify'.
	 * @param index int, the tab to select, it is 0-based index.
	 * @param verify boolean, if true then verify the selection;
	 * @throws SeleniumPlusException
	 */	
	public void selectTab(int index, boolean verify) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(this.getClass(), "selectTab");
		
		try{
			tabbable.selectItem(index, verify, null, null, WDLibrary.MOUSE_BUTTON_LEFT);
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "selectTab", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}
	
	/**
	 * Get all items of the tabcontrol.
	 */
	public Item[] getContent() throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(this.getClass(), "getContent");
		
		try{
			return tabbable.getContent();
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "getContent", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}
	
	class SapTabbable_TabStrip extends AbstractListSelectable{
		public static final String CLASS_NAME_TABSTRIP = "sap.ui.commons.TabStrip";

		public SapTabbable_TabStrip(Component component) throws SeleniumPlusException {
			super(component);
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.model.Supportable#getSupportedClassNames()
		 */
		public String[] getSupportedClassNames() {
			String[] clazzes = {CLASS_NAME_TABSTRIP};
			return clazzes;
		}

		protected void  clickElement(Element element, Keys key, Point offset, int mouseButtonNumber, int numberOfClick) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(getClass(), "clickElement");

			try{
				super.clickElement(element, key, offset, mouseButtonNumber, numberOfClick);

			}catch(SeleniumPlusException spe){
				if(SeleniumPlusException.CODE_OBJECT_IS_NULL.equals(spe.getCode())) throw spe;

				Item tab = convertToItem(element);
				if(tab.getIndex()<0) throw new SeleniumPlusException("Item's index is less than zero, cannot select.");
				try {
					//TODO, How the pass the othter parameters???
					//Select by index
					StringBuffer jsScript = new StringBuffer();
					jsScript.append(SAP.sap_ui_commons_TabStrip_setSelectedIndex(true));
					jsScript.append(" return sap_ui_commons_TabStrip_setSelectedIndex(arguments[0], arguments[1]);");
					WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement(), tab.getIndex());
				} catch(Exception e) {
					IndependantLog.error(debugmsg+" Met exception.",e);
					throw new SeleniumPlusException("Fail to select index '"+tab.getIndex()+"'. due to "+e.getMessage());
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.model.AbstractSelectable#verifyItemSelected(org.safs.selenium.webdriver.lib.model.Item)
		 */
		protected void verifyItemSelected(Element element) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(getClass(), "verifyItemSelected");
			String msg = null;
			
			WDLibrary.checkNotNull(element);
			Item tab = convertToItem(element);
			
			int index = tab.getIndex();
			if(index==Item.INVALID_INDEX){
				msg = "Item's index is invalid, cannot verify.";
				IndependantLog.error(debugmsg+msg);
				throw new SeleniumPlusException(msg);
			}else{
				//Verify by index
				int selectedIndex = -1;
				try {
					StringBuffer jsScript = new StringBuffer();
					jsScript.append(SAP.sap_ui_commons_xxx_getSelectedIndex(true));
					jsScript.append(" return sap_ui_commons_xxx_getSelectedIndex(arguments[0]);");
					selectedIndex = Integer.parseInt(WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement()).toString());
				} catch(Exception e) {
					IndependantLog.error(debugmsg+" Met exception.",e);
					throw new SeleniumPlusException("Fail to select index '"+index+"' due to "+e.getMessage());
				}
				if(selectedIndex!=index){
					msg = "verification error: selectedIndex '"+selectedIndex+"' doesn't equal to index '"+index+"'";
					throw new SeleniumPlusException(msg, SeleniumPlusException.CODE_VERIFICATION_FAIL);
				}
			}
			
		}
		
		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.model.ISelectable#getContent()
		 */
		public Item[] getContent() throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getContent");
			List<Item> tabs = new ArrayList<Item>();
			
			try {
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(SAP.sap_ui_commons_TabStrip_getTabs(true));
				jsScript.append(" return sap_ui_commons_TabStrip_getTabs(arguments[0]);");
				Object result = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());
				
				if(result instanceof List){
					Object[] objects = ((List<?>) result).toArray();
					Item tab = null;
					for(Object object:objects){
						if(object!=null){
							tab = new Item(object);
							tabs.add(tab);
						}
					}
				}else if(result!=null){
					IndependantLog.warn("Need to hanlde javascript result whose type is '"+result.getClass().getName()+"'!");
					IndependantLog.warn("javascript result:\n"+result);
				}
			} catch(Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}

			return tabs.toArray(new Item[0]);
			
		}

	}
	
	class DojoTabbable_TabContainer extends AbstractListSelectable{
		public static final String CLASS_NAME_TABCONTAINER = "dijit.layout.TabContainer";

		public DojoTabbable_TabContainer(Component component) throws SeleniumPlusException {
			super(component);
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.model.Supportable#getSupportedClassNames()
		 */
		public String[] getSupportedClassNames() {
			String[] clazzes = {CLASS_NAME_TABCONTAINER};
			return clazzes;
		}

		protected void clickElement(Element element, Keys key, Point offset, int mouseButtonNumber, int numberOfClick) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(getClass(), "clickElement");

			try{
				super.clickElement(element, key, offset, mouseButtonNumber, numberOfClick);

			}catch(SeleniumPlusException spe){
				if(SeleniumPlusException.CODE_OBJECT_IS_NULL.equals(spe.getCode())) throw spe;

				try {
					//TODO, How the pass the othter parameters???
					StringBuffer jsScript = new StringBuffer();
					jsScript.append(DOJO.dojo_dijit_layout_StackContainer_selectChild(true));
					jsScript.append(" dojo_dijit_layout_StackContainer_selectChild(arguments[0], arguments[1]);");
					WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement(), element.getId());
				} catch(Exception e) {
					IndependantLog.error(debugmsg+" Met exception.",e);
					throw new SeleniumPlusException("Fail to select tab due to "+e.getMessage());
				}
			}
		}
		
		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.model.AbstractSelectable#verifyItemSelected(org.safs.selenium.webdriver.lib.model.Item)
		 */
		protected void verifyItemSelected(Element element) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(getClass(), "verifyTabSelected");
			String msg = null;
			
			WDLibrary.checkNotNull(element);
			Item tab = convertToItem(element);
			int index = tab.getIndex();
			
			if(index==Item.INVALID_INDEX){
				msg = "Item's index is invalid, cannot verify.";
				IndependantLog.error(debugmsg+msg);
				throw new SeleniumPlusException(msg);
			}else{
				int selectedIndex = -1;
				try {
					StringBuffer jsScript = new StringBuffer();
					jsScript.append(DOJO.dojo_dijit_layout_StackContainer_getSelectedIndex(true));
					jsScript.append(" return dojo_dijit_layout_StackContainer_getSelectedIndex(arguments[0]);");
					selectedIndex = Integer.parseInt(WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement()).toString());
				} catch(Exception e) {
					IndependantLog.error(debugmsg+" Met exception.",e);
					throw new SeleniumPlusException("Fail to select index '"+index+"' due to "+e.getMessage());
				}
				if(selectedIndex!=index){
					msg = "verification error: selectedIndex '"+selectedIndex+"' doesn't equal to index '"+index+"'";
					throw new SeleniumPlusException(msg, SeleniumPlusException.CODE_VERIFICATION_FAIL);
				}
			}
			
		}
		
		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.model.ISelectable#getContent()
		 */
		public Item[] getContent() throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getContent");
			List<Item> tabs = new ArrayList<Item>();
			
			try {
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(DOJO.dojo_dijit_layout_TabContainerBase_getChildren(true));
				jsScript.append(" return dojo_dijit_layout_TabContainerBase_getChildren(arguments[0]);");
				Object result = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());
				
				if(result instanceof List){
					Object[] objects = ((List<?>) result).toArray();
					Item tab = null;
					for(Object object:objects){
						if(object!=null){
							tab = new Item(object);
							tabs.add(tab);
						}
					}
				}else if(result!=null){
					IndependantLog.warn("Need to hanlde javascript result whose type is '"+result.getClass().getName()+"'!");
					IndependantLog.warn("javascript result:\n"+result);
				}
			} catch(Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}

			return tabs.toArray(new Item[0]);
			
		}

	}

}

