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
 * JAN 16, 2018    (Lei Wang) Modified setLocatorParam(): parse the locator in JSON format.
 * JAN 18, 2018    (Lei Wang) Modified setLocatorParam(): parse the locator provided as Map object or as Map.toString().
 */
package org.safs.selenium.webdriver.lib.interpreter.selrunner;

import java.util.Map;

import org.safs.IndependantLog;
import org.safs.StringUtils;
import org.safs.Utils;
import org.safs.selenium.webdriver.lib.interpreter.WDLocator;
import org.safs.selenium.webdriver.lib.interpreter.WDScriptFactory;
import org.safs.selenium.webdriver.lib.interpreter.WDTestRun;
import org.safs.text.Comparator;

import com.sebuilder.interpreter.Step;

/**
 * Utility to convert SeRunner HTML FIT Tables into suitable SeInterpreter
 * Script objects for execution.
 *
 * @author Carl Nagle
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
	static final String json = "{";

	public static void setLocatorParam(Step step, String rs){
		setLocatorParam(step, rs, WDScriptFactory.LOCATOR_PARAM);
	}

	/**
	 * @param step Step, a step in sebuilder's script
	 * @param locatorObj Object, the locator object.
	 * @param locatorParamName String, the name of locator parameter, it could be 'locator' or 'locator2' etc.
	 */
	public static void setLocatorParam(Step step, Object locatorObj, String locatorParamName){
		String debugmsg = StringUtils.debugmsg(false);

		if(locatorObj instanceof String){
			setLocatorParam(step, locatorObj.toString(), locatorParamName);
		}else if(locatorObj instanceof Map){
			try{
				String type = null;
				String value = null;
				//the 'locatorObj' might be Map, such as {type=map, value=${Map:FormsMain:SingleLineText_test}}
				Map<?, ?> locatorMap = (Map<?, ?>) locatorObj;
				type = locatorMap.get(WDLocator.NAME_TYPE).toString();
				value = locatorMap.get(WDLocator.NAME_VALUE).toString();
				setLocatorParam(step, locatorParamName, type, value);
			}catch(Exception e){
				IndependantLog.debug(debugmsg+" failed to parse locator string '"+locatorObj+"'");
			}
		}
	}

	/**
	 * @param step Step, a step in sebuilder's script
	 * @param rs String, the locator string
	 * @param locatorParamName String, the name of locator parameter, it could be 'locator' or 'locator2' etc.
	 */
	public static void setLocatorParam(Step step, String rs, String locatorParamName){
		String type = null;
		String value = null;

		if(rs.startsWith(xp)){
			type = WDScriptFactory.XPATH_LOCATORTYPE;
			value = rs;
		}else if(rs.startsWith(WDTestRun.VARREF_START)){
			type = WDScriptFactory.MAP_LOCATORTYPE;
			value = rs;
		}else if(rs.startsWith(json)){
			//the locator 'rs' might be in JSON format, such as {"type":"map", "value":"${Map:FormsMain:SingleLineText_test"}}
			try{
				Map<String, Object> locatorMap = Utils.fromJsonString(rs, Map.class);
				type = (String) locatorMap.get(WDLocator.NAME_TYPE);
				value = (String) locatorMap.get(WDLocator.NAME_VALUE);
			}catch(Exception e){
				try{
					//{type=map, value=${Map:FormsMain:SingleLineText_test}} is the string format got from Map.toString(), and it cannot be
					//converted to Map as a JSON string, we have to parse it ourselves
					IndependantLog.warn("Failed to parse locator JSON string '"+rs+"', met "+e);
					//remove '{' and '}'
					rs = rs.trim();
					rs = rs.substring(1, rs.length()-1);
					String[] params = rs.split(",");
					String[] pair = null;
					for(int i=0;i<params.length;i++){
						pair = params[i].split(eq);
						if(WDLocator.NAME_TYPE.equalsIgnoreCase(pair[0].trim())) type = pair[1].trim();
						if(WDLocator.NAME_VALUE.equalsIgnoreCase(pair[0].trim())) value = pair[1].trim();
					}
				}catch(Exception e1){
					IndependantLog.error("Failed to parse locator JSON string '"+rs+"', met "+e1);
				}
			}

		}else{
			if(rs.contains(eq)){
				int i = rs.indexOf(eq);
				type = rs.substring(0,i).trim();
				value = rs.substring(i+1).trim();

				if(WDScriptFactory.CSS_LOCATORTYPE.equalsIgnoreCase(type)){
					type=WDScriptFactory.CSSSELECTOR_LOCATORTYPE;
				}else if(WDScriptFactory.XPATH_LOCATORTYPE.equalsIgnoreCase(type)){
					type=WDScriptFactory.XPATH_LOCATORTYPE;
				}
			}
		}

		setLocatorParam(step, locatorParamName, type, value);
	}

	/**
	 * Set step's locator by type and value.
	 * @param step Step, a step in sebuilder's script
	 * @param locatorParamName String, the name of locator parameter, it could be 'locator' or 'locator2' etc.
	 * @param type String, the type of the locator
	 * @param value String, the value of the locator
	 */
	public static void setLocatorParam(Step step, String locatorParamName, String type, String value){
		String debugmsg = StringUtils.debugmsg(false);
		if(type==null || value == null)
			throw new RuntimeException(
					"SRUtilities did not successfully process Locator parameter for StepType "+
							step.type.getClass().getSimpleName());

		try{
			IndependantLog.info(debugmsg+ step.type.getClass().getName()+" to receive WDLocator Type '"+type+"' with value '"+value+"'.");
			step.locatorParams.put(locatorParamName, new WDLocator(type, value));
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
