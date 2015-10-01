/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.util.JavaScriptFunctions;
import org.safs.selenium.util.JavaScriptFunctions.SAP;
import org.safs.selenium.webdriver.lib.model.AbstractListSelectable;
import org.safs.selenium.webdriver.lib.model.Element;
import org.safs.selenium.webdriver.lib.model.IListSelectable;
import org.safs.selenium.webdriver.lib.model.IOperable;
import org.safs.selenium.webdriver.lib.model.Item;
import org.safs.selenium.webdriver.lib.model.TextMatchingCriterion;

/**
 * 
 * History:<br>
 * 
 *  <br>   APR 2, 2014    (Lei Wang) Initial release.
 *  <br>   SEP 2, 2014    (LeiWang) Support sap.m.ListBase.
 */
public class ListView extends Component implements IListSelectable{
	IListSelectable listable = null;

	/**
	 * @param listview	WebElement listview object, for example a sap.ui.commons.ListBox object.
	 */
	public ListView(WebElement listview) throws SeleniumPlusException{
		initialize(listview);
	}

	protected void updateFields(){
		super.updateFields();
		listable = (IListSelectable) anOperableObject;
	}

	/**
	 * @param listview	WebElement listview object, for example a sap.ui.commons.ListBox object.
	 */
	protected IOperable createOperable(WebElement listview){
		String debugmsg = StringUtils.debugmsg(false);
		IListSelectable operable = null;
		try{
			if(WDLibrary.isDojoDomain(listview)){
				IndependantLog.warn(debugmsg+" Cannot create Selectable operable of Dojo Selectable at this time.");
				//				return new DojoSelectable_MultiSelect(this);
			}else if(WDLibrary.isSAPDomain(listview)){
				IndependantLog.info(debugmsg+" trying to create Selectable operable of SAP/OpenUI5 at this time.");
				try{ operable = new SapSelectable_ListBox(this);}catch(SeleniumPlusException se0){
					IndependantLog.debug(debugmsg+" Cannot create Selectable of "+Arrays.toString(SapSelectable_ListBox.supportedClazzes));
					try{ operable = new SapSelectable_m_List(this);}catch(SeleniumPlusException se1){
						IndependantLog.warn(debugmsg+" Cannot create Selectable of "+Arrays.toString(SapSelectable_m_List.supportedClazzes));
						try{ operable = new SapSelectable_m_SelectList(this);}catch(SeleniumPlusException se2){
							IndependantLog.warn(debugmsg+" Cannot create Selectable of "+Arrays.toString(SapSelectable_m_SelectList.supportedClazzes));
						}					
					}					
				}
			}
			if(operable == null) {
				IndependantLog.info(debugmsg+" trying to create Selectable operable of for Generic Lists at this time.");
				try{ operable = new GenericSelectableList(this);}catch(SeleniumPlusException se0){
					IndependantLog.debug(debugmsg+" Cannot create Selectable of "+Arrays.toString(GenericSelectableList.supportedClazzes));
				}
			}

		}catch(Exception e){ IndependantLog.debug(debugmsg+" Met Exception ", e); }

		if(operable==null){
			IndependantLog.error("Can not create a proper Selectable object.");
			return super.createOperable(listview);
		}
		return operable;
	}

	/**
	 * Try to select an item according to the name (fully or partially given), 
	 * and then verify if the tab has been really selected according to the parameter 'verify'.
	 * @param text String, the item to select
	 * @param partialMatch boolean, if the parameter text is given partially;
	 * @param matchIndex int, allows to match item N in a list containing duplicate entries, it is 0-based index.
	 * @param verify boolean, if true then verify the selection;
	 * @throws SeleniumPlusException
	 * @see {@link #selectItem(String, boolean, int, boolean, int)}
	 */
	public void selectItem(TextMatchingCriterion criterion, boolean verify) throws SeleniumPlusException{
		selectItem(criterion, verify, null, null, WDLibrary.MOUSE_BUTTON_LEFT);
	}
	/**
	 * Try to select an item according to the name (fully or partially given), 
	 * and then verify if the tab has been really selected according to the parameter 'verify'.
	 */
	public void selectItem(TextMatchingCriterion criterion, boolean verify, Keys key, Point offset, int mouseButtonNumber) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(this.getClass(), "selectItem");
		try{
			// prevent infinite loop
			if(listable != this) { 
				listable.selectItem(criterion, verify, key, offset, mouseButtonNumber);
			}else{
				String msg = debugmsg +" listable(this) infinite loop prevention!";
				IndependantLog.error(msg);
				throw new SeleniumPlusException(msg);
			}
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "selectItem", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	/**
	 * Try to select the item according to the index, and then verify if the item 
	 * has been really selected according to the parameter 'verify'.
	 * @param index int, the index of the item to select, it is 0-based index.
	 * @param verify boolean, if true then verify the selection;
	 * @throws SeleniumPlusException
	 * @see {@link #selectItem(int, boolean, int)}
	 */
	public void selectItem(int index, boolean verify) throws SeleniumPlusException{
		selectItem(index, verify, null, null, WDLibrary.MOUSE_BUTTON_LEFT);
	}
	/**
	 * Try to select the item according to the index, and then verify if the item 
	 * has been really selected according to the parameter 'verify'.
	 */	
	public void selectItem(int index, boolean verify, Keys key, Point offset, int mouseButtonNumber) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(this.getClass(), "selectItem");
		try{
			// prevent infinite loop
			if(listable != this) { 
				listable.selectItem(index, verify, key, offset, mouseButtonNumber);
			}else{
				String msg = debugmsg +" listable(this) infinite loop prevention!";
				IndependantLog.error(msg);
				throw new SeleniumPlusException(msg);
			}
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "selectItem", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}

	}

	public void activateItem(TextMatchingCriterion criterion, boolean verify, Keys key, Point offset) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(this.getClass(), "activateItem");
		try{
			if(listable != this) { 
				listable.activateItem(criterion, verify, key, offset);
			}else{
				String msg = debugmsg +" listable(this) infinite loop prevention!";
				IndependantLog.error(msg);
				throw new SeleniumPlusException(msg);
			}
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "activateItem", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	public void activateItem(int index, boolean verify, Keys key, Point offset) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(this.getClass(), "activateItem");
		try{
			if(listable != this) { 
				listable.activateItem(index, verify, key, offset);
			}else{
				String msg = debugmsg +" listable(this) infinite loop prevention!";
				IndependantLog.error(msg);
				throw new SeleniumPlusException(msg);
			}
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "activateItem", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	/**
	 * Verify the item (specified by text, partialMatch and matchIndex) is selected or un-selected in the listview.
	 */
	public void verifyItemSelection(TextMatchingCriterion criterion, boolean expectSelected) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(this.getClass(), "verifyItemSelection");

		try{
			if(listable != this) { 
				listable.verifyItemSelection(criterion, expectSelected);
			}else{
				String msg = debugmsg +" listable(this) infinite loop prevention!";
				IndependantLog.error(msg);
				throw new SeleniumPlusException(msg);
			}
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "verifyItemSelection", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	/**
	 * Verify the item (specified by text, partialMatch and matchIndex) is contained in the listview.
	 */
	public void verifyContains(TextMatchingCriterion criterion) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(this.getClass(), "verifyContains");

		try{
			if(listable != this) { 
				listable.verifyContains(criterion);
			}else{
				String msg = debugmsg +" listable(this) infinite loop prevention!";
				IndependantLog.error(msg);
				throw new SeleniumPlusException(msg);
			}
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "verifyContains", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	/**
	 * Verify the item (specified by index) is selected or un-selected in the listview. 
	 */
	public void verifyItemSelection(int index, boolean expectSelected) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(this.getClass(), "verifyItemSelection");

		try{
			if(listable != this) { 
				listable.verifyItemSelection(index, expectSelected);
			}else{
				String msg = debugmsg +" listable(this) infinite loop prevention!";
				IndependantLog.error(msg);
				throw new SeleniumPlusException(msg);
			}
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "verifyItemSelection", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}

	}

	/**
	 * Get all items of the listview.
	 */
	public Item[] getContent() throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(this.getClass(), "getContent");

		try{
			if(listable != this) { 
				return listable.getContent();
			}else{
				String msg = debugmsg +" listable(this) infinite loop prevention!";
				IndependantLog.error(msg);
				throw new SeleniumPlusException(msg);
			}
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "getContent", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	public Element getMatchedElement(TextMatchingCriterion criterion) throws SeleniumPlusException {
		String debugmsg = StringUtils.debugmsg(this.getClass(), "getMatchedElement");

		try{
			if(listable != this) { 
				return listable.getMatchedElement(criterion);
			}else{
				String msg = debugmsg +" listable(this) infinite loop prevention!";
				IndependantLog.error(msg);
				throw new SeleniumPlusException(msg);
			}
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "getMatchedElement", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}

	static class SapSelectable_ListBox extends AbstractListSelectable{
		public static final String CLASS_NAME_LISTBOX 		= "sap.ui.commons.ListBox";
		public static final String[] supportedClazzes = {CLASS_NAME_LISTBOX};

		public SapSelectable_ListBox(Component component) throws SeleniumPlusException {
			super(component);
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.model.Supportable#getSupportedClassNames()
		 */
		public String[] getSupportedClassNames() {
			return supportedClazzes;
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.model.ISelectable#getContent()
		 */
		public Item[] getContent() throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getContent");
			List<Item> items = new ArrayList<Item>();

			try {
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(SAP.sap_ui_commons_ListBox_getItems(true));
				jsScript.append(" return sap_ui_commons_ListBox_getItems(arguments[0]);");
				Object result = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());

				if(result instanceof List){
					Object[] objects = ((List<?>) result).toArray();
					Item item = null;
					for(Object object:objects){
						if(object!=null){
							item = new Item(object);
							items.add(item);
						}
					}
				}else if(result!=null){
					IndependantLog.warn("Need to hanlde javascript result whose type is '"+result.getClass().getName()+"'!");
					IndependantLog.warn("javascript result:\n"+result);
				}
			} catch(Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}

			return items.toArray(new Item[0]);
		}

		protected void clickElement(Element element, Keys key, Point offset, int mouseButtonNumber, int numberOfClick) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(getClass(), "clickElement");
			try{
				super.clickElement(element, key, offset, mouseButtonNumber, numberOfClick);
			}catch(SeleniumPlusException spe){
				if(spe.getCode()==SeleniumPlusException.CODE_OBJECT_IS_NULL) throw spe;

				Item item = convertToItem(element);
				if(item.getIndex()==Item.INVALID_INDEX) throw new SeleniumPlusException("Item's index is invalid, cannot click.");

				try {
					//Select by index
					StringBuffer jsScript = new StringBuffer();
					jsScript.append(SAP.sap_ui_commons_ListBox_setSelectedIndex(true));
					jsScript.append(" return sap_ui_commons_ListBox_setSelectedIndex(arguments[0], arguments[1]);");
					WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement(), item.getIndex());
				} catch(Exception e) {
					IndependantLog.error(debugmsg+" Met exception.",e);
					throw new SeleniumPlusException("Fail to select index '"+item.getIndex()+"'. due to '"+e.getMessage()+"'");
				}
			}
		}

		protected void showOnPage(Element element) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(this.getClass(), "showOnPage");
			WDLibrary.checkNotNull(element);

			Item item = convertToItem(element);

			if(item.getIndex()==Item.INVALID_INDEX) throw new SeleniumPlusException("Item's index is invalid, cannot scroll.");

			//Scroll by index
			try {
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(SAP.sap_ui_commons_ListBox_scrollToIndex(true));
				jsScript.append(" return sap_ui_commons_ListBox_scrollToIndex(arguments[0], arguments[1]);");
				WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement(), item.getIndex());
			} catch(Exception e) {
				IndependantLog.warn(debugmsg+"Fail to scroll to index '"+item.getIndex()+"'. Met "+StringUtils.debugmsg(e));
				//Try the general method provided by the superclass
				super.showOnPage(element);
			} 
		}

		protected void verifyItemSelected(Element element) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(getClass(), "verifyItemSelected");
			WDLibrary.checkNotNull(element);

			Item item = convertToItem(element);
			if(item.getIndex()==Item.INVALID_INDEX) throw new SeleniumPlusException("Item's index is invalid, cannot verify.");

			//Verify by index
			int index = item.getIndex();
			boolean selected = false;
			Object result = null;
			try {
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(SAP.sap_ui_commons_ListBox_getSelectedIndices(true));
				jsScript.append(" return sap_ui_commons_ListBox_getSelectedIndices(arguments[0]);");
				result = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());
				if(result instanceof List){
					List<?> indicesList = (List<?>) result;
					for(Object selectedIndex: indicesList){
						if(Integer.parseInt(selectedIndex.toString())==index){
							selected = true;
							break;
						}
					}
				}else{
					IndependantLog.error(debugmsg+"need handle result of type "+result.getClass().getName());
				}
			} catch(Exception e) {
				IndependantLog.error(debugmsg+" Met exception.",e);
				throw new SeleniumPlusException("Fail to verify selection of item '"+item+"' due to '"+e.getMessage()+"'");
			}

			if(!selected){
				IndependantLog.warn(debugmsg+" selectedIndices '"+result+"' doesn't contain index '"+index+"'");
				super.verifyItemSelected(element);
			}
		}
	}

	static class SapSelectable_m_List extends AbstractListSelectable{
		public static final String CLASS_NAME_M_LISTBASE 	= "sap.m.ListBase";
		public static final String CLASS_NAME_M_LIST 		= "sap.m.List";//child of sap.m.ListBase
		public static final String CLASS_NAME_SAS_HC_LIST 	= "sas.hc.m.List";//child of sap.m.List
		public static final String[] supportedClazzes = {CLASS_NAME_M_LISTBASE};

		public SapSelectable_m_List(Component component) throws SeleniumPlusException {
			super(component);
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.model.Supportable#getSupportedClassNames()
		 */
		public String[] getSupportedClassNames() {
			return supportedClazzes;
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.model.ISelectable#getContent()
		 */
		public Item[] getContent() throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getContent");
			List<Item> items = new ArrayList<Item>();

			try {
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(SAP.sap_m_List_getItems(true));
				jsScript.append(" return sap_m_List_getItems(arguments[0]);");
				Object result = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());

				if(result instanceof List){
					Object[] objects = ((List<?>) result).toArray();
					Item item = null;
					for(Object object:objects){
						if(object!=null){
							item = new Item(object);
							items.add(item);
						}
					}
				}else if(result!=null){
					IndependantLog.warn("Need to hanlde javascript result whose type is '"+result.getClass().getName()+"'!");
					IndependantLog.warn("javascript result:\n"+result);
				}
			} catch(Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}

			return items.toArray(new Item[0]);
		}

		protected void clickElement(Element element, Keys key, Point offset, int mouseButtonNumber, int numberOfClick) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(getClass(), "clickElement");
			try{
				super.clickElement(element, key, offset, mouseButtonNumber, numberOfClick);
			}catch(SeleniumPlusException spe){
				if(spe.getCode()==SeleniumPlusException.CODE_OBJECT_IS_NULL) throw spe;

				String id = element.getId();
				if(id==null || id.trim().isEmpty()) throw new SeleniumPlusException("Item's id is invalid, cannot click.");

				try {
					//Select by id
					StringBuffer jsScript = new StringBuffer();
					jsScript.append(SAP.sap_m_List_setSelectedItemById(true));
					jsScript.append(" return sap_m_List_setSelectedItemById(arguments[0], arguments[1]);");
					WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement(), id);
				} catch(Exception e) {
					IndependantLog.error(debugmsg+" Met exception.",e);
					throw new SeleniumPlusException("Fail to select item '"+element.getLabel()+"' by id '"+id+"'. due to '"+e.getMessage()+"'");
				}
			}
		}

		protected void verifyItemSelected(Element element) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(getClass(), "verifyItemSelected");
			WDLibrary.checkNotNull(element);

			boolean selected = false;
			Object result = null;
			//selectedItems is used for logging message, content is something like ['item1Label','item2Label','item3Label']
			StringBuffer selectedItems = new StringBuffer();
			try {
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(SAP.sap_m_List_getSelectedItems(true));
				jsScript.append(" return sap_m_List_getSelectedItems(arguments[0]);");
				result = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());

				if(result instanceof List){
					if(!((List<?>)result).isEmpty()){
						Object[] objects = ((List<?>) result).toArray();
						Item selectedItme = null;
						selectedItems.append("[");
						for(Object object:objects){
							if(object!=null){
								selectedItme = new Item(object);
								if(element.equals(selectedItme)){
									selected = true;
									break;
								}else{
									selectedItems.append("'"+selectedItme.getLabel()+"', ");
								}
							}
						}
						if(!selected){
							int lastCommaIndex = selectedItems.lastIndexOf(",");
							if(lastCommaIndex>0) selectedItems.replace(lastCommaIndex, lastCommaIndex+1, "");
							selectedItems.append("]");
						}
					}
				}else if(result!=null){
					IndependantLog.warn("Need to hanlde javascript result whose type is '"+result.getClass().getName()+"'!");
					IndependantLog.warn("javascript result:\n"+result);
				}

			} catch(Exception e) {
				IndependantLog.error(debugmsg+" Met exception.",e);
				throw new SeleniumPlusException("Fail to verify selection of item '"+element+"' due to '"+e.getMessage()+"'");
			}

			if(!selected){
				IndependantLog.warn(debugmsg+" selectedItems "+selectedItems+" doesn't contain item '"+element.getLabel()+"'");
				super.verifyItemSelected(element);
			}
		}
	}

	static class GenericSelectableList extends AbstractListSelectable{
		public static final String CLASS_NAME_UL_LIST 	= "ul";
		public static final String CLASS_NAME_OL_LIST	= "ol";//child of sap.m.ListBase
		public static final String[] supportedClazzes = {CLASS_NAME_UL_LIST, CLASS_NAME_OL_LIST};

		public GenericSelectableList(Component component) throws SeleniumPlusException {
			super(component);
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.model.Supportable#getSupportedClassNames()
		 */
		public String[] getSupportedClassNames() {
			return supportedClazzes;
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.model.ISelectable#getContent()
		 */
		public Item[] getContent() throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getContent");
			List<Item> items = new ArrayList<Item>();

			try {
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(JavaScriptFunctions.GENERIC.generic_getListItems());
				jsScript.append(" return generic_getListItems(arguments[0]);");
				Object result = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());

				if(result instanceof List){
					Object[] objects = ((List<?>) result).toArray();
					Item item = null;
					for(Object object:objects){
						if(object!=null){
							item = new Item(object);
							items.add(item);
						}
					}
				}else if(result!=null){
					IndependantLog.warn("Need to handle javascript result whose type is '"+result.getClass().getName()+"'!");
					IndependantLog.warn("javascript result:\n"+result);
				}
			} catch(Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}

			return items.toArray(new Item[0]);
		}

	}

	static class SapSelectable_m_SelectList extends AbstractListSelectable{
		public static final String CLASS_NAME_M_SELECTLIST 		= "sap.m.SelectList";
		public static final String CLASS_NAME_SAS_HC_SELECTLIST = "sas.hc.m.SelectList"; //child of sap.m.SelectList
		public static final String[] supportedClazzes = {CLASS_NAME_M_SELECTLIST};

		public SapSelectable_m_SelectList(Component component) throws SeleniumPlusException {
			super(component);
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.model.Supportable#getSupportedClassNames()
		 */
		public String[] getSupportedClassNames() {
			return supportedClazzes;
		}
	
		protected void clickElement(Element element, Keys key, Point offset, int mouseButtonNumber, int numberOfClick) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(getClass(), "clickElement");
			try{
				super.clickElement(element, key, offset, mouseButtonNumber, numberOfClick);
			}catch(SeleniumPlusException spe){
				if(spe.getCode()==SeleniumPlusException.CODE_OBJECT_IS_NULL) throw spe;

				String id = element.getId();
				if(id==null || id.trim().isEmpty()) throw new SeleniumPlusException("Item's id is invalid, cannot click.");

				try {
					//Select by id
					StringBuffer jsScript = new StringBuffer();
					jsScript.append(SAP.sap_m_SelectList_setSelectedItemById(true));
					jsScript.append(" return sap_m_SelectList_setSelectedItemById(arguments[0], arguments[1]);");
					WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement(), id);
				} catch(Exception e) {
					IndependantLog.error(debugmsg+" Met exception.",e);
					throw new SeleniumPlusException("Fail to select item '"+element.getLabel()+"' by id '"+id+"'. due to '"+e.getMessage()+"'");
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.model.ISelectable#getContent()
		 */
		public Item[] getContent() throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getContent");
			List<Item> items = new ArrayList<Item>();

			try {
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(SAP.sap_m_SelectList_getItems(true));
				jsScript.append(" return sap_m_SelectList_getItems(arguments[0]);");
				Object result = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());

				if(result instanceof List){
					Object[] objects = ((List<?>) result).toArray();
					Item item = null;
					for(Object object:objects){
						if(object!=null){
							item = new Item(object);
							items.add(item);
						}
					}
				}else if(result!=null){
					IndependantLog.warn("Need to hanlde javascript result whose type is '"+result.getClass().getName()+"'!");
					IndependantLog.warn("javascript result:\n"+result);
				}
			} catch(Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}

			return items.toArray(new Item[0]);
		}
		
		protected void verifyItemSelected(Element element) throws SeleniumPlusException {
			String debugmsg = StringUtils.debugmsg(getClass(), "verifyItemSelected");
			WDLibrary.checkNotNull(element);

			boolean selected = false;
			Object result = null;
			//selectedItems is used for logging message, content is something like ['item1Label','item2Label','item3Label']
			StringBuffer selectedItems = new StringBuffer();
			try {
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(SAP.sap_m_SelectList_getSelectedItems(true));
				jsScript.append(" return sap_m_SelectList_getSelectedItems(arguments[0]);");
				result = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());

				if(result instanceof List){
					if(!((List<?>)result).isEmpty()){
						Object[] objects = ((List<?>) result).toArray();
						Item selectedItem = null;
						selectedItems.append("[");
						for(Object object:objects){
							if(object!=null){
								selectedItem = new Item(object);
								if(element.equals(selectedItem)){
									selected = true;
									break;
								}else{
									selectedItems.append("'"+selectedItem.getLabel()+"', ");
								}
							}
						}
						if(!selected){
							int lastCommaIndex = selectedItems.lastIndexOf(",");
							if(lastCommaIndex>0) selectedItems.replace(lastCommaIndex, lastCommaIndex+1, "");
							selectedItems.append("]");
						}
					}
				}else if(result!=null){
					IndependantLog.warn("Need to handle javascript result whose type is '"+result.getClass().getName()+"'!");
					IndependantLog.warn("javascript result:\n"+result);
				}

			} catch(Exception e) {
				IndependantLog.error(debugmsg+" Met exception.",e);
				throw new SeleniumPlusException("Fail to verify selection of item '"+element+"' due to '"+e.getMessage()+"'");
			}

			if(!selected){
				IndependantLog.warn(debugmsg+" selectedItems "+selectedItems+" doesn't contain item '"+element.getLabel()+"'");
				super.verifyItemSelected(element);
			}
		}
	}
}

