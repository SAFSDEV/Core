/** 
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.selenium.webdriver.lib.interpreter.selrunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.selenium.webdriver.lib.interpreter.WDLocator;
import org.safs.selenium.webdriver.lib.interpreter.WDScriptFactory;
import org.safs.selenium.webdriver.lib.interpreter.WDTestRunFactory;
import org.safs.text.Comparator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.factory.StepTypeFactory;

/**
 * Utility to convert SeRunner HTML FIT Tables into suitable SeInterpreter 
 * Script objects for execution.
 *  
 * @author canagl
 */
public class SRUtilities {

	public static final String GLOB_PATTERN    = "glob:";
	public static final String REGEXP_PATTERN  = "regexp:";
	public static final String REGEXPI_PATTERN = "regexpi:";
	public static final String EXACT_PATTERN   = "exact:";
    
	static final String s = "*";
    static final String q = "?";
	static final String eq = "=";
	static final String xp = "//";

	public static void setLocatorParam(Step step, String rs){
		String debugmsg = StringUtils.debugmsg(false);
		String type = null;
		String value = null;
		
		if(rs.startsWith(xp)){
			type = WDScriptFactory.XPATH_LOCATORTYPE;
			value = rs;
		}else{
			if(rs.contains(eq)){
				int i = rs.indexOf(eq);
				type = rs.substring(0,i).trim();				
				value = rs.substring(i+1).trim();
				
				if(WDScriptFactory.CSS_LOCATORTYPE.equalsIgnoreCase(type)){
					type=WDScriptFactory.CSSSELECTOR_LOCATORTYPE;
				}
			}
		}
		
		if(type==null || value == null) 
			throw new RuntimeException(
					"SRUtilities did not successfully process Locator parameter for StepType "+
			        step.type.getClass().getSimpleName() +" using parameter "+ rs);
		
		try{
			IndependantLog.info(debugmsg+ step.type.getClass().getName()+" to receive WDLocator Type '"+type+"' with value '"+value+"'.");
			step.locatorParams.put(WDScriptFactory.LOCATOR_PARAM, new WDLocator(type, value));
		}catch(Throwable t){
			IndependantLog.debug(debugmsg+ step.type.getClass().getName()+" "+ t.getClass().getSimpleName()+", "+ t.getMessage());
			throw t;
		}
	}
	
	/**
	 * @param text
	 * @return true if the text contains a special Selenium 1.0 String-match Pattern. false otherwise.
	 */
	public static boolean containsStringMatchPattern(String text){
		return (text == null) ? false : (text.startsWith(GLOB_PATTERN) ||
		                                 text.startsWith(EXACT_PATTERN) ||
		                                 text.startsWith(REGEXP_PATTERN) ||
		                                 text.startsWith(REGEXPI_PATTERN));
	}
	
	public static boolean containsGlobMatchWildcards(String text){
	    if(text == null) return false;
	    return (text.contains(s))||(text.contains(q));
	}
	
	public static boolean isGlobMatchPattern(String text){
	    return (text == null) ? false : text.startsWith(GLOB_PATTERN) ||
	    		                        (! text.startsWith(EXACT_PATTERN) &&
	    		                         ! text.startsWith(REGEXPI_PATTERN) &&
	    		                         ! text.startsWith(REGEXP_PATTERN)
	    		                         );
	}	
	public static boolean isExactMatchPattern(String text){
	    return (text == null) ? false : text.startsWith(EXACT_PATTERN);
	}
	public static boolean isRegexpMatchPattern(String text){
	    return (text == null) ? false : text.startsWith(REGEXP_PATTERN);
	}
	public static boolean isRegexpiMatchPattern(String text){
	    return (text == null) ? false : text.startsWith(REGEXPI_PATTERN);
	}

	public static String stripStringMatchPatternPrefix(String pattern){
		if(pattern == null) return null;
	    if(pattern.startsWith(REGEXP_PATTERN)) 
	    	return pattern.substring(REGEXP_PATTERN.length());
	    if(pattern.startsWith(EXACT_PATTERN)) 
	    	return pattern.substring(EXACT_PATTERN.length());
	    if(pattern.startsWith(REGEXPI_PATTERN)) 
	    	return pattern.substring(REGEXPI_PATTERN.length());
	    if(pattern.startsWith(GLOB_PATTERN)) 
	    	return pattern.substring(GLOB_PATTERN.length());
	    // default when no pattern prefix present
	    return pattern;
	}
	
	/**
	 * Attempt a Selenium 1.0 pattern match on a given text string.
	 * <p>
	 * Selenium 1.0 patterns are:
	 * <p>
	 * <ul>
	 *     <li>glob:pattern, supports &quot;*&quot; and &quot;?&quot;.
	 *     <li>regexp:regexp, matches based on the regular expression.
	 *     <li>regexpi:regexpi, matches based on case-insensitive regular expression.
	 *     <li>exact:string, an exact verbatim match.
	 * </ul>
	 * If no pattern is specified, then 'glob' is assumed to be default.
	 * <p>
	 * @param text String to compare against the pattern
	 * @param pattern String that may include the pattern prefix for the type of comparison to be 
	 * performed.  If no pattern prefix is present, then 'glob' is assumed.
	 * 
	 * @return true if the text matches the provided pattern. false otherwise.
	 * if both parameters are null this is considered a match.
	 */
	public static boolean patternMatch(String text, String pattern){
	    if(text == null && pattern == null) return true;
	    if(text == null || pattern == null) return false;
	    if(pattern.startsWith(REGEXP_PATTERN)) 
	    	return Comparator.isRegexpMatch(text, pattern.substring(REGEXP_PATTERN.length()));
	    if(pattern.startsWith(EXACT_PATTERN)) 
	    	return Comparator.isExactMatch(text, pattern.substring(EXACT_PATTERN.length()));
	    if(pattern.startsWith(REGEXPI_PATTERN)) 
	    	return Comparator.isRegexpiMatch(text, pattern.substring(REGEXPI_PATTERN.length()));
	    if(pattern.startsWith(GLOB_PATTERN)) 
	    	return Comparator.isGlobMatch(text, pattern.substring(GLOB_PATTERN.length()));
	    // default when no pattern prefix present
		return Comparator.isGlobMatch(text, pattern);
	}
}
