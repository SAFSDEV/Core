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
 *
 * History:
 *
 *  JUN 25, 2014    (Lei Wang) Initial release.
 *  OCT 29, 2014    (Lei Wang) Move some XPATH related codes from SearchObject to here.
 *  FEB 03, 2016    (Lei Wang) Modify conditionXXX(): to avoid the InvalidSelectorException
 *                            caused by apostrophe existing in the text/value.
 *  JUN 28, 2017    (Lei Wang) Modified methods to convert CSS selector to XPATH if it contains deprecated functions.
 *  JUL 02, 2019    (Lei Wang) Added relativeDescendants(), allDescendants() and some constants to create xpath for searching.
 *  JUL 03, 2019    (Lei Wang) Added more XPATH to define type for different controls.
 *  SEP 04, 2019    (Lei Wang) Added forAttribute(): generate xpath pattern for searching elements.
 */
package org.safs.selenium.webdriver.lib;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.safs.Constants;
import org.safs.Constants.HTMLConst;
import org.safs.IndependantLog;
import org.safs.StringUtils;

/**
 * A convenient class to create xpath, cssSelector to create a By searching criterion for Selenium to find WebElement.<br>
 * It also provides "Recognition String" such as "xpath=xxx", "css=xxx", "id=xxx" etc. for SearchObject to find WebElement.<br>
 *
 */
public abstract class RS{

	/**
	 * This class contains methods returning an xpath.<br>
	 * please refer to <a href="https://www.w3schools.com/xml/xpath_syntax.asp">xpath syntax</a>.
	 */
	public abstract static class XPATH{
		/** <b>*</b> represents any tag name */
		public static final String TAG_ANY  			= HTMLConst.TAG_ANY;
		/** <b>DIV</b> tag name */
		public static final String TAG_DIV  			= HTMLConst.TAG_DIV;
		/** <b>//*[</b>, it is used as beginning to construct an xpath for searching elements under ENTIRE document. */
		public static final String MATCHING_ALL_START  			= "//"+TAG_ANY+"[";
		/** <b>.//*[</b>, it is used as beginning to construct an xpath for searching elements under current WebElement. */
		public static final String RELATIVE_MATCHING_ALL_START  	= ".//"+TAG_ANY+"[";
		/** <b>//DIV[</b>, it is used as beginning to construct an xpath for searching DIV elements under ENTIRE document.*/
		public static final String MATCHING_DIV_START  			= "//"+TAG_DIV+"[";
		/** <b>.//DIV[</b>, it is used as beginning to construct an xpath for searching DIV elements under current WebElement.*/
		public static final String RELATIVE_MATCHING_DIV_START  	= ".//"+TAG_DIV+"[";
		/** <b>(1=1)</b>, an always true condition to concatenate other conditions to create an XPAH for searching. */
		public static final String TRUE_CONDITION  						= " (1=1) ";
		/** <b>]</b> */
		public static final String END  					= "]";
		/** <b>and</b> */
		public static final String AND  					= "and";
		/** <b>or</b> used inside one XPATH as logical or, such as <span style="color:gray">input[@type='email' or @type='number']</span> */
		public static final String OR  						= "or";

		/** <b>|</b> used between multiple XPATH as logical or, such as <span style="color:gray">//book/title | //book/price</span> */
		public static final String REGEX_OR  					= "|";

		/** <b>return '/html'</b> */
		public static final String html(){ return "/html";}

		/** <b>..</b> represents the parent */
		public static final String PARENT  				= "..";
		/** <b>.//</b> represents the path to children, grand-children etc. relative to current element,
		 * it should be appended with real tag name or search-condition, for example ".//<b>input</b>" will represent
		 * all <b>input</b> tags as children, grand-children of current tree node on the page.
		 *
		 * @see #EDITBOX
		 * @see #BUTTON
		 * @see #COMBOBOX
		 * @see #CHECKBOX
		 * @see #COLOR
		 * @see #DATETIME
		 * @see #FILE
		 * @see #RADIO
		 * @see #RANGE
		 * @see #IMAGE
		 * @see #LIST
		 * @see #TREE
		 * @see #TABLE
		 * @see #relativeDescendants(String[])
		 */
		public static final String DESCENDANTS_PREFIX		= ".//";
		/** <b>//</b> represents the path to all elements on the page,
		 * it should be appended with real tag name or search-condition, for example "//<b>input</b>" will represent
		 * all <b>input</b> tags on the page.
		 *
		 * @see #EDITBOX
		 * @see #BUTTON
		 * @see #COMBOBOX
		 * @see #CHECKBOX
		 * @see #COLOR
		 * @see #DATETIME
		 * @see #FILE
		 * @see #RADIO
		 * @see #RANGE
		 * @see #IMAGE
		 * @see #LIST
		 * @see #TREE
		 * @see #TABLE
		 * @see #allDescendants(String[])
		 */
		public static final String ALL_ELEMENTS_PREFIX		= "//";

		/** Contains items from {@link HTML#EDITBOX}, {@link ARIA#EDITBOX} and {@link SAP#EDITBOX}<br>
		 * An array of search-conditions to be appended to {@link #DESCENDANTS_PREFIX} or {@link #ALL_ELEMENTS_PREFIX} to form a valid XPATH for searching.<br>
		 * It serves as parameter of {@link #relativeDescendants(String[])} or {@link #allDescendants(String[])}<br>
		 */
		public static final String[] EDITBOX		= ArrayUtils.addAll(HTML.EDITBOX, ArrayUtils.addAll(ARIA.EDITBOX, SAP.EDITBOX));
		/** Contains items from {@link HTML#BUTTON}, {@link ARIA#BUTTON} and {@link SAP#BUTTON} <br>
		 * An array of search-conditions to be appended to {@link #DESCENDANTS_PREFIX} or {@link #ALL_ELEMENTS_PREFIX} to form a valid XPATH for searching.<br>
		 * It serves as parameter of {@link #relativeDescendants(String[])} or {@link #allDescendants(String[])}<br>
		 */
		public static final String[] BUTTON			= ArrayUtils.addAll(HTML.BUTTON, ArrayUtils.addAll(ARIA.BUTTON, SAP.BUTTON));
		/** Contains items from {@link HTML#COMBOBOX}, {@link ARIA#COMBOBOX} and {@link SAP#COMBOBOX}<br>
		 * An array of search-conditions to be appended to {@link #DESCENDANTS_PREFIX} or {@link #ALL_ELEMENTS_PREFIX} to form a valid XPATH for searching combobox.<br>
		 * It serves as parameter of {@link #relativeDescendants(String[])} or {@link #allDescendants(String[])}<br>
		 */
		public static final String[] COMBOBOX		= ArrayUtils.addAll(HTML.COMBOBOX, ArrayUtils.addAll(ARIA.COMBOBOX, SAP.COMBOBOX));
		/** Contains items from {@link HTML#CHECKBOX}, {@link ARIA#CHECKBOX} and {@link SAP#CHECKBOX} <br>
		 * An array of search-conditions to be appended to {@link #DESCENDANTS_PREFIX} or {@link #ALL_ELEMENTS_PREFIX} to form a valid XPATH for searching checkbox.<br>
		 * It serves as parameter of {@link #relativeDescendants(String[])} or {@link #allDescendants(String[])}<br>
		 */
		public static final String[] CHECKBOX		= ArrayUtils.addAll(HTML.CHECKBOX, ArrayUtils.addAll(ARIA.CHECKBOX, SAP.CHECKBOX));
		/** Contains items from {@link HTML#COLOR}<br>
		 * An array of search-conditions to be appended to {@link #DESCENDANTS_PREFIX} or {@link #ALL_ELEMENTS_PREFIX} to form a valid XPATH for searching color picker.<br>
		 * It serves as parameter of {@link #relativeDescendants(String[])} or {@link #allDescendants(String[])}<br>
		 */
		public static final String[] COLOR			= HTML.COLOR;
		/** Contains items from {@link HTML#DATETIME}<br>
		 * An array of search-conditions to be appended to {@link #DESCENDANTS_PREFIX} or {@link #ALL_ELEMENTS_PREFIX} to form a valid XPATH for searching date-time input box.<br>
		 * It serves as parameter of {@link #relativeDescendants(String[])} or {@link #allDescendants(String[])}<br>
		 */
		public static final String[] DATETIME		= HTML.DATETIME;
		/** Contains items from {@link HTML#FILE}<br>
		 * An array of search-conditions to be appended to {@link #DESCENDANTS_PREFIX} or {@link #ALL_ELEMENTS_PREFIX} to form a valid XPATH for searching file picker.<br>
		 * It serves as parameter of {@link #relativeDescendants(String[])} or {@link #allDescendants(String[])}<br>
		 */
		public static final String[] FILE			= HTML.FILE;
		/** Contains items from {@link HTML#RADIO}, {@link ARIA#RADIO} and {@link SAP#RADIO} <br>
		 * An array of search-conditions to be appended to {@link #DESCENDANTS_PREFIX} or {@link #ALL_ELEMENTS_PREFIX} to form a valid XPATH for searching radio button.<br>
		 * It serves as parameter of {@link #relativeDescendants(String[])} or {@link #allDescendants(String[])}<br>
		 */
		public static final String[] RADIO			= ArrayUtils.addAll(HTML.RADIO, ArrayUtils.addAll(ARIA.RADIO, SAP.RADIO));
		/** Contains items from {@link HTML#RANGE} and {@link SAP#RANGE} <br>
		 * An array of search-conditions to be appended to {@link #DESCENDANTS_PREFIX} or {@link #ALL_ELEMENTS_PREFIX} to form a valid XPATH for searching range.<br>
		 * It serves as parameter of {@link #relativeDescendants(String[])} or {@link #allDescendants(String[])}<br>
		 */
		public static final String[] RANGE			= ArrayUtils.addAll(HTML.RANGE, SAP.RANGE);
		/** Contains items from {@link HTML#IMAGE}, {@link ARIA#IMAGE} and {@link SAP#IMAGE} <br>
		 * An array of search-conditions to be appended to {@link #DESCENDANTS_PREFIX} or {@link #ALL_ELEMENTS_PREFIX} to form a valid XPATH for searching image.<br>
		 * It serves as parameter of {@link #relativeDescendants(String[])} or {@link #allDescendants(String[])}<br>
		 */
		public static final String[] IMAGE			= ArrayUtils.addAll(HTML.IMAGE, ArrayUtils.addAll(ARIA.IMAGE, SAP.IMAGE));

		/** Contains items from {@link HTML#LIST}, {@link ARIA#LISTBOX}, {@link ARIA#LIST} and {@link SAP#LIST} <br>
		 * An array of search-conditions to be appended to {@link #DESCENDANTS_PREFIX} or {@link #ALL_ELEMENTS_PREFIX} to form a valid XPATH for searching list.<br>
		 * It serves as parameter of {@link #relativeDescendants(String[])} or {@link #allDescendants(String[])}<br>
		 */
		public static final String[] LIST			= ArrayUtils.addAll(HTML.LIST, ArrayUtils.addAll(ARIA.LISTBOX, ArrayUtils.addAll(ARIA.LIST, SAP.LIST)));

		/** Contains items from {@link ARIA#TREE} and {@link SAP#TREE} <br>
		 * An array of search-conditions to be appended to {@link #DESCENDANTS_PREFIX} or {@link #ALL_ELEMENTS_PREFIX} to form a valid XPATH for searching tree.<br>
		 * It serves as parameter of {@link #relativeDescendants(String[])} or {@link #allDescendants(String[])}<br>
		 */
		public static final String[] TREE			= ArrayUtils.addAll(ARIA.TREE, SAP.TREE);

		/** Contains items from {@link HTML#TABLE}, {@link ARIA#TABLE} and {@link SAP#TABLE} <br>
		 * An array of search-conditions to be appended to {@link #DESCENDANTS_PREFIX} or {@link #ALL_ELEMENTS_PREFIX} to form a valid XPATH for searching table.<br>
		 * It serves as parameter of {@link #relativeDescendants(String[])} or {@link #allDescendants(String[])}<br>
		 */
		public static final String[] TABLE			= ArrayUtils.addAll(HTML.TABLE, ArrayUtils.addAll(ARIA.TABLE, SAP.TABLE));

		/**
		 * @param attribute String, the attribute to check
		 * @param values String[], the values to match
		 * @return String, the condition of matching values of a certain attribute
		 */
		private static String forAttribute(String attribute, String... values){
			StringBuilder sb = new StringBuilder();
			String result = sb.toString().trim();

			if(values!=null){
				for(String value:values){
					sb.append("@"+attribute+"='"+value.toLowerCase()+"' "+OR+" ");
					sb.append(lowercase(attribute)+"='"+value.toLowerCase()+"' "+OR+" ");
				}
				//remove the last "or"
				result = sb.toString().trim();
				result = result.substring(0, result.length()-OR.length());
			}

			return result;
		}

		/**
		 * Generate the xpath to convert the attribute's value to lowercase, such as lower-case(@type).
		 * @param attribute String, the attribute name
		 * @return String, the xpath to convert the attribute's value to lowercase
		 */
		private static String lowercase(String attribute){
			StringBuilder sb = new StringBuilder();
			//lower-case(@type)
			//translate(@type, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')
			//lower-case() is a function for XPATH 2.0, cannot work with current selenium
			//sb.append("lower-case(@"+attribute+")");
			sb.append("translate(@"+attribute+", '"+Constants.ALPHABET+"', '"+Constants.alphabet+"')");
			return sb.toString();
		}

		/**
		 * Defines arrays of search-conditions for <a href="https://www.w3schools.com/tags/default.asp">HTML Controls</a>
		 * to be appended to {@link #DESCENDANTS_PREFIX} or {@link #ALL_ELEMENTS_PREFIX} to form a valid XPATH for searching.<br>
		 * It serves as parameter of {@link #relativeDescendants(String[])} or {@link #allDescendants(String[])}<br>
		 */
		public abstract static class HTML{
			//https://www.w3schools.com/html/html_form_input_types.asp
			/** <b>input</b> */
			public static final String INPUT			= "input";
//			public static final String[] EDITBOX	= {"input[matches(@type, 'email|password|tel|text|url|search')]", "textarea" };
			/** <b>{"input[@type='email' or @type='number' or @type='password' or @type='search' or @type='tel' or @type='text' or @type='url']", "textarea"}</b> */
			public static final String[] EDITBOX		= {"input["+forAttribute(HTMLConst.ATTRIBUTE_TYPE, "email", "number", "password", "search", "tel", "text", "url")+"]", "textarea"};
			/** <b>{"input[@type='button' or @type='reset' or @type='submit']"}</b> */
			public static final String[] BUTTON			= {"input["+forAttribute(HTMLConst.ATTRIBUTE_TYPE, "button", "reset", "submit")+"]", "button"};
			/** <b>{"select"}</b> */
			public static final String[] COMBOBOX		= {"select"};
			/** <b>{"input[@type='checkbox']"}</b> */
			public static final String[] CHECKBOX		= {"input["+forAttribute(HTMLConst.ATTRIBUTE_TYPE, "checkbox")+"]"};
			/** <b>{"input[@type='color']"}</b> */
			public static final String[] COLOR			= {"input["+forAttribute(HTMLConst.ATTRIBUTE_TYPE, "color")+"]"};
			/** <b>{"input[@type='date' or @type='datetime-local' or @type='month' or @type='time' or @type='week']"}</b> */
			public static final String[] DATETIME		= {"input["+forAttribute(HTMLConst.ATTRIBUTE_TYPE, "date", "datetime-local", "month", "time", "week")+"]"};
			/** <b>{"input[@type='file']"}</b> */
			public static final String[] FILE			= {"input["+forAttribute(HTMLConst.ATTRIBUTE_TYPE, "file")+"]"};
			/** <b>{"input[@type='radio']"}</b> */
			public static final String[] RADIO			= {"input["+forAttribute(HTMLConst.ATTRIBUTE_TYPE, "radio")+"]"};
			/** <b>{"input[@type='range']"}</b> */
			public static final String[] RANGE			= {"input["+forAttribute(HTMLConst.ATTRIBUTE_TYPE, "range")+"]"};
			/** <b>{"img", "image", "input[@type='image']"}</b> */
			public static final String[] IMAGE			= {"img", "image", "input["+forAttribute(HTMLConst.ATTRIBUTE_TYPE, "image")+"]"};
			/** <b>{"ul", "ol", "input[@list]"}</b> */
			public static final String[] LIST			= {"ul", "ol", "input[@list]"};
			/** <b>{"table"}</b> */
			public static final String[] TABLE			= {"table"};
		}

		/**
		 * Defines arrays of search-conditions (according to <a href="https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Roles">ARIA Roles</a>)
		 * to be appended to {@link #DESCENDANTS_PREFIX} or {@link #ALL_ELEMENTS_PREFIX} to form a valid XPATH for searching.<br>
		 * It serves as parameter of {@link #relativeDescendants(String[])} or {@link #allDescendants(String[])}<br>
		 */
		public abstract static class ARIA{
			/** <b>{"*[@role='button']"}</b> */
			public static final String[] BUTTON			= {"*["+forAttribute(HTMLConst.ATTRIBUTE_ROLE, "button")+"]"};
			/** <b>{"*[@role='combobox']"}</b> */
			public static final String[] COMBOBOX		= {"*["+forAttribute(HTMLConst.ATTRIBUTE_ROLE, "combobox")+"]"};
			/** <b>{"*[@role='checkbox']"}</b> */
			public static final String[] CHECKBOX		= {"*["+forAttribute(HTMLConst.ATTRIBUTE_ROLE, "checkbox")+"]"};
			/** <b>{"*[@role='textbox']"}</b> */
			public static final String[] EDITBOX		= {"*["+forAttribute(HTMLConst.ATTRIBUTE_ROLE, "textbox")+"]"};
			/** <b>{"*[@role='img']"}</b> */
			public static final String[] IMAGE			= {"*["+forAttribute(HTMLConst.ATTRIBUTE_ROLE, "img")+"]"};
			/** <b>{"*[@role='list']"}</b> */
			public static final String[] LIST			= {"*["+forAttribute(HTMLConst.ATTRIBUTE_ROLE, "list")+"]"};
			/** <b>{"*[@role='listitem']"}</b> */
			public static final String[] LISTITEM		= {"*["+forAttribute(HTMLConst.ATTRIBUTE_ROLE, "listitem")+"]"};
			/** <b>{"*[@role='listbox']"}</b> */
			public static final String[] LISTBOX		= {"*["+forAttribute(HTMLConst.ATTRIBUTE_ROLE, "listbox")+"]"};
			/** <b>{"*[@role='option']"}</b> */
			public static final String[] OPTION			= {"*["+forAttribute(HTMLConst.ATTRIBUTE_ROLE, "option")+"]"};
			/** <b>{"*[@role='radio']"}</b> */
			public static final String[] RADIO			= {"*["+forAttribute(HTMLConst.ATTRIBUTE_ROLE, "radio")+"]"};
			/** <b>{"*[@role='tab']"}</b> */
			public static final String[] TAB			= {"*["+forAttribute(HTMLConst.ATTRIBUTE_ROLE, "tab")+"]"};
			/** <b>{"*[@role='tree']"}</b> */
			public static final String[] TREE			= {"*["+forAttribute(HTMLConst.ATTRIBUTE_ROLE, "tree")+"]"};
			/** <b>{"*[@role='table']"}</b> */
			public static final String[] TABLE			= {"*["+forAttribute(HTMLConst.ATTRIBUTE_ROLE, "table")+"]"};
			/** <b>{*[contains(concat(' ',@aria-labelledby,' '), ' "+labelID+" ')], *[contains(concat(' ',@aria-describedby,' '), ' "+labelID+" ')]}</b>*/
			public static final String[] LABELLEDBY(String labelID){
				//https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/ARIA_Techniques/Using_the_aria-labelledby_attribute
				//https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/ARIA_Techniques/Using_the_aria-describedby_attribute
				String[] rs = {"*[contains(concat(' ',@aria-labelledby,' '), ' "+labelID+" ')]", "*[contains(concat(' ',@aria-describedby,' '), ' "+labelID+" ')]"};
				return rs;
			}
		}

		/**
		 * Defines arrays of search-conditions for <a href="https://sapui5.hana.ondemand.com/#/controls">SAPUI5 controls</a>
		 * to be appended to {@link #DESCENDANTS_PREFIX} or {@link #ALL_ELEMENTS_PREFIX} to form a valid XPATH for searching.<br>
		 * It serves as parameter of {@link #relativeDescendants(String[])} or {@link #allDescendants(String[])}<br>
		 */
		public abstract static class SAP{
			/** <b>{"*[contains(@class,'sapUi')]"}</b> */
			public static final String[] SAPOBJECT		= {"*[contains(@class,'sapUi')]"};
			/** <b>{"*[contains(concat(' ',@class,' '), ' sapMBtn ')]"}</b> */
			public static final String[] BUTTON			= {"*[contains(concat(' ',@class,' '), ' sapMBtn ')]"};
			/** <b>{"*[contains(concat(' ',@class,' '), ' sapMCb ')]"}</b> */
			public static final String[] CHECKBOX		= {"*[contains(concat(' ',@class,' '), ' sapMCb ')]"};
			/** <b>{"*[contains(concat(' ',@class,' '), ' sapUiCal ')]"}</b> */
			public static final String[] CALENDAR		= {"*[contains(concat(' ',@class,' '), ' sapUiCal ')]"};
			/** <b>{"*[contains(concat(' ',@class,' '), ' sapMComboBox ')]"}</b> */
			public static final String[] COMBOBOX		= {"*[contains(concat(' ',@class,' '), ' sapMComboBox ')]"};
			/** <b>{"*[contains(concat(' ',@class,' '), ' sapMInput ') or contains(concat(' ',@class,' '), ' sapMTextArea ') or contains(concat(' ',@class,' '), ' sapUiTf ')]"}</b> */
			public static final String[] EDITBOX		= {"*[contains(concat(' ',@class,' '), ' sapMInput ') or contains(concat(' ',@class,' '), ' sapMTextArea ') or contains(concat(' ',@class,' '), ' sapUiTf ')]"};
			/** <b>{"*[contains(concat(' ',@class,' '), ' sapUiTable ')]"}</b> */
			public static final String[] TABLE			= {"*[contains(concat(' ',@class,' '), ' sapUiTable ')]"};
			/** <b>{"*[contains(concat(' ',@class,' '), ' sapUiTree ')]"}</b> */
			public static final String[] TREE			= {"*[contains(concat(' ',@class,' '), ' sapUiTree ')]"};
			/** <b>{"*[contains(concat(' ',@class,' '), ' sapUiList ') or contains(concat(' ',@class,' '), ' sapMList ')]"}</b> */
			public static final String[] LIST			= {"*[contains(concat(' ',@class,' '), ' sapUiList ') or contains(concat(' ',@class,' '), ' sapMList ')]"};
			/** <b>{"*[contains(concat(' ',@class,' '), ' sapMImg ')]"}</b> */
			public static final String[] IMAGE			= {"*[contains(concat(' ',@class,' '), ' sapMImg ')]"};
			/** <b>{"*[contains(concat(' ',@class,' '), ' sapMSlider ')]"}</b> */
			public static final String[] RANGE			= {"*[contains(concat(' ',@class,' '), ' sapMSlider ')]"};
			/** <b>{"*[contains(concat(' ',@class,' '), ' sapMRbB ')]"}</b> */
			public static final String[] RADIO			= {"*[contains(concat(' ',@class,' '), ' sapMRbB ')]"};
			/** <b>{"*[contains(concat(' ',@class,' '), ' sapUiColorPicker-ColorPickerCircle ')]"}</b> */
			public static final String[] COLORPICKER	= {"*[contains(concat(' ',@class,' '), ' sapUiColorPicker-ColorPickerCircle ')]"};

		}

		/**
		 * @param tag Ex: span, table, ul, etc..
		 * @return "//&lt;TAG>["
		 */
		public static String MATCHING_TAG_START(String tag){
			return "//"+ tag.toUpperCase()+"[";
		}
		/**
		 * @param tag Ex: span, table, ul, etc..
		 * @return ".//&lt;TAG>["
		 */
		public static String RELATIVE_MATCHING_TAG_START(String tag){
			return ".//"+ tag.toUpperCase()+"[";
		}

		/**
		 * Create an xpath by text for searching webelement within direct and indirect children.
		 * @param text	String, the text of a webelement. can be regex if XPATH2.0
		 * @param partial boolean, if the parameter text is part of the webelement's content
		 * @param relative boolean, if true then searching elements under current WebElement; false then searching elements under ENTIRE document.
		 * @return String, the xpath for webdriver to search a webelement.
		 */
		public static String fromText(String text, boolean partial, boolean relative){
			return fromText(TAG_ANY, text, partial, relative);
		}

		/**
		 * Create an xpath by tag's name and text for searching webelement within direct and indirect children.
		 * @param tag	String, the name of the tag to find.
		 * @param text	String, the text of a webelement. can be regex if XPATH2.0
		 * @param partial boolean, if the parameter text is part of the webelement's content
		 * @param relative boolean, if true then searching elements under current WebElement; false then searching elements under ENTIRE document.
		 * @return String, the xpath for webdriver to search a webelement.
		 */
		public static String fromText(String tag, String text, boolean partial, boolean relative){
			String xpath = null;

			if(relative){
				xpath = RELATIVE_MATCHING_TAG_START(tag)+conditionForText(text, partial)+END;
			}else{
				xpath = MATCHING_TAG_START(tag)+conditionForText(text, partial)+END;
			}

			return xpath;
		}

		/**
		 * Create an xpath by (attribute, value) for searching webelement within direct and indirect children.
		 * @param attribute String, the attribute name
		 * @param value String, the attribute's value, can be regex if XPATH2.0.
		 * @param partial boolean, if the parameter value is part of the webelement's attribute
		 * @param relative boolean, if true then searching elements under current WebElement; false then searching elements under ENTIRE document.
		 * @return String, the xpath for webdriver to search a webelement.
		 */
		public static String fromAttribute(String attribute, String value, boolean partial, boolean relative){
			return fromAttribute(TAG_ANY, attribute, value, partial, relative);
		}

		/**
		 * Create an xpath by tag's name and (attribute, value) for searching webelement within direct and indirect children.
		 * @param attribute String, the attribute name
		 * @param value String, the attribute's value, can be regex if XPATH2.0.
		 * @param partial boolean, if the parameter value is part of the webelement's attribute
		 * @param relative boolean, if true then searching elements under current WebElement; false then searching elements under ENTIRE document.
		 * @return String, the xpath for webdriver to search a webelement.
		 */
		public static String fromAttribute(String tag, String attribute, String value, boolean partial, boolean relative){
			String xpath = null;

			if(relative){
				xpath = RELATIVE_MATCHING_TAG_START(tag)+condition(attribute, value, partial)+END;
			}else{
				xpath = MATCHING_TAG_START(tag)+condition(attribute, value, partial)+END;
			}

			return xpath;
		}

		/**
		 * @param attribute String, the attribute name
		 * @param value String, the expected value of the attribute
		 * @param partialMatch boolean, if the attribute value will be matched partially (considered as a substring)
		 * @return String, the xpath condition for searching
		 * @see #getCondition(String, boolean)
		 */
		public static String condition(String attribute, String value, boolean partialMatch){
			if(attribute==null || value==null){
				IndependantLog.warn(StringUtils.debugmsg(false)+"property or value is null, not valid!");
			}

			String normalizedText = quote(value);
			if(partialMatch){
				return " contains(@"+attribute+", "+normalizedText+")";
			}else{
				return " @"+attribute+"="+normalizedText;
			}
		}

		/**
		 * @param attribute String, the attribute name ending with "contains"
		 * @param value String, the expected substring value of the attribute
		 * @return String, the xpath condition for searching
		 * @see #getCondition(String, boolean)
		 */
		public static String conditionContains(String attribute, String value){
			if(attribute==null || value==null){
				IndependantLog.warn(StringUtils.debugmsg(false)+"property or value is null, not valid!");
			}

			String normalizedText = quote(value);
			int i = attribute.toUpperCase().indexOf(SearchObject.SEARCH_CRITERIA_CONTAINS_SUFFIX);
			if(i<1){
				IndependantLog.warn(StringUtils.debugmsg(false)+"<property>Contains name is NOT valid!");
				return " contains(@"+attribute+", "+normalizedText+")";
			}
			return " contains(@"+attribute.substring(0, i)+", "+normalizedText+")";
		}

		/**
		 * @param text String, the text value
		 * @param partialMatch boolean, if the text's value will be matched partially (considered as a substring)
		 * @return String, the xpath condition for searching
		 */
		public static String conditionForText(String text, boolean partialMatch){
			if(text==null){
				IndependantLog.warn(StringUtils.debugmsg(false)+"text value is null, not valid!");
			}

			String normalizedText = quote(text);
			if(partialMatch){
				//   //*[text()[contains(.,'ABC')]]  is more powerful than //*[contains(text(),'ABC')]
				//see https://stackoverflow.com/questions/3655549/xpath-containstext-some-string-doesnt-work-when-used-with-node-with-more
				//return " text()[contains(., "+normalizedText+")]";
				return " contains(text(), "+normalizedText+")";
			}else{
				/*
				 * Find all items accepting selection or input with the displayed text
				 * "//*[text()= '"+ text +"']" and "//*[.='"+ text +"']" are different, see below link
				 * https://stackoverflow.com/questions/38240763/xpath-difference-between-dot-and-text
				 */
				//return " .="+normalizedText;
				return " text()="+normalizedText;
			}
		}

		/**
		 * Add single quote around the text (such as 'text'), and it will be used in XPATH for searching a web element in DOM.<br>
		 * If the text has already been quoted, we simply return it.<br>
		 * If the text (such as Tom's) contains single quote, we cannot simply quote the text with single quote, which will cause the error;<br>
		 * instead we will use the function concat() to connect each part of the text, such as concat('Tom', "'", 's').<br>
		 *
		 * @param text String, the text of the web element.
		 * @return String, the quoted text; or a string combined by function concat().
		 */
		protected static String quote(String text){
			String normalized = StringUtils.QUOTE + text + StringUtils.QUOTE;

			if(text!=null){
				//It is already quoted, return it directly
				if(text.trim().startsWith(StringUtils.QUOTE) && text.trim().endsWith(StringUtils.QUOTE)){
					return text;
				}

				//if text contains single quote, we should not simply quote it with single quote.
				//we will use the function concat()
				int singleQuoteIndex = text.indexOf(StringUtils.QUOTE);
				if(singleQuoteIndex>-1){
					normalized="concat(";

					List<String> tokens = StringUtils.getTokenList(text, StringUtils.QUOTE);
					String singleQuoteParam = ", "+StringUtils.DOUBLE_QUOTE+StringUtils.QUOTE+StringUtils.DOUBLE_QUOTE + ", ";// , "'",

					for(String token:tokens){
						normalized += StringUtils.QUOTE+token+StringUtils.QUOTE;// 'some string'
						normalized += singleQuoteParam;// , "'",
					}

					int index = normalized.lastIndexOf(singleQuoteParam);
					normalized = normalized.substring(0, index);

					normalized += ")";
				}
			}

			return normalized;
		}

		public static boolean isRootHtml(SearchContext sc){
			if(sc==null) return false;
			String searchContext = sc.toString();
			IndependantLog.debug("Search Context="+searchContext);
			//[[RemoteDriver: internet explorer on WINDOWS (8469f5a5-11dd-4642-a34f-f2979f135694)] -> xpath: /html]
			if(searchContext.startsWith("[[") && searchContext.endsWith("xpath: /html]")){
				return true;
			}
			return false;
		}

		/**
		 * According to the search conditions, return the xpath to get the relative (to current node) elements.
		 * @param targets String[], the search conditions
		 * @return String, the xpath to get the relative (to current node) elements.
		 */
		public static String relativeDescendants(String[] targets){
			String dbgmsg = StringUtils.debugmsg(false);
			String xpath = "";
			for(String target:targets){
				xpath += XPATH.DESCENDANTS_PREFIX+target+REGEX_OR;
			}
			if(xpath.endsWith(REGEX_OR)){
				xpath = xpath.substring(0, xpath.length()-1);
			}
			IndependantLog.debug(dbgmsg+" returning xpath: "+xpath);
			return xpath;
		}

		/**
		 * According to the search conditions, return the xpath to get all matched elements.
		 * @param targets String[], the search conditions
		 * @return String, the xpath to get all matched elements.
		 */
		public static String allDescendants(String[] targets){
			String dbgmsg = StringUtils.debugmsg(false);
			String xpath = "";
			for(String target:targets){
				xpath += XPATH.ALL_ELEMENTS_PREFIX+target+REGEX_OR;
			}
			if(xpath.endsWith(REGEX_OR)){
				xpath = xpath.substring(0, xpath.length()-1);
			}
			IndependantLog.debug(dbgmsg+" returning xpath: "+xpath);
			return xpath;
		}

		/**
		 * The map containing pair(type, xpath), the key is WebElement's type, such as "ComboBox", "Button" etc.<br>
		 *                                       The value is the xpath to find the WebElement, such as <b>XPATH.relativeDescendants(XPATH.COMBOBOX)</b> is used to find ComboBox.
		 */
		private static Map<String, String> typeToXpathMap = new HashMap<String, String>();
		static{
			typeToXpathMap.put("BUTTON", relativeDescendants(BUTTON));

			typeToXpathMap.put("CHECKBOX", relativeDescendants(CHECKBOX));

			typeToXpathMap.put("COMBOBOX", relativeDescendants(COMBOBOX));

			typeToXpathMap.put("EDITBOX", relativeDescendants(EDITBOX));
			typeToXpathMap.put("INPUTFIELD", relativeDescendants(EDITBOX));
			typeToXpathMap.put("TEXTBOX", relativeDescendants(EDITBOX));
			typeToXpathMap.put("TEXTAREA", relativeDescendants(EDITBOX));

			typeToXpathMap.put("COLOR", relativeDescendants(COLOR));
			typeToXpathMap.put("DATETIME", relativeDescendants(DATETIME));
			typeToXpathMap.put("FILE", relativeDescendants(FILE));

			typeToXpathMap.put("RADIO", relativeDescendants(RADIO));
			typeToXpathMap.put("RADIOBUTTON", relativeDescendants(RADIO));

			typeToXpathMap.put("RANGE", relativeDescendants(RANGE));
			typeToXpathMap.put("IMAGE", relativeDescendants(IMAGE));
			typeToXpathMap.put("LIST", relativeDescendants(LIST));
			typeToXpathMap.put("TREE", relativeDescendants(TREE));
			typeToXpathMap.put("TABLE", relativeDescendants(TABLE));
		}

		/**
		 * @param type String, represents the WebElement's type, such as "ComboBox", "Button" etc.
		 * @return String, represents the XPATH according to which to find the WebElement. For example <b>XPATH.relativeDescendants(XPATH.COMBOBOX)</b> is used to find the ComboBox elements.
		 */
		public static String ofType(String type){
			return typeToXpathMap.get(type.toUpperCase());
		}

		/**
		 * Test if the element's type can match what is expected.
		 * @param element WebElement, the element to check.
		 * @param type String, the expected type, such as EditBox, RadioButton, ComboBox etc.
		 * @return boolean true if the web-element matches the expected type.
		 */
		public static boolean matched(WebElement element, String type){

			//according to the type, generate the xpath (used for web-element searching).
			//then we check the element's tagname and/or its attribute 'role', 'type', 'class' etc. to see if the element is what we want.
			String xpath = ofType(type);
			String tagName = element.getTagName();

			String role = element.getAttribute(HTMLConst.ATTRIBUTE_ROLE);
			if(StringUtils.isValid(role) && xpath.contains("@"+HTMLConst.ATTRIBUTE_ROLE+"='"+role.toLowerCase()+"'")) return true;

			String _type = element.getAttribute(HTMLConst.ATTRIBUTE_TYPE);
			if(StringUtils.isValid(_type) && HTMLConst.TAG_INPUT.equalsIgnoreCase(tagName) && xpath.contains("@"+HTMLConst.ATTRIBUTE_TYPE+"='"+_type.toLowerCase()+"'")) return true;

			String cssclass = element.getAttribute(HTMLConst.ATTRIBUTE_CLASS);
			if(StringUtils.isValid(cssclass)){
				String classes[] = cssclass.split(" ");
				for(String clazz: classes){
					if(xpath.contains("contains(concat(' ',@"+HTMLConst.ATTRIBUTE_CLASS+",' '), ' "+clazz+" ')")) return true;
				}
			}

			//Check the tag name, such as <img> <textarea> <table> <select> etc.
			if(StringUtils.isValid(tagName) && !tagName.equalsIgnoreCase(HTMLConst.TAG_INPUT) && xpath.contains(XPATH.DESCENDANTS_PREFIX+tagName.toLowerCase())) return true;

			return false;
		}
	}

	/** Return a Recognition String of format "xpath=xxx" according to the text.*/
	public static String text(String text, boolean partial, boolean relative){
		return SearchObject.SEARCH_CRITERIA_XPATH+SearchObject.assignSeparator+XPATH.fromText(text, partial, relative);
	}

	/** Return a Recognition String of format "xpath=xxx" according to the pair (attribute,value).*/
	public static String attribute(String attribute, String value, boolean partial, boolean relative){
		return SearchObject.SEARCH_CRITERIA_XPATH+SearchObject.assignSeparator+XPATH.fromAttribute(attribute, value, partial, relative);
	}

	/**
	 * This class contains methods returning an css-selector.
	 */
	public abstract static class CSS{
		/** 'contains' is not supported in CSS 3 anymore, so neither by selenium webdriver */
		private static final String DEPRECATED_FUNCTION_CONTAINS 	= "contains";
		/** An array of deprecated CSS functions not supported in selenium webdriver */
		private static final String[] DEPRECATED_FUNCTIONS 	= {DEPRECATED_FUNCTION_CONTAINS};

		/** ':' */
		private static final String FUNCTION_COLON 					= ":";
		/** '(' */
		private static final String FUNCTION_LEFT_PAREN 			= "(";
		/** ')' */
		private static final String FUNCTION_RIGHT_PAREN 			= ")";

		public static String from(String tagName, String className){
			StringBuffer cssselector = new StringBuffer();
			cssselector.append(tagName);
			cssselector.append("[class='"+className+"']");
			return cssselector.toString();
		}

		/**
		 * Some CSS function is not supported anymore, such as contains(), refer to defect
		 * <a href="https://github.com/SeleniumHQ/selenium-google-code-issue-archive/issues/1547">contains() is NOT valid in CSS3</a>
		 *
		 * @param cssSelector String the CSS selector
		 * @return boolean if this CSS selector contains any deprecated function
		 */
		public static boolean isDeprecated(String cssSelector){
			for(String function:DEPRECATED_FUNCTIONS){
				if(cssSelector.contains(function+FUNCTION_LEFT_PAREN)){
					return true;
				}
			}
			return false;
		}

		/**
		 * @param cssSelector String, the deprecated CSS selector needs to be converted
		 * @return By, the converted selector
		 */
		public static By convert(String cssSelector){
			By result = null;
			//E:contains('sub string')  --> //E[contains(text(), 'sub string')]
			String trimmedCssSelector = cssSelector.trim();
			if(trimmedCssSelector.endsWith(FUNCTION_RIGHT_PAREN)){
				String startPivot = FUNCTION_COLON+DEPRECATED_FUNCTION_CONTAINS+FUNCTION_LEFT_PAREN;// :contains(
				int startIndex = trimmedCssSelector.indexOf(startPivot);
				int endIndex = trimmedCssSelector.indexOf(FUNCTION_RIGHT_PAREN);
				if(startIndex>-1 && endIndex>-1 && endIndex>startIndex){
					String tagName = trimmedCssSelector.substring(0, startIndex);
					String text = trimmedCssSelector.substring(startIndex+startPivot.length(), endIndex);
					result = By.xpath(XPATH.fromText(tagName, text, true, false));
				}
			}

			if(result==null){
				IndependantLog.warn(StringUtils.debugmsg(false)+" don't how to convert deprected css selector '"+cssSelector+"'!");
			}else{
				IndependantLog.debug(StringUtils.debugmsg(false)+" css selector '"+cssSelector+"' has been converted to "+result);
			}

			return result;
		}
	}

	/** Return a Recognition String of format "css=xxx" according to the tagName and cssClassName.*/
	public static String css(String tagName, String cssClassName){
		return SearchObject.SEARCH_CRITERIA_CSS+SearchObject.assignSeparator+CSS.from(tagName, cssClassName);
	}

	/** Return a Recognition String of format "id=xxx".*/
	public static String id(String id){
		return SearchObject.SEARCH_CRITERIA_ID+SearchObject.assignSeparator+id;
	}

	/** Return a Recognition String of format "xpath=xxx".*/
	public static String xpath(String xpath){
		return SearchObject.SEARCH_CRITERIA_XPATH+SearchObject.assignSeparator+xpath;
	}

	private static void testNormalizeSingleQuote(){
		String text = "hello";
		String expected = "'hello'";
		String actual = XPATH.quote(text);

		if(!expected.equals(actual)){
			System.err.println("We expect: "+expected+" | But actual: "+actual);
		}else{
			System.out.println(actual);
		}

		text = "hello\"work";
		expected = "'hello\"work'";
		actual = XPATH.quote(text);
		if(!expected.equals(actual)){
			System.err.println("We expect: "+expected+" | But actual: "+actual);
		}else{
			System.out.println(actual);
		}

		text = " hello ' world ";
		expected = "concat(' hello ', \"'\", ' world ')";
		actual = XPATH.quote(text);
		if(!expected.equals(actual)){
			System.err.println("We expect: "+expected+" | But actual: "+actual);
		}else{
			System.out.println(actual);
		}

		text = "hello'e ll' word";
		expected = "concat('hello', \"'\", 'e ll', \"'\", ' word')";
		actual = XPATH.quote(text);
		if(!expected.equals(actual)){
			System.err.println("We expect: "+expected+" | But actual: "+actual);
		}else{
			System.out.println(actual);
		}
	}

	public static String getXPATHForControls(){
		StringBuilder xpathForControls = new StringBuilder();
		xpathForControls.append("EDITBOX		"+Arrays.toString(RS.XPATH.EDITBOX)+"\n");
		xpathForControls.append("BUTTON		"+Arrays.toString(RS.XPATH.BUTTON)+"\n");
		xpathForControls.append("COMBOBOX	"+Arrays.toString(RS.XPATH.COMBOBOX)+"\n");
		xpathForControls.append("CHECKBOX	"+Arrays.toString(RS.XPATH.CHECKBOX)+"\n");
		xpathForControls.append("COLOR		"+Arrays.toString(RS.XPATH.COLOR)+"\n");
		xpathForControls.append("DATETIME	"+Arrays.toString(RS.XPATH.DATETIME)+"\n");
		xpathForControls.append("FILE		"+Arrays.toString(RS.XPATH.FILE)+"\n");
		xpathForControls.append("RADIO		"+Arrays.toString(RS.XPATH.RADIO)+"\n");
		xpathForControls.append("RANGE		"+Arrays.toString(RS.XPATH.RANGE)+"\n");
		xpathForControls.append("IMAGE		"+Arrays.toString(RS.XPATH.IMAGE)+"\n");
		xpathForControls.append("LIST		"+Arrays.toString(RS.XPATH.LIST)+"\n");
		xpathForControls.append("TREE		"+Arrays.toString(RS.XPATH.TREE)+"\n");
		xpathForControls.append("TABLE		"+Arrays.toString(RS.XPATH.TABLE)+"\n");
		return xpathForControls.toString();
	}

	public static void main(String[] args){
		testNormalizeSingleQuote();
		System.out.println(getXPATHForControls());
	}
}
