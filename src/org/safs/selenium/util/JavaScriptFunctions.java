/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.util;
/**
 * History:<br>
 * 
 *  <br>   AUG 10, 2012    (Lei Wang) Initial release.
 *  <br>   DEC 25, 2013    (Lei Wang) Add javascript calling DOJO APIs.
 *  <br>   JAN 15, 2014    (Lei Wang) Add javascript functions to support Dojo combo box (FilteringSelect, ComboBox, and Select)
 *  <br>   APR 11, 2014    (Lei Wang) Fix global variable by adding prefix "window.", 
 *                                  Add methods to attach/detach browser event (like 'click' 'blur' etc.) to a SAP object.
 *  <br>   APR 18, 2014    (Lei Wang) Add methods to attach/detach browser event (like 'click' 'blur' etc.) to a DOJO/HTML object. 
 *  <br>   APR 22, 2014    (Lei Wang) Add methods to handle sap.ui.commons.TabStrip
 *  <br>   APR 23, 2014    (Lei Wang) Add methods to handle dijit.layout.TabContainer
 *  <br>   NOV 28, 2014    (Lei Wang) Modify SAP.parse_sap_m_ListItemBase(): get 'tooltip' from sas.hc.m.CustomListItem for value, it
 *                                         is a temporary fix, developer should set item value to property 'text'.
 *                                         Put "instanceof test" of special packages (sap.suite, sap.ca ) in try-catch.
 *                                  Add some fields/methods to handle javascript global error object so that java side
 *                                  can detect the global error code and get the error object of javascript execution.
 *                                  Add method throw_error().
 *                                  Use SAFS_JAVASCRIPT_GLOBAL_ERROR_OBJECT_VAR to contain error object instead of error message.
 *  <br>   DEC 31, 2014    (Lei Wang) Add codes to handle sas.hc.ui.commons.pushmenu.PushMenu, see method sas_hc_ui_commons_pushmenu_PushMenu_getItems().
 *                                  Handle the debug message during execution of js-codes, see method debug().
 *  <br>   JAN 12, 2015    (Lei Wang) Modify initializeJSArray(): put the codes into try{} clause to avoid exception.
 *                                  If some SAP js module is not loaded, we cannot refer to the class inside it; otherwise
 *                                  we will get an Exception. For example, "sap.m" is not always loaded, we will get Exception if we call following
 *                                  var clazz = sap.m.Select;
 *                                  if(object instanceof sap.m.ComboBoxBase)
 *                                  We need to put this kind of calling in a try catch clause.
 *  <br>   FEB 05, 2015    (Lei Wang) Modify parse_sap_m_ListItemBase(): Try property 'title' at last, and log debug message if no text is set.
 *  <br>   JUL 23, 2015    (Lei Wang) Add sendHttpRequest(): send HTTP request by AJAX.
 *  <br>   OCT 12, 2015    (Lei Wang) Add sap_getProperty(): get value of a property for a SAP object.
 *                                  
 */
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.text.WordUtils;
import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.net.IHttpRequest.HttpResponseStatus;
import org.safs.net.IHttpRequest.Key;
import org.safs.net.XMLHttpRequest;
import org.safs.net.XMLHttpRequest.AjaxReadyState;
import org.safs.selenium.webdriver.lib.ComboBox.SapSelect_ComboBox;
import org.safs.selenium.webdriver.lib.Component;
import org.safs.selenium.webdriver.lib.Menu.SapSelectable_Menu;
import org.safs.selenium.webdriver.lib.ScrollBar;
import org.safs.selenium.webdriver.lib.SearchObject;
import org.safs.selenium.webdriver.lib.SearchObject.BrowserWindow;
import org.safs.selenium.webdriver.lib.model.Element;
import org.safs.selenium.webdriver.lib.model.HierarchicalElement;
import org.safs.selenium.webdriver.lib.model.Item;
import org.safs.selenium.webdriver.lib.model.MenuItem;
import org.safs.selenium.webdriver.lib.model.Option;
import org.safs.selenium.webdriver.lib.model.TreeNode;
import org.safs.text.FileUtilities;

/**
 * This class is for Temporary usage. Refer to user-extension.js<br/>
 * You should keep this file and user-extension.js synchronized.<br/>
 * All methods should be loaded through <b>selenium.getEval()</b><br/>
 * For example: selenium.getEval(getSAFSgetElementFromXpathFunction());<br/>
 * We can load all the method by selenium.getEval(getAllFunctions());<br/>
 * <br>
 * This class contains a main method, user can run this class as a standard java application,<br>
 * it will output all the javascript to a file named SeleniumPlus.js<br>
 * <br>
 * 
 * <b>Problem in Selenium 1.0:</b><br/>
 * The injected javascript just can NOT operate on the test page, but on the playback page.<br/>
 * If we get javascript function as a string and pass it to selenium.getEval(), it works on the test page.<br/>
 * In future, if this problem is resolved, this class will be no useful.<br/>
 * <br>
 * 
 * <b>How to call in java?</b><br/>
 * <ul>
 * <li>Used with Selenium 1.0
 * <pre>
		StringBuffer scriptCommand = new StringBuffer();
		scriptCommand.append(JavaScriptFunctions.getSAFSgetElementFromXpathFunction(false));
		scriptCommand.append(JavaScriptFunctions.getSAFSgetAttributeFunction(false));
		scriptCommand.append(" var xpath = \""+compObject.getLocator()+"\";");
		scriptCommand.append(" var prop = \""+prop+"\";");
		scriptCommand.append(" SAFSgetAttribute(xpath,prop);");
		rval = selenium.getEval(scriptCommand.toString());
 * </pre>
 * <li>Used with Selenium 2.0
 * <pre>
		StringBuffer scriptCommand = new StringBuffer();
		scriptCommand.append(JavaScriptFunctions.getDojoClassNameByCSSSelector(true));
		scriptCommand.append(" return getDojoClassNameByCSSSelector(arguments[0]);");
		Object obj = ((JavascriptExecutor) webdriver).executeScript(scriptCommand.toString(), css);
 * </pre>
 * </ul>
 * <b>About parameter includeDependency?</b><br/>
 * You may set this parameter as true to include necessary javascript code for simplicity.<br>
 * <pre>
		StringBuffer scriptCommand = new StringBuffer();
		scriptCommand.append(JavaScriptFunctions.getDojoClassNameByCSSSelector(<b><font color='red'>true</font></b>));
		scriptCommand.append(" return getDojoClassNameByCSSSelector(arguments[0]);");
		Object obj = ((JavascriptExecutor) webdriver).executeScript(scriptCommand.toString(), css);
 * </pre>
 * It is better to set this parameter as false to avoid dead including loop.<br>
 * You need to read the doc of the method to know which javascript it depends on, and include them by yourself.<br>
 * <pre>
		StringBuffer scriptCommand = new StringBuffer();
		scriptCommand.append(JavaScriptFunctions.getDojoClassNameByCSSSelector(<b><font color='red'>false</font></b>));
		//getDojoClassNameByCSSSelector() depends on getDomNodesByCSSSelector(), include it manually.
		scriptCommand.append(JavaScriptFunctions.getDomNodesByCSSSelector(<b><font color='red'>false</font></b>));
		scriptCommand.append(" return getDojoClassNameByCSSSelector(arguments[0]);");
		Object obj = ((JavascriptExecutor) webdriver).executeScript(scriptCommand.toString(), css);
 * </pre>
 * 
 * <b>About method comments?</b><br/>
 * For the methods defined in this class, sevearl tags have been used:<br>
 * <ul>
 * <li><b>depending on:</b> a list of methods names that this method will depend on.<br>
 * <li><b>depending level:</b> an integer to show the level.<br>
 *                             0 if this method depends on nothing.<br>
 *                             1 if this method depends on methods of level 0, and so on.<br>
 * <li><b>depended by:</b> a list of methods names that depend on this method<br>
 * <b>Note:</b><br>
 * If we want to add a method, it can depend on method of level 0 without any problem. But if it will<br>
 * depend on a method with level bigger than 0, we should be careful, make sure that all the depended<br>
 * methods will NOT depend on the method we are writing.<br>
 * </ul>
 * <br>
 */
public class JavaScriptFunctions {
	/**Used to turn on/off the output of javascript functions to debug log.*/
	public static boolean DEBUG_OUTPUT_JAVASCRIPT_FUNCTIONS = false;
	/**Used to enable/disable debug message output of execution of javascript.*/
	public static boolean jsDebugLogEnable = false;
	public static void setJsDebugLogEnable(boolean enable){ jsDebugLogEnable = enable; }
	
	/**
	 * @return the string code of SAFSgetElementFromXpath(xpath) defined in user-extension.js
	 */
	public static String getSAFSgetElementFromXpathFunction(){
		return getSAFSgetElementFromXpathFunction(false);
	}
	/**
	 * @param includeDependency boolean, if true the output string will contain the depending javascript functions.
	 * @return the string code of SAFSgetElementFromXpath(xpath) defined in user-extension.js
	 */
	public static String getSAFSgetElementFromXpathFunction(boolean includeDependency){
		StringBuffer scriptCommand = new StringBuffer();
		scriptCommand.append("function SAFSgetElementFromXpath(xpath){\n");
		scriptCommand.append("   var element;\n");
		
		scriptCommand.append("   if(xpath=='//HTML[1]/' || xpath=='//HTML[1]'){\n");
		scriptCommand.append("     element = window.document.body;\n");
		scriptCommand.append("   } else {\n");
		scriptCommand.append("     try{ \n");
		scriptCommand.append("       //debug('Try Selenium Javascript API to get element----'); \n");
//		scriptCommand.append("       element = BrowserBot.prototype._findElementUsingFullXPath(xpath,window.document);\n");
		scriptCommand.append("       element = BrowserBot.prototype.locateElementByXPath(xpath,window.document,window);\n");
//		scriptCommand.append("       element = BrowserBot.prototype.findElementByXPath(xpath,window.document);\n");
		scriptCommand.append("       //debug('Selenium Javascript API got element: '+element); \n");
		scriptCommand.append("     }catch(x){\n");
		scriptCommand.append("       //debug('Try Selenium Javascript API to get element, Exception.'+x); \n");
		scriptCommand.append("       element = null;\n");
		scriptCommand.append("     }\n");
		
		scriptCommand.append("     if((element == null)||(element == 'undefined')){\n");
		scriptCommand.append("       try{\n");
		scriptCommand.append("         //For Mozilla, FireFox etc.\n");
		scriptCommand.append("         if(window.document.evaluate){\n");
		scriptCommand.append("           //debug('Try window.document.evaluate() to get element.'); \n");
		scriptCommand.append("           element = window.document.evaluate(xpath,window.document, null, XPathResult.ANY_TYPE, null).iterateNext();\n");
		scriptCommand.append("           //debug('Mozilla: Window.document.evaluate() get element '+element); \n");
		scriptCommand.append("         //For IE.\n");
		scriptCommand.append("         }else if (window.ActiveXObject){\n");
		scriptCommand.append("           //debug('Try window.document.SelectSingleNode() to get element');\n");
		scriptCommand.append("           //SelectSingleNode() or selectNodes() is not supported by IE HTML Document, there are method of w3c Document!!! \n");
		scriptCommand.append("           element = window.document.SelectSingleNode(xpath);\n");
		scriptCommand.append("           //debug('IE: selectSingleNode() get element '+element);\n");
		scriptCommand.append("         }\n");
		scriptCommand.append("       }catch(x){\n");
		scriptCommand.append("         //debug('Try Document API, Exception: '+x);\n");
		scriptCommand.append("         element = null;\n");
		scriptCommand.append("       }\n");
		scriptCommand.append("     }\n");
		scriptCommand.append("   }\n");
		
		scriptCommand.append("   return element;\n");
		scriptCommand.append(" }\n");
		
		return scriptCommand.toString();
	}	
	
	/**
	 * Support highlight by id, name or xpath.
	 */
	public static String getHighlightFunction(){
		return getHighlightFunction(false);
	}
	/**
	 * 
	 * @param includeDependency boolean, if true the output string will contain the depending javascript functions.
	 * @param xpathOrIdOrName (<b>Javascript</b>) String, used to search a DOM object.
	 * 
	 */
	public static String getHighlightFunction(boolean includeDependency){
		StringBuffer scriptCommand = new StringBuffer();
		if(includeDependency){
			scriptCommand.append(getSAFSgetElementFromXpathFunction());
		}
		//To keep compability, I need always include function highlight2()
		scriptCommand.append(highlight2());
		
		scriptCommand.append("function highlight(xpathOrIdOrName){\n");
		scriptCommand.append("   var element;\n");
		
		scriptCommand.append("   try{\n");
		scriptCommand.append("     element = SAFSgetElementFromXpath(xpathOrIdOrName);\n");
		scriptCommand.append("   }catch(err){\n");
		scriptCommand.append("     //debug('Try get element by xpath, Met Error: '+err);\n");
		scriptCommand.append("   }\n");
		
		scriptCommand.append("   if(element == null || element == undefined){\n");
		scriptCommand.append("      //debug('Can not get element for xpath: '+xpathOrIdOrName);\n");
		scriptCommand.append("      try{\n");
		scriptCommand.append("        element = window.document.getElementById(xpathOrIdOrName);\n");
		scriptCommand.append("      }catch(ex){}\n");
		scriptCommand.append("      if(element == null || element == undefined){\n");
		scriptCommand.append("        //debug('Can not get element for id: '+xpathOrIdOrName);\n");
		scriptCommand.append("        try{\n");
		scriptCommand.append("          element = window.document.getElementsByName(xpathOrIdOrName)[0];\n");
		scriptCommand.append("        }catch(ex){}\n");
		scriptCommand.append("      }\n");
		scriptCommand.append("      if(element == null || element == undefined){\n");
		scriptCommand.append("        //debug('Can not get element for name: '+xpathOrIdOrName);\n");
		scriptCommand.append("      }\n");
		scriptCommand.append("   }\n");
		
		scriptCommand.append("   highlight2(element);\n");
		scriptCommand.append(" }\n");
		
		return scriptCommand.toString();
	}
	
	/**
	 * Highlight a DOM object with a red rectangle. <br>
	 * 
	 * <br><b>depending on: nothing.</b><br>
	 * <br><b>depending level: 0</b><br>
	 * 
	 * @param element (<b>Javascript</b>) DOM OBJECT
	 */	
	public static String highlight2(){
		StringBuffer scriptCommand = new StringBuffer();

		scriptCommand.append("function highlight2(element){\n");
		scriptCommand.append("   try{\n");
		scriptCommand.append("     if(element == null || element == undefined){\n");
		scriptCommand.append("        throw \"Element is null.\";\n");
		scriptCommand.append("     } else {\n");
		scriptCommand.append("       //debug('Highlight element: '+element); \n");
		scriptCommand.append("       window.document.body.focus();\n");
		scriptCommand.append("       if("+SAFS_JAVASCRIPT_GLOBAL_PREVIOUS_HIGHLIGHT_ELEMENT_VAR+" != undefined){\n");
		scriptCommand.append("         "+SAFS_JAVASCRIPT_GLOBAL_PREVIOUS_HIGHLIGHT_ELEMENT_VAR+".style.border="+SAFS_JAVASCRIPT_GLOBAL_PREVIOUS_HIGHLIGHT_ELEMENT_STYLE_VAR+";\n");
		scriptCommand.append("       }\n");
		scriptCommand.append("       "+SAFS_JAVASCRIPT_GLOBAL_PREVIOUS_HIGHLIGHT_ELEMENT_VAR+"=element;\n");
		scriptCommand.append("       "+SAFS_JAVASCRIPT_GLOBAL_PREVIOUS_HIGHLIGHT_ELEMENT_STYLE_VAR+"=element.style.border;");
		scriptCommand.append("       element.style.border='3px solid red';\n");
		scriptCommand.append("     }\n");
		scriptCommand.append("   }catch(err){\n");
		scriptCommand.append("     //debug('Fail to highlight, Met Error: '+err);\n");
		scriptCommand.append("   }\n");
		scriptCommand.append(" }\n");
		
		return scriptCommand.toString();
	}
	
	/**
	 * Scroll the browser page to a location. <br>
	 * 
	 * <br><b>depending on: nothing.</b><br>
	 * <br><b>depending level: 0</b><br>
	 * 
	 *  @param x (<b>Javascript</b>) int, the x coordinates to scroll to.
	 *  @param y (<b>Javascript</b>) int, the y coordinates to scroll to.
	 */
	public static String scrollTo(){
		StringBuffer scriptCommand = new StringBuffer();

		scriptCommand.append("function scrollTo(x, y){\n");
		scriptCommand.append("   window.scrollTo(x, y);\n");
		scriptCommand.append(" }\n");
		
		return scriptCommand.toString();
	}
	/**
	 * Scroll the browser page to a location by increment. <br>
	 * 
	 * <br><b>depending on: nothing.</b><br>
	 * <br><b>depending level: 0</b><br>
	 * 
	 *  @param x (<b>Javascript</b>) int, the increment of x coordinates to scroll.
	 *  @param y (<b>Javascript</b>) int, the increment of y coordinates to scroll.
	 */
	public static String scrollBy(){
		StringBuffer scriptCommand = new StringBuffer();
		
		scriptCommand.append("function scrollBy(x, y){\n");
		scriptCommand.append("   window.scrollBy(x, y);\n");
		scriptCommand.append(" }\n");

		return scriptCommand.toString();
	}
	/**
	 * Scroll the browser scroll up/down by lines. Only valid for FireFox.<br>
	 * 
	 * <br><b>depending on: nothing.</b><br>
	 * <br><b>depending level: 0</b><br>
	 * 
	 *  @param lines (<b>Javascript</b>) int, the number of lines to scroll.
	 */
	public static String scrollByLines(){
		StringBuffer scriptCommand = new StringBuffer();
		
		scriptCommand.append("function scrollByLines(lines){\n");
		scriptCommand.append("   window.scrollByLines(lines);\n");
		scriptCommand.append(" }\n");
		
		return scriptCommand.toString();
	}
	/**
	 * Scroll the browser page up/down by pages. Only valid for FireFox.<br>
	 * 
	 * <br><b>depending on: nothing.</b><br>
	 * <br><b>depending level: 0</b><br>
	 * 
	 *  @param pages (<b>Javascript</b>) int, the number of pages to scroll.
	 */
	public static String scrollByPages(){
		StringBuffer scriptCommand = new StringBuffer();
		
		scriptCommand.append("function scrollByPages(pages){\n");
		scriptCommand.append("   window.scrollByPages(pages);\n");
		scriptCommand.append(" }\n");
		
		return scriptCommand.toString();
	}
	/**
	 * Scroll to show the dom element in the browser's viewport.<br>
	 * 
	 * <br><b>depending on: nothing.</b><br>
	 * <br><b>depending level: 0</b><br>
	 * 
	 *  @param domobject (<b>Javascript</b>) DOM object, the dom object to show.
	 *  @param alignToTop (<b>Javascript</b>) boolean, true the top of the element will align the top of viewport of browser.
	 *                                                 false the bottom of the element will align the bottom of viewport of browser.
	 */
	public static String scrollIntoView(){
		StringBuffer scriptCommand = new StringBuffer();
		
		scriptCommand.append("function scrollIntoView(domobject, alignToTop){\n");
		scriptCommand.append("   try{\n");
		scriptCommand.append("     domobject.scrollIntoView(alignToTop);\n");
		scriptCommand.append("   }catch(err){\n");
		scriptCommand.append("     throw err;\n");
		scriptCommand.append("   }\n");
		scriptCommand.append(" }\n");
		
		return scriptCommand.toString();
	}
	
	/**
	 * Remove the previous highlight of a DOM object. <br>
	 * 
	 * <br><b>depending on: nothing.</b><br>
	 * <br><b>depending level: 0</b><br>
	 */	
	public static String clearHighlight(){
		StringBuffer scriptCommand = new StringBuffer();

		scriptCommand.append("function clearHighlight(){\n");
		scriptCommand.append("   try{\n");
		scriptCommand.append("     window.document.body.focus();\n");
		scriptCommand.append("     if("+SAFS_JAVASCRIPT_GLOBAL_PREVIOUS_HIGHLIGHT_ELEMENT_VAR+" != undefined){\n");
		scriptCommand.append("       "+SAFS_JAVASCRIPT_GLOBAL_PREVIOUS_HIGHLIGHT_ELEMENT_VAR+".style.border="+SAFS_JAVASCRIPT_GLOBAL_PREVIOUS_HIGHLIGHT_ELEMENT_STYLE_VAR+";\n");
		scriptCommand.append("     }\n");
		scriptCommand.append("   }catch(err){\n");
		scriptCommand.append("     //debug('Fail to highlight, Met Error: '+err);\n");
		scriptCommand.append("   }\n");
		scriptCommand.append(" }\n");
		
		return scriptCommand.toString();
	}
	
	public static String getSAFSgetAttributeFunction(){
		return getSAFSgetAttributeFunction(false);
	}
	/**
	 * 
	 * @param includeDependency boolean, if true the output string will contain the depending javascript functions.
	 * @return
	 */
	public static String getSAFSgetAttributeFunction(boolean includeDependency){
		StringBuffer scriptCommand = new StringBuffer();
		
		if(includeDependency){
			scriptCommand.append(getSAFSgetElementFromXpathFunction());
		}
		
		scriptCommand.append("function SAFSgetAttribute(xpath, attribute){\n");
		scriptCommand.append("  var res = SAFSgetElementFromXpath(xpath);\n");
		scriptCommand.append("  if(attribute == 'text' || attribute == 'innerText' || attribute == 'innertext'){\n");
		scriptCommand.append("    var text = res.innerText;\n");
		scriptCommand.append("    if(text == undefined){\n");
		scriptCommand.append("      text = res.textContent;\n");
		scriptCommand.append("    }\n");
		scriptCommand.append("    if(text == ''){\n");
		scriptCommand.append("      text = res.value;\n");
		scriptCommand.append("    }\n");
		scriptCommand.append("    if(text == undefined){\n");
		scriptCommand.append("      text = res.alt;\n");
		scriptCommand.append("    }\n");
		scriptCommand.append("    if(text == undefined){\n");
		scriptCommand.append("      text = '';\n");
		scriptCommand.append("    }\n");
		scriptCommand.append("    return text;\n");
		scriptCommand.append("  } else {\n");
		scriptCommand.append("    var value = res[attribute];\n");
		scriptCommand.append("    if(value == null || value == 'undefined' || value === ''){\n");
		scriptCommand.append("      value = res.getAttribute(attribute);\n");
		scriptCommand.append("    }\n");
		scriptCommand.append("    return value;\n");
		scriptCommand.append("  }\n");
		scriptCommand.append("}\n");
		return scriptCommand.toString();
	}
	
	public static final String SAFS_JAVASCRIPT_GLOBAL_ERROR_OBJECT_VAR = genGlobalVariableName("SAFS_JAVASCRIPT_GLOBAL_ERROR_OBJECT_VAR");
	public static final String SAFS_JAVASCRIPT_GLOBAL_ERROR_CODE_VAR = genGlobalVariableName("SAFS_JAVASCRIPT_GLOBAL_ERROR_CODE_VAR");
	public static final String SAFS_JAVASCRIPT_GLOBAL_DEBUG_MESSAGES_VAR = genGlobalVariableName("SAFS_JAVASCRIPT_GLOBAL_DEBUG_MESSAGES_VAR");
	public static final int ERROR_CODE_NOT_SET = -1;
	public static final int ERROR_CODE_RESERVED = 0;
	public static final int ERROR_CODE_EXCEPTION = 1;
	public static final int ERROR_CODE_A = 2;
	public static final int ERROR_CODE_B = 3;
	
	public static final String SAFS_JAVASCRIPT_GLOBAL_PREVIOUS_HIGHLIGHT_ELEMENT_VAR = genGlobalVariableName("previous");
	public static final String SAFS_JAVASCRIPT_GLOBAL_PREVIOUS_HIGHLIGHT_ELEMENT_STYLE_VAR = genGlobalVariableName("previousBorderStyle");
	
	public static final String PREFIX_GLOBAL = "window.";
	public static final String SUFFIX_FUNCTION = "_function";
	public static final String SUFFIX_DOJO_HANDLE = "_dojo_handle";
	/**
	 * Generate the javascrpt global function name; define the function under object window.
	 * @param function, String the name of the function.
	 * @return String, the javascrpt global function name, like "window.xxx_function".
	 */
	private static final String genGlobalFunctionName(String function){
		return addPrefixAndSuffix(PREFIX_GLOBAL, function, SUFFIX_FUNCTION);
	}
	/**
	 * Generate the javascrpt global variable name; define the variable under object window.
	 * @param variable, String the name of variable.
	 * @return String, the javascrpt global variable name, like "window.variableName".
	 */
	private static final String genGlobalVariableName(String variable){
		return addPrefixAndSuffix(PREFIX_GLOBAL, variable, null);
	}
	/**
	 * Generate the javascrpt global dojo-handle name; define the handle under object window.
	 * @param handle, String the name of handle.
	 * @return String, the javascrpt global variable name, like "window.xxx_dojo_handle".
	 */
	private static final String genGlobalDojoHandleName(String handle){
		return addPrefixAndSuffix(PREFIX_GLOBAL, handle, SUFFIX_DOJO_HANDLE);
	}
	/**
	 * @param prefix String, the prefix to add. if null, no prefix will be added.
	 * @param value String, the value to be added with prefix/suffix
	 * @param suffix String, the suffix to append. if null, no suffix will be appended.
	 * @return String, a value with prefix and suffix
	 */
	private static String addPrefixAndSuffix(String prefix, String value, String suffix){
		if(value==null) return null;			
		if(prefix==null && suffix==null) return value;
		
		StringBuffer sb = new StringBuffer();
		if(prefix!=null && !value.startsWith(prefix)) sb.append(prefix);
		sb.append(value);
		if(suffix!=null && !value.endsWith(suffix)) sb.append(suffix);
		return sb.toString();
	}
	
	//INDICATOR_FOR_CALLBACK_CALLED_VAR is just used for generating javascript file.
	private static final String INDICATOR_FOR_CALLBACK_CALLED_VAR = "INDICATOR_FOR_CALLBACK_CALLED_VAR";
	
	/**
	 * Initialize the variables related to highlight element, initialize it value to undefined. <br>
	 * 
	 * <br><b>depending on: nothing.</b><br>
	 * <br><b>depending level: 0</b><br>
	 */	
	public static String initPreviousHighlightElement(){
		StringBuffer scriptCommand = new StringBuffer();
		scriptCommand.append(SAFS_JAVASCRIPT_GLOBAL_PREVIOUS_HIGHLIGHT_ELEMENT_VAR+" = undefined;\n");
		scriptCommand.append(SAFS_JAVASCRIPT_GLOBAL_PREVIOUS_HIGHLIGHT_ELEMENT_STYLE_VAR+" = undefined;\n");
		return scriptCommand.toString();
	}	
	
	/**
	 * Initialize the error code global variable, set it value to {@link #ERROR_CODE_NOT_SET}. <br>
	 * Initialize the error object global variable, set it value to undefined. <br>
	 * 
	 * <br><b>depending on: nothing.</b><br>
	 * <br><b>depending level: 0</b><br>
	 */
	public static String initJSError(){
		StringBuffer scriptCommand = new StringBuffer();
		scriptCommand.append(SAFS_JAVASCRIPT_GLOBAL_ERROR_CODE_VAR+" = "+ERROR_CODE_NOT_SET+";\n");
		scriptCommand.append(SAFS_JAVASCRIPT_GLOBAL_ERROR_OBJECT_VAR+" = undefined;\n");
		return scriptCommand.toString();
	}
	
	/**
	 * Initialize the global variable 'debug-message-array', set it to new Array(). <br>
	 * 
	 * <br><b>depending on: nothing.</b><br>
	 * <br><b>depending level: 0</b><br>
	 */
	public static String initJSDebugArray(){
		return SAFS_JAVASCRIPT_GLOBAL_DEBUG_MESSAGES_VAR+" = new Array();\n";
	}
	/**
	 * Add a message to the global variable 'debug-message-array'. The parameter is an javascript variable<br>
	 * name representing the 'debug message'.<br>
	 * Before executing this script, script {@link #initJSDebugArray()} MUST have been executed.<br>
	 * This method is used in {@link #debug()}<br>
	 * 
	 * <br><b>depending on: nothing.</b><br>
	 * <br><b>depending level: 0</b><br>
	 * 
	 * @see #initJSDebugArray()
	 * @see #debug()
	 */
	private static String addToJSDebugArray(String messageVar){
		return SAFS_JAVASCRIPT_GLOBAL_DEBUG_MESSAGES_VAR+".push("+messageVar+");\n";
	}
	/**
	 * Get the global variable 'debug-message-array'. <br>
	 * Before executing this script, script {@link #initJSDebugArray()} MUST have been executed.
	 * 
	 * <br><b>depending on: nothing.</b><br>
	 * <br><b>depending level: 0</b><br>
	 * 
	 * @see #initJSDebugArray()
	 */
	public static String getJSDebugArray(){
		return "return "+SAFS_JAVASCRIPT_GLOBAL_DEBUG_MESSAGES_VAR+";\n";
	}
	
	/**
	 * Clean the error code global variable, reset it to {@link #ERROR_CODE_NOT_SET}. <br>
	 * Clean the error object global variable, reset it to undefined. <br>
	 * 
	 * <br><b>depending on: nothing.</b><br>
	 * <br><b>depending level: 0</b><br>
	 */	
	public static String cleanJSError(){
		StringBuffer scriptCommand = new StringBuffer();
		scriptCommand.append(SAFS_JAVASCRIPT_GLOBAL_ERROR_CODE_VAR+" = "+ERROR_CODE_NOT_SET+";\n");
		scriptCommand.append(SAFS_JAVASCRIPT_GLOBAL_ERROR_OBJECT_VAR+" = undefined;\n");
		return scriptCommand.toString();
	}
	/** Get the value of error code global variable.*/
	public static String getJSErrorCode(){
		return "return "+SAFS_JAVASCRIPT_GLOBAL_ERROR_CODE_VAR+";\n";
	}
	/** Set the value of the error code global variable.*/
	public static String setJSErrorCode(int errorCode){
		return SAFS_JAVASCRIPT_GLOBAL_ERROR_CODE_VAR+" = "+errorCode+";\n";
	}
	/** Get the value of error Object global variable.*/
	public static String getJSErrorObject(){
		return "return "+SAFS_JAVASCRIPT_GLOBAL_ERROR_OBJECT_VAR+";\n";
	}
	/**
	 * Set the value of the error Object global variable.<br>
	 * @param errorObject String, the javascript variable containing the error Object.<br>
	 *                             or the "error message" itself, which should be passed in with double-quote.<br>
	 * 
	 * <br><b>depending on: nothing.</b><br>
	 * <br><b>depending level: 0</b><br>
	 * @example
	 * <pre>
	 * {@code
	 * //"error" is a javascript variable containing an error object.
	 * String errorObjectJSVarName = "error";
	 * JavaScriptFunctions.setJSErrorObject(errorObjectJSVarName);
	 * String errorMessage = "The real error message";
	 * JavaScriptFunctions.setJSErrorObject(StringUtils.quote(errorMessage));
	 * }
	 * </pre>
	 */
	public static String setJSErrorObject(String errorObject){
		return SAFS_JAVASCRIPT_GLOBAL_ERROR_OBJECT_VAR+" = "+errorObject+";\n";
	}
	/**
	 * Used to set the global "error code" and "error object", so that <br>
	 * in the java code, we can detect the global errorcode and errorobject to do some necessary work.<br>
	 * <pre>
	 * How to use it in javascript:
	 *   try{
	 *     //do some javascript actions
	 *   }catch(err){
	 *     throw_error(err);
	 *   }
	 * An actual example, refer to {@link SAP#sap_m_List_getItems(boolean)}
	 * 
	 * How to catch this error in java:
	 * //Reset the global error (code and message)
	 * SearchObject.js_cleanError();
	 * //Append the method throw_error() to the existing javascript code
	 * script = JavaScriptFunctions.throw_error()+script;
	 * //Execute javascript code
	 * ...
	 * //Detect the error code/object
	 * int errorcode = js_getErrorCode();
	 * if(errorcode!=JavaScriptFunctions.ERROR_CODE_NOT_SET){
	 *   Object error = SearchObject.js_getErrorObject();
	 *   throw JSException.instance(error, errorcode);
	 * }
	 * An actual example, refer to {@link SearchObject#executeScript(boolean, String, Object...)}
	 * </pre>
	 * 
	 * <br><b>depending on: nothing</b><br>
	 * <br><b>depending level: 0</b><br>
	 * 
	 * @param error (<b>Javascript</b>) Object, the javascript exception Error object.
	 */	
	public static String throw_error(){
		StringBuffer scriptCommand = new StringBuffer();
		
		final String errorVariable = "error"; 
		scriptCommand.append("function throw_error("+errorVariable+"){\n");
		scriptCommand.append("  "+setJSErrorCode(ERROR_CODE_EXCEPTION));
		scriptCommand.append("  "+setJSErrorObject(errorVariable));
		scriptCommand.append("}\n");
		
		return scriptCommand.toString();
	}
	
	/**
	 * Used to add debug messages the global array, so that <br>
	 * in the java code, we can get the javascript debug messages write them to debug-Log.<br>
	 * <pre>
	 * How to use it in javascript:
	 *   //do some javascript actions
	 *   debug('the parameter is XXX.');
	 *   //do some other javascript actions
	 *   debug('something is wrong.');
	 * 
	 * How to get these debug messages in java:
	 * //Reset the global debug message array, and include the debug() method.
	 * SearchObject.js_initJSDebugArray();
	 * try{
	 *   //Append the method debug() to the existing javascript code
	 *   script = JavaScriptFunctions.debug()+script;
	 *   //Then execute the javascript code
	 *   ...
	 * }catch(Exception e){
	 * 
	 * }finally{
	 *   List<?> messages = js_getJSDebugArray();
	 *   for(Object message: messages){
	 *     IndependantLog.debug(message);
	 *   }
	 * }
	 * An actual example, refer to {@link SearchObject#executeScript(boolean, String, Object...)}
	 * </pre>
	 * 
	 * <br><b>depending on: nothing</b><br>
	 * <br><b>depending level: 0</b><br>
	 * 
	 * @param message (<b>Javascript</b>) String, the javascript exception Error object.
	 */		
	public static String debug(){
		StringBuffer scriptCommand = new StringBuffer();
		
		final String msgVar = "message";
		final String currenttimeVar = "currenttime";
		scriptCommand.append("function debug("+msgVar+"){\n");
		if(jsDebugLogEnable){
			scriptCommand.append("  var currentdate = new Date();\n");
			scriptCommand.append("  var "+currenttimeVar+" = 'JS Log time '+ currentdate.getHours() + ':' + currentdate.getMinutes() + ':' + currentdate.getSeconds() + '.'+currentdate.getMilliseconds()+'# ';\n");
			scriptCommand.append("  "+addToJSDebugArray(currenttimeVar+"+"+msgVar));
		}
		scriptCommand.append("}\n");
		
		return scriptCommand.toString();
	}
	
	/**
	 * Define a javascript callback function; which will simply set a global variable to true.
	 * @param variable String, the global variable name.
	 * @return String, the javascript callback function.
	 */
	private static String defineGenericEventCallBack(String variable){
		StringBuffer scriptCommand = new StringBuffer();
		String callbackFunctionName = genGlobalFunctionName(variable);
		String globalVariable = genGlobalVariableName(variable);
		
		scriptCommand.append(globalVariable+" = false;\n");
		scriptCommand.append(callbackFunctionName+" = function(){\n");
//		scriptCommand.append("    alert('event fired.');\n");
		scriptCommand.append("    "+globalVariable+"=true;\n");
		scriptCommand.append("}\n");
		
		return scriptCommand.toString();
	}
	/**
	 * Remove the global variable, set it to undefined. <br>
	 * 
	 * <br><b>depending on: nothing.</b><br>
	 * <br><b>depending level: 0</b><br>
	 * 
	 * @param variable String, the name of the global variable.
	 * @see #genGlobalVariableName(String)
	 */
	public static String removeGlobalVariable(String variable){
		return genGlobalVariableName(variable)+" = undefined;\n";
	}
	/**
	 * Get the value of global variable. <br>
	 * 
	 * <br><b>depending on: nothing.</b><br>
	 * <br><b>depending level: 0</b><br>
	 * 
	 * @param variable String, the name of the global variable.
	 * @see #genGlobalVariableName(String)
	 */	
	public static String getGlobalVariable(String variable){
		//The returned javascript object needs to be parsed by yourself in java code.
		return "return "+genGlobalVariableName(variable)+";\n";
	}
	/**
	 * Set the value of global variable. <br>
	 * 
	 * <br><b>depending on: nothing.</b><br>
	 * <br><b>depending level: 0</b><br>
	 * 
	 * @param variable String, the name of the global variable.
	 * @param value String, the direct-value to set; or the name of an other javascript object.
	 * @see #genGlobalVariableName(String)
	 */		
	public static String setGlobalVariable(String variable, String value){
		String debugmsg = StringUtils.debugmsg(JavaScriptFunctions.class, "setGlobalVariable");
		if(value==null){
			IndependantLog.warn(debugmsg+" the value is null!");
		}
		return genGlobalVariableName(variable)+" = "+value+";\n";
	}
	
	/**
	 * Define a javascript object according to an Hashtable object.<br>
	 * 
	 * <pre>
	 *  StringBuffer jsScript = new StringBuffer();
	 *  Hashtable<String, Object> hash = new Hashtable<String, Object>();
	 *  hash.put("value", "AR");
	 *  hash.put("attribute1", "Hello");
	 *  hash.put("attribute2", new Date());
	 *  jsScript.append(JavaScriptFunctions.defineObject(hash));
	 *  jsScript.append("var hash = defineObject();");
	 *  jsScript.append("//write javascript code to handle hash object.");
	 *  WDLibrary.executeScript(jsScript.toString());
	 * </pre>
	 * 
	 * <br><b>depending on: nothing.</b><br>
	 * <br><b>depending level: 0</b><br>
	 * 
	 * @param properties Hashtable, contains <key, value> to define a javascript object.
	 */	
	public static String defineObject(Hashtable<String, Object> properties){
		StringBuffer scriptCommand = new StringBuffer();
		
		Enumeration<String> keys = properties.keys();
		String key = null;
		Object value = null;
		
		scriptCommand.append("function defineObject(){\n");
		scriptCommand.append("  try{\n");
		scriptCommand.append("    object = {\n");
		while(keys.hasMoreElements()){
			key = keys.nextElement();
			value = properties.get(key);
			if(value instanceof Boolean){
				scriptCommand.append(key+":"+value);
			}else{
				if(value.toString().indexOf("'")>-1){
					value = value.toString().replaceAll("'", "\\\\'");//replace ' by \'
				}
				scriptCommand.append(key+":'"+value+"'");
			}
			if(keys.hasMoreElements()) scriptCommand.append(", ");
			scriptCommand.append("\n");
		}
		scriptCommand.append("    }\n");
		scriptCommand.append("    return object;\n");
		scriptCommand.append("  }catch(error){}\n");
		scriptCommand.append("}\n");
		return scriptCommand.toString();
	}
	
	/**
	 * Compare 2 javascript objects.<br>
	 * 
	 * <pre>
	 *  StringBuffer jsScript = new StringBuffer();
	 *  List<String> properties = new List<String>();
	 *  properties.add("value");
	 *  properties.add("id");
	 *  properties.add("name");
	 *  jsScript.append(JavaScriptFunctions.compareObject(properties));
	 *  //object1 and object2 are javascript objects.
	 *  jsScript.append("return compareObject(object1, object2);");
	 *  boolean equaled = WDLibrary.executeScript(jsScript.toString());
	 * </pre>
	 * 
	 * <br><b>depending on: nothing.</b><br>
	 * <br><b>depending level: 0</b><br>
	 * 
	 * @param properties List, the property names to get value to compare between 2 objects.
	 * @param object1 (<b>Javascript</b>) Object, the javascript object to compare
	 * @param object2 (<b>Javascript</b>) Object, the javascript object to compare
	 */		
	public static String compareObject(List<String> properties){
		StringBuffer scriptCommand = new StringBuffer();
		
		scriptCommand.append("function compareObject(obj1, obj2){\n");
		scriptCommand.append("  equaled = false;\n");
		scriptCommand.append("  try{\n");
		scriptCommand.append("    if(obj1==obj2) equaled=true;\n");
		scriptCommand.append("    if(!equaled){\n");
		//compare each property of the 2 objects.
		if(!properties.isEmpty()){
			scriptCommand.append("      equaled=(");
			for(String property: properties){
				scriptCommand.append("obj1."+property+"==obj2."+property+" &&");
			}
			int lastAndSignIndex = scriptCommand.lastIndexOf("&&");
			scriptCommand.replace(lastAndSignIndex, scriptCommand.length(), ");\n");
		}
		scriptCommand.append("    }\n");
		scriptCommand.append("  }catch(error){}\n");
		scriptCommand.append("  return equaled;\n");
		scriptCommand.append("}\n");
		return scriptCommand.toString();	
	}

	/**
	 * Test if object is an instance of certain classes.<br>
	 * 
	 * <br><b>depending level: 0</b><br>
	 * 
	 * @param object (<b>Javascript</b>) Object, the javascript object.
	 * @param clazzes (<b>Javascript</b>) Array<JavaScriptClass>, the classes to compare with.
	 */	
	public static String objectIsInstanceof(){		
		StringBuffer scriptCommand = new StringBuffer();
		
		scriptCommand.append("function objectIsInstanceof(object, clazzes){\n");
		scriptCommand.append("  try{\n");
		scriptCommand.append("    if( object!=undefined && clazzes!=undefined ){\n");
		scriptCommand.append("      for(var i=0;i<clazzes.length;i++){\n");
		scriptCommand.append("        try{\n");
		scriptCommand.append("          if(object instanceof clazzes[i]) return true;\n");
		scriptCommand.append("        }catch(error){/*alert(error);*/}\n");
		scriptCommand.append("      }\n");
		scriptCommand.append("    }\n");
		scriptCommand.append("  }catch(error){\n");
		scriptCommand.append("    //alert(error);\n");
		scriptCommand.append("  }\n");
		scriptCommand.append("  return false;\n");
		scriptCommand.append("}\n");
		
		return scriptCommand.toString();
	}
	
	/**
	 * Find a DOM element according to the parameter By. By is an hash object containing keys like:<br>
	 * id, cssselector, xpath, name, class, tagname etc.<br>
	 * <br><b>depending level: 0</b><br>
	 * 
	 * @param By (<b>Javascript</b>) Object, the javascript object to be clicked.
	 */	
	public static String getDomElementBy(){		
		StringBuffer scriptCommand = new StringBuffer();
		
		scriptCommand.append("function getDomElementBy(by){\n");
		scriptCommand.append("  try{\n");
		scriptCommand.append("    var element;\n");
		scriptCommand.append("    if(by.id!=undefined){\n");
		scriptCommand.append("      element = document.getElementById(by.id);\n");
		scriptCommand.append("      if(element==undefined){\n");
		scriptCommand.append("        for (var key in document.all){\n");
		scriptCommand.append("          if(document.all[key].id == id){\n");
		scriptCommand.append("            element = document.all[key];\n");
		scriptCommand.append("            break;\n");
		scriptCommand.append("          }\n");
		scriptCommand.append("        }\n");
		scriptCommand.append("      }\n");
		scriptCommand.append("    }\n");
		scriptCommand.append("  }catch(error){\n");
		scriptCommand.append("    //alert(error);\n");
		scriptCommand.append("  }\n");
		scriptCommand.append("  return element;\n");
		scriptCommand.append("}\n");
		
		return scriptCommand.toString();
	}
	
	/**
	 * Click on an html element, this will ONLY fire an click event, the mouse point will not be<br>
	 * moved to the element and click on it.<br>
	 * <br><b>depending level: 0</b><br>
	 * 
	 * @param element (<b>Javascript</b>) Object, the javascript object to be clicked.
	 */		
	public static String fireMouseClick(){		
		StringBuffer scriptCommand = new StringBuffer();
		
		scriptCommand.append("function fireMouseClick(element){\n");
		scriptCommand.append("  try{\n");
		scriptCommand.append("    var e = document.createEvent('MouseEvents');\n");
		scriptCommand.append("    e.initMouseEvent('click', true, true, window, 1, 0, 0, 0, 0, false, false, false, false, 0, null);\n");
		scriptCommand.append("    element.dispatchEvent(e);\n");
		scriptCommand.append("  }catch(error){\n");
		scriptCommand.append("    //alert(error);\n");
		scriptCommand.append("  }\n");
		scriptCommand.append("}\n");
		
		return scriptCommand.toString();
	}

	/**
	 * Perform a Mouse Event on an html element. 
	 * The mouse pointer itself may not be moved, depending upon the event.<br>
	 * 
	 * <br><b>depending level: 0</b><br>
	 * 
	 * @param element (<b>Javascript</b>) Object, the javascript object (WebElement -- event.EVENT_TARGET) to be clicked.
	 * @param pview (<b>Javascript</b>) Object, the javascript object (event.EVENT_VIEW) that is the parent view or window of the target.
	 */		
	public static String fireMouseEvent(org.safs.selenium.util.MouseEvent event){		
		StringBuffer scriptCommand = new StringBuffer();
		String pview = "window";
		if(event.EVENT_VIEW instanceof Object) pview = "pview";
		scriptCommand.append("function fireMouseEvent(element, pview){\n");
		scriptCommand.append("  try{\n");
		scriptCommand.append("    var e = document.createEvent('MouseEvents');\n");
		scriptCommand.append("    e.initMouseEvent('"+ event.EVENT_TYPE +"', true, true, "+ pview +", "+ event.EVENT_DETAIL +", "+ 
		                                               event.EVENT_SCREENX  +", "+ event.EVENT_SCREENY +", "+
		                                               event.EVENT_CLIENTX  +", "+ event.EVENT_CLIENTY +", "+ 
		                                               event.EVENT_CTRLKEY  +", "+ event.EVENT_ALTKEY  +", "+ 
		                                               event.EVENT_SHIFTKEY +", "+ event.EVENT_METAKEY +", "+
		                                               event.EVENT_BUTTON   +", null);\n");
		scriptCommand.append("    element.dispatchEvent(e);\n");
		scriptCommand.append("  }catch(error){\n");
		scriptCommand.append("    //alert(error);\n");
		scriptCommand.append("  }\n");
		scriptCommand.append("}\n");
		
		return scriptCommand.toString();
	}

	public static String fireMouseHover(){		
		StringBuffer scriptCommand = new StringBuffer();
		
		scriptCommand.append("function fireMouseHover(element){\n");
		scriptCommand.append("  try{\n");
		scriptCommand.append("    var e = document.createEvent('MouseEvents');\n");
		scriptCommand.append("    e.initMouseEvent('mousehover', true, true, window, 1, 0, 0, 0, 0, false, false, false, false, 0, null);\n");
		scriptCommand.append("    element.dispatchEvent(e);\n");
		scriptCommand.append("  }catch(error){\n");
		scriptCommand.append("    //alert(error);\n");
		scriptCommand.append("  }\n");
		scriptCommand.append("}\n");
		
		return scriptCommand.toString();
	}
	
	public static String fireMouseClickById(boolean includeDependency){		
		StringBuffer scriptCommand = new StringBuffer();
		
		if(includeDependency){
			scriptCommand.append(fireMouseClick());
			scriptCommand.append(getDomElementBy());
		}
		
		scriptCommand.append("function fireMouseClickById(id){\n");
		scriptCommand.append("  try{\n");
		scriptCommand.append("    var by={'id':id}\n");
		scriptCommand.append("    var element = getDomElementBy(by);\n");
		scriptCommand.append("    if(element!=undefined){\n");
		scriptCommand.append("      fireMouseClick(element);\n");
		scriptCommand.append("    }\n");
		scriptCommand.append("  }catch(error){\n");
		scriptCommand.append("    //alert(error);\n");
		scriptCommand.append("  }\n");
		scriptCommand.append("}\n");
		
		return scriptCommand.toString();
	}
	
	/**
	 * Attach an event callback to an event for an object.<br>
	 * 
	 * <br><b>depending on: nothing</b><br>
	 * <br><b>depending level: 0</b><br>
	 * 
	 * @param domElement (<b>Javascript</b>) Object, the DOM object.
	 * @param event (<b>Javascript</b>) String, the event name, like 'click', 'blur' or 'mousedown' etc.
	 * @param callback (<b>Javascript</b>) Object, the 'event callback' function.
	 * @param useCapture (<b>Javascript</b>) boolean, If true, useCapture indicates that the user wishes to initiate capture.<br>
	 *                                                If not specified, useCapture defaults to false.<br>
	 * @return
	 */
	public static String addEventListener(){
		StringBuffer scriptCommand = new StringBuffer();
		
		scriptCommand.append("function addEventListener(domElement, event, callback, useCapture){\n");
		scriptCommand.append("  try{\n");
		scriptCommand.append("    if(domElement.addEventListener!=undefined){\n");
		scriptCommand.append("      //https://developer.mozilla.org/en-US/docs/Web/API/EventTarget.addEventListener\n");
		scriptCommand.append("      domElement.addEventListener(event, callback, useCapture);\n");
		scriptCommand.append("    }else{//For compablity of IE8 or below\n");
		scriptCommand.append("      domElement.attachEvent('on'+event, callback);\n");
		scriptCommand.append("    }\n");
		scriptCommand.append("  }catch(error){\n");
		scriptCommand.append("    //alert(error);\n");
		scriptCommand.append("  }\n");
		scriptCommand.append("}\n");
		
		return scriptCommand.toString();
	}
	
	/**
	 * Attach a generic event callback to a javasript event for an object.<br>
	 * 
	 * <br><b>depending on:</b><br>
	 * {@link #addEventListener()} <br>
	 * {@link #defineGenericEventCallBack(String)} <br>
	 * <br><b>depending level: 1</b><br>
	 * 
	 * @param includeDependency boolean, if true, will return the depended js as part of result.
	 * @param variable	String, the javascript global variable name, used by the 'generic event callback'.
	 * @param domNode (<b>Javascript</b>) Object (WebElement), used to find a SAP object.
	 * @param eventname (<b>Javascript</b>) String, the event name, like 'click', 'blur' or 'mousedown' etc.
	 * @param useCapture (<b>Javascript</b>) boolean, If true, useCapture indicates that the user wishes to initiate capture.<br>
	 *                                                If not specified, useCapture defaults to false.<br> 
	 * @return
	 */
	public static String addGenericEventListener(boolean includeDependency, String variable){
		StringBuffer scriptCommand = new StringBuffer();
		
		String callbackFunctionName = genGlobalFunctionName(variable);
		
		if(includeDependency){
			scriptCommand.append(addEventListener());
			scriptCommand.append(defineGenericEventCallBack(variable));
		}
		
		scriptCommand.append("function addGenericEventListener(domNode, eventname, useCapture){\n");
		scriptCommand.append("  if( domNode!=undefined ){\n");
		scriptCommand.append("    addEventListener(domNode, eventname, "+callbackFunctionName+", useCapture);\n");
		scriptCommand.append("  }\n");
		scriptCommand.append("}\n");
		
		return scriptCommand.toString();
		
	}
	
	/**
	 * Detach an event callback to an event from an object.<br>
	 * 
	 * <br><b>depending on: nothing</b><br>
	 * <br><b>depending level: 0</b><br>
	 * 
	 * @param domElement (<b>Javascript</b>) Object, the DOM object.
	 * @param event (<b>Javascript</b>) String, the event name, like 'click', 'blur' or 'mousedown' etc.
	 * @param callback (<b>Javascript</b>) Object, the 'event callback' function.
	 * @param useCapture (<b>Javascript</b>) boolean, Specifies whether the EventListener being removed was registered as a capturing listener or not.<br> 
	 *                                                If not specified, useCapture defaults to false.<br>
	 *                                                If must be the same as the value when calling addEventListener().<br>
	 * @return
	 */
	public static String removeEventListener(){
		StringBuffer scriptCommand = new StringBuffer();
		
		scriptCommand.append("function removeEventListener(domElement, event, callback, useCapture){\n");
		scriptCommand.append("  try{\n");
		scriptCommand.append("    if(domElement.removeEventListener!=undefined){\n");
		scriptCommand.append("      //https://developer.mozilla.org/en-US/docs/Web/API/EventTarget.removeEventListener\n");
		scriptCommand.append("      domElement.removeEventListener(event, callback, useCapture);\n");
		scriptCommand.append("    }else{//For compablity of IE8 or below\n");
		scriptCommand.append("      domElement.detachEvent('on'+event, callback);\n");
		scriptCommand.append("    }\n");
		scriptCommand.append("  }catch(error){\n");
		scriptCommand.append("    //alert(error);\n");
		scriptCommand.append("  }\n");
		scriptCommand.append("}\n");
		
		return scriptCommand.toString();
	}
	
	/**
	 * Detach a generic event callback to a javasript event from an object.<br>
	 * 
	 * <br><b>depending on:</b><br>
	 * {@link #removeEventListener()} <br>
	 * {@link #removeGlobalVariable(String)} <br>
	 * <br><b>depending level: 1</b><br>
	 * 
	 * @param includeDependency boolean, if true, will return the depended js as part of result.
	 * @param variable	String, the javascript global variable name, used by the 'generic event callback'.
	 * @param domNode (<b>Javascript</b>) Object (WebElement), used to find a SAP object.
	 * @param eventname (<b>Javascript</b>) String, the event name, like 'click', 'blur' or 'mousedown' etc.
	 * @param useCapture (<b>Javascript</b>) boolean, Specifies whether the EventListener being removed was registered as a capturing listener or not.<br>
	 *                                                If not specified, useCapture defaults to false.<br>
	 *                                                If must be the same as the value when calling addGenericEventListener().<br>
	 * @return
	 */
	public static String removeGenericEventListener(boolean includeDependency, String variable){
		StringBuffer scriptCommand = new StringBuffer();
		
		String callbackFunctionName = genGlobalFunctionName(variable);
		
		if(includeDependency){
			scriptCommand.append(removeEventListener());
			scriptCommand.append(removeGlobalVariable(variable));
		}
		
		scriptCommand.append("function removeGenericEventListener(domNode, eventname, useCapture){\n");
		scriptCommand.append("  if( domNode!=undefined ){\n");
		scriptCommand.append("    removeEventListener(domNode, eventname, "+callbackFunctionName+", useCapture);\n");
		scriptCommand.append("  }\n");
		scriptCommand.append("}\n");
		
		return scriptCommand.toString();
	}

	/**
	 * Get the browser's information, such like height, width, clientHeight, clientWidth etc.<br>
	 * <br><b>depending level: 0</b><br>
	 * 
	 * @param browserObject (<b>Javascript</b>) Object, the javascript object representing a browser object.<br>
	 * @return (<b>Javascript</b>) Object, the javascript object representing a browser object.<br>
	 *                             This browser object should contains properties as:<br>
	 *                             {@link BrowserWindow#PROPERTY_CLIENT_HEIGHT} <br>
	 *                             {@link BrowserWindow#PROPERTY_CLIENT_LOCATION_Y} <br>
	 *                             {@link BrowserWindow#PROPERTY_CLIENT_LOCATION_X} <br>
	 *                             {@link BrowserWindow#PROPERTY_CLIENT_WIDTH} <br>
	 *                             {@link BrowserWindow#PROPERTY_HEIGHT} <br>
	 *                             {@link BrowserWindow#PROPERTY_LOCATION_X} <br>
	 *                             {@link BrowserWindow#PROPERTY_LOCATION_Y} <br>
	 *                             {@link BrowserWindow#PROPERTY_WIDTH} <br>
	 *                             {@link BrowserWindow#PROPERTY_PAGE_X_OFFSET} <br>
	 *                             {@link BrowserWindow#PROPERTY_PAGE_Y_OFFSET} <br>
	 *                             {@link BrowserWindow#PROPERTY_BORDER_WIDTH} <br>
	 *                             {@link BrowserWindow#PROPERTY_HEADER_HEIGHT} <br>
	 *                             {@link BrowserWindow#PROPERTY_MAXIMIZED} <br>
	 */		
	public static String getBrowserInformation(){		
		StringBuffer scriptCommand = new StringBuffer();
		
		scriptCommand.append("function getBrowserInformation(browserObject){\n");
		scriptCommand.append("  try{\n");
		scriptCommand.append("    if(browserObject==undefined){\n");
		scriptCommand.append("      browserObject = {}\n");
		scriptCommand.append("    }\n");
		
		        			// x: screenX on IE, Chrome, Firefox seem to be consistent.
		scriptCommand.append("    try{\n");
		scriptCommand.append("      if(browserObject.x==undefined){\n");
		scriptCommand.append("        if(window.screenX!=undefined){\n");
		scriptCommand.append("          browserObject.x=window.screenX;\n");
		scriptCommand.append("        }\n");
		scriptCommand.append("      }\n");
		scriptCommand.append("    }catch(error){ }\n");

							// y: screenY on IE, Chrome, Firefox seem to be consistent.
		scriptCommand.append("    try{\n");
		scriptCommand.append("      if(browserObject.y==undefined){\n");
		scriptCommand.append("        if(window.screenY!=undefined){\n");
		scriptCommand.append("          browserObject.y=window.screenY;\n");
		scriptCommand.append("        }\n");
		scriptCommand.append("      }\n");
		scriptCommand.append("    }catch(error){ }\n");

							// width: outerWidth on IE, Chrome, Firefox seem to be consistent (UNTIL DOJO!).
		scriptCommand.append("    try{\n");
		scriptCommand.append("      if(browserObject.width==undefined){\n");
		scriptCommand.append("        if(window.outerWidth!=undefined){\n");
		scriptCommand.append("          browserObject.width=window.outerWidth;\n");
		scriptCommand.append("        }\n");
		scriptCommand.append("      }\n");
		scriptCommand.append("    }catch(error){ }\n");

							// height: outerHeight on IE, Chrome, Firefox seem to be consistent (UNTIL DOJO!).
		scriptCommand.append("    try{\n");
		scriptCommand.append("      if(browserObject.height==undefined){\n");
		scriptCommand.append("        if(window.outerHeight!=undefined){\n");
		scriptCommand.append("          browserObject.height=window.outerHeight;\n");
		scriptCommand.append("        }\n");
		scriptCommand.append("      }\n");
		scriptCommand.append("    }catch(error){ }\n");

							// maximized: If the browser is maximized
		scriptCommand.append("    try{\n");
		scriptCommand.append("      if(browserObject.maximized==undefined){\n");
		//scriptCommand.append("        //screenY==borderWidth means the window is maximized ? No. screenY=0, borderWidth=0 does NOT mean maximized! \n");
		//scriptCommand.append("        browserObject.maximized=(window.screenY==browserObject.borderWidth);\n");
		scriptCommand.append("        if(window.screen.availWidth!=undefined){\n");
		scriptCommand.append("          browserObject.maximized=(browserObject.width==window.screen.availWidth);\n");
		scriptCommand.append("          if(browserObject.maximized==true && window.screen.availHeight!=undefined){\n");
		scriptCommand.append("            browserObject.maximized=(browserObject.height==window.screen.availHeight);\n");
		scriptCommand.append("          }\n");
		scriptCommand.append("        }\n");
		scriptCommand.append("        if(browserObject.maximized==undefined){\n");
		scriptCommand.append("          browserObject.maximized=false;\n");
		scriptCommand.append("        }\n");
		scriptCommand.append("      }\n");
		scriptCommand.append("    }catch(error){ browserObject.maximized=false;}\n");
		
							// borderWidth: The width of the browser's vertical left/right border
		scriptCommand.append("    try{\n");
		scriptCommand.append("      if(browserObject.borderWidth==undefined){\n");
		scriptCommand.append("        if(window.innerWidth!=undefined){\n");
		scriptCommand.append("          browserObject.borderWidth=(window.outerWidth-window.innerWidth)/2;\n");
		scriptCommand.append("        }else if(window.screenLeft!=undefined && window.screenX!=undefined){\n");
		scriptCommand.append("          if(window.screenLeft!=window.screenX){\n");
		scriptCommand.append("            browserObject.borderWidth=window.screenLeft-window.screenX;\n");
		scriptCommand.append("          }\n");
		scriptCommand.append("        }\n");
		scriptCommand.append("        if(browserObject.borderWidth==undefined){\n");
		scriptCommand.append("          browserObject.borderWidth=6;\n"); //an average (IE/Firefox = 6, Chrome=5)
		scriptCommand.append("        }\n");
		scriptCommand.append("      }\n");
		scriptCommand.append("    }catch(error){ browserObject.borderWidth=6;}\n");
		
		// headerHeight: The height of header(menubar, tabbar, toolbar). BE CAREFUL: no status bar should be included!
		scriptCommand.append("    try{\n");
		scriptCommand.append("      if(browserObject.headerHeight==undefined){\n");
		scriptCommand.append("        if(window.mozInnerScreenY!=undefined){\n");//FF
		scriptCommand.append("          browserObject.headerHeight=window.mozInnerScreenY-window.screenY;\n");
		//scriptCommand.append("          if(window.menubar!=undefined && window.menubar.visible){\n");
		//scriptCommand.append("            //If the menubar is visible, need to substract the border width. but FF window.menubar.visible always give true!!!\n");
		//scriptCommand.append("            //With Selenium Webdriver, the menu is hidden, don't need to substract, just comment the following instruction. \n");
		//scriptCommand.append("            //browserObject.headerHeight-=browserObject.borderWidth;\n");
		//scriptCommand.append("          }\n");
		scriptCommand.append("        }else{\n");//  screenTop and screenLeft NOT on FF, Chrome screenTop and screenY same value!
		scriptCommand.append("          if(window.innerHeight!=undefined){\n");
		scriptCommand.append("            //for IE, do not forget to minus the height of 'status bar' if it exists!\n");
		scriptCommand.append("            //for Chrome, FireFox, 'statusbar' always exist, but it doesn't occupy any space in browser, do NOT need subtract!\n");
		scriptCommand.append("            browserObject.headerHeight=window.outerHeight-window.innerHeight-browserObject.borderWidth;\n");
		scriptCommand.append("            //window.statusbar doesn't work for IE to check existence of statusbar :-(\n");
		scriptCommand.append("            //window.statusbar does work for Chrome(FireFox) to check existence of statusbar, which doesn't need subtraction.\n");
		scriptCommand.append("            if(document.all || ('ActiveXObject' in window) || window.ActiveXObject){//only IE browser\n");
		scriptCommand.append("              if(window.statusbar){\n");
		scriptCommand.append("                debug('status bar is visible, minus status-bar-height (22) from headerHeight. ');\n");		
		scriptCommand.append("                browserObject.headerHeight -= 22;//hardcode as 22, not know how to get it\n");
		scriptCommand.append("              }\n");
		scriptCommand.append("            }\n");
		scriptCommand.append("          }else if(window.document.body.offsetHeight!=undefined){\n");
		scriptCommand.append("            browserObject.headerHeight=browserObject.height-window.document.body.offsetHeight-browserObject.borderWidth;\n"); //IE DOJO
		scriptCommand.append("          }else{\n");
		scriptCommand.append("                  \n");// what else would we be able to look for?
		scriptCommand.append("          }\n");
		scriptCommand.append("          if(browserObject.maximized){\n");
		scriptCommand.append("            browserObject.headerHeight-=browserObject.borderWidth;\n");
		scriptCommand.append("          }\n");
		scriptCommand.append("          //Maybe, we should subtract the height of 'statusbar' here.\n");
		scriptCommand.append("        }\n");
		scriptCommand.append("      }\n");
		scriptCommand.append("    }catch(error){ }\n");

		                    // clientX: browser's vertical border width ?
							//          screenX=topLeft clientX==screenLeft==screenX-border (NO screenLeft on Chrome!)
		scriptCommand.append("    try{\n");
		scriptCommand.append("      if(browserObject.clientX==undefined){\n");
		scriptCommand.append("        if(window.innerWidth!=undefined){\n");
		scriptCommand.append("          browserObject.clientX=(window.outerWidth-window.innerWidth)/2;\n");
		scriptCommand.append("        }else if(window.screenLeft!=undefined){\n");
		scriptCommand.append("          if(window.screenLeft > browserObject.x){\n");
		scriptCommand.append("            browserObject.clientX=window.screenLeft-browserObject.x;\n"); //IE DOJO
		scriptCommand.append("          }\n");
		scriptCommand.append("        }\n");
		scriptCommand.append("        if(browserObject.clientX==undefined){\n");
		scriptCommand.append("          browserObject.clientX=browserObject.borderWidth;\n");
		scriptCommand.append("        }\n");
		scriptCommand.append("      }\n");
		scriptCommand.append("    }catch(error){ }\n");
		
							// clientY: is headerHeight or outerHeight-innerHeight
		scriptCommand.append("    try{\n");
		scriptCommand.append("      if(browserObject.clientY==undefined){\n");
		scriptCommand.append("        if(browserObject.headerHeight){\n");
		scriptCommand.append("          browserObject.clientY=browserObject.headerHeight;\n");
		scriptCommand.append("        }else{\n");
		scriptCommand.append("          if(window.outerHeight!=undefined && window.innerHeight!=undefined){\n");
		scriptCommand.append("            browserObject.clientY=window.outerHeight-window.innerHeight;\n");
		scriptCommand.append("            if(window.mozInnerScreenY!=undefined){\n");//FF
		scriptCommand.append("              browserObject.clientY-=20;\n");// status bar height
		scriptCommand.append("            }else{\n");//IE
		scriptCommand.append("              browserObject.clientY-=browserObject.borderWidth;\n");
		scriptCommand.append("            }\n");
		scriptCommand.append("          }\n");
		scriptCommand.append("        }\n");// what if headerHeight AND outerHeight AND innerHeight are NOT present! (DOJO)
		scriptCommand.append("      }\n");
		scriptCommand.append("    }catch(error){ }\n");
		
							// clientWidth
		scriptCommand.append("    try{\n");
		scriptCommand.append("      if(browserObject.clientWidth==undefined){\n");
		scriptCommand.append("        if(window.innerWidth!=undefined){\n");
		scriptCommand.append("          browserObject.clientWidth=window.innerWidth;\n");
		scriptCommand.append("        }else if(window.document.body.offsetWidth!=undefined){\n");
		scriptCommand.append("          browserObject.clientWidth=window.document.body.offsetWidth;\n");
		scriptCommand.append("        }\n");
		scriptCommand.append("        if(browserObject.clientWidth==undefined){\n");
		scriptCommand.append("          browserObject.clientWidth=browserObject.width-(2*browserObject.borderWidth);\n");
		scriptCommand.append("        }\n");
		scriptCommand.append("      }\n");
		scriptCommand.append("    }catch(error){ }\n");
		
							// clientHeight
		scriptCommand.append("    try{\n");
		scriptCommand.append("      if(browserObject.clientHeight==undefined){\n");
		scriptCommand.append("        if(window.innerHeight!=undefined){\n");
		scriptCommand.append("          browserObject.clientHeight=window.innerHeight;\n");
		scriptCommand.append("        }else if(window.document.body.offsetHeight!=undefined){\n");
		scriptCommand.append("          browserObject.clientHeight=window.document.body.offsetHeight;\n");
		scriptCommand.append("        }\n");
		scriptCommand.append("        if(browserObject.clientHeight==undefined){\n");  // not sure about borderWidth below
		scriptCommand.append("          browserObject.clientHeight=browserObject.height-browserObject.headerHeight-browserObject.borderWidth;\n");
		scriptCommand.append("        }\n");
		scriptCommand.append("      }\n");
		scriptCommand.append("    }catch(error){ }\n");
		
							// pageXOffset: Scroll bar x offset
		scriptCommand.append("    try{\n");
		scriptCommand.append("      if(browserObject.pageXOffset==undefined){\n");
		scriptCommand.append("        if(window.pageXOffset!=undefined){\n");
		scriptCommand.append("          browserObject.pageXOffset=window.pageXOffset;\n");
		scriptCommand.append("        }else if(window.document.body.scrollLeft!=undefined){\n");
		scriptCommand.append("          browserObject.pageXOffset=window.document.body.scrollLeft;\n");
		scriptCommand.append("        }\n");
		scriptCommand.append("        if(browserObject.pageXOffset==undefined){\n");
		scriptCommand.append("          browserObject.pageXOffset=0;\n");
		scriptCommand.append("        }\n");
		scriptCommand.append("      }\n");
		scriptCommand.append("    }catch(error){ }\n");
		
							// pageYOffset: Scroll bar y offset
		scriptCommand.append("    try{\n");
		scriptCommand.append("      if(browserObject.pageYOffset==undefined){\n");
		scriptCommand.append("        if(window.pageYOffset!=undefined){\n");
		scriptCommand.append("          browserObject.pageYOffset=window.pageYOffset;\n");
		scriptCommand.append("        }else if(window.document.body.scrollTop!=undefined){\n");
		scriptCommand.append("          browserObject.pageYOffset=window.document.body.scrollTop;\n");
		scriptCommand.append("        }\n");
		scriptCommand.append("        if(browserObject.pageYOffset==undefined){\n");
		scriptCommand.append("          browserObject.pageYOffset=0;\n");
		scriptCommand.append("        }\n");
		scriptCommand.append("      }\n");
		scriptCommand.append("    }catch(error){ }\n");
		
		scriptCommand.append("    return browserObject;\n");
		scriptCommand.append("  }catch(error){\n");
		scriptCommand.append("    //alert(error);\n");
		scriptCommand.append("  }\n");
		scriptCommand.append("}\n");
		
		return scriptCommand.toString();
	}
	
	/**
	 * Get all attributes of a dom element.<br>
	 * 
	 * <br><b>depending level: 0</b><br>
	 * 
	 * @return Map, a set of pair (name, value)
	 */
	public static String getAttributes(){
		StringBuffer scriptCommand = new StringBuffer();
		
		scriptCommand.append("function getAttributes(domNode){\n");
		scriptCommand.append("  var attributesMap={}; \n");
		scriptCommand.append("  if( domNode!=undefined ){\n");
		scriptCommand.append("    var attrs = domNode.attributes;\n");
		scriptCommand.append("    var attr;\n");
		scriptCommand.append("    for(var i=0;i<attrs.length;i++){\n");
		scriptCommand.append("      attr=attrs[i];\n");
		scriptCommand.append("      attributesMap[attr.name]=attr.value;\n");
		scriptCommand.append("    }\n");
		scriptCommand.append("  }\n");
		scriptCommand.append("  return attributesMap;\n");
		scriptCommand.append("}\n");
		
		return scriptCommand.toString();
	}
	
	private static String appendPropertyMap(String prop, String ele, String map){
		return "    if('"+ prop +"' in "+ ele +"){\n" +
	           "      try{"+ map +"['"+ prop +"']="+ele+"."+ prop +";}\n" +
			   "      catch(ignore){}\n" +
	           "    }\n";
	}
	private static String appendPropertyFunctionMap(String prop, String ele, String map){
		return "    if(typeof "+ ele +"."+ prop +" === 'function'){\n" +
	           "       try{"+ map +"['"+ prop +"']="+ele+"."+ prop +"();}\n" +
			   "       catch(ignroe){}\n" +
	           "    }\n";
	}	
	/**
	 * Get standard Html properties of a dom element.<br>
	 * 
	 * <br><b>depending level: 0</b><br>
	 * 
	 * @return Map, a set of pair (name, value)
	 */
	public static String getHtmlProperties(){
		StringBuffer scriptCommand = new StringBuffer();
		
		scriptCommand.append("function getHtmlProperties(domNode){\n");
		scriptCommand.append("  var pMap={}; \n");
		scriptCommand.append("  if( domNode!=undefined ){\n");
		
		// standard HTML Element Properties
		scriptCommand.append( appendPropertyMap("accesskey",       "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("className",       "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("clientHeight",    "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("clientWidth",     "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("contentEditable", "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("dir",             "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("id",              "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("innerHTML",       "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("isContentEditable","domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("lang",            "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("namespaceURI",    "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("nodeName",        "domNode", "pMap"));
		//scriptCommand.append( appendPropertyMap("nodeType",        "domNode", "pMap")); // causes getHtmlProperties to fail
		//scriptCommand.append( appendPropertyMap("nodeValue",       "domNode", "pMap")); // likely an element
		scriptCommand.append( appendPropertyMap("offsetHeight",    "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("offsetLeft",      "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("offsetTop",       "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("offsetWidth",     "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("scrollHeight",    "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("scrollLeft",      "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("scrollTop",       "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("scrollWidth",     "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("style",           "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("tabIndex",        "domNode", "pMap"));
//		scriptCommand.append( appendPropertyMap("tagName",         "domNode", "pMap")); // causes getHtmlProperties to fail
		scriptCommand.append( appendPropertyMap("textContent",     "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("title",           "domNode", "pMap"));		
		scriptCommand.append( appendPropertyFunctionMap("hasAttributes",     "domNode", "pMap"));
		scriptCommand.append( appendPropertyFunctionMap("hasChildNodes",     "domNode", "pMap"));

        // TODO: standard HTML Anchor Properties
		scriptCommand.append( appendPropertyMap("href",           "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("name",           "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("password",       "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("rel",            "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("rev",            "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("target",         "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("text",           "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("type",           "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("username",       "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("crossOrigin",    "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("media",          "domNode", "pMap"));		

		// TODO: standard HTML Button Properties
		scriptCommand.append( appendPropertyMap("autofocus",      "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("disabled",       "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("defaultValue",   "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("value",          "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("formAction",     "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("formEncType",    "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("formMethod",     "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("formNoValidate", "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("formTarget",     "domNode", "pMap"));		
		
		// TODO: standard HTML CheckBox Properties
		scriptCommand.append( appendPropertyMap("checked",        "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("defaultChecked", "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("indeterminate",  "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("required",       "domNode", "pMap"));		
        
		// TODO: standard HTML ComboBox Properties
		scriptCommand.append( appendPropertyMap("length",         "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("multiple",       "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("selectedIndex",  "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("size",           "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("defaultSelected","domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("index",          "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("label",          "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("selected",       "domNode", "pMap"));		
		
		// TODO: standard HTML Div Properties
		scriptCommand.append( appendPropertyMap("align",          "domNode", "pMap"));
		
        // TODO: standard HTML EditBox Properties
		scriptCommand.append( appendPropertyMap("autocomplete",   "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("maxLength",      "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("pattern",        "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("placeholder",    "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("readOnly",       "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("cols",           "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("rows",           "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("wrap",           "domNode", "pMap"));		
        
		// TODO: standard HTML Image Properties
		scriptCommand.append( appendPropertyMap("alt",            "domNode", "pMap"));
		scriptCommand.append( appendPropertyMap("src",            "domNode", "pMap"));
		
        // TODO: standard HTML Menu Properties
		scriptCommand.append( appendPropertyMap("command",        "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("icon",           "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("radiogroup",     "domNode", "pMap"));				
		
        // TODO: standard HTML Number Properties
		scriptCommand.append( appendPropertyMap("max",            "domNode", "pMap"));				
		scriptCommand.append( appendPropertyMap("min",            "domNode", "pMap"));				
		scriptCommand.append( appendPropertyMap("step",           "domNode", "pMap"));				
		        
		// TODO: standard HTML Table Properties
		scriptCommand.append( appendPropertyMap("abbr",           "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("bgColor",        "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("background",     "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("border",         "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("cellIndex",      "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("cellPadding",    "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("cellSpacing",    "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("colSpan",        "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("nowrap",         "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("rowSpan",        "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("span",           "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("colIndex",       "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("rowIndex",       "domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("sectionColIndex","domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("sectionRowIndex","domNode", "pMap"));		
		scriptCommand.append( appendPropertyMap("vAlign",         "domNode", "pMap"));		
        		
		scriptCommand.append("  }\n");
		scriptCommand.append("  return pMap;\n");
		scriptCommand.append("}\n");
		
		return scriptCommand.toString();
	}
	
	/**
	 * Perform a HttpRequest by AJAX XMLHttpRequest.<br>
	 * <b>NOTE:</b> As this is performed by "AJAX XMLHttpRequest", it is normally <br>
	 * not permitted to request an URL of domain other than the Application under test.<br>
	 * This is the common cross-domain problem.<br>
	 * 
	 * <br><b>depending on:</b><br>
	 * {@link #initializeJSMap(StringBuffer, boolean, String, int, Map)}
	 * <br><b>depending level: 1</b><br>
	 * 
	 * @param requestHeaders Map<String, String>, pairs of (key,value) to set the headers for the request.
	 * 
	 * @return String, the javascript function sendHttpRequest(url, method, async, data, requestHeaders){}
	 */
	public static String sendHttpRequest(Map<String, String> requestHeaders){
		StringBuffer scriptCommand = new StringBuffer();
		
		scriptCommand.append("/**\n");
		scriptCommand.append("* Send a HTTP Request and get the response from the server.\n");
		scriptCommand.append("* Parameters:\n");
		scriptCommand.append("*   url String, the url to request\n");
		scriptCommand.append("*   method String, the method to execute, such as 'GET', 'POST', 'PUT' etc.\n");
		scriptCommand.append("*   async boolean, if the request will be executed aynchronously\n");
		scriptCommand.append("*   data String, the data to send with the request.\n");
		scriptCommand.append("*   requestHeaders Map, pairs of (key,value) to set the headers for the request.\n");
		scriptCommand.append("* Return:\n");
		scriptCommand.append("*   response Map<String,String> response from the server if the execution is synchronous, it contains key as: \n");
		scriptCommand.append("*            "+Key.READY_STATE.value()+": \t ready state, changes from 0 to 4.\n");
		scriptCommand.append("*            "+Key.RESPONSE_STATUS.value()+": \t http response status number\n");
		scriptCommand.append("*            "+Key.RESPONSE_STATUS_TEXT.value()+": \t http response status text\n");
		scriptCommand.append("*            "+Key.RESPONSE_HEADERS.value()+": \t http response headers\n");
		scriptCommand.append("*            "+Key.RESPONSE_TEXT.value()+": \t http response as string\n");
		scriptCommand.append("*            "+Key.RESPONSE_XML.value()+": \t http response as XML data\n");
		scriptCommand.append("*/\n");
		
		scriptCommand.append("function sendHttpRequest(url, method, async, data, requestHeaders){\n");
		//clear global variables for holding AJAX response, readyState, status, statusText etc.
		scriptCommand.append("  //clear global variables for holding AJAX response, readyState, status, statusText etc.\n");
		scriptCommand.append("  "+setGlobalVariable(XMLHttpRequest.VARIABLE_READY_STATE,"undefined"));
		scriptCommand.append("  "+setGlobalVariable(XMLHttpRequest.VARIABLE_STATUS,"undefined"));
		scriptCommand.append("  "+setGlobalVariable(XMLHttpRequest.VARIABLE_STATUS_TEXT,"undefined"));
		scriptCommand.append("  "+setGlobalVariable(XMLHttpRequest.VARIABLE_RESPONSE_TEXT,"undefined"));
		scriptCommand.append("  "+setGlobalVariable(XMLHttpRequest.VARIABLE_RESPONSE_XML,"undefined"));
		scriptCommand.append("  "+setGlobalVariable(XMLHttpRequest.VARIABLE_RESPONSE_HEADERS,"undefined"));
		scriptCommand.append("  \n");
		scriptCommand.append("  //output parameters to debug\n");
		scriptCommand.append("  debug('Executing function sendHttpRequest() with following parameters:');\n");
		scriptCommand.append("  debug('url='+url);\n");
		scriptCommand.append("  debug('method='+method);\n");
		scriptCommand.append("  debug('async='+async);\n");
		scriptCommand.append("  debug('data='+data);\n");
		scriptCommand.append("  debug('requestHeaders='+requestHeaders);\n");
		scriptCommand.append("  try{\n");
		scriptCommand.append("    var response = {};\n");
		scriptCommand.append("    var xhr;\n");
		scriptCommand.append("    if(window.XMLHttpRequest){\n");
		scriptCommand.append("      xhr=new XMLHttpRequest();\n");
		scriptCommand.append("    }else{\n");
		scriptCommand.append("      xhr=new ActiveXObject('Microsoft.XMLHTTP');\n");
		scriptCommand.append("    }\n");
		scriptCommand.append("    if(xhr==undefined){\n");
		scriptCommand.append("      throw new Error('can not initialize XMLHttpRequest object.');\n");
		scriptCommand.append("    }\n");
		scriptCommand.append("    //Add call back function for xhr, to get the response and set them to some global variables.\n");
		scriptCommand.append("    xhr.onreadystatechange = function(){\n");
		scriptCommand.append("      "+genGlobalVariableName(XMLHttpRequest.VARIABLE_READY_STATE)+"=xhr."+Key.READY_STATE.value()+";\n");
		scriptCommand.append("      if (xhr."+Key.READY_STATE.value()+" === "+AjaxReadyState.RESPONSE_READY.value()+"){\n");
		scriptCommand.append("        "+genGlobalVariableName(XMLHttpRequest.VARIABLE_STATUS)+"=xhr."+Key.RESPONSE_STATUS.value()+";\n");
		scriptCommand.append("        "+genGlobalVariableName(XMLHttpRequest.VARIABLE_STATUS_TEXT)+"=xhr."+Key.RESPONSE_STATUS_TEXT.value()+";\n");
		scriptCommand.append("        if (xhr."+Key.RESPONSE_STATUS.value()+" === "+HttpResponseStatus.OK.value()+"){\n");
		scriptCommand.append("          "+genGlobalVariableName(XMLHttpRequest.VARIABLE_RESPONSE_TEXT)+"=xhr."+Key.RESPONSE_TEXT.value()+";\n");
		scriptCommand.append("          "+genGlobalVariableName(XMLHttpRequest.VARIABLE_RESPONSE_XML)+"=xhr."+Key.RESPONSE_XML.value()+";\n");
		scriptCommand.append("          "+genGlobalVariableName(XMLHttpRequest.VARIABLE_RESPONSE_HEADERS)+"=xhr.getAllResponseHeaders();\n");
		scriptCommand.append("        }\n");
		scriptCommand.append("      }\n");
		scriptCommand.append("    };\n");
		scriptCommand.append("    \n");
		scriptCommand.append("    //Open connection\n");
		scriptCommand.append("    xhr.open(method, url, async);\n");
		scriptCommand.append("    \n");
		scriptCommand.append("    //Add http request headers\n");
		if(requestHeaders!=null && !requestHeaders.isEmpty()){
			String mapVariable = JavaScriptFunctions.initializeJSMap(scriptCommand, true, "  ", 2, requestHeaders);
			scriptCommand.append("    requestHeaders="+mapVariable+";\n");
		}
		scriptCommand.append("    if(requestHeaders!=undefined){\n");
		scriptCommand.append("      for(var key in requestHeaders){\n");
		scriptCommand.append("        xhr.setRequestHeader(key,requestHeaders[key]);\n");
		scriptCommand.append("      }\n");
		scriptCommand.append("    }\n");
		scriptCommand.append("    \n");
		scriptCommand.append("    //Perform a http request\n");
		scriptCommand.append("    if(data!=undefined) xhr.send(data);\n");
		scriptCommand.append("    else xhr.send();\n");
		scriptCommand.append("    \n");
		scriptCommand.append("    //get 'readyState' result from global variable, and set it to Map response.\n");
		scriptCommand.append("    response['"+Key.READY_STATE.value()+"']="+genGlobalVariableName(XMLHttpRequest.VARIABLE_READY_STATE)+";\n");
		scriptCommand.append("    \n");
		//Handle the response
		scriptCommand.append("    if(async){\n");
		scriptCommand.append("      //If execution is asynchronous, output readyState, status, statusText to debug, we can get responseText,responseXML and responseHeaders from global variable later.\n");
		scriptCommand.append("      debug('"+Key.READY_STATE.value()+"='+"+genGlobalVariableName(XMLHttpRequest.VARIABLE_READY_STATE)+");\n");
		scriptCommand.append("      debug('"+Key.RESPONSE_STATUS.value()+"='+"+genGlobalVariableName(XMLHttpRequest.VARIABLE_STATUS)+");\n");
		scriptCommand.append("      debug('"+Key.RESPONSE_STATUS_TEXT.value()+"='+"+genGlobalVariableName(XMLHttpRequest.VARIABLE_STATUS_TEXT)+");\n");
		scriptCommand.append("    }else{\n");
		scriptCommand.append("      //If execution is synchronous, get result from global variable.\n");
		scriptCommand.append("      response['"+Key.RESPONSE_STATUS.value()+"']="+genGlobalVariableName(XMLHttpRequest.VARIABLE_STATUS)+";\n");
		scriptCommand.append("      response['"+Key.RESPONSE_STATUS.value()+"']="+genGlobalVariableName(XMLHttpRequest.VARIABLE_STATUS_TEXT)+";\n");
		scriptCommand.append("      response['"+Key.RESPONSE_TEXT.value()+"']="+genGlobalVariableName(XMLHttpRequest.VARIABLE_RESPONSE_TEXT)+";\n");
		scriptCommand.append("      response['"+Key.RESPONSE_XML+"']="+genGlobalVariableName(XMLHttpRequest.VARIABLE_RESPONSE_XML)+";\n");
		scriptCommand.append("      response['"+Key.RESPONSE_HEADERS+"']="+genGlobalVariableName(XMLHttpRequest.VARIABLE_RESPONSE_HEADERS)+";\n");
		scriptCommand.append("    }\n");
		scriptCommand.append("    return response;\n");
		scriptCommand.append("  }catch(error){\n");
		scriptCommand.append("    throw error;\n");
		scriptCommand.append("  }\n");
		scriptCommand.append("}\n");
		
		return scriptCommand.toString();
	}
	
	public static final class GENERIC{
		/** OL or UL list items */
		public static String generic_getListItems(){
			StringBuffer scriptCommand = new StringBuffer();
			scriptCommand.append("function generic_getListItems(webElement){\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var object = webElement.getElementsByTagName('LI');\n");
			scriptCommand.append("    if(object==undefined){\n");
			scriptCommand.append("      throw new Error('can not find items in List.');\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("    return object;\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    throw error;\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");			
			return scriptCommand.toString();
		}
	}
	
	public static final class SAP{
		/**
		 * Get the value of a property for the sap object and return it.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param propertyName (<b>Java</b>) String, the property name.
		 * @param domelement (<b>Javascript</b>) Object, the dom-element used to find a SAP object.
		 * @param property (<b>Javascript</b>) String, the property name.
		 */
		public static String sap_getProperty(boolean includeDependency, String propertyName){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
			}
			
			scriptCommand.append("/**\n");
			scriptCommand.append(" * Get the value of a property for the sap object and return it.\n");
			scriptCommand.append(" * Parameters:\n");
			scriptCommand.append(" *   domelement Object, the dom-element used to find a SAP object.\n");
			scriptCommand.append(" *   property String, the property name.\n");
			scriptCommand.append(" * Retrun:\n");
			scriptCommand.append(" *   the value of the property.\n");
			scriptCommand.append(" */\n");
			
			scriptCommand.append("function sap_getProperty(domelement, property){\n");
			scriptCommand.append("  //get the SAP object according to dom object.\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  if(object==undefined){\n");
			scriptCommand.append("    throw new Error('did NOT get a valid SAP object.');\n");
			scriptCommand.append("  }\n");
			
			scriptCommand.append("  try{\n");
			scriptCommand.append("    //use the property name to form a method getXXX()\n");
			scriptCommand.append("    //if the property is value, we will call getValue() on this sap object\n");
			//Create the get method name
			String getMethod = "get"+WordUtils.capitalize(propertyName)+"()";
			scriptCommand.append("    var result = object."+getMethod+";\n");
			scriptCommand.append("    if(result==undefined){\n");
			scriptCommand.append("      debug('did NOT get value for property '+property+'.');\n");
			scriptCommand.append("    }else{\n");
			scriptCommand.append("      return result;\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    debug('Met JS error: '+error);\n");
			scriptCommand.append("  }\n");
			
			//Finally try the SAP low level API getProperty(), though it is not recommanded to use.
			scriptCommand.append("  try{\n");
			scriptCommand.append("    //getProperty is a low level method of sap.ui.base.ManagedObject, and it is not recommanded to use.\n");
			scriptCommand.append("    //see https://sapui5.hana.ondemand.com/docs/api/symbols/sap.ui.base.ManagedObject.html#lowlevelapi\n");
			scriptCommand.append("    var result = object.getProperty(property);\n");
			scriptCommand.append("    if(result==undefined){\n");
			scriptCommand.append("      throw new Error('did NOT get value for property '+property+'.');\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("    return result;\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    throw error;\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * According to a sap object, find the related DOM Object and return it.<br>
		 * 
		 * <br><b>depending on: existing DOM and SAP APIs only.</b><br>
		 * <br><b>depending level: 0</b><br>
		 * 
		 * @param sapElement (<b>Javascript</b>) Object, the SAP object.
		 */
		public static String sap_getDOMRef(){
			StringBuffer scriptCommand = new StringBuffer();
			
			scriptCommand.append("function sap_getDOMRef(sapElement){\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var object = sapElement.getDomRef();\n");
			scriptCommand.append("    if(object==undefined){\n");
			scriptCommand.append("      throw new Error('can not find the embedded DOM object.');\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("    return object;\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    throw error;\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * According to a dom object, find the related SAP Object and return it.<br>
		 * 
		 * <br><b>depending on: existing DOM and SAP APIs only.</b><br>
		 * <br><b>depending level: 0</b><br>
		 * 
		 * @param domElement (<b>Javascript</b>) Object (WebElement), the dom object of the SAP object.
		 */
		public static String sap_getObject(){
			StringBuffer scriptCommand = new StringBuffer();
			
			scriptCommand.append("function sap_getObject(domElement){\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var id = domElement.getAttribute('"+Component.ATTRIBUTE_ID+"');\n");
			scriptCommand.append("    var object = sap.ui.getCore().byId(id);\n");
			scriptCommand.append("    if(object==undefined){\n");
			scriptCommand.append("      //try jQuery to find by domElement, take the first object.\n");
			scriptCommand.append("      object=$(domElement).control()[0];\n");
			scriptCommand.append("      if(object==undefined){\n");
			scriptCommand.append("        //try jQuery to find by domElement, take the first object.\n");
			scriptCommand.append("        object=jQuery(domElement).control()[0];\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("    return object;\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * According to the id, find the SAP Object and return it.<br>
		 * 
		 * <br><b>depending on: existing DOM and SAP APIs only.</b><br>
		 * <br><b>depending level: 0</b><br>
		 * 
		 * @param id (<b>Javascript</b>) String, the html id of the SAP object.
		 */
		public static String sap_getObjectById(){
			StringBuffer scriptCommand = new StringBuffer();
			
			scriptCommand.append("function sap_getObjectById(id){\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var object = sap.ui.getCore().byId(id);\n");
			scriptCommand.append("    if(object!=undefined){\n");
			scriptCommand.append("      return object;\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Recurrsiv function to get class name from metadata of an UI element.<br>
		 * The second parameter 'buffer' will contain all the class names.<br>
		 * 
		 * <br><b>depending on: existing DOM and SAP APIs only.</b><br>
		 * <br><b>depending level: 0</b><br>
		 * 
		 * @param buffer (<b>Javascript</b>) Array, a javascript array to contain class names.
		 * @param metadata (<b>Javascript</b>) sap.ui.base.Metadata, the runtime metadata for an UI element.
		 */
		static String getSAPClassNames(){		
			StringBuffer scriptCommand = new StringBuffer();
			
			scriptCommand.append("function getSAPClassNames(buffer, metadata){\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if(buffer!=undefined && metadata!=undefined){\n");
			scriptCommand.append("      buffer.push(metadata.getName());\n");
			scriptCommand.append("      getSAPClassNames(buffer, metadata.getParent());\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}

		/**
		 * Test if object is an instance of certain classes.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #objectIsInstanceof()} <br>
		 * {@link #sap_getObject()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, dom-element used to find a javascript object.
		 * @param clazzes (<b>Javascript</b>) Array<JavaScriptClass>, the classes to compare with.
		 */	
		public static String sap_objectIsInstanceof(boolean includeDependency){		
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(objectIsInstanceof());
				scriptCommand.append(sap_getObject());
			}
			
			scriptCommand.append("function sap_objectIsInstanceof(domelement, clazzes){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  return objectIsInstanceof(object, clazzes);\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * According to the id, find the SAP Object and return its class name and super class names.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObjectById()} <br>
		 * {@link #getSAPClassNames()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * <br><b>depended by:</b><br>
		 * nothing.<br>
		 * 
		 * @param includeDependency boolean, if true the output string will contain the depending javascript functions.
		 * @param id (<b>Javascript</b>) String, the html id of the SAP object.
		 * @param classNames (<b>Javascript</b>) Array (out) , a javascript array to contain class names.
		 */
		public static String getSAPClassNamesById(boolean includeDependency){		
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObjectById());
				scriptCommand.append(getSAPClassNames());
			}
			
			scriptCommand.append("function getSAPClassNamesById(id, classNames){\n");
			scriptCommand.append("  var object = sap_getObjectById(id);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( object!=undefined && (object instanceof sap.ui.core.Element) ){\n");
			scriptCommand.append("      getSAPClassNames(classNames, object.getMetadata());\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * According to the id, find the SAP Object and return its class name<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObjectById()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * <br><b>depended by:</b><br>
		 * nothing.<br>
		 * 
		 * @param includeDependency boolean, if true the output string will contain the depending javascript functions.
		 * @param id (<b>Javascript</b>) String, the html id of the SAP object.
		 */		
		public static String getSAPClassNameById(boolean includeDependency){		
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObjectById());
			}
			
			scriptCommand.append("function getSAPClassNameById(id){\n");
			scriptCommand.append("  var object = sap_getObjectById(id);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( object!=undefined && (object instanceof sap.ui.core.Element) ){\n");
			scriptCommand.append("      return object.getMetadata().getName();\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}

		/**
		 * Select a combo box option indicated by key.<br>
		 * supported sap classes: {@link SapSelect_ComboBox#supportedClazzes}<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * {@link #objectIsInstanceof()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, Dom Object is used to find a SAP object.
		 * @param key (<b>Javascript</b>) String, the option's key to set.
		 * 
		 * @since
		 *  <br>   FEB 13, 2014    (Lei Wang) Call SAP combobox API fireChange() to invoke the associated callbacks.
		 */
		//support sap.ui.commons.ComboBox, sap.m.Select, sap.m.ComboBox
		public static String sap_ComboBox_setSelectedKey(boolean includeDependency){		
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
				scriptCommand.append(objectIsInstanceof());
			}
			
			scriptCommand.append("function sap_ComboBox_setSelectedKey(domelement, key){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			String supportedClazzes = initializeJSArray(scriptCommand, false, "  ", 2, SapSelect_ComboBox.supportedClazzes);
			scriptCommand.append("    if( object!=undefined && objectIsInstanceof(object,"+supportedClazzes+")) {\n");
			scriptCommand.append("      object.setSelectedKey(key);\n");
			scriptCommand.append("      //Need to fire the change event\n");
			scriptCommand.append("      var selectedKey = object.getSelectedKey();\n");
			scriptCommand.append("      var selectedItem;\n");
			scriptCommand.append("      var items = object.getItems();\n");
			scriptCommand.append("      for(var i=0; i<items.length; i++){\n");
			scriptCommand.append("        if(items[i].getKey()==selectedKey){\n");
			scriptCommand.append("          selectedItem = items[i];\n");
			scriptCommand.append("          break;\n");
			scriptCommand.append("        }\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("      if(selectedItem!=undefined){\n");
			scriptCommand.append("        var mArguments = {}\n");
			scriptCommand.append("        mArguments['newValue'] = selectedItem.getText();\n");
			scriptCommand.append("        mArguments['selectedItem'] = selectedItem;\n");

			//directly testing instance of sap.m.ComboBox will sometimes cause exception
			scriptCommand.append("        //if(object instanceof sap.m.ComboBox){\n");
			supportedClazzes = initializeJSArray(scriptCommand, false, "  ", 4, "sap.m.ComboBox");
			scriptCommand.append("        if(objectIsInstanceof(object, "+supportedClazzes+")){\n");
			scriptCommand.append("          object.fireSelectionChange(mArguments);\n");
			scriptCommand.append("        }else{\n");
			scriptCommand.append("          object.fireChange(mArguments);\n");
			scriptCommand.append("        }\n");

			scriptCommand.append("      }\n");
			scriptCommand.append("    }else{\n");
			scriptCommand.append("      if(object==undefined) throw new Error('sap component is undefined.');\n");
			scriptCommand.append("      else throw new Error(object.getMetadata().getName()+' is not supported');\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    throw error;\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Get a set of combo box options.<br>
		 * supported sap classes: {@link SapSelect_ComboBox#supportedClazzes}<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * {@link #parse_sap_ui_core_Item()} <br>
		 * {@link #objectIsInstanceof()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, Dom Object is used to find a SAP object.
		 */			
		//support sap.ui.commons.ComboBox, sap.m.Select, sap.m.ComboBox
		public static String sap_ComboBox_getItems(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
				scriptCommand.append(parse_sap_ui_core_Item());
				scriptCommand.append(objectIsInstanceof());
			}
			
			scriptCommand.append("function sap_ComboBox_getItems(domelement){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var properties = new Array();\n");
			String supportedClazzes = initializeJSArray(scriptCommand, false, "  ", 2, SapSelect_ComboBox.supportedClazzes);
			scriptCommand.append("    if( object!=undefined && objectIsInstanceof(object,"+supportedClazzes+")) {\n");
			scriptCommand.append("      items = object.getItems();//an array of sap.ui.core.Item\n");
			scriptCommand.append("      var selectedKey = object.getSelectedKey();\n");			
			scriptCommand.append("      if(items != undefined){\n");
			scriptCommand.append("        //items has properties: id, text, key and enabled\n");
			scriptCommand.append("        //if it contains more properties, we need to modify parse_sap_ui_core_Item() to get them.\n");
			scriptCommand.append("        if(items instanceof Array){\n");
			scriptCommand.append("          for(var i=0;i<items.length;i++){;\n");
			scriptCommand.append("            option = parse_sap_ui_core_Item(items[i], i, selectedKey);\n");
			scriptCommand.append("            properties.push(option);\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }else{\n");
			scriptCommand.append("          option = parse_sap_ui_core_Item(items, 0, selectedKey);\n");
			scriptCommand.append("          properties.push(option);\n");
			scriptCommand.append("        }\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("    }else{\n");
			scriptCommand.append("      if(object==undefined) throw new Error('sap component is undefined.');\n");
			scriptCommand.append("      else throw new Error(object.getMetadata().getName()+' is not supported');\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("    //alert(properties);\n");
			scriptCommand.append("    return properties;\n");

			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    throw error;\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Get selected index for a ListBox or a TabStrip, the index is 0-based.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, the dom-element used to find a SAP object.
		 * @return index (<b>Javascript</b>) int, the selected tab's index, 0-based.
		 */			
		public static String sap_ui_commons_xxx_getSelectedIndex(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
			}
			
			scriptCommand.append("function sap_ui_commons_xxx_getSelectedIndex(domelement){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( object!=undefined && (object instanceof sap.ui.commons.ListBox \n");
			scriptCommand.append("                              || object instanceof sap.ui.commons.TabStrip) ){\n");
			scriptCommand.append("      return object.getSelectedIndex();\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Get selected indices for a ListBox, the index is 0-based.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, the dom-element used to find a SAP object.
		 * @return index (<b>Javascript</b>) int[], the selected tab's indices, 0-based.
		 */			
		public static String sap_ui_commons_ListBox_getSelectedIndices(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
			}
			
			scriptCommand.append("function sap_ui_commons_ListBox_getSelectedIndices(domelement){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( object!=undefined && (object instanceof sap.ui.commons.ListBox) ){\n");
			scriptCommand.append("      return object.getSelectedIndices();\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Set selected index for a listbox and fire the 'select' event.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, the dom-element used to find a SAP object.
		 * @param index (<b>Javascript</b>) int, the index to select, 0-based.
		 */	
		public static String sap_ui_commons_ListBox_setSelectedIndex(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
			}
			
			scriptCommand.append("function sap_ui_commons_ListBox_setSelectedIndex(domelement, index){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( object!=undefined && (object instanceof sap.ui.commons.ListBox) ){\n");
			scriptCommand.append("      object.scrollToIndex(index);\n");
			scriptCommand.append("      //fireSelect() MUST be called after calling setSelectedIndex()\n");
			scriptCommand.append("      object.setSelectedIndex(index);\n");
			scriptCommand.append("      object.fireSelect({'selectedIndex':index});\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Scroll to the item indicated by index in the list box.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, the dom-element used to find a SAP object.
		 * @param index (<b>Javascript</b>) int, the index to scroll to, 0-based.
		 */	
		public static String sap_ui_commons_ListBox_scrollToIndex(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
			}
			
			scriptCommand.append("function sap_ui_commons_ListBox_scrollToIndex(domelement, index){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( object!=undefined && (object instanceof sap.ui.commons.ListBox) ){\n");
			scriptCommand.append("      object.scrollToIndex(index);\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Get a set of list box options for sap_m_List.<br>
		 * If some exception happens, this function will set the global errorcode and errormessage,<br>
		 * in the java code, user can detect the global errorcode and errormessage to do some necessary work.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * {@link #parse_sap_m_ListItemBase()} <br>
		 * {@link JavaScriptFunctions#throw_error()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, List Dom Object is used to find a SAP object.
		 */			
		public static String sap_m_List_getItems(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
				scriptCommand.append(parse_sap_m_ListItemBase());
			}
			
			scriptCommand.append("function sap_m_List_getItems(domelement){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var properties = new Array();\n");
			scriptCommand.append("    if( object!=undefined && object instanceof sap.m.ListBase){\n");
			scriptCommand.append("      var items = object.getItems();//an array of sap.m.ListItemBase\n");
			
			scriptCommand.append("      if(items != undefined){\n");
			scriptCommand.append("        //items has properties: id, selected\n");
			scriptCommand.append("        //if it contains more properties, we need to modify parse_sap_m_ListItemBase() to get them.\n");
			scriptCommand.append("        if(items instanceof Array){\n");
			scriptCommand.append("          for(var i=0;i<items.length;i++){;\n");
			scriptCommand.append("            option = parse_sap_m_ListItemBase(items[i], i);\n");
			scriptCommand.append("            properties.push(option);\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }else{\n");
			scriptCommand.append("          option = parse_sap_m_ListItemBase(items, 0);\n");
			scriptCommand.append("          properties.push(option);\n");
			scriptCommand.append("        }\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("    //alert(properties);\n");
			scriptCommand.append("    return properties;\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("    throw_error(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Get a set of list box options for sap_m_SelectList.<br>
		 * If some exception happens, this function will set the global errorcode and errormessage,<br>
		 * in the java code, user can detect the global errorcode and errormessage to do some necessary work.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * {@link #parse_sap_ui_core_Item()} <br>
		 * {@link JavaScriptFunctions#throw_error()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, List Dom Object is used to find a SAP object.
		 */			
		public static String sap_m_SelectList_getItems(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
				scriptCommand.append(parse_sap_ui_core_Item());
			}
			
			scriptCommand.append("function sap_m_SelectList_getItems(domelement){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var properties = new Array();\n");
			scriptCommand.append("    if( object!=undefined && (object instanceof sap.m.SelectList)){\n");
			scriptCommand.append("      var items = object.getItems();//an array of sap.ui.core.Item\n");
			scriptCommand.append("      var selectedKeys = new Array();\n");
			scriptCommand.append("      var selectedKey  = object.getSelectedKey();\n");
			scriptCommand.append("      if( selectedKey!=undefined){\n");			
			scriptCommand.append("            selectedKeys.push(selectedKey);\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("      if(items != undefined){\n");
			scriptCommand.append("        //items has properties: id, selected, index\n");
			scriptCommand.append("        //if it contains more properties, we need to modify parse_sap_ui_core_Item() to get them.\n");
			scriptCommand.append("        if(items instanceof Array){\n");
			scriptCommand.append("          for(var i=0;i<items.length;i++){;\n");
			scriptCommand.append("            option = parse_sap_ui_core_Item(items[i], i, selectedKeys);\n");
			scriptCommand.append("            properties.push(option);\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }else{\n");
			scriptCommand.append("          option = parse_sap_ui_core_Item(items, 0, selectedKeys);\n");
			scriptCommand.append("          properties.push(option);\n");
			scriptCommand.append("        }\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("    //alert(properties);\n");
			scriptCommand.append("    return properties;\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("    throw_error(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}

		/**
		 * Get selected items for a SelectList.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * {@link #parse_sap_ui.core.Item()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, the dom-element used to find a SAP object.
		 * @return items (<b>Javascript</b>) Items[], the selected items.
		 */			
		public static String sap_m_SelectList_getSelectedItems(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
				scriptCommand.append(parse_sap_ui_core_Item());
			}
			
			scriptCommand.append("function sap_m_SelectList_getSelectedItems(domelement){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var properties = new Array();\n");
			scriptCommand.append("    if( object!=undefined && (object instanceof sap.m.SelectList)){\n");
			scriptCommand.append("      var items = new Array();\n");
			scriptCommand.append("      var selectedItem = object.getSelectedItem();//a sap.ui.core.Item or null\n");
			scriptCommand.append("      if( selectedItem!=undefined && selectedItem instanceof sap.ui.core.Item){\n");			
			scriptCommand.append("            items.push(selectedItem);\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("      var selectedKeys = new Array();\n");
			scriptCommand.append("      var selectedKey  = object.getSelectedKey();\n");
			scriptCommand.append("      if( selectedKey!=undefined){\n");			
			scriptCommand.append("            selectedKeys.push(selectedKey);\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("      if(items != undefined){\n");
			scriptCommand.append("        //items has properties: id, selected, index..\n");
			scriptCommand.append("        //if it contains more properties, we need to modify parse_sap_ui_core_Item() to get them.\n");
			scriptCommand.append("        if(items instanceof Array){\n");
			scriptCommand.append("          for(var i=0;i<items.length;i++){;\n");
			scriptCommand.append("            option = parse_sap_ui_core_Item(items[i], i, selectedKeys);\n");
			scriptCommand.append("            properties.push(option);\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }else{\n");
			scriptCommand.append("          option = parse_sap_ui_core_Item(items, 0, selectedKeys);\n");
			scriptCommand.append("          properties.push(option);\n");
			scriptCommand.append("        }\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("    //alert(properties);\n");
			scriptCommand.append("    return properties;\n");
			
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Set selected item by id for a SelectList and fire the 'SelectionChange' event.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, the dom-element used to find a SAP object.
		 * @param id (<b>Javascript</b>) String, the id of the item to be selected.
		 */	
		public static String sap_m_SelectList_setSelectedItemById(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
			}
			
			scriptCommand.append("function sap_m_SelectList_setSelectedItemById(domelement, id){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( object!=undefined && (object instanceof sap.m.SelectList) ){\n");
			scriptCommand.append("      //fireSelectionChange() MUST be called after calling setSelectedItemId()\n");
			scriptCommand.append("      object.setSelectedItemId(id);\n");
			scriptCommand.append("      var item = object.getSelectedItem();//a sap.ui.core.Item or null\n");
			scriptCommand.append("      if(item != undefined){\n");
			scriptCommand.append("          object.fireSelectionChange({'selectedItem':item});\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("      //alert(items);\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}		
		
		
		/**
		 * Get selected items for a List.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * {@link #parse_sap_m_ListItemBase()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, the dom-element used to find a SAP object.
		 * @return items (<b>Javascript</b>) Items[], the selected items.
		 */			
		public static String sap_m_List_getSelectedItems(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
				scriptCommand.append(parse_sap_m_ListItemBase());
			}
			
			scriptCommand.append("function sap_m_List_getSelectedItems(domelement){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var properties = new Array();\n");
			scriptCommand.append("    if( object!=undefined && object instanceof sap.m.ListBase){\n");
			scriptCommand.append("      var items = object.getSelectedItems();//an array of sap.m.ListItemBase\n");
			
			scriptCommand.append("      if(items != undefined){\n");
			scriptCommand.append("        //items has properties: id, selected\n");
			scriptCommand.append("        //if it contains more properties, we need to modify parse_sap_m_ListItemBase() to get them.\n");
			scriptCommand.append("        if(items instanceof Array){\n");
			scriptCommand.append("          for(var i=0;i<items.length;i++){;\n");
			scriptCommand.append("            option = parse_sap_m_ListItemBase(items[i], i);\n");
			scriptCommand.append("            properties.push(option);\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }else{\n");
			scriptCommand.append("          option = parse_sap_m_ListItemBase(items, 0);\n");
			scriptCommand.append("          properties.push(option);\n");
			scriptCommand.append("        }\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("    //alert(properties);\n");
			scriptCommand.append("    return properties;\n");
			
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Set selected item by id for a listbox and fire the 'SelectionChange' event.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, the dom-element used to find a SAP object.
		 * @param id (<b>Javascript</b>) String, the id of the item to be selected.
		 */	
		public static String sap_m_List_setSelectedItemById(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
			}
			
			scriptCommand.append("function sap_m_List_setSelectedItemById(domelement, id){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( object!=undefined && (object instanceof sap.m.ListBase) ){\n");
			scriptCommand.append("      //fireSelectionChange() MUST be called after calling setSelectedItemById()\n");
			scriptCommand.append("      object.setSelectedItemById(id);\n");
			scriptCommand.append("      //will object.getSelectedItems() return the item recently set by id???\n");
			scriptCommand.append("      var items = object.getSelectedItems();//an array of sap.m.ListItemBase\n");
			scriptCommand.append("      if(items != undefined){\n");
			scriptCommand.append("        if(items instanceof Array){\n");
			scriptCommand.append("          object.fireSelectionChange({'listItems':items, 'selected':true});\n");
			scriptCommand.append("        }else{\n");
			scriptCommand.append("          object.fireSelectionChange({'listItem':items, 'selected':true});\n");
			scriptCommand.append("        }\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("      //alert(items);\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}		
		
		/**
		 * Get a set of list box options for sap_ui_commons_ListBox<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link JavaScriptFunctions#throw_error()} <br>
		 * {@link #sap_getObject()} <br>
		 * {@link #parse_sap_ui_core_Item()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, List Dom Object is used to find a SAP object.
		 */			
		public static String sap_ui_commons_ListBox_getItems(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
				scriptCommand.append(parse_sap_ui_core_Item());
			}
			
			scriptCommand.append("function sap_ui_commons_ListBox_getItems(domelement){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var properties = new Array();\n");
			scriptCommand.append("    if( object!=undefined && object instanceof sap.ui.commons.ListBox ){\n");
			scriptCommand.append("      var items = object.getItems();//an array of sap.ui.core.Item\n");
			scriptCommand.append("      var selectedKeys = object.getSelectedKeys();\n");

			scriptCommand.append("      if(items != undefined){\n");
			scriptCommand.append("        //items has properties: id, text, key and enabled\n");
			scriptCommand.append("        //if it contains more properties, we need to modify parse_sap_ui_core_Item() to get them.\n");
			scriptCommand.append("        if(items instanceof Array){\n");
			scriptCommand.append("          for(var i=0;i<items.length;i++){;\n");
			scriptCommand.append("            option = parse_sap_ui_core_Item(items[i], i, selectedKeys);\n");
			scriptCommand.append("            properties.push(option);\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }else{\n");
			scriptCommand.append("          option = parse_sap_ui_core_Item(items, 0, selectedKeys);\n");
			scriptCommand.append("          properties.push(option);\n");
			scriptCommand.append("        }\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("    //alert(properties);\n");
			scriptCommand.append("    return properties;\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("    throw_error(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Get a hierarchical structure representing a Menu.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * {@link #sap_ui_commons_Menu_getItems_Rec(boolean)} <br>
		 * <br><b>depending level: 2</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, MenuBar Dom Object is used to find a SAP object.
		 */					
		public static String sap_ui_commons_Menu_getItems(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
				scriptCommand.append(sap_ui_commons_Menu_getItems_Rec(includeDependency));
			}
			
			scriptCommand.append("function sap_ui_commons_Menu_getItems(domelement){\n");
			scriptCommand.append("  var menubar = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var menu = new Object();\n");
			scriptCommand.append("    menu."+Element.PROPERTY_ID+"=menubar.getId();\n");
			scriptCommand.append("    sap_ui_commons_Menu_getItems_Rec(menubar, menu);\n");
			scriptCommand.append("    return menu;\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    debug('sap_ui_commons_Menu_getItems(): '+error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Get a hierarchical structure representing a Menu.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #parse_sap_ui_commons_MenuItem()} <br>
		 * {@link #objectIsInstanceof()} <br>
		 * <br><b>depending level: 2</b><br>
		 * 
		 * @param menu (<b>Javascript</b>) Object, sap.ui.commons.MenuBar or sap.ui.commons.Menu or sap.ui.unified.Menu SAPUI5 Object.
		 * @param node (<b>Javascript</b>) Object, The hierarchical Object containing the menu-items.
		 */					
		private static String sap_ui_commons_Menu_getItems_Rec(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();

			if(includeDependency){
				scriptCommand.append(objectIsInstanceof());
				scriptCommand.append(parse_sap_ui_commons_MenuItem(false));
			}
			
			scriptCommand.append("function sap_ui_commons_Menu_getItems_Rec(menu, node){\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var children = new Array();\n");
			
			String supportedClazzes = initializeJSArray(scriptCommand, false, "  ", 2, SapSelectable_Menu.supportedClazzes);
			String menuClazzes = initializeJSArray(scriptCommand, false, "  ", 2, SapSelectable_Menu.menuClazzes);
			scriptCommand.append("    if(menu!=undefined && objectIsInstanceof(menu,"+supportedClazzes+")) {\n");
			scriptCommand.append("      var items = menu.getItems();//an array of sap.ui.commons.MenuItemBase, sap.ui.unified.MenuItemBase, or their subclass (MenuItem, MenuTextFieldItem) \n");			
			scriptCommand.append("      if(items != undefined){\n");
			scriptCommand.append("        //items has properties: id, enabled, visible. other properties depend on the type of MenuItem\n");
			scriptCommand.append("        //if it contains more properties, we need to modify parse_sap_ui_commons_MenuItem() to get them.\n");
			scriptCommand.append("        var submenu;\n");
			scriptCommand.append("        if(items instanceof Array){\n");
			scriptCommand.append("          for(var i=0;i<items.length;i++){\n");
			scriptCommand.append("            child = parse_sap_ui_commons_MenuItem(items[i]);\n");
			scriptCommand.append("            //if we create a child-parent tree, Selenium cannot return it to java side.\n");
			scriptCommand.append("            //child."+HierarchicalElement.PROPERTY_PARENT+"=node;\n");
			scriptCommand.append("            children.push(child);\n");
			scriptCommand.append("            submenu = items[i].getSubmenu();\n");
			scriptCommand.append("            if(submenu!=undefined && objectIsInstanceof(submenu,"+menuClazzes+")){\n");
			scriptCommand.append("              child."+MenuItem.PROPERTY_SUBMENUID+"=submenu.getId();\n");
			scriptCommand.append("              sap_ui_commons_Menu_getItems_Rec(submenu, child);\n");
			scriptCommand.append("            }\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }else{\n");
			scriptCommand.append("          child = parse_sap_ui_commons_MenuItem(items);\n");
			scriptCommand.append("          //if we create a child-parent tree, Selenium cannot return it to java side.\n");
			scriptCommand.append("          //child."+HierarchicalElement.PROPERTY_PARENT+"=node;\n");
			scriptCommand.append("          children.push(child);\n");
			scriptCommand.append("          submenu = items.getSubmenu();\n");
			scriptCommand.append("          if(submenu!=undefined && objectIsInstanceof(submenu,"+menuClazzes+")){\n");
			scriptCommand.append("            child."+MenuItem.PROPERTY_SUBMENUID+"=submenu.getId();\n");
			scriptCommand.append("            sap_ui_commons_Menu_getItems_Rec(submenu, child);\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }\n");
			scriptCommand.append("        node."+HierarchicalElement.PROPERTY_CHILDREN+"=children;\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("    }\n");
			
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    throw error;\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Convert a sap.ui.commons.MenuItemBase or sap.ui.unified.MenuItemBase object (or subclass) to 
		 * a standard MenuItem object with known properties like id, disabled, label, value and icon<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #objectIsInstanceof()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param itemobject (<b>Javascript</b>) Object, sap.ui.commons.MenuItemBase/sap.ui.unified.MenuItemBase object or subclass object.
		 * @return MenuItem (<b>Javascript</b>) Object, the uniformed MenuItem object.
		 */	
		private static String parse_sap_ui_commons_MenuItem(boolean include_objectIsInstanceof){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(include_objectIsInstanceof) scriptCommand.append(objectIsInstanceof());
			
			scriptCommand.append("function parse_sap_ui_commons_MenuItem(itemobject){\n");
			scriptCommand.append("  try{\n");
			String menuitemClazzes = initializeJSArray(scriptCommand, false, "  ", 2, SapSelectable_Menu.menuItemClazzes);
			String textMenuItemClazzes = initializeJSArray(scriptCommand, false, "  ", 2, SapSelectable_Menu.textMenuItemClazzes);
			scriptCommand.append("    if(itemobject!=undefined){\n");
			scriptCommand.append("      var item = new Object();\n");
			scriptCommand.append("      item."+MenuItem.PROPERTY_ID+"=itemobject.getId();\n");
			scriptCommand.append("      item."+MenuItem.PROPERTY_DISABLED+"=!itemobject.getEnabled();\n");
			//scriptCommand.append("      item."+MenuItem.PROPERTY_VISIBLE+"=!itemobject.getVisible();\n");

			scriptCommand.append("      if(objectIsInstanceof(itemobject,"+menuitemClazzes+")){\n");
			scriptCommand.append("        item."+MenuItem.PROPERTY_LABEL+"=itemobject.getText();\n");
			scriptCommand.append("        item."+MenuItem.PROPERTY_ICON+"=itemobject.getIcon();\n");
			scriptCommand.append("      }else if(objectIsInstanceof(itemobject,"+textMenuItemClazzes+")){\n");
			scriptCommand.append("        item."+MenuItem.PROPERTY_LABEL+"=itemobject.getLabel();\n");
			scriptCommand.append("        item."+MenuItem.PROPERTY_VALUE+"=itemobject.getValue();\n");
			scriptCommand.append("        item."+MenuItem.PROPERTY_ICON+"=itemobject.getIcon();\n");
			scriptCommand.append("      }\n");

			scriptCommand.append("      return item;\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		public static String sas_hc_ui_commons_pushmenu_PushMenu_goHome(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();

			if(includeDependency){
				scriptCommand.append(sap_getObject());
			}

			scriptCommand.append("function sas_hc_ui_commons_pushmenu_PushMenu_goHome(domelement){\n");
			scriptCommand.append("  var pushmenu = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    pushmenu.fireHomeClick({homeAppInfo: pushmenu.settings.homeAppInfo});\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    throw_error(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");

			return scriptCommand.toString();
		}
		
		/**
		 * Get a hierarchical structure representing a Menu.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * {@link #sas_hc_ui_commons_pushmenu_PushMenu_getItems_Rec(boolean)} <br>
		 * <br><b>depending level: 2</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, PushMenu DOM Object is used to find a SAP object.
		 */					
		public static String sas_hc_ui_commons_pushmenu_PushMenu_getItems(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
				scriptCommand.append(sas_hc_ui_commons_pushmenu_PushMenu_getItems_Rec(includeDependency));
			}
			
			scriptCommand.append("function sas_hc_ui_commons_pushmenu_PushMenu_getItems(domelement){\n");
			scriptCommand.append("  var menubar = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var menu = new Object();\n");
			scriptCommand.append("    menu."+Element.PROPERTY_ID+"=menubar.getId();\n");
			scriptCommand.append("    sas_hc_ui_commons_pushmenu_PushMenu_getItems_Rec(menubar, menu);\n");
			scriptCommand.append("    return menu;\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    throw_error(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Get a hierarchical structure representing a Menu.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #parse_sas_hc_ui_commons_pushmenu_PushMenuItemBase()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param menu (<b>Javascript</b>) SAPUI5 Object, sas.hc.ui.commons.pushmenu.PushMenu/PushMenuItemBase or its subclass.
		 * @param node (<b>Javascript</b>) Object, The hierarchical Object containing the menu-items of the 1th parameter menu.
		 */					
		private static String sas_hc_ui_commons_pushmenu_PushMenu_getItems_Rec(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(parse_sas_hc_ui_commons_pushmenu_PushMenuItemBase());
			}
			
			scriptCommand.append("//menu is the UI5 object, node is a hierarchical Object we defined; but they represent the same node in the menu.\n");
			scriptCommand.append("function sas_hc_ui_commons_pushmenu_PushMenu_getItems_Rec(menu, node){\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var children = new Array();\n");
			scriptCommand.append("    if( menu!=undefined && (menu instanceof sas.hc.ui.commons.pushmenu.PushMenu || menu instanceof sas.hc.ui.commons.pushmenu.PushMenuItemBase) ){\n");
			scriptCommand.append("      //PushMenu has method getItems(); but not all subcalss of PushMenuItemBase has this method, PushMenuGroup and PushMenuSuite have, while PushMenuItem has not.\n");			
			scriptCommand.append("      var items = menu.getItems? menu.getItems():undefined;//an array of sas.hc.ui.commons.pushmenu.PushMenuItemBase\n");			
			scriptCommand.append("      if(items != undefined){//there exist sub items\n");
			scriptCommand.append("        node."+MenuItem.PROPERTY_SUBMENUID+"=menu.getId();\n");
			scriptCommand.append("        var subitems = undefined;\n");
			scriptCommand.append("        if(items instanceof Array){\n");
			scriptCommand.append("          for(var i=0;i<items.length;i++){;\n");
			scriptCommand.append("            child = parse_sas_hc_ui_commons_pushmenu_PushMenuItemBase(items[i]);\n");
			scriptCommand.append("            children.push(child);\n");
			scriptCommand.append("            sas_hc_ui_commons_pushmenu_PushMenu_getItems_Rec(items[i], child);\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }else{\n");
			scriptCommand.append("          child = parse_sas_hc_ui_commons_pushmenu_PushMenuItemBase(items);\n");
			scriptCommand.append("          children.push(child);\n");
			scriptCommand.append("          sas_hc_ui_commons_pushmenu_PushMenu_getItems_Rec(items, child);\n");
			scriptCommand.append("        }\n");
			scriptCommand.append("        node."+HierarchicalElement.PROPERTY_CHILDREN+"=children;\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("    }\n");
			
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    throw error;\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Convert a sas.hc.ui.commons.pushmenu.PushMenuItemBase object (or subclass) to a standard MenuItem object 
		 * with known properties like id, disabled, label, value and icon<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * <br><b>depending level: 0</b><br>
		 * 
		 * @param itemobject (<b>Javascript</b>) Object, sas.hc.ui.commons.pushmenu.PushMenuItemBase object or subclass object.
		 * @return MenuItem (<b>Javascript</b>) Object, the uniformed MenuItem object.
		 */	
		private static String parse_sas_hc_ui_commons_pushmenu_PushMenuItemBase(){
			StringBuffer scriptCommand = new StringBuffer();
									
			scriptCommand.append("function parse_sas_hc_ui_commons_pushmenu_PushMenuItemBase(itemobject){\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    //itemobject is sas.hc.ui.commons.pushmenu.PushMenuItemBase (PushMenuItem, PushMenuGroup, PushMenuSuite) \n");			
			scriptCommand.append("    if(itemobject!=undefined){\n");
			scriptCommand.append("      var item = new Object();\n");
			scriptCommand.append("      item."+MenuItem.PROPERTY_ID+"=itemobject.getId();\n");
			//scriptCommand.append("      item."+MenuItem.PROPERTY_DISABLED+"=!itemobject.getEnabled();\n");
			//TODO how to know if the node is visible or not?
			scriptCommand.append("      //item."+MenuItem.PROPERTY_VISIBLE+"=!itemobject.getVisible();\n");
			scriptCommand.append("      //item."+MenuItem.PROPERTY_VISIBLE+"=($(\"#\"+itemobject.getId())[0].style.visibility==='visible');\n");
//			scriptCommand.append("      item."+MenuItem.PROPERTY_VISIBLE+"=(jQuery.sap.byId(itemobject.getId())[0].style.visibility==='visible');\n");
			scriptCommand.append("      debug('className='+jQuery.sap.byId(itemobject.getId())[0].className+' id='+itemobject.getId()+' label='+itemobject.getLabel());\n");
			
			scriptCommand.append("      if(itemobject instanceof sas.hc.ui.commons.pushmenu.PushMenuItemBase ){\n");
			scriptCommand.append("        item."+MenuItem.PROPERTY_LABEL+"=itemobject.getLabel();\n");
			scriptCommand.append("        item."+MenuItem.PROPERTY_VALUE+"=itemobject.getKey();\n");
			scriptCommand.append("        item."+MenuItem.PROPERTY_ICON+"=itemobject.getIcon();\n");
			//scriptCommand.append("        item.userobject=itemobject.getUserData();//object\n");
			//scriptCommand.append("        item.depth=itemobject.getDepth();//int\n");
			scriptCommand.append("      }else{\n");
			scriptCommand.append("        throw new Error(itemobject.getMetadata().getName()+' is not supported. ');\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("      return item;\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    throw error;\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Get all nodes content of a tree, the result is a tree hierarchy structure.<br>
		 * each tree node will contain 2 properties, children and parent. children is an<br>
		 * array, each child is a tree node; parent is the parent tree node.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * {@link #sap_ui_commons_Tree_getNodes_Rec(boolean)} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, Tree Dom Object is used to find a SAP object.
		 */			
		public static String sap_ui_commons_Tree_getNodes(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
				scriptCommand.append(sap_ui_commons_Tree_getNodes_Rec(includeDependency));
			}
			
			scriptCommand.append("function sap_ui_commons_Tree_getNodes(domelement){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var tree = new Object();\n");
			scriptCommand.append("    tree."+TreeNode.PROPERTY_ID+"=object.getId();\n");
			scriptCommand.append("    sap_ui_commons_Tree_getNodes_Rec(object, tree);\n");
			scriptCommand.append("    return tree;\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    console.log(error);\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Generate a tree hierarchy structure from a sap tree object. A sap tree object is already <br>
		 * a tree-hierarchy structure, but it contains too many information, and it is super-time-consuming <br>
		 * when return it to Java side by Selenium. So we will create a light-tree-structure to contain <br>
		 * only the necessary information thru this function.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #parse_sap_ui_commons_TreeNode(boolean)} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param nodeObject (<b>Javascript</b>) Object, SAP Tree/TreeNode Object.
		 * @param node (<b>Javascript</b>) Object, has structure of org.safs.selenium.webdriver.lib.model.TreeNode
		 *                                         and represents the first parameter nodeObject, it will contain
		 *                                         all children of nodeObject.
		 */			
		public static String sap_ui_commons_Tree_getNodes_Rec(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();

			if(includeDependency){
				scriptCommand.append(parse_sap_ui_commons_TreeNode());
			}
			
			scriptCommand.append("function sap_ui_commons_Tree_getNodes_Rec(nodeObject, node){\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var children = new Array();\n");
			scriptCommand.append("    if( nodeObject!=undefined && (nodeObject instanceof sap.ui.commons.Tree || nodeObject instanceof sap.ui.commons.TreeNode) ){\n");
			scriptCommand.append("      var nodes = nodeObject.getNodes();//an array of sap.ui.commons.TreeNode\n");			
			scriptCommand.append("      if(nodes != undefined){\n");
			scriptCommand.append("        //nodes has properties: id, text, isSelected, expended and selectable\n");
			scriptCommand.append("        //if it contains more properties, we need to modify parse_sap_ui_commons_TreeNode() to get them.\n");
			scriptCommand.append("        if(nodes instanceof Array){\n");
			scriptCommand.append("          for(var i=0;i<nodes.length;i++){;\n");
			scriptCommand.append("            child = parse_sap_ui_commons_TreeNode(nodes[i]);\n");
			scriptCommand.append("            //if we create a child-parent tree, Selenium cannot return it to java side.\n");
			scriptCommand.append("            //child."+HierarchicalElement.PROPERTY_PARENT+"=node;\n");
			scriptCommand.append("            children.push(child);\n");
			scriptCommand.append("            sap_ui_commons_Tree_getNodes_Rec(nodes[i], child);\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }else{\n");
			scriptCommand.append("          child = parse_sap_ui_commons_TreeNode(nodes);\n");
			scriptCommand.append("          //if we create a child-parent tree, Selenium cannot return it to java side.\n");
			scriptCommand.append("          //child."+HierarchicalElement.PROPERTY_PARENT+"=node;\n");
			scriptCommand.append("          children.push(child);\n");
			scriptCommand.append("          sap_ui_commons_Tree_getNodes_Rec(nodes, child);\n");
			scriptCommand.append("        }\n");
			scriptCommand.append("        node."+HierarchicalElement.PROPERTY_CHILDREN+"=children;\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("    }\n");
			
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    console.log(error);\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}

		/**
		 * Collapse all tree nodes.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObjectById()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param treeId (<b>Javascript</b>) String, SAP Tree's ID.
		 */
		public static String sap_ui_commons_Tree_collapseAll(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();

			if(includeDependency){
				scriptCommand.append(sap_getObjectById());
			}

			scriptCommand.append("function sap_ui_commons_Tree_collapseAll(treeId){\n");
			scriptCommand.append("  var tree = sap_getObjectById(treeId);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    tree.collapseAll();\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");

			return scriptCommand.toString();
		}
		/**
		 * Expand all tree nodes.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObjectById()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param treeId (<b>Javascript</b>) String, SAP Tree's ID.
		 */
		public static String sap_ui_commons_Tree_expandAll(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();

			if(includeDependency){
				scriptCommand.append(sap_getObjectById());
			}

			scriptCommand.append("function sap_ui_commons_Tree_expandAll(treeId){\n");
			scriptCommand.append("  var tree = sap_getObjectById(treeId);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    tree.expandAll();\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		/**
		 * Collapse a certain tree node.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObjectById()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param treeNodeId (<b>Javascript</b>) String, SAP Tree Node's ID.
		 * @param bCollapseChildren (<b>Javascript</b>) Boolean, if collapse the children of this node.
		 */
		public static String sap_ui_commons_TreeNode_collapse(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObjectById());
			}
			
			scriptCommand.append("function sap_ui_commons_TreeNode_collapse(treeNodeId, bCollapseChildren){\n");
			scriptCommand.append("  var treenode = sap_getObjectById(treeNodeId);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    treenode.collapse(bCollapseChildren);\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		/**
		 * Expand a certain tree node.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObjectById()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param treeNodeId (<b>Javascript</b>) String, SAP Tree Node's ID.
		 * @param bExpandChildren (<b>Javascript</b>) Boolean, if expand the children of this node.
		 */
		public static String sap_ui_commons_TreeNode_expand(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObjectById());
			}
			
			scriptCommand.append("function sap_ui_commons_TreeNode_expand(treeNodeId, bExpandChildren){\n");
			scriptCommand.append("  var treenode = sap_getObjectById(treeNodeId);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    treenode.expand(bExpandChildren);\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Select a certain tree node, show it on the page and fire the related 'Select' event.<br>
		 * <b>NOTE:</b> Remember to call sap_ui_commons_Tree_expandAll() firstly!!!<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObjectById()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param treeId (<b>Javascript</b>) String, SAP Tree's ID.
		 * @param treeNodeId (<b>Javascript</b>) String, SAP Tree Node's ID.
		 */
		public static String sap_ui_commons_TreeNode_select(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObjectById());
			}
			
			scriptCommand.append("function sap_ui_commons_TreeNode_select(treeId, treeNodeId){\n");
			scriptCommand.append("  var tree = sap_getObjectById(treeId);\n");
			scriptCommand.append("  var treenode = sap_getObjectById(treeNodeId);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    treenode.select();\n");
			scriptCommand.append("    treenode.fireSelected();\n");
			scriptCommand.append("    tree.fireSelect({'node': treenode});\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}

		/**
		 * Show a tree node on the page.<br>
		 * <b>NOTE:</b> Remember to call sap_ui_commons_Tree_expandAll() firstly!!!<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObjectById()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param treeNodeId (<b>Javascript</b>) String, SAP Tree Node's ID.
		 */
		public static String sap_ui_commons_TreeNode_showOnPage(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObjectById());
			}
			
			scriptCommand.append("function sap_ui_commons_TreeNode_showOnPage(treeNodeId){\n");
			scriptCommand.append("  var treenode = sap_getObjectById(treeNodeId);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    treenode.select();\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		/**
		 * Get a tree node's property 'isSelected'.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObjectById()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param treeNodeId (<b>Javascript</b>) String, SAP Tree Node's ID.
		 */
		public static String sap_ui_commons_TreeNode_getIsSelected(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();

			if(includeDependency){
				scriptCommand.append(sap_getObjectById());
			}

			scriptCommand.append("function sap_ui_commons_TreeNode_getIsSelected(treeNodeId){\n");
			scriptCommand.append("  var treenode = sap_getObjectById(treeNodeId);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    return treenode.getIsSelected();\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		/**
		 * Get a tree node's property 'expanded'.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObjectById()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param treeNodeId (<b>Javascript</b>) String, SAP Tree Node's ID.
		 */
		public static String sap_ui_commons_TreeNode_getExpanded(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObjectById());
			}
			
			scriptCommand.append("function sap_ui_commons_TreeNode_getExpanded(treeNodeId){\n");
			scriptCommand.append("  var treenode = sap_getObjectById(treeNodeId);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    return treenode.getExpanded();\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		/**
		 * Get a tree node's property 'selectable'.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObjectById()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param treeNodeId (<b>Javascript</b>) String, SAP Tree Node's ID.
		 */
		public static String sap_ui_commons_TreeNode_getSelectable(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObjectById());
			}
			
			scriptCommand.append("function sap_ui_commons_TreeNode_getSelectable(treeNodeId){\n");
			scriptCommand.append("  var treenode = sap_getObjectById(treeNodeId);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    return treenode.getSelectable();\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		/**
		 * Get a sap.ui.commons.TreeNode object and covert it to a org.safs.selenium.webdriver.lib.model.TreeNode object.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObjectById()} <br>
		 * {@link #parse_sap_ui_commons_TreeNode()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param treeNodeId (<b>Javascript</b>) String, SAP Tree Node's ID.
		 */
		public static String sap_ui_commons_TreeNode_refresh(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObjectById());
				scriptCommand.append(parse_sap_ui_commons_TreeNode());
			}
			
			scriptCommand.append("function sap_ui_commons_TreeNode_refresh(treeNodeId){\n");
			scriptCommand.append("  var treenode = sap_getObjectById(treeNodeId);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    return parse_sap_ui_commons_TreeNode(treenode);\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Convert a sap.ui.core.Item object to a standard Item object with known properties<br>
		 * like id, disabled, label, value, selected and index<br>
		 * 
		 * <br><b>depending on: nothing</b><br>
		 * <br><b>depending level: 0</b><br>
		 * 
		 * @param itemobject (<b>Javascript</b>) Object, sap.ui.core.Item object or its subclass.
		 * @param index (<b>Javascript</b>) int, the index of the item within a combobox/list.
		 * @param selectedKeyArray (<b>Javascript</b>) Array, an array of 'selected key'. To compare with item's key.
		 * @return Item (<b>Javascript</b>) Object, the uniformed Item object.
		 */	
		static String parse_sap_ui_core_Item(){
			StringBuffer scriptCommand = new StringBuffer();
				
			scriptCommand.append("function parse_sap_ui_core_Item(itemobject, index, selectedKeyArray){\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( itemobject!=undefined){\n");
			scriptCommand.append("      var item = new Object();\n");
			scriptCommand.append("      item."+Item.PROPERTY_ID+"=itemobject.getId();\n");
			scriptCommand.append("      item."+Item.PROPERTY_DISABLED+"=!itemobject.getEnabled();\n");
			scriptCommand.append("      item."+Item.PROPERTY_LABEL+"=itemobject.getText();\n");
			scriptCommand.append("      item."+Item.PROPERTY_VALUE+"=itemobject.getKey();\n");
			scriptCommand.append("      item."+Item.PROPERTY_SELECTED+"=false;\n");
			scriptCommand.append("      if(selectedKeyArray instanceof Array){\n");
			scriptCommand.append("        for(i=0;i<selectedKeyArray.length;i++){\n");
			scriptCommand.append("          if(itemobject.getKey()==selectedKeyArray[i]){\n");
			scriptCommand.append("            item."+Item.PROPERTY_SELECTED+"=true;\n");
			scriptCommand.append("            break;\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }\n");
			scriptCommand.append("      }else{\n");
			scriptCommand.append("        item."+Item.PROPERTY_SELECTED+"=(itemobject.getKey()==selectedKeyArray);\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("      item."+Item.PROPERTY_INDEX+"=index;\n");
			scriptCommand.append("      return item;\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Convert a sap.m.ListItemBase object to a standard Item object with known properties<br>
		 * like id, disabled, label, value, selected and index<br>
		 * 
		 * <br><b>depending on: nothing</b><br>
		 * <br><b>depending level: 0</b><br>
		 * 
		 * @param itemobject (<b>Javascript</b>) Object, sap.m.ListItemBase object or its subclass.
		 * @param index (<b>Javascript</b>) int, the index of the item within a list.
		 * @return Item (<b>Javascript</b>) Object, the uniformed Item object.
		 * @throws error (<b>Javascript</b>) Error, if some exception occurs
		 */	
		static String parse_sap_m_ListItemBase(){
			StringBuffer scriptCommand = new StringBuffer();
			
			scriptCommand.append(sap_getDOMRef());
				
			scriptCommand.append("function parse_sap_m_ListItemBase(itemobject, index){\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( itemobject!=undefined){\n");
			scriptCommand.append("      var item = new Object();\n");
			scriptCommand.append("      item."+Item.PROPERTY_ID+"=itemobject.getId();\n");
			scriptCommand.append("      item."+Item.PROPERTY_INDEX+"=index;\n");
			scriptCommand.append("      item."+Item.PROPERTY_SELECTED+"=itemobject.getSelected();\n");
			scriptCommand.append("      item."+Item.PROPERTY_VISIBLE+"=itemobject.getVisible();\n");
			scriptCommand.append("      try{\n");
			scriptCommand.append("        //try to test the attribute 'class' of embedded DOM object\n");
			scriptCommand.append("        var domobj=sap_getDOMRef(itemobject);\n");
			scriptCommand.append("        item."+Item.PROPERTY_CLASS+"=domobj.getAttribute('"+Item.PROPERTY_CLASS+"');\n");
			scriptCommand.append("      }catch(ignoreError){}\n");
			scriptCommand.append("      //For item's lable and value, analyze according to subclass name\n");
			scriptCommand.append("      //First, try the standard subclasses\n");
			scriptCommand.append("      if(itemobject instanceof sap.m.StandardListItem \n");
			scriptCommand.append("       ||itemobject instanceof sap.m.ObjectListItem \n");
			scriptCommand.append("       ||itemobject instanceof sap.m.GroupHeaderListItem \n");
			scriptCommand.append("       ){\n");
			scriptCommand.append("        item."+Item.PROPERTY_LABEL+"=itemobject.getTitle();\n");
			scriptCommand.append("        item."+Item.PROPERTY_VALUE+"=itemobject.getTitle();\n");
			scriptCommand.append("      }else if(itemobject instanceof sap.m.InputListItem){\n");
			scriptCommand.append("        item."+Item.PROPERTY_LABEL+"=itemobject.getLabel();\n");
			//TODO itemobject.getContent() will be sap.ui.core.Control, needs parsed to get value.
			scriptCommand.append("        //itemobject.getContent() will be sap.ui.core.Control, needs parsed to get value. \n");
			scriptCommand.append("        //item."+Item.PROPERTY_VALUE+"=itemobject.getContent();\n");
			scriptCommand.append("      }else if(itemobject instanceof sap.m.FacetFilterItem){\n");
			scriptCommand.append("        item."+Item.PROPERTY_LABEL+"=itemobject.getText();\n");
			scriptCommand.append("        item."+Item.PROPERTY_VALUE+"=itemobject.getKey();\n");
			scriptCommand.append("      }else if(itemobject instanceof sap.m.DisplayListItem){\n");
			scriptCommand.append("        item."+Item.PROPERTY_LABEL+"=itemobject.getLabel();\n");
			scriptCommand.append("        item."+Item.PROPERTY_VALUE+"=itemobject.getValue();\n");
			scriptCommand.append("      }else if(itemobject instanceof sap.m.FeedListItem \n");
			scriptCommand.append("       ||itemobject instanceof sap.m.ActionListItem ){\n");
			scriptCommand.append("        item."+Item.PROPERTY_LABEL+"=itemobject.getText();\n");
			scriptCommand.append("        item."+Item.PROPERTY_VALUE+"=itemobject.getText();\n");
			scriptCommand.append("      }else{\n");
			scriptCommand.append("        //for some classes from package sas.hc.m, sap.ushell, sap.suite, sap.ca,\n");
			scriptCommand.append("        //if these packages are not be loaded,\n");
			scriptCommand.append("        //test instanceof them will throw exception, catch it.\n");
			scriptCommand.append("        var labelSet=false;\n");
			scriptCommand.append("        try{\n");
			scriptCommand.append("          //get item value from attribute 'tooltip', a tempraire fix.\n");
			scriptCommand.append("          if(itemobject instanceof sas.hc.m.CustomListItem){\n");
			scriptCommand.append("            item."+Item.PROPERTY_LABEL+"=itemobject.getTooltip();\n");
			scriptCommand.append("            item."+Item.PROPERTY_VALUE+"=itemobject.getTooltip();\n");
			scriptCommand.append("            labelSet=true;\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }catch(ignoreError){}\n");
			scriptCommand.append("        try{\n");
			scriptCommand.append("          //get item value from attribute 'description', 'timestamp', a tempraire fix.\n");
			scriptCommand.append("          if(itemobject instanceof sas.hc.ui.commons.notifications.NotificationListItem){\n");
			scriptCommand.append("            item."+Item.PROPERTY_LABEL+"=itemobject.getDescription()+'\\n'+itemobject.getTimestamp();\n");
			scriptCommand.append("            item."+Item.PROPERTY_VALUE+"=itemobject.getDescription();\n");
			scriptCommand.append("            labelSet=true;\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }catch(ignoreError){}\n");
			scriptCommand.append("        try{\n");
			scriptCommand.append("          if(!labelSet &&\n");
			scriptCommand.append("            (itemobject instanceof sap.suite.ui.commons.FeedItemHeader \n");
			scriptCommand.append("            ||itemobject instanceof sap.ca.ui.charts.ClusterListItem)){\n");
			scriptCommand.append("            item."+Item.PROPERTY_LABEL+"=itemobject.getTitle();\n");
			scriptCommand.append("            item."+Item.PROPERTY_VALUE+"=itemobject.getTitle();\n");
			scriptCommand.append("            labelSet=true;\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }catch(ignoreError){}\n");
			scriptCommand.append("        try{\n");
			scriptCommand.append("          if(!labelSet &&\n");
			scriptCommand.append("            (itemobject instanceof sap.ca.ui.charts.BarListItem )){\n");
			scriptCommand.append("            item."+Item.PROPERTY_LABEL+"=itemobject.getValue();\n");
			scriptCommand.append("            item."+Item.PROPERTY_VALUE+"=itemobject.getValue();\n");
			scriptCommand.append("            labelSet=true;\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }catch(ignoreError){}\n");
			scriptCommand.append("        try{\n");
			scriptCommand.append("          //for sap.ushell.ui.launchpad.SearchSuggestionListItem and others\n");
			scriptCommand.append("          if(!labelSet || item."+Item.PROPERTY_LABEL+"===''){\n");
			scriptCommand.append("            item."+Item.PROPERTY_LABEL+"=itemobject.getText();\n");
			scriptCommand.append("            item."+Item.PROPERTY_VALUE+"=itemobject.getText();\n");
			scriptCommand.append("            labelSet=true;\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }catch(ignoreError){}\n");
			scriptCommand.append("        try{\n");
			scriptCommand.append("          //for sas.hc.m.OverflowListItem and others\n");
			scriptCommand.append("          if(!labelSet || item."+Item.PROPERTY_LABEL+"===''){\n");
			scriptCommand.append("            item."+Item.PROPERTY_LABEL+"=itemobject.getTitle();\n");
			scriptCommand.append("            item."+Item.PROPERTY_VALUE+"=itemobject.getTitle();\n");
			scriptCommand.append("            labelSet=true;\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }catch(ignoreError){}\n");
			scriptCommand.append("        if(!labelSet){\n");
			scriptCommand.append("          try{\n");
			scriptCommand.append("            debug('WARNING: cannot get label/value for item '+itemobject.getMetadata().getName()+' of id='+itemobject.getId());\n");
			scriptCommand.append("          }catch(ignoreError){}\n");
			scriptCommand.append("        }\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("      return item;\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("    throw error;\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		 
		/**
		 * Convert a sap.ui.commons.TreeNode object to a standard TreeNode object with known properties<br>
		 * like id, disabled, label, expanded, selected<br>
		 * 
		 * <br><b>depending on: nothing</b><br>
		 * <br><b>depending level: 0</b><br>
		 * 
		 * @param itemobject (<b>Javascript</b>) Object, sap.ui.commons.TreeNode object.
		 * @return TreeNode (<b>Javascript</b>) Object, the uniformed TreeNode object.
		 */	
		static String parse_sap_ui_commons_TreeNode(){
			StringBuffer scriptCommand = new StringBuffer();
				
			scriptCommand.append("function parse_sap_ui_commons_TreeNode(itemobject){\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( itemobject!=undefined){\n");
			scriptCommand.append("      var item = new Object();\n");
			scriptCommand.append("      item."+TreeNode.PROPERTY_ID+"=itemobject.getId();\n");
			scriptCommand.append("      item."+TreeNode.PROPERTY_DISABLED+"=!itemobject.getSelectable();\n");
			scriptCommand.append("      item."+TreeNode.PROPERTY_LABEL+"=itemobject.getText();\n");
			scriptCommand.append("      item."+TreeNode.PROPERTY_EXPANDED+"=itemobject.getExpanded();\n");
			scriptCommand.append("      item."+TreeNode.PROPERTY_SELECTED+"=itemobject.getIsSelected();\n");
			scriptCommand.append("      return item;\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Pageup/PageDown the scrollbar according to the pages.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, the dom-element used to find a SAP object.
		 * @param pages (<b>Javascript</b>) int, the pages to move for scrollbar.
		 *                                       positive --> pagedown; negative --> pageup
		 * 
		 */			
		public static String sap_ui_core_ScrollBar_page(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
			}
			
			scriptCommand.append("function sap_ui_core_ScrollBar_page(domelement, pages){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( object!=undefined ){\n");
			scriptCommand.append("      if( object instanceof sap.ui.core.ScrollBar ){\n");
			scriptCommand.append("        var position = object.getScrollPosition();\n");
			scriptCommand.append("        if(pages>0){\n");
			scriptCommand.append("          for(var i=0;i<pages;i++){\n");
			scriptCommand.append("            object.pageDown();\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }else if(pages<0){\n");
			scriptCommand.append("          for(var i=0;i<-pages;i++){\n");
			scriptCommand.append("            object.pageUp();\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }else{\n");
			scriptCommand.append("          return;\n");
			scriptCommand.append("        }\n");
			scriptCommand.append("        //if the 'ScrollPosition' doesn't change, maybe pageDown/pageUp didn't work\n");
			scriptCommand.append("        //then try the function setScrollPosition().\n");
			scriptCommand.append("        if(position==object.getScrollPosition()){\n");
			scriptCommand.append("          position += pages*"+ScrollBar.STEPS_OF_A_PAGE+"\n");
			scriptCommand.append("          object.setScrollPosition(position);\n");
			scriptCommand.append("        }\n");
			scriptCommand.append("      }else{\n");
			scriptCommand.append("        throw new Error(object.getMetadata().getName()+' is not supported');\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("    }else{\n");
			scriptCommand.append("      throw new Error('cannot find SAP object, it is null.');\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    throw error;\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Scroll the scrollbar according to the steps.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, the dom-element used to find a SAP object.
		 * @param steps (<b>Javascript</b>) int, the steps to move for scrollbar.
		 * 
		 */			
		public static String sap_ui_core_ScrollBar_scroll(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
			}
			
			scriptCommand.append("function sap_ui_core_ScrollBar_scroll(domelement, steps){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( object!=undefined ){\n");
			scriptCommand.append("      if( object instanceof sap.ui.core.ScrollBar ){\n");
			scriptCommand.append("        var position = object.getScrollPosition();\n");
			scriptCommand.append("        position += steps;\n");
			scriptCommand.append("        object.setScrollPosition(position);\n");
			scriptCommand.append("      }else{\n");
			scriptCommand.append("        throw new Error(object.getMetadata().getName()+' is not supported');\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("    }else{\n");
			scriptCommand.append("      throw new Error('cannot find SAP object, it is null.');\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    throw error;\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Set the value of property 'checked'.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * {@link JavaScriptFunctions#objectIsInstanceof()}
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, the dom-element used to find a SAP object.
		 * @param bChecked (<b>Javascript</b>) boolean, the value to set.
		 * 
		 * @since
		 *  <br>   FEB 13, 2014    (Lei Wang) Call SAP checkbox API fireChange() to invoke the associated callbacks.
		 *  <br>   SEP 11, 2014    (Lei Wang) Add support for sap.m.CheckBox
		 */			
		public static String sap_ui_commons_CheckBox_setChecked(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
				scriptCommand.append(objectIsInstanceof());
			}
			
			scriptCommand.append("function sap_ui_commons_CheckBox_setChecked(domelement, bChecked){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( object!=undefined){\n");
			String clazz = initializeJSArray(scriptCommand, false, "  ", 3, "sap.m.CheckBox");
			scriptCommand.append("      if(object instanceof sap.ui.commons.CheckBox){\n");
			scriptCommand.append("        object.setChecked(bChecked);\n");
			scriptCommand.append("        //Need to fire the change event\n");
			scriptCommand.append("        var mArguments = {}\n");
			scriptCommand.append("        mArguments['checked'] = object.checked;\n");
			scriptCommand.append("        object.fireChange(mArguments);\n");
			scriptCommand.append("      //}else if(object instanceof sap.m.CheckBox){\n");
			scriptCommand.append("      }else if(objectIsInstanceof(object,"+clazz+") ){\n");
			scriptCommand.append("        object.setSelected(bChecked);\n");
			scriptCommand.append("        //Need to fire the select event\n");
			scriptCommand.append("        var mArguments = {}\n");
			scriptCommand.append("        mArguments['selected'] = object.checked;\n");
			scriptCommand.append("        object.fireSelect(mArguments);\n");
			scriptCommand.append("      }else{\n");
			scriptCommand.append("        throw new Error(object.getMetadata().getName()+' is not supported');\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("    }else{\n");
			scriptCommand.append("      throw new Error('cannot find SAP object, it is null.');\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    throw error;\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Get the value of property 'checked'.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * {@link JavaScriptFunctions#objectIsInstanceof()}
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, the dom-element used to find a SAP object.
		 * @since
		 *  <br>   SEP 11, 2014    (Lei Wang) Add support for sap.m.CheckBox
		 */			
		public static String sap_ui_commons_CheckBox_getChecked(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
				scriptCommand.append(objectIsInstanceof());
			}
			
			scriptCommand.append("function sap_ui_commons_CheckBox_getChecked(domelement){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( object!=undefined ){\n");
			String clazz = initializeJSArray(scriptCommand, false, "  ", 3, "sap.m.CheckBox");
			scriptCommand.append("      if(object instanceof sap.ui.commons.CheckBox ){\n");
			scriptCommand.append("        return object.getChecked();\n");
			scriptCommand.append("      //}else if(object instanceof sap.m.CheckBox ){\n");
			scriptCommand.append("      }else if(objectIsInstanceof(object, "+clazz+") ){\n");
			scriptCommand.append("        return object.getSelected();\n");
			scriptCommand.append("      }else{\n");
			scriptCommand.append("        throw new Error(object.getMetadata().getName()+' is not supported');\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("    }else{\n");
			scriptCommand.append("      throw new Error('cannot find SAP object, it is null.');\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    throw error;\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Attach a generic event callback to a javasript event for an object.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * {@link JavaScriptFunctions#defineGenericEventCallBack(String)} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param includeDependency boolean, if true, will return the depended js as part of result.
		 * @param variable	String, the javascript global variable name, used by the 'generic event callback'.
		 * @param domNode (<b>Javascript</b>) Object (WebElement), used to find a SAP object.
		 * @param eventname (<b>Javascript</b>) String, the event name, like 'click', 'blur' or 'mousedown' etc.
		 * @return
		 */
		public static String sap_ui_core_Control_attachBrowserEvent(boolean includeDependency, String variable){
			StringBuffer scriptCommand = new StringBuffer();
			String callbackFunctionName = genGlobalFunctionName(variable);
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
				scriptCommand.append(defineGenericEventCallBack(variable));
			}
			
			scriptCommand.append("function sap_ui_core_Control_attachBrowserEvent(domNode, eventname){\n");
			
			scriptCommand.append("  var object = sap_getObject(domNode);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( object!=undefined && (object instanceof sap.ui.core.Control ) ){\n");
			scriptCommand.append("      object.attachBrowserEvent(eventname, "+callbackFunctionName+");\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Detach a generic event callback to a javasript event from an object.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * {@link JavaScriptFunctions#removeGlobalVariable(String)} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param includeDependency boolean, if true, will return the depended js as part of result.
		 * @param variable	String, the javascript global variable name, used by the 'generic event callback'.
		 * @param domNode (<b>Javascript</b>) Object (WebElement), used to find a SAP object.
		 * @param eventname (<b>Javascript</b>) String, the event name, like 'click', 'blur' or 'mousedown' etc.
		 * @return
		 */
		public static String sap_ui_core_Control_detachBrowserEvent(boolean includeDependency, String variable){
			StringBuffer scriptCommand = new StringBuffer();
			String callbackFunctionName = genGlobalFunctionName(variable);
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
				scriptCommand.append(removeGlobalVariable(variable));
			}
			
			scriptCommand.append("function sap_ui_core_Control_detachBrowserEvent(domNode, eventname){\n");
			
			scriptCommand.append("  var object = sap_getObject(domNode);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( object!=undefined && (object instanceof sap.ui.core.Control ) ){\n");
			scriptCommand.append("      object.detachBrowserEvent(eventname, "+callbackFunctionName+");\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Set selected index for a tabcontrol and fire the 'select' event.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, the dom-element used to find a SAP object.
		 * @param index (<b>Javascript</b>) int, the index to select, 0-based.
		 */			
		public static String sap_ui_commons_TabStrip_setSelectedIndex(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
			}
			
			scriptCommand.append("function sap_ui_commons_TabStrip_setSelectedIndex(domelement, index){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( object!=undefined && (object instanceof sap.ui.commons.TabStrip) ){\n");
			scriptCommand.append("      object.setSelectedIndex(index);\n");
			scriptCommand.append("      object.fireSelect({'index':index});\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}		
		
		/**
		 * Convert the sap.ui.commons.Tab object to a uniformed Tab object, which contains properties<br>
		 * like id, selected, label, disabled and index<br>
		 * 
		 * <br><b>depending on: nothing</b><br>
		 * <br><b>depending level: 0</b><br>
		 * 
		 * @param tabobject (<b>Javascript</b>) Object, the SAP sap.ui.commons.Tab object.
		 * @param index (<b>Javascript</b>) int, the index of the SAP tab object within the tabcontrol.
		 * @return Tab (<b>Javascript</b>) Object, the uniformed Tab object.
		 */
		static String parse_sap_ui_commons_Tab(){
			StringBuffer scriptCommand = new StringBuffer();
				
			scriptCommand.append("function parse_sap_ui_commons_Tab(tabobject, index){\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( tabobject!=undefined){\n");
			scriptCommand.append("      var tab = new Object();\n");
			scriptCommand.append("      tab."+Item.PROPERTY_LABEL+"=tabobject.getText();\n");
			scriptCommand.append("      tab."+Item.PROPERTY_SELECTED+"=tabobject.getSelected();\n");
			scriptCommand.append("      tab."+Item.PROPERTY_ID+"=tabobject.getId();\n");
			scriptCommand.append("      tab."+Item.PROPERTY_DISABLED+"=!tabobject.getEnabled();\n");
			scriptCommand.append("      tab."+Item.PROPERTY_INDEX+"=index;\n");
			scriptCommand.append("      return tab;\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Get selected index for a tabcontrol.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #sap_getObject()} <br>
		 * {@link #sap_initializeTabObject()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, the dom-element used to find a SAP object.
		 * @return tabs (<b>Javascript</b>) an array of map, the map containing the properties of a tab.
		 */			
		public static String sap_ui_commons_TabStrip_getTabs(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(sap_getObject());
				scriptCommand.append(parse_sap_ui_commons_Tab());
			}
			
			scriptCommand.append("function sap_ui_commons_TabStrip_getTabs(domelement){\n");
			scriptCommand.append("  var object = sap_getObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var properties = new Array();\n");
			scriptCommand.append("    if( object!=undefined && (object instanceof sap.ui.commons.TabStrip) ){\n");
			scriptCommand.append("      items = object.getTabs();//an array of sap.ui.commons.Tab\n");
			scriptCommand.append("      if(items != undefined){\n");
			scriptCommand.append("        //items has properties: id, text and selected\n");
			scriptCommand.append("        //if it contains more properties, we need to modify following code to get them.\n");
			scriptCommand.append("        if(items.length == undefined){\n");
			scriptCommand.append("          tab = parse_sap_ui_commons_Tab(items, 0);\n");
			scriptCommand.append("          properties.push(tab);\n");
			scriptCommand.append("        }else{\n");
			scriptCommand.append("          for(var i=0;i<items.length;i++){;\n");
			scriptCommand.append("            tab = parse_sap_ui_commons_Tab(items[i], i);\n");
			scriptCommand.append("            properties.push(tab);\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }\n");
			scriptCommand.append("        return properties;\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
	}

	
	/**
	 * Define javascript functions related to DOJO.<br>
	 * 
	 * History:<br>
	 * 
	 *  <br>   Jan 21, 2014    (Lei Wang) Initial release.
	 */
	public static final class DOJO{
		
		/** "dojo" */
		public static final String DOJO_KEY = "dojo";
		/** "dijit" */
		public static final String DIJIT_KEY = "dijit";
		/** "dojox" */
		public static final String DOJOX_KEY = "dojox";
		/** "window.dojo" */
		public static final String DOJO_DEFAULT = "window.dojo";
		/** "window.dijit" */
		public static final String DIJIT_DEFAULT = "window.dijit";
		/** "window.dojox" */
		public static final String DOJOX_DEFAULT = "window.dojox";
		/** "window.dojo" by default unless overridden by a djConfig/dojoConfig.scopemap. */
		public static String dojo = DOJO_DEFAULT;
		/** "window.dijit" by default unless overridden by a djConfig/dojoConfig.scopemap. */
		public static String dijit = DIJIT_DEFAULT;
		/** "window.dojox" by default unless overridden by a djConfig/dojoConfig.scopemap. */
		public static String dojox = DOJOX_DEFAULT;
		
		/**
		 * When executed, should return a Map object with keys "dojo", "dijit", and "dojox".<br>
		 * Dojo libraries not present in the browser will not have keys in the Map.<br>
		 * Values of these keys are usually "dojo", "dijit", and "dojox"; respectively.
		 * If Dojo is not running in the browser, the returned Map will be null (throws an Exception).<br>
		 * If there is a Dojo djConfig.scopeMap in the browser, the values returned will be 
		 * whatever the mapped values are. Example: "dojo"="mappedDojo", "dijit"="mappedDijit", etc..
		 * @return Map of dojo reference scopeMappings, or null/Exception if Dojo is not running in the browser.
		 */
		public static String getDojoScopemap(){
			StringBuffer scriptCommand = new StringBuffer();
			
			scriptCommand.append("function getDojoScopemap(){\n");
			scriptCommand.append("  var map = {}\n");
			scriptCommand.append("  var dc,v;\n");
			scriptCommand.append("  if(window.djConfig != undefined){\n");
			scriptCommand.append("    dc = window.djConfig;\n");
			scriptCommand.append("  }else if(window.dojoConfig != undefined){\n");
			scriptCommand.append("    dc = window.dojoConfig;\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("  if(dc.scopeMap != undefined){\n");
			scriptCommand.append("    for(i=0;i<dc.scopeMap.length;i++){\n");
			scriptCommand.append("      v = dc.scopeMap[i][0];\n");
			scriptCommand.append("      if(v == 'dojo') map.dojo = dc.scopeMap[i][1];\n");
			scriptCommand.append("      if(v == 'dijit') map.dijit = dc.scopeMap[i][1];\n");
			scriptCommand.append("      if(v == 'dojox') map.dojox = dc.scopeMap[i][1];\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("  if(map.dojo == undefined){\n");
			scriptCommand.append("    if(window.dojo != undefined) map.dojo = 'dojo';\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("  if(map.dijit == undefined){\n");
			scriptCommand.append("    if(window.dijit != undefined) map.dijit = 'dijit';\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("  if(map.dojox == undefined){\n");
			scriptCommand.append("    if(window.dojox != undefined) map.dojox = 'dojox';\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("  return map;\n");
			scriptCommand.append("}\n");
			return scriptCommand.toString();
		}
		
		/**
		 * According to the id, find the DOJO Object and return it.<br>
		 * 
		 * <br><b>depending on: existing DOM and DOJO APIs only.</b><br>
		 * <br><b>depending level: 0</b><br>
		 * 
		 * @param id (<b>Javascript</b>) String, the html id of the DOJO object.
		 */
		public static String getDojoObjectById(){
			StringBuffer scriptCommand = new StringBuffer();
			
			scriptCommand.append("function getDojoObjectById(id){\n");
			scriptCommand.append("  var dojoObject = "+ dijit +".registry.byId(id);\n");
			
			scriptCommand.append("  //If cannot find, use id to create a css selector to try.\n");
			scriptCommand.append("  if(dojoObject==undefined){\n");
			scriptCommand.append("    var cssselector =\"[id='\"+id+\"']\";\n");
			scriptCommand.append("    var domObjects = "+ dojo +".query(cssselector);\n");
			scriptCommand.append("    if(domObjects != undefined && domObjects[0]!=undefined){\n");
			scriptCommand.append("      dojoObject="+ dijit +".registry.byNode(domObjects[0]);\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }\n");
			
			scriptCommand.append("  if(dojoObject!=undefined){\n");
			scriptCommand.append("    return dojoObject;\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			return scriptCommand.toString();
		}

		/**
		 * According to the DOM node, find the DOJO Object and return it.<br>
		 * 
		 * <br><b>depending on: existing DOM and DOJO APIs only.</b><br>
		 * <br><b>depending level: 0</b><br>
		 * 
		 * @param domNode (<b>Javascript</b>) Object, the DOM Object of the DOJO object.
		 */
		public static String getDojoObjectByDomNode(){		
			StringBuffer scriptCommand = new StringBuffer();
			
			scriptCommand.append("function getDojoObjectByDomNode(domNode){\n");
			scriptCommand.append("  var dojoObject = "+ dijit +".registry.byNode(domNode);\n");
			scriptCommand.append("  if(dojoObject!=undefined){\n");
			scriptCommand.append("    return dojoObject;\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			return scriptCommand.toString();
		}

		/**
		 * According to the CSS Selector, find the DOM Objects and return them.<br>
		 * The CSS Selector can be something like table[@class='dijitReset dijitStretch dijitButtonContents'] or #id<br>
		 * <br>
		 * This javascript use dojo.query() to get DOM node, <br>
		 * please refer to link http://dojotoolkit.org/reference-guide/1.9/dojo/query.html<br>
		 * 
		 * <br><b>depending on: existing DOM and DOJO APIs only.</b><br>
		 * <br><b>depending level: 0</b><br>
		 * 
		 * @param cssselector (<b>Javascript</b>) String, the 'css selector' used to search the DOJO object.
		 * @param context (<b>Javascript</b>) String/Object, An optional context to limit the searching scope.<br>
		 * 						Only nodes under context will be scanned.<br>
		 * 						This can either be a string representing the node ID or a DOM node.<br>
		 */	
		public static String getDomNodesByCSSSelector(){
			StringBuffer scriptCommand = new StringBuffer();
			
			scriptCommand.append("function getDomNodesByCSSSelector(cssselector, context){\n");
			scriptCommand.append("  var selectNodes;\n");
			scriptCommand.append("  if(context != undefined){\n");
			scriptCommand.append("    selectNodes = "+ dojo +".query(cssselector, context);\n");
			scriptCommand.append("  }else{\n");
			scriptCommand.append("    selectNodes = "+ dojo +".query(cssselector);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("  if(selectNodes != undefined){\n");
			scriptCommand.append("     return selectNodes;\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			return scriptCommand.toString();
		}
		
		/**
		 * According to a dom object, find the related DOJO Object and return it.<br>
		 *
		 * <br><b>depending on:</b><br>
		 * {@link #getDojoObjectById()} <br>
		 * {@link #getDojoObjectByDomNode()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * <br><b>depended by:</b><br>
		 * nothing.<br>
		 * 
		 * @param includeDependency boolean, if true the output string will contain the depending javascript functions.
		 * @param domElement (<b>Javascript</b>) Object (WebElement), the dom object of the DOJO object.
		 */
		public static String getDojoObject(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();

			if(includeDependency){
				scriptCommand.append(getDojoObjectById());
				scriptCommand.append(getDojoObjectByDomNode());
			}
			
			scriptCommand.append("function getDojoObject(domElement){\n");
			scriptCommand.append("  var dojoObject=getDojoObjectByDomNode(domElement);\n");
			scriptCommand.append("  if(dojoObject==undefined){\n");
			scriptCommand.append("    var id = domElement.getAttribute('"+Component.ATTRIBUTE_ID+"');\n");
			scriptCommand.append("    if(id!=undefined){\n");
			scriptCommand.append("      dojoObject = getDojoObjectById(id);\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("  if(dojoObject!=undefined){\n");
			scriptCommand.append("    //alert(dojoObject);\n");
			scriptCommand.append("    return dojoObject;\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			return scriptCommand.toString();
		}
	
		/**
		 * Test if object is an instance of certain classes.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #objectIsInstanceof()} <br>
		 * {@link #getDojoObject(boolean)} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param domelement (<b>Javascript</b>) Object, dom-element used to find a javascript object.
		 * @param clazzes (<b>Javascript</b>) Array<JavaScriptClass>, the classes to compare with.
		 */	
		public static String dojo_objectIsInstanceof(boolean includeDependency){		
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(getDojoObject(includeDependency));
				scriptCommand.append(objectIsInstanceof());
			}
			
			scriptCommand.append("function dojo_objectIsInstanceof(domelement, clazzes){\n");
			scriptCommand.append("  var object = getDojoObject(domelement);\n");
			scriptCommand.append("  return objectIsInstanceof(object, clazzes);\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * According to the CSS Selector, find the DOJO Objects and return them.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #getDomNodesByCSSSelector()}<br>
		 * {@link #getDojoObjectByDomNode()}<br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * <br><b>depended by:</b><br>
		 * nothing.<br>
		 * 
		 * @param includeDependency boolean, if true the output string will contain the depending javascript functions.
		 * @param cssselector (<b>Javascript</b>) String, the 'css selector' used to search the DOJO object.
		 * @param context (<b>Javascript</b>) String/Object, An optional context to limit the searching scope.<br>
		 * 						Only nodes under context will be scanned.<br>
		 * 						This can either be a string representing the node ID or a DOM node.<br>
		 */
		public static String getDojoObjectsByCSSSelector(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(getDomNodesByCSSSelector());
				scriptCommand.append(getDojoObjectByDomNode());
			}
			
			scriptCommand.append("function getDojoObjectsByCSSSelector(cssselector, context){\n");
			scriptCommand.append("  var domNodes = getDomNodesByCSSSelector(cssselector, context);\n");
			scriptCommand.append("  var dojoNodes = new Array();\n");
			scriptCommand.append("  var dojoNode;\n");
			scriptCommand.append("  if(domNodes != undefined){\n");
			scriptCommand.append("    for(var i=0;i<domNodes.length;i++){\n");
			scriptCommand.append("      dojoNode = getDojoObjectByDomNode(domNodes[i]);\n");
			scriptCommand.append("      if(dojoNode!=undefined) dojoNodes.push(dojoNode);\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("  return dojoNodes;\n");
			scriptCommand.append("}\n");
			return scriptCommand.toString();
		}	
		
		/**
		 * According to the dom element, find the Dojo Object and return its class name (declaredClass).<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #getDojoObject()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * <br><b>depended by:</b><br>
		 * nothing.<br>
		 * 
		 * @param includeDependency boolean, if true the output string will contain the depending javascript functions.
		 * @param domelement (<b>Javascript</b>) Object, the dom element of the DOJO object.
		 */		
		public static String getDojoClassName(boolean includeDependency){		
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(getDojoObject(includeDependency));
			}
			
			scriptCommand.append("function getDojoClassName(domelement){\n");
			scriptCommand.append("  var dojoObject = getDojoObject(domelement);\n");
			scriptCommand.append("  if(dojoObject!=undefined){\n");
			scriptCommand.append("    return dojoObject.declaredClass;\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			return scriptCommand.toString();
		}
		
		/**
		 * According to the id, find the Dojo Object and return its class name (declaredClass).<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #getDojoObjectById()} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * <br><b>depended by:</b><br>
		 * nothing.<br>
		 * 
		 * @param includeDependency boolean, if true the output string will contain the depending javascript functions.
		 * @param id (<b>Javascript</b>) String, the html id of the DOJO object.
		 */
		public static String getDojoClassNameById(boolean includeDependency){		
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(getDojoObjectById());
			}
			
			scriptCommand.append("function getDojoClassNameById(id){\n");
			scriptCommand.append("  var dojoObject = getDojoObjectById(id);\n");
			scriptCommand.append("  if(dojoObject!=undefined){\n");
			scriptCommand.append("    return dojoObject.declaredClass;\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			return scriptCommand.toString();
		}
		
		/**
		 * According to the CSS Selector, find the DOJO Object and return its class name (declaredClass).<br>
		 * The CSS Selector can be something like table[@class='dijitReset dijitStretch dijitButtonContents']<br>
		 * <br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #getDojoObjectsByCSSSelector(boolean)}<br>
		 * <br><b>depending level: 2</b><br>
		 * 
		 * <br><b>depended by:</b><br>
		 * nothing.<br>
		 * 
		 * @param includeDependency boolean, if true the output string will contain the depending javascript functions.
		 * @param cssselector (<b>Javascript</b>) String, the 'css selector' used to search the DOJO object.
		 * @param context (<b>Javascript</b>) String/Object, An optional context to limit the searching scope.<br>
		 * 						Only nodes under context will be scanned.<br>
		 * 						This can either be a string representing the node ID or a DOM node.<br>
		 */	
		public static String getDojoClassNameByCSSSelector(boolean includeDependency){		
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(getDojoObjectsByCSSSelector(includeDependency));
			}
			
			scriptCommand.append("function getDojoClassNameByCSSSelector(cssselector, context){\n");
			scriptCommand.append("  var dojoObject=getDojoObjectsByCSSSelector(cssselector, context)[0];\n");
			scriptCommand.append("  if(dojoObject!=undefined) return dojoObject.declaredClass;\n");
			scriptCommand.append("}\n");
			return scriptCommand.toString();
		}
		
		/**
		 * get the value of an attribut of a dojo object.<br>
		 * <br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #getDojoObject(boolean)}<br>
		 * <br><b>depending level: 2</b><br>
		 * 
		 * @param includeDependency boolean, if true the output string will contain the depending javascript functions.
		 * @param domelement (<b>Javascript</b>) Object, the dom-element of the DOJO object.
		 * @param property (<b>Javascript</b>) String, the property name<br>
		 */	
		public static String dojo_get_property(boolean includeDependency){		
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(getDojoObject(includeDependency));
			}
			
			scriptCommand.append("function dojo_get_property(domelement, property){\n");
			scriptCommand.append("  var object;\n");
			scriptCommand.append("  object = getDojoObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    return object.get(property);\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			return scriptCommand.toString();
		}
		
		//================================  dojo_componentName_method     ===================================//
		/**
		 * According to the id or CSS Selector, find the dijit/_HasDropDown object, then call its<br>
		 * API closeDropDown(boolean) to close the associated drop-down menu.<br>
		 * The CSS Selector can be something like table[@class='dijitReset dijitStretch dijitButtonContents'] or #id<br>
		 * <br>
		 *
		 * <br><b>depending on:</b><br>
		 * {@link #getDojoObject(boolean)}<br>
		 * <br><b>depending level: 2</b><br>
		 * 
		 * <br><b>depended by:</b><br>
		 * nothing.<br>
		 * 
		 * @param includeDependency boolean, if true the output string will contain the depending javascript functions.
		 * @param domelement (<b>Javascript</b>) Object, the dom-element of the DOJO dijit._HasDropDown object.
		 * @param focusButton (<b>Javascript</b>) boolean, If true, refocuses the button widget
		 */
		public static String dojo_HasDropDown_closeDropDown(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(getDojoObject(includeDependency));
			}
			
			scriptCommand.append("function dojo_HasDropDown_closeDropDown(domelement, focusButton){\n");
			scriptCommand.append("  var dojoObject;\n");
			scriptCommand.append("  dojoObject = getDojoObject(domelement);\n");
			scriptCommand.append("  try{ dojoObject.closeDropDown(focusButton);\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			return scriptCommand.toString();
		}
		/**
		 * According to the dom-element, find the dijit/_HasDropDown object, then call its<br>
		 * API isLoaded() and return the boolean result.<br>
		 * The CSS Selector can be something like table[@class='dijitReset dijitStretch dijitButtonContents'] or #id<br>
		 * <br>
		 *
		 * <br><b>depending on:</b><br>
		 * {@link #getDojoObject(boolean)}<br>
		 * <br><b>depending level: 2</b><br>
		 * 
		 * <br><b>depended by:</b><br>
		 * nothing.<br>
		 * 
		 * @param includeDependency boolean, if true the output string will contain the depending javascript functions.
		 * @param domelement (<b>Javascript</b>) Object, the dom-element of the DOJO dijit._HasDropDown object.
		 * 
		 */	
		public static String dojo_HasDropDown_isLoaded(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(getDojoObject(includeDependency));
			}
			
			scriptCommand.append("function dojo_HasDropDown_isLoaded(domelement){\n");
			scriptCommand.append("  var dojoObject;\n");
			scriptCommand.append("  dojoObject = getDojoObject(domelement);\n");
			scriptCommand.append("  try{ return dojoObject.isLoaded();\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("  return false;\n");
			scriptCommand.append("}\n");
			return scriptCommand.toString();
		}
		
		/**
		 * According to the id or CSS Selector, find the dijit/_HasDropDown object, then call its<br>
		 * API loadAndOpenDropDown() to load the associated drop-down menu and open it.<br>
		 * The CSS Selector can be something like table[@class='dijitReset dijitStretch dijitButtonContents'] or #id<br>
		 * <br>
		 *
		 * <br><b>depending on:</b><br>
		 * {@link #getDojoObject(boolean)}<br>
		 * <br><b>depending level: 2</b><br>
		 * 
		 * <br><b>depended by:</b><br>
		 * nothing.<br>
		 * 
		 * @param includeDependency boolean, if true the output string will contain the depending javascript functions.
		 * @param domelement (<b>Javascript</b>) Object, the dom-element of the DOJO dijit._HasDropDown object.
		 */	
		public static String dojo_HasDropDown_loadAndOpenDropDown(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(getDojoObject(includeDependency));
			}
			
			scriptCommand.append("function dojo_HasDropDown_loadAndOpenDropDown(domelement){\n");
			scriptCommand.append("  var dojoObject;\n");
			scriptCommand.append("  dojoObject = getDojoObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    dojoObject.loadAndOpenDropDown();\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			return scriptCommand.toString();
		}

		/**
		 * According to the id or CSS Selector, find the dijit/_HasDropDown object, then call its<br>
		 * API openDropDown() to open the associated drop-down menu.<br>
		 * The CSS Selector can be something like table[@class='dijitReset dijitStretch dijitButtonContents'] or #id<br>
		 * <br>
		 *
		 * <br><b>depending on:</b><br>
		 * {@link #getDojoObject(boolean)}<br>
		 * <br><b>depending level: 2</b><br>
		 * 
		 * <br><b>depended by:</b><br>
		 * nothing.<br>
		 * 
		 * @param includeDependency boolean, if true the output string will contain the depending javascript functions.
		 * @param domelement (<b>Javascript</b>) Object, the dom-element of the DOJO dijit._HasDropDown object.
		 */		
		public static String dojo_HasDropDown_openDropDown(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(getDojoObject(includeDependency));
			}
			
			scriptCommand.append("function dojo_HasDropDown_openDropDown(domelement){\n");
			scriptCommand.append("  var dojoObject;\n");
			scriptCommand.append("  dojoObject = getDojoObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    dojoObject.openDropDown();\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			return scriptCommand.toString();
		}
		
		/**
		 * According to the id or CSS Selector, find the dijit/_FormSelectWidget object, then call its<br>
		 * API getOptions(valueOrIdx) to get the option/options.<br>
		 * <br>
		 *
		 * <br><b>depending on:</b><br>
		 * {@link #getDojoObject(boolean)}<br>
		 * {@link #parse_dijit_form_Select_Option()}<br>
		 * <br><b>depending level: 2</b><br>
		 * 
		 * <br><b>depended by:</b><br>
		 * nothing.<br>
		 * 
		 * @param includeDependency boolean, if true the output string will contain the depending javascript functions.
		 * @param domelement (<b>Javascript</b>) Object, the dom-element of the DOJO dijit._HasDropDown object.
		 * @param valueOrIdx (<b>Javascript</b>) anything, <br>If passed in as a string, that string is used to look up the option in the array of options - based on the value property. (See dijit/form/_FormSelectWidget.__SelectOption).<br>
		 *                                                 If passed in a number, then the option with the given index (0-based) within this select will be returned.<br>
		 *                                                 If passed in a dijit/form/_FormSelectWidget.__SelectOption, the same option will be returned if and only if it exists within this select.<br>
		 *                                                 If passed an array, then an array will be returned with each element in the array being looked up.<br>
		 *                                                 If not passed a value, then all options will be returned <br>
		 *                                                 
		 * @return (<b>Javascript</b>) any | object | undefined | null, The option corresponding with the given value or index. Or null.
		 */	
		public static String dojo_FormSelectWidget_getOptions(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(getDojoObject(includeDependency));
				scriptCommand.append(parse_dijit_form_Select_Option());
			}
			
			scriptCommand.append("function dojo_FormSelectWidget_getOptions(domelement, valueOrIdx){\n");
			scriptCommand.append("  var dojoObject;\n");
			scriptCommand.append("  dojoObject = getDojoObject(domelement);\n");
			scriptCommand.append("  //alert(dojoObject+' '+valueOrIdx);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var options;\n");
			scriptCommand.append("    if(valueOrIdx!=undefined){\n");
			scriptCommand.append("      options = dojoObject.getOptions(valueOrIdx);\n");
			scriptCommand.append("    }else{\n");
			scriptCommand.append("      options = dojoObject.getOptions();\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("    //alert(options);\n");
			scriptCommand.append("    //return options;\n");
			scriptCommand.append("    var properties = new Array();\n");
			scriptCommand.append("    if(options != undefined){\n");
			scriptCommand.append("      //options has 4 properties: disabled, label, selected and value\n");
			scriptCommand.append("      //if it contains more properties, we need to modify following code to get them.\n");
			scriptCommand.append("      if(options.length == undefined){\n");
			scriptCommand.append("        option = parse_dijit_form_Select_Option(options, 0);\n");
			scriptCommand.append("        properties.push(option);\n");
			scriptCommand.append("      }else{\n");
			scriptCommand.append("        for(var i=0;i<options.length;i++){;\n");
			scriptCommand.append("          option = parse_dijit_form_Select_Option(options[i], i);\n");
			scriptCommand.append("          properties.push(option);\n");
			scriptCommand.append("        }\n");
			scriptCommand.append("      }\n");
//			scriptCommand.append("      alert(properties);\n");
			scriptCommand.append("      return properties;\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			return scriptCommand.toString();
		}

		/**
		 * Convert the dijit.form.Select's option object to a uniformed Option object, which contains properties<br>
		 * like selected, label, disabled and value<br>
		 * 
		 * <br><b>depending on: nothing</b><br>
		 * <br><b>depending level: 0</b><br>
		 * 
		 * @param optionObject (<b>Javascript</b>) Object, the dijit.form.Select's option object.
		 * @param index (<b>Javascript</b>) int, the index of the DOJO option object within the Select object.
		 * @return Option (<b>Javascript</b>) Object, the uniformed Option object.
		 */
		static String parse_dijit_form_Select_Option(){
			StringBuffer scriptCommand = new StringBuffer();

			scriptCommand.append("function parse_dijit_form_Select_Option(optionObject, index){\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( optionObject!=undefined){\n");
			scriptCommand.append("      var option = new Object();\n");
			scriptCommand.append("      option."+Option.PROPERTY_LABEL+"=optionObject.label;\n");
			scriptCommand.append("      option."+Option.PROPERTY_SELECTED+"=optionObject.selected;\n");
			scriptCommand.append("      option."+Option.PROPERTY_DISABLED+"=optionObject.disabled;\n");
			scriptCommand.append("      option."+Option.PROPERTY_VALUE+"=optionObject.value;\n");
			scriptCommand.append("      option."+Option.PROPERTY_INDEX+"=index;\n");
			scriptCommand.append("      return option;\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * According to the id or CSS Selector, find the dijit/_WidgetBase object, then call its<br>
		 * API set(property, value) to set value to a property.<br>
		 * <br>
		 *
		 * <br><b>depending on:</b><br>
		 * {@link #getDojoObject(boolean)}<br>
		 * <br><b>depending level: 2</b><br>
		 * 
		 * <br><b>depended by:</b><br>
		 * nothing.<br>
		 * 
		 * @param includeDependency boolean, if true the output string will contain the depending javascript functions.
		 * @param domelement (<b>Javascript</b>) Object, the dom-element of the DOJO dijit._HasDropDown object.
		 * @param property (<b>Javascript</b>) String, the property name
		 * @param value (<b>Javascript</b>) anything, the value to set to the property
		 * 
		 */		
		public static String dojo_dijit_WidgetBase_set(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(getDojoObject(includeDependency));
			}
			
			scriptCommand.append("function dojo_dijit_WidgetBase_set(domelement, property, value){\n");
			scriptCommand.append("  var dojoObject;\n");
			scriptCommand.append("  dojoObject = getDojoObject(domelement);\n");
			scriptCommand.append("    //alert(dojoObject+' set '+property+'='+value);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    dojoObject.set(property, value);\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			return scriptCommand.toString();
		}
		
		/**
		 * According to the dom element, find a dojo object (for example dijit/form/FilteringSelect or dijit/form/ComboBox)<br>
		 * which contains a property 'store', and this property's value is an instance of dojo/store/api/Store or subclass, then call its<br>
		 * API query(query, options) to get a set of data from the store.<br>
		 * <Reference>Refer to http://dojotoolkit.org/documentation/tutorials/1.9/selects_using_stores/<br>
		 * <br>
		 *
		 * <br><b>depending on:</b><br>
		 * {@link #getDojoObject(boolean)}<br>
		 * {@link #parse_dijit_form_AutoCompleterMixin_Item(boolean)}<br>
		 * <br><b>depending level: 2</b><br>
		 * 
		 * <br><b>depended by:</b><br>
		 * nothing.<br>
		 * 
		 * @param includeDependency boolean, if true the output string will contain the depending javascript functions.
		 * @param domelement (<b>Javascript</b>) Object, the dom-element of of the DOJO dijit._HasDropDown object.
		 * @param query (<b>Javascript</b>) String|Object|Function, The query to use for retrieving objects from the store.
		 * @param options (<b>Javascript</b>) dojo/store/api/Store.QueryOptions, Optional, The optional arguments to apply to the resultset.
		 * 
		 * @return (<b>Javascript</b>) dojo/store/api/Store.QueryResults | undefined, The results of the query, extended with iterative methods.
		 */		
		public static String dojo_store_api_Store_query(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(getDojoObject(includeDependency));
				scriptCommand.append(parse_dijit_form_AutoCompleterMixin_Item(includeDependency));
			}
			
			scriptCommand.append("function dojo_store_api_Store_query(domelement, query, options){\n");
			scriptCommand.append("  var dojoObject;\n");
			scriptCommand.append("  dojoObject = getDojoObject(domelement);\n");
			scriptCommand.append("    //alert(dojoObject+' query with params: query='+query+'; options='+options);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    //alert(dojoObject.store);\n");
			scriptCommand.append("    if(dojoObject.store!=null && dojoObject.store!=undefined){\n");
			scriptCommand.append("      canQuery = false;\n");
			scriptCommand.append("      try{\n");
			scriptCommand.append("        canQuery = (dojoObject.store instanceof dojo.store.api.Store);\n");
			scriptCommand.append("      }catch(error){\n");
			scriptCommand.append("        //alert(error);\n");
			scriptCommand.append("        try{\n");
			scriptCommand.append("          canQuery = (dojoObject.store instanceof dojo.store.Memory);\n");
			scriptCommand.append("        }catch(error){\n");
			scriptCommand.append("          //alert(error);\n");
			scriptCommand.append("          try{\n");
			scriptCommand.append("            canQuery = (dojoObject.store instanceof dijit.form.DataList);\n");
			scriptCommand.append("          }catch(error){\n");
			scriptCommand.append("            //alert(error);\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("      //alert(canQuery);\n");
			scriptCommand.append("      if(canQuery){\n");
			scriptCommand.append("        options = dojoObject.store.query(query, options);\n");
			scriptCommand.append("        //alert(options);\n");
			scriptCommand.append("        //return options;\n");
			
			scriptCommand.append("        var properties = new Array();\n");
			scriptCommand.append("        if(options != undefined){\n");
			scriptCommand.append("          //options has 3 properties: id, name and value\n");
			scriptCommand.append("          //To make the result compatible, we need to set value for properties disabled, label, selected\n");
			scriptCommand.append("          //if it contains more properties, we need to modify following code to get them.\n");
			scriptCommand.append("          selectedItem = dojoObject.item;\n");
			scriptCommand.append("          if(options.length == undefined){\n");
			scriptCommand.append("            option = parse_dijit_form_AutoCompleterMixin_Item(options, 0, selectedItem);\n");
			scriptCommand.append("            properties.push(option);\n");
			scriptCommand.append("          }else{\n");
			scriptCommand.append("            for(var i=0;i<options.length;i++){;\n");
			scriptCommand.append("              option = parse_dijit_form_AutoCompleterMixin_Item(options[i], i, selectedItem);\n");
			scriptCommand.append("              properties.push(option);\n");
			scriptCommand.append("            }\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("          //alert(properties);\n");
			scriptCommand.append("          return properties;\n");
			scriptCommand.append("        }\n");
			
			scriptCommand.append("      }else{\n");
			scriptCommand.append("        //alert('Cannot query.');\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			return scriptCommand.toString();
		}
		
		/**
		 * Convert the dijit.form._AutoCompleterMixin's item object to a uniformed Option object, which contains properties<br>
		 * like id, selected, label, disabled and value<br>
		 * 
		 * <br><b>depending on: nothing</b><br>
		 * {@link #compareObject(List)}<br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param optionObject (<b>Javascript</b>) Object, the dijit.form._AutoCompleterMixin's item object.
		 * @param index (<b>Javascript</b>) int, the index of the DOJO item object within the Store object.
		 * @param selectedItemArray (<b>Javascript</b>) Array, an array of selected dijit.form._AutoCompleterMixin's item object.
		 * @return Option (<b>Javascript</b>) Object, the uniformed Option object.
		 */
		static String parse_dijit_form_AutoCompleterMixin_Item(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				//Prepare the javascript function compareObject(object1, object2)
				List<String> properties = new ArrayList<String>();
				properties.add(Option.PROPERTY_ID);
				properties.add(Option.PROPERTY_VALUE);
				properties.add(Option.PROPERTY_NAME);
				scriptCommand.append(compareObject(properties));
			}

			scriptCommand.append("function parse_dijit_form_AutoCompleterMixin_Item(itemObject, index, selectedItemArray){\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( itemObject!=undefined){\n");
			scriptCommand.append("      var item = new Object();\n");
			scriptCommand.append("      item."+Item.PROPERTY_DISABLED+"=false;\n");
			scriptCommand.append("      item."+Item.PROPERTY_LABEL+"=itemObject.name;\n");
			scriptCommand.append("      item."+Item.PROPERTY_ID+"=itemObject.id;\n");
			scriptCommand.append("      item."+Item.PROPERTY_VALUE+"=itemObject.value;\n");
			scriptCommand.append("      item."+Item.PROPERTY_INDEX+"=index;\n");
			
			scriptCommand.append("      item."+Item.PROPERTY_SELECTED+"=false;\n");
			scriptCommand.append("      if(selectedItemArray instanceof Array){\n");
			scriptCommand.append("        for(i=0;i<selectedItemArray.length;i++){\n");
			scriptCommand.append("          if(compareObject(itemObject, selectedItemArray[i])){\n");
			scriptCommand.append("            item."+Item.PROPERTY_SELECTED+"=true;\n");
			scriptCommand.append("            break;\n");
			scriptCommand.append("          }\n");
			scriptCommand.append("        }\n");
			scriptCommand.append("      }else{\n");
			scriptCommand.append("        item."+Item.PROPERTY_SELECTED+"=compareObject(itemObject, selectedItemArray);\n");
			scriptCommand.append("      }\n");
			
			scriptCommand.append("      return item;\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Attach a generic event callback to a javasript event for an object.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #getDojoObject(boolean)} <br>
		 * {@link JavaScriptFunctions#defineGenericEventCallBack(String)} <br>
		 * <br><b>depending level: 2</b><br>
		 * 
		 * @param includeDependency boolean, if true, will return the depended js as part of result.
		 * @param handleVariable	String, the javascript global variable name, used by the 'generic event callback'.
		 *                          It will also be used to generate the 'callback function name' and 'callback handle variable name'
		 * @param domelement (<b>Javascript</b>) Object, the dom-element of DOJO object.
		 * @param eventname (<b>Javascript</b>) String, the event name, like 'click', 'blur' or 'mousedown' etc.
		 */
		public static String dojo_dijit_WidgetBase_on(boolean includeDependency, String handleVariable){
			StringBuffer scriptCommand = new StringBuffer();
			String callbackFunctionName = genGlobalFunctionName(handleVariable);
			String callbackHandleName = genGlobalDojoHandleName(handleVariable);
			
			if(includeDependency){
				scriptCommand.append(getDojoObject(includeDependency));
				scriptCommand.append(defineGenericEventCallBack(handleVariable));
			}
			
			scriptCommand.append("function dojo_dijit_WidgetBase_on(domelement, eventname){\n");
			
			scriptCommand.append("  var object = getDojoObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( object!=undefined && (object instanceof dijit._WidgetBase ) ){\n");
			scriptCommand.append("      "+callbackHandleName+" = object.own(object.on(eventname, "+callbackFunctionName+"))[0];\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Detach a generic event callback to a javasript event from an object.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link JavaScriptFunctions#removeGlobalVariable(String)} <br>
		 * <br><b>depending level: 1</b><br>
		 * 
		 * @param includeDependency boolean, if true, will return the depended js as part of result.
		 * @param variable	String, the javascript global variable name, used to store the event-listener handle.
		 */
		public static String dojo_handle_remove(boolean includeDependency, String handleVariable){
			StringBuffer scriptCommand = new StringBuffer();
			String callbackHandleName = genGlobalDojoHandleName(handleVariable);
			
			if(includeDependency){
				scriptCommand.append(removeGlobalVariable(handleVariable));
			}
			
			scriptCommand.append("/*Remove the latest callback-handle, store as window.xxx_dojo_handle*/\n");
			scriptCommand.append("function dojo_handle_remove(){\n");
			
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( "+callbackHandleName+"!=undefined ){\n");
			scriptCommand.append("      "+callbackHandleName+".remove();\n");
			scriptCommand.append("      "+callbackHandleName+"=undefined;\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}

		/**
		 * Get direct children of the WidgetBase widget.<br>
		 * Should not be called by Selenium, it will cost long time to return dojo objects.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #getDojoObject(boolean)} <br>
		 * <br><b>depending level: 2</b><br>
		 * 
		 * @param includeDependency boolean, if true, will return the depended js as part of result.
		 * @param domelement (<b>Javascript</b>) Object, the dom-element of DOJO object.
		 * @return childrenArray (<b>Javascript</b>) List<Tab>, an array of the widget's children
		 */
		static String dojo_dijit_WidgetBase_getChildren(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(getDojoObject(includeDependency));
			}
			
			scriptCommand.append("function dojo_dijit_WidgetBase_getChildren(domelement){\n");
			scriptCommand.append("  var object = getDojoObject(domelement);\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( object!=undefined && (object instanceof dijit._WidgetBase ) && object.hasChildren()){\n");
			scriptCommand.append("      return object.getChildren();//an array of tab's child\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Convert the dojo tab object to a uniformed Tab object, which contains properties<br>
		 * like id, selected, disabled, label and index<br>
		 * 
		 * <br><b>depending on: nothing</b><br>
		 * <br><b>depending level: 0</b><br>
		 * 
		 * @param tabobject (<b>Javascript</b>) Object, the DOJO tab object.
		 * @param index (<b>Javascript</b>) int, the index of the DOJO tab object.
		 * @return Tab (<b>Javascript</b>) Object, the uniformed Tab object.
		 */
		static String dojo_initializeTabObject(){
			StringBuffer scriptCommand = new StringBuffer();
				
			scriptCommand.append("function dojo_initializeTabObject(tabobject, index){\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    if( tabobject!=undefined){\n");
			scriptCommand.append("      tab = new Object();\n");
			scriptCommand.append("      tab."+Item.PROPERTY_LABEL+"=tabobject.title;\n");
			scriptCommand.append("      tab."+Item.PROPERTY_SELECTED+"=tabobject.selected;\n");
			scriptCommand.append("      tab."+Item.PROPERTY_ID+"=tabobject.id;\n");
			scriptCommand.append("      tab."+Item.PROPERTY_INDEX+"=index;\n");
			scriptCommand.append("      tab."+Item.PROPERTY_DISABLED+"=tabobject.disabled;\n");
			scriptCommand.append("      return tab;\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Get direct children of the TabContainerBase widget.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #dojo_dijit_WidgetBase_getChildren(boolean)} <br>
		 * {@link #dojo_initializeTabObject()} <br>
		 * <br><b>depending level: 3</b><br>
		 * 
		 * @param includeDependency boolean, if true, will return the depended js as part of result.
		 * @param domelement (<b>Javascript</b>) Object, the dom-element of DOJO object.
		 * @return childrenArray (<b>Javascript</b>) List<Tab>, an array of the widget's children
		 */
		public static String dojo_dijit_layout_TabContainerBase_getChildren(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(dojo_dijit_WidgetBase_getChildren(includeDependency));
				scriptCommand.append(dojo_initializeTabObject());
			}
			
			scriptCommand.append("function dojo_dijit_layout_TabContainerBase_getChildren(domelement){\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var properties = new Array();\n");
			scriptCommand.append("    items = dojo_dijit_WidgetBase_getChildren(domelement);//an array of tab's child\n");
			scriptCommand.append("    if(items != undefined){\n");
			scriptCommand.append("      //items has properties: id, text and selected\n");
			scriptCommand.append("      //if it contains more properties, we need to modify following code to get them.\n");
			scriptCommand.append("      if(items.length == undefined){\n");
			scriptCommand.append("        tab = dojo_initializeTabObject(items, 0);\n");
			scriptCommand.append("        properties.push(tab);\n");
			scriptCommand.append("      }else{\n");
			scriptCommand.append("        for(var i=0;i<items.length;i++){;\n");
			scriptCommand.append("          tab = dojo_initializeTabObject(items[i], i);\n");
			scriptCommand.append("          properties.push(tab);\n");
			scriptCommand.append("        }\n");
			scriptCommand.append("      }\n");
			scriptCommand.append("      return properties;\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Select a direct children of the StackContainer widget.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #getDojoObject(boolean)} <br>
		 * <br><b>depending level: 2</b><br>
		 * 
		 * @param includeDependency boolean, if true, will return the depended js as part of result.
		 * @param container (<b>Javascript</b>) Object, the dom-element of DOJO object.
		 * @param childId (<b>Javascript</b>) String, the id of the child to select.
		 */
		public static String dojo_dijit_layout_StackContainer_selectChild(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(getDojoObject(includeDependency));
			}
			
			scriptCommand.append("function dojo_dijit_layout_StackContainer_selectChild(container, childId){\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var object = getDojoObject(container);\n");
			scriptCommand.append("    if(object!=undefined  && (object instanceof dijit.layout.StackContainer ) ){\n");
			scriptCommand.append("      object.selectChild(childId);\n");
			scriptCommand.append("      return;\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
		
		/**
		 * Get the index of selected item in a StackContainer widget.<br>
		 * 
		 * <br><b>depending on:</b><br>
		 * {@link #getDojoObject(boolean)} <br>
		 * <br><b>depending level: 2</b><br>
		 * 
		 * @param includeDependency boolean, if true, will return the depended js as part of result.
		 * @param container (<b>Javascript</b>) Object, the dom-element of DOJO object.
		 * @return index (<b>Javascript</b>) int, the index of the selected item.
		 */
		public static String dojo_dijit_layout_StackContainer_getSelectedIndex(boolean includeDependency){
			StringBuffer scriptCommand = new StringBuffer();
			
			if(includeDependency){
				scriptCommand.append(getDojoObject(includeDependency));
			}
			
			scriptCommand.append("function dojo_dijit_layout_StackContainer_getSelectedIndex(container){\n");
			scriptCommand.append("  try{\n");
			scriptCommand.append("    var object = getDojoObject(container);\n");
			scriptCommand.append("    if(object!=undefined  && (object instanceof dijit.layout.StackContainer ) ){\n");
			scriptCommand.append("      return object.getIndexOfChild(object.selectedChildWidget);\n");
			scriptCommand.append("    }\n");
			scriptCommand.append("  }catch(error){\n");
			scriptCommand.append("    //alert(error);\n");
			scriptCommand.append("  }\n");
			scriptCommand.append("}\n");
			
			return scriptCommand.toString();
		}
	}
	
	public static final int LOG_LEVEL_INFO 		= 1;
	public static final int LOG_LEVEL_DEBUG 	= 2;
	public static final int LOG_LEVEL_DEFAULT 	= LOG_LEVEL_DEBUG;
	/**
	 * Set javascript's program log level
	 * @param logLevel
	 * @return
	 */
	public static String setJavaScriptLogLevel(int logLevel){
		StringBuffer scriptCommand = new StringBuffer();
		
		scriptCommand.append(" log_level="+logLevel+";\n");
		
		return scriptCommand.toString();
	}
	
	private static AtomicInteger atomicInteger = new AtomicInteger();
	/**
	 * Generate a javascript array filled with the values provided by Java String array.
	 * @param scriptCommand StringBuffer, the string buffer to store the javascript command
	 * @param needquoted boolean, if the value needs to be quoted before setting to the array
	 * @param tab String, the tab string to calculate the indent before each command in the string buffer
	 * @param numberOfTab int, the number of tab to calculate the indent before each command in the string buffer
	 * @param arrayValues String..., an array of value
	 * @return String, the generated javascript array name
	 * @see #sap_ComboBox_getItems(boolean)
	 */
	public static String initializeJSArray(StringBuffer scriptCommand, boolean needquoted, String tab, int numberOfTab, String... arrayValues){
		String indent = indent(tab, numberOfTab);
		
		String arrayName = "SAFS_Generated_JS_Array_"+System.currentTimeMillis()+"_"+atomicInteger.incrementAndGet();
		scriptCommand.append(indent+"var "+arrayName+" = new Array();\n");
		int i = 0;
		for(String value: arrayValues){
			if(needquoted) scriptCommand.append(indent+indent(tab,1)+arrayName+"["+ (i++) +"] = '"+value+"';\n");
			else{
				scriptCommand.append(indent+"try{\n");
				//Put the following js code in the try{} clause to avoid exception
				scriptCommand.append(indent+indent(tab,1)+arrayName+"["+ (i++) +"] = "+value+";\n");
				scriptCommand.append(indent+"}catch(error){ /* debug(error); */}\n");
			}
		}
		return arrayName;
	}
	
	/**
	 * Generate a javascript map filled with the pairs (key, value) provided by Java Map<String, String>.
	 * @param needquoted boolean, if the pair(key,value) needs to be quoted before setting to the map
	 * @param tab String, the tab string to calculate the indent before each command in the string buffer
	 * @param numberOfTab int, the number of tab to calculate the indent before each command in the string buffer
	 * @param keyValueMap Map<String, String>, an map of pair(key, value)
	 * @return String, the generated javascript array name
	 */
	public static String initializeJSMap(StringBuffer scriptCommand, boolean needquoted, String tab, int numberOfTab, Map<String, String> keyValueMap){
		String indent = indent(tab, numberOfTab);
		
		String mapName = "SAFS_Generated_JS_Map_"+System.currentTimeMillis()+"_"+atomicInteger.incrementAndGet();
		scriptCommand.append(indent+"var "+mapName+" = {};\n");
		String value = null;
		for(String key: keyValueMap.keySet()){
			value = keyValueMap.get(key);
			if(needquoted) scriptCommand.append(indent+mapName+"['"+key+"'] = '"+value+"';\n");
			else{
				scriptCommand.append(indent+"try{\n");
				//Put the following js code in the try{} clause to avoid exception
				scriptCommand.append(indent+indent(tab,1)+mapName+"["+ key +"] = "+value+";\n");
				scriptCommand.append(indent+"}catch(error){ /* debug(error); */}\n");
			}
		}

		return mapName;
	}
	
	/**
	 * Concatenate several 'Tab/Space' to form a string to serve as indent.
	 * @param tab String, the tab string or a few of space
	 * @param numberOfTab int, the number of tab
	 * @return String, the indent string
	 */
	private static String indent(String tab, int numberOfTab){
		StringBuffer indent = new StringBuffer();
		if(tab==null || !tab.trim().isEmpty()) tab="\t";
		for(int i=0;i<numberOfTab;i++){
			indent.append(tab);
		}
		return indent.toString();
	}
	
	public static String getAllFunctions(){
		StringBuffer definition = new StringBuffer();
		
		definition.append(setJavaScriptLogLevel(LOG_LEVEL_DEFAULT));
		definition.append(JavaScriptFunctions.getSAFSgetElementFromXpathFunction(false));
		definition.append(JavaScriptFunctions.getHighlightFunction(false));
		definition.append(JavaScriptFunctions.getSAFSgetAttributeFunction(false));
		definition.append(JavaScriptFunctions.highlight2());
		
		definition.append(objectIsInstanceof());
		
		definition.append(getDomElementBy());
		definition.append(fireMouseClick());
		definition.append(fireMouseEvent(new MouseEvent("listener")));
		definition.append(fireMouseHover());
		definition.append(fireMouseClickById(false));
		definition.append(getBrowserInformation());
		
		definition.append(getElementAbsoluteXPath());
		definition.append(defineGenericEventCallBack(INDICATOR_FOR_CALLBACK_CALLED_VAR));
		definition.append(removeGlobalVariable(INDICATOR_FOR_CALLBACK_CALLED_VAR));
		
		definition.append(addEventListener());
		definition.append(addGenericEventListener(false, INDICATOR_FOR_CALLBACK_CALLED_VAR));
		definition.append(removeEventListener());
		definition.append(removeGenericEventListener(false, INDICATOR_FOR_CALLBACK_CALLED_VAR));
		definition.append(getAttributes());
		definition.append(getHtmlProperties());
		
		definition.append(scrollTo());
		definition.append(scrollBy());
		definition.append(scrollByLines());
		definition.append(scrollByPages());
		definition.append(scrollIntoView());
		
		definition.append(sendHttpRequest(null));
		
		//All functions related to dojo 
		definition.append(getAllDojoFunctions());
		//All functions related to SAP OPENUI5 
		definition.append(getAllSAPFunctions());
		
		//Function for defining a javascript object according to a Java Hashtable object.
		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("value", "A'R");
		hash.put("attribute1", "H\"el'lo");
		hash.put("attribute2", new Date());
		definition.append(defineObject(hash));
		
		//Generate function to compare 2 objects by their properties provided by a list.
		List<String> properties = new ArrayList<String>();
		properties.add("value");
		properties.add("id");
		properties.add("name");
		definition.append(compareObject(properties));
		
		//Functions related to global error
		definition.append(throw_error());
//		definition.append(initJSError());
//		definition.append(getJSErrorCode());
//		definition.append(cleanJSError());
//		definition.append(setJSErrorCode(ERROR_CODE_NOT_SET));

		//Functions related to debug message array
		definition.append(debug());
//		definition.append(initJSDebugArray());
//		definition.append(getJSDebugArray());
		
		return definition.toString();
	}
	
	public static String getAllDojoFunctions(){
		StringBuffer definition = new StringBuffer();
		
		definition.append(DOJO.getDojoScopemap());
		definition.append(DOJO.getDojoClassNameByCSSSelector(false));
		definition.append(DOJO.getDojoClassNameById(false));
		definition.append(DOJO.getDojoObject(false));
		definition.append(DOJO.getDojoObjectByDomNode());
		definition.append(DOJO.getDojoObjectById());
		definition.append(DOJO.getDojoObjectsByCSSSelector(false));
		definition.append(DOJO.getDomNodesByCSSSelector());
		definition.append(DOJO.dojo_HasDropDown_isLoaded(false));
		definition.append(DOJO.dojo_HasDropDown_closeDropDown(false));
		definition.append(DOJO.dojo_HasDropDown_loadAndOpenDropDown(false));
		definition.append(DOJO.dojo_HasDropDown_openDropDown(false));
		definition.append(DOJO.dojo_FormSelectWidget_getOptions(false));
		definition.append(DOJO.dojo_dijit_WidgetBase_set(false));
		definition.append(DOJO.dojo_store_api_Store_query(false));
		definition.append(DOJO.dojo_objectIsInstanceof(false));
		definition.append(DOJO.dojo_dijit_WidgetBase_on(false, INDICATOR_FOR_CALLBACK_CALLED_VAR));
		definition.append(DOJO.dojo_handle_remove(false, INDICATOR_FOR_CALLBACK_CALLED_VAR));
		definition.append(DOJO.dojo_dijit_WidgetBase_getChildren(false));
		definition.append(DOJO.dojo_initializeTabObject());
		definition.append(DOJO.dojo_dijit_layout_TabContainerBase_getChildren(false));
		definition.append(DOJO.dojo_dijit_layout_StackContainer_selectChild(false));
		definition.append(DOJO.dojo_dijit_layout_StackContainer_getSelectedIndex(false));
		definition.append(DOJO.dojo_get_property(false));
		
		definition.append(DOJO.parse_dijit_form_Select_Option());
		definition.append(DOJO.parse_dijit_form_AutoCompleterMixin_Item(false));
		
		return definition.toString();
	}
	
	public static String getAllSAPFunctions(){
		StringBuffer definition = new StringBuffer();
		
		definition.append(SAP.sap_getProperty(false, "value"));
		definition.append(SAP.sap_getDOMRef());
		definition.append(SAP.sap_getObject());
		definition.append(SAP.sap_getObjectById());
		definition.append(SAP.getSAPClassNames());
		definition.append(SAP.getSAPClassNamesById(false));
		definition.append(SAP.getSAPClassNameById(false));
		definition.append(SAP.sap_objectIsInstanceof(false));
		
		definition.append(SAP.sap_ComboBox_getItems(false));
		definition.append(SAP.sap_ComboBox_setSelectedKey(false));
		
		definition.append(SAP.sap_ui_commons_CheckBox_setChecked(false));
		definition.append(SAP.sap_ui_commons_CheckBox_getChecked(false));
		
		definition.append(SAP.sap_ui_core_Control_attachBrowserEvent(false, INDICATOR_FOR_CALLBACK_CALLED_VAR));
		definition.append(SAP.sap_ui_core_Control_detachBrowserEvent(false, INDICATOR_FOR_CALLBACK_CALLED_VAR));
		
		definition.append(SAP.sap_ui_commons_xxx_getSelectedIndex(false));
		
		definition.append(SAP.sap_ui_commons_TabStrip_getTabs(false));
		definition.append(SAP.sap_ui_commons_TabStrip_setSelectedIndex(false));
		
		definition.append(SAP.sap_ui_commons_ListBox_getItems(false));
		definition.append(SAP.sap_ui_commons_ListBox_setSelectedIndex(false));
		definition.append(SAP.sap_ui_commons_ListBox_getSelectedIndices(false));
		definition.append(SAP.sap_ui_commons_ListBox_scrollToIndex(false));
		definition.append(SAP.sap_m_List_getItems(false));
		definition.append(SAP.sap_m_List_getSelectedItems(false));
		definition.append(SAP.sap_m_List_setSelectedItemById(false));
		
		definition.append(SAP.sap_ui_commons_Tree_collapseAll(false));
		definition.append(SAP.sap_ui_commons_Tree_expandAll(false));
		definition.append(SAP.sap_ui_commons_Tree_getNodes(false));
		definition.append(SAP.sap_ui_commons_Tree_getNodes_Rec(false));		
		definition.append(SAP.sap_ui_commons_TreeNode_collapse(false));
		definition.append(SAP.sap_ui_commons_TreeNode_expand(false));
		definition.append(SAP.sap_ui_commons_TreeNode_select(false));
		definition.append(SAP.sap_ui_commons_TreeNode_showOnPage(false));
		definition.append(SAP.sap_ui_commons_TreeNode_getIsSelected(false));		
		definition.append(SAP.sap_ui_commons_TreeNode_getExpanded(false));		
		definition.append(SAP.sap_ui_commons_TreeNode_getSelectable(false));		
		definition.append(SAP.sap_ui_commons_TreeNode_refresh(false));
		
		definition.append(SAP.sap_ui_core_ScrollBar_page(false));
		definition.append(SAP.sap_ui_core_ScrollBar_scroll(false));
		
		definition.append(SAP.sap_ui_commons_Menu_getItems(false));
		definition.append(SAP.sap_ui_commons_Menu_getItems_Rec(false));
		definition.append(SAP.parse_sap_ui_commons_MenuItem(false));

		definition.append(SAP.parse_sap_ui_commons_Tab());
		definition.append(SAP.parse_sap_ui_core_Item());
		definition.append(SAP.parse_sap_ui_commons_TreeNode());
		definition.append(SAP.parse_sap_m_ListItemBase());
		
		definition.append(SAP.sas_hc_ui_commons_pushmenu_PushMenu_getItems(false));
		definition.append(SAP.sas_hc_ui_commons_pushmenu_PushMenu_getItems_Rec(false));
		definition.append(SAP.parse_sas_hc_ui_commons_pushmenu_PushMenuItemBase());
		
		return definition.toString();
	}

	/** !!! UNTESTED AND LIKELY NOT WORKING AT THIS TIME !!! */
	protected static String getElementAbsoluteXPath(){
		return  "function absoluteXPath(element) {"+
					"var comp, comps = [];"+
					"var parent = null;"+
					"var xpath = '';"+
					"var getPos = function(element) {"+
						"var position = 1, curNode;"+
						"if (element.nodeType == Node.ATTRIBUTE_NODE) {"+
							"return null;"+
						"}"+
						"for (curNode = element.previousSibling; curNode; curNode = curNode.previousSibling) {"+
							"if (curNode.nodeName == element.nodeName) {"+
								"++position;"+
							"}"+
						"}"+
						"return position;"+
					"};"+
					"if (element instanceof Document) {"+
						"return '/';"+
					"}"+
					"for (; element && !(element instanceof Document); element = element.nodeType == Node.ATTRIBUTE_NODE ? element.ownerElement : element.parentNode) {"+
						"comp = comps[comps.length] = {};"+
						"switch (element.nodeType) {"+
							"case Node.TEXT_NODE:"+
								"comp.name = 'text()';"+
								"break;"+
							"case Node.ATTRIBUTE_NODE:"+
								"comp.name = '@' + element.nodeName;"+
								"break;"+
							"case Node.PROCESSING_INSTRUCTION_NODE:"+
								"comp.name = 'processing-instruction()';"+
								"break;"+
							"case Node.COMMENT_NODE:"+
								"comp.name = 'comment()';"+
								"break;"+
							"case Node.ELEMENT_NODE:"+
								"comp.name = element.nodeName;"+
								"break;"+
						"}"+
						"comp.position = getPos(element);"+
					"}"+				
					"for (var i = comps.length - 1; i >= 0; i--) {"+
						"comp = comps[i];"+
						"xpath += '/' + comp.name.toLowerCase();"+
						"if (comp.position !== null) {"+
							"xpath += '[' + comp.position + ']';"+
						"}"+
					"}"+				
					"return xpath;"+				
				"} return absoluteXPath(arguments[0]);";
	}
	
	public static void main(String[] args){
		System.out.println(JavaScriptFunctions.getAllFunctions());
		try {
			FileUtilities.writeStringToUTF8File("SeleniumPlus.js", JavaScriptFunctions.getAllFunctions());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
