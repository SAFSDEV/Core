/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 * History:
 * 
 *  DEC 20, 2013    (sbjlwa) Initial release.
 *  JAN 15, 2014    (sbjlwa) Update to support Dojo combo box (FilteringSelect, ComboBox, and Select)
 *  SEP 02, 2014    (LeiWang) Update to support sap.m.Select, sap.m.ComboBox
 *  OCT 16, 2015    (sbjlwa) Refector to create IOperable object properly.
 *  OCT 29, 2015    (sbjlwa) Modify HtmlSelect.setSelected(): refresh after selection.
 */
package org.safs.selenium.webdriver.lib;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.util.JavaScriptFunctions.DOJO;
import org.safs.selenium.util.JavaScriptFunctions.SAP;
import org.safs.selenium.webdriver.lib.model.EmbeddedObject;
import org.safs.selenium.webdriver.lib.model.IOperable;
import org.safs.selenium.webdriver.lib.model.Option;

/** 
 * A library class to handle different specific ComboBox.
 */
public class ComboBox extends Component{

	public static final String ITEM_SEPARATOR = ";";

	//traditional HTML tag <select>
	public static final String CLASS_HTML_SELECT = HtmlSelect.CLASS_NAME;
	
	//dijit/form/FilteringSelect, is a HTML tag <div>, a popup is associated
	//https://dojotoolkit.org/documentation/tutorials/1.9/selects/demo/FilteringSelect.php
	
	//dijit/form/ComboBox, is a HTML tag <div>, a popup is associated
	//https://dojotoolkit.org/documentation/tutorials/1.9/selects/demo/ComboBox.php
	public static final String CLASS_DOJO_COMBOBOX = DojoSelect_ComboBox.CLASS_DOJO_COMBOBOX;
	
	//dijit/form/Select, is a HTML tag <table>, a popup is associated
	//https://dojotoolkit.org/documentation/tutorials/1.9/selects/demo/Select.php
	public static final String CLASS_DOJO_SELECT =  DojoSelect_Select.CLASS_DOJO_SELECT;
	
	public static final int DEFAULT_MAX_REFRESH_TIMES = 3;
	public static int MAX_REFRESH_TIMES = DEFAULT_MAX_REFRESH_TIMES;
	
	// Determine if need to force refreshing: true,  force refreshing after selecting items,
    //										  false, not force refreshing
	// Set it as 'false' by default, because mostly the 'id' of web element is not dynamic, which doesn't need refresh.
	private static boolean _forceRefresh = false; 
	
	
	public boolean getForceRefresh() {
		return _forceRefresh;
	}

	public void setForceRefresh(boolean forceRefresh) {
		_forceRefresh = forceRefresh;
	}

	Selectable select = null;
	
	/**
	 * @param combobox	WebElement combo box object, for example an HTML tag &lt;select&gt;.
	 */
	public ComboBox(WebElement combobox) throws SeleniumPlusException{
		initialize(combobox);
	}

	protected void castOperable(){
		super.castOperable();
		select = (Selectable) anOperableObject;
	}
	
	protected IOperable createDOJOOperable(){
		String debugmsg = StringUtils.debugmsg(false);
		Selectable operable = null;
		try{ operable = new DojoSelect_ComboBox(this); }catch(SeleniumPlusException e){
			IndependantLog.debug(debugmsg+" Cannot create Selectable of "+Arrays.toString(DojoSelect_ComboBox.supportedClazzes));
		}
		try{ if(operable==null) operable = new DojoSelect_FilteringSelect(this); }catch(SeleniumPlusException e1){
			IndependantLog.debug(debugmsg+" Cannot create Selectable of "+Arrays.toString(DojoSelect_FilteringSelect.supportedClazzes));
		}
		try{ if(operable==null) operable = new DojoSelect_Select(this); }catch(SeleniumPlusException e2){
			IndependantLog.debug(debugmsg+" Cannot create Selectable of "+Arrays.toString(DojoSelect_Select.supportedClazzes));
		}
		return operable;
	}
	protected IOperable createSAPOperable(){
		String debugmsg = StringUtils.debugmsg(false);
		Selectable operable = null;
		try{ operable = new SapSelect_ComboBox(this); }catch(SeleniumPlusException e){
			IndependantLog.debug(debugmsg+" Cannot create Selectable of "+Arrays.toString(SapSelect_ComboBox.supportedClazzes), e);
		}
		return operable;
	}
	protected IOperable createHTMLOperable(){
		String debugmsg = StringUtils.debugmsg(false);
		Selectable operable = null;
		try{ operable = new HtmlSelect(this); }catch(SeleniumPlusException e){
			IndependantLog.debug(debugmsg+" Cannot create Selectable of "+Arrays.toString(HtmlSelect.supportedClazzes), e);
		}
		return operable;
	}
	
	public List<String> getDataList() throws SeleniumPlusException{
		List<String> visibleTextList = new ArrayList<String>();
		
		for(Option option:select.getOptions()){
			visibleTextList.add(option.getLabel());
		}
		
		return visibleTextList;
	}
	
	public void hidePopup() throws SeleniumPlusException{
		select.hidePopup();
	}
	
	public void showPopup() throws SeleniumPlusException{
		select.showPopup();
	}
	
	/**
	 * Select the combo box by text.<br>
	 * First, try to check if the text exists in the combo box; if not found, a {@link ComboBoxException}<br>
	 * will be thrown out with {@link ComboBoxException#CODE_NO_MATCHING_ITEM}.<br>
	 * Then, try to select the text in the combo box.<br>
	 * Finally, try to verify that the text has been selected; if not selected, a {@link ComboBoxException}<br>
	 * will be thrown out with {@link ComboBoxException#CODE_NOTHING_SELECTED} or {@link ComboBoxException#CODE_FAIL_VERIFICATION}.<br>
	 * 
	 * @param item	String, the text item to select.
	 * @param verify boolean, if verification of the selected item is needed.
	 * @param partialMatch	boolean, if the item is part of the option to select.
	 * @param cleanSelectedItems	boolean, if true clean the selected items before selection.
	 *                              this parameter take effect only if the combo box permit multiple selection.
	 * @return	List<String>, the selected options.
	 * @throws SeleniumPlusException
	 * @see #checkComboBox(String)
	 * @see #getSelectedOptions(HtmlSelect)
	 * @see #verifySelectedText(HtmlSelect, String, boolean)
	 */
	public List<String> select(String item, boolean verify, boolean partialMatch, boolean cleanSelectedItems) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(this.getClass(), "select");
		String optionToSelect = null;
		List<String> options = null;
		List<String> selectedOptions = null;
		
		try{
			//Check if the item exists in the options of the combo box
			options = select.getOptionsVisibleText();
			for(String option:options){
				if(option!=null){
					if((partialMatch && option.indexOf(item)>-1) || (!partialMatch && option.equals(item))){
						optionToSelect = option;
						break;
					}
				}
			}
			
			if(optionToSelect==null){
				String msg= "Cannot find an option matching text '"+item+"'";
				IndependantLog.error(debugmsg+msg);
				throw new ComboBoxException(msg, ComboBoxException.CODE_NO_MATCHING_ITEM);
			}
			
			if(cleanSelectedItems && select.isMultiple()) select.deselectAll();
			IndependantLog.debug(debugmsg+"Trying to select item '"+optionToSelect+"'");
			//Select all options that have a value matching the argument
			//select.selectByValue(optionToSelect);//foo <option value="foo">Bar</option> 
			select.selectByVisibleText(optionToSelect);//Bar <option value="foo">Bar</option> 
			
			selectedOptions = getSelectedOptions(select);
			if(verify) verifySelectedText(select, item, partialMatch);
			
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "select", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
		
		return selectedOptions;
	}
	
	/**
	 * Select the combo box by index.<br>
	 * First, try to check if the index is within the combo box; if not, a {@link ComboBoxException} <br>
	 * will be thrown out with {@link ComboBoxException#CODE_INDEX_OUTOF_RANGE}.<br>
	 * Then, try to select the index in the combo box.<br>
	 * Finally, try to verify that the item of index has been selected; if not selected, a {@link ComboBoxException}<br>
	 * will be thrown out with {@link ComboBoxException#CODE_NOTHING_SELECTED} or {@link ComboBoxException#CODE_FAIL_VERIFICATION}.<br>
	 * 
	 * @param index	int, the index of the option to select, 0 based.
	 * @param verify boolean, if verification of the selected item is needed.
	 * @param cleanSelectedItems	boolean, clean the selected items before new selection if true.
	 *                              this parameter take effect only if the combo box permit multiple selection.	 * 
	 * @return	List<String>, the selected options.
	 * @throws SeleniumPlusException
	 * @see #checkComboBox(String)
	 * @see #getSelectedOptions(HtmlSelect)
	 */
	public List<String> selectIndex(int index, boolean verify, boolean cleanSelectedItems) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(this.getClass(), "selectIndex");
		int selectedIndex = 0;
		List<String> allOptions = null;
		List<String> selectedOptions = null;
		
		try{
			//check the index
			if(index<0 || index>=select.getOptions().size()){
				String msg = "The index '"+index+"' is out of range.";
				IndependantLog.error(debugmsg+msg);
				throw new ComboBoxException(msg, ComboBoxException.CODE_INDEX_OUTOF_RANGE);
			}
			
			if(cleanSelectedItems && select.isMultiple()) select.deselectAll();
			IndependantLog.debug(debugmsg+"Trying to select item by index '"+index+"'");
			//Select the option at the given index. 
			select.selectByIndex(index);
			
			selectedOptions = getSelectedOptions(select);
			if(verify){
				allOptions = select.getOptionsVisibleText();
				
				outerLoop:
				for(String option:allOptions){
					if(option!=null){
						for(String seleted:selectedOptions){
							if(option.equals(seleted)) break outerLoop;
						}
					}
					selectedIndex++;
				}
				
				if(selectedIndex!=index){
					String msg= "Selected option index '"+selectedIndex+"' does not equal to index '"+index+"'";
					IndependantLog.debug(debugmsg+msg);
					throw new ComboBoxException(msg, ComboBoxException.CODE_FAIL_VERIFICATION, String.valueOf(selectedIndex));
				}
			}
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "selectIndex", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
		
		return selectedOptions;
	}
	
	/**
	 * Verify that the item has been selected in the combo box, if not a {@link SeleniumPlusException} will be <br>
	 * thrown out with {@link ComboBoxException#CODE_NOTHING_SELECTED} or {@link ComboBoxException#CODE_FAIL_VERIFICATION}<br>
	 * 
	 * @param item	String the text excepted being selected
	 * @return	List<String>, the selected options.
	 * @throws SeleniumPlusException
	 * @see #checkComboBox(WebElement)
	 * @see #verifySelectedText(HtmlSelect, String, boolean)
	 */
	public List<String> verifySelected(String item) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(this.getClass(), "verifySelected");
		
		try{
			IndependantLog.debug(debugmsg+"verifying that '"+item+"' has been selected.");
			return verifySelectedText(select, item, false);
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(this.getClass(), "verifySelected", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}
	
	public Object getItem(int index) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(this.getClass(), "getItem");
		
		try{
			return select.getItemByIndex(index);
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(ComboBox.class, "getItem", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
	}
	
	/**
	 * 
	 * @param select Select, the Select object
	 * @param item	String, the text excepted being selected
	 * @param partialMatch	boolean if true, item can be substring of the selected option.
	 * @return	List<String>, the selected options.
	 * @throws SeleniumPlusException
	 * @see {@link #getSelectedOptions(HtmlSelect)}
	 */
	public static List<String> verifySelectedText(Selectable select, String item, boolean partialMatch) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(ComboBox.class, "verifySelectedText");
		List<String> selectedOptions = null;
		boolean verified = false;
		
		try{
			selectedOptions = getSelectedOptions(select);
			//selectedItems will contain all the selected item, separated by semi-comma ;
			StringBuffer selectedItems = new StringBuffer();
			for(String option:selectedOptions){
				if(option!=null){
					selectedItems.append(option+ITEM_SEPARATOR);
					if((partialMatch && option.indexOf(item)>-1) || (!partialMatch && option.equals(item))){
						verified = true;
						break;
					}
				}
			}
			
			if(!verified){
				String items = selectedItems.toString();
				int index = items.lastIndexOf(ITEM_SEPARATOR);
				if(index>0) items = items.substring(0, index);
				String msg = "None of selected options '"+items+"' can match text '"+item+"'";
				IndependantLog.debug(debugmsg+msg);
				throw new ComboBoxException(msg, ComboBoxException.CODE_FAIL_VERIFICATION, items);
			}
		}catch(Exception e){
			if(e instanceof SeleniumPlusException) throw (SeleniumPlusException)e;
			else{
				debugmsg = StringUtils.debugmsg(ComboBox.class, "verifySelectedText", e);
				IndependantLog.error(debugmsg, e);
				throw new SeleniumPlusException(debugmsg);
			}
		}
		
		return selectedOptions;		
	}
	
	/**
	 * Get all the selected options from the combo box, <br>
	 * meanwhile it will CHECK that something has been selected in the combo box.<br>
	 * 
	 * @param select	Select	the Selenium Select object
	 * @return	List<String>, the selected options.
	 * @throws SeleniumPlusException will be thrown out with code {@link ComboBoxException#CODE_NOTHING_SELECTED} if nothing has been selected
	 */
	public static List<String> getSelectedOptions(Selectable select) throws SeleniumPlusException{
		String debugmsg = StringUtils.debugmsg(ComboBox.class, "getSelectedOptions");
		
		List<String> selectedOptions = select.getAllSelectedOptionsText();
		if(selectedOptions==null || selectedOptions.isEmpty()){
			String msg = "Nothing is selected.";
			IndependantLog.error(debugmsg+msg);
			throw new SeleniumPlusException(msg, ComboBoxException.CODE_NOTHING_SELECTED);
		}
		return selectedOptions;
	}
	
	/**
	 * This interface wraps different kinds of ComboBox, and it provides the uniform methods.<br>
	 * In the class ComboBox, we call these uniform methods.<br>
	 * 
	 *  <br>   Jan 15, 2014    (sbjlwa) Initial release.
	 *  
	 * @see ComboBox#getSelect(WebElement)
	 */
	public interface Selectable extends IOperable{
		
		/**
		 * @param index int, the index for an option, 0-based.
		 * @return Option, the option object according to the index
		 */
		public Option getItemByIndex(int index);
		
		/**
		 * @return boolean, Whether this select element support selecting multiple options at the same time?
		 */
		public boolean isMultiple();
	
		/**
		 * @return All options belonging to this combo box.
		 */
		public List<Option> getOptions();
	
		/**
		 * @return List<String>, All the options text.
		 */
		public List<String> getOptionsVisibleText();
		
		/**
		 * @return List<String>, All the options value.
		 */	
		public List<String> getOptionsValue();

		/**
		 * @param index int, the index for an option, 0-based.
		 * @return String, the option text according to the index
		 */
		public String getOptionVisibleText(int index);
		
		/**
		 * @param index int, the index for an option, 0-based.
		 * @return String, the option value according to the index
		 */
		public String getOptionValue(int index);
		
		/**
		 * @return All selected options belonging to this combo box.
		 */
		public List<Option> getAllSelectedOptions();
		
		/**
		 * @return List<String>, All the selected options text.
		 */	
		public List<String> getAllSelectedOptionsText();
	
		/**
		 * @return The first selected option in this combo box.
		 */
		public Option getFirstSelectedOption();
	
		/**
		 * Select all options that display text matching the argument. That is, when given "Bar" this
		 * would select an option like:
		 * 
		 * &lt;option value="foo"&gt;Bar&lt;/option&gt;
		 * 
		 * @param text The visible text to match against
		 */
		public void selectByVisibleText(String text);
	
		/**
		 * Select the option at the given index. 
		 * 
		 * @param index The option at this index will be selected, 0-based.
		 */
		public void selectByIndex(int index);
	
		/**
		 * Select all options that have a value matching the argument. That is, when given "foo" this
		 * would select an option like:
		 * 
		 * &lt;option value="foo"&gt;Bar&lt;/option&gt;
		 * 
		 * @param value The value to match against
		 */
		public void selectByValue(String value);
	
		/**
		 * Clear all selected entries. This is only valid when this combo box supports multiple selections.
		 * 
		 * @throws UnsupportedOperationException If the SELECT does not support multiple selections
		 */
		public void deselectAll();
	
		/**
		 * Deselect all options that have a value matching the argument. That is, when given "foo" this
		 * would deselect an option like:
		 * 
		 * &lt;option value="foo"&gt;Bar&lt;/option&gt;
		 * 
		 * @param value The value to match against
		 */
		public void deselectByValue(String value);
	
		/**
		 * Deselect the option at the given index.
		 * 
		 * @param index The option at this index will be deselected, 0-based.
		 */
		public void deselectByIndex(int index);
	
		/**
		 * Deselect all options that display text matching the argument. That is, when given "Bar" this
		 * would deselect an option like:
		 * 
		 * &lt;option value="foo"&gt;Bar&lt;/option&gt;
		 * 
		 * @param text The visible text to match against
		 */
		public void deselectByVisibleText(String text);
	
//		protected boolean isSelected(Object option);
//		
//		protected boolean selectOption(Object option);
//		
//		protected void setSelected(Object option);
		
		public void hidePopup() throws SeleniumPlusException;
		
		public void showPopup() throws SeleniumPlusException;
		
	}//End of Select Interface
	
	/**
	 * Modified from Source code of Selenium.<br>
	 * Models a SELECT tag, providing helper methods to select and deselect options.<br>
	 * @see Selectable
	 */
	protected static class HtmlSelect extends EmbeddedObject implements Selectable{
		/**traditional HTML tag &lt;select&gt;*/
		public static final String CLASS_NAME = TAG_HTML_SELECT;
		public static final String[] supportedClazzes = {CLASS_NAME};
		
		protected final boolean isMulti;
	
		public void clearCache(){
			String methodName = StringUtils.getCurrentMethodName(true);
			IndependantLog.debug(methodName+" has not been implemented.");
		}
		
		/**
		 * Constructor. It will call method {@link #isSupported(WebElement)} to see if element is supported.<br>
		 * If it is not,then an SeleniumPlusException is thrown with {@link SeleniumPlusException#CODE_TYPE_IS_WRONG}.<br>
		 * 
		 * @param webelement SELECT element to wrap, such as html tag &lt;select&gt;, dijit.form.ComboBox, or dijit.form.Select etc
		 * @throws SeleniumPlusException when element is not supported as a combo box.
		 */
		public HtmlSelect(Component component) throws SeleniumPlusException {
			super(component);
			String value = component.getAttribute(ATTRIBUTE_MULTIPLE);
			isMulti = (value != null && !"false".equals(value));
		}
	
		/**
		 * The element will be used to create an instance of class {@link HtmlSelect} or subclass. <br>
		 * In the constructor {@link HtmlSelect#HtmlSelect(WebElement)} or constructor of subclass,<br>
		 * this method will be called to test if the element is supported by this class {@link HtmlSelect} or subclass.<br>
		 * 
		 * This method will check if the tag name is 'select' or not, if yes then supported, otherwise not supported.<br>
		 */
		public boolean isSupported(WebElement element){
			return WDLibrary.HTML.isSupported(element, getSupportedClassNames());
		}
	
		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.Component.Supportable#getSupportedClassNames()
		 */
		public String[] getSupportedClassNames() {
			return supportedClazzes;
		}
		
		public Option getItemByIndex(int index){
			List<Option> options = getOptions();
			for (Option option : options) {
				if(index==option.getIndex()) return option;
			}
			throw new NoSuchElementException("Cannot locate option with index: " + index);
		}
		
		/**
		 * @return Whether this select element support selecting multiple options at the same time? This
		 *         is done by checking the value of the "multiple" attribute.
		 */
		public boolean isMultiple() {
			//isMulti is set in the constructor HtmlSelect()
			return isMulti;
		}

		/**
		 * @return All options belonging to this select tag
		 */
		public List<Option> getOptions() {
			List<Option> options = new ArrayList<Option>();
			
			List<WebElement> elements = webelement().findElements(By.tagName("option"));
			int i =0;
			for(WebElement option:elements){
				options.add(new Option(option).setIndex(i++));
			}
			
			return options;
		}
	
		/**
		 * @return List<String>, All the options text.
		 */
		public List<String> getOptionsVisibleText(){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getOptionsVisibleText");
			List<String> texts = new ArrayList<String>();
			
			try {
				String tempText = null;
				List<Option> options = getOptions();
				for(Option option:options){
					tempText = option.getLabel();
					if(tempText!=null) texts.add(tempText);
				}
			} catch (Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}
			
			return texts;
		}
		
		/**
		 * @return List<String>, All the options value.
		 */	
		public List<String> getOptionsValue(){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getOptionsValue");
			List<String> values = new ArrayList<String>();
			
			try {
				String tempText = null;
				List<Option> options = getOptions();
				for(Option option:options){
					tempText = option.getValue();
					if(tempText!=null) values.add(tempText);
				}
			} catch (Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}
			
			return values;
		}
		
		public String getOptionVisibleText(int index){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getOptionVisibleText");
			String visibleText = null;
			
			try {
				for(Option option:getOptions()){
					if(option.getIndex()==index){
						visibleText = option.getLabel();
					}
				}
				if(visibleText==null){
					IndependantLog.warn(debugmsg+"Cannot get option visible text for index '"+index+"'!");
				}
			} catch (Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}
			
			return visibleText;
		}
		
		public String getOptionValue(int index){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getOptionValue");
			String value = null;
			
			try {
				List<Option> options = getOptions();
				for(Option option:options){
					if(option.getIndex()==index){
						value = option.getValue();
					}
				}
				if(value==null){
					IndependantLog.warn(debugmsg+"Cannot get option value for index '"+index+"'!");
				}
			} catch (Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}
			
			return value;
		}
		
		/**
		 * @return All selected options belonging to this select tag
		 */
		public List<Option> getAllSelectedOptions() {
			List<Option> toReturn = new ArrayList<Option>();
	
			List<Option> options = getOptions();
			for (Option option : options) {
				if (option.isSelected()) {
					toReturn.add(option);
				}
			}
	
			return toReturn;
		}
		
		/**
		 * @return List<String>, All the selected options text.
		 */	
		public List<String> getAllSelectedOptionsText() {
			List<Option> options = getAllSelectedOptions();
			String tempText = null;
			List<String> texts = new ArrayList<String>();
			
			for(Option option:options){
				tempText = option.getLabel();
				if(tempText!=null) texts.add(tempText);
			}
			
			return texts;
		}
	
		/**
		 * @return The first selected option in this select tag (or the currently selected option in a
		 *         normal select)
		 */
		public Option getFirstSelectedOption() {
			List<Option> options = getOptions();
			for (Option option : options) {
				if(option.isSelected()) return option;
			}
	
			throw new NoSuchElementException("No options are selected");
		}
	
		/**
		 * Select all options that display text matching the argument. That is, when given "Bar" this
		 * would select an option like:
		 * 
		 * &lt;option value="foo"&gt;Bar&lt;/option&gt;
		 * 
		 * @param text The visible text to match against
		 */
		public void selectByVisibleText(String text) {
			// try to find the option via XPATH ...
			List<WebElement> options = webelement().findElements(By.xpath(".//option[normalize-space(.) = " + escapeQuotes(text) + "]"));
	
			boolean matched = false;
			for (WebElement option : options) {
				setSelected(option);
				if (!isMultiple()) {
					return;
				}
				matched = true;
			}
	
			if (options.isEmpty() && text.contains(" ")) {
				String subStringWithoutSpace = getLongestSubstringWithoutSpace(text);
				List<WebElement> candidates;
				if ("".equals(subStringWithoutSpace)) {
					// hmm, text is either empty or contains only spaces - get all options ...
					candidates = webelement().findElements(By.tagName("option"));
				} else {
					// get candidates via XPATH ...
					candidates =
							webelement().findElements(By.xpath(".//option[contains(., " +
									escapeQuotes(subStringWithoutSpace) + ")]"));
				}
				for (WebElement option : candidates) {
					if (text.equals(option.getText())) {
						setSelected(option);
						if (!isMultiple()) {
							return;
						}
						matched = true;
					}
				}
			}
	
			if (!matched) {
				throw new NoSuchElementException("Cannot locate element with text: " + text);
			}
		}
	
		protected String getLongestSubstringWithoutSpace(String s) {
			String result = "";
			StringTokenizer st = new StringTokenizer(s, " ");
			while (st.hasMoreTokens()) {
				String t = st.nextToken();
				if (t.length() > result.length()) {
					result = t;
				}
			}
			return result;
		}
	
		/**
		 * Select the option at the given index. This is done by examing the "index" attribute of an
		 * element, and not merely by counting.
		 * 
		 * @param index The option at this index will be selected
		 */
		public void selectByIndex(int index) {
			boolean matched = false;
			for (Object option : getOptions()) {
				if(option instanceof Option){
					if (((Option)option).getIndex()==index) {
						setSelected(option);
						if (!isMultiple()) {
							return;
						}
						matched = true;
					}
				}
			}
			if (!matched) {
				throw new NoSuchElementException("Cannot locate option with index: " + index);
			}
		}
	
		/**
		 * Select all options that have a value matching the argument. That is, when given "foo" this
		 * would select an option like:
		 * 
		 * &lt;option value="foo"&gt;Bar&lt;/option&gt;
		 * 
		 * @param value The value to match against
		 */
		public void selectByValue(String value) {
			StringBuilder builder = new StringBuilder(".//option[@value = ");
			builder.append(escapeQuotes(value));
			builder.append("]");
			List<WebElement> options = webelement().findElements(By.xpath(builder.toString()));
	
			boolean matched = false;
			for (WebElement option : options) {
				setSelected(option);
				if (!isMultiple()) {
					return;
				}
				matched = true;
			}
	
			if (!matched) {
				throw new NoSuchElementException("Cannot locate option with value: " + value);
			}
		}
	
		/**
		 * Clear all selected entries. This is only valid when the SELECT supports multiple selections.
		 * 
		 * @throws UnsupportedOperationException If the SELECT does not support multiple selections
		 */
		public void deselectAll() {
			if (!isMultiple()) {
				throw new UnsupportedOperationException(
						"You may only deselect all options of a multi-select");
			}
	
			Option option = null;
			for (Object optionObject: getOptions()) {
				if(optionObject instanceof Option){
					option = (Option) optionObject;
					if (option.isSelected()) {
						if(option.getWebElement() != null) option.getWebElement().click();
					}
				}
			}
		}
	
		/**
		 * Deselect all options that have a value matching the argument. That is, when given "foo" this
		 * would deselect an option like:
		 * 
		 * &lt;option value="foo"&gt;Bar&lt;/option&gt;
		 * 
		 * @param value The value to match against
		 */
		public void deselectByValue(String value) {
			StringBuilder builder = new StringBuilder(".//option[@value = ");
			builder.append(escapeQuotes(value));
			builder.append("]");
			List<WebElement> options = webelement().findElements(By.xpath(builder.toString()));
			for (WebElement option : options) {
				if (option.isSelected()) {
					option.click();
				}
			}
		}
	
		/**
		 * Deselect the option at the given index. This is done by examing the "index" attribute of an
		 * element, and not merely by counting.
		 * 
		 * @param index The option at this index will be deselected
		 */
		public void deselectByIndex(int index) {
			Option option = null;
			for (Object optionObject : getOptions()) {
				if(optionObject instanceof Option){
					option = (Option) optionObject;
					if ((index==option.getIndex()) &&option.isSelected()) {
						if(option.getWebElement() != null) option.getWebElement().click();
					}
				}
			}
		}
	
		/**
		 * Deselect all options that display text matching the argument. That is, when given "Bar" this
		 * would deselect an option like:
		 * 
		 * &lt;option value="foo"&gt;Bar&lt;/option&gt;
		 * 
		 * @param text The visible text to match against
		 */
		public void deselectByVisibleText(String text) {
			StringBuilder builder = new StringBuilder(".//option[normalize-space(.) = ");
			builder.append(escapeQuotes(text));
			builder.append("]");
			List<WebElement> options = webelement().findElements(By.xpath(builder.toString()));
			for (WebElement option : options) {
				if (option.isSelected()) {
					option.click();
				}
			}
		}
	
		protected String escapeQuotes(String toEscape) {
			// Convert strings with both quotes and ticks into: foo'"bar -> concat("foo'", '"', "bar")
			if (toEscape.indexOf("\"") > -1 && toEscape.indexOf("'") > -1) {
				boolean quoteIsLast = false;
				if (toEscape.lastIndexOf("\"") == toEscape.length() - 1) {
					quoteIsLast = true;
				}
				String[] substrings = toEscape.split("\"");
	
				StringBuilder quoted = new StringBuilder("concat(");
				for (int i = 0; i < substrings.length; i++) {
					quoted.append("\"").append(substrings[i]).append("\"");
					quoted
					.append(((i == substrings.length - 1) ? (quoteIsLast ? ", '\"')" : ")") : ", '\"', "));
				}
				return quoted.toString();
			}
	
			// Escape string with just a quote into being single quoted: f"oo -> 'f"oo'
			if (toEscape.indexOf("\"") > -1) {
				return String.format("'%s'", toEscape);
			}
	
			// Otherwise return the quoted string
			return String.format("\"%s\"", toEscape);
		}
	
		protected boolean isSelected(Object option){
			if(option instanceof Option){
				return ((Option)option).isSelected();
				
			}else if(option instanceof WebElement){
				return ((WebElement)option).isSelected();
			}
			return false;
		}
		
		protected boolean selectOption(Object option){
			WebElement webelement = null;
	
			try{
				if(option instanceof WebElement){
					webelement = ((WebElement)option);
	
				}else if(option instanceof Option){
					webelement = ((WebElement) ((Option)option).getEmbeddedObject());
					((Option)option).setSelected(true);
				}
				webelement.click();
				return true;
			}catch(Throwable th){}
			
			return false;
		}
		
		protected void setSelected(Object option) {
			if (!isSelected(option)) {
				WebElement preWebElement = webelement();
				selectOption(option);
				
				int repeatTimes = 0;
				// If force refreshing, the implicit assumption is the web element 'id' is dynamic, otherwise
				// we don't need to refresh. Thus, we can deduce that the web element we're dealing must change.
				// So we need to keep refresh until the previous web element is not equal to current web element.
				while(_forceRefresh && (preWebElement.equals(webelement())) && (repeatTimes++ < MAX_REFRESH_TIMES)){
					IndependantLog.debug("Force web page refreshing, refresh repeating times: '" + repeatTimes + "'");
					refresh(false);
					try { Thread.sleep(1000); } catch (InterruptedException ignore) {}
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.ComboBox.Select#hidePopup()
		 */
		public void hidePopup() throws SeleniumPlusException {
			try{
				try {
					webelement().sendKeys(Keys.ESCAPE);
				} catch (WebDriverException wde) {
					// In Chrome, the Selenium API may fail. Using Robot as workaround.
					IndependantLog.debug("Failed to use Selenium API to hide the combo-box's popup, try to use Robot API instead.");
					WDLibrary.inputKeys(webelement(), "{Esc}");
				}
			}catch(Throwable th){
				IndependantLog.error("Fail to hide the combo-box's popup.", th);
				throw new ComboBoxException("Fail to hide the combo-box's popup.",ComboBoxException.CODE_FAIL_CLOSE_POPUP);
			}
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.ComboBox.Select#showPopup()
		 */
		public void showPopup() throws SeleniumPlusException {
			try{
				//First hide the popup
				try {
					webelement().sendKeys(Keys.ESCAPE);
				} catch (WebDriverException wde) {
					// In Chrome, the Selenium API may fail. Using Robot as workaround.
					IndependantLog.debug("Failed to use Selenium API to hide the combo-box's popup, try to use Robot API instead.");
					WDLibrary.inputKeys(webelement(), "{Esc}");
				}
				//Then click the combo-box to show the popup
				webelement().click();
//				Then click the combo-box at the button of right side to show the popup
//				Dimension d = webelement().getSize();
//				//offset will be the location of the combobox-button
//				Point offset = new Point(d.width-d.height/2, d.height/2);
//				WDLibrary.click(webelement(), offset);
			}catch(Throwable th){
				IndependantLog.error("Fail to show the combo-box's popup.", th);
				throw new ComboBoxException("Fail to show the combo-box's popup.",ComboBoxException.CODE_FAIL_OPEN_POPUP);
			}
		}
	}

	protected static abstract class AbstractSelect extends HtmlSelect{
		/**
		 * @param webelement
		 * @throws SeleniumPlusException
		 */
		public AbstractSelect(Component component) throws SeleniumPlusException {
			super(component);
		}
		
		/**
		 * Select all options that display text matching the argument. That is, when given "Bar" this
		 * would select an option like:
		 * 
		 * &lt;option value="foo"&gt;Bar&lt;/option&gt;
		 * 
		 * @param text The visible text to match against, should be full string.
		 */
		public void selectByVisibleText(String text) {
			boolean matched = false;
	
			if(text!=null){
				List<Option> options = getOptions();
				
				for (Option option : options) {
					if(text.equals(option.getLabel())){
						setSelected(option);
						matched = true;
						break;
					}
				}
			}

			if (!matched) {
				throw new NoSuchElementException("Cannot locate element with text: " + text);
			}
		}
		
		/**
		 * Execute javascript function to get a set of option object, the set may be returned as List.<br>
		 * Normally the option javascript object will contain a few of properties, this object<br>
		 * may be returned as a java Map object.<br> 
		 * 
		 * @return Object, a set of option object.
		 * @see #getOptions()
		 */
		abstract protected Object getOptionsJSObject();
		
		/**
		 * @return All options belonging to this combo box
		 * @see #getOptionsJSObject()
		 */
		public List<Option> getOptions(){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getOptions");
			List<Option> options = new ArrayList<Option>();
	
			try {
				Object result = getOptionsJSObject();
	
				if(result instanceof List){
					Object[] objects = ((List<?>) result).toArray();
					Option option = null;
					for(Object object:objects){
						if(object!=null){
							option = new Option(object);
							//TODO if the options are returned in order as shown in the combo box,
							//we can set the index like this, otherwise, we need to know the order of these options.
//							option.setIndex(i++);//set the index in the javascript function
							options.add(option);
						}
					}
				}else if(result!=null){
					IndependantLog.warn("Need to hanlde javascript result whose type is '"+result.getClass().getName()+"'!");
					IndependantLog.warn("javascript result:\n"+result);
				}
			} catch(Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}
	
			return options;
		}
	}
	
	public static class SapSelect_ComboBox extends AbstractSelect{
		public static final String CLASS_NAME_COMBOBOX = "sap.ui.commons.ComboBox";
		public static final String CLASS_NAME_AUTOCOMPLETE = "sap.ui.commons.AutoComplete";//subclass of sap.ui.commons.ComboBox
		public static final String CLASS_NAME_DROPDOWNBOX = "sap.ui.commons.DropdownBox";//subclass of sap.ui.commons.ComboBox
		
		public static final String CLASS_NAME_M_SELECT = "sap.m.Select";
		public static final String CLASS_NAME_M_ACTIONSELECT = "sap.m.ActionSelect ";//subclass of sap.m.Select
		
		//sap.m.ComboBoxBase lack of some javascript API like getSelectedKey(), setSelectedKey(sKey), be careful in javascript code.
		public static final String CLASS_NAME_M_COMBOBOXBASE = "sap.m.ComboBoxBase";
		public static final String CLASS_NAME_M_COMBOBOX = "sap.m.ComboBox";//subclass of sap.m.ComboBoxBase
		
		/**
		 * <ul>
		 * <li>sap.ui.commons.ComboBox
		 * <li>sap.m.Select
		 * <li>sap.m.ComboBoxBase
		 * </ul>
		 */
		public static final String[] supportedClazzes = {CLASS_NAME_COMBOBOX,
			                                             CLASS_NAME_M_SELECT,
			                                             CLASS_NAME_M_COMBOBOXBASE};
		/**
		 * @param webelement
		 * @throws SeleniumPlusException
		 */
		public SapSelect_ComboBox(Component component) throws SeleniumPlusException {
			super(component);
		}
		
		/**
		 * @see org.safs.selenium.webdriver.lib.SapSelect#getSupportedClassNames()
		 */
		public String[] getSupportedClassNames() {
			return supportedClazzes;
		}
		
		/**
		 * @see org.safs.selenium.webdriver.lib.HtmlSelect#isSupported(WebElement)
		 */	
		public boolean isSupported(WebElement element){
			return WDLibrary.SAP.isSupported(element, getSupportedClassNames());
		}
		
		/**
		 * Select an option of a combo box.<br>
		 * @return boolean, true if the option has been selected successfully.
		 */
		protected boolean selectOption(Object optionObject){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "selectOption");
			
			if(optionObject instanceof Option){
				Option option = (Option) optionObject;
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(SAP.sap_ComboBox_setSelectedKey(true));
				jsScript.append("sap_ComboBox_setSelectedKey(arguments[0],arguments[1]);");
				
				try {
					WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement(), option.getValue());
					option.setSelected(true);
					return true;
				} catch(Exception e) {
					IndependantLog.debug(debugmsg+" Met exception.",e);
				}
			}
			return false;
		}
		
		/**
		 * Execute javascript function to get a set of option object.<br>
		 * Normally the option javascript object will contain a few of properties, this object<br>
		 * will be returned as a java Map object.<br> 
		 * 
		 * @return Object, a set of option object.
		 * @see #getOptions()
		 */
		protected Object getOptionsJSObject(){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getOptionsJSObject");
			
			try {
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(SAP.sap_ComboBox_getItems(true));
				
				jsScript.append("return sap_ComboBox_getItems(arguments[0]);");
				Object result = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());
				return result;
				
			} catch(Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}
			
			return null;
		}
		
		public void hidePopup() throws SeleniumPlusException {
			super.hidePopup();//input "Escape" key to hide the popup
		}
		
		public void showPopup() throws SeleniumPlusException {
			try{
				//First hide the popup
				try {
					webelement().sendKeys(Keys.ESCAPE);
				} catch (WebDriverException wde) {
					// In Chrome, the Selenium API may fail. Using Robot as workaround.
					IndependantLog.debug("Failed to use Selenium API to hide the combo-box's popup, try to use Robot API instead.");
					WDLibrary.inputKeys(webelement(), "{Esc}");
				}
				//Then click the combo-box at the button of right side to show the popup
				Dimension d = webelement().getSize();
				//offset will be the location of the combobox-button
				Point offset = new Point(d.width-d.height/2, d.height/2);
				WDLibrary.click(webelement(), offset);
			}catch(Throwable th){
				IndependantLog.error("Fail to show the combo-box's popup.", th);
				throw new ComboBoxException("Fail to show the combo-box's popup.",ComboBoxException.CODE_FAIL_OPEN_POPUP);
			}
		}
	}
	
	/**
	 * Models a dojo Combo Box.<br>
	 * @see Selectable
	 * @see HtmlSelect
	 */
	protected static abstract class DojoSelect extends AbstractSelect{
		/**
		 * popup is the popup menu associated with the combo box.<br>
		 * It can be used to handle each 'menu item' (the combo box option)<br>
		 * For now, it has not been used yet. Maybe it will be useful in some case in future.<br>
		 */
		protected WebElement popup = null;
	
		public DojoSelect(Component component) throws SeleniumPlusException {
			super(component);
			//TODO getPopup() will take some time to load the popup-menu. For now, we have not
			//used this popup-menu, to save time, comment the loading of it. If we really need
			//this popup-menu, we can un-comment the following code, or put the following line
			//in the sub-class
			//popup = getPopup();
		}
		
		/**
		 * @see org.safs.selenium.webdriver.lib.HtmlSelect#isSupported(WebElement)
		 */	
		public boolean isSupported(WebElement element){
			return WDLibrary.DOJO.isSupported(element, getSupportedClassNames());
		}
		
		/**
		 * Some DOJO combo-box has an associated popup-menu, which can be used to select/click/count item.<br>
		 * This function will click the combo-box's button to load and show the associated popup-menu, then<br>
		 * find the popup-menu.<br>
		 * @return WebElement, the popup-menu associated with this combo-box.
		 */
		protected WebElement getPopup(){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getPopup");
			
			try{
				//dijit/_HasDropDown.isLoaded() Returns true if the dropdown exists and it's data is loaded.
				//make sure that the popup has been loaded
				if(!dojo_HasDropDown_isLoaded()){
					if(dojo_HasDropDown_loadAndOpenDropDown()){
						dojo_HasDropDown_closeDropDown();
					}
				}
				
				//get the value of attribute 'widgetid' of the combo box.
				String widgetid = webelement().getAttribute(Component.ATTRIBUTE_WIDGETID);
				
				//then, get the popup whose dijitpopupparent attribute has the same value as value of attribute 'widgetid' of combobox
				WebElement popup = WDLibrary.findElement(By.cssSelector("[dijitpopupparent='"+widgetid+"']"));
				
				String id = popup.getAttribute(Component.ATTRIBUTE_ID);
				IndependantLog.debug(debugmsg+" Popup id is "+id);
				
				return popup;
			}catch(Throwable th){
				IndependantLog.debug(debugmsg, th);
			}
			
			return null;
		}
		
		/**
		 * Some Dojo Combo-box (dijit.form.ComboBox, dijit.form.Select etc.) contains a drop-down menu,<br>
		 * but the drop-down will not be loaded until you trigger it by click the combo-box-button or calling<br>
		 * combo-box API. This method is used to test if the associated drop-down has been shown or not.<br>
		 * @return boolean, true if the associated drop-down menu has been loaded.
		 */
		protected boolean dojo_HasDropDown_isLoaded(){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "dojo_HasDropDown_isLoaded");
			StringBuffer jsScript = new StringBuffer();
	
			jsScript.append(DOJO.dojo_HasDropDown_isLoaded(true));
			jsScript.append(" return dojo_HasDropDown_isLoaded(arguments[0]);");
			try {
				Object obj = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());
				if(obj instanceof Boolean) return ((Boolean)obj).booleanValue();
			} catch (SeleniumPlusException e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}
			return false;
		}
		
		/**
		 * Some Dojo Combo-box (dijit.form.ComboBox, dijit.form.Select etc.) contains a drop-down menu,<br>
		 * but the drop-down will not be loaded until you trigger it by click the combo-box-button or calling<br>
		 * combo-box API. This method is used to load the drop-down menu and open it.<br>
		 * @return boolean, true if the associated drop-down menu has been successfully loaded.
		 */
		protected boolean dojo_HasDropDown_loadAndOpenDropDown(){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "dojo_HasDropDown_loadAndOpenDropDown");
			StringBuffer jsScript = new StringBuffer();
			
			jsScript.append(DOJO.dojo_HasDropDown_loadAndOpenDropDown(true));
			jsScript.append("dojo_HasDropDown_loadAndOpenDropDown(arguments[0]);");
			try {
				WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());
				return true;
			} catch (SeleniumPlusException e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}
			return false;
		}
		
		/**
		 * Close the drop-down menu.<br>
		 * @return boolean, true if the associated drop-down menu has been closed.
		 */
		protected boolean dojo_HasDropDown_closeDropDown(){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "dojo_HasDropDown_closeDropDown");
			StringBuffer jsScript = new StringBuffer();
			
			jsScript.append(DOJO.dojo_HasDropDown_closeDropDown(true));
			jsScript.append("dojo_HasDropDown_closeDropDown(arguments[0]);");
			try {
				WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());
				return true;
			} catch (SeleniumPlusException e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}
			return false;
		}
		
		/**
		 * Open the drop-down menu.<br>
		 * @return boolean, true if the associated drop-down menu has been closed.
		 */
		protected boolean dojo_HasDropDown_openDropDown(){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "dojo_HasDropDown_openDropDown");
			StringBuffer jsScript = new StringBuffer();
			
			jsScript.append(DOJO.dojo_HasDropDown_openDropDown(true));
			jsScript.append("dojo_HasDropDown_openDropDown(arguments[0]);");
			try {
				WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());
				return true;
			} catch (SeleniumPlusException e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}
			return false;
		}

		/**
		 * Select an option of a combo box.<br>
		 * @return boolean, true if the option has been selected successfully.
		 */
		protected boolean selectOption(Object optionObject){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "selectOption");
	
			if(optionObject instanceof Option){
				Option option = (Option) optionObject;
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(DOJO.dojo_dijit_WidgetBase_set(true));
				jsScript.append("dojo_dijit_WidgetBase_set(arguments[0],arguments[1],arguments[2]);");
	
				try {
					WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement(), "value", option.getValue());
					option.setSelected(true);
					return true;
				} catch(Exception e) {
					IndependantLog.debug(debugmsg+" Met exception.",e);
				}
			}
			return false;
		}
		
		/**
		 * 
		 * @see AbstractSelect#getOptions()
		 * @see #getOptions()
		 */
		protected Object getOptionsJSObject(){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getOptionsJSObject");
			
			try {
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(DOJO.dojo_store_api_Store_query(true));
				//TODO, dojo_store_api_Store_query() will get the data from the model of the combo box.
				//hope that dojo_store_api_Store_query() will return options the same order as what is shown on page.
				//if not, we have to know the order of the options in the combo box.
			    //jsScript.append("return dojo_store_api_Store_query(arguments[0],arguments[1],arguments[2],arguments[3]);");
				jsScript.append("return dojo_store_api_Store_query(arguments[0]);");
				Object result = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());
				return result;
			
			} catch(Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}
			
			return null;
		}
		
		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.ComboBox.Select#hidePopup()
		 */
		public void hidePopup() throws SeleniumPlusException {
			if(!dojo_HasDropDown_closeDropDown()){
				throw new ComboBoxException("Fail to hide the combo-box's popup.",ComboBoxException.CODE_FAIL_CLOSE_POPUP);
			}
		}

		/* (non-Javadoc)
		 * @see org.safs.selenium.webdriver.lib.ComboBox.Select#showPopup()
		 */
		public void showPopup() throws SeleniumPlusException {
			boolean show = false;
			//Make sure to load the combo-box's data, otherwise openDropDown() can't open the popup!
			if(!dojo_HasDropDown_isLoaded()){
				show = dojo_HasDropDown_loadAndOpenDropDown();
			}else{
				show = dojo_HasDropDown_openDropDown(); 
			}
			if(!show){
				throw new ComboBoxException("Fail to show the combo-box's popup.",ComboBoxException.CODE_FAIL_OPEN_POPUP);
			}
		}
		
	}

	/**
	 * Models a dojo dijit/form/Select<br>
	 * @see Selectable
	 * @see HtmlSelect
	 * @see DojoSelect
	 */
	protected static class DojoSelect_Select extends DojoSelect{
		//dijit/form/Select, is a HTML tag <table>, a popup is associated
		//https://dojotoolkit.org/documentation/tutorials/1.9/selects/demo/Select.php
		public static final String CLASS_NAME = "dijit.form.Select";//<table>
		public static final String CLASS_DOJO_SELECT = "dijit dijitReset dijitInline dijitLeft dijitDownArrowButton dijitSelect dijitValidationTextBox";//<table>
		public static final String TAG_DOJO_SELECT = "table"; 
		public static final String CLASS_DOJO_SELECT_INPUT = "dijitReset dijitStretch dijitButtonContents";//<td> .textContent show the current text of combo box
		public static final String CLASS_DOJO_SELECT_BUTTON = "dijitReset dijitInputField dijitArrowButtonInner";//<input>
		
		public static final String CLASS_POPUP_NAME = "dijit.form._SelectMenu";
		public static final String CLASS_DOJO_SELECT_POPUP = "dijitPopup dijitMenuPopup";//<div>
		public static final String TAG_DOJO_SELECT_POPUP = "div";
		public static final String CLASS_DOJO_SELECT_MENU = "dijit dijitReset dijitMenuTable dijitSelectMenu dijitValidationTextBoxMenu dijitMenuPassive dijitMenu";//<table>
		public static final String CLASS_DOJO_SELECT_MENUITEM = "dijitReset dijitMenuItem";//<tr>
		public static final String CLASS_DOJO_SELECT_MENU_LABEL = "dijitReset dijitMenuItemLabel";//<td> .innerHTML .textContent
		
		public static final String[] supportedClazzes = {CLASS_NAME};
		
		public DojoSelect_Select(Component component) throws SeleniumPlusException {
			super(component);
		}
		
		/**
		 * @see org.safs.selenium.webdriver.lib.DojoSelect#getSupportedClassNames()
		 */
		public String[] getSupportedClassNames() {
			return supportedClazzes;
		}
		
		public boolean isSupported(WebElement element){
			boolean supported = super.isSupported(element);
			
			if(!supported){
				String tagName = element.getTagName();
				String clazz = element.getAttribute(ComboBox.ATTRIBUTE_CLASS);
				
				supported =(CLASS_DOJO_SELECT.equals(clazz) && TAG_DOJO_SELECT.equalsIgnoreCase(tagName));
			}
			
			return supported;
		}
		
		/**
		 * @see org.safs.selenium.webdriver.lib.DojoSelect#getPopup()
		 */
		protected WebElement getPopup() {
			WebElement popup = super.getPopup();
			
			if(popup!=null){
//				String comboboxID = element.getAttribute(Component.ATTRIBUTE_ID);
//				WebElement popupMenu = popup.findElement(By.id(comboboxID+"_menu"));
				
				String widgetid = webelement().getAttribute(Component.ATTRIBUTE_WIDGETID);
				//then, get the popup menu whose widgetid attribute has the value as value of attribute 'widgetid' of combobox plus '_menu'
				WebElement popupMenu = popup.findElement(By.cssSelector("["+Component.ATTRIBUTE_WIDGETID+"='"+widgetid+"_menu']"));
				
				if(popupMenu!=null) return popupMenu;
			}
			
			return popup;
		}
		
		public List<String> getOptionsVisibleText(){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getOptionsVisibleText");
			List<String> options = new ArrayList<String>();
			
			try {
				List<Object> temps = getOptionsProperty(Option.PROPERTY_LABEL, null);
				for(Object val:temps){
					if(val instanceof String) options.add(val.toString());
					else IndependantLog.warn("Cannot add option of type "+val.getClass().getName());
				}
			} catch (Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
				options = super.getOptionsVisibleText();
			}
			
			return options;
		}
		
		public List<String> getOptionsValue(){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getOptionsValue");
			List<String> options = new ArrayList<String>();
			
			try {
				List<Object> temps = getOptionsProperty(Option.PROPERTY_VALUE, null);
				for(Object val:temps){
					if(val instanceof String) options.add(val.toString());
					else IndependantLog.warn("Cannot add option of type "+val.getClass().getName());
				}
			} catch (Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}
			
			return options;
		}
		
		public String getOptionVisibleText(int index){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getOptionVisibleText");
			String option = null;
			
			try {
				List<Object> temps = getOptionsProperty(Option.PROPERTY_LABEL, new Integer(index));
				if(temps.isEmpty()){
					IndependantLog.warn("Cannot get option for index '"+index+"'!");
				}else{
					Object temp = temps.get(0);
					if(temp instanceof String) option = temps.get(0).toString();
					else IndependantLog.warn("Cannot handle return object '"+temp.getClass().getName()+"'");
				}
			} catch (Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}
			
			return option;
		}
		
		public String getOptionValue(int index){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getOptionValue");
			String option = null;
			
			try {
				List<Object> temps = getOptionsProperty(Option.PROPERTY_VALUE, new Integer(index));
				if(temps.isEmpty()){
					IndependantLog.warn("Cannot get option for index '"+index+"'!");
				}else{
					Object temp = temps.get(0);
					if(temp instanceof String) option = temps.get(0).toString();
					else IndependantLog.warn("Cannot handle return object '"+temp.getClass().getName()+"'");
				}
			} catch (Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}
			
			return option;
		}
	
		/**
		 * Get the 'property value' of dijit.form.Select's option. if index is null, return for all options,<br>
		 * otherwise, return for the option specified by index.<br>
		 * @param property	String, the property name of the option object. It can be disabled, label, selected or value.
		 * @param index	Integer, the option index
		 * @return List<Object>, a list of value for certain property of option.
		 */
		private List<Object> getOptionsProperty(String property, Integer index){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getOptionsProperty");
			List<Object> options = new ArrayList<Object>();
			
			//check the property name
			if(Option.PROPERTY_DISABLED.equals(property)||
			   Option.PROPERTY_LABEL.equals(property)||
			   Option.PROPERTY_SELECTED.equals(property)||
			   Option.PROPERTY_VALUE.equals(property)){
				
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(DOJO.dojo_FormSelectWidget_getOptions(true));
				if(index==null){
					jsScript.append("var options = dojo_FormSelectWidget_getOptions(arguments[0]);");
				}else{
					jsScript.append("var options = dojo_FormSelectWidget_getOptions(arguments[0],arguments[1]);");
				}
				try {
					jsScript.append("  var properties = new Array();\n");
					jsScript.append("  if(options != undefined){\n");
					jsScript.append("    if(options.length == undefined){\n");
	//				jsScript.append("      //options has 4 properties: disabled, label, selected and value\n");
					jsScript.append("      properties.push(options."+property+");\n");
					jsScript.append("    }else{\n");
					jsScript.append("      for(var i=0;i<options.length;i++){;\n");
					jsScript.append("        properties.push(options[i]."+property+");\n");
					jsScript.append("      }\n");
					jsScript.append("    }\n");
	//				jsScript.append("    alert(properties);\n");
					jsScript.append("    return properties;\n");
					jsScript.append("  }\n");
					Object result = null;
					if(index==null){
						result = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());
					}else{
						result = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement(), index);
					}
					
					if(result instanceof List){
						@SuppressWarnings("rawtypes")
						Object[] labels = ((List) result).toArray();
						for(Object label:labels){
							if(label!=null) options.add(label);
						}
					}else{
						IndependantLog.warn("Need to hanlde javascript result whose type is '"+result.getClass().getName()+"'!");
						options.add(result);
					}
				} catch(Exception e) {
					IndependantLog.debug(debugmsg+" Met exception.",e);
				}
				
			}else{
				IndependantLog.error("property '"+property+"' is not supported.");
			}
			
			return options;
		}

		protected Object getOptionsJSObject(){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "getOptionsJSObject");
			
			try {
				StringBuffer jsScript = new StringBuffer();
				jsScript.append(DOJO.dojo_FormSelectWidget_getOptions(true));
				//TODO, hope that dojo_FormSelectWidget_getOptions() will return options the same order as what is shown on page.
				jsScript.append("return dojo_FormSelectWidget_getOptions(arguments[0]);");
				Object result = WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement());
				return result;
			
			} catch(Exception e) {
				IndependantLog.debug(debugmsg+" Met exception.",e);
			}
			
			return null;
		}
		
		public static final String CLASS_MENU_ITEM_LABEL = "dijitReset dijitMenuItemLabel";
	
		protected boolean selectOption(Object option){
			if(super.selectOption(option)){
				return true;
			}else{
				//do something special for dojo.form.Select
				return false;
			}
		}
	}

	/**
	 * Models a dojo dijit/form/ComboBox<br>
	 * @see Selectable
	 * @see HtmlSelect
	 * @see DojoSelect
	 */
	protected static class DojoSelect_ComboBox extends DojoSelect{
		//dijit/form/ComboBox, is a HTML tag <div>, a popup is associated
		//https://dojotoolkit.org/documentation/tutorials/1.9/selects/demo/ComboBox.php
		public static final String CLASS_NAME = "dijit.form.ComboBox";//<div>
		public static final String CLASS_DOJO_COMBOBOX = "dijit dijitReset dijitInline dijitLeft dijitTextBox dijitComboBox dijitValidationTextBox";//<div>
		public static final String TAG_DOJO_COMBOBOX = "div";
		public static final String CLASS_DOJO_COMBOBOX_INPUT = "dijitReset dijitInputInner";//<input> .value show the current text of combo box
		public static final String CLASS_DOJO_COMBOBOX_BUTTON = "dijitReset dijitInputField dijitArrowButtonInner";//<input>
		
		public static final String CLASS_POPUP_NAME = "dijit.form._ComboBoxMenu";
		public static final String CLASS_DOJO_COMBOBOX_POPUP = "dijitPopup dijitComboBoxMenuPopup";//<div>
		public static final String TAG_DOJO_COMBOBOX_POPUP = "div";
		public static final String CLASS_DOJO_COMBOBOX_MENU = "dijitReset dijitMenu dijitComboBoxMenu";//<div>
		public static final String CLASS_DOJO_COMBOBOX_MENUITEM = "dijitReset dijitMenuItem";//<div> .item seems like index from 0
		
		public static final String[] supportedClazzes = {CLASS_NAME};
		
		public DojoSelect_ComboBox(Component component) throws SeleniumPlusException {
			super(component);
		}
		
		public boolean isSupported(WebElement element){
			boolean supported = super.isSupported(element);
			
			if(!supported){
				String tagName = element.getTagName();
				String clazz = element.getAttribute(ComboBox.ATTRIBUTE_CLASS);
				
				supported =(CLASS_DOJO_COMBOBOX.equals(clazz) && TAG_DOJO_COMBOBOX_POPUP.equalsIgnoreCase(tagName));
			}
			
			return supported;
		}
	
		/**
		 * @see org.safs.selenium.webdriver.lib.DojoSelect#getSupportedClassNames()
		 */
		public String[] getSupportedClassNames() {
			return supportedClazzes;
		}
	
		/**
		 * @see org.safs.selenium.webdriver.lib.DojoSelect#getPopup()
		 */
		protected WebElement getPopup() {
			WebElement popup = super.getPopup();
					
			if(popup!=null){
//				String comboboxID = element.getAttribute(Component.ATTRIBUTE_ID);
//				WebElement popupMenu = popup.findElement(By.id(comboboxID+"_popup"));
				
				String widgetid = webelement().getAttribute(Component.ATTRIBUTE_WIDGETID);
				//then, get the popup menu whose widgetid attribute has the value as value of attribute 'widgetid' of combobox plus '_popup'
				WebElement popupMenu = popup.findElement(By.cssSelector("["+Component.ATTRIBUTE_WIDGETID+"='"+widgetid+"_popup']"));
								
				if(popupMenu!=null) return popupMenu;
			}
			
			return popup;
		}		
		
		/**
		 * Select an option of a combo box.<br>
		 * Set the attribute 'value' doesn't work for dijit/form/ComboBox, so try to set 'item' object.<br>
		 * @return boolean, true if the option has been selected successfully.
		 */		
		protected boolean selectOption(Object optionObject){
			String debugmsg = StringUtils.debugmsg(this.getClass(), "selectOption");
	
			if(optionObject instanceof Option){
				Option option = (Option) optionObject;
				StringBuffer jsScript = new StringBuffer();
				
				//define javasctipt item object {id=AL, value=AL, name=Alabama}
				Object item = null;
				try {
					jsScript.append(option.defineStoreItemObject());
					jsScript.append("return defineObject();");
					
					item = WDLibrary.executeScript(jsScript.toString());
				} catch(Exception e) {
					IndependantLog.debug(debugmsg+" Met exception.",e);
				}
				
				//reset the string buffer jsScript, define function dojo_WidgetBase_set()
				jsScript.setLength(0);
				jsScript.append(DOJO.dojo_dijit_WidgetBase_set(true));

				jsScript.append("dojo_dijit_WidgetBase_set(arguments[0],arguments[1],arguments[2]);");
	
				try {
					//set the attribute 'value' doesn't work for dijit/form/ComboBox
//					WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), element, "value", option.getValue());

					//item = "{id=AL, value=AL, name=Alabama}";
					WDLibrary.executeJavaScriptOnWebElement(jsScript.toString(), webelement(), "item", item);
					option.setSelected(true);
					return true;
				} catch(Exception e) {
					IndependantLog.debug(debugmsg+" Met exception.",e);
				}
			}
			return false;
		}
	}
	
	/**
	 * Models a dojo dijit/form/FilteringSelect<br>
	 * @see Selectable
	 * @see HtmlSelect
	 * @see DojoSelect
	 */
	protected static class DojoSelect_FilteringSelect extends DojoSelect{
		//dijit/form/FilteringSelect, is a traditional HTML tag <select>
		//https://dojotoolkit.org/documentation/tutorials/1.9/selects/demo/FilteringSelect.php
		public static final String CLASS_NAME = "dijit.form.FilteringSelect";//<table>
		public static final String[] supportedClazzes = {CLASS_NAME};
		
		public DojoSelect_FilteringSelect(Component component) throws SeleniumPlusException {
			super(component);
		}
	
		/**
		 * @see org.safs.selenium.webdriver.lib.DojoSelect#getSupportedClassNames()
		 */
		public String[] getSupportedClassNames() {
			return supportedClazzes;
		}
	
		/**
		 * @see org.safs.selenium.webdriver.lib.DojoSelect#getPopup()
		 */
		protected WebElement getPopup() {
			WebElement popup = super.getPopup();
			
			if(popup!=null){
//				String comboboxID = element.getAttribute(Component.ATTRIBUTE_ID);
//				WebElement popupMenu = popup.findElement(By.id(comboboxID+"_popup"));
				
				String widgetid = webelement().getAttribute(Component.ATTRIBUTE_WIDGETID);
				//then, get the popup menu whose widgetid attribute has the value as value of attribute 'widgetid' of combobox plus '_popup'
				WebElement popupMenu = popup.findElement(By.cssSelector("["+Component.ATTRIBUTE_WIDGETID+"='"+widgetid+"_popup']"));
								
				if(popupMenu!=null) return popupMenu;
			}
			
			return popup;
		}
	}
	
	
	//=============================================  Some Test Codes   ===============================================//
	public void testSelect(){
		 List<String> options = select.getOptionsVisibleText();
		 
		 System.out.println("Option visible texts:");
		 for(String option: options){
			 System.out.println(option);
		 }
		 
		 testGeneralSelect();
		 
		 if(select instanceof DojoSelect){
			 testDojoSelect();
			 testDojoSelect_FilteringSelect();
			 testDojoSelect_ComboBox();
			 testDojoSelect_Select();
		 }

	}
	
	private void testDojoSelect_FilteringSelect(){
		String debugmsg = StringUtils.debugmsg(this.getClass(), "testDojoSelect_FilteringSelect");
		
		if(select instanceof DojoSelect_FilteringSelect){
			@SuppressWarnings("unused")
			DojoSelect_FilteringSelect myselect = (DojoSelect_FilteringSelect) select;
			//Do some special test against DojoSelect_FilteringSelect
			
		}else{
			System.err.println(debugmsg+select.getClass().getName()+" should not be tested here.");
		}
	}
	
	private void testDojoSelect_ComboBox(){
		String debugmsg = StringUtils.debugmsg(this.getClass(), "testDojoSelect_ComboBox");
		
		if(select instanceof DojoSelect_ComboBox){
			@SuppressWarnings("unused")
			DojoSelect_ComboBox myselect = (DojoSelect_ComboBox) select;
			//Do some special test against DojoSelect_ComboBox
			
		}else{
			System.err.println(debugmsg+select.getClass().getName()+" should not be tested here.");
		}
	}
	
	private void testDojoSelect_Select(){
		String debugmsg = StringUtils.debugmsg(this.getClass(), "testDojoSelect_Select");
		
		if(select instanceof DojoSelect_Select){
			@SuppressWarnings("unused")
			DojoSelect_Select myselect = (DojoSelect_Select) select;
			//Do some special test against DojoSelect_Select

		}else{
			System.err.println(debugmsg+select.getClass().getName()+" should not be tested here.");
		}
	}
	
	private void testDojoSelect(){
		String debugmsg = StringUtils.debugmsg(this.getClass(), "testDojoSelect");
		if(select instanceof DojoSelect){
			@SuppressWarnings("unused")
			DojoSelect myselect = (DojoSelect) select;
			
		}else{
			System.err.println(debugmsg+select.getClass().getName()+" should not be tested here.");
		}
	}
	
	private void testGeneralSelect(){
		List<String> options = select.getOptionsValue();

		System.out.println("Option values:");
		for(String option: options){
			System.out.println(option);
		}

		options = select.getOptionsVisibleText();

		System.out.println("Option visible texts:");
		for(String option: options){
			System.out.println(option);
		}

		int index = 3;
		String text = select.getOptionVisibleText(index);
		System.out.println("option index '"+index+"', text is "+text);

		String value = select.getOptionValue(index);
		System.out.println("option index '"+index+"', value is "+value);

		index = 7;
		text = select.getOptionVisibleText(index);
		System.out.println("option index '"+index+"', text is "+text);

		value = select.getOptionValue(index);
		System.out.println("option index '"+index+"', value is "+value);

		index = -1;
		text = select.getOptionVisibleText(index);
		System.out.println("option index '"+index+"', text is "+text);

		value = select.getOptionValue(index);
		System.out.println("option index '"+index+"', value is "+value);

		List<Option> objects = select.getOptions();
		for(Option option:objects){
			System.out.println(option.toString());

			System.out.println(Option.PROPERTY_DISABLED+"="+option.isDisabled());
			System.out.println(Option.PROPERTY_LABEL+"="+option.getLabel());
			System.out.println(Option.PROPERTY_SELECTED+"="+option.isSelected());
			System.out.println(Option.PROPERTY_VALUE+"="+option.getValue());
		}

	}
}

