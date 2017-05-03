/**
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
/**
 *
 * History:
 *
 *  Jun 25, 2014    (Lei Wang) Initial release.
 *  OCT 29, 2014    (Lei Wang) Move some XPATH related codes from SearchObject to here.
 *  FEB 03, 2016    (Lei Wang) Modify conditionXXX(): to avoid the InvalidSelectorException
 *                           caused by apostrophe existing in the text/value.
 */
package org.safs.selenium.webdriver.lib;

import java.util.List;

import org.openqa.selenium.SearchContext;
import org.safs.IndependantLog;
import org.safs.StringUtils;

/**
 * A convenient class to create xpath, cssSelector to create a By searching criterion for Selenium to find WebElement.<br>
 * It also provides "Recognition String" such as "xpath=xxx", "css=xxx", "id=xxx" etc. for SearchObject to find WebElement.<br>
 *
 */
public class RS{

	/**
	 * This class contains methods returning an xpath.
	 */
	public static class XPATH{
		/** <b>//*[</b>, it is used as beginning to construct an xpath for searching elements under ENTIRE document. */
		public static final String MATCHING_ALL_START  			= "//*[";
		/** <b>.//*[</b>, it is used as beginning to construct an xpath for searching elements under current WebElement. */
		public static final String RELATIVE_MATCHING_ALL_START  	= ".//*[";
		/** <b>//DIV[</b>, it is used as beginning to construct an xpath for searching DIV elements under ENTIRE document.*/
		public static final String MATCHING_DIV_START  			= "//DIV[";
		/** <b>.//DIV[</b>, it is used as beginning to construct an xpath for searching DIV elements under current WebElement.*/
		public static final String RELATIVE_MATCHING_DIV_START  	= ".//DIV[";
		/** <b>(1=1)</b>, an always true condition to concatenate other conditions to create an XPAH for searching. */
		public static final String TRUE_CONDITION  						= " (1=1) ";
		/** <b>]</b> */
		public static final String END  					= "]";
		/** <b>and</b> */
		public static final String AND  					= "and";
		/** <b>or</b> */
		public static final String OR  					= "or";

		/** <b>return '/html'</b> */
		public static final String html(){ return "/html";}

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
			String xpath = null;

			if(relative){
				xpath = RELATIVE_MATCHING_ALL_START+conditionForText(text, partial)+END;
			}else{
				xpath = MATCHING_ALL_START+conditionForText(text, partial)+END;
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
			String xpath = null;

			if(relative){
				xpath = RELATIVE_MATCHING_ALL_START+condition(attribute, value, partial)+END;
			}else{
				xpath = MATCHING_ALL_START+condition(attribute, value, partial)+END;
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
				return " contains(text(), "+normalizedText+")";
			}else{
				return " text()="+normalizedText;
			}
		}

		/**
		 * Add single quote around the text (such as 'text'), and it will be used in XPATH for searching a web element in DOM.<br>
		 * If the text (such as Tom's) contains single quote, we cannot simply quote the text with single quote, which will cause the error;<br>
		 * instead we will use the function concat() to connect each part of the text, such as concat('Tom', "'", 's').<br>
		 *
		 * @param text String, the text of the web element.
		 * @return String, the quoted text; or a string combined by function concat().
		 */
		protected static String quote(String text){
			String normalized = "'" + text +"'";

			//if text contains single quote, we should not simply quote it with single quote.
			//we will use the function concat()
			if(text!=null){
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
	public static class CSS{

		public static String from(String tagName, String className){
			StringBuffer cssselector = new StringBuffer();
			cssselector.append(tagName);
			cssselector.append("[class='"+className+"']");
			return cssselector.toString();
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

	public static void main(String[] args){
		testNormalizeSingleQuote();
	}
}
