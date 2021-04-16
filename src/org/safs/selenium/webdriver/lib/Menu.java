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
 * History:
 *
 *  JAN 20, 2014    (DHARMESH4) Initial release.
 *  JUN 10, 2014    (Lei Wang) Implement keywords.
 *  JAN 08, 2015    (Lei Wang) Support "sas.hc.ui.commons.pushmenu.PushMenu"
 *  JAN 29, 2015    (Lei Wang) Provide a way to go home for "sas.hc.ui.commons.pushmenu.PushMenu"
 *  OCT 16, 2015    (Lei Wang) Refector to create IOperable object properly.
 *  MAY 20, 2016    (Lei Wang) Add SapSelectable_Menu.waitAndVerifyItemSelected(): override method to ignore the verification for SAPUI5 Menu.
 */
package org.safs.selenium.webdriver.lib;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.safs.GuiObjectRecognition;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.util.JavaScriptFunctions.SAP;
import org.safs.selenium.webdriver.lib.model.AbstractMenuSelectable;
import org.safs.selenium.webdriver.lib.model.Element;
import org.safs.selenium.webdriver.lib.model.EmbeddedObject;
import org.safs.selenium.webdriver.lib.model.IMenuSelectable;
import org.safs.selenium.webdriver.lib.model.IOperable;
import org.safs.selenium.webdriver.lib.model.MenuItem;
import org.safs.selenium.webdriver.lib.model.TextMatchingCriterion;
import org.safs.tools.stringutils.StringUtilities;

/**
 * A library class to handle different specific Menu.
 */
public class Menu extends Component implements IMenuSelectable{
	IMenuSelectable menuSelectable = null;
	/**"SAS_PushMenu_GoHome_SAS" The path to trigger 'go home' event of SAS_HC_PushMenu*/
	public static final String HOME_KEY = "SAS_PushMenu_GoHome_SAS";

	public Menu(WebElement menubar) throws SeleniumPlusException{
		initialize(menubar);
	}

	protected void castOperable(){
		super.castOperable();
		menuSelectable = (IMenuSelectable) anOperableObject;
	}

	protected IOperable createSAPOperable(){
		String debugmsg = StringUtils.debugmsg(false);
		IMenuSelectable operable = null;
		try{ operable = new SapSelectable_Menu(this);}catch(SeleniumPlusException se0){
			IndependantLog.debug(debugmsg+" Cannot create IMenuSelectable of "+Arrays.toString(SapSelectable_Menu.supportedClazzes));
			try{ operable = new SAS_HC_PushMenu(this);}catch(SeleniumPlusException se1){
				IndependantLog.warn(debugmsg+" Cannot create IMenuSelectable of "+Arrays.toString(SAS_HC_PushMenu.supportedClazzes));
			}
		}
		return operable;
	}
	protected IOperable createDefaultOperable(){
		String debugmsg = StringUtils.debugmsg(false);
		IMenuSelectable operable = null;
		try{ operable = new DefaultSelectable_Menu(this); }catch(SeleniumPlusException se0){
			IndependantLog.debug(debugmsg+" Cannot create IMenuSelectable. ");
		}
		return operable;
	}

	public static boolean isNodeSelected(WebElement menuitem){
		return SearchObject.getBoolean(menuitem, ATTRIBUTE_ARIA_SELECTED);
	}

	/**
	 * TODO adjust for MenuItem
	 * For menuitem, WebElement.getText() may return also the text of its children.<br>
	 * These text are separated by '\n', this method tries to get the text before the first '\n', which<br>
	 * should be text of this tree node.
	 *
	 * @param menuitem WebElement, represents the tree node.
	 * @return String, the label of the MenuItem.
	 */
	public static String parseNodeText(WebElement menuitem){
		String text = menuitem.getText();
		int indexofNewLine = text.indexOf("\n");
		if(indexofNewLine!=-1) text = text.substring(0, indexofNewLine);

		return text;
	}

	class DefaultSelectable_Menu extends AbstractMenuSelectable{
		/**
		 * @param parent
		 * @throws SeleniumPlusException
		 */
		public DefaultSelectable_Menu(Component parent) throws SeleniumPlusException {
			super(parent);
		}
		//Just return true for now, as getSupportedClassNames() cannot return an appropriate array.
		public boolean isSupported(WebElement element){
			return true;
		}
		public String[] getSupportedClassNames() {
			//TODO What kind of HTML-TAG/CSS-CLASS can be considered as a MenuBar??? Just let isSupported() return true for now.
			return null;
		}
		public MenuItem[] getContent() throws SeleniumPlusException {
			//TODO How to get content of HTML-Menu???, what TAG???
			throw new SeleniumPlusException("Not supported yet.");
		}

		/**
		 * This method override that of superclass. It will only try to find each node according to<br>
		 * the path provided as parameter, and then return a simple-chain of MenuItem which contains only<br>
		 * the nodes specified in the path. Not like the result return in superclass, which is a double-direction<br>
		 * chain and the whole MenuBar can be accessed thought it.<br>
		 *
		 * <b>Note:</b>The parameter matchIndex is not used yet here. Need to be considered. TODO<br>
		 */
		public MenuItem getMatchedElement(TextMatchingCriterion criterion) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getMatchedElement(String, boolean, int)");
			MenuItem matchedNode = null;
			String itempath = criterion.getText();
			boolean partialMatch = criterion.isPartialMatch();

			//MenuItem[] elements = getContent();
			String[] pathNodes = StringUtils.getTokenArray(itempath, GuiObjectRecognition.DEFAULT_PATH_SEPARATOR, null);
			WebElement parentElement = webelement;
			List<WebElement> childrenElement = null;
			WebElement childElement = null;
			MenuItem parentNode = null;
			String searchCriteria = null;

			boolean isAccessible = Menu.this.isAccessible();
			String tempText = null;
			int level = 0;

			for(String nodeText: pathNodes){
				if(isAccessible){
					//Treate as a Menu, which follows the rules of 'Web Accessible Internet'
					//Find the all nodes containing attribute role='treeitem', the result will include
					//direct children, grand-children, grand-grand-children ..., is there a way to
					//get only the direct children???
//					searchCriteria = "xpath=.//*[role='menuitem']";
					searchCriteria = RS.XPATH.fromAttribute(ATTRIBUTE_WAI_ROLE, WAI_ROLE_MENUITEM, false, true);
					//for 'menuitem' 'menuitemradio' 'menuitemcheckbox' etc.
					//searchCriteria = RS.XPATH.fromAttribute(ATTRIBUTE_WAI_ROLE, WAI_ROLE_MENUITEM, true, true);
					childrenElement = parentElement.findElements(By.xpath(searchCriteria));
					for(int i=0;i<childrenElement.size();i++){
						childElement = childrenElement.get(i);
						tempText = Menu.parseNodeText(childElement);
//						if(criterion.matchText(tempText)){
						if(criterion.matchText(tempText, level)){
							parentElement = childElement;
							break;
						}
					}
				}else{
					//Treate as a normal tree.
					searchCriteria = RS.text(nodeText, partialMatch, true);
					//'Using parentElement' risks find NO MenuItem, as parentElement is just a SPAN TAG containing only 'Text'
					//if we create MenuItem from it, the MenuItem will not contain correct value for 'expanded' 'selected', then showOnPage(), expandItem() will not work.
					//parentElement = SearchObject.getObject(parentElement, xpathSearchCriteria);
					parentElement = SearchObject.getObject(webelement, searchCriteria);
				}
				level++;

				if(parentElement==null){
					IndependantLog.error(debugmsg+"node '"+nodeText + "' not found. " + searchCriteria);
					matchedNode = null;
					break;
				}else{
					matchedNode = new MenuItem(parentElement);
					matchedNode.setParent(parentNode);
					parentNode = matchedNode;
				}
			}

			if(matchedNode==null){
				IndependantLog.error(debugmsg+"Fail to find element "+criterion.toString());
				throw new SeleniumPlusException("Fail to find element '"+itempath+"'.");
			}
			return matchedNode;
		}

		protected void showOnPage(Element element) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(this.getClass(), "showOnPage");
			WDLibrary.checkNotNull(element);

			MenuItem node = convertTo(element);

			try {
				expandItem(node, true);

				//TODO Show the node, if the node is out of the scroll area.

			} catch(Exception e) {
				IndependantLog.error(debugmsg+" Met exception.",e);
				throw new SeleniumPlusException("Fail to show node '"+node.getLabel()+"'. due to '"+e.getMessage()+"'");
			}
		}

		/**
		 * Expand the nodes (by double-click) from the root level by level.<br>
		 * <b>Note:</b>The second parameter expandChildren is not used yet here.<br>
		 */
		protected void expandItem(MenuItem node, boolean expandChildren) throws SeleniumPlusException {
			Stack<MenuItem> stack = new Stack<MenuItem>();
			MenuItem parent = node;
			while(parent!=null){
				stack.push(parent);
				parent = parent.getParent();
			}

			MenuItem item = null;
			while(!stack.isEmpty()){
				item = stack.pop();
				if(item!=null){
					Actions action = new Actions(SearchObject.getWebDriver());
					Action click = action.click(item.getClickableWebElement()).build();
					click.perform();
					StringUtilities.sleep(500);//slowdown,correct way?
					//TODO if 'double-click' cannot expand the tree node
//					item.refresh(false);
				}
			}
		}
		/**
		 * For menu, the menuitem is usually NOT shown on page.<br>
		 * The method {@link EmbeddedObject#isShowOnPage(Element)} in super-class will<br>
		 * try to search the related web-element (which doesn't exist yet) and will waste<br>
		 * a lot of time.<br>
		 * So override it, and just return false, so that we will always call {@link #showOnPage(Element)}<br>
		 * to make it visible on page and can be selected correctly.<br>
		 * @see EmbeddedObject#isShowOnPage(Element)
		 */
		protected boolean isShowOnPage(Element element) throws SeleniumPlusException {
			return false;
		}
	}

	public static class SapSelectable_Menu extends AbstractMenuSelectable{
		public static final String CLASS_NAME_MENUBAR 			= "sap.ui.commons.MenuBar";
		public static final String CLASS_NAME_MENU 				= "sap.ui.commons.Menu";
		public static final String CLASS_NAME_UNIFIED_MENU 		= "sap.ui.unified.Menu";
		public static final String[] supportedClazzes = {CLASS_NAME_MENUBAR, CLASS_NAME_MENU, CLASS_NAME_UNIFIED_MENU};
		public static final String[] menuClazzes = {CLASS_NAME_MENU, CLASS_NAME_UNIFIED_MENU};

		public static final String CLASS_NAME_COMMONS_MENUITEM 		= "sap.ui.commons.MenuItem";
		public static final String CLASS_NAME_UNIFIED_MENUITEM 		= "sap.ui.unified.MenuItem";
		public static final String[] menuItemClazzes = {CLASS_NAME_COMMONS_MENUITEM, CLASS_NAME_UNIFIED_MENUITEM};

		public static final String CLASS_NAME_COMMONS_TEXTMENUITEM 		= "sap.ui.commons.MenuTextFieldItem";
		public static final String CLASS_NAME_UNIFIED_TEXTMENUITEM 		= "sap.ui.unified.MenuTextFieldItem";
		public static final String[] textMenuItemClazzes = {CLASS_NAME_COMMONS_TEXTMENUITEM, CLASS_NAME_UNIFIED_TEXTMENUITEM};

		public SapSelectable_Menu(Component component) throws SeleniumPlusException {
			super(component);
		}

		public String[] getSupportedClassNames() {
			return supportedClazzes;
		}

		public MenuItem[] getContent() throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(getClass(), "getContent");
			MenuItem root = null;

			try {
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(SAP.sap_ui_commons_Menu_getItems(true));
				jsScript.append(" return sap_ui_commons_Menu_getItems(arguments[0]);");
				Object result = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());

				if(result instanceof Map){
					root = new MenuItem(result);
					return root.getChildren();
				}else if(result!=null){
					IndependantLog.warn("Need to hanlde javascript result whose type is '"+result.getClass().getName()+"'!");
					IndependantLog.warn("javascript result:\n"+result);
				}
			} catch(Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}

			return null;
		}

		/**
		 * Expand the menuitem by click their ancestor item on the path level by level.<br>
		 *
		 * @param item
		 * @param expandChildren
		 * @throws SeleniumPlusException
		 */
		protected void expandItem(MenuItem item, boolean expandChildren) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(this.getClass(), "expandItem");

			try {
				//We need to expand from the top node level by level.
				List<MenuItem> items = new ArrayList<MenuItem>();//a list of MenuItem, from this node to top node.
				MenuItem parent = item;
				while(parent!=null){
					//if node's parent is null, which means the node is a MenuBar, NOT a MenuItem
					if(parent.getParent()!=null) items.add(parent);
					parent = parent.getParent();
				}

				MenuItem tempItem = null;
				for(int i=items.size()-1;i>=0;i--){
					tempItem = items.get(i);
					if(tempItem.hasSubMenu()){
						//during debug, selenium's click() works fine; but during running, the submenu will collapse
//						tempItem.getWebElement().click();
//						tempItem.getClickableWebElement().click();
						//Use robot click, the submenu will stay there.
						WDLibrary.click(tempItem.getClickableWebElement(), null, null, WDLibrary.MOUSE_BUTTON_LEFT);
						StringUtilities.sleep(200);//Wait for the submenu to show
					}else{
						//If there is no submenu for certain item in the path, no need to continue expand.
						IndependantLog.warn(debugmsg+" There is no submenu for itme '"+tempItem.getLabel()+"'");
						break;
					}
				}

			} catch(Exception e) {
				IndependantLog.error(debugmsg+" Met exception.",e);
				throw new SeleniumPlusException("Fail to expand node '"+item.getLabel()+"'. due to '"+e.getMessage()+"'");
			}
		}

		/**
		 * In this implementation, we call {@link #expandItem(MenuItem, boolean)} to click<br>
		 * item on the path level by level.<br>
		 *
		 * @see #expandItem(MenuItem, boolean)
		 */
		protected void showOnPage(Element element) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(this.getClass(), "showOnPage");
			WDLibrary.checkNotNull(element);

			MenuItem item = convertTo(element);

			try {
				expandItem(item, true);

			} catch(Exception e) {
				IndependantLog.error(debugmsg+" Met exception.",e);
				throw new SeleniumPlusException("Fail to show node '"+item.getLabel()+"'. due to '"+e.getMessage()+"'");
			}
		}

		/**
		 * For menu, the menuitem is usually NOT shown on page.<br>
		 * The method {@link EmbeddedObject#isShowOnPage(Element)} in super-class will<br>
		 * try to search the related web-element (which doesn't exist yet) and will waste<br>
		 * a lot of time.<br>
		 * So override it, and just return false, so that we will always call {@link #showOnPage(Element)}<br>
		 * to make it visible on page and can be selected correctly.<br>
		 * @see EmbeddedObject#isShowOnPage(Element)
		 */
		protected boolean isShowOnPage(Element element) throws SeleniumPlusException {
			return false;
		}

	}//End of SapSelectable_Menu

	public static class SAS_HC_PushMenu extends AbstractMenuSelectable{
		public static final String CLASS_NAME_PUSHMENU 	= "sas.hc.ui.commons.pushmenu.PushMenu";
		public static final String[] supportedClazzes = {CLASS_NAME_PUSHMENU};

		//TODO If PushMenu has a PushMenuGroup, which is invisible. But this may change if the PushMenu changes it
		//structure, PushMenu theoretically can contain multiple PushMenuGroups, and these PushMenuGroups can be
		//set to visible or not
		private boolean hasInvisibleRoot = true;

		public SAS_HC_PushMenu(Component component) throws SeleniumPlusException {
			super(component);
		}

		public String[] getSupportedClassNames() {
			return supportedClazzes;
		}

		public MenuItem[] getContent() throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(getClass(), "getContent");
			MenuItem root = null;

			try {
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(SAP.sas_hc_ui_commons_pushmenu_PushMenu_getItems(true));
				jsScript.append(" return sas_hc_ui_commons_pushmenu_PushMenu_getItems(arguments[0]);");
				Object result = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());

				if(result instanceof Map){
					root = new MenuItem(result);
					//If PushMenu has a PushMenuGroup, which is invisible, we have to return the PushMenuSuite contained in PushMenuGroup
					//TODO If PushMenu has changed its structure, we have to modify code here.
					if(!hasInvisibleRoot){
						return root.getChildren();
					}else{
						MenuItem[] roots = root.getChildren();
						if(roots!=null && roots.length>0){
							return roots[0].getChildren();
						}
					}
				}else if(result!=null){
					IndependantLog.warn("Need to hanlde javascript result whose type is '"+result.getClass().getName()+"'!");
					IndependantLog.warn("javascript result:\n"+result);
				}
			} catch(Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}

			return null;
		}

		/**
		 * Goes back to the home page
		 */
		protected void goHome(){
			String debugmsg = StringUtils.debugmsg(false);

			try {
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(SAP.sas_hc_ui_commons_pushmenu_PushMenu_goHome(true));
				jsScript.append(" sas_hc_ui_commons_pushmenu_PushMenu_goHome(arguments[0]);");
				WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());

			} catch(Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}
		}

		/**
		 * Expand the menuitem by click their ancestor item on the path level by level.<br>
		 *
		 * @param item
		 * @param expandChildren
		 * @throws SeleniumPlusException
		 */
		protected void expandItem(MenuItem item, boolean expandChildren) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(this.getClass(), "expandItem");

			try {
				//We need to expand from the top node level by level.
				List<MenuItem> items = new ArrayList<MenuItem>();//a list of MenuItem, from this node to top node.
				MenuItem parent = item;
				while(parent!=null){
					//if node's parent is null, which means the node is a MenuBar, NOT a MenuItem
					if(parent.getParent()!=null) items.add(parent);
					parent = parent.getParent();
				}
				if(hasInvisibleRoot){
					items.remove(items.size()-1);//remove the invisible PushMenuGroup
				}

				MenuItem tempItem = null;
				WebElement itemWebElement = null;
				for(int i=items.size()-1;i>=0;i--){
					tempItem = items.get(i);
					if(tempItem.hasSubMenu()){
//						itemWebElement = tempItem.getClickableWebElement();
						itemWebElement = tempItem.getWebElement();
						if(itemWebElement!=null){
							WDLibrary.showOnPage(itemWebElement);
							//Should we click always? Sometimes the children are already shown, we don't need
							//Sometimes the children are hidden, we need. But it is no harm to click on a
							//item container 'PushMenuSuite'.
							itemWebElement.click();
						}
//						WDLibrary.click(itemWebElement, null, null, WDLibrary.MOUSE_BUTTON_LEFT);
						StringUtilities.sleep(200);//Wait for the submenu to show
					}else{
						//If there is no submenu for certain item in the path, no need to continue expand.
						IndependantLog.warn(debugmsg+" There is no submenu for itme '"+tempItem.getLabel()+"'");
						break;
					}
				}

			} catch(Exception e) {
				IndependantLog.error(debugmsg+" Met exception.",e);
				throw new SeleniumPlusException("Fail to expand node '"+item.getLabel()+"'. due to '"+e.getMessage()+"'");
			}
		}

		/**
		 * In this implementation, we call {@link #expandItem(MenuItem, boolean)} to click<br>
		 * item on the path level by level.<br>
		 *
		 * @see #expandItem(MenuItem, boolean)
		 */
		protected void showOnPage(Element element) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(this.getClass(), "showOnPage");
			WDLibrary.checkNotNull(element);

			MenuItem item = convertTo(element);

			try {
				expandItem(item, true);

			} catch(Exception e) {
				IndependantLog.error(debugmsg+" Met exception.",e);
				throw new SeleniumPlusException("Fail to show node '"+item.getLabel()+"'. due to '"+e.getMessage()+"'");
			}
		}

		public void selectItem(TextMatchingCriterion criterion, boolean verify, Keys key, Point offset, int mouseButtonNumber) throws SeleniumPlusException{
			if(criterion.getText().equalsIgnoreCase(HOME_KEY)){
				goHome();
			}else{
				super.selectItem(criterion, verify, key, offset, mouseButtonNumber);
			}
		}

	}//End of SAS_HC_PushMenu

	public void selectItem(TextMatchingCriterion criterion, boolean verify, Keys key, Point offset, int mouseButtonNumber) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(this.getClass(), "selectItem");

		try{
			menuSelectable.selectItem(criterion, verify, key, offset, mouseButtonNumber);
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "selectItem", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	public void selectItem(int index, boolean verify, Keys key, Point offset, int mouseButtonNumber) throws SeleniumPlusException {
		throw new SeleniumPlusException("Not supported.");
	}

	public void activateItem(TextMatchingCriterion criterion, boolean verify, Keys key, Point offset) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(this.getClass(), "activateItem");

		try{
			menuSelectable.activateItem(criterion, verify, key, offset);
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "activateItem", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	public void activateItem(int index, boolean verify, Keys key, Point offset) throws SeleniumPlusException {
		throw new SeleniumPlusException("Not supported.");
	}

	public void verifyItemSelection(TextMatchingCriterion criterion, boolean expectSelected) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(this.getClass(), "verifyItemSelection");

		try{
			menuSelectable.verifyItemSelection(criterion, expectSelected);
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "verifyItemSelection", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	public void verifyItemSelection(int index, boolean expectSelected) throws SeleniumPlusException {
		throw new SeleniumPlusException("Not supported.");
	}

	public void verifyContains(TextMatchingCriterion criterion) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(this.getClass(), "verifyContains");

		try{
			menuSelectable.verifyContains(criterion);
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "verifyContains", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	public MenuItem[] getContent() throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(this.getClass(), "getContent");

		try{
			return menuSelectable.getContent();
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "getContent", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	public MenuItem getMatchedElement(TextMatchingCriterion criterion) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(this.getClass(), "getMatchedElement");

		try{
			return menuSelectable.getMatchedElement(criterion);
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "getMatchedElement", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	public void verifyMenuItem(TextMatchingCriterion criterion, String expectedStatus) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(this.getClass(), "verifyMenuItem");

		try{
			menuSelectable.verifyMenuItem(criterion, expectedStatus);
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "verifyMenuItem", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}

	}
}
